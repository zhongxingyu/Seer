 /*
  * For authentication the password for the admin user.
  */
 
 package model;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 /**
  *
  * @author liufeng
  */
 public abstract class Authentication {
     private static boolean isAuthenticated;
     private static String password;
     private static final String PASS_FILE = "passwd";
 
     /**
      * Initialize the password and the user status.
      */
     public static void init() {
         isAuthenticated = false;
         password = "";
         try {
             BufferedReader in = new BufferedReader(new FileReader(PASS_FILE));
            password = in.readLine();
             in.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Check if the user is authenticationed.
      */
     public static boolean isAuthenticated() {
         return isAuthenticated;
     }
 
     /**
      * Check if the password is correct. And change the user status.
      * @param word The user input password
      * @return <strong>true</strong> if the password is correct;
      *          <strong>false</strong> otherwise.
      */
     public static boolean checkPassword(String word) {
         if (password.equals(encrypt(word))) {
             isAuthenticated = true;
         } else {
             isAuthenticated = false;
         }
 
         return isAuthenticated;
     }
 
     /**
      * To de-authenticat the user (a.k.a logout)
      */
     public static void deAuthenticate() {
         isAuthenticated = false;
     }
 
     /**
      * Modify the admin password.
      * @param newPass the new password.
      * @return <strong>true</strong> if the modification success;
      *          <strong>false</strong> otherwise (the user is not authenticated)
      */
     public static boolean modifyPassword(String newPass) {
         if (isAuthenticated()) {
             String encryptedPass = encrypt(newPass);
             try {
                 BufferedWriter out = new BufferedWriter(new FileWriter(PASS_FILE));
                 out.write(encryptedPass);
                 out.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
 
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * To encrypt the string. Uses MD5 for encription.
      * 
      * @param pass
      * @return the encrypted password.
      */
     private static String encrypt(String pass) {
         String md5val = "";
         MessageDigest algorithm = null;
 
         try {
             algorithm = MessageDigest.getInstance("MD5");
         } catch (NoSuchAlgorithmException e) {
             System.out.println("Cannot find digest algorithm");
             e.printStackTrace();
         }
 
         byte[] defaultBytes = pass.getBytes();
         algorithm.reset();
         algorithm.update(defaultBytes);
         byte messageDigest[] = algorithm.digest();
         StringBuffer hexString = new StringBuffer();
 
         for (int i = 0; i < messageDigest.length; i++) {
             String hex = Integer.toHexString(0xFF & messageDigest[i]);
             if (hex.length() == 1) {
                 hexString.append('0');
             }
             hexString.append(hex);
         }
         md5val = hexString.toString();
 
         return md5val;
     }
 }
