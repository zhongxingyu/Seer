 package com.intalker.borrow;
 
 import com.intalker.borrow.cloud.CloudAPI;
 import com.intalker.borrow.cloud.CloudAPIAsyncTask.ICloudAPITaskListener;
 import com.intalker.borrow.cloud.CloudUtility;
 import com.intalker.borrow.config.AppConfig;
 import com.intalker.borrow.config.ResultCode;
 import com.intalker.borrow.data.AppData;
 import com.intalker.borrow.data.BookInfo;
 import com.intalker.borrow.data.InitialCachedDataAsyncTask;
 import com.intalker.borrow.data.UserInfo;
 import com.intalker.borrow.isbn.ISBNResolver;
 import com.intalker.borrow.ui.book.BookGallery;
 import com.intalker.borrow.ui.book.BookShelfItem;
 import com.intalker.borrow.ui.book.BookShelfView;
 import com.intalker.borrow.ui.control.sliding.SlidingMenu;
 import com.intalker.borrow.ui.login.RegisterView;
 import com.intalker.borrow.ui.navigation.NavigationPanel;
 import com.intalker.borrow.ui.social.SocialPanel;
 import com.intalker.borrow.util.DBUtil;
 import com.intalker.borrow.util.DensityAdaptor;
 import com.intalker.borrow.util.StorageUtil;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.view.Window;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class HomeActivity extends Activity {
 	private static HomeActivity app = null;
 	private BookGallery mBookGallery = null;
 	private NavigationPanel mNavigationPanel = null;
 	private SocialPanel mSocialPanel = null;
 
 	private RegisterView mReg = null;
 
 	private SlidingMenu mSlidingMenu = null;
 	
 	private boolean mIsRegUIShown = false;
 
 	public void toggleLeftPanel() {
 		mSlidingMenu.toggleLeftView();
 	}
 
 	public void toggleRightPanel() {
 		mSlidingMenu.toggleRightView();
 	}
 
 	public static HomeActivity getApp() {
 		return app;
 	}
 
 	public BookGallery getBookGallery() {
 		return mBookGallery;
 	}
 
 	public SocialPanel getSocialPanel() {
 		return mSocialPanel;
 	}
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		app = this;
 		StorageUtil.initialize();
 		AppData.getInstance().initialize();
 		DensityAdaptor.init(this);
 		if (!AppConfig.disableAutoLoadCache) {
 			StorageUtil.loadCachedBooks();
 		}
 		CloudAPI.CloudToken = "";
 
 		setContentView(initializeWithSlidingStyle());
 		this.mSlidingMenu.invalidate();
 		
 		tryAutoLogin();
 	}
 	
 	private void tryAutoLogin() {
 		if (CloudUtility.setAccessToken(DBUtil.loadToken())) {
 			CloudAPI.getLoggedInUserInfo(this, new ICloudAPITaskListener() {
 
 				@Override
 				public void onFinish(int returnCode) {
 					doAfterGetUserInfoByToken(returnCode);
 				}
 
 			});
 		}
 	}
 
 	private View initializeWithSlidingStyle() {
 		mSlidingMenu = new SlidingMenu(this);
 		mSocialPanel = new SocialPanel(this);
 		mNavigationPanel = new NavigationPanel(this);
 
 		mSlidingMenu.setLeftView(mNavigationPanel);
 		mSlidingMenu.setRightView(mSocialPanel);
 		mSlidingMenu.setCenterView(createHomeUI());
 		return mSlidingMenu;
 	}
 
 	// private View createLeftNavigationPanel()
 	// {
 	// RelativeLayout naviPanel = new RelativeLayout(this);
 	// Button btn = new Button(this);
 	// RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
 	// RelativeLayout.LayoutParams.WRAP_CONTENT,
 	// RelativeLayout.LayoutParams.WRAP_CONTENT);
 	// lp.width = LayoutUtil.getNavigationPanelWidth();
 	// naviPanel.addView(btn, lp);
 	// return naviPanel;
 	// }
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// getMenuInflater().inflate(R.menu.activity_home, menu);
 		menu.addSubMenu("Settings");
 		menu.addSubMenu("Help");
 		menu.addSubMenu("About");
 		menu.addSubMenu("Exit");
 		return true;
 	}
 
 	private void doAfterGetUserInfoByToken(int returnCode) {
 		if (CloudAPI.isSuccessful(this, returnCode)) {
 			mBookGallery.updatePanels(UserInfo.getCurLoggedinUser());
 			mSocialPanel.getFriendsView().refreshList();
 
 			if (AppConfig.disableAutoLoadCache) {
 				AlertDialog alertDialog = new AlertDialog.Builder(this)
 						.setTitle("test")
 						.setMessage("msg")
 						.setIcon(R.drawable.question)
 						.setPositiveButton("OK",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 										InitialCachedDataAsyncTask initCachedDataTask = new InitialCachedDataAsyncTask();
 										initCachedDataTask.execute();
 									}
 								})
 						.setNegativeButton("cancel",
 								new DialogInterface.OnClickListener() {
 
 									@Override
 									public void onClick(DialogInterface dialog,
 											int which) {
 									}
 								}).create();
 				alertDialog.show();
 			}
 		}
 	}
 	
 	public void toggleSignUpPanel(boolean show) {
 		mIsRegUIShown = show;
 		if (show) {
 			mSlidingMenu.toggleLeftView();
 			mBookGallery.setVisibility(View.GONE);
 			mReg.setVisibility(View.VISIBLE);
 		} else {
 			mSlidingMenu.toggleLeftView();
 			mBookGallery.setVisibility(View.VISIBLE);
 			mReg.setVisibility(View.GONE);
 		}
 	}
 
 	public View createHomeUI() {
 		BookShelfItem.lastBookForTest = null;
 
 		LinearLayout mainLayout = new LinearLayout(this);
 
 		// book gallery ui
 		mBookGallery = new BookGallery(this);
 		LinearLayout.LayoutParams bookGalleryLP = new LinearLayout.LayoutParams(
 				LinearLayout.LayoutParams.FILL_PARENT,
 				LinearLayout.LayoutParams.FILL_PARENT);
 		mainLayout.addView(mBookGallery, bookGalleryLP);
 
 		mBookGallery.initialWithCachedData();
 
 		mReg = new RegisterView(this);
 		mainLayout.addView(mReg);
 		mReg.setVisibility(View.GONE);
 		mReg.setRegisterListener(new RegisterView.OnRegisterListener() {
 
 			@Override
 			public void onSuccess() {
 				HomeActivity app = HomeActivity.getApp();
 				app.toggleSignUpPanel(false);
 				app.getBookGallery().updatePanels(UserInfo.getCurLoggedinUser());
 				app.getSocialPanel().getFriendsView().refreshList();
 			}
 
 			@Override
 			public void onBack() {
 //				mReg.setVisibility(View.GONE);
 //				mBookGallery.setVisibility(View.VISIBLE);
 			}
 		});
 
 		// Test settings
 		mainLayout.setBackgroundColor(Color.GRAY);
 		return mainLayout;
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (requestCode == ResultCode.SCAN_RESULT_CODE) {
 			switch (resultCode) {
 			case RESULT_OK:
 				String isbn = data.getStringExtra("SCAN_RESULT");
 				int length = isbn.length();
 				if (10 == length || 13 == length) {
 					if(AppConfig.useSQLiteForCache)
 					{
 						BookInfo bookInfo = DBUtil.getBookInfo(isbn);
 						if (null != bookInfo) {
 							AppData.getInstance().addBook(bookInfo);
 							BookShelfView.getInstance().addBookByExistingInfo(
 									bookInfo);
 							break;
 						}
 					}
 					ISBNResolver.getInstance().getBookInfoByISBN(this, isbn);
 				} else {
 					Toast.makeText(this, this.getString(R.string.invalid_isbn),
 							Toast.LENGTH_SHORT).show();
 				}
 				break;
 			case RESULT_CANCELED:
 				break;
 			default:
 				break;
 			}
 		}
 	}
 
 	@Override
 	protected void onDestroy() {
 		// TODO Auto-generated method stub
 		super.onDestroy();
 		StorageUtil.saveCachedBooks();
 		DBUtil.uninitialize();
 	}
 
 	@Override
 	public void onBackPressed() {
 		if (mIsRegUIShown) {
 			toggleSignUpPanel(false);
 			return;
 		}
 		String confirm = this.getString(R.string.confirm);
 		String quitConfirm = this.getString(R.string.quit_confirm);
 		new AlertDialog.Builder(this)
 				.setTitle(confirm)
 				.setMessage(quitConfirm)
 				.setIcon(R.drawable.question)
 				.setPositiveButton(R.string.ok,
 						new DialogInterface.OnClickListener() {
 
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								HomeActivity.getApp().finish();
 							}
 						})
 				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
 
 					@Override
 					public void onClick(DialogInterface dialog, int which) {
 					}
 				}).show();
 	}
 
 //	@Override
 //	protected void onStop() {
 //		// TODO Auto-generated method stub
 //		super.onStop();
 //		StorageUtil.saveCachedBooks();
 //	}
 }
