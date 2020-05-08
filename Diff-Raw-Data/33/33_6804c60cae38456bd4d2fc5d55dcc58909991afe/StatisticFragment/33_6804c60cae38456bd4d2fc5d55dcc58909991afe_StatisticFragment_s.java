 package gogo.gadgeto.car;
 
 import gogo.gadgeto.model.Database;

import java.util.Iterator;
import java.util.Set;

import android.app.Fragment;
 import android.app.ListFragment;
 import android.content.Intent;
 import android.os.Bundle;
import android.view.LayoutInflater;
 import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
import android.widget.Button;
 import android.widget.ListView;
 
 public class StatisticFragment extends ListFragment{
 	
	private Button cashDeptsButton;
 	private Database database;
	private ListView myListView;
 
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
