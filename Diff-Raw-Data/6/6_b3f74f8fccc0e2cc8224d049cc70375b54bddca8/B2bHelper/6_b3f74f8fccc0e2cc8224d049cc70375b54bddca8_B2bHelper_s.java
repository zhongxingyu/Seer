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
 
 package org.cipango.server;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.servlet.sip.Address;
 import javax.servlet.sip.B2buaHelper;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletRequest;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipSession;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.TooManyHopsException;
 import javax.servlet.sip.UAMode;
 import javax.servlet.sip.ar.SipApplicationRoutingDirective;
 
 import org.cipango.server.session.AppSession;
 import org.cipango.server.session.Session;
 import org.cipango.server.session.SessionIf;
 import org.cipango.server.session.scope.ScopedSession;
 import org.cipango.server.transaction.ClientTransaction;
 import org.cipango.server.transaction.ServerTransaction;
 import org.cipango.sip.NameAddr;
 import org.cipango.sip.SipHeaders;
 import org.cipango.sip.SipHeaders.HeaderInfo;
 import org.cipango.sip.SipParams;
 import org.cipango.util.ContactAddress;
 import org.eclipse.jetty.io.BufferCache.CachedBuffer;
 
 public class B2bHelper implements B2buaHelper
 {
 	private static final B2bHelper __instance = new B2bHelper();
 	
 	public static B2bHelper getInstance()
 	{
 		return __instance;
 	}
 
 	public SipServletRequest createCancel(SipSession sipSession) 
 	{
 		Session session = ((SessionIf) sipSession).getSession();
 		for (ClientTransaction tx : session.getCallSession().getClientTransactions(session))
 		{
 			if (tx.getRequest().isInitial())
 				return tx.getRequest().createCancel();
 		}
 		return null;
 	}
 
 	/**
 	 * @see B2buaHelper#createRequest(SipServletRequest)
 	 */
 	public SipServletRequest createRequest(SipServletRequest origRequest) 
 	{ 
 		SipRequest srcRequest = (SipRequest) origRequest;
 
 		NameAddr local = (NameAddr) srcRequest.from().clone();
     	local.setParameter(SipParams.TAG, ID.newTag());
     	
     	NameAddr remote = (NameAddr) srcRequest.to().clone();
     	remote.removeParameter(SipParams.TAG);
     	
     	String callId = ID.newCallId(srcRequest.getCallId());
 		
 		AppSession appSession = srcRequest.appSession(); 
         
         Session session = appSession.createUacSession(callId, local, remote);
         session.setHandler(appSession.getContext().getSipServletHandler().getDefaultServlet());
 
         SipRequest request = session.getUA().createRequest(srcRequest);
         request.setRoutingDirective(SipApplicationRoutingDirective.CONTINUE, srcRequest);
         request.setInitial(true);
         
         return request;
 	}
 
 	public SipServletRequest createRequest(SipServletRequest origRequest, boolean linked, 
 			Map<String, List<String>> headerMap) throws IllegalArgumentException, TooManyHopsException
 	{
 		if (origRequest == null)
 			throw new NullPointerException("Original request is null");
 		if (!origRequest.isInitial())
 			throw new IllegalArgumentException("Original request is not initial");
 		
 		int mf = origRequest.getMaxForwards();
 		if (mf == 0)
 			throw new TooManyHopsException("Max-Forwards of original request is equal to 0");
 		
 		SipRequest request = (SipRequest) createRequest(origRequest);
 		addHeaders(request, headerMap);
 		
 		if (linked)
 			linkRequest((SipRequest) origRequest, request);
 		
 		return request;
 	}
 
 	public SipServletRequest createRequest(SipSession sipSession, SipServletRequest origRequest, 
 			Map<String, List<String>> headerMap) throws IllegalArgumentException 
 	{
 		if (!sipSession.getApplicationSession().equals(origRequest.getApplicationSession()))
 			throw new IllegalArgumentException("SipSession " + sipSession 
 					+ " does not belong to same application session as original request");
 		
 		SipSession linkedSession = getLinkedSession(origRequest.getSession());
 		if (linkedSession != null && !linkedSession.equals(sipSession))
 			throw new IllegalArgumentException("Original request is already linked to another sipSession");
 	
 		if (getLinkedSipServletRequest(origRequest) != null)
 			throw new IllegalArgumentException("Original request is already linked to another request");
 		
 		Session session = ((SessionIf) sipSession).getSession();
 		SipRequest srcRequest = (SipRequest) origRequest;
 		
 		SipRequest request = (SipRequest) session.getUA().createRequest(srcRequest);
 		request.setInitial(false);
 		addHeaders(request, headerMap);
 		
 		linkRequest(srcRequest, request);
 		
 		return request;
 		/*
 		SipRequest request = (SipRequest) session.createRequest(origRequest.getMethod());
 
 		SipFields fields = request.getFields();
 		
 		// Copy origRequest Headers
 		Iterator<String> it = origRequest.getHeaderNames();
 		while (it.hasNext())
 		{
 			String name = (String) it.next();
 			if (!_request.isSystemHeader(name))
 			{
 				ListIterator<String> values = origRequest.getHeaders(name);
 				// ensure headers are copied in right order
 				while (values.hasNext())
 					values.next();
 				while (values.hasPrevious())
 				{
 					String value = (String) values.previous();
 					request.addHeader(name, value);
 				}
 			}
 		}
 		request.setMaxForwards(origRequest.getMaxForwards() == -1 ? SipProxy.__maxForwards : origRequest.getMaxForwards() - 1);
 		// Copy headerMaps headers
 		List<String> contacts = processHeaderMap(headerMap, fields, false);
 		// TODO Merge contact with the one set by headerMap
 		
 		linkRequest(request);
 		
 		return request;
 		 */
 	}
 
 	/**
 	 * @see B2buaHelper#createResponseToOriginalRequest(SipSession, int, String)
 	 */
 	public SipServletResponse createResponseToOriginalRequest(SipSession sipSession, int status, String reason) 
 	{
 		if (!sipSession.isValid())
 			throw new IllegalArgumentException("SipSession " + sipSession + " is not valid");
 		
 		Session session = ((SessionIf) sipSession).getSession();
 		for (ServerTransaction tx : session.getCallSession().getServerTransactions(session))
 		{
 			SipRequest request = tx.getRequest();
 			if (request.isInitial())
 			{
 				if (tx.isCompleted())
 				{
 					if (status >= 300)
 						throw new IllegalStateException("Cannot send response with status" + status 
 								+ " since final response has already been sent");
 					SipResponse response = new SipResponse(request, status, reason);
 					Session derived = session.appSession().createDerivedSession(session);
 					response.setSession(derived);
 					response.setTransaction(null);
 					return response;
 				}
 				else
 				{
 					return request.createResponse(status, reason);
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @see B2buaHelper#getLinkedSession(SipSession)
 	 */
 	public SipSession getLinkedSession(SipSession session) 
 	{
 		if (!session.isValid())
 			throw new IllegalArgumentException("SipSession " + session + " is not valid");
 		Session linked = ((SessionIf) session).getSession().getLinkedSession();
 		return linked == null ?  null : new ScopedSession(linked);
 	}
 
 	/**
 	 * @see B2buaHelper#getLinkedSipServletRequest(SipServletRequest)
 	 */
 	public SipServletRequest getLinkedSipServletRequest(SipServletRequest request) 
 	{
 		return ((SipRequest) request).getLinkedRequest();
 	}
 
 	/**
 	 * @see B2buaHelper#getPendingMessages(SipSession, UAMode)
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public List<SipServletMessage> getPendingMessages(SipSession sipSession, UAMode mode) 
 	{
 		if (!sipSession.isValid())
 			throw new IllegalArgumentException("SipSession " + sipSession + " is not valid");
 		
 		Session session = ((SessionIf) sipSession).getSession();
 		
		if (!session.isUA())
			throw new IllegalArgumentException("SipSession " + session + " is not UA");
 		
 		List<SipServletMessage> messages = new ArrayList<SipServletMessage>();
 		if (mode == UAMode.UAS)
 		{
 			for (ServerTransaction tx : session.getCallSession().getServerTransactions(session))
 			{
 				if (!tx.getRequest().isCommitted())
 					messages.add(tx.getRequest());
 			}
 		}
 		else 
 		{
 			for (ClientTransaction tx : session.getCallSession().getClientTransactions(session))
 			{
 				if (!tx.isCompleted())
 					messages.add(tx.getRequest());
 			}
 		}
 		
 		messages.addAll(session.getUA().getUncommitted2xx(mode));
 		
 		Collections.sort(messages, new Comparator() {
 			public int compare(Object message1, Object message2)
 			{
 				long cseq1 = ((SipMessage) message1).getCSeq().getNumber();
 				long cseq2 = ((SipMessage) message2).getCSeq().getNumber();
 				
 				return (int) (cseq1 - cseq2);
 			}
 		});
 		return messages;
 	}
 
 	/**
 	 * @see B2buaHelper#linkSipSessions(SipSession, SipSession)
 	 */
 	public void linkSipSessions(SipSession sipSession1, SipSession sipSession2) 
 	{
 		Session session1 = ((SessionIf) sipSession1).getSession();
 		Session session2 = ((SessionIf) sipSession2).getSession();
 		
 		checkNotTerminated(session1);
 		checkNotTerminated(session2);	
 		
 		Session linked1 = session1.getLinkedSession();
 		
 		if (linked1 != null && !linked1.equals(session2))
 			throw new IllegalArgumentException("SipSession " + sipSession1 + " is already linked to " + linked1);
 		
 		Session linked2 = session2.getLinkedSession();
 		
 		if (linked2 != null && !linked2.equals(session1))
 			throw new IllegalArgumentException("SipSession " + sipSession2 + " is already linked to " + linked2);
 		
 		session1.setLinkedSession(session2);
 		session2.setLinkedSession(session1);
 	}
 
 	/**
 	 * @see B2buaHelper#unlinkSipSessions(SipSession)
 	 */
 	public void unlinkSipSessions(SipSession sipSession) 
 	{
 		Session session = ((SessionIf) sipSession).getSession();
 		checkNotTerminated(session);
 		
 		Session linked = session.getLinkedSession();
 		if (linked == null)
 			throw new IllegalArgumentException("SipSession " + session + " has no linked SipSession");
 		linked.setLinkedSession(null);
 		session.setLinkedSession(null);
 	}
 	
 	private void checkNotTerminated(Session session)
 	{
 		if (session.isTerminated())
 			throw new IllegalArgumentException("SipSession " + session + " is terminated");
 	}
 	
 	private void linkRequest(SipRequest request1, SipRequest request2)
 	{
 		request1.setLinkedRequest(request2);
 		request2.setLinkedRequest(request1);
 		
 		linkSipSessions(request1.session(), request2.session());
 	}
 	
 	protected void addHeaders(SipRequest request, Map<String, List<String>> headerMap)
 	{
 		if (headerMap != null)
 		{
 			for (Entry<String, List<String>> entry : headerMap.entrySet())
 			{
 				String s = entry.getKey();
 				
 				CachedBuffer name = SipHeaders.getCachedName(s);
 				HeaderInfo hi = SipHeaders.getType(name);
 				
 				if (hi.isSystem())
 				{
 					int ordinal = hi.getOrdinal();
 					if (ordinal == SipHeaders.CONTACT_ORDINAL)
 					{
 						NameAddr contact = (NameAddr) request.getFields().getAddress(SipHeaders.CONTACT_BUFFER);
 						if (contact != null)
 						{
 							List<String> contacts = entry.getValue();
 							if (contacts.size() > 0)
 								mergeContact(contacts.get(0), contact);
 						}
 					}
 					else if (ordinal == SipHeaders.FROM_ORDINAL || ordinal == SipHeaders.TO_ORDINAL)
 					{
 						// TODO merge RFC496
 					}
 					else if (ordinal == SipHeaders.ROUTE_ORDINAL && request.isInitial())
 					{
 						request.getFields().remove(SipHeaders.ROUTE_BUFFER);
 						for (String route : entry.getValue())
 						{
 							request.getFields().addString(SipHeaders.ROUTE_BUFFER, route);
 						}
 					}
 					else
 					{
 						throw new IllegalArgumentException("Header " + s + " is system.");
 					}
 				}
 				else
 				{
 					request.getFields().remove(name);
 					
 					for (String value : entry.getValue())
 					{
 						request.getFields().addString(name, value);
 					}
 				}
 			}
 		}
 	}
 	
 	public static void mergeContact(String src, Address dest)
 	{
 		try
 		{
 			NameAddr source = new NameAddr(src);
 			
 			SipURI srcUri = (SipURI) source.getURI();
 			SipURI destUri = (SipURI) dest.getURI();
 			
 			String user = srcUri.getUser();
 			if (user != null)
 				destUri.setUser(user);
 			
 			Iterator<String> it = srcUri.getHeaderNames();
 			while (it.hasNext())
 			{
 				String name = it.next();
 				destUri.setHeader(name, srcUri.getHeader(name));
 			}
 			
 			it = srcUri.getParameterNames();
 			while (it.hasNext())
 			{
 				String name = it.next();
 				if (!ContactAddress.isReservedUriParam(name))
 					destUri.setParameter(name, srcUri.getParameter(name));
 			}
 			String displayName = source.getDisplayName();
 			if (displayName != null)
 				dest.setDisplayName(displayName);
 			
 			it = source.getParameterNames();
 			while (it.hasNext())
 			{
 				String name = it.next();
 				dest.setParameter(name, source.getParameter(name));
 			}
 		}
 		catch (Exception e)
 		{
 			throw new IllegalArgumentException("Invalid Contact: " + src);
 		}
 	}
 }
