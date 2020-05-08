package application.element

import application.utils.ReflectionUtil
import javafx.collections.transformation.FilteredList
import javafx.collections.{FXCollections, ListChangeListener, ObservableList}
import javafx.scene.Node
import javafx.scene.control.{TextField, TreeItem}

/**
 * @title: CustomTreeItem
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/3/29 12:12
 */
class CustomTreeItem[T] extends TreeItem[T] {

  var value: T = _
  var graphic: Node = _

  var sources = FXCollections.observableArrayList[CustomTreeItem[T]]
  var filtered = new FilteredList(sources)
  setFieldChildren(filtered)

  /**
   * 添加构造方法
   */
  def this(value: T, graphic: Node) {
    this()
    setValue(value)
    setGraphic(graphic)
  }

  /**
   * 添加构造方法
   */
  def this(filed: TextField) {
    this()
    bind(filed)
  }

  /**
   * 绑定过滤器，传入指定的TextField
   */
  def bind(
    filed: TextField): Unit = {
    filed.textProperty.addListener((_, _, z) => filter(z))
  }

  /**
   * 过滤掉不匹配的信息
   */
  def filter(
    filter: String): Boolean = {
    this.filtered.setPredicate(s => s.filter(filter))
    !filtered.isEmpty || (this.getValue != null && this.getValue.toString.contains(filter))
  }

  /**
   * 获取所有的子节点
   */
  def getChildrens: ObservableList[CustomTreeItem[T]] = this.sources


  /**
   * 初始化子节点的信息
   *
   * @param list
   */
  def setFieldChildren(
    list: ObservableList[CustomTreeItem[T]])
  : Unit = {
    ReflectionUtil.setFieldValue(this, "children", list)
    val childrenListener = ReflectionUtil.getFieldValue(this, "childrenListener")
    list.addListener(childrenListener.asInstanceOf[ListChangeListener[_ >: TreeItem[T]]])
  }
}
