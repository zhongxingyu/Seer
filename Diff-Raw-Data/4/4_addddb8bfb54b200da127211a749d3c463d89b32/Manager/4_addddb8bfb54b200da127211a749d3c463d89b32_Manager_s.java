 import java.io.*;
 import java.net.*;
 import java.rmi.NotBoundException;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.UnicastRemoteObject;
 import java.security.InvalidAlgorithmParameterException;
 import java.security.InvalidKeyException;
 import java.security.KeyPair;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.SecureRandom;
 import java.security.Security;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Set;
 import java.util.concurrent.*;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.EncryptionException;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PasswordFinder;
 
 import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
 
 
 public class Manager {
     private static final boolean DEBUG = true;
     private static final boolean LAB = true;
     private static final String usage = "Usage: bindingName schedulerHost preparationCost [taskDir]";
     private static final String RSASPEC = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
     private String bindingName;
     private String schedHost;
     @SuppressWarnings("unused")
     private String keydir;
     private String enckeyloc;
     private String deckeyloc;
     private PublicKey schedpub;
     private PrivateKey manpriv;
     private int schedTP;
     private int regPort;
     private int prepcosts;
     private Socket schedsock;
     private PrintWriter schedout;
     private BufferedReader schedin;
     private ConcurrentHashMap<String,User> Users = new ConcurrentHashMap<String,User>();
     private ConcurrentHashMap<Integer,Double> Prices = new ConcurrentHashMap<Integer,Double>();
     private ConcurrentHashMap<Integer,MTask> Tasks = new ConcurrentHashMap<Integer,MTask>();
     private LoginHandler LHandler = new LoginHandler(this);
     private EncryptionHandler eh = null;
     public Semaphore RequestMutex = new Semaphore(1);
     
 
     
     public Manager(String bn, String sh, int p){
         //TODO check lab
         Security.insertProviderAt(new BouncyCastleProvider(), 1);
         bindingName = bn;
         schedHost = sh;
         prepcosts = p;
     }
     
     public void inputListen(){
         InputListener i = new InputListener();
         i.start();        
     }
     
     private void exitRoutine(){
         try {
             schedsock.close();
             Registry r = LocateRegistry.getRegistry(regPort);
             r.unbind(bindingName);
             //logout all users
             Enumeration<User> u = Users.elements();
             while(u.hasMoreElements()){
                 User i = u.nextElement();
                 if(i.callback != null){
                     try{
                         i.callback.forceLogout();
                     }
                     catch(Exception e){
                         continue;
                     }
                 }
             }
             UnicastRemoteObject.unexportObject(LHandler, true);
         } catch (IOException e) {
             if(DEBUG){e.printStackTrace();}
         } catch (NotBoundException e) {
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void exitRoutineFail(){
         try {
             if (schedsock != null) {
                 schedsock.close();
             }
             Registry r = LocateRegistry.getRegistry(regPort);
             r.unbind(bindingName);
             //logout all users
             Enumeration<User> u = Users.elements();
             while(u.hasMoreElements()){
                 User i = u.nextElement();
                 if(i.callback != null){
                     try{
                         i.callback.forceLogout();
                     }
                     catch(Exception e){
                         continue;
                     }
                 }
             }
             UnicastRemoteObject.unexportObject(LHandler, true);
         } catch (IOException e) {
             if(DEBUG){e.printStackTrace();}
         } catch (NotBoundException e) {
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void setupRMI(){
         if(!LAB){
             if (System.getSecurityManager() == null) {
                 System.setSecurityManager(new SecurityManager());
             }
         }
         
         try {
             Loginable l = (Loginable) UnicastRemoteObject.exportObject(LHandler, 0);
             Registry r = LocateRegistry.createRegistry(regPort);
             r.rebind(bindingName, l);
             System.out.println("Bound login to "+bindingName);
         } catch (RemoteException e) {
 
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void schedConnect(){
         try {
             schedsock = new Socket(schedHost, schedTP);
             schedout = new PrintWriter(schedsock.getOutputStream(), true);
             schedin = new BufferedReader(new InputStreamReader(schedsock.getInputStream()));
         } catch (UnknownHostException e) {
             System.out.print("Login: Unknown Host, check server name and port.\n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         } catch (IOException e) {
             System.out.print("Login: Could not get I/O for "+schedHost+" \n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         }
     }
     
     private void schedAuthenticate(){
         
         //initialize eh
         try {
             eh= new EncryptionHandler(schedpub, manpriv, RSASPEC);
         } catch (InvalidKeyException e1) {
             if(DEBUG){e1.printStackTrace();}
         } catch (NoSuchAlgorithmException e1) {
             if(DEBUG){e1.printStackTrace();}
         } catch (NoSuchPaddingException e1) {
             if(DEBUG){e1.printStackTrace();}
         }
         
         //generate secrandom for challenge
         SecureRandom r = new SecureRandom();
         final byte[] number = new byte[32];
         r.nextBytes(number);
         String[] firstmsg ={"!login", new String(number)};
         String encrypted;
 
         try {
             encrypted = eh.encryptMessage(firstmsg);
             
             //send challenge 
             schedout.print(encrypted);
             schedout.flush();
         } catch (IllegalBlockSizeException e1) {
             // TODO Auto-generated catch block
             if(DEBUG){e1.printStackTrace();}
         } catch (BadPaddingException e1) {
             // TODO Auto-generated catch block
             if(DEBUG){e1.printStackTrace();}
         }   
 
         
         //get response
         try {
             char[] target = new char[2048];
             String firstrspenc;
             schedin.read(target);
             firstrspenc = new String(target);
             String firstrsp = eh.decryptMessage(firstrspenc.trim());
                         
             String[] split = firstrsp.split(" ");
             if(split[0].contains("!ok")){
                 split = eh.debaseAllButFirst(split);
             }
             else{
                 System.out.println("Sorry Scheduler responded with "+ split[0]+" should habe been !ok");
                 return;
                 }
             if(!Arrays.equals(number,split[1].getBytes())){
                System.out.println("Scheduler retuned wrong Challenge:\n is: "+split[1]+"\n should be: "+new String(number));
                 exitRoutineFail();
             }
             //parse out shared AES and IV
             byte[] iv = split[4].getBytes();
             byte[] encodedsecret = split[3].getBytes();
             //TODO desirealize key
             SecretKeySpec sks = new SecretKeySpec(encodedsecret, "AES");   
     
            
             //reinitialize eh
             eh = new EncryptionHandler(sks,"AES", iv);
             
             
             schedout.println(eh.encryptMessage(split[2]));
             schedout.flush();
             
         } catch (IOException e) {
             if(DEBUG){e.printStackTrace();}
         } catch (Base64DecodingException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         } catch (IllegalBlockSizeException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         } catch (BadPaddingException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         } catch (NoSuchAlgorithmException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         } catch (InvalidKeyException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         } catch (NoSuchPaddingException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         } catch (InvalidAlgorithmParameterException e) {
             // TODO Auto-generated catch block
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void readProperties() throws FileNotFoundException{
         readRegistry();
         readUsers(true);
         readManager();
         readKeys();
     }
 
     private void readKeys() throws FileNotFoundException{
         PEMReader schedpubkey = new PEMReader(new FileReader(enckeyloc));
         try {
             schedpub = (PublicKey) schedpubkey.readObject();
         } catch (IOException e) {
             System.out.println("Something went wrong with reading the sched pub key bailing out");
             if(DEBUG){e.printStackTrace();}
             exitRoutineFail();
         }
         PEMReader mysec = new PEMReader(new FileReader(deckeyloc), new PasswordFinder(){
 
             public char[] getPassword() {
                 System.out.println("Enter pass phrase:");
                 try {
                     return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
                 } catch (IOException e){
                     System.out.println("Error reading from stdin. Bailing out.");
                     if(DEBUG){e.printStackTrace();}
                     exitRoutineFail();
                 } 
                 return null;
             }
         });
         
         try {
             KeyPair kp = (KeyPair) mysec.readObject();
             manpriv = kp.getPrivate();
         } 
         catch (EncryptionException e){
             System.out.println("Sorry wrong password. Try Again.");
             readKeys();
             return;
         }
         catch (IOException e) {
             System.out.println("Error reading from KeyPemMySec. Bailing out.");
             if(DEBUG){e.printStackTrace();}
             exitRoutineFail();
         }
         
     }
     
     private void readManager() throws FileNotFoundException{
         InputStream in = null;
         in = ClassLoader.getSystemResourceAsStream("manager.properties");
         if(in != null){
             java.util.Properties manpropfile = new java.util.Properties();
             try {
                 manpropfile.load(in);
                 Set<String> manprops = manpropfile.stringPropertyNames();
                 
                 for(String prop : manprops){
                     if(prop.contentEquals("scheduler.tcp.port")){
                         schedTP = Integer.parseInt(manpropfile.getProperty(prop));
                         //TODO throw
                     }
                     if(prop.contentEquals("keys.dir")){
                         keydir = manpropfile.getProperty(prop);
                     }
                     if(prop.contentEquals("key.en")){
                         enckeyloc = manpropfile.getProperty(prop);
                     }
                     if(prop.contentEquals("key.de")){
                         deckeyloc = manpropfile.getProperty(prop);                    
                     }
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from manager.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
         
     }
     
     private void readRegistry() throws FileNotFoundException{
         InputStream in = null;
         in = ClassLoader.getSystemResourceAsStream("registry.properties");
         if(in != null){
             java.util.Properties registry = new java.util.Properties();
             try {
                 registry.load(in);
                 Set<String> registryprops = registry.stringPropertyNames();
                 
                 for (String prop : registryprops){
                     String p = registry.getProperty(prop);
                     if(prop.contentEquals("registry.host")){
                         //not important for me it is me
                     }
                     if(prop.contentEquals("registry.port")){
                         regPort = Integer.parseInt(p);
                     }
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from registry.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
             catch(NumberFormatException e){
                 System.out.print("Your registry.proerties file is malformed. Port is not an integer.\n");
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
     }
     
     private void readUsers(boolean fr) throws FileNotFoundException{
         InputStream in = null;
         boolean firstrun = fr;
         in = ClassLoader.getSystemResourceAsStream("user.properties");
         if(in != null){
             java.util.Properties users = new java.util.Properties();
             try {
                 users.load(in);
                 Set<String> userNames = users.stringPropertyNames();
                 
                 for (String userName : userNames){
                     String attribute = users.getProperty(userName);
                     String user[] = userName.split("\\.");
                     if(firstrun){
                         if(user.length == 1){
                             Users.put(userName, new User(userName, attribute));
                         }
                     }
                     if(!firstrun && user.length != 1){
                         if(user[1].contentEquals("admin")){
                             if(attribute.contentEquals("true")){
                                 String pw = Users.get(user[0]).password;
                                 Users.remove(user[0]);
                                 Users.put(user[0], new Admin(user[0], pw));
                             }
                         }
                         if(user[1].contentEquals("credits")){
                             Users.get(user[0]).setCredits(Integer.parseInt(attribute));
                         }
                     }
                 }
                 if(firstrun){
                     readUsers(false);
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from user.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
             catch(NumberFormatException e){
                 System.out.print("Your user.proerties file is malformed, A credit value contains a non integer value.\n");
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
     }
 
     public static void main(String[] args) {
         if(args.length < 3 || args.length > 4){
             System.out.print(usage);
             System.exit(1);
         }
         
         try {
             Manager m = new Manager(args[0], args[1], Integer.parseInt(args[2]));
             
             m.readProperties();
             m.inputListen();
             m.schedConnect();
             m.schedAuthenticate();
             m.setupRMI();
             
             
         } catch (IOException e) {
             System.out.println("Ran in to an I/O Problem. Most likely Some config file is missing.");
             if(DEBUG){e.printStackTrace();}
             return;
         }
           catch(NumberFormatException e){
             System.out.print(usage + "All number values must be integers.\n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         }
     }
     
     
     private class InputListener extends Thread{
         public void run(){
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             String userin;
             
             try {
                 while((userin = stdin.readLine()) != null){
                     if(userin.contentEquals("!users")){
                         Enumeration<User> u = Users.elements();
                         int i = 1;
                         while(u.hasMoreElements()){
                             User a = u.nextElement();
                             System.out.println(i +". "+ a.toString());
                             i++;
                         }
                        
                     }
                     if(userin.contentEquals("!exit")){
                         System.out.println("Exiting on request. Bye.");
                         exitRoutine();
                         return;
                     }
                     if(!userin.contentEquals("")){
                         System.out.print("Unknown command.\n");
                     }
                 }
             } catch (IOException e) {
                 System.out.print("Could not read from stdin.\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
         }
         
     }
     
     private class LoginHandler implements Loginable{
         
         private Manager m = null;
         
         public LoginHandler(Manager m){
             this.m = m;
         }
 
         public Comunicatable login(String uname, String password, Callbackable cb)
                 throws RemoteException {
             User u;
             if((u = Users.get(uname)) != null){
                 if(u.verify(password) && u.callback == null){
                     u.callback = cb;
                     cb.sendMessage("Logged in.");
                     if(u instanceof Admin){
                         return (Comunicatable) UnicastRemoteObject.exportObject(new RAdmin(uname, Users, Prices), 0);
                     }
                     //TODO hand down the correct eh
                     Comunicatable retval =(Comunicatable) UnicastRemoteObject.exportObject(new RComp(uname,u,Tasks, Prices, schedin, schedout,m, prepcosts, eh), 0);
                     return retval;
                 }
             }
             cb.sendMessage("Wrong username or Password or account already in use.");
             return null;
         }
         
     }
     
     
 
 }
