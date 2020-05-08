 package se.chalmers.watchme.ui;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import se.chalmers.watchme.R;
 import se.chalmers.watchme.activity.MovieDetailsActivity;
 import se.chalmers.watchme.database.MoviesTable;
 import se.chalmers.watchme.database.WatchMeContentProvider;
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.utils.DateConverter;
 import android.annotation.TargetApi;
 import android.app.AlertDialog;
 import android.app.LoaderManager;
 import android.content.CursorLoader;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.Loader;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.SimpleCursorAdapter;
 import android.widget.SimpleCursorAdapter.ViewBinder;
 import android.widget.TextView;
 import android.widget.Toast;
 
 // TODO Important! API level required does not match with what is used
 @TargetApi(11)
 public class MovieListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
 	
 	SimpleCursorAdapter adapter;
 	private Uri uri = WatchMeContentProvider.CONTENT_URI_MOVIES;
 
 	
 	@Override
 	public void onActivityCreated(Bundle b) {
 		super.onActivityCreated(b);
 		Thread.currentThread().setContextClassLoader(getActivity().getClassLoader());
 
 		String[] from = new String[] { MoviesTable.COLUMN_MOVIE_ID, MoviesTable.COLUMN_TITLE,  MoviesTable.COLUMN_RATING , MoviesTable.COLUMN_DATE };
 		int[] to = new int[] { 0 , R.id.title, R.id.raiting, R.id.date };
 		
 		getActivity().getLoaderManager().initLoader(0, null, this);
 		adapter = new SimpleCursorAdapter(getActivity(), R.layout.list_item_movie , null, from, to, 0);
 		
 		/**
 		 * Convert date text from millis to dd-mm-yyyy format
 		 */
 		//TODO: Refactor?
 		adapter.setViewBinder(new ViewBinder() {
 			
 			public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
 				if (columnIndex == 3) {
 					String date = cursor.getString(columnIndex);
 					TextView textView = (TextView) view;
 					Calendar cal = Calendar.getInstance();
 					cal.setTimeInMillis(Long.parseLong(date));
 					String formattedDate = DateConverter.toSimpleDate(cal);
 					
 					textView.setText(formattedDate);
 					return true;
 				}
 				
 				return false;
 			}
 		});
 		
 		setListAdapter(adapter);
 	    
         this.getListView().setOnItemClickListener(new OnDetailsListener());
 	    this.getListView().setOnItemLongClickListener(new OnDeleteListener());
 	}
 	
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
 			Bundle savedInstanceState) {
 		return inflater.inflate(R.layout.movie_list_fragment_view, container, false);
 	}
 	
 	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
 		String[] projection = { MoviesTable.COLUMN_MOVIE_ID, MoviesTable.COLUMN_TITLE, MoviesTable.COLUMN_RATING, MoviesTable.COLUMN_DATE };
 	    CursorLoader cursorLoader = new CursorLoader(getActivity(),
 	        uri, projection, null, null, null);
 	    return cursorLoader;
 	}
 
 	public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
 		adapter.swapCursor(arg1);		
 	}
 
 	public void onLoaderReset(Loader<Cursor> arg0) {
 		// data is not available anymore, delete reference
 	    adapter.swapCursor(null);
 		
 	}
 	
 	/**
      * Listener for when the user clicks an item in the list
      * 
      * The movie object in the list is used to fill a new activity with data
      * 
      * @author Robin
      */
     private class OnDetailsListener implements OnItemClickListener {
 
 		public void onItemClick(AdapterView<?> parent, View view, int position,
 				long id) {
 			
 			final long movieId = id;
 			
 			Cursor movieCursor = getActivity().getContentResolver().query(uri, null,
 					"_id = " + movieId, null, null);
 			
 			if (movieCursor != null) {
 		        movieCursor.moveToFirst();
 			}
 			
 			final Movie movie = new Movie(movieCursor.getString(1));
 			movie.setId(movieId);
 			movie.setRating(movieCursor.getInt(2));
 			movie.setNote(movieCursor.getString(3));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(Long.parseLong(movieCursor.getString(4)));
			movie.setDate(c);
			movie.setImdbID(movieCursor.getString(5));
 			
 			//final Movie movie = (Movie) getListView().getItemAtPosition(arg2);
 			Intent intent = new Intent(getActivity(), MovieDetailsActivity.class);
 			
 			// TODO Fetch all data from database in DetailsActivity instead?
 			intent.putExtra("movie", movie);
 			
 			startActivity(intent);
 			
 		}
     	
     	
     }
 	
 	 /**
      * The listener for when the user does a long-tap on an item in the list.
      * 
      * The Movie object in the list is removed if the user confirms that he wants to remove the Movie.
      * 
      * @author Johan
      */
     public class OnDeleteListener implements OnItemLongClickListener {
     	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
 
 			final long movieId = id;
 			
 			String[] projection = { MoviesTable.COLUMN_TITLE };
 			Cursor movieCursor = getActivity().getContentResolver().query(uri, projection, "_id = " + movieId, null, null);
 			
 			if (movieCursor != null) {
 		        movieCursor.moveToFirst();
 			}
 			
 			final String movieTitle = movieCursor.getString(0);
 			
             AlertDialog.Builder alertbox = new AlertDialog.Builder(getActivity());
             alertbox.setMessage("Are you sure you want to delete the movie \"" + movieTitle + "\"?");           
             alertbox.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface arg0, int arg1) {
                 	getActivity().getContentResolver().delete(uri, "_id = " + movieId , null);
                     Toast.makeText(getActivity().getApplicationContext(), "\"" + movieTitle + "\" was deleted" , Toast.LENGTH_SHORT).show();
                 }
             });
             alertbox.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                 public void onClick(DialogInterface arg0, int arg1) {
                     
                 }
             });
             
             alertbox.show();
 			return true;
 		}    	
 	}
 
 	
 	
 
 }
