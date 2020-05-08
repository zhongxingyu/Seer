 /**********************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 *
  * Contributors:
  *    IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.internet.monitor.core;
 
 import java.util.Date;
 import java.util.Properties;
 
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.wst.internet.monitor.core.internal.Monitor;
 import org.eclipse.wst.internet.monitor.core.internal.Trace;
 /**
  * Represents a TCP/IP request made between the client and the server.
  * Each request represents a request-response pair, where the request
  * is from client -> server, and the response is from server -> client.
  * <p>
  * Requests are created by a running monitor. They do not have a reference
  * back to the monitor because the monitor may have been deleted or modified
  * since the request was created.
  * </p>
  * <p>
  * This interface is intended to be extended only by clients
  * to extend the <code>protocolAdapters</code> extension point. 
  * </p>
  * 
  * @since 1.0
  */
 public class Request implements IAdaptable {
 	protected Monitor monitor;
 	protected Date date;
 	protected long responseTime = -1;
 	protected int localPort;
 	protected String remoteHost;
 	protected int remotePort;
 	protected byte[] request;
 	protected byte[] response;
 	
 	protected String name;
 	protected String protocolId;
 
 	protected Properties properties;
 	
 	/**
 	 * Request2 content type (value 1) for the transport (header) of a request
 	 * or response.
 	 * 
 	 * @see #getRequest(int)
 	 * @see #getResponse(int)
 	 */
 	public static final int TRANSPORT = 1;
 
 	/**
 	 * Request2 content type (value 2) for the content (body) of a request
 	 * or response.
 	 * 
 	 * @see #getRequest(int)
 	 * @see #getResponse(int)
 	 */
 	public static final int CONTENT = 2;
 
 	/**
 	 * Request2 content type (value 3) for the entire content of a request
 	 * or response.
 	 * 
 	 * @see #getRequest(int)
 	 * @see #getResponse(int)
 	 */
 	public static final int ALL = 3;
 
 	/**
 	 * Create a new Request.
 	 */
 	public Request(Monitor monitor, String protocolId, int localPort, String remoteHost, int remotePort) {
 		super();
 		this.monitor = monitor;
 		this.protocolId = protocolId;
 		this.localPort = localPort;
 		this.remoteHost = remoteHost;
 		this.remotePort = remotePort;
 		date = new Date();
 		properties = new Properties();
 		if (monitor != null)
 			monitor.addRequest(this);
 	}
 
 	/**
 	 * Returns the protocol responsible for creating this request.
 	 * 
 	 * @return the protocol id
 	 */
 	public String getProtocol() {
 		return protocolId;
 	}
 
 	/**
 	 * Returns the time this request was made.
 	 *
 	 * @return the timestamp
 	 */
 	public Date getDate() {
 		return date;
 	}
 
 	/**
 	 * Returns the local (client) port.
 	 *
 	 * @return the local port number
 	 */
 	public int getLocalPort() {
 		return localPort;
 	}
 
 	/**
 	 * Returns the remote (server) host.
 	 *
 	 * @return the remote host
 	 */
 	public String getRemoteHost() {
 		return remoteHost;
 	}
 
 	/**
 	 * Returns the remote (server) port.
 	 *
 	 * @return the remote port number
 	 */
 	public int getRemotePort() {
 		return remotePort;
 	}
 
 	/**
 	 * Returns the selected content of the request portion of this request.
 	 * <p>
 	 * [issue: I don't know how to explain this. For basic TCP/IP requests,
 	 * distinction between transport and content is ignored.
 	 * For HTTP requests, this TRANSPORT returns just the HTTP header and 
 	 * CONTENT returns just the HTTP body without the headers. What would
 	 * it mean for other protocols?
 	 * </p>
 	 *
 	 * @param type the content type: one of {@link #TRANSPORT},
 	 * {@link #CONTENT}, or {@link #ALL}
 	 * @return the content bytes
 	 */
 	public byte[] getRequest(int type) {
 		return request;
 	}
 
 	/**
 	 * Returns the selected content of the response portion of this request.
 	 * <p>
 	 * [issue: I don't know how to explain this. For basic TCP/IP requests,
 	 * distinction between transport and content is ignored.
 	 * For HTTP requests, this TRANSPORT returns just the HTTP header and 
 	 * CONTENT returns just the HTTP body without the headers. What would
 	 * it mean for other protocols?]
 	 * </p>
 	 *
 	 * @param type the content type: one of {@link #TRANSPORT},
 	 * {@link #CONTENT}, or {@link #ALL}
 	 * @return the content bytes
 	 */
 	public byte[] getResponse(int type) {
 		return response;
 	}
 
 	/**
 	 * Returns the server's response time in milliseconds. If the request
 	 * has not been completed yet, -1 is returned.
 	 *
 	 * @return the server's response time, or -1 if there has been no
 	 *    response yet
 	 */
 	public long getResponseTime() {
 		return responseTime;
 	}
 
 	/**
 	 * Returns a name for this request.
 	 *
 	 * @return the name
 	 */
 	public String getName() {
 		if (name == null)
 			return getRemoteHost() + ":" + getRemotePort();
 		
 		return name;
 	}
 	
 	/**
 	 * Sets the name of this request.
 	 *
 	 * @param n the name
 	 */
 	protected void setName(String n) {
 		name = n;
 	}
 
 	/**
 	 * Sets the given key-value property on this request. To remove a property,
 	 * set the value to null.
 	 * <p>
 	 * This method is typically called by protocol adapters to attach protocol-
 	 * specific fields to the request, but it may be called by any client.
 	 * </p>
 	 * 
 	 * @param key the key
 	 * @param value the value
 	 */
 	public void setProperty(String key, Object value) {
 		try {
 			if (properties.containsKey(key))
 				properties.remove(key);
 			if (value != null)
 				properties.put(key, value);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Could not add property", e);
 		}
 	}
 
 	/**
 	 * Returns the value of the property with the given key from this request.
 	 * If the key does not exist, <code>null</code> is returned.
 	 * 
 	 * @param key the property key 
 	 * @return the property value
 	 */
 	public Object getProperty(String key) {
 		try {
 			return properties.get(key);
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	/**
 	 * Add to the request.
 	 *
 	 * @param addRequest byte[]
 	 */
 	public void addToRequest(byte[] addRequest) {
 		if (addRequest == null || addRequest.length == 0)
 			return;
 	
 		if (request == null || request.length == 0) {
 			setRequest(addRequest);
 			return;
 		}
 	
 		int size = request.length + addRequest.length;
 		byte[] b = new byte[size];
 		System.arraycopy(request, 0, b, 0, request.length);
 		System.arraycopy(addRequest, 0, b, request.length, addRequest.length);
 		request = b;
 		fireChangedEvent();
 	}
 
 	/**
 	 * Add to the response.
 	 *
 	 * @param addResponse byte[]
 	 */
 	public void addToResponse(byte[] addResponse) {
 		if (addResponse == null || addResponse.length == 0)
 			return;
 	
 		if (response == null || response.length == 0) {
 			setResponse(addResponse);
 			return;
 		}
 	
 		int size = response.length + addResponse.length;
 		byte[] b = new byte[size];
 		System.arraycopy(response, 0, b, 0, response.length);
 		System.arraycopy(addResponse, 0, b, response.length, addResponse.length);
 		response = b;
 		fireChangedEvent();
 	}
 
 	/**
 	 * Set the request.
 	 *
 	 * @param request byte[]
 	 */
 	protected void setRequest(byte[] request) {
 		if (request == null || request.length == 0)
 			return;
 	
 		this.request = request;
 		monitor.requestChanged(this);
 	}
 
 	/**
 	 * Set the response.
 	 *
 	 * @param response byte[]
 	 */
 	protected void setResponse(byte[] response) {
 		if (response == null || response.length == 0)
 			return;
 	
 		this.response = response;
 		responseTime = System.currentTimeMillis() - date.getTime();
 		monitor.requestChanged(this);
 	}
 
 	/**
 	 * Returns the monitor that created this request.
 	 * Change events will be fired from this monitor.
 	 * <p>
 	 * Note that the monitor may have been editted since this
 	 * request was created, so you cannot rely on the monitor's
 	 * hostname or port values.
 	 * </p>
 	 * 
 	 * @return the monitor that created this request
 	 */
 	public IMonitor getMonitor() {
 		return monitor;
 	}
 
	/**
	 * Fire a change event to notify monitor listeners that the request has changed.
	 */
 	protected void fireChangedEvent() {
		if (monitor != null)
			monitor.requestChanged(this);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	public Object getAdapter(Class adapter) {
 		return Platform.getAdapterManager().getAdapter(this, adapter);
 	}
 }
