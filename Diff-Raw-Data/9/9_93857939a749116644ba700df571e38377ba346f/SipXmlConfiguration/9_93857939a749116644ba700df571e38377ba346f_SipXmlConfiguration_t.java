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
 
 package org.cipango.sipapp;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.EventListener;
 import java.util.Iterator;
 
 import javax.servlet.Servlet;
 import javax.servlet.UnavailableException;
 
 import org.cipango.servlet.SipServletHandler;
 import org.cipango.servlet.SipServletHolder;
 import org.cipango.sipapp.rules.*;
 import org.mortbay.jetty.webapp.Configuration; 
 import org.mortbay.jetty.webapp.WebAppContext;
 import org.mortbay.jetty.webapp.WebXmlConfiguration;
 import org.mortbay.log.Log;
 import org.mortbay.resource.Resource;
 import org.mortbay.util.LazyList;
 import org.mortbay.xml.XmlParser;
 
 public class SipXmlConfiguration implements Configuration 
 {
 	public static final int VERSION_10 = 10;
 	public static final int VERSION_11 = 11;
 	private WebAppContext _context;
 	private XmlParser _xmlParser;
 	
 	protected int _version;
 	private String _appName;
 	
 	protected Object _listeners;
 	protected Object _servlets;
 	protected Object _servletMappings;
 	protected String _mainServlet;
 	
 	protected Object _listenerClasses;
 	
 	public SipXmlConfiguration() 
 	{
 		_xmlParser = sipXmlParser();
 	}
 	
 	public static XmlParser sipXmlParser() 
 	{
 		XmlParser xmlParser = WebXmlConfiguration.webXmlParser();
 		
 		URL dtd10 = SipAppContext.class.getResource("/javax/servlet/sip/resources/sip-app_1_0.dtd");
 		URL sipapp11xsd = SipAppContext.class.getResource("/javax/servlet/sip/resources/sip-app_1_1.xsd");
         
 		xmlParser.redirectEntity("-//Java Community Process//DTD SIP Application 1.0//EN", dtd10);
 		xmlParser.redirectEntity("sip-app_1_1.xsd", sipapp11xsd);
 		xmlParser.redirectEntity("http://www.jcp.org/xml/ns/sipservlet/sip-app_1_1.xsd", sipapp11xsd);
 		
 		return xmlParser;
 	}
 	
 	public void setWebAppContext(WebAppContext context) 
 	{
 		_context = context;
 	}
 
 	public WebAppContext getWebAppContext() 
 	{
 		return _context;
 	}
 	
     public SipAppContext getSipAppContext()
     {
     	return (SipAppContext) _context;
     }
 
 	public void configureClassLoader() throws Exception {}
 	
 	public void configureDefaults() throws Exception 
 	{
 		if (_context.isStarted())
 		{
 			if (Log.isDebugEnabled()){Log.debug("Cannot configure webapp after it is started");}
             return;
 		}
 		String defaultsSipDescriptor = getSipAppContext().getDefaultsSipDescriptor();
 		if (defaultsSipDescriptor != null && defaultsSipDescriptor.length() > 0)
 		{
 			Resource dftSipResource = Resource.newSystemResource(defaultsSipDescriptor);
 			if (dftSipResource == null)
 				dftSipResource = Resource.newResource(defaultsSipDescriptor);
 			configure(dftSipResource.getURL().toString());
 		}
 	}
 
 	public void configureWebApp() throws Exception 
 	{
         if (_context.isStarted())
         {
            	Log.debug("Cannot configure webapp after it is started");
             return;
         }
 		URL sipxml = findSipXml();
 		if (sipxml != null) 
 			configure(sipxml.toString());
 		
 		String overrideDescriptor = getSipAppContext().getOverrideSipDescriptor();
         if(overrideDescriptor!=null&&overrideDescriptor.length()>0)
         {
             Resource orideResource=Resource.newSystemResource(overrideDescriptor);
             if(orideResource==null)
                 orideResource=Resource.newResource(overrideDescriptor);
             _xmlParser.setValidating(false);
             configure(orideResource.getURL().toString());
         }
 	}
 	
 	protected URL findSipXml() throws IOException, MalformedURLException 
 	{
 		Resource webInf = getWebAppContext().getWebInf();
 		if (webInf != null && webInf.isDirectory()) 
 		{
 			Resource sip = webInf.addPath("sip.xml");
 			if (sip.exists()) 
 				return sip.getURL();
 			Log.debug("No WEB-INF/sip.xml in " + getWebAppContext().getWar());
 		}
 		return null;
 	}
 
 	public void configure(String sipXml) throws Exception 
 	{
 		XmlParser.Node config = _xmlParser.parse(sipXml);
 		initialize(config);
 	}
 	
	@SuppressWarnings("unchecked")
 	protected void initialize(XmlParser.Node config) throws ClassNotFoundException, UnavailableException 
 	{
 		SipServletHandler servletHandler = (SipServletHandler) getWebAppContext().getServletHandler();
 		
 		_servlets = LazyList.array2List(servletHandler.getSipServlets());
 		_listeners = LazyList.array2List(getWebAppContext().getEventListeners());
 		
 		String version = config.getAttribute("version", "unknown");
 		if ("1.0".equals(version))
 			_version = VERSION_10;
 		else if ("1.1".equals(version))
 			_version = VERSION_11;
 		else if ("unknown".equals(version))
 		{
 			_version = VERSION_11;
             String dtd=_xmlParser.getDTD();
             if (dtd!=null && dtd.indexOf("sip-app_1_0")>=0)
                 _version=VERSION_10;
 		}
 		else
 			throw new UnavailableException("Unsupported version " + version);
 		
 		getSipAppContext().setSpecVersion(_version);
 		
 		Iterator it = config.iterator();
 		XmlParser.Node node = null;
 		
 		while (it.hasNext()) 
 		{
 			try 
 			{
 				Object o = it.next();
 				if (!(o instanceof XmlParser.Node)) 
 					continue;
 	
 				node = (XmlParser.Node) o;
 				
 				initSipXmlElement(node);
 			} 
 			catch (ClassNotFoundException e) 
 			{
 				throw e;
 			} 
 			catch (UnavailableException e) 
 			{
 				throw e;
 			}
 			catch (Exception e) 
 			{
 				Log.warn("Configuration error at " + node, e);
 				throw new UnavailableException("Configuration problem: " + e.getMessage() + " at " + node);
 			}
 		}
 		
 		initListeners();
 		
 		servletHandler.setSipServlets((SipServletHolder[]) LazyList.toArray(_servlets, SipServletHolder.class));
 		servletHandler.setSipServletMappings((SipServletMapping[]) LazyList.toArray(_servletMappings, SipServletMapping.class));
 		
 		if (_mainServlet != null)
 			servletHandler.setMainServletName(_mainServlet);
 		
 		if (_appName == null)
 			_appName = getWebAppContext().getContextPath();
 		
 		((SipAppContext) getWebAppContext()).setName(_appName);
 		
 		getWebAppContext().setEventListeners((EventListener[]) LazyList.toArray(_listeners, EventListener.class));
 	}
 	
 	protected void initSipXmlElement(XmlParser.Node node) throws Exception 
 	{
 		String name = node.getTag();
 	
 		if ("app-name".equals(name))
 			initAppName(node);
 		else if ("display-name".equals(name)) 	
 			initDisplayName(node);
 		else if ("context-param".equals(name)) 
 			initContextParam(node);
 		else if ("servlet".equals(name))
 			initServlet(node);
 		else if ("servlet-mapping".equals(name))
 			initServletMapping(node);
 		else if ("listener".equals(name))
 			initListener(node);
         else if ("proxy-config".equals(name))
             initProxyConfig(node);
         else if("session-config".equals(name))
             initSessionConfig(node);
        else if ("servlet-selection".equals(name))
        	initMainServlet(node.get("main-servlet"));
         else if("main-servlet".equals(name))
        {
         	initMainServlet(node);
        	Log.warn("main-servlet node should be place into servlet-selection node");
        }
 	}
 	
     protected void initSessionConfig(XmlParser.Node node)
     {
         XmlParser.Node tNode=node.get("session-timeout");
         if(tNode!=null)
         {
             int timeout=Integer.parseInt(tNode.toString(false,true));
             getSipAppContext().setSessionTimeout(timeout);
         }
     }
 	
 	protected void initDisplayName(XmlParser.Node node) 
 	{
 		getWebAppContext().setDisplayName(node.toString(false, true));
 	}
 	
 	protected void initContextParam(XmlParser.Node node) 
 	{
 		String name = node.getString("param-name", false, true);
 		String value = node.getString("param-value", false, true);
 		getWebAppContext().getInitParams().put(name, value);
 	}
 	
 	protected void initServlet(XmlParser.Node node) throws Exception
 	{
 		String servletName = node.getString("servlet-name", false, true);
 		String servletClass = node.getString("servlet-class", false, true);
 		// FIXME allow deploy with prefix: javaee:servlet-name
 		SipServletHolder holder = new SipServletHolder();
 		holder.setName(servletName);
 		holder.setClassName(servletClass);
 		
 		Iterator params = node.iterator("init-param");
 		
 		while (params.hasNext()) 
 		{
 			XmlParser.Node param = (XmlParser.Node) params.next();
 			String pName = param.getString("param-name", false, true);
 			String pValue = param.getString("param-value", false, true);
 			holder.setInitParameter(pName, pValue);
 		}
 		
 		XmlParser.Node startup = node.get("load-on-startup");
 		if (startup != null) 
 		{
 			String s = startup.toString(false, true);
 			int order = 0; 
 			if (s != null && s.trim().length() > 0) 
 			{
 				try 
 				{
 					order = Integer.parseInt(s);
 				} 
 				catch (NumberFormatException e) 
 				{
 					Log.warn("Cannot parse load-on-startup " + s);
 				}
 			}
 			holder.setInitOrder(order);
 		}
 		_servlets = LazyList.add(_servlets, holder);
 	}
 	
 	protected void initServletMapping(XmlParser.Node node) 
 	{
 		String servletName = node.getString("servlet-name", false, true);
 		SipServletMapping mapping = new SipServletMapping();
 		
 		XmlParser.Node pattern = node.get("pattern");
 		XmlParser.Node start = null;
 		Iterator it = pattern.iterator();
 		
 		while (it.hasNext() && start == null) 
 		{
 			Object o = it.next();
 			if (!(o instanceof XmlParser.Node)) 
 				continue;
 
 			start = (XmlParser.Node) o;
 		}
 		MatchingRule rule = initRule(start);
 		mapping.setServletName(servletName);
 		mapping.setMatchingRule(rule);
 		
 		_servletMappings = LazyList.add(_servletMappings, mapping);
 	}
 	
 	private MatchingRule initRule(XmlParser.Node node) 
 	{
 		String name = node.getTag();
 		if ("and".equals(name)) 
 		{
 			AndRule and = new AndRule();
 			Iterator it = node.iterator();
 			while (it.hasNext()) 
 			{
 				Object o = it.next();
 				if (!(o instanceof XmlParser.Node)) 
 					continue;
 				
 				and.addCriterion(initRule((XmlParser.Node) o));
 			}
 			return and;
 		} 
 		else if ("equal".equals(name)) 
 		{
 			String var = node.getString("var", false, true);
 			String value = node.getString("value", false, true);
 			boolean ignoreCase = "true".equalsIgnoreCase(node.getAttribute("ignore-case"));
 			return new EqualsRule(var, value, ignoreCase);
 		} 
 		else if ("subdomain-of".equals(name)) 
 		{
 			String var = node.getString("var", false, true);
 			String value = node.getString("value", false, true);
 			return new SubdomainRule(var, value);
 		} 
 		else if ("or".equals(name)) 
 		{
 			OrRule or = new OrRule();
 			Iterator it = node.iterator();
 			while (it.hasNext()) 
 			{
 				Object o = it.next();
 				if (!(o instanceof XmlParser.Node)) 
 					continue;
 
 				or.addCriterion(initRule((XmlParser.Node) o));
 			}
 			return or;
 		} 
 		else if ("not".equals(name)) 
 		{
 			NotRule not = new NotRule();
 			Iterator it = node.iterator();
 			while (it.hasNext()) 
 			{
 				Object o = it.next();
 				if (!(o instanceof XmlParser.Node)) 
 					continue;
 				
 				not.setCriterion(initRule((XmlParser.Node) o));
 			}
 			return not;
 		} 
 		else if ("contains".equals(name)) 
 		{
 			String var = node.getString("var", false, true);
 			String value = node.getString("value", false, true);
 			boolean ignoreCase = "true".equalsIgnoreCase(node.getAttribute("ignore-case"));
 			return new ContainsRule(var, value, ignoreCase);
 		} 
 		else if ("exists".equals(name)) 
 		{
 			return new ExistsRule(node.getString("var", false, true));
 		} 
 		else 
 		{
 			throw new IllegalArgumentException("Unknown rule: " + name);
 		}
 	}
 	
     public void initProxyConfig(XmlParser.Node node)
     {
         String s = node.getString("proxy-timeout", false, true);
         
         if (s == null)
         	s = node.getString("sequential-search-timeout", false, true);
         
         if (s != null)
         {
             try 
             {
                 int timeout = Integer.parseInt(s);
                 ((SipAppContext) getWebAppContext()).setProxyTimeout(timeout);
             }
             catch (NumberFormatException e)
             {
                 Log.warn("Invalid sequential-search-timeout value: " + s);
             }
         }
     }
     
 	public void initListener(XmlParser.Node node) 
 	{
 		String className = node.getString("listener-class", false, true);
 		_listenerClasses = LazyList.add(_listenerClasses, className);
 	}
 	
 	protected SipServletHolder getServlet(String className)
 	{
 		for (int i = LazyList.size(_servlets); i-->0;)
 		{
 			SipServletHolder holder = (SipServletHolder) LazyList.get(_servlets, i);
 			if (className.equals(holder.getClassName()))
 				return holder;
 		}
 		return null;
 	}
 			
 	public void initListeners()
 	{
 		for (int i = 0; i < LazyList.size(_listenerClasses); i++)
 		{
 			String lc = (String) LazyList.get(_listenerClasses, i);
 			SipServletHolder holder = getServlet(lc);
 				
 			// Check listener has not been already added.
 			boolean found = false;
 			for (int j = LazyList.size(_listeners); j--> 0;)
 			{
 				Object listener = LazyList.get(_listeners, j);
 				if (listener.getClass().getName().equals(lc))
 				{
 					Log.debug("Found multiple listener declaration " +  lc);
 					if (holder != null)
 						holder.setServlet((Servlet) listener);
 					found = true;
 					break;
 				}
 			}
 			
 			if (found)
 				continue;
 			
 			try
 			{
 				Class listenerClass = getWebAppContext().loadClass(lc);
 				Object listener = newListenerInstance(listenerClass);
 				
 				if (holder != null)
 					holder.setServlet((Servlet) listener);
 					
 				if (!(listener instanceof EventListener))
 					Log.warn("Not an event listener: " + listener);
 				else
 					_listeners = LazyList.add(_listeners, listener);
 			}
 			catch (Exception e) 
 			{
 				Log.warn("Could not instantiate listener: " + lc, e);
 			}
 		}
 	}
 	
 	public void initMainServlet(XmlParser.Node node)
 	{
 		_mainServlet = node.toString(false, true);
 	}
 	
 	public void initAppName(XmlParser.Node node)
 	{
 		_appName = node.toString(false, true);
 	}
 	
 	protected Object newListenerInstance(Class clazz) 
 		throws InstantiationException, IllegalAccessException 
 	{	
 		return clazz.newInstance();
 	}
 	
 	public void deconfigureWebApp() throws Exception {}
 }
