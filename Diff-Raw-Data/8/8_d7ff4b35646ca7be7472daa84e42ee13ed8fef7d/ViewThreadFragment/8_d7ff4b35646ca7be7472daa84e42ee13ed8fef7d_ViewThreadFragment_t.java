 package com.hyperactivity.android_app.activities;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.*;
 import android.view.View.OnClickListener;
 import android.view.View.OnFocusChangeListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.*;
 import com.hyperactivity.android_app.R;
 import com.hyperactivity.android_app.core.AdminActionCallback;
 import com.hyperactivity.android_app.core.Engine;
 import com.hyperactivity.android_app.forum.models.Reply;
 import com.hyperactivity.android_app.forum.models.Thread;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 public class ViewThreadFragment extends Fragment {
 
     private int SINGLE_LINE_TEXT_HEIGHT;
     private int MULTIPLE_LINE_TEXT_HEIGHT;
 
     private ReplyListFragment replyList;
     private Thread currentThread;
     private TextView headlineField, textField, firstPostTimeStamp, firstPostAuthor;
    private ImageView firstPostImage, categoryImage;
     private EditText writeReplyField;
     private LinearLayout writeReplyButtonContainer, firstPostContainer;
     private Button writeReplyCancelButton, writeReplySubmitButton;
 
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.view_thread_fragment, null);
 
         headlineField = (TextView) view.findViewById(R.id.thread_headline_field);
         textField = (TextView) view.findViewById(R.id.thread_text_field);
         firstPostTimeStamp = (TextView) view.findViewById(R.id.firstPost_timestamp);
         firstPostAuthor = (TextView) view.findViewById(R.id.firstPost_author);
         firstPostImage = (ImageView) view.findViewById(R.id.firstPost_image);
         firstPostContainer = (LinearLayout) view.findViewById(R.id.first_post_container);
        categoryImage = (ImageView) view.findViewById(R.id.category_image);
 
         // Needed to take focus from writeReplyField
         headlineField.setFocusable(true);
         headlineField.setFocusableInTouchMode(true);
 
        categoryImage.setImageBitmap(currentThread.getParentCategory().getImage(getActivity()));
        categoryImage.setAlpha(30);

         initializeWriteReplyContainer(view);
 
         // TODO ugly
         SINGLE_LINE_TEXT_HEIGHT = 70;
         MULTIPLE_LINE_TEXT_HEIGHT = SINGLE_LINE_TEXT_HEIGHT * 3;
 
         replyList = new ReplyListFragment();
         replyList.updateReplyList(new ArrayList<Reply>());
 
         if (getActivity() instanceof AdminActionCallback) {
             replyList.setCallback((AdminActionCallback) getActivity());
         }
 
         return view;
     }
 
     private void initializeWriteReplyContainer(View view) {
         writeReplyField = (EditText) view.findViewById(R.id.write_reply_field);
         writeReplyButtonContainer = (LinearLayout) view.findViewById(R.id.write_reply_button_container);
         writeReplyCancelButton = (Button) view.findViewById(R.id.write_reply_cancel_button);
         writeReplySubmitButton = (Button) view.findViewById(R.id.write_reply_submit_button);
 
         writeReplyCancelButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 removeFocusFromWriteReply();
             }
         });
         writeReplySubmitButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 if (writeReplyField.getText().toString().trim().length() > 0) {
                     ((Engine) getActivity().getApplication()).getPublicForum().createReply(getActivity(), currentThread.getId(), writeReplyField.getText().toString().trim(), true);
                 }else{
                     AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
                     alert.setMessage(getString(R.string.reply_empty));
                     alert.setPositiveButton("Ok", null);
                     alert.show();
                 }
             }
         });
         writeReplyField.setOnFocusChangeListener(new OnFocusChangeListener() {
 
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 if (hasFocus) {
                     expandWriteReply();
                 } else {
                     shrinkWriteReply();
                 }
             }
         });
     }
 
     private void clearWriteReplyContainer() {
         writeReplyField.setText("");
         removeFocusFromWriteReply();
     }
 
     private void removeFocusFromWriteReply() {
         headlineField.requestFocus();
     }
 
     private void expandWriteReply() {
         writeReplyField.setSingleLine(false);
         writeReplyField.getLayoutParams().height = MULTIPLE_LINE_TEXT_HEIGHT;
         writeReplyButtonContainer.setVisibility(View.VISIBLE);
 
         // Show keyboard
         InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.showSoftInput(writeReplyField, InputMethodManager.SHOW_IMPLICIT);
     }
 
     private void shrinkWriteReply() {
         writeReplyField.getLayoutParams().height = SINGLE_LINE_TEXT_HEIGHT;
         writeReplyField.setSingleLine(true);
         writeReplyButtonContainer.setVisibility(View.INVISIBLE);
 
         // Hide keyboard
         InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
         imm.hideSoftInputFromWindow(writeReplyField.getWindowToken(), 0);
     }
 
     public void onPause() {
         super.onPause();
         getFragmentManager().beginTransaction().remove(replyList).commit();
     }
 
     public void onResume() {
         super.onResume();
         getFragmentManager().beginTransaction().replace(R.id.reply_list_container, replyList).commit();
 		if (currentThread != null) {
 			updateCurrentThread();
 		}
 	}
 	
 	public void setCurrentThread(Thread thread) {
 		currentThread = thread;
 		if (getActivity() != null) {
 			updateCurrentThread();
 		}
 	}
 	
 	public List<Reply> updateReplies() {
 		List<Reply> replies = currentThread.getReplies();
 		if (replies != null) {
 			replyList.updateReplyList(replies);
             currentThread.getAccount().setProfilePicture(MainActivity.cachedAccounts.get(currentThread.getAccount().getId()));
             firstPostImage.setImageBitmap(currentThread.getAccount().getProfilePicture());
             return replies;
 		}else{
             return new LinkedList<Reply>();
         }
 
 	}
 	
 	private void updateCurrentThread() {
 		headlineField.setText(currentThread.getHeadLine());
 		textField.setText(currentThread.getText());
         firstPostAuthor.setText(currentThread.getAccount().getUsername());
         firstPostTimeStamp.setText(currentThread.getTime().toString());
         currentThread.getAccount().setProfilePicture(MainActivity.cachedAccounts.get(currentThread.getAccount().getId()));
         firstPostImage.setImageBitmap(currentThread.getAccount().getProfilePicture());
 
         firstPostContainer.setOnClickListener(new OnClickListener() {
             @Override
             public void onClick(View view) {
                 ((MainActivity) getActivity()).visitAccount(currentThread.getAccount());
             }
         });
 
 		clearWriteReplyContainer();
         updateReplies();
 	}
 
 }
