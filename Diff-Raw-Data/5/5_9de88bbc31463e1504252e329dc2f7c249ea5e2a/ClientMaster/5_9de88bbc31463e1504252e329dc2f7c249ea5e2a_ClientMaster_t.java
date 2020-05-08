 package clientMaster;
 
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import java.io.File;
 import java.io.IOException;
 
 import clientSupreme.Client;
 
 import utils.Crypto;
 import utils.Tools;
 import utils.Utils;
 
 public class ClientMaster extends Client {
 	/** List of ip clients which accepted the invitation and are accepted to join the group */
 	private ArrayList<String> _acceptedClients; 
 	private MulticastSocket _broadcastSocketRing;
 	private InetAddress _groupIpRing;
 	
 	/** Server public key */
     private byte[] _publicKey;
 	
 	private volatile boolean _start = false;
 	private volatile boolean _loop = true;
 
 	/**
 	 * Constructor
 	 * @param adressServer is the address of the server          
 	 * @param portServer is the port of the server         
 	 */
 	public ClientMaster(String adressServer, int port, String username) {
 		try {
 			// Vrification de l'existence d'une paire de clef
 			// Sauvegarde si necessaire (si un seul fichier est absent on rgnre tout)
 			if(!(new File("keys/private.key").exists() && new File("keys/private.salt.key").exists() && new File("keys/public.key").exists()))
 				Tools.keyGenerator(); // Idem que Client Slave on devrait faire un constructeur commun
 						
             _username = username;
			connectionServer(adressServer, port);
 			_groupIp = InetAddress.getByName("239.255.80.84"); // A voir
 			_groupIpRing = InetAddress.getByName("239.255.80.85");
 			_broadcastSocket = new MulticastSocket();
 			_broadcastSocket.joinGroup(_groupIp);
 			_broadcastSocketRing = new MulticastSocket();
 			_acceptedClients = new ArrayList<String>();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	} // ClientMaster ()
 
 	/**
 	 * Used by a client to request a creation of a group to the server
 	 * @param nameGroup is the name of the group the client wants
 	 * @throws Exception 
 	 */
 	public void requestCreationGroup(String nameGroup) throws Exception {
 		send(CREATION);
 		
 		_keyPair = Crypto.loadKeyPair(new File("keys/private.key"), new File("keys/private.salt.key"), new File("keys/public.key"));
 		
 		identityControl();
 		
 		send(Utils.intToByteArray(_username.getBytes().length, 4));
 		send(_username.getBytes());
 		send(Utils.intToByteArray(nameGroup.getBytes().length, 4));
 		send(nameGroup.getBytes());
 		byte[] signature = Tools.sign(_keyPair.getPrivate(), Tools.concatenateByteArray(_username.getBytes(), nameGroup.getBytes()));
 		send(Utils.intToByteArray(signature.length, 4));
 		send(signature);
 	} // requestCreationGroup ()
 
 	/**
 	 * Used by the client to know the response of the group's creation from the server
 	 * @param nameGroupWished is the name of the group the client wants
 	 * @return boolean
 	 * @throws IOException
 	 * @throws ClassNotFoundException 
 	 */
 	public Boolean responseCreationGroup()throws IOException, ClassNotFoundException {
 		byte[] response = receive(2);
 		if(Arrays.equals(response, OK)) {
 		    byte[] size = receive(4);
 		    byte[] grpWished = receive(Utils.byteArrayToInt(size));
 		    System.out.println("Response OK : " + new String(grpWished)); // DEBUG
 		    return true;
 		} else if(Arrays.equals(response, NOK)) {
 			byte[] size = receive(4);
 		    byte[] grpWished = receive(Utils.byteArrayToInt(size)); // + Raison Echec
 		    return false;
 		}
 		return false;
 		
 	} // responseCreationGroup ()
 	
 	/**
 	 * Mthode ralisant le contrle d'identit
 	 * @throws Exception
 	 */
 	public void identityControl() throws Exception {
 		// Hashage MD5 de le clef publique
 		byte[] hash = Tools.hashFile("keys/public.key");
 		// Envoie du hash
 		System.out.println("Envoie de l'empreinte de la clef publique : " + Utils.byteArrayToHexString(hash)); // DEBUG
 		send(Utils.intToByteArray(hash.length, 1));
 		send(hash);
 
 		// Rsultat du hash
 		byte[] verif = receive(2);
 		if(Arrays.equals(verif, OK)) {
 			System.out.println("Votre clef est dj enregistre auprs du destinataire (rceprion OK)."); // DEBUG
 			
 			System.out.println("Inversion des rles."); // DEBUG
 			changeRole();
 		} else if(Arrays.equals(verif, NOK)) {
 			// Rception du challenge
 			byte[] challengeR = receive(16);
 			System.out.println("Rception du challenge (rception NOK)."); // DEBUG
 
 			// Envoie de la clef publique et de la signature clef publique/challenge
 			System.out.println("Envoie de la clef publique/signature."); // DEBUG
 			byte[] pubKey = _keyPair.getPublic().getEncoded();
 			send(Utils.intToByteArray(pubKey.length, 4));
 			send(pubKey);
 						
 			byte[] signature = Tools.sign(_keyPair.getPrivate(), Tools.concatenateByteArray(_keyPair.getPublic().getEncoded(), challengeR));
 			send(Utils.intToByteArray(signature.length, 4));
 			send(signature);
 
 			// Rception de l'empreinte
 			byte[] tailleHash = receive(1);
 			byte[] empreinte = receive(Utils.byteArrayToInt(tailleHash));
 			System.out.println("Enpreinte reue : " + Utils.byteArrayToHexString(empreinte)); // DEBUG
 
 			// Comparaison des empreintes
 			System.out.println("Mon empreinte : " + Utils.byteArrayToHexString(hash)); // DEBUG
 			// Validation des empreintes
 			if(Arrays.equals(empreinte, hash)) {
 				System.out.println("Les empreintes sont bien valides."); // DEBUG
 				send(OK);
 
 				System.out.println("Inversion des rles."); // DEBUG
 				changeRole();
 			} else {
 				System.out.println("Les empreintes reue et relle sont diffrentes."); // DEBUG
 				send(NOK);
 			}
 		}
 	} // identityControl()
 	/**
 	 * Mthode permettant d'changer les rles au cours du contrle d'identit
 	 * @throws Exception : Tout exception (non gr)
 	 */
 	public void changeRole() throws Exception {
         // Rception du hash de controle d'identit
 		byte[] tailleHash = receive(1);
 		byte[] hash = receive(Utils.byteArrayToInt(tailleHash));
 		System.out.println("Rception du hash : " + Utils.byteArrayToHexString(hash)); // DEBUG
 		
 		// Vrification du hash
 		if(Tools.isPubKeyStored(hash)) {
 			// Envoie OK
 			_publicKey = Crypto.loadPubKey(new File("contacts/" + Utils.byteArrayToHexString(hash) + ".key")).getEncoded();
 			System.out.println("Vrification OK (envoie OK)."); // DEBUG
 			send(OK);
 			// Fin de l'change
 		} else {
 			// Envoie du challenge
 			System.out.println("Envoie du challenge (envoie NOK)."); // DEBUG
 			byte[] challenge = Tools.getChallenge();
 			send(NOK);
 			send(challenge);
 			
 			// On recoit la cl publique et la signature
 			System.out.println("Rception de la clef publique et de la signature."); // DEBUG
 			byte[] taillePubKey = receive(4);
 			_publicKey = receive(Utils.byteArrayToInt(taillePubKey));
 			byte[] tailleSign = receive(4);
 		    byte[] signature = receive(Utils.byteArrayToInt(tailleSign));
 
 		    // Vrification de la signature
 		    byte[] data = Tools.concatenateByteArray(_publicKey, challenge);
 		    boolean verif = Tools.verifSign(data, _publicKey, signature);
 		    if(verif) {
 		    	System.out.println("La vrification a russie."); //DEBUG
 		    	
 		    	// Envoie du hash
 		    	System.out.println("Envoie du hash."); // DEBUG
 		    	byte[] empreinte = Tools.hash(_publicKey);
 		    	send(Utils.intToByteArray(empreinte.length, 1));
 		    	send(empreinte);
 		    	
 		    	// Validation de l'empreinte
 		    	byte[] valide = receive(2);
 		    	if(Arrays.equals(valide, OK)) {
 		    		System.out.println("Le serveur a valid l'empreinte."); // DEBUG
 		    	    // Sauvegarde de la cl publique
 		    		System.out.println("Sauvegarde de la cl publique."); // DEBUG
 		    	    Utils.saveBuffer(_publicKey, new File("contacts/" + Utils.byteArrayToHexString(empreinte) + ".key"));
 		    	    // Fin de l'change
 		    	} else if(Arrays.equals(valide, NOK))
 		    		System.out.println("Le serveur n'a pas valid l'empreinte."); //DEBUG
 		    } else 
 		    	System.out.println("La vrification a choue."); // DEBUG
 		}	
 		
 	} // changeRole()
 
 	/**
 	 * Used by the client master to invite clients to join its group
 	 * @param nameGroup the client Master's group
 	 * @param portClient the port of the clients
 	 * @throws IOException
 	 * @throws InterruptedException 
 	 */
 	public void Invitation(String nameGroup, int portClient) throws IOException, InterruptedException {
 		byte[] invitation = nameGroup.getBytes(); // The nameGroup is considered as an invitation we can use a key word as invitation !
 		byte[] receiveDtg = new byte[1024]; // answers from interested clients
 
 		DatagramPacket reception;
 		DatagramPacket toSend = new DatagramPacket(invitation, invitation.length, _groupIp, 9999);
 		// The client (bis) will stop the loop when he wants, so the discussion could begin
 		while (_loop) {
 			_broadcastSocket.send(toSend);
 			reception = new DatagramPacket(receiveDtg, receiveDtg.length);
 			if(_start) { // Use byte array constant for stop
 				byte[] stop = new String("stop").getBytes();
 				toSend = new DatagramPacket(stop, stop.length, _groupIp, 9999);
 				_broadcastSocket.send(toSend);
 				break;
 			}
 			_broadcastSocket.receive(reception);
 			System.out.println(reception.getAddress()); // DEBUG
 			// Les faire s'authentifier ICI avant d'accepter !!!
 			if (!_acceptedClients.contains(reception.getAddress().getHostAddress()))   
 				_acceptedClients.add(reception.getAddress().getHostAddress()); // IPAdress of a enjoyed client is added in the ArrayList to create the ring 		
 			System.out.println("Client added"); // DEBUG
 		}
 		
 	} // Invitation ()
 	
 	/**
 	 * Distribution of the adresses ips to the clients slave from the client master. Moreover,
 	 * this function creates the first link between the client master and the first client slave for the ring.
 	 * @throws IOException
 	 */
 	public void creationGroupDiscussion () throws IOException {
 		_acceptedClients.add(InetAddress.getLocalHost().getHostAddress());
 		byte[] toSend = Utils.arrayListToByte(_acceptedClients);
 		_broadcastSocketRing.joinGroup(_groupIpRing);
 		DatagramPacket pck = new DatagramPacket(toSend, toSend.length, _groupIpRing, _port);
 		_broadcastSocketRing.send(pck);
 		
 		String ipNeighboor = _acceptedClients.get(0);
		connectionNeighboor("192.168.0.13", _port);
 		
 		startServerMode(_port);
 		
 	} // creationGroupDiscussion()
 	
 	public boolean is_start() {
 		return _start;
 		
 	} // is_start ()
 
 	public void set_start(boolean start) {
 		_start = start;
 		
 	} // set_start ()
 
 } // ClientMaster
