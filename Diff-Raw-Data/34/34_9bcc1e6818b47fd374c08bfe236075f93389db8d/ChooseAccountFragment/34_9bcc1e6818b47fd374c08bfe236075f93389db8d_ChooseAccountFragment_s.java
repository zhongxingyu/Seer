 package ginger.connexus.fragment;
 
 import ginger.connexus.R;
 import ginger.connexus.util.AccountUtils;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.accounts.Account;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 public class ChooseAccountFragment extends DialogFragment {
 
    public interface AccountChooserListener {
         public void onAccountChosen(final Account accountEmail);
     }
 
    private AccountChooserListener mListener;
     private List<Account> mAccounts;
 
     public ChooseAccountFragment() {
     }
 
    public static ChooseAccountFragment newInstance(AccountChooserListener listener) {
         ChooseAccountFragment accountChooser = new ChooseAccountFragment();
         accountChooser.mListener = listener;
         return accountChooser;
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         mAccounts = AccountUtils.getAccounts(getActivity());
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setTitle(R.string.choose_account_text);
         ArrayList<String> emails = new ArrayList<String>();
         for (Account account : mAccounts) {
             emails.add(account.name);
         }
         builder.setItems(emails.toArray(new String[emails.size()]), new DialogInterface.OnClickListener() {
 
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 mListener.onAccountChosen(mAccounts.get(which));
             }
         });
         return builder.create();
     }
 
 }
