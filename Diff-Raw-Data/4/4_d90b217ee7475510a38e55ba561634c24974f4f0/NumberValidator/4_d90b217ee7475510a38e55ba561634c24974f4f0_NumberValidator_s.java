 
 package edu.common.dynamicextensions.validation;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.NumericAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.entitymanager.DataTypeFactory;
 import edu.common.dynamicextensions.entitymanager.DataTypeInformation;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 import edu.common.dynamicextensions.exception.DataTypeFactoryInitializationException;
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
 	 * @throws DataTypeFactoryInitializationException
 	 */
 	public boolean validate(AttributeMetadataInterface attribute, Object valueObject,
 			Map<String, String> parameterMap, String controlCaption)
 			throws DynamicExtensionsValidationException, DataTypeFactoryInitializationException
 	{
 		boolean isValid = true;
 		AttributeTypeInformationInterface attributeTypeInformation = attribute
 				.getAttributeTypeInformation();
 
 		String value = (String) valueObject;
 		if (value != null)
 		{
 			value = value.trim();
 		}
 		if (value == null || value.length() == 0)
 		{
 			isValid = true;
 		}
 		else
 		{
 
 			DataTypeFactory dataTypeFactory = null;
 
 			try
 			{
 				dataTypeFactory = DataTypeFactory.getInstance();
 			}
 			catch (DataTypeFactoryInitializationException e)
 			{
 				throw new DataTypeFactoryInitializationException(e.getMessage(),e);
 			}
 
 			if (attributeTypeInformation != null)
 			{
 				try
 				{
 					if (!(DynamicExtensionsUtility.isNumeric((String) valueObject)))
 					{
 						isValid = false;
 					}
 					else if (attributeTypeInformation instanceof LongAttributeTypeInformation)
 					{
 						checkLongNumberValidity(dataTypeFactory, controlCaption, value);
 					}
 					else if (attributeTypeInformation instanceof IntegerAttributeTypeInformation)
 					{
 						checkIntegerNumberValidity(dataTypeFactory, controlCaption, value);
 					}
 					else if (attributeTypeInformation instanceof ShortAttributeTypeInformation)
 					{
 						checkShortNumberValidity(dataTypeFactory, controlCaption, value);
 					}
 					else if (attributeTypeInformation instanceof DoubleAttributeTypeInformation)
 					{
 						validateDigitsAfterDecimalAgainstPrecision(attributeTypeInformation,
 								controlCaption, value.trim());
 						checkDoubleNumberValidity(dataTypeFactory, controlCaption, value);
 					}
 					else if (attributeTypeInformation instanceof FloatAttributeTypeInformation)
 					{
 						validateDigitsAfterDecimalAgainstPrecision(attributeTypeInformation,
 								controlCaption, value.trim());
 						checkFloatNumberValidity(dataTypeFactory, controlCaption, value);
 					}
 				}
 				catch (NumberFormatException numberFormatException)
 				{
 					isValid = false;
 				}
 			}
 		}
 
 		if (!isValid)
 		{
 			throw new DynamicExtensionsValidationException("Validation failed", null,
 					"dynExtn.validation.Number", controlCaption);
 		}
 
 		return isValid;
 	}
 
 	/**
 	 * This method checks the validity of the Long values.
 	 * @param attributeName Name of the Attribute.
 	 * @param value The value to be verified
 	 * @throws DynamicExtensionsValidationException if the value is not in the numeric range of Long.
 	 * @throws NumberFormatException if the value is not of numeric nature.
 	 */
 	private void checkLongNumberValidity(DataTypeFactory dataTypeFactory, String controlCaption,
 			String value) throws DynamicExtensionsValidationException, NumberFormatException
 	{
 		if (value.contains("."))
 		{
 			reportInvalidInput(controlCaption, EntityManagerConstantsInterface.REAL_ATTRIBUTE_TYPE,
 					EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE,
 					"dynExtn.validation.Number.errorInput");
 		}
 
 		validatePrecisionAndScale(dataTypeFactory, value, controlCaption,
 				EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE);
 
 		BigInteger numberValue = new BigInteger(value);
 		String strLongMin = new Long(Long.MIN_VALUE).toString();
 		String strLongMax = new Long(Long.MAX_VALUE).toString();
 		BigInteger longMin = new BigInteger(strLongMin);
 		BigInteger longMax = new BigInteger(strLongMax);
 
 		if (numberValue.compareTo(longMin) < 0 || numberValue.compareTo(longMax) > 0)
 		{
 			reportInvalidInput(controlCaption, strLongMin, strLongMax,
 					"dynExtn.validation.Number.Range");
 		}
 	}
 
 	/**
 	 * @param dataTypeFactory
 	 * @param controlCaption
 	 * @param value
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void checkIntegerNumberValidity(DataTypeFactory dataTypeFactory, String controlCaption,
 			String value) throws DynamicExtensionsValidationException
 	{
 		if (value.contains("."))
 		{
 			reportInvalidInput(controlCaption, EntityManagerConstantsInterface.REAL_ATTRIBUTE_TYPE,
 					EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE,
 					"dynExtn.validation.Number.errorInput");
 		}
 
 		validatePrecisionAndScale(dataTypeFactory, value, controlCaption,
 				EntityManagerConstantsInterface.INTEGER_ATTRIBUTE_TYPE);
 
 		BigInteger numberValue = new BigInteger(value);
		String strIntegerMin = new Integer(Integer.MIN_VALUE).toString();
		String strIntegerMax = new Integer(Integer.MAX_VALUE).toString();
 		BigInteger integerMin = new BigInteger(strIntegerMin);
 		BigInteger integerMax = new BigInteger(strIntegerMax);
 
 		if (numberValue.compareTo(integerMin) < 0 || numberValue.compareTo(integerMax) > 0)
 		{
 			reportInvalidInput(controlCaption, strIntegerMin, strIntegerMax,
 					"dynExtn.validation.Number.Range");
 		}
 	}
 
 	/**
 	 * @param dataTypeFactory
 	 * @param controlCaption
 	 * @param value
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void checkShortNumberValidity(DataTypeFactory dataTypeFactory, String controlCaption,
 			String value) throws DynamicExtensionsValidationException
 	{
 		if (value.contains("."))
 		{
 			reportInvalidInput(controlCaption, EntityManagerConstantsInterface.REAL_ATTRIBUTE_TYPE,
 					EntityManagerConstantsInterface.LONG_ATTRIBUTE_TYPE,
 					"dynExtn.validation.Number.errorInput");
 		}
 
 		validatePrecisionAndScale(dataTypeFactory, value, controlCaption,
 				EntityManagerConstantsInterface.SHORT_ATTRIBUTE_TYPE);
 
 		BigInteger numberValue = new BigInteger(value);
 		String strShortMin = new Short(Short.MIN_VALUE).toString();
 		String strShortMax = new Short(Short.MAX_VALUE).toString();
 		BigInteger shortMin = new BigInteger(strShortMin);
 		BigInteger shortMax = new BigInteger(strShortMax);
 
 		if (numberValue.compareTo(shortMin) < 0 || numberValue.compareTo(shortMax) > 0)
 		{
 			reportInvalidInput(controlCaption, strShortMin, strShortMax,
 					"dynExtn.validation.Number.Range");
 		}
 	}
 
 	/**
 	 * @param dataTypeFactory
 	 * @param controlCaption
 	 * @param value
 	 * @throws DynamicExtensionsValidationException
 	 * @throws NumberFormatException
 	 */
 	private void checkDoubleNumberValidity(DataTypeFactory dataTypeFactory, String controlCaption,
 			String value) throws DynamicExtensionsValidationException, NumberFormatException
 	{
 		validatePrecisionAndScale(dataTypeFactory, value, controlCaption,
 				EntityManagerConstantsInterface.DOUBLE_ATTRIBUTE_TYPE);
 
 		/*Double numberValue = new Double(value);
 		String strDoubleMin = (new Double(-Double.MAX_VALUE)).toString();
 		String strDoubleMax = (new Double(Double.MAX_VALUE)).toString();
 		Double doubleMin = new Double(strDoubleMin);
 		Double doubleMax = new Double(strDoubleMax);
 
 		if (numberValue.compareTo(doubleMin) < 0 || numberValue.compareTo(doubleMax) > 0)
 		{
 			reportInvalidInput(controlCaption, strDoubleMin, strDoubleMax, "dynExtn.validation.Number.Range");
 		}*/
 	}
 
 	/**
 	 * @param dataTypeFactory
 	 * @param controlCaption
 	 * @param value
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void checkFloatNumberValidity(DataTypeFactory dataTypeFactory, String controlCaption,
 			String value) throws DynamicExtensionsValidationException
 	{
 		validatePrecisionAndScale(dataTypeFactory, value, controlCaption,
 				EntityManagerConstantsInterface.FLOAT_ATTRIBUTE_TYPE);
 
 		/*Float numberValue = new Float(value);
 		String strFloatMin = (new Float(-Float.MAX_VALUE)).toString();
 		String strFloatMax = (new Float(Float.MAX_VALUE)).toString();
 		Float floatMin = new Float(strFloatMin);
 		Float floatMax = new Float(strFloatMax);
 
 		if (numberValue.compareTo(floatMin) < 0 || numberValue.compareTo(floatMax) > 0)
 		{
 			reportInvalidInput(controlCaption, strFloatMin, strFloatMax, "dynExtn.validation.Number.Range");
 		}*/
 	}
 
 	/**
 	 * @param dataTypeFactory
 	 * @param value
 	 * @param controlCaption
 	 * @param dataType
 	 * @throws DynamicExtensionsValidationException
 	 */
 	public void validatePrecisionAndScale(DataTypeFactory dataTypeFactory, String value,
 			String controlCaption, String dataType) throws DynamicExtensionsValidationException
 	{
 		DataTypeInformation dataTypeInfoObject = (DataTypeInformation) dataTypeFactory
 				.getDataTypePrecisionScaleInformation(dataType);
 
 		if (value != null && dataTypeInfoObject != null)
 		{
 			if (value.contains("."))
 			{
 				int decimalPointIndex = value.indexOf(".");
 				String stringBeforeDecimalPoint = value.substring(0, decimalPointIndex);
 				String stringAfterDecimalPoint = value.substring(decimalPointIndex + 1, value
 						.length());
 
 				if (stringBeforeDecimalPoint.length() > Integer.parseInt(dataTypeInfoObject
 						.getDigitsBeforeDecimal())
 						|| stringAfterDecimalPoint.length() > Integer.parseInt(dataTypeInfoObject
 								.getDigitsAfterDecimal()))
 				{
 					reportInvalidInput(controlCaption, dataType, dataTypeInfoObject,
 							"dynExtn.validation.Number.PrecisionScale");
 				}
 			}
 			else
 			{
 				if (value.length() > Integer.parseInt(dataTypeInfoObject.getDigitsBeforeDecimal()))
 				{
 					reportInvalidInput(controlCaption, dataType, dataTypeInfoObject,
 							"dynExtn.validation.Number.PrecisionScale");
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param controlCaption
 	 * @param dataType
 	 * @param precisionScaleInfoObject
 	 * @param errorKey
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void reportInvalidInput(String controlCaption, String dataType,
 			DataTypeInformation precisionScaleInfoObject, String errorKey)
 			throws DynamicExtensionsValidationException
 	{
 		List<String> placeHolders = new ArrayList<String>();
 		placeHolders.add(controlCaption + " (type: " + dataType + ") ");
 		placeHolders.add(precisionScaleInfoObject.getDigitsBeforeDecimal());
 		placeHolders.add(precisionScaleInfoObject.getDigitsAfterDecimal());
 		throw new DynamicExtensionsValidationException("Validation failed", null, errorKey,
 				placeHolders);
 	}
 
 	/**
 	 * @param controlCaption
 	 * @param value1
 	 * @param value2
 	 * @param errorKey
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void reportInvalidInput(String controlCaption, String value1, String value2,
 			String errorKey) throws DynamicExtensionsValidationException
 	{
 		List<String> placeHolders = new ArrayList<String>();
 		placeHolders.add(controlCaption);
 		placeHolders.add(value1);
 		placeHolders.add(value2);
 		throw new DynamicExtensionsValidationException("Validation failed", null, errorKey,
 				placeHolders);
 	}
 
 	/**
 	 * @param attributeTypeInformation
 	 * @param controlCaption
 	 * @param value
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private void validateDigitsAfterDecimalAgainstPrecision(
 			AttributeTypeInformationInterface attributeTypeInformation, String controlCaption,
 			String value) throws DynamicExtensionsValidationException
 	{
 		int decimalPlaces = ((NumericAttributeTypeInformation) attributeTypeInformation)
 				.getDecimalPlaces();
 
 		if (value != null)
 		{
 			if (value.contains("."))
 			{
 				int decimalPointIndex = value.indexOf(".");
 				String stringAfterDecimalPoint = value.substring(decimalPointIndex + 1, value
 						.length());
 
 				if (stringAfterDecimalPoint.length() > decimalPlaces)
 				{
 					reportInvalidInput(controlCaption, "Number of digits after decimal point",
 							"precision (" + decimalPlaces + ")",
 							"dynExtn.validation.Number.numberOfDigitsExceedsPrecision");
 				}
 			}
 		}
 	}
 
 }
