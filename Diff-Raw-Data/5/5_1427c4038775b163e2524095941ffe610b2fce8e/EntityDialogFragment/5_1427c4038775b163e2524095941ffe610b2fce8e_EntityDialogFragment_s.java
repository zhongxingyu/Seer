
 package com.kth.baasio.baassample.ui.dialog;
 
import com.actionbarsherlock.R;
 import com.actionbarsherlock.app.SherlockDialogFragment;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.TextUtils;
 import android.view.ViewGroup;
 import android.widget.CheckBox;
 import android.widget.EditText;
 
 public class EntityDialogFragment extends SherlockDialogFragment {
     public static final int CREATE_ENTITY = 0;
 
     public static final int UPDATE_ENTITY = 1;
 
     public static final int SEND_PUSH = 2;
 
     public static final int SEND_PUSH_BY_TARGET = 3;
 
     public static final int CREATE_FOLDER = 4;
 
     private int mMode = -1;
 
     private String mBody;
 
     private String mTitle;
 
     private ViewGroup mRoot;
 
     private EditText mTextBody;
 
     private EditText mTextBody2;
 
     private CheckBox mCheckBox1;
 
     private CheckBox mCheckBox2;
 
     public static EntityDialogFragment newInstance() {
         EntityDialogFragment frag = new EntityDialogFragment();
         return frag;
     }
 
     private void setTitle(String title) {
         this.mTitle = title;
     }
 
     public void setBody(String body) {
         this.mBody = body;
     }
 
     public void setShareMode(int mode) {
         this.mMode = mode;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         setStyle(SherlockDialogFragment.STYLE_NORMAL, R.style.Theme_Sherlock_Light_Dialog);
 
         switch (mMode) {
             case CREATE_ENTITY: {
                 setTitle(getString(R.string.create_entity_dialog_title));
                 mRoot = (ViewGroup)getActivity().getLayoutInflater().inflate(
                         R.layout.dialog_entity, null);
                 mTextBody = (EditText)mRoot.findViewById(R.id.textInput);
                 mTextBody.setHint(R.string.entity_dialog_body_hint);
                 break;
             }
             case UPDATE_ENTITY: {
                 setTitle(getString(R.string.update_entity_dialog_title));
                 mRoot = (ViewGroup)getActivity().getLayoutInflater().inflate(
                         R.layout.dialog_entity, null);
                 mTextBody = (EditText)mRoot.findViewById(R.id.textInput);
                 mTextBody.setHint(R.string.entity_dialog_body_hint);
                 break;
             }
             case SEND_PUSH: {
                 setTitle(getString(R.string.sendpush_dialog_title));
                 mRoot = (ViewGroup)getActivity().getLayoutInflater().inflate(
                         R.layout.dialog_sendpush, null);
                 mTextBody = (EditText)mRoot.findViewById(R.id.textInput);
                 mTextBody.setHint(R.string.sendpush_dialog_body_hint);
 
                 mTextBody2 = (EditText)mRoot.findViewById(R.id.textInput2);
                 mTextBody2.setHint(R.string.sendpush_dialog_body_hint2);
 
                 mCheckBox1 = (CheckBox)mRoot.findViewById(R.id.checkIOS);
                 mCheckBox2 = (CheckBox)mRoot.findViewById(R.id.checkAndroid);
                 break;
             }
             case SEND_PUSH_BY_TARGET: {
                 setTitle(getString(R.string.sendpush_dialog_title));
                 mRoot = (ViewGroup)getActivity().getLayoutInflater().inflate(
                         R.layout.dialog_sendpush_by_target, null);
                 mTextBody = (EditText)mRoot.findViewById(R.id.textInput);
                 mTextBody.setHint(R.string.sendpush_dialog_body_hint);
                 break;
             }
             case CREATE_FOLDER: {
                 setTitle(getString(R.string.create_folder_dialog_title));
                 mRoot = (ViewGroup)getActivity().getLayoutInflater().inflate(
                         R.layout.dialog_entity, null);
                 mTextBody = (EditText)mRoot.findViewById(R.id.textInput);
                 mTextBody.setHint(R.string.folder_dialog_body_hint);
                 break;
             }
             default:
                 break;
         }
 
         if (mBody != null && mBody.length() > 0) {
             mTextBody.setText(mBody);
 
             return new AlertDialog.Builder(getActivity())
                     .setTitle(mTitle)
                     .setView(mRoot)
                     .setPositiveButton(R.string.common_dialog_confirm,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     String body = mTextBody.getText().toString().trim();
 
                                     if (mListener != null) {
                                         Bundle data = new Bundle();
                                         data.putString("body", body);
                                         if (mMode == SEND_PUSH) {
                                             String platform = "I,G";
                                             if (!mCheckBox1.isChecked() || !mCheckBox2.isChecked()) {
                                                 if (mCheckBox1.isChecked()) {
                                                     platform = "I";
                                                 } else if (mCheckBox2.isChecked()) {
                                                     platform = "G";
                                                 }
                                             } else {
                                                 platform = "I,G";
                                             }
                                             data.putString("platform", platform);
                                         }
 
                                         if (mTextBody2 != null) {
                                             String tag = mTextBody2.getText().toString().trim();
                                             if (!TextUtils.isEmpty(tag)) {
                                                 data.putString("tag", tag);
                                             }
                                         }
                                         mListener.onPositiveButtonSelected(mMode, data);
                                     }
                                     dialog.dismiss();
                                 }
                             })
                     .setNegativeButton(R.string.common_dialog_cancel,
                             new DialogInterface.OnClickListener() {
 
                                 @Override
                                 public void onClick(DialogInterface dialog, int which) {
                                     dialog.dismiss();
                                 }
                             }).create();
         }
 
         return new AlertDialog.Builder(getActivity())
                 .setTitle(mTitle)
                 .setView(mRoot)
                 .setPositiveButton(R.string.common_dialog_confirm,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int whichButton) {
                                 String body = mTextBody.getText().toString().trim();
 
                                 if (mListener != null) {
                                     Bundle data = new Bundle();
                                     data.putString("body", body);
                                     if (mMode == SEND_PUSH) {
                                         String platform = "I,G";
                                         if (!mCheckBox1.isChecked() || !mCheckBox2.isChecked()) {
                                             if (mCheckBox1.isChecked()) {
                                                 platform = "I";
                                             } else if (mCheckBox2.isChecked()) {
                                                 platform = "G";
                                             }
                                         } else {
                                             platform = "I,G";
                                         }
                                         data.putString("platform", platform);
                                     }
 
                                     if (mTextBody2 != null) {
                                         String tag = mTextBody2.getText().toString().trim();
                                         if (!TextUtils.isEmpty(tag)) {
                                             data.putString("tag", tag);
                                         }
                                     }
                                     mListener.onPositiveButtonSelected(mMode, data);
                                 }
                                 dialog.dismiss();
                             }
                         })
                 .setNegativeButton(R.string.common_dialog_cancel,
                         new DialogInterface.OnClickListener() {
 
                             @Override
                             public void onClick(DialogInterface dialog, int which) {
                                 dialog.dismiss();
                             }
                         }).create();
     }
 
     private EntityDialogResultListener mListener;
 
     public interface EntityDialogResultListener {
         public boolean onPositiveButtonSelected(int mode, Bundle data);
     }
 
     public void setEntityDialogResultListener(EntityDialogResultListener listener) {
         mListener = listener;
     }
 }
