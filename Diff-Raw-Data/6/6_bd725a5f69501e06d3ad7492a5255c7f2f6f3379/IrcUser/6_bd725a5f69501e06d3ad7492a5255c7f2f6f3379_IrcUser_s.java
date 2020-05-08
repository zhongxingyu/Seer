 package org.openstatic.irc;
 
 import java.util.Enumeration;
 import java.util.Vector;
 
 public class IrcUser extends Thread
 {   
     private String username;
     private String nickname;
     private String realname;
     private String away_message;
     private String password;
     private String client_host;
     private String domain;
     private boolean welcomed;
     private int idle_time;
     private boolean stay_connected;
     private IrcServer server;
     private GatewayConnection connection;
     
     public IrcUser(IrcServer server)
     {
         this.nickname = null;
         this.realname = null;
         this.username = null;
         this.away_message = null;
         this.welcomed = false;
         this.server = server;
     }
         
     public void initGatewayConnection(GatewayConnection connection)
     {
         this.connection = connection;
         this.stay_connected = true;
         Thread idleClock = new Thread()
         {
             public void run()
             {
                 while (IrcUser.this.stay_connected)
                 {
                     IrcUser.this.idle_time++;
                     try
                     {
                         Thread.sleep(1000);
                     } catch (Exception vxc) {}
                 }
             }
         };
         idleClock.start();
         this.server.log(this.connection.getClientHostname(), 1, "IrcUser Class initGatewayConnection()");
     }
     
     public void disconnect()
     {
         this.server.log(this.username, 1, "IrcUser Class " + this.nickname + " disconnect()");
         this.connection.close();
         this.stay_connected = false;
         this.server.removeUser(this);
     }
     
     private String stringVector(Vector<String> vec, String sep)
     {
         StringBuffer new_string = new StringBuffer("");
         for (Enumeration<String> e = vec.elements(); e.hasMoreElements(); )
         {
             String ca = e.nextElement();
             new_string.append(ca);
             if (e.hasMoreElements())
             {
                 new_string.append(sep);
             }
         }
         return new_string.toString();
     }
     
     public void onGatewayCommand(IRCMessage cmd)
     {
         // we only want to set the command source if the user is fully authenticated.
         if (this.isWelcomed())
         {
             cmd.setSource(this.toString());
         }
         
         // Reset idle time on activity, as long as its not a ping, pong or ison
         if (!cmd.is("PING") && !cmd.is("PONG") && !cmd.is("ISON"))
         {
             this.idle_time = 0;
         }
         
         // Here is where we process the main commands
         if (cmd.is("NICK")) {
             if (this.server.findUser(cmd.getArg(0)) == null)
             {
                 if (this.isWelcomed())
                 {
                     sendCommand(new IRCMessage(cmd, this));
                     for(Enumeration<IrcChannel> e = this.server.getChannels().elements(); e.hasMoreElements(); )
                     {
                         IrcChannel chan = e.nextElement();
                         chan.updateNick(this, cmd.getArg(0));
                     }
                 }
                 this.setNick(cmd.getArg(0));
             } else {
                 sendResponse("433", ":" + cmd.getArg(0) + " is already in use");
             }
         } else if (cmd.is("KICK")) {
             if (cmd.argCount() >= 2)
             {
                 IrcChannel desired_channel = this.server.findChannel(cmd.getArg(0));
                 {
                     desired_channel.getHandler().onCommand(cmd);
                 }
             }
         } else if (cmd.is("INVITE")) {
             if (cmd.argCount() >= 2)
             {
                 IrcChannel desired_channel = this.server.findChannel(cmd.getArg(1));
                 if (desired_channel != null)
                 {
                     desired_channel.getHandler().onCommand(cmd);
                 }
             }
         } else if (cmd.is("JOIN") || cmd.is("PART") || cmd.is("WHO") ) {
             if (cmd.argCount() >= 1)
             {
                 String[] rooms = cmd.getArg(0).split(",");
                 for (int rms = 0; rms < rooms.length; rms++)
                 {
                     IrcChannel desired_channel = this.server.findChannel(rooms[rms]);
                     if (desired_channel != null)
                     {
                         if (cmd.is("JOIN"))
                             desired_channel.pendingJoin(this);
                         desired_channel.getHandler().onCommand(cmd);
                     } else if (cmd.is("JOIN")) {
                        sendResponse("403", rooms[rms] + " :No IrcChannel Class to handle this request");
                     }
                 }
             }
             
         } else if (cmd.is("TOPIC")) {
             if (cmd.argCount() >= 2)
             {
                 IrcChannel desired_channel = this.server.findChannel(cmd.getArg(0));
                 if (desired_channel != null)
                 {
                     desired_channel.getHandler().onCommand(cmd);
                 }
             }
         } else if (cmd.is("MODE")) {
             // channels require three arguments
             if (cmd.argCount() >= 3 && (cmd.getArg(0).startsWith("#") || cmd.getArg(0).startsWith("&") || cmd.getArg(0).startsWith("!") || cmd.getArg(0).startsWith("+")))
             {
                 IrcChannel desired_channel = this.server.findChannel(cmd.getArg(0));
                 if (desired_channel != null)
                 {
                     desired_channel.getHandler().onCommand(cmd);
                 }
 
             }
         } else if (cmd.is("AWAY")) {
             if (cmd.argCount() >= 1)
             {
                 this.setAway(cmd.getArg(0));
                 sendResponse("306", ":You are now marked as away");
             } else {
                 this.setAway(null);
                 sendResponse("305", ":You are no longer marked as away");
             }
         } else if (cmd.is("PING")) {
             IRCMessage pong = IRCMessage.prepare("PONG");
             pong.addArg(cmd.getArg(0));
             sendCommand(pong);
         } else if (cmd.is("ISON")) {
             Vector<String> on_nicks = new Vector<String>();
             for (int ison_n = 0; ison_n < cmd.argCount(); ison_n++)
             {
                 IrcUser ison = this.server.findUser(cmd.getArg(ison_n));
                 if (ison != null)
                 {
                     on_nicks.add(cmd.getArg(ison_n));
                 }
             }
             sendResponse("303", stringVector(on_nicks, " "));
         } else if (cmd.is("WHOIS")) {
             IrcUser wi = this.server.findUser(cmd.getArg(0));
             if (wi != null)
             {
                 sendResponse("311", wi.getNick() + " " + wi.getUserName() + " " + wi.getClientHostname() + " * :" + wi.getRealName());
                 sendResponse("312", wi.getNick() + " " + wi.getServerHostname() + " :IRC Server");
                 //sendResponse("338", wi.getNick() + " 255.255.255.255 :actually using host");
                 if (wi.getAway() != null)
                 {
                     sendResponse("301", wi.getNick() + " :" + wi.getAway());
                 }
                 sendResponse("317", wi.getNick() + " " + String.valueOf(wi.getIdleTime()) + " :Seconds Idle");
             }
             sendResponse("318", ":End of WHOIS list");
         } else if (cmd.is("LIST")) {
             // TODO: Add support For channel filtering
             for (Enumeration<IrcChannel> e = this.server.getChannels().elements(); e.hasMoreElements(); )
             {
                 IrcChannel room = e.nextElement();
                 sendResponse("322", room.getName() + " " + String.valueOf(room.getMemberCount()) + " :" + room.getTopic());
             }
             sendResponse("323", ":End of List");
             
         } else if (cmd.is("PRIVMSG") || cmd.is("NOTICE")) {
             if (cmd.getArg(0).startsWith("$"))
             {
                 // todo
             } else if (cmd.getArg(0).startsWith("#") || cmd.getArg(0).startsWith("&") || cmd.getArg(0).startsWith("!") || cmd.getArg(0).startsWith("+")) {
                 IrcChannel possible_target = this.server.findChannel(cmd.getArg(0));
                 if (possible_target != null)
                 {
                     possible_target.getHandler().onCommand(cmd);
                 } else {
                     sendResponse("401", cmd.getArg(0) + " :No such nick/channel");
                 }
             } else {
                 IrcUser possible_target2 = this.server.findUser(cmd.getArg(0));
                 if (possible_target2 != null)
                 {
                     if (possible_target2.getAway() != null)
                     {
                         sendResponse("301", possible_target2.getNick() + " :" + possible_target2.getAway());
                     }
                     possible_target2.sendCommand(new IRCMessage(cmd, this));
                 } else {
                     sendResponse("401", cmd.getArg(0) + " :No such nick/channel");
                 }
             }
         } else if (cmd.is("QUIT")) {
             this.disconnect();
         } else if (cmd.is("USER")) {
             this.processUser(cmd.getArgs());
         } else if (cmd.is("PONG")) {
             // just to stop the else
         } else {
             sendResponse("421", cmd.getCommand() + " :Unknown Command");
         }
         
         if (this.isReady() && !this.isWelcomed())
         {
             String chanCount = String.valueOf(this.server.getChannelCount());
             String connCount = String.valueOf(this.server.getUserCount());
             Vector<String> motd = this.server.getMotd();
             sendResponse("001", ":Welcome to the Internet Relay Chat Network");
             sendResponse("002", ":Your host is " + this.getServerHostname() + ", running version org.openstatic.irc");
             sendResponse("003", ":This server was created on  " + this.server.getBootTime());
             sendResponse("004", this.getServerHostname() + " org.openstatic.irc * *");
             sendResponse("251", ":There are " + connCount + " users on 1 server");
             sendResponse("252", "0 :IRC Operators online");
             sendResponse("254", chanCount + " :channels formed");
             sendResponse("255", ":I have " + connCount + " clients and 0 servers");
             sendResponse("265", ":Current local users: " + connCount + " Max: 0");
             sendResponse("266", ":Current global users: 0 Max: 0");
             sendResponse("250", ":Highest connection count: " + connCount + " (" + connCount + " clients)");
             sendResponse("375", ":- " + this.getServerHostname() + " Message of the day -");
             if (motd != null)
             {
                 for (Enumeration<String> motd_e = motd.elements(); motd_e.hasMoreElements(); )
                 {
                     sendResponse("372", ":- " + motd_e.nextElement());
                 }
                 
             }
             sendResponse("376", ":End of MOTD command");
             this.setWelcomed(true);
         }
     }
     
     
     // Is Block, booleans and stuff
     public boolean hasNick()
     {
         if (this.nickname != null)
         {
             return true;
         } else {
             return false;
         }
     }
     
     public boolean isReady()
     {
         if (this.username != null && this.nickname != null)
         {
             return true;
         } else {
             return false;
         }
     }
     
     public boolean isWelcomed()
     {
         return this.welcomed;
     }
     
     public boolean is(String value)
     {
         if (this.nickname != null)
         {
             if (this.nickname.equals(value) || this.toString().equals(value))
                 return true;
             else
                 return false;
         } else {
             return false;
         }
     }
     
     // Get Methods
     public int getIdleTime()
     {
         return this.idle_time;
     }
     
     public String getServerHostname()
     {
         return this.connection.getServerHostname();
     }
     
     public String getClientHostname()
     {
         return this.client_host;
     }
     
     public GatewayConnection getGatewayConnection()
     {
         return this.connection;
     }
     
     public IrcServer getIrcServer()
     {
         return this.server;
     }
     
     public String getRealName()
     {
         return this.realname;
     }
     
     public String getNick()
     {
         return this.nickname;
     }
     
     public String getAway()
     {
         return this.away_message;
     }
     
     public String getUserName()
     {
         return this.username;
     }
     
     // Set Methods
     public void setWelcomed(boolean bool)
     {
         this.welcomed = bool;
     }
     
     public void setAway(String value)
     {
         this.away_message = value;
     }
     
     public void setNick(String nick)
     {
         this.nickname = nick;
     }
     
     public void setRealName(String realname)
     {
         this.realname = realname;
     }
     
     public void setUserName(String username)
     {
         this.username = username;
     }
 
     public void setClientHost(String value)
     {
         this.client_host = value;
     }
     
     // Action functions
     public void sendCommand(IRCMessage pc)
     {
         this.connection.sendCommand(pc);
     }
     
     public void sendResponse(String response, String args)
     {
         this.connection.sendResponse(response, args);
     }
     
     private void processUser(Vector<String> args)
     {
         if (args.size() == 4)
         {
             this.username = args.elementAt(0);
             this.domain = args.elementAt(1).replaceAll("\"","");
             this.client_host = args.elementAt(2).replaceAll("\"","");
             this.realname = args.elementAt(3);
         }
     }
     
     public void loginUser(String username, String password)
     {
         this.server.log(this.connection.getClientHostname(), 1, "IrcUser Manual Authentication (" + username + ")");
         this.username = username;
         this.nickname = username;
         this.password = password;
         this.welcomed = true;
         
     }
     
     public String toString()
     {
         return this.nickname + "!" + this.username + "@" + this.getClientHostname();
     }
 }
