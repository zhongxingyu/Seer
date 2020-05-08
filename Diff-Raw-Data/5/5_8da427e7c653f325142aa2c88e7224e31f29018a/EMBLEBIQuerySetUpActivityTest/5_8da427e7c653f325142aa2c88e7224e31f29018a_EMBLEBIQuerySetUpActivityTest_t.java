 package com.bioinformaticsapp.test.activities;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 import com.bioinformaticsapp.AppPreferences;
 import com.bioinformaticsapp.EMBLEBISetUpQueryActivity;
 import com.bioinformaticsapp.R;
 import com.bioinformaticsapp.data.BLASTQueryController;
 import com.bioinformaticsapp.data.SearchParameterController;
 import com.bioinformaticsapp.models.BLASTQuery;
 import com.bioinformaticsapp.models.BLASTQuery.Status;
 import com.bioinformaticsapp.models.BLASTVendor;
 import com.bioinformaticsapp.models.SearchParameter;
 import com.bioinformaticsapp.test.testhelpers.OhBLASTItTestHelper;
 import com.jayway.android.robotium.solo.Solo;
 
 public class EMBLEBIQuerySetUpActivityTest extends ActivityInstrumentationTestCase2<EMBLEBISetUpQueryActivity> {
 
 	private BLASTQuery blastQuery;
 	private static final String TAG = "SetUpBLASTQueryActivityTest";
 	
 	private Solo solo;
 	private static final int SENDING_DIALOG_TIMEOUT = 35000;
 	
 	
 	public EMBLEBIQuerySetUpActivityTest() {
 		super("com.bioinformaticsapp", EMBLEBISetUpQueryActivity.class);
 		
 	}
 	
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		
 		blastQuery = new BLASTQuery("blastn", BLASTVendor.EMBL_EBI);
 		
 		OhBLASTItTestHelper helper = new OhBLASTItTestHelper(getInstrumentation().getTargetContext());
 		
 		helper.cleanDatabase();
 		
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		blastQuery = null;
 
 		if(solo != null){
 			solo.finishOpenedActivities();
 		}
 		
 		super.tearDown();
 		
 	}
 	
 	public void testUIComponentsSetToQueryDefaults(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity activity = this.getActivity();
 		
 		Spinner programSpinner = (Spinner)activity.findViewById(R.id.blastqueryentry_program_spinner);
 		Spinner databaseSpinner = (Spinner)activity.findViewById(R.id.blastqueryentry_database_spinner);
 		Spinner thresholdSpinner = (Spinner)activity.findViewById(R.id.blastqueryentry_expthreshold_spinner);
 		Spinner scoreSpinner = (Spinner)activity.findViewById(R.id.blastqueryentry_score_spinner);
 		EditText sequenceEditorView = (EditText)activity.findViewById(R.id.embl_sequence_editor);
		Spinner matchMismatchScoreSpinner = (Spinner)activity.findViewById(R.id.ebi_match_mismatch_score_spinner);
 		assertEquals(blastQuery.getBLASTProgram(), programSpinner.getSelectedItem());
 		assertEquals(blastQuery.getSearchParameter("database").getValue(), databaseSpinner.getSelectedItem());
 		assertEquals(blastQuery.getSearchParameter("exp_threshold").getValue(), thresholdSpinner.getSelectedItem());
 		assertEquals(blastQuery.getSearchParameter("score").getValue(), scoreSpinner.getSelectedItem());
		assertEquals(blastQuery.getSearchParameter("match_mismatch_score").getValue(), matchMismatchScoreSpinner.getSelectedItem());
 		assertEquals("Enter a sequence", sequenceEditorView.getHint().toString());
 	}
 	
 	public void testSequenceEditorViewIsSetToValueInQuery(){
 		Intent intent = new Intent();
 		blastQuery.setSequence("CCTTTATCTAATCTTTGGAGCATGAGCTGG");
 		intent.putExtra("query", blastQuery);
 		
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity activity = getActivity();
 		
 		EditText sequenceEditor = (EditText)activity.findViewById(com.bioinformaticsapp.R.id.embl_sequence_editor);
 		assertEquals("Sequence in Edit text", blastQuery.getSequence(), sequenceEditor.getEditableText().toString());
 		
 	}
 	
 	public void testEmailEditorViewIsSetToValueInQuery(){
 		Intent intent = new Intent();
 		blastQuery.setSearchParameter("email", "h.n.varambhia@gmail.com");
 		intent.putExtra("query", blastQuery);
 		
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity activity = getActivity();
 		
 		EditText emailEditor = (EditText)activity.findViewById(com.bioinformaticsapp.R.id.embl_send_to_email);
 		assertEquals("Sequence in Edit text", blastQuery.getSearchParameter("email").getValue(), emailEditor.getEditableText().toString());
 		
 	}
 	
 	public void testWeCanSaveANewlyCreatedDraftQuery(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		
         solo = new Solo(getInstrumentation(), getActivity());
         solo.clickOnActionBarItem(com.bioinformaticsapp.R.id.save_query);
         getInstrumentation().waitForIdleSync();
         
         BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		
         assertTrue("Query primary key expected to be more than 0 but got "+q.getPrimaryKey(), q.getPrimaryKey() > 0l);
         
         for(SearchParameter parameter: q.getAllParameters()){
         	assertTrue("parameters primary key expected to be more than 0, but got "+parameter.getPrimaryKey(), parameter.getPrimaryKey() > 0l);
         	assertEquals(q.getPrimaryKey().longValue(), parameter.getBlastQueryId().longValue());
         }
         
 	}
  
 	public void testWeCanLoadAndSaveDraftCreatedEarlier(){
 		
 		Intent intent = new Intent();
 		BLASTQueryController controller = new BLASTQueryController(getInstrumentation().getTargetContext());
 		SearchParameterController parametersController = new SearchParameterController(getInstrumentation().getTargetContext());
 		long id = controller.save(blastQuery);
 		blastQuery.setPrimaryKeyId(id);
 		controller.close();
 		List<SearchParameter> theParameters = new ArrayList<SearchParameter>();
 		List<SearchParameter> parameters = blastQuery.getAllParameters();
 		for(SearchParameter parameter: parameters){
 			long parameterId = parametersController.save(parameter);
 			parameter.setPrimaryKey(parameterId);
 			theParameters.add(parameter);
 		}
 		blastQuery.updateAllParameters(theParameters);
 		parametersController.close();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		
 		solo.clickOnActionBarItem(com.bioinformaticsapp.R.id.save_query);
 		getInstrumentation().waitForIdleSync();
 		
         BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		
         assertTrue(q.getPrimaryKey() > 0l);
         
 	}
 	
 	public void testWeCanEditSequenceOfADraftQuery(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		EditText sequenceEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_sequence_editor);
 		solo.typeText(sequenceEditor, "CCTTTATCTAATCTTTGGAGCATGAGCTGG");
 		getInstrumentation().waitForIdleSync();
 		BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		assertEquals(sequenceEditor.getText().toString(), q.getSequence());
 		
 	}
 	
 	public void testWeCanOnlyInputDNASymbolsIntoTheSequenceTextfield(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		EditText sequenceEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_sequence_editor);
 		solo.typeText(sequenceEditor, "INVALIDSEQUENCE");
 		getInstrumentation().waitForIdleSync();
 		//'A', 'C', 'G', 'T', 'U', 'W', 'S', 'M', 'K', 'R', 'Y', 'B', 'D', 'H', 'V'
 		assertEquals("VADSUC", sequenceEditor.getEditableText().toString());
 	}
 	
 	public void testWeCanEditTheDatabaseOfADraftQuery(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		solo.pressSpinnerItem(1, -2);
 		getInstrumentation().waitForIdleSync();
 		Spinner databaseSpinner = (Spinner)solo.getView(com.bioinformaticsapp.R.id.blastqueryentry_database_spinner);
 		BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		assertEquals(databaseSpinner.getSelectedItem().toString(), q.getSearchParameter("database").getValue());
 		
 	}
 	
 	public void testWeCanEditTheScoreOfADraftQuery(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		solo.pressSpinnerItem(2, -2);
 		getInstrumentation().waitForIdleSync();
 		Spinner databaseSpinner = (Spinner)solo.getView(com.bioinformaticsapp.R.id.blastqueryentry_score_spinner);
 		BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		assertEquals(databaseSpinner.getSelectedItem().toString(), q.getSearchParameter("score").getValue());
 		
 	}
 	
 	public void testWeCanEditMatchMisMatchScore(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		solo.pressSpinnerItem(4, -2);
 		getInstrumentation().waitForIdleSync();
 		Spinner matchMismatchScoreSpinner = (Spinner)solo.getView(com.bioinformaticsapp.R.id.ebi_match_mismatch_score_spinner);
 		BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		assertEquals(matchMismatchScoreSpinner.getSelectedItem().toString(), q.getSearchParameter("match_mismatch_score").getValue());
 		
 	}
 	
 	public void testWeCanEditTheExpThresholdOfADraftQuery(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		solo.pressSpinnerItem(2, 3);
 		getInstrumentation().waitForIdleSync();
 		Spinner expThresholdSpinner = (Spinner)solo.getView(com.bioinformaticsapp.R.id.blastqueryentry_expthreshold_spinner);
 		BLASTQuery q = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		assertEquals(expThresholdSpinner.getSelectedItem().toString(), q.getSearchParameter("exp_threshold").getValue());
 		
 	}
 	
 	public void testWeCanEditTheEmailAddressParameter(){
 		Intent intent = new Intent();
 		intent.putExtra("query", blastQuery);
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupActivity = getActivity();
 		solo = new Solo(getInstrumentation(), setupActivity);
 		EditText emailEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_send_to_email);
 		String validEmailAddress = "h.n.varambhia@gmail.com";
 		solo.typeText(emailEditor, validEmailAddress);
 		getInstrumentation().waitForIdleSync();
 		
 		BLASTQuery query = (BLASTQuery)setupActivity.getIntent().getSerializableExtra("query");
 		
 		assertEquals("Expected e-mail address where results would be sent to to be "+validEmailAddress+" " +
 				"but got "+query.getSearchParameter("email"), validEmailAddress, 
 				query.getSearchParameter("email").getValue());
 		
 	}
 	
 	public void testWeCanSendAValidQuery(){
 
 		Intent intent = new Intent();
 		
 		intent.putExtra("query", blastQuery);
 		
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupQueryActivity = (EMBLEBISetUpQueryActivity)getActivity();
 		
 		solo = new Solo(getInstrumentation(), setupQueryActivity);
 		
 		EditText sequenceEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_sequence_editor);
 		
 		solo.typeText(sequenceEditor, "CCTTTATCTAATCTTTGGAGCATGAGCTGG");
 		
 		EditText emailEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_send_to_email);
 		
 		solo.typeText(emailEditor, "h.n.varambhia@gmail.com");
 		
 		solo.clickOnActionBarItem(com.bioinformaticsapp.R.id.send_query);
 		
 		solo.waitForDialogToClose(SENDING_DIALOG_TIMEOUT);
 		
 		BLASTQuery q = (BLASTQuery)setupQueryActivity.getIntent().getSerializableExtra("query");
 		
 		assertEquals( "Expected query to be ready for sending", Status.PENDING, q.getStatus());
 		        
 	}
 	
 	public void testWeCannotSendAnInvalidQuery(){
 		Intent intent = new Intent();
 		
 		intent.putExtra("query", blastQuery);
 		
 		setActivityIntent(intent);
 		
 		EMBLEBISetUpQueryActivity setupQueryActivity = (EMBLEBISetUpQueryActivity)getActivity();
 		
 		solo = new Solo(getInstrumentation(), setupQueryActivity);
 		
 		EditText sequenceEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_sequence_editor);
 		
 		solo.typeText(sequenceEditor, "INVALIDSEQUENCE");
 		
 		EditText emailEditor = (EditText)solo.getView(com.bioinformaticsapp.R.id.embl_send_to_email);
 		
 		solo.typeText(emailEditor, "h.n.varambhia@gmail.com");
 		
 		solo.clickOnActionBarItem(com.bioinformaticsapp.R.id.send_query);
 		
 		solo.waitForDialogToClose(SENDING_DIALOG_TIMEOUT);
 		
 		BLASTQuery q = (BLASTQuery)setupQueryActivity.getIntent().getSerializableExtra("query");
 		
 		assertEquals( "Expected query to be ready for sending", Status.DRAFT, q.getStatus());
 		        
 	}
 	
 	public void testWeCanGoToTheApplicationPreferencesScreen(){
 
 		Intent intent = new Intent();
 		
 		intent.putExtra("query", blastQuery);
 		
 		setActivityIntent(intent);
 		
 		solo = new Solo(getInstrumentation(), getActivity());
 		
 		solo.clickOnMenuItem("Settings");
 		
 		solo.assertCurrentActivity("Should be able to go to the settings screen", AppPreferences.class);
 	}
 	
 }
