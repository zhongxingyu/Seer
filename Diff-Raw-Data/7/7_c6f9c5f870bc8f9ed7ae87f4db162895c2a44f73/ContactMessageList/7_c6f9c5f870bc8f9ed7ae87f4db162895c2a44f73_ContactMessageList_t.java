 /*
  * ContactMessageList.java
  *
  * Created on 19.02.2005, 23:54
  * Copyright (c) 2005-2008, Eugene Stahov (evgs), http://bombus-im.org
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * You can also redistribute and/or modify this program under the
  * terms of the Psi License, specified in the accompanied COPYING
  * file, as published by the Psi Project; either dated January 1st,
  * 2005, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  */
 
 package Client;
 //#ifndef WMUC
 import Conference.MucContact;
 //#endif
 //#ifdef HISTORY
 //# import History.HistoryAppend;
 //#ifdef LAST_MESSAGES
 //# import History.HistoryStorage;
 //#endif
 //#ifdef HISTORY_READER
 //# import History.HistoryReader;
 //#endif
 //#endif
 import Menu.RosterItemActions;
 import Messages.MessageList;
 import locale.SR;
 import ui.MainBar;
 import java.util.*;
 import Menu.MenuCommand;
 //#ifdef CLIPBOARD
 //# import util.ClipBoard;
 //#endif
 //#ifdef ARCHIVE
 import Archive.MessageArchive;
 //#endif
 //#ifdef JUICK
 //# import Menu.JuickThingsMenu;
 //# import Menu.MyMenu;
 //#endif
 import ui.VirtualList;
 //#ifdef FILE_TRANSFER
 import io.file.transfer.TransferAcceptFile;
 import io.file.transfer.TransferDispatcher;
 //#endif
 
 public class ContactMessageList extends MessageList {
     Contact contact;
 
     MenuCommand cmdSubscribe=new MenuCommand(SR.MS_SUBSCRIBE, MenuCommand.SCREEN, 1);
     MenuCommand cmdDecline = new MenuCommand(SR.MS_DECLINE, MenuCommand.SCREEN, 2);
     MenuCommand cmdAcceptFile = new MenuCommand("Accept", MenuCommand.SCREEN, 1);
     MenuCommand cmdDeclineFile = new MenuCommand(SR.MS_DECLINE, MenuCommand.SCREEN, 2);
     MenuCommand cmdMessage=new MenuCommand(SR.MS_NEW_MESSAGE,MenuCommand.SCREEN,3);
     MenuCommand cmdResume=new MenuCommand(SR.MS_RESUME,MenuCommand.SCREEN,1);
     MenuCommand cmdReply=new MenuCommand(SR.MS_REPLY,MenuCommand.SCREEN,4);
     MenuCommand cmdQuote=new MenuCommand(SR.MS_QUOTE,MenuCommand.SCREEN,5);
 //#ifdef ARCHIVE
     MenuCommand cmdArch=new MenuCommand(SR.MS_ADD_ARCHIVE,MenuCommand.SCREEN,6);
 //#endif
     MenuCommand cmdPurge=new MenuCommand(SR.MS_CLEAR_LIST, MenuCommand.SCREEN, 7);
     MenuCommand cmdSelect=new MenuCommand(SR.MS_SELECT, MenuCommand.SCREEN, 8);
     MenuCommand cmdActions=new MenuCommand(SR.MS_CONTACT,MenuCommand.SCREEN,9);
     MenuCommand cmdActive=new MenuCommand(SR.MS_ACTIVE_CONTACTS,MenuCommand.SCREEN,10);
 //#if TEMPLATES
 //#     MenuCommand cmdTemplate=new MenuCommand(SR.MS_SAVE_TEMPLATE,MenuCommand.SCREEN,11);
 //#endif
 //#ifdef FILE_IO
     MenuCommand cmdSaveChat=new MenuCommand(SR.MS_SAVE_CHAT, MenuCommand.SCREEN, 12);
 //#endif
 //#ifdef HISTORY
 //#ifdef HISTORY_READER
 //#          MenuCommand cmdReadHistory=new MenuCommand(SR.MS_HISTORY, MenuCommand.SCREEN, 13);
 //#endif
 //# //        if (cf.lastMessages && !contact.isHistoryLoaded()) loadRecentList();
 //#endif
 //#ifdef CLIPBOARD    
 //#     MenuCommand cmdSendBuffer=new MenuCommand(SR.MS_SEND_BUFFER, MenuCommand.SCREEN, 14);
 //#endif
 
     StaticData sd = StaticData.getInstance();
 
 //#ifdef JUICK
 //#     MenuCommand cmdJuickMessageReply=new MenuCommand(SR.MS_JUICK_MESSAGE_REPLY, MenuCommand.SCREEN, 1);
 //#     MenuCommand cmdJuickSendPrivateReply;
 //#     MenuCommand cmdJuickThings=new MenuCommand(SR.MS_JUICK_THINGS, MenuCommand.SCREEN, 3);
 //#     MenuCommand cmdJuickMessageDelete=new MenuCommand(SR.MS_JUICK_MESSAGE_DELETE, MenuCommand.SCREEN, 4);
 //#     MenuCommand cmdJuickPostSubscribe=new MenuCommand(SR.MS_JUICK_POST_SUBSCRIBE, MenuCommand.SCREEN, 5);
 //#     MenuCommand cmdJuickPostUnsubscribe=new MenuCommand(SR.MS_JUICK_POST_UNSUBSCRIBE, MenuCommand.SCREEN, 6);
 //#     MenuCommand cmdJuickPostRecommend=new MenuCommand(SR.MS_JUICK_POST_RECOMMEND, MenuCommand.SCREEN, 7);
 //#     MenuCommand cmdJuickPostShow=new MenuCommand(SR.MS_JUICK_POST_SHOW, MenuCommand.SCREEN, 8);
 //# 
 //#     public MenuCommand cmdJuickCommands=new MenuCommand(SR.MS_COMMANDS+" Juick", MenuCommand.SCREEN, 15);
 //#     Vector currentJuickCommands = new Vector();
 //# 
 //#     public ContactMessageList() {
 //#     }
 //#endif
 
 //#ifdef CLIPBOARD    
 //#     private ClipBoard clipboard=ClipBoard.getInstance();
 //#endif
     
     private Config cf;
     
     private boolean on_end;
     private boolean composing=true;
 
     private boolean startSelection;
     /** Creates a new instance of MessageList
      * @param c
      */
     public ContactMessageList(Contact c) {
         super();
         this.contact=c;
         sd.roster.activeContact=contact;
 
         cf=Config.getInstance();
         MainBar mb=new MainBar(contact);
         setMainBarItem(mb);
 
         cursor=0;//activate
         on_end = false;
         commandState();
         setMenuListener(this);
         
         contact.setIncoming(0);
 //#ifdef FILE_TRANSFER
         contact.fileQuery=false;
 //#endif
 //#ifdef HISTORY
 //#ifdef LAST_MESSAGES
 //#         if (cf.lastMessages && !contact.isHistoryLoaded()) loadRecentList();
 //#endif
 //#endif
         if (contact.msgs.size()>0)
             moveCursorTo(firstUnread());
         show(sd.roster);
     }
 
     public final int firstUnread(){
         int unreadIndex=0;
         for (Enumeration e=contact.msgs.elements(); e.hasMoreElements();) {
             if (((Msg)e.nextElement()).unread)
                 break;
             if (contact.mark == unreadIndex)
                 break;            
             unreadIndex++;
         }        
         return unreadIndex;
     }    
 
     public final void commandState(){
         menuCommands.removeAllElements();
         if (startSelection) addMenuCommand(cmdSelect);
         
         if (contact.msgSuspended!=null) addMenuCommand(cmdResume);
         
         if (cmdSubscribe==null) return;
         
         try {
             Msg msg=(Msg) contact.msgs.elementAt(cursor);
             if (msg.messageType==Msg.MESSAGE_TYPE_AUTH) {
                 addMenuCommand(cmdSubscribe);
                 addMenuCommand(cmdDecline);
             }
         } catch (Exception e) {}
 //#ifdef FILE_TRANSFER        
         try {
             Msg msg=(Msg) contact.msgs.elementAt(cursor);
             if (msg.messageType==Msg.MESSAGE_TYPE_FILE_REQ) {
                 addMenuCommand(cmdAcceptFile);
                 addMenuCommand(cmdDeclineFile);
             }
         } catch (Exception e) {}
 //#endif        
         
         addMenuCommand(cmdMessage);
         
         if (contact.msgs.size()>0) {
 //#ifndef WMUC
             if (contact instanceof MucContact && contact.origin==Contact.ORIGIN_GROUPCHAT) {
                 addMenuCommand(cmdReply);
             }
 //#endif
             addMenuCommand(cmdQuote);
             addMenuCommand(cmdPurge);
             
             if (!startSelection) addMenuCommand(cmdSelect);
         
 //#ifdef CLIPBOARD
 //#             if (cf.useClipBoard) {
 //#                 addMenuCommand(cmdCopy);
 //#                 if (!clipboard.isEmpty()) addMenuCommand(cmdCopyPlus);
 //#             }
 //#endif
             if (isHasScheme())
                 addMenuCommand(cmdxmlSkin);
             if (isHasUrl())
                 addMenuCommand(cmdUrl);
         }
         
         if (contact.origin!=Contact.ORIGIN_GROUPCHAT)
             addMenuCommand(cmdActions);
     
 	addMenuCommand(cmdActive);
         if (contact.msgs.size()>0) {
 //#ifdef ARCHIVE
 //#ifdef PLUGINS
 //#          if (sd.Archive)
 //#endif
             addMenuCommand(cmdArch);
 //#endif
 //#if TEMPLATES
 //#ifdef PLUGINS         
 //#          if (sd.Archive)
 //#endif
 //#             addMenuCommand(cmdTemplate);
 //#endif
         }
 //#ifdef CLIPBOARD
 //#         if (cf.useClipBoard && !clipboard.isEmpty()) {
 //#             addMenuCommand(cmdSendBuffer);
 //#         }
 //#endif
 //#ifdef HISTORY
 //#         if (cf.saveHistory)
 //#             if (cf.msgPath!=null)
 //#                 if (!cf.msgPath.equals(""))
 //#                     if (contact.msgs.size()>0)
 //#                         addMenuCommand(cmdSaveChat);
 //#ifdef HISTORY_READER
 //#         if (cf.saveHistory) // && cf.lastMessages)
 //#             addMenuCommand(cmdReadHistory);
 //#endif
 //#endif
         
 //#ifdef JUICK
 //#ifdef PLUGINS
 //#         if(sd.Juick) {
 //#endif
 //#         // http://code.google.com/p/bm2/issues/detail?id=94
 //#         addMenuCommand(cmdJuickCommands);
 //#ifdef PLUGINS
 //#         }
 //#endif
 //#endif
 
         addMenuCommand(cmdBack);
     }
     
 //#ifdef JUICK
 //#     private void updateJuickCommands() {
 //#         currentJuickCommands = null;
 //#         currentJuickCommands = new Vector();
 //#         currentJuickCommands.addElement(cmdJuickThings);
 //#         if (isJuickContact(contact) || isJuBoContact(contact)) {
 //#             String body = getBodyFromCurrentMsg();
 //#             String target = getTargetForJuickReply(body);
 //# 
 //#             if (!target.equals("toThings")) {
 //#                 switch (target.charAt(0)) {
 //#                     case '#':
 //#                         if (target.indexOf('/') < 0) {
 //#                             currentJuickCommands.addElement(cmdJuickPostRecommend);
 //#                             currentJuickCommands.addElement(cmdJuickPostShow);
 //#                         }
 //#                         currentJuickCommands.addElement(cmdJuickMessageReply);
 //#                         currentJuickCommands.addElement(cmdJuickMessageDelete);
 //#                         currentJuickCommands.addElement(cmdJuickPostSubscribe);
 //#                         currentJuickCommands.addElement(cmdJuickPostUnsubscribe);
 //#                         break;
 //#                     case '@':
 //#                         cmdJuickSendPrivateReply = new MenuCommand(SR.MS_JUICK_SEND_PRIVATE_REPLY +" "+ target, MenuCommand.SCREEN, 3);
 //#                         currentJuickCommands.addElement(cmdJuickSendPrivateReply);
 //#                         break;
 //#                 }
 //#             }
 //#         }
 //#     }
 //#endif
 
 public void showNotify() {
     if (contact != null)
         sd.roster.activeContact=contact;
 //#ifdef LOGROTATE
 //#         getRedraw(true);
 //#endif
         super.showNotify();
     }
     
     public void forceScrolling() { //by voffk
         if (contact != null)
         if (contact.moveToLatest) {
             contact.moveToLatest = false;
             if (on_end)
                 moveCursorEnd();
         }
     }
 
     protected void beginPaint() {
         markRead(cursor);
         forceScrolling();
         on_end = (cursor==(getItemCount()-1));
     }
     
     public void markRead(int msgIndex) {
 	if (msgIndex>=getItemCount()) return;
         if (msgIndex<contact.lastUnread) return;
 
         sd.roster.countNewMsgs();
 //#ifdef LOGROTATE
 //#         getRedraw(contact.redraw);
 //#endif
     }
 //#ifdef LOGROTATE
 //#     private void getRedraw(boolean redraw) {
 //#         if (!redraw) return;
 //# 
 //#         contact.redraw=false;
 //#         messages=null;
 //#         messages=new Vector();
 //#         redraw();
 //#     }
 //#endif
     public int getItemCount(){ return (contact == null)? 0 :contact.msgs.size(); }
 
     public Msg getMessage(int index) {
         if (index> getItemCount()-1) return null;
         
 	Msg msg=(Msg) contact.msgs.elementAt(index);
 	if (msg.unread) contact.resetNewMsgCnt();
 	msg.unread=false;
 	return msg;
     }
     
     public void focusedItem(int index){ 
         markRead(index);
     }
     
     public void menuAction(MenuCommand c, VirtualList d){
         super.menuAction(c,d);
 		
         /** login-insensitive commands */
 //#ifdef ARCHIVE
         if (c==cmdArch) {
             try {
                 MessageArchive.store(getMessage(cursor),1);
             } catch (Exception e) {/*no messages*/}
         }
 //#endif
 //#if TEMPLATES
 //#         if (c==cmdTemplate) {
 //#             try {
 //#                 MessageArchive.store(getMessage(cursor),2);
 //#             } catch (Exception e) {/*no messages*/}
 //#         }
 //#endif
         if (c==cmdPurge) {
            //if (messages.isEmpty()) return;
             
             if (startSelection) {
                 for (Enumeration select=contact.msgs.elements(); select.hasMoreElements(); ) {
                     Msg mess=(Msg) select.nextElement();
                     if (mess.selected) {
                         contact.msgs.removeElement(mess);
                     }
                 }
                 startSelection = false;
                 
                 messages=null;
                 messages=new Vector();
             } else {
                 clearReadedMessageList();
             }
         }
         if (c==cmdSelect) {
             startSelection=true;
             Msg mess=((Msg) contact.msgs.elementAt(cursor));
             mess.selected = !mess.selected;
             mess.oldHighlite = mess.highlite;
             mess.highlite = mess.selected;
             //redraw();
             return;
         }
 //#ifdef HISTORY
 //#ifdef HISTORY_READER
 //#         if (c==cmdReadHistory) {
 //#             new HistoryReader(contact);
 //#             return;
 //#         }
 //#endif
 //#endif
 //#if (FILE_IO && HISTORY)
 //#         if (c==cmdSaveChat) saveMessages();
 //#endif
 //#ifdef FILE_TRANSFER
         if (c == cmdAcceptFile)
             new TransferAcceptFile(TransferDispatcher.getInstance().getTransferByJid(contact.jid.getJid()));
         if (c == cmdDeclineFile)
             TransferDispatcher.getInstance().getTransferByJid(contact.jid.getJid()).cancel();
 //#endif        
         /** login-critical section */
         if (!sd.roster.isLoggedIn()) return;
 
         if (c==cmdMessage) { 
             contact.msgSuspended=null; 
             keyGreen(); 
         }
         if (c==cmdResume) keyGreen();
         if (c==cmdQuote) Quote();
         if (c==cmdReply) Reply();
         
         if (c==cmdActions) {
 //#ifndef WMUC
             if (contact instanceof MucContact) {
                 MucContact mc=(MucContact) contact;
                 new RosterItemActions(mc, -1);
             } else
 //#endif
                 new RosterItemActions(contact, -1);
         }
 	if (c==cmdActive) new ActiveContacts(contact);
         
         if (c==cmdSubscribe) sd.roster.doSubscribe(contact);
 		
         if (c==cmdDecline) sd.roster.sendPresence(contact.bareJid, "unsubscribed", null, false);
 
 //#ifdef CLIPBOARD
 //#         if (c==cmdSendBuffer) {
 //#             String from=sd.account.toString();
 //#             String body=clipboard.getClipBoard();
 //#             //String subj=null;
 //#             
 //#             String id=String.valueOf((int) System.currentTimeMillis());
 //#             Msg msg=new Msg(Msg.MESSAGE_TYPE_OUT,from,null,body);
 //#             msg.id=id;
 //#             msg.itemCollapsed=true;
 //#             
 //#             try {
 //#                 if (body!=null && body.length()>0) {
 //#                     sd.roster.sendMessage(contact, id, body, null, null);
 //#                     if (contact.origin!=Contact.ORIGIN_GROUPCHAT) contact.addMessage(msg);
 //#                 }
 //#             } catch (Exception e) {
 //#                 contact.addMessage(new Msg(Msg.MESSAGE_TYPE_OUT,from,null,"clipboard NOT sended"));
 //#             }
 //#             redraw();
 //#         }
 //#endif
         
 //#ifdef JUICK
 //#         String body = getBodyFromCurrentMsg();
 //#         if (c == cmdJuickMessageReply) {
 //#             juickAction("", body);
 //#         } else if (c == cmdJuickSendPrivateReply) {
 //#             juickAction("PM", body);
 //#         } else if (c == cmdJuickMessageDelete) {
 //#             juickAction("D", body);
 //#         } else if (c == cmdJuickPostSubscribe) {
 //#             juickAction("S", body);
 //#         } else if (c == cmdJuickPostUnsubscribe) {
 //#             juickAction("U", body);
 //#         } else if (c == cmdJuickPostRecommend) {
 //#             juickAction("!", body);
 //#         } else if (c == cmdJuickPostShow) {
 //#             juickAction("+", body);
 //#         } else if (c == cmdJuickThings) {
 //#             viewJuickThings(body);
 //#         } else if (c == cmdJuickCommands) {
 //#             updateJuickCommands();
 //#             if (currentJuickCommands.size() > 0)
 //#                 new MyMenu(this, (Menu.MenuListener) this, SR.MS_COMMANDS, null, currentJuickCommands);
 //#         }
 //#endif
     }
     
 //#ifdef JUICK
 //#     private String getBodyFromCurrentMsg() {
 //#         Msg msg = getMessage(cursor);
 //# 
 //#         if (msg != null) {
 //#             return msg.body;
 //#         } else {
 //#             return "";
 //#         }
 //#     }
 //# 
 //#     private void juickContactNotFound() {
 //#ifdef POPUPS
 //#             setWobble(ui.controls.PopUp.TYPE_SYSTEM, "Juick", SR.MS_JUICK_CONTACT_NOT_FOUND);
 //#endif
 //#     }
 //# 
 //#     public void viewJuickThings(String str) {
 //#         if (getActualJuickContact() == null) {
 //#             juickContactNotFound();
 //#             return;
 //#         }
 //#         char[] valueChars = str.toCharArray();
 //#         int msg_length = valueChars.length;
 //#         Vector things = new Vector();
 //#         for (int i = 0; i < msg_length; i++) {
 //#             if ((i == 0) || isCharBeforeJuickThing(valueChars[i - 1])) {
 //#                 switch (valueChars[i]) {
 //#                     case '#':
 //#                     case '@':
 //#                     case '*':
 //#                         char firstSymbol = valueChars[i];
 //#                         String thing = "" + firstSymbol;
 //#                         while (i < (msg_length - 1) && isCharFromJuickThing(valueChars[++i], firstSymbol)) {
 //#                             thing = thing + valueChars[i];
 //#                         }
 //#                         while (thing.charAt(thing.length() - 1) == '.') {
 //#                             thing = thing.substring(0, thing.length() - 1);
 //#                         }
 //#                         if ((thing.length() > 1) && (things.indexOf(thing) < 0)) {
 //#                             if (i < msg_length && ((firstSymbol == '*') && (valueChars[i] == '*'))) {
 //#                                 continue;
 //#                             }
 //#                             things.addElement(thing);
 //#                         }
 //#                         if (i > 0) {
 //#                             i--;
 //#                         }
 //#                         break;
 //#                 }
 //#             }
 //#         }
 //# 
 //#         if (things.isEmpty() && (isJuickContact(contact) || isJuBoContact(contact))) {
 //#             things.addElement("@top+");
 //#             things.addElement("#");
 //#             things.addElement("##");
 //#             things.addElement("###");
 //#             things.addElement("#+");
 //#             things.addElement("*");
 //#             things.addElement("@");
 //#             things.addElement("HELP");
 //#         }
 //# 
 //#         if (!things.isEmpty()) {
 //#             new JuickThingsMenu(things, getActualJuickContact());
 //#         }
 //#     }
 //# 
 //#     public boolean isCharBeforeJuickThing(char ch) {
 //#         switch(ch) {
 //#             case '\u0020': // space
 //#             case '\u0009': // tab
 //#             case '\u000C': // formfeed
 //#             case '\n': // newline
 //#             case '\r': // carriage return
 //#             case '(':
 //#                 return true;
 //#         }
 //#         return false;
 //#     }
 //#     
 //#     public boolean isCharFromJuickThing(char ch, char type) {
 //#         boolean result = false;
 //#         switch(type) {
 //#             case '#': // #number
 //#                 result = (ch>46) && (ch<58); // '/', [0-9]
 //#                 break;
 //#             case '@': // @username
 //#                 result = ((ch>47)&&(ch<58)) // [0-9]
 //#                         || ((ch>63)&&(ch<91)) // '@', [A-Z]
 //#                         || ((ch>96)&&(ch<123)) // [a-z]
 //#                         || ((ch=='_')||(ch=='|'))
 //#                         || ((ch>44)&&(ch<47)); // [-.]
 //#                 break;
 //#             case '*': // *tag
 //#                 result = ((ch>42)&&(ch<58)) // [+,-./], [0-9]
 //#                         || ((ch>64)&&(ch<91)) // [A-Z]
 //#                         || ((ch>96)&&(ch<123)) // [a-z]
 //#                         || ((ch>1039)&&(ch<1104)) || ((ch==1105)||(ch==1025)) // [А-Я], [а-я], 'ё', 'Ё'
 //#                         || ((ch=='_')||(ch=='|')||(ch=='?')||(ch=='!')||(ch==39)) // '
 //#                         || ((ch>44)&&(ch<47)); // [-.]
 //#                 break;
 //#         }
 //#         return result;
 //#     }
 //# 
 //#     public String getTargetForJuickReply(String str) {
 //#         if ((str == null) || (str.equals("")))
 //#             return "toThings";
 //#         if (str.startsWith("Private message from @")) {
 //#             return str.substring(21, str.indexOf('\n')-1);
 //#         }
 //#         if ((str.charAt(0) != '@') && !str.startsWith("Recommended by @") && !str.startsWith("Reply by @"))
 //#             return "toThings";
 //#         int lastStrStartIndex = str.lastIndexOf('\n')+1;
 //#         if (lastStrStartIndex < 0)
 //#             return "toThings";
 //#         int numberEndsIndex = str.indexOf(" http://juick.com/", lastStrStartIndex);
 //#         if (numberEndsIndex>0) {
 //#             numberEndsIndex = str.indexOf(' ', lastStrStartIndex);
 //#             return str.substring(lastStrStartIndex, numberEndsIndex);
 //#         }
 //#         return "toThings";
 //#     }
 //# 
 //#     public void juickAction(String action, String body) {
 //#         if (getActualJuickContact() == null) {
 //#             juickContactNotFound();
 //#             return;
 //#         }
 //#         String target = getTargetForJuickReply(body);
 //#         if ((action.equals("S") || action.equals("U")) && (target.indexOf("/") > 0)) {
 //#             target = target.substring(0, target.indexOf("/"));
 //#         } else if (action.equals("PM") || action.equals("")) {
 //#             target+=" ";
 //#         }
 //#         String resultAction = action + " " + target;
 //# 
 //#         if (action.equals("+") || action.equals("")) {
 //#             resultAction = target+action;
 //#         }
 //#         try {
 //#                 Roster.me = null; Roster.me = new MessageEdit(getActualJuickContact(), resultAction);
 //#                 Roster.me.show(this);
 //#             } catch (Exception e) {/*no messages*/}
 //#     }
 //# 
 //# 
 //#     public boolean isJuickContact(Contact c) {
 //#         return sd.roster.isJuickContact(c);
 //#     }
 //# 
 //#     public boolean isJuBoContact(Contact c) {
 //#         return (c.bareJid.equals("jubo@nologin.ru")
 //#          || c.bareJid.startsWith("jubo%jubo.ru@"));
 //#     }
 //# 
 //#     public boolean noRedirrectToJuickContact(Contact c) {
 //#         return (isJuickContact(c)
 //#          || c.bareJid.equals("implusplus@gmail.com")
 //#          || c.bareJid.startsWith("implusplus%gmail.com@")
 //#          || c.bareJid.equals("tweet@excla.im")
 //#          || c.bareJid.startsWith("tweet%excla.im@")
 //#          || c.bareJid.endsWith("@twitter.tweet.im")
 //#          || (c.bareJid.indexOf("%twitter.tweet.im@") >= 0)
 //#          || c.bareJid.equals("twitter@t2p.me")
 //#          || c.bareJid.startsWith("twitter%t2p.me@")); // Not tested (2010-04-12)
 //#     }
 //# 
 //#     private Contact getActualJuickContact() {
 //#         if (noRedirrectToJuickContact(contact))
 //#             return contact;
 //#         else return sd.roster.getMainJuickContact();
 //#     }
 //#endif
 
     public void clearReadedMessageList() {
         smartPurge();
         messages=null;
         messages=new Vector();
         cursor=0;
         moveCursorHome();
         redraw();
     }
     
     public void eventLongOk() {
         super.eventLongOk();
 //#ifndef WMUC
         if (contact instanceof MucContact && contact.origin==Contact.ORIGIN_GROUPCHAT) {
             Reply();
             return;
         }
 //#endif
 //#ifdef JUICK
 //#ifdef PLUGINS
 //#         if (sd.Juick)
 //#endif
 //#         if (isJuickContact(contact) || isJuBoContact(contact)) {
 //#             if (juickPoundFork())
 //#                 return;
 //#         }
 //#endif
         keyGreen();
     }
     
     public void keyGreen(){
         if (!sd.roster.isLoggedIn()) return;       
         Roster.me = null; Roster.me = new MessageEdit(contact, contact.msgSuspended);
         Roster.me.show(this);
         contact.msgSuspended=null;
     }
     
     protected void keyClear(){
         if (!messages.isEmpty())
             clearReadedMessageList();
     }
     
     public void keyRepeated(int keyCode) {
         if (keyCode==KEY_NUM0) 
             clearReadedMessageList();
 	else 
             super.keyRepeated(keyCode);
     }
 
 //#ifdef JUICK    
 //#     public boolean juickPoundFork() { // Fork — это развилка.
 //#         String body = getBodyFromCurrentMsg();
 //#         String target = getTargetForJuickReply(body);
 //#         if (target.equals("toThings")) {
 //#             viewJuickThings(body);
 //#         } else {
 //#             switch (target.charAt(0)) {
 //#                 case '#':
 //#                     if (getActualJuickContact() == null)
 //#                         return false;
 //#                     juickAction("", body);
 //#                     break;
 //#                 case '@':
 //#                     juickAction("PM", body);
 //#                     break;
 //#             }
 //#         }
 //#         return true;
 //#     }
 //#endif
 
     public void keyPressed(int keyCode) {
         //kHold=0;
         if (keyCode==KEY_POUND) {
 //#ifndef WMUC
             if (contact instanceof MucContact && contact.origin==Contact.ORIGIN_GROUPCHAT) {
                 Reply();
                 return;
             }
 //#endif
 //#ifdef JUICK
 //#ifdef PLUGINS
 //#         if (sd.Juick)
 //#endif
 //#             if (isJuickContact(contact) || isJuBoContact(contact)) {
 //#                 if (juickPoundFork())
 //#                     return;
 //#             }
 //#endif
             keyGreen();
             return;
         }
         super.keyPressed(keyCode);
     }
 
     public void userKeyPressed(int keyCode) {
         switch (keyCode) {
             case KEY_NUM4:
                 if (cf.useTabs) {
                     savePosition();
                     sd.roster.searchActiveContact(-1); //previous contact with messages
                 }
                 else
                     super.pageLeft();
                 break;
             case KEY_NUM6:
                 if (cf.useTabs) {
                     savePosition();
                     sd.roster.searchActiveContact(1); //next contact with messages
                 }
                 else
                     super.pageRight();
                 break;
             case KEY_NUM3:
                 new ActiveContacts(contact);
                 break;
             case KEY_NUM9:
                 Quote();
                 break;
         }
     }
 
     public void touchRightPressed(){ if (cf.oldSE) showMenu(); else destroyView(); }
     public void touchLeftPressed(){ if (cf.oldSE) keyGreen(); else showMenu(); }
     public void captionPressed() {
          savePosition();
          sd.roster.searchActiveContact(1); //next contact with messages
     }
     
     private void Reply() {
         if (!sd.roster.isLoggedIn()) return;
         
         try {
             Msg msg=getMessage(cursor);
             
             if (msg==null || msg.messageType == Msg.MESSAGE_TYPE_OUT || msg.messageType == Msg.MESSAGE_TYPE_SUBJ) {
                 keyGreen();
             } else {
                 Roster.me = null; Roster.me=new MessageEdit(contact, msg.from+":");
                 Roster.me.show(this);
             }
         } catch (Exception e) {/*no messages*/}
     }
     
     private void Quote() {
         if (!sd.roster.isLoggedIn()) return;
         
         try {
             String msg=new StringBuffer()
                 .append((char)0xbb) //
                 .append(" ")
                 .append(getMessage(cursor).quoteString())
                 .append("\n")
                 .append(" ")
                 .toString();
             Roster.me = null; Roster.me=new MessageEdit(contact, msg);
             Roster.me.show(this);
             msg=null;
         } catch (Exception e) {/*no messages*/}
     }
     
 //#ifdef HISTORY
 //#ifdef LAST_MESSAGES
 //#     public final void loadRecentList() {
 //#         contact.setHistoryLoaded(true);
 //#         HistoryStorage hs = new HistoryStorage(contact.bareJid);
 //#         Vector history=hs.importData();
 //#         for (Enumeration messages2=history.elements(); messages2.hasMoreElements(); )  {
 //#             Msg message=(Msg) messages2.nextElement();
 //#             if (!isMsgExists(message)) {
 //#                 message.history=true;
 //#                 contact.msgs.insertElementAt(message, 0);
 //#             }
 //#             message=null;
 //#         }
 //#         history=null;
 //#     }
 //# 
 //#     private boolean isMsgExists(Msg msg) {
 //#         if (msg == null) return true;
 //#          for (Enumeration contactMsgs=contact.msgs.elements(); contactMsgs.hasMoreElements(); )  {
 //#             Msg message=(Msg) contactMsgs.nextElement();
 //#             if (message.body.equals(msg.body)) {
 //#                 return true;
 //#             }
 //#             message=null;
 //#          }
 //#         return false;
 //#     }
 //#endif
 //# 
 //#     private void saveMessages() {
 //#         StringBuffer histRecord=new StringBuffer("chatlog_");
 //#ifndef WMUC
 //#         if (contact instanceof MucContact) {
 //#             if (contact.origin>=Contact.ORIGIN_GROUPCHAT) {
 //#                 histRecord.append(contact.bareJid);
 //#             } else {
 //#                 String nick=contact.getJid();
 //#                 int rp=nick.indexOf('/');
 //#                 histRecord.append(nick.substring(rp+1)).append("_").append(nick.substring(0, rp));
 //#                 nick=null;
 //#             }
 //#         } else {
 //#endif
 //#             histRecord.append(contact.bareJid);
 //#ifndef WMUC
 //#         }
 //#endif
 //#         StringBuffer messageList=new StringBuffer();
 //#         if (startSelection) {
 //#             for (Enumeration select=contact.msgs.elements(); select.hasMoreElements(); ) {
 //#                 Msg mess=(Msg) select.nextElement();
 //#                 if (mess.selected) {
 //#                     messageList.append(mess.quoteString()).append("\n").append("\n");
 //#                     mess.selected=false;
 //#                     mess.highlite = mess.oldHighlite;
 //#                 }
 //#             }
 //#             startSelection = false;
 //#         } else {
 //#             for (Enumeration cmessages=contact.msgs.elements(); cmessages.hasMoreElements(); ) {
 //#                 Msg message=(Msg) cmessages.nextElement();
 //#                 messageList.append(message.quoteString()).append("\n").append("\n");
 //#             }
 //#         }
 //#         HistoryAppend.getInstance().addMessageList(messageList.toString(), histRecord.toString());
 //#         messageList=null;
 //#         histRecord=null;
 //#     }
 //#endif
     
     public final void smartPurge() {
         Vector msgs=contact.msgs;
         int cur=cursor+1;
         try {
             if (msgs.size()>0){
                 int virtCursor=msgs.size();
                 boolean delete = false;
                 int i=msgs.size();
                 while (true) {
                     if (i<0) break;
 
                     if (i<cur) {
                         if (!delete) {
                             //System.out.println("not found else");
                             if (((Msg)msgs.elementAt(virtCursor)).dateGmt+1000<System.currentTimeMillis()) {
                                 //System.out.println("can delete: "+ delPos);
                                 msgs.removeElementAt(virtCursor);
                                 //delPos--;
                                 delete=true;
                             }
                         } else {
                             //System.out.println("delete: "+ delPos);
                             msgs.removeElementAt(virtCursor);
                             //delPos--;
                         }
                     }
                     virtCursor--;
                     i--;
                 }
                 contact.activeMessage=msgs.size()-1; //drop activeMessage count
             }
        } catch (Exception e) { 
            e.printStackTrace();
        }
         
         contact.clearVCard();
         
         contact.lastSendedMessage=null;
         contact.lastUnread=0;
         contact.resetNewMsgCnt();
     }
 
     public void savePosition() {
         contact.mark = on_end ? -1 : cursor;
     }
 
     public void destroyView(){
         /*
         if (startSelection) {
             for (Enumeration select=contact.msgs.elements(); select.hasMoreElements(); ) {
                 Msg mess=(Msg) select.nextElement();
 
                 mess.selected=false;
                 mess.highlite = mess.oldHighlite;
             }
             startSelection = false;
         }
         */
         savePosition();
         sd.roster.activeContact=null;
         sd.roster.reEnumRoster(); //to reset unread messages icon for this conference in roster
         super.destroyView();
     }
 
 
     public void showMenu() {
          commandState();
          super.showMenu();
     }
 
     public boolean isHasScheme() {
         if (contact.msgs.size()<1) {
             return false;
         }
         String body=((Msg) contact.msgs.elementAt(cursor)).body;
 
         if (body.indexOf("xmlSkin")>-1) return true;
         return false;
     }
 
     public boolean isHasUrl() {
         if (contact.msgs.size()<1) {
             return false;
         }
         String body=((Msg) contact.msgs.elementAt(cursor)).body;
         if (body.indexOf("http://")>-1) return true;
         if (body.indexOf("https://")>-1) return true;
         if (body.indexOf("ftp://")>-1) return true;
         if (body.indexOf("tel:")>-1) return true;
         if (body.indexOf("native:")>-1) return true;
         return false;
     }
 
     public String touchLeftCommand(){ return (Config.getInstance().oldSE)?((contact.msgSuspended!=null)?SR.MS_RESUME:SR.MS_NEW):SR.MS_MENU; }
     public String touchRightCommand(){ return (Config.getInstance().oldSE)?SR.MS_MENU:SR.MS_BACK; }
 
 }
