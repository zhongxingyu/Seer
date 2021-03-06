 /*******************************************************************************
  * Copyright (c) 2004 - 2006 University Of British Columbia and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     University Of British Columbia - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.mylar.internal.tasks.ui;
 
 import org.eclipse.mylar.internal.tasks.ui.views.TaskListView;
 import org.eclipse.ui.IFolderLayout;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 
 /**
  * @author Mik Kersten
  */
 public class PlanningPerspectiveFactory implements IPerspectiveFactory {
 
 	public void createInitialLayout(IPageLayout layout) {
 		defineActions(layout);
 		defineLayout(layout);
 	}
 
 	public void defineActions(IPageLayout layout) {
 		layout.addShowViewShortcut(IPageLayout.ID_RES_NAV);
 		layout.addShowViewShortcut(IPageLayout.ID_PROP_SHEET);
 		layout.addShowViewShortcut(TaskListView.ID);
 		// layout.addShowViewShortcut(TaskActivityView.ID);
 
 		layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET);
 	}
 
 	public void defineLayout(IPageLayout layout) {
 		String editorArea = layout.getEditorArea();
 
		IFolderLayout topRight = layout.createFolder("topRight", IPageLayout.RIGHT, (float) 0.6, editorArea);//$NON-NLS-1$
		topRight.addView(TaskListView.ID);
 
 		// IFolderLayout bottomLeft = layout.createFolder(
 		// "bottomLeft", IPageLayout.BOTTOM, (float) 0.50,//$NON-NLS-1$
 		// "topLeft");//$NON-NLS-1$
 		// bottomLeft.addView(TaskActivityView.ID);
		topRight.addPlaceholder(IPageLayout.ID_RES_NAV);
 
 		// IFolderLayout bottomRight = layout.createFolder(
 		// "bottomRight", IPageLayout.BOTTOM, (float) 0.66,//$NON-NLS-1$
 		// editorArea);
 		//		
 		// bottomRight.addView(IPageLayout.ID_TASK_LIST);
 
 	}
 }
