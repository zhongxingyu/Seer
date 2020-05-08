 package scrum.support;
 
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 
 import scrum.support.model.Project;
 import scrum.support.model.User;
 import scrum.support.services.ContentProvider;
 import scrum.support.services.ErrorService;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 
 public class ProjectActivity extends ListActivity implements Observer {
     /** Called when the activity is first created. */
 	
 	private final static int NEW_TOKEN = 1;
 	
 	private List<Project> projects;
 	private Button tokenAddButton;
 	private Activity activity;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState); 
         activity = this;
     	ErrorService.getInstance().addObserver(this);
         projects = ContentProvider.getInstance().getProjects();
         
         setListAdapter(new ArrayAdapter<Project>(this, android.R.layout.simple_list_item_1, projects));
    
     }
     
     /**
      * When the menu button is used the server related buttons will
      * toggle visible and invisible.
      */
     @Override
     public boolean onPrepareOptionsMenu(Menu menu) {
     	Intent confirmIntent = new Intent(activity, NewAccountActivity.class);
     	activity.startActivityForResult(confirmIntent, NEW_TOKEN);	
     	return true;
     }
     
 
 	/**
 	 * Called when an activity returns.
 	 * 
 	 * NEW_TOKEN is called when the new token activity is returned.
 	 * Once returned the app tries to get the new projects.
 	 */
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         switch (requestCode) {
             case NEW_TOKEN:
                 try {
                     String account = data.getStringExtra("account_added");
                     if (account != null &&account.length() > 0) {
                     	
                     	
                     }
                 } catch (Exception e) {
                 }
                 break;
             default:
                 break;
         }
     }
 
 	public void update(Observable arg0, Object arg1) {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle("An Error has ocurred");
		alertDialog.setMessage(arg1.toString());
		alertDialog.show();			
 	}
 	
 	/**
 	 * A worker thread to manage the authenticate and network activity
 	 *
 	 */
	private class AuthenticateUser extends AsyncTask<User, Integer, Boolean> {
 				
 		@Override
 		protected void onPreExecute() {
 			// do nothing
 		}
 
 		@Override
 		protected Boolean doInBackground(User...params) {
 			return ContentProvider.getInstance().getAccountProjects();
 		}
 
 		@Override
 	     protected void onPostExecute(Boolean result) {
 	    	 if(result != null) {
 	    		 if(result) {
 	    			 // update list with new projects
 		    	 }
 	    	 } else {
 	    		 
 	    		 // Result will only be null if an error has occurred. 
 	    		 // Pull the last error out of the ErrorService.
 	    		 
 	    			final AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
 	    			alertDialog.setTitle(activity.getString(R.string.title_error));
 	    			alertDialog.setMessage(ErrorService.getInstance().getError());
 					alertDialog.setButton(activity.getString(android.R.string.ok), 
 							new DialogInterface.OnClickListener() {
 						
 						public void onClick(DialogInterface dialog, int which) {
 							alertDialog.dismiss();
 						}
 					});
 	    			alertDialog.show();	
 	    	 }
 		}
 	 }
 }
