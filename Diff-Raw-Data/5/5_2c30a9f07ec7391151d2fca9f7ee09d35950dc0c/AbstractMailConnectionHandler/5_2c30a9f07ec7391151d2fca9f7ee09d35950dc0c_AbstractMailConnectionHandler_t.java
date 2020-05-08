 /*-
  * Copyright (c) 2008, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.mail;
 
 import java.io.IOException;
 
 import net.rim.device.api.i18n.ResourceBundle;
 import net.rim.device.api.system.EventLogger;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.util.Queue;
 
 /**
  * This class is responsible for managing the lifecycle of a mail
  * server network connection.  It also encapsulates the necessary
  * hooks for UI interactions.
  */
 public abstract class AbstractMailConnectionHandler {
 	protected static ResourceBundle resources = ResourceBundle.getBundle(LogicMailResource.BUNDLE_ID, LogicMailResource.BUNDLE_NAME);
 	private MailClient client;
 	private ConnectionThread connectionThread;
 	private int state;
 	private boolean transitionSilent;
 	private Queue requestQueue;
 	private int retryCount;
 	private boolean invalidLogin;
 	private boolean shutdownInProgress;
 	private ConnectionHandlerRequest requestInProgress;
 	
 	private static final int RETRY_LIMIT = 2;
 	
     public static final int REQUEST_DISCONNECT = 1;
     public static final int REQUEST_DISCONNECT_TIMEOUT = 2;
     
 	// The various states of a mail connection
 	public static final int STATE_CLOSED   = 0;
 	public static final int STATE_OPENING  = 1;
 	public static final int STATE_OPENED   = 2;
 	public static final int STATE_REQUESTS = 3;
 	public static final int STATE_IDLE     = 4;
 	public static final int STATE_CLOSING  = 5;
 
 	protected AbstractMailConnectionHandler(MailClient client) {
 		this.client = client;
 		this.connectionThread = new ConnectionThread();
 		this.state = STATE_CLOSED;
 		this.requestQueue = new Queue();
 		this.retryCount = 0;
 		this.invalidLogin = false;
 		this.shutdownInProgress = false;
 	}
 
 	/**
 	 * Start the mail connection handler thread.
 	 */
 	public void start() {
 		if(!connectionThread.isAlive()) {
 			if(connectionThread.isShutdown()) {
 				connectionThread = new ConnectionThread();
 			}
 			connectionThread.start();
 		}
     }
 
 	/**
 	 * Shutdown the mail connection handler thread.
 	 * 
 	 * @param wait If true, wait for all pending requests to finish.
 	 */
 	public void shutdown(boolean wait) {
 		if(!connectionThread.isAlive()) {
 			return;
 		}
 		synchronized(requestQueue) {
 			shutdownInProgress = true;
 		}
 		if(wait) {
 			Object element;
 			synchronized(requestQueue) {
 				element = requestQueue.element();
 			}
 			while(element != null) {
 				synchronized(requestQueue) {
 					element = requestQueue.element();
 				}
 				Thread.yield();
 			}
 		}
 		setConnectionState(STATE_CLOSING);
 		connectionThread.shutdown();
 		try {
 			connectionThread.join();
 		} catch (InterruptedException e) { }
 		
 		synchronized(requestQueue) {
 			shutdownInProgress = false;
 		}
 		MailConnectionManager.getInstance().fireMailConnectionStateChanged(
 				client.getConnectionConfig(),
 				MailConnectionStateEvent.STATE_DISCONNECTED);
 	}
 
 	/**
 	 * Gets whether the connection thread is currently running.
 	 * 
 	 * @return True if running, false if shutdown.
 	 */
 	public boolean isRunning() {
 		return connectionThread.isAlive();
 	}
 	
 	/**
 	 * Add a request to the queue.
 	 * If the connection is shutting down, all requests will be ignored.
 	 * 
 	 * @param request Request object to be added
 	 */
 	public void addRequest(ConnectionHandlerRequest request) {
 		synchronized(requestQueue) {
 			if(!shutdownInProgress) {
 				requestQueue.add(request);
 				requestQueue.notifyAll();
 			}
 		}
 	}
 	
 	/**
 	 * Handles the CLOSED state.
 	 */
 	private void handleClosedConnection() {
 		showTransitionStatus(null);
 		ConnectionHandlerRequest adminRequest = null;
 		synchronized(requestQueue) {
 			if(requestQueue.element() != null) {
 			    ConnectionHandlerRequest request = (ConnectionHandlerRequest)requestQueue.element();
 			    if(request.isAdministrative()) {
 			        adminRequest = request;
 			        requestQueue.remove();
 			    }
 			    else {
 			        setConnectionState(STATE_OPENING, !request.isDeliberate());
 			    }
 			}
 			else if(!connectionThread.isShutdown()) {
 				try {
 					requestQueue.wait();
 				} catch (InterruptedException e) { }
 			}
 		}
 		
 		if(adminRequest != null) {
 		    requestInProgress = adminRequest;
 		    handleAdministrativeRequest(adminRequest);
 		    requestInProgress = null;
 		}
 	}
 	
     /**
      * Handles the OPENING state.
      * Open a new connection, and authenticates with the server.
      * This method should typically establish a socket connection and
      * then send the protocol-specific login commands.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handleOpeningConnection() throws IOException, MailException {
 		showTransitionStatus(resources.getString(LogicMailResource.MAILCONNECTION_OPENING_CONNECTION));
 		if(checkLogin(client)) {
 			if(client.open()) {
 				invalidLogin = false;
 				setConnectionState(STATE_OPENED);
 				MailConnectionManager.getInstance().fireMailConnectionStateChanged(
 						client.getConnectionConfig(),
 						MailConnectionStateEvent.STATE_CONNECTED);
 				return;
 			}
 			else {
 				invalidLogin = true;
 				return;
 			}
 		}
 		// Unable to open, so transition to closing and clear the queue
         setConnectionState(STATE_CLOSING, this.transitionSilent);
         synchronized(requestQueue) {
             clearRequestQueue(null);
         }
 	}
     
 	/**
 	 * Handles the OPENED state.
 	 * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
 	 */
 	private void handleOpenedConnection() throws IOException, MailException {
 		showTransitionStatus(null);
 		retryCount = 0;
 		synchronized(requestQueue) {
 		    ConnectionHandlerRequest request = (ConnectionHandlerRequest)requestQueue.element();
 		    if(request != null) {
 			    showInitialStatusIfDeliberate(request);
 				setConnectionState(STATE_REQUESTS);
 			}
 			else {
 				setConnectionState(STATE_IDLE);
 			}
 		}
 	}
 
 	/**
      * Handles the REQUESTS state to process any pending server requests.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handlePendingRequests() throws IOException, MailException {
 		Queue requestQueue = getRequestQueue();
 		Object element;
 		synchronized(requestQueue) {
 			element = requestQueue.element();
 		}
 		while(element != null) {
 			synchronized (requestQueue) {
 				requestQueue.remove();
 			}
 			ConnectionHandlerRequest request = (ConnectionHandlerRequest)element;
 			
 			// Delegate to subclasses to handle the specific request
 			requestInProgress = request;
 			if(request.isAdministrative()) {
 			    handleAdministrativeRequest(request);
 			}
 			else {
 			    handleRequest(request);
 			}
 			requestInProgress = null;
 			
 			synchronized(requestQueue) {
 				element = requestQueue.element();
 			}
 		}
 		setConnectionState(STATE_IDLE);
 	}
 
 	/**
 	 * Handles a specific request during the REQUESTS state.
 	 * <p>
 	 * Subclasses should only override this method if they need to perform
 	 * operations before or after the normal processing of a request.
 	 * </p>
 	 * 
 	 * @param request The request to handle
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
 	 */
 	protected void handleRequest(ConnectionHandlerRequest request) throws IOException, MailException {
 	    request.execute(client);
 	}
 	
 	/**
 	 * Handles a specific administrative request, which could be processed
 	 * during any state.  These requests should only be used for simple
 	 * operations, like changing client properties, which do not require a
 	 * network connection.
 	 *
 	 * @param request The request to handle
 	 */
 	protected void handleAdministrativeRequest(ConnectionHandlerRequest request) {
 	    try {
 	        request.execute(client);
 	    } catch (Exception e) {
 	        request.notifyConnectionRequestFailed(e, true);
 	    }
 	}
 	
 	/**
 	 * Handles a specific request being removed from the queue due to errors.
 	 * <p>
 	 * Subclasses should use this as a hook to notify any listeners of the
 	 * request failing.
 	 * </p>
 	 * 
      * @param request the request that failed
 	 * @param exception Exception that was thrown to fail the request,
 	 *     or null if it failed due to a queue flush
 	 * @param isFinal true if the connection will be closed, false if it is being reopened
 	 */
 	protected void handleRequestFailed(ConnectionHandlerRequest request, Throwable exception, boolean isFinal) {
 	    request.notifyConnectionRequestFailed(exception, isFinal);
 	}
 	
     /**
      * Handles the IDLE connection state which occurs after all
      * pending requests have been handled.  This method is called
      * at a periodic interval when no requests are pending.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handleIdleConnection() throws IOException, MailException {
 		showTransitionStatus(null);
 		
 		handleBeginIdle();
 
 		while(true) {
 		    ConnectionHandlerRequest adminRequest = null;
 		    synchronized(requestQueue) {
 		        ConnectionHandlerRequest request = (ConnectionHandlerRequest)requestQueue.element();
 		        if(request != null) {
 		            showInitialStatusIfDeliberate(request);
 		            setConnectionState(STATE_REQUESTS);
 		        }
 		        else if(connectionThread.isShutdown()) {
 		            setConnectionState(STATE_CLOSING);
 		        }
 		        else {
 		            try {
 		                requestQueue.wait();
 		            } catch (InterruptedException e) { }
 
 		            // Check if a new request caused us to break out of idle,
 		            // in which case we should display that request's status
 		            // message while recovering from idle.
 		            synchronized(requestQueue) {
 		                request = (ConnectionHandlerRequest)requestQueue.element();
 		                if(request != null) {
 		                    if(request.isAdministrative()) {
 		                        adminRequest = request;
 		                        requestQueue.remove();
 		                    }
 		                    else {
 		                        showInitialStatusIfDeliberate(request);
 		                    }
 		                }
 		            }
 		        }
 		    }
 		    
 		    if(adminRequest != null) {
 		        requestInProgress = adminRequest;
 		        handleAdministrativeRequest(adminRequest);
 		        requestInProgress = null;
 		    }
		    else {
		        break;
		    }
 		}
 		
         handleEndIdle();
         
         synchronized(requestQueue) {
             if(getConnectionState() == STATE_IDLE) {
                 ConnectionHandlerRequest request = (ConnectionHandlerRequest)requestQueue.element();
                 if(request != null) {
                     showInitialStatusIfDeliberate(request);
                     setConnectionState(STATE_REQUESTS);
                 }
                 else if(connectionThread.isShutdown()) {
                     setConnectionState(STATE_CLOSING);
                 }
             }
         }
 	}
     
 	/**
 	 * Handles the start of the IDLE state.
 	 * <p>
 	 * Subclasses should do anything they need to do when their connection
 	 * enters an idle state with the server.  This could involve sending
 	 * a command, initializing a timer, or doing nothing at all.
 	 * After this method completes, the connection handler will sleep until
 	 * a new request arrives, or it is commanded to shutdown.
 	 * </p>
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
 	 */
 	protected abstract void handleBeginIdle() throws IOException, MailException;
 	
     /**
      * Handles the end of the IDLE state.
      * <p>
      * Subclasses should do anything they need to do to make their connection
      * leave the idle state with the server.  Exceptions should also be thrown
      * from this method for any errors that occurred during the idle state.
      * </p>
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	protected abstract void handleEndIdle() throws IOException, MailException;
 	
     /**
      * Handles the CLOSING state to close an existing connection.
      * This method should send protocol-specific logout commands and
      * then terminate the connection.
      * 
      * @throws IOException on I/O errors
      * @throws MailException on protocol errors
      */
 	private void handleClosingConnection() throws IOException, MailException {
 		showTransitionStatus(resources.getString(LogicMailResource.MAILCONNECTION_CLOSING_CONNECTION));
 		handleBeforeClosing();
 		try { client.close(); } catch (IOException e) {} catch (MailException e) {}
 		setConnectionState(STATE_CLOSED);
 		if(!shutdownInProgress) {
 			MailConnectionManager.getInstance().fireMailConnectionStateChanged(
 					client.getConnectionConfig(),
 					MailConnectionStateEvent.STATE_DISCONNECTED);
 		}
 	}
 
 	/**
 	 * Called at the start of the CLOSING state.
 	 * This method should be overridden if the subclass needs
 	 * to do any cleanup before its connection is closed.
 	 */
 	protected void handleBeforeClosing() { }
 	
 	/**
 	 * Gets the current connection state.
 	 * 
 	 * @return Connection state
 	 */
 	public synchronized int getConnectionState() {
 		return this.state;
 	}
 	
     /**
      * Sets the current connection state.
      * 
      * @param state Connection state
      */
     protected void setConnectionState(int state) {
         setConnectionState(state, false);
     }
     
 	/**
 	 * Sets the current connection state.
 	 * 
 	 * @param state Connection state
 	 * @param transitionSilent true, if the transition should not cause a notification
 	 */
 	protected synchronized void setConnectionState(int state, boolean transitionSilent) {
 		if(state >= STATE_CLOSED && state <= STATE_CLOSING) {
 			this.state = state;
 			this.transitionSilent = transitionSilent;
 		}
 	}
 	
 	/**
 	 * Gets the request queue.
 	 * Any use of the queue must be synchronized.
 	 * 
 	 * @return Request queue
 	 */
 	protected Queue getRequestQueue() {
 		return this.requestQueue;
 	}
 	
 	/**
 	 * Gets the request currently in progress.
 	 *
 	 * @return the request in progress, or <code>null</code>.
 	 */
 	public synchronized ConnectionHandlerRequest getRequestInProgress() {
 	    return requestInProgress;
 	}
 	
 	/**
 	 * Gets whether a shutdown is currently in progress.
 	 * @return True if shutdown is in progress.
 	 */
 	protected boolean getShutdownInProgress() {
 		return this.shutdownInProgress;
 	}
 	
 	/**
 	 * Sleep the connection thread.
 	 * @param time Time to sleep, in milliseconds.
 	 */
 	protected void sleepConnectionThread(long time) {
 		if(!connectionThread.isShutdown()) {
 			try {
 				ConnectionThread.sleep(time);
 			} catch (InterruptedException e) { }
 		}
 	}
 	
 	/**
 	 * Show a status message.
 	 * 
 	 * @param message The message to show
 	 */
 	protected void showTransitionStatus(String message) {
 	    if(message == null || !transitionSilent) {
 	        MailConnectionManager.getInstance().fireMailConnectionStatus(client.getConnectionConfig(), null, message);
 	    }
 	}
 	
 	/**
 	 * Show an error message.
 	 * 
 	 * @param message The message to show
 	 */
 	private void showError(String message) {
 		MailConnectionManager.getInstance().fireMailConnectionError(client.getConnectionConfig(), null, message);
 	}
 	
 	/**
 	 * Check the login information, requesting a prompt if necessary.
 	 * 
 	 * @return True if the information was validated, false otherwise.
 	 */
 	private boolean checkLogin(MailClient client) {
 	    if(!client.isLoginRequired()) { return true; }
 	    
         String username = client.getUsername();
         String password = client.getPassword();
         // If the username and password are not null,
         // but are empty, request login information.
         if(invalidLogin || ((username != null && password != null) &&
            (username.trim().equals("") || password.trim().equals("")))) {
 
         	MailConnectionLoginEvent e = new MailConnectionLoginEvent(
         			this,
         			client.getConnectionConfig(),
         			username,
         			password);
     		MailConnectionManager.getInstance().fireMailConnectionLogin(e);
         	
            if(!e.isCanceled()) {
                client.setUsername(e.getUsername());
                client.setPassword(e.getPassword());
                return true;
            }
            else {
         	   return false;
            }
         }
         else {
         	return true;
         }
 	}
 
 	/**
 	 * Called when a connection cannot be opened.
 	 * Subclasses should override this if they need to implement some sort of
 	 * polling and/or retry behavior that does not respond immediately.
 	 * 
 	 * @param isSilent true, if the operation in progress is not one that the
 	 *     user was being given status notifications for
 	 */
 	protected void handleFailedConnection(boolean isSilent) { }
 	
 	/**
 	 * Handles <tt>IOException</tt>s that occur during a connection.
 	 * These are typically due to network errors, and are handled
 	 * in different ways depending on the connection state:
 	 * <ul>
 	 * <li><b>OPENING</b> - Connection failure, switch to CLOSED and clear the queue.</li>
 	 * <li><b>CLOSING</b> - Disconnection failure, switch to CLOSED and clear the queue.</li>
 	 * <li><b>OPENED/REQUESTS/IDLE</b> - Connection timeout or loss, switch to OPENING to attempt reconnection</li>
 	 * </ul>
 	 * 
 	 * @param e Exception data.
 	 */
 	private void handleIOException(IOException e) {
 		EventLogger.logEvent(AppInfo.GUID, e.toString().getBytes(), EventLogger.ERROR);
 
 		int state = getConnectionState();
 		boolean isFinal;
 		boolean isSilent;
 		
 		// Keep silent if the connection died without a request in progress
 		if(requestInProgress == null) {
 		    isSilent = true;
 		}
 		else {
 		    isSilent = isSilentRequestInProgress();
 		}
 		
 		if(state == STATE_OPENING || state == STATE_CLOSING || retryCount > RETRY_LIMIT) {
 			// Switch to the CLOSING state and clear the request queue.
 			synchronized (requestQueue) {
 				setConnectionState(STATE_CLOSING, isSilent);
 				clearRequestQueue(e);
 			}
 			// Only display the error if we are not going to retry
 	        if(!isSilent) { showError(e.getMessage()); }
 	        isFinal = true;
 	        
 	        //TODO: Figure out a way of detecting if the failure was not coverage-related
 	        handleFailedConnection(isSilent);
 		}
 		else {
 			retryCount++;
 			
 			// Explicitly notify any listeners that the connection dropped,
 			// since the CLOSING state is not entered in this situation.
             MailConnectionManager.getInstance().fireMailConnectionStateChanged(
                     client.getConnectionConfig(),
                     MailConnectionStateEvent.STATE_DISCONNECTED);
             
             // Set the next state to opening
 			setConnectionState(STATE_OPENING, isSilent);
 			isFinal = false;
 		}
 
         // Notify failure of the current request-in-progress, if applicable
         if(requestInProgress != null) {
             handleRequestFailed(requestInProgress, e, isFinal);
             requestInProgress = null;
         }
 	}
 	
 	/**
 	 * Handles <tt>MailException</tt>s that occur during a connection.
 	 * These are typically due to protocol errors, and can often be recovered
 	 * from.  If they are recoverable, then they are not fatal to the connection.
 	 * Otherwise, they result in a graceful logout and connection termination.
 	 * 
 	 * @param e Exception data.
 	 */
 	private void handleMailException(MailException e) {
 	    boolean disconnect = (e.getCause() == REQUEST_DISCONNECT);
 	    boolean disconnectTimeout = (e.getCause() == REQUEST_DISCONNECT_TIMEOUT);
 	    boolean isSilent = isSilentRequestInProgress();
 	    
 	    if(!disconnect && !disconnectTimeout) { 
 	        EventLogger.logEvent(AppInfo.GUID, e.toString().getBytes(), EventLogger.ERROR);
 	    }
 	    
         if(disconnectTimeout) {
             // In this case, we behave like a fatal disconnect only if the
             // request queue is empty.
             synchronized (requestQueue) {
                 if(requestQueue.element() == null) {
                     setConnectionState(STATE_CLOSING, isSilent);
                 }
             }
         }
         else if(e.isFatal()) {
 			// Switch to the CLOSING state and clear the request queue.
 			synchronized (requestQueue) {
 				setConnectionState(STATE_CLOSING, isSilent);
 				clearRequestQueue(e);
 			}
 		}
 		
 		if(!disconnect && !disconnectTimeout && !isSilent) {
 		    showError(e.getMessage());
 		}
 
         // Notify failure of the current request-in-progress, if applicable
         if(requestInProgress != null) {
             handleRequestFailed(requestInProgress, e, true);
             requestInProgress = null;
         }
 	}
 	
 	/**
 	 * Handles any other <tt>Throwable</tt> that occurs during a connection.
 	 * These include any other unknown exception, and are usually caused by
 	 * software bugs that occur during server response parsing.  Since it is
 	 * impossible to know what their cause is, they are handled similarly
 	 * to a fatal <tt>MailException</tt>.
 	 * 
 	 * @param t Throwable data.
 	 */
 	private void handleThrowable(Throwable t) {
 		EventLogger.logEvent(AppInfo.GUID, t.toString().getBytes(), EventLogger.ERROR);
 		boolean isSilent = isSilentRequestInProgress();
 		
 		// Switch to the CLOSING state and clear the request queue.
 		synchronized (requestQueue) {
 			setConnectionState(STATE_CLOSING, isSilent);
 			clearRequestQueue(t);
 		}
 		if(!isSilent) { showError(t.getMessage()); }
 
 		// Notify failure of the current request-in-progress, if applicable
 		if(requestInProgress != null) {
             handleRequestFailed(requestInProgress, t, true);
             requestInProgress = null;
 		}
 	}
 
     private boolean isSilentRequestInProgress() {
         if(requestInProgress != null) {
             return !requestInProgress.isDeliberate();
         }
         else {
             return false;
         }
     }
 
 	/**
 	 * Clear the request queue, sending any necessary failure notifications.
 	 * 
 	 * @param t exception that lead to the queue flush, if applicable
 	 */
 	private void clearRequestQueue(Throwable t) {
 	    synchronized (requestQueue) {
 	        Object element = requestQueue.element();
 	        while(element != null) {
 	            ConnectionHandlerRequest request = (ConnectionHandlerRequest)element;
 	            requestQueue.remove();
                 handleRequestFailed(request, null, true);
 	            element = requestQueue.element();
 	        }
 	    }
 	}
 
     private static void showInitialStatusIfDeliberate(ConnectionHandlerRequest request) {
         if(request.isDeliberate()) { request.showInitialStatus(); }
     }
 
 	/**
 	 * Thread for the standard mail connection lifecycle.
 	 */
 	private class ConnectionThread extends Thread {
 		private boolean shutdown = false;
 		private boolean shutdownComplete = false;
 		
 		public ConnectionThread() {
 			super("ConnectionThread");
 		}
 		
 		public void shutdown() {
 			synchronized(this) {
 				shutdown = true;
 			}
 			synchronized(requestQueue) {
 				requestQueue.notifyAll();
 			}
 		}
 		
 		public boolean isShutdown() {
 			boolean result;
 			synchronized(this) {
 				result = shutdown;
 			}
 			return result;
 		}
 		
         public void run() {
         	while(!shutdownComplete) {
         		try {
 	        		switch(state) {
 	        		case STATE_CLOSED:
 	        			handleClosedConnection();
 	        			if(shutdown) {
 	        				shutdownComplete = true;
 	        			}
 	        			break;
 	        		case STATE_OPENING:
 	    				handleOpeningConnection();
 	        			break;
 	        		case STATE_OPENED:
 	    				handleOpenedConnection();
 	        			break;
 	        		case STATE_REQUESTS:
 	    				handlePendingRequests();
 	        			break;
 	        		case STATE_IDLE:
 	    				handleIdleConnection();
 	        			break;
 	        		case STATE_CLOSING:
 	    				handleClosingConnection();
 	        			break;
 	        		}
         		} catch (IOException e) {
         			if(state == STATE_CLOSING) {
         				shutdownComplete = true;
         			}
         			else {
         				handleIOException(e);
         			}
         		} catch (MailException e) {
         			if(state == STATE_CLOSING) {
         				shutdownComplete = true;
         			}
         			else {
         				handleMailException(e);
         			}
         		} catch (Throwable t) {
         			if(state == STATE_CLOSING) {
         				shutdownComplete = true;
         			}
         			else {
         				handleThrowable(t);
         			}
         		}
         	}
         }
 	}
 }
