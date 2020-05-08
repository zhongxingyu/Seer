 package gogo.gadgeto.car;
 
 import gogo.gadgeto.model.Database;
 import android.app.ListFragment;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 public class StatisticFragment extends ListFragment{
 	
 	private Database database;
 
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		database = Database.getInstance();
 		String[] fuelEntries = database.getFuelEntries(database.getUsername());
 		
         setListAdapter(new ArrayAdapter<String>(getActivity(),
                 android.R.layout.simple_list_item_1, fuelEntries));
 	}
 	
 	@Override
     public void onListItemClick(ListView l, View v, int position, long id) {
 		Intent newIntent = new Intent(getActivity().getApplicationContext(), DeptActivity.class);
 		startActivity(newIntent);	
     }
 }
