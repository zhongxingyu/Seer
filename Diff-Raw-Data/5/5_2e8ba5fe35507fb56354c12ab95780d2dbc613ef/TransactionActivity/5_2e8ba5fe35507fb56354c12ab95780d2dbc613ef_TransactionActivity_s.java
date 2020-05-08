 package net.gumbercules.loot.transaction;
 
 import java.text.NumberFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 
 import net.gumbercules.loot.R;
 import net.gumbercules.loot.account.Account;
 import net.gumbercules.loot.backend.Database;
 import net.gumbercules.loot.backend.Logger;
 import net.gumbercules.loot.preferences.SettingsActivity;
 import net.gumbercules.loot.premium.PremiumCaller;
 import net.gumbercules.loot.premium.ViewImage;
 import net.gumbercules.loot.repeat.RepeatManagerActivity;
 import net.gumbercules.loot.repeat.RepeatSchedule;
 import android.app.AlertDialog;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.StateListDrawable;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.text.Editable;
 import android.text.SpannableString;
 import android.text.Spanned;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.DatePicker;
 import android.widget.ImageButton;
 import android.widget.LinearLayout;
 import android.widget.ListView;
 import android.widget.MultiAutoCompleteTextView;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.MultiAutoCompleteTextView.Tokenizer;
 
 public class TransactionActivity extends ListActivity
 {
 	public static final String KEY_REQ		= "t_req";
 	public static final String KEY_CHANGES	= "t_changes";
 	public static final String KEY_IDS		= "t_ids";
 	public static final String KEY_TYPE		= "t_type";
 	
 	public static final int ACTIVITY_CREATE	= 0;
 	public static final int ACTIVITY_EDIT	= 1;
 	public static final int ACTIVITY_DEL	= 2;
 	
 	public static final int TRANSACTION		= 0;
 	public static final int TRANSFER		= 1;
 	
 	public static final int NEW_TRANSACT_ID	= Menu.FIRST;
 	public static final int NEW_TRANSFER_ID	= Menu.FIRST + 1;
 	public static final int SORT_ID			= Menu.FIRST + 2;
 	public static final int SEARCH_ID		= Menu.FIRST + 3;
 	public static final int PURGE_ID		= Menu.FIRST + 4;
 	public static final int EXPORT_ID		= Menu.FIRST + 5;
 	public static final int SETTINGS_ID		= Menu.FIRST + 6;
 	public static final int CHART_ID		= Menu.FIRST + 7;
 	public static final int IMPORT_ID		= Menu.FIRST + 8;
 	public static final int RMANAGER_ID		= Menu.FIRST + 9;
 	
 	public static final int CONTEXT_EDIT	= Menu.FIRST;
 	public static final int CONTEXT_COPY	= Menu.FIRST + 1;
 	public static final int CONTEXT_POST	= Menu.FIRST + 2;
 	public static final int CONTEXT_DEL		= Menu.FIRST + 3;
 	public static final int CONTEXT_REPEAT	= Menu.FIRST + 4;
 	public static final int CONTEXT_IMAGE	= Menu.FIRST + 5;
 	
 	private static ArrayList<Transaction> mTransList;
 	private static Account mAcct;
 	private static TransactionAdapter mTa;
 	
 	private MultiAutoCompleteTextView searchEdit;
 	
 	private TextView budgetValue;
 	private TextView balanceValue;
 	private TextView postedValue;
 	
 	private static boolean showSearch = false;
 	private static String searchString = "";
 	
 	private boolean new_account;
 	private boolean mLargeFonts;
 	
 	private Bundle mCurrentBundle;
 	
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
     	super.onCreate(savedInstanceState);
 
     	createContent();
     }
     
     private void createContent()
     {
 		int layoutResId = setContent();
 		
 		Bundle bun = getIntent().getExtras();
     	int acct_id = bun.getInt(Account.KEY_ID);
     	new_account = false;
     	if (mAcct == null || acct_id != mAcct.id())
     	{
     		new_account = true;
     		mAcct = Account.getAccountById(acct_id);
     	}
     	setTitle("loot :: " + mAcct.name);
 
     	int auto_purge = (int)Database.getOptionInt("auto_purge_days");
     	if (auto_purge > 0)
     	{
     		Calendar cal = Calendar.getInstance();
     		cal.add(Calendar.DAY_OF_YEAR, -auto_purge);
     		int[] ids = mAcct.purgeTransactions(cal.getTime());
     		if (ids != null && mTa != null && !mTa.isEmpty())
     		{
     			mTa.remove(ids);
     		}
     	}
 
 		budgetValue = (TextView)findViewById(R.id.budgetValue);
 		balanceValue = (TextView)findViewById(R.id.balanceValue);
 		postedValue = (TextView)findViewById(R.id.postedValue);
 
     	// add a listener to filter the list whenever the text changes
     	searchEdit = (MultiAutoCompleteTextView)findViewById(R.id.SearchEdit);
     	
     	// add a listener to clear searchEdit when pressed
     	ImageButton clearButton = (ImageButton)findViewById(R.id.ClearButton);
     	clearButton.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			public void onClick(View v)
 			{
 				searchEdit.setText("");
 			}
     	});
 
 		if (new_account)
     	{
 	    	mTransList = new ArrayList<Transaction>();
 		    mTa = new TransactionAdapter(this, layoutResId, mTransList, mAcct.id());
 	        setListAdapter(mTa);
 	        fillList();
     	}
     	else
     	{
     		mTa.setResource(layoutResId);
     		mTa.setContext(this);
     		setListAdapter(mTa);
     		mTa.updatePreferenceValues();
     		nullifyChildBackgrounds();
     		setBalances();
     	}
 
 		final TransactionAdapter.TransactionFilter filter = 
 			(TransactionAdapter.TransactionFilter)mTa.getFilter();
     	TextWatcher searchChanged = new TextWatcher()
     	{
     		// we only care what the end result is
 			public void afterTextChanged(Editable s)
 			{
 				searchString = s.toString();
 				filter.publish(searchString, filter.filtering(searchString));
 			}
 
 			public void beforeTextChanged(CharSequence s, int start, int count, int after)
 			{
 				if (after == 0)
 				{
 					searchString = "";
 					filter.publish(searchString, filter.filtering(searchString));
 				}
 			}
 
 			public void onTextChanged(CharSequence s, int start, int before, int count) { }
     	};
     	searchEdit.addTextChangedListener(searchChanged);
     	
     	setupActionBar();
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	
     	ListView view = getListView();
         registerForContextMenu(view);
         
         if (!prefs.getBoolean("top_sort", false))
         {
         	view.setStackFromBottom(true);
         }
         
         CheckBox cb = (CheckBox)findViewById(R.id.show_posted);
         cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
         {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				filter.setShowPosted(isChecked);
 				filter.publish(searchString, filter.filtering(searchString));
 			}
         });
         
         cb = (CheckBox)findViewById(R.id.show_non_posted);
         cb.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener()
         {
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 				filter.setShowNonPosted(isChecked);
 				filter.publish(searchString, filter.filtering(searchString));
 			}
         });
 
         // show the search if the orientation has changed and the activity has restarted
     	if (showSearch)
     	{
     		toggleSearch();
 			searchEdit.setText(searchString);
 			filter.publish(searchString, filter.filtering(searchString));
     	}
     }
 
 	private StateListDrawable createSLD()
 	{
 		StateListDrawable sld = new StateListDrawable();
 		
 		final int stateFocused = android.R.attr.state_focused;
 		final int stateSelected = android.R.attr.state_selected;
 		final int statePressed = android.R.attr.state_pressed;
 		final int stateActive = android.R.attr.state_active;
 		final int stateEnabled = android.R.attr.state_enabled;
 		final int stateWFocused = android.R.attr.state_window_focused;
 		
 		Bitmap bmp = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
 		bmp.setPixel(0, 0, Color.argb(85, 255, 130, 35));
 		final Drawable focusedDrawable = new BitmapDrawable(getResources(),
 				bmp.copy(Bitmap.Config.ARGB_8888, true));
 		bmp.setPixel(0, 0, Color.argb(85, 255, 159, 104));
 		final Drawable pressedDrawable = new BitmapDrawable(getResources(),
 				bmp.copy(Bitmap.Config.ARGB_8888, true));
 		bmp.setPixel(0, 0, Color.argb(0, 0, 0, 0));
 		final Drawable defaultDrawable = new BitmapDrawable(getResources(),
 				bmp.copy(Bitmap.Config.ARGB_8888, true));
 		
 		sld.addState(new int[] { stateWFocused, stateActive }, pressedDrawable);
 		sld.addState(new int[] { stateWFocused, statePressed }, pressedDrawable);
 		sld.addState(new int[] { stateWFocused, stateEnabled, statePressed }, pressedDrawable);
 		sld.addState(new int[] { stateWFocused, stateFocused, stateSelected, statePressed }, pressedDrawable);
 		sld.addState(new int[] { stateWFocused, stateFocused }, focusedDrawable);
 		sld.addState(new int[] { stateWFocused, stateSelected }, focusedDrawable);
 		sld.addState(new int[] { stateWFocused, stateFocused, stateSelected }, focusedDrawable);
 		sld.addState(new int[] { stateWFocused, stateEnabled, stateSelected }, focusedDrawable);
 		sld.addState(new int[] { stateWFocused, stateEnabled }, defaultDrawable);
 		sld.addState(new int[] { stateEnabled }, defaultDrawable);
 		
 		return sld;
 	}
     
     private void nullifyChildBackgrounds()
     {
     	ListView lv = getListView();
     	int child_count = lv.getChildCount();
     	for (int i = 0; i < child_count; ++i)
     	{
     		lv.getChildAt(i).setBackgroundDrawable(null);
     	}
     }
     
     @Override
 	protected void onResume()
 	{
 		super.onResume();
 		
 		int scroll_pos = -1;
 		
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	boolean new_large_fonts = prefs.getBoolean("large_fonts", false); 
     	if (mLargeFonts != new_large_fonts)
     	{
     		createContent();
     	}
 
         // only mess with the selector if we're drawing the background
         if (prefs.getBoolean("color_background", true))
         {
     		ListView view = getListView();
         	if (!prefs.getBoolean("color_bg_side", false))
         	{
 	        	view.setSelector(createSLD());
 	        	view.setDrawSelectorOnTop(true);
         	}
         	else
         	{
         		ListView temp = new ListView(this);
         		view.setSelector(temp.getSelector());
         		temp = null;
         		view.setDrawSelectorOnTop(false);
         	}
         }
     	
     	if (mCurrentBundle != null)
     	{
 			try
 			{
 				Bundle extras = mCurrentBundle;
 				if (extras.containsKey(Transaction.KEY_ID))
 				{
 					int trans_id = extras.getInt(Transaction.KEY_ID);
 					scroll_pos = updateList(trans_id, extras.getInt(KEY_REQ));
 				}
 				else if (extras.containsKey(KEY_CHANGES))
 				{
 					if (extras.getBoolean(KEY_CHANGES))
 					{
 						ArrayList<Integer> list = extras.getIntegerArrayList(KEY_IDS);
 						int[] array = new int[list.size()];
 						for (int i = list.size() - 1; i >= 0; --i)
 						{
 							array[i] = list.get(i);
 						}
 						updateList(array, ACTIVITY_CREATE);
 						scroll_pos = mTa.getPosition(Transaction.getTransactionById(array[0]));
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				Logger.logStackTrace(e, this);
 			}
     	}
     	
     	if (scroll_pos == -1 && new_account)
     	{
     		Date d = new Date();
     		d.setHours(0);
     		d.setMinutes(0);
     		d.setSeconds(0);
     		scroll_pos = mTa.findItemByDate(d);
     	}
     	
     	if (scroll_pos != -1)
     	{
     		final ListView lv = getListView();
     		final int pos = scroll_pos >= 1 ? scroll_pos - 1 : 0;
     		Thread thrd = new Thread()
     		{
     			@Override
     			public void run()
     			{
     				try
 					{
 						Thread.sleep(1000);
 					}
 					catch (InterruptedException e)
 					{
 						e.printStackTrace();
 					}
     				lv.setSelection(pos);
     			}
     		};
     		
     		if (new_account)
     		{
     			runOnUiThread(thrd);
     		}
     		else
     		{
     			lv.setSelection(pos);
     		}
     	}
 
 		new_account = false;
 		mCurrentBundle = null;
 	}
 
 	private int setContent()
     {
     	// find current orientation and send proper layout to constructor
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	int orientation = getResources().getConfiguration().orientation;
     	int layoutResId;
     	
     	if (prefs.getBoolean("large_fonts", false))
     	{
     		mLargeFonts = true;
     		setContentView(R.layout.main_large);
     		
         	if (orientation == Configuration.ORIENTATION_LANDSCAPE)
         	{
         		layoutResId = R.layout.trans_row_wide_large;
         	}
         	else
         	{
         		layoutResId = R.layout.trans_row_narrow_large;
         	}
     	}
     	else
     	{
     		mLargeFonts = false;
     		setContentView(R.layout.main);
 
     		if (orientation == Configuration.ORIENTATION_LANDSCAPE)
         	{
         		layoutResId = R.layout.trans_row_wide;
         	}
         	else
         	{
         		layoutResId = R.layout.trans_row_narrow;
         	}
     	}
     	
     	return layoutResId;
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	boolean result = super.onCreateOptionsMenu(menu);
     	menu.add(0, SORT_ID, 0, R.string.sort)
     		.setShortcut('1', 'o')
     		.setIcon(android.R.drawable.ic_menu_sort_by_size);
     	menu.add(0, PURGE_ID, 0, R.string.purge)
     		.setShortcut('2', 'p')
     		.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
     	menu.add(0, RMANAGER_ID, 0, R.string.repeat_manager)
     		.setShortcut('3', 'm')
     		.setIcon(android.R.drawable.ic_menu_recent_history);
     	menu.add(0, SETTINGS_ID, 0, R.string.settings)
     		.setShortcut('4', 's')
     		.setIcon(android.R.drawable.ic_menu_preferences);
     	
     	return result;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	switch (item.getItemId())
     	{
     	case SORT_ID:
     		sortDialog();
     		return true;
     		
     	case PURGE_ID:
     		purgeDialog();
     		return true;
     		
     	case SETTINGS_ID:
     		showSettings();
     		return true;
     		
     	case RMANAGER_ID:
     		Intent i = new Intent(this, RepeatManagerActivity.class);
     		i.putExtra(RepeatManagerActivity.ID_ACCOUNT, mAcct.id());
     		startActivity(i);
     		return true;
     	}
     	
     	return super.onOptionsItemSelected(item);
     }
     
     private void setupActionBar()
     {
     	final PremiumCaller pc = new PremiumCaller(this);
     	ImageButton button = (ImageButton)findViewById(R.id.new_transaction_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				createTransaction();
 			}
 		});
     	button = (ImageButton)findViewById(R.id.new_transfer_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				createTransfer();
 			}
 		});
     	// disable the transfer button if there is only one account
    	if (Account.getAccountIds().length <= 1)
     	{
     		button.setEnabled(false);
     	}
     	
     	button = (ImageButton)findViewById(R.id.search_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				toggleSearch();
 			}
 		});
     	button = (ImageButton)findViewById(R.id.import_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				pc.showActivity(PremiumCaller.IMPORT, mAcct.id(), ACTIVITY_CREATE);
 			}
 		});
     	button = (ImageButton)findViewById(R.id.export_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				pc.showActivity(PremiumCaller.EXPORT, mAcct.id());
 			}
 		});
     	button = (ImageButton)findViewById(R.id.chart_button);
     	button.setOnClickListener(new ImageButton.OnClickListener()
     	{
 			@Override
 			public void onClick(View v)
 			{
 				pc.showActivity(PremiumCaller.CHART, mAcct.id());
 			}
 		});
     }
     
 	private void showSettings()
 	{
 		Intent i = new Intent(this, SettingsActivity.class);
 		startActivityForResult(i, 0);
 	}
 
 	private void toggleSearch()
     {
 		LinearLayout searchLayout = (LinearLayout)findViewById(R.id.SearchLayout);
 		int new_vis = LinearLayout.VISIBLE;
 		int cur_vis = searchLayout.getVisibility();
 		
 		// if it is currently visible, set it to gone
 		if (new_vis == cur_vis)
 		{
 			new_vis = LinearLayout.GONE;
 			searchEdit.setText("");
 			showSearch = false;
 		}
 		else
 		{
 			showSearch = true;
 			searchEdit.requestFocus();
 			
 			// set the adapter each time to get new data for the autocomplete
 			String[] strings = Transaction.getAllStrings();
 			if (strings == null)
 				strings = new String[0];
 			ArrayAdapter<String> searchAdapter = new ArrayAdapter<String>(this,
 	    			android.R.layout.simple_dropdown_item_1line, strings);
 			searchEdit.setAdapter(searchAdapter);
 			searchEdit.setTokenizer(new SpaceTokenizer());
 		}
 		searchLayout.setVisibility(new_vis);
     }
     
     private void sortDialog()
     {
     	new AlertDialog.Builder(this)
     		.setTitle(R.string.sort_column)
     		.setItems(R.array.sort, new DialogInterface.OnClickListener()
     		{
     			public void onClick(DialogInterface dialog, int which)
     			{ 
     				if (which == 0)			// Date
     					Transaction.setComparator(Transaction.COMP_DATE);
     				else if (which == 1)	// Party
     					Transaction.setComparator(Transaction.COMP_PARTY);
     				else if (which == 2)	// Amount
     					Transaction.setComparator(Transaction.COMP_AMT);
     		    	mTa.sort();
     			}
     		})
     		.show();
     }
 
     private void purgeDialog()
     {
     	final Context context = (Context)this;
 		AlertDialog dialog = new AlertDialog.Builder(this)
 			.setTitle(R.string.account_del_box)
 			.setItems(R.array.purge, new DialogInterface.OnClickListener()
 			{
 				public void onClick(DialogInterface dialog, int which)
 				{
 					final int item = which;
 					DatePickerDialog.OnDateSetListener dateSetListener =
 				        new DatePickerDialog.OnDateSetListener()
 						{
 				            public void onDateSet(DatePicker view, int year, int month,  int day)
 				            {
 								Calendar cal = Calendar.getInstance();
 								cal.set(Calendar.HOUR_OF_DAY, 23);
 								cal.set(Calendar.MINUTE, 59);
 								cal.set(Calendar.SECOND, 59);
 				            	cal.set(Calendar.YEAR, year);
 				            	cal.set(Calendar.MONTH, month);
 				            	cal.set(Calendar.DAY_OF_MONTH, day);
 				            	Date date = cal.getTime();
 				            	
 				            	switch (item)
 				            	{
 				            	case 0:	// purge
 				            		int[] purged = mAcct.purgeTransactions(date);
 				            		if (purged != null)
 			            				updateList(purged, ACTIVITY_DEL);
 				            		break;
 				            		
 				            	case 1:	// restore
 				            		int[] restored = mAcct.restorePurgedTransactions(date);
 				            		if (restored != null)
 			            				updateList(restored, ACTIVITY_CREATE);
 				            		break;
 				            		
 				            	case 2:	// clear
 				            		mAcct.deletePurgedTransactions(date);
 				            		break;
 				            	}
 				            }
 				        };
 				        
 				    String title = "";
 				    if (item == 0)
 				    	title = "Purge Through";
 				    else if (item == 1)
 				    	title = "Restore Through";
 				    else if (item == 2)
 				    	title = "Clear Through";
 				    
 				    Calendar cal = Calendar.getInstance();
 				    DatePickerDialog pickerDialog = new DatePickerDialog(context, dateSetListener,
 				    		cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
 				    pickerDialog.setTitle(title);
 				    pickerDialog.show();
 				}
 			})
 			.create();
 		dialog.show();
     }
     
 	private void createTransaction()
     {
     	Intent i = new Intent(this, TransactionEdit.class);
     	int request = ACTIVITY_CREATE;
     	i.putExtra(TransactionActivity.KEY_REQ, request);
     	i.putExtra(TransactionActivity.KEY_TYPE, TRANSACTION);
     	i.putExtra(Account.KEY_ID, mAcct.id());
     	startActivityForResult(i, request);
     }
     
     private void createTransfer()
     {
     	Intent i = new Intent(this, TransactionEdit.class);
     	int request = ACTIVITY_CREATE;
     	i.putExtra(TransactionActivity.KEY_REQ, request);
     	i.putExtra(TransactionActivity.KEY_TYPE, TRANSFER);
     	i.putExtra(Account.KEY_ID, mAcct.id());
     	startActivityForResult(i, request);
     }
     
     private void editTransaction(int id)
     {
     	Intent i = new Intent(this, TransactionEdit.class);
     	int request = ACTIVITY_EDIT;
     	i.putExtra(Transaction.KEY_ID, id);
     	i.putExtra(Account.KEY_ID, mAcct.id());
     	i.putExtra(TransactionActivity.KEY_REQ, request);
     	startActivityForResult(i, request);
     }
     
     public void setBalances()
     {
     	Double posted = mAcct.calculatePostedBalance();
     	Double balance = mAcct.calculateActualBalance();
     	Double budget = mAcct.calculateBudgetBalance();
     	
 		// change the numbers to the locale currency format
 		NumberFormat nf = NumberFormat.getCurrencyInstance();
 		String new_currency = Database.getOptionString("override_locale");
 		if (new_currency != null && !new_currency.equals(""))
 		{
 			nf.setCurrency(Currency.getInstance(new_currency));
 		}
 		
 		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
 		boolean color = prefs.getBoolean("color_balance", true);
 		
 		setBalance(postedValue, posted, color, nf);
 		setBalance(balanceValue, balance, color, nf);
 		setBalance(budgetValue, budget, color, nf);
     }
     
     private void setBalance(TextView view, Double bal, boolean color, NumberFormat nf)
     {
     	String str = "Error";
     	if (bal != null)
     	{
     		str = nf.format(bal);
     	}
     	
     	view.setText(str);
     	
     	int textColor = Color.LTGRAY;
     	
     	if (color)
     	{
 			final int red = Color.rgb(255, 50, 50);
 			if (mAcct.credit)
 			{
 				if (bal > mAcct.creditLimit)
 				{
 					textColor = red;
 				}
 				else if (bal >= (mAcct.creditLimit * 0.9))
 				{
 					textColor = Color.YELLOW;
 				}
 			}
 			else
 			{
 				if (bal < 0.0)
 				{
 					textColor = red;
 				}
 			}
     	}
     	
     	view.setTextColor(textColor);
     }
     
     private void fillList(boolean repeat)
     {
     	if (repeat)
     	{
     		addRepeatedTransactions();
     	}
 
     	mTa.clear();
     	mTa.updatePreferenceValues();
 		nullifyChildBackgrounds();
     	mTa.add(mAcct.getTransactions());
 		mTa.sort();
 		mTa.calculateRunningBalances();
 		mTa.notifyDataSetChanged();
 		
 		setBalances();
     }
 
     private void fillList()
     {
     	fillList(true);
     }
     
     private int updateList(int trans_id, int request)
     {
     	addRepeatedTransactions();
     	TransactionAdapter ta = mTa;
     	Transaction trans;
     	int pos = 0;
     	
     	switch (request)
     	{
     	case ACTIVITY_EDIT:
     		pos = ta.findItemById(trans_id);
     		ta.remove(ta.getItem(pos));
     		// don't break, the transaction needs to be added back to the list
 
     	case ACTIVITY_CREATE:
     		trans = Transaction.getTransactionById(trans_id);
     		ta.add(trans);
     		ta.sort();
     		pos = ta.findItemNoFilter(trans_id);
     		break;
     		
     	case ACTIVITY_DEL:
     		pos = ta.findItemById(trans_id);
     		ta.remove(ta.getItem(pos));
     		break;
     	}
 
     	ta.calculateRunningBalances(pos);
 		setBalances();
 		
 		return pos;
     }
     
     private void updateList(int[] ids, int request)
     {
     	addRepeatedTransactions();
     	TransactionAdapter ta = mTa;
     	
     	switch (request)
     	{
     	case ACTIVITY_CREATE:
     		ta.add(ids);
     		ta.sort();
     		break;
     		
     	case ACTIVITY_DEL:
     		ta.remove(ids);
     		break;
     	}
 
     	ta.calculateRunningBalances();
 		setBalances();
     }
     
     private void addRepeatedTransactions()
     {
    		int[] ids = RepeatSchedule.processDueRepetitions(new Date());
    		
    		if (ids != null && ids.length != 0)
    		{
    			if (ids[0] == -1)
    			{
 	   			// if there is a -1 in the id list, something went wrong processing
 	   			// the repetition, and the schedule was deleted
 	   			Toast.makeText(this, R.string.bad_repeat, Toast.LENGTH_LONG).show();
    			}
    	    	mTa.add(ids);
    	    	
    	    	// send a broadcast out for widgets
    	    	Intent i = new Intent("net.gumbercules.loot.intent.ACCOUNT_UPDATED", null);
    	    	sendBroadcast(i);
    		}
     }
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data)
 	{
 		super.onActivityResult(requestCode, resultCode, data);
 		
 		if (resultCode == RESULT_OK && data != null)
 		{
 			mCurrentBundle = data.getExtras();
 		}
 		
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
         getListView().setStackFromBottom(!prefs.getBoolean("top_sort", false));
         mTa.sort();
 		
 		mTa.updatePreferenceValues();
 		nullifyChildBackgrounds();
 		mTa.notifyDataSetChanged();
 	}
 	
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id)
 	{
 		postListItem(position);
 	}
 
 	private int copyListItem(int id)
 	{
 		try
 		{
 			Transaction tr = Transaction.getTransactionById(id);
 			tr.setId(-1);
 			if (tr.type == Transaction.CHECK)
 				tr.check_num = mAcct.getNextCheckNum();
 			if (tr.isPosted())
 				tr.post(false);
 			id = tr.write(mAcct.id());
 			
 			Intent broadcast = new Intent("net.gumbercules.loot.intent.ACCOUNT_UPDATED", null);
 			broadcast.putExtra("account_id", mAcct.id());
 			sendBroadcast(broadcast);
 		}
 		catch (Exception e)
 		{
 			id = -1;
 			Logger.logStackTrace(e, this);
 		}
 		
 		return id;
 	}
 	
 	private void postListItem(int position)
 	{
 		// ListView.getChildAt only keeps track of visible children
 		// so we have to subtract the position of the first visible view
 		// in the ListAdapter from the position of the item we want
 		int vis = getListView().getFirstVisiblePosition();
 		View v = getListView().getChildAt(position - vis);
 		if (v != null)
 		{
 			CheckBox posted = (CheckBox)v.findViewById(R.id.PostedCheckBox);
 			if (posted != null)
 				posted.setChecked(!posted.isChecked());
 		}
 	}
 	
 	@Override
 	public boolean onContextItemSelected(MenuItem item)
 	{
 		AdapterView.AdapterContextMenuInfo info;
 		try
 		{
 			info = (AdapterContextMenuInfo)item.getMenuInfo();
 		}
 		catch (ClassCastException e)
 		{
 			Log.e(TransactionActivity.class.toString(), "Bad ContextMenuInfo", e);
 			return false;
 		}
 		final int pos = info.position;
 		int id = (int)getListAdapter().getItemId(pos);
 		switch (item.getItemId())
 		{
 		case CONTEXT_EDIT:
 			editTransaction(id);
 			return true;
 			
 		case CONTEXT_COPY:
 			id = copyListItem(id);
 			updateList(id, ACTIVITY_CREATE);
 			return true;
 			
 		case CONTEXT_POST:
 			postListItem(pos);
 			return true;
 			
 		case CONTEXT_REPEAT:
 			showDialog(id);
 			return true;
 			
 		case CONTEXT_DEL:
 			final Transaction trans = Transaction.getTransactionById(id);
 			final Context c = this;
 			AlertDialog dialog = new AlertDialog.Builder(this)
 				.setTitle(R.string.account_del_box)
 				.setMessage("Are you sure you wish to delete " + trans.party + "?")
 				.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int which)
 					{
 						try
 						{
 							int id = trans.id();
 							trans.erase();
 							updateList(id, ACTIVITY_DEL);
 						}
 						catch (Exception e)
 						{
 							Logger.logStackTrace(e, c);
 						}
 					}
 				})
 				.setNegativeButton(R.string.no, new DialogInterface.OnClickListener()
 				{
 					public void onClick(DialogInterface dialog, int which) { }
 				})
 				.create();
 			dialog.show();
 			
 			return true;
 			
 		case CONTEXT_IMAGE:
 			Intent i = new Intent(this, ViewImage.class);
 			Transaction t = (Transaction)getListAdapter().getItem(info.position);
 			i.putExtra(ViewImage.KEY_URIS, t.images);
 			startActivity(i);
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo)
 	{
 		AdapterView.AdapterContextMenuInfo info;
 		try
 		{
 			info = (AdapterContextMenuInfo)menuInfo;
 		}
 		catch (ClassCastException e)
 		{
 			Log.e(TransactionActivity.class.toString(), "Bad ContextMenuInfo", e);
 			return;
 		}
 		
 		if (info == null)
 		{
 			Log.e(TransactionActivity.class.toString(), "info == null");
 			return;
 		}
 
 		Transaction trans = (Transaction)getListAdapter().getItem(info.position);
 		if (trans == null)
 		{
 			return;
 		}
 		
 		menu.setHeaderTitle(trans.party);
 		
 		menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
 		menu.add(0, CONTEXT_COPY, 0, R.string.copy);
 		menu.add(0, CONTEXT_POST, 0, R.string.post);
 		menu.add(0, CONTEXT_REPEAT, 0, R.string.repeat);
 		if (trans.images != null && trans.images.size() > 0)
 		{
 			menu.add(0, CONTEXT_IMAGE, 0, R.string.view_receipts);
 		}
 		
 		menu.add(0, CONTEXT_DEL, 0, R.string.del);
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id)
 	{
 		return new RepeatDialog(this, id);
 	}
 	
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog)
 	{
 		((RepeatDialog)dialog).setTransId(id);
 	}
 
 	public static void setAccountNull()
 	{
 		mAcct = null;
 	}
 	
 	private class RepeatDialog extends Dialog
 	{
 		private ArrayAdapter<String> mAdapter;
 		private int mTransId;
 		
 		public RepeatDialog(Context context, int id)
 		{
 			super(context);
 			setContentView(R.layout.repeat_list);
 			ListView lv = (ListView)findViewById(R.id.repeat_list);
 			
 			this.setTitle("Repeat");
 			
 			final ArrayList<String> repeat =
 				new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.repeat_dialog)));
 			mAdapter = new ArrayAdapter<String>(getContext(),
 					android.R.layout.simple_expandable_list_item_1, repeat);
 			lv.setAdapter(mAdapter);
 			
 			lv.setOnItemClickListener(new OnItemClickListener()
 			{
 				public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
 				{
 					Transaction trans = Transaction.getTransactionById(copyListItem(mTransId));
 					if (trans == null)
 					{
 						dismiss();
 						return;
 					}
 					
 					trans.post(false);
 					trans.budget = true;
 					Calendar cal = Calendar.getInstance();
 					cal.setTime(trans.date);
 					int cal_field = -1, cal_value = 1;
 					
 					switch (pos)
 					{
 						case 0:
 							cal_field = Calendar.DATE;
 							break;
 
 						case 2:
 							cal_value = 2;
 							
 						case 1:
 							cal_field = Calendar.WEEK_OF_YEAR;
 							break;
 							
 						case 3:
 							cal_field = Calendar.MONTH;
 							break;
 							
 						case 4:
 							cal_field = Calendar.YEAR;
 							break;
 					}
 					
 					if (cal_field != -1)
 					{
 						cal.add(cal_field, cal_value);
 						trans.date = cal.getTime();
 						mTransId = trans.write(mAcct.id());
 						updateList(mTransId, ACTIVITY_CREATE);
 
 						Intent broadcast = new Intent("net.gumbercules.loot.intent.ACCOUNT_UPDATED", null);
 						broadcast.putExtra("account_id", trans.account);
 						sendBroadcast(broadcast);
 					}
 					dismiss();
 				}
 			});
 			
 			mTransId = id;
 		}
 		
 		public void setTransId(int t)
 		{
 			mTransId = t;
 		}
 	}
 	
 	public static class SpaceTokenizer implements Tokenizer
 	{
 		public int findTokenEnd(CharSequence text, int cursor)
 		{
 			int i = cursor;
 			int len = text.length();
 			
 			while (i < len)
 				if (text.charAt(i) == ' ')
 					return i;
 				else
 					++i;
 			
 			return len;
 		}
 
 		public int findTokenStart(CharSequence text, int cursor)
 		{
 			int i = cursor;
 			
 			while (i > 0 && text.charAt(i - 1) != ' ')
 				--i;
 			while (i < cursor && text.charAt(i) == ' ')
 				++i;
 			
 			return i;
 		}
 
 		public CharSequence terminateToken(CharSequence text)
 		{
 			int i = text.length();
 			
 			while (i > 0 && text.charAt(i -1) == ' ')
 				--i;
 			
 			if (i > 0 && text.charAt(i - 1) == ' ')
 				return text;
 			else
 			{
 				if (text instanceof Spanned)
 				{
 					SpannableString sp = new SpannableString(text + " ");
 					TextUtils.copySpansFrom((Spanned)text, 0, text.length(), Object.class, sp, 0);
 					return sp;
 				}
 				else
 					return text + " ";
 			}
 		}
 	}
 }
