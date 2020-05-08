 /*
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *
  */
 package net.chainmq;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import org.apache.log4j.Logger;
 
 /**
  * Basic Server
  * 
  * @author Guillermo Grandes / guillermo.grandes[at]gmail.com
  */
 public class Server implements Runnable {
 	private static final Logger log = Logger.getLogger(Server.class);
 	private ReservedJobsExpirer expirer = new ReservedJobsExpirer();
 	private long started = 0;
 
 	private InetAddress listenAddress = null;
 	private int listenPort = 11300;
 
 	public ReservedJobsExpirer getReservedJobsExpirer() {
 		return expirer;
 	}
 
 	public InetAddress getListenAddress() {
 		return listenAddress;
 	}
 
 	public void setListenAddress(final InetAddress listenAddress) {
 		this.listenAddress = listenAddress;
 	}
 
 	public int getListenPort() {
 		return listenPort;
 	}
 
 	public void setListenPort(int listenPort) {
 		this.listenPort = listenPort;
 	}
 
 	public int getUptime() {
 		return (int) ((System.currentTimeMillis() - started) / 1000);
 	}
 
 	public static void main(final String[] args) throws Throwable {
 		final Server srv = new Server();
 		final Thread handleListenerSocket = new Thread(srv);
 		for (int i = 0; i < args.length; i++) {
 			final String arg = args[i];
 			if (arg.equals("-h")) {
 				showHelp();
 			} else if (arg.equals("-l")) {
				final String value = args[++i];
 				srv.setListenAddress(InetAddress.getByName(value));
 			} else if (arg.equals("-p")) {
				final String value = args[++i];
 				srv.setListenPort(Integer.valueOf(value));
 			}
 		}
 		handleListenerSocket.start();
 	}
 
 	private static void showHelp() {
 		System.out.println("Options:");
 		System.out.println(" -l ADDR  listen on address (default is 0.0.0.0)");
 		System.out.println(" -p PORT  listen on port (default is 11300)");
 		System.out.println(" -h       show this help");
 		System.exit(0);
 	}
 
 	@Override
 	public void run() {
 		//
 		AsyncTaskTracker tracker = null;
 		ServerSocketChannel ssc = null;
 		Selector s = null;
 		started = System.currentTimeMillis();
 		expirer.start();
 		try {
 			ssc = ServerSocketChannel.open();
 			ssc.configureBlocking(false);
 			ssc.socket().bind(new InetSocketAddress(listenAddress, listenPort));
 			s = Selector.open();
 			ssc.register(s, SelectionKey.OP_ACCEPT);
 			tracker = new AsyncTaskTracker(Executors.newScheduledThreadPool(4));
 		} catch (IOException e) {
 			log.error("IOException before run(): " + e.toString(), e);
 			throw new RuntimeException(e);
 		}
 		log.info("Accepting connections on " + ssc.socket());
 		while (true) {
 			// tracker.dumpStats();
 			try {
 				final int events = s.select(1000);
 				if (events <= 0) {
 					continue;
 				}
 			} catch (IOException e) {
 				log.error("IOException in select(): " + e.toString(), e);
 			}
 			final Set<SelectionKey> keys = s.selectedKeys();
 			final Iterator<SelectionKey> i = keys.iterator();
 			while (i.hasNext()) {
 				final SelectionKey key = i.next();
 				try {
 					if (key.isAcceptable()) {
 						final ServerSocketChannel schan = (ServerSocketChannel) key.channel();
 						final SocketChannel sc = schan.accept();
 						sc.configureBlocking(false);
 						final ClientHandler cli = new ClientHandler(this, s, sc, tracker);
 						log.info(cli.getName() + " New connection");
 						sc.register(s, SelectionKey.OP_READ, cli);
 					}
 					if (key.isValid() && key.isReadable()) {
 						final ClientHandler ctx = (ClientHandler) key.attachment();
 						ctx.doRead();
 					}
 					if (key.isValid() && key.isWritable()) {
 						final ClientHandler ctx = (ClientHandler) key.attachment();
 						ctx.doWrite();
 					}
 				} catch (ClosedChannelException e) {
 					log.error("ClosedChannelException: " + e.toString());
 					final ClientHandler ctx = (ClientHandler) key.attachment();
 					ctx.doClose();
 				} catch (IOException e) {
 					log.error("IOException: " + e.toString(), e);
 					final ClientHandler ctx = (ClientHandler) key.attachment();
 					ctx.doClose();
 				} catch (Exception e) {
 					log.error("Exception: " + e.toString(), e);
 					final ClientHandler ctx = (ClientHandler) key.attachment();
 					ctx.doClose();
 				}
 				i.remove();
 			}
 		}
 	}
 
 }
