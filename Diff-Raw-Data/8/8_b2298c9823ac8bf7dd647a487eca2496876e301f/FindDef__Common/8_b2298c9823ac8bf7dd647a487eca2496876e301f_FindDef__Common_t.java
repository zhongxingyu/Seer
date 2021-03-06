 /*******************************************************************************
  * Copyright (c) 2008, 2011 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Bruno Medeiros - initial API and implementation
  *******************************************************************************/
 package dtool.tests.ref;
 
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertEquals;
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertFail;
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertNotNull;
 import static melnorme.utilbox.core.Assert.AssertNamespace.assertTrue;
 
 import java.util.Collection;
 
 import mmrnmhrm.core.codeassist.DeeSelectionEngine;
 import mmrnmhrm.tests.ITestResourcesConstants;
 import mmrnmhrm.tests.SampleMainProject;
 
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 
 import dtool.ast.ASTNeoNode;
 import dtool.ast.ASTNodeFinder;
 import dtool.ast.definitions.DefUnit;
 import dtool.ast.definitions.Module;
 import dtool.ast.references.Reference;
 import dtool.refmodel.NodeUtil;
 
 public class FindDef__Common {
 	
 	public static int counter = -666;
 	
 	protected static void staticClassInit(String testfile) {
 		counter = -1;
 		System.out.println("======== " + testfile + " ========");
 	}
 	
 	
 	public static String testdataRefsPath(String testfile) {
 		return ITestResourcesConstants.TR_REFS +"/"+ testfile;
 	}
 	
 	protected Module sourceModule;
 	protected int offset;
 	protected Module targetModule;
 	protected int targetOffset;
 	
 	
 	protected void prepSameModuleTest(String testdataFilePath) {
 		sourceModule = parseNeoModuleNode(testdataFilePath); 
 		targetModule = null;
 	}
 	
 	protected static Module parseNeoModuleNode(String filepath) {
 		ISourceModule sourceModuleDLTK = SampleMainProject.getSourceModule(filepath);
 		return SampleMainProject.parsedDeeModule(sourceModuleDLTK).neoModule;
 	}
 	
 	
 	protected int getMarkerEndOffset(String marker) throws ModelException {
 		String source = sourceModule.getModuleUnit().getSource();
 		return source.indexOf(marker) + marker.length();
 	}
 	
 	protected int getMarkerStartOffset(String marker) throws ModelException {
 		String source = sourceModule.getModuleUnit().getSource();
 		return source.indexOf(marker);
 	}
 	
 	
 	public static void assertFindReF(Module srcMod, int offset, Module targetMod, int targetOffset)
 			throws ModelException {
 		
 		counter++;
 		
 		Module newSrcModule = SampleMainProject.parsedDeeModule(srcMod.getModuleUnit()).neoModule;
 		ASTNeoNode node = ASTNodeFinder.findElement(newSrcModule, offset);
 		Reference ref = (Reference) node;
 		
 		Collection<DefUnit> defunits = ref.findTargetDefUnits(true);
 		
 		if(defunits == null || defunits.isEmpty()) {
 			if(targetOffset == -1)
 				return; // Ok, it matches the expected
 			assertFail(" Find Ref got no DefUnit.");
 		}
 		DefUnit defunit = defunits.iterator().next();
 		
 		assertNotNull(defunit);
 		
 		Module obtainedModule = NodeUtil.getParentModule(defunit);
 		assertTrue(equalModule(targetMod, obtainedModule),
 				" Find Ref got wrong target module.");
 		
 		assertTrue(defunit.defname.getStartPos() == targetOffset,
 				" Find Ref went to wrong offset: " + defunit.defname.getStartPos());
 		
 		
 		testDeeSelectionEngine(srcMod, offset, defunit);
 	}
 	
 	public static void testDeeSelectionEngine(Module srcMod, int offset, DefUnit defunit) {
 		DeeSelectionEngine selectionEngine = new DeeSelectionEngine();
 		IModelElement[] select = selectionEngine.select((IModuleSource) srcMod.getModuleUnit(), offset, offset-1);
		assertTrue(select.length >= 1);
 		IModelElement modelElement = select[0];
		for (int i = 1; i < select.length; i++) {
			assertEquals(modelElement.getElementName(), select[i].getElementName());
			assertEquals(modelElement.getParent(), select[i].getParent());
		}
		
 		while(true) {
 			assertNotNull(modelElement);
 			if(modelElement.getElementType() == IModelElement.SOURCE_MODULE) {
 				assertTrue(defunit == null);
 				break;
 			}
 			assertEquals(defunit.getName(), modelElement.getElementName());
 			defunit = NodeUtil.getOuterDefUnit(defunit);
 			modelElement = modelElement.getParent();
 		}
 	}
 	
 	private static boolean equalModule(Module targetMod, Module obtainedModule) {
 		return targetMod == obtainedModule || targetMod.getModuleUnit().equals(obtainedModule.getModuleUnit());
 	}
 	
 }
