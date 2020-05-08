 /*
  * Copyright (c) 2009, Julian Gosnell
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *     * Redistributions of source code must retain the above copyright
  *     notice, this list of conditions and the following disclaimer.
  *
  *     * Redistributions in binary form must reproduce the above
  *     copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided
  *     with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
  * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
  * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.dada.jms;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Exchanger;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 
 import org.dada.slf4j.Logger;
 import org.dada.slf4j.LoggerFactory;
 
 public class SynchronousClient extends AbstractClient implements InvocationHandler, Serializable {
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(SynchronousClient.class);
 	private /* final */ transient Map<String, Exchanger<Results>> correlationIdToResults;
 
 	public SynchronousClient(Session session, Destination destination, Class<?> interfaze, long timeout, boolean trueAsync) throws JMSException {
 		super(session, destination, interfaze, timeout, trueAsync);
 		correlationIdToResults = new ConcurrentHashMap<String, Exchanger<Results>>();
 	}
 
 	//@Override
 	private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
 		correlationIdToResults = new ConcurrentHashMap<String, Exchanger<Results>>();
 		ois.defaultReadObject();
 	}
 
 	@Override
 	public void onMessage(Message message) {
 		try {
 			String correlationID = message.getJMSCorrelationID();
 			Exchanger<Results> exchanger = correlationIdToResults.remove(correlationID);
 			if (exchanger == null) {
 			       LOGGER.warn("{}: no exchanger for message: {}", System.identityHashCode(this), message);
 			} else {
 				ObjectMessage response = (ObjectMessage) message;
 				Results results = (Results) response.getObject();
 				LOGGER.trace("RECEIVING: {} <- {}", results, message.getJMSDestination());
 				exchanger.exchange(results, timeout, TimeUnit.MILLISECONDS);
 			}
 		} catch (JMSException e) {
                        LOGGER.warn("problem unpacking message {}: ", e, message);
 		} catch (InterruptedException e) {
 			// TODO: how should we deal with this...
 		} catch (TimeoutException e) {
                        LOGGER.warn("timed out waiting for exchange: {}", e, message);
 		}
 	}
 
 	@Override
 	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
 		ObjectMessage message = session.createObjectMessage();
 		Integer methodIndex = mapper.getKey(method);
 		if (methodIndex == null) {
 			// log.warn("unproxied method invoked: {}", method);
 			return method.invoke(this, args);
 		}
 		message.setObject(new Invocation(methodIndex, args));
 
 		// TODO: whether a method is to be used asynchronously should be stored with it to save runtime overhead...
 		boolean async = trueAsync && method.getReturnType().equals(Void.TYPE) && method.getExceptionTypes().length == 0;
 
 		if (async) {
 			LOGGER.trace("SENDING ASYNC: {} -> {}", method, destination);
 			producer.send(destination, message);
 			return null;
 		} else {
 			String correlationId = "" + count++;
 			LOGGER.trace(System.identityHashCode(this) + ": setting correlationId: {}:{}", System.identityHashCode(this), correlationId);
 			message.setJMSCorrelationID(correlationId);
 			message.setJMSReplyTo(resultsQueue);
 			Exchanger<Results> exchanger = new Exchanger<Results>();
 			correlationIdToResults.put(correlationId, exchanger);
 			LOGGER.trace("SENDING SYNC: {} -> {}", method, destination);
 			producer.send(destination, message);
 			long start = System.currentTimeMillis();
 			try {
 				Results results = exchanger.exchange(null, timeout, TimeUnit.MILLISECONDS);
 				Object value = results.getValue();
 				if (results.isException())
 					throw (Exception) value;
 				else
 					return value;
 			} catch (TimeoutException e) {
 				long elapsed = System.currentTimeMillis() - start;
 				correlationIdToResults.remove(correlationId);
 				LOGGER.warn("timeout was: {}", timeout);
 				LOGGER.warn("timed out, after " + elapsed + " millis, waiting for results from invocation: " + method + " on " + destination); // TODO: SLF4j-ise
 				throw e;
 			}
 		}
 
 	}
 
 	@Override
 	public String toString() {
 		return "<" + getClass().getSimpleName() + ": " + destination + ">";
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		// strip off proxy if necessary
 		if (object == null) {
 			return false;
 		} else {
 			Object that = Proxy.isProxyClass(object.getClass()) ? Proxy.getInvocationHandler(object) : object;
 			return (that instanceof SynchronousClient && this.destination.equals(((SynchronousClient) that).destination));
 		}
 	}
 
 	@Override
 	public  int hashCode() {
 		// we need to define hashCode as well as equals to keep findbugs happy...
 		return destination.hashCode();
 	}
 
 }
