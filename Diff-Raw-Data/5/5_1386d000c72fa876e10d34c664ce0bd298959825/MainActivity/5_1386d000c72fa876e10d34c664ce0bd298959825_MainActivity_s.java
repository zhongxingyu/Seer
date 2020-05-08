 package com.megginson.sloop.activities;
 
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.AlertDialog;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Loader;
 import android.content.SharedPreferences;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.view.PagerAdapter;
 import android.support.v4.view.ViewPager;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.megginson.sloop.R;
 import com.megginson.sloop.model.DataCollection;
 import com.megginson.sloop.model.DataEntry;
 import com.megginson.sloop.model.ValueFilter;
 import com.megginson.sloop.ui.AddressActionProvider;
 import com.megginson.sloop.ui.DataCollectionLoader;
 import com.megginson.sloop.ui.DataCollectionPagerAdapter;
 import com.megginson.sloop.ui.DataCollectionResult;
 
 /**
  * Sloop's main UI activity (browse a data set).
  * 
  * @author David Megginson
  */
 @SuppressLint("DefaultLocale")
 public class MainActivity extends FragmentActivity {
 
 	public final static String ACTION_FILTER = "com.megginson.sloop.intent.FILTER";
 
 	public final static String PARAM_URL = "url";
 
 	public final static String PARAM_ENTRY = "entry";
 
 	public final static String PARAM_FORCE_LOAD = "forceLoad";
 
 	public final static String PREFERENCE_GROUP_MAIN = "main";
 
 	public final static String PREFERENCE_URL = "url";
 
 	public final static String DEFAULT_URL = "https://docs.google.com/spreadsheet/ccc?key=0AoDV0i2WefMXdEI2VV9Xb1I5eFpBeS1HYkw5NGNqR3c&output=csv#gid=0";
 
 	public final static String HELP_URL = "http://sloopdata.org";
 
 	//
 	// Saveable state
 	//
 
 	/**
 	 * The URL of the current data set.
 	 */
 	private String mUrl = null;
 
 	private boolean mIsLoading = false;
 
 	//
 	// UI components.
 	//
 
 	/**
 	 * The address action provider.
 	 */
 	private AddressActionProvider mAddressProvider;
 
 	/**
 	 * The {@link PagerAdapter} for the current data collection.
 	 */
 	private DataCollectionPagerAdapter mPagerAdapter;
 
 	/**
 	 * The {@link ViewPager} that will host the data collection.
 	 */
 	private ViewPager mViewPager;
 
 	/**
 	 * The seek bar for scrolling through the collection.
 	 */
 	private SeekBar mSeekBar;
 
 	private ProgressBar mProgressBar;
 
 	//
 	// Activity lifecycle methods.
 	//
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		setContentView(R.layout.activity_main);
 
 		// Set up the text field box
 		setupTextFilter();
 
 		// Set up the main display area
 		setupPager();
 
 		// Set up the progress indicator
 		setupProgressBar();
 
 		// Set up the seek bar.
 		setupSeekBar();
 
 		// What are we supposed to be doing?
 		doHandleIntent(getIntent());
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_main, menu);
 
 		// Set up the address bar action
 		setupAddressProvider(menu.findItem(R.id.menu_address_bar));
 
 		return true;
 	}
 
 	@Override
 	public void onSaveInstanceState(Bundle savedInstanceState) {
 		// save the URL for the next invocation
 		if (mUrl != null) {
 			super.onSaveInstanceState(savedInstanceState);
 			savedInstanceState.putString(PARAM_URL, mUrl);
 			SharedPreferences.Editor editor = getSharedPreferences(
 					PREFERENCE_GROUP_MAIN, MODE_PRIVATE).edit();
 			editor.putString(PREFERENCE_URL, mUrl);
 			editor.commit();
 		}
 	}
 
 	@Override
 	public void onNewIntent(Intent intent) {
 		doHandleIntent(intent);
 	}
 
 	@Override
 	public boolean onMenuItemSelected(int featureId, MenuItem item) {
 		// Items from the main menu
 		switch (item.getItemId()) {
 		case R.id.menu_bookmark_list:
 			doLaunchBookmarkList();
 			return true;
 		case R.id.menu_search:
 			doToggleTextFilterVisibility();
 			return true;
 		case R.id.menu_bookmark_create:
 			doLaunchBookmarkCreate(mUrl);
 			return true;
 		case R.id.menu_reload:
 			doLoadDataCollection(mUrl, true);
 			return true;
 		case R.id.menu_help:
 			doLaunchBrowser(HELP_URL);
 			return true;
 		default:
 			return super.onMenuItemSelected(featureId, item);
 		}
 	}
 
 	@Override
 	public boolean onSearchRequested() {
 		doToggleTextFilterVisibility();
 		return true;
 	}
 
 	//
 	// Configuration functions for UI components.
 	//
 	// Each of these functions sets listeners, etc. for its component. The
 	// listeners use the do*() action methods to perform actions.
 	//
 
 	/**
 	 * Set up the text filter field.
 	 */
 	private void setupTextFilter() {
 		final View filterLayout = findViewById(R.id.layout_filter);
 		final EditText textField = (EditText) findViewById(R.id.field_filter);
 		final Button cancelButton = (Button) findViewById(R.id.button_filter_clear);
 
 		filterLayout.setVisibility(View.GONE);
 
 		textField
 				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
 					@Override
 					public boolean onEditorAction(TextView v, int actionId,
 							KeyEvent event) {
 						doSetTextFilter(v.getText().toString());
 						return true;
 					}
 				});
 
 		cancelButton.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				doSetTextFilter(null);
 				if (textField.getText().length() == 0) {
 					filterLayout.setVisibility(View.GONE);
 					doHideKeyboard();
 				} else {
 					textField.setText(null);
 				}
 			}
 		});
 	}
 
 	/**
 	 * Set up the main ViewPager.
 	 */
 	private void setupPager() {
 		mPagerAdapter = new DataCollectionPagerAdapter(
 				getSupportFragmentManager());
 
 		// Set up the ViewPager with the data collection adapter.
 		mViewPager = (ViewPager) findViewById(R.id.pager);
 		mViewPager.setAdapter(mPagerAdapter);
 		mViewPager
 				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
 					@Override
 					public void onPageSelected(int position) {
 						doDisplayRecordNumber(position);
 					}
 				});
 	}
 
 	private void setupProgressBar() {
 		mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
 		mProgressBar.setVisibility(View.GONE);
 	}
 
 	private void setupSeekBar() {
 		mSeekBar = (SeekBar) findViewById(R.id.page_seek_bar);
 		mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
 
 			@Override
 			public void onStopTrackingTouch(SeekBar seekBar) {
 				// NO OP
 			}
 
 			@Override
 			public void onStartTrackingTouch(SeekBar seekBar) {
 				// NO OP
 			}
 
 			@Override
 			public void onProgressChanged(SeekBar seekBar, int progress,
 					boolean fromUser) {
 				mViewPager.setCurrentItem(progress, false);
 			}
 		});
 	}
 
 	/**
 	 * Set up the address bar action provider.
 	 * 
 	 * @param item
 	 *            The menu item for the address bar action.
 	 */
 	private void setupAddressProvider(MenuItem item) {
 		mAddressProvider = (AddressActionProvider) item.getActionProvider();
 		mAddressProvider.setMenuItem(item);
 		mAddressProvider.setUrl(mUrl);
 		mAddressProvider.setIsLoading(mIsLoading);
 		mAddressProvider
 				.setAddressBarListener(new AddressActionProvider.AddressBarListener() {
 					@Override
 					public void onLoadStarted(String url) {
 						doLoadDataCollection(url, true);
 					}
 
 					@Override
 					public void onLoadCancelled(String url) {
 
 					}
 				});
 	}
 
 	//
 	// Abstracted UI actions
 	//
 	// These are separate functions to avoid direct dependencies among UI
 	// components and their listeners. Each action represents something that can
 	// happen in the activity, and the functions take care of figuring out what
 	// to do with different components.
 	//
 
 	/**
 	 * Action: handle any special intents.
 	 * 
 	 * @param intent
 	 *            The intent passed to the activity.
 	 */
 	private void doHandleIntent(Intent intent) {
 		String action = intent.getAction();
 
 		// ACTION_MAIN is the default
 		if (action == null) {
 			action = Intent.ACTION_MAIN;
 		}
 
 		if (Intent.ACTION_MAIN.equals(action)) {
 			String url = intent.getStringExtra(PARAM_URL);
 			System.err.println("Intent " + url);
 			// Restore the last URL
 			if (url == null) {
 				url = getSharedPreferences(PREFERENCE_GROUP_MAIN, MODE_PRIVATE)
 						.getString(PREFERENCE_URL, null);
 				url = DEFAULT_URL;
 			}
 			if (url != null && url.length() > 0) {
 				doLoadDataCollection(url, false);
 			}
 		}
 
 		else if (ACTION_FILTER.equals(action)) {
 			DataEntry entry = intent.getParcelableExtra(PARAM_ENTRY);
 			doSetColumnFilter(entry);
 		}
 	}
 
 	/**
 	 * Action: set a text filter for the data collection.
 	 */
 	private void doSetTextFilter(final String query) {
 		DataCollection collection = mPagerAdapter.getDataCollection();
 		if (collection != null) {
 			if (query == null) {
 				collection.setTextFilter(null);
 			} else {
 				collection.setTextFilter(new ValueFilter() {
 					@Override
 					public boolean isMatch(String value) {
 						return value.toUpperCase(Locale.getDefault()).contains(
 								query.toUpperCase(Locale.getDefault()));
 					}
 				});
 			}
 			collection.setFilteringEnabled(true);
 			mViewPager.setAdapter(mPagerAdapter);
 			doDisplayRecordNumber(mViewPager.getCurrentItem());
 		}
 	}
 
 	/**
 	 * Action: add a column filter for the data collection.
 	 * 
 	 * @param entry
 	 *            the data entry (soon to be the filter)
 	 */
 	private void doSetColumnFilter(DataEntry entry) {
 		DataCollection collection = mPagerAdapter.getDataCollection();
 		if (collection.getColumnFilter(entry.getKey()) != null) {
 			collection.putColumnFilter(entry.getKey(), null);
 			if (!collection.hasFilters()) {
 				collection.setFilteringEnabled(false);
 			}
 			Toast.makeText(
 					this,
 					String.format(getString(R.string.msg_filter_cleared),
 							entry.getKey()), Toast.LENGTH_SHORT).show();
 		} else {
 			collection.setFilteringEnabled(true);
 			final String entryValue = entry.getValue();
 			collection.putColumnFilter(entry.getKey(), new ValueFilter() {
 				@Override
 				public boolean isMatch(String value) {
 					return entryValue.toUpperCase().equals(
 							value.toUpperCase());
 				}
 			});
 			Toast.makeText(
 					this,
 					String.format(getString(R.string.msg_filter_set),
 							entry.getKey(), entry.getValue()),
 					Toast.LENGTH_SHORT).show();
 		}
 		mViewPager.setAdapter(mPagerAdapter);
 		doDisplayRecordNumber(mViewPager.getCurrentItem());
 	}
 
 	/**
 	 * Action: report an error message.
 	 * 
 	 * @param message
 	 *            The error message as a string.
 	 */
 	private void doDisplayError(String message) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
 		builder.setMessage(message);
 		builder.setNeutralButton(R.string.btn_ok,
 				new DialogInterface.OnClickListener() {
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 						dialog.cancel();
 					}
 				});
 		builder.create().show();
 	}
 
 	/**
 	 * Action: show the text filter field.
 	 */
 	private void doToggleTextFilterVisibility() {
 		View textFilter = findViewById(R.id.layout_filter);
 		if (textFilter.getVisibility() == View.VISIBLE) {
 			textFilter.setVisibility(View.GONE);
 		} else {
 			textFilter.setVisibility(View.VISIBLE);
 			textFilter.requestFocus();
 		}
 	}
 
 	/**
 	 * Action: launch the bookmark list activity.
 	 */
 	private void doLaunchBookmarkList() {
 		Intent intent = new Intent(this, BookmarkListActivity.class);
 		startActivity(intent);
 	}
 
 	/**
 	 * Action: launch the bookmark create activity.
 	 * 
 	 * @param url
 	 *            The URL of the bookmark to be created or edited.
 	 */
 	private void doLaunchBookmarkCreate(String url) {
 		Intent intent = new Intent(this, BookmarkEditActivity.class);
 		intent.putExtra(BookmarkEditActivity.PARAM_URL, url);
 		startActivity(intent);
 	}
 
 	/**
 	 * Load a data collection from a URL.
 	 * 
 	 * When the load is complete, the loader will invoke the
 	 * {@link #onLoadFinished(Loader, DataCollectionResult)} method with the
 	 * result.
 	 * 
 	 * @param url
 	 *            the URL of the data collection.
 	 */
 	private void doLoadDataCollection(String url, boolean forceLoad) {
 		if (url == null || url.length() == 0) {
 			doDisplayError(getString(R.string.msg_web_address));
 			return;
 		}
 		mUrl = url;
 		mIsLoading = true;
 		Bundle args = new Bundle();
 		args.putString(PARAM_URL, url);
 		args.putBoolean(PARAM_FORCE_LOAD, forceLoad);
 		getLoaderManager().restartLoader(0, args,
 				new LoaderCallbacks<DataCollectionResult>() {
 
 					@Override
 					public Loader<DataCollectionResult> onCreateLoader(int id,
 							Bundle args) {
 						// only one loader for now, so ignore id
 						DataCollectionLoader loader = new DataCollectionLoader(
 								getApplicationContext());
 						if (args != null) {
 							loader.setURL(args.getString(PARAM_URL));
 							loader.setForceLoad(args
 									.getBoolean(PARAM_FORCE_LOAD));
 						}
 						return loader;
 					}
 
 					@Override
 					public void onLoadFinished(
 							Loader<DataCollectionResult> loader,
 							DataCollectionResult result) {
 						if (result.hasError()) {
 							// if the load failed, show and error and stick
 							// around
 							doDisplayError(result.getThrowable().getMessage());
 						} else if (result.getRedirectUrl() != null) {
 							// if it was a non-CSV resource, launch the browser
 							doLaunchBrowser(result.getRedirectUrl());
 						} else {
 							// succeeded - show the collection
 							doUpdateDataCollection(result.getDataCollection());
 							mIsLoading = false;
 							if (mAddressProvider != null) {
 								mAddressProvider.setUrl(mUrl);
 								mAddressProvider.setIsLoading(false);
 							}
 						}
 						mIsLoading = false;
 						mProgressBar.setVisibility(View.GONE);
 					}
 
 					@Override
 					public void onLoaderReset(
 							Loader<DataCollectionResult> loader) {
 						// NO OP
 					}
 
 				});
 		if (mAddressProvider != null) {
 			mAddressProvider.setIsLoading(true);
 			mAddressProvider.setUrl(mUrl);
 		}
 		doHideKeyboard();
 		mIsLoading = true;
 		mProgressBar.setVisibility(View.VISIBLE);
 	}
 
 	/**
 	 * Action: update the data collection displayed.
 	 * 
 	 * Assigns the new data collection to the pager adapter, and updates the
 	 * seekbar and the info bar.
 	 * 
 	 * @param dataCollection
 	 *            the new data collection, or null to clear.
 	 */
 	private void doUpdateDataCollection(DataCollection dataCollection) {
 		mPagerAdapter.setDataCollection(dataCollection);
 		if (dataCollection != null) {
			mSeekBar.setMax(mPagerAdapter.getCount());
 			doDisplayRecordNumber(0);
 		} else {
 			mSeekBar.setProgress(0);
 			mSeekBar.setMax(0);
 			doDisplayInfo(getString(R.string.msg_no_data));
 		}
 	}
 
 	/**
 	 * End this activity and launch a non-CSV URL.
 	 * 
 	 * This method invokes {@link #finish()}.
 	 * 
 	 * @param url
 	 *            The URL to launch in the browser (etc.).
 	 */
 	private void doLaunchBrowser(String url) {
 		Intent intent = new Intent(Intent.ACTION_VIEW);
 		intent.setData(Uri.parse(url));
 		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 		startActivity(intent);
 		finish();
 	}
 
 	/**
 	 * Action: display the current record number.
 	 * 
 	 * Updates the position of the horizontal seekbar, and displays the record
 	 * number in the info bar.
 	 * 
 	 * @param recordNumber
 	 *            the record number to display (zero-based).
 	 */
 	private void doDisplayRecordNumber(int recordNumber) {
 		DataCollection collection = mPagerAdapter.getDataCollection();
 		int count = collection.getFilteredRecords().size();
 		int unfilteredCount = collection.getRecords().size();
 		mSeekBar.setProgress(recordNumber);
		mSeekBar.setMax(count);
 		if (count < unfilteredCount) {
 			doDisplayInfo(String.format(
 					getString(R.string.info_records_filtered),
 					recordNumber + 1, count, unfilteredCount));
 		} else {
 			doDisplayInfo(String.format(
 					getString(R.string.info_records_unfiltered),
 					recordNumber + 1, count));
 		}
 	}
 
 	/**
 	 * Action: update text in the info bar.
 	 * 
 	 * @param message
 	 *            The message to display.
 	 */
 	private void doDisplayInfo(String message) {
 		TextView infoBar = (TextView) findViewById(R.id.info_bar);
 		infoBar.setText(message);
 	}
 
 	/**
 	 * Action: hide the soft keyboard.
 	 */
 	private void doHideKeyboard() {
 		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		imm.hideSoftInputFromWindow(mViewPager.getWindowToken(), 0);
 	}
 
 }
