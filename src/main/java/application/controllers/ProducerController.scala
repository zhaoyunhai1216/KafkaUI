package application.controllers

import java.util.concurrent.TimeUnit

import application.commons.BaseController
import application.element.Checker.check
import application.element.Row
import application.pane.ProducerPane
import application.utils.UtilCommons
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.control.{ComboBox, ScrollPane, TableView, TextArea, TextField}
import javafx.scene.layout.HBox

/**
 * @title: ProducerController
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2722:20
 */
class ProducerController extends BaseController {
  @FXML var loading: HBox = _
  @FXML var scroll: ScrollPane = _
  @FXML var partitions:HBox = _
  @FXML var unitFiled:ComboBox[String] = _
  @FXML var historyView: TableView[Row] = _
  @FXML var textArea:TextArea = _
  @FXML var keyFiled:TextField = _
  @FXML var intervalFiled:TextField = _
  @FXML var timeFiled:TextField = _
  @FXML var lineChart:LineChart[String,Number] = _


  /**
   * 初始化信息
   */
  @FXML
  def initialize()
  : Unit = {
    timeFiled.addCheckNumber
    intervalFiled.addCheckNumber
    unitFiled.getItems.add(TimeUnit.MILLISECONDS.name())
    unitFiled.getItems.add(TimeUnit.SECONDS.name())
    unitFiled.getItems.add(TimeUnit.MINUTES.name())
    unitFiled.getItems.add(TimeUnit.HOURS.name())
    unitFiled.getSelectionModel.select(0)
    UtilCommons.initReadonlyColumns(historyView)
  }

  /**
   * 添加发送的数据
   */
  def add() : Unit = {
    pane[ProducerPane].insertChart()
  }

  /**
   * 删除发送的数据
   */
  def delete() : Unit = {
    val row = historyView.getSelectionModel.getSelectedItem
    historyView.getItems.removeIf(x=> x == row)
  }
  /**
   * 选中重发
   */
  def onSelected(): Unit = {
    pane[ProducerPane].onSelected()
  }

  /**
   * 左滑动
   */
  def left(): Unit = {
    scroll.setHvalue(scroll.getHvalue - 0.1)
  }

  /**
   * 右滑动
   */
  def right(): Unit = {
    scroll.setHvalue(scroll.getHvalue + 0.1)
  }

}
