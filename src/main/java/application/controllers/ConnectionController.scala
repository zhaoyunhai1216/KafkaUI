package application.controllers

import java.io.IOException

import application.commons.BaseController
import application.element.Row
import application.pane.ConnectionPane
import application.utils.UtilCommons
import javafx.fxml.FXML
import javafx.scene.control.TableView
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent

/**
 * @title: ConnectionController
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/279:21
 */
class ConnectionController extends BaseController{
  @FXML
  var tableView: TableView[Row] = _
  @FXML
  var clusterConnectionSave: ImageView = _

  /**
   * 初始化信息
   */
  @FXML
  def initialize()
  : Unit = {
    UtilCommons.initEditTableColumn(tableView)
  }

  def open(e: MouseEvent): Unit ={
    pane[ConnectionPane].open(e)
  }

  /**
   * 添加新的kafka集群
   */
  @throws[InterruptedException]
  def addRow()
  : Unit = pane[ConnectionPane].addRow()


  /**
   * 添加新的kafka集群
   */
  def deleteRow()
  : Unit = pane[ConnectionPane].deleteRow()

  /**
   * 添加新的kafka集群
   */
  def editRow()
  : Unit = pane[ConnectionPane].editRow()

  /**
   * 添加新的kafka集群
   */
  @throws[IOException]
  def saveRow()
  : Unit = pane[ConnectionPane].saveRow()
}
