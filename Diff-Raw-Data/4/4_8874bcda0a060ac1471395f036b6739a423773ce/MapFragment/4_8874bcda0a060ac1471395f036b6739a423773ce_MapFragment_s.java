 package cz.kalcik.vojta.terraingis.fragments;
 
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.Toast;
 
 import com.vividsolutions.jts.geom.Coordinate;
 
 import cz.kalcik.vojta.terraingis.exception.CreateObjectException;
 import cz.kalcik.vojta.terraingis.io.SpatiaLiteManager;
 import cz.kalcik.vojta.terraingis.layer.AbstractLayer;
 import cz.kalcik.vojta.terraingis.layer.LayerManager;
 import cz.kalcik.vojta.terraingis.layer.VectorLayer;
 import cz.kalcik.vojta.terraingis.layer.AbstractLayer.AbstractLayerData;
 import cz.kalcik.vojta.terraingis.layer.VectorLayer.VectorLayerType;
 import cz.kalcik.vojta.terraingis.location.AutoRecordService;
 import cz.kalcik.vojta.terraingis.location.AutoRecordServiceConnection;
 import cz.kalcik.vojta.terraingis.view.MapView;
 import cz.kalcik.vojta.terraingis.MainActivity;
 import cz.kalcik.vojta.terraingis.R;
 
 /**
  * @author jules
  * fragment with MapView
  */
 public class MapFragment extends Fragment
 {
     // constants ==========================================================
     private static String MAP_VIEW_DATA = "cz.kalcik.vojta.terraingis.MapViewData";
     private static String LAYERS_DATA = "cz.kalcik.vojta.terraingis.LayersData";
     private static String MAP_FRAGMENT_DATA = "cz.kalcik.vojta.terraingis.MapFragmentData";
     
     // properties =========================================================
     public static class MapFragmentData implements Serializable
     {
         private static final long serialVersionUID = 1L;
         public boolean isRunAutoRecord;
         public String autoRecordLayerString;
 
         public MapFragmentData(boolean isRunAutoRecord)
         {
             this.isRunAutoRecord = isRunAutoRecord;
         }
     }
     
     private MapView map;
     private LayerManager mLayerManager = LayerManager.getInstance();
     private MainActivity mMainActivity;
     private Button mButtonRecordAuto;
     private Button mButtonRecordEndObject;
     private Button mButtonRecordPoint;
     private VectorLayer mAutoRecordLayer = null;
     private Intent mServiceIntent;
     private AutoRecordServiceConnection mServiceConnection = new AutoRecordServiceConnection(this);
     private MapFragmentData data = new MapFragmentData(false);
 
     // public methods =====================================================
     /**
      * set visibility of record buttons
      */
     public void changeRecordButtons()
     {
         boolean showObjectButton = false;
         boolean showPointButton = false;
         boolean showAutoButton = false;
         
         if(mMainActivity.isRecordMode())
         {
             AbstractLayer selectedLayer = mMainActivity.getLayersFragment().getSelectedLayer();
             // is selected layer
             if(selectedLayer != null)
             {
                 if(selectedLayer instanceof VectorLayer)
                 {
                     VectorLayer selectedVectorLayer = (VectorLayer)selectedLayer;
                     VectorLayerType type = selectedVectorLayer.getType();
                     // point button
                     showPointButton = true;
     
                     // object button
                     if(type == VectorLayerType.LINE || type == VectorLayerType.POLYGON)
                     {
                         showAutoButton = true;
                         if (selectedVectorLayer.haveOpenedRecordObject())
                         {
                             showObjectButton = true;
                         }
                     }
                 }
             }
             
             //run automatic recording
             if(data.isRunAutoRecord)
             {
                 showAutoButton = true;
             }
         }
         
         if(showObjectButton)
         {
             mButtonRecordEndObject.setVisibility(View.VISIBLE);
         }
         else
         {
             mButtonRecordEndObject.setVisibility(View.GONE);
         }
         
         if(showPointButton)
         {
             mButtonRecordPoint.setVisibility(View.VISIBLE);
         }
         else
         {
             mButtonRecordPoint.setVisibility(View.GONE);
         }
         
         if(showAutoButton)
         {
             mButtonRecordAuto.setVisibility(View.VISIBLE);
             if(data.isRunAutoRecord)
             {
                 mButtonRecordAuto.setText(R.string.record_auto_stop);
             }
             else
             {
                 mButtonRecordAuto.setText(R.string.record_auto_start);
             }
         }
         else
         {
             mButtonRecordAuto.setVisibility(View.GONE);
         }
     }
     
     /**
      * add recorded points
      * @param points
      */
     public void recordPointsAuto(ArrayList<Coordinate> points)
     {
         if(mAutoRecordLayer != null)
         {
             mAutoRecordLayer.addPoints(points, SpatiaLiteManager.EPSG_LONLAT);
         }
         
         changeRecordButtons();
         map.invalidate();
     }
 
     /**
      * record one point
      * @param point
      */
     public void recordPointAuto(Coordinate point)
     {
         if(mAutoRecordLayer != null)
         {
             recordPoint(point, mAutoRecordLayer, SpatiaLiteManager.EPSG_LONLAT);
         }
     }
     
     // getter, setter =====================================================
     
     public MapView getMap()
     {
         return map;
     }
     
     // on methods =========================================================
     @Override
     public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
     {
         // Inflate the layout for this fragment
         View myView = inflater.inflate(R.layout.map_layout, container, false);
         
         // record buttons
         mButtonRecordAuto = (Button)myView.findViewById(R.id.button_record_auto);
         mButtonRecordAuto.setOnClickListener(autoRecordObjectHandler);
         mButtonRecordEndObject = (Button)myView.findViewById(R.id.button_record_end_object);
         mButtonRecordEndObject.setOnClickListener(endObjectHandler);
         mButtonRecordPoint = (Button)myView.findViewById(R.id.button_record_point);
         mButtonRecordPoint.setOnClickListener(addPointHandler);
         
         // Map view state
         map = (MapView) myView.findViewById(R.id.map);
         mLayerManager.loadLayers((Context)mMainActivity, map);
         
         return myView;
     }
     
     @Override
     public void onActivityCreated(Bundle savedInstanceState)
     {       
         super.onActivityCreated(savedInstanceState);
 
         if(savedInstanceState != null)
         {
             // map view
             map.setData(savedInstanceState.getSerializable(MAP_VIEW_DATA));
             // layers data
             mLayerManager.setData((ArrayList<AbstractLayerData>)savedInstanceState.getSerializable(LAYERS_DATA));
             restoreData(savedInstanceState);
         }
     }
 
     @Override
     public void onSaveInstanceState (Bundle outState)
     {        
         super.onSaveInstanceState(outState);
 
         // Map view state
         outState.putSerializable(MAP_VIEW_DATA, map.getData());
         // Layers data
         outState.putSerializable(LAYERS_DATA, mLayerManager.getData());
         saveData(outState);
     }
     
     @Override
     public void onResume()
     {
         super.onResume();
         
         if(data.isRunAutoRecord)
         {
             bindAutoRecordService();
         }
     }
 
     @Override
     public void onPause()
     {
         super.onPause();
         
         if(data.isRunAutoRecord)
         {
             unbindAutoRecordService();
         }        
     }
 
     @Override
     public void onAttach(Activity activity)
     {
         super.onAttach(activity);
         
         mMainActivity = (MainActivity)activity;
         mServiceIntent = new Intent(mMainActivity, AutoRecordService.class);
     }
     
     // private methods ========================================================
 
     /**
      * add point to layer
      * @param location
      */
     private void recordPoint(Coordinate location, VectorLayer layer, int srid)
     {
         layer.addPoint(location, srid);
         if(layer.getType() == VectorLayerType.POINT)
         {
             layer.endObject();
         }
         
         changeRecordButtons();
         map.invalidate();
     }
 
     /**
      * start automatic recording of points
      */
     private void startAutoRecord()
     {
         mAutoRecordLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
         
         mMainActivity.startService(mServiceIntent);
         bindAutoRecordService();
         data.isRunAutoRecord = true;
         changeRecordButtons();
     }
     
     /**
      * stop automatic recording of points
      */
     private void stopAutoRecord()
     {
         mMainActivity.stopService(mServiceIntent);
         mAutoRecordLayer = null;
         data.isRunAutoRecord = false;
         changeRecordButtons();
     }
     
     /**
      * bind to auto record serice
      */
     private void bindAutoRecordService()
     {
         mMainActivity.bindService(mServiceIntent, mServiceConnection, 0);       
     }
     
     /**
      * unbind to auto record serice
      */
     private void unbindAutoRecordService()
     {
         mServiceConnection.unsetMapFragment();
         mMainActivity.unbindService(mServiceConnection);
     }
     
     /**
      * save data of MapFragment
      * @param savedInstanceState
      */
     private void saveData(Bundle outState)
     {
         // autorecordlayer
         if(mAutoRecordLayer != null)
         {
             data.autoRecordLayerString = mAutoRecordLayer.toString();
         }
         else
         {
             data.autoRecordLayerString = null;
         }
         
         outState.putSerializable(MAP_FRAGMENT_DATA, data);
     }
     
     /**
      * restore saved data
      * @param outState
      */
     private void restoreData(Bundle savedInstanceState)
     {
         data = (MapFragmentData)savedInstanceState.getSerializable(MAP_FRAGMENT_DATA);
         if(data.autoRecordLayerString != null)
         {
             mAutoRecordLayer = mLayerManager.getLayerByName(data.autoRecordLayerString);
         }
     }
     // handlers ===============================================================
     
     /**
      * add point
      */
     View.OnClickListener addPointHandler = new View.OnClickListener()
     {
         @Override
         public void onClick(View v)
         {
             VectorLayer selectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
             Coordinate location = map.getLocation();
             if(location == null)
             {
                 Toast.makeText(mMainActivity, R.string.location_fix_error, Toast.LENGTH_LONG).show();
                 return;
             }            
             
             recordPoint(location, selectedLayer, mLayerManager.getSrid());
         }        
     };
     
     /**
      * end object
      */
     View.OnClickListener endObjectHandler = new View.OnClickListener()
     {
         @Override
         public synchronized void onClick(View v)
         {
             VectorLayer selectedLayer = (VectorLayer)mMainActivity.getLayersFragment().getSelectedLayer();
 
             try
             {
                 selectedLayer.endObject();
                 
                 if(selectedLayer.equals(mAutoRecordLayer) && data.isRunAutoRecord)
                 {
                     stopAutoRecord();
                 }
             }
             catch (CreateObjectException e)
             {
                 Toast.makeText(mMainActivity, R.string.end_object_error, Toast.LENGTH_LONG).show();
             }
             
             changeRecordButtons();
             map.invalidate();
         }        
     };
 
     /**
      * start/stop automatic record points
      */
     View.OnClickListener autoRecordObjectHandler = new View.OnClickListener()
     {
         @Override
         public void onClick(View v)
         {
             if(data.isRunAutoRecord)
             {
                 stopAutoRecord();
             }
             else
             {
                 startAutoRecord();
             }
             
             changeRecordButtons();
         }        
     };
     
     // classes =================================================================
 }
