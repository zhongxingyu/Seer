 package com.teamsexy.helloTabs;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.ContactsContract;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class GroupDetailForm extends Activity { 
 	
 	String spotId = null;
 	EditText name=null;
 	
 	GroupHelper helper=null;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.group_detail_form);
 		
 		
 		helper = new GroupHelper(this);
         
         name=(EditText)findViewById(R.id.name); 
 //        address=(EditText)findViewById(R.id.addr); 
 //        types=(RadioGroup)findViewById(R.id.types);
         
         Button save = (Button)findViewById(R.id.save);
         save.setOnClickListener(onSave);
         Button addFriends = (Button)findViewById(R.id.addFriends);
        save.setOnClickListener(onAddFriends);
         
         spotId = getIntent().getStringExtra(GroupsActivity.ID_EXTRA);
         
         if (spotId != null){
         	load();
         }
 	}
 	
 	public void onDestroy(){
 		super.onDestroy();
 		helper.close(); 
 	}
 	
 	private void load() {
 	    Cursor c=helper.getById(spotId);
 
 	    c.moveToFirst();    
 	    name.setText(helper.getName(c));
 //	    address.setText(helper.getAddress(c));
 //	    
 //	    if (helper.getType(c).equals("happy_hr")) {
 //	      types.check(R.id.happy_hr);
 //	    }
 //	    else {
 //	      types.check(R.id.my_spot);
 //	    }
 	    
 	    c.close();
 	  }
 	
 	private View.OnClickListener onSave = new View.OnClickListener() {
 		public void onClick(View v) {
 
 //			String type = null;
 //
 //			switch (types.getCheckedRadioButtonId()) {
 //			case R.id.happy_hr:
 //				type ="happy_hr";
 //				break;
 //			case R.id.my_spot:
 //				type ="my_spot";
 //				break;
 //			}
 			
 			if (spotId == null){
 				helper.insert(name.getText().toString()); //, address.getText().toString(), type);
 			} else {
 				helper.update(spotId, name.getText().toString()); //, address.getText().toString(), type);
 			}
 			
 			finish();
 		}
 	};
 	
 	private View.OnClickListener onAddFriends = new View.OnClickListener() {
 		public void onClick(View v) {
 			
			startActivityForResult(new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI), 1);
 		}
 	};
 	
 	@Override
 	public void onActivityResult(int reqCode, int resultCode, Intent data) {
 	  super.onActivityResult(reqCode, resultCode, data);
 
 	  switch (reqCode) {
 	    case (1) :
 	      if (resultCode == Activity.RESULT_OK) {
 	        Uri contactData = data.getData();
 	        Cursor c =  managedQuery(contactData, null, null, null, null);
 	        if (c.moveToFirst()) {
 	          String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
 	          
 	          // TODO STUFF with contacts
 	        }
 	      }
 	      break;
 	  }
 	}
 }
