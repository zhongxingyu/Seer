 package util;
 
 import play.*;
 import java.util.*;
 import java.io.*;
 
 import org.codehaus.jackson.*;
 import org.codehaus.jackson.node.*;
 
 // Loads data in a raw/unmodified format. As other Layer_Base subclasses transform
 //	the data in various ways to make querying faster, etc. Thus, this class would
 //	be useful when you need access to the raw data in various formats
 //------------------------------------------------------------------------------
 public class Layer_Raw extends Layer_Base
 {
 	// save data as float and int? That cover most cases?
 	private float[][] mFloatData;
 
 	//--------------------------------------------------------------------------
 	public Layer_Raw(String name) {
 
 		super(name);
 	}
 	
 	//--------------------------------------------------------------------------
 	public float[][] getFloatData() {
 		
 		return mFloatData;
 	}
 	
 	//--------------------------------------------------------------------------
 	protected void allocMemory(int width, int height) {
 		
 		super.allocMemory(width, height);
 		mFloatData = new float[mHeight][mWidth];
 	}
 	
 	//--------------------------------------------------------------------------
 	protected void processASC_Line(int y, String lineElementsArray[]) {
 		
 		for (int x = 0; x < lineElementsArray.length; x++) {
 			// save data as both Int and Float
 			int iVal = Integer.parseInt(lineElementsArray[x]);
 			mIntData[y][x] = iVal;
 			
 			float fVal = Float.parseFloat(lineElementsArray[x]);
 			mFloatData[y][x] = fVal;
 		}
 	}
 
 	//--------------------------------------------------------------------------
 	protected void onLoadEnd() { /*does nothing at this point*/ }
 	
 	//--------------------------------------------------------------------------
	protected Selection query(JsonNode queryNode, Selection selection) {
 
 		// not sure if we need to implement queries on this type of data?
		//	At this point, just return the passed in selection
		return selection;
 	}
 }
 	
 
