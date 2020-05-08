  /*
   * Copyright  2012 Mufasa developer unit
   *
   * This file is part of Mufasa Budget.
   *
   *	Mufasa Budget is free software: you can redistribute it and/or modify
   * it under the terms of the GNU General Public License as published by
   * the Free Software Foundation, either version 3 of the License, or
   * (at your option) any later version.
   *
   * Mufasa Budget is distributed in the hope that it will be useful,
   * but WITHOUT ANY WARRANTY; without even the implied warranty of
   * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   * GNU General Public License for more details.
   *
   * You should have received a copy of the GNU General Public License
   * along with Mufasa Budget.  If not, see <http://www.gnu.org/licenses/>.
   */
 package it.chalmers.mufasa.android_budget_app.utilities;
 
 import it.chalmers.mufasa.android_budget_app.R;
 import it.chalmers.mufasa.android_budget_app.model.GraphViewModel;
 
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Locale;
 
 import org.taptwo.android.widget.TitleProvider;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ListView;
 
 
 public class GraphViewAdapter extends BaseAdapter implements TitleProvider {
 
 	private LayoutInflater inflater;
 	private GraphViewModel model;
 
 	public GraphViewAdapter(Context context, GraphViewModel model) {
 		this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
 		this.model = model;
 	}
 	
 	public int getCount() {
 		return 12;
 	}
 
 	public Object getItem(int position) {
 		return position;
 	}
 
 	public long getItemId(int position) {
 		return position;
 	}
 
 	public View getView(int position, View convertView, ViewGroup parent) {
 		if (convertView == null) {
 			
 			View view = inflater.inflate(R.layout.graph_list, null);
 			ListView listView = (ListView) view.findViewById(R.id.graphListView);
 			
 			listView.setAdapter(new GraphListAdapter(inflater.getContext(), model.getAccountBalanceListForGraph(12-position-1)));
 			
			return view;
 		}
 		return convertView;
 	}
 
 	public String getTitle(int position) {
 		Calendar calendar = new GregorianCalendar();
 		calendar.add(Calendar.MONTH, -12+position+1);
 		
 		return calendar.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US) + " " + calendar.get(Calendar.YEAR);
 	}
 
 }
