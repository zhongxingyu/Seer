 /*******************************************************************************
  * Copyright (C) 2012-2013 CNRS and University of Strasbourg
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package proteomics.cetoolbox.fragments.tabs;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 import android.widget.TextView;
 import java.text.DecimalFormat;
 import java.util.Locale;
 import proteomics.cetoolbox.CapillaryElectrophoresis;
 import proteomics.cetoolbox.R;
 
 public class SimpleActivity extends Activity implements
 		AdapterView.OnItemSelectedListener, View.OnClickListener {
 
 	Button calculate;
 	Button reset;
 	EditText capillaryLengthValue;
 	EditText diameterValue;
 	EditText pressureValue;
 	EditText durationValue;
 	EditText viscosityValue;
 	EditText toWindowLengthValue;
 	EditText concentrationValue;
 	EditText molecularWeightValue;
 	TextView tvConcentrationUnits;
 	Spinner concentrationSpin;
 	Spinner pressureSpin;
 	String concentrationUnit;
 	String pressureUnit;
 	CapillaryElectrophoresis capillary;
 
 	Double diameter;
 	Double duration;
 	Double viscosity;
 	Double capillaryLength;
 	Double pressure;
 	Double toWindowLength;
 	Double concentration;
 	Double molecularWeight;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (savedInstanceState == null) {
 			diameter = 30.0;
 			duration = 21.0;
 			viscosity = 1.0;
 			capillaryLength = 100.0;
 			pressure = 27.5792;
 			toWindowLength = 90.0;
 			concentration = 21.0;
 			molecularWeight = 150000.0;
 		} else {
 			diameter = savedInstanceState.getDouble("diameter");
 			duration = savedInstanceState.getDouble("duration");
 			viscosity = savedInstanceState.getDouble("viscosity");
 			capillaryLength = savedInstanceState.getDouble("capillaryLength");
 			pressure = savedInstanceState.getDouble("pressure");
 			toWindowLength = savedInstanceState.getDouble("toWindowLength");
 			concentration = savedInstanceState.getDouble("concentration");
 			molecularWeight = savedInstanceState.getDouble("molecularWeight");
 
 		}
 
 		String languageToLoad = "en";
 		Locale locale = new Locale(languageToLoad);
 		Locale.setDefault(locale);
 		Configuration config = new Configuration();
 		config.locale = locale;
 		getBaseContext().getResources().updateConfiguration(config,
 				getBaseContext().getResources().getDisplayMetrics());
 		this.setContentView(R.layout.simple);
 
 		capillaryLengthValue = (EditText) findViewById(R.id.capillaryLengthValue);
 		diameterValue = (EditText) findViewById(R.id.diameterValue);
 		toWindowLengthValue = (EditText) findViewById(R.id.toWindowLengthValue);
 		pressureValue = (EditText) findViewById(R.id.pressureValue);
 		durationValue = (EditText) findViewById(R.id.durationValue);
 		viscosityValue = (EditText) findViewById(R.id.viscosityValue);
 		concentrationValue = (EditText) findViewById(R.id.concentrationValue);
 		molecularWeightValue = (EditText) findViewById(R.id.molecularWeightValue);
 		concentrationSpin = (Spinner) findViewById(R.id.concentrationSpin);
 		concentrationSpin.setOnItemSelectedListener(this);
 		ArrayAdapter<CharSequence> concentrationUnitsAdapter = ArrayAdapter
 				.createFromResource(this, R.array.concentrationUnitArray,
 						android.R.layout.simple_spinner_item);
 		concentrationUnitsAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		concentrationSpin.setAdapter(concentrationUnitsAdapter);
 
 		pressureSpin = (Spinner) findViewById(R.id.pressureSpin);
 		pressureSpin.setOnItemSelectedListener(this);
 		ArrayAdapter<CharSequence> pressureUnitsAdapter = ArrayAdapter
 				.createFromResource(this, R.array.pressureUnitArray,
 						android.R.layout.simple_spinner_item);
 		pressureUnitsAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		pressureSpin.setAdapter(pressureUnitsAdapter);
 
 		calculate = (Button) findViewById(R.id.button1);
 		calculate.setOnClickListener(this);
 		reset = (Button) findViewById(R.id.button2);
 		reset.setOnClickListener(this);
 
 		/* Initialize content */
 		editTextInitialize();
 	}
 
 	private void editTextInitialize() {
 		capillaryLengthValue.setText(capillaryLength.toString());
 		diameterValue.setText(diameter.toString());
 		toWindowLengthValue.setText(toWindowLength.toString());
 		pressureValue.setText(pressure.toString());
 		durationValue.setText(duration.toString());
 		viscosityValue.setText(viscosity.toString());
 		concentrationValue.setText(concentration.toString());
 		molecularWeightValue.setText(molecularWeight.toString());
 	}
 
 	@Override
 	public void onClick(View view) {
 		if (view == calculate) {
 			/* Parameter validation */
 			diameter = Double.valueOf(diameterValue.getText().toString());
 			duration = Double.valueOf(durationValue.getText().toString());
 			viscosity = Double.valueOf(viscosityValue.getText().toString());
 			capillaryLength = Double.valueOf(capillaryLengthValue.getText()
 					.toString());
 			if (pressureUnit.compareTo("psi") == 0) {
 				pressure = Double.valueOf(pressureValue.getText().toString()) * 6894.8 / 100;
 				;
 			} else {
 				pressure = Double.valueOf(pressureValue.getText().toString());
 			}
 			toWindowLength = Double.valueOf(toWindowLengthValue.getText()
 					.toString());
 			concentration = Double.valueOf(concentrationValue.getText()
 					.toString());
 			molecularWeight = Double.valueOf(molecularWeightValue.getText()
 					.toString());
 
 			/* If all is fine, we create the capillary and compute */
 			capillary = new CapillaryElectrophoresis(pressure, diameter,
 					duration, viscosity, capillaryLength, toWindowLength,
 					concentration, molecularWeight);
 			DecimalFormat myFormatter = new DecimalFormat("#.##");
			Double deliveredVolume = capillary.getDeliveredVolume(); /* nl */
			Double capillaryVolume = capillary.getCapillaryVolume(); /* nl */
 			/* Compute injected quantity of analyte */
 			Double analyteMass; /* ng */
 			Double analyteMol; /* mmol */
 			if (concentrationUnit.compareTo("g/L") == 0) {
 				analyteMass = deliveredVolume * concentration;
 				analyteMol = analyteMass / molecularWeight * 1000;
 			} else {
 				analyteMol = deliveredVolume * concentration;
				analyteMass = analyteMol * molecularWeight / 1000;
 			}
 
 			Double plugLength = deliveredVolume / capillaryVolume * 100;
 
 			LayoutInflater li = LayoutInflater.from(this);
 			View simpleDetailsView = li.inflate(R.layout.simpleresults, null);
 
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 
 			builder.setView(simpleDetailsView);
 			
 			TextView title = new TextView(this);
             title.setText("Injection Details");
             title.setTextSize(20);
             title.setBackgroundColor(Color.DKGRAY);
             title.setTextColor(Color.WHITE);
             title.setPadding(10,10,10,10);
             title.setGravity(Gravity.CENTER);
             builder.setCustomTitle(title);
             
 			TextView tvHydrodynamicInjection = (TextView) simpleDetailsView.findViewById(R.id.hydrodynamicInjectionValue);
 			tvHydrodynamicInjection.setText(myFormatter.format(deliveredVolume)
 					+ " nl");
 			TextView tvCapillaryVolume = (TextView) simpleDetailsView.findViewById(R.id.capillaryVolumeValue);
 			tvCapillaryVolume.setText(myFormatter.format(capillaryVolume)
 					+ " nl");
 			TextView tvPlugLength = (TextView) simpleDetailsView.findViewById(R.id.plugLengthValue);
 			tvPlugLength.setText(myFormatter.format(plugLength));
 			TextView tvInjectedAnalyte = (TextView) simpleDetailsView.findViewById(R.id.injectedAnalyteValue);
 			tvInjectedAnalyte.setText(myFormatter.format(analyteMass) + " ng\n"
 					+ myFormatter.format(analyteMol) + " pmol");
 
 			builder.setNeutralButton("Close",
 					new DialogInterface.OnClickListener() {
 						@Override
 						public void onClick(DialogInterface dlg, int sumthin) {
 							// do nothing – it will close on its own
 						}
 					});
 
 			builder.show();
 
 		} else if (view == reset) {
 			editTextInitialize();
 		}
 	}
 
 	@Override
 	public void onItemSelected(AdapterView<?> parent, View view, int position,
 			long id) {
 
 		if (parent == concentrationSpin) {
 			concentrationUnit = (String) concentrationSpin
 					.getItemAtPosition(position);
 		} else if (parent == pressureSpin) {
 			pressureUnit = (String) pressureSpin.getItemAtPosition(position);
 		}
 	}
 
 	@Override
 	public void onNothingSelected(AdapterView<?> parent) {
 		/* concentrationSpin.setText(""); */
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle state) {
 		super.onSaveInstanceState(state);
 
 		state.putDouble("diameter",
 				Double.valueOf(diameterValue.getText().toString()));
 		state.putDouble("duration",
 				Double.valueOf(durationValue.getText().toString()));
 		state.putDouble("viscosity",
 				Double.valueOf(viscosityValue.getText().toString()));
 		state.putDouble("capillaryLength",
 				Double.valueOf(capillaryLengthValue.getText().toString()));
 		state.putDouble("pressure",
 				Double.valueOf(pressureValue.getText().toString()));
 		state.putDouble("toWindowLength",
 				Double.valueOf(toWindowLengthValue.getText().toString()));
 		state.putDouble("concentration",
 				Double.valueOf(concentrationValue.getText().toString()));
 		state.putDouble("molecularWeight",
 				Double.valueOf(molecularWeightValue.getText().toString()));
 	}
 
 }
