 package au.edu.uwa.csp.respreport;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.TimeUnit;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 import au.edu.uwa.csp.respreport.R;
 
 public class Register extends Activity {
 
 	public final static String USER_NAME = "com.edu.uwa.csp.respreport.RUSERNAME";
 	public final static String PASSWORD = "com.edu.uwa.csp.respreport.RPASSWORD";
 	private final String ERROR = "Error Registration failed ";
 
 	private String title;
 	private Spinner titleSp;
 	private String userName;
 	private long patientId;
 	private String password;
 	private String firstName;
 	private String lastName;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_register);
 
 		addItemsOnSpinner();
 	}
 
 	// the dropdown list for title.
 	public void addItemsOnSpinner() {
 		titleSp = (Spinner) findViewById(R.id.title);
 		List<String> list = new ArrayList<String>();
 
 		list.add("Dr.");
 		list.add("Mr.");
 		list.add("Miss.");
 		list.add("Mrs.");
 
 		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, list);
 		dataAdapter
 				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		titleSp.setAdapter(dataAdapter);
 	}
 
 	public void doRegister(View view) {
 		// setContentView(textView);
 		// get user name from input
 		EditText edt_userName = (EditText) findViewById(R.id.reg_user_name);
 		userName = edt_userName.getText().toString();
 
 		// get password from input
 		EditText edt_password = (EditText) findViewById(R.id.reg_password);
 		password = edt_password.getText().toString();
 
 		// get the title of the user
 		title = String.valueOf(titleSp.getSelectedItem());
 
 		// get user name from input
 		EditText edt_firstName = (EditText) findViewById(R.id.firstName);
 		firstName = edt_firstName.getText().toString();
 
 		// get password from input
 		EditText edt_lastName = (EditText) findViewById(R.id.lastName);
 		lastName = edt_lastName.getText().toString();
 
 		// Register a new user by starting a SOAP task to call the RegisterUser
 		// web service
 		SOAPTask task = new SOAPTask(Register.this, "RegisterUser");
 		// add the required parameters for the RegisterUser web service
 		task.addParam("userName", userName);
 		task.addParam("password", password);
 		task.parentActivity = Register.this;
 		task.execute();
 
 		// wait for 5 sec and get the response of the web service
 		String result = "";
 		try {
 			result = task.get(5, TimeUnit.SECONDS);
 		} catch (Exception e) {
 			result = ERROR;
 			e.printStackTrace();
 		}
		result+="";
		
 		// if registration is successful
 		if (result.equalsIgnoreCase("ok")) {
 			// Add a new patient by creating a SOAP task to call the AddPatient
 			// web service
 			SOAPTask newTask = new SOAPTask(Register.this, "AddPatient");
 
 			// add the required parameters for the AddPatient web service
 			newTask.addParam("userName", userName);
 			newTask.addParam("password", password);
 			newTask.addParam("title", title);
 			newTask.addParam("firstName", firstName);
 			newTask.addParam("lastName", lastName);
 			newTask.parentActivity = Register.this;
 			newTask.execute();
 
 			// wait for 5 sec and get the response from the web service
 			try {
 				result = newTask.get(5, TimeUnit.SECONDS);
 			} catch (Exception e) {
 				result = ERROR;
 				e.printStackTrace();
 			}
 
 			result += "";
 
 			// if add new patient successfully
 			if (!result.equalsIgnoreCase("Error")) {
 
 				patientId = Long.valueOf(result);
 				// store the patient info in sqlite database
 				PatientDataSource rds = new PatientDataSource(
 						getApplicationContext());
 
 				rds.open();
 				// create a new patient in the database
 				rds.createPatient(userName, patientId, title, firstName,
 						lastName);
 				rds.close();
 
 				// redirect to the login page
 				Intent intent = new Intent(Register.this,
 						AuthUserActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				startActivity(intent);
 			}
 		} else
			AppFunctions.alertDialog(ERROR+result, Register.this);
		
 
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_register, menu);
 		return true;
 	}
 
 }
