 /*
  * Author : ErVaLt / techwavedev.com
  * Description : TabLayout Andorid App
  */
 package com.example.tablayout;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
import android.widget.Button;
 import android.widget.EditText;
 
 public class MainActivity extends Activity implements OnClickListener {
 	private String naam;
 	private int poortNummer;
 	private String opdracht;
 	private String ipAdress;
 	private ServerCommunicator serverCommunicator;
 	
 
 	/** Called when the activity is first created. */
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.tab1);
		Button b = (Button)findViewById(R.id.button1);
		b.setOnClickListener(this);
		System.out.println("hij pakt de onclick");
 
 	}
 
 	public void onClick(View src) {
		EditText naamEditText = (EditText) this.findViewById(R.id.naamVeld); 
		naam = naamEditText.getText().toString();
		
		EditText poortNummerEditText = (EditText) this.findViewById(R.id.poortNummerVeld); 
		poortNummer = Integer.parseInt(poortNummerEditText.getText().toString());
		
		EditText opdrachtEditText = (EditText) this.findViewById(R.id.opdrachtVeld); 
		opdracht = opdrachtEditText.getText().toString();
		
		
		EditText ipAdressEditText = (EditText) this.findViewById(R.id.ipAdressVeld); 
		ipAdress = ipAdressEditText.getText().toString();
		
		String bericht = naam + " zegt: " + opdracht;
		
		System.out.println("ipadress:" + ipAdress);
		System.out.println("poortNummer:" + poortNummer);
		System.out.println("bericht:" + bericht);
		
		serverCommunicator = new ServerCommunicator(this, ipAdress, poortNummer, bericht);
		System.out.println("hij verzend");
 
 	}
 	
	/*public void VerstuurBericht(View v)
 	{
 		EditText naamEditText = (EditText) this.findViewById(R.id.naamVeld); 
 		naam = naamEditText.getText().toString();
 		
 		EditText poortNummerEditText = (EditText) this.findViewById(R.id.poortNummerVeld); 
 		poortNummer = Integer.parseInt(poortNummerEditText.getText().toString());
 		
 		EditText opdrachtEditText = (EditText) this.findViewById(R.id.opdrachtVeld); 
 		opdracht = naamEditText.getText().toString();
 		
 		
 		EditText ipAdressEditText = (EditText) this.findViewById(R.id.ipAdressVeld); 
 		ipAdress = naamEditText.getText().toString();
 		
 		String bericht = naam + " zegt: " + opdracht;
 		
 		serverCommunicator = new ServerCommunicator(this, ipAdress, poortNummer, bericht);
 		
 	}
	/*
	 * 
	 */
 }
