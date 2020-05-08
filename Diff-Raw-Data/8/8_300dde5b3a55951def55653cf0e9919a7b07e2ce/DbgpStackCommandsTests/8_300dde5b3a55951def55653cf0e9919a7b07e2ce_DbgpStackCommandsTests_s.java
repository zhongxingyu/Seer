 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.dbgp.tests;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import org.eclipse.dltk.dbgp.DbgpRequest;
 import org.eclipse.dltk.dbgp.IDbgpStackLevel;
import org.eclipse.dltk.dbgp.commands.IDbgpStatckCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.dbgp.internal.commands.DbgpStackCommands;
 import org.w3c.dom.Element;
 
 public class DbgpStackCommandsTests extends DbgpProtocolTests {
 	protected Element getStackDepthResponse(int transaction_id, int depth)
 			throws IOException {
 		String xml = getResourceAsString("get_stack_depth.xml");
 
 		xml = MessageFormat.format(xml, new Object[] {
 				Integer.toString(transaction_id), Integer.toString(depth) });
 
 		return parseResponse(xml);
 	}
 
 	protected Element getStackGetResponse(int transaction_id)
 			throws IOException {
 		String xml = getResourceAsString("stack_get.xml");
 
 		xml = MessageFormat.format(xml, new Object[] { Integer
 				.toString(transaction_id) });
 
 		return parseResponse(xml);
 	}
 
 	protected Element getStackLevelsResponse() {
 		return null;
 	}
 
 	public void testStackDepth() throws Exception {
 		final Element response = getStackDepthResponse(0, 3);
 
		IDbgpStatckCommands commands = new DbgpStackCommands(
 				new AbstractCommunicator() {
 					public Element communicate(DbgpRequest request)
 							throws DbgpException {
 
 						assertEquals(1, request.optionCount());
 						assertTrue(request.hasOption("-i"));
 
 						return response;
 					}
 				});
 
 		int depth = commands.getStackDepth();
 		assertEquals(3, depth);
 	}
 
 	public void testGetStackLevel() throws Exception {
 		final Element response = getStackGetResponse(0);
 
		IDbgpStatckCommands commands = new DbgpStackCommands(
 				new AbstractCommunicator() {
 					public Element communicate(DbgpRequest request)
 							throws DbgpException {
 
 						assertTrue(request.hasOption("-i"));
 
 						return response;
 					}
 				});
 
 		IDbgpStackLevel level = commands.getStackLevel(0);
 		assertEquals(0, level.getLevel());
 		assertEquals(8, level.getLineNumber());
 	}
 }
