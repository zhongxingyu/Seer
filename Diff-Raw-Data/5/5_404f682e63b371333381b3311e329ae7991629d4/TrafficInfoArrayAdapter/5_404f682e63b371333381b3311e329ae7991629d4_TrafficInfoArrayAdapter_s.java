 /*
  * Copyright 2010, 2011 Andrew De Quincey -  adq@lidskialf.net
  * This file is part of rEdBus.
  *
  *  rEdBus is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  rEdBus is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with rEdBus.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.redbus.ui.trafficinfo;
 
 import java.util.List;
 
 import org.redbus.R;
 import org.redbus.trafficnews.NewsItem;
 
 import android.content.Context;
 import android.text.format.DateFormat;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 
 public class TrafficInfoArrayAdapter extends ArrayAdapter<NewsItem> {
 	private List<NewsItem> items;
 	private int textViewResourceId;
 	private Context ctx;
 	
 	public TrafficInfoArrayAdapter(Context context, int textViewResourceId, List<NewsItem> items) {
 		super(context, textViewResourceId, items);
 
 		this.ctx = context;
 		this.textViewResourceId = textViewResourceId;
 		this.items = items;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 		if (v == null) {
 			LayoutInflater vi = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 			v = vi.inflate(textViewResourceId, null);
 		}
 
 		NewsItem newsItem = items.get(position);
 		if (newsItem == null)
 			return v;
 		
 		TextView dateView = (TextView) v.findViewById(R.id.trafficinfo_date);
 		TextView locationView = (TextView) v.findViewById(R.id.trafficinfo_location);
 		TextView descriptionView = (TextView) v.findViewById(R.id.trafficinfo_description);
 
 		dateView.setText(DateFormat.format("MMM dd kk:mm", newsItem.date));
 		if (newsItem.location != null)
 			locationView.setText("@" + newsItem.location);
 		if (newsItem.description != null) {
 			char firstChar = Character.toUpperCase(newsItem.description.charAt(0));
 			descriptionView.setText("" + firstChar + newsItem.description.substring(1));
 		}
 		
 		return v;
 	}
 }
