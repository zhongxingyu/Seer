 package hudson.plugins.downstream_ext;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.*;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Action;
 import hudson.model.Cause;
 import hudson.model.ItemGroup;
 import hudson.model.Result;
 import hudson.model.TaskListener;
 import hudson.plugins.downstream_ext.DownstreamTrigger.Strategy;
 import hudson.scm.SCM;
 import hudson.util.StreamTaskListener;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 
@SuppressWarnings("unchecked")
 public class AsynchPollingTest {
 
 	private AbstractProject upstream;
 	private AbstractProject downstream;
 	private AbstractBuild upstreamBuild;
 
 	@Before
 	public void setup() {
 		upstream = mock(AbstractProject.class);
 		ItemGroup parent = mock(ItemGroup.class);
 		when(parent.getUrl()).thenReturn("http://foo");
 		when(upstream.getParent()).thenReturn(parent);
 		
 		upstreamBuild = mock(AbstractBuild.class);
 		when(upstreamBuild.getProject()).thenReturn(upstream);
 		when(upstreamBuild.getResult()).thenReturn(Result.SUCCESS);
 		
 		this.downstream = createDownstreamProject();
 	}
 	
 	private static AbstractProject createDownstreamProject() {
 		final AbstractProject downstream = mock(AbstractProject.class);
 		when(downstream.pollSCMChanges(Mockito.<TaskListener>any())).thenReturn(Boolean.TRUE);
 		SCM blockingScm = mock(SCM.class);
 		when(blockingScm.requiresWorkspaceForPolling()).thenReturn(Boolean.TRUE);
 		
 		when(downstream.getScm()).thenReturn(blockingScm);
 		
 		return downstream;
 	}
 	
 	/**
 	 * Tests that for projects with SCMs which 'requiresWorkspaceForPolling'
 	 * polling is started on a different thread.
 	 */
 	@Test
 	public void testPollingIsAsynchronous() throws IOException, InterruptedException {
 		final CountDownLatch startLatch = new CountDownLatch(1);
 		final CountDownLatch endLatch = new CountDownLatch(1);
 		
 		final Cause[] causeHolder = new Cause[1];
 		
 		DownstreamDependency dependency = new DownstreamDependency(upstream, downstream,
 				new DownstreamTrigger("", Result.SUCCESS, true, Strategy.AND_HIGHER,
 						MatrixTrigger.BOTH)) {
 
 					@Override
 					Runnable getPoller(AbstractProject p, Cause cause,
 							List<Action> actions) {
 						causeHolder[0] = cause;
 						final Runnable run = super.getPoller(p, cause, actions);
 						
 						return new Runnable() {
 							@Override
 							public void run() {
 								startLatch.countDown();
 								run.run();
 								endLatch.countDown();
 							}
 						};
 					}
 		};
 		
 		Action action = mock(Action.class);
 		
 		boolean triggerSynchronously = dependency.shouldTriggerBuild(upstreamBuild,
 				new StreamTaskListener(), Collections.singletonList(action));
 		assertFalse(triggerSynchronously);
 		
 		if(!startLatch.await(60, TimeUnit.SECONDS)) {
 			fail("Time out waiting for start latch");
 		}
 		
 		if(!endLatch.await(60, TimeUnit.SECONDS)) {
 			fail("Time out waiting for end latch");
 		}
 		
 		verify(downstream).scheduleBuild(downstream.getQuietPeriod(),
 				causeHolder[0], action);
 	}
 	
 	/**
 	 * Tests that for asynchronous polling only one polling is done in parallel.
 	 */
 	@Test
 	public void testOnlyOneParallelPoll() throws IOException, InterruptedException {
 		final CountDownLatch startLatch1 = new CountDownLatch(1);
 		final CountDownLatch endLatch1 = new CountDownLatch(1);
 		
 		DownstreamDependency dependency = new DownstreamDependency(upstream, downstream,
 				new DownstreamTrigger("", Result.SUCCESS, true, Strategy.AND_HIGHER,
 						MatrixTrigger.BOTH)) {
 
 					@Override
 					Runnable getPoller(AbstractProject p, Cause cause,
 							List<Action> actions) {
 						final Runnable run = super.getPoller(p, cause, actions);
 						
 						return new Runnable() {
 							@Override
 							public void run() {
 								startLatch1.countDown();
 								run.run();
 								try {
 									endLatch1.await();
 								} catch (InterruptedException e) {
 									e.printStackTrace();
 								}
 							}
 						};
 					}
 		};
 		
 		boolean triggerSynchronously = dependency.shouldTriggerBuild(upstreamBuild,
 				new StreamTaskListener(), Collections.<Action>emptyList());
 		assertFalse(triggerSynchronously);
 		
 		// wait until 1st poller is definitely running
 		if (!startLatch1.await(60, TimeUnit.SECONDS)) {
 			fail("Time out waiting for start latch");
 		}
 		
 		final CountDownLatch startLatch2 = new CountDownLatch(1);
 		
 		DownstreamDependency dependency2 = new DownstreamDependency(upstream, downstream,
 				new DownstreamTrigger("", Result.SUCCESS, true, Strategy.AND_HIGHER,
 						MatrixTrigger.BOTH)) {
 					@Override
 					Runnable getPoller(AbstractProject p, Cause cause,
 							List<Action> actions) {
 						final Runnable run = super.getPoller(p, cause, actions);
 						
 						return new Runnable() {
 							@Override
 							public void run() {
 								startLatch2.countDown();
 								run.run();
 							}
 						};
 					}
 		};
 		dependency2.shouldTriggerBuild(upstreamBuild,
 				new StreamTaskListener(), Collections.<Action>emptyList());
 		
 		boolean noTimeout = startLatch2.await(2, TimeUnit.SECONDS);
 		// assert that we timeout waiting for poller2 to start
 		assertFalse(noTimeout);
 		
 		// poll on a different downstream job can still happen:
 		final CountDownLatch startLatch3 = new CountDownLatch(1);
 		AbstractProject newDownstream = createDownstreamProject();
 		DownstreamDependency dependency3 = new DownstreamDependency(upstream, newDownstream,
 				new DownstreamTrigger("", Result.SUCCESS, true, Strategy.AND_HIGHER,
 						MatrixTrigger.BOTH)) {
 					@Override
 					Runnable getPoller(AbstractProject p, Cause cause,
 							List<Action> actions) {
 						final Runnable run = super.getPoller(p, cause, actions);
 						
 						return new Runnable() {
 							@Override
 							public void run() {
 								startLatch3.countDown();
 								run.run();
 							}
 						};
 					}
 		};
 		dependency3.shouldTriggerBuild(upstreamBuild,
 				new StreamTaskListener(), Collections.<Action>emptyList());
 		noTimeout = startLatch3.await(60, TimeUnit.SECONDS);
 		assertTrue(noTimeout);
 		
 		
 		// when poller 1 finishes, poller2 can continue:
 		endLatch1.countDown();
 		noTimeout = startLatch2.await(60, TimeUnit.SECONDS);
 		assertTrue(noTimeout);
 	}
 }
