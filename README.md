akka-statsd
===========

ActorExtension for sending info to [StatsD](https://github.com/etsy/statsd).  Heavily inspired by the [Play StatsD Plugin](https://github.com/vznet/play-statsd).

application.conf:

```
akka {
  statsd {
		prefix   = example-akka-app
		hostname = statsd.example.com
		port     = 8125
	}
}
```


ExampleActor.scala:

```scala
class ExampleActor extends Actor with StatsD {
	receieve = {
		case Like                ⇒ statsd.counter("likes")             // increment by 1
		case Paycheck(cash)      ⇒ statsd.counter("balance", cash)     // increment by a fixed amount
		case TableReady(howlong) ⇒ statsd.timing("reso-time", howlong) // send timing
		case Temp(howhot)        ⇒ statsd.gauge("temperature", howhot) // update a gauge
	}
}
```

The `counter` and `timing` methods also support a sampling percentage if you don't want to send every event.  See [StatsDSupport.scala](src/main/scala/net/themodernlife/statsd/StatsDSupport.scala) for more details.
