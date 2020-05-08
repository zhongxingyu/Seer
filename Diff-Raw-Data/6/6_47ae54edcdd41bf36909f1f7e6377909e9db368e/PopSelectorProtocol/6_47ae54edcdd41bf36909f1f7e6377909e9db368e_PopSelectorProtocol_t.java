 package nio.server;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.text.ParseException;
 
 import proxy.Popeye;
 import proxy.Writeable;
 import service.Olivia;
 import user.User;
 import config.Configuration;
 import connection.Connection;
 import connection.PopConnection;
 
 public class PopSelectorProtocol implements SelectorProtocol, Writeable {
 	private int bufSize; // Size of I/O buffer
 	private int defaultPort;
 	private String defaultServer;
 	//private Map<SocketChannel,SocketChannel> clientMap=new HashMap<SocketChannel,SocketChannel>();
 	//private Map<SocketChannel,SocketChannel> serverMap=new HashMap<SocketChannel,SocketChannel>();
 	//private Map<SocketChannel,Popeye> proxyMap=new HashMap<SocketChannel,Popeye>();
 	//private Map<SocketChannel, Boolean> connection = new HashMap<SocketChannel, Boolean>();
 	private Selector selector;
 
 	public PopSelectorProtocol(int bufSize, Selector selector) {
 		this.bufSize = bufSize;
 		this.defaultPort=Configuration.getInstance().getDefaultPort();
 		this.selector=selector;
 		this.defaultServer=Configuration.getInstance().getDefaultServer();
 	}
 
 	public void handleAccept(SelectionKey key) throws IOException {
 		SocketChannel clntChan = ((ServerSocketChannel) key.channel()).accept();
 		String address=clntChan.socket().getRemoteSocketAddress().toString();
 		address=address.substring(1, address.indexOf(':'));
 		System.out.println(address);
 		if(!Popeye.isBlocked(address)){
 			clntChan.configureBlocking(false); // Must be nonblocking to register
 			// Register the selector with new channel for read and attach byte
 			// buffer
 			System.out.println("Accepted connection ->"+clntChan.socket().getRemoteSocketAddress());
 			PopConnection con = new PopConnection(bufSize,this,clntChan);
 			clntChan.register(key.selector(), SelectionKey.OP_READ, con);
 			connectToServer(con, defaultServer);
 		}else{
 			System.out.println("Blocked: "+address);
 			clntChan.close();
 		}
 	}
 
 	private void connectToServer(PopConnection con, String serverName) throws IOException{
 		SocketChannel clntChan = con.getClient();
 		SocketChannel server=con.getServer();
 		if(server!=null){
 			String address=server.socket().getRemoteSocketAddress().toString();
 			if(address.substring(0, address.indexOf('/')).equals(serverName)){
 				System.out.println("same address");
				return;
 			}
 			con.setConnection(false);
 		}
 		System.out.println("Server:"+serverName+" port:"+defaultPort);
 		SocketChannel hostChan = SocketChannel.open(new InetSocketAddress(serverName, defaultPort));
 		hostChan.configureBlocking(false); // Must be nonblocking to register
 		System.out.println("Creating connection ->"+hostChan.socket().getRemoteSocketAddress());
 		hostChan.register(selector, SelectionKey.OP_READ, con);
 		System.out.println("client:"+clntChan);
 		System.out.println("host:"+hostChan);
 		con.setServer(hostChan);
 	}
 
 	public void handleRead(SelectionKey key) throws IOException, InterruptedException, ParseException {
 		// Client socket channel has pending data
 		SocketChannel channel = (SocketChannel) key.channel();
 		PopConnection con =((PopConnection) key.attachment());
 		StringBuffer sBuf;
 		boolean isClient=con.isClient(channel);
 		if(con.isClient(channel)){
 			sBuf = con.getClientBuffer().getReadBuffer();
 		}else {
 			sBuf = con.getServerBuffer().getReadBuffer();
 		}
 		ByteBuffer buf=ByteBuffer.allocate(bufSize);
 		long bytesRead = channel.read(buf);
 		buf.flip();
 		if (bytesRead == -1) { // Did the other end close?
 			if(!isClient){
 				//SERVER DISCONNECTED
 				System.out.println("Server disconnected (client:"+con.getClient().socket().getRemoteSocketAddress()+")");
 			}else{
 				//CLIENT DISCONNECTED
 				System.out.println("Client disconnected:"+channel.socket().getRemoteSocketAddress());
 			}
 			disconnectClient(con);
 			return;
 		} else if (bytesRead > 0) {
 			String line=BufferUtils.bufferToString(buf);
 			sBuf.append(line);
 			if(!line.endsWith("\r\n")){
 				return;
 			}
 			line=sBuf.toString();
 			sBuf.delete(0, sBuf.length());
 			//System.out.print("READ:"+bytesRead+" "+line);
 			if(!isClient){
 				//SERVER
 				for(String s: line.split("\r\n")){
 					con.getProxy().proxyServer(s.concat("\r\n"));
 				}
 				//proxyMap.get(clientMap.get(channel)).proxyServer(line);
 			}else{
 				//CLIENT
 				//TODO turbio
 				//if(serverMap.get(channel)==null){
 				if(line.startsWith("USER")){
 					//AUTHENTICATION
 					System.out.println("AUTHENTICATION");
 					String serverName=con.getProxy().login(line);
 					if(serverName==null){
 						//FAIL
 						writeToClient(con, "-ERR\r\n");
 						return;
 					}else{
 						connectToServer(con, serverName);
 						writeToServer(con, line);
 					}
 				}else{
 					//NORMAL FLOW
 					for(String s: line.split("\r\n")){
 						con.getProxy().proxyClient(s.concat("\r\n"));
 					}
 					//proxyMap.get(channel).proxyClient(line);
 				}
 			}
 		}
 	}
 
 	private void disconnectClient(PopConnection con) throws IOException {
 		SocketChannel server=con.getServer();
 		if(server!=null){
 			disconnect(server);
 		}
 		disconnect(con.getClient());
 	}
 
 	public void disconnect(SocketChannel channel) throws IOException{
 		SelectionKey key=channel.keyFor(selector);
		System.out.println(selector.keys().size());
 		if(key!=null){
 			key.cancel();
 		}
 		channel.close();
 	}
 	
 	public void handleWrite(SelectionKey key) throws IOException {
 		PopConnection con = ((PopConnection) key.attachment());
 		/*
 		 * Channel is available for writing, and key is valid (i.e., client
 		 * channel not closed).
 		 */
 		// Retrieve data read earlier
 		//buf.flip(); // Prepare buffer for writing
 		SocketChannel channel = (SocketChannel) key.channel();
 		StringBuffer sBuf = con.getBuffer(channel).getWriteBuffer();
 		//System.out.println("write ("+sBuf+")");
 		//buf.flip();
 		ByteBuffer buf=ByteBuffer.wrap(sBuf.toString().getBytes());
 		int bytesWritten=channel.write(buf);
 		if (!buf.hasRemaining()) { // Buffer completely written?
 			//System.out.println("wrote all");
 			//System.out.println(BufferUtils.bufferToString(buf));
 			// Nothing left, so no longer interested in writes
 			key.interestOps(SelectionKey.OP_READ);
 		}
 		sBuf.delete(0, bytesWritten);
 		buf.compact(); // Make room for more data to be read in
 		buf.clear();
 	}
 
 	public void writeToClient(Connection con, String line) throws IOException, InterruptedException {
 		PopConnection pcon = (PopConnection)con;
 		SocketChannel client = con.getClient();
 		if(!pcon.getConnection()){
 			pcon.setConnection(true);
 		}else{
 			String message=line.length()>30?line.substring(0, 30)+"...\n":line;
 			System.out.print("S--> "+message + " to " +getUser(pcon));
 			writeToChannel(client,line,pcon.getClientBuffer().getWriteBuffer());
 			countBytes(pcon, line.length());
 		}
 	}
 
 	private String getUser(PopConnection con) {
 		return con.getProxy().getCurrentUserName();
 	}
 
 	public void writeToServer(Connection con, String line) throws IOException, InterruptedException {
 		PopConnection pcon=(PopConnection)con;
 		SocketChannel client=con.getClient();
 		String message=line.length()>30?line.substring(0, 30)+"...\n":line;
 		System.out.print("C ("+getUser(pcon)+")--> "+message);
 		SocketChannel server=pcon.getServer();
 		writeToChannel(server, line, pcon.getServerBuffer().getWriteBuffer());
 		countBytes(pcon, line.length());
 	}
 	
 	private void countBytes(PopConnection con, int size){
 		Olivia.addBytes(size);
 		User u =con.getProxy().getCurrentUser();
 		if(u!=null){
 			u.getStats().addBytes(size);
 		}
 	}
 
 	private void writeToChannel(SocketChannel channel, String line, StringBuffer sBuf) throws InterruptedException, IOException{
 		SelectionKey key=channel.keyFor(selector);
 		String before=sBuf.toString();
 		/*if(buf.hasRemaining()){
 			buf.compact();
 		}*/
 		sBuf.append(line);
 		String after=sBuf.toString();
 		key.interestOps(SelectionKey.OP_READ|SelectionKey.OP_WRITE);
 	}
 }
