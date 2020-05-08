 package com.rhythm.music;
 
 import android.graphics.Bitmap;
 
 public abstract class Note {
 	
 	
 	
 	protected Bitmap rest;
 	protected Bitmap single;
 
 	protected double trueLength;
 	protected double undottedLength;
 	
 	
 	protected Note(double length, int dots, Bitmap restImg, Bitmap singleImg)
 	{
 		undottedLength = length;
 		trueLength = length * (2-2^(-dots));
 		rest = restImg;
 		single = singleImg;		
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
 	
 	
 }
