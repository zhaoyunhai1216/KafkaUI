package application.controllers

import application.commons.BaseController
import application.element.Row
import application.pane.{BrokerPane, GroupPane}
import application.utils.UtilCommons
import javafx.fxml.FXML
import javafx.scene.chart.LineChart
import javafx.scene.control.{TableView, TextField}
import javafx.scene.layout.{AnchorPane, HBox}

/**
 * @title: GroupController
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2716:40
 */
class GroupController extends BaseController {
  @FXML
  var loading: HBox = _
  @FXML
  var groupFiled: TextField = _
  @FXML
  var leaderFiled: TextField = _
  @FXML
  var assignorFiled: TextField = _
  @FXML
  var stateFiled: TextField = _
  @FXML
  var tableView: TableView[Row] = _
  @FXML
  var tableFilter:TextField = _
  @FXML
  var lineChart:LineChart[String,Number] = _

  /**
   * 初始化信息
   */
  @FXML
  private def initialize(): Unit = {
    filter
    UtilCommons.initReadonlyColumns(tableView)
  }

  /**
   * 刷新数据
   */
  def refresh(): Unit = {
    pane[BrokerPane].load
    tableFilter.setText("")
  }

  /**
   * 过滤数据
   */
  def filter(): Unit = {
    tableFilter.textProperty.addListener((_, _, z) => pane[GroupPane].filter(z))
  }
}
