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
 
 import java.io.FileNotFoundException;
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.app.DialogFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.view.MenuItemCompat;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SearchView;
 import cmput301f13t10.model.AdventureCache;
 import cmput301f13t10.model.AdventureModel;
 import cmput301f13t10.model.Callback;
 import cmput301f13t10.model.DatabaseInteractor;
 import cmput301f13t10.model.FileInteractor;
 import cmput301f13t10.presenter.AppConstants;
 import cmput301f13t10.presenter.LibraryPresenter;
 import cmput301f13t10.presenter.Logger;
 import cs.ualberta.cmput301f13t10.R;
 
 /**
  * This is an activity that displays a list of adventures to the user. They can
  * select an adventure to edit, which will launch the adventureEditView.
  * 
  * @author Aly-Khan Jamal
  * @author Braeden Soetaert
  */
 public class LibraryEditView extends Activity implements Serializable, UpdatableView, SearchView.OnQueryTextListener, DeletePromptDialogFragment.DeleteSectionDialogListener
 {
 
 
 	private LibraryPresenter mPresenter;
 	
 	/**
 	 * The list view that will display all of the adventures
 	 */
 	private ListView adventureListView;
 
 	/**
 	 * The adventure that was selected by the user.
 	 */
 	private int AdventureId;
 
 	/**
 	 * The create adventure button.
 	 */
 	private Button btnCreateAdventure;
 	
 	/**
 	 * The item that gets searched
 	 */
 	private MenuItem mSearchItem;
 
 	@Override
 	protected void onCreate( Bundle savedInstanceState )
 	{
 
 		mPresenter = new LibraryPresenter(this);
 		
 		super.onCreate( savedInstanceState );
 
 		setContentView( R.layout.library_edit );
 
 		mPresenter.populateList();
 		updateView();
 		addListenerOnButton();
 
 		adventureListView.setOnItemClickListener( new OnItemClickListener()
 		{
 			public void onItemClick( AdapterView<?> parentAdapter, View view, int position, long id )
 			{
 				AdventureId = ( (AdventureModel) parentAdapter.getItemAtPosition( position ) ).getLocalId();
 				startAdventureEditViewId();
 			}
 		} );
 
 	}
 
 	/**
 	 * Starts the adventureEditView, using the adventureId previously declaired.
 	 */
 	private void startAdventureEditViewId()
 	{
 		Intent intent = new Intent( this, AdventureEditView.class );
 		intent.putExtra( AppConstants.ADVENTURE_ID, AdventureId );
 		startActivity( intent );
 	}
 
 	@Override
 	protected void onResume()
 	{
 		super.onResume();
 		mPresenter.loadData();
 		mPresenter.populateList();
 		updateView();
 	}
 
 	@Override
 	public void onPause()
 	{
 		super.onPause();
 		try
 		{
 			mPresenter.saveData( openFileOutput( AppConstants.FILE_NAME, 0 ) );
 		}
 		catch( FileNotFoundException e )
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 
 	@Override
 	public void updateView()
 	{
 		mPresenter.updateAdventures();
 
 		adventureListView = (ListView) findViewById( R.id.adventure_edit_list );
 		adventureListView.setAdapter( new AdventureArrayAdapter( this, mPresenter.getAdventures() ) );
 	}
 
 	/**
 	 * Add the onCreateAdventure button
 	 */
 	public void addListenerOnButton()
 	{
 
 		btnCreateAdventure = (Button) findViewById( R.id.create_adventure_button );
 
 		btnCreateAdventure.setOnClickListener( new OnClickListener()
 		{
 
 			@Override
 			public void onClick( View v )
 			{
 
 				startAdventureEditView();
 			}
 		} );
 	}
 
 	/**
 	 * Start the AdventureEditView with a blank new adventure.
 	 */
 	private void startAdventureEditView()
 	{
 		Intent intent = new Intent( this, AdventureEditView.class );
 		startActivity( intent );
 	}
 
 	@Override
 	public boolean onQueryTextChange( String searchText )
 	{
 		mPresenter.sortLibraryUsing( searchText );
		mPresenter.populateList();
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
 		mSearchItem = menu.findItem( R.id.action_search );
 
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
 	public void onUploadClick( View v )
 	{
 
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
 
 
 	public void deletePrompt( View view )
 	{
 		Bundle choicesBundle = new Bundle();
 		AdventureModel adventure = (AdventureModel) view.getTag();
 		choicesBundle.putString( AppConstants.TITLE, adventure.getTitle() );
 		choicesBundle.putInt( AppConstants.ADVENTURE_ID, adventure.getLocalId() );
 		DeletePromptDialogFragment dialog = new DeletePromptDialogFragment();
 		dialog.setArguments( choicesBundle );
 		dialog.show( getFragmentManager(), "" );
 	}
 
 	@Override
 	public void onDeleteConfirm( DialogFragment dialog )
 	{
 		mPresenter.deleteAdventure( dialog.getArguments().getInt( AppConstants.ADVENTURE_ID ) );
 		updateView();
 	}
 
 	@Override
 	public void onDeleteCancel( DialogFragment dialog )
 	{
 		// Do nothing
 	}
 
 }
