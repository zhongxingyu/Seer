 import net.tinyos.message.*;
 import net.tinyos.util.*;
 import java.io.*;
 import java.util.Scanner;
 
 
 
 public class TinyBlogClient implements MessageListener
 {
     short POST_TWEET = Constants.POST_TWEET;
     short ADD_USER   = Constants.ADD_USER;
     short GET_TWEETS = Constants.GET_TWEETS;
     short DIRECT_MESSAGE = Constants.DIRECT_MESSAGE;
 
     short RETURN_TWEETS = 4;
 
     MoteIF mote;
     int MYID = 0;
     int MOTEID = 0;
     short seqno = 1;
 
     private void cli(){
         Scanner input = new Scanner(System.in);
         System.out.print(">>");
         String command = input.next();
         while (!command.equals("quit")){
             if (command.equals("tweet")){
                 tweet(input.nextLine().trim());
             }
             else if (command.equals("get")){
                 getTweets();
             }
             else if (command.equals("follow")){
                 followUser(input.nextInt());
             } 
             else if(command.equals("direct")){
                 directMessage(input.nextInt(),input.nextLine().trim());
             }
             else if (command.equals("connect")){
                 MOTEID = input.nextInt();
             }
             else if (command.equals("help")){
                 System.out.println("Help:\n" +
                                    "tweet <message>\n" + 
                                    "get\t(gets followed tweets)\n" +
                                    "follow <id>\n" +
                                    "connect <id> (sets tweet node)");          
             }
             System.out.print(">>");
             command = input.next();
         }
         input.close();
         System.exit(0);
     }
 
     /* Main entry point */
     void run() {
         mote = new MoteIF(PrintStreamMessenger.err);
         mote.registerListener(new TinyBlogMsg(), this);
         cli();
     }
 
     short[] convertStringToShort(String s){
        short [] text = new short[s.length()+1];
        int i = 0;
        for (; i < s.length(); i++){
             text[i] = (new Integer(s.charAt(i))).shortValue();
         }
        text[i] = 4;
         return text;
     }
 
     String convertShortToString(short[] s, short len){
         String text = "";
         for (short i = 0; i < len; i++){
            if (s[i] == 4)continue;
             text += (char)s[i];
         }
         return text;
     }
 
     void followUser(int user){
         short[]data  = new short[1];
         data[0] = (short)user;
         System.out.printf("Node %d: Following user ID: %d\n", MOTEID, user);
         TinyBlogMsg msg = new TinyBlogMsg();
         msg.set_action(ADD_USER);
         msg.set_destMoteID(MOTEID);
         msg.set_data(data);
         sendMsg(msg);
     }
 
     void getTweets(){
         System.out.printf("Node %d: Getting tweets...\n",MOTEID);
         TinyBlogMsg msg = new TinyBlogMsg();
         msg.set_action(GET_TWEETS);
         msg.set_destMoteID(MOTEID);
         msg.set_nchars((short)0);
         sendMsg(msg);
     }
     void tweet(String text){
         if (text.length() >100){
             System.out.println("Tweet to long, needs to be < 101 chars");
             return;
         }
         System.out.println("Tweet: " + text);
         System.out.printf("Node %d: Sending tweet...", MOTEID);
         short[] data = convertStringToShort(text);
         short len = (short)data.length;
         TinyBlogMsg msg = new TinyBlogMsg();
         msg.set_action(POST_TWEET);
         msg.set_destMoteID(MOTEID);
         msg.set_data(data);
         msg.set_nchars(len);
         sendMsg(msg);
         System.out.println("sent!");
     }
 
     void directMessage(int dest, String text){
         if (text.length() >100){
             System.out.println("Tweet to long, needs to be < 101 chars");
             return;
         }
         System.out.println("Direct msg: " + text);
         System.out.printf("Node %d: Sending message...", MOTEID);
         short[] data = convertStringToShort(text);
         short len = (short)data.length;
         TinyBlogMsg msg = new TinyBlogMsg();
         msg.set_action(DIRECT_MESSAGE);
         msg.set_destMoteID(dest);
         msg.set_data(data);
         msg.set_nchars(len);
         sendMsg(msg);
         System.out.println("sent!");
 
     }
 
 
     public synchronized void messageReceived(int dest_addr, Message msg) {
         if (msg instanceof TinyBlogMsg) {
             TinyBlogMsg tbmsg = (TinyBlogMsg)msg;
             if (tbmsg.get_action() == RETURN_TWEETS){
                 System.out.printf("Node %d tweeted: %s\nMood = %d\nseqno: %d\n", tbmsg.get_sourceMoteID(), 
                     convertShortToString(tbmsg.get_data(),tbmsg.get_nchars()), tbmsg.get_mood(),tbmsg.get_seqno());
             } else if(tbmsg.get_action() == DIRECT_MESSAGE && tbmsg.get_destMoteID() == MYID ){
                 System.out.printf("Node %d direct messaged you: %s\n\nseqno: %d\n", tbmsg.get_sourceMoteID(), 
                     convertShortToString(tbmsg.get_data(),tbmsg.get_nchars()),tbmsg.get_seqno());
             }else if (tbmsg.get_sourceMoteID() != MOTEID ){
                 //System.out.println("Received a msg");
                 return;
             }
         }
         System.out.print(">>");
     }
 
     /* The user wants to set the interval to newPeriod. Refuse bogus values
        and return false, or accept the change, broadcast it, and return
        true */
 
     /* Broadcast a version+interval message. */
     void sendMsg(TinyBlogMsg msg) {
 
         msg.set_sourceMoteID(MYID);
         msg.set_seqno(seqno++);
         msg.set_hopCount((short)6);
         try {
             mote.send(MOTEID, msg);
         }
         catch (IOException e) {
             //System.err.out("Cannot send message to mote");
         }
     }
 
     /* User wants to clear all data. */
 
 
     public static void main(String[] args) {
         TinyBlogClient me = new TinyBlogClient();
         me.run();
     }
 }
