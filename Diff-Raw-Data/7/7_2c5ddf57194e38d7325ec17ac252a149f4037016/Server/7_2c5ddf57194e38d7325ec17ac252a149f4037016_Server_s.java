 import edu.purdue.cs.cs180.channel.*;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 class Server{
   public TCPChannel channel;//probably should have a get method for the request and responder channels but made them public insead because im lazy
   private LinkedList<Integer> requestersID;//list of backlogged requesters
   private LinkedList<Integer> respondersID;//list of backlogged responders
   private LinkedList<String> requestersLocation;//list of places that the requesters are located at.  Corresponds to the requester ID list
   private LinkedList<String> respondersTeam;//list of teams that the responders are on.  Corresponds to the responder ID list.
   
   public Server(int port){//init of all the object variables for the server
     channel = new TCPChannel(port);
     requestersID = new LinkedList<Integer>();
     respondersID = new LinkedList<Integer>();
     requestersLocation = new LinkedList<String>();
     respondersTeam = new LinkedList<String>();
     
     channel.setMessageListener(new MessageListener(){
       public void messageReceived(String message, int clientID){//when a message is received by a requester then...
         
         try{
           if(message.charAt(7) == ':'){//if message is from the requester then...
             String currentRequestersLocation = message.substring(7, message.length()); //removes the starting Request: from the recieved message string
             
             if(respondersID.size() == 0){//if there are no responders on call then...
               channel.sendMessage("Searching:", clientID); //tells the requester that it needs to wait
               requestersID.add(clientID); //adds the requester ID to the backlog
               requestersLocation.add(currentRequestersLocation);//adds the requester location to the backlog
               
             }else{//if there are no responders then...
               channel.sendMessage("Assigned:" + respondersTeam.removeFirst(), clientID);//sends a message to the requester telling them a responder has been assigned and what team that responder is on
               channel.sendMessage("Assigned:" + currentRequestersLocation, respondersID.removeFirst());//tells the longest waiting responder they have been assigned and the location of the requester they are to help
             }
             
           }else if(message.charAt(8) == ':'){//if the message is from responders
             String currentRespondersTeam = message.substring(8, message.length());//removes the stating Response: form the recieved message string
             
             if(requestersID.size() == 0){//if there are no backlogged requesters then...
               channel.sendMessage("Searching:", clientID);//tell the responder they need to wait
               respondersID.add(clientID);//log the responder ID
               respondersTeam.add(currentRespondersTeam);//log the responders team
               
             }else{//if there are backlogged requesters then...
               channel.sendMessage("Assigned:" + currentRespondersTeam, requestersID.removeFirst());//tell the longest waiting requester a responder has been assigned and the team of that responder
               channel.sendMessage("Assigned:" + requestersLocation.removeFirst(), clientID);//tell the responder they have been assigned and the location of the requester they are picking up
             }
           }else {
             System.out.println("message format error!!!");
           }
       }catch(ChannelException e){//catch for all that channel stuff
         System.out.println("message sending failure!!!");
           e.printStackTrace();
         }
       }
     });
   }
   
   public static void main(String[] args){
     Scanner s = new Scanner(System.in);
    Server server = new Server(8188);
     
     while(true){
       if(s.nextLine().equals("exit")){
         try{
           server.channel.close();
         }catch(ChannelException e){
           System.out.println("Channel failed to close");
           e.printStackTrace();
         }
         break;
       }
     }
   }
 }
 
