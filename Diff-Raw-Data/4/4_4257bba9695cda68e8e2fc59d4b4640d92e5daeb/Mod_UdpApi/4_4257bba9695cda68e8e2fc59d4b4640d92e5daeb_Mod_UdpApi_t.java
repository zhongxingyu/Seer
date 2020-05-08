 package udpApi;
 
 import aniAdd.Communication.ComEvent;
 import aniAdd.Communication.ComListener;
 import aniAdd.IAniAdd;
 import aniAdd.Modules.IModule;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.TreeMap;
 
 import aniAdd.misc.ICallBack;
 import aniAdd.misc.Misc;
 import java.util.Date;
 
 public class Mod_UdpApi implements IModule {
 
     public final int MAXRETRIES = 2;
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
     private String autoPass;
     private String session;
     private String aniDBsession;
     private boolean connected;
     private boolean aniDBAPIDown;
     private boolean auth, isAuthed;
     private ArrayList<Query> queries;
     private ArrayList<Reply> serverReplies;
     private final ArrayList<Cmd> cmdToSend;
     private Date lastDelayPackageMills;
     private Date lastReplyPackage;
     private int replyHeadStart; //TODO: Change handling
     private Thread send;
     private Thread receive;
     private Thread idle;
     private Idle idleClass; //-_-
     private TreeMap<String, ICallBack<Integer>> eventList;
 
     public ArrayList<Query> Queries() {
         return queries;
     }
 
     public ArrayList<Reply> ServerReplies() {
         return serverReplies;
     }
 
     public Mod_UdpApi() {
         idleClass = new Idle();
         idle = new Thread(idleClass);
         receive = new Thread(new Receive());
         send = new Thread(new Send());
 
         queries = new ArrayList<Query>();
         serverReplies = new ArrayList<Reply>();
         cmdToSend = new ArrayList<Cmd>();
         eventList = new TreeMap<String, ICallBack<Integer>>();
 
         replyHeadStart = 0;
 
         try {
             registerEvent(new ICallBack<Integer>() {
 
                 public void invoke(Integer queryIndex) {
                     InternalMsgHandling(queryIndex);
                 }
             }, "auth", "logout", "ping");
             registerEvent(new ICallBack<Integer>() {
 
                 public void invoke(Integer queryIndex) {
                     InternalMsgHandlingError(queryIndex);
                 }
             }, "501", "502", "505", "506", "555", "598", "600", "601", "602");
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     private void parseReply(String msg) {
         System.out.println("API Reply:" + msg.replace("\n", " \\n "));
         if (msg == null || msg.isEmpty()) {
             Log(ComEvent.eType.Warning, "Server reply is an empty string... ignoring");
             return;
         }
 
         Reply reply = new Reply();
         int Pos;
 
         if (!isNumber(msg.substring(0, 3))) {
             Pos = msg.indexOf("-");
             reply.Identifier(msg.substring(0, Pos));
             if (reply.Identifier().indexOf(":") >= 0) {
                 reply.Tag(reply.Identifier().split(":")[1]);
                 reply.Identifier(reply.Identifier().split(":")[0]);
             }
 
             msg = msg.substring(Pos + 1);
 
             Pos = msg.indexOf(" ");
             reply.QueryId(Integer.parseInt(msg.substring(0, Pos)));
             msg = msg.substring(Pos + 1);
 
         } else {
             reply.QueryId(serverReplies.size());
             reply.Identifier("[SERVER]");
         }
 
         Pos = msg.indexOf(" ");
         reply.ReplyId(Integer.parseInt(msg.substring(0, Pos)));
         msg = msg.substring(Pos + 1);
 
         Pos = msg.indexOf("\n");
         reply.ReplyMsg(msg.substring(0, Pos));
         msg = msg.substring(Pos + 1);
 
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
                 int I = dataFields.length - 1;
                 dataFields[I] = dataFields[I].substring(0, dataFields[I].length() - 1);
             }
 
             for (String dataField : dataFields) {
                 reply.DataField().add(dataField);
             }
 
         } else if (!msg.equals("")) {
             reply.DataField().add(msg);
         }
 
         if (!reply.Identifier().equals("[SERVER]")) {
             queries.get(reply.QueryId()).setReply(reply);
             queries.get(reply.QueryId()).setReplyOn(new Date());
             queries.get(reply.QueryId()).setSuccess(true);
         } else {
             serverReplies.add(reply);
         }
 
         Log(ComEvent.eType.Information, "Reply", (!reply.Identifier().equals("[SERVER]")) ? reply.QueryId() : reply.QueryId() ^ -1, false);
         deliverReply(reply);
     }
 
     private void deliverReply(Reply reply) {
         ICallBack<Integer> replyFunc = null;
         if (replyFunc == null) {
             replyFunc = eventList.get(reply.ReplyId().toString());
         }
         if (replyFunc == null) {
             replyFunc = eventList.get(reply.Identifier());
         }
         if (replyFunc != null) {
             replyFunc.invoke((!reply.Identifier().equals("[SERVER]")) ? reply.QueryId() : reply.QueryId() ^ -1);
         } else {
             Log(ComEvent.eType.Debug, "Reply couldn't be delivered (unhandled reply)");
         }
     }
 
     public boolean authenticate() {
         Log(ComEvent.eType.Debug, "Authenticating");
 
         boolean hasUserInfo, hasPass, hasAniDBSession, hasAutoPass;
         hasUserInfo = userName != null && !userName.isEmpty();
         hasPass = password != null && !password.isEmpty();
         hasAniDBSession = aniDBsession != null && !aniDBsession.isEmpty();
         hasAutoPass = autoPass != null && !autoPass.isEmpty();
 
         if (!hasUserInfo || !(hasPass || hasAniDBSession || hasAutoPass)) {
             Log(ComEvent.eType.Error, "UserName or Password not set. (Aborting)");
             return false;
         }
 
         if (!connected) {
             if (!connect()) {
                 return false;
             }
         }
 
         if (idle.getState() == java.lang.Thread.State.NEW) {
             Log(ComEvent.eType.Debug, "Starting Idle thread");
             idle.start();
         } else if (idle.getState() == java.lang.Thread.State.TERMINATED) {
             Log(ComEvent.eType.Debug, "Restarting Idle thread");
             idleClass = new Idle();
             idle = new Thread(idleClass);
             idle.start();
         }
 
         if (receive.getState() == java.lang.Thread.State.NEW) {
             Log(ComEvent.eType.Debug, "Starting Receive Thread");
             receive.start();
         } else if (receive.getState() == java.lang.Thread.State.TERMINATED) {
             Log(ComEvent.eType.Debug, "Restarting Receive thread");
             receive = new Thread(new Receive());
             receive.start();
         }
 
         auth = true;
         Cmd cmd = new Cmd("AUTH", "auth", null, false);
        if (aniDBsession != null && !aniDBsession.isEmpty()) {
             cmd.setArgs("sess", aniDBsession);
         }
         if (password != null && !password.isEmpty()) {
             cmd.setArgs("pass", password);
         }
         if (autoPass != null && !autoPass.isEmpty()) {
             cmd.setArgs("autopass", autoPass);
         }
 
 
         cmd.setArgs("user", userName.toLowerCase());
         cmd.setArgs("protover", Integer.toString(PROTOCOLVER), "client", CLIENTTAG.toLowerCase(), "clientver", Integer.toString(CLIENTVER));
         cmd.setArgs("nat", "1", "enc", "UTF8");
 
         synchronized (cmdToSend) {
             for (int i = cmdToSend.size() - 1; i >= 0; i--) {
                 if (cmdToSend.get(i).Action().equals("AUTH")) {
                     cmdToSend.remove(i);
                     Log(ComEvent.eType.Debug, "Pending (old) Authentication removed");
                 }
             }
         }
 
         Log(ComEvent.eType.Debug, "Adding Authentication Cmd to queue");
         queryCmd(cmd);
         return true;
     }
 
     public boolean connect() {
         try {
             aniDBIP = java.net.InetAddress.getByName(ANIDBAPIHOST);
             com = new java.net.DatagramSocket(ANIDBAPIPORT);
             connected = true;
             return true;
         } catch (Exception e) {
             Log(ComEvent.eType.Error, "Couldn't open connection. (Client may be running twice)");
             return false;
         }
     }
 
     public void queryCmd(Cmd cmd) {
         if (cmd == null) {
             Log(ComEvent.eType.Warning, "cmd cannot be a null reference... (ignored)");
             return;
         }
 
         synchronized (cmdToSend) {
             cmdToSend.add(cmd);
             Log(ComEvent.eType.Debug, "Added " + cmd.Action() + " cmd to queue");
         }
 
         if (send.getState() == java.lang.Thread.State.NEW) {
             Log(ComEvent.eType.Debug, "Starting Send thread");
             send.start();
         } else if (send.getState() == java.lang.Thread.State.TERMINATED) {
             Log(ComEvent.eType.Debug, "Restarting Send thread");
             send = new Thread(new Send());
             send.start();
         }
     }
 
     public void registerEvent(ICallBack<Integer> reply, String... events) {
         String evtLst = " ";
         for (String evt : events) {
             if (reply != null) {
                 eventList.put(evt, reply);
                 evtLst += evt + " ";
             }
         }
         Log(ComEvent.eType.Debug, "Registered Events (" + evtLst + ") for " + reply.getClass().getName());
     }
 
     public void logOut() {
         logOut(true);
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public void setAniDBSession(String aniDBsession) {
         this.aniDBsession = aniDBsession;
     }
 
     public void setAutoPass(String autoPass) {
         this.autoPass = autoPass;
     }
 
     public void setUsername(String userName) {
         this.userName = userName;
     }
 
     public int waitingCmdCount() {
         return cmdToSend.size();
     }
 
     public int cmdSendDelay() {
         return 2200;
     }
 
     public int currendCmdDelay() {
         if (lastDelayPackageMills != null) {
 
             int delay = (int) (2200 - ((new Date()).getTime() - lastDelayPackageMills.getTime()));
             return (delay < 0 ? 0 : delay);
         } else {
             return 0;
         }
     }
 
     public int totalCmdCount() {
         return queries.size() + cmdToSend.size();
     }
 
     private class Idle implements Runnable {
 
         private int replysPending;
         private Date authRetry;
         private boolean longDelay = false;
 
         public int getReplysPending() {
             return replysPending;
         }
 
         public void run() {
             Date now;
             int replysPending = 0;
 
             do {
                 now = new Date();
 
                 if (!aniDBAPIDown) {
                     authRetry = null;
                 }
 
                 if (!aniDBAPIDown && auth) {
                     replysPending = 0;
                     for (int i = 0; i < queries.size(); i++) {
                         if (queries.get(i).getSuccess() == null && queries.get(i).getSendOn() != null) {
                             if ((now.getTime() - queries.get(i).getSendOn().getTime()) > 15000) {
                                 if (queries.get(i).getRetries() < MAXRETRIES) {
                                     queries.get(i).setRetries(queries.get(i).getRetries() + 1);
                                     queries.get(i).setSendOn(null);
                                     Log(ComEvent.eType.Debug, "Cmd Timeout: Resend (Retries:" + queries.get(i).getRetries() + ")");
                                     queryCmd(queries.get(i).getCmd());
                                 } else {
                                     queries.get(i).setSuccess(false);
                                     Log(ComEvent.eType.Information, "Cmd", i, false);
                                     Log(ComEvent.eType.Error, "Sending command failed. (Retried " + MAXRETRIES + " times)");
                                 }
                             } else if (queries.get(i).getRetries() <= MAXRETRIES) {
                                 replysPending++;
                             }
                         }
                     }
                     this.replysPending = replysPending;
 
                     if ((lastReplyPackage == null || (now.getTime() - lastReplyPackage.getTime()) > 60000) && replyHeadStart >= 3 && !longDelay) { //quickfix
                         //logOut(false);
                         //authRetry = new Date(now.getTime() + 60 * 60 * 1000);
                         //aniDBAPIDown = true;
                         Log(ComEvent.eType.Warning, "Reply delay has passed 1 minute.");
                         longDelay = true;
 
                         if ((now.getTime() - lastReplyPackage.getTime()) > 300000) {
                             authenticate();
                         }
                     }//quickfix
                 }
 
                 if (aniDBAPIDown && authRetry == null) {
                     authRetry = new Date(now.getTime() + 5 * 60 * 1000);
                     Log(ComEvent.eType.Warning, "API down. Connection retry on " + Misc.DateToString(authRetry));
                 }
                 if (auth && aniDBAPIDown && authRetry != null && (authRetry.getTime() - now.getTime() < 0)) {
                     authRetry = null;
                     authenticate();
                 }
 
 
                 try {
                     Thread.sleep(500);
                 } catch (Exception exception) {
                 }
             } while (connected);
         }
     }
 
     private class Send implements Runnable {
 
         public void run() {
             byte[] cmdToSendBin;
             boolean cmdReordered;
             Date now;
 
             while (cmdToSend.size() > 0 && connected) {
                 cmdReordered = false;
                 now = new Date();
 
                 synchronized (cmdToSend) {
                     if ((replyHeadStart < 3 && idleClass.getReplysPending() < 2) || !isAuthed) {
 
                         if ((!cmdToSend.get(0).LoginReq() || isAuthed) &&
                                 (NODELAY.contains(cmdToSend.get(0).Action()) || lastDelayPackageMills == null || (now.getTime() - lastDelayPackageMills.getTime()) > 2200)) {
                             //Cmd doesn't need login or client is logged in and is allowed to send
                             //send cmds from top to bottom
 
                             cmdToSendBin = TransformCmd(cmdToSend.get(0));
                             System.out.println("API Send:" + cmdToSend.get(0).toString(session));
                             try {
                                 com.send(new DatagramPacket(cmdToSendBin, cmdToSendBin.length, aniDBIP, ANIDBAPIPORT));
                             } catch (IOException e) {
                                 e.printStackTrace();
                             }
                             //Debug.Print("Send: " + cmdToSend[0].ToString(session).Replace("\n", " # "));
 
                             if (!NODELAY.contains(cmdToSend.get(0).Action())) {
                                 lastDelayPackageMills = new Date(now.getTime());
                             }
 
                             replyHeadStart++;
 
                             cmdToSend.remove(0);
 
                         } else if (auth) {
                             //Cmd needs login but client is not connected OR Cmd needs delay which has not yet passed
                             //Move command without (login req./delay req.) to top
 
                             cmdReordered = false;
                             boolean r1, r2, n1, n2, canOptimize;
                             r1 = cmdToSend.get(0).LoginReq();
                             n1 = NODELAY.contains(cmdToSend.get(0).Action());
 
                             if ((!isAuthed && r1) || !n1) {
                                 for (int i = 0; i < cmdToSend.size(); i++) {
                                     r2 = cmdToSend.get(i).LoginReq();
                                     n2 = NODELAY.contains(cmdToSend.get(i).Action());
                                     canOptimize = (!isAuthed && !n1 && !r1 && n2 && !r2) ||
                                             (!isAuthed && !n1 && r1 && !r2) ||
                                             (!isAuthed && n1 && r1 && !r2) ||
                                             (isAuthed && !n1 && !r1 && n2) ||
                                             (isAuthed && !n1 && r1 && n2);
 
                                     if (canOptimize) {
                                         Log(ComEvent.eType.Debug, "cmdToSend reordered QueryId: " + i + " Action: " + cmdToSend.get(i).Action());
                                         cmdToSend.add(0, cmdToSend.get(i));
                                         cmdToSend.remove(i + 1);
                                         cmdReordered = true;
                                         break;
                                     }
                                 }
                             }
 
                         }
                     }
                 }
                 if (!cmdReordered) {
                     try {
                         Thread.sleep(200);
                     } catch (Exception exception) {
                     }
                 }
             }
 
         }
 
         private byte[] TransformCmd(Cmd cmd) {
             Query query;
 
             if (cmd.QueryId() == null) {
                 cmd.QueryId(queries.size());
                 query = new Query();
                 query.setCmd(cmd);
                 queries.add(query);
             } else {
                 query = queries.get(cmd.QueryId());
             }
             query.setSendOn(new Date());
 
             Log(ComEvent.eType.Information, "Cmd", cmd.QueryId(), query.getRetries() == 0);
 
             if (isEncodingSet) {
                 return cmd.toString(session).getBytes(Charset.forName("ASCII"));
             } else {
                 isEncodingSet = true;
                 return cmd.toString(session).getBytes(Charset.forName("UTF8"));
             }
         }
     }
 
     private class Receive implements Runnable {
 
         public void run() {
             DatagramPacket packet;
             Thread reply;
 
             while (connected) {
                 try {
                     packet = new DatagramPacket(new byte[1400], 1400);
                     com.receive(packet);
                     replyHeadStart = 0;
                     aniDBAPIDown = false;
 
                     reply = new Thread(new Reply(new String(packet.getData(), 0, packet.getLength(), "UTF8")));
                     reply.start();
                 } catch (Exception e) {
                     aniDBAPIDown = true;
                     connected = false;
                 }//TODO
             }
 
         }
 
         private class Reply implements Runnable {
 
             String replyStr;
 
             public Reply(String replyStr) {
                 this.replyStr = replyStr;
             }
 
             public void run() {
                 parseReply(replyStr);
             }
         }
     }
 
     private void logOut(boolean sendCmd) {
         if (sendCmd && isAuthed) {
             Cmd cmd = new Cmd("LOGOUT", "logout", null, false);
             queryCmd(cmd);
             auth = false;
         } else {
             //Unexpected logout
             Log(ComEvent.eType.Debug, "Sync Logout");
             isEncodingSet = false;
             isAuthed = false;
             session = null;
         }
     }
 
     private void InternalMsgHandling(int queryIndex) {
         Reply reply = queries.get(queryIndex).getReply();
 
         if (reply.Identifier().equals("auth")) {
             switch (reply.ReplyId()) {
                 case 200:
                 case 201:
                     if (isAuthed) {
                         logOut(false);
                     }
                     session = reply.ReplyMsg().substring(0, reply.ReplyMsg().indexOf(" "));
                     reply.ReplyMsg(reply.ReplyMsg().substring(reply.ReplyMsg().indexOf(" ") + 1));
                     isAuthed = true;
 
                     aniDBAPIDown = false;
 
                     if (reply.ReplyId() == 201); //TODO Client Out of Date
                     break;
 
                 case 500:
                     Log(ComEvent.eType.Error, "Wrong username and/or password. Try logging out of AniDB and back in.");
                     break;
                 case 503:
                     Log(ComEvent.eType.Error, "Outdated Version");
                     break;
                 case 504:
                     Log(ComEvent.eType.Error, "Client temporarily disabled by administrator - please try again later");
                     break;
             }
 
         } else if (reply.Identifier().equals("logout")) {
             isEncodingSet = false;
             isAuthed = false;
             session = null;
 
         } else if (reply.Identifier().equals("ping")) {
         } else if (reply.Identifier().equals("uptime")) {
         }
 
     }
 
     private void InternalMsgHandlingError(int queryIndex) {
         Reply reply = queryIndex < 0 ? serverReplies.get(queryIndex ^ -1) : queries.get(queryIndex).getReply();
 
         switch (reply.ReplyId()) {
             case 501:
             case 502:
             case 505:
             case 601:
             case 602:
             case 555:
                 logOut(false);
                 break;
         }
 
         switch (reply.ReplyId()) {
             case 501:
             case 506:
                 if (auth) {
                     authenticate();
                 }
                 queryCmd(queries.get(queryIndex).getCmd());
                 break;
 
             case 600:
             case 601:
             case 602:
             case 666:
                 aniDBAPIDown = true;
                 //Log(ComEvent.eType.Error, "Server API Failure Code: " + reply.ReplyId());
                 //connected = false;//TODO
                 break;
 
             case 502:
             case 505:
             case 555:
             case 598:
                 Log(ComEvent.eType.Error, "Client Failure Code: " + reply.ReplyId());
                 break;
         }
 
     }
     // <editor-fold defaultstate="collapsed" desc="IModule">
     protected String modName = "UdpApi";
     protected eModState modState = eModState.New;
 
     public eModState ModState() {
         return modState;
     }
 
     public String ModuleName() {
         return modName;
     }
 
     public void Initialize(IAniAdd aniAdd) {
         modState = eModState.Initializing;
         aniAdd.AddComListener(new AniAddEventHandler());
         modState = eModState.Initialized;
     }
 
     public void Terminate() {
         modState = eModState.Terminating;
 
         if (connected) {
             logOut();
             try {
                 send.join(1000);
             } catch (InterruptedException ex) {
             }
             com.close();
         }
 
         try {
             idle.join(1000);
         } catch (InterruptedException ex) {
         }
         try {
             receive.join(1000);
         } catch (InterruptedException ex) {
         }
         if (send.isAlive() || receive.isAlive() || idle.isAlive()) {
             Log(ComEvent.eType.Warning, "Thread abort timeout", idle.isAlive(), receive.isAlive(), send.isAlive());
         }
 
         if (com != null) {
             com.close();
         }
 
         modState = eModState.Terminated;
     }
     // </editor-fold>
     // <editor-fold defaultstate="collapsed" desc="Com System">
     private ArrayList<ComListener> listeners = new ArrayList<ComListener>();
 
     protected void ComFire(ComEvent comEvent) {
         for (ComListener listener : listeners) {
             listener.EventHandler(comEvent);
         }
     }
 
     public void AddComListener(ComListener comListener) {
         listeners.add(comListener);
     }
 
     public void RemoveComListener(ComListener comListener) {
         listeners.remove(comListener);
     }
 
     protected void Log(ComEvent.eType type, Object... params) {
         ComFire(new ComEvent(this, type, params));
     }
 
     class AniAddEventHandler implements ComListener {
 
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
