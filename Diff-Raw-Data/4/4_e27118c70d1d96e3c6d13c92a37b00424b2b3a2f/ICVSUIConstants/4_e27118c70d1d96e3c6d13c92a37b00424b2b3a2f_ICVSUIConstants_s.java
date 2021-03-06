 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.team.internal.ccvs.ui;
 
 public interface ICVSUIConstants {
 	public final String PREFIX = CVSUIPlugin.ID + "."; //$NON-NLS-1$
 
 	// image path
 	public final String ICON_PATH = "icons/full/"; //$NON-NLS-1$
 	
 	// images
 	public final String IMG_CVS_CONSOLE = "eview16/console_view.gif"; //$NON-NLS-1$
 	public final String IMG_CVS_PERSPECTIVE = "eview16/cvs_persp.gif"; //$NON-NLS-1$
 	
 	// overlays
 	public final String IMG_MERGEABLE_CONFLICT = "ovr16/confauto_ov.gif"; //$NON-NLS-1$
 	public final String IMG_QUESTIONABLE = "ovr16/question_ov.gif"; //$NON-NLS-1$
 	public final String IMG_MERGED = "ovr16/merged_ov.gif"; //$NON-NLS-1$
 	public final String IMG_EDITED = "ovr16/edited_ov.gif"; //$NON-NLS-1$
 	public final String IMG_NO_REMOTEDIR = "ovr16/no_remotedir_ov.gif"; //$NON-NLS-1$
 	
 	// objects
 	public final String IMG_REPOSITORY = "obj16/repository_rep.gif"; //$NON-NLS-1$
 	public final String IMG_TAG = "obj16/tag.gif"; //$NON-NLS-1$
 	public final String IMG_BRANCHES_CATEGORY = "obj16/branches_rep.gif"; //$NON-NLS-1$
 	public final String IMG_VERSIONS_CATEGORY = "obj16/versions_rep.gif"; //$NON-NLS-1$
 	public final String IMG_DATES_CATEGORY = "obj16/dates.gif"; //$NON-NLS-1$
 	
 	public final String IMG_MODULE = "obj16/module_rep.gif"; //$NON-NLS-1$
 	public final String IMG_PROJECT_VERSION = "obj16/prjversions_rep.gif"; //$NON-NLS-1$
 	public final String IMG_DATE = "obj16/date.gif"; //$NON-NLS-1$
 	
 	// toolbar
 	public final String IMG_REFRESH = "elcl16/refresh.gif"; //$NON-NLS-1$
 	public final String IMG_CLEAR = "elcl16/clear_co.gif"; //$NON-NLS-1$
 	public final String IMG_COLLAPSE_ALL = "elcl16/collapseall.gif"; //$NON-NLS-1$
 	public final String IMG_LINK_WITH_EDITOR = "elcl16/synced.gif"; //$NON-NLS-1$
 	
 	// toolbar (disabled)
 	public final String IMG_REFRESH_DISABLED = "dlcl16/refresh.gif"; //$NON-NLS-1$
 	public final String IMG_CLEAR_DISABLED = "dlcl16/clear_co.gif"; //$NON-NLS-1$
 		
 	// toolbar (enabled)
 	public final String IMG_REFRESH_ENABLED = "elcl16/refresh.gif"; //$NON-NLS-1$
 	public final String IMG_CLEAR_ENABLED = "elcl16/clear_co.gif"; //$NON-NLS-1$
 	public final String IMG_COLLAPSE_ALL_ENABLED = "elcl16/collapseall.gif"; //$NON-NLS-1$
 	public final String IMG_LINK_WITH_EDITOR_ENABLED = "elcl16/synced.gif"; //$NON-NLS-1$
 	
 	// wizards
	public final String IMG_NEWLOCATION = "wizards/newlocation_wiz.gif"; //$NON-NLS-1$
	public final String IMG_CVSLOGO = "wizards/newconnect_wiz.gif"; //$NON-NLS-1$
 	
 	// preferences
 	public final String PREF_SHOW_COMMENTS = "pref_show_comments"; //$NON-NLS-1$
 	public final String PREF_SHOW_TAGS = "pref_show_tags"; //$NON-NLS-1$
 	public final String PREF_HISTORY_VIEW_EDITOR_LINKING = "pref_history_view_linking"; //$NON-NLS-1$
 	public final String PREF_PRUNE_EMPTY_DIRECTORIES = "pref_prune_empty_directories";	 //$NON-NLS-1$
 	public final String PREF_TIMEOUT = "pref_timeout";	 //$NON-NLS-1$
 	public final String PREF_QUIETNESS = "pref_quietness"; //$NON-NLS-1$
 	public final String PREF_CVS_RSH = "pref_cvs_rsh"; //$NON-NLS-1$
 	public final String PREF_CVS_RSH_PARAMETERS = "pref_cvs_rsh_parameters"; //$NON-NLS-1$
 	public final String PREF_CVS_SERVER = "pref_cvs_server"; //$NON-NLS-1$
 	public final String PREF_CONSIDER_CONTENTS = "pref_consider_contents"; //$NON-NLS-1$
 	public final String PREF_REPLACE_UNMANAGED = "pref_replace_unmanaged"; //$NON-NLS-1$
 	public final String PREF_COMPRESSION_LEVEL = "pref_compression_level"; //$NON-NLS-1$
 	public final String PREF_TEXT_KSUBST = "pref_text_ksubst"; //$NON-NLS-1$
 	public final String PREF_USE_PLATFORM_LINEEND = "pref_lineend"; //$NON-NLS-1$
 	public final String PREF_PROMPT_ON_MIXED_TAGS = "pref_prompt_on_mixed_tags"; //$NON-NLS-1$
 	public final String PREF_PROMPT_ON_SAVING_IN_SYNC = "pref_prompt_on_saving_in_sync"; //$NON-NLS-1$
 	public final String PREF_SAVE_DIRTY_EDITORS = "pref_save_dirty_editors"; //$NON-NLS-1$
 	public final String PREF_PROMPT_ON_CHANGE_GRANULARITY = "pref_prompt_on_change_granularity"; //$NON-NLS-1$
 	public final String PREF_REPOSITORIES_ARE_BINARY = "pref_repositories_are_binary"; //$NON-NLS-1$
 	public final String PREF_DETERMINE_SERVER_VERSION = "pref_determine_server_version"; //$NON-NLS-1$
 	public final String PREF_CONFIRM_MOVE_TAG = "pref_confirm_move_tag"; //$NON-NLS-1$
 	public final String PREF_DEBUG_PROTOCOL = "pref_debug_protocol"; //$NON-NLS-1$
 	public final String PREF_WARN_REMEMBERING_MERGES = "pref_remember_merges"; //$NON-NLS-1$
 	public final String PREF_FIRST_STARTUP = "pref_first_startup"; //$NON-NLS-1$
 	
 	// console preferences
 	public final String PREF_CONSOLE_COMMAND_COLOR = "pref_console_command_color"; //$NON-NLS-1$
 	public final String PREF_CONSOLE_MESSAGE_COLOR = "pref_console_message_color"; //$NON-NLS-1$
 	public final String PREF_CONSOLE_ERROR_COLOR = "pref_console_error_color"; //$NON-NLS-1$
 	public final String PREF_CONSOLE_FONT = "pref_console_font"; //$NON-NLS-1$
 	public final String PREF_CONSOLE_SHOW_ON_MESSAGE = "pref_console_show_on_error"; //$NON-NLS-1$
 	public final String PREF_CONSOLE_SHOW_ON_ERROR = "pref_console_show_on_message"; //$NON-NLS-1$
 	
 		
 	// decorator preferences
 	public final String PREF_FILETEXT_DECORATION = "pref_filetext_decoration"; //$NON-NLS-1$
 	public final String PREF_FOLDERTEXT_DECORATION = "pref_foldertext_decoration"; //$NON-NLS-1$
 	public final String PREF_PROJECTTEXT_DECORATION = "pref_projecttext_decoration"; //$NON-NLS-1$
 	
 	public final String PREF_SHOW_DIRTY_DECORATION = "pref_show_overlaydirty"; //$NON-NLS-1$
 	public final String PREF_SHOW_ADDED_DECORATION = "pref_show_added"; //$NON-NLS-1$
 	public final String PREF_SHOW_HASREMOTE_DECORATION = "pref_show_hasremote"; //$NON-NLS-1$
 	public final String PREF_SHOW_NEWRESOURCE_DECORATION = "pref_show_newresource"; //$NON-NLS-1$
 	
 	public final String PREF_DIRTY_FLAG = "pref_dirty_flag"; //$NON-NLS-1$
 	public final String PREF_ADDED_FLAG = "pref_added_flag"; //$NON-NLS-1$
 	
 	public final String PREF_CALCULATE_DIRTY = "pref_calculate_dirty";	 //$NON-NLS-1$
 
 	// watch/edit preferences
 	public final String PREF_CHECKOUT_READ_ONLY = "pref_checkout_read_only"; //$NON-NLS-1$
 	public final String PREF_EDIT_ACTION = "pref_edit_action"; //$NON-NLS-1$
 	public final String PREF_EDIT_PROMPT_EDIT = "edit"; //$NON-NLS-1$
 	public final String PREF_EDIT_PROMPT_HIGHJACK = "highjack"; //$NON-NLS-1$
 	public final String PREF_EDIT_PROMPT = "pref_edit_prompt"; //$NON-NLS-1$
 	public final String PREF_EDIT_PROMPT_NEVER = "never"; //$NON-NLS-1$
 	public final String PREF_EDIT_PROMPT_ALWAYS = "always";	 //$NON-NLS-1$
 	public final String PREF_EDIT_PROMPT_IF_EDITORS = "only";	 //$NON-NLS-1$
 	
 	// Repositories view preferences
 	public final String PREF_GROUP_VERSIONS_BY_PROJECT = "pref_group_versions_by_project"; //$NON-NLS-1$
 	
 	// Wizard banners
 	public final String IMG_WIZBAN_SHARE = "wizban/newconnect_wizban.gif";	 //$NON-NLS-1$
 	public final String IMG_WIZBAN_MERGE = "wizban/mergestream_wizban.gif";	 //$NON-NLS-1$
 	public final String IMG_WIZBAN_DIFF = "wizban/createpatch_wizban.gif";   //$NON-NLS-1$
 	public final String IMG_WIZBAN_KEYWORD = "wizban/keywordsub_wizban.gif"; //$NON-NLS-1$
 	public final String IMG_WIZBAN_NEW_LOCATION = "wizban/newlocation_wizban.gif"; //$NON-NLS-1$
 	public final String IMG_WIZBAN_CHECKOUT = "wizban/newconnect_wizban.gif";	 //$NON-NLS-1$
 	
 	// Properties
 	public final String PROP_NAME = "cvs.name"; //$NON-NLS-1$
 	public final String PROP_REVISION = "cvs.revision"; //$NON-NLS-1$
 	public final String PROP_AUTHOR = "cvs.author"; //$NON-NLS-1$
 	public final String PROP_COMMENT = "cvs.comment"; //$NON-NLS-1$
 	public final String PROP_DATE = "cvs.date"; //$NON-NLS-1$
 	public final String PROP_DIRTY = "cvs.dirty"; //$NON-NLS-1$
 	public final String PROP_MODIFIED = "cvs.modified"; //$NON-NLS-1$
 	public final String PROP_KEYWORD = "cvs.date"; //$NON-NLS-1$
 	public final String PROP_TAG = "cvs.tag"; //$NON-NLS-1$
 	public final String PROP_PERMISSIONS = "cvs.permissions"; //$NON-NLS-1$
 	public final String PROP_HOST = "cvs.host"; //$NON-NLS-1$
 	public final String PROP_USER = "cvs.user"; //$NON-NLS-1$
 	public final String PROP_METHOD = "cvs.method"; //$NON-NLS-1$
 	public final String PROP_PORT = "cvs.port"; //$NON-NLS-1$
 	public final String PROP_ROOT = "cvs.root"; //$NON-NLS-1$
 	
 	// preference options
 	public final int OPTION_NEVER = 1;
 	public final int OPTION_PROMPT = 2;
 	public final int OPTION_AUTOMATIC = 3;	
 }
 
