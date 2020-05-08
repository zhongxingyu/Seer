 /**
  * Copyright 2011 Tag Spilman
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
 
 package com.johnzeringue.doubleslash;
 
 import android.app.Activity;
 import android.inputmethodservice.Keyboard;
 import android.inputmethodservice.KeyboardView;
 import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnKeyListener;
 import android.view.WindowManager;
 import android.widget.EditText;
 
 public class DemoActivity extends Activity implements OnKeyListener,
         OnKeyboardActionListener {
 
     private static final String TAG = DemoActivity.class.getName();
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.demo);
 
         KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboardView);
         Keyboard keyboard = new Keyboard(this, R.layout.querty);
         keyboardView.setKeyboard(keyboard);
         keyboardView.setEnabled(true);
         keyboardView.setPreviewEnabled(true);
         keyboardView.setOnKeyListener(this);
         keyboardView.setOnKeyboardActionListener(this);
 
         EditText editText = (EditText) findViewById(R.id.editText);
         editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean b) {
                 setKeyboardVisibility(b);
             }
         });
         editText.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                 toggleKeyboardVisibility();
             }
         });
        keyboardView.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent keyEvent) {
                EditText editText = (EditText) getCurrentFocus();
                editText.setText(editText.getText() + String.valueOf((char) i));
                return true;
            }
        });
     }
 
     private void setKeyboardVisibility(boolean isVisible) {
         KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboardView);
         if (isVisible) {
             keyboardView.setVisibility(View.VISIBLE);
         } else {
             keyboardView.setVisibility(View.INVISIBLE);
         }
     }
 
     private void toggleKeyboardVisibility() {
         KeyboardView keyboardView = (KeyboardView) findViewById(R.id.keyboardView);
         int visibility = keyboardView.getVisibility();
         switch (visibility) {
             case View.VISIBLE:
                 keyboardView.setVisibility(View.INVISIBLE);
                 break;
             case View.GONE:
             case View.INVISIBLE:
                 keyboardView.setVisibility(View.VISIBLE);
                 break;
         }
     }
 
     @Override
     public boolean onKey(View v, int keyCode, KeyEvent event) {
         Log.d(TAG, "onKey? keyCode=" + keyCode);
         return false;
     }
 
     @Override
     public void swipeUp() {
         Log.d(TAG, "swipeUp");
     }
 
     @Override
     public void swipeRight() {
         Log.d(TAG, "swipeRight");
     }
 
     @Override
     public void swipeLeft() {
         Log.d(TAG, "swipeLeft");
     }
 
     @Override
     public void swipeDown() {
         Log.d(TAG, "swipeDown");
     }
 
     @Override
     public void onText(CharSequence text) {
         Log.d(TAG, "onText? \"" + text + "\"");
     }
 
     @Override
     public void onRelease(int primaryCode) {
         Log.d(TAG, "onRelease? primaryCode=" + primaryCode);
     }
 
     @Override
     public void onPress(int primaryCode) {
         Log.d(TAG, "onPress? primaryCode=" + primaryCode);
     }
 
     @Override
     public void onKey(int primaryCode, int[] keyCodes) {
         Log.d(TAG, "onKey? primaryCode=" + primaryCode);
         int n1 = 0; // -1 count
         for (int keyCode : keyCodes) {
             if (keyCode == -1) {
                 n1++;
                 continue;
             }
             Log.v(TAG, "keyCode=" + keyCode);
         }
         Log.v(TAG, "keyCode=-1 *" + n1);
     }
 
 }
