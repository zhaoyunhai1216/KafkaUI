package application.controllers

import java.awt.Desktop
import java.net.URI

import application.commons.{BaseController, BasePane, ModulePane, RunnablePane}
import application.element.{CustomListCell, Javafx}
import application.pane.{AboutPane, AppPane, ConnectionPane, SponsorPane}
import javafx.fxml.FXML
import javafx.scene.control.{Button, ComboBox, Label, MenuButton}
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{AnchorPane, HBox}
import javafx.stage.Stage
/**
 * @title: RootController
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/3/2711:39
 */
class AppController extends BaseController {
  var uri = new URI("https://github.com/zhaoyunhai1216/KafkaUI")
  var menuList: Array[MenuButton] = _
  @FXML var prompt:Label = _
  @FXML var fileMenu: MenuButton = _
  @FXML var optionsMenu: MenuButton = _
  @FXML var helpMenu: MenuButton = _
  @FXML var switchButton: Button = _

  @FXML var titlePane: AnchorPane = _
  @FXML var shadow: AnchorPane = _
  @FXML var body: AnchorPane = _
  @FXML var latelyCluster: HBox = _ //展示所有集群按钮信息

  @FXML var startButton: Button = _
  @FXML var stopButton: Button = _
  @FXML var refreshButton: Button = _
  @FXML var openButton: Button = _
  @FXML var taskList: ComboBox[ModulePane[_]] = _ //最近操作信息

  /**
   * 初始化信息
   */
  @FXML
  def initialize(): Unit = {
    menuList = Array(fileMenu, helpMenu, optionsMenu)
    onMouseMoved()
    taskList.setButtonCell(new CustomListCell())
    taskList.setCellFactory(_ => new CustomListCell())
  }

  /**
   * 关闭窗口
   *
   * @return
   */
  def close(): Unit = {
    pane[AppPane].close()
  }

  /**
   * 窗口最小化
   */
  def minimum() = {
    pane[AppPane].minimum()
  }


  /**
   * 最大化最小化切换
   */
  def windowSwitch() = {
    pane[AppPane].windowSwitch()
  }

  /**
   * 双击title切换窗口大小
   */
  def onWindowSwitch(e: MouseEvent) = {
    if (e.getClickCount > 1) windowSwitch()
  }

  /**
   * 鼠标悬浮
   */
  def onMouseMoved()
  : Unit = {
    for (button <- menuList)
      button.setOnMouseMoved(_ => displayMenu(button))
  }

  /**
   * 展示指定的MenuButton
   *
   * @param button
   */
  def displayMenu(
    button: MenuButton)
  : Unit = {
    for (_button <- menuList)
      if (_button == button) _button.show()
      else _button.hide()
  }


  /**
   * 获取介绍文档
   */
  def gettingStarted()
  : Unit = {
    Desktop.getDesktop.browse(uri)
  }

  /**
   * 获取介绍文档
   */
  def webSite()
  : Unit = {
    Desktop.getDesktop.browse(uri)
  }

  /**
   * 获取介绍文档
   */
  def checkUpdate()
  : Unit = {
    Desktop.getDesktop.browse(uri)
  }

  /**
   * 获取介绍文档
   */
  def openAbout()
  : Unit = {
    pane[AppPane].openAbout()
  }
  /**
   * 执行任务
   */
  def run()
  : Unit = {
    pane[AppPane].run()
  }

  /**
   * 停止任务
   */
  def stop()
  : Unit = {
    pane[AppPane].stop()
  }

  /**
   * 刷新页面
   *
   * @return
   */
  def refresh(): Unit = {
    pane[AppPane].refresh()
  }

  /**
   * 选择指定的值
   */
  def onAction()
  : Unit = {
    pane[AppPane].onAction()
  }
  /**
   * 打开连接管理页面
   */
  def open()
  : Unit = {
    pane[AppPane].open()
  }

  /**
   * 创建主题
   */
  def createTopic()
  : Unit = {
    pane[AppPane].openTopicManager(0)
  }
  /**
   * 更新主题
   */
  def updateTopic()
  : Unit = {
    pane[AppPane].openTopicManager(1)
  }

  /**
   * 删除主题
   */
  def deleteTopic()
  : Unit = {
    pane[AppPane].openTopicManager(2)
  }

  /**
   * 捐款
   */
  def sponsor()
  : Unit ={
    val stage = new Stage
    new SponsorPane(stage).init()
    stage.show()
  }
}
