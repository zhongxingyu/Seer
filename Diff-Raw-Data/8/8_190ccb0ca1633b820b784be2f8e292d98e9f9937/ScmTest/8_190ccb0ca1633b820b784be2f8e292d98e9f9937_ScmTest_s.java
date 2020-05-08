 package net.praqma.hudson.scm;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 
 import hudson.FilePath;
 import hudson.model.FreeStyleBuild;
 import hudson.model.Result;
 import hudson.model.FreeStyleProject;
 import net.praqma.clearcase.Rebase;
 import net.praqma.clearcase.exceptions.ClearCaseException;
 import net.praqma.clearcase.exceptions.CleartoolException;
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Baseline.LabelBehaviour;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.clearcase.ucm.view.SnapshotView.Components;
 import net.praqma.clearcase.ucm.view.SnapshotView.LoadRules;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.jenkins.utils.test.ClearCaseJenkinsTestCase;
 import net.praqma.util.debug.Logger;
 
 public class ScmTest extends ClearCaseJenkinsTestCase {
 	
 	private static Logger logger = Logger.getLogger();
 	
 	@Test
 	public void test1() throws Exception {
 		String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		FreeStyleProject project = createFreeStyleProject( "ccucm-project-" + uniqueTestVobName );
 		
 		CCUCMScm scm = new CCUCMScm( "Model@" + coolTest.getPVob(), "INITIAL", "ALL", false, "self", uniqueTestVobName + "_one_int@" + coolTest.getPVob(), "successful", false, "", true, true, false, true, "jenkins" );
 		
 		project.setScm( scm );
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 
 		br.close();
 		
 		/* Validation */
 		assertTrue( b.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Workspace validation */
 		logger.info( "Checking workspace" );
 		int check = 0;
 		
 		logger.debug( "--------->" + new FilePath( b.getWorkspace(), "view" ).listDirectories() );
 		
 		FilePath viewPath = new FilePath( b.getWorkspace(), "view/" + uniqueTestVobName );
 		List<FilePath> rootDirs = viewPath.listDirectories();
 		logger.debug( "Checking file paths for " + viewPath );
 		for( FilePath f : rootDirs ) {
 			logger.debug( "Checking file path " + f );
 			if( f.getBaseName().equals( "Model" ) ) {
 				check |= 1;
 			}
 		}
 		
 		logger.debug( "Check = " + check );
 		
 		assertEquals( 1, check );
 		
 		logger.debug( "Checked" );
 		
 		/* Check the build baseline */
 		logger.info( "Getting action" );
 		CCUCMBuildAction action = b.getAction( CCUCMBuildAction.class );
 		logger.debug( "Asserting baseline" );
 		assertNotNull( action.getBaseline() );
 		if( action.getBaseline() != null ) {
 			logger.debug( "Asserting baseline promotion level" );
 			action.getBaseline().getPromotionLevel( true );
 		}
 	}
 	
 	
 	@Test
 	public void testChildPolling() throws Exception {
 		String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		
 		/* Prepare dev stream */
 		Stream devStream = Stream.get( uniqueTestVobName + "_one_dev", coolTest.getPVob() ).load();
 		File rebaseView = File.createTempFile( "ccucm-rebase-", "view" );
 		if( !rebaseView.delete() ) {
 			throw new IOException( "Unable to prepare temporary view" );
 		}
 		
 		if( !rebaseView.mkdir() ) {
 			throw new IOException( "Unable to create temporary view" );
 		}
 		System.out.println( "FILE: " + rebaseView );
 		System.out.println( "FILE(DIR): " + rebaseView.isDirectory() );
 		SnapshotView view = SnapshotView.create( devStream, rebaseView, uniqueTestVobName + "-my-view" );
 		
 		view.Update( false, true, true, false, new LoadRules( view, Components.ALL ) );
 		
		logger.debug( "VIEW LIST:" + Arrays.asList( rebaseView.list() ) );
		
 		Rebase rebase = new Rebase( devStream, view, Baseline.get( "_System_2.0", coolTest.getPVob() ) );
 		rebase.rebase( true );
 		
 
 		Component component = Component.get( "_System", coolTest.getPVob() ).load();
 		logger.debug( "Component: " + component );
 		
 		/**/
 		String filename = coolTest.uniqueTimeStamp + "/Model/model.h";
		File cfile = new File( "Model/model.h" );
 		Version.checkOut( cfile, rebaseView );
		File fullfile = new File( rebaseView, "Model/model.h" );
 		FileWriter fw = null;
 		try {
 			fw = new FileWriter( fullfile, true );
 			fw.write( "Content---" );
 		} catch( IOException e1 ) {
 			throw new ClearCaseException( e1 );
 		} finally {
 			try {
 				fw.close();
 			} catch( IOException e1 ) {
 				throw new ClearCaseException( e1 );
 			}
 		}
 		try {
 			List<File> files = Version.getUncheckedIn( rebaseView );
 			for( File f : files ) {
 				logger.debug( "Checking in " + f );
 				try {
 					Version.checkIn( f, false, rebaseView );
 				} catch( CleartoolException e1 ) {
 					logger.debug( "Unable to checkin " + f );
 					/* No op */
 				}
 			}
 		} catch( CleartoolException e1 ) {
 			logger.error( e1.getMessage() );				
 		}
 		Baseline nbl = Baseline.create( "Model-4", component, rebaseView, LabelBehaviour.INCREMENTAL, false );
 		logger.debug( "Created " + nbl );
 		
 		FreeStyleProject project = createFreeStyleProject( "ccucm-project-" + uniqueTestVobName );
 		
 		CCUCMScm scm = new CCUCMScm( "Model@" + coolTest.getPVob(), "INITIAL", "ALL", false, "child", uniqueTestVobName + "_one_int@" + coolTest.getPVob(), "successful", false, "My-super-hot-baseline", true, true, false, true, "jenkins" );
 		
 		project.setScm( scm );
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 
 		br.close();
 		
 		/* Validation */
 		assertTrue( b.getResult().isBetterOrEqualTo( Result.SUCCESS ) );
 		
 		/* Workspace validation */
 		logger.info( "Checking workspace" );
 		int check = 0;
 		FilePath viewPath = new FilePath( b.getWorkspace(), "view/" + uniqueTestVobName );
 		List<FilePath> rootDirs = viewPath.listDirectories();
 		logger.debug( "Checking file paths for " + viewPath );
 		for( FilePath f : rootDirs ) {
 			logger.debug( "Checking file path " + f );
 			if( f.getBaseName().equals( "Model" ) ) {
 				check |= 1;
 			}
 		}
 		
 		assertEquals( 1, check );
 		
 		/* Check the build baseline */
 		logger.info( "Getting action" );
 		CCUCMBuildAction action = b.getAction( CCUCMBuildAction.class );
 		logger.debug( "Asserting baseline" );
 		assertNotNull( action.getBaseline() );
 		if( action.getBaseline() != null ) {
 			logger.debug( "Asserting baseline promotion level" );
 			action.getBaseline().getPromotionLevel( true );
 		}
 	}
 	
 	/*
 	@Test
 	public void testWrongStream() throws Exception {
 		String uniqueTestVobName = "ccucm" + coolTest.uniqueTimeStamp;
 		coolTest.variables.put( "vobname", uniqueTestVobName );
 		coolTest.variables.put( "pvobname", uniqueTestVobName + "_PVOB" );
 		
 		coolTest.bootStrap();
 		FreeStyleProject project = createFreeStyleProject( "ccucm" );
 		
 		CCUCMScm scm = new CCUCMScm( "_System@", "INITIAL", "ALL", false, "self", "__int@", "successful", false, "", true, true, false, true, "jenkins" );
 		
 		project.setScm( scm );
 		
 		FreeStyleBuild b = project.scheduleBuild2( 0 ).get();
 		
 		System.out.println( "Workspace: " + b.getWorkspace() );
 		
 		System.out.println( "Logfile: " + b.getLogFile() );
 		
 		BufferedReader br = new BufferedReader( new FileReader( b.getLogFile() ) );
 		String line = "";
 		while( ( line = br.readLine() ) != null ) {
 			System.out.println( "[JENKINS] " + line );
 		}
 
 		br.close();
 	}
 	*/
 }
