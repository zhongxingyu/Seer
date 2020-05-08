 package nl.q42.hue2.dialogs;
 
 import nl.q42.hue2.R;
 import nl.q42.hue2.activities.GroupActivity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.os.Bundle;
 
 public class GroupRemoveDialog extends DialogFragment {
	public static GroupRemoveDialog newInstance(String id) {
		GroupRemoveDialog dialog = new GroupRemoveDialog();
		
		Bundle args = new Bundle();
		args.putString("id", id);
		dialog.setArguments(args);
		
		return dialog;
 	}
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final String id = getArguments().getString("id");
		
 		return new AlertDialog.Builder(getActivity())
 			.setMessage(R.string.dialog_remove_group)
 			.setPositiveButton(R.string.dialog_yes, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					((GroupActivity) getActivity()).removeGroup();
 				}
 			})
 			.setNegativeButton(R.string.dialog_no, new OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					dialog.cancel();
 				}
 			})
 			.create();
 	}
 }
