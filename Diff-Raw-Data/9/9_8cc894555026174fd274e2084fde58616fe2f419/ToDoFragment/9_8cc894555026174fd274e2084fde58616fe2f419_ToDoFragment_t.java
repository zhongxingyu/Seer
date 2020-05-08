 package com.jasonllinux.fragment;
 
 import com.jasonllinux.pa.R;
 
 import android.app.ListFragment;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 public class ToDoFragment extends ListFragment{
 	
     String[] presidents = {
             "Dwight D. Eisenhower",
             "John F. Kennedy",
             "Lyndon B. Johnson",
             "Richard Nixon",
             "Gerald Ford",
             "Jimmy Carter",
             "Ronald Reagan",
             "George H. W. Bush",
             "Bill Clinton",
             "George W. Bush",
             "Barack Obama"
         };
 	
     
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
        setListAdapter(new ArrayAdapter<String>(
					        		getActivity(),
					                android.R.layout.simple_expandable_list_item_1,
					                presidents)
                					);
     }
 
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
 //        return inflater.inflate(R.layout.fragment_todo, container, false);
     	return inflater.inflate(R.layout.listview_todo, container, false);
     }
 
  
     
     public void onListItemClick(ListView parent, View v, int position, long id) {          
         Toast.makeText(getActivity(), 
             "You have selected " + presidents[position], 
             Toast.LENGTH_SHORT).show();
         } 
     
     @Override
     public void onStop()
     {
         super.onStop();
     }
 
 }
