package application.commons

import application.utils.UtilCommons
import javafx.scene.Cursor
import javafx.scene.layout.Pane

import scala.reflect.ClassTag

/**
 * @title: BasePanel
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2518:36
 */
abstract class BasePane[T <: BaseController](
  fxml: String, var name: String) {
  val fxmlLoader = UtilCommons.newFXMLLoader(fxml)
  val pane = fxmlLoader.load[Pane]
  val controller = fxmlLoader.getController[T]
  if (controller != null) controller.setPane(this)
  pane.setCursor(Cursor.DEFAULT)
  /**
   * 初始化页面
   *
   * @return
   */
  def init()
  : Pane

  /**
   * 刷新页面
   *
   * @return
   */
  def refresh()
  : Unit


  /**
   * 销毁
   *
   * @return
   */
  def destroy()
  : Unit

  /**
   * 获取展示/隐藏
   */
  def visible()
  : Boolean = {
    this.pane.visibleProperty().get()
  }

  /**
   * 展示/隐藏
   */
  def visible(
    value: Boolean)
  : Unit = {
    this.pane.setVisible(value)
  }
}
