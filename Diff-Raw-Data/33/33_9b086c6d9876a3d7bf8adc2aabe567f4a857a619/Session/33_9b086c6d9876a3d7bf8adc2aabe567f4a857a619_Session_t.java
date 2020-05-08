 package chatServer;
 
 import chatServer.command.CommandParser;
 import chatServer.message.Broadcast;
 import chatServer.message.Notice;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.EnumSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import static java.util.EnumSet.of;
 
 /**
  * User: Piotr Kapera
  * Date: 2010-05-10
  * Time: 19:42:14
  */
 public class Session extends Thread {
     private Socket socket;
     private BufferedReader in;
     private PrintWriter out;
     private Server server;
     private long id;
     private String nick = "";
     private boolean closed = false;
     /* TODO: EnumSet is not synchronized.
        If multiple threads access an enum set concurrently,
        and at least one of the threads modifies the set,
        it should be synchronized externally.
      */
     private EnumSet<UserMode> modes = EnumSet.of(UserMode.u);
     private final Vector<Channel> channels = new Vector<Channel>();
 
     /**
      *
      * @param socket
      * @param server
      */
     public Session(Socket socket, Server server) {
         this.socket = socket;
         this.server = server;
         id = Utils.crc32(socket.toString());
 
         try {
             in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();
         }
 
         Utils.log("Nowa sesja! ID:" + this + " (" + socket + ")");
        this.send(new Broadcast(this, "dołączył do chata!"));
         server.send(new Notice(this, "twój nick: " + getNick() + " aby zmienić użyj komendy /nick <nazwa>"));
 
         if(server.getSessionsCount() == 0) {
             modes.add(UserMode.o);
             Utils.log("Administrator ID:" + this);
             server.send(new Notice(this, "jesteś właścicielem kanału!"));
         }
     }
 
    private void send(Broadcast message) {
        for (Channel channel : channels) {
            channel.send(message);
        }
    }

     /**
      * 
      */
     public void run() {
         try {
             while (!closed) {
                 String line = in.readLine();
                 if(line == null || line.isEmpty()) {
                     closed = true;
                 }
                 else {
                     try {
                         CommandParser.getInstance().handle(server, this, line);
                     } catch(Exception e) {
                         Utils.log(e);
                     }
                 }
             }
         } catch (Exception e) {
             Utils.log(e);
         } finally {
             // TODO: refactor
            close();
             server.kill(this);
             Utils.log("Połączenie zostało zakończone: " + this);
         }
     }
 
     public void close() {
        Utils.log("Session close");
         for (Channel channel : channels) {
             channel.send(new Broadcast(this, "Opuściłem kanał"));
            channel.removeSession(this);
            System.out.println("Part: " + channel);
         }
         
         out.close();
         try {
             in.close();
             socket.close();
         } catch (IOException e) {
             Utils.log(e);
         }
     }
 
     /**
      * 
      * @param message
      */
     public void send(String message) {
         out.println(message);
     }
 
     /**
      * 
      * @return
      */
     public long getId() {
         return id;
     }
 
     /**
      * 
      * @return
      */
     public String getNick() {
         return nick.isEmpty() ? Long.toString(getId()) : nick;
     }
 
     /**
      * 
      * @param nick
      */
     public void setNick(String nick) {
         this.nick = nick;
     }
 
     /**
      * Returns a string representation of this session
      *
      * @return a string representation of this session.
      */
     @Override
     public String toString() {
         return "{" + getNick() + "}";
     }
 
     public EnumSet<UserMode> getModes() {
         return modes;
     }
 
     public Vector<Channel> getChannels() {
         return channels;
     }
 
     public void addChannel(Channel channel) {
         channels.add(channel);
     }
 }
