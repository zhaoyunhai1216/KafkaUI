package application.commons

import application.pane.KafkaCluster

/**
 * @title: RunnablePane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2620:07
 */
abstract class RunnablePane[T <: BaseController](
  fxml: String, cluster: KafkaCluster, name: String) extends ModulePane[T](fxml, cluster, name) with Runnable {
  //当前运行状态
  var isRunning = false

  /**
   * 启动当前pane
   */
  def start(): Unit = {
    if (isRunning) return
    isRunning = true
    val thread = new Thread(this)
    thread.setDaemon(true)
    thread.start()
  }

  /**
   * 运行状态
   */
  def state(): Boolean = isRunning

  /**
   * 关闭当前pane
   */
  def stop(): Unit = {
    isRunning = false
  }

}
