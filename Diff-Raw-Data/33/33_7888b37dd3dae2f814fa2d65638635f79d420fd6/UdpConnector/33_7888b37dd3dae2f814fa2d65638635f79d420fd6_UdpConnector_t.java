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
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 
 import javax.servlet.sip.URI;
 
 import org.cipango.SipHeaders;
 import org.cipango.SipMessage;
 import org.cipango.SipRequest;
 import org.cipango.SipResponse;
 import org.cipango.URIFactory;
 import org.cipango.util.HexString;
 import org.mortbay.io.Buffer;
 import org.mortbay.io.ByteArrayBuffer;
 import org.mortbay.io.View;
 import org.mortbay.log.Log;
 
 public class UdpConnector extends AbstractSipConnector
 {
 	public static final int MAX_UDP_SIZE = 65536;
 	public static final int DEFAULT_PORT = 5060;
 	public static final boolean RELIABLE = false;
     
     private DatagramPacket[] _packets;
     private DatagramSocket _datagramSocket;
     private InetAddress _localAddr;
     
 	public UdpConnector() 
 	{
 		super(SipConnectors.UDP_ORDINAL);
 	}
 	
 	protected void doStart() throws Exception 
 	{		
 		_packets = new DatagramPacket[getAcceptors()];
 		for (int i = 0; i < getAcceptors(); i++)
 		{
 			_packets[i] = new DatagramPacket(new byte[MAX_UDP_SIZE], MAX_UDP_SIZE);
 		}
 		super.doStart();
 	}
 	
 	protected void doStop() throws Exception
 	{
 		super.doStop();
 	}
 	
 	public void open() throws IOException
 	{
 		_datagramSocket = newDatagramSocket();
 		_localAddr = _datagramSocket.getLocalAddress();
 	}
 	
 
 	public boolean isOpen()
 	{
 		return _datagramSocket != null && !_datagramSocket.isClosed();
 	}
 	
 	public int getLocalPort()
 	{
 		if (_datagramSocket==null || _datagramSocket.isClosed())
             return -1;
         return _datagramSocket.getLocalPort();
 	}
 	
 	public Object getConnection()
 	{
 		return _datagramSocket;
 	}
 	
 	protected DatagramSocket newDatagramSocket() throws IOException 
 	{
 		if (getHost() == null) 
 			_datagramSocket = new DatagramSocket(getPort());
 		else 
 			_datagramSocket = new DatagramSocket(getPort(), InetAddress.getByName(getHost()));
 
 		return _datagramSocket;
 	}
 	
 	public void close() 
 	{
 		_datagramSocket.close();
 	}
 	
 	public void accept(int acceptorID) throws IOException, InterruptedException 
 	{
 		DatagramPacket p = _packets[acceptorID];
 		
 		_datagramSocket.receive(p);
 
 		int length = p.getLength();
 		if (length == 2 || length == 4) return;
 		
 		byte[] b = new byte[length];
 		System.arraycopy(p.getData(), 0, b, 0, length);
 		
 		Buffer buffer = new ByteArrayBuffer(b);
 		
 		EventHandler handler = new EventHandler();
 		SipParser parser = new SipParser(buffer, handler);
 		
 		try
 		{
 			parser.parse();
 			
 			SipMessage message = handler.getMessage();
 			message.setConnection(new UdpConnection(p.getAddress(), p.getPort()));
 			
 			/*
 			message.set5uple(getTransportOrdinal(), getAddr(), getPort(), p.getAddress(), p.getPort());
 		
 			if (message.isRequest())
 				((SipRequest) message).setEndpoint(this);
 				
 			*/
 			
 			process(message);
 		}
 		catch (Throwable t)
 		{
 			if (_statsStartedAt != -1) 
 			{
 				synchronized (_statsLock)
 				{
 					_nbParseErrors++;
 				}
 			}
 			Log.warn(t);
 			//if (handler.hasException())
 				//Log.warn(handler.getException());
         
 			if (Log.isDebugEnabled())
 				Log.debug("Buffer content: \r\n" + HexString.toDetailedHexString(buffer.array(), p.getLength()));
 		}
 		
 	}
 	
 	public SipConnection getConnection(InetAddress address, int port)
 	{
 		return new UdpConnection(address, port);
 	}
 
 	public int getDefaultPort()
 	{
 		return DEFAULT_PORT;
 	}
 	
 	public InetAddress getAddr()
 	{
 		return _localAddr;
 	}
 	
 	public int getTransportOrdinal()
 	{
 		return _type;
 	}
 
 	public boolean isReliable()
 	{
 		return RELIABLE;
 	}
 
 	public boolean isSecure()
 	{
 		return false;
 	}
 	
 	public Buffer newBuffer()
 	{
 		return new ByteArrayBuffer(MAX_UDP_SIZE);
 	}
 
 	public SipConnector getConnector()
 	{
 		return this;
 	}
 	
 	class UdpConnection implements SipConnection
 	{
 		private InetAddress _remoteAddr;
 		private int _remotePort;
 		
 		public UdpConnection(InetAddress remoteAddr, int remotePort)
 		{
 			_remoteAddr = remoteAddr;
 			_remotePort = remotePort;
 		}
 		
 		public SipConnector getConnector()
 		{
 			return UdpConnector.this;
 		}
 		
 		public InetAddress getLocalAddress()
 		{
 			return _localAddr;
 		}
 		
 		public int getLocalPort()
 		{
 			return UdpConnector.this.getPort();
 		}
 		
 		public InetAddress getRemoteAddress()
 		{
 			return _remoteAddr;
 		}
 		
 		public int getRemotePort()
 		{
 			return _remotePort;
 		}
 		
 		public void write(Buffer buffer) throws IOException
 		{
 			DatagramPacket packet = new DatagramPacket(
 					buffer.array(),
 					buffer.length(),
 					_remoteAddr,
 					_remotePort);
 			_datagramSocket.send(packet);
 		}
 		
 		public String toString()
 		{
 			return "udp/" + _remoteAddr.getHostAddress() + ":" + _remotePort; 
 		}
 	}
 	
 	class EventHandler extends SipParser.EventHandler
 	{
 		private SipMessage _message;
 		
 		@Override
 		public void startRequest(Buffer method, Buffer uri, Buffer version) throws IOException
 		{
 			try
 			{
 				URI ruri = URIFactory.parseURI(uri.toString());
 				SipRequest request = new SipRequest();
 				request.setMethod(method.toString());
 				request.setRequestURI(ruri);
 				_message = request;
 			}
 			catch (Exception e)
 			{
 				throw new IOException("Parsing error: " + e.getMessage());
 			}
 		}
 		
 		@Override
 		public void startResponse(Buffer version, int status, Buffer reason) throws IOException
 		{
 			SipResponse response = new SipResponse();
 			response.setStatus(status, reason.toString());
 			_message = response;
 		}
 		
 		public SipMessage getMessage()
 		{
 			return _message;
 		}
 		
 		@Override
 		public void header(Buffer name, Buffer value) throws IOException
 		{
 			if (_message == null)
 				throw new IOException("no status line");
 			
 			if (SipHeaders.getType(name).isList())
 	        {
 	            boolean quote = false;
 	    
 	            int start = value.getIndex();
 	            int end = value.putIndex();
 	            byte[] b = value.array();
 	            
 	            //if (value == null) value = _value.asArray();
 	            
 	            int startValue = start;
 	            int endValue = end;
 	            
 	            while (end > start && b[end -1] <= ' ') end--;
 	                    
 	            for (int i = start; i < end; i++)
 	            {
 	                int c = b[i];
 	                if (c == '"') quote = !quote;
 	    
 	                if (c == ',' && !quote)
 	                {
 	                    endValue = i;
 	                    while (endValue > start && b[endValue -1] <= ' ') endValue--;
 	                    
 	                    while (startValue < endValue && b[startValue] <= ' ') startValue++;
 	                    
 	                    //byte[] bValue = asArray(b, startValue, endValue - startValue);
 	                    
 	                    Buffer buffer = new View(value, startValue, startValue, endValue, Buffer.READONLY);
 	                    _message.getFields().addBuffer(name, buffer);
 	                    
 	                    //value = new View(value, i + 1, i + 1, end, Buffer.READONLY);
 	                    
 	                    startValue = i + 1;
 	                }
 	            }
 	            while (startValue < end && b[startValue] <= ' ') startValue++;
 	            
 	            value = new View(value, startValue, startValue, end, Buffer.READONLY);
 	            
 	            //byte[] bValue = asArray(b, startValue, end - startValue);
 	            _message.getFields().addBuffer(name, value);
 	            
 	            //value = new View(value, startValue, startValue, end, Buffer.READONLY);
 	        }
 	        else
 	        {
 	        	_message.getFields().addBuffer(name, value);
 	        	//msg.getFields().addBuffer(name, new ByteArrayBuffer(value.asArray())); 
 	        }
 		}
 		
 		@Override
 		public void content(Buffer buffer) throws IOException
 		{
 			if (buffer.length() > 0)
 				_message.setRawContent(buffer.asArray()); // TODO buffer
 		}
 	}
 
 }
