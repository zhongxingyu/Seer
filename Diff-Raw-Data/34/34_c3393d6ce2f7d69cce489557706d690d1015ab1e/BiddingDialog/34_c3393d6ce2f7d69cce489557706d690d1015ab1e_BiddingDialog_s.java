 package com.androidtitlan.endeavorsubasta;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.androidtitlan.endeavorsubasta.io.DoBiddingTask;
 import com.androidtitlan.endeavorsubasta.io.DoHttpPostTask;
 import com.androidtitlan.endeavorsubasta.util.Resources;
 
 public class BiddingDialog extends Activity {
 	public static final String USERNAME_RESULT_FROM_DIALOG = "USERNAME";
 	public static final String NEW_USERNAME_RESULT_FROM_DIALOG = "NEW_USERNAME";
 	public static final String NEW_FULLNAME_RESULT_FROM_DIALOG = "NEW_FULLNAME";
 	public static final String NEW_TABLE_RESULT_FROM_DIALOG = "NEW_TABLE";
 
 	public static final int VISIBLE = 0;
 	public static final int GONE = 8;
 
 	private int userBid;
 	private int product;
 
 	private EditText nameEdit, nameRegistroEdit, userNameEdit, tableNumberEdit;
 	private CheckBox acceptedTermsAndConditions;
 	private boolean layoutIsLogin = true;
 	private String fullName;
 	private String tableNumber;
 	private String userName;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.custom_dialog);
 		nameEdit = (EditText) findViewById(R.id.biddername);
 		Bundle extras = getIntent().getExtras();
 		userBid = extras.getInt("Bid");
 		product = extras.getInt("Product");
 	}
 
 	/**
 	 * This is a method binded with the Button Onclick property
 	 */
 	public void returnNamefromBiddingDialog(View v) {
 		if (nameEdit.getText().toString().equals("")) {
 			Toast.makeText(this, "Por favor, escriba su nombre de usuario",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		userName = nameEdit.getText().toString();
 		new DoBiddingTask(userName, Integer.toString(product),
 				Integer.toString(userBid)).execute(Resources.URL_SUBASTA
 				+ "pujaProducto/");
 		Log.e(Resources.APP_NAME, "Puja enviada");
 
 		finish();
 	}
 
 	public void registerNewUser(View v) {
 		if ((!acceptedTermsAndConditions.isChecked())) {
 			Toast.makeText(this,
 					"Por favor, acepte los trminos y condiciones",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		if (nameRegistroEdit.getText().toString().equals("")) {
 			Toast.makeText(this, "Por favor, escriba su nombre",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		if (userNameEdit.getText().toString().equals("")) {
 			Toast.makeText(this, "Por favor, escriba su nombre de usuario",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 		if (tableNumberEdit.getText().toString().equals("")) {
 			Toast.makeText(this, "Por favor, escriba el nmero de su mesa",
 					Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		// Assigning to variables
 		userName = userNameEdit.getText().toString();
 		fullName = nameRegistroEdit.getText().toString();
 		tableNumber = tableNumberEdit.getText().toString();
 		String strCreaUsuario = Resources.URL_SUBASTA + "creaUsuario/";
 		Log.e(strCreaUsuario, "--------");
 
 		new DoHttpPostTask(userName, fullName, tableNumber, this)
 				.execute(Resources.URL_SUBASTA + "/creaUsuario/");
 		finish();
 	}
 
 	public void cambioEntreRegistroLogin(View v) {
 		if (layoutIsLogin) {
 			setContentView(R.layout.custom_dialog_register);
 			nameRegistroEdit = (EditText) findViewById(R.id.registro_biddername);
 			userNameEdit = (EditText) findViewById(R.id.registro_bidderusername);
 			tableNumberEdit = (EditText) findViewById(R.id.registro_biddertable);
 			acceptedTermsAndConditions = (CheckBox) findViewById(R.id.termsandconditions);
 			layoutIsLogin = false;
 		} else {
 			setContentView(R.layout.custom_dialog);
 			layoutIsLogin = true;
 		}
 	}
 
 	/**
 	 * This is a method binded with the Button Onclick property
 	 */
 	public void cancelBiddingDialog(View v) {
 		finish();
 	}
 
 }
