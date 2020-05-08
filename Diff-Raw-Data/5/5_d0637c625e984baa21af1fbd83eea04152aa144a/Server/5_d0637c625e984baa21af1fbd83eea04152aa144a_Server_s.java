 /**
  * Project 6 -- Server
  * A server that interacts with the Request and Response GUI's.  The server itself has a CLI and can be 
  * terminated by typing exit into the console.  To run the server requires one argmuent, the port it is
  * to run on.  
  *
  * @author Alex Aralis
  *
  * @recitation 004 Julian James Stephen
  *
  * @date 11/4/12
  *
  */
 
 import edu.purdue.cs.cs180.channel.*;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 class Server{
   public TCPChannel channel;//probably should have a get method for the request and responder channels but made them public insead because im lazy
   private LinkedList<Integer> requestersID;//list of backlogged requesters
   private LinkedList<Integer> respondersID;//list of backlogged responders
   private LinkedList<Location> requestersLocation;//list of places that the requesters are located at.  Corresponds to the requester ID list
   private LinkedList<String> respondersTeam;//list of teams that the responders are on.  Corresponds to the responder ID list.
   
   public Server(int port){
     channel = new TCPChannel(port);//init of all the object variables for the server
     requestersID = new LinkedList<Integer>();
     respondersID = new LinkedList<Integer>();
     requestersLocation = new LinkedList<Location>();
     respondersTeam = new LinkedList<String>();
     
     channel.setMessageListener(new MessageListener(){
       public void messageReceived(String message, int clientID){//when a message is received by a requester then...
         
         try{
           if(message.charAt(7) == ':'){//if message is from the requester then...
            Location location = encodeLocationString(message.substring(7, message.length())); //
             
             if(location == null){//if location was assigned a null value then...
               channel.sendMessage("The location you submitted was invalid!!!", clientID);//informs the requester that the location they submitted was invalid
             }
             
             assert (location != null) : "Requester message failure!!!"; //asserts that location must contain a value at this point
             
             if(respondersID.size() == 0){//if there are no responders on call then...
               channel.sendMessage("Searching:", clientID); //tells the requester that it needs to wait
               requestersID.add(clientID); //adds the requester ID to the backlog
               requestersLocation.add(location);//adds the requester location to the backlog
               
             }else{//if there are responders then...
               channel.sendMessage("Assigned:" + respondersTeam.removeFirst(), clientID);//sends a message to the requester telling them a responder has been assigned and what team that responder is on
               channel.sendMessage("Assigned:" + locationToString(location), respondersID.removeFirst());//tells the longest waiting responder they have been assigned and the location of the requester they are to help
             }
             
           }else if(message.charAt(8) == ':'){//if the message is from responders
            String teamString = message.substring(8, message.length());
             
             if(requestersID.size() == 0){//if there are no backlogged requesters then...
               channel.sendMessage("Searching:", clientID);//tell the responder they need to wait
               respondersID.add(clientID);//log the responder ID
               respondersTeam.add(teamString);//log the responders team
               
             }else{//if there are backlogged requesters then...
               channel.sendMessage("Assigned:" + teamString, requestersID.removeFirst());//tell the longest waiting requester a responder has been assigned and the team of that responder
               channel.sendMessage("Assigned:" + locationToString(requestersLocation.removeFirst()), clientID);//tell the responder they have been assigned and the location of the requester they are picking up
             }
           } else {//if the ':' was not found at either index 7 or 8 then...
             System.out.println("message format error. ':' in the wrong place!!!");
             channel.sendMessage("message format error. ':' in the wrong place!!!", clientID);
           }
         }catch(ChannelException e){//catch for all that channel stuff
           System.out.println("Exception occured while sending message!!!");
           e.printStackTrace();
         }
       }
     });
   }
   
   /**
    * Converts the passed string into a constant of enum Location
    * 
    * @param locationString 
    * the string of the location
    * 
    * @return Location
    * the location as a member of enum Location or null if the string didn't match anything
    */
   private Location encodeLocationString(String locationString){
     
     if (locationString.equals("CL50 - Class of 1950 Lecture Hall")){
       return Location.CL50;
     } else if (locationString.equals("EE - Electrical Engineering Building")){
       return Location.EE;
     } else if (locationString.equals("LWSN - Lawson Computer Science Building")){
       return Location.LWSN;
     } else if (locationString.equals("PMU - Purdue Memorial Union")){
       return Location.PMU;
     } else if (locationString.equals("PUSH - Purdue University Student Health Center")){
       return Location.PUSH;
     } else {
       return null;
     }
   }
   
   /**
    * Converts the passed constant of enum Location into a string to send to the Responer's client
    * 
    * @param Location loc
    * the enum Location to be converted to a string
    * 
    * @return String
    * the String to be passed to the Responder's client
    */
   private String locationToString(Location loc){
     switch (loc) {
       case CL50:
         return "CL50 - Class of 1950 Lecture Hall";
       case EE:
         return "EE - Electrical Engineering Building";
       case LWSN:
         return "LWSN - Lawson Computer Science Building";
       case PMU:
         return "PMU - Purdue Memorial Union";
       case PUSH:
         return "PUSH - Purdue University Student Health Center";
       default:
         return null;
     }
   }
   
   public static void main(String[] args){
     Scanner s = new Scanner(System.in);//to get input from the console
     Server server = new Server(Integer.parseInt(args[0]));//creates a server object that uses a port passed from the comsole
     
     while(true){//a loop that runs until "exit" is typed into the console
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
