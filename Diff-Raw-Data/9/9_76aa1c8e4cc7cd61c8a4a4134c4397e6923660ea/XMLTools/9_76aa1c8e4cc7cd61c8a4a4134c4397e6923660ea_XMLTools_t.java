 package ecologylab.xml;
 
 import java.io.StringBufferInputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.net.URL;
 import java.util.Collection;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 
 import ecologylab.types.Type;
 import ecologylab.types.TypeRegistry;
 
 /**
  * This class contains methods which are used during the translation of java objects
  * to XML and back. All the methods are static. The XML files can also be compressed
  * by using the compression variable. For compression to work, the developer should
  * provide a abbreviation table in the format listed in the code.   
  * 
  * @author      Andruid Kerne
  * @author      Madhur Khandelwal
  * @version     0.5
  */
 public class XmlTools extends TypeRegistry
 implements CharacterConstants
 {
 	private static Hashtable encodingTable 	=	new Hashtable();
 	private static Hashtable decodingTable	=	new Hashtable();
 	
 	//abbreviation table for storing the xml in a compressed form
 	private static String[][] elementAbbreviations;
 
 	static HashMap	entityTable				= new HashMap();
 
 	static final String entities[]=
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
       for (char i = 0; i != entities.length; i++)
 		 entityTable.put(entities[i], new Character((char) (i + 160)));
       
       // syntax such as &#38;
       for (int i = 0; i != 255; i++)
       {
 		 entityTable.put("#"+(int)i, new Character((char)i));
       }
       
       // defined in the XML 1.0 spec: "predefined entities"
       entityTable.put("amp", new Character('&'));
       entityTable.put("quot", new Character('"'));
       entityTable.put("lt", new Character('<'));
       entityTable.put("gt", new Character('>'));
       entityTable.put("apos", new Character('\''));
       entityTable.put("nbsp", new Character(' '));
    }
 	
 /**
  * This method generates a name for the xml tag given a reference type java object.
  * This is used during the translation of Java to xml. 
  * Part of this is to translate mixed case class name word separation into
  * "_" word separtion.
  * 
  * @param obj			a java reference type object 
  * @param suffix		string to remove from class name, null if nothing to be removed
  * @param compression	if the name of the element should be abbreviated
  * @return				name of the xml tag (element)
  */	
    public static String xmlTagFromObject(Object obj, 
    										 String suffix, boolean compression)
    {
 	  return getXmlTagName(obj.getClass(), suffix, compression);
    }
 /**
  * This method generates a name for the xml tag given a reference type java object.
  * This is used during the translation of Java to xml. 
  * Part of this is to translate mixed case class name word separation into
  * "_" word separtion.
  * 
  * @param thatClass		Class object to translate.
  * @param suffix		string to remove from class name, null if nothing to be removed
  * @param compression	if the name of the element should be abbreviated
  * @return				name of the xml tag (element)
  */	
    public static String getXmlTagName(Class thatClass, 
 									  String suffix, boolean compression)
    {
       String className	= 	getClassName(thatClass);
 	  return getXmlTagName(className, suffix, compression);
    }
 	
 /**
  * This method generates a name for the xml tag given a reference type java object.
  * This is used during the translation of Java to xml. 
  * Part of this is to translate mixed case class name word separation into
  * "_" word separtion.
  * 
  * @param className		class name of a java reference type object 
  * @param suffix		string to remove from class name, null if nothing to be removed
  * @param compression	if the name of the element should be abbreviated
  * @return				name of the xml tag (element)
  */	
    public static String getXmlTagName(String className,
    											String suffix, boolean compression)
    {
       if ((suffix != null) && (className.endsWith(suffix)))
       {
       	int suffixPosition	= className.lastIndexOf(suffix);
       	className			= className.substring(0, suffixPosition);
       }
 
       StringBuffer result = new StringBuffer(50);
 
       
 	  if (compression && (encodingTable.get(result) != null))
 	  {
 		  result.append((String)encodingTable.get(result));      	
 	  }
 	  else
 	  {   // translate mixed case class name word separation into
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
 	  }
       return XmlTools.toString(result);
    }
    
  /**
  * This method name for the attribute given a field name, which is a primitive java type.
  * This is used during the translation from Java to xml. 
  * @param field			the field(primitive type) in the state-class 
  * @param compression	if the name of the field should be abbreviated
  * @return				name of the attribute for the xml
  */
    public static String attrNameFromField(Field field, boolean compression)
    {
    		return field.getName();
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
     * @param elementName
     * @param capsOn			true if the first letter of output should be capitalized.
     * @param elementName	the name of the XML element or tag
     * @return				the name of the Java class corresponding to the elementName
     */
    public static String javaNameFromElementName(String elementName, boolean capsOn)
    {
 		if (ElementState.compressed && (decodingTable.get(elementName) != null))
 		{
 			elementName = (String)decodingTable.get(elementName);
 		}
    		
    		String result = "";
    		
    		for (int i = 0; i < elementName.length(); i++)
    		{
    			char c = elementName.charAt(i);
   			
    			if (capsOn)
    			{
    				result  += Character.toUpperCase(c);
    				capsOn = false;
    			}
    			else
    			{
    				if(c != '_')
 	   				result += c;
    			}
    			if(c == '_')
    				capsOn = true;
    		}
    		return result;
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
    		String result = "set";
    		
    		for(int i = 0; i < tagName.length(); i++){
    			
    			char c = tagName.charAt(i);
    			
    			if(i == 0 )
    				result += Character.toUpperCase(c);
    			else
    				result += c;
   				
    		}
    		return result;
    }
    /**
     * This method generates a field name from a reference type nested object. It just converts
     * the camelcase name to the lowercase. This is used while generating xml from Java.   
     * @param elementState	the reference type field for which a field field name needs to be generated
     * @return				field name for the given reference type field
     */
 	public static String fieldNameFromObject(ElementState elementState)
 	{
 		String result = "";
 		String elementName = getClassName(elementState);
 		
 		for(int i = 0; i < elementName.length(); i++)
 		{
 			char c = elementName.charAt(i);
 			if (i == 0)
 				c		= Character.toLowerCase(c);
 			result += c;
 		}
    		return result;
 	}
 	
    
 /**
  * This method gets called when the <code>Object</code> being passed for generating
  * a name-value pair is not of type <code>Field</code>. Thus, this method always returns
  * a null. 
  * @see <code>generateNameVal(Field field, Object obj)</code>
  * @return	String corresponding to an attr val pair, based on the field, and 
  * assumging that the field is of a primitive type.
  * If the field is not of a primitive type, return null.
  */
 	public static String generateNameVal(Object object, String fieldName)
 	{
 		String result	= null;
 		return result;
 	}
 	
     /**
      * This method generates a name value pair corresponding to the primitive Jave field. Returns
      * an empty string if the field contains a default value, which means that there is no need
      * to emit that field. Used while translation of Java to xml.  
      * @param field     the <code>Field</code> object type corresponding to the primitive type
      * @param obj       the object which contains the field
      * @return          name-value pair of the attribute, nothing if the field has a default value
      */
     public static String generateNameVal(Field field, Object obj, int floatingValuePrecision)
     {
         if (obj != null)
         {
             //take the field, generate tags and attach name value pair
             try
             {
                Type type        = TypeRegistry.getType(field);
                String fieldValue= escapeXML(type.toString(obj, field));
                if (type.isDefaultValue(fieldValue))
                   return "";
                
                StringBuffer result = new StringBuffer(50);
                result.append(' ').append(attrNameFromField(field, false))
                   .append("=\"");
                
                if ((floatingValuePrecision > ElementState.FLOATING_PRECISION_OFF) && 
                        (type.isFloatingPoint()))
                { // if we need to adjust for precision...
                    
                    // find the decimal, and where we SHOULD cut off...
                    int endPos = fieldValue.indexOf(".")+floatingValuePrecision+1;
                    
                    if (endPos > fieldValue.length())
                    { // if the cutoff is too far, do nothing
                        result.append(fieldValue);
                    }
                    else
                    { // if the cutoff is not too far, then cut off the extra
                       result.append(fieldValue.substring(0, endPos));
                    }
                    
                    // if there is nothing after the decimal, remove it
                    if (result.charAt(result.length()-1) == '.')
                    {
                        result.deleteCharAt(result.length()-1);
                    }
                    
                    result.append('"');
                }
                else
                {
                    result.append(fieldValue).append('"');
                }
                
 //             println("generateNameVal() = "+result);
                return XmlTools.toString(result);
                 
             }
             catch (Exception e)
             {
                println("generateNameVal("+field+", "+obj+" ERROR");
                e.printStackTrace();
             }
         }
         
         return "";
     }
     
 	public static String generateNameVal(Field field, Object obj)
 	{
 	    return generateNameVal(field, obj, ElementState.FLOATING_PRECISION_OFF);
     }
 	
 	static final HashMap		classAbbrevNames	= new HashMap();
 	static final HashMap		packageNames		= new HashMap();
 
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
 	   String fullName	= thatClass.getName();
 	   String abbrevName	= (String) classAbbrevNames.get(fullName);
 	   if (abbrevName == null)
 	   {
 		  abbrevName	= fullName.substring(fullName.lastIndexOf(".") + 1);
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
 	   String className	= thatClass.getName();
 	   String packageName = null;
 	   if(packageNames.containsKey(className))
 	   {
 		  packageName	= (String) packageNames.get(className);
 	   }
 	   else
 	   {
 	   	  if(thatClass.getPackage() != null)
 		  {	   	  
 //			  packageName	= 	className.substring(6, className.lastIndexOf("."));
 			  packageName	=	thatClass.getPackage().getName() + ".";
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
 	* <code>StringBuffer</code> on those occassions when you plan to keep
 	* using the <code>StringBuffer</code>, and want an efficiently made copy.
 	* In those cases, <i>much</i> better than 
 	* <code>new String(StringBuffer)</code>
 	*/
 	  public static final String toString(StringBuffer buffer)
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
 	  int		ampPos		= s.indexOf('&');
 	  
 	  
 	  
 	  if (ampPos == -1)
 		 return s;
 	  else
 	  {
 //	  	println("unescapeXML( found amp " + s);
 	  	return unescapeXML(new StringBuffer(s), ampPos).toString();
 	  }
    }
 	
 /**
  * Translate XML named entity special characters into their Unicode char
  * equivalents.
  */
     public static StringBuffer unescapeXML(StringBuffer sb, int startPos)
    {
 	  int		ampPos		= sb.indexOf("&", startPos);
 	  
 	  if (ampPos == -1)
 		 return sb;
 	  
 	  int		entityPos		= ampPos + 1;
 	  int		semicolonPos	= sb.indexOf(";", entityPos);
 	  
 	  if ((semicolonPos == -1) || (semicolonPos - ampPos > 7))
 		 return sb;
 	  
 	  // find position of & followed by ;
 	  
 	  // lookup entity in the middle of that in the HashMap
 	  // if you find a match, do a replacement *in place*
 	  
 	  // this includes shifting the rest of the string up, and 
 	  
 	  // resetting the length of the StringBuffer to be shorter 
 	  // (since the entity is always longer than the char it maps to)
 	  
 	  // then call recursively, setting the startPos index to after the last
 	  // entity that we found
 	  
 	  String encoded = sb.substring(entityPos, semicolonPos);
 
 	  if( encoded.startsWith("#") )
 	  {
 	  	String temp = encoded.substring(1);
 	  	encoded = "#"+Integer.valueOf(temp).toString();
 	  }
 	  	
 	  Character lookup = (Character)entityTable.get(encoded);
 	  println("unescapeXML: from " +encoded + " -> " + lookup );
 
 	  if ((semicolonPos+1 < sb.length()) && (lookup != null))
 	  {
 		  sb = sb.replace(ampPos, semicolonPos+1, ""+lookup.charValue());
 	  }	  
 	  return unescapeXML(sb, semicolonPos+1);
 	  
 /*	  String entityCandidate	= sb.substring(entityPos, semicolonPos);
 	  String result				= sb.substring(0, ampPos);
 	  Character lookup			= (Character) entityTable.get(entityCandidate);
 	  if (lookup != null)
 		 result				   += lookup.charValue();
 	  else
 		 result				   += entityCandidate;
 	  
 	  semicolonPos++;
 	  int length				= sb.length();
 	  if (semicolonPos > length)
 	  {
 		 String rest				= sb.substring(semicolonPos);
 		 if ((semicolonPos + 3) < sb.length())
 			result				   += unescapeXML(rest);
 		 else
 			result				   += rest;
 	  }
 	  return  result;
  */	  
    }
 	/**
 	* Replaces characters that may be confused by a HTML
 	* parser with their equivalent character entity references.
 	* @param s	original string which may contain some characters which are confusing
 	* to the HTML parser, for eg. &lt; and &gt;
 	* @return	the string in which the confusing characters are replaced by their 
 	* equivalent entity references
 	*/   
    public static String escapeXML(String s)
    {
         int length = s.length();
         int newLength = length;
         // first check for characters that might
         // be dangerous and calculate a length
         // of the string that has escapes.
         for (int i=0; i<length; i++)
         {
             char c = s.charAt(i);
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
                 default: 
 		   if (c >= ISO_LATIN1_START)
 		    newLength += 5;
             }
         }
         if (length == newLength)
         {
             // nothing to escape in the string
             return s;
         }
         StringBuffer sb = new StringBuffer(newLength);
         for (int i=0; i<length; i++)
         {
             char c = s.charAt(i);
             switch(c)
             {
                 case '\"':
                     sb.append("&quot;");
                 	break;
                 case '\'':
                     sb.append("&#39;");
                 	break;
                 case '&':
                     sb.append("&amp;");
                 	break;
                 case '<':
                     sb.append("&lt;");
                 	break;
                 case '>':
                     sb.append("&gt;");
                 	break;
                 case TAB:
                 case LF:
                 case CR:
      		       sb.append(c);
      		       break;
                 default: 
 				   if (c >= 0x20) 
 				   {
 				   		if (c >= ISO_LATIN1_START)
 							sb.append("&#"+Integer.toString(c) + ";");
 				   		else
 				      		sb.append(c);
 		           }
             }
         }
         return sb.toString();
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
 	
 	/**
 	 * Pretty printing xml, properly indented according to hierarchy.
 	 * @param plainXml	plain xml string
 	 * @param out		the <code>StreamResult</code> object where the output should be written
 	 */    
     public static void writePrettyXml(String plainXml, StreamResult out)
     {
         try
         {
             StringBufferInputStream s       =     new StringBufferInputStream(plainXml);
             DocumentBuilderFactory f        =     DocumentBuilderFactory.newInstance();
             DocumentBuilder builder         =     f.newDocumentBuilder();
             Document doc                    =     builder.parse(s);
             Transformer transformer         =     TransformerFactory.newInstance().newTransformer();
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
             transformer.transform(new DOMSource(doc), out);
         }
         catch(Exception e)
         {
              e.printStackTrace();
         }
     }   
 
 	/**
 	 * This method needs to be called when the users want compression of the generated xml
 	 * files. The compression is achieved by using abbreviated tag names instead of using 
 	 * the name of the file as the tag name. Normally if the name of the class is Foo, the 
 	 * tag name used is "foo", if the user wants compression, s/he must specify the corresponding
 	 * code to be used instead of "foo", for e.g. "f". The user can set the compression table
 	 * by creating name-vale pairs of class-name and tag-name, adding them to the hashtable
 	 * and then calling this method. If code for a particular class is not found, the name is
 	 * not abbreviated. 
 	 * @param compressionTable	Hashtable of name-value pairs of class-name and tag-name
 	 */
 	public static void setCompressionTable(Hashtable compressionTable)
 	{
 		int num	=	compressionTable.size();
 		elementAbbreviations = new String[num][2];
 		Enumeration keys = compressionTable.keys();
 		
 		int j = 0;
 		while(keys.hasMoreElements())
 		{
 			String elementName	=	(String)keys.nextElement();
 			String elementCode	=	(String)compressionTable.get(elementName);
 			
 			elementAbbreviations[j][0]		=	elementName;
 			elementAbbreviations[j++][1]	=	elementCode;
 		}
 		
 		for(int i=0 ;i<elementAbbreviations.length; i++)
 		{
 			  String[] thisEntry = elementAbbreviations[i];
 			  encodingTable.put(thisEntry[0], thisEntry[1]);
 			
 			  if((String)(decodingTable.get(thisEntry[1])) != null)
 				  throw new RuntimeException("duplicate code: " + thisEntry[1]);
 			  decodingTable.put(thisEntry[1], thisEntry[0]);
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
 	 * 
 	 * Also useful during other translation processes to determine 
 	 * if the field is one that would be emitted directly as an attribute,
 	 * because such fields require minimal processing.
 	 * 
 	 * @param field The field which might be emittable as an attribute.
 	 * 
 	 * @return		true if the field's type is contained within the 
 	 * 				{@link cm.types.TypeRegistry TypeRegistry}
 	 */
 	public static boolean emitFieldAsAttribute(Field field)
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
 		StringBuffer buffy	= new StringBuffer(htmlFragmentString.length() + 13);
 		return 
 		  buffy.append("<html>").append(htmlFragmentString).append("</html>").toString();
 	}
 }
