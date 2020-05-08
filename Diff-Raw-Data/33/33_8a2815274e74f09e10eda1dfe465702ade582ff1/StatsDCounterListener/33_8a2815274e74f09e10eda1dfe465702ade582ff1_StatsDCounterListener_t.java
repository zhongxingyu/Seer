 package net.joshdevins.metrics.statsd;
 
 import com.yammer.metrics.core.Counter;
 import com.yammer.metrics.core.CounterListener;
 
 /**
  * Send {@link Counter} metric changes to StatsD as gauges.
  * 
  * @author Josh Devins
  */
 public class StatsDCounterListener extends AbstractStatsDListener implements
 		CounterListener {
 
 	public StatsDCounterListener(final StatsDClient client) {
 		super(Counter.class, client);
 	}
 
 	public void onClear(final Counter counter) {
 		getClient().gauge(extractBucketName(counter), 0);
 	}
 
 	public void onUpdate(final Counter counter, final long delta) {
		getClient().gauge(extractBucketName(counter), counter.getCount());
 	}
 }
