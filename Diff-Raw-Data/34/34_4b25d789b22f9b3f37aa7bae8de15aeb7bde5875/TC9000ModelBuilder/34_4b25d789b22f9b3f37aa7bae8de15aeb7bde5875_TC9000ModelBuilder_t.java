 /**
  * Copyright (c) 2012 itemis AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Mark Broerkens - initial API and implementation
  * 
  */
 package org.eclipse.rmf.tests.reqif10.serialization.util;
 
 import java.math.BigInteger;
 
import org.eclipse.rmf.reqif10.ReqIF10Factory;
import org.eclipse.rmf.reqif10.SpecHierarchy;
 import org.eclipse.rmf.reqif10.SpecObject;
import org.eclipse.rmf.reqif10.Specification;
 import org.eclipse.rmf.tests.reqif10.serialization.uc001.tc1000.TC1000SimpleContentModelBuilder;
 
 @SuppressWarnings("nls")
 public class TC9000ModelBuilder extends TC1000SimpleContentModelBuilder {
 	final static String LAST_CHANGE_STRING = "2012-04-07T01:51:37.112+02:00";
 
 	final int numberOfSpecObjects;
 
 	public TC9000ModelBuilder(String lastChangeDateString, String identifier, String longName, int numberOfSpecObjects) throws Exception {
 		super(lastChangeDateString, identifier, longName);
 		this.numberOfSpecObjects = numberOfSpecObjects;
 	}
 
 	@Override
 	public void createSpecObjects() throws Exception {
 		for (int i = 0; i < numberOfSpecObjects; i++) {
 
			SpecObject specObject = createTC1000SpecObject("ID_TC9000_SpecObject" + i, toDate(LAST_CHANGE_STRING), true, false,
 					new BigInteger("5000"), "Plain", 1234.5, toDate(LAST_CHANGE_STRING), enumValueYellow);
 
 			getReqIF().getCoreContent().getSpecObjects().add(specObject);
 		}
 	}
 
 	@Override
	public void createSpecifications() throws Exception {
		Specification specification = ReqIF10Factory.eINSTANCE.createSpecification();
		specification.setIdentifier("ID_TC1000_Specification");
		specification.setType(specificationType);
		specification.setLastChange(toDate(LAST_CHANGE_STRING));

		for (SpecObject specObject : getReqIF().getCoreContent().getSpecObjects()) {
			SpecHierarchy specHierarchy = ReqIF10Factory.eINSTANCE.createSpecHierarchy();
			specHierarchy.setIdentifier(specObject.getIdentifier() + "_SpecHierarchy");
			specHierarchy.setLastChange(toDate(LAST_CHANGE_STRING));
			specHierarchy.setObject(specObject);
			specification.getChildren().add(specHierarchy);
		}

		getReqIF().getCoreContent().getSpecifications().add(specification);

	}

	@Override
 	public void createSpecRelations() throws Exception {
 		// TODO Auto-generated method stub
 		super.createSpecRelations();
 	}
 
 }
