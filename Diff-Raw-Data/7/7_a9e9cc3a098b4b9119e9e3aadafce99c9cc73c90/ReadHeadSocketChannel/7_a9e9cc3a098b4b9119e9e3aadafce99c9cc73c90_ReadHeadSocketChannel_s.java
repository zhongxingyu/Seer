 package us.exultant.ahs.io;
 
 import us.exultant.ahs.core.*;
 import java.io.*;
 import java.net.*;
 import java.nio.channels.*;
 
 /**
  * Creates a server socket for accepting new socket connections, makes it nonblocking, and
  * decorates the whole thing as a {@link ReadHead}.
  * 
  * The new {@link SocketChannel} returned when reading from this ReadHead are already
  * configured to be nonblocking themselves (but of course have not yet been registered
  * with any {@link PumperSelector}, since they aren't yet wrapped in any of the other
  * abstractions typical of the AHS library that would make a relationship with a
  * PumperSelector appropriate).
  * 
  * @author hash
  * 
  */
 public class ReadHeadSocketChannel extends ReadHeadAdapter<SocketChannel> {
 	// I could have done all this with some interesting hacks and extending ReadHeadAdapter.ChannelwiseSelecting and hurling around wild nulls and having Translator with a shitload of state (including the serversocket) and making more abstract methods about closure state... but that seemed like more work than it was worth just to keep the same pump.  Oh, and I guess there would have been issues with exceptions from different places too. 
 	
 	/**
 	 * Opens and binds a new non-blocking server socket, registers it with a selector,
 	 * and leaves you with a lovely ReadHead interface ready to rock.
 	 * 
 	 * @param $localBinding
 	 *                as per {@link ServerSocket#bind(SocketAddress)}.
 	 * @param $ps
 	 *                the PumperSelector to register this server socket with (the
 	 *                registration will be performed by the time this constructor
 	 *                returns).
 	 * 
 	 * @throws IOException
 	 *                 if the bind operation fails, or if the socket is already bound.
 	 * @throws SecurityException
 	 *                 if a SecurityManager is present and its checkListen method
 	 *                 doesn't allow the operation.
 	 * @throws IllegalArgumentException
 	 *                 if endpoint is a SocketAddress subclass not supported by this
 	 *                 socket
 	 */
 	public ReadHeadSocketChannel(SocketAddress $localBinding, PumperSelector $ps) throws IOException {
 		this.$pump = new PumpT();
 		
 		$ssc = ServerSocketChannel.open();
 		$ssc.configureBlocking(false);
 		$ssc.socket().bind($localBinding);
 		$ps.register($ssc, getPump());
 		this.$ps = $ps;
 	}
 	
 	private final ServerSocketChannel	$ssc;
 	private final PumpT			$pump;
 	private final PumperSelector		$ps;
 	
 	/**
 	 * Exposes the {@link ServerSocketChannel} that this ReadHead is decorating. (If
 	 * you want to, for example, see what port was bound to after starting the socket,
 	 * you can get it from this.)
 	 */
 	public ServerSocketChannel getServerSocketChannel() {
 		return $ssc;
 	}
 	
 	public Pump getPump() {
 		return $pump;
 	}
 	
 	/**
 	 * Closes the {@link ServerSocketChannel} that this ReadHead is decorating (as per
 	 * the general contract for {@link ReadHead#close()}), then instructs the
 	 * {@link PumperSelector} that has been handling events for this channel to
 	 * deregister it as soon as has a chance to do so.
 	 */
 	public void close() throws IOException {
 		$ssc.close();
 		$ps.deregister($pump);
 	}
 	
 	
 	
 	
 	
 	private class PumpT implements Pump {
 		public boolean isDone() {
 			return isClosed();
 		}
 		
 		public synchronized void run(final int $times) {
 			for (int $i = 0; $i < $times; $i++) {
 				if (isDone()) break;
 				if (!$ssc.isOpen()) {
 					baseEof();
 					break;
 				}
 				
 				try {
 					try {
 						SocketChannel $chunk = $ssc.accept();
 						
 						if ($chunk == null) break;
 						$chunk.configureBlocking(false);
 						
 						$pipe.SINK.write($chunk);
 					} catch (ClosedChannelException $e) {
 						baseEof();
 					}
 				} catch (IOException $e) {
 					ExceptionHandler<IOException> $dated_eh = $eh;
 					if ($dated_eh != null) $dated_eh.hear($e);
 					break;
 				}
 			}
 		}
 	}
 }
