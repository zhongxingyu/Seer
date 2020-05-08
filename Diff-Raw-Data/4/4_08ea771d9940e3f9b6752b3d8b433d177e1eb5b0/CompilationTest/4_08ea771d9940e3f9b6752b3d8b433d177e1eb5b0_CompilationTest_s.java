 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.gen;
 
 import java.io.IOException;
 import java.net.URL;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenLink;
 import org.eclipse.gmf.codegen.gmfgen.GenNode;
 import org.eclipse.gmf.tests.Plugin;
 import org.eclipse.gmf.tests.SessionSetup;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 import org.eclipse.gmf.tests.setup.GenProjectBaseSetup;
 
 public class CompilationTest extends TestCase {
 
 	public CompilationTest(String name) {
 		super(name);
 	}
 
 	// TODO EditPartViewer[Source|Setup]
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		SessionSetup.getRuntimeWorkspaceSetup();
 	}
 
 	public void testCodeCompilation() {
 		try {
 			URL gmfgenURL = Plugin.getInstance().getBundle().getEntry("/models/library/library.gmfgen");
 			String filePath = Platform.asLocalURL(gmfgenURL).toExternalForm();
 			URI selected = URI.createURI(filePath);
 			ResourceSet srcResSet = new ResourceSetImpl();
 	 		Resource srcRes = srcResSet.getResource(selected, true);
 			GenDiagram gd = (GenDiagram) srcRes.getContents().get(0);
 			new GenProjectBaseSetup().generateAndCompile(SessionSetup.getRuntimeWorkspaceSetup(), new FakeDiaGenSource(gd, null, null));
 		} catch (IOException ex) {
 			fail(ex.getMessage());
 		} catch (Exception ex) {
 			fail("Hm, looks like unexpected..." + ex.getMessage());
 		}
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	private static class FakeDiaGenSource implements DiaGenSource {
 		private final GenDiagram genDiagram;
 		private final GenNode genNode;
 		private final GenLink genLink;
 		FakeDiaGenSource(GenDiagram gd, GenNode gn, GenLink gl) {
 			genDiagram = gd;
 			genNode = gn;
 			genLink = gl;
 		}
 		public GenDiagram getGenDiagram() {
 			return genDiagram;
 		}
 		public GenLink getGenLink() {
 			return genLink;
 		}
 		public GenNode getGenNode() {
 			return genNode;
 		}
 	}
 }
