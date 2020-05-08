 /**
 	This file is part of Personal Trainer.
 
     Personal Trainer is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     any later version.
 
     Personal Trainer is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
 
     (C) Copyright 2012: Daniel Kvist, Henrik Hugo, Gustaf Werlinder, Patrik Thitusson, Markus Schutzer
  */
 package se.team05.adapter;
 
 import java.sql.Timestamp;
 import java.text.SimpleDateFormat;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import se.team05.data.DBResultAdapter;
 import android.content.Context;
 import android.database.Cursor;
 import android.view.View;
 import android.widget.SimpleCursorAdapter;
 import android.widget.TextView;
 
 /**
  * This adapter is used to bind the views in the result list to their
  * appropriate column in the Cursor. It sets a new custom ViewBinder to do the
  * actual binding.
  * 
  * @author Henrik Hugo, Daniel Kvist
  * 
  */
 public class ListResultsAdapter extends SimpleCursorAdapter
 {
 
 	/**
	 * Constructor which calls the super class to instantiate the adapter and calls the view binder to set a custom one.
 	 * 
 	 * @param context
 	 *            The context to operate in
 	 * @param layout
 	 *            The layout file to use in the list
 	 * @param c
 	 *            The cursor which is pointing to the reults
 	 * @param from
 	 *            The text column(s) you wish to display
 	 * @param to
 	 *            The views which to display the columns in
 	 * @param flags
 	 *            any addition flags that can be found in SimpleCursorAdapter
 	 */
 	public ListResultsAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags)
 	{
 		super(context, layout, c, from, to, flags);
		setViewBinder(new ResultsViewBinder());
 	}
 
 	/**
 	 * Sets the custom view binder
 	 */
 	@Override
 	public void setViewBinder(ViewBinder viewBinder)
 	{
		super.setViewBinder(viewBinder);
 	}
 
 	/**
 	 * This is the custom ViewBinder which does the actual mapping work of the
 	 * fields to the views.
 	 */
 	private class ResultsViewBinder implements ViewBinder
 	{
 		// Hook method for formatting the timestamp retreived from the database
 		// in the cursor into human readable date and time.
 		@Override
 		public boolean setViewValue(View view, Cursor cursor, int columnIndex)
 		{
 			int timestamp_index = cursor.getColumnIndex(DBResultAdapter.COLUMN_TIMESTAMP);
 
 			if (timestamp_index == columnIndex)
 			{
 				// Retrieve timestamp
 				String createDate = cursor.getString(timestamp_index);
 				TextView textView = (TextView) view;
 
 				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);
 				sdf.setTimeZone(TimeZone.getDefault());
 
 				String result = sdf.format(new Timestamp(
 						Long.parseLong( 	// To long
 						createDate.trim() 	// Trim excess of string
 						) * 1000 			// Convert to milliseconds instead of seconds
 					)
 				);
 
 				// Update textview
 				textView.setText(result);
 				return true;
 			}
 
 			return false;
 		}
 	}
 }
