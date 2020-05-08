 package com.abid_mujtaba.fetchheaders.fragments;
 
 /**
  * This fragment implements showing the email information for a single account.
  */
 
 import android.content.Context;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 import com.abid_mujtaba.fetchheaders.R;
 import com.abid_mujtaba.fetchheaders.misc.ThreadPool;
 import com.abid_mujtaba.fetchheaders.models.Account;
 import com.abid_mujtaba.fetchheaders.models.Email;
 import com.abid_mujtaba.fetchheaders.views.EmailView;
 
 import com.sun.mail.util.MailConnectException;
 
 import javax.mail.AuthenticationFailedException;
 import javax.mail.MessagingException;
 import javax.mail.NoSuchProviderException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 
 public class AccountFragment extends Fragment
 {
     private Account mAccount;
     private HashMap<Integer, Email> mEmails;                 // We use HashMaps so we can delete emails with impunity yet still have access to remaining emails with the same integer index
     private HashMap<Integer, EmailView> mEmailViews;         // Keeps track of the views associated with emails
 
     private LinearLayout mEmailList;
     private View uProgress;                                  // View for indicating a progress while emails are being fetched
     private View uRootView;
     private TextView uAccountName;
 
     private Handler mHandler;
 
     private String mErrorMessage;           // This is populated with the error message generated if an error occurs while fetching emails
 
     private boolean fEmailsFetched = false;     // A flag used to determine whether emails have been already fetched or not
 
     public static AccountFragment newInstance(int account_id)
     {
         AccountFragment af = new AccountFragment();
 
         af.mAccount = Account.get(account_id);          // Associate account with Fragment using account_id
 
         return af;
     }
 
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         uRootView = inflater.inflate(R.layout.account_fragment, container, false);     // The false specifies that this view is NOT to be attached to root since we will attach it explicitly
 
         setRetainInstance(true);        // The Fragment will be retained across configuration changes.
 
         mHandler = new Handler();      // Create a handler to give access to the UI Thread in this fragment
 
         uAccountName = (TextView) uRootView.findViewById(R.id.tvAccountName);
         mEmailList = (LinearLayout) uRootView.findViewById(R.id.emailList);      // The root layout of the fragment. We shall add views to this.
 
         uAccountName.setText(mAccount.name());
 
         if (mErrorMessage != null)          // An error occurred the last time emails were fetched so we simply display the error message and move on
         {
             setErrorView(mErrorMessage);
         }
         else if (fEmailsFetched)            // We check if emails have already been fetched.
         {
             if (mEmails.size() > 0)
             {
                 populateEmailViews();
             }
             else            // Emails were fetched but none were found so we empty out the view
             {
                 emptyRootView();
             }
         }
         else
         {
             LayoutInflater li = (LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
             uProgress = li.inflate(R.layout.progress_view, null);
             mEmailList.addView(uProgress);
 
             ThreadPool.executeTask(fetchEmails);        // Execute this runnable on a background thread, part of a pool
         }
 
         return uRootView;
     }
 
 
     private Runnable fetchEmails = new Runnable() {
 
         class ExceptionRunnable implements Runnable {       // The Runnable inner sub-class to be executed if an exception is raised
 
             public ExceptionRunnable(String error_message)      // The Runnable is declared with an error message
             {
                 mErrorMessage = error_message;          // This sets the mErrorMessage attribute at the Fragment class level.
             }
 
             @Override
             public void run()
             {
                 mEmailList.removeView(uProgress);
 
                 setErrorView(mErrorMessage);
             }
         }
 
         @Override
         public void run()
         {
             try
             {
                 mEmails = mAccount.fetchEmails(true);       // Passing "true" here means only unseen emails will be returned
                 mEmailViews = new HashMap<Integer, EmailView>();
 
                 if (mEmails.size() > 0)
                 {
                     mHandler.post(new Runnable() {           // We define tasks that need to be carried out on the frontend UI thread. Most UI tasks.
 
                         @Override
                         public void run()
                         {
                             mEmailList.removeView(uProgress);
 
                             populateEmailViews();
                         }
                     });
                 }
                 else        // No emails extracted so we remove the account from the list displayed
                 {
                     mHandler.post(new Runnable() {
 
                         @Override
                         public void run() { emptyRootView(); }
                     });
                 }
 
                 fEmailsFetched = true;          // Emails have been fetched so we set this flag.
             }
             catch(NoSuchProviderException e) { mHandler.post(new ExceptionRunnable("No Such Provider found. Verify credentials.")); }
             catch(AuthenticationFailedException e) { mHandler.post(new ExceptionRunnable("Authentication Failure. Verify credentials.")); }
             catch(MailConnectException e) { mHandler.post(new ExceptionRunnable("Unable to connect to Mail Server. Verify credentials.")); }
             catch(MessagingException e) { mHandler.post(new ExceptionRunnable("Messaging Error. Verify credentials.")); }
         }
     };
 
 
     private void populateEmailViews()           // Method for taking mEmails and using it to populate mEmailList with EmailViews corresponding to the fetched emails
     {
         for (Integer key : mEmails.keySet())             // We iterate over the key (unique int id) associated with the Email objects and in turn associate the EmailView Id and its position in its own HashMap with the same key for cross-referencing
         {
             Email email = mEmails.get(key);
 
             EmailView ev = new EmailView(AccountFragment.this.getActivity(), null);
             ev.setInfo(email.date(), email.from(), email.subject());
             ev.setId(key);
             ev.setOnClickListener(listener);
 
             mEmailList.addView(ev);
             mEmailViews.put(key, ev);        // We store the EmailView associated with this Email object. We will use it to delete views when required
 
             handleDeletion(email, ev);          // We handle strikethrough of EmailView if the email is marked for deletion.
         }
     }
 
 
     private void emptyRootView()            // Empties out the RootView, which should only have the AccountName in it at the time this method is called
     {
         ((LinearLayout) uRootView).removeView(uAccountName);       // Remove account title
 
         ViewGroup.LayoutParams params = uRootView.getLayoutParams();     // Collapse height of fragment layout to 0
         params.height = 0;
         uRootView.setLayoutParams(params);
     }
 
 
     private void setErrorView(String error_message)         // Method for creating and displaying an Error Message View.
     {
         // TODO: Create a custom layout for Error TextViews in general to be used in such cases. Possibly add a triangular icon indicating an error.
 
         TextView tv = new TextView(getActivity());
         tv.setText( error_message );
 
         mEmailList.addView(tv);
     }
 
 
     View.OnClickListener listener = new View.OnClickListener() {
 
         @Override
         public void onClick(View view)
         {
             Email email = mEmails.get(view.getId());
             EmailView ev = (EmailView) view;
 
             email.toggleDeletion();
 
             handleDeletion(email, ev);
         }
     };
 
 
     private void handleDeletion(Email email, EmailView ev)      // The method analyzes the Email object and its related EmailView and strikesthrough if so warranted
     {
         if (email.isToBeDeleted())
         {
             ev.strikethrough();
         }
         else
         {
             ev.removeStrikethrough();
         }
     }
 
 
     public void remove_emails_marked_for_deletion(Handler handler)           // Called by parent activity to force the fragment to refresh its contents. This will cause emails set for deletions to be deleted.
     {
         // We iterate over the emails weeding out the emails marked for deletion
         ArrayList<Email> emails = new ArrayList<Email>();
         ArrayList<Integer> keys = new ArrayList<Integer>();     // We store the keys that we have to delete from the HashMap. We can't do it while we are traversing the key set itself
 
         for(Integer key: mEmails.keySet())              // We iterate over the keys in the HashMap. This way we know that we are iterating over existing objects and have access to their unique keys
         {
             Email email = mEmails.get(key);
 
             if (email.isToBeDeleted())
             {
                 emails.add(email);
                 keys.add(key);
 
                 final EmailView email_view = mEmailViews.get(key);
                 mEmailViews.remove(key);         // Since we are deleting the email we remove the corresponding view from the ArrayList
 
                 handler.post(new Runnable() {
 
                     @Override
                     public void run()
                     {
                         mEmailList.removeView( email_view );        // Remove the view associated with this email from the list of views
                     }
                 });
             }
         }
 
         mAccount.delete(emails);
 
         for (Integer key: keys) { mEmails.remove(key); }            // Remove the Email objects corresponding to the deleted emails from the HashMap

        if (mEmails.size() == 0)        // If all of the emails have been deleted we should empty the root view
        {
            handler.post(new Runnable() {
                @Override
                public void run() { emptyRootView(); }
            });

        }
     }
 }
