 /**
  */
 package kompren.impl;
 
 import kompren.Constraint;
 import kompren.KomprenFactory;
 import kompren.KomprenPackage;
 import kompren.OppositeCreation;
 import kompren.Radius;
 import kompren.SlicedClass;
 import kompren.SlicedElement;
 import kompren.SlicedProperty;
 import kompren.Slicer;
 import kompren.VarDecl;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EGenericType;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 import org.eclipse.emf.ecore.ETypeParameter;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.impl.EPackageImpl;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Package</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class KomprenPackageImpl extends EPackageImpl implements KomprenPackage {
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public static final String copyright = "Inria/IRISA Diverse Team";
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass slicerEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass constraintEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass radiusEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass slicedClassEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass slicedPropertyEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass oppositeCreationEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass slicedElementEClass = null;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private EClass varDeclEClass = null;
 
 	/**
 	 * Creates an instance of the model <b>Package</b>, registered with
 	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
 	 * package URI value.
 	 * <p>Note: the correct way to create the package is via the static
 	 * factory method {@link #init init()}, which also performs
 	 * initialization of the package, or returns the registered package,
 	 * if one already exists.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see org.eclipse.emf.ecore.EPackage.Registry
 	 * @see kompren.KomprenPackage#eNS_URI
 	 * @see #init()
 	 * @generated
 	 */
 	private KomprenPackageImpl() {
 		super(eNS_URI, KomprenFactory.eINSTANCE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private static boolean isInited = false;
 
 	/**
 	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
 	 * 
 	 * <p>This method is used to initialize {@link KomprenPackage#eINSTANCE} when that field is accessed.
 	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #eNS_URI
 	 * @see #createPackageContents()
 	 * @see #initializePackageContents()
 	 * @generated
 	 */
 	public static KomprenPackage init() {
 		if (isInited) return (KomprenPackage)EPackage.Registry.INSTANCE.getEPackage(KomprenPackage.eNS_URI);
 
 		// Obtain or create and register package
 		KomprenPackageImpl theKomprenPackage = (KomprenPackageImpl)(EPackage.Registry.INSTANCE.get(eNS_URI) instanceof KomprenPackageImpl ? EPackage.Registry.INSTANCE.get(eNS_URI) : new KomprenPackageImpl());
 
 		isInited = true;
 
 		// Initialize simple dependencies
 		EcorePackage.eINSTANCE.eClass();
 
 		// Create package meta-data objects
 		theKomprenPackage.createPackageContents();
 
 		// Initialize created meta-data
 		theKomprenPackage.initializePackageContents();
 
 		// Mark meta-data to indicate it can't be changed
 		theKomprenPackage.freeze();
 
   
 		// Update the registry and return the package
 		EPackage.Registry.INSTANCE.put(KomprenPackage.eNS_URI, theKomprenPackage);
 		return theKomprenPackage;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSlicer() {
 		return slicerEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_Name() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_UriMetamodel() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_Active() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_Strict() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_Helper() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(4);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_OnStart() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(5);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicer_OnEnd() {
 		return (EAttribute)slicerEClass.getEStructuralFeatures().get(6);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicer_Radius() {
 		return (EReference)slicerEClass.getEStructuralFeatures().get(7);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicer_SlicedElements() {
 		return (EReference)slicerEClass.getEStructuralFeatures().get(8);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicer_InputClasses() {
 		return (EReference)slicerEClass.getEStructuralFeatures().get(9);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getConstraint() {
 		return constraintEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getConstraint_Expression() {
 		return (EAttribute)constraintEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getConstraint_Name() {
 		return (EAttribute)constraintEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getRadius() {
 		return radiusEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getRadius_FocusedClasses() {
 		return (EReference)radiusEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSlicedClass() {
 		return slicedClassEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicedClass_Ctx() {
 		return (EReference)slicedClassEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSlicedProperty() {
 		return slicedPropertyEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicedProperty_Opposite() {
 		return (EReference)slicedPropertyEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicedProperty_Src() {
 		return (EReference)slicedPropertyEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicedProperty_Tgt() {
 		return (EReference)slicedPropertyEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getOppositeCreation() {
 		return oppositeCreationEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getOppositeCreation_Name() {
 		return (EAttribute)oppositeCreationEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getSlicedElement() {
 		return slicedElementEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicedElement_IsOption() {
 		return (EAttribute)slicedElementEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getSlicedElement_Expression() {
 		return (EAttribute)slicedElementEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicedElement_Domain() {
 		return (EReference)slicedElementEClass.getEStructuralFeatures().get(2);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getSlicedElement_Constraints() {
 		return (EReference)slicedElementEClass.getEStructuralFeatures().get(3);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EClass getVarDecl() {
 		return varDeclEClass;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EAttribute getVarDecl_VarName() {
 		return (EAttribute)varDeclEClass.getEStructuralFeatures().get(0);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EReference getVarDecl_Type() {
 		return (EReference)varDeclEClass.getEStructuralFeatures().get(1);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public KomprenFactory getKomprenFactory() {
 		return (KomprenFactory)getEFactoryInstance();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isCreated = false;
 
 	/**
 	 * Creates the meta-model objects for the package.  This method is
 	 * guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void createPackageContents() {
 		if (isCreated) return;
 		isCreated = true;
 
 		// Create classes and their features
 		slicerEClass = createEClass(SLICER);
 		createEAttribute(slicerEClass, SLICER__NAME);
 		createEAttribute(slicerEClass, SLICER__URI_METAMODEL);
 		createEAttribute(slicerEClass, SLICER__ACTIVE);
 		createEAttribute(slicerEClass, SLICER__STRICT);
 		createEAttribute(slicerEClass, SLICER__HELPER);
 		createEAttribute(slicerEClass, SLICER__ON_START);
 		createEAttribute(slicerEClass, SLICER__ON_END);
 		createEReference(slicerEClass, SLICER__RADIUS);
 		createEReference(slicerEClass, SLICER__SLICED_ELEMENTS);
 		createEReference(slicerEClass, SLICER__INPUT_CLASSES);
 
 		constraintEClass = createEClass(CONSTRAINT);
 		createEAttribute(constraintEClass, CONSTRAINT__EXPRESSION);
 		createEAttribute(constraintEClass, CONSTRAINT__NAME);
 
 		radiusEClass = createEClass(RADIUS);
 		createEReference(radiusEClass, RADIUS__FOCUSED_CLASSES);
 
 		slicedClassEClass = createEClass(SLICED_CLASS);
 		createEReference(slicedClassEClass, SLICED_CLASS__CTX);
 
 		slicedPropertyEClass = createEClass(SLICED_PROPERTY);
 		createEReference(slicedPropertyEClass, SLICED_PROPERTY__OPPOSITE);
 		createEReference(slicedPropertyEClass, SLICED_PROPERTY__SRC);
 		createEReference(slicedPropertyEClass, SLICED_PROPERTY__TGT);
 
 		oppositeCreationEClass = createEClass(OPPOSITE_CREATION);
 		createEAttribute(oppositeCreationEClass, OPPOSITE_CREATION__NAME);
 
 		slicedElementEClass = createEClass(SLICED_ELEMENT);
 		createEAttribute(slicedElementEClass, SLICED_ELEMENT__IS_OPTION);
 		createEAttribute(slicedElementEClass, SLICED_ELEMENT__EXPRESSION);
 		createEReference(slicedElementEClass, SLICED_ELEMENT__DOMAIN);
 		createEReference(slicedElementEClass, SLICED_ELEMENT__CONSTRAINTS);
 
 		varDeclEClass = createEClass(VAR_DECL);
 		createEAttribute(varDeclEClass, VAR_DECL__VAR_NAME);
 		createEReference(varDeclEClass, VAR_DECL__TYPE);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	private boolean isInitialized = false;
 
 	/**
 	 * Complete the initialization of the package and its meta-model.  This
 	 * method is guarded to have no affect on any invocation but its first.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void initializePackageContents() {
 		if (isInitialized) return;
 		isInitialized = true;
 
 		// Initialize package
 		setName(eNAME);
 		setNsPrefix(eNS_PREFIX);
 		setNsURI(eNS_URI);
 
 		// Obtain other dependent packages
 		EcorePackage theEcorePackage = (EcorePackage)EPackage.Registry.INSTANCE.getEPackage(EcorePackage.eNS_URI);
 
 		// Create type parameters
 		ETypeParameter slicedElementEClass_T = addETypeParameter(slicedElementEClass, "T");
 
 		// Set bounds for type parameters
 		EGenericType g1 = createEGenericType(theEcorePackage.getENamedElement());
 		slicedElementEClass_T.getEBounds().add(g1);
 
 		// Add supertypes to classes
 		g1 = createEGenericType(this.getSlicedElement());
 		EGenericType g2 = createEGenericType(theEcorePackage.getEClass());
 		g1.getETypeArguments().add(g2);
 		slicedClassEClass.getEGenericSuperTypes().add(g1);
 		g1 = createEGenericType(this.getSlicedElement());
 		g2 = createEGenericType(theEcorePackage.getEStructuralFeature());
 		g1.getETypeArguments().add(g2);
 		slicedPropertyEClass.getEGenericSuperTypes().add(g1);
 
 		// Initialize classes, features, and operations; add parameters
 		initEClass(slicerEClass, Slicer.class, "Slicer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSlicer_Name(), ecorePackage.getEString(), "name", null, 1, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicer_UriMetamodel(), ecorePackage.getEString(), "uriMetamodel", null, 1, -1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicer_Active(), ecorePackage.getEBoolean(), "active", null, 0, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicer_Strict(), ecorePackage.getEBoolean(), "strict", null, 0, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicer_Helper(), ecorePackage.getEString(), "helper", null, 0, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicer_OnStart(), ecorePackage.getEString(), "onStart", null, 0, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicer_OnEnd(), ecorePackage.getEString(), "onEnd", null, 0, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSlicer_Radius(), this.getRadius(), null, "radius", null, 0, 1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(this.getSlicedElement());
 		g2 = createEGenericType();
 		g1.getETypeArguments().add(g2);
 		initEReference(getSlicer_SlicedElements(), g1, null, "slicedElements", null, 0, -1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSlicer_InputClasses(), theEcorePackage.getEClass(), null, "inputClasses", null, 1, -1, Slicer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(constraintEClass, Constraint.class, "Constraint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getConstraint_Expression(), ecorePackage.getEString(), "expression", null, 1, 1, Constraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getConstraint_Name(), ecorePackage.getEString(), "name", null, 1, 1, Constraint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(radiusEClass, Radius.class, "Radius", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getRadius_FocusedClasses(), this.getSlicedClass(), null, "focusedClasses", null, 0, -1, Radius.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(slicedClassEClass, SlicedClass.class, "SlicedClass", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSlicedClass_Ctx(), this.getVarDecl(), null, "ctx", null, 0, 1, SlicedClass.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(slicedPropertyEClass, SlicedProperty.class, "SlicedProperty", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEReference(getSlicedProperty_Opposite(), this.getOppositeCreation(), null, "opposite", null, 0, 1, SlicedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSlicedProperty_Src(), this.getVarDecl(), null, "src", null, 0, 1, SlicedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSlicedProperty_Tgt(), this.getVarDecl(), null, "tgt", null, 0, 1, SlicedProperty.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(oppositeCreationEClass, OppositeCreation.class, "OppositeCreation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getOppositeCreation_Name(), ecorePackage.getEString(), "name", null, 1, 1, OppositeCreation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(slicedElementEClass, SlicedElement.class, "SlicedElement", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getSlicedElement_IsOption(), ecorePackage.getEBoolean(), "isOption", null, 0, 1, SlicedElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEAttribute(getSlicedElement_Expression(), ecorePackage.getEString(), "expression", null, 0, 1, SlicedElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		g1 = createEGenericType(slicedElementEClass_T);
 		initEReference(getSlicedElement_Domain(), g1, null, "domain", null, 1, 1, SlicedElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 		initEReference(getSlicedElement_Constraints(), this.getConstraint(), null, "constraints", null, 0, -1, SlicedElement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
 
 		initEClass(varDeclEClass, VarDecl.class, "VarDecl", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
 		initEAttribute(getVarDecl_VarName(), ecorePackage.getEString(), "varName", null, 1, 1, VarDecl.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getVarDecl_Type(), theEcorePackage.getEClass(), null, "type", null, 1, 1, VarDecl.class, !IS_TRANSIENT, IS_VOLATILE, !IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
 
 		// Create resource
 		createResource(eNS_URI);
 	}
 
 } //KomprenPackageImpl
