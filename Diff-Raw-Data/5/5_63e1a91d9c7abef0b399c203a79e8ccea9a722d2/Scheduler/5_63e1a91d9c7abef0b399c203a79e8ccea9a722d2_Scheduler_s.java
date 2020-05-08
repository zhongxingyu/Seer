 /**
  * 
  * @author Eduard Thamm
  * @matnr 0525087
  * @brief A Scheduling server handling TaskEngins and Clients
  * @detail This Server manages TEs by receiving their isAlive messages and querying them for data if necessary as well as suspending them if they are not needed.
  *  It also manages company data for accounting and billing purposes. 
  * This program was not written with security in mind 
  * !!!DO NOT USE IN PRODUCTIVE ENVIROMENT!!!
  * THERE IS NO LOGIN CHECK ANY MORE
  */
 
 
 import java.io.*;
 import java.net.*;
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
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.*;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.KeyGenerator;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 
 import org.bouncycastle.jce.provider.BouncyCastleProvider;
 import org.bouncycastle.openssl.EncryptionException;
 import org.bouncycastle.openssl.PEMReader;
 import org.bouncycastle.openssl.PasswordFinder;
 
 
 
 public class Scheduler extends AbstractServer {
     
     private int uPort;
     private int minT;
     private int maxT;
     private int tout;
     private int checkP;
     private EncryptionHandler eh;
     private PrivateKey schedpriv;
     private PublicKey manpub;
     @SuppressWarnings("unused")
     private String keydir;
     private String enckeyloc;
     private String deckeyloc;
     private static final String RSASPEC = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
     private static final String AESSPEC = "AES/CTR/NoPadding";
     private static final int BUFSIZE = 1024;
     private static final int MAXCOEF = 1024;
     private Timer etime;
     private DatagramSocket uSock = null;
    private final static String usage = "Usage: Scheduler tcpPort udpPort min max tomeout checkPeriod\n";
     private ConcurrentHashMap<String,GTEntry> GTs = new ConcurrentHashMap<String,GTEntry>();
     private ExecutorService contE = Executors.newCachedThreadPool();
     private Controller c = null;
     private static final boolean DEBUG = true;
     
     public Scheduler(int udpPort, int min, int max, int timeout, int checkPeriod){
         //TODO check Lab
         Security.insertProviderAt(new BouncyCastleProvider(), 1);
         uPort = udpPort;
         minT = min;
         maxT = max;
         tout = timeout;
         checkP = checkPeriod;
     }
     
     private void control() {
         c = new Controller();
         c.start();        
     }
     
     private void setupEH() throws FileNotFoundException{
         readProperties();
         readKeys();
         try {
             eh = new EncryptionHandler(manpub, schedpriv, RSASPEC);
         } catch (InvalidKeyException e) {
             if(DEBUG){e.printStackTrace();}
         } catch (NoSuchAlgorithmException e) {
             if(DEBUG){e.printStackTrace();}
         } catch (NoSuchPaddingException e) {
             if(DEBUG){e.printStackTrace();}
         }
     }
     
     private void readKeys() throws FileNotFoundException{
         PEMReader manpubkey = new PEMReader(new FileReader(enckeyloc));
         try {
             manpub = (PublicKey) manpubkey.readObject();
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
             schedpriv = kp.getPrivate();
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
     
     private void readProperties() throws FileNotFoundException{
         InputStream in = null;
         in = ClassLoader.getSystemResourceAsStream("scheduler.properties");
         if(in != null){
             java.util.Properties schedpropfile = new java.util.Properties();
             try {
                 schedpropfile.load(in);
                 Set<String> shedprops = schedpropfile.stringPropertyNames();
                 
                 for(String prop : shedprops){
                     if(prop.contentEquals("tcp.port")){
                         //TODO throw
                         Tport = Integer.parseInt(schedpropfile.get(prop).toString());
                     }
                     if(prop.contentEquals("key.en")){
                         enckeyloc = schedpropfile.getProperty(prop);
                     }
                     if(prop.contentEquals("key.de")){
                         deckeyloc = schedpropfile.getProperty(prop);                    
                     }
                 }
                 
             } catch (IOException e) {
                 System.out.print("Could not read from scheduler.properties. Exiting.\n");
                 exitRoutineFail();
                 if(DEBUG){e.printStackTrace();}
             }
         }
         else{
             throw new FileNotFoundException();
         }
     }
 
     public void inputListen(){
         InputListener i = new InputListener();
         i.start();        
     }
     
     public void tcpListen() throws IOException {
         TCPListener l = new TCPListener();
         l.start();
         
     }
     
     
     private GTEntry schedule(String t){//String is load.
         int load = 0;
         int coef = MAXCOEF;
         GTEntry gc = null;
         GTEntry g = null;
         
         if(t.contentEquals("LOW")){
             load = 33;
         }
         if(t.contentEquals("MIDDLE")){
             load = 66;
         }
         if(t.contentEquals("HIGH")){
             load = 100;
         }
         if(load == 0 || GTs.isEmpty()){
             return g;
         }
         
         updateLoads();
 
         Enumeration<GTEntry> ge = GTs.elements();
         while(ge.hasMoreElements()){
             gc = ge.nextElement();
             if((gc.getLoad()+load) < 101 && (gc.maxE -gc.minE) < coef){
                 g = gc;
                 coef = gc.maxE -gc.minE;
             }
         }
         g.setLoad(g.getLoad()+load);
         return g;
     }
     
     private void efficencyCheck(){
         etime = new Timer(true);
         etime.scheduleAtFixedRate(new ECheck(), checkP, checkP);
     }
     
     private void updateLoads(){
         Enumeration<GTEntry> ge = GTs.elements();
         ExecutorService es = Executors.newCachedThreadPool();
         GTEntry g;
         
         while(ge.hasMoreElements()){
             g = ge.nextElement();
             try {
                 es.execute(new LWorker(new Socket(g.ip,g.tcp),g));
             } catch (UnknownHostException e) {
                 if(DEBUG){e.printStackTrace();}
             } catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
             }
             //maybe do err msgs
         }
         es.shutdown();
         while(!es.isTerminated()){
             //wait for all requests to finish yeah yeah busy waiting is bad practice
         }
         return;
 
     }
     
     
     public void exitRoutine(){
         contE.shutdownNow();
         if(uSock != null){uSock.close();}
         etime.cancel();
         cancelGETimer();
         super.exitRoutine();
     }
     
     public void exitRoutineFail(){
         contE.shutdownNow();
         if(uSock != null){uSock.close();}
         etime.cancel();
         super.exitRoutineFail();
     }
     
     private void cancelGETimer(){
     	if (!GTs.isEmpty()) {
     		Enumeration<GTEntry> gi = GTs.elements();
             while(gi.hasMoreElements()) {
             	GTEntry g = gi.nextElement();
             	g.stopTimer();
             }	
         }
     }
     
     public static void main(String[] args) {
         
        if(args.length != 6){
             System.out.print(usage);
             System.exit(1);
         }
         
         try {
             //TODO check arguments
             Scheduler sched = new Scheduler(Integer.parseInt(args[0]),Integer.parseInt(args[1]),Integer.parseInt(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]));
             
             sched.setupEH();
             sched.inputListen();
             sched.control();
             sched.tcpListen();
             sched.efficencyCheck();
             
             
         } catch (IOException e) {
             System.out.println("Ran in to an I/O Problem. Most likely Some config file is missing.");
             if(DEBUG){e.printStackTrace();}
             return;
         }
           catch(NumberFormatException e){
             System.out.print(usage + "All values must be integers.\n");
             if(DEBUG){e.printStackTrace();}
             System.exit(1);
         }
 
     }
     
 
     // Client handling is done in worker.
    
     private class Worker extends AbstractServer.Worker{
         PrintWriter out;
         BufferedReader inreader;
         EncryptionHandler ceh = eh;
         public Worker(Socket s) {
             super(s);
         }
         public void run(){
             try{
                 out = new PrintWriter(Csock.getOutputStream());
                 inreader = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                 String input, output;
                 char[] target = new char[2048];
                 while((inreader.read(target)) != -1){
                     input = new String(target);
                     target = new char[2048];
                     try {
                         output = processInput(input,Csock.getInetAddress().toString().substring(1));
                         if (output != null) {
                             String encryptedoutput = ceh.encryptMessage(output);
                             out.println(encryptedoutput);
                             out.flush();
                         }
                     } catch (IllegalBlockSizeException e) {
                         System.out.println("Sorry something went wrong got an " +e.toString()+" exception.");
                         if(DEBUG){e.printStackTrace();}
                     } catch (BadPaddingException e) {
                         System.out.println("Sorry something went wrong got an " +e.toString()+" exception.");
                         if(DEBUG){e.printStackTrace();}
                     }
                 }
                 inreader.close();
                 out.close();
                 Csock.close();
                 return;
             }
             catch(IOException e){
                 if(DEBUG){e.printStackTrace();}
             }
         }
 /*
  *  Preconditions: Client ensures that in !requestEngine only HIGH,MIDDLE,LOW are allowed on in[2], Companies not null
  *  Postconditions:
  */
         private String processInput(String encrypted, String ip) throws IllegalBlockSizeException, BadPaddingException, IOException {
             encrypted = encrypted.trim();
             String input = ceh.decryptMessage(encrypted);   
             
             String[] in = input.split(" ");
             if(in[0].contentEquals("!login")){
                 if(performLogin(ceh.debaseAllButFirst(in))){
                     if(DEBUG){System.out.println("manager logged in successfully");}
                     return null;
                 }
                 else{
                     //TODO maybe terminate connection or something to tell man that something is wrong
                     return null;
                 }
             }
             input = ceh.debaseMassage(input);
             in = input.split(" ");
             if(in[0].contentEquals("!requestEngine")){
                 GTEntry g = schedule(in[2]);
                 if(g == null){
                     return "Not enough capacity. Try again later.";
                 }
                 return "Assigned engine: "+g.ip+" "+g.tcp+" to task "+in[1];
                 
                 
             }
             return "Unrecognised message send !requestEngine or !login."; //would not do this in production gives away to much info.
         }
         
         private boolean performLogin(String [] in) throws IllegalBlockSizeException, BadPaddingException, IOException{
             SecureRandom r = new SecureRandom();
             final byte[] number = new byte[32];
             SecretKey key = null;
             final byte[] iv = new byte[16];
             r.nextBytes(number);
             r.nextBytes(iv);
             try {
                 KeyGenerator kg = KeyGenerator.getInstance("AES");
                 kg.init(256);
                 key = kg.generateKey();
             } catch (NoSuchAlgorithmException e) {
                 if(DEBUG){e.printStackTrace();}
             }
             
             String[] returnmsg ={"!ok", in[1], new String(number), new String(key.getEncoded()), new String(iv)};
             out.println(ceh.encryptMessage(returnmsg));
             out.flush();
             
             // reinitialize connection specific eh
             try {
                 ceh = new EncryptionHandler(key, AESSPEC, iv);
             } catch (InvalidKeyException e) {
                 System.out.println("Sorry something went wrong got an " +e.toString()+" exception.");
                 if(DEBUG){e.printStackTrace();}
             } catch (NoSuchAlgorithmException e) {
                 System.out.println("Sorry something went wrong got an " +e.toString()+" exception.");
                 if(DEBUG){e.printStackTrace();}
             } catch (NoSuchPaddingException e) {
                 System.out.println("Sorry something went wrong got an " +e.toString()+" exception.");
                 if(DEBUG){e.printStackTrace();}
             } catch (InvalidAlgorithmParameterException e) {
                 System.out.println("Sorry something went wrong got an " +e.toString()+" exception.");
                 if(DEBUG){e.printStackTrace();}
             }
             
             char[] target = new char[2048];
             String authentmsg;
             inreader.read(target);
             authentmsg = new String(target);
             authentmsg = authentmsg.trim();
             String challengeb64 = ceh.decryptMessage(authentmsg);
             String challenge = ceh.debaseMassage(challengeb64);
             if(!Arrays.equals(number,challenge.getBytes())){
                 return false;
             }
             return true;
         }
         
     }
         
     
     
     
     
     private class LWorker extends AbstractServer.Worker{
         GTEntry g;
         
         LWorker(Socket s, GTEntry gt){
             super(s);
             g = gt;
         }
         
         public void run(){
             try {
                 PrintWriter sout = new PrintWriter(Csock.getOutputStream(), true);
                 BufferedReader sin = new BufferedReader(new InputStreamReader(Csock.getInputStream()));
                 
                 sout.println("!Load");
                 sout.flush();
                 g.setLoad(Integer.parseInt(sin.readLine()));
                 
                 sin.close();
                 sout.close();
                 Csock.close();
                 return;
                 
             } 
               catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
                 return;
               }
               catch (NumberFormatException e){
                   System.out.println("Recieved a non number value from TE" + g.ip);
                   if(DEBUG){e.printStackTrace();}
                   return;
               }
         }
     }
    
     
     //udp controlling of GTEs is done here
     
     private class Controller extends Thread{
         byte[] buf = new byte[BUFSIZE];
         private DatagramPacket in = new DatagramPacket(buf, buf.length);
         
         /*
          * Preconditions: none
          * Postconditions: a controller listens on the designated port and instantiates a worker for each new incoming GTE, or send msgs to tes
          */
         public void run(){
             try {
                 uSock = new DatagramSocket(uPort);
                 
             } catch (SocketException e) {
                 System.out.print("Unable to listen on UDP "+uPort+"\n");
                 if(DEBUG){e.printStackTrace();}
                 return;
             } 
             
             while(true){
                 try {
                     uSock.receive(in);
                     contE.execute(new CWorker(in));
                 } 
                 catch (IOException e) {
                     if(DEBUG){e.printStackTrace();}
                     return;    
                 }
             }
         }
         
 
     }
     
     private class CWorker implements Runnable{
         //One CWorker manages on GTE
         private DatagramPacket in = null;
         public CWorker(DatagramPacket in) {
             this.in = in;
         }
         
         public CWorker(){
         	
         }
         
         public void run(){
             if(in == null){
                 System.out.println("Something with handing the Datagram to the worker went wrong. He recieved NULL.");
                 return;
             }
             try{
             String inString = new String(in.getData(), 0, in.getLength());
             String rcv[] = inString.split(" ");
         	if (!GTs.isEmpty()) {
         		Enumeration<GTEntry> gi = GTs.elements();
                 while(gi.hasMoreElements()) {
                 	GTEntry g = gi.nextElement();
                     if (g.ip.contains(in.getAddress().toString().substring(1)) && g.tcp == Integer.parseInt(rcv[0])) {
                         if (g.getStatus() != GTSTATUS.suspended) {//ignore isAlives of suspended engines
                             try{
                             g.resetTimer();
                             g.setStatus(GTSTATUS.online);
                             }
                             catch(NullPointerException e){
                                 if(DEBUG){e.printStackTrace();}
                             }                            
                             return;
                         } else {
                             return;// mutliple ges now supported but trouble with isAlives (runns in normal problems only in debug)
                         }
                     }
                 }
             }
             //engine unknown make new
             GTEntry g = new GTEntry(in.getAddress().toString().substring(1),Integer.parseInt(rcv[0]), in.getPort(), GTSTATUS.online, Integer.parseInt(rcv[1]), Integer.parseInt(rcv[2]), 0);
             GTs.put(g.ip+g.tcp, g);
             g.startTimer();
             } catch(NumberFormatException e){
                 if(DEBUG){System.out.print("An isAlive from a new TaskEngine is malformated. IP: "+in.getAddress().toString().substring(1)+" \n");}
                 if(DEBUG){e.printStackTrace();}
             }
             
         }
         
         public void sendToTaskEngine(GTEntry g, String msg){
             byte[]  buf = new byte[BUFSIZE];
             buf = msg.getBytes();
             try {
               DatagramPacket p = new DatagramPacket(buf, buf.length,InetAddress.getByName(g.ip),g.udp);
               uSock.send(p);
           } catch (UnknownHostException e) {
               System.out.print("Could not send message to "+g.ip+" on UDP "+g.udp+" Host Unknown.\n");
               if(DEBUG){e.printStackTrace();}
           } catch (IOException e) {
               System.out.print("Could not send message to "+g.ip+" on UDP "+g.udp+" I/O Error.\n");
               if(DEBUG){e.printStackTrace();}
           }
           }
         
     }
     
     
     //Handling user input
     
     private class InputListener extends Thread{
         public void run(){
             BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
             String userin;
             
             try {
                 while((userin = stdin.readLine()) != null){
                     if(userin.contentEquals("!engines")){
                         int i = 1;
                         Enumeration<GTEntry> gi = GTs.elements();
                         while(gi.hasMoreElements()) {
                         	GTEntry g = gi.nextElement();
                             System.out.print(i+". "+g.toString());
                             i++;
                         }                    
                         userin = "";
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
     private class GTEntry{
         private String ip = "";
         private int tcp = 0;
         private int udp = 0;
         private volatile GTSTATUS status1;
         private int minE = 0;
         private int maxE = 0;
         private volatile int load1 = 0;
         public synchronized GTSTATUS getStatus() {
             return status1;
         }
 
         public synchronized void setStatus(GTSTATUS status) {
             this.status1 = status;
         }
 
         public synchronized int getLoad() {
             return load1;
         }
 
         public synchronized void setLoad(int load) {
             this.load1 = load;
         }
 
         private volatile boolean isAlive = true;
         Timer time;
         //if the volatiles dont work, create synced get/set;
         
         public GTEntry(String ip, int tcp, int udp, GTSTATUS status, int minE, int maxE , int load){
             this.ip = ip;
             this.tcp = tcp;
             this.udp = udp;
             this.status1 = status;
             this.minE = minE;
             this.maxE = maxE;
             this.load1 = load;            
         }
         
         public void startTimer(){
             time = new Timer(true);
             time.scheduleAtFixedRate(new Timeout(this), 0, tout);        
         }
         
         
         
         public void stopTimer(){
             time.cancel();
             time.purge();
         }
         
         public void resetTimer(){
         	isAlive = true;
         }
         
         public String toString(){
             return("IP: "+ip+", TCP: "+tcp+", UDP: "+udp+", "+this.getStatus().toString()+", Energy Signature: min "+minE+", max "+maxE+", Load: "+this.getLoad()+"%\n");
         }
         
         private class Timeout extends TimerTask{
         	GTEntry g = null;
         	public Timeout(GTEntry g){
         		this.g = g;
         	}
 
             public void run() {
                 if(g.isAlive){
                 	g.isAlive = false;
                 	return;
                 }
                 else{
                     if(g.getStatus() != GTSTATUS.suspended){
                         g.setStatus(GTSTATUS.offline);
                     }
                 }
                 return;
             }
             
         }
     }
     
     private class ECheck extends TimerTask{
         
         public ECheck(){
         }
 
         public void run() {
             int highUsers = 0;
             int emptyRunners = 0;
             int gtsUp = 0;
             Enumeration<GTEntry> gi = GTs.elements();
             while(gi.hasMoreElements()) {
             	GTEntry g = gi.nextElement();
                 if(g.getStatus() == GTSTATUS.online){
                     gtsUp++;
                     if(g.getLoad() == 0){
                         emptyRunners++;
                     }
                     if(g.getLoad() > 65){
                         highUsers++;
                     }
                 }
             }
             if(!GTs.isEmpty() && (highUsers == gtsUp && gtsUp < maxT && gtsUp < GTs.size()) || (gtsUp < minT && gtsUp < GTs.size())){
                 // no engine <66 load up and less than max engines active and inactive engines exist
                 // active smaller min and suspended available
                 GTEntry minEngine = GTs.elements().nextElement();//the first element
                 gi = GTs.elements();
                 while(gi.hasMoreElements()) {
                 	GTEntry g = gi.nextElement();
                     if(minEngine.minE < g.minE && g.getStatus() == GTSTATUS.suspended){
                         minEngine = g;
                     }
                 }
                 activate(minEngine);
                 //worst case first engine and that is offline well s happens
             }
             if(emptyRunners > 1 && gtsUp > minT){
                 GTEntry maxEngine = GTs.elements().nextElement();
                 gi = GTs.elements();
                 while(gi.hasMoreElements()) {
                 	GTEntry g = gi.nextElement();
                     if(maxEngine.maxE > g.maxE && g.getStatus() == GTSTATUS.online){
                         maxEngine = g;
                     }
                 }
                 suspend(maxEngine);
             }
             
         }
         
         private void suspend(GTEntry g){
             (new CWorker()).sendToTaskEngine(g, "!suspend");
             GTs.get(g.ip+g.tcp).setStatus(GTSTATUS.suspended);
             g.stopTimer();
         }
         
         private void activate(GTEntry g){
             (new CWorker()).sendToTaskEngine(g, "!wakeUp");
             GTs.get(g.ip+g.tcp).setStatus(GTSTATUS.offline); // will change to online once the first is alive is received cautious approach don't know if machine will respond
             
         }
     }
 
     private class TCPListener extends Thread{
         public void run(){
         try {
             Ssock = new ServerSocket(Tport);
         } catch (IOException e) {
             System.out.print("Could not listen on port: " + Tport + "\n");
             if(DEBUG){e.printStackTrace();}
             exitRoutineFail();
         }
         
         while(true){
             try {
                 abservexe.execute(new Worker(Ssock.accept()));
             } catch (IOException e) {
                 if(DEBUG){e.printStackTrace();}
                 return;
             }
         }
     }
 }
 
 
 }
