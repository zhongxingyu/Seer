 package hudson.plugins.emailext;
 
 import hudson.model.FreeStyleBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.Result;
 import hudson.plugins.emailext.plugins.EmailTrigger;
 import hudson.plugins.emailext.plugins.trigger.FixedTrigger;
 import hudson.plugins.emailext.plugins.trigger.PreBuildTrigger;
 import hudson.plugins.emailext.plugins.trigger.StillFailingTrigger;
 import hudson.plugins.emailext.plugins.trigger.SuccessTrigger;
 import java.util.List;
 import org.jvnet.hudson.test.FailureBuilder;
 import org.jvnet.hudson.test.HudsonTestCase;
 import org.jvnet.mock_javamail.Mailbox;
 
 import static org.hamcrest.CoreMatchers.not;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.*;
 
 public class ExtendedEmailTriggerTest
     extends HudsonTestCase
 {
     private ExtendedEmailPublisher publisher;
 
     private FreeStyleProject project;
 
     public void setUp()
         throws Exception
     {
         super.setUp();
 
         publisher = new ExtendedEmailPublisher();
         publisher.defaultSubject = "%DEFAULT_SUBJECT";
         publisher.defaultContent = "%DEFAULT_CONTENT";
 
         project = createFreeStyleProject();
        project.getPublishersList().add( publisher );
     }
 
     public void tearDown()
         throws Exception
     {
         super.tearDown();
 
         Mailbox.clearAll();
     }
 
     public void testShouldNotSendEmailWhenNoTriggerEnabled()
         throws Exception
     {
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatusSuccess( build );
 
         List<String> log = build.getLog( 100 );
         assertThat( "No emails should have been trigger during pre-build or post-build.", log,
                     hasItems( "No emails were triggered.", "No emails were triggered." ) );
     }
 
     public void testPreBuildTriggerShouldAlwaysSendEmail()
         throws Exception
     {
         PreBuildTrigger trigger = new PreBuildTrigger();
         addEmailType( trigger );
         publisher.getConfiguredTriggers().add( trigger );
 
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatusSuccess( build );
 
         assertThat( "Email should have been triggered, so we should see it in the logs.", build.getLog( 100 ),
                     hasItems( "Email was triggered for: " + PreBuildTrigger.TRIGGER_NAME ) );
         assertEquals( 1, Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
     public void testSuccessTriggerShouldSendEmailWhenBuildSucceeds()
         throws Exception
     {
         SuccessTrigger successTrigger = new SuccessTrigger();
         addEmailType( successTrigger );
         publisher.getConfiguredTriggers().add( successTrigger );
 
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatusSuccess( build );
 
         assertThat( "Email should have been triggered, so we should see it in the logs.", build.getLog( 100 ),
                     hasItems( "Email was triggered for: Success" ) );
         assertEquals( 1, Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
     public void testSuccessTriggerShouldNotSendEmailWhenBuildFails()
         throws Exception
     {
         project.getBuildersList().add( new FailureBuilder() );
 
         SuccessTrigger trigger = new SuccessTrigger();
         addEmailType( trigger );
         publisher.getConfiguredTriggers().add( trigger );
 
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatus( Result.FAILURE, build );
 
         assertThat( "Email should not have been triggered, so we shouldn't see it in the logs.", build.getLog( 100 ),
                     not( hasItems( "Email was triggered for: " + SuccessTrigger.TRIGGER_NAME ) ) );
         assertEquals( 0, Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
     public void testFixedTriggerShouldNotSendEmailWhenBuildFirstFails()
         throws Exception
     {
         project.getBuildersList().add( new FailureBuilder() );
 
         FixedTrigger trigger = new FixedTrigger();
         addEmailType( trigger );
         publisher.getConfiguredTriggers().add( trigger );
 
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatus( Result.FAILURE, build );
 
         assertThat( "Email should not have been triggered, so we shouldn't see it in the logs.", build.getLog( 100 ),
                     not( hasItems( "Email was triggered for: " + SuccessTrigger.TRIGGER_NAME ) ) );
         assertEquals( "No email should have been sent out since the build failed only once.", 0,
                       Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
     public void testFixedTriggerShouldSendEmailWhenBuildIsFixed()
         throws Exception
     {
         project.getBuildersList().add( new FailureBuilder() );
 
         FixedTrigger trigger = new FixedTrigger();
         addEmailType( trigger );
         publisher.getConfiguredTriggers().add( trigger );
 
         FreeStyleBuild build1 = project.scheduleBuild2( 0 ).get();
         assertBuildStatus( Result.FAILURE, build1 );
 
         project.getBuildersList().clear();
         FreeStyleBuild build2 = project.scheduleBuild2( 0 ).get();
         assertBuildStatusSuccess( build2 );
 
         assertThat( "Email should have been triggered, so we should see it in the logs.", build2.getLog( 100 ),
                     hasItems( "Email was triggered for: " + FixedTrigger.TRIGGER_NAME ) );
         assertEquals( 1, Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
     public void testStillFailingTriggerShouldNotSendEmailWhenBuildSucceeds()
         throws Exception
     {
         StillFailingTrigger trigger = new StillFailingTrigger();
         addEmailType( trigger );
         publisher.getConfiguredTriggers().add( trigger );
 
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatusSuccess( build );
 
         assertThat( "Email should not have been triggered, so we should not see it in the logs.", build.getLog( 100 ),
                     not( hasItems( "Email was triggered for: " + StillFailingTrigger.TRIGGER_NAME ) ) );
         assertEquals( 0, Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
     public void testStillFailingTriggerShouldNotSendEmailWhenBuildFirstFails()
         throws Exception
     {
         project.getBuildersList().add( new FailureBuilder() );
 
         StillFailingTrigger trigger = new StillFailingTrigger();
         addEmailType( trigger );
         publisher.getConfiguredTriggers().add( trigger );
 
         // only fail once
         FreeStyleBuild build = project.scheduleBuild2( 0 ).get();
         assertBuildStatus( Result.FAILURE, build );
 
         assertThat( "Email should not have been triggered, so we should not see it in the logs.", build.getLog( 100 ),
                     not( hasItems( "Email was triggered for: " + StillFailingTrigger.TRIGGER_NAME ) ) );
         assertEquals( 0, Mailbox.get( "ashlux@gmail.com" ).size() );
     }
 
 
     private void addEmailType( EmailTrigger trigger )
     {
         trigger.setEmail( new EmailType()
         {{
                 setRecipientList( "ashlux@gmail.com" );
                 setSubject( "Yet another Hudson email" );
                 setBody( "Boom goes the dynamite." );
             }} );
     }
 }
