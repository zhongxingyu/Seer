 /*
  *	File: @(#)RuleSetsExcelElementItem.java 	Package: com.pace.base.project.excel.elements 	Project: Paf Base Libraries
  *	Created: Jan 12, 2010  		By: jmilliron
  *	Version: x.xx
  *
  * 	Copyright (c) 2005-2010 Palladium Group, Inc. All rights reserved.
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
 package com.pace.base.project.excel.elements;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.ss.usermodel.Workbook;
 
 import com.pace.base.PafException;
 import com.pace.base.app.AllocType;
 import com.pace.base.project.*;
 import com.pace.base.project.excel.*;
 import com.pace.base.project.utils.PafExcelUtil;
 import com.pace.base.rules.Formula;
 import com.pace.base.rules.Rule;
 import com.pace.base.rules.RuleGroup;
 import com.pace.base.rules.RuleSet;
 import com.pace.base.utility.CollectionsUtil;
 
 /**
  * Reads/writes rule sets from/to an Excel 2007 workbook.
  *
  * @author jmilliron
  * @version	1.00
  *
  */
 public class RuleSetsExcelElementItem<T extends Map<String, RuleSet>> extends PafExcelElementItem<T> implements IExcelDynamicReferenceElementItem {
 
 	private static final String RULE_SET_SHEET_IDENT = "RuleSet_";
 	
 	/**
 	 * Creates an excel element item. Mainly used for reading rule sets.
 	 * 
 	 * @param workbook workbook used for read/write
 	 */
 	public RuleSetsExcelElementItem(Workbook workbook) {
 		super(workbook);
 	}
 	
 		
 	@Override
 	protected void createHeaderListMapEntries() {
 		
 		getHeaderListMap().put(ProjectElementId.RuleSet_RuleSet.toString(), Arrays.asList("rule set name", "alloc type", "measure list", "lift existing measure list", "lift all measure list", "comment - rs", "comment - rg", "id - rg", "perpetual - rg", "skip protection processing - rg", "balance key set - rg", "delayed perpetual - rg", "perpetual allocation - rg", "perform initial allocation - rg", "base allocate measure", "trigger measures (pipe delimited)", "skip allocation", "lock allocation", "skip aggeration", "lock system evaluation result", "lock user evaluation result", "eval locked intersections", "lock all prior time", "calc all periods", "initial TB first allocation"));
 		getHeaderListMap().put(ProjectElementId.RuleSet_RuleGroup.toString(), Arrays.asList("rule group", "", "", "", "", "", ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, ExcelPaceProjectConstants.HEADER_IGNORE_IDENT, "", "", "", "", "", "", "", "", "", "", ""));
 		getHeaderListMap().put(ProjectElementId.RuleSet_Rule.toString(), Arrays.asList("", "rule(s)", "result term", "expression", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""));
 		
 	}
 
 	@Override
 	public ProjectElementId getProjectElementId() {
 		
 		return ProjectElementId.RuleSets;
 		
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected T readExcelSheet() throws PaceProjectReadException, PafException {
 		
 		Map<String, RuleSet> ruleSetMap = new HashMap<String, RuleSet>();
 						
 		for ( String sheetName : getRuleSetSheetNames() ) {
 				
 			RuleSet rs = getRuleSet(sheetName);
 	
 			if ( rs != null && rs.getName() != null ) {
 				
 				ruleSetMap.put(rs.getName(), rs);
 				
 			}
 			
 		}
 		
 		return (T) ruleSetMap;
 		
 	}
 	
 	private List<String> getRuleSetSheetNames() throws PafException {
 		
 		List<String> ruleSetSheetNameList = new ArrayList<String>();
 		
 		for ( String sheetName : PafExcelUtil.getWorkbookSheetNames(getWorkbook())) {
 			
 			//if a rule set sheet name
 			if ( sheetName.startsWith(RULE_SET_SHEET_IDENT) ) {
 				
 				ruleSetSheetNameList.add(sheetName);
 				
 			}
 			
 		}
 		
 		
 		return ruleSetSheetNameList;
 		
 	}
 
 	private RuleSet getRuleSet(String sheetName) throws PafException {
 				
 		PafExcelInput input = new PafExcelInput.Builder(getWorkbook(), sheetName, getHeaderListMap().get(ProjectElementId.RuleSet_RuleSet.toString()).size())
 			.headerListMap(getHeaderListMap())
 			.excludeHeaderRows(false)
 			.excludeEmptyRows(true)
 			.sheetRequired(true)
 			.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 			.multiDataRow(false)
 			.build();
 
 		List<PafExcelRow> excelRowList = PafExcelUtil.readExcelSheet(input);
 
 		ProjectElementId currentProjectElementId = null;
 
 		RuleSet rs = null;
 		
 		List<Rule> ruleList = null;
 		
 		boolean isNameFound = false;
 		
 		RuleGroup rg = null;
 		
 		int rowNdx = 0;
 		
 		ROW:
 		for (PafExcelRow excelRow : excelRowList) {
 		
 			//increment row index.
 			rowNdx++;
 			
 			PafExcelValueObject valueObject = null;
 			
 			if ( excelRow.isHeader() ) {
 				
 				currentProjectElementId = ProjectElementId.valueOf(excelRow.getHeaderIdent());
 				
 				switch (currentProjectElementId) {
 				
 					case RuleSet_RuleSet:
 						
 						rs = new RuleSet();
 						
 						break;
 						
 					case RuleSet_RuleGroup:
 						
 						if ( rs != null ) {
 						
 							RuleGroup[] ruleGroupAr = rs.getRuleGroups();
 							
 							List<RuleGroup> ruleGroupList = new ArrayList<RuleGroup>();
 							
 							if ( ruleGroupAr != null && ruleGroupAr.length > 0 ) {
 							
 								ruleGroupList.addAll(Arrays.asList(ruleGroupAr));
 								
 							}
 							
 							rg = new RuleGroup();
 							
 							for (Integer colIndex : excelRow.getRowItemOrderedIndexes()) {
 									
 								valueObject = excelRow.getRowItem(colIndex).get(0);
 								
 								try {
 								
 									switch (colIndex) {
 									
 									//rule group comment
 									case 6:
 										
 										rg.setComment(PafExcelUtil.getString(getProjectElementId(), valueObject));									
 										break;
 									
 									//id
 									case 7:
 										
 										rg.setRuleGroupId(PafExcelUtil.getString(getProjectElementId(), valueObject));									
 										break;
 										
 									//perpetual
 									case 8:
 										
 										rg.setPerpetual(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//skip protection processing
 									case 9:
 										
 										rg.setSkipProtProc(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//balance key set
 									case 10:
 										
 										rg.setBalanceSetKey(PafExcelUtil.getString(getProjectElementId(), valueObject));
 										break;
 										
 									//delayed perpetual
 									case 11:
 										
 										rg.setDelayedPerpetual(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//perpetual allocation
 									case 12:
 										
 										rg.setPerpetualAllocation(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//perform initial allocation 
 									case 13:
 										
 										rg.setPerformInitialAllocation(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;								
 									
 									
 									}
 									
 								} catch (ExcelProjectDataErrorException epdee) {
 									
 									addProjectDataErrorToList(epdee.getProjectDataError());
 									
 								}
 								
 								
 							}
 							
 							
 							ruleGroupList.add(rg);
 							
 							rs.setRuleGroups(ruleGroupList.toArray(new RuleGroup[0]));
 						
 						}
 						
 						break;			
 						
 					case RuleSet_Rule:
 						
 						ruleList = new ArrayList<Rule>();
 						break;
 				
 				}
 				
 				} else {
 					
 					if ( currentProjectElementId == null ) {
 						
 						addProjectDataErrorToList(new ProjectDataError(getProjectElementId(), "Row " + rowNdx , "Row could not be flagged as header or data.  Please ensure valid header row exists.  Row contents: " + excelRow));
 						
 					} else {
 						
 						switch (currentProjectElementId) {
 						
 						case RuleSet_RuleSet:
 							
 							if ( rs != null ) {
 							
 								for (Integer colIndex : excelRow.getRowItemOrderedIndexes()) {
 									
 									valueObject = excelRow.getRowItem(colIndex).get(0);
 									
 									try {
 									
 										switch (colIndex) {
 										
 										//name
 										case 0:
 											
 											//if the rule set name not found, try to get
 											if ( ! isNameFound ) {
 												
 												rs.setName(PafExcelUtil.getString(getProjectElementId(), valueObject, true));
 												isNameFound = true;
 											} 
 											
 											break;
 											
 											//aloc type
 										case 1:
 											
 											//if the rule set name not found, try to get
 											if ( ! valueObject.isBlank() ) {
 												rs.setAllocType(AllocType.valueOf(PafExcelUtil.getString(getProjectElementId(), valueObject)));
 											} 
 											
 											break;
 												
 										//measure list
 										case 2:
 											
 											if ( ! valueObject.isBlank() ) {
 												
 												String[] measureAr = rs.getMeasureList();
 												
 												List<String> measureList = new ArrayList<String>();
 												
 												if ( measureAr != null ) {
 													
 													measureList.addAll(Arrays.asList(measureAr)); 
 													
 												}
 												
 												String newMeasure = PafExcelUtil.getString(getProjectElementId(), valueObject);
 												
 												if ( newMeasure != null ) {
 												
 													measureList.add(newMeasure);
 													
 												}	
 												
 												if ( measureList.size() > 0 ) {
 												
 													rs.setMeasureList(measureList.toArray(new String[0]));
 													
 												}
 												
 											}
 											
 											break;
 											
 											
 										//lift existing measure list
 										case 3:
 											
 											if ( ! valueObject.isBlank() ) {
 												
 												String[] liftExistMeasureAr = rs.getLiftExistingMeasureList();
 												
 												List<String> liftExistMeasureList = new ArrayList<String>();
 												
 												if ( liftExistMeasureAr != null ) {
 													
 													liftExistMeasureList.addAll(Arrays.asList(liftExistMeasureAr)); 
 													
 												}
 												
 												String newMeasure = PafExcelUtil.getString(getProjectElementId(), valueObject);
 												
 												if ( newMeasure != null ) {
 												
 													liftExistMeasureList.add(newMeasure);
 													
 												}	
 												
 												if ( liftExistMeasureList.size() > 0 ) {
 												
													rs.setLiftExistingMeasureList(liftExistMeasureList.toArray(new String[0]));
 													
 												}
 												
 											}
 											
 											break;
 												
 												
 										//lift all measure list
 										case 4:
 											
 											if ( ! valueObject.isBlank() ) {
 												
 												String[] liftAllMeasureAr = rs.getLiftAllMeasureList();
 												
 												List<String> liftAllMeasureList = new ArrayList<String>();
 												
 												if ( liftAllMeasureAr != null ) {
 													
 													liftAllMeasureList.addAll(Arrays.asList(liftAllMeasureAr)); 
 													
 												}
 												
 												String newMeasure = PafExcelUtil.getString(getProjectElementId(), valueObject);
 												
 												if ( newMeasure != null ) {
 												
 													liftAllMeasureList.add(newMeasure);
 													
 												}	
 												
 												if ( liftAllMeasureList.size() > 0 ) {
 												
													rs.setLiftAllMeasureList(liftAllMeasureList.toArray(new String[0]));
 													
 												}
 												
 											}
 											
 											break;
 								
 										//comment
 										case 5:
 											
 											if ( ! valueObject.isBlank()) {
 												
 												rs.setComment(PafExcelUtil.getString(getProjectElementId(), valueObject));
 												
 											}
 											
 											//continue to next row
 											continue ROW;
 										
 										}
 										
 									} catch (ExcelProjectDataErrorException epdee) {
 										
 										addProjectDataErrorToList(epdee.getProjectDataError());
 										
 									}
 									
 									
 								}
 								
 							}
 							
 							break;
 							
 						case RuleSet_Rule:
 							
 							Rule rule = null;
 							
 							String resultTerm = null;
 							
 							for (Integer colIndex : excelRow.getRowItemOrderedIndexes()) {
 								
 								valueObject = excelRow.getRowItem(colIndex).get(0);
 								
 								try {
 								
 									switch (colIndex) {
 									
 									//result term
 									case 2:
 										
 										rule = new Rule();
 										
 										resultTerm = PafExcelUtil.getString(getProjectElementId(), valueObject, true);
 										
 										break;
 										
 									//expression
 									case 3:
 										
 										String expression = PafExcelUtil.getString(getProjectElementId(), valueObject, true);
 																				
 										rule.setFormula(new Formula(resultTerm, expression));
 										
 										break;
 										
 									//base allocate measure		
 									case 14:
 												
 										rule.setBaseAllocateMeasure(PafExcelUtil.getString(getProjectElementId(), valueObject));
 										break;
 										
 									//trigger measures
 									case 15:
 											
 										rule.setTriggerMeasures(PafExcelUtil.getStringArFromDelimValueObject(getProjectElementId(), valueObject));
 										
 										break;
 										
 									//skip allocation	
 									case 16:
 										
 										rule.setSkipAllocation(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										
 										break;
 										
 									//lock allocation	
 									case 17:
 										
 										rule.setLockAllocation(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										
 										break;
 										
 									//skip aggeration	
 									case 18:
 										
 										rule.setSkipAggregation(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//lock system evaluation result	
 									case 19:
 										
 										rule.setLockSystemEvaluationResult(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//lock user evaluation result	
 									case 20:
 										
 										rule.setLockUserEvaluationResult(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//eval locked intersections	
 									case 21:
 										
 										rule.setEvalLockedIntersections(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//lock all prior time	
 									case 22:
 										
 										rule.setLockAllPriorTime(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//calc all periods	
 									case 23:
 										
 										rule.setCalcAllPeriods(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									//initial TB first allocation
 									case 24:
 										
 										rule.setInitialTBFirstAllocation(PafExcelUtil.getBoolean(getProjectElementId(), valueObject, true));
 										break;
 										
 									}
 									
 								} catch (ExcelProjectDataErrorException epdee) {
 									
 									addProjectDataErrorToList(epdee.getProjectDataError());
 									
 								}							
 						
 						}
 						
 						if ( rule != null ) {
 							
 							ruleList.add(rule);
 							
 						}
 						
 						if ( rg != null && ruleList.size() > 0 ) {
 							
 							rg.setRules(ruleList.toArray(new Rule[0]));
 							
 						}
 						
 					}
 					
 				}
 				
 				
 			}
 		}
 
 			
 		
 		return rs;
 
 	}
 
 	@Override
 	protected void writeExcelSheet(T t) throws PaceProjectWriteException, PafException {
 		
 		Map<String, RuleSet> ruleSetMap = t;		
 		
 		//TODO: create sheet map with key being rs name and value being new sheetname. Truncate and add _1, _2 to end of value
 		
 		List<String> keyList = new ArrayList<String>();
 		
 		if ( ruleSetMap != null ) {
 			
 			keyList.addAll(ruleSetMap.keySet());
 			
 		}
 		
 		Collections.sort(keyList);
 		
 		//get map of updated sheet names in case sheet name was too long
 		Map<String, String> sheetNameMap = generateSheetNameMap(keyList);	
 		
 		List<String> existingRuleSetSheetNameList = getRuleSetSheetNames();
 		
 		for (String ruleSetKey : keyList ) {			
 		
 			String sheetName = ruleSetKey;
 			
 			//see if there is a generated sheet name to use
 			if ( sheetNameMap.containsKey(ruleSetKey)) {
 				
 				sheetName = sheetNameMap.get(ruleSetKey);
 				
 			}
 			
 			//if list containts sheet name, remove from list
 			if ( CollectionsUtil.containsIgnoreCase(existingRuleSetSheetNameList, sheetName) ) {
 			
 				String listItem = CollectionsUtil.getIgnoreCase(existingRuleSetSheetNameList, sheetName);
 				
 				if ( listItem != null ) {
 					
 					existingRuleSetSheetNameList.remove(listItem);
 					
 				}
 				
 			}
 			
 			PafExcelInput input = new PafExcelInput.Builder(getWorkbook(), sheetName, getHeaderListMap().get(ProjectElementId.RuleSet_RuleSet.toString()).size())
 				.headerListMap(getHeaderListMap())
 				.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 				.build();	
 					
 			List<PafExcelRow> excelRowList = new ArrayList<PafExcelRow>();
 		
 			//create and add header to list
 			excelRowList.add(PafExcelUtil.createHeaderRow(getHeaderListMap().get(ProjectElementId.RuleSet_RuleSet.toString())));
 			
 			if ( ruleSetKey != null ) {
 				
 				RuleSet ruleSet = ruleSetMap.get(ruleSetKey);
 				
 				if ( ruleSet != null ) {
 					
 					PafExcelRow excelRow = new PafExcelRow();
 					
 					//name
 					excelRow.addRowItem(0, PafExcelValueObject.createFromString(ruleSet.getName()));
 					
 					//alloc type
 					if( ruleSet.getAllocType() != null ) {
 						excelRow.addRowItem(1, PafExcelValueObject.createFromString(ruleSet.getAllocType().toString()));
 					}
 					
 					//measure list
 					if ( ruleSet.getMeasureList() != null ) {
 						
 						for (String measureName : ruleSet.getMeasureList() ) {
 							
 							excelRow.addRowItem(2, PafExcelValueObject.createFromString(measureName));
 							
 						}
 						
 					}
 					
 					//lift existing measure list
 					if ( ruleSet.getLiftExistingMeasureList() != null ) {
 						
 						for (String measureName : ruleSet.getLiftExistingMeasureList() ) {
 							
 							excelRow.addRowItem(3, PafExcelValueObject.createFromString(measureName));
 							
 						}
 						
 					}
 					
 					//lift all measure list
 					if ( ruleSet.getLiftAllMeasureList() != null ) {
 						
 						for (String measureName : ruleSet.getLiftAllMeasureList()) {
 							
 							excelRow.addRowItem(4, PafExcelValueObject.createFromString(measureName));
 							
 						}
 						
 					}
 					
 					//comment
 					excelRow.addRowItem(5, PafExcelValueObject.createFromString(ruleSet.getComment()));
 					
 					//add rule set row
 					excelRowList.add(excelRow);
 					
 					RuleGroup[] ruleGroupAr = ruleSet.getRuleGroups();
 					
 					if ( ruleGroupAr != null ) {
 						
 						for ( RuleGroup rg : ruleGroupAr ) {
 						
 							//blank row
 							excelRowList.add(new PafExcelRow());
 							
 							//add rule group row
 							excelRow = new PafExcelRow();
 							
 							//header ident
 							
 							PafExcelValueObject headerIdentValueObject = PafExcelValueObject.createFromString(getHeaderListMap().get(ProjectElementId.RuleSet_RuleGroup.toString()).get(0));
 
 							if ( headerIdentValueObject != null ) {
 							
 								headerIdentValueObject.setBoldItem(true);
 								
 							}
 							
 							excelRow.addRowItem(0, headerIdentValueObject);
 									
 							//comment - rg
 							excelRow.addRowItem(6, PafExcelValueObject.createFromString(rg.getComment()));
 							
 							//id - rg
 							excelRow.addRowItem(7, PafExcelValueObject.createFromString(rg.getRuleGroupId()));
 							
 							//perpectual - rg
 							excelRow.addRowItem(8, PafExcelValueObject.createFromBoolean(rg.isPerpetual()));
 							
 							//skip protection processing - rg
 							excelRow.addRowItem(9, PafExcelValueObject.createFromBoolean(rg.isSkipProtProc()));							
 
 							//balance key set - rg
 							excelRow.addRowItem(10, PafExcelValueObject.createFromString(rg.getBalanceSetKey()));
 							
 							//delayed perpetual - rg
 							excelRow.addRowItem(11, PafExcelValueObject.createFromBoolean(rg.isDelayedPerpetual()));
 
 							//perpetual allocation - rg
 							excelRow.addRowItem(12, PafExcelValueObject.createFromBoolean(rg.isPerpetualAllocation()));
 							
 							//perform initial allocation - rg
 							excelRow.addRowItem(13, PafExcelValueObject.createFromBoolean(rg.getPerformInitialAllocation()));
 							
 							//add rule group row
 							excelRowList.add(excelRow);
 							
 							Rule[] ruleAr =  rg.getRules();
 							
 							if ( ruleAr != null ) {
 													
 								excelRowList.add(new PafExcelRow());
 								
 								excelRowList.add(PafExcelUtil.createHeaderRow(getHeaderListMap().get(ProjectElementId.RuleSet_Rule.toString())));
 								
 								for ( Rule r : ruleAr ) {
 							
 									//add rule row
 									excelRow = new PafExcelRow();
 									
 									Formula f = r.getFormula();
 									
 									if ( f != null ) {
 										
 										//result term
 										excelRow.addRowItem(2, PafExcelValueObject.createFromString(f.getResultTerm()));
 										
 										//expression
 										excelRow.addRowItem(3, PafExcelValueObject.createFromString(f.getExpression()));
 									
 									}
 									
 									//base allocate measure
 									excelRow.addRowItem(14, PafExcelValueObject.createFromString(r.getBaseAllocateMeasure()));
 									
 									//trigger measures
 									if ( r.getTriggerMeasures() != null ) {
 										
 										List<PafExcelValueObject> valueObjectList = PafExcelValueObjectUtil.createListOfDynamicReferencePafExcelValueObjects(r.getTriggerMeasures(), null);
 																														
 										excelRow.addRowItem(15, PafExcelUtil.getDelimValueObjectFromList(valueObjectList));
 									}
 									
 									//skip allocation
 									excelRow.addRowItem(16, PafExcelValueObject.createFromBoolean(r.isSkipAllocation()));
 									
 									//lock allocation
 									excelRow.addRowItem(17, PafExcelValueObject.createFromBoolean(r.isLockAllocation()));
 									
 									//skip aggeration
 									excelRow.addRowItem(18, PafExcelValueObject.createFromBoolean(r.isSkipAggregation()));
 									
 									//lock system evaluation result
 									excelRow.addRowItem(19, PafExcelValueObject.createFromBoolean(r.isLockSystemEvaluationResult()));
 									
 									//lock user evaluation result
 									excelRow.addRowItem(20, PafExcelValueObject.createFromBoolean(r.getLockUserEvaluationResult()));
 									
 									//eval locked intersections
 									excelRow.addRowItem(21, PafExcelValueObject.createFromBoolean(r.getEvalLockedIntersections()));
 
 									//lock all prior time
 									excelRow.addRowItem(22, PafExcelValueObject.createFromBoolean(r.isLockAllPriorTime()));
 
 									//calc all periods
 									excelRow.addRowItem(23, PafExcelValueObject.createFromBoolean(r.isCalcAllPeriods()));
 
 									//initial TB first allocation
 									excelRow.addRowItem(24, PafExcelValueObject.createFromBoolean(r.isInitialTBFirstAllocation()));
 
 									
 									//add rule row
 									excelRowList.add(excelRow);
 								
 								
 								}
 							}
 						
 						}
 						
 					}
 					
 					
 				}
 				
 			}	
 				
 			PafExcelUtil.writeExcelSheet(input, excelRowList);
 		
 		}
 		
 		//remove invalid rule set tabs
 		if ( existingRuleSetSheetNameList.size() > 0 ) {
 			
 			for (String ruleSetToDelete : existingRuleSetSheetNameList ) {
 				
 				PafExcelUtil.deleteSheet(getWorkbook(), ruleSetToDelete);
 				
 			}
 			
 		}
 		
 	}
 	
 	/**
 	 * 
 	 *  Creates a map to hold generated rule set sheet names.  If the initial sheet name
 	 *  plus the RuleSet_ addition are over 28 characters in length, the final sheet name
 	 *  should be generated with a _1, _2, etc on the end.
 	 *  
 	 *  For example.
 	 *  
 	 *  Initial sheet name (key):
 	 *     	Sheet name 1: iAmAVeryLongLongSheetName1
 	 *     	Sheet name 2: iAmAVeryLongLongSheetName2
 	 *     	Sheet name 3: sheetName3
 	 *   
 	 *  Generated sheet name (value)
 	 *  	Sheet name 1: RuleSet_iAmAVeryLongLongShee_1
 	 *		Sheet name 2: RuleSet_iAmAVeryLongLongShee_2
 	 *		Sheet name 3: RuleSet_sheetName3
 	 *
 	 * @param sheetNameList list of sheet names
 	 * @return map of generated sheet names.
 	 */
 	private Map<String, String> generateSheetNameMap(List<String> sheetNameList) {
 
 		Map<String, String> sheetNameMap = new HashMap<String, String>();
 				
 		if ( sheetNameList != null ) {
 
 			//max lenght of uniqueness
 			int maxSheetLengthUniqueness = 28;
 			
 			Map<String, Integer> uniqueSheetNameMap = new HashMap<String, Integer>();
 			
 			for ( String initialSheetName : sheetNameList ) {
 				
 				String sheetName = RULE_SET_SHEET_IDENT + initialSheetName;
 				
 				if ( sheetName.length() > maxSheetLengthUniqueness  ) {
 									
 					String newSheetName = sheetName.substring(0, maxSheetLengthUniqueness);
 					
 					int uniqueSheetIndex = 1;
 					
 					if ( uniqueSheetNameMap.containsKey(newSheetName)) {
 						
 						uniqueSheetIndex = uniqueSheetNameMap.get(newSheetName);
 						
 						uniqueSheetIndex++;
 						
 					}
 					
 					uniqueSheetNameMap.put(newSheetName, uniqueSheetIndex);
 					
 					sheetName = newSheetName + "_" + uniqueSheetIndex;			
 				} 
 				
 				sheetNameMap.put(initialSheetName, sheetName);
 				
 			}
 				
 		}		
 		
 		return sheetNameMap;
 		
 	}
 
 	/**
 	 * Creates a dynamic reference map used to reference rule set name cells.  Key is rule set name, value
 	 * is the Excel sheet/cell reference.  Example =RuleSet_ContribPct!$A$1.
 	 */
 	public Map<String, String> getDynamicReferenceMap() {
 
 		Map<String, String> dynamicRefMap = null;
 		
 		try {
 			
 			for (String sheetName : getRuleSetSheetNames() ) {
 			
 				PafExcelInput ruleSetInput = new PafExcelInput.Builder(this.getWorkbook(), sheetName, getHeaderListMap().get(ProjectElementId.RuleSet_RuleSet.toString()).size())
 					.headerListMap(this.getHeaderListMap())
 					.excludeHeaderRows(true)
 					.excludeEmptyRows(true)
 					.multiDataRow(true)
 					.startDataReadColumnIndex(0)
 					.rowLimit(1)
 					.sheetRequired(false)
 					.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 					.build();
 					
 					Map<String, String> singleSheetMap = PafExcelUtil.createCellReferenceMap(ruleSetInput);				
 				
 					if ( singleSheetMap != null && singleSheetMap.size() == 1 ) {
 						
 						if ( dynamicRefMap == null ) {
 							
 							dynamicRefMap = new HashMap<String, String>();
 														
 						}
 						
 						dynamicRefMap.putAll(singleSheetMap);
 						
 					}
 				
 			}
 			
 		} catch (PafException e) {
 			
 			logger.warn(ExcelPaceProjectConstants.COULD_NOT_CREATE_THE_REFERENCE_MAP + e.getMessage());
 			
 		}
 		
 		return dynamicRefMap;		
 
 	}
 
 
 }
