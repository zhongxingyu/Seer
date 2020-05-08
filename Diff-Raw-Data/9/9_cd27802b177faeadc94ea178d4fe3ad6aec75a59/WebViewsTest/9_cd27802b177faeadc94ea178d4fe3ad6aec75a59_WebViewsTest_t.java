 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.test;
 
 import junit.framework.TestCase;
 
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IPerspectiveRegistry;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.jboss.tools.common.model.ui.views.palette.PaletteViewPart;
 import org.jboss.tools.jst.web.ui.WebDevelopmentPerspectiveFactory;
 import org.jboss.tools.jst.web.ui.navigator.WebProjectsNavigator;
 
 /**
  * @author eskimo
  *
  */
 public class WebViewsTest extends TestCase {
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	protected void setUp() throws Exception {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		try {
 			workbench.showPerspective(WebDevelopmentPerspectiveFactory.PERSPECTIVE_ID,workbench.getActiveWorkbenchWindow());
 		} catch (WorkbenchException e) {
 			fail("Cannot load perspective '" +WebDevelopmentPerspectiveFactory.PERSPECTIVE_ID + "'");
 		}
 	}
 
 	/**
 	 * 
 	 */
 	public void testRhdsPerspectiveIsDefined() {
 		IPerspectiveDescriptor perspective = getRhdsPerspective();
 		assertNotNull("Cannot find perspective '" +WebDevelopmentPerspectiveFactory.PERSPECTIVE_ID + "'",perspective);		
 	}
 	
 	
 	public void testPerspectiveIsShowed() {
 		IPerspectiveDescriptor perspective = getRhdsPerspective();
 		IWorkbench workbench =PlatformUI.getWorkbench();
 		try {
 			workbench.showPerspective(WebDevelopmentPerspectiveFactory.PERSPECTIVE_ID, workbench.getActiveWorkbenchWindow());
 		} catch (WorkbenchException e) {
 			fail("Cannot show perspective '" +WebDevelopmentPerspectiveFactory.PERSPECTIVE_ID + "'");
 		}
 	}
 	
 	public void testWebProjectsViewIsShowed() {
 		IViewPart webProjectsView = findView(WebProjectsNavigator.VIEW_ID);
 		assertNotNull("Web project Navigator hasn't been loaded",webProjectsView);
 	}
 	
 	public void testPaletteViewIsShowed() {
 		IViewPart paletteView = findView(PaletteViewPart.VIEW_ID);
 		assertNotNull("Palette View hasn't been loaded",paletteView);
 	}
 	
 	// Helper methods 
 	
 	private IViewPart findView(String id) {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		return workbench.getActiveWorkbenchWindow().getActivePage().findView(id);
 	}
 	
 	/*
 	 * 
 	 */
 	private IPerspectiveDescriptor getRhdsPerspective() {
 		IPerspectiveRegistry reg = getPerspectiveRegistry();
 		return reg.findPerspectiveWithId(WebDevelopmentPerspectiveFactory.PERSPECTIVE_ID);
 	}
 
 	/*
 	 * 
 	 */
 	private IPerspectiveRegistry getPerspectiveRegistry() {
 		IWorkbench workbench = PlatformUI.getWorkbench();
 		return workbench.getPerspectiveRegistry();
 	}
 }
