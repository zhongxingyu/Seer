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
 
 package org.cipango.sip;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.servlet.ServletException;
 import javax.servlet.sip.Address;
 import javax.servlet.sip.SipServletMessage;
 import javax.servlet.sip.SipServletResponse;
 import javax.servlet.sip.SipURI;
 import javax.servlet.sip.URI;
 
 import org.cipango.NameAddr;
 import org.cipango.Server;
 import org.cipango.SipGenerator;
 import org.cipango.SipHandler;
 import org.cipango.SipHeaders;
 import org.cipango.SipMessage;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.Via;
 import org.cipango.log.AccessLog;
 import org.cipango.util.SystemUtil;
 import org.mortbay.component.AbstractLifeCycle;
 import org.mortbay.component.LifeCycle;
 import org.mortbay.io.Buffer;
 import org.mortbay.io.Buffers;
 import org.mortbay.io.ByteArrayBuffer;
 import org.mortbay.io.EndPoint;
 import org.mortbay.log.Log;
 import org.mortbay.util.LazyList;
 import org.mortbay.util.MultiException;
 
 public class TransportManager extends AbstractLifeCycle implements SipHandler, Buffers
 {
     private static final int DEFAULT_MTU = 1500;
     private static final int DEFAULT_MESSAGE_SIZE = 16*1024; // FIXME
     private static final int MAX_MESSAGE_SIZE = 64*1024;
     
     private Server _server;
     private LocalConnector _localConnector;
    
     private SipConnector[] _connectors;
     private int _mtu;
     
     private SipGenerator _sipGenerator;
     
     private AccessLog _logger;
     
     private transient long _statsStartedAt = -1;
     private Object _statsLock = new Object();
     private transient long _messagesReceived;
     private transient long _messagesSent;
     private transient long _nbParseErrors;
     
     private ArrayList<Buffer> _buffers;
     private int _messageSize = 10000;
     
     private int _largeMessageSize = MAX_MESSAGE_SIZE;
     
     public TransportManager() 
     {
         _mtu = SystemUtil.getIntOrDefault("sip.mtu", DEFAULT_MTU);
     }
     
     public void addConnector(SipConnector transport) 
     {
         setConnectors((SipConnector[]) LazyList.addToArray(
         		getConnectors(), transport, SipConnector.class));
     }
     
     public SipConnector[] getConnectors()
     {
         return _connectors;
     }
     
     public void setConnectors(SipConnector[] connectors)
     {
         if (connectors != null) 
         {
             for (int i = 0; i < connectors.length; i++)
             {
                 SipConnector connector = connectors[i];
                 connector.setHandler(this);
                 connector.setServer(_server);
             }
         }
         _server.getContainer().update(this, _connectors, connectors, "connectors");
         _connectors = connectors;
     }
     
     public void setServer(org.mortbay.jetty.Server server)
     {
     	_server = (Server) server;
     }
     
     public Server getServer()
     {
     	return _server;
     }
     
     public Via getVia(int type, InetAddress address)
     {
         return (Via) findConnector(type, address).getVia().clone();
     }
     
     public Address getContact(int type)
     {
         SipConnector sc = findConnector(type, null);
         return new NameAddr((URI) sc.getSipUri().clone());
         //return (Address) findTransport(type, null).getContact().clone();
     }
     
     
     protected void doStart() throws Exception
     {
     	super.doStart();
 
         if (_buffers != null)
     		_buffers.clear();
     	else
     		_buffers = new ArrayList<Buffer>();
         
         _sipGenerator = new SipGenerator();
         
         if (_logger != null && _logger instanceof LifeCycle)
             ((LifeCycle) _logger).start();
         
         for (int i = 0; i < _connectors.length; i++)
         {
             SipConnector connector = _connectors[i];
             connector.start();
         }
     }
 
     protected void doStop() throws Exception
     {
         MultiException mex = new MultiException();
         
         if (_connectors != null)
         {
             for (int i = _connectors.length; i--> 0;)
             {
                 try
                 {
                     _connectors[i].stop();
                 } 
                 catch(Throwable e)
                 {
                     mex.add(e);
                 }
             }
         }
         
         if (_logger != null && _logger instanceof LifeCycle)
         {
         	try { ((LifeCycle)_logger).stop(); } catch (Throwable t) { Log.warn(t); }
         }
         
         super.doStop();
         
         mex.ifExceptionThrow();
     }
     
     public SipConnector findConnector(int type, InetAddress addr)
     {
         for (int i = 0; i < _connectors.length; i++) 
         {
             SipConnector t = _connectors[i];
             if (t.getTransportOrdinal() == type) 
                 return t;
         }
         return _connectors[0];
     }
     
     public void messageReceived()
     {
     	if (_statsStartedAt == -1) 
     		return;
     	synchronized (_statsLock)
         {
             _messagesReceived++;
         }
     }
     
     public void messageSent()
     {
     	if (_statsStartedAt == -1)
     		return;
          synchronized (_statsLock)
          {
              _messagesSent++;
          }
     }
         
     public void handle(SipServletMessage message) throws IOException, ServletException
     {   
     	SipMessage msg = (SipMessage) message;
     	
     	messageReceived();
     	
         if (_logger != null)
             _logger.messageReceived(msg);
         
         if (preValidateMessage((SipMessage) message))
 		{
         	if (msg.isRequest())
             {
                 Via via = msg.getTopVia();
                 String remoteAddr = msg.getRemoteAddr();
                 
                 String host = via.getHost();
                 if (host.indexOf('[') != -1)
                 {
                 	// As there is multiple presentation of an IPv6 address, normalize it.
                 	host = InetAddress.getByName(host).getHostAddress();
                 }
                 
                 if (!host.equals(remoteAddr))
                     via.setReceived(remoteAddr);
 
                 if (via.getRport() != null)
                     via.setRport(Integer.toString(message.getRemotePort()));
             }
 
             getServer().handle(msg);
 		}
 		else if (_statsStartedAt != -1)
 		{
 			synchronized (_statsLock)
 			{
 				_nbParseErrors++;
 			}
 		}  
     }
     
     public boolean isLocalUri(URI uri)
     {
         if (!uri.isSipURI())
             return false;
         
         SipURI sipUri = (SipURI) uri;
 
         if (!sipUri.getLrParam())
             return false;
 
         String host = sipUri.getHost();
         for (int i = 0; i < _connectors.length; i++)
         {
             SipConnector connector = _connectors[i];
             boolean samePort = connector.getPort() == sipUri.getPort() 
 						|| (sipUri.getPort() == -1 && connector.getPort() == connector.getDefaultPort());
             if (samePort)
             {
             	
 	            if ((connector.getHost().equals(host) || connector.getAddr().getHostAddress().equals(host))) 
 	                return true;
 	            if (host.indexOf("[") != -1) // IPv6
 	            {
 	            	try
 					{
 						InetAddress addr = InetAddress.getByName(host);
 						if (connector.getAddr().equals(addr))
 							return true;
 					} catch (UnknownHostException e)
 					{
 						Log.ignore(e);
 					}
 	            }
             }
         }
         return false;
     }
     
     public void send(SipRequest request, int transport, InetAddress address, int port) throws IOException
     {   
     	SipConnector connector = findConnector(transport, address);
     	
         Via via = request.getTopVia();
         via.setTransport(SipConnectors.getName(connector.getTransportOrdinal()));
        String host = connector.getAddr().getHostAddress();
        if (host.contains(":") && !host.contains("["))
    		host = "[" + host + "]";
        via.setHost(host);
         via.setPort(connector.getPort());
                 
         //if (connector.getTransportOrdinal() == SipConnectors.UDP_ORDINAL && (buffer.length() + 200 > _mtu)) 
         {
             // TODO type = Transports.TCP_ORDINAL;
         }
         
         //System.out.println("Sending buffer " + buffer.toString());
         //System.out.println("Sending: " + request.getMethod() + " to " + addr);
         //System.out.println("MF: " + request.getMaxForwards());
 	    
         if (connector.getTransportOrdinal() == SipConnectors.LOCAL_ORDINAL)
         {
         	((LocalConnector) connector).send(request);
         }
         else
         {
         	Buffer buffer = getBuffer(_messageSize); 
         	_sipGenerator.generate(buffer, request);
         	
         	try
         	{
         		connector.send(buffer, address, port);
         	}
         	finally
         	{
         		returnBuffer(buffer);
         	}
         }
 	        
         if (_logger != null) 
             _logger.messageSent(
             		request, 
             		connector.getTransportOrdinal(), 
             		connector.getAddr().getHostAddress(),
             		connector.getPort(),
             		address.getHostAddress(),
             		port);
         
         
         messageSent();
     }
     
     public void send(SipResponse response, SipRequest request) throws IOException 
     {
     	SipConnector connector = null;
     	
     	if (request != null && request.getEndpoint() != null)
     	{
     		connector = request.getEndpoint().getConnector();
     		
     		if (connector.isReliable())
     		{
     			if (connector.getTransportOrdinal() == SipConnectors.LOCAL_ORDINAL)
     			{
     				((LocalConnector) connector).send(response);
     				if (_logger != null) 
     		        	_logger.messageSent(
     		        			response, 
     		        			connector.getTransportOrdinal(), 
     		        			connector.getAddr().getHostAddress(), 
     		        			connector.getPort(), 
     		        			connector.getAddr().getHostAddress(), 
     		        			connector.getPort());  
     		        
     		        messageSent();
     				return;
     			}
     			else
     			{
     	        	Buffer buffer = getBuffer(_messageSize);
     	        	_sipGenerator.generate(buffer, response);
     				try
     				{
     					SipEndpoint endpoint = request.getEndpoint();
     					endpoint.getConnector().send(buffer, endpoint);
     					
     					if (_logger != null)
     					{
     						EndPoint ep = (EndPoint) endpoint;
 				        	_logger.messageSent(
 				        			response, 
 				        			connector.getTransportOrdinal(), 
 				        			ep.getLocalAddr(),
 				        			ep.getLocalPort(), 
 				        			ep.getRemoteAddr(), 
 				        			ep.getRemotePort());    				        
     					}
     					messageSent();
     					return;
     				}
     				finally
     				{
     					returnBuffer(buffer);
     				}
     			}
     		}
     	}
 
     	int transport = -1; 
     	InetAddress address = null;
     	int port = -1;
     	
     	if (request != null)
     		transport = request.transport();
     	else
     		transport = SipConnectors.getOrdinal(response.getTopVia().getTransport());
     	
     	if (transport == SipConnectors.LOCAL_ORDINAL)
     	{
     		connector = _localConnector;
     		((LocalConnector) connector).send(response);
     		
     		address = connector.getAddr();
     		port = connector.getPort();
     	}
     	else 
     	{
 	        Via via = response.getTopVia();
 	        
     		if (request != null)
     			address = request.remoteAddress();
     		else
     			address = InetAddress.getByName(via.getHost());
 	    	
 	    	if (connector == null)
 	    		connector = findConnector(transport, address);
 	        
 	        String srport = via.getRport();
 	        if (srport != null) 
 	        {
 	            port = Integer.parseInt(srport);
 	        } 
 	        else 
 	        {
 	            port = via.getPort();
 	            if (port == -1) 
 	                port = SipConnectors.getDefaultPort(transport);
 	        }
 	        
 	        Buffer buffer = getBuffer(_messageSize); 
         	_sipGenerator.generate(buffer, response);
         	try
         	{
         		connector.send(buffer, address, port);
         	}
         	finally 
         	{
         		returnBuffer(buffer);
         	}
     	}
     	
         if (_logger != null) 
         	_logger.messageSent(
         			response, 
         			connector.getTransportOrdinal(), 
         			connector.getAddr().getHostAddress(), 
         			connector.getPort(), 
         			address.getHostAddress(), 
         			port);  
         
         messageSent();
     }
     
     public Buffer getBuffer(int size) 
     {
 		if (size == _messageSize)
 		{
 			synchronized (_buffers)
 			{
 				if (_buffers.size() == 0)
 					return newBuffer(size);
 	            return (Buffer) _buffers.remove(_buffers.size() - 1);
             }
         }
 		else 
 			return newBuffer(size);
     }
     
     public void returnBuffer(Buffer buffer)
     {
         buffer.clear();
         int c = buffer.capacity();
         if (c == _messageSize)
         {
 	        synchronized (_buffers)
 	        {
 	            _buffers.add(buffer);
 	        }
         }
     }
     
     public Buffer newBuffer(int size)
     {
     	return new ByteArrayBuffer(size);
     }
     
     
     public static void putStringUTF8(Buffer buffer, String s) 
     {
         byte[] bytes = null;
         try 
         {
             bytes = s.getBytes("UTF-8");
         } 
         catch (UnsupportedEncodingException e) 
         {
             throw new RuntimeException();
         }
         buffer.put(bytes);
     }
     
     public void setAccessLog(AccessLog logger)
     {
         try
         {
             if (_logger != null && _logger instanceof LifeCycle)
                 ((LifeCycle) _logger).stop();
         }
         catch (Exception e)
         {
             Log.warn (e);
         }
         
         if (getServer()!=null)
             getServer().getContainer().update(this, _logger, logger, "messagelistener", true);
         
         _logger = logger;
         
         try
         {
             if (isStarted() && (_logger != null) && _logger instanceof LifeCycle)
                 ((LifeCycle)_logger).start();
         }
         catch (Exception e)
         {
             Log.warn(e);
         }
     }
     
     public long getMessagesReceived() 
     {
         return _messagesReceived;
     }
     
     public long getMessagesSent() 
     {
         return _messagesSent;
     }
     
 	public long getNbParseError()
 	{
 		long val = _nbParseErrors;
 		for (int i = 0; i <_connectors.length; i++)
 		{
 			val += _connectors[i].getNbParseError();
 		}
 		return val;
 	}
     
     public void statsReset() 
     {
         synchronized (_statsLock) 
         {
             _statsStartedAt = _statsStartedAt == -1 ? -1 : System.currentTimeMillis();
             _messagesReceived = _messagesSent = 0;
             _nbParseErrors = 0;
             for (int i = 0; i <_connectors.length; i++)
             {
 				 _connectors[i].statsReset();
 			}
         }
     }
     
     public void setStatsOn(boolean on) 
     {
         if (on && _statsStartedAt != -1) 
             return;
         statsReset();
         _statsStartedAt = on ? System.currentTimeMillis() : -1;
     }
     
     public boolean isStatsOn() 
     {
         return  _statsStartedAt != -1;
     }
 
     
 	
     
 	public boolean preValidateMessage(SipMessage message)
 	{
 		boolean valid = true;
 		try 
 		{
 			if (!isUnique(SipHeaders.FROM_BUFFER, message)
 					|| !isUnique(SipHeaders.TO_BUFFER, message)
 					|| !isUnique(SipHeaders.CALL_ID_BUFFER, message)
 					|| !isUnique(SipHeaders.CSEQ_BUFFER, message))
 			{
 				valid = false;
 			}
 			else if (message.getTopVia() == null
 					|| message.getFrom() == null
 					|| message.getTo() == null
 					|| message.getCSeq() == null)
 			{
 				Log.info("Received bad message: unparsable required headers");
 				valid = false;
 			}
 			message.getAddressHeader("contact");
 				
 			if (message instanceof SipRequest)
 			{
 				SipRequest request = (SipRequest) message;
 				if (request.getRequestURI() == null)
 					valid = false;
 				request.getTopRoute();
 				if (!request.getCSeq().getMethod().equals(request.getMethod()))
 				{
 					Log.info("Received bad request: CSeq method does not match");
 					valid = false;
 				}
 			}
 			else
 			{
 				int status = ((SipResponse) message).getStatus();
 				if (status < 100 || status > 699)
 				{
 					Log.info("Received bad response: Invalid status code: " + status);
 					valid = false;
 				}
 			}
 		}
 		catch (Exception e) 
 		{
 			Log.info("Received bad message: Some headers are not parsable: {}", e);
 			Log.debug("Received bad message: Some headers are not parsable", e);
 			valid = false;
 		}
 				
 		try 
 		{
 			if (!valid 
 					&& message instanceof SipRequest 
 					&& !message.isAck()
 					&& message.getTopVia() != null)
 			{
 				// TODO send response stateless
 				SipResponse response = 
 					(SipResponse) ((SipRequest) message).createResponse(SipServletResponse.SC_BAD_REQUEST);
 				send(response, (SipRequest) message);
 			}
 		}
 		catch (Exception e) 
 		{
 			Log.ignore(e);
 		}
 		
 		return valid;
 	}
 	
 	private boolean isUnique(Buffer headerName, SipMessage message)
 	{
 		Iterator it = message.getFields().getValues(headerName);
 		if (!it.hasNext())
 		{
 			Log.info("Received bad message: Missing required header: " + headerName);
 			return false;
 		}
 		it.next();
 		if (it.hasNext())
 			Log.info("Received bad message: Duplicate header: " + headerName);
 		return !it.hasNext();
 	}
 
 
 }
