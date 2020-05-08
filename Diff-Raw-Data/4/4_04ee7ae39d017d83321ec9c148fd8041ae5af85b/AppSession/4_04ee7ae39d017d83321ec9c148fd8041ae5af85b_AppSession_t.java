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
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpSession;
 
 import javax.servlet.sip.ServletTimer;
 import javax.servlet.sip.SipApplicationSession;
 import javax.servlet.sip.SipApplicationSessionAttributeListener;
 import javax.servlet.sip.SipApplicationSessionBindingEvent;
 import javax.servlet.sip.SipApplicationSessionBindingListener;
 import javax.servlet.sip.SipApplicationSessionEvent;
 import javax.servlet.sip.SipApplicationSessionListener;
 import javax.servlet.sip.SipErrorEvent;
 import javax.servlet.sip.SipErrorListener;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 import javax.servlet.sip.SipSessionEvent;
 import javax.servlet.sip.SipSessionListener;
 import javax.servlet.sip.TimerListener;
 import javax.servlet.sip.URI;
 
 import org.cipango.Call;
 import org.cipango.NameAddr;
 import org.cipango.Server;
 import org.cipango.Call.TimerTask;
 import org.cipango.handler.SipContextHandlerCollection;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.sipapp.SipXmlConfiguration;
 import org.cipango.util.ID;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 
 public class AppSession implements AppSessionIf, Serializable
 {
 	private static final long serialVersionUID = 1L;
 	
 	public static final String APP_ID = "org.cipango.aid";
 	public static final String APP_ID_PREFIX = ";" + APP_ID + "=";
 	
 	public static final int VALID = 0;
 	public static final int EXPIRED = 1;
 	public static final int INVALID = 2;
 	public static final int INVALIDATING = 3;
 	
 	private static String[] __states = {"valid", "expired", "invalid", "invalidating"};
 	
 	protected static Method __noAck;
     protected static Method __noPrack;
     protected static Method __appSessionCreated;
     protected static Method __appSessionReadyToInvalidate;
     protected static Method __appSessionExpired;
     protected static Method __appSessionDestroyed;
     protected static Method __timerExpired;
     protected static Method __sessionCreated;
     protected static Method __sessionReadyToInvalidate;
     protected static Method __sessionDestroyed;
     
     static 
     {
         try 
         {
             __noAck = SipErrorListener.class.getMethod("noAckReceived", SipErrorEvent.class);
             __noPrack = SipErrorListener.class.getMethod("noPrackReceived", SipErrorEvent.class);
             __appSessionCreated = SipApplicationSessionListener.class.getMethod("sessionCreated", SipApplicationSessionEvent.class);
             __appSessionReadyToInvalidate = 
             	SipApplicationSessionListener.class.getMethod("sessionReadyToInvalidate", SipApplicationSessionEvent.class);
             __appSessionExpired = SipApplicationSessionListener.class.getMethod("sessionExpired", SipApplicationSessionEvent.class);
             __appSessionDestroyed = SipApplicationSessionListener.class.getMethod("sessionDestroyed", SipApplicationSessionEvent.class);
             __timerExpired = TimerListener.class.getMethod("timeout", ServletTimer.class);
             __sessionCreated = SipSessionListener.class.getMethod("sessionCreated", SipSessionEvent.class);
             __sessionReadyToInvalidate = SipSessionListener.class.getMethod("sessionReadyToInvalidate", SipSessionEvent.class);
             __sessionDestroyed = SipSessionListener.class.getMethod("sessionDestroyed", SipSessionEvent.class);
         } 
         catch (NoSuchMethodException e)
         {
             throw new ExceptionInInitializerError(e);
         }
     }
     
 	private int _state = VALID;
     
     private transient SipAppContext _context;
     private String _contextName;
     
     private Object _sessions; // LazyList<Session>
     private Object _httpSessions; // LazyList<HttpSession>
     
     private Map<String, Object> _attributes;
     private Call _call;
     
     private String _appId; 
 
     private long _created = System.currentTimeMillis();
     private long _accessed = _created;
     private long _expirationTime = 0;
     
     private Object _timers; // LazyList<Timer>
 
     private TimerTask _expiryTimer;
     
     private boolean _invalidateWhenReady;
 	
     public AppSession(Call call)
     {
         _call = call;
         _appId = ID.newSessionID();
     }
     
     /**
      * @see SipApplicationSession#getCreationTime()
      */
 	public long getCreationTime()
 	{
 		checkValid();
 		return _created;
 	}
 
 	/**
 	 * @see SipApplicationSession#getLastAccessedTime()
 	 */
 	public long getLastAccessedTime()
 	{
 		return _accessed;
 	}
 
 	/**
 	 * @see SipApplicationSession#getId()
 	 */
 	public String getId()
 	{
 		return _call.getCallId() + ";" + _appId;
 	}
 
 	/**
 	 * @see SipApplicationSession#setExpires(int)
 	 */
 	public int setExpires(int deltaMinutes) 
 	{
 		if (!(_state == VALID || _state == EXPIRED))
 			throw new IllegalStateException();
 		
 		if (_expiryTimer != null)
 		{
 			_call.cancel(_expiryTimer);
 			_expiryTimer = null;
 		}
 		
 		if (deltaMinutes > 0)
 		{
 			long delayMs = deltaMinutes * 60000l;
 			_expiryTimer = _call.schedule(new Expired(), delayMs);
 			_expirationTime = System.currentTimeMillis() + delayMs;
 		}
 
 		return deltaMinutes;
 	}
 	
 	/**
 	 * @see SipApplicationSession#invalidate()
 	 */
 	public void invalidate()
 	{
 		checkValid();
 		try 
 		{
 			if (_expiryTimer != null)
 			{
 				_call.cancel(_expiryTimer);
 				_expiryTimer = null;
 			}
 			
 			synchronized (this)
 			{			
 				while (LazyList.size(_sessions) > 0)
 				{
 					Session session = (Session) LazyList.get(_sessions, 0);
 					_sessions = LazyList.remove(_sessions, 0);
 					session.invalidate();
 				}
 				while (LazyList.size(_httpSessions) > 0)
 				{
 					HttpSession session = (HttpSession) LazyList.get(_httpSessions, 0);
 					_httpSessions = LazyList.remove(_httpSessions, 0);
 					session.invalidate();
 				}
 				while (LazyList.size(_timers) > 0)
 				{
 					Timer timer = (Timer) LazyList.get(_timers, 0);
 					_timers = LazyList.remove(_timers, 0);
 					timer.cancel();
 				}
 			}
 			getCall().removeSession(this);
 			
 			if (getContext() != null)
 				getContext().updateNbSessions(false);
 			
 			SipApplicationSessionListener[] listeners = getContext().getSipApplicationSessionListeners();
 			if (listeners.length > 0)
 				fireEvent(listeners, __appSessionDestroyed, new SipApplicationSessionEvent(this));
 			
 			// Call remove attributes and call associated listeners
 			SipApplicationSessionAttributeListener[] attrListeners = getContext().getSipApplicationSessionAttributeListeners();
 	        while (_attributes!=null && _attributes.size()>0)
 	        {
 	            ArrayList<String> keys;
 	            synchronized (this)
 	            {
 	                keys=new ArrayList<String>(_attributes.keySet());
 	            }
 	
 	            Iterator<String> iter = keys.iterator();
 	            while (iter.hasNext())
 	            {
 	                String key = iter.next();
 	
 	                Object value;
 	                synchronized (this)
 	                {
 	                    value=_attributes.remove(key);
 	                }
 	                unbindValue(key,value);
 	
 	                if (attrListeners!=null)
 	                {
 	                	SipApplicationSessionBindingEvent event 
 							= new SipApplicationSessionBindingEvent(this, key);
 						for (int i = 0; i < attrListeners.length; i++)
 							attrListeners[i].attributeRemoved(event);
 		            }
 	            }
 	        }
 		}
 		finally
 		{
 			_state = INVALID;
 		}
 	}
 	
 	protected void expired()
 	{
 		if (_state == VALID)
 		{
 			_state = EXPIRED;
 			
 			SipApplicationSessionListener[] listeners = getContext().getSipApplicationSessionListeners();
 			if (listeners.length > 0)
 				fireEvent(listeners, __appSessionExpired, new SipApplicationSessionEvent(this));
 					
 			if (_state == EXPIRED)
 				invalidate();
 		}
 	}
 	
 	public synchronized Session getSession(String dialogId) 
 	{
 		for (int i = LazyList.size(_sessions); i-->0;)
 		{
 			Session session = (Session) LazyList.get(_sessions, i);
 			if (dialogId.equals(session.getUpstreamId()) || dialogId.equals(session.getDownstreamId())) // TODO mv2 session
 				return session;
 		}
 		return null;
 	}
 	
 
 	@SuppressWarnings("unchecked")
 	public synchronized Iterator<?> getSessions() 
 	{
 		checkValid();
 		List<?> sessions = LazyList.getList(_sessions);
 		sessions.addAll(LazyList.getList(_httpSessions));
 		return sessions.iterator();
 	}
 
 	public synchronized Iterator<?> getSessions(String protocol)
 	{
 		checkValid();
 		
 		if (protocol == null)
 			throw new NullPointerException("null protocol");
 		
 		if ("sip".equalsIgnoreCase(protocol))
 			return LazyList.iterator(_sessions);
 		else if ("http".equalsIgnoreCase(protocol))
 			return LazyList.iterator(_httpSessions);
 		else
 			throw new IllegalArgumentException("unknown protocol: " + protocol);
 	}
 
 	public synchronized ServletTimer getTimer(String id)
 	{
 		checkValid();
 		
 		for (int i = LazyList.size(_sessions); i-->0;)
 		{
 			Timer timer = (Timer) LazyList.get(_timers, i);
 			if (timer.getId().equals(id))
 				return timer;
 		}
 		return null;
 	}
 
 	public void encodeURI(URI uri) 
 	{
 		checkValid();
 		uri.setParameter(APP_ID, getId());
 	}
 
 	public synchronized Object getAttribute(String name) 
 	{
 		checkValid();
 		if (_attributes == null) 	
 			return null;
 		
 		return _attributes.get(name);
 	}
 
	@SuppressWarnings("unchecked")
 	public synchronized Iterator<String> getAttributeNames() 
 	{
 		checkValid();
 		if (_attributes == null) 
			return Collections.EMPTY_LIST.iterator();
 		
 		return _attributes.keySet().iterator();
 	}
 
 	public synchronized void setAttribute(String name, Object value) 
 	{
 		checkValid();
 		
 		if (value == null || name == null)
 			throw new NullPointerException("Name or attribute is null");
 
 		if (_attributes == null)
 			_attributes = new HashMap<String, Object>();
 	
 		Object oldValue = _attributes.put(name, value);
 
 		if (oldValue == null || !value.equals(oldValue))
 		{
 			unbindValue(name, oldValue);
 			bindValue(name, value);
 		
 			SipApplicationSessionAttributeListener[] listeners = getContext().getSipApplicationSessionAttributeListeners();
 			if (listeners.length > 0)
 			{
 				SipApplicationSessionBindingEvent event = 
 					new SipApplicationSessionBindingEvent(this, name);
 				for (int i = 0; i < listeners.length; i++)
 				{
 					if (oldValue == null)
 						listeners[i].attributeAdded(event);
 					else
 						listeners[i].attributeReplaced(event);
 				}
 			}
 		}
 	}
 
 	public synchronized void removeAttribute(String name)
 	{
 		checkValid();
 		
 		if (_attributes == null)
 			return;
 		
 		Object oldValue = _attributes.remove(name);
 		
 		if (oldValue != null)
 		{
 			unbindValue(name, oldValue);
 			
 			SipApplicationSessionAttributeListener[] listeners = getContext().getSipApplicationSessionAttributeListeners();
 			if (listeners != null)
 			{
 				SipApplicationSessionBindingEvent event 
 					= new SipApplicationSessionBindingEvent(this, name);
 				for (int i = 0; i < listeners.length; i++)
 					listeners[i].attributeRemoved(event);
 			}
 		}
 	}
 
 	public Collection<ServletTimer> getTimers()
 	{
 		checkValid();
 		if (_timers == null)
 			return Collections.emptyList();
 		
 		return new ArrayList<ServletTimer>(LazyList.getList(_timers));
 	}
 
 	public void unbindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipApplicationSessionBindingListener)
 			((SipApplicationSessionBindingListener) value).valueUnbound(new SipApplicationSessionBindingEvent(this, name));
 	}
 	
 	public void bindValue(String name, Object value)
 	{
 		if (value != null && value instanceof SipApplicationSessionBindingListener)
 			((SipApplicationSessionBindingListener) value).valueBound(new SipApplicationSessionBindingEvent(this, name));
 	}
 	
     public void setCall(Call call)
     {
         _call = call;
     }
     
     public void access(long accessed)
     {
         _accessed = accessed;
     }
     
 	private void checkValid() 
 	{
 		if (!isValid())
 			throw new IllegalStateException("SipApplicationSession has been invalidated");
 	}
 	
 	public boolean isValid()
 	{
 		return (!(_state == INVALID));
 	}
 
 	public URL encodeURL(URL url)
 	{
 		checkValid();
 		
 		try {
 			String sUrl = url.toExternalForm();
 			String id= getId();
 			int prefix=sUrl.indexOf(APP_ID_PREFIX);
 	        if (prefix!=-1)
 	        {
 	            int suffix=sUrl.indexOf("?",prefix);
 	            if (suffix<0)
 	                suffix=sUrl.indexOf("#",prefix);
 	
 	            if (suffix<=prefix)
 	                return new URL(sUrl.substring(0, prefix + APP_ID_PREFIX.length()) + id);
 	            return new URL(sUrl.substring(0, prefix + APP_ID_PREFIX.length()) + id + sUrl.substring(suffix));
 	        }
 	
 	        // edit the session
 	        int suffix=sUrl.indexOf('?');
 	        if (suffix<0)
 	            suffix=sUrl.indexOf('#');
 	        if (suffix<0)
 	            return new URL(sUrl+APP_ID_PREFIX+id);
 	        return new URL(sUrl.substring(0,suffix) + APP_ID_PREFIX + id + sUrl.substring(suffix));
 		} catch (Exception e) {
 			throw new IllegalStateException(e);
 		}
 	}
 
 	public String getApplicationName()
 	{
 		return _contextName;
 	}
 
 	public long getExpirationTime()
 	{
 		checkValid();
 		
 		return _expirationTime;
 	}
 
 	/**
 	 * @see SipApplicationSession#getSession(String, javax.servlet.sip.SipApplicationSession.Protocol)
 	 */
 	public synchronized Object getSession(String id, Protocol protocol)
 	{
 		checkValid();
 		
 		if (id == null || protocol == null)
 			throw new NullPointerException((id == null) ? "null id" : "null protocol");
 		
 		if (protocol == Protocol.SIP)
 		{
 			for (int i = LazyList.size(_sessions); i-->0;)
 			{
 				Session session = (Session) LazyList.get(_sessions, i);
 				if (session.getId().equals(id))
 					return session;
 			}
 		}
 		else if (protocol == Protocol.HTTP)
 		{
 			for (int i = LazyList.size(_httpSessions); i-->0;)
 			{
 				HttpSession session = (HttpSession) LazyList.get(_httpSessions, i);
 				if (session.getId().equals(id))
 					return session;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see SipApplicationSession#getSipSession(String)
 	 */
 	public SipSession getSipSession(String id)
 	{
 		return ((SipSession) getSession(id, Protocol.SIP));
 	}
 
 	
 	public boolean isReadyToInvalidate()
 	{
 		checkValid();
 		
 		for (int i = LazyList.size(_sessions); i-->0;)
 		{
 			Session session = (Session) LazyList.get(_sessions, i);
 			if (!session.isReadyToInvalidate())
 				return false;
 		}
 		return LazyList.size(_timers) == 0;
 	}
 	
 	protected void checkReadyToInvalidate()
 	{
 		if (_state != INVALID && _state != INVALIDATING  && getInvalidateWhenReady()
 				&& LazyList.size(_sessions) == 0 && LazyList.size(_httpSessions) == 0
 				&& LazyList.size(_timers) == 0)
 		{
 			SipApplicationSessionListener[] listeners = getContext().getSipApplicationSessionListeners();
 			if (listeners.length > 0)
 				fireEvent(listeners, __appSessionReadyToInvalidate, new SipApplicationSessionEvent(this));
 			
 			if (_invalidateWhenReady)
 				invalidate();
 		}
 	}
 
 	public void setInvalidateWhenReady(boolean invalidateWhenReady)
 	{
 		checkValid();
 		_invalidateWhenReady = invalidateWhenReady;
 	}
 	
 	public boolean getInvalidateWhenReady()
 	{
 		checkValid();
 		return _invalidateWhenReady;
 	}
 	
 	public SipAppContext getContext()
 	{
 		if (_context == null)
 		{
 			Server server = _call.getServer();
 			_context = ((SipContextHandlerCollection) server.getHandler()).getContext(_contextName);
 		}
 		return _context;
 	}
 		
 	public void setContext(SipAppContext context) 
 	{
 		if (_context != null)
 			throw new IllegalStateException("context != null");
 
 		_context = context;
 		_contextName = _context.getName();
 
 		_context.updateNbSessions(true);
 		_invalidateWhenReady = _context.getSpecVersion() != SipXmlConfiguration.VERSION_10;
 		
 		SipApplicationSessionListener[] listeners = _context.getSipApplicationSessionListeners();
 		if (listeners.length > 0)
 			fireEvent(listeners, __appSessionCreated, new SipApplicationSessionEvent(this));
 
 		setExpires(_context.getSessionTimeout());
 	}
 	
     public Session newSession()
     {
         Session session = new Session(this);
         addSession(session);
         return session;
     }
     
     public Session newSession(Session session)
     {
     	if (session.appSession() != this)
     		throw new IllegalArgumentException("!same appsession");
     	Session clone = null;
     	
     	try
     	{
     		clone = session.clone();
     	}
     	catch (CloneNotSupportedException _) {}
     	addSession(clone);
     	return clone;
     }
     
     public Session newUacSession(String callId, NameAddr from, NameAddr to)
     {
         Session session = new Session(this, callId, from, to);
         addSession(session);
         return session;
     }
     
 	private synchronized void addSession(Session session)
 	{
 		_sessions = LazyList.add(_sessions, session);
 		
 		SipSessionListener[] listeners = getContext().getSipSessionListeners();
 		if (listeners.length > 0)
 			fireEvent(listeners, __sessionCreated, new SipSessionEvent(session));
 	}
 	
 	public synchronized void removeSession(Session session)
 	{
 		_sessions = LazyList.remove(_sessions, session);
 		
 		SipSessionListener[] listeners = getContext().getSipSessionListeners();
 		if (listeners.length > 0)
 			fireEvent(listeners, __sessionDestroyed, new SipSessionEvent(session));
 		checkReadyToInvalidate();
 	}
 	
 	public synchronized void addHttpSession(HttpSession session) 
 	{
 		_httpSessions = LazyList.add(_httpSessions, session);
 	}
 	
 	public synchronized void removeHttpSession(HttpSession session)
 	{
 		_httpSessions = LazyList.remove(_httpSessions, session);
 		checkReadyToInvalidate();
 	}
 	
 	public Call getCall() 
 	{
 		return _call;
 	}
 	
 	public String getAid()
 	{
 		return _appId;
 	}
     
 	public void fireEvent(EventListener[] listeners, Method method, Object... args)
     {
 		ClassLoader oldClassLoader = null;
 		Thread currentThread = null;
 		
 		ClassLoader contextCL = getContext().getClassLoader();
 		
 		if (contextCL != null)
 		{
 			currentThread = Thread.currentThread();
 			oldClassLoader = currentThread.getContextClassLoader();
 			currentThread.setContextClassLoader(contextCL);
 		}
 
 		for (int i = 0; i < listeners.length; i++)
 		{
 			try
 			{
 				method.invoke(listeners[i], args);
 			}
 			catch (Throwable t)
 			{
 			}
 		}
 		if (contextCL != null)
 		{
 			currentThread.setContextClassLoader(oldClassLoader);
 		}
     }
 	
 	public void noAck(SipServletRequest request, SipServletResponse response)
 	{
 		SipErrorListener[] listeners = getContext().getSipErrorListeners();
 		if (listeners.length > 0)
 			fireEvent(listeners, __noAck, new SipErrorEvent(request, response));
 	}
 	
 	public void noPrack(SipServletRequest request, SipServletResponse response)
 	{
 		SipErrorListener[] listeners = getContext().getSipErrorListeners();
 		if (listeners.length > 0)
 			fireEvent(listeners, __noPrack, new SipErrorEvent(request, response));
 	}
 	
     public ServletTimer newTimer(long delay, boolean persistent, Serializable info)
     {
         checkValid();
         return new Timer(delay, persistent, info);
     }
     
     public ServletTimer newTimer(long delay, long period, boolean fixedDelay, boolean isPersistent, Serializable info)
     {
         checkValid();
         return new Timer(delay, period, fixedDelay, isPersistent, info);
     }
     
     private synchronized void addTimer(Timer timer)
     {
     	_timers = LazyList.add(_timers, timer);
     }
     
     private synchronized void removeTimer(Timer timer)
     {
     	_timers = LazyList.remove(_timers, timer);
     	checkReadyToInvalidate();
     }
     
 	public AppSession getAppSession()
 	{
 		return this;
 	}
     
     public String toString()
     {
     	return _appId + "/" + _contextName;
     }
     
     public boolean equals(Object o)
     {
     	if (o == null || !(o instanceof AppSessionIf))
 			return false;
     	AppSession session = ((AppSessionIf) o).getAppSession();
     	return this == session;
     }
     
     class Expired implements Runnable, Serializable
     {
 		private static final long serialVersionUID = 1L;
 
 		public void run()
     	{
     		expired();
     	}
     	
     	public String toString()
     	{
     		return "session_timer";
     	}
     }
     
     public class Timer implements ServletTimer, Serializable
     {
 		private static final long serialVersionUID = 1L;
 
 		private Serializable _info;
         private long _period = -1;
         private TimerTask _timerTask;
         private long _executionTime;
         
         private String _id = ID.newID(4);
         
         public Timer(long delay, boolean persistent, Serializable info)
         {
             addTimer(this);
             _info = info;
             _executionTime = System.currentTimeMillis() + delay;
             _timerTask = _call.schedule(new TimeoutTask(), delay);
         }
         
         public Timer(long delay, long period, boolean fixedDelay, boolean isPersistent, Serializable info)
         {
             addTimer(this);
             _info = info;
             _period = period;
             _executionTime = System.currentTimeMillis() + delay;
             _timerTask = _call.schedule(new TimeoutTask(), delay);
         }
         
         public SipApplicationSession getApplicationSession()
         {
             return AppSession.this;
         }
 
         public Serializable getInfo()
         {
             return _info;
         }
 
         public long scheduledExecutionTime()
         {
             return _executionTime;
         }
 
         public String getId()
 		{
 			return _id;
 		}
 
 		public long getTimeRemaining()
 		{
 			return _executionTime - System.currentTimeMillis();
 		}
 		
         public void cancel()
         {
         	if (_timerTask != null)
         		getCall().cancel(_timerTask);
         	_timerTask = null;
         	removeTimer(this);
            _period = -1;
         }
         
         private void timeout()
         {
         	TimerListener[] listeners = getContext().getTimerListeners();
         	if (listeners.length > 0)
         		fireEvent(listeners, __timerExpired, this);
 
         	if (_period != -1)
             {
             	_executionTime = System.currentTimeMillis() + _period;
                 _timerTask = getCall().schedule(new TimeoutTask(), _period);
             }
             else
             {
                removeTimer(this);
             }
         }
         
         class TimeoutTask implements Runnable, Serializable
         {
 			private static final long serialVersionUID = 1L;
 
 			public void run()
             {
                 try 
                 {
                     timeout();
                 }
                 catch (Throwable t)
                 {
                     Log.debug("Exception in servlet timer {}", t);
                 }
             }
         }
     }
 }
