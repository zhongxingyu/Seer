 /**
  * Copyright 2013 Jim Hurne and Joseph Kramer
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kdt;
 
 import java.util.List;
 
 import org.kdt.kanbandatatracker.R;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
		onNewIntent(getIntent());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	@Override
     protected void onNewIntent(Intent intent) {
 		Scanner scanner = new NfcScanner();
 		List<String> payloads = scanner.scan(intent);
 		
 		for (String text : payloads) {
 			final TextView helloWorldText = getSnapshotControl();
 			//each activity is new and doesn't just keep a running list
		    helloWorldText.append("\n"+text);
 		}
 	}
 	
     @Override
     protected void onResume() {
         super.onResume();
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putString("snapshot", getSnapshotControl().getText().toString());
     }
     
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         getSnapshotControl().setText(savedInstanceState.getString("snapshot"));
     }
     
     public void saveSnapshot(View view) {
         getSnapshotControl().setText("");
     }    
     
     private TextView getSnapshotControl() {
        return (TextView) findViewById(R.id.snapshot_editText);
     }
 
 }
