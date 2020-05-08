 /*
  * Copyright (c) 2006, 2010 Borland Software Corporation and others
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Dmitry Stadnik (Borland) - initial API and implementation
  *    Artem Tikhomirov (Borland) - tests for Ant build
  */
 package org.eclipse.gmf.tests.gen;
 
 import java.io.File;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.SortedSet;
 import java.util.TreeSet;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathExpression;
 import javax.xml.xpath.XPathFactory;
 
 import org.eclipse.ant.core.AntRunner;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.gmf.codegen.gmfgen.CreateShortcutAction;
 import org.eclipse.gmf.codegen.gmfgen.CustomParser;
 import org.eclipse.gmf.codegen.gmfgen.DynamicModelAccess;
 import org.eclipse.gmf.codegen.gmfgen.GMFGenFactory;
 import org.eclipse.gmf.codegen.gmfgen.GenCommandAction;
 import org.eclipse.gmf.codegen.gmfgen.GenContextMenu;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomAction;
 import org.eclipse.gmf.codegen.gmfgen.GenCustomPreferencePage;
 import org.eclipse.gmf.codegen.gmfgen.GenDiagram;
 import org.eclipse.gmf.codegen.gmfgen.GenEditorGenerator;
 import org.eclipse.gmf.codegen.gmfgen.GenGroupMarker;
 import org.eclipse.gmf.codegen.gmfgen.GenMenuManager;
 import org.eclipse.gmf.codegen.gmfgen.GenPlugin;
 import org.eclipse.gmf.codegen.gmfgen.GenPreference;
 import org.eclipse.gmf.codegen.gmfgen.GenStandardPreferencePage;
 import org.eclipse.gmf.codegen.gmfgen.LoadResourceAction;
 import org.eclipse.gmf.codegen.gmfgen.StandardPreferencePages;
 import org.eclipse.gmf.internal.bridge.genmodel.InnerClassViewmapProducer;
 import org.eclipse.gmf.mappings.AuditContainer;
 import org.eclipse.gmf.mappings.AuditRule;
 import org.eclipse.gmf.mappings.Constraint;
 import org.eclipse.gmf.mappings.DiagramElementTarget;
 import org.eclipse.gmf.mappings.DomainElementTarget;
 import org.eclipse.gmf.mappings.GMFMapFactory;
 import org.eclipse.gmf.mappings.Mapping;
 import org.eclipse.gmf.mappings.MetricContainer;
 import org.eclipse.gmf.mappings.MetricRule;
 import org.eclipse.gmf.mappings.Severity;
 import org.eclipse.gmf.tests.setup.DiaGenSource;
 import org.eclipse.gmf.tests.setup.RuntimeBasedGeneratorConfiguration;
 import org.eclipse.gmf.tests.setup.annotated.GenASetup;
 import org.eclipse.jdt.core.Flags;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IField;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.w3c.dom.Document;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 /**
  * @author dstadnik
  */
 public class RuntimeCompilationTest extends CompilationTest {
 
 	public RuntimeCompilationTest(String name) {
 		super(name, new RuntimeBasedGeneratorConfiguration(), new InnerClassViewmapProducer());
 	}
 
 	/**
 	 * Pure design diagrams are not supported in lite version.
 	 */
 	public void testCompilePureDesignDiagram() throws Exception {
 		myMapSource.detachFromDomainModel();
 		DiaGenSource gmfGenSource = createLibraryGen(false);
 		gmfGenSource.getGenDiagram().getEditorGen().setPackageNamePrefix("org.eclipse.gmf.examples.library.diagram"); //$NON-NLS-1$
 		generateAndCompile(gmfGenSource);
 	}
 
 	public void testCompileDynamicDomainModel() throws Exception {
 		DiaGenSource s = createLibraryGen(false);
 		final GenEditorGenerator editorGen = s.getGenDiagram().getEditorGen();
 		assertNull("prereq", editorGen.getModelAccess());
 		DynamicModelAccess dma = GMFGenFactory.eINSTANCE.createDynamicModelAccess();
 		editorGen.setModelAccess(dma);
 		generateAndCompile(s, new GenDiagramMutator("dynmodel") {
 			@Override
 			public void doMutation(GenDiagram d) {
 				final DynamicModelAccess modelAccess = d.getEditorGen().getModelAccess();
 				modelAccess.setClassName("NonDefaultDynamicAccessorName");
 			}
 			@Override
 			public void undoMutation(GenDiagram d) {
 				final DynamicModelAccess modelAccess = d.getEditorGen().getModelAccess();
 				modelAccess.setClassName(null);
 			}
 		});
 	}
 
 	public void testPreferencePages() throws Exception {
 		DiaGenSource s = createLibraryGen(false);
 		final GenDiagram gd = s.getGenDiagram();
 		// Part 1: compile all standard
 		GenStandardPreferencePage[] p = new GenStandardPreferencePage[StandardPreferencePages.values().length];
 		assertTrue("sanity", p.length > 5);
 		for (int i = 0; i < p.length; i++) {
 			p[i] = GMFGenFactory.eINSTANCE.createGenStandardPreferencePage();
 			p[i].setClassName("Page" + i);
 			p[i].setKind(StandardPreferencePages.values()[i]);
 			if (i > 0) {
 				p[i-1].getChildren().add(p[i]);
 			}
 		}
 		gd.getPreferencePages().clear();
 		gd.getPreferencePages().add(p[0]);
 		generateAndCompile(s);
 		GenPlugin gp = gd.getEditorGen().getPlugin();
 		IProject generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(gp.getID());
 		IFile generatedManifest = generatedProject.getFile("plugin.xml");
 		assertTrue(generatedManifest.exists());
 		XPathFactory xf = XPathFactory.newInstance();
 		XPathExpression xe = xf.newXPath().compile("/plugin/extension[@point = 'org.eclipse.ui.preferencePages']/page");
 		NodeList result = (NodeList) xe.evaluate(new InputSource(generatedManifest.getContents()), XPathConstants.NODESET);
 		assertEquals(p.length, result.getLength());
 		//
 		// Part 2: generateBoilerplate and subset of pages
 		gp.setID(gp.getID() + ".boilerplateprefpage");
 		gd.getPreferencePages().clear();
 		GenCustomPreferencePage cp1 = GMFGenFactory.eINSTANCE.createGenCustomPreferencePage();
 		cp1.setQualifiedClassName(gd.getPreferencesPackageName() + ".CustomPageNoCodeGenerated");
 		cp1.setGenerateBoilerplate(false);
 		GenCustomPreferencePage cp2 = GMFGenFactory.eINSTANCE.createGenCustomPreferencePage();
 		cp2.setQualifiedClassName(gd.getPreferencesPackageName() + ".CustomPageWithBoilerplateCode");
 		cp2.setGenerateBoilerplate(true);
 		GenStandardPreferencePage onePage = GMFGenFactory.eINSTANCE.createGenStandardPreferencePage();
 		onePage.setKind(StandardPreferencePages.CONNECTIONS_LITERAL);
 		onePage.getChildren().add(cp1);
 		onePage.getChildren().add(cp2);
 		gd.getPreferencePages().add(onePage);
 		generateAndCompile(s);
 		generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(gp.getID());
 		generatedManifest = generatedProject.getFile("plugin.xml");
 		assertTrue(generatedManifest.exists());
 		// check all three have been registered
 		result = (NodeList) xe.evaluate(new InputSource(generatedManifest.getContents()), XPathConstants.NODESET);
 		assertEquals(3, result.getLength());
 		HashSet<String> names = new HashSet<String>();
 		names.add(onePage.getQualifiedClassName());
 		names.add(cp1.getQualifiedClassName());
 		names.add(cp2.getQualifiedClassName());
 		assertTrue("sanity", names.size() == 3);
 		for (int i = 0; i < result.getLength(); i++) {
 			String className = result.item(i).getAttributes().getNamedItem("class").getNodeValue();
 			assertTrue(className, names.remove(className));
 		}
 		assertTrue(names.isEmpty());
 		// check real files
 		IFile file_sp = generatedProject.getFile("/src/" + onePage.getQualifiedClassName().replace('.', '/') + ".java");
 		IFile file_cp1 = generatedProject.getFile("/src/" + cp1.getQualifiedClassName().replace('.', '/') + ".java");
 		IFile file_cp2 = generatedProject.getFile("/src/" + cp2.getQualifiedClassName().replace('.', '/') + ".java");
 		assertTrue(file_sp.exists());
 		assertTrue(file_cp2.exists());
 		assertFalse(file_cp1.exists());
 	}
 
 	public void testCustomActions() throws Exception {
 		DiaGenSource s = createLibraryGen(false);
 		final GenEditorGenerator editorGen = s.getGenDiagram().getEditorGen();
 		GenContextMenu menu = GMFGenFactory.eINSTANCE.createGenContextMenu();
 		GenCustomAction a1 = GMFGenFactory.eINSTANCE.createGenCustomAction();
 		GenCustomAction a2 = GMFGenFactory.eINSTANCE.createGenCustomAction();
 		GenCustomAction a3 = GMFGenFactory.eINSTANCE.createGenCustomAction();
 		a1.setGenerateBoilerplate(false);
 		a2.setGenerateBoilerplate(true);
 		a3.setGenerateBoilerplate(true);
 		a1.setQualifiedClassName("org.sample.actions.Action1");
 		a2.setQualifiedClassName("org.sample.actions.Action2");
 		a3.setQualifiedClassName("org.sample.actions.Action3");
 		a1.setName("testaction-1");
 		a2.setName("testaction-2");
 		a3.setName("testaction-3");
 		GenMenuManager subMenu = GMFGenFactory.eINSTANCE.createGenMenuManager();
 		subMenu.setID("org.sample.submenu");
 		GenGroupMarker gm = GMFGenFactory.eINSTANCE.createGenGroupMarker();
 		gm.setGroupName("group.name");
 		GenCommandAction cmdAction = GMFGenFactory.eINSTANCE.createGenCommandAction();
 		cmdAction.setCommandIdentifier("org.sample.command");
 		subMenu.getItems().add(a3);
 		subMenu.getItems().add(gm);
 		subMenu.getItems().add(cmdAction);
 		menu.getItems().add(a1);
 		menu.getItems().add(GMFGenFactory.eINSTANCE.createGenSeparator());
 		menu.getItems().add(a2);
 		menu.getItems().add(subMenu);
 		editorGen.getContextMenus().clear(); // make sure there's no other (default) menus
 		editorGen.getContextMenus().add(menu);
 		//
 		generateAndCompile(s);
 		//
 		IProject generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(editorGen.getPlugin().getID());
 		IFile generatedManifest = generatedProject.getFile("plugin.xml");
 		assertTrue(generatedManifest.exists());
 		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		Document parsedManifest = db.parse(new InputSource(generatedManifest.getContents()));
 		XPath xf = XPathFactory.newInstance().newXPath();
 		XPathExpression xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.menus']/menuContribution");
 		NodeList result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(2, result.getLength()); // one contribution to global context menu and another for submenu
 		String l1 = result.item(0).getAttributes().getNamedItem("locationURI").getNodeValue();
 		String l2 = result.item(1).getAttributes().getNamedItem("locationURI").getNodeValue();
 		assertEquals(l1, "popup:org.eclipse.gmf.runtime.diagram.ui.DiagramEditorContextMenu");
 		assertEquals(l2, "popup:" + subMenu.getID());
 		//
 		xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.menus']/menuContribution/menu");
 		result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(1, result.getLength());
 		String menuIdAttr = result.item(0).getAttributes().getNamedItem("id").getNodeValue();
 		assertEquals(subMenu.getID(), menuIdAttr);
 		//
 		xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.menus']/menuContribution/command");
 		result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(4, result.getLength());
 		// FIXME assert command contribution goes into correct locationURI
 		//
 		xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.menus']/menuContribution/separator");
 		result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(2, result.getLength());
 		//
 		xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.commands']/command[starts-with(@name,'testaction-')]");
 		result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(3, result.getLength());
 		//
 		xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.handlers']/handler");
 		result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(3, result.getLength());
 		String h1 = result.item(0).getAttributes().getNamedItem("class").getNodeValue();
 		String h2 = result.item(1).getAttributes().getNamedItem("class").getNodeValue();
 		String h3 = result.item(2).getAttributes().getNamedItem("class").getNodeValue();
 		assertEquals(a1.getQualifiedClassName(), h1);
 		assertEquals(a2.getQualifiedClassName(), h2);
 		assertEquals(a3.getQualifiedClassName(), h3);
 
 		// check real files for handlers
 		IFile file_a1 = generatedProject.getFile("/src/" + a1.getQualifiedClassName().replace('.', '/') + ".java");
 		IFile file_a2 = generatedProject.getFile("/src/" + a2.getQualifiedClassName().replace('.', '/') + ".java");
 		IFile file_a3 = generatedProject.getFile("/src/" + a3.getQualifiedClassName().replace('.', '/') + ".java");
 		assertFalse(file_a1.exists());
 		assertTrue(file_a2.exists());
 		assertTrue(file_a3.exists());
 	}
 
 	// CreateShortcut, LoadResource, InitDiagramFileAction 
 	public void testPredefinedActions() throws Exception {
 		DiaGenSource s1 = createLibraryGen(false);
 		final GenEditorGenerator editorGen = s1.getGenDiagram().getEditorGen();
 		GenContextMenu menu = GMFGenFactory.eINSTANCE.createGenContextMenu();
 		menu.getContext().add(s1.getGenDiagram());
 		final CreateShortcutAction createShortcutAction = GMFGenFactory.eINSTANCE.createCreateShortcutAction();
 		final LoadResourceAction loadResourceAction = GMFGenFactory.eINSTANCE.createLoadResourceAction();
 		menu.getItems().add(createShortcutAction);
 		menu.getItems().add(loadResourceAction);
 		editorGen.getContextMenus().clear(); // make sure there's no other (default) menus
 		editorGen.getContextMenus().add(menu);
 		editorGen.getDiagram().getContainsShortcutsTo().add("ecore");
 		assertTrue("sanity", editorGen.getDiagram().generateCreateShortcutAction());
 		//
 		generateAndCompile(s1);
 		//
 		IProject generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(editorGen.getPlugin().getID());
 		IFile generatedManifest = generatedProject.getFile("plugin.xml");
 		assertTrue(generatedManifest.exists());
 		DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 		Document parsedManifest = db.parse(new InputSource(generatedManifest.getContents()));
 		XPath xf = XPathFactory.newInstance().newXPath();
 		XPathExpression xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.menus']/menuContribution/command");
 		NodeList result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertEquals(2, result.getLength());
 		xe = xf.compile("/plugin/extension[@point = 'org.eclipse.ui.commands']/command");
 		result = (NodeList) xe.evaluate(parsedManifest, XPathConstants.NODESET);
 		assertTrue(result.getLength() > 2);
 		HashSet<String> allCommands = new HashSet<String>();
 		for (int i = result.getLength() - 1; i >= 0; i--) {
 			allCommands.add(result.item(i).getAttributes().getNamedItem("defaultHandler").getNodeValue());
 		}
 		assertTrue(allCommands.contains(createShortcutAction.getQualifiedClassName()));
 		assertTrue(allCommands.contains(loadResourceAction.getQualifiedClassName()));
 		IFile file1 = generatedProject.getFile("/src/" + createShortcutAction.getQualifiedClassName().replace('.', '/') + ".java");
 		IFile file2 = generatedProject.getFile("/src/" + loadResourceAction.getQualifiedClassName().replace('.', '/') + ".java");
 		assertTrue(file1.exists());
 		assertTrue(file2.exists());
 		//
 //		DiaGenSource s2 = createLibraryGen(true);
 //		fail("TODO");
 	}
 
 	public void testCustomPreferences() throws Exception {
 		DiaGenSource s = createLibraryGen(false);
 		final GenDiagram gd = s.getGenDiagram();
 		GenCustomPreferencePage pp = GMFGenFactory.eINSTANCE.createGenCustomPreferencePage();
 		if (gd.getPreferencePages().isEmpty()) {
 			gd.getPreferencePages().add(pp);
 		} else {
 			gd.getPreferencePages().get(0).getChildren().add(pp);
 		}
 		pp.setGenerateBoilerplate(true);
 		pp.setName("Page Name");
 		pp.setQualifiedClassName(gd.getEditorGen().getEditor().getPackageName() + ".CustomPreferencePage");
 		GenPreference p1 = GMFGenFactory.eINSTANCE.createGenPreference();
 		p1.setName("PREF_XXX_ONE");
 		p1.setDefaultValue("\"XXX_ONE_DEFAULT\"");
 		GenPreference p2 = GMFGenFactory.eINSTANCE.createGenPreference();
 		p2.setName("NO_PREFIX_XXX_TWO");
 		p2.setKey("KEY.XXX.TWO");
 		pp.getPreferences().add(p1);
 		pp.getPreferences().add(p2);
 		//
 		generateAndCompile(s);
 		//
 		IProject generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(gd.getEditorGen().getPlugin().getID());
 		IFile file_pp = generatedProject.getFile("/src/" + pp.getQualifiedClassName().replace('.', '/') + ".java");
 		assertTrue(file_pp.exists());
 		ICompilationUnit cuPage = (ICompilationUnit) JavaCore.create(file_pp);
 		assertNotNull(cuPage);
 		IType mainClass = cuPage.getTypes()[0];
 		assertNotNull(mainClass);
 		assertEquals(2, mainClass.getFields().length);
 		final IField p1field = mainClass.getField(p1.getName());
 		final IField p2field = mainClass.getField(p2.getName());
 		assertTrue(Flags.isPublic(p1field.getFlags()));
 		assertTrue(Flags.isStatic(p1field.getFlags()));
 		assertTrue(Flags.isPublic(p2field.getFlags()));
 		assertTrue(Flags.isStatic(p2field.getFlags()));
 		assertEquals('"' + p1.getKey() + '"', p1field.getConstant());
 		assertEquals('"' + p2.getKey() + '"', p2field.getConstant());
 		IMethod initMethod = mainClass.getMethod("initDefaults",  new String[] { "Q" + IPreferenceStore.class.getSimpleName() + ";" });
 		assertNotNull(initMethod);
 		String methodText = initMethod.getSource();
 		assertTrue(methodText.indexOf(p1.getName()) != -1);
 		assertTrue(methodText.indexOf(p1.getDefaultValue()) != -1);
 		assertTrue(methodText.indexOf(p2.getName()) == -1);
 	}
 
 	// check CustomParser#isGenerateBoilerplate == true emits java class 
 	public void testCustomParsers() throws Exception {
 		DiaGenSource s = createLibraryGen(false);
 		final GenEditorGenerator gd = s.getGenDiagram().getEditorGen();
 		CustomParser cp1 = GMFGenFactory.eINSTANCE.createCustomParser();
 		cp1.setQualifiedName(gd.getLabelParsers().getImplPackageName() + "CustomParserOne");
 		CustomParser cp2 = GMFGenFactory.eINSTANCE.createCustomParser();
 		cp2.setQualifiedName(gd.getLabelParsers().getImplPackageName() + "CustomParserTwo");
 		cp2.setGenerateBoilerplate(true);
 		gd.getLabelParsers().getImplementations().add(cp1);
 		gd.getLabelParsers().getImplementations().add(cp2);
 		//
 		generateAndCompile(s);
 		//
 		IProject generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(gd.getPlugin().getID());
 		IFile file_cp1 = generatedProject.getFile("/src/" + cp1.getQualifiedName().replace('.', '/') + ".java");
 		IFile file_cp2 = generatedProject.getFile("/src/" + cp2.getQualifiedName().replace('.', '/') + ".java");
 		assertFalse(file_cp1.exists());
 		assertTrue(file_cp2.exists());
 	}
 
	public void testAntScriptEmitsSameStructure() throws Exception {
 		Mapping mapping = myMapSource.getMapping();
 		//
 		// metrics
 		MetricContainer mc = GMFMapFactory.eINSTANCE.createMetricContainer();
 		MetricRule mr = GMFMapFactory.eINSTANCE.createMetricRule();
 		mr.setKey("metric.rule1"); //$NON-NLS-1$
 		// Note: use characters that need to be escaped in java source string literals
 		mr.setName("Name of " + mr.getKey()); //$NON-NLS-1$
 		mr.setDescription("Description of " + mr.getKey()); //$NON-NLS-1$
 		mr.setRule(GMFMapFactory.eINSTANCE.createValueExpression());
 		mr.getRule().setBody("'aaa'.size() + 2");
 		mr.setLowLimit(new Double(2));
 		mr.setHighLimit(new Double(6));
 		DiagramElementTarget diagramElementTarget = GMFMapFactory.eINSTANCE.createDiagramElementTarget();
 		diagramElementTarget.setElement(mapping.getNodes().get(0).getChild());
 		mr.setTarget(diagramElementTarget);
 		mc.getMetrics().add(mr);
 		mapping.setMetrics(mc);
 		//
 		// audits
 		AuditContainer ac = GMFMapFactory.eINSTANCE.createAuditContainer();
 		ac.setId("ac1"); //$NON-NLS-1$
 		ac.setName(ac.getId());
 		AuditRule ar = GMFMapFactory.eINSTANCE.createAuditRule();
 		String ar_id = "audit.rule1"; 
 		ar.setId(ar_id);
 		ar.setName("Name of " + ar_id); //$NON-NLS-1$
 		ar.setMessage("Violation of " + ar_id); //$NON-NLS-1$
 		ar.setDescription("Description of " + ar_id); //$NON-NLS-1$
 		DomainElementTarget classLibrary = GMFMapFactory.eINSTANCE.createDomainElementTarget();
 		classLibrary.setElement(mapping.getDiagram().getDomainMetaElement()); 	
 		ar.setTarget(classLibrary);
 		Constraint rule = GMFMapFactory.eINSTANCE.createConstraint();
 		// body is not essential, just to look nice
 		rule.setBody("Library.allInstances()->size() > 0"); //$NON-NLS-1$
 		ar.setRule(rule);
 		ar.setSeverity(Severity.ERROR_LITERAL);
 		ar.setUseInLiveMode(true);
 		ac.getAudits().add(ar);
 		mapping.setAudits(ac);
 		DiaGenSource s = new GenASetup(mapping, myViewmapProducer, false);
 		//
 		// validation
 		s.getGenDiagram().setValidationEnabled(true); // although presence of audits effectively does the same
 		//
 		// shortcuts
 		s.getGenDiagram().getContainsShortcutsTo().add("ecore");
 		s.getGenDiagram().getShortcutsProvidedFor().add("ecore");
 		s.getGenDiagram().eResource().save(null);
 		
 		testAntScriptEmitsSameStructure(s);
 	}
 
	public void testAntScriptEmitsSameStructure_rcp() throws Exception {
 		testAntScriptEmitsSameStructure(createLibraryGen(true));
 	}
 
 	private void testAntScriptEmitsSameStructure(DiaGenSource s) throws Exception {
 		System.out.println(s.getGenDiagram().eResource().getURI().toString());
 		URL scriptLocation = FileLocator.find(Platform.getBundle("org.eclipse.gmf.xpand.ant"), new Path("/examples/gmfgen-run.xml"), null);
 		assertNotNull(scriptLocation);
 		AntRunner r = new AntRunner();
 		r.setBuildFileLocation(FileLocator.toFileURL(scriptLocation).getFile());
 		File antOutputRoot = File.createTempFile("aaa", "");
 		antOutputRoot.delete();
 		antOutputRoot = new File(antOutputRoot.getParentFile(), antOutputRoot.getName());
 		antOutputRoot.mkdir();
 		r.setArguments("-Dinput-gmfgen-model-uri=" + s.getGenDiagram().eResource().getURI().toString() + "#/ -DoutputRoot=" + antOutputRoot.getAbsolutePath());
 		r.run(new NullProgressMonitor());
 		//
 		FileCollector antResult = new FileCollector();
 		FileCollector javaResult = new FileCollector();
 		final String[] filters = { ".*\\.java$", ".*\\.properties$", "MANIFEST\\.MF$", "plugin\\.xml$", "\\.options$", ".*\\.gif$" };
 		antResult.addNameFilter(filters);
 		javaResult.addNameFilter(filters);
 		//
 		System.out.println(antOutputRoot);
 		TreeSet<String> antResultFileNames = new TreeSet<String>();
 		for (File f : antResult.collect(antOutputRoot)) {
 			antResultFileNames.add(f.getPath().substring(antOutputRoot.getPath().length()));
 		}
 		generateAndCompile(s, (GenDiagramMutator[]) null);
 		//
 		IProject generatedProject = ResourcesPlugin.getWorkspace().getRoot().getProject(s.getGenDiagram().getEditorGen().getPlugin().getID());
 		File javaOutputRoot = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
 		javaResult.collect(generatedProject.getLocation().toFile());
 		boolean missingFiles = false, excessiveFiles = false;
 		for (File f : javaResult.getResult()) {
 			String javaResultFile = f.getPath().substring(javaOutputRoot.getPath().length());
 			if (!antResultFileNames.remove(javaResultFile)) {
 				missingFiles = true;
 				System.err.println("Missing file: " + javaResultFile);
 			}
 		}
 		for (String a : antResultFileNames) {
 			excessiveFiles = true;
 			System.out.println("Excessive file generated by ANT: " + a);
 		}
 		assertFalse("Few files are not generated by Ant codegen script", missingFiles); // TODO provide list of missing files as part of the message
 		assertFalse("Ant codegen script produces excessive files", excessiveFiles); // TODO list of files in the message
 	}
 
 	static class FileCollector {
 		private final TreeSet<File> myResult;
 		private boolean myIsRecursive = true;
 		private final List<Pattern> myFilters = new LinkedList<Pattern>();
 
 		public FileCollector() {
 			myResult = new TreeSet<File>();
 		}
 
 		public void addNameFilter(String... nameFilter) {
 			for (String f : nameFilter) {
 				myFilters.add(Pattern.compile(f));
 			}
 		}
 		
 		public void setRecursive(boolean recurseIntoSubfolders) {
 			myIsRecursive = recurseIntoSubfolders;
 		}
 
 		public SortedSet<File> getResult() {
 			return Collections.unmodifiableSortedSet(myResult);
 		}
 
 		public SortedSet<File> collect(File dir) {
 			if (!dir.isDirectory()) {
 				throw new IllegalArgumentException();
 			}
 			LinkedList<File> queue = new LinkedList<File>(Arrays.asList(dir.listFiles()));
 			while (!queue.isEmpty()) {
 				File f = queue.removeFirst();
 				if (f.isDirectory() && myIsRecursive) {
 					queue.addAll(Arrays.asList(f.listFiles()));
 					continue;
 				}
 				if (f.isFile()) {
 					if (myFilters.isEmpty()) {
 						myResult.add(f);
 						continue;
 					} else {
 						for (Pattern p : myFilters) {
 							if (p.matcher(f.getName()).matches()) {
 								myResult.add(f);
 								break; // continue outer cycle
 							}
 						}
 					}
 				}
 			}
 			return getResult();
 		}
 	}
 }
