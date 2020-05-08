 package com.github.alexesprit.noisefmtor.adapter;
 
 import android.content.Context;
 import android.database.Cursor;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.CursorAdapter;
 import android.widget.TextView;
 import com.github.alexesprit.noisefmtor.R;
 import com.github.alexesprit.noisefmtor.database.FavouritesDatabase;
 
 public final class FavouritesAdapter extends CursorAdapter {
     private LayoutInflater inflater;
 
     public FavouritesAdapter(Context context, Cursor cursor) {
         super(context, cursor, true);
         this.inflater = LayoutInflater.from(context);
     }
 
     @Override
     public View newView(Context context, Cursor cursor, ViewGroup parent) {
         View v = inflater.inflate(R.layout.track_view, parent, false);
         bindView(v, context, cursor);
         return v;
     }
 
     @Override
     public void bindView(View view, Context context, Cursor cursor) {
         TextView titleView = (TextView)view.findViewById(R.id.track_view_title);
         TextView artistView = (TextView)view.findViewById(R.id.track_view_artist);
        artistView.setText(cursor.getString(cursor.getColumnIndex(FavouritesDatabase.COLUMN_TITLE)));
        titleView.setText(cursor.getString(cursor.getColumnIndex(FavouritesDatabase.COLUMN_ARTIST)));
     }
 }
