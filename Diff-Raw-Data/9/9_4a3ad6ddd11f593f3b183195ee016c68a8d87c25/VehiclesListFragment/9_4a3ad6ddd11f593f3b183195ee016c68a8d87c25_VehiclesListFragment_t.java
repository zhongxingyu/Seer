 package com.agilevent.repairshop;
 
 import android.os.Bundle;
 import android.support.v4.app.ListFragment;
 
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 public class VehiclesListFragment extends ListFragment {
 
 	private static final String CAR = "Car";
 	private static final String SUV = "SUV";
 	private static final String TRUCK = "Truck";
 	private VehicleSelected vehicleSelected;
 	
 	@Override
 	public void onActivityCreated(Bundle savedInstanceState) {
 		super.onActivityCreated(savedInstanceState);
 		
 		initializeAdapter(); 
 		
 	}
 	
 	@Override
 	public void onListItemClick(ListView l, View v, int position, long id) {
 		
 		// Get the vehicle that they selected (in this case its a string)
 		String vehicle = l.getItemAtPosition(position).toString(); 
 		
 		// Notify the listener (the damage waiver)
 		vehicleSelected.OnVehicleSelected(vehicle); 
 	}
 
 	private void initializeAdapter() {
 		String[] vehicles = new String[] { CAR, SUV, TRUCK};
 		
 		ArrayAdapter<String> vehiclesAdapter = new ArrayAdapter<String>( getActivity(), android.R.layout.simple_list_item_1, vehicles );
 		setListAdapter(vehiclesAdapter);
 		
 	}
 
 	public void reset() {
 		initializeAdapter(); 
 		vehicleSelected.OnVehicleSelected("");
 		Toast.makeText(getActivity(), getString(R.string.reset), Toast.LENGTH_SHORT).show(); 
 	}
 
	public void setVechicleListener(VehicleSelected vehicleSelected) {
 		this.vehicleSelected = vehicleSelected; 
 	}
 	
 
 }
