 package network;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.NoSuchElementException;
 import java.util.regex.PatternSyntaxException;
 
 import misc.Config;
 import misc.Print;
 
 /**
  * Hier werden einkommende verarbeitet. 
  * 
  */
 
 public class HandleConnections implements Runnable {
 
 	private Socket client;
 	private HTTP http;
 	private final String webroot = Config.getWebroot();
 	private final String errorPage = Config.getErrorPage();
 	public static final Object LOCK = new Object();
 
 	
 	/**
 	 * Thread welcher die eingehenden Verbindungen verarbeitet.
 	 */
 	@Override
 	public void run() {
 		Print.deb("NEUER THREAD: " + Thread.currentThread());
 		
 		Print.deb(Thread.currentThread()+" Neue Verbindung! IP: '" + client.getInetAddress()
 				+ "' Port: '" + client.getPort() + "'");
 		// Daten empfangen
 		String empfang = "";
 		try {
 			empfang = TCP.recv(client);
 		} catch (IOException e) {
 			Print.err(Thread.currentThread()+" Empfangen der Daten von " + client.getInetAddress()
 					+ " fehlgeschlagen!");
 			e.printStackTrace();
 			return;
 		} catch (NoSuchElementException e2) {
 			Print.deberr(Thread.currentThread()+" Empfangen der Daten von " + client.getInetAddress()
 					+ " fehlgeschlagen!");
 			return;
 		}
 
 		Print.deb(Thread.currentThread()+" Empfangene Daten: '" + empfang + "' von "
 				+ client.getInetAddress());
 
 		// Empfangene Daten prüfen und entsprechen reagieren
 		if (empfang == "") {
 			Print.deb(Thread.currentThread() + "Es wurde nichts gesendet!");
 			http.error(client, HTTP.SYNTAX_ERROR, Thread.currentThread());
 		}
 		String[] split = null;
 		try {
 			split = empfang.split(" ");
 			if (split[0].equalsIgnoreCase("GET")) { // GET Reqest
 				http.get(client, split, Thread.currentThread());
			} else if (split[1].equalsIgnoreCase("POST")) { // POST Request
 				// http.post(client, empfang);
 				Print.deb("POST REQEST!");
 			} else {
 				Print.deb(Thread.currentThread() + "Es wurde ein unbekannter HTTP-Request gesendet!");
 				http.error(client, HTTP.SYNTAX_ERROR, Thread.currentThread());
 			}
 		} catch (PatternSyntaxException e) {
 			Print.deb(Thread.currentThread() + "Es wurde kein HTTP-Request gesendet!");
 			http.error(client, HTTP.SYNTAX_ERROR, Thread.currentThread());
 		} 
 		
 		try {
 			client.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	
 	/**
 	 * Konstruktor der das Verarbeiten einer eingegangenen Verbindung initialiiert. 
 	 * @param client Socket mit dem die Verbindung aufgebaut wurde und der behandelt werden muss.
 	 * @throws SocketException Socket konnte nicht erreicht werden.
 	 */
 	public HandleConnections(Socket client) throws SocketException {
 		client.getKeepAlive();
 		this.client = client;
 
 		// HTTP implementieren
 		http = new HTTP(webroot, errorPage);
 	}
 
 }
