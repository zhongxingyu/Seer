 package edu.berkeley.cs.cs162.Client;
 
 import java.io.IOException;
 import java.util.Random;
 
 import edu.berkeley.cs.cs162.Server.BoardLocation;
 import edu.berkeley.cs.cs162.Server.GoBoard;
 import edu.berkeley.cs.cs162.Writable.Location;
 import edu.berkeley.cs.cs162.Writable.MessageFactory;
 import edu.berkeley.cs.cs162.Writable.MessageProtocol;
 
 public class MachinePlayer extends Player {
 
     public MachinePlayer(String name) {
         super(name, MessageProtocol.TYPE_MACHINE);
     }
 
     public static void main(String[] args) {
 
         MachinePlayer player;
         String address;
         Integer port;
 
         try{
             player = new MachinePlayer(args[2]);
             address = args[0];
             port = Integer.valueOf(args[1]);
         }
         catch (Exception e){
             System.out.println("Enter arguments in the following format: <host> <port> <playername>");
             return;
         }
 
         if (player.connectTo(address, port)) {
             System.out.println("MachinePlayer " + player.getName() + " is connected to the server!");
             try {
                 player.runExecutionLoop();
             } catch (IOException e) {
                 System.out.println("An error occurred... MachinePlayer " + player.getName() + " terminating.");
             }
         }
     }
 
     private Location decideMove() {
         Random rng = new Random();
         int size = board.getCurrentBoard().getSize();
         BoardLocation loc = new BoardLocation(rng.nextInt(size), rng.nextInt(size));
         int chanceOfPass = 0;
 
         boolean valid = false;
         while (!valid) {
 
             //adds .5% of pass per try
             chanceOfPass += 5;
 
             if (chanceOfPass >= 10000 || rng.nextInt(10000 - chanceOfPass) == 0) {
                 return null;
             }
 
             loc = new BoardLocation(rng.nextInt(size), rng.nextInt(size));
 
             try {
                 board.testMove(loc, currentColor);
                 valid = true;
             } catch (GoBoard.IllegalMoveException e) {
 
             }
         }
 
         return MessageFactory.createLocationInfo(loc.getX(), loc.getY());
     }
 
     @Override
     protected void handleGetMove() throws IOException {
 
         byte moveCode;
 
         Location loc = decideMove();
 
         if (loc == null) {
             moveCode = MessageProtocol.MOVE_PASS;
             loc = MessageFactory.createLocationInfo(0, 0);
         } else {
             moveCode = MessageProtocol.MOVE_STONE;
         }
 
         getConnection().sendReplyToServer(MessageFactory.createGetMoveStatusOkMessage(moveCode, loc));
=======
        getConnection().sendReplyToServer(MessageFactory.createGetMoveStatusOkMessage(moveCode, loc));
     }
 }
