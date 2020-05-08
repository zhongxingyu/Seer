 /*
  * Copyright 2010-2013 Heads Up Development Ltd.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.headsupdev.irc.impl;
 
 import org.headsupdev.irc.IRCCommand;
 import org.headsupdev.irc.IRCConnection;
 import org.headsupdev.irc.IRCListener;
 import org.headsupdev.irc.IRCServiceManager;
 import org.schwering.irc.lib.IRCEventListener;
 import org.schwering.irc.lib.IRCModeParser;
 import org.schwering.irc.lib.IRCUser;
 import org.schwering.irc.lib.IRCUtil;
 
 import java.util.Date;
 import java.util.Iterator;
 import java.util.ResourceBundle;
 
 /**
  * The internal listener that maps underlying irc commands to our framework.
  *
  * @author Andrew Williams
  * @version $Id: DefaultIRCServiceListener.java 55 2009-12-08 15:46:03Z handyande $
  * @since 1.0
  */
 public class DefaultIRCServiceListener
     implements IRCEventListener
 {
     private static ResourceBundle info = ResourceBundle.getBundle( "org.headsupdev.irc.impl.info" );
 
     private static String ADDRESS_CHARS = ":,";
 
     private static String INTRO_CHARS = "!~'";
 
     private IRCConnection conn;
 
     public DefaultIRCServiceListener( IRCConnection conn )
     {
         this.conn = conn;
     }
 
     protected IRCServiceManager getManager()
     {
         return conn.getManager();
     }
 
     public void onRegistered()
     {
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onRegistered( conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onDisconnected()
     {
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onDisconnected( conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onError( String message )
     {
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onError( message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onError( int num, String message )
     {
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onError( num, message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onInvite( String channel, IRCUser user, String invitee )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onInvite( channel, ircUser, invitee, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onJoin( String channel, IRCUser user )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onJoin( channel, ircUser, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onKick( String channel, IRCUser user, String kickee, String message )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onKick( channel, ircUser, kickee, message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onMode( String channel, IRCUser user, IRCModeParser modeParser )
     {
 /* FIXME - figure out the modeparser stuff - do we need it?
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onMode( channel, ircUser, modeParser );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
 */
     }
 
     public void onMode( IRCUser user, String string, String mode )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onMode( ircUser, string, mode, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onNick( IRCUser user, String newNick )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onNick( ircUser, newNick, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onNotice( String target, IRCUser user, String message )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onNotice( target, ircUser, message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onPart( String channel, IRCUser user, String message )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onPart( channel, ircUser, message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onPing( String ping )
     {
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onPing( ping, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
    private void doCommand( String target, org.headsupdev.irc.IRCUser user, String message, boolean replyErrors )
     {
         String command = message.split( " " )[0];
         IRCCommand match = getManager().getCommands().get( command );
         if ( match == null )
         {
            if ( replyErrors )
            {
                if ( target.equals( conn.getNick() ) )
                {
                    conn.sendMessage( user.getNick(), "Unrecognised command " + command );
                }
                else
                {
                    conn.sendMessage( target, "Unrecognised command " + command );
                }
            }
             return;
         }
         String rest = message.substring( command.length() ).trim();
 
         try
         {
             if ( target.equals( conn.getNick() ) )
             {
                 match.onPrivateCommand( user, rest, conn );
             }
             else
             {
                 match.onCommand( target, user, rest, conn );
             }
         }
         catch ( Exception e )
         {
             System.err.println( "Exception in Command " + match + ":" );
             e.printStackTrace();
         }
     }
 
     public void onPrivmsg( String target, IRCUser user, String message )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         if ( message.startsWith( "ACTION " ) )
         {
             String theMessage = message.substring( 7 ).trim();
             onAction( target, user, theMessage );
             return;
         }
         else if ( message.equals( "VERSION" ) )
         {
             onVersion( target, user );
             return;
         }
         else if ( message.equals( "TIME" ) )
         {
             onTime( target, user );
             return;
         }
 
         /* addressed through a shortcut */
         if ( message.length() > 0 && isIntroChar( message.charAt( 0 ) ) )
         {
            doCommand( target, ircUser, message.substring( 1 ).trim(), false );
             return;
         }
         else
         {
             /* addressed using our nick */
             String arg1 = message.split( " " )[0];
             if ( arg1 != null && arg1.length() != 0 && isAddressChar( arg1.charAt( arg1.length() - 1 ) ) )
             {
                 arg1 = arg1.substring( 0, arg1.length() - 1 );
 
                 if ( arg1.equals( conn.getNick() ) )
                 {
                    doCommand( target, ircUser, message.substring( arg1.length() + 1 ).trim(), true );
                     return;
                 }
             }
         }
 
         if ( target.equals( conn.getNick() ) )
         {
             /* private commands */
            doCommand( target, ircUser, message, true );
 
             Iterator listenerIter = getManager().getListeners().iterator();
             while ( listenerIter.hasNext() )
             {
                 IRCListener next = (IRCListener) listenerIter.next();
                 try
                 {
                     next.onPrivateMessage( ircUser, message, conn );
                 }
                 catch ( Exception e )
                 {
                     System.err.println( "Exception in Listener " + next + ":" );
                     e.printStackTrace();
                 }
             }
         }
         else
         {
             Iterator listenerIter = getManager().getListeners().iterator();
             while ( listenerIter.hasNext() )
             {
                 IRCListener next = (IRCListener) listenerIter.next();
                 try
                 {
                     next.onMessage( target, ircUser, message, conn );
                 }
                 catch ( Exception e )
                 {
                     System.err.println( "Exception in Listener " + next + ":" );
                     e.printStackTrace();
                 }
             }
         }
     }
 
     public void onQuit( IRCUser user, String message )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onQuit( ircUser, message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onReply( int num, String value, String message )
     {
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onReply( num, value, message, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onTopic( String channel, IRCUser user, String topic )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onTopic( channel, ircUser, topic, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onAction( String target, IRCUser user, String action )
     {
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onAction( target, ircUser, action, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onVersion( String target, IRCUser user )
     {
         conn.sendNotice( user.getNick(), IRCUtil.actionIndicator + "VERSION " + getManager().getServiceName() + " " +
             info.getString( "version" ) + " - " + getManager().getDescription() + " " );
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onVersion( target, ircUser, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void onTime( String target, IRCUser user )
     {
         conn.sendNotice( user.getNick(), IRCUtil.actionIndicator + "Time " + new Date() + " " );
         org.headsupdev.irc.IRCUser ircUser = new DefaultIRCUser( user );
 
         Iterator listenerIter = getManager().getListeners().iterator();
         while ( listenerIter.hasNext() )
         {
             IRCListener next = (IRCListener) listenerIter.next();
             try
             {
                 next.onTime( target, ircUser, conn );
             }
             catch ( Exception e )
             {
                 System.err.println( "Exception in Listener " + next + ":" );
                 e.printStackTrace();
             }
         }
     }
 
     public void unknown( String prefix, String command, String middle, String trailing )
     {
     }
 
     public boolean isAddressChar( char in )
     {
         for ( int i = 0; i < ADDRESS_CHARS.length(); i++ )
         {
             if ( in == ADDRESS_CHARS.charAt( i ) )
             {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean isIntroChar( char in )
     {
         for ( int i = 0; i < INTRO_CHARS.length(); i++ )
         {
             if ( in == INTRO_CHARS.charAt( i ) )
             {
                 return true;
             }
         }
 
         return false;
     }
 }
