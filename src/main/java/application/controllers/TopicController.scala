package application.controllers

import application.commons.BaseController
import application.pane.TopicPane
import javafx.fxml.FXML
import javafx.scene.control.{Button, Label, TextField}

/**
 * @title: CreateController
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/115:12
 */
class TopicController extends BaseController{
  @FXML
  var topicName: TextField = _
  @FXML
  var partitionCnt: TextField = _
  @FXML
  var replicFactor: TextField = _
  @FXML
  var prompt:Label = _
  @FXML
  var loading:Label = _
  @FXML
  var cancelButton: Button = _
  @FXML
  var submitButton: Button = _

  /**
   * 初始化信息
   */
  @FXML
  def initialize(): Unit = {

  }

  /**
   * 关闭窗口
   *
   * @return
   */
  def close(): Unit = {
    pane[TopicPane].closeAndSleep()
  }

  /**
   * 提交处理
   */
  def submit(): Unit = {
    pane[TopicPane].submit()
  }
}
