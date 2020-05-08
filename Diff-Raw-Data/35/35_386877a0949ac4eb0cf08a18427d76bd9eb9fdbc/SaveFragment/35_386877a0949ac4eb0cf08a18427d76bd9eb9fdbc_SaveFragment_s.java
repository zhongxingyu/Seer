 package com.ecn.urbapp.fragments;
 
 import java.util.ArrayList;
 
import com.ecn.urbapp.R;
import com.ecn.urbapp.db.*;
import com.ecn.urbapp.activities.MainActivity;

 import android.app.Fragment;
import android.content.ContentValues;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.View;
import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 
 
 
 
 /**
  * @author	COHENDET S�bastien
  * 			DAVID Nicolas
  * 			GUILBART Gabriel
  * 			PALOMINOS Sylvain
  * 			PARTY Jules
  * 			RAMBEAU Merwan
  * 
  * SaveFragment class
  * 
  * This is the fragment used to save the project.
  * 			
  */
 
 
 public class SaveFragment extends Fragment{
 	
 
 	private Button saveToLocal = null;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View v = inflater.inflate(R.layout.layout_save, null);
 		
 		saveToLocal=(Button)v.findViewById(R.id.save_button_ync);
 		saveToLocal.setOnClickListener(OnClickSaveToLocal);
 		
 		return v;
 	}
 	
 	
     private OnClickListener OnClickSaveToLocal = new OnClickListener(){
     	public void onClick(View view){
     		
     		MainActivity.datasource.open();
     		saveGpsGeomListToLocal(MainActivity.gpsGeom);
     		savePixelGeomListToLocal(MainActivity.pixelGeom);
     		MainActivity.photo.saveToLocal(MainActivity.datasource);
     		saveProjectListToLocal(MainActivity.project);
     		saveComposedListToLocal(MainActivity.composed);
     		saveElementListToLocal(MainActivity.element); 		 		
     		MainActivity.datasource.close();
     		
     		
     	};
     };
 	
 	//TODO pour la photo il faut appeler direct la liste car on n'a pas de list de photo vu qu'ion bosse avecu ne seule.
 	
 	public void saveProjectListToLocal(ArrayList<Project> l1){
 		for (Project p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void saveElementListToLocal(ArrayList<Element> l1){
 		for (Element p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void saveGpsGeomListToLocal(ArrayList<GpsGeom> l1){
 		for (GpsGeom p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void savePixelGeomListToLocal(ArrayList<PixelGeom> l1){
 		for (PixelGeom p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void saveElementTypeListToLocal(ArrayList<ElementType> l1){
 		for (ElementType p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void saveMaterialListToLocal(ArrayList<Material> l1){
 		for (Material p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 	public void saveComposedListToLocal(ArrayList<Composed> l1){
 		for (Composed p : l1){
 			p.saveToLocal(MainActivity.datasource);
 		}
 	}
 	
 }
