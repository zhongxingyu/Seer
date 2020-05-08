 /*
  *  NIO Framework
  *  
  *  Copyright 2009 Boris HUISGEN <bhuisgen@hbis.fr>
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
 
 package fr.hbis.ircs.lib.nio.worker;
 
 import java.nio.ByteBuffer;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import fr.hbis.ircs.lib.nio.Pipeline;
 import fr.hbis.ircs.lib.nio.Task;
 import fr.hbis.ircs.lib.nio.WorkerThread;
 
 /**
 * The class <code>DefaultWorkerThread</code> is the default implementation of a
 * worker thread.
  * 
  * @author bhuisgen
  */
 public class DefaultWorkerThread extends Thread implements WorkerThread
 {
 	/**
 	 * Constructs a new <code>DefaultWorkerThread</code> object.
 	 * 
 	 * @param runnable
 	 *            the <code>Runnable</code> object of the worker thread.
 	 * @param name
 	 *            the name of the worker thread.
 	 */
 	public DefaultWorkerThread (Runnable runnable, String name)
 	{
 		super (threadGroup, runnable, name);
 
 		this.runnable = runnable;
 		this.pipeline = null;
 		this.byteBuffer = null;
 		this.running = false;
 
 		setDaemon (true);
 
 		logger.log (Level.FINE, "new worker thread constructed");
 	}
 
 	/**
 	 * Constructs a new <code>DefaultWorkerThread</code> object.
 	 * 
 	 * @param pipeline
 	 *            the pipeline of the worker thread.
 	 * @param name
 	 *            the name of the worker thread.
 	 */
 	public DefaultWorkerThread (Pipeline pipeline, String name)
 	{
 		super (threadGroup, name);
 
 		if (pipeline == null)
 			throw new IllegalArgumentException ("invalid pipeline");
 
 		this.runnable = null;
 		this.pipeline = pipeline;
 		this.byteBuffer = null;
 		this.running = false;
 
 		setDaemon (true);
 
 		logger.log (Level.FINE, "new worker thread constructed");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.WorkerThread#init()
 	 */
 	public void init ()
 	{
 		logger.log (Level.FINE, "initializing worker thread");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#start()
 	 */
 	public void start ()
 	{
 		logger.log (Level.FINE, "notify worker thread to start");
 
 		super.start ();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Thread#run()
 	 */
 	public void run ()
 	{
 		logger.log (Level.FINE, "starting worker thread");
 
 		running = true;
 
 		if (runnable != null)
 		{
 			logger.log (Level.FINE, "executing runnable");
 
 			runnable.run ();
 		}
 		else
 		{
 			while (running)
 			{
 				logger.log (Level.FINE, "popping task");
 
 				Task task = pipeline.popTask ();
 				if (task == null)
 					break;
 
 				logger.log (Level.FINE, "executing task");
 
 				try
 				{
 					task.doTask ();
 				}
 				catch (Exception exception)
 				{
 					if (byteBuffer != null)
 						byteBuffer.clear ();
 
 					StringBuilder stackTrace = new StringBuilder (
 							"exception during execution: "
 									+ exception.getMessage ());
 
 					for (StackTraceElement element : exception.getStackTrace ())
 					{
 						stackTrace.append ("\n\tat " + element.getClassName ()
 								+ ":" + element.getMethodName () + "("
 								+ element.getFileName () + ":"
 								+ element.getLineNumber () + ")");
 					}
 
 					logger.log (Level.SEVERE, stackTrace.toString ());
 				}
 			}
 		}
 
 		logger.log (Level.FINE, "ending worker thread");
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.WorkerThread#terminate()
 	 */
 	public void terminate ()
 	{
 		logger.log (Level.FINE, "notify worker thread to terminate");
 
 		running = false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.client.lib.nio.WorkerThread#isStarted()
 	 */
 	public boolean isStarted ()
 	{
 		return running;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.WorkerThread#getByteBuffer()
 	 */
 	public final ByteBuffer getByteBuffer ()
 	{
 		return byteBuffer;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see fr.hbis.ircs.lib.nio.WorkerThread#setByteBuffer(java.nio.ByteBuffer)
 	 */
 	public final void setByteBuffer (ByteBuffer byteBuffer)
 	{
 		this.byteBuffer = byteBuffer;
 	}
 
 	private final static ThreadGroup threadGroup = new ThreadGroup (
 			"WorkerThread");
 	private Runnable runnable;
 	private Pipeline pipeline;
 	private ByteBuffer byteBuffer;
 	private boolean running;
 	private static Logger logger = Logger
 			.getLogger ("fr.hbis.ircs.lib.nio.worker.DefaultWorkerThread");
 }
