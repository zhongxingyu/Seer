 package server;
 
 import java.net.InetAddress;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import common.logging.EventType;
 import common.logging.Log;
 
 /**
  * This class handles client timeouts, keeping track of the date at which each client's
  * message was received. Clients will be removed from the timeout table according to the
  * Server's timeout time.
  * @author etudiant
  * @see Server
  */
 public class ServerTimeoutHandler extends Thread {
 	
 	private Map<String, Date> timeoutTable;
 	private Server serveur;
 	private Log log;
 	
 	/**
 	 * Constructs a TimeoutHandler for the specified Server with an empty timeout table.
 	 * <br />Note: the thread must be started by the Server.
 	 * @param serveur
 	 */
 	public ServerTimeoutHandler(Server serveur) {
 		this.serveur = serveur;
 		timeoutTable = new HashMap<String, Date>();
 		log = serveur.getLog();
 	}
 	
 	public Map<String, Date> getTimeoutTable() {
 		return timeoutTable;
 	}
 
 	public void setTimeoutTable(Map<String, Date> timeoutTable) {
 		this.timeoutTable = timeoutTable;
 	}
 
 	public Server getServeur() {
 		return serveur;
 	}
 
 	/**
 	 * Synchronized method adding a client to the timeout table,
 	 * with its time stamp set to the current time.
 	 * @param ip - Client's IP address.
 	 */
 	public synchronized void addClient(String login) {
 		timeoutTable.put(login, new Date());
 	}
 	
 	/**
 	 * Synchronized method removing the specified client from the
 	 * timeout table.
 	 * @param login - Client's IP address.
 	 */
 	public synchronized void removeClient(String login) {
 		timeoutTable.remove(login);
 	}
 	
 	/**
 	 * Updates a client's time stamp.
 	 * @param login - Client's IP address.
 	 * @param time - When the last message from this client was received.
 	 */
 	public synchronized void updateClient(String login, Date time) {
 		if (timeoutTable.containsKey(login)) {
 			timeoutTable.put(login, time);
 		}
 	}
 
 	@Override
 	public void run() {
 		while (serveur.isRunning()) {
 			for (String login : timeoutTable.keySet()) {
 				Date now = new Date();
 				if ((now.getTime() - timeoutTable.get(login).getTime()) > serveur.getTimeoutTime()) {
 					//log.log(EventType.TIMEOUT, "Client timed out: "	+ timeoutTable.get(ip) + "(" + ip + ")");
 					removeClient(login);
 				}
 			}
 		}
 	}
 
 }
