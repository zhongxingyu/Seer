 /*******************************************************************************
 * Copyright (c) 2009 Composent, Inc. and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Composent, Inc. - initial API and implementation
 ******************************************************************************/
 package org.eclipse.ecf.examples.tests.remoteservices.dictionary.soap.client;
 
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.examples.remoteservices.dictionary.common.IDictionary;
 import org.eclipse.ecf.examples.remoteservices.dictionary.common.WordDefinition;
 import org.eclipse.ecf.remoteservice.IRemoteService;
 import org.eclipse.ecf.remoteservice.IRemoteServiceContainerAdapter;
 import org.eclipse.ecf.remoteservice.IRemoteServiceReference;
 import org.eclipse.ecf.tests.ECFAbstractTestCase;
 
 public class DictionaryTest extends ECFAbstractTestCase {
 
 	private static final String DICTIONARY_PROVIDER = "ecf.dictionary.soap.client";
 
 	IContainer container;
 	IRemoteServiceContainerAdapter containerAdapter;
 	
  	protected void setUp() throws Exception {
 		super.setUp();
  		container = getContainerFactory().createContainer(DICTIONARY_PROVIDER);
  		containerAdapter = (IRemoteServiceContainerAdapter) container.getAdapter(IRemoteServiceContainerAdapter.class);
 	}
  	
  	protected void tearDown() throws Exception {
  		super.tearDown();
  		containerAdapter = null;
  		container.dispose();
  		container = null;
  		getContainerManager().removeAllContainers();
  	}
  	
  	public void testDictionaryService() throws Exception {
  		IRemoteServiceReference[] refs = containerAdapter.getRemoteServiceReferences((ID) null, IDictionary.class.getName(), null);
  		assertNotNull(refs);
  		assertTrue(refs.length > 0);
  		IRemoteService remoteService = containerAdapter.getRemoteService(refs[0]);
  		// Get proxy
  		IDictionary dictionary = (IDictionary) remoteService.getProxy();
  		// Now call it
  		WordDefinition def = dictionary.define("abstruse");
  		
  		assertNotNull(def);
 		printWordDefinition(def);
  	}
 
	private void printWordDefinition(WordDefinition def) {
 		System.out.println("word='"+def.getWord()+"'");
 		String[] defs = def.getDefinitions();
 		for(int i=0; i < defs.length; i++) {
 			System.out.println("   def="+defs[i]);
 		}
 	}
 }
