 /*
  * Copyright (C) 2012 Pixmob (http://github.com/pixmob)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.pixmob.fm2.ui;
 
 import static org.pixmob.fm2.Constants.TAG;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.pixmob.fm2.R;
 import org.pixmob.fm2.model.Account;
 import org.pixmob.fm2.model.AccountRepository;
 import org.pixmob.fm2.net.AccountNetworkClient;
 import org.pixmob.fm2.services.SyncService;
 import org.pixmob.fm2.util.HttpUtils;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.LoaderManager.LoaderCallbacks;
 import android.support.v4.content.AsyncTaskLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.Menu;
 import android.support.v4.view.MenuItem;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.Toast;
 
import com.bugsense.trace.BugSenseHandler;

 /**
  * {@link Fragment} for displaying account details.
  * @author Pixmob
  */
 public class AccountDetailsFragment extends Fragment implements
         LoaderCallbacks<Set<String>> {
     private WebView webView;
     private View webLoadingPanel;
     private View webLoadingError;
     private boolean dualPane;
     
     public static AccountDetailsFragment newInstance(Account account) {
         final Bundle args = new Bundle(1);
         args.putSerializable("account", account);
         
         final AccountDetailsFragment f = new AccountDetailsFragment();
         f.setArguments(args);
         
         return f;
     }
     
     public Account getAccount() {
         return (Account) getArguments().getSerializable("account");
     }
     
     public void refresh() {
         onActionRefresh();
     }
     
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         super.onCreateOptionsMenu(menu, inflater);
         if (!dualPane) {
             menu.add(Menu.NONE, R.string.menu_refresh, Menu.NONE,
                 R.string.menu_refresh).setIcon(R.drawable.ic_menu_refresh)
                     .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
         }
         menu.add(Menu.NONE, R.string.menu_delete_account, Menu.NONE,
             R.string.menu_delete_account).setIcon(R.drawable.ic_menu_delete)
                 .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case R.string.menu_refresh:
                 onActionRefresh();
                 break;
             case R.string.menu_delete_account:
                 onActionDeleteAccount();
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
     
     private void onActionRefresh() {
         webLoadingPanel.setVisibility(View.VISIBLE);
         webLoadingError.setVisibility(View.GONE);
         webView.setVisibility(View.GONE);
         getLoaderManager().restartLoader(0, getArguments(), this);
     }
     
     private void onActionDeleteAccount() {
         ConfirmAccountDeletionDialogFragment.newInstance(getArguments()).show(
             getSupportFragmentManager(), "dialog");
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
         super.onActivityCreated(savedInstanceState);
         
         final View detailsFrame = getActivity().findViewById(
             R.id.account_details);
         dualPane = detailsFrame != null
                 && detailsFrame.getVisibility() == View.VISIBLE;
         
         setHasOptionsMenu(true);
         
         webLoadingError = getView()
                 .findViewById(R.id.account_web_loading_error);
         webLoadingPanel = getView()
                 .findViewById(R.id.account_web_loading_panel);
         webView = (WebView) getView().findViewById(R.id.account_web);
         webView.getSettings().setBuiltInZoomControls(true);
         webView.getSettings().setJavaScriptEnabled(true);
         webView.getSettings().setUserAgentString(
             HttpUtils.getUserAgent(getActivity()));
         webView.setWebViewClient(new WebViewClient() {
             @Override
             public void onPageFinished(WebView view, String url) {
                 webLoadingPanel.setVisibility(View.GONE);
                 webLoadingError.setVisibility(View.GONE);
                 webView.setVisibility(View.VISIBLE);
             }
             
             @Override
             public void onPageStarted(WebView view, String url, Bitmap favicon) {
                 webLoadingPanel.setVisibility(View.VISIBLE);
                 webLoadingError.setVisibility(View.GONE);
                 webView.setVisibility(View.GONE);
             }
             
             @Override
             public void onReceivedError(WebView view, int errorCode,
                     String description, String failingUrl) {
                 webLoadingPanel.setVisibility(View.GONE);
                 webLoadingError.setVisibility(View.VISIBLE);
                 webView.setVisibility(View.GONE);
             }
         });
         
         // Authenticate the user in a background task.
         // When authentication cookies are available, the method onLoadFinished
         // will be called, where we can load the account URL.
         getLoaderManager().initLoader(0, getArguments(), this);
     }
     
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,
             Bundle savedInstanceState) {
         return inflater.inflate(R.layout.account_details, container, false);
     }
     
     @Override
     public Loader<Set<String>> onCreateLoader(int id, Bundle args) {
         final Account account = args != null ? (Account) args
                 .getSerializable("account") : null;
         return new GetAuthCookies(getActivity(), account);
     }
     
     @Override
     public void onLoaderReset(Loader<Set<String>> loader) {
     }
     
     @Override
     public void onLoadFinished(Loader<Set<String>> loader, Set<String> cookies) {
         if (cookies != null && !cookies.isEmpty()) {
             // Build the Http Cookie header.
             final StringBuilder cookieHeader = new StringBuilder(128);
             for (final String cookie : cookies) {
                 if (cookieHeader.length() != 0) {
                     cookieHeader.append("; ");
                 }
                 cookieHeader.append(cookie);
             }
             
             final Map<String, String> httpHeaders = new HashMap<String, String>(
                     1);
             httpHeaders.put("Cookie", cookieHeader.toString());
             
             // Load the account URL with authentication cookies.
             webView.loadUrl(
                 "https://mobile.free.fr/moncompte/index.php?page=commande",
                 httpHeaders);
         }
     }
     
     /**
      * Task for getting authentication cookies.
      * @author Pixmob
      */
     private static class GetAuthCookies extends AsyncTaskLoader<Set<String>> {
         private final Account account;
         private Set<String> cookies;
         
         public GetAuthCookies(final Context context, final Account account) {
             super(context);
             this.account = account;
         }
         
         @Override
         protected void onStartLoading() {
             super.onStartLoading();
             
             if (cookies != null && !cookies.isEmpty()) {
                 deliverResult(cookies);
             } else {
                 forceLoad();
             }
         }
         
         @Override
         public Set<String> loadInBackground() {
             if (account != null) {
                 try {
                     return authenticate();
                 } catch (IOException e) {
                     Log.e(TAG, "Cannot load authentication cookies for user "
                             + account.login, e);
                    BugSenseHandler.log(TAG, e);
                 }
             }
             return Collections.emptySet();
         }
         
         private Set<String> authenticate() throws IOException {
             cookies = new HashSet<String>(4);
             final AccountNetworkClient client = new AccountNetworkClient(
                     getContext());
             if (!client.authenticate(account, cookies)) {
                 throw new IOException("Authentication failed for user "
                         + account.login);
             }
             return cookies;
         }
     }
     
     /**
      * Dialog for confirming account deletion.
      * @author Pixmob
      */
     private static class ConfirmAccountDeletionDialogFragment extends
             DialogFragment {
         public static ConfirmAccountDeletionDialogFragment newInstance(
                 Bundle arguments) {
             final ConfirmAccountDeletionDialogFragment f = new ConfirmAccountDeletionDialogFragment();
             f.setArguments(arguments);
             return f;
         }
         
         @Override
         public Dialog onCreateDialog(Bundle savedInstanceState) {
             return new AlertDialog.Builder(getActivity())
                     .setTitle(R.string.menu_delete_account)
                     .setIcon(android.R.drawable.ic_dialog_alert)
                     .setMessage(R.string.confirm_account_deletion)
                     .setPositiveButton(R.string.dialog_delete,
                         new DialogInterface.OnClickListener() {
                             @Override
                             public void onClick(DialogInterface dialog,
                                     int which) {
                                 final boolean dualPane = getSupportFragmentManager()
                                         .findFragmentById(R.id.accounts) != null;
                                 final Account account = (Account) getArguments()
                                         .getSerializable("account");
                                 new DeleteAccountTask(getActivity(), account,
                                         dualPane).execute();
                             }
                         }).setNegativeButton(R.string.dialog_cancel, null)
                     .create();
         }
     }
     
     /**
      * Background task for deleting an account.
      * @author Pixmob
      */
     private static class DeleteAccountTask extends AsyncTask<Void, Void, Void> {
         private final Activity context;
         private final Account account;
         private final boolean dualPane;
         
         public DeleteAccountTask(final Activity context, final Account account,
                 final boolean dualPane) {
             this.context = context;
             this.account = account;
             this.dualPane = dualPane;
         }
         
         @Override
         protected Void doInBackground(Void... params) {
             final AccountRepository accountRepository = new AccountRepository(
                     context);
             accountRepository.delete(account);
             return null;
         }
         
         @Override
         protected void onPostExecute(Void result) {
             super.onPostExecute(result);
             Toast.makeText(context,
                 context.getString(R.string.account_deleted), Toast.LENGTH_SHORT)
                     .show();
             
             if (dualPane) {
                 context.startService(new Intent(context, SyncService.class));
             } else {
                 context.finish();
             }
         }
     }
 }
