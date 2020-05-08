 /**
  * Part of one of andsens open source project (a41cv / aSQLiteManager) 
  *
  * @author andsen
  *
  */
 package dk.andsen.utils;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.TextView;
 import dk.andsen.asqlitemanager.DBViewer;
 import dk.andsen.asqlitemanager.Prefs;
 import dk.andsen.asqlitemanager.R;
 import dk.andsen.asqlitemanager.SQLViewer;
 
 /**
  * @author andsen
  *
  */
 public class NewFilePicker extends ListActivity {
 
 	private List<String> item = null;
 	private List<String> path = null;
 	private String root="/";
 	private TextView myPath;
 	boolean mExternalStorageAvailable = false;
 	boolean mExternalStorageWriteable = false;
 	private Context context = null;
 	// _SQLtype is true if opening a .sql file false if opening a database
 	private boolean _SQLtype = false;
 	private String _dbPath = null;
 	private boolean logging = false;
 	// _rootMode controls whether filePicker is in roor or normal mode
 	//private boolean _rootMode = false;
 	
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
		context = this.getBaseContext();
 		logging = Prefs.getLogging(context);
 		Bundle extras = getIntent().getExtras();
 		if(extras !=null)
 		{
 			//TODO need to pass path to database from caller to SQLViewer
 			_SQLtype = extras.getBoolean("SQLtype");
 			_dbPath = extras.getString("dbPath");
 			//_rootMode = extras.getBoolean("RootMode");
 		}
 		setContentView(R.layout.filepicker);
 		myPath = (TextView)findViewById(R.id.path);
 		File path = null;
 		String state = Environment.getExternalStorageState();
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			mExternalStorageAvailable = mExternalStorageWriteable = true;
 			path = Environment.getExternalStorageDirectory();
 			File programDirectory = new File(path.getAbsolutePath());
 			// have the object build the directory structure, if needed.
 			//programDirectory.mkdirs();
 			getDir(programDirectory.getAbsolutePath());
 		} else {
 			// No SDCard
 			new AlertDialog.Builder(this)
 			.setIcon(R.drawable.sqlite_icon)
 			.setTitle(getText(R.string.NoSDCard))
 			.setPositiveButton(R.string.OK, 
 					new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 				}
 			}).show();
 		}
 	}
 
 	/**
 	 * Updates the item and path lists
 	 * @param dirPath
 	 */
 	private void getDir(String dirPath)
 	//TODO create rootGetDir
 	/*
 	 * if (_root)
 	 *   rootGetDir(dirPath);
 	 * else
 	 *   normalGetDir(dirPatH);
 	 */
 	{
 		myPath.setText(getText(R.string.Path)+ " " + dirPath);
 		item = new ArrayList<String>();
 		path = new ArrayList<String>();
 		File f = new File(dirPath);
 		File[] files = f.listFiles();
 		if(!dirPath.equals(root))
 		{
 			item.add(root);
 			path.add(root);
 			item.add("../");
 			path.add(f.getParent());
 		}
 		Arrays.sort(files, new FileComparator());
 		for(int i=0; i < files.length; i++)
 		{
 			File file = files[i];
 			path.add(file.getPath());
 			if(file.isDirectory())
 				item.add(file.getName() + "/");
 			else
 				item.add(file.getName());
 		}
 		String[] filetypes = {".sqlite", ".db"};
 		MyArrayAdapter mlist = new MyArrayAdapter(this, item, filetypes); 
 		setListAdapter(mlist);
 	}
 
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		// TODO Must be devided into a root and normal mode part
 		final File file = new File(path.get(position));
 		if (file.isDirectory())
 		{
 			if(file.canRead())
 				getDir(path.get(position));
 			else
 			{
 				new AlertDialog.Builder(this)
 				.setIcon(R.drawable.sqlite_icon)
 				.setTitle(getText(R.string.SystemFolder))
 				.setMessage(getText(R.string.SystemFolderText))
 				.setPositiveButton(getText(R.string.OK), 
 						new DialogInterface.OnClickListener() {
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				}).show();
 			}
 		} else {
 			// Open the database
 			final SharedPreferences settings = getSharedPreferences("aSQLiteManager", MODE_PRIVATE);
 			if(!settings.getBoolean("FPJustOpen", false))
 			{
 				final Dialog dial = new Dialog(this);
 				if(_SQLtype) {
 					dial.setTitle(getText(R.string.OpenSQL));
 				} else {
 					dial.setTitle(getText(R.string.OpenDatabase));
 				}
 				dial.setContentView(R.layout.dialog);
 				LinearLayout ll = (LinearLayout)dial.findViewById(R.id.dialog);
 				LinearLayout ll2 = new LinearLayout(context);
 				ll2.setPadding(0, 0, 5, 0);
 				ll2.setOrientation(LinearLayout.HORIZONTAL);
 				ImageView iv = new ImageView(context);
 				iv.setBackgroundResource(R.drawable.ic_app);
 				ll2.addView(iv);
 				TextView tv = new TextView(context);
 				tv.setText("[" + file.getName() + "]?");
 				tv.setPadding(5, 0, 0, 0);
 				ll2.addView(tv);
 				ll.addView(ll2);
 				final CheckBox cb = new CheckBox(context);
 				cb.setText(R.string.AlwaysJustOpen);
 				ll.addView(cb);
 				LinearLayout ll3 = new LinearLayout(context);
 				ll3.setOrientation(LinearLayout.HORIZONTAL);
 				Button btnOk = new Button(context);
 				btnOk.setText(R.string.OK);
 				btnOk.setLayoutParams(new LinearLayout.LayoutParams(
 	          LinearLayout.LayoutParams.FILL_PARENT,
 	          LinearLayout.LayoutParams.WRAP_CONTENT,
 	          1.0F
 	      ));
 				btnOk.setOnClickListener(new OnClickListener(){
 					public void onClick(View arg0) {
 						// OK pressed
 						if (cb.isChecked()) {
 							// Save preferences
 							SharedPreferences.Editor editor = settings.edit();
 							editor.putBoolean("FPJustOpen", true);
 							editor.commit();
 						}
 						// Open database or SQL
 						if (_SQLtype) {
 							openSQL(file);
 						} else {
 							//TODO also a root mode needed here
 							openDatabase(file);
 						}
 						dial.dismiss();
 					}
 				});
 				ll3.addView(btnOk);
 				Button btnCancel = new Button(context);
 				btnCancel.setText(R.string.Cancel);
 				btnCancel.setOnClickListener(new OnClickListener(){
 					public void onClick(View arg0) {
 						dial.dismiss();
 					}
 				});
 				btnCancel.setLayoutParams(new LinearLayout.LayoutParams(
 	          LinearLayout.LayoutParams.FILL_PARENT,
 	          LinearLayout.LayoutParams.WRAP_CONTENT,
 	          1.0F
 	      ));
 				ll3.addView(btnCancel);
 				ll.addView(ll3);
 				dial.show();
 			} else {
 				Utils.logD("Path to SQL " + file.getAbsolutePath(), logging);
 				if(_SQLtype) {
 					// only open files ending with .sql
 					if(file.getAbsolutePath().endsWith(".sql")) {
 						openSQL(file);
 					} 					
 				} else {
 					openDatabase(file);
 				}
 			}
 		}
 	}
 	
 	private void openSQL(File file) {
 		Utils.logD("SQL file", logging);
 		Intent iSqlViewer = new Intent(context, SQLViewer.class);
 		iSqlViewer.putExtra("script", ""+ file.getAbsolutePath());
 		iSqlViewer.putExtra("db", _dbPath);
 		startActivity(iSqlViewer);
 	}
 	
 	private void openDatabase(File file) {
 		//TODO add root handling here
 		/*
 		 * If in root mode copy database to sdcard/aSQLiteManager and open it there
 		 * after close of database ask the user it it should replace the original
 		 */
 		Utils.logD("Other file file", logging);
 		Intent iDBViewer = new Intent(context, DBViewer.class);
 		iDBViewer.putExtra("db", ""+ file.getAbsolutePath());
 		startActivity(iDBViewer);
 	}
 	
 	/**
 	 * Sort files first directories then files	
 	 * @author andsen
 	 *
 	 */
 	class FileComparator implements Comparator<File> {
 	   
 	    public int compare(File file1, File file2){
 	    	String f1 = ((File)file1).getName();
 	    	String f2 = ((File)file2).getName();
 	    	int f1Length = f1.length();
 	    	int f2Length = f2.length();
 	    	boolean f1Dir = (((File)file1).isDirectory()) ? true: false;
 	    	boolean f2Dir = (((File)file2).isDirectory()) ? true: false;
 	    	int shortest = (f1Length > f2Length) ? f2Length : f1Length;
 	    	// one of the files is a directory
 	    	if (f1Dir && !f2Dir)
 	    		return -1;
 	    	if (f2Dir && !f1Dir)
 	    		return 1;
 	    	// sort alphabetically
 	    	for (int i = 0; i < shortest; i++) {
 	    		if (f1.charAt(i) > f2.charAt(i))
 	    			return 1;
 	    		else if (f1.charAt(i) < f2.charAt(i))
 	    			return -1;
 	    	}
 	    	if (f1Length > f2Length)
 	    		return 1;
 	    	else
 	    		return 0; 
 	    }
 	}
 }
