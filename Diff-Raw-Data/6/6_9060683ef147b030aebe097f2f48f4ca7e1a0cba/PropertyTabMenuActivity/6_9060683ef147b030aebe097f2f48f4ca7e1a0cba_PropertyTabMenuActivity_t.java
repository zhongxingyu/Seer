 package pt.up.fe.cmov.app;
 
 import org.apache.http.conn.ConnectTimeoutException;
 import org.json.JSONObject;
 
 import pt.up.cmov.entities.Property;
import pt.up.fe.cmov.display.Display;
 import pt.up.fe.cmov.gridadapter.PropertyGridAdapter;
 import pt.up.fe.cmov.propertymarket.R;
 import pt.up.fe.cmov.propertymarket.rest.JSONOperations;
 import pt.up.fe.cmov.propertymarket.rest.RailsRestClient;
 import android.app.TabActivity;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.GestureDetector;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.GestureDetector.SimpleOnGestureListener;
 import android.widget.TabHost;
 
 public class PropertyTabMenuActivity extends TabActivity {
 	
 	static Property propertyInfo = null;
 	private static final int SWIPE_MIN_DISTANCE = 50;
     private static final int SWIPE_MAX_OFF_PATH = 500;
 	private static final int SWIPE_THRESHOLD_VELOCITY = 50;
 	private GestureDetector gestureDetector;
 	View.OnTouchListener gestureListener;	
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 	    super.onCreate(savedInstanceState);
 	    setContentView(R.layout.tab_property_layout);
 
 	    Resources res = getResources(); 
 	    TabHost tabHost = getTabHost();  
 	    TabHost.TabSpec spec;  
 	    Intent intent; 
 	    
 	    loadProperty();
 	    
 	    intent = new Intent().setClass(this, PropertyDetailsActivity.class);
 
 	    spec = tabHost.newTabSpec(this.getString(R.string.property_title_lowercase))
 	    			  .setIndicator(this.getString(R.string.property_title),res.getDrawable(R.drawable.ic_tab_ic_tab_info_selected))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 
 	    intent = new Intent().setClass(this, PropertyAditionalInfoActivity.class);
 	    spec = tabHost.newTabSpec(this.getString(R.string.details_title_lowercase))
 	    			  .setIndicator(this.getString(R.string.details_title),res.getDrawable(R.drawable.ic_tab_aditional_info_unselected))
 	                  .setContent(intent);
 	    tabHost.addTab(spec);
 	    
 	    tabHost.setCurrentTab(0);
 	    
 	    gestureDetector = new GestureDetector(new MyGestureDetector());
         gestureListener = new View.OnTouchListener() {
             public boolean onTouch(View v, MotionEvent event) {
                 if (gestureDetector.onTouchEvent(event)) {
                     return true;
                 }
                 return false;
             }
         };
 	}
 	
 	class MyGestureDetector extends SimpleOnGestureListener {
 		  @Override
 		  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
 		    try {
 		         if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH){
 		            return false;
 		         }
 		         if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX*velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 		        	 PropertyMarketActivity.selectedPropertyID = PropertyGridAdapter.getItemStaticId(getPositionToMoveFoward());
 		             Intent intent = new Intent(PropertyTabMenuActivity.this,PropertyTabMenuActivity.class);
 		             PropertyTabMenuActivity.this.startActivity(intent);
 		             PropertyTabMenuActivity.this.finish();	         
 		         }else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX*velocityY) > SWIPE_THRESHOLD_VELOCITY) {
 		        	 PropertyMarketActivity.selectedPropertyID = PropertyGridAdapter.getItemStaticId(getPositionToMoveBackward());
 		             Intent intent = new Intent(PropertyTabMenuActivity.this,PropertyTabMenuActivity.class);
 		             PropertyTabMenuActivity.this.startActivity(intent);
 		             PropertyTabMenuActivity.this.finish();	         
 		         }
 		     } catch (Exception e) {
 		     return false;
 		    }
 			return true;   
 		 }
 		}
 	    
 	@Override
 	public boolean onTouchEvent(MotionEvent event) {
 	  if (gestureDetector.onTouchEvent(event))
 		return true;
 	  else
 		return false;
 	}
 	 
 	public int getPositionToMoveFoward(){
 		if(PropertyGridAdapter.getStaticCount() - 1 > PropertyMarketActivity.selectedPropertyPosition)
    		 	PropertyMarketActivity.selectedPropertyPosition += 1;
    	 	else
    	 		PropertyMarketActivity.selectedPropertyPosition = 0;
 		return PropertyMarketActivity.selectedPropertyPosition;
 	}
 	
 	public int getPositionToMoveBackward(){
 		 if(PropertyMarketActivity.selectedPropertyPosition > 0)
     		 PropertyMarketActivity.selectedPropertyPosition -= 1;
     	 else
     		 PropertyMarketActivity.selectedPropertyPosition = PropertyGridAdapter.getStaticCount();
 		return PropertyMarketActivity.selectedPropertyPosition;
 	}
 	
 	public boolean loadProperty(){
 		try {
 			JSONObject selectedProperty = RailsRestClient.Get(this.getString(R.string.proprety_controller) + 
 		    PropertyMarketActivity.selectedPropertyID + this.getString(R.string.long_field));
 			PropertyTabMenuActivity.propertyInfo = JSONOperations.JSONToProperty(selectedProperty);
 			return true;
 		} catch (ConnectTimeoutException e) {
			Display.dialogMessageNotConnected(this);
 		}	
 		return false;
 	}
 
 }
