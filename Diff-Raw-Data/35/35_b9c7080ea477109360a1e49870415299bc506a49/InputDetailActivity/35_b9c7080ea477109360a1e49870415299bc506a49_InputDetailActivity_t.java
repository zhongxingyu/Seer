 package com.mimo.app;
 
 import org.apache.commons.codec.binary.StringUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.mimo.app.interfaces.IApp;
 import com.mimo.app.interfaces.IConfiguration;
 import com.mimo.app.model.adapter.DBAdapter;
 import com.mimo.app.model.pojo.ActivityEvent;
 import com.mimo.app.view.form.FormInputView;
 
 //Test
 public class InputDetailActivity extends FormInputView implements IConfiguration, IApp{
 	
 	private ActivityEvent a;
 	private int paramid;
 	private TextView tLat;
 	private TextView tLng;
 
 	public void onCreate(Bundle savedInstanceState){
 		super.onCreate(savedInstanceState);
 		
 		Bundle bundle = getIntent().getExtras();
 		
 		if(bundle == null){
 			initialize(R.layout.layout_form_new);
 			return;
 		} else {
 			initialize(R.layout.layout_form_detail);
 		}
 		
 		action = bundle.getInt("paramaction");
 		paramid = bundle.getInt(PARAMS_KEY);
 		
 		double paramlat = bundle.getDouble("paramlat");
 		double paramlng = bundle.getDouble("paramlng");
 		p.println("paramlat :  "+paramlat);
 		p.println("paramlng :  "+paramlng);
 		a = new ActivityEvent();
 		
 		showForm(paramid);  
 			
 		tLat = (TextView)findViewById(R.id.edit_lat);
 		tLng = (TextView)findViewById(R.id.edit_lng);
 		
 		if(paramlat!=0&&paramlng!=0){
 			tLat.setText(""+paramlat);
 			tLng.setText(""+paramlng);
 		}
 	}
 	
 	@Override 
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {   
 		Log.e("--------------- debug paramlat:xxxxx: ", "dodol");
 		Bundle bundle = data.getExtras();
 		
 		tLat = (TextView)findViewById(R.id.edit_lat);
 		tLng = (TextView)findViewById(R.id.edit_lng);
 		
 		tLat.setText(bundle.getDouble("paramlat")+"");
 		tLng.setText(bundle.getDouble("paramlng")+"");
 		
 		Log.e("--------------- debug paramlat:xxxxx", bundle.getDouble("paramlat")+"");
 	}
 	
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch (v.getId()){
 		case R.id.bStartDatePicker:
 			showDialog(DATE_DIALOG_ID);
 			
 			break;
 		case R.id.bStartTimePicker:
 			showDialog(TIME_DIALOG_ID);
 			
 			break;
 		case R.id.bEndDatePicker:
 			showDialog(DATE_END_DIALOG_ID);
 			
 			break;
 		case R.id.bEndTimePicker:
 			showDialog(TIME_END_DIALOG_ID);
 			
 			break;
 		case R.id.bMapPicker:
 			Intent mapIntent = new Intent(this, MapLocationActivity.class); 
 			mapIntent.putExtra(PARAMS_KEY, paramid);
 			startActivityForResult(mapIntent, paramid);
 
 			break; 
 		case R.id.btn_submit:
 			a =new ActivityEvent();
 			DBAdapter db = new DBAdapter(this);
 			
 			
 			if(action > 0){
 				TextView t = (TextView)findViewById(R.id.hidden_value);
 				a.setId(Integer.parseInt(t.getText().toString()));
 				setActivityValue(a);
 				db.updateRecord(a);
 				
 				Intent i = new Intent(this, DetailActivity.class);
 		    	i.putExtra(PARAMS_KEY, a.getId());
 				startActivity(i); 
 				
 			} else {
 				setActivityValue(a);
 				db.insertRecord(a);
 				finish();
 			}
 			
 			break;
 		case R.id.icon_activity:
 			String[] labels = idIcon.getLabels();
 			final int[] icons = idIcon.getIcons();
 			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
 			dialog.setTitle("Choose your Activity icon");
 			dialog.setAdapter(getDialogAdapter(labels, icons), new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					ImageView img =(ImageView)findViewById(R.id.icon_activity);
             		Drawable drawable = getResources().getDrawable(icons[which]);
             		img.setImageDrawable(drawable);
             		
             		//set label icon name
             		TextView eIcon = (TextView)findViewById(R.id.icon_label);
             		eIcon.setText(idIcon.getLabels()[which]);
 					
 				}});
 			dialog.show();
 		}
 		
 		if (v == homeButton) {
			Intent i = new Intent();
			i.setClass(this, HomeActivity.class);
			i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(i);
 		}
 	}
 	
 }                      
