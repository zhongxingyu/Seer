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
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.jboss.tools.jst.web.WebUtils;
 
 /**
  * @author Alexey Kazakov
  */
 public class WebUtilTest extends TestCase {
 
 	private IProject project;
 
 	/* (non-Javadoc)
 	 * @see junit.framework.TestCase#setUp()
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		project = ResourcesPlugin.getWorkspace().getRoot().getProject(JstWebAllTests.PROJECT_NAME);
 	}
 
 	/**
 	 * See https://issues.jboss.org/browse/JBIDE-9766
 	 * @throws Exception
 	 */
 	public void testGetWebRootFolders() throws Exception {
		assertEquals(1, WebUtils.getWebRootFolders(project));
		assertEquals(2, WebUtils.getWebRootFolders(project, false));
 	}
 }
