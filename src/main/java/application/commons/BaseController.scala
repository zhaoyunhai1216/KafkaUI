package application.commons

/**
 * @title: BaseController
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/3/27 19:46
 */
trait BaseController {
  var pane: BasePane[_] = _

  /**
   * 返回具体类型的字段
   * @tparam T
   * @return
   */
  def pane[T](): T = pane.asInstanceOf[T]

  /**
   * 设置pane
   *
   * @return
   */
  def setPane[T <: BaseController](pane: BasePane[T]) = this.pane = pane




}
