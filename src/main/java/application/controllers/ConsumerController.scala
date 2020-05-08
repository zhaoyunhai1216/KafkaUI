package application.controllers

import application.commons.BaseController
import application.element.Checker.check
import application.element.Row
import application.pane.{BrokerPane, ConsumerPane}
import application.utils.UtilCommons
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.control.{Label, ScrollPane, TableView, TextArea, TextField}
import javafx.scene.layout.{AnchorPane, HBox, VBox}

/**
 * @title: TopicController
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/3/27 21:41
 */
class ConsumerController extends BaseController {
  @FXML var loading: HBox = _
  @FXML var scroll: ScrollPane = _
  @FXML var partitions: HBox = _
  @FXML var recordView: TableView[Row] = _
  @FXML var textArea: TextArea = _
  @FXML var keepSize: TextField = _
  @FXML var tableFilter: TextField = _
  @FXML var groupIdFiled: TextField = _
  @FXML var lineChart: LineChart[String, Number] = _


  /**
   * 初始化信息
   */
  @FXML
  def initialize()
  : Unit = {
    filter()
    UtilCommons.initTextAreaColumns(recordView,textArea)
    keepSize.addCheckNumber
    groupIdFiled.prefColumnCountProperty.bind(groupIdFiled.textProperty.length)
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

  /**
   * 过滤数据
   */
  def filter(): Unit = {
    tableFilter.textProperty.addListener((_, _, z) => pane[ConsumerPane].filter(z))
  }
}
