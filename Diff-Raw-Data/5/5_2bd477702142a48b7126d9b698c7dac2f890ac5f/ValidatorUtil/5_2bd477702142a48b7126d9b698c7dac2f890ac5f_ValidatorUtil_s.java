 
 package edu.common.dynamicextensions.validation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import edu.common.dynamicextensions.domaininterface.AssociationInterface;
 import edu.common.dynamicextensions.domaininterface.AssociationMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.AttributeMetadataInterface;
 import edu.common.dynamicextensions.domaininterface.BaseAbstractAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAssociationInterface;
 import edu.common.dynamicextensions.domaininterface.CategoryAttributeInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.AbstractContainmentControlInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ContainerInterface;
 import edu.common.dynamicextensions.domaininterface.userinterface.ControlInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleInterface;
 import edu.common.dynamicextensions.domaininterface.validationrules.RuleParameterInterface;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsValidationException;
 import edu.common.dynamicextensions.ui.util.ControlConfigurationsFactory;
 import edu.common.dynamicextensions.util.global.DEConstants.AssociationType;
 import edu.wustl.common.util.global.ApplicationProperties;
 
 /**
  * @author Sujay Narkar
  * @author Rahul Ner
  * @author chetan_patil
  */
 public class ValidatorUtil
 {
 
 	/**
 	 * @param attributeValueMap  key - AttributeInterface
 	 *                           value - value that user has entered for this attribute
 	 * @return listOfError if any
 	 * @throws DynamicExtensionsSystemException : Exception
 	 * @throws DynamicExtensionsValidationException
 	 */
 	public static List<String> validateEntity(
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap, List<String> listOfError,
 			ContainerInterface containerInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsValidationException
 	{
 		List<String> errorList=listOfError;
 		if (errorList == null)
 		{
 			errorList = new ArrayList<String>();
 		}
 		Set<Map.Entry<BaseAbstractAttributeInterface, Object>> attributeSet = attributeValueMap
 				.entrySet();
 		if (attributeSet != null || !attributeSet.isEmpty())
 		{
 			for (Map.Entry<BaseAbstractAttributeInterface, Object> attributeValueNode : attributeSet)
 			{
 				BaseAbstractAttributeInterface abstractAttribute = attributeValueNode.getKey();
 				if (abstractAttribute instanceof AttributeMetadataInterface)
 				{
 					ControlInterface control = getControlForAbstractAttribute(
 							(AttributeMetadataInterface) abstractAttribute, containerInterface);
 					errorList.addAll(validateAttributes(attributeValueNode, control.getCaption()));
 				}
 				else if (abstractAttribute instanceof AssociationMetadataInterface)
 				{
 
 					AssociationMetadataInterface associationInterface = (AssociationMetadataInterface) abstractAttribute;
 					if (AssociationType.CONTAINTMENT.equals(associationInterface
 							.getAssociationType()))
 					{
 						List<Map<BaseAbstractAttributeInterface, Object>> valueObject = (List<Map<BaseAbstractAttributeInterface, Object>>) attributeValueMap
 								.get(abstractAttribute);
 						for (Map<BaseAbstractAttributeInterface, Object> subAttributeValueMap : valueObject)
 						{
 							errorList.addAll(validateEntityAttributes(subAttributeValueMap,
 									getContainerForAbstractAttribute(associationInterface)));
 						}
 					}
 				}
 			}
 		}
 
 		return errorList;
 	}
 
 	/**
 	 *
 	 * @param abstractAttribute
 	 * @param containerInterface
 	 * @return
 	 */
 	public static ControlInterface getControlForAbstractAttribute(
 			AttributeMetadataInterface attributeMetadataInterface,
 			ContainerInterface containerInterface)
 	{
 		ControlInterface control = null;
 		Collection<ControlInterface> controlCollection = containerInterface.getAllControls();
 		for (ControlInterface controlInterface : controlCollection)
 		{
 			if (controlInterface instanceof AbstractContainmentControlInterface)
 			{
 				control = getControlForAbstractAttribute(attributeMetadataInterface,
 						((AbstractContainmentControlInterface) controlInterface).getContainer());
 				if (control != null)
 				{
 					return control;
 				}
 			}
 			else
 			{
 				if (controlInterface.getBaseAbstractAttribute().equals(attributeMetadataInterface))
 				{
 					control = controlInterface;
 					break;
 				}
 			}
 		}
 		return control;
 
 	}
 
 	/**
 	 *
 	 * @param abstractAttribute
 	 * @param containerInterface
 	 * @return
 	 */
 	public static ContainerInterface getContainerForAbstractAttribute(
 			AssociationMetadataInterface associationMetadataInterface)
 	{
 		ContainerInterface containerInterface = null;
 		Collection<ContainerInterface> containerCollection = null;
 		if (associationMetadataInterface instanceof AssociationInterface)
 		{
 			AssociationInterface associationInterface = (AssociationInterface) associationMetadataInterface;
 			containerCollection = associationInterface.getTargetEntity().getContainerCollection();
 
 		}
 		else if (associationMetadataInterface instanceof CategoryAssociationInterface)
 		{
 			CategoryAssociationInterface categoryAssociationInterface = (CategoryAssociationInterface) associationMetadataInterface;
 			containerCollection = categoryAssociationInterface.getTargetCategoryEntity()
 					.getContainerCollection();
 		}
 		List<ContainerInterface> containerList = new ArrayList<ContainerInterface>(
 				containerCollection);
 		if (!containerList.isEmpty())
 		{
 			containerInterface = containerList.get(0);
 		}
 		return containerInterface;
 
 	}
 
 	/**
 	 *
 	 * @param attributeValueMap
 	 * @param containerInterface
 	 * @param abstractContainmentControlInterface
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private static List<String> validateEntityAttributes(
 			Map<BaseAbstractAttributeInterface, Object> attributeValueMap,
 			ContainerInterface containerInterface) throws DynamicExtensionsSystemException,
 			DynamicExtensionsValidationException
 	{
 		List<String> errorList = new ArrayList<String>();
 
 		Set<Map.Entry<BaseAbstractAttributeInterface, Object>> attributeSet = attributeValueMap
 				.entrySet();
 		if (attributeSet == null || attributeSet.isEmpty())
 		{
 			return errorList;
 		}
 
 		for (Map.Entry<BaseAbstractAttributeInterface, Object> attributeValueNode : attributeSet)
 		{
 			BaseAbstractAttributeInterface abstractAttribute = attributeValueNode.getKey();
 			if (abstractAttribute instanceof AttributeMetadataInterface)
 			{
 				ControlInterface control = getControlForAbstractAttribute(
 						(AttributeMetadataInterface) abstractAttribute, containerInterface);
 				if (control != null)
 				{
 					errorList.addAll(validateAttributes(attributeValueNode, control.getCaption()));
 				}
 			}
 		}
 
 		return errorList;
 	}
 
 	/**
 	 * @param attributeValueNode
 	 * @param controlCaption
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsValidationException
 	 */
 	private static List<String> validateAttributes(
 			Map.Entry<BaseAbstractAttributeInterface, Object> attributeValueNode,
 			String controlCaption) throws DynamicExtensionsSystemException,
 			DynamicExtensionsValidationException
 	{
 		List<String> errorList = new ArrayList<String>();
 
 		BaseAbstractAttributeInterface abstractAttribute = attributeValueNode.getKey();
 		AttributeMetadataInterface attribute = (AttributeMetadataInterface) abstractAttribute;
 		//Bug: 9778 : modified to get explicit and implicit rules also in case of CategoryAttribute.
 		//Reviewer: Rajesh Patil
 		Collection<RuleInterface> attributeRuleCollection = getRuleCollection(abstractAttribute);
 		if (attributeRuleCollection != null || !attributeRuleCollection.isEmpty())
 		{
 			Long recordId = attribute.getId();
 			String errorMessage = null;
 			for (RuleInterface rule : attributeRuleCollection)
 			{
 				String ruleName = rule.getName();
				if (!ruleName.equals("unique"))
 				{
 					Object valueObject = attributeValueNode.getValue();
 					if (valueObject instanceof List)
 					{
 						valueObject = ((List) valueObject).get(0);
 
 					}
 					Map<String, String> parameterMap = getParamMap(rule);
 					try
 					{
 						checkValidation(attribute, valueObject, rule, parameterMap, controlCaption);
 					}
 					catch (DynamicExtensionsValidationException e)
 					{
 						errorMessage = ApplicationProperties.getValue(e.getErrorCode(), e
 								.getPlaceHolderList());
 						errorList.add(errorMessage);
 					}
 				}
 				else
 				{
 					Object valueObject = attributeValueNode.getValue();
 					if (valueObject instanceof List)
 					{
 						valueObject = ((List) valueObject).get(0);
 
 					}
 					try
 					{
 						checkUniqueValidationForAttribute(attribute, valueObject, recordId,
 								controlCaption);
 					}
 					catch (DynamicExtensionsValidationException e)
 					{
 						errorMessage = ApplicationProperties.getValue(e.getErrorCode(), e
 								.getPlaceHolderList());
 						errorList.add(errorMessage);
 					}
 				}
 			}
 		}
 
 		return errorList;
 	}
 
 	/**
 	 * @param abstractAttribute
 	 * @return
 	 */
 	private static Collection<RuleInterface> getRuleCollection(
 			BaseAbstractAttributeInterface abstractAttribute)
 	{
 		AttributeMetadataInterface attribute = (AttributeMetadataInterface) abstractAttribute;
 		Collection<RuleInterface> attributeRuleCollection = attribute.getRuleCollection();
 		if (attribute instanceof CategoryAttributeInterface)
 		{
 			Collection<RuleInterface> implicitRuleCollection = ((CategoryAttributeInterface) attribute)
 					.getAbstractAttribute().getRuleCollection();
 			attributeRuleCollection.addAll(implicitRuleCollection);
 		}
 		return attributeRuleCollection;
 	}
 
 	public static void checkUniqueValidationForAttribute(AttributeMetadataInterface attribute,
 			Object valueObject, Long recordId, String controlCaption)
 			throws DynamicExtensionsValidationException, DynamicExtensionsSystemException
 	{
 		Collection<RuleInterface> attributeRuleCollection = attribute.getRuleCollection();
 
 		if (attributeRuleCollection != null || !attributeRuleCollection.isEmpty())
 		{
 			for (RuleInterface rule : attributeRuleCollection)
 			{
 				String ruleName = rule.getName();
				if (ruleName.equals("unique"))
 				{
 					Map<String, String> parameterMap = new HashMap<String, String>();
 					if (recordId != null)
 					{
 						parameterMap.put("recordId", recordId.toString());
 					}
 					checkValidation(attribute, valueObject, rule, parameterMap, controlCaption);
 					break;
 				}
 			}
 		}
 	}
 
 	private static void checkValidation(AttributeMetadataInterface attribute, Object valueObject,
 			RuleInterface rule, Map<String, String> parameterMap, String controlCaption)
 			throws DynamicExtensionsSystemException, DynamicExtensionsValidationException
 	{
 		String ruleName = rule.getName();
 		ValidatorRuleInterface validatorRule = ControlConfigurationsFactory.getInstance()
 				.getValidatorRule(ruleName);
 		validatorRule.validate(attribute, valueObject, parameterMap, controlCaption);
 	}
 
 	/**
 	 * This method returns the Map of parameters of the Rule.
 	 * @param ruleInterface the Rule instance whose Map of parameter are to be fetched.
 	 * @return the Map of parameters of the Rule.
 	 * 					key - name of parameter
 	 * 					value - value of parameter
 	 */
 	private static Map<String, String> getParamMap(RuleInterface rule)
 	{
 		Map<String, String> parameterMap = new HashMap<String, String>();
 		Collection<RuleParameterInterface> ruleParamCollection = rule.getRuleParameterCollection();
 		if (ruleParamCollection != null && !ruleParamCollection.isEmpty())
 		{
 			for (RuleParameterInterface ruleParameter : ruleParamCollection)
 			{
 				parameterMap.put(ruleParameter.getName(), ruleParameter.getValue());
 			}
 		}
 		return parameterMap;
 	}
 
 }
