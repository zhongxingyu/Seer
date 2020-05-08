 package com.Grupp01.gymapp.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.TextView;
 
 import com.Grupp01.gymapp.View.Workout.ListWorkoutActivity;
 import com.Grupp01.gymapp.View.Workout.RegisterCardioActivity;
 import com.Grupp01.gymapp.View.Workout.RegisterDynamicActivity;
 import com.Grupp01.gymapp.View.Workout.RegisterStaticActivity;
 import com.jayway.android.robotium.solo.Solo;
 
 public class TestWorkoutRegister extends ActivityInstrumentationTestCase2<ListWorkoutActivity> {
 
 	private Solo solo;
 	
 	 public static final String FULL_BODY = "Full body";
 	 public static final String TRIATHLON = "Triathlon";
 	 public static final String SWIMMING = "Swimming";
 	 public static final String WRONG_ACTIVITY = "Wrong activity"; 
 	 public static final String ADD_SET = "Add set";
 	 public static final String BENCH_PRESS = "Bench Press";
 	 public static final String CHINS = "Chins";
 	 public static final String PUSH_UPS = "Push-ups";
 	 public static final String EDIT_WORKOUT = "EDIT WORKOUT!";
 	 public static final String START = "Start";
 	 public static final int TIME = 1500;
 	
 	/**The constructor for the testWorkoutA
 	 * 
 	 */
 	public TestWorkoutRegister() {
 		//Get the listWorkoutActivity as start class
 		super(ListWorkoutActivity.class);
 	}
 
 	/**Method for a "clean" start on every test case. Runs in the beginning
 	 * of every test case.
 	 * 
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 	/**Testing to start a cardio-exercise. If the exercise is started,
 	 * the test is ok.
 	 * Reference to Test-case in documentation: 2.9
 	 */
 	public void testToStartAnCardioExercise()
 	{
 		//Click on the workout called "Full body"
 		solo.clickOnText(TRIATHLON);
 		//Start the workout
 		solo.clickOnText(START);
 		//Go to chins exercise
 		solo.clickOnText(SWIMMING);
 		//Check if the current activity is the cardioActivity
 		solo.assertCurrentActivity(WRONG_ACTIVITY, RegisterCardioActivity.class);
 	}
 	
 	/**Testing to register an invalid TIME(in our case, 0 minutes and 0 seconds).
 	 * When adding the set, no set is added. If nothing is added, test is ok.
 	 * Reference to Test-case in documentation: 2.10
 	 */
 	public void testRegisterCardioNotValidTime()
 	{
 		solo.clickOnText(TRIATHLON);
 		solo.clickOnText(START);
 		solo.clickOnText(SWIMMING);
 		
 		//Add one set with invalid time, first field is minutes
 		solo.enterText(0, "0");
 		//Field for seconds
 		solo.enterText(1, "0");
 		//Field for distance
 		solo.enterText(2, "3");
 		//Press the button "Add set"
 		solo.clickOnButton(ADD_SET);
 		solo.sleep(TIME);
 		//Get the textview from the activity
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSetsCardio);
 		//Check if the textfield is empty
 		assertEquals("", text.getText());
 	}
 	
 	/**Testing to register an valid TIME in Cardio, if the set exist,
 	 * the test is ok.
 	 * Reference to Test-case in documentation: 2.11
 	 */
 	public void testRegisterCardioValidTime()
 	{
 		solo.clickOnText(TRIATHLON);
 		solo.clickOnText(START);
 		solo.clickOnText(SWIMMING);
 
 		//Add one set with valid time, first field is minutes
 		solo.enterText(0, "16");
 		//Second field is seconds
 		solo.enterText(1, "10");
 		//Last field is distance
 		solo.enterText(2, "8");
 		//Click on "add set"
 		solo.clickOnButton(ADD_SET);
 		solo.sleep(TIME);
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSetsCardio);
 		//Check if the set was added
		assertEquals("16m 10s x8km, ", text.getText());
 	}
 	
 	/** Testing to start a static exercise, the starting activity should be
 	 * 	RegisterStaticActivity.
 	 * Reference to Test-case in documentation: 2.12
 	 */
 	public void testToStartAnStaticExercise()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		//Start the exercise called "Push-ups"
 		solo.clickOnText(PUSH_UPS);
 		//Check if the current activity is a Static-exercise activity
 		solo.assertCurrentActivity(WRONG_ACTIVITY, RegisterStaticActivity.class);
 	}
 	
 	/**Testing to add a set on a static-exercise with a invalid TIME.
 	 * If it doesn't find the set when searching for it, the test is ok.
 	 * Reference to Test-case in documentation: 2.13
 	 */
 	public void testRegisterStaticExerciseNotValidTime()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		solo.clickOnText(PUSH_UPS);
 		//Add an invalid time, first field minutes
 		solo.enterText(0, "0");
 		//Second field seconds
 		solo.enterText(1, "0");
 		//Last field, extra weight
 		solo.enterText(2, "3");
 		solo.sleep(TIME);
 		//Get the textview
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSetsStatic);
 		//Check if the set was added
 		assertEquals("",text.getText());
 	}
 	
 	/**Testing to add a set on a static-exercise with a valid time, when added a set it searches if the set is added.
 	 * If added, test is ok.
 	 * Reference to Test-case in documentation: 2.14
 	 */
 	public void testRegisterStaticExerciseValidTime()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		solo.clickOnText(PUSH_UPS);
 		//Add a valid time
 		solo.enterText(0, "0");
 		solo.enterText(1, "3");
 		solo.enterText(2, "3");
 		solo.clickOnText(ADD_SET);
 		solo.sleep(TIME);
 		//Get the textview
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSetsStatic);
 		//Check if the set was added
		assertEquals("0:3 3 | ",text.getText() );
 	}
 	
 	/** Testing to start a dynamic exercise, the starting activity should be
 	 * 	RegisterDynamicActivity.
 	 * Reference to Test-case in documentation: 2.15
 	 */
 	public void testToStartAnDynamicExercise()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		//Start the exercise called "Bench-press"
 		solo.clickOnText(BENCH_PRESS);
 		//Check if the current activity is a dynamic-exercise activity
 		solo.assertCurrentActivity(WRONG_ACTIVITY, RegisterDynamicActivity.class);
 	}
 	/**Testing to add a set on a dynamic-exercise with no reps.
 	 * If it doesn't find the set when searching for it, the test is ok.
 	 * Reference to Test-case in documentation: 2.16
 	 */
 	public void testRegisterDynamicExerciseNoRepsInField()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		solo.clickOnText(BENCH_PRESS);
 		//Enter an invalid number of reps, first field is reps
 		solo.enterText(0, "0");
 		//Second field is weight
 		solo.enterText(1, "3");
 		solo.clickOnButton(ADD_SET);
 		solo.sleep(TIME);
 		//Get textview
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSets);
 		//Check if the set was added
 		assertEquals("", text.getText());
 
 	}
 	
 	/**Testing to add a set on a dynamic-exercise with no reps filled in reps field.
 	 *(The user hasn't written in any reps at all)
 	 * If it doesn't find the set when searching for it, the test is ok.
 	 * Reference to Test-case in documentation: 2.16
 	 */
 	public void testRegisterDynamicExerciseNoWeight()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		solo.clickOnText(BENCH_PRESS);
 		//
 		solo.enterText(0, "");
 		solo.enterText(1, "3");
 		solo.clickOnButton(ADD_SET);
 		solo.sleep(TIME);
 		//Get textview
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSets);
 		//Check if an error message pops-up and if the set was added
 		if(solo.searchText("Cant add set with 0 repetitions"))
 				 assertTrue(text.equals(""));
 		
 	}
 	
 	/**Testing to add a set on a dynamic-exercise with reps, when added a set it searches if the set is added.
 	 * If added, the test is ok.
 	 * Reference to Test-case in documentation: 2.17
 	 */
 	public void testRegisterDynamicExerciseReps()
 	{
 		solo.clickOnText(FULL_BODY);
 		solo.clickOnText(START);
 		solo.clickOnText(BENCH_PRESS);
 		solo.enterText(0, "3");
 		solo.enterText(1, "3");
 		solo.clickOnButton(ADD_SET);
 		solo.sleep(TIME);
 		//get textview
 		TextView text = (TextView) solo.getView(com.Grupp01.gymapp.R.id.thisTimeSets);
 		//Check if the set was added
		assertEquals(" 3x3kg, ", text.getText());
 	}
 	/**When a test method are done, close it.
 	 */
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
