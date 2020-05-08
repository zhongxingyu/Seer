 package com.trugertech.quickbart;
 
 import java.util.ArrayList;
 
 import android.content.Context;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.ArrayAdapter;
 import android.widget.TextView;
 
 public class FavoriteTripLegAdapter extends ArrayAdapter<FavoriteTripLeg> {
 	
 	private ArrayList<FavoriteTripLeg> legs;
 	private Context mContext;
 	
 	public FavoriteTripLegAdapter(Context context, int textViewResourceID, 
 			ArrayList<FavoriteTripLeg> tripLegs){
 		super(context, textViewResourceID, tripLegs);
 		this.legs = tripLegs;
 		this.mContext = context;
 	}
 
 	@Override
 	public View getView(int position, View convertView, ViewGroup parent) {
 		View v = convertView;
 		if(v == null){
 			LayoutInflater vi = LayoutInflater.from(this.mContext);
 			v = vi.inflate(R.layout.favorite_details_item, null);
 		}
 		FavoriteTripLeg leg = legs.get(position);
 		if(leg != null){
			TextView transfers = (TextView) v.findViewById(R.id.detailsTransfers);
 			TextView platform = (TextView) v.findViewById(R.id.detailsPlatformInfo);
 			TextView departTxt = (TextView) v.findViewById(R.id.detailsDepartText);
 			TextView departInfo = (TextView) v.findViewById(R.id.detailsDepartInfo);
 			TextView destTxt = (TextView) v.findViewById(R.id.detailsDestText);
 			TextView destInfo = (TextView) v.findViewById(R.id.detailsDestInfo);
 			
 			//if there is a transfer code set notification
 			if(leg.getTransferCode().equals("")){
				transfers.setVisibility(View.GONE);
 			}
 			
 			platform.setText(leg.getTrainHeadStation());
 			
 			String text = leg.getOriginTime() + " from " + leg.getOrigin();
 			departInfo.setText(text);
 			
 			departTxt.setText("Depart: ");
 			
 			text =  leg.getDestinationTime() + " at " + leg.getDestination();
 			destInfo.setText(text);
 			
 			destTxt.setText("Arrive: ");
 			
 		}
 		return v;
 	}
 
 }
