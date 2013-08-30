/**
 * Heavily inspired by play-statsd <https://github.com/vznet/play-statsd>
 */
package net.themodernlife.statsd

import akka.actor.ActorSystem
import akka.actor.ExtendedActorSystem
import akka.actor.Extension
import akka.actor.ExtensionId
import akka.actor.ExtensionIdProvider


trait StatsD extends Extension {
	def counter(key: String, value: Long = 1, samplingRate: Double = 1.0)

	def timing(key: String, millis: Long, samplingRate: Double = 1.0)

	def gauge(key: String, value: Long)
}

object StatsDExtension extends ExtensionId[StatsD] with ExtensionIdProvider {
	override def createExtension(system: ExtendedActorSystem): StatsD = new DefaultStatsD(system)

	override def lookup(): ExtensionId[StatsD] = StatsDExtension

	override def get(system: ActorSystem): StatsD = super.get(system)
}