 package ca.jdoda.pwgen;
 
 /* Copyright 2011 Jonathan Doda
 
  This file is part of pwgen.
 
  pwgen is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License, version 3,
  as published by the Free Software Foundation.
 
  pwgen is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with pwgen. If not, see <http://www.gnu.org/licenses/>.
  */
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 
 public class PWGen extends Activity {
 	private final int PASS_LENGTH = 12;
 	private final int ROUNDS = 1000;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		final EditText passphraseEditText = (EditText) findViewById(R.id.passphraseEditText);
 		final EditText confirmEditText = (EditText) findViewById(R.id.confirmEditText);
 		final EditText nicknameEditText = (EditText) findViewById(R.id.nicknameEditText);
 		final EditText passwordEditText = (EditText) findViewById(R.id.passwordEditText);
 
 		nicknameEditText.setOnEditorActionListener(new OnEditorActionListener() {
 			@Override
 			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 				Button generateButton = (Button) findViewById(R.id.generateButton);
 				generateButton.performClick();
 				return true;
 			}
 		});
 
 		Button generateButton = (Button) findViewById(R.id.generateButton);
 		generateButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String passphrase = passphraseEditText.getText().toString();
 				String confirm = confirmEditText.getText().toString();
 				String nickname = nicknameEditText.getText().toString();
 
 				if (passphrase.equals(confirm)) {
 					try {
 						byte[] hash = (passphrase + nickname).getBytes("UTF-8");
 						MessageDigest digester = MessageDigest
 								.getInstance("SHA256");
 						for (int i = 0; i < ROUNDS; i++) {
 							hash = digester.digest(hash);
 						}
 						String password = Base64.encodeBytes(hash,
 								Base64.URL_SAFE);
 						passwordEditText.setText(password.subSequence(0,
 								PASS_LENGTH));
					} catch (NoSuchAlgorithmException e) {
						passwordEditText.setText("Crap");
					} catch (UnsupportedEncodingException e) {
						passwordEditText.setText("Double Crap");
					} catch (IOException e) {
						passwordEditText.setText("Triple Crap");
 					}
 				} else {
 					passwordEditText.setText("Passphrases don't match");
 				}
 			}
 		});
 
 		Button clearButton = (Button) findViewById(R.id.clearButton);
 		clearButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				passphraseEditText.getText().clear();
 				confirmEditText.getText().clear();
 				nicknameEditText.getText().clear();
 				passwordEditText.getText().clear();
 			}
 		});
 	}
 }
