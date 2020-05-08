 package com.money;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteDatabase;
 import android.content.ContentValues;
 import android.widget.Spinner;
 import android.widget.Toast;
 import android.widget.ArrayAdapter;
 
 public class ModifyAccount extends Activity 
 { 
 	private static String DB_NAME = "money.db";
 	private static int DB_VERSION = 1;
 	private static int POSTION;
 	
 	private SQLiteDatabase db;
 	private SQLiteHelper dbHelper;
 	private Cursor cursor;
 	
 	private Spinner accname;
 	private Spinner accclassify;
 	private EditText money;
 	private EditText commit;
 	
 	private String account_classify[];
 	
 	public void onCreate(Bundle savedInstanceState)
 	{
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.modifyacc);
 	    
     	try{
     		dbHelper = new SQLiteHelper(this, DB_NAME, null, DB_VERSION);
     		db = dbHelper.getWritableDatabase();
     	}
 		catch(IllegalArgumentException e){
     		e.printStackTrace();
     		++ DB_VERSION;
     		dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
     	}	   
 		
 		accname = (Spinner) findViewById(R.id.accname);
 		accclassify = (Spinner) findViewById(R.id.accclassify);
 		money = (EditText) findViewById(R.id.money);
 		commit = (EditText) findViewById(R.id.commit);
 		
 		account_classify = this.getResources().getStringArray(R.array.accclassify_list);
 
 		query_account();
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, account_classify);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		accclassify.setAdapter(adapter);		
 
 		accname.setOnItemSelectedListener(new Spinner.OnItemSelectedListener(){
 	           public void onItemSelected(AdapterView adapterView, View view, int position, long id)
 	           {
 	        	   //openOptionsDialog(Account_item.ACCOUNT + "='" + accname.getSelectedItem().toString().trim() + "'");
 	        	   try{
 			    		cursor = db.query(SQLiteHelper.TB_NAME_A, null, Account_item.ACCOUNT + "='" + accname.getSelectedItem().toString().trim() + "'" , null, null, null, null);
 			    		cursor.moveToFirst();
 			    		
 			    		//nodata
 			    		if (cursor.isAfterLast())
 			    		{
 			    			return;
 			    		}
 			        	while(!cursor.isAfterLast())
 			        	{
 			        		//find it
 			        		int len = account_classify.length;
 			        		for (int i=0; i<len; i++)
 			        		{
 			        			if (account_classify[i].equals(cursor.getString(2)))
 			        			{
 			        				accclassify.setSelection(i);
 			        				break;
 			        			}
 			        		}
 			        		money.setText(cursor.getString(3));
 			        		commit.setText(cursor.getString(4));
 	
 			         		cursor.moveToNext();
 			        	}
 			    	}catch(IllegalArgumentException e){
 			    		e.printStackTrace();
 			    		++ DB_VERSION;
 			    		dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
 			    	}	 
 		    	 
 	           }
 	           public void onNothingSelected(AdapterView arg0) 
 	           {
 	           }
 	        });
 
 		
 	    Button accadd=(Button) findViewById(R.id.addacc);
 	    accadd.setOnClickListener(new Button.OnClickListener()
 	     {
 	        	public void onClick(View v)
 	        	{
 	        		//upgrade
 					ContentValues values = new ContentValues();
 					//values.put(Account_item.ACCOUNT, accname.getSelectedItem().toString().trim());
 					values.put(Account_item.CLASSIFY, accclassify.getSelectedItem().toString().trim());
 					values.put(Account_item.MONEY, money.getText().toString().trim());
 					values.put(Account_item.COMMIT, commit.getText().toString().trim());
 					values.put(Account_item.COST, money.getText().toString().trim());
 					
 					//SQL
 					
 			    	try{
						//int AccountID = db.update(SQLiteHelper.TB_NAME_A, values, where, null);
 			    	}
 					catch(IllegalArgumentException e){
 			    		e.printStackTrace();
 			    		++ DB_VERSION;
 			    		dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
 			    	}
 					Toast.makeText(ModifyAccount.this, "modify success.", Toast.LENGTH_LONG).show();	        		
 	        	}
 	      }
 	    );
 	    
 	    Button exitb=(Button) findViewById(R.id.exitb);
 	    exitb.setOnClickListener(new Button.OnClickListener()
 	     {
 	        	public void onClick(View v)
 	        	{
 	        		db.close();
 	        		dbHelper.close();
 
 	        		Intent intent = new Intent();
 	        		intent.setClass(ModifyAccount.this, MoneyListView.class);
 
 	        		startActivity(intent);
 	        		ModifyAccount.this.finish();	  
 	        	}
 	      }
 	    );
 	    
 	  }
 
     //error message
     private void openOptionsDialog(String info)
 	{
 	    new AlertDialog.Builder(this)
 	    .setTitle("Inquire")
 	    .setMessage(info)
 	    .setPositiveButton("OK",
 	        new DialogInterface.OnClickListener()
 	        {
 	         public void onClick(DialogInterface dialoginterface, int i)
 	         {
 	         }
 	         }
 	        )
 	    .show();
 	}
 	
 	  private int query_account()
       {
 		    List<String> account_c = new ArrayList<String>();
 	    	//fetch acoount's cost
 	    	try{
 	    		cursor = db.query(SQLiteHelper.TB_NAME_A, null, null, null, null, null, null);
 	    		cursor.moveToFirst();
 	    		
 	    		//nodata
 	    		if (cursor.isAfterLast())
 	    		{
 	    			return 0;
 	    		}
 	        	while(!cursor.isAfterLast())
 	        	{   
 	        		account_c.add(cursor.getString(1));
 	         		cursor.moveToNext();
 	        	}
 	    	}catch(IllegalArgumentException e){
 	    		e.printStackTrace();
 	    		++ DB_VERSION;
 	    		dbHelper.onUpgrade(db, --DB_VERSION, DB_VERSION);
 	    	}	    
 	    	
 			ArrayAdapter<String> adapter_a = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item, account_c);
 			adapter_a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 			accname.setAdapter(adapter_a);
 
 	    	
 			return 1;
       }
 	  
 
 }
