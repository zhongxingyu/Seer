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
 
 package org.eclipse.rmf.reqif10.provider;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.net.URISyntaxException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.edit.provider.IItemLabelProvider;
 import org.eclipse.emf.edit.provider.ItemProviderAdapter;
 import org.eclipse.rmf.reqif10.ReqIF;
 import org.eclipse.rmf.reqif10.ReqIF10Factory;
 import org.eclipse.rmf.reqif10.ReqIF10Package;
 import org.eclipse.rmf.reqif10.SpecElementWithAttributes;
 import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
 import org.eclipse.rmf.reqif10.SpecType;
 import org.eclipse.rmf.reqif10.pror.provider.VirtualSpecObjectItemProvider;
 import org.eclipse.rmf.reqif10.pror.util.ProrUtil;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * A test case for the model object '<em><b>Spec Object</b></em>'.
  */
 public class SpecObjectTest extends SpecElementWithAttributesTest {
 
 	/**
 	 * Returns the fixture for this Spec Object test case.
 	 */
 	@Override
 	protected SpecObject getFixture() {
 		return (SpecObject)fixture;
 	}
 
 	/**
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Before
 	public void setUp() throws Exception {
 		setFixture(ReqIF10Factory.eINSTANCE.createSpecObject());
 		setupReqif();
 	}
 
 	/**
 	 * @see junit.framework.TestCase#tearDown()
 	 */
 	@After
 	public void tearDown() throws Exception {
 		setFixture(null);
 	}
 	
 	
 	private ReqIF reqif;
 	private SpecObject specObject;
 	private SpecHierarchy specHierarchy;
 
 	@Before
 	public void setupReqif() throws URISyntaxException {
 		reqif = getTestReqif("simple.reqif");
 		specObject = reqif.getCoreContent().getSpecObjects().get(0);
 		specHierarchy = reqif.getCoreContent().getSpecifications().get(0).getChildren().get(0);
 		// Just to make sure that the test file is correct
 		assertEquals(specObject, specHierarchy.getObject());
 	}
 	
 	/**
 	 * Ensure that the SpecObject is notified if the AttributeValue changes.
 	 */
 	@Test public void testSpecObjectNotificationValueChanged() {
 		ItemProviderAdapter ip = getItemProvider(specObject);
 		ip.addListener(listener);
 		assertEquals(0, notifications.size());		
 		ProrUtil.setTheValue(specObject.getValues().get(0), "newDescription", editingDomain);
 		assertEquals(2, notifications.size());
 		assertEquals(specObject, notifications.get(0).getNotifier());
 		assertEquals(ReqIF10Package.Literals.SPEC_ELEMENT_WITH_ATTRIBUTES__VALUES, notifications.get(0).getFeature());
 	}
 
 	/**
 	 * Ensure that the SpecHierarchy is notified if the AttributeValue changes.
 	 */
 	@Test public void testSpecHierarchyNotificationValueChanged() {
 		adapterFactory.adapt(specObject, IItemLabelProvider.class);
 		ItemProviderAdapter ip = getItemProvider(specHierarchy);
 		ip.addListener(listener);
 		assertEquals(0, notifications.size());		
 		ProrUtil.setTheValue(specObject.getValues().get(0), "newDescription", editingDomain);
		assertEquals(2, notifications.size());
 		assertEquals(specHierarchy, notifications.get(0).getNotifier());
 		assertEquals(ReqIF10Package.Literals.SPEC_HIERARCHY__OBJECT, notifications.get(0).getFeature());
 	}
 
 	@Test
 	public void testParentIsVirtual() throws URISyntaxException {
 		reqif = getTestReqif("simple.reqif");
 		// Required for generating the Virtual Element lazily.
 		getItemProvider(reqif.getCoreContent()).getChildren(reqif.getCoreContent());
 
 		specObject = reqif.getCoreContent().getSpecObjects().get(0);
 		ItemProviderAdapter ip = ProrUtil.getItemProvider(adapterFactory, specObject);
 		assertTrue(ip.getParent(specObject) instanceof VirtualSpecObjectItemProvider);
 	}
 
 	@Override
 	public void addFixtureToReqIf(ReqIF reqif) {
 		reqif.getCoreContent().getSpecObjects().add(getFixture());
 	}
 	
 	@Override
 	protected Set<String> getStandardPropertyNames() {
 		return new HashSet<String>(Arrays.asList(STANDARD_PROPERTIES));
 	}
 	
 	@Override
 	protected void setFixtureType(SpecElementWithAttributes specElement,
 			SpecType specType) {
 		setViaCommand(specElement, ReqIF10Package.Literals.SPEC_OBJECT__TYPE, specType);
 	}
 
 	@Override
 	protected SpecType getSpecTypeInstance() {
 		return ReqIF10Factory.eINSTANCE.createSpecObjectType();
 	}
 
 	@Override
 	protected EStructuralFeature getParentFeature() {
 		return ReqIF10Package.eINSTANCE.getReqIFContent_SpecObjects();
 	}
 
 } //SpecObjectTest
