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
 
 import org.headsupdev.irc.IRCServiceManager;
 
 import junit.framework.TestCase;
 
 /**
  * A simple test of the IRCServiceManager framework
  *
  * @author Andrew Williams
  * @version $Id: IRCServiceManagerTest.java 55 2009-12-08 15:46:03Z handyande $
  * @since 1.0
  */
 public class IRCServiceManagerTest
     extends TestCase
 {
 
     public void testStartup()
         throws Exception
     {
         IRCServiceManager ircManager = new DefaultIRCServiceManager();
         assertNotNull( ircManager );
 // TODO somehow check if we can call out on irc port first...
 //        ircManager.connect( "irc.codehaus.org", "nick1234", "user", "real" );
 
 //        ircManager.join( "#test" );
 //        Thread.sleep( 2000 ); // this seems to help establish the connection
 //        assertNotNull( ircManager.getTopic( "#test" ) );
 
 //        ircManager.disconnect( "test ending" );
     }
 
     public void testCatching()
         throws Exception
     {
         String exception = "none";
 
         final IRCServiceManager ircManager = new DefaultIRCServiceManager();
         DefaultIRCServiceListener listeners = new DefaultIRCServiceListener( new DefaultIRCConnection( null, ircManager )
         {
             @Override
             public void checkConnected()
             {
                 // not testing this function
             }
 
             @Override
             public void sendMessage( String to, String message )
             {
                 // not testing this function
             }

            @Override
            public String getNick()
            {
                return "test";
            }
         } );
         try
         {
             listeners.onPrivmsg( "#test", new org.schwering.irc.lib.IRCUser( "nick", "user", "host" ), "~broken test" );
         }
         catch ( Exception e )
         {
             exception = e.getMessage();
         }
 
         assertEquals( "none", exception );
     }
 }
