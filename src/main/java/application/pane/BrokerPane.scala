package application.pane

import java.util.Collection
import java.util.concurrent.atomic.AtomicBoolean

import application.Async
import application.commons.{ModulePane, SimplePane, StaticPane}
import application.controllers.BrokerController
import application.element.Row
import application.utils.KafkaUtils
import javafx.collections.FXCollections
import javafx.scene.layout.Pane
import org.apache.kafka.clients.admin.ConfigEntry
import org.apache.kafka.common.Node
import org.apache.logging.log4j.LogManager
import org.slf4j.{Logger, LoggerFactory}

/**
 * @title: BrokerPane
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/3/25 20:11
 */
class BrokerPane(
  cluster: KafkaCluster,
  name: String,
  node: Node) extends StaticPane[BrokerController]("/broker1.fxml", cluster , name) {
  val isLoading = new AtomicBoolean(false)
  val adminClient = cluster.adminClient
  var rows = FXCollections.observableArrayList[Row]
  val logger = LogManager.getLogger(this.getClass)

  /**
   * 获取字符串表示信息
   *
   * @return
   */
  override def toString: String = name

  /**
   * 初始化集群展示组件
   */
  def init(): Pane = {
    controller.brokerView.setItems(rows)
    load()
    this.pane
  }

  /**
   * 刷新页面
   *
   * @return
   */
  override def refresh(): Unit = {
    Async.runMoreLater(
      () => load(),
      () => controller.tableFilter.setText("")
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
    rows.clear()
    KafkaUtils
      .broker(cluster.name, adminClient, node)
      .thenAcceptAsync(x => drawTable(x))
      .whenCompleteAsync((_, e) => loading(false,e))
  }

  /**
   * 加载表格数据
   */
  def drawTable(
    configs: Collection[ConfigEntry])
  : Unit = {
    setTitle(configs.size())
    configs.forEach(x => drawRow(x))
  }

  /**
   * 描画单行配置信息
   */
  def drawRow(
    config: ConfigEntry)
  : Unit = {
    val row = new Row()
    row.putObject("name", config.name())
    row.putObject("value", config.value())
    Async.runLater(() => rows.add(row))
  }

  /**
   * 加载数据
   */
  def setTitle(
    size: Int)
  : Unit = {
    val title = s"show configuration($size records)..."
    Async.runLater(() => controller.title.setText(title))
  }

  /**
   * 加载数据
   */
  def filter(filter: String): Unit = {
    val filtered = rows.filtered(x => x.contains(filter))
    setTitle(filtered.size())
    Async.runLater(() => controller.brokerView.setItems(filtered))
  }

}
