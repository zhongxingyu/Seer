 package com.cs2340.spacetrader;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class PlanetListAdapter extends ArrayAdapter<Planet> 
 {
 	Context context;
 	int layoutResourceId;
 	Planet data[] = null;
 
 	public PlanetListAdapter(Context context, int layoutResourceId,
 			Planet[] data) {
 		super(context, layoutResourceId, data);
 		this.layoutResourceId = layoutResourceId;
 		this.context = context;
 		this.data = data;
 	}
 
 	@Override
 	public View getView(final int position, View convertView, ViewGroup parent) 
 	{
 		View row = convertView;
 		PlanetHolder holder = null;
 
 		if (row == null) 
 		{
 			LayoutInflater inflater = ((Activity) context).getLayoutInflater();
 			row = inflater.inflate(layoutResourceId, parent, false);
 
 			holder = new PlanetHolder();
 			holder.button = (Button) row
 					.findViewById(R.id.button_planet_select);
 
 			row.setTag(holder);
 		} 
 		else 
 		{
 			holder = (PlanetHolder) row.getTag();
 		}
 
 		String buttonText = String.format
 		(
 			"Go to %s at %d, %d",
 			data[position].getName(), 
 			data[position].getCoordinate()[0],
 			data[position].getCoordinate()[1]
 		);
 
 		holder.button.setText(buttonText);
 		holder.button.setOnClickListener(new Button.OnClickListener()
 		{
 			public void onClick(View view) 
 			{
 				GameSetup.thePlayer.getship().moveToPlanet(data[position]);
 				if (GameSetup.thePlayer.hasContract){
 					if (GameSetup.thePlayer.getContract().checkContract()){
 						Toast.makeText(context, "Contract Completed. You recieved " + GameSetup.thePlayer.getContract().getReward() + " credits in payment.", Toast.LENGTH_LONG).show();
 					}
 				}
 				//Intent intent = new Intent(context, Space.class);
             	//context.startActivity(intent);
 			}
 		});
 		
 		//if cost is greater than fuel available, disable travel
 		if (GameSetup.thePlayer.getship().fuelCost(data[position]) > GameSetup.thePlayer.getship().getFuel())
 		{
 			holder.button.setEnabled(false);
 		}
 
 		return row;
 	}
 	
 	static class PlanetHolder 
 	{
 		Button button;
 	}
 }
