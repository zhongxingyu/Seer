 package com.ivinny.tempcalc;
 
 import android.app.Fragment;
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ConversionFragment extends Fragment {
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setRetainInstance(true);
 	}
 	
 	@Override
 	public void onSaveInstanceState(Bundle outState) {
 		// TODO Auto-generated method stub
 		super.onSaveInstanceState(outState);
 		outState.putBoolean("clicked", clicked);
 		outState.putInt("lasTemp", lastTemp);
 	}
 	
 	String[] types;
 	TextView result;
 	String url = "";
 	TextView tv;
 	EditText et;
 	Boolean clicked = false;
 	int lastTemp = 0;
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		
 //		if(savedInstanceState != null){
 //			clicked = savedInstanceState.getBoolean("clicked");
 //			lastTemp = savedInstanceState.getInt("lastTemp");
 //		}
 		
 		types = new String[3];
 		types[0] = getString(R.string.f);
 		types[1] = getString(R.string.c);
 		types[2] = getString(R.string.k);
 		
 		
 		LinearLayout ll = (LinearLayout) inflater.inflate(R.layout.conversion, container, false);
 
 		//simple info view
 		tv = (TextView)ll.findViewById(R.id.textView1);;
 		tv.setText("Convert Fahrenheit to Celsius and Kelvin");
 				
 		et = (EditText)ll.findViewById(R.id.editText1);
 		et.setHint("Enter Farhenheit");
		if(lastTemp > 0){
			et.setText(String.valueOf(lastTemp));
		}
 //				ll.addView(et);
 		
 		Button b = (Button)ll.findViewById(R.id.button1);
 		b.setText("Convert");
 //				ll.addView(b);
 		b.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				
 				updateTemps(et.getText().toString());
 //				clicked = true;
 //				lastTemp = Integer.parseInt(et.getText().toString());
 			}
 		});
 		
 
 		
 		result = (TextView)ll.findViewById(R.id.textView2);
 		
 		
         Bundle bundleObj= getActivity().getIntent().getExtras();
         try {
         		String temp = bundleObj.get("temp").toString();
         		String city = bundleObj.get("city").toString();
         		url = bundleObj.get("url").toString();
                 updateUI(url, city, temp);
         }
         catch (Exception e) {
                 Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
         }
         
 		Button mInfoB = (Button)ll.findViewById(R.id.button2);
 		mInfoB.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 				startActivity(browserIntent);
 			}
 		});
 		
 		ScrollView sv = new ScrollView(getActivity());
 		sv.addView(ll);
 		
 //		if(clicked){
 //			b.performClick();
 //		}
 		return sv;
 	}
 	
 	public void updateUI(String aUrl, String aCity, String aTemp){
 		url = aUrl;
 		tv.setText("Convert Fahrenheit to Celsius and Kelvin for " + aCity);
         et.setText(aTemp);
         updateTemps(aTemp);
 	}
 	
 	//does the temp conversion
 	public void updateTemps(String temp){
 		String text = "";
 		for(int i = 0; i<types.length; i++){
 			String type = types[i];
 			Float finalTemp = Float.valueOf(0);
 			Boolean isF = type == getString(R.string.f);
 			Boolean isC = type == getString(R.string.c);
 			if(isF){
 				finalTemp = Float.parseFloat(temp);
 			}else if(isC){
 				finalTemp = (float) ((Float.parseFloat(temp)-32)*.55);
 			}else{
 				finalTemp = (float) ((Float.parseFloat(temp)-32)*.55 + 273.15);
 			}
 			text = text+"\r\n"+finalTemp+" "+type;
 		}
 		Log.i("temps", text);
 		result.setText(text);
 	}
 	
 	@Override
 	public void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 	}
 	
 }
