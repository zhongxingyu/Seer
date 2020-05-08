 package clientServer;
 
 import dataLink.Protocol;
 import dataLink.ProtocolUDP;
 
 public class ThreadListenerUDP extends Thread
 {
 
 	private AbstractClientServer clientServer;
 	// Pourquoi 60000 ? Limite théorique = 65535, limite en IPv4 = 65507
	private int port;
 	// Boolean permettant de stopper le Thread
 	private boolean running;
 
 	/**
 	 * Constructeur de la classe ThreadListenerUDP. Il recoit le port d'écoute
 	 * en paramètre.
 	 * 
 	 * @param port
 	 */
 	public ThreadListenerUDP(int port)
 	{
 		this.port = port;
 	}
 
 	/**
 	 * Ce thread récéptionne les messages et les affiches dans la console.
 	 */
 	public void run()
 	{
 		this.running = true;
 		Protocol protocol = new ProtocolUDP(this.port);
 		try
 		{
 			while (this.running)
 			{
 				String message = protocol.readMessage();
 				this.clientServer.treatIncomeUDP(message);
 			}
 		} catch (Exception e)
 		{
 			System.err.println("Erreur du ThreadListenerUDP, message: " + e.getMessage());
 		}
 	}
 
	public int getPort()
 	{
		return this.port;
 	}
 
 }
