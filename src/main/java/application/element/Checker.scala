package application.element

import application.Async
import javafx.beans.value.ChangeListener
import javafx.scene.control.TextField
import org.apache.kafka.common.TopicPartition

/**
 * @title: Checker
 * @projectName KafkaUI
 * @description: TODO
 * @author qdyk
 * @date 2020/3/2817:13
 */
object Checker {

  /**
   * 添加校验数字方法
   *
   * @param textField
   */
  implicit class check(textField: TextField) {
    def checkNumber(): ChangeListener[String] = (_, o, z) =>
      if (!z.matches("^[1-9]\\d*|0$")) {
        textField.setText(o)
      }

    def addCheckNumber = Async.runLater(()=>textField.textProperty.addListener(checkNumber()))
  }

}
