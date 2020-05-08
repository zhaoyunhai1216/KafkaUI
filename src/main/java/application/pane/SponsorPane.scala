package application.pane

import application.commons.SimplePane
import application.element.Picture
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.layout.Pane
import javafx.stage.{Modality, Stage, StageStyle}

/**
 * @title: SponsorPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/2317:41
 */
class SponsorPane(stage: Stage) extends SimplePane("/sponsor1.fxml", "Sponsor") {
  stage.getIcons.add(new Image(Picture.SPONSOR))
  stage.setTitle("捐赠")
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

