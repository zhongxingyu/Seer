 /*******************************************************************************
  * Copyright (c) 2009, 2010 Sven Kiera
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpunit.ui.actions;

 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.action.IAction;
 import org.phpsrc.eclipse.pti.core.jobs.MutexRule;
 import org.phpsrc.eclipse.pti.tools.phpunit.core.PHPUnit;
 import org.phpsrc.eclipse.pti.tools.phpunit.validator.PHPUnitValidator;
 import org.phpsrc.eclipse.pti.ui.Logger;
 import org.phpsrc.eclipse.pti.ui.actions.ResourceAction;
 
 public class RunTestCaseAction extends ResourceAction {
 
 	final MutexRule rule = new MutexRule();
 
 	public void run(IAction action) {
 		final IResource[] resources = getSelectedResources();
 		if (resources.length > 0) {
 			Job job = new Job("PHPUnit") {
 				protected IStatus run(IProgressMonitor monitor) {
 					monitor.beginTask("Run Test", resources.length);
 
 					PHPUnitValidator validator = new PHPUnitValidator();
 					int worked = 0;
 					for (IResource resource : resources) {
 						monitor.subTask("Run " + resource.getProjectRelativePath().toString());
 
 						if (resource instanceof IFolder) {
 							validator.validateFolder((IFolder) resource);
 						} else if (resource instanceof IFile) {
 							IFile file = (IFile) resource;
 							if (PHPUnit.isTestSuite(file)) {
 								validator.validateTestSuite(file);
 							} else {
 								IFile testCaseFile = PHPUnit.searchTestCase(file);
 								if (testCaseFile != null) {
									validator.validateTestCase(testCaseFile);
 								} else {
 									Logger.logToConsole("Error: No test case / test suite found for file "
 											+ file.getProjectRelativePath().toString());
 								}
 							}
 						}
 
 						monitor.worked(++worked);
 					}
 
 					return Status.OK_STATUS;
 				}
 			};
 			job.setRule(rule);
 			job.setUser(false);
 			job.schedule();
 		}
 	}
 }
