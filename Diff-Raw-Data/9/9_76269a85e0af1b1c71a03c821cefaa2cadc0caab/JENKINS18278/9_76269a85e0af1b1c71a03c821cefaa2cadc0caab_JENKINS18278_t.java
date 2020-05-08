 package net.praqma.hudson.test.integration.userstories;
 
 import hudson.FilePath;
 import hudson.model.AbstractBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.Project;
 import hudson.model.Result;
 import net.praqma.clearcase.Deliver;
 import net.praqma.clearcase.Rebase;
 import net.praqma.clearcase.exceptions.CleartoolException;
 import net.praqma.clearcase.exceptions.UCMEntityNotFoundException;
 import net.praqma.clearcase.exceptions.UnableToInitializeEntityException;
 import net.praqma.clearcase.exceptions.UnableToLoadEntityException;
 import net.praqma.clearcase.interfaces.Diffable;
 import net.praqma.clearcase.test.annotations.ClearCaseUniqueVobName;
 import net.praqma.clearcase.test.junit.ClearCaseRule;
 import net.praqma.clearcase.ucm.entities.*;
 import net.praqma.hudson.CCUCMBuildAction;
 import net.praqma.hudson.scm.ChangeLogEntryImpl;
 import net.praqma.hudson.scm.ChangeLogSetImpl;
 import net.praqma.hudson.test.BaseTestClass;
 import net.praqma.hudson.test.CCUCMRule;
 import net.praqma.hudson.test.SystemValidator;
 import net.praqma.util.test.junit.DescriptionRule;
 import net.praqma.util.test.junit.TestDescription;
 import org.junit.Rule;
 import org.junit.Test;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * @author cwolfgang
  */
 public class JENKINS18278 extends BaseTestClass {
 
     private static Logger logger = Logger.getLogger( JENKINS18278.class.getName() );
 
     @Rule
     public static ClearCaseRule ccenv = new ClearCaseRule( "JENKINS-18278", "setup-basic.xml" );
 
     @Rule
     public static DescriptionRule desc = new DescriptionRule();
 
     @Test
     @ClearCaseUniqueVobName( name = "regular" )
     @TestDescription( title = "JENKINS-18278", text = "When the foundation of a Stream has changed, the change set is miscalculated, because -pred selects the foundation baseline of the other Stream" )
     public void jenkins18278() throws Exception {
 
         Stream target = ccenv.context.streams.get( "one_int" );
         Stream stream = ccenv.context.streams.get( "one_dev" );
         Component component = ccenv.context.components.get( "_System" );
 
         /* Create first baseline on int and rebase dev */
         ClearCaseRule.ContentCreator ccint1 = ccenv.getContentCreator().setBaselineName( "blint-1" ).setFilename( "foo.bar" ).setActivityName( "int-act1" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).setNewElement( true ).create();
         new Rebase( stream ).addBaseline( ccint1.getBaseline() ).rebase( true );
 
         /* Create first baseline on dev */
         ClearCaseRule.ContentCreator cc1 = ccenv.getContentCreator().setBaselineName( "bl-1" ).setFilename( "foo.bar" ).setActivityName( "dev-act1" ).create();
 
         /* Create baselines on int and rebase the last to dev */
         ClearCaseRule.ContentCreator ccint2 = ccenv.getContentCreator().setBaselineName( "blint-2" ).setFilename( "foo.bar" ).setActivityName( "int-act2" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).create();
         ClearCaseRule.ContentCreator ccint3 = ccenv.getContentCreator().setBaselineName( "blint-3" ).setFilename( "foo.bar" ).setActivityName( "int-act3" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).create();
         ClearCaseRule.ContentCreator ccint4 = ccenv.getContentCreator().setBaselineName( "blint-4" ).setFilename( "foo.bar" ).setActivityName( "int-act4" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).create();
 
         new Rebase( stream ).addBaseline( ccint4.getBaseline() ).rebase( true );
 
         /* Create the last baseline on dev */
         ClearCaseRule.ContentCreator cc2 = ccenv.getContentCreator().setBaselineName( "bl-2" ).setFilename( "foo.bar" ).setActivityName( "dev-act2" ).create();
 
         /* Create the Jenkins project for the dev stream */
         Project project = new CCUCMRule.ProjectCreator( "JENKINS-18278", "_System@" + ccenv.getPVob(), "one_dev@" + ccenv.getPVob() ).getProject();
 
         /* I need a first build */
         jenkins.getProjectBuilder( project ).build();
 
         printDiffs( cc1.getBaseline(), cc2.getBaseline(), cc2.getPath() );
         printDiffs( ccint4.getBaseline(), cc2.getBaseline(), cc2.getPath() );
 
         AbstractBuild build2 = jenkins.getProjectBuilder( project ).build();
 
         printChangeLog( build2 );
 
         FilePath path = new FilePath( project.getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project ), "view/" + ccenv.getUniqueName() + "/Model" );
         listPath( path );
 
         List<Activity> activities = Version.getBaselineDiff( cc1.getBaseline(), cc2.getBaseline(), true, cc2.getPath() );
         new SystemValidator( build2 ).validateBuild( Result.SUCCESS ).addActivitiesToCheck( activities ).validate();
     }
 
    @Test
     @ClearCaseUniqueVobName( name = "trimmed" )
     @TestDescription( title = "JENKINS-18278", text = "When the foundation of a Stream has changed, the change set is miscalculated, because -pred selects the foundation baseline of the other Stream AND trim changeset is enabled" )
     public void jenkins18278Trimmed() throws Exception {
 
         Stream target = ccenv.context.streams.get( "one_int" );
         Stream stream = ccenv.context.streams.get( "one_dev" );
         Component component = ccenv.context.components.get( "_System" );
 
         /* Create first baseline on int and rebase dev */
         ClearCaseRule.ContentCreator ccint1 = ccenv.getContentCreator().setBaselineName( "blint-1" ).setFilename( "foo.bar" ).setActivityName( "int-act1" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).setNewElement( true ).create();
         new Rebase( stream ).addBaseline( ccint1.getBaseline() ).rebase( true );
 
         /* Create first baseline on dev */
         ClearCaseRule.ContentCreator cc1 = ccenv.getContentCreator().setBaselineName( "bl-1" ).setFilename( "foo.bar" ).setActivityName( "dev-act1" ).create();
 
         /* Create baselines on int and rebase the last to dev */
         ClearCaseRule.ContentCreator ccint2 = ccenv.getContentCreator().setBaselineName( "blint-2" ).setFilename( "foo.bar" ).setActivityName( "int-act2" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).create();
         ClearCaseRule.ContentCreator ccint3 = ccenv.getContentCreator().setBaselineName( "blint-3" ).setFilename( "foo.bar" ).setActivityName( "int-act3" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).create();
         ClearCaseRule.ContentCreator ccint4 = ccenv.getContentCreator().setBaselineName( "blint-4" ).setFilename( "foo.bar" ).setActivityName( "int-act4" ).setStreamName( "one_int" ).setPostFix( "_one_int" ).create();
 
         new Rebase( stream ).addBaseline( ccint4.getBaseline() ).rebase( true );
 
         /* Create the last baseline on dev */
         ClearCaseRule.ContentCreator cc2 = ccenv.getContentCreator().setBaselineName( "bl-2" ).setFilename( "foo.bar" ).setActivityName( "dev-act2" ).create();
 
         /* Create the Jenkins project for the dev stream */
         Project project = new CCUCMRule.ProjectCreator( "JENKINS-18278-trimmed", "_System@" + ccenv.getPVob(), "one_dev@" + ccenv.getPVob() ).setTrim( true ).getProject();
 
         /* I need a first build */
         jenkins.getProjectBuilder( project ).build();
 
         printDiffs( cc1.getBaseline(), cc2.getBaseline(), cc2.getPath() );
         printDiffs( ccint4.getBaseline(), cc2.getBaseline(), cc2.getPath() );
 
         AbstractBuild build2 = jenkins.getProjectBuilder( project ).build();
 
         printChangeLog( build2 );
 
         FilePath path = new FilePath( project.getLastBuiltOn().getWorkspaceFor( (FreeStyleProject)project ), "view/" + ccenv.getUniqueName() + "/Model" );
         listPath( path );
 
         List<Activity> activities = Version.getBaselineDiff( cc1.getBaseline(), cc2.getBaseline(), true, cc2.getPath() );
         new SystemValidator( build2 ).validateBuild( Result.SUCCESS ).addActivitiesToCheck( activities ).validate();
     }
 
     public void printChangeLog( AbstractBuild build ) {
         System.out.println( "LISTING THE CHANGESET FOR " + build );
         ChangeLogSetImpl cls = (ChangeLogSetImpl) build.getChangeSet();
         for( ChangeLogEntryImpl e : cls.getEntries() ) {
             System.out.println( "Author           : " + e.getAuthor() );
             System.out.println( "Activity headline: " + e.getActHeadline() );
             System.out.println( "Affected paths   : " + e.getAffectedPaths() );
             System.out.println( "Message          : " + e.getMsg() );
         }
         System.out.println( "END OF LISTING" );
     }
 
 }
