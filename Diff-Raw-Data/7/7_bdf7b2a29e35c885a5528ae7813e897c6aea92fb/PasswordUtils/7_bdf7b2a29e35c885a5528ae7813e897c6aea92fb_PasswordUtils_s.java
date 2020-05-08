 package by.vsu.emdsproject.common;
 
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
 
 public class PasswordUtils {
 
     private static String SALT = "";
 
     public static String encode(String password) {
        Md5PasswordEncoder md5PasswordEncoder = new Md5PasswordEncoder();
        return md5PasswordEncoder.encodePassword(password, SALT);
     }
 }
