 package com.github.cmput301w13t04.food.view;
 
 import com.github.cmput301w13t04.food.R;
 import com.github.cmput301w13t04.food.model.Step;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.support.v4.app.NavUtils;
 
 public class ActivityManageStep extends Activity {
 
 	public final static int RESULT_DELETE = 10;
 
 	private int position;
 	private Step step;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_manage_step);
 		// Show the Up button in the action bar.
 		setupActionBar();
 
 		// Get Step Position
 		position = getIntent().getIntExtra("POSITION", -1);
 
 		if (position != -1) {
 			// Modify Existing Step
 			step = getIntent().getParcelableExtra("STEP");
 
 			TextView number = (TextView) findViewById(R.id.step_number);
 			number.setText(String.valueOf(position + 1) + ".");
 
 			EditText name = (EditText) findViewById(R.id.step_name);
 			name.setText(step.getName());
 
 			EditText description = (EditText) findViewById(R.id.step_description);
 			description.setText(step.getDescription());
 
 		} else {
 			position = getIntent().getIntExtra("NEW_POSITION", -1);
 			if (position != -1) {
 				TextView number = (TextView) findViewById(R.id.step_number);
 				number.setText(String.valueOf(position + 1) + ".");
 			}
 			// New Ingredient
 			step = new Step();
 		}
 
 	}
 
 	/**
 	 * Set up the {@link android.app.ActionBar}.
 	 */
 	private void setupActionBar() {
 
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_manage_step, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case android.R.id.home:
 			// This ID represents the Home or Up button. In the case of this
 			// activity, the Up button is shown. Use NavUtils to allow users
 			// to navigate up one level in the application structure. For
 			// more details, see the Navigation pattern on Android Design:
 			//
 			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
 			//
 			NavUtils.navigateUpFromSameTask(this);
 			return true;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 
 	public void removeStep(View view) {
 		// Remove Step
 		Intent result = new Intent();
 		result.putExtra("POSITION", position);
 		setResult(RESULT_DELETE, result);
 		finish();
 	}
 
 	public void updateStep(View view) {
 		// Get Name
 		EditText name = (EditText) findViewById(R.id.step_name);
 		String stepName = name.getText().toString();
 		if (stepName.isEmpty()) {
			Toast.makeText(view.getContext(), "Missing Ingredient Name!",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		step.setName(stepName);
 
 		// Get Description
 		EditText description = (EditText) findViewById(R.id.step_description);
 		String stepDescription = description.getText().toString();
 		if (stepDescription.isEmpty()) {
			Toast.makeText(view.getContext(), "Missing Ingredient Name!",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		step.setDescription(stepDescription);
 
 		// Send step back
 		Intent result = new Intent();
 		result.putExtra("STEP", step);
 		result.putExtra("POSITION", position);
 		setResult(Activity.RESULT_OK, result);
 		finish();
 	}
 
 }
