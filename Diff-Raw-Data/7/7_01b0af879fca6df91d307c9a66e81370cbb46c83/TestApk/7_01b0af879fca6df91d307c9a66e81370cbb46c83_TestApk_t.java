 package com.testcalculator;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 
 @SuppressWarnings("unchecked")
 public class TestApk extends ActivityInstrumentationTestCase2{
 
 private static final String TARGET_PACKAGE_ID="com.calculator";
 private static final String LAUNCHER_ACTIVITY_FULL_CLASSNAME="com.calculator.Main";
 private static Class launcherActivityClass;
 static{
 
 try
 {
 launcherActivityClass = Class.forName(LAUNCHER_ACTIVITY_FULL_CLASSNAME);
 } catch (ClassNotFoundException e){
 throw new RuntimeException(e);
 }
 }
 
 
 public TestApk()throws ClassNotFoundException{
 super(TARGET_PACKAGE_ID,launcherActivityClass);
 }
 
/*private Solo solo;
 
 @Override
 protected void setUp() throws Exception
 {
 solo = new Solo(getInstrumentation(),getActivity());
 }
 
 public void testDisplayBlackBox() {
 //Enter any integer/decimal value for first editfield, we are writing 10
 	
 solo.enterText(0, "10");
 
 //Enter any interger/decimal value for first editfield, we are writing 20
 solo.enterText(1, "20");
 
 //Click on Multiply button
 solo.clickOnButton("Multiply");
 
 //Verify that resultant of 10 x 20
 assertTrue(solo.searchText("200"));
 }
 
 @Override
 public void tearDown() throws Exception {
 solo.finishOpenedActivities();
}*/
 
 }
