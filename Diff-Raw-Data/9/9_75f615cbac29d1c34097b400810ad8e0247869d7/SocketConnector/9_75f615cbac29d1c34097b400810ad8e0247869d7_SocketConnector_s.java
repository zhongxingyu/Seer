 package panchat.connector;
 
 import java.util.*;
 import java.util.Map.Entry;
 import java.net.*;
 import java.io.*;
 
 import panchat.data.User;
 import panchat.messages.Message;
 import panchat.messages.Message.Type;
 import panchat.messages.register.UserMessage;
 import panchat.order.OrderLayer;
 
 /**
  * Esta gestiona los sockets con el conjunto de clientes
  * 
  * @author Jon Ander Hernández & Javier Mediavilla
  * 
  */
 public class SocketConnector extends OrderLayer implements Connector {
 
 	public final static boolean DEBUG = false;
 
 	/*
 	 * Constantes y variables para el socket multicast
 	 */
 	public final static int LOCALPORT = 50000;
 	public final static int PORTMAX = LOCALPORT + 100;
 
 	private ServerSocket listener;
 
 	private Hashtable<User, Socket> link;
 	private Hashtable<User, ObjectInputStream> hashOIS;
 	private Hashtable<User, ObjectOutputStream> hashOOS;
 
 	// Pool de threads
 	private Hashtable<User, Thread> threadPool;
 
 	/**
 	 * 
 	 * @param user
 	 * @throws Exception
 	 */
 	public SocketConnector(User user) {
 		super(user);
 
 		// Inicializamos link
 		link = new Hashtable<User, Socket>();
 		hashOIS = new Hashtable<User, ObjectInputStream>();
 		hashOOS = new Hashtable<User, ObjectOutputStream>();
 
 		// Inicializamos la threadPool
 		threadPool = new Hashtable<User, Thread>();
 
 		// Inicializamos los sockets.
 		inicializarSockets();
 	}
 
 	/*
 	 * Métodos del OrderLayer
 	 */
 
 	@Override
	public synchronized void sendMsg(LinkedList<User> users, Message msg) {
 		for (User user : users)
 			write(user, msg);
 	}
 
 	@Override
 	public synchronized void sendMsg(User user, Message msg) {
 		write(user, msg);
 	}
 
 	@Override
 	protected boolean okayToRecv(Message msg) {
 		return true;
 	}
 
 	@Override
 	public Type orderCapability() {
 		return null;
 	}
 
 	private void inicializarSockets() {
 		/*
 		 * Creamos un server Socket
 		 */
 		printDebug("Creando ServerSocket");
 
 		int port;
 		for (port = LOCALPORT; port < PORTMAX && listener == null; port++) {
 			try {
 				// Probamos a ver si el socket está disponible
 				listener = new ServerSocket(port);
 			} catch (IOException e) {
 				// Si se libera una excepción es que estaba ocupado
 				printDebug("Puerto [" + port + "] Ocupado");
 			}
 		}
 
 		if (listener != null) {
 			// Intentamos conseguir la IP del equipo local
 			try {
 				this.user.ip = InetAddress.getLocalHost().getHostAddress();
 				this.user.port = port - 1;
 			} catch (UnknownHostException e) {
 
 				// No se ha podido, luego soltamos una excepcion de tiempo de
 				// ejecución
 				String msg = "Su ordenación no tiene configurada una conexion de internet, y terminará";
 				System.out.println(msg);
 				System.exit(0);
 			}
 		} else {
 			String msg = "No hemos conseguido encontrar un puerto disponible para su equipo, y la aplicación terminará";
 			System.out.println(msg);
 			System.exit(0);
 		}
 	}
 
 	/**
 	 * Lee un objeto de un socket multicast
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	public void write(User user, Message message) {
 		try {
 			ObjectOutputStream oos = hashOOS.get(user);
 			oos.writeObject(message);
 			oos.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Registramos el usuario, creamos un socket con el
 	 * 
 	 * Este es el caso cuando UUID del usuario es menor que nuestro UUID, si no
 	 * se llama a acceptConnect()
 	 * 
 	 * @param user
 	 */
 	public synchronized void connect(User pUser) {
 		Socket socket = null;
 		ObjectInputStream ois = null;
 		ObjectOutputStream oos = null;
 
 		try {
 			// Creamos el socket
 			socket = new Socket(pUser.ip, pUser.port);
 
 			// Creamos los object streams
 			oos = new ObjectOutputStream(socket.getOutputStream());
 			ois = new ObjectInputStream(socket.getInputStream());
 
 			// Enviamos petición
 			oos.writeObject(user.userMessage(true));
 
 			link.put(pUser, socket);
 			hashOIS.put(pUser, ois);
 			hashOOS.put(pUser, oos);
 
 			// Creamos el ListenerThread para escuchar al socket
 			Thread thread = new SocketListenerThread(this);
 			thread.start();
 
 			// Añadimos el thread al thread Pool
 			threadPool.put(pUser, thread);
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Creamos un socket server y esperamos a que nos manden un usuario
 	 * 
 	 * Este es el caso cuando UUID del usuario es menor que nuestro UUID, si no
 	 * se llama a acceptConnect()
 	 */
 	public synchronized User acceptConnect() {
 		Socket socket = null;
 		ObjectInputStream ois = null;
 		ObjectOutputStream oos = null;
 		User user = null;
 		try {
 			// Creamos el socket
 			socket = listener.accept();
 
 			// Creamos los object streams
 			ois = new ObjectInputStream(socket.getInputStream());
 			oos = new ObjectOutputStream(socket.getOutputStream());
 
 			// Leemos el objeto Usuario que nos envia el cliente
 			UserMessage readUser = (UserMessage) ois.readObject();
 
 			if (readUser instanceof UserMessage) {
 				user = readUser.user();
 				this.receive(new Message(readUser, user));
 
 				link.put(user, socket);
 				hashOIS.put(user, ois);
 				hashOOS.put(user, oos);
 
 				// Creamos el ListenerThread para escuchar al socket
 				Thread thread = new SocketListenerThread(this);
 				thread.start();
 
 				// Añadimos el thread al thread Pool
 				threadPool.put(user, thread);
 			}
 
 		} catch (ClassNotFoundException e) {
 			System.out.println("Connector.java: Objeto no recibido");
 			e.printStackTrace();
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		return user;
 	}
 
 	/**
 	 * Lee un objeto de un socket multicast
 	 * 
 	 * @return
 	 * @throws IOException
 	 */
 	@Override
 	public Object read(User user) {
 		Object msg = null;
 
 		try {
 			// Leyendo objeto
 			msg = hashOIS.get(user).readObject();
 
 		} catch (IOException e) {
 			// El socket se ha cerrado
 			try {
 
 				// Mandar un mensaje a las capas de arriba indicando que el
 				// usuario se ha cerrado.
 				UserMessage msg2 = new UserMessage(user, false);
 				this.receive(new Message(msg2, user));
 
 				link.get(user).close();
 			} catch (IOException e1) {
 			}
 
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		return msg;
 	}
 
 	@Override
 	public void addUser(User user) {
 		super.addUser(user);
 
 		if (this.user.uuid.compareTo(user.uuid) < 0) {
 
 			connect(user);
 
 		} else {
 
 			acceptConnect();
 
 		}
 	}
 
 	@Override
 	public void removeUser(User user) {
 		super.removeUser(user);
 	}
 
 	@Override
 	public boolean isClosed(User user) {
 		return link.get(user).isClosed();
 	}
 
 	@Override
 	public void receive(Message msg) {
 		this.deliveryQueue.add(msg);
 		this.setChanged();
 		this.notifyObservers(this);
 	}
 
 	/**
 	 * Cerramos los sockets del cliente.
 	 */
 	public void close() {
 		printDebug("Cerrando Sockets");
 
 		// Cerramos los Socket con el resto de clientes.
 		Iterator<Entry<User, Socket>> iter = link.entrySet().iterator();
 
 		while (iter.hasNext()) {
 			// Por cada socket guardado intentamos cerrarlo (por si no está
 			// cerrado ya)
 
 			Entry<User, Socket> entry = iter.next();
 
 			printDebug("Cerrando socket de " + user + " a " + entry.getKey());
 
 			try {
 				entry.getValue().close();
 
 			} catch (Exception e) {
 				printDebug("Fallo cerrando el socket de " + user + " a "
 						+ entry.getKey());
 			}
 		}
 
 		printDebug("Cerrados todos los sockets abiertos");
 
 		Iterator<Entry<User, Thread>> iter2 = threadPool.entrySet().iterator();
 
 		while (iter2.hasNext()) {
 
 			Entry<User, Thread> entry = iter2.next();
 
 			printDebug("Esperando a parar ListenerThread de " + user + " a "
 					+ entry.getKey());
 			try {
 
 				entry.getValue().join(2000);
 
 			} catch (InterruptedException e) {
 				printDebug("Fallo al esperar a parar ListenerThread de " + user
 						+ " a " + entry.getKey());
 			}
 		}
 
 		printDebug("Parados todos los hilos de la thread pool");
 
 		try {
 
 			// Cerramos el ServerSocket
 			listener.close();
 
 		} catch (Exception e) {
 		}
 
 		printDebug("Cerrado ServerSocket");
 	}
 
 	/*
 	 * funciones de debug
 	 */
 
 	final static String msgClase = "Connector.java: ";
 
 	private void printDebug(String string) {
 
 		if (DEBUG)
 			System.out.println(msgClase + string);
 	}
 }
