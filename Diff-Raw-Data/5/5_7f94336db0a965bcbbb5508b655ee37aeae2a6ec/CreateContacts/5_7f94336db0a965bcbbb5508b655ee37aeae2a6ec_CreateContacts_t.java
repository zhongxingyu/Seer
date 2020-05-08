 package com.capgemini.igor;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.provider.ContactsContract.RawContacts;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 import com.capgemini.igor.pusher.ContactPusher;
 
 public class CreateContacts extends Activity {
 	private static final String logTag = "Igor:CreateContacts";
 
 	private Thread createContactsThread;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.main);
 
 		setUpButtons();
 	}
 
 	private void setUpButtons() {
 		Button createButton = (Button) findViewById(R.id.CreateButton);
 		createButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				createContacts();
 			}
 		});
 
 		Button deleteButton = (Button) findViewById(R.id.DeleteButton);
 		deleteButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				deleteAllContacts();
 			}
 		});
 
 		Button exitButton = (Button) findViewById(R.id.ExitButton);
 		exitButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				exit();
 			}
 		});
 	}
 
 	private void createContacts() {
 		if (createContactsThread != null && createContactsThread.isAlive()) {
 			createContactsThread.interrupt();
 		}
 		createContactsThread = new Thread(new ContactPusher(getContentResolver()));
 		createContactsThread.start();
 	}
 
 	private void deleteAllContacts() {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
				int numberOfContactsMarkedForDeletion = getContentResolver().delete(RawContacts.CONTENT_URI, null, null);

				Log.d(logTag, "Marked " + numberOfContactsMarkedForDeletion + " raw contacts for deletion");
 			}
 		}).start();
 	}
 
 	private void exit() {
 		if (createContactsThread != null && createContactsThread.isAlive()) {
 			try {
 				createContactsThread.join(5000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 
 		this.finish();
 	}
 
 }
