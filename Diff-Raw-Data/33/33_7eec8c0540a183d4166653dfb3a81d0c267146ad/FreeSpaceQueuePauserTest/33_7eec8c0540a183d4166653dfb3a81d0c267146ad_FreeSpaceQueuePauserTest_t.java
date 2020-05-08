 package jetbrains.buildServer.queueManager.server;
 
 import jetbrains.buildServer.BaseTestCase;
 import jetbrains.buildServer.queueManager.settings.QueueState;
 import jetbrains.buildServer.queueManager.settings.QueueStateManager;
 import jetbrains.buildServer.serverSide.*;
 import jetbrains.buildServer.serverSide.impl.DiskSpaceWatcher;
 import jetbrains.buildServer.users.User;
 import jetbrains.buildServer.util.EventDispatcher;
 import jetbrains.buildServer.util.FileUtil;
 import jetbrains.buildServer.util.TestFor;
 import org.jetbrains.annotations.NotNull;
 import org.jmock.Expectations;
 import org.jmock.Mockery;
 import org.jmock.lib.legacy.ClassImposteriser;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.lang.reflect.Field;
 import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
 
 /**
  * Pauses build queue based on free space left
  *
  * @author Oleg Rybak (oleg.rybak@jetbrains.com)
  */
 @TestFor(testForClass = {FreeSpaceQueuePauser.class}, issues = "TW-10787")
 public class FreeSpaceQueuePauserTest extends BaseTestCase {
 
   private Mockery m;
 
   private DiskSpaceWatcher myDiskSpaceWatcher;
 
   private EventDispatcher<BuildServerListener> myDispatcher;
 
   private QueueStateManager myQueueStateManager;
 
   private QueueState myQueueState;
 
   @BeforeMethod
   @Override
   @SuppressWarnings("unchecked")
   protected void setUp() throws Exception {
     super.setUp();
     m = new Mockery() {{
       setImposteriser(ClassImposteriser.INSTANCE);
     }};
     myDiskSpaceWatcher = m.mock(DiskSpaceWatcher.class);
     myDispatcher = m.mock(EventDispatcher.class);
     myQueueState = m.mock(QueueState.class);
     myQueueStateManager = m.mock(QueueStateManager.class);
   }
 
  @Test(enabled = false)
   public void testBuildQueueChanged() {
     runTests(setupForBuildQueueChanged());
   }
 
  @Test(enabled = false)
   public void testBuildTypeAddedToQueue() {
     runTests(setupForBuildTypeAdded());
   }
 
  @Test(enabled = false)
   public void testQueueBuildAddedToQueue() {
     runTests(setupForQueuedBuildTypeAdded());
   }
 
  @Test(enabled = false)
   public void testBuildRemovedFromQueue() {
     runTests(setupForQueuedBuildRemoved());
   }
 
   private void runTests(@NotNull final Runnable r) {
     testQueuePaused_DoNothing(r);
     testSpaceSufficient(r);
     testSpaceInsufficient_PauseQueue(r);
     testDisabled_DoNothing(r);
   }
 
   @NotNull
   private Runnable setupForQueuedBuildRemoved() {
     return new Runnable() {
       private final SQueuedBuild myBuildType = m.mock(SQueuedBuild.class);
       private final User myUser = m.mock(User.class);
       private final String myComment = "Comment";
 
       @Override
       public void run() {
         final FreeSpaceQueuePauser pauser = new FreeSpaceQueuePauser(myDispatcher, myQueueStateManager, myDiskSpaceWatcher);
         pauser.buildRemovedFromQueue(myBuildType, myUser, myComment);
       }
     };
   }
 
   @NotNull
   private Runnable setupForQueuedBuildTypeAdded() {
     return new Runnable() {
       private final SQueuedBuild myBuildType = m.mock(SQueuedBuild.class);
 
       @Override
       public void run() {
         final FreeSpaceQueuePauser pauser = new FreeSpaceQueuePauser(myDispatcher, myQueueStateManager, myDiskSpaceWatcher);
         pauser.buildTypeAddedToQueue(myBuildType);
       }
     };
   }
 
   @NotNull
   private Runnable setupForBuildQueueChanged() {
     return new Runnable() {
       @Override
       public void run() {
         final FreeSpaceQueuePauser pauser = new FreeSpaceQueuePauser(myDispatcher, myQueueStateManager, myDiskSpaceWatcher);
         pauser.buildQueueOrderChanged();
       }
     };
   }
 
   @NotNull
   private Runnable setupForBuildTypeAdded() {
     return new Runnable() {
       private final SBuildType myBuildType = m.mock(SBuildType.class);
 
       @Override
       public void run() {
         final FreeSpaceQueuePauser pauser = new FreeSpaceQueuePauser(myDispatcher, myQueueStateManager, myDiskSpaceWatcher);
         pauser.buildTypeAddedToQueue(myBuildType);
       }
     };
   }
 
   private void testQueuePaused_DoNothing(@NotNull final Runnable r) {
     m.checking(new Expectations() {{
       allowing(myDispatcher);
 
       oneOf(myQueueStateManager).readQueueState();
       will(returnValue(myQueueState));
 
       oneOf(myQueueState).isQueueEnabled();
       will(returnValue(false));
     }});
     r.run();
   }
 
   private void testSpaceSufficient(@NotNull final Runnable r) {
     m.checking(new Expectations() {{
       allowing(myDispatcher);
 
       oneOf(myQueueStateManager).readQueueState();
       will(returnValue(myQueueState));
 
       oneOf(myQueueState).isQueueEnabled();
       will(returnValue(true));
 
      oneOf(myDiskSpaceWatcher).getDirsSpaceCritical();
      will(returnValue(Collections.emptyMap()));
     }});
     r.run();
   }
 
   private void testSpaceInsufficient_PauseQueue(@NotNull final Runnable r) {
    final Map<String, Long> paths = new HashMap<String, Long>() {{
      put("path1", 123L);
      put("path2", 123L);
      put("path3", 123L);
     }};
 
     m.checking(new Expectations() {{
       allowing(myDispatcher);
 
       oneOf(myQueueStateManager).readQueueState();
       will(returnValue(myQueueState));
 
       oneOf(myQueueState).isQueueEnabled();
       will(returnValue(true));
 
      oneOf(myDiskSpaceWatcher).getDirsSpaceCritical();
       will(returnValue(paths));
 
       exactly(1).of(same(myQueueStateManager)).method("writeQueueState");
     }});
     r.run();
   }
 
   private void testDisabled_DoNothing(@NotNull final Runnable r) {
     try {
       final String text = "teamcity.internal.properties.reread.interval.ms=100\n" +
               "teamcity.queuePauser.pauseOnNoDiskSpace=false";
       final File myProps = createTempFile(text);
       final FileWatchingPropertiesModel myModel = new FileWatchingPropertiesModel(myProps);
       final Field field = TeamCityProperties.class.getDeclaredField("ourModel");
       field.setAccessible(true);
       field.set(TeamCityProperties.class, myModel);
       FileUtil.writeFileAndReportErrors(myProps, text);
       myModel.forceReloadProperties();
     } catch (Exception e) {
       fail(e.getMessage());
     }
     m.checking(new Expectations() {{
       allowing(myDispatcher);
     }});
     r.run();
 
   }
 }
