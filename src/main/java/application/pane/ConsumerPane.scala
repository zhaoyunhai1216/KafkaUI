package application.pane

import java.time.Duration
import java.util
import java.util.concurrent.atomic.AtomicBoolean
import java.util.{Map, UUID}

import application.Async
import application.commons.RunnablePane
import application.controllers.ConsumerController
import application.element.{FxChart, FxOutput, Javafx, Row}
import application.utils.KafkaUtils
import javafx.scene.chart.XYChart
import javafx.scene.layout.Pane
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.clients.consumer.{ConsumerRecord, KafkaConsumer}
import org.apache.kafka.common.{Node, TopicPartition, TopicPartitionInfo}
import org.apache.logging.log4j.LogManager
import org.slf4j.{Logger, LoggerFactory}

/**
 * @title: TopicPanel
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/3/25 21:27
 */
class ConsumerPane(
  root: AppPane,
  cluster: KafkaCluster,
  bootstrap: String,
  tp: TopicPartition) extends RunnablePane[ConsumerController]("/consumer.fxml", cluster, "Consumer-" + tp.toString) {
  val isLoading = new AtomicBoolean(false)
  val fxChart = new FxChart("接收数据量", () => getChartValue())
  var lastOffset = 0L
  var offset = 0L
  val records = controller.recordView.getItems
  var consumer: KafkaConsumer[Array[Byte], Array[Byte]] = _
  val logger = LogManager.getLogger(this.getClass)
  val console = new FxOutput(controller.recordView, controller.keepSize)

  /**
   * 获取字符串表示信息
   *
   * @return
   */
  override def toString: String = {
    if (tp.partition() == -1)
      "Consumer-" + tp.topic()
    else
      name
  }

  /**
   * 初始化集群展示组件
   */
  @throws[Exception]
  def init(): Pane = {
    controller.lineChart.getData.add(fxChart.series)
    controller.groupIdFiled.setText(UUID.randomUUID().toString)
    load()
    this.pane
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
   * 刷新页面
   */
  override def refresh(): Unit = {
    Async.runMoreLater(
      () => fxChart.clear(),
      () => load(),
      () => console.clear()
    )
    logger.info(s"[${cluster.name}] 正在刷新<$name>页面信息.")
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
    load()
    val value = offset - lastOffset
    lastOffset = offset
    value
  }

  /**
   * 任务执行方法
   */
  override def run(): Unit = {
    initConsumer()
    while (isRunning) {
      val records = consumer.poll(Duration.ofMillis(100))
      records.forEach(x => write(x))
      offset = offset + records.count()
      fxChart.refresh()
    }
    consumer.close()
  }

  /**
   * 定于信息
   */
  def initConsumer(): Unit = {
    val groupId = controller.groupIdFiled.getText
    consumer = KafkaUtils.getConsumer(bootstrap, groupId)
    if (tp.partition() != -1) {
      consumer.assign(util.Arrays.asList(tp))
    } else {
      consumer.subscribe(util.Arrays.asList(tp.topic()))
    }
  }

  /**
   * 写入面板
   *
   * @param record
   */
  def write(
    record: ConsumerRecord[Array[Byte], Array[Byte]])
  : Unit = {
    console.println(record)
  }

  /**
   * 加载数据
   */
  def filter(filter: String): Unit = {
    val filtered = records.filtered(x => x.contains(filter))
    Async.runLater(() => controller.recordView.setItems(filtered))
  }

}
