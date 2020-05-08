 package com.sgs.hotelguru;
 
 import android.app.Activity;
 import android.graphics.PointF;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 
 
 
 public class MapsExample extends Activity {
 
 	View mainView;
     Button QM2_12002;
     Button QM2_12081;
     Button buttonZoomIn;
     final float Zoom_max = 4f;
     final float Zoom_min = 1f;
     private static final String TAG = "Die Karte";
     myDatabase db;
 	float myWorkX = 0;
 	float myWorkY = 0;
	String GlobalCruiseLine;
	String GlobalShipName;
	String GlobalShipDeck;

 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_maps_example);
        
    	Bundle extras = getIntent().getExtras();
    	if (extras != null) {
    	    GlobalCruiseLine = extras.getString("GlobalCruiseLine");
    	    GlobalShipName = extras.getString("GlobalShipName");
    	    GlobalShipDeck = extras.getString("GlobalShipDeck");
    	    Toast.makeText(MapsExample.this,"MOVED to selected Deck Plans for "+GlobalCruiseLine+" "+GlobalShipName+" "+GlobalShipDeck,
                    Toast.LENGTH_SHORT).show();
    	}
        
         mainView =findViewById(R.id.DeckLayout);
         db = new myDatabase(getApplicationContext());
 
         Button buttonZoomOut = (Button)findViewById(R.id.zoom_out);
         buttonZoomOut.setX(350);
         //Button buttonNormal = (Button)findViewById(R.id.reset);
         buttonZoomIn = (Button)findViewById(R.id.zoom_in);
         QM2_12002 = (Button)findViewById(R.id.QM2_12002);
         QM2_12081 = (Button)findViewById(R.id.QM2_12081);
       buttonZoomOut.setOnClickListener(new OnClickListener() {
 
        @Override
        public void onClick(View v) {
         zoom_out(new PointF(350,400));
        }
       });/*
       buttonNormal.setOnClickListener(new OnClickListener() {
 
        @Override
        public void onClick(View v) {
         zoom(1f,1f,new PointF(0,0));
        }
       });*/
       mainView.setOnTouchListener(new OnTouchListener(){
 
 			@Override
 			public boolean onTouch(View arg0, MotionEvent arg1) {
 				// TODO Auto-generated method stub
 				Toast.makeText(MapsExample.this,"Setting zoom point to here",Toast.LENGTH_SHORT).show();
 				myWorkX = arg1.getX();
 				myWorkY = arg1.getY();
 				return true;
 			}
   	});
         buttonZoomIn.setOnClickListener(new View.OnClickListener() {
 
             @Override
             public void onClick(View v) {
             	
 
             	
             	
                 zoom(new PointF(myWorkX,myWorkY));
             }
         });
         QM2_12002.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             	//float myWorkX = QM2_12002.getX();
             	//float myWorkY = QM2_12002.getY();
             	roomDataStruct myRoomData = db.getRoomData("Queen Mary 2", 12002);
                 Toast.makeText(MapsExample.this,myRoomData.getRoomNum()+" "+myRoomData.getRoomType()+" "+myRoomData.getRoomDeck(),
                         Toast.LENGTH_SHORT).show();
             }
             
         });
         QM2_12081.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
             	//float myWorkX = QM2_12081.getX();
             	//float myWorkY = QM2_12081.getY();
             	roomDataStruct myRoomData = db.getRoomData("Queen Mary 2", 12081);
                 Toast.makeText(MapsExample.this,myRoomData.getRoomNum()+" "+myRoomData.getRoomType()+" "+myRoomData.getRoomDeck(),
                         Toast.LENGTH_SHORT).show();
             }
             
         });
         mainView.post(new Runnable() {
             // Post in the parent's message queue to make sure the parent
             // lays out its children before you call getHitRect()
             @Override
             public void run() {
                 // The bounds for the delegate view (an ImageButton
                 // in this example)
                 QM2_12002.setX(325);
                 QM2_12002.setY(92);
                 QM2_12002.setEnabled(true);
                 QM2_12081.setX(390);
                 QM2_12081.setY(370);
                 QM2_12081.setEnabled(true);
             }
         });
     }
 
 
     /** zooming is done from here */
     public void zoom(PointF pivot){
         mainView.setPivotX(pivot.x);
         mainView.setPivotY(pivot.y);
         float workScaleX = mainView.getScaleX();
         float workScaleY = mainView.getScaleY();
        /* float workButtonX = QM2_12002.getX();
         float workButtonY = QM2_12002.getY();
         float workButtonX12081 = QM2_12081.getX();
         float workButtonY12081 = QM2_12081.getY();*/
         if(workScaleX<Zoom_max)
         {
             mainView.setScaleX(workScaleX*2f);
             mainView.setScaleY(workScaleY*2f);
             /*QM2_12002.setX(workButtonX/2f);
             QM2_12002.setY(workButtonY/2f);
             QM2_12081.setX(workButtonX12081/2f);
             QM2_12081.setY(workButtonY12081/2.45f);*/
         }
     }
     public void zoom_out(PointF pivot){
     	 mainView.setPivotX(pivot.x);
          mainView.setPivotY(pivot.y);
          float workScaleX = mainView.getScaleX();
          float workScaleY = mainView.getScaleY();
          if(workScaleX>Zoom_min)
          {
              mainView.setScaleX(workScaleX/2f);
              mainView.setScaleY(workScaleY/2f);
          }
     }
 }
