 /*
  Copyright (c) 2013, Brendan Cowan, Tyler Meen, Steven Gerdes, Braeden Soetaert, Aly-khan Jamal
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met: 
 
  1. Redistributions of source code must retain the above copyright notice, this
  list of conditions and the following disclaimer. 
  2. Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution. 
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
  The views and conclusions contained in the software and documentation are those
  of the authors and should not be interpreted as representing official policies, 
  either expressed or implied, of the FreeBSD Project.
  */
 package cmput301f13t10.view;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.MenuItemCompat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SearchView;
 import cmput301f13t10.model.AdventureCache;
 import cmput301f13t10.model.AdventureModel;
 import cmput301f13t10.model.Callback;
 import cmput301f13t10.model.DatabaseInteractor;
 import cmput301f13t10.model.InvalidSearchTypeException;
 import cmput301f13t10.presenter.AppConstants;
 import cmput301f13t10.presenter.Logger;
 import cmput301f13t10.presenter.Searcher;
 import cs.ualberta.cmput301f13t10.R;
 
 /**
  * This is an activity that displays a list of adventures to the user. They can
  * select an adventure to view, which will launch the SectionReadView.
  * 
  * @author Aly-Khan Jamal
  * @author Braeden Soetaert
  */
 public class LibraryView extends Activity implements Serializable, SearchView.OnQueryTextListener
 {
 	/**
 	 * The adventures to display
 	 */
 	ArrayList<AdventureModel> adventure;
 
 	/**
 	 * The cache from which to grab the adventures
 	 */
 	AdventureCache cache;
 
 	/**
 	 * The list view that will display all of the adventures
 	 */
 	private ListView adventureListView;
 
 	/**
 	 * The adventure that was selected by the user.
 	 */
 	int AdventureId;
 	private MenuItem mSearchItem;
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 
 		cache = AdventureCache.getAdventureCache();
 
 		adventure = new ArrayList<AdventureModel>();
 
 		super.onCreate( savedInstanceState );
 
 		setContentView( R.layout.library_view );
 
 		populateList();
 
 		Button feelingLuckyButton = (Button) findViewById( R.id.random_choice_button );
 		feelingLuckyButton.setOnClickListener( new OnClickListener()
 		{
 
 			@Override
 			public void onClick( View arg0 )
 			{
 				try
 				{
 					Random rand = new Random();
					int choiceNumber = rand.nextInt( adventure.size() );
					AdventureId = adventure.get( choiceNumber ).getLocalId();
 					startSectionReadView();
 				}
 				catch( IllegalArgumentException e )
 				{
 					// If there are no items in the list do nothing
 				}
 			}
 
 		} );
 		adventureListView.setOnItemClickListener( new AdapterView.OnItemClickListener()
 		{
 
 			public void onItemClick( AdapterView<?> parentAdapter, View view, int position, long id )
 			{
 				AdventureId = ( (AdventureModel) parentAdapter.getItemAtPosition( position ) ).getLocalId();
 				startSectionReadView();
 			}
 		} );
 	}
 
 	/**
 	 * Start the section read view with the start section of the start adventure
 	 */
 	private void startSectionReadView()
 	{
 		Intent intent = new Intent( this, SectionReadView.class );
 		Bundle b = new Bundle();
 		b.putSerializable( AppConstants.CURRENT_ADVENTURE, AdventureId );
 		intent.putExtra( AppConstants.CURRENT_ADVENTURE, b );
 		startActivity( intent );
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		populateList();
 	}
 
 	/**
 	 * Start the section read view with the start section of the start adventure
 	 */
 	private void populateList()
 	{
 		Callback getAdventureCallback = new Callback()
 		{
 
 			@Override
 			public void callBack( Object adventureList )
 			{
 				try
 				{
 					adventure = (ArrayList<AdventureModel>) adventureList;
 					updateList();
 				}
 				catch( ClassCastException e )
 				{
 					Logger.log( "bad!", e );
 				}
 			}
 
 		};
 		DatabaseInteractor.getDatabaseInteractor().getAllAdventures( getAdventureCallback );
 		updateList();
 
 	}
 
 	private void updateList()
 	{
 		// we should always see local adventures
 		for( AdventureModel adv : AdventureCache.getAdventureCache().getAllAdventures() )
 		{
 			if( !libContains( adv ) )
 				adventure.add( adv );
 		}
 
 		adventureListView = (ListView) findViewById( R.id.adventure_read_list );
 		ArrayAdapter<AdventureModel> adapter = new AdventureArrayAdapter( this, adventure );
 		adventureListView.setAdapter( adapter );
 	}
 
 	private boolean libContains( AdventureModel adv )
 	{
 		for( AdventureModel thisAdv : adventure )
 		{
 			if( thisAdv.getLocalId() == adv.getLocalId() )
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onQueryTextChange( String searchText )
 	{
 		try
 		{
 			adventure = Searcher.searchBy( adventure, searchText, Searcher.sTITLE );
 		}
 		catch( InvalidSearchTypeException e )
 		{
 			Log.v( "Library Search Error", Searcher.sTITLE + " not a valid search type" );
 			adventure = cache.getAllAdventures();
 		}
 		populateList();
 		return true;
 	}
 
 	@Override
 	public boolean onQueryTextSubmit( String arg0 )
 	{
 		if( mSearchItem != null )
 		{
 			mSearchItem.collapseActionView();
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu( Menu menu )
 	{
 		getMenuInflater().inflate( R.menu.library_view, menu );
 
 		MenuItem helpMenuItem = menu.add( "Help" );
 
 		helpMenuItem.setShowAsAction( MenuItem.SHOW_AS_ACTION_NEVER );
 		helpMenuItem.setOnMenuItemClickListener( new OnMenuItemClickListener()
 		{
 
 			@Override
 			public boolean onMenuItemClick( MenuItem arg0 )
 			{
 				help();
 				return true;
 			}
 
 		} );
 
 		mSearchItem = menu.findItem( R.id.action_search );
 		final SearchView searchView = (SearchView) MenuItemCompat.getActionView( mSearchItem );
 		searchView.setOnQueryTextListener( this );
 		searchView.setOnQueryTextFocusChangeListener( new View.OnFocusChangeListener()
 		{
 			@Override
 			public void onFocusChange( View view, boolean queryTextFocused )
 			{
 				if( !queryTextFocused )
 				{
 					mSearchItem.collapseActionView();
 					searchView.setQuery( "", false );
 				}
 			}
 		} );
 		return super.onCreateOptionsMenu( menu );
 	}
 
 	/**
 	 * Starts up the help view on help button click.
 	 * 
 	 * @param view
 	 *            the view that was clicked
 	 */
 	public void help()
 	{
 		Intent intent = new Intent( this, HelpView.class );
 		startActivity( intent );
 	}
 
 }
