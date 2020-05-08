 package fr.ribesg.alix.network;
 import fr.ribesg.alix.api.Server;
 import org.apache.log4j.Logger;
 
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.Deque;
 import java.util.concurrent.ConcurrentLinkedDeque;
 
 /**
  * This class handles sending packets.
  * TODO: All the docz
  *
  * @author Ribesg
  */
 public class SocketSender implements Runnable {
 
 	private static final Logger LOGGER = Logger.getLogger(SocketSender.class.getName());
 
 	private final BufferedWriter writer;
 	private final Deque<String>  buffer;
 
 	private final Server server;
 
 	private boolean stopAsked;
 	private boolean stopped;
 
 	/* package */ SocketSender(final Server server, final BufferedWriter writer) {
 		this.writer = writer;
 		this.buffer = new ConcurrentLinkedDeque<>();
 		this.server = server;
 		this.stopAsked = false;
 		this.stopped = true;
 	}
 
 	@Override
 	public void run() {
 		this.stopped = false;
 		String mes;
 		while (!this.stopAsked) {
 			try {
 				while ((mes = this.buffer.poll()) != null) {
 					LOGGER.debug(server.getUrl() + ':' + server.getPort() +
 					             " - SENDING MESSAGE: '" + mes.replace("\n", "\\n").replace("\r", "\\r") + "'");
 					this.writer.write(mes);
 					this.writer.flush();
 					Tools.pause(1_000);
 				}
 			} catch (final IOException e) {
				LOGGER.error("Failed to send IRC Packet", e);
 			}
 			Tools.pause(50);
 		}
 		this.kill();
 	}
 
 	public void write(final String message) {
 		this.buffer.offer(message);
 	}
 
 	public void writeFirst(final String message) {
 		this.buffer.offerFirst(message);
 	}
 
 	/* package */ boolean hasAnythingToWrite() {
 		return !this.buffer.isEmpty();
 	}
 
 	/* package */ void askStop() {
 		this.stopAsked = true;
 	}
 
 	/* package */ boolean isStopped() {
 		return this.stopped;
 	}
 
 	/* package */ void kill() {
 		try {
 			this.writer.close();
 		} catch (final IOException e) {
 			LOGGER.error("Failed to close Writer stream", e);
 		}
 		this.stopped = true;
 	}
 }
