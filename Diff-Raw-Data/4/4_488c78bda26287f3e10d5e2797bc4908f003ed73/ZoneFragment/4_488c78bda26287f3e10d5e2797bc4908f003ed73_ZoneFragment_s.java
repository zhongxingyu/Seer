 package com.ecn.urbapp.fragments;
 
 import java.io.File;
 import java.util.Vector;
 
 import android.app.Fragment;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.activities.MainActivity;
 import com.ecn.urbapp.db.Element;
 import com.ecn.urbapp.db.PixelGeom;
 import com.ecn.urbapp.dialogs.TopologyExceptionDialogFragment;
 import com.ecn.urbapp.utils.ConvertGeom;
 import com.ecn.urbapp.zones.BitmapLoader;
 import com.ecn.urbapp.zones.DrawZoneView;
 import com.ecn.urbapp.zones.UtilCharacteristicsZone;
 import com.ecn.urbapp.zones.Zone;
 import com.vividsolutions.jts.geom.TopologyException;
 import com.vividsolutions.jts.io.ParseException;
 
 /**
  * @author	COHENDET Sébastien
  * 			DAVID Nicolas
  * 			GUILBART Gabriel
  * 			PALOMINOS Sylvain
  * 			PARTY Jules
  * 			RAMBEAU Merwan
  * 
  * ZoneFragment class
  * 
  * This is the fragment used to define the different zones.
  * 			
  */
 
 public class ZoneFragment extends Fragment{
 	private int TOUCH_RADIUS_TOLERANCE = 20;//only for catching points in edit mode
 	private Button create; 
 	private Button edit;
 	private Button delete;
 
 	private Button create_back;
 	private Button create_help;
 	private Button create_cancel;
 	private Button create_validate;
 	private Button create_edit;	
 
 	private Button delete_cancel;
 	private Button delete_help;
 
 	private Button edit_cancel;
 	private Button edit_validate;
 	private Button edit_deletePoint;
 	private Button edit_releasePoint;
 	private Button edit_help;
 	
 
 	private ImageView myImage; private Matrix matrix;
 	private Zone zoneCache ; 
 	private PixelGeom geomCache;
 	private Zone zone;
 	private Point selected;
 	private DrawZoneView drawzoneview;
 	private int imageHeight; private int imageWidth;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View v = inflater.inflate(R.layout.layout_zone, null);
 
 		create = (Button) v.findViewById(R.id.zone_button_create);
 		create.setOnClickListener(createListener);
 		edit = (Button) v.findViewById(R.id.zone_button_edit);
 		edit.setOnClickListener(editListener);
 		delete = (Button) v.findViewById(R.id.zone_button_delete);
 		delete.setOnClickListener(deleteListener);
 
 		create_back = (Button) v.findViewById(R.id.zone_create_button_back);
 		create_help = (Button) v.findViewById(R.id.zone_create_button_help);
 		create_cancel = (Button) v.findViewById(R.id.zone_create_button_cancel);
 		create_validate = (Button) v.findViewById(R.id.zone_create_button_validate);
 		create_edit = (Button) v.findViewById(R.id.zone_create_button_edit);
 		
 		create_back.setVisibility(View.GONE);
 		create_help.setVisibility(View.GONE);
 		create_cancel.setVisibility(View.GONE);
 		create_validate.setVisibility(View.GONE);
 		create_edit.setVisibility(View.GONE);
 
 		create_back.setOnClickListener(createBackListener);
 		create_cancel.setOnClickListener(createCancelListener);
 		create_validate.setOnClickListener(createValidateListener);
 		create_edit.setOnClickListener(editListener);//TODO change listener !
 
 		delete_cancel = (Button) v.findViewById(R.id.zone_delete_button_cancel);
 		delete_help = (Button) v.findViewById(R.id.zone_delete_button_help);
 
 		delete_cancel.setVisibility(View.GONE);
 		delete_help.setVisibility(View.GONE);
 		
 		delete_cancel.setOnClickListener(deleteCancelListener);
 
 		edit_cancel = (Button) v.findViewById(R.id.zone_edit_button_cancel);
 		edit_validate = (Button) v.findViewById(R.id.zone_edit_button_validate);
 		edit_deletePoint = (Button) v.findViewById(R.id.zone_edit_button_delete_point);
 		edit_releasePoint = (Button) v.findViewById(R.id.zone_edit_button_release_point);
 		edit_help = (Button) v.findViewById(R.id.zone_edit_button_help);
 
 		edit_cancel.setVisibility(View.GONE);
 		edit_validate.setVisibility(View.GONE);
 		edit_deletePoint.setVisibility(View.GONE);
 		edit_releasePoint.setVisibility(View.GONE);
 		edit_help.setVisibility(View.GONE);
 
 		edit_cancel.setOnClickListener(editCancelListener);
 		edit_validate.setOnClickListener(editValidateListener);
 		edit_deletePoint.setOnClickListener(editDeletePointListener);
 		edit_releasePoint.setOnClickListener(editReleasePointListener);
 		
 		zone = new Zone(); zoneCache = new Zone(); selected = new Point(0,0); 
 
 		myImage = (ImageView) v.findViewById(R.id.image_zone);
 		
 		MainActivity.sphoto=new File(Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url());	
 		
 		drawzoneview = new DrawZoneView(zone, selected) ;
 
 
 		Drawable[] drawables = {
 			new BitmapDrawable(
 				getResources(),
 				BitmapLoader.decodeSampledBitmapFromFile(
 						Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url(), 1000, 1000)), drawzoneview
 				};
 		imageWidth = BitmapLoader.getWidth();
 		imageHeight = BitmapLoader.getHeight();
 		
 		float ratioW =((float)getActivity().getWindow().getDecorView().getWidth()/imageWidth);
 		float ratioH =((float)getActivity().getWindow().getDecorView().getHeight()/imageHeight);
 		float ratio = ratioW < ratioH ? ratioW : ratioH ;
 			
 		Log.d("Size","ratioH:"+ ratioH + ";ratioW:" + ratioW + ";ratio:" + ratio);
 		drawzoneview.setRatio(ratio);
 		TOUCH_RADIUS_TOLERANCE/=ratio;
 		
 		myImage.setImageDrawable(new LayerDrawable(drawables));	
 		
 		myImage.setOnTouchListener(deleteImageTouchListener);
 		
 		return v;
 	}
 	
 	private void enterAction(){
 		//invisible, not gone, to keep buttons outside the picture
 		//TODO there are probably better ways :)
 		create.setVisibility(View.INVISIBLE);
         edit.setVisibility(View.INVISIBLE);
         delete.setVisibility(View.INVISIBLE);
         //in case we're coming switching from create
         create_back.setVisibility(View.GONE);
 		create_help.setVisibility(View.GONE);
 		create_cancel.setVisibility(View.GONE);
 		create_validate.setVisibility(View.GONE);
 		create_edit.setVisibility(View.GONE);
 	}
 	
 	private void exitAction(){
         drawzoneview.onZonePage();
 		
         create.setVisibility(View.VISIBLE);
         edit.setVisibility(View.VISIBLE);
         delete.setVisibility(View.VISIBLE);
 
 		create_back.setVisibility(View.GONE);
 		create_help.setVisibility(View.GONE);
 		create_cancel.setVisibility(View.GONE);
 		create_validate.setVisibility(View.GONE);
 		create_edit.setVisibility(View.GONE);
 		
 		edit_cancel.setVisibility(View.GONE);
 		edit_validate.setVisibility(View.GONE);
 		edit_deletePoint.setVisibility(View.GONE);
 		edit_releasePoint.setVisibility(View.GONE);
 		edit_help.setVisibility(View.GONE);
 
 		delete_cancel.setVisibility(View.GONE);
 		delete_help.setVisibility(View.GONE);
 		
 		myImage.setOnTouchListener(new View.OnTouchListener() {
 		//just erasing previous listeners 	
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				return false;
 			}
 		});
 		zone.setZone(new Zone());
 		zoneCache.setZone(new Zone());
 		selected.set(0,0);
 		drawzoneview.setIntersections(new Vector<Point>());
 		myImage.invalidate();
 	}
 	
 	/** Declarations for create zone **/
 	private OnClickListener createListener = new View.OnClickListener() {
 
         @Override
         public void onClick(View v) {
             enterAction();
             drawzoneview.onCreateMode();
 
     		create_back.setVisibility(View.VISIBLE);
     		create_help.setVisibility(View.VISIBLE);
     		create_cancel.setVisibility(View.VISIBLE);
     		create_validate.setVisibility(View.VISIBLE);
             create_edit.setVisibility(View.VISIBLE);
     		
     		getView().findViewById(R.id.zone_create_button_validate).setEnabled(false);
     		getView().findViewById(R.id.zone_create_button_back).setEnabled(false);
     		
     		myImage.setOnTouchListener(imageCreateTouchListener);
         }
 	};
     private OnClickListener createBackListener = new View.OnClickListener() {
     	@Override
 		public void onClick(View v) {
 			zone.getPoints().remove(zone.getPoints().lastElement());
 			refreshCreate();
 		}
     };
     private OnClickListener createValidateListener = new View.OnClickListener() {			
 		@Override
 		public void onClick(View v) {
 			try {
 				UtilCharacteristicsZone.addInMainActivityZones((new Zone(zone)).getPolygon());
 				exitAction();
 			} catch(TopologyException e) {
 				TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 				diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 /*
             /** set of the database object **//*
 			PixelGeom pg = new PixelGeom();
 			pg.setPixelGeom_the_geom(ConvertGeom.ZoneToPixelGeom(zone));
 			pg.setPixelGeomId(MainActivity.pixelGeom.size()+1);
 			
 			Element element = new Element();
 			element.setElement_id(MainActivity.element.size()+1);
 			element.setPhoto_id(MainActivity.photo.getPhoto_id());
 			element.setPixelGeom_id(pg.getPixelGeomId());
 			element.setElement_color(""+Color.RED);
 			element.setGpsGeom_id(1);//TODO DELETE
 			
 
 			MainActivity.element.add(element);
 			MainActivity.pixelGeom.add(pg);
 			exitAction();*/
 		}
 	};
 	private OnClickListener createCancelListener = new View.OnClickListener() {			
 		@Override
 		public void onClick(View v) {
 			exitAction();
 		}
 	};
 	private OnTouchListener imageCreateTouchListener = new ImageView.OnTouchListener() {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 	    		getMatrix();
 				zone.addPoint(getTouchedPoint(event));
 				refreshCreate();//display new point, refresh buttons' availabilities					
 			}
 			return true;
 		}
 	};
 	public Point getTouchedPoint(MotionEvent event){
 		float[] coord = {event.getX(),event.getY()};//get touched point coord
 
 		matrix.mapPoints(coord);//apply matrix transformation on points coord
 		int pointX = (int)coord[0]; int pointY = (int)coord[1];
 		if(pointX<0){
 			pointX=0;
 		}else{
 			if(pointX>imageWidth){
 				pointX=imageWidth;
 			}
 		}
 		if(pointY<0){
 			pointY=0;
 		}else{
 			if(pointY>imageHeight){
 				pointY=imageHeight;
 			}
 		}
 		return(new Point(pointX,pointY));
 	}
 	
 	public void getMatrix(){
 		//if(matrix == null){//tried to create it at fragment's beginning but doesn't work at all
 			matrix = new Matrix();
 			myImage.getImageMatrix().invert(matrix);
 		//}//removed the if for a little, problems when reload
 	}
 	
     public void refreshCreate(){
 		Vector<Point> points = zone.getPoints();
 		if(! points.isEmpty()){
 			getView().findViewById(R.id.zone_create_button_back).setEnabled(false);
 			if(points.size()>1){							
 				getView().findViewById(R.id.zone_create_button_validate).setEnabled(false);
 				getView().findViewById(R.id.zone_create_button_back).setEnabled(true);
 				if(points.size()>2){
 					getView().findViewById(R.id.zone_create_button_validate).setEnabled(true);
 					if(points.size()>3){
 						Log.d("Intersect","enough points to test !");
 						Vector<Point> intersections = new Vector<Point>(zone.isSelfIntersecting());
 						if(!intersections.isEmpty()){
 							getView().findViewById(R.id.zone_create_button_validate).setEnabled(false);
 						}
 						drawzoneview.setIntersections(intersections);
 					}
 				}
 			}
 		}
 		myImage.invalidate();
 	}
     public void refreshEdit(){
 		Vector<Point> points = zone.getPoints();
 		if(points.size()<3){
 			getView().findViewById(R.id.zone_edit_button_validate).setEnabled(false);
 		}
 		else{
 			if(points.size()>2){
 				getView().findViewById(R.id.zone_edit_button_validate).setEnabled(true);
 				if(points.size()>3){
 					Vector<Point> intersections = new Vector<Point>(zone.isSelfIntersecting());
 					if(!intersections.isEmpty()){
 						getView().findViewById(R.id.zone_edit_button_validate).setEnabled(false);
 					}
 					drawzoneview.setIntersections(intersections);
 				}
 			}
 		}
 		myImage.invalidate();
 	}
     
     /***** zone edition main *****/
     private OnClickListener editListener = new View.OnClickListener(){
     	@Override
         public void onClick(View v) {
     		enterAction();
             drawzoneview.onEditMode();
 
     		edit_cancel.setVisibility(View.VISIBLE);
     		edit_validate.setVisibility(View.VISIBLE);
     		edit_deletePoint.setVisibility(View.VISIBLE);
     		edit_releasePoint.setVisibility(View.VISIBLE);
     		edit_help.setVisibility(View.VISIBLE);
             
     		getView().findViewById(R.id.zone_edit_button_delete_point).setEnabled(false);
     		getView().findViewById(R.id.zone_edit_button_release_point).setEnabled(false);
 
     		myImage.setOnTouchListener(imageEditTouchListener); 
     	}
     };
     
     private ImageView.OnTouchListener imageEditTouchListener = new ImageView.OnTouchListener() {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 	    		getMatrix();
 				Point touch = getTouchedPoint(event);
 				
 				//Creation of thelist ofthe zone setted
 				Vector<Zone> zones = new Vector<Zone>();
 				for(PixelGeom pg: MainActivity.pixelGeom){
 					zones.add(ConvertGeom.pixelGeomToZone(pg));
 				}
 				
 				//If no zone has been selected yet, try to select one
 				if(zone.getPoints().isEmpty()){
 					for(Zone test : zones){
 						if(test.containPoint(touch)){
 							zoneCache = test;
 							zone.setZone(test);
 
 							for(PixelGeom pg : MainActivity.pixelGeom){
 								if(pg.getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
 									geomCache = pg;
 								}
 							}
 /*=======
 							zoneCache = new Zone(test);
 							zone = new Zone(test);;
 >>>>>>> dev_database*/
 							//zone.setZone(test);
 						}
 					}
 				}
 				else{
 					//If a zone is selected, and one of its points was, it's a point move
 					if(selected.x != 0 && selected.y != 0){
 						if (! zone.updatePoint(selected, touch)){//Is it a normal point ?
 							zone.updateMiddle(selected, touch);	//If not it's a "middle" point, and it's upgraded to normal
 							//TODO transfer to zone
 						}
 						selected.set(0, 0);//No selected point anymore
 						//Disable point delete or release actions 
 						getView().findViewById(R.id.zone_edit_button_delete_point).setEnabled(false);
 						getView().findViewById(R.id.zone_edit_button_release_point).setEnabled(false);
 					}
 					//If a zone is selected, but none of its points, is the touch point a normal or middle point ?
 					else{
 						for(Point p : zone.getPoints()){//is the touched point a normal point ?
 							float dx=Math.abs(p.x-touch.x);
 							float dy=Math.abs(p.y-touch.y);
 							if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){//10 radius tolerance
 								selected.set(p.x,p.y);
 								//enable point deleting or releasing
 								getView().findViewById(R.id.zone_edit_button_delete_point).setEnabled(true);
 								getView().findViewById(R.id.zone_edit_button_release_point).setEnabled(true);
 							}
 						}
 						if(selected.x == 0 && selected.y == 0){//is the touched point a middle point ?
 							for(Point p : zone.getMiddles()){
 								float dx=Math.abs(p.x-touch.x);
 								float dy=Math.abs(p.y-touch.y);
 								if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){
 									selected.set(p.x,p.y);
 									//impossible to delete a middle point, but enable releasing
 									getView().findViewById(R.id.zone_edit_button_release_point).setEnabled(true);
 								}
 							}
 						}
 					}							
 				}
 				//myImage.invalidate();//refresh points' displaying
 				refreshEdit();
 			}
 			return true;
 		}
 	};
 	private OnClickListener editValidateListener = new View.OnClickListener() {			
 		@Override
 
 		public void onClick(View v) {
 			//zones.remove(zoneCache);//delete original 
 
 			if(!zone.getPoints().isEmpty()){
 				try {
 					//MainActivity.zones.remove(zoneCache); //delete original
 					MainActivity.pixelGeom.remove(geomCache);
 					UtilCharacteristicsZone.addInMainActivityZones((new Zone(zone)).getPolygon());
 					exitAction();
 				} catch(TopologyException e) {
 					TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 					diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			else{
 				exitAction();
 			}
 	/*
 			long id=0;
 			int row=-1;
 			PixelGeom pgeom = new PixelGeom();
 			for(PixelGeom pg : MainActivity.pixelGeom){
 				if(pg.getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
 					id=pg.getPixelGeomId();
 					pgeom=pg;
 					break;
 				}
 			}
 			MainActivity.pixelGeom.remove(pgeom);
 			MainActivity.zones.remove(zoneCache);
 			pgeom = new PixelGeom();
 			pgeom.setPixelGeom_the_geom(ConvertGeom.ZoneToPixelGeom(zone));
 			pgeom.setPixelGeomId(id);
 			MainActivity.pixelGeom.add(pgeom);
 			MainActivity.zones.add(new Zone(zone));//save edited
 			exitAction();*/
 		}
 	};
 	private OnClickListener editCancelListener = new View.OnClickListener() {		
 		@Override
 		public void onClick(View v) {
 			if(zoneCache != null){//if user is coming from CreateZone there is no original to save
 				//MainActivity.zones.add(new Zone(zoneCache));//save original
 				/*PixelGeom pgeom = new PixelGeom();
 				pgeom.setPixelGeomId(MainActivity.pixelGeom.size());
 				pgeom.setPixelGeom_the_geom(ConvertGeom.ZoneToPixelGeom(zoneCache));
 				
 				MainActivity.pixelGeom.add(pgeom);*/
 			}
             exitAction();
 		}
 	};
 	private OnClickListener editDeletePointListener = new View.OnClickListener() {			
 		@Override
 		public void onClick(View v) {
 			zone.deletePoint(selected);
 			selected.set(0,0);//no selected point anymore
 			getView().findViewById(R.id.zone_edit_button_delete_point).setEnabled(false);
 			getView().findViewById(R.id.zone_edit_button_release_point).setEnabled(false);
 			myImage.invalidate();//refresh image
 		}
 	};
 	private OnClickListener editReleasePointListener = new View.OnClickListener() {			
 		@Override
 		public void onClick(View v) {
 			selected.set(0,0);
 			getView().findViewById(R.id.zone_edit_button_delete_point).setEnabled(false);
 			getView().findViewById(R.id.zone_edit_button_release_point).setEnabled(false);
 			myImage.invalidate();
 		}
 	};
 	
 	/**** deletion part *****/
 	private OnClickListener deleteListener = new View.OnClickListener() {			
 		@Override
 		public void onClick(View v) {
 			enterAction();
 
     		delete_cancel.setVisibility(View.VISIBLE);
     		delete_help.setVisibility(View.VISIBLE);
 			
 			myImage.setOnTouchListener(deleteImageTouchListener);
 		}
 	};
 	
 	private OnTouchListener deleteImageTouchListener = new ImageView.OnTouchListener() {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if (event.getAction() == MotionEvent.ACTION_DOWN) {
 				getMatrix();
 				Point touch = getTouchedPoint(event);
 
 				//Creation of thelist ofthe zone setted
 				Vector<Zone> zones = new Vector<Zone>();
 				for(PixelGeom pg: MainActivity.pixelGeom){
 					zones.add(ConvertGeom.pixelGeomToZone(pg));
 				}
 				
 				if(zone.getPoints().isEmpty()){
 					for(Zone test : zones){
 						if(test.containPoint(touch)){
 							zoneCache = test;
 						}
 					}
 					if(zoneCache != null){
 						//zones.remove(zoneCache);//delete original 
 						PixelGeom pgeom = new PixelGeom();
 						for(PixelGeom pg : MainActivity.pixelGeom){
 							if(pg.getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
 								pgeom=pg;
 								break;
 							}
 						}
 						if(pgeom.getPixelGeomId()!=0){
 							MainActivity.pixelGeom.remove((int)pgeom.getPixelGeomId()-1);
 						}
 						long id=0;
 						for(Element el : MainActivity.element){
 							if(el.getPixelGeom_id()==pgeom.getPixelGeomId()){
 								id=el.getElement_id();
 							}
 						}
						MainActivity.element.remove((int)id-1);
 						
 						//MainActivity.zones.remove(zoneCache);						
 			            exitAction();
 					}
 				}
 			}
 			return true;
 		}
 	};
 	private OnClickListener deleteCancelListener = new View.OnClickListener() {			
 		@Override
 		public void onClick(View v) {
             exitAction();
 		}
 	};
 	
 	@Override
 	public void onStop(){
 		super.onStop();
 	}
 }
