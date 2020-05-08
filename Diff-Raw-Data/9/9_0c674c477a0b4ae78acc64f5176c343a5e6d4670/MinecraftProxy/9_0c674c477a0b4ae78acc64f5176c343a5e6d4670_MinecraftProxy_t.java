 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Inet4Address;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Map.Entry;
 
 
 public class MinecraftProxy extends Thread {
 	
 	private static final int DEFAULT_PORT = 25565;
 	private static final String PLAYER = "Player";
 	private static final String PROPERTIES_FILE = "static-players.properties";
 	
 	private class Worker {
 		public Worker(Socket proxyClientSocket, String name) throws IOException {
 			Socket serverClientSocket = new Socket(
 					serverSocketAddress.getAddress(),
 					serverSocketAddress.getPort());
 			
 			InputStream proxyClientIn = proxyClientSocket.getInputStream();
 			proxyClientIn = new MinecraftPlayerInputFilterStream(proxyClientIn,
 					PLAYER, name);
 			OutputStream proxyClientOut = proxyClientSocket.getOutputStream();
 			InputStream serverClientIn = serverClientSocket.getInputStream();
 			// serverClientIn = new
 			// MinecraftPlayerInputFilterStream(serverClientIn, name,
 			// PLAYER);
 			OutputStream serverClientOut = serverClientSocket.getOutputStream();
 			
 			new InputStreamPipe(proxyClientIn, serverClientOut);
 			new InputStreamPipe(serverClientIn, proxyClientOut);
 		}
 	}
 	
 	private final NameFactory nameFactory;
 	private final ServerSocket proxyServerSocket;
 	private final InetSocketAddress serverSocketAddress;
 	private final Map<InetAddress, String> nameForAddress = new HashMap<InetAddress, String>();
 	
 	public MinecraftProxy(int port, InetSocketAddress serverSocketAddress)
 			throws IOException {
 		this(new CountingNameFactory(), port, serverSocketAddress);
 	}
 	
 	public MinecraftProxy(String username, int port,
 			InetSocketAddress serverSocketAddress) throws IOException {
 		this(new StaticNameFactory(username), port, serverSocketAddress);
 	}
 	
 	public MinecraftProxy(NameFactory nameFactory, int port,
 			InetSocketAddress serverSocketAddress) throws IOException {
 		this.nameFactory = nameFactory;
 		this.proxyServerSocket = new ServerSocket(port);
 		this.serverSocketAddress = serverSocketAddress;
 		
 		Properties staticNames = new Properties();
 		staticNames.load(new FileInputStream(PROPERTIES_FILE));
 		for (Entry<Object, Object> entry : staticNames.entrySet()) {
 			nameForAddress.put(Inet4Address.getByName((String) entry.getKey()),
 					(String) entry.getValue());
 		}
 		
 		start();
 	}
 	
 	@Override
 	public void run() {
 		while (true) {
 			try {
 				Socket socket = proxyServerSocket.accept();
 				InetAddress address = socket.getInetAddress();
				String name = nameForAddress.get(address);
				
				if (name == null) {
					name = nameFactory.getName();
					nameForAddress.put(address, name);
				}
 				
 				new Worker(socket, name);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public static void main(String[] args) throws Throwable {
 		InetSocketAddress socketAddress;
 		int port;
 		
 		try {
 			String[] socketAddressString = args[0].split(":");
 			socketAddress = new InetSocketAddress(
 					socketAddressString[0],
 					(socketAddressString.length > 1) ? Integer.parseInt(socketAddressString[1])
 							: 25565);
 			port = (args.length > 1) ? Integer.parseInt(args[1]) : DEFAULT_PORT;
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println();
 			System.err.println("usage: <program> <srvAddress>:<srvPort> [<proxyPort> [<staticName>]]");
 			System.exit(-1);
 			return;
 		}
 		
 		if (args.length > 2) new MinecraftProxy(args[2], port, socketAddress);
 		else new MinecraftProxy(port, socketAddress);
 		
 		System.out.println("proxy server started...");
 	}
 	
 }
