package application.pane

import java.util
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean

import application.Async
import application.commons.RunnablePane
import application.controllers.GroupController
import application.element.{FxChart, Javafx, Row}
import application.utils.KafkaUtils
import javafx.collections.FXCollections
import javafx.scene.chart.XYChart
import javafx.scene.control.{Label, ProgressIndicator}
import javafx.scene.layout.Pane
import org.apache.kafka.clients.admin.{ConsumerGroupDescription, MemberDescription}
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.TopicPartition
import org.slf4j.{Logger, LoggerFactory}

/**
 * @title: GroupPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2716:25
 */
class GroupPane(
  root: AppPane,
  cluster: KafkaCluster,
  bootstrap: String,
  g: ConsumerGroupDescription) extends RunnablePane[GroupController]("/group.fxml", cluster, "Group-" + g.groupId()) {
  var rows = FXCollections.observableArrayList[Row]
  val isLoading = new AtomicBoolean(false)
  val fxChart = new FxChart("读取数据量", () => getChartValue())
  var lastOffset = -1L
  var offset = 0L
  val logger: Logger = LoggerFactory.getLogger(this.getClass)
  override def toString = name

  /**
   * 初始化页面
   *
   * @return
   */
  override def init(): Pane = {
    controller.lineChart.getData.add(fxChart.series)
    load()
    controller.tableView.setItems(rows)
    pane
  }

  /**
   * 设置加载状态
   */
  def loading(
    visible: Boolean,
    e: Throwable = null)
  : Unit = {
    if (e != null) {
      e.printStackTrace()
    }
    this.isLoading.compareAndSet(!visible, visible)
    Async.runLater(() => controller.loading.setVisible(visible))
  }

  /**
   * 刷新页面
   *
   * @return
   */
  def load(): Unit = {
    if (isLoading.get()) return
    loading(true)
    KafkaUtils
      .groupMetadata(cluster.name, cluster.adminClient, g.groupId())
      .thenAcceptAsync(t => setState(t))
      .whenCompleteAsync((_, e) => loading(false, e))
  }

  /**
   * 设置状态字段
   */
  def setState(
    group: ConsumerGroupDescription
  ): Unit = {
    resetTable(group).get()
    Async.runMoreLater(
      () => controller.groupFiled.setText(group.groupId()),
      () => controller.assignorFiled.setText(group.partitionAssignor()),
      () => controller.stateFiled.setText(group.state().toString),
      () => controller.leaderFiled.setText(group.coordinator().toString)
    )
  }

  /**
   * 加载数据
   */
  def resetTable(
    desc: ConsumerGroupDescription
  ): CompletableFuture[_] = {
    KafkaUtils
      .groupOffset(cluster.name, cluster.adminClient, g.groupId())
      .thenAcceptAsync(x=>setTable(x,desc))
  }

  /**
   * 设置表数据信息
   */
  def setTable(
    offsets: util.Map[TopicPartition, OffsetAndMetadata],
    desc: ConsumerGroupDescription
  ): Unit = {
    setLastOffset(offsets)
    fxChart.refresh()
    rows.clear();
    desc.members().forEach(x => setRows(offsets, x))
  }

  /**
   * 添加行数据信息
   */
  def setRows(
    offsets: util.Map[TopicPartition, OffsetAndMetadata],
    m: MemberDescription)
  : Unit = {
    m.assignment().topicPartitions().forEach(x => serRow(offsets, m, x))
  }

  /**
   * 设置缓存的offset
   */
  def setLastOffset(
    offsets: util.Map[TopicPartition, OffsetAndMetadata]
  ): Unit ={
    offset = offsets.values().stream().mapToLong(x => x.offset()).sum()
    if(lastOffset == -1)
      lastOffset = offset
  }
  /**
   * 运行
   */
  override def run(): Unit = {
    while (isRunning) {
      load()
      Thread.sleep(1000)
    }
  }

  /**
   * 获取本次增长值
   *
   * @return
   */
  def getChartValue(): Long = {
    load()
    val value = offset - lastOffset
    lastOffset = offset
    value
  }

  /**
   * 刷新页面
   *
   * @return
   */
  override def refresh(): Unit = {
    Async.runMoreLater(
      () => fxChart.clear(),
      () => load(),
      () => controller.tableFilter.clear()
    )
    logger.info(s"[${cluster.name}] 正在刷新<$name>页面信息.")
  }

  /**
   * 销毁
   *
   * @return
   */
  override def destroy(): Unit = {
    isRunning = false
  }


  /**
   * 过滤后进行展示数据
   */
  def filter(filter: String): Unit = {
    val filtered = rows.filtered(x => x.contains(filter))
    Async.runLater(() => controller.tableView.setItems(filtered))
  }


  /**
   * 添加行数据信息
   */
  def serRow(
    offsets: util.Map[TopicPartition, OffsetAndMetadata],
    m: MemberDescription,
    tp: TopicPartition
  )
  : Unit = {
    val offset = offsets.get(tp);
    val row = new Row()
    rows.add(row)
    row.putObject("host", m.host())
    row.putObject("client", m.clientId())
    row.putObject("topic", tp.topic())
    row.putObject("partition", tp.partition())
    if (offset != null)
      row.putObject("offset", offset.offset())
  }
}
