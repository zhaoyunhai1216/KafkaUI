package application.pane

import application.Async
import application.commons.{BasePane, ModulePane, RunnablePane}
import application.controllers.AppController
import application.element._
import application.utils.UtilCommons
import javafx.collections.ObservableList
import javafx.event.ActionEvent
import javafx.geometry.Insets
import javafx.scene.control.{Button, MenuItem}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Paint
import javafx.scene.text.Text
import javafx.stage.Stage

import scala.collection.JavaConversions._
import scala.collection.mutable._

/**
 * @title: RootPane
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/3/2620:24
 */
class AppPane(stage: Stage)
  extends BasePane[AppController]("/application.fxml", "Root") {
  new DragListener(stage).enableDrag(controller.titlePane)
  new ShadowListener(stage, pane).enableShadow()
  stage.setOnHidden(_ => Async.runLater(() => stage.hide()))
  var isMaximized = false
  var appSize = Javafx.getSize(stage)
  val clusters = new TreeMap[String, KafkaCluster]
  val on = s"-fx-background-image:url(${Picture.FULL_SCREEN});"
  val off = s"-fx-background-image:url(${Picture.MINI_SCREEN});"

  /**
   * 初始化页面
   *
   * @return
   */
  def init(): Pane = {
    showCluster(Row.home)
    pane
  }

  /**
   * 窗口切换
   */
  def windowSwitch() = {
    if (!isMaximized)
      maximum()
    else
      fitWindow()
  }

  /**
   * 最大化
   */
  def maximum() = {
    setShadow(Insets.EMPTY)
    isMaximized = true
    appSize = Javafx.setMaximized(stage)
    controller.switchButton.setStyle(off)
  }

  /**
   * 窗口缩放
   */
  def fitWindow()
  : Unit = {
    Javafx.setSize(stage, appSize)
    isMaximized = false
    setShadow(new Insets(15))
    controller.switchButton.setStyle(on)
  }

  /**
   * 窗口最小化
   */
  def minimum() = {
    stage.setIconified(true)
  }

  /**
   * 关闭窗口
   *
   * @return
   */
  def close(): Unit = System.exit(0)


  /**
   * 打开about窗口
   */
  def openAbout(): Unit = {
    val stage = new Stage
    new AboutPane(stage).init()
    stage.show()
  }

  /**
   * 设置提示消息
   */
  def setPrompt(
    prompt: String): Unit = {
    controller.prompt.setText(prompt)
  }

  /**
   * 设置提示消息
   */
  def setPrompt(
    prompt: String,
    paint: Paint): Unit = {
    setPrompt(prompt)
    controller.prompt.setTextFill(paint)
  }

  /**
   * 刷新页面
   *
   * @return
   */
  def refresh(): Unit = {
    for (cluster <- clusters.values
         if cluster.visible() && !cluster.isHome) {
      cluster.refresh()
    }
  }

  /**
   * 销毁
   *
   * @return
   */
  def destroy(): Unit = {
    clusters
      .filter(kv => !kv._2.isHome)
      .foreach(kv => kv._2.destroy())
    clusters.clear()
  }

  /**
   * 设置阴影效果是否显示
   *
   * @param insets
   */
  def setShadow(
    insets: Insets)
  : Unit = {
    controller.shadow.setPadding(insets)
  }

  /**
   * 销毁指定集群
   *
   * @return
   */
  def destroy(
    cluster: KafkaCluster)
  : Unit = {
    cluster.destroy()
    clusters.remove(cluster.name)
    displayCluster(clusters.lastKey)
    controller.body.getChildren.remove(cluster.pane)
  }

  /**
   * 任务列表选择
   *
   * @return
   */
  def selected()
  : ModulePane[_] = {
    controller.taskList.getSelectionModel.getSelectedItem
  }

  /**
   * 任务列表选择
   *
   * @return
   */
  def select(
    pane: ModulePane[_]
  ): Unit = {
    val _task = controller.taskList
    _task.setItems(pane.cluster.tasks)
    _task.getSelectionModel.select(pane)
  }

  /**
   * 任务列表选择
   *
   * @return
   */
  def reselect(
    pane: ModulePane[_]
  ): Unit = {
    val _task = controller.taskList
    _task.getItems.remove(pane)
    _task.getItems.add(pane)
    _task.getSelectionModel.select(pane)
  }

  /**
   * 选择正在显示的任务
   *
   * @return
   */
  def autoSelect(cluster: KafkaCluster): Unit = {
    select(cluster.getDisplayPane())
  }


  /**
   * 任务列表删除任务
   *
   * @return
   */
  def autoWidth(value: String): Unit = {
    val text = new Text(value)
    val width = text.getLayoutBounds.getWidth.toInt
    controller.taskList.prefWidthProperty().setValue(width + 70)
  }

  /**
   * 选择指定的值
   */
  def onAction()
  : Unit = {
    val pane = selected()
    if (pane != null) switch(pane)
  }

  /**
   * 切换到指定选项
   */
  def switch(
    pane: ModulePane[_])
  : Unit = {
    buttonState(pane)
    displayCluster(pane)
    //autoWidth(pane.toString)
  }


  /**
   * 重置状态
   */
  def restate(
    pane: ModulePane[_])
  : Unit = {
    reselect(pane)
  }

  /**
   * 执行任务
   */
  def run()
  : Unit = {
    val r = selected()
    if (r == null || !r.isInstanceOf[RunnablePane[_]]) return
    r.asInstanceOf[RunnablePane[_]].start()
    restate(r)
  }

  /**
   * 停止任务
   */
  def stop()
  : Unit = {
    val r = selected()
    if (r == null || !r.isInstanceOf[RunnablePane[_]]) return
    r.asInstanceOf[RunnablePane[_]].stop()
    restate(r)
  }


  /**
   * 设置按钮状态
   */
  def buttonState(
    pane: ModulePane[_])
  : Unit = {
    if (!isRunning(pane)) startState else stopState
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
   * 设置按钮状态
   */
  def startState()
  : Unit = {
    controller.startButton.setDisable(false)
    controller.stopButton.setDisable(true)
  }

  /**
   * 设置按钮状态
   */
  def stopState()
  : Unit = {
    controller.startButton.setDisable(true)
    controller.stopButton.setDisable(false)
  }

  /**
   * 显示指定集群, 其他集群进行隐藏处理
   */
  def getDisplayCluster()
  : KafkaCluster = {
    clusters
      .find(kv => kv._2.visible())
      .getOrElse((null, null))._2
  }

  /**
   * 显示指定集群, 其他集群进行隐藏处理
   */
  def displayCluster(
    pane: ModulePane[_])
  : Unit = {
    pane.display()
  }

  /**
   * 显示指定集群, 其他集群进行隐藏处理
   */
  def displayCluster(
    key: String)
  : Unit = {
    hidenCluster()
    clusters(key).visible(true)
    clusters(key).button.setStyle("-fx-padding: 0 3 0 3;-fx-font-weight: bold;")
    autoSelect(clusters(key))
  }

  /**
   * 隐藏所有集群
   */
  def hidenCluster()
  : Unit = {
    for (cluster <- clusters.values) {
      cluster.visible(false)
      cluster.button.setStyle("-fx-padding: 0 3 0 3;")
    }
  }

  /**
   * 删除集群按钮
   */
  def removeCluster(
    menuItem: MenuItem,
    button: Button,
    row: Row)
  : Unit = {
    if (row.isHome) return
    val key = row.getString("name")
    destroy(clusters(key))
    controller.optionsMenu.getItems.remove(menuItem)
    controller.latelyCluster.getChildren.remove(button)
  }

  /**
   * 打开连接管理页面
   */
  @throws[Exception]
  def open()
  : Unit = {
    val stage = new Stage
    new ConnectionPane(this, stage).init()
    stage.show()
  }

  /**
   * 打开连接管理页面
   */
  def openTopicManager(opera: Int)
  : Unit = {
    val stage = new Stage
    val cluster = getDisplayCluster
    new TopicPane(this, cluster, stage, opera).init()
    stage.show()
  }

  /**
   * 添加集群切换按钮
   */
  def showCluster(row: Row): Unit = {
    val key = row.getString("name")
    if (!clusters.containsKey(key)) addCluster(key, row)
    try displayCluster(key)
    catch {
      case e: Exception => e.printStackTrace()
    }
  }

  /**
   * 缓存指定的面板信息
   */
  def cacheCluster(
    key: String,
    cluster: KafkaCluster)
  : Unit = {
    clusters.put(key, cluster)
    val pane = UtilCommons.bindSize(cluster.pane, controller.body)
    controller.body.getChildren.add(pane)
  }

  /**
   * 添加集群切换按钮
   */
  def addCluster(
    key: String,
    row: Row)
  : Unit = {
    val button = addButton(key, row)
    val cluster = new KafkaCluster(this, button, row)
    cluster.init
    cacheCluster(key, cluster)
  }


  /**
   * 添加集群切换按钮
   */
  def addButton(
    name: String,
    row: Row)
  : Button = {
    val button = Javafx.button(name, Picture.CLUSTER_BUTTON, "cluster-button")
    controller.latelyCluster.getChildren.add(button)
    val menuItem = addClusterMenu(name,row)
    onClicked(menuItem, button, row)
    button
  }

  /**
   * 添加当前的菜单列表
   *
   * @param name
   */
  def addClusterMenu(name: String, row: Row): MenuItem = {
    val item = Javafx.menuItem(name, Picture.CLUSTER_BUTTON)
    controller.optionsMenu.getItems.add(item)
    item.setOnAction(_=> showCluster(row))
    item
  }

  /**
   * 集群按钮点击事件
   *
   * @param button
   * @param row
   */
  def onClicked(
    menuItem: MenuItem,
    button: Button,
    row: Row): Unit = {
    button.setOnMouseClicked((e: MouseEvent) =>
      if (e.getClickCount == 1) showCluster(row)
      else removeCluster(menuItem, button, row)
    )
  }
}
