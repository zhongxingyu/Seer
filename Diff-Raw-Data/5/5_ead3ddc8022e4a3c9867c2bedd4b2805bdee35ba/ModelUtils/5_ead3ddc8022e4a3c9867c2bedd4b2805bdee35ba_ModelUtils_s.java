 package org.eclipse.emf.texo.utils;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Proxy;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.Enumerator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EAnnotation;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EDataType;
 import org.eclipse.emf.ecore.EEnum;
 import org.eclipse.emf.ecore.EFactory;
 import org.eclipse.emf.ecore.EModelElement;
 import org.eclipse.emf.ecore.ENamedElement;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EcorePackage;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.ExtendedMetaData;
 import org.eclipse.emf.ecore.util.FeatureMapUtil;
 import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
 import org.eclipse.emf.ecore.xml.type.XMLTypePackage;
 import org.eclipse.emf.texo.component.ComponentProvider;
 import org.eclipse.emf.texo.model.ModelConstants;
 import org.eclipse.emf.texo.model.ModelEFactory;
 import org.eclipse.emf.texo.model.ModelFeatureMapEntry;
 import org.eclipse.emf.texo.model.ModelPackage;
 import org.eclipse.emf.texo.model.ModelResolver;
 import org.eclipse.emf.texo.provider.IdProvider;
 import org.eclipse.emf.texo.store.EObjectStore;
 
 /**
  * Utility methods which are used at runtime.
  * 
  * @author <a href="mtaal@elver.org">Martin Taal</a>
  */
 public class ModelUtils {
   public static final String QUALIFIERSEPARATOR = "|"; //$NON-NLS-1$
 
   private static SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.S'Z'"); //$NON-NLS-1$
 
   private static final String TEMP_URI_CODE = "tempUriNum"; //$NON-NLS-1$
   private static long TEMP_COUNTER = 0;
 
   /**
    * Get the documentation from the {@link ENamedElement}.
    */
   public static String getDocumentation(ENamedElement eNamedElement) {
     final EAnnotation eAnnotation = eNamedElement.getEAnnotation("http://www.eclipse.org/emf/2002/GenModel"); //$NON-NLS-1$
     if (eAnnotation == null) {
       return null;
     }
     if (eAnnotation.getDetails().get("documentation") != null) { //$NON-NLS-1$
       return eAnnotation.getDetails().get("documentation").trim(); //$NON-NLS-1$
     }
     return null;
   }
 
   /**
    * Replaces the standard {@link EFactory} for an {@link EPackage} with the special {@link ModelEFactory} which handles
    * creation of dynamic eobjects in a better way.
    * 
    * @param modelPackage
    * @param ePackage
    */
   public static void setEFactoryProxy(ModelPackage modelPackage, EPackage ePackage) {
 
     // use a java proxy to ensure that the correct class is used in case of
     // generated code
     final ModelEFactory modelEFactory = ComponentProvider.getInstance().newInstance(ModelEFactory.class);
     modelEFactory.setEPackage(ePackage);
     modelEFactory.setModelFactory(modelPackage.getModelFactory());
 
     final Class<?> factoryClass = ePackage.getEFactoryInstance().getClass();
     final ModelEFactory.EFactoryInvocationHandler handler = new ModelEFactory.EFactoryInvocationHandler(modelEFactory);
 
     final Class<?>[] interfaces = new Class<?>[factoryClass.getInterfaces().length + 1];
     int i = 0;
     for (Class<?> clz : factoryClass.getInterfaces()) {
       interfaces[i] = clz;
       i++;
     }
     interfaces[i] = InternalEObject.class;
 
     final EFactory eFactory = (EFactory) Proxy.newProxyInstance(factoryClass.getClassLoader(), interfaces, handler);
     ePackage.setEFactoryInstance(eFactory);
 
   }
 
   /**
    * Override the default xml dateformat with your own settings.
    */
   public static void setXMLDateFormat(SimpleDateFormat simpleDateFormat) {
     xmlDateFormat = simpleDateFormat;
   }
 
   public static SimpleDateFormat getXMLDateFormat() {
     return xmlDateFormat;
   }
 
   /**
    * Return true if the {@link EStructuralFeature} is unsettable, non required and has no default.
    */
   public static boolean isUnsettable(EStructuralFeature eFeature) {
     return !eFeature.isMany() && eFeature.isUnsettable() && eFeature.getDefaultValueLiteral() == null
         && eFeature.getLowerBound() == 0;
   }
 
   /**
    * Returns true if the {@link EStructuralFeature} models an xsd:any.
    * 
    * @see AnyFeatureMapEntry
    */
   public static boolean isAnyType(EStructuralFeature eFeature) {
     final EAnnotation eAnnotation = eFeature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
     if (eAnnotation == null) {
       return false;
     }
     final String kind = eAnnotation.getDetails().get("kind"); //$NON-NLS-1$
     return kind != null && (kind.equals("elementWildcard") || kind.equals("attributeWildcard")); //$NON-NLS-1$//$NON-NLS-2$
   }
 
   /**
    * If the value is a featuregroup then walk through the structure to find the deepest one and return that value.
    */
   public static Object findValue(ModelFeatureMapEntry<?> modelFeatureMap) {
     if (FeatureMapUtil.isFeatureMap(modelFeatureMap.getEStructuralFeature())) {
       final ModelFeatureMapEntry<?> modelFeatureMapEntry = ModelResolver.getInstance().getModelFeatureMapEntry(
           modelFeatureMap.getEStructuralFeature(), modelFeatureMap.getValue());
 
       return findValue(modelFeatureMapEntry);
     }
     return modelFeatureMap.getValue();
   }
 
   /**
    * if the value is a featuregroup then walk through the structure to find the deepest one and return that value.
    * 
    * In EMF nested featuremaps are stored in a flat list, in Texo the hierarchical structure is maintained this method
    * helps in the conversion.
    */
   public static EStructuralFeature findFeature(ModelFeatureMapEntry<?> modelFeatureMap) {
     if (FeatureMapUtil.isFeatureMap(modelFeatureMap.getEStructuralFeature())) {
       final ModelFeatureMapEntry<?> modelFeatureMapEntry = ModelResolver.getInstance().getModelFeatureMapEntry(
           modelFeatureMap.getEStructuralFeature(), modelFeatureMap.getValue());
 
       return findFeature(modelFeatureMapEntry);
     }
     return modelFeatureMap.getEStructuralFeature();
   }
 
   /**
    * Returns a qualified string representation of the {@link EStructuralFeature} using
    * {@link #getQualifiedNameFromEClass(EClass)}.
    */
   public static String getQualifiedNameFromEStructuralFeature(EStructuralFeature eFeature) {
     return getQualifiedNameFromEClass(eFeature.getEContainingClass()) + QUALIFIERSEPARATOR + eFeature.getName();
   }
 
   /**
    * Returns the {@link EStructuralFeature} of a certain {@link EClass} also encoded in the name.
    * 
    * @param name
    * @see #getEClassFromQualifiedName(String)
    */
   public static EStructuralFeature getEStructuralFeatureFromQualifiedName(String name) {
     final int index = name.lastIndexOf(QUALIFIERSEPARATOR);
     final String eClassString = name.substring(0, index);
     final EClass eClass = getEClassFromQualifiedName(eClassString);
     final String featureName = name.substring(1 + index);
     return eClass.getEStructuralFeature(featureName);
   }
 
   /**
    * Returns a qualified string representation of the class using the ns prefix of the epackage.
    */
   public static String getQualifiedNameFromEClass(EClass eClass) {
     return eClass.getEPackage().getNsPrefix() + QUALIFIERSEPARATOR + eClass.getName();
   }
 
   /**
    * Searches all the eclassifiers of the {@link ModelPackage} instances that have been registered with the
    * {@link ModelResolver}.
    * 
    * Note, can handle qualified as well as unqualified names. The qualified names will have the epackage ns prefix
    * prepended separated by the {@link #QUALIFIERSEPARATOR}, for example: library|Book
    * 
    * But also unqualified names are handled, although then if different epackages have eclasses with the same name then
    * the wrong eclass can be returned.
    * 
    * @param name
    *          the (qualified) name of the EClass
    * @return an EClass
    * @throws IllegalArgumentException
    *           if not EClass was found
    */
 
   /**
    * Searches all the eclassifiers of the {@link ModelPackage} instances that have been registered with the
    * {@link ModelResolver}.
    * 
    * Note, can handle qualified as well as unqualified names. The qualified names will have the epackage ns prefix
    * prepended separated by the {@link #QUALIFIERSEPARATOR}, for example: library|Book
    * 
    * But also unqualified names are handled, although then if different epackages have eclasses with the same name then
    * the wrong eclass can be returned.
    * 
    * @param name
    *          the (qualified) name of the EClass
    * @return an EClass
    * @throws IllegalArgumentException
    *           if no EClass was found
    */
   public static EClass getEClassFromQualifiedName(String name) {
     return (EClass) getEClassifierFromQualifiedName(name);
   }
 
   /**
    * @see #getEClassFromQualifiedName(String)
    */
   public static EClassifier getEClassifierFromQualifiedName(String name) {
     String nameSpacePrefix = null;
     String eClassName = name;
     if (eClassName.contains(QUALIFIERSEPARATOR)) {
       final int index = eClassName.indexOf(QUALIFIERSEPARATOR);
       nameSpacePrefix = eClassName.substring(0, index);
       eClassName = eClassName.substring(index + 1);
     }
     for (ModelPackage modelPackage : ModelResolver.getInstance().getModelPackages()) {
       final EPackage ePackage = modelPackage.getEPackage();
       if (nameSpacePrefix != null && !ePackage.getNsPrefix().equals(nameSpacePrefix)) {
         continue;
       }
       for (EClassifier eClassifier : ePackage.getEClassifiers()) {
         if (eClassifier.getName().equals(eClassName)) {
           return eClassifier;
         }
       }
     }
     for (Object key : new HashSet<Object>(ModelResolver.getInstance().getEPackageRegistry().keySet())) {
       final EPackage ePackage = ModelResolver.getInstance().getEPackageRegistry().getEPackage((String) key);
       if (nameSpacePrefix != null && !ePackage.getNsPrefix().equals(nameSpacePrefix)) {
         continue;
       }
       for (EClassifier eClassifier : ePackage.getEClassifiers()) {
         if (eClassifier.getName().equals(eClassName)) {
           return eClassifier;
         }
       }
     }
     if (nameSpacePrefix != null) {
       EPackage otherEPackage = null;
       if (XMLTypePackage.eNS_PREFIX.equals(nameSpacePrefix)) {
         otherEPackage = XMLTypePackage.eINSTANCE;
       } else if (EcorePackage.eNS_PREFIX.equals(nameSpacePrefix)) {
         otherEPackage = EcorePackage.eINSTANCE;
       }
       if (otherEPackage != null) {
         for (EClassifier eClassifier : otherEPackage.getEClassifiers()) {
           if (eClassifier.getName().equals(eClassName)) {
             return eClassifier;
           }
         }
       }
     }
 
     throw new IllegalArgumentException("No EClass(ifier) found using name " + name); //$NON-NLS-1$
   }
 
   /**
    * Searches for an {@link EPackage} available in the {@link ModelResolver} or the {@link XMLTypePackage} or the
    * {@link EcorePackage}. It first tries to match the namespace uri, then the name and finally the namespace prefix.
    * 
    * @param identifier
    *          , the uri, name or namespace prefix of the {@link EPackage} to search for
    * @return a found {@link EPackage}
    * @throws IllegalArgumentException
    *           if no {@link EPackage} can be found
    */
   public static EPackage getEPackageFromNameUriOrPrefix(String identifier) {
     final List<EPackage> toSearch = new ArrayList<EPackage>();
     toSearch.add(XMLTypePackage.eINSTANCE);
     toSearch.add(EcorePackage.eINSTANCE);
     for (ModelPackage modelPackage : ModelResolver.getInstance().getModelPackages()) {
       final EPackage ePackage = modelPackage.getEPackage();
       toSearch.add(ePackage);
     }
     for (EPackage ePackage : toSearch) {
       if (identifier.equals(ePackage.getNsURI())) {
         return ePackage;
       }
     }
     for (EPackage ePackage : toSearch) {
       if (identifier.equals(ePackage.getName())) {
         return ePackage;
       }
     }
     for (EPackage ePackage : toSearch) {
       if (identifier.equals(ePackage.getNsPrefix())) {
         return ePackage;
       }
     }
 
     throw new IllegalArgumentException("No EPackage found using identifier " + identifier); //$NON-NLS-1$
   }
 
   /** Returns the lower case version of the string converted with English Locale **/
   public static String toLowerCase(String value) {
     return value == null ? null : value.toLowerCase(Locale.ENGLISH);
   }
 
   /** Returns the upper case version of the string converted with English Locale **/
   public static String toUpperCase(String value) {
     return value == null ? null : value.toUpperCase(Locale.ENGLISH);
   }
 
   /** Returns the first char only lower case version of the string converted with English Locale **/
   public static String lowerCaseFirst(String value) {
     if (value == null) {
       return null;
     }
 
     if (value.length() == 1) {
       return toLowerCase(value);
     }
 
     return value.substring(0, 1).toLowerCase(Locale.ENGLISH) + value.substring(1);
   }
 
   /** Returns the first char only upper case version of the string converted with English Locale **/
   public static String upCaseFirst(String value) {
     if (value == null) {
       return null;
     }
 
     if (value.length() == 1) {
       return toUpperCase(value);
     }
 
     return value.substring(0, 1).toUpperCase(Locale.ENGLISH) + value.substring(1);
   }
 
   /**
    * Converts a XML String date(time) to a Date object. Note: method is synchronized as it uses a shared
    * {@link SimpleDateFormat} instance.
    * 
    * @param xmlString
    *          the xml String containing the date time in XML Schema format.
    * @return a java Date object representing the date time coded in the xmlString
    */
   public static synchronized Date createFromXML(final String xmlString) {
     try {
       return xmlDateFormat.parse(xmlString);
     } catch (final ParseException e) {
       throw new IllegalArgumentException(e);
     }
   }
 
   /**
    * Converts a Date to a valid XML String. Note: method is synchronized as it uses a shared {@link SimpleDateFormat}
    * instance.
    * 
    * @param dt
    *          the Date to format
    * @return the String format of a {@link Date}, the standard xml format is used: yyyy-MM-dd'T'HH:mm:ss.S'Z'
    */
   public static synchronized String convertToXML(final Date dt) {
     return xmlDateFormat.format(dt);
   }
 
   /**
    * Read the ecore file belonging to the {@link ModelPackage} ({@see ModelPackage#getEcoreFileName()}. The default
    * impl. will read the ecore file from the classpath of the passed ModelPackage . All read EPackages are registered in
    * the central {@link EPackage.Registry} from where they can be retrieved.
    * 
    * @param modelPackage
    *          the modelPackage for which the EPackage is read
    */
   public static void readEPackagesFromFile(final ModelPackage modelPackage) {
 
     // if already present in the package registry then don't read anymore from file.
     if (ModelResolver.getInstance().getEPackageRegistry().getEPackage(modelPackage.getNsURI()) != null) {
       return;
     }
 
     final String ecoreFileName = modelPackage.getEcoreFileName();
     Check.isNotEmpty(ecoreFileName, "Ecore file of ModelPackage may not be empty."); //$NON-NLS-1$
     if (ecoreFileName != null && ecoreFileName.length() > 0) {
       try {
         final ResourceSet rs = new ResourceSetImpl();
         rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", //$NON-NLS-1$
             new EcoreResourceFactoryImpl());
         //    rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xcore", //$NON-NLS-1$
         // new XcoreResourceFactory());
         rs.setPackageRegistry(ModelResolver.getInstance().getEPackageRegistry());
 
         // note the resource uri is the same as by which it is saved in the
         // GenEPackage.getECoreFileContent
         final Resource res = new EcoreResourceFactoryImpl().createResource(URI.createURI(modelPackage.getNsURI()));
         rs.getResources().add(res);
         final InputStream is = modelPackage.getClass().getResourceAsStream(ecoreFileName);
         if (is == null) {
           throw new RuntimeException("File " + ecoreFileName + " not found within class path of " //$NON-NLS-1$//$NON-NLS-2$
               + modelPackage.getClass().getName());
         }
         res.load(is, Collections.EMPTY_MAP);
         is.close();
 
         // NOTE beware: for reference the below does not work if local references need to
         // resolved, probably because the resource is created with the wrong uri
         // final InputStream is = ecoreModelPackage.getClass().getResourceAsStream(ecoreFileName);
         // final ResourceSet rs = new ResourceSetImpl();
         // rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new
         // EcoreResourceFactoryImpl());
         // final Resource res = rs.createResource(URI.createURI(ecoreFileName));
         // res.load(is, Collections.EMPTY_MAP);
         // is.close();
 
         final Iterator<?> it = res.getAllContents();
         while (it.hasNext()) {
           final Object obj = it.next();
           if (obj instanceof EPackage) {
             final EPackage epack = (EPackage) obj;
             registerEPackage(epack, ModelResolver.getInstance().getEPackageRegistry());
           }
         }
       } catch (final IOException e) {
         throw new IllegalStateException("Exception while loading models from ecorefile " //$NON-NLS-1$
             + ecoreFileName);
       }
     }
   }
 
   /**
    * Recursively registers an EPackage and its subpackages. If an ePackage was already registered then that one is
    * returned.
    * 
    * @param the
    *          ePackage to register
    * @return the ePackage found in the registry or the passed ePackage
    */
   public static EPackage registerEPackage(final EPackage ePackage, EPackage.Registry registry) {
 
     if (registry.containsKey(ePackage.getNsURI())) {
 
       // log.warn("EPackage with this \"" + ePackage.getNsURI()
       // + "\" already registered. Returning the registered one if it is an epackage");
       final Object packageObject = registry.get(ePackage.getNsURI());
       if (packageObject instanceof EPackage) {
         return (EPackage) packageObject;
       }
     }
 
     registry.put(ePackage.getNsURI(), ePackage);
     for (final EPackage eSubPackage : new ArrayList<EPackage>(ePackage.getESubpackages())) {
       final EPackage registeredSubPackage = registerEPackage(eSubPackage, registry);
       if (registeredSubPackage != eSubPackage) {
         final int currentIndex = ePackage.getESubpackages().indexOf(eSubPackage);
         assert eSubPackage == ePackage.getESubpackages().set(currentIndex, registeredSubPackage);
       }
     }
     return ePackage;
   }
 
   /**
    * See the {@link #isObjectTypeWithEnumBaseType(EDataType)} for information.
    * 
    * @param eDataType
    * @return
    */
   public static EEnum getEnumBaseDataTypeIfObject(EDataType eDataType) {
     if (eDataType instanceof EEnum) {
       return null;
     }
     final ExtendedMetaData extendedMetaData = ExtendedMetaData.INSTANCE;
     for (EDataType baseDataType = eDataType; baseDataType != null; baseDataType = extendedMetaData
         .getBaseType(baseDataType)) {
       if (baseDataType instanceof EEnum) {
         return (EEnum) baseDataType;
       }
     }
     return null;
   }
 
   /**
    * @param nsuri
    *          the namespace uri to search for the epackage
    * @return the EPackage on the basis of the nsuri
    */
   public static EPackage getEPackage(final String nsuri) {
     final EPackage epackage = ModelResolver.getInstance().getEPackageRegistry().getEPackage(nsuri);
     if (epackage == null) {
       throw new IllegalArgumentException("No EPackage registered using the nsuri: " + nsuri); //$NON-NLS-1$
     }
     return epackage;
   }
 
   /**
    * True if the eDataType is an EEnum or has as instance class the {@link Enumerator}.
    */
   public static boolean isEEnum(EDataType eDataType) {
     // special case happens when (de-)serializing ecore models
     if (eDataType == EcorePackage.eINSTANCE.getEEnumerator()) {
       return false;
     }
     return eDataType instanceof EEnum || Enumerator.class.getName().equals(eDataType.getInstanceClassName());
   }
 
   /** @return true if the eClass represents a Map.Entry */
   // Note method is copied in DataGeneratorUtils
   public static boolean isEMap(final EClass eClass) {
     return eClass != null && eClass.getInstanceClass() != null
         && Map.Entry.class.isAssignableFrom(eClass.getInstanceClass()) && eClass.getEStructuralFeature("key") != null //$NON-NLS-1$
         && eClass.getEStructuralFeature("value") != null; //$NON-NLS-1$
   }
 
   /** @return true if the eFeature refers to an EMap EClass */
   public static boolean isEMap(final EStructuralFeature eFeature) {
     if (!(eFeature.getEType() instanceof EClass)) {
       return false;
     }
     return isEMap((EClass) eFeature.getEType());
   }
 
   /** Returns true if the passed feature is a wildcard mixed feature */
   public static boolean isMixed(EStructuralFeature feature) {
     EAnnotation eAnnotation = feature.getEAnnotation(ExtendedMetaData.ANNOTATION_URI);
     if (eAnnotation == null) {
       return false;
     }
     final String kind = eAnnotation.getDetails().get("kind"); //$NON-NLS-1$
     final String name = eAnnotation.getDetails().get("name"); //$NON-NLS-1$
     return kind != null && kind.compareTo("elementWildcard") == 0 && name != null //$NON-NLS-1$
         && name.compareTo(":mixed") == 0; //$NON-NLS-1$
   }
 
   /** Returns true if the eclass has a mixed efeature */
   public static boolean hasMixedEFeature(EClass eClass) {
     for (EStructuralFeature eFeature : eClass.getEAllStructuralFeatures()) {
       if (isMixed(eFeature)) {
         return true;
       }
     }
     return false;
   }
 
   /**
    * Returns true if the {@link EModelElement} has an {@link EAnnotation} with source
    * {@link ModelConstants#EANNOTATION_SOURCE} and the passed in annotionKey.
    * 
    * @param eModelElement
    *          the model element for which EAnnotations are checked
    * @param annotationKey
    *          the key to search for in the EAnnotations
    * @return true if the annotation is present
    * @see #getEAnnotation(EModelElement, String)
    */
   public static boolean hasEAnnotation(EModelElement eModelElement, String annotationKey) {
     return null != getEAnnotation(eModelElement, annotationKey);
   }
 
   /**
    * Returns the value of the EAnnotation of the {@link EModelElement} with source
    * {@link ModelConstants#EANNOTATION_SOURCE} and as the key the annotionKey.
    * 
    * @param eModelElement
    *          the model element for which the EAnnotations are checked
    * @param annotationKey
    *          the key to search for in the EAnnotations
    * @return the value for the annotation key, or null if not present
    */
   public static String getEAnnotation(EModelElement eModelElement, String annotationKey) {
     final EAnnotation eAnnotation = eModelElement.getEAnnotation(ModelConstants.EANNOTATION_SOURCE);
     if (eAnnotation == null) {
       return null;
     }
     return eAnnotation.getDetails().get(annotationKey);
   }
 
   /**
    * Temp uris are used to correctly handle references when inserting new eobjects.
    * 
    * @see EObjectStore
    */
   public synchronized static URI makeTempURI(URI baseURI) {
     TEMP_COUNTER++;
     final long tempCounter = TEMP_COUNTER;
     return baseURI.appendQuery(TEMP_URI_CODE + "=" + tempCounter); //$NON-NLS-1$
   }
 
   /**
    * Is the uri string a temporary uri which needs to be handled differently than other uris.
    */
   public static boolean isTempURI(String baseURI) {
     return baseURI.contains(TEMP_URI_CODE + "="); //$NON-NLS-1$
   }
 
   /**
    * Convert a web service uri for an object to an EMF uri. EMF uri's use the fragment for the object identifier while
    * web service uri's will have the the identifier encoded in the segments.
    * 
    * example: http://example.com#Book||1 http://exampke.com/Book/1
    */
   public static URI convertToEMFURI(URI webServiceURI) {
     final TypeIdTuple tuple = getTypeAndIdFromUri(true, webServiceURI);
     final URI baseURI = webServiceURI.trimSegments(2);
     return baseURI.appendFragment(ModelUtils.getQualifiedNameFromEClass(tuple.getEClass())
         + ModelConstants.FRAGMENTSEPARATOR + tuple.getId());
   }
 
   /**
    * @see #convertToEMFURI(URI)
    */
   public static URI convertToWebServiceURI(URI emfURI) {
     final TypeIdTuple tuple = getTypeAndIdFromUri(true, emfURI);
     final URI baseURI = emfURI.trimFragment();
     return baseURI.appendSegment(ModelUtils.getQualifiedNameFromEClass(tuple.getEClass()))
         .appendSegment(tuple.getId() + "").appendFragment(""); //$NON-NLS-1$ //$NON-NLS-2$
   }
 
   public static TypeIdTuple getTypeAndIdFromUri(boolean useWebServiceFormat, URI objectUri) {
     EClass eClass;
     Object idValue;
     if (useWebServiceFormat && !objectUri.toString().contains(ModelConstants.FRAGMENTSEPARATOR)) {
       final String idString = objectUri.lastSegment();
       final String eClassName = objectUri.trimSegments(1).lastSegment();
       eClass = ModelUtils.getEClassFromQualifiedName(eClassName);
       if (eClass == null) {
         throw new IllegalArgumentException("No eclass found for uri " + objectUri.toString()); //$NON-NLS-1$
       }
       idValue = IdProvider.getInstance().convertIdStringToId(eClass, idString);
     } else {
       final String fragment = objectUri.fragment();
 
       final int separatorIndex = fragment == null ? -1 : fragment.indexOf(ModelConstants.FRAGMENTSEPARATOR);
       if (fragment == null || separatorIndex == -1) {
         if (fragment == null || fragment.trim().length() == 0) {
           throw new IllegalArgumentException(
               "No fragment, is the object URL maybe in webservice format? Consider setting the property useWebServiceFormat " //$NON-NLS-1$
               + "to true on the ObjectResolver (of the converter)"); //$NON-NLS-1$
         }
         throw new IllegalArgumentException("Fragment format not supported for fragment: " + fragment); //$NON-NLS-1$
       }
       eClass = ModelUtils.getEClassFromQualifiedName(fragment.substring(0, separatorIndex));
       final String idString = fragment.substring(separatorIndex + ModelConstants.FRAGMENTSEPARATOR_LENGTH);
       idValue = IdProvider.getInstance().convertIdStringToId(eClass, idString);
     }
     final TypeIdTuple tuple = new TypeIdTuple();
     tuple.setEClass(eClass);
     tuple.setId(idValue);
     return tuple;
   }
 
   public static class TypeIdTuple {
     private EClass eClass;
     private Object id;
 
     public EClass getEClass() {
       return eClass;
     }
 
     public void setEClass(EClass eClass) {
       this.eClass = eClass;
     }
 
     public Object getId() {
       return id;
     }
 
     public void setId(Object id) {
       this.id = id;
     }
 
   }
 
 }
