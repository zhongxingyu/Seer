 /**
  * Copyright (c) 2006-2010
  * Software Technology Group, Dresden University of Technology
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Software Technology Group - TU Dresden, Germany
  *      - initial API and implementation
  */
 package org.reuseware.coconut.compositionprogram.util;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.reuseware.coconut.compositionprogram.CompositionLink;
 import org.reuseware.coconut.compositionprogram.CompositionProgram;
 import org.reuseware.coconut.compositionprogram.CompositionprogramPackage;
 import org.reuseware.coconut.compositionprogram.DerivedCompositionProgram;
 import org.reuseware.coconut.compositionprogram.FragmentInstance;
 import org.reuseware.coconut.compositionprogram.PhysicalCompositionProgram;
 import org.reuseware.coconut.compositionprogram.PortInstance;
 import org.reuseware.coconut.compositionprogram.Setting;
 import org.reuseware.coconut.fragment.AddressablePoint;
 import org.reuseware.coconut.fragment.HeterogeneousPort;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Validator</b> for the model.
  * <!-- end-user-doc -->
  * @see org.reuseware.coconut.compositionprogram.CompositionprogramPackage
  * @generated
  */
 public class CompositionprogramValidator extends EObjectValidator
 {
   /**
    * The cached model package
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public static final CompositionprogramValidator INSTANCE = new CompositionprogramValidator();
 
   /**
    * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.eclipse.emf.common.util.Diagnostic#getSource()
    * @see org.eclipse.emf.common.util.Diagnostic#getCode()
    * @generated
    */
   public static final String DIAGNOSTIC_SOURCE = "org.reuseware.coconut.compositionprogram";
 
   /**
    * A constant with a fixed name that can be used as the base value for additional hand written constants.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   private static final int GENERATED_DIAGNOSTIC_CODE_COUNT = 0;
 
   /**
    * A constant with a fixed name that can be used as the base value for additional hand written constants in a derived class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   protected static final int DIAGNOSTIC_CODE_COUNT = GENERATED_DIAGNOSTIC_CODE_COUNT;
 
   /**
    * Creates an instance of the switch.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public CompositionprogramValidator()
   {
     super();
   }
 
   /**
    * Returns the package of this validator switch.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   protected EPackage getEPackage()
   {
     return CompositionprogramPackage.eINSTANCE;
   }
 
   /**
    * Calls <code>validateXXX</code> for the corresponding classifier of the model.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   protected boolean validate(int classifierID, Object value, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     switch (classifierID)
     {
       case CompositionprogramPackage.COMPOSITION_LINK:
         return validateCompositionLink((CompositionLink)value, diagnostics, context);
       case CompositionprogramPackage.COMPOSITION_PROGRAM:
         return validateCompositionProgram((CompositionProgram)value, diagnostics, context);
       case CompositionprogramPackage.DERIVED_COMPOSITION_PROGRAM:
         return validateDerivedCompositionProgram((DerivedCompositionProgram)value, diagnostics, context);
       case CompositionprogramPackage.FRAGMENT_INSTANCE:
         return validateFragmentInstance((FragmentInstance)value, diagnostics, context);
       case CompositionprogramPackage.PHYSICAL_COMPOSITION_PROGRAM:
         return validatePhysicalCompositionProgram((PhysicalCompositionProgram)value, diagnostics, context);
       case CompositionprogramPackage.PORT_INSTANCE:
         return validatePortInstance((PortInstance)value, diagnostics, context);
       case CompositionprogramPackage.SETTING:
         return validateSetting((Setting)value, diagnostics, context);
       default:
         return true;
     }
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
   public boolean validateCompositionLink(CompositionLink compositionLink, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(compositionLink, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(compositionLink, diagnostics, context);
     // if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(compositionLink, diagnostics, context);
     // if (result || diagnostics != null) result &= validate_EveryProxyResolves(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionLink_Valid(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionLink_Typed(compositionLink, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionLink_PortsExist(compositionLink, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the Valid constraint of '<em>Composition Link</em>'. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateCompositionLink_Valid(
 			CompositionLink compositionLink, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 
 		compositionLink.match();
 		
 		if (isCompositionLinkDerivedAndEmpty(compositionLink)) {
 			return true;
 		}
 		
 		if (!compositionLink.isValid()) {
 			List<EObject> cause = new ArrayList<EObject>();
 			cause.add(compositionLink);
 			cause.addAll(compositionLink.getDerivedFrom());
 			addWarning(diagnostics, constructInvalidLinkMsg(compositionLink), cause);
 		}
 		return true;
 	}
 
 	/**
 	 * Validates the Typed constraint of '<em>Composition Link</em>'. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateCompositionLink_Typed(
 			CompositionLink compositionLink, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 
 		if (isCompositionLinkDerivedAndEmpty(compositionLink)) {
 			return true;
 		}
 		
 		if (compositionLink.compositionAssociation() == null) {
 			String message = "Link not allowed in fracol";
 			if (compositionLink.getSource() != null && compositionLink.getTarget() != null &&
 					compositionLink.getSource().getFragmentInstance() != null && compositionLink.getTarget().getFragmentInstance() != null) {
 				message = message + ": "
 					+ compositionLink.getSource().getFragmentInstance().getName()
 					+ "(" + compositionLink.getSource().getFragmentRoleName() + "." + compositionLink.getSource().getPortName() + ")";
 				message = message + " --> " + compositionLink.getTarget().getFragmentInstance().getName()
 					+ "(" + compositionLink.getTarget().getFragmentRoleName() + "." + compositionLink.getTarget().getPortName() + ")";
 			}
 			List<EObject> cause = new ArrayList<EObject>();
 			cause.add(compositionLink);
 			cause.addAll(compositionLink.getDerivedFrom());
 			addWarning(diagnostics, message, cause);
 		}
 
 		return true;
 	}
 
 	/**
 	 * Validates the PortsExist constraint of '<em>Composition Link</em>'. <!--
 	 * begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateCompositionLink_PortsExist(
 			CompositionLink compositionLink, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		//if (isCompositionLinkDerivedAndEmpty(compositionLink)) {
 		//	return true;
 		//}
 		
 		boolean result = true;
 		
 		if(compositionLink.getSource().port() == null) {
 			List<EObject> cause = new ArrayList<EObject>();
 			cause.add(compositionLink.getSource());
 			cause.addAll(compositionLink.getDerivedFrom());
 			
 			addWarning(diagnostics,
 					"Port '" + 
 					compositionLink.getSource().getPortName() + 
 					"' can not be identified on fragment '" +
 					compositionLink.getSource().getFragmentInstance().getName() + "'.",
 					cause);
 			result = false;
 		}
 
 		if(compositionLink.getTarget().port() == null) {
 			List<EObject> cause = new ArrayList<EObject>();
 			cause.add(compositionLink.getTarget());
 			cause.addAll(compositionLink.getDerivedFrom());
 			
 			addWarning(diagnostics,
 					"Port '" + 
 					compositionLink.getTarget().getPortName() + 
 					"' can not be identified on fragment '" +
 					compositionLink.getTarget().getFragmentInstance().getName() + "'.",
 					cause);
 			result = false;
 		}
 
 		return result;
 	}
 
   private boolean isCompositionLinkDerivedAndEmpty(
 			CompositionLink compositionLink) {
 		boolean empty = false;
 		if (compositionLink.getCompositionProgram() instanceof DerivedCompositionProgram) {
 			if (compositionLink.getSource().port() == null
 					|| compositionLink.getTarget().port() == null) {
 				empty = true;
 			} else {
 				for (HeterogeneousPort port : compositionLink.getSource()
 						.allPorts()) {
 					if (port.getAddressablePoints().isEmpty()) {
 						empty = true;
 					}
 				}
 				for (HeterogeneousPort port : compositionLink.getTarget()
 						.allPorts()) {
 					if (port.getAddressablePoints().isEmpty()) {
 						empty = true;
 					}
 				}
 			}
 		}
 		return empty;
 	}
 	
 	private String constructInvalidLinkMsg(CompositionLink compositionLink) {
 		String msg = "Invalid composition";
 		if (compositionLink.getSource() != null && compositionLink.getTarget() != null &&
 				compositionLink.getSource().getFragmentInstance() != null && compositionLink.getTarget().getFragmentInstance() != null) {
 			msg = msg + ": "
 				+ compositionLink.getSource().getFragmentInstance().getName()
 				+ "(" + compositionLink.getSource().getFragmentRoleName() + "." + compositionLink.getSource().getPortName() + ")[";
 			for(HeterogeneousPort p : compositionLink.getSource().allPorts()) {
 				for(Iterator<AddressablePoint> i = p.getAddressablePoints().iterator(); i.hasNext(); ) {
 					AddressablePoint ap = i.next();
 					msg = msg + ap.eClass().getName() + ":" + ap.getVarTypedEObject().eClass().getName();
 					if (i.hasNext()) msg = msg + ",";
 				}
 				break;
 			}
 			msg = msg + "] --> " + compositionLink.getTarget().getFragmentInstance().getName()
 				+ "(" + compositionLink.getTarget().getFragmentRoleName() + "." + compositionLink.getTarget().getPortName() + ")[";
 			for(HeterogeneousPort p : compositionLink.getTarget().allPorts()) {
 				for(Iterator<AddressablePoint> i = p.getAddressablePoints().iterator(); i.hasNext(); ) {
 					AddressablePoint ap = i.next();
 					msg = msg + ap.eClass().getName() + ":" + ap.getVarTypedEObject().eClass().getName();
 					if (i.hasNext()) msg = msg + ",";
 				}
 				break;
 			}
 			msg = msg + "]";
 		}
 		return msg;
 	}
 
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateCompositionProgram(CompositionProgram compositionProgram, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(compositionProgram, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(compositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionProgram_TargetDefined(compositionProgram, diagnostics, context);
     return result;
   }
 
 	/**
 	 * Validates the TargetDefined constraint of '<em>Composition Program</em>'.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	public boolean validateCompositionProgram_TargetDefined(
 			CompositionProgram compositionProgram, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		
 		for(FragmentInstance fragmentInstance : compositionProgram.getFragmentInstances()) {
 			if (fragmentInstance.isTarget()) {
 				return true;
 			}
 		}
 		
 		addWarning(diagnostics, 
 				"The composition program must define at least one target", 
 				Collections.singletonList(compositionProgram));
 		
 		return false;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateDerivedCompositionProgram(DerivedCompositionProgram derivedCompositionProgram, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(derivedCompositionProgram, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(derivedCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionProgram_TargetDefined(derivedCompositionProgram, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
   public boolean validateFragmentInstance(FragmentInstance fragmentInstance, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
 	    if (!validate_NoCircularContainment(fragmentInstance, diagnostics, context)) return false;
 	    boolean result = validate_EveryMultiplicityConforms(fragmentInstance, diagnostics, context);
 	    if (result || diagnostics != null) result &= validate_EveryDataValueConforms(fragmentInstance, diagnostics, context);
 	    // if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(fragmentInstance, diagnostics, context);
 	    if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(fragmentInstance, diagnostics, context);
 	    // if (result || diagnostics != null) result &= validate_EveryProxyResolves(fragmentInstance, diagnostics, context);
 	    if (result || diagnostics != null) result &= validate_UniqueID(fragmentInstance, diagnostics, context);
 	    if (result || diagnostics != null) result &= validate_EveryKeyUnique(fragmentInstance, diagnostics, context);
 	    if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(fragmentInstance, diagnostics, context);
 	    return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePhysicalCompositionProgram(PhysicalCompositionProgram physicalCompositionProgram, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(physicalCompositionProgram, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(physicalCompositionProgram, diagnostics, context);
     if (result || diagnostics != null) result &= validateCompositionProgram_TargetDefined(physicalCompositionProgram, diagnostics, context);
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortInstance(PortInstance portInstance, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portInstance, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateSetting(Setting setting, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(setting, diagnostics, context);
   }
   
 	@Override
 	protected boolean validate_MultiplicityConforms(EObject eObject,
 			EStructuralFeature eStructuralFeature, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 
 		BasicDiagnostic tempDiagnostics = new BasicDiagnostic();
 		boolean result = super.validate_MultiplicityConforms(eObject,
 				eStructuralFeature, tempDiagnostics, context);
 
 		if (!result) {
 			if (eStructuralFeature
 					.equals(CompositionprogramPackage.Literals.FRAGMENT_INSTANCE__FRAGMENT)) {
 				FragmentInstance fragmentInstance = (FragmentInstance) eObject;
 				
 				List<EObject> cause = new ArrayList<EObject>();
 				cause.add(fragmentInstance);
 				cause.addAll(fragmentInstance.getDerivedFrom());
 				
 				addWarning(diagnostics, 
 						"Fragment " + fragmentInstance.getUFI() + " not found",
 						cause);
 			} else {
 				diagnostics.add(tempDiagnostics);
 			}
 		}
 		return result;
 	}
 
   /**
    * Returns the resource locator that will be used to fetch messages for this validator's diagnostics.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
   @Override
 	public ResourceLocator getResourceLocator() {
 		// Specialize this to return a resource locator for messages specific to
 		// this validator.
 		return super.getResourceLocator();
 	}
   
 	protected void addWarning(DiagnosticChain diagnostics, String message, List<? extends EObject> data) {
 		if (diagnostics != null) {
 			diagnostics.add(new BasicDiagnostic(
 					Diagnostic.WARNING,
 					DIAGNOSTIC_SOURCE,
 					0,
 					message,
 					data.toArray()));
 		}
 	}
 
 } //CompositionprogramValidator
