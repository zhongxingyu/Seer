 package org.fnppl.opensdx.securesocket;
 
 /*
  * Copyright (C) 2010-2011 
  * 							fine people e.V. <opensdx@fnppl.org> 
  * 							Henning Thieß <ht@fnppl.org>
  * 
  * 							http://fnppl.org
 */
 
 /*
  * Software license
  *
  * As far as this file or parts of this file is/are software, rather than documentation, this software-license applies / shall be applied.
  *  
  * This file is part of openSDX
  * openSDX is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * openSDX is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * and GNU General Public License along with openSDX.
  * If not, see <http://www.gnu.org/licenses/>.
  *      
  */
 
 /*
  * Documentation license
  * 
  * As far as this file or parts of this file is/are documentation, rather than software, this documentation-license applies / shall be applied.
  * 
  * This file is part of openSDX.
  * Permission is granted to copy, distribute and/or modify this document 
  * under the terms of the GNU Free Documentation License, Version 1.3 
  * or any later version published by the Free Software Foundation; 
  * with no Invariant Sections, no Front-Cover Texts, and no Back-Cover Texts. 
  * A copy of the license is included in the section entitled "GNU 
  * Free Documentation License" resp. in the file called "FDL.txt".
  * 
  */
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.Socket;
 import java.util.Arrays;
 import java.util.Vector;
 
 
 import org.bouncycastle.asn1.x509.V1TBSCertificateGenerator;
 import org.fnppl.opensdx.security.AsymmetricKeyPair;
 import org.fnppl.opensdx.security.OSDXKey;
 import org.fnppl.opensdx.security.OSDXMessage;
 import org.fnppl.opensdx.security.Result;
 import org.fnppl.opensdx.security.SecurityHelper;
 import org.fnppl.opensdx.security.Signature;
 import org.fnppl.opensdx.security.SymmetricKey;
 import org.fnppl.opensdx.xml.Document;
 import org.fnppl.opensdx.xml.Element;
 
 
 
 public class OSDXSocket implements OSDXSocketSender, OSDXSocketLowLevelDataHandler {
 	
 	private static String version = "openSDX 0.1";
 	protected Socket socket = null;
 	private long timeout = 2000;
 	
 	protected String host = null;
 	private int port = -1;
 	private String prepath = "/";
 	
 	private OSDXKey mySigningKey = null;
 	private byte[] client_nonce = null;
 	private byte[] server_nonce = null;
 	private SymmetricKey agreedEncryptionKey = null;
 	
 	protected String message = null;
 	public OutputStream log = null;
 	
 	private OSDXSocketDataHandler dataHandler = null;
 	private OSDXSocketReceiver receiver = null;
 	private boolean secureConnectionEstablished = false;
 	
 	public final static String ERROR_NO_RESPONSE = "ERROR: server does not respond.";
 	
 	
 	public OSDXSocket() {
 		dataHandler = null;
 	}
 	
 	//this one can take control after secure connection is established
 	public void setDataHandler(OSDXSocketDataHandler dataHandler) {
 		this.dataHandler = dataHandler;
 	}
 	
 	//you can take over control after secure connection is established
 	public void handleNewText(String text, OSDXSocketSender sender) {
 		if (secureConnectionEstablished) {
 			if (text.equals("Connection closed.")) {
 				try {
 					closeConnection();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				if (dataHandler!=null) {
 					dataHandler.handleNewText(text, this);
 				} else {
 					System.out.println("RECEIVED TEXT: "+text);
 				}
 			}
 		}
 	}
 	
 	public void handleNewInitMsg(String[] lines, byte[] encdata, OSDXSocketSender sender) {
 //		System.out.println("handle init msg");
 //		for (int i=0;i<lines.length;i++) {
 //			System.out.println((i+1)+" :: "+lines[i]);
 //		}
 //		System.out.println("data len :: "+encdata.length);
 		
 		try {
 			//check signature
 			byte[] server_mod = SecurityHelper.HexDecoder.decode(lines[3]);
 			byte[] server_exp = SecurityHelper.HexDecoder.decode(lines[4]);
 			byte[] server_signature = SecurityHelper.HexDecoder.decode(lines[5]);
 			
 			AsymmetricKeyPair server_pubkey = new AsymmetricKeyPair(server_mod, server_exp, null);
 			byte[][] checks = SecurityHelper.getMD5SHA1SHA256(encdata);
 			boolean verifySig = server_pubkey.verify(server_signature, checks[1],checks[2],checks[3],0L);
 			
 			//System.out.println("signature verified: "+verifySig);
 			if (verifySig)  {
 				//build enc key
 				
 				byte[] data =  mySigningKey.decryptBlocks(encdata);
 				String[] encLines = new String(data, "UTF-8").split("\n");
 //				for (int i=0;i<encLines.length;i++) {
 //					System.out.println("ENC "+(i+1)+" :: "+encLines[i]);
 //				}
 				server_nonce = SecurityHelper.HexDecoder.decode(encLines[1]);
 				
 				byte[] concat_nonce = SecurityHelper.concat(client_nonce, server_nonce);
 //				System.out.println("byte len :: concat_nonce = "+concat_nonce.length);
 				byte[] key_bytes = SecurityHelper.getSHA256(concat_nonce); 			//32 bytes = 256 bit
 				byte[] iv = Arrays.copyOf(SecurityHelper.getMD5(concat_nonce),16);	//16 bytes = 128 bit				
 //				System.out.println("byte len :: iv = "+iv.length+"  b = "+key_bytes.length);
 //				System.out.println(SecurityHelper.HexDecoder.encode(iv, '\0', -1));
 //				System.out.println(SecurityHelper.HexDecoder.encode(key_bytes, '\0', -1));
 				agreedEncryptionKey = new SymmetricKey(key_bytes, iv);
 				receiver.setEncryptionKey(agreedEncryptionKey);
 				secureConnectionEstablished = true;
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 
 	public void handleNewData(byte[] data, OSDXSocketSender sender) {
 		if (secureConnectionEstablished && dataHandler != null) {
 			dataHandler.handleNewData(data, this);
 		} else {
 			System.out.println("RECEIVED UNHANDLED DATA, length = "+data.length+" bytes");
 		}
 	}
 	
 	public boolean connect(String host, int port, String prepath, OSDXKey mySigningKey) throws Exception {
 		this.host = host;
 		this.port = port;
 		if (prepath==null || prepath.length()==0) {
 			this.prepath = "/";
 		} else {
 			this.prepath = prepath;
 		}
 		this.mySigningKey = mySigningKey;
 		//this.myEncryptionKey = myEncryptionKey;
 		secureConnectionEstablished = false;
 		client_nonce = null;
 		server_nonce = null;
 		agreedEncryptionKey = null;
 		socket = new Socket(host, port);
 		//System.out.println("inner connect ok: "+socket.isConnected());
 		if (socket.isConnected()) {
 			//System.out.println("socket connected");
 			receiver = OSDXSocketReceiver.initClientReceiver(socket.getInputStream(),this,this);
 			sendInitConnection(host, mySigningKey);
 			long start = System.currentTimeMillis();
 			boolean timeoutevent = false;
 			while(!secureConnectionEstablished && !timeoutevent) {
 				timeoutevent = System.currentTimeMillis()>start+timeout;
 				//System.out.println("waiting...");
 				Thread.sleep(100);
 			}
 			return !timeoutevent;
 		} else {
 			System.out.println("ERROR: Connection to server could NOT be established!");
 			return false;
 		}
 	}
 	
 	private void sendInitConnection(String host, OSDXKey key) {
 		try {
 			client_nonce = SecurityHelper.getRandomBytes(32);
 			String init = version +"\n";
 			init += host+"\n";
 			init += SecurityHelper.HexDecoder.encode(client_nonce,':',-1)+"\n";
 //			if (keyid==null) {
 //				init += "00\n";
 //			} else {
 //				init += keyid+"\n";
 //			}
 			init += key.getKeyID()+"\n";
 			init += SecurityHelper.HexDecoder.encode(key.getPublicModulusBytes(),':',-1)+"\n";
 			init += SecurityHelper.HexDecoder.encode(key.getPublicExponentBytes(),':',-1)+"\n";
 			byte[][] checks = SecurityHelper.getMD5SHA1SHA256(client_nonce);
 			init += SecurityHelper.HexDecoder.encode(key.sign(checks[1],checks[2],checks[3],0L),':',-1)+"\n";
 			init += "\n";
 			sendBytesPacket(init.getBytes("UTF-8"), TYPE_NULL, false);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	private static byte TYPE_TEXT = 84;
 	private static byte TYPE_DATA = 68;
 	private static byte TYPE_NULL = 0;
 	private Object o = new Object();
 	private boolean sendBytesPacket(byte[] data, byte type, boolean encrypt) {
 		if (encrypt && !secureConnectionEstablished) {
 			return false;
 		}
 		synchronized (o) {
 			try {
 				OutputStream out = socket.getOutputStream();
 				if (type==TYPE_NULL) {
 					out.write(data);
 					out.flush();
 				} else {
 					byte[] send = new byte[data.length+1];
 					send[0] = type;
 					System.arraycopy(data, 0, send, 1, data.length);;
 					if (encrypt && agreedEncryptionKey!=null) {
 						send = agreedEncryptionKey.encrypt(send);
 					}
 					out.write((send.length+"\n").getBytes("UTF-8"));
 					out.write(send);
 					out.flush();
 				}
 				return true;
 			} catch (Exception ex) {
 				System.out.println("ERROR in sendBytesPacket: "+ex.getMessage());
 				if (!ex.getMessage().equals("Broken pipe")) {
 					secureConnectionEstablished = false;
 					ex.printStackTrace();
 				}
 				message = ex.getMessage();
 			}
 		}
 		return false;
 	}
 	
 	public boolean sendEncryptedData(byte[] data) {
 		if (secureConnectionEstablished && agreedEncryptionKey!=null) {
 			try {
 				return sendBytesPacket(data,TYPE_DATA,true);
 			} catch (Exception ex) {
 				ex.printStackTrace();
 				message = ex.getMessage();
 			}
 		}
 		return false;
 	}
 	
 	public boolean sendEncryptedText(String text) {
 		if (secureConnectionEstablished && agreedEncryptionKey!=null) {
 			try {
 				return sendBytesPacket(text.getBytes("UTF-8"),TYPE_TEXT,true);
 			} catch (Exception e) {
 				e.printStackTrace();
 				message = e.getMessage();
 			}
 		}
 		return false;
 	}
 	
 	
 //	public void writeLog(HTTPClientRequest req, HTTPClientResponse resp, String name) {
 //		if (log!=null) {
 //			try {
 //				log.write(("--- REQUEST "+name+" ----------\n").getBytes());
 //				req.toOutputNOT_URL_ENCODED_FOR_TESTING(log);
 //				log.write(("--- END of REQUEST "+name+" ---\n").getBytes());
 //				if (resp == null) {
 //					log.write(("-> --- "+ERROR_NO_RESPONSE+" ---\n").getBytes());
 //				} else {
 //					log.write(("\n--- RESPONSE "+name+" ----------\n").getBytes());
 //					resp.toOutput(log);
 //					log.write(("--- END of RESPONSE "+name+" ---\n").getBytes());
 //				}
 //			} catch (Exception ex) {
 //				ex.printStackTrace();
 //			}
 //		}
 //	}
 	
 	public boolean isConnected() {
 		return (socket!=null && socket.isConnected() && secureConnectionEstablished && receiver!=null && receiver.isRunning());
 	}
 	
 
 	public void closeConnection() throws Exception {
		if (receiver!=null) {
			receiver.stop();
		}
 		if (socket != null) {
 			sendEncryptedText("Connection closed.");
 			socket.close();
 		}
 		secureConnectionEstablished = false;
 		mySigningKey = null;
 		//myEncryptionKey = null;
 		agreedEncryptionKey = null;
 		System.out.println("Connection closed.");
 	}
 	
 	public String getMessage() {
 		return message;
 	}
 	
 	public static void main(String[] args) {
 		OSDXSocket s = new OSDXSocket();
 		try {
 			
 			OSDXKey mysigning = OSDXKey.fromElement(Document.fromString("<?xml version=\"1.0\" encoding=\"UTF-8\"?>  <keypair><identities><identity><identnum>0001</identnum><email>test</email><mnemonic restricted=\"true\">test</mnemonic><sha256>2D:83:44:CA:3A:4C:85:3A:FB:E9:A3:15:D1:B4:70:BA:CC:7C:16:C7:DC:80:D9:AF:4F:E5:3D:74:4A:58:47:CE</sha256></identity></identities><sha1fingerprint>27:61:62:78:C1:29:F3:C6:A9:03:44:D2:18:36:37:22:E2:9F:63:BF</sha1fingerprint><authoritativekeyserver>localhost</authoritativekeyserver><datapath><step1><datasource>LOCAL</datasource><datainsertdatetime>2011-06-08 04:42:04 GMT+00:00</datainsertdatetime></step1></datapath><valid_from>2011-06-08 04:42:04 GMT+00:00</valid_from><valid_until>2036-06-07 10:42:04 GMT+00:00</valid_until><usage>ONLYSIGN</usage><level>MASTER</level><parentkeyid /><algo>RSA</algo><bits>3072</bits><modulus>00:88:FA:94:53:C5:EC:A9:31:63:FD:20:E3:38:A6:C8:B7:7F:32:41:4B:84:07:A1:AF:81:48:21:6F:D8:87:58:40:BF:DC:1A:E5:BD:A9:8E:ED:90:84:ED:84:BB:2E:04:FB:4F:33:F9:46:8B:0D:D0:58:F5:85:5C:F7:43:93:60:3A:BF:10:4B:92:65:DF:86:13:11:C0:6A:46:6F:4B:68:C3:5B:C3:48:BF:8E:16:00:36:68:A5:0E:C6:03:3B:87:7C:49:0C:18:FE:34:DA:78:03:F8:4B:B3:22:B9:D5:63:D8:74:B8:88:4C:E9:4D:A4:CE:A5:7C:09:B2:19:55:77:55:81:4C:FF:76:CD:87:69:B9:D4:B8:53:BE:9C:07:52:ED:53:09:D7:66:82:FC:A5:0A:79:2B:0D:06:5A:A7:76:77:F9:C4:27:B3:FD:BB:4A:80:44:8F:83:0F:DA:7E:A1:E7:22:24:D0:CC:EA:B2:F0:7F:03:BF:FC:FA:BB:B5:2D:17:63:40:1F:78:41:34:E4:ED:5A:F7:A2:1A:C5:75:FC:0F:93:44:95:AD:09:0D:10:90:D1:77:6B:D4:02:3C:8B:43:0B:91:3C:F5:F9:4A:94:0C:D4:EC:DD:2B:56:D4:AB:B9:C1:A4:74:AC:85:2E:6C:7C:AE:21:17:11:41:CB:9D:1C:16:98:1A:4F:03:8A:34:80:C5:2E:F6:E8:29:DB:3F:1C:EA:B1:B7:21:A9:5F:FA:93:D3:47:FA:DD:28:8F:4F:AA:53:1B:16:32:61:3B:B2:41:0E:37:DB:16:5B:14:AA:A9:D4:6C:C3:3E:0E:8D:90:B9:C4:83:C1:A6:6A:BF:E0:7F:56:AF:7D:7F:47:E5:4E:9C:8E:E9:E4:27:06:F9:0A:8A:22:7A:85:2D:FE:B2:AF:10:EC:5B:36:E7:96:60:E2:77:C5:9F:78:B9:51:A4:CE:7C:1D:D3:43:BF:4F:B1:2C:3F:DF:30:04:B6:7E:40:7E:F3:0E:F1:12:42:78:C6:4A:07</modulus><pubkey><exponent>01:00:01</exponent></pubkey><privkey><exponent><locked><mantraname>mantra: password</mantraname><algo>AES@256</algo><initvector>6F:67:A3:5C:C0:5A:67:F6:30:32:9A:0E:1E:3A:8B:1B</initvector><padding>CBC/PKCS#5</padding><bytes>12:9A:B4:1C:1F:8D:8B:88:39:CD:CD:C4:C9:4D:BE:65:56:2C:48:40:E8:3A:ED:09:F3:BF:0D:A7:A8:09:77:B7:C1:15:FB:8C:93:57:B6:38:F3:31:9B:A8:1E:21:27:40:9E:93:E9:4A:1F:B1:41:02:CF:40:96:2C:A6:17:2B:48:68:58:70:AD:B7:E1:52:6F:09:19:11:67:59:BC:1F:FD:BE:88:C0:B2:FF:76:34:EF:1B:26:DA:9F:4C:47:66:0E:87:BB:1C:09:CB:F2:77:BC:CB:AE:89:CA:C4:65:98:DA:D1:6E:ED:22:08:70:FC:BB:E2:CC:41:7F:5C:12:7B:A6:D3:32:73:FA:BB:E2:95:A6:1C:34:3E:FD:A8:90:D0:9A:0B:4E:96:06:89:DD:6F:35:02:E5:FB:CA:0A:E7:0D:2E:A1:B3:81:17:DE:8D:7F:96:F4:36:AA:02:4E:EF:C0:EF:56:37:C0:53:FB:B9:E3:C0:5B:69:9E:7C:EC:1F:A3:0B:C5:99:B7:5D:54:52:28:17:4A:B1:3D:C8:36:54:2A:94:0C:32:F7:1B:6A:11:37:91:B5:43:5D:BF:DB:6F:D3:B4:37:18:32:81:81:C1:72:80:B6:95:0E:B0:61:FF:05:CE:FC:98:E5:F1:E4:D1:33:B7:EF:B8:EB:EF:6B:A7:FE:C6:37:77:CF:43:12:C3:5F:2B:2A:51:19:E8:C4:6D:F6:0E:15:C4:C3:AD:BE:4C:FE:D6:D5:3A:00:D8:E0:0B:00:78:A9:5F:D8:21:28:06:B8:74:F2:06:23:63:81:B8:CC:03:EC:2C:ED:6B:74:23:E4:31:C1:4E:9C:B2:24:F0:93:A4:7D:6C:6A:E2:C1:95:EC:EA:DF:DC:85:2B:60:15:24:DE:FD:DD:94:BF:CF:C9:8D:74:DE:8A:D8:89:DC:16:FA:9D:28:37:EE:65:44:AD:61:FB:33:D4:E8:66:D3:BA:D8:38:E1:16:F1:EF:97:FF:01:D7:C3:7D:76:CE:A2:12:1C:24:AC:EC:AE:AC:2B:08:99:7A:D8:A4:41:49:A4:0E:AF:83</bytes></locked></exponent></privkey><gpgkeyserverid /></keypair>  ").getRootElement());
 			mysigning.unlockPrivateKey("password");			
 			String username = "testuser";
 			
 			s.connect("localhost", 4221,"/",mysigning);
 			
 			s.sendEncryptedText("ECHO MUHAHAHAHAHHH, NOBODY BUT THE SERVER WILL EVER READ THIS TEXT!!!");
 			s.sendEncryptedText("CWD test");
 			s.sendEncryptedText("CDUP");
 			
 			Thread.sleep(5000);
 			s.closeConnection();
 			
 			
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private String id = "[not set]";
 	public String getID() {
 		return id;
 	}
 	
 	public void setID(String id) {
 		this.id = id;
 	}
 	
 }
