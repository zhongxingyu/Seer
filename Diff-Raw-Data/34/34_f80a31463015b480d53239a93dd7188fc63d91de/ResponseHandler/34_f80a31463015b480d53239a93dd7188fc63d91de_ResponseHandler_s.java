 package client;
 
 import protocol.Registration.LoginResponse;
 import protocol.Response;
 import protocol.RoomResponse.JoinedRoomResponse;
 import protocol.RoomResponse.LeftRoomResponse;
 import protocol.SendMessageRequest;
 import protocol.ServerErrorResponse.Type;
 import protocol.ServerErrorResponse;
 import protocol.UserJoinOrLeaveRoomResponse;
 import protocol.UsersInRoomResponse;
 
 /**
  * Represents a RequestHandler thread
  */
 public class ResponseHandler implements Runnable {
     
     private ChatSession session;
     private ResponseHandlerVisitor visitor;
 
     class ResponseHandlerVisitor implements ClientVisitor<Void> {
         /**
          * Creates a new ResponseHandlerVisitor object
          */
         public ResponseHandlerVisitor() {
         }
 
         /**
          * Sends Message to other users in room.
          */
         @Override
         public synchronized Void visit(LoginResponse response) {
             //given that we have received a loginresponse
             //we know that the client was successfully logged in
             session.gui.setIsConnected(true);
             session.gui.writeToWindow("System Message: You have been successfully " +
                                       "logged in with the username " + response.getUsername());
             return null;
         }
 
         @Override
         public Void visit(JoinedRoomResponse response) {
             //create chat window
             ChatWindow c = new ChatWindow(response.getRoomName());
             //add window to session
             session.addChatWindow(c);
             return null;
         }
 
         @Override
         public Void visit(LeftRoomResponse response) {
             //TODO
             return null;
         }
 
         @Override
         public Void visit(ServerErrorResponse response) {
             if (response.getType().equals(Type.LOGIN_TAKEN)) {
                 session.gui.writeToWindow("System Message: Login failed. " +
                                           "The username you requested is taken.");
             } else if (response.getType().equals(Type.UNAUTHORIZED)) {
                 session.gui.writeToWindow("System Message: " + response.getError());
             }
             return null;
         }
 
         @Override
         public Void visit(SendMessageRequest response) {
             if (!response.getUsername().equals(session.getUsername())) { //ensure the message wasn't sent by you
                 //add the message and render it in the gui if the 
                 //chat window is the current window
                 //otherwise adjust the table on the right of the gui
                session.getChatWindows().get(response.getRoomName()).addMessage(response.getMessage(),session.gui);
             }
             return null;
         }
 
         @Override
         public Void visit(UsersInRoomResponse response) {
             // TODO Auto-generated method stub
             return null;
         }
 
         @Override
         public Void visit(UserJoinOrLeaveRoomResponse response) {
            ChatWindow c = session.getChatWindows().get(response.getRoomName());
             String username = response.getUsername();
             if (response.isJoining()) { //joining room
                 c.addUser(username, session.gui);
             } else { //exiting room
                 c.removeUser(username, session.gui);
             }
             return null;
         }
     }
     
     /**
      * Creates a new RequestHandler object
      * @param requestQueue the blocking queue of requests
      * @param users the map of users connected to the server
      */
     public ResponseHandler(ChatSession session) {
         this.session = session;
         this.visitor = new ResponseHandlerVisitor();
     }
     
     /**
      * Stops the response handler
      */
     public void stop() {
         //TODO
     }
     
     @Override
     public void run() {
         try {
             while (true) {
                 Response response = session.responseQueue.take();
                 response.accept(this.visitor);
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
