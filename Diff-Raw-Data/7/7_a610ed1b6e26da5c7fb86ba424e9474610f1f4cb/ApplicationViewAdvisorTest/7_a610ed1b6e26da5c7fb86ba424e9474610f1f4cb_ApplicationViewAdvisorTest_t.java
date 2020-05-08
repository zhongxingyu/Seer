 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.views;
 
 import org.easymock.EasyMock;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.application.ActionBarAdvisor;
 import org.eclipse.ui.application.IActionBarConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.core.wire.Wire;
 import org.eclipse.riena.internal.core.test.RienaTestCase;
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.internal.navigation.ui.swt.IAdvisorHelper;
import org.eclipse.riena.navigation.IModuleGroupNode;
 import org.eclipse.riena.navigation.IModuleNode;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.NavigationNodeId;
 import org.eclipse.riena.navigation.model.ApplicationNode;
import org.eclipse.riena.navigation.model.ModuleGroupNode;
 import org.eclipse.riena.navigation.model.ModuleNode;
 import org.eclipse.riena.navigation.model.SubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.ApplicationController;
 import org.eclipse.riena.ui.swt.utils.SWTBindingPropertyLocator;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.riena.ui.workarea.IWorkareaDefinition;
 import org.eclipse.riena.ui.workarea.WorkareaManager;
 
 /**
  * Tests of the class <code>ApplicationViewAdvisor</code>.
  */
 @UITestCase
 public class ApplicationViewAdvisorTest extends RienaTestCase {
 
 	private ApplicationViewAdvisor advisor;
 	private IWorkbenchWindowConfigurer winConfig;
 	private ApplicationNode applicationNode;
 	private ApplicationController controller;
 
 	@Override
 	protected void setUp() throws Exception {
 		winConfig = EasyMock.createNiceMock(IWorkbenchWindowConfigurer.class);
 		applicationNode = new ApplicationNode();
 		controller = new ApplicationController(applicationNode);
 		IAdvisorHelper factory = EasyMock.createMock(IAdvisorHelper.class);
 		advisor = new ApplicationViewAdvisor(winConfig, controller, factory);
 		Wire.instance(advisor).andStart(getContext());
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		applicationNode = null;
 		controller = null;
 		winConfig = null;
 		advisor.dispose();
 		advisor = null;
 	}
 
 	/**
 	 * Tests the method <code>initShell</code>.
 	 */
 	public void testInitShell() {
 		Shell shell = new Shell();
 		assertNotSame(SWT.INHERIT_FORCE, shell.getBackgroundMode());
 		Point defMinSize = shell.getMinimumSize();
 
 		ReflectionUtils.invokeHidden(advisor, "initShell", shell);
 
 		assertFalse(defMinSize.equals(shell.getMinimumSize()));
 		assertNotNull(shell.getData(SWTBindingPropertyLocator.BINDING_PROPERTY));
 
 		SwtUtilities.disposeWidget(shell);
 	}
 
 	/**
 	 * Tests the <i>private</i> method <code>initApplicationSize</code>.
 	 */
 	public void testInitApplicationSize() {
 		System.setProperty("riena.application.width", "9000");
 		System.setProperty("riena.application.height", "8000");
 		EasyMock.reset(winConfig);
 		winConfig.setInitialSize(new Point(9000, 8000));
 		EasyMock.replay(winConfig);
 		ReflectionUtils.invokeHidden(advisor, "initApplicationSize", winConfig);
 		EasyMock.verify(winConfig);
 
 		System.setProperty("riena.application.width", "90");
 		System.setProperty("riena.application.height", "80");
 		EasyMock.reset(winConfig);
 		winConfig.setInitialSize(new Point(800, 600));
 		EasyMock.replay(winConfig);
 		ReflectionUtils.invokeHidden(advisor, "initApplicationSize", winConfig);
 		EasyMock.verify(winConfig);
 	}
 
 	/**
 	 * Tests that an IAdvisorFactory is used to create the ActionBarAdvisor.
 	 */
 	public void testInvokesAdvisorFactory() {
 		IActionBarConfigurer actionConfig = EasyMock.createNiceMock(IActionBarConfigurer.class);
 		ActionBarAdvisor actionAdvisor = new ActionBarAdvisor(actionConfig);
 
 		IAdvisorHelper factory = EasyMock.createMock(IAdvisorHelper.class);
 		EasyMock.expect(factory.createActionBarAdvisor(actionConfig)).andReturn(actionAdvisor);
 		EasyMock.replay(factory);
 
 		advisor = new ApplicationViewAdvisor(winConfig, controller, factory);
 		advisor.createActionBarAdvisor(actionConfig);
 
 		EasyMock.verify(factory);
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code prepare(INavigationNode<?>)}.
 	 */
 	public void testPrepare() {
 
		IModuleGroupNode moduleGroup = new ModuleGroupNode(new NavigationNodeId("mg"));
 		IModuleNode module = new ModuleNode(new NavigationNodeId("m"));
		moduleGroup.addChild(module);
 		ISubModuleNode subModule1 = new SubModuleNode(new NavigationNodeId("sm1"));
 		module.addChild(subModule1);
 		ISubModuleNode subModule2 = new SubModuleNode(new NavigationNodeId("sm2"));
 		module.addChild(subModule2);
 		IWorkareaDefinition def = WorkareaManager.getInstance().registerDefinition(subModule1, "dummy1");
 		def.setRequiredPreparation(false);
 		def = WorkareaManager.getInstance().registerDefinition(subModule2, "dummy2");
 		def.setRequiredPreparation(true);
 
 		ReflectionUtils.invokeHidden(advisor, "prepare", module);
 		assertFalse(module.isPrepared());
 		assertFalse(subModule1.isPrepared());
 		assertFalse(subModule1.isActivated());
 		assertTrue(subModule2.isPrepared());
 
 		def = WorkareaManager.getInstance().registerDefinition(subModule1, "dummy1");
 		def.setRequiredPreparation(true);
 		subModule1.activate();
 		ReflectionUtils.invokeHidden(advisor, "prepare", module);
 		assertFalse(module.isPrepared());
 		assertFalse(subModule1.isPrepared());
 		assertTrue(subModule1.isActivated());
 		assertTrue(subModule2.isPrepared());
 
 	}
 
 }
