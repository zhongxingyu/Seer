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
 
 package org.cipango.servlet;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.URI;
 
 import org.cipango.Server;
 import org.cipango.SipException;
 import org.cipango.SipHandler;
 import org.cipango.SipMessage;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.sip.ServerTransaction;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.ExceptionUtil;
 import org.mortbay.jetty.handler.AbstractHandler;
 import org.mortbay.log.Log;
 
 public class SipSessionHandler extends AbstractHandler implements SipHandler
 {
 	public void handle(String target, HttpServletRequest request,
 			HttpServletResponse response, int dispatch) throws IOException,
 			ServletException 
 	{
 		throw new UnsupportedOperationException("sip-only handler");
 	}
 
 	
 	public void handle(SipServletMessage message) throws IOException,
 			ServletException 
 	{
 		SipMessage baseMessage = (SipMessage) message;
 		
 		if (baseMessage.isRequest())
 		{
 			SipRequest request = (SipRequest) message;
 			
 			// preprocess route
 
 			SipURI paramUri = null;
 			
 			Address route = request.getTopRoute();
			if (route != null && ((Server) getServer()).getTransportManager().isLocalUri(route.getURI()))
 			{
 				request.removeTopRoute();
 				paramUri = (SipURI) route.getURI();
 				request.setPoppedRoute(route);
 			}
 			else 
 			{
 				URI uri = request.getRequestURI();
 				if (uri.isSipURI())
 					paramUri = (SipURI) uri;
 			}
 			request.setParamUri(paramUri);
 			
 			Session session = null;
 			
 			if (request.isInitial())
 			{
 				SipAppContext context = request.getContext();
 				SipServletHolder handler = ((SipServletHandler) context.getServletHandler()).findHolder(request);
 		
 				AppSession appSession;
 				/*
 				String key = context.getSipApplicationKey(request);
 				if (key != null)
 					appSession = getCallManager().getApplicationSessionByKey(context, request, key);
 				else
 				{
 		            appSession = request.getCall().newSession();
 		            appSession.setContext(context);
 				}
 				*/
 				appSession = request.getCall().newSession();
 				appSession.setContext(context);
 				
 				session = appSession.newSession();
 				session.setHandler(handler);
 	        
 				session.setSubscriberURI(request.getSubscriberURI());
 				session.setRegion(request.getRegion());
 			
 		        if (Log.isDebugEnabled())
 		            Log.debug("new session {}", session);
 			}
 			else
 			{
 				session = request.getCall().findSession(request);
 				
 				if (session == null) 
 	            {
 					if (!request.isAck()) 
 	                {
 						SipResponse response = (SipResponse) request.createResponse(SipServletResponse.SC_CALL_LEG_DONE);
 						((ServerTransaction) request.getTransaction()).send(response);
 					}
 					return;
 				}
 			}
 			if (request.isInvite()) 
 	        { 
 				SipResponse response = (SipResponse) request.createResponse(SipServletResponse.SC_TRYING);
 				((ServerTransaction) request.getTransaction()).send(response);
 			}
 			
 			request.setSession(session);
 	        ((ServerTransaction) request.getTransaction()).setListener(session.getSession());
 	        
 	        try
 	        {
 	            session.getSession().handleRequest(request);
 	        }
 	        catch (Exception e)
 	        {
 	        	if (!request.isAck() && !request.isCommitted())
 	        	{
 	        		int code = SipServletResponse.SC_SERVER_INTERNAL_ERROR;
 	        		if (e instanceof SipException)
 	        			code = ((SipException) e).getCode();
 	        		
 	        		SipServletResponse response;
 	        		if (code == SipServletResponse.SC_SERVER_INTERNAL_ERROR)
 	        		{
 	        			response = request.createResponse(
 	    	        			SipServletResponse.SC_SERVER_INTERNAL_ERROR,
 	    	        			"Error in handler: " + e.getMessage());
 	        			ExceptionUtil.fillStackTrace(response, e);
 	        		}
 	        		else
 	        		{
 	        			response = request.createResponse(code);
 	        		}
 		        	response.send();
 	        	}
 	        	else
 	        	{
 	        		Log.debug(e);
 	        	}
 	        }
 		}
 	}
 }
