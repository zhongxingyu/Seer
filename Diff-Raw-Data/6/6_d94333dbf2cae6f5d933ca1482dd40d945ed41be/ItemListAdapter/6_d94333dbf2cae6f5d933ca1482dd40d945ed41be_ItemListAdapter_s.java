 /**
  * Copyright (C) 2011 Matthias Jordan <matthias.jordan@googlemail.com>
  *
  * This file is part of piRSS.
  *
  * piRSS is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * piRSS is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with piRSS.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.codefu.android.rss.itemlist;
 
 import java.text.DateFormat;
 import java.util.Date;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.graphics.drawable.ColorDrawable;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.ImageView.ScaleType;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 import de.codefu.android.rss.R;
 import de.codefu.android.rss.db.ItemProvider;
 import de.codefu.android.rss.updateservice.Utils;
 
 
 
 /**
  * Adapter that adapts an item Cursor to the item list view.
  * 
  * @author mj
  */
 public class ItemListAdapter extends SimpleCursorAdapter {
 
     /**
      * Reference to the date format that is used in the current context.
      */
     private DateFormat datef = null;
     /**
      * Reference to the time format that is used in the current context.
      */
     private DateFormat timef = null;
 
 
     public ItemListAdapter(Context context, Cursor c) {
         super(context, R.layout.itemlist_row, c, new String[] {
             ItemProvider.ITEMS_COL_HEADLINE
         }, new int[] {
             R.id.itemlist_row_headline
         });
         datef = android.text.format.DateFormat.getDateFormat(context.getApplicationContext());
         timef = android.text.format.DateFormat.getTimeFormat(context.getApplicationContext());
     }
 
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         final LayoutInflater inflater = LayoutInflater.from(context);
         final View view = inflater.inflate(R.layout.itemlist_row, null);
         return view;
     }
 
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         final boolean itemRead = cursor.getInt(cursor.getColumnIndex(ItemProvider.ITEMS_COL_READ)) == 1;
         final TextView headlineView = (TextView) view.findViewById(R.id.itemlist_row_headline);
         final String headline = cursor.getString(cursor.getColumnIndex(ItemProvider.ITEMS_COL_HEADLINE));
         if (headline != null) {
             headlineView.setMaxLines(2);
             final int end = Math.min(100, headline.length());
             final String headlineShort = headline.substring(0, end);
             headlineView.setText(headlineShort);
             if (itemRead) {
                 headlineView.setTextAppearance(context, R.style.Base_ItemHeadline_Read);
             }
             else {
                 headlineView.setTextAppearance(context, R.style.Base_ItemHeadline_Unread);
             }
         }
         else {
             headlineView.setHeight(0);
         }
 
         final TextView contentView = (TextView) view.findViewById(R.id.itemlist_row_content);
         final String content = cursor.getString(cursor.getColumnIndex(ItemProvider.ITEMS_COL_CONTENT));
         if (content != null) {
             contentView.setMaxLines(2);
            contentView.setText(Utils.htmlClean(content));
             contentView.setTextAppearance(context, R.style.Base_ItemBody);
         }
         else {
             contentView.setHeight(0);
         }
 
         final TextView dateView = (TextView) view.findViewById(R.id.itemlist_row_date);
         final long dateLong = cursor.getLong(cursor.getColumnIndex(ItemProvider.ITEMS_COL_DATE));
         if (dateLong != 0L) {
             final Date d = new Date(dateLong);
             final String fd = datef.format(d) + " - " + timef.format(d);
             dateView.setMaxLines(1);
             dateView.setText(fd);
             dateView.setTextAppearance(context, R.style.Base_ItemDate);
         }
         else {
             dateView.setHeight(0);
         }
 
         final int keeper = cursor.getInt(cursor.getColumnIndex(ItemProvider.ITEMS_COL_KEEPER));
         if (keeper == 0) {
             view.setBackgroundDrawable(null);
         }
         else {
             view.setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(R.color.Keeper)));
         }
 
         final ImageView statusView = (ImageView) view.findViewById(R.id.itemlist_row_status);
         if (!itemRead) {
             statusView.setVisibility(ImageView.VISIBLE);
             statusView.setImageResource(R.drawable.unread);
             statusView.setScaleType(ScaleType.FIT_XY);
         }
         else {
             statusView.setVisibility(ImageView.INVISIBLE);
         }
 
     }
 
 }
