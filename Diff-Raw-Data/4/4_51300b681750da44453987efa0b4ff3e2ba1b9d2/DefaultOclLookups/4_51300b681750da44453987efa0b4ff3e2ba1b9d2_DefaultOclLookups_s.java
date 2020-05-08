 /*
  * Copyright (c) 2006 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 
 package org.eclipse.uml2.diagram.parser.lookup;
 
 public class DefaultOclLookups {
 
 	public static final String DEFAULT_TYPE_LOOKUP = "" + // 
 			" let pakkage : Package = self.getNearestPackage() in " + // 
 			" let siblings : Set(Type) = pakkage.ownedType in " + // 
 			" let imports : Bag(Type) = " + //
 			" 		pakkage.elementImport->select( " + //
 			" 			importedElement.oclIsKindOf(Type) " + //
 			" 			and (importedElement.oclIsKindOf(Class) implies not importedElement.oclAsType(Class).isMetaclass()) " + //
 			" 		)->collect(" + //
 			"			importedElement.oclAsType(Type)" + //
 			"		) in " + // 
			" Set{}->union(siblings)->union(imports->asSet()) ";
 
 }
