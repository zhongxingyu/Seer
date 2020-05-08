 package com.kth.baasio.baassample.ui.dialog;
 
 import com.actionbarsherlock.app.SherlockDialogFragment;
import com.kth.baasio.baassample.ui.dialog;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.text.InputFilter;
 import android.widget.EditText;
 
 public class ShareDialogFragment extends SherlockDialogFragment {
     public static final int SHARE_TWITTER = 0;
 
     public static final int SHARE_FACBOOK = 1;
 
     public static final int LIMIT_SHARE_TEXT_LENGTH = 140;
 
     private int mMode = -1;
 
     private String mBody;
 
     private String mTitle;
 
     private EditText mTextBody;
 
     public static ShareDialogFragment newInstance() {
         ShareDialogFragment frag = new ShareDialogFragment();
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
 
         mTextBody = new EditText(getActivity());
         mTextBody.setHint(R.string.share_dialog_body_hint);
         mTextBody.setFilters(new InputFilter[] {
             new InputFilter.LengthFilter(LIMIT_SHARE_TEXT_LENGTH)
         });
 
         if (mMode == SHARE_TWITTER) {
             setTitle(getString(R.string.share_dialog_title_twitter));
         } else if (mMode == SHARE_TWITTER) {
             setTitle(getString(R.string.share_dialog_title_facebook));
         } else {
             setTitle(getString(R.string.share_dialog_title));
         }
 
         if (mBody != null && mBody.length() > 0) {
             mTextBody.setText(mBody);
 
             return new AlertDialog.Builder(getActivity())
                     .setTitle(mTitle)
                     .setView(mTextBody)
                     .setPositiveButton(R.string.share_dialog_post,
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     String body = mTextBody.getText().toString().trim();
 
                                     if (mListener != null) {
                                         mListener.onPositiveButtonSelected(mMode, body);
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
                 .setView(mTextBody)
                 .setPositiveButton(R.string.share_dialog_post,
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int whichButton) {
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
 
     private ShareDialogResultListener mListener;
 
     public interface ShareDialogResultListener {
         public boolean onPositiveButtonSelected(int mode, String body);
     }
 
     public void setShareDialogResultListener(ShareDialogResultListener listener) {
         mListener = listener;
     }
 }
