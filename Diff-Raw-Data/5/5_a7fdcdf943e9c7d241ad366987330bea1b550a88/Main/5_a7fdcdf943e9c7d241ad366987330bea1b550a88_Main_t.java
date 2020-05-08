 package org.ybiquitous.messages.sample;
 
 import java.util.Locale;
 
 import org.ybiquitous.messages.MessageKey;
 import org.ybiquitous.messages.ThreadLocalLocaleHolder;
 
 public class Main {
 
     public static void main(String[] args) {
 
         Locale.setDefault(Locale.FRENCH);
 
         // default locale
         p(MessageKey.of("test.key").get(1, "abc"));
         p(MessageKey.of("test.key", "messages").get(1, "abc"));
 
         // specify locale
        p(MessageKey.of("test.key").get(Locale.ENGLISH, 1, "abc"));
 
         // use locale saved on thread-local
        ThreadLocalLocaleHolder.set(Locale.ENGLISH);
         p(MessageKey.of("test.key").get(1, "abc"));
     }
 
     private static void p(Object msg) {
         System.out.println(msg);
     }
 }
