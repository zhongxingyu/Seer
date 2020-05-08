 package delightex.server;
 
 import delightex.client.Chat;
 import delightex.client.model.Message;
 import delightex.client.model.MessageSerializer;
 import delightex.client.model.User;
 import delightex.server.model.Model;
 import delightex.server.model.RoomContainer;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketServlet;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 public class ChatWebSocketServlet extends WebSocketServlet {
   public static final String MODEL_KEY = "model";
   public static final String USER_KEY = "user";
  public static final String USER_HACK_KEY = "hackuser";
 
   @Override
   public WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
     User user = (User) request.getSession().getAttribute(USER_KEY);
    if(user==null){
        final String userName = request.getParameter(USER_HACK_KEY);
        user = new User(userName);
    }
     final String roomName = request.getParameter(Chat.ROOM_KEY);
     String lastMessage = request.getParameter(Chat.STAMP_KEY);
 
     long lastStamp = lastMessage == null || lastMessage.isEmpty() ? 0 : Long.parseLong(lastMessage);
     if (user == null) return null;
 
     final Model model = (Model) getServletContext().getAttribute(MODEL_KEY);
     RoomContainer room = model.getRoom(roomName);
     if (room == null) return null;
     return new MyWebSocket(room, lastStamp, user);
   }
 
   @Override
   public void init() throws ServletException {
     super.init();
     Model model = new Model();
     getServletContext().setAttribute(MODEL_KEY, model);
   }
   
   private static class MyWebSocket implements WebSocket.OnTextMessage, RoomPort {
     private final RoomContainer myContainer;
     private final long myStamp;
     private final User myUser;
 
     private volatile Connection myConnection;
 
     MyWebSocket(RoomContainer container, long stamp, User user) {
       myContainer = container;
       myStamp = stamp;
       myUser = user;
     }
 
     @Override
     public void onOpen(Connection connection) {
       myConnection = connection;
       myContainer.addPort(this);
 
       List<Message> messages = myContainer.getMessageContainer().getMessages(myStamp);
       for (Message message : messages) {
         send(message);
       }
     }
 
     @Override
     public void onClose(int closeCode, String message) {
       myContainer.removePort(this);
     }
 
     @Override
     public void onMessage(String text) {
       myContainer.onMessage(new Message(myUser, text));
     }
 
     @Override
     public void send(Message message) {
       try {
         myConnection.sendMessage(MessageSerializer.toJson(message));
       } catch (Throwable t) {
         connectionBroken();
       }
     }
 
     @Override
     public User getUser() {
       return myUser;
     }
 
     private void connectionBroken() {
       if (myConnection != null && myConnection.isOpen()) {
         myConnection.close();
         myConnection = null;
       }
     }
   }
 }
