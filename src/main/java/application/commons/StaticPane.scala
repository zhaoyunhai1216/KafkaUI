package application.commons

import application.pane.KafkaCluster

/**
 * @title: StaticPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/1913:07
 */
abstract class StaticPane[T <: BaseController](
  fxml: String, cluster: KafkaCluster, name: String) extends ModulePane[T](fxml, cluster, name) {

  /**
   * 刷新页面
   *
   * @return
   */
  override def refresh(): Unit = {}

  /**
   * 销毁
   *
   * @return
   */
  override def destroy(): Unit = {}
}
