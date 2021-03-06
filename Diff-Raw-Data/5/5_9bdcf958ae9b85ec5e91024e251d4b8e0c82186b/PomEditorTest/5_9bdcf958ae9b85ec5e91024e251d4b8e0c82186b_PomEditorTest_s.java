 /*******************************************************************************
  * Copyright (c) 2008 Sonatype, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.maven.ide.eclipse.editor.pom;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.Writer;
 
 import org.apache.maven.model.Model;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StyledText;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IPerspectiveDescriptor;
 import org.eclipse.ui.IPerspectiveRegistry;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPage;
 import org.eclipse.ui.IWorkbenchPreferenceConstants;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.internal.IPreferenceConstants;
 import org.eclipse.ui.internal.WorkbenchPlugin;
 import org.eclipse.ui.internal.util.PrefUtil;
 import org.eclipse.ui.part.FileEditorInput;
 import org.eclipse.wst.sse.ui.StructuredTextEditor;
 import org.maven.ide.eclipse.MavenPlugin;
 import org.maven.ide.eclipse.core.IMavenConstants;
 import org.maven.ide.eclipse.project.IProjectConfigurationManager;
 import org.maven.ide.eclipse.project.ProjectImportConfiguration;
 
 import com.windowtester.finder.swt.ShellFinder;
 import com.windowtester.runtime.IUIContext;
 import com.windowtester.runtime.WT;
 import com.windowtester.runtime.WaitTimedOutException;
 import com.windowtester.runtime.WidgetSearchException;
 import com.windowtester.runtime.condition.HasTextCondition;
 import com.windowtester.runtime.condition.IConditionMonitor;
 import com.windowtester.runtime.condition.IHandler;
 import com.windowtester.runtime.locator.WidgetReference;
 import com.windowtester.runtime.swt.UITestCaseSWT;
 import com.windowtester.runtime.swt.condition.eclipse.FileExistsCondition;
 import com.windowtester.runtime.swt.condition.shell.ShellDisposedCondition;
 import com.windowtester.runtime.swt.condition.shell.ShellShowingCondition;
 import com.windowtester.runtime.swt.internal.condition.NotCondition;
 import com.windowtester.runtime.swt.internal.condition.eclipse.DirtyEditorCondition;
 import com.windowtester.runtime.swt.locator.ButtonLocator;
 import com.windowtester.runtime.swt.locator.CTabItemLocator;
 import com.windowtester.runtime.swt.locator.NamedWidgetLocator;
 import com.windowtester.runtime.swt.locator.SWTWidgetLocator;
 import com.windowtester.runtime.swt.locator.TableItemLocator;
 import com.windowtester.runtime.swt.locator.TreeItemLocator;
 import com.windowtester.runtime.swt.locator.eclipse.ViewLocator;
 
 
 /**
  * @author Eugene Kuleshov
  * @author Anton Kraev 
  */
 public class PomEditorTest extends UITestCaseSWT {
 
   private static final String FIND_REPLACE = "Find/Replace";
 
 	static final String TEST_POM_POM_XML = "test-pom/pom.xml";
 	
   static final String TAB_POM_XML = null;  // "pom.xml";
   static final String TAB_OVERVIEW = IMavenConstants.PLUGIN_ID + ".pom.overview"; // "Overview";
   static final String TAB_DEPENDENCIES = IMavenConstants.PLUGIN_ID + ".pom.dependencies";
   static final String TAB_REPOSITORIES = IMavenConstants.PLUGIN_ID + ".pom.repositories";
   static final String TAB_BUILD = IMavenConstants.PLUGIN_ID + ".pom.build";
   static final String TAB_PLUGINS = IMavenConstants.PLUGIN_ID + ".pom.plugins";
   static final String TAB_REPORTING = IMavenConstants.PLUGIN_ID + ".pom.reporting";
   static final String TAB_PROFILES = IMavenConstants.PLUGIN_ID + ".pom.profiles";
   static final String TAB_TEAM = IMavenConstants.PLUGIN_ID + ".pom.team";
   static final String TAB_DEPENDENCY_TREE = IMavenConstants.PLUGIN_ID + ".pom.dependencyTree";
   static final String TAB_DEPENDENCY_GRAPH = IMavenConstants.PLUGIN_ID + ".pom.dependencyGraph";
   
   private static final String PROJECT_NAME = "test-pom";
 
   IUIContext ui;
 
   IWorkspaceRoot root;
 
   IWorkspace workspace;
 
   protected void setUp() throws Exception {
     super.setUp();
     
     WorkbenchPlugin.getDefault().getPreferenceStore().setValue(IPreferenceConstants.RUN_IN_BACKGROUND, true);
     PrefUtil.getAPIPreferenceStore().setValue(IWorkbenchPreferenceConstants.ENABLE_ANIMATIONS, false);
     
     ShellFinder.bringRootToFront(getActivePage().getWorkbenchWindow().getShell().getDisplay());
     
     workspace = ResourcesPlugin.getWorkspace();
     
     root = workspace.getRoot();
     ui = getUI();
     
     if("Welcome".equals(getActivePage().getActivePart().getTitle())) {
       ui.close(new CTabItemLocator("Welcome"));
     }
     
     // close unnecessary tabs (different versions have different defaults in java perspective)
     // closeView("org.eclipse.mylyn.tasks.ui.views.tasks", "Task List");
     // closeView("org.eclipse.ui.views.ContentOutline", "Outline");
   }
 
   protected void oneTimeSetup() throws Exception {
     super.oneTimeSetup();
 
     ShellFinder.bringRootToFront(getActivePage().getWorkbenchWindow().getShell().getDisplay());
     
     ui = getUI();
 
     if("Welcome".equals(getActivePage().getActivePart().getTitle())) {
       ui.close(new CTabItemLocator("Welcome"));
     }
     
     IConditionMonitor monitor = (IConditionMonitor) getUI().getAdapter(IConditionMonitor.class);
     monitor.add(new ShellShowingCondition("Save Resource"), //
       new IHandler() {
         public void handle(IUIContext ui) {
           try {
             ui.click(new ButtonLocator("&Yes"));
           } catch(WidgetSearchException ex) {
             // ignore
           }
         }
       });
 
     IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
     IPerspectiveDescriptor perspective = perspectiveRegistry
         .findPerspectiveWithId("org.eclipse.jdt.ui.JavaPerspective");
     getActivePage().setPerspective(perspective);
 
     createTestProject();
   }
 
 	public void testUpdatingArtifactIdInXmlPropagatedToForm() throws Exception {
 	  openPomFile();
 
 	  selectEditorTab(TAB_POM_XML);
 	  
     replaceText("test-pom", "test-pom1");
     
     selectEditorTab(TAB_OVERVIEW);
     assertTextValue("artifactId", "test-pom1");
   }
 
   public void testFormToXmlAndXmlToFormInParentArtifactId() throws Exception {
     // test FORM->XML and XML->FORM update of parentArtifactId
     selectEditorTab(TAB_OVERVIEW);
     ui.click(new SWTWidgetLocator(Label.class, "Parent"));
     setTextValue("parentArtifactId", "parent2");
 
     selectEditorTab(TAB_POM_XML);
     replaceText("parent2", "parent3");
     
     selectEditorTab(TAB_OVERVIEW);
     assertTextValue("parentArtifactId", "parent3");
   }
 
   public void testNewSectionCreation() throws Exception {
     ui.click(new SWTWidgetLocator(Label.class, "Organization"));
 		ui.click(new NamedWidgetLocator("organizationName"));
 		ui.enterText("orgfoo");
 		selectEditorTab(TAB_POM_XML);
     replaceText("orgfoo", "orgfoo1");
     selectEditorTab(TAB_OVERVIEW);
     assertTextValue("organizationName", "orgfoo1");
   }
 
   public void testUndoRedo() throws Exception {
 	  //test undo
 	  ui.keyClick(SWT.CTRL, 'z');
 	  assertTextValue("organizationName", "orgfoo");
 	  //test redo
 	  ui.keyClick(SWT.CTRL, 'y');
 	  assertTextValue("organizationName", "orgfoo1");
   }
 
   public void testDeletingScmSectionInXmlPropagatedToForm() throws Exception {
     selectEditorTab(TAB_OVERVIEW);
     ui.click(new SWTWidgetLocator(Label.class, "SCM"));
     setTextValue("scmUrl", "http://svn.sonatype.org/m2eclipse");
     assertTextValue("scmUrl", "http://svn.sonatype.org/m2eclipse");
     selectEditorTab(TAB_POM_XML);
     delete("<scm>", "</scm>");
     selectEditorTab(TAB_OVERVIEW);
     assertTextValue("scmUrl", "");
     selectEditorTab(TAB_POM_XML);
     delete("<organization>", "</organization>");
     selectEditorTab(TAB_OVERVIEW);
     assertTextValue("organizationName", "");
     setTextValue("scmUrl", "http://svn.sonatype.org/m2eclipse");
     assertTextValue("scmUrl", "http://svn.sonatype.org/m2eclipse");
   }
 
   public void testExternalModificationEditorClean() throws Exception {
     // save editor
     ui.keyClick(SWT.CTRL, 's');
     Thread.sleep(2000);
 
     // externally replace file contents
     IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
     IFile file = root.getFile(new Path(TEST_POM_POM_XML));
     File f = new File(file.getLocation().toOSString());
     String text = getContents(f);
     setContents(f, text.replace("parent3", "parent4"));
 
     // reload the file
     ui.click(new CTabItemLocator("Package Explorer"));
     ui.click(new CTabItemLocator(TEST_POM_POM_XML));
     // ui.contextClick(new TreeItemLocator(TEST_POM_POM_XML, new ViewLocator("org.eclipse.jdt.ui.PackageExplorer")), "Refresh");
     
     ui.wait(new ShellShowingCondition("File Changed"));
     ui.click(new ButtonLocator("&Yes"));
     
     assertTextValue("parentArtifactId", "parent4");
 
     // verify that value changed in xml and in the form
     selectEditorTab(TAB_POM_XML);
     String editorText = getEditorText();
     assertTrue(editorText, editorText.contains("<artifactId>parent4</artifactId>"));
     
     // XXX verify that value changed on a page haven't been active before
   }
 
   // test that form and xml is not updated when refused to pickup external changes
   public void testExternalModificationNotUpdate() throws Exception {
     // XXX test that form and xml are not updated when refused to pickup external changes
   }
   
   // XXX update for new modification code 
   public void testExternalModificationEditorDirty() throws Exception {
     // make editor dirty
     ui.click(new CTabItemLocator(TEST_POM_POM_XML));
     selectEditorTab(TAB_POM_XML);
     replaceText("parent4", "parent5");
     selectEditorTab(TAB_OVERVIEW);
 
     // externally replace file contents
     IFile file = root.getFile(new Path(TEST_POM_POM_XML));
     File f = new File(file.getLocation().toOSString());
     String text = getContents(f);
     setContents(f, text.replace("parent4", "parent6"));
 
     // reload the file
     ui.click(new CTabItemLocator("Package Explorer"));
     // ui.click(new CTabItemLocator("*" + TEST_POM_POM_XML));  // take dirty state into the account
     ui.keyClick(SWT.F12);
     
     // ui.contextClick(new TreeItemLocator(TEST_POM_POM_XML, new ViewLocator("org.eclipse.jdt.ui.PackageExplorer")), "Refresh");
     
     ui.wait(new ShellShowingCondition("File Changed"));
     ui.click(new ButtonLocator("&Yes"));
     
     assertTextValue("parentArtifactId", "parent6");
     
     // verify that value changed in xml and in the form
     selectEditorTab(TAB_POM_XML);
     String editorText = getEditorText();
     assertTrue(editorText, editorText.contains("<artifactId>parent6</artifactId>"));
 
     // XXX verify that value changed on a page haven't been active before
   }
 
   public void testEditorIsClosedWhenProjectIsClosed() throws Exception {
     // XXX test editor is closed when project is closed
     
   }
   
   public void testEditorIsClosedWhenProjectIsDeleted() throws Exception {
     // XXX test editor is closed when project is deleted
   
   }
   
   public void testNewEditorIsClean() throws Exception {
     // close/open the file 
     ui.close(new CTabItemLocator(TEST_POM_POM_XML));
     ui.click(2, new TreeItemLocator(TEST_POM_POM_XML, new ViewLocator("org.eclipse.jdt.ui.PackageExplorer")));
 
     // test the editor is clean
     ui.assertThat(new NotCondition(new DirtyEditorCondition()));
   }
 
   //MNGECLIPSE-874
   public void testUndoAfterSave() throws Exception {
     // make a change 
     ui.click(new CTabItemLocator(TEST_POM_POM_XML));
     selectEditorTab(TAB_POM_XML);
     replaceText("parent6", "parent7");
     selectEditorTab(TAB_OVERVIEW);
     
     //save file
     ui.keyClick(SWT.CTRL, 's');
 
     // test the editor is clean
     ui.assertThat(new NotCondition(new DirtyEditorCondition()));
 
     // undo change
     ui.keyClick(SWT.CTRL, 'z');
 
     // test the editor is dirty
     ui.assertThat(new DirtyEditorCondition());
 
     //test the value
     assertTextValue("parentArtifactId", "parent6");
 
     //save file
     ui.keyClick(SWT.CTRL, 's');
   }
 
   public void testAfterUndoEditorIsClean() throws Exception {
     // make a change 
     ui.click(new CTabItemLocator(TEST_POM_POM_XML));
     selectEditorTab(TAB_POM_XML);
     replaceText("parent6", "parent7");
     selectEditorTab(TAB_OVERVIEW);
     // undo change
     ui.keyClick(SWT.CTRL, 'z');
 
     // test the editor is clean
     ui.assertThat(new NotCondition(new DirtyEditorCondition()));
   }
 
   public void testEmptyFile() throws Exception {
 		ui.contextClick(new TreeItemLocator(PROJECT_NAME, new ViewLocator(
 				"org.eclipse.jdt.ui.PackageExplorer")), "New/File");
 		ui.wait(new ShellShowingCondition("New File"));
 		ui.enterText("test.pom");
 		ui.click(new ButtonLocator("&Finish"));
 		ui.wait(new ShellDisposedCondition("Progress Information"));
 		ui.wait(new ShellDisposedCondition("New File"));
 	  assertTextValue("artifactId", "");
 	  setTextValue("artifactId", "artf1");
 	  selectEditorTab(TAB_POM_XML);
 	  replaceText("artf1", "artf2");
 	  selectEditorTab(TAB_OVERVIEW);
 	  assertTextValue("artifactId", "artf2");
 	  ui.keyClick(SWT.CTRL, 's');
 		ui.close(new CTabItemLocator(PROJECT_NAME + "/test.pom"));
   }
 
 	//MNGECLIPSE-834
 	public void testDiscardedFileDeletion() throws Exception {
 		String name = PROJECT_NAME + "/another.pom";
 		ui.contextClick(new TreeItemLocator(PROJECT_NAME, new ViewLocator(
 				"org.eclipse.jdt.ui.PackageExplorer")), "New/File");
 		ui.wait(new ShellShowingCondition("New File"));
 		ui.enterText("another.pom");
 		ui.keyClick(WT.CR);
 		ui.wait(new ShellDisposedCondition("Progress Information"));
 		ui.wait(new ShellDisposedCondition("New File"));
 		ui.keyClick(SWT.CTRL, 's');
 		ui.close(new CTabItemLocator(name));
 		ui.click(2, new TreeItemLocator(PROJECT_NAME + "/another.pom", new ViewLocator(
 				"org.eclipse.jdt.ui.PackageExplorer")));
 		ui.click(new NamedWidgetLocator("groupId"));
 		ui.enterText("1");
 		ui.close(new CTabItemLocator("*" + name));
 		ui.wait(new ShellDisposedCondition("Progress Information"));
 		ui.wait(new ShellShowingCondition("Save Resource"));
 		ui.click(new ButtonLocator("&No"));
 		ui.contextClick(new TreeItemLocator(name, new ViewLocator(
 				"org.eclipse.jdt.ui.PackageExplorer")), "Delete");
 		ui.wait(new ShellDisposedCondition("Progress Information"));
 		ui.wait(new ShellShowingCondition("Confirm Delete"));
 		ui.keyClick(WT.CR);
 		IFile file = root.getFile(new Path(name));
 		ui.wait(new FileExistsCondition(file, false));
 	}
 	
 	//MNGECLIPSE-833
 	public void testSaveAfterPaste() throws Exception {
 		String name = PROJECT_NAME + "/another.pom";
 		String str = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\"><modelVersion>4.0.0</modelVersion>	<groupId>test</groupId>	<artifactId>parent</artifactId>	<packaging>pom</packaging>	<version>0.0.1-SNAPSHOT</version></project>";
 		IFile file = root.getFile(new Path(name));
 		file.create(new ByteArrayInputStream(str.getBytes()), true, null);
 		ui.wait(new ShellDisposedCondition("Progress Information"));
 		ui.click(2, new TreeItemLocator(PROJECT_NAME + "/another.pom", new ViewLocator(
 		"org.eclipse.jdt.ui.PackageExplorer")));
 	  selectEditorTab(TAB_POM_XML);
 		ui.wait(new NotCondition(new DirtyEditorCondition()));
 		findText("</project>");
 		ui.keyClick(WT.ARROW_LEFT);
 		
 		putIntoClipboard("<properties><sample>sample</sample></properties>");
 		
 		
 		ui.keyClick(SWT.CTRL, 'v');
 		ui.wait(new DirtyEditorCondition());
 		ui.keyClick(SWT.CTRL, 's');
 		ui.wait(new NotCondition(new DirtyEditorCondition()));
 	}
 
 	// MNGECLIPSE-835
   public void testModulesEditorActivation() throws Exception {
     openPomFile();
     
     selectEditorTab(TAB_OVERVIEW);
   
     ui.click(new ButtonLocator("Add..."));
     ui.click(new TableItemLocator("?"));
     ui.enterText("foo1");
     ui.keyClick(WT.CR);
     ui.keyClick(WT.CR);
     
     ui.click(new ButtonLocator("Add..."));
     ui.click(new TableItemLocator("?"));
     ui.enterText("foo2");
     ui.keyClick(WT.CR);
     ui.keyClick(WT.CR);
   
     // save
     ui.keyClick(SWT.CTRL, 's');
     
     ui.click(new TableItemLocator("foo1"));
     ui.click(new TableItemLocator("foo2"));
 
     try {
       // test the editor is clean
       ui.assertThat(new NotCondition(new DirtyEditorCondition()));
     } finally {
       ui.keyClick(SWT.CTRL, 's');
     }
   }
 
   private void closeView(String id, String title) throws Exception {
     IViewPart view = getActivePage().findView(id);
     if (view != null) {
       ui.close(new CTabItemLocator(title));
     }
   }
 
   private void putIntoClipboard(final String str) throws Exception {
 //		ui.contextClick(new TreeItemLocator(PROJECT_NAME, new ViewLocator("org.eclipse.jdt.ui.PackageExplorer")), "New/File");
 //		ui.wait(new ShellShowingCondition("New File"));
 //		ui.enterText("t.txt");
 //		ui.keyClick(WT.CR);
 //		ui.wait(new ShellDisposedCondition("Progress Information"));
 //		ui.wait(new ShellDisposedCondition("New File"));
 //		ui.enterText(str);
 //		ui.keyClick(SWT.CTRL, 'a');
 //		ui.keyClick(SWT.CTRL, 'c');
 //		ui.keyClick(SWT.CTRL, 'z');
 //		ui.close(new CTabItemLocator("t.txt"));
     
     Display.getDefault().syncExec(new Runnable() {
       public void run() {
         Clipboard clipboard = new Clipboard(Display.getDefault());
         TextTransfer transfer = TextTransfer.getInstance();
         clipboard.setContents(new String[] {str}, new Transfer[] {transfer});
         clipboard.dispose();
       }
     });
 	}
 	
 	private void createTestProject() throws CoreException {
     // create new project with POM using new project wizard
     // ui.contextClick(new SWTWidgetLocator(Tree.class, new ViewLocator("org.eclipse.jdt.ui.PackageExplorer")),
     //      "Ne&w/Maven Project");
     // ui.wait(new ShellShowingCondition("New Maven Project"));
     // ui.click(new ButtonLocator("Create a &simple project (skip archetype selection)"));
     // ui.click(new ButtonLocator("&Next >"));
     // ui.enterText("org.foo");
     // ui.keyClick(WT.TAB);
     // ui.enterText("test-pom");
     // ui.click(new ButtonLocator("&Finish"));
     // ui.wait(new ShellDisposedCondition("New Maven Project"));
 
     IProjectConfigurationManager configurationManager = MavenPlugin.getDefault().getProjectConfigurationManager();
     
     Model model = new Model();
     model.setModelVersion("4.0.0");
     model.setGroupId("org.foo");
     model.setArtifactId("test-pom");
     model.setVersion("1.0.0");
     
     String[] folders = new String[0];
     ProjectImportConfiguration config = new ProjectImportConfiguration();
     
     IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(PROJECT_NAME);
     IPath location = null;
     configurationManager.createSimpleProject(project, location, model, folders, config, new NullProgressMonitor());
   }
 
 	private void selectEditorTab(final String id) {
 	  final MavenPomEditor editor = (MavenPomEditor) getActivePage().getActiveEditor();
     Display.getDefault().syncExec(new Runnable() {
       public void run() {
         editor.setActivePage(id);
       }
     });
 	}
 	
   private String openPomFile() {
     IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
     IFile file = root.getFile(new Path(TEST_POM_POM_XML));
 
     final IEditorInput editorInput = new FileEditorInput(file);
     Display.getDefault().syncExec(new Runnable() {
       public void run() {
         try {
           getActivePage().openEditor(editorInput, "org.maven.ide.eclipse.editor.MavenPomEditor", true);
         } catch(PartInitException ex) {
           throw new RuntimeException(ex);
         }
       }
     });
     return file.getLocation().toOSString();
   }
 
   private String getContents(File aFile) throws Exception {
     StringBuilder contents = new StringBuilder();
 
     BufferedReader input = new BufferedReader(new FileReader(aFile));
     String line = null; //not declared within while loop
     while((line = input.readLine()) != null) {
       contents.append(line);
       contents.append(System.getProperty("line.separator"));
     }
     return contents.toString();
   }
 
   static public void setContents(File aFile, String aContents) throws Exception {
     Writer output = new BufferedWriter(new FileWriter(aFile));
     output.write(aContents);
     output.flush();
     output.close();
   }
 
   private void delete(String startMarker, final String endMarker) throws WaitTimedOutException {
 	    final MavenPomEditor editor = (MavenPomEditor) getActivePage().getActiveEditor();
 	    final StructuredTextEditor[] sse = new StructuredTextEditor[1];
 	    Display.getDefault().syncExec(new Runnable() {
 	      public void run() {
 	        sse[0] = (StructuredTextEditor) editor.getActiveEditor();
 	      }
 	    });
 	    
 	    @SuppressWarnings("restriction")
 	    IDocument structuredDocument = sse[0].getModel().getStructuredDocument();
       String text = structuredDocument.get();
 	    int pos1 = text.indexOf(startMarker); 
 	    int pos2 = text.indexOf(endMarker);
 	    text = text.substring(0, pos1) + text.substring(pos2 + endMarker.length());
 	    structuredDocument.set(text);
   }
 
   private void assertTextValue(String id, String value) {
     ui.assertThat(new HasTextCondition(new NamedWidgetLocator(id), value));
   }
 
   private void setTextValue(String id, String value) throws WidgetSearchException {
     ui.setFocus(new NamedWidgetLocator(id));
     ui.keyClick(SWT.CTRL, 'a');
     ui.enterText(value);
   }
 
   private void replaceText(String src, String target) throws WaitTimedOutException, WidgetSearchException {
     ui.keyClick(SWT.CTRL, 'f');
     ui.wait(new ShellShowingCondition(FIND_REPLACE));
 
     ui.enterText(src);
     ui.keyClick(WT.TAB);
     ui.enterText(target);
     ui.keyClick(SWT.ALT, 'a'); // "replace all"
     ui.close(new SWTWidgetLocator(Shell.class, FIND_REPLACE));
     ui.wait(new ShellDisposedCondition(FIND_REPLACE));
   }
 
   private void findText(String src) throws WaitTimedOutException, WidgetSearchException {
     ui.keyClick(SWT.CTRL, 'f');
     ui.wait(new ShellShowingCondition(FIND_REPLACE));
     ui.enterText(src);
     ui.keyClick(WT.CR); // "find"
     ui.close(new SWTWidgetLocator(Shell.class, FIND_REPLACE));
     ui.wait(new ShellDisposedCondition(FIND_REPLACE));
   }
 
   ISelectionProvider getSelectionProvider() {
     return getEditorSite().getSelectionProvider();
   }
 
   private IEditorSite getEditorSite() {
     IEditorPart editor = getActivePage().getActiveEditor();
     IEditorSite editorSite = editor.getEditorSite();
     return editorSite;
   }
 
   IWorkbenchPage getActivePage() {
     IWorkbench workbench = PlatformUI.getWorkbench();
     IWorkbenchWindow window = workbench.getWorkbenchWindows()[0];
     return window.getActivePage();
   }
 
   private String getEditorText() {
     final String[] texts = new String[1];
     Display.getDefault().syncExec(new Runnable() {
       public void run() {
         try {
           WidgetReference ref = (WidgetReference) ui.find(new SWTWidgetLocator(StyledText.class));
           texts[0] = ((StyledText) ref.getWidget()).getText();
         } catch(WidgetSearchException ex) {
           // ignore
         }
       }
     });
     return texts[0];
   }
 
 }
