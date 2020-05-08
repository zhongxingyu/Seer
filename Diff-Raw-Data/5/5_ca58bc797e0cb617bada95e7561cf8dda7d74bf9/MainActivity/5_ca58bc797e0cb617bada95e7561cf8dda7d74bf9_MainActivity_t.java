 /*
  * Authored By Julian Chu <walkingice@0xlab.org>
  *
  * Copyright (c) 2012 0xlab.org - http://0xlab.org/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.zeroxlab.momome.impl;
 
 import org.zeroxlab.momome.R;
 import org.zeroxlab.momome.data.Item;
 import org.zeroxlab.momome.Momo;
 import org.zeroxlab.momome.MomoApp;
 import org.zeroxlab.momome.MomoModel;
 import org.zeroxlab.momome.widget.ItemAdapter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import org.json.JSONObject;
 
 public class MainActivity extends Activity implements Momo {
     ListView mListView;
     ItemAdapter mAdapter;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         initViews();
 
         MomoModel model = MomoApp.getModel();
         mAdapter = new ItemAdapter(this);
         mListView.setAdapter(mAdapter);
         mListView.setOnItemClickListener(new OnItemClickListener() {
             public void onItemClick(AdapterView<?> a, View v, int pos, long id) {
                 launchDetailActivity(v.getTag().toString());
             }
         });
     }
 
     private void initViews() {
         mListView = (ListView) findViewById(R.id.main_list_view);
     }
 
     private void launchDetailActivity(String key) {
         Intent intent = new Intent(this, DetailActivity.class);
         intent.putExtra(CROSS_ITEM_KEY, key);
         startActivity(intent);
     }
 
     public void onClickSettings(View v) {
     }
 
     public void onClickEdit(View v) {
        MomoModel model = MomoApp.getModel();
        if (model.status() == DataStatus.OK) {
            getNewItemName();
        }
     }
 
     public void onClickReload(View v) {
     }
 
     private void getNewItemName() {
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         EditText edit = new EditText(this);
         DialogInterface.OnClickListener okListener = new AddItemListener(edit);
 
         builder.setMessage("Name of new Item");
         builder.setCancelable(true);
         builder.setView(edit);
         builder.setPositiveButton(android.R.string.ok, okListener);
         builder.setNegativeButton(android.R.string.cancel, okListener);
 
         builder.show();
     }
 
     private void onAddItem(CharSequence name) {
         Item item = new Item(name.toString());
         MomoApp.getModel().addItem(item);
         mAdapter.notifyDataSetChanged();
     }
 
     private class AddItemListener implements DialogInterface.OnClickListener {
         TextView iTextView;
 
         AddItemListener(TextView tv) {
             iTextView = tv;
         }
 
         public void onClick(DialogInterface dialog, int button) {
             CharSequence input = iTextView.getText();
             if (button != AlertDialog.BUTTON_POSITIVE
                     || input == null
                     || input.toString().equals("")) {
 
                 return; // cancel or does not give a name, nothing happen
             } else {
                 onAddItem(input);
             }
         }
     }
 }
