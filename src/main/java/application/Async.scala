package application

import java.util.concurrent.{CompletableFuture, Executor, Executors}

import javafx.application.Platform
import org.apache.kafka.common.KafkaFuture

/**
 * @title: Async
 * @projectName KafkaUI
 * @description: TODO
 * @author 赵云海
 * @date 2020/3/2423:02
 */
object Async {
  /**
   * 并发线程池, 用于并发处理
   */
  val executor: Executor = Executors.newCachedThreadPool

  /**
   * 多线程描画页面
   *
   * @param runnable
   * @tparam A
   */
  def runLater[A](
    runnable: Runnable)
  : Unit = Platform.runLater(runnable)

  /**
   * 多线程描画页面
   *
   * @param runnables
   * @tparam A
   */
  def runMoreLater[A](
    runnables: Runnable*)
  : Unit = runnables.foreach(x => runLater(x))

  /**
   * 多线程并发异步处理
   */
  def run(
    runnable: Runnable)
  : CompletableFuture[_] = {
    CompletableFuture.supplyAsync(() => runnable.run(), executor)
  }

  /**
   * 多线程并发异步处理
   */
  def run(
    runnables: Runnable*)
  : CompletableFuture[_] = {
    CompletableFuture.allOf(runnables.map(x => run(x)).toList: _*)
  }

  /**
   * 转换成java的Future
   *
   * @param future
   * @tparam T
   * @return
   */
  def toJavaFuture[T](
    future: KafkaFuture[T])
  : CompletableFuture[T] = {
    val _future = new CompletableFuture[T]()
    whenComplete(future, _future)
    _future
  }

  /**
   * 转换成java的Future
   *
   * @param future0
   * @tparam T
   * @return
   */
  def whenComplete[T](
    future0: KafkaFuture[T],
    future1: CompletableFuture[T])
  : Unit = {
    future0.whenComplete((t, e) =>
      if (e == null)
        future1.complete(t)
      else
        future1.completeExceptionally(e)
    )
  }
}