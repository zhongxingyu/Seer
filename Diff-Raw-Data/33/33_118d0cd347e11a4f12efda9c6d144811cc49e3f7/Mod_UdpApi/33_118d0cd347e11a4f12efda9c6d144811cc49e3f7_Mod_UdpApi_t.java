 package udpApi;
 
 import aniAdd.Communication.ComEvent;
 import aniAdd.Communication.ComListener;
 import aniAdd.IAniAdd;
 import aniAdd.Module;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import aniAdd.misc.ICallBack;
import aniAdd.misc.Misc;
 
 public class Mod_UdpApi  implements Module{
 	public final int MAXRETRIES = 3;
 	public final int CLIENTVER = 3;
 	public final int PROTOCOLVER = 3;
 	public final String CLIENTTAG = "AniAdd";
 	public final String ANIDBAPIHOST = "api.anidb.info";
 	public final int ANIDBAPIPORT = 9000;
 	public final String NODELAY = ""; //"FILE,ANIME,MYLISTADD";
 
 	private InetAddress aniDBIP;
 	private boolean isEncodingSet;
 	private DatagramSocket com;
 	
     private String userName;
     private String password;
     private String session;
     private boolean connected;
     private boolean aniDBAPIDown;
     private boolean auth, isAuthed;
 	
     private ArrayList<Query> queries;
     
     public ArrayList<Query> Queries() {return queries;}
 
 	private ArrayList<Cmd> cmdToSend;
     
 	private Long lastDelayPackageMills;
 
     private Thread send;
     private Thread recieve;
     private Thread idle;
     private Idle idleClass; //-_-
 
     private TreeMap<String, ICallBack<Integer>> eventList;
       
     public Mod_UdpApi() {
         idleClass = new Idle();
         idle = new Thread(idleClass);
         recieve = new Thread(new Recieve());
         send = new Thread(new Send());
 
         queries = new ArrayList<Query>();
         cmdToSend = new ArrayList<Cmd>();
         eventList = new TreeMap<String, ICallBack<Integer>>();
                 
         try {
 			registerEvent(new ICallBack<Integer>() {public void invoke(Integer queryIndex) {InternalMsgHandling(queryIndex);}}, "auth", "logout", "ping");
 			registerEvent(new ICallBack<Integer>() {public void invoke(Integer queryIndex) {InternalMsgHandlingError(queryIndex);}}, "501", "502", "505", "506", "555", "598", "600", "601", "602");
 		} catch (Exception e) {e.printStackTrace();}
 	}
     
 	private void parseReply(String msg) {
         System.out.println("API Reply:"+msg.replace("\n", " \\n "));
         if(msg==null || msg.isEmpty()){
             Log(ComEvent.eType.Warning, "Server reply is an empty string... ignoring");
             return;
         }
 
         Reply reply = new Reply();
         int Pos;
         
         if (!isNumber(msg.substring(0, 3))) {
             Pos = msg.indexOf("-");
             reply.Identifier(msg.substring(0, Pos));
             if(reply.Identifier().indexOf(":") >= 0){
             	reply.Tag(reply.Identifier().split(":")[1]);
             	reply.Identifier(reply.Identifier().split(":")[0]);
             }
             
             msg = msg.substring(Pos+1);
             
             Pos = msg.indexOf(" ");
             reply.QueryId(Integer.parseInt(msg.substring(0, Pos)));
             msg = msg.substring(Pos+1);
             
         } else {
         	reply.Identifier("[SERVER]");
         }
         
         Pos = msg.indexOf(" ");
         reply.ReplyId(Integer.parseInt(msg.substring(0, Pos)));
         msg = msg.substring(Pos+1);
         
         Pos = msg.indexOf("\n");
         reply.ReplyMsg(msg.substring(0, Pos));
         msg = msg.substring(Pos+1);
         
         if (msg.endsWith("\n")) {
             msg = msg.substring(0, msg.length() - 1);
         }
         
         if (msg.indexOf("|") != -1) {
             String[] dataFields;
             if (msg.indexOf("\n") != msg.lastIndexOf("\n")) {
                 dataFields = msg.split("\n");
             } else {
                  //wierd splitting function: if last field empty, it is omitted. Adding a space & & delete it again after splitting
                 dataFields = (msg + " ").split("\\|");
                 int I = dataFields.length-1;
                 dataFields[I] = dataFields[I].substring(0, dataFields[I].length()-1);
             }
             
             for (String dataField : dataFields) reply.DataField().add(dataField);
             
         } else if (!msg.equals("")) {
             reply.DataField().add(msg);
         }
         
         if (!reply.Identifier().equals("")) {
             queries.get(reply.QueryId()).setReply(reply);
             queries.get(reply.QueryId()).setReplyOn(System.currentTimeMillis());
             queries.get(reply.QueryId()).setSuccess(true);
         } else {
         	//TODO ServerReply
         }
 
         Log(ComEvent.eType.Information, "Cmd", (!reply.Identifier().equals("[SERVER]")) ? reply.QueryId() : reply.QueryId() ^ -1, false);
         deliverReply(reply);		
 	}
 	private void deliverReply(Reply reply) {
 		ICallBack<Integer> replyFunc = null;
         if(replyFunc == null) replyFunc = eventList.get(reply.ReplyId().toString());
         if(replyFunc == null) replyFunc = eventList.get(reply.Identifier());
         if(replyFunc != null) {
         	replyFunc.invoke(reply.QueryId());
         }		
 	}
 
 	public boolean authenticate() {
 		if(userName==null || userName.isEmpty() || password==null || password.isEmpty()){
             Log(ComEvent.eType.Error, "UserName or Password not set. (Aborting)");
             return false;
         }
 		
 		if(!connected){
 			if(!connect()) return false;
 		}
 		
         if (idle.getState() == java.lang.Thread.State.NEW) {
         	idle.start();
         } else if (idle.getState() == java.lang.Thread.State.TERMINATED) {
         	idleClass = new Idle();
         	idle = new Thread(idleClass);
         	idle.start();            
         }
       
         if (recieve.getState() == java.lang.Thread.State.NEW) {
             recieve.start();
         } else if (recieve.getState() == java.lang.Thread.State.TERMINATED) {
             recieve = new Thread(new Recieve());
             recieve.start();            
         }
         
         auth = true;
         
         Cmd cmd = new Cmd("AUTH", "auth", null, false);
         if(session!=null && !session.isEmpty()) cmd.setArgs("sess", session);
         if(password!=null && !password.isEmpty()) cmd.setArgs("pass", password);
         cmd.setArgs("user", userName.toLowerCase());
         cmd.setArgs("protover", Integer.toString(PROTOCOLVER), "client", CLIENTTAG.toLowerCase(), "clientver", Integer.toString(CLIENTVER));
         cmd.setArgs("nat", "1", "enc", "UTF8");
         queryCmd(cmd);
         return true;
 	}
 	public boolean connect() {
         try {
             aniDBIP = java.net.InetAddress.getByName(ANIDBAPIHOST);
             com = new java.net.DatagramSocket(ANIDBAPIPORT);
 	        connected = true;
             return true;
         } catch(Exception e) {
             Log(ComEvent.eType.Error, "Couldn't open connection");
             return false;
         }
 	}
 	public void queryCmd(Cmd cmd) {
         if(cmdToSend == null) {
             Log(ComEvent.eType.Warning, "cmd cannot be a null reference... (ignored)");
             return;
         }
 
         synchronized (queries) {
         	cmdToSend.add(cmd);
 		}
         
         if (send.getState() == java.lang.Thread.State.NEW) {
         	send.start();
         } else if (send.getState() == java.lang.Thread.State.TERMINATED) {
         	send = new Thread(new Send());
         	send.start();            
         }
         
         
 	}
 	public void registerEvent(ICallBack<Integer> reply, String... events) {
         for(String evt : events) {
             if(reply != null) {
             	eventList.put(evt, reply);
             }
         }
 	}
 	public void logOut() {
         logOut(true);
     }
 
     public void setPassword(String password){ this.password = password; }
     public void setSession(String session ){ this.session = session; }
     public void setUsername(String userName){ this.userName = userName; }
 
     public int waitingCmdCount(){return cmdToSend.size();}
     public int cmdSendDelay(){return 2200;}
     public int currendCmdDelay(){
         if(lastDelayPackageMills!=null){
             int delay = (int)(2200 - (System.currentTimeMillis() - lastDelayPackageMills));
             return (delay<0?0:delay);
         } else {
             return 0;
         }
     }
     public int totalCmdCount(){ return queries.size() + cmdToSend.size(); }
 
 
 	private class Idle implements Runnable{
 	    private int replysPending;
        private long authRetry;
 		public int getReplysPending() {return replysPending;}
 		
 		public void run() {
             long now;
             int replysPending=0;
 
             do {
                 now = System.currentTimeMillis();
 
                 if(!aniDBAPIDown && isAuthed) {
                     replysPending = 0;
                     for(int i = 0;i < queries.size();i++) {
                         if(queries.get(i).getSuccess() == null && queries.get(i).getSendOn() != null) {
                             if((now - queries.get(i).getSendOn()) / 1000 > 15) {
                                 if(queries.get(i).getRetries() < MAXRETRIES) {
                                     queries.get(i).setRetries(queries.get(i).getRetries()+1);
                                     queries.get(i).setSendOn(null);
                                     System.out.println("Cmd Timeout: Resend (Retries:"+ queries.get(i).getRetries() +")");
                                     queryCmd(queries.get(i).getCmd());
                                 } else {
                                     queries.get(i).setSuccess(false);
                                 }
                             } else if(queries.get(i).getRetries() <= 3) {
                                 replysPending++;
                             }
                         }
                     }
                     this.replysPending=replysPending;
                 }
 
                if(aniDBAPIDown && authRetry==0){
                    authRetry = System.currentTimeMillis() + 5*60*1000;
                    Log(ComEvent.eType.Warning, "API down. Connection retry on "+ Misc.longToTime(authRetry));
                }
                if(auth && aniDBAPIDown && authRetry!=0 && (authRetry - System.currentTimeMillis()<0)){
                    authRetry=0;
                    authenticate();
                }

                 
                 try {Thread.sleep(500);} catch (Exception exception) {}
             } while(connected);
 		}
 	}
 	private class Send implements Runnable{		
 		public void run() {
             byte[] cmdToSendBin;
             boolean cmdReordered;
             long now;
 
             while(cmdToSend.size() > 0 && connected) {
                 now = System.currentTimeMillis();
                 
                 if(idleClass.getReplysPending() < 3 || !isAuthed) {
                     
                     if((!cmdToSend.get(0).LoginReq() || isAuthed) &&
                      (NODELAY.contains(cmdToSend.get(0).Action()) || lastDelayPackageMills == null || (now - lastDelayPackageMills) > 2200)) {
                         //Cmd doesn't need login or client is logged in and is allowed to send
                         //send cmds from top to bottom
 
                         cmdToSendBin = TransformCmd(cmdToSend.get(0));
                         System.out.println("API Send:"+cmdToSend.get(0).toString(session));
                         try {
 							com.send(new DatagramPacket(cmdToSendBin, cmdToSendBin.length, aniDBIP, ANIDBAPIPORT));
 						} catch (IOException e) {e.printStackTrace();}
                         //Debug.Print("Send: " + cmdToSend[0].ToString(session).Replace("\n", " # "));
 
                         if(!NODELAY.contains(cmdToSend.get(0).Action())) {
                             lastDelayPackageMills = now;
                         }
 
                         synchronized (cmdToSend){
                         	cmdToSend.remove(0); 
                         }
 
                     } else if(auth) {
                         //Cmd needs login but client is not connected OR Cmd needs delay which has not yet passed
                         //Move command without (login req./delay req.) to top
 
                         cmdReordered = false;
                         boolean r1, r2, n1, n2, magic;
                         r1 = cmdToSend.get(0).LoginReq();
                         n1 = NODELAY.contains(cmdToSend.get(0).Action());
 
                         if((!isAuthed && r1) || !n1) {
                             synchronized (cmdToSend) {
                                 for(int i = 0;i < cmdToSend.size();i++) {
                                     r2 = cmdToSend.get(0).LoginReq();
                                     n2 = NODELAY.contains(cmdToSend.get(0).Action());
                                     magic = (!isAuthed && !n1 && !r1 && n2 && !r2) ||
                                             (!isAuthed && !n1 && r1 && !r2) ||
                                             (!isAuthed && n1 && r1 && !r2) ||
                                             (isAuthed && !n1 && !r1 && n2) ||
                                             (isAuthed && !n1 && r1 && n2);
 
                                     if(magic) {
                                         cmdToSend.add(0, cmdToSend.get(0));
                                         cmdToSend.remove(i + 1);
                                         cmdReordered = true;
                                         break;
                                     }
                                 }
                             }
                             if(cmdReordered) continue;
                         }
                     }
                 }
 
                 try {Thread.sleep(200);} catch (Exception exception) {}
             }
 			
 		}
 
 		private byte[] TransformCmd(Cmd cmd) {
             Query query;
 
             if(cmd.QueryId() == null) {
                 cmd.QueryId(queries.size());
                 query = new Query();
                 query.setCmd(cmd);
                 queries.add(query);
             } else {
                 query = queries.get(cmd.QueryId());
             }
             query.setSendOn(System.currentTimeMillis());
 
             Log(ComEvent.eType.Information, "Cmd", cmd.QueryId(), query.getRetries() == 0);
 
             if(isEncodingSet) {
                 return cmd.toString(session).getBytes(Charset.forName("ASCII"));
             } else {
             	isEncodingSet = true;
                 return cmd.toString(session).getBytes(Charset.forName("UTF8"));            
             }
 		}	
 	}
 	private class Recieve implements Runnable{
 		public void run() {
             DatagramPacket packet;
             Thread reply;
 
             while(connected) {
                 try {
                 	packet = new DatagramPacket(new byte[1400], 1400);
                     com.receive(packet);
                     reply = new Thread(new Reply(new String(packet.getData(), 0, packet.getLength(), "UTF8")));
                     reply.start();
                 } catch(Exception e) {aniDBAPIDown = true; connected = false;}//TODO
             }
 			
 		}
 		
 		private class Reply implements Runnable{
 			String replyStr;		
 			public Reply(String replyStr) { this.replyStr = replyStr;}
 			public void run() {
 				parseReply(replyStr);
 			}
 		}
 	}
  
     private void logOut(boolean sendCmd) {
         if(sendCmd && isAuthed) {
             Cmd cmd = new Cmd("LOGOUT", "logout", null, false);
             queryCmd(cmd);
             auth = false;
         } else {
             //Unexpected logout
             isEncodingSet = false;
             isAuthed = false;
         }
     }
 	
     private void InternalMsgHandling(int queryIndex) {
         Reply reply = queries.get(queryIndex).getReply();
 
         if(reply.Identifier().equals("auth")) {
                 switch(reply.ReplyId()) {
                     case 200:
                     case 201:
                         if(isAuthed) logOut(false);
                         session = reply.ReplyMsg().substring(0, reply.ReplyMsg().indexOf(" "));
                         reply.ReplyMsg(reply.ReplyMsg().substring(reply.ReplyMsg().indexOf(" ") + 1));
                         isAuthed = true;
 
                         aniDBAPIDown = false;
 
                         if(reply.ReplyId() == 201) ; //TODO Client Out of Date
                         break;
 
                     case 500:
                     case 503:
                     case 504:
                         break;
                 }
 
         } else if(reply.Identifier().equals("logout")) {
                 isEncodingSet = false;
                 isAuthed = false;
 
         } else if(reply.Identifier().equals("ping")) {
 
         } else if(reply.Identifier().equals("uptime")) {
         }
     	
     }
     private void InternalMsgHandlingError(int queryIndex) {
         Query query = queries.get(queryIndex);
 
         switch(query.getReply().ReplyId()) {
             case 501:
             case 502:
             case 505:
             case 601:
             case 602:
             case 555:
                 logOut(false);
                 break;
         }
 
         switch(query.getReply().ReplyId()) {
             case 501:
             case 506:
                 if(auth) authenticate();
                 queryCmd(query.getCmd());
                 break;
 
            case 600:
            case 601:
             case 602:
                 aniDBAPIDown = true;
                 connected = false;//TODO
                 break;
         }
     	
     }
 
 
     // <editor-fold defaultstate="collapsed" desc="IModule">
     protected String modName = "UdpApi";
     protected eModState modState = eModState.New;
 
     public eModState ModState() { return modState; }
     public String ModuleName() {return modName;}
     public void Initialize(IAniAdd aniAdd) {
         modState = eModState.Initializing;
         aniAdd.AddComListener(new AniAddEventHandler());
         modState = eModState.Initialized;
    }
     public void Terminate() {
         modState = eModState.Terminating;
 
         if(connected){
             logOut();
             try {send.join(1000);} catch (InterruptedException ex) {}
             com.close();
         }
 
         try {idle.join(1000);} catch (InterruptedException ex) {}
         try {recieve.join(1000);} catch (InterruptedException ex) {}
         if(send.isAlive() || recieve.isAlive() || idle.isAlive()){
             Log(ComEvent.eType.Warning, "Thread abort timeout", idle.isAlive(), recieve.isAlive(), send.isAlive());
         }
 
         if(com!= null) com.close();
 
         modState = eModState.Terminated;
     }
     // </editor-fold>
 
     // <editor-fold defaultstate="collapsed" desc="Com System">
 	private ArrayList<ComListener> listeners = new ArrayList<ComListener>();
     protected void ComFire(ComEvent comEvent){
         for (ComListener listener : listeners) {
             listener.EventHandler(comEvent);
         }
     }
 	public void AddComListener(ComListener comListener){ listeners.add(comListener); }
 	public void RemoveComListener(ComListener comListener){ listeners.remove(comListener); }
 
     protected void Log(ComEvent.eType type, Object... params){
         ComFire(new ComEvent(this, type, params));
     }
     class AniAddEventHandler implements ComListener{
         public void EventHandler(ComEvent comEvent) {
 
         }
     }
     // </editor-fold>
     
     public static final boolean isNumber(final String s) {
         try {
             Integer.parseInt(s);
             return true;
         } catch (Exception exception) {
             return false;
         }
     }
 }
