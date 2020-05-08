 package ch.yarb.service.impl;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import ch.yarb.service.api.YarbService;
 import ch.yarb.service.to.ChangeType;
 import ch.yarb.service.to.ChangedPath;
 import ch.yarb.service.to.LogEntry;
 import ch.yarb.service.to.RepoConfiguration;
 import ch.yarb.service.to.RevisionRange;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Tests for {@link YarbServiceImpl}.
  *
  * @author pellaton
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = "/test-context.xml", inheritLocations = false)
 public class YarbServiceImplTest {
 
   @Autowired
   private YarbService service;
 
   /**
    * Tests {@link YarbServiceImpl#ping()}.
    */
   @Test
   public void ping() {
     assertEquals("yarb", this.service.ping());
   }
 
   /**
    * Tests the {@code null} check of the {@code logRepoConfigurationNull}
    * {@link YarbServiceImpl#getRepositoryLog(RepoConfiguration, RevisionRange)}.
    */
   @Test(expected = IllegalArgumentException.class)
   public void getRepositoryLogRepoConfigurationNull() {
     this.service.getRepositoryLog(null, RevisionRange.ALL);
   }
 
   /**
    * Tests the {@code null} check of the {@code revisionRange}
    * {@link YarbServiceImpl#getRepositoryLog(RepoConfiguration, RevisionRange)}.
    */
   @Test(expected = IllegalArgumentException.class)
   public void getRepositoryLogRevisionRangeNull() {
     this.service.getRepositoryLog(new RepoConfiguration(null, null, null), null);
   }
 
   /**
    * Tests {@link YarbServiceImpl#getRepositoryLog(RepoConfiguration, RevisionRange)}.
    */
   @Test
   public void getRepositoryLogRevision() {
     List<LogEntry> repositoryLog = this.service.getRepositoryLog(new RepoConfiguration(
        "file:///Users/michael/.gitrepositories/yarb/yarb-service/src/test/resources/svntestrepo/",
         "anonymous", "anonymous"),
         RevisionRange.ALL);
     assertNotNull(repositoryLog);
     assertFalse(repositoryLog.isEmpty());
     assertTrue(repositoryLog.size() >= 8);
     LogEntry logEntry = repositoryLog.get(1);
     assertNotNull(logEntry);
     assertEquals("commit revision", "1", logEntry.getRevision());
     assertEquals("commit author", "michael", logEntry.getAuthor());
     assertEquals("commit comment", "bump basic directory structure", logEntry.getComment());
     assertNotNull("commit timestamp", logEntry.getTimestamp());
     List<ChangedPath> changedPathList = logEntry.getChangedPathList();
     assertNotNull(changedPathList);
     assertFalse(changedPathList.isEmpty());
     assertEquals(3, changedPathList.size());
     assertEquals("/branches", changedPathList.get(0).getPath());
     assertEquals(ChangeType.ADDED, changedPathList.get(0).getChangeType());
   }
 }
