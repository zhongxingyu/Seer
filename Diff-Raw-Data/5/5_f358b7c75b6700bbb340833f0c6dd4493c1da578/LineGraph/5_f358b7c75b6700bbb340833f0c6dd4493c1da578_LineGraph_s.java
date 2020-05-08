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
 import org.concord.waba.extra.util.*;
 
 public class LineGraph extends Graph2D
 	implements ActionListener
 {
     public final static int DEFAULT_STOR_SIZE = 5;
 
     public final static float X_MIN = 0;
     public final static float X_MAX = 100;
     public final static float Y_MIN = -200;
     public final static float Y_MAX = 200;
 
     boolean palm = false;
     public int annotTopY = 3;
     public int annotPadding = 1;
     public int topPadding = 5 + Annotation.height + annotPadding;
     public int rightPadding = 8;
 
     int xOriginOff, yOriginOff;
     public int dwWidth, dwHeight;
     public SplitAxis xaxis = null;
     public Axis yaxis = null;
 
     int platform = 0;
 
     int width, height;
 
     int [][] lineColors = { {255,  0,  0},   // red
 							{0,  255,  0},   // green
 							{0,  0,  255},   // blue
 							{255, 255, 0}, // yellow
 							{255, 0, 255}, // purple
 							{0, 255, 255},}; // turquois
 
     protected int length = 0;
 	Vector bins = new Vector();
 
     Bin curBin;
 
     boolean axisFlipped = false;
     Object [][] graphLayout = new Object[3][3];
     Object annotSection = new Object();
 
     // These hold the top left corner of the dataWin in 
     // screen coord.
     int dwX, dwY;
 
     public boolean profile = false;
 
     public LineGraph(int w, int h, int xOrigin, int yOrigin,
 					 SplitAxis xAx, Axis yAx)
 	{ 
 		int i;
 		width = w;
 		height = h;
 
 		dwX = xOrigin;
 		dwY = yOrigin;
 
 		dwWidth = w - dwX - 10;
 		if(profile)
 			dwHeight = h - dwY - 40;
 		else
 			dwHeight = h - dwY - 30;
 
 		yOriginOff = dwHeight + dwY;
 		xOriginOff = dwX;
 
 		switchXAxis(xAx);
 		switchYAxis(yAx);
 
 		reset();
 
 		graphLayout[1][0] = graphLayout[2][0] = annotSection;
 		graphLayout[1][1] = this;
 		graphLayout[2][1] = graphLayout[0][2] = null;
     }
 
 	public void setupYAxis(Axis yAx)
 	{
 		// We want to preserve the range of this axis
 		// so we need to save the max
 		float max = yAx.getDispMax();
 		yAx.setLength(-dwHeight);
 		yAx.gridEndOff = dwWidth-1;
 		yAx.setRange(max - yAx.getDispMin());
 		yAx.init(xOriginOff,yOriginOff);
 	}
 
 	public void setupXAxis(Axis xAx)
 	{
 		// We want to preserve the range of this axis
 		// so we need to save the max
 		float max = xAx.getDispMax();
 		xAx.setLength(dwWidth);
 		xAx.gridEndOff = -dwHeight+1;
 		xAx.setRange(max - xAx.getDispMin());
 		xAx.init(xOriginOff,yOriginOff);
 	}
 
 	public void switchXAxis(SplitAxis xAx)
 	{
 		if(xAx != xaxis){
 			if(xaxis != null) xaxis.removeActionListener(this);
 			xaxis = xAx;
 			setupXAxis(xAx);
 			xaxis.addActionListener(this);
 			graphLayout[1][2] = graphLayout[2][2] = xaxis;
 		}		
 	}
 
 	public void switchYAxis(Axis yAx)
 	{
 		if(yAx != yaxis){
 			if(yaxis != null) yaxis.removeActionListener(this);
 			yaxis = yAx;
 			setupYAxis(yAx);
 			yaxis.addActionListener(this);
 			graphLayout[0][1] = graphLayout[0][0] = yaxis;
 		}
 	}
 
 	public void free(){}
 
     // need to find correct axis
     public Annotation addAnnot(String label, int pos)
     {
 		Axis [] aPtr = new Axis [1];
 		float time = xaxis.getValue(pos, aPtr);
 
 		if(aPtr[0] != null){
 			return addAnnot(label, time, aPtr[0]);
 		}
 
 		return null;
     }
 
     public Annotation addAnnot(String label, float time)
     {
 		return addAnnot(label, time, xaxis.lastAxis);
     }
 
     public boolean getValue(float time, Axis xa, float []value)
     {
 		int i,k;
 		Bin bin = null;
 		boolean valid = true;
 		float [] binValue = new float [1];
 
 		i = 0;
 		for(k=0; k<bins.getCount(); k++){
 			bin = (Bin)bins.get(k);
 
 			if(xa == bin.xaxis){
 				valid = valid && bin.getValue(time, binValue);
 				value [i] = binValue[0];
 				i++;
 			}
 		}
 
 		return valid && (i > 0);
     }
 
 
     float []  tempVal = new float[1];
     public Annotation addAnnot(String label, float time, Axis xa)
     {
 		Annotation a = null;
 		int k;
 		Bin bin = null;
 
 		for(k=0; k<bins.getCount(); k++){
 			bin = (Bin)bins.get(k);
 
 			if(xa == bin.xaxis){
 				a = bin.addAnnot(label, time);
 			}
 		}
 
 		return a;
     }
 
     public void drawAnnots(Graphics g)
     {
 		int i;
 
 		for(i=0; i<bins.getCount(); i++){
 			Bin bin = (Bin)bins.get(i);
 			bin.drawAnnots(g, annotTopY, xaxis);
 		}
     }
 
     public void resize(int w, int h){}
 
     // return the maximum x offset plotted
     public int plot(Graphics g)
     {
 		// set the clipping region
 		g.setClip(xOriginOff+1, yOriginOff-dwHeight, dwWidth, dwHeight);
 		for(int k=0; k<bins.getCount(); k++){
 			((Bin) bins.get(k)).draw(g);
 		}
 		g.clearClip();
 		return 0;
     }
 
     // This is the yucky one
     public boolean removeBin(Object id)
     {
 		// need to remove the arrays
 		// need to shift the rest of the arrays down
 		// need to go through the vector of bins and trash the 
 		// deleted one and update the rest of them.
 		return true;
     }
 
 	public void actionPerformed(ActionEvent e)
 	{
 		redraw = true;
 	}
 
     public void scroll(int xDist, int yDist)
     {
 		xaxis.scrollStartPos(xDist);
 		xaxis.cacheAxis();
 
 		yaxis.setDispOffset(yaxis.dispMin, yDist, true);
     }
 
 	public void scrollNoCache(int xDist, int yDist)
 	{
 		xaxis.scrollStartPos(xDist);
 
 		if(yDist != 0){
 			yaxis.setDispOffset(yaxis.dispMin, yDist, false);
 		}
 	}
 
     // return a Object linked to this location
     // we are ignoring location for now
     public void addBin(Bin newBin)
     {
 		bins.add(newBin);
 		newBin.color = lineColors[0];
 
 		// hack need to fix the autoscroll code
 		// so I can fix this
 		curBin = newBin;
 	}
 
     public void drawAxis(Graphics g)
     {
 		g.setColor(255,255,255);
 		g.fillRect(0,0,width,height);
 	
 		g.setColor(0,0,0);
 
 		if(profile)
 			yaxis.lGraph = this;
 		else 
 			yaxis.lGraph = null;
 
 		yaxis.draw(g,xOriginOff,yOriginOff);
 		yaxis.drawAxisLabel(g, 0);
       
 		if(profile){
 			endTime = Vm.getTimeStamp();
 			g.drawText(endTime - startTime + "", xText, yText);
 			startTime = endTime;
 			xText += 20;
 		}
 
 		xaxis.draw(g, xOriginOff, yOriginOff);
 		xaxis.drawAxisLabel(g, height-1);
     }
 
     public int yText = 0;
     int xText =0;
     int beginTime, startTime,endTime;
     public void draw(Graphics g)
     {
 		yText = height-10;
 		xText =0;
 		boolean dataWinChanged;
 		int i;
 		int curStartPos;
        
 		if(profile){
 			g.setColor(255,255,255);
 			g.fillRect(0, yText, 200, 30);
 			g.setColor(0,0,0);
 			startTime = beginTime = Vm.getTimeStamp();
 		}
 
 		drawAxis(g);
 
 		for(i=0; i<bins.getCount(); i++){
 			((Bin) bins.get(i)).resetDraw();
 		}
 
 		if(profile){
 			endTime = Vm.getTimeStamp();
 			g.drawText(endTime - startTime + "", xText, yText);
 			startTime = endTime;
 			xText += 20;
 		}
 
 		if(profile){
 			endTime = Vm.getTimeStamp();
 			g.drawText(endTime - startTime + "", xText, yText);
 			startTime = endTime;
 			xText += 20;
 		}
 
 		plot(g);
 
 		drawAnnots((Graphics)g);
 
 		if(profile){
 			endTime = Vm.getTimeStamp();
 			g.drawText(endTime - startTime + "", xText, yText);
 			startTime = endTime;
 			xText += 20;
 
 			g.drawText(endTime - beginTime + "", xText, yText);
 			startTime = endTime;
 			xText += 20;      
 		}
 
 		redraw = false;
     }
 
     public boolean calcDataWin(Graphics g, int w, int h)
     {	
 		// This should be a bit of an iteration
 		// attempting to arrive at the approx
 		int widthSpace = -1*(yaxis.getOutsideSize());
 		int heightSpace = xaxis.getOutsideSize();
 		int bottomAxisSpace = h - yOriginOff;
 		while((widthSpace + 1) > xOriginOff || (heightSpace + 1) > bottomAxisSpace){
 			xOriginOff = widthSpace + 1;
 			bottomAxisSpace = heightSpace + 1;
 			dwWidth = width - rightPadding - widthSpace + 1;
 			dwHeight = height - topPadding - bottomAxisSpace;
 			yaxis.setScale(((float)dwHeight * yaxis.scale) / (float)(yaxis.dispLen*yaxis.axisDir));
 			yaxis.dispLen = -dwHeight;
 			yaxis.gridEndOff = dwWidth;
 			xaxis.setScale((float)dwWidth / 1f);
 			xaxis.dispLen = dwWidth;
 			xaxis.gridEndOff = -dwHeight+1;
 			widthSpace = -1*(yaxis.getOutsideSize());
 			heightSpace = xaxis.getOutsideSize();
 		}
 		yOriginOff = h - bottomAxisSpace - 1;
 
 		return true;
     }
 
 
     public void reset()
     {
 		int i;
 		length = 0;
 		xaxis.reset();
 
 		bins = new Vector();
 
     }
 
     /*
      * There are 9 sections that we can be in:
      *  1 2 3
      *  4 5 6
      *  7 8 9
      * The Data Window  will always be in section 5
      * The axis could be in 4,2,6,8
      * The annotations can be in three neighboring sections on
      *   any of the sides.
      * We won't do any bounds checking.
      * We use the array graphLayout that represents the grid above.
      */
     Object getObjAtPoint(int x, int y)
     {
 		int xPos, yPos;
 		Object obj = null;
 
 		if(x <= dwX) xPos = 0;
 		else if(x < (dwX  + dwWidth)) xPos = 1;
 		else xPos = 2;
 
 		if(y <= dwY) yPos = 0;
 		else if(y < (dwY + dwHeight)) yPos = 1;
 		else yPos = 2;
 
 		return graphLayout[xPos][yPos];
     }
 
 	Axis getAxisBlobAtPoint(Axis curAxis, int x, int y)
 	{
 
 		// use axis.dispLen
 		if(curAxis == yaxis){
 			if(x >= (xOriginOff - curAxis.majTicSize) &&
 			   x <= xOriginOff &&
 			   y >= (yOriginOff + curAxis.dispLen + curAxis.axisDir) &&
 			   y <= (yOriginOff + curAxis.dispLen + curAxis.axisDir - curAxis.axisDir*curAxis.majTicSize)){
 				return yaxis;
 			}
 			return null;
 		} 
 
 		if(curAxis == xaxis){
 			return xaxis.getAxisFromBlob(x, y);
 		}
 		return null;
 	}
 
     /*
      *  We need to check the bounds or only take
      * one direction or something
      */
     Annotation getAnnotAtPoint(int x, int y)
 	{
 		int i;
 		Annotation a = null;
 
 		for(i=0; i<bins.getCount(); i++){
 			Bin bin = (Bin)bins.get(i);
 			a = bin.getAnnotAtPoint(x);
 			if(a != null) break;
 		}
 
 		return a;
 	}
 
 	void setSelectedAnnot(Annotation a)
 	{
 		for(int i=0; i<bins.getCount(); i++){
 			Bin bin = (Bin)bins.get(i);
 			if(bin.xaxis == a.xaxis){				
 				a.selected = true;
 				int index = bin.annots.find(a);
 				if(index >=0){
 					bin.annots.del(index);
 				}
 				bin.annots.add(a);
 			}
 		}
 	}
 }
 
