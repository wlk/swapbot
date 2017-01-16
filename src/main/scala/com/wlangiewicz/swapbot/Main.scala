package com.wlangiewicz.swapbot

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.wlangiewicz.bitmarket.Client

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import Config._
import scala.math.BigDecimal.RoundingMode

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val client = new Client(privateApiKey, publicApiKey)

  while (true) {
    try {
      run()
    } catch {
      case e: Exception => e.printStackTrace
    }

  }

  def run(): Unit = {
    val allSwaps = Await.result(client.swaps, 10 seconds)

    val cutoff = allSwaps.cutoff

    val openSwaps = Await.result(client.swapList, 10 seconds)

    val currentRate: BigDecimal = if (openSwaps.data.nonEmpty) openSwaps.data.map(_.rate).min else 0

    println(s"Current rate: $currentRate")

    val expectedRate = cutoff - safetyBuffer(safetyMargin, cutoff)
    println(s"Expected rate: $expectedRate")
    println(s"safetyBuffer(safetyMargin, cutoff): ${safetyBuffer(safetyMargin, cutoff)}")
    println(s"cutoff - safetyBuffer(safetyMargin, cutoff): ${cutoff - safetyBuffer(safetyMargin, cutoff)}")
    println(s"cutoff - safetyBuffer(safetyMargin, cutoff) * 1.1: ${cutoff - safetyBuffer(safetyMargin, cutoff) * 1.1}")

    if (shouldUpdateRate(currentRate, expectedRate, cutoff)) {

      val closeFutures = Future.sequence(openSwaps.data.map(openSwapContract => client.swapClose(openSwapContract.id)))

      val totalFundsTmp = Await.result(closeFutures, 10 seconds).map {
        d =>
          Thread.sleep(200)
          d.data.balances.available.BTC
      }

      val myTotalFunds = if (totalFundsTmp.nonEmpty) totalFundsTmp.max else Await.result(client.info, 10 seconds).data.balances.available.BTC

      println(s"Total funds: $myTotalFunds")

      client.swapOpen(myTotalFunds, expectedRate.setScale(4, RoundingMode.HALF_EVEN))

      println(s"New Rate: $expectedRate")
    } else {
      println(s"No change")
    }

    Thread.sleep(5000)
  }

  private def shouldUpdateRate(currentRate: BigDecimal, expectedRate: BigDecimal, cutoff: BigDecimal) = {
    currentRate < expectedRate - (0.001 * expectedRate) || currentRate < cutoff - (safetyBuffer(safetyMargin, cutoff) * 1.1) || currentRate >= cutoff
  }

  private def safetyBuffer(safetyMargin: BigDecimal, cutoff: BigDecimal): BigDecimal = {
    cutoff * (safetyMargin / 100.0)
  }
}
