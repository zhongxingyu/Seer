 /*
  * Copyright (c) 2006, 2009 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Michael Golubev (Borland) - initial API and implementation
  */
 
 package org.eclipse.uml2.diagram.common.conventions;
 
 import org.eclipse.uml2.diagram.common.Messages;
 import org.eclipse.uml2.uml.AggregationKind;
 import org.eclipse.uml2.uml.Association;
 import org.eclipse.uml2.uml.Property;
 import org.eclipse.uml2.uml.Type;
 
 public class AssociationEndConvention {
 
 	public static Property getMemberEnd(Association association, boolean sourceNotTarget) {
		if (sourceNotTarget && association.getMemberEnds().size() > 0) {
			return (Property) association.getMemberEnds().get(0);
		}
		if (!sourceNotTarget && association.getMemberEnds().size() > 1) {
			return (Property) association.getMemberEnds().get(1);
		}
		return null;
 	}
 
 	public static Property getSourceEnd(Association association) {
 		return getMemberEnd(association, true);
 	}
 
 	public static Property getTargetEnd(Association association) {
 		return getMemberEnd(association, false);
 	}
 
 	public static Association createAssociation(Type diagramSource, Type diagramTarget, boolean setNavigability) {
 		//due to association end conventions (see AssociationEndConvention) 
 		//we need to have member end of type SourceType to be the first one created
 		//thus, we are calling UML2 createAssociation() in opposite order
 		Association newElement = diagramTarget.createAssociation(//
 				false, AggregationKind.NONE_LITERAL, Messages.AssociationEndConvention_source_end_name, 1, 1, // 
 				diagramSource, setNavigability, AggregationKind.NONE_LITERAL, Messages.AssociationEndConvention_target_end_name, 1, 1);
 
 		//also we need to have associations stored at the same package as a source (not target like it is done in UML), scr #264509
 		if (diagramSource.getNearestPackage() != diagramTarget.getNearestPackage() && newElement.getNearestPackage() != diagramSource.getNearestPackage()) {
 			diagramSource.getNearestPackage().getOwnedTypes().add(newElement);
 		}
 		return newElement;
 	}
 }
