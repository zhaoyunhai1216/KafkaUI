package application.pane

import java.util.concurrent.atomic.AtomicBoolean

import application.Async
import application.commons.{BasePane, SimplePane}
import application.controllers.TopicController
import application.element.Picture
import application.utils.KafkaUtils
import javafx.scene.Scene
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.KeyEvent
import javafx.scene.layout.Pane
import javafx.stage.{Modality, Stage, StageStyle}
import org.apache.kafka.clients.admin.TopicDescription
import org.apache.kafka.common.KafkaFuture

/**
 * @title: TopicPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/115:54
 */
class TopicPane(
  root: AppPane,
  cluster: KafkaCluster,
  stage: Stage,
  opera: Int) extends SimplePane[TopicController]("/topicManager.fxml", "TopicPane") {
  stage.getIcons.add(new Image(Picture.MANAGER))
  stage.setResizable(false)
  stage.initModality(Modality.APPLICATION_MODAL)
  stage.initStyle(StageStyle.DECORATED)
  stage.setScene(new Scene(pane))

  /**
   * 初始化页面
   *
   * @return
   */
  override def init()
  : Pane = {
    check()
    setTitle(opera)
    setDisable(true)
    if (cluster.adminClient != null)
      setOnKey(controller.topicName)
    pane
  }

  /**
   * 校验是否连接到集群
   */
  def check(): Unit = {
    if (cluster.adminClient == null) {
      controller.prompt.setText("未连接到集群.")
    }
  }

  /**
   * 设置title信息
   *
   * @param opera
   */
  def setTitle(
    opera: Int)
  : Unit = {
    if (opera == 0) {
      stage.setTitle("主题添加")
    } else if (opera == 2) {
      stage.setTitle("主题删除")
      clear()
    }
  }

  /**
   * 关闭
   *
   * @return
   */
  def closeAndSleep(): Unit = {
    Thread.sleep(200)
    stage.close()
  }

  /**
   * 设置是否可编辑
   *
   * @param isEditable
   */
  def setEditable(
    isEditable: Boolean)
  : Unit = {
    controller.partitionCnt.setEditable(isEditable)
    controller.replicFactor.setEditable(isEditable)
  }

  /**
   * 设置字段信息
   */
  def clear()
  : Unit = {
    controller.partitionCnt.setText("")
    controller.replicFactor.setText("")
  }

  /**
   * 设置是否可编辑
   *
   * @param isDisable
   */
  def setDisable(
    isDisable: Boolean)
  : Unit = {
    controller.submitButton.setDisable(isDisable)
  }

  /**
   * 设置按键监听
   */
  def setOnKey(
    textField: TextField)
  : Unit = {
    textField.setOnKeyReleased(e => setOnKeyTyped(textField, e))
  }

  /**
   * 捕获字母案件
   *
   * @param e
   */
  def setOnKeyTyped(
    textField: TextField,
    e: KeyEvent)
  : Unit = {
    check(textField.getText)
  }

  /**
   * 校验操作
   */
  def check(
    topic: String)
  : Unit = {
    setDisable(true)
    loading()
    KafkaUtils
      .topic(cluster.name, topic, cluster.adminClient)
      .whenCompleteAsync((v, e) => switch(v, e))
  }

  /**
   * 根据操作切换执行
   *
   * @param t
   * @param e
   */
  def switch(
    t: TopicDescription,
    e: Throwable)
  : Unit = {
    if (opera == 0) checkCreate(t, e)
    else if (opera == 2) checkDelete(t, e)
  }

  /**
   * 校验操作
   */
  def checkCreate(
    topic: TopicDescription,
    e: Throwable)
  : Unit = {
    if (e != null) {
      setDisable(false)
      loaded()
    } else {
      setDisable(true)
      loaded("主题已存在.")
    }
  }


  /**
   * 校验操作
   */
  def checkDelete(
    topic: TopicDescription,
    e: Throwable)
  : Unit = {
    if (e == null) {
      setDisable(false)
      loaded()
    } else {
      setDisable(true)
      clear()
      loaded("主题不存在.")
    }
  }


  /**
   * 加载状态设置
   */
  def loaded(
    msg: String = "")
  : Unit = {
    Async.runMoreLater(
      () => controller.prompt.setText(msg),
      () => controller.prompt.setVisible(true),
      () => controller.loading.setVisible(false)
    )
  }

  /**
   * 加载状态设置
   */
  def loading()
  : Unit = {
    Async.runMoreLater(
      () => controller.prompt.setVisible(false),
      () => controller.loading.setVisible(true)
    )
  }

  /**
   * 创建主题
   */
  def createTopic(): Unit = {
    KafkaUtils.createTopic(cluster.name,
      controller.topicName.getText,
      controller.partitionCnt.getText.toInt,
      controller.replicFactor.getText.toShort,
      cluster.adminClient
    )
  }


  /**
   * 创建主题
   */
  def deleteTopic(): Unit = {
    KafkaUtils.deleteTopic(
      cluster.name,
      controller.topicName.getText,
      cluster.adminClient
    )
  }

  /**
   * 提交处理
   */
  def submit(): Unit = {
    if (opera == 0)
      createTopic()
    else if (opera == 2)
      deleteTopic()
    closeAndSleep()
  }
}
