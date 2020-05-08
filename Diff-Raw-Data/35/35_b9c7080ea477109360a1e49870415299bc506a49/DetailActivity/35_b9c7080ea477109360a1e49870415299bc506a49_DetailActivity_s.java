 package com.mimo.app;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.mimo.app.interfaces.IApp;
 import com.mimo.app.interfaces.IConfiguration;
 import com.mimo.app.model.adapter.DBAdapter;
 import com.mimo.app.view.detail.DetailView;
 
 //Test
 public class DetailActivity extends DetailView implements IConfiguration, IApp {
 	
 	private ImageButton homeButton;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState); 
 		
 		
 				
 		Bundle bundle = getIntent().getExtras();
 		int paramid = bundle.getInt(PARAMS_KEY);
 		initialize(R.layout.layout_detail);
 		// initialize component ui
 		homeButton = (ImageButton) findViewById(R.id.home_button);
 		homeButton.setOnClickListener(this);
 		
 		showDetail(paramid);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		final Intent intent;
 		final TextView tv = (TextView)findViewById(R.id.hidden_value);
 		
 		switch(v.getId()){ 
 		case R.id.btn_edit:
 			Intent i = new Intent(this, InputDetailActivity.class);
 			i.putExtra("paramaction", ACTION_UPDATE);
 			i.putExtra(PARAMS_KEY, Integer.parseInt(tv.getText().toString()));
 			startActivity(i);
 			
 			break;
 		case R.id.btn_map:
 			intent = new Intent(this, MapDashboardActivity.class);
 			startActivity(intent);
 			
 			break;
 		case R.id.btn_delete:
 			DBAdapter db = new DBAdapter(this);
 			db.deleteRecord(Integer.parseInt(tv.getText().toString()));
 			Toast.makeText(this, "Deleted", Toast.LENGTH_LONG).show();
 			intent = new Intent(this, EventListActivity.class);
 			startActivity(intent);
 			
 			break;
 		
 		}
 		
 		if (v == homeButton) {
			Intent i = new Intent(this, HomeActivity.class);
 			startActivity(i);
 		}
 		
 	}
 
 }
