 package com.crypto;
 
 import com.data.ChatMessage;
 import com.data.User;
 
 import javax.crypto.Cipher;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.IvParameterSpec;
 import java.util.Calendar;
 
 /**
  *  This class represents a secure (encrypted) Chatmessage, which will be sent.
  *  @author Elias Frantar (0.3)
  *  @version 2.11.2013 (0.3)
  */
 public class S_ChatMessage {
 
     /* message constants */
     private final int TIMESTAMP_LENGTH = 6; // timestamps are always 6 bytes long: (Y - 2000):M:D:H:M:S
     private final byte[] SEPARATOR = {0, 0}; // the separator between message parts is 00
 
     /* plain */
     private Calendar timestamp;
     private User sender;
     private User receiver;
 
     private IvParameterSpec iv;
 
     /* encrypted */
     private byte[] encrypted_message; // the encrypted message
     private byte[] tag; // the MAC authentification tag
 
     /**
      * Creates a new secure_Chatmessage-instance from a plain Chatmessage which will be encrypted using the given keys.
      * @param message the Chatmessage which should be encrypted
      * @param symm_key the key for the symmetric encryption
      * @param MAC_key the key for the MAC
      */
     public S_ChatMessage(ChatMessage message, SecretKey symm_key, SecretKey MAC_key) {
         this.timestamp = message.getTimestamp();
         this.sender = message.getSender();
         this.receiver = message.getReceiver();
 
         iv = Cryptography.gen_symm_IV();
 
         encrypt(message, symm_key, MAC_key, iv);
     }
 
     /**
      * Creates a new secure_Chatmessage-instance from a received byte[] secure_Chatmessage.
      * @param s_message a secure_Chatmessage in byte[] form
      */
     public S_ChatMessage(byte[] s_message) {
         this.timestamp = Calendar.getInstance();
         timestamp.set(2000 + s_message[1], s_message[2], s_message[3], s_message[4], s_message[5]);
 
     }
 
     /**
      * Encrypts a plain message using the given keys.
      * @param message the ChatMessage to encrypt
      * @param symm_key the key to use for symmetric encryption
      * @param MAC_key the key to use for MAC
      * @param iv the initialization vector of the symmetric encryption
      */
     private void encrypt(ChatMessage message, SecretKey symm_key, SecretKey MAC_key, IvParameterSpec iv) {
         encrypted_message = Cryptography.symm_crypt(symm_key, iv, message.getMessage().getBytes(), Cipher.ENCRYPT_MODE);
         tag = Cryptography.mac(MAC_key, getDataBytes());
     }
 
     /**
      * Decrypts this message and returns the plain message.
      * @param symm_key the key to use for symmetric encryption
      * @param MAC_key the key to use for MAC
      * @return the plain message; or null if some error happened
      */
     public ChatMessage decrypt(SecretKey symm_key, SecretKey MAC_key) {
         ChatMessage message = null;
 
         try {
             if(Cryptography.mac(MAC_key, getDataBytes()).equals(tag)) {// check if Mac is valid
                 message = new ChatMessage(sender, receiver,
                                           Cryptography.symm_crypt(symm_key, iv, encrypted_message, Cipher.DECRYPT_MODE).toString(),
                                           timestamp);
             }
         }
         catch(Exception e) {
             message = null; // we must not give any info about the kind of error
         }
 
         return message;
     }
 
     /**
      * Returns a byte[] of all data which will be MACed.
      * @return timestamp || sender || receiver || encrypted_message
      */
     private byte[] getDataBytes() {
         byte[] b_timestamp = {(byte)(timestamp.get(Calendar.YEAR) - 2000), (byte)timestamp.get(Calendar.MONTH), (byte)timestamp.get(Calendar.DAY_OF_MONTH), (byte)timestamp.get(Calendar.HOUR_OF_DAY), (byte)timestamp.get(Calendar.MINUTE), (byte)timestamp.get(Calendar.SECOND)};
         byte[] b_sender = sender.getName().getBytes();
         byte[] b_receiver = receiver.getName().getBytes();
 
         return S_ChatMessage.concat(b_timestamp, b_sender, SEPARATOR, b_receiver, SEPARATOR, iv.getIV(), encrypted_message);
     }
 
     /**
      * Returns the whole message in byte data
     * @return the bytes of this message
      */
     public byte[] getBytes() {
         return S_ChatMessage.concat(getDataBytes(), tag);
     }
 
     /**
      * Concatinates  any number of byte[]
      * @param arrays the byte[] to concatenate
      * @return a single byte[] with all given byte[] concatenated
      */
     public static byte[] concat(byte[] ... arrays)
     {
         int length = 0;
         for (int i = 0; i < arrays.length; i++)
             length += arrays[i].length;
 
         byte[] result = new byte[length];
 
         int index = 0;
         for (int i = 0; i < arrays.length; i++) {
             System.arraycopy(arrays[i], 0, result, index, arrays[i].length);
             index += arrays[i].length;
         }
 
         return result;
     }
 }
