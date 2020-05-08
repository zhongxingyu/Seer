 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests;
 
 import java.util.Calendar;
 
import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenClassifier;
 import org.eclipse.emf.codegen.ecore.genmodel.GenModel;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.gmf.bridge.genmodel.BasicGenModelAccess;
 
 /**
  * @author artem
  *
  */
 public class Utils {
 
 	/**
 	 * Create in-memory genmodel for provided (in-memory) domain model
 	 * @param aModel source model
 	 * @return initilized genModel, ready to run code generation
 	 */
 	public static GenModel createGenModel(EPackage aModel, String pluginID) {
 		BasicGenModelAccess gmAccess = new BasicGenModelAccess(aModel);
		IStatus s = gmAccess.createDummy();
		assert s.isOK();
 		GenModel genModel = gmAccess.model();
 		// not sure I need these
         genModel.setModelPluginID(pluginID);
         genModel.setModelDirectory("/" + pluginID + "/src/");
        genModel.setEditDirectory(genModel.getModelDirectory());
         return genModel;
 	}
 
 	public static GenClass findGenClass(GenModel genModel, String className) {
 		for (TreeIterator it = genModel.eAllContents(); it.hasNext();) {
 			Object next = it.next();
 			if (next instanceof GenClassifier == false) {
 				continue;
 			}
 			if (next instanceof GenClass) {
 				GenClass rc = (GenClass) next;
 				if (rc.getName().equals(className)) {
 					return rc;
 				}
 			}
 			it.prune();
 		}
 		return null;
 	}
 
 	/**
 	 * Tests need class matching using names as it's not always ok 
 	 * to compare ecore classes as done in 
 	 * {@link org.eclipse.gmf.bridge.genmodel.GenModelMatcher#findGenClass(EClass)} 
 	 */
 	public static GenClass findGenClass(GenModel genModel, EClass domainClass) {
 		return findGenClass(genModel, domainClass.getName());
 	}
 
 	public static String createUniquePluginID() {
 		Calendar c = Calendar.getInstance();
 		return "sample.d" + c.get(Calendar.DAY_OF_YEAR)+ ".h" + c.get(Calendar.HOUR_OF_DAY) + ".m" + c.get(Calendar.MINUTE);
 	}
 }
