 package com.dev.campus.directory;
 
 import java.util.List;
 
 import com.dev.campus.R;
 
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class DirectoryAdapter extends ArrayAdapter<Contact> {
 
 	private List<Contact> contacts;
 	private Activity mContext;
 
 	public DirectoryAdapter(Activity context, List<Contact> contacts) {
 		super(context, R.layout.directory_list_item, contacts);
 		mContext = context;
 		this.contacts = contacts;
 	}
 
 	private static class ContactHolder {
 		TextView name;
 		TextView tel;
 		TextView email;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View row = convertView;
 		ContactHolder contactHolder = null;
 
 		if (row == null) {
 			LayoutInflater inflater = mContext.getLayoutInflater();
 			row = inflater.inflate(R.layout.directory_list_item, parent, false);
 
 			contactHolder = new ContactHolder();
 			contactHolder.name = (TextView) row.findViewById(R.id.contact_name);
 			contactHolder.tel = (TextView) row.findViewById(R.id.contact_tel);
 			contactHolder.email = (TextView) row.findViewById(R.id.contact_email);
 			row.setTag(contactHolder);
 
 		} else {
 			contactHolder = (ContactHolder) row.getTag();
 		}
 
 		Contact contact = contacts.get(position);
 
 		contactHolder.name.setText(contact.getLastName().toUpperCase() + " " + contact.getFirstName());
 		contactHolder.tel.setText(contact.getTel());
 		contactHolder.email.setText(contact.getEmail());
 
 		return row;
 	}
 
 }
