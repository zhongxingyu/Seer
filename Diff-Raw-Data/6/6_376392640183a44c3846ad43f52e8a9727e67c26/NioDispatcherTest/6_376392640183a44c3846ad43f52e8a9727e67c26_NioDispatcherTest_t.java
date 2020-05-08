 package org.net;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.junit.Test;
 import org.net.ChannelListeners.BufferedChannel;
 import org.net.ChannelListeners.RequestResponseChannel;
 import org.net.ChannelListeners.RequestResponseMatcher;
 import org.net.NioDispatcher.ChannelListenerFactory;
 import org.util.IoUtil;
 import org.util.Util;
 import org.util.Util.Event;
 
 public class NioDispatcherTest {
 
 	@Test
 	public void testTextExchange() throws IOException {
 		final String hello = "HELLO";
 
 		NioDispatcher<BufferedChannel> dispatcher = new NioDispatcher<BufferedChannel>(
 				new ChannelListenerFactory<BufferedChannel>() {
 					public BufferedChannel create() {
 						return new BufferedChannel() {
 							public void onConnected() {
 								String s = hello + "\n";
 								send(new Bytes(s.getBytes(IoUtil.charset)));
 							}
 
 							public void onReceive(Bytes request) {
 								send(request);
 							}
 						};
 					}
 				});
 
 		dispatcher.spawn();
 		Event closeServer = dispatcher.listen(5151);
 
 		Socket socket = new Socket("localhost", 5151);
 		InputStream is = socket.getInputStream();
 		OutputStream os = socket.getOutputStream();
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		PrintWriter writer = new PrintWriter(os);
 
 		try {
 			String m = "testing nio";
 			writer.println(m);
 			writer.flush();
 
 			assertEquals(hello, reader.readLine());
 			assertEquals(m, reader.readLine());
 		} finally {
 			Util.closeQuietly(reader);
 			Util.closeQuietly(writer);
 		}
 
 		closeServer.perform(null);
 		dispatcher.unspawn();
 	}
 
 	@Test
 	public void testRequestResponse() throws IOException, InterruptedException {
 		final RequestResponseMatcher matcher = new RequestResponseMatcher();
 		final ThreadPoolExecutor executor = Util.createExecutor();
 
 		NioDispatcher<RequestResponseChannel> dispatcher = new NioDispatcher<RequestResponseChannel>(
 				new ChannelListenerFactory<RequestResponseChannel>() {
 					public RequestResponseChannel create() {
 						return new RequestResponseChannel(matcher, executor) {
 							public Bytes respondToRequest(Bytes request) {
 								return request;
 							}
 						};
 					}
 				});
 
 		dispatcher.spawn();
 		Event closeServer = dispatcher.listen(5151);
 
 		RequestResponseChannel client = dispatcher
 				.connect(new InetSocketAddress(InetAddress.getLocalHost(), 5151));
 
 		for (String s : new String[] { "ABC", "WXYZ", "" }) {
			byte bs[] = s.getBytes(IoUtil.charset);
			Bytes request = new Bytes(bs);
			Bytes response = matcher.requestForResponse(client, request);
			assertEquals(request, response);
 			System.out.println("Request '" + s + "' is okay");
 		}
 
 		closeServer.perform(null);
 		executor.awaitTermination(0, TimeUnit.SECONDS);
 		dispatcher.unspawn();
 	}
 
 }
