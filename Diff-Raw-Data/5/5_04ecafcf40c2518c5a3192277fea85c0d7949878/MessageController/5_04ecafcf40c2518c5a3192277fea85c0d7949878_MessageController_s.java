 package main.java.Client;
 
 import main.java.RSAEngine.Crypter;
 import main.java.CryptoEngine.Signature;
 
 import java.math.BigInteger;
 
 import java.security.NoSuchAlgorithmException;
 import java.security.GeneralSecurityException;
 import java.security.InvalidKeyException;
 
 import javax.crypto.*;
 import javax.crypto.spec.*;
 
 import org.json.JSONObject;
 import org.json.JSONException;
 
 public class MessageController {
 
    public static boolean processMessage(JSONObject message) {
  		/* Setup 
 		/* A message is invalid if:
 		 * 1) it is not intended for you
 		 * 2) invalid AES key
 		 * 3) for wall posts, the sender is not in your list of friends
 		 *    (ie check the signature in the message)
 		 * 4) invalid message signature (must decrypt the aes key first)
 		 * 5) unrecognized command
 		*/
 		try {
 			// Check 1
 			if (message.get("to_user") != DataController.getUsername()){
 				return false;
 			}
 		
 			// Setup parts for decryption
 			Crypter c = new Crypter();
 			JSONObject cert = new JSONObject(DataController.getCertificate());
 			BigInteger n = new BigInteger((String) cert.get("modulus"), 16);
 			BigInteger d = DataController.getPrivateKey();
 
 			String eak_string = (String) message.get("encrypted_aes_key");
 			String eak = new String(new BigInteger(eak_string, 16).toByteArray());
 		
 			// Decrypt the aes key
 			String aes_key_string = c.RSADecrypt(eak, d, n);
 			BigInteger aes_key_int = new BigInteger(aes_key_string, 16);
 		
 			// Check 2
 			if (aes_key_int.bitLength() != 128) {
 				return false;
 			}
 		
 			// Set up AES decryption
 			Cipher aesCipher;
 			try {
 				aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
 			} catch (NoSuchAlgorithmException e) {
 				return false;
 			} catch (NoSuchPaddingException e) {
 				return false;
 			}
 			SecretKeySpec aesKeyObj = new SecretKeySpec(aes_key_int.toByteArray(), "AES");
 			
 			try {
 				aesCipher.init(aesCipher.DECRYPT_MODE, aesKeyObj);
 			} catch (InvalidKeyException e) {
 				return false;
 			}
 		
 			// Decrypt the payload
 			byte[] encrypted_signature = new BigInteger((String) message.get("encrypted_signature"), 16).toByteArray();
 			String raw_signature;
 			
 			try {
 				raw_signature = new String(aesCipher.doFinal(encrypted_signature));
 			} catch (IllegalBlockSizeException e) {
 				return false;
 			} catch (BadPaddingException e) {
 				return false;
 			}
 		
 			// Check 3
 			JSONObject signature = new JSONObject(raw_signature);
 			if (!DataController.isFriend((String) signature.get("username"))) {
 				return false;
 			}
 		
 			// Grab the message
 			JSONObject payload = new JSONObject(signature.get("message"));
 			String commandType = (String) payload.get("command_type");
 			
 			// Process the signature's message:
 			if (commandType == "wall_post") {
 				DataController.addWallPost((String) signature.get("username"), (String) payload.get("command_args"));
 				return true;
 			} else if (commandType == "friend_request") {
 				// Setup up signature verification
 				JSONObject friend_signed_cert = new JSONObject(payload.get("command_args"));
 				JSONObject friend_unsigned_cert = new JSONObject(friend_signed_cert.get("message"));
 				Signature s = new Signature();
 		
 				// Check 4
 				boolean isVerified = s.verifySignature((String) payload.get("command_args"), new BigInteger((String) friend_unsigned_cert.get("public_exponent"), 16), new BigInteger((String) friend_unsigned_cert.get("modulus"), 16));
 				if (!isVerified) {
 					return false;
 				} else {
 					DataController.addFriend((String) friend_unsigned_cert.get("realname"), (String) friend_unsigned_cert.get("username"), friend_unsigned_cert);
 					return true;
 				}
 			} else {
 				// Check 5
 				return false;
 			}
			
			return false;
 		} catch (JSONException e) {
 			return false;
 		}
     }
 
 	public static String createMessage(String recipient, String cmd, String cmdargs){
 		Crypter a = new Crypter();					//Init crypter
 		
 		if(!DataController.isFriend(recipient)){
 			System.out.println(recipient + " IS NOT A FRIEND");		//check if user is friend
 			return null;
 		}
 		
 		JSONObject sigmsg = new JSONObject();		//build signature's massage
 		try {
 			sigmsg.put("command_type", cmd);
 			sigmsg.put("command_args", cmdargs);
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
 		JSONObject message = new JSONObject();
             /*
 		try {
 			Signature s = new Signature();
 			BigInteger[] key = DataController.getPrivateKey();
 			String signature = s.generateSignature("bolotov", sigmsg.toString(), key[0], key[1]);			//build signature
 		
 			KeyGenerator kgen = KeyGenerator.getInstance("AES");				//AES init, session key generation
 			kgen.init(256);
 			SecretKey skey = kgen.generateKey();
 		    byte[] raw = skey.getEncoded();
 		    SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
 		    Cipher cipher = Cipher.getInstance("AES");
 		    cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
 	    
 		    byte[] encsig = cipher.doFinal(signature.getBytes());				//encrypt sig with AES
 	    
 		    JSONObject recipientCert = new JSONObject(DataController.getCertificate(recipient));
 		    BigInteger hise = new BigInteger(recipientCert.getString("public_exponent"), 16);
 	    	BigInteger hisn = new BigInteger(recipientCert.getString("modulus"), 16);
 	    	String keystr = new String(skeySpec.getEncoded());
 	    	String enckeystr = a.RSAESPKCS1Encrypt(keystr, hise, hisn);			//encrypt session key
 	    
 	    	message = new JSONObject();								//build message
 	    	message.put("to_user", recipient);
 	    	message.put("encrypted_aes_key", new BigInteger(1, enckeystr.getBytes()).toString(16));
 	    	message.put("encrypted_signature", new BigInteger(1, encsig).toString(16));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (GeneralSecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
         }
             */
 		return message.toString();
 	}
 
 }
