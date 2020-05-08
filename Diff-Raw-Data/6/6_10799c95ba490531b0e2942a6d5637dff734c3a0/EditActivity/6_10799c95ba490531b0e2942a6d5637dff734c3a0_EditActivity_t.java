 /*
  * Copyright (c) 2011 Hiroshi Okada (http://toycode.com/hiroshi/)
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *   1.The origin of this software must not be misrepresented; you must
  *     not claim that you wrote the original software. If you use this
  *     software in a product, an acknowledgment in the product
  *     documentation would be appreciated but is not required.
  *   
  *   2.Altered source versions must be plainly marked as such, and must
  *     not be misrepresented as being the original software.
  *   
  *   3.This notice may not be removed or altered from any source
  *     distribution.
  */
 
 package com.toycode.pwmemo;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.widget.EditText;
 
 import java.util.Observable;
 import java.util.Observer;
 
 /**
  * The Activity for edit old and new item.
  * 
  * @author Hiroshi Okada
  *
  */
 public class EditActivity extends Activity implements OnClickListener, Observer, TextWatcher, OnFocusChangeListener {
     EditText mTitleEdit;
     EditText mUserIdEdit;
     EditText mPasswordEdit;
     EditText mMemoEdit;
     DbRw mDbRw;
     Long mId;
     Boolean mIsNewRow;
     App mApp;
     boolean mDirty;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mApp = App.GetApp(this);
         mDirty = false;
         SQLiteDatabase db = (new PwMemoDbOpenHelper(this)).getReadableDatabase();
         if (db == null) {
             setResult(RESULT_CANCELED, new Intent());
             finish();
             return;
         }
 
         byte[] mainPasswod = PasswordManager.getInstance(this).getDecryptedMainPassword();
         if (mainPasswod == null) {
             setResult(RESULT_CANCELED, new Intent());
             finish();
             return;
         }
 
         mDbRw = new DbRw(db, mainPasswod);
 
         Bundle extras = getIntent().getExtras();
         if (extras != null) {
             mId = extras.getLong(Const.COLUMN.ID);
             if (mId != null) {
                 setContentView(R.layout.edit);
                 mTitleEdit = setupEditText(R.id.title_textedit);
                 mUserIdEdit = setupEditText(R.id.user_id_edittext);
                 mPasswordEdit = setupEditText(R.id.password_edittext);
                 mMemoEdit = setupEditText(R.id.memo_edittext);
                 findViewById(R.id.copy_user_id_button).setOnClickListener(this);
                 findViewById(R.id.copy_passwword_button).setOnClickListener(this);
                 findViewById(R.id.copy_memo_button).setOnClickListener(this);
                 findViewById(R.id.ok_button).setOnClickListener(this);
                 findViewById(R.id.cancel_button).setOnClickListener(this);
                 dbToField();
             } else {
                 setResult(RESULT_CANCELED, new Intent());
                 finish();
             }
             
             switch (extras.getInt(Const.REQUEST_TYPE.NAME)) {
                 case Const.REQUEST_TYPE.EDIT:
                     mIsNewRow = false;
                     setTitle(R.string.app_name_edit);
                     findViewById(R.id.user_id_edittext).requestFocus();                    
                     break;
                 case Const.REQUEST_TYPE.NEW:
                     mIsNewRow = true;
                     setTitle(R.string.app_name_new);
                     findViewById(R.id.title_textedit).requestFocus();
                     break;
                 default:
                     // never come
                     finish();
             }
         }
     }
     
     @Override
     protected void onResume() {
         super.onResume();
         TimeOutChecker.getInstance().addObserver(this);
     }
 
     @Override
     protected void onPause() {
         TimeOutChecker.getInstance().deleteObserver(this);
         super.onPause();
     }
 
     @Override
     public void onDestroy() {
         App.debugLog(this, "onDestroy");
        if (mDbRw != null) {
            mDbRw.cleanup();
            mDbRw = null;
        }
         super.onDestroy();
     }
 
     @Override
     public void onBackPressed() {
         if (mDirty) {
             new AlertDialog.Builder(this)
                     .setTitle(getString(R.string.commit_change_q))
                     .setPositiveButton(android.R.string.yes,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int which) {
                                     EditActivity.this.fieldToDb();
                                     EditActivity.this.setResult(RESULT_OK, new Intent());
                                     EditActivity.this.finish();
                                 }
                             })
                     .setNegativeButton(android.R.string.no,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int which) {
                                     EditActivity.this.setResult(RESULT_CANCELED, new Intent());
                                     EditActivity.this.finish();
                                 }
                             })
                     .create().show();
         } else {
             setResult(RESULT_CANCELED, new Intent());
             finish();
         }
     }
 
     @Override
     public void onClick(View v) {
         TimeOutChecker.getInstance().onUser();
         switch (v.getId()) {
             case R.id.ok_button:
                 fieldToDb();
                 setResult(RESULT_OK, new Intent());
                 finish();
                 break;
             case R.id.cancel_button:
                 if (mIsNewRow) {
                     deleteItem();
                 }
                 setResult(RESULT_CANCELED, new Intent());
                 finish();
                 break;
             case R.id.copy_user_id_button:
                 mApp.copyTextToClipboard(mUserIdEdit.getText());
                 break;
             case R.id.copy_passwword_button:
                 mApp.copyTextToClipboard(mPasswordEdit.getText());
                 break;
             case R.id.copy_memo_button:
                 mApp.copyTextToClipboard(mMemoEdit.getText());
                 break;
             default:
                 // did not come
                 break;
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         TimeOutChecker.getInstance().onUser();
         getMenuInflater().inflate(R.menu.edit_main_menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete_menu_item:
                final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_this_item))
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                    EditActivity.this.deleteItem();
                                }
                        }).setNegativeButton(android.R.string.cancel, null)
                .create();
                alertDialog.show();
                return true;
         }
        return false;
     }
 
     private void deleteItem() {
         mDbRw.deleteById(mId);
         finish();
     }
     
     private EditText setupEditText(int id) {
         EditText et = (EditText) findViewById(id);
         et.addTextChangedListener(this);
         et.setOnFocusChangeListener(this);
         return et;
     }
     private void dbToField() {
         DbRw.Data data = mDbRw.getRecord(mId);
         mTitleEdit.setText(data.getTitle());
         mUserIdEdit.setText(data.getUserId());
         mPasswordEdit.setText(data.getPassword());
         mMemoEdit.setText(data.getMemo());
         mDirty = false;
     }
 
     private void fieldToDb() {
         DbRw.Data data = new DbRw.Data(
                 mTitleEdit.getText().toString(),
                 mUserIdEdit.getText().toString(),
                 mPasswordEdit.getText().toString(),
                 mMemoEdit.getText().toString());
         mDbRw.updateRecord(mId, data);
     }
 
     @Override
     public void update(Observable observable, Object data) {
         if (TimeOutChecker.getInstance().isTimeOut()) {
             setResult(RESULT_CANCELED, new Intent());
             finish();
         }        
     }
 
     @Override
     public void afterTextChanged(Editable s) {
         mDirty = true;
     }
 
     @Override
     public void beforeTextChanged(CharSequence s, int start, int count, int after) {
     }
 
     @Override
     public void onTextChanged(CharSequence s, int start, int before, int count) {
         TimeOutChecker.getInstance().onUser();
     }
 
     @Override
     public void onFocusChange(View v, boolean hasFocus) {
         TimeOutChecker.getInstance().onUser();
     }
 }
