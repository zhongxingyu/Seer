 package com.example.AndroidContactViewer;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.*;
 
 import com.example.AndroidContactViewer.datastore.ContactDataSource;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 
 /**
  * Created with IntelliJ IDEA. User: raytiley Date: 2/17/13 Time: 6:12 PM To
  * change this template use File | Settings | File Templates.
  */
 public class ContactEditActivity extends Activity implements OnClickListener {
 
 	private Contact _contact;
 	private ImageButton _defaultPhoneButton;
 	private ImageButton _defaultTextButton;
 	private ImageButton _defaultEmailButton;
 
     private String _defaultCallPhone;
     private String _defaultMessagePhone;
     private String _defaultEmail;
 
 	public void onCreate(Bundle savedInstanceState) {
 		Resources res;
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.contact_edit);
 
 		res = getResources();
 		ToolbarConfig toolbar = new ToolbarConfig(this,
 				res.getString(R.string.edit_contact));
 
 		int contactID = (Integer) getIntent().getExtras().get("ContactID");
 		if (contactID == 0) {
 			toolbar.getToolbarTitleView().setText(
 					res.getString(R.string.new_contact));
 			// Lets create a brand spanking new contact.
 			_contact = new Contact(0, "");
             _defaultMessagePhone = "";
             _defaultCallPhone = "";
             _defaultEmail = "";
 		} else {
 			ContactDataSource datasource = new ContactDataSource(this);
 			datasource.open();
 			_contact = datasource.get(contactID);
 			datasource.close();
 		}
 
         //Check if we have gravatar on disk
         String filename = Integer.toString(_contact.getId()) + "-gravatar.jpg";
         try
         {
             File imgFile = getFileStreamPath(filename);
             if(imgFile.exists())
             {
                 ImageView iv = (ImageView) findViewById(R.id.contact_image);
                 Bitmap gravatar = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                 iv.setImageBitmap(gravatar);
             }
         }
         catch(Exception e)
         {
             Log.e("gravatar", e.getMessage());
         }
 
 		// setup the "Edit" button
 		Button button = toolbar.getToolbarRightButton();
 		button.setText("Save");
 		button.setOnClickListener(this);
 
 		toolbar.hideLeftButton();
 
 		((EditText) findViewById(R.id.edit_contact_name)).setText(_contact
 				.getName());
 		((EditText) findViewById(R.id.edit_contact_title)).setText(_contact
 				.getTitle());
 
         _defaultEmail = _contact.getDefaultEmail() == null ? "" : _contact.getDefaultEmail();
         _defaultCallPhone = _contact.getDefaultContactPhone() == null ? "" : _contact.getDefaultContactPhone();
         _defaultMessagePhone = _contact.getDefaultTextPhone() == null ? "" : _contact.getDefaultTextPhone();
 
 		List<String> emailList = _contact.getEmails();
 		LinearLayout emails = (LinearLayout) findViewById(R.id.contact_edit_email_list);
 		if (emailList.size() != 0) {
 			// Can't use a ListView for e-mails or phone lists since we want the
 			// entire
 			// profile screen to scroll rather than two scroll areas for the
 			// e-mails and
 			// phone lists.
 
 			for (String email : emailList) {
 				LayoutInflater inflater = getLayoutInflater();
 				View item = inflater.inflate(R.layout.contact_email_edit_item,
 						emails, false);
 
 				((EditText) item
 						.findViewById(R.id.contact_edit_email_edit_text))
 						.setText(email);
 
 				ImageButton email_btn = (ImageButton) item
 						.findViewById(R.id.contact_edit_email_action);
 				if (email.equals(_contact.getDefaultEmail())) {
 					email_btn.setImageResource(R.drawable.email_selected_transparent);
 					_defaultEmailButton = email_btn;
 				}
 				email_btn.setTag(item);
 				email_btn.setOnClickListener(this);
 
 				ImageButton remove_btn = (ImageButton) item
 						.findViewById(R.id.contact_edit_email_delete);
 				remove_btn.setTag(item);
 				remove_btn.setOnClickListener(this);
 
 				emails.addView(item);
 			}
 		}
 
 		// Add phones to view
 
 		List<String> phoneList = _contact.getPhoneNumbers();
 		LinearLayout phones = (LinearLayout) findViewById(R.id.contact_edit_phone_list);
 		if (phoneList.size() != 0) {
 
 			for (String phone : phoneList) {
 				LayoutInflater inflater = getLayoutInflater();
 				View item = inflater.inflate(R.layout.contact_phone_edit_item,
 						phones, false);
 
 				((EditText) item
 						.findViewById(R.id.contact_edit_phone_edit_text))
 						.setText(phone);
 
 				ImageButton contact_btn = (ImageButton) item
 						.findViewById(R.id.contact_edit_call_action);
 				if (phone.equals(_contact.getDefaultContactPhone())) {
 					contact_btn.setImageResource(R.drawable.phone_selected_transparent);
 					_defaultPhoneButton = contact_btn;
 				}
 				contact_btn.setTag(item);
 				contact_btn.setOnClickListener(this);
 
 				ImageButton text_btn = (ImageButton) item
 						.findViewById(R.id.contact_edit_txt_action);
 				if (phone.equals(_contact.getDefaultTextPhone())) {
 					text_btn.setImageResource(R.drawable.texting_selected_transparent);
 					_defaultTextButton = text_btn;
 				}
 				text_btn.setTag(item);
 				text_btn.setOnClickListener(this);
 
 				ImageButton remove_btn = (ImageButton) item
 						.findViewById(R.id.contact_edit_phone_delete);
 				remove_btn.setTag(item);
 				remove_btn.setOnClickListener(this);
 
 				phones.addView(item);
 			}
 		}
 
 		// Setup Add Buttons
 		ImageButton phone_add = (ImageButton) findViewById(R.id.contact_edit_phone_add);
 		phone_add.setOnClickListener(this);
 
 		ImageButton emails_add = (ImageButton) findViewById(R.id.contact_edit_email_add);
 		emails_add.setOnClickListener(this);
 	}
 
 	public void onClick(View v) {
 		View view = (View) v.getTag();
 		String text = null;
 		switch (v.getId()) {
 		case R.id.contact_edit_txt_action:
 		case R.id.contact_edit_call_action:
 		case R.id.contact_edit_phone_delete:
 			text = ((EditText) view
 					.findViewById(R.id.contact_edit_phone_edit_text)).getText()
 					.toString();
 			break;
 		case R.id.contact_edit_email_action:
 		case R.id.contact_edit_email_delete:
 			text = ((EditText) view
 					.findViewById(R.id.contact_edit_email_edit_text)).getText()
 					.toString();
 			break;
 		default:
 			text = "Unknown";
 		}
 
 		switch (v.getId()) {
 		case R.id.contact_edit_txt_action:
 			_defaultMessagePhone = text;
 			if (_defaultTextButton != null) {
 				_defaultTextButton.setImageResource(R.drawable.texting_transparent);
 			}
 			_defaultTextButton = (ImageButton)view.findViewById(R.id.contact_edit_txt_action);
 			_defaultTextButton.setImageResource(R.drawable.texting_selected_transparent);
 			break;
 		case R.id.contact_edit_call_action:
 			_defaultCallPhone = text;
 			if (_defaultPhoneButton != null) {
 				_defaultPhoneButton.setImageResource(R.drawable.phone_transparent);
 			}
 			_defaultPhoneButton = (ImageButton)view.findViewById(R.id.contact_edit_call_action);
 			_defaultPhoneButton.setImageResource(R.drawable.phone_selected_transparent);
 			break;
 		case R.id.contact_edit_email_action:
 			_defaultEmail = text;
 			
 			if (_defaultEmailButton != null) {
 				_defaultEmailButton.setImageResource(R.drawable.email_transparent);
 			}
 			_defaultEmailButton = (ImageButton)view.findViewById(R.id.contact_edit_email_action);
 			_defaultEmailButton.setImageResource(R.drawable.email_selected_transparent);
 			break;
 		case R.id.toolbar_left_button:
 			finish();
 			break;
 		case R.id.toolbar_right_button:
 			Toast.makeText(ContactEditActivity.this, "Saving Contact",
 					Toast.LENGTH_SHORT).show();
 
             saveContact();
             finish();
 			break;
 		case R.id.contact_edit_phone_delete:
             if(_defaultMessagePhone.equals(text)) {
                 _defaultMessagePhone = "";
             }
             if(_defaultCallPhone.equals(text)) {
                 _defaultCallPhone = "";
             }
 			Toast.makeText(ContactEditActivity.this, "Remove phone: " + text,
 					Toast.LENGTH_SHORT).show();
 			((LinearLayout) findViewById(R.id.contact_edit_phone_list))
 					.removeView(view);
 			break;
 		case R.id.contact_edit_email_delete:
 			Toast.makeText(ContactEditActivity.this, "Remove email: " + text,
 					Toast.LENGTH_SHORT).show();
 			((LinearLayout) findViewById(R.id.contact_edit_email_list))
 					.removeView(view);
 			break;
 		case R.id.contact_edit_phone_add:
 			addViewForPhone("", false, false);
 			break;
 		case R.id.contact_edit_email_add:
 			addViewForEmail("", false);
 			break;
 		default:
 			Toast.makeText(ContactEditActivity.this,
 					"Unknown clickable item ID", Toast.LENGTH_SHORT).show();
 			break;
 
 		}
 	}
 
 	private void addViewForPhone(String text, Boolean defaultCall, Boolean defaultText) {
 		LinearLayout phones = (LinearLayout) findViewById(R.id.contact_edit_phone_list);
 
 		LayoutInflater inflater = getLayoutInflater();
 		View item = inflater.inflate(R.layout.contact_phone_edit_item, phones,
 				false);
 
         EditText editText = (EditText) item.findViewById(R.id.contact_edit_phone_edit_text);
         editText.setText(text);
 
 		ImageButton contact_btn = (ImageButton) item
 				.findViewById(R.id.contact_edit_call_action);
 		contact_btn.setTag(item);
 		contact_btn.setOnClickListener(this);
 
 		ImageButton text_btn = (ImageButton) item
 				.findViewById(R.id.contact_edit_txt_action);
 		text_btn.setTag(item);
 		text_btn.setOnClickListener(this);
 
         if(defaultCall) {
             contact_btn.setImageResource(R.drawable.phone_selected_transparent);
         } else {
             contact_btn.setImageResource(R.drawable.phone_transparent);
         }
 
         if(defaultText) {
             text_btn.setImageResource(R.drawable.texting_selected_transparent);
         } else {
             text_btn.setImageResource(R.drawable.texting_transparent);
         }
 
 		ImageButton remove_btn = (ImageButton) item
 				.findViewById(R.id.contact_edit_phone_delete);
 		remove_btn.setTag(item);
 		remove_btn.setOnClickListener(this);
 
 		phones.addView(item);
 	}
 
 	private void addViewForEmail(String text, Boolean defaultEmail) {
 		LinearLayout emails = (LinearLayout) findViewById(R.id.contact_edit_email_list);
 
 		LayoutInflater inflater = getLayoutInflater();
 		View item = inflater.inflate(R.layout.contact_email_edit_item, emails,
 				false);
 
         EditText editText = (EditText) item.findViewById(R.id.contact_edit_email_edit_text);
         editText.setText(text);
 
 		ImageButton contact_btn = (ImageButton) item
 				.findViewById(R.id.contact_edit_email_action);
 		contact_btn.setTag(item);
 		contact_btn.setOnClickListener(this);
 
         if(defaultEmail) {
             contact_btn.setImageResource(R.drawable.email_selected_transparent);
         } else {
             contact_btn.setImageResource(R.drawable.email_transparent);
         }
 
 		ImageButton remove_btn = (ImageButton) item
 				.findViewById(R.id.contact_edit_email_delete);
 		remove_btn.setTag(item);
 		remove_btn.setOnClickListener(this);
 
 		emails.addView(item);
 	}
 
     private void saveContact() {
         String name = ((EditText)findViewById(R.id.edit_contact_name)).getText().toString();
         _contact.setName(name);
 
         String title = ((EditText) findViewById(R.id.edit_contact_title)).getText().toString();
         _contact.setTitle(title);
 
         // Because views are added and removed we need to make the contact reflect the current UI
         _contact.clearEmailsAndPhones();
 
         //Set all emails
         LinearLayout emails = (LinearLayout) findViewById(R.id.contact_edit_email_list);
         for (int i = 0; i < emails.getChildCount(); i++) {
             LinearLayout email = (LinearLayout)emails.getChildAt(i);
             EditText text = (EditText)email.findViewById(R.id.contact_edit_email_edit_text);
             _contact.addEmail(text.getText().toString());
         }
 
         //Set all phones
         LinearLayout phones = (LinearLayout) findViewById(R.id.contact_edit_phone_list);
         for (int i = 0; i < phones.getChildCount(); i++) {
             LinearLayout phone = (LinearLayout)phones.getChildAt(i);
             EditText text = (EditText)phone.findViewById(R.id.contact_edit_phone_edit_text);
             _contact.addPhoneNumber(text.getText().toString());
         }
 
         //Setup Defaults
         _contact.setDefaultContactPhone(_defaultCallPhone);
         _contact.setDefaultTextPhone(_defaultMessagePhone);
         _contact.setDefaultEmail(_defaultEmail);
 
 
         ContactDataSource datasource = new ContactDataSource(this);
         datasource.open();
         if(_contact.getId() > 0) {
             datasource.update(_contact);
         }
         else {
             _contact = datasource.add(_contact);
         }
 
         datasource.close();
 
         //Download Gravatar
         _contact.downloadGravatar(this);
     }
 
     @Override
     protected void onSaveInstanceState(Bundle icicle) {
         super.onSaveInstanceState(icicle);
 
         icicle.putString("contactName", ((EditText)findViewById(R.id.edit_contact_name)).getText().toString());
         icicle.putString("contactTitle", ((EditText)findViewById(R.id.edit_contact_title)).getText().toString());
         icicle.putString("defaultEmail", _defaultEmail);
         icicle.putString("defaultCallPhone", _defaultCallPhone);
         icicle.putString("defaultMessagePhone", _defaultMessagePhone);
         ArrayList<String> emailStore = new ArrayList<String>();
         ArrayList<String> phoneStore = new ArrayList<String>();
 
         LinearLayout emails = (LinearLayout) findViewById(R.id.contact_edit_email_list);
         for (int i = 0; i < emails.getChildCount(); i++) {
             LinearLayout email = (LinearLayout)emails.getChildAt(i);
             EditText text = (EditText)email.findViewById(R.id.contact_edit_email_edit_text);
             emailStore.add(text.getText().toString());
         }
 
         //Set all phones
         LinearLayout phones = (LinearLayout) findViewById(R.id.contact_edit_phone_list);
         for (int i = 0; i < phones.getChildCount(); i++) {
             LinearLayout phone = (LinearLayout)phones.getChildAt(i);
             EditText text = (EditText)phone.findViewById(R.id.contact_edit_phone_edit_text);
             phoneStore.add(text.getText().toString());
         }
 
         icicle.putStringArrayList("emails", emailStore);
         icicle.putStringArrayList("phones", phoneStore);
     }
 
     @Override
     public void onRestoreInstanceState(Bundle icicle) {
         if (icicle != null) {
 
             LinearLayout phones = (LinearLayout) findViewById(R.id.contact_edit_phone_list);
             LinearLayout emails = (LinearLayout) findViewById(R.id.contact_edit_email_list);
 
             phones.removeAllViews();
             emails.removeAllViews();
 
             //dethaw some stuff
             ((EditText)findViewById(R.id.edit_contact_name)).setText(icicle.getString("contactName"));
             ((EditText)findViewById(R.id.edit_contact_title)).setText(icicle.getString("contactTitle"));
 
             ArrayList<String> emailStore = icicle.getStringArrayList("emails");
             ArrayList<String> phoneStore = icicle.getStringArrayList("phones");
             _defaultEmail = icicle.getString("defaultEmail");
             _defaultCallPhone = icicle.getString("defaultCallPhone");
             _defaultMessagePhone = icicle.getString("defaultMessagePhone");
 
             for (String email : emailStore) {
                 addViewForEmail(email, email.equals(_defaultEmail));
             }
 
             for (String phone : phoneStore) {
                 addViewForPhone(phone,
                         phone.equals(_defaultCallPhone),
                         phone.equals(_defaultMessagePhone));
             }
         }
     }
 }
