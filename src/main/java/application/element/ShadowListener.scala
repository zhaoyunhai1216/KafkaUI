package application.element

import com.sun.javafx.cursor.CursorType
import javafx.event.EventHandler
import javafx.scene.Cursor
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.stage.Stage

/**
 * @title: StretchListener
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/310:41
 */
class ShadowListener extends EventHandler[MouseEvent] {
  var stage: Stage = _
  var pane: Pane = _
  var Range = 15
  var width: Double = 0
  var height: Double = 0
  var xOffset: Double = 0
  var yOffset: Double = 0
  var (top, bottom, left, right) = (false, false, false, false)

  def this(stage: Stage, pane: Pane) {
    this()
    this.stage = stage
    this.pane = pane
  }

  /**
   * 开启放大缩小
   */
  def enableShadow(): Unit = {
    pane.setOnMouseMoved(this)
    pane.setOnMousePressed(this)
    pane.setOnMouseDragged(this)
  }

  def handle(
    e: MouseEvent)
  : Unit = {
    e.consume
    if (e.getEventType eq MouseEvent.MOUSE_MOVED) {
      onMouseMoved(e)
    }
    if (e.getEventType eq MouseEvent.MOUSE_PRESSED) {
      onMouseRressed(e)
    }
    else if (e.getEventType eq MouseEvent.MOUSE_DRAGGED) {
      onMouseDragged(e)
    }
  }

  def onMouseRressed(
    e: MouseEvent): Unit = {
    e.consume
    width = stage.getWidth
    height = stage.getHeight
    xOffset = e.getScreenX
    yOffset = e.getScreenY
  }

  def onMouseDragged(
    e: MouseEvent): Unit = {
    e.consume
    if (left) {
      val offset = e.getScreenX - xOffset
      if(width - offset > 1024){
        stage.setX(e.getScreenX)
        stage.setWidth(width - offset)
      }
    }
    if (right) {
      stage.setWidth(Math.max(e.getSceneX,1024))
    }

    if (top) {
      val offset = e.getScreenY - yOffset
      if(height - offset > 768){
        stage.setY(e.getScreenY)
        stage.setHeight(height - offset)
      }
    }

    if (bottom) {
      stage.setHeight(Math.max(e.getSceneY,768))
    }
  }

  def onMouseMoved(
    e: MouseEvent): Unit = {
    e.consume();
    val cursorType = getCursorType(
      e.getSceneX,
      e.getSceneY,
      stage.getWidth(),
      stage.getHeight()
    )
    // 最后改变鼠标光标
    pane.setCursor(cursorType);
  }


  def isCursorType(
    x: Double,
    y: Double,
    w: Double,
    h: Double)
  : Unit = {
    left = isCursorLeft(x)
    right = isCursorRight(x, w)
    top = isCursorTop(y)
    bottom = isCursorBottom(y, h)
  }

  /**
   * 光标是否在左部
   *
   * @param x
   * @return
   */
  def isCursorLeft(
    x: Double
  ): Boolean = {
    x >= -Range && x <= Range
  }

  /**
   * 光标是否在右部
   *
   * @param x
   * @param w
   * @return
   */
  def isCursorRight(
    x: Double,
    w: Double
  ): Boolean = {
    x >= w - Range && x <= w + Range
  }

  /**
   * 光标是否在顶部
   *
   * @param y
   * @return
   */
  def isCursorTop(
    y: Double
  ): Boolean = {
    y >= -Range && y <= Range
  }

  /**
   * 光标是否在底部
   *
   * @param y
   * @param h
   * @return
   */
  def isCursorBottom(
    y: Double,
    h: Double
  ): Boolean = {
    y >= h - Range && y <= h + Range
  }

  /**
   * 获取光标类型
   *
   * @param x
   * @param y
   * @param w
   * @param h
   * @return
   */
  def getCursorType(
    x: Double,
    y: Double,
    w: Double,
    h: Double)
  : Cursor = {
    isCursorType(x, y, w, h)
    if (left) {
      if (top)
        Cursor.NW_RESIZE
      else if (bottom)
        Cursor.SW_RESIZE
      else
        Cursor.E_RESIZE
    }
    else if (right) {
      if (top)
        Cursor.NE_RESIZE
      else if (bottom)
        Cursor.SE_RESIZE
      else
        Cursor.E_RESIZE
    } else {
      if (top)
        Cursor.N_RESIZE
      else if (bottom)
        Cursor.S_RESIZE
      else
        Cursor.DEFAULT
    }
  }
}
