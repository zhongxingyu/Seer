 /*******************************************************************************
  * Copyright (c) 2012 MASConsult Ltd
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package eu.masconsult.bgbanking.activity.fragment;
 
 import java.util.ArrayList;
 
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.accounts.OnAccountsUpdateListener;
 import android.app.Activity;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.support.v4.app.LoaderManager;
 import android.support.v4.content.CursorLoader;
 import android.support.v4.content.Loader;
 import android.support.v4.view.MenuItemCompat;
 import android.support.v4.widget.CursorAdapter;
 import android.support.v4.widget.ResourceCursorAdapter;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.TextView;
 
 import com.commonsware.cwac.merge.MergeAdapter;
 
 import eu.masconsult.bgbanking.BankingApplication;
 import eu.masconsult.bgbanking.R;
 import eu.masconsult.bgbanking.banks.Bank;
 import eu.masconsult.bgbanking.provider.BankingContract;
 
 public class AccountsListFragment extends ListFragment implements
         LoaderManager.LoaderCallbacks<Cursor>, OnAccountsUpdateListener {
 
     protected static final String TAG = BankingApplication.TAG + "AccountsListFragment";
 
     // This is the Adapter being used to display the list's data.
     MyMergeAdapter mAdapter;
 
     // If non-null, this is the current filter the user has provided.
     String mCurFilter;
 
     final ArrayList<Account> mAccounts = new ArrayList<Account>();
 
     private AccountManager accountManager;
 
     @Override
     public void onActivityCreated(Bundle savedInstanceState) {
        Log.v(TAG, "onActivityCreated");
         super.onActivityCreated(savedInstanceState);
 
         // Give some text to display if there is no data. In a real
         // application this would come from a resource.
         setEmptyText("No bank accounts");
 
         // We have a menu item to show in action bar.
         setHasOptionsMenu(true);
 
         // Create an empty adapter we will use to display the loaded data.
         mAdapter = new MyMergeAdapter();
         setListAdapter(mAdapter);
 
         // Start out with a progress indicator.
         setListShown(false);
     }
 
     private void populateList() {
         // Prepare the loader. Either re-connect with an existing one,
         // or start a new one.
         Bank[] banks = Bank.values();
         for (Bank bank : banks) {
             Account[] accounts = accountManager.getAccountsByType(bank
                     .getAccountType(getActivity()));
             for (Account account : accounts) {
                 TextView view = (TextView) getActivity().getLayoutInflater().inflate(
                         R.layout.row_bank_account_header,
                         null);
                 view.setText(getString(bank.labelRes) + " - " + account.name);
                 view.setCompoundDrawablesWithIntrinsicBounds(bank.iconResource, 0, 0, 0);
                 mAdapter.addView(view);
                 mAdapter.addAdapter(new BankAccountsAdapter(getActivity(),
                         R.layout.row_bank_account,
                         null, 0));
 
                 int idx = mAccounts.size();
                 mAccounts.add(idx, account);
                 getLoaderManager().initLoader(idx, null, this);
             }
         }
 
         Log.v(TAG, "found " + mAccounts.size() + " accounts");
     }
 
     @Override
     public void onAttach(Activity activity) {
         Log.v(TAG, "onAttach");
 
         super.onAttach(activity);
 
         accountManager = AccountManager.get(getActivity());
         accountManager.addOnAccountsUpdatedListener(this, null, true);
     }
 
     @Override
     public void onDetach() {
         accountManager.removeOnAccountsUpdatedListener(this);
         accountManager = null;
 
         super.onDetach();
     }
 
     static String formatIBAN(String string) {
         StringBuilder sb = new StringBuilder();
         int i = 4;
         while (i < string.length()) {
             sb.append(string.substring(i - 4, i)).append(' ');
             i += 4;
         }
         sb.append(string.substring(i - 4));
         return sb.toString();
     }
 
     @Override
     public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
         MenuItem refreshItem = menu.add("Refresh");
         refreshItem.setIcon(R.drawable.ic_menu_refresh);
         MenuItemCompat.setShowAsAction(refreshItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);
         refreshItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 AccountManager accountManager = AccountManager.get(getActivity());
                if (accountManager == null) {
                    return false;
                }

                 Bank[] banks = Bank.values();
                 for (Bank bank : banks) {
                     for (Account account : accountManager.getAccountsByType(bank
                             .getAccountType(getActivity()))) {
                         Log.v(TAG,
                                 "account: "
                                         + account.name
                                         + ", "
                                         + account.type
                                         + ", "
                                         + ContentResolver.getIsSyncable(account,
                                                 BankingContract.AUTHORITY));
                         Bundle bundle = new Bundle();
                         bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
                         ContentResolver.requestSync(account, BankingContract.AUTHORITY, bundle);
                     }
                 }
                 return true;
             }
         });
     }
 
     @Override
     public void onListItemClick(ListView l, View v, int position, long id) {
         // Insert desired behavior here.
         Log.i("FragmentComplexList", "Item clicked: " + id);
     }
 
     // These are the Contacts rows that we will retrieve.
     static final String[] ACCOUNTS_SUMMARY_PROJECTION = new String[] {
             BankingContract.BankAccount._ID,
             BankingContract.BankAccount.COLUMN_NAME_IBAN,
             BankingContract.BankAccount.COLUMN_NAME_CURRENCY,
             BankingContract.BankAccount.COLUMN_NAME_BALANCE,
             BankingContract.BankAccount.COLUMN_NAME_AVAILABLE_BALANCE,
             BankingContract.BankAccount.COLUMN_NAME_LAST_TRANSACTION_DATE,
     };
 
     @Override
     public Loader<Cursor> onCreateLoader(int id, Bundle args) {
         // This is called when a new Loader needs to be created. This
         // sample only has one Loader, so we don't care about the ID.
         // First, pick the base URI to use depending on whether we are
         // currently filtering.
         Uri baseUri = BankingContract.BankAccount.CONTENT_URI;
 
         Account account = mAccounts.get(id);
         Log.v(TAG, "creating cursor loader for account: " + account);
 
         // Now create and return a CursorLoader that will take care of
         // creating a Cursor for the data being displayed.
         return new CursorLoader(getActivity(), baseUri, ACCOUNTS_SUMMARY_PROJECTION,
                 BankingContract.BankAccount.ACCOUNT_NAME
                         + "=? AND " + BankingContract.BankAccount.ACCOUNT_TYPE + "=?",
                 new String[] {
                         account.name, account.type
                 }, BankingContract.BankAccount.COLUMN_NAME_IBAN + " COLLATE LOCALIZED ASC");
     }
 
     @Override
     public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
         // Swap the new cursor in. (The framework will take care of closing
         // the
         // old cursor once we return.)
         getCursorAdapter(loader).swapCursor(data);
 
         // The list should now be shown.
         if (isResumed()) {
             setListShown(true);
         } else {
             setListShownNoAnimation(true);
         }
     }
 
     private CursorAdapter getCursorAdapter(Loader<Cursor> loader) {
         return (CursorAdapter) mAdapter.getPiece(loader.getId() * 2 + 1);
     }
 
     @Override
     public void onLoaderReset(Loader<Cursor> loader) {
         // This is called when the last Cursor provided to onLoadFinished()
         // above is about to be closed. We need to make sure we are no
         // longer using it.
         getCursorAdapter(loader).swapCursor(null);
     }
 
     private static final class BankAccountsAdapter extends ResourceCursorAdapter {
         private BankAccountsAdapter(Context context, int layout, Cursor c, int flags) {
             super(context, layout, c, flags);
         }
 
         @Override
         public void bindView(View view, Context context, Cursor cursor) {
             TextView name = (TextView) view.findViewById(R.id.name);
             if (name != null) {
                 name.setText(formatIBAN(cursor.getString(cursor
                         .getColumnIndex(BankingContract.BankAccount.COLUMN_NAME_IBAN))));
             }
 
             TextView description = (TextView) view.findViewById(R.id.description);
             if (description != null) {
                 description
                         .setText(cursor.getString(cursor
                                 .getColumnIndex(BankingContract.BankAccount.COLUMN_NAME_LAST_TRANSACTION_DATE)));
             }
 
             TextView sum = (TextView) view.findViewById(R.id.sum);
             if (sum != null) {
                 sum.setText(cursor.getString(cursor
                         .getColumnIndex(BankingContract.BankAccount.COLUMN_NAME_AVAILABLE_BALANCE))
                         + " "
                         + cursor.getString(cursor
                                 .getColumnIndex(BankingContract.BankAccount.COLUMN_NAME_CURRENCY)));
             }
         }
     }
 
     private static final class MyMergeAdapter extends MergeAdapter {
 
         ListAdapter getPiece(int index) {
             return pieces.get(index);
         }
 
     }
 
     @Override
     public void onAccountsUpdated(Account[] accounts) {
         populateList();
     }
 }
