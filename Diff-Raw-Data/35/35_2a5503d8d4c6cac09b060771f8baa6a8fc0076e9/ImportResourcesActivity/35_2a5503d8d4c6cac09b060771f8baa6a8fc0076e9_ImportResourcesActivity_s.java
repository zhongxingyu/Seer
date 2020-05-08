 package spaceappschallenge.moonville.activities;
 
 import java.util.ArrayList;
 
 import spaceappschallenge.moonville.GameActivity;
 import spaceappschallenge.moonville.R;
 import spaceappschallenge.moonville.businessmodels.Resource;
 import spaceappschallenge.moonville.factories.Resources;
 import spaceappschallenge.moonville.listadapters.ImportResourceListAdapter;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ListView;
 
 public class ImportResourcesActivity extends GameActivity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_import_resources);
 		Log.i("ImportResourcesActivity","gathering resources..");
 		ArrayList<Resource> resources = Resources.getInstance().getAllResources();
 		ImportResourceListAdapter resourceListAdapter = new ImportResourceListAdapter();
 		Log.i("ResourcesActivity","showing import resources screen..");
 		ListView resourceListView = (ListView) this
 				.findViewById(R.id.importResourceslist);
 		resourceListView.setAdapter(resourceListAdapter);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_import_resources, menu);
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
 
 	public void showResourcesScreen( View view )
 	{
 		view.getContext().startActivity( new Intent( this, ResourcesActivity.class ) );
 	}
 
 	public void showExportScreen(View view) {
 		view.getContext().startActivity(
 				new Intent(this, ImportResourcesActivity.class));
 	}
 
 }
