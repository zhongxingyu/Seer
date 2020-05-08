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
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.sip.SipServlet;
 
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletRequest;
 
 import org.cipango.Server;
 import org.cipango.SipHandler;
 import org.cipango.SipMessage;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.sipapp.SipServletMapping;
 import org.cipango.sipapp.SipXmlConfiguration;
 import org.mortbay.jetty.handler.ContextHandler;
 import org.mortbay.jetty.handler.ContextHandler.SContext;
 import org.mortbay.jetty.servlet.ServletHandler;
 import org.mortbay.jetty.servlet.ServletHolder;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 import org.mortbay.util.MultiException;
 
 public class SipServletHandler extends ServletHandler implements SipHandler
 {
     private SipServletHolder _defaultServlet;
     private SipServletHolder _mainServlet;
 	private SipServletHolder[] _sipServlets;
 	private SipServletMapping[] _sipServletMappings;
 	private Map _sipServletNameMap;
     
     private SipAppContext _context;
     
 	public void handle(SipServletMessage message) throws IOException, ServletException 
     {
 		if (!isStarted())
 			return;
 	
 		SipMessage baseMessage = (SipMessage) message;
 		Session session = baseMessage.session();
 		
 		SipServletHolder holder = session.getHandler();
 	
 		if (holder == null)
 			throw new IllegalStateException("No holder for session " + session);
 		
 		/*
 		if (holder == null) 
 		{
 			holder = findHolder((SipServletRequest) baseMessage);
 			if (Log.isDebugEnabled()) 
 				Log.debug("sipservlet holder: {}", holder);
 			
 			if (holder != null) 
 			{
 				session.setHandler(holder); 
                 ((AppSession) session.getApplicationSession()).setContext(_context);
 			}
 		}
 		*/
 
 		if (baseMessage.isRequest()) 
 			holder.handle((ServletRequest) baseMessage, null);
 		else
 			holder.handle(null, (ServletResponse) baseMessage);
 	}
 	
     public void handle(String target, HttpServletRequest request,HttpServletResponse response, int type)
     throws IOException, ServletException
     {
     	int semi = request.getRequestURI().lastIndexOf(';');
         if (semi>=0)
  		{
  			((org.cipango.http.servlet.ConvergedSessionManager.Session) request.getSession(true)).updateSession(request);
  		}
     	super.handle(target, request, response, type);
     }
 	
 	protected void doStart() throws Exception 
 	{    
 		super.doStart();
         
         SContext servletContext = ContextHandler.getCurrentContext();
         _context = servletContext == null ? null: (SipAppContext) servletContext.getContextHandler();
         
         if (_context == null)
             Log.warn("Null context for sip handler: " + this);
         
         if (_sipServlets != null && _sipServlets.length > 0)
             _defaultServlet = _sipServlets[0];
         
 		updateSipMappings();  
                 
         if (_mainServlet == null && _sipServlets != null && (_sipServletMappings == null || _sipServletMappings.length == 0))
         {
         	if (_sipServlets.length == 1)
         		_mainServlet = _sipServlets[0];
         	else if (_sipServlets.length != 0)
         		throw new IllegalStateException("Multiple servlets and no SIP servlet mappping defined.");
         }
         
 	}
 	
 	protected void doStop() throws Exception 
 	{
 		MultiException mx = new MultiException();
 		
 		try { super.doStop(); } catch(Exception e) { mx.add(e); }
 			
 		if (_sipServlets != null) 
 		{
 	        for (int i = _sipServlets.length; i-- > 0;) 
 	        {
 	            try 
 	            {
 	            	_sipServlets[i].stop();
 	            }
 	            catch(Exception e) 
 	            {
 	                Log.warn(Log.EXCEPTION, e);
 	                //mx.add(e);
 	            }
 	        } 
 		}
         mx.ifExceptionThrow();  
 	}
     
 	protected void servletInitialized(SipServlet servlet)
 	{
 		((Server) getServer()).servletInitialized(_context, servlet);
 	}
 	
     public SipServletHolder getDefaultServlet()
     {
    	if (_mainServlet == null)
     		return _defaultServlet;
    	else 
     		return _mainServlet;
     }
     
     public SipServletHolder getMainServlet()
     {
         return _mainServlet;
     }
 	
 	protected void updateSipMappings() 
     {
         if (_sipServlets == null)
         {
             _sipServletNameMap = null;
         }
         else
         {   
             HashMap nm = new HashMap();
             
             for (int i = 0; i < _sipServlets.length; i++)
             {
                 nm.put(_sipServlets[i].getName(), _sipServlets[i]);
                 _sipServlets[i].setServletHandler(this);
             }
             _sipServletNameMap = nm;
         }
 	}
 	
 	public void initializeSip() throws Exception 
 	{
 		MultiException mx = new MultiException();
 
 		if (_sipServlets != null) 
 		{
 	        // Sort and Initialize servlets
 	        SipServletHolder[] servlets = (SipServletHolder[]) _sipServlets.clone();
 	        Arrays.sort(servlets);
 	        for (int i=0; i<servlets.length; i++) 
 	        {
 	            try 
 	            {
 	                servlets[i].start();
 	            }
 	            catch(Exception e) 
 	            {
 
 	            	Log.debug(Log.EXCEPTION, e);
 	                mx.add(e);
 	            }
 	        } 
 	        mx.ifExceptionThrow();  
 		}
 	}
 	
 	public SipServletHolder findHolder(SipServletRequest request) 
 	{
 		if (_mainServlet != null)
 			return _mainServlet;
 		
 		if (_sipServletMappings != null) 
 		{
 			for (int i = 0; i < _sipServletMappings.length; i++) 
 			{
 				SipServletMapping mapping = _sipServletMappings[i];
 				if (mapping.getMatchingRule().matches(request)) 
 					return (SipServletHolder) _sipServletNameMap.get(mapping.getServletName());
 			}
 		}
 		return null;
 	}
     
     public SipServletHolder getHolder(String name)
     {
         return (SipServletHolder) _sipServletNameMap.get(name);
     }
 	
 	public SipServletHolder[] getSipServlets() 
 	{
 		return _sipServlets;
 	}
 	
 	
 	public SipServletHolder newSipServletHolder(Class servlet)
 	{
 		return new SipServletHolder(servlet);
 	}
 	
 	public void addSipServlet(SipServletHolder servlet) 
 	{
 		SipServletHolder[] holders = getSipServlets();
 		
 		if (holders != null) 
 			holders = (SipServletHolder[]) holders.clone();
 		
 		setSipServlets((SipServletHolder[]) LazyList.addToArray(holders, servlet, SipServletHolder.class));
 	}
 	
 	public void addSipServletWithMapping(SipServletHolder servlet, SipServletMapping mapping) 
 	{
 		SipServletHolder[] holders = getSipServlets();
 		
 		if (holders != null) 
 			holders = (SipServletHolder[]) holders.clone();
 		
 		try
 	    {
 			setSipServlets(
 		    		   (SipServletHolder[]) LazyList.addToArray(holders, servlet,
 		    				   SipServletHolder.class));
 			setSipServletMappings(
 		    		   (SipServletMapping[]) LazyList.addToArray(getSipServletMappings(), 
 		    				   mapping,
 		    				   SipServletMapping.class));
 			
 	        if (_defaultServlet == null)
 	            _defaultServlet = servlet;
 			
 			servlet.start();
 	    }
 		catch (Exception e)
         {
             setSipServlets(holders);
             if (e instanceof RuntimeException)
                 throw (RuntimeException) e;
             throw new RuntimeException(e);
         }
 	}
 	
     public SipServletHolder getSipServlet(String name)
     {
         return (SipServletHolder) _sipServletNameMap.get(name);
     }
 	
 	public SipServletHolder removeSipServlet(String servletName)
 	{
 		SipServletHolder holder = getSipServlet(servletName);
 		
 	    setServlets((ServletHolder[])LazyList.removeFromArray(super.getServlets(), holder));
 	       
 	    SipServletMapping[] mappings = getSipServletMappings();
         for (int i = 0; mappings != null && i < mappings.length; i++) 
         {
         	if (mappings[i] != null) 
         	{
 	        	String name = mappings[i].getServletName();
 	        	if (servletName.equals(name)) 
 	        	    setSipServletMappings(
 	        	    	(SipServletMapping[])LazyList.removeFromArray(getServletMappings(), mappings[i]));
         	}
         }
         
         return holder;
 	}
 	
 	public void setSipServlets(SipServletHolder[] holders) 
 	{
 		if (getServer() != null) 
 			getServer().getContainer().update(this, _sipServlets, holders, "sipServlet", true);
 	
 		_sipServlets = holders;
 		updateSipMappings();
 	}
 	
 	public void setSipServletMappings(SipServletMapping[] sipServletMappings)
 	{
 		if (getServer() != null)
             getServer().getContainer().update(this, _sipServletMappings, sipServletMappings, "sipServletMapping", true);
             
         _sipServletMappings = sipServletMappings;
     }
 	
 	public void setMainServletName(String name)
 	{
 		SipServletHolder previous = _mainServlet;
 		_mainServlet = getSipServlet(name);
 		if (getServer() != null)
 			getServer().getContainer().update(this, previous, _mainServlet, "mainServlet", true);
 	}
 	
 	public SipServletMapping[] getSipServletMappings()
 	{
 		return _sipServletMappings;
 	}
 }
