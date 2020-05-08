 package com.crypto;
 
 import com.data.ChatMessage;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.IvParameterSpec;
 import java.io.Serializable;
 
 /**
  *
  */
 public class S_ChatMessage implements Serializable {
 
     private ChatMessage message;
 
     private String encrypted_message; // the encrypted message
    private String tag; // the MAC authentication tag
 
     /**
      * Creates a new secure message instance which allows to encrypt the given message.
      * @param message the plain message to encrypt
      */
     public S_ChatMessage(ChatMessage message) {
         this.message = message;
         encrypted_message = "";
         tag = "";
     }
 
     /**
      * Encrypts a plain message using the given keys
      * @param sym_key the key to use for symmetric encryption
      * @param MAC_key the key to use for MAC
      * @param iv the initialization vector of the symmetric encryption
      */
     public void encrypt(SecretKey sym_key, SecretKey MAC_key, IvParameterSpec iv) {
         encrypted_message = Cryptography.symm_crypt(sym_key, iv, message.getMessage().getBytes(), Cipher.ENCRYPT_MODE).toString();
     }
 
     /**
     * Decrypts this message and and initializes the plain values of the message.
      * @param sym_key the key to use for symmetric encryption
      * @param MAC_key the key to use for MAC
      */
     public void decrypt(SecretKey sym_key, SecretKey MAC_key) {
     }
 
     public void setEncrypted_message(String encrypted_message) {
         this.encrypted_message = encrypted_message;
     }
 
     public void setTag(String tag) {
         this.tag = tag;
     }
 
     public String getEncrypted_message() {
         return encrypted_message;
     }
 
     public String getTag() {
         return tag;
     }
 
     public void setMessage(ChatMessage message) {
         this.message = message;
     }
 
     public ChatMessage getMessage() {
         return message;
     }
 }
