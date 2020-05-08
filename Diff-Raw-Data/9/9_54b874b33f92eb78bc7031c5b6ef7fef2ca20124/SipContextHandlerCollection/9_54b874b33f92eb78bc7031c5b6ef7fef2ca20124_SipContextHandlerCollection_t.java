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
 
 package org.cipango.handler;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.ServletParseException;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.ar.SipApplicationRouter;
 import javax.servlet.sip.ar.SipApplicationRouterInfo;
 import javax.servlet.sip.ar.SipApplicationRoutingDirective;
 
 import org.cipango.Server;
 import org.cipango.SipHandler;
 import org.cipango.SipMessage;
 import org.cipango.SipParams;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.URIFactory;
 import org.cipango.ar.RouterInfoUtil;
 import org.cipango.servlet.SipSessionHandler;
 import org.cipango.sip.TransactionManager;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.ExceptionUtil;
 import org.cipango.util.ID;
 import org.mortbay.component.LifeCycle;
 import org.mortbay.jetty.Handler;
 
 import org.mortbay.jetty.handler.ContextHandlerCollection;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 
 /**
  * Handler which performs application selection. 
  * It extends the HTTP {@link ContextHandlerCollection} handler but application selection 
  * is performed based on the {@link SipApplicationRouter} rather than context path.
  */
 public class SipContextHandlerCollection extends ContextHandlerCollection implements SipHandler
 {  
     private SipAppContext[] _sipContexts;
     private SipHandler _handler;
     
 	public SipContextHandlerCollection() 
     {
 		super();
 		setContextClass(SipAppContext.class);
 		
 		CallHandler callHandler = new CallHandler();
 		TransactionManager txHandler = new TransactionManager();
 		SipSessionHandler sessionHandler = new SipSessionHandler();
 		
 		callHandler.setHandler(txHandler);
 		txHandler.setHandler(sessionHandler);
 		
 		_handler = callHandler;
 	}
 	
 	@Override
 	protected void doStart() throws Exception
 	{
 		if (_handler instanceof LifeCycle)
 			((LifeCycle) _handler).start();
 		super.doStart();
 	}
 	
 	@Override
 	public void setServer(org.mortbay.jetty.Server server)
 	{
 		_handler.setServer(server);
 		super.setServer(server);
 	}
 	
 	public SipAppContext[] getSipContexts()
 	{
 		return _sipContexts;
 	}
 	
 	public SipAppContext getContext(String name)
 	{
 		if (_sipContexts != null)
 		{
 			for (int i = 0; i < _sipContexts.length; i++)
 			{
 				if (_sipContexts[i].getName().equals(name))	
 					return _sipContexts[i];
 			}
 		}
 		return null;
 	}
 	
 	@Override
     public void setHandlers(Handler[] handlers)
     {
     	super.setHandlers(handlers);
         
         Object sipHandlers = null;
         if (handlers != null)
         {
             for (int i = 0; i < handlers.length; i++)
             {
                 if (handlers[i] instanceof SipAppContext)
                     sipHandlers = LazyList.add(sipHandlers, handlers[i]);
             }
         }
         _sipContexts = (SipAppContext[]) LazyList.toArray(sipHandlers, SipAppContext.class);
     }
 
 	private boolean isInitial(SipRequest request)
 	{
 		return ((request.getTo().getParameter(SipParams.TAG) == null) && !request.isCancel());
 	}
 	
 	public void handle(SipServletMessage message) throws ServletException, IOException 
     {
 		SipMessage baseMessage = (SipMessage) message;
 		
 		if (baseMessage.isRequest())
 		{
 			SipRequest request = (SipRequest) message;
 			
 			if (isInitial(request))
 	        {
 				request.setInitial(true);
 				
 				SipApplicationRouterInfo routerInfo = null;
 				SipAppContext context = null;
 				
 				try
 				{
 					Address route = request.getTopRoute();
 					if (route != null && ((Server)getServer()).getTransportManager().isLocalUri(route.getURI()))
 					{
 						SipURI uri = (SipURI) route.getURI();
 						if ("router-info".equals(uri.getUser()))
 						{
 							request.removeTopRoute();
 							routerInfo = RouterInfoUtil.decode(uri);
 						}
 					}
 					
 					if (routerInfo == null)
 					{
 						routerInfo = ((Server) getServer()).getApplicationRouter().getNextApplication(
 							request, null, SipApplicationRoutingDirective.NEW, null, null);
 					}
 				}
 				catch (Throwable t) 
 				{
 					if (!request.isAck())
 					{
 						SipResponse response = new SipResponse(
 								request,
 								SipServletResponse.SC_SERVER_INTERNAL_ERROR,
 								"Application router error: " + t.getMessage());
 						ExceptionUtil.fillStackTrace(response, t);
 						((Server) getServer()).getTransportManager().send(response, request);
 					}
 					return;
 				}
 				
				if (routerInfo != null && routerInfo.getNextApplicationName() != null)
 				{
 					/*
 					String[] routes = routerInfo.getRoutes();
 					try
 					{
 						if (SipRouteModifier.ROUTE == routerInfo.getRouteModifier() && routes != null)
 						{
 							NameAddr topRoute = new NameAddr(routes[0]);
 							if (getTransportLayer().isLocalUri(topRoute.getURI()))
 								request.setPoppedRoute(topRoute);
 							else
 							{
 								for (int i = routes.length; i >= 0; --i)
 									request.pushRoute(new NameAddr(routes[i]));
 								request.send();
 								return;
 							}
 						}
 						else if (SipRouteModifier.ROUTE_BACK == routerInfo.getRouteModifier() && routes != null)
 						{
 							Address ownRoute = getTransportLayer().getContact(SipConnectors.getOrdinal(SipConnectors.TCP));
 							ownRoute.getURI().setParameter("lr", null);
 							// TODO encode AR state in own route
 							request.pushRoute(ownRoute);
 							for (int i = routes.length; i >= 0; --i)
 								request.pushRoute(new NameAddr(routes[i]));
 							request.send();
 							return;
 						} 
 						else if (routes == null 
 								&& (SipRouteModifier.ROUTE_BACK == routerInfo.getRouteModifier() || SipRouteModifier.ROUTE == routerInfo.getRouteModifier()))
 						{
 							Log.debug("Router info set route modifier to ROUTE_BACK but no route provided, assume NO_ROUTE");
 						}
 					}
 					catch (Exception e)
 					{
 						// Could have ServletParseException or IllegalArgumentException on pushRoute
 						SipResponse response = (SipResponse) request.createResponse(
 	    	        			SipServletResponse.SC_SERVER_INTERNAL_ERROR,
 	    	        			"Error in handler: " + e.getMessage());
 	    	        	fillStackTrace(response, e);
 	    	        	((ServerTransaction) request.getTransaction()).send(response);
 	    	        	return;
 					}
 					*/
 					request.setStateInfo(routerInfo.getStateInfo());
 					request.setRegion(routerInfo.getRoutingRegion());
 					
 					String s = routerInfo.getSubscriberURI();
 					if (s != null)
 					{
 						try
 						{
 							request.setSubscriberURI(URIFactory.parseURI(s));
 						}
 						catch (ServletParseException e)
 						{
 							Log.debug(e);
 						}
 					}
 					
 					String applicationName = routerInfo.getNextApplicationName();
 					context = (SipAppContext) getContext(applicationName);
 					
 					//System.out.println("application : " + applicationName + " context " + context);
 					if (Log.isDebugEnabled())
 						Log.debug("AR returned application {} for initial request {}", applicationName, request.getMethod());
 				}
 				
 				if (context == null)
 				{
 					if (!request.isAck())
 					{
 						SipResponse response = new SipResponse(request, SipServletResponse.SC_NOT_FOUND, null);
 						response.to().setParameter(SipParams.TAG, ID.newTag());
 						((Server) getServer()).getTransportManager().send(response, request);
 					}
 					return;
 				}				
 				request.setContext(context);
 			}
 		}		
 		_handler.handle(baseMessage);
     }
 }
