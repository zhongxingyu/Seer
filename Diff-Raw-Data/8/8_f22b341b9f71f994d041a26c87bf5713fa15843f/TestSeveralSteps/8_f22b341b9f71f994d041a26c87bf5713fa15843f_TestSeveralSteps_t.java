 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.locationpaths.tests;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.emf.ecore.impl.EObjectImpl;
import org.eclipse.wazaabi.engine.locationpaths.model.Pointer;
 import org.eclipse.wazaabi.engine.locationpaths.nonosgi.LocationPathsHelper;
 import org.eclipse.wazaabi.engine.locationpaths.runtime.Evaluator;
 import org.eclipse.wazaabi.engine.locationpaths.runtime.LocationSelector;
 import org.junit.Before;
 import org.junit.Test;
 
 public class TestSeveralSteps extends AbstractTest {
 
 	public static class EObjectWithData extends EObjectImpl {
 
 		private HashMap<String, Object> data = new HashMap<String, Object>();
 
 		public Object get(String key) {
 			return data.get(key);
 		}
 
 		public void set(String key, Object value) {
 			data.put(key, value);
 		}
 	};
 
 	@Before
 	public void setUp() throws Exception {
 		LocationPathsHelper.init();
 	}
 
 	@Test
 	public void testInitialContextWithEClassifier() {
 		String path = "eClassifier(\"" + SUB_EPACKAGE1_NSURI + "\", \"" + SUB_ECLASS1_NAME + "\")/&eAllAttributes[1]/@name"; //$NON-NLS-1$
 		System.out.println("testing \"" + path + "\""); //$NON-NLS-1$ $NON-NLS-2$
 
 		@SuppressWarnings("unchecked")
		List<Pointer<?>> result = LocationSelector.select(getTestEPackage(),
 				path);
 		assertNotNull(result);
 		assertEquals(1, result.size());
 
 		List<?> objects = Evaluator.evaluate(result.get(0));
 		assertNotNull(objects);
 		assertEquals(1, objects.size());
 		assertEquals(SUB_ECLASS1_ATTR2_NAME, objects.get(0));
 	}
 
 	@Test
 	public void testSeveralSteps1() {
 		String path = "EPackage/EClass/&eAllAttributes[1]/attribute::name"; //$NON-NLS-1$
 		System.out.println("testing \"" + path + "\""); //$NON-NLS-1$ $NON-NLS-2$
 
 		@SuppressWarnings("unchecked")
		List<Pointer<?>> result = LocationSelector.select(getTestEPackage(),
 				path);
 		assertNotNull(result);
 
 		// we expect on list of EMFPointers
 		assertEquals(1, result.size());
 
 		List<?> objects = Evaluator.evaluate(result.get(0));
 		assertNotNull(objects);
 		assertEquals(1, objects.size());
 
 		assertEquals(SUB_ECLASS1_ATTR2_NAME, objects.get(0));
 	}
 
 }
