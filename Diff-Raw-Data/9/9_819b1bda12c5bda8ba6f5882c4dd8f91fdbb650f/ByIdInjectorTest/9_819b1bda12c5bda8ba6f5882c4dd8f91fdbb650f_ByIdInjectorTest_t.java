 /*******************************************************************************
  * Copyright (c) 2008 Ralf Ebert
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Ralf Ebert - initial API and implementation
  *******************************************************************************/
 package com.swtxml.swt.byid;
 
 import static org.easymock.EasyMock.createMock;
 import static org.easymock.EasyMock.expect;
 import static org.easymock.EasyMock.replay;
 import static org.easymock.EasyMock.verify;
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import com.swtxml.adapter.IIdResolver;
 
 public class ByIdInjectorTest {
 
 	@Test
 	public void testInject() {
 
 		IIdResolver idResolver = createMock(IIdResolver.class);
 		expect(idResolver.getById("baseNumber", Integer.class)).andReturn(5);
 		expect(idResolver.getById("test", String.class)).andReturn("Hallo");
 
 		ByIdView view = new ByIdView();
 
 		replay(idResolver);
 		new ByIdInjector().inject(view, idResolver);
 
		assertEquals(new Integer(5), view.getBaseNumberX());
 		assertEquals("Hallo", view.getTestX());
 		verify(idResolver);
 	}
 
 }
