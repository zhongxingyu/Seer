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
 
 import waba.util.*;
 import waba.fx.*;
 import waba.ui.*;
 
 public class BarGraph extends Graph2D
 {
     final static float DEFAULT_RANGE = (float)30.0;
     final static float DEFAULT_MIN = (float)10.0;
 
     BarSet barSet = null;
     public ColorAxis yaxis = null;
     public float range  = (float)30.0;
     public float minValue = (float)10.0;
     public String units = null;
 
     int width, height;
     int numBars;
     Vector bars = new Vector();
     FontMetrics	fontMet = 
 	MainWindow.getMainWindow().getFontMetrics(MainWindow.defaultFont);
 
     int xOriginOff, yOriginOff;
     int dwWidth, dwHeight;
 
     float [] curValues;
 
     public BarGraph(int w, int h)
     {
 	width = w;
 	height = h;
 
 	range = DEFAULT_RANGE;
 	minValue = DEFAULT_MIN;
 
 	dwWidth = w - 50;
 	dwHeight = h - 40;
 
 	xOriginOff = 40;
 	yOriginOff = h - 30;
 
 	yaxis = new ColorAxis(minValue, minValue + range, -dwHeight, Axis.LEFT);
 	yaxis.gridEndOff=dwWidth-1;
 
 	barSet = new BarSet(yaxis, 1, BarSet.BOTTOM);	
 
 	units = new String("C");
 	
 	numBars = 0;	
 	
     }
 
     public void free()
     {
 	if(yaxis != null)yaxis.free();
 	if(barSet != null)barSet.free();	
     }
 
     public void resize(int w, int h){}
 
     public void setXRange(float min, float range){}
 
     public void setYRange(float min, float range)
     {
 	minValue = min;
 	this.range = range;
 
 	yaxis.dispMin = min;
 	yaxis.setScale((yaxis.dispLen - yaxis.axisDir)/range);
     }
 
     public boolean addBar(int location, DecoratedValue dv)
     {
 	int i;
 	DecoratedValue curBar = null;
 
 	numBars++;
 	
 	bars.insert(location, dv);
 
 	barSet = new BarSet(yaxis, numBars, BarSet.BOTTOM);
 
 	curValues = new float[numBars];
 	for(i=0; i < numBars; i++){
 	    curBar = (DecoratedValue)bars.get(i);
 	    barSet.labels[i].setText(curBar.getLabel());
 	    curValues[i] = curBar.getValue();
 	}
 
 	return true;
     }
 
     public void removeAllBars()
     {
 	bars = new Vector();
 	curValues = null;
 	barSet = new BarSet(yaxis, 1, BarSet.BOTTOM);
 	numBars = 0;
 	redraw = true;
     }
 
     public boolean removeBar(DecoratedValue dv)
     {
 	DecoratedValue curBar = null;
 	int i;
 
 	int index = bars.find(dv);
 	if(index < 0) return false;
 
 	numBars--;
 	bars.del(index);
 
 	if(numBars == 0){
 	    barSet = new BarSet(yaxis, 1, BarSet.BOTTOM);
 	    curValues = null;
 	} else {
 	    curValues = new float[numBars];
 	    barSet = new BarSet(yaxis, numBars, BarSet.BOTTOM);
 	    
 	    for(i=0; i < numBars; i++){
 		curBar = (DecoratedValue)bars.get(i);
 		barSet.labels[i].setText(curBar.getLabel());
 		curValues[i] = curBar.getValue();
 	    }
 
 	}
 
 	return true;
     }
 
     public void draw(Graphics g)
     {
 	int w = width;
 	int h = height;
 
 	barSet.reset();
 
 	g.setColor(255,255,255);
 	g.fillRect(0,0,w,h);
 	
 	g.setColor(0,0,0);
 
 	// DrawAxis
 	yaxis.draw(g,xOriginOff,yOriginOff-1);
 
 	barSet.draw(g,xOriginOff+1,yOriginOff,
 		   dwWidth, dwHeight);
 	
 	plot(g);
 
 	redraw = false;
     }
 
     public int plot(Graphics g)
     {
 	float x = 0;
 	int i;
 	DecoratedValue curBar = null;
 
 	if(g != null && numBars > 0){
 	    // Plot data
 	    for(i=0; i < numBars; i++){
 		curBar = (DecoratedValue)bars.get(i);
 		barSet.labels[i].setText(curBar.getLabel());
 		curValues[i] = curBar.getValue();
 	    }
 
 	    g.setColor(0,0,0);
 	    barSet.addColorPoint(g, x, curValues);
 	}
 	
 	return 0;
     }
 
     public void reset()
     {
 	barSet.reset();
     }
 
 }
 
 
 
 
 
 
 
 
 
 
