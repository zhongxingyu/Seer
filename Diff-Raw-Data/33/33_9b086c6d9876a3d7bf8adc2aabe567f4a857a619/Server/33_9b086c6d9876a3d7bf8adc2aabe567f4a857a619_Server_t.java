 package chatServer;
 
 import chatServer.message.Broadcast;
 import chatServer.message.Message;
 import chatServer.message.Notice;
 
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.*;
 import java.util.concurrent.ConcurrentHashMap;
 
 /**
  * User: Piotr Kapera
  * Date: 2010-05-10
  * Time: 20:00:05
  */
 public final class Server implements Runnable {
     private int port;
 
     private final Vector<Session> sessions = new Vector<Session>();
     private final Vector<Channel> channels = new Vector<Channel>();
 
     private final Channel defaultChannel = new Channel("default");
 
     public Server() {
         channels.add(defaultChannel);
     }
 
     /**
      *
      */
     public void run() {
         try {
             ServerSocket listener = new ServerSocket(port);
             Utils.log("Serwer uruchomiony na porcie: " + port);
             while (true) {
                 Socket socket = listener.accept();
 
                 Session session = new Session(socket, this);
                 sessions.add(session);
                 defaultChannel.addSession(session);
                 session.start();
             }
         } catch (Exception e) {
             System.out.println(e);
         }
     }
 
     /**
      * 
      * @return
      */
     public int getPort() {
         return port;
     }
 
     /**
      * 
      * @param port
      */
     public void setPort(int port) {
         this.port = port;
     }
 
     // TODO: implement priv message
     public void send(Message msg) {
         msg.getRecipient().send(msg.toString());
     }
 
     /**
      * 
      * @param nick
      * @return
      */
     public boolean checkIfNickOccupied(String nick) {
         for(Session session : sessions) {
             if (session.getNick().equals(nick)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * 
      * @param session
      */
     public void kill(Session session) {
         Utils.log("Killed: " + session);
        //session.close();
        
         sessions.remove(session);
     }
 
     public Session findSession(long id) {
         for (Session session : getSessions()) {
             if (session.getId() == id) {
                 return session;
             }
         }
         return null;
     }
 
     public Session findSession(String nick) {
         for (Session session : getSessions()) {
             if (session.getNick().equals(nick)) {
                 return session;
             }
         }
         return null;
     }
 
     /**
      * 
      * @return
      */
     public Vector<Session> getSessions() {
         return sessions;
     }
 
     public Vector<Session> getSessions(Channel channel) {
         if(!channels.contains(channel)) {
             return null;
         }
         return channel.getSessions();
     }
 
     /**
      * 
      * @return number of active sessions
      */
     public int getSessionsCount() {
         return sessions.size();
     }
 
     public Vector<Channel> getChannels() {
         return channels;
     }
 
     public Channel findChannel(String name) {
         for (Channel channel : getChannels()) {
             if (channel.getName().equals(name)) {
                 return channel;
             }
         }
         return null;
     }
 
     public void registerChannel(Channel channel) {
         channels.add(channel);
     }
 }
