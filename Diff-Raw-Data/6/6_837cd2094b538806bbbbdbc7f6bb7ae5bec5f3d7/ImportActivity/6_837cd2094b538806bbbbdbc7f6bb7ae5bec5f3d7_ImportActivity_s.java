 /*
  * This file is part of the loot project for Android.
  *
  * This program is free software: you can redistribute it and/or modify it 
  * under the terms of the GNU General Public License as published by 
  * the Free Software Foundation, either version 3 of the License, or 
  * (at your option) any later version. This program is distributed in the 
  * hope that it will be useful, but WITHOUT ANY WARRANTY; without 
  * even the implied warranty of MERCHANTABILITY or FITNESS FOR 
  * A PARTICULAR PURPOSE. See the GNU General Public License 
  * for more details. You should have received a copy of the GNU General 
  * Public License along with this program. If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (C) 2008, 2009, 2010, 2011 Christopher McCurdy
  */
 
 package net.gumbercules.loot.premium;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import net.gumbercules.loot.R;
 import net.gumbercules.loot.backend.MemoryStatus;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Looper;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.csvreader.CsvReader;
 
 public class ImportActivity extends Activity
 {
 	private static final String TAG	= "net.gumbercules.loot.premium.ImportActivity";
 	
 	private ListView mFileView;
 	private TextView mEmptyView;
 	
 	private ArrayList<String> mFiles;
 
 	private int mInsertedRows;
 	private ArrayList<Integer> mInserted;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 
 		if (!requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS))
 		{
 			Log.d(TAG + ".onCreate", "Could not get requested window feature: indeterminate progress");
 		}
 
 		final int account_id = getIntent().getIntExtra("id", -1);
 		
 		setContentView(R.layout.import_);
 		setProgressBarIndeterminateVisibility(true);
 		
 		mFileView = (ListView)findViewById(R.id.file_list);
 		mEmptyView = (TextView)findViewById(R.id.empty_view);
 		
 		mFiles = new ArrayList<String>();
 		final FileListAdapter fileAdapter = new FileListAdapter(this,
 				R.layout.import_row, mFiles);
 		mFileView.setAdapter(fileAdapter);
 		mFileView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		
 		mInsertedRows = 0;
 		mInserted = new ArrayList<Integer>();
 
 		ImageButton import_button = (ImageButton)findViewById(R.id.import_button);
 		final ImportActivity ia = this;
 		import_button.setOnClickListener(new Button.OnClickListener()
 		{
 			@Override
 			public void onClick(View view)
 			{
 				String item;
 				try
 				{
 					item = fileAdapter.getItem(mFileView.getCheckedItemPosition());
 				}
 				catch (IndexOutOfBoundsException e)
 				{
 					Toast.makeText(ia, R.string.select_item, Toast.LENGTH_SHORT).show();
 					return;
 				}
 				
 				ProgressDialog pd = new ProgressDialog(view.getContext());
 				pd.setCancelable(true);
 				pd.setIndeterminate(true);
 				pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 				pd.setMessage("Importing records. Please wait.");
 
 				ImportThread it = new ImportThread(ia, pd);
 				it.setAcctId(account_id);
 				it.setFilename(item);
 				
 				pd.show();
 				it.start();
 			}
 		});
 		
 		if (!MemoryStatus.checkMemoryStatus(this, false))
 		{
 			import_button.setEnabled(false);
 		}
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		
 		final FileFinder ff = new FileFinder();
 		ff.addFilter(".csv");
 		ff.addFilter(".qif");
 		final Context context = this;
 		
 		new Thread()
 		{
 			public void run()
 			{
 				boolean empty = false;
 				String search_dir = Environment.getExternalStorageDirectory().getPath();
 				
 				SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
 				String imp = prefs.getString("import_search", "/");
 
 				if (imp != null && imp.equals("true"))
 				{
 					search_dir += "/loot";
 				}
 				
 				if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
 				{
 					ff.findFiles(search_dir, mFiles);
 				}
 				
 				if (mFiles.size() == 0)
 				{
 					empty = true;
 				}
 				
 				final boolean f_empty = empty;
 				runOnUiThread(new Runnable()
 				{
 					@Override
 					public void run()
 					{
 						setFileview(f_empty);
 						setProgressBar(false);
 					}
 				});
 			}
 		}.start();
 	}
 	
 	@Override
 	public void finish()
 	{
 		Intent i = new Intent();
 		boolean changes = false;
 		
 		if (mInsertedRows > 0)
 		{
 			changes = true;
 		}
 		
 		i.putExtra("t_changes", changes);
 		i.putExtra("t_ids", mInserted);
 		setResult(RESULT_OK, i);
 		
 		super.finish();
 	}
 
 	@Override
 	protected void onPause()
 	{
 		finish();
 		super.onPause();
 	}
 	
 	private void setFileview(boolean empty)
 	{
 		if (empty)
 		{
 			mFileView.setVisibility(View.GONE);
 			mEmptyView.setVisibility(View.VISIBLE);
 		}
 		else
 		{
 			((FileListAdapter)mFileView.getAdapter()).notifyDataSetChanged();
 			mFileView.setVisibility(View.VISIBLE);
 			mEmptyView.setVisibility(View.GONE);
 		}
 	}
 	
 	private void setProgressBar(boolean b)
 	{
 		setProgressBarIndeterminateVisibility(b);
 	}
 	
 	private void setInsertedRows(int rows)
 	{
 		mInsertedRows = rows;
 	}
 	
 	private class ImportThread extends Thread
 	{
 		private static final int FORMAT_QUICKEN	= 0;
 		private static final int FORMAT_CSV		= 1;
 		
 		private ProgressDialog mPd;
 		private Activity mActivity;
 		private int mAcctId;
 		private String mFilename;
 		private boolean mInvalidRows;
 		private int mFormat;
 
 		public ImportThread(Activity a, ProgressDialog pd)
 		{
 			mPd = pd;
 			mActivity = a;
 			mInvalidRows = false;
 		}
 		
 		@Override
 		public void run()
 		{
 			int rows = importAccount(mAcctId, mFilename);
 			setInsertedRows(rows);
 			mPd.dismiss();
 
 			Looper.prepare();
 
 			// if there are invalid rows, notify the user through an alert dialog
 			if (mInvalidRows)
 			{
 				int res;
 				if (mFormat == FORMAT_QUICKEN)
 				{
 					res = R.string.import_problem_qif;
 				}
 				else
 				{
 					res = R.string.import_problem_csv;
 				}
 			
 				new AlertDialog.Builder(mActivity)
 						.setTitle(R.string.import_problem_title)
 						.setMessage(res)
 						.setNeutralButton(android.R.string.ok, null)
 						.show();
 			}
 			
 			Toast.makeText(mActivity, rows + " " + mActivity.getResources()
 					.getString(R.string.import_successful), Toast.LENGTH_LONG).show();
 
 			Looper.loop();
 		}
 		
 		public void setAcctId(int id)
 		{
 			mAcctId = id;
 		}
 		
 		public void setFilename(String filename)
 		{
 			mFilename = filename;
 		}
 		
 		private int importAccount(int acct_id, String filename)
 		{
 			if (filename.contains(".qif"))
 			{
 				mFormat = FORMAT_QUICKEN;
 			}
 			else if (filename.contains(".csv"))
 			{
 				mFormat = FORMAT_CSV;
 			}
 			
 			BufferedReader reader = null;
 			try
 			{
 				reader = new BufferedReader(new FileReader(filename), 8096);
 			}
 			catch (FileNotFoundException e)
 			{
 				return -1;
 			}
 			
 			return importFile(reader, mFormat);
 		}
 		
 		private boolean importTransaction(ContentValues cv)
 		{
 			Uri uri = Uri.parse("content://net.gumbercules.loot.transactionprovider/transaction");
 			
 			ContentResolver cr = mActivity.getContentResolver(); 
 			if (cr != null && cv != null)
 			{
 				Uri result = cr.insert(uri, cv);
 				if (result != null)
 				{
 					List<String> path = result.getPathSegments();
 					if (path != null && path.size() != 0)
 					{
 						mInserted.add(Integer.valueOf(path.get(path.size() - 1)));
 					}
 					return true;
 				}
 			}
 			
 			return false;
 		}
 		
 		private int importFile(BufferedReader in, int format)
 		{
 			if (format == FORMAT_QUICKEN)
 			{
 				return importQuickenFile(in);
 			}
 			else if (format == FORMAT_CSV)
 			{
 				return importCsvFile(in);
 			}
 			
 			return -1;
 		}
 		
 		private int importQuickenFile(BufferedReader in)
 		{
 			int rows = 0;
 			ContentValues cv = new ContentValues();
 
 			String line;
 			try
 			{
 				line = in.readLine();
 			}
 			catch (IOException e)
 			{
 				Log.e(TAG + "$ImportThread.getQuickenImportValues", "Error reading import file");
 				return rows;
 			}
 			
 			while (line != null)
 			{
 				char prefix = '\0';
 				
 				try
 				{
 					prefix = line.charAt(0);
 					line = line.substring(1);
 				}
 				catch (IndexOutOfBoundsException e) { }
 				
 				if (prefix == 'D')
 				{
 					String default_date = "MM/dd/yyyy";
 					
 					ContentResolver cr = mActivity.getContentResolver();
 					String uri = "content://net.gumbercules.loot.premium.settingsprovider/date_format";
 					Cursor cur = cr.query(Uri.parse(uri), null, null, null, null);
 					
 					if (cur != null && cur.moveToFirst())
 					{
 						String date = cur.getString(1);
 						if (date != null && !date.equals("null") && !date.equals(""))
 						{
 							default_date = date;
 						}
 					}
 
 					SimpleDateFormat sdf = new SimpleDateFormat(default_date);
 					Date d;
 					try
 					{
 						d = sdf.parse(line);
 					}
 					catch (ParseException e)
 					{
 						try
 						{
 							// parse the date and add 2000 to it
 							sdf = new SimpleDateFormat("MM/dd/yy");
 							d = sdf.parse(line);
 						}
 						catch (ParseException pe)
 						{
 							try
 							{
 								String[] dates = line.split("'");
 								sdf = new SimpleDateFormat("M/d");
 								d = sdf.parse(dates[0]);
 								int year = Integer.valueOf(dates[1].trim());
 								Calendar cal = Calendar.getInstance();
 								cal.setTime(d);
 								cal.set(Calendar.YEAR, year);
 								d = cal.getTime();
 							}
 							catch (Exception ex)
 							{
 								Log.e(TAG + "$ImportThread.getQuickenImportValues",
 										"Could not parse date: " + line + "; record skipped");
 								continue;
 							}
 						}
 					}
 					
 					Calendar cal = Calendar.getInstance();
 					cal.setTime(d);
 					int year = cal.get(Calendar.YEAR);
 					if (year < 100)
 					{
 						// add 2000 years if it is in the first half of the century, 
 						// or 1900 years if it is in the second half
 						cal.set(Calendar.YEAR, year + (year < 50 ? 2000 : 1900));
 						d = cal.getTime();
 					}
 					cv.put("date", new Long(d.getTime()));
 				}
 				else if (prefix == 'P')
 				{
 					cv.put("party", line);
 				}
 				else if (prefix == 'T')
 				{
 					try
 					{
 						cv.put("amount", Double.valueOf(line.replace(",", "")));
 					}
 					catch (NumberFormatException e)
 					{
 						Log.e(TAG + "$ImportThread.getQuickenImportValues",
 								"Could not parse record beginning with 'T'. Could be fine. Continuing.");
 						Log.e(TAG + "$ImportThread.getQuickenImportValues",
 								"Record in question: " + line);
 					}
 				}
 				else if (prefix == 'N')
 				{
 					try
 					{
 						cv.put("check_num", Integer.valueOf(line));
 					}
 					catch (NumberFormatException e)
 					{
 						Log.e(TAG + "$ImportThread.getQuickenImportValues",
 								"Could not parse record beginning with 'N'. Could be fine. Continuing.");
 						Log.e(TAG + "$ImportThread.getQuickenImportValues",
 								"Record in question: " + line);
 					}
 				}
 				else if (prefix == 'M')
 				{
 					cv.put("tags", line);
 				}
 				else if (prefix == '^')
 				{
 					if (cv.containsKey("date") && cv.containsKey("party") && cv.containsKey("amount"))
 					{
 						cv.put("account", mAcctId);
 						if (importTransaction(cv) == false)
 						{
 							Log.i(TAG + "$ImportThread.getQuickenImportValues",
 									"There was an error inserting this transaction");
 						}
 						else
 						{
 							++rows;
 						}
 					}
 					
 					cv.clear();
 				}
 				
 				try
 				{
 					line = in.readLine();
 				}
 				catch (IOException e)
 				{
 					Log.e(TAG + "$ImportThread.getQuickenImportValues",
 							"Error reading import line");
 				}
 			}
 			
 			return rows;
 		}
 		
 		private String[] parseCsvString(String str)
 		{
 			if (str == null || str.equals("null"))
 			{
 				return null;
 			}
 			
 			CsvReader reader;
 			reader = new CsvReader(new ByteArrayInputStream(str.getBytes()),
 					Charset.defaultCharset());
 			
 			String[] values = null;
 			int size = 0;
 			
 			try
 			{
 				reader.readRecord();
 			}
 			catch (IOException e)
 			{
 				Log.e(TAG + "$ImportThread.parseCsvString", "IO exception reading record");
 				return null;
 			}
 			
 			size = reader.getColumnCount();
 			
 			if (size > 0)
 			{
 				values = new String[size];
 			}
 			
 			for (int i = 0; i < size; ++i)
 			{
 				try
 				{
 					values[i] = reader.get(i);
 				}
 				catch (IOException e)
 				{
 					Log.e(TAG + "$ImportThread.parseCsvString",
 							"IO exception getting value for column " + i);
 				}
 			}
 			
 			return values;
 		}
 		
 		private int importCsvFile(BufferedReader in)
 		{
 			int rows = 0;
 			ContentValues cv = new ContentValues();
 			CsvReader reader = new CsvReader(in);
 
 			ContentResolver cr = mActivity.getContentResolver();
 			String uri = "content://net.gumbercules.loot.premium.settingsprovider/";
 			Cursor cur = cr.query(Uri.parse(uri + "date_format"), null, null, null, null);
 			String default_date = "yyyy/MM/dd";
 			
 			// parse the date format
 			if (cur != null && cur.moveToFirst())
 			{
 				String date = cur.getString(1);
 				if (date != null && !date.equals("null") && !date.equals(""))
 				{
 					default_date = date;
 				}
 			}
 
 			SimpleDateFormat sdf = new SimpleDateFormat(default_date);
 			Log.i(TAG + "$ImportThread.importCsvFile",
 					"date format: " + default_date);
 			
 			String csv_uri = uri + "csv/";
 			// parse the csv order
 			String[] csv_order = { "%d", "%c", "%p", "%a", "%t" };
 			cur = cr.query(Uri.parse(csv_uri + "order"), null, null, null, null);
 			if (cur != null && cur.moveToFirst())
 			{
 				String[] tmp = parseCsvString(cur.getString(1));
 				if (tmp != null)
 				{
 					csv_order = tmp;
 				}
 			}
 			
 			final int num_columns = csv_order.length;
 			
 			// parse the credit and debit values
 			String[] credit_types = { "credit", "deposit" };
 			String[] debit_types = { "debit", "withdraw" };
 			
 			cur = cr.query(Uri.parse(csv_uri + "credit_type"), null, null, null, null);
 			if (cur != null && cur.moveToFirst())
 			{
 				String[] tmp = parseCsvString(cur.getString(1));
 				if (tmp != null)
 				{
 					credit_types = tmp;
 				}
 			}
 			
 			cur = cr.query(Uri.parse(csv_uri + "debit_type"), null, null, null, null);
 			if (cur != null && cur.moveToFirst())
 			{
 				String[] tmp = parseCsvString(cur.getString(1));
 				if (tmp != null)
 				{
 					debit_types = tmp;
 				}
 			}
 
 			try
 			{
 				while (reader.readRecord())
 				{
 					cv.clear();
 					
 					try
 					{
 						for (int i = 0; i < num_columns; ++i)
 						{
 							parseColumn(csv_order[i], reader.get(i), sdf, cv);
 						}
 						
 						cv.put("account", new Integer(mAcctId));
 						
 						// check to see if it contains a type and if it's valid
 						checkType(cv, credit_types, debit_types);
 						
 						checkColumns(cv);
 					}
 					catch (ParseException e)
 					{
 						Log.e(TAG + "$ImportThread.getCsvImportValues",
 								"Could not parse the date; record skipped");
 						mInvalidRows = true;
 						continue;
 					}
 					catch (NumberFormatException e)
 					{
 						Log.e(TAG + "$ImportThread.getCsvImportValues",
 								"Could not parse the check number or amount; record skipped");
 						mInvalidRows = true;
 						continue;
 					}
 					catch (RequiredColumnsMissingException e)
 					{
 						Log.e(TAG + "$ImportThread.getCsvImportValues",
 								"Missing either date, party, or amount values");
 					}
 					
 					if (importTransaction(cv) == false)
 					{
 						Log.i(TAG + "$ImportThread.getCsvImportValues",
 								"There was an error inserting this transaction");
 						mInvalidRows = true;
 						continue;
 					}
 					
 					++rows;
 				}
 				
 				reader.close();
 			}
 			catch (IOException e)
 			{
 				Log.e(TAG + "$ImportThread.getCsvImportValues", "Error reading import file");
 			}
 			
 			return rows;
 		}
 		
 		private void checkColumns(ContentValues cv)
 			throws RequiredColumnsMissingException
 		{
 			if (!cv.containsKey("party") || !cv.containsKey("amount") && !cv.containsKey("date"))
 			{
 				throw new RequiredColumnsMissingException();
 			}
 		}
 
 		private void checkType(ContentValues cv, String[] credit_types, String[] debit_types)
 		{
 			if (cv.containsKey("type") && cv.containsKey("abs"))
 			{
 				String type = cv.getAsString("type");
 				boolean isDebit = false;
 				
 				for (String s : debit_types)
 				{
 					if (s.equalsIgnoreCase(type))
 					{
 						isDebit = true;
 						break;
 					}
 				}
 				
 				for (String s : credit_types)
 				{
 					if (s.equalsIgnoreCase(type))
 					{
 						isDebit = false;
 						break;
 					}
 				}
 				
 				if (isDebit)
 				{
 					double amount = cv.getAsDouble("amount");
 					cv.remove("amount");
 					cv.put("amount", -amount);
 				}
 			}
 		}
 		
 		private void parseColumn(String column, String val, SimpleDateFormat sdf, ContentValues cv) 
 			throws ParseException, NumberFormatException
 		{
 			if (column.equals("%d"))
 			{
 				cv.put("date", sdf.parse(val).getTime());
 			}
 			else if (column.equals("%c"))
 			{
				cv.put("check_num", new Integer(val));
 			}
 			else if (column.equals("%p"))
 			{
 				cv.put("party", val);
 			}
 			else if (column.equals("%a"))
 			{
 				cv.put("amount", new Double(val));
 			}
 			else if (column.equals("%b"))
 			{
 				cv.put("amount", new Double(val));
 				cv.put("abs", true);
 			}
 			else if (column.equals("%t"))
 			{
 				cv.put("tags", val);
 			}
 			else if (column.equals("%y"))
 			{
 				cv.put("type", val);
 			}
 			else if (column.equals("%z"))
 			{
 				// do nothing
 			}
 		}
 		
 		private class RequiredColumnsMissingException extends Exception
 		{
 			private static final long serialVersionUID = -4409440405806817446L;
 		}
 	}
 	
 	private class FileListAdapter extends ArrayAdapter<String>
 	{
 		private int mResource;
 
 		public FileListAdapter(Context context, int textViewResourceId, List<String> objects)
 		{
 			super(context, textViewResourceId, objects);
 			mResource = textViewResourceId;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent)
 		{
 			View v = createViewFromResource(convertView, parent, mResource);
 			TextView text = (TextView)v.findViewById(android.R.id.text1);
 			File f = new File(super.getItem(position));
 			text.setText(f.getName());
 			v.setMinimumWidth(parent.getWidth());
 			text.setWidth(v.getWidth());
 			
 			return v;
 		}
 		
 		private View createViewFromResource(View convertView, ViewGroup parent, int resource)
 		{
 			View view;
 			
 			if (convertView == null)
 			{
 				view = ((LayoutInflater)super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).
 						inflate(resource, parent, false);
 			}
 			else
 			{
 				view = convertView;
 			}
 		
 			return view;
 		}
 	}
 }
