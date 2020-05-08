 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.Enumeration;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import javax.crypto.BadPaddingException;
 import javax.crypto.IllegalBlockSizeException;
 
 import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
 
 public class RComp implements Companyable{
         private String name ="";
         private User me;
         private Callbackable cb = null;
         private EncryptionHandler eh = null;
         private EncryptionHandler vh = null;
         private int pcost;
         private ConcurrentHashMap<Integer,MTask> Tasks;
         private ExecutorService TaskEServ = Executors.newCachedThreadPool();
         private ConcurrentHashMap<Integer,Double> Prices;
         private BufferedReader schedin;
         private PrintWriter schedout;
         private static final boolean DEBUG = false;
         private Manager Manager;
         
         public RComp(String n, User u, ConcurrentHashMap<Integer,MTask> t,ConcurrentHashMap<Integer,Double> p,BufferedReader i,PrintWriter o, Manager m, int pc, EncryptionHandler enchandle, EncryptionHandler verhandle){
             name = n;
             me = u;
             cb = me.callback;
             Tasks = t;
             Prices = p;
             schedin = i;
             schedout = o;
             Manager = m;
             pcost = pc;
             eh = enchandle;
             vh = verhandle;
         }
         
         public void buyCredits(int amount) throws RemoteException {
             if (amount > 0) {
                 me.setCredits(me.getCredits() + amount);
                 if (me.callback != null) {
                     me.callback.sendMessage("You have bought " + amount
                             + " Credits. Your balance now is: "
                             + me.getCredits());
                 }
                 return;
             }
             else{
                 throw new RemoteException("Amount of credits to by must be positive");
             }
         }
         
         public void executeTask(int id, String execln) throws RemoteException {
             MTask t = Tasks.get(Integer.valueOf(id));
             if(t == null){
                 if (me.callback != null) {
                     cb.sendMessage("No Task with id: " + id + " known.");
                 }
                 return;
             }
             if(t.owner.contentEquals(name)){
                 t.execln = execln;
                 try {
                     Manager.RequestMutex.acquire();
                 } catch (InterruptedException e) {
                     if(DEBUG){e.printStackTrace();}
                 }
                 TaskEServ.execute(new TaskExecutor(t, cb, me));
             }
             else{
                 if (me.callback != null) {
                     cb.sendMessage("This Task does not belong to you.");
                 }
                 throw new RemoteException("Task not yours.");
             }
                 return;
         }
 
         public int getCredits() throws RemoteException {
             return me.getCredits();
         }
 
         public void getOutputOf(int id) throws RemoteException {
             if(Tasks.containsKey(id)){
                 MTask T = Tasks.get(id);
                 if(T.owner.contentEquals(name)){
                     if(me.getCredits() < 0){
                         if (me.callback != null) {
                             cb.sendMessage("Not enough credits to pay for execution. Please buy credits.");
                         }
                         throw new RemoteException("Not enough credits to pay for execution. Please buy credits.");
                     }                                       
                     if (T.status == TASKSTATE.finished) {
                         if (me.callback != null) {
                            byte[] hash = eh.generateIntegrityCheck(T.output.getBytes());
                             cb.handleResult(T.output, hash);
                         }
                         return;
                     }
                     else{
                         if (me.callback != null) {
                             cb.sendMessage("Sorry Task has not finished executing.");
                             throw new RemoteException("Sorry Task has not finished executing.");
                         }
                     }
                 }
                 else{
                     if (me.callback != null) {
                         cb.sendMessage("This Task does not belong to you!");
                         throw new RemoteException("This Task does not belong to you!");
                     }
                 }
             }
             if (me.callback != null) {
                 cb.sendMessage("Sorry. Task inexistant.");
                 throw new RemoteException("Sorry. Task inexistant.");
             }
         }
 
         public void getTaskInfo(int id) throws RemoteException {
             if(Tasks.containsKey(id)){
                 MTask t = Tasks.get(id);
                 if(t.owner.contentEquals(name)){
                     if (me.callback != null) {
                         cb.sendMessage("Task: " + id + " (" + t.tname + ")\n"
                                 + "Type: " + t.ttype.toString() + "\n"
                                 + "Assigned Engine: " + t.taskEngine + ":"
                                 + t.port + "\n" + "Status: "
                                 + t.status.toString() + "\n" + "Costs: "
                                 + t.cost);
                     }
                     return;
                 }
                 else{
                     if (me.callback != null) {
                         cb.sendMessage("This Task does not belong to you!");
                         throw new RemoteException("This Task does not belong to you!");
                     }
                 }
             }
             if (me.callback != null) {
                 cb.sendMessage("Sorry. Task inexistant.");
                 throw new RemoteException("Sorry. Task inexistant.");
             }
             
         }
 
         public void logout() throws RemoteException {
             Callbackable c = cb;
             c.sendMessage("Logging out...");
             me.callback = null;
             c.sendMessage("done");
             cb = null;
             UnicastRemoteObject.unexportObject(this, true);
             
         }
 
         public void prepareTask(Task t) throws RemoteException {
             double d = getDiscount();
             int costs = Double.valueOf((pcost*(100-d)/100)).intValue();
             
             if (me.getCredits() >= costs) {
                 MTask mt = new MTask(t);
                 mt.status = TASKSTATE.prepared;
                 mt.owner = name;
                 Tasks.put(Integer.valueOf(mt.id), mt);
                 if (t.ttype == TASKTYPE.HIGH) {
                     me.high++;
                 }
                 if (t.ttype == TASKTYPE.MIDDLE) {
                     me.middle++;
                 }
                 if (t.ttype == TASKTYPE.LOW) {
                     me.low++;
                 }
                 int newcreds = me.getCredits() - costs;
                 me.setCredits(newcreds);
                 mt.cost = String.valueOf(costs);
                 if (me.callback != null) {
                     cb.sendMessage("Task prepared with id: " + mt.id);
                 }
                 return;
             }
             else{
                 throw new RemoteException("Sorry insufficient funds to prepare task.");
             }
         }
         
 
         private double getDiscount() {
             int total = me.totalTasks();
             Integer max = 0;
             double discount = 0;
             Enumeration<Integer> k = Prices.keys();
             while (k.hasMoreElements()){
                 Integer s = k.nextElement();
                 int t = s.intValue();
                 if(total >= t && s > max){
                     discount = Prices.get(s);
                     max = s;
                 }
             }
             return discount;
         }
 
 
         private class TaskExecutor implements Runnable{
                 MTask m;
                 Callbackable cb;
                 User me;
                 
                 public TaskExecutor(MTask mt, Callbackable c, User u){
                     m = mt;
                     cb = c;
                     me = u;
 
                 }
                //TODO encrypt all messages
                 public void run() {
                     if (m.status == TASKSTATE.prepared) {
                         try {
                             String requestmsg = "!requestEngine " + m.id + " "+ m.ttype.toString();
                             String encrm = eh.encryptMessage(requestmsg);
                             schedout.println(encrm);
 
                             char[] target = new char[2048];
                             String encrcv;
                             schedin.read(target);
                             encrcv = new String(target);
                             encrcv = encrcv.trim();
                             String rcv = eh.decryptMessage(encrcv);
                             
                             if (rcv.contains("Assigned engine:")) {
                                 String rs[] = rcv.split(" ");
                                 System.out.print(rs[6] + "\n");
                                 m.port = Integer.parseInt(rs[3]);
                                 m.taskEngine = rs[2];
                                 m.status = TASKSTATE.assigned;
                                 System.out.print("Assigned engine: " + rs[2]
                                         + " Port: " + rs[3] + "\n");
                             } else {
                                 System.out.println(rcv);
                                 Manager.RequestMutex.release(); //else this will block for ever
                                 throw new RemoteException(
                                         "Sorry no engine available at the moment, please try again later.");
                             }
                         } catch (IOException e) {
                             if (DEBUG) {
                                 e.printStackTrace();
                             }
                         } catch (IllegalBlockSizeException e) {
                             // TODO Auto-generated catch block
                             if(DEBUG){e.printStackTrace();}
                         } catch (BadPaddingException e) {
                             // TODO Auto-generated catch block
                             if(DEBUG){e.printStackTrace();}
                         } catch (Base64DecodingException e) {
                             // TODO Auto-generated catch block
                             if(DEBUG){e.printStackTrace();}
                         }
                     }
                     
                     if(m.status != TASKSTATE.assigned){
                         try {
                             if (me.callback != null) {
                                 cb.sendMessage("Sorry something went wrong. Taskstate should be assigned but is: "
                                                 + m.status.toString()
                                                 + "\nIf you are trying to resubmit an executing or finished Task be aware that a Task can be submitted only once.\n"
                                                 + "If you are encountering a different problem please contact our staff.");
                             }
                             return;
                         } catch (RemoteException e) {
                             if(DEBUG){e.printStackTrace();}
                             return;
                         }
                     }
                     
                     //Start talking to GTE
                     Socket tsock = null;
                     PrintWriter tout = null;
                     DataOutputStream dout = null;
                     BufferedReader tin = null;
                     
                     
                     try {
                         tsock = new Socket(m.taskEngine,m.port);
                         dout = new DataOutputStream(tsock.getOutputStream());
                         tout = new PrintWriter(tsock.getOutputStream());
                     } catch (UnknownHostException e) {
                         System.out.print("The Host Task Engine "+m.taskEngine+" is unknown. Can not connect.\n");
                         if(DEBUG){e.printStackTrace();}
                         Manager.RequestMutex.release();
                         return;
                     } catch (IOException e) {
                         System.out.print("Sorry encounterd a problem in opening the outgoing Task Engine socket.\n");
                         if(DEBUG){e.printStackTrace();}
                         Manager.RequestMutex.release();
                         return;
                     }
                     
                     try {
                         tin = new BufferedReader(new InputStreamReader(tsock.getInputStream()));
                     } catch (IOException e) {
                         System.out.print("Could not listen for replay from Task Engine.\n");
                         if(DEBUG){e.printStackTrace();}
                         Manager.RequestMutex.release();
                         return;
                     }
                 
                     //Transmit the command string string.
                     //BEWARE THIS IS UNVALIDATE USER INPUT!!!        
                     tout.println(m.execln);
                     tout.println(m.id);
                     tout.println(m.tname);
                     tout.println(m.ttype.toString());
                     tout.println(m.flength);
                     tout.flush();
                     try {
                         tin.readLine(); //this is for sync; a Send will be received maybe useful for later implementations
                     } catch (IOException e1) {
                         if(DEBUG){e1.printStackTrace();}
                     }
                     
 
                     try {
                         byte[] ba = m.binary;
                         dout.write(ba,0,ba.length);
                         dout.flush();
                     }
                         catch (IOException e) {
                         System.out.print("There was a problem with the remote connection, could not send file.\n");
                         if(DEBUG){e.printStackTrace();}
                         Manager.RequestMutex.release();
                         return;
                     }
                         
                         
                     try {
                         String in;
                         if((in = tin.readLine()).contains("execution started")){
                             m.status = TASKSTATE.executing;
                             m.start = System.currentTimeMillis();
                             Manager.RequestMutex.release();
                             if (me.callback != null) {
                                 cb.sendMessage("Execution of task " + m.id+ " started.");
                             }
                         }
                         else{
                             m.status = TASKSTATE.assigned;
                             if (me.callback != null) {
                                 cb.sendMessage("Engine currently not available, try again later.");
                             }
                             return;
                         }
 
                         while((in = tin.readLine())!= null){
                             m.output = m.output.concat(in)+"\n";
                         }
                         m.status = TASKSTATE.finished;
                         m.finish = System.currentTimeMillis();
                         if (me.callback != null) {
                             cb.sendMessage("Execution of Task " + m.id+ " finished.");
                         }
                         long time = m.finish-m.start;
                         float timeinmin = time/(60*1000F);
                         int cost = calcCost(timeinmin);
                         me.setCredits(me.getCredits() - cost);
                         m.cost = String.valueOf((Integer.parseInt(m.cost)+cost));
                         
                         tout.close();
                         dout.close();
                         tin.close();
                         tsock.close();
                         return;
                         
                     } catch (IOException e) {
                         if(DEBUG){e.printStackTrace();}
                     }
 
                 }
 
                 private int calcCost(float time) {
                     int total = me.totalTasks();
                     Integer max = 0;
                     int price;
                     double discount = 0;
                     Enumeration<Integer> k = Prices.keys();
                     while (k.hasMoreElements()){
                         Integer s = k.nextElement();
                         int t = s.intValue();
                         if(total > t && s > max){
                             discount = Prices.get(s);
                             max = s;
                         }
                     }
                     
                     discount= discount/100;
                     price = Double.valueOf(((10*time)-((10*time)*discount))).intValue();
                     
                     return price;
                 }
                 
         }
 
     }
     
 
