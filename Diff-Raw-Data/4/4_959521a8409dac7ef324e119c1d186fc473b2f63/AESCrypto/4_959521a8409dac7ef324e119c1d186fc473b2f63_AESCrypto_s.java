 package com.aes;
 
 import org.apache.commons.codec.binary.Base64;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.InvalidKeyException;
 import java.security.KeyStore;
 import java.security.KeyStore.SecretKeyEntry;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.UnrecoverableEntryException;
 import java.security.cert.CertificateException;
 
 /**
  * keytool command.
  * keytool -genseckey -keyalg AES -alias myseckey -keysize 256 -keypass
  * mykeypass -storetype jceks -keystore mystore.jck -storepass mystorepass
  */
 
 public class AESCrypto {
 
     private static final String TYPE_OF_KEYSTORE = "JCEKS";
     private static final String PATH_TO_KEYSTORE = "src/main/resources/mystore.jck";
     private static final String KEY_ALIAS = "myseckey";
     private static final String ALIAS_PASSWORD = "mykeypass";
     private static final String STORE_PASSWORD = "mystorepass";
     private static final String SECRET_TEXT = "My Secret Text";
     private static final String UNICODE_FORMAT  = "UTF8";
     private static final String AES  = "AES";
 
     /**
      * @param typeKeyStore
      * @param pathToKeyStore
      * @param storePassword
      * @param aliasPassword
      * @param keyAlias
      * @throws java.io.FileNotFoundException
      * @throws java.security.cert.CertificateException
      *
      * @throws java.security.UnrecoverableEntryException
      *
      */
 
     public SecretKey getSecretKey(final String typeKeyStore,
                                   final String pathToKeyStore,
                                   final String storePassword,
                                   final String keyAlias,
                                   final String aliasPassword) throws KeyStoreException,
             IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableEntryException {
 
         KeyStore ks = KeyStore.getInstance(typeKeyStore);
         ks.load(new FileInputStream(pathToKeyStore),
                 storePassword.toCharArray());
 
         SecretKeyEntry entry = (SecretKeyEntry) ks.getEntry(
                 keyAlias,
                 new KeyStore.PasswordProtection(aliasPassword
                         .toCharArray()));
         return entry.getSecretKey();
     }
 
     private static byte[] decryptText(final Cipher desCipher, final SecretKey myDesKey, final String textEncrypted)
             throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
 
         // Switch the cipher to decryption
         desCipher.init(Cipher.DECRYPT_MODE, myDesKey);
         byte[] decodedValue = Base64.decodeBase64(textEncrypted);
 
         // Decrypt the text
         return desCipher.doFinal(decodedValue);
     }
 
     private static byte[] encryptText(final Cipher desCipher, final SecretKey myDesKey, final String secretText)
             throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
         // Initialize the cipher for encryption
         desCipher.init(Cipher.ENCRYPT_MODE, myDesKey);
         // Encrypt the text
 
         return desCipher.doFinal(secretText.getBytes(UNICODE_FORMAT));
     }
 
     public static void main(String[] argv) {
 
         try {
 
             SecretKey myDesKey = new AESCrypto().getSecretKey(
                     TYPE_OF_KEYSTORE,
                     PATH_TO_KEYSTORE,
                     STORE_PASSWORD,
                     KEY_ALIAS,
                     ALIAS_PASSWORD);
 
             Cipher desCipher;
 
             // Create the cipher with AES algorithm
             desCipher = Cipher.getInstance(AES);
 
             // secret text
             byte[] text = SECRET_TEXT.getBytes();
 
             System.out.println("Text [Byte Format] : " + text);
             System.out.println("Text : " + new String(text));
 
             // Encrypt the text
 //
             byte[] textEncrypted = encryptText(desCipher, myDesKey, SECRET_TEXT);
 
             System.out.println("Text Encrypted : " + textEncrypted);
             String encodeBase64String = Base64.encodeBase64String(textEncrypted);
            System.out.println("String Encrypted : " +  encodeBase64String);
 
             // Decrypt the text
 
             byte[] textDecrypted =  decryptText(desCipher, myDesKey, encodeBase64String);
 
             System.out.println("Text Decrypted : " + new String(textDecrypted));
 
         } catch (NoSuchAlgorithmException e) {
             e.printStackTrace();
         } catch (NoSuchPaddingException e) {
             e.printStackTrace();
         } catch (InvalidKeyException e) {
             e.printStackTrace();
         } catch (IllegalBlockSizeException e) {
             e.printStackTrace();
         } catch (BadPaddingException e) {
             e.printStackTrace();
         } catch (KeyStoreException e) {
             e.printStackTrace();
         } catch (CertificateException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } catch (UnrecoverableEntryException e) {
             e.printStackTrace();
         }
     }
 
 
 
 }
