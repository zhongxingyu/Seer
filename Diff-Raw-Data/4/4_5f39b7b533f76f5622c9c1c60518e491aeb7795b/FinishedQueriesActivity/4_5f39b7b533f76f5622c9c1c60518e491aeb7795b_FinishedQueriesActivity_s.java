 package com.bioinformaticsapp;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 
 import com.bioinformaticsapp.models.BLASTQuery;
 import com.bioinformaticsapp.models.BLASTQuery.Status;
 import com.bioinformaticsapp.models.SearchParameter;
 import com.bioinformaticsapp.web.BLASTHitsDownloadingTask;
 
 public class FinishedQueriesActivity extends BLASTQueryListingActivity {
 
 	private static final int FINISHED_CURSOR_LOADER = 0x02;
 	
 	private BLASTQuery selected;
 	
 	private final static int REFRESH_MENU_ITEM = 0;
 	
 	
 	/** Called when the activity is first created. */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         
     	super.onCreate(savedInstanceState);
     
     	mStatus = Status.FINISHED;
     	
         getLoaderManager().initLoader(FINISHED_CURSOR_LOADER, null, this);
         
         setListAdapter(mQueryAdapter);
         
         registerForContextMenu(getListView());
         
     }
 	
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		
 		MenuItem item = menu.add(0, REFRESH_MENU_ITEM, 0, "Refresh");
 		
 		item.setIcon(android.R.drawable.ic_popup_sync);
 		
 		item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
 		
 		return true;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		
 		boolean itemSelectionHandled = false;
 		
 		int itemId = item.getItemId();
 		
 		switch(itemId){
 		case REFRESH_MENU_ITEM:
 			getLoaderManager().restartLoader(FINISHED_CURSOR_LOADER, null, this);
 			itemSelectionHandled = true;
 			break;
 			
 		default:
 			itemSelectionHandled = super.onOptionsItemSelected(item);
 			break;
 			
 		}
 		
 		return itemSelectionHandled;
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 		
 		MenuInflater menuInflater = getMenuInflater();
 		menu.setHeaderTitle("Select an option:");
 		menuInflater.inflate(R.menu.general_context_menu, menu);
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
 	 */
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		
 		boolean itemSelectionHandled = false;
 		
 		AdapterView.AdapterContextMenuInfo menuinfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		
 		int itemId = item.getItemId();
 		
 		switch(itemId){
 		case R.id.delete_menu_item: {
 
 			BLASTQuery selected = mQueryAdapter.getItem(menuinfo.position);
 			
 			doDeleteAction(selected.getPrimaryKey());
 			
 			itemSelectionHandled = true;
 		}
 		
 		break;
 		
 		case R.id.view_parameters_menu_item: {
 			
 			BLASTQuery selected = mQueryAdapter.getItem(menuinfo.position);
 			Intent viewParameters = new Intent(this, BLASTQuerySearchParametersActivity.class);
 			List<SearchParameter> parameters = parametersController.getParametersForQuery(selected.getPrimaryKey());
 			selected.updateAllParameters(parameters);
 			viewParameters.putExtra("query", selected);
 			startActivity(viewParameters);
 			itemSelectionHandled = true;
 		}
 		break;
 		default:
 			itemSelectionHandled = super.onContextItemSelected(item);
 			break;
 		}
 		
 		getLoaderManager().restartLoader(FINISHED_CURSOR_LOADER, null, this);
 		
 		return itemSelectionHandled;
 		
 	}
 
 	/* (non-Javadoc)
 	 * @see android.app.Activity#onResume()
 	 */
 	@Override
 	protected void onResume() {
 		super.onResume();
 		
 		getLoaderManager().restartLoader(FINISHED_CURSOR_LOADER, null, this);
 	}
 
 	/* Event handling when the user taps a row in the list item
 	 * 
 	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id) {
 		
 		super.onListItemClick(l, v, position, id);
 		
		selected = queryController.findBLASTQueryById(id);
		List<SearchParameter> parameters = parametersController.getParametersForQuery(id);
 		
 		selected.updateAllParameters(parameters);
 		
 		if(!fileExists(selected.getJobIdentifier()+".xml")){
 			BLASTHitsDownloader downloader = new BLASTHitsDownloader(this);
 			downloader.execute(selected);
 		}else{
 			Intent viewResults = new Intent(this, ViewBLASTHitsActivity.class);
 			viewResults.putExtra("query", selected);
 			startActivity(viewResults);
 		}
 	}
 
 	private boolean fileExists(String blastHitsFile){
 		boolean fileExists = false;
 		try {
 			openFileInput(blastHitsFile);
 			fileExists = true;
 		} catch (FileNotFoundException e) {
 			// No need to do anything as fileExists is set to false initially
 			// anyway
 		}
 		
 		return fileExists;
 	}
 	
 	private File getBLASTXMLFile(BLASTQuery selected){
 		
 		File blastXmlFile = getFileStreamPath(selected.getJobIdentifier()+".xml");
 		
 		return blastXmlFile;
 	}
 	
 	private void doDeleteAction(final long id){
 		
 		AlertDialog.Builder builder = new Builder(this);
 		builder = builder.setTitle("Deleting");
 		builder.setIcon(android.R.drawable.ic_dialog_alert);
 		builder = builder.setMessage(R.string.delete_query_message);
 		builder.setCancelable(false);
 		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int which) {
 				
 				BLASTQuery queryToDelete = queryController.findBLASTQueryById(id);
 				
 				final File blastXmlFile = getBLASTXMLFile(queryToDelete);
 				
 				if(blastXmlFile != null){
 					if(blastXmlFile.exists()){
 						blastXmlFile.delete();
 					}
 				}
 				
 				deleteQuery(id);
 				
 				getLoaderManager().restartLoader(FINISHED_CURSOR_LOADER, null, FinishedQueriesActivity.this);
 				
 			}
 		});
 		
 		builder.setNegativeButton("Cancel", null);
 		
 		Dialog dialog = builder.create();
 		dialog.show();
 		
 	}
 	
 	private class BLASTHitsDownloader extends BLASTHitsDownloadingTask {
 
 		private ProgressDialog mProgressDialog;
 		
 		public BLASTHitsDownloader(Context context) {
 			super(context);
 			mProgressDialog = new ProgressDialog(context, ProgressDialog.STYLE_SPINNER);
 		}
 
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
 		 */
 		@Override
 		protected void onPostExecute(String fileName) {
 			// TODO Auto-generated method stub
 			super.onPostExecute(fileName);
 			mProgressDialog.dismiss();
 			
 			if(fileName != null){
 				Intent viewResults = new Intent(FinishedQueriesActivity.this, ViewBLASTHitsActivity.class);
 				viewResults.putExtra("query", selected);
 				
 				startActivity(viewResults);
 				
 			}
 			
 		}
 
 		/* (non-Javadoc)
 		 * @see android.os.AsyncTask#onPreExecute()
 		 */
 		@Override
 		protected void onPreExecute() {
 			mProgressDialog.setTitle("Downloading BLAST Hits");
 			mProgressDialog.setMessage("Please wait...");
 			mProgressDialog.show();
 		}
 		
 	}
 
 }
