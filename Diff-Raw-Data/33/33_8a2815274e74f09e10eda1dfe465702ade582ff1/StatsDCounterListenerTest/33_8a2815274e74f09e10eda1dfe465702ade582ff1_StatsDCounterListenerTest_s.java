 package net.joshdevins.metrics.statsd;
 
 import static org.mockito.Mockito.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.yammer.metrics.core.Counter;
 import com.yammer.metrics.core.MetricName;
 
 public class StatsDCounterListenerTest {
 
 	private StatsDClient mockClient;
 
 	private StatsDCounterListener listener;
 
	private Counter mockCounter;
 
 	@Before
 	public void before() {
 		mockClient = mock(StatsDClient.class);
 		listener = new StatsDCounterListener(mockClient);
		mockCounter = mock(Counter.class);
		when(mockCounter.getName()).thenReturn(metricName());
 	}
 
 	@Test
 	public void onClear() {
		listener.onUpdate(mockCounter, 1);
		verify(mockClient).gauge("group.type.name", 1);
 	}
 
 	@Test
 	public void onUpdate() {
		listener.onClear(mockCounter);
		verify(mockClient).gauge("group.type.name", 0);
	}
 
	private MetricName metricName() {
		return new MetricName("group", "type", "name");
 	}
 }
