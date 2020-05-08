 package com.ebooka;
 
 import java.sql.Date;
 import java.util.Calendar;
 
 import com.ebooka.R;
 
 import com.ebooka.events.CurrentPageListener;
 import com.ebooka.events.DecodingProgressListener;
 import com.ebooka.models.CurrentPageModel;
 import com.ebooka.models.DecodingProgressModel;
 import com.ebooka.models.ZoomModel;
 import com.ebooka.views.MenuLayoutVisibilityEventListener;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.location.Location;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public abstract class BaseViewerActivity extends Activity implements
 		DecodingProgressListener, CurrentPageListener {
 	private static final int MENU_EXIT = 0;
 	private static final int MENU_GOTO = 1;
 	private static final int MENU_FULL_SCREEN = 2;
 	private static final int DIALOG_GOTO = 0;
 	private static final String DOCUMENT_VIEW_STATE_PREFERENCES = "DjvuDocumentViewState";
 	private DecodeService decodeService;
 	private DocumentView documentView;
 	private ViewerPreferences viewerPreferences;
 	private CurrentPageModel currentPageModel;
 
 	/**
 	 * Called when the activity is first created.
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		viewerPreferences = new ViewerPreferences(this);
 		setFullScreen();
 		setContentView(R.layout.document_view);
 		initDecodeService();
 		zoomModel = new ZoomModel();
 		final DecodingProgressModel progressModel = new DecodingProgressModel();
 		progressModel.addEventListener(this);
 		currentPageModel = new CurrentPageModel();
 		currentPageModel.addEventListener(this);
 
 		documentView = (DocumentView) findViewById(R.id.documentView);
 		documentView.init(zoomModel, progressModel, currentPageModel, findViewById(R.id.menuLayout));
 		
 
 		zoomModel.addEventListener(documentView);
 
 		decodeService.setContentResolver(getContentResolver());
 		decodeService.setContainerView(documentView);
 		documentView.setDecodeService(decodeService);
 		decodeService.open(getIntent().getData());
 
 		final SharedPreferences sharedPreferences = getSharedPreferences(
 				DOCUMENT_VIEW_STATE_PREFERENCES, 0);
 		currentPageIndex = sharedPreferences.getInt(getIntent().getData()
 				.toString(), 0);
 		documentView.goToPage(currentPageIndex);
 		documentView.showDocument();
 
 		viewerPreferences.addRecent(getIntent().getData());
 
 		((View) findViewById(R.id.zoomPlus)).setOnClickListener(onPlus);
 		((View) findViewById(R.id.zoomMinus)).setOnClickListener(onMinus);
 
 		((View) findViewById(R.id.nextPage)).setOnClickListener(onNextPage);
 		((View) findViewById(R.id.prevPage)).setOnClickListener(onPrevPage);
 
 		((View) findViewById(R.id.moveLeft)).setOnClickListener(onMoveLeft);
 		((View) findViewById(R.id.moveRight)).setOnClickListener(onMoveRight);
 		((View) findViewById(R.id.moveDefault)).setOnClickListener(onDefault);
 		((View) findViewById(R.id.prefDialog)).setOnClickListener(onPrefDialog);
 
 		MenuLayoutVisibilityEventListener menuControls = new MenuLayoutVisibilityEventListener(
 				this);
 		zoomModel.addEventListener(menuControls);
 		showCurrentTime();
 
 		View imageMenu = (View) findViewById(R.id.linearMenuLayout);
 		imageMenu.setOnClickListener(onMenu);
 	}
 
 	public View.OnClickListener onPrefDialog = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			String fullScreenTxt = "Full Screen "
 					+ (viewerPreferences.isFullScreen() ? "Off" : "On");
 			String items[] = { "Close Book", fullScreenTxt, "Exit" };
 			AlertDialog.Builder builder = new AlertDialog.Builder(
 					BaseViewerActivity.this);
 			builder.setTitle("Preferences");
 			builder.setItems(items, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int item) {
 					if (item == 0) {
 						finish();
 					} else if (item == 1) {
 						viewerPreferences.setFullScreen(!viewerPreferences
 								.isFullScreen());
 						finish();
 						startActivity(getIntent());
 					} else if (item == 2) {
 						exit();
 					}
 				}
 			});
 			builder.show();
 		}
 	};
 
 	public void exit() {
 		Intent e = new Intent(Intent.ACTION_MAIN);
 		e.addCategory(Intent.CATEGORY_HOME);
 		e.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 		finish();
 		startActivity(e);
 	}
 
 	public View.OnClickListener onMenu = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			zoomModel.toggleZoomControls();
 		}
 	};
 
 	public View.OnClickListener onDefault = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			// documentView.setInitialized(false);
 			// documentView.showDocument();
 		}
 	};
 
 	public View.OnClickListener onMoveLeft = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			documentView.scrollBy(1, 0);
 		}
 	};
 	public View.OnClickListener onMoveRight = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			documentView.scrollBy(-1, 0);
 		}
 	};
 
 	public View.OnClickListener onNextPage = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			documentView.scrollNextPage();
 
 		}
 	};
 	public View.OnClickListener onPrevPage = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			documentView.scrollPrevPage();
 
 		}
 	};
 
 	public View.OnClickListener onPlus = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			zoomModel.increaseZoom();
 			zoomModel.commit();
 		}
 	};
 	public View.OnClickListener onMinus = new View.OnClickListener() {
 
 		public void onClick(View arg0) {
 			zoomModel.decreaseZoom();
 			zoomModel.commit();
 		}
 	};
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		Log.d("DEBUG", "" + keyCode);
 		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == 94
 				|| keyCode == 92) {
 			documentView.scrollNextPage();
 		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == 95
 				|| keyCode == 93) {
 			documentView.scrollPrevPage();
 		} else {
 			return super.onKeyDown(keyCode, event);
 		}
 		return true;
 	};
 
 	private ZoomModel zoomModel;
 	private int currentPageIndex;
 	private View menuLayout;
 
 	public void decodingProgressChanged(final int currentlyDecoding) {
 		runOnUiThread(new Runnable() {
 			public void run() {
 				getWindow().setFeatureInt(
 						Window.FEATURE_INDETERMINATE_PROGRESS,
 						currentlyDecoding == 0 ? 10000 : currentlyDecoding);
 			}
 		});
 	}
 
 	private void showCurrentTime() {
 		Date date = new Date(System.currentTimeMillis());
 		java.text.DateFormat dateFormat = android.text.format.DateFormat
 				.getTimeFormat(getApplicationContext());
 		((TextView) findViewById(R.id.currentTime)).setText(dateFormat
 				.format(date));
 
 	}
 
 	public void currentPageChanged(int pageIndex) {
 		// page num
 		final String pageText = (pageIndex + 1) + " / "
 				+ decodeService.getPageCount();
 		((TextView) findViewById(R.id.currentPageIndex)).setText(pageText);
 		showCurrentTime();
 
 		saveCurrentPage();
 	}
 
 	private void setWindowTitle() {
 		final String name = getIntent().getData().getLastPathSegment();
 		//getWindow().setTitle(getResources().getResourceName(R.string.app_name));
 		setTitle(R.string.app_name);
 		((TextView) findViewById(R.id.bookName)).setText(name);
 	}
 
 	@Override
 	protected void onPostCreate(Bundle savedInstanceState) {
 		super.onPostCreate(savedInstanceState);
 		setWindowTitle();
 
 	}
 
 	private void setFullScreen() {
 		if (viewerPreferences.isFullScreen()) {
 			getWindow().requestFeature(Window.FEATURE_NO_TITLE);
 			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 					WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		} else {
 			getWindow().requestFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
 		}
 	}
 
 	private void initDecodeService() {
 		if (decodeService == null) {
 			decodeService = createDecodeService();
 		}
 	}
 
 	protected abstract DecodeService createDecodeService();
 
 	@Override
 	protected void onStop() {
 		super.onStop();
 	}
 
 	@Override
 	protected void onDestroy() {
 		decodeService.recycle();
 		decodeService = null;
 		super.onDestroy();
 	}
 
 	private void saveCurrentPage() {
 		final SharedPreferences sharedPreferences = getSharedPreferences(
 				DOCUMENT_VIEW_STATE_PREFERENCES, 0);
 		final SharedPreferences.Editor editor = sharedPreferences.edit();
 		editor.putInt(getIntent().getData().toString(),
 				documentView.getCurrentPage());
 		editor.commit();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		zoomModel.toggleZoomControls();
 		/*
 		 * menu.add(0, MENU_EXIT, 0, "Exit"); menu.add(0, MENU_GOTO, 0,
 		 * "Go to page"); final MenuItem menuItem = menu.add(0,
 		 * MENU_FULL_SCREEN, 0, "Full screen").setCheckable(true)
 		 * .setChecked(viewerPreferences.isFullScreen());
 		 * setFullScreenMenuItemText(menuItem);
 		 */
 		return false;
 	}
 
 	private void setFullScreenMenuItemText(MenuItem menuItem) {
 		menuItem.setTitle("Full screen "
 				+ (menuItem.isChecked() ? "on" : "off"));
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case MENU_EXIT:
 			System.exit(0);
 			return true;
 		case MENU_GOTO:
 			showDialog(DIALOG_GOTO);
 			return true;
 		case MENU_FULL_SCREEN:
 			item.setChecked(!item.isChecked());
 			setFullScreenMenuItemText(item);
 			viewerPreferences.setFullScreen(item.isChecked());
 
 			finish();
 			startActivity(getIntent());
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DIALOG_GOTO:
 			return new GoToPageDialog(this, documentView, decodeService);
 		}
 		return null;
 	}
 }
