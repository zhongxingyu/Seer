 /*-
  * Copyright (c) 2008, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.mail;
 
 import org.logicprobe.LogicMail.conf.OutgoingConfig;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 
 public class NetworkMailSender extends AbstractMailSender {
 	private OutgoingMailClient client;
 	private OutgoingMailConnectionHandler connectionHandler;
 	private OutgoingConfig outgoingConfig;
 
 	public NetworkMailSender(OutgoingConfig outgoingConfig) {
 		super();
 		this.client = MailClientFactory.createOutgoingMailClient(outgoingConfig);
 		this.outgoingConfig = outgoingConfig;
 		this.connectionHandler = new OutgoingMailConnectionHandler(client);
 		this.connectionHandler.setListener(new MailConnectionHandlerListener() {
			public void mailConnectionRequestComplete(int type, Object tag, Object result) {
				connectionHandler_mailConnectionRequestComplete(type, tag, result);
 			}
 
             public void mailConnectionRequestFailed(int type, Object tag, Throwable exception) {
                 connectionHandler_mailConnectionRequestFailed(type, tag, exception);
             }
 		});
 		this.connectionHandler.start();
 	}
 
     /**
 	 * Gets the outgoing account configuration associated with this network mail sender.
 	 * 
 	 * @return Outgoing account configuration.
 	 */
 	public OutgoingConfig getOutgoingConfig() {
 		return this.outgoingConfig;
 	}
 	
 	public void shutdown(boolean wait) {
 		connectionHandler.shutdown(wait);
 	}
 
 	/**
 	 * Restarts the mail connection handler thread.
 	 */
 	public void restart() {
 		if(!connectionHandler.isRunning()) {
 			connectionHandler.start();
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString() {
 	    return this.outgoingConfig.toString();
 	}
 	
 	public void requestSendMessage(MessageEnvelope envelope, Message message) {
 		connectionHandler.addRequest(OutgoingMailConnectionHandler.REQUEST_SEND_MESSAGE, new Object[] { envelope, message }, new Object[] { envelope, message });
 	}
 	
	private void connectionHandler_mailConnectionRequestComplete(int type, Object tag, Object result) {
 		Object[] results;
 		switch(type) {
 		case OutgoingMailConnectionHandler.REQUEST_SEND_MESSAGE:
 			results = (Object[])result;
 			fireMessageSent((MessageEnvelope)results[0], (Message)results[1], (String)results[2]);
 			break;
         }
 	}
 	
 	private void connectionHandler_mailConnectionRequestFailed(int type, Object tag, Throwable exception) {
         switch(type) {
         case OutgoingMailConnectionHandler.REQUEST_SEND_MESSAGE:
             Object[] params = (Object[])tag;
             fireMessageSendFailed((MessageEnvelope)params[0], (Message)params[1]);
             break;
         }
     }
 }
