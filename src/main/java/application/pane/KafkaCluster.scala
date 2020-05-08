package application.pane

import java.time.Duration
import java.util
import java.util.Map
import java.util.concurrent.CompletableFuture
import java.util.concurrent.atomic.AtomicBoolean
import java.util.stream.Collectors

import application.Async
import application.commons.{BasePane, ModulePane, RunnablePane}
import application.controllers.ClusterController
import application.element.{CustomTreeItem, Javafx, Picture, Row}
import application.utils.{KafkaUtils, UtilCommons}
import javafx.collections.FXCollections
import javafx.scene.control.{Button, TreeView}
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import org.apache.kafka.clients.admin
import org.apache.kafka.clients.admin.{ConsumerGroupDescription, TopicDescription}
import org.apache.kafka.common.{Node, TopicPartition, TopicPartitionInfo}
import org.apache.logging.log4j.LogManager
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.JavaConversions._
import scala.collection.immutable.Stream

/**
 * @title: KafkaCluster
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/3/2418:03
 */
class KafkaCluster(
  root: AppPane, val button: Button, row: Row)
  extends BasePane[ClusterController]("/cluster1.fxml", row.getString("name")) {
  val logger = LogManager.getLogger(this.getClass)
  val bootstrap = row.getString("address")
  var adminClient: admin.AdminClient = _
  var isHome: Boolean = row.isHome
  var tasks = FXCollections.observableArrayList[ModulePane[_]]
  val isLoading = new AtomicBoolean(false)

  /**
   * 初始化集群展示组件
   */
  def init(): Pane = {
    showHome()
    if (row.isHome) return this.pane
    adminClient = KafkaUtils.getAdminClient(name, bootstrap)
    load()
    this.pane
  }

  /**
   * 找到指定名称的pane
   *
   * @param name
   */
  def find(
    name: String)
  : scala.Option[ModulePane[_]] = {
    tasks.find(p => p.name.equals(name))
  }

  /**
   * 任务列表删除任务
   *
   * @return
   */
  def resize(
    newSize: Int): Unit = {
    val cnt = tasks.stream().filter(x => canRemove(x)).count().toInt
    for (_ <- 0 until Math.max(cnt - newSize, 0)) {
      removeFirst()
    }
  }

  /**
   * 删除第一个未运行的任务
   *
   * @return
   */
  def removeFirst(): Unit = {
    for (pane <- tasks if canRemove(pane)) {
      remove(pane)
      return
    }
  }


  /**
   * 任务列表删除任务
   *
   * @return
   */
  def remove(
    pane: ModulePane[_]
  ): Unit = {
    tasks.remove(pane)
  }

  /**
   * 删除指定面板
   */
  def removeIfNeed()
  : Unit = {
    for(i <- 0 until tasks.size() reverse)
      removeIfNeed(tasks(i))
  }

  /**
   * 删除指定面板
   */
  def removeIfNeed(
    pane: ModulePane[_])
  : Unit = {
    if (!canRemove(pane)) return
    tasks.remove(pane)
    controller.details.getChildren.remove(pane.pane)
  }

  /**
   * 指定面板是否可以删除(没运行并且不是主页)
   */
  def canRemove(
    pane: ModulePane[_])
  : Boolean = {
    !isRunning(pane) && !pane.visible() && !pane.name.equals("HOME")
  }

  /**
   * pane是否正在运行
   */
  def isRunning(
    pane: ModulePane[_])
  : Boolean = {
    pane.isInstanceOf[RunnablePane[_]] && pane.asInstanceOf[RunnablePane[_]].state()
  }

  /**
   * 显示指定集群, 其他集群进行隐藏处理
   */
  def displayIfNeed[T](
    key: String, f: () => Option[ModulePane[_]])
  : Unit = {
    var op = find(key)
    if (op.isEmpty) {
      op = f()
    }
    if (!op.isEmpty) root.select(op.get)
  }

  /**
   * 显示指定集群, 其他集群进行隐藏处理
   */
  def displayPane(
    pane: ModulePane[_])
  : Unit = {
    tasks.foreach(x => x.visible(false))
    pane.visible(true)
    resize(5)
  }

  /**
   * 当前集群正在显示的面板
   */
  def getDisplayPane()
  : ModulePane[_] = {
    tasks
      .find(v => v.visible())
      .getOrElse(null)
  }
  /**
   * 加载状态设置
   *
   * @param b
   */
  def loading(
    b: Boolean,
    e: Throwable = null)
  : Unit = {
    if (e != null) {
      e.printStackTrace()
    }
    this.isLoading.compareAndSet(!b, b)
    Async.runLater(() => button.setGraphic(getImage(b)))
  }

  /**
   * 加载数据
   */
  def load(): Unit = {
    if (isLoading.get()) return
    loading(true)
    CompletableFuture
      .allOf(brokers(), topics(), groupConsumers())
      .whenComplete((_, e) => loading(false, e))
  }

  /**
   * 获取当前Button的现实效果
   *
   * @param b
   * @return
   */
  def getImage(
    b: Boolean): javafx.scene.Node = {
    if (!b)
      new ImageView(Picture.CLUSTER_BUTTON)
    else
      Javafx.progress("red", 16, 16)
  }


  /**
   * 销毁集群信息
   *
   * @return
   */
  override def destroy(): Unit = {
    tasks.foreach(x => destroy(x))
    adminClient.close(Duration.ofMillis(1000))
    logger.info(s"[${name}] 销毁集群页面信息.")
  }

  /**
   * 销毁指定面板
   */
  def destroy(
    pane: ModulePane[_])
  : Unit = {
    pane.destroy()
    controller.details.getChildren.remove(pane.pane)
  }

  /**
   * 刷新页面
   */
  override def refresh(): Unit = {
    removeIfNeed()
    load()
    _refresh()
    logger.info(s"[${name}] 刷新集群所有页面信息.")
  }

  /**
   * 刷新选中的页面信息
   *
   * @return
   */
  def _refresh()
  : Unit = {
    for (pane <- tasks if pane.visible()) {
      pane.refresh()
    }
  }


  /**
   * 展示Broker信息
   */
  def brokers()
  : CompletableFuture[_] = {
    val root = new CustomTreeItem[String](controller.queryField)
    Async.runLater(() => controller.brokerView.setRoot(root))
    CompletableFuture.allOf(
      controllers("Kafka Controller", root),
      brokers("节点列表", root)
    )
  }

  /**
   * 展示Broker信息
   */
  def controllers(
    name: String,
    root: CustomTreeItem[String])
  : CompletableFuture[Unit] = {
    KafkaUtils
      .controller(this.name, adminClient)
      .thenApplyAsync(controller => drawControllers(name, root, controller))
  }

  /**
   * 描画Controllers
   *
   * @param root
   * @param controller
   */
  def drawControllers(
    name: String,
    root: CustomTreeItem[String],
    controller: Node)
  : Unit = {
    val _parent = Javafx.treeItem(name, Picture.BROKERS)
    root.getChildrens.add(_parent)
    val item = Javafx.treeItem(controller.toString, controller, Picture.BROKER)
    _parent.getChildrens.add(item)
  }

  /**
   * 展示Broker信息
   */
  def brokers(
    name: String,
    root: CustomTreeItem[String])
  : CompletableFuture[Unit] = {
    KafkaUtils
      .brokers(this.name, adminClient)
      .thenApplyAsync(nodes => drawBrokers(name, root, nodes))
  }

  /**
   * 描画Broker节点
   *
   * @param parent
   * @param node
   */
  def drawBroker(
    parent: CustomTreeItem[String],
    node: Node)
  : Unit = {
    val item = Javafx.treeItem(node.toString, node, Picture.BROKER)
    Async.runLater(() => parent.getChildrens.add(item))
  }

  /**
   * 描画Brokers节点
   *
   * @param root
   * @param nodes
   */
  def drawBrokers(
    name: String,
    root: CustomTreeItem[String],
    nodes: util.Collection[Node])
  : Unit = {
    val _parent = Javafx.treeItem(name, Picture.BROKERS)
    nodes.foreach(x => drawBroker(_parent, x))
    Async.runLater(() => root.getChildrens.add(_parent))
  }

  /**
   * 展示topic信息
   */
  def topics()
  : CompletableFuture[Unit] = {
    KafkaUtils
      .topics(name, adminClient)
      .thenApplyAsync(topics => drawTopics(topics))
  }

  /**
   * 描画主题信息
   *
   * @param topics
   */
  def drawTopics(
    topics: Map[String, TopicDescription])
  : Unit = {
    val _topics = topics.toStream.sortBy(_._1)
    consumer(_topics)
    producer(_topics)
  }

  /**
   * 展示topic信息
   */
  def consumer(
    topics: Stream[(String, TopicDescription)])
  : Unit = {
    val root = new CustomTreeItem[String](controller.queryField)
    topics.foreach(v => drawTopic(v._2, root))
    Async.runLater(() => controller.consumerView.setRoot(root))
  }

  /**
   * 展示topic信息
   */
  def producer(
    topics: Stream[(String, TopicDescription)])
  : Unit = {
    val root = new CustomTreeItem[String](controller.queryField)
    topics.foreach(v => drawTopic(v._2, root))
    Async.runLater(() => controller.producerView.setRoot(root))
  }

  /**
   * 展示topic信息
   */
  def drawTopic(
    f: TopicDescription,
    root: CustomTreeItem[String])
  : Unit = {
    val name = f.name() + " (" + f.partitions.size() + ")"
    val tp = new TopicPartition(f.name(), -1)
    val item = Javafx.treeItem(name, tp, Picture.TOPIC)
    root.getChildrens.add(item)
    f.partitions.foreach(x => drawPartition(item, f, x))
  }

  /**
   * 展示partition信息
   */
  def drawPartition(
    root: CustomTreeItem[String],
    f: TopicDescription,
    p: TopicPartitionInfo): Unit = {
    val tp = new TopicPartition(f.name(), p.partition())
    val item = Javafx.treeItem(p.partition().toString, tp, Picture.PARTIRION)
    root.getChildrens.add(item)
  }

  /**
   * 展示消费组信息
   */
  def groupConsumers()
  : CompletableFuture[Unit] = {
    KafkaUtils.groupConsumers(name, adminClient)
      .thenApplyAsync(
        gConsumers => drawGroupConsumers(gConsumers.values())
      )
  }

  /**
   * 展示消费组信息
   */
  def drawGroupConsumers(
    gConsumers: util.Collection[ConsumerGroupDescription])
  : Unit = {
    val root = new CustomTreeItem[String]
    root.bind(controller.queryField)
    sortGroupConsumers(gConsumers).foreach(t => drawGroupConsumer(root, t))
    Async.runLater(() => controller.groupConsumerView.setRoot(root))
  }

  /**
   * 排序消费组信息
   *
   * @param gConsumers
   * @return
   */
  def sortGroupConsumers(
    gConsumers: util.Collection[ConsumerGroupDescription])
  : util.Collection[ConsumerGroupDescription] = {
    gConsumers.parallelStream()
      .sorted((x, y) => y.members().size().compareTo(x.members().size()))
      .collect(Collectors.toList())
  }

  /**
   * 展示消费组信息
   */
  def drawGroupConsumer(
    root: CustomTreeItem[String],
    g: ConsumerGroupDescription)
  : Boolean = {
    val item = if (g.members().isEmpty)
      Javafx.treeItem(g.groupId(), g, Picture.TOPIC)
    else
      Javafx.treeItem(g.groupId(), g, Picture.TOPIC_ON)
    root.getChildrens.add(item)
  }

  /**
   * 获取当前选择的节点
   */
  def selected(
    treeView: TreeView[_])
  : Object = {
    val node = treeView.getSelectionModel.getSelectedItem
    if (node == null)
      return null
    node.getGraphic.getUserData
  }

  /**
   * 添加展示集群节点面板
   */
  def showHome()
  : Unit = {
    val name = "HOME"
    displayIfNeed(name, () => addHome)
  }

  /**
   * 展示节点信息
   */
  def showBroker(): Unit = {
    val node = selected(controller.brokerView).asInstanceOf[Node]
    if (node == null)
      return
    val name = node.toString
    displayIfNeed(name, () => addBroker(node))
  }


  /**
   * 展示消费者信息
   */
  def showConsumer(): Unit = {
    val tp = selected(controller.consumerView).asInstanceOf[TopicPartition]
    if (tp == null) return
    if (tp.partition() == -1)
      showConsumerTopic(tp)
    else
      showConsumerPartition(tp)
  }

  /**
   * 展示生成这信息
   */
  def showProducer(): Unit = {
    val tp = selected(controller.producerView).asInstanceOf[TopicPartition]
    if (tp == null) return
    if (tp.partition() == -1)
      showProducerTopic(tp)
    else
      showProducerPartition(tp)
  }

  /**
   * 展示消费主题信息
   *
   * @param tp
   */
  def showConsumerTopic(
    tp: TopicPartition)
  : Unit = {
    val name = "Consumer-" + tp.toString
    displayIfNeed(name, () => addConsumerTopic(tp))
  }

  /**
   * 展示生产主题信息
   *
   * @param tp
   */
  def showProducerTopic(
    tp: TopicPartition)
  : Unit = {
    val name = "Producer-" + tp.toString
    displayIfNeed(name, () => addProducerTopic(tp))
  }


  /**
   * 展示消费分区信息
   *
   * @param tp
   */
  def showConsumerPartition(
    tp: TopicPartition)
  : Unit = {
    val name = "Consumer-" + tp.toString
    displayIfNeed(name, () => addConsumerPartition(tp))
  }

  /**
   * 展示生产分区信息
   *
   * @param tp
   */
  def showProducerPartition(
    tp: TopicPartition)
  : Unit = {
    val name = "Producer-" + tp.toString
    displayIfNeed(name, () => addProducerPartition(tp))
  }

  /**
   * 展示消费组信息
   */
  def showGroupConsumer(): Unit = {
    val g = selected(controller.groupConsumerView).asInstanceOf[ConsumerGroupDescription]
    if (g == null)
      return
    val name = "Group-" + g.groupId()
    displayIfNeed(name, () => addGroupConsumer(g))
  }


  /**
   * 添加展示集群节点面板
   */
  def addHome()
  : Option[ModulePane[_]] = {
    val home = new Home(this)
    home.init
    cacheAndBindSize(home)
  }

  /**
   * 添加展示集群节点面板
   */
  def addBroker(
    node: Node)
  : Option[ModulePane[_]] = {
    if (node == null)
      return None
    val broker = new BrokerPane(this, node.toString, node)
    broker.init
    cacheAndBindSize(broker)
  }

  /**
   * 添加展示集群节点面板
   */
  def addConsumerTopic(
    tp: TopicPartition)
  : Option[ModulePane[_]] = {
    val consumer = new ConsumerPane(root, this, bootstrap, tp)
    consumer.init
    cacheAndBindSize(consumer)
  }

  /**
   * 添加展示集群节点面板
   */
  def addProducerTopic(
    tp: TopicPartition)
  : Option[ModulePane[_]] = {
    if (tp == null)
      return None
    val producer = new ProducerPane(root, this, bootstrap, tp)
    producer.init
    cacheAndBindSize(producer)
  }

  /**
   * 添加展示集群节点面板
   */
  def addConsumerPartition(
    tp: TopicPartition)
  : Option[ModulePane[_]] = {
    val consumer = new ConsumerPane(root, this, bootstrap, tp)
    consumer.init
    cacheAndBindSize(consumer)
  }

  /**
   * 添加展示集群节点面板
   */
  def addProducerPartition(
    tp: TopicPartition)
  : Option[ModulePane[_]] = {
    val producer = new ProducerPane(root, this, bootstrap, tp)
    producer.init
    cacheAndBindSize(producer)
  }

  /**
   * 添加展示集群节点面板
   */
  def addGroupConsumer(
    group: ConsumerGroupDescription)
  : Option[ModulePane[_]] = {
    val pane = new GroupPane(root, this, bootstrap, group)
    pane.init
    cacheAndBindSize(pane)
  }

  /**
   * 缓存指定的面板信息
   */
  def cacheAndBindSize(
    pane: ModulePane[_])
  : Option[ModulePane[_]] = {
    tasks.add(pane)
    UtilCommons.bindSize(pane.pane, controller.details)
    controller.details.getChildren.add(pane.pane)
    Some[ModulePane[_]](pane)
  }
}
