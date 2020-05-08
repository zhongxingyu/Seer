 import java.security.*;
 
 import javax.crypto.*;
 import javax.crypto.spec.IvParameterSpec;
 
 import org.bouncycastle.util.encoders.Base64;
 
 public class EncryptionHandler {
 
     private Key key;
     private Mac hmac;
     private Cipher enccipher;
     private Cipher deccipher;
     final String B64 = "a-zA-Z0-9/+";
     
     //Initialize a msg verifyer
     public EncryptionHandler(Key secret, String Algorithm) throws NoSuchAlgorithmException, InvalidKeyException{
         key = secret;
         hmac= Mac.getInstance(Algorithm);
         hmac.init(key);
     }
     //initialize a cipher with an IV (i.e. AES in our case)
     public EncryptionHandler(Key secret, String Algorithm, byte[] IV) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException{
             enccipher = Cipher.getInstance(Algorithm);
             deccipher = Cipher.getInstance(Algorithm);
             IvParameterSpec ivp = new IvParameterSpec(IV);
             enccipher.init(Cipher.ENCRYPT_MODE, secret, ivp);
             deccipher.init(Cipher.DECRYPT_MODE, secret, ivp);
     }
     //init a chipher w/o iv but with 2 keys RSA-PSK in our case
     public EncryptionHandler(Key enc, Key dec, String Algorithm) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException{
         enccipher = Cipher.getInstance(Algorithm);
         deccipher = Cipher.getInstance(Algorithm);
         enccipher.init(Cipher.ENCRYPT_MODE, enc);
         deccipher.init(Cipher.DECRYPT_MODE, dec);
     }
     
     public String encryptMessage(String msg) throws IllegalBlockSizeException, BadPaddingException{
         //TODO rethink inner base 64 
         byte[] b64 = Base64.encode(msg.getBytes());
         byte[] cipher = enccipher.doFinal(b64);
         String cipherb64 = new String(Base64.encode(cipher));
         return cipherb64;
     }
     
     public String decryptMessage(String msg) throws IllegalBlockSizeException, BadPaddingException{
         byte[] cipher = Base64.decode(msg);
         byte[] clearbytes = deccipher.doFinal(cipher);
         String clear = new String(clearbytes);
         return clear;
     }
     
     public String encryptMessage(String[] msg) throws IllegalBlockSizeException, BadPaddingException{
        //base64 everything but the first part
         boolean first = true;
         
        int length = msg.length;
        for(int s = 1; s< length; s++){
        	//wenn der string noch nicht b64 ist b64 ihn
        	if(!msg[s].matches("["+B64+"]{43}=")){
        		msg[s] = new String(Base64.encode(msg[s].getBytes()));
        	}
        }
         //concat it in to one string
         String b64 = msg[0];
         for(String s : msg){
             if(!first){
                 b64 = b64.concat(" "+s);
             }
             else{
                 first = false;
             }
         }
         byte[] cipher = enccipher.doFinal(b64.getBytes());
         String cipherb64 = new String(Base64.encode(cipher));
         return cipherb64;
     }
         
     public String[] debaseAllButFirst(String[] msg){
         int length = msg.length;
         for(int s = 1; s < length ; s++){
                 msg[s] = new String(Base64.decode(msg[s]));
         }
         
         return msg;
     }
     
     public String debaseMassage(String msg){
         String ret = new String(Base64.decode(msg));
         return ret;
     }
     
     public byte[] generateIntegrityCheck(byte[] msg){
         hmac.update(msg);
         byte[] hash = hmac.doFinal();
         hmac.reset();
         return hash;
     }
     
     public boolean checkIntegrity(String message, byte[] rcvhash){
         byte[] msg = message.getBytes();
         hmac.update(msg);
         byte[] msghash = hmac.doFinal();
         hmac.reset();
         return MessageDigest.isEqual(rcvhash, msghash);
     }
 }
