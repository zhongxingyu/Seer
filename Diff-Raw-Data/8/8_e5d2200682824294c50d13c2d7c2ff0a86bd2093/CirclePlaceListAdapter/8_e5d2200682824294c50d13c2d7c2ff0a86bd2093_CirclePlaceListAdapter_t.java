 package com.barestodo.android.adapteur;
 
 import java.util.List;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.BaseAdapter;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 import com.barestodo.android.PlaceDescriptionActivity;
 import com.barestodo.android.R;
 import com.barestodo.android.place.Place;
 import com.barestodo.android.service.IPlaceRepository;
 import com.barestodo.android.service.RepositoryFactory;
 

 public class CirclePlaceListAdapter extends BaseAdapter {
 	IPlaceRepository placeRepository = RepositoryFactory.getPlaceRepository();
 	
     private static final String TAG = CirclePlaceListAdapter.class.getSimpleName();
     List<Place> listPlace ;
 
     public CirclePlaceListAdapter(Long circleId) {
         listPlace=placeRepository.getListPlace(circleId);
     }
 
     @Override
     public int getCount() {
         return listPlace.size();    // total number of elements in the list
     }
 
     @Override
     public Object getItem(int i) {
         return listPlace.get(i);
         // single item in the list
     }
 
     @Override
     public long getItemId(int i) {
         return i;                   // index number
     }
 
     @Override
     public View getView(int index, View view, final ViewGroup parent) {
 
         if (view == null) {
             LayoutInflater inflater = LayoutInflater.from(parent.getContext());
             view = inflater.inflate(R.layout.circle_place_list_adapter_layout, parent, false);
         }
 
         final Place place = listPlace.get(index);
 
         TextView textName = (TextView) view.findViewById(R.id.nameText);
         textName.setText(place.getName());
         
         TextView textLocation = (TextView) view.findViewById(R.id.locationText);
         textLocation.setText(place.getLocation());
 
         ImageButton button = (ImageButton) view.findViewById(R.id.detailImageButton);
         
         // button click listener
         // this chunk of code will run, if user click the button
         // because, we set the click listener on the button only
 
         button.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             	Intent intent = new Intent(parent.getContext(),	PlaceDescriptionActivity.class);
             	Bundle b = new Bundle();
 				b.putSerializable("placeToShow",place);
 				intent.putExtras(b); 
 				view.getContext().startActivity(intent);
                 //Toast.makeText(parent.getContext(), "button clicked: " + place.getName(), Toast.LENGTH_SHORT).show();
             }
         });
         return view;
     }
 }
 
