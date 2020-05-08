 package com.teamluper.luper;
 
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.view.View;
 import android.widget.*;
 import java.util.Random;
 import android.graphics.Color;
 
 
 public class ColorChipButton extends Button {
 
 
 	private static final String TAG = "ColorClipButton";
   private static final float PIXELS_PER_MILLISECOND = 0.4f;
 
 	//the clip that is associated with this CCB
 	Clip associated;
   int mColor;
   Random rnd = new Random();
 
 
 	//set a click listener for the buttons that will activate promptDialog() when clicked
 	OnClickListener clicker = new OnClickListener(){
 		public void onClick(View v){
 			//promptDialog();
             showListDialog();
 		}
 	};
 
     public void showListDialog(){
         final CharSequence[] items = {"Details", "Edit", "Delete", "Cancel"};
         new AlertDialog.Builder(getContext())
                 .setTitle("Clip Options")
                 .setItems(items, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
                 if (items[item].equals("Details"))
                 {
                     new AlertDialog.Builder(getContext())
                             .setTitle("Length " + associated.getDurationMS() + " ms")
                             .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     //Do nothing for now
                                 }
                             })
                             .show();
                 }
                 if (items[item].equals("Edit"))
                 {
                     DialogFactory.prompt(getContext(),"Edit Start Time","",
                             new Lambda.StringCallback() {
                                 public void go(String value) {
                                     int val= Integer.parseInt(value);
                                     associated.setStartTime(val);
                                 }
                             }
                     );
                 }
                 else if (items[item].equals("Delete"))
                 {
                     LuperProjectEditorActivity a = (LuperProjectEditorActivity) getContext();
                     a.dataSource.deleteClip(associated.getId());
                     // TODO remove clip from editor
 
                     new AlertDialog.Builder(getContext())
                             .setTitle("Done")
                             .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog, int whichButton) {
                                     //Do nothing for now
                                 }
                             })
                             .show();
                 }
                 else if (items[item].equals("Cancel"))
                 {
                     // does nothing and will never do anything
                 }
             }
         }).show();
     }
 
 
 	//constructor sets the associated clip, calls init, and sets the onclicklistener
 	public ColorChipButton(Context context, Clip clip){
 		super(context);
 		associated = clip;
         mColor = clip.getColor();
 		init();
 		setOnClickListener(clicker);
 	}
 
 	//this method will determine where the clip should be placed, based on its start time
 	public void init(){
 		//this.setX(this.getStartTime() + 50);
 		//this.setWidth((this.getStartTime() + 50) + this.getLength()/10);
		this.setX(this.getStartTime()*PIXELS_PER_MILLISECOND + 99);
		this.setWidth(Math.round(this.getLength()*PIXELS_PER_MILLISECOND) + 99);
        this.setHeight(140);
        mColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
        this.setBackgroundColor(mColor);
 	}
 
 	public void displayStats(){
 //		to do, make it return the clip's length and start time; start time being editable
 	}
 
 	//returns the clip associated with this button
 	public Clip getClip(){
 		return this.associated;
 	}
 
 	//returns the length of this button (and inherently its clip)
 	public int getLength(){
 		return this.associated.getDurationMS();
 	}
 
 	//returns the start time of the button (and clip) in this track
 	public int getStartTime(){
 		return this.associated.getStartTime();
 	}
   public void setColor(int color) {
     mColor = color;
     this.setBackgroundColor(mColor);
     invalidate();
   }
   public void setRandColor() {
     mColor = Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
     this.setBackgroundColor(mColor);
     invalidate();
   }
 //	when you click the button this method is activated. currently it only shows the length of
 //	the clip. eventually it should allow you to modify things about the clip
 	public void promptDialog(){
 		new AlertDialog.Builder(getContext())
             .setTitle("Clip Details")
 			.setMessage("Length " + associated.getDurationMS() + " ms" + "    The Current Color is: " + mColor)
 		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int whichButton) {
 		      	  //Do nothing for now
 		        }
 		    })
 		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 		        public void onClick(DialogInterface dialog, int whichButton) {
 		            // Do nothing.
 		        }
 		    })
 		    .show();
 	}
 
 }
