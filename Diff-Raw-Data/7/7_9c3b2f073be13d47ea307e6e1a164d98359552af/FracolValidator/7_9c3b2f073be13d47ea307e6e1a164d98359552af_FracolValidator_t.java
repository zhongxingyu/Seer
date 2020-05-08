 package org.reuseware.coconut.fracol.util;
 
 
 import java.util.Map;
 
 import org.eclipse.emf.common.util.BasicDiagnostic;
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.common.util.DiagnosticChain;
 import org.eclipse.emf.common.util.ResourceLocator;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.util.EObjectValidator;
 import org.reuseware.coconut.fracol.*;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Validator</b> for the model.
  * <!-- end-user-doc -->
  * @see org.reuseware.coconut.fracol.FracolPackage
  * @generated
  */
 public class FracolValidator extends EObjectValidator
 {
   /**
    * The cached model package
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public static final FracolValidator INSTANCE = new FracolValidator();
 
   /**
    * A constant for the {@link org.eclipse.emf.common.util.Diagnostic#getSource() source} of diagnostic {@link org.eclipse.emf.common.util.Diagnostic#getCode() codes} from this package.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.eclipse.emf.common.util.Diagnostic#getSource()
    * @see org.eclipse.emf.common.util.Diagnostic#getCode()
    * @generated
    */
   public static final String DIAGNOSTIC_SOURCE = "org.reuseware.coconut.fracol";
 
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
   public FracolValidator()
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
     return FracolPackage.eINSTANCE;
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
       case FracolPackage.COMPOSITION_ASSOCIATION:
         return validateCompositionAssociation((CompositionAssociation)value, diagnostics, context);
       case FracolPackage.CONFIGURATION:
         return validateConfiguration((Configuration)value, diagnostics, context);
       case FracolPackage.CONTRIBUTION:
         return validateContribution((Contribution)value, diagnostics, context);
       case FracolPackage.DYNAMIC_PORT_TYPE:
         return validateDynamicPortType((DynamicPortType)value, diagnostics, context);
       case FracolPackage.FRAGMENT_COLLABORATION:
         return validateFragmentCollaboration((FragmentCollaboration)value, diagnostics, context);
       case FracolPackage.FRAGMENT_ROLE:
         return validateFragmentRole((FragmentRole)value, diagnostics, context);
       case FracolPackage.PORT_TYPE:
         return validatePortType((PortType)value, diagnostics, context);
       case FracolPackage.STATIC_PORT_TYPE:
         return validateStaticPortType((StaticPortType)value, diagnostics, context);
       default:
         return true;
     }
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateCompositionAssociation(CompositionAssociation compositionAssociation, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(compositionAssociation, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateConfiguration(Configuration configuration, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(configuration, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateContribution(Contribution contribution, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(contribution, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateDynamicPortType(DynamicPortType dynamicPortType, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(dynamicPortType, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentCollaboration(FragmentCollaboration fragmentCollaboration, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     if (!validate_NoCircularContainment(fragmentCollaboration, diagnostics, context)) return false;
     boolean result = validate_EveryMultiplicityConforms(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryDataValueConforms(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryReferenceIsContained(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryBidirectionalReferenceIsPaired(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryProxyResolves(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_UniqueID(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryKeyUnique(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validate_EveryMapEntryUnique(fragmentCollaboration, diagnostics, context);
     if (result || diagnostics != null) result &= validateFragmentCollaboration_All(fragmentCollaboration, diagnostics, context);
     return result;
   }
 
   /**
    * Validates the All constraint of '<em>Fragment Collaboration</em>'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated NOT
    */
 	public boolean validateFragmentCollaboration_All(
 			FragmentCollaboration fragmentCollaboration,
 			DiagnosticChain diagnostics, Map<Object, Object> context) {
 		return true;
 	}
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateFragmentRole(FragmentRole fragmentRole, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(fragmentRole, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validatePortType(PortType portType, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(portType, diagnostics, context);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public boolean validateStaticPortType(StaticPortType staticPortType, DiagnosticChain diagnostics, Map<Object, Object> context)
   {
     return validate_EveryDefaultConstraint(staticPortType, diagnostics, context);
   }
   
 	/**
 	 * Returns the resource locator that will be used to fetch messages for this
 	 * validator's diagnostics. <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	@Override
 	public ResourceLocator getResourceLocator() {
 		// Ensure that you remove @generated or mark it @generated NOT
 		return super.getResourceLocator();
 	}
 	
 	@Override
 	protected boolean validate_KeyUnique(EObject eObject,
 			EReference eReference, DiagnosticChain diagnostics,
 			Map<Object, Object> context) {
 		BasicDiagnostic tempDiagnostics = new BasicDiagnostic();
 		boolean result = super.validate_KeyUnique(eObject, eReference, tempDiagnostics, context);
 		if (!result) {
 			for (Diagnostic diagnostic : tempDiagnostics.getChildren()) {
 				EObject data1 = (EObject) diagnostic.getData().get(2);
 				addError(diagnostics, "Duplicated name", data1);
 				EObject data2 = (EObject) diagnostic.getData().get(3);
 				addError(diagnostics, "Duplicated name", data2);
 			}
 		}
 		return result;
 	}
 	
	@Override
	public boolean validate_EveryProxyResolves(EObject eObject,
			DiagnosticChain diagnostics, Map<Object, Object> context) {
		// errors are sufficiently reported by EMFText
		return true;
	}
	
 	protected void addError(DiagnosticChain diagnostics, String message, EObject data) {
 		diagnostics.add(new BasicDiagnostic(
 				Diagnostic.ERROR,
 				DIAGNOSTIC_SOURCE,
 				0,
 				message,
 				new Object[] { data }));
 	}
 
 } //FracolValidator
