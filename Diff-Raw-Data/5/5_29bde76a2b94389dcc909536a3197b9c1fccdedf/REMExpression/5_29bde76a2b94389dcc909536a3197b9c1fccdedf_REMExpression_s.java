 /*******************************************************************************
  * Copyright (c) 2004, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  *  $RCSfile: REMExpression.java,v $
 *  $Revision: 1.18 $  $Date: 2005/08/24 20:39:07 $ 
  */
 package org.eclipse.jem.internal.proxy.remote;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.Level;
 
 import org.eclipse.jem.internal.proxy.common.CommandException;
 import org.eclipse.jem.internal.proxy.common.remote.*;
 import org.eclipse.jem.internal.proxy.common.remote.Commands.ValueObject;
 import org.eclipse.jem.internal.proxy.core.*;
 import org.eclipse.jem.internal.proxy.initParser.tree.*;
  
 /**
  * The Remote proxy version of Expression.
  * 
  * @since 1.0.0
  */
 public class REMExpression extends Expression {
 
 	private IREMExpressionConnection connection;
 	private boolean closed;	// Are we closed.
 	
 	protected Commands.ValueObject workerValue;	// A worker object so that we don't need to keep creating one and throwing it away.
 	
 	protected Map beanTypeCache;	// Use to cache pending BeanTypes. Used in conjunction with REMStandardBeanTypeFactory.
 	protected Map methodsCache;	// Use to cache pending expression method proxies. Used in conjunction with REMProxyConsants.
 	protected Map fieldsCache;	// Use to cache pending expression field proxies. Used in conjunction with REMProxyConsants.
 	
 	/*
 	 * This is very special list. It tries to eliminate unneeded traffic. For example a mark immediately followed by an endmark does
 	 * not need to be sent. Many expressions can look like: mark, endmark, endtransaction. This is a do nothing and we don't want
 	 * to create a connection to just send this. So this list is used to queue up these and remove them too when found as not needed.
 	 * 
 	 *  However, this is very tricky because all pushToProxy transactions that actually do something MUST call the processPending() method 
 	 *  first to make sure any pending transactions are submitted. Because once a real type transaction, such as assignment occurs, any
 	 *  pending transaction is a valid transaction, and no longer a do-nothing transaction.
 	 *  
 	 *  Each transaction type uses a subclass of PendingTransaction to be an entry on the list.
 	 *  
 	 *  The pendings currently supported are:
 	 *  mark/endmark
 	 *  try/catch/endtry
 	 *  block/endblock
 	 *  
 	 *  See each individual transaction type to see how it is handled.
 	 */
 	protected List pendingTransactions;
 	
 	/**
 	 * PendingTransaction entry.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected abstract static class PendingTransaction {
 		
 		/**
 		 * The transaction is now being pushed. The implementation should
 		 * actually do the push.
 		 * 
 		 * @param remExpression The REMExpression for this transaction.
 		 * 
 		 * @since 1.1.0
 		 */
 		public abstract void pushTransaction(REMExpression remExpression);
 	}
 	
 	/**
 	 * @param registry
 	 * 
 	 * @since 1.0.0
 	 */
 	public REMExpression(REMProxyFactoryRegistry registry) {
 		super(registry);
 	}
 	
 	/**
 	 * Return the expression id for this REMExpression. This id is used on the remote vm to
 	 * identify who the request is for.
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	protected int getREMExpressionID() {
 		return this.hashCode();
 	}
 	/**
 	 * Get the pending transactions list.
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	protected List getPendingTransactions() {
 		if (pendingTransactions == null)
 			pendingTransactions = new ArrayList();
 		return pendingTransactions;
 	}
 	
 	// Use this flag when debugging to test if errors are due to improper pending processing.
 	// If true they will be treated as if not pending and will be executed immediately.
 	private static final boolean EXECUTE_PENDING_IMMEDIATELY = false;
 	protected void addPendingTransaction(PendingTransaction pending) {
 		if (!EXECUTE_PENDING_IMMEDIATELY)
 			getPendingTransactions().add(pending);
 		else
 			pending.pushTransaction(this);
 	}
 
 	private boolean sentData;	// Flag to indicate if we have sent anything yet to the remote vm. This is used for the pending optimizations.
 	
 	/**
 	 * Have we sent any data in this transaction yet.
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	protected boolean haveSentData() {
 		return sentData;
 	}
 	
 	/**
 	 * @return Returns the connection.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected IREMExpressionConnection getConnection() {
 		if (connection == null) {
 			if (!sentData)
 				getREMBeanProxyFactory().startTransaction();	// This is the first time we send data, so start transaction.
 			
 			sentData = true;	// If we are getting a transaction, that means we are sending data.
 			connection = (IREMExpressionConnection) getREMRegistry().getFreeConnection();
 			// This will actually not be stopped until closeproxy. There could be a slight problem if the expression is never closed.
 			// But that shouldn't happen. This is to prevent any proxy that was released during the execution but was used by
 			// the expression from being released on the remote vm until after the expression is finished.
 			try {
 				if (workerValue == null)
 					workerValue = new Commands.ValueObject();
 				if (expressionProcesserController == null) {
 					byte trace = !isTraceSet() ? ExpressionCommands.TRACE_DEFAULT : (isTrace() ? ExpressionCommands.TRACE_ON : ExpressionCommands.TRACE_OFF); 
 					connection.startExpressionProcessing(getREMExpressionID(), trace);	// It is a new expression.
 				} else {
 					fillProxy(expressionProcesserController, workerValue);
 					connection.resumeExpression(getREMExpressionID(), workerValue);
 					expressionProcesserController = null;
 				}
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				throwIllegalStateException(IO_EXCEPTION_MSG);
 			} catch (CommandException e) {
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				if (!e.isRecoverable()) {
 					connection.close();
 					connection = null;
 				}
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}	
 		}
 		return connection;
 	}
 
 	/**
 	 * General IOException occurred msg.
 	 */
 	protected static final String IO_EXCEPTION_MSG = ProxyRemoteMessages.REMExpression_IOExceptionSeeLog_INFO_; 
 	
 	protected static final String COMMAND_EXCEPTION_MSG = ProxyRemoteMessages.REMExpression_CommandExceptionSeeLog_INFO_; 
 	
 	/**
 	 * Throw an an illegal state exception if some general error, in particular an I/O or Command Exception
 	 * occurred so that callers know there is something wrong.
 	 * 
 	 * @param msg
 	 * @throws IllegalStateException
 	 * 
 	 * @since 1.0.0
 	 */
 	protected void throwIllegalStateException(String msg) throws IllegalStateException {
 		throw new IllegalStateException(msg);
 	}
 	
 	/**
 	 * Return the registry as a REMProxyFactoryRegistry
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final REMProxyFactoryRegistry getREMRegistry() {
 		return (REMProxyFactoryRegistry) registry;
 	}
 	
 	/**
 	 * Return the bean proxy factory as a REMStandardBeanProxyFactory.
 	 * @return
 	 * 
 	 * @since 1.0.0
 	 */
 	protected final REMStandardBeanProxyFactory getREMBeanProxyFactory() {
 		return (REMStandardBeanProxyFactory) beanProxyFactory;
 	}
 
 	/**
 	 * Process any pending transactions.
 	 * <p>
 	 * <b>Note: </b>It is required that all non-pending-participating transactions must
 	 * call this method first to make sure pending transactions are sent. If this is
 	 * not done, there will be errors in the expression.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void processPendingTransactions() {
 		if (pendingTransactions != null && !pendingTransactions.isEmpty()) {
 			try {
 				for (int i = 0; i < pendingTransactions.size(); i++) {
 					((PendingTransaction) pendingTransactions.get(i)).pushTransaction(this);
 				}
 			} finally {
 				pendingTransactions.clear();
 			}
 		}
 	}
 	
 	/**
 	 * Get the pending entry from top. If top is 1, then get top entry (i.e. last one added), 2 is next one. 
 	 * @param fromTop
 	 * @return entry requested, or <code>null</code> if no such entry.
 	 * 
 	 * @since 1.1.0
 	 */
 	protected PendingTransaction getPendingEntryFromTop(int fromTop) {
 		if (pendingTransactions != null && pendingTransactions.size() >= fromTop) {
 			return (PendingTransaction) pendingTransactions.get(pendingTransactions.size()-fromTop);
 		} else
 			return null;
 	}
 	
 	/**
 	 * Pop up the top entry from the pending transactions queue.
 	 * @param fromTop how many entries to pop from the pending transaction list.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void popPendingEntry(int fromTop) {
 		if (pendingTransactions != null)
 			if (pendingTransactions.size() > fromTop) {
 				while(fromTop-- >0)
 					pendingTransactions.remove(pendingTransactions.size()-1);
 			} else
 				pendingTransactions.clear();
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushToProxy(org.eclipse.jem.internal.proxy.core.IProxy)
 	 */
 	protected void pushToProxy(IProxy proxy) {
 		if (proxy == null || proxy.isBeanProxy())
 			pushToProxy((IBeanProxy) proxy);
 		else
 			pushToExpressionProxy((ExpressionProxy) proxy);
 	}
 	
 	private void pushToProxy(IBeanProxy proxy) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push proxy command is:
 			//	PushExpressionCommand(push to proxy) followed by:
 			//		ValueObject containing the rendered proxy.
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.PUSH_TO_PROXY_EXPRESSION_VALUE);
 			if (proxy == null)
 				workerValue.set();
 			else
 				((IREMBeanProxy) proxy).renderBean(workerValue);
 			connection.pushValueObject(workerValue);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#closeProxy()
 	 */
 	protected void closeProxy() {
 		if (!closed) {
 			try {
 				if (connection != null && connection.isConnected()) {
 					try {
 							connection.stopExpressionProcessing(getREMExpressionID());
 					} catch (IOException e) {
 						connection.close();
 						ProxyPlugin.getPlugin().getLogger().log(e, Level.INFO);
 						// Not throwing an illegal state here because we don't care, other than logging and not 
 						// returning the connection to the registry that there was an error on close.
 					} finally {
 						getREMRegistry().returnConnection(connection);
 					}
 				}
 			} finally {
 				closed = true;
 				if (sentData)
 					getREMBeanProxyFactory().stopTransaction();	// Resume proxy releases. We've sent data at least once.
 			}
 		}
 		methodsCache = null;
 		fieldsCache = null;
 		beanTypeCache = null;
 		pendingTransactions = null;
 		connection = null;
 	}
 	
 	private static final Object VOIDTYPE = new Object();	// A void type was sent in expression proxy resolution.
 	private static final Object NOTRESOLVED = new Object();	// A not resolved type was sent in expression proxy resolution.
 	
 	/*
 	 * Get the sender to use for pulling the expression proxy resolutions.
 	 */
 	private BeanProxyValueSender getExpressionProxiesSender() {
 		return new BeanProxyValueSender(getREMBeanProxyFactory()) {
 
 			/*
 			 * (non-Javadoc)
 			 * 
 			 * @see org.eclipse.jem.internal.proxy.remote.BeanProxyValueSender#sendValue(org.eclipse.jem.internal.proxy.common.remote.Commands.ValueObject)
 			 */
 			public void sendValue(ValueObject value) {
 				if (value.getType() == Commands.FLAG) {
 					switch (value.anInt) {
 						case ExpressionCommands.EXPRESSIONPROXY_NOTRESOLVED:
 							array[index++] = NOTRESOLVED;
 							break;
 						case ExpressionCommands.EXPRESSIONPROXY_VOIDTYPE:
 							array[index++] = VOIDTYPE;
 							break;
 						default:
 							// Shouldn't happen.
 							break;
 					}
 				} else
 					super.sendValue(value);
 			}
 		};
 	}
 		
 	/*
 	 * Process the pulled expression proxy resolutions.
 	 */
 	private void processpulledExpressionProxies(List expressionProxies, BeanProxyValueSender sender) {
 
 		// It is expected that each entry will coorespond to the next non-null expression proxy and will be the bean proxy or one of the special
 		// types.
 		int len = expressionProxies.size();
 		int j = 0;
 		Object[] resolveds = sender.getArray();
 		for (int i = 0; i < len; i++) {
 			ExpressionProxy ep = (ExpressionProxy) expressionProxies.get(i);
 			if (ep != null) {
 				Object resolved = resolveds[j++];
 				if (resolved == NOTRESOLVED)
 					fireProxyNotResolved(ep);
 				else if (resolved == VOIDTYPE)
 					fireProxyVoid(ep);
 				else
 					fireProxyResolved(ep, (IBeanProxy) resolved);
 			}
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pullProxyValue(int, java.util.List)
 	 */
 	protected IBeanProxy pullProxyValue(int proxycount, List expressionProxies) throws ThrowableProxy, NoExpressionValueException {
 		if (!haveSentData()) {
 			markAllProxiesNotResolved(expressionProxies);
 			return null;	// We haven't pushed any commands, so there is nothing to do. Don't create a connection for this.
 		}
 		// If there are any pending transactions at this point in time, there is no need to send them. They would be do nothings anyway.
 		
 		boolean processedExpressionProxies = false;
 		IREMExpressionConnection lclConnection = getConnection();
 		markInTransaction(lclConnection);
 		try {
 			Commands.ValueObject proxyids = null;
 			BeanProxyValueSender sender = null;
 			if (proxycount > 0) {
 				proxyids = createExpressionProxiesValueObject(proxycount, expressionProxies);
 				sender = getExpressionProxiesSender();
 			}
 			
 			lclConnection.pullValue(getREMExpressionID(), proxyids, sender);
 			// If we got this far, then if there are proxies, we need to process these too.
 			if (proxycount > 0)
 				processpulledExpressionProxies(expressionProxies, sender);
 			processedExpressionProxies =true;
 			lclConnection.getFinalValue(workerValue);	// Get the returned value.
 			return getREMBeanProxyFactory().getBeanProxy(workerValue);
 		} catch (CommandErrorException e) {
 			try {
 				if (e.getErrorCode() == ExpressionCommands.EXPRESSION_NOEXPRESSIONVALUE_EXCEPTION) {
 					// Need to turn it into a Throwable.
 					ThrowableProxy t = null;
 					try {
 						getREMBeanProxyFactory().getBeanProxy(e.getValue());	// This will cause a throw to occur, but we don't want it going out, we want to capture it.
 					} catch (ThrowableProxy e1) {
 						t = e1;
 					}
 					throw new REMNoExpressionValueException(t);
 				}
 				getREMBeanProxyFactory().processErrorReturn(e);
 			} catch (CommandException e1) {
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				if (!e.isRecoverable()) {
 					lclConnection.close();
 					throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 				}			
 			}
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			if (!e.isRecoverable()) {
 				lclConnection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		} finally {
 			markEndTransaction(lclConnection);
 			if (!processedExpressionProxies)
 				markAllProxiesNotResolved(expressionProxies);	// We failed before we could process the expression proxies. So mark all as not resolved.
 		}
 		return null;
 	}
 	
 	/**
 	 * This is called by commands that write some data and will be reading data back immediately 
 	 * (i.e. pull value and invoke expression). If we are on a callback thread and have the 
 	 * used the connection from the callback thread, we need to tell the callback thread that
 	 * it is in a transaction. This is needed because while reading data back there are
 	 * sometimes calls back to the vm to get beantype data for new classes. This would 
 	 * normally be through a new connection so that it doesn't get stuck in the middle of the
 	 * data being sent back. But when running on a callback the same connection is used. So it
 	 * would stick data in the middle of the return stream of data. To prevent this we need
 	 * to tell the callback thread that it is in a transaction during this call so that any
 	 * such new connection requests will get a new connection.
 	 * <p>
 	 * This is not nestable (i.e. the first markEndTransaction will set it false, even if several nested
 	 * markInTransactions are called).
 	 * <p>
 	 * markEndTransaction must be called in ALL cases, such use try/finally.
 	 * @param remConnection the connection to see check against and mark in transaction for.
 	 * 
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void markInTransaction(IREMExpressionConnection remConnection) {
 		Thread thread = Thread.currentThread();
 		if (thread instanceof REMCallbackThread) {
 			// We are in a callback, and the callback connection is our connection, tell the callback that it is in transaction.
 			REMCallbackThread callbackThread = (REMCallbackThread) thread;
 			if (callbackThread.getConnection() == remConnection) {
 				callbackThread.setIntransaction(true);
 			}
 		}
 	}
 	
 	/**
 	 * Mark end of transaction.
 	 * @param remConn REMConnection to test and mark not in connection for.
 	 * 
 	 * @see REMExpression#markInTransaction(IREMExpressionConnection)
 	 * @since 1.1.0
 	 */
 	protected void markEndTransaction(IREMExpressionConnection remConn) {
 		Thread thread = Thread.currentThread();
 		if (thread instanceof REMCallbackThread) {
 			// We are in a callback, and the callback connection is our connection, tell the callback that it is in transaction.
 			REMCallbackThread callbackThread = (REMCallbackThread) thread;
 			if (callbackThread.getConnection() == remConn) {
 				callbackThread.setIntransaction(false);
 			}
 		}		
 	}
 
 	/**
 	 * @param expressionProxies
 	 * 
 	 * @since 1.1.0
 	 */
 	private Commands.ValueObject createExpressionProxiesValueObject(int actualCount, List expressionProxies) {
 		class ExpressionProxyRetriever implements Commands.ValueRetrieve {
 			Iterator expressionProxiesItr;
 			Commands.ValueObject worker = new Commands.ValueObject();
 
 			public ExpressionProxyRetriever(List expressionProxies) {
 				this.expressionProxiesItr = expressionProxies.iterator();
 			}
 
 			public Commands.ValueObject nextValue() {
 				worker.set(-1);
 				while (expressionProxiesItr.hasNext()) {
 					Object parm = expressionProxiesItr.next();
 					if (parm != null) {
 						worker.set(((ExpressionProxy) parm).getProxyID());
 						break;
 					}
 				} 
 				return worker;
 			}
 		};
 
 		workerValue.setArrayIDS(new ExpressionProxyRetriever(expressionProxies), actualCount, Commands.INT);
 		return workerValue;
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushCastToProxy(org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */
 	protected void pushCastToProxy(IProxyBeanType type) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push cast to proxy command is:
 			//	PushExpressionCommand(push cast to proxy) followed by:
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.CAST_EXPRESSION_VALUE);
 			fillProxy(type, workerValue);
 			connection.pushValueObject(workerValue);
 		} catch (IOException e) {
 			connection.close();			
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/**
 	 * Push the proxy bean type in the format depending on expression proxy or beantype proxy.
 	 * @param type
 	 * @throws IOException
 	 * 
 	 * @since 1.1.0
 	 */
 	protected void fillProxy(IProxy type, Commands.ValueObject value) throws IOException {
 		//		ValueObject containing the rendered bean type proxy if IBeanTypeProxy or int (for expression proxy id) if expression proxy.
 		if (type.isBeanProxy()) {
 			((IREMBeanProxy) type).renderBean(value);
 		} else {
 			ExpressionProxy ep = (ExpressionProxy) type;
 			value.set(ep.getProxyID());
 		}
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushInstanceofToProxy(org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */
 	protected void pushInstanceofToProxy(IProxyBeanType type) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push instanceof to proxy command is:
 			//	PushExpressionCommand(push instanceof to proxy) followed by:
 			//		ValueObject containing the rendered bean type proxy or the String representing the name of class.
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.INSTANCEOF_EXPRESSION_VALUE);
 			fillProxy(type, workerValue);
 			connection.pushValueObject(workerValue);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushInfixToProxy(org.eclipse.jem.internal.proxy.initParser.tree.InfixOperator, int)
 	 */
 	protected void pushInfixToProxy(InfixOperator operator, InternalInfixOperandType operandType) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push infix to proxy command is:
 			//	PushExpressionCommand(push infix to proxy) followed by:
 			//		byte: operator
 			//		byte: operandType
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.INFIX_EXPRESSION_VALUE);
 			connection.pushByte((byte) operator.getValue());
 			connection.pushByte((byte) operandType.getValue());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}		
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushPrefixToProxy(org.eclipse.jem.internal.proxy.initParser.tree.PrefixOperator)
 	 */
 	protected void pushPrefixToProxy(PrefixOperator operator) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push prefix to proxy command is:
 			//	PushExpressionCommand(push prefix to proxy) followed by:
 			//		byte: operator
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.PREFIX_EXPRESSION_VALUE);
 			connection.pushByte((byte) operator.getValue());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushArrayAccessToProxy(int)
 	 */
 	protected void pushArrayAccessToProxy(int indexCount) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push array access to proxy command is:
 			//	PushExpressionCommand(push array acces to proxy) followed by:
 			//		int: indexCount
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.ARRAY_ACCESS_EXPRESSION_VALUE);
 			connection.pushInt(indexCount);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushArrayCreationToProxy(org.eclipse.jem.internal.proxy.core.IProxyBeanType, int)
 	 */
 	protected void pushArrayCreationToProxy(IProxyBeanType type, int dimensionCount) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push array creation to proxy command is:
 			//	PushExpressionCommand(push array creation to proxy) followed by:
 			//		ValueObject containing the rendered bean type proxy or the expression proxy.
 			//		int: dimension count
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.ARRAY_CREATION_EXPRESSION_VALUE);
 			fillProxy(type, workerValue);
 			connection.pushValueObject(workerValue);
 			connection.pushInt(dimensionCount);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	protected void pushArrayInitializerToProxy(IProxyBeanType type, int stripCount, int expressionCount) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push array initializer to proxy command is:
 			//	PushExpressionCommand(push array initializer to proxy) followed by:
 			//		ValueObject containing the rendered bean type proxy or expression proxy.
 			//		int: strip count
 			//		int: expression count
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.ARRAY_INITIALIZER_EXPRESSION_VALUE);
 			fillProxy(type, workerValue);
 			connection.pushValueObject(workerValue);
 			connection.pushInt(stripCount);
 			connection.pushInt(expressionCount);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushClassInstanceCreationToProxy(org.eclipse.jem.internal.proxy.core.IProxyBeanType, int)
 	 */
 	protected void pushClassInstanceCreationToProxy(IProxyBeanType type, int argumentCount) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push class instance creation to proxy command is:
 			//	PushExpressionCommand(push class instance creation to proxy) followed by:
 			//		ValueObject containing the rendered bean type proxy or the expression proxy
 			//		int: argument count
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.CLASS_INSTANCE_CREATION_EXPRESSION_VALUE);
 			fillProxy(type, workerValue);
 			connection.pushValueObject(workerValue);
 			connection.pushInt(argumentCount);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushTypeReceiverToProxy(org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */ 
 	protected void pushTypeReceiverToProxy(IProxyBeanType type) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push type receiver to proxy command is:
 			//	PushExpressionCommand(push type receiver to proxy) followed by:
 			//		ValueObject containing the rendered bean type proxy or the expression proxy.
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.TYPERECEIVER_EXPRESSION_VALUE);
 			fillProxy(type, workerValue);
 			connection.pushValueObject(workerValue);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushFieldAccessToProxy(java.lang.String, boolean)
 	 */
 	protected void pushFieldAccessToProxy(Object field, boolean hasReceiver) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push field access to proxy command is:
 			//	PushExpressionCommand(push field access to proxy) followed by:
 			//		Commands.Value: fieldName or IProxyField
 			//		boolean: hasReceiver
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.FIELD_ACCESS_EXPRESSION_VALUE);
 			if (field instanceof String) {
 				workerValue.set((String) field);
 			} else {
 				fillProxy((IProxy) field, workerValue);
 			}
 			connection.pushValueObject(workerValue);
 			connection.pushBoolean(hasReceiver);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushMethodInvocationToProxy(java.lang.String, boolean, int)
 	 */
 	protected void pushMethodInvocationToProxy(Object method, boolean hasReceiver, int argCount) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push method invocation to proxy command is:
 			//	PushExpressionCommand(push method invocation to proxy) followed by:
 			//		Commands.ValueObject: methodName or IMethodProxy
 			//		boolean: hasReceiver
 			//		int: argCount
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.METHOD_EXPRESSION_VALUE);
 			if (method instanceof String) {
 				workerValue.set((String) method);
 			} else {
 				fillProxy((IProxy) method, workerValue);
 			}
 			connection.pushValueObject(workerValue);
 			connection.pushBoolean(hasReceiver);
 			connection.pushInt(argCount);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushConditionalToProxy(int)
 	 */
 	protected void pushConditionalToProxy(InternalConditionalOperandType expressionType) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push conditional to proxy command is:
 			//	PushExpressionCommand(push conditional to proxy) followed by:
 			//		byte: expression type
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.CONDITIONAL_EXPRESSION_VALUE);
 			connection.pushByte((byte) expressionType.getValue());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/*
 	 * A special one that takes the ThrowableProxy for no expression value and 
 	 * wrappers it prints its stack trace instead, but still makes it a subclass
 	 * of NoExpressionValueException.
 	 * 
 	 * @since 1.1.0
 	 */
 	private static class REMNoExpressionValueException extends NoExpressionValueException {
 		/**
 		 * Comment for <code>serialVersionUID</code>
 		 * 
 		 * @since 1.1.0
 		 */
 		private static final long serialVersionUID = 1692406777391812694L;
 
 
 		public REMNoExpressionValueException(ThrowableProxy e) {
 			super(e);
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Throwable#getLocalizedMessage()
 		 */
 		public String getLocalizedMessage() {
 			return ((ThrowableProxy) getCause()).getProxyLocalizedMessage();
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Throwable#getMessage()
 		 */
 		public String getMessage() {
 			return ((ThrowableProxy) getCause()).getProxyMessage();
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Throwable#printStackTrace()
 		 */
 		public void printStackTrace() {
 			getCause().printStackTrace();
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Throwable#printStackTrace(java.io.PrintStream)
 		 */
 		public void printStackTrace(PrintStream s) {
 			getCause().printStackTrace(s);
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see java.lang.Throwable#printStackTrace(java.io.PrintWriter)
 		 */
 		public void printStackTrace(PrintWriter s) {
 			getCause().printStackTrace(s);
 		}
 	}
 	
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushInvoke(int, java.util.List)
 	 */
 	protected void pushInvoke(int proxycount, List expressionProxies) throws ThrowableProxy, NoExpressionValueException {
 		if (!haveSentData()) {
 			markAllProxiesNotResolved(expressionProxies);
 			return;	// We haven't pushed any commands, so there is nothing to do. Don't create a connection for this.
 		}
 		// If at this point there are pending transactions, there is no need to send them because they would all be do-nothings.
 		
 		boolean processedExpressionProxies = false;
 		IREMExpressionConnection lclConnection = getConnection();
 		markInTransaction(lclConnection);
 		try {
 			Commands.ValueObject proxyids = null;
 			BeanProxyValueSender sender = null;
 			if (proxycount > 0) {
 				proxyids = createExpressionProxiesValueObject(proxycount, expressionProxies);
 				sender = getExpressionProxiesSender();
 			}
 
 			lclConnection.sync(getREMExpressionID(), proxyids, sender);
 			
 			// If we got this far, then if there are proxies, we need to process these too.
 			if (proxycount > 0)
 				processpulledExpressionProxies(expressionProxies, sender);
 			processedExpressionProxies = true;
 			lclConnection.getFinalValue(workerValue);	// We don't care what it is, we just need to see if there is an error.
 		} catch (CommandErrorException e) {
 			try {
 				if (e.getErrorCode() == ExpressionCommands.EXPRESSION_NOEXPRESSIONVALUE_EXCEPTION) {
 					// Need to turn it into a Throwable.
 					ThrowableProxy t = null;
 					try {
 						getREMBeanProxyFactory().getBeanProxy(e.getValue());	// This will cause a throw to occur, but we don't want it going out, we want to capture it.
 					} catch (ThrowableProxy e1) {
 						t = e1;
 					}
 					throw new REMNoExpressionValueException(t);
 				}
 				getREMBeanProxyFactory().processErrorReturn(e);
 			} catch (CommandException e1) {
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				if (!e.isRecoverable()) {
 					lclConnection.close();
 					throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 				}			
 			}
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			if (!e.isRecoverable()) {
 				lclConnection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		} finally {
 			markEndTransaction(lclConnection);
 			if (!processedExpressionProxies)
 				markAllProxiesNotResolved(expressionProxies);	// We failed before we could process the expression proxies. So mark all as not resolved.
 		}
 	}
 
 	private static class REMBeanTypeExpressionProxy extends ExpressionProxy implements IBeanTypeExpressionProxy {
 		
 		private String typeName;
 		
 		/**
 		 * @param proxyid
 		 * 
 		 * @since 1.1.0
 		 */
 		private REMBeanTypeExpressionProxy(int proxyid, Expression expression) {
 			super(proxyid, BEANTYPE_EXPRESSION_PROXY, expression);
 		}
 		
 		public void setTypeName(String typeName) {
 			this.typeName = typeName;
 		}
 		
 		public String getTypeName() {
 			return typeName;
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.core.ExpressionProxy#toString()
 		 */
 		public String toString() {
 			return super.toString()+" - "+getTypeName(); //$NON-NLS-1$
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.core.IProxyBeanType#getMethodProxy(org.eclipse.jem.internal.proxy.core.IExpression, java.lang.String, org.eclipse.jem.internal.proxy.core.IProxyBeanType[])
 		 */
 		public IProxyMethod getMethodProxy(IExpression expression, String methodName, IProxyBeanType[] parameterTypes) {
 			REMProxyFactoryRegistry registry = (REMProxyFactoryRegistry) expression.getRegistry();
 			return ((REMMethodProxyFactory) registry.getMethodProxyFactory()).getMethodProxy(expression, this, methodName, parameterTypes);
 		}
 		
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.core.IProxyBeanType#getMethodProxy(org.eclipse.jem.internal.proxy.core.IExpression, java.lang.String, java.lang.String[])
 		 */
 		public IProxyMethod getMethodProxy(IExpression expression, String methodName, String[] parameterTypes) {
 			REMProxyFactoryRegistry registry = (REMProxyFactoryRegistry) expression.getRegistry();
 			return ((REMMethodProxyFactory) registry.getMethodProxyFactory()).getMethodProxy(expression, this, methodName, parameterTypes);
 		}
 		
 		public IProxyMethod getMethodProxy(IExpression expression, String methodName) {
 			return getMethodProxy(expression, methodName, (IProxyBeanType[]) null);
 		}
 
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.core.IProxyBeanType#getFieldProxy(org.eclipse.jem.internal.proxy.core.IExpression, java.lang.String)
 		 */
 		public IProxyField getFieldProxy(IExpression expression, String fieldName) {
 			REMProxyFactoryRegistry registry = (REMProxyFactoryRegistry) expression.getRegistry();
 			return ((REMMethodProxyFactory) registry.getMethodProxyFactory()).getFieldProxy(expression, this, fieldName);
 		}
 	}
 	
 	private static class REMMethodExpressionProxy extends ExpressionProxy implements IProxyMethod {
 		
 			/**
 		 * @param proxyid
 		 * @param proxyType
 		 * @param expression
 		 * 
 		 * @since 1.1.0
 		 */
 		private REMMethodExpressionProxy(int proxyid, Expression expression) {
 			super(proxyid, METHOD_EXPRESSION_PROXY, expression);
 		}
 	}
 	
 	private static class REMFieldExpressionProxy extends ExpressionProxy implements IProxyField {
 		
 			/**
 		 * @param proxyid
 		 * @param proxyType
 		 * @param expression
 		 * 
 		 * @since 1.1.0
 		 */
 		private REMFieldExpressionProxy(int proxyid, Expression expression) {
 			super(proxyid, FIELD_EXPRESSION_PROXY, expression);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#createExpressionProxy(int)
 	 */
 	protected ExpressionProxy createExpressionProxy(int proxyType, int proxyID) {
 		switch (proxyType) {
 			case NORMAL_EXPRESSION_PROXY:
 			default:
 				return new ExpressionProxy(proxyID, NORMAL_EXPRESSION_PROXY, this);
 			
 			case BEANTYPE_EXPRESSION_PROXY:
 				return new REMBeanTypeExpressionProxy(proxyID, this);
 				
 			case METHOD_EXPRESSION_PROXY:
 				return new REMMethodExpressionProxy(proxyID, this);
 				
 			case FIELD_EXPRESSION_PROXY:
 				return new REMFieldExpressionProxy(proxyID, this);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushAssignmentToProxy(org.eclipse.jem.internal.proxy.core.ExpressionProxy)
 	 */
 	protected void pushAssignmentToProxy(ExpressionProxy proxy) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push assignment to proxy command is:
 			//	PushExpressionCommand(push assignment to proxy) followed by:
 			//		int: proxy id
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.ASSIGNMENT_PROXY_EXPRESSION_VALUE);
 			connection.pushInt(proxy.getProxyID());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 	
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushAssignmentToProxy()
 	 */
 	protected void pushAssignmentToProxy() {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of the push assignment command is:
 			//   PushAssignmentCommand.
 			connection.pushExpressionCommand(getREMExpressionID(), (byte) InternalExpressionTypes.ASSIGNMENT_EXPRESSION_VALUE);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 	
 
 	private void pushToExpressionProxy(ExpressionProxy proxy) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to expression proxy command is:
 			//	PushExpressionCommand(push expression proxy to proxy) followed by:
 			//		int: proxy id
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.PUSH_TO_EXPRESSION_PROXY_EXPRESSION_VALUE);
 			connection.pushInt(proxy.getProxyID());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 
 	}
 	
 	private static class BlockBegin extends PendingTransaction {
 		public int blockNumber;
 		
 		public BlockBegin(int blockNumber) {
 			this.blockNumber = blockNumber;
 		}
 		
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push to block begin proxy command is:
 				//	PushExpressionCommand(push block begin proxy to proxy) followed by:
 				//		int: block id
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.BLOCK_BEGIN_EXPRESSION_VALUE);
 				connection.pushInt(blockNumber);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			}			
 		}
 		
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushBlockBeginToProxy(int)
 	 */
 	protected void pushBlockBeginToProxy(int blockNumber) {
 		addPendingTransaction(new BlockBegin(blockNumber));
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushBlockEndToProxy(int)
 	 */
 	protected void pushBlockEndToProxy(int blockNumber) {
 		// See if the top pending transactions is BreakBlock(blockNumber). If it is then the BreakBlock can be thrown away.
 		PendingTransaction topEntry = getPendingEntryFromTop(1);
 		if (topEntry instanceof BlockBreak && ((BlockBreak) topEntry).blockNumber == blockNumber) {
 			popPendingEntry(1);
 			topEntry = getPendingEntryFromTop(1);
 		}
 		// See if the top pending transaction is now BeginBlock(blockNumber). If it is, then this transaction and the block begin
 		// can be thrown away because they are an empty block.
 		if (topEntry instanceof BlockBegin && ((BlockBegin) topEntry).blockNumber == blockNumber) {
 			popPendingEntry(1);
 			return;
 		}
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to block end proxy command is:
 			//	PushExpressionCommand(push block end proxy to proxy) followed by:
 			//		int: block id
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.BLOCK_END_EXPRESSION_VALUE);
 			connection.pushInt(blockNumber);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 	
 	private static class BlockBreak extends PendingTransaction {
 		public int blockNumber;
 		
 		public BlockBreak(int blockNumber) {
 			this.blockNumber = blockNumber;
 		}
 		
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push to block break proxy command is:
 				//	PushExpressionCommand(push block break proxy to proxy) followed by:
 				//		int: block id
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.BLOCK_BREAK_EXPRESSION_VALUE);
 				connection.pushInt(blockNumber);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			}
 		}		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushBlockBreakToProxy(int)
 	 */
 	protected void pushBlockBreakToProxy(int blockNumber) {
 		// Even if there is no pending block begin for this block, we will pend the break.
 		// This is so that if the break occurred just before the block end, then it can be ignored.
 		addPendingTransaction(new BlockBreak(blockNumber));
 	}
 
 	private static class TryBegin extends PendingTransaction {
 
 		public final int tryNumber;
 		
 		public TryBegin(int tryNumber) {
 			this.tryNumber = tryNumber;
 			
 		}
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push to try begin proxy command is:
 				//	PushExpressionCommand(push try begin to proxy) followed by:
 				//		int: try id
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.TRY_BEGIN_EXPRESSION_VALUE);
 				connection.pushInt(tryNumber);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			}
 		}
 		
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushTryBeginToProxy(int)
 	 */
 	protected void pushTryBeginToProxy(int tryNumber) {
 		addPendingTransaction(new TryBegin(tryNumber));
 	}
 
 	private static class TryCatch extends PendingTransaction {
 
 		public final int tryNumber;
 		private final IProxyBeanType exceptionType;
 		private final ExpressionProxy ep;
 
 		public TryCatch(int tryNumber, IProxyBeanType exceptionType, ExpressionProxy ep) {
 			this.tryNumber = tryNumber;
 			this.exceptionType = exceptionType;
 			this.ep = ep;
 		}
 		
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push to try begin proxy command is:
 				//	PushExpressionCommand(push try begin to proxy) followed by:
 				//		int: try id
 				//		object: expression type (as beantype or as expression proxy)
 				//		int: proxy id or (-1 if null).
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.TRY_CATCH_EXPRESSION_VALUE);
 				connection.pushInt(tryNumber);
 				remExpression.fillProxy(exceptionType, remExpression.workerValue);
 				connection.pushValueObject(remExpression.workerValue);
 				if (ep != null)
 					connection.pushInt(ep.getProxyID());
 				else
 					connection.pushInt(-1);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			} catch (CommandException e) {
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				if (!e.isRecoverable()) {
 					connection.close();
 					remExpression.throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 				}			
 			}
 		}
 		
 	}
 	/*
 	 *  (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushTryCatchClauseToProxy(int, org.eclipse.jem.internal.proxy.core.IProxyBeanType, org.eclipse.jem.internal.proxy.core.ExpressionProxy)
 	 */
 	protected void pushTryCatchClauseToProxy(int tryNumber, IProxyBeanType exceptionType, ExpressionProxy ep) {
 		addPendingTransaction(new TryCatch(tryNumber, exceptionType, ep));
 	}
 
 	private static class TryFinally extends PendingTransaction {
 		
 		public final int tryNumber;
 
 		public TryFinally(int tryNumber) {
 			this.tryNumber = tryNumber;
 			
 		}
 
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push to try begin proxy command is:
 				//	PushExpressionCommand(push try finally to proxy) followed by:
 				//		int: try id
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.TRY_FINALLY_EXPRESSION_VALUE);
 				connection.pushInt(tryNumber);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			}
 		}
 		
 	}
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushTryFinallyClauseToProxy(int)
 	 */
 	protected void pushTryFinallyClauseToProxy(int tryNumber) {
 		addPendingTransaction(new TryFinally(tryNumber));
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushTryEndToProxy(int)
 	 */
 	protected void pushTryEndToProxy(int tryNumber) {
 		// This is a little tricky. We need to find if there is nothing but try/catch/finally for this tryNumber on the pending
 		// transactions up to the try begin, if there is nothing else, then we can throw the entire try away. That
 		// means there was no code at all in any of the try/catch/finally blocks.
 		int fromTop = 0;
 		while (true) {
 			PendingTransaction topEntry = getPendingEntryFromTop(++fromTop);
 			if (topEntry instanceof TryFinally) {
 				if (((TryFinally) topEntry).tryNumber != tryNumber)
 					break;	// We met a finally that wasn't ours, so entire try group must be sent.
 			} else if (topEntry instanceof TryCatch) {
 				if (((TryCatch) topEntry).tryNumber != tryNumber)
 					break;	// We met a catch that wasn't ours, so entire try group must be sent.
 			} else if (topEntry instanceof TryBegin) {
 				if (((TryBegin) topEntry).tryNumber == tryNumber) {
 					// We've met our try begin, and nothing but empty catch/finally in between, so the entire group can be thrown away
 					popPendingEntry(fromTop);
 					return;
 				} else
 					break;	// We've hit a try begin that wasn't ours, so the entire try group must be sent.	
 			} else
 				break;	// We've hit something other than our try group, so process everything.
 		}
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to try begin proxy command is:
 			//	PushExpressionCommand(push try end to proxy) followed by:
 			//		int: try id
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.TRY_END_EXPRESSION_VALUE);
 			connection.pushInt(tryNumber);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushThrowToProxy()
 	 */
 	protected void pushThrowToProxy() {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to try begin proxy command is:
 			//	PushExpressionCommand(push throw to proxy)
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.THROW_EXPRESSION_VALUE);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushRethrowToProxy(int)
 	 */
 	protected void pushRethrowToProxy(int tryNumber) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to rethow proxy command is:
 			//	PushExpressionCommand(push rethrow to proxy)
 			//		int: try id
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.RETHROW_EXPRESSION_VALUE);
 			connection.pushInt(tryNumber);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushBeanTypeToProxy(org.eclipse.jem.internal.proxy.core.IBeanTypeExpressionProxy)
 	 */
 	protected void pushBeanTypeToProxy(IBeanTypeExpressionProxy proxy) {
 		// Push beantype to proxy is sent out of sequence without respect to where in expression we are,
 		// so no need to handle pending transactions at this point. They would not affect the result
 		// of this call.
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to beanType proxy command is:
 			//	PushExpressionCommand(push bean type expression proxy)
 			//		int: proxy id
 			//		string: typename
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.PUSH_BEANTYPE_EXPRESSIONPROXY_EXPRESSION_VALUE);
 			REMBeanTypeExpressionProxy ep = (REMBeanTypeExpressionProxy) proxy;
 			connection.pushInt(ep.getProxyID());
 			connection.pushString(ep.getTypeName());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushMethodToProxy(org.eclipse.jem.internal.proxy.core.ExpressionProxy, org.eclipse.jem.internal.proxy.core.IProxyBeanType, java.lang.String, org.eclipse.jem.internal.proxy.core.IProxyBeanType[])
 	 */
 	protected void pushMethodToProxy(ExpressionProxy proxy, IProxyBeanType declaringType, String methodName, IProxyBeanType[] parameterTypes) {
 		// Push method to proxy is sent out of sequence without respect to where in expression we are,
 		// so no need to handle pending transactions at this point. They would not affect the result
 		// of this call.
 
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to method proxy command is:
 			//	PushExpressionCommand(push method type expression proxy)
 			//		int: proxy id
 			//		ValueObject: containing the rendered bean type proxy or the expression proxy for the declaring type
 			//		string: method name
 			//		int: number of parameter types
 			//		ValueObject(s): containing the rendered bean type proxy or the expression proxy for the parameter types.
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.PUSH_METHOD_EXPRESSIONPROXY_EXPRESSION_VALUE);
 			connection.pushInt(proxy.getProxyID());
 			fillProxy(declaringType, workerValue);
 			connection.pushValueObject(workerValue);
 			connection.pushString(methodName);
 			if (parameterTypes == null || parameterTypes.length == 0)
 				connection.pushInt(0);
 			else {
 				connection.pushInt(parameterTypes.length);
 				for (int i = 0; i < parameterTypes.length; i++) {
 					fillProxy(parameterTypes[i], workerValue);
 					connection.pushValueObject(workerValue);					
 				}
 			}
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushFieldToProxy(org.eclipse.jem.internal.proxy.core.ExpressionProxy, org.eclipse.jem.internal.proxy.core.IProxyBeanType, java.lang.String)
 	 */
 	protected void pushFieldToProxy(ExpressionProxy proxy, IProxyBeanType declaringType, String fieldName) {
 		// Push field to proxy is sent out of sequence without respect to where in expression we are,
 		// so no need to handle pending transactions at this point. They would not affect the result
 		// of this call.
 
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to field proxy command is:
 			//	PushExpressionCommand(push field type expression proxy)
 			//		int: proxy id
 			//		ValueObject: containing the rendered bean type proxy or the expression proxy for the declaring type
 			//		string: field name
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.PUSH_FIELD_EXPRESSIONPROXY_EXPRESSION_VALUE);
 			connection.pushInt(proxy.getProxyID());
 			fillProxy(declaringType, workerValue);
 			connection.pushValueObject(workerValue);
 			connection.pushString(fieldName);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 	
 	/**
 	 * Get the map of IProxyMethods for a beantype. Meant to be used only in conjunction with REMProxyConstants.
 	 * It is here so the REMProxyConstants can store pending proxies per expression.
 	 * 
 	 * @param beanType
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public Map getMethods(IProxyBeanType beanType) {
 		if (methodsCache == null)
 			methodsCache = new HashMap();
 		Map methods = (Map) methodsCache.get(beanType.getTypeName());
 		if(methods == null){
 			methods = new HashMap(20);
 			methodsCache.put(beanType.getTypeName(),methods);
 		}
 		return methods;	
 	}
 	
 	/**
 	 * Get the map of IProxyFields for a beantype. Meant to be used only in conjunction with REMProxyConstants.
 	 * It is here so the REMProxyConstants can store pending proxies per expression.
 	 * 
 	 * @param beanType
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public Map getFields(IProxyBeanType beanType) {
 		if (fieldsCache == null)
 			fieldsCache = new HashMap();
 		Map fields = (Map) fieldsCache.get(beanType.getTypeName());
 		if(fields == null){
 			fields = new HashMap(20);
 			fieldsCache.put(beanType.getTypeName(),fields);
 		}
 		return fields;	
 	}
 	
 	/**
 	 * Get the map of IProxyBeanTypes for a beantype name. Meant to be used only in conjunction with REMSgtandardBeanTypeFactory.
 	 * It is here so the REMStandardBeanTypeFactory can store pending proxies per expression.
 	 * 
 	 * @param beanType
 	 * @return
 	 * 
 	 * @since 1.1.0
 	 */
 	public IProxyBeanType getBeanType(String beanTypeName) {
 		if (beanTypeCache == null)
 			beanTypeCache = new HashMap();
 		return (IProxyBeanType) beanTypeCache.get(beanTypeName);
 	}
 	
 	/**
 	 * Add the beantype expression proxy to the map of bean type expression proxies. Used in conjunction with REMStandardBeanTypeFactory.
 	 * It is here so the REMStandardBeanTypeFactory can store pending proxies per expression.
 	 * @param beanTypeName
 	 * @param beantype
 	 * 
 	 * @since 1.1.0
 	 */
 	public void addBeanType(String beanTypeName, IProxyBeanType beantype) {
 		beanTypeCache.put(beanTypeName, beantype);
 	}
 	
 	/**
 	 * Remove the beantype expression proxy from the map. This is called because there was a rollback due to an endmark.
 	 * @param beanTypeName
 	 * 
 	 * @since 1.1.0
 	 */
 	public void removeBeanType(String beanTypeName) {
 		beanTypeCache.remove(beanTypeName);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushIfTestToProxy()
 	 */
 	protected void pushIfTestToProxy() {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push if test to proxy command is:
 			//	PushExpressionCommand(push if test to proxy)
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.IF_TEST_EXPRESSION_VALUE);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushIfElseToProxy(org.eclipse.jem.internal.proxy.initParser.tree.InternalIfElseOperandType)
 	 */
 	protected void pushIfElseToProxy(InternalIfElseOperandType clauseType) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push conditional to proxy command is:
 			//	PushExpressionCommand(push if/else clause to proxy) followed by:
 			//		byte: clause type
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.IF_ELSE_EXPRESSION_VALUE);
 			connection.pushByte((byte) clauseType.getValue());
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jem.internal.proxy.core.Expression#pushNewInstanceToProxy(java.lang.String, org.eclipse.jem.internal.proxy.core.IProxyBeanType)
 	 */
 	protected void pushNewInstanceToProxy(String initializationString, IProxyBeanType resultType) {
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push new instance from initstring to proxy command is:
 			//	PushExpressionCommand(push new instance to proxy) followed by:
 			//		string: init string
 			//		ValueObject: containing the rendered bean type proxy or the expression proxy for the declaring type
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.NEW_INSTANCE_VALUE);
 			connection.pushString(initializationString);
 			fillProxy(resultType, workerValue);
 			connection.pushValueObject(workerValue);			
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		} catch (CommandException e) {
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			if (!e.isRecoverable()) {
 				connection.close();
 				throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 			}			
 		}
 	}
 
 	private static class Mark extends PendingTransaction {
 		public int markID;
 		
 		public Mark(int markID) {
 			this.markID = markID;
 		}
 		
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push mark to proxy command is:
 				//	PushExpressionCommand(push mark to proxy) followed by:
 				//		int: markID
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.MARK_VALUE);
 				connection.pushInt(markID);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			}
 		}
 		
 	}
 	
 	protected void pushMarkToProxy(int markID) {
 		addPendingTransaction(new Mark(markID));
 	}
 
 	protected void pushEndmarkToProxy(int markID, boolean restore) {
 		// See if the top pending transaction is now Mark(markID). If it is, then this transaction and the mark begin
 		// can be thrown away because they are an empty block.
 		PendingTransaction topEntry = getPendingEntryFromTop(1);
 		if (topEntry instanceof Mark && ((Mark) topEntry).markID == markID) {
 			popPendingEntry(1);
 			return;
 		}
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push end mark to proxy command is:
 			//	PushExpressionCommand(push end mark to proxy) followed by:
 			//		int: markID
 			//		boolean: restore
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.ENDMARK_VALUE);
 			connection.pushInt(markID);
 			connection.pushBoolean(restore);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 	}
 
 	// This is the expression processor controller used to transfer.
 	// This is the guy that maintains continuity of the transaction as
 	// it is passed from one connection to another.
 	protected IBeanProxy expressionProcesserController;	
 	protected void pushBeginTransferThreadToProxy() throws ThrowableProxy {
 		// If the controller is not null, that means we had already requested a transfer
 		// but had not used it in this thread so there is no need to do anything. It
 		// will be handled when switching back to the other thread.
 		// If the connection is null, no need to do anything since there is no connection
 		// to transfer.
 		if (connection != null && expressionProcesserController == null) {
 			IREMExpressionConnection lclConnection = getConnection();
 			markInTransaction(lclConnection);
 			try {
 				workerValue.set();
 				lclConnection.transferExpression(getREMExpressionID(), workerValue);
 				expressionProcesserController = getREMBeanProxyFactory().getBeanProxy(workerValue);
 				getREMRegistry().returnConnection(lclConnection);
 				this.connection = null;
 			} catch (CommandException e) {
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				if (!e.isRecoverable()) {
 					lclConnection.close();
 					throwIllegalStateException(COMMAND_EXCEPTION_MSG);
 				}			
 			} finally {
 				markEndTransaction(lclConnection);
 			}
 		}
 	}
 
 	protected void pushTransferThreadToProxy() {
 		// Don't need to do anything. The next time we need to push data across, we will get a connection and the getConnection()
 		// will hook up the expression processor controller for us. This way if nothing happens in this thread then we won't
 		// waste communication time on it.
 	}
 
 	private static class SubexpressionBegin extends PendingTransaction {
 		public int subexpressionNumber;
 		
 		public SubexpressionBegin(int subexpressionNumber) {
 			this.subexpressionNumber = subexpressionNumber;
 		}
 		
 		public void pushTransaction(REMExpression remExpression) {
 			IREMExpressionConnection connection = remExpression.getConnection();
 			try {
 				// Format of push to subexpression begin proxy command is:
 				//	PushExpressionCommand(push subexpression begin proxy to proxy) followed by:
 				//		int: subexpression id
 				connection.pushExpressionCommand(remExpression.getREMExpressionID(), (byte)InternalExpressionTypes.SUBEXPRESSION_BEGIN_EXPRESSION_VALUE);
 				connection.pushInt(subexpressionNumber);
 			} catch (IOException e) {
 				connection.close();
 				ProxyPlugin.getPlugin().getLogger().log(e);
 				remExpression.markInvalid(e.getLocalizedMessage());
 				remExpression.throwIllegalStateException(IO_EXCEPTION_MSG);
 			}			
 		}
 		
 	}
 
 	protected void pushSubexpressionBeginToProxy(int subexpressionNumber) {
 		addPendingTransaction(new SubexpressionBegin(subexpressionNumber));
 	}
 
 	protected void pushSubexpressionEndToProxy(int subexpressionNumber) {
 		// See if the top pending transactions is SubexpressionBegin(subexpressionNumber). If it is then the SubexpressionBegin can be thrown away.
 		PendingTransaction topEntry = getPendingEntryFromTop(1);
 		if (topEntry instanceof SubexpressionBegin && ((SubexpressionBegin) topEntry).subexpressionNumber == subexpressionNumber) {
 			popPendingEntry(1);
			topEntry = getPendingEntryFromTop(1);
 		}
 		processPendingTransactions();
 		IREMExpressionConnection connection = getConnection();
 		try {
 			// Format of push to block end proxy command is:
 			//	PushExpressionCommand(push subexpression end proxy to proxy) followed by:
 			//		int: subexpression id
 			connection.pushExpressionCommand(getREMExpressionID(), (byte)InternalExpressionTypes.SUBEXPRESSION_END_EXPRESSION_VALUE);
 			connection.pushInt(subexpressionNumber);
 		} catch (IOException e) {
 			connection.close();
 			ProxyPlugin.getPlugin().getLogger().log(e);
 			markInvalid(e.getLocalizedMessage());
 			throwIllegalStateException(IO_EXCEPTION_MSG);
 		}
 		}
 }
