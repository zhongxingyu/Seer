 package com.bioinformaticsapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.widget.Toast;
 
 import com.bioinformaticsapp.data.BLASTQueryController;
 import com.bioinformaticsapp.data.SearchParameterController;
 import com.bioinformaticsapp.models.BLASTQuery;
 import com.bioinformaticsapp.models.SearchParameter;
 
 public abstract class SetUpBLASTQueryActivity extends Activity {
 
 	protected void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		Intent launchingIntent = getIntent();
 		
 		controller = new BLASTQueryController(this);
 		
 		optionalParametersController = new SearchParameterController(this);
 		
 		query = (BLASTQuery)launchingIntent.getSerializableExtra("query");
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		MenuInflater inflater = getMenuInflater();
 		
 		inflater.inflate(R.menu.blastqueryentry_menu, menu);
 		
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		int itemId = item.getItemId();
 		
 		switch(itemId){
 		
 		case R.id.send_query: {
 			
 			BLASTQueryValidator validator = new BLASTQueryValidator();
 			
 			validator.execute(new BLASTQuery[]{query});
 			
 		}
 		//...and exit	
 		break;
 		
 		case R.id.save_query: {
 			//Store into our database or update
 			storeQueryInDatabase();
 			
 			//Create a toast message to tell the user the query was saved
 			Toast querySavedMessage = Toast.makeText(this, R.string.blastquerysaved_text, Toast.LENGTH_LONG);
 			
 			//Now show it
 			querySavedMessage.show();
 		
 		}
 		
 		break;
 		
		case R.id.settings: {
			storeQueryInDatabase();
			Intent settings = new Intent(this, AppPreferences.class);
			startActivity(settings);
		}
		break;
		
 		default:
 			break;
 		
 		}
 		
 		return true;
 	}
 
 	protected void onResume(){
 		
 		super.onResume();
 		
 		setUpScreenWithInitialValues();
 		
 	}
 	
 	protected abstract void setUpScreenWithInitialValues();
 	
 	protected void storeQueryInDatabase(){
 		
 		
 		if(query.getPrimaryKey() == null){
 			//Add the job to our database of BLAST queries...
 			long primaryKey = controller.save(query);
 			
 			query.setPrimaryKeyId(primaryKey);
 			
 			//Save the parameters:
 			List<SearchParameter> parameters = new ArrayList<SearchParameter>();
 			for(SearchParameter parameter: query.getAllParameters()){
 				parameter.setBlastQueryId(query.getPrimaryKey());
 				long parameterPrimaryKey = optionalParametersController.save(parameter);
 				parameter.setPrimaryKey(parameterPrimaryKey);
 				parameters.add(parameter);
 				
 			}
 			
 			query.updateAllParameters(parameters);
 			
 			
 		}else{ 
 			//...or date the columns for the specified row:
 			controller.update(query.getPrimaryKey(), query);
 			
 			for(SearchParameter parameter: query.getAllParameters()){
 				optionalParametersController.update(parameter.getPrimaryKey(), parameter);
 			}
 			
 		}
 	}
 	
 	protected class BLASTQueryValidator extends AsyncTask<BLASTQuery, Void, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(BLASTQuery... query) {
 			
 			boolean isValid = query[0].isValid();
 			return isValid;
 		}
 		
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onPreExecute()
 		 */
 		@Override
 		protected void onPreExecute() {
 			mProgressDialog.setTitle("Validating BLAST query");
 			mProgressDialog.setMessage("Please wait...");
 			mProgressDialog.show();
 			
 		}
 		
 		protected void onPostExecute(Boolean isValid){
 			
 			if(isValid.booleanValue()){
 				
 				//The query is ready to be sent
 				query.setStatus(BLASTQuery.Status.PENDING);
 				storeQueryInDatabase();
 				setResult(DraftBLASTQueriesActivity.READY_TO_SEND);
 				Toast t = Toast.makeText(SetUpBLASTQueryActivity.this, "Sending query", Toast.LENGTH_LONG);
 				t.show();
 				//Start the polling service
 				finish();
 			}else{
 				if(mProgressDialog.isShowing()){
 					mProgressDialog.dismiss();
 				}
 				Toast t = Toast.makeText(SetUpBLASTQueryActivity.this, "Query could not be sent as it is invalid", Toast.LENGTH_LONG);
 				t.show();
 				
 			}
 
 		}
 		
 	}
 	
 	protected ProgressDialog mProgressDialog;
 	
 	protected BLASTQuery query;
 	
 	protected BLASTQueryController controller;
 	
 	protected SearchParameterController optionalParametersController;
 	
 }
