 /* File worker thread handles the business of uploading, downloading, and removing files 
  * for clients with valid tokens
  */
 
 import java.lang.Thread;
 import java.net.Socket;
 import java.security.KeyFactory;
 import java.security.PublicKey;
 import java.security.spec.X509EncodedKeySpec;
 import java.util.ArrayList;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 public class FileThread extends Thread
 {
 	private final Socket socket;
 	private FileServer my_fs;
 	private SecretKey sessionKey;
 	private PublicKey groupKey;
 
 	public FileThread(Socket _socket, FileServer fs)
 	{
 		socket = _socket;
 		my_fs = fs;
 	}
 
 	public void run()
 	{
 		boolean proceed = true;
 		try
 		{
 			System.out.println("*** New connection from " + socket.getInetAddress() + ":" + socket.getPort() + "***");
 			final ObjectInputStream input = new ObjectInputStream(socket.getInputStream());
 			final ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
 			Envelope response;
 
 			Cipher rsaCipher = Cipher.getInstance("RSA");
 			Cipher aesCipher = Cipher.getInstance("AES");
 			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
 			byte[] decrypted;
 			byte[] encrypted;
 
 			do
 			{				
 				Envelope e = (Envelope)input.readObject();
 				System.out.println("Request received: " + e.getMessage());
 
 				if(e.getMessage().equals("CONNECT"))//Client wants a token
 				{
 					// alert the user of the file servers public key on connect
 					response = new Envelope("PUBLICKEY");
 					response.addObject(my_fs.RSAkeys.getPublic());
 					output.writeObject(response);
 				}
 				else if (e.getMessage().equals("SESSIONKEY"))
 				{
 					// initialize your cipher
 					rsaCipher.init(Cipher.DECRYPT_MODE, my_fs.RSAkeys.getPrivate());
 					decrypted = rsaCipher.doFinal((byte[]) e.getObjContents().get(0));
 
 					// return the random number to the client
 					response = new Envelope("AUTHVALUE");
 					response.addObject(decrypted);
 					output.writeObject(response);
 
 					decrypted = rsaCipher.doFinal((byte[]) e.getObjContents().get(1));
 
 					sessionKey = new SecretKeySpec(decrypted, "AES");
 				}
 				else if(e.getMessage().equals("GROUPKEY"))
 				{
 					aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
 					decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(0));
 					
 					groupKey = keyFactory.generatePublic(new X509EncodedKeySpec(decrypted));
 					
 					response = new Envelope("OK");
 					output.writeObject(response);
 				}
 				// Handler to list files that this user is allowed to see
 				else if(e.getMessage().equals("LFILES"))
 				{
 					aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
 					decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(0));
 					
 					rsaCipher.init(Cipher.DECRYPT_MODE, groupKey);
 					decrypted = rsaCipher.doFinal(decrypted);
 
 					String tokenParts = new String(decrypted);
 					UserToken t = new Token(tokenParts); //Extract the token
 
 					ArrayList<String> groups = new ArrayList<String>(t.getGroups());
 
 					aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey);
 
 					response = new Envelope("OK");
 
 					for (ShareFile f : FileServer.fileList.getFiles())
 					{
 						for (String g : groups)
 						{
 							if (f.getGroup().equals(g))
 							{
 								encrypted = aesCipher.doFinal(f.getPath().getBytes());
 								response.addObject(encrypted);
 							}
 						}
 					}
 
 					output.writeObject(response);
 				}
 				else if(e.getMessage().equals("UPLOADF"))
 				{
 
 					if(e.getObjContents().size() < 3)
 					{
 						response = new Envelope("FAIL-BADCONTENTS");
 					}
 					else
 					{
 						if(e.getObjContents().get(0) == null) {
 							response = new Envelope("FAIL-BADPATH");
 						}
 						if(e.getObjContents().get(1) == null) {
 							response = new Envelope("FAIL-BADGROUP");
 						}
 						if(e.getObjContents().get(2) == null) {
 							response = new Envelope("FAIL-BADTOKEN");
 						}
 						else {
 
 							aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
 
 							decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(0));
 							String remotePath = new String(decrypted); // Extract the remotePath
 
 							decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(1));
 							String group = new String(decrypted); // Extract the group
 
 							decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(2));
 							rsaCipher.init(Cipher.DECRYPT_MODE, groupKey);
 							decrypted = rsaCipher.doFinal(decrypted);
 
 							String tokenParts = new String(decrypted);
 							UserToken yourToken = new Token(tokenParts); //Extract the token
 
 							if (FileServer.fileList.checkFile(remotePath)) {
 								System.out.printf("Error: file already exists at %s\n", remotePath);
 								response = new Envelope("FAIL-FILEEXISTS"); //Success
 							}
 							else if (!yourToken.getGroups().contains(group)) {
 								System.out.printf("Error: user missing valid token for group %s\n", group);
 								response = new Envelope("FAIL-UNAUTHORIZED"); //Success
 							}
 							else  {
 								File file = new File("shared_files/"+remotePath.replace('/', '_'));
 								file.createNewFile();
 								FileOutputStream fos = new FileOutputStream(file);
 								System.out.printf("Successfully created file %s\n", remotePath.replace('/', '_'));
 
 								response = new Envelope("READY"); //Success
 								output.writeObject(response);
 
 								e = (Envelope)input.readObject();
 								while (e.getMessage().compareTo("CHUNK")==0) {
 									decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(0));
 									fos.write(decrypted, 0, (Integer)e.getObjContents().get(1));
 									response = new Envelope("READY"); //Success
 									output.writeObject(response);
 									e = (Envelope)input.readObject();
 								}
 
 								if(e.getMessage().compareTo("EOF")==0) {
 									System.out.printf("Transfer successful file %s\n", remotePath);
 									FileServer.fileList.addFile(yourToken.getSubject(), group, remotePath);
 									response = new Envelope("OK"); //Success
 								}
 								else {
 									System.out.printf("Error reading file %s from client\n", remotePath);
 									response = new Envelope("ERROR-TRANSFER"); //Success
 								}
 								fos.close();
 							}
 						}
 					}
 
 					output.writeObject(response);
 				}
 				else if (e.getMessage().compareTo("DOWNLOADF")==0) {
 
 					aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
 
 					decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(0));
 					String remotePath = new String(decrypted); // Extract the remotePath
 
 					decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(1));
 					rsaCipher.init(Cipher.DECRYPT_MODE, groupKey);
 					decrypted = rsaCipher.doFinal(decrypted);
 
 					String tokenParts = new String(decrypted);
 					UserToken t = new Token(tokenParts); //Extract the token
 
 					ShareFile sf = FileServer.fileList.getFile("/"+remotePath);
 					if (sf == null) {
 						System.out.printf("Error: File %s doesn't exist\n", remotePath);
 						e = new Envelope("ERROR_FILEMISSING");
 						output.writeObject(e);
 
 					}
 					else if (!t.getGroups().contains(sf.getGroup())){
 						System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
 						e = new Envelope("ERROR_PERMISSION");
 						output.writeObject(e);
 					}
 					else {
 
 						try
 						{
 							File f = new File("shared_files/_"+remotePath.replace('/', '_'));
 							if (!f.exists()) {
 								System.out.printf("Error file %s missing from disk\n", "_"+remotePath.replace('/', '_'));
 								e = new Envelope("ERROR_NOTONDISK");
 								output.writeObject(e);
 
 							}
 							else {
 								FileInputStream fis = new FileInputStream(f);
 
								aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
 
 								do {
 									byte[] buf = new byte[4096];
 									if (e.getMessage().compareTo("DOWNLOADF")!=0) {
 										System.out.printf("Server error: %s\n", e.getMessage());
 										break;
 									}
 									e = new Envelope("CHUNK");
 									int n = fis.read(buf); //can throw an IOException
 									if (n > 0) {
 										System.out.printf(".");
 									} else if (n < 0) {
 										System.out.println("Read error");
 
 									}
 
 									encrypted = aesCipher.doFinal(buf);
 
 									e.addObject(encrypted);
 									e.addObject(new Integer(n));
 
 									output.writeObject(e);
 
 									e = (Envelope)input.readObject();


								}
								while (fis.available()>0);
 
 								fis.close();
 
 								//If server indicates success, return the member list
 								if(e.getMessage().compareTo("DOWNLOADF")==0)
 								{
 
 									e = new Envelope("EOF");
 									output.writeObject(e);
 
 									e = (Envelope)input.readObject();
 									if(e.getMessage().compareTo("OK")==0) {
 										System.out.printf("File data upload successful\n");
 									}
 									else {
 
 										System.out.printf("Upload failed: %s\n", e.getMessage());
 
 									}
 
 								}
 								else {
 
 									System.out.printf("Upload failed: %s\n", e.getMessage());
 
 								}
 							}
 						}
 						catch(Exception e1)
 						{
 							System.err.println("Error: " + e.getMessage());
 							e1.printStackTrace(System.err);
 
 						}
 					}
 				}
 				else if (e.getMessage().compareTo("DELETEF")==0) {
 
 					aesCipher.init(Cipher.DECRYPT_MODE, sessionKey);
 
 					decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(0));
 					String remotePath = new String(decrypted); // Extract the remotePath
 
 					decrypted = aesCipher.doFinal((byte[]) e.getObjContents().get(1));
 					rsaCipher.init(Cipher.DECRYPT_MODE, groupKey);
 					decrypted = rsaCipher.doFinal(decrypted);
 
 					String tokenParts = new String(decrypted);
 					UserToken t = new Token(tokenParts); //Extract the token
 
 					ShareFile sf = FileServer.fileList.getFile("/"+remotePath);
 					if (sf == null) {
 						System.out.printf("Error: File %s doesn't exist\n", remotePath);
 						e = new Envelope("ERROR_DOESNTEXIST");
 					}
 					else if (!t.getGroups().contains(sf.getGroup())){
 						System.out.printf("Error user %s doesn't have permission\n", t.getSubject());
 						e = new Envelope("ERROR_PERMISSION");
 					}
 					else {
 
 						try
 						{
 							File f = new File("shared_files/"+"_"+remotePath.replace('/', '_'));
 
 							if (!f.exists()) {
 								System.out.printf("Error file %s missing from disk\n", "_"+remotePath.replace('/', '_'));
 								e = new Envelope("ERROR_FILEMISSING");
 							}
 							else if (f.delete()) {
 								System.out.printf("File %s deleted from disk\n", "_"+remotePath.replace('/', '_'));
 								FileServer.fileList.removeFile("/"+remotePath);
 								e = new Envelope("OK");
 							}
 							else {
 								System.out.printf("Error deleting file %s from disk\n", "_"+remotePath.replace('/', '_'));
 								e = new Envelope("ERROR_DELETE");
 							}
 
 
 						}
 						catch(Exception e1)
 						{
 							System.err.println("Error: " + e1.getMessage());
 							e1.printStackTrace(System.err);
 							e = new Envelope(e1.getMessage());
 						}
 					}
 					output.writeObject(e);
 
 				}
 				else if(e.getMessage().equals("DISCONNECT"))
 				{
 					socket.close();
 					proceed = false;
 				}
 			} while(proceed);
 		}
 		catch(Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 			e.printStackTrace(System.err);
 		}
 	}
 
 }
