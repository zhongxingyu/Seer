 package repo;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.security.NoSuchProviderException;
 import java.security.Security;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.ShortBufferException;
 import javax.crypto.spec.SecretKeySpec;
 
 /**
  * Class to encrypt stuff
  *
  */
 public class Crypto {
     
 	private byte[] keyBytes;
     private SecretKeySpec key;
     
     private Cipher cipher;
     
     public Crypto(){
    	Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    	
     	keyBytes = new byte[] { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09,
                 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
     	key = new SecretKeySpec(keyBytes, "AES");
     	
     	try {
			cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace(System.out);
 		}
     }
     
     
     public String encrypt(String inputString){
     	try {
 			byte[] input = inputString.getBytes();
 			// encryption pass
 			cipher.init(Cipher.ENCRYPT_MODE, key);
 			byte[] cipherText = new byte[cipher.getOutputSize(input.length)];
 			int ctLength = cipher.update(input, 0, input.length, cipherText, 0);
 			ctLength += cipher.doFinal(cipherText, ctLength);
 			System.out.println("Encrypted "+inputString+" to "+new String(cipherText));
 			return new String(cipherText);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace(System.out);
 			return inputString;
 		}
     	
     }
     
     public String decrypt(String inputString){
     	try {
 			byte[] cipherText = inputString.getBytes();
 			int ctLength = cipherText.length;
 			// decryption pass
 			cipher.init(Cipher.DECRYPT_MODE, key);
 			byte[] plainText = new byte[cipher.getOutputSize(ctLength)];
 			int ptLength = cipher.update(cipherText, 0, ctLength, plainText, 0);
 			ptLength += cipher.doFinal(plainText, ptLength);
 			System.out.println("Decrypted "+inputString+" to "+new String(plainText));
 			return new String(plainText);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace(System.out);
 			return inputString;
 		}
     }
     
   
 }
