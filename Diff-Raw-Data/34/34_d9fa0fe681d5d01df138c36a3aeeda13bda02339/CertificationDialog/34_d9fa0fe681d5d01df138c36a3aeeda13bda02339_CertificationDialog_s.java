 package hal.taskscheduler.dialogs;
 
 import hal.taskscheduler.R;
 
 import java.util.List;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class CertificationDialog extends DialogFragment {
 	
 	List<String> certifications;
 	LinearLayout view;
 	View availView;
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		Bundle bundle = this.getArguments();
 		certifications=bundle.getStringArrayList("certification");
 		
 		Builder builder = new AlertDialog.Builder(getActivity());
 		LayoutInflater inflater = ((Activity) getActivity()).getLayoutInflater();
 		
 		availView = inflater.inflate(R.layout.certification_dialog_layout, null);
 		//view = (LinearLayout)availView.findViewById(R.id.cert_layout);
 		TextView v = (TextView) availView.findViewById(R.id.certification_text);
 		String result = "";
 		for (String c: certifications){
 			result += c;
 			result += '\n';
 		}
 		v.setText(result);
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
