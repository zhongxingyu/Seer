 package by.vsu.emdsproject.common;
 
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 
 public class PasswordUtils {
 
     private static String SALT = "";
 
     public static String encode(String password) {
        ShaPasswordEncoder shaPasswordEncoder = new ShaPasswordEncoder(256);
        return shaPasswordEncoder.encodePassword(password, SALT);
     }
 }
