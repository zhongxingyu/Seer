 package de.tum.in.tumcampus;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.database.MatrixCursor;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.EditText;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 import de.tum.in.tumcampus.models.TransportManager;
 
 public class Transports extends Activity implements OnItemClickListener,
 		OnItemLongClickListener, OnEditorActionListener {
 
 	private boolean connected() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		if (getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
 			setContentView(R.layout.transports_horizontal);
 		} else {
 			setContentView(R.layout.transports);
 		}
 
 		TransportManager tm = new TransportManager(this, "database.db");
 		Cursor c = tm.getAllFromDb();
 
 		ListAdapter adapter = new SimpleCursorAdapter(this,
 				android.R.layout.simple_list_item_1, c, c.getColumnNames(),
 				new int[] { android.R.id.text1 });
 
 		final ListView lv = (ListView) findViewById(R.id.listView);
 		lv.setAdapter(adapter);
 		lv.setOnItemClickListener(this);
 		lv.setOnItemLongClickListener(this);
 		tm.close();
 
 		final EditText et = (EditText) findViewById(R.id.search);
 		et.setOnEditorActionListener(this);
 
 		et.addTextChangedListener(new TextWatcher() {
 
 			public void onTextChanged(CharSequence input, int arg1, int arg2,
 					int arg3) {
 				if (input.length() == 3) {
 					et.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_DONE);
 				}
 			}
 
 			public void beforeTextChanged(CharSequence arg0, int arg1,
 					int arg2, int arg3) {
 			}
 
 			public void afterTextChanged(Editable arg0) {
 			}
 		});
 
 		MatrixCursor c2 = new MatrixCursor(
 				new String[] { "name", "desc", "_id" });
 		SimpleCursorAdapter adapter2 = new SimpleCursorAdapter(this,
 				android.R.layout.two_line_list_item, c2, c2.getColumnNames(),
 				new int[] { android.R.id.text1, android.R.id.text2 }) {
 
 			public boolean isEnabled(int position) {
 				return false;
 			}
 		};
 		ListView lv2 = (ListView) findViewById(R.id.listView2);
 		lv2.setAdapter(adapter2);
 	}
 
 	@Override
 	public void onItemClick(final AdapterView<?> av, View v, int position,
 			long id) {
 		Cursor c = (Cursor) av.getAdapter().getItem(position);
 		final String location = c.getString(c.getColumnIndex("name"));
 
 		TextView tv = (TextView) findViewById(R.id.transportText);
 		tv.setText("Abfahrt: " + location);
 
 		tv = (TextView) findViewById(R.id.transportText2);
 		tv.setText("Gespeicherte Stationen:");
 
 		SimpleCursorAdapter adapter = (SimpleCursorAdapter) av.getAdapter();
 		TransportManager tm = new TransportManager(this, "database.db");
		tm.replaceIntoDb(location);
 		adapter.changeCursor(tm.getAllFromDb());
 		tm.close();
 
 		final ProgressDialog progress = ProgressDialog.show(this, "",
 				"Lade ...", true);
 
 		new Thread(new Runnable() {
 			public void run() {
 				Cursor c = null;
 				try {
 					TransportManager tm = new TransportManager(av.getContext(),
 							"database.db");
 					if (!connected()) {
 						throw new Exception("<Keine Internetverbindung>");
 					}
 					c = tm.getDeparturesFromExternal(location);
 					tm.close();
 				} catch (Exception e) {
 					MatrixCursor c2 = new MatrixCursor(new String[] { "name",
 							"desc", "_id" });
 					c2.addRow(new String[] { e.getMessage(), "", "0" });
 					c = c2;
 				}
 
 				final Cursor c2 = c;
 				runOnUiThread(new Runnable() {
 					public void run() {
 						progress.hide();
 
 						ListView lv2 = (ListView) findViewById(R.id.listView2);
 						SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv2
 								.getAdapter();
 						adapter.changeCursor(c2);
 					}
 				});
 			}
 		}).start();
 	}
 
 	@Override
 	public boolean onItemLongClick(final AdapterView<?> av, View v,
 			final int position, long id) {
 
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage("Wirklch lschen?");
 		builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 				Cursor c = (Cursor) av.getAdapter().getItem(position);
 				String location = c.getString(c.getColumnIndex("name"));
 
 				TransportManager tm = new TransportManager(av.getContext(),
 						"database.db");
 				tm.deleteFromDb(location);
 
 				SimpleCursorAdapter adapter = (SimpleCursorAdapter) av
 						.getAdapter();
 				adapter.changeCursor(tm.getAllFromDb());
 				tm.close();
 
 				dialog.dismiss();
 			}
 		});
 		builder.setNegativeButton("Nein",
 				new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int id) {
 						dialog.cancel();
 					}
 				});
 		builder.show();
 
 		return false;
 	}
 
 	@Override
 	public boolean onEditorAction(final TextView input, int code, KeyEvent key) {
 		final ProgressDialog progress = ProgressDialog.show(this, "",
 				"Lade ...", true);
 
 		new Thread(new Runnable() {
 			public void run() {
 				String message = "";
 				Cursor c = null;
 				try {
 					if (!connected()) {
 						throw new Exception("<Keine Internetverbindung>");
 					}
 					TransportManager tm = new TransportManager(
 							input.getContext(), "database.db");
 					c = tm.getStationsFromExternal(input.getText().toString());
 					tm.close();
 				} catch (Exception e) {
 					message = e.getMessage();
 				}
 
 				final Cursor c2 = c;
 				final String message2 = message;
 
 				runOnUiThread(new Runnable() {
 					public void run() {
 						progress.hide();
 
 						if (c2 != null) {
 							TextView tv = (TextView) findViewById(R.id.transportText2);
 							tv.setText("Suchergebnis:");
 
 							ListView lv = (ListView) findViewById(R.id.listView);
 							SimpleCursorAdapter adapter = (SimpleCursorAdapter) lv
 									.getAdapter();
 							adapter.changeCursor(c2);
 						}
 						if (message2.length() > 0) {
 							Toast.makeText(input.getContext(), message2,
 									Toast.LENGTH_LONG).show();
 						}
 					}
 				});
 			}
 		}).start();
 		return false;
 	}
 }
