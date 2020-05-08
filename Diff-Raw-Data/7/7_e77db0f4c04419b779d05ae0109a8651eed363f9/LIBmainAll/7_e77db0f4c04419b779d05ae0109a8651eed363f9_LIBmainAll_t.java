 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarp.library;
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.*;
 import java.util.List;
 import no.hials.muldvarp.R;
 import no.hials.muldvarp.courses.CoursePublicDetailActivity;
 import no.hials.muldvarp.entities.LibraryItem;
 
    
 /**
  *
  * @author Nospherus
  */
 public class LIBmainAll extends Fragment implements View.OnClickListener {
 
 private View mainV;
 private GridLibraryAdapter g;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setRetainInstance(true);
     }
     /**
      * Called when the activity is first created.
      */
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState){
         mainV = inflater.inflate(R.layout.library_all, container, false);
         return mainV;
     }
      
     public void itemsReady(List l) {
        
         g = new GridLibraryAdapter(getActivity(), R.layout.library_adapter_grid, R.id.courselisttext, l, false);
         GridView grid = (GridView)getView().findViewById(R.id.allGrid);
          grid.setAdapter(g);
          
          grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                 public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
                     Intent myIntent = new Intent(view.getContext(), LibraryDetail.class);
                     Bundle b = new Bundle();
                     b.putSerializable("item", (LibraryItem)g.getItem(position));
                     myIntent.putExtras(b);
                     startActivityForResult(myIntent, 0);
                 }  
             });
      }
      public void onClick(View view) {
             ((Button) view).setText("*");
             ((Button) view).setEnabled(false);
         }
 }
