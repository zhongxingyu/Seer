 package com.storassa.android.scuolasci;
 
 import java.util.Calendar;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.text.method.HideReturnsTransformationMethod;
 import android.text.method.PasswordTransformationMethod;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.webkit.WebView.FindListener;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 
 public class MeteoNoteDialog extends DialogFragment {
    
    CheckBox showAnymoreBox;
    boolean notShowAnymore;
    String customerName;
 
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
       
       final SharedPreferences settings = getActivity().getSharedPreferences("scuolasci",0);
 
       // Get the layout inflater
       LayoutInflater inflater = getActivity().getLayoutInflater();
       final View view = inflater.inflate(R.layout.note_meteo, null);
       
       showAnymoreBox = (CheckBox)view.findViewById(R.id.note_meteo_checkbox);
       showAnymoreBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
          public void onCheckedChanged(CompoundButton buttonView,
                boolean isChecked) {
             // checkbox status is changed from uncheck to checked.
             if (isChecked) {
                // show password
                notShowAnymore = true;
             }
          }
       });
 
       // Inflate and set the layout for the dialog
       // Pass null as the parent view because its going in the dialog layout
      builder.setView(view).setTitle(R.string.attention)
       // Add action buttons
             .setPositiveButton(R.string.ok,
                   new DialogInterface.OnClickListener() {
                      @Override
                      public void onClick(DialogInterface dialog, int id) {
 
                         if (notShowAnymore) {
                            SharedPreferences.Editor editor = settings.edit();
                            editor.putBoolean("meteonotebox", true).commit();
                         }
                      }
                   });
 
       return builder.create();
    }
 }
