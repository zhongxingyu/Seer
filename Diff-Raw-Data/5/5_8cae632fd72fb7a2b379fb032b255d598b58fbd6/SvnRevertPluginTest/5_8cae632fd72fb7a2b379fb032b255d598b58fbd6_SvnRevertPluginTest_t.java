 package jenkins.plugins.svn_revert;
 
 import static hudson.model.Result.SUCCESS;
 import static hudson.model.Result.UNSTABLE;
 import static org.hamcrest.Matchers.containsString;
 import static org.junit.Assert.assertThat;
 import hudson.FilePath;
 import hudson.model.FreeStyleBuild;
 import hudson.model.Result;
 import hudson.model.AbstractProject;
 import hudson.model.FreeStyleProject;
 import hudson.scm.NullSCM;
 import hudson.scm.SubversionSCM;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.ExecutionException;
 
 import org.jvnet.hudson.test.HudsonHomeLoader.CopyExisting;
 import org.jvnet.hudson.test.HudsonTestCase;
 import org.jvnet.hudson.test.MockBuilder;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNCommitClient;
 
 @SuppressWarnings("deprecation")
 public class SvnRevertPluginTest extends HudsonTestCase {
 
     private static final String REVISION_THAT_TRIGGERED_CURRENT_BUILD = "6";
     private static final String REVISION_THAT_TRIGGERED_PREVIOUS_BUILD = "5";
     private static final String NEXT_SVN_REVISION = "7";
     private static final int LOG_LIMIT = 1000;
     private FreeStyleProject job;
     private String svnUrl;
     private SubversionSCM scm;
     private FreeStyleBuild currentBuild;
     private FreeStyleBuild revertedBuild;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         givenSubversionScmWithOneRepo();
     }
 
     public void testShouldNotRevertWhenNotSubversionSCM() throws Exception {
         givenJobWithNullScm();
 
         currentBuild = whenPreviousJobSuccessfulAndCurrentUnstable();
 
         assertThat(logFor(currentBuild), containsString(Messenger.NOT_SUBVERSION_SCM));
         assertBuildStatus(UNSTABLE, currentBuild);
     }
 
     public void testShouldNotRevertWhenBuildStatusIsSuccess() throws Exception {
         givenJobWithSubversionScm();
         givenChangesInSubversion();
 
         currentBuild = scheduleBuild();
 
         assertThat(logFor(currentBuild), containsString(Messenger.BUILD_STATUS_NOT_UNSTABLE));
         assertBuildStatus(SUCCESS, currentBuild);
         verifyNothingReverted();
     }
 
     public void testShouldLogAndRevertWhenBuildStatusChangesToUnstable() throws Exception {
         givenJobWithSubversionScm();
 
         currentBuild = whenPreviousJobSuccessfulAndCurrentUnstable();
 
         final String buildLog = logFor(currentBuild);
         assertThat(buildLog, containsString(svnUrl));
         assertThat(buildLog, containsString(REVISION_THAT_TRIGGERED_PREVIOUS_BUILD + ":"
                 + REVISION_THAT_TRIGGERED_CURRENT_BUILD));
         assertBuildStatus(UNSTABLE, currentBuild);
         verifySometingReverted();
     }
 
     private void givenSubversionScmWithOneRepo() throws Exception {
         final File repo = new CopyExisting(getClass().getResource("repoAtRevision5.zip")).allocate();
         svnUrl = "file://" + repo.getPath();
         scm = new SubversionSCM(svnUrl);
     }
 
     private void givenChangesInSubversion() throws Exception {
         createCommit(scm, "random_file.txt");
     }
 
     private FreeStyleBuild whenPreviousJobSuccessfulAndCurrentUnstable() throws Exception,
             InterruptedException, ExecutionException {
         givenPreviousBuildSuccessful();
         givenChangesInSubversion();
         givenNextBuildWillBe(UNSTABLE);
         return scheduleBuild();
     }
 
     private void givenPreviousBuildSuccessful() throws Exception {
         assertBuildStatusSuccess(scheduleBuild());
     }
 
     private void givenNextBuildWillBe(final Result result) throws Exception {
         job.getBuildersList().add(new MockBuilder(result));
     }
 
     private void givenJobWithNullScm() throws Exception {
         job = createFreeStyleProject("no-scm-job");
         job.getPublishersList().add(new JenkinsGlue(""));
         job.setScm(new NullSCM());
     }
 
     private void givenJobWithSubversionScm() throws Exception {
         job = createFreeStyleProject("subversion-scm-job");
         job.getPublishersList().add(new JenkinsGlue(""));
         job.setScm(scm);
     }
 
     private String logFor(final FreeStyleBuild build) throws IOException {
         final String log = build.getLog(LOG_LIMIT).toString();
         System.out.println("Log for build: " + log);
         return log;
     }
 
     private FreeStyleBuild scheduleBuild() throws Exception {
         return job.scheduleBuild2(0).get();
     }
 
     private void createCommit(final SubversionSCM scm, final String... paths) throws Exception {
         final FreeStyleProject forCommit = createFreeStyleProject();
         forCommit.setScm(scm);
         forCommit.setAssignedLabel(hudson.getSelfLabel());
         final FreeStyleBuild b = assertBuildStatusSuccess(forCommit.scheduleBuild2(0).get());
         final SVNClientManager svnm = SubversionSCM.createSvnClientManager((AbstractProject)null);
 
         final List<File> added = new ArrayList<File>();
         for (final String path : paths) {
             final FilePath newFile = b.getWorkspace().child(path);
             added.add(new File(newFile.getRemote()));
             if (!newFile.exists()) {
                 newFile.touch(System.currentTimeMillis());
                 svnm.getWCClient().doAdd(new File(newFile.getRemote()),false,false,false, SVNDepth.INFINITY, false,false);
             } else {
                 newFile.write("random content","UTF-8");
             }
         }
         final SVNCommitClient cc = svnm.getCommitClient();
         cc.doCommit(added.toArray(new File[added.size()]),false,"added",null,null,false,false,SVNDepth.EMPTY);
     }
 
     private void verifyNothingReverted() throws Exception, IOException, InterruptedException {
         revertedBuild = scheduleBuild();
         assertEquals(REVISION_THAT_TRIGGERED_CURRENT_BUILD, revertedBuild.getEnvironment().get("SVN_REVISION"));
     }
 
     private void verifySometingReverted() throws Exception, IOException, InterruptedException {
         revertedBuild = scheduleBuild();
         assertEquals(NEXT_SVN_REVISION, revertedBuild.getEnvironment().get("SVN_REVISION"));
     }
 
 }
