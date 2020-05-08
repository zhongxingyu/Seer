 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity
 /*******************************************************************************
  * Copyright (c) 2010, 2012 Ericsson AB and others.
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class defines various constants used in the R4E UI plugin
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   Jacques Bouthillier - Add definition for Report
  *   
  ******************************************************************************/
 package org.eclipse.mylyn.reviews.r4e.ui.internal.utils;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.TimeZone;
 
 /**
  * @author Sebastien Dubois
  * @authot Jacques Bouthillier
  * @version $Revision: 1.0 $
  */
 public class R4EUIConstants { // $codepro.audit.disable convertClassToInterface
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 
 	/**
 	 * Field TIME_ZONE_OFFSET. (value is "TimeZone.getDefault().getOffset(System.currentTimeMillis()")
 	 */
 	public final static long TIME_ZONE_OFFSET = TimeZone.getDefault().getOffset(System.currentTimeMillis());
 
 	/**
 	 * Field R4E_CONTEXT_ID. (value is ""org.eclipse.mylyn.reviews.r4e.ui.context"")
 	 */
 	public static final String R4E_CONTEXT_ID = "org.eclipse.mylyn.reviews.r4e.ui.context";
 
 	/**
 	 * Field EXCEPTION_MSG. (value is ""Exception: "")
 	 */
 	public static final String EXCEPTION_MSG = "Exception: ";
 
 	/**
 	 * Field CANCEL_EXCEPTION_MSG. (value is ""Operation cancelled by User"")
 	 */
 	public static final String CANCEL_EXCEPTION_MSG = "Operation cancelled by User";
 
 	/**
 	 * Field VERSION_APPLICATION_OLDER. (value is -1)
 	 */
 	public static final int VERSION_APPLICATION_OLDER = -1;
 
 	/**
 	 * Field VERSION_APPLICATION_NEWER. (value is 1)
 	 */
 	public static final int VERSION_APPLICATION_NEWER = 1;
 
 	/**
 	 * Field SEPARATOR. (value is ""/"")
 	 */
 	public static final String SEPARATOR = "/";
 
 	/**
 	 * Field LINE_FEED. (value is "\n" or "\r\n") Depends on operating system
 	 */
 	public static final String LINE_FEED = System.getProperty("line.separator");
 
 	/**
 	 * Field TAB_FEED. (value is ""\t"")
 	 */
 	public static final String TAB_FEED = "\t";
 
 	/**
 	 * Field LIST_SEPARATOR. (value is "";"")
 	 */
 	public static final String LIST_SEPARATOR = ";";
 
 	/**
 	 * Field ELLIPSIS_STR. (value is ""..."")
 	 */
 	public static final String ELLIPSIS_STR = "...";
 
 	/**
 	 * Field R4E_TEMP_PROJECT. (value is ""R4ETemp"")
 	 */
 	public static final String R4E_TEMP_PROJECT = "R4ETemp";
 
 	/**
 	 * Field R4E_TEMP_FOLDER. (value is ""temp"")
 	 */
 	public static final String R4E_TEMP_FOLDER = "temp";
 
 	/**
 	 * Field GROUP_FILE_SUFFIX. (value is ""_group_root.xrer"")
 	 */
 	public static final String GROUP_FILE_SUFFIX = "_group_root.xrer";
 
 	/**
 	 * Field RULE_SET_FILE_SUFFIX. (value is ""_rule_set.xrer"")
 	 */
 	public static final String RULE_SET_FILE_SUFFIX = "_rule_set.xrer";
 
 	/**
 	 * Field VALUE_TRUE_STR. (value is ""true"")
 	 */
 	public static final String VALUE_TRUE_STR = "true";
 
 	/**
 	 * Field NO_OFFSET. (value is 0)
 	 */
 	public static final int NO_OFFSET = 0;
 
 	/**
 	 * Field LINE_OFFSET. (value is 1)
 	 */
 	public static final int LINE_OFFSET = 1;
 
 	/**
 	 * Field ANNOTATION_TOOLBAR. (value is ""annotationToolbar"")
 	 */
 	public static final String ANNOTATION_TOOLBAR = "ANNOTATION_TOOLBAR";
 
 	/**
 	 * Field REVIEW_GROUP_PATHS_LENGTH. (value is 128)
 	 */
 	public static final int REVIEW_GROUP_PATHS_LENGTH = 128;
 
 	/**
 	 * Field TOOLTIP_CONTENTS_LENGTH. (value is 25)
 	 */
 	public static final int TOOLTIP_CONTENTS_LENGTH = 25;
 
 	/**
 	 * Field INVALID_VALUE. (value is -1)
 	 */
 	public static final int INVALID_VALUE = -1;
 
 	/**
 	 * Field SELECTIONS_LABEL. (value is ""Selections"")
 	 */
 	public static final String SELECTIONS_LABEL = "Selections";
 
 	/**
 	 * Field DELTAS_LABEL. (value is ""Deltas"")
 	 */
 	public static final String DELTAS_LABEL = "Deltas";
 
 	/**
 	 * Field ANOMALIES_LABEL_NAME. (value is ""Anomalies"")
 	 */
 	public static final String ANOMALIES_LABEL = "Anomalies";
 
 	/**
 	 * Field GLOBAL_ANOMALIES_LABEL_NAME. (value is ""Global Anomalies"")
 	 */
 	public static final String GLOBAL_ANOMALIES_LABEL = "Global Anomalies";
 
 	/**
 	 * Field GLOBAL_ANOMALIES_LABEL_NAME. (value is ""Global Postponed Anomalies"")
 	 */
 	public static final String GLOBAL_POSTPONED_ANOMALIES_LABEL = "Global Postponed Anomalies";
 
 	/**
 	 * Field ELEMENTS_LABEL_NAME. (value is ""Elements"")
 	 */
 	public static final String ELEMENTS_LABEL_NAME = "Elements";
 
 	/**
 	 * Field POSTPONED_ELEMENTS_LABEL_NAME. (value is ""Imported Anomalies"")
 	 */
 	public static final String IMPORTED_ANOMALIES_LABEL_NAME = "Imported Anomalies";
 
 	/**
 	 * Field PARTICIPANTS_LABEL_NAME. (value is ""Participants"")
 	 */
 	public static final String PARTICIPANTS_LABEL = "Participants";
 
 	/**
 	 * Field CREATE_LABEL. (value is ""Create..."")
 	 */
 	public static final String CREATE_LABEL = "Create...";
 
 	/**
 	 * Field UPDATE_LABEL. (value is ""Update..."")
 	 */
 	public static final String UPDATE_LABEL = "Update...";
 
 	/**
 	 * Field CLEAR_LABEL. (value is ""Clear"")
 	 */
 	public static final String CLEAR_LABEL = "Clear";
 
 	/**
 	 * Field ADD_LABEL. (value is ""Add..."")
 	 */
 	public static final String ADD_LABEL = "Add...";
 
 	/**
 	 * Field REMOVE_LABEL. (value is ""Remove..."")
 	 */
 	public static final String REMOVE_LABEL = "Remove...";
 
 	/**
 	 * Field REFRESH_LABEL. (value is ""Refresh"")
 	 */
 	public static final String REFRESH_LABEL = "Refresh";
 
 	/**
 	 * Field SYNC_LABEL. (value is ""Sync"")
 	 */
 	public static final String SYNC_LABEL = "Sync";
 
 	/**
 	 * Field LINE_TAG. (value is ""Line "")
 	 */
 	public static final String LINE_TAG = "Line ";
 
 	/**
 	 * Field LINES_TAG. (value is ""Lines "")
 	 */
 	public static final String LINES_TAG = "Lines ";
 
 	/**
 	 * Field DEFAULT_LINE_TAG_LENGTH. (value is 28)
 	 */
 	public static final int DEFAULT_LINE_TAG_LENGTH = 28;
 
 	/**
 	 * Field DIALOG_TITLE_ERROR. (value is ""R4E Error"")
 	 */
 	public static final String DIALOG_TITLE_ERROR = "R4E Error";
 
 	/**
 	 * Field DIALOG_TITLE_WARNING. (value is ""R4E Warning"")
 	 */
 	public static final String DIALOG_TITLE_WARNING = "R4E Warning";
 
 	/**
 	 * Field DIALOG_TITLE_INFO. (value is ""R4E Info"")
 	 */
 	public static final String DIALOG_TITLE_INFO = "R4E Info";
 
 	/**
 	 * Field REVIEW_NOT_COMPLETED_ERROR. (value is ""Review Error"")
 	 */
 	public static final String REVIEW_NOT_COMPLETED_ERROR = "Review Error";
 
 	/**
 	 * Field SHOW_DISABLED_FILTER_NAME. (value is ""Show disabled elements"")
 	 */
 	public static final String SHOW_DISABLED_FILTER_NAME = "Show Disabled Elements";
 
 	/**
 	 * Field DIALOG_DEFAULT_HEIGHT. (value is 500)
 	 */
 	public static final int DIALOG_DEFAULT_HEIGHT = 300;
 
 	/**
 	 * Field DIALOG_DEFAULT_WIDTH. (value is 500)
 	 */
 	public static final int DIALOG_DEFAULT_WIDTH = 400;
 
 	//Tooltips
 
 	/**
 	 * Field TOOLTIP_DISPLAY_DELAY. (value is 250)
 	 */
 	public static final int TOOLTIP_DISPLAY_DELAY = 250; //milliseconds
 
 	/**
 	 * Field TOOLTIP_DISPLAY_TIME. (value is 10000)
 	 */
 	public static final int TOOLTIP_DISPLAY_TIME = 10000; //milliseconds
 
 	/**
 	 * Field TOOLTIP_DISPLAY_OFFSET_X. (value is 5)
 	 */
 	public static final int TOOLTIP_DISPLAY_OFFSET_X = 5; //pixels
 
 	/**
 	 * Field TOOLTIP_DISPLAY_OFFSET_Y. (value is 5)
 	 */
 	public static final int TOOLTIP_DISPLAY_OFFSET_Y = 5; //pixels
 
 	/**
 	 * Field DIALOG_YES. (value is 0)
 	 */
 	public static final int DIALOG_YES = 0;
 
 	/**
 	 * Field DIALOG_NO. (value is 1)
 	 */
 	public static final int DIALOG_NO = 1;
 
 	/**
 	 * Field OPEN_NORMAL. (value is 0)
 	 */
 	public static final int OPEN_NORMAL = 0;
 
 	/**
 	 * Field OPEN_READONLY. (value is 1)
 	 */
 	public static final int OPEN_READONLY = 1;
 
 	//Icons
 
 	/**
 	 * Field BUTTON_ADD_LABEL. (value is ""Add"")
 	 */
 	public static final String BUTTON_ADD_LABEL = "Add";
 
 	/**
 	 * Field BUTTON_REMOVE_LABEL. (value is ""Remove"")
 	 */
 	public static final String BUTTON_REMOVE_LABEL = "Remove";
 
 	/**
 	 * Field ANNOTATION_CONTROL_MAX_WIDTH. (value is 600)
 	 */
 	public static final int ANNOTATION_CONTROL_MAX_WIDTH = 600;
 
 	/**
 	 * Field ANNOTATION_CONTROL_MAX_HEIGHT. (value is 450)
 	 */
 	public static final int ANNOTATION_CONTROL_MAX_HEIGHT = 450;
 
 	//Properties
 
 	/**
 	 * Field AVAILABLE_PROJECTS_LABEL. (value is ""Available Projects: "")
 	 */
 	public static final String AVAILABLE_PROJECTS_LABEL = "Available Projects: ";
 
 	/**
 	 * Field AVAILABLE_COMPONENTS_LABEL. (value is ""Available Components: "")
 	 */
 	public static final String AVAILABLE_COMPONENTS_LABEL = "Available Components: ";
 
 	/**
 	 * Field DEFAULT_ENTRY_CRITERIA_LABEL. (value is ""Default Entry Criteria: "")
 	 */
 	public static final String DEFAULT_ENTRY_CRITERIA_LABEL = "Default Entry Criteria: ";
 
 	/**
 	 * Field TABBED_PROPERTY_LABEL_WIDTH. (value is 105)
 	 */
 	public static final int TABBED_PROPERTY_LABEL_WIDTH = 150; //pixels
 
 	/**
 	 * Field TABBED_PROPERTY_TEXT_HEIGHT_DEFAULT. (value is 50)
 	 */
 	public static final int TABBED_PROPERTY_TEXT_HEIGHT_DEFAULT = 50; //pixels
 
 	/**
 	 * Field FILE_LOCATION_LABEL. (value is ""File location: "")
 	 */
 	public static final String FILE_LOCATION_LABEL = "File Location: ";
 
 	/**
 	 * Field NAME_LABEL. (value is ""Name: "")
 	 */
 	public static final String NAME_LABEL = "Name: ";
 
 	/**
 	 * Field FOLDER_LABEL. (value is ""Folder: "")
 	 */
 	public static final String FOLDER_LABEL = "Folder: ";
 
 	/**
 	 * Field DESCRIPTION_LABEL. (value is ""Description: "")
 	 */
 	public static final String DESCRIPTION_LABEL = "Description: ";
 
 	/**
 	 * Field COMPONENTS_LABEL. (value is ""Components: "")
 	 */
 	public static final String COMPONENTS_LABEL = "Components: ";
 
 	/**
 	 * Field ENTRY_CRITERIA_LABEL. (value is ""Entry Criteria: "")
 	 */
 	public static final String ENTRY_CRITERIA_LABEL = "Entry Criteria: ";
 
 	/**
 	 * Field OBJECTIVES_LABEL. (value is ""Objectives: "")
 	 */
 	public static final String OBJECTIVES_LABEL = "Objectives: ";
 
 	/**
 	 * Field REFERENCE_MATERIAL_LABEL. (value is ""Reference Material: "")
 	 */
 	public static final String REFERENCE_MATERIAL_LABEL = "Reference Material: ";
 
 	/**
 	 * Field EXIT_DECISION_LABEL. (value is ""Exit Decision: "")
 	 */
 	public static final String EXIT_DECISION_LABEL = "Exit Decision: ";
 
 	/**
 	 * Field CREATION_DATE_LABEL. (value is ""Created: "")
 	 */
 	public static final String CREATION_DATE_LABEL = "Creation Date: ";
 
 	/**
 	 * Field START_DATE_LABEL. (value is ""Started: "")
 	 */
 	public static final String START_DATE_LABEL = "Start Date: ";
 
 	/**
 	 * Field PREPARATION_DATE_LABEL. (value is ""Preparation Date: "")
 	 */
 	public static final String PREPARATION_DATE_LABEL = "Preparation Date: ";
 
 	/**
 	 * Field DECISION_DATE_LABEL. (value is ""Decision Date: "")
 	 */
 	public static final String DECISION_DATE_LABEL = "Decision Date: ";
 
 	/**
 	 * Field REWORK_DATE_LABEL. (value is ""Rework Date: "")
 	 */
 	public static final String REWORK_DATE_LABEL = "Rework Date: ";
 
 	/**
 	 * Field END_DATE_LABEL. (value is ""CEnd Date: "")
 	 */
 	public static final String END_DATE_LABEL = "End Date: ";
 
 	/**
 	 * Field MEETING_DECISION_LABEL. (value is ""Meeting: "")
 	 */
 	public static final String DECISION_MEETING_LABEL = "Meeting: ";
 
 	/**
 	 * Field DECISION_PARTICIPANTS_LABEL. (value is ""Decision Participants: "")
 	 */
 	public static final String DECISION_PARTICIPANTS_LABEL = "Decision Participants: ";
 
 	/**
 	 * Field DECISION_TIME_SPENT_LABEL. (value is ""Decision Time Spent: "")
 	 */
 	public static final String DECISION_TIME_SPENT_LABEL = "Decision Time Spent: ";
 
 	/**
 	 * Field SUBJECT_LABEL. (value is ""Subject: "")
 	 */
 	public static final String SUBJECT_LABEL = "Subject: ";
 
 	/**
 	 * Field TIME_SPENT_LABEL. (value is ""Time Spent: "")
 	 */
 	public static final String TIME_SPENT_LABEL = "Time Spent: ";
 
 	/**
 	 * Field TIME_SPENT_TOTAL_LABEL. (value is ""Time Spent (total): "")
 	 */
 	public static final String TIME_SPENT_TOTAL_LABEL = "Time Spent (total): ";
 
 	/**
 	 * Field EMAIL_LABEL. (value is ""Email: "")
 	 */
 	public static final String EMAIL_LABEL = "Email: ";
 
 	/**
 	 * Field USER_DETAILS_LABEL. (value is ""User Details: "")
 	 */
 	public static final String USER_DETAILS_LABEL = "User Details: ";
 
 	/**
 	 * Field ROLES_LABEL. (value is ""Roles: "")
 	 */
 	public static final String ROLES_LABEL = "Roles: ";
 
 	/**
 	 * Field FOCUS_AREA_LABEL. (value is ""Focus Area: "")
 	 */
 	public static final String FOCUS_AREA_LABEL = "Focus Area: ";
 
 	/**
 	 * Field PHASE_LABEL. (value is ""Phase: "")
 	 */
 	public static final String PHASE_LABEL = "Phase: ";
 
 	/**
 	 * Field PHASE_INFO_LABEL. (value is ""Phase Information: "")
 	 */
 	public static final String PHASE_INFO_LABEL = "Phase Information: ";
 
 	/**
 	 * Field PHASE_MAP_LABEL. (value is ""Phase Map: "")
 	 */
 	public static final String PHASE_MAP_LABEL = "Phase Map: ";
 
 	/**
 	 * Field PHASE_OWNER_LABEL. (value is ""Owner: "")
 	 */
 	public static final String PHASE_OWNER_LABEL = "Owner: ";
 
 	/**
 	 * Field REVIEW_PHASE_PLANNING. (value is ""PLANNING"")
 	 */
 	public static final String PHASE_PLANNING_LABEL = "PLANNING";
 
 	/**
 	 * Field REVIEW_PHASE_PREPARATION. (value is ""PREPARATION"")
 	 */
 	public static final String PHASE_PREPARATION_LABEL = "PREPARATION";
 
 	/**
 	 * Field REVIEW_PHASE_DECISION. (value is ""DECISION"")
 	 */
 	public static final String PHASE_DECISION_LABEL = "DECISION";
 
 	/**
 	 * Field REVIEW_PHASE_REWORK. (value is ""REWORK"")
 	 */
 	public static final String PHASE_REWORK_LABEL = "REWORK";
 
 	/**
 	 * Field REVIEW_PHASE_COMPLETED. (value is ""COMPLETED"")
 	 */
 	public static final String PHASE_COMPLETED_LABEL = "COMPLETED";
 
 	/**
 	 * Field DECISION_LABEL. (value is ""Decision: "")
 	 */
 	public static final String DECISION_LABEL = "Decision: ";
 
 	/**
 	 * Field DECISION_INFO_LABEL. (value is ""Decision Information: "")
 	 */
 	public static final String DECISION_INFO_LABEL = "Decision Information: ";
 
 	/**
 	 * Field START_TIME_LABEL. (value is ""Starts: "")
 	 */
 	public static final String START_TIME_LABEL = "Starts: ";
 
 	/**
 	 * Field DURATION_LABEL. (value is ""Duration (minutes): "")
 	 */
 	public static final String DURATION_LABEL = "Duration (minutes): ";
 
 	/**
 	 * Field LOCATION_LABEL. (value is ""Location: "")
 	 */
 	public static final String LOCATION_LABEL = "Location: ";
 
 	/**
 	 * Field AUTHOR_LABEL. (value is ""Added by: "")
 	 */
 	public static final String AUTHOR_LABEL = "Added By: ";
 
 	/**
 	 * Field ID_LABEL. (value is ""Id: "")
 	 */
 	public static final String ID_LABEL = "Id: ";
 
 	/**
 	 * Field NUM_ITEMS_LABEL. (value is ""Review Items Added: "")
 	 */
 	public static final String NUM_ITEMS_LABEL = "Review Items Added: ";
 
 	/**
 	 * Field NUM_ANOMALIES_LABEL. (value is ""Anomalies Added: "")
 	 */
 	public static final String NUM_ANOMALIES_LABEL = "Anomalies Added: ";
 
 	/**
 	 * Field NUM_COMMENTS_LABEL. (value is ""Comments Added: "")
 	 */
 	public static final String NUM_COMMENTS_LABEL = "Comments Added: ";
 
 	/**
 	 * Field GIT_INVALID_VERSION_ID. (value is ""0000000000000000000000000000000000000000"")
 	 */
 	public static final String GIT_INVALID_VERSION_ID = "0000000000000000000000000000000000000000";
 
 	/**
 	 * Field NO_VERSION_PROPERTY_MESSAGE. (value is ""(Not present)"")
 	 */
 	public static final String NO_VERSION_PROPERTY_MESSAGE = "(Not Present)";
 
 	/**
 	 * Field PROJECT_LABEL. (value is ""Project Id: "")
 	 */
 	public static final String PROJECT_LABEL = "Project Id: ";
 
 	/**
 	 * Field CHANGE_ID_LABEL. (value is ""Change Id: "")
 	 */
 	public static final String CHANGE_ID_LABEL = "Change Id: ";
 
 	/**
 	 * Field DATE_SUBMITTED_LABEL. (value is ""Date Submitted: "")
 	 */
 	public static final String DATE_SUBMITTED_LABEL = "Date Submitted: ";
 
 	/**
 	 * Field CHANGES_LABEL. (value is ""Changes"")
 	 */
 	public static final String CHANGES_LABEL = "Changes";
 
 	/**
 	 * Field PATH_LABEL. (value is ""Path"")
 	 */
 	public static final String PATH_LABEL = "Path";
 
 	/**
 	 * Field PATH_INFORMATION_LABEL. (value is ""Path Information"")
 	 */
 	public static final String PATH_INFORMATION_LABEL = "Path Information";
 
 	/**
 	 * Field PATH_REPOSITORY_LABEL. (value is ""Repository: "")
 	 */
 	public static final String PATH_REPOSITORY_LABEL = "Repository Path: ";
 
 	/**
 	 * Field PATH_ABSOLUTE_LABEL. (value is ""Absolute: "")
 	 */
 	public static final String PATH_ABSOLUTE_LABEL = "Absolute Path: ";
 
 	/**
 	 * Field PATH_PROJECT_LABEL. (value is ""Project Relative: "")
 	 */
 	public static final String PATH_PROJECT_LABEL = "Project Relative Path: ";
 
 	/**
 	 * Field VERSION_LABEL. (value is ""Version: "")
 	 */
 	public static final String VERSION_LABEL = "Version: ";
 
 	/**
 	 * Field FILE_LABEL. (value is ""File: "")
 	 */
 	public static final String FILE_LABEL = "File: ";
 
 	/**
 	 * Field POSITION_LABEL. (value is ""Position: "")
 	 */
 	public static final String POSITION_LABEL = "Position: ";
 
 	/**
 	 * Field TITLE_LABEL. (value is ""Title: "")
 	 */
 	public static final String TITLE_LABEL = "Title: ";
 
 	/**
 	 * Field ORIGINAL_REVIEW_LABEL. (value is ""Original Review: "")
 	 */
 	public static final String ORIGINAL_REVIEW_LABEL = "Original Review: ";
 
 	/**
 	 * Field PARENT_ITEM_DESCRIPTION_LABEL. (value is ""Parent Item: "")
 	 */
 	public static final String PARENT_ITEM_DESCRIPTION_LABEL = "Parent Item: ";
 
 	/**
 	 * Field GLOBAL_ANOMALY_PROPERTY_VALUE. (value is ""(Global review anomaly)"")
 	 */
 	public static final String GLOBAL_ANOMALY_PROPERTY_VALUE = "(Global Review Anomaly)";
 
 	/**
 	 * Field POSTPONED_GLOBAL_ANOMALY_PROPERTY_VALUE. (value is ""(Postponed Global review anomaly. Original Review: "")
 	 */
 	public static final String POSTPONED_GLOBAL_ANOMALY_PROPERTY_VALUE = "(Postponed Global review anomaly.  Original Review: ";
 
 	/**
 	 * Field FILE_NOT_IN_VERSION_CONTROL_MSG. (value is ""(Not version controlled)"")
 	 */
 	public static final String FILE_NOT_IN_VERSION_CONTROL_MSG = "(Not Version Controlled)";
 
 	/**
 	 * Field IN_PROGRESS_MSG. (value is ""(In Progress)"")
 	 */
 	public static final String IN_PROGRESS_MSG = "(In Progress)";
 
 	/**
 	 * Field NO_DUE_DATE_MSG. (value is ""(No Due Date)"")
 	 */
 	public static final String NO_DUE_DATE_MSG = "(No Due Date)";
 
 	/**
 	 * Field DUE_DATE_PASSED_MSG. (value is ""(Overdue) "")
 	 */
 	public static final String DUE_DATE_PASSED_MSG = "(Overdue) ";
 
 	/**
 	 * Field STATE_LABEL. (value is ""State: "")
 	 */
 	public static final String STATE_LABEL = "State: ";
 
 	/**
 	 * Field DUE_DATE_LABEL. (value is ""Due Date: "")
 	 */
 	public static final String DUE_DATE_LABEL = "Due Date: ";
 
 	/**
 	 * Field CLASS_LABEL. (value is ""Class: "")
 	 */
 	public static final String CLASS_LABEL = "Class: ";
 
 	/**
 	 * Field RANK_LABEL. (value is ""Rank: "")
 	 */
 	public static final String RANK_LABEL = "Rank: ";
 
 	/**
 	 * Field RULE_ID_LABEL. (value is ""Rule ID: "")
 	 */
 	public static final String RULE_ID_LABEL = "Rule ID: ";
 
 	/**
 	 * Field ASSIGNED_TO_LABEL. (value is ""Assigned to: "")
 	 */
 	public static final String ASSIGNED_TO_LABEL = "Assigned to: ";
 
 	/**
 	 * Field ASSIGNED_TO_LABEL2. (value is ""Assigned to"")
 	 */
 	public static final String ASSIGNED_TO_LABEL2 = "Assigned to";
 
 	/**
 	 * Field NOT_ACCEPTED_REASON_LABEL. (value is ""Reason for Rejection: "")
 	 */
 	public static final String NOT_ACCEPTED_REASON_LABEL = "Reason for Rejection: ";
 
 	/**
 	 * Field DECIDED_BY_LABEL. (value is ""Decided by: "")
 	 */
 	public static final String DECIDED_BY_LABEL = "Decided by: ";
 
 	/**
 	 * Field FIXED_BY_LABEL. (value is ""Fixed by: "")
 	 */
 	public static final String FIXED_BY_LABEL = "Fixed by: ";
 
 	/**
 	 * Field FOLLOWUP_BY_LABEL. (value is ""Follow-up by: "")
 	 */
 	public static final String FOLLOWUP_BY_LABEL = "Follow-up by: ";
 
 	/**
 	 * Field RULE_SETS_LABEL. (value is ""Applied Rule Sets: "")
 	 */
 	public static final String RULE_SETS_LABEL = "Applied Rule Sets: ";
 
 	/**
 	 * Field DEFAULT_DATE_FORMAT. (value is ""yyyy/MM/dd HH:mm:ss"")
 	 */
 	public static final String DEFAULT_DATE_FORMAT = "yyyy-MMM-dd HH:mm:ss";
 
 	/**
 	 * Field SIMPLE_DATE_FORMAT. (value is ""yyyy/MM/dd"")
 	 */
 	public static final String SIMPLE_DATE_FORMAT = "yyyy-MMM-dd";
 
 	/**
 	 * Field SIMPLE_DATE_FORMAT_MINUTES. (value is ""yyyy/MM/dd hh:mm"")
 	 */
 	public static final String SIMPLE_DATE_FORMAT_MINUTES = "yyyy-MMM-dd HH:mm";
 
 	/**
 	 * Field SPENT_TIME_COLUMN_HEADER. (value is ""Time spent (minutes)"")
 	 */
 	public static final String SPENT_TIME_COLUMN_HEADER = "Time spent (minutes)";
 
 	/**
 	 * Field ENTRY_TIME_COLUMN_HEADER. (value is ""Time of entry"")
 	 */
 	public static final String ENTRY_TIME_COLUMN_HEADER = "Time of entry";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER. (value is ""Basic Parameters"")
 	 */
 	public static final String BASIC_PARAMS_HEADER = "Basic Parameters";
 
 	/**
 	 * Field BASIC_PARAMS_HEADER_DETAILS. (value is ""Enter the mandatory basic parameters for this "")
 	 */
 	public static final String BASIC_PARAMS_HEADER_DETAILS = "Enter the mandatory basic parameters for this ";
 
 	/**
 	 * Field EXTRA_PARAMS_HEADER. (value is ""Extra Parameters"")
 	 */
 	public static final String EXTRA_PARAMS_HEADER = "Extra Parameters";
 
 	/**
 	 * Field EXTRA_PARAMS_HEADER_DETAILS. (value is ""Enter the optional extra parameters for this "")
 	 */
 	public static final String EXTRA_PARAMS_HEADER_DETAILS = "Enter the optional extra parameters for this ";
 
 	/**
 	 * Field ANOMALY_TITLE_LABEL_VALUE. (value is ""Anomaly Title: "")
 	 */
 	public static final String ANOMALY_TITLE_LABEL_VALUE = "Anomaly Title: ";
 
 	/**
 	 * Field ANOMALY_DESCRIPTION_LABEL_VALUE. (value is ""Anomaly Description: "")
 	 */
 	public static final String ANOMALY_DESCRIPTION_LABEL_VALUE = "Anomaly Description: ";
 
 	//Review types and phases
 
 	/**
 	 * Field REVIEW_TYPE_BASIC. (value is ""Basic"")
 	 */
 	public static final String REVIEW_TYPE_BASIC = "Basic";
 
 	/**
 	 * Field REVIEW_TYPE_INFORMAL. (value is ""Informal"")
 	 */
 	public static final String REVIEW_TYPE_INFORMAL = "Informal";
 
 	/**
 	 * Field REVIEW_TYPE_FORMAL. (value is ""Formal"")
 	 */
 	public static final String REVIEW_TYPE_FORMAL = "Formal";
 
 	/**
 	 * Field REVIEW_PHASE_STARTED. (value is ""PLANNING"")
 	 */
 	public static final String REVIEW_PHASE_STARTED = "STARTED";
 
 	/**
 	 * Field REVIEW_PHASE_COMPLETED. (value is ""COMPLETED"")
 	 */
 	public static final String REVIEW_PHASE_COMPLETED = "COMPLETED";
 
 	//Review item types
 
 	/**
 	 * Field REVIEW_ITEM_TYPE_RESOURCE. (value is 0)
 	 */
 	public static final int REVIEW_ITEM_TYPE_RESOURCE = 0;
 
 	/**
 	 * Field REVIEW_ITEM_TYPE_COMMIT. (value is 1)
 	 */
 	public static final int REVIEW_ITEM_TYPE_COMMIT = 1;
 
 	/**
 	 * Field REVIEW_ITEM_TYPE_POSTPONED. (value is 2)
 	 */
 	public static final int REVIEW_ITEM_TYPE_POSTPONED = 2;
 
 	//User Roles
 
 	/**
 	 * Field USER_ROLE_LEAD. (value is ""Lead"")
 	 */
 	public static final String USER_ROLE_LEAD = "Lead";
 
 	/**
 	 * Field USER_ROLE_AUTHOR. (value is ""Author"")
 	 */
 	public static final String USER_ROLE_AUTHOR = "Author";
 
 	/**
 	 * Field USER_ROLE_REVIEWER. (value is ""Reviewer"")
 	 */
 	public static final String USER_ROLE_REVIEWER = "Reviewer";
 
 	/**
 	 * Field USER_ROLE_ORGANIZER. (value is ""Organizer"")
 	 */
 	public static final String USER_ROLE_ORGANIZER = "Organizer";
 
 	/**
 	 * Field PARTICIPANT_ROLES.
 	 */
 	public static final List<String> PARTICIPANT_ROLES = Collections.unmodifiableList(Arrays.asList(
 			R4EUIConstants.USER_ROLE_LEAD, R4EUIConstants.USER_ROLE_AUTHOR, R4EUIConstants.USER_ROLE_REVIEWER,
 			USER_ROLE_ORGANIZER));
 
 	//Anomaly Classes & Ranks
 
 	/**
 	 * Field ANOMALY_CLASS_ERRONEOUS. (value is ""Erroneous"")
 	 */
 	public static final String ANOMALY_CLASS_ERRONEOUS = "Erroneous";
 
 	/**
 	 * Field ANOMALY_CLASS_SUPERFLUOUS. (value is ""Superfluous"")
 	 */
 	public static final String ANOMALY_CLASS_SUPERFLUOUS = "Superfluous";
 
 	/**
 	 * Field ANOMALY_CLASS_IMPROVEMENT. (value is ""Improvement"")
 	 */
 	public static final String ANOMALY_CLASS_IMPROVEMENT = "Improvement";
 
 	/**
 	 * Field ANOMALY_CLASS_COMMENT. (value is ""Comment"")
 	 */
 	public static final String ANOMALY_CLASS_COMMENT = "Comment";
 
 	/**
 	 * Field ANOMALY_CLASS_MISSSING. (value is ""Missing"")
 	 */
 	public static final String ANOMALY_CLASS_MISSSING = "Missing";
 
 	/**
 	 * Field ANOMALY_CLASS_QUESTION. (value is ""Question"")
 	 */
 	public static final String ANOMALY_CLASS_QUESTION = "Question";
 
 	/**
 	 * Field ANOMALY_RANK_NONE. (value is ""NONE"")
 	 */
 	public static final String ANOMALY_RANK_NONE = "NONE";
 
 	/**
 	 * Field ANOMALY_RANK_MINOR. (value is ""MINOR"")
 	 */
 	public static final String ANOMALY_RANK_MINOR = "MINOR";
 
 	/**
 	 * Field ANOMALY_RANK_MAJOR. (value is ""MAJOR"")
 	 */
 	public static final String ANOMALY_RANK_MAJOR = "MAJOR";
 
 	/**
 	 * Field RANK_VALUES.
 	 */
 	public static final String[] RANK_VALUES = { R4EUIConstants.ANOMALY_RANK_NONE, R4EUIConstants.ANOMALY_RANK_MINOR,
 			R4EUIConstants.ANOMALY_RANK_MAJOR }; //NOTE: This has to match R4EAnomalyRank in R4E core plugin
 
 	/**
 	 * Field CLASS_VALUES.
 	 */
 	public static final String[] CLASS_VALUES = { R4EUIConstants.ANOMALY_CLASS_ERRONEOUS,
 			R4EUIConstants.ANOMALY_CLASS_SUPERFLUOUS, R4EUIConstants.ANOMALY_CLASS_IMPROVEMENT,
 			R4EUIConstants.ANOMALY_CLASS_QUESTION, R4EUIConstants.ANOMALY_CLASS_COMMENT,
 			R4EUIConstants.ANOMALY_CLASS_MISSSING }; //NOTE: This has to match CommentType in R4E core plugin
 
 	/**
 	 * Field MESSAGE_TYPE_ITEMS_READY. (value is "0")
 	 */
 	public static final int MESSAGE_TYPE_ITEMS_READY = 0;
 
 	/**
 	 * Field MESSAGE_TYPE_ITEMS_REMOVED. (value is "1")
 	 */
 	public static final int MESSAGE_TYPE_ITEMS_REMOVED = 1;
 
 	/**
 	 * Field MESSAGE_TYPE_PROGRESS. (value is "2")
 	 */
 	public static final int MESSAGE_TYPE_PROGRESS = 2;
 
 	/**
 	 * Field MESSAGE_TYPE_COMPLETION. (value is "3")
 	 */
 	public static final int MESSAGE_TYPE_COMPLETION = 3;
 
 	/**
 	 * Field MESSAGE_TYPE_QUESTION. (value is "4")
 	 */
 	public static final int MESSAGE_TYPE_QUESTION = 4;
 
 	/**
 	 * Field MESSAGE_TYPE_MEETING. (value is "5")
 	 */
 	public static final int MESSAGE_TYPE_MEETING = 5;
 
 	/**
 	 * Field POSTPONED_ATTR_STR. (value is ""isPostponed"")
 	 */
 	public static final String POSTPONED_ATTR_STR = "isPostponed";
 
 	/**
 	 * Field TRUE_ATTR_VALUE_STR. (value is ""true"")
 	 */
 	public static final String TRUE_ATTR_VALUE_STR = "true";
 
 	/**
 	 * Field POSTPONED_ATTR_ORIG_REVIEW_NAME. (value is ""originalReviewName"")
 	 */
 	public static final String POSTPONED_ATTR_ORIG_REVIEW_NAME = "originalReviewName";
 
 	/**
 	 * Field POSTPONED_ATTR_ORIG_ANOMALY_ID. (value is ""originalAnomalyId"")
 	 */
 	public static final String POSTPONED_ATTR_ORIG_ANOMALY_ID = "originalAnomalyId";
 
 	/**
 	 * Field POSTPONED_ATTR_ORIG_COMMENT_ID. (value is ""originalCommentId"")
 	 */
 	public static final String POSTPONED_ATTR_ORIG_COMMENT_ID = "originalCommentId";
 
 	//Commands
 
 	/**
 	 * Field ACTIVE_REVIEW_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.review.reviewActive"")
 	 */
 	public static final String ACTIVE_REVIEW_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.review.reviewActive";
 
 	/**
 	 * Field JOB_IN_PROGRESS_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.dialogOpen"")
 	 */
 	public static final String JOB_IN_PROGRESS_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.dialogOpen";
 
 	/**
 	 * Field TOGGLE_STATE_COMMAND_KEY. (value is ""org.eclipse.ui.commands.toggleState"")
 	 */
 	public static final String TOGGLE_STATE_COMMAND_KEY = "org.eclipse.ui.commands.toggleState";
 
 	/**
 	 * Field LINK_EDITOR_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.LinkEditor"")
 	 */
 	public static final String LINK_EDITOR_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.LinkEditor";
 
 	/**
 	 * Field LINK_PROPERTIES_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.LinkProperties"")
 	 */
 	public static final String LINK_PROPERTIES_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.LinkProperties";
 
 	/**
 	 * Field ALPHA_SORTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.sorters.AlphaSort"")
 	 */
 	public static final String ALPHA_SORTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.sorters.AlphaSort";
 
 	/**
 	 * Field ALPHA_SORTER_ICON_FILE. (value is ""icons/elcl16/alphasort_menu.gif"")
 	 */
 	public static final String ALPHA_SORTER_ICON_FILE = "icons/elcl16/alphasort_menu.gif";
 
 	/**
 	 * Field REVIEW_TYPE_SORTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.sorters.ReviewTypeSort"")
 	 */
 	public static final String REVIEW_TYPE_SORTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.sorters.ReviewTypeSort";
 
 	/**
 	 * Field REVIEW_TYPE_SORTER_ICON_FILE. (value is ""icons/elcl16/revtypesort_menu.gif"")
 	 */
 	public static final String REVIEW_TYPE_SORTER_ICON_FILE = "icons/elcl16/revtypesort_menu.gif";
 
 	/**
 	 * Field ANOMALIES_MY_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.AnomaliesMy"")
 	 */
 	public static final String ANOMALIES_MY_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.AnomaliesMy";
 
 	/**
 	 * Field ANOMALIES_MY_FILTER_NAME. (value is ""Show My Anomalies"")
 	 */
 	public static final String ANOMALIES_MY_FILTER_NAME = "Show My Anomalies";
 
 	/**
 	 * Field REVIEWS_COMPLETED_FILTER_COMMAND. (value is
 	 * ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsCompleted"")
 	 */
 	public static final String REVIEWS_COMPLETED_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsCompleted";
 
 	/**
 	 * Field REVIEWS_COMPLETED_FILTER_NAME. (value is ""Hide Completed Reviews"")
 	 */
 	public static final String REVIEWS_COMPLETED_FILTER_NAME = "Hide Completed Reviews";
 
 	/**
 	 * Field REVIEWS_ONLY_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsOnly"")
 	 */
 	public static final String REVIEWS_ONLY_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsOnly";
 
 	/**
 	 * Field REVIEWS_ONLY_FILTER_NAME. (value is ""Show reviews only"")
 	 */
 	public static final String REVIEWS_ONLY_FILTER_NAME = "Show Reviews Only";
 
 	/**
 	 * Field REVIEWS_MY_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsMy"")
 	 */
 	public static final String REVIEWS_MY_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsMy";
 
 	/**
 	 * Field REVIEWS_MY_FILTER_NAME. (value is ""Show my reviews"")
 	 */
 	public static final String REVIEWS_MY_FILTER_NAME = "Show My Reviews";
 
 	/**
 	 * Field REVIEWS_PARTICIPANT_FILTER_COMMAND. (value is
 	 * ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsParticipant"")
 	 */
 	public static final String REVIEWS_PARTICIPANT_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewsParticipant";
 
 	/**
 	 * Field REVIEWS_PARTICIPANT_FILTER_NAME. (value is ""Show reviews for participant... "")
 	 */
 	public static final String REVIEWS_PARTICIPANT_FILTER_NAME = "Show Reviews for Participant... ";
 
 	/**
 	 * Field REVIEWS_PARTICIPANT_FILTER_TOOLTIP. (value is ""Show reviews the given participant is participating in"")
 	 */
 	public static final String REVIEWS_PARTICIPANT_FILTER_TOOLTIP = "Show Reviews the Given Participant is Participating In";
 
 	/**
 	 * Field REVIEWS_PARTICIPANT_FILTER_MNEMONIC. (value is ""P"")
 	 */
 	public static final String REVIEWS_PARTICIPANT_FILTER_MNEMONIC = "P";
 
 	/**
 	 * Field ASSIGN_MY_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.AssignMy"")
 	 */
 	public static final String ASSIGN_MY_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.AssignMy";
 
 	/**
 	 * Field ASSIGN_MY_FILTER_NAME. (value is ""Show My Assigned Elements"")
 	 */
 	public static final String ASSIGN_MY_FILTER_NAME = "Show My Assigned Elements";
 
 	/**
 	 * Field ASSIGN_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.AssignParticipants"")
 	 */
 	public static final String ASSIGN_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.AssignParticipants";
 
 	/**
 	 * Field ASSIGN_FILTER_NAME. (value is ""Show Assigned Elements for Participant... "")
 	 */
 	public static final String ASSIGN_FILTER_NAME = "Show Assigned Elements for Participant... ";
 
 	/**
 	 * Field UNASSIGN_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.Unassign"")
 	 */
 	public static final String UNASSIGN_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.Unassign";
 
 	/**
 	 * Field UNASSIGN_FILTER_NAME. (value is ""Show Unassigned Elements"")
 	 */
 	public static final String UNASSIGN_FILTER_NAME = "Show Unassigned Elements";
 
 	/**
 	 * Field ANOMALIES_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.Anomalies"")
 	 */
 	public static final String ANOMALIES_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.Anomalies";
 
 	/**
 	 * Field ANOMALIES_FILTER_NAME. (value is ""Hide selections"")
 	 */
 	public static final String ANOMALIES_FILTER_NAME = "Show Anomalies Only";
 
 	/**
 	 * Field REVIEWED_ELEMS_FILTER_COMMAND. (value is
 	 * ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewedElems"")
 	 */
 	public static final String REVIEWED_ELEMS_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.ReviewedElems";
 
 	/**
 	 * Field REVIEWED_ELEMS_FILTER_NAME. (value is ""Hide reviewed elements"")
 	 */
 	public static final String REVIEWED_ELEMS_FILTER_NAME = "Hide Reviewed Elements";
 
 	/**
 	 * Field HIDE_RULE_SETS_FILTER_COMMAND. (value is
 	 * ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.HideRuleSets"")
 	 */
 	public static final String HIDE_RULE_SETS_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.HideRuleSets";
 
 	/**
 	 * Field HIDE_RULE_SETS_FILTER_NAME. (value is ""HideRuleSets"")
 	 */
 	public static final String HIDE_RULE_SETS_FILTER_NAME = "Hide Rule Sets";
 
 	/**
 	 * Field HIDE_DELTAS_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.HideDeltas"")
 	 */
 	public static final String HIDE_DELTAS_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.HideDeltas";
 
 	/**
 	 * Field HIDE_DELTAS_FILTER_NAME. (value is ""HideDeltas"")
 	 */
 	public static final String HIDE_DELTAS_FILTER_NAME = "Hide Deltas";
 
 	/**
 	 * Field ASSIGN_FILTER_TOOLTIP. (value is ""Show Review Elements Assigned to Participant"")
 	 */
 	public static final String ASSIGN_FILTER_TOOLTIP = "Show Review Elements Assigned to Participant";
 
 	/**
 	 * Field ASSIGN_FILTER_MNEMONIC. (value is ""A"")
 	 */
 	public static final String ASSIGN_FILTER_MNEMONIC = "A";
 
 	/**
 	 * Field GO_INTO_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.GoInto"")
 	 */
 	public static final String GO_INTO_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.GoInto";
 
 	/**
 	 * Field GO_INTO_FILTER_NAME. (value is ""Go Into"")
 	 */
 	public static final String GO_INTO_FILTER_NAME = "Go Into";
 
 	/**
 	 * Field REMOVE_ALL_FILTER_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.filters.RemoveAll"")
 	 */
 	public static final String REMOVE_ALL_FILTER_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.filters.RemoveAll";
 
 	/**
 	 * Field REMOVE_ALL_FILTER_NAME. (value is ""Remove all filters"")
 	 */
 	public static final String REMOVE_ALL_FILTER_NAME = "Remove All Filters";
 
 	/**
 	 * Field NEW_LINKED_ANOMALY_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.NewLinkedAnomaly"")
 	 */
 	public static final String NEW_LINKED_ANOMALY_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.NewLinkedAnomaly";
 
 	/**
 	 * Field NEW_LINKED_ANOMALY_NAME. (value is ""New Linked Anomaly..."")
 	 */
 	public static final String NEW_LINKED_ANOMALY_NAME = "New Linked Anomaly...";
 
 	/**
 	 * Field NEW_LINKED_ANOMALY_TOOLTIP. (value is ""Add a new anomaly that is linked to this selection"")
 	 */
 	public static final String NEW_LINKED_ANOMALY_TOOLTIP = "Add a New Anomaly that is Linked to this Content";
 
 	/**
 	 * Field NEW_LINKED_ANOMALY_MNEMONIC. (value is ""L"")
 	 */
 	public static final String NEW_LINKED_ANOMALY_MNEMONIC = "L";
 
 	/**
 	 * Field OPEN_EDITOR_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.openEditor"")
 	 */
 	public static final String OPEN_EDITOR_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.openEditor";
 
 	/**
 	 * Field OPEN_EDITOR_ICON_FILE. (value is ""icons/elcl16/openfile_menu.gif"")
 	 */
 	public static final String OPEN_EDITOR_ICON_FILE = "icons/elcl16/openfile_menu.gif";
 
 	/**
 	 * Field OPEN_EDITOR_COMMAND_NAME. (value is ""Open file in editor"")
 	 */
 	public static final String OPEN_EDITOR_COMMAND_NAME = "Open File in Editor";
 
 	/**
	 * Field OPEN_EDITOR_COMMAND_TOOLTIP. (value is ""Open the File with the associated Editor and Locate Element"")
 	 */
	public static final String OPEN_EDITOR_COMMAND_TOOLTIP = "Open the File with the associated Editor and Locate Element";
 
 	/**
 	 * Field OPEN_EDITOR_COMMAND_MNEMONIC. (value is ""E"")
 	 */
 	public static final String OPEN_EDITOR_COMMAND_MNEMONIC = "E";
 
 	/**
 	 * Field CHANGE_REVIEW_STATE_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.changeReviewState"")
 	 */
 	public static final String CHANGE_REVIEW_STATE_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.changeReviewState";
 
 	/**
 	 * Field CHANGE_REVIEW_STATE_ICON_FILE. (value is ""icons/obj16/done_tsk.gif"")
 	 */
 	public static final String CHANGE_REVIEW_STATE_ICON_FILE = "icons/obj16/done_tsk.gif";
 
 	/**
 	 * Field MARK_REVIEW_STATE_COMMAND_NAME. (value is ""Mark as User Reviewed"")
 	 */
 	public static final String MARK_REVIEW_STATE_COMMAND_NAME = "Mark as User Reviewed";
 
 	/**
 	 * Field MARK_REVIEW_STATE_COMMAND_TOOLTIP. (value is ""Mark this Element as Reviewed by Participant"")
 	 */
 	public static final String MARK_REVIEW_STATE_COMMAND_TOOLTIP = "Mark this Element as Reviewed by Participant";
 
 	/**
 	 * Field UNMARK_REVIEW_STATE_COMMAND_NAME. (value is ""Unmark User Reviewed"")
 	 */
 	public static final String UNMARK_REVIEW_STATE_COMMAND_NAME = "Unmark User Reviewed";
 
 	/**
 	 * Field UNMARK_REVIEW_STATE_COMMAND_TOOLTIP. (value is ""Unmark this Element as Reviewed by Participant"")
 	 */
 	public static final String UNMARK_REVIEW_STATE_COMMAND_TOOLTIP = "Unmark this Element as Reviewed by Participant";
 
 	/**
 	 * Field CHANGE_REVIEW_STATE_COMMAND_MNEMONIC. (value is ""U"")
 	 */
 	public static final String CHANGE_REVIEW_STATE_COMMAND_MNEMONIC = "U";
 
 	/**
 	 * Field ASSIGN_TO_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.assignElement"")
 	 */
 	public static final String ASSIGN_TO_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.assignElement";
 
 	/**
 	 * Field ASSIGN_TO_ICON_FILE. (value is ""icons/obj16/assign_tsk.png"")
 	 */
 	public static final String ASSIGN_TO_ICON_FILE = "icons/obj16/assign_tsk.png";
 
 	/**
 	 * Field ASSIGN_TO_COMMAND_NAME. (value is ""Assign to Participant..."")
 	 */
 	public static final String ASSIGN_TO_COMMAND_NAME = "Assign to Participant...";
 
 	/**
 	 * Field ASSIGN_TO_COMMAND_TOOLTIP. (value is ""Assign Review Element to Participant(s)"")
 	 */
 	public static final String ASSIGN_TO_COMMAND_TOOLTIP = "Assign Review Element to Participant(s)";
 
 	/**
 	 * Field ASSIGN_TO_COMMAND_MNEMONIC. (value is ""A"")
 	 */
 	public static final String ASSIGN_TO_COMMAND_MNEMONIC = "A";
 
 	/**
 	 * Field UNASSIGN_TO_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.unassignElement"")
 	 */
 	public static final String UNASSIGN_TO_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.unassignElement";
 
 	/**
 	 * Field UNASSIGN_TO_ICON_FILE. (value is ""icons/obj16/unassign_tsk.png"")
 	 */
 	public static final String UNASSIGN_TO_ICON_FILE = "icons/obj16/unassign_tsk.png";
 
 	/**
 	 * Field UNASSIGN_TO_COMMAND_NAME. (value is ""Unassign Participant..."")
 	 */
 	public static final String UNASSIGN_TO_COMMAND_NAME = "Unassign Participant...";
 
 	/**
 	 * Field UNASSIGN_TO_COMMAND_TOOLTIP. (value is ""Unassign Review Element from Participant(s)"")
 	 */
 	public static final String UNASSIGN_TO_COMMAND_TOOLTIP = "Unassign Review Element from Participant(s)";
 
 	/**
 	 * Field UNASSIGN_TO_COMMAND_MNEMONIC. (value is ""U"")
 	 */
 	public static final String UNASSIGN_TO_COMMAND_MNEMONIC = "U";
 
 	/**
 	 * Field OPEN_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.openElement"")
 	 */
 	public static final String OPEN_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.openElement";
 
 	/**
 	 * Field OPEN_ELEMENT_ICON_FILE. (value is ""icons/obj16/open_tsk.gif"")
 	 */
 	public static final String OPEN_ELEMENT_ICON_FILE = "icons/obj16/open_tsk.gif";
 
 	/**
 	 * Field OPEN_ELEMENT_COMMAND_NAME. (value is ""Open element"")
 	 */
 	public static final String OPEN_ELEMENT_COMMAND_NAME = "Open Element";
 
 	/**
 	 * Field OPEN_ELEMENT_COMMAND_TOOLTIP. (value is ""Open and Load Data for this Element"")
 	 */
 	public static final String OPEN_ELEMENT_COMMAND_TOOLTIP = "Open and Load Data for this Element";
 
 	/**
 	 * Field OPEN_ELEMENT_COMMAND_MNEMONIC. (value is ""O"")
 	 */
 	public static final String OPEN_ELEMENT_COMMAND_MNEMONIC = "O";
 
 	/**
 	 * Field REPORT_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.reportElement"")
 	 */
 	public static final String REPORT_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.reportElement";
 
 	/**
 	 * Field REPORT_ELEMENT_ICON_FILE. (value is ""icons/obj16/report_tsk.gif"")
 	 */
 	public static final String REPORT_ELEMENT_ICON_FILE = "icons/obj16/report_tsk.gif";
 
 	/**
 	 * Field REPORT_ELEMENT_COMMAND_NAME. (value is ""Generate Report"")
 	 */
 	public static final String REPORT_ELEMENT_COMMAND_NAME = "Generate Report";
 
 	/**
 	 * Field REPORT_ELEMENT_COMMAND_TOOLTIP. (value is ""Generate a Report for this Element"")
 	 */
 	public static final String REPORT_ELEMENT_COMMAND_TOOLTIP = "Generate a Report for this Element";
 
 	/**
 	 * Field REPORT_ELEMENT_COMMAND_MNEMONIC. (value is ""R"")
 	 */
 	public static final String REPORT_ELEMENT_COMMAND_MNEMONIC = "R";
 
 	/**
 	 * Field CLOSE_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.closeElement"")
 	 */
 	public static final String CLOSE_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.closeElement";
 
 	/**
 	 * Field CLOSE_ELEMENT_ICON_FILE. (value is ""icons/obj16/close_tsk.gif"")
 	 */
 	public static final String CLOSE_ELEMENT_ICON_FILE = "icons/obj16/close_tsk.gif";
 
 	/**
 	 * Field CLOSE_ELEMENT_COMMAND_NAME. (value is ""Close element"")
 	 */
 	public static final String CLOSE_ELEMENT_COMMAND_NAME = "Close Element";
 
 	/**
 	 * Field CLOSE_ELEMENT_COMMAND_TOOLTIP. (value is ""Close and Unload Data for this Element"")
 	 */
 	public static final String CLOSE_ELEMENT_COMMAND_TOOLTIP = "Close and Unload Data for this Element";
 
 	/**
 	 * Field CLOSE_ELEMENT_COMMAND_MNEMONIC. (value is ""C"")
 	 */
 	public static final String CLOSE_ELEMENT_COMMAND_MNEMONIC = "C";
 
 	/**
 	 * Field COPY_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.CopyElement"")
 	 */
 	public static final String COPY_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.CopyElement";
 
 	/**
 	 * Field COPY_ELEMENT_ICON_FILE. (value is ""icons/obj16/copy_tsk.gif"")
 	 */
 	public static final String COPY_ELEMENT_ICON_FILE = "icons/obj16/copy_tsk.gif";
 
 	/**
 	 * Field COPY_ELEMENT_COMMAND_NAME. (value is ""Copy"")
 	 */
 	public static final String COPY_ELEMENT_COMMAND_NAME = "Copy";
 
 	/**
 	 * Field COPY_ELEMENT_COMMAND_TOOLTIP. (value is ""Copy current element to Clipboard"")
 	 */
 	public static final String COPY_ELEMENT_COMMAND_TOOLTIP = "Copy current element to Clipboard";
 
 	/**
 	 * Field COPY_ELEMENT_COMMAND_MNEMONIC. (value is ""C"")
 	 */
 	public static final String COPY_ELEMENT_COMMAND_MNEMONIC = "C";
 
 	/**
 	 * Field PASTE_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.PasteElement"")
 	 */
 	public static final String PASTE_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.PasteElement";
 
 	/**
 	 * Field PASTE_ELEMENT_ICON_FILE. (value is ""icons/obj16/paste_tsk.gif"")
 	 */
 	public static final String PASTE_ELEMENT_ICON_FILE = "icons/obj16/paste_tsk.gif";
 
 	/**
 	 * Field PASTE_ELEMENT_COMMAND_NAME. (value is ""Paste"")
 	 */
 	public static final String PASTE_ELEMENT_COMMAND_NAME = "Paste";
 
 	/**
 	 * Field PASTE_ELEMENT_COMMAND_TOOLTIP. (value is ""Paste clipboard contents to currently selected element"")
 	 */
 	public static final String PASTE_ELEMENT_COMMAND_TOOLTIP = "Paste clipboard contents to currently selected element";
 
 	/**
 	 * Field PASTE_ELEMENT_COMMAND_MNEMONIC. (value is ""V"")
 	 */
 	public static final String PASTE_ELEMENT_COMMAND_MNEMONIC = "V";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.NextState"")
 	 */
 	public static final String NEXT_STATE_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.NextState";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_ICON_FILE. (value is ""icons/elcl16/nxtstate_menu.gif"")
 	 */
 	public static final String NEXT_STATE_ELEMENT_ICON_FILE = "icons/elcl16/nxtstate_menu.gif";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_COMMAND_NAME. (value is ""Progress State"")
 	 */
 	public static final String NEXT_STATE_ELEMENT_COMMAND_NAME = "Progress State";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_COMMAND_TOOLTIP. (value is ""Progress the element to one of its Next logical state"")
 	 */
 	public static final String NEXT_STATE_ELEMENT_COMMAND_TOOLTIP = "Progress the element to one of its Next logical state";
 
 	/**
 	 * Field NEXT_STATE_ELEMENT_COMMAND_MNEMONIC. (value is ""P"")
 	 */
 	public static final String NEXT_STATE_ELEMENT_COMMAND_MNEMONIC = "P";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.PreviousState"")
 	 */
 	public static final String PREVIOUS_STATE_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.PreviousState";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_ICON_FILE. (value is ""icons/elcl16/prevstate_menu.gif"")
 	 */
 	public static final String PREVIOUS_STATE_ELEMENT_ICON_FILE = "icons/elcl16/prevstate_menu.gif";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_COMMAND_NAME. (value is ""Regress State"")
 	 */
 	public static final String PREVIOUS_STATE_ELEMENT_COMMAND_NAME = "Regress State";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_COMMAND_TOOLTIP. (value is ""Regress the element to its Previous logical State"")
 	 */
 	public static final String PREVIOUS_STATE_ELEMENT_COMMAND_TOOLTIP = "Regress the element to its Previous logical State";
 
 	/**
 	 * Field PREVIOUS_STATE_ELEMENT_COMMAND_MNEMONIC. (value is ""R"")
 	 */
 	public static final String PREVIOUS_STATE_ELEMENT_COMMAND_MNEMONIC = "R";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.addChildElement"")
 	 */
 	public static final String NEW_CHILD_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.NewChildElement";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_NAME. (value is ""New Child Element"")
 	 */
 	public static final String NEW_CHILD_ELEMENT_COMMAND_NAME = "New Child Element";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_TOOLTIP. (value is ""Add a New Child Element"")
 	 */
 	public static final String NEW_CHILD_ELEMENT_COMMAND_TOOLTIP = "Add a New Child Element";
 
 	/**
 	 * Field NEW_REVIEW_GROUP_COMMAND_NAME. (value is ""New Review Group..."")
 	 */
 	public static final String NEW_REVIEW_GROUP_COMMAND_NAME = "New Review Group...";
 
 	/**
 	 * Field NEW_REVIEW_GROUP_COMMAND_TOOLTIP. (value is ""Add a New Review Group Element"")
 	 */
 	public static final String NEW_REVIEW_GROUP_COMMAND_TOOLTIP = "Add a New Review Group Element";
 
 	/**
 	 * Field NEW_CHILD_ELEMENT_COMMAND_MNEMONIC. (value is ""A"")
 	 */
 	public static final String NEW_CHILD_ELEMENT_COMMAND_MNEMONIC = "A";
 
 	/**
 	 * Field NEW_RULE_SET_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.NewRuleSetElement"")
 	 */
 	public static final String NEW_RULE_SET_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.NewRuleSetElement";
 
 	/**
 	 * Field REMOVE_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.removeElement"")
 	 */
 	public static final String REMOVE_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.removeElement";
 
 	/**
 	 * Field REMOVE_ELEMENT_COMMAND_NAME. (value is ""Disable Element"")
 	 */
 	public static final String REMOVE_ELEMENT_COMMAND_NAME = "Disable Element";
 
 	/**
 	 * Field REMOVE_ELEMENT_COMMAND_TOOLTIP. (value is ""Disable this Element"")
 	 */
 	public static final String REMOVE_ELEMENT_COMMAND_TOOLTIP = "Disable this Element";
 
 	/**
 	 * Field REMOVE_ELEMENT_COMMAND_MNEMONIC. (value is ""D"")
 	 */
 	public static final String REMOVE_ELEMENT_COMMAND_MNEMONIC = "D";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.restoreElement"")
 	 */
 	public static final String RESTORE_ELEMENT_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.restoreElement";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_NAME. (value is ""Restore element"")
 	 */
 	public static final String RESTORE_ELEMENT_COMMAND_NAME = "Restore Element";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_TOOLTIP. (value is ""Restore this disabled element"")
 	 */
 	public static final String RESTORE_ELEMENT_COMMAND_TOOLTIP = "Restore this Disabled Element";
 
 	/**
 	 * Field RESTORE_ELEMENT_COMMAND_MNEMONIC. (value is ""R"")
 	 */
 	public static final String RESTORE_ELEMENT_COMMAND_MNEMONIC = "R";
 
 	/**
 	 * Field SEND_EMAIL_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.sendNotification"")
 	 */
 	public static final String SEND_EMAIL_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.sendNotification";
 
 	/**
 	 * Field SEND_EMAIL_ICON_FILE. (value is ""icons/view16/sendmail_tsk.gif"")
 	 */
 	public static final String SEND_EMAIL_ICON_FILE = "icons/view16/sendmail_tsk.gif";
 
 	/**
 	 * Field SEND_EMAIL_COMMAND_NAME. (value is ""Send Email/Notification..."")
 	 */
 	public static final String SEND_EMAIL_COMMAND_NAME = "Send Email/Notification...";
 
 	/**
 	 * Field SEND_EMAIL_COMMAND_TOOLTIP. (value is ""Send Email or Notification"")
 	 */
 	public static final String SEND_EMAIL_COMMAND_TOOLTIP = "Send Email or Notification";
 
 	/**
 	 * Field SEND_EMAIL_COMMAND_MNEMONIC. (value is ""N"")
 	 */
 	public static final String SEND_EMAIL_COMMAND_MNEMONIC = "N";
 
 	/**
 	 * Field IMPORT_POSTPONED_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.importPostponed"")
 	 */
 	public static final String IMPORT_POSTPONED_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.importPostponed";
 
 	/**
 	 * Field IMPORT_POSTPONED_ICON_FILE. (value is ""icons/view16/importpost_tsk.gif"")
 	 */
 	public static final String IMPORT_POSTPONED_ICON_FILE = "icons/view16/importpost_tsk.gif";
 
 	/**
 	 * Field IMPORT_POSTPONED_COMMAND_NAME. (value is ""Import Postponed Elements"")
 	 */
 	public static final String IMPORT_POSTPONED_COMMAND_NAME = "Import Postponed Anomalies";
 
 	/**
 	 * Field IMPORT_POSTPONED_COMMAND_TOOLTIP. (value is ""Import Anomalies that were set to Postponed in previous
 	 * Reviews for the current Files "")
 	 */
 	public static final String IMPORT_POSTPONED_COMMAND_TOOLTIP = "Import Anomalies that were set to Postponed in previous Reviews for the current Files ";
 
 	/**
 	 * Field IMPORT_POSTPONED_COMMAND_MNEMONIC. (value is ""I"")
 	 */
 	public static final String IMPORT_POSTPONED_COMMAND_MNEMONIC = "I";
 
 	/**
 	 * Field NEW_REVIEW_ITEM_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.NewReviewItem"")
 	 */
 	public static final String NEW_REVIEW_ITEM_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.NewReviewItem";
 
 	/**
 	 * Field SHOW_PROPERTIES_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.showProperties"")
 	 */
 	public static final String SHOW_PROPERTIES_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.showProperties";
 
 	/**
 	 * Field SHOW_PROPERTIES_ICON_FILE. (value is ""icons/obj16/showProps_tsk.gif"")
 	 */
 	public static final String SHOW_PROPERTIES_ICON_FILE = "icons/obj16/showProps_tsk.gif";
 
 	/**
 	 * Field SHOW_PROPERTIES_COMMAND_NAME. (value is ""Show Properties"")
 	 */
 	public static final String SHOW_PROPERTIES_COMMAND_NAME = "Show Properties";
 
 	/**
 	 * Field SHOW_PROPERTIES_COMMAND_TOOLTIP. (value is ""Show Element Properties in Properties View"")
 	 */
 	public static final String SHOW_PROPERTIES_COMMAND_TOOLTIP = "Show Element Properties in Properties View";
 
 	/**
 	 * Field SHOW_PROPERTIES_COMMAND_MNEMONIC. (value is ""P"")
 	 */
 	public static final String SHOW_PROPERTIES_COMMAND_MNEMONIC = "P";
 
 	/**
 	 * Field CHANGE_DISPLAY_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.changeDisplay"")
 	 */
 	public static final String CHANGE_DISPLAY_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.changeDisplay";
 
 	/**
 	 * Field DEFAULT_DISPLAY_COMMAND. (value is ""org.eclipse.mylyn.reviews.r4e.ui.commands.display.defaultDisplay"")
 	 */
 	public static final String DEFAULT_DISPLAY_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.commands.display.defaultDisplay";
 
 	/**
 	 * Field NEXT_ANOMALY_ANNOTATION_COMMAND. (value is
 	 * ""org.eclipse.mylyn.reviews.r4e.ui.annotation.commands.NextAnomalyAnnotation"")
 	 */
 	public static final String NEXT_ANOMALY_ANNOTATION_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.annotation.commands.NextAnomalyAnnotation";
 
 	/**
 	 * Field NEXT_ANOMALY_ANNOTATION_ICON_FILE. (value is ""icons/elcl16/nxtanmly_menu.gif"")
 	 */
 	public static final String NEXT_ANOMALY_ANNOTATION_ICON_FILE = "icons/elcl16/nxtanmly_menu.png";
 
 	/**
 	 * Field NEXT_ANOMALY_ANNOTATION_COMMAND_NAME. (value is ""Next Anomaly"")
 	 */
 	public static final String NEXT_ANOMALY_ANNOTATION_COMMAND_NAME = "Next Anomaly";
 
 	/**
 	 * Field NEXT_ANOMALY_ANNOTATION_COMMAND_TOOLTIP. (value is ""Go To Next Open Anomaly Annotation"")
 	 */
 	public static final String NEXT_ANOMALY_ANNOTATION_COMMAND_TOOLTIP = "Go To Next Open Anomaly Annotation";
 
 	/**
 	 * Field NEXT_ANOMALY_ANNOTATION_COMMAND_MNEMONIC. (value is ""N"")
 	 */
 	public static final String NEXT_ANOMALY_ANNOTATION_COMMAND_MNEMONIC = "N";
 
 	/**
 	 * Field PREVIOUS_ANOMALY_ANNOTATION_COMMAND. (value is
 	 * ""org.eclipse.mylyn.reviews.r4e.ui.annotation.commands.PreviousAnomalyAnnotation"")
 	 */
 	public static final String PREVIOUS_ANOMALY_ANNOTATION_COMMAND = "org.eclipse.mylyn.reviews.r4e.ui.annotation.commands.PreviousAnomalyAnnotation";
 
 	/**
 	 * Field PREVIOUS_ANOMALY_ANNOTATION_ICON_FILE. (value is ""icons/elcl16/prevanmly_menu.gif"")
 	 */
 	public static final String PREVIOUS_ANOMALY_ANNOTATION_ICON_FILE = "icons/elcl16/prevanmly_menu.png";
 
 	/**
 	 * Field PREVIOUS_ANOMALY_ANNOTATION_COMMAND_NAME. (value is ""Previous Anomaly"")
 	 */
 	public static final String PREVIOUS_ANOMALY_ANNOTATION_COMMAND_NAME = "Previous Anomaly";
 
 	/**
 	 * Field PREVIOUS_ANOMALY_ANNOTATION_COMMAND_TOOLTIP. (value is ""Go To Previous Open Anomaly Annotation"")
 	 */
 	public static final String PREVIOUS_ANOMALY_ANNOTATION_COMMAND_TOOLTIP = "Go To Previous Open Anomaly Annotation";
 
 	/**
 	 * Field PREVIOUS_ANOMALY_ANNOTATION_COMMAND_MNEMONIC. (value is ""P"")
 	 */
 	public static final String PREVIOUS_ANOMALY_ANNOTATION_COMMAND_MNEMONIC = "P";
 
 	/**
 	 * Field ANNOTATION_COMMAND_PARAM. (value is ""editor"")
 	 */
 	public static final String ANNOTATION_COMMAND_PARAM = "editor";
 
 	/**
 	 * Field START_STRING_INDEX. (value is 0)
 	 */
 	public static final int START_STRING_INDEX = 0;
 
 	/**
 	 * Field END_STRING_NAME_INDEX. (value is 10)
 	 */
 	public static final int END_STRING_NAME_INDEX = 10;
 
 	/**
 	 * Field ANCESTOR_CONTRIBUTOR. (value is 'A')
 	 */
 	public static final char ANCESTOR_CONTRIBUTOR = 'A';
 
 	/**
 	 * Field RIGHT_CONTRIBUTOR. (value is 'R')
 	 */
 	public static final char RIGHT_CONTRIBUTOR = 'R';
 
 	/**
 	 * Field LEFT_CONTRIBUTOR. (value is 'L')
 	 */
 	public static final char LEFT_CONTRIBUTOR = 'L';
 
 	//Tooltips for dialogs and property views
 
 	/**
 	 * Field ANOMALY_TITLE_TOOLTIP. (value is ""A Title that Identifies this Anomaly"")
 	 */
 	public static final String ANOMALY_TITLE_TOOLTIP = "A Title that Identifies this Anomaly";
 
 	/**
 	 * Field ANOMALY_DESCRIPTION_TOOLTIP. (value is ""A Detailed Description of the Anomaly"")
 	 */
 	public static final String ANOMALY_DESCRIPTION_TOOLTIP = "A Detailed Description of the Anomaly";
 
 	/**
 	 * Field ANOMALY_AUTHOR_TOOLTIP. (value is ""The Id of the Participant that created this Anomaly"")
 	 */
 	public static final String ANOMALY_AUTHOR_TOOLTIP = "The Id of the Participant that created this Anomaly";
 
 	/**
 	 * Field ANOMALY_CREATION_DATE_TOOLTIP. (value is ""The Creation Date for this Anomaly"")
 	 */
 	public static final String ANOMALY_CREATION_DATE_TOOLTIP = "The Creation Date for this Anomaly";
 
 	/**
 	 * Field ANOMALY_POSITION_TOOLTIP. (value is ""The Position in the Parent File of the Contents for this Anomaly"")
 	 */
 	public static final String ANOMALY_POSITION_TOOLTIP = "The Position in the Parent File of the Contents for this Anomaly";
 
 	/**
 	 * Field ANOMALY_STATE_TOOLTIP. (value is ""The Current State of the Anomaly"")
 	 */
 	public static final String ANOMALY_STATE_TOOLTIP = "The Current State of the Anomaly";
 
 	/**
 	 * Field ANOMALY_CLASS_TOOLTIP. (value is ""The Class of this Anomaly"")
 	 */
 	public static final String ANOMALY_CLASS_TOOLTIP = "The Class of this Anomaly";
 
 	/**
 	 * Field ANOMALY_RANK_TOOLTIP. (value is ""The Rank (or Severity) of this Anomaly"")
 	 */
 	public static final String ANOMALY_RANK_TOOLTIP = "The Rank (or Severity) of this Anomaly";
 
 	/**
 	 * Field ANOMALY_RULE_ID_TOOLTIP. (value is ""An Identifier for the Design Rule Violation reported by this
 	 * Anomaly"")
 	 */
 	public static final String ANOMALY_RULE_ID_TOOLTIP = "An Identifier for the Design Rule Violation reported by this Anomaly";
 
 	/**
 	 * Field ANOMALY_DUE_DATE_TOOLTIP. (value is ""The Target Date to Address this Anomaly"")
 	 */
 	public static final String ANOMALY_DUE_DATE_TOOLTIP = "The Target Date to Address this Anomaly";
 
 	/**
 	 * Field ANOMALY_DECIDED_BY_TOOLTIP. (value is ""The Participant that Analyzed this reported Anomaly"")
 	 */
 	public static final String ANOMALY_DECIDED_BY_TOOLTIP = "The Participant that Analyzed this reported Anomaly";
 
 	/**
 	 * Field ANOMALY_FIXED_BY_TOOLTIP. (value is ""The Participant that Fixed the Issue Reported in this Anomaly"")
 	 */
 	public static final String ANOMALY_FIXED_BY_TOOLTIP = "The Participant that Fixed the Issue Reported in this Anomaly";
 
 	/**
 	 * Field ANOMALY_FOLLOWUP_BY_TOOLTIP. (value is ""The Participant that Verified the Fix for the Issue Reported in
 	 * this Anomaly"")
 	 */
 	public static final String ANOMALY_FOLLOWUP_BY_TOOLTIP = "The Participant that Verified the Fix for the Issue Reported in this Anomaly";
 
 	/**
 	 * Field ANOMALY_NOT_ACCEPTED_REASON_TOOLTIP. (value is ""The Reason why this Anomaly is Rejected"")
 	 */
 	public static final String ANOMALY_NOT_ACCEPTED_REASON_TOOLTIP = "The Reason why this Anomaly is Rejected";
 
 	/**
 	 * Field COMMENT_DESCRIPTION_TOOLTIP. (value is ""The Comment Description"")
 	 */
 	public static final String COMMENT_DESCRIPTION_TOOLTIP = "The Comment Description";
 
 	/**
 	 * Field COMMENT_AUTHOR_TOOLTIP. (value is ""The Participant that Adds this Comment"")
 	 */
 	public static final String COMMENT_AUTHOR_TOOLTIP = "The Participant that Adds this Comment";
 
 	/**
 	 * Field COMMENT_CREATION_DATE_TOOLTIP. (value is ""The Creation Time of this Comment"")
 	 */
 	public static final String COMMENT_CREATION_DATE_TOOLTIP = "The Creation Time of this Comment";
 
 	/**
 	 * Field FILECONTEXT_BASE_FILE_NAME_TOOLTIP. (value is ""The base (or Reference File) Name for the File to Review"")
 	 */
 	public static final String FILECONTEXT_BASE_FILE_NAME_TOOLTIP = "The base (or Reference File) Name for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_BASE_FILE_PATH_REPOSITORY_TOOLTIP. (value is ""The base (or Reference File) Repository Path for
 	 * the File to Review"")
 	 */
 	public static final String FILECONTEXT_BASE_FILE_PATH_REPOSITORY_TOOLTIP = "The base (or Reference File) Repository Path for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_BASE_FILE_PATH_ABSOLUTE_TOOLTIP. (value is ""The base (or Reference File) Absolute Path for the
 	 * File to Review"")
 	 */
 	public static final String FILECONTEXT_BASE_FILE_PATH_ABSOLUTE_TOOLTIP = "The base (or Reference File) Absolute Path for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_BASE_FILE_PATH_PROJECT_TOOLTIP. (value is ""The base (or Reference File) Project Relative Path
 	 * for the File to Review"")
 	 */
 	public static final String FILECONTEXT_BASE_FILE_PATH_PROJECT_TOOLTIP = "The base (or Reference File) Project Relative Path for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_BASE_FILE_VERSION_TOOLTIP. (value is ""The base (or Reference File) Version for the File to
 	 * Review"")
 	 */
 	public static final String FILECONTEXT_BASE_FILE_VERSION_TOOLTIP = "The base (or Reference File) Version for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_TARGET_FILE_NAME_TOOLTIP. (value is ""The target (or current File) Name for the File to
 	 * Review"")
 	 */
 	public static final String FILECONTEXT_TARGET_FILE_NAME_TOOLTIP = "The target (or current File) Name for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_TARGET_FILE_PATH_TOOLTIP. (value is ""The target (or current File) Path for the File to
 	 * Review"")
 	 */
 	public static final String FILECONTEXT_TARGET_FILE_PATH_TOOLTIP = "The target (or current File) Path for the File to Review";
 
 	/**
 	 * Field FILECONTEXT_TARGET_FILE_VERSION_TOOLTIP. (value is ""The target (or current File) Version for the File to
 	 * Review"")
 	 */
 	public static final String FILECONTEXT_TARGET_FILE_VERSION_TOOLTIP = "The target (or current File) Version for the File to Review";
 
 	/**
 	 * Field PARTICIPANT_ID_TOOLTIP. (value is ""A Tag that Identifies this Participant"")
 	 */
 	public static final String PARTICIPANT_ID_TOOLTIP = "A Tag that Identifies this Participant";
 
 	/**
 	 * Field PARTICIPANTS_ADD_TOOLTIP. (value is ""Add User to Participant List"")
 	 */
 	public static final String PARTICIPANTS_ADD_TOOLTIP = "Add User to Participant List";
 
 	/**
 	 * Field PARTICIPANTS_ADDED_TOOLTIP. (value is ""Participants that will be added to the Review"")
 	 */
 	public static final String PARTICIPANTS_ADDED_TOOLTIP = "Participants that will be added to the Review";
 
 	/**
 	 * Field PARTICIPANTS_REMOVE_TOOLTIP. (value is ""Remove selected Participants from list"")
 	 */
 	public static final String PARTICIPANT_REMOVE_TOOLTIP = "Remove selected Participants from list";
 
 	/**
 	 * Field PARTICIPANTS_CLEAR_TOOLTIP. (value is ""Clear Participants list"")
 	 */
 	public static final String PARTICIPANTS_CLEAR_TOOLTIP = "Clear Participants list";
 
 	/**
 	 * Field PARTICIPANT_ADD_USER_TOOLTIP. (value is ""Semicolon-separated List of Users to be added to the Participant
 	 * List"")
 	 */
 	public static final String PARTICIPANT_ADD_USER_TOOLTIP = "Semicolon-separated List of Users to be added to the Participant List";
 
 	/**
 	 * Field PARTICIPANT_FIND_USER_TOOLTIP. (value is ""Search for Users..."")
 	 */
 	public static final String PARTICIPANT_FIND_USER_TOOLTIP = "Search for Users...";
 
 	/**
 	 * Field PARTICIPANT_EMAIL_TOOLTIP. (value is ""The Email Address for the Participant"")
 	 */
 	public static final String PARTICIPANT_EMAIL_TOOLTIP = "The Email Address for the Participant";
 
 	/**
 	 * Field PARTICIPANT_DETAILS_TOOLTIP. (value is ""Extra Information about this Participant"")
 	 */
 	public static final String PARTICIPANT_DETAILS_TOOLTIP = "Extra Information about this Participant";
 
 	/**
 	 * Field PARTICIPANT_ROLES_TOOLTIP. (value is ""The Roles this Participant holds for the Current Review. " +
 	 * "Roles are used to Manage the Actions the Participant can do"")
 	 */
 	public static final String PARTICIPANT_ROLES_TOOLTIP = "The Roles this Participant holds for the Current Review.  "
 			+ "Roles are used to Manage the Actions the Participant can do";
 
 	/**
 	 * Field PARTICIPANT_FOCUS_AREA_TOOLTIP. (value is ""The Focus Area for this Participant. " +
 	 * "A Focus Area is Defined by Specific Aspects of the Review this Participant should Focus his Attention to"")
 	 */
 	public static final String PARTICIPANT_FOCUS_AREA_TOOLTIP = "The Focus Area for this Participant.  "
 			+ "A Focus Area is Defined by Specific Aspects of the Review this Participant should Focus his Attention to";
 
 	/**
 	 * Field PARTICIPANT_NUM_ITEMS_TOOLTIP. (value is ""Number of Review Items added by this Participant"")
 	 */
 	public static final String PARTICIPANT_NUM_ITEMS_TOOLTIP = "Number of Review Items added by this Participant";
 
 	/**
 	 * Field PARTICIPANT_NUM_ANOMALIES_TOOLTIP. (value is ""Number of Anomlaies added by this Participant"")
 	 */
 	public static final String PARTICIPANT_NUM_ANOMALIES_TOOLTIP = "Number of Anomlaies added by this Participant";
 
 	/**
 	 * Field PARTICIPANT_NUM_COMMENTS_TOOLTIP. (value is ""Number of Comments (other than Anomalies) added by this
 	 * Participant"")
 	 */
 	public static final String PARTICIPANT_NUM_COMMENTS_TOOLTIP = "Number of Comments (other than Anomalies) added by this Participant";
 
 	/**
 	 * Field PARTICIPANT_TIME_SPENT_TOOLTIP. (value is ""The time the Participant spent working on this Review"")
 	 */
 	public static final String PARTICIPANT_TIME_SPENT_TOOLTIP = "The time the Participant spent working on this Review";
 
 	/**
 	 * Field REVIEW_GROUP_NAME_TOOLTIP. (value is ""The Name that Identifies the Review Group"")
 	 */
 	public static final String REVIEW_GROUP_NAME_TOOLTIP = "The Name that Identifies the Review Group";
 
 	/**
 	 * Field REVIEW_GROUP_FOLDER_TOOLTIP. (value is ""Browse Folders..."")
 	 */
 	public static final String REVIEW_GROUP_FOLDER_TOOLTIP = "Browse Folders...";
 
 	/**
 	 * Field REVIEW_GROUP_FILE_PATH_TOOLTIP. (value is ""The Location (Folder) where the Review Group Information is
 	 * Stored. " + "Review Group Information is stored in files that have a _group_root.xrer suffix"")
 	 */
 	public static final String REVIEW_GROUP_FILE_PATH_TOOLTIP = "The Location (Folder) where the Review Group Information is Stored.  "
 			+ "Review Group Information is stored in files that have a _group_root.xrer suffix";
 
 	/**
 	 * Field REVIEW_GROUP_DESCRIPTION_TOOLTIP. (value is ""A Brief Description of ths REview Group"")
 	 */
 	public static final String REVIEW_GROUP_DESCRIPTION_TOOLTIP = "A Brief Description of ths Review Group";
 
 	/**
 	 * Field REVIEW_GROUP_PROJECTS_TOOLTIP. (value is ""The Projects that are available for Review in this Review Group.
 	 * " + "Take Note that these Project can have Arbitrary Names, not only Eclipse Project Names"")
 	 */
 	public static final String REVIEW_GROUP_PROJECTS_TOOLTIP = "The Projects that are available for Review in this Review Group.  "
 			+ "Take Note that these Project can have Arbitrary Names, not only Eclipse Project Names";
 
 	/**
 	 * Field REVIEW_GROUP_COMPONENTS_TOOLTIP. (value is ""The Components (Subsystems, Sub-Projects etc.) that are
 	 * available for Review in this Review Group. " + "These can have Arbitrary Names"")
 	 */
 	public static final String REVIEW_GROUP_COMPONENTS_TOOLTIP = "The Components (Subsystems, Sub-Projects etc.) that are available for Review in this Review Group.  "
 			+ "These can have Arbitrary Names";
 
 	/**
 	 * Field REVIEW_GROUP_ENTRY_CRITERIA_TOOLTIP. (value is ""The Entry Criteria that will be used for all Reviews
 	 * created under this Review Group"")
 	 */
 	public static final String REVIEW_GROUP_ENTRY_CRITERIA_TOOLTIP = "The Entry Criteria that will be used for all Reviews created under this Review Group";
 
 	/**
 	 * Field REVIEW_GROUP_RULESET_REFERENCE_TOOLTIP. (value is ""The Location of the RuleSet definition Files that can
 	 * be used for Reviews created under this Review Group"")
 	 */
 	public static final String REVIEW_GROUP_RULESET_REFERENCE_TOOLTIP = "The Location of the RuleSet definition Files that can be used for Reviews created under this Review Group";
 
 	/**
 	 * Field REVIEW_TYPE_TOOLTIP. (value is ""The type of the Review: " +
 	 * "Basic Reviews are the Simplest and most Flexible ones.  " +
 	 * "Informal Reviews introduces State Tracking for Anomalies.  " +
 	 * "Formal Reviews are Structured Reviews that Conforms to the IEEE standard 1028"")
 	 */
 	public static final String REVIEW_TYPE_TOOLTIP = "The type of the Review:  "
 			+ "Basic Reviews are the Simplest and most Flexible ones.  "
 			+ "Informal Reviews introduces State Tracking for Anomalies.  "
 			+ "Formal Reviews are Structured Reviews that Conforms to the IEEE standard 1028";
 
 	/**
 	 * Field REVIEW_NAME_TOOLTIP. (value is ""A Name that Identifies this Review"")
 	 */
 	public static final String REVIEW_NAME_TOOLTIP = "A Name that Identifies this Review";
 
 	/**
 	 * Field REVIEW_DESCRIPTION_TOOLTIP. (value is ""A Description of the Review"")
 	 */
 	public static final String REVIEW_DESCRIPTION_TOOLTIP = "A Description of the Review";
 
 	/**
 	 * Field REVIEW_PROJECT_TOOLTIP. (value is ""The Project the Reviewed Code belongs to"")
 	 */
 	public static final String REVIEW_PROJECT_TOOLTIP = "The Project the Reviewed Code belongs to";
 
 	/**
 	 * Field REVIEW_COMPONENTS_TOOLTIP. (value is ""The Components for the Elements being Reviewed"")
 	 */
 	public static final String REVIEW_COMPONENTS_TOOLTIP = "The Components for the Elements being Reviewed";
 
 	/**
 	 * Field REVIEW_ENTRY_CRITERIA_TOOLTIP. (value is ""The Entry Criteria for this Review. " +
 	 * "This is automatically set to the Parent Review Group Default Value"")
 	 */
 	public static final String REVIEW_ENTRY_CRITERIA_TOOLTIP = "The Entry Criteria for this Review.  "
 			+ "This is automatically set to the Parent Review Group Default Value";
 
 	/**
 	 * Field REVIEW_OBJECTIVES_TOOLTIP. (value is ""The Objectives of this Review"")
 	 */
 	public static final String REVIEW_OBJECTIVES_TOOLTIP = "The Objectives of this Review";
 
 	/**
 	 * Field REVIEW_REFERENCE_MATERIAL_TOOLTIP. (value is ""The Reference Materials to be used for This Review"")
 	 */
 	public static final String REVIEW_REFERENCE_MATERIAL_TOOLTIP = "The Reference Materials to be used for This Review";
 
 	/**
 	 * Field REVIEW_PHASE_TOOLTIP. (value is ""The Current Review Phase. " +
 	 * "Review Phases are used mainly for Formal Reviews"")
 	 */
 	public static final String REVIEW_PHASE_TOOLTIP = "The Current Review Phase.  "
 			+ "Review Phases are used mainly for Formal Reviews";
 
 	/**
 	 * Field REVIEW_START_DATE_TOOLTIP. (value is ""The Start Date for this Review"")
 	 */
 	public static final String REVIEW_START_DATE_TOOLTIP = "The Start Date for this Review";
 
 	/**
 	 * Field REVIEW_END_DATE_TOOLTIP. (value is ""The Closing Date for this Review"")
 	 */
 	public static final String REVIEW_END_DATE_TOOLTIP = "The Closing Date for this Review";
 
 	/**
 	 * Field REVIEW_DUE_DATE_TOOLTIP. (value is ""The Closing Date for this Review"")
 	 */
 	public static final String REVIEW_DUE_DATE_TOOLTIP = "The Due Date for this Review";
 
 	/**
 	 * Field REVIEW_DUE_DATE_TOOLTIP. (value is ""The Closing Date for this Review"")
 	 */
 	public static final String REVIEW_ASSIGN_DUE_DATE_TOOLTIP = "Assign a due date for Review.";
 
 	/**
 	 * Field REVIEW_PHASE_TABLE_TOOLTIP. (value is ""The Review Phase Map that show the Review Progression"")
 	 */
 	public static final String REVIEW_PHASE_TABLE_TOOLTIP = "The Review Phase Map that show the Review Progression";
 
 	/**
 	 * Field REVIEW_PHASE_OWNER_TOOLTIP. (value is ""The Owner of the Review Phase. " +
 	 * "Only the Owner can Change the Current Review Phase"")
 	 */
 	public static final String REVIEW_PHASE_OWNER_TOOLTIP = "The Owner of the Review Phase.  "
 			+ "Only the Owner can Change the Current Review Phase";
 
 	/**
 	 * Field REVIEW_MEETING_TOOLTIP. (value is ""The Review Decision Meeting Details, as sent in the Meeting Request
 	 * Email"")
 	 */
 	public static final String REVIEW_MEETING_TOOLTIP = "The Review Decision Meeting Details, as sent in the Meeting Request Email";
 
 	/**
 	 * Field REVIEW_MEETING_SUBJECT_TOOLTIP. (value is ""The Review Decision Meeting Subject, as sent in the Meeting
 	 * Request Email"")
 	 */
 	public static final String REVIEW_MEETING_SUBJECT_TOOLTIP = "The Review Decision Meeting Subject, as sent in the Meeting Request Email";
 
 	/**
 	 * Field REVIEW_MEETING_UPDATE_TOOLTIP. (value is ""Update Meeting Details and re-send the New Meeting Request
 	 * Email"")
 	 */
 	public static final String REVIEW_MEETING_UPDATE_TOOLTIP = "Update Meeting Details and re-send the New Meeting Request Email";
 
 	/**
 	 * Field REVIEW_MEETING_TIME_TOOLTIP. (value is ""The Review Decision Meeting Time, as sent in the Meeting Request
 	 * Email"")
 	 */
 	public static final String REVIEW_MEETING_TIME_TOOLTIP = "The Review Decision Meeting Time, as sent in the Meeting Request Email";
 
 	/**
 	 * Field REVIEW_MEETING_DURATION_TOOLTIP. (value is ""The Review Decision Meeting Duration, as sent in the Meeting
 	 * Request Email"")
 	 */
 	public static final String REVIEW_MEETING_DURATION_TOOLTIP = "The Review Decision Meeting Duration, as sent in the Meeting Request Email";
 
 	/**
 	 * Field REVIEW_MEETING_LOCATION_TOOLTIP. (value is ""The Review Decision Meeting Location, as sent in the Meeting
 	 * Request Email"")
 	 */
 	public static final String REVIEW_MEETING_LOCATION_TOOLTIP = "The Review Decision Meeting Location, as sent in the Meeting Request Email";
 
 	/**
 	 * Field REVIEW_EXIT_DECISION_TOOLTIP. (value is ""The Exit Decision for this Review"")
 	 */
 	public static final String REVIEW_EXIT_DECISION_TOOLTIP = "The Exit Decision for this Review";
 
 	/**
 	 * Field REVIEW_EXIT_DECISION_PARTICIPANTS_TOOLTIP. (value is ""The Participants that were part of the Exit
 	 * Decision"")
 	 */
 	public static final String REVIEW_EXIT_DECISION_PARTICIPANTS_TOOLTIP = "The Participants that were part of the Exit Decision";
 
 	/**
 	 * Field REVIEW_EXIT_DECISION_TIME_SPENT_TOOLTIP. (value is ""The Time Spent for the Review Exit Decision"")
 	 */
 	public static final String REVIEW_EXIT_DECISION_TIME_SPENT_TOOLTIP = "The Time Spent for the Review Exit Decision";
 
 	/**
 	 * Field REVIEW_ITEM_AUTHOR_TOOLTIP. (value is ""The Participant that Adds this Review Item"")
 	 */
 	public static final String REVIEW_ITEM_AUTHOR_TOOLTIP = "The Participant that Adds this Review Item";
 
 	/**
 	 * Field REVIEW_ITEM_AUTHOR_REP_TOOLTIP. (value is ""The Email of the Participant that Created the Change"")
 	 */
 	public static final String REVIEW_ITEM_AUTHOR_REP_TOOLTIP = "The Email of the Participant that Created the Change";
 
 	/**
 	 * Field REVIEW_ITEM_PROJECT_IDS_TOOLTIP. (value is ""The Projects the Children Files of this Review Items belong
 	 * to"")
 	 */
 	public static final String REVIEW_ITEM_PROJECT_IDS_TOOLTIP = "The Projects the Children Files of this Review Items belong to";
 
 	/**
 	 * Field REVIEW_ITEM_CHANGE_ID_TOOLTIP. (value is ""The Change ID for this Change, as tagged by the Revision Control
 	 * System (if applicable)"")
 	 */
 	public static final String REVIEW_ITEM_CHANGE_ID_TOOLTIP = "The Change ID for this Change, as tagged by the Revision Control System (if applicable)";
 
 	/**
 	 * Field REVIEW_ITEM_DATE_SUBMITTED_TOOLTIP. (value is ""The Date this Change was put under Source Control (if
 	 * applicable)"")
 	 */
 	public static final String REVIEW_ITEM_DATE_SUBMITTED_TOOLTIP = "The Date this Change was put under Source Control  (if applicable)";
 
 	/**
 	 * Field REVIEW_ITEM_DESCRIPTION_TOOLTIP. (value is ""A Brief Dexcription of this Review Item. " +
 	 * "This can be entered by the User or it could be coming from the Version Control System"")
 	 */
 	public static final String REVIEW_ITEM_DESCRIPTION_TOOLTIP = "A Brief Dexcription of this Review Item.  "
 			+ "This can be entered by the User or it could be coming from the Version Control System";
 
 	/**
 	 * Field RULE_AREA_NAME_TOOLTIP. (value is ""The Area covered by the Children Design Rules (e.g. Java, C++ or any
 	 * arbitrary division)"")
 	 */
 	public static final String RULE_AREA_NAME_TOOLTIP = "The Area covered by the Children Design Rules (e.g. Java, C++ or any arbitrary division)";
 
 	/**
 	 * Field RULE_ID_TOOLTIP. (value is ""An Tag that Identifies this Design Rule"")
 	 */
 	public static final String RULE_ID_TOOLTIP = "An Tag that Identifies this Design Rule";
 
 	/**
 	 * Field RULE_TITLE_TOOLTIP. (value is ""The Design Rule Title"")
 	 */
 	public static final String RULE_TITLE_TOOLTIP = "The Design Rule Title";
 
 	/**
 	 * Field RULE_DESCRIPTION_TOOLTIP. (value is ""A Description of the Design Rule"")
 	 */
 	public static final String RULE_DESCRIPTION_TOOLTIP = "A Description of the Design Rule";
 
 	/**
 	 * Field RULE_CLASS_TOOLTIP. (value is ""The Class for this Design Rule. " +
 	 * "It will be automatically set in Anomalies that refers to this Rule"")
 	 */
 	public static final String RULE_CLASS_TOOLTIP = "The Class for this Design Rule.  "
 			+ "It will be automatically set in Anomalies that refers to this Rule";
 
 	/**
 	 * Field RULE_RANK_TOOLTIP. (value is ""The Rank (or Severity) for this Design Rule. " +
 	 * "It will be automatically set in Anomalies that refers to this Rule"")
 	 */
 	public static final String RULE_RANK_TOOLTIP = "The Rank (or Severity) for this Design Rule.  "
 			+ "It will be automatically set in Anomalies that refers to this Rule";
 
 	/**
 	 * Field RULESET_VERSION_TOOLTIP. (value is ""The Version for this Rule Set"")
 	 */
 	public static final String RULESET_VERSION_TOOLTIP = "The Version for this Rule Set";
 
 	/**
 	 * Field RULESET_NAME_TOOLTIP. (value is ""A Name that Identifies this Rule Set." +
 	 * "A Rule Set is a Collection of Related Design Rules bundled together"")
 	 */
 	public static final String RULESET_NAME_TOOLTIP = "A Name that Identifies this Rule Set."
 			+ "A Rule Set is a Collection of Related Design Rules bundled together";
 
 	/**
 	 * Field RULESET_FOLDER_TOOLTIP. (value is ""Browse Folders..."")
 	 */
 	public static final String RULESET_FOLDER_TOOLTIP = "Browse Folders...";
 
 	/**
 	 * Field RULESET_FILE_PATH_TOOLTIP. (value is ""The Location (Folder) where the Rule Set Information is Stored. " +
 	 * "Rule Set Information is stored in files that have a _rule_set.xrer suffix"")
 	 */
 	public static final String RULESET_FILE_PATH_TOOLTIP = "The Location (Folder) where the Rule Set Information is Stored.  "
 			+ "Rule Set Information is stored in files that have a _rule_set.xrer suffix";
 
 	/**
 	 * Field RULE_VIOLATION_NAME_TOOLTIP. (value is ""The Violation Highlighted by the Children Design Rules"")
 	 */
 	public static final String RULE_VIOLATION_NAME_TOOLTIP = "The Violation Highlighted by the Children Design Rules";
 
 	/**
 	 * Field CONTENTS_POSITION_TOOLTIP. (value is ""The Position in the Parent File of the Selected Contents"")
 	 */
 	public static final String CONTENTS_POSITION_TOOLTIP = "The Position in the Parent File of the Selected Contents";
 
 	/**
 	 * Field USER_ID_TOOLTIP. (value is ""A Tag that Identifies this User"")
 	 */
 	public static final String USER_ID_TOOLTIP = "A Tag that Identifies this User";
 
 	/**
 	 * Field USER_NAME_TOOLTIP. (value is ""The Actual Name of this User"")
 	 */
 	public static final String USER_NAME_TOOLTIP = "The Actual Name of this User";
 
 	/**
 	 * Field USER_OFFICE_TOOLTIP. (value is ""The location of this User"")
 	 */
 	public static final String USER_OFFICE_TOOLTIP = "The location of this User";
 
 	/**
 	 * Field USER_COMPANY_TOOLTIP. (value is ""The Organization this User is Part of"")
 	 */
 	public static final String USER_COMPANY_TOOLTIP = "The Organization this User is Part of";
 
 	/**
 	 * Field USER_DEPARTMENT_TOOLTIP. (value is ""The Department this User is Part of"")
 	 */
 	public static final String USER_DEPARTMENT_TOOLTIP = "The Department this User is Part of";
 
 	/**
 	 * Field USER_CITY_TOOLTIP. (value is ""The City where the User's Organization is located"")
 	 */
 	public static final String USER_CITY_TOOLTIP = "The City where the User's Organization is located";
 
 	/**
 	 * Field USER_COUNTRY_TOOLTIP. (value is ""The Country where the User's Organization is located"")
 	 */
 	public static final String USER_COUNTRY_TOOLTIP = "The Country where the User's Organization is located";
 
 	/**
 	 * Field USER_SEARCH_TOOLTIP. (value is ""Search for Users matching the Criteria(s)"")
 	 */
 	public static final String USER_SEARCH_TOOLTIP = "Search for Users matching the Criteria(s)";
 
 	/**
 	 * Field USER_CLEAR_TOOLTIP. (value is ""Clear Serach Results"")
 	 */
 	public static final String USER_CLEAR_TOOLTIP = "Clear Serach Results";
 
 	/**
 	 * Field USER_NUM_ENTRIES_TOOLTIP. (value is ""Number of Matches Found"")
 	 */
 	public static final String USER_NUM_ENTRIES_TOOLTIP = "Number of Matches Found";
 
 	/**
 	 * Field USER_ADD_TOOLTIP. (value is ""Add Selected Users"")
 	 */
 	public static final String USER_ADD_TOOLTIP = "Add Selected Users";
 
 	/**
 	 * Field USER_ADDED_TOOLTIP. (value is ""Semicolon separated List of Users to be added"")
 	 */
 	public static final String USER_ADDED_TOOLTIP = "Semicolon-separated List of Users to be added";
 
 	/**
 	 * Field USER_GROUP_ADD_TOOLTIP. (value is ""Add Users to User Group"")
 	 */
 	public static final String USER_GROUP_ADD_TOOLTIP = "Add Users to User Group";
 
 	/**
 	 * Field USER_GROUP_ADDED_TOOLTIP. (value is ""User Group to add Users to"")
 	 */
 	public static final String USER_GROUP_ADDED_TOOLTIP = "User Group to add Users to";
 
 	/**
 	 * Field NOTIFICATION_COMPLETION_TOOLTIP. (value is ""Send an Automatic Email to Report that we are done reviewing
 	 * the Utems for the Current Review"")
 	 */
 	public static final String NOTIFICATION_COMPLETION_TOOLTIP = "Send an Automatic Email to Report that we are done reviewing the Utems for the Current Review";
 
 	/**
 	 * Field NOTIFICATION_ITEMS_UPDATED_TOOLTIP. (value is ""Send an Automatic Email to Report Modifications to the
 	 * Items to Review for the Current Review"")
 	 */
 	public static final String NOTIFICATION_ITEMS_UPDATED_TOOLTIP = "Send an Automatic Email to Report Modifications to the Items to Review for the Current Review";
 
 	/**
 	 * Field NOTIFICATION_PROGRESS_TOOLTIP. (value is ""Send an Automatic Email to Report about our Progress Reviewing
 	 * the Items for the Current Review"")
 	 */
 	public static final String NOTIFICATION_PROGRESS_TOOLTIP = "Send an Automatic Email to Report about our Progress Reviewing the Items for the Current Review";
 
 	/**
 	 * Field NOTIFICATION_QUESTION_TOOLTIP. (value is ""Send an Automatic Email to ask a Question about the Selected
 	 * Element or Text"")
 	 */
 	public static final String NOTIFICATION_QUESTION_TOOLTIP = "Send an Automatic Email to ask a Question about the Selected Element or Text";
 
 	/**
 	 * Field MEETING_REQUEST_TOOLTIP. (value is ""Send a new Meeting Request Notification"")
 	 */
 	public static final String MEETING_REQUEST_TOOLTIP = "Send a new Meeting Request Notification";
 
 	/**
 	 * Field PARENT_REVIEW_TOOLTIP. (value is ""The name of the review where the original postponed anomaly was
 	 * raised"")
 	 */
 	public static final String PARENT_REVIEW_TOOLTIP = "The name of the review where the original postponed anomaly was raised";
 
 	/**
 	 * Field POSTPONED_FILE_NAME_TOOLTIP. (value is ""The Name of the file where the original postponed anomaly was
 	 * raised"")
 	 */
 	public static final String POSTPONED_FILE_NAME_TOOLTIP = "The Name of the file where the original postponed anomaly was raised";
 
 	/**
 	 * Field POSTPONED_FILE_PATH_REPOSITORY_TOOLTIP. (value is ""The Repository Path to the file where the original
 	 * postponed anomaly was raised"")
 	 */
 	public static final String POSTPONED_FILE_PATH_REPOSITORY_TOOLTIP = "The Repository Path to the file where the original postponed anomaly was raised";
 
 	/**
 	 * Field POSTPONED_FILE_PATH_ABSOLUTE_TOOLTIP. (value is ""The Absolute Path to the file where the original
 	 * postponed anomaly was raised"")
 	 */
 	public static final String POSTPONED_FILE_PATH_ABSOLUTE_TOOLTIP = "The Absolute Path to the file where the original postponed anomaly was raised";
 
 	/**
 	 * Field POSTPONED_FILE_PATH_PROJECT_TOOLTIP. (value is ""The Project Relative Path to the file where the original
 	 * postponed anomaly was raised"")
 	 */
 	public static final String POSTPONED_FILE_PATH_PROJECT_TOOLTIP = "The Project Relative Path to the file where the original postponed anomaly was raised";
 
 	/**
 	 * Field POSTPONED_FILE_VERSION_TOOLTIP. (value is ""The Version of the file where the original postponed anomaly
 	 * was raised"")
 	 */
 	public static final String POSTPONED_FILE_VERSION_TOOLTIP = "The Version of the file where the original postponed anomaly was raised";
 
 	/**
 	 * Field PARTICIPANTS_LISTS_TOOLTIP. (value is ""Participants Lists. Participants Lists are a bundle of users that
 	 * can be added at once to the reviews"")
 	 */
 	public static final String PARTICIPANTS_LISTS_TOOLTIP = "Participants Lists.  Participants Lists are a bundle of users that can be added at once to the reviews";
 
 	/**
 	 * Field PARTICIPANTS_TOOLTIP. (value is ""Users that are part of the selected Participants List."")
 	 */
 	public static final String PARTICIPANTS_TOOLTIP = "Users that are part of the selected Participants List.";
 
 	/**
 	 * Field ASSIGNED_TO_TOOLTIP. (value is ""Participant assigned to Review this element."")
 	 */
 	public static final String ASSIGNED_TO_TOOLTIP = "Participant assigned to Review this element.";
 
 	//Listeners change types
 	/**
 	 * Field CHANGE_TYPE_ADD. (value is 0)
 	 */
 	public static final int CHANGE_TYPE_ADD = 0;
 
 	/**
 	 * Field CHANGE_TYPE_REMOVE. (value is 1)
 	 */
 	public static final int CHANGE_TYPE_REMOVE = 1;
 
 	/**
 	 * Field CHANGE_TYPE_OPEN. (value is 2)
 	 */
 	public static final int CHANGE_TYPE_OPEN = 2;
 
 	/**
 	 * Field CHANGE_TYPE_CLOSE. (value is 3)
 	 */
 	public static final int CHANGE_TYPE_CLOSE = 3;
 
 	/**
 	 * Field CHANGE_TYPE_REVIEWED_STATE. (value is 4)
 	 */
 	public static final int CHANGE_TYPE_REVIEWED_STATE = 4;
 
 	//Test Constants
 	/**
 	 * Field R4E_UI_JOB_FAMILY.
 	 */
 	public static final String R4E_UI_JOB_FAMILY = "R4EUI";
 
 	//Inline Markers
 
 	public static final String ANOMALY_OPEN_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.anomalyOpen.Annotation";
 
 	public static final String ANOMALY_CLOSED_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.anomalyClosed.Annotation";
 
 	public static final String ANOMALY_DISABLED_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.anomalyDisabled.Annotation";
 
 	public static final String DELTA_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.delta.Annotation";
 
 	public static final String DELTA_REVIEWED_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.deltaReviewed.Annotation";
 
 	public static final String DELTA_DISABLED_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.deltaDisabled.Annotation";
 
 	public static final String SELECTION_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.selection.Annotation";
 
 	public static final String SELECTION_REVIEWED_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.selectionReviewed.Annotation";
 
 	public static final String SELECTION_DISABLED_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.selectionDisabled.Annotation";
 
 	public static final String COMMENT_ANNOTATION_ID = "org.eclipse.mylyn.reviews.r4e.ui.comment.Annotation";
 }
