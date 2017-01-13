package com.wlangiewicz.swapbot

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.wlangiewicz.bitmarket.Client

object Main extends App {
  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()

  val client = new Client(Config.privateApiKey, Config.publicApiKey)

  system.terminate()
}
