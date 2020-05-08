 package com.beef.homework;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 
 
 import android.R.string;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.graphics.Point;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.Spinner;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
 	String FILENAME = "todo";
 	int run = 0;
 	ArrayList<String> list = new ArrayList<String>();
 	String[] values = new String[list.size()];
 		int counter = 0;
 	String position1 = "0";
 		
 	 @Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Spinner s1 = (Spinner)findViewById(R.id.spinner1);
 		ArrayAdapter<CharSequence> adapter1 =
 		ArrayAdapter.createFromResource(this,
 		R.array.array, android.R.layout.simple_spinner_item);
 		
 		adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		s1.setAdapter(adapter1);
 		
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
 		 ListView listview = (ListView) findViewById(R.id.listview);
 		 listview.setAdapter(adapter);
 		 listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 				
 			 public void onItemClick(AdapterView<?> parent,View v, int position, long id){
 				 String item = (String) parent.getItemAtPosition(position);
 				 position1 = item;
 				showPopup(MainActivity.this );
 				 
 				 
 				 saveArray();
 
 			 }
 		 
 			 
 	});
 		 
 		 //if(run() == false){
 		 readArray();
 			 refresh();
 		// }
 		// else{
 		 
 		// refresh();
 		// }
 	 }
 	 private void showPopup(final Activity context) {
 		   int popupWidth = 200;
 		   int popupHeight = 150;
 		
 		   LinearLayout viewGroup = (LinearLayout) context.findViewById(R.id.popup);
 		   LayoutInflater layoutInflater = (LayoutInflater) context
 		     .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		   View layout = layoutInflater.inflate(R.layout.popup, viewGroup);
 
 		   final PopupWindow popup = new PopupWindow(context);
 		   popup.setContentView(layout);
 		   popup.setHeight(400);
 		   popup.setWidth(600);
 		   popup.setFocusable(true);
		   popup.showAtLocation(layout, 50 ,50 ,50);
 		   
 		   Button close = (Button) layout.findViewById(R.id.dismiss);
 		   
 		   close.setOnClickListener(new OnClickListener() {
 		 
 		     @Override
 		     public void onClick(View v) {
 		       popup.dismiss();
 		       
 		     }
 		   });
 		   Button confirm = (Button) layout.findViewById(R.id.confirm);
 		   confirm.setOnClickListener(new OnClickListener() {
 			   public void onClick(View v) {
 				   list.remove(position1);
 				   refresh();
 				   popup.dismiss();
 				   
 			   }
 		   });
 		}
 	 public void refresh(){
 		 
 		 ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
 		 ListView listview = (ListView) findViewById(R.id.listview);
 		 listview.setAdapter(adapter);
 	 }
 	public void onclick(View v) throws IOException{
 		EditText field = (EditText) findViewById(R.id.etext1);
 		String homework = field.getText().toString();
 		if(homework.equals("")){
 			Toast toast = Toast.makeText(this, "Please enter a value!", Toast.LENGTH_LONG);
 			toast.show();
 		}
 		else{
 		add(homework);
 		saveArray();
 		}
 	}
 	public void add(String nice) throws IOException{
 		
		
 			list.add(nice);
 			refresh();	
 		
 	}
 
 	public boolean run(){
 		SharedPreferences sharedpres = PreferenceManager.getDefaultSharedPreferences(this);
 		String run11 = sharedpres.getString("run", "");
 		if(run11.equals("0")){
 			return true;
 		}
 		else{
 		return false;
 		}
 		
 	}
 	public void saveArray(){
 		
 		values = list.toArray(values);
 		SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);
 		SharedPreferences.Editor editor = sharedprefs.edit();
 		for(int i = 0; i < list.size(); i++){
 	    String key = Integer.toString(i);
 		editor.putString(key, values[i]);
 		editor.commit();
 		counter++;
 		
 		}
 		run = 1;
 		String run1 = String.valueOf(run);
 		editor.putString("run", run1);
 		String counts = String.valueOf(counter);
 		editor.putString("counter",counts);
 		editor.commit();
 		counter = 0;
 	}
 	
 	public void readArray(){
 		SharedPreferences sharedprefs = PreferenceManager.getDefaultSharedPreferences(this);
 		int sharecount = Integer.valueOf(sharedprefs.getString("counter",""));
 		
 		for(int k = 0; k < sharecount; k++ ){
 			String key = Integer.toString(k);
 			String object = sharedprefs.getString(key, "");
 			list.add(object);
 		}
 		
 		refresh();
 	}
 	//String[] values = new String[MstringList.size();]
 	//values= values.toArray(mstringarray);
 
 	
 
 	
 
 	
 
 }
 
