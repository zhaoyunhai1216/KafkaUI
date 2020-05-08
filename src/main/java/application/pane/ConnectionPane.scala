package application.pane

import java.io.{File, IOException}

import application.Async
import application.commons.SimplePane
import application.controllers.ConnectionController
import application.element.{Picture, Row}
import application.utils.UtilCommons
import javafx.collections.FXCollections
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.stage.{Modality, Stage, StageStyle}
import org.apache.commons.io.FileUtils

import scala.collection.JavaConversions._
/**
 * @title: ConnectionPane
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2621:18
 */
class ConnectionPane(
  root:AppPane,
  stage:Stage) extends SimplePane[ConnectionController]("/connection.fxml","ConnectionManager") {
  stage.getIcons.add(new Image(Picture.CLUSTER))
  stage.initModality(Modality.APPLICATION_MODAL)
  stage.initStyle(StageStyle.DECORATED)
  stage.setTitle("集群管理")
  stage.setResizable(false)
  stage.setScene(new Scene(pane))
  val tableView = controller.tableView
  val clusterConnectionSave = controller.clusterConnectionSave
  var rows = FXCollections.observableArrayList[Row]
  val dir = new File(System.getProperty("user.dir") + "/kafkaUI.tmp")
  if(!dir.exists()) dir.createNewFile()


  /**
   * 初始化页面
   *
   * @return
   */
  override def init(): Pane = {
    val lines = FileUtils.readLines(dir)
    for (line <- lines) {
      rows.add(UtilCommons.toRow(line))
    }
    tableView.setItems(rows)
    pane
  }

  /**
   * 列表双击事件
   * @param e
   */
  def open(e: MouseEvent): Unit ={
    if (e.getClickCount > 1) showCluster()
  }
  /**
   * 打开集群, 并展示到主页面
   */
  def showCluster()
  : Unit = {
    val row = tableView.getSelectionModel.getFocusedIndex
    root.showCluster(rows.get(row))
    val stage = tableView.getScene.getWindow.asInstanceOf[Stage]
    stage.close()
  }

  /**
   * 添加新的kafka集群
   */
  @throws[InterruptedException]
  def addRow()
  : Unit = {
    val id = String.valueOf(rows.size)
    rows.add(new Row(id, "name", "address", "version"))
    Thread.sleep(50)
    Async.runLater(()=>editAutoRow())
  }

  /**
   * 添加新的kafka集群
   */
  def editAutoRow()
  : Unit = {
    val index = rows.size - 1
    tableView.getSelectionModel.select(index)
    tableView.getSelectionModel.focus(index)
    editRow()
  }

  /**
   * 添加新的kafka集群
   */
  def deleteRow()
  : Unit = {
    clusterConnectionSave.setImage(new Image(Picture.UNCOMMIT))
    rows.remove(tableView.getSelectionModel.getFocusedIndex)
    tableView.refresh()
  }

  /**
   * 添加新的kafka集群
   */
  def editRow()
  : Unit = {
    clusterConnectionSave.setImage(new Image(Picture.UNCOMMIT))
    tableView.setEditable(true)
    val row = tableView.getSelectionModel.getFocusedIndex
    tableView.edit(row, tableView.getColumns.get(1))
  }

  /**
   * 添加新的kafka集群
   */
  @throws[IOException]
  def saveRow()
  : Unit = {
    clusterConnectionSave.setImage(new Image(Picture.COMMIT))
    tableView.setEditable(false)
    FileUtils.writeLines(dir, rows)
  }
}
