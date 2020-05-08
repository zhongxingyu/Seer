 
 package edu.common.dynamicextensions.domain;
 
 import java.text.DecimalFormat;
 import java.text.NumberFormat;
 
 import edu.common.dynamicextensions.domaininterface.DoubleTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleValueInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManagerConstantsInterface;
 
 /**
  * @hibernate.joined-subclass table="DYEXTN_DOUBLE_TYPE_INFO" 
  * @hibernate.joined-subclass-key column="IDENTIFIER"  
  * @author sujay_narkar
  *
  */
 public class DoubleAttributeTypeInformation extends NumericAttributeTypeInformation
 		implements
 			DoubleTypeInformationInterface
 {
 
 	/**
 	 * Serial version id.
 	 */
 	private static final long serialVersionUID = 7901101750879429344L;
 
 	/** 
 	 * @see edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface#getDataType()
 	 */
 	public String getDataType()
 	{
 
 		return EntityManagerConstantsInterface.DOUBLE_ATTRIBUTE_TYPE;
 	}
 
 	/**
 	 * 
 	 */
 	public PermissibleValueInterface getPermissibleValueForString(String value)
 	{
 		DomainObjectFactory factory = DomainObjectFactory.getInstance();
 		DoubleValueInterface doubleValue = factory.createDoubleValue();
 		doubleValue.setValue(new Double(value));
 
 		return doubleValue;
 	}
 	/**
 	 * 
 	 */
 	public String getFormattedValue(Double value)
 	{
 		String formattedValue = "";
 		if (value != null)
 		{
 			if (this.decimalPlaces.intValue() > 0)
 			{
 					DecimalFormat formatDecimal = (DecimalFormat) NumberFormat
 							.getNumberInstance();
 					formatDecimal.setParseBigDecimal(true);
 					formatDecimal.setMaximumFractionDigits(this.decimalPlaces
 							.intValue());
 					formattedValue = formatDecimal.format(value);
 			}
 			else
 			{
				formattedValue = value.toString();
 			} 
 		}
 		return formattedValue;
 	}
 }
