 /*
  *  hbIRCS
  *  
  *  Copyright 2005 Boris HUISGEN <bhuisgen@hbis.fr>
  * 
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Library General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 
 package fr.hbis.ircs.nio;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.SocketChannel;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.Client;
 import fr.hbis.ircs.IRC;
 import fr.hbis.ircs.Utilities;
 import fr.hbis.ircs.lib.nio.Algorithm;
 import fr.hbis.ircs.lib.nio.WorkerThread;
 import fr.hbis.ircs.lib.nio.task.TaskBase;
 
 
 /**
  * The class <code>MessageTask</code> is the implementation of a
  * <code>Task</code> object for the <code>SubReactor</code> class.
  * 
  * @author Boris HUISGEN
  */
 public class MessageTask extends TaskBase
 {
 	/**
 	 * Constructs a <code>MessageTask</code> object.
 	 */
 	private MessageTask ()
 	{
 		m_subReactor = null;
 		m_algorithm = null;
 		m_byteBuffer = null;
 		m_bUseDirectByteBuffer = false;
 		m_bUseByteBufferView = false;
 	}
 
 	/**
 	 * Creates a new <code>MessageTask</code> object.
 	 * 
 	 * @param subReactor
 	 *            the reactor of the task.
 	 * 
 	 * @return the <code>MessageTask</code> object of the task.
 	 */
 	public static final MessageTask create (SubReactor subReactor)
 	{
 		if (subReactor == null)
 			throw new IllegalArgumentException ("invalid reactor");
 
 		MessageTask messageTask = new MessageTask ();
 
 		messageTask.m_subReactor = subReactor;
 		messageTask.m_bUseDirectByteBuffer = false;
 		messageTask.m_bUseByteBufferView = false;
 
 		return (messageTask);
 	}
 
 	/**
 	 * Initializes the task.
 	 * 
 	 * @param algorithm
 	 *            the algorithm to determine if readed data are complete.
 	 * @param useDirectByteBuffer
 	 *            flag to use direct byte buffer.
 	 * @param useByteBufferView
 	 *            flag to use byte buffer view.
 	 */
 	public void init (Algorithm algorithm, boolean useDirectByteBuffer,
 			boolean useByteBufferView)
 	{
 		this.m_algorithm = algorithm;
 		this.m_bUseDirectByteBuffer = useDirectByteBuffer;
 		this.m_bUseByteBufferView = useByteBufferView;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.client.lib.nio.Task#doTask()
 	 */
 	public void doTask ()
 	{
 		SelectionKey selectionKey = getSelectionKey ();
 
 		if (selectionKey == null)
 		{
 			m_logger.log (Level.WARNING, "invalid selection key");
 
 			terminate ();
 
 			return;
 		}
 
 		SocketChannel socketChannel = (SocketChannel) selectionKey.channel ();
 
 		if (socketChannel == null)
 		{
 			m_logger.log (Level.WARNING, "invalid socket channel");
 
 			terminate ();
 
 			return;
 		}
 
 		if (m_byteBuffer == null)
 		{
 			final WorkerThread workerThread = (WorkerThread) Thread
 					.currentThread ();
 
 			m_byteBuffer = workerThread.getByteBuffer ();
 
 			if (m_byteBuffer == null)
 			{
 				m_byteBuffer = m_algorithm.allocate (m_bUseDirectByteBuffer,
 						m_bUseByteBufferView);
 				workerThread.setByteBuffer (m_byteBuffer);
 			}
 		}
 
 		if (m_byteBuffer == null)
 		{
 			m_logger.log (Level.WARNING, "invalid byte buffer");
 
 			terminate ();
 
 			return;
 		}
 
 		doRead (selectionKey, socketChannel, m_byteBuffer);
 
 		terminate ();
 	}
 
 	/**
 	 * Read the data of the client.
 	 * 
 	 * @param selectionKey
 	 *            the <code>SelectionKey</code> of the task.
 	 * @param socketChannel
 	 *            the <code>SocketChannel</code> of the task.
 	 * @param m_byteBuffer
 	 *            the <code>ByteBuffer</code> of the task.
 	 */
 	private void doRead (SelectionKey selectionKey,
 			SocketChannel socketChannel, ByteBuffer byteBuffer)
 	{
 		try
 		{
 			int count;
 
 			while (true)
 			{
 				if (!socketChannel.isOpen ()
 						|| ((count = socketChannel.read (byteBuffer)) < 0))
 					throw new EOFException (IRC.ERRMSG_READERROR);
 
 				if (count == 0)
 				{
 					m_logger.log (Level.FINE, "no bytes to read");
 
 					break;
 				}
 
 				m_logger.log (Level.FINE, "bytes readed");
 
 				if (m_algorithm.parse (byteBuffer))
 				{
 					m_logger.log (Level.FINE,
 							"read request complete, process client message");
 
 					doProcess (socketChannel, byteBuffer);
 				}
 			}
						
			getReactor ().register (socketChannel);
			m_logger.log (Level.FINE,
					"socket channel registered to subreactor");
 		}
 		catch (IOException ioException)
 		{
 			m_logger.log (Level.WARNING, "error during reading: "
 					+ ioException.getMessage ());
 
 			Client client = m_subReactor.getMainReactor ().getClient (
 					socketChannel);
 			if (client != null)
 			{
 				m_subReactor.getManager ().disconnectClient (client,
 						ioException.getMessage ());
 			}
 		}
 	}
 
 	/**
 	 * Process the data of the client.
 	 * 
 	 * @param socketChannel
 	 *            the <code>SocketChannel</code> of the task.
 	 * @param m_byteBuffer
 	 *            the <code>ByteBuffer</code> of the task.
 	 */
 	private void doProcess (SocketChannel socketChannel, ByteBuffer byteBuffer)
 	{
 		Client client = getReactor ().getMainReactor ().getClient (
 				socketChannel);
 		if (client == null)
 		{
 			m_logger.log (Level.WARNING, "invalid client");
 
 			terminate ();
 
 			return;
 		}
 
 		String buffer = Utilities.decodeMessage (byteBuffer,
 				fr.hbis.ircs.Constants.SERVER_CHARSET);
 		if (buffer == null)
 		{
 			m_logger
 					.log (
 							Level.WARNING,
 							"failed to decode buffer with charset "
 									+ fr.hbis.ircs.Constants.SERVER_CHARSET);
 
 			return;
 		}
 
 		String[] messages = IRC.BUFFER_MESSAGE.split (buffer);
 
 		for (String message : messages)
 			client.read (message);
 
 		m_logger.log (Level.FINE, "client message processed");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.client.lib.nio.task.TaskBase#recycle()
 	 */
 	public void recycle ()
 	{
 		if (m_byteBuffer != null)
 		{
 			final WorkerThread workerThread = (WorkerThread) Thread
 					.currentThread ();
 
 			m_byteBuffer.clear ();
 
 			if (workerThread.getByteBuffer () == null)
 				workerThread.setByteBuffer (m_byteBuffer);
 		}
 
 		m_algorithm.recycle ();
 
 		setSelectionKey (null);
 		m_byteBuffer = null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.task.TaskBase#getReactor()
 	 */
 	public SubReactor getReactor ()
 	{
 		return (m_subReactor);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.Task#getByteBuffer()
 	 */
 	public ByteBuffer getByteBuffer ()
 	{
 		return (m_byteBuffer);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * fr.hbis.ircs.lib.nio.Task#setByteBuffer(java.nio.ByteBuffer)
 	 */
 	public void setByteBuffer (ByteBuffer byteBuffer)
 	{
 		m_byteBuffer = byteBuffer;
 	}
 
 	private SubReactor m_subReactor;
 	private Algorithm m_algorithm;
 	private ByteBuffer m_byteBuffer;
 	private boolean m_bUseDirectByteBuffer;
 	private boolean m_bUseByteBufferView;
 	private static Logger m_logger = Logger
 			.getLogger ("fr.hbis.ircs.nio.MessageTask");
 }
