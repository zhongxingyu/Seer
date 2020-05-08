 /*
  * Copyright 2010, 2011 Eric Myhre <http://exultant.us>
  * 
  * This file is part of AHSlib.
  *
  * AHSlib is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, version 3 of the License, or
  * (at the original copyright holder's option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
 import us.exultant.ahs.util.*;
 import us.exultant.ahs.test.*;
 import us.exultant.ahs.thread.*;
 import java.io.*;
 import java.nio.channels.*;
 import java.util.*;
 import java.util.concurrent.*;
 
 /**
  * General tests that any InputSystem and OutputSystem with matching framers attached to
  * connected channels should be able to pass... and only for networky stuff that uses
  * schedulers and selectors, because the signatures of the factory methods makes it hard
  * to unite tests for those with tests for simpler systems. Does not include tests for
  * framers like HTTP that have metadata as well as a straight data channel, because it's
  * impossible to generalize those.
  * 
  * @author Eric Myhre <tt>hash@exultant.us</tt>
  * 
  */
 public abstract class IOSystemTest<$MSG> extends TestCase {
 	public List<Unit> getUnits() {
 		List<Unit> $units = new ArrayList<Unit>();
 		$units.add(new TestBasic());
 		if (defineSupportsMultimessage()) $units.add(new TestBasicMultimessage());
 		$units.add(new TestBasicBig());
 		$units.add(new TestBidiBig());
 		if (defineSupportsMultimessage()) $units.add(new TestBidiBigMultimessage());
 		$units.add(new TestClosureWaitingOnPipe());
 		if (defineSupportsMultimessage()) $units.add(new TestClosureMultimessageWaitingOnPipe());
 		return $units;
 	}
 
 	protected abstract boolean defineSupportsMultimessage();
 	//protected abstract boolean defineSupportsBinaryMessage();	you can deal with this already by defining the messages.
 	protected abstract ChannelReader<$MSG> defineReader();
 	protected abstract ChannelWriter<$MSG> defineWriter();
 	protected abstract $MSG defineTestMessage1();
 	protected abstract $MSG defineTestMessage2();
 	protected abstract $MSG defineTestMessage3();
 	protected abstract $MSG defineTestMessageBig();
 	
 	
 	
 	abstract class TestTemplate extends TestCase.Unit {
 		protected WorkScheduler $scheduler = new WorkSchedulerFlexiblePriority(8).start();
 		protected SelectionSignaller $selector = new SelectionSignaller(10000);
 		private WorkScheduler $ssws = new WorkSchedulerFlexiblePriority(1).start();
 		private WorkFuture<Void> $sswf;
 		{ $sswf = $selector.schedule($ssws, ScheduleParams.NOW); }
 		
 		protected Tup2<SocketChannel,SocketChannel> makeSocketChannelPair() throws IOException {
 			ServerSocketChannel $ssc = ServerSocketChannel.open();
 			$ssc.socket().bind(null);
 			SocketChannel $outsock = SocketChannel.open($ssc.socket().getLocalSocketAddress());
 			SocketChannel $insock = $ssc.accept();
 			$ssc.close();
 			return new Tup2<SocketChannel,SocketChannel>($outsock, $insock);
 		}
 
 		
 		protected InputSystem<$MSG> buildInputSystem(SocketChannel $base, final WriteHead<$MSG> $flow) {
 			final InputSystem<$MSG> $insys = InputSystem.setup(
 					$scheduler,
 					$selector,
 					$flow,
 					$base,
 					defineReader()
 			);
 			$insys.getFuture().addCompletionListener(new Listener<WorkFuture<?>>() {
 				public void hear(WorkFuture<?> $x) {
 					$flow.close();
 				}
 			});
 			return $insys;
 		}
 		
 		
 		protected OutputSystem<$MSG> buildOutputSystem(SocketChannel $base, final ReadHead<$MSG> $flow) {
 			final OutputSystem<$MSG> $outsys = OutputSystem.setup(
 					$scheduler,
 					$selector,
 					$flow,
 					$base,
 					defineWriter()
 			);
 			return $outsys;
 		}
 		
 		
 		/**
 		 * call this at the end of a test in order to stop the schedulers that
 		 * were created for this unit. failure to call this method results in
 		 * wasted resources and threads outliving their usefulness, but is
 		 * otherwise not actually very important.
 		 */
 		protected void cleanup() {
 			$log.debug("cleaning up:");
 			$log.trace("cancelling selector...");
 			$sswf.cancel(true);
 			$log.trace("stopping selector scheduler...");
 			$ssws.stop(true);
 			$log.trace("stopping main scheduler...");
 			$scheduler.stop(true);
 			$log.debug("cleanup done");
 		}
 	}
 	
 	
 	
 	/**
 	 * 
 	 * A pair of (TCP) SocketChannel are constructed; one is read from, and one is
 	 * written to (each is used unidirectionally).
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestBasic extends TestTemplate {
 		public Object call() throws IOException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $outsock = $socks.getA();
 			SocketChannel $insock = $socks.getB();
 			
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipe = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insys = buildInputSystem($insock, $incomingPipe.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipe = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsys = buildOutputSystem($outsock, $outgoingPipe.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipe.sink().write(defineTestMessage1()); 
 			
 			// do some reads, and make assertion
 			$log.debug("reading chunks...");
 			assertEquals(defineTestMessage1(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			
 			cleanup();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * 
 	 * A pair of (TCP) SocketChannel are constructed; one is read from, and one is
 	 * written to (each is used unidirectionally); two messages are sent.
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestBasicMultimessage extends TestTemplate {
 		public Object call() throws IOException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $outsock = $socks.getA();
 			SocketChannel $insock = $socks.getB();
 			
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipe = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insys = buildInputSystem($insock, $incomingPipe.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipe = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsys = buildOutputSystem($outsock, $outgoingPipe.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipe.sink().write(defineTestMessage1());
 			$outgoingPipe.sink().write(defineTestMessage2());
 			$outgoingPipe.sink().write(defineTestMessage3());
 			
 			// do some reads, and make assertion
 			$log.debug("reading chunks...");
 			assertEquals(defineTestMessage1(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			assertEquals(defineTestMessage2(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			assertEquals(defineTestMessage3(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			
 			cleanup();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * 
 	 * A pair of (TCP) SocketChannel are constructed; one is read from, and one is
 	 * written to (each is used unidirectionally); the data size is sufficient that we
 	 * should definitely be filling up the write buffers at some point.
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestBasicBig extends TestTemplate {
 		public Object call() throws IOException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $outsock = $socks.getA();
 			SocketChannel $insock = $socks.getB();
 			
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipe = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insys = buildInputSystem($insock, $incomingPipe.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipe = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsys = buildOutputSystem($outsock, $outgoingPipe.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipe.sink().write(defineTestMessageBig());
 			
 			// do some reads, and make assertion
 			// note it's tough to use assertEquals on these byte arrays because they're just huge
 			$log.debug("reading chunks...");
 			assertTrue(defineTestMessageBig().equals($incomingPipe.source().readSoon(1, TimeUnit.SECONDS)));
 			
 			cleanup();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * A pair of (TCP) SocketChannel are constructed; each is used for both writing
 	 * and reading the other and the same time; the data size is sufficient that we
 	 * should definitely be filling up the write buffers at some point.
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestBidiBig extends TestTemplate {
 		public Object call() throws IOException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $sockA = $socks.getA();
 			SocketChannel $sockB = $socks.getB();
 			
 
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipeA = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insysA = buildInputSystem($sockA, $incomingPipeA.sink());
 			final DataPipe<$MSG> $incomingPipeB = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insysB = buildInputSystem($sockB, $incomingPipeB.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipeA = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsysA = buildOutputSystem($sockA, $outgoingPipeA.source());
 			final DataPipe<$MSG> $outgoingPipeB = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsysB = buildOutputSystem($sockB, $outgoingPipeB.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipeA.sink().write(defineTestMessageBig());
 			$outgoingPipeB.sink().write(defineTestMessageBig());
 			
 			// do some reads, and make assertion
 			$log.debug("reading chunks...");
 			assertTrue(defineTestMessageBig().equals($incomingPipeA.source().readSoon(1, TimeUnit.SECONDS)));
 			assertFalse($incomingPipeA.source().hasNext());
 			assertTrue(defineTestMessageBig().equals($incomingPipeB.source().readSoon(1, TimeUnit.SECONDS)));
 			assertFalse($incomingPipeB.source().hasNext());
 			
 			cleanup();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * A pair of (TCP) SocketChannel are constructed; each is used for both writing
 	 * and reading the other and the same time; the data size is sufficient that we
 	 * should definitely be filling up the write buffers at some point.
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestBidiBigMultimessage extends TestTemplate {
 		public Object call() throws IOException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $sockA = $socks.getA();
 			SocketChannel $sockB = $socks.getB();
 			
 
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipeA = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insysA = buildInputSystem($sockA, $incomingPipeA.sink());
 			final DataPipe<$MSG> $incomingPipeB = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insysB = buildInputSystem($sockB, $incomingPipeB.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipeA = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsysA = buildOutputSystem($sockA, $outgoingPipeA.source());
 			final DataPipe<$MSG> $outgoingPipeB = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsysB = buildOutputSystem($sockB, $outgoingPipeB.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipeA.sink().write(defineTestMessageBig());
 			$outgoingPipeA.sink().write(defineTestMessageBig());
 			$outgoingPipeB.sink().write(defineTestMessageBig());
 			$outgoingPipeB.sink().write(defineTestMessageBig());
 			$outgoingPipeA.sink().write(defineTestMessageBig());
 			
 			// do some reads, and make assertion
 			$log.debug("reading chunks...");
 			assertTrue(defineTestMessageBig().equals($incomingPipeA.source().readSoon(1, TimeUnit.SECONDS)));
 			assertTrue(defineTestMessageBig().equals($incomingPipeA.source().readSoon(1, TimeUnit.SECONDS)));
			assertTrue(defineTestMessageBig().equals($incomingPipeA.source().readSoon(1, TimeUnit.SECONDS)));
 			assertFalse($incomingPipeA.source().hasNext());
 			assertTrue(defineTestMessageBig().equals($incomingPipeB.source().readSoon(1, TimeUnit.SECONDS)));
 			assertTrue(defineTestMessageBig().equals($incomingPipeB.source().readSoon(1, TimeUnit.SECONDS)));
 			assertFalse($incomingPipeB.source().hasNext());
 			
 			cleanup();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * A pair of (TCP) SocketChannel are constructed; one is read from, and one is
 	 * written to (each is used unidirectionally); two messages are written and then
 	 * the channel closed. Closure is checked after blocking for a readAll on the
 	 * inputsystem pipe.
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestClosureWaitingOnPipe extends TestTemplate {
 		public Object call() throws IOException, ExecutionException, InterruptedException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $outsock = $socks.getA();
 			SocketChannel $insock = $socks.getB();
 
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipe = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insys = buildInputSystem($insock, $incomingPipe.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipe = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsys = buildOutputSystem($outsock, $outgoingPipe.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipe.sink().write(defineTestMessage1());
 			$outgoingPipe.sink().close();
 			
 			// do some reads, and make assertion
 			$log.debug("reading chunks...");
 			assertEquals("message 1 read", defineTestMessage1(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			$incomingPipe.source().readAll();
 			assertTrue("underlying channel (write side) is closed", !$outsys.getChannel().isOpen());
 			assertTrue("underlying channel (read side) is closed", !$insys.getChannel().isOpen());
 			assertTrue("incoming pipe is closed", $incomingPipe.source().isClosed());
 			
 			// check for any errors
 			$insys.getFuture().get();
 			$outsys.getFuture().get();
 			
 			cleanup();
 			return null;
 		}
 	}
 	
 	
 	
 	/**
 	 * A pair of (TCP) SocketChannel are constructed; one is read from, and one is
 	 * written to (each is used unidirectionally); two messages are written and then
 	 * the channel closed. Closure is checked after blocking for a readAll on the
 	 * inputsystem pipe.
 	 * 
 	 * @author Eric Myhre <tt>hash@exultant.us</tt>
 	 * 
 	 */
 	private class TestClosureMultimessageWaitingOnPipe extends TestTemplate {
 		public Object call() throws IOException, ExecutionException, InterruptedException {
 			// set up ye olde sockets to stuff to test against
 			Tup2<SocketChannel,SocketChannel> $socks = makeSocketChannelPair();
 			SocketChannel $outsock = $socks.getA();
 			SocketChannel $insock = $socks.getB();
 
 			// set up the input system!
 			$log.debug("setting up InputSystem");
 			final DataPipe<$MSG> $incomingPipe = new DataPipe<$MSG>();
 			final InputSystem<$MSG> $insys = buildInputSystem($insock, $incomingPipe.sink());
 			
 			// set up the output system!
 			$log.debug("setting up OutputSystem");
 			final DataPipe<$MSG> $outgoingPipe = new DataPipe<$MSG>();
 			final OutputSystem<$MSG> $outsys = buildOutputSystem($outsock, $outgoingPipe.source());
 			
 			// do some writes
 			$log.debug("writing chunks...");
 			$outgoingPipe.sink().write(defineTestMessage1());
 			$outgoingPipe.sink().write(defineTestMessage2());
 			$outgoingPipe.sink().close();
 			
 			// do some reads, and make assertion
 			$log.debug("reading chunks...");
 			assertEquals("message 1 read", defineTestMessage1(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			assertEquals("message 3 read", defineTestMessage3(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			assertEquals("message 2 read", defineTestMessage2(), $incomingPipe.source().readSoon(1, TimeUnit.SECONDS));
 			$incomingPipe.source().readAll();
 			assertTrue("underlying channel (write side) is closed", !$outsys.getChannel().isOpen());
 			assertTrue("underlying channel (read side) is closed", !$insys.getChannel().isOpen());
 			assertTrue("incoming pipe is closed", $incomingPipe.source().isClosed());
 			
 			// check for any errors
 			$insys.getFuture().get();
 			$outsys.getFuture().get();
 			
 			cleanup();
 			return null;
 		}
 	}
 }
