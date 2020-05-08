 /*******************************************************************************
  * Copyright (c) 2011 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.pror.reqif10.editor.agilegrid;
 
 import static org.junit.Assert.assertEquals;
 
 import java.net.URISyntaxException;
 
import org.eclipse.emf.common.util.EList;
 import org.eclipse.rmf.pror.reqif10.configuration.ConfigurationFactory;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrSpecViewConfiguration;
 import org.eclipse.rmf.pror.reqif10.configuration.ProrToolExtension;
 import org.eclipse.rmf.pror.reqif10.testframework.AbstractItemProviderTest;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
import org.eclipse.rmf.reqif10.SpecRelation;
 import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.reqif10.common.util.ReqIFToolExtensionUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * Tests {@link ProrAgileGridContentProvider}
  * 
  * @author jastram
  */
 public abstract class AbstractContentProviderTests extends AbstractItemProviderTest {
 
 	protected ProrAgileGridContentProvider contentProvider;
 	protected Specification specification;
 	protected ProrSpecViewConfiguration specViewConfig;
 	protected ReqIF reqif;
 	protected SpecObject specObject;
	protected EList<SpecRelation> specRelations;
 	protected SpecHierarchy specHierarchy;
 
 	@Before
 	public void setup() throws URISyntaxException {
 		reqif = this.getTestReqif("simple.reqif");
 		specification = reqif.getCoreContent().getSpecifications().get(0);
 		specObject = reqif.getCoreContent().getSpecObjects().get(0);
 		specHierarchy = specification.getChildren().get(0);
 		
 		
 		// Build up the data structures that hold specViewConfig
 		ProrToolExtension prorToolExtension = ConfigurationFactory.eINSTANCE.createProrToolExtension();
 		specViewConfig = ConfigurationFactory.eINSTANCE.createProrSpecViewConfiguration();
 		prorToolExtension.getSpecViewConfigurations().add(specViewConfig);
 		ReqIFToolExtensionUtil.addToolExtension(reqif, prorToolExtension);
 		
 		contentProvider = new ProrAgileGridContentProvider(specification, specViewConfig);
 	}
 	
 	@After
 	public void teardownAbstractItemProviderTest() {
 		reqif = null;
 		specification = null;
 		specViewConfig = null;
 		contentProvider = null;
 	}
 	
 	
 	@Test
 	public void testInitialRowCount() {
 		assertEquals(1, contentProvider.getRowCount());
 	}
 
 	@Test(expected = IndexOutOfBoundsException.class)
 	public void testNonexistingRow() {
 		contentProvider.getContentAt(1, 0);
 	}
 
 	@Test
 	public void testSpecViewConfigContent() {
 		assertEquals(0, specViewConfig.getColumns().size());
 	}
 	
 
 }
