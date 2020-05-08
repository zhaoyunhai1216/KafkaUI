package application.element

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Stage
/**
 * @title: DragListener
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/219:32
 */
class DragListener extends EventHandler[MouseEvent] {
  var stage: Stage = _
  var xOffset:Double = 0
  var yOffset:Double = 0

  def this(stage: Stage) {
    this()
    this.stage = stage
  }

  def handle(
    event: MouseEvent)
  : Unit = {
    event.consume
    if (event.getEventType eq MouseEvent.MOUSE_PRESSED) {
      xOffset = event.getSceneX
      yOffset = event.getSceneY
    }
    else if (event.getEventType eq MouseEvent.MOUSE_DRAGGED) {
      stage.setX(event.getScreenX - xOffset)
      if (event.getScreenY - yOffset < 0) stage.setY(0)
      else stage.setY(event.getScreenY - yOffset)
    }
  }

  def enableDrag(
    node: Node )
  : Unit = {
    node.setOnMousePressed(this)
    node.setOnMouseDragged(this)
  }
}
