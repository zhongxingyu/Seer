 package com.olegkostiuk.colorpicker;
 
 import java.awt.Color;
 
 /**
  * 
  * @author Oleg Kostiuk
  *
  */
 
 public class ColorViewHSV extends ColorView {
 
 	public ColorViewHSV() {
 		fieldNames = new char[] { 'H', 'S', 'V'};
 		initialization();
 	}
 	
 	@Override
 	protected void updateColorView() {
 		float[] HSB = new float[3];
 		Color.RGBtoHSB(RGB[0], RGB[1], RGB[2], HSB);
 	
 		HSB[0]=HSB[0]*360;
		HSB[1]=HSB[1]*100;
		HSB[2]=HSB[2]*100;

 		for (int i = 0; i < RGB.length; i++) {
 			fields[1][i].setText(String.valueOf(Math.round(HSB[i])));
 		}
 		
 	}
 
 }
