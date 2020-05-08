 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.osgi.service.log.LogService;
 
 /**
  * A class for sharing an InputStream among multiple clients.
  * 
  * @author Angel Roman
  * 
  */
 public class StreamMultiplexer extends Thread {
 
 	private ArrayList outputStreamWriters = new ArrayList();
 	private InputStream is;
 	private int bufferLength = 1;
 	private ArrayList listeners = new ArrayList();
 	private int delay = 0;
 	private long read_delay = 0;
 	private LogService log;
 
 	/**
 	 * 
 	 * @param is
 	 *            InputStream to use as source
 	 * @param bufferLength
 	 *            the number of bytes to attempt reading simultaneously from the
 	 *            Input Stream.
 	 * @param delay
 	 *            how long to wait in milliseconds until we check if anyone has
 	 *            requested an inputstream.
 	 */
 	public StreamMultiplexer(InputStream is, int bufferLength, int delay) {
 		this.is = is;
 		this.bufferLength = bufferLength;
 		this.delay = delay;
 	}
 
 	/**
 	 * 
 	 * @param is
 	 *            InputStream to use as source
 	 * @param bufferLength
 	 *            the number of bytes to attempt reading simultaneously from the
 	 *            Input Stream.
 	 * @param delay
 	 *            how long to wait in milliseconds until we check if anyone has
 	 *            requested an inputstream.
 	 * @param read_delay
 	 *            the amount of time to wait between empty reads.
 	 */
 	public StreamMultiplexer(InputStream is, int bufferLength, int delay, long read_delay) {
 		this.is = is;
 		this.bufferLength = bufferLength;
 		this.delay = delay;
 		this.read_delay = read_delay;
 	}
 
 	/**
 	 * @param is
 	 *            The input stream to multiplex.
 	 * @param bufferLength
 	 *            The amounts of bytes to read simultaneously.
 	 */
 	public StreamMultiplexer(InputStream is, int bufferLength) {
 		this.is = is;
 		this.bufferLength = bufferLength;
 	}
 
 	public StreamMultiplexer(InputStream is) {
 		this.is = is;
 	}
 
 	public void setLogService(LogService logService) {
 		this.log = logService;
 	}
 
 	private void notifyStreamMultiplexerListeners(StreamMultiplexerEvent event) {
 		synchronized (listeners) {
 			Iterator iter = listeners.iterator();
 			while (iter.hasNext()) {
 				IStreamMultiplexerListener listener = (IStreamMultiplexerListener) iter.next();
 				listener.streamNotification(event);
 			}
 		}
 	}
 
 	public void run() {
 		int read = 0;
 		byte[] buff = new byte[bufferLength];
 
 		String name = getName();
 
 		try {
 			ArrayList faultyStreams = new ArrayList();
 			if (log != null) {
 				log.log(LogService.LOG_DEBUG, name + ": Started");
 			}
 			while (!isInterrupted()) {
 
 				if (outputStreamWriters.size() > 0) {
 					read = is.read(buff);
 					if (read == -1) {
 						if (read_delay != 0) {
 							sleep(read_delay);
 						}
 						continue;
 					}
 
 					synchronized (outputStreamWriters) {
 						Iterator iter = outputStreamWriters.iterator();
 						while (iter.hasNext() && read != -1) {
 							PipedOutputStream osw = (PipedOutputStream) iter.next();
 							try {
 								osw.write(buff, 0, read);
 							} catch (IOException e) {
 								osw.close();
 								faultyStreams.add(osw);
 							}
 						}
						removeStreams(faultyStreams);
 					}
 				}
 
 				try {
 					if ((delay > 0) && (outputStreamWriters.size() == 0)) {
 						sleep(delay);
 					}
 				} catch (InterruptedException e) {
 					// this is not an error.
 				}
 			}
 
 		} catch (IOException e1) {
 			if (log != null) {
 				log.log(LogService.LOG_WARNING, "An IOException was generated", e1);
 			}
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			// If we are going down, close all streams.
 			try {
 				Iterator iter = outputStreamWriters.iterator();
 				while (iter.hasNext()) {
 					PipedOutputStream pos = (PipedOutputStream) iter.next();
 					try {
 						pos.close();
 					} catch (IOException e) {
 						// Log This
 						e.printStackTrace();
 					}
 				}
 
 				// Close the gps stream
 				if (is != null)
 					is.close();
 			} catch (IOException e) {
 				if (log != null) {
 					log.log(LogService.LOG_WARNING, "An IOException was generated", e);
 				}
 			}
 		}
 
 		if (log != null) {
 			log.log(LogService.LOG_DEBUG, name + ": Goodbye!");
 		}
 	}
 
 	public void register(IStreamMultiplexerListener listener) {
 		synchronized (listeners) {
 			listeners.add(listener);
 		}
 	}
 
 	public void unregister(IStreamMultiplexerListener listener) {
 		synchronized (listeners) {
 			listeners.remove(listener);
 		}
 	}
 
 	private void removeStreams(ArrayList faultyStreams) {
 
 		if (faultyStreams.size() > 0) {
 			// Remove faulty streams
 			Iterator iter = faultyStreams.iterator();
 			while (iter.hasNext()) {
 				outputStreamWriters.remove(iter.next());
 			}
 
 			notifyStreamMultiplexerListeners(new StreamMultiplexerEvent(StreamMultiplexerEvent.EVENT_STREAM_REMOVED, outputStreamWriters.size()));
 		}
 	}
 
 	public InputStream getInputStream() {
 		PipedOutputStream pos = new PipedOutputStream();
 		PipedInputStream pis = null;
 
 		try {
 			pis = new PipedInputStream(pos);
 		} catch (IOException e) {
 			if (log != null) {
 				log.log(LogService.LOG_WARNING, "An IOException was generated", e);
 			}
 		}
 
 		if (pis != null) {
 			synchronized (outputStreamWriters) {
 				outputStreamWriters.add(pos);
 				notifyStreamMultiplexerListeners(new StreamMultiplexerEvent(StreamMultiplexerEvent.EVENT_STREAM_ADDED, outputStreamWriters.size()));
 			}
 		}
 		return pis;
 	}
 }
