 /*
 TOD - Trace Oriented Debugger.
 Copyright (c) 2006-2008, Guillaume Pothier
 All rights reserved.
 
 This program is free software; you can redistribute it and/or 
 modify it under the terms of the GNU General Public License 
 version 2 as published by the Free Software Foundation.
 
 This program is distributed in the hope that it will be useful, 
 but WITHOUT ANY WARRANTY; without even the implied warranty of 
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 General Public License for more details.
 
 You should have received a copy of the GNU General Public License 
 along with this program; if not, write to the Free Software 
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 MA 02111-1307 USA
 
 Parts of this work rely on the MD5 algorithm "derived from the 
 RSA Data Security, Inc. MD5 Message-Digest Algorithm".
 */
 package java.tod.transport;
 
 import java.lang.ref.WeakReference;
 import java.nio.channels.ByteChannel;
 import java.tod.AgentReady;
 import java.tod.TOD;
 import java.tod.ThreadData;
 import java.tod.io._EOFException;
 import java.tod.io._IO;
 import java.tod.io._IOException;
 import java.tod.io._SocketChannel;
 import java.tod.util._ArrayList;
 import java.tod.util._StringBuilder;
 import java.tod.util._SyncRingBuffer;
 
 import tod2.access.TODAccessor;
 import tod2.agent.AgentDebugFlags;
 import tod2.agent.Command;
 import tod2.agent.Message;
 import tod2.agent.io._ByteBuffer;
 
 /**
  * This class implements the thread that communicates with the TOD database.
  * - Sends the packets buffered by a set of {@link PacketBuffer} to a given
  * {@link ByteChannel}.
  * Packets of a given thread are sent together, prefixed by a header that indicates the 
  * size of the "meta-packet", the corresponding thread id, and whether the meta-packet contains
  * split packets.
  * - Receives commands from the database.
  * 
  * Because we are using native iostream calls to perform IO, we can't have read operations and 
  * write operations in different threads. 
  * @author gpothier
  */
 public class IOThread extends Thread
 {
 	private static final boolean PRINT_STATS_ON_SHUTDOWN = false;
 	
 	private _SocketChannel itsChannel;
 	
 	/**
 	 * Used to send the header of each packet.
 	 */
 	private final _ByteBuffer itsHeaderBuffer;
 	
 	/**
 	 * Packets that are waiting to be sent.
 	 */
 	private final _SyncRingBuffer<Packet> itsPendingPackets = new _SyncRingBuffer<Packet>(1000);
 	
 	private final _ArrayList<ThreadPacket>[] itsFreeThreadPackets;
 	
 	/**
 	 * A set of {@link ThreadData} objects registered with this {@link IOThread}.
 	 * They are periodically requested to write all pending data.
 	 */
 	private final _ArrayList<WeakReference<ThreadData>> itsThreadDatas = new _ArrayList<WeakReference<ThreadData>>();
 	
 	
 	private final MyShutdownHook itsShutdownHook;
 	
 	private volatile boolean itsShutdownStarted = false;
 	
 	private long itsBytesSent = 0;
 	private long itsPacketsSent = 0;
 	
 	/**
 	 *  Time at which last stale buffer check was performed
 	 */
 	private long itsCheckTime;
 	
 	/**
 	 * Last observed value of the Capture Enabled flag
 	 */
 	private boolean itsLastEnabled; 
 	
 	/**
 	 *  Number of buffers that were sent since last timestamp was taken (taking timestamps is costly)
 	 */
 	private int itsSentPackets;
 	
 	/**
 	 * A buffer for the command reader
 	 */
 	private _ByteBuffer its1b = _ByteBuffer.allocate(1);
 
 
 
 	public IOThread()
 	{
 		super("[TOD] IOThread");
 		setDaemon(true);
 		
 		itsHeaderBuffer = _ByteBuffer.allocate(256);
 		
 		itsShutdownHook = new MyShutdownHook();
 		Runtime.getRuntime().addShutdownHook(itsShutdownHook);
 		
 		itsFreeThreadPackets = new _ArrayList[ThreadPacket.RECYCLE_QUEUE_COUNT];
 		for(int i=0;i<itsFreeThreadPackets.length;i++) itsFreeThreadPackets[i] = new _ArrayList<ThreadPacket>();
 	}
 	
 	public void setChannel(_SocketChannel aChannel)
 	{
 		assert aChannel != null;
 		if (itsChannel != null) throw new RuntimeException("Already have the channel.");
 		itsChannel = aChannel;
 		start();
 	}
 	
 	public void printStats()
 	{
 		if (! AgentDebugFlags.COLLECT_PROFILE) return;
 		_StringBuilder b = new _StringBuilder();
 		
 		b.append("[IOThread] Bytes sent: ");
 		b.append(itsBytesSent);
 		b.append(" - packets: ");
 		b.append(itsPacketsSent);
 		b.append("\n");
 
 		_IO.out(b.toString());
 	}
 	
 	public void registerThreadData(ThreadData aThreadData)
 	{
 		itsThreadDatas.add(new WeakReference<ThreadData>(aThreadData));
 	}
 	
 	public boolean hasShutdownStarted()
 	{
 		return itsShutdownStarted;
 	}
 	
 	/**
 	 * Assertions don't seem to work in bootclasspath code...
 	 */
 	public static void _assert(boolean aValue)
 	{
 		if (! aValue) throw new AssertionError();
 	}
 
 	@Override
 	public void run()
 	{
 		try
 		{
 			itsCheckTime = System.currentTimeMillis();
 			itsLastEnabled = ! AgentReady.CAPTURE_ENABLED; 
 			itsSentPackets = 0;
 			
			while(!hasShutdownStarted())
 			{
 //				_IO.out("PacketBufferSender.run() - sentBuffers: "+sentBuffers);
 				Packet thePacket = popPacket();
 				
 				if (thePacket != null)
 				{
 					itsSentPackets++;
 					thePacket.send(this);
 				}
 				
 				if (thePacket == null || itsSentPackets > 100)
 				{
 					// Check stale buffers at a regular interval
 					checkStaleBuffers();
 					itsSentPackets = 0;
 				}
 				
 				readCommands();
 			}
 		}
 		catch (Exception e)
 		{
 			if (itsShutdownStarted && (e instanceof _IOException))
 			{
 				// Broken pipe, just exit
 			}
 			else
 			{
 				_IO.err("[TOD] FATAL:");
 				e.printStackTrace();
 				Runtime.getRuntime().removeShutdownHook(itsShutdownHook);
 				System.exit(1);
 			}
 		}
 	}
 	
 	private void sendThreadPacket(ThreadPacket aPacket) throws _IOException
 	{
 		itsHeaderBuffer.clear();
 		itsHeaderBuffer.put(Message.PACKET_TYPE_THREAD);
 		itsHeaderBuffer.putInt(aPacket.threadId);
 		itsHeaderBuffer.putInt(aPacket.length);
 		
 		itsHeaderBuffer.flip();
 		itsChannel.write(itsHeaderBuffer);
 		
 		itsChannel.writeAll(aPacket.data, aPacket.offset, aPacket.length);
 		
 		if (AgentDebugFlags.COLLECT_PROFILE) 
 		{
 			itsBytesSent += 9+aPacket.length;
 			itsPacketsSent++;
 		}
 		
 		synchronized (itsFreeThreadPackets)
 		{
 			itsFreeThreadPackets[aPacket.recycleQueue].add(aPacket);
 		}
 	}
 	
 	public ThreadPacket getFreeThreadPacket(int aRecycleQueue)
 	{
 		if (itsFreeThreadPackets[aRecycleQueue].isEmpty()) return null;
 		synchronized (itsFreeThreadPackets)
 		{
 			return itsFreeThreadPackets[aRecycleQueue].removeLast();
 		}
 	}
 	
 	private void sendStringPacket(StringPacket aPacket) throws _IOException
 	{
 		itsHeaderBuffer.clear();
 		itsHeaderBuffer.put(Message.PACKET_TYPE_STRING);
 		
 		itsHeaderBuffer.flip();
 		itsChannel.write(itsHeaderBuffer);
 		
 		itsChannel.writeStringPacket(aPacket.id, aPacket.string);
 
 		if (AgentDebugFlags.COLLECT_PROFILE) 
 		{
 			itsBytesSent += 1+8+4+TODAccessor.getStringCount(aPacket.string)*2;
 			itsPacketsSent++;
 		}
 	}
 	
 	private void sendModeChangesPacket(ModeChangesPacket aPacket) throws _IOException
 	{
 		itsHeaderBuffer.clear();
 		itsHeaderBuffer.put(Message.PACKET_TYPE_MODECHANGES);
 		itsHeaderBuffer.putInt(aPacket.data.length);
 
 		itsHeaderBuffer.flip();
 		itsChannel.write(itsHeaderBuffer);
 		
 		itsChannel.writeAll(aPacket.data, 0, aPacket.data.length);
 		
 		if (AgentDebugFlags.COLLECT_PROFILE) 
 		{
 			itsBytesSent += 5+aPacket.data.length;
 			itsPacketsSent++;
 		}
 	}
 	
 	private void checkStaleBuffers()
 	{
 //		long t = System.currentTimeMillis();
 //		long delta = t - itsCheckTime;
 //		
 //		if (delta > 100)
 //		{
 //			itsCheckTime = t;
 //			
 //			PacketBuffer[] theArray;
 //			synchronized(this)
 //			{
 //				// Might deadlock if we synchronize the pleaseSwaps, so copy the list
 //				theArray = itsBuffers.toArray(new PacketBuffer[itsBuffers.size()]);
 //			}
 //			
 //			for (PacketBuffer theBuffer : theArray) theBuffer.pleaseSwap();
 //			
 //			// Notify of capture enabled state
 //			if (itsLastEnabled != AgentReady.CAPTURE_ENABLED)
 //			{
 //				itsEventCollector.evCaptureEnabled(AgentReady.CAPTURE_ENABLED);
 //				itsLastEnabled = AgentReady.CAPTURE_ENABLED;
 //			}
 //		}
 	}
 	
 	private byte readByte() throws _IOException
 	{
 		its1b.clear();
 		int theCount = itsChannel.read(its1b);
 		if (theCount == -1) throw new _EOFException();
 		return its1b.get();
 	}
 	
 	private boolean readBoolean() throws _IOException
 	{
 		return readByte() != 0;
 	}
 
 	private int readInt() throws _IOException
 	{
 		byte b0 = readByte();
 		byte b1 = readByte();
 		byte b2 = readByte();
 		byte b3 = readByte();
 		
 		return _ByteBuffer.makeInt(b3, b2, b1, b0);
 	}
 
 	
 	private void readCommands() throws _IOException
 	{
 		while(itsChannel.hasInput())
 		{
 			byte theMessage = readByte();
 			if (theMessage >= Command.BASE)
 			{
 				switch (theMessage)
 				{
 				case Command.AGCMD_ENABLECAPTURE:
 					processEnableCapture();
 					break;
 					
 				default: throw new RuntimeException("Not handled: "+theMessage); 
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Processes the {@link Command#AGCMD_ENABLECAPTURE} command.
 	 */
 	private void processEnableCapture() throws _IOException
 	{
 		boolean theEnable = readBoolean();
 		if (theEnable) 
 		{
 			_IO.out("[TOD] Enable capture request received.");
 			TOD.enableCapture();
 		}
 		else 
 		{
 			_IO.out("[TOD] Disable capture request received.");
 			TOD.disableCapture();
 		}
 	}
 
 	/**
 	 * Pushes the given packet buffer to the pending queue.
 	 * @param aRecycle If true, the buffer in the given {@link ThreadPacket}
 	 * will be recycled and a free recycled buffer will be returned if available 
 	 */
 	public void pushPacket(Packet aPacket) 
 	{
		if (hasShutdownStarted()) return;
 		itsPendingPackets.add(aPacket);
 	}
 	
 	/**
 	 * Pops a buffer from the stack, waiting for up to 100ms if none is available.
 	 */
 	private Packet popPacket() throws InterruptedException
 	{
 		if (itsPendingPackets.isEmpty()) Thread.sleep(100);
 		return itsPendingPackets.poll();
 	}
 	
 	/**
 	 * Base class for the packets that can be sent by this {@link IOThread}.
 	 * @author gpothier
 	 */
 	public abstract static class Packet
 	{
 		protected abstract void send(IOThread aIOThread) throws _IOException;
 	}
 	
 	/**
 	 * A data packet that was created by a thread.
 	 * The range of valid data is indicated by {@link #offset}
 	 * and {@link #length}. By default, the whole data is valid,
 	 * but sometimes the {@link IOThread} can request {@link ThreadData} to
 	 * send all available data. In that case, partial packets are sent. 
 	 * @author gpothier
 	 */
 	public static final class ThreadPacket extends Packet
 	{
 		/**
 		 * Recycle queue for standard packets (all have the same length).
 		 */
 		public static final int RECYCLE_QUEUE_STANDARD = 0;
 		
 		/**
 		 * Recycle queue for other packets.
 		 */
 		public static final int RECYCLE_QUEUE_OTHER = 1;
 		public static final int RECYCLE_QUEUE_COUNT = 2;
 		
 		public int threadId;
 		public byte[] data;
 		
 		/**
 		 * When the packet has been sent, it will be placed on this recycle queue.
 		 */
 		public int recycleQueue;
 		
 		public int offset;
 		public int length;
 		
 		
 		public void set(int aThreadId, byte[] aData, int aRecycleQueue, int aOffset, int aLength)
 		{
 			threadId = aThreadId;
 			data = aData;
 			recycleQueue = aRecycleQueue;
 			offset = aOffset;
 			length = aLength;
 		}
 
 		public void set(int aThreadId, byte[] aData, int aRecycleQueue)
 		{
 			set(aThreadId, aData, aRecycleQueue, 0, aData.length);
 		}
 
 		@Override
 		protected void send(IOThread aIOThread) throws _IOException
 		{
 			aIOThread.sendThreadPacket(this);
 		}
 	}
 	
 	/**
 	 * A packet that represents a {@link String} whose value must be sent.
 	 * @author gpothier
 	 */
 	public static final class StringPacket extends Packet
 	{
 		public final long id;
 		public final String string;
 		
 		public StringPacket(long aId, String aString)
 		{
 			id = aId;
 			string = aString;
 		}
 
 		@Override
 		protected void send(IOThread aIOThread) throws _IOException
 		{
 			aIOThread.sendStringPacket(this);
 		}
 	}
 	
 	public static final class ModeChangesPacket extends Packet
 	{
 		public final byte[] data;
 
 		public ModeChangesPacket(byte[] aData)
 		{
 			data = aData;
 		}
 
 		@Override
 		protected void send(IOThread aIOThread) throws _IOException
 		{
 			aIOThread.sendModeChangesPacket(this);
 		}
 	}
 
 	
 	private class MyShutdownHook extends Thread
 	{
 		public MyShutdownHook() 
 		{
 			super("Shutdown hook (SocketCollector)");
 		}
 
 		@Override
 		public void run()
 		{
 			_IO.out("[TOD] Shutting down...");
 			for(int i=0;i<itsThreadDatas.size();i++)
 			{
 				WeakReference<ThreadData> theRef = itsThreadDatas.get(i); 
 				ThreadData theThreadData = theRef.get();
 				if (theThreadData == null) continue;
 				
 				theThreadData.flushBuffer(); // TODO: see how to properly synchronize this.
 				if (PRINT_STATS_ON_SHUTDOWN) theThreadData.printStats();
 			}
 			
 			if (PRINT_STATS_ON_SHUTDOWN) printStats();
 			
 			_IO.out("[TOD] Flushing buffers...");
 			try
 			{
 				while(! itsPendingPackets.isEmpty()) Thread.sleep(100);
				itsShutdownStarted = true;
				Thread.sleep(100); // False synchronization...
 				itsChannel.close();
 			}
 			catch (_IOException e)
 			{
 				e.printStackTrace();
 			}
 			catch (InterruptedException e)
 			{
 				e.printStackTrace();
 			}
 			
 			_IO.out("[TOD] Goodbye.");
 			
 //			
 //			EventCollector.INSTANCE.end();
 //			
 //			for (int i=0;i<itsBuffers.size();i++) itsBuffers.get(i).swapBuffers();
 //			
 //			try
 //			{
 //				int thePrevSize = itsPendingBuffers.size();
 //				while(thePrevSize > 0)
 //				{
 //					Thread.sleep(200);
 //					int theNewSize = itsPendingBuffers.size();
 //					if (theNewSize == thePrevSize)
 //					{
 //						_IO.err("[TOD] Buffers are not being sent, shutting down anyway ("+theNewSize+" buffers remaining).");
 //						break;
 //					}
 //					thePrevSize = theNewSize;
 //				}
 //				
 //				// Give some more time to allow for the buffers to be sent
 //				Thread.sleep(3000);
 //				
 //				itsChannel.close();
 //			}
 //			catch (Exception e)
 //			{
 //				throw new RuntimeException(e);
 //			}
 //			
 //			_IO.out("[TOD] Shutting down.");
 		}
 	}
 	
 
 }
