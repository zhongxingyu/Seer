 package ecologylab.xml;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.FactoryConfigurationError;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.stream.StreamResult;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 import ecologylab.generic.Debug;
 import ecologylab.generic.ReflectionTools;
 import ecologylab.generic.StringInputStream;
 import ecologylab.net.ParsedURL;
 import ecologylab.types.Type;
 import ecologylab.types.TypeRegistry;
 
 /**
  * This class is the heart of the <code>ecologylab.xml</code>
  * translation framework.
  * 
  * <p/>
  * To use the framework, the programmer must define a tree of objects derived
  * from this class. The public fields in each of these derived objects 
  * correspond to the XML DOM. The declarations of attribute fields  must 
  * preceed thos for nested XML elements. Attributes are built directly from
  * Strings, using classes derived from
  * @link ecologylab.types.Type ecologylab.types.Type}.
  *
  * <p/>
  * The framework proceeds automatically through the application of rules.
  * In the standard case, the rules are based on the automatic mapping of
  * XML element names (aka tags), to ElementState class names.
  * An mechanism for supplying additional translations may also be provided.
  * 
  * <p/>
  * <code>ElementState</code> is based on 2 methods, each of which employs 
  * Java reflection and recursive descent.
  * 
  * <li><code>translateToXML(...)</code> translates a tree of these 
  * <code>ElementState</code> objects into XML.</li>
  *
  * <li><code>translateFromXML(...)</code> translates an XML DOM into a tree of these
  * <code>ElementState</code> objects</li>
  *  
  * @author      Andruid Kerne
  * @author      Madhur Khandelwal
  * @version     2.9
  */
 public class ElementState extends Debug
 implements ParseTableEntryTypes
 {
 	/**
 	 * Link for a DOM tree.
 	 */
 	ElementState				parent;
 /**
  * Enables storage of a single text node child.
  * This facility is meager and rarely used, since
  * the leaf nodes facility does the same thing but better.
  * <p/>
  * We might want to implement the ability to store multiple text nodes
  * here some time in the future.
  */	
 	String						textNodeString;
 	
 	/**
 	 * Just-in time look-up tables to make translation be efficient.
 	 * Allocated on a per class basis.
 	 */
 	Optimizations				optimizations;
 	
 	/**
 	 * Use for resolving getElementById()
 	 */
 	HashMap						elementByIdMap;
 
 	
 	public static final int 	UTF16_LE	= 0;
 	public static final int 	UTF16		= 1;
 	public static final int 	UTF8		= 2;
 	/**
 	 * xml header
 	 */
 	static protected final String XML_FILE_HEADER = "<?xml version=" + "\"1.0\"" + " encoding=" + "\"UTF-8\"" + "?>\n";
 //	static protected final String XML_FILE_HEADER = "<?xml version=" + "\"1.0\"" + " encoding=" + "\"US-ASCII\"" + "?>";
 	
 	static protected final int	ESTIMATE_CHARS_PER_FIELD	= 80;
 	/**
 	 * whether the generated XML should be in compressed form or not
 	 */
 	protected static boolean 	compressed = false;
 
 	static final int 			TOP_LEVEL_NODE		= 1;
 	
 	private static final TranslationSpace globalNameSpace	= TranslationSpace.get("global");
 	
 /**
  * Used for argument marshalling with reflection to access 
  * a set method that takes a String as an argument.
  */
 	protected static Class[] 	MARSHALLING_PARAMS	= {String.class};
 
     /**
      * Constant indicating that floating precision cutoff is disabled. If floatingPrecision is set
      * to this value, then all available decimal places will be emitted.
      */
     public static final short             FLOATING_PRECISION_OFF   = -1;
 
     /**
      * Indicates how many digits after the decimal will be emitted on all floating values (floats
      * and doubles). If set to FLOATING_PRECISION_OFF (the default value), nothing will be done.
      */
     private short                         floatingPrecision = FLOATING_PRECISION_OFF;
     
 	public ElementState()
 	{
 	   optimizations			= Optimizations.lookup(this);
 	}
 /**
  * Emit XML header, then the object's XML.
  */
 	public String translateToXMLWithHeader(boolean compression) throws XmlTranslationException
 	{
 	   return XML_FILE_HEADER + translateToXML(compression);
 	}
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements.
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression				if the emitted xml needs to be compressed
 	 * @param nodeNumber				counts the depth of recursive descent.
 	 * 
 	 * @return 							the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	public String translateToXML(boolean compression) throws XmlTranslationException
 	{
 		//nodeNumber is just to indicate which node number(#1 is the root node of the DOM)
 		//is being processed. compression attr is emitted only for node number 1
 		return translateToXML(compression, true);
 	}
 	
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements
 	 * -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression				if the emitted xml needs to be compressed
 	 * @param doRecursiveDescent		true for recursive descent parsing.
 	 * 									false to parse just one level of attributes.
 	 * 										In this case, only the open tag w attributes is generated.
 	 * 										There is no close.
 	 * 
 	 * @return 							the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	public String translateToXML(boolean compression, boolean doRecursiveDescent) throws XmlTranslationException
 	{
 		return translateToXML(getClass(), compression, doRecursiveDescent);
 	}
 
 	public String translateToXML(Class thatClass, boolean compression, boolean doRecursiveDescent) throws XmlTranslationException
 	{
 		return translateToXML(thatClass, compression, doRecursiveDescent, TOP_LEVEL_NODE);
 	}
 
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements
 	 * -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression			true to compress the xml while emitting.
 	
 	 * @param doRecursiveDescent	true for recursive descent parsing.
 	 * 								false to parse just 1 level of attributes.
 	 * 										In this case, only the open tag w attributes is generated.
 	 * 										There is no close.
 	 * @param nodeNumber			counts the depth of recursive descent.
 	 * 
 	 * @return 						the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	private String translateToXML(Class thatClass, boolean compression, boolean doRecursiveDescent, int nodeNumber)
 		throws XmlTranslationException
 	{
 	   
 	   return translateToXML(thatClass, compression, doRecursiveDescent, nodeNumber,
 							 getTagMapEntry(thatClass, compression));
 	}
 	
 	/**
 	 * Translates a tree of ElementState objects into an equivalent XML string.
 	 * 
 	 * Uses Java reflection to iterate through the public fields of the object.
 	 * When primitive types are found, they are translated into attributes.
 	 * When objects derived from ElementState are found, 
 	 * they are recursively translated into nested elements
 	 * -- if doRecursiveDescent is true).
 	 * <p/>
 	 * Note: in the declaration of <code>this</code>, all nested elements 
 	 * must be after all attributes.
 	 * <p/>
 	 * The result is a hierarchichal XML structure.
 	 * <p/>
 	 * Note: to keep XML files from growing unduly large, there is a default 
 	 * value for each type.
 	 * Attributes which are set to the default value (for that type), 
 	 * are not emitted.
 	 * 
 	 * @param compression			true to compress the xml while emitting.
 	
 	 * @param doRecursiveDescent	true for recursive descent parsing.
 	 * 								false to parse just 1 level of attributes.
 	 * 										In this case, only the open tag w attributes is generated.
 	 * 										There is no close.
 	 * @param nodeNumber			counts the depth of recursive descent.
 	 * 
 	 * @return 						the generated xml string
 	 * 
 	 * @throws XmlTranslationException if there is a problem with the 
 	 * structure. Specifically, in each ElementState object, fields for 
 	 * attributes must be declared
 	 * before all fields for nested elements (those derived from ElementState).
 	 * If there is any public field which is not derived from ElementState
 	 * declared after the declaration for 1 or more ElementState instance
 	 * variables, this exception will be thrown.
 	 */
 	private String translateToXML(Class thatClass, 
 									boolean compression, boolean doRecursiveDescent, 
 									int nodeNumber, TagMapEntry tagMapEntry)
 		throws XmlTranslationException
 	{
 		compressed = compression;
 		nodeNumber++;
 		
 		StringBuffer	buffy			= null;
 		
 		try
 		{
 			//Class theClass = getClass();
 			Field[] fields	= thatClass.getFields();
 			//arrange the fields such that all primitive types occur before the reference types
 			arrangeFields(fields);
 			boolean	processingNestedElements= false;
 			
 			// maybe this should be getClass()
 			String className			= thatClass.getName();
 			int	numFields				= fields.length;
 			
 			buffy		= new StringBuffer(numFields * ESTIMATE_CHARS_PER_FIELD);
 			
 			buffy.append(tagMapEntry.startOpenTag);
 			
 			//emit compresseion = true only for the top node, so this dirty hack
 			//so if the nodeNumber is 1 (top node) then emit the compression attribute
 			if (compression && (nodeNumber == TOP_LEVEL_NODE))
 			{
 				buffy.append(' ').append("compression=\"").append(compression).append("\" ");
 			}
 
 			for (int i=0; i<numFields; i++)
 			{
 				// iterate through fields
 				Field thatField			= fields[i];
 				int fieldModifiers		= thatField.getModifiers();
 
 				// skip static fields, since we're saving instances,
 				// and inclusion w each instance would be redundant.
 				if ((fieldModifiers & Modifier.STATIC) == Modifier.STATIC)
 				{
 //					debug("Skipping " + thatField + " because its static!");
 					continue;
 				 }
 				if (XmlTools.emitFieldAsAttribute(thatField))
 				{
 					String declaringClassName			= 
 						thatField.getDeclaringClass().getName();
 
 					// false if from a parent / super class
 					boolean fieldIsFromDeclaringClass= 
 						declaringClassName.equals(className);
 					
 					String thatFieldName			= thatField.getName();
 					if (isLeafElementField(thatFieldName))
 					{
 						Type type		= TypeRegistry.getType(thatField);
 						String value	= XmlTools.escapeXML(type.toString(this, thatField));
 						buffy.append(tagMapEntry.startOpenTag).append('>')
 						.append(value).append(tagMapEntry.closeTag);
 					}
 
 					//TODO is field one that we are supposed to translate
 					// as a nested element with a with a single 
 					// TEXT_NODE child, instead of as an attribute.
 					if (fieldIsFromDeclaringClass && processingNestedElements)
 						throw new XmlTranslationException("Primitive type " + thatField + 
 				   					" found after Reference type " + fields[i-1].getType().getName());
 					
 					// emit only if the field is present in this classs
 					// parent class fields should not be emitted,
 					// coz thats confusing
 					if (fieldIsFromDeclaringClass || emitParentFields())
 						buffy.append(XmlTools.generateNameVal(thatField, this, floatingPrecision));
 				}
 				else if (doRecursiveDescent)	// recursive descent
 				{	
 					if (!processingNestedElements)
 					{	// found *first* recursive element
 						buffy.append('>');	// close element tag behind attributes
 						processingNestedElements	= true;
 					}
 					Object thatReferenceObject = null;
 					try
 					{
 						thatReferenceObject	= thatField.get(this);
 					}
 					catch (IllegalAccessException e)
 					{
 						e.printStackTrace();
 					}
 					// ignore null reference objects
 					if (thatReferenceObject == null)
 					   continue;
 					
 					Collection thatCollection = XmlTools.getCollection(thatReferenceObject);
 					
 					if (thatCollection != null)
 					{
 						//if the object is a collection, 
 						//basically iterate thru the collection and emit Xml from each element
 						Iterator elementIterator = thatCollection.iterator();
 					
 						while (elementIterator.hasNext())
 						{
 							Object next = elementIterator.next();
 							// this is a special hack for working with pre-translated XML Strings
 							if (next instanceof String)
 								buffy.append((String) next);
 							else
 							{
 								ElementState element;
 								try
 								{
 									element = (ElementState) next;
 								} catch(ClassCastException e)
 								{
 									throw new XmlTranslationException("Collections MUST contain " +
 											"objects of class derived from ElementState or XML Strings, but " +
 											thatReferenceObject +" contains some that aren't.");
 								}
 								buffy.append(element.translateToXML(element.getClass(), compression, true, nodeNumber));
 							}
 						}
 					}
 					else if (thatReferenceObject instanceof ElementState)
 					{	// one of our nested elements, so recurse
 						ElementState thatElementState	= (ElementState) thatReferenceObject;
 						String fieldName		= thatField.getName();
 						// if the field type is the same type of the instance (that is, if no subclassing),
 						// then use the field name to determine the XML tag name.
 						// if the field object is an instance of a subclass that extends the declared type of the
 						// field, use the instance's type to determine the XML tag name.
 						Class thatNewClass			= thatElementState.getClass();
 //						debug("checking: " + thatReferenceObject+" w " + thatNewClass+", " + thatField.getType());
 						if (thatNewClass == thatField.getType())
 							buffy.append( 
 							  thatElementState.translateToXML(thatNewClass, compression, true, nodeNumber,
 									  getTagMapEntry(fieldName, compression)));
 						else
 						{
 //						   debug("derived class -- using class name for " + thatNewClass);
 							buffy.append(
 							  thatElementState.translateToXML(thatNewClass, compression, true, nodeNumber,
 									  getTagMapEntry(thatNewClass, compression)));
 						}
 					}
 				} //end of doRecursiveDescent
 			} //end of for loop
 			
 			// end the element (or, at least, our contribution to it)
 			if (!doRecursiveDescent)
 				buffy.append('>'); // dont close it
 			else if (processingNestedElements)
 			{
 				//TODO emit text node
 				String textNode = this.getTextNodeString();
 				if ( textNode != null)
 				{
 					buffy.append(textNode);
 				}
 				buffy.append(tagMapEntry.closeTag);
 			}
 			else
 			{
 				String textNode = this.getTextNodeString();
 				if ( textNode != null)
 				{	
 					buffy.append('>').append(textNode).append(tagMapEntry.closeTag);
 				}
 				else
 				{
 					buffy.append("/>");	// simple element w attrs but no embedded elements and no text node
 				}
 			}
 				
 		} catch (SecurityException e)
 		{
 			e.printStackTrace();
 		}
 		return (buffy == null) ? "" : buffy.toString();
 	}
 	
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlDocumentPURL	ParsedURL for the XML document that needs to be translated.
 	 * @return 	   Parent ElementState object of the corresponding Java tree.
 	 */
 
 	public static ElementState translateFromXML(ParsedURL xmlDocumentPURL)
 	throws XmlTranslationException
 	{
 		return translateFromXML(xmlDocumentPURL, globalNameSpace);
 	}
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlDocumentPURL	ParsedURL for the XML document that needs to be translated.
 	 * @param nameSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 	   Parent ElementState object of the corresponding Java tree.
 	 */
 
 	public static ElementState translateFromXML(ParsedURL xmlDocumentPURL,
 												TranslationSpace nameSpace)
 	throws XmlTranslationException
 	{
 		return (xmlDocumentPURL == null) ? 
 		   null : translateFromXML(xmlDocumentPURL.url(), nameSpace);
 	}
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them 
 	 * into a tree of state-objects.
 	 * <p/>
 	 * Uses the default globalNameSpace as the basis for translation.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlDocumentURL	URL for the XML document that needs to be translated.
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 */
 
 	public static ElementState translateFromXML(URL xmlDocumentURL)
 	throws XmlTranslationException
 	{
 	   return translateFromXML(xmlDocumentURL, globalNameSpace);
 	}
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * <p/>
 	 * Uses the default globalNameSpace as the basis for translation.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlDocumentURL	URL for the XML document that needs to be translated.
 	 * 
 	 * @param nameSpace		NameSpace that provides basis for translation.
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 */
 
 	public static ElementState translateFromXML(URL xmlDocumentURL,
 												TranslationSpace nameSpace)
 	throws XmlTranslationException
 	{
 	   Document document	= buildDOM(xmlDocumentURL);
 	   return (document == null) ? 
 		  null : translateFromXML(document, nameSpace);
 	}
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlFile		the path to the XML document that needs to be translated.
 	 * @return 					the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(File xmlFile, 
 												TranslationSpace nameSpace)
 	throws XmlTranslationException
 	{
 	   Document document	= buildDOM(xmlFile);
 	   ElementState result	= null;
 	   if (document != null)
 		  result			= translateFromXML(document, nameSpace);
 	   return result;
 	}
 	/**
 	 * Given the URL of a valid XML document,
 	 * reads the document and builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * <p/>
 	 * Uses the default globalNameSpace as the basis for translation.
 	 * 
 	 * @param xmlFile		the path to the XML document that needs to be translated.
 	 * @return 					the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(File xmlFile)
 	throws XmlTranslationException
 	{
 	   return translateFromXML(xmlFile, globalNameSpace);
 	}
 	/**
 	 * Given the name of a valid XML file,
 	 * reads the file and builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param fileName	the name of the XML file that needs to be translated.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(String fileName,
 												TranslationSpace nameSpace)
 		throws XmlTranslationException
 	{
 		Document document	= buildDOM(fileName);
 		return (document == null) ? null : translateFromXML(document, nameSpace);
 	}
 	/**
 	 * Given the name of a valid XML file,
 	 * reads the file and builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param fileName	the name of the XML file that needs to be translated.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(String fileName)
 		throws XmlTranslationException
 	{
 		return translateFromXML(fileName, globalNameSpace);
 	}
 	
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlStream	An InputStream to the XML that needs to be translated.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(InputStream xmlStream,
 												TranslationSpace nameSpace)
 		throws XmlTranslationException
 	{
 		Document document	= buildDOM(xmlStream);
 		return (document == null) ? null : translateFromXML(document, nameSpace);
 	}	
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlStream	An InputStream to the XML that needs to be translated.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(InputStream xmlStream)
 		throws XmlTranslationException
 	{
 		return translateFromXML(xmlStream, globalNameSpace);
 	}	
 	
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlString	the actual XML that needs to be translated.
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString, 
 													  int charsetType,
 													  TranslationSpace nameSpace)
 		throws XmlTranslationException
 	{
 	   return translateFromXMLString(xmlString, charsetType, nameSpace, true);
 	}
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlString	the actual XML that needs to be translated.
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString, 
 													  int charsetType,
 													  TranslationSpace nameSpace,
 													boolean doRecursiveDescent)
 		throws XmlTranslationException
 	{
 	   Document document	= buildDOMFromXMLString(xmlString, charsetType);
 	   return (document == null) ? null : 
 		  translateFromXML(document,nameSpace, doRecursiveDescent);
 	}
 	/**
 	 * Given an XML-formatted String, 
 	 * builds a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which is 
 	 * an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of classes derived
 	 * from ElementState, which corresponds to the structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlString	the actual XML that needs to be translated.
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * @return 			the parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString, 
 													  int charsetType)
 		throws XmlTranslationException
 	{
 	   return translateFromXMLString(xmlString, charsetType, globalNameSpace);
 	}
 	
 	/**
 	 * Given an XML-formatted String, uses charset type UTF-8 to create
 	 * a stream, and build a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the
 	 * structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into
 	 * a tree of state-objects. Uses the default UTF8 charset.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlString	the actual XML that needs to be translated.
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString,
 													  TranslationSpace nameSpace)
 		throws XmlTranslationException
 	{
 
 	   return translateFromXMLString(xmlString, nameSpace, true);
 	}
 	/**
 	 * Given an XML-formatted String, uses charset type UTF-8 to create
 	 * a stream, and build a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the
 	 * structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into
 	 * a tree of state-objects. Uses the default UTF8 charset.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlString	the actual XML that needs to be translated.
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString,
 													  TranslationSpace nameSpace,
 													boolean doRecursiveDescent)
 		throws XmlTranslationException
 	{
 
 	   xmlString = XML_FILE_HEADER + xmlString;
 	   return translateFromXMLString(xmlString, StringInputStream.UTF8,
 									 nameSpace, doRecursiveDescent);
 	}
 	/**
 	 * Given an XML-formatted String, uses charset type UTF-8 to create
 	 * a stream, and build a tree of equivalent ElementState objects.
 	 * 
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the
 	 * structure of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlString	the actual XML that needs to be translated.
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * @return 		 Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXMLString(String xmlString)
 		throws XmlTranslationException
 	{
 	   return translateFromXMLString(xmlString, globalNameSpace);
 	}
 	
 	/**
 	 * Given the Document object for an XML DOM, builds a tree of equivalent
 	 * ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of 
 	 * classes derived from ElementState, which corresponds to the structure
 	 * of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them
 	 * into a tree of state-objects.
 	 * <p/>
 	 * Uses the default globalNameSpace as the basis for translation.
 	 * <p/>
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param doc	Document object for DOM tree that needs to be translated.
 	 * @return 	  Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Document doc)
 	throws XmlTranslationException
 	{
 	   return translateFromXML(doc, globalNameSpace);
 	}
 	
 	/**
 	 * Given the Document object for an XML DOM, builds a tree of equivalent
 	 * ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the 
 	 * structure of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param doc	Document object for DOM tree that needs to be translated.
 	 * @param nameSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 		Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Document doc, 
 												TranslationSpace nameSpace)
 	throws XmlTranslationException
 	{
 		return translateFromXML(doc, nameSpace, true);
 	}
 	
 	/**
 	 * Given the Document object for an XML DOM, builds a tree of equivalent
 	 * ElementState objects.
 	 * <p/>
 	 * That is, translates the XML into a tree of Java objects, each of which
 	 * is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree 
 	 * of classes derived from ElementState, which corresponds to the 
 	 * structure of the XML DOM that needs to be parsed.
 	 * <p/>
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file.
 	 * S/he passes it to this method to create a Java hierarchy equivalent to 
 	 * the DOM.
 	 * <p/>
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param dom	Document object for DOM tree that needs to be translated.
 	 * @param nameSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 		Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Document dom, 
 												TranslationSpace nameSpace,
 												boolean doRecursiveDescent)
 	throws XmlTranslationException
 	{
 		Node rootNode				= (Node) dom.getDocumentElement();
 		return translateFromXML(rootNode, nameSpace, doRecursiveDescent);
 	}
 	
 	/**
 	 * A recursive method.
 	 * Typically, this method is initially passed the root Node of an XML DOM,
 	 * from which it builds a tree of equivalent ElementState objects.
 	 * It does this by recursively calling itself for each node/subtree of 
 	 * ElementState objects.
 	 * 
 	 * The method translates any tree of DOM into a tree of Java objects, each
 	 * of which is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of 
 	 * classes derived from ElementState, which corresponds to the structure 
 	 * of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file, and access the 
 	 * root Node. S/he passes it to this method to create a Java hierarchy 
 	 * equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them 
 	 * into a tree of state-objects.
 	 * <p/>
 	 * Uses the default globalNameSpace as the basis for translation.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlNode	Root node of the DOM tree that needs to be translated.
 	 * @return 			Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Node xmlNode)
 	   throws XmlTranslationException
 	{
 	   return translateFromXML(xmlNode, globalNameSpace);
 	}
 
 	/**
 	 * A recursive method.
 	 * Typically, this method is initially passed the root Node of an XML DOM,
 	 * from which it builds a tree of equivalent ElementState objects.
 	 * It does this by recursively calling itself for each node/subtree of 
 	 * ElementState objects.
 	 * 
 	 * The method translates any tree of DOM into a tree of Java objects, each
 	 * of which is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of 
 	 * classes derived from ElementState, which corresponds to the structure 
 	 * of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file, and access the 
 	 * root Node. S/he passes it to this method to create a Java hierarchy 
 	 * equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlNode	Root node of the DOM tree that needs to be translated.
 	 * @param nameSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 			Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Node xmlNode,
 												TranslationSpace nameSpace)
 	   throws XmlTranslationException
 	{
 	   return translateFromXML(xmlNode, nameSpace, true);
 	   
 	}
 	/**
 	 * A recursive method.
 	 * Typically, this method is initially passed the root Node of an XML DOM,
 	 * from which it builds a tree of equivalent ElementState objects.
 	 * It does this by recursively calling itself for each node/subtree of 
 	 * ElementState objects.
 	 * 
 	 * The method translates any tree of DOM into a tree of Java objects, each
 	 * of which is an instance of a subclass of ElementState.
 	 * The operation of the method is predicated on the existence of a tree of 
 	 * classes derived from ElementState, which corresponds to the structure 
 	 * of the XML DOM that needs to be parsed.
 	 * 
 	 * Before calling the version of this method with this signature,
 	 * the programmer needs to create a DOM from the XML file, and access the 
 	 * root Node. S/he passes it to this method to create a Java hierarchy 
 	 * equivalent to the DOM.
 	 * 
 	 * Recursively parses the XML nodes in DFS order and translates them into 
 	 * a tree of state-objects.
 	 * 
 	 * This method used to be called builtStateObject(...).
 	 * 
 	 * @param xmlNode		Root node of the DOM tree that needs to be translated.
 	 * @param nameSpace		NameSpace that provides basis for translation.
 	 * 
 	 * @return 				Parent ElementState object of the corresponding Java tree.
 	 */
 	public static ElementState translateFromXML(Node xmlNode,
 												TranslationSpace nameSpace,
 												boolean doRecursiveDescent)
 	   throws XmlTranslationException
 	{
 	   // find the class for the new object derived from ElementState
 		Class stateClass			= null;
 		String tagName				= xmlNode.getNodeName();
 		int colonIndex				= tagName.indexOf(':');
 		if (colonIndex > 1)
 		{   // we are dealing with an XML Namespace
 			//TODO -- do something more substantial than throwing away the prefix
 			tagName					= tagName.substring(colonIndex + 1);
 		}
 		try
 		{			  
 		   stateClass= nameSpace.xmlTagToClass(tagName);
 		   if (stateClass != null)
 		   {
 		   	  ElementState rootState= getElementState(stateClass);
 		   	  if (rootState != null)
 		   	  {
 		   	  	 rootState.elementByIdMap		= new HashMap();
 		   	  	 rootState.translateFromXML(xmlNode, stateClass, nameSpace, doRecursiveDescent);
 		   	  	 return rootState;
 		   	  }
 		   }
 		   else
 		   {
 			   // else, we dont translate this field; we ignore it.
 			   println("XML Translation WARNING: Cant find class object for Root XML element <"
 					   + tagName + ">: Ignored. ");
 		   }
 		}
 		catch (Exception e)
 		{
 		   StackTraceElement stackTrace[] = e.getStackTrace();
 		   println("XML Translation WARNING: Exception while trying to translate XML element <" 
 				   + tagName+ "> class="+stateClass + ". Ignored.\nThe exception was " 
 				   + e.getMessage() + " from " +stackTrace[0]);
 		   //e.printStackTrace();
 //		   throw new XmlTranslationException("All ElementState subclasses"
 //							       + "MUST contain an empty constructor, but "+
 //								   stateClass+" doesn't seem to.");
 		}
 		return null;
 	 }		
 /**
  * Get an instance of an ElementState based Class object.
  * 
  * @param stateClass		Must be derived from ElementState. The type of the object to translate in to.
  * 
  * @return				The ElementState subclassed object.
  * 
  * @throws XmlTranslationException	If its not an ElementState Class object, or
  *  if that class lacks a constructor that takes no paramebers.
  */
 	public static ElementState getElementState(Class stateClass)
 	   throws XmlTranslationException
 	{
 		// form the new object derived from ElementState
 		ElementState elementState		= null;
 		try
 		{			  
 			elementState	=	(ElementState) stateClass.newInstance();
 		}
 		catch (Exception e)
 		{
 		   throw new XmlTranslationException("Instantiation ERROR for " + stateClass +":", e);
 		}
 		return elementState;
 	}
     /**
      * A recursive method.
      * Typically, this method is initially passed the root Node of an XML DOM,
      * from which it builds a tree of equivalent ElementState objects.
      * It does this by recursively calling itself for each node/subtree of ElementState objects.
      * 
      * The method translates any tree of DOM into a tree of Java objects, each
      * of which is an instance of a subclass of ElementState.
      * The operation of the method is predicated on the existence of a tree of
      * classes derived from ElementState, which corresponds to the structure 
 	 * of the XML DOM that needs to be parsed.
      * 
      * Before calling the version of this method with this signature, the
 	 *  programmer needs to create a DOM from the XML file, and access the root
      * Node. S/he passes it to this method to create a Java hierarchy 
 	 * equivalent to the DOM.
      * 
      * Recursively parses the XML nodes in DFS order and translates them into
 	 * a tree of state-objects.
      * 
      * @param xmlNode	Root node of the DOM tree that needs to be translated.
      * @param stateClass		Must be derived from ElementState. 
 	 *							The type of the object to translate in to.
      * @param translationSpace		NameSpace that provides basis for translation.
      * @return 			Parent ElementState object of the corresponding Java tree.
      */
 	private void translateFromXML(Node xmlNode, Class stateClass,
 								  TranslationSpace translationSpace, boolean doRecursiveDescent)
 	   throws XmlTranslationException
 	{
 		// translate attribtues
 		if (xmlNode.hasAttributes())
 		{
 			NamedNodeMap xmlNodeAttributes = xmlNode.getAttributes();
 			
 			for (int i = 0; i < xmlNodeAttributes.getLength(); i++) 
 			{
 				Node xmlAttr 		= xmlNodeAttributes.item(i);
 				String value		= xmlAttr.getNodeValue();
                
 				if (value != null)
 				{
 					//TODO consider getting rid of the TranslationSpace parameter here!
 					ParseTableEntry pte	= 
 						optimizations.parseTableAttrEntry(translationSpace, this, xmlAttr);
 					switch (pte.type())
 					{
 					case REGULAR_ATTRIBUTE:
 						pte.setAttribute(this, value);
 						if ("id".equals(pte.tag()))
 							this.elementByIdMap.put(value, this);
 						break;
 					default:
 						break;	
 					}
 				}
 			}
 		}
 		if (!doRecursiveDescent)
 			return;
 		
 		// translate nested elements (aka children):
 		// loop through them, recursively build them, and add them to ourself
 		NodeList childNodes	= xmlNode.getChildNodes();
 		int numChilds		= childNodes.getLength();
 	
 		for (int i = 0; i < numChilds; i++)
 		{
 			Node childNode		= childNodes.item(i);
 			short childNodeType	= childNode.getNodeType();
 			if ((childNodeType == Node.TEXT_NODE) || (childNodeType == Node.CDATA_SECTION_NODE))
 			{
 				setTextNodeString(childNode.getNodeValue());
 			}
 			else
 			{
 				ParseTableEntry pte		= optimizations.parseTableEntry(translationSpace, this, childNode);
 				ParseTableEntry nsPTE	= pte.nestedPTE();
 				ParseTableEntry	activePTE;
 				ElementState	activeES;
 				if (nsPTE != null)
 				{
 					activePTE				= nsPTE;
 					// get (create if necessary) the ElementState object corresponding to the XML Namespace
 					activeES				= (ElementState) ReflectionTools.getFieldValue(this, pte.field());
 					if (activeES == null)
 					{	// first time using the Namespace element, so we gotta create it
 						activeES			= pte.getChildElementState(this, null);
 						ReflectionTools.setFieldValue(this, pte.field(), activeES);
 					}
 				}
 				else
 				{
 					activePTE				= pte;
 					activeES				= this;
 				}
 				switch (pte.type())
 				{
 				case REGULAR_NESTED_ELEMENT:
 					activeES.setFieldToNestedElement(activePTE.field(), activePTE.getChildElementState(activeES, childNode));
 					break;
 				case LEAF_NODE_VALUE:
 					
 					Node textElementChild		= childNode.getFirstChild();	
 					activeES.setLeafNodeValue(activePTE.field(), textElementChild);
 					break;
 				case COLLECTION_ELEMENT:
 					Collection collection		= activeES.getCollection(activePTE.classOp(), activePTE.tag());
 					// the sleek new way to add elements to collections
 					collection.add(activePTE.getChildElementState(activeES, childNode));
 					break;
 				case OTHER_NESTED_ELEMENT:
 					activeES.addNestedElement(activePTE.getChildElementState(activeES, childNode));
 					break;
 				case IGNORED_ELEMENT:
 				case BAD_FIELD:
 				default:
 					break;
 				}
 			}
 		}
 	}
 	/**
 	 * Set an extended primitive value using the textElementChild Node as the source,
 	 * the stateClass as the template for where the field is located, 
 	 * the childFieldName as the name of the field to select in the template,
 	 * and this as the object to do the set in.
 	 * 
 	 * @param stateClass
 	 * @param childFieldName
 	 * @param textElementChildd	The leaf node with the text element value.
 	 * 
 	 * @throws NoSuchFieldException
 	 */
 	private void setLeafNodeValue(Class stateClass, String childFieldName, Node textElementChild)
 	throws NoSuchFieldException
 	{
 		Field childField		= stateClass.getField(childFieldName);;
 		setLeafNodeValue(childField, textElementChild);
 	}
 	/**
 	 * Set an extended primitive value using the textElementChild Node as the source,
 	 * the stateClass as the template for where the field is located, 
 	 * the childFieldName as the name of the field to select in the template,
 	 * and this as the object to do the set in.
 	 * 
 	 * @param childField		The Field in this that holds the value for the leaf node.
 	 * @param textElementChild	The leaf node with the text element value.
 	 */
 	private void setLeafNodeValue(Field childField, Node textElementChild)
 	{
 		if (textElementChild != null)
 		{
 			String textNodeValue	= textElementChild.getNodeValue();
 			if (textNodeValue != null)
 			{
 				textNodeValue		= XmlTools.unescapeXML(textNodeValue);
 				textNodeValue		= textNodeValue.trim();
 				//debug("setting special text node " +childFieldName +"="+textNodeValue);
 				if (textNodeValue.length() > 0)
 					this.setFieldUsingTypeRegistry(childField, textNodeValue);
 			}
 		}
 	}
 	/**
 	 * Construct the ElementState object corresponding to the childClass.
 	 * Set its elementByIdMap to be the same one that this uses.
 	 * @param childNode
 	 * @param childClass
 	 * @param translationSpace
 	 * 
 	 * @return
 	 * @throws XmlTranslationException
 	 */
 	ElementState getChildElementState(Node childNode,
 			Class childClass, TranslationSpace translationSpace)
 	throws XmlTranslationException
 	{
 		ElementState childElementState		= getElementState(childClass);
 		childElementState.elementByIdMap	= this.elementByIdMap;
 		childElementState.parent			= this;
 		
 		if (childNode != null)
 			childElementState.translateFromXML(childNode, childClass, translationSpace, true);
 		return childElementState;
 	}
 	
 
 	//////////////// methods to generate DOM objects ///////////////////////
 	/**
 	 * This method creates a DOM Document from the XML file at a given URL.
 	 *
 	 * @param url	the URL to the XML from which the DOM is to be created
 	 * 
 	 * @return			the Document object
 	 */
 	static public Document buildDOM(URL url)
 	{
 		return buildDOM(url.toString());
 	}
 	/**
 	 * This method creates a DOM Document from the local XML file.
 	 *
 	 * @param file		the XML file from which the DOM is to be created
 	 * 
 	 * @return			the Document object
 	 */
 	static public Document buildDOM(File file)
 	{
 		return buildDOM(file.toString());
 	}
 	/**
 	 * This method creates a DOM Document from the XML file at a given URI,
 	 * which could be a local file or a URL.
 	 *
 	 * @param xmlFileOrURLName	the path to the XML from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOM(String xmlFileOrURLName)
 	{		       
 		Document document	= null;
 		try
 		{
     	  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     	  DocumentBuilder builder = factory.newDocumentBuilder();
     	  createErrorHandler(builder);
     	  
   		  document = builder.parse(xmlFileOrURLName);
 		} 
 		
 		catch (SAXParseException spe) {
 			// Error generated by the parser
 		    println(xmlFileOrURLName + ":\n** Parsing error" + ", line " + spe.getLineNumber() + ", uri " + spe.getSystemId());
 		    println("   " + spe.getMessage());
 		  
 		    // Use the contained exception, if any
 		    Exception  x = spe;
 		    if (spe.getException() != null)
 		   	   x = spe.getException();
 		    x.printStackTrace();
 	  	}
 	  	
 	  	catch (SAXException sxe) {
 		    // Error generated during parsing
 		    Exception  x = sxe;
 		    if (sxe.getException() != null)
 		      x = sxe.getException();
 		    x.printStackTrace();
 	   	}
 	   	
 	   	catch (ParserConfigurationException pce) {
 		    // Parser with specified options can't be built
 		    pce.printStackTrace();
 	   	}
 	   	
 	   	catch (IOException ioe) {
 		    // I/O error
 		    ioe.printStackTrace();
 	  	}
 	  	
 	  	catch(FactoryConfigurationError fce){
 	  		fce.printStackTrace();
 	  	}
 	  	catch(Exception e){
 	  		e.printStackTrace();
 	  	}
 		return document;
 	}
 
 	/**
 	 * This method creates a DOM Document from the XML file at a given URI,
 	 * which could be a local file or a URL.
 	 *
 	 * @param xmlFileOrURLName	the path to the XML from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOM(InputStream inStream)
 	{		       
 		Document document	= null;
 		try
 		{
     	  DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
     	  DocumentBuilder builder = factory.newDocumentBuilder();
     	  createErrorHandler(builder);
     	  
   		  document = builder.parse(inStream);
 		} 
 		catch (SAXParseException spe)
 		{
 			// Error generated by the parser
 		    println("ERROR parsing DOM in" + inStream + ":\n\t** Parsing error on line " + spe.getLineNumber() + ", uri=" + spe.getSystemId());
 		    println("   " + spe.getMessage());
 	  	}
 	  	catch (SAXException sxe)
 	  	{   // Error generated during parsing
 		    Exception  x = sxe;
 		    if (sxe.getException() != null)
 		      x = sxe.getException();
 		    x.printStackTrace();
 	   	}
 	   	catch (ParserConfigurationException pce)
 	   	{
 		    // Parser with specified options can't be built
 		    pce.printStackTrace();
 	   	}
 	   	catch (IOException ioe)
 	   	{
 		    // I/O error
 		    ioe.printStackTrace();
 	  	}
 	  	catch(FactoryConfigurationError fce)
 	  	{
 	  		fce.printStackTrace();
 	  	}
 	  	catch(Exception e)
 	  	{
 	  		e.printStackTrace();
 	  	}
 		return document;
 	}
 	/**
 	 * This method creates a DOM Document from an XML-formatted String.
 	 *
 	 * @param xmlString	the XML-formatted String from which the DOM is to be created
 	 * @param charsetType	A constant from ecologylab.generic.StringInputStream.
 	 * 						0 for UTF16_LE. 1 for UTF16. 2 for UTF8.
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOMFromXMLString(String xmlString,
 												 int charsetType)
     {
 	   InputStream xmlStream =
 		  new StringInputStream(xmlString, charsetType);
 
 	   return buildDOM(xmlStream);
 	}
 
 	/**
 	 * This method creates a DOM Document from an XML-formatted String,
 	 * encoded as UTF8.
 	 *
 	 * @param xmlString	the XML-formatted String from which the DOM is to be created
 	 * 
 	 * @return					the Document object
 	 */
 	static public Document buildDOMFromXMLString(String xmlString)
     {
 	   return buildDOMFromXMLString(xmlString, UTF8);
 	}
 
   	static private void createErrorHandler(final DocumentBuilder builder){
   		
   		builder.setErrorHandler(
 	  	new org.xml.sax.ErrorHandler() {
 	    	// ignore fatal errors (an exception is guaranteed)
 		    public void fatalError(SAXParseException exception)
 		    throws SAXException {
 		    }
 		    // treat validation errors as fatal
 		    public void error(SAXParseException e)
 		    throws SAXParseException
 		    {
 		      throw e;
 		    }
 		
 		     // dump warnings too
 		    public void warning(SAXParseException err)
 		    throws SAXParseException
 		    {
 		      println(builder + "** Warning"
 		        + ", line " + err.getLineNumber()
 		        + ", uri " + err.getSystemId());
 		      println("   " + err.getMessage());
 		    }
 	    
 	  	}  
 		); 
   	}
 
 	//////////////// methods to generate XML, and write to a file /////////////
 /**
  * 	Translate to XML, then write the result to a file.
  * 
  * 	@param filePath		the file in which the xml needs to be saved
  * 	@param prettyXml	whether the xml should be written in an indented fashion
  *  @param compression	whether the xml should be compressed while being emitted
  */	
 	public void saveXmlFile(String filePath, boolean prettyXml, boolean compression)
 		throws XmlTranslationException
 	{
 		final String xml = translateToXMLWithHeader(compression);
 
 		//write the Xml in the file		
 		try
 		{
 			String xmlFileName = filePath;
 			if(!filePath.endsWith(".xml") && !filePath.endsWith(".XML"))
 			{
 				xmlFileName = filePath + ".xml";
 			}
 			else
 			{
 				filePath	=	filePath.substring(0,filePath.indexOf(".xml"));
 			}
 		 if (prettyXml)
 		 {
 		 	XmlTools.writePrettyXml(xml, new StreamResult(new File(xmlFileName)));
 		 }
 		 
 			else
 			{
 				FileOutputStream out = new FileOutputStream(new File(xmlFileName));
 				PrintStream p = new PrintStream(out);
 				p.println(xml);
 				p.close();
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}		
 	}
 	
 /**
  * Andruid [1/2/05]: this method is supposed to do the following, but it doesn't seem
  * that its being used, nor does it really seem to do this.
  * <p/>
  * 
  * Translate to XML, then appends the result to a file. 
  * This method is used when XML should be emitted and written to a file
  * incrementally. In other words, it does not wait for the complete XML to be
  * emitted before saving it to a file. This is useful in cases such as logging.
  * 
  * @see  <code>saveXmlFile</code>
 
  * 	@param filePath		the file in which the xml needs to be saved
  * 	@param prettyXml	whether the xml should be written in an indented fashion
  *  @param compression	whether the xml should be compressed while being emitted
  */	
 	public void appendXmlFile(String xmlToWrite, String filePath, 
 							  boolean prettyXml, boolean compression)
 	{
 		try
 		{
 			String xmlFileName = filePath;
 			if(!filePath.endsWith(".xml") && !filePath.endsWith(".XML"))
 			{
 				xmlFileName = filePath + ".xml";
 			}
 			else
 			{
 				filePath	=	filePath.substring(0,filePath.indexOf(".xml"));
 			}
 			if (prettyXml)
 				XmlTools.writePrettyXml(xmlToWrite, new StreamResult(new File(xmlFileName)));
 			else
 			{
 				BufferedWriter writer = IO.openWriter(xmlFileName, true);
 				File temp = new File(xmlFileName);
 				if(!temp.exists())
 				IO.writeLine(writer,XML_FILE_HEADER);								
 				IO.writeLine(writer,translateToXML(compression));
 				IO.closeWriter(writer);
 			}
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	//////////////// helper methods used by translateToXML() //////////////////
 
 /**
  * Get the map for translating field names to startOpenTags for this.
  * We have to create a HashMap to do this, instead of using a static,
  * because all relevant objects are subclassed from <code>ElementState</code>,
  * so a static declaration wouldn't actually be class wide.
  */
 /*
 	private HashMap getFieldNamesToOpenTagsMap()
 	{
 	   Class thisClass= getClass();
 	   HashMap result = (HashMap) eStateToFieldNameOrClassToOpenTagMapMap.get(thisClass);
 	   // stay out of the synchronized block most of the time
 	   if (result == null)
 	   {
 		  synchronized (eStateToFieldNameOrClassToOpenTagMapMap)
 		  {
 			 result = (HashMap) eStateToFieldNameOrClassToOpenTagMapMap.get(thisClass);
 			 if (result == null)
 			 {
 				result	= new HashMap();
 				eStateToFieldNameOrClassToOpenTagMapMap.put(thisClass, result);
 			 }
 		  }
 	   }
 	   return result;
 	}
 */
 	/**
 	 * @param nameSpace TODO
 	 * @return	the XML element name, or <i>tag</i>, that maps to this ElementState derived class.
 	 */
 	public String tagName(TranslationSpace nameSpace)
 	{
 	   return globalNameSpace.objectToXmlTag(this);
 	}
 	
 /**
  * Get a tag translation object that corresponds to the fieldName,
  * with this class. If necessary, form that tag translation object,
  * and cache it.
  */
 	private TagMapEntry getTagMapEntry(String fieldName, boolean compression)
 	{
 		/*
 	   TagMapEntry result= (TagMapEntry)fieldNameOrClassToTagMap.get(fieldName);
 	   if (result == null)
 	   {
 		  synchronized (fieldNameOrClassToTagMap)
 		  {
 			 result		= (TagMapEntry) fieldNameOrClassToTagMap.get(fieldName);
 			 if (result == null)
 			 {
 				String tagName	= XmlTools.getXmlTagName(fieldName, "State", compression);
 				result	= new TagMapEntry(tagName);
 //				debug(tagName.toString());
 				fieldNameOrClassToTagMap.put(fieldName, result);
 			 }
 		  }
 	   }
 	   return result;
 	   */
 		return optimizations.getTagMapEntry(fieldName, compression);
 	}
 /**
  * Get a tag translation object that corresponds to the fieldName,
  * with this class. If necessary, form that tag translation object,
  * and cache it.
  */
 	protected TagMapEntry getTagMapEntry(Class thatClass, boolean compression)
 	{
 		/*
 	   TagMapEntry result= (TagMapEntry)fieldNameOrClassToTagMap.get(thatClass);
 	   if (result == null)
 	   {
 		  synchronized (fieldNameOrClassToTagMap)
 		  {
 			 result		= (TagMapEntry) fieldNameOrClassToTagMap.get(thatClass);
 			 if (result == null)
 			 {
 				String tagName	= XmlTools.getXmlTagName(thatClass, "State", compression);
 				result	= new TagMapEntry(tagName);
 				fieldNameOrClassToTagMap.put(thatClass, result);
 				debug(tagName.toString());
 			 }
 		  }
 	   }
 	   return result;
 	   */
 		return optimizations.getTagMapEntry(thatClass, compression);
 	}
 	/**
 	 * Reorders fields so that all primitive types occur first and then the reference types.
 	 * 
 	 * @param fields
 	 */
 	private void arrangeFields(Field[] fields)
 	{
 		int primitivePos = 0;
 		ArrayList refTypes = new ArrayList();
 		
 		for (int i = 0; i < fields.length; i++)
 		{
 			Field thatField	= fields[i];
 			if (XmlTools.emitFieldAsAttribute(thatField))
 			{
 				if(i > primitivePos)
 				{
 					fields[primitivePos] = thatField;
 				}
 				primitivePos++;
 			}
 			else
 			{
 				refTypes.add(thatField);
 			}
 		}
 		
 		//copy the ref types at the end of the primitive types
 		int j = 0;
 		for(int i = fields.length - refTypes.size(); i < fields.length; i++)
 		{
 			fields[i]	=	(Field)refTypes.get(j++);			
 		}
 	}
 
 
 	//////////////// helper methods used by translateFromXML() ////////////////
 	/**
 	 * Set the specified extended primitive field in this, if possible.
 	 * 
 	 * @param fieldName		name of the field to set.
 	 * @param fieldValue	String representation of the value.
 	 * 
 	 * @return true if the field is set successfully. false if it seems to not exist.
 	 */
 	protected boolean setFieldUsingTypeRegistry(String fieldName, String fieldValue)
 	
 	{
 		boolean result		= false;
 		try
 		{
 		   Field field		= getClass().getField(fieldName);
 		   result			= setFieldUsingTypeRegistry(field, fieldValue);
 		}
 		catch (NoSuchFieldException e)
 		{
 			debug("ERROR no such field to set "+fieldName+" = "+
 			      fieldValue);
 		}
 		return result;
 	}
 	/**
 	 * Set a field that is an extended primitive -- a non ElementState --
 	 * using the type registry.
 	 * 
 	 * @param field
 	 * @param fieldValue
 	 * @return
 	 */
 	protected boolean setFieldUsingTypeRegistry(Field field, String fieldValue)
 	{
 		boolean result		= false;
 		Type fieldType		= TypeRegistry.getType(field);
 		if (fieldType != null)
 			result			= fieldType.setField(this, field, fieldValue);
 		else
 			debug("Can't find type for " + field + " with value=" + fieldValue);
 		return result;
 	}
 
 	/**
 	 * Used to add a nested object to <code>this ElementState</code> object.
 	 * 
 	 * @param elementState	the nested state-object to be added
 	 */
 	protected void addNestedElementToField(ElementState elementState)
 	{
 		String fieldName = XmlTools.fieldNameFromObject(elementState);
 //		debug("<<<<<<<<<<<<<<<<<<<<<<<<fieldName is: " + fieldName);
 		try
 		{
 			Field field = getClass().getField(fieldName);
 			setFieldToNestedElement(field, elementState);
 		}
 		catch (Exception e)
 		{
 		   debug("ERROR: Can't find a field called " + fieldName);
 		   e.printStackTrace();
 		}
 	}
 	boolean nestedElementHasBeenWarned;
 	
 	/**
 	 * This is the hook that enables programmers to do something special
 	 * when handling a nested XML element and its associate ElementState (subclass),
 	 * by overriding this method and providing a custom implementation.
 	 * <p/>
 	 * The default implementation is a no-op.
 	 * fields that get here are ignored.
 	 * 
 	 * @param elementState
 	 */
 	protected void addNestedElement(ElementState elementState)
 	{
 		if (!nestedElementHasBeenWarned)
 		{
 			nestedElementHasBeenWarned	= true;
 			debugA("IGNORED special nested element: " + elementState);
 		}
 	}
 	/**
 	 * Used to set a field in this to a nested ElementState object.
 	 * 
 	 * his method is called during translateFromXML(...).
 	 *
 	 * @param nestedElementState	the nested state-object to be added
 	 */
 	protected void setFieldToNestedElement(Field field, ElementState nestedElementState)
 		throws XmlTranslationException
 	{
 //		debug("<<<<<<<<<<<<<<<<<<<<<<<<fieldName is: " + fieldName);
 		try
 		{
 			field.set(this,nestedElementState);
 		}
 		catch (Exception e)
 		{
 		   throw new XmlTranslationException(
					"Object / Field set mismatch -- unexpected. This should never happen.\n\t"+
					"with Field = " + field +"\n\tin object " + this +"\n\tbeing set to " + nestedElementState.getClassName(), e);
 		}
 	}
 	/**
 	 * Called during translateFromXML().
 	 * If the textNodeString is currently null, assign to.
 	 * Otherwise, append to it.
 	 * 
 	 * @param textNodeString	Text Node value just found parsing the XML.
 	 */
 	public void setTextNodeString(String textNodeString)
 	{
 	   if (textNodeString != null)
 	   {
 		   String unescapedString = XmlTools.unescapeXML(textNodeString);
 		   String previousTextNodeString = this.textNodeString;
 		   this.textNodeString	= (previousTextNodeString == null) ?
 				   unescapedString : previousTextNodeString + unescapedString;
 	   }
 	}
 	public String getTextNodeString()
 	{
 		return textNodeString;
 //		return (textNodeString == null) ? null : XmlTools.unescapeXML(textNodeString);
 	}
 	/////////////////////////// other methods //////////////////////////
 
 	/**
 	 * Call this method if the object should be translated using a compression table to
 	 * mininmize space (and legibility :-).
 	 * 
 	 * @param value		String version of a boolean. Use "true" to turn it on.
 	 */
 	public void setCompressed(String value)
 	{
 		if ("true".equals(value))
 			compressed	=	true;
 	}
 	
 	/**
 	 * Add a package name to className mapping to the translation table in the NameSpace.
 	 * <br/><br/>Example:<br/><code>
 	 * 	  addTranslation("cf.history", "KeyframeState");<br/>
 	 *    addTranslation("cf.history", "KeyframeTimeStampSet");<br/></code>
 	 * <br/>
 	 * The class name will be translated into an xml tag name, using the usual rules.
 	 * 
 	 * @param packageName
 	 * @param className
 	 */
 	public static void addTranslation(String packageName, String className)
 	{
 		globalNameSpace.addTranslation(packageName, className);
 	}
    /**
 	* Set the default package name for XML tag to ElementState sub-class translations,
 	* for the global name space.
 	* 
 	* @param packageName	The new default package name.
 	*/
    public static void setDefaultPackageName(String packageName)
    {
 	  globalNameSpace.setDefaultPackageName(packageName);
    }
 
 	/**
 	 * The DOM classic accessor method.
 	 * 
 	 * @return element in the tree rooted from this, whose id attrribute is as in the parameter.
 	 * 
 	 */
 	public ElementState getElementStateById(String id)
 	{
 		return (ElementState) this.elementByIdMap.get(id);
 	}
 	
 	   /**
 	    * This is used by subclasses to declare primitive fields, each of
 	    * which gets translated to XML as an element with a single TEXT_NODE child,
 	    * instead of as an attribute.
 	    * 
 	    * @return	false, because by default there are no leaf elements defined in this.
 	    */
 	protected boolean isLeafElementField(String fieldName)
 	{
 		return optimizations.isLeafElementField(fieldName);
 	}
 
 	/**
 	 * Controls if the public fields of a parent class (= super class)
 	 * will be emitted or not, during translation to XML.
 	 * <p/>
 	 * Override this to change the behavior in subclasses.
 	 * 
 	 * @return		true by default.
 	 */
 	protected boolean emitParentFields()
 	{
 		return true;
 	}
 
 	/**
 	 * When translating from XML, if a tag is encountered with no matching field, perhaps
 	 * it belongs in a Collection.
 	 * This method tells us which collection object that would be.
 	 * 
 	 * @param thatClass		The class of the ElementState superclass that could be stored in a Collection.
 	 * @param tag 			The tag that represents the field in the XML where the ElementState object is stored.
 	 * @return
 	 */
 	protected Collection getCollection(Class thatClass, String tag)
 	{
 		return null;
 	}
 	
 	/**
 	 * An array of Strings with the names of the leaf elements.
 	 * Must be overridden to provide leaf elements as direct, typed field values.
 	 * 
 	 * @return		null in the default implementation.
 	 */
 	protected String[] leafElementFieldNames()
 	{
 		return null;
 	}
 	
 	
 	/**
 	 * Convenience for specifying what collection to put objects of a given
 	 * type into, where there is a clear mapping based on type (class).
 	 *
 	 * @author andruid
 	 */
 	protected class ClassToCollectionMap
 	extends HashMap
 	{
 		public ClassToCollectionMap(Object[][] mappings)
 		{
 			int numMappings	= mappings.length;
 			for (int i=0; i<numMappings; i++)
 			{
 				Object[] thatMapping			= mappings[i];
 				try
 				{
 					Class thatClass				= (Class) thatMapping[0];
 					Collection thatCollection	= (Collection) thatMapping[1];
 					put(thatClass.getSimpleName(), thatCollection);
 				} catch (ClassCastException e)
 				{
 					debug("ERROR in ClassToCollectionMap initializer("+i+" has wrong type:\n\t"+
 						  thatMapping[0] +", " + thatMapping[i]);
 				}
 			}
 		}
 		public Collection lookup(String className)
 		{
 			return (Collection) get(className);
 		}
 		public Collection lookup(Class thatClass)
 		{
 			return lookup(thatClass.getSimpleName());
 		}
 	}
 
 
 	/**
 	 * @return the parent
 	 */
 	protected ElementState parent()
 	{
 		return parent;
 	}
 }
