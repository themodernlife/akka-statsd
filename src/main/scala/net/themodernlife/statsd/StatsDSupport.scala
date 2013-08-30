/**
 * Heavily inspired by play-statsd <https://github.com/vznet/play-statsd>
 */
package net.themodernlife.statsd

import akka.actor.Actor


trait StatsDSupport { this: Actor ⇒
	protected val statsd = StatsDExtension(context.system)
}
