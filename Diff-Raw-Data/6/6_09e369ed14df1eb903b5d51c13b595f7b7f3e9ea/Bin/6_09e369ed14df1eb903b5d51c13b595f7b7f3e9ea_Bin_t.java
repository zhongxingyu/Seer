 /*
   Copyright (C) 2001 Concord Consortium
 
   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.
 
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package graph;
 
 import waba.ui.*;
 import waba.fx.*;
 import waba.io.*;
 import waba.sys.*;
 import waba.util.*;
 import org.concord.waba.extra.event.*;
 import extra.util.*;
 
 public class Bin
     implements DecoratedValue
 {
     public static int START_DATA_SIZE = 10000;
 
     int [] points = null;
     int numPoints;
     int lastPlottedPoint;
     int c;
     int collection;
     int curX;
     int sumY;
     int numXs;
     int minPtY;
     int maxPtY;
     float refX = 0f;
     float refY = 0f;
     public float minX, minY, maxX, maxY;
     int [] color = {255,0,0};
     public int xaIndex = 0;
     
     public String label;
     public LargeFloatArray lfArray = new LargeFloatArray();
     public String description = null;
     public Time time = null;
 	float curXscale, curYscale;
 
 
     public Bin(int xIndex)
     {
 		xaIndex = xIndex;
 
 		// We store three ints for each point
 		// (x),(avgY),(maxOff << 16 | -minOff)
 		// points = new int [START_DATA_SIZE*3];
 
 		// System.out.println("Creating bin with size:" + START_DATA_SIZE);
 
 		reset();
     }
 
     public String getLabel()
     {
 		return label;
     }
 
     public float getValue()
     {
 		if(lfArray.getCount() == 0) return 0f;
 		return lfArray.getFloat(lfArray.getCount()-1) + lfArray.ref;
     }
 
     public float getTime()
     {
 		return getCurX();
     }
 
     public Color getColor()
     {
 		return null;
     }
 
     public float getCurX()
     {
 		if(lfArray.getCount() == 0) return 0f;
 		return maxX;
     }
 
     public int getNumVals()
     {
 		return lfArray.getCount();
     }
 
 	int remainder;
 	int remainderSum;
 
     public void recalc(float xscale, float yscale)
     {
 		curXscale = xscale;
 		curYscale = yscale;
 
 		lastCalcValue = 1;
 
 		update();
     }
 
     int lastCalcValue = 0;
 
     public boolean update()
     {
 		int i;
 		int numValues = lfArray.getCount();
 		int newX, newY;
 		int avgY;
 
 		if(numValues < 2 ||
 		   numValues - lastCalcValue < 1){
 			return false;
 		} else if(lastCalcValue == 1){
 			// we've got points for the first time
 			// reset again
 			resetPts();
 		}
 
 		int curPtPos;
 
 		curPtPos = (numPoints-1)*3;
 
 		i=lastCalcValue;
 	
 		remainderSum += remainder;
 		newX = curX + remainderSum/denom;
 		newY = (int)(lfArray.getFloat(i) * curYscale);
 		i++;		
 
 		while(true){
 			while(newX == curX && i < numValues){
 				sumY += newY;
 				numXs++;
 				if(newY > maxPtY) maxPtY = newY;
 				else if(newY < minPtY) minPtY = newY;
 
 				remainderSum += remainder;
 				newX = curX + remainderSum/denom;
 				newY = (int)(lfArray.getFloat(i) * curYscale);
 				i++;		
 
 			}
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 			if(i >= numValues) break;
 			curX = newX;
 			remainderSum = remainderSum % denom;
 			numXs = 0;
 			maxPtY = minPtY = newY;
 			sumY = 0;
 		}
 
 		if(curX != newX){
 			// This means we have a newX 
 			// so what is curX
 			// it is the old x.
 			// if we don't change it then we need to 
 			// reparse the newX point
 			// however we should change it 
 			curX = newX;
 			remainderSum = remainderSum % denom;
 			minPtY = maxPtY = sumY = newY;
 			numXs = 1;
 
 			// We increase the i to move the lastCalcVal next
 			i++;
 
 			// This is just an estimate
 			// so we need to set the curPtPos to 
 			// check this point the next time
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 		} else {
 			// They match so we should update the sums and xs
 
 			sumY += newY;
 			numXs++;
 			if(newY > maxPtY) maxPtY = newY;
 			else if(newY < minPtY) minPtY = newY;
 
 			i++;
 
 			// update the points.
 			curPtPos-=3;
 			points[curPtPos++] = curX;
 			avgY = sumY / numXs;
 			points[curPtPos++] = avgY;
 			points[curPtPos++] = (maxPtY - avgY) << 16 | (avgY - minPtY);
 
 		}
 		// else the curX == newX so 
 		// we should be ok
 
 
 
 		numPoints = curPtPos / 3;
 		lastCalcValue = i-1;
 
 		return true;
     }
 
 
     public float dT = 0f;
     int sampSize = 1;
 
     public boolean dataReceived(DataEvent dataEvent)
     {
 		if(lfArray.getCount() == 0){
 			dT = dataEvent.getDataDesc().getDt();
 			sampSize = dataEvent.getDataDesc().getChPerSample();
 			refY = dataEvent.data[dataEvent.dataOffset];
 			lfArray.ref = refY;
 			minX = 0;
 			if(points == null){
 				points = new int [START_DATA_SIZE*3];
 			}
 		}
 
 		maxX += dT*dataEvent.numbSamples;
 	
 		boolean ret = lfArray.addFloats(dataEvent.data, dataEvent.dataOffset, 
 										sampSize, dataEvent.numbSamples);
 
 		minY = lfArray.min;
 		maxY = lfArray.max;
 		return ret;
     }
 
 	int denom = 0;
 
 	void resetPts()
 	{
		if(curXscale != 0f && dT != 0f){
			denom = (int) (1000f/curXscale/dT);
		} else {
			denom = 1;
		}
 		remainder = (int)(dT*curXscale*denom);
 		remainderSum = 0;
 		numXs = 0;
 		curX = 0;
 		minPtY = maxPtY = sumY = (int)(lfArray.getFloat(0)* curYscale);
 		lastCalcValue = 1;
 		numXs = 1;
 		numPoints = 1;
 	}
 
     void reset()
     {
 		int i;
 
 		lfArray.clear();
 
 		minX = minY = 1;
 		maxY = -1;
 		maxX = 0f;
 
 		for(i=0; i<100; i++){
 			minX *= (float)10;
 			minY *= (float)10;
 			maxY *= (float)10;
 		}
 
 		lastPlottedPoint = -1;
 
 		resetPts();
     }
 
     public boolean getValue(float time, float [] value)
     {
 		int i;
 
 		if(time > 0f && time < maxX){
 			value[0] = lfArray.getFloat((int)(time / dT )) + refY;
 			return true;
 		}
 
 		return false;
     }
 
     public int numDataChunks(){return 1;}
 
     public DataEvent getDataChunk(int index)
     {
 		DataEvent dEvent;
 		DataDesc dDesc = new DataDesc(dT, 1);
 
 		float [] data = lfArray.getFloats(0, lfArray.getCount());
 
 		int numValues = lfArray.getCount();
 		for(int i=0; i<numValues; i++){
 			data[i] += refY;
 		}
 
 		dEvent = new DataEvent(DataEvent.DATA_RECEIVED, 
 							   0, data , dDesc);
 		dEvent.dataOffset = 0;
 		dEvent.numbSamples = numValues;
 
 		return dEvent;
     }
 	    
 }
 
 
