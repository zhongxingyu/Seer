 package net.praqma.hudson.test.integration.userstories;
 
 import hudson.FilePath;
 import hudson.model.AbstractBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.Project;
 import hudson.model.Result;
 import net.praqma.clearcase.Rebase;
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 import net.praqma.clearcase.ucm.entities.Activity;
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.entities.Version;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.util.test.junit.TestDescription;
 import org.junit.Rule;
 import org.junit.Test;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author cwolfgang
  */
 public class JENKINS18278Interproject extends JENKINS18278Base {
 
     private static Logger logger = Logger.getLogger( JENKINS18278Interproject.class.getName() );
 
     @Rule
     public static ClearCaseRule ccenv = new ClearCaseRule( "JENKINS-18278", "setup-interproject-basic.xml" );
 
    @Test
     @ClearCaseUniqueVobName( name = "interproject" )
     @TestDescription( title = "JENKINS-18278", text = "When the foundation of a Stream has changed, the change set is miscalculated, because -pred selects the foundation baseline of the other Stream - interproject" )
     public void jenkins18278() throws Exception {
 
         Stream source = ccenv.context.streams.get( "one_int" );
         Stream target = ccenv.context.streams.get( "two_int" );
         Component component = ccenv.context.components.get( "_System" );
 
         /* Create first baseline on int and rebase dev */
         ClearCaseRule.ContentCreator cc_target1 = ccenv.getContentCreator().setBaselineName( "bl-target-1" ).setFilename( "foo.bar" ).setActivityName( "target-act1" ).setStreamName( "two_int" ).setPostFix( "_two_int" ).setNewElement( true ).create();
         new Rebase( source ).addBaseline( cc_target1.getBaseline() ).rebase( true );
 
         /* Create first baseline on dev */
         ClearCaseRule.ContentCreator cc_source1 = ccenv.getContentCreator().setBaselineName( "bl-source-1" ).setFilename( "foo.bar" ).setActivityName( "source-act1" ).create();
 
         /* Create baselines on int and rebase the last to dev */
         ClearCaseRule.ContentCreator cc_target2 = ccenv.getContentCreator().setBaselineName( "bl-target-2" ).setFilename( "foo.bar" ).setActivityName( "target-act2" ).setStreamName( "two_int" ).setPostFix( "_two_int" ).create();
         ClearCaseRule.ContentCreator cc_target3 = ccenv.getContentCreator().setBaselineName( "bl-target-3" ).setFilename( "foo.bar" ).setActivityName( "target-act3" ).setStreamName( "two_int" ).setPostFix( "_two_int" ).create();
         ClearCaseRule.ContentCreator cc_target4 = ccenv.getContentCreator().setBaselineName( "bl-target-4" ).setFilename( "foo.bar" ).setActivityName( "target-act4" ).setStreamName( "two_int" ).setPostFix( "_two_int" ).create();
 
         new Rebase( source ).addBaseline( cc_target4.getBaseline() ).rebase( true );
 
         /* Create the last baseline on dev */
         ClearCaseRule.ContentCreator cc_source2 = ccenv.getContentCreator().setBaselineName( "bl-source-2" ).setFilename( "foo.bar" ).setActivityName( "source-act2" ).create();
 
         /* Create the Jenkins project for the dev stream */
         Project project = new CCUCMRule.ProjectCreator( "JENKINS-18278-interproject", "_System@" + ccenv.getPVob(), "one_int@" + ccenv.getPVob() ).getProject();
 
         /* I need a first build */
         jenkins.getProjectBuilder( project ).build();
 
         printDiffs( cc_source1.getBaseline(), cc_source2.getBaseline(), cc_source2.getPath() );
         printDiffs( cc_target4.getBaseline(), cc_source2.getBaseline(), cc_source2.getPath() );
 
         AbstractBuild build2 = jenkins.getProjectBuilder( project ).build();
 
         printChangeLog( build2 );
 
         FilePath path = new FilePath( project.getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project ), "view/" + ccenv.getUniqueName() + "/Model" );
         listPath( path );
 
         List<Activity> activities = Version.getBaselineDiff( cc_source1.getBaseline(), cc_source2.getBaseline(), true, cc_source2.getPath() );
         new SystemValidator( build2 ).validateBuild( Result.SUCCESS ).addActivitiesToCheck( activities ).validate();
     }
 
 }
