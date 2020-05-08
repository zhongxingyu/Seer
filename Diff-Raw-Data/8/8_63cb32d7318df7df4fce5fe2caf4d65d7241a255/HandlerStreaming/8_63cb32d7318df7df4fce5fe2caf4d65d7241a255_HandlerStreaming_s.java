 package org.th3falc0n.nn2.packets.handler;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.th3falc0n.nn2.Address;
 import org.th3falc0n.nn2.Port;
 import org.th3falc0n.nn2.Router;
 import org.th3falc0n.nn2.packets.Packet;
 
 public class HandlerStreaming extends PacketHandler {
 	public static PacketHandler $ = new HandlerStreaming();
 
 	public static class NNInputStream extends InputStream {	
 		Queue<Byte> buffer = new ConcurrentLinkedQueue<Byte>();
 		
 		@Override
 		public int available() throws IOException {
 			return buffer.size();
 		}
 		
 		@Override
 		public boolean markSupported() {
 			return false;
 		}
 		
 		@Override
 		public long skip(long n) throws IOException {
 			for(;n > 0;n--) {
 				read();
 			}
 			
 			return n;
 		}
 		
 		@Override
 		public int read() throws IOException {
 			while(buffer.size() < 1) { try {
 				Thread.sleep(1);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} }
 			return (int)buffer.poll();
 		}
 		
 		@Override
 		public int read(byte[] b) throws IOException {
 			int i;
 			for(i = 0; i < b.length; i++) {
 				if(buffer.size() > 0 || i < 1) {
 					if(i < 1) {
 						while(buffer.size() < 1);
 					}
 					b[i] = buffer.poll();
 				}
 				else
 				{
 					return i;
 				}
 			}
 			
 			return i;
 		}
 		
 		@Override
 		public int read(byte[] b, int off, int len) throws IOException {
 			byte[] buffer = new byte[len];
			read(buffer);
 			
			for(int i = 0; i < len; i++) {
 				b[i+off] = buffer[i];
 			}
  			
			return len;
 		}
 		
 		public void addInput(byte[] in) {
 			for(Byte b : in) {
 				buffer.add(b);
 			}
 		}
 	}
 
 	public static class NNOutputStream extends OutputStream {
 		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
 		int bfLimit = 16;
 		
 		EndPoint ep = null;
 		
 		public NNOutputStream(int bufferLimit, EndPoint endpoint) {
 			bfLimit = bufferLimit;
 			ep = endpoint;
 		}
 		
 		@Override
 		public void write(int arg0) throws IOException {
 			buffer.write((byte)arg0);	
 			checkFlush();
 		}
 		
 		@Override
 		public void write(byte[] b) throws IOException {
 			buffer.write(b);
 			checkFlush();
 		}
 		
 		@Override
 		public void write(byte[] b, int off, int len) throws IOException {
 			for(int i = 0; i < len; i++) {
 				write(b[i+off]);
 			}
 		}
 		
 		public void checkFlush() throws IOException {
 			if(buffer.size() >= bfLimit) {
 				flush();
 			}
 		}
 		
 		@Override
 		public void flush() throws IOException {
 			super.flush();
 			
 			byte[] send = buffer.toByteArray();
 			buffer.reset();
 			
 			Router.$Instance.routePacket(getFlushPacket(ep.addr, send, ep.portID));
 		}
 	}
 	
 	public static class NNSocket {
 		NNOutputStream out;
 		NNInputStream in;
 		
 		public NNSocket(int outBufferLimit, int portID, Address dest) {
 			out = new NNOutputStream(outBufferLimit, new EndPoint(dest, portID));
 			in = new NNInputStream();
 			
 			HandlerStreaming.streams.put(new EndPoint(dest, portID), this);
 		}
 		
 		public OutputStream getOutputStream() {
 			return out;
 		}
 		
 		public InputStream getInputStream()  {
 			return in;
 		}
 	}
 	
 	public static class NNServerSocket {
 		Queue<NNSocket> waitingSockets = new ConcurrentLinkedQueue<NNSocket>();
 		
 		public NNServerSocket(int portID) {
 			servers.put(portID, this);
 		}
 		
 		public NNSocket accept() {
 			while(waitingSockets.size() < 1) { try {
 				Thread.sleep(1);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} }
 			return waitingSockets.poll();
 		}
 	}
 	
 	public static class EndPoint {
 		Address addr;
 		int portID;
 		
 		public EndPoint(Address addr, int portID) {
 			this.addr = addr;
 			this.portID = portID;
 		}
 		
 		@Override
 		public boolean equals(Object obj) {
 			if(!(obj instanceof EndPoint)) return false;
 			EndPoint cp = (EndPoint)obj;
 			return addr.toString().equals(cp.addr.toString()) && portID == cp.portID;
 		}
 		
 		@Override
 		public int hashCode() {
 			return addr.toString().hashCode() + ((Integer)portID).hashCode();
 		}
 	}
 	
 	private static  Map<EndPoint, NNSocket> streams = new ConcurrentHashMap<EndPoint, NNSocket>();
 	private static  Map<Integer, NNServerSocket> servers = new ConcurrentHashMap<Integer, NNServerSocket>();
 
 	public static Packet getFlushPacket(Address to, byte[] data, int port) {
 		Packet p = $.getEmptyPacketWithID(to);
 
 		ByteBuffer buffer = ByteBuffer.allocate(data.length + 5);
 		
 		buffer.put((byte)0);
 		buffer.putInt(port);
 		buffer.put(data);
 		
 		p.setData(buffer.array());
 		
 		return p;
 	}
 	
 	public static Packet getConnectionRefusedPacket(Address to, int port) {
 		Packet p = $.getEmptyPacketWithID(to);
 
 		ByteBuffer buffer = ByteBuffer.allocate(5);
 		
 		buffer.put((byte)1);
 		buffer.putInt(port);
 		
 		p.setData(buffer.array());
 		
 		return p;
 	}
 	
 	@Override
 	public void handlePacket(Packet packet, Port port) {
 		ByteBuffer data = ByteBuffer.wrap(packet.getData());
 		
 		byte subID = data.get();
 		int portID = data.getInt();
 		
 		if(subID == 0) {
 			byte[] buffer = new byte[data.remaining()];
 			if(data.remaining() > 0) {
 				data.get(buffer);			
 			}
 			
 			EndPoint pep = new EndPoint(packet.getSource(), portID);
 			
 			if(streams.containsKey(pep)) {
 				streams.get(pep).in.addInput(buffer);
 			}
 			else if(servers.containsKey(portID)) {
 				servers.get(portID).waitingSockets.add(new NNSocket(16, portID, pep.addr));
 				streams.get(pep).in.addInput(buffer);
 			}
 			else
 			{
 				Router.$Instance.routePacket(getConnectionRefusedPacket(packet.getSource(), portID));
 			}
 		}
 		
 		if(subID == 1) {
 			port.log("Node at " + packet.getSource() + " refused to open stream on PID " + portID);
 		}
 	}
 
 }
