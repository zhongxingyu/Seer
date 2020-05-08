 package lo52.messaging.services;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import lo52.messaging.model.Conversation;
 import lo52.messaging.model.Localisation;
 import lo52.messaging.model.Message;
 import lo52.messaging.model.User;
 import lo52.messaging.model.broadcast.MessageBroacast;
 import lo52.messaging.model.network.ContentNetwork;
 import lo52.messaging.model.network.PacketNetwork;
 import lo52.messaging.util.LibUtil;
 import lo52.messaging.util.Network;
 import android.app.Service;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 
 import com.google.gson.Gson;
 
 /**
  * Service qui gère les communcations réseau, stocke les différentes données,
  * reçoit des actions depuis les activity, leur ennvoit des messages/création de groupe,
  * met à disposition la liste des utilisateurs, conversations.
  * 
  * Principe de base:
  * 
  * *L'activity envoit:
  * 
  * Hello
  * Creation group
  * Message
  * Alive
  * Disconnected
  * 
  * *Le service envoit:
  * 
  * Message d'une conversation
  * 
  * *L'activity récupère:
  * 
  * La liste des utilisateurs
  * La listes des conversations
  * 
  * 
  * 
  * @author SYSTEMMOI
  *
  */
 public class NetworkService extends Service {
 
 	// Contient la liste des utilisateur
 	private static Hashtable<Integer,User> listUsers = new Hashtable<Integer,User>();
 
 	// contient la liste des conversations
 	private static Hashtable<Integer,Conversation> listConversations = new Hashtable<Integer,Conversation>();
 
 	//liste des packets en attente d'ACK, l'integer est le temps auquel on envoyé le paquet (en millisecondes)
 	private Hashtable<Integer,PacketNetwork> packetListACK = new Hashtable<Integer,PacketNetwork>();
 
 	private ArrayList<Integer> previousReceivedPacket = new ArrayList<Integer>();
 
 	//liste des liste de paquets
 	private Hashtable<Integer,ArrayList<PacketNetwork>> listPaquetDivided = new Hashtable<Integer,ArrayList<PacketNetwork>>();
 
 	// Liste des IDs des conversations qui ont été créées par le service mais qui n'ont pas encore de fragment associés dans l'UI
 	private static ArrayList<Conversation> conversationsToCreateUI = new ArrayList<Conversation>();
 
 	private static User user_me;
 
 
 	// Socket d'écoute
 	ListenSocket listenSocket = null;
 
 	private static final String TAG = "NetworkService";
 
 
 	public static final String ReceivePacket 		= "NetworkService.receive.Packet";
 	public static final String ReceiveMessage 		= "NetworkService.receive.Message";
 	public static final String ReceiveConversation 	= "NetworkService.receive.Conversation";
 	public static final String SendMessage 			= "NetworkService.send.Message";
 	public static final String SendConversation 	= "NetworkService.send.Conversation";
 	public static final String Receivelocalisation 	= "NetworkService.receive.Localisation";
 	public static final String UserListUpdated		= "NetworkService.userlist.updated";
 	public static final String FileTransferStart	= "NetworkService.file.Receive.Start";
 	public static final String FileTransferFinish	= "NetworkService.file.Receive.Finish";
 
 
 	private int PORT_DEST = 5008;
 	private int PORT_LOCAL = 5008;
 
 	//Taille du buffer en réception, en Byte
	//TODO : avant production de l'application final mettre à 30 000
 	public static final int BUFFER_SIZE = 15000;
 
 	public NetworkService() {
 
 	}
 
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return null;
 	}
 
 	@Override
 	public void onCreate()
 	{
 
 		super.onCreate();
 
 		/*
 		 *On récupère les prots définits dans les préférences
 		 */
 		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean isDev = preferences.getBoolean("dev_prefs_emulateur", false);
 
 		if (isDev) {
 			PORT_DEST = Integer.valueOf (preferences.getString("dev_prefs_port_distant", "5008"));
 			PORT_LOCAL = Integer.valueOf (preferences.getString("dev_prefs_port_entrant", "5008"));
 		}
 
 
 		/*
 		 * enregistrer l'intent permettant de recevoir les messages
 		 */
 		IntentFilter filter = new IntentFilter();
 		filter.addAction(ReceivePacket);
 		registerReceiver(SendPacket, filter);
 
 		/*
 		 * enregistrer l'intent permettant de recevoir les messages depuis un intent
 		 */
 		IntentFilter filter2 = new IntentFilter();
 		filter2.addAction(ReceiveMessage);
 		registerReceiver(Message, filter2);
 
 		/*
 		 * enregistrer l'intent permettant de recevoir les conversation depuis un intent
 		 */
 		IntentFilter filter3 = new IntentFilter();
 		filter3.addAction(ReceiveConversation);
 		registerReceiver(Conversation, filter3);
 
 		/*
 		 * enregistrer l'intent permettant de recevoir les infos de localisation depuis un intent
 		 */
 		IntentFilter filter4 = new IntentFilter();
 		filter4.addAction(Receivelocalisation);
 		registerReceiver(LocalisationUser, filter4);
 
 
 		/*
 		 * création de l'utilisateur actuel
 		 */
 
 		String user_name = preferences.getString("prefs_userName", "default");
 
 		/*
 		 * on récupère l'id de l'utilisateur sinon on le créé
 		 */
 		int user_id = preferences.getInt("gen_userId", 0);
 
 		if (user_id == 0) {
 			user_me = new User(user_name);
 			Editor prefEditor =  preferences.edit();
 			prefEditor.putInt("gen_userId", user_me.getId());
 			prefEditor.commit();
 		} else {
 			user_me = new User(user_name);
 			user_me.setId(user_id);
 		}
 
 
 
 		InetAddress addres = null;
 		try {
 			addres = Network.getWifiAddress(getApplicationContext());
 		} catch (IOException e) {
 			Log.e(TAG, "not possible to get wifi addresse");
 			e.printStackTrace();
 		}
 
 
 
 		/**
 		 * TODO : si le wifi est déconnecté reconnecté: intercepter les changement avec un broadcastreceiver
 		 * http://stackoverflow.com/questions/5165099/android-how-to-handle-change-in-network-from-gprs-to-wi-fi-and-vice-versa-whi
 		 */
 
 		InetSocketAddress inetAddres = new InetSocketAddress(addres, PORT_LOCAL);
 
 		user_me.setInetSocketAddressLocal(inetAddres);
 
 
 
 		/*
 		 * on lance la socket d'écoute sur le réseau 
 		 */
 
 		listenSocket = new ListenSocket();
 		listenSocket.execute((Integer)null);
 
 		/*
 		 * On s'annonce sur le réseau, utilisation d'un timer pour attendre que tout le reste soit en place
 		 */
 		Timer timer = new Timer();
 		timer.schedule(new SendBroadcatsimeTask(), 200);
 
 		Timer timer2 = new Timer();
 		timer2.schedule(new checkACKTask(), 20000);
 
 	}
 
 	/**
 	 * Permet d'envoyer un broadcast Hello
 	 */
 	class SendBroadcatsimeTask extends TimerTask {
 
 		@Override
 		public void run() {
 			sendBroadcastHelloNetwork();
 		}
 	}
 
 
 
 	private void sendBroadcastHelloNetwork() {
 
 		PacketNetwork packet = new PacketNetwork(PacketNetwork.HELLO);
 		packet.setUser_envoyeur(user_me);		
 		SendPacket(packet);
 
 	}
 
 	/*
 	 * Recoit un autre type de packet
 	 */
 	private BroadcastReceiver SendPacket = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String json = intent.getStringExtra("json");
 
 			Gson gson = new Gson();
 			PacketNetwork packet = gson.fromJson(json, PacketNetwork.class);
 
 			packet.setUser_envoyeur(user_me);
 
 
 			SendPacket(packet);
 		}
 	};
 
 	/*
 	 * Recoit un message à envoyer à un client depuis une activity
 	 */
 	private BroadcastReceiver Message = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle bundle = intent.getBundleExtra("message");
 			MessageBroacast message = bundle.getParcelable(MessageBroacast.tag_parcelable);
 
 			Log.d(TAG, "message à envoyer depuis client " + message.getClient_id() );
 			// on ajoute le message à la liste
 
 			Conversation conversation = listConversations.get(message.getConversation_id());
 			ArrayList<Integer> listIdUser = conversation.getListIdUser();
 
 			ContentNetwork content = new ContentNetwork(message.getConversation_id(), message.getMessage(), message.getClient_id());
 
 			if ( message.getLink_file() != null ) {
 				File file = new File(message.getLink_file());
 				content.setByte_content(LibUtil.getByte(file));
 				content.setFile_name(file.getName());
 			}
 
 			for(int id_user : listIdUser) {
 				//ne pas s'envoyer à soit même le message
 				if (id_user != user_me.getId()) {
 					User user_destinataire = listUsers.get(id_user);
 
 					PacketNetwork packet = new PacketNetwork(content, user_destinataire, PacketNetwork.MESSAGE);
 
 					packet.setUser_envoyeur(user_me);
 
 					SendPacket(packet);
 				}
 
 			}
 		}
 	};
 
 	/*
 	 * Recoit un Conversation à créer depuis une activity
 	 */
 	private BroadcastReceiver Conversation = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle bundle = intent.getBundleExtra("conversation");
 			Conversation conversation = bundle.getParcelable("conversation");
 
 			// on ajoute la conversation à la liste
 			listConversations.put(conversation.getConversation_id(), conversation);
 			Log.w(TAG, "AJOUT CONVERSATION LISTE GLOBALE, new size " + listConversations.size());
 
 			sendConversation(conversation);
 
 		}
 	};
 
 	private void sendConversation(Conversation conversation) {
 
 		ArrayList<User> users = new ArrayList<User>();
 		for(int id_user : conversation.getListIdUser()) {
 			users.add(listUsers.get(id_user));
 		}
 		// On recherche si user_me n'est pas dans la liste des users, sinon on l'ajoute
 		boolean user_me_found = false;
 
 		for (User u : users) {
 			if (u != null && u.getId() == user_me.getId()) user_me_found = true;
 		}
 		if (!user_me_found) users.add(user_me);
 
 		ContentNetwork content = new ContentNetwork(conversation.getConversation_id(), conversation.getConversation_name(), users);
 
 		for(User user_destinataire : users) {
 
 			// On évite de s'envoyer le paquet de création à soi même...
 			if (user_destinataire != null && user_destinataire.getId() != user_me.getId()) {
 
 				PacketNetwork packet = new PacketNetwork(content, user_destinataire, PacketNetwork.CREATION_GROUP);
 
 				packet.setUser_envoyeur(user_me);
 
 				SendPacket(packet);
 			}
 		}
 	}
 
 	/*
 	 * Recoit  les infos de localisation à enovoyer à tous les clients
 	 */
 	private BroadcastReceiver LocalisationUser = new BroadcastReceiver() {
 
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			Bundle bundle = intent.getBundleExtra("localisation");
 			Localisation loca_user = bundle.getParcelable("localisation");
 
 			user_me.setLocalisation(loca_user);
 
 			ContentNetwork content = new ContentNetwork(loca_user.getLat(),loca_user.getLon(),user_me.getId());
 
 			for(User user_destinataire : getListUsers().values()) {
 
 				PacketNetwork packet = new PacketNetwork(content, user_destinataire, PacketNetwork.LOCALISATION);
 
 				packet.setUser_envoyeur(user_me);
 
 				SendPacket(packet);
 
 			}
 		}
 	};
 
 
 
 	/**
 	 * Fonction qui finit la contruction du packet et l'envoit suivant son type
 	 * @param packet
 	 */
 	private void SendPacket(PacketNetwork packet) {
 
 		packet.setNext_packet(0);
 		packet.setPrevious_packet(0);
 
 		/**
 		 * Dans le cas de l'annonciation de l'arrivée dans le réseau (broadcast)
 		 */
 		if (packet.type == PacketNetwork.HELLO && packet.getUser_destinataire() == null) {
 			//Création de l'asyncTask pour envoyer le packet
 			BroadcastSocket broadcastSocket = new BroadcastSocket();
 			PacketNetwork[] packets = new PacketNetwork[1];
 			packets[0] = packet;
 
 			//Exécution de l'asyncTask
 			broadcastSocket.execute(packets);
 
 		} else {
 			/**
 			 * Dans le cas de l'envoit d'un message
 			 */
 			//Création de l'asyncTask pour envoyer le packet
 			SendSocket sendSocket = new SendSocket();
 			PacketNetwork[] packets = new PacketNetwork[1];
 			packets[0] = packet;
 
 			//Exécution de l'asyncTask
 			sendSocket.execute(packets);
 		}
 
 	}
 
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId)
 	{
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	@Override
 	public void onDestroy()
 	{
 		Log.i(TAG, "====================");
 		Log.i(TAG, "onDestroy service");
 		Log.i(TAG, "====================");
 		
 		prepareForClosure();
 
 		// Arrêter tous les broadcast receivers
 		unregisterReceiver(Conversation);
 		unregisterReceiver(Message);
 		unregisterReceiver(SendPacket);
 		unregisterReceiver(LocalisationUser);
 
 		super.onDestroy();
 	}
 
 
 	/**
 	 * Fait du nettoyage avant la fermeture du service
 	 */
 	private void prepareForClosure() {
 		// Arrêter l'AsyncTask  "socket"
 		listenSocket.cancel(true);
 		listenSocket = null;
 
 		listUsers.clear();
 		listConversations.clear();
 		conversationsToCreateUI.clear();
 		listUsers = null;
 		listConversations = null;
 		conversationsToCreateUI = null;
 		user_me = null;
 	}
 
 
 	/**
 	 * AsyncTask pour envoyer un message à un utilisateur.
 	 * Socket qui reçoit un paquet déjà tout emballé
 	 * Dans ce paquet il prend le client, regarde dans table son adresse ip et lui envoit
 	 */
 	private class SendSocket extends AsyncTask <PacketNetwork, Integer, Long> {
 
 		protected Long doInBackground(PacketNetwork... packets) {
 
 			PacketNetwork packet = packets[0];
 
 			/*
 			 * Vérification du paquet
 			 */
 
 			if (packet.getUser_destinataire() == null || packet.getUser_envoyeur() == null ) {
 				Log.e(TAG, "Error about user_dest or user_env inside packet");
 				Log.e(TAG,"type:" + packet.type);
 				return null ;
 			}
 
 			InetSocketAddress inetAddres = packet.getUser_destinataire().getInetSocketAddressLocal();
 
 			// au cas où la local addrese est nulle on utilise celle publique
 			if (inetAddres == null) {
 				inetAddres = packet.getUser_destinataire().getInetSocketAddressPublic();
 			}
 
 			if (inetAddres == null) {
 				Log.e(TAG, "Error, user sans addrese" + packet.toString());
 				return null;
 			}
 
 
 			/*TODO remplacer l'User du packet par soi-même */
 
 			DatagramSocket datagramSocket = null;
 			try {
 				datagramSocket = new DatagramSocket();
 			} catch (SocketException e1) {
 				e1.printStackTrace();
 			}
 
 			Gson gson = new Gson();
 
 			if (packet.getContent() != null && packet.getContent().getByte_content() != null && packet.getContent().getByte_content().length >= (BUFFER_SIZE)/2 - 2000) {
 				Log.d(TAG, "Taille total du content:" + packet.getContent().getByte_content().length);
 
 
 				ArrayList<PacketNetwork> listPacket = PacketNetwork.division(packet);
 
 
 				// Envoi d'un broadcast pour signier qu'on commence le transfert
 				Intent broadcastIntent = new Intent(NetworkService.FileTransferStart);
 				broadcastIntent.putExtra("conversation_id", packet.getContent().getConversation_id());
 				broadcastIntent.putExtra("isReceiver", false);	// Pour signifier qu'on n'est pas la personne qui reçoit le fichier
 				packet.getContent().getConversation_id();
 				sendBroadcast(broadcastIntent);
 
 				//on boucle sur la liste des paquets pour l'envoyer
 				for(PacketNetwork packet1 : listPacket) {
 					Log.d(TAG, "Taille après découpe du content:" + packet1.getContent().getByte_content().length);
 					String json1 = gson.toJson(packet1);
 
 					byte[] packet_byte1 = null;
 					try {
 						packet_byte1 = LibUtil.compress(json1);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					if (sendFinalPacket(packet_byte1, inetAddres, datagramSocket)) {
 						//on met une pause qui représente le temps de réception/décodage de l'autre device
 
 						try {
 							Thread.sleep(200);         
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 
 						Log.d(TAG, "envoyé:" + json1 + "a : " + inetAddres.toString());
 						packet1.setDate_send((int) System.currentTimeMillis());
 						packetListACK.put(packet1.getRamdom_identifiant(), packet1);
 					} else {
 						Log.e(TAG, "échec envoit datagramsocket");
 					}
 				}
 
 				// Intent pour signaler que le transfert est terminé
 				Intent broadcastIntent2 = new Intent(NetworkService.FileTransferFinish);
 				broadcastIntent2.putExtra("conversation_id", packet.getContent().getConversation_id());
 				packet.getContent().getConversation_id();
 				sendBroadcast(broadcastIntent2);
 
 				// sinon on envoit le packet seul
 			} else {
 
 				String json = gson.toJson(packet);
 
 				byte[] packet_byte = null;
 				try {
 					packet_byte = LibUtil.compress(json);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 				if (sendFinalPacket(packet_byte, inetAddres, datagramSocket)) {
 					Log.d(TAG, "envoyé:" + json + "a : " + inetAddres.toString());
 					packet.setDate_send((int) System.currentTimeMillis());
 
 					// si ce n'est pas un ACK on le mets dans la liste des paquets en attente d'ACK
 					if (packet.type != PacketNetwork.ACK) {
 						packetListACK.put(packet.getRamdom_identifiant(), packet);
 					}
 				} else {
 					Log.e(TAG, "échec envoit datagramsocket");
 				}
 			}
 			return null;
 
 		}
 
 		protected void onPostExecute(Long result) {
 		}
 	}
 
 	private boolean sendFinalPacket(byte[] packet_byte, InetSocketAddress inetAddres,DatagramSocket datagramSocket ) {
 
 		DatagramPacket dataPacket = null;
 		try {
 			Log.d(TAG, "Taille total du packet:" + packet_byte.length);
 			dataPacket = new DatagramPacket(packet_byte, packet_byte.length, inetAddres);
 			datagramSocket.send(dataPacket);
 			return true;
 
 
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 
 		}
 
 
 	}
 
 
 	/*
 	 * AsyncTask pour envoyer un message broadcast à tout le réseau
 	 * Socket qui reçoit un paquet déjà tout emballé
 	 * TODO a faire, et tester si le wifi dispo, sinon à ne pas faire
 	 */
 	private class BroadcastSocket extends AsyncTask <PacketNetwork, Integer, Long> {
 		protected Long doInBackground(PacketNetwork... packets) {
 
 			PacketNetwork packet = packets[0];
 
 			/*
 			 * on set par défault que c'est un paque isolé
 			 */
 
 			packet.setNext_packet(0);
 			packet.setPrevious_packet(0);
 
 			/*
 			 * Vérification du paquet
 			 */
 
 			if (packet.getUser_envoyeur() == null ) {
 				Log.e(TAG, "Error about user_env inside packet");
 				Log.e(TAG,packet.toString());
 			}
 
 			InetAddress addres = null;
 			try {
 				addres = Network.getBroadcastAddress(getApplicationContext());
 			} catch (IOException e2) {
 				Log.e(TAG, "Echec de la construction de l'adresse de broadcast");
 				e2.printStackTrace();
 				return (long) 0;
 			}
 
 			InetSocketAddress inetAddres = new InetSocketAddress(addres, PORT_DEST);
 
 			DatagramSocket datagramSocket = null;
 			try {
 				datagramSocket = new DatagramSocket();
 			} catch (SocketException e1) {
 				e1.printStackTrace();
 			}
 
 			Gson gson = new Gson();
 			String json = gson.toJson(packet);
 
 			byte[] buffer = null;
 			try {
 				buffer = LibUtil.compress(json);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			DatagramPacket dataPacket = null;
 			try {
 				dataPacket = new DatagramPacket(buffer, buffer.length, inetAddres);
 				datagramSocket.send(dataPacket);
 				Log.d(TAG, "envoyé:" + json + "a : " + inetAddres.toString());
 				Log.d(TAG, "Taille total du packet:" + buffer.length + "taille du contenu sans les en têtes " + gson.toJson(packet.getContent()).getBytes("UTF-8").length);
 
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 
 			return null;
 		}
 
 		protected void onPostExecute(Long result) {
 		}
 	}
 
 	/**
 	 * AsyncTask pour écouter la socket sur le port local
 	 * 
 	 * Integer non utiliser pour le moment
 	 *
 	 */
 	private class ListenSocket extends AsyncTask <Integer, Integer, Long> {
 		protected Long doInBackground(Integer... integers) {
 
 			/*
 			 *  initier et écouter sur la socket qui bind le port local et récupérer les données*/
 			DatagramSocket datagramSocket;
 			try {
 				datagramSocket = new DatagramSocket(PORT_LOCAL);
 				Log.d(TAG, "socket d'écoute sur " + datagramSocket.getLocalPort());
 
 
 				do{
 					byte[] buffer2 = new byte[BUFFER_SIZE]; //TODO vérifier à l'envoit que la taille du packet n'excède pas la taille du buffer
 					DatagramPacket dataPacket = new DatagramPacket(buffer2, buffer2.length);
 					try {
 						datagramSocket.receive(dataPacket);
 
 						analysePacket(dataPacket);
 
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 				}while(true);
 
 
 			} catch (SocketException e) {
 				e.printStackTrace();
 			}
 			return null;
 		}
 
 		protected void onPostExecute(Long result) {
 		}
 	}
 
 
 	/**
 	 *	 
 	 * Foncton déclenché pour chaque Paquet reçu,
 	 * 
 	 * A un switch sur les le type de paquet et va exécuter les fonctions nécessaires
 	 * 
 	 * @param dataPacket, contient un packet
 	 */
 	private void analysePacket(DatagramPacket dataPacket) {
 
 		String json = null;
 		try {
 			json = LibUtil.decompress(dataPacket.getData());
 		} catch (IOException e) {
 			Log.e(TAG, e.getMessage());
 
 		}
 		Log.d(TAG, "Analyse packet:" + json);
 
 		Gson gson = new Gson();
 		PacketNetwork packetReceive = gson.fromJson(json, PacketNetwork.class);
 
 		Log.d(TAG, "Packet recu de " + packetReceive.getUser_envoyeur().getName());
 
 
 		/**
 		 * Si le packet n'est pas un ACK, on envoit un ACK
 		 */
 		if (packetReceive.type != PacketNetwork.ACK ) {
 
 			//On envoit un ACK
 			PacketNetwork packetSend = new PacketNetwork(packetReceive.getUser_envoyeur(), packetReceive.getRamdom_identifiant(),PacketNetwork.ACK);
 
 			packetSend.setUser_envoyeur(user_me);
 
 			SendPacket(packetSend);
 
 		}
 
 		if (packetReceive.type == PacketNetwork.ACK || !previousReceivedPacket.contains(packetReceive.getRamdom_identifiant())) {
 			previousReceivedPacket.add(packetReceive.getRamdom_identifiant());
 			analysePacket(packetReceive);
 		}
 
 	}
 
 	/**
 	 *	 
 	 * Foncton déclenché pour chaque Paquet reçu,
 	 * 
 	 * A un switch sur les le type de paquet et va exécuter les fonctions nécessaires
 	 * 
 	 * @param packet, contient un packet
 	 */
 	private void analysePacket(PacketNetwork packet) {
 
 		/*
 		 * En premier on vérifie que le paquet n'est pas contenu dans une liste de paquets à reconstituer
 		 */
 
 		if (packet.getRamdom_identifiant_groupe() != 0) {
 
 			if (packet.getNext_packet() == 0 && packet.getPrevious_packet() == 0) {
 				Log.d(TAG, "error previous et next 0);");
 				return;
 			}
 			if (listPaquetDivided.containsKey(packet.getRamdom_identifiant_groupe())) {
 				listPaquetDivided.get(packet.getRamdom_identifiant_groupe()).add(packet);
 			} else {
 				ArrayList<PacketNetwork> al = new ArrayList<PacketNetwork>();
 				al.add(packet);
 				listPaquetDivided.put(packet.getRamdom_identifiant_groupe(), al);
 			}
 			Log.d(TAG, "Taille de la pile de packet :" + listPaquetDivided.get(packet.getRamdom_identifiant_groupe()).size() + " taille attendue : " +packet.getNb_packet_groupe());
 
 			// Si on est sur le premier paquet on envoit un broadcast pour dire d'afficher un toast comme quoi on reçoit un fichier
 			if (listPaquetDivided.get(packet.getRamdom_identifiant_groupe()).size() == 1) {
 				Intent broadcastIntent = new Intent(NetworkService.FileTransferStart);
 				broadcastIntent.putExtra("conversation_id", packet.getContent().getConversation_id());
 				broadcastIntent.putExtra("isReceiver", true);	// Pour signifier qu'on est la personne qui *reçoit* le fichier
 				packet.getContent().getConversation_id();
 				sendBroadcast(broadcastIntent);
 			} 
 			// Dernie packet
 			else if (packet.getNb_packet_groupe() == listPaquetDivided.get(packet.getRamdom_identifiant_groupe()).size()) {
 
 				PacketNetwork packetFinal = PacketNetwork.reassemble(listPaquetDivided.get(packet.getRamdom_identifiant_groupe()));
 				analysePacket(packetFinal);
 
 				Intent broadcastIntent = new Intent(NetworkService.FileTransferFinish);
 				broadcastIntent.putExtra("conversation_id", packet.getContent().getConversation_id());
 				packet.getContent().getConversation_id();
 				sendBroadcast(broadcastIntent);
 			}
 
 		} else {
 
 			/*
 			 * on exécute les traitement à faire au niveau de la couche réseau
 			 */
 			switch (packet.type) {
 			case PacketNetwork.ACK : paquetACK(packet);
 			break;
 			case PacketNetwork.CREATION_GROUP : paquetCreationGroup(packet);
 			break;
 			case PacketNetwork.DISCONNECTED : paquetDisconnecter(packet);
 			break;
 			case PacketNetwork.HELLO : paquetHello(packet);
 			break;
 			case PacketNetwork.MESSAGE : paquetMessage(packet);
 			break;
 			case PacketNetwork.LOCALISATION : paquetLocalisation(packet);
 			break;
 			case PacketNetwork.ALIVE : paquetAlive(packet);
 			break;
 			default: paquetInconnu(packet);
 			break;
 
 			}
 
 		}
 
 	}
 
 	private void paquetAlive(PacketNetwork packet) {
 		// on le met à alive
 		listUsers.get(packet.getUser_envoyeur().getId()).setAlive(true);
 
 	}
 
 	/**
 	 * traite un packet de localisation reçu, ajout met à jour la localisation de l'user
 	 * @param packet
 	 */
 	private void paquetLocalisation(PacketNetwork packet) {
 		Log.d(TAG, "localisation reçu dans le NetworkService");
 		if (listUsers.containsKey(packet.getContent().getClient_id())) {
 			Localisation loca = new Localisation(packet.getContent().getLat(), packet.getContent().getLon());
 			User user = listUsers.get(packet.getContent().getClient_id());
 
 			user.setLocalisation(loca);
 		} else {
 			Log.d(TAG, "user inconnu");
 		}
 
 	}
 
 	/**
 	 * Traite un message reçu, le fait suivre à l'activity correspondant
 	 * Met l'état de l'utilisateur à "en ligne"
 	 * @param packetReceive
 	 */
 	private void paquetMessage(PacketNetwork packetReceive) {
 
 		Log.w(TAG, "> Message reçu dans le NetworkService");
 		if (listConversations.containsKey(packetReceive.getContent().getConversation_id())) {
 			Message message = new Message(packetReceive.getContent().getClient_id(),packetReceive.getContent().getMessage());
 			Log.d(TAG, "Network, ajout message avant " + listConversations.get(packetReceive.getContent().getConversation_id()).getListMessage().size());
 			Conversation conv = listConversations.get(packetReceive.getContent().getConversation_id());
 			conv.addMessage(message);
 			Log.d(TAG, "Network, ajout message apres " + listConversations.get(packetReceive.getContent().getConversation_id()).getListMessage().size());
 
 			if (packetReceive.getUser_envoyeur() != user_me) {
 				Intent broadcastIntent = new Intent(NetworkService.SendMessage);
 
 				Bundle bundle = new Bundle();
 
 				MessageBroacast messageBroad = new MessageBroacast(message.getClient_id(), message.getMessage(), packetReceive.getContent().getConversation_id());
 				/*
 				 * On teste si il y a un lien
 				 */
 				Log.d(TAG, "fichier :" +packetReceive.getContent().getFile_name() );
 
 				if (packetReceive.getContent().getFile_name() != null && packetReceive.getContent().getFile_name() != "") {
 
 					// Créer le sous dossier lo52 s'il n'existe pas 
 					File receiveDir = new File("/sdcard/lo52/");
 					receiveDir.mkdirs();
 
 					File file = new File(receiveDir, packetReceive.getContent().getFile_name());
 					Log.d(TAG, "ecriture fichier :" +file.getAbsolutePath());
 					LibUtil.writeFile(file, packetReceive.getContent().getByte_content());
 
 					messageBroad.setLink_file(file.getAbsolutePath());
 
 					// On set le message avec le chemin vers le fichier, puisqu'on a pas accès aux MessageBroadcasts depuis une Conversation
 					conv.getListMessage().get(conv.getMessageCount()-1).setMessage(MessageBroacast.MESSAGE_FILE_IDENTIFIER + ";" + file.getAbsolutePath());
 
 				}
 
 				bundle.putParcelable("message", messageBroad);
 				broadcastIntent.putExtra(MessageBroacast.tag_parcelable, bundle);
 
 				sendBroadcast(broadcastIntent);
 			}
 
 
 		} else {
 			Log.e(TAG, "Conversation non existante");
 		}
 
 		// on met le user à alive
 		listUsers.get(packetReceive.getUser_envoyeur().getId()).setAlive(true);
 
 	}
 
 	/**
 	 * Traite un message hello 
 	 * Vérfie que l'utilisateur existe sinon l'utilisateur est rajouté à la liste
 	 * Met l'état de l'utilisateur à "en ligne"
 	 * @param packetReceive
 	 * @param isACK	Détermine si la fonction est appelée après réception d'un ACK
 	 */
 	private void paquetHello(PacketNetwork packetReceive) {
 
 		if (packetReceive.getUser_envoyeur().getId() == user_me.getId()) {
 			return;
 		}
 
 		// on teste si l'user est connu
 		if ( listUsers.containsKey(packetReceive.getUser_envoyeur().getId()) ) {
 
 			// on teste si il est différent de celui stocké
 			if (listUsers.get(packetReceive.getUser_envoyeur().getId()) != packetReceive.getUser_envoyeur()) {
 
 				//alors on le met à ajour
 				listUsers.remove(packetReceive.getUser_envoyeur().getId());
 				listUsers.put(packetReceive.getUser_envoyeur().getId(), packetReceive.getUser_envoyeur());
 			}
 
 			//Si on a des conversations avec lui, on lui renvoit... 
 			for( Conversation convers : listConversations.values()) {
 				for(int user_id : convers.getListIdUser()) {
 					if (user_id == packetReceive.getUser_envoyeur().getId()) {
 						sendConversation(convers);
 					}
 				}
 			}
 
 		} else {
 			//on l'ajoute à la liste
 			listUsers.put(packetReceive.getUser_envoyeur().getId(), packetReceive.getUser_envoyeur());
 		}
 
 
 		paquetAlive(packetReceive);
 
 		// Envoi d'un broadcast à l'activité Lobby pour lui dire de rafraichir la vue de liste des utilisateurs
 		Intent broadcastIntent = new Intent(NetworkService.UserListUpdated);
 		Bundle bundle = new Bundle();
 		bundle.putString("new_user", packetReceive.getUser_envoyeur().getName());
 		broadcastIntent.putExtra("new_user", bundle);
 
 		sendBroadcast(broadcastIntent);
 	}
 
 
 	/**
 	 * Traite un message disconnected
 	 * Met l'état de l'utilisateur à disconnected
 	 * @param packetReceive
 	 */
 	private void paquetDisconnecter(PacketNetwork packetReceive) {
 		if (packetReceive.getUser_envoyeur().getId() == user_me.getId()) {
 			return;
 		}
 		// on teste si l'user est connu
 		if ( listUsers.containsKey(packetReceive.getUser_envoyeur().getId()) ) {
 			// on le met à no alive
 			listUsers.get(packetReceive.getUser_envoyeur().getId()).setAlive(false);
 		}
 
 		//on le fait le suivre à l'activity qui gère la liste des users (et peut être aussi à l'activity qui gère les conversations)
 		//sendToActivity(packetReceive,"lo52.messaging.activities.LobbyActivity");
 
 	}
 
 	/** TODO ajouter la possibilité de rajouter un user à la liste
 	 * Traite la la création d'un groupe
 	 * Ajoute un groupe avec sa liste de User
 	 * Vérifie que tout les users sont connus sinon on les rajoute dans notre liste
 	 * @param packetReceive
 	 */
 	private void paquetCreationGroup(PacketNetwork packetReceive) {
 		if (listConversations.containsKey(packetReceive.getContent().getConversation_id())) {
 
 			//Si le nom a changé, on le met à jour
 			if ( !(listConversations.get(packetReceive.getContent().getConversation_id()).getConversation_name() ==  packetReceive.getContent().getConversation_name())) {
 				Conversation conversation = listConversations.get(packetReceive.getContent().getConversation_id());
 				conversation.setConversation_name(packetReceive.getContent().getConversation_name());
 
 			}
 
 		} else {
 			ArrayList<Integer> listIdUser = new ArrayList<Integer>();
 			for(User user : packetReceive.getContent().getUserList()) {
 
 				if (user != null) {
 					listIdUser.add(user.getId());
 
 					if (!listUsers.containsKey(user.getId()) && user.getId() != user_me.getId()) {
 						//on considère alors le paquet comme aussi un paquet hello
 						listUsers.put(user.getId(), user);
 						// Envoi d'un broadcast à l'activité Lobby pour lui dire de rafraichir la vue de liste des utilisateurs
 						Intent broadcastIntent = new Intent(NetworkService.UserListUpdated);
 						Bundle bundle = new Bundle();
 						bundle.putString("new_user", packetReceive.getUser_envoyeur().getName());
 						broadcastIntent.putExtra("new_user", bundle);
 					}
 				}
 			}
 
 			// Fix pour ajouter user_me
 			if (!listIdUser.contains(user_me.getId())) {
 				listIdUser.add(user_me.getId());
 			}
 
 			Conversation conversation = new Conversation(packetReceive.getContent().getConversation_id(), packetReceive.getContent().getConversation_name(),listIdUser);
 
 			listConversations.put(conversation.getConversation_id(),conversation);
 
 			if (packetReceive.getUser_envoyeur() != user_me) {
 				Intent broadcastIntent = new Intent(NetworkService.SendConversation);
 				Bundle bundle = new Bundle();
 
 				bundle.putParcelable("conversation", conversation);
 				broadcastIntent.putExtra("conversation", bundle);
 
 				Log.d(TAG, "Envoi d'un broadcast de création de conversation");
 				sendBroadcast(broadcastIntent);
 			}
 
 		}
 
 
 	}
 
 	/**
 	 * Traite un ACK
 	 * Retire le paquet de la liste des paquets en attente d'ACK
 	 * TODO: voir si on renvoit quelque chose à l'activity
 	 * 
 	 * @param packetReceive
 	 */
 	private void paquetACK(PacketNetwork packetReceive) {
 
 		packetListACK.remove(packetReceive.getRamdom_identifiant());
 
 		if ( listUsers.get(packetReceive.getUser_envoyeur().getId()) == null ) {
 			//on considère un ACK comme un hello le cas échéant
 			paquetHello(packetReceive);
 		} else {
 			paquetAlive(packetReceive);
 		}
 		//sendToActivity(packetReceive,"lo52.messaging.activities.LobbyActivity");
 
 	}
 
 	/**
 	 * Pour traiter un paquet d'un type inconnu
 	 * @param packet
 	 */
 	private void paquetInconnu(PacketNetwork packet) {
 		Log.w(TAG, "Packet inconnu");
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public static Hashtable<Integer, User> getListUsers() {
 		return (Hashtable<Integer, User>) listUsers.clone();
 	}
 
 	/**
 	 * @deprecated
 	 * @return
 	 */
 	public static Hashtable<Integer, User> getListUsersNoClone() {
 		return (Hashtable<Integer, User>) listUsers;
 	}
 
 	//public static void setListUsers(Hashtable<Integer, User> listUsersE) {
 	//	listUsers = listUsersE;
 	//}
 
 	@SuppressWarnings("unchecked")
 	public static Hashtable<Integer, Conversation> getListConversations() {
 		return (Hashtable<Integer, lo52.messaging.model.Conversation>) listConversations.clone();
 	}
 
 	//public static Hashtable<Integer, Conversation> getListConversationsNoClone() {
 	//	return (Hashtable<Integer, lo52.messaging.model.Conversation>) listConversations;
 	//}
 
 	//public static void setListConversations(Hashtable<Integer, Conversation> listConversations) {
 	//	NetworkService.listConversations = listConversations;
 	//}
 
 	public static User getUser_me() {
 		return user_me;
 	}
 
 	/**
 	 * Retourne l'ID de l'user en fonction de son nom ou 0 s'il n'existe pas
 	 * @param username
 	 * @return
 	 */
 	@SuppressWarnings("rawtypes")
 	public static int getUserIdByName(String username) {
 		int userId = 0;
 		Hashtable<Integer, User> users = getListUsers();	// Récupère un clone
 
 
 		boolean found = false;
 		Iterator it = users.entrySet().iterator();
 		while (it.hasNext() && !found) {
 			Map.Entry pairs = (Map.Entry) it.next();
 
 			User u = (User) pairs.getValue();
 
 			if (u.getName().equals(username)) {
 				userId = u.getId();
 				found = true;
 			}
 		}
 
 		return userId;
 	}
 
 	/**
 	 * Ajoute la conversation qui a été créée par le service et dont le fragment doit être créé
 	 * Ajoute l'ID correspondant à la conversation qui a été créée par le service et dont le fragment correspondant doit être créé
 	 * dans le tab des conversations.
 	 * @param conversation_id
 	 */
 	public static void setHasLocalConversationToCreate(Conversation conversation) {
 		conversationsToCreateUI.add(conversation);
 	}
 
 
 	/**
 	 * Retourne la liste des IDs des conversations dont le fragment UI doit être créé. /!\ La liste est vidée une fois retournée. 
 	 * @return ArrayList contenant
 	 */
 	@SuppressWarnings("unchecked")
 	public static ArrayList<Conversation> getLocalConversationsToCreate() {
 		ArrayList<Conversation> l = (ArrayList<Conversation>) conversationsToCreateUI.clone();
 		conversationsToCreateUI.clear();
 
 		return l;
 	}
 
 
 	/**
 	 * Vérifie si une conversation existe déjà, en fonction des membres qui la composent
 	 * @param userIds	La liste des membres qui composent cette conversation
 	 * @return	Boolean
 	 */
 	@SuppressWarnings("rawtypes")
 	public static boolean doesConversationExist(ArrayList<Integer> userIds) {
 
 		boolean exists = false;
 		Iterator it = listConversations.entrySet().iterator();
 		while (it.hasNext() && !exists) {
 			Map.Entry pairs = (Map.Entry)it.next();
 			Conversation c = (lo52.messaging.model.Conversation) pairs.getValue();
 
 			// On compare la liste des utilisateurs à celle donnée en paramètre
 			//if (c.getListIdUser().equals(userIds)) exists = true;
 			if (LibUtil.equalsListsOrderInsensitive(c.getListIdUser(), userIds)) exists = true;
 		}
 
 		return exists;
 	}
 
 	/**
 	 * Pemet de vérifier que les messages ont bien été reçut
 	 * @author mtparet3
 	 *
 	 */
 	private class checkACKTask extends TimerTask {
 		public void run() {
 
 			while(true){
 				@SuppressWarnings("unchecked")
 				Hashtable<Integer,PacketNetwork> cl = (Hashtable<Integer, PacketNetwork>) packetListACK.clone();
 
 				for(PacketNetwork packet : cl.values()) {
 					int now = (int) System.currentTimeMillis();
 
 					if (now > (packet.getDate_send() + 100000)) {
 						Log.d(TAG, "paquet sans ACK détruit:" + packet.getRamdom_identifiant());
 						packetListACK.remove(packet);
 					} else {
 						if (now > (packet.getDate_send() + 20000)) {
 							Log.d(TAG, "paquet sans ACK renvoyé:" + packet.getRamdom_identifiant());
 
 							SendSocket sendSocket = new SendSocket();
 							PacketNetwork[] packets = new PacketNetwork[1];
 							packets[0] = packet;
 
 							//Exécution de l'asyncTask
 							sendSocket.execute(packets);
 
 							packetListACK.remove(packet);
 
 							try {
 								Thread.sleep(200);         
 							} catch (InterruptedException e) {
 								e.printStackTrace();
 							}
 						}
 					}
 				}
 
 				try {
 					Thread.sleep(10000);         
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 
 
 }
