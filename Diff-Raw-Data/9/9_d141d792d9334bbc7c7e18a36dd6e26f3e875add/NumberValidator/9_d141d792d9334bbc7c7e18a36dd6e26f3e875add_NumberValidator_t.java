 
 package edu.common.dynamicextensions.validation;
 
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 
 /**
  * This Class validates the the numeric data entered by the user. It checks whether the value entered is numeric or not.
  * @author chetan_patil
  * @version 1.0
  */
 public class NumberValidator implements ValidatorRuleInterface
 {
 
 	/**
 	 * This method implements the validate method of the ValidatorRuleInterface.
 	 * This method validates the numeric data entered by the user on the User Interface.
 	 * @param attribute the Attribute whose corresponding value is to be verified.
 	 * @param valueObject the value entered by the user.
 	 * @param parameterMap the parameters of the Rule.
 	 * @throws DynamicExtensionsValidationException if the value is not following the Numeric Rule. 
 	 */
 	public boolean validate(AttributeMetadataInterface attribute, Object valueObject,
 			Map<String, String> parameterMap) throws DynamicExtensionsValidationException
 	{
 		boolean isValid = true;
 		AttributeTypeInformationInterface attributeTypeInformation = attribute
 				.getAttributeTypeInformation();
 		String attributeName = attribute.getName();
 
 		String value = (String) valueObject;
 		value = value.trim();
 		
 		if (attributeTypeInformation != null)
 		{
 			try
 			{
 				if (!(DynamicExtensionsUtility.isNumeric((String) valueObject)) )
 						{
 					isValid=false;
 						}
				else if (attributeTypeInformation instanceof LongAttributeTypeInformation)
 				{
 					checkIntegerNumberValidity(attributeName, value.trim());
 				}
 				else if (attributeTypeInformation instanceof DoubleAttributeTypeInformation)
 				{
 					checkRealNumberValidity(attributeName, value.trim());
 				}
 			}
 			catch (NumberFormatException numberFormatException)
 			{
 				isValid = false;
 			}
 		}
 		if(value.isEmpty())
 		{
 			isValid = true;
 		}
 		if(!isValid)
 		{
 			throw new DynamicExtensionsValidationException("Validation failed", null,
 					"dynExtn.validation.Number", attributeName);
 		}
 
 		return isValid;
 	}
 
 	/**
 	 * This method checks the validty of the Integer values.
 	 * @param attributeName Name of the Attribute.
 	 * @param value The value to be verified
 	 * @throws DynamicExtensionsValidationException if the value is not in the numeric range of Long.
 	 * @throws NumberFormatException if the value is not of numeric nature.
 	 */
 	private void checkIntegerNumberValidity(String attributeName, String value)
 			throws DynamicExtensionsValidationException, NumberFormatException
 	{
         if (value.contains("."))
         {
             if (DynamicExtensionsUtility.isNumeric(value) && (value.indexOf(".") != -1))
             {
                 //Integer decimalPlaces = 0;
                 StringBuffer decimalFormat = new StringBuffer("#");
                 NumberFormat numberFormat = new DecimalFormat(decimalFormat.toString());
                 value = numberFormat.format(Double.parseDouble(value));
             }
         }
         
 		BigInteger numberValue = new BigInteger(value);
 		String strLongMin = (new Long(Long.MIN_VALUE)).toString();
 		String strLongMax = (new Long(Long.MAX_VALUE)).toString();
 		BigInteger longMin = new BigInteger(strLongMin);
 		BigInteger longMax = new BigInteger(strLongMax);
 
 		if (numberValue.compareTo(longMin) < 0 || numberValue.compareTo(longMax) > 0)
 		{
 			List<String> placeHolders = new ArrayList<String>();
 			placeHolders.add(attributeName);
 			placeHolders.add(strLongMin);
 			placeHolders.add(strLongMax);
 			throw new DynamicExtensionsValidationException("Validation failed", null,
 					"dynExtn.validation.Number.Range", placeHolders);
 		}
 	}
 
 	/**
 	 * This method checks the validty of the Real values.
 	 * @param attributeName Name of the Attribute.
 	 * @param value The value to be verified
 	 * @throws DynamicExtensionsValidationException if the value is not in the numeric range of Double.
 	 * @throws NumberFormatException if the value is not of numeric nature. 
 	 */
 	private void checkRealNumberValidity(String attributeName, String value)
 			throws DynamicExtensionsValidationException, NumberFormatException
 	{
 		BigDecimal numberValue = new BigDecimal(value);
 		String strDoubleMin = (new Double(-Double.MAX_VALUE)).toString();
 		String strDoubleMax = (new Double(Double.MAX_VALUE)).toString();
 		BigDecimal doubleMin = new BigDecimal(strDoubleMin);
 		BigDecimal doubleMax = new BigDecimal(strDoubleMax);
 
 		if (numberValue.compareTo(doubleMin) < 0 || numberValue.compareTo(doubleMax) > 0)
 		{
 			List<String> placeHolders = new ArrayList<String>();
 			placeHolders.add(attributeName);
 			placeHolders.add(strDoubleMin);
 			placeHolders.add(strDoubleMax);
 			throw new DynamicExtensionsValidationException("Validation failed", null,
 					"dynExtn.validation.Number.Range", placeHolders);
 		}
 	}
 
 }
