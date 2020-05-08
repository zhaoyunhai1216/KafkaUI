package application.element

import javafx.scene.control.Alert.AlertType
import javafx.scene.control._
import javafx.scene.image.ImageView
import javafx.scene.layout.Pane
import javafx.scene.paint.Paint
import javafx.scene.shape.Circle
import javafx.scene.control.Tooltip
import javafx.geometry.Rectangle2D
import javafx.stage.{Screen, Stage}

/**
 * @title: Javafx
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2418:20
 */
object Javafx {
  val screen = Screen.getPrimary.getVisualBounds

  /**
   * 创建加载器
   *
   * @param color
   * @return
   */
  def progress(
    color: String,
    width: Int,
    height: Int,
  )
  : ProgressIndicator = {
    val p = new ProgressIndicator()
    p.setStyle("-fx-progress-color:" + color)
    p.setPrefSize(width, height)
    p
  }

  /**
   * 创建TreeItem
   *
   * @param image
   * @return
   */
  def button(
    name: String,
    image: String): Button = {
    val view = new ImageView(image)
    new Button(name, view)
  }

  /**
   * 创建TreeItem
   *
   * @param image
   * @return
   */
  def button(
    name: String,
    image: String,
    style: String): Button = {
    val view = new ImageView(image)
    val button = new Button(name, view)
    button.getStyleClass.add("cluster-button")
    button
  }

  /**
   * 创建TreeItem
   *
   * @param image
   * @return
   */
  def menuItem(
    name: String,
    image: String): MenuItem = {
    val view = new ImageView(image)
    new MenuItem(name, view)
  }

  /**
   * 绑定组件大小
   */
  def bindTooltip(
    button: Button,
    text: String)
  : Unit = {
    val tip = new Tooltip
    tip.setText(text)
    button.setTooltip(tip)
  }

  /**
   * 创建TreeItem
   *
   * @param text
   * @param image
   * @return
   */
  def treeItem(
    text: String,
    image: String): CustomTreeItem[String] = {
    val view = new ImageView(image)
    new CustomTreeItem[String](text, view)
  }

  /**
   * 创建TreeItem
   *
   * @param text
   * @param image
   * @return
   */
  def treeItem(
    text: String,
    userData: Object,
    image: String): CustomTreeItem[String] = {
    val view = new ImageView(image)
    val item = new CustomTreeItem[String](text, view)
    item.getGraphic.setUserData(userData)
    item
  }

  /**
   * 创建Circle
   *
   * @return
   */
  def circle()
  : Circle = {
    val circle = new Circle(16, Paint.valueOf("#78e43a"))
    circle.setStroke(Paint.valueOf("707070"))
    circle.setStrokeWidth(1)
    circle
  }

  /**
   * 创建Circle
   *
   * @return
   */
  def circle(
    color: String)
  : Circle = {
    val circle = new Circle(15, Paint.valueOf(color))
    circle.setStroke(Paint.valueOf("707070"))
    circle.setStrokeWidth(1)
    circle
  }

  /**
   * 创建label
   *
   * @param text
   * @return
   */
  def label(
    text: String,
    tooltip: String): Label = {
    val label = new Label(text)
    label.setContentDisplay(ContentDisplay.CENTER)
    label.setGraphic(circle)
    label.setTooltip(new Tooltip(tooltip))
    label
  }

  /**
   * 创建label
   *
   * @param text
   * @return
   */
  def label(
    text: String,
    tooltip: String,
    color: String): Label = {
    val label = new Label(text)
    label.setContentDisplay(ContentDisplay.CENTER)
    label.setGraphic(circle(color))
    label.setTooltip(new Tooltip(tooltip))
    label
  }

  /**
   * 是否设置全屏显示
   *
   * @param stage
   */
  def getSize(
    stage: Stage)
  : (Double, Double, Double, Double) = {
    (stage.getX, stage.getY, stage.getWidth, stage.getHeight)
  }

  /**
   * 是否设置全屏显示
   *
   * @param stage
   */
  def setMaximized(
    stage: Stage): (Double, Double, Double, Double) = {
    val original = getSize(stage)
    stage.setX(screen.getMinX)
    stage.setY(screen.getMinY)
    stage.setWidth(screen.getWidth)
    stage.setHeight(screen.getHeight)
    original
  }

  /**
   * 是否设置全屏显示
   *
   * @param stage
   */
  def setSize(
    stage: Stage,
    size: (Double, Double, Double, Double))
  : Unit = {
    stage.setX(size._1)
    stage.setY(size._2)
    stage.setWidth(size._3)
    stage.setHeight(size._4)
  }
}
