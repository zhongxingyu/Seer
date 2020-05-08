 package communication.rudp.socket;
 
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.Timer;
 
 public class RUDPSocket implements RUDPLinkTimeoutListener {
 	private DatagramSocket sock;
 	private HashMap<InetSocketAddress,RUDPLink> links = new HashMap<InetSocketAddress,RUDPLink>();
 	private Timer timer;
 	private boolean failed = false;
 
 	public RUDPSocket() throws SocketException {
 		this.sock = new DatagramSocket();
 	}
 
 	public RUDPSocket(int port)  throws SocketException {
 		this.sock = new DatagramSocket(port);
 	}
 	
 	public RUDPSocket(SocketAddress bindaddr) throws SocketException {
 		this.sock = new DatagramSocket(bindaddr);
 	}
 
 	public RUDPSocket(int port,InetAddress inetaddr) throws SocketException {
 		this.sock = new DatagramSocket(port,inetaddr);
 	}
 	
 	public void send(RUDPDatagram datagram) throws SocketException {
 		RUDPLink link;
 		InetSocketAddress sa;
 		
		//Get or create link
 		sa = datagram.getSocketAddress();
 		link = links.get(sa);
 		if(link == null) {
 			link = new RUDPLink(sa,this, timer);
 			links.put(sa, link);
 		}
		
		//Send
		link.send(datagram);
		
 	}
 	
 	public void receive(RUDPDatagram datagram) throws SocketException {
 	}
 	
 	public boolean isFailed() {
 		return failed;
 	}
 	
 	public void restore() {
 		failed = false;
 	}
 	
 	@Override
 	public void onLinkTimeout(InetSocketAddress sa,RUDPLink link) {
 		//Link timed out => remove it from list
 		links.remove(sa);
 	}
 }
