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
 
 package org.cipango.http.servlet;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.sip.ConvergedHttpSession;
 import javax.servlet.sip.SipApplicationSession;
 
 import org.cipango.servlet.AppSessionIf;
 import org.cipango.sipapp.SipAppContext;
 import org.cipango.util.ID;
 import org.mortbay.jetty.HttpSchemes;
 import org.mortbay.jetty.Request;
 import org.mortbay.jetty.servlet.AbstractSessionManager;
 import org.mortbay.jetty.servlet.HashSessionManager;
 import org.mortbay.log.Log;
 import org.mortbay.util.URIUtil;
 
 public class ConvergedSessionManager extends HashSessionManager
 {
 	public class Session extends HashSessionManager.Session implements ConvergedHttpSession
 	{
 		private AppSessionIf _appSession;
 		private String _serverName;
 		private String _scheme;
 		private int _port;
 		private int _confidentialPort;
 		
 		protected Session(HttpServletRequest httpServletRequest)
         {
            super(httpServletRequest);
            Request request = (Request) httpServletRequest;
            _serverName = request.getServerName();
            _scheme = request.getScheme();
            _confidentialPort = ((Request) request).getConnection().getConnector().getConfidentialPort();
            _port = request.getServerPort();
            updateSession(request);
            
         }
 		
 		public void updateSession(HttpServletRequest request)
 		{
 
 			String appId = null;
 			String uri = request.getRequestURI();
             
             int semi = uri.lastIndexOf(';');
             if (semi>=0)
             {   
                 String path_params=uri.substring(semi+1);
                 
                 if (path_params!=null && path_params.startsWith(ID.APP_SESSION_ID_PARAMETER))
                 {
                 	appId = path_params.substring(ID.APP_SESSION_ID_PARAMETER.length() + 1);
                     if(Log.isDebugEnabled())
                     	Log.debug("Got App ID " + appId + " from URL");
                 }
             }
 			
			if (appId != null && !appId.trim().equals("")) 
			{
				appId = appId.replace("%3B", ";");
				AppSessionIf appSession = (AppSessionIf) getSipAppContext().getSipSessionsUtil().getApplicationSessionById(appId);
 				if (appSession != null && appSession.isValid())
 				{
					if (isValid() && _appSession == null)
						appSession.getAppSession().addSession(this);
					_appSession = appSession;
 				}
 			}
 		}
 		
 		private SipAppContext getSipAppContext()
 		{
 			return (SipAppContext) _context.getContextHandler();
 		}
 
 		public String encodeURL(String url)
 		{
 			String sessionURLPrefix = getSessionURLPrefix();
 			String id= getNodeId();
 			int prefix=url.indexOf(sessionURLPrefix);
 	        if (prefix!=-1)
 	        {
 	            int suffix=url.indexOf("?",prefix);
 	            if (suffix<0)
 	                suffix=url.indexOf("#",prefix);
 
 	            if (suffix<=prefix)
 	                return url.substring(0, prefix + sessionURLPrefix.length()) + id;
 	            return url.substring(0, prefix + sessionURLPrefix.length()) + id + url.substring(suffix);
 	        }
 
 	        // edit the session
 	        int suffix=url.indexOf('?');
 	        if (suffix<0)
 	            suffix=url.indexOf('#');
 	        if (suffix<0)
 	            return url+sessionURLPrefix+id;
 	        return url.substring(0,suffix)+
 	            sessionURLPrefix+id+url.substring(suffix);
 		}
 
 		public String encodeURL(String relativePath, String scheme)
 		{
 			StringBuffer sb = new StringBuffer();
 			sb.append(scheme).append("://");
 			sb.append(_serverName);
 			if (_scheme.equalsIgnoreCase(scheme))
 			{
 				if (_port>0 && 
 		                ((scheme.equalsIgnoreCase(URIUtil.HTTP) && _port != 80) || 
 		                 (scheme.equalsIgnoreCase(URIUtil.HTTPS) && _port != 443)))
 	                sb.append(':').append(_port);
 			} 
 			else if (HttpSchemes.HTTPS.equals(scheme) && _confidentialPort != 0)
 			{
 				if (_confidentialPort != 443)
 	                sb.append(':').append(_port);
 			}
 			else
 			{
 				throw new IllegalArgumentException("Scheme " + scheme + " is not the scheme used for this session "
 						+ " and unable to detect the port for this scheme");
 			}
 			sb.append(getSipAppContext().getContextPath());
 			sb.append(relativePath);
 			return encodeURL(sb.toString());
 		}
 
 		public SipApplicationSession getApplicationSession()
 		{
 			if (_appSession == null)
 			{
 				_appSession = (AppSessionIf) getSipAppContext().getSipFactory().createApplicationSession();
				if (isValid())
					_appSession.getAppSession().addSession(this);
 			}
 			return _appSession;
 		}
 		
 		@Override
 		protected void access(long time) 
 		{
 			super.access(time);
 			if (_appSession != null)
 				_appSession.getAppSession().access(time);
 		}
 		
 		@Override
 		 protected void doInvalidate() throws IllegalStateException
 		{
 			 super.doInvalidate();
 			 if (_appSession != null)
 				 _appSession.getAppSession().removeSession(this);
 		}
 	}
 	
 	@Override
 	protected AbstractSessionManager.Session newSession(HttpServletRequest request)
 	{
 		return new Session(request);
 	}
 
 }
