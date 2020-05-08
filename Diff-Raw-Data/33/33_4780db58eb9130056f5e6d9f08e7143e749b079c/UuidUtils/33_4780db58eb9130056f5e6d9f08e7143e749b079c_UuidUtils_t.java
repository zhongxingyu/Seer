 package daniel.web.util;
 
 import java.security.SecureRandom;
 import java.util.Random;
 
 public final class UuidUtils {
   private static final Random random = new SecureRandom();
   private static final char[] alphanumericCharacters = new char[36];
   private static final int UUID_LENGTH = 24;
 
   static {
     for (int i = 0; i < 10; ++i)
       alphanumericCharacters[i] = (char) ('0' + i);
    for (int i = 0; i < 26; ++i)
      alphanumericCharacters[10 + i] = (char) ('a' + i);
   }
 
   private UuidUtils() {}
 
   public static String randomAlphanumericUuid() {
     StringBuilder sb = new StringBuilder(UUID_LENGTH);
     for (int i = 0; i < UUID_LENGTH; ++i)
       sb.append(alphanumericCharacters[random.nextInt(alphanumericCharacters.length)]);
     return sb.toString();
   }
 }
