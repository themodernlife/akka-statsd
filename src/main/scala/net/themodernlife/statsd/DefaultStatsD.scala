/**
 * Heavily inspired by play-statsd <https://github.com/vznet/play-statsd>
 */
package net.themodernlife.statsd

import com.typesafe.config.Config
import akka.actor.ActorSystem
import akka.event.Logging
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import scala.util.Random
import scala.util.control.NonFatal


private[statsd] class StatsDSettings(config: Config) {
	val prefix   = config.getString("tml.statsd.prefix")
	val hostname = config.getString("tml.statsd.hostname")
	val port     = config.getInt("tml.statsd.port")
}

private[statsd] class DefaultStatsD(system: ActorSystem) extends StatsD {
	val log = Logging(system, "StatsD")
	val settings = if (system.settings.config.hasPath("tml.statsd")) {
		Some(new StatsDSettings(system.settings.config))
	} else {
		None
	}

	def counter(key: String, value: Long = 1, samplingRate: Double = 1.0) {
		safely {
			maybeSend(key, value, "c", samplingRate)
		}
	}

	def timing(key: String, millis: Long, samplingRate: Double = 1.0) {
		safely {
			maybeSend(key, millis, "ms", samplingRate)
		}
	}

	def gauge(key: String, value: Long) {
		safely {
			maybeSend(key, value, "g", 1.0) 
		}
	}

	def now(): Long = System.currentTimeMillis()

	lazy val random = new Random()

	def maybeSend(key: String, value: Long, suffix: String, samplingRate: Double) {
		if (samplingRate >= 1.0 || random.nextFloat() < samplingRate) {
			send(key: String, value: Long, suffix: String, samplingRate: Double)
		}
	}

	def safely(operation: => Unit) {
		try {
			operation
		} catch {
			case NonFatal(e) ⇒ log.warning("Error attempting to send stat to StatsD", e)
		}
	}

	lazy val send: Function4[String, Long, String, Double, Unit] = {
		try {
			settings match {
				case Some(statsdSettigs) ⇒ {
					val socket = new DatagramSocket()
					val prefix = statsdSettigs.prefix
					val host = InetAddress.getByName(statsdSettigs.hostname)
					val port = statsdSettigs.port
					socketSend(socket, host, port, prefix) _
				}
				case None ⇒ {
					log.debug("StatsD output disabled becuase no configuration was found")
					noopSend _
				}
			}
		} catch {
	  		case NonFatal(e) ⇒ {
	  			log.error(e, "StatsD output disabled due to configuration error")
	  			noopSend _
	  		}
		}
  	}

	def socketSend(socket: DatagramSocket, host: InetAddress, port: Int, prefix: String)(key: String, value: Long, suffix: String, samplingRate: Double) {
		try {
			val data = samplingRate match {
				case x if x >= 1.0 ⇒ "%s.%s:%s|%s".format(prefix, key, value, suffix)
				case _             ⇒ "%s.%s:%s|%s|@%f".format(prefix, key, value, suffix, samplingRate)
			}
			socket.send(new DatagramPacket(data.getBytes("utf-8"), data.length, host, port))
		} catch {
			case NonFatal(e) ⇒ log.error(e, "Error sending stat")
		}
	}

  	def noopSend(key: String, value: Long, suffix: String, samplingRate: Double) = Unit
}