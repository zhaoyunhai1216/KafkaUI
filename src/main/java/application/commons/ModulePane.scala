package application.commons

import application.pane.KafkaCluster

/**
 * @title: ElementPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/1913:00
 */
abstract class ModulePane[T <: BaseController] (
  fxml: String, val cluster: KafkaCluster, name: String) extends BasePane[T](fxml, name){
  /**
   * 显示当前集群
   */
  def display(): Unit ={
    cluster.displayPane(this)
  }
}
