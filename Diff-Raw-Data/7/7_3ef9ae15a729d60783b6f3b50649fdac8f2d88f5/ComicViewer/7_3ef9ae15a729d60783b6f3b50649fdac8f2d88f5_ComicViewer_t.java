 /**
  * @author Yixin Zhu, Joel Jauregui
  */
 package com.comic.viewer;
 
 import java.util.Arrays;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Html;
 import android.view.KeyEvent;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.webkit.WebChromeClient;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.TextView.OnEditorActionListener;
 import android.widget.Toast;
 
 import com.comic.globals.Globals;
 import com.comic.misc.ComicUtils;
 import com.comic.misc.NavBarListener;
 
 public class ComicViewer extends Activity implements OnClickListener {
 	public ProgressDialog loadingDialog;
 	private Button first, back, next, last;
 	private EditText goto_vol_page;
 	private int firstVolPage, lastVolPage, currentPage, currentVol;
 	private WebView myWebView;
 	private TextView comicTitleView;
	private View navbar, navReplace; 
 	private AlertDialog helpDialog;
 	private final String helpBundleKey = "helpDialogBundle";
 	private static Pattern comicTitleRegex = 
 		Pattern.compile("http://samandfuzzy.com/comics/.+?alt=\"(.+?)\"");
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		// sets up custom title bar
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.comicviewer);
 		// sets up objects in view
 		setup();
 		//sets up first view to display
 		setupInitialView(); 
 	}
 
 	/**
 	 * Initial startup consisting of setting up of xml instances and zoom controls
 	 */
 	public void setup() {
 		//gets instances from xml
 		myWebView = (WebView) findViewById(R.id.webView);
 		first = (Button) findViewById(R.id.start);
 		first.setOnClickListener(this);
 		back = (Button) findViewById(R.id.back);
 		back.setOnClickListener(this);
 		goto_vol_page = (EditText) findViewById(R.id.goto_vol_page);
 		goto_vol_page.setOnEditorActionListener(new OnEditorActionListener() {
             public boolean onEditorAction(TextView v, int actionId,
                     KeyEvent event) {
             	int num = Integer.valueOf(goto_vol_page.getText().toString());
             	if (num != currentPage)
             		displayNewView(num);
                 return false;
             }
         });
 		next = (Button) findViewById(R.id.next);
 		next.setOnClickListener(this);
 		last = (Button) findViewById(R.id.current);
 		last.setOnClickListener(this);
 		comicTitleView = (TextView) findViewById(R.id.comictitle);
 		comicTitleView.setOnClickListener(this);
 		navbar = findViewById(R.id.navbar);
 		navbar.setOnClickListener(this);
 		navReplace = findViewById(R.id.navreturn);
 		navReplace.setOnClickListener(this);
 		
 		//sets up image display and zoom
 		myWebView.setClickable(true);
 		final Activity activity = this;
 		final ComicViewer comicviewer = this;
 		myWebView.setWebChromeClient(new WebChromeClient() {
 			   public void onProgressChanged(WebView view, int progress) {
 				   		//sets progress dialog
 				   		comicviewer.setLoading(progress);
 				   }
 				 });
 		myWebView.setWebViewClient(new WebViewClient() {
 			public void onReceivedError(WebView view, int errorCode,
 					String description, String failingUrl) {
 				Toast.makeText(activity, "Oh no! " + description,
 						Toast.LENGTH_SHORT).show();
 			}
 		});
 		myWebView.requestFocus();
 		myWebView.setBackgroundColor(Color.BLACK);
 		myWebView.getSettings().setBuiltInZoomControls(true);
 		
 		// get volume range
 		Bundle bundle = getIntent().getExtras();
 		// sets up the range of this volume
 		setThisVolumeRange((String) bundle.getString("volumeRange"));
 		currentVol = (int) bundle.getInt("volumeNumber");
 	}
 
 	/**
 	 * Calculates the range for this specific volume
 	 * 
 	 * @param range 
 	 * 				The specified range in String format
 	 */
 	public void setThisVolumeRange(String range) {
 		firstVolPage = Integer.valueOf(range.substring(0, range.indexOf("-")))
 				.intValue();
 		lastVolPage = Integer.valueOf(range.substring(range.indexOf("-") + 1))
 				.intValue();
 		goto_vol_page.setHint(firstVolPage + " - " + lastVolPage);
 	}
 
 	/**
 	 * Sets up the initial view, called when launched from main menu
 	 */
 	private void setupInitialView() {
 		SharedPreferences settings = getSharedPreferences("VOLUME_SAVES", 0);
 		if (settings != null)
 			currentPage = settings.getInt(Globals.lastComicKey + currentVol, firstVolPage);
 		displayNewView(currentPage);
 	}
 	
 	/**
 	 * Displays a new comic image to the view
 	 */
 	private void displayNewView(int pageToView) {
 		if (pageToView < firstVolPage || pageToView > lastVolPage){
 			displayError("Please enter a valid page number");
 			goto_vol_page.setText("");
 			return;
 		}
 		currentPage = pageToView;
 		goto_vol_page.setText(String.valueOf(currentPage));
 		adjustControls();
 		myWebView.clearView();
 		String imageURL = Globals.StartImageURL 
 					+ ComicUtils.zfill(currentPage, Globals.numZeros);
 		int in = Arrays.binarySearch(Globals.guest_img_ids, currentPage);
 		imageURL += in >= 0 ? Globals.guest_img_exts[in] : Globals.EndImageURL;
 		myWebView.loadUrl(imageURL);
 		setComicTitle();
 		if (loadingDialog != null && loadingDialog.isShowing())
 			doneLoading();
 		fadeNavBar();
 		myWebView.setInitialScale(70);
 	}
 	
 	/**
 	 * Sets the comic title to the current page
 	 */
 	private void setComicTitle() {
 		String comicSource = ComicUtils.getHTTPSource("http://samandfuzzy.com/" + currentPage);
 		Matcher m = comicTitleRegex.matcher(comicSource);
 		m.find();
 		String comicTitle = m.group(1);
 		comicTitle = Html.fromHtml(comicTitle).toString();
 		comicTitleView.setText("Volume " + currentVol + " - " + comicTitle);
 	}
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		fadeNavBar();
 	}
 	
 	private void fadeNavBar() {
 		Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fade_out);
 		fadeout.setAnimationListener(new NavBarListener(navbar, navReplace));
 		navbar.startAnimation(fadeout);
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v == next) {
 			setupNextView();
 		} else if (v == back) {
 			setupPrevView();
 		} else if (v == first) {
 			setupFirstView();
 		} else if (v == last) {
 			setupLastView();
 		} else if (v == goto_vol_page) {
 			finish();
 		} else if (v == comicTitleView || v == navReplace) {
 			navReplace.setVisibility(View.GONE);
 			navbar.setVisibility(View.VISIBLE);
 		} else if (v == navbar) {
 			fadeNavBar();
 		} 
 	}
 
 	/**
 	 * Setups for displaying of next immediate view
 	 */
 	private void setupNextView() {
 		displayNewView(++currentPage);
 	}
 
 	/**
 	 * Setups for displaying of previous immediate view
 	 */
 	private void setupPrevView() {
 		displayNewView(--currentPage);
 	}
 
 	/**
 	 * Setups for displaying of the first view of the volume
 	 */
 	private void setupFirstView() {
 		currentPage = firstVolPage;
 		displayNewView(currentPage);
 	}
 
 	/**
 	 * Setups for displaying of the last view of the volume
 	 */
 	private void setupLastView() {
 		currentPage = lastVolPage;
 		displayNewView(currentPage);
 	}
 
 	/**
 	 * Adjusts the clickable controls based on displayed page
 	 */
 	private void adjustControls() {
 		if (currentPage > firstVolPage && currentPage < lastVolPage) {
 			first.setEnabled(true);
 			first.setFocusable(true);
 			back.setEnabled(true);
 			back.setFocusable(true);
 			next.setEnabled(true);
 			next.setFocusable(true);
 			last.setEnabled(true);
 			last.setFocusable(true);
 		} else if (currentPage == firstVolPage) {
 			first.setEnabled(false);
 			first.setFocusable(false);
 			back.setEnabled(false);
 			back.setFocusable(false);
 			next.setEnabled(true);
 			next.setFocusable(true);
 			last.setEnabled(true);
 			last.setFocusable(true);
 		} else if (currentPage == lastVolPage) {
 			first.setEnabled(true);
 			first.setFocusable(true);
 			back.setEnabled(true);
 			back.setFocusable(true);
 			next.setEnabled(false);
 			next.setFocusable(false);
 			last.setEnabled(false);
 			last.setFocusable(false);
 		} else {
 			first.setEnabled(false);
 			first.setFocusable(false);
 			back.setEnabled(false);
 			back.setFocusable(false);
 			next.setEnabled(false);
 			next.setFocusable(false);
 			last.setEnabled(false);
 			last.setFocusable(false);
 		}
 	}
 	
 	/**
 	 * Used to create an options menu
 	 */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		//add menu options
 		MenuInflater inflater = getMenuInflater();
     	inflater.inflate(R.menu.viewer_menu, menu);
     	return true;
     }
 	
 	/**
 	 * Called when an option is selected
 	 */
     @Override
     public boolean onOptionsItemSelected(MenuItem item)
     {
     	return (applyMenuChoice(item) || super.onOptionsItemSelected(item));
     }
     
     /**
      * Performs the action on a selected item choice
      * 
      * @param item The id of the item selected
      * @return true if the item selected was performed
      */
 	private boolean applyMenuChoice(MenuItem item) {
 		switch(item.getItemId())
 		{
 		case R.id.help: //display help menu
 			buildHelpDialog(Globals.HelpTitle);
 			return true;
 		case R.id.mainmenu: //go back to main menu
 			finish();
 			return true;
 		case R.id.store: //send user to store
 			startActivity(new Intent(Intent.ACTION_VIEW, 
 					Uri.parse(Globals.storeURL)));
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Builds an help alert dialog displaying a title and message
 	 * 
 	 * @param title The title of the alert dialog
 	 */
 	private void buildHelpDialog(String title) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		//inflate view for setting of content
 		LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
 		View layout = inflater.inflate(R.layout.help_dialog_context,
                 (ViewGroup) findViewById(R.id.layout_root));
 		builder.setTitle(title); //sets title
 		builder.setView(layout); //sets content
 		builder.setNegativeButton("OK", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.dismiss();
 			}
 		});
 		helpDialog = builder.create();
 		helpDialog.show(); //display
 	}
 
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 		outState.putInt("currentPage", currentPage);
 		if (helpDialog != null && helpDialog.isShowing()){
 			//save help dialog if screen orientation changed
 			outState.putBundle(helpBundleKey, helpDialog.onSaveInstanceState());
 		}
 		super.onSaveInstanceState(outState);
 	}
 	
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onRestoreInstanceState(android.os.Bundle)
 	 */
 	@Override
 	protected void onRestoreInstanceState(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onRestoreInstanceState(savedInstanceState);
 		currentPage = savedInstanceState.getInt("currentPage");
 		displayNewView(currentPage);
 		if (savedInstanceState.getBundle(helpBundleKey) != null) {
 			//launch help dialog
 			buildHelpDialog(Globals.HelpTitle);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onStop()
 	 */
 	@Override
 	protected void onStop() {
 		super.onStop();
 		
 		if(currentPage != firstVolPage) {
 			// We need an Editor object to make preference changes.
 		    // All objects are from android.context.Context
 	    	SharedPreferences settings = getSharedPreferences("VOLUME_SAVES", 0);
 	    	SharedPreferences.Editor editor = settings.edit();
 	    	editor.putInt(Globals.lastComicKey + currentVol, currentPage);
 	    
 	    	// Commit the edits!
 	    	editor.commit();
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (myWebView != null){
 			myWebView.clearCache(true);
 			myWebView.clearHistory();
			myWebView.destroy();
 		}
 	}
 
 	/**
 	 * Displays loading dialog with progress
 	 */
 	public void setLoading(int progress) {
 		if (loadingDialog == null) {
 			loadingDialog = new ProgressDialog(this);
 			loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			loadingDialog.setMessage("Loading...Please wait");
 			loadingDialog.setCancelable(false);
 		}
 		if (!loadingDialog.isShowing())
 			loadingDialog.show();
 		loadingDialog.setProgress(progress);
 		if (progress == 100)
 			doneLoading();
 	}
 
 	/**
 	 * Disables loading dialog
 	 */
 	public void doneLoading() {
 		loadingDialog.dismiss();
 	}
 	
 	public void displayError(String message) {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setMessage(message)
 		.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				dialog.cancel();
 			}
 		});
 		builder.create().show();
 	}
 	
 }
