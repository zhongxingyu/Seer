 /*	GenesisChess, an Android chess application
 	Copyright 2012, Justin Madru (justin.jdm64@gmail.com)
 
 	Licensed under the Apache License, Version 2.0 (the "License");
 	you may not use this file except in compliance with the License.
 	You may obtain a copy of the License at
 
 	http://apache.org/licenses/LICENSE-2.0
 
 	Unless required by applicable law or agreed to in writing, software
 	distributed under the License is distributed on an "AS IS" BASIS,
 	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 	See the License for the specific language governing permissions and
 	limitations under the License.
 */
 
 package com.chess.genesis;
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.EditText;
 
 class StatsLookupDialog extends BaseDialog implements OnClickListener
 {
 	public final static int MSG = 123;
 
 	private final Handler handle;
 
 	public StatsLookupDialog(final Context context, final Handler handler)
 	{
 		super(context);
 
 		handle = handler;
 	}
 
 	@Override
 	public void onCreate(final Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);

 		setTitle("User Stats Lookup");
 		setBodyView(R.layout.dialog_statslookup);
 		setButtonTxt(R.id.ok, "Lookup");
 	}
 
 	public void onClick(final View v)
 	{
 		if (v.getId() == R.id.ok) {
 			final EditText txt = (EditText) findViewById(R.id.username);
 			final String username = txt.getText().toString().trim();
 
 			if (username.length() >= 3)
 				handle.sendMessage(handle.obtainMessage(MSG, username));
 		}
 		dismiss();
 	}
 }
