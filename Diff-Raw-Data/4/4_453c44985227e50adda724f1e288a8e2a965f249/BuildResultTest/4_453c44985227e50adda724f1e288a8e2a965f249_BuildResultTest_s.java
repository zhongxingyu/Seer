 package hudson.plugins.analysis.test;
 
 import static junit.framework.Assert.*;
 import static org.mockito.Mockito.*;
 import hudson.model.AbstractBuild;
 import hudson.plugins.analysis.Messages;
 import hudson.plugins.analysis.core.BuildHistory;
 import hudson.plugins.analysis.core.BuildResult;
 import hudson.plugins.analysis.core.ParserResult;
 import hudson.plugins.analysis.util.model.DefaultAnnotationContainer;
 import hudson.plugins.analysis.util.model.FileAnnotation;
 import hudson.plugins.analysis.util.model.Priority;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import org.apache.commons.lang.SystemUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.junit.Test;
 
 /**
  * Tests the class {@link BuildResultTest}.
  *
  * @param <T>
  *            type of the result to test
  */
 public abstract class BuildResultTest<T extends BuildResult> extends AbstractEnglishLocaleTest {
     /** Error message. */
     private static final String WRONG_NEW_HIGHSCORE_INDICATOR = "Wrong new highscore indicator.";
     /** Two days in msec. */
     private static final long TWO_DAYS_IN_MS = 2 * DateUtils.MILLIS_PER_DAY;
     /** Error message. */
     private static final String WRONG_ZERO_WARNINGS_HIGH_SCORE = "Wrong zero warnings high score.";
     /** Error message. */
     private static final String WRONG_ZERO_WARNINGS_SINCE_DATE_COUNTER = "Wrong zero warnings since date counter.";
     /** Error message. */
     private static final String WRONG_ZERO_WARNINGS_SINCE_BUILD_COUNTER = "Wrong zero warnings since build counter.";
     /** Error message. */
     private static final String WRONG_NUMBER_OF_ANNOTATIONS = "Wrong number of annotations.";
     /** Filename of the annotation. */
     private static final String FILENAME = "filename";
 
     /**
      * Verifies that the zero warnings since build counter is correctly
      * propagated from build to build.
      */
    @Test
     public void checkThatZeroWarningsIsUpdated() {
         GregorianCalendar calendar = new GregorianCalendar(2008, 8, 8, 12, 30);
         long timeOfFirstZeroWarningsBuild = calendar.getTime().getTime();
 
         T result; // the result is replaced by each new result
 
         // No change of defaults at the beginning
         result = createBuildResult(createBuild(0, calendar), new ParserResult(), createNullHistory());
         verifyResult(0, 0, timeOfFirstZeroWarningsBuild, 0, true, 0, result);
 
         // Compare with a result that has warnings
         result = createDefaultResultWithNoWarnings(1, calendar, createResultWithWarnings());
         verifyResult(0, 1, timeOfFirstZeroWarningsBuild, 0, true, 0, result);
 
         // Again a result without warnings, two days after the first build
         calendar.add(Calendar.DAY_OF_YEAR, 2);
         result = createDefaultResultWithNoWarnings(2, calendar, result);
         verifyResult(0, 1, timeOfFirstZeroWarningsBuild, TWO_DAYS_IN_MS, true, 0, result);
 
         // Now the results contains warnings again, resetting everything besides the high score
         result = createBuildResult(createBuild(3, calendar), createProjectWithWarning(), result);
         verifyResult(1, 0, 0, TWO_DAYS_IN_MS, false, TWO_DAYS_IN_MS, result);
 
         // Now a result without warnings, one day after the previous build, e.g., no high score
         calendar.add(Calendar.DAY_OF_YEAR, 1);
         timeOfFirstZeroWarningsBuild = calendar.getTime().getTime();
         result = createDefaultResultWithNoWarnings(3, calendar, result);
         verifyResult(0, 3, timeOfFirstZeroWarningsBuild, TWO_DAYS_IN_MS, false, TWO_DAYS_IN_MS, result);
 
         // Again a result without warnings, one day after the previous build, e.g., still no high score
         calendar.add(Calendar.DAY_OF_YEAR, 1);
         result = createDefaultResultWithNoWarnings(4, calendar, result);
         verifyResult(0, 3, timeOfFirstZeroWarningsBuild, TWO_DAYS_IN_MS, false, TWO_DAYS_IN_MS - TWO_DAYS_IN_MS / 2, result);
 
         // Again a result without warnings, one day after the previous build, e.g., still no high score
         calendar.add(Calendar.DAY_OF_YEAR, 1);
         result = createDefaultResultWithNoWarnings(5, calendar, result);
         verifyResult(0, 3, timeOfFirstZeroWarningsBuild, TWO_DAYS_IN_MS, false, 0, result);
 
         // Finally, a result without warnings, three more days after the previous build, e.g., a high score of 4 days
         calendar.add(Calendar.DAY_OF_YEAR, 2);
         result = createDefaultResultWithNoWarnings(4, calendar, result);
         verifyResult(0, 3, timeOfFirstZeroWarningsBuild, 2 * TWO_DAYS_IN_MS, true, 0, result);
 
         result.getDataFile().delete();
     }
 
     /**
      * Creates a history that has no previous and reference builds.
      *
      * @return a null history object
      */
     private BuildHistory createNullHistory() {
         BuildHistory history = mock(BuildHistory.class);
 
         when(history.getReferenceAnnotations()).thenReturn(new DefaultAnnotationContainer());
 
         return history;
     }
 
     /**
      * Verifies that the zero warnings since build counter is correctly
      * initialized in the beginning.
      */
     @Test
     public void checkZeroWarningsCounterInitialization() {
         GregorianCalendar calendar = new GregorianCalendar(2008, 8, 8, 12, 30);
         long timeOfFirstZeroWarningsBuild = calendar.getTime().getTime();
 
         T result = createBuildResult(createBuild(0, calendar), new ParserResult(), createNullHistory());
         verifyResult(0, 0, timeOfFirstZeroWarningsBuild, 0, true, 0, result);
 
         calendar.add(Calendar.DAY_OF_YEAR, 2);
         result = createBuildResult(createBuild(0, calendar), new ParserResult(), result);
         verifyResult(0, 0, timeOfFirstZeroWarningsBuild, TWO_DAYS_IN_MS, true, 0, result);
 
         result.getDataFile().delete();
     }
 
     /**
      * Verifies that the zero warnings since build counter is correctly
      * initialized in the beginning.
      */
     @Test
     public void checkZeroWarningsCounterInitializationStartUnstable() {
         GregorianCalendar calendar = new GregorianCalendar(2008, 8, 8, 12, 30);
 
         T result = createBuildResult(createBuild(0, calendar), createProjectWithWarning(), createNullHistory());
         verifyResult(1, 0, 0, 0, false, 0, result);
 
         calendar.add(Calendar.DAY_OF_YEAR, 2);
         long timeOfFirstZeroWarningsBuild = calendar.getTime().getTime();
 
         result = createBuildResult(createBuild(1, calendar), new ParserResult(), result);
         verifyResult(0, 1, timeOfFirstZeroWarningsBuild, 0, true, 0, result);
 
         calendar.add(Calendar.DAY_OF_YEAR, 2);
         result = createBuildResult(createBuild(0, calendar), new ParserResult(), result);
         verifyResult(0, 1, timeOfFirstZeroWarningsBuild, TWO_DAYS_IN_MS, true, 0, result);
 
         result.getDataFile().delete();
     }
 
     /**
      * Creates a new result that has no warnings.
      *
      * @param buildNumner
      *            build ID
      * @param calendar
      *            current calendar
      * @param previousResult
      *            previous result
      * @return the new result
      */
     private T createDefaultResultWithNoWarnings(final int buildNumner, final GregorianCalendar calendar, final T previousResult) {
         return createBuildResult(createBuild(buildNumner, calendar), new ParserResult(), previousResult);
     }
 
     /**
      * Returns a result with some warnings.
      *
      * @return a result with some warnings
      */
     private T createResultWithWarnings() {
         ParserResult projectWithWarning = createProjectWithWarning();
         return createBuildResult(createBuild(42, new GregorianCalendar(2008, 1, 1, 12, 00)), projectWithWarning, createNullHistory());
     }
 
     /**
      * Verifies the build result.
      *
      * @param expectedAnnotationCount
      *            expected number of annotations
      * @param expectedZeroWarningsBuildNumber
      *            expected build number of last zero warnings build
      * @param expectedZeroWarningsBuildDate
      *            expected build date of last zero warnings build
      * @param expectedHighScore
      *            expected highscore time
      * @param expectedIsNewHighScore
      *            expected value of is highscore flag
      * @param gap
      *            gap of msec to reach highscore
      * @param result
      *            the actual result to verify
      */
     private void verifyResult(final int expectedAnnotationCount, final int expectedZeroWarningsBuildNumber,
             final long expectedZeroWarningsBuildDate, final long expectedHighScore, final boolean expectedIsNewHighScore, final long gap, final T result) {
         assertEquals(WRONG_NUMBER_OF_ANNOTATIONS, expectedAnnotationCount, result.getNumberOfAnnotations());
         assertEquals(WRONG_ZERO_WARNINGS_SINCE_BUILD_COUNTER, expectedZeroWarningsBuildNumber, result.getZeroWarningsSinceBuild());
         assertEquals(WRONG_ZERO_WARNINGS_SINCE_DATE_COUNTER, expectedZeroWarningsBuildDate, result.getZeroWarningsSinceDate());
         assertEquals(WRONG_ZERO_WARNINGS_HIGH_SCORE, expectedHighScore, result.getZeroWarningsHighScore());
         assertEquals(WRONG_NEW_HIGHSCORE_INDICATOR, expectedIsNewHighScore, result.isNewZeroWarningsHighScore());
 
         verifyHighScoreMessage(expectedZeroWarningsBuildNumber, expectedIsNewHighScore, expectedHighScore, gap, result);
     }
 
     /**
      * Verifies the highscore message.
      *
      * @param expectedZeroWarningsBuildNumber
      *            expected build number of last zero warnings build
      * @param expectedIsNewHighScore
      *            expected value of is highscore flag
      * @param expectedHighScore
      *            expected highscore time
      * @param gap
      *            gap of msec to reach highscore
      * @param result
      *            the actual result to verify
      */
     protected void verifyHighScoreMessage(final int expectedZeroWarningsBuildNumber, final boolean expectedIsNewHighScore, final long expectedHighScore, final long gap, final T result) {
         if (result.hasNoAnnotations() && result.getDelta() == 0) {
             assertTrue(result.getDetails().contains(Messages.ResultAction_NoWarningsSince(expectedZeroWarningsBuildNumber)));
             if (expectedIsNewHighScore) {
                 long days = BuildResult.getDays(expectedHighScore);
                 assertTrue("Wrong message", result.getDetails().contains(Messages.ResultAction_MultipleHighScore(days)));
             }
             else {
                 long days = BuildResult.getDays(gap);
                 if (days == 1) {
                     assertTrue("Wrong message", result.getDetails().contains(Messages.ResultAction_OneNoHighScore()));
                 }
                 else {
                     assertTrue("Wrong message", result.getDetails().contains(Messages.ResultAction_MultipleNoHighScore(days)));
                 }
             }
         }
     }
 
     /**
      * Creates a project that contains a single annotation.
      *
      * @return the created project
      */
     private ParserResult createProjectWithWarning() {
         ParserResult project = new ParserResult();
 
         FileAnnotation annotation = mock(FileAnnotation.class);
         when(annotation.getPriority()).thenReturn(Priority.HIGH);
         when(annotation.getFileName()).thenReturn(FILENAME);
         when(annotation.getModuleName()).thenReturn("Module");
         when(annotation.getPackageName()).thenReturn("Package");
         project.addAnnotation(annotation);
 
         return project;
     }
 
     /**
      * Creates a mock for the build.
      *
      * @param buildNumber
      *            the current build number
      * @param calendar
      *            calendar representing the time of the build
      * @return a mock for the build
      */
     private AbstractBuild<?, ?> createBuild(final int buildNumber, final Calendar calendar) {
         AbstractBuild<?, ?> build = mock(AbstractBuild.class);
 
         when(build.getTimestamp()).thenReturn(calendar);
         when(build.getRootDir()).thenReturn(SystemUtils.getJavaIoTmpDir());
         when(build.getNumber()).thenReturn(buildNumber);
 
         return build;
     }
 
     /**
      * Creates the build result under test.
      *
      * @param build
      *            the current build
      * @param project
      *            the project of the current build
      * @param history
      *            the history of build results of the associated plug-in
      * @return the build result under test
      */
     protected abstract T createBuildResult(AbstractBuild<?, ?> build, ParserResult project, BuildHistory history);
 
     /**
      * Creates the build result under test.
      *
      * @param build
      *            the current build
      * @param project
      *            the project of the current build
      * @param previousResult
      *            the result of the previous build
      * @return the build result under test
      */
     private T createBuildResult(final AbstractBuild<?, ?> build, final ParserResult project, final T previousResult) {
         BuildHistory history = mock(BuildHistory.class);
         DefaultAnnotationContainer value = new DefaultAnnotationContainer(project.getAnnotations());
         when(history.getReferenceAnnotations()).thenReturn(value);
         when(history.hasPreviousResult()).thenReturn(true);
         when(history.getPreviousResult()).thenReturn(previousResult);
 
         return createBuildResult(build, project, history);
     }
 }
 
