 package com.quanleimu.adapter;
 
 import java.util.ArrayList;
 import java.util.List;
 import com.quanleimu.activity.R;
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.TextView;
 import android.widget.CheckBox;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter;
 import com.quanleimu.adapter.BXAlphabetSortableAdapter.BXPinyinSortItem;
 
 public class CheckableAdapter extends BXAlphabetSortableAdapter {
 	public static class CheckableItem extends Object{
 		public String txt;
 		public boolean checked;
 		public String id;
 		@Override
 		public String toString(){
 			return txt;
 		}
 	}
 //	private Context context;
 //	private List<? extends CheckableItem> list = new ArrayList<CheckableItem>();
 	private Object tag;
 	private int checkedResourceId = -1;//R.drawable.pic_radio_normal_2x;
 	private int uncheckedResourceId = -1;//R.drawable.pic_radio_selected_2x;
 	
 	private int left = -1, right = -1, top = -1, bottom = -1;
 	
 	public void setPadding(int left, int right, int top, int bottom){
 		this.left = left;
 		this.right = right;
 		this.top = top;
 		this.bottom = bottom;
 	}
 
 	public void setTag(Object obj){
 		tag = obj;
 	}
 	
 	public Object getTag(){
 		return tag;
 	}
 	
 	public void setItemCheckStatus(int position, boolean check){
 		CheckableItem item = getItem(position) instanceof BXPinyinSortItem ? 
 				(CheckableItem)((BXPinyinSortItem)getItem(position)).obj
 				: (CheckableItem)getItem(position);
 		item.checked = check;
 		this.list.remove(position);
 		this.list.add(position, item);
 
 	}
 	
 
 	public CheckableAdapter(Context context,List<? extends CheckableItem> list, int sortIfMoreThan) {
 		super(context, list, list.size() > sortIfMoreThan);
 //		this.context = context;
 //		this.list = list;
 	}
 		
 	public void setList(List<? extends CheckableItem> list_){
 		this.list.addAll(list_);
 		this.notifyDataSetChanged();
 	}
 	
 	public List<? extends Object> getList(){
 		return this.list;
 	}
 
 
 	@Override
 	public int getCount() {
 		// TODO Auto-generated method stub
 		return list.size();
 	}
 
 	@Override
 	public Object getItem(int arg0) {
 		// TODO Auto-generated method stub
 		return list.get(arg0); 
 	} 
 
 	@Override
 	public long getItemId(int position) {
 		// TODO Auto-generated method stub
 		return position; 
 	}
 	
 	public void setIconId(int checkedId, int uncheckedId){
 		this.checkedResourceId = checkedId;
 		this.uncheckedResourceId = uncheckedId;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View header = getHeaderIfItIs(position, convertView);
 		if(header != null){
 			return header;
 		}
 		LayoutInflater inflater = LayoutInflater.from(context);
 		View v = null;
 		if(convertView == null || convertView.findViewById(R.id.checkitem) == null)
 		{
 			v = inflater.inflate(R.layout.item_text_checkbox, null);
 		}else{
 			v = (View)convertView; 
 		}
 		
 		if(left >= 0 && right >= 0 && top >= 0 && bottom >= 0){
 			v.setPadding(left, top, right, bottom);
 		}
 		
 		TextView tvCateName = (TextView)v.findViewById(R.id.checktext);
 		tvCateName.setText(list.get(position).toString());
 		
 		CheckBox box = (CheckBox)v.findViewById(R.id.checkitem);
 		if(this.getItem(position) instanceof BXHeader){
 			box.setVisibility(View.GONE);
 			v.setBackgroundResource(R.drawable.alphabetheaderbk);
 		}
 		else{
 			box.setVisibility(View.VISIBLE);
 			CheckableItem item = (list.get(position) instanceof BXPinyinSortItem) ? 
 					(CheckableItem)((BXPinyinSortItem)list.get(position)).obj : (CheckableItem)list.get(position);
 			box.setChecked(item.checked);
 			if(this.checkedResourceId > 0 && this.uncheckedResourceId > 0){
 				if(item.checked){
 					box.setButtonDrawable(this.checkedResourceId);
 				}
 				else{
 					box.setButtonDrawable(this.uncheckedResourceId);
 				}
 			}
 		}
 		return v;
 	}
 
 }
