 /*
  * Source code in 3rd-party is licensed and owned by their respective
  * copyright holders.
  *
  * All other source code is copyright Tresys Technology and licensed as below.
  *
  * Copyright (c) 2012 Tresys Technology LLC, Columbia, Maryland, USA
  *
  * This software was developed by Tresys Technology LLC
  * with U.S. Government sponsorship.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.tresys.jalop.jnl.impl;
 
 import static org.junit.Assert.assertEquals;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.soap.MimeHeaders;
 
 import mockit.Mocked;
 import mockit.NonStrictExpectations;
 import mockit.VerificationsInOrder;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.beepcore.beep.core.BEEPException;
 import org.beepcore.beep.core.Channel;
 import org.beepcore.beep.core.InputDataStream;
 import org.beepcore.beep.core.InputDataStreamAdapter;
 import org.beepcore.beep.core.MessageMSG;
import org.beepcore.beep.core.OutputDataStream;
 import org.beepcore.beep.core.Session;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.tresys.jalop.jnl.DigestPair;
 import com.tresys.jalop.jnl.DigestStatus;
 import com.tresys.jalop.jnl.Publisher;
 import com.tresys.jalop.jnl.RecordType;
 import com.tresys.jalop.jnl.exceptions.JNLException;
 import com.tresys.jalop.jnl.impl.messages.DigestMessage;
 import com.tresys.jalop.jnl.impl.messages.Utils;
 import com.tresys.jalop.jnl.impl.publisher.PublisherSessionImpl;
 
 public class DigestRequestHandlerTest {
 
 	// Needed to mock static functions in the Utils class.
     @Mocked
     private Utils utils;
 
 	@Before
 	public void setUp() {
 		// Disable logging so the build doesn't get spammed.
 		Logger.getRootLogger().setLevel(Level.OFF);
 	}
 
 	@Test
 	public void testDigestRequestHandlerWorks(final ContextImpl contextImpl, final PublisherSessionImpl sess) {
 		final DigestRequestHandler drh = new DigestRequestHandler(RecordType.Audit, contextImpl, sess);
 		assertEquals(contextImpl, drh.contextImpl);
 		assertEquals(RecordType.Audit, drh.recordType);
 		assertEquals(sess, drh.sess);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testReceiveMSGWorks(final ContextImpl contextImpl, final MessageMSG msg,
 			final Publisher publisher, final DigestMessage digestMessage,
 			final PublisherSessionImpl publisherSessionImpl, final Session sess,
			final InputDataStream ids, final InputDataStreamAdapter isa, final Channel channel,
			final OutputDataStream ods)
 			throws JNLException, SecurityException, NoSuchMethodException, InstantiationException,
 			IllegalAccessException, InvocationTargetException, BEEPException {
 
 		final DigestRequestHandler drh = new DigestRequestHandler(RecordType.Audit, contextImpl, publisherSessionImpl);
 		final Constructor<DigestMessage> constructor = DigestMessage.class.getDeclaredConstructor(Map.class, MimeHeaders.class);
 		constructor.setAccessible(true);
 		final DigestMessage dm = constructor.newInstance(new HashMap<String, String>(), new MimeHeaders());
 		final Map<String, String> map = new HashMap<String, String>();
 		map.put("serial1", "313233343536");
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream(); result = ids;
                 ids.getInputStream(); result = isa;
                 Utils.processDigestMessage(isa); result = dm;
 				msg.getChannel(); result = channel;
                 channel.getSession(); result = sess;
                 dm.getMap(); result = map;
                 publisherSessionImpl.fetchAndRemoveDigest(anyString); result = "123456".getBytes();
                 contextImpl.getPublisher(); result = publisher;
                Utils.createDigestResponse((Map<String, DigestStatus>) any); result = ods;
 			}
 		};
 
 		drh.receiveMSG(msg);
 
 		new VerificationsInOrder() {
 			{
 				publisher.notifyPeerDigest(publisherSessionImpl, (Map<String, DigestPair>) any);
				msg.sendRPY(ods);
 			}
 		};
 	}
 
 	@SuppressWarnings("unchecked")
 	@Test
 	public void testReceiveMSGSetsInvalid(final ContextImpl contextImpl, final MessageMSG msg,
 			final Publisher publisher, final DigestMessage digestMessage,
 			final PublisherSessionImpl publisherSessionImpl, final Session sess,
 			final InputDataStream ids, final InputDataStreamAdapter isa, final Channel channel)
 			throws JNLException, SecurityException, NoSuchMethodException, InstantiationException,
 			IllegalAccessException, InvocationTargetException, BEEPException {
 
 		final DigestRequestHandler drh = new DigestRequestHandler(RecordType.Audit, contextImpl, publisherSessionImpl);
 		final Constructor<DigestMessage> constructor = DigestMessage.class.getDeclaredConstructor(Map.class, MimeHeaders.class);
 		constructor.setAccessible(true);
 		final DigestMessage dm = constructor.newInstance(new HashMap<String, String>(), new MimeHeaders());
 		final Map<String, String> map = new HashMap<String, String>();
 		map.put("serial1", "9876543210");
 
 		new NonStrictExpectations() {
 			{
 				msg.getDataStream(); result = ids;
                 ids.getInputStream(); result = isa;
                 Utils.processDigestMessage(isa); result = dm;
 				msg.getChannel(); result = channel;
                 channel.getSession(); result = sess;
                 dm.getMap(); result = map;
                 publisherSessionImpl.fetchAndRemoveDigest(anyString); result = "123456".getBytes();
                 contextImpl.getPublisher(); result = publisher;
 			}
 		};
 
 		drh.receiveMSG(msg);
 
 		new VerificationsInOrder() {
 			{
 				new DigestPairImpl(anyString, (byte[])any, (byte[])any, DigestStatus.Invalid);
 				publisher.notifyPeerDigest(publisherSessionImpl, (Map<String, DigestPair>) any);
 			}
 		};
 	}
 
 }
