 package com.crumbdev.chris.Danroth;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: chris
  * Date: 1/12/11
  * Time: 9:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Danroth {
     public static void main(String[] args)
     {
         Boolean usenickserv = false;
         String nickserv = "";
         String server = "irc.esper.net";
         int port = 6667;
         List channels = new ArrayList();
         String nick = "Danroth";
         String ident = "Danroth";
         for(int i = 0; i < args.length; i++)
         {
             if(args[i].toLowerCase().equalsIgnoreCase("--help"))
             {
                 System.out.println("Help page here :P");
                 System.out.println("Arguments list:");
                 System.out.println("\t--help                        Shows this page");
                 System.out.println("\t--nickserv=[Password]         Identifies the bot to nickserv upon connect using the specified password");
                 System.out.println("\t--server=[IRC Server Address] The address of the IRC server. Default Esper");
                 System.out.println("\t--port=[Port]                 The port to connect to the server using. Default 6667");
                 System.out.println("\t--nick=[Nick]                 The nickname for the bot to use");
                 System.out.println("\t--ident=[Ident]               The ident/username for the bot to use");
                 System.out.println("");
                 System.exit(0);
                 return;
             }
         }
         for(int i = 0; i < args.length; i++)
         {
             //System.out.println(args[i]);
             if(args[i].toLowerCase().startsWith("--nickserv="))
             {
                 usenickserv = true;
                 nickserv = args[i].substring("--nickserv=".length(), args[i].length());
                 System.out.println("Using NickServ authentication.");
             }
             else if(args[i].toLowerCase().startsWith("--server="))
             {
                 server = args[i].toLowerCase().substring("--server=".length(), args[i].length());
                 System.out.println("Using " + server + " as the server.");
             }
             else if(args[i].toLowerCase().startsWith("--port="))
             {
                 port = Integer.parseInt(args[i].substring("--port=".length(), args[i].length()));
                 System.out.println("Using port " + port);
             }
             else if(args[i].toLowerCase().startsWith("--nick="))
             {
                 nick = args[i].substring("--nick=".length(), args[i].length());
                 System.out.println("Using nick " + nick);
             }
             else if(args[i].toLowerCase().startsWith("--ident="))
             {
                 nick = args[i].substring("--ident=".length(), args[i].length());
                 System.out.println("Using ident " + ident);
             }
            else if(args[i].startsWith("--channel="))
             {
                channels.add(args[i].substring("--channel=".length()));
                 System.out.println("Channel: " + args[i]);
             }
         }
         System.out.println("Channel List");
         for(int i = 0; i < channels.toArray().length; i++)
         {
             System.out.println("\t" + channels.toArray()[i]);
         }
     }
 }
