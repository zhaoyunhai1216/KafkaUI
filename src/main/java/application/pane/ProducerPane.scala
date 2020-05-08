package application.pane

import java.text.SimpleDateFormat
import java.util
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Date, Map, UUID}

import application.Async
import application.commons.RunnablePane
import application.controllers.ProducerController
import application.element.{FxChart, Javafx, Row}
import application.utils.KafkaUtils
import javafx.scene.layout.Pane
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}
import org.apache.kafka.common.{Node, TopicPartition, TopicPartitionInfo}
import org.apache.logging.log4j.LogManager
import org.slf4j.{Logger, LoggerFactory}

/**
 * @title: ProducerPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/289:29
 */
class ProducerPane(
  root: AppPane,
  cluster: KafkaCluster,
  bootstrap: String,
  tp: TopicPartition
) extends RunnablePane[ProducerController]("/producer.fxml", cluster, "Producer-" + tp.toString) {
  val isLoading = new AtomicBoolean(false)
  val fxChart = new FxChart("发送数据量", () => getChartValue())
  var lastOffset = 0L
  var offset = 0L
  val records = controller.historyView.getItems
  var producer: KafkaProducer[Array[Byte], Array[Byte]] = _
  val logger = LogManager.getLogger(this.getClass)
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS")

  /**
   * 获取字符串表示信息
   *
   * @return
   */
  override def toString: String = {
    if (tp.partition() == -1)
      "Producer-" + tp.topic()
    else
      name
  }

  /**
   * 初始化集群展示组件
   */
  @throws[Exception]
  def init(): Pane = {
    controller.lineChart.getData.add(fxChart.series)
    load()
    this.pane
  }


  /**
   * 刷新页面
   */
  override def refresh(): Unit = {
    Async.runMoreLater(
      () => fxChart.clear(),
      () => load(),
      () => clear()
    )
    logger.info(s"[${cluster.name}] 正在刷新<$name>页面信息.")
  }

  /**
   * 清理页面
   */
  def clear(): Unit = {
    if (state()) return
    controller.keyFiled.clear()
    controller.textArea.clear()
    controller.historyView.getItems.clear()
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
   * 加载数据
   */
  def load(): Unit = {
    if (isLoading.get()) return
    loading(true)
    KafkaUtils
      .topics(cluster.name, cluster.adminClient)
      .thenAcceptAsync(topics => loadTopic(topics))
      .whenCompleteAsync((_, e) => loading(false, e))
  }


  /**
   * 加载主题信息
   *
   * @param topics
   */
  def loadTopic(
    topics: Map[String, TopicDescription])
  : Unit = {
    val topic = topics.get(tp.topic())
    if (tp.partition() == -1)
      partitions(topic)
    else
      replicas(getPartition(topic.partitions()))
  }

  /**
   * 获取当前的分区信息
   *
   * @param partitions
   */
  def getPartition(
    partitions: util.List[TopicPartitionInfo])
  : TopicPartitionInfo = {
    partitions.stream().filter(p =>
      p.partition() == tp.partition()
    ).findFirst().get()
  }

  /**
   * 加载分区信息数据
   */
  def partitions(
    topic: TopicDescription): Unit = {
    Async.runLater(() => controller.partitions.getChildren.clear())
    topic.partitions().forEach(x =>
      partition(topic.partitions(), x)
    )
  }

  /**
   * 加载分区信息数据
   */
  def partition(
    list: util.List[TopicPartitionInfo],
    tp: TopicPartitionInfo
  ): Unit = {
    val leader = tp.leader()
    val label = Javafx.label(tp.partition().toString, leader.toString)
    val list = controller.partitions.getChildren
    Async.runLater(() => list.add(label))
  }

  /**
   * 加载分区信息数据
   */
  def replicas(
    _partition: TopicPartitionInfo)
  : Unit = {
    Async.runLater(() => controller.partitions.getChildren.clear())
    _partition.replicas().forEach(x => replica(_partition, x))
  }

  /**
   * 加载分区信息数据
   */
  def getReplicaState(
    tp: TopicPartitionInfo,
    n: Node)
  : String = {
    if (tp.leader().equals(n))
      "red"
    else if (tp.isr().contains(n))
      "yellow"
    else
      "#78e43a"
  }

  /**
   * 加载分区信息数据
   */
  def replica(
    tp: TopicPartitionInfo,
    node: Node
  ): Unit = {
    val color = getReplicaState(tp, node)
    val label = Javafx.label(node.id().toString, node.toString, color)
    val list = controller.partitions.getChildren
    Async.runLater(() => list.add(label))
  }


  /**
   * 获取本次增长值
   *
   * @return
   */
  def getChartValue(): Long = {
    controller.historyView.refresh()
    load()
    val value = offset - lastOffset
    lastOffset = offset
    value
  }


  /**
   * 线程休眠
   */
  def sleep(): Unit = {
    val name = controller.unitFiled.getSelectionModel.getSelectedItem
    TimeUnit.valueOf(name).sleep(controller.intervalFiled.getText.toLong)
  }

  /**
   * 执行条件
   *
   * @return
   */
  def condition(): Boolean = {
    isRunning &&
      (controller.timeFiled.getText.toLong == 0L
        || offset < controller.timeFiled.getText.toLong)
  }

  /**
   * 执行任务
   */
  override def run()
  : Unit = {
    producer = KafkaUtils.getProducer(bootstrap)
    if(records.isEmpty) insertChart()
    while (condition()) {
      sendAll()
      sleep()
    }
    producer.close()
  }

  /**
   * 执行操作
   */
  def sendAll(): Unit = {
    fxChart.refresh()
    controller.historyView.getItems
      .forEach(x => sendIfNeed(x))
  }

  /**
   * 发送指定的行
   */
  def sendIfNeed(
    row: Row)
  :Unit = {
    offset = offset + 1
    producer.send(getRecord(row))
    row.putObject("times", row.getString("times").toLong + 1)
  }

  /**
   * 获取key分组情况
   */
  def getkey(row: Row)
  : Array[Byte] = {
    val key = row.getString("Key")
    if (key.equals("")) null else key.getBytes()
  }

  /**
   * 获取写入记录信息
   */
  def getRecord(row: Row)
  : ProducerRecord[Array[Byte], Array[Byte]] = {
    val value = row.getString("Value").getBytes
    if (tp.partition() == -1)
      new ProducerRecord(tp.topic(), getkey(row), value)
    else
      new ProducerRecord(tp.topic(), tp.partition(), getkey(row), value)
  }


  /**
   * 插入延迟信息
   */
  def insertChart(): Unit = {
    val row = new Row()
    row.putObject("Timetamp", dateFormat.format(new Date()))
    row.putObject("Key", controller.keyFiled.getText)
    row.putObject("Value", controller.textArea.getText)
    row.putObject("times", 0)
    records.add(row)
  }


  /**
   * 选中重发
   */
  def onSelected(): Unit = {
    val row = controller.historyView.getSelectionModel.getSelectedItem
    if (row == null) return
    controller.keyFiled.setText(row.getString("Key"))
    controller.textArea.setText(row.getString("Value"))
  }
}
