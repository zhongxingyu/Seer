 /*
  *	File: @(#)TestPafBaseMemberProps.java 	Package: com.pace.base.mdb 	Project: Essbase Provider
  *	Created: Oct 22, 2005  		By: Alan Farkas
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *	This software is the confidential and proprietary information of Palladium Group, Inc.
  *	("Confidential Information"). You shall not disclose such Confidential Information and 
  * 	should use it only in accordance with the terms of the license agreement you entered into
  *	with Palladium Group, Inc.
  *
  *
  *
 	Date			Author			Version			Changes
 	xx/xx/xx		xxxxxxxx		x.xx			..............
  * 
  */
 package com.pace.mdb.essbase;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import org.apache.log4j.Logger;
 
 import com.essbase.api.base.EssException;
 import com.essbase.api.metadata.IEssMember;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.mdb.PafBaseMemberProps;
 import com.pace.base.mdb.PafSimpleBaseMemberProps;
 
 /**
  * Test PafBaseMemberProps 
  *
  * @version	x.xx
  * @author Alan Farkas
  *
  */
 public class TestPafBaseMemberProps extends TestCase {
 	
 	private static Logger logger = Logger.getLogger(TestPafBaseMemberProps.class);
 		
 	/*
 	 * Test method for 'com.pace.base.mdb.PafBaseMemberProps.getSimpleVersion()'
 	 */
 	public void testGetSimpleVersion() {
 
 		boolean isSuccess = true;
 		
 		String validationErrors = "";
 		final int levelNumber = 3;
 		final int generationNumber = 2;
 		final String description = "Test Member";
 		final String formula = "x = x + 1";
 		final String lastFormula = "y = 2x";
 		final String[] aliasKeys = {"Default", "Table1", "Table2"};
 		final String[] aliasValues = {"Test1", "Test2", "Test3"};
 		final String[] associatedAttrKeys = {"Color", "Size", "Brand"};
 		final String[] associatedAttrValues = {"Red", "10 Oz.", "GE"};
 		final String[] UDAs = {"Big", "Tall"}; 
 		final HashMap<String,String> aliases = new HashMap<String,String>();
 		final HashMap<String,String> attributes = new HashMap<String,String>();
 		final IEssMember.EEssConsolidationType consolidationType = IEssMember.EEssConsolidationType.MULTIPLICATION;
 		final IEssMember.EEssShareOption shareOption = IEssMember.EEssShareOption.SHARED_MEMBER;
 		
 		PafBaseMemberProps memberAttr = null;
 		PafSimpleBaseMemberProps simple = null;
 		
 		
 		logger.info("***************************************************");
 		logger.info(this.getName() +  " - Test Started");
 		try {
 			// Create and populate a representative PafBaseMemberProps
 			memberAttr = new PafBaseMemberProps();
 			memberAttr.setDescription(description);
 			for (int i = 0; i < aliasKeys.length;i++) {
 				aliases.put(aliasKeys[i], aliasValues[i]);
 			}
 			for (int i = 0; i < associatedAttrKeys.length;i++) {
 				aliases.put(associatedAttrKeys[i], associatedAttrValues[i]);
 			}
 			memberAttr.setAliases(aliases);
 			memberAttr.setAssociatedAttributes(attributes);
 			memberAttr.setConsolidationType(consolidationType);
 			memberAttr.setGenerationNumber(generationNumber);
 			memberAttr.setFormula(formula);
 			memberAttr.setLastFormula(lastFormula);
 			memberAttr.setLevelNumber(levelNumber);
 			memberAttr.setShareOption(shareOption);
 			memberAttr.setUDAs(UDAs);
 			
 			// Get simple version of PafBaseMemberProps
 			simple = memberAttr.getSimpleVersion();
 
 			// Validate PafSimpleAttr object
 			validationErrors = validateSimpleProps(simple, memberAttr);
 							
 		} catch (Exception e) {
 			logger.info("Java Exception: " + e.getMessage());	
 			isSuccess = false;
 		} finally {
 			try {
 				if (validationErrors.length() > 0) {
 					isSuccess = false;
 					logger.info("Validation Error(s) Encountered:\r" + validationErrors);
 				}
 				assertTrue(isSuccess);
 			} finally {
 				System.out.print(this.getName());
 				if (isSuccess) {
 					logger.info(this.getName() + " - Successful");
 					logger.info("***************************************************\n");
 				}
 				else {
 					logger.info(this.getName() + " - Failed");			
 					logger.info("***************************************************\n");
 				}
 			}
 		}
 	}
 
 	
 	/**
 	 *	Validate Paf Simple Properties
 	 *
 	 * @param simple PafSimpleBaseMemberProps object to validate
 	 * @param memberProps PafBaseMemberProps object to validate against
 	 * @return String containing a list any errors encountered
 	 * @throws PafException 
 	 */
 	static String validateSimpleProps(PafSimpleBaseMemberProps simple, PafBaseMemberProps memberProps) throws PafException {
 		
 		int aliasCount = 0, attributeCount = 0;
 		String validationErrors = "";
 		String[] simpleAssociatedAttrKeys = null, simpleAssociatedAttrValues = null;
 		String[] simpleAliasKeys = null, simpleAliasValues = null;
 		String[] simpleUDAs = null, UDAs = null;
 		Map<String,String> aliases = null, attributes = null;
 	
 		// Validate "simple" properties
 		if (!simple.getDescription().equals(memberProps.getDescription())) {
 			validationErrors += "Description doesn't match\n";
 		}
 		if (simple.getGenerationNumber() != memberProps.getGenerationNumber()) {
 			validationErrors += "Generation number doesn't match\n";
 		}
 		if (!simple.getFormula().equals(memberProps.getFormula())) {
 			validationErrors += "Formula doesn't match\n";
 		}
 		if (!simple.getLastFormula().equals(memberProps.getLastFormula())) {
 			validationErrors += "Last Formula doesn't match\n";
 		}		
 		if (simple.getLevelNumber() != memberProps.getLevelNumber()) {
 			validationErrors += "Level number doesn't match\n";
 		}		
 
 		// Validate Aliases
		aliases = memberProps.getAliases();
		aliasCount = aliases.size();
 		if (simple.getAliasKeys().length == aliasCount) {
 			simpleAliasKeys = simple.getAliasKeys();
 			simpleAliasValues = simple.getAliasValues();
 			for (int i = 0; i < aliasCount; i++) {
 				String key = simpleAliasKeys[i];
 				String value = simpleAliasValues[i];
 				if (!aliases.containsKey(key) || !aliases.get(key).equals(value)) {
 					validationErrors += "Aliases don't match\n";
 					break;
 					}
 			}
 		} else {
 			validationErrors += "Alias count doesn't match\n";
 		}
 
 		// Validate Associated Attributes
 		attributeCount = memberProps.getAssociatedAttributes().size();
 		if (simple.getAssociatedAttrKeys().length == attributeCount) {
 			simpleAssociatedAttrKeys = simple.getAssociatedAttrKeys();
 			simpleAssociatedAttrValues = simple.getAssociatedAttrValues();
 			attributes = memberProps.getAssociatedAttributes();
 			for (int i = 0; i < aliasCount; i++) {
 				String key = simpleAssociatedAttrKeys[i];
 				String value = simpleAssociatedAttrValues[i];
 				if (!attributes.containsKey(key) || !attributes.get(key).equals(value)) {
 					validationErrors += "Associated attributes don't match\n";
 					break;
 					}
 			}
 		} else {
 			validationErrors += "Number of associated attributes doesn't match\n";
 		}
 		
 		// Validate UDA's
 		UDAs = memberProps.getUDAs();
 		simpleUDAs = simple.getUDAs();
 		if (UDAs.length == simpleUDAs.length) {
 			for (int i = 0; i < UDAs.length; i++) {
 				if (!simpleUDAs[i].equals(UDAs[i])) {
 					validationErrors += "UDAs don't match\n";			
 				}
 			}
 		} else {
 			validationErrors += "Number of UDAs doesn't match\n";			
 		}
 			
 		// Validate Consolidation Type, Share Option, and Associated Attributes
 		try {
 			if (simple.getConsolidationType() !=  memberProps.getConsolidationType().intValue()) {
 				validationErrors += "Consolidation type doesn't match\n";
 			}
 			if (simple.getShareOption() !=  memberProps.getShareOption().intValue()) {
 				validationErrors += "Share option doesn't match\n";
 			}
 		} catch (EssException esx) {
 			// throw Paf Exception
 			String errMsg = esx.getMessage();
 			logger.error(errMsg);
 			PafException pfe = new PafException(errMsg, PafErrSeverity.Error, esx);	
 			throw pfe;
 		}
 		
 		// Return any validation errors
  		return validationErrors;
 	}
 }
