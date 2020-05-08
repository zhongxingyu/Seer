 package com.example.git;
 
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Security;

import android.os.Bundle;
import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
 import android.text.InputType;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.spongycastle.jce.provider.BouncyCastleProvider;

 import com.jcraft.jsch.JSch;
 import com.jcraft.jsch.JSchException;
 import com.jcraft.jsch.KeyPair;
 
import com.example.git.SqlLiteDatabaseHelper;
 
 public class MainActivity extends Activity {
   
   static {
   	BouncyCastleProvider provider = new BouncyCastleProvider();
   	Security.insertProviderAt(provider, 1);
   }
   
 	String selectedPath = "";
	String TAG = getClass().getName();
 	
 	//TODO select via intent if only files, dirs or both are allowed to select
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
               
         Button button_list_repos = (Button) findViewById(R.id.button_list_repos);
         Button button_init_repo = (Button) findViewById(R.id.button_init_repo);
         Button button_clone = (Button) findViewById(R.id.button_clone);
         Button button_genKeys = (Button) findViewById(R.id.button_genKeys);
         
         button_list_repos.setOnClickListener(new View.OnClickListener() {
       		public void onClick(View v) {
       			Intent intent = new Intent(MainActivity.this, RepositoryListActivity.class);
       				startActivity(intent);
       			}
         });
         
         button_genKeys.setOnClickListener(new View.OnClickListener() {
       		public void onClick(View v) {
       			SharedPreferences settings = getSharedPreferences(MainActivity.this.getResources().getString(R.string.APPSETTINGS), 0);
       			String privateKeyFilenameWithPath = settings.getString(MainActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
      			String publicKeyFilenameWithPath = settings.getString(MainActivity.this.getResources().getString(R.string.SSHPRIVATEKEYPATHSETTING), "");
       			
 						boolean privateKey = new File(privateKeyFilenameWithPath).isFile();
 						boolean publicKey = new File(publicKeyFilenameWithPath).isFile();
 						if (!privateKey && !publicKey) {
 							AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);                 
 							alert.setTitle("Enter pw");  
 							alert.setMessage("pw");                
 
 							final EditText input = new EditText(MainActivity.this); 
 							input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
 							alert.setView(input);
 
 							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {  
 								public void onClick(DialogInterface dialog, int whichButton) {
 									Log.d(TAG,"generate Keys");				
   								if (generateKeyPair("RSA", Environment.getExternalStorageDirectory() + "/.ssh/", "id_rsa", "",input.getText().toString())) {
   									ToastNotification.makeToast("Was able to gen keys : )", Toast.LENGTH_LONG, MainActivity.this);
   								} else {
   									ToastNotification.makeToast("Wasn't able to gen keys : (", Toast.LENGTH_LONG, MainActivity.this);
   								}									
 								}
 							});
       			
 							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 								public void onClick(DialogInterface dialog, int which) {
 									// TODO Auto-generated method stub
 									return;   
 								}
 							});
 							alert.show();
 						}
 						else {
 							ToastNotification.makeToast("There are keys already. Nothing to do here.", Toast.LENGTH_LONG, MainActivity.this);
 						}
       		}
       	});
         
         
         button_clone.setOnClickListener(new View.OnClickListener(){
       		public void onClick(View v) {
       			Intent intent = new Intent(MainActivity.this, CloneRepositoryActivity.class);
     				startActivity(intent);
   				}
         });
         
         button_init_repo.setOnClickListener(new View.OnClickListener(){
         		public void onClick(View v) {
         			Intent intent = new Intent(MainActivity.this, InitRepositoryActivity.class);
       				startActivity(intent);
         			}
         });
     }   
     
 		/**
 		 * 
 		 */
 		protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 
 			if (requestCode == 1) {
 			     if(resultCode == RESULT_OK) {
 			    	 selectedPath = data.getStringExtra("currentPath");
 			    	 ToastNotification.makeToast("Directory " + selectedPath.toString() + " selected!", Toast.LENGTH_LONG, MainActivity.this);	     					      
 			     }
 
 			     if (resultCode == RESULT_CANCELED) {
 			    	 //Write your code on no result return 
 			     }
 			}
 		}
 		  
    @Override
    /**
     * 
     */
    public boolean onCreateOptionsMenu(Menu menu) {
   	 getMenuInflater().inflate(R.menu.activity_main, menu);
   	 return true;
    }  
  	
    @Override
    /**
     * 
     */
    public boolean onOptionsItemSelected (MenuItem item) {
   	 Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
   	 startActivity(intent);
      return true;
    }
    
  	/**
  	 * 
  	 * @param keyType
  	 * @param absolutePath
  	 * @param filename
  	 * @param comment
  	 * @param password
  	 */
  	private boolean generateKeyPair(String keyType, String absolutePath, String filename, String comment, String password) {
  		boolean success = false;
  	 int type = 0;
  		if (keyType.equals("DSA")) {
  			type = KeyPair.DSA;
  		}
  		else {
  			type = KeyPair.RSA;
  		}
  	  JSch jsch = new JSch();
  		KeyPair kpair; 		
  		try {
  			kpair = KeyPair.genKeyPair(jsch, type);
  			kpair.setPassphrase(password);
 	    kpair.writePrivateKey(absolutePath + filename);
 	    kpair.writePublicKey(absolutePath + filename + ".pub", comment);
 	 		Log.d(TAG,"Finger print: " + kpair.getFingerPrint());
 	 		kpair.dispose();
 	 		success = true;
     } catch (JSchException e) {
     	Log.e(TAG, "JSchException gen keys");
 	    e.printStackTrace();
     } catch (FileNotFoundException e) {
    		Log.e(TAG, "FileNotFoundException gen keys");
 	    e.printStackTrace();
     } catch (IOException e) {
     	Log.e(TAG, "IOException gen key");
 	    e.printStackTrace();
     }
  	 	return success;
  	}
 }
