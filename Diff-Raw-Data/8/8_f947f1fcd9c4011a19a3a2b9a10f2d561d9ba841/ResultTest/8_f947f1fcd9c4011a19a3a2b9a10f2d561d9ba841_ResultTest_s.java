 package com.surevine.profileserver.packetprocessor.iq.namespace.command;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.concurrent.LinkedBlockingQueue;
 
 import junit.framework.Assert;
 
 import org.dom4j.Element;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mockito;
 import org.xmpp.packet.IQ;
 import org.xmpp.packet.JID;
 import org.xmpp.packet.Packet;
 
 import com.surevine.profileserver.db.DataStore;
 import com.surevine.profileserver.helpers.IQTestHandler;
 import com.surevine.profileserver.packetprocessor.iq.namespace.register.Set;
 
 public class ResultTest extends IQTestHandler {
 
 	private DataStore dataStore;
 	private Result result;
 	private LinkedBlockingQueue<Packet> queue;
 	private IQ resultRequest;
 	private DataStore dataStoreSpy;
 
 	@Before
 	public void setUp() throws Exception {
 		dataStore = Mockito.mock(DataStore.class);
 
 		queue = new LinkedBlockingQueue<Packet>();
 		result = new Result(queue, readConf(), dataStore);
 		resultRequest = readStanzaAsIq("/command/result");
 	}
 
 	@Test
 	public void testIncorrectFromValueDoesNothing() throws Exception {
 		resultRequest.setFrom(new JID("not.from.here"));
 
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.never()).hasOwner(
 				Mockito.any(JID.class));
 	}
 
 	@Test
 	public void testIncorrectNodeDoesNothing() throws Exception {
 
 		Element command = resultRequest.getElement().element("command");
 		command.remove(command.element("x"));
 		command.addAttribute("node", "http://incorrect.node");
 
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.never()).hasOwner(
 				Mockito.any(JID.class));
 	}
 
 	@Test
 	public void testNotCompletedStatusDoesNothing() throws Exception {
 		Element command = resultRequest.getElement().element("command");
 		command.addAttribute("status", "pending");
 
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.never()).hasOwner(
 				Mockito.any(JID.class));
 	}
 
 	@Test
 	public void testMissingFormElementDoesNothing() throws Exception {
 		Element command = resultRequest.getElement().element("command");
 		command.remove(command.element("x"));
 
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.never()).hasOwner(
 				Mockito.any(JID.class));
 	}
 
 	@Test
 	public void testIncorrectFormTypeDoesNothing() throws Exception {
 		Element form = resultRequest.getElement().element("command")
 				.element("x");
 		List<Element> elements = (List<Element>) form.elements("field");
 		for (Element f : elements) {
 			if (f.attributeValue("var").equals("FORM_TYPE")) {
 				f.element("value").setText("http://wrong/protocol");
 			}
 		}
 
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.never()).hasOwner(
 				Mockito.any(JID.class));
 	}
 
 	@Test
 	public void testIncorrectAccountJidsValueChecksOnce() throws Exception {
 		Element form = resultRequest.getElement().element("command")
 				.element("x");
 		List<Element> elements = (List<Element>) form.elements("field");
 		for (Element f : elements) {
 			if (f.attributeValue("var").equals("accountjids")) {
 				f.element("value").setText("someone@not-from.here");
 			}
 		}
 
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.times(1)).hasOwner(
 				Mockito.any(JID.class));
 	}
 	
 	@Test
 	public void testWriteActionCalled() throws Exception {
 		Mockito.when(dataStore.hasOwner(Mockito.any(JID.class))).thenReturn(true);
 		
 		result.process(resultRequest);
 
 		Mockito.verify(dataStore, Mockito.times(1)).hasOwner(Mockito.any(JID.class));
		Mockito.verify(dataStore, Mockito.times(4)).addRosterMember(
				Mockito.any(JID.class), 
				Mockito.any(String.class),
				Mockito.any(JID.class));
 	}
 }
