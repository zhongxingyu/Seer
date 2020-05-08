 /*******************************************************************************
  * Copyright (c) 2011 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.test;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.jboss.tools.common.base.test.validation.TestUtil;
 import org.jboss.tools.common.validation.ValidatorManager;
 import org.jboss.tools.jst.web.WebUtils;
 import org.jboss.tools.jst.web.validation.WebXMLCoreValidator;
 import org.jboss.tools.test.util.JobUtils;
 import org.jboss.tools.test.util.ProjectImportTestSetup;
 import org.jboss.tools.test.util.ResourcesUtils;
 
 /**
  * @author Alexey Kazakov
  */
 public class WebXMLValidationTest extends TestCase {
 
 	private IProject project;
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		project = ProjectImportTestSetup.loadProject(JstWebAllTests.PROJECT_NAME);
 		IResource target = project.getFolder("target");
 		target.setDerived(true, null);
 	}
 
 	public void testValidationInDerived() throws Exception {
 		//Check first that original web.xml contains errors. 
 		String path0 = "WebContent/WEB-INF/web.xml";
 		IFile webxml = project.getFile(path0);
 		assertTrue(webxml.exists());
 		IMarker[] markers = webxml.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(markers.length > 0);
 		
 		//Now we will copy that original web.xml to different folders and check incremental validation.
 
 		//Check that file added to a non-derived folder is validated incrementally.
 		String path1 = "aFolder/WEB-INF/web.xml";
 		IFile webxml1 = project.getFile(path1);
 		replaceFile(project, path0, path1);
 		markers = webxml1.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(markers.length > 0);
 		for (IMarker m: markers) {
 			System.out.println(m.getAttribute(IMarker.MESSAGE));
 		}
 
 		//Check that added to a derived folder, file will be skipped at incremental validation.
 		String path2 = "target/m2e-wtp/web-resources/WEB-INF/web.xml";
 		IFile webxml2 = project.getFile(path2);
 		replaceFile(project, path0, path2);
 		markers = webxml2.findMarkers(WebXMLCoreValidator.PROBLEM_TYPE, false, IResource.DEPTH_ZERO);
 		assertTrue(markers.length == 0);
 	}
 
 	public static void replaceFile(IProject project, String sourcePath, String targetPath) throws CoreException {
 		boolean saveAutoBuild = ResourcesUtils.setBuildAutomatically(false);
 		try {
 			IFile target = project.getFile(new Path(targetPath));
 			IFile source = project.getFile(new Path(sourcePath));
 			assertTrue(source.exists());
 			ValidatorManager.setStatus(ValidatorManager.RUNNING);
 			if(!target.exists()) {
 				target.create(source.getContents(), true, new NullProgressMonitor());
 			} else {
 				target.setContents(source.getContents(), true, false, new NullProgressMonitor());
 			}
 			TestUtil.validate(target);
 		} finally {
 			ResourcesUtils.setBuildAutomatically(saveAutoBuild);
 			JobUtils.waitForIdle();
 		}
 	}
 
 }
