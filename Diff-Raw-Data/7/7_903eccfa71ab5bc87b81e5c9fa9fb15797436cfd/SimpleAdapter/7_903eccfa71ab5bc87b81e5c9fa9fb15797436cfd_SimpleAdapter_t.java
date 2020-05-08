 package com.siu.android.andutils.adapter;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 
import java.util.List;

 /**
  * @author Lukasz Piliszczuk <lukasz.pili AT gmail.com>
  */
 public abstract class SimpleAdapter<T_Item, T_ViewHolder extends SimpleViewHolder> extends ArrayAdapter<T_Item> {
 
     protected int rowLayoutId;
 
    public SimpleAdapter(Context context, int rowLayoutId, List<T_Item> items) {
        super(context, rowLayoutId, items);
         this.rowLayoutId = rowLayoutId;
     }
 
     @Override
     public View getView(int position, View row, ViewGroup parent) {
         if (null == row) {
             row = LayoutInflater.from(getContext()).inflate(rowLayoutId, parent, false);
         }
 
         T_ViewHolder viewHolder = (T_ViewHolder) row.getTag();
 
         if (null == viewHolder) {
             viewHolder = createViewHolder();
             viewHolder.setRow(row);
             row.setTag(viewHolder);
         }
 
         T_Item item = getItem(position);
         configure(viewHolder, item);
 
         return row;
     }
 
     protected abstract void configure(T_ViewHolder viewHolder, T_Item item);
 
     protected abstract T_ViewHolder createViewHolder();
 }
