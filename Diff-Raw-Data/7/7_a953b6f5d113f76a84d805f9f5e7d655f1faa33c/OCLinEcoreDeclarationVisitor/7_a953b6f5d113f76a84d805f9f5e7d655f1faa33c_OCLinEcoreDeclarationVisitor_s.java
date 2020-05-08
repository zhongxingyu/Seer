 /**
  * <copyright>
  *
  * Copyright (c) 2010 E.D.Willink and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     E.D.Willink - initial API and implementation
  *
  * </copyright>
  *
  * $Id: OCLinEcoreDeclarationVisitor.java,v 1.8 2011/05/14 10:38:08 ewillink Exp $
  */
 package org.eclipse.ocl.examples.xtext.oclinecore.pivot2cs;
 
 import org.eclipse.jdt.annotation.NonNull;
 import org.eclipse.ocl.examples.pivot.Constraint;
 import org.eclipse.ocl.examples.pivot.ExpressionInOCL;
 import org.eclipse.ocl.examples.pivot.OCLExpression;
 import org.eclipse.ocl.examples.pivot.OpaqueExpression;
 import org.eclipse.ocl.examples.pivot.ValueSpecification;
 import org.eclipse.ocl.examples.pivot.prettyprint.PrettyPrinter;
 import org.eclipse.ocl.examples.pivot.utilities.PivotUtil;
 import org.eclipse.ocl.examples.xtext.base.baseCST.ElementCS;
 import org.eclipse.ocl.examples.xtext.base.baseCST.SpecificationCS;
 import org.eclipse.ocl.examples.xtext.base.pivot2cs.Pivot2CSConversion;
 import org.eclipse.ocl.examples.xtext.essentialocl.pivot2cs.EssentialOCLDeclarationVisitor;
 import org.eclipse.ocl.examples.xtext.oclinecore.oclinEcoreCST.OCLinEcoreCSTPackage;
 import org.eclipse.ocl.examples.xtext.oclinecore.oclinEcoreCST.OCLinEcoreConstraintCS;
 import org.eclipse.ocl.examples.xtext.oclinecore.oclinEcoreCST.OCLinEcoreSpecificationCS;
 
 public class OCLinEcoreDeclarationVisitor extends EssentialOCLDeclarationVisitor
 {
 	public OCLinEcoreDeclarationVisitor(@NonNull Pivot2CSConversion context) {
 		super(context);
 	}
 
 	@Override
 	public ElementCS visitConstraint(@NonNull Constraint object) {
 		OCLinEcoreConstraintCS csElement = context.refreshNamedElement(OCLinEcoreConstraintCS.class, OCLinEcoreCSTPackage.Literals.OC_LIN_ECORE_CONSTRAINT_CS, object);
 		csElement.setStereotype(object.getStereotype());
 		csElement.setCallable(object.isCallable());
 		ValueSpecification specification = object.getSpecification();
 		if (specification != null) {
 			csElement.setSpecification(context.visitDeclaration(SpecificationCS.class, specification));
 			if (specification instanceof OpaqueExpression) {		// FIXME ExpressionInOCL too??
 				OpaqueExpression opaqueExpression = (OpaqueExpression)specification;
 				String message = PivotUtil.getMessage(opaqueExpression);
 				if ((message != null) && (message.length() > 0)) {
 					OCLinEcoreSpecificationCS csMessageElement = context.refreshElement(OCLinEcoreSpecificationCS.class, OCLinEcoreCSTPackage.Literals.OC_LIN_ECORE_SPECIFICATION_CS, opaqueExpression);
 					csMessageElement.setExprString(message);
 					csElement.setMessageSpecification(csMessageElement);
 				}
 			}
 		}
 		return csElement;
 	}
 
 	@Override
 	public ElementCS visitExpressionInOCL(@NonNull ExpressionInOCL object) {
 		OCLinEcoreSpecificationCS csElement = context.refreshElement(OCLinEcoreSpecificationCS.class, OCLinEcoreCSTPackage.Literals.OC_LIN_ECORE_SPECIFICATION_CS, object);
 //		csElement.setOwnedExpression(context.visitDeclaration(ExpCS.class, object.getBodyExpression()));
 		OCLExpression bodyExpression = object.getBodyExpression();
 		if (bodyExpression != null) {
 			String body = PrettyPrinter.print(bodyExpression);
 			csElement.setExprString(body);
 		}
 		return csElement;
 	}
 
 	@Override
 	public ElementCS visitOpaqueExpression(@NonNull OpaqueExpression object) {
 		String body = PivotUtil.getBody(object);
 		if (body == null) {
 			return null;
 		}
 		OCLinEcoreSpecificationCS csElement = context.refreshElement(OCLinEcoreSpecificationCS.class, OCLinEcoreCSTPackage.Literals.OC_LIN_ECORE_SPECIFICATION_CS, object);
 		csElement.setExprString(body);
 		return csElement;
 	}	
 }
