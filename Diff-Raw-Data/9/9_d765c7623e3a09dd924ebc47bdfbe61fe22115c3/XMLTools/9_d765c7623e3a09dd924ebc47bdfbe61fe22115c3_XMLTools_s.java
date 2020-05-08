 package ecologylab.xml;
 
 import java.util.*;
 import java.io.*;
 import java.net.URL;
 import java.lang.reflect.*;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 /**
  * This class contains methods which are used during the translation of java objects
  * to xml and back. All the methods are static. The xml files can also be compressed
  * by using the compression variable. For compression to work, the developer should
  * provide a abbreviation table in the format listed in the code.   
  * @author      Andruid Kerne
  * @author      Madhur Khandelwal
  * @version     0.5
  */
 public class XmlTools extends IO
 {
 	private static Hashtable stateClasses	=	new Hashtable();
 	
 	private static Hashtable encodingTable 	=	new Hashtable();
 	private static Hashtable decodingTable	=	new Hashtable();
 	
 	//the variables for each of the types String, boolean, int, float, url will not be
 	//emitted if they contain the following default values
 	private static String DEFAULT_STRING 	= 	"null";
 	private static String DEFAULT_BOOLEAN 	= 	"false";
 	private static String DEFAULT_INT 		= 	"0";
 	private static String DEFAULT_FLOAT		= 	"1.0";
 	private static String DEFAULT_URL		= 	"null";		
 	
 	//abbreviation table for storing the xml in a compressed form
 	private static String[][] elementAbbreviations;
 
 /**
  * This method generates a name for the xml tag given a reference type java object.
  * This is used during the translation of Java to xml. 
  * @param obj			name of a java reference type object 
  * @param suffix		string to remove from class name, null if nothing to be removed
  * @param compression	if the name of the element should be abbreviated
  * @return				name of the xml tag (element)
  */	
    public static String elementNameFromObject(Object obj, String suffix, boolean compression)
    {
       String className	= 	getClassName(obj.getClass());            
       
       if ((suffix != null) && (className.endsWith(suffix)))
       {
       	int suffixPosition	= className.lastIndexOf(suffix);
       	className			= className.substring(0, suffixPosition);
       }
 
 //    String result
       StringBuffer result = new StringBuffer(50);
       int classNameLength	= className.length();
       
 	  if(compression && (encodingTable.get(result) != null))
 	  {
 		  result.append((String)encodingTable.get(result));      	
 	  }
 	  else
 	  {
 	      for (int i=0; i<classNameLength; i++)
 	      {
 		 	char	c	= className.charAt(i);
 			
 			if ((c >= 'A') && (c <= 'Z') )
 			{
 				if(i == 0)
 					result.append(Character.toLowerCase(c));
 				else	
 				    result.append('_').append(Character.toLowerCase(c));
 			}
 			else
 			{
 			    result.append(c);
 			}
 	      }
 	  }
             
 //      StringBuffer packageName = new StringBuffer(getPackageName(obj));
 //      
 //      if(packageName != null)
 //      	return XmlTools.toString(packageName.append('-').append(result));
       
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
    * This method generates a name for the attribute given a field name, 
    * which is a primitive java type. It is used during translation of xml to Java. Using
    * this class name the appropriate class is instantiated using reflection.  
    * @param elementName		the name of the xml element or tag
    * @return				the name of the Java class corresponding to the elementName
    */   
    public static String classNameFromElementName(String elementName)
    {
 		if(ElementState.compressed && (decodingTable.get(elementName) != null))
 		{
 			elementName = (String)decodingTable.get(elementName);
 		}
    		
    		String result = "";
    		boolean capsOn = true;
    		
    		for(int i = 0; i < elementName.length(); i++)
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
 			if(i == 0)
 			{
 				char c = elementName.charAt(i);
 				result += Character.toLowerCase(c);
 			}
 			else
 				result += elementName.charAt(i);
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
  * @param field		the <code>Field</code> object type corresponding to the primitive type
  * @param obj		the object which contains the field
  * @return			name-value pair of the attribute, nothing if the field has a default value
  */
 	public static String generateNameVal(Field field, Object obj)
 	{
 		if (obj != null)
 		{
 			//take the field, generate tags and attach name value pair
 			try
 			{
 				String fieldValue = escapeXML( field.get(obj) + "" );
 				
 				//default values are not emitted, to keep the xml short
 				if(fieldValue.equals(DEFAULT_STRING) || fieldValue.equals(DEFAULT_BOOLEAN) || 
 				   fieldValue.equals(DEFAULT_FLOAT) || fieldValue.equals(DEFAULT_INT))
 				{
 					return "";
 				}
 				StringBuffer result = new StringBuffer(50);
 				result.append(" ").append(attrNameFromField(field, false)).append(" = ").append("\"").append(fieldValue).append("\"").append(" ");
 //				return " " + attrNameFromField(field, false) + " = " + "\"" + fieldValue + "\"" + " ";
 				return XmlTools.toString(result);
 				
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 		return "";
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
   * @return   			the package name of the class 
   */
 	public static String getPackageName(Class thatClass)
 	{
 	   String className	= thatClass.toString();
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
 			  packageName	=	thatClass.getPackage().getName();
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
 	
    static String q(String s)
    {
       return "\""+ s + "\" ";
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
     * creates a <code>Class</code> object from a given element name in the xml.
     * Also keeps it in the hashtable, so that when requested for the same class again
     * it doesnt have to create one.
     * @param classNameWithPackage	name of the state class along with its package name
     * @return 						a <code>Class</code> object for the given state class
     */
    public static Class getStateClass(String classNameWithPackage)
    {
 	   		Class stateClass 		= 	null;
 //
 //   		int packageNameIndex	=	classNameWithPackage.indexOf("-");
 //	   		String packageName		=	"";
 	   		String stateName		=	classNameWithPackage;
 //   		
 //   		if(packageNameIndex != -1)
 //   		{
 //   			packageName += 	classNameWithPackage.substring(0, packageNameIndex) + ".";
 //   			stateName	=   classNameWithPackage.substring(packageNameIndex+1); 		
 //   		}
 // 
 		
 //		String packageName	=	XMLTranslationConfig.getFullName(stateName);		  		
    		stateClass				=	(Class)stateClasses.get(stateName);   	   		
    		   		  		   		
    		if(stateClass	==	null)
    		{
 //   			String className = packageName;   						
 //			className += classNameFromElementName(stateName);
 			String className = classNameFromElementName(stateName);
 			String packageName = XMLTranslationConfig.getPackageName(className);
 			className = packageName + className;
 
 			try
 			{				
 				stateClass	=	Class.forName(className + "State");		
 				stateClasses.put(stateName,stateClass);				
 			}
 			catch(Exception e1)
 			{
 				try
 				{
 					stateClass	=	Class.forName(className);	
 					stateClasses.put(stateName, stateClass);					
 				}
 				catch(Exception e2)
 				{
 					e2.printStackTrace();
 				}
 			}
    		}
 		return stateClass;
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
                 default: 
 		   if (c >= ISO_LATIN1_START)
 		      sb.append("&#"+Integer.toString(c) + ";");
 		    else
 		       sb.append(c);
             }
         }
         return sb.toString();
     }
     
     public static Document getDocument(String contents)
     {
 		Document doc = null;
 		try 
 		{
 			StringBufferInputStream s       =     new StringBufferInputStream(contents);
 			DocumentBuilderFactory f        =     DocumentBuilderFactory.newInstance();
 			DocumentBuilder builder         =     f.newDocumentBuilder();
 			doc = builder.parse(s);
 		} catch (FactoryConfigurationError e) {
 			e.printStackTrace();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return doc;
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
 	 * Sets the default value for the boolean types so that boolean types containing
 	 * this value will NOT be emitted. Since they are default, they can be populated
 	 * automatically when generating Java object from the xml.
 	 * @param booleanVal	the default value of boolean type
 	 */
 	public static void setDEFAULT_BOOLEAN(boolean booleanVal) 
 	{
 		if(booleanVal == true)
 			DEFAULT_BOOLEAN = "true";
 		else
 			DEFAULT_BOOLEAN = "false";
 	}
 
 	/**
 	 * Sets the default value for the float types so that float types containing
 	 * this value will NOT be emitted. Since they are default, they can be populated
 	 * automatically when generating Java object from the xml.
 	 * @param floatVal	the default value of float type
 	 */
 	public static void setDEFAULT_FLOAT(float floatVal) 
 	{
 		DEFAULT_FLOAT = floatVal + "";
 	}
 
 	/**
 	 * Sets the default value for the integer types so that integer types containing
 	 * this value will NOT be emitted. Since they are default, they can be populated
 	 * automatically when generating Java object from the xml.
 	 * @param intVal	the default value of int type
 	 */
 	public static void setDEFAULT_INT(int intVal) 
 	{
 		DEFAULT_INT = intVal + "";
 	}
 	
 	/**
 	 * Sets the default value for the string types so that boolean types containing
 	 * this value will NOT be emitted. Since they are default, they can be populated
 	 * automatically when generating Java object from the xml.
 	 * @param stringVal	the default value of string type
 	 */
 	public static void setDEFAULT_STRING(String stringVal) 
 	{
 		DEFAULT_STRING = stringVal;
 	}
 	
 	/**
 	 * Sets the default value for the URL types so that URL types containing
 	 * this value will NOT be emitted. Since they are default, they can be populated
 	 * automatically when generating Java object from the xml.
 	 * @param urlVal	the default value of URL type
 	 */
 	public static void setDEFAULT_URL(String urlVal) 
 	{
 		DEFAULT_URL = urlVal;
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
 }
