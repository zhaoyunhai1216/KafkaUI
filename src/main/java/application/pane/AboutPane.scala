package application.pane

import application.commons.{BasePane, SimplePane}
import application.element.Picture
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.{Modality, Stage, StageStyle}

/**
 * @title: AboutPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/515:20
 */
class AboutPane(stage: Stage) extends SimplePane("/about.fxml", "About") {
  stage.getIcons.add(new Image(Picture.BROKERS))
  stage.setTitle("About Kafka UI")
  stage.setResizable(false)
  stage.initModality(Modality.APPLICATION_MODAL)
  stage.initStyle(StageStyle.DECORATED)
  stage.setScene(new Scene(pane))
  /**
   * 初始化页面
   *
   * @return
   */
  override def init(): Pane = {
    pane
  }
}
