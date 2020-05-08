 package com.ec.morsms;
 //hello
 import android.app.Application;
import com.ec.morsms.R;
 
 public class Global extends Application {
 	
 	
 	// variables
     private int unitSpeed;		//the value of of vibration speed
     private int buttonState;	//whether message vibration is enabled or not
     private int maxChar;		//maximum number of characters input message
     
     //on create application, initialize things
     @Override
     public void onCreate() {
         // Here we could pull values from a config file in res/raw or somewhere else
         unitSpeed = 300;
         buttonState = 0;
         maxChar = 0;	//0 means no maximum.. for now.
         super.onCreate();
     }
     
     
   //global application variables gets/sets
     
     public int getEnableState() {
         return this.buttonState;
     }
     
     public void setEnableState(int set) {
         this.buttonState = set;
     }
 	
     //vibration unit speed
     public int getUnitSpeed(){
     	return unitSpeed;
     }
 	
     public void setUnitSpeed(int set) {
     	this.unitSpeed = set;
     }
 
     
   //vibration maxChar
     public int getMaxChar(){
     	return maxChar;
     }
 	
     public void setMaxChar(int set) {
     	this.maxChar = set;
     }
 
 }
