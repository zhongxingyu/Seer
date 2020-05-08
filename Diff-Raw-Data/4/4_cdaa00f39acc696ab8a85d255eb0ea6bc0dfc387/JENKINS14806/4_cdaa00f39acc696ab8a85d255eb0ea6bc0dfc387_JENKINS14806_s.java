 package net.praqma.hudson.test.integration.userstories;
 
 import java.io.IOException;
 import java.util.concurrent.Future;
 
 import org.junit.ClassRule;
 import org.junit.Rule;
 import org.junit.Test;
 import org.jvnet.hudson.test.TestBuilder;
 
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.BuildListener;
 import hudson.model.FreeStyleBuild;
 import hudson.scm.PollingResult;
 import net.praqma.hudson.scm.CCUCMScm;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.junit.DescriptionRule;
 import net.praqma.junit.TestDescription;
 
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 
 import static org.junit.Assert.*;
 
 import static org.hamcrest.CoreMatchers.*;
 
 public class JENKINS14806 {
 
 	@ClassRule
 	public static CCUCMRule jenkins = new CCUCMRule();
 	
 	@Rule
 	public static ClearCaseRule ccenv = new ClearCaseRule( "JENKINS-14806", "setup-JENKINS-14806.xml" );
 	
 	@Rule
 	public static DescriptionRule desc = new DescriptionRule();
 
 	@Test
 	@TestDescription( title = "JENKINS-14806", text = "Multisite polling finds the same baseline times", configurations = { "ClearCase multisite = true" }	)
 	public void jenkins13944() throws Exception {
 	
 		CCUCMScm ccucm = jenkins.getCCUCM( "child", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob(), "INITIAL", false, false, false, false, true, "[project]_[date]_[time]" );
 		ccucm.setMultisitePolling( true );
 		System.out.println( "MP: " + ccucm.getMultisitePolling() );
 		FreeStyleProject project = jenkins.createProject( ccenv.getUniqueName(), ccucm );
 		
 		/* First build to create a view */
 		System.out.println( "First build" );
 		jenkins.buildProject( project, false );
 		
 		/* Add builder to sleep */
 		System.out.println( "Adding waiter" );
 		project.getBuildersList().add( new WaitBuilder( 10000 ) );
 		
 		System.out.println( "Async build" );
 		Future<FreeStyleBuild> futureBuild = project.scheduleBuild2( 0 );
 		
 		/* Remove the builder again */
 		System.out.println( "Clear builders" );
 		project.getBuildersList().clear();
 		
 		/* And then poll */
 		System.out.println( "Poll" );
 		PollingResult result = project.poll( jenkins.createTaskListener() );
 		
 		System.out.println( "Assert" );
		assertThat( result, is( PollingResult.NO_CHANGES ) );
 		
 		System.out.println( "Waiting for waiter" );
 		futureBuild.get();
 	}
 
 	public class WaitBuilder extends TestBuilder {
 		
 		int millisecs;
 		
 		public WaitBuilder( int ms ) {
 			this.millisecs = ms;
 		}
 
 		@Override
 		public boolean perform( AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener ) throws InterruptedException, IOException {
 			System.out.println( "Sleeping...." );
 			Thread.sleep( this.millisecs );
 			System.out.println( "Awaken...." );
 			return true;
 		}
 		
 	}
 
 	
 }
