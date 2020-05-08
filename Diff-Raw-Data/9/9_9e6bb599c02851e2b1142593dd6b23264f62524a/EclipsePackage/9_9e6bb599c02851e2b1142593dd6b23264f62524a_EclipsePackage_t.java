 /**
  */
 package org.obeonetwork.dsl.gen.eclipse;
 
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 
 /**
  * <!-- begin-user-doc -->
  * The <b>Package</b> for the model.
  * It contains accessors for the meta objects to represent
  * <ul>
  *   <li>each class,</li>
  *   <li>each feature of each class,</li>
  *   <li>each operation of each class,</li>
  *   <li>each enum,</li>
  *   <li>and each data type</li>
  * </ul>
  * <!-- end-user-doc -->
  * @see org.obeonetwork.dsl.gen.eclipse.EclipseFactory
  * @model kind="package"
  *        annotation="http://www.eclipse.org/emf/2002/GenModel basePackage='org.obeonetwork.dsl.gen' editDirectory='/org.obeonetwork.dsl.gen.eclipse.model.edit/src-gen' editorDirectory='/org.obeonetwork.dsl.gen.eclipse.model.editor/src-gen'"
  * @generated
  */
 public interface EclipsePackage extends EPackage
 {
   /**
    * The package name.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   String eNAME = "eclipse";
 
   /**
    * The package namespace URI.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   String eNS_URI = "http://www.obeonetwork.org/dsl/eclipse";
 
   /**
    * The package namespace name.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   String eNS_PREFIX = "eclipse";
 
   /**
    * The singleton instance of the package.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   EclipsePackage eINSTANCE = org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl.init();
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ApplicationImpl <em>Application</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ApplicationImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getApplication()
    * @generated
    */
   int APPLICATION = 0;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Application ID</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__APPLICATION_ID = 1;
 
   /**
    * The feature id for the '<em><b>Provider</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__PROVIDER = 2;
 
   /**
    * The feature id for the '<em><b>Copyright</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__COPYRIGHT = 3;
 
   /**
    * The feature id for the '<em><b>License</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__LICENSE = 4;
 
   /**
    * The feature id for the '<em><b>Version</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__VERSION = 5;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__DESCRIPTION = 6;
 
   /**
    * The feature id for the '<em><b>Base Namespace</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__BASE_NAMESPACE = 7;
 
   /**
    * The feature id for the '<em><b>Maven Compilation</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__MAVEN_COMPILATION = 8;
 
   /**
    * The feature id for the '<em><b>Projects</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION__PROJECTS = 9;
 
   /**
    * The number of structural features of the '<em>Application</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION_FEATURE_COUNT = 10;
 
   /**
    * The number of operations of the '<em>Application</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int APPLICATION_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ProjectImpl <em>Project</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ProjectImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getProject()
    * @generated
    */
   int PROJECT = 1;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT__NAME = 0;
 
   /**
    * The feature id for the '<em><b>ID</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT__ID = 1;
 
   /**
    * The number of structural features of the '<em>Project</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_FEATURE_COUNT = 2;
 
   /**
    * The number of operations of the '<em>Project</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.RepositoryImpl <em>Repository</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.RepositoryImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getRepository()
    * @generated
    */
   int REPOSITORY = 2;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY__NAME = PROJECT__NAME;
 
   /**
    * The feature id for the '<em><b>ID</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY__ID = PROJECT__ID;
 
   /**
    * The feature id for the '<em><b>Repository Categories</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY__REPOSITORY_CATEGORIES = PROJECT_FEATURE_COUNT + 0;
 
   /**
    * The number of structural features of the '<em>Repository</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_FEATURE_COUNT = PROJECT_FEATURE_COUNT + 1;
 
   /**
    * The number of operations of the '<em>Repository</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_OPERATION_COUNT = PROJECT_OPERATION_COUNT + 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.RepositoryCategoryImpl <em>Repository Category</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.RepositoryCategoryImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getRepositoryCategory()
    * @generated
    */
   int REPOSITORY_CATEGORY = 3;
 
   /**
    * The feature id for the '<em><b>Label</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_CATEGORY__LABEL = 0;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_CATEGORY__DESCRIPTION = 1;
 
   /**
    * The feature id for the '<em><b>ID</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_CATEGORY__ID = 2;
 
   /**
    * The feature id for the '<em><b>Features</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_CATEGORY__FEATURES = 3;
 
   /**
    * The number of structural features of the '<em>Repository Category</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_CATEGORY_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Repository Category</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REPOSITORY_CATEGORY_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.FeatureImpl <em>Feature</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.FeatureImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getFeature()
    * @generated
    */
   int FEATURE = 4;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__NAME = PROJECT__NAME;
 
   /**
    * The feature id for the '<em><b>ID</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__ID = PROJECT__ID;
 
   /**
    * The feature id for the '<em><b>Copyright</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__COPYRIGHT = PROJECT_FEATURE_COUNT + 0;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__DESCRIPTION = PROJECT_FEATURE_COUNT + 1;
 
   /**
    * The feature id for the '<em><b>Version</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__VERSION = PROJECT_FEATURE_COUNT + 2;
 
   /**
    * The feature id for the '<em><b>License</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__LICENSE = PROJECT_FEATURE_COUNT + 3;
 
   /**
    * The feature id for the '<em><b>Provider</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__PROVIDER = PROJECT_FEATURE_COUNT + 4;
 
   /**
    * The feature id for the '<em><b>Bundles</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE__BUNDLES = PROJECT_FEATURE_COUNT + 5;
 
   /**
    * The number of structural features of the '<em>Feature</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE_FEATURE_COUNT = PROJECT_FEATURE_COUNT + 6;
 
   /**
    * The number of operations of the '<em>Feature</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int FEATURE_OPERATION_COUNT = PROJECT_OPERATION_COUNT + 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.BundleImpl <em>Bundle</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.BundleImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getBundle()
    * @generated
    */
   int BUNDLE = 5;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__NAME = PROJECT__NAME;
 
   /**
    * The feature id for the '<em><b>ID</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__ID = PROJECT__ID;
 
   /**
    * The feature id for the '<em><b>Version</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__VERSION = PROJECT_FEATURE_COUNT + 0;
 
   /**
    * The feature id for the '<em><b>Required Environment</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__REQUIRED_ENVIRONMENT = PROJECT_FEATURE_COUNT + 1;
 
   /**
    * The feature id for the '<em><b>Vendor</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__VENDOR = PROJECT_FEATURE_COUNT + 2;
 
   /**
    * The feature id for the '<em><b>Declarative Services</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__DECLARATIVE_SERVICES = PROJECT_FEATURE_COUNT + 3;
 
   /**
    * The feature id for the '<em><b>Imported Package Declarations</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__IMPORTED_PACKAGE_DECLARATIONS = PROJECT_FEATURE_COUNT + 4;
 
   /**
    * The feature id for the '<em><b>Natures</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__NATURES = PROJECT_FEATURE_COUNT + 5;
 
   /**
    * The feature id for the '<em><b>Builders</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__BUILDERS = PROJECT_FEATURE_COUNT + 6;
 
   /**
    * The feature id for the '<em><b>Wizards</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__WIZARDS = PROJECT_FEATURE_COUNT + 7;
 
   /**
    * The feature id for the '<em><b>Extension Points</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__EXTENSION_POINTS = PROJECT_FEATURE_COUNT + 8;
 
   /**
    * The feature id for the '<em><b>Decorators</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__DECORATORS = PROJECT_FEATURE_COUNT + 9;
 
   /**
    * The feature id for the '<em><b>Markers</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__MARKERS = PROJECT_FEATURE_COUNT + 10;
 
   /**
    * The feature id for the '<em><b>Perspectives</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__PERSPECTIVES = PROJECT_FEATURE_COUNT + 11;
 
   /**
    * The feature id for the '<em><b>Editors</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__EDITORS = PROJECT_FEATURE_COUNT + 12;
 
   /**
    * The feature id for the '<em><b>Views</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__VIEWS = PROJECT_FEATURE_COUNT + 13;
 
   /**
    * The feature id for the '<em><b>Help Contents</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__HELP_CONTENTS = PROJECT_FEATURE_COUNT + 14;
 
   /**
    * The feature id for the '<em><b>Commands</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__COMMANDS = PROJECT_FEATURE_COUNT + 15;
 
   /**
    * The feature id for the '<em><b>Menus</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__MENUS = PROJECT_FEATURE_COUNT + 16;
 
   /**
    * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__HANDLERS = PROJECT_FEATURE_COUNT + 17;
 
   /**
    * The feature id for the '<em><b>Contexts</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__CONTEXTS = PROJECT_FEATURE_COUNT + 18;
 
   /**
    * The feature id for the '<em><b>Categories</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__CATEGORIES = PROJECT_FEATURE_COUNT + 19;
 
   /**
    * The feature id for the '<em><b>Bindings</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__BINDINGS = PROJECT_FEATURE_COUNT + 20;
 
   /**
    * The feature id for the '<em><b>Exported Packages</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__EXPORTED_PACKAGES = PROJECT_FEATURE_COUNT + 21;
 
   /**
    * The feature id for the '<em><b>Owned Packages</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE__OWNED_PACKAGES = PROJECT_FEATURE_COUNT + 22;
 
   /**
    * The number of structural features of the '<em>Bundle</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE_FEATURE_COUNT = PROJECT_FEATURE_COUNT + 23;
 
   /**
    * The number of operations of the '<em>Bundle</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUNDLE_OPERATION_COUNT = PROJECT_OPERATION_COUNT + 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.DeclarativeServiceImpl <em>Declarative Service</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.DeclarativeServiceImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getDeclarativeService()
    * @generated
    */
   int DECLARATIVE_SERVICE = 6;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECLARATIVE_SERVICE__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Implentation Class</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECLARATIVE_SERVICE__IMPLENTATION_CLASS = 1;
 
   /**
    * The feature id for the '<em><b>Provided Services</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECLARATIVE_SERVICE__PROVIDED_SERVICES = 2;
 
   /**
    * The feature id for the '<em><b>Required Services</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECLARATIVE_SERVICE__REQUIRED_SERVICES = 3;
 
   /**
    * The number of structural features of the '<em>Declarative Service</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECLARATIVE_SERVICE_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Declarative Service</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECLARATIVE_SERVICE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ProvidedServiceImpl <em>Provided Service</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ProvidedServiceImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getProvidedService()
    * @generated
    */
   int PROVIDED_SERVICE = 7;
 
   /**
    * The feature id for the '<em><b>Interface</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROVIDED_SERVICE__INTERFACE = 0;
 
   /**
    * The number of structural features of the '<em>Provided Service</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROVIDED_SERVICE_FEATURE_COUNT = 1;
 
   /**
    * The number of operations of the '<em>Provided Service</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROVIDED_SERVICE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.RequiredServiceImpl <em>Required Service</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.RequiredServiceImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getRequiredService()
    * @generated
    */
   int REQUIRED_SERVICE = 8;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Lower Bound</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE__LOWER_BOUND = 1;
 
   /**
    * The feature id for the '<em><b>Upper Bound</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE__UPPER_BOUND = 2;
 
   /**
    * The feature id for the '<em><b>Bind</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE__BIND = 3;
 
   /**
    * The feature id for the '<em><b>Interface</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE__INTERFACE = 4;
 
   /**
    * The number of structural features of the '<em>Required Service</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE_FEATURE_COUNT = 5;
 
   /**
    * The number of operations of the '<em>Required Service</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int REQUIRED_SERVICE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ImportedPackageDeclarationImpl <em>Imported Package Declaration</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ImportedPackageDeclarationImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getImportedPackageDeclaration()
    * @generated
    */
   int IMPORTED_PACKAGE_DECLARATION = 9;
 
   /**
    * The feature id for the '<em><b>Package Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int IMPORTED_PACKAGE_DECLARATION__PACKAGE_NAME = 0;
 
   /**
    * The feature id for the '<em><b>Package Version</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int IMPORTED_PACKAGE_DECLARATION__PACKAGE_VERSION = 1;
 
   /**
    * The number of structural features of the '<em>Imported Package Declaration</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int IMPORTED_PACKAGE_DECLARATION_FEATURE_COUNT = 2;
 
   /**
    * The number of operations of the '<em>Imported Package Declaration</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int IMPORTED_PACKAGE_DECLARATION_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.BuilderImpl <em>Builder</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.BuilderImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getBuilder()
    * @generated
    */
   int BUILDER = 10;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUILDER__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Natures</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUILDER__NATURES = 1;
 
   /**
    * The number of structural features of the '<em>Builder</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUILDER_FEATURE_COUNT = 2;
 
   /**
    * The number of operations of the '<em>Builder</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BUILDER_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.NatureImpl <em>Nature</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.NatureImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getNature()
    * @generated
    */
   int NATURE = 11;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int NATURE__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Has Toggle Nature</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int NATURE__HAS_TOGGLE_NATURE = 1;
 
   /**
    * The feature id for the '<em><b>Builders</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int NATURE__BUILDERS = 2;
 
   /**
    * The number of structural features of the '<em>Nature</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int NATURE_FEATURE_COUNT = 3;
 
   /**
    * The number of operations of the '<em>Nature</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int NATURE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.WizardImpl <em>Wizard</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.WizardImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getWizard()
    * @generated
    */
   int WIZARD = 12;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Title</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD__TITLE = 1;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD__DESCRIPTION = 2;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD__ICON = 3;
 
   /**
    * The feature id for the '<em><b>Is Project</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD__IS_PROJECT = 4;
 
   /**
    * The feature id for the '<em><b>Category</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD__CATEGORY = 5;
 
   /**
    * The number of structural features of the '<em>Wizard</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD_FEATURE_COUNT = 6;
 
   /**
    * The number of operations of the '<em>Wizard</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int WIZARD_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ProjectWizardImpl <em>Project Wizard</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ProjectWizardImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getProjectWizard()
    * @generated
    */
   int PROJECT_WIZARD = 13;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__NAME = WIZARD__NAME;
 
   /**
    * The feature id for the '<em><b>Title</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__TITLE = WIZARD__TITLE;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__DESCRIPTION = WIZARD__DESCRIPTION;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__ICON = WIZARD__ICON;
 
   /**
    * The feature id for the '<em><b>Is Project</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__IS_PROJECT = WIZARD__IS_PROJECT;
 
   /**
    * The feature id for the '<em><b>Category</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__CATEGORY = WIZARD__CATEGORY;
 
   /**
    * The feature id for the '<em><b>Natures</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD__NATURES = WIZARD_FEATURE_COUNT + 0;
 
   /**
    * The number of structural features of the '<em>Project Wizard</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD_FEATURE_COUNT = WIZARD_FEATURE_COUNT + 1;
 
   /**
    * The number of operations of the '<em>Project Wizard</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PROJECT_WIZARD_OPERATION_COUNT = WIZARD_OPERATION_COUNT + 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ExtensionPointImpl <em>Extension Point</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ExtensionPointImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getExtensionPoint()
    * @generated
    */
   int EXTENSION_POINT = 14;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EXTENSION_POINT__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Required</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EXTENSION_POINT__REQUIRED = 1;
 
   /**
    * The feature id for the '<em><b>Attributes</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EXTENSION_POINT__ATTRIBUTES = 2;
 
   /**
    * The number of structural features of the '<em>Extension Point</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EXTENSION_POINT_FEATURE_COUNT = 3;
 
   /**
    * The number of operations of the '<em>Extension Point</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EXTENSION_POINT_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.AttributeImpl <em>Attribute</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.AttributeImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getAttribute()
    * @generated
    */
   int ATTRIBUTE = 15;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int ATTRIBUTE__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Required</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int ATTRIBUTE__REQUIRED = 1;
 
   /**
    * The feature id for the '<em><b>Translatable</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int ATTRIBUTE__TRANSLATABLE = 2;
 
   /**
    * The feature id for the '<em><b>Type</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int ATTRIBUTE__TYPE = 3;
 
   /**
    * The number of structural features of the '<em>Attribute</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int ATTRIBUTE_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Attribute</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int ATTRIBUTE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.DecoratorImpl <em>Decorator</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.DecoratorImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getDecorator()
    * @generated
    */
   int DECORATOR = 16;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR__ICON = 1;
 
   /**
    * The feature id for the '<em><b>Is Lightweight</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR__IS_LIGHTWEIGHT = 2;
 
   /**
    * The feature id for the '<em><b>Is Adaptable</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR__IS_ADAPTABLE = 3;
 
   /**
    * The feature id for the '<em><b>Location</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR__LOCATION = 4;
 
   /**
    * The number of structural features of the '<em>Decorator</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR_FEATURE_COUNT = 5;
 
   /**
    * The number of operations of the '<em>Decorator</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DECORATOR_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.MarkerImpl <em>Marker</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.MarkerImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getMarker()
    * @generated
    */
   int MARKER = 17;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MARKER__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Is Persistant</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MARKER__IS_PERSISTANT = 1;
 
   /**
    * The number of structural features of the '<em>Marker</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MARKER_FEATURE_COUNT = 2;
 
   /**
    * The number of operations of the '<em>Marker</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MARKER_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ContextImpl <em>Context</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ContextImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getContext()
    * @generated
    */
   int CONTEXT = 18;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CONTEXT__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CONTEXT__DESCRIPTION = 1;
 
   /**
    * The feature id for the '<em><b>Contexts</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CONTEXT__CONTEXTS = 2;
 
   /**
    * The number of structural features of the '<em>Context</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CONTEXT_FEATURE_COUNT = 3;
 
   /**
    * The number of operations of the '<em>Context</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CONTEXT_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.PerspectiveImpl <em>Perspective</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.PerspectiveImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getPerspective()
    * @generated
    */
   int PERSPECTIVE = 19;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE__ICON = 1;
 
   /**
    * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE__HANDLERS = 2;
 
   /**
    * The feature id for the '<em><b>Menus</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE__MENUS = 3;
 
   /**
    * The feature id for the '<em><b>Wizards</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE__WIZARDS = 4;
 
   /**
    * The feature id for the '<em><b>Views</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE__VIEWS = 5;
 
   /**
    * The number of structural features of the '<em>Perspective</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE_FEATURE_COUNT = 6;
 
   /**
    * The number of operations of the '<em>Perspective</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PERSPECTIVE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.CategoryImpl <em>Category</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.CategoryImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getCategory()
    * @generated
    */
   int CATEGORY = 20;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CATEGORY__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Commands</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CATEGORY__COMMANDS = 1;
 
   /**
    * The feature id for the '<em><b>Views</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CATEGORY__VIEWS = 2;
 
   /**
    * The feature id for the '<em><b>Wizards</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CATEGORY__WIZARDS = 3;
 
   /**
    * The number of structural features of the '<em>Category</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CATEGORY_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Category</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int CATEGORY_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.PartImpl <em>Part</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.PartImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getPart()
    * @generated
    */
   int PART = 21;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PART__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PART__ICON = 1;
 
   /**
    * The feature id for the '<em><b>Dynamic Help</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PART__DYNAMIC_HELP = 2;
 
   /**
    * The number of structural features of the '<em>Part</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PART_FEATURE_COUNT = 3;
 
   /**
    * The number of operations of the '<em>Part</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int PART_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ViewImpl <em>View</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.ViewImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getView()
    * @generated
    */
   int VIEW = 22;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__NAME = PART__NAME;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__ICON = PART__ICON;
 
   /**
    * The feature id for the '<em><b>Dynamic Help</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__DYNAMIC_HELP = PART__DYNAMIC_HELP;
 
   /**
    * The feature id for the '<em><b>Is Tree</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__IS_TREE = PART_FEATURE_COUNT + 0;
 
   /**
    * The feature id for the '<em><b>Is Visible</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__IS_VISIBLE = PART_FEATURE_COUNT + 1;
 
   /**
    * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__HANDLERS = PART_FEATURE_COUNT + 2;
 
   /**
    * The feature id for the '<em><b>Menus</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__MENUS = PART_FEATURE_COUNT + 3;
 
   /**
    * The feature id for the '<em><b>Perspectives</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__PERSPECTIVES = PART_FEATURE_COUNT + 4;
 
   /**
    * The feature id for the '<em><b>Category</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW__CATEGORY = PART_FEATURE_COUNT + 5;
 
   /**
    * The number of structural features of the '<em>View</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW_FEATURE_COUNT = PART_FEATURE_COUNT + 6;
 
   /**
    * The number of operations of the '<em>View</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int VIEW_OPERATION_COUNT = PART_OPERATION_COUNT + 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.EditorImpl <em>Editor</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EditorImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getEditor()
    * @generated
    */
   int EDITOR = 23;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__NAME = PART__NAME;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__ICON = PART__ICON;
 
   /**
    * The feature id for the '<em><b>Dynamic Help</b></em>' containment reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__DYNAMIC_HELP = PART__DYNAMIC_HELP;
 
   /**
    * The feature id for the '<em><b>Extension</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__EXTENSION = PART_FEATURE_COUNT + 0;
 
   /**
    * The feature id for the '<em><b>Dynamic Menu</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__DYNAMIC_MENU = PART_FEATURE_COUNT + 1;
 
   /**
    * The feature id for the '<em><b>Handlers</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__HANDLERS = PART_FEATURE_COUNT + 2;
 
   /**
    * The feature id for the '<em><b>Menus</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR__MENUS = PART_FEATURE_COUNT + 3;
 
   /**
    * The number of structural features of the '<em>Editor</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR_FEATURE_COUNT = PART_FEATURE_COUNT + 4;
 
   /**
    * The number of operations of the '<em>Editor</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int EDITOR_OPERATION_COUNT = PART_OPERATION_COUNT + 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.MenuImpl <em>Menu</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.MenuImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getMenu()
    * @generated
    */
   int MENU = 24;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Mnemonic</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU__MNEMONIC = 1;
 
   /**
    * The feature id for the '<em><b>Menu Contribution</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU__MENU_CONTRIBUTION = 2;
 
   /**
    * The feature id for the '<em><b>Toolbar Contribution</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU__TOOLBAR_CONTRIBUTION = 3;
 
   /**
    * The feature id for the '<em><b>Commands</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU__COMMANDS = 4;
 
   /**
    * The number of structural features of the '<em>Menu</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU_FEATURE_COUNT = 5;
 
   /**
    * The number of operations of the '<em>Menu</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int MENU_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.CommandImpl <em>Command</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.CommandImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getCommand()
    * @generated
    */
   int COMMAND = 25;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Icon</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__ICON = 1;
 
   /**
    * The feature id for the '<em><b>Tooltip</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__TOOLTIP = 2;
 
   /**
    * The feature id for the '<em><b>Handler</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__HANDLER = 3;
 
   /**
    * The feature id for the '<em><b>Menu</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__MENU = 4;
 
   /**
    * The feature id for the '<em><b>Category</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__CATEGORY = 5;
 
   /**
    * The feature id for the '<em><b>Binding</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND__BINDING = 6;
 
   /**
    * The number of structural features of the '<em>Command</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND_FEATURE_COUNT = 7;
 
   /**
    * The number of operations of the '<em>Command</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int COMMAND_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.HandlerImpl <em>Handler</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.HandlerImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getHandler()
    * @generated
    */
   int HANDLER = 26;
 
   /**
    * The feature id for the '<em><b>Name</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HANDLER__NAME = 0;
 
   /**
    * The feature id for the '<em><b>Mnemonic</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HANDLER__MNEMONIC = 1;
 
   /**
    * The feature id for the '<em><b>Command</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HANDLER__COMMAND = 2;
 
   /**
    * The feature id for the '<em><b>Contexts</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HANDLER__CONTEXTS = 3;
 
   /**
    * The number of structural features of the '<em>Handler</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HANDLER_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Handler</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HANDLER_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.HelpContentsImpl <em>Help Contents</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.HelpContentsImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getHelpContents()
    * @generated
    */
   int HELP_CONTENTS = 27;
 
   /**
    * The feature id for the '<em><b>Label</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_CONTENTS__LABEL = 0;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_CONTENTS__DESCRIPTION = 1;
 
   /**
    * The feature id for the '<em><b>Help Pages</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_CONTENTS__HELP_PAGES = 2;
 
   /**
    * The feature id for the '<em><b>External Help Pages</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_CONTENTS__EXTERNAL_HELP_PAGES = 3;
 
   /**
    * The number of structural features of the '<em>Help Contents</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_CONTENTS_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Help Contents</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_CONTENTS_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.HelpPageImpl <em>Help Page</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.HelpPageImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getHelpPage()
    * @generated
    */
   int HELP_PAGE = 28;
 
   /**
    * The feature id for the '<em><b>Label</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_PAGE__LABEL = 0;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_PAGE__DESCRIPTION = 1;
 
   /**
    * The feature id for the '<em><b>Help Pages</b></em>' containment reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_PAGE__HELP_PAGES = 2;
 
   /**
    * The feature id for the '<em><b>External Help Pages</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_PAGE__EXTERNAL_HELP_PAGES = 3;
 
   /**
    * The number of structural features of the '<em>Help Page</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_PAGE_FEATURE_COUNT = 4;
 
   /**
    * The number of operations of the '<em>Help Page</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int HELP_PAGE_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.DynamicHelpImpl <em>Dynamic Help</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.DynamicHelpImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getDynamicHelp()
    * @generated
    */
   int DYNAMIC_HELP = 29;
 
   /**
    * The feature id for the '<em><b>Label</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DYNAMIC_HELP__LABEL = 0;
 
   /**
    * The feature id for the '<em><b>Description</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DYNAMIC_HELP__DESCRIPTION = 1;
 
   /**
    * The feature id for the '<em><b>Help Pages</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DYNAMIC_HELP__HELP_PAGES = 2;
 
   /**
    * The number of structural features of the '<em>Dynamic Help</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DYNAMIC_HELP_FEATURE_COUNT = 3;
 
   /**
    * The number of operations of the '<em>Dynamic Help</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int DYNAMIC_HELP_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.BindingImpl <em>Binding</em>}' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.impl.BindingImpl
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getBinding()
    * @generated
    */
   int BINDING = 30;
 
   /**
    * The feature id for the '<em><b>Key Sequence</b></em>' attribute.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BINDING__KEY_SEQUENCE = 0;
 
   /**
    * The feature id for the '<em><b>Command</b></em>' reference.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BINDING__COMMAND = 1;
 
   /**
    * The feature id for the '<em><b>Contexts</b></em>' reference list.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BINDING__CONTEXTS = 2;
 
   /**
    * The number of structural features of the '<em>Binding</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BINDING_FEATURE_COUNT = 3;
 
   /**
    * The number of operations of the '<em>Binding</em>' class.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    * @ordered
    */
   int BINDING_OPERATION_COUNT = 0;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.Type <em>Type</em>}' enum.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.Type
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getType()
    * @generated
    */
   int TYPE = 31;
 
   /**
    * The meta object id for the '{@link org.obeonetwork.dsl.gen.eclipse.Location <em>Location</em>}' enum.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see org.obeonetwork.dsl.gen.eclipse.Location
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getLocation()
    * @generated
    */
   int LOCATION = 32;
 
   /**
    * The meta object id for the '<em>Version</em>' data type.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see java.lang.String
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getVersion()
    * @generated
    */
   int VERSION = 33;
 
   /**
    * The meta object id for the '<em>Namespace</em>' data type.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see java.lang.String
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getNamespace()
    * @generated
    */
   int NAMESPACE = 34;
 
   /**
    * The meta object id for the '<em>Java Name</em>' data type.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see java.lang.String
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getJavaName()
    * @generated
    */
   int JAVA_NAME = 35;
 
   /**
    * The meta object id for the '<em>Name</em>' data type.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @see java.lang.String
    * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getName_()
    * @generated
    */
   int NAME = 36;
 
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Application <em>Application</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Application</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application
    * @generated
    */
   EClass getApplication();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getName()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getApplicationID <em>Application ID</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Application ID</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getApplicationID()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_ApplicationID();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getProvider <em>Provider</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Provider</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getProvider()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_Provider();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getCopyright <em>Copyright</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Copyright</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getCopyright()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_Copyright();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getLicense <em>License</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>License</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getLicense()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_License();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getVersion <em>Version</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Version</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getVersion()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_Version();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getDescription()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_Description();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#getBaseNamespace <em>Base Namespace</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Base Namespace</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getBaseNamespace()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_BaseNamespace();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Application#isMavenCompilation <em>Maven Compilation</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Maven Compilation</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#isMavenCompilation()
    * @see #getApplication()
    * @generated
    */
   EAttribute getApplication_MavenCompilation();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Application#getProjects <em>Projects</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Projects</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Application#getProjects()
    * @see #getApplication()
    * @generated
    */
   EReference getApplication_Projects();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Project <em>Project</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Project</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Project
    * @generated
    */
   EClass getProject();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Project#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Project#getName()
    * @see #getProject()
    * @generated
    */
   EAttribute getProject_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Project#getID <em>ID</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>ID</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Project#getID()
    * @see #getProject()
    * @generated
    */
   EAttribute getProject_ID();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Repository <em>Repository</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Repository</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Repository
    * @generated
    */
   EClass getRepository();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Repository#getRepositoryCategories <em>Repository Categories</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Repository Categories</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Repository#getRepositoryCategories()
    * @see #getRepository()
    * @generated
    */
   EReference getRepository_RepositoryCategories();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.RepositoryCategory <em>Repository Category</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Repository Category</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RepositoryCategory
    * @generated
    */
   EClass getRepositoryCategory();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getLabel <em>Label</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Label</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getLabel()
    * @see #getRepositoryCategory()
    * @generated
    */
   EAttribute getRepositoryCategory_Label();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getDescription()
    * @see #getRepositoryCategory()
    * @generated
    */
   EAttribute getRepositoryCategory_Description();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getID <em>ID</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>ID</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getID()
    * @see #getRepositoryCategory()
    * @generated
    */
   EAttribute getRepositoryCategory_ID();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getFeatures <em>Features</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Features</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RepositoryCategory#getFeatures()
    * @see #getRepositoryCategory()
    * @generated
    */
   EReference getRepositoryCategory_Features();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Feature <em>Feature</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Feature</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature
    * @generated
    */
   EClass getFeature();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Feature#getCopyright <em>Copyright</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Copyright</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature#getCopyright()
    * @see #getFeature()
    * @generated
    */
   EAttribute getFeature_Copyright();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Feature#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature#getDescription()
    * @see #getFeature()
    * @generated
    */
   EAttribute getFeature_Description();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Feature#getVersion <em>Version</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Version</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature#getVersion()
    * @see #getFeature()
    * @generated
    */
   EAttribute getFeature_Version();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Feature#getLicense <em>License</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>License</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature#getLicense()
    * @see #getFeature()
    * @generated
    */
   EAttribute getFeature_License();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Feature#getProvider <em>Provider</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Provider</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature#getProvider()
    * @see #getFeature()
    * @generated
    */
   EAttribute getFeature_Provider();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Feature#getBundles <em>Bundles</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Bundles</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Feature#getBundles()
    * @see #getFeature()
    * @generated
    */
   EReference getFeature_Bundles();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Bundle <em>Bundle</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Bundle</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle
    * @generated
    */
   EClass getBundle();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getVersion <em>Version</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Version</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getVersion()
    * @see #getBundle()
    * @generated
    */
   EAttribute getBundle_Version();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getRequiredEnvironment <em>Required Environment</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Required Environment</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getRequiredEnvironment()
    * @see #getBundle()
    * @generated
    */
   EAttribute getBundle_RequiredEnvironment();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getVendor <em>Vendor</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Vendor</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getVendor()
    * @see #getBundle()
    * @generated
    */
   EAttribute getBundle_Vendor();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getDeclarativeServices <em>Declarative Services</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Declarative Services</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getDeclarativeServices()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_DeclarativeServices();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getImportedPackageDeclarations <em>Imported Package Declarations</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Imported Package Declarations</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getImportedPackageDeclarations()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_ImportedPackageDeclarations();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getNatures <em>Natures</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Natures</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getNatures()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Natures();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getBuilders <em>Builders</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Builders</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getBuilders()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Builders();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getWizards <em>Wizards</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Wizards</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getWizards()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Wizards();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getExtensionPoints <em>Extension Points</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Extension Points</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getExtensionPoints()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_ExtensionPoints();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getDecorators <em>Decorators</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Decorators</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getDecorators()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Decorators();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getMarkers <em>Markers</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Markers</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getMarkers()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Markers();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getPerspectives <em>Perspectives</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Perspectives</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getPerspectives()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Perspectives();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getEditors <em>Editors</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Editors</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getEditors()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Editors();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getViews <em>Views</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Views</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getViews()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Views();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getHelpContents <em>Help Contents</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Help Contents</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getHelpContents()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_HelpContents();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getCommands <em>Commands</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Commands</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getCommands()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Commands();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getMenus <em>Menus</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Menus</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getMenus()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Menus();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getHandlers <em>Handlers</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Handlers</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getHandlers()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Handlers();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getContexts <em>Contexts</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Contexts</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getContexts()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Contexts();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getCategories <em>Categories</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Categories</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getCategories()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Categories();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getBindings <em>Bindings</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Bindings</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getBindings()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_Bindings();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getExportedPackages <em>Exported Packages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Exported Packages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getExportedPackages()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_ExportedPackages();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Bundle#getOwnedPackages <em>Owned Packages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Owned Packages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Bundle#getOwnedPackages()
    * @see #getBundle()
    * @generated
    */
   EReference getBundle_OwnedPackages();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.DeclarativeService <em>Declarative Service</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Declarative Service</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DeclarativeService
    * @generated
    */
   EClass getDeclarativeService();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getName()
    * @see #getDeclarativeService()
    * @generated
    */
   EAttribute getDeclarativeService_Name();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getImplentationClass <em>Implentation Class</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Implentation Class</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getImplentationClass()
    * @see #getDeclarativeService()
    * @generated
    */
   EReference getDeclarativeService_ImplentationClass();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getProvidedServices <em>Provided Services</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Provided Services</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getProvidedServices()
    * @see #getDeclarativeService()
    * @generated
    */
   EReference getDeclarativeService_ProvidedServices();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getRequiredServices <em>Required Services</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Required Services</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DeclarativeService#getRequiredServices()
    * @see #getDeclarativeService()
    * @generated
    */
   EReference getDeclarativeService_RequiredServices();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.ProvidedService <em>Provided Service</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Provided Service</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ProvidedService
    * @generated
    */
   EClass getProvidedService();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.ProvidedService#getInterface <em>Interface</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Interface</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ProvidedService#getInterface()
    * @see #getProvidedService()
    * @generated
    */
   EReference getProvidedService_Interface();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.RequiredService <em>Required Service</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Required Service</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RequiredService
    * @generated
    */
   EClass getRequiredService();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RequiredService#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RequiredService#getName()
    * @see #getRequiredService()
    * @generated
    */
   EAttribute getRequiredService_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RequiredService#getLowerBound <em>Lower Bound</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Lower Bound</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RequiredService#getLowerBound()
    * @see #getRequiredService()
    * @generated
    */
   EAttribute getRequiredService_LowerBound();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RequiredService#getUpperBound <em>Upper Bound</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Upper Bound</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RequiredService#getUpperBound()
    * @see #getRequiredService()
    * @generated
    */
   EAttribute getRequiredService_UpperBound();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.RequiredService#getBind <em>Bind</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Bind</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RequiredService#getBind()
    * @see #getRequiredService()
    * @generated
    */
   EAttribute getRequiredService_Bind();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.RequiredService#getInterface <em>Interface</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Interface</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.RequiredService#getInterface()
    * @see #getRequiredService()
    * @generated
    */
   EReference getRequiredService_Interface();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.ImportedPackageDeclaration <em>Imported Package Declaration</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Imported Package Declaration</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ImportedPackageDeclaration
    * @generated
    */
   EClass getImportedPackageDeclaration();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.ImportedPackageDeclaration#getPackageName <em>Package Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Package Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ImportedPackageDeclaration#getPackageName()
    * @see #getImportedPackageDeclaration()
    * @generated
    */
   EAttribute getImportedPackageDeclaration_PackageName();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.ImportedPackageDeclaration#getPackageVersion <em>Package Version</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Package Version</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ImportedPackageDeclaration#getPackageVersion()
    * @see #getImportedPackageDeclaration()
    * @generated
    */
   EAttribute getImportedPackageDeclaration_PackageVersion();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Builder <em>Builder</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Builder</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Builder
    * @generated
    */
   EClass getBuilder();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Builder#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Builder#getName()
    * @see #getBuilder()
    * @generated
    */
   EAttribute getBuilder_Name();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Builder#getNatures <em>Natures</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Natures</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Builder#getNatures()
    * @see #getBuilder()
    * @generated
    */
   EReference getBuilder_Natures();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Nature <em>Nature</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Nature</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Nature
    * @generated
    */
   EClass getNature();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Nature#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Nature#getName()
    * @see #getNature()
    * @generated
    */
   EAttribute getNature_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Nature#isHasToggleNature <em>Has Toggle Nature</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Has Toggle Nature</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Nature#isHasToggleNature()
    * @see #getNature()
    * @generated
    */
   EAttribute getNature_HasToggleNature();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Nature#getBuilders <em>Builders</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Builders</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Nature#getBuilders()
    * @see #getNature()
    * @generated
    */
   EReference getNature_Builders();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Wizard <em>Wizard</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Wizard</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard
    * @generated
    */
   EClass getWizard();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Wizard#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard#getName()
    * @see #getWizard()
    * @generated
    */
   EAttribute getWizard_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Wizard#getTitle <em>Title</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Title</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard#getTitle()
    * @see #getWizard()
    * @generated
    */
   EAttribute getWizard_Title();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Wizard#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard#getDescription()
    * @see #getWizard()
    * @generated
    */
   EAttribute getWizard_Description();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Wizard#getIcon <em>Icon</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Icon</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard#getIcon()
    * @see #getWizard()
    * @generated
    */
   EAttribute getWizard_Icon();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Wizard#isIsProject <em>Is Project</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Is Project</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard#isIsProject()
    * @see #getWizard()
    * @generated
    */
   EAttribute getWizard_IsProject();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Wizard#getCategory <em>Category</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Category</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Wizard#getCategory()
    * @see #getWizard()
    * @generated
    */
   EReference getWizard_Category();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.ProjectWizard <em>Project Wizard</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Project Wizard</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ProjectWizard
    * @generated
    */
   EClass getProjectWizard();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.ProjectWizard#getNatures <em>Natures</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Natures</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ProjectWizard#getNatures()
    * @see #getProjectWizard()
    * @generated
    */
   EReference getProjectWizard_Natures();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.ExtensionPoint <em>Extension Point</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Extension Point</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ExtensionPoint
    * @generated
    */
   EClass getExtensionPoint();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.ExtensionPoint#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ExtensionPoint#getName()
    * @see #getExtensionPoint()
    * @generated
    */
   EAttribute getExtensionPoint_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.ExtensionPoint#isRequired <em>Required</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Required</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ExtensionPoint#isRequired()
    * @see #getExtensionPoint()
    * @generated
    */
   EAttribute getExtensionPoint_Required();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.ExtensionPoint#getAttributes <em>Attributes</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Attributes</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.ExtensionPoint#getAttributes()
    * @see #getExtensionPoint()
    * @generated
    */
   EReference getExtensionPoint_Attributes();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Attribute <em>Attribute</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Attribute</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Attribute
    * @generated
    */
   EClass getAttribute();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Attribute#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Attribute#getName()
    * @see #getAttribute()
    * @generated
    */
   EAttribute getAttribute_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Attribute#isRequired <em>Required</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Required</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Attribute#isRequired()
    * @see #getAttribute()
    * @generated
    */
   EAttribute getAttribute_Required();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Attribute#isTranslatable <em>Translatable</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Translatable</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Attribute#isTranslatable()
    * @see #getAttribute()
    * @generated
    */
   EAttribute getAttribute_Translatable();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Attribute#getType <em>Type</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Type</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Attribute#getType()
    * @see #getAttribute()
    * @generated
    */
   EAttribute getAttribute_Type();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Decorator <em>Decorator</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Decorator</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Decorator
    * @generated
    */
   EClass getDecorator();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Decorator#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Decorator#getName()
    * @see #getDecorator()
    * @generated
    */
   EAttribute getDecorator_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Decorator#getIcon <em>Icon</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Icon</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Decorator#getIcon()
    * @see #getDecorator()
    * @generated
    */
   EAttribute getDecorator_Icon();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Decorator#isIsLightweight <em>Is Lightweight</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Is Lightweight</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Decorator#isIsLightweight()
    * @see #getDecorator()
    * @generated
    */
   EAttribute getDecorator_IsLightweight();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Decorator#isIsAdaptable <em>Is Adaptable</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Is Adaptable</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Decorator#isIsAdaptable()
    * @see #getDecorator()
    * @generated
    */
   EAttribute getDecorator_IsAdaptable();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Decorator#getLocation <em>Location</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Location</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Decorator#getLocation()
    * @see #getDecorator()
    * @generated
    */
   EAttribute getDecorator_Location();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Marker <em>Marker</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Marker</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Marker
    * @generated
    */
   EClass getMarker();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Marker#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Marker#getName()
    * @see #getMarker()
    * @generated
    */
   EAttribute getMarker_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Marker#isIsPersistant <em>Is Persistant</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Is Persistant</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Marker#isIsPersistant()
    * @see #getMarker()
    * @generated
    */
   EAttribute getMarker_IsPersistant();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Context <em>Context</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Context</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Context
    * @generated
    */
   EClass getContext();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Context#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Context#getName()
    * @see #getContext()
    * @generated
    */
   EAttribute getContext_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Context#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Context#getDescription()
    * @see #getContext()
    * @generated
    */
   EAttribute getContext_Description();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Context#getContexts <em>Contexts</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Contexts</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Context#getContexts()
    * @see #getContext()
    * @generated
    */
   EReference getContext_Contexts();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Perspective <em>Perspective</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Perspective</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective
    * @generated
    */
   EClass getPerspective();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Perspective#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective#getName()
    * @see #getPerspective()
    * @generated
    */
   EAttribute getPerspective_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Perspective#getIcon <em>Icon</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Icon</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective#getIcon()
    * @see #getPerspective()
    * @generated
    */
   EAttribute getPerspective_Icon();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Perspective#getHandlers <em>Handlers</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Handlers</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective#getHandlers()
    * @see #getPerspective()
    * @generated
    */
   EReference getPerspective_Handlers();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Perspective#getMenus <em>Menus</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Menus</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective#getMenus()
    * @see #getPerspective()
    * @generated
    */
   EReference getPerspective_Menus();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Perspective#getWizards <em>Wizards</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Wizards</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective#getWizards()
    * @see #getPerspective()
    * @generated
    */
   EReference getPerspective_Wizards();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Perspective#getViews <em>Views</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Views</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Perspective#getViews()
    * @see #getPerspective()
    * @generated
    */
   EReference getPerspective_Views();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Category <em>Category</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Category</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Category
    * @generated
    */
   EClass getCategory();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Category#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Category#getName()
    * @see #getCategory()
    * @generated
    */
   EAttribute getCategory_Name();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Category#getCommands <em>Commands</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Commands</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Category#getCommands()
    * @see #getCategory()
    * @generated
    */
   EReference getCategory_Commands();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Category#getViews <em>Views</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Views</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Category#getViews()
    * @see #getCategory()
    * @generated
    */
   EReference getCategory_Views();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Category#getWizards <em>Wizards</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Wizards</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Category#getWizards()
    * @see #getCategory()
    * @generated
    */
   EReference getCategory_Wizards();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Part <em>Part</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Part</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Part
    * @generated
    */
   EClass getPart();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Part#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Part#getName()
    * @see #getPart()
    * @generated
    */
   EAttribute getPart_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Part#getIcon <em>Icon</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Icon</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Part#getIcon()
    * @see #getPart()
    * @generated
    */
   EAttribute getPart_Icon();
 
   /**
    * Returns the meta object for the containment reference '{@link org.obeonetwork.dsl.gen.eclipse.Part#getDynamicHelp <em>Dynamic Help</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference '<em>Dynamic Help</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Part#getDynamicHelp()
    * @see #getPart()
    * @generated
    */
   EReference getPart_DynamicHelp();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.View <em>View</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>View</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View
    * @generated
    */
   EClass getView();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.View#isIsTree <em>Is Tree</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Is Tree</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View#isIsTree()
    * @see #getView()
    * @generated
    */
   EAttribute getView_IsTree();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.View#isIsVisible <em>Is Visible</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Is Visible</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View#isIsVisible()
    * @see #getView()
    * @generated
    */
   EAttribute getView_IsVisible();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.View#getHandlers <em>Handlers</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Handlers</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View#getHandlers()
    * @see #getView()
    * @generated
    */
   EReference getView_Handlers();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.View#getMenus <em>Menus</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Menus</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View#getMenus()
    * @see #getView()
    * @generated
    */
   EReference getView_Menus();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.View#getPerspectives <em>Perspectives</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Perspectives</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View#getPerspectives()
    * @see #getView()
    * @generated
    */
   EReference getView_Perspectives();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.View#getCategory <em>Category</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Category</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.View#getCategory()
    * @see #getView()
    * @generated
    */
   EReference getView_Category();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Editor <em>Editor</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Editor</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Editor
    * @generated
    */
   EClass getEditor();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Editor#getExtension <em>Extension</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Extension</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Editor#getExtension()
    * @see #getEditor()
    * @generated
    */
   EAttribute getEditor_Extension();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Editor#isDynamicMenu <em>Dynamic Menu</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Dynamic Menu</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Editor#isDynamicMenu()
    * @see #getEditor()
    * @generated
    */
   EAttribute getEditor_DynamicMenu();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Editor#getHandlers <em>Handlers</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Handlers</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Editor#getHandlers()
    * @see #getEditor()
    * @generated
    */
   EReference getEditor_Handlers();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.Editor#getMenus <em>Menus</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Menus</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Editor#getMenus()
    * @see #getEditor()
    * @generated
    */
   EReference getEditor_Menus();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Menu <em>Menu</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Menu</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Menu
    * @generated
    */
   EClass getMenu();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Menu#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Menu#getName()
    * @see #getMenu()
    * @generated
    */
   EAttribute getMenu_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Menu#getMnemonic <em>Mnemonic</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Mnemonic</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Menu#getMnemonic()
    * @see #getMenu()
    * @generated
    */
   EAttribute getMenu_Mnemonic();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Menu#isMenuContribution <em>Menu Contribution</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Menu Contribution</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Menu#isMenuContribution()
    * @see #getMenu()
    * @generated
    */
   EAttribute getMenu_MenuContribution();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Menu#isToolbarContribution <em>Toolbar Contribution</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Toolbar Contribution</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Menu#isToolbarContribution()
    * @see #getMenu()
    * @generated
    */
   EAttribute getMenu_ToolbarContribution();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Menu#getCommands <em>Commands</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Commands</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Menu#getCommands()
    * @see #getMenu()
    * @generated
    */
   EReference getMenu_Commands();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Command <em>Command</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Command</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command
    * @generated
    */
   EClass getCommand();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Command#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getName()
    * @see #getCommand()
    * @generated
    */
   EAttribute getCommand_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Command#getIcon <em>Icon</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Icon</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getIcon()
    * @see #getCommand()
    * @generated
    */
   EAttribute getCommand_Icon();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Command#getTooltip <em>Tooltip</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Tooltip</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getTooltip()
    * @see #getCommand()
    * @generated
    */
   EAttribute getCommand_Tooltip();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Command#getHandler <em>Handler</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Handler</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getHandler()
    * @see #getCommand()
    * @generated
    */
   EReference getCommand_Handler();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Command#getMenu <em>Menu</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Menu</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getMenu()
    * @see #getCommand()
    * @generated
    */
   EReference getCommand_Menu();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Command#getCategory <em>Category</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Category</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getCategory()
    * @see #getCommand()
    * @generated
    */
   EReference getCommand_Category();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Command#getBinding <em>Binding</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Binding</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Command#getBinding()
    * @see #getCommand()
    * @generated
    */
   EReference getCommand_Binding();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Handler <em>Handler</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Handler</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Handler
    * @generated
    */
   EClass getHandler();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Handler#getName <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Name</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Handler#getName()
    * @see #getHandler()
    * @generated
    */
   EAttribute getHandler_Name();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Handler#getMnemonic <em>Mnemonic</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Mnemonic</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Handler#getMnemonic()
    * @see #getHandler()
    * @generated
    */
   EAttribute getHandler_Mnemonic();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Handler#getCommand <em>Command</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Command</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Handler#getCommand()
    * @see #getHandler()
    * @generated
    */
   EReference getHandler_Command();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Handler#getContexts <em>Contexts</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Contexts</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Handler#getContexts()
    * @see #getHandler()
    * @generated
    */
   EReference getHandler_Contexts();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.HelpContents <em>Help Contents</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Help Contents</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpContents
    * @generated
    */
   EClass getHelpContents();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.HelpContents#getLabel <em>Label</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Label</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpContents#getLabel()
    * @see #getHelpContents()
    * @generated
    */
   EAttribute getHelpContents_Label();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.HelpContents#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpContents#getDescription()
    * @see #getHelpContents()
    * @generated
    */
   EAttribute getHelpContents_Description();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.HelpContents#getHelpPages <em>Help Pages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Help Pages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpContents#getHelpPages()
    * @see #getHelpContents()
    * @generated
    */
   EReference getHelpContents_HelpPages();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.HelpContents#getExternalHelpPages <em>External Help Pages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>External Help Pages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpContents#getExternalHelpPages()
    * @see #getHelpContents()
    * @generated
    */
   EReference getHelpContents_ExternalHelpPages();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.HelpPage <em>Help Page</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Help Page</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpPage
    * @generated
    */
   EClass getHelpPage();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.HelpPage#getLabel <em>Label</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Label</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpPage#getLabel()
    * @see #getHelpPage()
    * @generated
    */
   EAttribute getHelpPage_Label();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.HelpPage#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpPage#getDescription()
    * @see #getHelpPage()
    * @generated
    */
   EAttribute getHelpPage_Description();
 
   /**
    * Returns the meta object for the containment reference list '{@link org.obeonetwork.dsl.gen.eclipse.HelpPage#getHelpPages <em>Help Pages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the containment reference list '<em>Help Pages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpPage#getHelpPages()
    * @see #getHelpPage()
    * @generated
    */
   EReference getHelpPage_HelpPages();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.HelpPage#getExternalHelpPages <em>External Help Pages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>External Help Pages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.HelpPage#getExternalHelpPages()
    * @see #getHelpPage()
    * @generated
    */
   EReference getHelpPage_ExternalHelpPages();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.DynamicHelp <em>Dynamic Help</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Dynamic Help</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DynamicHelp
    * @generated
    */
   EClass getDynamicHelp();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.DynamicHelp#getLabel <em>Label</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Label</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DynamicHelp#getLabel()
    * @see #getDynamicHelp()
    * @generated
    */
   EAttribute getDynamicHelp_Label();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.DynamicHelp#getDescription <em>Description</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Description</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DynamicHelp#getDescription()
    * @see #getDynamicHelp()
    * @generated
    */
   EAttribute getDynamicHelp_Description();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.DynamicHelp#getHelpPages <em>Help Pages</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Help Pages</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.DynamicHelp#getHelpPages()
    * @see #getDynamicHelp()
    * @generated
    */
   EReference getDynamicHelp_HelpPages();
 
   /**
    * Returns the meta object for class '{@link org.obeonetwork.dsl.gen.eclipse.Binding <em>Binding</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for class '<em>Binding</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Binding
    * @generated
    */
   EClass getBinding();
 
   /**
    * Returns the meta object for the attribute '{@link org.obeonetwork.dsl.gen.eclipse.Binding#getKeySequence <em>Key Sequence</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the attribute '<em>Key Sequence</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Binding#getKeySequence()
    * @see #getBinding()
    * @generated
    */
   EAttribute getBinding_KeySequence();
 
   /**
    * Returns the meta object for the reference '{@link org.obeonetwork.dsl.gen.eclipse.Binding#getCommand <em>Command</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference '<em>Command</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Binding#getCommand()
    * @see #getBinding()
    * @generated
    */
   EReference getBinding_Command();
 
   /**
    * Returns the meta object for the reference list '{@link org.obeonetwork.dsl.gen.eclipse.Binding#getContexts <em>Contexts</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for the reference list '<em>Contexts</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Binding#getContexts()
    * @see #getBinding()
    * @generated
    */
   EReference getBinding_Contexts();
 
   /**
    * Returns the meta object for enum '{@link org.obeonetwork.dsl.gen.eclipse.Type <em>Type</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for enum '<em>Type</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Type
    * @generated
    */
   EEnum getType();
 
   /**
    * Returns the meta object for enum '{@link org.obeonetwork.dsl.gen.eclipse.Location <em>Location</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for enum '<em>Location</em>'.
    * @see org.obeonetwork.dsl.gen.eclipse.Location
    * @generated
    */
   EEnum getLocation();
 
   /**
    * Returns the meta object for data type '{@link java.lang.String <em>Version</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for data type '<em>Version</em>'.
    * @see java.lang.String
    * @model instanceClass="java.lang.String"
    *        annotation="http://www.eclipse.org/emf/2002/GenModel create='boolean _and = false;\nboolean _notEquals = (!<%com.google.common.base.Objects%>.equal(it, null));\nif (!_notEquals)\n{\n\t_and = false;\n} else\n{\n\tboolean _matches = it.matches(\"\\\\d+\\\\.\\\\d+\\\\.\\\\d+\");\n\tboolean _not = (!_matches);\n\t_and = (_notEquals && _not);\n}\nif (_and)\n{\n\t<%java.lang.RuntimeException%> _runtimeException = new <%java.lang.RuntimeException%>(\"Bad format for version\");\n\tthrow _runtimeException;\n}\nreturn it;'"
    * @generated
    */
   EDataType getVersion();
 
   /**
    * Returns the meta object for data type '{@link java.lang.String <em>Namespace</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for data type '<em>Namespace</em>'.
    * @see java.lang.String
    * @model instanceClass="java.lang.String"
    *        annotation="http://www.eclipse.org/emf/2002/GenModel create='boolean _and = false;\nboolean _notEquals = (!<%com.google.common.base.Objects%>.equal(it, null));\nif (!_notEquals)\n{\n\t_and = false;\n} else\n{\n\tboolean _matches = it.matches(\"([a-z]+([0-9]*[A-Z]*[a-z]*)*\\\\.)*[a-z]+([0-9]*[A-Z]*[a-z]*)*\");\n\tboolean _not = (!_matches);\n\t_and = (_notEquals && _not);\n}\nif (_and)\n{\n\t<%java.lang.RuntimeException%> _runtimeException = new <%java.lang.RuntimeException%>(\"Bad format for ID\");\n\tthrow _runtimeException;\n}\nreturn it;'"
    * @generated
    */
   EDataType getNamespace();
 
   /**
    * Returns the meta object for data type '{@link java.lang.String <em>Java Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for data type '<em>Java Name</em>'.
    * @see java.lang.String
    * @model instanceClass="java.lang.String"
   *        annotation="http://www.eclipse.org/emf/2002/GenModel create='boolean _and = false;\nboolean _notEquals = (!<%com.google.common.base.Objects%>.equal(it, null));\nif (!_notEquals)\n{\n\t_and = false;\n} else\n{\n\tboolean _matches = it.matches(\"[A-Z]+([0-9]*[A-Z]*[a-z]*)*\");\n\tboolean _not = (!_matches);\n\t_and = (_notEquals && _not);\n}\nif (_and)\n{\n\t<%java.lang.RuntimeException%> _runtimeException = new <%java.lang.RuntimeException%>(\"Bad format for a java name\");\n\tthrow _runtimeException;\n}\nreturn it;'"
    * @generated
    */
   EDataType getJavaName();
 
   /**
    * Returns the meta object for data type '{@link java.lang.String <em>Name</em>}'.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the meta object for data type '<em>Name</em>'.
    * @see java.lang.String
    * @model instanceClass="java.lang.String"
    *        annotation="http://www.eclipse.org/emf/2002/GenModel create='boolean _and = false;\nboolean _notEquals = (!<%com.google.common.base.Objects%>.equal(it, null));\nif (!_notEquals)\n{\n\t_and = false;\n} else\n{\n\tboolean _matches = it.matches(\"([A-Z]*[a-z]+)+([ ]*[0-9]*[A-Z]*[a-z]*)*\");\n\tboolean _not = (!_matches);\n\t_and = (_notEquals && _not);\n}\nif (_and)\n{\n\t<%java.lang.RuntimeException%> _runtimeException = new <%java.lang.RuntimeException%>(\"Bad format for name\");\n\tthrow _runtimeException;\n}\nreturn it;'"
    * @generated
    */
   EDataType getName_();
 
   /**
    * Returns the factory that creates the instances of the model.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @return the factory that creates the instances of the model.
    * @generated
    */
   EclipseFactory getEclipseFactory();
 
   /**
    * <!-- begin-user-doc -->
    * Defines literals for the meta objects that represent
    * <ul>
    *   <li>each class,</li>
    *   <li>each feature of each class,</li>
    *   <li>each operation of each class,</li>
    *   <li>each enum,</li>
    *   <li>and each data type</li>
    * </ul>
    * <!-- end-user-doc -->
    * @generated
    */
   interface Literals
   {
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ApplicationImpl <em>Application</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ApplicationImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getApplication()
      * @generated
      */
     EClass APPLICATION = eINSTANCE.getApplication();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__NAME = eINSTANCE.getApplication_Name();
 
     /**
      * The meta object literal for the '<em><b>Application ID</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__APPLICATION_ID = eINSTANCE.getApplication_ApplicationID();
 
     /**
      * The meta object literal for the '<em><b>Provider</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__PROVIDER = eINSTANCE.getApplication_Provider();
 
     /**
      * The meta object literal for the '<em><b>Copyright</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__COPYRIGHT = eINSTANCE.getApplication_Copyright();
 
     /**
      * The meta object literal for the '<em><b>License</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__LICENSE = eINSTANCE.getApplication_License();
 
     /**
      * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__VERSION = eINSTANCE.getApplication_Version();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__DESCRIPTION = eINSTANCE.getApplication_Description();
 
     /**
      * The meta object literal for the '<em><b>Base Namespace</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__BASE_NAMESPACE = eINSTANCE.getApplication_BaseNamespace();
 
     /**
      * The meta object literal for the '<em><b>Maven Compilation</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute APPLICATION__MAVEN_COMPILATION = eINSTANCE.getApplication_MavenCompilation();
 
     /**
      * The meta object literal for the '<em><b>Projects</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference APPLICATION__PROJECTS = eINSTANCE.getApplication_Projects();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ProjectImpl <em>Project</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ProjectImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getProject()
      * @generated
      */
     EClass PROJECT = eINSTANCE.getProject();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute PROJECT__NAME = eINSTANCE.getProject_Name();
 
     /**
      * The meta object literal for the '<em><b>ID</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute PROJECT__ID = eINSTANCE.getProject_ID();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.RepositoryImpl <em>Repository</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.RepositoryImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getRepository()
      * @generated
      */
     EClass REPOSITORY = eINSTANCE.getRepository();
 
     /**
      * The meta object literal for the '<em><b>Repository Categories</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference REPOSITORY__REPOSITORY_CATEGORIES = eINSTANCE.getRepository_RepositoryCategories();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.RepositoryCategoryImpl <em>Repository Category</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.RepositoryCategoryImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getRepositoryCategory()
      * @generated
      */
     EClass REPOSITORY_CATEGORY = eINSTANCE.getRepositoryCategory();
 
     /**
      * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REPOSITORY_CATEGORY__LABEL = eINSTANCE.getRepositoryCategory_Label();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REPOSITORY_CATEGORY__DESCRIPTION = eINSTANCE.getRepositoryCategory_Description();
 
     /**
      * The meta object literal for the '<em><b>ID</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REPOSITORY_CATEGORY__ID = eINSTANCE.getRepositoryCategory_ID();
 
     /**
      * The meta object literal for the '<em><b>Features</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference REPOSITORY_CATEGORY__FEATURES = eINSTANCE.getRepositoryCategory_Features();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.FeatureImpl <em>Feature</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.FeatureImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getFeature()
      * @generated
      */
     EClass FEATURE = eINSTANCE.getFeature();
 
     /**
      * The meta object literal for the '<em><b>Copyright</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute FEATURE__COPYRIGHT = eINSTANCE.getFeature_Copyright();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute FEATURE__DESCRIPTION = eINSTANCE.getFeature_Description();
 
     /**
      * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute FEATURE__VERSION = eINSTANCE.getFeature_Version();
 
     /**
      * The meta object literal for the '<em><b>License</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute FEATURE__LICENSE = eINSTANCE.getFeature_License();
 
     /**
      * The meta object literal for the '<em><b>Provider</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute FEATURE__PROVIDER = eINSTANCE.getFeature_Provider();
 
     /**
      * The meta object literal for the '<em><b>Bundles</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference FEATURE__BUNDLES = eINSTANCE.getFeature_Bundles();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.BundleImpl <em>Bundle</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.BundleImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getBundle()
      * @generated
      */
     EClass BUNDLE = eINSTANCE.getBundle();
 
     /**
      * The meta object literal for the '<em><b>Version</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute BUNDLE__VERSION = eINSTANCE.getBundle_Version();
 
     /**
      * The meta object literal for the '<em><b>Required Environment</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute BUNDLE__REQUIRED_ENVIRONMENT = eINSTANCE.getBundle_RequiredEnvironment();
 
     /**
      * The meta object literal for the '<em><b>Vendor</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute BUNDLE__VENDOR = eINSTANCE.getBundle_Vendor();
 
     /**
      * The meta object literal for the '<em><b>Declarative Services</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__DECLARATIVE_SERVICES = eINSTANCE.getBundle_DeclarativeServices();
 
     /**
      * The meta object literal for the '<em><b>Imported Package Declarations</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__IMPORTED_PACKAGE_DECLARATIONS = eINSTANCE.getBundle_ImportedPackageDeclarations();
 
     /**
      * The meta object literal for the '<em><b>Natures</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__NATURES = eINSTANCE.getBundle_Natures();
 
     /**
      * The meta object literal for the '<em><b>Builders</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__BUILDERS = eINSTANCE.getBundle_Builders();
 
     /**
      * The meta object literal for the '<em><b>Wizards</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__WIZARDS = eINSTANCE.getBundle_Wizards();
 
     /**
      * The meta object literal for the '<em><b>Extension Points</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__EXTENSION_POINTS = eINSTANCE.getBundle_ExtensionPoints();
 
     /**
      * The meta object literal for the '<em><b>Decorators</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__DECORATORS = eINSTANCE.getBundle_Decorators();
 
     /**
      * The meta object literal for the '<em><b>Markers</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__MARKERS = eINSTANCE.getBundle_Markers();
 
     /**
      * The meta object literal for the '<em><b>Perspectives</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__PERSPECTIVES = eINSTANCE.getBundle_Perspectives();
 
     /**
      * The meta object literal for the '<em><b>Editors</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__EDITORS = eINSTANCE.getBundle_Editors();
 
     /**
      * The meta object literal for the '<em><b>Views</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__VIEWS = eINSTANCE.getBundle_Views();
 
     /**
      * The meta object literal for the '<em><b>Help Contents</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__HELP_CONTENTS = eINSTANCE.getBundle_HelpContents();
 
     /**
      * The meta object literal for the '<em><b>Commands</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__COMMANDS = eINSTANCE.getBundle_Commands();
 
     /**
      * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__MENUS = eINSTANCE.getBundle_Menus();
 
     /**
      * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__HANDLERS = eINSTANCE.getBundle_Handlers();
 
     /**
      * The meta object literal for the '<em><b>Contexts</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__CONTEXTS = eINSTANCE.getBundle_Contexts();
 
     /**
      * The meta object literal for the '<em><b>Categories</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__CATEGORIES = eINSTANCE.getBundle_Categories();
 
     /**
      * The meta object literal for the '<em><b>Bindings</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__BINDINGS = eINSTANCE.getBundle_Bindings();
 
     /**
      * The meta object literal for the '<em><b>Exported Packages</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__EXPORTED_PACKAGES = eINSTANCE.getBundle_ExportedPackages();
 
     /**
      * The meta object literal for the '<em><b>Owned Packages</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUNDLE__OWNED_PACKAGES = eINSTANCE.getBundle_OwnedPackages();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.DeclarativeServiceImpl <em>Declarative Service</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.DeclarativeServiceImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getDeclarativeService()
      * @generated
      */
     EClass DECLARATIVE_SERVICE = eINSTANCE.getDeclarativeService();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DECLARATIVE_SERVICE__NAME = eINSTANCE.getDeclarativeService_Name();
 
     /**
      * The meta object literal for the '<em><b>Implentation Class</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference DECLARATIVE_SERVICE__IMPLENTATION_CLASS = eINSTANCE.getDeclarativeService_ImplentationClass();
 
     /**
      * The meta object literal for the '<em><b>Provided Services</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference DECLARATIVE_SERVICE__PROVIDED_SERVICES = eINSTANCE.getDeclarativeService_ProvidedServices();
 
     /**
      * The meta object literal for the '<em><b>Required Services</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference DECLARATIVE_SERVICE__REQUIRED_SERVICES = eINSTANCE.getDeclarativeService_RequiredServices();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ProvidedServiceImpl <em>Provided Service</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ProvidedServiceImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getProvidedService()
      * @generated
      */
     EClass PROVIDED_SERVICE = eINSTANCE.getProvidedService();
 
     /**
      * The meta object literal for the '<em><b>Interface</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PROVIDED_SERVICE__INTERFACE = eINSTANCE.getProvidedService_Interface();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.RequiredServiceImpl <em>Required Service</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.RequiredServiceImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getRequiredService()
      * @generated
      */
     EClass REQUIRED_SERVICE = eINSTANCE.getRequiredService();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REQUIRED_SERVICE__NAME = eINSTANCE.getRequiredService_Name();
 
     /**
      * The meta object literal for the '<em><b>Lower Bound</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REQUIRED_SERVICE__LOWER_BOUND = eINSTANCE.getRequiredService_LowerBound();
 
     /**
      * The meta object literal for the '<em><b>Upper Bound</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REQUIRED_SERVICE__UPPER_BOUND = eINSTANCE.getRequiredService_UpperBound();
 
     /**
      * The meta object literal for the '<em><b>Bind</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute REQUIRED_SERVICE__BIND = eINSTANCE.getRequiredService_Bind();
 
     /**
      * The meta object literal for the '<em><b>Interface</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference REQUIRED_SERVICE__INTERFACE = eINSTANCE.getRequiredService_Interface();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ImportedPackageDeclarationImpl <em>Imported Package Declaration</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ImportedPackageDeclarationImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getImportedPackageDeclaration()
      * @generated
      */
     EClass IMPORTED_PACKAGE_DECLARATION = eINSTANCE.getImportedPackageDeclaration();
 
     /**
      * The meta object literal for the '<em><b>Package Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute IMPORTED_PACKAGE_DECLARATION__PACKAGE_NAME = eINSTANCE.getImportedPackageDeclaration_PackageName();
 
     /**
      * The meta object literal for the '<em><b>Package Version</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute IMPORTED_PACKAGE_DECLARATION__PACKAGE_VERSION = eINSTANCE.getImportedPackageDeclaration_PackageVersion();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.BuilderImpl <em>Builder</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.BuilderImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getBuilder()
      * @generated
      */
     EClass BUILDER = eINSTANCE.getBuilder();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute BUILDER__NAME = eINSTANCE.getBuilder_Name();
 
     /**
      * The meta object literal for the '<em><b>Natures</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BUILDER__NATURES = eINSTANCE.getBuilder_Natures();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.NatureImpl <em>Nature</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.NatureImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getNature()
      * @generated
      */
     EClass NATURE = eINSTANCE.getNature();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute NATURE__NAME = eINSTANCE.getNature_Name();
 
     /**
      * The meta object literal for the '<em><b>Has Toggle Nature</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute NATURE__HAS_TOGGLE_NATURE = eINSTANCE.getNature_HasToggleNature();
 
     /**
      * The meta object literal for the '<em><b>Builders</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference NATURE__BUILDERS = eINSTANCE.getNature_Builders();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.WizardImpl <em>Wizard</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.WizardImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getWizard()
      * @generated
      */
     EClass WIZARD = eINSTANCE.getWizard();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute WIZARD__NAME = eINSTANCE.getWizard_Name();
 
     /**
      * The meta object literal for the '<em><b>Title</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute WIZARD__TITLE = eINSTANCE.getWizard_Title();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute WIZARD__DESCRIPTION = eINSTANCE.getWizard_Description();
 
     /**
      * The meta object literal for the '<em><b>Icon</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute WIZARD__ICON = eINSTANCE.getWizard_Icon();
 
     /**
      * The meta object literal for the '<em><b>Is Project</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute WIZARD__IS_PROJECT = eINSTANCE.getWizard_IsProject();
 
     /**
      * The meta object literal for the '<em><b>Category</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference WIZARD__CATEGORY = eINSTANCE.getWizard_Category();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ProjectWizardImpl <em>Project Wizard</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ProjectWizardImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getProjectWizard()
      * @generated
      */
     EClass PROJECT_WIZARD = eINSTANCE.getProjectWizard();
 
     /**
      * The meta object literal for the '<em><b>Natures</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PROJECT_WIZARD__NATURES = eINSTANCE.getProjectWizard_Natures();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ExtensionPointImpl <em>Extension Point</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ExtensionPointImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getExtensionPoint()
      * @generated
      */
     EClass EXTENSION_POINT = eINSTANCE.getExtensionPoint();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute EXTENSION_POINT__NAME = eINSTANCE.getExtensionPoint_Name();
 
     /**
      * The meta object literal for the '<em><b>Required</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute EXTENSION_POINT__REQUIRED = eINSTANCE.getExtensionPoint_Required();
 
     /**
      * The meta object literal for the '<em><b>Attributes</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference EXTENSION_POINT__ATTRIBUTES = eINSTANCE.getExtensionPoint_Attributes();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.AttributeImpl <em>Attribute</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.AttributeImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getAttribute()
      * @generated
      */
     EClass ATTRIBUTE = eINSTANCE.getAttribute();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute ATTRIBUTE__NAME = eINSTANCE.getAttribute_Name();
 
     /**
      * The meta object literal for the '<em><b>Required</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute ATTRIBUTE__REQUIRED = eINSTANCE.getAttribute_Required();
 
     /**
      * The meta object literal for the '<em><b>Translatable</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute ATTRIBUTE__TRANSLATABLE = eINSTANCE.getAttribute_Translatable();
 
     /**
      * The meta object literal for the '<em><b>Type</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute ATTRIBUTE__TYPE = eINSTANCE.getAttribute_Type();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.DecoratorImpl <em>Decorator</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.DecoratorImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getDecorator()
      * @generated
      */
     EClass DECORATOR = eINSTANCE.getDecorator();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DECORATOR__NAME = eINSTANCE.getDecorator_Name();
 
     /**
      * The meta object literal for the '<em><b>Icon</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DECORATOR__ICON = eINSTANCE.getDecorator_Icon();
 
     /**
      * The meta object literal for the '<em><b>Is Lightweight</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DECORATOR__IS_LIGHTWEIGHT = eINSTANCE.getDecorator_IsLightweight();
 
     /**
      * The meta object literal for the '<em><b>Is Adaptable</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DECORATOR__IS_ADAPTABLE = eINSTANCE.getDecorator_IsAdaptable();
 
     /**
      * The meta object literal for the '<em><b>Location</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DECORATOR__LOCATION = eINSTANCE.getDecorator_Location();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.MarkerImpl <em>Marker</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.MarkerImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getMarker()
      * @generated
      */
     EClass MARKER = eINSTANCE.getMarker();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute MARKER__NAME = eINSTANCE.getMarker_Name();
 
     /**
      * The meta object literal for the '<em><b>Is Persistant</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute MARKER__IS_PERSISTANT = eINSTANCE.getMarker_IsPersistant();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ContextImpl <em>Context</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ContextImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getContext()
      * @generated
      */
     EClass CONTEXT = eINSTANCE.getContext();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute CONTEXT__NAME = eINSTANCE.getContext_Name();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute CONTEXT__DESCRIPTION = eINSTANCE.getContext_Description();
 
     /**
      * The meta object literal for the '<em><b>Contexts</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference CONTEXT__CONTEXTS = eINSTANCE.getContext_Contexts();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.PerspectiveImpl <em>Perspective</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.PerspectiveImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getPerspective()
      * @generated
      */
     EClass PERSPECTIVE = eINSTANCE.getPerspective();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute PERSPECTIVE__NAME = eINSTANCE.getPerspective_Name();
 
     /**
      * The meta object literal for the '<em><b>Icon</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute PERSPECTIVE__ICON = eINSTANCE.getPerspective_Icon();
 
     /**
      * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PERSPECTIVE__HANDLERS = eINSTANCE.getPerspective_Handlers();
 
     /**
      * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PERSPECTIVE__MENUS = eINSTANCE.getPerspective_Menus();
 
     /**
      * The meta object literal for the '<em><b>Wizards</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PERSPECTIVE__WIZARDS = eINSTANCE.getPerspective_Wizards();
 
     /**
      * The meta object literal for the '<em><b>Views</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PERSPECTIVE__VIEWS = eINSTANCE.getPerspective_Views();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.CategoryImpl <em>Category</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.CategoryImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getCategory()
      * @generated
      */
     EClass CATEGORY = eINSTANCE.getCategory();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute CATEGORY__NAME = eINSTANCE.getCategory_Name();
 
     /**
      * The meta object literal for the '<em><b>Commands</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference CATEGORY__COMMANDS = eINSTANCE.getCategory_Commands();
 
     /**
      * The meta object literal for the '<em><b>Views</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference CATEGORY__VIEWS = eINSTANCE.getCategory_Views();
 
     /**
      * The meta object literal for the '<em><b>Wizards</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference CATEGORY__WIZARDS = eINSTANCE.getCategory_Wizards();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.PartImpl <em>Part</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.PartImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getPart()
      * @generated
      */
     EClass PART = eINSTANCE.getPart();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute PART__NAME = eINSTANCE.getPart_Name();
 
     /**
      * The meta object literal for the '<em><b>Icon</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute PART__ICON = eINSTANCE.getPart_Icon();
 
     /**
      * The meta object literal for the '<em><b>Dynamic Help</b></em>' containment reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference PART__DYNAMIC_HELP = eINSTANCE.getPart_DynamicHelp();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.ViewImpl <em>View</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.ViewImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getView()
      * @generated
      */
     EClass VIEW = eINSTANCE.getView();
 
     /**
      * The meta object literal for the '<em><b>Is Tree</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute VIEW__IS_TREE = eINSTANCE.getView_IsTree();
 
     /**
      * The meta object literal for the '<em><b>Is Visible</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute VIEW__IS_VISIBLE = eINSTANCE.getView_IsVisible();
 
     /**
      * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference VIEW__HANDLERS = eINSTANCE.getView_Handlers();
 
     /**
      * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference VIEW__MENUS = eINSTANCE.getView_Menus();
 
     /**
      * The meta object literal for the '<em><b>Perspectives</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference VIEW__PERSPECTIVES = eINSTANCE.getView_Perspectives();
 
     /**
      * The meta object literal for the '<em><b>Category</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference VIEW__CATEGORY = eINSTANCE.getView_Category();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.EditorImpl <em>Editor</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EditorImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getEditor()
      * @generated
      */
     EClass EDITOR = eINSTANCE.getEditor();
 
     /**
      * The meta object literal for the '<em><b>Extension</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute EDITOR__EXTENSION = eINSTANCE.getEditor_Extension();
 
     /**
      * The meta object literal for the '<em><b>Dynamic Menu</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute EDITOR__DYNAMIC_MENU = eINSTANCE.getEditor_DynamicMenu();
 
     /**
      * The meta object literal for the '<em><b>Handlers</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference EDITOR__HANDLERS = eINSTANCE.getEditor_Handlers();
 
     /**
      * The meta object literal for the '<em><b>Menus</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference EDITOR__MENUS = eINSTANCE.getEditor_Menus();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.MenuImpl <em>Menu</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.MenuImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getMenu()
      * @generated
      */
     EClass MENU = eINSTANCE.getMenu();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute MENU__NAME = eINSTANCE.getMenu_Name();
 
     /**
      * The meta object literal for the '<em><b>Mnemonic</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute MENU__MNEMONIC = eINSTANCE.getMenu_Mnemonic();
 
     /**
      * The meta object literal for the '<em><b>Menu Contribution</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute MENU__MENU_CONTRIBUTION = eINSTANCE.getMenu_MenuContribution();
 
     /**
      * The meta object literal for the '<em><b>Toolbar Contribution</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute MENU__TOOLBAR_CONTRIBUTION = eINSTANCE.getMenu_ToolbarContribution();
 
     /**
      * The meta object literal for the '<em><b>Commands</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference MENU__COMMANDS = eINSTANCE.getMenu_Commands();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.CommandImpl <em>Command</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.CommandImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getCommand()
      * @generated
      */
     EClass COMMAND = eINSTANCE.getCommand();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute COMMAND__NAME = eINSTANCE.getCommand_Name();
 
     /**
      * The meta object literal for the '<em><b>Icon</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute COMMAND__ICON = eINSTANCE.getCommand_Icon();
 
     /**
      * The meta object literal for the '<em><b>Tooltip</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute COMMAND__TOOLTIP = eINSTANCE.getCommand_Tooltip();
 
     /**
      * The meta object literal for the '<em><b>Handler</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference COMMAND__HANDLER = eINSTANCE.getCommand_Handler();
 
     /**
      * The meta object literal for the '<em><b>Menu</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference COMMAND__MENU = eINSTANCE.getCommand_Menu();
 
     /**
      * The meta object literal for the '<em><b>Category</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference COMMAND__CATEGORY = eINSTANCE.getCommand_Category();
 
     /**
      * The meta object literal for the '<em><b>Binding</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference COMMAND__BINDING = eINSTANCE.getCommand_Binding();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.HandlerImpl <em>Handler</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.HandlerImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getHandler()
      * @generated
      */
     EClass HANDLER = eINSTANCE.getHandler();
 
     /**
      * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute HANDLER__NAME = eINSTANCE.getHandler_Name();
 
     /**
      * The meta object literal for the '<em><b>Mnemonic</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute HANDLER__MNEMONIC = eINSTANCE.getHandler_Mnemonic();
 
     /**
      * The meta object literal for the '<em><b>Command</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference HANDLER__COMMAND = eINSTANCE.getHandler_Command();
 
     /**
      * The meta object literal for the '<em><b>Contexts</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference HANDLER__CONTEXTS = eINSTANCE.getHandler_Contexts();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.HelpContentsImpl <em>Help Contents</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.HelpContentsImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getHelpContents()
      * @generated
      */
     EClass HELP_CONTENTS = eINSTANCE.getHelpContents();
 
     /**
      * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute HELP_CONTENTS__LABEL = eINSTANCE.getHelpContents_Label();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute HELP_CONTENTS__DESCRIPTION = eINSTANCE.getHelpContents_Description();
 
     /**
      * The meta object literal for the '<em><b>Help Pages</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference HELP_CONTENTS__HELP_PAGES = eINSTANCE.getHelpContents_HelpPages();
 
     /**
      * The meta object literal for the '<em><b>External Help Pages</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference HELP_CONTENTS__EXTERNAL_HELP_PAGES = eINSTANCE.getHelpContents_ExternalHelpPages();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.HelpPageImpl <em>Help Page</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.HelpPageImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getHelpPage()
      * @generated
      */
     EClass HELP_PAGE = eINSTANCE.getHelpPage();
 
     /**
      * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute HELP_PAGE__LABEL = eINSTANCE.getHelpPage_Label();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute HELP_PAGE__DESCRIPTION = eINSTANCE.getHelpPage_Description();
 
     /**
      * The meta object literal for the '<em><b>Help Pages</b></em>' containment reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference HELP_PAGE__HELP_PAGES = eINSTANCE.getHelpPage_HelpPages();
 
     /**
      * The meta object literal for the '<em><b>External Help Pages</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference HELP_PAGE__EXTERNAL_HELP_PAGES = eINSTANCE.getHelpPage_ExternalHelpPages();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.DynamicHelpImpl <em>Dynamic Help</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.DynamicHelpImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getDynamicHelp()
      * @generated
      */
     EClass DYNAMIC_HELP = eINSTANCE.getDynamicHelp();
 
     /**
      * The meta object literal for the '<em><b>Label</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DYNAMIC_HELP__LABEL = eINSTANCE.getDynamicHelp_Label();
 
     /**
      * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute DYNAMIC_HELP__DESCRIPTION = eINSTANCE.getDynamicHelp_Description();
 
     /**
      * The meta object literal for the '<em><b>Help Pages</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference DYNAMIC_HELP__HELP_PAGES = eINSTANCE.getDynamicHelp_HelpPages();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.impl.BindingImpl <em>Binding</em>}' class.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.impl.BindingImpl
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getBinding()
      * @generated
      */
     EClass BINDING = eINSTANCE.getBinding();
 
     /**
      * The meta object literal for the '<em><b>Key Sequence</b></em>' attribute feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EAttribute BINDING__KEY_SEQUENCE = eINSTANCE.getBinding_KeySequence();
 
     /**
      * The meta object literal for the '<em><b>Command</b></em>' reference feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BINDING__COMMAND = eINSTANCE.getBinding_Command();
 
     /**
      * The meta object literal for the '<em><b>Contexts</b></em>' reference list feature.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @generated
      */
     EReference BINDING__CONTEXTS = eINSTANCE.getBinding_Contexts();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.Type <em>Type</em>}' enum.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.Type
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getType()
      * @generated
      */
     EEnum TYPE = eINSTANCE.getType();
 
     /**
      * The meta object literal for the '{@link org.obeonetwork.dsl.gen.eclipse.Location <em>Location</em>}' enum.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see org.obeonetwork.dsl.gen.eclipse.Location
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getLocation()
      * @generated
      */
     EEnum LOCATION = eINSTANCE.getLocation();
 
     /**
      * The meta object literal for the '<em>Version</em>' data type.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see java.lang.String
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getVersion()
      * @generated
      */
     EDataType VERSION = eINSTANCE.getVersion();
 
     /**
      * The meta object literal for the '<em>Namespace</em>' data type.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see java.lang.String
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getNamespace()
      * @generated
      */
     EDataType NAMESPACE = eINSTANCE.getNamespace();
 
     /**
      * The meta object literal for the '<em>Java Name</em>' data type.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see java.lang.String
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getJavaName()
      * @generated
      */
     EDataType JAVA_NAME = eINSTANCE.getJavaName();
 
     /**
      * The meta object literal for the '<em>Name</em>' data type.
      * <!-- begin-user-doc -->
      * <!-- end-user-doc -->
      * @see java.lang.String
      * @see org.obeonetwork.dsl.gen.eclipse.impl.EclipsePackageImpl#getName_()
      * @generated
      */
     EDataType NAME = eINSTANCE.getName_();
 
   }
 
 } //EclipsePackage
