 package bytecrawl.evtj.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.LinkedList;
 import java.util.Stack;
 
 import bytecrawl.evtj.server.handlers.EventHandler;
 import bytecrawl.evtj.server.handlers.ExpeditorHandler;
 import bytecrawl.evtj.server.handlers.GatewayHandler;
 import bytecrawl.evtj.utils.NetEvent;
 import bytecrawl.evtj.utils.SocketBook;
 
 public class EvtJServer {
 	
 	private boolean active;
 	private boolean	paused;
 	private boolean	initialising;
 	private int connected_clients;
 	private int queued_events; // Unused
 	
 	private ServerSocket server_socket;
 	
 	private SocketBook socket_book;
 	private LinkedList<NetEvent> event_book;
 	
 	private Stack<EvtJExecutor> executor_pool; // Unused
 	
 	private EvtJExecutor gateway_executor;
 	private EvtJExecutor handler_executor;
 	private EvtJExecutor expeditor_executor;
 	
 	private int port;
 
 	public EvtJServer(int port)
 	{
 		try
 		{
 			server_socket = new ServerSocket(port);
 			socket_book = new SocketBook();
 			event_book = new LinkedList<NetEvent>();
 		}catch(IOException e){
 			System.out.println("EvtJServer could not bind the port "+port);
 			System.exit(1);
 		}finally{
 			this.port = port;
 			connected_clients = 0;
 		}
 	}
 	
 	public synchronized boolean isActive() { return active; }
 	public synchronized boolean isPaused() { return paused; }
 	public synchronized boolean isInitialising() { return initialising; }
 	
 	public synchronized int getConnectedClients() { return connected_clients; }
 	public synchronized ServerSocket getServerSocket() { return server_socket; }
 	public synchronized SocketBook getSocketBook() { return socket_book; }
 	public synchronized LinkedList<NetEvent> getEventBook() { return event_book; }
 
 	public synchronized void pause() { paused = true; }
 	public synchronized void resume() { paused = false; }
 	
 	public synchronized void addSocketToBook(Socket s)
 	{
 		socket_book.add(s);
 		connected_clients++;
 	}
 	
 	public synchronized void removeSocketFromBook(int index)
 	{
 		socket_book.remove(index);
 		connected_clients--;
 	}
 	
 	public synchronized void addEventToBook(NetEvent evt)
 	{
 		event_book.addLast(evt);
 		queued_events++;
 	}
 	
 	public synchronized void removeEventFromBook(int index)
 	{
 		event_book.remove(index);
 		queued_events--;
 	}
 	
	public void start_handler()
 	{
 		EventHandler handler = new EventHandler(this);
 		handler_executor = new EvtJExecutor(this, handler);
 		handler_executor.start();
 		while(!handler_executor.isAlive()) {};
 	}
 	
	public void start_gateway()
 	{
 		GatewayHandler handler = new GatewayHandler(this);
 		gateway_executor = new EvtJExecutor(this, handler);
 		gateway_executor.start();
 		while(!gateway_executor.isAlive()) {}
 	}
 	
	public void start_expeditor()
 	{
 		ExpeditorHandler handler = new ExpeditorHandler(this);
 		expeditor_executor = new EvtJExecutor(this, handler);
 		expeditor_executor.start();
 		while(!expeditor_executor.isAlive()) {}
 	}
 
 	public void start()
 	{
 		initialising = true;
 		start_gateway();
 		start_handler();
 		start_expeditor();
 		paused = false;
 		active = true;
 		initialising = false;
 	}
 	
 	public void stop()
 	{
 		paused = false;
 		active = false;
 
 		handler_executor.interrupt();
 		gateway_executor.interrupt();
 		expeditor_executor.interrupt();
 		
 		try {
 			server_socket.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		while(gateway_executor.isAlive()) {}
 		while(handler_executor.isAlive()) {}
 		while(expeditor_executor.isAlive()) {}
 		
 		System.out.println("Server stopped\n");
 	}
 	
 }
