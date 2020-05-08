 /**
  * IRCMessageProcessor.java
  */
 package de.ekdev.ekirc.core;
 
 import static de.ekdev.ekirc.core.IRCNumericServerReply.*;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Objects;
 
 import de.ekdev.ekirc.core.event.ActionMessageToChannelEvent;
 import de.ekdev.ekirc.core.event.ActionMessageToUserEvent;
 import de.ekdev.ekirc.core.event.IRCChannelInfoEvent;
 import de.ekdev.ekirc.core.event.ChannelListUpdateEvent;
 import de.ekdev.ekirc.core.event.ChannelModeChangeEvent;
 import de.ekdev.ekirc.core.event.ChannelModeUpdateEvent;
 import de.ekdev.ekirc.core.event.ChannelTopicUpdateEvent;
 import de.ekdev.ekirc.core.event.IRCNetworkInfoEvent;
 import de.ekdev.ekirc.core.event.JoinEvent;
 import de.ekdev.ekirc.core.event.KickEvent;
 import de.ekdev.ekirc.core.event.MotdUpdatedEvent;
 import de.ekdev.ekirc.core.event.MotdUpdatingEvent;
 import de.ekdev.ekirc.core.event.NickAlreadyInUseEvent;
 import de.ekdev.ekirc.core.event.NickChangeEvent;
 import de.ekdev.ekirc.core.event.NoticeToChannelEvent;
 import de.ekdev.ekirc.core.event.NoticeToUserEvent;
 import de.ekdev.ekirc.core.event.PartEvent;
 import de.ekdev.ekirc.core.event.PingEvent;
 import de.ekdev.ekirc.core.event.PrivateMessageToChannelEvent;
 import de.ekdev.ekirc.core.event.PrivateMessageToUserEvent;
 import de.ekdev.ekirc.core.event.QuitEvent;
 import de.ekdev.ekirc.core.event.UnknownCTCPCommandEvent;
 import de.ekdev.ekirc.core.event.UnknownDCCCommandEvent;
 import de.ekdev.ekirc.core.event.UnknownServerCommandEvent;
 import de.ekdev.ekirc.core.event.UserInfoUpdateEvent;
 import de.ekdev.ekirc.core.event.UserModeChangeEvent;
 
 /**
  * @author ekDev
  */
 public class IRCMessageProcessor
 {
     // TODO: work with chars?
     public final static String MQUOTE = "\u0010";
     public final static String CTCP_XDELIM = "\u0001";
     public final static String CTCP_XQUOTE = "\\"; // "\u005C\u005C";
     // http://www.robelle.com/smugbook/ascii.html
 
     private final IRCNetwork ircNetwork;
 
     private IRCChannelList.Builder ircChannelListBuilder;
 
     // ------------------------------------------------------------------------
 
     public IRCMessageProcessor(IRCNetwork ircNetwork) throws NullPointerException
     {
         Objects.requireNonNull(ircNetwork, "ircNetwork must not be null!");
 
         this.ircNetwork = ircNetwork;
 
         this.ircChannelListBuilder = new IRCChannelList.Builder();
     }
 
     // ------------------------------------------------------------------------
 
     protected IRCMessage parseRawLine(String line) throws IRCMessageFormatException
     {
         // throw more exceptions?
         if (line == null || line.length() == 0) return null;
 
         int i = line.indexOf(IRCMessage.IRC_SPACE);
 
         // optional prefix
         String prefix = null;
         if (line.charAt(0) == IRCMessage.IRC_COLON)
         {
             // only prefix? -> not allowed
             if (i == -1)
             {
                 throw new IRCMessageFormatException("IRC message with a prefix only!");
             }
             // colon and prefix have to stand together
             if (line.charAt(1) == IRCMessage.IRC_SPACE)
             {
                 throw new IRCMessageFormatException(
                         "Wrong IRC message format! after prefix colon can't follow a space.");
             }
 
             prefix = line.substring(1, i);
             line = line.substring(i + 1);
             i = line.indexOf(IRCMessage.IRC_SPACE);
         }
 
         String command;
         if (i == -1)
         {
             command = line;
             line = "";
         }
         else
         {
             command = line.substring(0, i);
             line = line.substring(i + 1);
         }
 
         List<String> params = new ArrayList<String>(15);
         while (line.length() > 0)
         {
             if (line.charAt(0) == IRCMessage.IRC_COLON)
             {
                 // remove colon of trailing parameter
                 params.add(line.substring(1));
                 break;
             }
             i = line.indexOf(IRCMessage.IRC_SPACE);
             if (i == -1)
             {
                 params.add(line);
                 break;
             }
             else
             {
                 params.add(line.substring(0, i));
                 line = line.substring(i + 1);
             }
         }
         if (params.size() > IRCMessage.MAX_PARAM_COUNT)
         {
             throw new IRCMessageFormatException("IRC message with over 15 parameters!");
         }
 
         return new IRCMessage(prefix, command, params);
     }
 
     // ------------------------------------------------------------------------
 
     public void handleLine(String line)
     {
         IRCMessage im = null;
         try
         {
             im = this.parseRawLine(line);
         }
         catch (IRCMessageFormatException e)
         {
             this.ircNetwork.getIRCConnectionLog().exception(e);
         }
         catch (Exception e)
         {
             // index, null ?
             this.ircNetwork.getIRCConnectionLog().exception(e);
         }
 
         // silently ignore empty messages
         if (im == null) return;
 
         if (im.isNumericReply())
         {
             processServerResponse(im, false);
         }
         else
         {
             processCommand(im, false);
         }
     }
 
     protected void processServerResponse(IRCMessage im, boolean handledAlready)
     {
         // im.getParams().get(0); // --> always me ?
 
         switch (im.getNumericReply())
         {
             case RPL_WELCOME:
             {
                 int i = im.getParams().get(1).lastIndexOf(IRCMessage.IRC_SPACE);
                 String nickuserhost = im.getParams().get(1).substring(i + 1);
 
                 // set my user object !
                 IRCUser ircUser = new IRCUser(this.ircNetwork.getIRCUserManager(),
                         IRCUser.getNickByPrefix(nickuserhost));
                 ircUser.setUsername(IRCUser.getUsernameByPrefix(nickuserhost));
                 ircUser.setHost(IRCUser.getHostByPrefix(nickuserhost));
                 this.ircNetwork.getMyIRCIdentity().setIRCUser(ircUser);
                 this.ircNetwork.getIRCConnectionLog().object("ircUser", ircUser);
                 // go on
             }
             case RPL_YOURHOST:
             case RPL_CREATED:
             case RPL_MYINFO:
             case RPL_ISUPPORT:
             {
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 this.ircNetwork.getIRCNetworkInfo().update(im);
                 break; // -----------------------------------------------------
             }
             case RPL_MOTDSTART:
             {
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 this.ircNetwork.raiseEvent(new MotdUpdatingEvent(this.ircNetwork));
                 this.ircNetwork.getIRCNetworkInfo().newMotd().addModtLine(im.getParams().get(1).substring(2));
                 break; // -----------------------------------------------------
             }
             case RPL_MOTD:
             {
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 this.ircNetwork.getIRCNetworkInfo().addModtLine(im.getParams().get(1).substring(2));
                 break; // -----------------------------------------------------
             }
             case RPL_ENDOFMOTD:
             {
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 this.ircNetwork.getIRCNetworkInfo().addModtLine(im.getParams().get(1).substring(2)).finishNewMotd();
                 this.ircNetwork.raiseEvent(new MotdUpdatedEvent(this.ircNetwork));
                 break; // -----------------------------------------------------
             }
             case RPL_LUSERCLIENT:
             case RPL_LUSEROP:
             case RPL_LUSERUNKNOWN:
             case RPL_LUSERCHANNELS:
             case RPL_LUSERME:
             {
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 this.ircNetwork.getIRCConnectionLog().message(
                         im.getCommand() + "-Handler (LUSER-Info) not yet implemented.");
                 break; // -----------------------------------------------------
             }
             case ERR_NICKNAMEINUSE:
             {
                 this.ircNetwork.raiseEvent(new NickAlreadyInUseEvent(this.ircNetwork, im));
                 break; // -----------------------------------------------------
             }
             // ----------------------------------------------------------------
             // reply to TOPIC command
             case RPL_NOTOPIC:
             case RPL_TOPIC:
             {
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(1));
                 ircChannel.setTopic(im.getParams().get(2));
                 this.ircNetwork.raiseEvent(new ChannelTopicUpdateEvent(this.ircNetwork, ircChannel));
                 break; // -----------------------------------------------------
             }
             case RPL_TOPICINFO:
             {
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(1));
 
                 // timestamp when topic was set
                 try
                 {
                     long time = Long.valueOf(im.getParams().get(3)) * 1000;
                     ircChannel.setTopicTimestamp(time);
                 }
                 catch (NumberFormatException e)
                 {
                 }
 
                 // topic setter
                 ircChannel.setTopicSetter(im.getParams().get(2));
 
                 this.ircNetwork.raiseEvent(new ChannelTopicUpdateEvent(this.ircNetwork, ircChannel));
                 break; // -----------------------------------------------------
             }
             // ----------------------------------------------------------------
             // reply to NAMES command
             case RPL_NAMREPLY:
             {
                 // TODO:
                IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(1));
 
                String users = im.getParams().get(2);
                 String[] userstokens = users.split(" ");
                 for (String usertoken : userstokens)
                 {
                     char status = 0x00;
                     if (IRCUser.USER_PREFIXES.indexOf(usertoken.charAt(0)) != -1)
                     {
                         status = usertoken.charAt(0);
                         usertoken = usertoken.substring(1);
                     }
 
                     IRCUser ircUser = this.ircNetwork.getIRCUserManager().getIRCUser(usertoken);
                     ircChannel.addIRCUser(ircUser);
                 }
 
                 this.ircNetwork.raiseEvent(new IRCChannelInfoEvent(this.ircNetwork, im, ircChannel));
                 break; // -----------------------------------------------------
             }
             case RPL_ENDOFNAMES:
             {
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(1));
                 this.ircNetwork.raiseEvent(new IRCChannelInfoEvent(this.ircNetwork, im, ircChannel));
                 break; // -----------------------------------------------------
             }
             // ----------------------------------------------------------------
             // reply to MODE <channel> command
             case RPL_CHANNELMODEIS:
             {
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(1));
                 ircChannel.setMode(im.getParams().get(2));
 
                 this.ircNetwork.raiseEvent(new ChannelModeUpdateEvent(this.ircNetwork, ircChannel, im.getParams()
                         .get(2)));
                 break; // -----------------------------------------------------
             }
             case RPL_CREATIONTIME:
             {
                 // TODO: channel creator/creation time
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(1));
 
                 try
                 {
                     // in seconds (need millis)
                     long time = Long.valueOf(im.getParams().get(im.getParams().size() - 1)) * 1000;
                     ircChannel.setCreationTimestamp(time);
                 }
                 catch (NumberFormatException e)
                 {
                     break; // no update ...
                 }
 
                 this.ircNetwork.raiseEvent(new ChannelModeUpdateEvent(this.ircNetwork, ircChannel, im.getParams()
                         .get(2)));
                 break; // -----------------------------------------------------
             }
 
             // ----------------------------------------------------------------
             // reply to LIST command
             case RPL_LISTSTART:
             {
                 // we could also ignore this reply ...
                 this.ircChannelListBuilder.clear();
                 break; // -----------------------------------------------------
             }
             case RPL_LIST:
             {
                 try
                 {
                     this.ircChannelListBuilder.add(im.getParams().get(1), im.getParams().get(2), im.getParams().get(3));
                 }
                 catch (Exception e)
                 {
                     // shouldn't occur but better safe than sorry ... ;-)
                     // IRCMessageFormatException
                     // NullPointerException
                     // IllegalArgumentException
                     this.ircNetwork.getIRCConnectionLog().exception(e);
                 }
                 break; // -----------------------------------------------------
             }
             case RPL_LISTEND:
             {
                 IRCChannelList old = this.ircNetwork.getIRCChannelManager().getIRCChannelList();
 
                 this.ircChannelListBuilder.sort(); // ?
                 this.ircNetwork.getIRCChannelManager().updateIRCChannelList(ircChannelListBuilder.build());
                 this.ircChannelListBuilder.clear();
 
                 this.ircNetwork.raiseEvent(new ChannelListUpdateEvent(this.ircNetwork, old));
                 break; // -----------------------------------------------------
             }
             // ----------------------------------------------------------------
             // reply to WHO
             case RPL_WHOREPLY:
             {
                 IRCUser ircUser = this.ircNetwork.getIRCUserManager().getIRCUser(im.getParams().get(5));
 
                 String chnl = im.getParams().get(1);
                 if (!chnl.equals(IRCChannel.NO_CHANNEL) && this.ircNetwork.getIRCChannelManager().hasIRCChannel(chnl))
                 {
                     // * are the channel-less un-invisible users ...
                     // only continue here if channel exists because we don't want to create lots of channel we aren't
                     // in ...
                     IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(chnl);
 
                     ircChannel.addIRCUser(ircUser); // if not already there
                 }
 
                 ircUser.setUsername(im.getParams().get(2));
                 ircUser.setHost(im.getParams().get(3));
                 ircUser.setServer(im.getParams().get(4));
 
                 // TODO: status parsing (in channel?)
                 String status = im.getParams().get(6);
                 ircUser.setIRCOp(status.indexOf('*') != -1);
                 ircUser.setAway(status.indexOf('G') != -1); // ?
 
                 try
                 {
                     String last = im.getParams().get(7);
                     int index = last.indexOf(' ');
                     if (index != -1)
                     {
                         ircUser.setRealname(last.substring(index + 1));
 
                         ircUser.setHops(Integer.valueOf(last.substring(0, index)));
                     }
                 }
                 catch (Exception e)
                 {
                 }
 
                 this.ircNetwork.raiseEvent(new UserInfoUpdateEvent(this.ircNetwork, ircUser));
             }
             case RPL_ENDOFWHO:
             {
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 break; // -----------------------------------------------------
             }
             case 334:
             {
                 // help to WHO ?
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 break; // -----------------------------------------------------
             }
 
             // ----------------------------------------------------------------
             // ERROR replies
             case ERR_TOOMANYMATCHES: // e. g. too many WHO-lines ... need stricter query
             case ERR_NOSUCHSERVER:
             {
                 // TODO: Error reply event ?
                 this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
                 break; // -----------------------------------------------------
             }
 
             // ----------------------------------------------------------------
             default:
             {
                 if (!handledAlready) this.ircNetwork.raiseEvent(new UnknownServerCommandEvent(this.ircNetwork, im));
                 break;
             }
         }
     }
 
     protected void processCommand(IRCMessage im, boolean handledAlready)
     {
         IRCServerCommand isc = null;
         try
         {
             isc = IRCServerCommand.valueOf(im.getCommand());
         }
         catch (Exception e)
         {
         }
 
         if (isc == null)
         {
             this.ircNetwork.raiseEvent(new UnknownServerCommandEvent(this.ircNetwork, im));
             return;
         }
 
         switch (isc)
         {
             case PING:
             {
                 // TODO: this.ircNetwork.sendPong(im.getParams().get(0)); // send reply immediately?
                 this.ircNetwork.raiseEvent(new PingEvent(this.ircNetwork, im.getParams().get(0)));
                 break; // -----------------------------------------------------
             }
             case PRIVMSG:
             case NOTICE:
             {
                 this.processMessage(im);
                 break; // -----------------------------------------------------
             }
             case JOIN:
             {
                 IRCUser ircUser = this.ircNetwork.getIRCUserManager().getIRCUserByPrefix(im.getPrefix());
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(0));
 
                 if (ircUser.isMe())
                 {
                     ircChannel.refreshMode(); // send MODE request
                     ircChannel.refreshIRCUserList(); // send WHO/NAMES?
                 }
 
                 ircChannel.addIRCUser(ircUser);
 
                 this.ircNetwork.raiseEvent(new JoinEvent(this.ircNetwork, ircChannel, ircUser));
                 break; // -----------------------------------------------------
             }
             case PART:
             {
                 IRCUser ircUser = this.ircNetwork.getIRCUserManager().getIRCUserByPrefix(im.getPrefix());
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(0));
                 String reason = (im.getParams().size() > 1) ? im.getParams().get(1) : null;
 
                 // TODO: to get a snapshot add code here
 
                 if (ircUser.isMe())
                 {
                     // remove channel?
                     this.ircNetwork.getIRCChannelManager().removeIRCChannel(ircChannel);
                 }
                 else
                 {
                     // remove the parting user
                     ircChannel.removeIRCUser(ircUser);
                 }
 
                 this.ircNetwork.raiseEvent(new PartEvent(this.ircNetwork, ircChannel, ircUser, reason));
                 break; // -----------------------------------------------------
             }
             case QUIT:
             {
                 IRCUser ircUser = this.ircNetwork.getIRCUserManager().getIRCUserByPrefix(im.getPrefix());
 
                 // TODO: to get a snapshot add code here
 
                 this.ircNetwork.getIRCUserManager().removeIRCUser(ircUser);
                 this.ircNetwork.raiseEvent(new QuitEvent(this.ircNetwork, ircUser, im.getParams().get(0)));
                 break; // -----------------------------------------------------
             }
             case NICK:
             {
                 String sourceNick = IRCUser.getNickByPrefix(im.getPrefix());
                 IRCUser ircUser = this.ircNetwork.getIRCUserManager().getIRCUser(sourceNick);
 
                 ircUser.setNickname(im.getParams().get(0));
 
                 this.ircNetwork.raiseEvent(new NickChangeEvent(this.ircNetwork, ircUser, sourceNick, im.getParams()
                         .get(0)));
                 break; // -----------------------------------------------------
             }
             case KICK:
             {
                 IRCUser source = this.ircNetwork.getIRCUserManager().getIRCUserByPrefix(im.getPrefix());
                 IRCChannel ircChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(im.getParams().get(0));
                 IRCUser recipient = this.ircNetwork.getIRCUserManager().getIRCUser(im.getParams().get(1));
                 String reason = (im.getParams().size() > 2) ? im.getParams().get(2) : null;
 
                 // TODO: add code here to get a snapshot
 
                 if (recipient.isMe())
                 {
                     this.ircNetwork.getIRCChannelManager().removeIRCChannel(ircChannel);
                 }
                 else
                 {
                     ircChannel.removeIRCUser(recipient);
                 }
 
                 this.ircNetwork.raiseEvent(new KickEvent(this.ircNetwork, ircChannel, source, recipient, reason));
                 break; // -----------------------------------------------------
             }
             case MODE:
             {
                 this.processMode(im);
                 break; // -----------------------------------------------------
             }
             // case TOPIC:
             // {
             //
             // }
             // case INVITE:
             // {
             //
             // }
             case ERROR:
             {
                 // TODO: something more to do?
                 this.ircNetwork.getIRCConnectionLog().message(
                         im.getCommand() + " - reason: '" + im.getParams().get(0) + "'");
                 this.ircNetwork.disconnect(false);
                 break; // -----------------------------------------------------
             }
             default:
             {
                 if (!handledAlready)
                     this.ircNetwork.getIRCConnectionLog().message(im.getCommand() + "-Handler not yet implemented.");
                 break;
             }
         }
     }
 
     // --------------------------------
 
     protected void processMessage(IRCMessage im)
     {
         // check command - abort if wrong
         boolean isNotice = false;
         try
         {
             IRCServerCommand isc = IRCServerCommand.valueOf(im.getCommand());
             if (isc == IRCServerCommand.NOTICE)
             {
                 isNotice = true;
             }
             else if (isc != IRCServerCommand.PRIVMSG)
             {
                 throw new IllegalArgumentException("IRCMessage im is neither PRIVMSG or NOTICE!");
             }
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("IRCMessage has unknown command!");
         }
 
         // check if message from user or server
         if (im.isServerPrefix())
         {
             this.ircNetwork.raiseEvent(new IRCNetworkInfoEvent(this.ircNetwork, im));
             return;
         }
 
         // --------------------------------------------------------------------
 
         // message sender
         IRCUser sourceIRCUser = this.ircNetwork.getIRCUserManager().getIRCUserByPrefix(im.getPrefix());
 
         // the message sent
         String message = im.getParams().get(1);
 
         // lowlevel decode message
         message = IRCMessageProcessor.dequoteLowLevel(message);
 
         // TODO: allow inline new lines? max new lines?
         // remove unallowed chars ...
         message = this.stripMiddleLevel(message);
 
         // --------------------------------------------------------------------
 
         // check for CTCP messages and process them
         if (this.containsCTCPMessage(message))
         {
             this.processCTCP(im, sourceIRCUser);
 
             message = this.removeCTCPMessages(message); // normal message part
         }
 
         // --------------------------------------------------------------------
 
         // process normal message part
         if (message != null && message.trim().length() > 0)
         {
             // check recipient
             String target = im.getParams().get(0);
             if (IRCChannel.CHANNEL_PREFIXES.indexOf(target.charAt(0)) == -1)
             {
                 // message to user
                 IRCUser targetIRCUser = this.ircNetwork.getIRCUserManager().getIRCUser(target);
                 if (isNotice)
                 {
                     this.ircNetwork.raiseEvent(new NoticeToUserEvent(this.ircNetwork, sourceIRCUser, targetIRCUser,
                             message));
                 }
                 else
                 {
                     this.ircNetwork.raiseEvent(new PrivateMessageToUserEvent(this.ircNetwork, sourceIRCUser,
                             targetIRCUser, message));
                 }
             }
             else
             {
                 // message to channel
                 IRCChannel targetIRCChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(target);
                 if (isNotice)
                 {
                     this.ircNetwork.raiseEvent(new NoticeToChannelEvent(this.ircNetwork, sourceIRCUser,
                             targetIRCChannel, message));
                 }
                 else
                 {
                     this.ircNetwork.raiseEvent(new PrivateMessageToChannelEvent(this.ircNetwork, sourceIRCUser,
                             targetIRCChannel, message));
                 }
             }
         }
         // empty message -> ignore
     }
 
     protected void processCTCP(IRCMessage im, IRCUser sourceIRCUser)
     {
         this.ircNetwork.getIRCConnectionLog().message("CTCP-" + im.getCommand());
 
         String ctcpMessage = im.getParams().get(1);
 
         this.ircNetwork.getIRCConnectionLog().object("lowLevelMessage   ", ctcpMessage);
         String middleLevelMessage = IRCMessageProcessor.dequoteLowLevel(ctcpMessage);
         this.ircNetwork.getIRCConnectionLog().object("middleLevelMessage", middleLevelMessage);
         String highLevelMessage = IRCMessageProcessor.dequoteCTCP(this.removeCTCPMessages(middleLevelMessage));
         this.ircNetwork.getIRCConnectionLog().object("highLevelMessage  ", highLevelMessage);
 
         // --------------------------------------------------------------------
 
         // check command (if request or reply ...)
         boolean isNotice = false;
         try
         {
             IRCServerCommand isc = IRCServerCommand.valueOf(im.getCommand());
             if (isc == IRCServerCommand.NOTICE)
             {
                 isNotice = true;
             }
             else if (isc != IRCServerCommand.PRIVMSG)
             {
                 throw new IllegalArgumentException("IRCMessage im is neither PRIVMSG or NOTICE!");
             }
         }
         catch (Exception e)
         {
             throw new IllegalArgumentException("IRCMessage has unknown command!");
         }
 
         List<IRCExtendedDataMessage> le = this.extractCTCPDataMessages(middleLevelMessage);
         for (IRCExtendedDataMessage edm : le)
         {
             this.processCTCPCommand(im, sourceIRCUser, edm, isNotice, false);
         }
     }
 
     protected void processCTCPCommand(IRCMessage im, IRCUser sourceIRCUser, IRCExtendedDataMessage edm,
             boolean isNotice, boolean handledAlready)
     {
         IRCCTCPType icc = null;
         try
         {
             icc = IRCCTCPType.valueOf(edm.getTag());
         }
         catch (Exception e)
         {
         }
 
         if (icc == null)
         {
             this.ircNetwork.raiseEvent(new UnknownCTCPCommandEvent(this.ircNetwork, im, edm));
             return;
         }
 
         // TODO: do the magick
         switch (icc)
         {
             case ACTION:
             {
                 String target = im.getParams().get(0);
                 if (IRCChannel.CHANNEL_PREFIXES.indexOf(target.charAt(0)) == -1)
                 {
                     // message to user
                     IRCUser targetIRCUser = this.ircNetwork.getIRCUserManager().getIRCUser(target);
                     this.ircNetwork.raiseEvent(new ActionMessageToUserEvent(this.ircNetwork, sourceIRCUser,
                             targetIRCUser, edm.getExtendedData()));
                 }
                 else
                 {
                     // message to channel
                     IRCChannel targetIRCChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(target);
                     this.ircNetwork.raiseEvent(new ActionMessageToChannelEvent(this.ircNetwork, sourceIRCUser,
                             targetIRCChannel, edm.getExtendedData()));
                 }
                 break; // -----------------------------------------------------
             }
             case DCC:
             {
                 boolean ok = false;
                 IRCDCCMessage ircDCCMessage = null;
 
                 try
                 {
                     ircDCCMessage = this.parseDCCMessage(edm.getExtendedData());
 
                     ok = this.ircNetwork.getIRCDCCManager().processRequest(sourceIRCUser, ircDCCMessage);
                 }
                 catch (Exception e)
                 {
                     // IRCDCCMessageFormatException
                     // NullPointerException
                     this.ircNetwork.getIRCConnectionLog().exception(e);
                 }
 
                 if (!ok)
                 {
                     if (ircDCCMessage != null)
                     {
                         this.ircNetwork.raiseEvent(new UnknownDCCCommandEvent(this.ircNetwork, im, edm, ircDCCMessage));
                     }
                     else
                     {
                         this.ircNetwork.raiseEvent(new UnknownCTCPCommandEvent(this.ircNetwork, im, edm));
                     }
                 }
 
                 break; // -----------------------------------------------------
             }
             default:
             {
                 if (!handledAlready) this.ircNetwork.raiseEvent(new UnknownCTCPCommandEvent(this.ircNetwork, im, edm));
                 break; // -----------------------------------------------------
             }
         }
     }
 
     // ------------
 
     public static String quoteCTCP(String highLevelMessage)
     {
         StringBuilder sb = new StringBuilder(highLevelMessage);
 
         // X-DELIM --> X-QUOTE 'a'
         // 0x01 -> \a
         int index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.CTCP_XDELIM, index)) != -1)
         {
             sb.replace(index, index + 1, IRCMessageProcessor.CTCP_XQUOTE + 'a');
             index += 2;
         }
 
         // X-QUOTE --> X-QUOTE X-QUOTE
         // \ -> \\
         index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.CTCP_XQUOTE, index)) != -1)
         {
             sb.insert(index, IRCMessageProcessor.CTCP_XQUOTE);
             index += 2;
         }
 
         return sb.toString();
     }
 
     public static String dequoteCTCP(String middleLevelMessage)
     {
         StringBuilder sb = new StringBuilder(middleLevelMessage);
 
         // X-QUOTE [NOT X-QUOTE | 'a'] -> ignore X-QUOTE
         // TODO: how?
 
         // X-QUOTE X-QUOTE --> X-QUOTE
         int index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.CTCP_XQUOTE + IRCMessageProcessor.CTCP_XQUOTE, index)) != -1)
         {
             sb.deleteCharAt(index);
             index += 1;
         }
 
         // X-QUOTE 'a' --> X-DELIM
         index = 0;
         String quotea = IRCMessageProcessor.CTCP_XQUOTE + 'a';
         while ((index = sb.indexOf(quotea, index)) != -1)
         {
             sb.replace(index, index + 2, IRCMessageProcessor.CTCP_XDELIM);
             index += 1;
         }
 
         return sb.toString();
     }
 
     // ------------
 
     protected boolean containsCTCPMessage(String message)
     {
         return (message != null && message.indexOf(IRCMessageProcessor.CTCP_XDELIM) != -1);
     }
 
     protected List<Integer> getCTCPMessageIndizes(String message)
     {
         // gets the indizes of the CTCP_XDELIMs
         // returns an even sized list with indizes
         List<Integer> ints = new ArrayList<Integer>();
 
         int index = 0;
         while ((index = message.indexOf(IRCMessageProcessor.CTCP_XDELIM, index)) != -1)
         {
             ints.add(index);
             index++;
         }
 
         // last checks
         if (ints.size() > 2 && ints.size() % 2 == 1) ints.remove(ints.size() - 2); // fix
         if (ints.size() == 1) return new ArrayList<Integer>(); // would result in an error ?
 
         return ints;
     }
 
     protected String removeCTCPMessages(String message)
     {
         StringBuilder sb = new StringBuilder(message);
 
         List<Integer> ints = this.getCTCPMessageIndizes(message);
         for (int i = 0; i < ints.size(); i += 2)
         {
             sb.replace(ints.get(i), ints.get(i + 1) + 1, "");
         }
 
         return sb.toString();
     }
 
     protected List<String> extractCTCPStrings(String message, boolean dequoteCTCP)
     {
         List<String> list = new ArrayList<>();
 
         List<Integer> ints = this.getCTCPMessageIndizes(message);
         for (int i = 0; i < ints.size(); i += 2)
         {
             // empty messages too
             String m = message.substring(ints.get(i) + 1, ints.get(i + 1));
             if (dequoteCTCP) m = IRCMessageProcessor.dequoteCTCP(m);
             list.add(m);
         }
 
         return list;
     }
 
     protected List<IRCExtendedDataMessage> extractCTCPDataMessages(String message)
     {
         List<IRCExtendedDataMessage> list = new ArrayList<>();
 
         List<String> messages = this.extractCTCPStrings(message, true);
         for (String m : messages)
         {
             if (m.length() == 0)
             {
                 // no tag -> empty message
                 list.add(new IRCExtendedDataMessage(null, null));
                 continue;
             }
 
             int i = m.indexOf(' ');
             if (i == -1)
             {
                 // no space -> only tag
                 list.add(new IRCExtendedDataMessage(m, null));
                 continue;
             }
             else
             {
                 // with space -> tag + opt. ext.data
                 String tag = m.substring(0, i);
                 String extData = ((m.length() > i + 1) ? m.substring(i + 1) : "");
                 list.add(new IRCExtendedDataMessage(tag, extData));
                 continue;
             }
         }
 
         return list;
     }
 
     // ------------
 
     public static String quoteLowLevel(String middleLevelMessage)
     {
         // middle level (can contain every character) -> low level
         StringBuilder sb = new StringBuilder(middleLevelMessage);
 
         // M-QUOTE --> M-QUOTE M-QUOTE
         int index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.MQUOTE, index)) != -1)
         {
             sb.insert(index, IRCMessageProcessor.MQUOTE);
             index += 2;
         }
 
         // NUL --> M-QUOTE '0'
         index = 0;
         while ((index = sb.indexOf("\u0000", index)) != -1)
         {
             sb.replace(index, index + 1, IRCMessageProcessor.MQUOTE + '0');
             index += 2;
         }
 
         // NL --> M-QUOTE 'n'
         index = 0;
         while ((index = sb.indexOf("\n", index)) != -1)
         {
             sb.replace(index, index + 1, IRCMessageProcessor.MQUOTE + 'n');
             index += 2;
         }
 
         // CR --> M-QUOTE 'r'
         index = 0;
         while ((index = sb.indexOf("\r", index)) != -1)
         {
             sb.replace(index, index + 1, IRCMessageProcessor.MQUOTE + 'r');
             index += 2;
         }
 
         return sb.toString();
     }
 
     public static String dequoteLowLevel(String lowLevelMessage)
     {
         // low level -> middle level
         StringBuilder sb = new StringBuilder(lowLevelMessage);
 
         // M-QUOTE 'r' --> CR
         int index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.MQUOTE + 'r', index)) != -1)
         {
             sb.replace(index, index + 2, "\r");
             index += 1;
         }
 
         // M-QUOTE 'n' --> NL
         index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.MQUOTE + 'n', index)) != -1)
         {
             sb.replace(index, index + 2, "\n");
             index += 1;
         }
 
         // M-QUOTE '0' --> NUL
         index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.MQUOTE + '0', index)) != -1)
         {
             sb.replace(index, index + 2, "\u0000");
             index += 1;
         }
 
         index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.MQUOTE + 'r', index)) != -1)
         {
             sb.replace(index, index + 2, "\r");
             index += 1;
         }
 
         // M-QUOTE M-QUOTE --> M-QUOTE
         index = 0;
         while ((index = sb.indexOf(IRCMessageProcessor.MQUOTE + IRCMessageProcessor.MQUOTE, index)) != -1)
         {
             sb.deleteCharAt(index);
             index += 1;
         }
 
         return sb.toString();
     }
 
     // ------------
 
     protected String stripMiddleLevel(String middleLevelMessage)
     {
         // TODO: overwrite in subclasses?
         // TODO: remove chars we don't want to see ...
         return middleLevelMessage;
     }
 
     // --------------------------------
 
     protected IRCDCCMessage parseDCCMessage(String message) throws NullPointerException, IRCDCCMessageFormatException
     {
         Objects.requireNonNull(message, "message must not be null!");
 
         // TODO: for later: check out different DCC standards, how to implement them, ...
 
         // ----------------------------
         // get type (first token)
         int index = message.indexOf(' ');
         if (index == -1)
         {
             throw new IRCDCCMessageFormatException(
                     "DCC message must contain at least 4 tokens separated with space characters!");
         }
         String type = message.substring(0, index);
         message = message.substring(index + 1);
 
         // ----------------------------
         // parse argument (can contain space characters, will be enclosed in quotes)
         String argument = null;
         if ('"' == message.charAt(0))
         {
             index = message.indexOf("\" ", 1);
             if (index == -1)
             {
                 throw new IRCDCCMessageFormatException("Could not parse String from argument token!");
             }
             argument = message.substring(1, index);
             message = message.substring(index + 2);
         }
         else if ('\'' == message.charAt(0))
         {
             index = message.indexOf("' ", 1);
             if (index == -1)
             {
                 throw new IRCDCCMessageFormatException("Could not parse String from argument token!");
             }
             argument = message.substring(1, index);
             message = message.substring(index + 2);
         }
         else
         {
             index = message.indexOf(' ');
             if (index == -1)
             {
                 throw new IRCDCCMessageFormatException(
                         "DCC message must contain at least 4 tokens separated with space characters!");
             }
             argument = message.substring(0, index);
             message = message.substring(index + 1);
         }
 
         // ----------------------------
         // parse address
         index = message.indexOf(' ');
         if (index == -1)
         {
             throw new IRCDCCMessageFormatException(
                     "DCC message must not contain only 2 tokens! (arguments <address> <port> missing)");
         }
 
         String address = message.substring(0, index);
         message = message.substring(index + 1);
 
         // ----------------------------
         // parse port + optional size
         String port = null;
         String size = null;
 
         index = message.indexOf(' ');
         if (index == -1)
         {
             port = message;
         }
         else
         {
             port = message.substring(0, index);
             message = message.substring(index + 1);
 
             // parse size
             size = message;
         }
 
         return new IRCDCCMessage(type, argument, address, port, size);
     }
 
     // --------------------------------
 
     protected void processMode(IRCMessage im)
     {
         IRCServerCommand isc = null;
         try
         {
             isc = IRCServerCommand.valueOf(im.getCommand());
 
             if (isc != IRCServerCommand.MODE) isc = null;
         }
         catch (Exception e)
         {
         }
 
         if (isc == null)
         {
             throw new IllegalArgumentException("Invalid IRCMessage! No MODE command found!");
         }
 
         IRCUser sourceIRCUser = this.ircNetwork.getIRCUserManager().getIRCUserByPrefix(im.getPrefix());
 
         String target = im.getParams().get(0);
         String mode = im.getParams().get(1);
 
         if (IRCChannel.CHANNEL_PREFIXES.indexOf(target.charAt(0)) == -1)
         {
             // USER-MODE
             IRCUser targetIRCUser = this.ircNetwork.getIRCUserManager().getIRCUser(target);
 
             this.ircNetwork.raiseEvent(new UserModeChangeEvent(this.ircNetwork, sourceIRCUser, targetIRCUser, mode));
         }
         else
         {
             // (CHANNEL-) MODE
             IRCChannel targetIRCChannel = this.ircNetwork.getIRCChannelManager().getIRCChannel(target);
 
             String oldMode = targetIRCChannel.getMode(false); // dont't want to trigger request
 
             // TODO: add code for immutable object creation ... here
 
             List<String> modeparams = new ArrayList<>();
             modeparams.addAll(im.getParams().subList(2, im.getParams().size()));
 
             StringBuilder modeChange = new StringBuilder(mode);
             for (int i = 2; i < im.getParams().size(); i++)
             {
                 modeChange.append(' ').append(im.getParams().get(i));
             }
 
             // update channel mode
             targetIRCChannel.updateMode(mode, modeparams);
 
             this.ircNetwork.raiseEvent(new ChannelModeChangeEvent(this.ircNetwork, sourceIRCUser, targetIRCChannel,
                     oldMode, modeChange.toString()));
         }
     }
 
     // ------------------------------------------------------------------------
 
     protected final IRCManager getIRCManager()
     {
         return this.ircNetwork.getIRCManager();
     }
 }
