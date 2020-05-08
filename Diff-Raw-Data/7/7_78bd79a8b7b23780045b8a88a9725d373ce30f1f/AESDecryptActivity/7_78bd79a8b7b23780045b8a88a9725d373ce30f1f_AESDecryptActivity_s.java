 package com.yeyaxi.SecureChat;
 
 import java.security.SecureRandom;
 
 import javax.crypto.Cipher;
 import javax.crypto.KeyGenerator;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.telephony.SmsMessage;
 import android.util.Base64;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class AESDecryptActivity extends Activity{
 	
 	public EditText CipherText;
 	private EditText SecretText;
 	private TextView PlainMessage;
 	private Button Decrypt;
 	
 	public void onCreate(Bundle icicle){
 		super.onCreate(icicle);
 		setContentView(R.layout.aesdecrypt);
 		
 		CipherText = (EditText) findViewById(R.id.editText1);
 		SecretText = (EditText) findViewById(R.id.editText2);
 		PlainMessage = (TextView) findViewById(R.id.textView3);
 		Decrypt = (Button) findViewById(R.id.button1);
 	}
 	public void onStart() {
 		super.onStart();
 		Decrypt.setOnClickListener(new OnClickListener() {
 			public void onClick (View view) {
 				AES aes = new AES();
 				try {
 					String cipherText = aes.AESDecrypt(SecretText.getText().toString(), CipherText.getText().toString());
 					PlainMessage.setText(cipherText);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			}
 		});		
 	}
 	//For receiving SMS (Value passed from SMSReceiver)
 	public void onResume() {
 		super.onResume();
 		Bundle bundleReceiver = getIntent().getExtras();
		String msg = bundleReceiver.getString("SMS");
		CipherText.setText(msg);
 	}
 
 	public class AES implements AESInterface {
 
 		
 		@Override
 		public String AESEncrypt(String sKey, String PlainMsg)
 				throws Exception {
 			/**
 			 * For Full Implementation,
 			 * @see AESEncryptActivity
 			 */
 			return null;
 		}
 
 		@Override
 		public String AESDecrypt(String sKey, String EncryptMsg)
 				throws Exception {			
 			byte[] rawKey = getRawKey(sKey.getBytes("UTF-8"));
 			//byte[] rawKey = getRawKey(sKey.getBytes());
 			SecretKeySpec keySpec = new SecretKeySpec(rawKey, "AES");
 			Cipher cipher = Cipher.getInstance("AES");
 			cipher.init(Cipher.DECRYPT_MODE, keySpec);
 			//byte[] plainText = Base64Decoded(EncryptMsg.getBytes("UTF-8"));
 			byte[] plainText = Base64Decoded(EncryptMsg);			
 			cipher.doFinal(plainText);
 			return new String(plainText, "UTF-8");
 		}
 
 		@Override
 		public byte[] getRawKey(byte[] seed) throws Exception {
 			//Initialize the KeyGenerator
 			KeyGenerator kgen = KeyGenerator.getInstance("AES");
 			SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
 			sr.setSeed(seed);
 			//Init for 256bit AES key
 			kgen.init(Constants.AES_KEY_SIZE, sr);
 			SecretKey secret = kgen.generateKey();
 			//Get secret raw key
 			byte[] rawKey = secret.getEncoded();
 			return rawKey;
 		}
 /**
  * 
  * @param toBeDecoded
  * @return
  */
 		public byte[] Base64Decoded(String toBeDecoded) {
 			byte[] decoded = Base64.decode(toBeDecoded, 0);
 			return decoded;
 		}
 		
 
 		
 	}
 	
 
 }
 
