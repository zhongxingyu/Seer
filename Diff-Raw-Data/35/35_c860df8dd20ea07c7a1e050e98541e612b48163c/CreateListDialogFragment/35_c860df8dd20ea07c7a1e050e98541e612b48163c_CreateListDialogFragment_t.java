 package com.sysiq.android.callmanager.ui.fragments;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
import android.os.Build;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.RadioGroup;
 import android.widget.TextView;
 import com.sysiq.android.callmanager.R;
 import com.sysiq.android.callmanager.model.entity.CMList;
 
 public class CreateListDialogFragment extends DialogFragment {
     private CMListsFragment.CMListsCallback callback;
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         try {
             callback = (CMListsFragment.CMListsCallback) activity;
         } catch (ClassCastException e) {
             throw new ClassCastException(activity.toString() + " must implement CMListsCallback interface.");
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
         LayoutInflater inflater = getActivity().getLayoutInflater();
 
        View dialogView = inflater.inflate(R.layout.create_list_dialog, null);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
            View silence = dialogView.findViewById(R.id.silence);
            silence.setVisibility(View.GONE);
        }
         final TextView listNameView = (TextView) dialogView.findViewById(R.id.list_name);
         final RadioGroup groupView = (RadioGroup) dialogView.findViewById(R.id.type_radio_group);
         builder.setTitle(R.string.create_list_title).
                 setView(dialogView).
                 setPositiveButton(R.string.create_ok, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
                         CMList.ListType type;
                         switch (groupView.getCheckedRadioButtonId()) {
                             case R.id.white:
                                 type = CMList.ListType.WHITE;
                                 break;
                             case R.id.black:
                                 type = CMList.ListType.BLACK;
                                 break;
                             case R.id.silence:
                                 type = CMList.ListType.SILENCE;
                                 break;
                             default:
                                 type = CMList.ListType.WHITE;
                         }
                         callback.onListCreate(listNameView.getText().toString(), type);
                     }
                 }).setNegativeButton(R.string.create_cancel, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 // nothing
             }
         });
         return builder.create();
     }
 }
