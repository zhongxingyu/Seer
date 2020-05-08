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
 import waba.util.*;
 import waba.sys.*;
 import org.concord.waba.extra.ui.*;
 import org.concord.waba.extra.util.*;
 import org.concord.waba.extra.event.*;
 import extra.util.*;
 
 public class GraphViewLine extends GraphView
     implements DialogListener
 {
     final static float Y_MAX = (float)50.0;
     final static float Y_MIN = (float)-5.0;
     final static float X_MAX = (float)60.0;
     final static float X_MIN = (float)0.0;
 
     final static char DRAG_MODE = 'D';
     final static char ZOOM_MODE = 'Z';
     final static char ANNOT_MODE = 'A';
 
     public char mode = 'D';
 
     public char curChar = 'A';
 
     public float minY = Y_MIN;
     public float maxY = Y_MAX;
     public float minX = X_MIN;
     public float maxX = X_MAX;
 
     public String units = null;
 
     PropertyDialog pDialog = null;
     PropContainer props = null;
     PropObject propXmin = new PropObject("Min", minX + "");
     PropObject propXmax = new PropObject("Max", maxX + "");
     PropObject propYmin = new PropObject("Min", minY + "");
     PropObject propYmax = new PropObject("Max", maxY + "");
 
     LineGraph lGraph;
     Annotation curAnnot = null;
 
     FloatConvert fConvert = new FloatConvert();
 
     public GraphViewLine(int w, int h)
     {
 	super(w,h);
 
 	fConvert.minDigits = 2;
 	fConvert.maxDigits = 4;
 
 	graph = lGraph = new LineGraph(w, h);
 	lGraph.setYRange(minY, maxY - minY);
 	lGraph.setXRange(0, minX, maxX - minX);
 
 	units = new String("C");
 	
 	props = new PropContainer();
 	PropContainer pCont = props.createSubContainer("X Axis");
 	props.createSubContainer("Y Axis");	
 	props.addProperty(propXmin, "X Axis");
 	props.addProperty(propXmax, "X Axis");
 	props.addProperty(propYmin, "Y Axis");
 	props.addProperty(propYmax, "Y Axis");
 
     }
 
     public void dialogClosed(DialogEvent e)
     {
 	if(!e.getActionCommand().equals("Cancel")){
 	    minX = propXmin.createFValue();
 	    maxX = propXmax.createFValue();
 	    minY = propYmin.createFValue();
 	    maxY = propYmax.createFValue();
	    lGraph.setYRange(minY, maxY - minY);
	    lGraph.setXRange(minX, maxX - minY);
 	}
     }
 
     public boolean autoScroll = true;
     float scrollFract = (float)0.25;
     float scrollStepSize = (float)0.15;
     int scrollSteps = 5;
 
     public void plot()
     {
 	Bin bin = lGraph.curBin;
 	float range;
 	float scrollEnd;
 	int myScrollStep = (int)(lGraph.dwWidth * scrollStepSize);
 
 
 	if(autoScroll && 
 	   (bin.getNumVals() > 0) && 
 	   (bin.maxX > (lGraph.xaxis.dispMin + (float)lGraph.xaxis.dispLen / lGraph.xScale ) ||
 	   lGraph.xaxis.drawnX == -1)){
 	    // scroll
 	    range = lGraph.xRange;
 	    scrollEnd = bin.maxX - range * scrollFract;
 	    //		System.out.println("xRange: " + lGraph.xRange + ", scrollEnd: " + scrollEnd);
 	    if(scrollEnd < (float)0)
 		scrollEnd = (float)0;
 	    if((scrollEnd - lGraph.xaxis.dispMin) * lGraph.xScale > (10 * myScrollStep)){
 		myScrollStep = (int)((scrollEnd - lGraph.xaxis.dispMin) * lGraph.xScale + 2);
 	    }
 	    while((lGraph.xaxis.dispMin < scrollEnd) || 
 		  (lGraph.xaxis.drawnX > (lGraph.xOriginOff + 4)) ||
 		  (lGraph.xaxis.drawnX == -1)){
 		lGraph.scroll(myScrollStep, 0);
 		drawn = false;
 		super.plot();
 	    }
 	} else {	    
 	    super.plot();
 	}
     }
     
     int downX, downY, dragX, dragY;
     boolean xAxisDown, yAxisDown, graphDown, barDown, annotDown;
     Timer timer = null;
 
     public Annotation selAnnot = null;
     
     float [] tempVal = new float [1];
     public void onEvent(Event e)
     {
 	PenEvent pe;
 	int moveX, moveY;
 	float xChange;
 	float yChange;
 	int i;
 
 	if(e.target == this){
 	    if(e instanceof PenEvent){
 		pe = (PenEvent)e;
 		switch(e.type){
 		case PenEvent.PEN_DOWN:
 		    xAxisDown = yAxisDown = graphDown = annotDown = false;
 		    Object obj = lGraph.getObjAtPoint(pe.x, pe.y);
 		    if(obj == lGraph.xaxis){
 			xAxisDown = true;
 		    } else if(obj == lGraph.yaxis) {
 			yAxisDown = true;
 		    } else if(obj != null){
 			Annotation oldAnnot = selAnnot;
 			if(selAnnot != null){
 			    selAnnot.selected = false;
 			}
 			if(mode == ANNOT_MODE){
 			    curAnnot = lGraph.addAnnot("" + curChar, pe.x);
 			    curChar++;
 			    draw();
 			    if(curAnnot != null)
 				postEvent(new ControlEvent(1002, this));
 			    return;
 			} else {
 			    if(obj == lGraph){
 				selAnnot = null;
 				graphDown = true;
 			    } else {
 				selAnnot = lGraph.getAnnotAtPoint(pe.x, pe.y);
 				if(selAnnot != null){
 				    selAnnot.selected = true;
 				    lGraph.annots.del(lGraph.annots.find(selAnnot));
 				    lGraph.annots.add(selAnnot);
 				    annotDown = true;
 				} 
 				draw();
 				    
 			    }
 			    if(oldAnnot != selAnnot) postEvent(new ControlEvent(1003, this));
 			}
 		    }
 
 		    if(timer == null)
 			timer = addTimer(750);
 		    
 		    downX = pe.x;
 		    downY = pe.y;
 		    dragY = 0;
 		    dragX = 0;
 		    break;
 		case PenEvent.PEN_DRAG:
 		case PenEvent.PEN_UP:
 		    moveX = pe.x - downX;
 		    moveY = pe.y - downY;
 		    if(moveX < 0)
 			dragX -= moveX;
 		    else
 			dragX += moveX;
 		    if(moveY < 0)
 			dragY -= moveY;
 		    else
 			dragY += moveY;
 
 		    if(graphDown){
 			if(timer != null){
 			    removeTimer(timer);
 			    timer = null;
 			}
 			lGraph.scroll(-moveX, -moveY);
 			draw();
 		    } else if(annotDown){
 			if(timer != null){
 			    removeTimer(timer);
 			    timer = null;
 			}
 			if(selAnnot != null){
 			    Axis xa = selAnnot.xaxis;
 			    float newTime = (moveX + (selAnnot.time - xa.dispMin) * xa.scale)/
 				xa.scale + xa.dispMin;
 			    if(lGraph.getValue(newTime, xa, tempVal)){
 				selAnnot.time = newTime;
 				selAnnot.value = tempVal[0];
 				postEvent(new ControlEvent(1003, this));
 				draw();
 			    }
 			}
 
 		    } else if(dragY > 10 || dragX > 10){ 
 			if(timer != null){
 			    removeTimer(timer);
 			    timer = null;
 			}
 			if(yAxisDown){
 			    if(lGraph.yOriginOff - pe.y > 20){
 				yChange = (float)(lGraph.yOriginOff - pe.y)/ (float)(lGraph.yOriginOff - downY);
 				
 				lGraph.setYscale(lGraph.yaxis.scale * yChange);
 				
 				draw();
 			    }
 			}else if(xAxisDown && graph == lGraph){
 			    if(pe.x - lGraph.xOriginOff > 20){
 				xChange = (float)(lGraph.xOriginOff - pe.x)/ (float)(lGraph.xOriginOff - downX);
 				lGraph.setXscale(lGraph.xScale * xChange);
 				draw();
 			    }
 			}
 		    }
 		     
 		    downX = pe.x;
 		    downY = pe.y;
 		    if(e.type == PenEvent.PEN_UP){
 			graphDown = false;
 			xAxisDown = false;
 			yAxisDown = false;
 			barDown = false;
 			if(timer != null){
 			    removeTimer(timer);
 			    timer = null;
 			}
 		    }
 		    break;
 		}		
 	    } else if(e.type == ControlEvent.TIMER){
 		if(timer != null){
 		    removeTimer(timer);
 		    timer = null;
 		} else {
 		    // We have already cleared the timer so ignore this 
 		    return;
 		}
 		MainWindow mw = MainWindow.getMainWindow();
 		if(mw instanceof ExtraMainWindow){
 		    propXmin.setValue("" + lGraph.xaxis.dispMin);
 		    propXmax.setValue("" + (lGraph.xaxis.dispMin+lGraph.dwWidth/lGraph.xaxis.scale));
 		    propYmin.setValue("" + lGraph.yaxis.dispMin);
 		    propYmax.setValue("" + (lGraph.yaxis.dispMin+lGraph.yaxis.dispLen/lGraph.yaxis.scale));
 
 		    PropertyDialog pd = new PropertyDialog((ExtraMainWindow)mw, this, "Properties", props);
 		    pd.setRect(0,0, 140,140);
 		    pd.show();
 		}
 	    }
 	}
     }
 	
 
 
 
 }
 
 
 
 
 
 
 
 
 
 
 
 
