 package inou.net.rpc;
 
 import inou.net.InvocationPool;
 import inou.net.Utils;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InterruptedIOException;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.ProtocolException;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 
 public class MessageServer implements BinConstants {
 
 	private Logger monitor = Logger.getLogger(this.getClass());
 	private String name;
 	private ServiceManager handlerTable;
 
 	private String salt = Long.toString((long)(Math.random()*System.currentTimeMillis()));
 	private long sidCounter = 0;
 	private Object sidLock = new Integer(1);
 
 	private Object sendingLock = new Integer(2);
 	private LinkedList sendingQueue = new LinkedList();
 	private LinkedList waitingList = new LinkedList();
 
 	private Object receivingLock = new Integer(3);
 	private HashMap receivingTable = new HashMap();
 
 	private Thread sendingThread,receivingThread;
 	private InvocationPool invocationPool;
 
 	public final static int SOCKET_NOT_CONNECTED = 0;
 	public final static int SOCKET_CLOSING = 1;
 	public final static int SOCKET_OPENED = 2;
 
 	private Object socketLock = new Integer(3);
 	private int socketState = SOCKET_NOT_CONNECTED;
 	private Socket socket;
 
 	private long lastErrorTime = -1;
 	private int fatalErrorCount = 0;
 	private static final long FATAL_ERROR_CONTINUETIME = 500;
 	private static final long FATAL_ERROR_STOPCOUNT = 5;
 
 	private OutputStream out;
 	private InputStream in;
 
     private OutputStream debugOut;
 
 	public MessageServer(String name,ServiceManager sm,InvocationPool pool) {
 		this.name = name;
 		this.handlerTable = sm;
 		this.invocationPool = pool;
 	}
     
     void setDebugOutput(OutputStream dout) {
         debugOut = dout;
     }
     OutputStream createDebugOutput(final OutputStream normalOutput) {
         if (debugOut != null) {
             return new OutputStream() {
                 public void write(int b) throws IOException {
                     debugOut.write(b);
                     normalOutput.write(b);
                 }
                 public void flush() throws IOException {
                     debugOut.flush();
                     normalOutput.flush();
                 }
                 public void close() throws IOException {
                     flush();
                     debugOut.close();
                     normalOutput.close();
                 }
             };
         } else {
             return normalOutput;
         }
     }
     
     public void setSocket(Socket s) throws IOException {
 		synchronized(socketLock) {
 			if (socketState != SOCKET_NOT_CONNECTED) {
 				throw new RuntimeException("Wrong socket state: "+socketState);
 			}
 			socket = s;
 			in = new BufferedInputStream(socket.getInputStream(),2048*4);
 			out = createDebugOutput(new BufferedOutputStream(socket.getOutputStream(),2048*4));
 			socketState = SOCKET_OPENED;
 			monitor.debug("MS:ready for I/O stream.");
 		}
 	}
 
     public Socket getSocket() {
         return socket;
     }
 
     public boolean isConnected() {
         synchronized (socketLock) {
             return socketState == SOCKET_OPENED;
         }
     }
 
 	public void blockWorkingThread() {
         synchronized (socketLock) {
             if (socketState != SOCKET_OPENED) {
                 return;
             }
             monitor.debug("MS:started working block.");
             
 			receivingThread = new Thread(receiver);
 			receivingThread.setName(name+"|receiving");
 			receivingThread.start();
 			sendingThread = new Thread(sender);
 			sendingThread.setName(name+"|sending");
 			sendingThread.start();
 		}
 		while(true) {
 			synchronized(socketLock) {
 				if (sendingThread == null) {
 					break;
 				}
 				try {
 					socketLock.wait(500);
 				} catch (InterruptedException e) {
 					monitor.warn(e.getMessage(),e);
 					break;
 				}
 			}
 		}
 		monitor.debug("MS:closing socket.");
 		synchronized(socketLock) {
 			try {
 				socket.close();
 			} catch (IOException e) {
 				monitor.warn(e.getMessage(),e);
 			}
 			socketState = SOCKET_NOT_CONNECTED;
 			socketLock.notifyAll();
 		}
 		monitor.debug("MS:finished working block.");
 	}
 	
 	public void shutdown() {
 		monitor.debug("MS:shutdown message arived.");
 		synchronized(socketLock) {
 			if (socketState == SOCKET_NOT_CONNECTED) {
 				monitor.debug("MS:shutdown: not connecting state: "+socketState);
 				return;
 			}
 			socketState = SOCKET_CLOSING;
 			synchronized(sendingLock) {
 				sendingLock.notifyAll();
 			}
 		}
 		while(true) {
 			synchronized(socketLock) {
 				if (socketState == SOCKET_NOT_CONNECTED) {
 					break;
 				}
 				try {
 					socketLock.wait(500);
 				} catch (InterruptedException e) {
 					monitor.warn(e.getMessage(),e);
 				}
 			}
 		}
 		if (sendingQueue.size() > 0) {
 			monitor.debug("MS: "+sendingQueue.size()+" messages are remained.");
 		}
 		monitor.debug("MS:shutdowned.");
 	}
 
 	private Object getSID() {
 		synchronized(sidLock) {
 			sidCounter++;
 			return "SB:"+salt+":"+sidCounter;
 		}
 	}
 	
 	//====(sending)=============================================
 
 	public Object send(String name,Object[] args) throws IOException {
 		Object sid = null;
 		synchronized(sendingLock) {
 			sid = addCallingMessageToSendingQueue(name,args);
 			waitingList.add(sid);
 			sendingLock.notifyAll();
 		}
 		try {
 			while(true) {
 				synchronized(receivingLock) {
 					AbstractResultObject ret = (AbstractResultObject)receivingTable.get(sid);
 					if (ret != null) {
 						return ret.getValue();
 					}
                     if (!isConnected()) {
                         throw new EOFException("Connection was closed unexpectedly.");
                     }
 				}
 				synchronized(receivingLock) {
 					try {
 						receivingLock.wait(500);
 					} catch (InterruptedException e) {
 						monitor.warn(e.getMessage(),e);
 						throw new RuntimeException(e);
 					}
 				}
 			}
 		} finally {
            synchronized(receivingLock) {
                Object removedObj = receivingTable.remove(sid);
                if (removedObj == null) {
                    monitor.debug("MS: Wrong removal: "+sid);
                }
            }
 			synchronized(sendingLock) {
 				waitingList.remove(sid);
 			}
 		}
 	}
 
 	private Object addResultToSendingQueue(Object sid,Object obj) {
 		AbstractResultObject r = new ResultOkObject(sid,obj);
 		sendingQueue.add(r);
 		monitor.debug("MS: +Queue["+sendingQueue.size()+"] : "+r);
 		return sid;
 	}
 
 	private Object addErrorToSendingQueue(Object sid,int code,String klass,String message,String detail) {
 		AbstractResultObject c = new ResultErrObject(sid,code,klass,message,detail);
 		sendingQueue.add(c);
 		monitor.debug("MS: +Queue["+sendingQueue.size()+"] : "+c);
 		return sid;
 	}
 
 	private Object addCallingMessageToSendingQueue(String name,Object[] args) {
 		Object sid = getSID();
 		CallingObject c = new CallingObject(sid,name,args); 
 		sendingQueue.add(c);
 		monitor.debug("MS: +Queue["+sendingQueue.size()+"] : "+c);
 		return sid;
 	}
 
 	private Runnable sender = new Runnable() {
 			public void run() {
 				try {
 					sendingLoop();
 				} finally {
 					monitor.debug("MS: Sender-thread finished.");
 					sendingThread = null;
 					synchronized(socketLock) {
 						socketLock.notifyAll();
 					}
 				}
 			}
 		};
 
 	private void sendingLoop() {
 		while(true) {
 			AbstractTransferObject entry = null;
 			try {
 				synchronized(sendingLock) {
 					if (sendingQueue.size() > 0) {
 						entry = (AbstractTransferObject)sendingQueue.removeFirst();
 						monitor.debug("MS: -Queue["+sendingQueue.size()+"] : "+entry.sid);
 						entry.exec(out);
 						monitor.debug("MS:  sent a message : "+entry.sid);
 					}
 				}
 			} catch (BinStreamException e) {
 				monitor.warn(e.getMessage(),e);
 				if (entry != null) {
 					StringWriter sw = new StringWriter();
 					e.printStackTrace(new PrintWriter(sw));
 					sw.flush();
 					synchronized(sendingLock) {
 						addErrorToSendingQueue(entry.sid,R_PROTOCOL_ERROR,"BinStreamException",
 											   e.getMessage()+" (maybe bug...)",sw.toString());
 						sendingLock.notifyAll();
 					}
 				}
 				continue;
 			} catch (IOException e) {
 				if (entry != null) {
 					monitor.warn("Failed to sending the message: "+entry);
 					synchronized(sendingLock) {
 						sendingQueue.add(entry);
 					}
 				}
 				String mes = e.getMessage();
 				if (e instanceof SocketException && mes != null && 
 					(mes.toLowerCase().indexOf("abort")>=0 || 
 					 mes.toLowerCase().indexOf("broken")>=0)) {
 					monitor.info("MS: [sendloop] disconnected by remote host.");
 					synchronized(socketLock) {
 						socketState = SOCKET_CLOSING;
 					}
 					break;
 				} else if (isRecoverableException(e)) {
 					monitor.debug("MS: [sendloop] going to recover the communication.");
 					continue;
 				} else {
 					monitor.warn(e.getMessage(),e);
 					monitor.info("MS: [sendloop] try to reset the connection.");
 					synchronized(socketLock) {
 						socketState = SOCKET_CLOSING;
 					}
 					break;
 				}
 			} catch (RuntimeException e) {
 				monitor.warn(e.getMessage(),e);
 				monitor.info("MS: [sendloop] going to recover the communication.");
 				continue;
 			}
 			synchronized(socketLock) {
 				if (socketState == SOCKET_CLOSING) {
 					monitor.debug("MS: sender-thread terminating...");
 					return;
 				}
 			}
 			synchronized(sendingLock) {
 				if (sendingQueue.size() > 0) {
 					continue;
 				}
 				try {
 					sendingLock.wait(500);
 				} catch (InterruptedException e) {
 					monitor.warn(e.getMessage(),e);
 					break;
 				}
 			}
 		}
 	}
 	
 	//====(receiving)=============================================
 	
 	private Runnable receiver = new Runnable() {
 			public void run() {
 				try {
 					receivingLoop();
 				} finally {
 					monitor.debug("MS: Receive-thread finished.");
 					receivingThread = null;
 					synchronized(socketLock) {
 						socketLock.notifyAll();
 					}
 				}
 			}
 		};
 
 	private void receivingLoop() {
 		final byte[] mcode = {M_NONE};
 		DecodeHandlerClass mcodeHandler = new DecodeHandlerClass() {
 				public void readByte(byte a) {
 					mcode[0] = a;
 				}
 			};
 		while(true) {
 			try {
 				mcode[0] = M_NONE;
 				BinDecoder.read(in,mcodeHandler);//reading header
 				monitor.debug("MS: receiving a message : code="+mcode[0]);
 				if (mcode[0] == M_CALL) {
 					CallingObject c = CallingObject.getCallingObject(in,handlerTable.getStreamGenerator());
 					monitor.debug("MS: received: "+c);
 					received(c);
 				} else if (mcode[0] == M_RETURN) {
 					AbstractResultObject ret = AbstractResultObject.get(in,handlerTable.getStreamGenerator());
 					monitor.debug("MS: received: "+ret);
 					synchronized(receivingLock) {
 						receivingTable.put(ret.sid,ret);
 						receivingLock.notifyAll();
 					}
 				} else if (mcode[0] == M_NONE) {
 					monitor.info("MS: disconnected by remote host.");
 					synchronized(socketLock) {
 						socketState = SOCKET_CLOSING;
 					}
 					synchronized(sendingLock) {
 						sendingLock.notifyAll();
 					}
 					return;
 				} else {
 					throw new RuntimeException("Wrong method code: "+mcode[0]);
 				}
 				synchronized(socketLock) {
 					if (socketState == SOCKET_CLOSING) {
 						monitor.debug("MS: receiver-thread terminating...");
 						return;
 					}
 				}
 			} catch (IOException e) {
 				String mes = e.getMessage();
 				if (e instanceof SocketException && mes != null && 
 					(mes.toLowerCase().indexOf("close")>=0 || 
 					 mes.toLowerCase().indexOf("broken")>=0)) {
 					monitor.info("MS: [rcvloop] disconnected by remote host.");
 					break;
 				} else if (isRecoverableException(e)) {
 					monitor.warn("MS: [rcvloop] going to recover the communication.");
 					continue;
 				} else {
 					monitor.warn(e.getMessage(),e);
 					monitor.warn("MS: [rcvloop] try to reset the connection.");
 					synchronized(socketLock) {
 						socketState = SOCKET_CLOSING;
 					}
 					break;
 				}
 			} catch (RuntimeException e) {
 				monitor.warn(e.getMessage(),e);
 				monitor.warn("MS: [rcvloop] going to recover the communication.");
 				continue;
 			}
 		}
 	}
 
 	boolean isRecoverableException(IOException e) {
 		if (e instanceof EOFException) {
 			monitor.warn("Connection closed by peer : "+e.getMessage());
 			return true;
 		} else if (e instanceof InterruptedIOException) {
 			monitor.warn("Interrupted by time out : "+e.getMessage());
 			return true;
 		} else if (e instanceof ProtocolException || e instanceof SocketException) {
 			monitor.warn(e.getMessage(),e);
 			long corTime = System.currentTimeMillis();
 			if (lastErrorTime > 0) {
 				if (Math.abs(lastErrorTime-corTime) < FATAL_ERROR_CONTINUETIME) {
 					fatalErrorCount++;
 					if (fatalErrorCount >= FATAL_ERROR_STOPCOUNT) {
 						return false;
 					}
 				} else {
 					fatalErrorCount = 0;
 				}
 			}
 			lastErrorTime = corTime;
 			return true;
 		} else {
 			monitor.warn("Unrecoverable error occued. ["+e.getClass().getName()+" : "+e.getMessage()+"]");
 			monitor.warn(e.getMessage(),e);
 			return false;
 		}
 	}
 	
 	private void received(CallingObject c) {
 		IMessageHandler handler = null;
 		synchronized(handlerTable) {
 			handler = handlerTable.getHandler(c.name);
 		}
 		if (handler == null) {
 			synchronized(sendingLock) {
 				addErrorToSendingQueue(c.sid,R_PROTOCOL_ERROR,"NoSuchRemoteMethodException",
 									   "Not found the remote method "+c.name+".","");
 				sendingLock.notifyAll();
 			}
 			return;
 		}
 		invocationPool.invokes(new MethodInvocation(handler,c));
 	}
 
 	private class MethodInvocation implements Runnable {
 		private IMessageHandler handler;
 		private CallingObject callingObj;
 		MethodInvocation(IMessageHandler h,CallingObject c) {
 			handler = h;
 			callingObj = c;
 		}
 		public void run() {
             monitor.debug("??"+monitor.getEffectiveLevel());
 			Utils.writeArguments(monitor,Level.DEBUG,new String[]{"MS:methodInvocation: Start: ",callingObj.sid.toString(),"  ",callingObj.name},callingObj.args);
 			try {
 				Object ret = handler.send(callingObj.args);
 				synchronized(sendingLock) {
 					addResultToSendingQueue(callingObj.sid,ret);
 				}
 			} catch (Exception e) {
 				Throwable t = e;
 				while (t.getCause() != null) {
 					t = t.getCause();
 				}
 				monitor.debug("Exporting exception : "+t.getClass().getName());
 				synchronized(sendingLock) {
 					addErrorToSendingQueue(callingObj.sid,R_APP_ERROR,t.getClass().getName(),t.getMessage(),Utils.trace2str(t));
 				}
 			} catch (Throwable e) {
 				monitor.warn(e.getMessage(),e);
 				monitor.warn("Handler raise an error: "+e.getClass().getName()+" : "+e.getMessage());
 				synchronized(sendingLock) {
 					addErrorToSendingQueue(callingObj.sid,R_FATAL_ERROR,e.getClass().getName(),e.getMessage(),Utils.trace2str(e));
 				}
 			} finally {
 				synchronized(sendingLock) {
 					sendingLock.notifyAll();
 				}
 				Utils.writeArray(monitor,Level.DEBUG,new Object[]{"MS:methodInvocation: End:",callingObj.sid.toString(),"  ",callingObj.name});
 			}
 		}
 	}
 }
