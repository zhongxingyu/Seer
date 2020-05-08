 package com.SteveApp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Random;
 
 import com.SteveApp.R;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentProviderOperation;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.OperationApplicationException;
 import android.database.Cursor;
 import android.graphics.Rect;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.os.Message;
 import android.os.RemoteException;
 import android.provider.ContactsContract;
 import android.text.Html;
 import android.text.Spanned;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.AdapterView;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.AdapterView.OnItemClickListener;
 
 public class MainActivity extends Activity
 {
 	final Context context = this;
 	public final static String EXTRA_MESSAGE = "com.SteveApp.MESSAGE";
 	private int prg = 0;
 	private ArrayList<contactDuo> contacts;
 	private ContentResolver resolver;
 	private TextView tv;
 	private ProgressBar pb;
 	private String universal;
 	private ArrayList <String> namesList;
 	private String currentPage = "";
 
 	public void showDialog(String message, Context context)
 	{
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
 		// set dialog message
 		alertDialogBuilder.setMessage(message).setCancelable(false)
 				.setPositiveButton("Ok", new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						dialog.cancel();
 					}
 				});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	
 	public void confirmationDialog(String message, Context context, final String chosenList)
 	{
 		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
 
 		// set dialog message
 		alertDialogBuilder.setMessage(Html.fromHtml(message)).setCancelable(false)
 				.setPositiveButton("Yes", new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int id)
 					{
 						dialog.cancel();
 						changeContacts(Presets.getList(chosenList));
 					}
 				}).setNegativeButton("No", new DialogInterface.OnClickListener()
 				{
 					public void onClick(final DialogInterface dialog, int id)
 					{
 						dialog.dismiss();
 					}
 				});
 
 		// create alert dialog
 		AlertDialog alertDialog = alertDialogBuilder.create();
 
 		// show it
 		alertDialog.show();
 	}
 	
 	public boolean killKeyboard()
 	{
 		View view = this.getWindow().getDecorView().findViewById(android.R.id.content);
 		Rect r = new Rect();
 		view.getWindowVisibleDisplayFrame(r);
 
 		int heightDiff = view.getRootView().getHeight() - (r.bottom - r.top);
 		if (heightDiff > 100)
 		{
 			InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
 			inputManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		}
 		return false;
 	}
 	
 	public void createBackup(String contactInfo)
 	{
 		// Write pristine contact info to file for future reversal
 		// Try external first
 		if (Util.isExternalStorageWritable())
 		{
 			File file = new File(Environment.getExternalStorageDirectory().getPath() + "/SteveApp/",
 					"backup_contacts.txt");
 			if (!file.exists())
 			{
 				try
 				{
 					FileWriter fWriter = new FileWriter(Environment.getExternalStorageDirectory()
 							.getPath() + "/SteveApp/backup_contacts.txt");
 					fWriter.write(contactInfo);
 					fWriter.close();
 				}
 				catch (Exception e)
 				{
 					showDialog("Creating a backup file in external memory failed.", context);
 					setContentView(R.layout.activity_main);
 					return;
 				}
 			}
 		}
 		// Now internal
 		else
 		{
 			String ContactLists = "Contact_Lists";
 			FileOutputStream fos;
 			try
 			{
 				File file = getBaseContext().getFileStreamPath("Contact_Lists");
 				if (!file.exists())
 				{
 					fos = openFileOutput(ContactLists, Context.MODE_PRIVATE);
 					fos.write(contactInfo.getBytes());
 				}
 			}
 			catch (FileNotFoundException e)
 			{
 				showDialog("Backup file creation failed.", context);
 				setContentView(R.layout.activity_main);
 				return;
 			}
 			catch (IOException e)
 			{
 				showDialog("Reading from backup file failed.", context);
 				setContentView(R.layout.activity_main);
 				return;
 			}
 		}	
 	}
 	
 	public void changeContacts (final ArrayList<String> targets)
 	{
 		killKeyboard();
 		getActionBar().setDisplayHomeAsUpEnabled(false);
 		setContentView(R.layout.activity_display_message);
 
 		pb = (ProgressBar) findViewById(R.id.pbId);
 		tv = (TextView) findViewById(R.id.tvId);
 
 		String contactInfo = "";
 
 		resolver = getContentResolver();
 		final String[] projection = {ContactsContract.RawContacts._ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY};
 
 		Cursor cc = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection,
 				ContactsContract.RawContacts._ID + " > 0", null, null);
 
 		contacts = new ArrayList<contactDuo>();
 
 		int nameFieldColumnIndex1;
 		int nameFieldColumnIndex2;
 		while (cc.moveToNext())
 		{
 			nameFieldColumnIndex1 = cc.getColumnIndex(ContactsContract.RawContacts._ID);
 			nameFieldColumnIndex2 = cc
 					.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
 			if (!cc.getString(nameFieldColumnIndex2).equals("null"))
 			{
 				contacts.add(new contactDuo(cc.getString(nameFieldColumnIndex1),
 								cc.getString(nameFieldColumnIndex2)));
 			}
 		}
 		cc.close();
 
 		for (int i = 0; i < contacts.size(); i++)
 		{
 			contactInfo += contacts.get(i).id;
 			contactInfo += ":";
 			contactInfo += contacts.get(i).display_name;
 			contactInfo += "|";
 		}
 		pb.setMax(contacts.size());
 		createBackup(contactInfo);
 		
 		Runnable myThread = new Runnable()
 		{
 			@SuppressLint("InlinedApi")
 			@Override
 			public void run()
 			{
 				// Reset progress from older runs
 				if (prg != 0)
 				{
 					prg = 0;
 				}
 				// This loop changes all of the contacts
 				for (int i = 0; i < contacts.size(); i++)
 				{
 					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
 					// CONTACT UPDATE DONE HERE
 					ops.add(ContentProviderOperation
 							.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
 							.withSelection(ContactsContract.RawContacts._ID + " LIKE ?",
 									new String[] {contacts.get(i).id})
 							.withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
 									Util.randomString(targets)).build());
 
 					try
 					{
 						resolver.applyBatch(ContactsContract.AUTHORITY, ops);
 						hnd.sendMessage(hnd.obtainMessage());
 					}
 					catch (RemoteException e)
 					{
 						showDialog("An error occured during contacts changing.", context);
 						setContentView(R.layout.activity_main);
 						return;
 					}
 					catch (OperationApplicationException e)
 					{
 						showDialog("An error occured during contacts changing.", context);
 						setContentView(R.layout.activity_main);
 						return;
 					}
 				}
 
 				runOnUiThread(new Runnable()
 				{
 					public void run()
 					{
 						tv.setText("All contacts successfully changed.");
 						showDialog("All contacts successfully changed.", context);
 						setContentView(R.layout.activity_main);
 					};
 				});
 			}
 
 			@SuppressLint("HandlerLeak")
 			Handler hnd = new Handler()
 			{
 				@Override
 				public void handleMessage(Message msg)
 				{
 					prg++;
 					pb.setProgress(prg);
 					
 					String perc = String.valueOf(prg).toString();
 					tv.setText(perc + "/" + String.valueOf(contacts.size())
 							+ " contacts changed.");
 				}
 			};
 		};
 		new Thread(myThread).start();
 	}
 	
 	@SuppressLint("InlinedApi")
 	public void goToGrabBag(final View v)
 	{
		killKeyboard();
 		currentPage = "Grab Page";
 		setContentView(R.layout.combo);
 		getActionBar().setDisplayHomeAsUpEnabled(true);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 	}
 	
 	@SuppressLint("InlinedApi")
 	public void grabChange(final View v)
 	{
 		String name1 = ((EditText) findViewById(R.id.edit_message1)).getText().toString();
 		String name2 = ((EditText) findViewById(R.id.edit_message2)).getText().toString();
 		String name3 = ((EditText) findViewById(R.id.edit_message3)).getText().toString();
 		String name4 = ((EditText) findViewById(R.id.edit_message4)).getText().toString();
 		String name5 = ((EditText) findViewById(R.id.edit_message5)).getText().toString();
 		universal = name1 + name2 + name3 + name4 + name5;
 		if (universal.trim().equals(""))
 		{
 			showDialog("Please enter at least one name.", context);
 		}
 		else
 		{
 			ArrayList<String> targets = new ArrayList<String>();
 			if (!name1.trim().equals(""))
 			{
 				targets.add(name1);
 			}
 			if (!name2.trim().equals(""))
 			{
 				targets.add(name2);
 			}
 			if (!name3.trim().equals(""))
 			{
 				targets.add(name3);
 			}
 			if (!name4.trim().equals(""))
 			{
 				targets.add(name4);
 			}
 			if (!name5.trim().equals(""))
 			{
 				targets.add(name5);
 			}
 			changeContacts(targets);
 		}
 	}
 	
 	@SuppressLint("InlinedApi")
 	public void goToSuggestions(final View v)
 	{
 		currentPage = "Suggestions Page";
 		killKeyboard();
 		setContentView(R.layout.suggestions);
 		
 		final ListView listview = (ListView) findViewById(R.id.presetList);
 
 		// clear previous results in the LV
 		listview.setAdapter(null);
 		ArrayList<String> presetList = new ArrayList<String>(Arrays.asList(Presets.precons));
 		ArrayList<Spanned> presetListFinal = new ArrayList<Spanned>();
 		for (int i = 0; i < presetList.size(); i++)
 		{
 			presetListFinal.add(Html.fromHtml(presetList.get(i)));
 		}
 		
 		PresetAdapter lvAdapter = new PresetAdapter(context, presetListFinal);
 		listview.setAdapter(lvAdapter);
 		
 		listview.setOnItemClickListener(new OnItemClickListener()
 		{
 			@Override
 			public void onItemClick(AdapterView<?> parent, final View view, int position, long id) throws IllegalArgumentException, IllegalStateException
 			{
 				String chosenList = ((Spanned) parent.getItemAtPosition(position)).toString();
 				String confirmationMessage = "Are you sure that you want to change each of the contacts on this phone "
 											 + " to a randomly selected member of the <b>" + chosenList + "</b>?";
 				confirmationDialog(confirmationMessage, context, chosenList);
 			}
 		});
 		
 		TextView list_message = (TextView) findViewById(R.id.message);
 		String stats = "Here are the " + presetListFinal.size() + " preset lists that this app currently has. " + 
 					   "Simply click on one of them in order to use it as your \"grab bag\".";
 		list_message.setText(Html.fromHtml(stats));
 	}
 
 	@SuppressLint("InlinedApi")
 	public void onChangeClick(final View v)
 	{
		killKeyboard();
 		EditText editText = (EditText) findViewById(R.id.edit_message);
 		universal = editText.getText().toString();
 		if (universal.trim().equals(""))
 		{
 			showDialog("Please enter something to change all of this phone's contacts into.", context);
 		}
 		else
 		{
 			ArrayList<String> targets = new ArrayList<String>();
 			targets.add(universal);
 			changeContacts(targets);
 		}
 	}
 
 	@SuppressWarnings("resource")
 	public void onUndoClick(View arg0)
 	{
		killKeyboard();
 		setContentView(R.layout.activity_display_message);
 		pb = (ProgressBar) findViewById(R.id.pbId);
 		tv = (TextView) findViewById(R.id.tvId);
 
 		String contactString = "";
 		Boolean done = false;
 
 		// Try to read from external storage first
 		if (Util.isExternalStorageWritable())
 		{
 			File extFile = new File(Environment.getExternalStorageDirectory().getPath()
 									+ "/SteveApp/","backup_contacts.txt");
 			// If file exists in external storage, grab its contents
 			if (extFile.exists())
 			{
 				BufferedReader br = null;
 				try
 				{
 					String sCurrentLine;
 					br = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory().getPath()
 							+ "/SteveApp/backup_contacts.txt"));
 					while ((sCurrentLine = br.readLine()) != null)
 					{
 						contactString += sCurrentLine;
 					}
 					// Set a boolean so we don't look around in internal storage
 					done = true;
 
 					// Delete the file
 					extFile.delete();
 				}
 				catch (IOException e)
 				{
 					showDialog("Error reading from external storage.", context);
 					setContentView(R.layout.activity_main);
 					return;
 				}
 			}
 		}
 		// Now try to read from internal storage
 		if (!done)
 		{
 			try
 			{
 				FileInputStream test = openFileInput("Contact_Lists");
 				byte[] bytes = new byte[1];
 				while (test.read(bytes, 0, 1) != -1)
 				{
 					contactString += (char) bytes[0];
 					bytes = new byte[1];
 				}
 				test.close();
 
 				// Delete reversion file
 				File dir = getFilesDir();
 				File file = new File(dir, "Contact_Lists");
 				file.delete();
 			}
 			catch (FileNotFoundException e)
 			{
 				showDialog("No need to undo changes. Contacts list is pristine.", context);
 				setContentView(R.layout.activity_main);
 				return;
 			}
 			catch (IOException e)
 			{
 				showDialog("Reading from internal storage failed.", context);
 				setContentView(R.layout.activity_main);
 				return;
 			}
 		}
 
 		// Create ArrayList of contactDuos to populate
 		contacts = new ArrayList<contactDuo>();
 
 		// Now contactString is a string that contains the entire contact list 'encoded'
 		// Parse its contents into contacts
 		for (int i = 0; i < contactString.length(); i++)
 		{
 			String id = "";
 			while (contactString.charAt(i) != ':')
 			{
 				id += contactString.charAt(i);
 				i++;
 			}
 			i++;
 
 			String name = "";
 			while (contactString.charAt(i) != '|')
 			{
 				name += contactString.charAt(i);
 				i++;
 			}
 			contacts.add(new contactDuo(id, name));
 		}
 		pb.setMax(contacts.size());
 
 		resolver = getContentResolver();
 
 		Runnable myThread = new Runnable()
 		{
 			@SuppressLint("InlinedApi")
 			@Override
 			public void run()
 			{
 				if (prg != 0)
 				{
 					prg = 0;
 				}
 
 				// 'Repair' contact list
 				for (int i = 0; i < contacts.size(); i++)
 				{
 					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
 
 					// CONTACT UPDATE DONE HERE
 					ops.add(ContentProviderOperation
 							.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
 							.withSelection(ContactsContract.RawContacts._ID + " LIKE ?",
 									new String[] { contacts.get(i).id })
 							.withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
 									contacts.get(i).display_name).build());
 
 					try
 					{
 						resolver.applyBatch(ContactsContract.AUTHORITY, ops);
 						hnd.sendMessage(hnd.obtainMessage());
 					}
 					catch (RemoteException e)
 					{
 						tv.setText("Reverting contacts failed.");
 						showDialog("Reverting contacts failed.", context);
 						setContentView(R.layout.activity_main);
 						return;
 					}
 					catch (OperationApplicationException e)
 					{
 						tv.setText("Reverting contacts failed.");
 						showDialog("Reverting contacts failed.", context);
 						setContentView(R.layout.activity_main);
 						return;
 					}
 				}
 
 				runOnUiThread(new Runnable()
 				{
 					public void run()
 					{
 						tv.setText("Contacts list repaired.");
 						showDialog("Contacts list repaired.", context);
 						setContentView(R.layout.activity_main);
 					}
 				});
 			}
 
 			@SuppressLint("HandlerLeak")
 			Handler hnd = new Handler()
 			{
 				@Override
 				public void handleMessage(Message msg)
 				{
 					prg++;
 					pb.setProgress(prg);
 
 					String perc = String.valueOf(prg).toString();
 					tv.setText(perc + "/" + String.valueOf(contacts.size()) + " contacts repaired.");
 				}
 			};
 		};
 		new Thread(myThread).start();
 	}
 	
 	@SuppressLint("InlinedApi")
 	public void onScrambleClick(final View v)
 	{
		killKeyboard();
 		setContentView(R.layout.activity_display_message);
 
 		pb = (ProgressBar) findViewById(R.id.pbId);
 		tv = (TextView) findViewById(R.id.tvId);
 
 		// Long delimited, formatted string that acts as a 'backup'
 		String contactInfo = "";
 
 		resolver = getContentResolver();
 		final String[] projection = {ContactsContract.RawContacts._ID, ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY };
 
 		Cursor cc = resolver.query(ContactsContract.RawContacts.CONTENT_URI, projection,
 				ContactsContract.RawContacts._ID + " > 0", null, null);
 
 		contacts = new ArrayList<contactDuo>();
 
 		int nameFieldColumnIndex1;
 		int nameFieldColumnIndex2;
 		while (cc.moveToNext())
 		{
 			nameFieldColumnIndex1 = cc.getColumnIndex(ContactsContract.RawContacts._ID);
 			nameFieldColumnIndex2 = cc
 					.getColumnIndex(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY);
 			if (!cc.getString(nameFieldColumnIndex2).equals("null"))
 			{
 				contacts.add(new contactDuo(cc.getString(nameFieldColumnIndex1), cc
 						.getString(nameFieldColumnIndex2)));
 			}
 		}
 		cc.close();
 
 		for (int i = 0; i < contacts.size(); i++)
 		{
 			contactInfo += contacts.get(i).id;
 			contactInfo += ":";
 			contactInfo += contacts.get(i).display_name;
 			contactInfo += "|";
 		}
 		pb.setMax(contacts.size());
 
 		createBackup(contactInfo);
 
 		namesList = new ArrayList<String>();
 		// Create an arrayList of strings from the array list of contactDuos
 		for (int i = 0; i < contacts.size(); i++)
 		{
 			namesList.add(contacts.get(i).display_name);
 		}
 
 		Runnable myThread = new Runnable()
 		{
 			@SuppressLint("InlinedApi")
 			@Override
 			public void run()
 			{
 				// Reset progress from older runs
 				if (prg != 0)
 				{
 					prg = 0;
 				}
 				// This loop changes all of the contacts
 				for (int i = 0; i < contacts.size(); i++)
 				{
 					ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
 					// Grab a random contact here
 					Random numGen = new Random();
 					int randNum = numGen.nextInt(namesList.size());
 
 					String randomContact = namesList.get(randNum);
 					namesList.remove(randNum);
 
 					// CONTACT UPDATE DONE HERE
 					ops.add(ContentProviderOperation
 							.newUpdate(ContactsContract.RawContacts.CONTENT_URI)
 							.withSelection(ContactsContract.RawContacts._ID + " LIKE ?",
 									new String[] { contacts.get(i).id })
 							.withValue(ContactsContract.RawContacts.DISPLAY_NAME_PRIMARY,
 									randomContact).build());
 
 					try
 					{
 						resolver.applyBatch(ContactsContract.AUTHORITY, ops);
 						hnd.sendMessage(hnd.obtainMessage());
 					}
 					catch (RemoteException e)
 					{
 						showDialog("An error occured during contacts changing.", context);
 						setContentView(R.layout.activity_main);
 						return;
 					}
 					catch (OperationApplicationException e)
 					{
 						showDialog("An error occured during contacts changing.", context);
 						setContentView(R.layout.activity_main);
 						return;
 					}
 				}
 
 				runOnUiThread(new Runnable()
 				{
 					public void run()
 					{
 						tv.setText("All contacts successfully scrambled.");
 						showDialog("All contacts successfully scrambled.", context);
 						setContentView(R.layout.activity_main);
 					};
 				});
 			}
 
 			@SuppressLint("HandlerLeak")
 			Handler hnd = new Handler()
 			{
 				@Override
 				public void handleMessage(Message msg)
 				{
 					prg++;
 					pb.setProgress(prg);
 
 					String perc = String.valueOf(prg).toString();
 					tv.setText(perc + "/" + String.valueOf(contacts.size()) + " contacts scrambled.");
 				}
 			};
 		};
 		new Thread(myThread).start();
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		// Create folder in external storage for us to store things in
 		// Check if SD card is mounted
 		if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
 		{
 			File Dir = new File(android.os.Environment.getExternalStorageDirectory(), "SteveApp");
 			if (!Dir.exists()) // if directory is not here
 			{
 				Dir.mkdirs(); // make directory
 			}
 		}
 		setContentView(R.layout.activity_main);
 		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 	
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		switch (item.getItemId())
 		{
 			// I use the home button as a derpy back button
 			case android.R.id.home:
 				killKeyboard();
 				if (currentPage.equals("Grab Page"))
 				{
 					currentPage = "";
 					setContentView(R.layout.activity_main);
 					getActionBar().setDisplayHomeAsUpEnabled(false);
 				}
 				else if (currentPage.equals("Suggestions Page"))
 				{
 					currentPage = "Grab Page";
 					setContentView(R.layout.combo);
 				}	
 				break;
 			default:
 				break;
 		}
 		return super.onOptionsItemSelected(item);
 	}
 }
