 /*****************************************************************************
  * Copyright (c) 2012 CEA LIST.
  *
  *    
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the CeCILL-C Free Software License v1.0
  * which accompanies this distribution, and is available at
  * http://www.cecill.info/licences/Licence_CeCILL-C_V1-en.html
  *
  * Contributors:
  *  Saadia DHOUIB (CEA LIST) - Initial API and implementation
  *
  *****************************************************************************/
 package org.eclipse.papyrus.robotml.diagram.componentdef.provider;
 
 import org.eclipse.gef.EditPart;
 import org.eclipse.papyrus.robotml.diagram.componentdef.edit.policy.StereotypeNodeLabelDisplayEditPolicy;
 import org.eclipse.papyrus.robotml.diagram.componentdef.provider.ComponentdefDiagramEditPolicyProvider;
 import org.eclipse.papyrus.uml.diagram.common.editparts.NamedElementEditPart;
 import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeCompartmentEditPolicy;
 import org.eclipse.papyrus.uml.diagram.common.editpolicies.AppliedStereotypeLabelDisplayEditPolicy;
 
 
 public class CustomComponentDefDiagramEditPolicyProvider extends ComponentdefDiagramEditPolicyProvider {
 
 	@Override
 	public void createEditPolicies(EditPart editPart) {
 		super.createEditPolicies(editPart);
		editPart.installEditPolicy(AppliedStereotypeLabelDisplayEditPolicy.STEREOTYPE_LABEL_POLICY, new StereotypeNodeLabelDisplayEditPolicy());
		if(editPart instanceof NamedElementEditPart ){
            editPart.installEditPolicy(AppliedStereotypeLabelDisplayEditPolicy.STEREOTYPE_LABEL_POLICY, new AppliedStereotypeCompartmentEditPolicy());
     }
 
 	}
 	
 	
 	
 	
 }
