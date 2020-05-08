 package seclogin.historyfile;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import seclogin.MeasurementStats;
 import seclogin.TestRandom;
 import seclogin.User;
 
 import java.util.Arrays;
 import java.util.Random;
 
 public class HistoryFileTest {
 
     Random random;
 
     int maxNrOfEntries = 2;
     int nrOfFeatures = 3;
     final HistoryFileParams params = new HistoryFileParams(maxNrOfEntries, nrOfFeatures);
 
     @Before
     public void setUp() throws Exception {
         random = TestRandom.random();
     }
 
     @Test
     public void testEmptyHistoryFile() throws Exception {
         HistoryFile empty = HistoryFile.emptyHistoryFile(new User("asdf"), params);
 
         Assert.assertEquals(0, empty.nrOfMeasurements);
 
         double[][] expectedMeasurements = new double[params.maxNrOfMeasurements][params.nrOfFeatures];
         for (double[] expectedMeasurement : expectedMeasurements) {
             Arrays.fill(expectedMeasurement, Double.NaN);
         }
         Assert.assertArrayEquals(expectedMeasurements, empty.measurements);
     }
 
     @Test
     public void testVerifyUserHash() throws Exception {
         User user = new User("someuser");
         HistoryFile historyFile = HistoryFile.emptyHistoryFile(user, params);
         Assert.assertTrue(historyFile.verifyUserHash(new User("someuser")));
         Assert.assertFalse(historyFile.verifyUserHash(new User("someotheruser")));
     }
 
     @Test
     public void testWithMostRecentMeasurements() throws Exception {
         HistoryFile empty = HistoryFile.emptyHistoryFile(new User("asdf"), params);
         double[] measurements = new double[params.nrOfFeatures];
         for (int i = 0; i < measurements.length; i++) {
             measurements[i] = random.nextInt(20);
         }
         HistoryFile withMeasurements = empty.withMostRecentMeasurements(measurements);
 
         double[][] expectedMeasurements = new double[params.maxNrOfMeasurements][params.nrOfFeatures];
         for (double[] expectedMeasurement : expectedMeasurements) {
             Arrays.fill(expectedMeasurement, Double.NaN);
         }
         expectedMeasurements[0] = measurements;
         Assert.assertArrayEquals(expectedMeasurements, withMeasurements.measurements);
 
 
         double[] moreMeasurements = new double[params.nrOfFeatures];
         for (int i = 0; i < moreMeasurements.length; i++) {
             moreMeasurements[i] = random.nextInt(20);
         }
         withMeasurements = withMeasurements.withMostRecentMeasurements(moreMeasurements);
         expectedMeasurements[0] = moreMeasurements;
         expectedMeasurements[1] = measurements;
         Assert.assertArrayEquals(expectedMeasurements, withMeasurements.measurements);
     }
 
     @Test
     public void testCalculateStats() throws Exception {
         HistoryFile file = HistoryFile.emptyHistoryFile(new User("asdf"), new HistoryFileParams(4, 2));
         file = file.withMostRecentMeasurements(new double[]{1.0, 10.0});
         file = file.withMostRecentMeasurements(new double[]{2.0, Double.NaN});
         file = file.withMostRecentMeasurements(new double[]{Double.NaN, 20.0});
         file = file.withMostRecentMeasurements(new double[]{6.0, 60.0});
         System.out.println("asdfasfd " + file.nrOfMeasurements);
 
         MeasurementStats[] stats = file.calculateStats();
         MeasurementStats[] expectedStats = new MeasurementStats[]{
                new MeasurementStats(9.0/3.0, Math.sqrt(7), 1.0/4.0),
                new MeasurementStats(90.0/3.0, Math.sqrt(700), 1.0/4.0)
         };
         Assert.assertArrayEquals(expectedStats, stats);
     }
 }
