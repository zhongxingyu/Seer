 package model;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.ArrayList;
 import java.util.logging.FileHandler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.logging.SimpleFormatter;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import network.Protocol;
 import network.ProtocolUDP;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * Classe représentant un serveur. Hérite de la classe AbstractClientServer.
  * 
  * @author Dorian, Mickaël, Raphaël, Thibault
  * @see AbstractClientServer
  */
 public class Server extends AbstractClientServer
 {
 	/**
 	 * Protocol permettant au serveur de communiquer via le threadListernerUDP.
 	 * 
 	 * @see Protocol
 	 */
 	private Protocol protocol;
 	/**
 	 * Thread TCP permettant au serveur de recevoir les connexions TCP.
 	 * 
 	 * @see ThreadListenerTCP
 	 */
 	private ThreadListenerTCP threadListenerTCP;
 	/**
 	 * Thread UDP permettant au serveur de revevoir des paquets UDP.
 	 * 
 	 * @see ThreadListenerUDP
 	 */
 	private ThreadListenerUDP threadListenerUDP;
 	/**
 	 * Liste des TTL clients permettant de gerer la deconexion si un client ne
 	 * donne plus signe de vie.
 	 */
 	private HashMap<ClientServerData, Integer> clientTTL;
 	/**
 	 * Paramètre permettant de savoir si le serveur est en train de tourner.
 	 */
 	private boolean running;
 
 	/**
 	 * Constructeur par défaut de la classe Server. Initialise les variables
 	 * server,clients et threadListener ouvrant le port TCP 30970 et le port UDP
 	 * 30971.
 	 */
 	public Server()
 	{
 		super();
 		try
 		{
 			FileHandler file = new FileHandler("log.txt", true);
 			SimpleFormatter formatter = new SimpleFormatter();
 			file.setFormatter(formatter);
 			setLogger(Logger.getLogger(Server.class.toString()));
 			getLogger().setLevel(Level.FINEST);
 			getLogger().addHandler(file);
 		} catch (SecurityException | IOException e)
 		{
 			System.err.println("Erreur d'ouverture du fichier de log, message : " + e.getMessage());
 		}
 
 		this.protocol = new ProtocolUDP(30971);
 		this.threadListenerTCP = new ThreadListenerTCP(this, 30970);
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.clientTTL = new HashMap<ClientServerData, Integer>();
 	}
 
 	/**
 	 * Constructeur qui prend 1 paramètre. Initialise les variables
 	 * server,clients et threadListener.
 	 * 
 	 * @param port
 	 *            numéro de port TCP, le port UDP est incrémenté de 1
 	 */
 	public Server(int port)
 	{
 		super();
 		try
 		{
 			FileHandler file = new FileHandler("log.txt", true);
 			SimpleFormatter formatter = new SimpleFormatter();
 			file.setFormatter(formatter);
 			setLogger(Logger.getLogger(Server.class.toString()));
 			getLogger().setLevel(Level.FINE);
 			getLogger().addHandler(file);
 		} catch (SecurityException | IOException e)
 		{
 			System.err.println("Erreur d'ouverture du fichier de log, message : " + e.getMessage());
 		}
 
 		this.protocol = new ProtocolUDP(port + 1);
 		this.threadListenerTCP = new ThreadListenerTCP(this, port);
 		this.threadListenerUDP = new ThreadListenerUDP(this, this.protocol);
 		this.clientTTL = new HashMap<ClientServerData, Integer>();
 	}
 
 	/**
 	 * Méthode permettant de lancer le serveur.
 	 */
 	public void launch()
 	{
 		getLogger().info("Lancement du serveur");
 		this.running = true;
 		this.threadListenerTCP.start();
 		this.threadListenerUDP.start();
 		// Thread de mise à jour des TTL Clients
 		new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				getLogger().info("Lancement du thread TLL Client");
 				while (running)
 				{
 					try
 					{
 						Thread.sleep(1000);
 						ArrayList<ClientServerData> copyClients = new ArrayList<ClientServerData>(getClients());
 						for (ClientServerData client : copyClients)
 						{
 							int ttl = clientTTL.get(client);
 							clientTTL.put(client, ttl - 1);
 							if ((ttl - 1) < 0)
 							{
 								if (removeClient(client))
 								{
 									clientTTL.remove(client);
 								}
 							}
 						}
 					} catch (InterruptedException e)
 					{
 
 					}
 				}
 			}
 		}).start();
 	}
 
 	/**
 	 * Méthode permettant de stopper le serveur.
 	 */
 	public void stopServer()
 	{
 		// System.out.println("Fermeture du serveur");
 		this.running = false;
 		this.threadListenerTCP.stopThread();
 		this.threadListenerUDP.stopThread();
 		getLogger().info("Fermeture du serveur");
 	}
 
 	/**
 	 * Methode permettant de verifier les identifiants de connexion d'un client.
 	 * 
 	 * @param id
 	 *            identifiant du client
 	 * @param password
 	 *            mot de passe du client
 	 * @return true si le couple log/mdp est correct, sinon false
 	 */
 	protected boolean verifyIDAndPassword(String id, String password)
 	{
 		boolean ret = false;
 		try
 		{
 			File fXmlFile = new File("server.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder;
 			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = null;
			try
			{
				doc = dBuilder.parse(fXmlFile);
			} catch (SAXException | IOException e)
			{
				doc = dBuilder.newDocument();
				Element rootElement = doc.createElement("clients");
				doc.appendChild(rootElement);
			}
 			doc.getDocumentElement().normalize();
 			NodeList clientsList = doc.getElementsByTagName("clients");
 			Node clients = clientsList.item(0);
 			for (int temp = 0; temp < clients.getChildNodes().getLength(); temp++)
 			{
 				Node clientNode = clients.getChildNodes().item(temp);
 				// System.out.println("\nCurrent Element :" +
 				// clientNode.getNodeName());
 				if (clientNode.getNodeType() == Node.ELEMENT_NODE)
 				{
 					Element client = (Element) clientNode;
 					String idClient = client.getElementsByTagName("name").item(0).getTextContent();
 					String passwordClient = client.getElementsByTagName("password").item(0).getTextContent();
 					if (id.equals(idClient) && password.equals(passwordClient))
 					{
 						ret = true;
 					}
 
 				}
 			}
		} catch (ParserConfigurationException e)
 		{
 			// System.out.println("Erreur de vérifiaction id/mdp, message : " +
 			// e.getMessage());
 			getLogger().severe("Erreur de vérifiaction id/mdp sur fichier");
 		}
 		return ret;
 	}
 
 	/**
 	 * Methode permettant d'ajouter des identifiants/motDePasse dans la base du
 	 * serveur
 	 * 
 	 * @param id
 	 *            identifiant du client
 	 * @param password
 	 *            mot de passe du client
 	 */
 	protected void registerClientInBase(String id, String password)
 	{
 		try
 		{
 			getLogger().info("Ajout identifiant/mdp dans la base serveur : " + id + "/" + password);
 			File fXmlFile = new File("server.xml");
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = null;
			try
			{
				doc = dBuilder.parse(fXmlFile);
			} catch (SAXException | IOException e)
			{
				doc = dBuilder.newDocument();
				Element rootElement = doc.createElement("clients");
				doc.appendChild(rootElement);
			}
 
 			Element clientElement = doc.createElement("client");
 			Element nameClient = doc.createElement("name");
 			nameClient.setTextContent(id);
 			clientElement.appendChild(nameClient);
 
 			Element passwordClient = doc.createElement("password");
 			passwordClient.setTextContent(password);
 			clientElement.appendChild(passwordClient);
 
 			doc.getFirstChild().appendChild(clientElement);
 
 			TransformerFactory transformerFactory = TransformerFactory.newInstance();
 			Transformer transformer = transformerFactory.newTransformer();
 			DOMSource source = new DOMSource(doc);
 			StreamResult result = new StreamResult(fXmlFile);
 
 			transformer.transform(source, result);
 
		} catch (ParserConfigurationException | TransformerException e)
 		{
 			// System.out.println("Erreur de vérifiaction id/mdp, message : " +
 			// e.getMessage());
 			getLogger().severe("Erreur d'enregistrement id/mdp sur fichier");
 		}
 	}
 
 	/**
 	 * Méthode pour ajouter un client
 	 * 
 	 * @param name
 	 *            Nom du client
 	 * @param client
 	 *            Socket du client
 	 * @param listeningUDPPort
 	 *            port UDP sur lequel le client écoute
 	 * @return une string de la clé publique du client.
 	 */
 	public String addClient(String name, Socket client, int listeningUDPPort)
 	{
 		getLogger().info("Ajout d'un client : " + name);
 		ClientServerData newClient = new ClientServerData(name, client.getInetAddress(), listeningUDPPort);
 		if (this.getClients().add(newClient))
 		{
 			new Thread(new Runnable()
 			{
 
 				@Override
 				public void run()
 				{
 					try
 					{
 						Thread.sleep(3000);
 					} catch (InterruptedException e)
 					{
 						// e.printStackTrace();
 					}
 					for (ClientServerData clientServerData : getClients())
 					{
 						sendListClient(clientServerData);
 					}
 				}
 			}).start();
 			this.clientTTL.put(newClient, 10);
 			return newClient.getId();
 		} else
 		{
 			return null;
 		}
 	}
 
 	/**
 	 * Méthode pour supprimer un client via ses informations.
 	 * 
 	 * @param client
 	 *            {@link ClientServerData}
 	 * @return true si réussi, false sinon
 	 */
 	public boolean removeClient(ClientServerData client)
 	{
 		getLogger().info("Suppression d'un client : " + client.getName());
 		boolean ret = this.getClients().remove(client);
 		for (ClientServerData clientServerData : this.getClients())
 		{
 			this.sendListClient(clientServerData);
 		}
 		return ret;
 	}
 
 	/**
 	 * Méthode pour supprimer un client via son ID
 	 * 
 	 * @param id
 	 *            Clé publique du client
 	 * @return true si réussi, false sinon
 	 */
 	public boolean removeClient(String id)
 	{
 		getLogger().info("Suppression d'un client(id) : " + id);
 		boolean erase = false;
 		ClientServerData eraseClient = null;
 		for (ClientServerData client : this.getClients())
 		{
 			if (client.getId().equals(id))
 			{
 				eraseClient = client;
 				erase = true;
 			}
 		}
 		if (erase)
 		{
 			boolean ret = this.getClients().remove(eraseClient);
 			for (ClientServerData clientServerData : this.getClients())
 			{
 				this.sendListClient(clientServerData);
 			}
 			return ret;
 		}
 		return false;
 	}
 
 	/**
 	 * Méthode pour supprimer un client via son adresse IP
 	 * 
 	 * @param ip
 	 *            Ip du client {@link InetAddress}
 	 * @return true si reussi, false sinon
 	 */
 	public boolean removeClient(InetAddress ip)
 	{
 		getLogger().info("Suppression d'un/des clients (Ip) : " + ip);
 		boolean erase = false;
 		for (ClientServerData client : this.getClients())
 		{
 			if (client.getIp().equals(ip))
 			{
 				this.getClients().remove(client);
 				erase = true;
 			}
 		}
 		for (ClientServerData clientServerData : this.getClients())
 		{
 			this.sendListClient(clientServerData);
 		}
 		return erase;
 	}
 
 	/**
 	 * Getter permettant de recupérer la liste des clients que connait le
 	 * serveur. Cette méthode est utilisée pour envoyer une liste actualisée des
 	 * clients connectés.
 	 * 
 	 * @return chaine, client sous la forme
 	 *         "ClePublic-NomCLient,ClePublic-NomClient...."
 	 */
 	public String getListClient()
 	{
 		getLogger().log(Level.FINER, "Construction de list Client");
 		String ret = "";
 		boolean firstOne = true;
 		for (ClientServerData client : this.getClients())
 		{
 			ret += ((firstOne) ? "" : ",") + client.getId() + "-" + client.getName() + "-" + client.getPersonalMessage();
 			firstOne = false;
 		}
 		return ret;
 	}
 
 	/**
 	 * Méthode permettant d'envoyer la liste des clients connectés.
 	 * 
 	 * @param client
 	 *            auquel on envoie la liste
 	 */
 	public void sendListClient(ClientServerData client)
 	{
 		String listClient = this.getListClient();
 		getLogger().log(Level.FINER, "Envoie de liste client");
 		protocol.sendMessage("listClient:" + listClient, client.getIp(), client.getPort());
 	}
 
 	/**
 	 * Méthode permettant de recupérer les informations d'un client.
 	 * 
 	 * @param id
 	 *            clé publique du client
 	 * @return chaine, sous la forme
 	 *         "ClePublic,NomClient,IpClient,PortEcouteClient"
 	 */
 	public String getClient(String id)
 	{
 		getLogger().log(Level.FINER, "Construction Information Client");
 		for (ClientServerData client : this.getClients())
 		{
 
 			if (client.getId().equals(id))
 			{
 				return client.getId() + "," + client.getName() + "," + client.getIp().getCanonicalHostName() + "," + client.getPort();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Getter du thread d'écoute TCP
 	 * 
 	 * @return le thread d'écoute TPC
 	 */
 	public ThreadListenerTCP getThreadListener()
 	{
 		return threadListenerTCP;
 	}
 
 	/**
 	 * Setter qui fixe le thread d'écoute TCP
 	 * 
 	 * @param threadListener
 	 *            le thread d'écoute TCP
 	 */
 	public void setThreadListener(ThreadListenerTCP threadListener)
 	{
 		this.threadListenerTCP = threadListener;
 	}
 
 	/**
 	 * Méthode permettant de traiter les éléments reçu en TCP
 	 * 
 	 * @param object
 	 *            paquet reçu en TCP {@link AbstractClientServer}
 	 */
 	@Override
 	public void treatIncomeTCP(Object object)
 	{
 		if (object instanceof Socket)
 		{
 			getLogger().log(Level.FINEST, "Traitement Income TCP");
 			ThreadComunicationServer threadClientCom = new ThreadComunicationServer(this, (Socket) object);
 			threadClientCom.start();
 		} else
 		{
 			// System.err.println("Erreur serveur, treatIncome: mauvaise argument");
 			getLogger().severe("Erreur serveur, treatIncome: mauvaise argument");
 		}
 	}
 
 	/**
 	 * Méthode permettant de traiter les éléments reçu en UDP
 	 * 
 	 * @param message
 	 *            paquet reçu en UDP {@link AbstractClientServer}
 	 */
 	@Override
 	public void treatIncomeUDP(String message)
 	{
 		getLogger().log(Level.FINEST, "Traitement Income UDP");
 		StringTokenizer token = new StringTokenizer(message, ":");
 		String firstToken = token.nextToken();
 		switch (firstToken)
 		{
 		case "alive":
 			if (token.hasMoreElements())
 			{
 				String[] elements = token.nextToken().split("-");
 				ClientServerData client = null;
 				String key = elements[0];
 				String personalMessage = "";
 				if (elements.length > 1)
 				{
 					personalMessage = elements[1];
 				}
 				for (ClientServerData clientD : this.getClients())
 				{
 					if (key.trim().equals(clientD.getId().trim()))
 					{
 						client = clientD;
 					}
 				}
 				if (client != null)
 				{
 					if (!client.getPersonalMessage().equals(personalMessage))
 					{
 						client.setPersonalMessage(personalMessage);
 						for (ClientServerData clientServerData : this.getClients())
 						{
 							this.sendListClient(clientServerData);
 						}
 					}
 					clientTTL.put(client, 10);
 				}
 			}
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	/**
 	 * Main du programme permet de lancer le serveur
 	 */
 	public static void main(String[] args)
 	{
 		System.out.println("EXIT to quit");
 		System.out.println("ADD LOGIN to Add log/mdp");
 		Server server = new Server();
 		server.launch();
 		Scanner sc = new Scanner(System.in);
 		boolean running = true;
 		while (running)
 		{
 			switch (sc.nextLine())
 			{
 			case "EXIT":
 				server.stopServer();
 				running = false;
 				break;
 			case "ADD LOGIN":
 				System.out.println("login :");
 				String login = sc.nextLine();
 				System.out.println("Password :");
 				String pass = sc.nextLine();
 				server.registerClientInBase(login, pass);
 				System.out.println("OK");
 				break;
 			default:
 				break;
 			}
 		}
 		sc.close();
 	}
 }
