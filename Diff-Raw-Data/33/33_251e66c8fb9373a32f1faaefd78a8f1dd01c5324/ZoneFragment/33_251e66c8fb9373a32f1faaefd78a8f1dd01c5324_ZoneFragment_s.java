 package com.ecn.urbapp.fragments;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 import android.app.Fragment;
 import android.content.res.Resources;
 import android.graphics.Matrix;
 import android.graphics.Point;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.os.Bundle;
 import android.os.Environment;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 import com.ecn.urbapp.R;
 import com.ecn.urbapp.activities.MainActivity;
 import com.ecn.urbapp.db.Element;
 import com.ecn.urbapp.db.PixelGeom;
 import com.ecn.urbapp.dialogs.TopologyExceptionDialogFragment;
 import com.ecn.urbapp.dialogs.UnionDialogFragment;
 import com.ecn.urbapp.utils.ConvertGeom;
 import com.ecn.urbapp.utils.GetId;
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
 
 public class ZoneFragment extends Fragment implements OnClickListener, OnTouchListener{
 	/**
 	 * Field defining the radius tolerance on touch
 	 */
 	private int TOUCH_RADIUS_TOLERANCE = 30;//only for catching points in edit mode
 	
 	/**
 	 * Constant field defining the reference height to correct the size of point for the zone creation
 	 */
 	private final int REFERENCE_HEIGHT = 600;
 	
 	/**
 	 * Constant field defining the reference width to correct the size of point for the zone creation
 	 */
 	private final int REFERENCE_WIDTH = 1200;
 	
 	/**
 	 * Field defining the actual sate of definition of zones
 	 */
 	public static int state;
 
 	/**
 	 * Button cancel
 	 */
 	private Button cancel;
 	/**
 	 * Button fusion
 	 */
 	private Button fusion;
 	/**
 	 * Button back
 	 */
 	private Button back;
 	/**
 	 * Button validate
 	 */
 	private Button validate;
 	/**
 	 * Button delete
 	 */
 	private Button delete;
 
 	/**
 	 * Image displayed
 	 */
	private ImageView myImage;
 	
 	/**
 	 * Matrix for displaying
 	 */
 	private Matrix matrix;
 	
 	/**
 	 * Temporary zone for edition
 	 */
 	private Zone zoneCache ; 
 	
 
 	/**
 	 * Temporary pixelGeom for edition
 	 */
 	public static PixelGeom geomCache;
 	
 	/**
 	 * Zone selected
 	 */
 	private Zone zone;
 	
 	/**
 	 * Point selected
 	 */
 	private Point selected;
 	public static Element elementTemp;
 	private DrawZoneView drawzoneview;
 	private int imageHeight; private int imageWidth;
 	
 	/**
 	 * Constant value
 	 */
 	public static final int IMAGE_CREATION = 2;
 	/**
 	 * Constant value
 	 */
 	public static final int IMAGE_EDITION = 3;
 	/**
 	 * Constant value
 	 */
 	public static final int IMAGE_SELECTION= 1;
 	
 	/**
 	 * Constant value
 	 */
 	private final int POINT_SELECTED = 4;
 	
 	private int moving;
 
 	private SetCharactFragment scf;
 	private RecapCharactFragment rcf;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 	}
 	
 
 	@Override
 	public void onClick(View v) {
 		Log.d("Move","Etat:"+state);
 		switch(state){
 		
 		case IMAGE_SELECTION:
 			break;
 		case IMAGE_CREATION:
 			switch(v.getId()){
 			case R.id.zone_button_back:
 				zone.deletePoint(zone.getPoints().size()-2);
 				refreshCreate();
 				break;
 			case R.id.zone_button_cancel:
 				scf.resetAffichage();
 				state = IMAGE_SELECTION;
 				exitAction();
 				break;
 			case R.id.zone_button_fusion:
 				UnionDialogFragment summarydialog = new UnionDialogFragment();
 				summarydialog.show(getFragmentManager(), "UnionDialogFragment");
 				break;
 			case R.id.zone_button_validate:
 				scf.validation();
 				ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
 				for (PixelGeom pg : MainActivity.pixelGeom) {
 					lpg.add(pg);
 				}
 				ArrayList<Element> le = new ArrayList<Element>();
 				for (Element elt : MainActivity.element) {
 					le.add(elt);
 				}
 				try {
 					PixelGeom pg = new PixelGeom();
 					pg.setPixelGeom_the_geom((new Zone(zone)).getPolygon().toText());
 					if(elementTemp.getElementType_id()==0 && elementTemp.getMaterial_id()==0){
 						UtilCharacteristicsZone.addInMainActivityZones(pg, null);
 					}
 					else{
 						UtilCharacteristicsZone.addInMainActivityZones(pg, elementTemp);
 						//MainActivity.element.add(elementTemp);
 					}
 					geomCache=pg;
 					exitAction();
 				} catch(TopologyException e) {
 					MainActivity.pixelGeom = lpg;
 					MainActivity.element = le;
 					TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 					diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 				} catch (ParseException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				state = IMAGE_SELECTION;
 				exitAction();
 				break;
 			}
 			break;
 		case IMAGE_EDITION:
 			switch(v.getId()){
 			case R.id.zone_button_delete:
 				if(selected.x!=0 || selected.y!=0){
 					zone.deletePoint(selected);
 					selected.set(0,0);//no selected point anymore
 					myImage.invalidate();//refresh image
 				}
 				else{
 					int pos;
 					for(pos=0; pos<MainActivity.pixelGeom.size(); pos++){
 						if(MainActivity.pixelGeom.get(pos).getPixelGeomId()==geomCache.getPixelGeomId()){
 							for(int i=0; i<MainActivity.element.size(); i++){
 								if(MainActivity.element.get(i).getPixelGeom_id()==MainActivity.pixelGeom.get(pos).getPixelGeomId()){
 									MainActivity.element.remove(i);
 									scf.resetAffichage();
 									break;
 								}
 							}
 							MainActivity.pixelGeom.remove(pos);
 							break;
 						}
 					}
 					state = IMAGE_SELECTION;
 					exitAction();
 				}
 				break;
 			case R.id.zone_button_back:
 				break;
 			case R.id.zone_button_cancel:
 				scf.resetAffichage();
 	            exitAction();
 				state = IMAGE_SELECTION;
 				break;
 			case R.id.zone_button_fusion:
 				UnionDialogFragment summarydialog = new UnionDialogFragment();
 				summarydialog.show(getFragmentManager(), "UnionDialogFragment");
 				break;
 			case R.id.zone_button_validate:
 				if(!zone.getPoints().isEmpty()){
 					scf.validation();
 					ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
 					for (PixelGeom pg : MainActivity.pixelGeom) {
 						lpg.add(pg);
 					}
 					ArrayList<Element> le = new ArrayList<Element>();
 					for (Element elt : MainActivity.element) {
 						le.add(elt);
 					}
 					try {
 						//MainActivity.zones.remove(zoneCache); //delete original
 						MainActivity.pixelGeom.remove(geomCache);
 						PixelGeom pg = new PixelGeom();
 						pg.setPixelGeom_the_geom((new Zone(zone)).getPolygon().toText());
 						UtilCharacteristicsZone.addInMainActivityZones(pg, null);
 						exitAction();
 					} catch(TopologyException e) {
 						MainActivity.pixelGeom = lpg;
 						MainActivity.element = le;
 						TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 						diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 					} catch (ParseException e) {
 						e.printStackTrace();
 					}
 				}
 				state = IMAGE_SELECTION;
 				exitAction();
 				validate();
 				break;
 			}
 			break;
 		case POINT_SELECTED://TODO other buttons behavior when a point is selected 
 			switch(v.getId()){
 				case R.id.zone_button_delete:
 					if (!zone.deletePoint(selected)){
 						Toast.makeText(getActivity(), "Impossible de supprimer ce point", Toast.LENGTH_SHORT).show();
 					}
 					selected.set(0,0);//no selected point anymore
 					//myImage.invalidate();
 					refreshCreate();
 					state = IMAGE_CREATION; drawzoneview.onCreateMode();
 					delete.setEnabled(false);
 					//exitAction();
 			}
 			break;
 		}
 	}
 
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
 		View v = inflater.inflate(R.layout.layout_zone, null);
 
 		back = (Button) v.findViewById(R.id.zone_button_back);
 		fusion = (Button) v.findViewById(R.id.zone_button_fusion);
 		cancel = (Button) v.findViewById(R.id.zone_button_cancel);
 		validate = (Button) v.findViewById(R.id.zone_button_validate);
 		delete = (Button) v.findViewById(R.id.zone_button_delete);
 
 		back.setOnClickListener(this);
 		cancel.setOnClickListener(this);
 		validate.setOnClickListener(this);
 		delete.setOnClickListener(this);
 		fusion.setOnClickListener(this);
 		
 		validate.setEnabled(false);
 		back.setEnabled(false);
 		cancel.setEnabled(false);
 		fusion.setEnabled(false);
 		delete.setEnabled(false);
 
 		zone = new Zone(); zoneCache = new Zone(); selected = new Point(0,0); 
 		
 		zone = new Zone(); selected = new Point(0,0); 
 		myImage = (ImageView) v.findViewById(R.id.image_zone);
 		
 		drawzoneview = new DrawZoneView(zone, selected) ;
 
 		DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
 
 		Drawable[] drawables = {
 			new BitmapDrawable(
 				getResources(),
 				BitmapLoader.decodeSampledBitmapFromFile(
 						Environment.getExternalStorageDirectory()+"/featureapp/"+MainActivity.photo.getPhoto_url(), metrics.widthPixels, metrics.heightPixels - 174)), drawzoneview
 				};
 				
 		myImage.setImageDrawable(new LayerDrawable(drawables));
 		myImage.setOnTouchListener(this);
 
 		scf = (SetCharactFragment)getFragmentManager().findFragmentById(R.id.fragmentCaract);
 		rcf = (RecapCharactFragment)getFragmentManager().findFragmentById(R.id.fragmentRecap);
 		rcf.setZoneFragment(this);
 		
 		
 		return v;
 	}
 	
 	@Override
 	public void onStart(){
 		super.onStart();
 		
 		state=1;
 		
 		imageHeight = myImage.getDrawable().getIntrinsicHeight(); 
 		imageWidth = myImage.getDrawable().getIntrinsicWidth();
 		
 		float ratioW =((float)REFERENCE_WIDTH/imageWidth);
 		float ratioH =((float)REFERENCE_HEIGHT/imageHeight);
 		float ratio = ratioW < ratioH ? ratioW : ratioH ;
 			
 		drawzoneview.setRatio(ratio);
 		TOUCH_RADIUS_TOLERANCE/=ratio;
 	}
 	
 	/**
 	 * Common action to do on exit (cancel or validation)
 	 */
 	private void exitAction(){
         drawzoneview.onZonePage();
 		validate.setEnabled(false);
 		back.setEnabled(false);
 		cancel.setEnabled(false);
 		fusion.setEnabled(false);
 		delete.setEnabled(false);
 		
 		zone.setZone(new Zone());
 		selected.set(0,0);
 		drawzoneview.setIntersections(new Vector<Point>());
 		myImage.invalidate();
 		rcf.refresh();
 	}
 	
 	private void validate(){
 		if(!zone.getPoints().isEmpty()){
 			ArrayList<PixelGeom> lpg = new ArrayList<PixelGeom>();
 			for (PixelGeom pg : MainActivity.pixelGeom) {
 				lpg.add(pg);
 			}
 			ArrayList<Element> le = new ArrayList<Element>();
 			for (Element elt : MainActivity.element) {
 				le.add(elt);
 			}
 			try {
 				//MainActivity.zones.remove(zoneCache); //delete original
 				MainActivity.pixelGeom.remove(geomCache);
 				PixelGeom pg = new PixelGeom();
 				pg.setPixelGeom_the_geom((new Zone(zone)).getPolygon().toText());
 				UtilCharacteristicsZone.addInMainActivityZones(pg, null);
 				exitAction();
 			} catch(TopologyException e) {
 				MainActivity.pixelGeom = lpg;
 				MainActivity.element = le;
 				TopologyExceptionDialogFragment diag = new TopologyExceptionDialogFragment();
 				diag.show(getFragmentManager(), "TopologyExceptionDialogFragment");
 			} catch (ParseException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		state = IMAGE_SELECTION;
 		exitAction();
 	}
 	
 	public Point getTouchedPoint(MotionEvent event){
 		float[] coord = {event.getX(),event.getY()};//get touched point coord
 
 		getMatrix();
 		matrix.mapPoints(coord);//apply matrix transformation on points coord
 		int pointX = (int)coord[0]; int pointY = (int)coord[1];
 		Log.d("Touch","x:"+pointX+" y:"+pointY);
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
 		matrix = new Matrix();
 		myImage.getImageMatrix().invert(matrix);
 	}
 	
 	public void refreshCreate(){
 		Vector<Point> points = zone.getPoints();
 		if(! points.isEmpty()){
 			back.setEnabled(false);
 			if(points.size()>1){							
 				validate.setEnabled(false);
 				back.setEnabled(true);
 				if(points.size()>2+1){
 					validate.setEnabled(true);
 					if(points.size()>2+1){//cannot be intersections with less than 3 points but needed for refreshing displaying
 						Vector<Point> intersections = new Vector<Point>(zone.isSelfIntersecting());
 						if(!intersections.isEmpty()){
 							validate.setEnabled(false);
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
 		if(points.size()<3+1){//+1 corresponds to the ending point closing polygon 
 			validate.setEnabled(false);
 		}
 		else{
 			if(points.size()>2+1){
 				validate.setEnabled(true);
 				if(points.size()>3+1){
 					Vector<Point> intersections = new Vector<Point>(zone.isSelfIntersecting());
 					if(!intersections.isEmpty()){
 						validate.setEnabled(false);
 					}
 					drawzoneview.setIntersections(intersections);
 				}
 			}
 		}
 		myImage.invalidate();
 	}
 
 
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 			switch(state){
 			case IMAGE_EDITION:	case IMAGE_CREATION: //app behavior in these two cases are quite the same, except some ifs
 	    		/*getMatrix();
 				zone.addPoint(getTouchedPoint(event));
 				refreshCreate();
 				break;*/
 				Log.d("Move","Moving:"+moving);
 				if(event.getAction() == MotionEvent.ACTION_DOWN){
 					moving = 0;//ACTION_MOVE occurrences
 					Log.d("Move","Action Down");
 					selected.set(0, 0);
 					Point touch = getTouchedPoint(event);
 					for(Point p : zone.getPoints()){//is the touched point a normal point ?
 						float dx=Math.abs(p.x-touch.x);
 						float dy=Math.abs(p.y-touch.y);
 						if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){//10 radius tolerance
 							selected.set(p.x,p.y);
 						}
 					}
 					if(selected.x == 0 && selected.y == 0){//is the touched point a middle point ?
 						for(Point p : zone.getMiddles()){
 							float dx=Math.abs(p.x-touch.x);
 							float dy=Math.abs(p.y-touch.y);
 							if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){
 								selected.set(p.x,p.y);
 							}
 						}
 					}
 				}
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					Log.d("Move","Action Up, down time:"+(event.getEventTime()-event.getDownTime()));
 					if(selected.x==0 && selected.y==0){
 						if(state == IMAGE_CREATION){
 							zone.addPoint2(getTouchedPoint(event));				
 						}else{
 							//TODO behavior in IMAGE_EDITION when user touch out of the selected zone intentionally
							/*state = IMAGE_SELECTION;
							exitAction();*/
 						}
 					}
 					else{
 						if(state == IMAGE_CREATION && event.getEventTime()-event.getDownTime()<150){
 							float dx=Math.abs(zone.getPoints().get(0).x-selected.x);//TODO there is a math problem no ?
 							float dy=Math.abs(zone.getPoints().get(0).y-selected.y);
 							if((dx*dx+dy*dy)<TOUCH_RADIUS_TOLERANCE*TOUCH_RADIUS_TOLERANCE){//10 radius tolerance
 								validate();
 								break;
 							}
 						}
 						Point touch = getTouchedPoint(event);
 						if(moving > 5){//TODO if the app count so few ACTION_MOVE action should not be a movement, but instead of moving times we should be check distance
 							zone.updatePoint(selected, touch);
 							selected.set(0, 0);//No selected point anymore
 						}
 						else{
 							state = POINT_SELECTED;
 							delete.setEnabled(true);
 							moving=0;
 						}
 					}
 				}
 				if (event.getAction() == MotionEvent.ACTION_MOVE) {
 					moving ++;
 					Log.d("Move","Action Move");
 					if(selected.x!=0 || selected.y!=0){
 						Point touch = getTouchedPoint(event);
 						if (! zone.updatePoint(selected, touch)){//Is it a normal point ?
 							zone.updateMiddle(selected, touch);	//If not it's a "middle" point, and it's upgraded to normal
 																//So in the first move the middle point becomes a real one
 							//TODO transfer to zone
 						}
 						selected.set(touch.x,touch.y);
 					}
 				}
 				refreshCreate();//display new point, refresh buttons' availabilities	
 				break;
 
 			case IMAGE_SELECTION:
 				if (event.getAction() == MotionEvent.ACTION_UP) {
 					getMatrix();
 					Point touch = getTouchedPoint(event);
 					
 					boolean flag=false;
 					Zone z=null;
 					for(PixelGeom pg: MainActivity.pixelGeom){
 						if(ConvertGeom.pixelGeomToZone(pg).containPoint(touch)){
 							flag=true;
 							z=ConvertGeom.pixelGeomToZone(pg);
 							scf.setAffichage(pg);
 							break;
 						}
 					}
 					if(flag){
 						zoneCache = z;
 						zone.setZone(z);
 	
 						for(int i=0; i<MainActivity.pixelGeom.size(); i++){
 							if(MainActivity.pixelGeom.get(i).getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
 								geomCache = MainActivity.pixelGeom.get(i);
 								MainActivity.pixelGeom.get(i).selected=true;
 							}
 						}
 						state = IMAGE_EDITION;	drawzoneview.onEditMode();
 						validate.setEnabled(true);
 						back.setEnabled(false);
 						cancel.setEnabled(true);
 						fusion.setEnabled(true);
 						delete.setEnabled(true);
 						refreshEdit();
 					}
 					else{
 			    		getMatrix();
 						zone.addPoint2(getTouchedPoint(event));
 						refreshCreate();
 						state = IMAGE_CREATION; drawzoneview.onCreateMode();
 						validate.setEnabled(false);
 						back.setEnabled(false);
 						cancel.setEnabled(true);
 						fusion.setEnabled(true);
 						
 						elementTemp = new Element();
 						elementTemp.setElement_id(GetId.Element());
 						elementTemp.setPhoto_id(MainActivity.photo.getPhoto_id());
 						elementTemp.setPixelGeom_id(GetId.PixelGeom());
 						elementTemp.setGpsGeom_id(MainActivity.photo.getGpsGeom_id());
 					}
 				}
 				break;
 			case POINT_SELECTED ://TODO check behavior
 				state=IMAGE_CREATION; drawzoneview.onCreateMode();
 				selected.set(0,0);
 				break;
 			}
 		return true;
 	}
 
 	@Override
 	public void onStop(){
 		super.onStop();
 		getFragmentManager().beginTransaction().remove(scf).commit();
 		getFragmentManager().beginTransaction().remove(rcf).commit();
 	}
 	
 	public void selectGeom(long i){
 		if(state==IMAGE_CREATION){
 			scf.resetAffichage();
 			state = IMAGE_SELECTION;
 			exitAction();
 		}
 		else if(state==IMAGE_EDITION){
 			scf.resetAffichage();
             exitAction();
 		}
 			Zone z=null;
 			for(PixelGeom pg : MainActivity.pixelGeom){
 				z=ConvertGeom.pixelGeomToZone(pg);
 			}
 			zoneCache = z;
 			zone.setZone(z);
 
 			for(int j=0; j<MainActivity.pixelGeom.size(); j++){
 				if(MainActivity.pixelGeom.get(j).getPixelGeom_the_geom().equals(ConvertGeom.ZoneToPixelGeom(zoneCache))){
 					geomCache = MainActivity.pixelGeom.get(j);
 					MainActivity.pixelGeom.get(j).selected=true;
 				}
 			}
 			state = IMAGE_EDITION;	drawzoneview.onEditMode();
 			validate.setEnabled(true);
 			back.setEnabled(false);
 			cancel.setEnabled(true);
 			fusion.setEnabled(true);
 			delete.setEnabled(true);
 			refreshEdit();
 			scf.setAffichage(geomCache);
 	}
	
 }
