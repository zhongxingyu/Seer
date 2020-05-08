 // ========================================================================
 // Copyright 2008-2009 NEXCOM Systems
 // ------------------------------------------------------------------------
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at 
 // http://www.apache.org/licenses/LICENSE-2.0
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 // ========================================================================
 
 package org.cipango.sip;
 
 import java.io.IOException;
 
 import org.cipango.SipHeaders;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.mortbay.log.Log;
 
 public class ServerTransaction extends Transaction
 {	
 	private SipResponse _provisionalResponse;
     private SipResponse _gResponse;
     private SipResponse _finalResponse;
     
     private ServerTransactionListener _listener;
     
     private long gDelay = __T1;
     
 	public ServerTransaction(SipRequest request) 
     {
 		super(request, request.getTopVia().getBranch());
 		setConnection(request.getConnection());
 		
 		if (isInvite()) 
 			setState(STATE_PROCEEDING);
 		else 
 			setState(STATE_TRYING);
 	}
 	
     public void setListener(ServerTransactionListener listener)
     {
         _listener = listener;
     }
     
     public void cancel(SipRequest cancel)
     {
        _listener.handleCancel(this, cancel);
     }
     
 	public boolean handleRequest(SipRequest request)
     {
 		if (request.isAck())
         {
 			if (isInvite())
             {
 				if (_state != STATE_COMPLETED)
                 {
 					Log.info("ACK in state: {}", STATES[_state]);
                     return true;
 				}
 				_state = STATE_CONFIRMED;
 				cancelTimer(TIMER_H);
 				cancelTimer(TIMER_G);
 				if (isTransportReliable())
 					terminate();
 				else 
 					startTimer(TIMER_I, __T4);
 			} 
             else 
 				Log.warn("Dropped ACK for non-INVITE: {}", this);
             
             return true;
 		} 
         else 
         {
             if (!request.getHeader(SipHeaders.CSEQ).equals(_request.getHeader(SipHeaders.CSEQ)))
                 return false;
 
         	getServer().getTransactionManager().incrementNbRetransmission();
         	
             SipResponse response = null;
 			if (_state == STATE_PROCEEDING) 
                 response = _provisionalResponse;
             else if (_state == STATE_COMPLETED)
                 response = _finalResponse;
             
             if (response != null)
             {
                 try 
                 { 
                     doSend(response);
                 }
                catch (IOException e)
                 {
                     Log.ignore(e);
                 }
 			} 
 		}
         return true;
 	}
 	
 	public boolean isServer() 
     {
 		return true;
 	}
 	
 	public void send(SipResponse response) 
     {
 		if (_state >= STATE_COMPLETED) 
 			throw new IllegalStateException("Completed && send");
 		
         // update transaction state
 		if (isInvite()) 
         {
             switch (_state)
             {
             case STATE_PROCEEDING:
                 if (response.getStatus() < 200) 
                 {
                     _provisionalResponse = response;
                 } 
                 else if (response.getStatus() >= 300) 
                 {
                     setState(STATE_COMPLETED);
                     if (!isTransportReliable()) 
                     {
                         _gResponse = response;
                         startTimer(TIMER_G, gDelay);
                     }
                     _finalResponse = response;
                     startTimer(TIMER_H, 64*__T1);
                 } 
                 else if (response.getStatus() >= 200) 
                 {
                     terminate();
                 }
                 break;
             default:
                 throw new IllegalStateException("sendInvite && !Proceeding");
             }
         }
 		else 
         {
             switch (_state)
             {
             case STATE_TRYING:
             case STATE_PROCEEDING:
                 if (response.getStatus() < 200) 
                 {
                     _provisionalResponse = response;
                     if (_state == STATE_TRYING) 
                         setState(STATE_PROCEEDING);                    
                 } 
                 else if (response.getStatus() >= 200) 
                 {
                     setState(STATE_COMPLETED);
                     _finalResponse = response;
                     if (isTransportReliable()) 
                         terminate();
                     else 
                         //startOldTimer(TIMER_J, __T1 * 64);
                     	startTimer(TIMER_J, 64*__T1);
                 } 
                 break;
             default:
                 throw new IllegalStateException("sendNonInvite && !(state == Trying || state == Proceeding)");
             }
         }
         // send response
 		try 
         {
 			doSend(response);
         }
 		catch (IOException e) 
         {
 			Log.warn("Failed to send response {}", response);
 			//terminate();
 		}
 	}
 	
 	public boolean isTransportReliable()
 	{
 		return getConnection().getConnector().isReliable();
 	}
 	
 	private void doSend(SipResponse response) throws IOException 
     {
		getServer().getConnectorManager().send(response, getConnection());
 	}
 	
 	public void timeout(int id) 
     {
 		switch(id) 
         {
 		case TIMER_G:
 			try 
             {
 				doSend(_gResponse);
 			} 
             catch (IOException e) 
             {
 				Log.warn("Failed to retransmit G response", e);
 			}
 			gDelay = gDelay * 2;
 			startTimer(TIMER_G, Math.min(gDelay, __T2));
 			break;
 		case TIMER_H:
 			// TODO ? SipErrorListener.noAck 
 			cancelTimer(TIMER_G);
 			terminate();
 			break;
 		case TIMER_I:
 			terminate();
 			break;
 		case TIMER_J:
 			terminate();
 			break;
 		default:
 			throw new RuntimeException("!(g || h || i || j)");
 		}
 	}
 	
 	private void terminate()
     {
 		_provisionalResponse = _finalResponse = _gResponse = null;
         setState(STATE_TERMINATED);
         getCallSession().removeServerTransaction(this);
     }
 }
