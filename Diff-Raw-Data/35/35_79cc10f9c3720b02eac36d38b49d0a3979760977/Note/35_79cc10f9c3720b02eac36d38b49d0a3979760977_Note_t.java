 package com.rhythm.music;
 
 import android.graphics.Bitmap;
 
 public abstract class Note 
 {
 	protected Bitmap rest;
 	protected Bitmap single;
 	protected boolean isRest;
 
 	protected double trueLength;
 	
 	protected double undottedLength;
 	
 	
 	protected Note(double length, int dots, boolean aRest, Bitmap restImg, Bitmap singleImg)
 	{
 		undottedLength = length;
		trueLength = length * (2-(2^(-dots)));
 		rest = restImg;
 		single = singleImg;
 		isRest = aRest;
 	}
 	
 	public  Bitmap getIcon(IconType style)
 	{
 		switch(style)
 		{
 			case SINGLE:
 				return single;
 			case REST :
 				return rest;
 			default:
 				break;
 		}
 	return null;
 	}
 	
 	public double getLength()
 	{
 		return trueLength;
 	}
 	
 	public boolean Rest()
 	{//true for rest, false for note
 		return isRest;
 	}
 	
 }
