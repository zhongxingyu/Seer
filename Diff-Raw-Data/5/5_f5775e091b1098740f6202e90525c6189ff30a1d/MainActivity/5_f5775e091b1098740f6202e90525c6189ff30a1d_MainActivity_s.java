 package com.felixware.gw2w;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentTransaction;
 import android.text.Editable;
 import android.text.TextUtils;
 import android.text.TextWatcher;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.inputmethod.EditorInfo;
 import android.view.inputmethod.InputMethodManager;
 import android.webkit.WebView;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.app.ActionBar.OnNavigationListener;
 import com.actionbarsherlock.app.SherlockFragmentActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 import com.actionbarsherlock.view.MenuItem.OnActionExpandListener;
 import com.felixware.gw2w.adapters.DropDownAdapter;
 import com.felixware.gw2w.fragments.FirstLoadFragment;
 import com.felixware.gw2w.fragments.ImageDialogFragment;
 import com.felixware.gw2w.http.RequestTask;
 import com.felixware.gw2w.http.WebService;
 import com.felixware.gw2w.http.WebService.GetContentListener;
 import com.felixware.gw2w.http.WebService.GetSearchResultsListener;
 import com.felixware.gw2w.http.WebServiceException;
 import com.felixware.gw2w.listeners.MainListener;
 import com.felixware.gw2w.utilities.ArticleWebViewClient;
 import com.felixware.gw2w.utilities.Constants;
 import com.felixware.gw2w.utilities.Dialogs;
 import com.felixware.gw2w.utilities.Language;
 import com.felixware.gw2w.utilities.PrefsManager;
 import com.felixware.gw2w.utilities.Regexer;
 
 public class MainActivity extends SherlockFragmentActivity implements OnNavigationListener, OnActionExpandListener, OnClickListener, MainListener, OnEditorActionListener, GetContentListener, GetSearchResultsListener, OnItemClickListener, OnFocusChangeListener {
 
 	private WebView mWebContent;
 	private RelativeLayout mNavBar, mWebSpinner;
 	private EditText mSearchBox;
 	private TextView mPageTitle;
 	private ImageButton mSearchBtn;
 	private ImageView mFavoriteBtn;
 	private ProgressBar mSearchSpinner;
 	private Boolean isGoingBack = false, isNotSelectedResult = true, isFavorite = false, isFirstLoad = true, isRotating = false;
 	private List<String> backHistory = new ArrayList<String>(), favorites = new ArrayList<String>();
 	private String currentPageTitle;
 	private List<String> currentPageCategories;
 	private Handler mSearchHandle;
 	private ListView mSearchResultsListView;
 	private DropDownAdapter mAdapter;
 	private ActionBar mActionBar;
 	private MenuItem mSearch;
 	private View mSearchView;
 	private FrameLayout dummyView, firstLoadLayout;
 	private InputMethodManager imm;
 	private String[] languages;
 	private String[] langCodes;
 	private Dialogs mDialogs;
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getSupportMenuInflater().inflate(R.menu.main_activity, menu);
 
 		mSearch = menu.findItem(R.id.search);
 
 		mSearchView = (View) mSearch.getActionView();
 
 		mSearch.setOnActionExpandListener(this);
 
 		mSearchBox = (EditText) mSearchView.findViewById(R.id.searchET);
 		mSearchBox.setOnEditorActionListener(this);
 		mSearchBox.addTextChangedListener(new SearchTextWatcher(mSearchBox));
 		mSearchBox.setOnFocusChangeListener(this);
 
 		mSearchBtn = (ImageButton) mSearchView.findViewById(R.id.searchBtn);
 		mSearchBtn.setOnClickListener(this);
 
 		mSearchSpinner = (ProgressBar) mSearchView.findViewById(R.id.spinner);
 
 		return super.onCreateOptionsMenu(menu);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.favorites:
 			favorites = Constants.getFavoritesListFromJSON(this);
 			if (favorites.isEmpty()) {
 				mDialogs.buildNoFavoritesDialog();
 			} else {
 				mDialogs.buildFavoritesDialog(favorites);
 			}
 			return true;
 		case R.id.share:
 			shareArticle();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main_activity);
 
 		mActionBar = getSupportActionBar();
 
 		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
 
 		languages = getResources().getStringArray(R.array.Settings_wiki_languages);
 		langCodes = getResources().getStringArray(R.array.Settings_wiki_langcodes);
 		mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
 
 		mDialogs = new Dialogs(this);
 
 		bindViews();
 		if (isFirstLoad) {
 			isFirstLoad = false;
 
 			firstLoadLayout.bringToFront();
 			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
 			ft.add(R.id.firstLoadLayout, new FirstLoadFragment());
 			ft.commit();
 		}
 
 		mSearchHandle = new Handler();
 
 		mWebContent = (WebView) findViewById(R.id.webContent);
 		mWebContent.setWebViewClient(new ArticleWebViewClient(this));
 
 		if (savedInstanceState != null) {
 			if (mWebContent.restoreState(savedInstanceState) == null) {
 				// Log.i("Something broke", "Dang");
 			}
 		} else if (getIntent().getDataString() != null) {
 			// open URI directly
 			Uri uri = getIntent().getData();
 			String title = null;
 			if (uri.getPath().startsWith("/wiki/"))
 				title = uri.getPath().substring(6);
 			else
 				title = uri.getQueryParameter("title");
 
 			// fallback to start page
 			if (title == null)
 				getContent(Constants.getStartPage(this));
 
 			// change language to match URI
 			String languageTag = uri.getHost().substring(4, uri.getHost().indexOf('.'));
 			if (languageTag.equals(Language.GERMAN.getSubdomainSuffix()))
 				PrefsManager.getInstance(this).setLanguage(Language.GERMAN);
 			else if (languageTag.equals(Language.FRENCH.getSubdomainSuffix()))
 				PrefsManager.getInstance(this).setLanguage(Language.FRENCH);
 			else if (languageTag.equals(Language.SPANISH.getSubdomainSuffix()))
 				PrefsManager.getInstance(this).setLanguage(Language.SPANISH);
 			else
 				PrefsManager.getInstance(this).setLanguage(Language.ENGLISH);
 
 			// open article
 			getContent(title);
 		} else {
 			getContent(Constants.getStartPage(this));
 		}
 
 		mActionBar.setSelectedNavigationItem(PrefsManager.getInstance(this).getWikiLanguage());
 	}
 
 	private void bindViews() {
 
 		dummyView = (FrameLayout) findViewById(R.id.dummy);
 
 		firstLoadLayout = (FrameLayout) findViewById(R.id.firstLoadLayout);
 
 		mSearchResultsListView = (ListView) findViewById(R.id.searchResultsListView);
 		mSearchResultsListView.setOnItemClickListener(this);
 
 		mNavBar = (RelativeLayout) findViewById(R.id.navBar);
 		mNavBar.bringToFront();
 
 		mWebSpinner = (RelativeLayout) findViewById(R.id.webSpinnerLayout);
 
 		mFavoriteBtn = (ImageView) findViewById(R.id.favoritesBtn);
 		mFavoriteBtn.setOnClickListener(this);
 
 		mPageTitle = (TextView) findViewById(R.id.pageTitle);
 
 		switch (getResources().getConfiguration().orientation) {
 		case Configuration.ORIENTATION_PORTRAIT:
 			mAdapter = new DropDownAdapter(this, languages, langCodes, DropDownAdapter.ORIENTATION_PORTRAIT);
 			mActionBar.setSelectedNavigationItem(PrefsManager.getInstance(this).getWikiLanguage());
 			break;
 		case Configuration.ORIENTATION_LANDSCAPE:
 			mAdapter = new DropDownAdapter(this, languages, langCodes, DropDownAdapter.ORIENTATION_LANDSCAPE);
 			mActionBar.setSelectedNavigationItem(PrefsManager.getInstance(this).getWikiLanguage());
 			break;
 		}
 		mActionBar.setListNavigationCallbacks(mAdapter, this);
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 		isRotating = true;
 
 		mActionBar.setListNavigationCallbacks(mAdapter, null);
 		switch (newConfig.orientation) {
 		case Configuration.ORIENTATION_PORTRAIT:
 			mAdapter = new DropDownAdapter(this, languages, langCodes, DropDownAdapter.ORIENTATION_PORTRAIT);
 			break;
 		case Configuration.ORIENTATION_LANDSCAPE:
 			mAdapter = new DropDownAdapter(this, languages, langCodes, DropDownAdapter.ORIENTATION_LANDSCAPE);
 			break;
 		}
 		mActionBar.setListNavigationCallbacks(mAdapter, this);
 		mActionBar.setSelectedNavigationItem(PrefsManager.getInstance(this).getWikiLanguage());
 
 		isRotating = false;
 	}
 
 	@Override
 	public boolean onKeyUp(int keyCode, KeyEvent event) {
 		switch (keyCode) {
 		case KeyEvent.KEYCODE_BACK:
 			if (backHistory.size() > 1) {
 				navigateBack();
 				return true;
 			}
 			break;
 		}
 		return super.onKeyUp(keyCode, event);
 	}
 
 	private void navigateBack() {
 		backHistory.remove(backHistory.size() - 1);
 		getContent(backHistory.get(backHistory.size() - 1));
 		isGoingBack = true;
 	}
 
 	protected void onSaveInstanceState(Bundle outState) {
 		mWebContent.saveState(outState);
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.searchBtn:
 			searchForTerm();
 			break;
 
 		case R.id.favoritesBtn:
 			if (currentPageTitle != null) {
 				if (!isFavorite) {
 					favorites.add(currentPageTitle);
 					PrefsManager.getInstance(this).setFavorites(Constants.getJSONStringFromList(favorites));
 					mFavoriteBtn.setImageResource(R.drawable.nav_favorites_on);
 					isFavorite = true;
 				} else {
 					favorites.remove(currentPageTitle);
 					PrefsManager.getInstance(this).setFavorites(Constants.getJSONStringFromList(favorites));
 					mFavoriteBtn.setImageResource(R.drawable.nav_favorites_off);
 					isFavorite = false;
 				}
 			}
 			break;
 		}
 
 	}
 
 	public void getContent(String title) {
 		if (title == null || title.equals(""))
 			return;
 
 		WebService.getInstance(this).cancelAllRequests();
 		mWebSpinner.setVisibility(View.VISIBLE);
 
 		if (PrefsManager.getInstance(this).getLanguage() == Language.ENGLISH) {
 			WebService.getInstance(this).getTitleEnglish(this, title);
 		} else {
 			WebService.getInstance(this).getContent(this, title);
 		}
 	}
 
 	private void searchForTerm() {
 		getContent(mSearchBox.getText().toString());
 		mSearch.collapseActionView();
 	}
 
 	@Override
 	public void onLink(String url) {
 		Matcher matcher = Pattern.compile("(?<=wiki/).*").matcher(url);
		matcher.find();
		getContent(matcher.group());
 	}
 
 	@Override
 	public void onExternalLink(String url) {
 		if (PrefsManager.getInstance(this).getExternalWarning()) {
 			mDialogs.buildExternalLinkDialog(url);
 		} else {
 			externalLink(url);
 		}
 
 	}
 
 	@Override
 	public void onExternalOkay(String url) {
 		externalLink(url);
 	}
 
 	public void externalLink(String url) {
 		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 		startActivity(intent);
 	}
 
 	@Override
 	public void onShowCategories() {
 		mDialogs.buildCategoriesDialog(currentPageCategories);
 	}
 
 	@Override
 	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
 		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
 			searchForTerm();
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void onRequestError(RequestTask request, WebServiceException e) {
 		if (mSearchSpinner != null) {
 			mSearchSpinner.setVisibility(View.INVISIBLE);
 		}
 		mWebSpinner.setVisibility(View.GONE);
 		firstLoadLayout.removeAllViews();
 		firstLoadLayout.setVisibility(View.GONE);
 		mDialogs.buildErrorDialog(e.getErrorCode());
 
 	}
 
 	@Override
 	public void didGetContent(RequestTask request, String content, String title) {
 		StringBuilder html = new StringBuilder("<!DOCTYPE html><html><head>");
 		html.append("<title>GW2W</title>");
 
 		// load default site styles
 		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
 		html.append(Constants.getBaseURL(this) + "/index.php?title=MediaWiki:Common.css&amp;action=raw&amp;ctype=text/css");
 		html.append("\" />");
 
 		// load custom styles
 		html.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"file:///android_asset/style.css\" />");
 
 		html.append("</head><body><main>");
 		html.append(Regexer.strip(content));
 		html.append("</main>");
 
 		// category list
 		if (currentPageCategories != null && currentPageCategories.size() > 0) {
 			html.append("<a id=\"android-category-list\" href=\"about:categories\">");
 			html.append(TextUtils.htmlEncode(getString(R.string.categories, currentPageCategories.size())));
 			html.append("</a>");
 		}
 
 		html.append("</body></html>");
 
 		mWebContent.loadDataWithBaseURL(Constants.getBaseURL(this), html.toString(), "text/html", "UTF-8", title);
 		mPageTitle.setText(title);
 		// Log.i("checking titles", "current page title is " + currentPageTitle + " new title is " + title);
 		if (!isGoingBack && (currentPageTitle == null || !currentPageTitle.equals(title))) {
 			// Log.i("back history", "Adding " + title + " to the back history");
 			backHistory.add(title);
 		} else {
 			isGoingBack = false;
 		}
 		currentPageTitle = title;
 		mFavoriteBtn.setImageResource(R.drawable.nav_favorites_off);
 		isFavorite = false;
 		determineFavoriteStatus();
 		mWebSpinner.setVisibility(View.GONE);
 		firstLoadLayout.removeAllViews();
 		firstLoadLayout.setVisibility(View.GONE);
 	}
 
 	@Override
 	public void didGetFileUrl(RequestTask request, String url, String title) {
 		mWebSpinner.setVisibility(View.GONE);
 		ImageDialogFragment.newInstance(url).show(getSupportFragmentManager(), "dialog");
 	}
 
 	@Override
 	public void didGetCategories(RequestTask request, List<String> categories, String title) {
 		currentPageCategories = categories;
 	}
 
 	private void determineFavoriteStatus() {
 		favorites = Constants.getFavoritesListFromJSON(this);
 		for (String pageName : favorites) {
 			if (pageName.equals(currentPageTitle)) {
 				isFavorite = true;
 				mFavoriteBtn.setImageResource(R.drawable.nav_favorites_on);
 				break;
 			}
 		}
 
 	}
 
 	private void shareArticle() {
 		Intent shareIntent = new Intent(Intent.ACTION_SEND);
 		String pageURL = getPageURL().replace(" ", "_");
 		// most options will be able to use key Intent.EXTRA_TEXT
 		shareIntent.putExtra(Intent.EXTRA_TEXT, pageURL);
 		// sms sharing requires the message to be in key sms_body
 		shareIntent.putExtra("sms_body", pageURL);
 		shareIntent.setType("text/plain");
 		// Log.i("Sharing", pageURL);
 		startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_picker_title)));
 	}
 
 	private String getPageURL() {
 		return new String(Constants.getBaseURL(this) + File.separator + currentPageTitle);
 	}
 
 	@Override
 	protected void onPause() {
 		super.onPause();
 		WebService.getInstance(this).cancelAllRequests();
 		if (mSearchSpinner != null) {
 			mSearchSpinner.setVisibility(View.GONE);
 		}
 		if (mWebSpinner != null) {
 			mWebSpinner.setVisibility(View.GONE);
 		}
 	}
 
 	private class SearchTextWatcher implements TextWatcher {
 
 		private Runnable mSearchRunnable;
 
 		public SearchTextWatcher(final EditText e) {
 			mSearchRunnable = new Runnable() {
 				public void run() {
 					String searchText = e.getText().toString().trim();
 					if (searchText != null && searchText.length() > 1) {
 						mSearchSpinner.setVisibility(View.VISIBLE);
 						WebService.getInstance(MainActivity.this).getSearchResults(MainActivity.this, searchText, 10);
 					} else {
 
 					}
 				}
 			};
 		}
 
 		@Override
 		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
 			// nothing
 		}
 
 		@Override
 		public void onTextChanged(CharSequence s, int start, int before, int count) {
 			// nothing
 		}
 
 		@Override
 		public void afterTextChanged(Editable s) {
 			if (isNotSelectedResult) {
 				try {
 					mSearchHandle.removeCallbacks(mSearchRunnable);
 					mSearchHandle.postDelayed(mSearchRunnable, 1000);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 
 		}
 	}
 
 	@Override
 	public void didGetSearchResults(RequestTask request, List<String> list) {
 		mSearchSpinner.setVisibility(View.INVISIBLE);
 		mSearchResultsListView.setVisibility(View.VISIBLE);
 		mPageTitle.setText(R.string.search_results);
 		mSearchResultsListView.setAdapter(new ArrayAdapter<String>(this, R.layout.search_results_item, list));
 
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
 		mSearch.collapseActionView();
 		getContent(((TextView) v).getText().toString());
 	}
 
 	@Override
 	public void onFocusChange(View v, boolean hasFocus) {
 		switch (v.getId()) {
 		case R.id.searchET:
 			if (hasFocus) {
 				imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
 			} else {
 				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
 			}
 			break;
 		default:
 			break;
 		}
 
 	}
 
 	@Override
 	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
 		if (PrefsManager.getInstance(this).getWikiLanguage() != itemPosition && !isRotating) {
 			PrefsManager.getInstance(this).setWikiLanguage(itemPosition);
 			backHistory.clear();
 			mWebContent.clearView();
 			mFavoriteBtn.setImageResource(R.drawable.nav_favorites_off);
 			currentPageTitle = null;
 			mPageTitle.setText("");
 			getContent(Constants.getStartPage(this));
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onMenuItemActionExpand(MenuItem item) {
 		mSearchBox.getSelectionStart();
 		return true;
 	}
 
 	@Override
 	public boolean onMenuItemActionCollapse(MenuItem item) {
 		mSearchSpinner.setVisibility(View.INVISIBLE);
 		mSearchBox.setText("");
 		dummyView.requestFocus();
 		mSearchResultsListView.setVisibility(View.GONE);
 		mPageTitle.setText(currentPageTitle);
 		return true;
 	}
 
 }
