package application.controllers

import application.commons.BaseController
import application.element.Row
import application.pane.BrokerPane
import application.utils.UtilCommons
import javafx.fxml.FXML
import javafx.scene.control.{Hyperlink, TableView, TextField}
import javafx.scene.layout.{AnchorPane, HBox}
/**
 * @title: BrokerController
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/3/27 20:30
 */
class BrokerController extends BaseController {
  @FXML var loading:HBox = _
  @FXML var title:Hyperlink = _
  @FXML var setting:AnchorPane = _
  @FXML var tableFilter:TextField = _
  @FXML var brokerView:TableView[Row] = _

  /**
   * 初始化信息
   */
  @FXML
  private def initialize(): Unit = {
    filter()
    UtilCommons.initReadonlyColumns(brokerView)
  }

  /**
   * 点击切换隐藏状态
   */
  def onAction(): Unit={
    setting.setVisible(!setting.isVisible)
  }

  /**
   * 刷新数据
   */
  def refresh(): Unit = {
    pane[BrokerPane].refresh()
  }

  /**
   * 过滤数据
   */
  def filter(): Unit = {
    tableFilter.textProperty.addListener((_, _, z) => pane[BrokerPane].filter(z))
  }
}
