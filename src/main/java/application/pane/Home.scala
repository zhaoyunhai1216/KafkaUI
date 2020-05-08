package application.pane

import application.commons.StaticPane
import javafx.scene.layout.Pane

/**
 * @title: Home
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2614:37
 */
class Home(cluster: KafkaCluster) extends StaticPane("/home.fxml", cluster, "HOME") {

  /**
   * 初始化集群展示组件
   */
  def init(): Pane = {
    this.pane
  }

  /**
   * 当前主页显示的名称
   *
   * @return
   */
  override def toString = "Home"
}