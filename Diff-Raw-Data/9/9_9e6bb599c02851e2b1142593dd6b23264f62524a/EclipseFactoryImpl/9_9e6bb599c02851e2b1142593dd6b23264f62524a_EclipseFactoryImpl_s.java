 /**
  */
 package org.obeonetwork.dsl.gen.eclipse.impl;
 
 import com.google.common.base.Objects;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 
 import org.eclipse.emf.ecore.impl.EFactoryImpl;
 
 import org.eclipse.emf.ecore.plugin.EcorePlugin;
 
 import org.obeonetwork.dsl.gen.eclipse.*;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model <b>Factory</b>.
  * <!-- end-user-doc -->
  * @generated
  */
 public class EclipseFactoryImpl extends EFactoryImpl implements EclipseFactory
 {
   /**
    * Creates the default factory implementation.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public static EclipseFactory init()
   {
     try
     {
       EclipseFactory theEclipseFactory = (EclipseFactory)EPackage.Registry.INSTANCE.getEFactory("http://www.obeonetwork.org/dsl/eclipse"); 
       if (theEclipseFactory != null)
       {
         return theEclipseFactory;
       }
     }
     catch (Exception exception)
     {
       EcorePlugin.INSTANCE.log(exception);
     }
     return new EclipseFactoryImpl();
   }
 
   /**
    * Creates an instance of the factory.
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public EclipseFactoryImpl()
   {
     super();
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public EObject create(EClass eClass)
   {
     switch (eClass.getClassifierID())
     {
       case EclipsePackage.APPLICATION: return createApplication();
       case EclipsePackage.REPOSITORY: return createRepository();
       case EclipsePackage.REPOSITORY_CATEGORY: return createRepositoryCategory();
       case EclipsePackage.FEATURE: return createFeature();
       case EclipsePackage.BUNDLE: return createBundle();
       case EclipsePackage.DECLARATIVE_SERVICE: return createDeclarativeService();
       case EclipsePackage.PROVIDED_SERVICE: return createProvidedService();
       case EclipsePackage.REQUIRED_SERVICE: return createRequiredService();
       case EclipsePackage.IMPORTED_PACKAGE_DECLARATION: return createImportedPackageDeclaration();
       case EclipsePackage.BUILDER: return createBuilder();
       case EclipsePackage.NATURE: return createNature();
       case EclipsePackage.WIZARD: return createWizard();
       case EclipsePackage.PROJECT_WIZARD: return createProjectWizard();
       case EclipsePackage.EXTENSION_POINT: return createExtensionPoint();
       case EclipsePackage.ATTRIBUTE: return createAttribute();
       case EclipsePackage.DECORATOR: return createDecorator();
       case EclipsePackage.MARKER: return createMarker();
       case EclipsePackage.CONTEXT: return createContext();
       case EclipsePackage.PERSPECTIVE: return createPerspective();
       case EclipsePackage.CATEGORY: return createCategory();
       case EclipsePackage.VIEW: return createView();
       case EclipsePackage.EDITOR: return createEditor();
       case EclipsePackage.MENU: return createMenu();
       case EclipsePackage.COMMAND: return createCommand();
       case EclipsePackage.HANDLER: return createHandler();
       case EclipsePackage.HELP_CONTENTS: return createHelpContents();
       case EclipsePackage.HELP_PAGE: return createHelpPage();
       case EclipsePackage.DYNAMIC_HELP: return createDynamicHelp();
       case EclipsePackage.BINDING: return createBinding();
       default:
         throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
     }
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public Object createFromString(EDataType eDataType, String initialValue)
   {
     switch (eDataType.getClassifierID())
     {
       case EclipsePackage.TYPE:
         return createTypeFromString(eDataType, initialValue);
       case EclipsePackage.LOCATION:
         return createLocationFromString(eDataType, initialValue);
       case EclipsePackage.VERSION:
         return createVersionFromString(eDataType, initialValue);
       case EclipsePackage.NAMESPACE:
         return createNamespaceFromString(eDataType, initialValue);
       case EclipsePackage.JAVA_NAME:
         return createJavaNameFromString(eDataType, initialValue);
       case EclipsePackage.NAME:
         return createNameFromString(eDataType, initialValue);
       default:
         throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
     }
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   @Override
   public String convertToString(EDataType eDataType, Object instanceValue)
   {
     switch (eDataType.getClassifierID())
     {
       case EclipsePackage.TYPE:
         return convertTypeToString(eDataType, instanceValue);
       case EclipsePackage.LOCATION:
         return convertLocationToString(eDataType, instanceValue);
       case EclipsePackage.VERSION:
         return convertVersionToString(eDataType, instanceValue);
       case EclipsePackage.NAMESPACE:
         return convertNamespaceToString(eDataType, instanceValue);
       case EclipsePackage.JAVA_NAME:
         return convertJavaNameToString(eDataType, instanceValue);
       case EclipsePackage.NAME:
         return convertNameToString(eDataType, instanceValue);
       default:
         throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
     }
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Application createApplication()
   {
     ApplicationImpl application = new ApplicationImpl();
     return application;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Repository createRepository()
   {
     RepositoryImpl repository = new RepositoryImpl();
     return repository;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public RepositoryCategory createRepositoryCategory()
   {
     RepositoryCategoryImpl repositoryCategory = new RepositoryCategoryImpl();
     return repositoryCategory;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Feature createFeature()
   {
     FeatureImpl feature = new FeatureImpl();
     return feature;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Bundle createBundle()
   {
     BundleImpl bundle = new BundleImpl();
     return bundle;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public DeclarativeService createDeclarativeService()
   {
     DeclarativeServiceImpl declarativeService = new DeclarativeServiceImpl();
     return declarativeService;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public ProvidedService createProvidedService()
   {
     ProvidedServiceImpl providedService = new ProvidedServiceImpl();
     return providedService;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public RequiredService createRequiredService()
   {
     RequiredServiceImpl requiredService = new RequiredServiceImpl();
     return requiredService;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public ImportedPackageDeclaration createImportedPackageDeclaration()
   {
     ImportedPackageDeclarationImpl importedPackageDeclaration = new ImportedPackageDeclarationImpl();
     return importedPackageDeclaration;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Builder createBuilder()
   {
     BuilderImpl builder = new BuilderImpl();
     return builder;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Nature createNature()
   {
     NatureImpl nature = new NatureImpl();
     return nature;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Wizard createWizard()
   {
     WizardImpl wizard = new WizardImpl();
     return wizard;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public ProjectWizard createProjectWizard()
   {
     ProjectWizardImpl projectWizard = new ProjectWizardImpl();
     return projectWizard;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public ExtensionPoint createExtensionPoint()
   {
     ExtensionPointImpl extensionPoint = new ExtensionPointImpl();
     return extensionPoint;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Attribute createAttribute()
   {
     AttributeImpl attribute = new AttributeImpl();
     return attribute;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Decorator createDecorator()
   {
     DecoratorImpl decorator = new DecoratorImpl();
     return decorator;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Marker createMarker()
   {
     MarkerImpl marker = new MarkerImpl();
     return marker;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Context createContext()
   {
     ContextImpl context = new ContextImpl();
     return context;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Perspective createPerspective()
   {
     PerspectiveImpl perspective = new PerspectiveImpl();
     return perspective;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Category createCategory()
   {
     CategoryImpl category = new CategoryImpl();
     return category;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public View createView()
   {
     ViewImpl view = new ViewImpl();
     return view;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Editor createEditor()
   {
     EditorImpl editor = new EditorImpl();
     return editor;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Menu createMenu()
   {
     MenuImpl menu = new MenuImpl();
     return menu;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Command createCommand()
   {
     CommandImpl command = new CommandImpl();
     return command;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Handler createHandler()
   {
     HandlerImpl handler = new HandlerImpl();
     return handler;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public HelpContents createHelpContents()
   {
     HelpContentsImpl helpContents = new HelpContentsImpl();
     return helpContents;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public HelpPage createHelpPage()
   {
     HelpPageImpl helpPage = new HelpPageImpl();
     return helpPage;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public DynamicHelp createDynamicHelp()
   {
     DynamicHelpImpl dynamicHelp = new DynamicHelpImpl();
     return dynamicHelp;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Binding createBinding()
   {
     BindingImpl binding = new BindingImpl();
     return binding;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Type createTypeFromString(EDataType eDataType, String initialValue)
   {
     Type result = Type.get(initialValue);
     if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String convertTypeToString(EDataType eDataType, Object instanceValue)
   {
     return instanceValue == null ? null : instanceValue.toString();
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public Location createLocationFromString(EDataType eDataType, String initialValue)
   {
     Location result = Location.get(initialValue);
     if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
     return result;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String convertLocationToString(EDataType eDataType, Object instanceValue)
   {
     return instanceValue == null ? null : instanceValue.toString();
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createVersion(String it)
   {
     boolean _and = false;
     boolean _notEquals = (!Objects.equal(it, null));
     if (!_notEquals)
     {
       _and = false;
     } else
     {
       boolean _matches = it.matches("\\d+\\.\\d+\\.\\d+");
       boolean _not = (!_matches);
       _and = (_notEquals && _not);
     }
     if (_and)
     {
       RuntimeException _runtimeException = new RuntimeException("Bad format for version");
       throw _runtimeException;
     }
     return it;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createVersionFromString(EDataType eDataType, String initialValue)
   {
     return createVersion(initialValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String convertVersionToString(EDataType eDataType, Object instanceValue)
   {
     return super.convertToString(eDataType, instanceValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createNamespace(String it)
   {
     boolean _and = false;
     boolean _notEquals = (!Objects.equal(it, null));
     if (!_notEquals)
     {
       _and = false;
     } else
     {
       boolean _matches = it.matches("([a-z]+([0-9]*[A-Z]*[a-z]*)*\\.)*[a-z]+([0-9]*[A-Z]*[a-z]*)*");
       boolean _not = (!_matches);
       _and = (_notEquals && _not);
     }
     if (_and)
     {
       RuntimeException _runtimeException = new RuntimeException("Bad format for ID");
       throw _runtimeException;
     }
     return it;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createNamespaceFromString(EDataType eDataType, String initialValue)
   {
     return createNamespace(initialValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String convertNamespaceToString(EDataType eDataType, Object instanceValue)
   {
     return super.convertToString(eDataType, instanceValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createJavaName(String it)
   {
     boolean _and = false;
     boolean _notEquals = (!Objects.equal(it, null));
     if (!_notEquals)
     {
       _and = false;
     } else
     {
       boolean _matches = it.matches("[A-Z]+([0-9]*[A-Z]*[a-z]*)*");
       boolean _not = (!_matches);
       _and = (_notEquals && _not);
     }
     if (_and)
     {
      RuntimeException _runtimeException = new RuntimeException("Bad format for name");
       throw _runtimeException;
     }
     return it;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createJavaNameFromString(EDataType eDataType, String initialValue)
   {
     return createJavaName(initialValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String convertJavaNameToString(EDataType eDataType, Object instanceValue)
   {
     return super.convertToString(eDataType, instanceValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createName(String it)
   {
     boolean _and = false;
     boolean _notEquals = (!Objects.equal(it, null));
     if (!_notEquals)
     {
       _and = false;
     } else
     {
       boolean _matches = it.matches("([A-Z]*[a-z]+)+([ ]*[0-9]*[A-Z]*[a-z]*)*");
       boolean _not = (!_matches);
       _and = (_notEquals && _not);
     }
     if (_and)
     {
       RuntimeException _runtimeException = new RuntimeException("Bad format for name");
       throw _runtimeException;
     }
     return it;
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String createNameFromString(EDataType eDataType, String initialValue)
   {
     return createName(initialValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public String convertNameToString(EDataType eDataType, Object instanceValue)
   {
     return super.convertToString(eDataType, instanceValue);
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @generated
    */
   public EclipsePackage getEclipsePackage()
   {
     return (EclipsePackage)getEPackage();
   }
 
   /**
    * <!-- begin-user-doc -->
    * <!-- end-user-doc -->
    * @deprecated
    * @generated
    */
   @Deprecated
   public static EclipsePackage getPackage()
   {
     return EclipsePackage.eINSTANCE;
   }
 
 } //EclipseFactoryImpl
