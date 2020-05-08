 package com.asa.sms2honeycomb.tablet;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.app.ListFragment;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.ContactsContract.CommonDataKinds.Phone;
 import android.provider.ContactsContract.Contacts;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.asa.sms2honeycomb.DatabaseAdapter;
 import com.asa.sms2honeycomb.MessageItem;
 import com.asa.sms2honeycomb.Preferences;
 import com.asa.sms2honeycomb.R;
 import com.asa.sms2honeycomb.Util.Util;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParsePush;
 import com.parse.ParseQuery;
 import com.parse.SaveCallback;
 import com.parse.SendCallback;
 
 public class MessageFragment extends ListFragment {
 	LayoutInflater inflater;
 	ViewGroup container;
 
 	private final String TAG = "MessageFragment";
 	private DatabaseAdapter dbAdapter;
 	private ArrayAdapter<String> messageAdapter;
 	private ArrayList<MessageItem> messageResults;
 
 	private ListView messageListView;
 	private EditText toField;
 	private EditText messageField;
 	private Button sendButton;
 	private Button addContactButton;
 
 	private Context mContext;
 
 	private String phoneNumber;
 	private int threadId = 1;
 
 	private final int CONTACT_PICKER_RESULT = 0;
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		// inflates the main.xml resource, but the default ListView is still
 		// generated on top of this view.
 		mContext = inflater.getContext();
 		inflater.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		// inflater.inflate(R.layout.fragment_message_view_new, container);
 		View view = inflater.inflate(R.layout.fragment_message_view_new, null);
 
 		// Open up the database needs to be above the conversationAdapter
 		dbAdapter = new DatabaseAdapter(getActivity());
 		dbAdapter.open();
 
 		// Get the Adapter for the list so iy can be updated separately
 		// I dont know why simple_list_item_1 works i copied it from an example
 		// messageAdapter = new ArrayAdapter<String>(getActivity(),
 		// android.R.layout.simple_list_item_1,
 		// dbAdapter.getMessageArrayList(phoneNumber));
 		//
 		// messageAdapter.notifyDataSetChanged();
 
 		QueryParseAsyncTask task = new QueryParseAsyncTask(0,
 				Util.getUsernameString());
 		AsyncTask<Void, Void, ArrayList<MessageItem>> asyncTask = task
 				.execute();
 
 		// BEGIN Adding contact stuff
 		addContactButton = (Button) view
 				.findViewById(R.id.message_add_contact_button);
 		addContactButton.setOnClickListener(new OnClickListener() {
 			public void onClick(View arg0) {
 				Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
 						Contacts.CONTENT_URI);
 				startActivityForResult(contactPickerIntent,
 						CONTACT_PICKER_RESULT);
 			}
 		});
 		// END Adding contact stuff.
 
 		// IT FUCKING HAS TO BE andriod.R.id.list fucking POS differences
 		messageListView = (ListView) view.findViewById(android.R.id.list);
 		toField = (EditText) view.findViewById(R.id.phone_to_field);
 		messageField = (EditText) view.findViewById(R.id.main_message_felid);
 		sendButton = (Button) view.findViewById(R.id.main_send_btn);
 		sendButton.setOnClickListener(new OnClickListener() {
 
 			public void onClick(View v) {
 				// TODO: get strings from the text feilds
 				// then send them on to parse and to the push channel(phone)
 				// on then on the phone send the messages via a sms message to
 				// the to number
 				final String to = toField.getText().toString().trim();
 				final String body = messageField.getText().toString().trim();
 				if (to.length() == 0) {
 					// User has not specified a user, thus, nothing happens.
 					Toast toast = Toast.makeText(getActivity(),
 							R.string.message_list_empty_target,
 							Toast.LENGTH_LONG);
 					toast.show();
 				} else {
 					if (phoneNumber.length() == 0) {
 						phoneNumber = to;
 					}
 					MessageItem item = new MessageItem();
 					item.setMessageAddress(phoneNumber);
 					item.setMessageBody(body);
 					item.setMessageRead(Preferences.READ);
 					item.setMessageThreadId(threadId);
 					item.setMessageType(Preferences.SENT);
 					item.setMessageUsername(Util.getUsernameString());
					item.setMessageTime(Util.getCurrentDate());
 					// Adds the Message to the list
 					messageResults.add(item);
 					messageAdapter.notifyDataSetChanged();
 					ParseObject outgoingMessage = new ParseObject(
 							Preferences.PARSE_TABLE_SMS);
 					outgoingMessage.put(Preferences.PARSE_SMS_ADDRESS,
 							phoneNumber);
 					outgoingMessage.put(Preferences.PARSE_SMS_BODY, body);
 					outgoingMessage.put(Preferences.PARSE_USERNAME_ROW,
 							Util.getUsernameString());
 					// Always mark the message as read.
 					outgoingMessage.put(Preferences.PARSE_SMS_READ, 1);
 					outgoingMessage.put(Preferences.PARSE_SMS_THREAD_ID,
 							threadId);
 					// Always mark the message type as a sent type.
 					outgoingMessage.put(Preferences.PARSE_SMS_TYPE,
 							Preferences.SENT);
 					outgoingMessage.saveInBackground(new SaveCallback() {
 						@Override
 						public void done(ParseException e) {
 							if (e != null) {
 								/*
 								 * Did not save correctly. Informing user
 								 * through toast
 								 */
 								Log.e(TAG, "Message not successfully saved.");
 								Toast toast = Toast.makeText(getActivity(),
 										R.string.message_list_unsuccessful,
 										Toast.LENGTH_LONG);
 								toast.show();
 							} else {
 								ParsePush push = new ParsePush();
 								push.setChannel(Util.getPushChannel(
 										Util.getUsernameString(),
 										Preferences.TABLET));
 								push.setMessage("To: " + to + " Message: "
 										+ body);
 								push.sendInBackground(new SendCallback() {
 									@Override
 									public void done(ParseException e) {
 										if (e != null) {
 											// Did not send push notification
 											// correctly.
 											Log.e(TAG,
 													"Push notification did not send correctly.",
 													e);
 											e.printStackTrace();
 										} else {
 											Log.d(TAG, "Push sent.");
 										}
 									}
 								});
 							}
 						}
 					});
					// clear the messageField when the text is send
					messageField.setText("");
 				}
 			}
 		});
 		return view;
 	}
 
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		// Log.d(TAG, "Item clicked: " + id);
 		// // When clicked, show a toast with the TextView text
 		// Toast.makeText(getActivity().getApplicationContext(),
 		// ((TextView) v).getText(), Toast.LENGTH_SHORT).show();
 
 	}
 
 	@Override
 	public void onDestroy() {
 		// Closing the database adapter when the activity gets destroyed.
 		super.onDestroy();
 		dbAdapter.close();
 	}
 
 	/**
 	 * A custom adapter that populates the Message ListView. We are going to be
 	 * using a custom list view (message_list_item). Thus need to create a
 	 * custom list adapter to accommodate the custom list view.
 	 * 
 	 * @author Aaron
 	 * 
 	 */
 	public class MessageListAdapter extends ArrayAdapter<String> {
 		private ArrayList<MessageItem> mMessages;
 		private Context mContext;
 		private LayoutInflater inflater;
 
 		public MessageListAdapter(Context context, int textViewResourceId,
 				ArrayList<MessageItem> messages) {
 			super(context, textViewResourceId);
 			mMessages = messages;
 			mContext = context;
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			View v = convertView;// super.getView(position, convertView,
 									// parent);
 			MessageItem item = mMessages.get(position);
 
 			inflater = (LayoutInflater) mContext
 					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			if (convertView == null) {
 				v = inflater.inflate(R.layout.message_list_item, null);
 			}
 			TextView nameTv = (TextView) v.findViewById(R.id.message_list_name);
 			TextView messageTv = (TextView) v
 					.findViewById(R.id.message_list_body);
 			messageTv.setText(item.getMessageBody());
 			TextView timeTv = (TextView) v.findViewById(R.id.message_list_time);
 			timeTv.setText(item.getMessageTime());
 			switch (item.getMessageType()) {
 			case Preferences.RECEIVED:
 				nameTv.setText(item.getMessageAddress());
 				break;
 			case Preferences.SENT:
 				nameTv.setText("Me");
 				break;
 			}
 			return v;
 		}
 
 		@Override
 		public int getCount() {
 			return mMessages.size();
 		}
 
 		// /**
 		// * Tells how many types of pools of messages to keep. Read:
 		// *
 		// http://logc.at/2011/10/10/handling-listviews-with-multiple-row-types/
 		// *
 		// * @return
 		// */
 		// @Override
 		// public int getViewTypeCount() {
 		// return Preferences.NUM_TYPE_OF_MESSAGES;
 		// }
 		//
 		// /**
 		// * Tells the list view which pool the view belongs to. Read:
 		// *
 		// http://logc.at/2011/10/10/handling-listviews-with-multiple-row-types/
 		// *
 		// * @param position
 		// * @return
 		// */
 		// @Override
 		// public int getItemViewType(int position) {
 		// if (mMessages.get(position).getMessageType() == RECEIVED) {
 		// return RECEIVED;
 		// }
 		// return SENT;
 		// }
 
 	}
 
 	/**
 	 * This is called after the user selects a contact.
 	 */
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		Activity activity = (Activity) mContext;
 		if (resultCode == Activity.RESULT_OK) {
 			switch (requestCode) {
 			case CONTACT_PICKER_RESULT:
 				Cursor cursor = null;
 				Uri uri = data.getData();
 				Preferences.URI = uri;
 				// Get the contactId from the URI
 				String contactId = uri.getLastPathSegment();
 
 				/*
 				 * Query the Contacts database with the Phone content_uri. The
 				 * selection is the Contact_ID that is equal to the contactId
 				 * from the uri returned
 				 */
 				cursor = activity.getContentResolver().query(Phone.CONTENT_URI,
 						null, Phone.CONTACT_ID + "=?",
 						new String[] { contactId }, null);
 				if (cursor.moveToFirst()) {
 					String columns[] = cursor.getColumnNames();
 
 					// Iterates throguh the cursor
 					// for (String column : columns) {
 					// int index = cursor.getColumnIndex(column);
 					// Log.v(TAG, "Contact Info - Column: " + column + " == ["
 					// + cursor.getString(index) + "]");
 					// }
 					int phoneIndex = cursor.getColumnIndex(Phone.DATA1);
 					int nameIndex = cursor.getColumnIndex(Phone.DISPLAY_NAME);
 					phoneNumber = cursor.getString(phoneIndex);
 					String displayName = cursor.getString(nameIndex);
 					displayName = displayName + " <" + phoneNumber + ">, ";
 					String currentText = toField.getText().toString();
 					// If there is current text, then concatenage the new number
 					if (currentText.length() > 0) {
 						displayName = currentText + displayName;
 					}
 					toField.setText(displayName);
 				} else {
 					// Contact has no phone information.
 					// TODO : Prompt user to input phone info?
 					Util.displayToast(mContext,
 							"Contact has no phone information.");
 				}
 				break;
 			}
 		} else {
 			Log.w(TAG, "Activity returned with no data.");
 		}
 	}
 
 	public class QueryParseAsyncTask extends
 			AsyncTask<Void, Void, ArrayList<MessageItem>> {
 		private final String TAG = "QueryParseAsyncTask";
 		private final int SMS = 0;
 		private final int THREAD = 1;
 
 		private int queryType;
 		private String username;
 
 		// private ArrayList<MessageItem> messageResults;
 
 		public QueryParseAsyncTask(int type, String user) {
 			queryType = type;
 			username = user;
 		}
 
 		@Override
 		protected ArrayList<MessageItem> doInBackground(Void... arg0) {
 			if (queryType == SMS) {
 				ParseQuery querySms = new ParseQuery(
 						Preferences.PARSE_TABLE_SMS);
 
 				querySms.whereEqualTo(Preferences.PARSE_USERNAME_ROW, username);
 				messageResults = new ArrayList<MessageItem>();
 				// Begin query for messages.
 				List<ParseObject> objects;
 				try {
 					objects = querySms.find();
 					if (Preferences.DEBUG)
 						Log.d(TAG, "Message size: " + objects.size());
 					for (ParseObject messageObject : objects) {
 						// TODO: Add the rest of the db items.
 						MessageItem messageItem = new MessageItem();
 						messageItem.setMessageBody(messageObject
 								.getString(Preferences.PARSE_SMS_BODY));
 						messageItem.setMessageUsername(messageObject
 								.getString(username));
 						messageItem.setMessageAddress(messageObject
 								.getString(Preferences.PARSE_SMS_ADDRESS));
 						messageItem.setMessageTime(messageObject.createdAt()
 								.toLocaleString());
 						messageItem.setMessageType(messageObject
 								.getInt(Preferences.PARSE_SMS_TYPE));
 						messageResults.add(messageItem);
 						String str = messageObject.toString();
 
 					}
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			return messageResults;
 		}
 
 		@Override
 		protected void onPostExecute(ArrayList<MessageItem> messageList) {
 			messageAdapter = new MessageListAdapter(getActivity(),
 					R.layout.message_list_item, messageList);
 
 			setListAdapter(messageAdapter);
 		}
 
 	}
 }
