 package ecologylab.xml;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.StringBufferInputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 
 import ecologylab.xml.ElementState.xml_format;
 import ecologylab.xml.ElementState.xml_other_tags;
 import ecologylab.xml.ElementState.xml_tag;
 import ecologylab.xml.types.scalar.ScalarType;
 import ecologylab.xml.types.scalar.TypeRegistry;
 
 /**
  * Static helper methods that are used during the translation of java objects
  * to XML and back. The XML files can also be compressed
  * by using the compression variable. For compression to work, the developer should
  * provide a abbreviation table in the format listed in the code.   
  * 
  * @author      Andruid Kerne
  * @author      Madhur Khandelwal
  * @version     0.5
  */
 public class XMLTools extends TypeRegistry
 implements CharacterConstants, SpecialCharacterEntities
 {
 	private static final int DEFAULT_TAG_LENGTH 		= 15;
 
 	static HashMap<String, String>	entityTable		= new HashMap<String, String>();
 
 	static final String SPECIAL_SPELLINGS[]=
 	{
 		"nbsp", "iexcl", "cent", "pound", "curren", "yen", "brvbar",
 		"sect", "uml", "copy", "ordf", "laquo", "not", "shy", "reg",
 		"macr", "deg", "plusmn", "sup2", "sup3", "acute", "micro", "para",
 		"middot", "ccedil", "sup1", "ordm", "raquo", "frac14", "frac12",
 		"frac34", "iquest", "Agrave", "Aacute", "Acirc", "Atilde", "Auml",
 		"Aring", "AElig", "Ccedil", "Egrave", "Eacute", "Ecirc", "Euml",
 		"Igrave", "Iacute", "Icirc", "Iuml", "ETH", "Ntilde", "Ograve",
 		"Oacute", "Ocirc", "Otilde", "Ouml", "times", "Oslash", "Ugrave",
 		"Uacute", "Ucirc", "Uuml", "Yacute", "THORN", "szlig", "agrave",
 		"aacute", "acirc", "atilde", "auml", "aring", "aelig", "ccedil",
 		"egrave", "eacute", "ecirc", "euml", "igrave", "iacute", "icirc",
 		"iuml", "eth", "ntilde", "ograve", "oacute", "ocirc", "otilde",
 		"ouml", "divide", "oslash", "ugrave", "uacute", "ucirc", "uuml",
 		"yacute", "thorn" 
 	};
 
 	static
 	{
 		// special spellings
 		for (char i = 0; i < SPECIAL_SPELLINGS.length; i++)
 			entityTable.put(SPECIAL_SPELLINGS[i], Character.toString((char) (i + 160)));
 
 		entityTable.put("#x00bb", Character.toString((char) 187));	// a hack for weird hex references to &raquo;		&#187;		right-pointing double angle quotation mark = right pointing guillemet
 
 		// even though we fill the table from 0-255, actually
 		// 0-8 are illegal. 9,10 (decimal) are legal. 11-31 are illegal. 31-127 are legal.
 		// syntax such as &#38;
 		for (char c = 0; c < 255; c++)
 			putNumberedEntityInTable(c);
 
 		for (char c = 913; c < 982; c++)	// greek letters
 			putNumberedEntityInTable(c);
 
 		// the rest of the special chars:
 		for (int i= 0; i < SPECIAL_CHARACTER_ENTITIES.length; i++)
 			putNumberedEntityInTable(SPECIAL_CHARACTER_ENTITIES[i]);
 		
 		putEntityInTable("#039", '\'');
 		putEntityInTable("#x0027", '\'');
 
 		putEntityInTable("#x22", '\"');
 		putEntityInTable("#x27", '\'');
 		// magic chars from the NY TIMES
 		// &#8217; is really ' (ascii 39)
 
 		putEntityInTable("#8217", '\'');
 		// &#8220; is really  = &#147;
 		putEntityInTable("#8220", '');
 		// &#8221; is really  = &#148;
 		putEntityInTable("#8221", '');
 		// &#8212; is really  = &#151; -- em dash
 		putEntityInTable("#8212", '');
 		putEntityInTable("#151", '');
 
 
 		// defined in the XML 1.0 spec: "predefined entities"
 		putEntityInTable("amp", '&');
 		putEntityInTable("AMP", '&');
 		putEntityInTable("quot", '"');
 		putEntityInTable("lt", '<');
 		putEntityInTable("gt", '>');
 		putEntityInTable("apos", '\'');
 		putEntityInTable("nbsp", ' ');	//TODO -- start handling nbsp as a char and in TextTokn
 
 		putEntityInTable("bull", BULL);
 		putEntityInTable("hellip", HELLIP);
 		putEntityInTable("prime", PRIME);
 		putEntityInTable("oline", OLINE);
 		putEntityInTable("frasl", FRASL);
 		putEntityInTable("weierp", WEIERP);
 		putEntityInTable("image", IMAGE);
 		putEntityInTable("real", REAL);
 		putEntityInTable("trade", TRADE);
 		putEntityInTable("alefsym", ALEFSYM);
 		putEntityInTable("larr", LARR);
 		putEntityInTable("uarr", UARR);
 		putEntityInTable("rarr", RARR);
 		putEntityInTable("darr", DARR);
 		putEntityInTable("harr", HARR);
 		putEntityInTable("crarr", CRARR);
 
 		putEntityInTable("forall",FORALL);
 		putEntityInTable("part", PART);
 		putEntityInTable("exist", EXIST);
 		putEntityInTable("empty", EMPTY);
 		putEntityInTable("nabla", NABLA);
 		putEntityInTable("isin", ISIN);
 		putEntityInTable("notin", NOTIN);
 		putEntityInTable("ni", NI);
 		putEntityInTable("prod", PROD);
 		putEntityInTable("sum", SUM);
 		putEntityInTable("minus", MINUS);
 		putEntityInTable("lowast", LOWAST);
 		putEntityInTable("radic", RADIC);
 		putEntityInTable("prop", PROP);
 		putEntityInTable("infin", INFIN);
 		putEntityInTable("ang", ANG);
 		putEntityInTable("and", AND);
 		putEntityInTable("or", OR);
 		putEntityInTable("cap", CAP);
 		putEntityInTable("cup", CUP);
 		putEntityInTable("int", INT);
 		putEntityInTable("there4", THERE4);
 		putEntityInTable("sim", SIM);
 		putEntityInTable("cong", CONG);
 		putEntityInTable("asymp", ASYMP);
 		putEntityInTable("ne", NE);
 		putEntityInTable("equiv", EQUIV);
 		putEntityInTable("le", LE);
 		putEntityInTable("ge", GE);
 		putEntityInTable("sub", SUB);
 		putEntityInTable("sup", SUP);
 		putEntityInTable("nsub", NSUB);
 		putEntityInTable("sube", SUBE);
 		putEntityInTable("supe", SUPE);
 		putEntityInTable("oplus", OPLUS);
 		putEntityInTable("otimes", OTIMES);
 		putEntityInTable("perp", PERP);
 		putEntityInTable("sdot", SDOT);
 		putEntityInTable("lceil", LCEIL);
 		putEntityInTable("rceil", RCEIL);
 		putEntityInTable("lfloor", LFLOOR);
 		putEntityInTable("rfloor", RFLOOR);
 		putEntityInTable("lang", LANG);
 		putEntityInTable("rang", RANG);
 		putEntityInTable("loz", LOZ);
 		putEntityInTable("spades", SPADES);
 		putEntityInTable("clubs", CLUBS);
 		putEntityInTable("hearts", HEARTS);
 		putEntityInTable("diams", DIAMS);
 		putEntityInTable("oelig", OELIG);
 		putEntityInTable("scaron", SCARON);
 		putEntityInTable("yuml", YUML);
 		putEntityInTable("circ", CIRC);
 		putEntityInTable("tilde", TILDE);
 		putEntityInTable("ensp", ENSP);
 		putEntityInTable("emsp", EMSP);
 		putEntityInTable("thinsp", THINSP);
 		putEntityInTable("zwnj", ZWNJ);
 		putEntityInTable("zwj", ZWJ);
 		putEntityInTable("lrm", LRM);
 		putEntityInTable("rlm", RLM);
 		putEntityInTable("ndash", NDASH);
 		putEntityInTable("mdash", MDASH);
 		putEntityInTable("lsquo", LSQUO);
 		putEntityInTable("rsquo", RSQUO);
 		putEntityInTable("sbquo", SBQUO);
 		putEntityInTable("ldquo", LDQUO);
 		putEntityInTable("rdquo", RDQUO);
 		putEntityInTable("bdquo", BDQUO);
 		putEntityInTable("dagger", DAGGER);
 		putEntityInTable("permil", PERMIL);
 		putEntityInTable("lsaquo", LSAQUO);
 		putEntityInTable("rsaquo", RSAQUO);
 		putEntityInTable("euro", EURO);
 		putEntityInTable("dblrarr", DBLRARR);
 		putEntityInTable("imdbDblQt", ANOTHER_DBL_QUOTE);
 		putEntityInTable("imdbQt", ANOTHER_QUOTE);
 	}
 
 	/**
 	 * Generate lookup from numbered XML entity character reference (e.g., &#123;) to the actual Character.
 	 * 
 	 * @param i
 	 */
 	private static void putNumberedEntityInTable(char c)
 	{
 		String entityString = "#"+ (int) c;
 		putEntityInTable(entityString, c);
 	}
 
 	private static void putEntityInTable(String name, char c)
 	{
 		entityTable.put(name, Character.toString(c));
 	}
 	
 	private static final String		BOGUS	= "BOGUS";
 
 	/**
 	 * This method generates a name for the xml tag given a reference type java object.
 	 * This is used during the translation of Java to xml. 
 	 * Part of this is to translate mixed case class name word separation into
 	 * "_" word separtion.
 	 * 
 	 * @param obj			a java reference type object 
 	 * @param suffix		string to remove from class name, null if nothing to be removed
 	 * @return				name of the xml tag (element)
 	 */	
 	public static String xmlTagFromObject(Object obj, 
 			String suffix)
 	{
 		return getXmlTagName(obj.getClass(), suffix);
 	}
 	/**
 	 * This method generates a name for the xml tag given a reference type java object.
 	 * This is used during the translation of Java to xml. 
 	 * Part of this is to translate mixed case class name word separation into
 	 * "_" word separtion.
 	 * 
 	 * @param thatClass		Class object to translate.
 	 * @param suffix		string to remove from class name, null if nothing to be removed
 	 * @return				name of the xml tag (element)
 	 */	
 	public static String getXmlTagName(Class<?> thatClass, String suffix)
 	{
 		final ElementState.xml_tag tagAnnotation 	= thatClass.getAnnotation(xml_tag.class);
 		
    	String result 			= getXmlTagAnnotationIfPresent(tagAnnotation);
 		if (result == null)
 		{
 			result						= getXmlTagName(getClassName(thatClass), suffix);
 		}
 		return result;
 	}
 
 	/**
 	 * This method generates a name for the xml tag given a reference type java object.
 	 * This is used during the translation of Java to xml. 
 	 * Part of this is to translate mixed case class name word separation into
 	 * "_" word separtion.
 	 * 
 	 * @param describedClass		Class object to translate.
 	 * @param suffix		string to remove from class name, null if nothing to be removed
 	 * @return				name of the xml tag (element)
 	 */	
 	public static String getXmlTagName(Field thatField)
 	{
 		final ElementState.xml_tag tagAnnotation 	= thatField.getAnnotation(xml_tag.class);
 		
    	String result 	= getXmlTagAnnotationIfPresent(tagAnnotation);
 		if (result == null)
 		{
 			result				= getXmlTagName(thatField.getName(), null);
 		}
 		return result;
 	}
 
 	public static String getXmlTagAnnotationIfPresent(final ElementState.xml_tag tagAnnotation)
 	{
 		String result			= null;
 		if (tagAnnotation != null)
 		{
 			String thatTag	= tagAnnotation.value();
 			if ((thatTag != null) && (thatTag.length() > 0))
 				result			  = thatTag;
 		}
 		return result;
 	}
 	/**
 	 * This method generates a name for the xml tag given a reference type java object.
 	 * This is used during the translation of Java to xml. 
 	 * Part of this is to translate mixed case class name word separation into
 	 * "_" word separtion.
 	 * 
 	 * @param className		class name of a java reference type object 
 	 * @param suffix		string to remove from class name, null if nothing to be removed
 	 * @return				name of the xml tag (element)
 	 */	
 	public static String getXmlTagName(String className, String suffix)
 	{
 		if ((suffix != null) && (className.endsWith(suffix)))
 		{
 			int suffixPosition	= className.lastIndexOf(suffix);
 			className			= className.substring(0, suffixPosition);
 		}
 
 		StringBuilder result = new StringBuilder(DEFAULT_TAG_LENGTH);
 
 		// translate mixed case class name word separation into
 		// _ word separtion
 		int classNameLength	= className.length();
 		for (int i=0; i<classNameLength; i++)
 		{
 			char	c	= className.charAt(i);
 
 			if ((c >= 'A') && (c <= 'Z') )
 			{
 				char lc = Character.toLowerCase(c);
 				if (i > 0)
 					result.append('_');
 				result.append(lc);
 			}
 			else
 				result.append(c);
 		}
 		return result.toString();
 	}
 
 	/**
 	 * This method generates a name for an ElementState object, given an XML element name. 
 	 * It is used during translation of XML to Java. Using
 	 * the returned class name, the appropriate class can be instantiated using reflection.  
 	 * @param elementName		the name of the XML element
 	 * @return				the name of the Java class corresponding to the elementName
 	 */   
 	public static String classNameFromElementName(String elementName)
 	{
 		return javaNameFromElementName(elementName, true);
 	}
 	/**
 	 * This method generates a name for an ElementState object, given an XML attribute name. 
 	 * It is used during translation of XML to Java. Using
 	 * the returned class name, the appropriate class can be instantiated using reflection.  
 	 * @param elementName	the name of the XML element attribute
 	 * @return				the name of the Java class corresponding to the elementName
 	 */   
 	public static String fieldNameFromElementName(String elementName)
 	{
 		return javaNameFromElementName(elementName, false);
 	}
 	/**
 	 * Generate the name of a Java class (capitalized) or field (starts with lower case), given
 	 * the name of an XML tag or attribute.
 	 * Used during translation of XML to Java.
 	 * 
 	 * @param elementName	the name of the XML element or tag
 	 * @param capsOn			true if the first letter of output should be capitalized.
 	 * 
 	 * @return				the name of the Java class corresponding to the elementName
 	 */
 	public static String javaNameFromElementName(String elementName, boolean capsOn)
 	{
 		StringBuilder result = new StringBuilder(DEFAULT_TAG_LENGTH);  
 
 		for (int i = 0; i < elementName.length(); i++)
 		{
 			char c = elementName.charAt(i);
 
 			if (capsOn)
 			{
 				result.append(Character.toUpperCase(c));
 				capsOn = false;
 			}
 			else
 			{
 				if(c != '_')
 					result.append(c);
 			}
 			if(c == '_')
 				capsOn = true;
 		}
 		return result.toString();
 	}
 
 	public static String fieldNameFromNodeName(String nodeName)
 	{
 		return javaNameFromElementName(nodeName, false);
 	}
 
 	/**
 	 * This method generates a name for the *setter* method for a given Java primitive type. 
 	 * For example, for the attribute <code> intensity </code>, it will generate a setter 
 	 * method named <code> setIntensity </code>. 
 	 * It is used during translation of xml to Java. Using
 	 * this method name, the appropriate field is populated.  
 	 * @param tagName			the name of the xml element or tag
 	 * @return				the name of the *setter* method corresponding to the tagName
 	 */      
 	public static String methodNameFromTagName(String tagName)
 	{
 		StringBuilder result = new StringBuilder(DEFAULT_TAG_LENGTH);  
 		result.append("set");
 
 		for(int i = 0; i < tagName.length(); i++){
 
 			char c = tagName.charAt(i);
 
 			if(i == 0 )
 				result.append(Character.toUpperCase(c));
 			else
 				result.append(c);
 
 		}
 		return result.toString();
 	}
 	/**
 	 * This method generates a field name from a reference type nested object. It just converts
 	 * the camelcase name to the lowercase. This is used while generating xml from Java.   
 	 * @param elementState	the reference type field for which a field field name needs to be generated
 	 * @return				field name for the given reference type field
 	 */
 	public static String fieldNameFromObject(ElementState elementState)
 	{
 		StringBuilder result = new StringBuilder(DEFAULT_TAG_LENGTH);  
 		String elementName = getClassName(elementState);
 
 		for(int i = 0; i < elementName.length(); i++)
 		{
 			char c = elementName.charAt(i);
 			if (i == 0)
 				c		= Character.toLowerCase(c);
 			result.append(c);
 		}
 		return result.toString();
 	}
 
 	static final HashMap<String, String>		classAbbrevNames	= new HashMap<String, String>();
 	static final HashMap<String, String>		packageNames			= new HashMap<String, String>();
 
 	/**
 	 * This method returns the abbreviated name of the class, without the package qualifier.
 	 * It also puts the name into a hashtable, so that next time the function is called for the
 	 * same class, the class name can be retrieved quickly from the hashtable. 
 	 * Used while generating xml from Java. 
 	 * @param thatClass  the <code>Class</code> type of an object
 	 * @return   		  the abbreviated name of the class - without the package qualifier
 	 */
 	public static String getClassName(Class thatClass)
 	{
 		String fullName		= thatClass.getName();
 		String abbrevName	= classAbbrevNames.get(fullName);
 		if (abbrevName == null)
 		{
 			abbrevName			= thatClass.getSimpleName();
 			synchronized (classAbbrevNames)
 			{
 				classAbbrevNames.put(fullName, abbrevName);
 			}
 		}
 		return abbrevName;
 	}
 
 	/**
 	 * This method gets the package name of a give Java class. 
 	 * It also puts the name into a hashtable, so that next time the function is called for the
 	 * same class, the package name can be retrieved quickly from the hashtable. 
 	 * Used while generating Java class from xml. 
 	 * @param	thatClass   the <code>Class</code> type of an object
 	 * @return   			the package name of the class, with an extra "." at the end.
 	 */
 	public static String getPackageName(Class thatClass)
 	{
 		String className		= thatClass.getName();
 		String packageName	= null;
 		if (packageNames.containsKey(className))
 		{
 			packageName				= packageNames.get(className);
 		}
 		else
 		{
 			if (thatClass.getPackage() != null)
 			{	   	  
 				//			  packageName	= 	className.substring(6, className.lastIndexOf("."));
 				packageName			=	thatClass.getPackage().getName() + ".";
 				synchronized (packageNames)
 				{
 					packageNames.put(className, packageName);
 				}
 			}
 		} 
 		return packageName;
 	}
 
 	/**
 	 * This method returns the abbreviated name of the class, without the package qualifier.
 	 * @param o	the object 
 	 * @return   the abbreviated name of the class - without the package qualifier.
 	 */
 	public static String getClassName(Object o)
 	{
 		return getClassName(o.getClass());
 	}
 
 	/**
 	 * This method gets the name of <code>this</code> class.
 	 * @return  the abbreviated name of this class - without the package qualifier
 	 */
 	public String getClassName()
 	{
 		return getClassName(this);
 	}
 
 	/**
 	 * This method gets the package name of a give Java class. 
 	 * Used while generating Java class from xml. 
 	 * @param o	the <code>Class</code> type of an object
 	 * @return   the package name of the class
 	 */
 	public static String getPackageName(Object o)
 	{
 		return getPackageName(o.getClass());
 	}
 
 	/**
 	 * This method gets the package name of <code>this</code>Java class. 
 	 * @return   the package name of the class
 	 */
 	public String getPackageName()
 	{
 		return getPackageName(this);
 	}
 
 	public String toString()
 	{
 		return getClassName(this);
 	}
 
 	/**
 	 * Seek an @xml_format annotaion on the Field object.
 	 * If there is one with a non-zero length, return it.
 	 * 
 	 * @param field
 	 * 
 	 * @return		An array of Strings with format info in them, or null if there wasn't one or it was empty.
 	 */
 	public static String[] getFormatAnnotation(Field field)
 	{
 		String format[]				= null;
 		ElementState.xml_format formatAnnotation 	= field.getAnnotation(ElementState.xml_format.class);
 		if (formatAnnotation != null)
 		{
 			String[] formatStrings	= formatAnnotation.value();
 			if (formatStrings.length > 0)
 				format	= formatStrings;
 		}
 		return format;
 	}
 
 	/**
 	 * Get an instance; generate an XmlTranslationException if there's a problem.
 	 * 
 	 * @param thatClass		The type of the object to translate in to.
 	 * 
 	 * @return				The resulting object.
 	 * 
 	 * @throws XMLTranslationException	If the constructor fails, or
 	 *  if that class lacks a constructor that takes no parameters.
 	 */
 	public static<T> T getInstance(Class<T> thatClass)
 	throws XMLTranslationException
 	{
 		// form the new object derived from ElementState
 		T nestedObject		= null;
 		try
 		{			  
 			nestedObject	=	thatClass.newInstance();
 		}
 		catch (Exception e)
 		{
 			throw new XMLTranslationException("Instantiation ERROR for " + thatClass +". Is there a public constructor with no arguments?", e);
 		}
 		return nestedObject;
 	}
 	public static String toString(Object o)
 	{
 		return getClassName(o);
 	}
 
 	/**
 	 * @param string
 	 * @return	The string wrapped in double quote marks.
 	 */
 	static String q(String string)
 	{
 		return "\""+ string + "\" ";
 	}
 
 	public static String nameVal(String label, String val)
 	{
 		return (val == null) ? "" : "\n " + label +"="+ q(val);
 	}
 
 	public static String nameVal(String label, URL val)
 	{
 		String valStr = (val == null) ? null : val.toString();
 		return nameVal(label, valStr);
 	}
 	public static String nameVal(String label, long val)
 	{
 		return nameVal(label, val+"");
 	}
 	public static String nameVal(String label, boolean val)
 	{
 		return nameVal(label, val+"");
 	}
 	public static String nameVal(String label, float val)
 	{
 		return nameVal(label, val+"");
 	}
 
 	/**
 	 * This method propages the values of all the primitive types from the source object
 	 * to the destination object. 
 	 * @param src	source object from the values need to be copies
 	 * @param dest	destination object to which the values need to be copied
 	 */
 	public static void propagateFields(Object src, Object dest)
 	{
 		//propagate values automatically
 		Field[] fields	= src.getClass().getFields();
 
 		for(int i = 0; i < fields.length; i++)
 		{
 			Field thatField = fields[i];
 			//we propage values only for primitive types
 			//for others we build RealObjects!
 
 			//the second condition being checked is for parent objects
 			//getFields gets the public fields in parent classes too, we dont want them
 			if((thatField.getType().isPrimitive()) && 
 					(thatField.getDeclaringClass().getName() == src.getClass().getName()))
 			{
 				try
 				{
 					String methodName = methodNameFromTagName(thatField.getName());
 
 					Class[] parameters = new Class[1]; //just 1 parameter in all the *setter* methods
 					Method attrMethod = null;		   //and thats the value of the field to be set
 
 					//this gets the type of the field, 
 					//the setter method will have the same type for its argument obviously
 					parameters[0] = thatField.getType();
 
 					attrMethod = dest.getClass().getMethod(methodName,parameters);
 
 					if(attrMethod != null)
 					{
 						Object[] args = new Object[1]; //just one argument to the method
 						args[0] = thatField.get(src);
 
 						attrMethod.invoke(dest,args);
 					}
 				} catch (Exception e)  // IllegalArgument, IllegalAccess, IllegalCast  
 				{
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Use this method to efficiently get a <code>String</code> from a
 	 * <code>StringBuilder</code> on those occassions when you plan to keep
 	 * using the <code>StringBuilder</code>, and want an efficiently made copy.
 	 * In those cases, <i>much</i> better than 
 	 * <code>new String(StringBuilder)</code>
 	 */
 	public static final String toString(StringBuilder buffer)
 	{
 		return buffer.substring(0);
 	}
 
 	public static String xmlHeader()
 	{
 		return "<?xml version=" + "\"1.0\"" + " encoding=" + "\"US-ASCII\"" + "?>";
 	}
 
 
 	static final int ISO_LATIN1_START	= 128;
 	/**
 	 * Translate XML named entity special characters into their Unicode char
 	 * equivalents.
 	 */
 	public static String unescapeXML(String s)
 	{
 		if( s == null )
 			return null;
 		int		ampPos			= s.indexOf('&');
 
 		if (ampPos == -1) 
 			return s;
 		else
 		{
 			StringBuilder buffy	= new StringBuilder(s);
 			unescapeXML(buffy, ampPos);
 			return buffy.toString();
 		}
 	}
 
 	public static void unescapeXML(StringBuilder buffy)
 	{
 		unescapeXML(buffy, 0);
 	}
 	/**
 	 * Translate XML named entity special characters into their Unicode char
 	 * equivalents.
 	 */
 	public static void unescapeXML(StringBuilder buffy, int startPos)
 	{
 		int		ampPos; //, startPos); 
 
 		while( (ampPos=buffy.indexOf("&", startPos)) != -1 )
 		{
 			int		entityPos		= ampPos + 1;
 			int		semicolonPos	= buffy.indexOf(";", entityPos);
 
 			if (semicolonPos == -1) 
 				return;
 			else if (semicolonPos - ampPos > 7)
 			{
 				startPos = semicolonPos+1;
 				continue;
 			}
 
 			/* We are aready checking whether entityLenth is larger than 7, so we don't need the below 
 		  	 - Eunyee
 		  int entityLength			= semicolonPos - ampPos;
 		  if (entityLength > 8)
 			  return sb;
 			 */
 
 			// find position of & followed by ;
 
 			// lookup entity in the middle of that in the HashMap
 			// if you find a match, do a replacement *in place*
 
 			// this includes shifting the rest of the string up, and 
 
 			// resetting the length of the StringBuilder to be shorter 
 			// (since the entity is always longer than the char it maps to)
 
 			// then call recursively, setting the startPos index to after the last
 			// entity that we found
 
 			String encoded = buffy.substring(entityPos, semicolonPos);
 
 
 			String lookup = entityTable.get(encoded);
 
 			if (lookup != null)
 			{
 				//			  if (!"nbsp".equals(encoded))
 				//				  println("unescapeXML[" +encoded + "] -> " + lookup );
 				if (!lookup.equals(BOGUS))
 				{
 					buffy = buffy.replace(ampPos, semicolonPos+1, lookup);
 				}				
 				startPos = ampPos+1;
 			}
 			else 
 			{
 				if (buffy.charAt(entityPos) == '#')
 				{
 					int numPos	= entityPos++;
 					try
 					{
 						Integer newEntity	= Integer.decode(buffy.substring(numPos, semicolonPos));
 						println("unescapeXML[" +encoded + "] Create New Entity!");
 						char newChar = (char) newEntity.intValue();
 						putEntityInTable(encoded, newChar);
 						buffy = buffy.replace(ampPos, semicolonPos+1, Character.toString(newChar));
 						startPos = ampPos+1;
 					} catch (NumberFormatException e)
 					{
 						println("unescapeXML[" +encoded + "] FAILED");
 						entityTable.put(encoded, BOGUS);
 					}
 				}
 				
 				startPos	= semicolonPos+1;
 				if (startPos >= buffy.length())
 					return;
 			}
 		}
 	}
 	/**
 	 * Replaces characters that may be confused by a HTML
 	 * parser with their equivalent character entity references.
 	 * @param stringToEscape	original string which may contain some characters which are confusing
 	 * to the HTML parser, for eg. &lt; and &gt;
 	 * @return	the string in which the confusing characters are replaced by their 
 	 * equivalent entity references
 	 */   
 	public static void oldEscapeXML(StringBuilder result, CharSequence stringToEscape)
 	{
 		int length = stringToEscape.length();
 		int newLength = length;
 		// first check for characters that might
 		// be dangerous and calculate a length
 		// of the string that has escapes.
 		for (int i=0; i<length; i++)
 		{
 			char c = stringToEscape.charAt(i);
 			switch(c)
 			{
 			case '\"':
 				newLength += 5;
 				break;
 			case '&':
 			case '\'':
 				newLength += 4;
 				break;
 			case '<':
 			case '>':
 				newLength += 3;
 				break;
 			case '\n':
 				newLength += 4;
 				break;
 			default: 
 				if (c >= ISO_LATIN1_START)
 					newLength += 5;
 			}
 		}
 		if (length == newLength)
 		{
 			// nothing to escape in the string
 			//result.append(stringToEscape)
 			result.append(stringToEscape);
 			return;
 			//            return stringToEscape;
 		}
 		if (result == null)
 			result = new StringBuilder(newLength);
 		for (int i=0; i<length; i++)
 		{
 			char c = stringToEscape.charAt(i);
 		}
 	}
 	/**
 	 * Table of replacement Strings for characters deemed nasty in XML.
 	 */
 	//   static final String[] ESCAPE_TABLE	= new String[ISO_LATIN1_START];
 	static final String[] ESCAPE_TABLE	= new String[Character.MAX_VALUE];
 
 	static
 	{
 		for (char c=0; c<ISO_LATIN1_START; c++)
 		{
 			switch(c)
 			{
 			case '\"':
 				ESCAPE_TABLE[c]		= "&quot;";
 				break;
 			case '\'':
 				ESCAPE_TABLE[c]		= "&#39;";
 				break;
 			case '&':
 				ESCAPE_TABLE[c]		= "&amp;";
 				break;
 			case '<':
 				ESCAPE_TABLE[c]		= "&lt;";
 				break;
 			case '>':
 				ESCAPE_TABLE[c]		= "&gt;";
 				break;
 			case '\n':
 				ESCAPE_TABLE[c]		= "&#10;";
 				break;
 			case TAB:
 			case CR:
 			default: 
 				break;
 			}
 		}
 		int n	= SPECIAL_CHARACTER_ENTITIES.length;
 		for (int i=0; i< n; i++)
 			addEscapeTableEntry(SPECIAL_CHARACTER_ENTITIES[i]);
 
 		//    	for (char c=ISO_LATIN1_START; c<Character.MAX_VALUE; c++)
 		for (char c=ISO_LATIN1_START; c<256; c++)
 		{
 			addEscapeTableEntry(c);
 		}
 	}
 
 	private static void addEscapeTableEntry(char c)
 	{
 		StringBuilder entry	= new StringBuilder(7);
 		entry.append('&').append('#').append((int) c).append(';');
 		ESCAPE_TABLE[c]		= entry.toString();
 	}
 	static boolean noCharsNeedEscaping(CharSequence stringToEscape)
 	{
 		int length	= stringToEscape.length();
 		for (int i=0; i<length; i++)
 		{
 			char c = stringToEscape.charAt(i);
 			if (c >= ISO_LATIN1_START)
 			{
 				return false;
 			}
 			else if (ESCAPE_TABLE[c] != null)
 			{
 				return false;
 			}
 			else
 			{
 				switch (c)
 				{
 				case TAB:
 				case CR:
 					break;
 				default:
 					if (c < 0x20)	//TODO we seem, currently to throw these chars away. this would be easy to fix.
 					return false;
 				break;
 				}
 			}
 		}
 		return true;
 	}
 	/**
 	 * Replaces characters that may be confused by a HTML
 	 * parser with their equivalent character entity references.
 	 * @param stringToEscape	original string which may contain some characters which are confusing
 	 * to the HTML parser, for eg. &lt; and &gt;
 	 * @return	the string in which the confusing characters are replaced by their 
 	 * equivalent entity references
 	 */   
 	public static void escapeXML(StringBuilder buffy, CharSequence stringToEscape)
 	{
 		if (noCharsNeedEscaping(stringToEscape))
 			buffy.append(stringToEscape);
 		else
 		{
 			int length	= stringToEscape.length();
 			for (int i=0; i<length; i++)
 			{
 				final char c 		= stringToEscape.charAt(i);
 				final String escaped	= ESCAPE_TABLE[c];
 
 				if (escaped != null)
 					buffy.append(escaped);		// append as String
 				else
 				{
 					switch (c)
 					{
 					case TAB:
 					case CR:
 						buffy.append(c);		// append as char (fastest!)
 						break;
 					default:
 						if (c >= 255)
 						{
 							//						   println("escapeXML() ERROR: " + ((int) c));
 							int cInt	= (int) c;
 							buffy.append('&').append('#').append(cInt).append(';');
 						}
 						else if (c >= 0x20)
 							buffy.append(c);	// append as char (fastest!)
 					break;
 					}
 				}
 			}
 		}
 	}
 
 	public static void escapeXML(Appendable appendable, CharSequence stringToEscape) 
 	throws IOException
 	{
 		if (noCharsNeedEscaping(stringToEscape))
 			appendable.append(stringToEscape);
 		else
 		{
 			int length	= stringToEscape.length();
 			for (int i=0; i<length; i++)
 			{
 				final char c 		= stringToEscape.charAt(i);
 				final String escaped	= ESCAPE_TABLE[c];
 
 				if (escaped != null)
 					appendable.append(escaped);		// append as String
 				else
 				{
 					switch (c)
 					{
 					case TAB:
 					case CR:
 						appendable.append(c);		// append as char (fastest!)
 						break;
 					default:
 						if (c >= 255)
 						{
 							//						   println("escapeXML() ERROR: " + ((int) c));
 							int cInt	= (int) c;
 							appendable.append('&').append('#').append(String.valueOf(cInt)).append(';');
 						}
 						else if (c >= 0x20)
 							appendable.append(c);	// append as char (fastest!)
 					break;
 					}
 				}
 			}
 		}
 	}
 	/**
 	 * Generate a DOM tree from a given String in the XML form.
 	 * <p/>
 	 * Uses the deprecated StringBufferInputStream class.
 	 * 
 	 * @param contents	the string for which the DOM needs to be constructed.
 	 * @return			the DOM tree representing the XML string.
 	 * 
 	 * @deprecated
 	 */
 	public static Document getDocument(String contents)
 	{
 		return ElementState.buildDOM(new StringBufferInputStream(contents));
 	}
 
 	public static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException
 	{
 		return DocumentBuilderFactory.newInstance().newDocumentBuilder();
 	}
 
 
 	/**
 	 * Pretty printing XML, properly indented according to hierarchy.
 	 * 
 	 * @param xmlDoc
 	 * @param outputStreamWriter
 	 * @throws FileNotFoundException 
 	 * @throws XMLTranslationException 
 	 */
 	public static void writePrettyXML(Document xmlDoc, File outFile) 
 	throws XMLTranslationException 
 	{
 		try
 		{
 			writePrettyXML(xmlDoc, new FileOutputStream(outFile));
 		} catch (FileNotFoundException e)
 		{
 			throw new XMLTranslationException("Writing pretty XML[" + outFile + "]", e);
 		}
 	}
 
 	static final int 	INDENT_AMOUNT			= 2;
 	static final String INDENT_AMOUNT_STRING	= Integer.toString(INDENT_AMOUNT);
 	/**
 	 * Pretty print XML, properly indented according to hierarchy.
 	 * 
 	 * @param xmlDoc
 	 * @param out
 	 * @throws XMLTranslationException 
 	 * @throws IOException 
 	 */
 	public static void writePrettyXML(Document xmlDoc, OutputStream outputStream) 
 	throws XMLTranslationException 
 	{
 		Transformer transformer;
 		try
 		{
 			TransformerFactory factory	= TransformerFactory.newInstance();
 			factory.setAttribute("indent-number", new Integer(INDENT_AMOUNT));
 			transformer 				= factory.newTransformer();
 			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
 			//transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", INDENT_AMOUNT_STRING);
 			OutputStreamWriter osw		= new java.io.OutputStreamWriter(outputStream, "utf-8");
 			transformer.transform(new DOMSource(xmlDoc), new StreamResult(osw));
 			osw.close();
 		} catch (Exception e)
 		{
 			throw new XMLTranslationException("Writing pretty XML", e);
 		} 
 	}
 
 	/**
 	 * @param field
 	 * 
 	 * @return	true if the class name and variable name for the field are equivalent.
 	 * This is the case when the variable name is capitalized, it equals the class name.
 	 */
 	public static boolean equivalentClassAndVarNames(Field field)
 	{
 		boolean result	= false;
 
 		String className	= field.getType().getName();
 		String varName	= field.getName();
 		int classLength	= className.length();
 		if (classLength == varName.length())
 		{
 			char classStart	= className.charAt(0);
 			char varStart	= varName.charAt(0);
 			if (classStart == Character.toUpperCase(varStart))
 			{
 				result = className.regionMatches(1, varName, 1, classLength - 1);
 			}
 		}
 		return result;
 	}
 	/**
 	 * @param object	which might be representable as a collection. Must not be null.
 	 * 
 	 * @return	if the Object passed in is of Collection or Map type, 
 	 * 			return a Collection representing it. <br/>
 	 * 			else
 	 * 			return null.
 	 */
 	public static Collection getCollection(Object object)
 	{
 		Collection result	= null;
 		if (object instanceof Collection)
 			result			= (Collection) object;
 		else if (object instanceof Map)
 			result			= ((Map) object).values();
 
 		return result;
 	}
 
 	/**
 	 * During translation to XML, uses a field's type to determine
 	 * if the field is one that is emitted directly as an attribute.
 	 * This is true for types defined in the TypeRegistry (scalar values),
 	 * which are not declared as leaf nodes, using @leaf.
 	 * <p/>
 	 * Also useful during other translation processes to determine 
 	 * if the field is one that would be emitted directly as an attribute,
 	 * because such fields require minimal processing.
 	 * 
 	 * @param field The field which might be emittable as an attribute.
 	 * @param optimizations -- used to lookup leaf nodes. TODO -- get rid of this!
 	 * 
 	 * @return		true if the field's type is contained within the 
 	 * 				{@link cm.types.TypeRegistry TypeRegistry}
 	 */
 	static boolean emitFieldAsAttribute(Field field, ClassDescriptor optimizations)
 	{
 		return isScalarValue(field) && 
 		!(representAsLeafNode(field));
 	}
 	/**
 	 * Determine if the field is a scalar value that is represented in XML as a an leaf node.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	static boolean representAsLeafNode(Field field)
 	{
 		return representAsLeaf(field);
 	}
 	/**
 	 * Determine if the field is a scalar value that is represented in XML as a an leaf node.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	static boolean leafIsCDATA(Field field)
 	{
 		ElementState.xml_leaf leafAnnotation		= field.getAnnotation(ElementState.xml_leaf.class);
 		return ((leafAnnotation != null) && (leafAnnotation.value() == ElementState.CDATA));
 	}
 	/**
 	 * Determine if the field is a scalar value that is represented in XML as a an attribute.
 	 * 
 	 * @param field
 	 * @return
 	 */
 	static boolean representAsAttribute(Field field)
 	{
 		return field.isAnnotationPresent(ElementState.xml_attribute.class);
 	}
 	public static boolean representAsLeaf(Field field)
 	{
 		return field.isAnnotationPresent(ElementState.xml_leaf.class);
 	}
 
 	/**
 	 * 
 	 * @param field
 	 * @return	true if the field is leaf, nested, collection, or map.
 	 */
 	static boolean representAsLeafOrNested(Field field)
 	{
 		return representAsLeaf(field) || representAsNested(field);
 	}
 
 	public static boolean representAsText(Field field)
 	{
 		return field.isAnnotationPresent(ElementState.xml_text.class);
 	}
 
 	public static boolean representAsNested(Field field)
 	{
 		return field.isAnnotationPresent(ElementState.xml_nested.class);
 	}
 
 	public static boolean representAsCollectionOrMap(Field field)
 	{
 		return representAsCollection(field) || representAsMap(field);
 	}
 
 	public static boolean representAsMap(Field field)
 	{
 		return field.isAnnotationPresent(ElementState.xml_map.class);
 	}
 
 	public static boolean representAsCollection(Field field)
 	{
 		return field.isAnnotationPresent(ElementState.xml_collection.class);
 	}
 
 	/**
 	 * @param field
 	 * @return	true if the Field is one translated by the Type system.
 	 */
 	public static boolean isScalarValue(Field field)
 	{
 		return TypeRegistry.contains(field.getType());
 	}
 	/**
 	 * Wrap the passed in argument in HTML tags, so it can be parsed as XML.
 	 * 
 	 * @param htmlFragmentString	A piece of valid XHTML.
 	 * 
 	 * @return
 	 */
 	public static String wrapInHTMLTags(String htmlFragmentString)
 	{
 		StringBuilder buffy	= new StringBuilder(htmlFragmentString.length() + 13);
 		return 
 		buffy.append("<html>").append(htmlFragmentString).append("</html>").toString();
 	}
 
 
 	public static void main(String[] a)
 	{
 		String s	= "This is the first&amp; this is the next&rsquo;";
 
 		System.err.println(unescapeXML(s));
 	}
 
 	public static boolean isEnum(Field thatField)
 	{		
 		return Enum.class.isAssignableFrom(thatField.getType());
 	}
 
 	public static Enum<?> createEnumeratedType(Field field, String valueString)
 	{
 		if(field.getType().isEnum())
 		{
 			Object[] enumArray = field.getType().getEnumConstants();
 			for(Object enumObj : enumArray)
 			{
 				if(enumObj instanceof Enum<?>)
 				{
 					Enum<?> enumeratedType = ((Enum<?>) enumObj);
					if(enumeratedType.toString() == valueString)
 						return enumeratedType;
 				}
 			}
 		}
 		return null;
 	}
 }
