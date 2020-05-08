 package net.gumbercules.loot;
 
 import java.text.DateFormat;
 import java.text.DecimalFormatSymbols;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.AutoCompleteTextView;
 import android.widget.Button;
 import android.widget.CompoundButton;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.RadioButton;
 import android.widget.Spinner;
 import android.widget.TableRow;
 import android.widget.AdapterView.OnItemSelectedListener;
 
 public class TransactionEdit extends Activity
 {
 	public static final String KEY_TRANSFER = "te_transfer";
 	
 	private Transaction mTrans;
 	private RepeatSchedule mRepeat;
 	private int mTransId;
 	private int mFinishIntent;
 	private int mRequest;
 	private int mType;
 	private int mAccountId;
 	private boolean mFinished;
 	private int mDefaultRepeatValue;
 	private int mLastRepeatValue;
 	private Date mDate;
 	private int mAccountPos;
 	private CurrencyKeyListener mCurrencyListener;
 
 	private RadioButton checkRadio;
 	private RadioButton withdrawRadio;
 	private RadioButton depositRadio;
 	
 	private EditText dateEdit;
 	private ImageButton dateButton;
 	private AutoCompleteTextView partyEdit;
 	private EditText amountEdit;
 	private EditText checkEdit;
 	private MultiAutoCompleteTextView tagsEdit;
 	
 	private Spinner accountSpinner;
 	private Spinner repeatSpinner;
 	private ArrayAdapter<String> mRepeatAdapter;
 	
 	private RadioButton budgetRadio;
 	private RadioButton actualRadio;
 	
 	private Button saveButton;
 	private Button cancelButton;
 	
 	private boolean restarted;
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.trans);
 		
 		mFinishIntent = RESULT_CANCELED;
 		mFinished = false;
 		mDefaultRepeatValue = -1;
 		mDate = null;
 		restarted = false;
 
 		ArrayList<String> repeat =
 			new ArrayList(Arrays.asList(getResources().getStringArray(R.array.repeat)));
 		mRepeatAdapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_spinner_item, repeat);
         mRepeatAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
         
 		// get the type code so we know whether to show a transaction or a transfer window
 		if (savedInstanceState != null)
 		{
 			mRequest = savedInstanceState.getInt(TransactionActivity.KEY_REQ);
 			mType = savedInstanceState.getInt(TransactionActivity.KEY_TYPE);
 			mTransId = savedInstanceState.getInt(Transaction.KEY_ID);
 			mAccountId = savedInstanceState.getInt(Account.KEY_ID);
 			long date = savedInstanceState.getLong(Transaction.KEY_DATE);
 			mDate = (date == 0 ? null : new Date(date));
 			
 			mRepeat = new RepeatSchedule();
 			mRepeat.iter = savedInstanceState.getInt(RepeatSchedule.KEY_ITER);
 			mRepeat.freq = savedInstanceState.getInt(RepeatSchedule.KEY_FREQ);
 			mRepeat.custom = savedInstanceState.getInt(RepeatSchedule.KEY_CUSTOM);
 			long end = savedInstanceState.getLong(RepeatSchedule.KEY_DATE);
 			mRepeat.end = (end == 0 ? null : new Date(end));
 			mAccountPos = savedInstanceState.getInt(TransactionEdit.KEY_TRANSFER);
 			
 			restarted = true;
 		}
 		else
 		{
 			Bundle extras = getIntent().getExtras();
 			mRequest = extras.getInt(TransactionActivity.KEY_REQ);
 			mType = extras.getInt(TransactionActivity.KEY_TYPE);
 			mTransId = extras.getInt(Transaction.KEY_ID);
 			mAccountId = extras.getInt(Account.KEY_ID);
 		}
 
 		populateFields();
 	}
 	
 	private void populateFields()
 	{
 		depositRadio = (RadioButton)findViewById(R.id.depositRadio);
 		withdrawRadio = (RadioButton)findViewById(R.id.withdrawRadio);
 		checkRadio = (RadioButton)findViewById(R.id.checkRadio);
 		
 		dateEdit = (EditText)findViewById(R.id.dateEdit);
		dateEdit.setEnabled(false);
 		dateButton = (ImageButton)findViewById(R.id.datePickerButton);
 		
 		amountEdit = (EditText)findViewById(R.id.amountEdit);
 		mCurrencyListener = new CurrencyKeyListener();
 		amountEdit.setKeyListener(mCurrencyListener);
 		amountEdit.addTextChangedListener(new CurrencyKeyListener.CurrencyWatcher());
 		tagsEdit = (MultiAutoCompleteTextView)findViewById(R.id.tagsEdit);
 		String[] tags = Transaction.getAllTags();
 		if (tags == null)
 			tags = new String[0];
 		ArrayAdapter<String> tagsAdapter = new ArrayAdapter<String>(this, 
 				android.R.layout.simple_dropdown_item_1line, tags);
 		tagsEdit.setAdapter(tagsAdapter);
 		tagsEdit.setTokenizer(new TransactionActivity.SpaceTokenizer());
 		
 		// create the repeat spinner and populate the values
 		repeatSpinner = (Spinner)findViewById(R.id.repeatSpinner);
         repeatSpinner.setAdapter(mRepeatAdapter);
 
         actualRadio = (RadioButton)findViewById(R.id.ActualRadio);
 		budgetRadio = (RadioButton)findViewById(R.id.BudgetRadio);
 		
 		saveButton = (Button)findViewById(R.id.saveButton);
 		cancelButton = (Button)findViewById(R.id.cancelButton);
 		
         dateButton.setOnClickListener(new View.OnClickListener()
         {
             public void onClick(View v)
             {
                 showDialog(0);
             }
         });
 
 		// load the transaction if mTransId > 0
         Transaction trans = null;
 		if (mTransId == 0)
 		{
 			mTrans = new Transaction();
 			if (mRepeat == null)
 				mRepeat = new RepeatSchedule();
 			else
 				setRepeatSpinnerSelection(mRepeat);
 			
 			// set the date edittext to the current date by default
         	setDateEdit(mDate);
 		}
 		else
 		{
 			mTrans = Transaction.getTransactionById(mTransId);
 			trans = mTrans;
 			
 			if (!restarted)
 			{
 				if (mRepeat == null)
 				{
 					int repeat_id = RepeatSchedule.getRepeatId(mTransId);
 					if (repeat_id != -1)
 						mRepeat = RepeatSchedule.getSchedule(repeat_id);
 					else
 						mRepeat = new RepeatSchedule();
 				}
 				
 				if (trans == null)
 				{
 					Log.e(TransactionEdit.class.toString(), "trans is null in populateFields()");
 					return;
 				}
 	
 				// figure out if this is a normal transaction or a transfer
 				int transfer_id = mTrans.getTransferId();
 				if (transfer_id != -1)
 				{
 					Transaction transfer = Transaction.getTransactionById(transfer_id, true);
 					if (transfer == null)
 					{
 						transfer = new Transaction();
 						transfer.setId(-1);
 						mTrans.removeTransfer(transfer);
 						mType = TransactionActivity.TRANSACTION;
 					}
 					else
 						mType = TransactionActivity.TRANSFER;
 				}
 				else
 					mType = TransactionActivity.TRANSACTION;
 				
 				if (trans.type == Transaction.WITHDRAW)
 				{
 					withdrawRadio.setChecked(true);
 				}
 				else if (trans.type == Transaction.DEPOSIT)
 				{
 					depositRadio.setChecked(true);
 				}
 				
 				if (trans.budget && !trans.isPosted())
 				{
 					budgetRadio.setChecked(true);
 				}
 				else
 				{
 					actualRadio.setChecked(true);
 				}
 				
 				if (mDate == null)
 					setDateEdit(trans.date);
 				else
 					setDateEdit(mDate);
 	
 				// replace comma and currency symbol with empty string
 				NumberFormat nf = NumberFormat.getCurrencyInstance();
 				String num = nf.format(trans.amount);
 				StringBuilder sb = new StringBuilder();
 				sb.append(mCurrencyListener.getAcceptedChars());
 				String accepted = "[^" + sb.toString() + "]";
 				num = num.replaceAll(accepted, "");
 
 				amountEdit.setText(num);
 				
 				tagsEdit.setText(trans.tagListToString());
 			}
 
 			setRepeatSpinnerSelection(mRepeat);
 		}
         
 		if (mType == TransactionActivity.TRANSFER)
 		{
 	        ArrayAdapter<CharSequence> accountAdapter = showTransferFields();
 	        if (accountAdapter != null)
 	        {
 	        	if (!restarted && mTransId != 0)
 	        	{
 	        		Transaction transfer = Transaction.getTransactionById(mTrans.getTransferId(), true);
 	        		Account acct = Account.getAccountById(transfer.account);
 	        		int pos = accountAdapter.getPosition(acct.name);
 	        		accountSpinner.setSelection(pos);
 	        	}
 		        else if (restarted)
 		        {
 		        	accountSpinner.setSelection(mAccountPos);
 		        }
 	        }
 		}
 		else
 		{
 			showTransactionFields();
 			if (trans != null)
 			{
 				partyEdit.setText(trans.party);
 
 				if (trans.type == Transaction.CHECK)
 				{
 					checkEdit.setText(new Integer(trans.check_num).toString());
 					checkRadio.setChecked(true);
					checkEdit.setEnabled(true);
 				}
 			}
 		}
 		
 		repeatSpinner.setSelection(mDefaultRepeatValue);
 		repeatSpinner.setOnItemSelectedListener(mRepeatSpinnerListener);
 		
 		saveButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				mFinishIntent = RESULT_OK;
 				onPause();
 			}
 		});
 		
 		cancelButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View view)
 			{
 				setResult(mFinishIntent);
 				finish();
 			}
 		});
 	}
 
 	private OnItemSelectedListener mRepeatSpinnerListener = new Spinner.OnItemSelectedListener()
 		{
 			public void onItemSelected(AdapterView<?> adapter, View view, int pos, long id)
 			{
 				// only remove the row if the adapter has more than the 7 default items
 				// and the position isn't the row to be deleted
 				if (mRepeatAdapter.getCount() > 7 && pos != 7)
 					mRepeatAdapter.remove("Custom");
 
 				if (pos == 6)
 				{
 					mLastRepeatValue = mDefaultRepeatValue;
 					
 			    	Intent i = new Intent(view.getContext(), RepeatActivity.class);
 			    	i.putExtra(RepeatSchedule.KEY_ITER, mRepeat.iter);
 			    	i.putExtra(RepeatSchedule.KEY_FREQ, mRepeat.freq);
 			    	i.putExtra(RepeatSchedule.KEY_CUSTOM, mRepeat.custom);
 			    	i.putExtra(RepeatSchedule.KEY_DATE, (mRepeat.end != null) ? mRepeat.end.getTime() : 0);
 			    	startActivityForResult(i, 0);
 				}
 				
 				mDefaultRepeatValue = pos;
 			}
 	
 			public void onNothingSelected(AdapterView<?> adapter) { }
 		};
 	
 	private void setDateEdit(Date date)
 	{
 		Calendar cal = Calendar.getInstance();
 		if (date != null)
 			cal.setTime(date);
 
 		mDate = date;
 		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
 		dateEdit.setText(df.format(cal.getTime()));
 	}
 	
 	private Date parseDateEdit()
 	{
 		Date date;
 		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT);
 		
 		try
 		{
 			date = df.parse(dateEdit.getText().toString());
 		}
 		catch (ParseException e)
 		{
 			// set the date to today if there's a parsing error
 			date = new Date();
 		}
 		date.setHours(0);
 		date.setMinutes(0);
 		date.setSeconds(0);
 		
 		return date;
 	}
 	
 	private int[] dateEditToYMD()
 	{
 		int[] ymd = new int[3];
 		Date date = parseDateEdit();
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(date);
 		ymd[0] = cal.get(Calendar.YEAR);
 		ymd[1] = cal.get(Calendar.MONTH);
 		ymd[2] = cal.get(Calendar.DAY_OF_MONTH);
 		
 		return ymd;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		int[] ymd = dateEditToYMD();
 		return new DatePickerDialog(this, mDateSetListener, ymd[0], ymd[1], ymd[2]);
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog)
 	{
 		int[] ymd = dateEditToYMD();
 		((DatePickerDialog)dialog).updateDate(ymd[0], ymd[1], ymd[2]);
 	}
 
 	private DatePickerDialog.OnDateSetListener mDateSetListener =
         new DatePickerDialog.OnDateSetListener()
 		{
             public void onDateSet(DatePicker view, int year, int month,  int day)
             {
             	Calendar cal = Calendar.getInstance();
             	cal.set(Calendar.YEAR, year);
             	cal.set(Calendar.MONTH, month);
             	cal.set(Calendar.DAY_OF_MONTH, day);
             	setDateEdit(cal.getTime());
             }
         };
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (resultCode == RESULT_OK)
 		{
 			Bundle extras = data.getExtras();
 			mRepeat.iter = extras.getInt(RepeatSchedule.KEY_ITER);
 			mRepeat.freq = extras.getInt(RepeatSchedule.KEY_FREQ);
 			mRepeat.custom = extras.getInt(RepeatSchedule.KEY_CUSTOM);
 			mRepeat.end = new Date(extras.getLong(RepeatSchedule.KEY_DATE));
 			
 			setRepeatSpinnerSelection(mRepeat);
 		}
 		else
 		{
 			mDefaultRepeatValue = mLastRepeatValue;
 			if (mDefaultRepeatValue == 7)
 				setRepeatSpinnerSelection(mRepeat);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void setRepeatSpinnerSelection(RepeatSchedule repeat)
 	{
 		int spinner_num = 7;
 		
 		switch (repeat.iter)
 		{
 		case RepeatSchedule.NO_REPEAT:
 			spinner_num = 0;
 			break;
 		
 		case RepeatSchedule.DAILY:
 			if (repeat.freq == 1 && (repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 1;
 			break;
 		
 		case RepeatSchedule.WEEKLY:
 			if (repeat.freq == 1 && repeat.custom == 0 &&
 					(repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 2;
 			else if (repeat.freq == 2 && repeat.custom == 0 &&
 					(repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 3;
 			break;
 		
 		case RepeatSchedule.MONTHLY:
 			if (repeat.freq == 1 && repeat.custom == RepeatSchedule.DATE &&
 					(repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 4;
 			break;
 
 		case RepeatSchedule.YEARLY:
 			if (repeat.freq == 1 && (repeat.end == null || repeat.end.getTime() <= 0))
 				spinner_num = 5;
 			break;
 		}
 		
 		if (spinner_num == 7)
 		{
 			mRepeatAdapter = (ArrayAdapter<String>)repeatSpinner.getAdapter();
 			if (mRepeatAdapter.getCount() <= 7)
 				mRepeatAdapter.add("Custom");
 		}
 		
 		mDefaultRepeatValue = spinner_num;
 	}
 
 	private void showTransactionFields()
 	{
 		// set the check radio to enable/disable and automatically populate the check entry field
 		checkRadio.setOnCheckedChangeListener( new RadioButton.OnCheckedChangeListener()
 		{
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				checkEdit = (EditText)findViewById(R.id.checkEdit);
 				checkEdit.setKeyListener(new CurrencyKeyListener());
 
 				if (isChecked)
 				{
 					if (checkEdit.getText().toString().equals(""))
 					{
 						// autopopulate the edit with the next check number
 						Account acct = Account.getAccountById(mAccountId);
 						int check_num = acct.getNextCheckNum();
 						checkEdit.setText(new Integer(check_num).toString());
 					}
 				}
 				checkEdit.setEnabled(isChecked);
 			}
 		});
 		
 		partyEdit = (AutoCompleteTextView)findViewById(R.id.partyEdit);
 		checkEdit = (EditText)findViewById(R.id.checkEdit);
		checkEdit.setEnabled(false);
 		
 		// set the autocompletion values for partyEdit
 		String[] parties = Transaction.getAllParties();
 		if (parties == null)
 			parties = new String[0];
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
 				android.R.layout.simple_dropdown_item_1line, parties);
 		partyEdit.setAdapter(adapter);
 	}
 	
 	private ArrayAdapter<CharSequence> showTransferFields()
 	{
 		// if we're showing a transfer window, hide the check button, check field, and party field
 		checkRadio.setVisibility(RadioButton.GONE);
 		
 		TableRow row = (TableRow)findViewById(R.id.partyRow);
 		row.setVisibility(TableRow.GONE);
 		row = (TableRow)findViewById(R.id.checkRow);
 		row.setVisibility(TableRow.GONE);
 		row = (TableRow)findViewById(R.id.accountRow);
 		row.setVisibility(TableRow.VISIBLE);
 		
 		accountSpinner = (Spinner)findViewById(R.id.accountSpinner);
 		String[] names = Account.getAccountNames();
 		
 		// if there is only one account in the database, tell the user they can't transfer and cancel
 		if (names.length == 1)
 		{
 			setResult(mFinishIntent);
 			finish();
 			return null;
 		}
 		
 		String[] acctNames = new String[names.length - 1];
 		
 		Account acct = Account.getAccountById(mAccountId);
 		int i = 0;
 		for ( String name : names )
 			if (!name.equalsIgnoreCase(acct.name))
 				acctNames[i++] = name;
 		
 		ArrayAdapter<CharSequence> adapter = new ArrayAdapter<CharSequence>(this,
 				android.R.layout.simple_spinner_item, acctNames);
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         accountSpinner.setAdapter(adapter);
         
         return adapter;
 	}
 
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		try
 		{
 			saveState();
 		}
 		catch (Exception e)
 		{
 			Logger.logStackTrace(e, this);
 		}
 	}
 
 	private void saveState() throws Exception
 	{
 		if (mFinishIntent == RESULT_CANCELED || mFinished)
 			return;
 
 		Transaction trans;
 		Account acct2 = null;
 
 		if (mTransId != 0)
 			trans = mTrans;
 		else
 		{
 			trans = new Transaction();
 		}
 
 		if (mType == TransactionActivity.TRANSACTION)
 			trans.party = partyEdit.getText().toString();
 		else
 			acct2 = Account.getAccountByName((String)accountSpinner.getSelectedItem());
 		
 		// clear the list so we don't write tags leftover from loading the transaction
 		trans.tags.clear();
 		trans.addTags(tagsEdit.getText().toString());
 		
 		// get the date of the transaction and set time values to 0
 		trans.date = parseDateEdit();
 		
 		// get the amount of the transaction
 		try
 		{
 			DecimalFormatSymbols dfs = new DecimalFormatSymbols();
 			char sep = dfs.getMonetaryDecimalSeparator();
 			
 			String str = amountEdit.getText().toString();
 			if (sep != '.')
 				str = str.replaceAll(String.valueOf(sep), ".");
 			trans.amount = new Double(str);
 		}
 		catch (NumberFormatException e)
 		{
 			trans.amount = 0.0;
 		}
 
 		// get the type of transaction
 		if (checkRadio.isChecked())
 		{
 			trans.type = Transaction.CHECK;
 
 			try
 			{
 				trans.check_num = new Integer(checkEdit.getText().toString());
 			}
 			catch (NumberFormatException e)
 			{
 				trans.type = Transaction.WITHDRAW;
 				trans.check_num = 0;
 			}
 		}
 		else if (withdrawRadio.isChecked())
 		{
 			trans.type = Transaction.WITHDRAW;
 		}
 		else
 		{
 			trans.type = Transaction.DEPOSIT;
 		}
 		
 		// get if it's a budget transaction
 		trans.budget = budgetRadio.isChecked();
 		if (trans.budget && trans.id() != -1 && trans.isPosted())
 		{
 			trans.post(false);
 			trans.budget = true; // set to true because trans.post() sets it to false
 		}
 
 		setRepeat();
 		
 		int id = -1;
 		if (mType == TransactionActivity.TRANSACTION)
 			id = trans.write(mAccountId);
 		else
 		{
 			trans.account = mAccountId;
 			id = trans.transfer(acct2);
 		}
 		
 		mFinished = true;
 		if (id != -1)
 		{
 			// write the repeat schedule if it's not set to NO_REPEAT
 			if (mRepeat.iter != RepeatSchedule.NO_REPEAT || mRepeat.id() > 0)
 			{
 				mRepeat.start = trans.date;
 				mRepeat.write(id);
 			}
 			
 			mTransId = id;
 			Intent i = new Intent();
 			Bundle b = new Bundle();
 			b.putInt(Transaction.KEY_ID, mTransId);
 			b.putInt(TransactionActivity.KEY_REQ, mRequest);
 			i.putExtras(b);
 			setResult(mFinishIntent, i);
 		}
 		else
 		{
 			setResult(RESULT_CANCELED);
 		}
 
 		finish();
 	}
 	
 	private void setRepeat()
 	{
 		// set repeat values
 		switch (repeatSpinner.getSelectedItemPosition())
 		{
 		// No Repeat
 		case 0:
 			mRepeat.iter = RepeatSchedule.NO_REPEAT;
 			break;
 		
 		// Daily
 		case 1:
 			mRepeat.iter = RepeatSchedule.DAILY;
 			mRepeat.freq = 1;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 			
 		// Weekly
 		case 2:
 			mRepeat.iter = RepeatSchedule.WEEKLY;
 			mRepeat.freq = 1;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 			
 		// Bi-weekly
 		case 3:
 			mRepeat.iter = RepeatSchedule.WEEKLY;
 			mRepeat.freq = 2;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 			
 		// Monthly
 		case 4:
 			mRepeat.iter = RepeatSchedule.MONTHLY;
 			mRepeat.freq = 1;
 			mRepeat.custom = RepeatSchedule.DATE;
 			mRepeat.end = null;
 			break;
 			
 		// Yearly
 		case 5:
 			mRepeat.iter = RepeatSchedule.YEARLY;
 			mRepeat.freq = 1;
 			mRepeat.custom = 0;
 			mRepeat.end = null;
 			break;
 		
 		// if it's past position 5, mRepeat has already been set
 		}
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		populateFields();
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState)
 	{
 		super.onSaveInstanceState(outState);
 		outState.putInt(TransactionActivity.KEY_REQ, mRequest);
 		outState.putInt(TransactionActivity.KEY_TYPE, mType);
 		if (mAccountId > 0)
 			outState.putInt(Account.KEY_ID, mAccountId);
 		if (mTransId > 0)
 			outState.putInt(Transaction.KEY_ID, mTransId);
 		
 		setRepeat();
 		outState.putInt(RepeatSchedule.KEY_ITER, mRepeat.iter);
 		outState.putInt(RepeatSchedule.KEY_FREQ, mRepeat.freq);
 		outState.putInt(RepeatSchedule.KEY_CUSTOM, mRepeat.custom);
 		long end = (mRepeat.end == null ? 0 : mRepeat.end.getTime());
 		outState.putLong(RepeatSchedule.KEY_DATE, end);
 		
 		if (mDate != null)
 			outState.putLong(Transaction.KEY_DATE, mDate.getTime());
 		if (mType == TransactionActivity.TRANSFER)
 			outState.putInt(TransactionEdit.KEY_TRANSFER, accountSpinner.getSelectedItemPosition());
 	}
 }
