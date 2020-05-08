 /*
 * EchoProfile.java    $Revision: 1.1 $ $Date: 2001/04/02 08:45:27 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.invisible.net/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.lib;
 
 
 import org.beepcore.beep.core.*;
 
 /**
  * This class acts as a sink for all replies to the <code>sendMSG()</code>.
  * The reply is received by <code>NullReplyListener</code>, freed from the
  * channel's receive buffers and discarded.
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.1 $, $Date: 2001/04/02 08:45:27 $
  */
 public class NullReplyListener implements ReplyListener
 {
   private static NullReplyListener listener = new NullReplyListener();
 
   private NullReplyListener()
   {
   }
 
   public static NullReplyListener getListener()
   {
     return listener;
   }
 
   public void receiveRPY(Message message)
   {
     ((FrameDataStream)message.getDataStream()).close();
   }
   public void receiveERR(Message message)
   {
     ((FrameDataStream)message.getDataStream()).close();
   }
   public void receiveANS(Message message)
   {
     ((FrameDataStream)message.getDataStream()).close();
   }
   public void receiveNUL(Message message)
   {
    ((FrameDataStream)message.getDataStream()).close();
   }
 }
