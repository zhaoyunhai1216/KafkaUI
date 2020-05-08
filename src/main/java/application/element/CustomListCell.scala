package application.element
import application.commons.{BasePane, ModulePane, RunnablePane}
import javafx.scene.Node
import javafx.scene.control.ListCell
import javafx.scene.image.ImageView
/**
 * @title: CustomListCell
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/3015:00
 */
class CustomListCell extends ListCell[ModulePane[_]]{
  /**
   * pane是否正在运行
   */
  def isRunning(
    pane: ModulePane[_])
  : Boolean = {
    pane.isInstanceOf[RunnablePane[_]] && pane.asInstanceOf[RunnablePane[_]].state()
  }


  override def updateItem(
    item: ModulePane[_],
    empty: Boolean)
  : Unit = {
    super.updateItem(item, empty)
    if(item == null)
      setGraphic(null)
    else if (!isRunning(item))
      setGraphic(new ImageView(Picture.TOPIC))
    else
      setGraphic(new ImageView(Picture.TOPIC_ON))
    setText(if(item == null) "" else item.toString)
  }
}
