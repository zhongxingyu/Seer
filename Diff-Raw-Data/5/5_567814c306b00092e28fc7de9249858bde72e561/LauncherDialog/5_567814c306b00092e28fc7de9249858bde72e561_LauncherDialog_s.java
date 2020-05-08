 package ca.idrc.tagin.app.util;
 
 import ca.idrc.tagin.app.TaginActivity;
 import ca.idrc.tagin.app.TagsActivity;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 
 /**
  * Custom AlertDialog class which handles subscriptions management
  * @author CampusUB1 Development Team
  *
  */
 public class LauncherDialog extends AlertDialog {
 	
 	private AlertDialog mLauncherDialog;
 	private Activity mContext;
 	
 	public LauncherDialog(Activity context) {
 		super(context);
 		mContext = context;
 	}
 
 	public void showDialog() {
 		LauncherDialogBuilder builder = new LauncherDialogBuilder(mContext);
 		mLauncherDialog = builder.create();
 		mLauncherDialog.show();
 	}
 	
 	
 	private class LauncherDialogBuilder extends AlertDialog.Builder {
 		
 		public LauncherDialogBuilder(Activity context) {
 			super(context);
 			CharSequence[] items = {"tagin-api", "tagin-tags"};
 
 			setCancelable(true);
 			setTitle("Select your desired service");
 			setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					switch (which) {
 					case 0:
 						mContext.startActivity(new Intent(mContext, TaginActivity.class));
 						break;
 					case 1:
 						mContext.startActivity(new Intent(mContext, TagsActivity.class));
 						break;
 					default:
 						break;
 					}
 				}
 			});
 			
 			setOnCancelListener(new OnCancelListener() {
 				
 				@Override
 				public void onCancel(DialogInterface dialog) {
 					mContext.finish();
 					dismiss();
 				}
 			});
 		}
 	}
 
 }
