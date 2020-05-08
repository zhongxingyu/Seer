 /*
  *  Copyright (c) 2012-2013, The Linux Foundation. All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are
  *  met:
  *      * Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *     * Redistributions in binary form must reproduce the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer in the documentation and/or other materials provided
  *       with the distribution.
  *     * Neither the name of The Linux Foundation nor the names of its
  *       contributors may be used to endorse or promote products derived
  *       from this software without specific prior written permission.
  *
  *  THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
  *  WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
  *  MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
  *  ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
  *  BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  *  SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  *  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  *  WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
  *  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
  *  IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  */
 
 
 package com.mogoweb.browser;
 
 import org.chromium.base.PathUtils;
 import org.chromium.content.browser.ContentView;
 import org.chromium.content.browser.ContentViewRenderView;
 
 import android.app.AlertDialog;
 import android.content.ClipData;
 import android.content.ClipboardManager;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.drawable.BitmapDrawable;
 import android.os.Handler;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.MeasureSpec;
 import android.view.ViewGroup;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.PopupWindow;
 
 import com.mogoweb.browser.Intention.Type;
 import com.mogoweb.browser.TabManager.TabData;
 import com.mogoweb.browser.preferences.BrowserPreferenceActivity;
 import com.mogoweb.browser.preferences.BrowserPreferences;
 import com.mogoweb.browser.utils.Logger;
 import com.mogoweb.browser.views.DesignShared;
 import com.mogoweb.browser.web.Readability;
 import com.mogoweb.browser.web.WebTab;
 
 public class BrowserUi implements Tab.Listener, TabManager.Listener, ToolbarUi.Listener {
 
     private final BrowserActivity mActivity;
     private final Context mContext;
 
     private final TabManager mTabManager;
     private final BookmarkManager mBookmarkManager;
     private final ToolbarUi mToolbarUi;
     private final BrowserPreferences mBrowserPrefs;
     private final MemoryMonitor mMemoryMonitor;
 
     // views referenced
     private HomeScreen mHomeView;
     private TabScreen mTabScreen;
     FrameLayout mTabContainer;
     private ContentViewRenderView mRenderTarget;
 
     private View mOldTabView; // saved for remove when add new tab
 
     private ImageView mMenuBookmark;
     private ImageView mMenuBack;
     private ImageView mMenuForward;
     private PopupWindow mMenuPopupWindow;
     private Button mMenuAboutBuildButton;
     private Button mMenuSettingsButton;
     private Button mMenuShowHistoryButton;
     private Button mMenuReadModeButton;
 
     // state
     private boolean mHomeViewShown;
     private boolean mArrivedFromHomeButton;
 
     private String mContextMenuUrl;
     private int mContextMenuType;
     private String mContextMenuText;
 
     protected static final FrameLayout.LayoutParams COVER_SCREEN_PARAMS =
             new FrameLayout.LayoutParams(
                     ViewGroup.LayoutParams.MATCH_PARENT,
                     ViewGroup.LayoutParams.MATCH_PARENT);
 
     public BrowserUi(BrowserActivity browserActivity) {
         mActivity = browserActivity;
         mContext = browserActivity;
         mHomeViewShown = false;
         mArrivedFromHomeButton = false;
 
         DesignShared.initialize(mActivity.getResources());
 
         mTabManager = TabManager.create(mActivity);
         mTabManager.addListener(this);
 
         MostFrequentManager.create(mContext);
 
         mBookmarkManager = BookmarkManager.create(mContext);
 
         mBrowserPrefs = BrowserPreferences.create(mContext.getApplicationContext());
 
         mMemoryMonitor = MemoryMonitor.create(mContext);
 
         // inflate the main layout and reference the sub-components
         mActivity.setContentView(R.layout.main);
 
         mToolbarUi = new ToolbarUi(mActivity.findViewById(R.id.toolbar), mContext);
         mToolbarUi.setListener(this);
 
         mTabContainer = (FrameLayout) mActivity.findViewById(R.id.tab_main_container);
         Logger.warn("creating RenderTarget");
         mRenderTarget = new ContentViewRenderView(mActivity) {
             @Override
             protected void onReadyToRender() {
 
             }
         };
         mTabContainer.addView(mRenderTarget, COVER_SCREEN_PARAMS);
 
         mHomeView = (HomeScreen) mActivity.findViewById(R.id.home_view);
         mTabScreen = (TabScreen) mActivity.findViewById(R.id.tab_screen);
 
         mActivity.registerForContextMenu(mTabContainer);
 
         // restore the last session, or (even if crashed) an empty session
         final boolean isThereASession = false; // TODO
         if (isThereASession) {
             Logger.notImplemented("restore the session");
             return;
         }
 
         // show the last active tab if any
         if (mTabManager.getActiveTabData() != null) {
             onTabSelected(mTabManager.getActiveTabData(), true);
         }
 
         showHomeScreen();
     }
 
     public boolean handleIntent(Intent intent) {
         // if there is nothing to do, return
         if (intent == null)
             return false;
 
         String action = intent.getAction();
         String dataString = intent.getDataString();
 
         if (action == "tab:welcome") {
             mTabManager.setTabIntention(new Intention(Type.I_Welcome));
             return true;
         } else if (action == "tab:next" || action == "tab:prev") {
             long startTime = System.currentTimeMillis();
             boolean bSwitched = mTabManager.switchTab(action == "tab:next" ? true : false);
 
             if (bSwitched)
                 Logger.debug("Tab switched in ms: " + (System.currentTimeMillis() - startTime));
             else
                 Logger.debug("Tab switch not attempted");
 
             return true;
         } else if (action == "tab:close") {
             TabData td = mTabManager.getActiveTabData();
             mTabManager.closeTab(td);
             return true;
         }
 
         if (dataString == null) {
             return false;
         }
 
         if (action == "tab:new") {
             mTabManager.setTabIntention(new Intention(Type.I_OpenAndConsume, dataString));
             return true;
         } else if (action == "tab:load") {
             mTabManager.setTabIntention(new Intention(Type.I_Consume, dataString));
             return true;
         }
 
         // catch-all: intent with data, load the data in new tab
         loadUrlInNewTab(dataString);
         return true;
     }
 
     public void createNewWelcomeTab() {
         // add a new welcome tab (empty tab)
         mTabManager.setTabIntention(new Intention(Type.I_Welcome));
 
         // fade in the control menu after 100ms
         // If mHideHomeScreen is set to true don't show HomeScreen
         // This is required for running gpu benchmarks.
         if (!BrowserActivity.requestedGpuBenchmarkMode()) {
             new Handler().postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     showHomeScreen();
                 }
             }, 300);
         }
     }
 
     public void loadUrlInNewTab(String url) {
         mTabManager.setTabIntention(new Intention(Type.I_OpenAndConsume, url));
     }
 
     public void onStart() {
     }
 
     public void onResume() {
         mTabManager.onActivityResume();
     }
 
     public void onPause() {
         mTabManager.onActivityPause();
     }
 
     public void onDestroy() {
         mTabManager.removeListener(this);
         mTabContainer.removeAllViews();
     }
 
     public void onCreateContextMenu(ContextMenu menu, View v,
                                 ContextMenuInfo menuInfo) {
         MenuInflater inflater = mActivity.getMenuInflater();
         inflater.inflate(R.menu.webpage_context_menu, menu);
         menu.setHeaderTitle(mContextMenuUrl);
         if (mContextMenuType == MediaType.MEDIA_TYPE_IMAGE) {
             menu.setGroupVisible(R.id.IMAGE_MENU, true);
         } else {
             menu.setGroupVisible(R.id.IMAGE_MENU, false);
         }
 
         MenuItem copyLinkText = menu.findItem(R.id.copy_link_text_context_menu_id);
         if (mContextMenuText.isEmpty()) {
             copyLinkText.setVisible(false);
         } else {
             copyLinkText.setVisible(true);
         }
     }
 
     public boolean onContextItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.id.open_in_new_tab_context_menu_id:
                 mTabManager.setTabIntention(new Intention(Type.I_OpenAndConsume, mContextMenuUrl));
                 return true;
             case R.id.copy_link_address_context_menu_id: {
                 ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                 clipboard.setPrimaryClip(ClipData.newPlainText("URL", mContextMenuUrl));
                 return true;
             }
             case R.id.copy_link_text_context_menu_id: {
                 ClipboardManager clipboard = (ClipboardManager) mActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                 clipboard.setPrimaryClip(ClipData.newPlainText("TEXT", mContextMenuText));
                 return true;
             }
             default:
                 return mActivity.onContextItemSelected(item);
         }
     }
 
     private void resetFocus() {
         FrameLayout container = (FrameLayout) mActivity.findViewById(R.id.browser_activity_root);
         container.requestFocus();
         InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(container.getWindowToken(), 0);
     }
 
     private void focusContentView() {
         mTabContainer.clearFocus();
         mTabContainer.requestFocus();
 
         InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(mTabContainer.getWindowToken(), 0);
     }
 
     private void showTabScreen() {
         mTabScreen.updateTabTiles();
         mTabScreen.setVisibility(View.VISIBLE);
         mTabScreen.bringToFront();
     }
 
     private void hideTabScreen() {
         mTabScreen.setVisibility(View.GONE);
     }
 
     private void showHomeScreen() {
         if (mHomeViewShown)
             return;
 
         resetFocus();
         Logger.debug("Showing HomeScreen");
 
         mHomeView.updateMostFrequentTiles();
         mHomeView.updateBookmarkTiles();
 
         mHomeView.setVisibility(View.VISIBLE);
         mHomeViewShown = true;
     }
 
     private void hideHomeScreen() {
         if (!mHomeViewShown)
             return;
 
         resetFocus();
         Logger.debug("Hiding HomeScreen");
 
         mHomeView.setVisibility(View.GONE);
         mHomeViewShown = false;
     }
 
 
     @Override
     public void onToolbarPageSetIntention(Intention i) {
         // handle the new intention
         mTabManager.setTabIntention(i);
 
         // remove the focus from the toolbar, especially useful if the
         // intention came from the smartbox completion
         resetFocus();
 
         // show the tab
         hideHomeScreen();
     }
 
     @Override
     public void onToolbarPageStop() {
         // ask the tab implementation to stop loading (if still loading)
         mTabManager.getActiveTab().stopLoading();
     }
 
     @Override
     public void onToolbarPageReload() {
         // ask the tab implementation to reload
         mTabManager.getActiveTab().reload();
     }
 
     @Override
     public void onToolbarToggleTabs() {
         showTabScreen();
     }
 
     @Override
     public void onToolbarToggleOverflowMenu() {
         LayoutInflater inflater = this.mActivity.getLayoutInflater();
         View menuView = inflater.inflate(R.layout.menu_custom_overflow, null, false);
         menuView.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
         mMenuPopupWindow = new PopupWindow(menuView, (int) Math.max(
                 menuView.getMeasuredWidth(), 240 * DesignShared.DP_TO_PX),
                 menuView.getMeasuredHeight(), false);
         mMenuPopupWindow.setOutsideTouchable(true);
         mMenuPopupWindow.setFocusable(true);
         mMenuPopupWindow.setBackgroundDrawable(new BitmapDrawable(this.mActivity
                 .getResources()));
         mMenuPopupWindow.setOnDismissListener(mMenuDismissListener);
 
         //Setting up click listener event for all buttons in Menu Item
         mMenuBack = (ImageView) menuView.findViewById(R.id.menu_nav_back);
         mMenuBack.setOnClickListener(mMenuItemClickListener);
         mMenuBack.setEnabled(mTabManager.getActiveTab().canGoBack());
 
         mMenuForward = (ImageView) menuView.findViewById(R.id.menu_nav_forward);
         mMenuForward.setOnClickListener(mMenuItemClickListener);
         mMenuForward.setEnabled(mTabManager.getActiveTab().canGoForward());
 
         mMenuBookmark = (ImageView) menuView.findViewById(R.id.menu_nav_bookmark);
         mMenuBookmark.setOnClickListener(mMenuItemClickListener);
         mMenuForward.setEnabled(mTabManager.getActiveTab().canGoForward());
 
         mMenuAboutBuildButton = (Button) menuView.findViewById(R.id.menu_about_build);
         mMenuAboutBuildButton.setOnClickListener(mMenuItemClickListener);
 
         mMenuSettingsButton = (Button) menuView.findViewById(R.id.menu_settings);
         mMenuSettingsButton.setOnClickListener(mMenuItemClickListener);
 
         mMenuShowHistoryButton = (Button) menuView.findViewById(R.id.menu_history);
         mMenuShowHistoryButton.setOnClickListener(mMenuItemClickListener);
 
         mMenuReadModeButton = (Button) menuView.findViewById(R.id.menu_readmode);
         mMenuReadModeButton.setOnClickListener(mMenuItemClickListener);
 
         // Set the Bookmark if visible
         needsBookMarkUpdate(mTabManager.getActiveTab().getUrl());
 
         mMenuPopupWindow.showAsDropDown(mToolbarUi.getMenuAnchor());
     }
 
     private final PopupWindow.OnDismissListener mMenuDismissListener = new PopupWindow.OnDismissListener() {
         @Override
         public void onDismiss() {
             mMenuPopupWindow.setFocusable(false);
             mMenuPopupWindow = null;
         }
     };
 
 
     private final View.OnClickListener mMenuItemClickListener = new View.OnClickListener() {
         @Override
         public void onClick(View v) {
             switch (v.getId()) {
             case R.id.menu_nav_back:
                 mTabManager.getActiveTab().goBack();
                 break;
 
             case R.id.menu_nav_forward:
                 mTabManager.getActiveTab().goForward();
                 break;
 
             case R.id.menu_nav_bookmark:
                 addBookmarkDialog();
                 break;
 
             case R.id.menu_about_build:
                 displayAboutBuildInformation();
                 break;
             case R.id.menu_history:
                 displayHistory();
                 break;
             case R.id.menu_readmode:
                 ContentView contentView = mTabManager.getActiveTab().getContentView();
                 String js = Readability.JS;
                js.replace("__READABILITY_JS__", getResourcesFilename(Readability.READABILITY_JS));
                js.replace("__READABILITY_CSS__", getResourcesFilename(Readability.READABILITY_CSS));
                js.replace("__READABILITY_PRINT_CSS__", getResourcesFilename(Readability.READABILITY_PRINT_CSS));
                 contentView.evaluateJavaScript(js);
                 break;
 
             case R.id.menu_settings:
                 Intent myIntent = new Intent(mContext, BrowserPreferenceActivity.class);
                 mContext.startActivity(myIntent);
                 break;
             }
             mMenuPopupWindow.dismiss();
         }
     };
 
     @Override
     public void onTabAdded(TabData td, int idx) {}
 
     @Override
     public void onTabRemoved(TabData td, int idx) {
         // //disable home button if last tab was deleted
         // if (mTabManager.getTabsCount() == 0 && mHomeViewShown) {
         //     mToolbarUi.setHomeButtonDisabled(true);
         // }
     }
 
     private void resetToolbarUi(TabData td) {
         if (td == null) {
             mToolbarUi.setCurrentProgress(0);
             mToolbarUi.setCurrentUrl("");
         } else {
             mToolbarUi.setCurrentProgress(td.tab.getCurrentLoadProgress());
             mToolbarUi.setCurrentIsLoading(td.tab.getCurrentLoadProgress() != 0);
             mToolbarUi.setCurrentUrl(td.tab.getUrl());
         }
     }
 
     //If selected, the view may also have changed
     @Override
     public void onTabSelected(TabData td, boolean bSelected) {
 
         Logger.debug("onTabSelected: " + td + ": " + bSelected);
 
         if (mOldTabView != null)
             mTabContainer.removeView(mOldTabView);
 
         if (bSelected == false) {
             resetToolbarUi(null);
             td.tab.onHide();
             td.tab.removeListener(this);
             return;
         }
 
         if (td.tab instanceof WebTab) {
             Logger.debug("show new tab");
             td.tab.onShow();
             ContentView contentView = td.tab.getContentView();
             mRenderTarget.setCurrentContentView(contentView);
             mTabContainer.addView(contentView);
             mOldTabView = contentView;
         }
 
         td.tab.addListener(this);
         resetToolbarUi(td);
     }
 
 
     @Override
     public void onTabShow(TabData tab) {
         Logger.debug("onTabShow");
         if (tab.tab.getEmbodiment() == Tab.Embodiment.E_Welcome) {
             hideTabScreen();
             showHomeScreen();
         } else {
             hideTabScreen();
             hideHomeScreen();
         }
     }
 
     // Tab.Listener implementation
     @Override
     public void onLoadProgressChanged(int progress) {
         mToolbarUi.setCurrentProgress(progress);
         if (progress == 100)
             mToolbarUi.setCurrentProgress(0);
     }
 
     @Override
     public void onUpdateUrl(String url) {
         mToolbarUi.setCurrentUrl(url);
     }
 
     @Override
     public void onLoadStarted(boolean isMainFrame) {
         if (isMainFrame) {
             mToolbarUi.setCurrentIsLoading(true);
             focusContentView();
         }
     }
 
     @Override
     public void onLoadStopped(boolean isMainFrame) {
         if (isMainFrame) {
             mToolbarUi.setCurrentIsLoading(false);
             focusContentView();
         }
     }
 
     @Override
     public void didFailLoad(boolean isProvisionalLoad, boolean isMainFrame, int errorCode,
             String description, String failingUrl) {
 
     }
 
     @Override
     public void showContextMenu(String url, int mediaType, String linkText,
             String unfilteredLinkUrl, String srcUrl) {
         mContextMenuUrl = url;
         mContextMenuType = mediaType;
         mContextMenuText = linkText;
         mActivity.openContextMenu(mTabContainer);
     }
 
     @Override
     public void didStartLoading(String url) {
     }
 
     @Override
     public void didStopLoading(String url) {
     }
 
     private void addBookmarkDialog() {
         // Get the url and title from tab
         String title = null;
         CharSequence unfilteredUrl = null;
         AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
         LayoutInflater inflater = (LayoutInflater) mContext.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
         View bookmarkView = inflater.inflate(R.layout.bookmark_add, null);
 
         builder.setView(bookmarkView);
 
         title = mTabManager.getActiveTab().getTitle().trim();
         unfilteredUrl = mTabManager.getActiveTab().getUrl();
 
         // Populate the title & URL
         final EditText titleBox = ((EditText)bookmarkView.findViewById(R.id.add_bookmark_title));
         final EditText addressBox = ((EditText)bookmarkView.findViewById(R.id.add_bookmark_address));
 
         titleBox.setText(title);
         addressBox.setText(unfilteredUrl);
 
         builder.setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                @Override
                 public void onClick(DialogInterface dialog, int id) {
                     mBookmarkManager.addBookmark(titleBox.getText().toString().trim(), addressBox.getText().toString().trim());
                     if (mHomeView != null)
                         mHomeView.updateBookmarkTiles();
                 }
         });
 
         builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                }
         });
 
         builder.show();
     }
 
     private void needsBookMarkUpdate(String url) {
         if (mMenuBookmark == null)
             return;
 
         if (url != null) {
             mMenuBookmark.setEnabled(true);
             Bookmark bookmark = mBookmarkManager.getBookmark(url);
             if (bookmark != null)
                 mMenuBookmark.setImageResource(R.drawable.ic_action_pin_pressed);
             else
                 mMenuBookmark.setImageResource(R.drawable.ic_action_pin_normal);
         } else
             mMenuBookmark.setEnabled(false);
     }
 
     public boolean handleBackButton() {
         boolean handledByApp = false;
 
         if (mHomeViewShown) {
             if (mArrivedFromHomeButton) {
                 hideHomeScreen();
                 handledByApp = true;
             }
         } else if (mTabManager.getActiveTab().canGoBack()) {
             mTabManager.getActiveTab().goBack();
             handledByApp = true;
         } else if (mTabManager.hasParent(mTabManager.getActiveTabData())) {
             mTabManager.closeTab(mTabManager.getActiveTabData());
             //if closing tab results in another tab activated, we're done
             if (mTabManager.getTabsCount() != 0) {
                 handledByApp = true;
             }
         } else if (!mHomeViewShown) {
             showHomeScreen();
             handledByApp = true;
         }
 
         return handledByApp;
     }
 
     /**
     * Fetches the version Information from the chrome:://version and displays it
     */
     private void displayAboutBuildInformation() {
         loadUrlInNewTab("chrome://version");
     }
 
     private void displayHistory() {
         loadUrlInNewTab("chrome://history");
     }
 
     private String getResourcesFilename(String file) {
         return "http://mogoweb.net/" + file;
     }
 }
