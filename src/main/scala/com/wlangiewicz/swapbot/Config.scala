package com.wlangiewicz.swapbot

import com.typesafe.config.ConfigFactory

object Config {
  private val config = ConfigFactory.load

  val privateApiKey = config.getString("swapbot.apiKey.private")

  val publicApiKey = config.getString("swapbot.apiKey.public")

  val safetyMargin = BigDecimal(config.getString("swapbot.safetyMargin"))
}
