package application.element

import application.Async
import javafx.scene.chart.XYChart

/**
 * @title: FxChart
 * @projectName KafkaUI
 * @description: TODO
 * @author zhaoyunhai
 * @date 2020/4/917:19
 */
class FxChart(title:String, f: ()=>Long) {
  val series = new XYChart.Series[String, Number]
  series.setName(title)
  var chartLastTime = 0L
  var chartIndex = 0

  /**
   * 清理数据
   */
  def clear(): Unit ={
    series.getData.clear()
  }
  /**
   * 是否插入到图表中
   */
  def isAppendChart()
  : Boolean = {
    val timestamp = System.currentTimeMillis()
    timestamp - chartLastTime >= 1000
  }

  /**
   * 在图表中删除掉多余的项
   */
  def removeChart()
  : Unit = {
    while (series.getData.size() > 5 * 60) {
      series.getData.remove(0)
    }
  }

  /**
   * 获取本次增长值
   *
   * @return
   */
  def getChartValue(): Long = {
    chartIndex = chartIndex + 1
    f()
  }

  /**
   * 加载延迟数据
   */
  def refresh()
  : Unit = {
    if (!isAppendChart())
      return
    chartLastTime = System.currentTimeMillis()
    Async.runLater(()=>delayChart())
  }


  /**
   * 加入数据
   */
  def delayChart()
  : Unit = {
    val value = getChartValue()
    series.getData.add(new XYChart.Data(chartIndex.toString, value))
    removeChart()
  }
}
