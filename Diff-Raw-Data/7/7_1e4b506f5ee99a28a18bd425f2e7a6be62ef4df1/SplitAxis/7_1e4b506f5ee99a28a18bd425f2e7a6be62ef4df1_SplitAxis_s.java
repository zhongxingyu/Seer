 package graph;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import waba.sys.*;
 import waba.util.*;
 import extra.util.*;    
 import extra.io.*;
 
 public class SplitAxis extends Axis
 {
 	public Axis [] axisArray = new Axis [10];
 	public Axis lastAxis = null;
     int numAxis;
 	int type;
 
     public final static float MIN = 0;
     public final static float MAX = 100;
 
 	public SplitAxis(int t)
 	{
 		super(t);
 
 		type = t;
 		lastAxis = new Axis(type);
 		lastAxis.max = (float)1E30;  // some huge number 		
 
 		axisArray[0] = lastAxis;
 		numAxis = 1;
 	}
 
 	public void init()
 	{
 		super.init();
 
 		if(axisArray != null){
 			for(int i=0; i<axisArray.length; i++){
 				if(axisArray[i] != null){
 					axisArray[i].init();
 				}
 			}
 		}
 		
 	}
 
     public void free()
     {
 		super.free();
 
 		if(axisArray != null){
 			for(int i=0; i<axisArray.length; i++){
 				if(axisArray[i] != null){
 					axisArray[i].free();
 				}
 			}
 		}
     }
 
 	public float getRange()
 	{
 		return (float)dispLen / scale;
 	}
 
 	public float getValue(int pos, Axis [] ax)
 	{
 		Axis curAxis;
 		float time;
 		int i;
 
 		curAxis = null;
 		for(i=0; i < numAxis; i++){
 			curAxis = axisArray[i];	    
 			if(pos*curAxis.axisDir > curAxis.drawnX*curAxis.axisDir && 
 			   pos*curAxis.axisDir < curAxis.axisDir*(curAxis.drawnX + curAxis.axisDir + 
 											curAxis.dispLen)){
 				break;
 			}
 		}
 		if(i != numAxis){
 			time = (pos - curAxis.drawnX) / curAxis.scale + curAxis.dispMin;
 			ax[0] = curAxis;
 			return time;
 		}
 
 		ax[0] = null;
 		return Maths.NaN;
 	}
 
 	public Axis getAxis(int i)
 	{
 		if((i >= 0) && (i < numAxis)){
 			return axisArray[i];
 		} else {
 			return null;
 		}
 
 	}
 
 	// This is a hack it just looks at the change in 
 	// dispMin and changes startPos by the same amount
 	// this doesn't make changing the which axis the dispMin 
 	// is relavent to easy very feasible
     public void setDispOffset(float startMin, int newDO)
     {
 		float newDispMin = startMin + (float)newDO / scale;
 		// update dispMin for safety
 		getDispMin();
 		startPos = startPos + (int)((newDispMin - dispMin) * scale);
 		dispMin = newDispMin;
 
 		notifyListeners(ORIGIN_CHANGE);
     }
 
     int startPos = 0;
     /* 
      * This is a very tricky piece of code where we change
      * the scale.  However we need to keep the startPos
      * in the same place
      */
     public void setScale(float newScale, boolean eScale)
     {
 		boolean saveScale = false;
 		if(!estimateScale && eScale){
 			oldScale = scale;
 			saveScale = true;
 		}
 		estimateScale = eScale;
 
 		scale = newScale;
 		axisDir = 1;
 		if(scale < (float)0) axisDir = -1;
 
 		/*
 		  Do I need to watch out for this stuff:
 
 		  axisDir = 1;
 		  if(scale < (float)0) axisDir = -1;
 		  setDispOffset(dispMin, 0);
 		  setStepSize();
 		  needCalcTics = true;       
 		*/
 
 		int i;
 		int oldScaleSP = 0;
 		int newXaxisStartPos = -1;
 		int curStartPos = 0;
 		Axis xa;
 
 		for(i =0; i < numAxis-1; i++){
 			xa = axisArray[i];
 			if(newXaxisStartPos == -1 &&
 			   startPos < (oldScaleSP + (int)(xa.max * xa.scale))){
 				newXaxisStartPos = curStartPos + (int)(((startPos - oldScaleSP) / xa.scale) * scale);
 			}
 			oldScaleSP += 10 + (int)(xa.max * xa.scale);
 			xa.setScale(scale, eScale);
 			curStartPos += 10 + (int)(xa.max * xa.scale);
 		}
 
 		xa = axisArray[i];
 		if(newXaxisStartPos == -1){
 			newXaxisStartPos = curStartPos + (int)(((startPos - oldScaleSP) / xa.scale) * scale);
 		}
 		xa.setScale(scale, eScale);
 
 		startPos = newXaxisStartPos;
 
 		labelExp = lastAxis.labelExp;
 		notifyListeners(SCALE_CHANGE);
     }
 
 	public float getDispMin()
 	{
 		int curStartPos = 0;
 		int xaScMax = 0;
 		int firstVisible = -1;
 
 		for(int i=0;i<numAxis;i++){
 			Axis xa = axisArray[i];
 			if(xa.max > (float)1E25)
 				// This is the active axis
 				xaScMax = (int)0x7FFFFFF - curStartPos - 10;
 			else
 				xaScMax = (int)(xa.max * xa.scale);
 			if(firstVisible == -1){
 				if(startPos < (curStartPos + xaScMax)){
 					firstVisible = i;
 					// Once we found a visible one we need to leave the
 					// curStartPos at the begining of this axis
 					break;
 				}
 				curStartPos += xaScMax + 10;
 			}
 		}
 
 		return dispMin = (startPos - curStartPos) / scale;
 	}
 
 	public void cacheAxis()
 	{
 		int curStartPos = 0;
 		Axis xa;
 		int cacheLen;
 		int i;
 
 		int startCachePos = startPos - dispLen;
 		curStartPos = 0;
 
 		// Find the first axis to cache
 		int firstCache = -1;
 		int xaScMax;
 		for(i=0;i<numAxis;i++){
 			xa = axisArray[i];
 			if(xa.max > (float)1E25)
 				// This is the active axis
 				xaScMax = (int)0x7FFFFFF - curStartPos - 10;
 			else
 				xaScMax = (int)(xa.max * xa.scale);
 			if(firstVisible == -1){
 				if(startCachePos < (curStartPos + xaScMax)){
 					firstCache = i;
 					// Once we found a visible one we need to leave the
 					// curStartPos at the begining of this axis
 					continue;
 				}
 				curStartPos += xaScMax + 10;
 			}
 		}
 
 		if(firstCache == -1){
 			return;
 		}
 
 		int endPoint = startCachePos + dispLen*3;
 		int cacheOffset = 0;
 		for(i=firstCache;i<numAxis;i++){
 			xa = axisArray[i];
 			if(curStartPos >= endPoint){
 				break;
 			}
 
 			if(curStartPos < startCachePos){
 				// This axis starts before the cache area so we need to offset it
 				cacheOffset = startCachePos - curStartPos;
 				curStartPos = startCachePos;
 				if(xa.max > (float)1E25){
 					// this is the active axis
 					cacheLen = dispLen*3;
 				} else {
 					cacheLen = (int)(xa.max * xa.scale) - (startCachePos - curStartPos);
 				}
 			} else {
 				// The axis starts in the cache area
 				// Set the dispMin correctly
 				cacheOffset = 0;
 				if(xa.max > (float)1E25){
 					// this is the active axis
 					cacheLen = dispLen*3;
 				} else {
 					cacheLen = (int)(xa.max * xa.scale);
 				}
 			} 
 	
 			if(cacheLen + (curStartPos - startCachePos) >= dispLen*3){
 				// The axis extends beyond the cache area
 				xa.setCacheAbs(cacheOffset, cacheLen);
 			} else {
 				// This axis ends before the end of the cache area 
 				xa.setCacheAbs(cacheOffset, cacheLen);
 			}
 	    
 			curStartPos += cacheLen + 10;
 		}
 	}
 
     public void scrollStartPos(int xDist)
 	{
 		startPos += xDist;
 		if(startPos < 0){
 			startPos = 0;
 		}
     }
 
 	public Axis getAxisFromBlob(int x, int y)
 	{
 		Axis xa;
 
 		for(int i=0;i<numAxis;i++){
 			xa = axisArray[i];
 			if(xa.drawnX == -1) continue;
 			if(x <= (xa.drawnX + xa.dispLen + xa.axisDir) &&
 			   x >= (xa.drawnX + xa.dispLen + xa.axisDir - xa.axisDir*xa.majTicSize) &&
 			   y >= drawnY &&
 			   y <= (drawnY + xa.majTicSize - xa.ticDir)){
 				return xa;
 			}
 		}
 		return null;
 	}
 
 	public final static int ADDED_AXIS = 4002;
 
 	// Return index of newAxis
 	// This creates a new axis two
 	// by necessity
     public void setAxisEndPoint(Axis a, float max)
 	{
 		// Note this may be called twice if there are two 
 		// lines on one axis
 
 		if(a.max > (float)1E25 || a.max < max) a.max = max;
 
 		if(a == lastAxis){
 			// we need to add a new axis
 			// beacause something has to fill the space to the 
 			// right of the closed axis
 			if(numAxis >= axisArray.length){
 				Axis [] newAxis = new Axis[(numAxis * 3)/ 2];
 				Vm.copyArray(axisArray, 0, newAxis, 0, numAxis);
 				axisArray = newAxis;
 			} 
 			axisArray[numAxis] = lastAxis = new Axis(type);
 			lastAxis.init();
 			lastAxis.max = (float)1E30;  // some huge number 
 			lastAxis.gridEndOff = gridEndOff;
 			lastAxis.setLength(dispLen);
 			lastAxis.setDispMin(MIN);
 			lastAxis.setScale(scale);
 			numAxis++;
 
 			notifyListeners(ADDED_AXIS);
 		}
     }
 
 	public void init(int x, int y)
 	{
 		drawnX = x;
 		drawnY = y;
 
 		// This is a hack
 		if(readExternalFlag){
 			setRange(lastDispMax - firstDispMin);
 			if(lastAxis != null){
 				lastAxis.setDispMin(firstDispMin);
 			}
 			startPos = (int)(firstDispMin * scale);
 		}		
 		draw(null, x, y);
 	}
 
 	// This is only valid once the axis has been drawn or
 	// readExternal has been called
 	public int firstVisible = -1;
 	public float firstDispMin = 0f;
 	public int lastVisible = -1;
 	public float lastDispMax = 0f;
 	public int readNumAxis = -1;
 
     public void draw(Graphics g, int xOriginOff, int yOriginOff)
 	{
 		int xaxisOffset = xOriginOff;
 		int curStartPos = 0;
 		Axis xa;
 		int axisLen;
 		int i;
 
 		drawnY = yOriginOff;
 		drawnX = xOriginOff;
 
 		curStartPos = 0;
 
 		// Find the first visible axis
 		// And set all the axis drawnX to -1;
 		firstVisible = -1;
 		int xaScMax;
 
 		for(i=0;i<numAxis;i++){
 			xa = axisArray[i];
 			xa.drawnX = -1;
 			if(xa.max > (float)1E25)
 				// This is the active axis
 				xaScMax = (int)0x7FFFFFF - curStartPos - 10;
 			else
 				xaScMax = (int)(xa.max * xa.scale);
 			if(firstVisible == -1){
 				if(startPos < (curStartPos + xaScMax)){
 					firstVisible = i;
 					// Once we found a visible one we need to leave the
 					// curStartPos at the begining of this axis
 					continue;
 				}
 				curStartPos += xaScMax + 10;
 			}
 		}
 
 		if(firstVisible == -1){
 			return;
 		}
 
 		int endPoint = startPos + dispLen;
 		int dispOffset = 0;
 		lastVisible = -1;
 		for(i=firstVisible;i<numAxis;i++){
 			xa = axisArray[i];
 			if(curStartPos >= endPoint){
 				break;
 			}
 
 			if(g != null)g.setColor(0,0,0);
 			if(curStartPos < startPos){
 				// This axis starts before the visible area so we need to offset it
 				dispOffset = startPos - curStartPos;
 				xa.setDispOffset(0f, dispOffset, false);
 				curStartPos = startPos;
 				if(xa.max > (float)1E25){
 					// this is the active axis
 					axisLen = dispLen;
 				} else {
 					axisLen = (int)(xa.max * xa.scale) - dispOffset;
 				}
 			} else {
 				// The axis starts in the visible area
 				// Need to draw the beginning line of the axis
 				// draw the next axis
 				if(g != null) g.drawLine(xaxisOffset + curStartPos - startPos, yOriginOff, 
 						   xaxisOffset + curStartPos - startPos, yOriginOff + gridEndOff);
 
 				// And set the dispMin correctly
 				xa.setDispOffset(MIN,0,false);
 				if(xa.max > (float)1E25){
 					// this is the active axis
 					axisLen = dispLen;
 				} else {
 					axisLen = (int)(xa.max * xa.scale);
 				}
 			} 
 	
 			if(axisLen + (curStartPos - startPos) >= dispLen){
 				// The axis extends beyond the visible area
 				xa.dispLen = dispLen - (curStartPos - startPos);
 			} else {
 				// This axis ends before the end of the visible area so draw an end line
 				xa.dispLen = axisLen;				
 			}
 	    
 			xa.gridEndOff = gridEndOff;
 			if(g == null) xa.init(xaxisOffset + (curStartPos - startPos), yOriginOff);
 			else xa.draw(g, xaxisOffset + (curStartPos - startPos), yOriginOff);
 			curStartPos += axisLen + 10;
 		}
 		lastVisible = i-1;
     }
 
 	public void reset()
 	{
 		axisArray[0] = lastAxis;		
 		lastAxis.dispLen = dispLen;
 		lastAxis.setDispMin(MIN);
 		lastAxis.max = (float)1E30;  // some huge number 		
 		dispMin = MIN;
 
 		numAxis = 1;
 		startPos = 0;
 	}
 
 	boolean readExternalFlag = false;
 	public void readExternal(DataStream ds)
 	{
 		readExternalFlag = true;
 
 		readNumAxis = ds.readInt();
 		firstVisible = ds.readInt();
 		if(firstVisible >= 0){
 			firstDispMin = ds.readFloat();
 			lastVisible = ds.readInt();
 			if(lastVisible >= 0){
 				lastDispMax = ds.readFloat();
 			}
 		}
 
 		String labelStr = ds.readString();
 
 		int labelUnitCode = ds.readInt();
 		CCUnit labelUnit = null;
 		if(labelUnitCode >= 0){
 			labelUnit = CCUnit.getUnit(labelUnitCode);
 		}
		if(labelStr != null){
			setAxisLabel(labelStr, labelUnit);
		}
 
 		autoLabel = ds.readBoolean();
 	}
 
 	// This will only be valid after the axis has been drawn
 	// this might be a problem
     public void writeExternal(DataStream ds)
     {
 		if(drawnX == -1 && !readExternalFlag){
 			// This is an odd way of doing it but hopefully it works.
 			ds.writeInt(1);
 			ds.writeInt(0);
 			ds.writeFloat(0f);
 			ds.writeInt(0);
 			ds.writeFloat(getDispMax());
 		} else if(drawnX == -1 && readExternalFlag){
 			ds.writeInt(readNumAxis);
 			ds.writeInt(firstVisible);
 			if(firstVisible >= 0){
 				ds.writeFloat(firstDispMin);
 				ds.writeInt(lastVisible);
 				if(lastVisible >= 0){
 					ds.writeFloat(lastDispMax);
 				}
 			}
 		} else {
 			ds.writeInt(numAxis);
 			ds.writeInt(firstVisible);
 			if(firstVisible >= 0){
 				ds.writeFloat(axisArray[firstVisible].dispMin);
 				ds.writeInt(lastVisible);
 				if(lastVisible >= 0){
 					ds.writeFloat(axisArray[lastVisible].getDispMax());
 				}
 			}
 		}
 
 		ds.writeString(axisLabelStr);
 
 		if(axisLabelUnit == null) ds.writeInt(-1);
 		else ds.writeInt(axisLabelUnit.code);
 
 		ds.writeBoolean(autoLabel);
     }
 
 
 }
