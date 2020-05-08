 package fh.itb.avalanchewarning;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import android.os.Bundle;
 import android.os.StrictMode;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 
 /**
  * Dise Aktivity bildet den Loginscreen.
  * @author Kno
  *
  */
 public class LoginActivity extends Activity {
 
 	Button _btnLogin;
 	Button _btnAbort;
 
 	EditText _tfUser;
 	EditText _tfPw;
 
 	RadioGroup _rgLanguage;
 	RadioButton _rbEnglish;
 	RadioButton _rbGerman;
 	
 	Socket socket;
 
 	//String[] _languageGerman;
 	//String[] _languageEnglish;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_login);
 
 		// Init
 		_btnLogin = (Button) findViewById(R.id.btnLogin);
 		_btnAbort = (Button) findViewById(R.id.btnAbort);
 
 		_tfUser = (EditText) findViewById(R.id.txtUsername);
 		_tfPw = (EditText) findViewById(R.id.pwdUserpassword);
 
 		_rgLanguage = (RadioGroup) findViewById(R.id.radioLanguage);
 		_rbEnglish = (RadioButton) findViewById(R.id.rdoLanguageEnglish);
 		_rbGerman = (RadioButton) findViewById(R.id.rdoLanguageGerman);
 
 		// Listener
 		_btnLogin.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				// LOGIN Button clicked!
 
 				// TODO: Validtsprfung
 				// TODO: Weiterleitung auf Andere Form
 
 				String enteredUsername = _tfUser.getText().toString();
 				String enteredPassword = _tfPw.getText().toString();
 
 				boolean checkedLogin = checkLogin(enteredUsername,
 						enteredPassword);
 
 				if (checkedLogin) {
 					
 					// Username und Passwort waren Ok
 					Intent intObj = new Intent(LoginActivity.this, MainActivity.class);
 					//intObj.putExtra("Language", "German");
 					startActivity(intObj);
 
 				} else {
 
 					// Username und Password waren nicht Ok
 					ErrorMessage.showErrorMessage(
 							getResources().getString(R.string.login_err_wrongPasswordOrUsername), "Ok",
 							v.getContext());
 				}
 			}
 		});
 
 		// ABORT Button clicked!
 		_btnAbort.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				finish();
 				System.exit(0);
 			}
 		});
 
 		// RadioButtonListener
 		_rgLanguage.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 			public void onCheckedChanged(RadioGroup group, int checkedId) {
 				if (checkedId == _rbGerman.getId()){
 					//germanRBClicked();
 				}
 				else{
 					//englishRBClicked();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Methode wird aufgerufen, falls der Radiobutton der Deutschen Sprache
 	 * ausgewhlt wird.
 	 * 
 	 * Hier werden alle Elemente auf die deutsche Sprache umgestellt.
 	 */
 	/*private void germanRBClicked() {
 
 		// falls noch kein Benutzername eingetragen wurde:
 		if (_tfUser.getText().toString().equals(_languageEnglish[0]))
 			_tfUser.setText(_languageGerman[0]);
 
 		_rbGerman.setText(_languageGerman[1]);
 		_rbEnglish.setText(_languageGerman[2]);
 		_btnLogin.setText(_languageGerman[3]);
 		_btnAbort.setText(_languageGerman[4]);
 	}*/
 
 	/**
 	 * Methode wird aufgerufen, falls der Radiobutton der Englischen Sprache
 	 * ausgewhlt wird.
 	 * 
 	 * Hier werden alle Elemente auf die englische Sprache umgestellt.
 	 */
 	/*private void englishRBClicked() {
 
 		// fals noch kein Benutzername eingetragen wurde:
 		if (_tfUser.getText().toString().equals(_languageGerman[0]))
 			_tfUser.setText(_languageEnglish[0]);
 
 		_rbGerman.setText(_languageEnglish[1]);
 		_rbEnglish.setText(_languageEnglish[2]);
 		_btnLogin.setText(_languageEnglish[3]);
 		_btnAbort.setText(_languageEnglish[4]);
 	}*/
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_login, menu);
 		return true;
 	}
 
 	/**
 	 * Methode wird aufgerufen, falls man Username und Password berprfen
 	 * mchte
 	 * 
 	 * Hier werden Username und Passwort in der Datenbank geprft. Stimmen
 	 * Benutezrname und Password berein, wird true, ansonsten false
 	 * zurckgegeben.
 	 */
 	private boolean checkLogin(String username, String password) {
 //Verbindung zum Server erstellen
	//	return true;
 		try {
 			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
 			.permitAll().build();
 			StrictMode.setThreadPolicy(policy);
 			socket = new Socket("10.0.2.2", 4711);
 			//validtsprfung
 			
 			PrintWriter out;
 			out = new PrintWriter(socket.getOutputStream(), true);
 			out.println("Login");
 			out.flush();
 			String user = ((EditText)findViewById(R.id.txtUsername)).getText().toString();
 			String pw = ((EditText)findViewById(R.id.pwdUserpassword)).getText().toString();;
 			out.println(user + " " + pw);
 			out.flush();
 			//empfangen
 			
 			BufferedReader reader = new BufferedReader(new InputStreamReader(
 					socket.getInputStream()));
 			
 			try{
 				String x = reader.readLine();
 				if(x.equals("Success"))
 						return true;
 				if(x.equals("Failure"))
 						return false;
 				((EditText)findViewById(R.id.txtUsername)).setText("HURENKIND " + x);
 			return false;
 			} catch (IOException e){
 				((EditText)findViewById(R.id.txtUsername)).setText("HURENKIND2 ");
 				return false;
 			}
 		} catch (UnknownHostException e) {
 			System.out.println("UNKNOWN HOST");
 			return false;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 }
