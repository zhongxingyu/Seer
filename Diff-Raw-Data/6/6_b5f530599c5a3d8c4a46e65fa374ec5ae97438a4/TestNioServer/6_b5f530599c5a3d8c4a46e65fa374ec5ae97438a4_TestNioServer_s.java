 package rich.net.nio;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.nio.charset.Charset;
 import java.util.Iterator;
import java.util.Set;
 
 import rich.util.TestUtil;
 
import com.gargoylesoftware.htmlunit.javascript.host.Selection;

 public class TestNioServer implements Runnable {
 
 	ServerSocketChannel ssc;
 	int port = 9876;
 	Selector selector;
 	int capasicy = 10;
 	ByteBuffer buffer = ByteBuffer.allocate(capasicy);
 
 	public TestNioServer() {
 
 	}
 
 	private void initData() {
 		try {
 			TestUtil.printLog("init the data.....");
 			ssc = ServerSocketChannel.open();
 			ssc.socket().bind(new InetSocketAddress(port));
 			ssc.configureBlocking(false);
 
 			selector = Selector.open();
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 	public void process() throws IOException {
 		initData();
 		SelectionKey serverKey = ssc.register(selector, SelectionKey.OP_ACCEPT);
 		TestUtil.printLog("init finished,will loop....");
 		while (true) {
 			TestUtil.printLog("waiting for selector event...");
 
 			while (selector.select() > 0) {
 
 				//				printLog("selector.keys() is :" + selector.keys().size());
 				//				printLog("selector.selectedKeys() is :" + selector.selectedKeys().size());
 
 				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
 				while (it.hasNext()) {
 					SelectionKey skey = it.next();
 					it.remove();
 
 					if (!skey.isValid() || !skey.channel().isOpen())
 						continue;
 
 					if (skey.isAcceptable()) {
 						accept(skey);
 					} else if (skey.isReadable()) {
 						read(skey);
 
 					} else if (skey.isWritable()) {
 						write(skey);
 
 					}
 				}
 			}
 
 		}
 	}
 
 	private void write(SelectionKey skey) throws IOException {
 		TestUtil.printLog("writing data to socket...");
 		SocketChannel socketChannel = (SocketChannel) skey.channel();
 		//		Charset.forName("us-ascii").newEncoder().encode(buffer);
 
 		buffer.put("nimen hao".getBytes());
 		buffer.flip();
 		socketChannel.write(buffer);
 		buffer.compact();
 		socketChannel.register(selector, SelectionKey.OP_READ);
 	}
 
 	private void read(SelectionKey skey) throws IOException {
 		TestUtil.printLog("Reading data from channel...");
 		SocketChannel socketChannel = (SocketChannel) skey.channel();
 
 		int result;
 		while ((result = socketChannel.read(buffer)) > 0 && buffer.position() > 0) {
 			try {
 				TestUtil.printLog("result is : " + result);
 				buffer.flip();
 				CharBuffer ch = Charset.forName("utf-8").newDecoder().decode(buffer);
 				String readFrom = ch.toString();
 				
 				TestUtil.printLog("Read data from socket is :" + readFrom);
 
 			} finally {
 				buffer.compact();
 			}
 		}
 		if (result < 0) {
 			socketChannel.close();
 		}else if (result == 0){
 			socketChannel.register(selector, SelectionKey.OP_WRITE);
 		}
 
 	}
 
 	private void accept(SelectionKey skey) throws IOException {
 		TestUtil.printLog("accept the skey, and reggister this channel");
 		ServerSocketChannel ssc = (ServerSocketChannel) skey.channel();
 		SocketChannel socketChannel = ssc.accept();
 		socketChannel.configureBlocking(false);
 		socketChannel.register(selector, SelectionKey.OP_READ);
 
 	}
 
 	@Override
 	public void run() {
 		try {
 			process();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 
 
 
 	public static void main(String[] args) {
 		new Thread(new TestNioServer()).start();
 	}
 }
