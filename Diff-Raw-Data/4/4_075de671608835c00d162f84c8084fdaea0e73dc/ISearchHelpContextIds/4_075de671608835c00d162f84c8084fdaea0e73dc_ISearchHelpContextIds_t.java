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
 package org.eclipse.search.internal.ui;
 
 import org.eclipse.search.ui.SearchUI;
 
 public interface ISearchHelpContextIds {
 	
 	public static final String PREFIX= SearchUI.PLUGIN_ID + "."; //$NON-NLS-1$
 
 	public static final String SEARCH_DIALOG= PREFIX + "search_dialog_context";	 //$NON-NLS-1$
 
 	public static final String TEXT_SEARCH_PAGE= PREFIX + "text_search_page_context"; //$NON-NLS-1$
 	public static final String TYPE_FILTERING_DIALOG= PREFIX + "type_filtering_dialog_context"; //$NON-NLS-1$
 
 	public static final String SEARCH_VIEW= PREFIX + "search_view_context"; //$NON-NLS-1$
 	
 	public static final String SEARCH_PREFERENCE_PAGE= PREFIX + "search_preference_page_context"; //$NON-NLS-1$
 
 	public static final String SELECT_ALL_ACTION = PREFIX + "select_all_action_context"; //$NON-NLS-1$
	
	public static final String SEARCH_ACTION = PREFIX + "search_action_context"; //$NON-NLS-1$
	
	public static final String FILE_SEARCH_ACTION= PREFIX + "file_search_action_context"; //$NON-NLS-1$
 }
