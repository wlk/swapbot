package com.wlangiewicz.swapbot

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.wlangiewicz.bitmarket.Client

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val client = new Client(Config.privateApiKey, Config.publicApiKey)

  //val response = client.performRequest(client.infoRequest).flatMap(client.unmarshalResponse)

  //println(Await.result(response, 10 seconds))

  val response = client.swapList
  println(Await.result(response, 10 seconds))

  val openResponse = client.swapOpen(1, 6)
  val opened = Await.result(openResponse, 10 seconds)

  val swapToClose = opened.data.id
  val closeResponse = client.swapClose(swapToClose)
  println(Await.result(closeResponse, 10 seconds))


  system.terminate()
}
