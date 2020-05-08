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
 
 import waba.fx.*;
 import waba.ui.*;
 import waba.util.*;
 import waba.sys.*;
 import extra.util.Maths;
 import extra.util.*;    
 import org.concord.waba.extra.event.*;
 
 public class Axis
 {
     public final static int X_SCREEN_AXIS = 0;
     public final static int Y_SCREEN_AXIS = 1;
 
     public final static int TOP = 0;
     public final static int BOTTOM = 1;
     public final static int LEFT = 2;
     public final static int RIGHT = 3;
 
     int orient = X_SCREEN_AXIS;
 
     TextLine label = new TextLine("0");
 
     /*  The absolute min and max
      *  The Axis is designed to show a portion of its full length
      *  These values are the full length values.
      *  the disp* values are the "portion". 
      */
     float min;
     int length;
 
     // This is for external use it does not affect the drawing
     float max;
 
     // Range to display
     public float dispMin;
     public int dispLen;
     
     // If the absolute min is at 0 in screen coordiates
     // this is the position of dispMin 
     public int dispOffset;
 
     // (screen units)/(input units)
     public float scale;
 
     // 1 or -1 this gives the sign of the scale
     int axisDir;
 
     // This is used to compute the graph grid
     // You can adjust this to compensate for diff screen sizes
     // and resolutions
     int minMajTicSpacing = 20;
 
     // This is computed from the length and the minTicSpacing
     // it should be a relatively nice number (1,2,5,10) in the correct 
     // magnitude of course
    float majTicStep;
 
     // This is used to compute the minor grid spacing
     // Hopefully this will be picked integently 
     int numMinTics = 1;
 
     // Whether the tics go in the direction of the screen coords
     // or opposite them
     int ticDir = 1;
 
     int gridEndOff = 100;
     int gridDir = -1;
 
     int [] gridMinCol = {205,205,205};
     int [] gridMajCol = {170,170,170};
     int [] axisCol = {0,0,0};
 
     int labelEdge, labelOff;
 
     int minTicSize = 3;
     int majTicSize = 5;
 
 	int maxDigits = 3;
 
     int drawnX = -1;
     int drawnY = -1;
     int drawnOffset = 1;
 
     boolean nonNegative = true;
 
 	TextLine axisLabel;
 	String axisLabelStr;
 	CCUnit axisLabelUnit;
 
 	Vector scaleListeners = new Vector();
 
     public Axis(int type)
     {
 		this.min = dispMin = 0f;
 		label.maxDigits = 2;
 		dispOffset = 0;	
 		needCalcTics = true;
 
 		switch(type){
 		case BOTTOM:
 			length = dispLen = 100;	
 			axisDir = 1;
 			ticDir = 1;
 			orient = Axis.X_SCREEN_AXIS;
 			labelOff = 7;
 			labelEdge = TextLine.TOP_EDGE;
 			minMajTicSpacing = 40;
 			axisLabel = new TextLine("", TextLine.RIGHT);
 			break;
 		case LEFT:
 			length = dispLen = -100;	
 			axisDir = -1;
 			ticDir = -1;
 			orient = Axis.Y_SCREEN_AXIS;
 			labelOff = -6;
 			labelEdge = TextLine.RIGHT_EDGE;
 			axisDir = -1;
 			gridDir = 1;
 			nonNegative = false;
 			axisLabel = new TextLine("", TextLine.UP);
 			break;
 		}
 
 		scale = 1f*(float)axisDir;
     }
 
 	public void setLength(int len)
 	{
 		length = dispLen = len;	
 	}
 
 	public void setMaxDigits(int axisDigits)
 	{
 		maxDigits = axisDigits;
 	}
 
 	ActionEvent scaleEvent = new ActionEvent(this, null, null);
 	public final static int SCALE_CHANGE = 3000;
 
 	public void addActionListener(ActionListener al)
 	{
 		scaleListeners.add(al);
 	}
 
     void notifyListeners()
 	{
 		scaleEvent.type = SCALE_CHANGE;
 		for(int i=0; i<scaleListeners.getCount(); i++){
 			ActionListener al = (ActionListener)scaleListeners.get(i);
 			al.actionPerformed(scaleEvent);
 		}
 	}
 
 	public void setAxisLabel(String label, CCUnit unit)
 	{
 		axisLabelStr = label;
 		axisLabelUnit = unit;
 	}
 
     public void setRange(float min, float range)
     {
 		setDispMin(min);
 		setRange(range);
     }
 
     public void setRange(float range)
     {		
 		setScale((dispLen - axisDir) / range);
     }
 
 	public void drawAxisLabel(Graphics g, int edgePos)
 	{
 		String unitStr;
 		if(axisLabelUnit != null){
 			unitStr = " (" + axisLabelUnit.abbreviation + ")";
 		} else {
 			unitStr = "";
 		}
 
 		if(labelExp != 0){
 			axisLabel.setText(axisLabelStr + unitStr + "  10^"+ labelExp);
 		} else {
 			axisLabel.setText(axisLabelStr + unitStr);
 		}
 		if(labelEdge == TextLine.RIGHT_EDGE){
 		   axisLabel.drawCenter(g, edgePos, drawnY + dispLen/2, TextLine.LEFT_EDGE);
 		} else if(labelEdge == TextLine.TOP_EDGE){
 			axisLabel.drawCenter(g, drawnX + dispLen/2, edgePos, TextLine.BOTTOM_EDGE);
 		}
 	}
 
     public void free()
     {
 		int i;
 
 		if(label != null)label.free();
 		if(majTicLabels != null){
 			for(i=0; i<majTicLabels.length; i++){
 				if(majTicLabels[i] != null){
 					majTicLabels[i].free();
 				}
 			}
 		}
 		if(axisLabel != null)axisLabel.free();
     }
 
 	public float getValue(int pos)
 	{
 		if(orient == Axis.Y_SCREEN_AXIS){
 			if(pos*axisDir > drawnY*axisDir &&
 			   pos*axisDir < axisDir*(drawnY + axisDir + dispLen)){
 				return Maths.NaN;
 			}
 
 			return (pos - drawnY) / scale + dispMin;
 		} else {
 			if(pos*axisDir > drawnX*axisDir && 
 			   pos*axisDir < axisDir*(drawnX + axisDir + dispLen)){
 				return Maths.NaN;
 			}
 
 			return (pos - drawnX) / scale + dispMin;
 		}
 	}
 
 	public float getDispMax()
 	{
 		return dispMin+dispLen/scale;
 	}
 
     /**
      * SERC: this comes from the SpecialFunction class
      */
 	final static float log10(float x) {
 		int radix = 0;
 
 		if( x <= (float)0.0 ) return 0;
 	 
 
 		while(x < (float)1.0) {
 			x *= 10f;
 			radix--;
 		}
 
 		while(x >= (float)10.0) {
 			x /= 10f;
 			radix++;
 		}
 
 		return radix;
 	}
 
     final static float floor(float x) {
 		if(x >= 0)
 			return (float)((int)x);
 		else 
 			return (float)((int)(x- (float)1.0));
     }
 
     final static float exp10(float val, int exp)
     {
 		int mult = 1;
 		int i;
 
 		if(exp >= 0){
 			for(i = 0; i < exp; i++) mult *= 10;
 			return val * (float)mult;
 		} else {
 			for(i = 0; i > exp; i--)  mult *= 10;
 			return val / (float)mult;
 		}
     }
 
     /*
      * This should only be called when the scale changes or the 
      * Idealy it should return the same size for the same scale
      *  (given the same tic spacing)
      */
     int labelExp = 0;
 	int labelExpStep = 3;
 	float labelExpStepVal = 1000f;
     float labelTicStep = 1;
 	int labelTicStepExp = 0;
 	int labelTicStepRawExp = 0;
 
     void setStepSize()
     {
 		float rawStep = axisDir * (float)minMajTicSpacing / scale;
 		int stepSign = 1;
 		int exponent;
 		int i;
 
 		if(rawStep < (float)0){
 			stepSign = -1;
 			rawStep = -rawStep;
 		}
 
 		exponent = (int)log10(rawStep);
 
 		rawStep = exp10(rawStep, -exponent);
 		// rawStep should now be in the range of 1 -> 10
 		// seems we can do a trick here to cut out the last special case
 		// but I don't think it will buy us much speed
 
 		if(rawStep < 2) labelTicStep = (float)2;
 		else if(rawStep < 5) labelTicStep = (float)5;
 		else if(rawStep < 10){
 			labelTicStep = (float)1;
 			exponent++;
 		}
 
 		labelTicStepRawExp = exponent;
 
 		majTicStep = exp10(labelTicStep, exponent);
 		if(exponent > 0) labelTicStepExp = exponent / labelExpStep * labelExpStep;
 		else labelTicStepExp = (((exponent + 1) / labelExpStep) - 1) * labelExpStep;
 		labelExp = labelTicStepExp;
 
 		setFirstTic();
     }
 
 	int getRoundExp(float val)
 	{
 		float x = val;
 		if(x < 0) x = -x;
 		if(x == 0f) return 0;
 
 		int radix = 0;
 
 		while(x < (float)1.0) {
 			x *= labelExpStepVal;
 			radix--;
 		}
 		
 		while(x >= labelExpStepVal) {
 			x /= labelExpStepVal;
 			radix++;
 		}
 
 		return radix*labelExpStep;
 	}
 
     float firstTic;
     float firstLabelTic;
 
     void setFirstTic()
     {
 		float rawMinSteps = min / majTicStep;
 		int intFloor = (int)rawMinSteps;
 
 		if(rawMinSteps < (float)0){
 			firstTic = (float)(intFloor - 1) * majTicStep;
 		} else {
 			firstTic = (float)intFloor * majTicStep;
 		}
 
 		int firstTicExp = getRoundExp(firstTic);
 		int approxLastTicExp = getRoundExp(length/scale + firstTic);
 		int maxEndExp = firstTicExp;
 		if(maxEndExp < approxLastTicExp) maxEndExp = approxLastTicExp;
 
 		if(maxEndExp > labelTicStepExp){
 			labelExp = maxEndExp;
 		} else {
 			labelExp = labelTicStepExp;
 		}
 		labelTicStep = exp10(majTicStep, -labelExp);
 		firstLabelTic = exp10(firstTic, -labelExp);
 		if(labelExp > labelTicStepRawExp){
 			int decDigits = labelExp - labelTicStepRawExp;
 			if(decDigits > (maxDigits - 1)) decDigits = maxDigits - 1;
 			label.minDigits = label.maxDigits = decDigits;
 		} else {
 			label.minDigits = 0;
 			label.maxDigits = 0;
 		}
 
     }
 	
     int maxLabelOff = 0;
     int minLabelOff = 0;
     float ticStep = (float)1;
     int [] ticOffsets = null;
     TextLine [] majTicLabels = null;
     int maxLabelSize;
 
     void computeTicArrays()
     {
 		int i;
 
 		ticStep = majTicStep / (float)(numMinTics + 1);
 		float lTicStep = labelTicStep / (float) (numMinTics + 1);
 
 		float range = length / scale;
 
 		if(range < 0){
 			range = (float)0;
 		}
 
 		int maxNumTics = (int)(range / ticStep) + 4;
 
 		// To be super efficient we should not reallocate
 		// if we don't have to
 		ticOffsets = new int[maxNumTics];
 
 		if(majTicLabels != null){
 			for(i=0; i<majTicLabels.length; i++){
 				if(majTicLabels[i] != null){
 					majTicLabels[i].free();
 				}
 			}
 		}
 		majTicLabels = new TextLine[maxNumTics];
 
 		float curPos = firstTic;
 		float curLabelVal = firstLabelTic;
 		float max = min + range;
 		i = 0;
 
 		int offset = i;
 		TextLine curLabel;
 		int newLabelSize, newLabelMinOff, newLabelMaxOff;
 
 		if(orient == X_SCREEN_AXIS){
 			maxLabelSize = 0;
 			while(i < ticOffsets.length){
 				ticOffsets[i-offset] = (int)((curPos - min) * scale);
 				if(i % (numMinTics + 1) == 0){
 					// Its a major tic
 					// make TextLine
 					majTicLabels[i - offset] = curLabel = 
 						new TextLine(label.fToString(curLabelVal));
 					newLabelSize = curLabel.height;
 					newLabelMinOff = curLabel.getXOffset(labelEdge);
 					newLabelMaxOff = newLabelMinOff + newLabelSize;
 					if(newLabelMaxOff > maxLabelOff)
 						maxLabelOff = newLabelMaxOff;
 					if(newLabelMinOff < minLabelOff)
 						minLabelOff = newLabelMinOff;
 				} else {
 					majTicLabels[i - offset] = null;
 				}
 				i++;
 				curLabelVal += lTicStep;
 				curPos += ticStep;		
 			}
 		} else {
 			maxLabelSize = 0;
 			while(i < ticOffsets.length){
 				ticOffsets[i-offset] = (int)((curPos - min) * scale);
 				if(i % (numMinTics + 1) == 0){
 					// Its a major tic
 					// make TextLine
 					majTicLabels[i - offset] = curLabel = 
 						new TextLine(label.fToString(curLabelVal));
 					newLabelSize = curLabel.width;
 					newLabelMinOff = curLabel.getYOffset(labelEdge);
 					newLabelMaxOff = newLabelMinOff + newLabelSize;
 					if(newLabelMaxOff > maxLabelOff)
 						maxLabelOff = newLabelMaxOff;
 					if(newLabelMinOff < minLabelOff)
 						minLabelOff = newLabelMinOff;
 
 				} else {
 					majTicLabels[i - offset] = null;
 				}
 				i++;
 				curPos += ticStep;
 				curLabelVal += lTicStep;
 			}
 		}
     }
 
 
     // This holds the dispMin fixed and changes the scale around that
     public void setScale(float s)
     {
 		scale = s;
 		axisDir = 1;
 		if(scale < (float)0) axisDir = -1;
 		setDispOffset(dispMin, 0);
 		setStepSize();
 		needCalcTics = true;
 		notifyListeners();
     }
 
     public void setDispOffset(float startMin, int newDO)
     {
 		dispMin = startMin + (float)newDO / scale;
 		dispOffset = (int)((dispMin - min) * scale);
 	
 		if((axisDir*(dispOffset + dispLen) > axisDir*length) ||
 		   (dispMin < min)){
 			min = dispMin - (dispLen/scale);
 			length = 3*dispLen;
 			dispOffset = (int)((dispMin - min) * scale);
 			setFirstTic();
 			needCalcTics = true;
 		}
     }
 
     public void setDispMin(float newDM)
     {
 		setDispOffset(newDM, 0);
     }
 
     boolean needCalcTics = false;
 
     LineGraph lGraph = null;
 
     // have the two arrays
     // tic offsets and tic labels
     // also have first dispTic
     public void draw(Graphics g, int x, int y)
     {
 		drawnX = x;
 		drawnY = y;
 
 		if(lGraph != null){
 			lGraph.endTime = Vm.getTimeStamp();
 			g.drawText(lGraph.endTime - lGraph.startTime + "", lGraph.xText, lGraph.yText);
 			lGraph.startTime = lGraph.endTime;
 			lGraph.xText += 20;
 		}
 
 		if(axisDir*dispLen*11 < axisDir*length){
 			// we are tracking way too many tics here.
 			length = 3*dispLen;
 			min = dispMin - (dispLen/scale);
 			if(nonNegative && min < (float)0){
 				min = (float)0;
 			}
 			setDispOffset(dispMin, 0);
 			setFirstTic();
 			needCalcTics = true;
 		}
 	
 		if(needCalcTics){
 			computeTicArrays();
 			needCalcTics = false;
 		}
 
 		// Find first valid tic
 		int i = 0;
 		while((i < ticOffsets.length) && 
 			  (ticOffsets[i]*axisDir < dispOffset*axisDir))
 			i++;
 
 
 
 		int curPos;
 		int firstIndex = i;
 
 		int lastIndex;
 		int endPos = dispOffset + dispLen - axisDir;
 		int majTicEndOff = ticDir * majTicSize;
 		int minTicEndOff = ticDir * minTicSize;
 		int j;
 	
 		if(orient == X_SCREEN_AXIS){
 			drawnOffset = x - dispOffset;
 	    
 			g.translate(x, y);
 
 			// draw axis line
 			g.setColor(axisCol[0],axisCol[1],axisCol[2]);
 			g.drawLine(0,0, dispLen + axisDir, 0);
 			for(j=0; j <= majTicSize; j++){
 				g.drawLine(dispLen + axisDir - axisDir*j,0,
 						   dispLen + axisDir - axisDir*j, majTicEndOff - ticDir);
 			}
 
 			g.translate(axisDir - dispOffset, gridDir);
 		       
 
 
 			// draw tic marks and labels
 			while((i < ticOffsets.length) &&
 				  ((curPos = ticOffsets[i])*axisDir <= endPos*axisDir)){
 				if(majTicLabels[i] == null){
 					g.drawLine(curPos, ticDir, curPos, minTicEndOff);
 				} else {
 					g.drawLine(curPos, ticDir, curPos, majTicEndOff);
 					majTicLabels[i].drawCenter(g, curPos, labelOff, labelEdge);
 				}
 				i++;
 			}
 
 			lastIndex = i;
 
 			// draw Minor GridLines
 			g.setColor(gridMinCol[0],gridMinCol[1],gridMinCol[2]);	    
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] == null)
 					g.drawLine(curPos, 0, curPos, gridEndOff);
 			}
 
 			// draw Major GridLines
 			g.setColor(gridMajCol[0],gridMajCol[1],gridMajCol[2]);
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] != null){
 					g.drawLine(curPos, 0, curPos, gridEndOff);
 				}
 			}
 
 
 			g.translate(-(x + axisDir - dispOffset), -(y + gridDir));
 		} else {
 			if(lGraph != null){
 				lGraph.endTime = Vm.getTimeStamp();
 				g.drawText(lGraph.endTime - lGraph.startTime + "", lGraph.xText, lGraph.yText);
 				lGraph.startTime = lGraph.endTime;
 				lGraph.xText += 20;
 			}
 
 			drawnOffset = y + axisDir - dispOffset;
 
 			g.translate(x, y);
 
 			// draw axis line
 			g.setColor(axisCol[0],axisCol[1],axisCol[2]);
 			g.drawLine(0,0, 0, dispLen + axisDir);
 			for(j=0; j<= majTicSize; j++){
 				g.drawLine(0, dispLen + axisDir - axisDir*j,
 						   majTicEndOff - ticDir, dispLen + axisDir - axisDir*j);
 			}
 
 			g.translate(gridDir, axisDir - dispOffset);
 
 			// draw tic marks and labels	    
 			while((i < ticOffsets.length) &&
 				  ((curPos = ticOffsets[i])*axisDir <= endPos*axisDir)){
 				if(majTicLabels[i] == null){
 					g.drawLine(ticDir, curPos, minTicEndOff, curPos);
 				} else {
 					g.drawLine(ticDir, curPos, majTicEndOff, curPos);
 					majTicLabels[i].drawCenter(g, labelOff, curPos, labelEdge);
 				}
 				i++;
 			}
 
 			lastIndex = i;
 
 			if(lGraph != null){
 				g.translate(-(x + gridDir), -(y + axisDir - dispOffset));
 				lGraph.endTime = Vm.getTimeStamp();
 				g.drawText(lGraph.endTime - lGraph.startTime + "", lGraph.xText, lGraph.yText);
 				lGraph.startTime = lGraph.endTime;
 				lGraph.xText += 20;
 				g.translate((x + gridDir), (y + axisDir - dispOffset));
 			}
 
 			// draw Minor GridLines
 			g.setColor(gridMinCol[0],gridMinCol[1],gridMinCol[2]);	    
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] == null)
 					g.drawLine(0, curPos, gridEndOff, curPos);
 			}
 
 			// draw Major GridLines
 			g.setColor(gridMajCol[0],gridMajCol[1],gridMajCol[2]);	    
 			for(i=firstIndex; i< lastIndex; i++){
 				curPos = ticOffsets[i];
 				if(majTicLabels[i] != null)
 					g.drawLine(0, curPos, gridEndOff, curPos);
 			}
 
 
 			g.translate(-(x + gridDir), -(y + axisDir - dispOffset));
 		}	    
 
     }
 
     // Need to have functions to calc width without drawing
     // Do we need a g for this??
     int getOutsideSize()
     {
 		int minOffset, maxOffset;
 
 		// Check if the real size changed
 		/*
 		  if(minLast != min){
 		  minLast = min;
 		  dispMin = min;
 		  setFirstTic();
 		  needCalcTics = true;
 		  }
 		*/	
 
 		if(needCalcTics){
 			computeTicArrays();
 			needCalcTics = false;
 		}
 
 		/* 
 		 * find max or min of all graph 
 		 * element offsets.
 		 * Elements:
 		 *   labels, ticMarks
 		 * The big problem is labels
 		 *  the edge is set and the offset is set
 		 *  so we'd have to switch on each edge to figure out
 		 *  the relevent offset.
 		 *
 		 * This should incorporate tic marks if the label happens
 		 *   to be on the inside
 		 */
 		if(gridEndOff > 0){
 			// find min
 			return labelOff + minLabelOff;
 		} else {
 			// find max
 			return labelOff + maxLabelOff;	    
 		}
     }
 
 }
