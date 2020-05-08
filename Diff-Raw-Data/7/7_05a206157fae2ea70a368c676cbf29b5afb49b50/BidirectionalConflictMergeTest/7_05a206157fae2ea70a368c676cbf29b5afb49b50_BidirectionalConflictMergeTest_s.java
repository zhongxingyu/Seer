 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.client.test.ui.conflictdetection;
 
 import static org.eclipse.swtbot.swt.finder.matchers.WidgetMatcherFactory.withText;
 import static org.eclipse.swtbot.swt.finder.waits.Conditions.waitForShell;
 
 import java.util.concurrent.Callable;
 
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.handler.ESOperationModifier;
 import org.eclipse.emf.emfstore.client.test.common.dsl.Create;
 import org.eclipse.emf.emfstore.client.test.common.util.ProjectUtil;
 import org.eclipse.emf.emfstore.client.test.ui.controllers.AbstractUIControllerTestWithCommit;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.common.model.ESModelElementId;
 import org.eclipse.emf.emfstore.internal.client.model.impl.AutoOperationWrapper;
 import org.eclipse.emf.emfstore.internal.client.ui.controller.UIUpdateProjectController;
 import org.eclipse.emf.emfstore.internal.common.ExtensionRegistry;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.emf.emfstore.test.model.TestElement;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
 import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
 import org.eclipse.swtbot.swt.finder.results.VoidResult;
 import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.hamcrest.Matcher;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
 /**
  * Tests:
  * UpdateProjectController
  * CheckoutController
  * 
  * @author emueller
  * 
  */
 @RunWith(SWTBotJunit4ClassRunner.class)
 public class BidirectionalConflictMergeTest extends AbstractUIControllerTestWithCommit {
 
 	@Override
 	// @Ignore
 	@Test
 	public void testController() throws ESException {
 
 		ExtensionRegistry.INSTANCE.set(
 			ESOperationModifier.ID,
 			new AutoOperationWrapper());
 
 		final TestElement testElement = Create.testElement("ELEMENT 1");
 		final TestElement testElement2 = Create.testElement("ELEMENT 2");
 		final TestElement testElement3 = Create.testElement("ELEMENT 3");
 
 		final TestElement child = Create.testElement("CHILD");
 		testElement.getContainedElements2().add(child);
 
 		ProjectUtil.addElement(localProject, testElement);
 		ProjectUtil.addElement(localProject, testElement2);
 		ProjectUtil.addElement(localProject, testElement3);
 
 		commit();
 
 		checkout();
 
 		changeContainer(localProject,
 			localProject.getModelElementId(child),
 			localProject.getModelElementId(testElement2));
 		commit();
 
 		changeContainer(getCopy(),
 			localProject.getModelElementId(child),
 			localProject.getModelElementId(testElement3));
 
 		UIThreadRunnable.asyncExec(new VoidResult() {
 			public void run() {
 				final UIUpdateProjectController updateProjectController = new UIUpdateProjectController(
 					bot.getDisplay().getActiveShell(),
 					getCopy());
 				updateProjectController.execute();
 			}
 		});
 		final Matcher<Shell> matcher = withText("Update");
 		bot.waitUntil(waitForShell(matcher));
 		bot.button("OK").click();
 
 		bot.waitUntil(new DefaultCondition() {
 
 			public boolean test() throws Exception {
 				return bot.shell("Merge Wizard") != null;
 			}
 
 			public String getFailureMessage() {
 				return "Merge wizard did not appear";
 			}
 		});
 
 		final SWTBotShell shell2 = bot.shell("Merge Wizard");
 		shell2.bot().button(1).click();
 		shell2.bot().button(4).click();
 
 	}
 
 	protected void changeContainer(final ESLocalProject localProject, final ESModelElementId childId,
 		final ESModelElementId newParentId) {
 		final TestElement child = (TestElement) localProject.getModelElement(childId);
 		final TestElement newParent = (TestElement) localProject.getModelElement(newParentId);
		newParent.getContainedElements2().add(child);
 
 	}
 
 	protected void clearContainedElements(final ESLocalProject localProject) {
 
 		final TestElement testElement = (TestElement) localProject.getModelElements().get(0);
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				testElement.getContainedElements2().clear();
 				return null;
 			}
 		});
 
 	}
 }
