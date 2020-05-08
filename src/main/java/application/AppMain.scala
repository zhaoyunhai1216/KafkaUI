package application
import application.element.Picture
import application.pane.AppPane
import javafx.application.Application
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.scene.{Scene, paint}
import javafx.stage.{Stage, StageStyle}
import org.slf4j.LoggerFactory
/**
 * @title: Application
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/4/210:20
 */

class AppMain extends Application{


  @throws[Exception]
  def create(stage: Stage): Pane = {
    val pane = new AppPane(stage)
    pane.init
  }


  override def start(
    stage: Stage): Unit = {
    stage.getIcons.add(new Image(Picture.APP))
    val root = create(stage)
    stage.initStyle(StageStyle.TRANSPARENT)
    stage.setScene(new Scene(root, 1440, 768 , paint.Color.TRANSPARENT))
    stage.show()
  }
}


object AppMain{
  def main(args: Array[String]): Unit = {
    Application.launch(classOf[AppMain], args: _*)
    logger.info("主窗体启动完毕.")
  }
  /**
   * 日志
   */
  val logger = LoggerFactory.getLogger(AppMain.getClass)
}
