 package com.halcyonwaves.apps.meinemediathek.fragments;
 
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.ListFragment;
 import android.app.LoaderManager.LoaderCallbacks;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.content.Intent;
 import android.content.Loader;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ListView;
 
 import com.halcyonwaves.apps.meinemediathek.R;
 import com.halcyonwaves.apps.meinemediathek.SearchResultEntry;
 import com.halcyonwaves.apps.meinemediathek.activities.MovieOverviewActivity;
 import com.halcyonwaves.apps.meinemediathek.adapter.SearchResultsAdapter;
 import com.halcyonwaves.apps.meinemediathek.loaders.ZDFSearchResultsLoader;
 
 public class SearchResultsFragment extends ListFragment implements LoaderCallbacks< List< SearchResultEntry > >, OnClickListener {
 
 	private final static String TAG = "SearchResultsFragment";
 	private SearchResultsAdapter searchResultsAdapter = null;
 
 	@Override
 	public void onActivityCreated( final Bundle savedInstanceState ) {
 		super.onActivityCreated( savedInstanceState );
 
 		// initialize the adapter for fetching the data
 		this.searchResultsAdapter = new SearchResultsAdapter( this.getActivity() );
 		this.setListAdapter( this.searchResultsAdapter );
 
 		// start out with a progress indicator.
 		this.setListShown( false );
 
 		// prepare the loader. Either re-connect with an existing one, or start a new one.
 		this.getLoaderManager().initLoader( 0, this.getActivity().getIntent().getExtras(), this );
 	}
 
 	@Override
 	public void onClick( final DialogInterface dialog, final int which ) {
 		this.getActivity().finish();
 	}
 
 	@Override
 	public Loader< List< SearchResultEntry > > onCreateLoader( final int id, final Bundle args ) {
 
 		// get the supplied information from the intent which started this fragment
 		final String searchFor = this.getActivity().getIntent().getExtras().getString( "searchFor" );
 		Log.v( SearchResultsFragment.TAG, "The user is searching for: " + searchFor );
 
 		// return the requested loader
 		return new ZDFSearchResultsLoader( this.getActivity(), searchFor );
 	}
 
 	@Override
 	public void onListItemClick( final ListView l, final View v, final int position, final long id ) {
 		super.onListItemClick( l, v, position, id );
 
 		// get the item the user has selected
 		final SearchResultEntry selectedResults = (SearchResultEntry) this.getListAdapter().getItem( position );
 
 		// open the activity which shows the details about the selected entry
 		final Intent intent = new Intent( SearchResultsFragment.this.getActivity(), MovieOverviewActivity.class );
 		intent.putExtra( "title", selectedResults.title );
 		intent.putExtra( "description", selectedResults.description );
 		intent.putExtra( "downloadLink", selectedResults.downloadLink );
 		intent.putExtra( "previewImage", selectedResults.previewImage.getAbsolutePath() );
 		SearchResultsFragment.this.startActivity( intent );
 	}
 
 	@Override
 	public void onLoaderReset( final Loader< List< SearchResultEntry > > loader ) {
 		this.searchResultsAdapter.setData( null ); // clear the data in the adapter.
 	}
 
 	@Override
 	public void onLoadFinished( final Loader< List< SearchResultEntry > > loader, final List< SearchResultEntry > data ) {
 		// if an socket exception occurred, show a message
 		if( ((ZDFSearchResultsLoader) loader).socketTimeoutOccurred() ) {
 			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this.getActivity() );
 			alertDialogBuilder.setTitle( this.getString( R.string.dlg_title_timeout ) );
 			alertDialogBuilder.setMessage( this.getString( R.string.dlg_msg_timeout ) );
 			alertDialogBuilder.setPositiveButton( android.R.string.ok, this );
 			alertDialogBuilder.create().show();
 		}
 
		// if there were no results tell it to the user
		if( !((ZDFSearchResultsLoader) loader).socketTimeoutOccurred() && data.size() <= 0 ) {
 			final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder( this.getActivity() );
 			alertDialogBuilder.setTitle( this.getString( R.string.dlg_title_noresults ) );
 			alertDialogBuilder.setMessage( this.getString( R.string.dlg_msg_noresults ) );
 			alertDialogBuilder.setPositiveButton( android.R.string.ok, this );
 			alertDialogBuilder.create().show();
 		}
 
 		// set the new data in the adapter and show the list
 		this.searchResultsAdapter.setData( data );
 		if( this.isResumed() ) {
 			this.setListShown( true );
 		} else {
 			this.setListShownNoAnimation( true );
 		}
 	}
 }
