 package co.lab4u.instruments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.app.SearchManager;
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.SearchView;
 import android.widget.TextView;
 import android.widget.Toast;
 import co.lab4u.instruments.adapters.LabItemAdapter;
 import co.lab4u.instruments.models.ILaboratory;
 import co.lab4u.instruments.proxies.ILabPlatformProxy;
 import co.lab4u.instruments.proxies.LabPlatformProxy;
 
 public class LabFinder extends ListActivity {
 	
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         Intent intent = getIntent();
         
         performIntentAction(intent);
     }
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 	    MenuInflater inflater = getMenuInflater();
 	    inflater.inflate(R.menu.lab_finder, menu);
 
 	    this.initSearchBar(menu);
 	    
 	    return true;
 	}
 	
 	private void initSearchBar(Menu menu) {
 	    // gets current searhManager
 	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
 	    
 	    // gets searchview from ##@@#|@1
 	    SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
 	    
 	    // set searchable info to searchview from search manager
 	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));		
 	}
 
 	private void performIntentAction(Intent intent) {
 
 		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
 			int idLab = this.geLabIdFrom(intent);
 			
 			if (idLab < 0) { 
 				this.showNotANumberErrorMessage();
 			    
 				return;
 			}
 			
 			// async call to web service
 			LaboratoryAsyncTask task = new LaboratoryAsyncTask(LabFinder.this);
 			task.execute(new Integer[]{ idLab } );	    
 		}
 	}
 
 	private void showNotANumberErrorMessage() {
 		Toast.makeText(this, R.string.errorNotANumber, Toast.LENGTH_LONG).show();
 	}
 
 	private int geLabIdFrom(Intent intent) {
 		try {
 			String query = intent.getStringExtra(SearchManager.QUERY);
 			return Integer.parseInt(query);
 		} catch (NumberFormatException e) {
 			return -1;
 		}
 	}
 
 	private class LaboratoryAsyncTask extends AsyncTask<Integer, Void, ILaboratory> {
 		ProgressDialog progressDialog;
 		ListActivity activity;
 		private Context context;
 		
 		public LaboratoryAsyncTask(ListActivity activity) {
 			this.activity = activity;
 			this.context = activity;
 			
 			progressDialog = new ProgressDialog(context);
 		}
 		
 		@Override
 		protected void onPreExecute() {
 			 this.progressDialog.setTitle(getResources().getString(R.string.lab_finder_progress_title));   
 			 this.progressDialog.setMessage(getResources().getString(R.string.lab_finder_progress_content));
 	            
 			 this.progressDialog.show();
 	    }
 		
 		@Override
 		protected ILaboratory doInBackground(Integer... args) {
 			int idLab = args[0];
 			
 			ILaboratory lab = getLaboratory(idLab);
 			
 	        return lab;
 		}
 
 		private ILaboratory getLaboratory(int idLab) {
 			ILabPlatformProxy proxy = new LabPlatformProxy();
 	        ILaboratory lab = proxy.getLaboratory(idLab);
 			return lab;
 		}
 		
 		@Override
 	    protected void onPostExecute(ILaboratory lab) {
 			progressDialog.dismiss();
 			
 			if (lab.isEmpty() == false) {
 				showLaboratoryOnScreen(lab);
 				Toast.makeText(getBaseContext(), getResources().getString(R.string.errorLabNotFound), Toast.LENGTH_LONG).show();
 			}
 			
 			super.onPostExecute(lab);
 	    }
 
 		private void showLaboratoryOnScreen(ILaboratory result) {
 			List<ILaboratory> labs = new ArrayList<ILaboratory>();
 			
 			labs.add(result);
 			
 			if (labs.isEmpty()) this.showLabNotFoundWarning();
 	        
 			this.setListLabItemAdapterOnScreen(labs);
 		}
 		
 		private void showLabNotFoundWarning() {
 			Toast.makeText(getApplicationContext(), R.string.warningNotANumber, Toast.LENGTH_LONG).show();
 		}
 		
 		private void setListLabItemAdapterOnScreen(List<ILaboratory> labs) {
 			ListAdapter adapter = new LabItemAdapter(getApplicationContext(), labs);
 		      
 		    // bind to adapter
 		    setListAdapter(adapter);
 		      
 		    ListView listView = getListView();
 		      
 		    listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
 		    
 		    listView.setOnItemClickListener(new OnItemClickListener() {
 		
 				@Override
 			    public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
 	
 					Intent intent = new Intent(LabFinder.this, LabViewer.class);
 				
 			   	    TextView titleView = (TextView)view.findViewById(R.id.labTitle);
 				    TextView contentView = (TextView)view.findViewById(R.id.labContent);
 				    TextView labCreationDate = (TextView)view.findViewById(R.id.labCreationDate);
 				
 				    // put values
 				    Bundle bundle = new Bundle();
 				    bundle.putString(Const.LAB_TITLE_KEY, titleView.getText().toString());
 				    bundle.putString(Const.LAB_CONTENT_KEY, contentView.getText().toString());
 				    bundle.putString(Const.LAB_CREATION_DATE_KEY, labCreationDate.getText().toString());
 				    
 				    intent.putExtra(Const.BUNDLE_GENERIC_KEY, bundle);
 				  
 				    startActivity(intent);
 				}
 		    });
 		}
 	}
 }
