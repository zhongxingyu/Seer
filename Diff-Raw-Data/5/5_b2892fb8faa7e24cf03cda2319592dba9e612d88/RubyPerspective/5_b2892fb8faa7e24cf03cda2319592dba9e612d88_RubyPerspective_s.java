 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ruby.internal.ui;
 
 
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.progress.IProgressConstants;
 
 public class RubyPerspective implements IPerspectiveFactory  {
 		
 	public void createInitialLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
 		
 		IFolderLayout folder= layout.createFolder("left", IPageLayout.LEFT, (float)0.2, editorArea); //$NON-NLS-1$
 		String navigator = "org.eclipse.dltk.ui.ScriptExplorer";
 		folder.addView(navigator);		
 		folder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 		
 		IFolderLayout outputfolder= layout.createFolder("bottom", IPageLayout.BOTTOM, (float)0.75, editorArea); //$NON-NLS-1$
 		outputfolder.addView(IPageLayout.ID_PROBLEM_VIEW);
 		outputfolder.addView(IPageLayout.ID_TASK_LIST);
 		outputfolder.addView("org.eclipse.dltk.ruby.ui.RubyDocumentationView");
 		
 		outputfolder.addPlaceholder(IPageLayout.ID_BOOKMARKS);
 		outputfolder.addPlaceholder(IProgressConstants.PROGRESS_VIEW_ID);
 		
 		layout.addView(IPageLayout.ID_OUTLINE, IPageLayout.RIGHT, (float)0.75, editorArea);
 				
 		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
 		layout.addActionSet("org.eclipse.dltk.ruby.ui.RubyActionSet"); // TODO: externalize constant
 		
 		// views - standard workbench
 		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
 		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
 		layout.addShowViewShortcut(navigator);
 		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
 		layout.addShowViewShortcut(IProgressConstants.PROGRESS_VIEW_ID);
 		
 		// new actions - Ruby project creation wizard
 		layout.addNewWizardShortcut("org.eclipse.dltk.ruby.internal.ui.wizards.RubyProjectWizard"); //$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.dltk.ruby.internal.ui.wizards.RubyFileCreationWizard"); //$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
 		layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$
 	}
 }
