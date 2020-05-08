 package il.ac.tau.team3.uiutils;
 
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.Pray;
 import il.ac.tau.team3.common.SPUtils;
 import il.ac.tau.team3.shareaprayer.FindPrayer;
 import il.ac.tau.team3.shareaprayer.IStatusWriter;
 import il.ac.tau.team3.shareaprayer.R;
 import android.accounts.Account;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.util.Log;
 import android.view.View;
 import android.view.Window;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class MenuSettingsUtils {
 	
 	static final String _sChooseMinMaxText =  "Choose what will be shown on places:";
 	// this boolean indicates rather the user want to show max prayers (true) or min prayers (flase)
 	private static boolean showMax = true ;
 
 	public static boolean getShowMax() {
 		return showMax;
 	}
 
 	public static void setShowMax(boolean toShowMax) {
 		showMax = toShowMax;
 	}
 	
 	
 	public static int chooseMaxOrMin(GeneralPlace place){
 		if(showMax){
 			return determinMax(place);
 		}else{
 			return determinMin(place);
 		}
 	}
 	
 	static int determinMax(GeneralPlace place){
 		int max = 0;
 		
 		try	{
 			for (Pray p : place.getPraysOfTheDay())	{
 				if (max < p.numberOfJoiners())	{
 					max = p.numberOfJoiners();
 				}
 			}
 		} catch (NullPointerException e)	{
 			// no prays for place
 			
 		}
 		
 		
 		return max;
 	}
 	
 	static int determinMin(GeneralPlace place){
 		int min = (int) SPUtils.INFINITY;
 		
 		try	{
 			for (Pray p : place.getPraysOfTheDay())	{
 				if (min > p.numberOfJoiners())	{
 					min = p.numberOfJoiners();
 				}
 			}
 		} catch (NullPointerException e)	{
 			// no prays for place
 		}
 		
 		
 		return min;
 	}
 	public static void CreateChooseMinMaxDialog(Activity activity){
 		final CharSequence[] items = {"Show MAX prayers of all daily prays", "Show MIN prayers of all daily prays"};
 		final IStatusWriter statusBar = ((FindPrayer) activity).getStatusBar();
 
 		try {
 			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
 			builder.setTitle(_sChooseMinMaxText);
 			builder.setSingleChoiceItems(items, (getShowMax()? 0:1), new DialogInterface.OnClickListener() {
 			    public void onClick(DialogInterface dialog, int item) {
			    	String minOrMax[] = {"Min","Max"};
 			    	int oldVal = (getShowMax()? 0:1);
 			    	int newVal = oldVal;
 			        if(item == 0){
 			        	setShowMax(true);
			        	newVal = 1; 
 			        }else{
 			        	setShowMax(false);
			        	newVal = 0; 
 			        }
 			        dialog.dismiss();
 			        if(null!= statusBar && newVal != oldVal){
 			        	statusBar.write("Show " + minOrMax[newVal] + " is set.", R.drawable.status_bar_accept_icon, 2000);
 			        	
 			         }
 			    }
 			});
 			AlertDialog alert = builder.create();
 			alert.show();
 		} catch (NullPointerException e) {
 			e.printStackTrace();
 			if(null!= statusBar){
 				statusBar.write("An Error accoured.", R.drawable.status_bar_error_icon, 2000);
 			}
 		}
 	}
 	
 	public static String[] createEditDetailsDialog(final GeneralUser user, final FindPrayer activity){
 		final Dialog dialog = new Dialog(activity);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		final String account = user.getName();
 		 dialog.setContentView(R.layout.dialog_edit_profile);
 		 final EditText editTextFirstName = (EditText)dialog.findViewById(R.id.dep_name_first);
 		 final String firstnameOld = user.getFirstName();
 		 editTextFirstName.setText(firstnameOld);
 		 
          final EditText editTextLastName = (EditText)dialog.findViewById(R.id.dep_name_last);
          final String lastnameOld = user.getLastName();
          editTextLastName.setText(lastnameOld);
          final TextView accountView = (TextView)dialog.findViewById(R.id.dep_accounts_email);
          accountView.setText(account);
          Button okButton = (Button) dialog.findViewById(R.id.dep_button_ok);
                
          final String names[] = new String[3];
         
          
          okButton.setOnClickListener(new OnClickListener()
          {                
              public void onClick(View v)
              {
             	 if(editTextFirstName.getText() == null || editTextFirstName.getText().toString() == null ||
             			 editTextFirstName.getText().toString().equals("") || 
             			 editTextLastName.getText() == null || editTextLastName.getText().toString() == null ||
             			 editTextLastName.getText().toString().equals("") ){
             		 UIUtils.createAlertDialog("Please enter your first and last name", activity, "OK");
             	 }else if (!(firstnameOld.equals(editTextFirstName.getText().toString()) && lastnameOld.equals(editTextLastName.getText().toString()))){
             	 names[0] = editTextFirstName.getText().toString();
             	 names[1] = editTextLastName.getText().toString();
             	 names[2] = account;
             	 final IStatusWriter statusBar = activity.getStatusBar();
             	 try{
             		 activity.setUser(names);
             		 statusBar.write("User details updated", R.drawable.status_bar_accept_icon, 2000);
             	 }catch (Exception e){
             		 e.printStackTrace();
             		 statusBar.write("An Error accoured. User details weren't updated", R.drawable.status_bar_error_icon, 2000);
             	 }
             	            	
             	 }
             	 dialog.dismiss();
              }
          });
          
          
 			
          
      
      
          dialog.show();
 
      return names;
 		
 	}
 }
