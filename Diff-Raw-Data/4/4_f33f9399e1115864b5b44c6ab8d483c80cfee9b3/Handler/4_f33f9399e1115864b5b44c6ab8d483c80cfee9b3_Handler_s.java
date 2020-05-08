 package co.raawr.cah;
 
 public class Handler {
 
     static Main cah;
 
     public static void init(Main c) {
         cah = c;
     }
 
     public static void handleMessage(String nick, String channel, String message) {
 
         String[] parse = message.split(" ");
 
         String command = parse[0].toLowerCase();
 
         if (command.startsWith(".")) {
             if (command.equals(".cah")) {
                 if (parse.length > 1) {
                     if (isInteger(parse[1])) {
                         CAH.prepGame(Integer.parseInt(parse[1]), CAH.createPlayer(nick));
                     }
                 }
             } else if (channel.equals("#cah")) {
                 // It's a command - handle it
                 switch (command) {
                     case ".join":
                         CAH.addPlayer(CAH.createPlayer(nick));
                         break;
                     case ".leave":
                     case ".quit":
                         CAH.removePlayer(CAH.lookupPlayer(nick));
                         break;
                     case ".start":
                         CAH.begin(CAH.lookupPlayer(nick));
                         break;
                     case ".end":
                         CAH.endGame(CAH.lookupPlayer(nick));
                         break;
                     case ".score":
                         CAH.getScore(CAH.lookupPlayer(nick));
                         break;
                     case ".stop":
                         CAH.endGame(CAH.lookupPlayer(nick));
                         break;
                     case ".sudo":
                         //sudo(nick, parse[1], channel, parse[2]);
                         break;
                     case ".scan":
                     case ".rescan":
                         //CAH.rescanCards();
                         break;
                     case ".remove":
                     case ".kick":
                         if (parse.length == 2) {
                             CAH.kickPlayer(CAH.lookupPlayer(nick), CAH.lookupPlayer(parse[1]));
                         }
                         break;
                     default:
                         break;
                 }
             }
         } else if (isInteger(command)) {
             // It's the czar picking a card
             CAH.czarPickCard(CAH.lookupPlayer(nick), Integer.parseInt(command));
         }
 
     }
 
     public static void sudo(String sender, String nick, String channel, String command) {
         if (sender.equals("Rurigok") || sender.equals("Tako")) {
             // Disabled for the time being
             handleMessage(nick, channel, command);
         }
     }
 
     public static void handlePM(String nick, String message) {
         if (isInteger(message)) {
             // Player picked their card
             CAH.pickCard(CAH.lookupPlayer(nick), Integer.parseInt(message));
         }
     }
 
     public static boolean isInteger(String s) {
         try {
             Integer.parseInt(s);
             return true;
         } catch (NumberFormatException e) {
             return false;
         }
     }
 }
