 /******************************************************************************* 
  * Copyright (c) 2012 Red Hat, Inc. 
  * Distributed under license by Red Hat, Inc. All rights reserved. 
  * This program is made available under the terms of the 
  * Eclipse Public License v1.0 which accompanies this distribution, 
  * and is available at http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  * Red Hat, Inc. - initial API and implementation 
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.kb.test;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Path;
import org.jboss.tools.common.el.core.resolver.ELContext;
 import org.jboss.tools.common.el.core.resolver.Var;
 import org.jboss.tools.jst.web.kb.IKbProject;
 import org.jboss.tools.jst.web.kb.PageContextFactory;
 import org.jboss.tools.jst.web.kb.internal.scanner.LoadedDeclarations;
 import org.jboss.tools.jst.web.kb.internal.scanner.ScannerException;
 import org.jboss.tools.jst.web.kb.internal.scanner.XMLScanner;
 import org.jboss.tools.jst.web.kb.internal.taglib.ELFunction;
 import org.jboss.tools.jst.web.kb.taglib.IELFunction;
 import org.jboss.tools.jst.web.kb.taglib.IFunctionLibrary;
 import org.jboss.tools.jst.web.kb.taglib.ITagLibrary;
 import org.jboss.tools.test.util.JUnitUtils;
 
 public class IncludeModelTest extends TestCase {
 	IProject project = null;
 	boolean makeCopy = true;
 
 	public IncludeModelTest() {
 		super("Kb Include Model Test");
 	}
 
 	public void setUp() throws Exception {
 		project = ResourcesPlugin.getWorkspace().getRoot().getProject("TestKbModel");
 		assertNotNull("Can't load TestKbModel", project); //$NON-NLS-1$
 	}
 
 	private IKbProject getKbProject() {
 		IKbProject kbProject = null;
 		try {
 			kbProject = (IKbProject)project.getNature(IKbProject.NATURE_ID);
 		} catch (Exception e) {
 			JUnitUtils.fail("Cannot get seam nature.",e);
 		}
 		return kbProject;
 	}
 
 	public void testIncludeModel() {
 		IKbProject kbProject = getKbProject();
 		
 		IFile f = project.getFile("WebContent/pages/params/page1.xhtml");
 		assertTrue(f.exists());
 		PageContextFactory.createPageContext(f);
 		
 		f = project.getFile("WebContent/pages/params/page2.xhtml");
 		assertTrue(f.exists());
 		PageContextFactory.createPageContext(f);
 
 		List<Var> vars = kbProject.getIncludeModel().getVars(new Path("/TestKbModel/WebContent/pages/params/template.xhtml"));
 		assertEquals(6, vars.size());
		
		IFile f1 = project.getFile("WebContent/pages/params/template.xhtml");
		ELContext context = PageContextFactory.createPageContext(f1);
		Var[] vars1 = context.getVars(0);
		assertEquals(6, vars1.length);
		
 	}
 
 }
