 package com.ticot.simuimmo;
 
 import java.text.DecimalFormat;
 import java.text.DecimalFormatSymbols;
 import java.util.Locale;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import com.ticot.simuimmo.calculs.Temp;
 import com.ticot.simuimmo.model.Inputs;
 import com.ticot.simuimmo.model.Settings;
 import com.ticot.simuimmo.model.acquisition.Acquisition;
 import com.ticot.simuimmo.model.bien.Bien;
 import com.ticot.simuimmo.model.gestion.Gestion;
 
 public class MainActivity extends Activity {
 
 	//Declaration of variables
 	public boolean AcquisitionCollpased = true;			//Global variable to know the state of the UI, collapsed or expanded
 	public boolean emptyMandatoryField = false;			//Global variable to know if mandatory field have been found empty or not
 	public Bien bien = Temp.test();						//Creation of the class Bien through the temporary class
 	
 	/**Called when the activity is first created*/
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 	}
 
 	//TODO onCreateOptionsMenu to do
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 		return true;
 	}
 	
 	//Calculation button
 	//==============================================================================
 	public void onClick (View v){
 		//Methods to get user's inputs, launch calculation and fill back the result in the UI 
 		
 		//Initialize the Mandatory variable
 		emptyMandatoryField = false;
 		
 		//Get and check the user's input values
 		getFormInput();
 		
 		//If the emptyMandatoryField has been switched to "True" during the check
 		//So display a message saying some fields require correct value
 		//And quit the method, so no calculation performed
 		if (emptyMandatoryField)
 		{
 			Toast.makeText(getBaseContext(), R.string.message_champs_obligatoires, Toast.LENGTH_SHORT).show();
 			return;
 		}
 		
 		//If mandatory fields have appropriate value
 		//Popultate the created instance of Bien (and run the different calculs) temporarily through the Temp class
 		bien.setAcquisition(Temp.TestAcquisition());
 	//	bien.setGestion(Temp.TestGestion());
 		
 		//Launch the update of the UI to display the computed values
 		fillComputedValues(bien.getAcquisition(), bien.getGestion());
 		
 	}
 	
 	private void getFormInput(){
 		//Get the user's input values from the EditText and CheckBox
 		//Values are set in an Input class, used afterward
 
 		//Boolean to know if computed are user input
 		Inputs.reelNetvendeur = ((CheckBox)findViewById(R.id.ReelNetVendeur)).isChecked();
 		Inputs.reelFraisAgence = ((CheckBox)findViewById(R.id.ReelFraisAgence)).isChecked();
 		Inputs.reelFraisNotaire = ((CheckBox)findViewById(R.id.ReelFraisNotaire)).isChecked();
 		Inputs.reelHonoraireConseil = ((CheckBox)findViewById(R.id.ReelHonoraireConseil)).isChecked();
 		Inputs.reelCapitalEmrpunte = ((CheckBox)findViewById(R.id.ReelCapitalEmprunte)).isChecked();
 		Inputs.reelTauxCredit = ((CheckBox)findViewById(R.id.ReelTauxCredit)).isChecked();
 		Inputs.reelTauxAssurance = ((CheckBox)findViewById(R.id.ReelTauxAssurance)).isChecked();
 		
 		//Get and check input filled by user
 		Inputs.prixFAI = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelPrixFAI),"0"));
 		Inputs.netVendeur = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelNetVendeur),"0"));
 		Inputs.agence = ((CheckBox)findViewById(R.id.valueAgence)).isChecked();
 		Inputs.fraisAgence = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelFraisAgence),"0"));
 		Inputs.fraisNotaire = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelFraisNotaire),"0"));
 		Inputs.travaux = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueTravaux),"0"));
 		Inputs.amenagement = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueAmenagement),"0"));
 		Inputs.conseil = ((CheckBox)findViewById(R.id.valueConseil)).isChecked();
 		Inputs.honoraireConseil = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelHonoraireConseil),"0"));
 		Inputs.autresFrais = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueAutresFrais),"0"));
 		Inputs.apport = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueApport),"0"));
 		Inputs.capitalEmprunte = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelCapitalEmprunte),"0"));
 		Inputs.dureeCredit = Integer.valueOf(checkFieldValue((EditText)findViewById(R.id.valueDureeCredit),"25"));
 		Inputs.tauxCredit = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelTauxCredit), String.valueOf(Settings.taux25ans)));
 		Inputs.tauxAssuranceCredit = Double.valueOf(checkFieldValue((EditText)findViewById(R.id.valueReelTauxAssurance),String.valueOf(Settings.tauxAssuranceCredit)));
 		//TODO Directly instanciate the fields' values in the object Bien instead of the Inputs intermediate classe
 	}
 	
 	private String checkFieldValue(EditText view, String defaultValue){
 		//Method to check the value of a field, if empty, if mandatory
 		
 		//Initialize the returned variable
		String value = "";
 		
 		//Initialize the background of the parent of the view, in case it have already been highlighted
 		((View) view.getParent()).setBackgroundResource(0);
 		
 		//If the user field is not empty, so check if it is formated
 		//Then replace characters to get the appropriate numbers (Double format => 130000.00) 
 		if (!view.getText().toString().isEmpty())
 		{ 
 			value = view.getText().toString();
 			value = value.replaceAll("[^0-9,.]", "");
 			value = value.replace(',', '.');		//TODO Take care of the localization with a different separator
 		}
 		
 		//If the user field is empty or the result of the previous replacement is empty
 		//Check if the field is mandatory (tagged as "Mandatory") => if yes, change the gloabl variable and highlight the parents background
		if (view.getText().toString().isEmpty() || value == "" || value.replaceAll("[^0-9]", "").isEmpty())
 		{
 			if (view.getTag().toString().contains("Mandatory"))
 			{
 				emptyMandatoryField = true;
 				((View) view.getParent()).setBackgroundResource(R.color.red_light);
 			}
 			value = defaultValue;	//Whatever the mandatory value, return the default value to avoid error
 		}
 		
 		//Finally return the value
 		return value;
 	}
 	
 	private void fillComputedValues(Acquisition a, Gestion g){
 		//Methods to update the UI fields with all the computed values;
 		
 		//Define the format for the values
 		DecimalFormat formatEur = new DecimalFormat("###,##0.00 €");
 		formatEur.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.FRANCE));
 		DecimalFormat formatPer = new DecimalFormat("#0.00 %");
 		formatPer.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.FRANCE));
 		
 		//Format user's inputs for FraisAcquisition
 		((EditText) findViewById(R.id.valueReelPrixFAI)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getPrixFAI())));
 		((EditText) findViewById(R.id.valueReelNetVendeur)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getNetVendeur())));
 		((EditText) findViewById(R.id.valueReelFraisAgence)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getFraisAgence())));
 		((EditText) findViewById(R.id.valueReelFraisNotaire)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getFraisNotaire())));
 		((EditText) findViewById(R.id.valueTravaux)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getTravaux())));
 		((EditText) findViewById(R.id.valueAmenagement)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getAmenagement())));
 		((EditText) findViewById(R.id.valueReelHonoraireConseil)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getHonoraireConseil())));
 		((EditText) findViewById(R.id.valueAutresFrais)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getAutresFrais())));
 		((EditText) findViewById(R.id.valueApport)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getApport())));
 
 		//Format user's inputs for Emprunt
 		((EditText) findViewById(R.id.valueReelCapitalEmprunte)).setText(
 				String.valueOf(formatEur.format((a.getEmprunt()).getCapitalEmprunte())));
 		((EditText) findViewById(R.id.valueReelTauxCredit)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxCredit())));
 		((EditText) findViewById(R.id.valueReelTauxAssurance)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxAssuranceCredit())));
 		
 		//Fill computed values for FraisAcquisition
 		((TextView) findViewById(R.id.valuePrixFAI)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getPrixFAI())));
 		((TextView) findViewById(R.id.valueNetVendeur)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getNetVendeur())));
 		((TextView) findViewById(R.id.valueFraisAgence)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getFraisAgence())));
 		((TextView) findViewById(R.id.valueFraisNotaire)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getFraisNotaire())));
 		((TextView) findViewById(R.id.valueHonoraireConseil)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getHonoraireConseil())));
 		((TextView) findViewById(R.id.valueCoutTotal)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getCoutTotal())));
 		((TextView) findViewById(R.id.valueSequestre)).setText(
 				String.valueOf(formatEur.format((a.getFraisAcquisition()).getSequestre())));
 		
 		//Fill computed values for Emprunt
 		((TextView) findViewById(R.id.valueCapitalEmprunte)).setText(
 				String.valueOf(formatEur.format((a.getEmprunt()).getCapitalEmprunte())));
 		((TextView) findViewById(R.id.valueNbMensualite)).setText(
 				String.valueOf((a.getEmprunt()).getNbMensualiteCredit()));
 		((TextView) findViewById(R.id.valueTauxCredit)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxCredit())));
 		((TextView) findViewById(R.id.valueTauxAssurance)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxAssuranceCredit())));
 		((TextView) findViewById(R.id.valueMensualite)).setText(
 				String.valueOf(formatEur.format((a.getEmprunt()).getMensualiteCredit())));
 		((TextView) findViewById(R.id.valueTauxEndettement)).setText(
 				String.valueOf(formatPer.format((a.getEmprunt()).getTauxEndettement())));
 	}
 	
 	public void collapseUI (View view){
 		//Methods to collapse or expand the UI to focus on most important fields or display all fields
 		
 		//Get the Form layout aggregating all fields
 		LinearLayout ll = (LinearLayout)findViewById(R.id.layoutForm);
 
 		//Get all childs of the Form layout and check for each one if their tag is "Collapsable"
 		for (int i=0; i < ll.getChildCount(); i++){
 			View v = ll.getChildAt(i);
 			if (v.getTag().toString().contains("Collapsable")){			//Condition doesn't work for view without tag, need to check if tag is empty
 				//If the item is "Collapsable"
 				//Checking if the UI is already collapsed or not
 				if (AcquisitionCollpased)
 					//If already collapsed, so set the visibility to visible (0) 
 					v.setVisibility(0);
 				else
 					//If not already collapsed, so set the visibility to gone (8)
 					v.setVisibility(8);
 			}
 		}
 		
 		//Finally update the global variable 
 		if (AcquisitionCollpased)
 		{
 			AcquisitionCollpased = false;
 			((Button)view).setText(R.string.afficher_moins);
 		}		
 		else
 		{
 			AcquisitionCollpased = true;
 			((Button)view).setText(R.string.afficher_plus);
 		}
 	}
 	
 	public void switchRealField(View view){
 		//Methods to switch between the TextView (for the computed values) to the EditText (allowing user to fill its own real value)
 		
 		switch (view.getId()){		//Get the ID of the item requesting to switch TextView/EditText
 		//Several fields are able to switch between TextView and EditText
 		
 		case R.id.ReelNetVendeur:
 			if (((CheckBox)view).isChecked()){										//If it has been checked
 				findViewById(R.id.valueNetVendeur).setVisibility(8);				//Turn TextView visibility to GONE
 				findViewById(R.id.valueReelNetVendeur).setVisibility(0);			//Turn EditText visibility to VISIBLE
 				findViewById(R.id.valueReelNetVendeur).requestFocus();				//Set focus to the EditText
 				findViewById(R.id.valueReelNetVendeur).setTag("Mandatory");			//Set the tag of NetVendeur as mandatory
 				if (((CheckBox)findViewById(R.id.ReelFraisAgence)).isChecked()){	//If ReelFraisAgence is checked, so PrixFAI will be computed, so switch the EditText to a TextView 
 					findViewById(R.id.valueReelPrixFAI).setVisibility(8);			//Turn PrixFAI EditText visibility to GONE
 					findViewById(R.id.valueReelPrixFAI).setTag("Optional");			//Set the tag of valueReelPrixFAI as optional
 					findViewById(R.id.valuePrixFAI).setVisibility(0);				//Turn PrixFAI TextView visibility to VISIBLE
 				}else{
 					findViewById(R.id.valueReelPrixFAI).setTag("Mandatory");		//
 				}
 			} else{																	//If it has been unchecked
 				findViewById(R.id.valueNetVendeur).setVisibility(0);				//Turn TextView visibility to VISIBLE
 				findViewById(R.id.valueReelNetVendeur).setVisibility(8);			//Turn EditText visibility to GONE
 				findViewById(R.id.valueReelNetVendeur).setTag("Optional");			//Set the tag of NetVendeur as optional
 				findViewById(R.id.valueReelPrixFAI).setVisibility(0);				//Turn PrixFAI EditText visibility to VISIBLE
 				findViewById(R.id.valueReelPrixFAI).setTag("Mandatory");			//Set the tag of valueReelPrixFAI as mandatory
 				findViewById(R.id.valuePrixFAI).setVisibility(8);					//Turn PrixFAI TextView visibility to GONE
 			}
 			break;
 		case R.id.ReelFraisAgence:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueFraisAgence).setVisibility(8);
 				findViewById(R.id.valueReelFraisAgence).setVisibility(0);
 				findViewById(R.id.valueReelFraisAgence).requestFocus();
 				findViewById(R.id.valueReelFraisAgence).setTag("Mandatory");
 				if (((CheckBox)findViewById(R.id.ReelNetVendeur)).isChecked()){
 					findViewById(R.id.valueReelPrixFAI).setVisibility(8);
 					findViewById(R.id.valueReelPrixFAI).setTag("Optional");
 					findViewById(R.id.valuePrixFAI).setVisibility(0);
 				}else{
 					findViewById(R.id.valueReelPrixFAI).setTag("Mandatory");
 				}
 			} else{
 				findViewById(R.id.valueFraisAgence).setVisibility(0);
 				findViewById(R.id.valueReelFraisAgence).setVisibility(8);
 				findViewById(R.id.valueReelFraisAgence).setTag("Optional");
 				findViewById(R.id.valueReelPrixFAI).setVisibility(0);
 				findViewById(R.id.valueReelPrixFAI).setTag("Mandatory");
 				findViewById(R.id.valuePrixFAI).setVisibility(8);
 			}
 			break;
 		case R.id.ReelFraisNotaire:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueFraisNotaire).setVisibility(8);
 				findViewById(R.id.valueReelFraisNotaire).setVisibility(0);
 				findViewById(R.id.valueReelFraisNotaire).requestFocus();
 			} else{
 				findViewById(R.id.valueFraisNotaire).setVisibility(0);
 				findViewById(R.id.valueReelFraisNotaire).setVisibility(8);
 			}
 			break;
 		case R.id.ReelHonoraireConseil:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueHonoraireConseil).setVisibility(8);
 				findViewById(R.id.valueReelHonoraireConseil).setVisibility(0);
 				findViewById(R.id.valueReelHonoraireConseil).requestFocus();
 			} else{
 				findViewById(R.id.valueHonoraireConseil).setVisibility(0);
 				findViewById(R.id.valueReelHonoraireConseil).setVisibility(8);
 			}
 			break;
 		case R.id.ReelCapitalEmprunte:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueCapitalEmprunte).setVisibility(8);
 				findViewById(R.id.valueReelCapitalEmprunte).setVisibility(0);
 				findViewById(R.id.valueReelCapitalEmprunte).requestFocus();
 				findViewById(R.id.valueReelCapitalEmprunte).setTag("Mandatory");
 			} else{
 				findViewById(R.id.valueCapitalEmprunte).setVisibility(0);
 				findViewById(R.id.valueReelCapitalEmprunte).setVisibility(8);
 				findViewById(R.id.valueReelCapitalEmprunte).setTag("Optional");
 			}
 			break;
 		case R.id.ReelTauxCredit:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueTauxCredit).setVisibility(8);
 				findViewById(R.id.valueReelTauxCredit).setVisibility(0);
 				findViewById(R.id.valueReelTauxCredit).requestFocus();
 			} else{
 				findViewById(R.id.valueTauxCredit).setVisibility(0);
 				findViewById(R.id.valueReelTauxCredit).setVisibility(8);
 			}
 			break;
 		case R.id.ReelTauxAssurance:
 			if (((CheckBox)view).isChecked()){
 				findViewById(R.id.valueTauxAssurance).setVisibility(8);
 				findViewById(R.id.valueReelTauxAssurance).setVisibility(0);
 				findViewById(R.id.valueReelTauxAssurance).requestFocus();
 			} else{
 				findViewById(R.id.valueTauxAssurance).setVisibility(0);
 				findViewById(R.id.valueReelTauxAssurance).setVisibility(8);
 			}
 			break;
 		}
 	}
 }
