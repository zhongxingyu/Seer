 package com.crypto;
 
 import javax.crypto.SecretKey;
import java.math.BigInteger;
 import java.security.KeyPair;
 import java.security.SignedObject;
 import java.util.Calendar;
 
import com.data.ChatContent;
import com.data.ChatMessage;
 import com.data.Message;
 import com.data.User;
 
 /**
  *
  */
 public class TestCrypto {
 
     public static void main(String[] args) {
 
         String text = "This is a simple Test!";
 
         User sender = new User("Alice");
         User receiver = new User("Bob");
 
         SecretKey skey = Cryptography.gen_symm_key();
         KeyPair keypair1 = Cryptography.gen_asymm_key();
         KeyPair keypair2 = Cryptography.gen_asymm_key();
 
         Message<ChatContent> message = new Message<>(Calendar.getInstance().getTime(), "Alice", "Bob", new ChatContent(text));
         Envelope secure_message = new Envelope(message, skey, keypair2.getPublic(), keypair1.getPrivate());
         System.out.println(secure_message.<ChatContent>decryptMessage(keypair2.getPrivate(), keypair1.getPublic()));
     }
 
 }
