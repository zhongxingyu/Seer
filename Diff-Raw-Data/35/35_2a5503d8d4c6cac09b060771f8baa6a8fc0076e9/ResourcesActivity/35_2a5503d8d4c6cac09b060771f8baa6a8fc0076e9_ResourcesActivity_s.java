 package spaceappschallenge.moonville.activities;
 
 import java.util.ArrayList;
 
 import spaceappschallenge.moonville.GameActivity;
 import spaceappschallenge.moonville.R;
 import spaceappschallenge.moonville.businessmodels.Resource;
 import spaceappschallenge.moonville.factories.Resources;
 import spaceappschallenge.moonville.listadapters.ResourceListAdapter;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 
 public class ResourcesActivity extends GameActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_resources);
 
 		ArrayList<Resource> resources = Resources.getInstance()
 				.getAllResources();
 		ResourceListAdapter resourceListAdapter = new ResourceListAdapter();
 		ListView resourceListView = (ListView) this
 				.findViewById(R.id.resourceslist);
 		resourceListView.setAdapter(resourceListAdapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_resources, menu);
 		return true;
 	}
 
 	// methods called by onClick property of button in xml
 	public void showBaseOverviewScreen(View view) {
 		view.getContext().startActivity(
 				new Intent(this, BaseOverviewActivity.class));
 	}
 
	public void showBuildingScreen(View view) {
		view.getContext().startActivity(
				new Intent(this, BuildingsActivity.class));
	}

 	
 
 	public void showImportResourcesScreen(View view) {
 		Log.i("ResourcesActivity", "showing import resources screen..");
 		view.getContext().startActivity(
 				new Intent(this, ImportResourcesActivity.class));
 	}
 
 	public void showExportResourcesScreen(View view) {
 		// Not implemented yet
 		view.getContext().startActivity(
 				new Intent(this, ResourcesActivity.class));}
 	
 	public void showResourcesScreen( View view )
 	{
 		//we are already here
 		//view.getContext().startActivity( new Intent( this, ResourcesActivity.class ) );
 	}
 
 	
 	public void showExportScreen( View view )
 	{
 		view.getContext().startActivity( new Intent( this, ResourcesActivity.class ) );
 	}
 
 }
