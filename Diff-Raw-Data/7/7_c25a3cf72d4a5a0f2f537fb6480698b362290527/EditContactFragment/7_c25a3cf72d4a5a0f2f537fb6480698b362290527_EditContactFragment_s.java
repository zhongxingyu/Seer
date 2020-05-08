 package com.example.prolog;
 
 import java.io.FileNotFoundException;
 import java.util.HashMap;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import com.example.prolog.db.ContactsDataSource;
 import com.example.prolog.model.Contact;
 
 import android.app.Activity;
 import android.app.Fragment;
 import android.app.FragmentManager;
 import android.app.FragmentTransaction;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.BitmapDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.QuickContactBadge;
 import android.widget.RelativeLayout;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class EditContactFragment extends Fragment {
 
 	private static final AtomicInteger sNextGeneratedId = new AtomicInteger(2);
 	
 	ContactsDataSource datasource;
 	Contact contact;
 	long contactId;
 	EditText tvName, tvTitle, tvCompany, tvPhone, tvEmail, tvLocation;
 	QuickContactBadge quickContactBadge;
 	public static final String TAG = EditContactFragment.class.getSimpleName();
 
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onActivityCreated(savedInstanceState);
 		
 		final HashMap<Integer, Integer> customFieldIDs = new HashMap<Integer, Integer>();
 		
 
 		contactId = getArguments().getLong("contactId");
 
 		datasource = new ContactsDataSource(getActivity());
 		datasource.open();
 		contact = datasource.findContactbyId(contactId);
 		quickContactBadge = (QuickContactBadge) getActivity().findViewById(
 				R.id.quickContactBadge);
 		if (contact.getPhoto() != null)
 			quickContactBadge.setImageBitmap(contact.getPhoto());
 		else
 			quickContactBadge.setImageResource(R.drawable.face);
 		quickContactBadge.setOnClickListener(new View.OnClickListener() {
 
 			@Override
 			public void onClick(View v) {
 				Intent intent = new Intent(
 						Intent.ACTION_PICK,
 						android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				startActivityForResult(intent, 0);
 			}
 		});
 
 		tvName = (EditText) getActivity().findViewById(R.id.editTextName);
 		tvTitle = (EditText) getActivity().findViewById(R.id.editTextTitle);
 		tvCompany = (EditText) getActivity().findViewById(R.id.editTextCompany);
 		tvPhone = (EditText) getActivity().findViewById(R.id.editTextPhone);
 		tvEmail = (EditText) getActivity().findViewById(R.id.editTextEmail);
 		tvLocation = (EditText) getActivity().findViewById(
 				R.id.editTextLocation);
 		Button buttonSave = (Button) getActivity().findViewById(
 				R.id.activityAddNewContactSaveButton);
 		Button buttonCancel = (Button) getActivity().findViewById(
 				R.id.activityAddNewContactCancelButton);
 		tvName.setText(contact.getName());
 		tvTitle.setText(contact.getTitle());
 		tvCompany.setText(contact.getCompany());
 		tvPhone.setText(contact.getHome_phone());
 		tvEmail.setText(contact.getEmail());
 		tvLocation.setText(contact.getLocation());
 		
 		  /***
          * Display custom fields
          */
         if (contact.getAllCustomFields() != null && contact.getAllCustomFields().size() > 0){
             
             TableLayout tl=(TableLayout)getActivity().findViewById(R.id.add_new_contact_lo);
                         
             TextView tvFieldName = null;
             EditText editFieldValue = null;
             
             TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);  
             
             HashMap<String, Object> customFields = contact.getAllCustomFields();
             Set<String> keys = customFields.keySet();
             
             // for each custom field
             for (String string : keys) {
                 tvFieldName = new TextView(EditContactFragment.this.getActivity());
                 tvFieldName.setText(string);
                 tvFieldName.setLayoutParams(lparams);
                 tvFieldName.setTextAppearance(EditContactFragment.this.getActivity(),android.R.style.TextAppearance_Medium);
                 tvFieldName.setTextColor(Color.WHITE);
                 tvFieldName.setEms(4);
                 
                 editFieldValue = new EditText(EditContactFragment.this.getActivity());
                 editFieldValue.setText((String)contact.getCustomField(string));
                 editFieldValue.setLayoutParams(lparams);
                 editFieldValue.setTextAppearance(EditContactFragment.this.getActivity(),android.R.style.TextAppearance_Medium);
                 editFieldValue.setTextColor(Color.BLACK);              
                 editFieldValue.setEms(10);
                 editFieldValue.setBackgroundColor(Color.WHITE);
                 
                 int idNameField = generateViewId();
                 int idValueField = generateViewId();
                 
                 customFieldIDs.put(idNameField, idValueField);
                 tvFieldName.setId(idNameField);
                 editFieldValue.setId(idValueField);
                 
                 // create a new row for label and edit text field
                 TableRow.LayoutParams tparams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);                    
                 TableRow tr = new TableRow(EditContactFragment.this.getActivity());
                 tr.setLayoutParams(tparams);
                 
                 tr.addView(tvFieldName);
                 tr.addView(editFieldValue);
                 
                                             
                 tl.addView(tr, tl.getChildCount() - 1);
             }
             
         }
 		
 		
 		
 
 		buttonSave.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 
 				if (!tvName.getText().toString().equals(""))
 
 				{
 
 					contact.setName(tvName.getText().toString());
 					contact.setTitle(tvTitle.getText().toString());
 					contact.setCompany(tvCompany.getText().toString());
 					contact.setHome_phone(tvPhone.getText().toString());
 					contact.setEmail(tvEmail.getText().toString());
 					contact.setLocation(tvLocation.getText().toString());
 					
 					// Save custom fields as well
 					Set<Integer> keys = customFieldIDs.keySet();
 					for (Integer integer : keys) {
 						// get the field name
 						String fieldName = ((TextView) getActivity().findViewById(integer.intValue())).getText().toString();
 						String fieldValue = ((EditText) getActivity().findViewById(customFieldIDs.get(integer).intValue())).getText().toString();					
 						contact.setCustomField(fieldName, fieldValue);
 					}
 					
 					BitmapDrawable bitmapDrawable = ((BitmapDrawable) quickContactBadge
 							.getDrawable());
 					contact.setPhoto(bitmapDrawable.getBitmap());
 					datasource.updateContact(contact);
 					Bundle b = new Bundle();
 					b.putLong("contactId", contactId);
 					EditContactFragment viewContactFragment = new EditContactFragment();
 					viewContactFragment.setArguments(b);
 					FragmentManager fmi = getFragmentManager();
 					/*
 					 * FragmentTransaction ftu = fmi.beginTransaction();
 					 * ftu.replace(android.R.id.content, viewContactFragment)
 					 * .addToBackStack(null).commit();
 					 */
 					fmi.popBackStack();
 				} else {
 
 					Toast.makeText(getActivity(),
 							Commons.pleaseEnterNameContact, Toast.LENGTH_SHORT)
 							.show();
 				}
 			}
 		});
 
 		buttonCancel.setOnClickListener(new View.OnClickListener() {
 			public void onClick(View v) {
 				Bundle b = new Bundle();
 				b.putLong("contactId", contactId);
				EditContactFragment viewContactFragment = new EditContactFragment();
 				viewContactFragment.setArguments(b);
 				FragmentManager fmi = getFragmentManager();
				FragmentTransaction ftu = fmi.beginTransaction();
				ftu.replace(android.R.id.content, viewContactFragment)
						.addToBackStack(null).commit();
 
 			}
 		});
 		
 		
 		 // button to add field
         Button buttAddField = (Button) getActivity().findViewById(R.id.activityAddNewFieldButton);
         buttAddField.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 try{
                 	TableLayout tl = (TableLayout)  getActivity().findViewById(R.id.add_new_contact_lo);
 	                TableRow.LayoutParams lparams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);                  
 	                EditText editNewFieldName=new EditText(EditContactFragment.this.getActivity());                 
 	                editNewFieldName.setLayoutParams(lparams);                  
 	                editNewFieldName.setTextColor(Color.WHITE);                                             
 	                editNewFieldName.setHint("New field");                  
 	                editNewFieldName.requestFocus();
 	                editNewFieldName.setEms(4);     
 	            
 	                
 	                EditText editNewFieldValue = new EditText(EditContactFragment.this.getActivity());
 	                editNewFieldValue.setLayoutParams(lparams);                    
 	                editNewFieldValue.setTextColor(Color.WHITE);
 	                editNewFieldValue.setHint("Value");          
 	                editNewFieldValue.setEms(10);
 	                
 	                int idNameField = generateViewId();
 	                int idValueField = generateViewId();
 	                
 	                customFieldIDs.put(idNameField, idValueField);
 	                editNewFieldName.setId(idNameField);
 	                editNewFieldValue.setId(idValueField);
 	                
 	                // create a new row for label and edit text field
 	                TableRow.LayoutParams tparams = new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT);                    
 	                TableRow tr = new TableRow(EditContactFragment.this.getActivity());
 	                tr.setLayoutParams(tparams);
 	                
 	                tr.addView(editNewFieldName);
 	                tr.addView(editNewFieldValue);
 	                                    
 	                tl.addView(tr); 
 	            } catch(Exception e){
 	                Log.d("test", e.toString());
 	            }
 	                
 	            }
         });
 
 	}
 	
 	 /**
      * Generate a value suitable for use in {@link #setId(int)}.
      * This value will not collide with ID values generated at build time by aapt for R.id.
      *
      * @return a generated ID value
      */
     private static int generateViewId() {
         for (;;) {
             final int result = sNextGeneratedId.get();
             // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
             int newValue = result + 1;
             if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
             if (sNextGeneratedId.compareAndSet(result, newValue)) {
                 return result;
             }
         }
     }
 
 	@Override
 	public void onResume() {
 		// TODO Auto-generated method stub
 		super.onResume();
 		datasource.open();
 	}
 
 	@Override
 	public void onPause() {
 		// TODO Auto-generated method stub
 		super.onPause();
 		datasource.close();
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 
 		return (RelativeLayout) inflater.inflate(
 				R.layout.activity_add_new_contact, container, false);
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 
 		if (resultCode == Activity.RESULT_OK) {
 			Uri targetUri = data.getData();
 			// textTargetUri.setText(targetUri.toString());
 			Bitmap bitmap;
 			try {
 				bitmap = BitmapFactory.decodeStream(getActivity()
 						.getContentResolver().openInputStream(targetUri));
 				quickContactBadge.setImageBitmap(bitmap);
 			} catch (FileNotFoundException e) {
 				Log.e(TAG, e.getMessage());
 			}
 		}
 	}
 }
