 /*
  *	File: @(#)UserSecurityExcelElementItem.java 	Package: com.pace.base.project.excel.elements 	Project: Paf Base Libraries
  *	Created: Jan 11, 2010  		By: jmilliron
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
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.poi.ss.usermodel.Workbook;
 
 import com.pace.base.PafException;
 import com.pace.base.app.*;
 import com.pace.base.project.*;
 import com.pace.base.project.excel.IExcelDynamicReferenceElementItem;
 import com.pace.base.project.excel.PafExcelInput;
 import com.pace.base.project.excel.PafExcelRow;
 import com.pace.base.project.excel.PafExcelValueObject;
 import com.pace.base.project.utils.PafExcelUtil;
 
 /**
  * Reads/writes roles from/to an Excel 2007 workbook.
  *
  * @author jmilliron
  * @version	1.00
  *
  */
 public class UserSecurityExcelElementItem<T extends List<PafUserSecurity>> extends PafExcelElementItem<T> {
 
 	List<String> hierarchyDimList = new ArrayList<String>();
 	
 	/**
 	 * Creates an excel element item. Mainly used for reading user security.
 	 * 
 	 * @param workbook workbook used for read/write
 	 * @param hierarchyDimList list of hierarchy dimensions
 	 */
 	public UserSecurityExcelElementItem(Workbook workbook, List<String> hierarchyDimList) {
 		this(workbook, false, hierarchyDimList);		
 	}
 	
 	/**
 	 * Creates an excel element item. Mainly used for writing user security.
 	 * 
 	 * @param workbook workbook used for read/write
 	 * @param isCellReferencingEnabled true if cell referencing is enabled
 	 * @param hierarchyDimList list of hierarchy dimensions
 	 */
 	public UserSecurityExcelElementItem(Workbook workbook,
 			boolean isCellReferencingEnabled, List<String> hierarchyDimList) {
 		super(workbook, isCellReferencingEnabled);
 
 		//if hierarchy dim list exists, add to existing list
 		if ( hierarchyDimList == null )  {
 									
 			logger.error("No hierarchy dimensions exist.  Please ensure " + ProjectElementId.ApplicationDef.toString() + " tab exists and defines the hierarchy dimensions in workbook.");
 			
 		} else {
 			
 			this.hierarchyDimList.addAll(hierarchyDimList);
 			
 		}
 		
 	}
 
 	@Override
 	protected void createHeaderListMapEntries() {
 
 		getHeaderListMap().put(getSheetName(), new ArrayList<String>(Arrays.asList("username", "domain name", "is testing user", "role name")));												
 
 	}
 
 	/* (non-Javadoc)
 	 * @see com.pace.base.project.excel.elements.PafExcelElementItem#getHeaderListMap()
 	 */
 	@Override
 	public Map<String, List<String>> getHeaderListMap() {
 
 		Map<String, List<String>> headerListMap = super.getHeaderListMap();
 		
 		//if map contans sheet, create new map and append new hierarcy values to end of list
 		if ( this.headerListMap != null && headerListMap.containsKey(getSheetName())) {
 			
 			List<String> newHeaderList = new ArrayList<String>(headerListMap.get(getSheetName()));
 			
 			newHeaderList.addAll(this.hierarchyDimList);
 			
 			headerListMap = new HashMap<String, List<String>>();
 			
 			headerListMap.put(getSheetName(), newHeaderList);		
 			
 		}
 		
 		return headerListMap;
 	}
 
 	@Override
 	public ProjectElementId getProjectElementId() {
 		return ProjectElementId.UserSecurity;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	protected T readExcelSheet() throws PaceProjectReadException, PafException {
 		
 		PafExcelInput input = new PafExcelInput.Builder(getWorkbook(), getSheetName(), getHeaderListMap().get(getSheetName()).size())
 			.headerListMap(getHeaderListMap())
 			.excludeHeaderRows(true)
 			.excludeEmptyRows(true)
 			.sheetRequired(true)
 			.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 			.multiDataRow(true)
 			.build();
 
 		List<PafExcelRow> excelRowList = PafExcelUtil.readExcelSheet(input);
 		
 		List<PafUserSecurity> userSecurityList = new ArrayList<PafUserSecurity>();
 				
 		PafUserSecurity userSecurity = null;
 		
 		for ( PafExcelRow row : excelRowList ) {
 		
 			userSecurity = new PafUserSecurity();
 			
 			List<String> roleNameList = null;
 			
 			int startCaseNum = getHeaderListMap().get(getSheetName()).size() - hierarchyDimList.size();
 			
 			for ( Integer rowIndex : row.getRowItemOrderedIndexes()) {
 			
 				List<PafExcelValueObject> rowItemList = row.getRowItem(rowIndex);
 				
 				PafExcelValueObject firstValueObject = rowItemList.get(0);
 			
 				try {
 				
 					switch (rowIndex) {
 					
 						//username
 						case 0:											
 						
 							userSecurity.setUserName(PafExcelUtil.getString(getProjectElementId(), firstValueObject, true));
 							break;
 						
 						//domain name
 						case 1:						
 							
 							userSecurity.setDomainName(PafExcelUtil.getString(getProjectElementId(), firstValueObject));
 							break;
 							
 						//testing user
 						case 2:											
 						
 							userSecurity.setAdmin(PafExcelUtil.getBoolean(getProjectElementId(), firstValueObject));
 							break;
 							
 						//role name
 						case 3:
 							
 							roleNameList = new ArrayList<String>();
 							
 							roleNameList.addAll(Arrays.asList(PafExcelUtil.getStringAr(getProjectElementId(), rowItemList)));
 							
 							LinkedHashMap<String, PafWorkSpec[]> roleFilterMap = userSecurity.getRoleFilters();
 							
 							for (String roleName : roleNameList ) {
 								
 								//ensure role name is not null
 								if ( roleName != null && ! roleName.trim().equals("")) {
 								
 									PafWorkSpec pws = new PafWorkSpec();
 									
 									pws.setName(roleName);
 									
 									roleFilterMap.put(roleName, new PafWorkSpec[] { pws });
 									
 								}
 																							
 							}
 							
 							userSecurity.setRoleFilters(roleFilterMap);
 							
 							break;
 							
 						//hierarchy dimensions
 						default:
 							
 							if ( roleNameList != null ) {
 								List<String> secList = new ArrayList<String>();
 								secList.addAll(Arrays.asList(PafExcelUtil.getStringAr(getProjectElementId(), rowItemList)));
 								if( secList != null )
 									manageDimensionHiearchy( startCaseNum, userSecurity, roleNameList, secList );
 								//increment start case number.  controls which hier dim index to start on.
 								startCaseNum++;
 							}
 							
 							break;							
 						}			
 					
 					
 					} catch (ExcelProjectDataErrorException epdee) {
 						
 						addProjectDataErrorToList(epdee.getProjectDataError());
 						
 					}
 			
 			
 			}
 		
 		userSecurityList.add(userSecurity);
 		
 		}
 		
 		return (T) userSecurityList;
 		
 	}
 
 	private void manageDimensionHiearchy( int startCaseNum, PafUserSecurity userSecurity, List<String> listRoleName, List<String> listSecurity ) {
 		PafWorkSpec[] pwsAr = null;
 		PafDimSpec[] pafDimSpecAr = null;
 		PafDimSpec pafDimSpec = null;
 		String prevRoleName = null;
 		for( int i=0; i < listRoleName.size(); i++ ) {
 			
 			String roleName =  listRoleName.get(i);
 			if ( roleName != null &&  userSecurity.getRoleFilters().containsKey(roleName)) {
 				prevRoleName = roleName;
 				//It's a new role name
 				pwsAr = userSecurity.getRoleFilters().get(roleName);
 			
 				//get an array of dimension
 				pafDimSpecAr = pwsAr[0].getDimSpec();
 				
 				//set dim name based on end of header list map starting at hier dim location (eg. Product)
 				//if empty, create a new one
 				if ( pafDimSpecAr == null ) {
 					pafDimSpecAr = new PafDimSpec[hierarchyDimList.size()];
 				}
 				pafDimSpec = new PafDimSpec();
 				pafDimSpec.setDimension(getHeaderListMap().get(getSheetName()).get(startCaseNum));
 				//get the expression list for each dimension
 				List<String> secList = new ArrayList<String>();
 				secList.add(listSecurity.get(i));
 				pafDimSpec.setExpressionList(secList.toArray(new String[0]));
 				
 				int hierDimIndex = hierarchyDimList.indexOf(pafDimSpec.getDimension());
 				
 				pafDimSpecAr[hierDimIndex] = pafDimSpec;
 				
 				pwsAr[0].setDimSpec(pafDimSpecAr);
 				
 				userSecurity.getRoleFilters().put(roleName, pwsAr);
 					
 			}
 			if( roleName == null && listSecurity.get(i) != null && prevRoleName != null && pafDimSpec != null ) {
 				String[] expList = pafDimSpec.getExpressionList();
 				List<String> secList = new ArrayList<String>();
 				secList.addAll(Arrays.asList(expList));
 				secList.add(listSecurity.get(i));
 				pafDimSpec.setExpressionList(secList.toArray(new String[0]));
 				int hierDimIndex = hierarchyDimList.indexOf(pafDimSpec.getDimension());
 				
 				pafDimSpecAr[hierDimIndex] = pafDimSpec;
 				
 				pwsAr[0].setDimSpec(pafDimSpecAr);
 				
 				userSecurity.getRoleFilters().put(prevRoleName, pwsAr);
 			}
 		}
 	}
 
 	@Override
 	protected void writeExcelSheet(T t) throws PaceProjectWriteException, PafException {
 				
 		Map<String, String> roleReferenceMap = null;
 		Map<String, String> dimensionRefMap = null;
 		
 		//if cell referencing is enabled, read in versions
 		if ( isCellReferencingEnabled() ) {
 			
 			//roles
 			IExcelDynamicReferenceElementItem elementItem = new RolesExcelElementItem<List<PafPlannerRole>>(getWorkbook());			
 			roleReferenceMap = elementItem.getDynamicReferenceMap();
 			
 			//dimension names
 			IExcelDynamicReferenceElementItem appDefElementItem = new ApplicationDefExcelElementItem<List<PafApplicationDef>>(getWorkbook());
 			dimensionRefMap = appDefElementItem.getDynamicReferenceMap();		
 			
 		
 		}
 		
 				
 		//plan cycle input
 		PafExcelInput input = new PafExcelInput.Builder(getWorkbook(), getSheetName(), getHeaderListMap().get(getSheetName()).size())
 			.endOfSheetIdnt(ExcelPaceProjectConstants.END_OF_SHEET_IDENT)
 			.build();
 		
 		
 		List<PafUserSecurity> userSecuirtyList = t;
 		
 		List<PafExcelRow> excelRowList = new ArrayList<PafExcelRow>();
 
 		PafExcelRow headerRow = PafExcelUtil.createHeaderRow(getHeaderListMap().get(getSheetName()));
 		
 		//if cell ref enabled, try to replace hier dim names with dim cell references
 		if ( isCellReferencingEnabled() ) {
 		
 			Map<Integer, List<PafExcelValueObject>> headerMap = headerRow.getPafExcelValueObjectListMap();
 			
 			for (Integer headerIndex : headerMap.keySet()) {
 				
 				List<PafExcelValueObject> headerValueObjectList = headerMap.get(headerIndex);
 				
 				if ( headerValueObjectList != null && headerValueObjectList.size() == 1 ) {
 					
 					String headerValue = headerValueObjectList.get(0).getValueAsString();
 					
 					headerMap.put(headerIndex, Arrays.asList(PafExcelValueObject.createFromFormulaReferenceMap(headerValue, dimensionRefMap)));
 					
 				}
 				
 			}
 			
 			headerRow.setPafExcelValueObjectListMap(headerMap);
 			
 		}
 		
 		//create and add header to list
 		excelRowList.add(headerRow);
 				
 		if ( userSecuirtyList != null ) {
 		
 			Set<String> hierDimSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
 			
 			if ( this.hierarchyDimList != null ) {
 				
 				hierDimSet.addAll(this.hierarchyDimList);
 				
 			}
 			
 			
 			for ( PafUserSecurity userSecurity : userSecuirtyList ) {
 				
 				PafExcelRow excelRow = new PafExcelRow();
 				
 				//username
 				excelRow.addRowItem(0, PafExcelValueObject.createFromString(userSecurity.getUserName()));
 												
 				//domain name
 				excelRow.addRowItem(1, PafExcelValueObject.createFromString(userSecurity.getDomainName()));
 												
 				//is testing user
 				excelRow.addRowItem(2, PafExcelValueObject.createFromBoolean(userSecurity.getAdmin()));
 										
 				
 				//role filters
 				Map<String, PafWorkSpec[]> roleFilterMap = userSecurity.getRoleFilters();
 				
 				if ( roleFilterMap != null ) {
 					
 					for ( String roleName : roleFilterMap.keySet() ) {
 						
 						//role -> (dimesion, security) list
 						PafWorkSpec[] pafWorkSpecAr = roleFilterMap.get(roleName);
 						
 						if ( pafWorkSpecAr != null ) {
 							for ( PafWorkSpec pafWorkSpec : pafWorkSpecAr ) {
 								//dimension -> security list
 								PafDimSpec[] pafDimSpecAr = pafWorkSpec.getDimSpec();
 																								
 								if ( pafDimSpecAr != null && this.hierarchyDimList != null && pafDimSpecAr.length == this.hierarchyDimList.size()) {
 								
 									Map<String, PafDimSpec> dimSpecMap = new TreeMap<String, PafDimSpec>(String.CASE_INSENSITIVE_ORDER);
 									
 									for (PafDimSpec unOrderPafDimSpec : pafDimSpecAr ) {
 										dimSpecMap.put(unOrderPafDimSpec.getDimension(), unOrderPafDimSpec);
 									}
 										
 									int startDynamicHeaderNdx = 4;
 									int maxExpCount = 0;
 									//go thru dimension-security list and find the max expression counts
 									for (int i = 0; i < pafDimSpecAr.length; i++ ) {
 										
 										String dynamicHierDim = this.hierarchyDimList.get(i);
 										
 										if ( dimSpecMap.containsKey(dynamicHierDim)) {
 											
 											PafDimSpec dimSpec = dimSpecMap.get(dynamicHierDim);
 											
 											if ( dimSpec.getExpressionList() != null ) {
 												//go thru each security
												int expIdx = dimSpec.getExpressionList().length;
 												if( expIdx > maxExpCount ) {
 													maxExpCount = expIdx;
 												}
 											}
 										}
 									}
 									//add members for each dimension
 									for (int i = 0; i < pafDimSpecAr.length; i++ ) {
 										
 										String dynamicHierDim = this.hierarchyDimList.get(i);
 										
 										if ( dimSpecMap.containsKey(dynamicHierDim)) {
 											
 											PafDimSpec dimSpec = dimSpecMap.get(dynamicHierDim);
 											
 											if ( dimSpec.getExpressionList() != null ) {
 												//go thru each security
 												int expIdx = 0;
 												for (String expression : dimSpec.getExpressionList() ) {
 										            //dynamic hier member
 										            excelRow.addRowItem(startDynamicHeaderNdx + i, PafExcelValueObject.createFromString(expression));	
 										            expIdx++;
 												}
												//insert blanks for that dimension if has fewer members than others
 												if( expIdx < maxExpCount ) {
 													for( int ii=0; ii<maxExpCount-expIdx; ii++) {
 											            excelRow.addRowItem(startDynamicHeaderNdx + i, PafExcelValueObject.createBlank());
 													}
 												}
 											}
 											
 										}
 										
 									}
 									//insert blanks under role name column before adding another role 
 									for( int i=0; i < maxExpCount; i++ ) {
 										if( i == 0 )
 											if ( isCellReferencingEnabled() && roleReferenceMap != null && roleReferenceMap.containsKey(roleName) ) {
 												excelRow.addRowItem(3, PafExcelValueObject.createFromFormula(roleReferenceMap.get(roleName)));
 											}
 											else {
 												excelRow.addRowItem(3, PafExcelValueObject.createFromString(roleName));
 											}
 										else {
 											excelRow.addRowItem(3, PafExcelValueObject.createBlank());
 										}
 									}
 								}
 							}
 						}
 					}
 				}
 				excelRowList.add(excelRow);	
 			}
 		}
 			
 		PafExcelUtil.writeExcelSheet(input, excelRowList);
 	}
 }
