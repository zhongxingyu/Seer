 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static play.test.Helpers.fakeApplication;
 import static play.test.Helpers.inMemoryDatabase;
 
 import java.util.Arrays;
 import java.util.List;
 
 import models.Student;
 import models.Tutor;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.test.FakeApplication;
 import play.test.Helpers;
 
 //For session tests:
 import models.Session;
 
 import java.util.Date;
 
 
 
 public class StudentTest {
 
   private static FakeApplication app;
 
   private static final String TEST_USERNAME = "Test Username";
   private static final String TEST_EMAIL = "test@email.com";
   private static final String TEST_NAME = "Test Name";
   private static final String TEST_PASSWORD = "Test Password";
   private static final String TEST_SALT = "1";
 
   private static final String INVALID_SUBJECT = "INVALID";
   private static final String MATH_SUBJECT = "MATH";
   private static final String HISTORY_SUBJECT = "HISTORY";
   private static final String SCIENCE_SUBJECT = "SCIENCE";
   private static final double CHEAP_COST = 1.0;
   private static final double EXPENSIVE_COST = 100.0;
   private static final double LOW_RATING = 1.0;
   private static final double HIGH_RATING = 5.0;
 
   private Student testStudent;
   private Tutor testMathTutor;
   private Tutor testHistoryTutorCheapHighRated;
   private Tutor testHistoryTutorCheapLowRated;
   private Tutor testHistoryTutorExpensiveHighRated;
   private Tutor testHistoryTutorExpensiveLowRated;
   private Tutor testMultiSubjectTutor;
 
   @Before
   public void setUp() {
     app = fakeApplication(inMemoryDatabase());
     Helpers.start(app);
 
     testStudent = new Student();
     testStudent.setUsername(TEST_USERNAME);
     testStudent.setEmail(TEST_EMAIL);
     testStudent.setName(TEST_NAME);
     testStudent.setSalt(TEST_SALT);
     testStudent.setPwhash(Student.encrypt(TEST_PASSWORD, TEST_SALT));
     testStudent.save();
 
     testMathTutor = new Tutor();
     testMathTutor.setSubjects(Arrays.asList(MATH_SUBJECT));
     testMathTutor.setCostUSD(EXPENSIVE_COST);
     testMathTutor.setRating(LOW_RATING);
     testMathTutor.save();
 
     testHistoryTutorCheapHighRated = new Tutor();
     testHistoryTutorCheapHighRated.setSubjects(Arrays.asList(HISTORY_SUBJECT));
     testHistoryTutorCheapHighRated.setCostUSD(CHEAP_COST);
     testHistoryTutorCheapHighRated.setRating(HIGH_RATING);
     testHistoryTutorCheapHighRated.save();
 
     testHistoryTutorCheapLowRated = new Tutor();
     testHistoryTutorCheapLowRated.setSubjects(Arrays.asList(HISTORY_SUBJECT));
     testHistoryTutorCheapLowRated.setCostUSD(CHEAP_COST);
     testHistoryTutorCheapLowRated.setRating(LOW_RATING);
     testHistoryTutorCheapLowRated.save();
 
     testHistoryTutorExpensiveHighRated = new Tutor();
     testHistoryTutorExpensiveHighRated.setSubjects(Arrays
         .asList(HISTORY_SUBJECT));
     testHistoryTutorExpensiveHighRated.setCostUSD(EXPENSIVE_COST);
     testHistoryTutorExpensiveHighRated.setRating(HIGH_RATING);
     testHistoryTutorExpensiveHighRated.save();
 
     testHistoryTutorExpensiveLowRated = new Tutor();
     testHistoryTutorExpensiveLowRated.setSubjects(Arrays
         .asList(HISTORY_SUBJECT));
     testHistoryTutorExpensiveLowRated.setCostUSD(EXPENSIVE_COST);
     testHistoryTutorExpensiveLowRated.setRating(LOW_RATING);
     testHistoryTutorExpensiveLowRated.save();
 
     testMultiSubjectTutor = new Tutor();
     testMultiSubjectTutor.setSubjects(Arrays.asList(HISTORY_SUBJECT,
         SCIENCE_SUBJECT));
     testMultiSubjectTutor.save();
   }
 
   /**
    * Retrieve and verify the fields
    */
   @Test
   public void testFields() {
     assertEquals(TEST_USERNAME, testStudent.getUsername());
     assertEquals(TEST_EMAIL, testStudent.getEmail());
     assertEquals(TEST_NAME, testStudent.getName());
     assertEquals(TEST_SALT, testStudent.getSalt());
     assertEquals(Student.encrypt(TEST_PASSWORD, TEST_SALT),
         testStudent.getPwhash());
   }
 
   /**
    * Tests Student.findStudent and Student.authenticate
    */
   @Test
   public void testAuthentication() {
     // Valid email
     assertEquals(testStudent, Student.findStudent(TEST_EMAIL));
 
     // Valid username
     assertEquals(testStudent, Student.findStudent(TEST_USERNAME));
 
     // Invalid identifier
     assertNull(Student.findStudent(TEST_NAME));
 
     // Valid identifier and password
     assertTrue(Student.authenticate(TEST_EMAIL, TEST_PASSWORD));
 
     // Valid identifier but invalid password
     assertFalse(Student.authenticate(TEST_EMAIL, "NOT " + TEST_PASSWORD));
 
     // Invalid identifier but valid password
     assertFalse(Student.authenticate(TEST_NAME, TEST_PASSWORD));
 
     // Invalid identifier and password
     assertFalse(Student.authenticate(TEST_NAME, "NOT " + TEST_PASSWORD));
   }
 
   /**
    * Test database lookup
    */
   @Test
   public void testLookup() {
     assertTrue(Student.find.all().contains(testStudent));
     assertEquals(testStudent, Student.find.byId(testStudent.getID()));
 
     // Correct email and username
     assertTrue(Student.existsStudent(TEST_USERNAME, TEST_EMAIL));
 
     // Correct email but incorrect username
     assertTrue(Student.existsStudent("NOT " + TEST_USERNAME, TEST_EMAIL));
 
     // Incorrect email but correct username
     assertTrue(Student.existsStudent(TEST_USERNAME, "NOT " + TEST_EMAIL));
 
     // Incorrect email and username
     assertFalse(Student
         .existsStudent("NOT " + TEST_USERNAME, "NOT "
             + TEST_EMAIL));
   }
 
   /**
    * Test session
    */
   /*
   @Test
   public void testSession() {
     java.util.Date date = new java.util.Date();
     testSimpleSession = new Session(testStudent, testMathTutor, date.getTime()+30000, date.getTime()+3030000);
     assertEqual(testSimpleSession.getScribblarId(), );
     testSimpleSession.activateRoom();
     testSimpleSession.deactivateRoom();
   }
   */
   // TODO test the emails for creating and canceling requests
 
   /**
    * Test Students.searchForTutors
    */
   @Test
   public void testSearchForTutor() {
     // Subject with no tutors
     assertTrue(Student.searchForTutors(INVALID_SUBJECT).isEmpty());
 
     // Subject with 1 tutor
     assertEquals(testMathTutor, Student.searchForTutors(MATH_SUBJECT));
 
     // Subject with many tutors
     List<Tutor> historyResults = Student.searchForTutors(HISTORY_SUBJECT);
     assertTrue(historyResults.size() == 5);
     assertTrue(historyResults.contains(testHistoryTutorCheapHighRated));
     assertTrue(historyResults.contains(testHistoryTutorCheapLowRated));
     assertTrue(historyResults.contains(testHistoryTutorExpensiveHighRated));
     assertTrue(historyResults.contains(testHistoryTutorExpensiveLowRated));
     assertTrue(historyResults.contains(testMultiSubjectTutor));
 
     // Cost range containing no tutors
     assertTrue(Student.searchForTutors(MATH_SUBJECT, CHEAP_COST, CHEAP_COST)
         .isEmpty());
 
     // Cost range containing 1 tutor
     assertEquals(Arrays.asList(testMathTutor),
         Student.searchForTutors(MATH_SUBJECT, EXPENSIVE_COST, EXPENSIVE_COST));
 
     // Cost range containing multiple tutor
     List<Tutor> cheapHistoryResults = Student.searchForTutors(HISTORY_SUBJECT,
         CHEAP_COST,
         CHEAP_COST);
     assertTrue(cheapHistoryResults.size() == 2);
     assertTrue(cheapHistoryResults.contains(testHistoryTutorCheapHighRated));
     assertTrue(cheapHistoryResults.contains(testHistoryTutorCheapLowRated));
 
     // Rating range containing no tutors
     assertTrue(Student.searchForTutors(MATH_SUBJECT, EXPENSIVE_COST,
         EXPENSIVE_COST, HIGH_RATING).isEmpty());
 
     // Rating range containing 1 tutor
     assertEquals(Arrays.asList(testHistoryTutorExpensiveHighRated),
         Student.searchForTutors(MATH_SUBJECT, EXPENSIVE_COST, EXPENSIVE_COST,
             HIGH_RATING));
 
     // Rating range containing multiple tutors
     List<Tutor> expensiveHighRatedHistoryResults = Student.searchForTutors(
         HISTORY_SUBJECT, EXPENSIVE_COST, EXPENSIVE_COST, LOW_RATING);
     assertTrue(expensiveHighRatedHistoryResults.size() == 2);
     assertTrue(expensiveHighRatedHistoryResults
         .contains(testHistoryTutorExpensiveLowRated));
     assertTrue(expensiveHighRatedHistoryResults
         .contains(testHistoryTutorExpensiveHighRated));
 
     //Metamorphic property:
     // If we search for HISTORY_SUBJECT for any combination of mincost, maxcost, and rating,
     //  the list of results should be a subset of the list of all unconstrained history results
    double[] costs = {CHEAP_COST, EXPENSIVE_COST};
    double[] ratings = {LOW_RATING, HIGH_RATING};
     for(double cost1 : costs) {
       for(double cost2 : costs) {
         for(double rating : ratings) {
           for(Tutor thisResult : Student.searchForTutors(HISTORY_SUBJECT, cost1, cost2, rating)) {
             assertTrue(historyResults.contains(thisResult));
           }
         }
       }
     }
 
    /* // Metamorphic property:
     // Searching for a subject twice should yield the same results
     for(double cost1 : costs) {
       for(double cost2 : costs) {
         for(double rating : ratings) {
             List<Tutor> search1 = Student.searchForTutors(HISTORY_SUBJECT, cost1, cost2, rating);
             List<Tutor> search2 = Student.searchForTutors(HISTORY_SUBJECT, cost1, cost2, rating);
             assertEquals(search1.size(), search2.size());
             for(Tutor tutor : search1){
               assertTrue(search2.contains(tutor));
             }
           }
         }
       }
     */
 
     
   }
 
   @After
   public void tearDown() {
     Helpers.stop(app);
   }
 }
