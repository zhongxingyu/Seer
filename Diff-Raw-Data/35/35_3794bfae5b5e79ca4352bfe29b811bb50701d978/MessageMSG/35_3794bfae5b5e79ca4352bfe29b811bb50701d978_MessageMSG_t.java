 /*
 * MessageMSG.java  $Revision: 1.7 $ $Date: 2002/04/30 04:06:22 $
  *
  * Copyright (c) 2001 Invisible Worlds, Inc.  All rights reserved.
  *
  * The contents of this file are subject to the Blocks Public License (the
  * "License"); You may not use this file except in compliance with the License.
  *
  * You may obtain a copy of the License at http://www.beepcore.org/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied.  See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  */
 package org.beepcore.beep.core;
 
 /**
  * Represents received BEEP MSG messages. Provides methods to reply to
  * the MSG.
  *
  * @author Eric Dixon
  * @author Huston Franklin
  * @author Jay Kint
  * @author Scott Pead
 * @version $Revision: 1.7 $, $Date: 2002/04/30 04:06:22 $
  *
  */
 class MessageMSG extends Message
 {
     MessageMSG(Channel channel, int msgno, InputDataStream data) {
         super(channel, msgno, data, MESSAGE_TYPE_MSG);
     }
 
     /**
      * Sends a message of type ANS.
      *
      * @param stream Data to send in the form of <code>OutputDataStream</code>.
      *
      * @see OutputDataStream
      * @see MessageStatus
      * @see #sendNUL
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendANS(OutputDataStream stream) throws BEEPException
     {
         MessageStatus m;
 
         synchronized (this) {
             // reusing ansno (initialized to -1) from Message since
             // this is a MSG
             ++ansno;
 
            m = new MessageStatus(this.channel, MESSAGE_TYPE_ANS, this.msgno,
                                  this.ansno, stream);
         }
 
         this.channel.sendMessage(m);
         return m;
     }
 
     /**
      * Sends a message of type ERR.
      *
      * @param error Error to send in the form of <code>BEEPError</code>.
      *
      * @see BEEPError
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendERR(BEEPError error) throws BEEPException
     {
         OutputDataStream stream =
             new StringOutputDataStream(error.createErrorMessage());
         MessageStatus m = new MessageStatus(this.channel, MESSAGE_TYPE_ERR,
                                             this.msgno, stream);
         this.channel.sendMessage(m);
         return m;
     }
 
     /**
      * Sends a message of type ERR.
      *
      * @param code <code>code</code> attibute in <code>error</code> element.
      * @param diagnostic Message for <code>error</code> element.
      *
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendERR(int code, String diagnostic)
         throws BEEPException
     {
         String error = BEEPError.createErrorMessage(code, diagnostic);
         MessageStatus m = new MessageStatus(this.channel, MESSAGE_TYPE_ERR,
                                             this.msgno,
                                             new StringOutputDataStream(error));
         this.channel.sendMessage(m);
         return m;
     }
 
     /**
      * Sends a message of type ERR.
      *
      * @param code <code>code</code> attibute in <code>error</code> element.
      * @param diagnostic Message for <code>error</code> element.
      * @param xmlLang <code>xml:lang</code> attibute in <code>error</code>
      *                element.
      *
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendERR(int code, String diagnostic, String xmlLang)
         throws BEEPException
     {
         String error = BEEPError.createErrorMessage(code, diagnostic, xmlLang);
         MessageStatus m = new MessageStatus(this.channel, MESSAGE_TYPE_ERR,
                                             this.msgno,
                                             new StringOutputDataStream(error));
         this.channel.sendMessage(m);
         return m;
     }
 
     /**
      * Sends a message of type NUL.
      *
      * @see MessageStatus
      * @see #sendANS
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendNUL() throws BEEPException
     {
        OutputDataStream ds = new ByteOutputDataStream(new byte[0]);
         MessageStatus m = new MessageStatus(this.channel, MESSAGE_TYPE_NUL,
                                            this.msgno, ds);
         this.channel.sendMessage(m);
         return m;
     }
 
     /**
      * Sends a message of type RPY.
      *
      * @param stream Data to send in the form of <code>OutputDataStream</code>.
      *
      * @see OutputDataStream
      * @see MessageStatus
      *
      * @return MessageStatus
      *
      * @throws BEEPException if an error is encoutered.
      */
     public MessageStatus sendRPY(OutputDataStream stream) throws BEEPException
     {
         MessageStatus m = new MessageStatus(this.channel, MESSAGE_TYPE_RPY,
                                             this.msgno, stream);
         this.channel.sendMessage(m);
         return m;
     }
 }
