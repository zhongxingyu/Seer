 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tresys.jalop.jnl.impl;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.crypto.dsig.DigestMethod;
 
 import com.tresys.jalop.jnl.ConnectionHandler;
 import com.tresys.jalop.jnl.Context;
 import com.tresys.jalop.jnl.Publisher;
 import com.tresys.jalop.jnl.RecordType;
 import com.tresys.jalop.jnl.Session;
 import com.tresys.jalop.jnl.Subscriber;
 import com.tresys.jalop.jnl.exceptions.JNLException;
import com.tresys.jalop.jnl.impl.messages.Utils;
 import com.tresys.jalop.jnl.impl.subscriber.SubscriberSessionImpl;
 
 /**
  * The {@link ContextImpl} is the implementation of the {@link Context} class. It
  * is responsible for initiating connections to, or listening for connections
  * from, remote JALoP Network Stores.
  */
 public final class ContextImpl implements Context {
 
 	private final Publisher publisher;
 	private final Subscriber subscriber;
 	private final ConnectionHandler connectionHandler;
 	private final List<Session> jalSessions;
 	private final List<String> allowedMessageDigests;
 	private final List<String> allowedXmlEncodings;
 	private final int defaultDigestTimeout;
 	private final int defaultPendingDigestMax;
 	private final boolean tlsRequired;
 	private ConnectionState connectionState;
 	private final Map<org.beepcore.beep.core.Session, Map<RecordType, SubscriberSessionImpl>> subscriberMap;
 
 	/**
 	 * Create a new {@link ContextImpl}. The returned {@link Context} is in a
 	 * {@link ConnectionState#DISCONNECTED} state. If none are provided, the context
 	 * will be configured to use the default required options for message digests
 	 * (i.e. sha256) and for XML Encoding (i.e. none).
 	 *
 	 * @param publisher
 	 *            The Publisher to register. Can be null. At least one of
 	 *            publisher and subscriber must be provided.
 	 * @param subscriber
 	 *            The Subscriber to register. Can be null. At least one of
 	 *            publisher and subscriber must be provided.
 	 * @param connectionHandler
 	 *            The ConnectionHandler to register.
 	 * @param defaultDigestTimeout
 	 *            The default number of seconds to wait in between the sending of
 	 *            digest messages.
 	 * @param defaultPendingDigestMax
 	 *            The maximum number of records to receive before sending a
 	 *            'digest-message'.
 	 * @param tlsRequired
 	 *            <code>true</code> to force the use of TLS. <code>false</code> if TLS is not
 	 *            required or requested.
 	 * @param allowedMessageDigests
 	 *            A List of digest algorithms to allow. The order of this list is important;
 	 *            The first element in the list is considered to have the highest priority,
 	 *            and the last element in the list is considered to have the lowest priority.
 	 * @param allowedXmlEncodings
 	 *            A List of xml encodings to allow. The order of this list is important;
 	 *            The first element in the list is considered to have the highest priority,
 	 *            and the last element in the list is considered to have the lowest priority.
 	 */
 	public ContextImpl(final Publisher publisher, final Subscriber subscriber,
 			final ConnectionHandler connectionHandler, final int defaultDigestTimeout,
 			final int defaultPendingDigestMax, final boolean tlsRequired,
 			final List<String> allowedMessageDigests, final List<String> allowedXmlEncodings) {
 
 		if(publisher == null && subscriber == null) {
 			throw new IllegalArgumentException("Either a Publisher or a Subscriber must be provided.");
 		}
 
 		if(defaultDigestTimeout <= 0) {
 			throw new IllegalArgumentException("defaultDigestTimeout must be a positive integer.");
 		}
 
 		if(defaultPendingDigestMax <= 0) {
 			throw new IllegalArgumentException("defaultPendingDigestMax must be a positive integer.");
 		}
 
 		this.connectionState = ConnectionState.DISCONNECTED;
 		this.jalSessions = Collections.synchronizedList(new ArrayList<Session>());
 
 		if(allowedMessageDigests != null && !allowedMessageDigests.isEmpty()) {
 			this.allowedMessageDigests = allowedMessageDigests;
 		} else {
 			this.allowedMessageDigests = Arrays.asList(DigestMethod.SHA256);
 		}
 
 		if(allowedXmlEncodings != null && !allowedXmlEncodings.isEmpty()) {
 			this.allowedXmlEncodings = allowedXmlEncodings;
 		} else {
			this.allowedXmlEncodings = Arrays.asList(Utils.ENC_XML);
 		}
 
 		this.subscriber = subscriber;
 		this.publisher = publisher;
 		this.connectionHandler = connectionHandler;
 
 		this.defaultDigestTimeout = defaultDigestTimeout;
 		this.defaultPendingDigestMax = defaultPendingDigestMax;
 		this.tlsRequired = tlsRequired;
 		this.subscriberMap = new HashMap<org.beepcore.beep.core.Session,
 		                            Map<RecordType,SubscriberSessionImpl>>();
 	}
 
 	@Override
 	public void listen(final InetAddress addr, final int port)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void publish(final InetAddress addr, final int port, final RecordType... types)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void subscribe(final InetAddress addr, final int port, final RecordType... types)
 			throws IllegalArgumentException {
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * Add a session to the set of tracked JALoP Sessions.
 	 * @param sess The {@link org.beepcore.beep.core.Session} that owns
 	 * the <code>subSess;
 	 * @param subSess The {@link SubscriberSessionImpl} that should be tracked by
 	 * this {@link ContextImpl}.
 	 * @throws JNLException If a {@link SubscriberSessionImpl} already exists
 	 * in the given {@link org.beepcore.beep.core.Session} for the same
 	 * {@link RecordType} as <code>subSess</code>
 	 */
 	public void addSession(final org.beepcore.beep.core.Session sess, final SubscriberSessionImpl subSess) throws JNLException {
 	    if (subSess.getRecordType() == RecordType.Unset) {
 	        throw new IllegalArgumentException();
 	    }
 	    Map<RecordType, SubscriberSessionImpl> map;
 	    synchronized (this.subscriberMap) {
 	        map = this.subscriberMap.get(sess);
 	        if (map == null) {
 	            map = new HashMap<RecordType, SubscriberSessionImpl>();
 	            this.subscriberMap.put(sess, map);
 	        }
 	        RecordType rtype = subSess.getRecordType();
 	        if (map.containsKey(rtype)) {
 	            throw new JNLException("Attempting to add multiple sessions for the same rtype");
 	        }
 	        map.put(rtype, subSess);
         }
 	}
 	@Override
 	public void close() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void shutdown() {
 		// TODO Auto-generated method stub
 
 	}
 
 	/**
 	 * @return the allowedMessageDigests
 	 */
 	public List<String> getAllowedMessageDigests() {
 		return this.allowedMessageDigests;
 	}
 
 	/**
 	 * @return the allowedXmlEncodings
 	 */
 	public List<String> getAllowedXmlEncodings() {
 		return this.allowedXmlEncodings;
 	}
 
 	/**
 	 * @return the registered subscriber
 	 */
 	public Subscriber getSubscriber() {
 		return this.subscriber;
 	}
 
 	/**
 	 * @return the registered publisher
 	 */
 	public Publisher getPublisher() {
 		return this.publisher;
 	}
 
 	/**
 	 * @return the registered connectionHandler
 	 */
 	public ConnectionHandler getConnectionHandler() {
 		return this.connectionHandler;
 	}
 
 	/**
 	 * @return the defaultDigestTimeout
 	 */
 	public int getDefaultDigestTimeout() {
 		return this.defaultDigestTimeout;
 	}
 
 	/**
 	 * @return the defaultPendingDigestMax
 	 */
 	public int getDefaultPendingDigestMax() {
 		return this.defaultPendingDigestMax;
 	}
 
 	/**
 	 * An enum that represents the state of the {@link Context}'s connection
 	 * to a remote JALoP Network Store.
 	 */
 	public enum ConnectionState {
 		/**
 		 * Indicates that a connection to a remote JALoP Network Store has
 		 * been established
 		 */
 		CONNECTED,
 		/**
 		 * Indicates that there is no current connection to a remote JALoP
 		 * Network Store
 		 */
 		DISCONNECTED
 	}
 
 }
