 /**
 *	AutoCompleteAdapter.java
 *
 *	Custom Array adapter for auto complete dropdowns.
 *
 *	Includes custom view drawing with objects of the type Movie instead
 *	of plain Strings.	
 *
 *	@author Johan Brook
 *	@copyright (c) 2012 Johan Brook
 *	@license MIT
 */
 
 package se.chalmers.watchme.activity;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.net.MovieSource;
 import se.chalmers.watchme.utils.DateTimeUtils;
 import se.chalmers.watchme.utils.MovieHelper;
 
 import android.content.Context;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Filter;
 import android.widget.Filterable;
 import android.widget.TextView;
 
 public class AutoCompleteAdapter extends ArrayAdapter<JSONObject> implements Filterable {
 
 	private LayoutInflater inflater;
 	private MovieSource source;
 	private JSONArray movies;
 	
 	/**
 	 * Create a new AutoCompleteAdapter.
 	 * 
 	 * <p>Custom filtering of movie suggestions from IMDb.</p>
 	 * 
 	 * @param context The context
 	 * @param textViewResourceId The view ID
 	 * @param source The data source to fetch movies from
 	 */
 	public AutoCompleteAdapter(Context context, int textViewResourceId, MovieSource source) {
 		super(context, textViewResourceId);
 		
 		this.source = source;
 		this.inflater = LayoutInflater.from(getContext());
 	}
 	
 	@Override
 	public int getCount() {
 		return this.movies.length();
 	}
 	
 	@Override
 	public JSONObject getItem(int position) {
 		return this.movies.optJSONObject(position);
 	}
 	
 	@Override
 	public Filter getFilter() {
 		
 		return new Filter() {
 
 			/*
 			 * Override this method in order to populate the auto complete field
 			 * with the actual movie name when selecting a suggestion from the list,
 			 * instead of the default toString implementation.
 			 * 
 			 * @see android.widget.Filter#convertResultToString(java.lang.Object)
 			 */
 			@Override
 			public CharSequence convertResultToString(Object result) {
 				if(result instanceof JSONObject) {
 					return ((JSONObject) result).optString(Movie.JSON_KEY_NAME);
 				}
 				
 				return super.convertResultToString(result);
 			}
 			
 			/*
 			 * The actual filtering, where the call to the API is made. The method
 			 * is called in a worker thread apart from the UI thread, so we're okay.
 			 * 
 			 * @see android.widget.Filter#performFiltering(java.lang.CharSequence)
 			 */
 			@Override
 			protected FilterResults performFiltering(CharSequence constraint) {
 				
 				FilterResults results = new FilterResults();
 				if(constraint != null) {
 					JSONArray json = source.getMoviesByTitle(constraint.toString());
 					
 					results.values = json;
 					results.count = json.length();
 				}
 				
 				return results;
 			}
 
 			/*
 			 * Update the list.
 			 * 
 			 * @see android.widget.Filter#publishResults(java.lang.CharSequence, android.widget.Filter.FilterResults)
 			 */
 			@Override
 			protected void publishResults(CharSequence constraint, FilterResults results) {
 				if(results != null && results.count > 0) {
 					movies = (JSONArray) results.values;
 					notifyDataSetChanged();
 				}
 				else {
 					notifyDataSetInvalidated();
 				}
 			}
 			
 		};
 	}
 	
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		
 		/**
 		 * Performance tweaks in overriden getView:
 		 * 
 		 * Re-use the "trash view" convertView instead of always inflating
 		 * a new view from XML. Only inflate if convertView is null.
 		 * 
 		 * Also make use of a ViewHolder to cache references to subviews. A 
 		 * reference to a ViewHolder is created if a new view is inflated,
 		 * otherwise we just use the attached ViewHolder of the old view.
 		 */
 		
 		ViewHolder holder;
 		JSONObject suggestion = this.getItem(position);
 		
 		if(convertView == null) {
 			convertView = this.inflater.inflate(R.layout.auto_complete_item, null);
 			holder = new ViewHolder();
 			holder.title = (TextView) convertView.findViewById(R.id.autocomplete_title);
 			convertView.setTag(holder);
 		}
 		else {
 			holder = (ViewHolder) convertView.getTag();
 		}
 		
 		if(suggestion != null) {
 			String releaseDate = suggestion.optString("released");
 			releaseDate = (releaseDate.isEmpty()) ? null : releaseDate;
 			String year = DateTimeUtils.parseYearFromDate(releaseDate);
 			
 			holder.title.setText(suggestion.optString(Movie.JSON_KEY_NAME) + 
 					( (year != null) ? " ("+ year +")" : "" ) );
 		}
 		
 		return convertView;
 	}
 	
 	/**
 	 * A ViewHolder with a title, year and position fields. 
 	 * 
 	 * @author Johan
 	 */
 	static class ViewHolder {
		TextView title;
		int position;
 	}
 
 }
