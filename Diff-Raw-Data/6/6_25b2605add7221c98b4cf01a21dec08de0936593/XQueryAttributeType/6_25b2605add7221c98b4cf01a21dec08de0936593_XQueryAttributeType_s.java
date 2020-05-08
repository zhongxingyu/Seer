 
 package edu.wustl.query.xquerydatatypes;
 
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.dom4j.Document;
 import org.dom4j.DocumentException;
 import org.dom4j.Element;
 import org.dom4j.io.SAXReader;
 
 /**
  * This is a Singleton class which parses the XML file that consists of mapping between PrimitiveAttribute 
  * and Database DataTypes and generates the Map of the same. 
  * @author chetan_patil
  */
 /**
  * @author mandar_shidhore
  *
  */
 public class XQueryAttributeType
 {
 
 	/**
 	 * 
 	 */
 	private static XQueryAttributeType xqueryAttrType = null;
 
 	/**
 	 * 
 	 */
 	private Map<String, Object> xqueryAttributeTypeMap;
 
 	/**
 	 * Empty constructor
 	 */
 	protected XQueryAttributeType()
 	{
 	}
 
 	/**
 	 * This method returns the instance of DataTypeFactory
 	 * @return DataTypeFactory instance
 	 * @throws XQueryDataTypeInitializationException 
 	 * @throws DataTypeFactoryInitializationException on Exception
 	 */
 	public static XQueryAttributeType getInstance() throws XQueryDataTypeInitializationException
 	{
		synchronized (xqueryAttrType)
 		{
			if (xqueryAttrType == null)
 			{
 				xqueryAttrType = new XQueryAttributeType();
 				String xqueryDataTypeMappingFileName = "XQuery_Datatypes" + ".xml";
 				xqueryAttrType.populateDataTypeMap(xqueryDataTypeMappingFileName);
 			}
 		}
 
 		return xqueryAttrType;
 	}
 
 	/**
 	 * This method updates module map by parsing xml file
 	 * @param xmlFileName file to be parsed
 	 * @return dataType Map
 	 * @throws DataTypeFactoryInitializationException on Exception
 	 */
 	public final Map<String, Object> populateDataTypeMap(String xmlFileName)
 			throws XQueryDataTypeInitializationException
 	{
 		xqueryAttributeTypeMap = new HashMap<String, Object>();
 
 		SAXReader saxReader = new SAXReader();
 		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(xmlFileName);
 
 		Document document = null;
 
 		try
 		{
 			document = saxReader.read(inputStream);
 			Element name = null;
 			Element xqueryDataType = null;
 
 			Element xQueryAttributesElement = document.getRootElement();
 			Iterator xQueryDataTypeElementIterator = xQueryAttributesElement
 					.elementIterator("XQuery-Datatype");
 
 			Element xQueryAttributeElement = null;
 
 			while (xQueryDataTypeElementIterator.hasNext())
 			{
 				xQueryAttributeElement = (Element) xQueryDataTypeElementIterator.next();
 
 				name = xQueryAttributeElement.element("name");
 				xqueryDataType = xQueryAttributeElement.element("datatype");
 
 				XQueryDataTypeInformation xqueryDataTypeInfo = new XQueryDataTypeInformation();
 				xqueryDataTypeInfo.setName(name.getStringValue());
 				xqueryDataTypeInfo.setDataType(xqueryDataType.getStringValue());
 
 				xqueryAttributeTypeMap.put(name.getStringValue(), xqueryDataTypeInfo);
 			}
 		}
 		catch (DocumentException documentException)
 		{
 			throw new XQueryDataTypeInitializationException(documentException);
 		}
 
 		return xqueryAttributeTypeMap;
 	}
 
 	/**
 	 * This method returns the name of the Database Data type given the name
 	 * of the corresponding Primitive attribute.
 	 * @param primitiveAttribute The name of the primitive attribute
 	 * @return String The name of Database data type
 	 * @throws DataTypeFactoryInitializationException If dataTypeMap is not populated
 	 */
 	public String getDataType(String primitiveAttribute)
 			throws XQueryDataTypeInitializationException
 	{
 		String xQuerydataType = null;
 		if (xqueryAttributeTypeMap != null)
 		{
 			XQueryDataTypeInformation dataTypeInfo = (XQueryDataTypeInformation) xqueryAttributeTypeMap
 					.get(primitiveAttribute);
 			xQuerydataType = (dataTypeInfo != null) ? dataTypeInfo.getDataType() : null;
 		}
 		else
 		{
 			throw new XQueryDataTypeInitializationException("Cannot find populated dataType Map.");
 		}
 
 		return xQuerydataType;
 	}
 }
