 package com.dnsalias.sanja.simplecarbocalc;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.LinkedList;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.Environment;
 import android.text.ClipboardManager;
 import android.text.TextUtils;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 
 public class SimpleCarboCalcImport extends Activity
 {
 	private static final String LOGTAG= "SimpleCarboCalcImport";
 
 	static final Pattern FILES_PATTERN= Pattern.compile(".*backup.*\\.txt$");
 	static final Pattern REPLACE_PATTERN= Pattern.compile("^.*" + "backup");
 	RadioGroup mType;
 	RadioGroup mSource;
 	RadioButton mRestoreBackup;
 	RadioButton mInternalConf;
 	RadioButton mFileConf;
 	RadioButton mClipConf= null;
 	EditText mFile;
 	CheckBox mAllConfig;
 	ListView mBackupFiles;
 	ListView mImportConfig;
 	Button mCheck;
 	Button mImport;
 	
 	File mStandardFiles[]= null;
 	String mStandardFilesPrompts[]= null;
 	String mWarnArray[]= new String[1];
 	String mEmptyArray[]= new String[0];
 	String mConfigCheckResults[]= mEmptyArray;
 	String mConfigLines[]= null;
 	
 	ClipboardManager mClipboard= null;
 
 	RadioGroup.OnCheckedChangeListener typeChangeListener= new RadioGroup.OnCheckedChangeListener()
     {
     	public void onCheckedChanged(RadioGroup group, int checkedId)
         {
         	if (checkedId == R.id.restoreBackup)
         	{
         		mAllConfig.setEnabled(false); mAllConfig.setChecked(true);
         		mSource.check(R.id.importFileConf);
         		mCheck.setEnabled(false);
         		mImport.setEnabled(true);
         		mConfigCheckResults= mWarnArray;
         		mConfigCheckResults[0]= getResources().getString(R.string.RestoreBackupWarn);
         		mConfigLines= null;
         		mImportConfig.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_checked, mConfigCheckResults));
         	}
         	else
         	{ 
         		mAllConfig.setEnabled(true);
         		mCheck.setEnabled(true);
         		if (mConfigLines == null)
         		{
         			mConfigCheckResults= mEmptyArray;
         			mImportConfig.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_checked, mConfigCheckResults));
         		}
         		mImport.setEnabled(mConfigLines != null);
         	}
         	checkSource();
         	checkClipAvaliable();
         }
     };
     
     RadioGroup.OnCheckedChangeListener typeSourceListener= new RadioGroup.OnCheckedChangeListener()
     {
     	public void onCheckedChanged(RadioGroup group, int checkedId)
         {
     		checkSource();
         }
     };
     
     
     View.OnClickListener doImportListener= new View.OnClickListener()
     {
     	public void onClick(View v)
     	{
     		Resources resources= getResources();
     		boolean res= false;
     		if (mRestoreBackup.isChecked())
     			if (mInternalConf.isChecked())
     				res= ProdList.getInstance().loadInitFile(resources);
     			else
     				res= ProdList.getInstance().restoreBackupConfig();
     		else
     		{
     			if (!mAllConfig.isChecked())
     			{
     				long checked[]= mImportConfig.getCheckItemIds();
     				String lines[]= new String[checked.length];
     				for(int i= 0; i < checked.length; i++)
     					lines[i]= mConfigLines[(int)checked[i]];
     				mConfigLines= lines;
     			}
     			res= ProdList.getInstance().addConfig(mConfigLines);
     		}
 
        		mConfigCheckResults= mWarnArray;
        		mConfigCheckResults[0]= (res ? resources.getString(R.string.ImportFailure) : resources.getString(R.string.ImportSuccess));
     		mImportConfig.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_multiple_choice, mConfigCheckResults));
     		mConfigLines= null;
     		mImport.setEnabled(false);
     	}
     };
     
     View.OnClickListener doCheckListener= new View.OnClickListener()
     {
     	public void onClick(View v)
     	{
     		BufferedReader reader= null;
     		if (!mClipConf.isChecked())
     		{
     			InputStream inputStream= null;
     			if (mInternalConf.isChecked())
     			{
     				inputStream= getResources().openRawResource(R.raw.initial_backup);
     				if (inputStream != null)
     					reader = new BufferedReader(new InputStreamReader(inputStream));
     			}
     			else
     			{
     				try
     				{
     					inputStream= new FileInputStream(mFile.getText().toString());
     				}
     				catch (FileNotFoundException ex)
     				{
     					Log.e(LOGTAG, "exceprion: " + ex);
     					mConfigCheckResults= mWarnArray;
     					mConfigCheckResults[0]= ex.toString();
     				}
     			}
     			if (inputStream != null)
     				reader= new BufferedReader(new InputStreamReader(inputStream));
     		}
     		else
     		{
    			reader= new BufferedReader(new StringReader(mClipboard.getText().toString().
    					replaceAll("\\s+prod=", "\nprod=").
    					replaceAll("\\s+lang=", "\nlang=").
    					replaceAll("\\s+unit=", "\nunit=")));
     		}
     		if (reader != null)
     		{
     			checkFile(reader);
     			try { reader.close(); } catch (IOException ex) {};
     		}
     		mImportConfig.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_multiple_choice, mConfigCheckResults));
     		mImport.setEnabled(mConfigLines != null);
     	}
     };
     
     private OnItemClickListener mBackupListener = new OnItemClickListener()
 	{
 		public void onItemClick(AdapterView parent, View view, int position,
 				long id) {
 			mFile.setText(mStandardFiles[position].toString());
 		}
 	};
 	
 	CompoundButton.OnCheckedChangeListener allCangeListener= new CompoundButton.OnCheckedChangeListener()
 	{
 		public void	 onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 		{
 			mImportConfig.setChoiceMode(isChecked ? ListView.CHOICE_MODE_NONE : ListView.CHOICE_MODE_MULTIPLE);
 		}
 	};
 
     private void checkSource()
     {
     	if (!mFileConf.isChecked())
 		{
 			mFile.setEnabled(false);
     		mBackupFiles.setEnabled(false); setEmptyBackupFiles();
  		}
 		else
 		{
 			if (mRestoreBackup.isChecked())
 			{
 				File dir= new File(Environment.getExternalStorageDirectory(), "SimpleCarboCalc");
 				File file= new File(dir, "backup.txt");
 				mFile.setEnabled(false); mFile.setText(file.toString());
 				mBackupFiles.setEnabled(false); setEmptyBackupFiles();
 			}
 			else
 			{
 				mFile.setEnabled(true);
 				mBackupFiles.setEnabled(true); fillBackupFiles();
 			}
 		}
     	
     }
     
     private void setEmptyBackupFiles()
     {
     	mBackupFiles.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, new String[0]));
     }
     
     private void fillBackupFiles()
     {
 		int number, i;
 		File dir= new File(Environment.getExternalStorageDirectory(), "SimpleCarboCalc");
 		File[] files= dir.listFiles();
 
 		for(i= 0, number= 0; i < files.length; i++)
 		{
 			if (FILES_PATTERN.matcher(files[i].toString()).matches())
 			//if (files[i].toString().matches(".*backup.*\\.txt$"))
 			{
 				number++;
 				Log.v(LOGTAG, files[i].toString() + " matches");
 			}
 			else
 				Log.v(LOGTAG, files[i].toString() + " does not match '" + FILES_PATTERN.toString() + "'");
 		}
 		mStandardFilesPrompts= new String[number];
 		mStandardFiles= new File[number];
 		for(i= 0, number= 0; i < files.length; i++)
 			if (FILES_PATTERN.matcher(files[i].toString()).matches())
 			{
 				mStandardFilesPrompts[number]= REPLACE_PATTERN.matcher(files[i].toString()).replaceFirst("");
 				mStandardFilesPrompts[number]= mStandardFilesPrompts[number].substring(0, mStandardFilesPrompts[number].length() - 4);
 				Log.v(LOGTAG, "replaced: '" + mStandardFilesPrompts[number] + "'");
 				if (mStandardFilesPrompts[number].length() == 0)
 					mStandardFilesPrompts[number]= getResources().getString(R.string.BackupPlacer);
 				mStandardFiles[number]= files[i];
 				number++;
 			}
 		mBackupFiles.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, mStandardFilesPrompts));
 	}
     
 	@Override
     protected void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.import_config);
         setTitle(R.string.import_name);
         
         mClipboard= (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
         
         mType= (RadioGroup) findViewById(R.id.radioImportType);
         mSource= (RadioGroup) findViewById(R.id.radioSource);
         mRestoreBackup= (RadioButton) findViewById(R.id.restoreBackup);
         mInternalConf= (RadioButton) findViewById(R.id.importInternalConf);
         mFileConf= (RadioButton) findViewById(R.id.importFileConf);
         mClipConf= (RadioButton) findViewById(R.id.importClipConf);
         mFile= (EditText) findViewById(R.id.importFile);
         mAllConfig= (CheckBox) findViewById(R.id.allProds);
         mBackupFiles= (ListView) findViewById(R.id.backupFiles);
         mImportConfig= (ListView) findViewById(R.id.importConfigList);
         mCheck= (Button) findViewById(R.id.doCheck);
         mImport= (Button) findViewById(R.id.doImport);
         
         mBackupFiles.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_checked, new String[0]));
         mImportConfig.setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_checked, mConfigCheckResults));
         
         checkClipAvaliable();
         mAllConfig.setOnCheckedChangeListener(allCangeListener);
         mBackupFiles.setOnItemClickListener(mBackupListener);
         mSource.setOnCheckedChangeListener(typeSourceListener);
         mType.setOnCheckedChangeListener(typeChangeListener);
         mType.check(R.id.restoreBackup);
         mCheck.setOnClickListener(doCheckListener);
         mImport.setOnClickListener(doImportListener);
 	}
 	
 	void checkClipAvaliable()
 	{
 		if (mClipConf != null)
 			mClipConf.setEnabled(mClipboard.hasText() && !mRestoreBackup.isChecked());
 	}
 	
 	void checkFile(BufferedReader reader)
 	{
 		LinkedList<String> strings= new LinkedList<String>();
 		LinkedList<String> results= new LinkedList<String>();
 		ProdList prods= ProdList.getInstance();
 
 		try {
 			String line;
 			while ((line = reader.readLine()) != null) {
 				Log.v(LOGTAG, "read: '" + line + "'");
 				if (line.length() != 0 && line.charAt(0) != '#') {
 					String[] conf = TextUtils.split(line, "=");
 					if (conf.length != 2) {
 						Log.e(LOGTAG, "Unrecognized config line: '" + line + "'");
 					} else {
 						String res= null;
 						String desc= null;
 						if (conf[0].equals("lang")) {
 							desc= getResources().getString(R.string.LangLine);
 							res= prods.checkLangLine(conf[1]);
 						} else if (conf[0].equals("prod")) {
 							desc= getResources().getString(R.string.ProdLine);
 							res= prods.checkProdLine(conf[1]);
 						}
 						if (res != null)
 						{
 							strings.add(line);
 							results.add(desc+res);
 						}
 					}
 				}
 			}
 		} catch (IOException ex) {
 			Log.e(LOGTAG, "IO error: " + ex.toString());
 			mConfigCheckResults= mWarnArray;
 			mConfigCheckResults[0]= ex.toString();
 		} finally {
 			try {reader.close();} catch (IOException ex) {}
 		}
 		if (!strings.isEmpty())
 		{
 			mConfigLines= strings.toArray(new String[strings.size()]);
 			mConfigCheckResults= results.toArray(new String[strings.size()]);
 		}
 	}
 	
 }
