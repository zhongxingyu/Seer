 package nl.coralic.beta.sms.test;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import nl.coralic.beta.sms.*;
 
 public class S006_WizardActivityTest extends ActivityInstrumentationTestCase2<Wizard>
 {
     private Solo solo;
     //TODO: username/password/provider not in the code?
    private static String username = "testingwbc";
    private static String password = "wbcwbc123";
     private static int provider = 31;
 
     public S006_WizardActivityTest()
     {
 	super("nl.coralic.beta.sms", Wizard.class);
     }
 
     @Override
     protected void setUp() throws Exception
     {
 	super.setUp();
 	solo = new Solo(getInstrumentation(), getActivity());
     }
 
     @Override
     public void tearDown() throws Exception
     {
 
 	try
 	{
 	    solo.finalize();
 	}
 	catch (Throwable e)
 	{
 
 	    e.printStackTrace();
 	}
 	getActivity().finish();
 	super.tearDown();
 
     }
     
     public void testUserPassCorrect()
     {
 	solo.pressSpinnerItem(0, provider);	
 	solo.enterText((EditText)solo.getView(nl.coralic.beta.sms.R.id.txtUsername), username);
 	solo.enterText((EditText)solo.getView(nl.coralic.beta.sms.R.id.txtPassword), password);
 	solo.clickOnButton("Done");
 	assertTrue(solo.searchText("Verifying account"));
 	solo.waitForDialogToClose(5000);
 	assertEquals("", ((EditText)solo.getView(nl.coralic.beta.sms.R.id.txtUsername)).getText().toString());
     }
 }
