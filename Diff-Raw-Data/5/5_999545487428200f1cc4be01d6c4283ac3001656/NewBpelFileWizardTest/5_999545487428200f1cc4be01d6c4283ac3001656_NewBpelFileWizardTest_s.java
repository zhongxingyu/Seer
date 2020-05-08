 /******************************************************************************
  * Copyright (c) 2011, EBM WebSourcing
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     EBM WebSourcing - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.bpel.ui.wizards;
 
 import java.net.URL;
 
 import org.eclipse.bpel.validator.Builder;
 import org.eclipse.bpel.validator.model.IProblem;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
 import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 /**
  * A set of tests for the BPEL creation wizard.
  * @author Vincent Zurczak - EBM WebSourcing
  */
 public class NewBpelFileWizardTest {
 
 	private static final String BPEL_DIR = "bpelContent";
 	private static final String BPEL_NAME = "myProcess";
 	private static final String PROJECT_NAME = "BpelTest";
 	private static final String BPEL_FILE_NAME = BPEL_NAME + ".bpel";
 
 	private static SWTWorkbenchBot bot = new SWTWorkbenchBot();
 	private final Builder bpelBuilder = new Builder();
 
 
 	/**
 	 * Deletes the created project (and will thus close the BPEL editor).
 	 * @throws Exception
 	 */
 	@After
 	public void afterTest() throws Exception {
 
 		// Close a previous wizard whose test failed?
 		try {
 			bot.button( "Cancel" ).click();
 		} catch( Exception e ) {
 			// nothing
 		}
 
 		// Delete the project
 		final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject( PROJECT_NAME );
 		if( ! project.exists())
 			return;
 
 		project.delete( true, null );
 		bot.waitUntil( new DefaultCondition() {
 
 			@Override
 			public boolean test() throws Exception {
 				return ! project.exists();
 			}
 
 			@Override
 			public String getFailureMessage() {
 				return "The project could not be deleted.";
 			}
 		});
 	}
 
 
 	/**
 	 * Prepares the work.
 	 * @throws Exception
 	 */
 	@BeforeClass
 	public static void beforeClass() throws Exception {
 
 		// Close the welcome page
 		try {
 			bot.viewByTitle( "Welcome" ).close();
 		} catch( Exception e ) {
 			// nothing
 		}
 
 		// Disable auto-build
 		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=260010
 		SWTBotMenu menu = null;
 		String ba = "Build Automatically";
 		try {
 			menu = bot.menu( "Project" ).menu( ba );
 		} catch( Exception e ) {
 			// nothing
 		}
 
 		if( menu == null ) {
 			try {
 				menu = bot.menu( "Project", 1 ).menu( ba );
 			} catch( Exception e2 ) {
 				// nothing
 			}
 		}
 
 		if( menu == null ) {
 			try {
 				menu = bot.menu( ba );
 			} catch( Exception e ) {
 				// nothing
 			}
 		}
 
 		if( menu != null && menu.isChecked())
 			menu.click();
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Empty process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInBpelDirectoryEmpty() throws Exception {
 		testTemplate( 1, false, 1, true, true );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Synchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInBpelDirectorySynchronous() throws Exception {
 		testTemplate( 2, false, 0, true, true );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Asynchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInBpelDirectoryAsynchronous() throws Exception {
 		testTemplate( 0, false, 0, true, true );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Empty process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInBpelProjectEmpty() throws Exception {
 		testTemplate( 1, false, 1, true, false );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Synchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInBpelProjectSynchronous() throws Exception {
 		testTemplate( 2, false, 0, true, false );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Asynchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInBpelProjectAsynchronous() throws Exception {
 		testTemplate( 0, false, 0, true, false );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Empty process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInNonBpelProjectEmpty() throws Exception {
 		testTemplate( 1, false, 1, false, false );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Synchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInNonBpelProjectSynchronous() throws Exception {
 		testTemplate( 2, false, 0, false, false );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * <p>
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * Asynchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testTemplateInNonBpelProjectAsynchronous() throws Exception {
 		testTemplate( 0, false, 0, false, false );
 	}
 
 
 	/**
 	 * Tests the creation of an abstract BPEL from a template.
 	 * <p>
 	 * The process is created at the root of a BPEL project.
 	 * Empty process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testAbstractTemplateEmpty() throws Exception {
 		testTemplate( 1, true, 1, true, false );
 	}
 
 
 	/**
 	 *Tests the creation of an abstract BPEL from a template.
 	 * <p>
 	 * The process is created at the root of a BPEL project.
 	 * Synchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void  testAbstractTemplateSynchronous() throws Exception {
 		testTemplate( 2, true, 1, true, false );
 	}
 
 
 	/**
 	 * Tests the creation of an abstract BPEL from a template.
 	 * <p>
 	 * The process is created at the root of a BPEL project.
 	 * Asynchronous process.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void  testAbstractTemplateAsynchronous() throws Exception {
 		testTemplate( 0, true, 1, true, false );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL definition.
 	 * <p>
 	 * The WSDL is imported in the project.
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * The created process is NOT abstract.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testWsdlWithImportTux() throws Exception {
 		testWsdlFirst( "test1/tuxDroid.wsdl", false, true, true, true, 0 );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL definition.
 	 * <p>
 	 * The WSDL is imported in the project.
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * The created process is NOT abstract.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testWsdlWithImportTo() throws Exception {
 		testWsdlFirst( "test2/To.wsdl", false, true, true, true, 0 );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL definition.
 	 * <p>
 	 * The WSDL is NOT imported in the project.
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * The created process is NOT abstract.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testWsdlWithoutImportTux() throws Exception {
 		testWsdlFirst( "test1/tuxDroid.wsdl", false, true, true, true, 0 );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL definition.
 	 * <p>
 	 * The WSDL is NOT imported in the project.
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * The created process is NOT abstract.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testWsdlWithoutImportTo() throws Exception {
 		testWsdlFirst( "test2/To.wsdl", false, true, true, true, 0 );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL definition.
 	 * <p>
 	 * The WSDL is imported in the project.
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * The created process is abstract.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testWsdlAsAbstractWithImportTux() throws Exception {
 		testWsdlFirst( "test1/tuxDroid.wsdl", true, true, true, true, 1 );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL definition.
 	 * <p>
 	 * The WSDL is imported in the project.
 	 * The process is created in the "bpelContent" directory of a BPEL project.
 	 * The created process is abstract.
 	 * </p>
 	 *
 	 * @throws Exception
 	 */
 	@Test
 	public void testWsdlAsAbstractWithImportTo() throws Exception {
 		testWsdlFirst( "test2/To.wsdl", true, true, true, true, 1 );
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a WSDL.
 	 * @param wsdlFile the relative location of the WSDL file to use as input
 	 * @param isAbstract true f the created process must be abstract, false otherwise
 	 * @param importWsdl true to import the WSDL in the project, false to reference the remote one
 	 * @param createBpelProject true to create a BPEL project, false for a simple Eclipse project
 	 * @param selectBpelDir true to select the BPEL directory, false otherwise (only makes sense for a BPEL project)
 	 * @param expectedNumberOfProblems the number of problems the created process should contain
 	 * @throws Exception
 	 */
 	private void testWsdlFirst(
 			String wsdlFile, boolean isAbstract, boolean importWsdl,
 			boolean createBpelProject, boolean selectBpelDir, int expectedNumberOfProblems )
 	throws Exception {
 
 		if( createBpelProject )
 			createBpelProject();
 		else
 			createSimpleProject();
 
 		SWTBotMenu newMenu = bot.menu( "File" ).menu( "New" );
 		newMenu.menu( "BPEL Process File" ).click();
 
 		bot.comboBox().setSelection( 1 );
 		bot.text( 0 ).setText( BPEL_NAME );
 		bot.comboBox( 1 ).setText( "http://" + BPEL_NAME );
 		if( isAbstract )
 			bot.checkBox( 0 ).select();
 		bot.button( "Next >" ).click();
 
 		URL url = getClass().getResource( "/wsdls/" + wsdlFile );
 		url = FileLocator.toFileURL( url );
 
 		bot.text( 0 ).setText( url.toString());
 		bot.link( 2 ).click();
 		bot.comboBox( 0 ).setSelection( 0 );
 		if( ! importWsdl )
 			bot.checkBox( 0 ).deselect();
 		bot.button( "Next >" ).click();
 
 		completeBpelFileCreation( selectBpelDir );
 		IPath path;
 		if( selectBpelDir )
 			path = new Path( PROJECT_NAME  ).append( BPEL_DIR ).append( BPEL_FILE_NAME );
 		else
 			path = new Path( PROJECT_NAME  ).append( BPEL_FILE_NAME );
 
 		IFile bpelFile = ResourcesPlugin.getWorkspace().getRoot().getFile( path );
 		Assert.assertTrue( bpelFile.isAccessible());
 
 		IProblem[] problems = this.bpelBuilder.validate( bpelFile, null );
 		Assert.assertEquals( problems.length, expectedNumberOfProblems );
 
 		// No import => only 1 WSDL, the one for the artifacts
 		if( ! importWsdl ) {
 			int wsdlCpt = 0;
 			for( IResource r : bpelFile.getParent().members()) {
 				if( r instanceof IFile && "wsdl".equalsIgnoreCase( r.getFileExtension()))
 					wsdlCpt ++;
 			}
 
 			Assert.assertEquals( wsdlCpt, 1 );
 		}
 	}
 
 
 	/**
 	 * Tests the creation of a BPEL from a template.
 	 * @param tplId the template ID (index of the template in the associated combo)
 	 * @param isAbstract true if the created process must be abstract, false otherwise
 	 * @param expectedNumberOfProblems the number of problems the created process should contain
 	 * @param createBpelProject true to create a BPEL project, false for a simple Eclipse project
 	 * @param selectBpelDir true to select the BPEL directory, false otherwise (only makes sense for a BPEL project)
 	 * @throws Exception
 	 */
 	private void testTemplate( int tplId, boolean isAbstract, int expectedNumberOfProblems, boolean createBpelProject, boolean selectBpelDir )
 	throws Exception {
 
 		if( createBpelProject )
 			createBpelProject();
 		else
 			createSimpleProject();
 
 		bot.defaultPerspective().activate();
 		bot.menu( "File" ).menu( "New" ).menu( "Other..." ).click();
 		SWTBotShell shell = bot.shell( "New" );
 		shell.activate();
 
 		bot.tree( 0 ).setFocus();
 		bot.tree( 0 ).expandNode( "BPEL 2.0" ).select( "BPEL Process File" );
 		bot.button("Next >").click();
 
 		bot.text( 0 ).setText( BPEL_NAME );
 		bot.comboBox( 1 ).setText( "http://" + BPEL_NAME );
 		if( isAbstract )
 			bot.checkBox( 0 ).select();
 		bot.button( "Next >" ).click();
 
 		bot.comboBox( 0 ).setSelection( tplId );
 		bot.button( "Next >" ).click();
 
 		completeBpelFileCreation( selectBpelDir );
 		IPath path;
 		if( selectBpelDir )
 			path = new Path( PROJECT_NAME  ).append( BPEL_DIR ).append( BPEL_FILE_NAME );
 		else
 			path = new Path( PROJECT_NAME  ).append( BPEL_FILE_NAME );
 
 		IFile bpelFile = ResourcesPlugin.getWorkspace().getRoot().getFile( path );
 		Assert.assertTrue( bpelFile.isAccessible());
 
 		// The empty template is not valid because it does not start with an starting activity
 		IProblem[] problems = this.bpelBuilder.validate( bpelFile, null );
 		Assert.assertEquals( problems.length, expectedNumberOfProblems );
 	}
 
 
 	/**
 	 * Completes the creation of a new BPEL file.
 	 * @param selectBpelDir true to create the BPEL in the {@link #BPEL_DIR} directory, false for the root of the project
 	 * @throws Exception if the created project could not be built
 	 */
 	private void completeBpelFileCreation( boolean selectBpelDir ) throws Exception {
 
 		// Create the project
 		if( selectBpelDir )
 			bot.tree( 0 ).expandNode( PROJECT_NAME ).select( BPEL_DIR );
 		else
 			bot.tree( 0 ).select( PROJECT_NAME );
 
 		bot.button( "Finish" ).click();
 		bot.waitUntil( new DefaultCondition() {
 
 			@Override
 			public boolean test() throws Exception {
 				String title = NewBpelFileWizardTest.bot.activeEditor().getTitle();
 				return (BPEL_NAME + ".bpel").equals( title );
 			}
 
 			@Override
 			public String getFailureMessage() {
 				return "Could not wait for the BPEL Designer to be open";
 			}
 		});
 	}
 
 
 	/**
 	 * Creates a BPEL project.
 	 */
 	private void createBpelProject() {
 
		bot.menu( "Other..." ).click();
 		SWTBotShell shell = bot.shell( "New" );
 		shell.activate();
 
 		bot.tree( 0 ).setFocus();
 		bot.tree( 0 ).expandNode( "BPEL 2.0" ).select( "BPEL Project" );
 		bot.button("Next >").click();
 
 		bot.text( 0 ).setText( PROJECT_NAME );
 		bot.button( "Next >" ).click();
 		bot.button( "Finish" ).click();
 
 		try {
 			shell = bot.shell( "Open Associated Perspective?" );
 			shell.activate();
 			bot.button( "Yes" ).click();
 
 		} catch( Exception e ) {
 			// nothing - the shell does not appear all the time
 		}
 	}
 
 
 	/**
 	 * Creates a simple project.
 	 */
 	private void createSimpleProject() {
 
		bot.menu( "Other..." ).click();
 		SWTBotShell shell = bot.shell( "New" );
 		shell.activate();
 
 		bot.tree( 0 ).setFocus();
 		bot.tree( 0 ).expandNode( "General" ).select( "Project" );
 		bot.button("Next >").click();
 
 		bot.text( 0 ).setText( PROJECT_NAME );
 		bot.button( "Finish" ).click();
 
 		// The following code is useless - there is no default perspective for simple projects -
 		// but it makes one test work (otherwise, this test fails and there is no reason for it).
 		try {
 			shell = bot.shell( "Open Associated Perspective?" );
 			shell.activate();
 			bot.button( "Yes" ).click();
 
 		} catch( Exception e ) {
 			// nothing - the shell does not appear all the time
 		}
 	}
 }
