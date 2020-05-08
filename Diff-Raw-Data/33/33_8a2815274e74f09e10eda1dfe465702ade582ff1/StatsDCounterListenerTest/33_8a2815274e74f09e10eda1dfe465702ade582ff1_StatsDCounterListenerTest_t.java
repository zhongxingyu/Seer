 package net.joshdevins.metrics.statsd;
 
 import static org.mockito.Mockito.*;
 
import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
import com.yammer.metrics.Metrics;
 import com.yammer.metrics.core.Counter;
 import com.yammer.metrics.core.MetricName;
 
 public class StatsDCounterListenerTest {
 
 	private StatsDClient mockClient;
 
 	private StatsDCounterListener listener;
 
	private Counter counter;
 
 	@Before
 	public void before() {
 		mockClient = mock(StatsDClient.class);
 		listener = new StatsDCounterListener(mockClient);
		counter = Metrics.newCounter(new MetricName("group", "type", "name"));
 	}
 
 	@Test
 	public void onClear() {
		listener.onClear(counter);
		verify(mockClient).gauge("group.type.name", 0);
 	}
 
 	@Test
 	public void onUpdate() {
		counter.inc();
		Assert.assertEquals(1, counter.getCount());
 
		listener.onUpdate(counter, 1);
		verify(mockClient).gauge("group.type.name", 1);
 	}
 }
