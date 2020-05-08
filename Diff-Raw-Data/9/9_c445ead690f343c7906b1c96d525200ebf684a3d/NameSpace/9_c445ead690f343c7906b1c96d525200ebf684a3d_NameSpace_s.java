 package ecologylab.xml;
 
 import java.util.HashMap;
 
 /**
  * This class contains the package information for the classes that are converted
  * from Java to XML and back. This is done by maintaining a Hashtable containing
  * the name of the class as the key and its package as the value. If no entry is 
  * present in the hashtable, then the default package is returned. This is used when
  * an XML is converted back to its Java State-Object
  */
 public class NameSpace extends IO 
 {
    protected String		name;
    /**
 	* The default package. If an entry for a class is not found in the hashtable,
 	* this package name is returned.
 	*/
    private String		defaultPackageName = "cf.state";
    
    /**
 	* This boolean controls whether package names are added to the class names
 	* in the corresponding XML file. If its false, the package names are not added, otherwise
 	* every class name is prepended by its package name.
 	*/
    private boolean emitPackageNames = false;
    
    private HashMap entriesByClassName	= new HashMap();
    private HashMap entriesByTag			= new HashMap();
 //   private HashMap entriesByClassName	= new HashMap();
    
    /**
 	* The hashtable containing the class name to package name mapping.
 	* the name of the class is the key and the name of package is the value.
 	*/
    private final HashMap classPackageMappings = new HashMap();
    
    public NameSpace(String name)
    {
 	  // !!! these lines need to be moved to the studies package !!!
 //	  addTranslation("studies", "SubjectState");
 //	  addTranslation("studies", "SubjectSet");
 	  this.name	= name;
    }
    
    /**
 	* Add a translation table entry for an ElementState derived sub-class.
 	* Assumes that the xmlTag can be derived automatically from the className,
 	* by translating case-based separators to "_"-based separators.
 	* 
 	* @param packageName	Package that the class lives in.
 	* @param className		Name of the class.
 	*/
    public void addTranslation(String packageName, String className)
    {
    	  new NameEntry(packageName, className);
    }
    /**
 	* Add a translation table entry for an ElementState derived sub-class.
 	* Use this signature when the xmlTag cannot be generated automatically from the className.
 	* 
 	* @param packageName	Package that the class lives in.
 	* @param className		Name of the class.
 	* @param xmlTag		XML tag that the class maps to.
 	*/
    public void addTranslation(String packageName, String className,
 							  String xmlTag)
    {
 //	  debugA("addTranslation: "+ className " : " + packageName);
 	  classPackageMappings.put(className, packageName+".");
 	  new NameEntry(packageName, className);
    }
    /**
 	* returns a package name for a corresponding class name
 	* @param className	the class for which the package names needs to be found out.
 	* @return	package name of the given class name
 	*/
    public String getPackageName(String className)
    {
 	  String packageName = (String) classPackageMappings.get(className);
 	  
 	  return (packageName != null) ? packageName : defaultPackageName;
    }
    /**
 	* Set the default package name for XML tag to ElementState sub-class translations.
 	* 
 	* @param packageName	The new default package name.
 	*/
    public void setDefaultPackageName(String packageName)
    {
	  defaultPackageName	= packageName + ".";
    }
    /**
 	* creates a <code>Class</code> object from a given element name (aka tag) in the xml.
 	* Also keeps it in the hashtable, so that when requested for the same class again
 	* it doesnt have to create one.
 	* @param xmlTag	name of the state class along with its package name
 	* @return 						a <code>Class</code> object for the given state class
 	*/
    public Class xmlTagToClass(String xmlTag)
    {
 	  NameEntry entry		= (NameEntry) entriesByTag.get(xmlTag);
 
 	  if (entry == null)
 	  {
 		 String className	= XmlTools.classNameFromElementName(xmlTag);
 		 String packageName = defaultPackageName;
 		 entry				= new NameEntry(packageName, className, xmlTag);
 	  }
 	  else if (entry.empty)
 	  {
 //		 debug("using memorized no mapping for " + xmlTag);
 		 return null;
 	  }
 	  return entry.classObj;
    }
 /**
  * Find an appropriate XML tag name, based on the type of the object passed.
  * 
  * @param object	Object whose type becomes the basis for the tag name we derive.
  */   
    public String objectToXmlTag(Object object)
    {
 	  return classToXmlTag(object.getClass());
    }
    /**
     * Find an appropriate XML tag name, based on the class object passed.
     * 
     * @param classObj	The type which becomes the basis for the tag name we derive.
     */   
    public String classToXmlTag(Class classObj)
    {
 	  String className	= classObj.getName();
 	  NameEntry entry	= (NameEntry) entriesByClassName.get(className);
 	  if (entry == null)
 	  {
 	  	 synchronized (this) 
 	  	 {
 	  	 	 entry	= (NameEntry) entriesByClassName.get(className);
 	  	 	 if (entry == null)
 	  	 	 {
 				 String packageName = classObj.getPackage().getName();
 				 int index			= className.lastIndexOf('.') + 1;
 				 className			= className.substring(index);
 				 entry				= new NameEntry(packageName, className);
 	  	 	 }
 	  	 }
 	  }
 	  return entry.getTag();
    }
    
    /**
 	* @return	true	If package names should be emitted as part of tags 
 	*  while translating to XML.
 	*					This corresponds to quite verbose XML.
 	*/
    public boolean emitPackageNames()
    {
 	  return emitPackageNames;
    }
 
    public class NameEntry extends IO
    {
 	  public final String		packageName;
 	  public final String		className;
 	  public final String		tag;
 
 	  public final String		dottedPackageName;
 	  public final String		tagWithPackage;
 	  public final Class		classObj;
 
 	  boolean					empty;
 	  
 /**
  * Create the entry by package name and class name.
  */
 	  public NameEntry(String packageName, String className)
 	  {
 		 this(packageName, className,
 			  XmlTools.getXmlTagName(className, "State", false));
 	  }
 	  public NameEntry(String packageName, String className, 
 					   String tag)
 	  {
 	  	 String wholeClassName	= packageName + "." + className;
 		 this.packageName		= packageName;
 		 this.className			= wholeClassName;
 		 this.tag				= tag;
 		 String dottedPackageName		= packageName + ".";
 		 this.dottedPackageName	= dottedPackageName;
 		 this.tagWithPackage	= dottedPackageName + tag;
 		 Class classObj			= null;
 		 try
 		 {  
 			classObj			= Class.forName(wholeClassName);
 		 } catch (ClassNotFoundException e)
 		 {
 			// maybe we need to use State
 			try
 			{
 //			   debug("trying " + wholeClassName+"State");
 			   classObj			= Class.forName(wholeClassName+"State");
 			} catch (ClassNotFoundException e2)
 			{
 			   debug("WARNING: can't find class object, create empty entry.");
 
 //			   this.classObj			= classObj;
 			   this.empty				= true;
 //			   return;
 			}
 		 }
 		 this.classObj			= classObj;
 
 		 entriesByTag.put(tag, this);
 		 entriesByClassName.put(wholeClassName, this);
 		 if (wholeClassName.endsWith("State"))
 		 {
 			int beforeState		= wholeClassName.length() - 5;
 			String wholeClassNameNoState = 
 			   wholeClassName.substring(0, beforeState);
 //			debug("create entry including " + wholeClassNameNoState);
 			entriesByClassName.put(wholeClassNameNoState, this);
 		 }
 //		 else
 //			debug("create entry");
 	  }
 	  public String getTag()
 	  {
 		 return emitPackageNames() ? tagWithPackage : tag;
 	  }
 	  public String toString()
 	  {
 		 StringBuffer buffy = new StringBuffer(50);
 		 buffy.append("NameEntry[").append(className).
 			append(" <").append(tag).append('>');
 		 if (classObj != null)
 			buffy.append(' ').append(classObj);
 		 buffy.append(']');
 		 return XmlTools.toString(buffy);
 	  }
    }
    public String toString()
    {
       return "NameSpace";
    }
 }
