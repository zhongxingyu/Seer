 package com.cookcook.main.socialfragment;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.cookcook.main.R;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Paint;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ShoppingListAdapter extends ArrayAdapter<Model>{
 	
 	private final Activity context;
 	private  ArrayList<Model> list;
 	
 	static class ViewHolder {
 	    public TextView text;
 	    public CheckBox check;
 	}
 	public ShoppingListAdapter(Activity context, ArrayList<Model> list) {
 		// TODO Auto-generated constructor stub
 		super(context, R.layout.shopping_list_item, list);
 		this.context = context;
 		this.list = list;
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		// TODO Auto-generated method stub
 //		return super.getView(position, convertView, parent);
 		View rowView=convertView;
 		if(rowView == null)
 		{
 			LayoutInflater inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			rowView=inflater.inflate(R.layout.shopping_list_item, null);
 			final ViewHolder viewHolder = new ViewHolder();
 			viewHolder.text = (TextView) rowView.findViewById(R.id.text_view_shopping_list);
 			viewHolder.check = (CheckBox) rowView.findViewById(R.id.check_box_shopping_list);
 			viewHolder.check.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
 				
 				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
 					// TODO Auto-generated method stub
 					Model element = (Model)viewHolder.check.getTag();
 					element.setSelected(buttonView.isChecked());
					//Make strike through on text if checkbox is selected
					if (buttonView.isChecked())
					{
						viewHolder.text.setPaintFlags(viewHolder.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
					}
					else
					{
						viewHolder.text.setPaintFlags(viewHolder.text.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
					}
 				}
 			});
 			rowView.setTag(viewHolder);
 			viewHolder.check.setTag(list.get(position));
 		}
 		else
 		{
 			((ViewHolder)rowView.getTag() ).check.setTag(list.get(position));
 		}
 		rowView.setOnClickListener(new View.OnClickListener() {
 			
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				ViewHolder viewHold=(ViewHolder)v.getTag();
 				Model element = (Model)viewHold.check.getTag();
 				element.setSelected(!((Model)viewHold.check.getTag()).getSelected());
 				viewHold.check.setChecked(element.getSelected());
 //				Toast.makeText(context, "You choose:"+((Model)viewHold.check.getTag()).getName()+";isChecked:"+((Model)viewHold.check.getTag()).getSelected(), Toast.LENGTH_SHORT).show();
 				
 			}
 		});
 		ViewHolder viewHolder=(ViewHolder)rowView.getTag();
 		viewHolder.text.setText(list.get(position).getName());
 		viewHolder.check.setChecked(list.get(position).isSelected());
 		
 		//Make strike through on text if checkbox is selected
 		if (list.get(position).isSelected())
 		{
 			viewHolder.text.setPaintFlags(viewHolder.text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
 		}
 		else
 		{
 			viewHolder.text.setPaintFlags(viewHolder.text.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));
 		}
 		return rowView;
 	}
 	
 }
