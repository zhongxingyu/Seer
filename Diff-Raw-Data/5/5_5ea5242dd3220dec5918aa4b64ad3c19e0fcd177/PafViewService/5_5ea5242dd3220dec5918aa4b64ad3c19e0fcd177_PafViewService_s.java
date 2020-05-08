 /*
  *  File: @(#)PafViewService.java     	Package: com.pace.base.server.data  	Project: PafServer
  *  Created: Aug 1, 2005        		By: JWatkins
  *  Version: x.xx
  *
  *  Copyright (c) 2005-2006 Palladium Group, Inc. All rights reserved.
  *
  *  This software is the confidential and proprietary information of Palladium Group, Inc.
  *  ("Confidential Information"). You shall not disclose such Confidential Information and 
  *  should use it only in accordance with the terms of the license agreement you entered into
  *  with Palladium Group, Inc.
  *
  *
  *
  Date            Author          Version         Changes
  xx/xx/xx        xxxxxxxx        x.xx            ..............
  * 
  */
 package com.pace.server;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.regex.Matcher;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Session;
 
 import com.pace.base.PafBaseConstants;
 import com.pace.base.PafErrHandler;
 import com.pace.base.PafErrSeverity;
 import com.pace.base.PafException;
 import com.pace.base.ViewPrintState;
 import com.pace.base.app.AliasMapping;
 import com.pace.base.app.AliasMemberDisplayType;
 import com.pace.base.app.MdbDef;
 import com.pace.base.app.MeasureDef;
 import com.pace.base.app.PafApplicationDef;
 import com.pace.base.app.SuppressZeroSettings;
 import com.pace.base.app.VersionDef;
 import com.pace.base.app.VersionFormula;
 import com.pace.base.app.VersionType;
 import com.pace.base.comm.IPafViewRequest;
 import com.pace.base.comm.PafViewTreeItem;
 import com.pace.base.comm.SimpleCoordList;
 import com.pace.base.data.Coordinates;
 import com.pace.base.data.Intersection;
 import com.pace.base.data.IntersectionUtil;
 import com.pace.base.data.MemberTreeSet;
 import com.pace.base.data.PafDataSlice;
 import com.pace.base.data.TimeSlice;
 import com.pace.base.db.cellnotes.SimpleCellNote;
 import com.pace.base.db.membertags.MemberTagCommentEntry;
 import com.pace.base.db.membertags.MemberTagCommentPosition;
 import com.pace.base.db.membertags.MemberTagData;
 import com.pace.base.db.membertags.MemberTagDef;
 import com.pace.base.db.membertags.MemberTagViewSectionData;
 import com.pace.base.db.membertags.SimpleMemberTagId;
 import com.pace.base.mdb.AttributeUtil;
 import com.pace.base.mdb.PafBaseTree;
 import com.pace.base.mdb.PafDataCache;
 import com.pace.base.mdb.PafDataSliceParms;
 import com.pace.base.mdb.PafDimMember;
 import com.pace.base.mdb.PafDimMemberProps;
 import com.pace.base.mdb.PafDimTree;
 import com.pace.base.mdb.PafDimTree.LevelGenType;
 import com.pace.base.mdb.SortingTuple;
 import com.pace.base.mdb.SortingTuples;
 import com.pace.base.project.ProjectElementId;
 import com.pace.base.state.PafClientState;
 import com.pace.base.ui.PrintStyle;
 import com.pace.base.ui.PrintStyles;
 import com.pace.base.utility.CompressionUtil;
 import com.pace.base.utility.PafViewSectionUtil;
 import com.pace.base.utility.StringUtils;
 import com.pace.base.utility.UserSelectionUtil;
 import com.pace.base.view.Dimension;
 import com.pace.base.view.HierarchyFormat;
 import com.pace.base.view.LockedCell;
 import com.pace.base.view.PafAxis;
 import com.pace.base.view.PafMVS;
 import com.pace.base.view.PafNumberFormat;
 import com.pace.base.view.PafUserSelection;
 import com.pace.base.view.PafView;
 import com.pace.base.view.PafViewHeader;
 import com.pace.base.view.PafViewSection;
 import com.pace.base.view.PageTuple;
 import com.pace.base.view.ViewTuple;
 
 public class PafViewService {
 
 	private static PafViewService _instance;
 
 	private PafDataService pafDataService = PafDataService.getInstance();
 
 	private PafView[] viewCache;
 
 	private Map<String, PafNumberFormat> globalNumericFormatCache = null;
 
 	private MeasureDef[] measuresCache = null;
 
 	private VersionDef[] versionsCache = null;
 
 	private Map<String, Boolean> measuresPlannableCache = null;
 
 	private Map<String, PafNumberFormat> measuresMapCache = null;
 
 	private Map<String, PafNumberFormat> versionsNumberFormatMapCache = null;
 
 	private Map<String, VersionType> versionsTypeCache = null;
 
 	private Map<String, HierarchyFormat> hierarchyFormatsCache = null;
 	
 	//TTN 900 - Print Preferences
 	private Map<String, PrintStyle> globalPrintStyleCache = null; //<guid, PrintStyle>
 	private PrintStyles printStyles;
 	
 	private PafApplicationDef pafApp = null;
 	
 	static private Map<String, String> invalidViewsMap;
 	
 	private Map<String, String> invalidViewSectionsMap;
 	
 	private Map<String, Map<String, PafAxis>> validMemberTagMap = null;
 
 	private static Logger logger = Logger.getLogger(PafViewService.class);
 	
 
 
 
 	public void getViewMetaData() {
 		// IMdStore mdstore = MdFactory.createIMdStore("test1");
 		// PafHierarchy = mdStore.getDimensionData("Measures", "Net Income", 0);
 		// return;
 	}
 
 	public void updateViewCache(String viewKey, PafView view) {
 		// TODO Implement update view cache
 
 	}
 
 	public void saveViewCache() {
 		if (viewCache != null)
 			PafMetaData.exportScreens(viewCache);
 	}
 
 	public void loadViewCache() {
 
 		logger.info("Loading view cache");
 
 		viewCache = assembleViews();
 
 		
 		globalNumericFormatCache = PafMetaData.getPaceProject().getNumericFormats();
 		hierarchyFormatsCache = PafMetaData.getPaceProject().getHierarchyFormats();
 		//TTN 900 - load global print styles
 		globalPrintStyleCache = PafMetaData.getPaceProject().getPrintStyles();
 		printStyles = new PrintStyles();
 		printStyles.setPrintStyles(globalPrintStyleCache);
 
 		loadMeasuresCache();
 		loadVersionsCache();
 		
 		
 		// Initialize member tag map
 		setValidMemberTagMap(null);
 	}
 
 	/**
 	 * Imports the views and view sections and merges them into 1 set of paf
 	 * views.
 	 * 
 	 * @return PafView
 	 * 
 	 */
 	public PafView[] assembleViews() {
 
 		
 //		if ( PafMetaData.isDebugMode()) {
 			
 			//reload view groups, views and view sections (TTN-1456)
 			try {
 				PafMetaData.getPaceProject().loadData(new HashSet<ProjectElementId>(Arrays.asList(ProjectElementId.ViewGroups, ProjectElementId.Views, ProjectElementId.ViewSections)));
 			} catch (PafException e) {
 				logger.error(e.getMessage());
 			}
 			
 //		}
 		
 		
 		//import view array of objects
 		PafView[] pafViews = PafMetaData.getPaceProject().getViews().toArray(new PafView[0]);
 		
 		//import view section array of objects
 		PafViewSection[] pafViewSections = PafMetaData.getPaceProject().getViewSections().toArray(new PafViewSection[0]);
 
 		//initialize the invalid views and view sections list.  This will be filled
 		//with views and view sections that are invalid and the value part of the 
 		//map will be the error message
 		invalidViewsMap = new HashMap<String, String>();
 		invalidViewSectionsMap = new HashMap<String, String>();
 		
 		// if view sections exist
 		if (pafViewSections != null) {
 
 			//create a map to hold the view section name and view section object
 			Map<String, PafViewSection> pafViewSectionMap = new HashMap<String, PafViewSection>();
 
 			// populate view section map by looping over view section objects
 			for (PafViewSection pafViewSection : pafViewSections) {
 				
 				//check if view section is valid.  If either col or row tuples are null, then this would
 				//be an invalid view section
 				if ( pafViewSection.getColTuples() == null || pafViewSection.getRowTuples() == null ) {
 					
 					logger.error("View Section: " + pafViewSection.getName() + " was not a valid view section");
 					
 					//add error message to invalidViewSection map
 					if ( pafViewSection.getColTuples() == null && pafViewSection.getRowTuples() == null ) {
 						invalidViewSectionsMap.put(pafViewSection.getName(), "View Section '" 
 								+ pafViewSection.getName() + "' didn't include row or column tuples");
 					} else if ( pafViewSection.getColTuples() == null ) {
 						invalidViewSectionsMap.put(pafViewSection.getName(), "View Section '" 
 								+ pafViewSection.getName() + "' didn't include column tuples");
 					} else if ( pafViewSection.getRowTuples() == null ) {
 						invalidViewSectionsMap.put(pafViewSection.getName(), "View Section '" 
 								+ pafViewSection.getName() + "' didn't include row tuples");
 					}
 								
 					//continue to next view section
 					continue;
 					
 				}
 				
 				// Apply default property values on each row and column tuple
 				// the default property of this now should be false if null
 				// (04/24/2006)
 				for (ViewTuple viewTuple : pafViewSection.getColTuples()) {
 					if (viewTuple.getParentFirst() == null)
 						viewTuple.setParentFirst(false);
 				}
 				// the default property of this now should be false if null
 				// (04/24/2006)
 				for (ViewTuple viewTuple : pafViewSection.getRowTuples()) {
 					if (viewTuple.getParentFirst() == null)
 						viewTuple.setParentFirst(false);
 				}
 
 				/*
 				 * if the map already contains the key, throw a duplicate view
 				 * section error otherwise add view section to map.
 				 */
 				if (!pafViewSectionMap.containsKey(pafViewSection.getName())) {
 
 					pafViewSectionMap.put(pafViewSection.getName(),
 							pafViewSection);
 
 				} else {
 
 					PafErrHandler.handleException(new PafException(
 							"Duplicate View Section: " + pafViewSection.getName(), PafErrSeverity.Error));
 					
 					//continue to next view section if duplicate
 					continue;
 
 				}
 
 			}
 
 			// if views exist and view sections exist
 			if (pafViews != null && pafViewSectionMap != null) {
 
 				//create a list to hold
 				ArrayList<PafView> validPafViewsList = new ArrayList<PafView>();
 				
 				Map<String, PafUserSelection> pafUserSelectionsMap = getUserSelectionsMap();
 
 				// loop through all views and assign view section arrays
 				for (PafView pafView : pafViews) {
 
 					//create empty set of paf user selections
 					Set<PafUserSelection> pafUserSelectionsSet = new LinkedHashSet<PafUserSelection>();
 
 					//get the view section names from the view object
 					String[] viewSectionsNames = pafView.getViewSectionNames();
 
 					//basic boolean to state if current view is valid or not
 					boolean validView = true;
 					
 					// if view section names exist
 					if (viewSectionsNames != null && viewSectionsNames.length != 0 ) {
 												
 						//create an empty list to hold view sections
 						ArrayList<PafViewSection> pafViewSectionList = new ArrayList<PafViewSection>();
 
 						// for each view section name, see if exist in map and
 						// add to array list if exist
 						for (String viewSectionName : viewSectionsNames) {
 
 							// if view section map contains the view section
 							// name
 							if (pafViewSectionMap.containsKey(viewSectionName)) {
 
 								// get view section object from view section map
 								PafViewSection pafViewSection = pafViewSectionMap
 										.get(viewSectionName);
 
 								// find the user selections set based on current
 								// view section
 								Set<String> userSelectionNameSet = UserSelectionUtil
 										.findUserSelections(pafViewSection);
 
 								// for each user selection name found, check to
 								// see if in user
 								// selection map and if so, get it and assign it
 								// to the user selection
 								// set.
 								for (String userSelectionName : userSelectionNameSet) {
 
 									// if user selection name exist in the user
 									// selection map
 									if (pafUserSelectionsMap
 											.containsKey(userSelectionName)) {
 
 										PafUserSelection pafUserSelection = null;
 										try {
 											
 											//get user selection from map and clone
 											pafUserSelection = (PafUserSelection) pafUserSelectionsMap
 													.get(userSelectionName)
 													.clone();
 											
 											//get paf axis from view section util
 											PafAxis pafAxis = PafViewSectionUtil.getDimensionAxis(pafViewSection, pafUserSelection.getDimension());
 											
 											//set paf axis
 											pafUserSelection.setPafAxis(pafAxis);
 											
 											// add to user selection set
 											pafUserSelectionsSet
 													.add(pafUserSelection);
 										} catch (CloneNotSupportedException e) {
 											// do nothing
 										}
 
 									}
 
 								}
 
 								// add view section to the view section list
 								pafViewSectionList.add(pafViewSection);
 
 							} else {
 								
 								//view is invalid
 								validView = false;
 								
 								//write to error log
 								logger.error("View '" + pafView.getName() + "' could not assign View Section " + 
 										viewSectionName + " because view Section was invalid.");
 								
 								//if view seciton exist, but was not loaded because of an error
 								if ( invalidViewSectionsMap.containsKey(viewSectionName)) {
 									
 									//get view section error message
 									String viewSectionErrorMsg = invalidViewSectionsMap.get(viewSectionName);
 									
 									//add view to invalid views map with error message from view section
 									invalidViewsMap.put(pafView.getName(), viewSectionErrorMsg); 
 									
 								} else {
 									
 									//add view to invalid views map with error message
 									invalidViewsMap.put(pafView.getName(), "View Section '" 
 											+ viewSectionName + "' does not exist.");
 									
 								}
 								
 								//continue to next view section name
 								continue;
 							}
 
 						}
 
 						// convert the list to an array and set to the view
 						pafView.setViewSections(pafViewSectionList
 								.toArray(new PafViewSection[0]));
 
 					} else {
 						
 						//create error message
 						String errMsg = "View '" + pafView.getName() + "' did not have any view sections assigned to it.";
 						
 						//add view and error message to invalid views map
 						invalidViewsMap.put(pafView.getName(), errMsg);
 						
 						//write to error log
 						logger.error(errMsg);
 						
 						//set view invalid
 						validView = false;
 						
 					}
 
 					// if there are more than 0 user selections, add to the paf
 					// view
 					if (pafUserSelectionsSet.size() > 0) {
 
 						pafView.setUserSelections(pafUserSelectionsSet
 								.toArray(new PafUserSelection[0]));
 
 					} else {
 
 						pafView
 								.setUserSelections(new PafUserSelection[] { null });
 					}
 					
 					//if view is still valid, add to valid views list
 					if ( validView ) {
 						validPafViewsList.add(pafView);
 					}
 
 				}
 
 				//set pafViews to a valid array of paf view objects
 				pafViews = validPafViewsList.toArray(new PafView[0]);
 				
 			}			
 			
 		}
 
 		// return the full paf view with populated view sections
 		return pafViews;
 	}
 
 	private Map<String, PafUserSelection> getUserSelectionsMap() {
 
 		// import he paf user selections
 		List<PafUserSelection> pafUserSelections = PafMetaData.getPaceProject().getUserSelections();
 
 		// create a map and populate it with
 		Map<String, PafUserSelection> pafUserSelectionsMap = new HashMap<String, PafUserSelection>();
 
 		// if the user selections is not null, add all user selections to a map
 		if (pafUserSelections != null) {
 
 			for (PafUserSelection pafUserSelection : pafUserSelections) {
 
 				// populate user selections map with user selection
 				pafUserSelectionsMap.put(pafUserSelection.getId(),
 						pafUserSelection);
 
 			}
 
 		}
 
 		return pafUserSelectionsMap;
 	}
 
 	public void loadMeasuresCache() {
 
 			measuresCache = PafMetaData.getPaceProject().getMeasures().toArray(new MeasureDef[0]);
 
 			populateMeasuresPlannableCache();
 
 			populateMeasuresMapCache();
 
 	}
 
 	public void populateMeasuresPlannableCache() {
 
 		measuresPlannableCache = new TreeMap<String, Boolean>();
 
 		if (measuresCache != null) {
 
 			for (MeasureDef measure : measuresCache) {
 				measuresPlannableCache.put(measure.getName(), measure
 						.getPlannable());
 			}
 
 		}
 
 	}
 
 	public void populateMeasuresMapCache() {
 
 		measuresMapCache = new HashMap<String, PafNumberFormat>();
 
 		if (measuresCache != null) {
 
 			PafNumberFormat defaultFormat = getDefaultNumberFormat();
 
 			for (MeasureDef measure : measuresCache) {
 
 				if (measure.getNumericFormatName() != null) {
 
 					measuresMapCache.put(measure.getName(),
 							globalNumericFormatCache
 									.get(measure.getNumericFormatName()));
 
 				} else {
 
 					if (defaultFormat != null) {
 						measuresMapCache.put(measure.getName(), defaultFormat);
 					}
 				}
 			}
 		}
 	}
 
 	public void loadVersionsCache() {
 		
 		versionsCache = PafMetaData.getPaceProject().getVersions().toArray(new VersionDef[0]);
 
 		versionsTypeCache = PafMetaData.getVersionTypeMap();
 		
 		populateVersionsMapCache();
 
 	}
 
 	
 	/**
 	 * Populates the version type cache with all the versions from the 
 	 * version cache and their version type. If a version is derived meaning
 	 * not NonPlannable, Plannable, or ForwardPlannable, then the base version
 	 * from the version formula is used to get the base version name, then that 
 	 * name is used to get the base version type.  
 	 * 
 	 * For example, if the derived version is WP_vs_LY, then the base version would be
 	 * WP, and if WP was Plannable, then WP_vs_LY should be added to the version type
 	 * cache as Plannable.
 	 */
 	
 	public void populateVersionsTypeCache() {
 
 		//create a new instance of versions type cache
 		versionsTypeCache = new HashMap<String, VersionType>();
 		
 		//if the cache is not null
 		if (versionsCache != null) {
 			
 			//create a new instance to hold all the derived versions
 			Set<VersionDef> derivedVersions = new HashSet<VersionDef>();
 
 			//for each version def in the versions cache, loop
 			for (VersionDef versionDef : versionsCache) {
 								
 				//get the version type from the version def
 				VersionType versionType = versionDef.getType();
 								
 				//if version type is not null, check to see if it's derived, if so
 				//add to the derived set else add to the version type cache.
 				if ( versionType != null ) {
 				
 					//if derived version
 					if ( ! versionType.equals(VersionType.Plannable) &&
 							! versionType.equals(VersionType.NonPlannable) &&
 							! versionType.equals(VersionType.ForwardPlannable) ) {
 						
 						//add to derived version set
 						derivedVersions.add(versionDef);
 						
 					} else {
 					
 						//add to the version type cache
 						versionsTypeCache.put(versionDef.getName(), versionType);
 						
 					}
 				}
 				
 			}
 			
 			//loop over the derived versions
 			for (VersionDef derivedVersionDef : derivedVersions ) {
 									
 				//get the version formula so we can get the base version name
 				VersionFormula versionFormula = derivedVersionDef.getVersionFormula();
 				
 				//if the version formula exist and a base version name exist
 				if ( versionFormula != null && versionFormula.getBaseVersion() != null ) {
 					
 					//get base version name
 					String baseVersion = versionFormula.getBaseVersion();
 					
 					//if a base version is not null
 					if ( baseVersion != null ) {
 											
 						//if base version exist in key
 						if ( versionsTypeCache.containsKey(baseVersion)) {
 							
 							//get base version type from versions type cache
 							VersionType baseVersionType = versionsTypeCache.get(baseVersion);
 											
 							//add the derived version to the version type cache with it's
 							//base version type
 							versionsTypeCache.put(derivedVersionDef.getName(), baseVersionType);
 							
 						}
 						
 					}
 					
 				}				
 				
 			}
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void populateVersionsMapCache() {
 
 		//create a new instance of the version map cache
 		versionsNumberFormatMapCache = new HashMap<String, PafNumberFormat>();
 			
 		//loop through the versions cache
 		for (VersionDef version : versionsCache) {
 			
 			//if numeric format name is not null, then put the version/format 
 			//into the map
 			if (version.getNumericFormatName() != null) {
 				
 				versionsNumberFormatMapCache.put(version.getName(),
 						globalNumericFormatCache
 								.get(version.getNumericFormatName()));
 				
 			}
 		}
 	}
 
 	public static PafViewService getInstance() {
 		if (_instance == null) {
 			logger.info("Initializing PafViewService");			
 			_instance = new PafViewService();
 		}
 
 		return _instance;
 	}
 
 	private PafViewService() {	}
 
 
 	/**
 	 * Given a view name, returns a pafview object. This is directly available
 	 * via the service interface. The view is searched for in the viewCache,
 	 * which is built upon class loading.
 	 * 
 	 * @param viewRequest
 	 *            Unique view name
 	 * @param clientState
 	 *            Client identification tokens
 	 * 
 	 * @return Complex view object
 	 * @throws PafException
 	 */
 
 	public PafView getView(IPafViewRequest viewRequest, PafClientState clientState)
 			throws PafException {
 
 		PafView definedView = null, renderedView = null;
 		String viewName = viewRequest.getViewName();
 		Set<Coordinates> explodedSessionLocks = new HashSet<Coordinates>();
 		boolean bSessionLocksExploded = false;
 
 		logger.info("View Requested: " + viewName
 				+ " by client: " + viewRequest.getClientId());
 
 
 		try {
 			// reload view cache every time if in debug mode. always reload
 			// measures and versions
 			if (PafMetaData.isDebugMode()) {
 				loadViewCache();
 			} else {
 				loadMeasuresCache();
 				loadVersionsCache();
 			}
 
 			// Get data cache (TTN-1893)
 			PafDataCache dataCache = pafDataService.getDataCache(clientState.getClientId());
 			
 			// try to find the view by name in the view cache
 			for (int i = 0; i < viewCache.length; i++) {
 				
 				if ( invalidViewsMap.containsKey(viewName)) {
 					
 					throw new PafException(invalidViewsMap.get(viewName), PafErrSeverity.Warning);
 					
 				}
 				
 				if (viewCache[i].getName().equals(viewName)) {
 					logger.debug("Checking view: " + viewCache[i].getName());
 					definedView = viewCache[i];
 					logger.debug(viewCache[i].getDesc());
 					i = viewCache.length;
 				}
 			}
 
 			// if no view found, throw a paf exception
 			if (definedView == null) {
 				throw new PafException("View not found: "
 						+ viewName, PafErrSeverity.Warning);
 			}
 
 			// get current paf app
 			pafApp = clientState.getApp();
 			
 
 			Properties tokenCatalog = clientState.generateTokenCatalog(new Properties());
 			clientState.setTokenCatalog(tokenCatalog);
 			
 			// clone original view for rendering purposes. This is done to avoid
 			// collisions between multiple user threads (TTN-1066)
 			renderedView = definedView.clone();
 			
 			// replace operators used in tuple definitions
 			renderedView = replaceUserOperators(renderedView, viewRequest.getUserSelections(), clientState);
 		
 			//TTN 900 - Print Settings - populate PrintStyle object if the view is not using the embedded print style
 			if( renderedView.getViewPrintState() != null && globalPrintStyleCache != null ) {
 				if( renderedView.getViewPrintState() == ViewPrintState.GLOBAL )
 					renderedView.setPrintStyle(globalPrintStyleCache.get(renderedView.getGlobalPrintStyleGUID()));
 				else if( renderedView.getViewPrintState() == ViewPrintState.DEFAULT ) {
 					renderedView.setPrintStyle(printStyles.getDefaultPrintStyle());
 				}
 			}
 			
 			PafViewSection[] sections = renderedView.getViewSections();
 			
 			//Set View Section attribute properties
 			initView(sections, clientState);
 
 			pafDataService.expandView(renderedView, clientState);
 			
 			// Populate each view section with required data
 			logger.info("Fetching view data: " + viewName);
 			for (PafViewSection viewSection : renderedView.getViewSections()) {
 				
 				if( ! viewSection.isEmpty() ) {
 					boolean isViewSectionChanged = false;
 					
 					// Add view and all of it's view sections to "Materialized View
 					// Section" collection or use previous MVS entry if it exists
 					String mvsKey = PafMVS.generateKey(renderedView, viewSection);
 					PafMVS pafMVS = clientState.getMVS(mvsKey);
 					PafDataSliceParms prevDataSliceParms = null;
 					if (pafMVS == null ){
 						pafMVS = new PafMVS(renderedView, viewSection);
 						clientState.addMVS(pafMVS.getKey(), pafMVS);
 					} else {
 						// MVS exists - also get previous data slice parms
 						prevDataSliceParms = pafMVS.getDataSliceParms();
 					}
 				
 					// Populate data slice
 					PafDataSlice dataSlice = pafDataService.getDataSlice(renderedView, viewSection, clientState, false);
 					viewSection.setPafDataSlice(dataSlice);
 									
 					//Suppress Zeros ***********************************************************				
 					//Resolve the Suppress Zero Settings - view section specific settings override global settings
 					String appId = PafAppService.getInstance().getApplications().get(0).getAppId();
 					viewSection.setSuppressZeroSettings(PafAppService.getInstance().resolveSuppressZeroSettings(viewSection.getSuppressZeroSettings(), appId));
 									
 					LinkedHashSet<Integer> suppressedRows = new LinkedHashSet<Integer>();
 					LinkedHashSet<Integer> suppressedColumns = new LinkedHashSet<Integer>();
 					
 					//If it is possible for the user to make setting changes
 					if (viewSection.getSuppressZeroSettings().getEnabled() == true && 
 							viewSection.getSuppressZeroSettings().getVisible() == true){
 						
 						//get settings from client
 						viewSection.getSuppressZeroSettings().setColumnsSuppressed(viewRequest.getColumnsSuppressed());
 						viewSection.getSuppressZeroSettings().setRowsSuppressed(viewRequest.getRowsSuppressed());
 						
 						if(viewSection.getSuppressZeroSettings().getRowsSuppressed() == true || viewSection.getSuppressZeroSettings().getColumnsSuppressed() == true){
 													
 					    	suppressZeros(viewSection, suppressedRows, suppressedColumns);
 					    	suppressTuples(viewSection, suppressedRows, suppressedColumns);
 					    	
 					    	if(prevDataSliceParms != null){
 						    	if(suppressedViewIsChanged(viewSection, prevDataSliceParms) == true){
 						    		renderedView.setDirtyFlag(true);
 						    		isViewSectionChanged = true;
 						    	}
 					    	}
 						}
 					}
 					else{
 						//otherwise use the original settings
 						if(viewSection.getSuppressZeroSettings().getRowsSuppressed() == true || 
 								viewSection.getSuppressZeroSettings().getColumnsSuppressed() == true){
 							
 					    	suppressZeros(viewSection, suppressedRows, suppressedColumns);
 					    	suppressTuples(viewSection, suppressedRows, suppressedColumns);
 					    	
 					    	if(prevDataSliceParms != null){
 						    	if(suppressedViewIsChanged(viewSection, prevDataSliceParms) == true){
 						    		renderedView.setDirtyFlag(true);
 						    		isViewSectionChanged = true;
 						    	}
 					    	}
 						}
 					}
 					
 					// Set the suppressed flag
 					if (suppressedRows.size() > 0 || suppressedColumns.size() > 0){
 						viewSection.setSuppressed(true);
 					}else{
 						viewSection.setSuppressed(false);
 					}
 					
 					// Build data slice parameters after the suppression
 	//				if (isViewSectionChanged) {
 						PafDataSliceParms sliceParms = pafDataService.buildDataSliceParms(viewSection);
 						pafMVS.setDataSliceParms(sliceParms);
 	//				}
 					
 					// Populate cell note data
 					CellNoteCache noteCache = CellNoteCacheManager.getInstance().getNoteCache(viewRequest.getClientId() );
 					SimpleCellNote[] simpleCellNotes = noteCache.getAllNotes(viewSection.getDimensionsPriority());
 					viewSection.setCellNotes(simpleCellNotes);
 					
 					//TTN-1228
 					viewSection.setReadOnly(viewSection.isReadOnly() || clientState.getPlannerRole().isReadOnly());
 					
 					// Explode session locks for later attribute view processing. Only do once per 
 					// getView() request. Don't bother doing if view section is read only. (TTN-1893)
					if (viewSection.hasAttributes() && !bSessionLocksExploded && !viewSection.isReadOnly()) {
						explodedSessionLocks = pafDataService.explodeSessionLocks(viewRequest.getSessionLockedCells(), clientState);
 						bSessionLocksExploded = true;
 					}
 
 					// 
 					// compress slice for return.
 					//try {
 						//if the data array is null set it to null.  The client checks for the null.
 						if (dataSlice.getData().length == 0){
 							dataSlice.setData(null);
 						}else{ 
 							//dataSlice.compressData();
 						}
 					//}
 	//				catch (IOException iox) {
 	//					throw new PafException(iox.getLocalizedMessage(), PafErrSeverity.Error);
 	//				}
 				}
 			}
 			
 			// resolve numeric formatting for measure/versions
 			sections = resolveNumericFormatting(sections); //ok
 			
 			sections = lockVersionIntersections(sections, clientState); //ok
 
 			sections = lockMeasureIntersections(sections, clientState); //ok
 			
 			sections = addNonPlannableTuplesToClientState(sections, explodedSessionLocks, dataCache);
 
 			sections = applyReplicationSecurity(sections);
 
 			sections = applyHierarchyFormatting(sections); //ok
 
 			sections = applyMeasureSecurity(sections); //?
 			
 			sections = applyMemberTagSecurity(sections);
 			
 			sections = applyGlobalAliasMappings(sections);
 			
 			for (PafViewSection viewSection : renderedView.getViewSections()) {
 				// Populate member tag data
 				if( ! viewSection.isEmpty() ) {
 				
 					//Member Tag Data cache
 					Map<SimpleMemberTagId,Map<Intersection, MemberTagData>> memberTags = new HashMap<SimpleMemberTagId,Map<Intersection, MemberTagData>>();
 					
 					viewSection = populateMemberTagIntersections(viewSection, new PafAxis(PafAxis.ROW), memberTags);
 					viewSection = populateMemberTagIntersections(viewSection, new PafAxis(PafAxis.COL), memberTags);
 					viewSection = populateMemberTagComments(viewSection, memberTags);
 					
 					//Resolve page headers
 					viewSection = resolvePageHeaders(viewSection, clientState.getPlanningVersion().getName(), viewRequest.getUserSelections(), tokenCatalog, viewName);		
 	//				viewSection = resolvePageHeaders(viewSection, clientState.getPlanningVersion().getName(), viewRequest.getUserSelections(), clientState.generateTokenCatalog(new Properties()), viewName);		
 				}
 			}
 			
 		} catch (Exception ex) {
 			logger.warn("Exception getting view " + ex.getMessage());
 			ex.printStackTrace();
 			PafErrHandler.handleException(ex, PafErrSeverity.Error);
 			throw new PafException(ex);
 		}
 		logger.info(String.format("Returning rendered view: [%s]", viewName));
 		return renderedView;
 	}
 
 	/**
 	 *  Apply global alias mapping to view sections
 	 *
 	 * @param sections Array of view sections
 	 * @return PafViewSection[]
 	 */
 	private PafViewSection[] applyGlobalAliasMappings(PafViewSection[] sections) {
 
 		Map<String, AliasMapping> globalAliasMappingByDim = PafMetaData.getGlobalAliasMappingsByDim();  //TTN-1454
 		AliasMapping[] aliasMappings = configureGlobalAliasMappings(globalAliasMappingByDim, sections);  //TTN-1454
 
 		// Resolve alias mappings on each view section
 		for (int i = 0; i < sections.length; i++) {
 			if( ! sections[i].isEmpty() ) {
 				sections[i].setAliasMappings(aliasMappings);
 			}
 		}
 		return sections;
 	}
 
 	/**
 	 *  Apply member tag settings and check for invalid member tag intersections
 	 *
 	 * @param sections Array of view sections
 	 * @return PafViewSection[]
 	 */
 	private PafViewSection[] applyMemberTagSecurity(PafViewSection[] sections) {
 		
 		Map<String, MemberTagDef> memberTagDefs = pafApp.getMemberTagDefs();
 		Map<String, Map<String, PafAxis>> validMemberTagMap = getValidMemberTagMap();
 
 		for (PafViewSection section:sections) {
 			if( ! section.isEmpty() ) {
 				// Initialization
 				ViewTuple[] rowTuples = section.getRowTuples();
 				ViewTuple[] colTuples = section.getColTuples();
 				Map<String, MemberTagCommentEntry> mtCommentEntryMap = section.getMemberTagCommentEntryMap();
 				List<MemberTagCommentEntry> visibleMtCommentEntries = new ArrayList<MemberTagCommentEntry>();
 				List<LockedCell> invalidMemberTagIntersections = new ArrayList<LockedCell>();
 				String sectionName = section.getName();
 				boolean isFirstTimeThruCols = true;
 	
 				// Initialize the set of valid member tags for this view section (Lazy loaded)
 				if (!validMemberTagMap.containsKey(sectionName)) {
 					Map<String, PafAxis> validMemberTags = new HashMap<String, PafAxis>();
 					for (String memberTagName:memberTagDefs.keySet()) {
 						PafAxis axis = findValidMemberTagAxis(memberTagName, section);
 						if (axis != null) {
 							validMemberTags.put(memberTagName, axis);
 						}
 					}
 					validMemberTagMap.put(sectionName, validMemberTags);
 				}
 				
 				// Create an array of valid member tag comments for this view section
 				Map<String, PafAxis> validMemberTags = validMemberTagMap.get(sectionName);			
 				for (String memberTagName:validMemberTags.keySet()) {
 					
 					// Reconcile possible overrides on member tag comment visible property
 					if (!mtCommentEntryMap.containsKey(memberTagName)) {
 						// No override on comment - Choose member tags with a default comment visible property of true
 						MemberTagDef mtDef = memberTagDefs.get(memberTagName);
 						if (mtDef.isCommentVisible()) {
 							MemberTagCommentEntry visibleMtCommentEntry = new MemberTagCommentEntry(memberTagName, mtDef.isCommentVisible());
 							visibleMtCommentEntries.add(visibleMtCommentEntry);
 						}
 					} else {
 						// Override on comment - Choose member tags whose comment visible override value is true
 						MemberTagCommentEntry overrideEntry = mtCommentEntryMap.get(memberTagName);
 						if (overrideEntry.isVisible()) {
 							MemberTagCommentEntry visibleMtCommentEntry = new MemberTagCommentEntry(memberTagName, overrideEntry.isVisible());
 							visibleMtCommentEntries.add(visibleMtCommentEntry);						
 						}
 	
 					}
 				}
 				section.setMemberTagCommentEntries(visibleMtCommentEntries.toArray(new MemberTagCommentEntry[0]));
 				
 				// Apply member tag security to each row and column tuple. In addition, 
 				// each row/col tuple intersection will be checked to see if it is a 
 				// valid member tag intersection. 
 				for (int rowInx = 0; rowInx < rowTuples.length; rowInx++) {
 					
 					// Skip this row tuple if it doesn't contain a member tag and
 					// the column tuples have already been checked.
 					ViewTuple rowTuple = rowTuples[rowInx];
 					if (!rowTuple.isMemberTag() && !isFirstTimeThruCols) {
 						continue;
 					}
 					
 					// Apply member tag security to row tuple
 					rowTuple = applyMemberTagSecurityToTuple(rowTuple);
 					
 					// Check each column tuple
 					for (int colInx = 0; colInx < colTuples.length; colInx ++) {
 						ViewTuple colTuple = colTuples[colInx];
 						
 						// Apply member tag security to column tuple
 						if (isFirstTimeThruCols) {
 							colTuple = applyMemberTagSecurityToTuple(colTuple);
 						}
 						
 						// Check if member tag intersection is invalid. An intersection is 
 						// invalid if it consists of a member tag on both the row and 
 						// column axes.
 						if (rowTuple.isMemberTag() && colTuple.isMemberTag()) {
 							// Add intersection to invalid member tag intersection collection (1-based)
 							LockedCell invalidMemberTagIntersection = new LockedCell(rowInx + 1, colInx + 1);
 							invalidMemberTagIntersections.add(invalidMemberTagIntersection);
 						}
 					}
 					
 					isFirstTimeThruCols = false;
 					
 				}
 				// Set the invalid member tag intersections on this view section
 				section.setInvalidMemberTagIntersectionsLC(invalidMemberTagIntersections.toArray(new LockedCell[0]));
 			}
 		}
 		
 		// Return the updated view sections
 		return sections;
 	}
 
 	/**
 	 *  Apply member tag security to supplied tuple
 	 *
 	 * @param viewTuple View tuple
 	 */
 	private ViewTuple applyMemberTagSecurityToTuple(ViewTuple viewTuple) {
 		
 		if (viewTuple.isMemberTag()) {
 			// If isMemberTagEditable property is null, use global property
 			if (viewTuple.getIsMemberTagEditable() == null) {
 				String memberTagName = viewTuple.getMemberDefs()[0];
 				MemberTagDef memberTagDef = pafApp.getMemberTagDef(memberTagName);
 				Boolean editable = new Boolean(memberTagDef.isEditable());
 				viewTuple.setIsMemberTagEditable(editable);
 			}
 		}
 		
 		// Return updated view tuple
 		return viewTuple;
 	}
 
 	
 	/**
 	 *  Determine which intersections are unsuitable for replication
 	 *
 	 * @param sections Array of view sections
 	 * @return PafViewSection[]
 	 */
 	private PafViewSection[] applyReplicationSecurity(PafViewSection[] sections) {
 
 		String versionDim = pafApp.getMdbDef().getVersionDim();
 		List<String> contribPctVersions = pafApp.getContribPctVersions();
 		
 						
 		/*
 		 *  Currently, replication on contribution % members is not supported. 
 		 *  This method is optimized to just check the version axis. This method
 		 *  will need to be modified accordingly if other criteria for invalidating
 		 *  replicated intersections is added.
 		 */
 		
 		for (PafViewSection section:sections) {
 			if( ! section.isEmpty() ) {
 			
 				// Initialization
 				String[] dimensions = section.getDimensionsPriority(); // This is the order that the pace client is expecting
 				ViewTuple[] rowTuples = section.getRowTuples();
 				ViewTuple[] colTuples = section.getColTuples();
 				ArrayList<Intersection>  invalidReplicationIntersections = new ArrayList<Intersection>();
 				
 				// Determine which axis contains the version dimension. Skip section
 				// if version is a page dimension.
 				PafAxis versionAxis = section.getAxis(versionDim);
 				if (versionAxis.getValue() == PafAxis.PAGE) {
 					continue;
 				}
 				
 				// Iterate through each row and column checking for invalid replication
 				// intersections.
 				int versionDimIndex = section.getDimensionIndex(versionDim);
 				for (int rowInx = 0; rowInx < rowTuples.length; rowInx++) {
 					
 					ViewTuple rowTuple = rowTuples[rowInx];
 					for (int colInx = 0; colInx < colTuples.length; colInx ++) {
 						
 						ViewTuple colTuple = colTuples[colInx];
 						
 						// Set search tuple
 						ViewTuple searchTuple = null;
 						if (versionAxis.getValue() == PafAxis.ROW) {
 							searchTuple = rowTuple;						
 						} else {
 							searchTuple = colTuple;
 						}
 						
 						// Check for any contribution percent versions
 						String versionMember = searchTuple.getMemberDefs()[versionDimIndex];
 						if (contribPctVersions.contains(versionMember)) {
 							// Add intersection to invalid replication intersection collection 
 							Intersection viewIs = section.createDataCacheIntersection(rowInx, colInx);
 							invalidReplicationIntersections.add(viewIs);
 						}
 					}
 					
 					
 				}
 				// Set the invalid replication coordinates on this view section
 				SimpleCoordList simpleCoordList = new SimpleCoordList(dimensions);
 				SimpleCoordList[] simpleCoordListArray = new SimpleCoordList[]{simpleCoordList};
 				List<String> coordList = new ArrayList<String>();
 				for (Intersection invalidReplicationIs : invalidReplicationIntersections) {
 					for (String dimension : dimensions) {
 						coordList.add(invalidReplicationIs.getCoordinate(dimension));					
 					}
 				}
 				simpleCoordList.setCoordinates(coordList.toArray(new String[0]));
 				section.setInvalidReplicationIntersections(simpleCoordListArray);
 			}
 		}
 		
 		// Return the updated view sections
 		return sections;
 
 	}
 
 
 	/**
 	 * 
 	 *  Creates an array of Alias Mappings. The view section alias mappings overwrite the global alias mappings.
 	 *  If a dimension doesn't have an alias mapping on either view section or global, a default will 
 	 *  be created.
 	 *
 	 * @param globalAliasMappingByDim A map of global alias mappings from paf apps, by dimension.
 	 * @param viewSections View sections to have alias mappings applied
 	 * 
 	 * @return AliasMapping[]
 	 */
 	private AliasMapping[] configureGlobalAliasMappings(Map<String, AliasMapping> globalAliasMappingByDim, PafViewSection[] viewSections) {
 										
 		//create map to hold order of alias mappings by dim (TTN-1454)
 		Map<String, AliasMapping> aliasMappingByDim = new HashMap<String, AliasMapping>();  
 		
 		//if global alias mappings exists, add to map (TTN-1454)
 		if (globalAliasMappingByDim != null && globalAliasMappingByDim.size() > 0) {  
 			aliasMappingByDim.putAll(globalAliasMappingByDim);  
 		}		
 		
 		//if view section exists
 		if ( viewSections != null && viewSections.length > 0 ) {						
 
 			PafViewSection viewSection = viewSections[0];
 				
 			//update any global alias mappings that are explicitly defined in the view section (TTN-1454)
 			if (viewSection.getAliasMappings() != null ) {							
 				for (AliasMapping viewSectionAliasMapping : Arrays.asList(viewSection.getAliasMappings()) ) {
 					String dimName = viewSectionAliasMapping.getDimName(); 
 					aliasMappingByDim.put(dimName, viewSectionAliasMapping);				
 				}
 			}
 			
 			//add any dimension alias mapping that doesn't already exists.  Usually for attributes.
 			for (String dimName : viewSection.getDimensions()) {
 				if (!aliasMappingByDim.containsKey(dimName) ) {  //TTN-1454
 					AliasMapping defaultAliasMapping = AliasMapping.createDefaultAliasMapping(dimName);  //TTN-1454		
 					aliasMappingByDim.put(dimName, defaultAliasMapping);  //TTN-1454
 				}
 			}
 		}
 		
 		//return alias mappings as an array
 		AliasMapping[] aliasMappingAr = null;
 		if (aliasMappingByDim.size() > 0 ) {
 			
 			// convert the set of alias mappings to an array
 			aliasMappingAr = aliasMappingByDim.values().toArray(new AliasMapping[0]);  //TTN-1454
 
 			//validate each alias mapping
 			for (AliasMapping aliasMapping : aliasMappingAr) {				
 				aliasMapping = validateAliasMapping(aliasMapping);			
 			}
 		}
 		
 		// return alias mappings
 		return aliasMappingAr;
 		
 		
 	}
 
 	/**
 	 * 
 	 * Validates an alias mapping.  A valid alias mapping includes:
 	 * 	A alias table name.
 	 *  Primary row column format of { Alias or Member }
 	 *  Additional row column format of { "", Alias or Member }
 	 *
 	 * @param aliasMapping Alias Mapping to validate
 	 * @return corrected alias mapping.
 	 */
 	private AliasMapping validateAliasMapping(AliasMapping aliasMapping) {
 		
 		if ( aliasMapping != null ) {
 		
 			//if alias table is null or empty, set to default
 			if ( aliasMapping.getAliasTableName() == null || aliasMapping.getAliasTableName().trim().length() == 0 ) {
 			
 				aliasMapping.setAliasTableName(PafBaseConstants.ALIAS_TABLE_DEFAULT);
 				
 			}
 
 			//if additional row column format is null or not "", Alias or Member, set to blank			
 			if ( aliasMapping.getAdditionalRowColumnFormat() == null || 
 					(! aliasMapping.getAdditionalRowColumnFormat().equals(AliasMemberDisplayType.Alias.toString()) &&
 					! aliasMapping.getAdditionalRowColumnFormat().equals(AliasMemberDisplayType.Member.toString()) && 
 					! aliasMapping.getAdditionalRowColumnFormat().equals(""))) {
 			
 				aliasMapping.setAdditionalRowColumnFormat("");
 			
 			}	
 			
 			//if primary row column format is null or Alias or Member, set pri to Alias and additoinal to blank
 			if ( aliasMapping.getPrimaryRowColumnFormat() == null || 
 					(! aliasMapping.getPrimaryRowColumnFormat().equals(AliasMemberDisplayType.Alias.toString()) &&
 					 ! aliasMapping.getPrimaryRowColumnFormat().equals(AliasMemberDisplayType.Member.toString()) )) {
 				
 				aliasMapping.setPrimaryRowColumnFormat(AliasMemberDisplayType.Alias.toString());
 				aliasMapping.setAdditionalRowColumnFormat("");
 			}
 			
 		}
 		
 		return aliasMapping;
 	}
 
 	/**
 	 * Applies Hierarchy formatting to all the row and column tuples by
 	 * checking the member of a tuple, getting the dimension and level then
 	 * applying a format mapped to that dimension and level.
 	 * 
 	 * @param viewSections
 	 *            all the paf view sections to have Hierarchy formatting
 	 *            applied to
 	 * 
 	 * @return PafViewSection Array
 	 */
 	private PafViewSection[] applyHierarchyFormatting(PafViewSection[] viewSections) {
 
 		if (viewSections != null) {
 
 			//for each section
 			for (PafViewSection viewSection : viewSections) {
 
 				if( ! viewSection.isEmpty() ) {
 					//get the hierarchy format name from the view section
 					String hierarchyFormatName = viewSection.getHierarchyFormatName();
 	
 					//if the hierarchy format name is not null, apply the hierarchy
 					//formatting to the current view section
 					if (hierarchyFormatName != null) {
 						
 						//if hierarchy format exist in the hier formats cache
 						if (hierarchyFormatsCache
 								.containsKey(hierarchyFormatName)) {
 	
 							//get hierarchy from from cache
 							HierarchyFormat hierFormat = hierarchyFormatsCache
 									.get(hierarchyFormatName);
 							
 							//get hierarchy from the hierarchy format
 							Map<String, Dimension> dimensions = hierFormat
 									.getDimensions();
 							
 							//if dimensions exist, apply hierarchy formatting to col and row dimensions
 							if (dimensions != null) {
 								
 								LevelGenType[] levelGenTypeAr = new LevelGenType [] {LevelGenType.LEVEL, LevelGenType.GEN};  
 								
 								for (LevelGenType levelGenType : levelGenTypeAr) {
 								
 									int axisDimIndex = 0;
 									
 									for (String dim : viewSection.getColAxisDims()) {
 		
 										if (dimensions.containsKey(dim)) {
 		
 											PafDimTree tree = pafDataService
 													.getDimTree(dim);
 		
 											for (ViewTuple viewTuple : viewSection
 													.getColTuples()) {
 		
 												applyHierarchyFormatingToDimension(
 														dimensions.get(dim), viewTuple,
 														axisDimIndex, tree, levelGenType);
 		
 											}
 		
 											// By taking this out, generation formatting
 											// will be applied to all dimensions
 											// break;
 										}
 										axisDimIndex++;
 									}
 		
 									axisDimIndex = 0;
 		
 									for (String dim : viewSection.getRowAxisDims()) {
 		
 										if (dimensions.containsKey(dim)) {
 		
 											PafDimTree tree = pafDataService
 													.getDimTree(dim);
 		
 											for (ViewTuple viewTuple : viewSection
 													.getRowTuples()) {
 		
 												applyHierarchyFormatingToDimension(
 														dimensions.get(dim), viewTuple,
 														axisDimIndex, tree, levelGenType);
 											}
 		
 											//
 											// break;
 										}
 										axisDimIndex++;
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		return viewSections;
 	}
 
 	/**
 	 * 
 	 *	Method_description_goes_here
 	 *
 	 * @param dimension
 	 * @param viewTuple
 	 * @param axisDimIndex
 	 * @param tree
 	 */
 	private void applyHierarchyFormatingToDimension(Dimension dimension,
 			ViewTuple viewTuple, int axisDimIndex, PafDimTree tree, LevelGenType levelGenType) {
 
 		// get member name from view tuple based on axis dimension index
 		String memberName = viewTuple.getMemberDefs()[axisDimIndex];
 
 		// if paf blank or member tag, return
 		if (memberName.equals(PafBaseConstants.PAF_BLANK) || viewTuple.isMemberTag()) {
 			return;
 		}
 
 		// get paf member from paf member tree
 		PafDimMember member = tree.getMember(memberName);
 
 		// get memmber attribute from paf member
 		PafDimMemberProps memberAtt = member.getMemberProps();
 
 		String formatName = null;
 		
 		if ( levelGenType.equals(LevelGenType.LEVEL)) {
 			
 			// get member attribute level
 			int level = memberAtt.getLevelNumber();
 	
 			// get format name from dimension per level
 			formatName = dimension.getLevelFormatName(level);
 			
 			if ( formatName != null ) {
 				logger.debug("Setting Hierarchy Format on Dimension: "
 						+ dimension.getName() + "; Member: " + memberName
 						+ "; Level = " + level + "; Format: " + formatName);
 			}
 			
 		} else if ( levelGenType.equals(LevelGenType.GEN)) {
 			
 			int generation = memberAtt.getGenerationNumber();
 			
 			formatName = dimension.getGenFormatName(generation);			
 
 			if ( formatName != null) {
 				logger.debug("Setting Hierarchy Format on Dimension: "
 						+ dimension.getName() + "; Member: " + memberName
 						+ "; Generation = " + generation + "; Format: " + formatName);
 			}
 		}
 		
 		// if format name is found, set the data global style name to it
 		if (formatName != null) {
 			
 			viewTuple.setDataGlobalStyleName(formatName);
 
 		}
 
 	}
 
 	/**
 	 * Determine which axis the version is on and the version type. Lock all
 	 * intersections that have a version type of NonPlannable and if
 	 * ForwardPlannable, used elapsed year and elapsed time to determine if the
 	 * intersection should be locked.
 	 * 
 	 * 
 	 * @param Complex
 	 *            view section array
 	 * 
 	 * @return Complex view section array
 	 */
 	private PafViewSection[] lockVersionIntersections(PafViewSection[] sections, PafClientState clientState) {
 
 		Set<String> activeVersions = clientState.getActiveVersions();
 		
 		MdbDef mdbDef = pafApp.getMdbDef();
 		String timeDim = mdbDef.getTimeDim();
 		String yearDim = mdbDef.getYearDim();		
 		String baseVersion = clientState.getPlanningVersion().getName();
 		Set<String> lockedYears = clientState.getLockedYears();
 		String[] serverDimensionOrder;
 //		String[] serverDimensionOrder = mdbDef.getAllDims();
 
 
 		for (PafViewSection section : sections) {
 
 			if( ! section.isEmpty() ) {
 				// index counters for both row and column
 				int rowId = 0;
 				
 				serverDimensionOrder = section.getDimensionsPriority();
 	
 				ArrayList<LockedCell> notPlannableList = new ArrayList<LockedCell>();
 				ArrayList<LockedCell> forwardPlannableList = new ArrayList<LockedCell>();
 				
 				Set<Intersection> lockedForwardPlannableIntersectionsSet = new HashSet<Intersection>();
 	
 				// get row, col and page tuples
 				ViewTuple[] rowTuples = section.getRowTuples();
 				ViewTuple[] colTuples = section.getColTuples();
 				PageTuple[] pageTuples = section.getPageTuples();
 	
 				// get axis where version dimension resides
 				PafAxis versionAxis = section.getAxis(mdbDef.getVersionDim());
 	
 				String versionMember = null;
 	
 	
 				// map dimensions to axis. key = dimensions name, value = actual
 				// axis obj
 	//			Map<String, PafAxis> mappedDimensions = mapDimensionsToAxis(section);
 	
 				Map<String, String> mappedDimsWithMembers = new HashMap<String, String>();
 	
 				for (PageTuple pageTuple : section.getPageTuples()) {
 					mappedDimsWithMembers.put(pageTuple.getAxis(), pageTuple
 							.getMember());
 				}
 	
 				// loop over each row tuple, then over col tuple
 				for (ViewTuple rowTuple : rowTuples) {
 					rowId++;
 	
 					// create row members
 					// ArrayList<String> rowMembers = new ArrayList<String>();
 					ArrayList<String> rowMembers = getTupleMemberDefs(rowTuple);
 	
 					int rowMemberIndex = 0;
 	
 					for (String dimension : section.getRowAxisDims()) {
 						mappedDimsWithMembers.put(dimension, rowMembers.get(rowMemberIndex++));
 					}
 	
 					int colId = 0;
 	
 					// loop over all column tuples and then determine if
 					// intersection is locked
 					for (ViewTuple colTuple : colTuples) {
 	
 						colId++;
 	
 						// if row or column are pafblank, process next col
 						if (rowTuple.containsNonMember() || colTuple.containsNonMember()) {
 							continue;
 						}
 	
 						// get version member from col or row
 						switch (versionAxis.getValue()) {
 						case PafAxis.PAGE:
 							
 							// get version member
 							for (PageTuple tuple : pageTuples) {
 								if (tuple.getAxis().equals(mdbDef.getVersionDim())) {
 									versionMember = tuple.getMember();
 									break;
 								}
 							}
 							// if page version is not an active version, lock page tuple
 							if ( ! activeVersions.contains(versionMember)) {
 								
 								switch (section.getPrimaryFormattingAxis()) {
 								case (PafAxis.COL):
 									colTuple.setPlannable(false);
 									break;
 								case (PafAxis.ROW):
 									rowTuple.setPlannable(false);
 								}
 								
 								//add to locked cell
 								notPlannableList.add(new LockedCell(rowId, colId));
 								continue;
 							}					
 							break;
 	
 						case PafAxis.COL:
 	
 							versionMember = getVersionMember(section.getColAxisDims(), colTuple);					
 							if ( ! activeVersions.contains(versionMember)) {				
 								colTuple.setPlannable(false);
 								notPlannableList.add(new LockedCell(rowId, colId));
 								continue;
 							}					
 							break;
 	
 						case PafAxis.ROW:
 	
 							versionMember = getVersionMember(section.getRowAxisDims(), rowTuple);					
 							if ( ! activeVersions.contains(versionMember)) {					
 								rowTuple.setPlannable(false);
 								notPlannableList.add(new LockedCell(rowId, colId));
 								continue;					
 							}				
 							break;
 						}
 	
 						// get version type from cache
 						VersionType versionType = versionsTypeCache.get(versionMember);
 						
 						// if null, loop
 						if (versionType == null) {
 							continue;
 						}
 						
 	
 						ArrayList<String> colMembers = getTupleMemberDefs(colTuple);
 	
 						/*
 						 * if version is not plannable, then add row and column
 						 * indexes to appropriate arrays and add intersection to
 						 * locked intersection set.
 						 */
 	
 						int colMemberIndex = 0;
 	
 						// map dimensions with members
 						for (String dimension : section.getColAxisDims()) {
 							mappedDimsWithMembers.put(dimension, colMembers.get(colMemberIndex++));
 						}
 	
 						// holds the order of the members based on server dimension
 						// order
 						String[] coords = new String[serverDimensionOrder.length];
 	
 						int coordMemberIndex = 0;
 	
 						// populate the members
 						for (String dimension : serverDimensionOrder) {
 							coords[coordMemberIndex++] = mappedDimsWithMembers.get(dimension);
 						}
 	
 						// lock all tuples if the version is non-plannable 
 						if (versionType.equals(VersionType.NonPlannable)) {
 	
 							// add cell to non plannable list
 							notPlannableList.add(new LockedCell(rowId, colId));
 													
 							switch (versionAxis.getValue()) {
 							case PafAxis.PAGE:
 								switch (section.getPrimaryFormattingAxis()) {
 								case (PafAxis.COL):
 									colTuple.setPlannable(false);
 									break;
 								case (PafAxis.ROW):
 									rowTuple.setPlannable(false);
 								}
 								break;
 	
 							case PafAxis.COL:
 	
 								colTuple.setPlannable(false);
 								break;
 	
 							case PafAxis.ROW:
 	
 								rowTuple.setPlannable(false);
 								break;
 	
 							}
 	
 					   /*
 						* if forward plannable, lock each tuple intersection
 						* that contains a locked period, using the locked period
 						* information from the client state (TTN-1595). 
 						* 
 						* Also lock any tuple intersections where the year is locked (TTN-1860).
 						*  
 						*/
 	
 						} else if (versionType.equals(VersionType.Plannable) || versionType.equals(VersionType.ForwardPlannable)) {
 	
 							String timeMember = getMember(timeDim, section, rowTuple, colTuple);
 							String yearMember = getMember(yearDim, section, rowTuple, colTuple);
 							Map<String, Set<String>> lockedPeriodMap = clientState.getLockedPeriodMap();
 							Set<String> lockedPeriods = lockedPeriodMap.get(yearMember);
 							if ((versionType.equals(VersionType.Plannable) && lockedYears.contains(yearMember))	// TTN-1860 non-plannable year support
 									|| lockedPeriods.contains(timeMember)) {
 	
 								forwardPlannableList.add(new LockedCell(rowId, colId));
 	
 								//only add the forward plannable intersections for planning version
 								if ( versionMember.equals(baseVersion)) {
 									lockedForwardPlannableIntersectionsSet.add(new Intersection(serverDimensionOrder, coords));
 								}
 	
 	//						The following tuple locking code has been commented out as it erroneously locking cells on
 	//							multi-year views. Based on initial testing, it doesn't look like this code was needed (TTN-1860).
 	//							
 	//							// lock the tuple based on time axis. if time is
 	//							// on page, use primary formatting axis, if on
 	//							// col axis, lock column tuple. if on row axis, 
 	//							// lock row tuple.
 	//							PafAxis timeAxis = section.getAxis(mdbDef.getTimeDim());
 	//							switch (timeAxis.getValue()) {
 	//							case PafAxis.PAGE:
 	//
 	//								switch (section.getPrimaryFormattingAxis()) {
 	//								case (PafAxis.COL):
 	//									colTuple.setPlannable(false);
 	//								break;
 	//								case (PafAxis.ROW):
 	//									rowTuple.setPlannable(false);
 	//								break;
 	//								}
 	//
 	//							case PafAxis.COL:
 	//
 	//								colTuple.setPlannable(false);
 	//								break;
 	//
 	//							case PafAxis.ROW:
 	//
 	//								rowTuple.setPlannable(false);
 	//								break;
 	//
 	//							}
 							}
 						}
 					}
 				}
 	
 	
 	
 				clientState.addLockedForwardPlannableInterMap(section.getName(), lockedForwardPlannableIntersectionsSet);
 	
 				section.setNotPlannableLockedCells(notPlannableList
 						.toArray(new LockedCell[0]));
 				section.setForwardPlannableLockedCell(forwardPlannableList
 						.toArray(new LockedCell[0]));
 			}
 
 		}
 
 		return sections;
 	}
 
 /**
  *  
  *  Lock non-plannable view section measure intersections
  * 
  * @param sections View section array
  * @param clientState 
  * @return Complex view section array
 	 */
 	
 	private PafViewSection[] lockMeasureIntersections(PafViewSection[] sections, PafClientState clientState) {
 
 		//get mdbdef from paf app
 		MdbDef mdbDef = pafApp.getMdbDef();
 
 		// get all dimensions in order
 		String[] serverDimensionOrder;
 
 		// for each section, see if measure is not plannable and if not, lock
 		for (PafViewSection section : sections) {
 			
 			if( ! section.isEmpty() ) {
 				//get all dimensions in order
 				serverDimensionOrder = section.getDimensionsPriority();
 	
 				// get measures axis
 				PafAxis measureAxis = section.getAxis(mdbDef.getMeasureDim());
 	
 				// index counters for both row and column
 				int rowId = 0;
 	
 				//get current not plannable list
 				ArrayList<LockedCell> notPlannableList = new ArrayList<LockedCell>();
 				if (section.getNotPlannableLockedCells() != null)
 					notPlannableList.addAll(Arrays.asList(section.getNotPlannableLockedCells()));
 	
 				// get row tuples
 				ViewTuple[] rowTuples = section.getRowTuples();
 	
 				// get column tuples
 				ViewTuple[] colTuples = section.getColTuples();
 	
 				// used to map a dimension to a member
 				Map<String, String> mappedDimsWithMembers = new HashMap<String, String>();
 	
 				// map page tuple members to dimensions
 				for (PageTuple pageTuple : section.getPageTuples()) {
 					mappedDimsWithMembers.put(pageTuple.getAxis(), pageTuple
 							.getMember());
 				}
 	
 				// loop over each row tuple, then over col tuple
 				for (ViewTuple rowTuple : rowTuples) {
 					rowId++;
 	
 					// create row members
 					ArrayList<String> rowMembers = getTupleMemberDefs(rowTuple);
 	
 					int rowMemberIndex = 0;
 	
 					// populate dimension map with dimension and row members
 					for (String dimension : section.getRowAxisDims()) {
 						mappedDimsWithMembers.put(dimension, rowMembers
 								.get(rowMemberIndex++));
 					}
 	
 					int colId = 0;
 	
 					// loop over all column tuples and then determine if
 					// intersection is locked
 					for (ViewTuple colTuple : colTuples) {
 	
 						colId++;
 	
 						// TODO: maybe remove this
 						// if row or column are pafblank or member tag, process next col
 						if (rowTuple.containsNonMember() || colTuple.containsNonMember()) {
 							continue;
 						}
 	
 						// get column members in a list
 						ArrayList<String> colMembers = getTupleMemberDefs(colTuple);
 	
 						int colMemberIndex = 0;
 	
 						for (String dimension : section.getColAxisDims()) {
 							mappedDimsWithMembers.put(dimension, colMembers
 									.get(colMemberIndex++));
 						}
 	
 						// get measure member
 						String measureMember = mappedDimsWithMembers.get(pafApp
 								.getMdbDef().getMeasureDim());
 	
 						// populate member order
 						String[] coordMemberOrder = new String[serverDimensionOrder.length];
 						int coordMemberIndex = 0;
 						for (String dimension : serverDimensionOrder) {
 							coordMemberOrder[coordMemberIndex++] = mappedDimsWithMembers.get(dimension);
 						}
 	
 						// by default, measure is plannable
 						boolean measurePlannable = true;
 						
 						//TTN-1413: Read Only Measures
 						//if client state contains read only measure, set plannable to false
 						if ( measureMember != null && clientState.getReadOnlyMeasures().contains(measureMember) ) {
 							measurePlannable = false;
 						} else if (measureMember != null && measuresPlannableCache.containsKey(measureMember)) {
 							measurePlannable = measuresPlannableCache.get(measureMember);
 						} 
 						
 						// if measure is not plannable lock cell and intersection
 						if ( !measurePlannable ) {
 	
 							// add to not plannable list
 							notPlannableList.add(new LockedCell(rowId, colId));
 							
 							switch (measureAxis.getValue()) {
 							case PafAxis.PAGE:
 	
 								switch (section.getPrimaryFormattingAxis()) {
 								case (PafAxis.COL):
 									colTuple.setPlannable(false);
 									break;
 								case (PafAxis.ROW):
 									rowTuple.setPlannable(false);
 								}
 	
 								break;
 	
 							case PafAxis.COL:
 	
 								colTuple.setPlannable(false);
 								break;
 	
 							case PafAxis.ROW:
 	
 								rowTuple.setPlannable(false);
 								break;
 	
 							}
 						}
 					}
 	
 				}
 				
 	
 				// remove any non-plannable cells from the locked forward plannable cells collection
 				LockedCell[] lockedForwardPlannableCells = section.getForwardPlannableLockedCell();
 				if ( lockedForwardPlannableCells != null ) {
 	
 					//create a temp locked forward plannable cell set to hold all the locked cells
 					Set<LockedCell> lockedForwardPlannableCellSet = new TreeSet<LockedCell>();
 					
 					//create a temp set, so searching is faster.
 					Set<LockedCell> tempNonPlannable = new HashSet<LockedCell>();
 					tempNonPlannable.addAll(notPlannableList);
 					
 					//populate set with forward locked cells
 					for (LockedCell lockedCell : lockedForwardPlannableCells) {
 						//don't add non plannable locked cells
 						if(! tempNonPlannable.contains(lockedCell)) {
 							lockedForwardPlannableCellSet.add(lockedCell);
 						}
 					}
 							
 					//set the fp lc on the view section
 					section.setForwardPlannableLockedCell(lockedForwardPlannableCellSet.toArray(new LockedCell[0]));
 					
 				}	
 				
 				
 				
 				
 				// create unique locked cells array from non plannable list
 				section.setNotPlannableLockedCells(notPlannableList.toArray(new LockedCell[0]));
 			}
 		}
 
 		return sections;
 	}
 	
 	
 	/**
 	 *  Checks the tuples to see if they are not plannable.  If not plannable, then
 	 *  a locked cell is added to the non plannable locked cell list and added to the 
 	 *  array of the view section.
 	 *  
 	 * @param sections Complex view section array
 	 * @param explodedSessionLocks Set of exploded session lock coordinates
 	 * @param dataCache Data cache
 	 * 
 	 * @return Complex view section array
 	 */
 	private PafViewSection[] addNonPlannableTuplesToClientState(PafViewSection[] sections, Set<Coordinates> explodedSessionLocks, PafDataCache dataCache) {
 	
 		String[] baseDims = dataCache.getBaseDimensions();
 		
 		// for each section 
 		for (PafViewSection section : sections) {
 			
 			if( ! section.isEmpty() ) {
 				// index counters for both row and column
 				int rowId = 0;
 	
 				// track each cell's dimension member coordinates (TTN-1893)
 				String[] viewDims = section.getDimensionsPriority();
 				String[] coords = new String[viewDims.length];
 				Map<String, String> mappedDimsWithMembers = new HashMap<String, String>();
 				 
 				// initialize session lock tracking variables (TTN-1893)
 				Set<LockedCell> fpLockedCellSet = new HashSet<LockedCell>();
 				Set<LockedCell> invalidAttrCellSet = new HashSet<LockedCell>();
 				List<LockedCell> sessionLockedCellList = new ArrayList<LockedCell>();
 				if (section.hasAttributes()) {		     
 				    // convert locked cells arrays to sets for fast lookup 
 				    if (section.getForwardPlannableLockedCell() != null) 
 				    	fpLockedCellSet.addAll(Arrays.asList(section.getForwardPlannableLockedCell()));
 				    if (section.getInvalidAttrIntersectionsLC() != null) 
 				    	invalidAttrCellSet.addAll(Arrays.asList(section.getInvalidAttrIntersectionsLC()));				    
 				}
 				
 				//get current not plannable list
 				ArrayList<LockedCell> notPlannableList = new ArrayList<LockedCell>();
 				if (section.getNotPlannableLockedCells() != null)
 					notPlannableList.addAll(Arrays.asList(section.getNotPlannableLockedCells()));
 				
 				// get page dimension coordinates (TTN-1893)
 				for (PageTuple pageTuple: section.getPageTuples()){
 					mappedDimsWithMembers.put(pageTuple.getAxis(), pageTuple.getMember());
 				}
 							
 
 				// get row tuples
 				ViewTuple[] rowTuples = section.getRowTuples();
 
 				// get column tuples
 				ViewTuple[] colTuples = section.getColTuples();
 
 				// loop over each row tuple, then over col tuple
 				for (ViewTuple rowTuple : rowTuples) {
 					rowId++;
 
 					// get row dimension coordinates (TTN-1893)
 					List<String> rowMembers = getTupleMemberDefs(rowTuple);					
 					int rowMemberIndex = 0;
 					for (String dimension : section.getRowAxisDims()){
 						mappedDimsWithMembers.put(dimension, rowMembers.get(rowMemberIndex++));
 					}
 
 					// loop over all column tuples and then determine if
 					// intersection is locked
 					int colId = 0;
 					for (ViewTuple colTuple : colTuples) {
 
 						colId++;
 
 						// get column dimension coordinates (TTN-1893)
 						List<String> colMembers = getTupleMemberDefs(colTuple);					
 						int colMemberIndex = 0;
 						for (String dimension : section.getColAxisDims()){
 							mappedDimsWithMembers.put(dimension, colMembers.get(colMemberIndex++));
 						}
 
 						//if the row or column tuple contain blank or member tag, continue to next tuple
 						if (rowTuple.containsNonMember() || colTuple.containsNonMember()) {
 							continue;
 						}
 
 						//if row is not plannable, or if col is not plannable, add to not plannable list
 						if ( (rowTuple.getPlannable() != null && ! rowTuple.getPlannable()) || (colTuple.getPlannable() != null && ! colTuple.getPlannable() )) {
 							notPlannableList.add(new LockedCell(rowId, colId));	
 							continue;
 						}				
 
 						// apply session lock, if attribute view (TTN-1893)
 						if (section.hasAttributes() && !explodedSessionLocks.isEmpty()) { 
 
 							
 							// don't attempt to apply session lock if the cell is invalid or  previously locked or protected
 							LockedCell currLockedCell = new LockedCell(rowId, colId);
 							if (invalidAttrCellSet.contains(currLockedCell) || notPlannableList.contains(currLockedCell) 
 									|| fpLockedCellSet.contains(currLockedCell)) {
 								continue;
 							}
 
 							// get coordinates of current view cell
 							int coordMemberIndex = 0;
 							for (String dimension : section.getDimensionsPriority()){
 								coords[coordMemberIndex++] = mappedDimsWithMembers.get(dimension);
 							}
 							
 							// strip off attribute components
 							String[] baseCoords = new String[baseDims.length];
 							System.arraycopy(coords, 0, baseCoords, 0, baseDims.length);
 							Coordinates baseCoordinates = new Coordinates(baseCoords);						
 													
 							// is current cell a session lock cell?
 							if (explodedSessionLocks.contains(baseCoordinates)) {
 								sessionLockedCellList.add(currLockedCell);
 							}
 						}
 
 					}
 				}
 
 				// set on view section by creating a unique locked cell array
 				section.setNotPlannableLockedCells(notPlannableList.toArray(new LockedCell[0]));
 
 			    // set view section session lock property (TTN-1893)
 				section.setSessionLockedCells(sessionLockedCellList.toArray(new LockedCell[0]));
 			}
 		}
 
 		return sections;
 
 	}
 
 	
 	/**
 	 *  Populate member tag data intersections on specified axis
 	 *
 	 * @param viewSection View section
 	 * @param mtAxis - Axis (row or col) in which member tags will be populated
 	 * 
 	 * @return PafViewSection
 	 * @throws PafException 
 	 */
 	private PafViewSection populateMemberTagIntersections(PafViewSection viewSection, PafAxis mtAxis, Map<SimpleMemberTagId,Map<Intersection, MemberTagData>> memberTags) throws PafException {
 	
 		// Initialization
 		PafMemberTagManager mtManager = PafMemberTagManager.getInstance();
 		String [] mtCoordinateDims = null;
 		ViewTuple[] mtCandidateTuples = null, mtCoordinateTuples = null;
 		List<MemberTagViewSectionData> mtViewSectionDataList = new ArrayList<MemberTagViewSectionData>();
 		MemberTagViewSectionData[] mtViewSectionDataArray = new MemberTagViewSectionData[0];
 		Session session = null;
 		
 		// Look for member tag coordinates on opposing view axis
 		if (mtAxis.getValue() == PafAxis.COL) {
 			mtCandidateTuples = viewSection.getColTuples();
 			mtCoordinateTuples = viewSection.getRowTuples();
 			mtCoordinateDims = viewSection.getRowAxisDims();
 			viewSection.setColMemberTagData(mtViewSectionDataArray);
 		} else if (mtAxis.getValue() == PafAxis.ROW) {
 			mtCandidateTuples = viewSection.getRowTuples();			
 			mtCoordinateTuples = viewSection.getColTuples();
 			mtCoordinateDims = viewSection.getColAxisDims();
 			viewSection.setRowMemberTagData(mtViewSectionDataArray);
 		} else {
 			String errMsg = "Invalid axis type of: [" + mtAxis.getValue() + "] passed to populateMemberTagIntersections";
 			logger.equals(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 		
 		// Look for any tuple that contains a member tag reference and populate it with the
 		// corresponding member tag data
 		for (ViewTuple mtTuple:mtCandidateTuples) {
 			
 			// Skip any tuples that aren't a member tag
 			if (!mtTuple.isMemberTag()) {
 				continue;
 			}
 			
 			// Each member of a member tag tuple contains the member tag name
 			String memberTagName = mtTuple.getMemberDefs()[0];
 			
 			// Validate member tag
 			if (!isValidMemberTagAxis(memberTagName, viewSection, mtAxis)) {
 				String errMsg = "Invalid member tag reference: [" + memberTagName + "] found on view section: [" + viewSection.getName() + "]";
 				throw new IllegalArgumentException(errMsg);				
 			}
 			
 			// Get initial member tag intersection
 			Intersection memberTagIS = initMemberTagIntersection(memberTagName, viewSection);
 					
 			SimpleCoordList mtCoordinates = null;
 			List<String> coordList = null;
 			MemberTagData[] mtDataArray = null;
 			try {
 				// Open a new hibernate session
 				session = PafMemberTagManager.getSession();
 
 				// Iterate through coordinate tuples to determine the list of member tag coordinates
 				MemberTagDef mtDef = pafApp.getMemberTagDef(memberTagName);
 				mtCoordinates = new SimpleCoordList();
 				List<String> mtDimList = Arrays.asList(mtDef.getDims());		
 				mtCoordinates.setAxis(mtDimList.toArray(new String[0]));
 				coordList = new ArrayList<String>();
 				for (ViewTuple coordTuple:mtCoordinateTuples) {
 					
 					// Skip any member tag tuples
 					if (coordTuple.isMemberTag()) {
 						continue;
 					}
 					
 					// Get coordinates of selected member tag intersection
 					memberTagIS = getMemberTagIntersection(memberTagIS, coordTuple, mtCoordinateDims);
 					
 					// Add coordinates to coordinate list in member tag dimension order
 					for (String coordinate:memberTagIS.getCoordinates()) {
 						coordList.add(coordinate);
 					}
 					
 				}
 
 				// Retrieve member tag data at all specified coordinates
 				mtCoordinates.setCoordinates(coordList.toArray(new String[0]));
 				mtDataArray = mtManager.getMemberTagData(pafApp.getAppId(), memberTagName, mtCoordinates, session, memberTags);
 
 			} finally {
 				// Terminate hibernate session
 				PafMemberTagManager.terminateSession(session);
 			}
 			
 			// Add member tag data to view section member tag data collection
 			List<String> mtValuesList = new ArrayList<String>();
 			for (MemberTagData mtData:mtDataArray) {
 				String mtValue = "";
 				if (mtData != null) {
 					mtValue = mtData.getData();
 				}
 				mtValuesList.add(mtValue);				
 			}
 			MemberTagViewSectionData mtViewSectionData = new MemberTagViewSectionData();
 			mtViewSectionData.setMemberTagName(memberTagName);
 			mtViewSectionData.setMemberTagValues(mtValuesList.toArray(new String[0]));
 			mtViewSectionDataList.add(mtViewSectionData);
 		}
 		
 		// Update view section member tag property for appropriate axis
 		if (mtAxis.getValue() == PafAxis.COL) {
 			viewSection.setColMemberTagData(mtViewSectionDataList.toArray(new MemberTagViewSectionData[0]));
 		} else {
 			viewSection.setRowMemberTagData(mtViewSectionDataList.toArray(new MemberTagViewSectionData[0]));
 		}
 		
 		// Return updated view section
 		return viewSection;
 	}
 
 	/**
 	 * Populate member tag comments. Member tag comments are read-only fields
 	 * tied to a column or row header, and show up in the client as a cell comment.
 	 *
 	 * @param viewSection View section
 	 * 
 	 * @return PafViewSection
 	 * @throws PafException 
 	 */
 	private PafViewSection populateMemberTagComments(PafViewSection viewSection, Map<SimpleMemberTagId,Map<Intersection, MemberTagData>> memberTags) throws PafException {
 		
 		// Initialization
 		PafMemberTagManager mtManager = PafMemberTagManager.getInstance();
 		MemberTagCommentEntry[] mtCommentEntries = viewSection.getMemberTagCommentEntries();
 		List<MemberTagCommentPosition> rowCommentPositions = new ArrayList<MemberTagCommentPosition>();
 		List<MemberTagCommentPosition> colCommentPositions = new ArrayList<MemberTagCommentPosition>();
 		Session session = null;
 		
 		// Exit, if no member tag comments on view
 		if (mtCommentEntries == null || mtCommentEntries.length == 0) {
 			return viewSection;
 		}
 			
 		// Process each member tag comment
 		for (MemberTagCommentEntry mtCommentEntry:mtCommentEntries) {
 
 			SimpleCoordList coordList = new SimpleCoordList();
 			ViewTuple[] commentTuples = null;
 			String[] tupleDims = null;
 			String commentDim = null;
 			String mtCommentName = mtCommentEntry.getName();
 			List<MemberTagCommentPosition> commentPositions = null;
 			PafAxis axis = getMemberTagAxis(mtCommentName, viewSection);
 
 			// Determine which non-page tuples (row/col) contain the coordinates
 			// of the member tag comment
 			if (axis.getValue() == PafAxis.ROW) {
 				commentTuples = viewSection.getColTuples();
 				tupleDims = viewSection.getColAxisDims();
 				commentPositions = colCommentPositions;
 			} else {
 				commentTuples = viewSection.getRowTuples();
 				tupleDims = viewSection.getRowAxisDims();
 				commentPositions = rowCommentPositions;
 			}
 			
 			// Determine which tuple dimension to place the member tag comment on. It 
 			// should be placed on the most inner tuple dimension that's part of the 
 			// member tag definition.
 			MemberTagDef mtDef = pafApp.getMemberTagDef(mtCommentName);
 			List<String> mtDimList = new ArrayList<String>(Arrays.asList(mtDef.getDims()));	
 			for (int i =  tupleDims.length - 1; i >= 0; i--) {
 				String dim = tupleDims[i];
 				if (mtDimList.contains(dim)) {
 					commentDim = dim;
 					break;
 				}
 			}
 			if (commentDim == null) {
 				String errMsg = "Unable to find the innermost tuple dimension for member tag comment: [";
 				errMsg = errMsg + mtCommentName + "]";
 				logger.error(errMsg);
 				throw new IllegalArgumentException(errMsg);
 			}
 			
 			// Record member tag comment position
 			MemberTagCommentPosition commentPosition = new MemberTagCommentPosition(mtCommentName, commentDim);
 			commentPositions.add(commentPosition);
 			
 			// Get initial member tag intersection
 			Intersection memberTagIS = initMemberTagIntersection(mtCommentName, viewSection);
 			
 			try {				
 				// Open a new hibernate session
 				session = PafMemberTagManager.getSession();
 				
 				//get cached member tag map or load from db
 				Map<Intersection, MemberTagData> memberTagMap = null;
 				SimpleMemberTagId simpleMTId = new SimpleMemberTagId(pafApp.getAppId(), mtCommentName);
 				if (memberTags.containsKey(simpleMTId)){
 					memberTagMap = memberTags.get(simpleMTId);
 				}else{
 					//Get all the memberTagData data objects for the member tag.
 					memberTagMap = mtManager.getMemberTagMap(pafApp.getAppId(), mtCommentName, session);
 				}
 				
 				// Populate member tag comment value in each coordinate tuple of the 
 				// appropriate view axis (row or column).
 				coordList.setAxis(mtDef.getDims());
 				List<String> mtCommentVals = new ArrayList<String>();
 				for (ViewTuple commentTuple:commentTuples) {
 
 					// Get any existing member tag comments on view tuple
 					List<String>  commentNames = commentTuple.getMemberTagCommentNames();
 					List<String>  commentValues = commentTuple.getMemberTagCommentValues();
 					if (commentNames == null) {
 						commentNames = new ArrayList<String>();
 						commentTuple.setMemberTagCommentNames(commentNames);
 						commentValues = new ArrayList<String>();
 						commentTuple.setMemberTagCommentValues(commentValues);
 					}
 
 					// Get member tag comment value
 					memberTagIS = getMemberTagIntersection(memberTagIS, commentTuple, tupleDims);
 					coordList.setCoordinates(memberTagIS.getCoordinates());
 
 					//Get an array of memberTagData data objects found in the memberTagMap for the coordinate List
 					MemberTagData[] mtData = mtManager.getDataFromMemberTagMap(memberTagMap, coordList, false);
 
 					// Add comment name and value to tuple
 					if (mtData[0] != null) {
 						String mtCommentValue = mtData[0].getData();
 						commentNames.add(mtCommentName);
 						commentValues.add(mtCommentValue);
 					}	
 					
 					//builds list of member tag comments
 					for(String mtValue : commentValues){
 						mtCommentVals.add(mtValue);
 					}
 				}
 				
 				//get dynamic element escape sequence
 				Integer elementEscapeCount = CompressionUtil.generateEscapeSequenceForArray(mtCommentVals.toArray(new String[0]), PafBaseConstants.DELIMETER_ELEMENT, PafBaseConstants.ESCAPE_CHARACTER_ELEMENT);
 				int counter = 0;
 				String elementEscapeSequence = "";
 				if(elementEscapeCount != null){
 					while (counter <= elementEscapeCount){
 						elementEscapeSequence = elementEscapeSequence + PafBaseConstants.ESCAPE_CHARACTER_ELEMENT;
 						counter++;
 					}
 				}
 				
 				//get dynamic group escape sequence
 				Integer groupEscapeCount = CompressionUtil.generateEscapeSequenceForArray(mtCommentVals.toArray(new String[0]), PafBaseConstants.DELIMETER_GROUP, PafBaseConstants.ESCAPE_CHARACTER_GROUP);
 				counter = 0;
 				String groupEscapeSequence = "";
 				if(groupEscapeCount != null){
 					while (counter <= groupEscapeCount){
 						groupEscapeSequence = groupEscapeSequence + PafBaseConstants.ESCAPE_CHARACTER_GROUP;
 						counter++;
 					}
 				}
 				
 				//update escape sequences for each tuple with a member tag comment
 				for (ViewTuple commentTuple:commentTuples) {
 					commentTuple.setElementDelimiter(elementEscapeSequence + PafBaseConstants.DELIMETER_ELEMENT);
 					commentTuple.setGroupDelimiter(groupEscapeSequence + PafBaseConstants.DELIMETER_GROUP);
 				}
 				
 			} finally {
 				// Terminate hibernate session
 				PafMemberTagManager.terminateSession(session);
 			}
 		}
 
 		// Update member tag comment position fields
 		viewSection.setRowMemberTagCommentPositions(rowCommentPositions.toArray(new MemberTagCommentPosition[0]));
 		viewSection.setColMemberTagCommentPositions(colCommentPositions.toArray(new MemberTagCommentPosition[0]));
 
 		// Return updated view section
 		return viewSection;
 	}
 
 	/**
 	 *	Returns the view axis that the specified member tag is valid on in the specified view section
 	 *
 	 * @param memberTagName Member tag name
 	 * @param viewSection View section
 	 * 
 	 * @return PafAxis 
 	 */
 	private PafAxis findValidMemberTagAxis(String memberTagName, PafViewSection viewSection) {
 		
 		/*
 		 * A view axis (row or column) is valid if one of the following conditions is true:
 		 * 
 		 * 1) All member tag dimensions are contained in the opposite axis.
 		 * 2) The opposite axis contains at least one member tag dimension and the remaining 
 		 * 	  dimensions are page dimensions.
 		 * 
 		 */
 
 		
 		PafAxis[] viewAxes = new PafAxis[] {new PafAxis(PafAxis.COL), new PafAxis(PafAxis.ROW)};
 		List<String> pageDims = Arrays.asList(viewSection.getPageAxisDims());
 		
 		// Get member tag dimensions
 		MemberTagDef memberTagDef = pafApp.getMemberTagDef(memberTagName);
 		List<String> memberTagDims = Arrays.asList(memberTagDef.getDims());
 		
 		// Cycle through each view axis to find the one that is valid. 
 		for (PafAxis axis:viewAxes) {
 			
 			// Get the list of dimensions on the opposite axis
 			List<String> axisDims = null;
 			if (axis.getValue() == PafAxis.ROW) {
 				axisDims = Arrays.asList(viewSection.getColAxisDims());
 			} else {
 				axisDims = Arrays.asList(viewSection.getRowAxisDims());
 			}
 			
 			// Check for all dimensions contained on opposite axis
 			List<String> mtDimsInAxis = new ArrayList<String>(memberTagDims);
 			mtDimsInAxis.retainAll(axisDims);
 			if (memberTagDims.size() == mtDimsInAxis.size()) {
 				return axis;
 			}
 			
 			// Check for all member tag dimensions contained in a combination of axis and page dimensions
 			if (mtDimsInAxis.size() > 0) {
 				List<String> mtDimsInPage = new ArrayList<String>(memberTagDims);
 				mtDimsInPage.retainAll(pageDims);
 				if (memberTagDims.size() == mtDimsInAxis.size() + mtDimsInPage.size()) {
 					return axis;
 				}				
 			}
 			
 		}
 	
 		// Neither axis was valid - return null
 		return null;
 		
 //		// Neither axis was valid - throw exception
 //		String errMsg = "Invalid member tag / member tag comment reference: [" + memberTagName + "] found on view section: [" + viewSection.getName() + "]";
 //		logger.warn(errMsg);
 //		throw new IllegalArgumentException(errMsg);
 		
 	}
 
 	/**
 	 *  Get the axis of the specified member tag on the specified view section
 	 *
 	 * @param memberTagName Member tag name
 	 * @param viewSection View section
 	 * 
 	 * @return true if member tag is valid
 	 */
 	private PafAxis getMemberTagAxis(String memberTagName, PafViewSection viewSection) {
 
 		Map<String, PafAxis> validMemberTags = getValidMemberTagMap().get(viewSection.getName());
 
 		// Check if member tag is valid
 		if (!validMemberTags.containsKey(memberTagName)) {
 			String errMsg = "Member tag: [" + memberTagName + "] on view section: [" + viewSection.getName() + "] ";
 			errMsg = errMsg + " is not valid";
 			logger.error(errMsg);
 			throw new IllegalArgumentException(errMsg);
 		}
 
 		// Return axis
 		PafAxis axis = validMemberTags.get(memberTagName);
 		return axis;
 
 
 	}
 
 	/**
 	 *  Check if the specified member tag if valid on the specified view axis 
 	 *
 	 * @param memberTagName Member tag name
 	 * @param viewSection View section
 	 * @param axis View axis 
 	 * 
 	 * @return true if member tag is valid on specified axis
 	 */
 	private boolean isValidMemberTagAxis(String memberTagName, PafViewSection viewSection, PafAxis axis) {
 
 		PafAxis validAxis = getMemberTagAxis(memberTagName, viewSection);
 		if (validAxis.equals(axis)) {
 			return true;
 		} else {
 			return false;
 		}
 		
 		
 //		String errMsg = "Member tag: [" + memberTagName + "] on view section: [" + viewSection.getName() + "] ";
 //		errMsg = errMsg + " is not valid on the specified axis";
 //		logger.error(errMsg);
 //		throw new IllegalArgumentException(errMsg);
 
 			
 	}
 
 	/**
 	 *	Creates an initial member tag intersection and populated any related page coordinates
 	 *
 	 * @param memberTagName Member tag name
 	 * @param viewSection View section
 	 * 
 	 * @return Intersection
 	 */
 	private Intersection initMemberTagIntersection(String memberTagName, PafViewSection viewSection) {
 		
 		Intersection memberTagIS = null;
 		MemberTagDef mtDef = pafApp.getMemberTagDef(memberTagName);
 		List<String> mtDimList = new ArrayList<String>(Arrays.asList(mtDef.getDims()));		
 
 		
 		// Create new member tag intersection
 		memberTagIS = new Intersection(mtDimList.toArray(new String[0]));
 
 		// Set the coordinates of any page dimensions that are part of the member tag definition.
 		for (PageTuple pageTuple:viewSection.getPageTuples()) {
 			String pageDim = pageTuple.getAxis();
 			if (mtDimList.contains(pageDim)) {
 				memberTagIS.setCoordinate(pageDim, pageTuple.getMember());
 			}
 		}
 		
 		// Return intersection 
 		return memberTagIS;
 	}
 
 	/**
 	 *  Get fully specified member tag intersection
 	 *
 	 * @param partialMemberTagIS Partially populated member tag intersection
 	 * @param coordTuple View tuple containing row/col member tag coordinates
 	 * @param coordDims View tuple containing row/col member tag coordinates
 	 * 
 	 * @return Intersection
 	 */
 	private Intersection getMemberTagIntersection(Intersection partialMemberTagIS, ViewTuple coordTuple, String coordDims[]) {
 		
 		Intersection memberTagIS = partialMemberTagIS.clone();
 		List<String> mtDimList = Arrays.asList(partialMemberTagIS.getDimensions());
 		
 		// Get coordinates of selected member tag intersection
 		for (int i = 0; i < coordDims.length; i++) {
 			String coordDim = coordDims[i];
 			if (mtDimList.contains(coordDim)) {
 				memberTagIS.setCoordinate(coordDim, coordTuple.getMemberDefs()[i] );
 			}
 		}
 
 		// Return coordinates
 		return memberTagIS;
 	}
 
 	/**
 	 *  Resolve dynamic tokens contained in page headers
 	 *
 	 * @param viewSection View section
 	 * @param planVersion Plan version
 	 * @param userSelections User selections
 	 * @param tokenCatalog Resolved view and application tokens
 	 * 
 	 * @return PafViewSection
 	 * @throws PafException 
 	 */
 	private PafViewSection resolvePageHeaders(PafViewSection viewSection, String planVersion, PafUserSelection[] userSelections, Properties tokenCatalog, String viewName) throws PafException {
 
 		final String TOKEN_START = PafBaseConstants.HEADER_TOKEN_START_CHAR;
 		final String TOKEN_PARM_START = PafBaseConstants.HEADER_TOKEN_PARM_START_CHAR;
 		final String TOKEN_PARM_END = PafBaseConstants.HEADER_TOKEN_PARM_END_CHAR;
         final String MEMBER_TAG_TOKEN = PafBaseConstants.HEADER_TOKEN_MEMBER_TAG;
         final String PLAN_VERSION_TOKEN = PafBaseConstants.HEADER_TOKEN_PLAN_VERSION;
         final String USER_SEL_TOKEN = PafBaseConstants.HEADER_TOKEN_USER_SEL;
         final String ROLE_FILTER_SEL_TOKEN = PafBaseConstants.HEADER_TOKEN_ROLE_FILTER_SEL;
         final String PLAN_YEARS_TOKEN = PafBaseConstants.VIEW_TOKEN_PLAN_YEARS;
         final String NONPLAN_YEARS_TOKEN = PafBaseConstants.VIEW_TOKEN_NONPLAN_YEARS;
         final String FIRST_PLAN_YEAR_TOKEN = PafBaseConstants.VIEW_TOKEN_FIRST_PLAN_YEAR;
 		final String FIRST_NONPLAN_YEAR_TOKEN = PafBaseConstants.VIEW_TOKEN_FIRST_NONPLAN_YEAR;
 		final String FIRST_PLAN_PERIOD_TOKEN = PafBaseConstants.VIEW_TOKEN_FIRST_PLAN_PERIOD;
         final String VIEW_NAME_TOKEN = PafBaseConstants.HEADER_TOKEN_VIEW_NAME;
         PafViewHeader[] viewHeaders = viewSection.getPafViewHeaders();
         AliasMapping[] aliasMappings = viewSection.getAliasMappings();
         Map<String, AliasMapping> aliasMappingByDim = new HashMap<String, AliasMapping>();
    		Map<String, PafDimTree> dimTreeMap = pafDataService.getAllDimTrees();
 		Map<String, MemberTagDef> memberTagDefs = pafApp.getMemberTagDefs();
 		String appName = pafApp.getAppId();
 		String token = null;
         String[] pageDims = viewSection.getPageAxisDims();
         String[] pageMembers = viewSection.getPageAxisMembers();
         Session session = null;
          
 
         // Put alias mappings in map for easier lookup
         for (AliasMapping aliasMapping:aliasMappings) {
         	aliasMappingByDim.put(aliasMapping.getDimName(), aliasMapping);
         }
         
         
         // Resolve tokens in each page header using a case-insensitive search. Multiple
         // occurences of the same token in a single page header row is supported.
         for (PafViewHeader viewHeader:viewHeaders) {
         	
         	// Skip header if no tokens are found
        		String headerText = viewHeader.getLabel();
        		if (headerText.indexOf(TOKEN_START) == -1) {
        			continue;
        		}
        			
        		// Resolve @PLAN_VERSION references, where @PLAN_VERSION is a reference to 
        		// the active planning version. 
      		token = PLAN_VERSION_TOKEN;
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 				
 				// Get token value (active planning version)
 				String versionDim = pafApp.getMdbDef().getVersionDim();
 				PafDimMember dimMember = dimTreeMap.get(versionDim).getMember(planVersion);
 				String aliasTable = getAliasTableName(aliasMappingByDim, versionDim);
 				String tokenValue = dimMember.getMemberProps().getMemberAlias(aliasTable);
 				
 				// Replace any occurences of token with page member alias 
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);
 			}   
 			
       		// Resolve @VIEW_NAME references, where @VIEW_NAME is a reference to viewname in ViewSection header
      		token = VIEW_NAME_TOKEN;
 			// Resolve token if used in header
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 				// Get token value from token catalog. If not found then replace token with a blank.
 				String tokenValue = viewName;
 				if (tokenValue == null) {
 					tokenValue = "";
 				}
 
 				// Replace token with resolved value
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 			}
 			
        		// Resolve @PLAN_YEARS references, where @PLAN_YEARS is a reference to 
        		// the set of Plannable Years. 
  			token = PLAN_YEARS_TOKEN;
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 				// Get token value from token catalog. If not found then replace token with a blank.
 				String tokenValue = tokenCatalog.getProperty(token);
 				if (tokenValue == null) {
 					tokenValue = "";
 				}
 
 				// Replace token with resolved value
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 			}
 
        		// Resolve @NONPLAN_YEARS references, where @PLAN_YEARS is a reference to 
        		// the set of Non-Plannable Years. 
  			token = NONPLAN_YEARS_TOKEN;
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 				// Get token value from token catalog. If not found then replace token with a blank.
 				String tokenValue = tokenCatalog.getProperty(token);
 				if (tokenValue == null) {
 					tokenValue = "";
 				}
 
 				// Replace token with resolved value
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 			}
 
        		// Resolve @FIRST_PLAN_YEAR references, where @PLAN_YEARS is a reference to 
        		// the first Plannable Year 
  			token = FIRST_PLAN_YEAR_TOKEN;
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 				// Get token value from token catalog. If not found then replace token with a blank.
 				String tokenValue = tokenCatalog.getProperty(token);
 				if (tokenValue == null) {
 					tokenValue = "";
 				}
 
 				// Replace token with resolved value
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 			}
 
        		// Resolve @FIRST_NONPLAN_YEAR references, where @PLAN_YEARS is a reference to 
        		// the first non-Plannable Year 
  			token = FIRST_NONPLAN_YEAR_TOKEN;
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 				// Get token value from token catalog. If not found then replace token with a blank.
 				String tokenValue = tokenCatalog.getProperty(token);
 				if (tokenValue == null) {
 					tokenValue = "";
 				}
 
 				// Replace token with resolved value
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 			}
 
        		// Resolve @FIRST_PLAN_PERIOD references, where @PLAN_YEARS is a reference to 
        		// the first Plannable period 
  			token = FIRST_PLAN_PERIOD_TOKEN;
 			if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 				// Get token value from token catalog. If not found then replace token with a blank.
 				String tokenValue = tokenCatalog.getProperty(token);
 				if (tokenValue == null) {
 					tokenValue = "";
 				}
 
 				// Replace token with resolved value
 				headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 			}
 
 			// Resolve @DIMNAME references, where @DIMNAME is a reference to a view
         	// section page dimension member. 
         	for (int i = 0; i < pageDims.length; i++) {
         		
         		// Define search token (page dimension)
         		String pageDim = pageDims[i];
         		token = TOKEN_START + pageDim;
 
         		// Get token value (page member alias)
         		String pageMember = pageMembers[i];
         		PafDimMember dimMember = dimTreeMap.get(pageDim).getMember(pageMember);
            		String aliasTable = getAliasTableName(aliasMappingByDim, pageDim);
            		String tokenValue = dimMember.getMemberProps().getMemberAlias(aliasTable);
 
            		// Replace any occurences of token with page member alias 
          		headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);
         	}
         	
 			// Resolve @USER_SEL(ID) references, where ID is the ID of the user selection
         	// referenced on the view section.
 			if (headerText.toUpperCase().indexOf(USER_SEL_TOKEN) >= 0) {
 				
 				// Check each user selection
 				for (PafUserSelection userSel:userSelections) {
 
 					// Define search token
 					token = USER_SEL_TOKEN + TOKEN_PARM_START + userSel.getId() + TOKEN_PARM_END;
 
 					// Get user selections
 					String[] selMbrs = userSel.getValues();
 					
 					// Replace token with alias of user selection. In the case a multi-select, 
 					// concatenate the aliases of each individual member selection.
 					String selDim = userSel.getDimension();
 					String aliasTable = getAliasTableName(aliasMappingByDim, selDim);
 					StringBuffer tokenValue = new StringBuffer();
 					for (String selectedMbr:selMbrs) {
 						
 						// Get alias of selected member
 						PafDimMember dimMember = dimTreeMap.get(selDim).getMember(selectedMbr);
 						tokenValue.append(dimMember.getMemberProps().getMemberAlias(aliasTable));
 						tokenValue.append(", ");
 						
 					}
 					// Replace any occurences of token with aliases of member selection(s) 
 					tokenValue.deleteCharAt(tokenValue.length() - 2);
 					headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue.toString());
 				}
 			}   
 			
 			// Resolve @USER_SEL(DIMNAME) references, where DIMNAME is the name of a dimension
 			// associated with a user selection referenced on the view section.
 			if (headerText.toUpperCase().indexOf(USER_SEL_TOKEN + TOKEN_PARM_START) >= 0) {
 				
 				// Check each user selection
 				for (PafUserSelection userSel:userSelections) {
 
 					// Define search token
 					String selDim = userSel.getDimension();
 					token = USER_SEL_TOKEN + TOKEN_PARM_START + selDim + TOKEN_PARM_END;
 
 					// Get user selections
 					String[] selMbrs = userSel.getValues();
 					
 					// Replace token with alias of user selection. In the case a multi-select, 
 					// concatenate the aliases of each individual member selection.
 					String aliasTable = getAliasTableName(aliasMappingByDim, selDim);
 					StringBuffer tokenValue = new StringBuffer();
 					for (String selectedMbr:selMbrs) {
 						
 						// Get alias of selected member
 						PafDimMember dimMember = dimTreeMap.get(selDim).getMember(selectedMbr);
 						tokenValue.append(dimMember.getMemberProps().getMemberAlias(aliasTable));
 						tokenValue.append(", ");
 						
 					}
 					// Replace any occurences of token with aliases of member selection(s) 
 					tokenValue.deleteCharAt(tokenValue.length() - 2);
 					headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue.toString());
 				}
 			}   
 			
 			// Resolve @MEMBER_TAG(NAME) references, where NAME is the name of a valid member tag
 			// whose dimensions are all page dimensions on this view section.
 			if (headerText.toUpperCase().indexOf(MEMBER_TAG_TOKEN + TOKEN_PARM_START) >= 0) {
 
 				try {
 					// Open a new hibernate session
 					session = PafMemberTagManager.getSession();
 
 					// Check each defined member tag
 					List<String> pageDimList = Arrays.asList(pageDims);
 					for (MemberTagDef memberTagDef:memberTagDefs.values()) {
 
 						// Look for member tag reference in token
 						String memberTagName = memberTagDef.getName();
 						token = MEMBER_TAG_TOKEN + TOKEN_PARM_START + memberTagName + TOKEN_PARM_END;
 						if (headerText.toUpperCase().indexOf(token.toUpperCase()) >= 0) {
 
 							// Validate member tag - all member tag dimensions must be page dimensions
 							List<String> memberTagDimList = Arrays.asList(memberTagDef.getDims());
 							if (!pageDimList.containsAll(memberTagDimList)) {
 									String errMsg = "Invalid member tag reference [" + memberTagName + "] in page header"
 													+ "- not all dimensions are page dimensions";
 									logger.warn(errMsg);
 									continue;
 							}
 							
 							// Get member tag value
 							Intersection memberTagIS = initMemberTagIntersection(memberTagName, viewSection);
 							SimpleCoordList mtCoords = new SimpleCoordList(memberTagIS.getDimensions(), memberTagIS.getCoordinates());
 							MemberTagData memberTagData[] = null;
 							String tokenValue = null;
 							try {
 								memberTagData = PafMemberTagManager.getInstance().getMemberTagData(appName, memberTagName, mtCoords, session);
 								if (memberTagData[0] != null) {
 									tokenValue = memberTagData[0].getData();
 								} else {
 									tokenValue = "";
 								}
 							} catch (PafException pfe) {
 								String errMsg = "Error resolving member tag reference ["  + memberTagName + "] in page header";
 								logger.warn(errMsg);
 								continue;
 							}
 							
 							// Replace any occurences of token with member tag value
 							headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);
 							
 						}
 					}
 
 				} finally {
 					PafMemberTagManager.getInstance();
 					// Terminate hibernate session
 					PafMemberTagManager.terminateSession(session);
 				}				
 			}
 
 
 			// Resolve @ROWFILTER_SEL(DIMNAME) references, where DIMNAME is the name of a dimension 
 			// associated with a role filter selection. (TTN-1472)
 			if (headerText.toUpperCase().indexOf(ROLE_FILTER_SEL_TOKEN) >= 0) {
 
 				// Check each dimension
 				for (String dim : dimTreeMap.keySet()) {
 
 					// Define search token
 					token = ROLE_FILTER_SEL_TOKEN + TOKEN_PARM_START + dim.toUpperCase() + TOKEN_PARM_END;
 
 					// Resolve token if used in header
 					if (headerText.toUpperCase().indexOf(token) >= 0) {
 
 						// Get token value from token catalog. If not found then replace token with a blank.
 						String tokenValue = tokenCatalog.getProperty(token);
 						if (tokenValue == null) {
 							tokenValue = "";
 						}
 
 						// Replace token with resolved value
 						headerText = StringUtils.replaceAllIgnoreCase(headerText, token, tokenValue);							
 					}
 
 				}
 			}   
 
 			
 			// Update header text
 			viewHeader.setLabel(headerText);
         }   
 
          
         // Return updated view section
         return viewSection;
 	}
 	
 
 	/**
 	 *  Return the alias table name for the specified dimension
 	 *
 	 * @param aliasMappingsByDim Map of alias mappings by dimension
 	 * @param pageDim
 	 * @return
 	 */
 	private String getAliasTableName(Map<String, AliasMapping> aliasMappingsByDim, String dim) {
 
 		AliasMapping aliasMapping = aliasMappingsByDim.get(dim);
        	if (aliasMapping == null) {
        		String errMsg = "Unable to find alias mapping for dimension [" + dim + "]";
        		logger.error(errMsg);
        		throw new IllegalArgumentException(errMsg);
        	}
        	String aliasTable = aliasMapping.getAliasTableName();
        	if (aliasTable == null) {
        		String errMsg = "Unable to find alias table for dimension [" + dim + "]";
        		logger.error(errMsg);
        		throw new IllegalArgumentException(errMsg);
        	}
 
 		return aliasTable;
 	}
 
 	/**
 	 * @return the validMemberTagMap
 	 */
 	private Map<String, Map<String, PafAxis>> getValidMemberTagMap() {
 		
 		if (validMemberTagMap == null) {
 			validMemberTagMap = new HashMap<String, Map<String, PafAxis>>();
 		}
 		return validMemberTagMap;
 	}
 
 	/**
 	 * @param validMemberTagMap the memberTagMap to set
 	 */
 	private void setValidMemberTagMap(Map<String, Map<String, PafAxis>> validMemberTagMap) {
 		this.validMemberTagMap = validMemberTagMap;
 	}
 
 	private LockedCell[] sortLockedCells(ArrayList<LockedCell> lockedCellList) {
 
 		//create Locked Cell array from locked cells list
 		LockedCell[] lockedCells = lockedCellList.toArray(new LockedCell[0]);
 
 		//sort locked cells
 		Arrays.sort(lockedCells);
 
 		//return sorted locked cells.
 		return lockedCells;
 
 	}
 
 	private ArrayList<String> getTupleMemberDefs(ViewTuple tuple) {
 
 		ArrayList<String> memberDefs = new ArrayList<String>();
 
 		if (tuple.getMemberDefs() != null) {
 			for (String memberDef : tuple.getMemberDefs()) {
 				memberDefs.add(memberDef);
 			}
 		}
 
 		return memberDefs;
 	}
 
 	private String getVersionMember(String[] dimensions, ViewTuple tuple) {
 
 		String versionMember = null;
 
 		int index = 0;
 
 		String versionDim = pafApp.getMdbDef().getVersionDim();
 
 		if (dimensions != null) {
 			for (String dim : dimensions) {
 				if (dim.equals(versionDim)) {
 					break;
 				}
 				index++;
 			}
 			versionMember = tuple.getMemberDefs()[index];
 		}
 
 		return versionMember;
 	}
 
 	/**
 	 * Create an ordered list of year members
 	 * 
 	 * 
 	 * @return list of year members
 	 */
 	private List<String> getListofYearMembers() {
 
 		PafBaseTree yearTree = pafDataService.getBaseTree(pafApp
 				.getMdbDef().getYearDim());
 
 		List<PafDimMember> yearTreeList = yearTree.getMembersAtLevel(yearTree
 				.getRootNode().getKey(), (short) 0);
 
 		List<String> yearTreeStrList = null;
 
 		if (yearTreeList != null) {
 
 			yearTreeStrList = new ArrayList<String>(yearTreeList.size());
 
 			for (PafDimMember member : yearTreeList) {
 				yearTreeStrList.add(member.getKey());
 			}
 
 		}
 
 		return yearTreeStrList;
 	}
 
 	/**
 	 * Search the section to find the member for the given dimension.
 	 * 
 	 * @param dimension
 	 *            member to find, section, current row tuple, current col tuple
 	 * 
 	 * @return name of member
 	 */
 	private String getMember(String dimension, PafViewSection section,
 			ViewTuple row, ViewTuple col) {
 
 		//@TODO - This method is inefficient if called repeatedly on the same view section; add another method that determines what axis a given dimension is in
 		// and and an optional parameter that allows you to sear the specific axis.
 		String member = null;
 
 		if (section.getPageTuples() != null) {
 			for (PageTuple pageTuple : section.getPageTuples()) {
 				if (pageTuple.getAxis().equals(dimension)) {
 					member = pageTuple.getMember();
 				}
 			}
 		}
 
 		if (section.getRowAxisDims() != null && member == null) {
 			for (int i = 0; i < section.getRowAxisDims().length; i++) {
 				if (section.getRowAxisDims()[i].equals(dimension)) {
 					member = row.getMemberDefs()[i];
 				}
 			}
 		}
 
 		if (section.getColAxisDims() != null && member == null) {
 			for (int i = 0; i < section.getColAxisDims().length; i++) {
 				if (section.getColAxisDims()[i].equals(dimension)) {
 					member = col.getMemberDefs()[i];
 				}
 			}
 		}
 
 		return member;
 
 	}
 
 	private Map<String, PafAxis> mapDimensionsToAxis(PafViewSection section) {
 
 		Map<String, PafAxis> dimensions = new HashMap<String, PafAxis>();
 
 		if (section.getPageTuples() != null) {
 			for (PageTuple pageTuple : section.getPageTuples()) {
 				dimensions.put(pageTuple.getAxis(), new PafAxis(PafAxis.PAGE));
 			}
 		}
 
 		if (section.getRowAxisDims() != null) {
 			for (String dim : section.getRowAxisDims()) {
 				dimensions.put(dim, new PafAxis(PafAxis.ROW));
 			}
 		}
 
 		if (section.getColAxisDims() != null) {
 			for (String dim : section.getColAxisDims()) {
 				dimensions.put(dim, new PafAxis(PafAxis.COL));
 			}
 		}
 
 		return dimensions;
 	}
 	
 //	/**
 //	 * Set the attribute dimensions
 //	 * pmack
 //	 */
 //	private void addAttributeDimensions(PafViewSection[] sections, PafClientState clientState){
 //		List<String> attributeDims;
 //		boolean hasAttributes;
 //		
 //		for (PafViewSection section : sections) {
 //			attributeDims = new ArrayList<String>();
 //			hasAttributes = false;
 //			
 //			for (String dimName : section.getDimensions()){
 //				if (pafDataService.isAttributeDimension(dimName)){					
 //					//Add the attribute dimension name to the array of attribute dims
 //					attributeDims.add(dimName);
 //				}
 //			}
 //
 //			//Add the attribute dimension array to the View Section
 //			if (!attributeDims.isEmpty()){
 //				
 //				//The View Section has attribute dims
 //				hasAttributes = true;
 //				
 //				section.setAttributeDims((String[]) attributeDims.toArray(new String[0]));
 //			}
 //			
 //			//Set the has attributes flag for the View Section
 //			section.setHasAttributes(hasAttributes);
 //		}
 //		
 //	}
 	
 	/**
 	 * Set View Section properties
 	 * 
 	 */
 	private void initView(PafViewSection[] sections, PafClientState clientState){
 		
 		if (sections != null) {
 			List<String> attributeDims;
 			List<String> dimensionOrder;
 			List<String> axisPriority;
 			int baseDimIndexAxisPriority;
 			int baseDimIndexAxisSequence;
 			boolean hasAttributes;
 			
 		
 			for (PafViewSection section : sections) {
 				attributeDims = new ArrayList<String>();
 				dimensionOrder = new ArrayList<String>();
 				axisPriority = new ArrayList<String>();
 				hasAttributes = false;
 				
 				for (String dimName : section.getDimensions()){
 					if (pafDataService.isAttributeDimension(dimName)){
 						
 						//Add the attribute dimension name to the array of attribute dims
 						attributeDims.add(dimName);
 					}
 				}
 							
 				if (!attributeDims.isEmpty()){
 					//The View Section has attribute dims
 					hasAttributes = true;
 					
 					//Sort the attibute dimension in alpha order
 					java.util.Collections.sort(attributeDims);
 					
 					//Add the attribute dimension array to the View Section
 					section.setAttributeDims(attributeDims.toArray(new String[0]));
 					
 					//Get the list of base dimension names used in the View Section:
 					//dimensionPriority = java.util.Arrays.asList(clientState.getUnitOfWork().getDimensions());
 					dimensionOrder = new ArrayList<String>(Arrays.asList(clientState.getUnitOfWork().getDimensions()));					
 					axisPriority = new ArrayList<String>(Arrays.asList(clientState.getApp().getMdbDef().getAxisPriority()));					
 
 					//Attributes are added directly before the base dim in alpha order
 					for(String attDimName : attributeDims){
 						String baseDim = pafDataService.getAttributeTree(attDimName).getBaseDimName();
 						
 						baseDimIndexAxisPriority = dimensionOrder.indexOf(baseDim);
 						dimensionOrder.add(attDimName);
 						
 						baseDimIndexAxisSequence = axisPriority.indexOf(baseDim);
 						axisPriority.add(baseDimIndexAxisSequence, attDimName);
 					}
 					section.setDimensionsPriority(dimensionOrder.toArray(new String[0]));
 					section.setDimensionCalcSequence(axisPriority.toArray(new String[0]));
 
 				} else {
 					section.setDimensionsPriority(clientState.getUnitOfWork().getDimensions());
 					section.setDimensionCalcSequence(clientState.getApp().getMdbDef().getAxisPriority());
 				}
 
 				//Set the has attributes flag for the View Section
 				section.setHasAttributes(hasAttributes);
 			}
 		}
 	}
 
 	private PafViewSection[] resolveNumericFormatting(PafViewSection[] sections) {
 
 		if (sections != null) {
 
 			for (PafViewSection section : sections) {
 
 				if( ! section.isEmpty() ) {
 					int measureVersionAxis = getMeasureVersionAxis(section);
 					String measureDim = pafApp.getMdbDef().getMeasureDim();
 					String versionDim = pafApp.getMdbDef().getVersionDim();
 	
 					switch (measureVersionAxis) {
 	
 					case (PafViewSection.MEASURE_PAGE_AXIS | PafViewSection.VERSION_PAGE_AXIS):
 	
 						String measureMember = null;
 						String versionMember = null;
 	
 						for (PageTuple pageTuple : section.getPageTuples()) {
 							if (pageTuple.getAxis().equals(measureDim)) {
 								measureMember = pageTuple.getMember();
 							} else if (pageTuple.getAxis().equals(versionDim)) {
 								versionMember = pageTuple.getMember();
 							}
 						}
 	
 						if (section.getPrimaryFormattingAxis() == PafAxis.ROW) {
 							resolvePageNumericFormatting(section.getRowTuples(),
 									versionMember, measureMember);
 						} else if (section.getPrimaryFormattingAxis() == PafAxis.COL) {
 							resolvePageNumericFormatting(section.getColTuples(),
 									versionMember, measureMember);
 						}
 	
 						break;
 	
 					case (PafViewSection.MEASURE_COL_AXIS | PafViewSection.VERSION_COL_AXIS):
 	
 						Integer measureNdx = null;
 						Integer versionNdx = null;
 	
 						String[] colDims = section.getColAxisDims();
 	
 						if (colDims != null) {
 	
 							measureNdx = getArrayIndex(colDims, measureDim);
 							versionNdx = getArrayIndex(colDims, versionDim);
 	
 							resolveTupleNumericFormatting(section.getColTuples(),
 									versionNdx, measureNdx);
 	
 						}
 	
 						break;
 	
 					case (PafViewSection.MEASURE_ROW_AXIS | PafViewSection.VERSION_ROW_AXIS):
 	
 						measureNdx = 0;
 						versionNdx = 0;
 	
 						String[] rowDims = section.getRowAxisDims();
 	
 						if (rowDims != null) {
 	
 							measureNdx = getArrayIndex(rowDims, measureDim);
 							versionNdx = getArrayIndex(rowDims, versionDim);
 	
 							resolveTupleNumericFormatting(section.getRowTuples(),
 									versionNdx, measureNdx);
 	
 						}
 	
 						break;
 	
 					case (PafViewSection.MEASURE_COL_AXIS | PafViewSection.VERSION_ROW_AXIS):
 	
 						measureNdx = getArrayIndex(section.getColAxisDims(),
 								measureDim);
 						versionNdx = getArrayIndex(section.getRowAxisDims(),
 								versionDim);
 	
 						applyNumericFormattingToMeasuresOnly(
 								section.getColTuples(), measureNdx);
 						applyNumericFormattingToVersionsOnly(
 								section.getRowTuples(), versionNdx);
 	
 						break;
 	
 					case (PafViewSection.MEASURE_ROW_AXIS | PafViewSection.VERSION_COL_AXIS):
 	
 						measureNdx = getArrayIndex(section.getRowAxisDims(),
 								measureDim);
 						versionNdx = getArrayIndex(section.getColAxisDims(),
 								versionDim);
 	
 						applyNumericFormattingToMeasuresOnly(
 								section.getRowTuples(), measureNdx);
 						applyNumericFormattingToVersionsOnly(
 								section.getColTuples(), versionNdx);
 	
 						break;
 	
 					case (PafViewSection.MEASURE_PAGE_AXIS | PafViewSection.VERSION_COL_AXIS):
 					case (PafViewSection.MEASURE_PAGE_AXIS | PafViewSection.VERSION_ROW_AXIS):
 					case (PafViewSection.VERSION_PAGE_AXIS | PafViewSection.MEASURE_COL_AXIS):
 					case (PafViewSection.VERSION_PAGE_AXIS | PafViewSection.MEASURE_ROW_AXIS):
 	
 						measureMember = null;
 						versionMember = null;
 						versionNdx = null;
 						measureNdx = null;
 	
 						if ((measureVersionAxis & PafViewSection.MEASURE_PAGE_AXIS) > 0) {
 	
 							for (PageTuple pageTuple : section.getPageTuples()) {
 								if (pageTuple.getAxis().equals(measureDim)) {
 									measureMember = pageTuple.getMember();
 								}
 							}
 	
 							if ((measureVersionAxis & PafViewSection.VERSION_COL_AXIS) > 0) {
 	
 								versionNdx = getArrayIndex(
 										section.getColAxisDims(), versionDim);
 	
 								applyNumericFormatsWithProvidedMeasure(section
 										.getColTuples(), measureMember, versionNdx);
 	
 							} else if ((measureVersionAxis & PafViewSection.VERSION_ROW_AXIS) > 0) {
 	
 								versionNdx = getArrayIndex(
 										section.getRowAxisDims(), versionDim);
 	
 								applyNumericFormatsWithProvidedMeasure(section
 										.getRowTuples(), measureMember, versionNdx);
 	
 							}
 	
 						} else if ((measureVersionAxis & PafViewSection.VERSION_PAGE_AXIS) > 0) {
 	
 							for (PageTuple pageTuple : section.getPageTuples()) {
 								if (pageTuple.getAxis().equals(versionDim)) {
 									versionMember = pageTuple.getMember();
 								}
 							}
 	
 							if ((measureVersionAxis & PafViewSection.MEASURE_COL_AXIS) > 0) {
 	
 								measureNdx = getArrayIndex(
 										section.getColAxisDims(), measureDim);
 	
 								applyNumericFormatsWithProvidedVersion(section
 										.getColTuples(), versionMember, measureNdx);
 	
 							} else if ((measureVersionAxis & PafViewSection.MEASURE_ROW_AXIS) > 0) {
 	
 								measureNdx = getArrayIndex(
 										section.getRowAxisDims(), measureDim);
 	
 								applyNumericFormatsWithProvidedVersion(section
 										.getRowTuples(), versionMember, measureNdx);
 	
 							}
 						}
 	
 						break;
 					}
 				}
 			}
 		}
 
 		return sections;
 	}
 
 	// TODO refactor this ever growing list of user operators into something
 	// better
 	private PafView replaceUserOperators(PafView view, PafUserSelection[] userSelections,
 			PafClientState clientState) {
 		//PafUserSelection[] userSelections = viewRequest.getUserSelections();
         clientState.getUserSelections().put(view.getName(), userSelections);
         
         // save user selections into client state
 
 		// search each tuple collection for @USER_SEL operators
 		for (PafViewSection viewSection : view.getViewSections()) {
 			// process page tuples
 			for (PageTuple pt : viewSection.getPageTuples()) {
 				if (pt.getMember().contains("@USER_SEL(")) {
 					pt
 							.setMember(replaceUserSel(pt.getMember(),
 									userSelections));
 				}
 				if (pt.getMember().contains("@UOW_ROOT")) {
 					pt.setMember(replaceUserUow(pt.getMember(), clientState, pt
 							.getAxis()));
 				}
 				if (pt.getMember().contains("@PLAN_VERSION")) {
 					pt.setMember(replaceUserVers(pt.getMember(), clientState));
 				}
 				if ( pt.getMember().contains("@FIRST_PLAN_YEAR") || pt.getMember().contains("@FIRST_NONPLAN_YEAR") 
 						|| pt.getMember().contains("@FIRST_PLAN_PERIOD")  ) {
 					pt.setMember(replaceUserMultiYear(pt.getMember(), clientState));
 				}
 			}
 			
 			// process row tuples
 			for (ViewTuple vt : viewSection.getRowTuples()) {
 				int dimCnt = viewSection.getRowAxisDims().length;
 				for (int i = 0; i < dimCnt; i++) {
 					if (vt.getMemberDefs()[i].contains("@USER_SEL(")) {
 						vt.getMemberDefs()[i] = replaceUserSel(vt
 								.getMemberDefs()[i], userSelections);
 					}
 					if (vt.getMemberDefs()[i].contains("@UOW_ROOT")) {
 						vt.getMemberDefs()[i] = replaceUserUow(vt
 								.getMemberDefs()[i], clientState, viewSection
 								.getRowAxisDims()[i]);
 					}
 					if (vt.getMemberDefs()[i].contains("@PLAN_VERSION")) {
 						vt.getMemberDefs()[i] = replaceUserVers(vt
 								.getMemberDefs()[i], clientState);
 					}
 					if ( vt.getMemberDefs()[i].contains("@FIRST_PLAN_YEAR") || vt.getMemberDefs()[i].contains("@FIRST_NONPLAN_YEAR") 
 							 || vt.getMemberDefs()[i].contains("@PLAN_YEARS") || vt.getMemberDefs()[i].contains("@NONPLAN_YEARS") 
 							 || vt.getMemberDefs()[i].contains("@FIRST_PLAN_PERIOD")  ) {
 						vt.getMemberDefs()[i] = replaceUserMultiYear(vt.getMemberDefs()[i], clientState);
 					}
 				}
 			}
 
 			// process col tuples
 			for (ViewTuple vt : viewSection.getColTuples()) {
 				int dimCnt = viewSection.getColAxisDims().length;
 				for (int i = 0; i < dimCnt; i++) {
 					if (vt.getMemberDefs()[i].contains("@USER_SEL(")) {
 						vt.getMemberDefs()[i] = replaceUserSel(vt
 								.getMemberDefs()[i], userSelections);
 					}
 					if (vt.getMemberDefs()[i].contains("@UOW_ROOT")) {
 						vt.getMemberDefs()[i] = replaceUserUow(vt
 								.getMemberDefs()[i], clientState, viewSection
 								.getColAxisDims()[i]);
 					}
 					if (vt.getMemberDefs()[i].contains("@PLAN_VERSION")) {
 						vt.getMemberDefs()[i] = replaceUserVers(vt
 								.getMemberDefs()[i], clientState);
 					}
 					if ( vt.getMemberDefs()[i].contains("@FIRST_PLAN_YEAR") || vt.getMemberDefs()[i].contains("@FIRST_NONPLAN_YEAR") 
 							 || vt.getMemberDefs()[i].contains("@PLAN_YEARS") || vt.getMemberDefs()[i].contains("@NONPLAN_YEARS") 
 							 || vt.getMemberDefs()[i].contains("@FIRST_PLAN_PERIOD")  ) {
 						vt.getMemberDefs()[i] = replaceUserMultiYear(vt.getMemberDefs()[i], clientState);
 					}
 
 				}
 			}
 			
 			/*	TTN 609 - presorted ranking view
 			 * 
 			 * 	process sorting tuples
 			 */ 
 			SortingTuples sortTuples = viewSection.getSortingTuples();
 			if( sortTuples != null ) {
 				for (SortingTuple st : sortTuples.getSortingTupleList()) {
 					int dimCnt = st.getIntersection().getDimensions().length;
 					for (int i = 0; i < dimCnt; i++) {
 						String member = st.getIntersection().getCoordinates()[i];
 						String dimension = st.getIntersection().getDimensions()[i];
 						if (member.contains("@USER_SEL(")) {
 							member = replaceUserSel(member,	userSelections);
 						}
 						else if (member.contains("@UOW_ROOT")) {
 							member = replaceUserUow(member,	clientState, dimension);
 						}
 						else if (member.contains("@PLAN_VERSION")) {
 							member = replaceUserVers(member, clientState);
 						}
 						if ( member.contains("@FIRST_PLAN_YEAR") || member.contains("@FIRST_NONPLAN_YEAR") 
 								 || member.contains("@PLAN_YEARS") || member.contains("@NONPLAN_YEARS") 
 								 || member.contains("@FIRST_PLAN_PERIOD")  ) {
 							member = replaceUserMultiYear(member, clientState);
 						}
 						/*	TTN 1774- Pace Client is not sorting on the tuples set on AC.
 						 * 
 						 *	Update sorting tuples from user selections 
 						 */
 						else {
 							if ( userSelections != null ) {
 								member = replaceUserSelForSorting(dimension, member, userSelections);
 							}
 						}
 						st.getIntersection().getCoordinates()[i] = member;
 					}
 				}
 			}
 		}
 
 		return view;
 	}
 
 	/** 
 	 * Go through user selections and replace sorting column if any member value is different from user selection
 	 * 
 	 * @param dimension - input dimension
 	 * @param member - input member
 	 * @param userSelections - an array of user selections
 	 * @return modified member
 	 */
 	private String replaceUserSelForSorting(String dimension, String member, PafUserSelection[] userSelections) {
 		for (PafUserSelection sel : userSelections) {
 			if (sel != null) {
 				if (sel.getPafAxis().getValue() == sel.getPafAxis().getColAxis() && 
 						sel.getDimension().equals(dimension) && ! sel.getValues()[0].equals(member)) {
 					return sel.getValues()[0].trim();
 				}
 			}
 		}
 		return member.trim();
 	}
 
 	public String replaceUserUow(String member, PafClientState clientState, String dim) {
 
 		//UnitOfWork uow = clientState.getUnitOfWork();
 		//PafDimMember uowRoot = pafDataService.getUowRoot(clientState, dim, uow);
 		if( clientState.getUowTrees() != null ) {
 			final PafDimTree dimTree = clientState.getUowTrees().getTree(dim);
 			final String uowRoot = dimTree.getRootNode().getKey();
 			
 			//Matcher.quoteReplacement replaces all $ with \$
 			member = member.replaceAll("@UOW_ROOT", Matcher.quoteReplacement(uowRoot));
 			return member.trim();
 		} else {
             String errMsg = "UOWTrees collection in clientState is null";
             logger.error(errMsg);
  			throw new IllegalStateException(errMsg);
 		}
 	}
 
 	public String replaceUserVers(String member, PafClientState clientState) {
 
 		//Matcher.quoteReplacement replaces all $ with \$
 		member = member.replaceAll("@PLAN_VERSION", Matcher.quoteReplacement(clientState
 				.getPlanningVersion().getName()));
 		return member.trim();
 	}
 
 	private String replaceUserSel(String member,
 			PafUserSelection[] userSelections) {
 		String key = null;
 		String[] tokens;
 		tokens = member.split("[@\\(\\)]");
 		for (int i = 0; i < tokens.length; i++) {
 			if (tokens[i].trim().equalsIgnoreCase("USER_SEL")) {
 				key = tokens[i + 1].trim();
 				break;
 			}
 		}
 
 		if (key == null) {
 			throw new IllegalArgumentException(
 					"User selection key not found collection");
 		}
 
 		for (PafUserSelection sel : userSelections) {
 			if (sel != null) {
 				if (sel.getId().equalsIgnoreCase(key)) {
 					String toReplace = "@USER_SEL\\(" + key + "\\)";
 
 					//Matcher.quoteReplacement replaces all $ with \$
 					String fixedUserSelections = Matcher.quoteReplacement(sel.getValuesAsString());										
 					
 					member = member.replaceAll(toReplace, fixedUserSelections);
 
 					if (sel.getValues().length > 1) {
 
 						member = "@MEMBERS(" + member.trim() + ")";
 
 					}
 				}
 			}
 		}
 
 		return member.trim();
 	}
 
 	public String replaceUserMultiYear (String member, PafClientState clientState) {
 		Properties tokenCatalog = null; 
 		String parmKey = "", parmVal = "";
 		if( clientState != null ) {
 			tokenCatalog = clientState.getTokenCatalog();
 		}
 
 		if( member.contains("@FIRST_PLAN_YEAR")) {
 			parmKey = PafBaseConstants.VIEW_TOKEN_FIRST_PLAN_YEAR;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			member = member.replaceAll("@FIRST_PLAN_YEAR", Matcher.quoteReplacement(parmVal));
 		}
 		if( member.contains("@FIRST_NONPLAN_YEAR")) {
 			parmKey = PafBaseConstants.VIEW_TOKEN_FIRST_NONPLAN_YEAR;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			member = member.replaceAll("@FIRST_NONPLAN_YEAR", Matcher.quoteReplacement(parmVal));
 		}
 		if( member.contains("@FIRST_PLAN_PERIOD")) {
 			parmKey = PafBaseConstants.VIEW_TOKEN_FIRST_PLAN_PERIOD;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			member = member.replaceAll("@FIRST_PLAN_PERIOD", Matcher.quoteReplacement(parmVal));
 		}
 		if( member.contains("@PLAN_YEARS")) {
 			parmKey = PafBaseConstants.VIEW_TOKEN_PLAN_YEARS;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			member = member.replaceAll("@PLAN_YEARS", Matcher.quoteReplacement(parmVal));
 		}
 		if( member.contains("@NONPLAN_YEARS")) {
 			parmKey = PafBaseConstants.VIEW_TOKEN_NONPLAN_YEARS;
 			parmVal = tokenCatalog.getProperty(parmKey);
 			if ( parmVal == null ) {
 				String errMsgDtl = "Unable to resolve the [" + parmKey + "] property";
 				logger.error(errMsgDtl);
 				throw new IllegalArgumentException(errMsgDtl);
 			}
 			member = member.replaceAll("@NONPLAN_YEARS", Matcher.quoteReplacement(parmVal));
 		}
 		//Matcher.quoteReplacement replaces all $ with \$
 		return member.trim();
 	}
 
 	protected void GenerateSampleView() {
 		logger.info("Generating Sample View.");
 		PafMetaData.exportScreens(UtilityStubs.getSampleView());
 	}
 
 	/**
 	 * Populate the paf view tree items from the view cache.
 	 *	 
 	 * @return PafViewTreeItem[]
 	 */
 	public PafViewTreeItem[] getViewTreeItems() {
 		// TODO Implement client specific view tree
 		// TODO Implement hierarchical view trees
 		
 		//if in debug mode, reload view cache
 		if (PafMetaData.isDebugMode()) {
 			loadViewCache();
 		}
 		
 		//get global alias mappings
 		Map<String, AliasMapping> globalAliasMappingSet = PafMetaData.getGlobalAliasMappingsByDim();  //TTN-1454
 		
 		String appId = PafAppService.getInstance().getApplications().get(0).getAppId();
 		
 		PafViewTreeItem entry[] = new PafViewTreeItem[viewCache.length];
 		int i = 0;
 		for (PafView view : viewCache) {
 			entry[i] = new PafViewTreeItem();
 			entry[i].setLabel(view.getName());
 			entry[i].setDesc(view.getDesc());
 			entry[i].setGroup(false);
 			entry[i].setUserSelections(view.getUserSelections());
 			entry[i].setAliasMappings(configureGlobalAliasMappings(globalAliasMappingSet, view.getViewSections()));
 			
 			int j = 0;
 			if( view.getViewSectionNames() != null ) {
 				SuppressZeroSettings[] suppressZeroSettings = new SuppressZeroSettings [view.getViewSectionNames().length];
 				for(PafViewSection viewSection : view.getViewSections()){
 					suppressZeroSettings[j] = PafAppService.getInstance().resolveSuppressZeroSettings(viewSection.getSuppressZeroSettings(), appId);			
 				}
 				
 				entry[i].setSuppressZeroSettings(suppressZeroSettings);
 				
 			}
 			i++;
 		}
 		
 		return entry;
 	}
 
 	/**
 	 * By passing in the view section, the measure and version axis's will be
 	 * determined by looping through the page, col, and row tuples.
 	 * 
 	 * @param Complex
 	 *            view section
 	 * @return A unique int identifying which axis measure and version exist
 	 */
 	private int getMeasureVersionAxis(PafViewSection section) {
 
 		int measureVersionAxis = 0;
 
 		PageTuple[] pageTuples = section.getPageTuples();
 
 		String measureDim = pafApp.getMdbDef().getMeasureDim();
 		String versionDim = pafApp.getMdbDef().getVersionDim();
 
 		if (pageTuples != null) {
 
 			for (PageTuple pageTuple : pageTuples) {
 				if (pageTuple.getAxis().equals(measureDim)) {
 					measureVersionAxis += PafViewSection.MEASURE_PAGE_AXIS;
 				} else if (pageTuple.getAxis().equals(versionDim)) {
 					measureVersionAxis += PafViewSection.VERSION_PAGE_AXIS;
 				}
 			}
 		}
 
 		String[] colDims = section.getColAxisDims();
 
 		if (colDims != null) {
 			for (String colDim : colDims) {
 				if (colDim.equals(measureDim)) {
 					measureVersionAxis += PafViewSection.MEASURE_COL_AXIS;
 				} else if (colDim.equals(versionDim)) {
 					measureVersionAxis += PafViewSection.VERSION_COL_AXIS;
 				}
 			}
 		}
 
 		String[] rowDims = section.getRowAxisDims();
 
 		if (rowDims != null) {
 			for (String rowDim : rowDims) {
 				if (rowDim.equals(measureDim)) {
 					measureVersionAxis += PafViewSection.MEASURE_ROW_AXIS;
 				} else if (rowDim.equals(versionDim)) {
 					measureVersionAxis += PafViewSection.VERSION_ROW_AXIS;
 				}
 			}
 		}
 
 		return measureVersionAxis;
 
 	}
 
 	private Integer getArrayIndex(String[] dimensions, String searchKey) {
 
 		Integer index = null;
 
 		for (int i = 0; i < dimensions.length; i++) {
 			if (dimensions[i].equals(searchKey)) {
 				index = i;
 				break;
 			}
 		}
 
 		return index;
 	}
 
 	private void applyNumericFormattingToMeasuresOnly(ViewTuple[] tuples,
 			int measureNdx) {
 
 		if (tuples != null) {
 
 			for (ViewTuple tuple : tuples) {
 
 				if (tuple.getNumberFormatOverrideLabel() == null) {
 
 					String measureMember = tuple.getMemberDefs()[measureNdx];
 
 					// if is a paf blank, continue to next tuple
 					if (measureMember.equals(PafBaseConstants.PAF_BLANK)) {
 						continue;
 					}
 
 					PafNumberFormat measureNumericFormat = getMeasureNumericFormat(measureMember);
 
 					tuple.setNumberFormat(measureNumericFormat);
 
 				} else {
 
 					String key = tuple.getNumberFormatOverrideLabel();
 					if (globalNumericFormatCache.containsKey(key)) {
 						tuple
 								.setNumberFormat(globalNumericFormatCache
 										.get(key));
 					}
 
 				}
 			}
 		}
 
 	}
 
 	private void applyNumericFormattingToVersionsOnly(ViewTuple[] tuples,
 			int versionNdx) {
 
 		if (tuples != null) {
 
 			for (ViewTuple tuple : tuples) {
 
 				if (tuple.getNumberFormatOverrideLabel() == null) {
 
 					String versionMember = tuple.getMemberDefs()[versionNdx];
 
 					// if is a paf blank, continue to next tuple
 					if (versionMember.equals(PafBaseConstants.PAF_BLANK)) {
 						continue;
 					}
 
 					tuple
 							.setNumberFormat(getVersionNumericFormat(versionMember));
 
 				} else {
 
 					String key = tuple.getNumberFormatOverrideLabel();
 					if (globalNumericFormatCache.containsKey(key)) {
 						tuple
 								.setNumberFormat(globalNumericFormatCache
 										.get(key));
 					}
 
 				}
 			}
 		}
 
 	}
 
 	private void resolvePageNumericFormatting(ViewTuple[] tuples,
 			String versionMember, String measureMember) {
 
 		PafNumberFormat versionNumericFormat = getVersionNumericFormat(versionMember);
 		PafNumberFormat measureNumericFormat = getMeasureNumericFormat(measureMember);
 
 		for (ViewTuple tuple : tuples) {
 
 			// apply numeric formatting using the measure version logic
 			tuple = numericFormattingMeasureVersionLogic(tuple,
 					versionNumericFormat, measureNumericFormat);
 
 		}
 
 	}
 
 	private void resolveTupleNumericFormatting(ViewTuple[] tuples,
 			int versionNdx, int measureNdx) {
 
 		for (ViewTuple tuple : tuples) {
 
 			if (versionNdx > tuple.getMemberDefs().length
 					|| measureNdx > tuple.getMemberDefs().length) {
 
 				logger.debug("Version NDX: " + versionNdx + ", Measure NDX: "
 						+ measureNdx + ", MemberDefs Ar Length: "
 						+ tuple.getMemberDefs().length);
 				logger.debug("Check file: "
 						+ PafBaseConstants.FN_ScreenMetaData);
 
 			}
 
 			String versionMember = tuple.getMemberDefs()[versionNdx];
 			String measureMember = tuple.getMemberDefs()[measureNdx];
 
 			// if is a paf blank, continue to next tuple
 			if (versionMember.equals(PafBaseConstants.PAF_BLANK)
 					|| measureMember.equals(PafBaseConstants.PAF_BLANK)) {
 				continue;
 			}
 
 			PafNumberFormat versionNumericFormat = getVersionNumericFormat(versionMember);
 			PafNumberFormat measureNumericFormat = getMeasureNumericFormat(measureMember);
 
 			// apply numeric formatting using the measure version logic
 			tuple = numericFormattingMeasureVersionLogic(tuple,
 					versionNumericFormat, measureNumericFormat);
 
 		}
 
 	}
 
 	private ViewTuple numericFormattingMeasureVersionLogic(ViewTuple tuple,
 			PafNumberFormat versionNumericFormat,
 			PafNumberFormat measureNumericFormat) {
 
 		// if override label exist for numeric format, then use, otherwise
 		// use version/measure logic
 		if (tuple.getNumberFormatOverrideLabel() != null) {
 			String key = tuple.getNumberFormatOverrideLabel();
 			if (globalNumericFormatCache.containsKey(key)) {
 				tuple
 						.setNumberFormat(globalNumericFormatCache
 								.get(key));
 			} else {
 				logger
 						.info("Number Format Override Label: "
 								+ key
 								+ " was in tuple but didn't exist in the numeric format cache.");
 			}
 
 		} else {
 
 			if (versionNumericFormat != null) {
 				tuple.setNumberFormat(versionNumericFormat);
 			} else {
 				tuple.setNumberFormat(measureNumericFormat);
 			}
 		}
 
 		return tuple;
 	}
 
 	// get Version numberic format from loaded version cache
 	private PafNumberFormat getVersionNumericFormat(String versionMember) {
 
 		PafNumberFormat versionNumericFormat = null;
 
 		if (versionsNumberFormatMapCache != null
 				&& versionsNumberFormatMapCache.containsKey(versionMember)) {
 			versionNumericFormat = versionsNumberFormatMapCache.get(versionMember);
 		}
 
 		return versionNumericFormat;
 	}
 
 	// get Measure numberic format from loaded measure cache
 	private PafNumberFormat getMeasureNumericFormat(String measureMember) {
 
 		PafNumberFormat measureNumericFormat = null;
 
 		if (measuresMapCache != null
 				&& measuresMapCache.containsKey(measureMember)) {
 			measureNumericFormat = measuresMapCache.get(measureMember);
 		}
 
 		return measureNumericFormat;
 	}
 
 	public void applyNumericFormatsWithProvidedMeasure(ViewTuple[] tuples,
 			String measureMember, int versionNdx) {
 
 		if (tuples != null) {
 
 			PafNumberFormat measureNumberFormat = getMeasureNumericFormat(measureMember);
 
 			for (ViewTuple tuple : tuples) {
 
 				String versionMember = tuple.getMemberDefs()[versionNdx];
 
 				PafNumberFormat versionNumberFormat = getVersionNumericFormat(versionMember);
 
 				tuple = numericFormattingMeasureVersionLogic(tuple,
 						versionNumberFormat, measureNumberFormat);
 
 			}
 
 		}
 
 	}
 
 	public void applyNumericFormatsWithProvidedVersion(ViewTuple[] tuples,
 			String versionMember, int measureNdx) {
 
 		if (tuples != null) {
 
 			PafNumberFormat versionNumberFormat = getVersionNumericFormat(versionMember);
 
 			for (ViewTuple tuple : tuples) {
 
 				String measureMember = tuple.getMemberDefs()[measureNdx];
 
 				PafNumberFormat measureNumberFormat = getMeasureNumericFormat(measureMember);
 
 				tuple = numericFormattingMeasureVersionLogic(tuple,
 						versionNumberFormat, measureNumberFormat);
 
 			}
 
 		}
 
 	}
 
 	private PafViewSection[] applyMeasureSecurity(PafViewSection[] sections) {
 
 		String measureDim = pafApp.getMdbDef().getMeasureDim();
 
 		if (sections != null && sections.length > 0) {
 
 			for (PafViewSection section : sections) {
 
 				if( ! section.isEmpty() ) {
 					PafAxis measureAxis = section.getAxis(measureDim);
 	
 					String measureMember = null;
 	
 					boolean measurePlannable = false;
 	
 					switch (measureAxis.getValue()) {
 	
 					case PafAxis.PAGE:
 	
 						for (PageTuple tuple : section.getPageTuples()) {
 							if (tuple.getAxis().equals(measureDim)) {
 								measureMember = tuple.getMember();
 								break;
 							}
 						}
 	
 						measurePlannable = measuresPlannableCache
 								.get(measureMember);
 	
 						if (section.getPrimaryFormattingAxis() == PafAxis.ROW) {
 	
 							for (ViewTuple tuple : section.getRowTuples()) {
 	
 								tuple = applyMeasureSecurityToTuple(
 										measurePlannable, tuple);
 	
 							}
 	
 						} else if (section.getPrimaryFormattingAxis() == PafAxis.COL) {
 	
 							for (ViewTuple tuple : section.getColTuples()) {
 	
 								tuple = applyMeasureSecurityToTuple(
 										measurePlannable, tuple);
 	
 							}
 	
 						}
 	
 						break;
 					case PafAxis.COL:
 	
 						section.setColTuples(applyMeasureSecurityToTuples(section
 								.getColAxisDims(), section.getColTuples()));
 	
 						break;
 					case PafAxis.ROW:
 	
 						section.setRowTuples(applyMeasureSecurityToTuples(section
 								.getRowAxisDims(), section.getRowTuples()));
 	
 						break;
 	
 					}
 				}
 			}
 
 		}
 
 		return sections;
 	}
 
 	private ViewTuple[] applyMeasureSecurityToTuples(String[] dimensions,
 			ViewTuple[] tuples) {
 
 		String measureDim = pafApp.getMdbDef().getMeasureDim();
 
 		String measureMember = null;
 
 		Boolean measurePlannable = false;
 
 		int measureIndex = getArrayIndex(dimensions, measureDim);
 
 		for (ViewTuple tuple : tuples) {
 
 			measureMember = tuple.getMemberDefs()[measureIndex];
 
 			if (measureMember.equals(PafBaseConstants.PAF_BLANK)) {
 				continue;
 			}
 
 			measurePlannable = measuresPlannableCache.get(measureMember);
 
 			if (measurePlannable != null) {
 				tuple = applyMeasureSecurityToTuple(measurePlannable, tuple);
 			}
 
 		}
 
 		return tuples;
 
 	}
 
 	private ViewTuple applyMeasureSecurityToTuple(boolean measurePlannable,
 			ViewTuple tuple) {
 
 		Boolean tuplePlannable = tuple.getPlannable();
 
 		if (tuplePlannable == null) {
 			tuplePlannable = new Boolean(measurePlannable);
 			tuple.setPlannable(tuplePlannable);
 		}
 		// if measure is not plannable and tuple is
 		// plannable
 		if (!measurePlannable && tuple.getPlannable()) {
 			tuple.setPlannable(measurePlannable);
 		}
 
 		return tuple;
 
 	}
 
 	private PafNumberFormat getDefaultNumberFormat() {
 
 		for (PafNumberFormat format : globalNumericFormatCache.values()) {
 
 			if (format.isDefaultFormat()) {
 				return format;
 			}
 
 		}
 
 		return null;
 	}
 
 	/**
 	 * @return Returns the invalidViewsMap.
 	 */
 	public static Map<String, String> getInvalidViewsMap() {
 		return invalidViewsMap;
 	}
 
 	/**
 	 * @param invalidViewsMap The invalidViewsMap to set.
 	 */
 	public static void setInvalidViewsMap(Map<String, String> invalidViewsMap) {
 		PafViewService.invalidViewsMap = invalidViewsMap;
 	}
 	
 
 	/**
 	 * Process invalid view tuples
 	 * 
 	 * @param viewSection View section
 	 * @param clientState Client state
 	 * @throws PafException 
 	 */
 	public void processInvalidTuples(PafViewSection viewSection, PafClientState clientState) throws PafException {
 
 		processInvalidTimeHorizTuples(viewSection, clientState);
 		processInvalidAttrTuples(viewSection, clientState);
 		
 	}
 
 	/**
 	 * Process invalid time horizon tuples
 	 * 
 	 * @param viewSection View section
 	 * @param clientState Client state
 	 * @throws PafException 
 	 */
 	private void processInvalidTimeHorizTuples(PafViewSection viewSection, PafClientState clientState) throws PafException {
 		
 		// Filter out any col/tuple combinations that correspond to invalid time horizon coorindates
 		ViewTuple[] rowViewTuples = viewSection.getRowTuples();
 		ViewTuple[] colViewTuples = viewSection.getColTuples();
 		String[] rowAxes = viewSection.getRowAxisDims();
 		String[] colAxes = viewSection.getColAxisDims();
 		String rowAxisList = "", colAxisList = "";
 		MdbDef mdbDef = clientState.getMdbDef();
 		String timeDim = mdbDef.getTimeDim(), yearDim = mdbDef.getYearDim();
 		Set<String> invalidTimeHorizonPeriods = clientState.getInvalidTimeHorizonPeriods();
 		List<ViewTuple> expandedRowTuples = new ArrayList<ViewTuple>();
 		List<ViewTuple> expandedColTuples = new ArrayList<ViewTuple>();
 		List<ViewTuple> blankTupleList = new ArrayList<ViewTuple>();
 		
 		// Initialization
 		for (String a : rowAxes) {
 			rowAxisList += a + " "; 
 		}
 		logger.debug("Expanding row tuples for row axis: " + rowAxisList);  
 		for (String a : colAxes) {
 			colAxisList += a + " "; 
 		}
 		logger.debug("Expanding column tuples for column axis: " + colAxisList);  
 		
 		// Expand tuples
 		int rowDimCount = rowAxes.length;
 		int innerRowAxisIndex = rowDimCount -1;
 		for (ViewTuple vt:rowViewTuples) {   
 			expandedRowTuples.addAll(pafDataService.expandTuple(vt, innerRowAxisIndex, rowAxes[innerRowAxisIndex], clientState));   
 		}
 		
 		int colDimCount = colAxes.length;
 		int innerColAxisIndex = colDimCount -1;
 		for (ViewTuple vt:colViewTuples) {   
 			expandedColTuples.addAll(pafDataService.expandTuple(vt, innerColAxisIndex, colAxes[innerColAxisIndex], clientState));   
 		}
 		
 		// Compile a list of attribute dimensions used in this tuple or the page tuple. This
 		// information will be used to in an initial pass at filtering out invalid member 
 		// intersections. (TTN-1469)
 		String[] attributeDims = viewSection.getAttributeDims();
 		Set<String> tupleAttrDims = new HashSet<String>();
 		List<String> rowAxisDimList = Arrays.asList(rowAxes);
 		List<String> colAxisDimList = Arrays.asList(colAxes);
 		if (rowAxes.length > 0 && attributeDims != null && attributeDims.length > 1) {
 			// Attribute View
 			// Check for attributes on the tuple (page or current axis)
 			for (String attrDim : attributeDims) {
 				if (rowAxisDimList.contains(attrDim) || colAxisDimList.contains(attrDim)) {
 					tupleAttrDims.add(attrDim);
 				}
 			}
 		}
 		
 		// if Time are rows and Year are Columns or Year are Rows and Time are Columns, we need to perform time horizon validation 
 		// if the tuple doesn't contain at least one of these dimensions, No time horizon validation will be performed.
 		boolean bDoTimeHorizValidation = true, bTimeOnRow = false, bYearOnRow = false, bTimeOnCol = false, bYearOnCol = false;
 		int timeAxis = 0, yearAxis = 0;
 		String period = null, year = null;	
 
 		do {
 			// Search for time dimension
 			if (rowAxisDimList.contains(timeDim)) {
 				timeAxis = rowAxisDimList.indexOf(timeDim);
 				bTimeOnRow = true;
 			} else if (colAxisDimList.contains(timeDim)) {
 				timeAxis = colAxisDimList.indexOf(timeDim);
 				bTimeOnCol = true;
 			} else {
 				// Time dimension was not found - skip validation
 				bDoTimeHorizValidation = false;	
 				break;
 			}
 			
 			// Search for year dimension
 			if (rowAxisDimList.contains(yearDim)) {
 				yearAxis = rowAxisDimList.indexOf(yearDim);
 				bYearOnRow = true;
 			} else if (colAxisDimList.contains(yearDim)) {
 				yearAxis = colAxisDimList.indexOf(yearDim);
 				bYearOnCol = true;
 			} else {
 				// Year dimension was not found - skip validation
 				bDoTimeHorizValidation = false;	
 				break;
 			}
 		 } while (false);
 		
 
 		// Expand each symmetric tuple group (for tuples comprised of multiple dimensions)
 		for (int rowAxisIndex = innerRowAxisIndex - 1; rowAxisIndex >= 0; rowAxisIndex--) {
 			expandedRowTuples = pafDataService.expandSymetricTupleGroups(rowAxisIndex, expandedRowTuples.toArray(new ViewTuple[0]), rowAxes, tupleAttrDims, viewSection.getPageAxisDims(), viewSection.getAttributeDims(), clientState);
 		}
 		for (int colAxisIndex = innerColAxisIndex - 1; colAxisIndex >= 0; colAxisIndex--) {
 			expandedColTuples = pafDataService.expandSymetricTupleGroups(colAxisIndex, expandedColTuples.toArray(new ViewTuple[0]), colAxes, tupleAttrDims, viewSection.getPageAxisDims(), viewSection.getAttributeDims(), clientState);
 		}
 
 		List<ViewTuple> tuplesToLock = new ArrayList<ViewTuple>();
 		List<String> coordToLockList = new ArrayList<String>(); 
 		boolean bFoundValidTimeHorizPeriod = false;
 		// -- Now check each expanded view tuple
 		// Test for invalid time horizon coordinate when time and year on column or tuple
 		if ( bTimeOnRow && bYearOnCol && expandedRowTuples.size() > 0 && expandedColTuples.size() > 0 ) {
 			for (ViewTuple viewTuple:expandedRowTuples) {
 				// Initialization
 				String[] memberArray = viewTuple.getMemberDefs();
 	
 				// If any tuple member is set to PAFBLANK, set the remaining members to PAFBLANK as well
 				if (pafDataService.isBlankViewTuple(viewTuple)) {
 					for (int i = 0; i < memberArray.length; i++) {
 						memberArray[i] = PafBaseConstants.PAF_BLANK;
 					}
 					viewTuple.setMemberDefs(memberArray);
 					blankTupleList.add(viewTuple);
 					continue;
 				}
 				// Compile a list of any tuples with invalid time / year combinations (TTN-1858 / TTN-1886).
 				if (bDoTimeHorizValidation) {
 					period = memberArray[timeAxis];
 					year = expandedColTuples.get(0).getMemberDefs()[yearAxis];
 					if( ! period.isEmpty() && ! year.isEmpty() ) {
 						String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(period, year);
 						if (invalidTimeHorizonPeriods.contains(timeHorizCoord)) {
 							coordToLockList.add(PafBaseConstants.SYNTHETIC_YEAR_ROOT_ALIAS + " / " + period);
 							tuplesToLock.add(viewTuple);
 						}
 						else {
 							bFoundValidTimeHorizPeriod = true;
 						}
 					}
 				}
 			}	
 		}
 		// -- Lastly, remove any filtered tuples
 		if( ! bFoundValidTimeHorizPeriod  && expandedRowTuples.size() != 0 && tuplesToLock.size() + blankTupleList.size() == expandedRowTuples.size() ) {
 			String errMsg = "The view can not be displayed since all the selected Year/Time combinations are invalid: "
 					+ StringUtils.arrayListToString(coordToLockList);
 			throw new PafException(errMsg, PafErrSeverity.Error);
 		}
 		
 		//reset collection and reinitialize
 		tuplesToLock.clear();
 		coordToLockList.clear();
 		blankTupleList.clear();
 		bFoundValidTimeHorizPeriod = false;
 		if ( bTimeOnCol && bYearOnRow && expandedColTuples.size() > 0  && expandedRowTuples.size() > 0 ) {
 			for (ViewTuple viewTuple:expandedColTuples) {
 			// Initialization
 				String[] memberArray = viewTuple.getMemberDefs();
 	
 				// If any tuple member is set to PAFBLANK, set the remaining members to PAFBLANK as well
 				if (pafDataService.isBlankViewTuple(viewTuple)) {
 					for (int i = 0; i < memberArray.length; i++) {
 						memberArray[i] = PafBaseConstants.PAF_BLANK;
 					}
 					viewTuple.setMemberDefs(memberArray);
 					blankTupleList.add(viewTuple);
 					continue;
 				}
 				// Compile a list of any tuples with invalid time / year combinations (TTN-1858 / TTN-1886).
 				if (bDoTimeHorizValidation) {
 					period = memberArray[timeAxis];
 					year = expandedRowTuples.get(0).getMemberDefs()[yearAxis];
 					if( ! period.isEmpty() && ! year.isEmpty() ) {
 						String timeHorizCoord = TimeSlice.buildTimeHorizonCoord(period, year);
 						if (invalidTimeHorizonPeriods.contains(timeHorizCoord)) {
 							coordToLockList.add(PafBaseConstants.SYNTHETIC_YEAR_ROOT_ALIAS + " / " + period);
 							tuplesToLock.add(viewTuple);
 						}
 						else {
 							bFoundValidTimeHorizPeriod = true;
 						}
 					}
 				}			
 			}	
 		}
 		if( ! bFoundValidTimeHorizPeriod  && expandedColTuples.size() != 0 && tuplesToLock.size() + blankTupleList.size()  == expandedColTuples.size() ) {
 			String errMsg = "The view can not be displayed since all the selected Year/Time combinations are invalid: "
 					+ StringUtils.arrayListToString(coordToLockList);
 			throw new PafException(errMsg, PafErrSeverity.Error);
 		}
 	}
 
 	/**
 	 * Process each attribute base dimension for invalid attribute combinations.
 	 * @param viewSection View section
 	 * @param clientState Client state
 	 */
 	public void processInvalidAttrTuples(PafViewSection viewSection, PafClientState clientState){
 		
 		PafAxis baseDimAxis;
 		List<String> viewSectionAttrDims;
 		Set<String> allPossibleAttrDims;
 		List<String> attrDims;
 		int primaryAttrAxis;
 		Map<String, List<String>> crossAttrBaseDims = new HashMap<String, List<String>>();
 		boolean isCrossAttrBaseDim = false;
 		
 		//Skip processing if the View Section has no attribute dimensions
 		if (viewSection.getAttributeDims() == null){
 			return;
 		}
 
 		PafDataService dataService = PafDataService.getInstance();
 		
 		//Get all the view section attribute dimensions
 		viewSectionAttrDims = Arrays.asList(viewSection.getAttributeDims());
 
 		//Get all possible base dimension with attributes
 		Set<String> baseDimsWithAttributes = dataService.getBaseDimNamesWithAttributes();
 		
 		for(String baseDim : baseDimsWithAttributes){
 
 			//Get the View Section Axis for the base dimension
 			baseDimAxis = viewSection.getAxis(baseDim);
 
 			//Get all possible attributes for the base dimension
 			allPossibleAttrDims = dataService.getBaseTree(baseDim).getAttributeDimNames();
 
 			//Determine the view section attribute dimensions for the base dimension
 			attrDims = new ArrayList<String>();
 			for(String attrDim : allPossibleAttrDims){
 				if(viewSectionAttrDims.contains(attrDim)){
 					attrDims.add(attrDim);
 				}
 			}
 
 			// Skip this base dimension if it has no attributes on the view
 			if (attrDims.size() == 0) {
 				continue;
 			}
 			
 			//Tuples can only be removed if the base and attribute dimensions are on:
 			//1) The page axis and either the column or row axis
 			//2) Either the column or row axis exclusively
 			primaryAttrAxis = baseDimAxis.getValue();
 			isCrossAttrBaseDim = false;
 			for(String attrDim : attrDims){
 				//Determine if attributes are on the row axis, column axis or both
 				if(viewSection.getAxis(attrDim).getValue() != PafAxis.PAGE){
 					if (primaryAttrAxis == PafAxis.PAGE){
 						primaryAttrAxis = viewSection.getAxis(attrDim).getValue();
 					}
 
 					if (viewSection.getAxis(attrDim).getValue() != primaryAttrAxis){
 						//The base dimensions with attributes on both the row and column will be processed later
 						crossAttrBaseDims.put(baseDim, attrDims);
 						isCrossAttrBaseDim = true;
 						continue;	
 					}
 				}
 			}
 			
 			removeInvalidAttrTuples(viewSection, baseDim, attrDims, baseDimAxis, primaryAttrAxis, isCrossAttrBaseDim, clientState.getUowTrees());
 		}
 		
 		//Now, lock the invalid attribute intersections for the base dimensions with attributes on both the 
 		//row and column axi.
 		if (!crossAttrBaseDims.isEmpty()){
 			lockInvalidAttributeIntersections(viewSection, crossAttrBaseDims, clientState.getUowTrees());
 		}
 	}	
 
 	/**
 	 * Remove invalid attribute rows/cols from the row/col tuples arrays.
 	 *
 	 * @param viewSection View section
 	 * @param baseDim Base dimension name
 	 * @param attrDims List of attribute dimensions on view
 	 * @param baseDimAxis
 	 * @param primaryAttrAxis
 	 * @param isCrossAttrBaseDim
 	 * @param uowTrees Collection of uow cache trees
 	 */
 	private void removeInvalidAttrTuples(PafViewSection viewSection, String baseDim, List<String> attrDims, 
 			PafAxis baseDimAxis, int primaryAttrAxis, boolean isCrossAttrBaseDim, MemberTreeSet uowTrees){
 		ViewTuple[] tuples = null;
 		String [] axisDims = null;
 		List<String> attrPageDimNames = new ArrayList<String>();
 		List<String> attrPageMembersInTuple = new ArrayList<String>();
 		String baseDimName = "";
 		List<ViewTuple> filteredTuples;
 		
 		PafDataService dataService = PafDataService.getInstance();
 
 		//Cycle through all the view section dimensions on the page dimension axis
 		for (PageTuple pTuple : viewSection.getPageTuples()){
 
 			//If a view section dimension is an attribute dimension
 			if (attrDims.contains(pTuple.getAxis())){
 				attrPageDimNames.add(pTuple.getAxis());
 				attrPageMembersInTuple.add(pTuple.getMember());
 			}
 
 			//If a view section dimension is the base dimension
 			if (baseDimAxis.getValue() == PafAxis.PAGE){
 				if (pTuple.getAxis().equals(baseDim)){
 					baseDimName = pTuple.getMember();
 				}
 			}
 		}
 
 		//Get the view section tuples and dimensions for the base dimension axis
 		if(baseDimAxis.getValue() == PafAxis.PAGE && isCrossAttrBaseDim == true){
 			//Row
 			tuples = viewSection.getRowTuples();
 			axisDims = viewSection.getRowAxisDims();
 			filteredTuples = FilterTuples(viewSection, baseDim, attrDims, baseDimAxis, tuples, axisDims, baseDimName, 
 					attrPageDimNames, attrPageMembersInTuple, uowTrees);
 			viewSection.setRowTuples(filteredTuples.toArray(new ViewTuple[0]));
 			
 			//Column
 			tuples = viewSection.getColTuples();
 			axisDims = viewSection.getColAxisDims();
 			filteredTuples = FilterTuples(viewSection, baseDim, attrDims, baseDimAxis, tuples, axisDims, baseDimName, 
 					attrPageDimNames, attrPageMembersInTuple, uowTrees);
 			viewSection.setColTuples(filteredTuples.toArray(new ViewTuple[0]));
 		}
 		else if(baseDimAxis.getValue() == PafAxis.ROW ||
 				(baseDimAxis.getValue() == PafAxis.PAGE && primaryAttrAxis == PafAxis.ROW)){
 			tuples = viewSection.getRowTuples();
 			axisDims = viewSection.getRowAxisDims();
 			filteredTuples = FilterTuples(viewSection, baseDim, attrDims, baseDimAxis, tuples, axisDims, baseDimName, 
 					attrPageDimNames, attrPageMembersInTuple, uowTrees);
 			viewSection.setRowTuples(filteredTuples.toArray(new ViewTuple[0]));
 		}
 		else if(baseDimAxis.getValue() == PafAxis.COL ||
 				(baseDimAxis.getValue() == PafAxis.PAGE && primaryAttrAxis == PafAxis.COL)){
 			tuples = viewSection.getColTuples();
 			axisDims = viewSection.getColAxisDims();
 			filteredTuples = FilterTuples(viewSection, baseDim, attrDims, baseDimAxis, tuples, axisDims, baseDimName, 
 					attrPageDimNames, attrPageMembersInTuple, uowTrees);
 			viewSection.setColTuples(filteredTuples.toArray(new ViewTuple[0]));
 		}
 		//If the base dim and attributes are all on the page axis and if the attribute interesection is
 		//invalid, then the entire view section is invalid.
 		else if(baseDimAxis.getValue() == PafAxis.PAGE && primaryAttrAxis == PafAxis.PAGE){
 			if(!attrPageDimNames.isEmpty()){
 				if (!AttributeUtil.isValidAttributeCombo(baseDim, baseDimName, attrPageDimNames.toArray(new String[0]),
 						attrPageMembersInTuple.toArray(new String[0]),uowTrees)){
 					viewSection.setRowTuples(new ViewTuple[0]);
 					viewSection.setColTuples(new ViewTuple[0]);
 				}
 			}
 		}
 		
 	}
 	 /**
 	  * Builds list of filtered tuples
 	  */
 	private List<ViewTuple> FilterTuples(PafViewSection viewSection, String baseDimName, List<String> attrDims, 
 			PafAxis baseDimAxis, ViewTuple[] tuples, String [] axisDims, String baseMemberName, 
 			List<String> attrPageDimNames, List<String> attrPageMembersInTuple, MemberTreeSet uowTrees){
 		List<String> attrMembers;
 		List<String> attrDimNames = new ArrayList<String>();
 		List<Integer> attrDimIndicesInTuple = new ArrayList<Integer>();
 		List<ViewTuple> filteredTuples = new ArrayList<ViewTuple>();
 		int i = 0;
 		Integer baseDimIndex = null;
 		int tupleBlankCount = 0;
 		
 		attrDimNames.addAll(attrPageDimNames);
 		
 		//Cycle through all the view section dimensions on either the row or column dimension axis
 		for (String dim : axisDims){
 
 			//If a view section dimension is an attribute dimension
 			if (attrDims.contains(dim)){
 				//List of attribute dim names
 				attrDimNames.add(dim);
 				//List of Tuple locations for dimension member names
 				attrDimIndicesInTuple.add(i);
 			}
 
 			//If a view section dimension is the base dimension
 			if (baseDimAxis.getValue() != PafAxis.PAGE){
 				if (dim.equals(baseDimName)){
 					//The Tuple location for the base dimension member name
 					baseDimIndex = i;
 				}
 			}
 			i++;
 		}
 		
 		Set<Intersection> validAttrCombos = null;
 		String[] attrDimNameArray = attrDimNames.toArray(new String[0]);
 		
 		if (baseDimAxis.getValue() == PafAxis.PAGE){
 			//Get the list of attribute intersections
 			validAttrCombos = AttributeUtil.getValidAttributeCombos(baseDimName, baseMemberName, attrDimNameArray, uowTrees);
 		}
 
 		//Get each view tuple
 		for(ViewTuple tuple : tuples){
 			
 			//If  tuple contains a pafblank or is a member tag, add it to the filtered list
 			if (tuple.containsPafBlank() || tuple.isMemberTag()){
 				filteredTuples.add(tuple);
 				tupleBlankCount++;
 				continue;
 			}
 			
 			// If the the current base dimension and corresponding attribute dimension(s)
 			// appear on opposing axes (row/col) AND no corresponding attribute dimension(s) 
 			// occur in the page or same axis, then add the tuple to the filtered list (TTN-1328).
 			if (attrDimNames.isEmpty()) {
 				filteredTuples.add(tuple);
 				continue;				
 			}
 			
 			//Get the list of attibute member names
 			attrMembers = new ArrayList<String>();
 			attrMembers.addAll(attrPageMembersInTuple);
 			for(int index : attrDimIndicesInTuple){
 				attrMembers.add( tuple.getMemberDefs()[index]);
 			}
 
 			//If the base dimension is not on the page then get it from the tuple
 			if (baseDimAxis.getValue() != PafAxis.PAGE){
 				baseMemberName = tuple.getMemberDefs()[baseDimIndex];
 				
 				//Get the list of valid attribute combinations
 				validAttrCombos = AttributeUtil.getValidAttributeCombos(baseDimName, baseMemberName, attrDimNameArray, uowTrees);
 			}
 
 			//Add tuple to collection of filtered tuples, if the attribute combination is valid
 			Intersection attrCombo = new Intersection(attrDimNameArray, attrMembers.toArray(new String[0]));
 			if (validAttrCombos.contains(attrCombo)) {
 				filteredTuples.add(tuple);
 			}
 		}
 		
 		//If the filtered list is all blanks then return an empty list
 		if(filteredTuples.size() == tupleBlankCount){
 			return new ArrayList<ViewTuple>();
 		}
 		
 		return filteredTuples;
 	}
 	
 	/**
 	 * Lock the invalid attribute intersections.
 	 */
 	private void lockInvalidAttributeIntersections(PafViewSection viewSection, Map<String, List<String>> crossAttrBaseDims, MemberTreeSet uowTrees){
 		PafAxis baseDimAxis;
 		List<String> attrDimNames;
 		List<Integer> attrRowDimIndicesInTuple;
 		List<Integer> attrColDimIndicesInTuple;
 		List<String> attrPageMembersInTuple;
 		List<String> attrMembers;
 		int i;
 		Integer baseDimIndex;
 		String baseDimName;
 		int rowId;
 		int colId;
 		int rowMemberIndex;
 		int colMemberIndex;
 		List<String> rowMembers;
 		List<String> colMembers;
 		int coordMemberIndex;
 		Set<Intersection> validAttributeCombos = null;
 		int rowTupleBlankCount = 0;
 		int colTupleBlankCount = 0;
 		boolean isFirstPass = true;
 		
 		Set<LockedCell> invalidCells = new LinkedHashSet<LockedCell>();
 		Set<Intersection> invalidIntersections = new HashSet<Intersection>();
 		Map<Integer, Set<Intersection>> rowCache = new HashMap<Integer, Set<Intersection>>();
 		Map<Integer, Set<Intersection>> colCache = new HashMap<Integer, Set<Intersection>>();
 	
 
 		for(String baseDim: crossAttrBaseDims.keySet()){
 			attrDimNames = new ArrayList<String>();
 			attrRowDimIndicesInTuple = new ArrayList<Integer>();
 			attrColDimIndicesInTuple = new ArrayList<Integer>();
 			attrPageMembersInTuple = new ArrayList<String>();
 			baseDimIndex = null;
 			baseDimName = "";
 
 			//Get the View Section Axis for the base dimension
 			baseDimAxis = viewSection.getAxis(baseDim);
 
 			//Cycle through all the view section dimensions on the page dimension axis
 			for (PageTuple pTuple : viewSection.getPageTuples()){
 
 				//If a view section dimension is an attribute dimension
 				if (crossAttrBaseDims.get(baseDim).contains(pTuple.getAxis())){
 					attrDimNames.add(pTuple.getAxis());
 					attrPageMembersInTuple.add(pTuple.getMember());
 				}
 
 				//If a view section dimension is the base dimension
 				if (baseDimAxis.getValue() == PafAxis.PAGE){
 					if (pTuple.getAxis().equals(baseDim)){
 						baseDimName = pTuple.getMember();
 					}
 				}
 			}
 
 			//Get the view section tuples and dimensions for the base dimension axis:
 			i = 0;
 			//Cycle through all the view section dimensions on the row dimension axis
 			for (String dim : viewSection.getRowAxisDims()){
 
 				//If a view section dimension is an attribute dimension
 				if (crossAttrBaseDims.get(baseDim).contains(dim)){
 					//List of attribute dim names
 					attrDimNames.add(dim);
 					//List of Tuple locations for dimension member names
 					attrRowDimIndicesInTuple.add(i);
 				}
 
 				//If a view section dimension is the base dimension
 				if (baseDimAxis.getValue() == PafAxis.ROW){
 					if (dim.equals(baseDim)){
 						//The Tuple location for the base dimension member name
 						baseDimIndex = i;
 					}
 				}
 				i++;
 			}
 
 			i = 0;
 			//Cycle through all the view section dimensions on the column dimension axis
 			for (String dim : viewSection.getColAxisDims()){
 
 				//If a view section dimension is an attribute dimension
 				if (crossAttrBaseDims.get(baseDim).contains(dim)){
 					//List of attribute dim names
 					attrDimNames.add(dim);
 					//List of Tuple locations for dimension member names
 					attrColDimIndicesInTuple.add(i);
 				}
 
 				//If a view section dimension is the base dimension
 				if (baseDimAxis.getValue() == PafAxis.COL){
 					if (dim.equals(baseDim)){
 						//The Tuple location for the base dimension member name
 						baseDimIndex = i;
 					}
 				}
 				i++;
 			}
 			String[] attrDimNameArray = attrDimNames.toArray(new String[0]);
 						
 			//Store dimension/member combinations
 			Map<String, String> mappedDimsWithMembers = new HashMap<String, String>();
 			
 			//Add page dims/members to intermediate structure to build an intersection object
 			for (PageTuple pageTuple: viewSection.getPageTuples()){
 				mappedDimsWithMembers.put(pageTuple.getAxis(), pageTuple.getMember());
 			}
 						
 			rowId = 0;
 			
 			if (baseDimAxis.getValue() == PafAxis.PAGE){
 				//Get the list of attribute intersections
 				validAttributeCombos = AttributeUtil.getValidAttributeCombos(baseDim, baseDimName, attrDimNameArray, uowTrees);
 			}
 			
 			//Get each view tuple
 			for(ViewTuple rowTuple : viewSection.getRowTuples()){
 				rowId++;  //1 based
 				
 				//If  row is pafblank, process the next row
 				if (rowTuple.containsPafBlank()){
 					if (isFirstPass){
 						rowTupleBlankCount++;
 					}
 					continue;
 				}
 				
 				// If row tuple is a member tag, process the next row
 				if (rowTuple.isMemberTag()) {
 					continue;
 				}
 				
 				if(!rowCache.containsKey(rowId)){
 					rowCache.put(rowId, new HashSet<Intersection>());
 				}
 				
 				//Get the base dimension member name if the base dimension axis is the row axis
 				if (baseDimAxis.getValue() == PafAxis.ROW){
 					baseDimName = rowTuple.getMemberDefs()[baseDimIndex];
 					
 					//Get the list of attribute intersections
 					validAttributeCombos = AttributeUtil.getValidAttributeCombos(baseDim, baseDimName, attrDimNameArray, uowTrees);
 				}
 				
 				rowMembers = getTupleMemberDefs(rowTuple);
 				
 				rowMemberIndex = 0;
 				
 				//Add row dims/members to intermediate structure to build an intersection object
 				for (String dimension : viewSection.getRowAxisDims()){
 					mappedDimsWithMembers.put(dimension, rowMembers.get(rowMemberIndex++));
 				}
 				
 				colId = 0;
 				
 				for(ViewTuple colTuple : viewSection.getColTuples()){
 					colId++;  //1 based
 					
 					//If column is pafblank, process the next column
 					if (colTuple.containsPafBlank()){
 						if (isFirstPass){
 							colTupleBlankCount++;
 						}
 						continue;
 					}
 					
 					// If column tuple is a member tag, process the next column
 					if (colTuple.isMemberTag()) {
 						continue;
 					}
 					
 					if(!colCache.containsKey(colId)){
 						colCache.put(colId, new HashSet<Intersection>());
 					}
 					
 					//Get the base dimension member name if the base dimension axis is the column axis
 					if (baseDimAxis.getValue() == PafAxis.COL){
 						baseDimName = colTuple.getMemberDefs()[baseDimIndex];
 						
 						//Get the list of attribute intersections
 						validAttributeCombos = AttributeUtil.getValidAttributeCombos(baseDim, baseDimName, attrDimNameArray, uowTrees);
 					}
 					
 					colMembers = getTupleMemberDefs(colTuple);
 					
 					colMemberIndex = 0;
 					
 					//Add Column dims/members to intermediate structure to build an intersection object
 					for (String dimension : viewSection.getColAxisDims()){
 						mappedDimsWithMembers.put(dimension, colMembers.get(colMemberIndex++));
 					}
 					
 					//String array used to instantiate an intersection object
 					String[] coordMemberOrder = new String[viewSection.getDimensionsPriority().length];
 					
 					coordMemberIndex = 0;
 					
 					//populate the members in priority order
 					for (String dimension : viewSection.getDimensionsPriority()){
 						coordMemberOrder[coordMemberIndex++] = mappedDimsWithMembers.get(dimension);
 					}
 					
 					//Get the list of attribute member names:
 					attrMembers = new ArrayList<String>();
 
 					//Page members
 					attrMembers.addAll(attrPageMembersInTuple);
 
 					//Row members
 					for(int index : attrRowDimIndicesInTuple){
 						attrMembers.add( rowTuple.getMemberDefs()[index]);
 					}
 
 					//Column members
 					for(int index : attrColDimIndicesInTuple){
 						attrMembers.add( colTuple.getMemberDefs()[index]);
 					}
 
 					//If the attribute intersection valid, add it to the invalid intersections collection
 					Intersection attributeCombo = new Intersection(attrDimNameArray, attrMembers.toArray(new String[0]));
 					if (!validAttributeCombos.contains(attributeCombo)) {
 
 						LockedCell invalidCell = new LockedCell(rowId, colId);
 						invalidCells.add(invalidCell);
 
 						Intersection invalidIntersection = new Intersection(viewSection.getDimensionsPriority(),
 								coordMemberOrder);
 						invalidIntersections.add(invalidIntersection);
 						
 						rowCache.get(rowId).add(invalidIntersection);
 						colCache.get(colId).add(invalidIntersection);
 					}
 				}
 			}
 			if(isFirstPass == true){
 				isFirstPass = false;
 			}
 		}
 		
 		Set<LockedCell> filteredInvalidCells = new LinkedHashSet<LockedCell>();
 		List<Integer> rowTupleIndicesToRemove;
 		List<Integer> colTupleIndicesToRemove;
 		int rowCount;
 		int colCount;
 		List<ViewTuple> filteredRowTuples;
 		List<ViewTuple> filteredColTuples;
 		int adjustRowCount;
 		int adjustColCount;
 		
 		//Rows to remove
 		rowTupleIndicesToRemove = new ArrayList<Integer>();
 		for (Integer row : rowCache.keySet())
 		{
 			if(rowCache.get(row).size() == viewSection.getColTuples().length - colTupleBlankCount)
 			{
 				invalidIntersections.removeAll(rowCache.get(row));
 				rowTupleIndicesToRemove.add(row); 
 			}
 			
 		}
 		
 		//Columns to remove
 		colTupleIndicesToRemove = new ArrayList<Integer>();
 		for (Integer col : colCache.keySet()){
 			if(colCache.get(col).size() == viewSection.getRowTuples().length - rowTupleBlankCount)
 			{
 				invalidIntersections.removeAll(colCache.get(col));
 				colTupleIndicesToRemove.add(col); 
 			}
 		}
 		
 		//Filter and adjust invalid cells
 		if (!rowTupleIndicesToRemove.isEmpty() || !colTupleIndicesToRemove.isEmpty()){
 			
 			for (LockedCell lCell : invalidCells){
 
 				if (!rowTupleIndicesToRemove.contains(lCell.getRowIndex()) && 
 						!colTupleIndicesToRemove.contains(lCell.getColIndex())){
 					
 					adjustRowCount = 0;
 					for (Integer rowTupleIndex : rowTupleIndicesToRemove)
 					{
 						if (rowTupleIndex < lCell.getRowIndex()){
 							adjustRowCount++;
 						}
 					}
 					
 					adjustColCount = 0;
 					for (Integer colTupleIndex : colTupleIndicesToRemove)
 					{
 						if (colTupleIndex < lCell.getColIndex()){
 							adjustColCount++;
 						}
 					}
 					
 					lCell.setRowIndex(lCell.getRowIndex() - adjustRowCount);
 					lCell.setColIndex(lCell.getColIndex() - adjustColCount);
 					filteredInvalidCells.add(lCell);
 				}
 			}
 		}
 		
 		if (!rowTupleIndicesToRemove.isEmpty()){
 			rowCount = 0;
 			filteredRowTuples = new ArrayList<ViewTuple>();
 			for(ViewTuple rTuple : viewSection.getRowTuples()){
 				rowCount++;
 				
 				if(!rowTupleIndicesToRemove.contains(rowCount)){
 					filteredRowTuples.add(rTuple);
 				}
 			}
 			viewSection.setRowTuples(filteredRowTuples.toArray(new ViewTuple[0]));
 		}
 
 
 		if (!colTupleIndicesToRemove.isEmpty()){
 			colCount = 0;
 			filteredColTuples = new ArrayList<ViewTuple>();
 			for(ViewTuple cTuple : viewSection.getColTuples()){
 				colCount++;
 
 				if(!colTupleIndicesToRemove.contains(colCount)){
 					filteredColTuples.add(cTuple);
 				}
 			}
 			viewSection.setColTuples(filteredColTuples.toArray(new ViewTuple[0]));
 		}
 
 		if (!rowTupleIndicesToRemove.isEmpty() || !colTupleIndicesToRemove.isEmpty()){
 			viewSection.setInvalidAttrIntersectionsLC(filteredInvalidCells.toArray(new LockedCell[0]));
 		}
 		else{
 			viewSection.setInvalidAttrIntersectionsLC(invalidCells.toArray(new LockedCell[0]));
 		}
 
 		viewSection.invalidAttrIntersections(invalidIntersections);	
 	}
 	
 	
 	/**
 	 * Suppress zero rows and zero columns in the data slice
 	 * 
 	 * @param viewSection The View Section
 	 * @param suppressedRows The collection of row numbers from the data slice that need to be zeroed
 	 * @param suppressedColumns The collection of column numbers from the data slice that need to be zeroed
 	 *
 	 */
 	private void suppressZeros(PafViewSection viewSection, LinkedHashSet<Integer> suppressedRows, LinkedHashSet<Integer> suppressedColumns ){
 		
 		PafDataSlice dataSlice = viewSection.getPafDataSlice();
 		boolean areRowsSuppressed = viewSection.getSuppressZeroSettings().getRowsSuppressed();
 		boolean areColumnsSuppressed = viewSection.getSuppressZeroSettings().getColumnsSuppressed();
 		boolean zeroFlag;
 		int rowCount = dataSlice.getRowCount();
 		int columnCount = dataSlice.getColumnCount();
 		int k;
 		
 		k = 0;  //Dataslice count
 		List<Integer> columnsWithZeros = new ArrayList<Integer>();
 	
 		//get the rows and columns to suppress
 		for(int i = 0; i < rowCount; i++){
 			zeroFlag = true;
 			for(int j = 0; j < columnCount; j++){
 				if (areRowsSuppressed == true && dataSlice.getData()[k] != 0){
 					zeroFlag = false;
 				}else if(areColumnsSuppressed == true && dataSlice.getData()[k] == 0){ 
 					if(i == 0){  // get the row 0 columns with a zero data value
 						suppressedColumns.add(j);
 					}
 					columnsWithZeros.add(j);
 				}
 				k++;
 			}
 			
 			if (areRowsSuppressed == true){
 				if (zeroFlag == true){
 					suppressedRows.add(i);
 				}
 			}
 			
 			if(areColumnsSuppressed == true){
 				suppressedColumns.retainAll(columnsWithZeros);
 				columnsWithZeros.clear();
 			}
 		}
 		
 		int arrayLength;
 		double[] data;
 		int l;
 	
 		//suppress rows - the suppressedRows collection contains the row numbers in the data slice to suppress
 		//The new data array does not contain the suppressed rows
 		if (areRowsSuppressed == true){
 			rowCount = rowCount - suppressedRows.size();
 			arrayLength = rowCount * columnCount;
 			
 			k = 0;  //Dataslice count
 			l = 0;  //New data array count
 			data = new double[arrayLength];
 	
 			for(int i =0; i < dataSlice.getRowCount(); i++){ //got to use original row count
 				for(int j = 0; j < columnCount; j++){
 	
 					if (! suppressedRows.contains(i)){
 						data[l++] = (dataSlice.getData()[k]);
 						k++;
 					}else{//skip row
 						k = k + columnCount;
 						break;
 					}
 				}
 			}
 			dataSlice.setData(data.clone());
 		}
 		
 		//suppress columns - the suppressedColumns collection contains the column numbers in the data slice to suppress
 		//The new data array does not contain the suppressed columns
 		//Note - Columns are suppressed after rows
 		if(areColumnsSuppressed == true){
 			columnCount = columnCount - suppressedColumns.size();
 			arrayLength = rowCount * columnCount;
 		
 			k = 0;  //Dataslice count
 			l = 0;  //New data array count
 			data = new double[arrayLength];
 
 			for(int i =0; i < rowCount; i++){
 				for(int j = 0; j < dataSlice.getColumnCount(); j++){ //got to use original column count
 
 					if (! suppressedColumns.contains(j)){
 						data[l++] = (dataSlice.getData()[k]);
 					}
 					k++;
 				}
 			}
 			dataSlice.setData(data.clone());
 
 			//if you don't have any rows, then you can't you have any columns
 			if(rowCount == 0){
 				columnCount = 0;
 			}
 			dataSlice.setColumnCount(columnCount);
 
 		}
 		
 	}
  
 	/**
 	 * Suppress zero rows and zero columns in the row and column tuples
 	 * 
 	 * @param viewSection The View Section
 	 * @param suppressedRows The collection of row numbers from the data slice that need to be zeroed
 	 * @param suppressedColumns The collection of column numbers from the data slice that need to be zeroed
 	 *
 	 */
 	private void suppressTuples (PafViewSection viewSection, LinkedHashSet<Integer> suppressedRows, LinkedHashSet<Integer> suppressedColumns){
 		boolean areRowsSuppressed = viewSection.getSuppressZeroSettings().getRowsSuppressed();
 		boolean areColumnsSuppressed = viewSection.getSuppressZeroSettings().getColumnsSuppressed();
 		int k;
 		boolean prevBlankFlag = true;
 		
 		if(areRowsSuppressed == true){
 			List<ViewTuple> rowTuples = new ArrayList<ViewTuple>();  //the tuples after suppression
 			k = 0;
 			
 			outer:
 			for (ViewTuple rowTuple : viewSection.getRowTuples()){
 
 				//skip blanks
 				for (String member : rowTuple.getMemberDefs()){
 					if (member.equalsIgnoreCase(PafBaseConstants.PAF_BLANK)){
 						if (prevBlankFlag == false) // Only allow a single adjacent blank row after a suppression
 						{
 							rowTuples.add(rowTuple);
 							prevBlankFlag = true;
 						}
 						
 						continue outer;
 					}
 				}
 				
 				//skip member tags
 				if (rowTuple.isMemberTag() == true){
 					rowTuples.add(rowTuple);
 					prevBlankFlag = false;
 					continue outer;
 				}
 				
 				if (! suppressedRows.contains(k)){  //Add the tuple if it is not in the suppressed collection
 					rowTuples.add(rowTuple);
 					prevBlankFlag = false;
 				}
 				k++;
 			}
 			
 			//remove last row if it is a blank
 			int tupleSize = rowTuples.size();
 			if (tupleSize > 0 && prevBlankFlag == true){
 				rowTuples.remove(tupleSize - 1);
 			}
 			
 			viewSection.setRowTuples(rowTuples.toArray(new ViewTuple[0]));
 		}
 		
 		prevBlankFlag = true;
 		
 		if(areColumnsSuppressed == true){
 			List<ViewTuple> colTuples = new ArrayList<ViewTuple>();  //the tuples after suppression
 			k = 0;
 			
 			outer2:
 			for (ViewTuple colTuple : viewSection.getColTuples()){
 
 				//skip blanks
 				for (String member : colTuple.getMemberDefs()){
 					if (member.equalsIgnoreCase(PafBaseConstants.PAF_BLANK)){
 						if (prevBlankFlag == false)  // Only allow a single adjacent blank column after a suppression
 						{
 							colTuples.add(colTuple);
 							prevBlankFlag = true;
 						}
 						
 						continue outer2;
 					}
 				}
 				
 				//skip member tags
 				if (colTuple.isMemberTag() == true){
 					colTuples.add(colTuple);
 					prevBlankFlag = false;
 					continue outer2;
 				}
 				if (! suppressedColumns.contains(k)){
 					colTuples.add(colTuple);  //Add the tuple if it is not in the suppressed collection
 					prevBlankFlag = false;
 				}
 				k++;
 			}
 			
 			//remove last column if it is a blank
 			int tupleSize = colTuples.size();
 			if (tupleSize > 0 && prevBlankFlag == true){
 				colTuples.remove(tupleSize - 1);
 			}
 			
 			viewSection.setColTuples(colTuples.toArray(new ViewTuple[0]));
 		}	
 	}
 	
 	
 	/**
 	 * Determines whether or not the view has changed due to suppression on an evaluate
 	 * 
 	 * @param viewSection The View Section
 	 * @param sliceParms The view section Slice Parms
 	 *
 	 */
 	private boolean suppressedViewIsChanged(PafViewSection viewSection, PafDataSliceParms sliceParms){
 						
 		if (sliceParms != null){
 			
 			int nonDataRowCount = 0;
 
 			String[][] rows = sliceParms.getRowTuples();
 			int k = 0;  //row counter
 			outer:
 			for(ViewTuple tuple : viewSection.getRowTuples()){
 
 				//skip member tags
 				if (tuple.isMemberTag() == true){
 					nonDataRowCount++;
 					continue outer;
 				}
 				
 				int l = 0;
 				String[] members = tuple.getMemberDefs();
 				for(String member : members){
 					//skip blanks
 					if (member.equalsIgnoreCase(PafBaseConstants.PAF_BLANK)){
 						nonDataRowCount++;
 						continue outer;
 					}
 					
 					//There are no members in the previous viewSection tuples - but there are valid tuples now
 					if ((rows.length == 0) || (rows.length == 1 && rows[0].length == 0)){
 						return true;
 					}
 					
 					if (! member.equalsIgnoreCase(rows[k][l++])){
 						return true;  //There is a difference between the prev and new view on the rows
 					}
 				}
 				k++;
 			}
 			
 			int nonDataColCount = 0;
 			
 			String[][] cols = sliceParms.getColTuples();
 			k = 0;  //column counter
 			outer:
 			for(ViewTuple tuple : viewSection.getColTuples()){
 
 				//skip member tags
 				if (tuple.isMemberTag() == true){
 					nonDataColCount++;
 					continue outer;
 				}
 				
 				int l = 0;
 				String[] members = tuple.getMemberDefs();
 				for(String member : members){
 					//skip blanks
 					if (member.equalsIgnoreCase(PafBaseConstants.PAF_BLANK)){
 						nonDataColCount++;
 						continue outer;
 					}
 					
 					//There are no members in the previous viewSection Tuples - but there are valid tuples now
 					if ((cols.length == 0) || (cols.length == 1 && cols[0].length == 0)){
 						return true;
 					}
 
 					// slipped out of bounds indicating that the previous view had more columns
 					// therefore return true for a change indicated.
 					// TTN - 1346
 					if (k > cols.length - 1) {
 						return true;
 					}
 					
 					if (! member.equalsIgnoreCase(cols[k][l++])){
 						return true;  //There is a difference between the prev and new view on the columns
 					}
 				}
 				k++;
 			}
 			
 			//If all data rows or column have been removed from the view then the view has changed
 			if(viewSection.getRowTuples().length == nonDataRowCount || viewSection.getColTuples().length == nonDataColCount){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 		
 }
