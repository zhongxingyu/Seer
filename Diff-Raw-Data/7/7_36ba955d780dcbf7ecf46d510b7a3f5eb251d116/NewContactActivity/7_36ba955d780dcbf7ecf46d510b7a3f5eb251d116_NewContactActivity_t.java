 /*
  * Copyright 2012 Roman Nurik
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package edu.ucsb.cs290.touch.to.chat;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import edu.ucsb.cs290.touch.to.chat.crypto.KeyExchange;
 import edu.ucsb.cs290.touch.to.chat.crypto.SealablePublicKey;
 
 public class NewContactActivity extends Activity {
 	private SealablePublicKey key;
 	private EditText contactName;
 	private byte[] secret;
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Inflate a "Done/Discard" custom action bar view.
         LayoutInflater inflater = (LayoutInflater) getActionBar().getThemedContext()
                 .getSystemService(LAYOUT_INFLATER_SERVICE);
         final View customActionBarView = inflater.inflate(
                 R.layout.actionbar_custom_view_done_discard, null);
         customActionBarView.findViewById(R.id.actionbar_done).setOnClickListener(
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         // "Done"
                     	Intent i = new Intent();
                     	i.putExtra("name", contactName.getText().toString());
                     	i.putExtra("date", System.currentTimeMillis());
                     	i.putExtra("key", key);
                     	i.putExtra("signedsecret", secret);
                     	setResult(RESULT_OK,i);
                         finish();
                     }
                 });
         customActionBarView.findViewById(R.id.actionbar_discard).setOnClickListener(
                 new View.OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         // "Discard"
                     	setResult(RESULT_CANCELED);
                         finish(); // TODO: don't just finish()!
                     }
                 });
 
         // Show the custom action bar view and hide the normal Home icon and title.
         final ActionBar actionBar = getActionBar();
         actionBar.setDisplayOptions(
                 ActionBar.DISPLAY_SHOW_CUSTOM,
                 ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                         | ActionBar.DISPLAY_SHOW_TITLE);
         actionBar.setCustomView(customActionBarView, new ActionBar.LayoutParams(
                 ViewGroup.LayoutParams.MATCH_PARENT,
                 ViewGroup.LayoutParams.MATCH_PARENT));
         contactName = (EditText) findViewById(R.id.edit_contact_name);
         //Now that we've inflated the ActionBar, let's do the content:
         setContentView(R.layout.edit_contact_info);
     }
     
     public void getPublicKey(View v) {
     	Button b = (Button) v.findViewById(R.id.edit_contact_key_btn);
     	b.setText("Generating Keys...");
     	b.setClickable(false);
     	final Activity current = this;
     	new AsyncTask<Object, Object, KeyExchange> () {
 
 			@Override
 			protected KeyExchange doInBackground(Object... params) {
 				return new KeyExchange();
 			}
 			
 			@Override
 			protected void onPostExecute(KeyExchange result) {
 				Intent i = new Intent(current,BeginKeyExchangeActivity.class);
 				i.putExtra("keyExchange", result);
 		    	startActivityForResult(i, 1);
 			}
     	}.execute();
     }
     @Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
     	super.onActivityResult(requestCode, resultCode, data);
     	if(resultCode == RESULT_CANCELED) {
     		return;
     	}
     	switch(requestCode) {
     	case 1: 
     		key = (SealablePublicKey) data.getSerializableExtra("key_package");
     		TextView t = (TextView) findViewById(R.id.edit_contact_key_signature);
     		t.setText(key.digest());
     		// Should add user in caller of this Activity
     		//DatabaseHelper.getInstance(getApplicationContext()).addPublicKey(key);    		
     	}
     }
 }
