 package chatServer;
 
 import chatServer.message.Broadcast;
 import chatServer.message.Message;
 import chatServer.message.Notice;
 
 import java.util.EnumSet;
 import java.util.Vector;
 
 /**
  * User: Piotr Kapera
  * Date: 2010-06-02
  * Time: 21:26:57
  */
 public class Channel {
     private String name;
     private String topic;
     private EnumSet<ChannelMode> modes;
     private final Vector<Session> sessions = new Vector<Session>();
 
     public Channel(String name) {
         this.name = name;
     }
 
     public Channel(String name, String topic) {
         this.name = name;
         this.topic = topic;
     }
 
     public String getName() {
         return name;
     }
 
     public String getTopic() {
         return topic;
     }
 
     public void setTopic(String topic) {
         this.topic = topic;
     }
 
     public EnumSet<ChannelMode> getModes() {
         return modes;
     }
 
     public void addSession(Session session) {
         getSessions().add(session);
     }
 
    public void removeSession(Session session) {
        getSessions().remove(session);
    }

     /**
      *
      * @param id
      * @return
      */
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
      * @param msg
      */
     public void send(Broadcast msg) {
         synchronized (sessions) {
             for (Session session : sessions) {
                 if (!session.equals(msg.getSender())) {
                     session.send(msg.toString());
                 }
             }
         }
     }
 
     /**
      * @return a string representation of the object.
      */
     @Override
     public String toString() {
         return name;
     }
 
     public Vector<Session> getSessions() {
         return sessions;
     }
 }
