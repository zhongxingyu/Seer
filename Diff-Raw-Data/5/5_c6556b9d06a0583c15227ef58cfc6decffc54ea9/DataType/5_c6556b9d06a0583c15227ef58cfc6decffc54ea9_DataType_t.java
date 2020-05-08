 package edu.wustl.cab2b.server.path;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.BooleanValueInterface;
 import edu.common.dynamicextensions.domaininterface.CaDSRValueDomainInfoInterface;
 import edu.common.dynamicextensions.domaininterface.DateValueInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleValueInterface;
 import edu.common.dynamicextensions.domaininterface.FloatValueInterface;
 import edu.common.dynamicextensions.domaininterface.IntegerValueInterface;
 import edu.common.dynamicextensions.domaininterface.LongValueInterface;
 import edu.common.dynamicextensions.domaininterface.ShortValueInterface;
 import edu.common.dynamicextensions.domaininterface.StringValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.util.global.DEConstants.ValueDomainType;
 import edu.wustl.cab2b.server.util.DynamicExtensionUtility;
 import gov.nih.nci.cagrid.metadata.common.Enumeration;
 import gov.nih.nci.cagrid.metadata.common.UMLAttribute;
 import gov.nih.nci.cagrid.metadata.common.ValueDomain;
 
 /**
  * Enumeration for DataType.
  * It also provides method to get this enumeration based on String Data-type
  * @author Chandrakant Talele
  */
 enum DataType {
 
     STRING() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createStringAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     StringValueInterface value = domainObjectFactory.createStringValue();
                     value.setValue(e.getPermissibleValue());
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
 
     INTEGER() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createIntegerAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     IntegerValueInterface value = domainObjectFactory.createIntegerValue();
                    value.setValue(Integer.valueOf(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
 
     DATE() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createDateAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     DateValueInterface value = domainObjectFactory.createDateValue();
                     //TODO what is meaning of permissible values for Date ??? 
                     //Not clear about date format string
                     logger.info("Date Attribute has permissible value : " + e.getPermissibleValue());
                     //value.setValue(new Date(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     FLOAT() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createFloatAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     FloatValueInterface value = domainObjectFactory.createFloatValue();
                     value.setValue(new Float(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     BOOLEAN() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createBooleanAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     BooleanValueInterface value = domainObjectFactory.createBooleanValue();
                     logger.info("boolean Attribute has permissible value : " + e.getPermissibleValue());
                     //TODO what is meaning of permissible values for boolean???
                     //is any string processing needed??
                    value.setValue(Boolean.valueOf(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     LONG() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createLongAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     LongValueInterface value = domainObjectFactory.createLongValue();
                     value.setValue(new Long(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
     },
     DOUBLE() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createDoubleAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     DoubleValueInterface value = domainObjectFactory.createDoubleValue();
                     value.setValue(new Double(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
 
     },
     SHORT() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          * @see DataType#createAttribute(UMLAttribute)
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createShortAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
 
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 UserDefinedDEInterface userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
                 for (Enumeration e : arr) {
                     ShortValueInterface value = domainObjectFactory.createShortValue();
                     value.setValue(new Short(e.getPermissibleValue()));
                     DynamicExtensionUtility.setSemanticMetadata(value, e.getSemanticMetadata());
                     userDefinedDE.addPermissibleValue(value);
                 }
                 attribute.getAttributeTypeInformation().setDataElement(userDefinedDE);
             }
             return attribute;
         }
 
     },
     OBJECT() {
         /**
          * Creates and populates attribute from UMLAttribute
          * @param umlAttribute
          * @return 
          */
         public AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
             AttributeInterface attribute = domainObjectFactory.createObjectAttribute();
             ValueDomain valueDomain = umlAttribute.getValueDomain();
             Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
             if (arr != null) {
                 for (Enumeration e : arr) {
                     //for the time being setting this as error to get all such attributes
                     logger.error("For attribute of Object type found permissible value : " + e.getPermissibleValue());
                 }
             }
             return attribute;
         }
 
     }
     ;
 
     /**
      * Builds enumeration
      */
     DataType() {
     	// TODO Auto-generated constructor stub
     }
 
     private static Map<String, DataType> dataTypeMap = new HashMap<String, DataType>();
 
     private static DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
     private static final Logger logger = edu.wustl.common.util.logger.Logger.getLogger(DataType.class);
     static {
         //put data type strings in small case
         dataTypeMap.put("number", DataType.INTEGER);
         dataTypeMap.put("java.lang.integer", DataType.INTEGER);
         dataTypeMap.put("int", DataType.INTEGER);
         dataTypeMap.put("java.lang.string", DataType.STRING);
         dataTypeMap.put("string", DataType.STRING);
         dataTypeMap.put("alphanumeric", DataType.STRING);
         dataTypeMap.put("character", DataType.STRING);
         dataTypeMap.put("java.util.date", DataType.DATE);
         dataTypeMap.put("date", DataType.DATE);
         dataTypeMap.put("java.lang.float", DataType.FLOAT);
         dataTypeMap.put("float", DataType.FLOAT);
         dataTypeMap.put("java.lang.boolean", DataType.BOOLEAN);
         dataTypeMap.put("boolean", DataType.BOOLEAN);
         dataTypeMap.put("java.lang.long", DataType.LONG);
         dataTypeMap.put("long", DataType.LONG);
         dataTypeMap.put("java.lang.double", DataType.DOUBLE);
         dataTypeMap.put("double", DataType.DOUBLE);
         dataTypeMap.put("java.lang.short", DataType.SHORT);
         dataTypeMap.put("short", DataType.SHORT);
 //        dataTypeMap.put("java.lang.object", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.collection", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.vector", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.arrayList", DataType.UNDEFINED);
 //        dataTypeMap.put("java.util.hashSet", DataType.UNDEFINED);
         
     }
 
     /**
      * Method which creates dynamic extension attribute based on data-type. 
      * Each enumeration must provide implement of this method.    
      * @param umlAttribute source UML attribute 
      * @return the newly created dynamic extension attribute.
      */
     public final AttributeInterface createAttribute(UMLAttribute umlAttribute) {
         AttributeInterface attribute = createAndPopulateAttribute(umlAttribute);
         if (attribute != null) {
             postProcessing(attribute, umlAttribute);
         }
         return attribute;
     }
 
     /**
      * Creates and populates attribute from UMLAttribute
      * @param umlAttribute
      * @return 
      */
     AttributeInterface createAndPopulateAttribute(UMLAttribute umlAttribute) {
         // TODO bypassing attributes, need to decide how to handle it.
         logger.error("found attribute with type" + umlAttribute.getDataTypeName() + ". Not storing it");
         return null;
     }
 
     private void postProcessing(AttributeInterface attribute, UMLAttribute umlAttribute) {
         attribute.setPublicId(Long.toString(umlAttribute.getPublicID()));
         ValueDomain valueDomain = umlAttribute.getValueDomain();
         CaDSRValueDomainInfoInterface valueDomainInfo = domainObjectFactory.createCaDSRValueDomainInfo();
         valueDomainInfo.setDatatype(umlAttribute.getDataTypeName());
         valueDomainInfo.setName(valueDomain.getLongName());
         valueDomainInfo.setValueDomainType(ValueDomainType.NON_ENUMERATED);
         Enumeration[] arr = valueDomain.getEnumerationCollection().getEnumeration();
         if (arr != null && arr.length != 0) {
             valueDomainInfo.setValueDomainType(ValueDomainType.ENUMERATED);
         }
         attribute.setCaDSRValueDomainInfo(valueDomainInfo);
     }
 
     /**
      * Returns the enumeration for input String.
      * @param value
      * @return Returns the DataType
      */
     public static DataType get(String value) {
 
         DataType dataType = dataTypeMap.get(value.toLowerCase());
         if (dataType == null) {
             //throw new RuntimeException("unknown data-type found : " + value);
             //for the time being setting this as error to get all such attributes
             logger.warn("Found attribute with type : " + value + ". Creating Object type attribute for it.");
             return  DataType.OBJECT;
         }
         return dataType;
     }
 
 }
