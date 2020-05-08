 package com.titankingdoms.dev.TitanIRC;
 
 import com.titankingdoms.dev.TitanIRC.api.IRCChannel;
 import com.titankingdoms.dev.TitanIRC.api.IRCServer;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserJoinChannelEvent;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserMessageChannelEvent;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserPartChannelEvent;
 import com.titankingdoms.dev.TitanIRC.api.event.IRCUserQuitServerEvent;
 
 import java.io.*;
 import java.net.Socket;
 import java.util.ArrayList;
 
 /**
  * Copyright (C) 2012 Chris Ward
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 public class ServerConnection implements Runnable {
     public String getAddress() { return address; }
     public int getPort() { return port; }
     public String getBotname () { return botname; }
 
     private String address;
     private int port;
     public ArrayList<String> channels = new ArrayList<String>();
     private String botname;
     private TitanIRC instance;
     public ServerConnection(String botname, String address, int port, String[] channels, TitanIRC instance)
     {
         this.address = address;
         this.port = port;
         for(String chan : channels)
             this.channels.add(chan);
         this.botname = botname;
         this.instance = instance;
     }
 
     private Socket socket;
     BufferedReader reader;
     PrintWriter writer;
 
     private boolean breakwhile;
 
     public void run()
     {
         try {
             socket = new Socket(address, port);
             reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
         }
         catch(IOException e)
         {
             instance.getLogger().severe("An error occured (" + e.getMessage() +") while connecting to " + address + ":" + port);
             close();
             return;
         }
         changeNick(botname);
         writeln("USER TitanIRC 0 * :TitanIRC Bot");
         breakwhile = true;
         while(breakwhile)
         {
             String read;
             try {
                 read = reader.readLine();
             }
             catch (Exception e)
             {
                 close();
                 return;
             }
 
             if(read.split(" ")[0].equalsIgnoreCase("PING"))
                 writeln("PONG " + read.split(" ")[1]);
             else if(read.split(" ")[0].equalsIgnoreCase("ERROR"))
                 close();
             else if(read.split(" ")[1].equalsIgnoreCase("001"))            {
                 for(Object channel : channels.toArray())
                     writeln("JOIN " + channel);
                 channels.clear();
             }
             else if(read.split(" ")[1].equalsIgnoreCase("JOIN"))
             {
                 if(read.split("!")[0].substring(1).equals(botname))
                    channels.add(read.split(" ")[2]);
                 else
                     instance.getServer().getPluginManager().callEvent(new IRCUserJoinChannelEvent(read.split(" ")[0], new IRCChannel(read.split(" ")[2], new IRCServer(this))));
             }
             else if(read.split(" ")[1].equalsIgnoreCase("PART"))
             {
                 if(read.split("!")[0].substring(1).equals(botname))
                     channels.remove(read.split(" ")[2]);
                 else
                     instance.getServer().getPluginManager().callEvent(new IRCUserPartChannelEvent(read.split(" ")[0], new IRCChannel(read.split(" ")[2], new IRCServer(this))));
             }
             else if(read.split(" ")[1].equalsIgnoreCase("QUIT"))
             {
                 instance.getServer().getPluginManager().callEvent(new IRCUserQuitServerEvent(read.split(" ")[0], new IRCServer(this)));
             }
             else if(read.split(" ")[1].equalsIgnoreCase("PRIVMSG"))
             {
                 instance.getServer().getPluginManager().callEvent(new IRCUserMessageChannelEvent(read.split(" ")[0], read.substring(1).split(":")[1], new IRCChannel(read.split(" ")[2], new IRCServer(this))));
             }
             //instance.debug(read);
         }
     }
 
     private void close()
     {
         try { socket.close(); } catch (Exception eX) {}
         try { reader.close(); } catch (Exception eX) {}
         try { writer.close(); } catch (Exception eX) {}
     }
 
     public void writeln(String message)
     {
         try{
             writer.write(message + "\n");
             writer.flush();
         }
         catch (Exception e) { close(); }
     }
 
     public void changeNick(String nick)
     {
         writeln("NICK " + nick);
     }
 
     public void disconnect()
     {
         writeln("QUIT :TitanIRC v" + instance.getDescription().getVersion());
         close();
     }
 
     public void connect()
     {
         run();
     }
 
     public void reconnect()
     {
         breakwhile = false;
         disconnect();
         connect();
     }
 
 
 }
