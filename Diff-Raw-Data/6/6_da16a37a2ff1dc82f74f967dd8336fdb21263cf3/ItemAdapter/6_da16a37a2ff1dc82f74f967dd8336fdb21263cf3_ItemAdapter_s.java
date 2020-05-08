 package edu.pitt.cs1635group3;
 
 import java.util.ArrayList;
 
 import zebrafish.util.DBHelper;
 
 import android.content.Context;
 import android.graphics.Paint;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.RelativeLayout.LayoutParams;
 import android.widget.TextView;
 
 public class ItemAdapter extends ArrayAdapter<Item> {
 	// This pulls in items and inflates them appropriately for the layout.
 
 	private final ArrayList<Item> items;
 	private Button assign_button;
 	private Button complete_button;
 	private Button invite_button;
 	private boolean inviteUp;
 	private int checkedItems = 0;
 	private int checkedCompletedItems = 0;
 
 	private static final String TAG = "ItemAdapter";
 	private DBHelper db;
 	protected Context context;
 
 	public ItemAdapter(Context c, int textViewResourceId,
 			ArrayList<Item> items, Button assign_button,
 			Button complete_button, Button invite_button, boolean inviteUp) {
 		super(c, textViewResourceId, items);
 		context = c;
 		this.items = items;
 		this.assign_button = assign_button;
 		this.complete_button = complete_button;
 		this.invite_button = invite_button;
 		this.inviteUp = inviteUp;
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) {
 		db = new DBHelper(getContext());
 		View v = convertView;
 
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
 					Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(R.layout.item_row, null);
 		}
 
 		Item o = items.get(position);
 		CheckBox cb;
 
 		if (o != null) {
 			TextView name = (TextView) v.findViewById(R.id.item_name);
 			TextView assignee = (TextView) v.findViewById(R.id.item_assignee);
 			cb = (CheckBox) v.findViewById(R.id.check);
 			Button b = (Button) v.findViewById(R.id.delete_item_button);
 			ImageView img = (ImageView) v.findViewById(R.id.chat_bubble);
 			
 			String comments = o.getNotes();
 			if (comments != null)
 			{
 				if (!comments.equals(""))
 				{
 					img.setVisibility(View.VISIBLE);
					LayoutParams params = (RelativeLayout.LayoutParams)name.getLayoutParams();
					params.addRule(RelativeLayout.LEFT_OF, R.id.chat_bubble);
					name.setLayoutParams(params);
 				}
 			}
 			if (!o.isSelected())
 				cb.setChecked(false); // This solves the jumping problem
 										// [facepalm]
 
 			
 			
 			if (name != null) {
 				name.setText(o.getName());
 
 				if (o.isCompleted()) {
 					name.setPaintFlags(name.getPaintFlags()
 							| Paint.STRIKE_THRU_TEXT_FLAG);
 				}
 
 			}
 
 			if (assignee != null) {
 
 				int userID = o.getAssignee();
 				db.open();
 				String assignment = (userID > 0 ? db.getUserByID(userID)
 						.getName() : "Unassigned");
 				db.close();
 				assignee.setText(assignment);
 
 			}
 
 			cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
 
 				public void onCheckedChanged(CompoundButton buttonView,
 						boolean isChecked) {
 					db.open();
 					Item activeItem = items.get(position);
 
 					if (isChecked) {
 						checkedItems++;
 					} else {
 						checkedItems--;
 						if (!activeItem.isCompleted())
 							checkedCompletedItems--; // ultimately we want to
 														// change the button to
 														// say "mark incomplete"
 						// if the items selected are all already completed
 					}
 					if (checkedItems == 0) {
 						invite_button.setVisibility(View.VISIBLE);
 						complete_button.setVisibility(View.GONE);
 						assign_button.setVisibility(View.GONE);
 					} else if (checkedItems > 0) {
 						invite_button.setVisibility(View.GONE);
 						complete_button.setVisibility(View.VISIBLE);
 						assign_button.setVisibility(View.VISIBLE);
 					}
 
 					activeItem.setSelected(isChecked);
 					db.updateItem(activeItem); // the item needs to be marked as
 												// selected in the db so
 												// InsideListActivity can
 												// identify it as needing to be
 												// acted upon for assign or
 												// selected
 					db.close();
 				}
 			}); // end onCheckedChangeListener
 
 			/*
 			 * String note = o.getNotes(); if (snippit != null && note != null)
 			 * { snippit.setVisibility(View.VISIBLE); snippit.setText(note); }
 			 */// TODO - in item_row.xml, use this field for note snippit
 
 		}
 		return v;
 	}
 
 	public void flipButtons() {
 
 		if (inviteUp) {
 			invite_button.setVisibility(View.GONE);
 			complete_button.setVisibility(View.VISIBLE);
 			assign_button.setVisibility(View.VISIBLE);
 			inviteUp = false;
 		} else {
 			invite_button.setVisibility(View.VISIBLE);
 			complete_button.setVisibility(View.GONE);
 			assign_button.setVisibility(View.GONE);
 
 			// assign_button.setVisibility(View.VISIBLE); // remove this for
 			// final
 			// product (testing now)
 			inviteUp = true;
 		}
 	}
 
 }
