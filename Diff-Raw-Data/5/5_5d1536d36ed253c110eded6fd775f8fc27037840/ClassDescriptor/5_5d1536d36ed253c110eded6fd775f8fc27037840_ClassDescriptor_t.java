 package ecologylab.serialization;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.serialization.types.element.Mappable;
 
 /**
  * Cached object that holds all of the structures needed to optimize translations to and from XML
  * for a single subclass of ElementState. A rootOptimizationsMap keeps track of these, using the XML
  * tag as the key.
  * <p/>
  * This structure itself, as well as the structure within it, are created just in time, by lazy
  * evaluation.
  * 
  * @author andruid
  */
 public class ClassDescriptor<ES extends ElementState, FD extends FieldDescriptor> extends
 		ElementState implements FieldTypes, Mappable<String>, Iterable<FD>
 {
 	/**
 	 * Class object that we are describing.
 	 */
 	@simpl_scalar
 	private Class<ES>																			describedClass;
 
 	@simpl_scalar
 	private String																				tagName;
 
 	private String																				decribedClassSimpleName;
 
 	private String																				describedClassPackageName;
 
 	// @xml_attribute
 	// private String describedClassName;
 
 	/**
 	 * This is a pseudo FieldDescriptor object, defined for the class, for cases in which the tag for
 	 * the root element or a field is determined by class name, not by field name.
 	 */
 	private FieldDescriptor																pseudoFieldDescriptor;
 
 	/**
 	 * Handles a text node.
 	 */
 	private FieldDescriptor																scalarTextFD;
 
 	FieldDescriptor getScalarTextFD()
 	{
 		return scalarTextFD;
 	}
 
 	void setScalarTextFD(FieldDescriptor scalarTextFD)
 	{
 		this.scalarTextFD = scalarTextFD;
 	}
 	
 	public boolean hasScalarFD()
 	{
 		return scalarTextFD != null;
 	}
 
 
 	private boolean																				isGetAndOrganizeComplete;
 
 	/**
 	 * Map of FieldToXMLOptimizations, with field names as keys.
 	 * 
 	 * Used to optimize translateToXML(). Also handy for providing functionality like associative
 	 * arrays in Perl, JavaScript, PHP, ..., but with less overhead, because the hashtable is only
 	 * maintained per class, not per instance.
 	 */
 	@simpl_nowrap
 	@simpl_map("field_descriptor")
 	private HashMapArrayList<String, FD>									fieldDescriptorsByFieldName		= new HashMapArrayList<String, FD>();
 
 	/**
 	 * This data structure is handy for translateFromXML(). There can be multiple tags (keys in this
 	 * map) for a single FieldDescriptor if @xml_other_tags is used.
 	 */
 	// TODO -- consider changing this to Scope<FieldDescriptor>, then nesting scopes when @xml_scope
 	// is encountered, to support dynamic binding of @xml_scope.
 	private HashMap<String, FD>														allFieldDescriptorsByTagNames	= new HashMap<String, FD>();
 
 	private ArrayList<FD>																	attributeFieldDescriptors			= new ArrayList<FD>();
 
 	private ArrayList<FD>																	elementFieldDescriptors				= new ArrayList<FD>();										;
 
 	private static final HashMap<String, ClassDescriptor>	globalClassDescriptorsMap			= new HashMap<String, ClassDescriptor>();
 
 	private ArrayList<FD>																	unresolvedScopeAnnotationFDs;
 
 	// private HashMap<String, Class<? extends ElementState>> nameSpaceClassesById = new
 	// HashMap<String, Class<? extends ElementState>>();
 
 	/**
 	 * Default constructor only for use by translateFromXML().
 	 */
 	public ClassDescriptor()
 	{
 		super();
 	}
 
 	protected ClassDescriptor(Class<ES> thatClass)
 	{
 		super();
 		this.describedClass = thatClass;
 		this.decribedClassSimpleName = thatClass.getSimpleName();
 		this.describedClassPackageName = thatClass.getPackage().getName();
 		// this.describedClassName = thatClass.getName();
 		this.tagName = XMLTools.getXmlTagName(thatClass, TranslationScope.STATE);
 	}
 
 	public String getTagName()
 	{
 		return tagName;
 	}
 
 	/**
 	 * Obtain Optimizations object in the global scope of root Optimizations. Uses just-in-time / lazy
 	 * evaluation. The first time this is called for a given ElementState class, it constructs a new
 	 * Optimizations saves it in our rootOptimizationsMap, and returns it.
 	 * <p/>
 	 * Subsequent calls merely pass back the already created object from the rootOptimizationsMap.
 	 * 
 	 * @param elementState
 	 *          An ElementState object that we're looking up Optimizations for.
 	 * @return
 	 */
 	public static ClassDescriptor getClassDescriptor(ElementState elementState)
 	{
 		Class<? extends ElementState> thatClass = elementState.getClass();
 
 		return getClassDescriptor(thatClass);
 	}
 
 	static final Class[]	CONSTRUCTOR_ARGS	=
 																					{ Class.class };
 
 	/**
 	 * Obtain Optimizations object in the global scope of root Optimizations. Uses just-in-time / lazy
 	 * evaluation. The first time this is called for a given ElementState class, it constructs a new
 	 * Optimizations saves it in our rootOptimizationsMap, and returns it.
 	 * <p/>
 	 * Subsequent calls merely pass back the already created object from the rootOptimizationsMap.
 	 * 
 	 * @param thatClass
 	 * @return
 	 */
 	public static ClassDescriptor getClassDescriptor(Class<? extends ElementState> thatClass)
 	{
 		String className = thatClass.getName();
 		// stay out of the synchronized block most of the time
 		ClassDescriptor result = globalClassDescriptorsMap.get(className);
 		if (result == null || !result.isGetAndOrganizeComplete)
 		{
 			// but still be thread safe!
 			synchronized (globalClassDescriptorsMap)
 			{
 				result = globalClassDescriptorsMap.get(className);
 				if (result == null)
 				{
 					final simpl_descriptor_classes descriptorsClassesAnnotation = thatClass
 							.getAnnotation(simpl_descriptor_classes.class);
 					if (descriptorsClassesAnnotation == null)
 						result = new ClassDescriptor(thatClass);
 					else
 					{
 						Class aClass = descriptorsClassesAnnotation.value()[0];
 						Object[] args = new Object[1];
 						args[0] = thatClass;
 						result = (ClassDescriptor) ReflectionTools.getInstance(aClass, CONSTRUCTOR_ARGS, args);
 					}
 					globalClassDescriptorsMap.put(className, result);
 
 					// NB: this call was moved out of the constructor to avoid recursion problems
 					result.deriveAndOrganizeFieldsRecursive(thatClass, null);
 					result.isGetAndOrganizeComplete = true;
 				}
 				// THIS SHOULD NEVER HAPPEN!!!
 				// but it does in classes like linked list, that is, one with a field that refers to an
 				// instance of itself
 				else if (!result.isGetAndOrganizeComplete)
 				{
 					if (!thatClass.equals(FieldDescriptor.class) && !thatClass.equals(ClassDescriptor.class))
 						result
 								.warning(" Circular reference (probably fine, but perhaps a race condition that should never happen.");
 				}
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Form a pseudo-FieldDescriptor-object for a root element. We say pseudo, because there is no
 	 * Field corresponding to this element. The pseudo-FieldDescriptor-object still guides the
 	 * translation process.
 	 * 
 	 * @return
 	 */
 	public FieldDescriptor pseudoFieldDescriptor()
 	{
 		FieldDescriptor result = pseudoFieldDescriptor;
 		if (result == null)
 		{
 			synchronized (this)
 			{
 				result = pseudoFieldDescriptor;
 				if (result == null)
 				{
 					result = new FieldDescriptor(this);
 					pseudoFieldDescriptor = result;
 				}
 			}
 		}
 		return result;
 	}
 
 	// /**
 	// * @return Returns the nameSpacePrefix.
 	// */
 	// String nameSpacePrefix()
 	// {
 	// return nameSpacePrefix;
 	// }
 	// /**
 	// * @param nameSpacePrefix The nameSpacePrefix to set.
 	// */
 	// void setNameSpacePrefix(String nameSpacePrefix)
 	// {
 	// this.nameSpacePrefix = nameSpacePrefix;
 	// }
 
 	public ArrayList<FD> attributeFieldDescriptors()
 	{
 		return attributeFieldDescriptors;
 	}
 
 	public ArrayList<FD> elementFieldDescriptors()
 	{
 		return elementFieldDescriptors;
 	}
 
 	private HashMapArrayList<String, FD>	fieldDescriptors;
 
 	public FD getFieldDescriptorByTag(String tag, TranslationScope tScope, ElementState context)
 	{
 		// TODO -- add support for name space lookup in context here
 		if (unresolvedScopeAnnotationFDs != null)
 			resolveUnresolvedScopeAnnotationFDs();
 
 		return allFieldDescriptorsByTagNames.get(tag);
 	}
 
 	public FD getFieldDescriptorByTag(String tag, TranslationScope tScope)
 	{
 		return getFieldDescriptorByTag(tag, tScope, null);
 	}
 
 	public FieldDescriptor getFieldDescriptorByFieldName(String fieldName)
 	{
 		return fieldDescriptorsByFieldName.get(fieldName);
 	}
 
 	public Iterator<FD> iterator()
 	{
 		return fieldDescriptorsByFieldName.iterator();
 	}
 
 	/**
 	 * Build and return an ArrayList with Field objects for all the annotated fields in this class.
 	 * 
 	 * @param fieldDescriptorClass
 	 *          The Class to use for instantiating each FieldDescriptor. The default is
 	 *          FieldDescriptor, but class objects may be passed in that extend that class.
 	 * 
 	 * @return HashMapArrayList of Field objects, using the XML tag name for each field (not its Java
 	 *         field name!) as the keys. Could be empty. Never null.
 	 */
 
 	private Class<FieldDescriptor> fieldDescriptorAnnotationValue(
 			Class<? extends ElementState> thatClass)
 	{
 		final simpl_descriptor_classes fieldDescriptorsClassAnnotation = thatClass
 				.getAnnotation(simpl_descriptor_classes.class);
 		Class<FieldDescriptor> result = null;
 		if (fieldDescriptorsClassAnnotation != null)
 		{
 			Class annotatedFieldDescriptorClass = fieldDescriptorsClassAnnotation.value()[1];
 			if (annotatedFieldDescriptorClass != null
 					&& FieldDescriptor.class.isAssignableFrom(annotatedFieldDescriptorClass))
 				result = (Class<FieldDescriptor>) annotatedFieldDescriptorClass;
 		}
 		return result;
 	}
 
 	/**
 	 * Recursive method to create optimized data structures needed for translation to and from XML,
 	 * and also for efficient reflection-based access to field (descriptors) at run-time, with field
 	 * name as a variable.
 	 * <p/>
 	 * Recurses up the chain of inherited Java classes, when @xml_inherit is specified.
 	 * 
 	 * @param fdc
 	 *          TODO
 	 */
 	private synchronized Class<FD> deriveAndOrganizeFieldsRecursive(
 			Class<? extends ElementState> classWithFields, Class<FD> fieldDescriptorClass)
 	{
 		if (fieldDescriptorClass == null)
 		{ // look for annotation in super class if subclass didn't have one
 			fieldDescriptorClass = (Class<FD>) fieldDescriptorAnnotationValue(classWithFields);
 		}
 
 		if (classWithFields.isAnnotationPresent(simpl_inherit.class))
 		{ // recurse on super class first, so subclass declarations shadow those in superclasses, where
 			// there are field name conflicts
 			Class superClass = classWithFields.getSuperclass();
 
 			if (superClass != null)
 			{
 				Class<FD> superFieldDescriptorClass = deriveAndOrganizeFieldsRecursive(superClass,
 						fieldDescriptorClass);
 				// only assign (override) from super if we haven't found one here.
 				if (fieldDescriptorClass == null)
 					fieldDescriptorClass = superFieldDescriptorClass;
 			}
 		}
 
 		Field[] fields = classWithFields.getDeclaredFields();
 
 		for (int i = 0; i < fields.length; i++)
 		{
 			Field thatField = fields[i];
 
 			// skip static fields, since we're saving instances,
 			// and inclusion w each instance would be redundant.
 			if ((thatField.getModifiers() & Modifier.STATIC) == Modifier.STATIC)
 			{
 				// debug("Skipping " + thatField + " because its static!");
 				continue;
 			}
 //			boolean isEnum = XMLTools.isEnum(thatField);
 			// TODO -- if fieldDescriptorClass is already defined, then use it w reflection, instead of
 			// FieldDescriptor, itself
 //			if (XMLTools.representAsAttribute(thatField))
 //			{
 //				isElement = false;
 //				int type = !isEnum ? ATTRIBUTE : ENUMERATED_ATTRIBUTE;
 //				fieldDescriptor = newFieldDescriptor(thatField, type, fieldDescriptorClass);
 //			}
 //			else if (XMLTools.representAsLeaf(thatField))
 //			{
 //				int type = !isEnum ? LEAF : ENUMERATED_LEAF;
 //				fieldDescriptor = newFieldDescriptor(thatField, type, fieldDescriptorClass);
 //			}
 //			else 
 			int fieldType			= UNSET_TYPE;
 			
 			if (XMLTools.isScalar(thatField))
 			{
 				fieldType				= SCALAR;
 			}
 			else if (XMLTools.representAsComposite(thatField))
 			{
 				fieldType				= COMPOSITE_ELEMENT;
 			}
 			else if (XMLTools.representAsCollection(thatField))
 			{
 				fieldType				= COLLECTION_ELEMENT;
 			}
 			else if (XMLTools.representAsMap(thatField))
 			{
 				fieldType				= MAP_ELEMENT;
 			}
 			if (fieldType == UNSET_TYPE)
 				continue;	// not a simpl serialization annotated field
 			
 			FD fieldDescriptor = newFieldDescriptor(thatField, fieldType, fieldDescriptorClass);
			
 			// create indexes for serialize
			if (fieldDescriptor.getType() == SCALAR)
 			{
 				Hint xmlHint = fieldDescriptor.getXmlHint();
 				switch (xmlHint)
 				{
 				case XML_ATTRIBUTE:
 					attributeFieldDescriptors.add(fieldDescriptor);
 					break;
 				case XML_TEXT:
 				case XML_TEXT_CDATA:
 					break;
 				case XML_LEAF:
 				case XML_LEAF_CDATA:
 					elementFieldDescriptors.add(fieldDescriptor);
 					break;
 				}
 			}
 			else
 				elementFieldDescriptors.add(fieldDescriptor);
 			
 			// TODO -- throughout this block -- instead of just put, do contains() before put,
 			// and generate a warning message if a mapping is being overridden
 			fieldDescriptorsByFieldName.put(thatField.getName(), fieldDescriptor);
 
 			if (fieldDescriptor.isMarshallOnly())
 				continue; // not translated from XML, so don't add those mappings
 
 			// create mappings for translateFromXML() --> allFieldDescriptorsByTagNames
 			final String fieldTagName = fieldDescriptor.getTagName();
 			if (fieldDescriptor.isWrapped())
 			{
 				FD wrapper = newFieldDescriptor(fieldDescriptor, fieldTagName, fieldDescriptorClass);
 				mapTagToFdForTranslateFrom(fieldTagName, wrapper);
 			}
 			else if (!fieldDescriptor.isPolymorphic()) // tag(s) from field, not from class :-)
 			{
 				String tag = fieldDescriptor.isCollection() ? fieldDescriptor.getCollectionOrMapTagName()
 						: fieldTagName;
 				mapTagToFdForTranslateFrom(tag, fieldDescriptor);
 
 				// also add mappings for @xml_other_tags
 				String[] otherTags = fieldDescriptor.otherTags();
 				if (otherTags != null)
 				{
 					// TODO -- @xml_other_tags for collection/map how should it work?!
 					for (String otherTag : otherTags)
 						mapTagToFdForTranslateFrom(otherTag, fieldDescriptor);
 				}
 			}
 			else
 			{ // add mappings by class tagNames for polymorphic elements & collections
 				// TODO add support for wrapped polymorphic collections!
 				mapTagClassDescriptors(fieldDescriptor);
 			}
 			thatField.setAccessible(true); // else -- ignore non-annotated fields
 		} // end for all fields
 		return fieldDescriptorClass;
 	}
 
 	/**
 	 * @param fieldDescriptor
 	 */
 	void mapTagClassDescriptors(FD fieldDescriptor)
 	{
 		HashMapArrayList<String, ClassDescriptor> tagClassDescriptors = fieldDescriptor
 				.getTagClassDescriptors();
 
 		if (tagClassDescriptors != null)
 			for (String tagName : tagClassDescriptors.keySet())
 			{
 				mapTagToFdForTranslateFrom(tagName, fieldDescriptor);
 			}
 
 		// Collection<ClassDescriptor> tagClassDescriptors = fieldDescriptor.getTagClassDescriptors();
 		// if (tagClassDescriptors != null)
 		// {
 		// for (ClassDescriptor classDescriptor: tagClassDescriptors)
 		// {
 		// mapTagToFdForTranslateFrom(classDescriptor.tagName, fieldDescriptor);
 		// }
 		// }
 	}
 
 	static final Class[]	FIELD_DESCRIPTOR_ARGS	=
 																							{ ClassDescriptor.class, Field.class, int.class };
 
 	/**
 	 * @param thatField
 	 * @param fieldDescriptorClass
 	 *          TODO
 	 * @return
 	 */
 	private FD newFieldDescriptor(Field thatField, int annotationType, Class<FD> fieldDescriptorClass)
 	{
 		if (fieldDescriptorClass == null)
 			return (FD) new FieldDescriptor(this, thatField, annotationType);
 
 		Object args[] = new Object[3];
 		args[0] = this;
 		args[1] = thatField;
 		args[2] = annotationType;
 
 		return (FD) ReflectionTools.getInstance(fieldDescriptorClass, FIELD_DESCRIPTOR_ARGS, args);
 	}
 
 	static final Class[]	WRAPPER_FIELD_DESCRIPTOR_ARGS	=
 																											{ ClassDescriptor.class,
 			FieldDescriptor.class, String.class						};
 
 	private FD newFieldDescriptor(FD wrappedFD, String wrapperTag, Class<FD> fieldDescriptorClass)
 	{
 		if (fieldDescriptorClass == null)
 			return (FD) new FieldDescriptor(this, wrappedFD, wrapperTag);
 
 		Object args[] = new Object[3];
 		args[0] = this;
 		args[1] = wrappedFD;
 		args[2] = wrapperTag;
 
 		return (FD) ReflectionTools.getInstance(fieldDescriptorClass, WRAPPER_FIELD_DESCRIPTOR_ARGS,
 				args);
 	}
 
 	/**
 	 * Map the tag to the FieldDescriptor for use in translateFromXML() for elements of this class
 	 * type.
 	 * 
 	 * @param tagName
 	 * @param fdToMap
 	 */
 	private void mapTagToFdForTranslateFrom(String tagName, FD fdToMap)
 	{
 		FD previousMapping = allFieldDescriptorsByTagNames.put(tagName, fdToMap);
 		if (previousMapping != null)
 			warning(" tag <" + tagName + ">:\tfield[" + fdToMap.getFieldName() + "] overrides field["
 					+ previousMapping.getFieldName() + "]");
 	}
 
 	/**
 	 * @param thatField
 	 * @param tagFromAnnotation
 	 * @param required
 	 */
 	private void annotatedFieldError(Field thatField, String tagFromAnnotation, String required)
 	{
 		String tagMsg = ((tagFromAnnotation == null) || (tagFromAnnotation.length() == 0)) ? "" : ("\""
 				+ tagFromAnnotation + "\"");
 
 		error("@xml_collection(" + tagMsg + ") declared as type " + thatField.getType().getSimpleName()
 				+ " for field named " + thatField.getName() + ", which is not a " + required + ".");
 	}
 
 	/**
 	 * Add an entry to our map of Field objects, using the field's name as the key. Used, for example,
 	 * for ignored fields.
 	 * 
 	 * @param fieldDescriptor
 	 */
 	void addFieldDescriptorMapping(FD fieldDescriptor)
 	{
 		// FIXME is tag determined by field by class?
 		String tagName = fieldDescriptor.getTagName();
 		if (tagName != null)
 			mapTagToFdForTranslateFrom(tagName, fieldDescriptor);
 	}
 
 	public String toString()
 	{
 		return getClassName() + "[" + describedClass.getName() + "]";
 	}
 
 	/**
 	 * Map an XML namespace id to the class that should be instantiated to handle it.
 	 * 
 	 * @param translationScope
 	 *          Used for error messages.
 	 * @param nsID
 	 * @param urn
 	 *          TODO
 	 */
 	void mapNamespaceIdToClass(TranslationScope translationScope, String nsID, String urn)
 	{
 		// TODO -- Name Space support!
 		// if (!nameSpaceClassesById.containsKey(nsID))
 		// {
 		// Class<? extends ElementState> nsClass = translationScope.lookupNameSpaceByURN(urn);
 		// final boolean nsUnsupported = (nsClass == null);
 		// nameSpaceClassesById().put(nsID, nsClass);
 		// // FieldToXMLOptimizations xmlnsF2XO = new FieldToXMLOptimizations(nsID, urn, nsUnsupported);
 		// if (nsUnsupported)
 		// warning("No Namespace found in " + translationScope + " for\t\txmlns:" + nsID +"=" + urn);
 		// else
 		// {
 		// debug("FIXME -- COOL! " + translationScope + " \t" + nsClass.getName() + " ->\t\txmlns:" +
 		// nsID +"=" + urn);
 		// // xmlnsAttributeOptimizations().add(xmlnsF2XO);
 		// }
 		// }
 	}
 
 	// boolean containsNameSpaceClass(String nsID)
 	// {
 	// return nameSpaceClassesById().containsKey(nsID);
 	// }
 	// Class<? extends ElementState> lookupNameSpaceClassById(String nsID)
 	// {
 	// return nameSpaceClassesById().get(nsID);
 	// }
 	//
 	// HashMap<String, Class<? extends ElementState>> nameSpaceClassesById()
 	// {
 	// HashMap<String, Class<? extends ElementState>> result = nameSpaceClassesById;
 	// if (result == null)
 	// {
 	// result = new HashMap<String, Class<? extends ElementState>>(2);
 	// nameSpaceClassesById = result;
 	// }
 	// return result;
 	// }
 
 	/**
 	 * @return the Class Object that this describes.
 	 */
 	public Class<ES> describedClass()
 	{
 		return describedClass;
 	}
 
 	public Class<ES> getDescribedClass()
 	{
 		return describedClass;
 	}
 
 	/**
 	 * 
 	 * @return true if this is an empty entry, for a tag that we do not parse. No class is associated
 	 *         with such an entry.
 	 */
 	public boolean isEmpty()
 	{
 		return describedClass == null;
 	}
 
 	public String getDecribedClassSimpleName()
 	{
 		return decribedClassSimpleName;
 	}
 
 	public String getDescribedClassPackageName()
 	{
 		return describedClassPackageName;
 	}
 
 	public ES getInstance() throws SIMPLTranslationException
 	{
 		return XMLTools.getInstance(describedClass);
 	}
 
 	public int numFields()
 	{
 		return allFieldDescriptorsByTagNames.size();
 	}
 
 	/**
 	 * The tagName.
 	 */
 	public String key()
 	{
 		return tagName;
 	}
 
 	public HashMapArrayList<String, FD> getFieldDescriptorsByFieldName()
 	{
 		return fieldDescriptorsByFieldName;
 	}
 
 	public String getSuperClassName()
 	{
 		return XMLTools.getClassName(describedClass.getSuperclass());
 	}
 
 	public static void main(String[] s)
 	{
 		TranslationScope mostBasicTranslations = TranslationScope.get("most_basic",
 				ClassDescriptor.class, FieldDescriptor.class, TranslationScope.class);
 
 		try
 		{
 			mostBasicTranslations.serialize(System.out);
 		}
 		catch (SIMPLTranslationException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Keep track of any FieldDescriptors with unresolved @serial_scope declarations so we can try to
 	 * resolve them later when there is use.
 	 * 
 	 * @param fd
 	 */
 	void registerUnresolvedScopeAnnotationFD(FD fd)
 	{
 		if (unresolvedScopeAnnotationFDs == null)
 		{
 			synchronized (this)
 			{
 				if (unresolvedScopeAnnotationFDs == null)
 					unresolvedScopeAnnotationFDs = new ArrayList<FD>();
 			}
 		}
 		unresolvedScopeAnnotationFDs.add(fd);
 	}
 
 	/**
 	 * Late evaluation of @serial_scope, if it failed the first time around.
 	 */
 	public void resolveUnresolvedScopeAnnotationFDs()
 	{
 		if (unresolvedScopeAnnotationFDs != null)
 		{
 			for (int i = unresolvedScopeAnnotationFDs.size() - 1; i >= 0; i--)
 			{
 				// TODO -- do we want to enable retrying multiple times in case it gets fixed even later
 				FieldDescriptor fd = unresolvedScopeAnnotationFDs.remove(i);
 				fd.resolveUnresolvedScopeAnnotation();
 			}
 		}
 		unresolvedScopeAnnotationFDs = null;
 	}
 
 	/**
 	 * Use the @xml_other_tags annotation to obtain an array of alternative (old) tags for this class.
 	 * 
 	 * @return The array of old tags, or null, if there is no @xml_other_tags annotation.
 	 */
 	String[] otherTags()
 	{
 		Class<ES> thisClass = describedClass();
 		final ElementState.xml_other_tags otherTagsAnnotation = thisClass
 				.getAnnotation(xml_other_tags.class);
 
 		// commented out since getAnnotation also includes inherited annotations
 		// ElementState.xml_other_tags otherTagsAnnotation =
 		// thisClass.getAnnotation(ElementState.xml_other_tags.class);
 		if (otherTagsAnnotation == null)
 			return null;
 		String[] result = otherTagsAnnotation.value();
 		return result == null ? null : result.length == 0 ? null : result;
 	}
 }
