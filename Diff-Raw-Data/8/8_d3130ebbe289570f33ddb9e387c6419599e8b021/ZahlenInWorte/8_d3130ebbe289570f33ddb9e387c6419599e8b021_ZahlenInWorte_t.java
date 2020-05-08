 package com.example.infoapps;
 
 import android.app.Activity;
 import android.app.Dialog;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ZahlenInWorte extends Activity {
 
 	Button go;
 	EditText inputTxt;
 	TextView outputTxt;
 	TextView numLength, wordLength;
 	String anfangsString;
 	String ausgabeString;
 	int number;
 	LinearLayout scrollView;
 	int absaetze;
 	boolean absatzOn;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.zahleninworte);
 
 		Initialize();
 
 		inputTxt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 
 			@Override
 			public boolean onEditorAction(TextView v, int actionId,
 					KeyEvent event) {
 				// TODO Auto-generated method stub
 				ausgabeString = "";
 				absaetze = 0;
 
 				if (inputTxt.length() == 0)
 					anfangsString = "0";
 				else
 					anfangsString = inputTxt.getText().toString();
 
 				Compute();
 				Stats();
 
 				outputTxt.setText(ausgabeString);
 				return false;
 			}
 		});
 
 		go.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				ausgabeString = "";
 				absaetze = 0;
 
 				if (inputTxt.length() == 0)
 					anfangsString = "0";
 				else
 					anfangsString = inputTxt.getText().toString();
 
 				Compute();
 				Stats();
 				outputTxt.setText(ausgabeString);
 
 			}
 		});
 
 	}
 
 	protected void Stats() {
 		// TODO Auto-generated method stub
 
 		// android:paddingBottom="10dp"
 		// android:paddingTop="25dp"
 
 		SharedPreferences showNumberLength = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		boolean showNumberLengthB = showNumberLength.getBoolean(
 				"zahlenZahlLaenge", false);
 
 		if (showNumberLengthB) {
 			numLength.setVisibility(View.VISIBLE);
 			numLength.setText("Zahlen: " + Integer.toString(inputTxt.length()));
 		} else
 			numLength.setVisibility(View.INVISIBLE);
 
 		SharedPreferences showWordLength = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		boolean showWordLengthB = showWordLength.getBoolean("zahlenWortLaenge",
 				false);
 
 		if (showWordLengthB) {
 			wordLength.setVisibility(View.VISIBLE);
 			wordLength.setText("Buchstaben: "
 					+ Integer.toString(outputTxt.length() - 1));
 		} else
 			wordLength.setVisibility(View.INVISIBLE);
 
 		if (showNumberLengthB == false && showWordLengthB == false) {
 			scrollView.setPadding(0, 25, 0, 0);
 			// inputTxt.setText("No Text");
 		}
 		if (showNumberLengthB == true || showWordLengthB == true)
 			scrollView.setPadding(0, 25, 0, 30);
 
 	}
 
 	protected void Compute() {
 		// TODO Auto-generated method stub
 
 		int laenge = anfangsString.length();
 
 		int extra = Math.abs(laenge % 3 - 3);
 
 		if (extra == 1)
 			anfangsString = "0" + anfangsString;
 		else if (extra == 2)
 			anfangsString = "00" + anfangsString;
 		// inputTxt.setText(anfangsString);
 
 		int durchlauf = anfangsString.length() / 3;
 
 		String[] BigNumberSingle = { "", "tausend", "million", "milliarde",
 				"billion", "billiarde", "trillion", "trilliarde",
 				"quadrillion", "quadrilliarde", "quintillion", "quintilliarde",
 				"sextillion", "sextilliarde", "septillion", "septilliarde",
 				"oktillion", "oktilliarde", "nonillion", "nonilliarde",
 				"dezillion", "dezilliarde", "undezillion", "undezilliarde",
 				"dodezillion", "dodezilliarde", "tredezillion",
 				"tredezilliarde", "quattuordezillion" };
 		String[] BigNumberMulti = { "", "tausend", "millionen", "milliarden",
 				"billionen", "billiarden", "trillionen", "trilliarden",
 				"quadrillionen", "quadrilliarden", "quintillionen",
 				"quintilliarden", "sextillionen", "sextilliarden",
 				"septillionen", "septilliarden", "oktillionen", "oktilliarden",
 				"nonillionen", "nonilliarden", "dezillionen", "dezilliarden",
 				"undezillionen", "undezilliarden", "dodezillionen",
 				"dodezilliarden ", "tredezillionen", "tredezilliarden",
 				"quattuordezillionen" };
 		String spaceString;
 
 		SharedPreferences absatzOnP = PreferenceManager
 				.getDefaultSharedPreferences(getBaseContext());
 		absatzOn = absatzOnP.getBoolean("zahlenAbsatz", true);
 
 		if (absatzOn == true) {
 			spaceString = "\n";
 			absaetze++;
 		} else
 			spaceString = "";
 
 		if (laenge > 87)
 			ausgabeString = "Jetzt ist aber gut. \nIch wei nicht mehr weiter";
 		else {
 			String ubergabeString;
 			int namenStelle = durchlauf;
 			for (int p = 0; p < durchlauf; p++) {
 				namenStelle--;
 
 				int anfang = p * 3;
 				ubergabeString = anfangsString.substring(anfang, anfang + 3);
 				// if (namenStelle == 1)
 				// inputTxt.setText(ubergabeString);
 				int ubergabeInt = Integer.parseInt(ubergabeString);
 				ausgabeString += BisTausend(ubergabeString, namenStelle,
 						ausgabeString);
				if (ubergabeInt == 0);
 				else if (ubergabeInt == 1) {
 					ausgabeString += BigNumberSingle[namenStelle];
 					ausgabeString += spaceString;
 				} else {
 					ausgabeString += BigNumberMulti[namenStelle];
 					ausgabeString += spaceString;
 				}
 			}
 		}
 		if (absatzOn == true)
 			ausgabeString = ausgabeString.substring(0,
 					ausgabeString.length() - 1);
 		outputTxt.setText(ausgabeString);
 		Toast t = Toast.makeText(ZahlenInWorte.this, ausgabeString,
 				Toast.LENGTH_LONG);
 	}
 
 	private String BisTausend(String zahlen, int durchgang, String totalStr) {
 		// TODO Auto-generated method stub
 		String wort = "";
 		// int durchgang = 0;
 		String zehnerString = zahlen.substring(1, 3);
 		String hunderterString = zahlen.substring(0, 1);
 
 		int hunderterInt = Integer.parseInt(hunderterString);
 		int zehnerInt = Integer.parseInt(zehnerString);
 		int zehnerProzentInt = zehnerInt % 10;
 		int zehnerIntStelle = zehnerInt / 10;
 		int einerStelleInt = zehnerInt % 10;
 
 		String[] einerArray = { "null", "ein", "zwei", "drei", "vier", "fnf",
 				"sechs", "sieben", "acht", "neun", "zehn", "elf", "zwlf",
 				"dreizehn", "vierzehn", "fnfzehn", "sechszehn", "siebzehn",
 				"achtzehn", "neunzehn" };
 		String[] zehnerArray = { "zwanzig", "dreiig", "vierzig", "fnfzig",
 				"sechzig", "siebzig", "achtzig", "neunzig" };
 		String[] hunderterArray = { "", "ein", "zwei", "drei", "vier", "fnf",
 				"sechs", "sieben", "acht", "neun" };
 
 		if (hunderterInt > 0) {
 			wort += hunderterArray[hunderterInt];
 			wort += "hundert";
 		}
 
		if (zehnerInt == 0 && hunderterInt == 0 && durchgang == 0 &&totalStr.equalsIgnoreCase(""))
			wort += "nulll"; // oder einerArray[0];
 
 		if (zehnerInt == 1 && durchgang == 0)
 			wort += "eins";
 
 		else if (hunderterInt == 0 && zehnerInt == 1 && durchgang > 1)
 			wort += "eine";
 
 		else if (zehnerInt >= 1 && zehnerInt < 20) {
 			wort += einerArray[zehnerInt];
 		} else if (zehnerInt > 19 && hunderterInt >= 0 && zehnerProzentInt == 0) {
 			wort += zehnerArray[zehnerIntStelle - 2];
 		} else if (zehnerInt > 19 && hunderterInt >= 0) {
 			wort += hunderterArray[einerStelleInt];
 			wort += "und";
 			wort += zehnerArray[zehnerIntStelle - 2];
 		}
 		return wort;
 	}
 
 	private void Initialize() {
 		// TODO Auto-generated method stub
 		go = (Button) findViewById(R.id.zahlensGo);
 		inputTxt = (EditText) findViewById(R.id.zahlensInput);
 		outputTxt = (TextView) findViewById(R.id.zahlensOutput);
 		numLength = (TextView) findViewById(R.id.zahlensNumLength);
 		wordLength = (TextView) findViewById(R.id.zahlensWordLength);
 		scrollView = (LinearLayout) findViewById(R.id.zahlenslayoutScroll);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// TODO Auto-generated method stub
 		super.onCreateOptionsMenu(menu);
 		MenuInflater showMenu = getMenuInflater();
 		showMenu.inflate(R.menu.zahlenworte, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		// TODO Auto-generated method stub
 		String aufgabeNum = "Blatt 8 Aufgabe 2";
 		String realClassName = this.getClass().getName().substring(21);
 		String aufgabe = this.getTitle().toString();
 		switch (item.getItemId()) {
 		case (R.id.zahlensEinstellungen):
 			Intent i = new Intent("com.example.infoapps.ZAHLENSPREFS");
 			startActivity(i);
 			break;
 
 		case (R.id.aufgabe):
 			Dialog d = new Dialog(this, 0);
 			TextView tvAufgabe = new TextView(this);
 			String aufgabeText = "Auf berweisungsformularen, Schecks usw. wird der Betrag mit Ziffern und als Text notiert. Schreiben "
 					+ "Sie ein Programm, dass eine eingegebene Zahl in Textform ausgibt (z. B. 123->einhundertdreiundzwanzig)";
 			d.setTitle(aufgabe);
 			tvAufgabe.setText(aufgabe + " - " + aufgabeNum + "\n\n"
 					+ aufgabeText);
 			d.setContentView(tvAufgabe);
 			d.show();
 
 			break;
 
 		case R.id.bug:
 			Bundle sendClassName = new Bundle();
 			sendClassName.putString("bugClass", realClassName);
 			sendClassName.putString("bugNum", aufgabeNum);
 			Intent bugSend = new Intent(this, BugSubmit.class);
 			bugSend.putExtras(sendClassName);
 			startActivity(bugSend);
 			break;
 
 		case R.id.code:
 			Intent myWebLink = new Intent(android.content.Intent.ACTION_VIEW);
 			myWebLink
 					.setData(Uri
 							.parse("https://github.com/psud/BWS-Info-Apps/blob/master/src/com/example/infoapps/"
 									+ realClassName + ".java"));
 			startActivity(myWebLink);
 			break;
 		}
 		return false;
 	}
 }
