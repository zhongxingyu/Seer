 package es.uc3m.setichat.utils;
 
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Random;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.IvParameterSpec;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.lang.ArrayUtils;
 
 import es.uc3m.setichat.utils.datamodel.Contact;
 
 public class SecurityModule {
 	
	private static final int RSA_LENGTH = 0;
	private static final int VECTOR_LENGTH = 0;
 	private final String PREFERENCES_FILE = "SeTiChat-Settings";
 	private final String SERVER_NAME = "setichat@appspot.com";
 	private Contact contact;
 	
 	public SecurityModule(Contact contact){
 		this.contact = contact;
 	}
 	
 	public Contact getContact() {
 		return contact;
 	}
 
 	public void setContact(Contact contact) {
 		this.contact = contact;
 	}
 
 	
 	
 	public byte[] encrypt(String content){
 		// Gen random key
 		byte [] k = new byte[16];
 		new Random().nextBytes(k);
 		
 		byte [] c = content.getBytes();
 		byte [] encryptedMessage = null;
 		
 		// Cipher initialization
 		Cipher cipher = null;
 		SecretKeySpec aeskeySpec = new SecretKeySpec(k, "AES");
 		
 		try {
 			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
 			cipher.init(Cipher.ENCRYPT_MODE, aeskeySpec);
 			encryptedMessage = cipher.doFinal(c);
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		// Encrypt key
 		byte [] RSA = encryptRSA(k);
 		byte [] xml = ArrayUtils.addAll(ArrayUtils.addAll(RSA, cipher.getIV()), encryptedMessage);
 		
 		return xml;
 	}
 	
 	private byte[] encryptRSA(byte[] key){
 		byte[] keyContact = getContact().getPublicKey();
 		byte [] result = null;
 		Cipher publicKeyCipher = null;
 		SecretKeySpec rsakeySpec = new SecretKeySpec(keyContact, "RSA");
 		try {
 			publicKeyCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
 			publicKeyCipher.init(Cipher.ENCRYPT_MODE, rsakeySpec);
 			result = publicKeyCipher.doFinal(key);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	public String decrypt (String content, String privateKey){
 		byte [] bContent = content.getBytes();
		
		//TODO: Inicializar RSA_LENGTH y VECTOR_LENGTH con los valores correspondientes
 		byte [] rsaKey = new byte [RSA_LENGTH];
 		byte [] iv = new byte [VECTOR_LENGTH];
 		ArrayList <Byte> aesContent = new ArrayList();
 		// Extract contents in different byte streams
 		for(int i = 0; i < RSA_LENGTH; i++){
 			rsaKey[i] = bContent[i];
 		}
 		
 		for(int i = RSA_LENGTH; i < RSA_LENGTH + VECTOR_LENGTH; i++){
 			iv[i] = bContent[i];
 		}
 		
 		for(int i = RSA_LENGTH + VECTOR_LENGTH; i < bContent.length; i++){
 			aesContent.add(bContent[i]);
 		}
 		
 		// Start decryption
 		byte [] aesDecryptedKey = decryptRSA(privateKey, iv, rsaKey);
 		
 		return "";
 	}
 	
 	private byte[] decryptRSA(String privateKey, byte[] iv, byte[]rsaKey){
 		byte[] pKey = privateKey.getBytes();
 		byte [] result = null;
 		Cipher cipher = null;
 		SecretKeySpec rsakeySpec = new SecretKeySpec(pKey, "RSA");
 		try {
 			cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
 			cipher.init(Cipher.DECRYPT_MODE, rsakeySpec, new IvParameterSpec(iv));
 			result = cipher.doFinal(rsaKey);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalBlockSizeException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (BadPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InvalidAlgorithmParameterException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return result;
 	}
 }
