 
 package edu.common.dynamicextensions.util.parser;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.List;
 
 import edu.common.dynamicextensions.domain.DomainObjectFactory;
 import edu.common.dynamicextensions.domaininterface.AttributeTypeInformationInterface;
 import edu.common.dynamicextensions.domaininterface.EntityGroupInterface;
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.common.dynamicextensions.domaininterface.PermissibleValueInterface;
 import edu.common.dynamicextensions.domaininterface.UserDefinedDEInterface;
 import edu.common.dynamicextensions.entitymanager.EntityGroupManager;
 import edu.common.dynamicextensions.exception.DynamicExtensionsApplicationException;
 import edu.common.dynamicextensions.exception.DynamicExtensionsSystemException;
 import edu.common.dynamicextensions.util.CategoryHelper;
 import edu.common.dynamicextensions.util.CategoryHelperInterface;
 import edu.common.dynamicextensions.util.DynamicExtensionsUtility;
 import edu.common.dynamicextensions.validation.category.CategoryValidator;
 
 /**
  * @author kunal_kamble
  * This class the imports the permissible values from csv file into the database.
  *
  */
 public class ImportPermissibleValues
 {
 
 	private CategoryCSVFileParser categoryCSVFileParser;
 
 	private static final String ENTITY_GROUP = "Entity_Group";
 
 	/**
 	 *
 	 * @param filePath
 	 * @throws DynamicExtensionsSystemException
 	 * @throws FileNotFoundException
 	 */
 	public ImportPermissibleValues(String filePath) throws DynamicExtensionsSystemException, FileNotFoundException
 	{
 		this.categoryCSVFileParser = new CategoryCSVFileParser(filePath);
 	}
 
 	/**
 	 *
 	 * @return
 	 * @throws DynamicExtensionsSystemException
 	 * @throws DynamicExtensionsApplicationException
 	 * @throws ParseException 
 	 */
 	public void importValues() throws DynamicExtensionsApplicationException, DynamicExtensionsSystemException, ParseException
 	{
 		CategoryHelperInterface categoryHelper = new CategoryHelper();
 
 		try
 		{
 			while (categoryCSVFileParser.readNext())
 			{
 				//first line in the categopry file is Category_Definition
 				if (ENTITY_GROUP.equals(categoryCSVFileParser.readLine()[0]))
 				{
 					continue;
 				}
 				//1:read the entity group
 				EntityGroupInterface entityGroup = DynamicExtensionsUtility.retrieveEntityGroup(categoryCSVFileParser.getEntityGroupName());
 
				CategoryValidator.checkForNullRefernce(entityGroup, "Entity group with name " + categoryCSVFileParser.getEntityGroupName()
						+ " at line number " + categoryCSVFileParser.getLineNumber() + " does not exist");
 
 				categoryCSVFileParser.getCategoryValidator().setEntityGroup(entityGroup);
 
 				EntityInterface currentEntity = null;
 				while (categoryCSVFileParser.readNext())
 				{
 					boolean overridePv = categoryCSVFileParser.isOverridePermissibleValues();
 					if (ENTITY_GROUP.equals(categoryCSVFileParser.readLine()[0]))
 					{
 						break;
 					}
 					String entityName = categoryCSVFileParser.getEntityName();
 					currentEntity = entityGroup.getEntityByName(entityName);
 
 					String attributeName = categoryCSVFileParser.getAttributeName();
 
 					List<String> pvList = categoryCSVFileParser.getPermissibleValues();
 					List<String> finalPvList = new ArrayList<String>();
 
 					AttributeTypeInformationInterface attributeTypeInformation = currentEntity.getAttributeByName(attributeName)
 							.getAttributeTypeInformation();
 					UserDefinedDEInterface userDefinedDE = (UserDefinedDEInterface) attributeTypeInformation.getDataElement();
 
 					if (userDefinedDE == null)
 					{
 						userDefinedDE = DomainObjectFactory.getInstance().createUserDefinedDE();
 						attributeTypeInformation.setDataElement(userDefinedDE);
 						finalPvList = pvList;
 					}
 					else
 					{
 						if (overridePv)
 						{
 							userDefinedDE.clearPermissibleValues();
 						}
 
 						List<String> list = new ArrayList<String>();
 						for (PermissibleValueInterface permissibleValue : userDefinedDE.getPermissibleValueCollection())
 						{
 							list.add(permissibleValue.getValueAsObject().toString());
 						}
 
 						for (String string : pvList)
 						{
 							if (!list.contains(string))
 							{
 								finalPvList.add(string);
 							}
 						}
 					}
 
 					List<PermissibleValueInterface> list = categoryHelper.getPermissibleValueList(currentEntity.getAttributeByName(attributeName)
 							.getAttributeTypeInformation(), finalPvList);
 
 					for (PermissibleValueInterface pv : list)
 					{
 						userDefinedDE.addPermissibleValue(pv);
 					}
 					if (list != null && !list.isEmpty())
 					{
 						//set the first value in the list as the default value
 						attributeTypeInformation.setDefaultValue(list.get(0));
 					}
 
 				}
 				EntityGroupManager.getInstance().persistEntityGroup(entityGroup);
 
 			}
 		}
 		catch (IOException e)
 		{
 			throw new DynamicExtensionsSystemException("Line number:" + categoryCSVFileParser.getLineNumber() + "Error while reading csv file "
 					+ categoryCSVFileParser.getFilePath(), e);
 		}
 
 	}
 
 	public static void main(String args[]) throws DynamicExtensionsSystemException, DynamicExtensionsApplicationException
 	{
 		try
 		{
 			if (args.length == 0)
 			{
 				throw new Exception("Please Specify the path for .csv file");
 			}
 			String filePath = args[0];
 			System.out.println("---- The .csv file path is " + filePath + " ----");
 			ImportPermissibleValues importPermissibleValues = new ImportPermissibleValues(filePath);
 			importPermissibleValues.importValues();
 			System.out.println("Added permissible values successfully!!!!");
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 			throw new RuntimeException(ex);
 		}
 
 	}
 
 }
