 
 package edu.common.dynamicextensions.processor;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import edu.common.dynamicextensions.domain.BooleanAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.BooleanValue;
 import edu.common.dynamicextensions.domain.ByteArrayAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DateValue;
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domain.DoubleAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.DoubleValue;
 import edu.common.dynamicextensions.domain.FileAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FileExtension;
 import edu.common.dynamicextensions.domain.FloatAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.FloatValue;
 import edu.common.dynamicextensions.domain.IntegerAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.IntegerValue;
 import edu.common.dynamicextensions.domain.LongAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.LongValue;
 import edu.common.dynamicextensions.domain.PermissibleValue;
 import edu.common.dynamicextensions.domain.ShortAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.ShortValue;
 import edu.common.dynamicextensions.domain.StringAttributeTypeInformation;
 import edu.common.dynamicextensions.domain.StringValue;
 import edu.common.dynamicextensions.domain.UserDefinedDE;
 import edu.common.dynamicextensions.domaininterface.AbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.BooleanValueInterface;
 import edu.common.dynamicextensions.domaininterface.DataElementInterface;
 import edu.common.dynamicextensions.domaininterface.DateValueInterface;
 import edu.common.dynamicextensions.domaininterface.DoubleValueInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.FloatValueInterface;
 import edu.common.dynamicextensions.domaininterface.IntegerValueInterface;
 import edu.common.dynamicextensions.domaininterface.LongValueInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.RoleInterface;
 import edu.common.dynamicextensions.domaininterface.SemanticPropertyInterface;
 import edu.common.dynamicextensions.domaininterface.ShortValueInterface;
 import edu.common.dynamicextensions.domaininterface.StringValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.entitymanager.EntityManager;
 import edu.common.dynamicextensions.entitymanager.EntityManagerInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.ui.interfaces.AbstractAttributeUIBeanInterface;
 import edu.common.dynamicextensions.ui.util.Constants;
 import edu.common.dynamicextensions.ui.util.ControlConfigurationsFactory;
 import edu.common.dynamicextensions.ui.util.RuleConfigurationObject;
 import edu.common.dynamicextensions.ui.util.SemanticPropertyBuilderUtil;
 import edu.common.dynamicextensions.ui.webui.util.OptionValueObject;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.util.global.Constants.AssociationDirection;
 import edu.common.dynamicextensions.util.global.Constants.AssociationType;
 import edu.common.dynamicextensions.util.global.Constants.Cardinality;
 import edu.wustl.common.beans.NameValueBean;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * @author preeti_munot
  *
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class AttributeProcessor extends BaseDynamicExtensionsProcessor
 {
 
 	/**
 	 * Protected constructor for attribute processor
 	 *
 	 */
 	protected AttributeProcessor()
 	{
 	}
 
 	/**
 	 * this method gets the new instance of the entity processor to the caller.
 	 * @return EntityProcessor EntityProcessor instance
 	 */
 	public static AttributeProcessor getInstance()
 	{
 		return new AttributeProcessor();
 	}
 
 	/**
 	 * Creates a new AttributeInterface object based on the Datatype.
 	 * If datatype is "DATATYPE_STRING" get a new instance of String attribute from DomainObjectFactory
 	 * and return it.
 	 * Similarly for each Datatype a new Attribute object is created and returned back.
 	 * @param attributeUIBeanInformationIntf : UI Bean Information interface object that contains information of
 	 * datatype selected by the user on the UI.
 	 * @return New (Domain Object) Attribute object based on datatype
 	 * @throws DynamicExtensionsApplicationException  : Exception
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public AbstractAttributeInterface createAttribute(AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf,
 			AttributeTypeInformationInterface... attrTypeInformation) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		AbstractAttributeInterface attribute = null;
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		if (attributeUIBeanInformationIntf != null)
 		{
 			String displayChoice = attributeUIBeanInformationIntf.getDisplayChoice();
 			if ((displayChoice != null) && (displayChoice.equals(ProcessorConstants.DISPLAY_CHOICE_LOOKUP)))
 			{
 				attribute = domainObjectFactory.createAssociation();
 			}
 			else
 			{
 				String attributeType = attributeUIBeanInformationIntf.getDataType();
 				if (attributeType != null)
 				{
 					if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_STRING))
 					{
 						attribute = domainObjectFactory.createStringAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_DATE))
 					{
 						attribute = domainObjectFactory.createDateAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_BOOLEAN))
 					{
 						attribute = domainObjectFactory.createBooleanAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_BYTEARRAY))
 					{
 						attribute = domainObjectFactory.createByteArrayAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_FILE))
 					{
 						attribute = domainObjectFactory.createFileAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_NUMBER))
 					{
 						int noOfDecimals = DynamicExtensionsUtility.convertStringToInt(attributeUIBeanInformationIntf.getAttributeDecimalPlaces());
 						attribute = createAttributeForNumericDataType(noOfDecimals);
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_SHORT))
 					{
 						attribute = domainObjectFactory.createShortAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_INTEGER))
 					{
 						attribute = domainObjectFactory.createIntegerAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_LONG))
 					{
 						attribute = domainObjectFactory.createLongAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_FLOAT))
 					{
 						attribute = domainObjectFactory.createFloatAttribute();
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_DOUBLE))
 					{
 						attribute = domainObjectFactory.createDoubleAttribute();
 					}
 				}
 			}
 		}
 
 		return attribute;
 	}
 
 	private RoleInterface getRole(AssociationType associationType, String name, Cardinality minCard, Cardinality maxCard)
 	{
 		RoleInterface role = DomainObjectFactory.getInstance().createRole();
 		role.setAssociationsType(associationType);
 		role.setName(name);
 		role.setMinimumCardinality(minCard);
 		role.setMaximumCardinality(maxCard);
 		return role;
 	}
 
 	/**
 	 * This method populates the Attribute Interface objects with appropriate information based on its type.
 	 * Each attribute object has different relevant information to be filled in based on the interface it implements
 	 * This method accepts an AbstractAttributeInterface object and populates required fields.
 	 * Information to be filled is available in the  AbstractAttributeUIBeanInterface object which is populated
 	 * in the UI.
 	 * @param attributeInterface : Attribute(Domain Object to be populated)
 	 * @param attributeUIBeanInformationIntf : UI Bean object containing the information entered by the end-user on the UI.
 	 *  @throws DynamicExtensionsSystemException : Exception
 	 *  @throws DynamicExtensionsApplicationException : Excedption
 	 */
 	public void populateAttribute(String userSelectedControlName, AbstractAttributeInterface attributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if ((attributeUIBeanInformationIntf != null) && (attributeInterface != null))
 		{
 			if (attributeInterface instanceof AssociationInterface)
 			{
 				populateAssociation(userSelectedControlName, (AssociationInterface) attributeInterface, attributeUIBeanInformationIntf, entityGroup);
 			}
 			//populate information specific to attribute type
 			populateAttributeSpecificInfo(attributeInterface, attributeUIBeanInformationIntf);
 
 			//populate information common to attributes
 			populateAttributeCommomInfo(attributeInterface, attributeUIBeanInformationIntf);
 
 			//Set is identified
 			populateIsIdentifiedInfo(attributeInterface, attributeUIBeanInformationIntf.getAttributeIdentified());
 
 			//set concept codes
 			populateSemanticPropertiesInfo(attributeInterface, attributeUIBeanInformationIntf.getAttributeConceptCode());
 
 			//populate rules
 			populateRules(userSelectedControlName, attributeInterface, attributeUIBeanInformationIntf);
 		}
 		else
 		{
 			Logger.out.error("Either Attribute interface or attribute information interface is null [" + attributeInterface + "] / ["
 					+ attributeUIBeanInformationIntf + "]");
 		}
 
 	}
 
 	/**
 	 * @param userSelectedControlName
 	 * @param attributeInterface
 	 * @param attributeUIBeanInformationIntf
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void populateAssociation(String userSelectedControlName, AssociationInterface associationIntf,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		EntityInterface targetEntity = entityGroup[0].getEntityByName(attributeUIBeanInformationIntf.getFormName());
 		for (EntityInterface entity : entityGroup[0].getEntityCollection())
 		{
 			Collection<ContainerInterface> containerCollection = entity.getContainerCollection();
 			for (ContainerInterface container : containerCollection)
 			{
 				if (container.getId() != null)
 				{
 					if (container.getId().toString().equals(attributeUIBeanInformationIntf.getFormName()))
 					{
 						targetEntity = entity;
 					}
 				}
 			}
 		}
 		if ((targetEntity != null) && (associationIntf != null))
 		{
 			associationIntf.setTargetEntity(targetEntity);
 			associationIntf.setAssociationDirection(AssociationDirection.SRC_DESTINATION);
 			associationIntf.setName(attributeUIBeanInformationIntf.getName());
 			associationIntf.setSourceRole(getRole(AssociationType.ASSOCIATION, null, Cardinality.ONE, Cardinality.ONE));
 			if ((userSelectedControlName != null) && (userSelectedControlName.equals(ProcessorConstants.LISTBOX_CONTROL)))
 			{
 				associationIntf.setTargetRole(getRole(AssociationType.ASSOCIATION, targetEntity.getName(), Cardinality.ONE, Cardinality.MANY));
 			}
 			else
 			{
 				associationIntf.setTargetRole(getRole(AssociationType.ASSOCIATION, targetEntity.getName(), Cardinality.ONE, Cardinality.ONE));
 			}
 		}
 
 	}
 
 	/**
 	 * @param attributeInterface
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateAttributeCommomInfo(AbstractAttributeInterface attributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		//Set name of attribute
 		attributeInterface.setName(attributeUIBeanInformationIntf.getName());
 		//desc of attribute
 		attributeInterface.setDescription(attributeUIBeanInformationIntf.getDescription());
 	}
 
 	/**
 	 * @param attributeInterface
 	 * @param attributeUIBeanInformationIntf
 	 */
 	public void populateIsIdentifiedInfo(AbstractAttributeInterface attributeInterface, String strIsIdentified)
 	{
 		if (attributeInterface instanceof AttributeInterface)
 		{
 			Boolean isIdentified = new Boolean(strIsIdentified);
 			((AttributeInterface) attributeInterface).setIsIdentified(isIdentified);
 		}
 	}
 
 	/**
 	 * @param attributeInterface
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateSemanticPropertiesInfo(AbstractAttributeInterface attributeInterface, String attributeConceptCode)
 	{
 		attributeInterface.removeAllSemanticProperties();
 		Collection collection = SemanticPropertyBuilderUtil.getSymanticPropertyCollection(attributeConceptCode);
 		if (collection != null && !collection.isEmpty())
 		{
 			Iterator iterator = collection.iterator();
 			while (iterator.hasNext())
 			{
 				attributeInterface.addSemanticProperty((SemanticPropertyInterface) iterator.next());
 			}
 		}
 	}
 
 	/**
 	 * @param attributeTypeInformation
 	 * @param attributeUIBeanInformationIntf
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private void populateAttributeSpecificInfo(AbstractAttributeInterface attributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsApplicationException
 	{
 		AttributeTypeInformationInterface attributeTypeInformation = DynamicExtensionsUtility.getAttributeTypeInformation(attributeInterface);
 		if ((attributeTypeInformation != null) && (attributeUIBeanInformationIntf != null))
 		{
 			if (attributeTypeInformation instanceof StringAttributeTypeInformation)
 			{
 				populateStringAttributeInterface((StringAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof BooleanAttributeTypeInformation)
 			{
 				populateBooleanAttributeInterface((BooleanAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof DateAttributeTypeInformation)
 			{
 				populateDateAttributeInterface((DateAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof ByteArrayAttributeTypeInformation)
 			{
 				populateByteArrayAttributeInterface((ByteArrayAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof FileAttributeTypeInformation)
 			{
 				populateFileAttributeInterface((FileAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof ShortAttributeTypeInformation)
 			{
 				populateShortAttributeInterface((ShortAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof LongAttributeTypeInformation)
 			{
 				populateLongAttributeInterface((LongAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof IntegerAttributeTypeInformation)
 			{
 				populateIntegerAttributeInterface((IntegerAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof FloatAttributeTypeInformation)
 			{
 				populateFloatAttributeInterface((FloatAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformation instanceof DoubleAttributeTypeInformation)
 			{
 				populateDoubleAttributeInterface((DoubleAttributeTypeInformation) attributeTypeInformation, attributeUIBeanInformationIntf);
 			}
 		}
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	public void populateFileAttributeInterface(FileAttributeTypeInformation fileAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		if ((fileAttributeInformation != null) && (attributeUIBeanInformationIntf != null))
 		{
 			//Set File Size
 			if ((attributeUIBeanInformationIntf.getAttributeSize() != null) && (!attributeUIBeanInformationIntf.getAttributeSize().trim().equals("")))
 			{
 				Float fileSize = new Float(attributeUIBeanInformationIntf.getAttributeSize());
 				fileAttributeInformation.setMaxFileSize(fileSize);
 			}
 
 			//Set list of extensions supported
 			fileAttributeInformation.setFileExtensionCollection(getFileExtensionCollection(attributeUIBeanInformationIntf.getFileFormats(),
 					attributeUIBeanInformationIntf.getFormat()));
 		}
 	}
 
 	/**
 	 * @param fileFormats : List of file formats selected by user
 	 * @param fileFormatsString Comma separated set of file formats specified by the user explicitly
 	 * @return
 	 */
 	private Collection<FileExtension> getFileExtensionCollection(String[] fileFormats, String fileFormatsString)
 	{
 		Collection<FileExtension> fileExtensionCollection = new HashSet<FileExtension>();
 		if (fileFormats != null)
 		{
 			int noOfFileFormats = fileFormats.length;
 			for (int i = 0; i < noOfFileFormats; i++)
 			{
 				fileExtensionCollection.add(getFileExtension(fileFormats[i]));
 			}
 		}
 		if (fileFormatsString != null)
 		{
 			StringTokenizer stringTokenizer = new StringTokenizer(fileFormatsString, ProcessorConstants.FILE_FORMATS_SEPARATOR);
 			if (stringTokenizer != null)
 			{
 				while (stringTokenizer.hasMoreElements())
 				{
 					fileExtensionCollection.add(getFileExtension(stringTokenizer.nextToken()));
 				}
 			}
 		}
 		return fileExtensionCollection;
 
 	}
 
 	/**
 	 * @param string
 	 * @return
 	 */
 	private FileExtension getFileExtension(String string)
 	{
 		FileExtension fileExtension = null;
 		if (string != null)
 		{
 			fileExtension = new FileExtension();
 			fileExtension.setFileExtension(string);
 		}
 		return fileExtension;
 	}
 
 	/**
 	 * @param byteArrayAttribute : Byte Array Attribute
 	 * @param attributeUIBeanInformationIntf : UI bean containing information entered  by the user
 	 */
 	private void populateByteArrayAttributeInterface(ByteArrayAttributeTypeInformation byteArrayAttribute,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		//TODO : Code for byte array attribute initialization
 	}
 
 	/**
 	 * @author deepti_shelar
 	 *
 	 * Populate validation rules information for the attribute.
 	 * There are some validation rules that are applicable to the attributes. These need to be stored along with the attributes
 	 *
 	 * @param abstractAttributeInterface : attribute interface
 	 * @param attributeUIBeanInformationIntf : UI Bean containing rule information specified by the user
 	 * @throws DynamicExtensionsSystemException : dynamicExtensionsSystemException
 	 */
 	public void populateRules(String userSelectedControlName, AbstractAttributeInterface abstractAttributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsSystemException
 	{
 		ControlConfigurationsFactory configurationsFactory = ControlConfigurationsFactory.getInstance();
 		HashSet<String> allValidationRules = new HashSet<String>();
 
 		// Collect all the applicable Rule names
 		List<String> implicitRuleList = null;
 
 		implicitRuleList = configurationsFactory.getAllImplicitRules(userSelectedControlName, attributeUIBeanInformationIntf.getDataType());
 		for (String implicitRule : implicitRuleList)
 		{
 			allValidationRules.add(implicitRule);
 		}
 
 		String[] validationRules = attributeUIBeanInformationIntf.getValidationRules();
 
 		for (int i = 0; i < validationRules.length; i++)
 		{
 			if (validationRules[i].length() != 0)
 			{
 				allValidationRules.add(validationRules[i]);
 			}
 		}
 
 		Collection<RuleInterface> attributeRuleCollection = abstractAttributeInterface.getRuleCollection();
 		if (attributeRuleCollection != null && !(attributeRuleCollection.isEmpty()))
 		{
 			HashSet<RuleInterface> obsoleteRules = new HashSet<RuleInterface>();
 			HashSet<RuleInterface> newRules = new HashSet<RuleInterface>();
 			for (RuleInterface rule : attributeRuleCollection)
 			{
 				String attributeRuleName = rule.getName();
 				if (allValidationRules.contains(attributeRuleName) == false)
 				{
 					obsoleteRules.add(rule);
 				}
 				else
 				{
 					obsoleteRules.add(rule);
 					rule = instantiateRule(attributeRuleName, attributeUIBeanInformationIntf);
 					newRules.add(rule);
 					allValidationRules.remove(attributeRuleName);
 				}
 			}
 			attributeRuleCollection.removeAll(obsoleteRules);
 			attributeRuleCollection.addAll(newRules);
 		}
 
 		if (allValidationRules != null && allValidationRules.size() > 0)
 		{
 			for (String validationRule : allValidationRules)
 			{
 				RuleInterface rule = instantiateRule(validationRule, attributeUIBeanInformationIntf);
 				abstractAttributeInterface.addRule(rule);
 			}
 		}
 	}
 
 	/**
 	 * This method populates and returns a new Rule depending upon the Rule name
 	 * @param validationRule
 	 * @param attributeUIBeanInformationIntf
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private RuleInterface instantiateRule(String validationRule, AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 			throws DynamicExtensionsSystemException
 	{
 		RuleConfigurationObject ruleConfigurationObject = null;
 		RuleInterface rule = null;
 
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		ControlConfigurationsFactory configurationsFactory = ControlConfigurationsFactory.getInstance();
 		Collection<RuleParameterInterface> ruleParameterCollection = new HashSet<RuleParameterInterface>();
 
 		ruleConfigurationObject = configurationsFactory.getRuleObject(validationRule);
 		ruleParameterCollection = getRuleParameterCollection(ruleConfigurationObject, attributeUIBeanInformationIntf);
 
 		rule = domainObjectFactory.createRule();
 		rule.setName(ruleConfigurationObject.getRuleName());
 
 		if (ruleParameterCollection != null && !(ruleParameterCollection.isEmpty()))
 		{
 			rule.setRuleParameterCollection(ruleParameterCollection);
 		}
 
 		return rule;
 	}
 
 	/**
 	 * This method populates and returns the Collection of parameters of the Rule.
 	 * @param ruleConfigurationObject the Rule configuration object
 	 * @param abstractAttributeUIBeanInterface the UI Bean for attribute information
 	 * @return the Collection of parameters of the Rule.
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private Collection<RuleParameterInterface> getRuleParameterCollection(RuleConfigurationObject ruleConfigurationObject,
 			AbstractAttributeUIBeanInterface abstractAttributeUIBeanInterface) throws DynamicExtensionsSystemException
 	{
 		Collection<RuleParameterInterface> ruleParameterCollection = new HashSet<RuleParameterInterface>();
 		DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 		List ruleParametersList = ruleConfigurationObject.getRuleParametersList();
 		if (ruleParametersList != null)
 		{
 			StringBuffer operationNameBuff = null;
 			Iterator ruleParametersListIter = ruleParametersList.iterator();
 			while (ruleParametersListIter.hasNext())
 			{
 				NameValueBean param = (NameValueBean) ruleParametersListIter.next();
 
 				String paramName = param.getName();
 				operationNameBuff = new StringBuffer(paramName);
 				operationNameBuff.setCharAt(0, Character.toUpperCase(operationNameBuff.charAt(0)));
 				String methodName = "get" + operationNameBuff.toString();
 
 				try
 				{
 					Class clas = Class.forName("edu.common.dynamicextensions.ui.interfaces.AbstractAttributeUIBeanInterface");
 					Class[] types = new Class[]{};
 
 					Method method = clas.getMethod(methodName, types);
 					Object result = method.invoke(abstractAttributeUIBeanInterface, new Object[0]);
 					RuleParameterInterface ruleParameterInterface = domainObjectFactory.createRuleParameter();
 					ruleParameterInterface.setName(paramName);
 					if (result != null)
 					{
 						ruleParameterInterface.setValue(result.toString());
 					}
 					ruleParameterCollection.add(ruleParameterInterface);
 
 				}
 				catch (Exception e)
 				{
 					throw new DynamicExtensionsSystemException(e.getMessage(), e);
 				}
 
 			}
 		}
 
 		return ruleParameterCollection;
 	}
 
 	/**
 	 * @param attributeUIBeanInformationIntf : UI Bean attribute information object
 	 * @return : Data Element containing list of permisible values
 	 * @throws DynamicExtensionsApplicationException :dynamicExtensionsApplicationException
 	 */
 	public DataElementInterface getDataElementInterface(AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 			throws DynamicExtensionsApplicationException
 	{
 		DataElementInterface dataEltInterface = null;
 		if (attributeUIBeanInformationIntf != null)
 		{
 			String displayChoice = attributeUIBeanInformationIntf.getDisplayChoice();
 			if (displayChoice != null)
 			{
 				if (displayChoice.equalsIgnoreCase(ProcessorConstants.DISPLAY_CHOICE_USER_DEFINED))
 				{
 					dataEltInterface = getDataElementForUserDefinedValues(attributeUIBeanInformationIntf);
 				}
 			}
 		}
 		return dataEltInterface;
 	}
 
 	/**
 	 * @param attributeUIBeanInformationIntf
 	 * @throws DynamicExtensionsApplicationException
 	 *
 	 */
 	private DataElementInterface getDataElementForUserDefinedValues(AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 			throws DynamicExtensionsApplicationException
 	{
 		DataElementInterface userDefinedDataEltInterface = null;
 		PermissibleValueInterface permissibleValue = null;
 		userDefinedDataEltInterface = DomainObjectFactory.getInstance().createUserDefinedDE();
 
 		String csvString = attributeUIBeanInformationIntf.getCsvString();
 		String[][] csvValues = getValuesFromCsv(csvString);
 
 		String[] optionNames = csvValues[0];
 		String[] optionDescriptions = csvValues[2];
 		String[] optionConceptCodes = csvValues[1];
 
 		String optionName = null, optionDesc = null, optionConceptCode = null;
 		Collection<SemanticPropertyInterface> semanticPropertiesForOptions = null;
 
 		if (optionNames != null)
 		{
 			for (int i = 0; i < optionNames.length; i++)
 			{
 				optionName = optionNames[i];
 				optionDesc = optionDescriptions[i];
 				optionConceptCode = optionConceptCodes[i];
 				semanticPropertiesForOptions = SemanticPropertyBuilderUtil.getSymanticPropertyCollection(optionConceptCode);
 				if ((optionName != null) && (optionName.trim() != null))
 				{
 					permissibleValue = getPermissibleValue(attributeUIBeanInformationIntf, optionName, optionDesc, semanticPropertiesForOptions);
 					((UserDefinedDE) userDefinedDataEltInterface).addPermissibleValue(permissibleValue);
 				}
 			}
 		}
 		return userDefinedDataEltInterface;
 	}
 
 	/**
 	 *
 	 * @param csvString
 	 * @return
 	 */
 	private String[][] getValuesFromCsv(String csvString)
 	{
 		if (csvString == null)
 		{
 			csvString = "";
 		}
 
 		String[] rowsStrings = csvString.split("\n");
 		String[][] csvValues = new String[3][];
 		for (int i = 0; i < csvValues.length; i++)
 		{
 			csvValues[i] = new String[rowsStrings.length];
 		}
 
 		for (int i = 0; i < rowsStrings.length; i++)
 		{
 			rowsStrings[i] = rowsStrings[i].trim();
 			String[] columnValues = rowsStrings[i].split("\t");
 
 			int j = 2;
 			while (j < columnValues.length)
 			{
 				if (columnValues[j] != null)
 				{
 					csvValues[j - 2][i] = columnValues[j++];
 				}
 				else
 				{
 					csvValues[j - 2][i] = "";
 				}
 			}
 		}
 		return csvValues;
 	}
 
 	/**
 	 * @param attributeUIBeanInformationIntf : UI Bean containing attribute information
 	 * @param permissibleValue : permissible value  for attribute
 	 * @return Permissible value object for given permissible value
 	 * @throws DynamicExtensionsApplicationException  : dynamicExtensionsApplicationException
 	 */
 	private PermissibleValueInterface getPermissibleValue(AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf, String permissibleValue,
 			String permissibleValueDesc, Collection permissibleValueSematicPropColln) throws DynamicExtensionsApplicationException
 	{
 		PermissibleValueInterface permissibleValueIntf = null;
 		if (attributeUIBeanInformationIntf != null)
 		{
 			String attributeType = attributeUIBeanInformationIntf.getDataType();
 			if (attributeType != null)
 			{
 				try
 				{
 					if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_STRING))
 					{
 						permissibleValueIntf = DomainObjectFactory.getInstance().createStringValue();
 						((StringValue) permissibleValueIntf).setValue(permissibleValue);
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_DATE))
 					{
 						permissibleValueIntf = DomainObjectFactory.getInstance().createDateValue();
 						Date value = Utility.parseDate(permissibleValue, ProcessorConstants.DATE_ONLY_FORMAT);
 						((DateValue) permissibleValueIntf).setValue(value);
 					}
 					else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_BOOLEAN))
 					{
 						permissibleValueIntf = DomainObjectFactory.getInstance().createBooleanValue();
 						Boolean value = new Boolean(permissibleValue);
 						((BooleanValue) permissibleValueIntf).setValue(value);
 					}
 					else if (DynamicExtensionsUtility.isDataTypeNumeric(attributeType))
 					{
 						permissibleValueIntf = getPermissibleValueInterfaceForNumber(attributeUIBeanInformationIntf, permissibleValue,
 								permissibleValueDesc, permissibleValueSematicPropColln);
 					}
 					//populate common properties
 					if (permissibleValueIntf instanceof PermissibleValue)
 					{
 						((PermissibleValue) permissibleValueIntf).setDescription(permissibleValueDesc);
 						((PermissibleValue) permissibleValueIntf).setSemanticPropertyCollection(permissibleValueSematicPropColln);
 					}
 				}
 				catch (Exception e)
 				{
 					throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 				}
 
 			}
 		}
 
 		return permissibleValueIntf;
 	}
 
 	/**
 	 * @param attributeUIBeanInformationIntf : attribute UI Information
 	 * @param permissibleValue : Permissible values
 	 * @return PermissibleValueInterface for numberic field
 	 * @throws DynamicExtensionsApplicationException :Exception
 	 */
 	private PermissibleValueInterface getPermissibleValueInterfaceForNumber(AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf,
 			String permissibleValue, String permissibleValueDesc, Collection permissibleValueSematicPropColln)
 			throws DynamicExtensionsApplicationException
 	{
 
 		PermissibleValueInterface permissibleValueIntf = null;
 		//If it is numberic it can either be float, simple integer, etc based on number of decimals
 		int noOfDecimalPlaces = 0;
 		//Number of decimal places
 		String strNoOfDecimalPlaces = attributeUIBeanInformationIntf.getAttributeDecimalPlaces();
 		if (strNoOfDecimalPlaces != null)
 		{
 			try
 			{
 				if (strNoOfDecimalPlaces.trim().equals(""))
 				{
 					noOfDecimalPlaces = 0;
 				}
 				else
 				{
 					noOfDecimalPlaces = Integer.parseInt(strNoOfDecimalPlaces);
 				}
 			}
 			catch (NumberFormatException e)
 			{
 				throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 			}
 		}
 
 		if (noOfDecimalPlaces == 0)
 		{
 			permissibleValueIntf = DomainObjectFactory.getInstance().createLongValue();
 			Long value = null;
 			try
 			{
 				value = new Long(permissibleValue);
 			}
 			catch (Exception e)
 			{
 				throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 			}
 			((LongValue) permissibleValueIntf).setValue(value);
 		}
 		else if (noOfDecimalPlaces > 0)
 		{
 			permissibleValueIntf = DomainObjectFactory.getInstance().createDoubleValue();
 			Double value = null;
 			try
 			{
 				value = new Double(permissibleValue);
 			}
 			catch (Exception e)
 			{
 				throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 			}
 			((DoubleValue) permissibleValueIntf).setValue(value);
 		}
 		return permissibleValueIntf;
 
 	}
 
 	/**
 	 *
 	 * @param attributeUIBeanInformationIntf  :UI Bean containing attribute information entered by user on UI
 	 * @return Attribute object populated with all required information
 	 * @throws DynamicExtensionsSystemException : Exception
 	 * @throws DynamicExtensionsApplicationException : Exception
 	 */
 	public AbstractAttributeInterface createAndPopulateAttribute(String userSelectedControlName,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf, EntityGroupInterface... entityGroup)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		AbstractAttributeInterface attributeInterface = createAttribute(attributeUIBeanInformationIntf);
 		populateAttribute(userSelectedControlName, attributeInterface, attributeUIBeanInformationIntf, entityGroup);
 		return attributeInterface;
 	}
 
 	/**
 	 *
 	 * @param booleanAttributeIntf Boolean attribute object
 	 * @param attributeUIBeanInformationIntf : UI Bean containing attribute information entered by user on UI
 	 */
 	private void populateBooleanAttributeInterface(BooleanAttributeTypeInformation booleanAttributeIntf,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		BooleanValueInterface booleanValue = DomainObjectFactory.getInstance().createBooleanValue();
 		booleanValue.setValue(new Boolean(attributeUIBeanInformationIntf.getAttributeDefaultValue()));
 		booleanAttributeIntf.setDefaultValue(booleanValue);
 	}
 
 	/**
 	 *
 	 * @param dateAttributeIntf : date Attribute ObjectUI Bean containing attribute information entered by user on UI
 	 * @param attributeUIBeanInformationIntf : UI Bean containing attribute information entered by user on UI
 	 * @throws DynamicExtensionsApplicationException : Exception
 	 */
 	public void populateDateAttributeInterface(DateAttributeTypeInformation dateAttributeIntf,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsApplicationException
 	{
 		// Set Date format based on the UI selection : DATE ONLY or DATE And TIME
 		String format = attributeUIBeanInformationIntf.getFormat();
 		String dateFormat = DynamicExtensionsUtility.getDateFormat(format);
 		dateAttributeIntf.setFormat(dateFormat);
 
 		Date defaultValue = null;
 		String dateValueType = attributeUIBeanInformationIntf.getDateValueType();
 		if (dateValueType != null)
 		{
 			try
 			{
 				//				if (dateValueType.equalsIgnoreCase(ProcessorConstants.DATE_VALUE_TODAY))
 				//				{
 				//					String todaysDate = Utility.parseDateToString(new Date(), dateFormat);
 				//					defaultValue = Utility.parseDate(todaysDate, dateFormat);
 				//				}
 				if (dateValueType.equalsIgnoreCase(ProcessorConstants.DATE_VALUE_SELECT))
 				{
 					if (attributeUIBeanInformationIntf.getAttributeDefaultValue() != null)
 					{
 						String value = DynamicExtensionsUtility.formatMonthAndYearDate(attributeUIBeanInformationIntf.getAttributeDefaultValue());
 						defaultValue = Utility.parseDate(value, "MM-dd-yyyy");
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 			}
 		}
 		//Set default value
 		DateValueInterface dateValue = DomainObjectFactory.getInstance().createDateValue();
 		dateValue.setValue(defaultValue);
 		dateAttributeIntf.setDefaultValue(dateValue);
 	}
 
 	/**
 	 *
 	 * @param stringAttributeIntf : String attribute object
 	 * @param attributeUIBeanInformationIntf  : UI Bean containing attribute information entered by user on UI
 	 * @throws DynamicExtensionsApplicationException : Exception
 	 */
 	public void populateStringAttributeInterface(StringAttributeTypeInformation stringAttributeIntf,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsApplicationException
 	{
 		//Default Value
 		StringValueInterface stringValue = DomainObjectFactory.getInstance().createStringValue();
 		stringValue.setValue(attributeUIBeanInformationIntf.getAttributeDefaultValue());
 		stringAttributeIntf.setDefaultValue(stringValue);
 
 		//Size for string attribute
 		Integer size = null;
 		try
 		{
 			if ((attributeUIBeanInformationIntf.getAttributeSize() != null) && (!attributeUIBeanInformationIntf.getAttributeSize().trim().equals("")))
 			{
 				size = new Integer(attributeUIBeanInformationIntf.getAttributeSize());
 			}
 			else
 			{
 				size = new Integer(0);
 			}
 			stringAttributeIntf.setSize(size);
 		}
 		catch (NumberFormatException e)
 		{
 			throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 		}
 
 	}
 
 	/**
 	 *
 	 * @param shortAttributeInterface : Short attribute object
 	 * @param attributeUIBeanInformationIntf  : UI Bean containing attribute information entered by user on UI
 	 */
 	private void populateShortAttributeInterface(ShortAttributeTypeInformation shortAttributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		//Set default value
 		if ((attributeUIBeanInformationIntf.getAttributeDefaultValue() != null)
 				&& (!attributeUIBeanInformationIntf.getAttributeDefaultValue().trim().equals("")))
 		{
 			ShortValueInterface shortValue = DomainObjectFactory.getInstance().createShortValue();
 			shortValue.setValue(new Short(attributeUIBeanInformationIntf.getAttributeDefaultValue()));
 			shortAttributeInterface.setDefaultValue(shortValue);
 		}
 		shortAttributeInterface.setMeasurementUnits(attributeUIBeanInformationIntf.getAttributeMeasurementUnits());
 		//Decimal places
 		String strDecimalPlaces = attributeUIBeanInformationIntf.getAttributeDecimalPlaces();
 		if ((strDecimalPlaces != null) && (!strDecimalPlaces.trim().equals("")))
 		{
 			shortAttributeInterface.setDecimalPlaces(Integer.parseInt(strDecimalPlaces));
 		}
 		//digits
 		String strDigits = attributeUIBeanInformationIntf.getAttributeDigits();
 		if ((strDigits != null) && (!strDigits.trim().equals("")))
 		{
 			shortAttributeInterface.setDigits(Integer.parseInt(strDigits));
 		}
 	}
 
 	/**
 	 *
 	 * @param integerAttributeInterface : Integer Attribute object
 	 * @param attributeUIBeanInformationIntf  : UI Bean containing attribute information entered by user on UI
 	 * @throws DynamicExtensionsApplicationException : Excpetion
 	 */
 	public void populateIntegerAttributeInterface(IntegerAttributeTypeInformation integerAttributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsApplicationException
 	{
 		//Set default value
 		if (attributeUIBeanInformationIntf.getAttributeDefaultValue() != null)
 		{
 			Integer defaultValue;
 			try
 			{
 				if (attributeUIBeanInformationIntf.getAttributeDefaultValue().trim().equals(""))
 				{
 					defaultValue = new Integer(0); //Assume 0 for blank fields
 				}
 				else
 				{
 					defaultValue = new Integer(attributeUIBeanInformationIntf.getAttributeDefaultValue());
 				}
 			}
 			catch (NumberFormatException e)
 			{
 				throw new DynamicExtensionsApplicationException(e.getMessage(), e);
 			}
 			IntegerValueInterface integerValue = DomainObjectFactory.getInstance().createIntegerValue();
 			integerValue.setValue(defaultValue);
 			integerAttributeInterface.setDefaultValue(integerValue);
 		}
 		integerAttributeInterface.setMeasurementUnits(attributeUIBeanInformationIntf.getAttributeMeasurementUnits());
 		//Decimal places
 		String strDecimalPlaces = attributeUIBeanInformationIntf.getAttributeDecimalPlaces();
 		if ((strDecimalPlaces != null) && (!strDecimalPlaces.trim().equals("")))
 		{
 			integerAttributeInterface.setDecimalPlaces(Integer.parseInt(strDecimalPlaces));
 		}
 		//digits
 		String strDigits = attributeUIBeanInformationIntf.getAttributeDigits();
 		if ((strDigits != null) && (!strDigits.trim().equals("")))
 		{
 			integerAttributeInterface.setDigits(Integer.parseInt(strDigits));
 		}
 
 	}
 
 	/**
 	 *
 	 * @param longAttributeInterface : Long attribute object
 	 * @param attributeUIBeanInformationIntf : UI Bean containing attribute information entered by user on UI
 	 */
 	public void populateLongAttributeInterface(LongAttributeTypeInformation longAttributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		//Set Default Value
 		if ((attributeUIBeanInformationIntf.getAttributeDefaultValue() != null)
 				&& (!attributeUIBeanInformationIntf.getAttributeDefaultValue().trim().equals("")))
 		{
 			LongValueInterface longValue = DomainObjectFactory.getInstance().createLongValue();
 			longValue.setValue(new Long(attributeUIBeanInformationIntf.getAttributeDefaultValue()));
 			longAttributeInterface.setDefaultValue(longValue);
 		}
 		longAttributeInterface.setMeasurementUnits(attributeUIBeanInformationIntf.getAttributeMeasurementUnits());
 		//		Decimal places
 		String strDecimalPlaces = attributeUIBeanInformationIntf.getAttributeDecimalPlaces();
 		if ((strDecimalPlaces != null) && (!strDecimalPlaces.trim().equals("")))
 		{
 			longAttributeInterface.setDecimalPlaces(Integer.parseInt(strDecimalPlaces));
 		}
 		//digits
 		String strDigits = attributeUIBeanInformationIntf.getAttributeDigits();
 		if ((strDigits != null) && (!strDigits.trim().equals("")))
 		{
 			longAttributeInterface.setDigits(Integer.parseInt(strDigits));
 		}
 
 	}
 
 	/**
 	 *
 	 * @param floatAttributeInterface : Float attribute
 	 * @param attributeUIBeanInformationIntf  : UI Bean containing attribute information entered by user on UI
 	 */
 	public void populateFloatAttributeInterface(FloatAttributeTypeInformation floatAttributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		if ((attributeUIBeanInformationIntf.getAttributeDefaultValue() != null)
 				&& (!attributeUIBeanInformationIntf.getAttributeDefaultValue().trim().equals("")))
 		{
 			FloatValueInterface floatValue = DomainObjectFactory.getInstance().createFloatValue();
 			floatValue.setValue(new Float(attributeUIBeanInformationIntf.getAttributeDefaultValue()));
 			floatAttributeInterface.setDefaultValue(floatValue);
 		}
 		floatAttributeInterface.setMeasurementUnits(attributeUIBeanInformationIntf.getAttributeMeasurementUnits());
 		//Decimal places
 		String strDecimalPlaces = attributeUIBeanInformationIntf.getAttributeDecimalPlaces();
 		if ((strDecimalPlaces != null) && (!strDecimalPlaces.trim().equals("")))
 		{
 			floatAttributeInterface.setDecimalPlaces(Integer.parseInt(strDecimalPlaces));
 		}
 		//digits
 		String strDigits = attributeUIBeanInformationIntf.getAttributeDigits();
 		if ((strDigits != null) && (!strDigits.trim().equals("")))
 		{
 			floatAttributeInterface.setDigits(Integer.parseInt(strDigits));
 		}
 
 	}
 
 	/**
 	 *
 	 * @param doubleAttributeInterface : Double attribute
 	 * @param attributeUIBeanInformationIntf : UI Bean containing attribute information entered by user on UI
 	 */
 	public void populateDoubleAttributeInterface(DoubleAttributeTypeInformation doubleAttributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		if ((attributeUIBeanInformationIntf.getAttributeDefaultValue() != null)
 				&& (!attributeUIBeanInformationIntf.getAttributeDefaultValue().trim().equals("")))
 		{
 			DoubleValueInterface doubleValue = DomainObjectFactory.getInstance().createDoubleValue();
 			doubleValue.setValue(new Double(attributeUIBeanInformationIntf.getAttributeDefaultValue()));
 			doubleAttributeInterface.setDefaultValue(doubleValue);
 		}
 		doubleAttributeInterface.setMeasurementUnits(attributeUIBeanInformationIntf.getAttributeMeasurementUnits());
 
 		//Decimal places
 		String strDecimalPlaces = attributeUIBeanInformationIntf.getAttributeDecimalPlaces();
 		if ((strDecimalPlaces != null) && (!strDecimalPlaces.trim().equals("")))
 		{
 			doubleAttributeInterface.setDecimalPlaces(Integer.parseInt(strDecimalPlaces));
 		}
 		//digits
 		String strDigits = attributeUIBeanInformationIntf.getAttributeDigits();
 		if ((strDigits != null) && (!strDigits.trim().equals("")))
 		{
 			doubleAttributeInterface.setDigits(Integer.parseInt(strDigits));
 		}
 		//doubleAttributeInterface.setValidationRules(attributeInformationIntf.getValidationRules());
 	}
 
 	/**
 	 * Create attribute type depending on decimal places value in UI.
 	 * @param noOfDecimalPlaces
 	 * @return
 	 */
 	private AttributeInterface createAttributeForNumericDataType(int noOfDecimalPlaces)
 	{
 		AttributeInterface attribute = null;
 
 		if (noOfDecimalPlaces == Constants.ZERO)
 		{
 			attribute = DomainObjectFactory.getInstance().createLongAttribute();
 		}
 		if (noOfDecimalPlaces > Constants.ZERO)
 		{
 			if (noOfDecimalPlaces <= Constants.FLOAT_PRECISION)
 			{
 				attribute = DomainObjectFactory.getInstance().createFloatAttribute();
 			}
 			else if (noOfDecimalPlaces > Constants.FLOAT_PRECISION && noOfDecimalPlaces <= Constants.DOUBLE_PRECISION)
 			{
 				attribute = DomainObjectFactory.getInstance().createDoubleAttribute();
 			}
 		}
 
 		return attribute;
 	}
 
 	/**
 	 *
 	 * @param attributeInterface : Attribute object
 	 * @param attributeUIBeanInformationIntf  : UI Bean containing attribute information entered by user on UI
 	 */
 	private void populateAttributeValidationRules(AbstractAttributeInterface attributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		String[] ruleNames = new String[0];
 
 		int i = 0;
 		if (attributeInterface.getRuleCollection() != null && !attributeInterface.getRuleCollection().isEmpty())
 		{
 			Iterator rulesIter = attributeInterface.getRuleCollection().iterator();
 			ruleNames = new String[attributeInterface.getRuleCollection().size()];
 			while (rulesIter.hasNext())
 			{
 				RuleInterface rule = (RuleInterface) rulesIter.next();
 				ruleNames[i++] = rule.getName();
 				if (rule.getRuleParameterCollection() != null && !rule.getRuleParameterCollection().isEmpty())
 				{
 					Iterator paramIter = rule.getRuleParameterCollection().iterator();
 					while (paramIter.hasNext())
 					{
 						RuleParameterInterface param = (RuleParameterInterface) paramIter.next();
 						if (param.getName().equalsIgnoreCase("min"))
 						{
 							attributeUIBeanInformationIntf.setMin(param.getValue());
 							attributeUIBeanInformationIntf.setMinTemp(param.getValue());
 						}
 						else if (param.getName().equalsIgnoreCase("max"))
 						{
 							attributeUIBeanInformationIntf.setMax(param.getValue());
 							attributeUIBeanInformationIntf.setMaxTemp(param.getValue());
 						}
 					}
 				}
 			}
 		}
 		attributeUIBeanInformationIntf.setValidationRules(ruleNames);
 		attributeUIBeanInformationIntf.setTempValidationRules(ruleNames);
 	}
 
 	/**
 	 *
 	 * @param attributeInterface :Attribute object
 	 * @param attributeUIBeanInformationIntf  : UI Bean containing attribute information to be displayed on UI
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public void populateAttributeUIBeanInterface(AbstractAttributeInterface attributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		if ((attributeUIBeanInformationIntf != null) && (attributeInterface != null))
 		{
 			attributeUIBeanInformationIntf.setName(attributeInterface.getName());
 			attributeUIBeanInformationIntf.setDescription(attributeInterface.getDescription());
 
 			//is Identified
 			if (attributeInterface instanceof AttributeInterface)
 			{
 				Boolean isIdentified = ((AttributeInterface) attributeInterface).getIsIdentified();
 				if (isIdentified != null)
 				{
 					attributeUIBeanInformationIntf.setAttributeIdentified(isIdentified.toString());
 				}
 			}
 			//Concept code
 			if (attributeInterface.getSemanticPropertyCollection() != null && !attributeInterface.getSemanticPropertyCollection().isEmpty())
 			{
 				attributeUIBeanInformationIntf.setAttributeConceptCode(SemanticPropertyBuilderUtil.getConceptCodeString(attributeInterface));
 			}
 			populateAttributeValidationRules(attributeInterface, attributeUIBeanInformationIntf);
 			//Permissible values
 			setOptionsInformation(attributeInterface, attributeUIBeanInformationIntf);
 			populateAttributeInformationInUIBean(attributeInterface, attributeUIBeanInformationIntf);
 
 		}
 	}
 
 	/**
 	 * @param attributeInterface
 	 * @param attributeUIBeanInformationIntf
 	 */
 	public void populateAttributeInformationInUIBean(AbstractAttributeInterface attributeInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		AttributeTypeInformationInterface attributeTypeInformationInterface = DynamicExtensionsUtility
 				.getAttributeTypeInformation(attributeInterface);
 		if (attributeTypeInformationInterface != null)
 		{
 			if (attributeTypeInformationInterface instanceof StringAttributeTypeInformation)
 			{
 				populateStringAttributeUIBeanInterface((StringAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof DateAttributeTypeInformation)
 			{
 				populateDateAttributeUIBeanInterface((DateAttributeTypeInformation) attributeTypeInformationInterface, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof BooleanAttributeTypeInformation)
 			{
 				populateBooleanAttributeUIBeanInterface((BooleanAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof ByteArrayAttributeTypeInformation)
 			{
 				populateByteArrayAttributeUIBeanInterface((ByteArrayAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof FileAttributeTypeInformation)
 			{
 				populateFileAttributeUIBeanInterface((FileAttributeTypeInformation) attributeTypeInformationInterface, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof IntegerAttributeTypeInformation)
 			{
 				populateIntegerAttributeUIBeanInterface((IntegerAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof ShortAttributeTypeInformation)
 			{
 				populateShortAttributeUIBeanInterface((ShortAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof LongAttributeTypeInformation)
 			{
 				populateLongAttributeUIBeanInterface((LongAttributeTypeInformation) attributeTypeInformationInterface, attributeUIBeanInformationIntf);
 			}
 			else if (attributeTypeInformationInterface instanceof FloatAttributeTypeInformation)
 			{
 				populateFloatAttributeUIBeanInterface((FloatAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 
 			}
 			else if (attributeTypeInformationInterface instanceof DoubleAttributeTypeInformation)
 			{
 				populateDoubleAttributeUIBeanInterface((DoubleAttributeTypeInformation) attributeTypeInformationInterface,
 						attributeUIBeanInformationIntf);
 
 			}
 		}
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateFileAttributeUIBeanInterface(FileAttributeTypeInformation fileAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_FILE);
 		if (fileAttributeInformation.getMaxFileSize() != null)
 		{
 			attributeUIBeanInformationIntf.setAttributeSize(fileAttributeInformation.getMaxFileSize().toString());
 		}
 		attributeUIBeanInformationIntf.setFileFormats(getFileFormats(fileAttributeInformation));
 	}
 
 	/**
 	 * @param fileAttributeInformation
 	 * @return
 	 */
 	private String[] getFileFormats(FileAttributeTypeInformation fileAttributeInformation)
 	{
 		ArrayList<String> fileFormatList = new ArrayList<String>();
 		if (fileAttributeInformation != null)
 		{
 			FileExtension fileExtn = null;
 			Collection<FileExtension> fileExtensionColln = fileAttributeInformation.getFileExtensionCollection();
 			if (fileExtensionColln != null)
 			{
 				Iterator<FileExtension> iterator = fileExtensionColln.iterator();
 				while (iterator.hasNext())
 				{
 					fileExtn = iterator.next();
 					if (fileExtn != null)
 					{
 						fileFormatList.add(fileExtn.getFileExtension());
 					}
 				}
 			}
 		}
 		return fileFormatList.toArray(new String[fileFormatList.size()]);
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateByteArrayAttributeUIBeanInterface(ByteArrayAttributeTypeInformation information,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		// TODO Auto-generated method stub
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateDoubleAttributeUIBeanInterface(DoubleAttributeTypeInformation doubleAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_NUMBER);
 		if (doubleAttributeInformation.getDefaultValue() != null)
 		{
 			DoubleValue defaultDoubleValue = (DoubleValue) doubleAttributeInformation.getDefaultValue();
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(defaultDoubleValue.getValue() + "");
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 		attributeUIBeanInformationIntf.setAttributeMeasurementUnits(doubleAttributeInformation.getMeasurementUnits());
 		attributeUIBeanInformationIntf.setAttributeDecimalPlaces(doubleAttributeInformation.getDecimalPlaces().toString());
 		//attributeUIBeanInformationIntf.setAttributeDigits(doubleAttributeInformation.getDigits().toString());
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateFloatAttributeUIBeanInterface(FloatAttributeTypeInformation floatAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_NUMBER);
 		if (floatAttributeInformation.getDefaultValue() != null)
 		{
 			FloatValue floatValue = (FloatValue) floatAttributeInformation.getDefaultValue();
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(floatValue.getValue() + "");
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 		attributeUIBeanInformationIntf.setAttributeMeasurementUnits(floatAttributeInformation.getMeasurementUnits());
 		attributeUIBeanInformationIntf.setAttributeDecimalPlaces(floatAttributeInformation.getDecimalPlaces().toString());
 		//attributeUIBeanInformationIntf.setAttributeDigits(floatAttributeInformation.getDigits().toString());
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateLongAttributeUIBeanInterface(LongAttributeTypeInformation longAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_NUMBER);
 		if (longAttributeInformation.getDefaultValue() != null)
 		{
 			LongValue longDefaultValue = (LongValue) longAttributeInformation.getDefaultValue();
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(longDefaultValue.getValue() + "");
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 		attributeUIBeanInformationIntf.setAttributeMeasurementUnits(longAttributeInformation.getMeasurementUnits());
 		attributeUIBeanInformationIntf.setAttributeDecimalPlaces(longAttributeInformation.getDecimalPlaces().toString());
 		//attributeUIBeanInformationIntf.setAttributeDigits((longAttributeInformation.getDigits().toString()));
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateShortAttributeUIBeanInterface(ShortAttributeTypeInformation shortAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_SHORT);
 		if (shortAttributeInformation.getDefaultValue() != null)
 		{
 			ShortValue shortDefaultValue = (ShortValue) shortAttributeInformation.getDefaultValue();
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(shortDefaultValue.getValue() + "");
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 		attributeUIBeanInformationIntf.setAttributeMeasurementUnits(shortAttributeInformation.getMeasurementUnits());
 		attributeUIBeanInformationIntf.setAttributeDecimalPlaces(shortAttributeInformation.getDecimalPlaces().toString());
 		//	attributeUIBeanInformationIntf.setAttributeDigits(shortAttributeInformation.getDigits().toString());
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateIntegerAttributeUIBeanInterface(IntegerAttributeTypeInformation integerAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_INTEGER);
 		if (integerAttributeInformation.getDefaultValue() != null)
 		{
 			IntegerValue integerDefaultValue = (IntegerValue) integerAttributeInformation.getDefaultValue();
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(integerDefaultValue.getValue() + "");
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 		attributeUIBeanInformationIntf.setAttributeMeasurementUnits(integerAttributeInformation.getMeasurementUnits());
 		attributeUIBeanInformationIntf.setAttributeDecimalPlaces(integerAttributeInformation.getDecimalPlaces().toString());
 		//	attributeUIBeanInformationIntf.setAttributeDigits(integerAttributeInformation.getDigits().toString());
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateBooleanAttributeUIBeanInterface(BooleanAttributeTypeInformation booleanAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_BOOLEAN);
 		if (booleanAttributeInformation.getDefaultValue() != null)
 		{
 			BooleanValue booleanDefaultValue = (BooleanValue) booleanAttributeInformation.getDefaultValue();
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(booleanDefaultValue.getValue() + "");
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateDateAttributeUIBeanInterface(DateAttributeTypeInformation datAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_DATE);
 
 		String format = getDateFormat(datAttributeInformation.getFormat());
 		attributeUIBeanInformationIntf.setFormat(format);
 
 		if (datAttributeInformation.getDefaultValue() != null)
 		{
 			String dateFormat = DynamicExtensionsUtility.getDateFormat(format);
 			String defaultValue = Utility.parseDateToString((Date) datAttributeInformation.getDefaultValue().getValueAsObject(), dateFormat);
 			attributeUIBeanInformationIntf.setAttributeDefaultValue(defaultValue);
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 	}
 
 	private String getDateFormat(String dateFormat)
 	{
 		String format = null;
 		if (dateFormat != null)
 		{
 			if (dateFormat.equalsIgnoreCase(ProcessorConstants.DATE_ONLY_FORMAT))
 			{
 				format = ProcessorConstants.DATE_FORMAT_OPTION_DATEONLY;
 			}
 			else if (dateFormat.equalsIgnoreCase(ProcessorConstants.DATE_TIME_FORMAT))
 			{
 				format = ProcessorConstants.DATE_FORMAT_OPTION_DATEANDTIME;
 			}
 			else if (dateFormat.equalsIgnoreCase(ProcessorConstants.YEAR_ONLY_FORMAT))
 			{
 				format = ProcessorConstants.DATE_FORMAT_OPTION_YEARONLY;
 			}
 			else if (dateFormat.equalsIgnoreCase(ProcessorConstants.MONTH_YEAR_FORMAT))
 			{
 				format = ProcessorConstants.DATE_FORMAT_OPTION_MONTHANDYEAR;
 			}
 
 		}
 		else
 		{
 			format = ProcessorConstants.DATE_FORMAT_OPTION_DATEONLY;
 		}
 
 		return format;
 	}
 
 	/**
 	 * @param information
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateStringAttributeUIBeanInterface(StringAttributeTypeInformation stringAttributeInformation,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		attributeUIBeanInformationIntf.setDataType(ProcessorConstants.DATATYPE_STRING);
 		if (stringAttributeInformation.getDefaultValue() != null)
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue((String) stringAttributeInformation.getDefaultValue().getValueAsObject());
 		}
 		else
 		{
 			attributeUIBeanInformationIntf.setAttributeDefaultValue("");
 		}
 		Integer size = stringAttributeInformation.getSize();
 		if (size != null)
 		{
 			attributeUIBeanInformationIntf.setAttributeSize(size.toString());
 		}
 	}
 
 	/**
 	 *
 	 * @param attributeInterface : Attribute interface
 	 * @param attributeUIBeanInformationIntf    : UI Bean containing attribute information to be displayed on UI
 	 * @return Comma separated list of permissible values
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void setOptionsInformation(AbstractAttributeInterface attributeInterface, AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 			throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 
 		if ((attributeUIBeanInformationIntf != null) && (attributeInterface != null))
 		{
 			if (attributeInterface instanceof AssociationInterface)
 			{
 				//Lookup options selected
 				populateUIBeanAssociationInformation((AssociationInterface) attributeInterface, attributeUIBeanInformationIntf);
 			}
 			else
 			{
 				AttributeTypeInformationInterface attributeTypeInformationInterface = DynamicExtensionsUtility
 						.getAttributeTypeInformation(attributeInterface);
 				if (attributeTypeInformationInterface != null)
 				{
 					DataElementInterface dataEltInterface = attributeTypeInformationInterface.getDataElement();
 					if ((dataEltInterface != null) && (dataEltInterface instanceof UserDefinedDEInterface))
 					{
 						populateUserDefinedOptionValues(attributeTypeInformationInterface, attributeUIBeanInformationIntf);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @param interface1
 	 * @param attributeUIBeanInformationIntf
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private void populateUIBeanAssociationInformation(AssociationInterface associationInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf) throws DynamicExtensionsSystemException,
 			DynamicExtensionsApplicationException
 	{
 		attributeUIBeanInformationIntf.setDisplayChoice(ProcessorConstants.DISPLAY_CHOICE_LOOKUP);
 		if ((associationInterface != null) && (attributeUIBeanInformationIntf != null))
 		{
 			EntityInterface targetEntity = associationInterface.getTargetEntity();
 			if (targetEntity != null)
 			{
 				attributeUIBeanInformationIntf.setGroupName(getGroupName(targetEntity));
 				attributeUIBeanInformationIntf.setFormName(getFormName(targetEntity));
 			}
 		}
 	}
 
 	/**
 	 * @param targetEntity
 	 * @return
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	private String getFormName(EntityInterface entity) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		if (entity != null)
 		{
 			EntityManagerInterface entityManager = EntityManager.getInstance();
 			ContainerInterface containerInterface = entityManager.getContainerByEntityIdentifier(entity.getId());
 			if ((containerInterface != null) && (containerInterface.getId() != null))
 			{
 				return containerInterface.getId().toString();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param targetEntity
 	 * @return
 	 */
 	private String getGroupName(EntityInterface targetEntity)
 	{
 		//Initialize group name
 		EntityGroupInterface entityGroup = targetEntity.getEntityGroup();
 		//Assumed that the collection will contain just one entity. So fetching first elt of collection
 		if (entityGroup != null)
 		{
 			//			EntityGroupInterface entityGroup = entityGroups.iterator().next();
 			//			if (entityGroup != null)
 			//			{
 			//				if (entityGroup.getId() != null)
 			//				{
 			//					return entityGroup.getId().toString();
 			//				}
 			//			}
 			return entityGroup.getId().toString();
 		}
 		return null;
 	}
 
 	/**
 	 * @param attributeTypeInformationInterface
 	 * @param attributeUIBeanInformationIntf
 	 */
 	private void populateUserDefinedOptionValues(AttributeTypeInformationInterface attributeTypeInformationInterface,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf)
 	{
 		ArrayList<OptionValueObject> optionDetails = new ArrayList<OptionValueObject>();
 		DataElementInterface dataEltInterface = attributeTypeInformationInterface.getDataElement();
 		if ((attributeTypeInformationInterface != null) && (attributeUIBeanInformationIntf != null))
 		{
 			attributeUIBeanInformationIntf.setDisplayChoice(ProcessorConstants.DISPLAY_CHOICE_USER_DEFINED);
 			UserDefinedDEInterface userDefinedDE = (UserDefinedDEInterface) dataEltInterface;
 			Collection userDefinedValues = userDefinedDE.getPermissibleValueCollection();
 			if (userDefinedValues != null)
 			{
 				PermissibleValueInterface permissibleValueIntf = null;
 				Iterator userDefinedValuesIterator = userDefinedValues.iterator();
 				while (userDefinedValuesIterator.hasNext())
 				{
 					permissibleValueIntf = (PermissibleValueInterface) userDefinedValuesIterator.next();
 					optionDetails.add(getOptionDetails(permissibleValueIntf));
 				}
 			}
 			attributeUIBeanInformationIntf.setOptionDetails(optionDetails);
 		}
 
 	}
 
 	/**
 	 * @param permissibleValueIntf
 	 * @return
 	 */
 	private OptionValueObject getOptionDetails(PermissibleValueInterface permissibleValueIntf)
 	{
 		if (permissibleValueIntf != null)
 		{
 			Object permissibleValueObjectValue = permissibleValueIntf.getValueAsObject();
 			if ((permissibleValueObjectValue != null) && (permissibleValueObjectValue.toString() != null)
 					&& (permissibleValueObjectValue.toString().trim() != ""))
 			{
 				OptionValueObject optionDetail = new OptionValueObject();
 				optionDetail.setOptionName(permissibleValueObjectValue.toString().trim());
 				if (permissibleValueIntf instanceof PermissibleValue)
 				{
 					populateOptionDetails(optionDetail, ((PermissibleValue) permissibleValueIntf));
 				}
 				return optionDetail;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * @param optionValue
 	 * @param permissibleValueIntf
 	 */
 	private void populateOptionDetails(OptionValueObject optionValue, PermissibleValue permissibleValue)
 	{
 		if ((optionValue != null) && (permissibleValue != null))
 		{
 			if (permissibleValue.getDescription() != null)
 			{
 				optionValue.setOptionDescription(permissibleValue.getDescription());
 			}
 			else
 			{
 				optionValue.setOptionDescription("");
 			}
 			String optionConceptCode = SemanticPropertyBuilderUtil.getConceptCodeString(permissibleValue.getSemanticPropertyCollection());
 			if (optionConceptCode != null)
 			{
 				optionValue.setOptionConceptCode(optionConceptCode);
 			}
 			else
 			{
 				optionValue.setOptionConceptCode("");
 			}
 		}
 	}
 
 	/**
 	 * @param abstractAttributeInterface
 	 * @param controlsForm
 	 * @return TODO
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws DynamicExtensionsSystemException
 	 */
 	public AbstractAttributeInterface updateAttributeInformation(String userSelectedControlName,
 			AbstractAttributeInterface abstractAttributeInformation, AbstractAttributeUIBeanInterface attributeUIBeanInformation,
 			EntityGroupInterface... entityGroup) throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException
 	{
 		AbstractAttributeInterface attributeInterface = null;
 		if ((abstractAttributeInformation != null) && (attributeUIBeanInformation != null))
 		{
 			if (canUpdateExistingAttribute(abstractAttributeInformation, attributeUIBeanInformation))
 			{
 				attributeInterface = abstractAttributeInformation;
 				if (attributeInterface instanceof AttributeInterface)
 				{
 					AttributeInterface attribute = (AttributeInterface) attributeInterface;
 
 					AttributeTypeInformationInterface attributeTypeInformation = createAttributeTypeInformation(attributeUIBeanInformation, attribute
 							.getAttributeTypeInformation());
 					// Added by Rajesh to check if data-type is changed then only reset attributeTypeInfo (Bug 7677)
 					if (isDataTypeChanged(attribute.getAttributeTypeInformation(), attributeTypeInformation))
 					{
 						attribute.setAttributeTypeInformation(attributeTypeInformation);
 					}
 					populateAttribute(userSelectedControlName, attribute, attributeUIBeanInformation);
 				}
 				else if (attributeInterface instanceof AssociationInterface)
 				{
 					populateAssociation(userSelectedControlName, (AssociationInterface) attributeInterface, attributeUIBeanInformation, entityGroup);
 				}
 			}
 			else
 			//Cannot update same instance
 			{
 				/*//Create a new instance and set that in the control
 				 attributeInterface = createAndPopulateAttribute(userSelectedControlName, attributeUIBeanInformation);*/
 
 				//Throw Exception cannot convert attribute type
 				DynamicExtensionsApplicationException applnException = new DynamicExtensionsApplicationException(
 						"Cannot convert from Lookup to User defined or vice-versa");
 				throw applnException;
 			}
 		}
 		return attributeInterface;
 	}
 
 	/**
 	 * Checks the dataType for the edited and new attributetypeinfo.
 	 * @param originalAttributeTypeInformation
 	 * @param newAttributeTypeInformation
 	 * @return
 	 */
 	private boolean isDataTypeChanged(AttributeTypeInformationInterface originalAttributeTypeInformation,
 			AttributeTypeInformationInterface newAttributeTypeInformation)
 	{
 		boolean isChanged = false;
 		if (originalAttributeTypeInformation.getDataType() != null && newAttributeTypeInformation.getDataType() != null)
 		{
 			if (!originalAttributeTypeInformation.getDataType().equals(newAttributeTypeInformation.getDataType()))
 			{
 				isChanged = true;
 			}
 		}
 		return isChanged;
 	}
 
 	/**
 	 * @param attributeUIBeanInformation
 	 * @param abstractAttributeInformation
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private boolean canUpdateExistingAttribute(AbstractAttributeInterface existingAbstractAttributeIntf,
 			AbstractAttributeUIBeanInterface attributeUIBeanInformation) throws DynamicExtensionsApplicationException,
 			DynamicExtensionsSystemException
 	{
 		boolean areInstancesOfSameType = false;
 		if (existingAbstractAttributeIntf != null)
 		{
 			AbstractAttributeInterface newAbstractAttribute = createAttribute(attributeUIBeanInformation);
 			if ((newAbstractAttribute instanceof AttributeInterface) && (existingAbstractAttributeIntf instanceof AttributeInterface))
 			{
 				areInstancesOfSameType = true;
 			}
 			if ((newAbstractAttribute instanceof AssociationInterface) && (existingAbstractAttributeIntf instanceof AssociationInterface))
 			{
 				areInstancesOfSameType = true;
 			}
 		}
 		return areInstancesOfSameType;
 	}
 
 	/**
 	 * @param attributeUIBeanInformation
 	 * @throws DynamicExtensionsApplicationException
 	 */
 	private AttributeTypeInformationInterface createAttributeTypeInformation(AbstractAttributeUIBeanInterface attributeUIBeanInformationIntf,
 			AttributeTypeInformationInterface originalAttrTypeInfo) throws DynamicExtensionsApplicationException
 	{
 		AttributeTypeInformationInterface attributeTypeInformation = null;
 		if (attributeUIBeanInformationIntf != null)
 		{
 			String attributeType = attributeUIBeanInformationIntf.getDataType();
 			if (attributeType != null)
 			{
 				DomainObjectFactory domainObjectFactory = DomainObjectFactory.getInstance();
 				if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_STRING))
 				{
 					attributeTypeInformation = domainObjectFactory.createStringAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_DATE))
 				{
 					attributeTypeInformation = domainObjectFactory.createDateAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_BOOLEAN))
 				{
 					attributeTypeInformation = domainObjectFactory.createBooleanAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_BYTEARRAY))
 				{
 					attributeTypeInformation = domainObjectFactory.createByteArrayAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_FILE))
 				{
 					attributeTypeInformation = domainObjectFactory.createFileAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_NUMBER))
 				{
 					int noOfDecimals = DynamicExtensionsUtility.convertStringToInt(attributeUIBeanInformationIntf.getAttributeDecimalPlaces());
 					attributeTypeInformation = createAttributeTypeInfoForNumericDataType(noOfDecimals, originalAttrTypeInfo);
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_SHORT))
 				{
 					attributeTypeInformation = domainObjectFactory.createShortAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_INTEGER))
 				{
 					attributeTypeInformation = domainObjectFactory.createIntegerAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_LONG))
 				{
 					attributeTypeInformation = domainObjectFactory.createLongAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_FLOAT))
 				{
 					attributeTypeInformation = domainObjectFactory.createFloatAttributeTypeInformation();
 				}
 				else if (attributeType.equalsIgnoreCase(ProcessorConstants.DATATYPE_DOUBLE))
 				{
 					attributeTypeInformation = domainObjectFactory.createDoubleAttributeTypeInformation();
 				}
 			}
 		}
 		return attributeTypeInformation;
 	}
 
 	/**
 	 * @param noOfDecimalPlaces
 	 * @param originalAttrTypeInfo
 	 * @return
 	 */
 	private AttributeTypeInformationInterface createAttributeTypeInfoForNumericDataType(int noOfDecimalPlaces,
 			AttributeTypeInformationInterface originalAttrTypeInfo)
 	{
 		AttributeTypeInformationInterface numAttrTypeInfo = null;
 
 		if (noOfDecimalPlaces == Constants.ZERO)
 		{
 			if (originalAttrTypeInfo instanceof ShortAttributeTypeInformation)
 			{
 				numAttrTypeInfo = DomainObjectFactory.getInstance().createShortAttributeTypeInformation();
 			}
 			else if (originalAttrTypeInfo instanceof IntegerAttributeTypeInformation)
 			{
 				numAttrTypeInfo = DomainObjectFactory.getInstance().createIntegerAttributeTypeInformation();
 			}
 			else
 			{
 				numAttrTypeInfo = DomainObjectFactory.getInstance().createLongAttributeTypeInformation();
 			}
 		}
 		if (noOfDecimalPlaces > Constants.ZERO)
 		{
 			if (noOfDecimalPlaces <= Constants.FLOAT_PRECISION)
 			{
 				numAttrTypeInfo = DomainObjectFactory.getInstance().createFloatAttributeTypeInformation();
 			}
 			else if (noOfDecimalPlaces > Constants.FLOAT_PRECISION && noOfDecimalPlaces <= Constants.DOUBLE_PRECISION)
 			{
 				numAttrTypeInfo = DomainObjectFactory.getInstance().createDoubleAttributeTypeInformation();
 			}
 		}
 
 		return numAttrTypeInfo;
 	}
 
 }
