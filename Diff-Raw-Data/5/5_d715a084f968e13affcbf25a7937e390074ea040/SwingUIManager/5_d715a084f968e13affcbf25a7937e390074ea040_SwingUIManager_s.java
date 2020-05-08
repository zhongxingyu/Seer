 /**
  *<p>Copyright: (c) Washington University, School of Medicine 2006.</p>
  *<p>Company: Washington University, School of Medicine, St. Louis.</p>
  *<p>ClassName: edu.wustl.cab2b.client.ui.main.SwingUIManager </p> 
  */
 
 package edu.wustl.cab2b.client.ui.main;
 
 import java.awt.Dimension;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ByteArrayAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.wustl.cab2b.common.errorcodes.ErrorCodeConstants;
 import edu.wustl.cab2b.common.exception.CheckedException;
 
 /**
  * This class is used for creating UI panel that shows attribute name, data-type
  * conditions and components for entering/showing the values.
  * 
  * @author Kaushal Kumar
  * @version 1.0
  */
 public class SwingUIManager {
     /**
      * This method returns a panel object that shows attribute name, data-type
      * conditions and components for entering/showing the values.
      * 
      * @param parseFile
      * @param attributeEntity
      * @param maxLabelDimension
      * @return panel object
      * @throws CheckedException
      */
     public static Object generateUIPanel(ParseXMLFile parseFile, AttributeInterface attributeEntity,
                                          Dimension maxLabelDimension) throws CheckedException {
         Object[] object = new Object[2];
         String className = null;
         AttributeTypeInformationInterface attributeTypeInformation = attributeEntity.getAttributeTypeInformation();
         String dataTypeString = getDataType(attributeTypeInformation);
 
         // Check if attribute has value domain (i.e it is enum)
         if (true == edu.wustl.cab2b.common.util.Utility.isEnumerated(attributeEntity)) {
             object[0] = parseFile.getEnumConditionList(dataTypeString);
             className = parseFile.getEnumClassName(dataTypeString);
         } else { // If an entity is of non-enumerated type
             object[0] = parseFile.getNonEnumConditionList(dataTypeString);
             className = parseFile.getNonEnumClassName(dataTypeString);
         }
         object[1] = maxLabelDimension;
 
         Object uiObject = null;
         try {
             Class[] arr = { ArrayList.class, Dimension.class };
             Class<?> clazz = Class.forName(className);
             Constructor<?> constructor = clazz.getDeclaredConstructor(arr);
             uiObject = constructor.newInstance(object);
         } catch (Exception e) {
             throw new CheckedException(e.getMessage(), e, ErrorCodeConstants.RF_0001);
         }
         return uiObject;
     }
 
     /**
      * This method returns the datatype of the given AttributeTypeInformation
      * 
      * @param attributeTypeInformation
      * @return the datatype
      */
     public static String getDataType(AttributeTypeInformationInterface attributeTypeInformation) {
         String dataTypeString = null;
 
         if ((attributeTypeInformation instanceof StringAttributeTypeInformation)
                || (attributeTypeInformation instanceof ByteArrayAttributeTypeInformation)) {
             dataTypeString = "string";
         } else if ((attributeTypeInformation instanceof IntegerAttributeTypeInformation)
                 || (attributeTypeInformation instanceof LongAttributeTypeInformation)
                 || (attributeTypeInformation instanceof FloatAttributeTypeInformation)
                 || (attributeTypeInformation instanceof DoubleAttributeTypeInformation)
                 || (attributeTypeInformation instanceof NumericAttributeTypeInformation)
                 || (attributeTypeInformation instanceof ShortAttributeTypeInformation)) {
             dataTypeString = "number";
         } else if (attributeTypeInformation instanceof BooleanAttributeTypeInformation) {
             dataTypeString = "boolean";
         } else if (attributeTypeInformation instanceof DateAttributeTypeInformation) {
             dataTypeString = "date";
         }
         return dataTypeString;
     }
 
 }
