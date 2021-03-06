 /*******************************************************************************
  * Copyright (c) 2006-2010 
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany 
  *      - initial API and implementation
  ******************************************************************************/
 package org.emftext.sdk.util;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.codegen.ecore.genmodel.GenClass;
 import org.eclipse.emf.codegen.ecore.genmodel.GenPackage;
 import org.emftext.sdk.concretesyntax.GenClassCache;
 
 /**
  * A utility class to work with EMF GenClasses.
  */
 public class GenClassUtil {
 
 	/**
 	 * Searches for 'genClass' in 'genClasses'. If a class with the
 	 * same qualified interface name is found, this method returns
 	 * true, otherwise false.
 	 * 
 	 * @param genClasses the collection of classes to search in
 	 * @param genClass the class to search for
 	 * @param genClassCache
 	 * @return
 	 */
 	public boolean contains(Collection<GenClass> genClasses,
 			GenClass genClass, GenClassCache genClassCache) {
		String genClassInterfaceName = genClassCache.getQualifiedInterfaceName(genClass);
 		for (GenClass next : genClasses) {
			String nextInterfaceName = genClassCache.getQualifiedInterfaceName(next);
			if (nextInterfaceName != null && nextInterfaceName.equals(genClassInterfaceName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the given class is neither abstract nor
 	 * an interface.
 	 * 
 	 * @param genClass
 	 * @return
 	 */
 	public boolean isConcrete(GenClass genClass) {
 		return !genClass.isAbstract() && !genClass.isInterface();
 	}
 
 	/**
 	 * Returns true if the given class is either abstract or
 	 * an interface.
 	 * 
 	 * @param genClass
 	 * @return
 	 */
 	public boolean isNotConcrete(GenClass genClass) {
 		return !isConcrete(genClass);
 	}
 
 	/**
 	 * Returns true if superClass is a superclass of subClass.
 	 * 
 	 * @param superClass
 	 * @param subClass
 	 * @param genClassCache
 	 * @return
 	 */
 	public boolean isSuperClass(GenClass superClass, GenClass subClass, GenClassCache genClassCache) {
 		List<GenClass> superClasses = subClass.getAllBaseGenClasses();
 		for (GenClass nextSuperclass : superClasses) {
 			if (genClassCache.getQualifiedInterfaceName(nextSuperclass).equals(genClassCache.getQualifiedInterfaceName(superClass))) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the code for a method call that obtains the EClass of the given
 	 * GenClass from the generated EPackage.
 	 * 
 	 * @param genClass
 	 * @return
 	 */
 	public String getAccessor(GenClass genClass) {
 		return genClass.getGenPackage().getQualifiedPackageInterfaceName() + ".eINSTANCE.get" + genClass.getClassifierAccessorName() + "()";
 	}
 
 	/**
 	 * Returns the code for a method call that creates an instance of the EClass 
 	 * of the given GenClass using the generated EFactory.
 	 * 
 	 * @param genClass
 	 * @return
 	 */
 	public String getCreateObjectCall(GenClass genClass, String qualifiedDummyEObjectClassName) {
 		GenPackage genPackage = genClass.getGenPackage();
 		if (Map.Entry.class.getName().equals(genClass.getEcoreClass().getInstanceClassName())) {
 			return "new " + qualifiedDummyEObjectClassName + "("+ genPackage.getQualifiedPackageClassName() + ".eINSTANCE.get" + genClass.getName() 
 					+ "(),\"" + genClass.getName() + "\")";
 	    } else {
 	    	return genPackage.getQualifiedFactoryInterfaceName() + ".eINSTANCE.create" + genClass.getName() + "()";
 	    }
 	}
 }
