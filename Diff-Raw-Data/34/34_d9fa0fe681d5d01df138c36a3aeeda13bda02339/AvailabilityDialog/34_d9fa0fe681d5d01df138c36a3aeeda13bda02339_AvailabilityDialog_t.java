 package hal.taskscheduler.dialogs;
 
 import hal.taskscheduler.R;
 import hal.taskscheduler.model.WorkerAvailability;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.Spinner;
 
/**
 * Dialog to view and change worker availabilities
 * 
 * @author RadhikaMalik
 *
 */
 public class AvailabilityDialog extends DialogFragment {
 	
 	int workerId;
 	RadioGroup rg;
 	View availView;
 	
 	public interface ChangeAvailabilityListener {
         public void onSaveChangeAvailability(DialogFragment dialog);
     }
     
 	ChangeAvailabilityListener mListener;	
 	
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		
 		Bundle bundle = this.getArguments();
 		WorkerAvailability avail = (WorkerAvailability) bundle.getSerializable("availability");
 			
 		Builder builder = new AlertDialog.Builder(getActivity());
 		LayoutInflater inflater = ((Activity) getActivity()).getLayoutInflater();
 		
 		availView = inflater.inflate(R.layout.worker_availability, null);
 		rg = (RadioGroup)availView.findViewById(R.id.availabilityGroup);
 		
 		rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
 
 			@Override
 			public void onCheckedChanged(RadioGroup rg, int id) {
 				View radioButton = rg.findViewById(id);
 				int idx = rg.indexOfChild(radioButton);
 				
 				if (idx == 2){ //part time availability, so show the views to enter time!
 					
 					LinearLayout partAvailLayout = (LinearLayout) availView.findViewById(R.id.partAvailLayout);
 					partAvailLayout.setVisibility(View.VISIBLE);
 				} else{
 
 					LinearLayout partAvailLayout = (LinearLayout) availView.findViewById(R.id.partAvailLayout);
 					partAvailLayout.setVisibility(View.GONE);
 				}
 				
 			}});
 		
 		RadioButton rb1;
 		if (avail==WorkerAvailability.FULL_SHIFT_AVAILABLE){
  			rb1 = (RadioButton) availView.findViewById(R.id.availEntire);
  		}else if (avail == WorkerAvailability.FULL_SHIFT_UNAVAILABLE){
  			rb1 = (RadioButton) availView.findViewById(R.id.unavailEntire);
  		}else {//if (avail == WorkerAvailability.PART_SHIFT)
  			rb1 = (RadioButton) availView.findViewById(R.id.availPart);
  			String fromHr = bundle.getString("fromHr");
 			String toHr = bundle.getString("toHr");
 			String fromAmPm = bundle.getString("fromAmPm");
 			String fromMin = bundle.getString("fromMin");
 			String toMin = bundle.getString("toMin");
 			String toAmPm = bundle.getString("toAmPm");
 			
 			EditText fromHrText = (EditText) availView.findViewById(R.id.fromHour);
 			EditText toHrText = (EditText) availView.findViewById(R.id.toHour);
 			EditText fromMinText = (EditText) availView.findViewById(R.id.fromMin);
 			EditText toMinText = (EditText) availView.findViewById(R.id.toMin);
 			Spinner fromAmPmText = (Spinner) availView.findViewById(R.id.fromAmPm);
 			Spinner toAmPmText = (Spinner) availView.findViewById(R.id.toAmPm);
 			
 			fromHrText.setText(fromHr);
 			toHrText.setText(toHr);
 			if (fromAmPm.equals("am"))
 				fromAmPmText.setSelection(0);
 			else
 				fromAmPmText.setSelection(1);
 			
 			if (toAmPm.equals("am"))
 				toAmPmText.setSelection(0);
 			else
 				toAmPmText.setSelection(1);
 			
 			fromMinText.setText(fromMin);
 			toMinText.setText(toMin);
 			
 			fromHrText.clearFocus();
  		}
 		
 		rb1.setChecked(true);
 		builder.setView(availView);
 				
 		builder.setNegativeButton("Close",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                     	
                     	dialog.dismiss();
 
                     }
                 });
 		
 		builder.setPositiveButton("Save",
                 new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                     	
                     	int rbuttonid=  rg.getCheckedRadioButtonId();
                     	View radioButton = rg.findViewById(rbuttonid);
                     	
                     	int idx = rg.indexOfChild(radioButton);
                     	
                     	
                     	Bundle bundle = AvailabilityDialog.this.getArguments();
                     	
                     	WorkerAvailability availability;
                     	
                     	if (idx ==0){
                     		availability=WorkerAvailability.FULL_SHIFT_AVAILABLE;
                     		
                     	} else if (idx == 1){
                     		availability=WorkerAvailability.FULL_SHIFT_UNAVAILABLE;
                     	}
                     	else { //part availability!
                     		availability=WorkerAvailability.PART_SHIFT;
                     		String fromHr = ((EditText)availView.findViewById(R.id.fromHour)).getText().toString();
                     		String fromMin = ((EditText)availView.findViewById(R.id.fromMin)).getText().toString();
                     		String fromAmPm=((Spinner)availView.findViewById(R.id.fromAmPm)).getSelectedItem().toString();
                     		
                     		String toHr = ((EditText)availView.findViewById(R.id.toHour)).getText().toString();
                     		String toMin = ((EditText)availView.findViewById(R.id.toMin)).getText().toString();
                     		String toAmPm = ((Spinner)availView.findViewById(R.id.toAmPm)).getSelectedItem().toString();
                     		
                     		bundle.putString("fromHr", fromHr);
                     		bundle.putString("fromMin", fromMin);
                     		bundle.putString("fromAmPm", fromAmPm);
                     		bundle.putString("toHr", toHr);
                     		bundle.putString("toMin", toMin);
                     		bundle.putString("toAmPm", toAmPm);
                     	}
                     	bundle.putSerializable("availability", availability);
                     	
                    	// send back to main activity!
                     	mListener.onSaveChangeAvailability(AvailabilityDialog.this);
 
                     }
                 });
 		
          AlertDialog dialog = builder.create();
          return dialog;
 	}
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         // Verify that the host activity implements the callback interface
         try {
             // Instantiate the NoticeDialogListener so we can send events to the host
             mListener = (ChangeAvailabilityListener) activity;
         } catch (ClassCastException e) {
             // The activity doesn't implement the interface, throw exception
             throw new ClassCastException(activity.toString()
                     + " must implement NoticeDialogListener");
         }
     }
 }
