package application.commons

/**
 * @title: StaticPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/517:45
 */
abstract class SimplePane[T <: BaseController](
  fxml: String, name: String) extends BasePane[T](fxml, name) {

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
