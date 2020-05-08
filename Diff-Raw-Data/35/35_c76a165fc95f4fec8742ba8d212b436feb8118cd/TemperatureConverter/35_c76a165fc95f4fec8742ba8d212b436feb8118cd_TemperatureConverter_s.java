 package com.webs.samirapplications.temperature_convert;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Button;
 import android.view.View;
 
 
 
 public class TemperatureConverter extends Activity 
 {
     /** Called when the activity is first created. */
 	
 	EditText in;
 	double temp = 0;
 	double ans = 0;
 	double tempc = 0;
 	double tempf = 0;
 	double tempk = 0;
 	TextView txtanswer;
 	Button toF;
 	Button toC;
 	Button toK;
 	Button C;
 	Button F;
 	Button K;
	final String fah = "F";
	final String cel = "C";
 	final String kel = " K";
 	
 	
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         initControls();
     }
     
     public void initControls()
     {
         toF = (Button)findViewById(R.id.toF);
         toC = (Button)findViewById(R.id.toC);
         toK = (Button)findViewById(R.id.toK);
         C = (Button)findViewById(R.id.C);
         F = (Button)findViewById(R.id.F);
         K = (Button)findViewById(R.id.K);
         txtanswer = (TextView)findViewById(R.id.txtanswer);
         in = (EditText)findViewById(R.id.tempin);
         
         F.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ 

       	        temp=Double.parseDouble(in.getText().toString());
       			F(temp);
       		} 
       		
        	});
         C.setOnClickListener(new Button.OnClickListener() 
       	{ 
         	public void onClick (View v)
       		{ 

       	        temp=Double.parseDouble(in.getText().toString());
       			C(temp);
       		} 
       		
        	});
         K.setOnClickListener(new Button.OnClickListener() 
       	{ 
         	public void onClick (View v)
       		{ 

       	        temp=Double.parseDouble(in.getText().toString());
       			K(temp);
       		} 
       		
        	});
         toC.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ print(tempc, cel);} 
       		
        	});
       	toF.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ print(tempf, fah);} 
       		
        	});
       	toK.setOnClickListener(new Button.OnClickListener() 
       	{ 
       		public void onClick (View v)
       		{ print(tempk, kel);} 
       		
        	});
     }
     public void F(double temp)
     {
     	tempf = temp;
     	tempc = (temp - 32) * (5/9);
     	tempk = ((temp - 32) * (5/9))+ 273.15;
     }
     public void C(double temp) 
 	{
 		tempc = temp;
 		tempf = temp * (9/5) + 32;
 		tempk = temp + 273.15;
 	}
     public void K(double temp) 
 	{
 		tempk = temp;
 		tempc = temp - 273.15;
 		tempf = (temp - 273.15) * (9/5) + 32;
 	}
     public void print(double ans, String deg)
     {
     	String answer = ans + deg;
     	txtanswer.setText(answer);
     }
     public void reset()
     {
     	in.setText("");
     }
 }
