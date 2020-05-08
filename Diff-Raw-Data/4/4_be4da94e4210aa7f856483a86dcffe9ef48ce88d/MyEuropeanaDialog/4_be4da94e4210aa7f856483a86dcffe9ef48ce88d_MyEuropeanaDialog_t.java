 package net.eledge.android.eu.europeana.gui.dialog;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.widget.TextView;
 
 import net.eledge.android.eu.europeana.EuropeanaApplication;
 import net.eledge.android.eu.europeana.R;
 import net.eledge.android.eu.europeana.myeuropeana.task.GetProfileTask;
 import net.eledge.android.toolkit.async.listener.TaskListener;
 
 import org.springframework.social.europeana.api.Europeana;
 import org.springframework.social.europeana.api.model.Profile;
 
 public class MyEuropeanaDialog extends DialogFragment implements TaskListener<Profile> {
     private static final String TAG = MyEuropeanaDialog.class.getSimpleName();
 
     private EuropeanaApplication mApplication;
     private Europeana mEuropeanaApi;
 
    public MyEuropeanaDialog() {
        super();
    }

     public MyEuropeanaDialog(EuropeanaApplication application) {
         super();
         mApplication = application;
         mEuropeanaApi = application.getMyEuropeanaApi();
     }
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         LayoutInflater inflater = getActivity().getLayoutInflater();
 
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         builder.setView(inflater.inflate(R.layout.dialog_myeuropeana, null));
         builder.setPositiveButton(R.string.dialog_myeuropeana_button_close, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                 MyEuropeanaDialog.this.dismiss();
             }
         });
         builder.setNegativeButton(R.string.dialog_myeuropeana_button_logoff, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int id) {
                 mApplication.getConnectionRepository().removeConnections(mApplication.getEuropeanaConnectionFactory().getProviderId());
                 MyEuropeanaDialog.this.dismiss();
             }
         });
         return builder.create();
     }
 
     @Override
     public void onStart() {
         super.onStart();
         new GetProfileTask(getActivity(), mEuropeanaApi, this).execute();
     }
 
     @Override
     public void onTaskStart() {
 
     }
 
     @Override
     public void onTaskFinished(Profile profile) {
         if (profile != null) {
             TextView email = (TextView) getDialog().findViewById(R.id.dialog_myeuropeana_textview_email);
             email.setText(profile.getEmail());
             TextView username = (TextView) getDialog().findViewById(R.id.dialog_myeuropeana_textview_username);
             username.setText(profile.getUserName());
             TextView saveditem = (TextView) getDialog().findViewById(R.id.dialog_myeuropeana_textview_saveditem);
             saveditem.setText(String.valueOf(profile.getNrOfSavedItems()));
             TextView savedsearch = (TextView) getDialog().findViewById(R.id.dialog_myeuropeana_textview_savedsearch);
             savedsearch.setText(String.valueOf(profile.getNrOfSavedSearches()));
             TextView tags = (TextView) getDialog().findViewById(R.id.dialog_myeuropeana_textview_tags);
             tags.setText(String.valueOf(profile.getNrOfSocialTags()));
         }
     }
 
 }
