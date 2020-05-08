 package net.acuttone.reddimg;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class SubredditsPickerActivity extends Activity {
 	protected static final int DELETE_CONFIRMATION_DLG = 0;
 	private static final String STRING_SEPARATOR = ";";
 	private static final String SUBREDDIT_PREFIX = "r/";
 	public static final String SUBREDDITS_LIST_KEY = "SUBREDDIT_LIST";
 
 	private EditText editText;
 	private ListView listView;
 	private Button btnNew;
 	private SubredditArrayAdapter arrayAdapter;
 	private String selectedItem;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		setContentView(R.layout.subredditpicker);
 
 		editText = (EditText) findViewById(R.id.subredditpicker_edittextnew);
 		listView = (ListView) findViewById(R.id.subredditpicker_listView);
 		btnNew = (Button) findViewById(R.id.subredditpicker_btnnew);
 
 		arrayAdapter = new SubredditArrayAdapter(this, new ArrayList<String>(), R.drawable.minus, false);
 		listView.setAdapter(arrayAdapter);
 
 		btnNew.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				String text = editText.getText().toString();
 				if (text.matches("[a-zA-Z]+")) {
 					arrayAdapter.add(SUBREDDIT_PREFIX + text);
 				} else {
 					Toast.makeText(getBaseContext(), "Invalid name!", Toast.LENGTH_SHORT).show();
 				}
 				editText.getText().clear();
 			}
 		});
 
 		listView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
 				selectedItem = arrayAdapter.getItem((int) id);
 				showDialog(DELETE_CONFIRMATION_DLG, null);
 			}
 		});
 	}
 	
 	@Override
 	protected void onPause() {
 		super.onPause();
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		arrayAdapter.getSubreddits().clear();
 		arrayAdapter.getSubreddits().addAll(getSubredditsFromPref());
 		arrayAdapter.notifyDataSetChanged();
 	}
 	
 	public static List<String> getSubredditsFromPref() {
 		SharedPreferences sp = ReddimgApp.instance().getPrefs();
 		String subreddits = sp.getString(SUBREDDITS_LIST_KEY, getDefaultSubreddits());
 		List<String> names = new ArrayList<String>((Arrays.asList(subreddits.split(STRING_SEPARATOR))));
 		return names;
 	}
 
 	private static String getDefaultSubreddits() {
 		String[] subreddits = { "funny", "pics" };
 		StringBuilder sb = new StringBuilder();
 		for (String s : subreddits) {
 			sb.append(SUBREDDIT_PREFIX + s + STRING_SEPARATOR);
 		}
 		String result = sb.toString();
 		result = result.substring(0, result.length() - 1);
 		return result;
 	}
 
 //	@Override
 //	protected void onStop() {
 //		super.onStop();
 //		saveSubreddits(getBaseContext(), arrayAdapter.getSubreddits());
 //	}
 
 	public static void saveSubreddits(List<String> subreddits) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < subreddits.size(); i++) {
 			sb.append(subreddits.get(i));
 			sb.append(STRING_SEPARATOR);
 		}
 		SharedPreferences sp = ReddimgApp.instance().getPrefs();
 		Editor editor = sp.edit();
 		editor.putString(SUBREDDITS_LIST_KEY, sb.toString());
 		editor.commit();
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		if (DELETE_CONFIRMATION_DLG == id) {
 			((AlertDialog) dialog).setMessage("Do you want to remove '" + selectedItem + "' from your subreddits?");
 		}
 		super.onPrepareDialog(id, dialog);
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id, Bundle args) {
 		if (DELETE_CONFIRMATION_DLG == id) {
 			AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setMessage("");
 			builder.setCancelable(false);
 			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int id) {
 					arrayAdapter.remove(selectedItem);
 					saveSubreddits(arrayAdapter.getSubreddits());
 					arrayAdapter.notifyDataSetChanged();
 				}
 			});
 			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 
 				}
 			});
 			return builder.create();
 		}
 		return super.onCreateDialog(id, args);
 	}
 }
