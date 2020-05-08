 package panchat.connector;
 
 import java.util.*;
 import java.net.*;
 import java.io.*;
 
 import panchat.data.User;
 import panchat.messages.Message;
 import panchat.messages.Message.Type;
 import panchat.order.OrderLayer;
 
 /**
  * Esta gestiona los sockets con el conjunto de clientes
  * 
  * @author Jon Ander Hernández & Javier Mediavilla
  * 
  */
 public class MulticastConnector extends OrderLayer implements Connector {
 
 	public final static boolean DEBUG = false;
 
 	/*
 	 * Constantes y variables para el socket multicast
 	 */
 	public final static String MDNS_GROUP = "224.0.0.251";
 	public final static int MDNS_PORT = 5454;
 	public final static int LOCALPORT = 50000;
 	public final static int PORTMAX = LOCALPORT + 100;
 
 	/**
 	 * This is the multicast group, we are listening to for multicast DNS
 	 * messages.
 	 */
 	private MulticastSocket socket;
 	private InetAddress group;
 
 	private SocketListenerThread multicastThread;
 
 	/**
 	 * 
 	 * @param user
 	 * @throws Exception
 	 */
 	public MulticastConnector(User user) {
 		super(user);
 
 		/*
 		 * Creamos un server Socket
 		 */
 		printDebug("Creando SocketMulticast");
 
 		/*
 		 * Creamos el Socket multicast
 		 */
 		try {
 			group = InetAddress.getByName(MDNS_GROUP);
 
 			socket = new MulticastSocket(MDNS_PORT);
 
 			socket.joinGroup(group);
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		/*
 		 * Creamos un MulticastListenerThread para escuchar los eventos
 		 * Multicast
 		 */
 		printDebug("Creando el MulticastListenerThread 0:-)");
 
 		this.multicastThread = new SocketListenerThread(this);
 		this.multicastThread.start();
 	}
 
 	/*
 	 * Métodos del OrderLayer
 	 */
 
 	@Override
	public synchronized void sendMsg(List<User> users, Message msg) {
 		write(msg);
 	}
 
 	@Override
 	public synchronized void sendMsg(User user, Message msg) {
 		write(msg);
 	}
 
 	@Override
 	protected boolean okayToRecv(Message msg) {
 		return true;
 	}
 
 	@Override
 	public Type orderCapability() {
 		return Message.Type.MULTICAST;
 	}
 
 	/*
 	 * Funciones del connector
 	 */
 
 	/**
 	 * Lee un objeto de un socket multicast
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	public void write(Object objeto) {
 
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		ObjectOutputStream oos;
 
 		try {
 
 			oos = new ObjectOutputStream(baos);
 			oos.writeObject(objeto);
 			oos.close();
 
 		} catch (IOException e1) {
 			e1.printStackTrace();
 		}
 		byte[] buffer = baos.toByteArray();
 
 		DatagramPacket hi = new DatagramPacket(buffer, buffer.length, group,
 				MDNS_PORT);
 		try {
 			socket.send(hi);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Lee un objeto de un socket multicast
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	@Override
 	public Object read(User user) {
 
 		// Creamos un buffer donde guardar lo leído
 		byte[] buf = new byte[1000];
 
 		DatagramPacket recv = new DatagramPacket(buf, buf.length);
 
 		// Leemos del Socket
 		try {
 			socket.receive(recv);
 
 			// Creamos un ByteArrayInputStream desde donde serializar el objeto
 			ByteArrayInputStream bais = new ByteArrayInputStream(
 					recv.getData(), 0, recv.getData().length);
 			ObjectInputStream ois;
 
 			Object objeto;
 
 			// Intentamos leer el objeto
 			try {
 
 				ois = new ObjectInputStream(bais);
 				objeto = ois.readObject();
 				ois.close();
 
 				return objeto;
 
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 
 		} catch (IOException e2) {
 			// Se ha producido un error con el socket
 		}
 
 		// Si algo ha salido mal, devolvemos null
 		return null;
 	}
 
 	@Override
 	public void receive(Message msg) {
 		this.deliveryQueue.add(msg);
 		this.setChanged();
 		this.notifyObservers(this);
 	}
 
 	@Override
 	public boolean isClosed(User user) {
 		return socket.isClosed();
 	}
 
 	@Override
 	public void close() {
 		printDebug("Cerrando Socket multicast");
 
 		try {
 
 			// Cerramos el socket multicast
 			socket.close();
 
 		} catch (Exception e) {
 		}
 
 		printDebug("Cerrado el socket multicastThread");
 
 		// Esperamos a que termine el multicastThread
 		try {
 			multicastThread.join();
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		printDebug("Parado el multicastThread");
 	}
 
 	/*
 	 * funciones de debug
 	 */
 	private void printDebug(String string) {
 
 		final String msgClase = "Connector.java: ";
 
 		if (DEBUG)
 			System.out.println(msgClase + string);
 	}
 }
