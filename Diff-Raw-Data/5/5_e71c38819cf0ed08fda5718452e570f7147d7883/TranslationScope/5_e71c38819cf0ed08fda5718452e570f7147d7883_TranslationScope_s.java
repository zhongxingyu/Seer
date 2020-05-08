 package ecologylab.serialization;
 
 import java.io.File;
 import java.io.InputStream;
 import java.lang.annotation.Annotation;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 
 import ecologylab.collections.Scope;
 import ecologylab.generic.HashMapArrayList;
 import ecologylab.net.ParsedURL;
 import ecologylab.serialization.types.scalar.ScalarType;
 import ecologylab.serialization.types.scalar.TypeRegistry;
 
 /**
  * A set of bindings between XML element names (tags) and associated simple (without package) class
  * names, and associated Java ElementState classes. Inheritance is supported.
  */
 public final class TranslationScope extends ElementState
 {
 	private static final int														GUESS_CLASSES_PER_TSCOPE	= 5;
 
 	@simpl_scalar
 	private/* final */String														name;
 
 	private TranslationScope[]													inheritedTranslationScopes;
 
 	/**
 	 * Fundamentally, a TranslationScope consists of a set of class simple names. These are mapped to
 	 * tag names (camel case conversion), and to Class objects. Because there are many packages,
 	 * globally, there could be more than one class with one single name.
 	 * <p/>
 	 * Among other things, a TranslationScope tells us *which* package's version will be used, if
 	 * there are multiple possibilities. This is the case when internal and external versions of a
 	 * message and its constituents are defined for a messaging API.
 	 */
 	private Scope<ClassDescriptor>											entriesByClassSimpleName	= new Scope<ClassDescriptor>();
 
 	private Scope<ClassDescriptor>											entriesByClassName				= new Scope<ClassDescriptor>();
 
 	@simpl_nowrap
 	@simpl_map("class_descriptor")
 	private Scope<ClassDescriptor>											entriesByTag							= new Scope<ClassDescriptor>();
 
 	private final Scope<Class<? extends ElementState>>	nameSpaceClassesByURN			= new Scope<Class<? extends ElementState>>();
 
 	private static HashMap<String, TranslationScope>		allTranslationScopes			= new HashMap<String, TranslationScope>();
 
 	public static final String													STATE											= "State";
 	
 	private boolean																			performFilters;
 
 	/**
 	 * Default constructor only for use by translateFromXML().
 	 */
 	public TranslationScope()
 	{
 
 	}
 
 	/**
 	 * Building block called by other constructors for most basic name registration functionality.
 	 * 
 	 * @param name
 	 */
 	private TranslationScope(String name)
 	{
 		this.name = name;
 	}
 
 	/**
 	 * Create a new TranslationScope that defines how to translate xml tag names into class names of
 	 * subclasses of ElementState. Begin by copying in the translations from another, pre-existing
 	 * "base" TranslationScope.
 	 * 
 	 * @param name
 	 * @param inheritedTranslationScope
 	 */
 	private TranslationScope(String name, TranslationScope inheritedTranslationScope)
 	{
 		this(name);
 		addTranslations(inheritedTranslationScope);
 		TranslationScope[] inheritedTranslationScopes = new TranslationScope[1];
 		inheritedTranslationScopes[0] = inheritedTranslationScope;
 		this.inheritedTranslationScopes = inheritedTranslationScopes;
 	}
 
 	private TranslationScope(String name, TranslationScope inheritedTranslationScope,
 			Class<? extends ElementState> translation)
 	{
 		this(name, inheritedTranslationScope);
 		addTranslation(translation);
 		addTranslationScope(name);
 	}
 
 	/**
 	 * Create a new TranslationScope that defines how to translate xml tag names into class names of
 	 * subclasses of ElementState. Begin by copying in the translations from another, pre-existing
 	 * "base" TranslationScope.
 	 * 
 	 * @param name
 	 * @param baseTranslationSet
 	 */
 	private TranslationScope(String name, TranslationScope... inheritedTranslationScopes)
 	{
 		this(name);
 
 		if (inheritedTranslationScopes != null)
 		{
 			this.inheritedTranslationScopes = inheritedTranslationScopes;
 			int n = inheritedTranslationScopes.length;
 			for (int i = 0; i < n; i++)
 				addTranslations(inheritedTranslationScopes[i]);
 		}
 	}
 
 	/**
 	 * Create a new TranslationScope that defines how to translate xml tag names into class names of
 	 * subclasses of ElementState. Begin by copying in the translations from another, pre-existing
 	 * "base" TranslationScope.
 	 * 
 	 * @param name
 	 * @param baseTranslationSet
 	 */
 	private TranslationScope(String name, Collection<TranslationScope> baseTranslationsSet)
 	{
 		this(name);
 		for (TranslationScope thatTranslationScope : baseTranslationsSet)
 			addTranslations(thatTranslationScope);
 		inheritedTranslationScopes = (TranslationScope[]) baseTranslationsSet.toArray();
 	}
 
 	/**
 	 * Create a new TranslationScope that defines how to translate xml tag names into class names of
 	 * subclasses of ElementState.
 	 * 
 	 * Set a new default package, and a set of defined translations.
 	 * 
 	 * @param name
 	 *          Name of the TranslationSpace to be A key for use in the TranslationSpace registry.
 	 * @param translations
 	 *          Set of initially defined translations for this.
 	 * @param defaultPackgeName
 	 */
 	private TranslationScope(String name, Class<? extends ElementState>... translations)
 	{
 		this(name, (TranslationScope[]) null, translations);
 		addTranslationScope(name);
 	}
 
 	/**
 	 * Construct a new TranslationScope, with this name, using the baseTranslations first. Then, add
 	 * the array of translations, then, make the defaultPackageName available.
 	 * 
 	 * @param name
 	 * @param inheritedTranslationScopes
 	 * @param translations
 	 */
 	private TranslationScope(String name, TranslationScope[] inheritedTranslationScopes,
 			Class<? extends ElementState>[]... translations)
 	{
 		this(name, inheritedTranslationScopes);
 		addTranslations(translations);
 	}
 
 	/**
 	 * Construct a new TranslationScope, with this name, using the baseTranslations first. Then, add
 	 * the array of translations, then, make the defaultPackageName available.
 	 * 
 	 * @param name
 	 * @param translations
 	 * @param baseTranslations
 	 */
 	private TranslationScope(String name, Collection<TranslationScope> inheritedTranslationsSet,
 			Class<? extends ElementState>[] translations)
 	{
 		this(name, inheritedTranslationsSet);
 		addTranslations(translations);
 
 		addTranslationScope(name);
 	}
 
 	/**
 	 * Construct a new TranslationScope, with this name, using the baseTranslations first. Then, add
 	 * the array of translations, then, make the defaultPackageName available.
 	 * 
 	 * @param name
 	 * @param inheritedTranslationScope
 	 * @param translations
 	 */
 	private TranslationScope(String name, TranslationScope inheritedTranslationScope,
 			Class<? extends ElementState>[]... translations)
 	{
 		this(name, inheritedTranslationScope);
 		addTranslations(translations);
 
 		addTranslationScope(name);
 	}
 
 	/**
 	 * Construct a new TranslationScope, with this name, using the baseTranslations first. Then, add
 	 * the array of translations, then, make the defaultPackageName available. Map XML Namespace
 	 * declarations.
 	 * 
 	 * @param name
 	 * @param nameSpaceDecls
 	 * @param inheritedTranslationScopes
 	 * @param translations
 	 * @param defaultPackgeName
 	 */
 	private TranslationScope(String name, NameSpaceDecl[] nameSpaceDecls,
 			TranslationScope[] inheritedTranslationScopes, Class<? extends ElementState>[] translations)
 	{
 		this(name, inheritedTranslationScopes, translations);
 		addNameSpaceDecls(nameSpaceDecls);
 
 		addTranslationScope(name);
 	}
 
 	/**
 	 * Map XML Namespace ElementState subclasses to URIs.
 	 * 
 	 * @param nameSpaceDecls
 	 */
 	private void addNameSpaceDecls(NameSpaceDecl[] nameSpaceDecls)
 	{
 		if (nameSpaceDecls != null)
 			for (NameSpaceDecl nsd : nameSpaceDecls)
 			{
 				registerNameSpaceDecl(nsd);
 			}
 	}
 
 	/**
 	 * Enter a NameSpaceDecl into nameSpaceClassesByURN.
 	 * 
 	 * @param nsd
 	 */
 	private void registerNameSpaceDecl(NameSpaceDecl nsd)
 	{
 		nameSpaceClassesByURN.put(nsd.urn, nsd.esClass);
 	}
 
 	/**
 	 * Add translations, where each translation is defined by an actual Class object. We can get both
 	 * the class name and the package name from the Class object.
 	 * 
 	 * @param classes
 	 */
 	private void addTranslations(Class<? extends ElementState>[]... arrayOfClasses)
 	{
 		if (arrayOfClasses != null)
 		{
 			int numClasses = arrayOfClasses.length;
 
 			for (int i = 0; i < numClasses; i++)
 			{
 				if (arrayOfClasses[i] != null)
 				{
 					for (Class<? extends ElementState> thatClass : arrayOfClasses[i])
 					{
 						addTranslation(thatClass);
 					}
 				}
 			}
 		}
 
 		allTranslationScopes.put(name, this);
 	}
 
 	/**
 	 * Utility for composing <code>TranslationScope</code>s. Performs composition by value. That is,
 	 * the entries are copied.
 	 * 
 	 * Unlike in union(), if there are duplicates, they will override identical entries in this.
 	 * 
 	 * @param inheritedTranslationScope
 	 */
 	private void addTranslations(TranslationScope inheritedTranslationScope)
 	{
 		if (inheritedTranslationScope != null)
 		{
 			// copy map entries from inherited maps into new maps
 			updateMapWithValues(inheritedTranslationScope.entriesByClassSimpleName,
 					entriesByClassSimpleName, "classSimpleName");
 			updateMapWithValues(inheritedTranslationScope.entriesByClassName, entriesByClassName,
 					"className");
 			updateMapWithValues(inheritedTranslationScope.entriesByTag, entriesByTag, "tagName");
 
 			HashMap<String, Class<? extends ElementState>> inheritedNameSpaceClassesByURN = inheritedTranslationScope.nameSpaceClassesByURN;
 			if (inheritedNameSpaceClassesByURN != null)
 			{
 				for (String urn : inheritedNameSpaceClassesByURN.keySet())
 				{
 					nameSpaceClassesByURN.put(urn, inheritedNameSpaceClassesByURN.get(urn));
 				}
 			}
 		}
 	}
 
 	/**
 	 * Update the Map with all the entries in the inherited Map.
 	 * 
 	 * @param inheritedMap
 	 * @param warn
 	 */
 	private void updateMapWithValues(Map<String, ClassDescriptor> inheritedMap,
 			Map<String, ClassDescriptor> newMap, String warn)
 	{
 		// XXX ANDRUID + ZACH -> concurrent modification exception can occur here (for loop) if
 		// inheritedMap is modified elsewhere
 		for (String key : inheritedMap.keySet())
 		{
 			ClassDescriptor translationEntry = inheritedMap.get(key);
 			updateMapWithEntry(newMap, key, translationEntry, warn);
 		}
 	}
 
 	/**
 	 * Update the Map with the entry.
 	 * 
 	 * @param newMap
 	 * @param key
 	 * @param translationEntry
 	 *          Must be non-null.
 	 * @param warn
 	 */
 	private void updateMapWithEntry(Map<String, ClassDescriptor> newMap, String key,
 			ClassDescriptor translationEntry, String warn)
 	{
 		ClassDescriptor existingEntry = newMap.get(key);
 
 		// final boolean entryExists = existingEntry != null;
 		// final boolean newEntry = existingEntry != translationEntry;
 
 		final boolean entryExists = existingEntry != null;
 		final boolean newEntry = !entryExists ? true
 				: existingEntry.getDescribedClass() != translationEntry.getDescribedClass();
 
 		if (newEntry)
 		{
 			if (entryExists) // look out for redundant entries
 				warning("Overriding " + warn + " " + key + " with " + translationEntry);
 
 			newMap.put(key, translationEntry);
 		}
 		// if (entryExists && newEntry) // look out for redundant entries
 		// warning("Overriding " + warn + " " + key + " with " + translationEntry);
 		//
 		// if (/** !entryExists || **/ newEntry)
 		// newMap.put(key, translationEntry);
 	}
 
 	/**
 	 * Add a translation table entry for an ElementState derived sub-class. Assumes that the xmlTag
 	 * can be derived automatically from the className, by translating case-based separators to
 	 * "_"-based separators.
 	 * 
 	 * @param classObj
 	 *          The object for the class.
 	 */
 	public void addTranslation(Class<? extends ElementState> classObj)
 	{
 		ClassDescriptor entry = ClassDescriptor.getClassDescriptor(classObj);
 		entriesByTag.put(entry.getTagName(), entry);
 		entriesByClassSimpleName.put(entry.getDecribedClassSimpleName(), entry);
 		entriesByClassName.put(classObj.getName(), entry);
 
 		String[] otherTags = entry.otherTags();
 		if (otherTags != null)
 			for (String otherTag : otherTags)
 			{
 				if ((otherTag != null) && (otherTag.length() > 0))
 				{
 					entriesByTag.put(otherTag, entry);
 				}
 			}
 	}
 
 	/**
 	 * Look-up a <code>Class</code> object for the xmlTag, using translations in this, and in
 	 * inherited TranslationScopes. Will use defaultPackage name here and, recursivley, in inherited
 	 * scopes, as necessary.
 	 * 
 	 * @param xmlTag
 	 *          XML node name that we're seeking a Class for.
 	 * @return Class object, or null if there is no associated translation.
 	 */
 	public Class<? extends ElementState> xmlTagToClass(String xmlTag)
 	{
 		ClassDescriptor entry = xmlTagToTranslationEntry(xmlTag);
 		return entry.isEmpty() ? null : entry.getDescribedClass();
 	}
 
 	/**
 	 * Seek the entry associated with the tag. Recurse through inherited TranslationScopes, if
 	 * necessary.
 	 * 
 	 * @param xmlTag
 	 * @return
 	 */
 	private ClassDescriptor xmlTagToTranslationEntry(String xmlTag)
 	{
 		return getClassDescriptorByTag(xmlTag);
 		/*
 		 * TranslationEntry entry = entriesByTag.get(xmlTag); if (entry == null) { String
 		 * defaultPackageName = this.defaultPackageName; if (defaultPackageName != null) { String
 		 * classSimpleName = XMLTools.classNameFromElementName(xmlTag); entry = new
 		 * TranslationEntry(defaultPackageName, classSimpleName, xmlTag); if (entry.empty) { if
 		 * (inheritedTranslationScopes != null) { // recurse through inherited, continuing to seek a
 		 * translation for (TranslationScope inherited : inheritedTranslationScopes) { entry =
 		 * inherited.xmlTagToTranslationEntry(xmlTag); if (entry != null) { // got one from an inherited
 		 * TranslationScope // register translation for the inherited entry in this
 		 * entriesByTag.put(xmlTag, entry); entriesByClassSimpleName.put(classSimpleName, entry); break;
 		 * } } } } } else { // empty entry construction added by andruid 11/11/07 entry = new
 		 * TranslationEntry(xmlTag); // new empty entry } } return entry;
 		 */
 	}
 
 	/**
 	 * Get the Class object associated with this tag, if there is one. Unlike xmlTagToClass, this call
 	 * will not generate a new blank NameEntry.
 	 * 
 	 * @param tag
 	 * @return
 	 */
 	public Class<? extends ElementState> getClassByTag(String tag)
 	{
 		ClassDescriptor entry = getClassDescriptorByTag(tag);
 
 		return (entry == null) ? null : entry.getDescribedClass();
 	}
 
 	public ClassDescriptor getClassDescriptorByTag(String tag)
 	{
 		return entriesByTag.get(tag);
 	}
 
 	/**
 	 * Get the Class object associated with the provided class name, if there is one. Unlike
 	 * xmlTagToClass, this call will not generate a new blank NameEntry.
 	 * 
 	 * @param classSimpleName
 	 *          Simple name of the class (no package).
 	 * @return
 	 */
 	public Class<? extends ElementState> getClassBySimpleName(String classSimpleName)
 	{
 		ClassDescriptor entry = entriesByClassSimpleName.get(classSimpleName);
 
 		return (entry == null) ? null : entry.getDescribedClass();
 	}
 
 	public Class<? extends ElementState> getClassByName(String className)
 	{
 		ClassDescriptor entry = entriesByClassName.get(className);
 
 		return (entry == null) ? null : entry.getDescribedClass();
 	}
 
 	public ArrayList<Class<? extends ElementState>> getAllClasses()
 	{
 		ArrayList<Class<? extends ElementState>> classes = new ArrayList<Class<? extends ElementState>>();
 		Collection<ClassDescriptor> classDescriptors = this.getClassDescriptors();
 
 		for (TranslationScope translationScope : allTranslationScopes.values())
 		{
 			for (ClassDescriptor<?, ?> classDescriptor : translationScope.entriesByClassSimpleName
 					.values())
 			{
 				classes.add(classDescriptor.getDescribedClass());
 			}
 		}
 		return classes;
 	}
 
 	/**
 	 * Use this TranslationScope to lookup a class that has the same simple name as the argument
 	 * passed in here. It may have a different full name, that is, a different package, which could be
 	 * quite convenient for overriding with subclasses.
 	 * 
 	 * @param thatClass
 	 * @return
 	 */
 	public Class<? extends ElementState> getClassBySimpleNameOfClass(
 			Class<? extends ElementState> thatClass)
 	{
 		return getClassBySimpleName(classSimpleName(thatClass));
 	}
 
 	/**
 	 * Lookup the tag for the class in question, using this.
 	 * 
 	 * @param thatClass
 	 * @return
 	 */
 	public String getTag(Class<? extends ElementState> thatClass)
 	{
 		return getTagBySimpleName(classSimpleName(thatClass));
 	}
 
 	public String getTagBySimpleName(String simpleName)
 	{
 		ClassDescriptor entry = entriesByClassSimpleName.get(simpleName);
 
 		return (entry == null) ? null : entry.getTagName();
 	}
 
 	/**
 	 * Derive the XML tag from the Class object, using camel case conversion, or the @xml_tag
 	 * annotation that may be present in a class declaration.
 	 * 
 	 * @param thatClass
 	 * @return
 	 */
 	private static String determineXMLTag(Class<? extends ElementState> thatClass)
 	{
 		Annotation[] annotations = thatClass.getDeclaredAnnotations();
 		for (Annotation annotation : annotations)
 		{
 			if (annotation.annotationType().equals(xml_tag.class))
 			{
 				return xml_tag.class.cast(annotation).value();
 			}
 		}
 		return XMLTools.getXmlTagName(thatClass.getSimpleName(), "State");
 	}
 
 	private String	toStringCache;
 
 	public String toString()
 	{
 		if (toStringCache == null)
 		{
 			toStringCache = "TranslationScope[" + name + "]";
 		}
 		return toStringCache;
 	}
 
 	/**
 	 * Find the TranslationScope called <code>name</code>, if there is one.
 	 * 
 	 * @param name
 	 * @return
 	 */
 	public static TranslationScope lookup(String name)
 	{
 		return (TranslationScope) allTranslationScopes.get(name);
 	}
 
 	public static TranslationScope get(String name)
 	{
 		return lookup(name);
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one.
 	 * 
 	 * @param name
 	 *          the name of the TranslationScope
 	 * @param translations
 	 *          a set of Classes to be used as a part of this TranslationScope
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name, Class... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, translations);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Inherit from the previous
 	 * TranslationScope, by including all mappings from there.
 	 * 
 	 * If new translations are provided when the TranslationScope already exists in the static scope
 	 * map, they are ignored.
 	 * 
 	 * @param name
 	 * @param inheritedTranslations
 	 * @param translations
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name, TranslationScope inheritedTranslations,
 			Class[]... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, inheritedTranslations, translations);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Inherit from the previous
 	 * TranslationScope, by including all mappings from there.
 	 * 
 	 * If new translations are provided when the TranslationScope already exists in the static scope
 	 * map, they are ignored.
 	 * 
 	 * @param name
 	 * @param inheritedTranslations
 	 * @param translations
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name, TranslationScope inheritedTranslations,
 			Class... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, inheritedTranslations, translations);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on a previous
 	 * TranslationScope, by including all mappings from there. Add just a single new class.
 	 * 
 	 * @param name
 	 * @param inheritedTranslations
 	 * @param translation
 	 * @return
 	 */
 	public static TranslationScope get(String name, TranslationScope inheritedTranslations,
 			Class<? extends ElementState> translation)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, inheritedTranslations, translation);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Add just a single new
 	 * class.
 	 * 
 	 * @param name
 	 * @param translation
 	 * @return
 	 */
 	public static TranslationScope get(String name, Class<? extends ElementState> translation)
 	{
 		return get(name, null, translation);
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on the previous
 	 * TranslationScope, by including all mappings from there.
 	 * 
 	 * @param name
 	 *          the name of the TranslationScope to acquire.
 	 * @param translations
 	 *          an array of translations to add to the scope.
 	 * @param inheritedTranslations
 	 *          a list of previous translation scopes to build upon.
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name, TranslationScope[] inheritedTranslationsSet,
 			Class... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, inheritedTranslationsSet, translations);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on the previous
 	 * TranslationScope, by including all mappings from there.
 	 * 
 	 * @param name
 	 *          the name of the TranslationScope to acquire.
 	 * @param translations
 	 *          an array of translations to add to the scope.
 	 * @param inheritedTranslations
 	 *          a list of previous translation scopes to build upon.
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name, TranslationScope[] inheritedTranslationsSet,
 			Class[]... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, inheritedTranslationsSet, translations);
 			}
 		}
 		return result;
 	}
 
 	public static TranslationScope get(String name, TranslationScope inheritedTranslations0,
 			TranslationScope inheritedTranslations1, Class... translations)
 	{
 		TranslationScope[] inheritedArray = new TranslationScope[2];
 		inheritedArray[0] = inheritedTranslations0;
 		inheritedArray[1] = inheritedTranslations1;
 		return get(name, inheritedArray, translations);
 	}
 
 	public static TranslationScope get(String name, NameSpaceDecl[] nameSpaceDecls,
 			Class... translations)
 	{
 		return get(name, nameSpaceDecls, null, translations);
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on a set of
 	 * inherited TranslationScopes, by including all mappings from them.
 	 * 
 	 * @param name
 	 * @param nameSpaceDecls
 	 *          Array of ElementState class + URI key map entries for handling XML Namespaces.
 	 * @param inheritedTranslationsSet
 	 * @param translations
 	 * @param defaultPackageName
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name, NameSpaceDecl[] nameSpaceDecls,
 			TranslationScope[] inheritedTranslationsSet, Class... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, nameSpaceDecls, inheritedTranslationsSet,
 							translations);
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on a set of
 	 * inherited TranslationScopes, by including all mappings from them.
 	 * 
 	 * @param name
 	 * @param inheritedTranslations
 	 * @return
 	 */
 	public static TranslationScope get(String name, TranslationScope... inheritedTranslations)
 	{
 		return get(name, inheritedTranslations, null);
 	}
 
 	/**
 	 * Find an existing TranslationScope by this name, or create a new one. Build on a set of
 	 * inherited TranslationScopes, by including all mappings from them.
 	 * 
 	 * @param name
 	 * @param inheritedTranslationsSet
 	 * @param translations
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	public static TranslationScope get(String name,
 			Collection<TranslationScope> inheritedTranslationsSet, Class... translations)
 	{
 		TranslationScope result = lookup(name);
 		if (result == null)
 		{
 			synchronized (name)
 			{
 				result = lookup(name);
 				if (result == null)
 					result = new TranslationScope(name, inheritedTranslationsSet, translations);
 			}
 		}
 		return result;
 	}
 
 	protected HashMap<String, ClassDescriptor> entriesByClassSimpleName()
 	{
 		return entriesByClassSimpleName;
 	}
 
 	public HashSet<String> addClassNamesToHashSet(HashSet<String> hashSet)
 	{
 		if (inheritedTranslationScopes != null)
 		{
 			for (TranslationScope inheritedTScope : inheritedTranslationScopes)
 			{
 				inheritedTScope.generateImports(hashSet);
 			}
 		}
 		this.generateImports(hashSet);
 		return hashSet;
 	}
 
 	protected void generateImports(HashSet<String> hashSet)
 	{
 		for (String className : entriesByClassName.keySet())
 		{
 			hashSet.add(className);
 		}
 	}
 
 	private Collection<ClassDescriptor>	classDescriptors;
 
 	// FIXME -- implement this!
 	public Collection<ClassDescriptor> getClassDescriptors()
 	{
 		Collection<ClassDescriptor> result = classDescriptors;
 		if (result == null)
 		{
 			result = entriesByClassSimpleName.values();
 			this.classDescriptors = result;
 		}
 		return result;
 	}
 
 	/**
 	 * Get the Scalar Type corresponding to the Class.
 	 * 
 	 * @param thatClass
 	 * @return Type associated with thatClass
 	 */
 	<U> ScalarType<U> getType(Class<U> thatClass)
 	{
 		return TypeRegistry.getType(thatClass);
 	}
 
 	/**
 	 * Lookup a NameSpace ElementState subclass, with a URN as the key.
 	 * 
 	 * @param urn
 	 * @return
 	 */
 	public Class<? extends ElementState> lookupNameSpaceByURN(String urn)
 	{
 		return nameSpaceClassesByURN.get(urn);
 	}
 
 	/**
 	 * 
 	 * @return The unique name of this.
 	 */
 	public String getName()
 	{
 		return name;
 	}
 
 	public static void main(String[] s)
 	{
 
 	}
 
 	public static final String	BASIC_TRANSLATIONS	= "basic_translations";
 
 	private void addTranslationScope(String name)
 	{
 		allTranslationScopes.put(name, this);
 	}
 
 	/**
 	 * Translate a file XML to a strongly typed tree of XML objects.
 	 * 
 	 * Use SAX or DOM parsing depending on the value of useDOMForTranslateTo.
 	 * 
 	 * @param fileName
 	 *          the name of the XML file that needs to be translated.
 	 * @return Strongly typed tree of ElementState objects.
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState deserialize(String fileName)
 			throws SIMPLTranslationException
 	{
 		File xmlFile = new File(fileName);
 		if (!xmlFile.exists() && !xmlFile.canRead())
 			throw new SIMPLTranslationException("Can't access " + xmlFile.getAbsolutePath(), FILE_NOT_FOUND);
 	
 		return deserialize(xmlFile);
 	}
 
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param charSequence
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState deserializeCharSequence(CharSequence charSequence) throws SIMPLTranslationException
 	{
 		ElementStateSAXHandler saxHandler = new ElementStateSAXHandler(this);
 		return saxHandler.parse(charSequence);
 	}
 
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param purl
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState deserialize(ParsedURL purl)
 			throws SIMPLTranslationException
 	{
 		if (purl == null)
 			throw new SIMPLTranslationException("Null PURL", NULL_PURL);
 	
 		if (!purl.isNotFileOrExists())
 			throw new SIMPLTranslationException("Can't find " + purl.toString(), FILE_NOT_FOUND);
 	
 		ElementStateSAXHandler saxHandler = new ElementStateSAXHandler(this);
 		return saxHandler.parse(purl);
 	}
 
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param inputStream
 	 *          An InputStream to the XML that needs to be translated.
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState deserialize(InputStream inputStream) throws SIMPLTranslationException
 	{
 		ElementStateSAXHandler saxHandler = new ElementStateSAXHandler(this);
 		return saxHandler.parse(inputStream);
 	}
 
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param url
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState deserialize(URL url)
 			throws SIMPLTranslationException
 	{
 		ElementStateSAXHandler saxHandler = new ElementStateSAXHandler(this);
 		return saxHandler.parse(url);
 	}
 
 	/**
 	 * Use the (faster!) SAX parser to form a strongly typed tree of ElementState objects from XML.
 	 * 
 	 * @param file
 	 * @return
 	 * @throws SIMPLTranslationException
 	 */
 	public ElementState deserialize(File file)
 			throws SIMPLTranslationException
 	{
 		ElementStateSAXHandler saxHandler = new ElementStateSAXHandler(this);
 		return saxHandler.parse(file);
 	}
 
 	public static TranslationScope getBasicTranslations()
 	{
 		return get(BASIC_TRANSLATIONS, TranslationScope.class, FieldDescriptor.class,
 				ClassDescriptor.class);
 	}
 
 	public static TranslationScope augmentTranslationScope(TranslationScope translationScope)
 	{
 		ArrayList<Class<? extends ElementState>> allClasses = translationScope.getAllClasses();
 		Collection<Class<? extends ElementState>> augmentedClasses = augmentTranslationScope(allClasses)
 				.values();
 
 		Class<? extends ElementState>[] augmentedClassesArray = (Class<? extends ElementState>[]) augmentedClasses
 				.toArray(new Class<?>[augmentedClasses.size()]);
 
 		return new TranslationScope(translationScope.getName(), augmentedClassesArray);
 	}
 
 	private static HashMap<String, Class<? extends ElementState>> augmentTranslationScope(
 			ArrayList<Class<? extends ElementState>> allClasses)
 	{
 		HashMap<String, Class<? extends ElementState>> augmentedClasses = new HashMap<String, Class<? extends ElementState>>();
 		for (Class<? extends ElementState> thatClass : allClasses)
 		{
 			augmentTranslationScope(thatClass, augmentedClasses);
 		}
 		return augmentedClasses;
 	}
 
 	private static void augmentTranslationScope(Class<? extends ElementState> thatClass,
 			HashMap<String, Class<? extends ElementState>> augmentedClasses)
 	{
 		if(augmentedClasses.put(thatClass.getSimpleName(), thatClass) != null) return;		
 		
 		if (thatClass.getSuperclass() != ElementState.class)
 		{
 			augmentTranslationScope(thatClass.getSuperclass().asSubclass(ElementState.class),
 					augmentedClasses);
 		}
 
 		ClassDescriptor<?, ?> thatClassDescriptor = ClassDescriptor.getClassDescriptor(thatClass);
 
 		HashMapArrayList<String, ? extends FieldDescriptor> fieldDescriptors = thatClassDescriptor
 				.getFieldDescriptorsByFieldName();
 
 		if (fieldDescriptors.size() > 0)
 		{
 			thatClassDescriptor.resolveUnresolvedScopeAnnotationFDs();
 
 			for (FieldDescriptor fieldDescriptor : fieldDescriptors)
 			{
 				if (fieldDescriptor.isNested())
 				{
 					augmentTranslationScope(fieldDescriptor.getFieldType().asSubclass(ElementState.class),
 							augmentedClasses);
 				}
 				else
 				{
					if(fieldDescriptor.isCollection())
 					{
 						Class<?>[] genericClasses = XMLTools.getGenericParameters(fieldDescriptor.getField());
 						
 						for(Class<?> genericClass : genericClasses)
 						{
							if(genericClass.isAssignableFrom(ElementState.class))
 							{
 								augmentTranslationScope(genericClass.asSubclass(ElementState.class), augmentedClasses);
 							}
 						}
 					}
 					else
 					if (fieldDescriptor.isPolymorphic())
 					{
 						HashMapArrayList<String, ? extends ClassDescriptor> tagClassDescriptors = fieldDescriptor
 								.getTagClassDescriptors();
 
 						if (tagClassDescriptors != null)
 						{
 							for (ClassDescriptor<?, ?> classDescriptor : tagClassDescriptors)
 							{
 								augmentTranslationScope(classDescriptor.getDescribedClass(), augmentedClasses);
 							}
 						}
 					}
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * @return the performFilters
 	 */
 	public boolean isPerformFilters()
 	{
 		return performFilters;
 	}
 
 	/**
 	 * @param performFilters the performFilters to set
 	 */
 	public void setPerformFilters(boolean performFilters)
 	{
 		this.performFilters = performFilters;
 	}
 }
