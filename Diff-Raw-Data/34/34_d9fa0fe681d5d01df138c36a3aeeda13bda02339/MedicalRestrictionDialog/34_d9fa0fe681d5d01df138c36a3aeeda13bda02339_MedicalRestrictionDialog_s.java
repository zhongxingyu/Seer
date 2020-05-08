 package hal.taskscheduler.dialogs;
 
 import hal.taskscheduler.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.TextView;
 
 public class MedicalRestrictionDialog extends DialogFragment {
 	
 	int workerId;
 	TextView view;
 	View availView;
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		Bundle bundle = this.getArguments();
 		String restriction = bundle.getString("restriction");
 		
 		
 		Builder builder = new AlertDialog.Builder(getActivity());
 		LayoutInflater inflater = ((Activity) getActivity()).getLayoutInflater();
 		
 		availView = inflater.inflate(R.layout.medical_restriction_dialog_layout, null);
 		view = (TextView)availView.findViewById(R.id.medical_restriction);
 		view.setText(restriction);
 		
 		builder.setView(availView);
 				
 		builder.setNeutralButton("Done",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                     	dialog.dismiss();
                     }
                 });
 		
 		
          AlertDialog dialog = builder.create();
          return dialog;
 	}
 
     
 }
