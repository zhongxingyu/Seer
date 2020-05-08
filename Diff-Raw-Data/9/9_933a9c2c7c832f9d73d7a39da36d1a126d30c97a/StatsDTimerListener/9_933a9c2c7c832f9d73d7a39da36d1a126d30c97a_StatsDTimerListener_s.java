 package net.joshdevins.metrics.statsd;
 
 import java.util.concurrent.TimeUnit;
 
 import com.yammer.metrics.core.Timer;
 import com.yammer.metrics.core.TimerListener;
 
 /**
  * Send {@link Timer} metric changes to StatsD as timings.
  * 
  * @author Josh Devins
  */
 public class StatsDTimerListener extends AbstractSamplingStatsDListener
 		implements TimerListener {
 
	protected StatsDTimerListener(final StatsDClient client) {
 		super(Timer.class, client);
 	}
 
 	public void onUpdate(final Timer timer, final long duration,
 			final TimeUnit unit) {
 
 		if (shouldSample()) {
 			getClient().timing(extractBucketName(timer),
 					unit.toMillis(duration), getSampleRate());
 		} else {
 			getClient().timing(extractBucketName(timer),
 					unit.toMillis(duration));
 		}
 	}
 }
