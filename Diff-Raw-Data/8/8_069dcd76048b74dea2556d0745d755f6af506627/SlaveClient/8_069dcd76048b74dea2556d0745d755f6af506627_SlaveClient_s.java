 package client.slave;
 
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 
 import java.security.InvalidKeyException;
 import java.security.Key;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.PublicKey;
 import java.security.SignatureException;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.crypto.KeyAgreement;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.PBEKeySpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import utils.Crypto;
 import utils.Tools;
 import utils.Utils;
 
 import client.Client;
 import client.slave.ihm.SlaveClientGUI;
 
 public class SlaveClient extends Client {
 
 	/** Groups associated with their creator (ip) which a client has been invited */
 	private HashMap<String, String> _listGroups;
 	/** */
 	private ArrayList<String> _groupMembers;
 	/** */
 	private int _pos;
 	
 	/** Disposable certificate of a client */
 	private byte[] _dcertif;
 	
 	/**
 	 * Constructor
 	 */
 	public SlaveClient (String username) {
 		super(username);
 		
 		try {
 			_dcertif = Tools.encryptWithPass(_username, new String(Utils.readPassword("Enter your password : ")), Crypto.loadPubKey(new File("server/public.key")).getEncoded()); // Pour l'instant un certificat=chiffr avec mon pass de pubKeyServer + on verra si autre chose
 			_listGroups = new HashMap<String,String>();
 			_broadcastSocket = new MulticastSocket(9999);
 			_broadcastSocket.joinGroup(_ipGroup);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 	} // SlaveClient ()
 
 	/**
 	 * Allow a client to receive invitation then if the client wished this group, he sends an answer
 	 * @throws IOException
 	 * @throws NoSuchPaddingException 
 	 * @throws InvalidKeySpecException 
 	 * @throws NoSuchAlgorithmException 
 	 * @throws InvalidKeyException 
 	 * @throws SignatureException 
 	 */
 	public void receiveInvitation () throws IOException, InvalidKeyException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, SignatureException {
 		while(_loop) {
 			// Receiving a broadcast invitation
 			byte[] receiveDtg = new byte[1024];
 			DatagramPacket invitation = new DatagramPacket(receiveDtg, receiveDtg.length);
 			_broadcastSocket.receive(invitation);
 			byte[] tInvitation = invitation.getData();
 			int size = Utils.byteArrayToInt(Arrays.copyOfRange(tInvitation, 0, 4));
 			size += 4; // Not very beautiful
 			byte[] grpInvitation = Arrays.copyOfRange(tInvitation, 4, size-128);
 			byte[] certInvitation = Arrays.copyOfRange(tInvitation, size-128, size);
 			byte[] serverPubKey = Crypto.loadPubKey(new File("server/public.key")).getEncoded();
 			// Verifying the certificate
 			boolean signOK = Tools.verifSign(grpInvitation, serverPubKey, certInvitation);
 			System.out.println("Receiving invitation : " + invitation.getAddress()); // DEBUG
 			// Verifying if this group has already been created
 			if (signOK && !(_listGroups.containsKey(invitation.getAddress()) && _listGroups.containsValue(new String(grpInvitation)))) {
 				_listGroups.put(invitation.getAddress().getHostAddress(), new String(grpInvitation));
 				SlaveClientGUI.get_jgroup().refresh(Utils.byteArrayToHexString(certInvitation));
 				System.out.println("New invitation request.");
 			} else
 				System.err.println("This group has already been created (or certificate error).");
 		}
 		
 	} // receiveInvitation ()
 	
 	/**
 	 * Following process invitation. The client choose among groups in listGroups he's interested, the group to join
 	 * @param grp the name of the group to join
 	 * @param ipClientBis the Address IP of the client Master
 	 * @throws Exception
 	 */
 	public void requestJoinGroup (String grp, String ipClientBis) throws Exception {
 		System.out.println("Request join group"); // DEBUG
 		connectionServer(ipClientBis, 10000);
 		
 		// Here we have to send : certificate + encrypt(identity + auth + certificate's footprint) with pubKeyServer
 		System.out.println("Sending the certificate."); // DEBUG
 		send(Utils.intToByteArray(_dcertif.length, 4));
 		send(_dcertif);
 		byte[] serverPubKey = Crypto.loadPubKey(new File("server/public.key")).getEncoded();
 		byte[] imprint = Tools.hash(_dcertif);
 		byte[] ciphered = Tools.encrypt(Utils.concatenateByteArray(_username.getBytes(), imprint), serverPubKey); // + auth
 		System.out.println("Sending the encrypted."); // DEBUG
 		send(Utils.intToByteArray(ciphered.length, 4));
 		send(ciphered);
 		
 		// Receiving the signed certificate
 		System.out.println("Receiving the signed certificate.\n"); // DEBUG
 		byte[] size = receive(4);
 		_certificate = receive(Utils.byteArrayToInt(size));
 		
 	} // requestJoinGroup ()
 	
 	/**
 	 * Each client must use this function to bind itself with its neighboor for the creation of the ring
 	 * @param ipClientBis is the Address IP of the client master
 	 * @throws Exception 
 	 */
 	public void linkNeighboor () throws Exception {
 		System.out.println("Linking neighboor."); // DEBUG
 		// Receiving the ip list of the ring (for this group)
 		_inServer = _clientSocket.getInputStream();
 		byte[] sizeHash = receive(4);
 		byte[] hash = receive(Utils.byteArrayToInt(sizeHash));
 		byte[] sizeList = receive(4);
 		byte[] list = receive(Utils.byteArrayToInt(sizeList));
 		closeConnectionServer();
 		
 		// Verifying the hash
 		if(!Arrays.equals(Tools.hash(list), hash)) {
 			System.err.println("The integrity of the list has not been verified."); // DEBUG
 			return;
 		}
 		
 		_groupMembers = Utils.byteArrayToList(list);
 		
 		// The client searchs its ip to determinate the ip following its own ip :
 		if((_pos = (_groupMembers.indexOf(InetAddress.getLocalHost().getHostAddress()))) > -1){
 			connectionNeighboor(_groupMembers.get(_pos+1), _port);
 			System.out.println("I'm linked with my neighboor."); // DEBUG
 		}
 		else
 			System.err.println("Ip doesn't exist in the list of accepted clients."); // DEBUG
 		
 		startServerMode(_port);
 		closeConnectionServer();
 		
 	} // linkNeighboor ()
 	
 	public void doDiffieHellman () throws Exception {
 		System.out.println("Beginning Diffie Hellman."); // DEBUG
 	    KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
 
 	    keyGen.initialize(PARAMETER_SPEC);
 
 	    KeyAgreement bKeyAgree = KeyAgreement.getInstance("DH");
 	    KeyPair bPair = keyGen.generateKeyPair();
 
 	    bKeyAgree.init(bPair.getPrivate());
 	    sendChat(Utils.intToByteArray(bPair.getPublic().getEncoded().length, 4));
 		sendChat(bPair.getPublic().getEncoded());
 		Key key = null;
 	    for(int i = 0; i < _groupMembers.size()-1; ++i) {
 		    // Receiving the publicKey of a
 		    System.out.println("Receiving a public key (a part of the secret key)."); // DEBUG
 		    byte[] size = receiveChat(4);
 			byte[] pubA = receiveChat(Utils.byteArrayToInt(size));
 			KeyFactory factory = KeyFactory.getInstance("DH");
 			PublicKey aPubKey = factory.generatePublic(new X509EncodedKeySpec(pubA));
 			
 			if(i == _groupMembers.size()-2) {
 		        key = bKeyAgree.doPhase(aPubKey, true);
 		        break;
 			} else
 				key = bKeyAgree.doPhase(aPubKey, false);
 			
 		    sendChat(Utils.intToByteArray(key.getEncoded().length, 4));
 			sendChat(key.getEncoded());
 	    }
 	    
 	   byte[] sharedSecret = bKeyAgree.generateSecret();
 	   SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WITHHMACSHA1");
 	   byte[] salt = new byte[] { (byte)0xe0, 0x4f, (byte)0xd0, 0x20, (byte)0xea, 0x3a, 0x69, 0x10, (byte)0xa2, (byte)0xd8, 0x08, 0x00, 0x2b, 0x30, 0x30, (byte)0x9d };
 	   PBEKeySpec keySpec = new PBEKeySpec(new String(sharedSecret).toCharArray(), salt, 1000, 128);
 	   _sk = new SecretKeySpec(factory.generateSecret(keySpec).getEncoded(), "AES");
 	    
 	   System.out.println("Secret: " + Utils.byteArrayToHexString(_sk.getEncoded()) + "\nSize : " + _sk.getEncoded().length + "\n"); // DEBUG
 	    
 	} // doDiffieHellman ()
 	
 	public void sendMessage (String text) throws Exception {
 		System.out.println("Send a message (slave)."); // DEBUG
 		int cpt = 0;
 		byte[] count = Utils.intToByteArray(cpt, 2);
 		byte[] messageTmp = text.getBytes();
 		byte[] cipher = Tools.encryptSym(messageTmp, _sk);
 		System.out.println("Send cipher: " + Utils.byteArrayToHexString(cipher)); // DEBUG
 		byte[] message = Utils.concatenateByteArray(cipher, count);
 		
 		sendChat(Utils.intToByteArray(message.length, 4));
 		sendChat(message);
 		
 	} // sendMessage ()
 	
 	public void transmitMessage () throws Exception {
 		while(true) {
 			byte[] size = receiveChat(4);
 			byte[] message = receiveChat(Utils.byteArrayToInt(size));
 			byte[] messageTmp = Arrays.copyOfRange(message, 0, message.length-2);
 			System.out.println("Receive cipher: " + Utils.byteArrayToHexString(messageTmp)); // DEBUG
 			
 			int cpt = Utils.byteArrayToInt(Arrays.copyOfRange(message, message.length-2, message.length));
 			System.out.println("Counter : " + cpt); // DEBUG
 			if(cpt < _groupMembers.size()-1) {
 				// Decrypting the message
 				byte[] plain = Tools.decryptSym(messageTmp, _sk);
				int pos = _groupMembers.size()-(cpt+_pos+1);
				String emetteur = _groupMembers.get(pos); // A toi cest pa la bonne ip
 				if(SlaveClientGUI.get_chat().get_fieldChat().getText().contentEquals(""))
 					SlaveClientGUI.get_chat().get_fieldChat().setText(emetteur + ": " + new String(plain));
 				else
 				    SlaveClientGUI.get_chat().get_fieldChat().setText(SlaveClientGUI.get_chat().get_fieldChat().getText() + "\n" + emetteur + ": " + new String(plain));
 				cpt += 1;
 				message = Utils.concatenateByteArray(messageTmp, Utils.intToByteArray(cpt, 2));
 				sendChat(size);
 				sendChat(message);
 			}
 		}
 		
 	} // transmitMessage ()
 	
 	/**
 	 * Accessor
 	 * @return the groups with their creators (IP)
 	 */
 	public HashMap<String, String> get_listGroups () {
 		return _listGroups;
 		
 	} // get_listGroups ()
 
 	/**
 	 * 
 	 * @param loop
 	 */
 	public void set_loop (boolean loop) {
 		_loop = loop;
 		
 	} // set_loop ()
 
 } // SlaveClient
