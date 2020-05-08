package application.element

import application.Async
import javafx.scene.control.{TableView, TextField}
import org.apache.kafka.clients.consumer.ConsumerRecord
import java.text.SimpleDateFormat
import java.util.Date

/**
 * @title: FxOutput
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/4/1315:12
 */
class FxOutput(recordView: TableView[Row], field: TextField) {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS")
  val records = recordView.getItems

  /**
   * 写入数据
   *
   * @param record
   */
  def println(
    record: ConsumerRecord[Array[Byte], Array[Byte]])
  : Unit = {
    Async.runLater(() => delete())
    appendText(editRow(record))
  }

  /**
   * 清空数据
   */
  def clear(): Unit = {
    records.clear()
  }

  /**
   * 最佳文件到显示区
   */
  def delete()
  : Unit = {
    val overflow = records.size() - field.getText.toInt
    records.remove(0, overflow)
  }

  /**
   * 最佳文件到显示区
   *
   * @param row
   */
  def appendText(
    row: Row)
  : Unit = {
    Async.runLater(() => records.add(row))
  }

  /**
   * 获取字符串或者返回空
   * @param b
   * @return
   */
  def getStringOrNull(
    b: Array[Byte])
  : String = {
    if(b == null) null else new String(b)
  }

  /**
   * 最佳文件到显示区
   *
   * @param record
   */
  def editRow(
    record: ConsumerRecord[Array[Byte], Array[Byte]])
  : Row = {
    val row = new Row()
    row.putObject("Partition", record.partition())
    row.putObject("Offset", record.offset())
    row.putObject("Key", getStringOrNull(record.key()))
    row.putObject("Value", getStringOrNull(record.value()))
    row.putObject("Timetamp", dateFormat.format(new Date(record.timestamp())))
    row
  }
}
