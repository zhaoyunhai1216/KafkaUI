package application.controllers

import application.Async
import application.commons.BaseController
import application.pane.KafkaCluster
import javafx.fxml.FXML
import javafx.scene.control.{Button, TextField, TreeView}
import javafx.scene.input.{KeyCode, KeyEvent}
import javafx.scene.layout.{AnchorPane, HBox}

/**
 * @title: BodyController
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/3/24 18:55
 */
class ClusterController extends BaseController {
  @FXML
  var queryField: TextField = _
  @FXML
  var queryButton: Button = _
  @FXML
  var searchBox: HBox = _
  @FXML
  var details: AnchorPane = _
  @FXML
  var brokerView: TreeView[String] = _
  @FXML
  var consumerView: TreeView[String] = _
  @FXML
  var producerView: TreeView[String] = _
  @FXML
  var groupConsumerView: TreeView[String] = _

  /**
   * 初始化信息
   */
  @FXML
  def initialize(): Unit = {
    setShowRoot(producerView,brokerView,consumerView,groupConsumerView)
    setOnKey(brokerView, producerView, consumerView,groupConsumerView)
    queryField.prefColumnCountProperty.bind(queryField.textProperty.length)
  }

  /**
   * 展示节点信息
   */
  def showBroker(): Unit = {
    pane[KafkaCluster].showBroker()
  }


  /**
   * 展示消费者信息
   */
  def showConsumer(): Unit = {
    pane[KafkaCluster].showConsumer()
  }

  /**
   * 展示生成这信息
   */
  def showProducer(): Unit = {
    pane[KafkaCluster].showProducer()
  }

  /**
   * 展示消费组信息
   */
  def showGroupConsumer(): Unit = {
    pane[KafkaCluster].showGroupConsumer()
  }
  /**
   * 批量设置根节点显示
   * @param treeViews
   */
  def setShowRoot(treeViews: TreeView[_]*): Unit ={
    treeViews.foreach(x=>x.setShowRoot(false))
  }
  /**
   * 查询窗口隐藏显示
   */
  def visible(): Unit = {
    searchBox.visibleProperty().setValue(!searchBox.isVisible)
    if(searchBox.isVisible)
      queryField.requestFocus()
    else
      queryField.setText("")
  }

  /**
   * 设置按键监听
   */
  def setOnKey(
    treeViews: TreeView[String]*)
  : Unit = {
    for (treeView <- treeViews) {
      treeView.setOnKeyTyped(e => setOnKeyTyped(e))
      treeView.setOnKeyPressed(e => setOnKeyPressed(e))
    }
  }

  /**
   * 捕获字母案件
   *
   * @param e
   */
  def setOnKeyTyped(
    e: KeyEvent)
  : Unit = {
    searchBox.setVisible(true)
    Async.runLater(()=>queryField.setText(queryField.getText + e.getCharacter))
  }

  /**
   * 捕获特殊案件
   *
   * @param e
   */
  def setOnKeyPressed(
    e: KeyEvent)
  : Unit = {
    if (e.getCode eq KeyCode.ESCAPE) {
      queryField.setText("")
      Async.runLater(()=>searchBox.setVisible(false))
    }
    if (e.getCode eq KeyCode.BACK_SPACE)
      Async.runLater(()=>queryField.setText(queryField.getText.substring(0, Math.max(queryField.getText.size - 1, 0))))
  }

}
