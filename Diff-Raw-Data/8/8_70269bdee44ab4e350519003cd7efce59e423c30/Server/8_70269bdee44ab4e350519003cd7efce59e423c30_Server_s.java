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
 
 package org.cipango;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.SipServlet;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.URI;
 import javax.servlet.sip.ar.SipApplicationRouter;
 import javax.servlet.sip.ar.SipApplicationRouterInfo;
 
 import org.cipango.ar.ApplicationRouterFactory;
 import org.cipango.ar.RouterInfoUtil;
 import org.cipango.handler.SipContextHandlerCollection;
 import org.cipango.sip.ClientTransaction;
 import org.cipango.sip.ClientTransactionListener;
 import org.cipango.sip.SipConnector;
 import org.cipango.sip.TransactionManager;
 import org.cipango.sip.TransportManager;
 import org.cipango.sipapp.SipAppContext;
 import org.mortbay.component.LifeCycle;
 import org.mortbay.log.Log;
 import org.mortbay.thread.BoundedThreadPool;
 import org.mortbay.thread.ThreadPool;
 import org.mortbay.util.MultiException;
 import org.mortbay.util.TypeUtil;
 
 /**
  * Cipango SIP/HTTP Servlet Server. 
  * It extends Jetty HTTP Server and is in addition a SIP handler.
  */
 public class Server extends org.mortbay.jetty.Server implements SipHandler
 {
 	private static String __sipVersion = (Server.class.getPackage() != null && Server.class.getPackage().getImplementationVersion() != null)
    	?Server.class.getPackage().getImplementationVersion() : "2.0.x";
     	
 	private ThreadPool _sipThreadPool;
     
     private TransportManager _transportManager = new TransportManager();
     private TransactionManager _transactionManager = new TransactionManager();
     
     private CallManager _callManager = new CallManager();    
     private IdManager _idManager;
     private SipApplicationRouter _applicationRouter;
 
     private long _statsStartedAt = -1;
     private Object _statsLock = new Object();
     
     private long _messages;
     
     public Server()
     {
     	setTransportManager(_transportManager);
 		setTransactionManager(_transactionManager);
 		setCallManager(_callManager);
     }
     
 	
 	@Override
 	protected void doStart() throws Exception 
     {
 		Log.info("cipango-" + __sipVersion);
 		SipGenerator.setServerVersion(__sipVersion);
 
 		MultiException mex = new MultiException();
 		
 		if (_sipThreadPool == null) 
 			setSipThreadPool(new BoundedThreadPool());
 
 		try
 		{
 			if (_sipThreadPool instanceof LifeCycle)
 				((LifeCycle) _sipThreadPool).start();
 		}
 		catch (Throwable t) { mex.add(t); }
 
 		_idManager = new IdManager();
 		
 		try
 		{
 			super.doStart();
 		}
 		catch (Throwable t) { mex.add(t); }
 
 		if (_applicationRouter == null)
 			setApplicationRouter(ApplicationRouterFactory.newApplicationRouter());
 		if (_callManager == null)
 			setCallManager(new CallManager());
 		
 		try 
 		{
 			_applicationRouter.init();
 			
 			List<String> appNames = new ArrayList<String>();
 			SipAppContext[] contexts = ((SipContextHandlerCollection) getHandler()).getSipContexts();
 			if (contexts != null)
 			{
 				for (SipAppContext context : contexts)
 					appNames.add(context.getName());
 			}
 			_applicationRouter.applicationDeployed(appNames);
 			
 			_callManager.start();
 			_transportManager.start();
 			
 			if (contexts != null)
 			{
 				for (SipAppContext context: contexts)
 					context.initialized();
 			}
 		}
 		catch (Throwable t) { mex.add(t); }
 		
 		mex.ifExceptionThrow();
 	}
 	
 	@Override
     protected void doStop() throws Exception
     {
     	MultiException mex = new MultiException();
         
         try 
         {
         	_applicationRouter.destroy();
         	_transportManager.stop();
     		_callManager.stop();
 		} 
         catch (Throwable e) { mex.add(e); }
         
         try 
         {
 			super.doStop();
 		} 
         catch (Throwable e) { mex.add(e); }
         
         try
         {
         	if (_sipThreadPool instanceof LifeCycle)
         		((LifeCycle) _sipThreadPool).stop();
         }
         catch (Throwable e) { mex.add(e); }
        
         mex.ifExceptionThrow();
     }
     
 
 	public void setApplicationRouter(SipApplicationRouter applicationRouter)
 	{
 		_applicationRouter = applicationRouter;
 	}
 	
 	public SipApplicationRouter getApplicationRouter()
 	{
 		return _applicationRouter;
 	}
 	
     public void applicationDeployed(SipAppContext context)
     {
     	if (isStarted())
     		_applicationRouter.applicationDeployed(Collections.singletonList(context.getName()));
     }
     
     public void applicationUndeployed(SipAppContext context)
     {
     	if (isStarted())
     		_applicationRouter.applicationUndeployed(Collections.singletonList(context.getName()));
     }
     
     public void servletInitialized(SipAppContext context, SipServlet servlet)
     {
     	if (isStarted())
     		context.fireServletInitialized(servlet);
     }
     
     /**
      * AR for outgoing message
      */
     public ClientTransaction sendRequest(SipRequest request, ClientTransactionListener listener) 
     {
     	if (!request.isInitial())
     	{
     		return request._session.sendRequest(request, listener);
     	}
     	else 
     	{
     		SipApplicationRouterInfo routerInfo = null;
     		try
     		{
 	    		routerInfo = _applicationRouter.getNextApplication(
 	    				request,
 	    				request.getRegion(),
 	    				request.getRoutingDirective(),
 	    				null,
 	    				request.getStateInfo());
 	    		
 	    		if (routerInfo != null)
 	    		{
 	    			SipConnector defaultConnector = _transportManager.getDefaultConnector();
 	    			SipURI internalRoute = new SipURIImpl(null, defaultConnector.getHost(), defaultConnector.getPort());
 	    			RouterInfoUtil.encode(internalRoute, routerInfo);
 
 	    			internalRoute.setLrParam(true);
 	    			request.pushRoute(internalRoute);
 	    		}
     		}
 			catch (Throwable t) 
 			{
 				// TODO, send 500 sync/async ?
 				Log.warn(t);
 			}
 			return request._session.sendRequest(request, listener);
        	}
     }
 	
     /**
      * SIP handler.
      */
 	public void handle(SipServletMessage message) throws IOException, ServletException
 	{
 		if (isStatsOn())
 		{
 			synchronized (_statsLock) 
 			{
 				_messages++;
 			}
 		}
     	
 		((SipHandler) getHandler()).handle(message);
 	}
 	
 	public void setTransactionManager(TransactionManager transactionManager) 
 	{
 		getContainer().update(this, _transactionManager, transactionManager, "transactionManager", true);
 		_transactionManager = transactionManager;
 		_transactionManager.setServer(this);
 	}
 	
 	public void setTransportManager(TransportManager transportManager)
 	{
 		getContainer().update(this, _transportManager, transportManager, "transportManager", true);
 		_transportManager = transportManager;
 		_transportManager.setServer(this);
 	}
 	
 	public void setSipThreadPool(ThreadPool sipThreadPool) 
 	{
 		getContainer().update(this, _sipThreadPool, sipThreadPool, "sipThreadPool", true);
 		_sipThreadPool = sipThreadPool;
 	}
 	
 	public void setCallManager(CallManager callManager) 
 	{
 		getContainer().update(this, _callManager, callManager, "callManager", true);
 		_callManager = callManager;
 		_callManager.setServer(this);
 	}
 	
 	public ThreadPool getSipThreadPool()
 	{
 		return _sipThreadPool;
 	}
 	
 	public TransportManager getTransportManager()
 	{
 		return _transportManager;
 	}
 	
 	public TransactionManager getTransactionManager()
 	{
 		return _transactionManager;
 	}
 	
 	public CallManager getCallManager()
 	{
 		return _callManager;
 	}
 	
 	public IdManager getIdManager()
 	{
 		return _idManager;
 	}
 	
 	public void statsReset()
 	{
 		synchronized (_statsLock)
 		{
 			_statsStartedAt = _statsStartedAt == -1 ? -1 : System.currentTimeMillis();
 			_messages = 0;
 		}
 	}
 	
 	public void statsAllReset()
 	{
 		statsReset();
 		getCallManager().statsReset();
 		getTransportManager().statsReset();
 		getTransactionManager().statsReset();
 	}
 	
 	public void setAllStatsOn(boolean on) 
 	{
 		setStatsOn(on);
 		if (getCallManager() != null)
 			getCallManager().setStatsOn(on);
 		getTransportManager().setStatsOn(on);		
 		getTransactionManager().setStatsOn(on);
 	}
 	
 	public boolean getAllStatsOn() 
 	{
 		return isStatsOn()
 			&& getCallManager().isStatsOn()
 			&& getTransportManager().isStatsOn()
 			&& getTransactionManager().isStatsOn();
 	}
 	
 	public void setStatsOn(boolean on) 
 	{
         if (on && _statsStartedAt != -1) 
         	return;
         
         Log.info("Statistics set to " + on);
         statsReset();
         _statsStartedAt = on ? System.currentTimeMillis() : -1;
     }
 	
 	public boolean isStatsOn() 
     {
 		return  _statsStartedAt != -1;
 	}
 	
 	public long getMessages()
 	{
 		return _messages;
 	}
 
 	public static String getSipVersion()
 	{
 		return __sipVersion;
 	}
 	
 	@Override
 	public String toString()
 	{
 		return "cipango-" + __sipVersion;
 	}
 }
