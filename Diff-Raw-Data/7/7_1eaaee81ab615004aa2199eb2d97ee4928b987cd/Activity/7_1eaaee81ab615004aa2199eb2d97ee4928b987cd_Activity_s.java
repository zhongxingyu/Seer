 package com.irr310.i3d.view;
 
 import com.irr310.common.tools.Log;
 import com.irr310.i3d.Bundle;
 import com.irr310.i3d.Graphics;
 import com.irr310.i3d.I3dContext;
 import com.irr310.i3d.I3dRessourceManager;
 import com.irr310.i3d.Intent;
 import com.irr310.i3d.Surface;
 import com.irr310.server.Time;
 
 import fr.def.iss.vd2.lib_v3d.V3DMouseEvent;
 
 public abstract class Activity implements ViewParent {
 
     private View mview;
     private LayoutParams mLayout;
     private boolean mLayoutUpdated;
 	private Surface parentSurface;
     private Intent intent;
     private I3dContext context;
     private boolean stackable;
 
     public abstract void onCreate(Bundle bundle);
     public abstract void onResume();
     public abstract void onPause();
     public abstract void onDestroy();
     
     public Activity() {
         mLayoutUpdated = false;
         stackable = true;
     }
     
     public void assignSurface(Surface parentSurface) {
         this.parentSurface = parentSurface;
 		mLayout = new LayoutParams();
         forceLayout();
     }
     
     public void setContext(I3dContext context) {
         this.context = context;
     }
     
     protected void setContentView(String string) {
         mview = I3dRessourceManager.loadView(string);
         mview.assignParent(this);
     }
     
     public void draw(Graphics g) {
         mview.draw(g);
     }
     
     public void update(Time absTime, Time gameTime) {
         onUpdate(absTime, gameTime);
         if(!mLayoutUpdated) {
             mLayoutUpdated = true;
             mview.measure();
             mview.layout(mLayout.mLeft, mLayout.mTop, mLayout.mRight,mLayout.mBottom);
         }
     }
 
     protected void onUpdate(Time absTime, Time gameTime) {
     }
     
     @Override
     public ViewParent getParent() {
         return null;
     }
     
     @Override
     public void requestLayout() {
         mLayoutUpdated = false;
     }
   
     @Override
     public void addChild(View view) {
         // TODO Auto-generated method stub
         
     }
     
     public void setStackable(boolean stackable) {
         this.stackable = stackable;
         
     }
     
     public boolean isStackable() {
         return stackable;
     }
     
     protected View findViewById(String id) {
 		View view = mview.findViewById(id);
 		if(view == null) {
 			Log.error("Fail to find View with '"+id+"' as id.");
 		}
 		return view;
 	}
 	public void forceLayout() {
 		mLayoutUpdated = false;
 		mLayout.setFrame(0, 0, parentSurface.width, parentSurface.height);
 	}
 
 	/**
 	 * Call by the activity to leave the hand to another activity
 	 * @param mainMenuActivity
 	 */
 	protected void startActivity(Intent intent) {
         parentSurface.startActivity(intent);
     }
 	
     public void setIntent(Intent intent) {
         this.intent = intent;
     }
     
     public Intent getIntent() {
         return intent;
     }
 
     public I3dContext getContext() {
         return context;
     }
     
     public void onMouseEvent(V3DMouseEvent mouseEvent) {
         mview.onMouseEvent(mouseEvent);
     }
 
 }
