 package com.moac.android.opensecretsanta.fragment;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import com.moac.android.opensecretsanta.OpenSecretSantaApplication;
 import com.moac.android.opensecretsanta.R;
 import com.moac.android.opensecretsanta.activity.Intents;
 import com.moac.android.opensecretsanta.database.DatabaseManager;
 import com.moac.android.opensecretsanta.model.Assignment;
 import com.moac.android.opensecretsanta.model.Group;
 import com.moac.android.opensecretsanta.model.Member;
 import com.moac.android.opensecretsanta.receiver.SmsSendReceiver;
 import com.moac.android.opensecretsanta.util.Notifier;
 import com.moac.android.opensecretsanta.util.SmsNotifier;
 import com.squareup.picasso.Picasso;
 
 public class NotifyFragment extends DialogFragment {
 
     private static final String TAG = NotifyFragment.class.getSimpleName();
     private static final String MESSAGE_TAG = "MESSAGE";
 
     protected EditText mMsgField;
     protected DatabaseManager mDb;
     protected Group mGroup;
     protected long[] mMemberIds;

    // Apparently this is how you retain EditText fields - http://code.google.com/p/android/issues/detail?id=18719
     private String cachedMsg;
 
     /**
      * Factory method for this fragment class
      *
      * We do this because according to the Fragment docs -
      *
      * "It is strongly recommended that subclasses do not have other constructors with parameters"
      */
     public static NotifyFragment create(long _groupId, long[] _memberIds) {
         Log.i(TAG, "NotifyFragment() - factory creating for groupId: " + _groupId + " memberIds: " + _memberIds);
         NotifyFragment fragment = new NotifyFragment();
         Bundle args = new Bundle();
         args.putLong(Intents.GROUP_ID_INTENT_EXTRA, _groupId);
         args.putLongArray(Intents.MEMBER_ID_ARRAY_INTENT_EXTRA, _memberIds);
         fragment.setArguments(args);
         return fragment;
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
 
         Log.i(TAG, "onCreateDialog() - start: " + this);
         mDb = OpenSecretSantaApplication.getDatabase();
         long groupId = getArguments().getLong(Intents.GROUP_ID_INTENT_EXTRA);
         long[] memberIds = getArguments().getLongArray(Intents.MEMBER_ID_ARRAY_INTENT_EXTRA);
         mGroup = mDb.queryById(groupId, Group.class);
 
         String message = cachedMsg == null ? mGroup.getMessage() :
           savedInstanceState.getString(MESSAGE_TAG);
 
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
         // Inflate layout
         LayoutInflater inflater = getActivity().getLayoutInflater();
         View view = inflater.inflate(R.layout.notify_fragment_dialog, null);
 
         // Take the values and populate the dialog
         builder.setTitle("Notify Group");
         builder.setIcon(R.drawable.ic_menu_notify);
 
         mMsgField = (EditText) view.findViewById(R.id.messageTxtEditText);
         mMsgField.setText(message);
 
         final TextView charCountView = (TextView) view.findViewById(R.id.msg_char_count);
         charCountView.setText(String.valueOf(mMsgField.length()));
 
         // Add the callback to the field
         mMsgField.addTextChangedListener(new TextWatcher() {
             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {}
 
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
 
             @Override
             public void afterTextChanged(Editable s) {
                 // Update the reported character length
                 charCountView.setText(String.valueOf(s.length()));
             }
         });
 
         if(mMemberIds != null) {
             LinearLayout container = (LinearLayout) view.findViewById(R.id.avatar_container_layout);
             for(long id : mMemberIds) {
                 Member member = mDb.queryById(id, Member.class);
                 Uri uri = member.getContactUri(getActivity());
                 if(uri != null) {
                     Log.v(TAG, "onCreateDialog() - adding avatar: " + member.getName());
                     Log.v(TAG, "onCreateDialog() - uri: " + uri);
                     ImageView avatar = new ImageView(getActivity());
                     avatar.setLayoutParams(new LinearLayout.LayoutParams(80, 80));
                     avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
                     container.addView(avatar);
                     Picasso.with(getActivity()).load(uri).into(avatar);
                 }
             }
         }
 
         builder.setCancelable(true);
         builder.setNegativeButton(android.R.string.cancel, null);
         builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 executeNotify();
             }
         });
 
         builder.setView(view);
         return builder.create();
     }
 
     @Override
     public void onSaveInstanceState(Bundle outState) {
         Log.i(TAG, "onSaveInstanceState()");
         super.onSaveInstanceState(outState);
         outState.putString(MESSAGE_TAG, mMsgField.getText().toString());
         Log.d(TAG,  "onSaveInstanceState() msg: " + outState.getString(MESSAGE_TAG));
         cachedMsg = outState.getString(MESSAGE_TAG);
     }
 
     @Override
     public void onDestroyView() {
        // Refer to - http://code.google.com/p/android/issues/detail?id=17423
         if (getDialog() != null && getRetainInstance())
             getDialog().setDismissMessage(null);
         super.onDestroyView();
     }
 
     private void executeNotify() {
         // Get the custom message.
         mGroup.setMessage(mMsgField.getText().toString().trim());
         mDb.update(mGroup);
 
         // Iterate through the provided members - get their Assignment.
         for(long memberId : mMemberIds) {
             Member member = mDb.queryById(memberId, Member.class);
             Assignment assignment = mDb.queryAssignmentForMember(memberId);
             if(assignment == null) {
                 Log.e(TAG, "executeNotify() - No Assignment for Member: " + member.getName());
                 continue;
             }
             Member giftReceiver = mDb.queryById(assignment.getReceiverMemberId(), Member.class);
 
             switch(member.getContactMode()) {
                 case SMS:
                     Log.i(TAG, "executeNotify() - Building SMS Notifier for: " + member.getName());
                     Notifier notifier = new SmsNotifier(getActivity(), new SmsSendReceiver(mDb), true);
                     notifier.notify(member, giftReceiver.getName(), mGroup.getMessage());
                     break;
                 case EMAIL:
                     Log.i(TAG, "executeNotify() - Building Email Notifier for: " + member.getName());
                     break;
                 case REVEAL_ONLY:
                     break;
                 default:
                     Log.e(TAG, "executeNotify() - Unknown contact mode: " + member.getContactMode());
             }
         }
     }
 }
