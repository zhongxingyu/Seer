 package cmput301f13t10.presenter;
 
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 
 import android.util.Log;
 import cmput301f13t10.model.AdventureCache;
 import cmput301f13t10.model.AdventureModel;
 import cmput301f13t10.model.Callback;
 import cmput301f13t10.model.DatabaseInteractor;
 import cmput301f13t10.model.FileInteractor;
 import cmput301f13t10.model.InvalidSearchTypeException;
 import cmput301f13t10.view.UpdatableView;
 
 public class LibraryPresenter
 {
 	private LibraryModel mLibraryModel;
 	private UpdatableView mView;
 	
 	public LibraryPresenter( UpdatableView view )
 	{
 		mLibraryModel = new LibraryModel();
 		mView = view;
 	}
 
 	public void loadData()
 	{
 		mLibraryModel.loadData();
 	}
 
 	public void saveData(FileOutputStream fileOutputStream)
 	{
 		mLibraryModel.saveData( fileOutputStream );
 	}
 
 	public void updateAdventures()
 	{
 		mLibraryModel.updateAdventures();
 	}

 	public void populateList()
 	{
 		Callback getAdventureCallback = new Callback()
 		{
 			@Override
 			public void callBack( Object adventureList )
 			{
 				try
 				{
 					mLibraryModel.setAdventureList( (ArrayList<AdventureModel>) adventureList );
 					mView.updateView();
 				}
 				catch( ClassCastException e )
 				{
 					Logger.log( "bad!", e );
 				}
 			}
 		};
 		mLibraryModel.getAdventureList().clear();
 		DatabaseInteractor.getDatabaseInteractor().getAllAdventures( getAdventureCallback );
 		
 	}
 
 	public ArrayList<AdventureModel> getAdventures()
 	{
 		return mLibraryModel.getAdventureList();
 	}
 	
 	public void sortLibraryUsing( String searchText )
 	{
 		mLibraryModel.sortLibraryUsing( searchText );
 	}
 
 	public void deleteAdventure( int localId )
 	{		
 		mLibraryModel.deleteAdventure( localId );
 	}
 
 	
 	
 }
