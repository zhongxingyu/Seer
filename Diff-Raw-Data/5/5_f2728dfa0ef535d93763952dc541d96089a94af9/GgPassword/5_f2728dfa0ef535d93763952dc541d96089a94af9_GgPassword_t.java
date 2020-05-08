 /**
  * Copyright 2009, Frederic Bregier, and individual contributors
  * by the @author tags. See the COPYRIGHT.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 3.0 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package goldengate.uip;
 
 import goldengate.common.crypto.Blowfish;
 import goldengate.common.crypto.Des;
 import goldengate.common.crypto.KeyObject;
 import goldengate.common.exception.CryptoException;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 /**
  * Console Command Line Main class to provide Password Management for GoldenGate Products.
  *
  * @author Frederic Bregier
  *
  */
 public class GgPassword {
     public static boolean desModel = true;
     public static boolean clearPasswordView = false;
     public static String HELPOPTIONS = "Options available\r\n"+
         "* -ki file to specify the Key File by default\r\n"+
         "* -ko file to specify a new Key File to build and save\r\n"+
         "* -pi file to specify a GGP File by default(password)\r\n"+
         "* -des to specify DES format (default)\r\n"+
         "* -blf to specify BlowFish format\r\n"+
         "* -pwd to specify a clear password as entry\r\n"+
         "* -cpwd to specify a crypted password as entry\r\n"+
         "* -po file to specify a GGP File as output\r\n"+
         "* -clear to specify uncrypted password shown as clear text";
     public static String GGPEXTENSION = "ggp";
     public static String ki = null;
     public static String ko = null;
     public static String pi = null;
     public static String po = null;
     public static String pwd = null;
     public static String cpwd = null;
 
     private File keyFile = null;
     private File passwordFile = null;
     private String clearPassword = null;
     private String cryptedPassword = null;
 
     private KeyObject currentKey;
 
 
     /**
      * @param args
      * @throws Exception
      */
     public static void main(String[] args) throws Exception {
         // TODO Auto-generated method stub
         if (! GgPassword.loadOptions(args)) {
             // Bad options
             System.exit(2);
         }
         GgPassword ggPassword = new GgPassword();
         if (ggPassword.clearPassword == null || ggPassword.clearPassword.length() == 0) {
             System.err.println("Password to crypt:");
             String newp = ggPassword.readString();
             if (newp == null || newp.length() == 0) {
                 System.err.println("No password as input");
                 System.exit(4);
             }
             ggPassword.setClearPassword(newp);
             if (po != null) {
                 ggPassword.setPasswordFile(new File(po));
                 ggPassword.savePasswordFile();
             }
             if (clearPasswordView) {
                 System.err.println("ClearPwd: "+ggPassword.getClearPassword());
                 System.err.println("CryptedPwd: "+ggPassword.getCryptedPassword());
             }
         }
     }
 
     public static boolean loadOptions(String[] args) {
         int i = 0;
         for (i = 0; i < args.length; i++) {
             if (args[i].equalsIgnoreCase("-ki")) {
                 i++;
                 if (i < args.length) {
                     ki = args[i];
                 } else {
                     System.err.println("-ki needs a file as argument");
                     return false;
                 }
             } else if (args[i].equalsIgnoreCase("-ko")) {
                 i++;
                 if (i < args.length) {
                     ko = args[i];
                 } else {
                     System.err.println("-ko needs a file as argument");
                     return false;
                 }
             } else if (args[i].equalsIgnoreCase("-pi")) {
                 i++;
                 if (i < args.length) {
                     pi = args[i];
                 } else {
                     System.err.println("-pi needs a file as argument");
                     return false;
                 }
             } else if (args[i].equalsIgnoreCase("-po")) {
                 i++;
                 if (i < args.length) {
                     po = args[i];
                 } else {
                     System.err.println("-po needs a file as argument");
                     return false;
                 }
             } else if (args[i].equalsIgnoreCase("-des")) {
                 desModel = true;
             } else if (args[i].equalsIgnoreCase("-blf")) {
                 desModel = false;
             } else if (args[i].equalsIgnoreCase("-pwd")) {
                 i++;
                 if (i < args.length) {
                     pwd = args[i];
                 } else {
                     System.err.println("-pwd needs a password as argument");
                     return false;
                 }
             } else if (args[i].equalsIgnoreCase("-cpwd")) {
                 i++;
                 if (i < args.length) {
                     cpwd = args[i];
                 } else {
                     System.err.println("-cpwd needs a crypted password as argument");
                     return false;
                 }
             } else if (args[i].equalsIgnoreCase("-clear")) {
                 clearPasswordView = true;
             } else {
                 System.err.println("Unknown option: "+args[i]);
                 return false;
             }
         }
         return true;
     }
 
     public GgPassword() throws Exception {
         if (desModel) {
             currentKey = new Des();
         } else {
             currentKey = new Blowfish();
         }
         if (ki != null) {
             loadKey(new File(ki));
         }
         if (pi != null) {
             setPasswordFile(new File(pi));
             loadPasswordFile();
         }
         if (pwd != null) {
             setClearPassword(pwd);
         }
         if (cpwd != null) {
             setCryptedPassword(cpwd);
         }
         if (ko != null) {
             createNewKey();
             saveKey(new File(ko));
         }
         if (po != null) {
             setPasswordFile(new File(po));
             savePasswordFile();
         }
         if (clearPassword != null) {
             if (clearPasswordView) {
                 System.err.println("ClearPwd: "+getClearPassword());
             }
             System.err.println("CryptedPwd: "+getCryptedPassword());
         }
     }
 
     private String readString() {
         String read = "";
         InputStreamReader input = new InputStreamReader(System.in);
         BufferedReader reader = new BufferedReader(input);
         try {
             read = reader.readLine();
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
         return read;
     }
 
     /**
      * Create a new Key but do not save it on file
      * @throws Exception
      */
     public void createNewKey() throws Exception {
         try {
             currentKey.generateKey();
         } catch (Exception e) {
             throw new CryptoException("Create New Key in error", e);
         }
         if (clearPassword != null) {
             setClearPassword(clearPassword);
         }
     }
     /**
      *
      * @param file source file
      * @throws CryptoException
      */
     public void loadKey(File file) throws CryptoException {
         keyFile = file;
         try {
             currentKey.setSecretKey(file);
         } catch (IOException e) {
             throw new CryptoException("Load Key in error", e);
         }
     }
 
     /**
      *
      * @param file destination file, if null previously set file is used
      * @throws CryptoException
      */
     public void saveKey(File file) throws CryptoException {
         if (file != null) {
             keyFile = file;
         }
         try {
             currentKey.saveSecretKey(keyFile);
         } catch (IOException e) {
             throw new CryptoException("Save Key in error", e);
         }
     }
 
     /**
      *
      * @return True if the associated key is ready
      */
     public boolean keyReady() {
         return currentKey.keyReady();
     }
 
     /**
      *
      * @return The File associated with the current Key
      */
     public File getKeyFile() {
         return keyFile;
     }
 
     /**
      * Set the new password and its crypted value
      * @param passwd
      * @throws Exception
      */
     public void setClearPassword(String passwd) throws Exception {
         clearPassword = passwd;
        cryptedPassword = currentKey.cryptToHex(clearPassword);
     }
 
     /**
      * @return the passwordFile
      */
     public File getPasswordFile() {
         return passwordFile;
     }
 
     /**
      * @param passwordFile the passwordFile to set
      * @throws IOException
      */
     public void setPasswordFile(File passwordFile) {
         this.passwordFile = passwordFile;
     }
 
     /**
      * Save the Crypted Paswword to the File
      * @throws IOException
      */
     public void savePasswordFile() throws IOException {
         FileOutputStream outputStream = new FileOutputStream(passwordFile);
         outputStream.write(cryptedPassword.getBytes());
         outputStream.flush();
         outputStream.close();
     }
 
     /**
      * Load the crypted password from the file
      * @throws Exception
      */
     public void loadPasswordFile() throws Exception {
         if (passwordFile.canRead()) {
             int len = (int)passwordFile.length();
             byte []key = new byte[len];
             FileInputStream inputStream = null;
             inputStream = new FileInputStream(passwordFile);
             DataInputStream dis = new DataInputStream(inputStream);
             dis.readFully(key);
             dis.close();
             setCryptedPassword(new String(key));
         } else {
             throw new CryptoException("Cannot read crypto file");
         }
     }
     /**
      * @return the cryptedPassword
      */
     public String getCryptedPassword() {
         return cryptedPassword;
     }
 
     /**
      * @param cryptedPassword the cryptedPassword to set
      * @throws Exception
      */
     public void setCryptedPassword(String cryptedPassword) throws Exception {
         this.cryptedPassword = cryptedPassword;
        this.clearPassword = currentKey.decryptHexInString(cryptedPassword);
     }
 
     /**
      * @return the clearPassword
      */
     public String getClearPassword() {
         return clearPassword;
     }
 
 }
