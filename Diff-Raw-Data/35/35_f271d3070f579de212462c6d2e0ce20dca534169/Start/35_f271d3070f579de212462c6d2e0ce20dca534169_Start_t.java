 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.KeyStore;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.cert.X509Certificate;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.Hashtable;
 
 import javax.crypto.Cipher;
 import javax.crypto.KeyAgreement;
 import javax.crypto.SealedObject;
 import javax.crypto.SecretKey;
 import javax.crypto.SecretKeyFactory;
 import javax.crypto.spec.DESedeKeySpec;
 
 public class Start {
 
 	private static char[] serverPassword = "ece6102server".toCharArray();
 	private static String keystoreFile = "server.jks";
 	private static String serverAlias = "server";
 	private static PublicKey pubKey;
 	private static PrivateKey privKey;
 	private static Hashtable<String, SecretKey> privatekeys = new Hashtable<String,SecretKey>();
 	private static SecretKey caSecret;
 	private static SealedObject so;
 	private static X509Certificate serverCert;
 	private static ServerSocket serverSocket;
 	private static Socket socket,caSocket;
 	private static FileOutputStream ksos = null;
 	private ObjectOutputStream caOut;
 	private ObjectInputStream caIn;
 	private static Cipher ecipher = null;
 	private static Cipher dcipher = null;
 	
 	
 	public void GetKeys() throws Exception
 	{
 		FileInputStream input = new FileInputStream(keystoreFile);
 	    KeyStore keyStore = KeyStore.getInstance("JKS");
 	    //KeyStore ks = KeyStore.getInstance("JCEKS");
 	    keyStore.load(input, serverPassword);
 	    input.close();
 	    privKey = (PrivateKey) keyStore.getKey(serverAlias, serverPassword);
 	    serverCert = (X509Certificate)keyStore.getCertificate(serverAlias);
 	    pubKey = serverCert.getPublicKey();
 	}
 	public void listen()
 	{
 		try{
 		serverSocket = new ServerSocket(10001,20);
 		GetKeys();    
 		ksos = new FileOutputStream("keystoreAuthenticateFile");
 		KeyStore ks = KeyStore.getInstance("JCEKS");
 		
 		ObjectOutputStream out = null;
 		ObjectInputStream in = null;
 		socket = serverSocket.accept();
 		
 		X509Certificate cert = null;
 		while(socket != null)
 	    {	    	
 	    		synchronized(this)
 	    		{
 	    			out = new ObjectOutputStream(socket.getOutputStream());
 	    			in = new ObjectInputStream(socket.getInputStream());
 	    			
 	    			Hashtable request = new Hashtable();
 	    			  request = (Hashtable)in.readObject();
 	    			  cert = (X509Certificate)request.get("cert");
 	    			if(request.containsKey("authenticate"))
 	    			  {
 	    				byte[] clientKey = (byte[])request.get("authenticate");
 	    				cert = (X509Certificate)request.get("cert");
 	    				KeyPairGenerator keyGen = KeyPairGenerator.getInstance("DH");
 	    			    keyGen.initialize(Skip.sDHParameterSpec);
 	    			    KeyPair   keypair = keyGen.genKeyPair();
 	    			 
 	    		    // Get the generated public and private keys
 	    		       PrivateKey diffiePriv = keypair.getPrivate();
 	    		       PublicKey  diffiePub = keypair.getPublic();
 	    		       caSocket = new Socket("127.0.0.1",2358);
 	    		       caOut = new ObjectOutputStream(caSocket.getOutputStream());
 	    		       caIn = new ObjectInputStream(caSocket.getInputStream());
 
 	    				Hashtable table = new Hashtable();
 	    				table.put("authenticate", diffiePub.getEncoded());
 	    				table.put("cert", serverCert);
 	    				caOut.writeObject(table);
 	    				caOut.flush();
 	    				table = (Hashtable)caIn.readObject();
 	 				   if(table.containsKey("authenticateResponse"))
 	 				   {
 	 					  byte[] caKey = (byte[])table.get("authenticateResponse");
 	 					  caSecret = InitiateProcess(caKey,diffiePriv,diffiePub);
 	 					  Hashtable hsend = new Hashtable();
 							hsend.put("queryId", 2);
 							hsend.put("cert",cert);
 
 							ecipher = Cipher.getInstance("DES");
 							ecipher.init(Cipher.ENCRYPT_MODE, caSecret);
 							
 							so = new SealedObject(hsend, ecipher);
 							
 							request = new Hashtable();
 					        request.put("message", so);
 					        request.put("cert", serverCert);
 					        caOut.writeObject(request);
 					        caOut.flush();
 					        
 					        
 					        hsend = (Hashtable)caIn.readObject();
 					        dcipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
 					        dcipher.init(Cipher.DECRYPT_MODE, caSecret);
 					        so = (SealedObject)hsend.get("response");
 					        
 					        cert = (X509Certificate)so.getObject(dcipher);
 					        caOut.flush();
 					        caOut.close();
 					        caIn.close();
 					        if(cert == null)
 					        {
 					        	out.close();
 					        	in.close();
 					        }
 					        else{
 	    				   keypair = keyGen.genKeyPair();
 
 	    				    // Get the generated public and private keys
 	    				    diffiePriv = keypair.getPrivate();
 	    				    diffiePub = keypair.getPublic();
 	    			      SecretKey key = InitiateProcess(clientKey,diffiePriv,diffiePub);
 	    			      privatekeys.put(new String(cert.getPublicKey().getEncoded(),"UTF-8"), key);
 	    			         table = new Hashtable<String,byte[]>();
 	    			         table.put("authenticateResponse", diffiePub.getEncoded());
 	    			         table.put("cert", serverCert);
 	    			         out.writeObject(table);
 	    			         out.flush();
 
 					        }
 	    			  } 
 	    		}
 	    			else
 	    			{
 	    				 if(request.containsKey("message"))
 	     				  {
 	    					 FileInputStream input = new FileInputStream(keystoreFile);
 			          	      KeyStore keyStore = KeyStore.getInstance("JCEKS");
 			          	      keyStore.load(input, serverPassword);
 			          	      input.close();
 	  					  
 	  					  Cipher dcipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
 	  					  SecretKey key = privatekeys.get(new String(cert.getPublicKey().getEncoded(),"UTF-8"));
 	  					  
 	  					if(key != null)
 	  					  {
 	  			              dcipher.init(Cipher.DECRYPT_MODE, key);
 	  			              SealedObject so = (SealedObject)request.get("message"); 
 	  			              Hashtable process = new Hashtable();
 	  			              process = (Hashtable)so.getObject(dcipher);		              
 				          	  Cipher ecipher = Cipher.getInstance("DES");
 		    			      ecipher.init(Cipher.ENCRYPT_MODE, key);
 		    			      Hashtable hsend = new Hashtable();
 		    			      Hashtable response = new Hashtable();
 		    			      if(process.containsKey("terminate"))
 				    		  {
 				        		 //keyStore.deleteEntry(cert);
 		    			    	  privatekeys.remove(key);
 				    			 String status = "1";
 				    			 byte[] enc = ecipher.doFinal(status.getBytes("UTF-8"));
 				    			 response.put("responseCode", enc);
 				    		  }
 		    			        
 		    			      else if(process.containsKey("data"))
 					        	  {
 					        		  Hashtable data = Server.processRequest((Hashtable)process.get("data"));
 					        		  
 					        		  if(data != null)
 					        		  {
 					        		     response.put("data", data);
 					        		     hsend.put("responseCode", 1);
 					        		  }
 					        		  else
 					        		  {
 						        		  hsend.put("responseCode", 99);
 					        		  }
 					        	  }
 		    			       hsend.put("cert", serverCert);
 		    			       ecipher = Cipher.getInstance("DES");
 								ecipher.init(Cipher.ENCRYPT_MODE, key);
 								
 								so = new SealedObject(response, ecipher);
 								
 								hsend.put("message", so);
 						        out.flush();
 						        out.writeObject(hsend);
 	  					  }
 	     				  }
 	    			}
	    			socket.close();
 	    			socket = serverSocket.accept();
             		//out = new ObjectOutputStream(socket.getOutputStream());
             		//in = new ObjectInputStream(socket.getInputStream());
 		}}}
 		catch(Exception ex)
 		{
 			try {
 				caSocket.close();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			System.out.println(ex.toString());
 		}
 		
 	}
 	public static SecretKey InitiateProcess(byte[] key, PrivateKey diffiePriv, PublicKey diffiePub)
     {
     	
 	try {
 		
        
 	    // Get the generated public and private keys
 		FileInputStream input = new FileInputStream(keystoreFile);
 	    KeyStore keyStore = KeyStore.getInstance("JKS");
 	    keyStore.load(input, serverPassword);
 
 	 // Get public key
         //publicKey = caCert.getPublicKey();
         Hashtable<String,byte[]> response = new Hashtable<String,byte[]>();
         
 	    // Retrieve the public key bytes of the other party
         byte[] publicKeyBytes = key;
 
 	    // Convert the public key bytes into a PublicKey object
 	    X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(publicKeyBytes);
 	    //KeyFactory keyFact = KeyFactory.getInstance("DH");
 	    KeyFactory keyFact = KeyFactory.getInstance("DH");
 	    diffiePub = keyFact.generatePublic(x509KeySpec);
 
 	    // Prepare to generate the secret key with the private key and public key of the other party
 	    KeyAgreement ka = KeyAgreement.getInstance("DH");
 	    ka.init(diffiePriv);
 	    ka.doPhase(diffiePub, true);
 
 	    // Specify the type of key to generate;
 	    // see Listing All Available Symmetric Key Generators
 	    String algorithm = "DES";
 
 	    // Generate the secret key
 	    SecretKey secretKey = ka.generateSecret(algorithm);
         return secretKey;
 	    
 	} 
 	catch (java.security.InvalidKeyException e) {
 		return null;
 	} 
 	catch (java.security.spec.InvalidKeySpecException e) {
 		System.out.println(e.getMessage());
 		return null;
 	} 
 
 	catch (java.security.NoSuchAlgorithmException e) {
 		return null;
 	}
 	catch(Exception evt)
 	{
 		return null;
 	}
     }
 	
 	public static void main(String args[])
 	{
 		new Start().listen();
 	}
 }
