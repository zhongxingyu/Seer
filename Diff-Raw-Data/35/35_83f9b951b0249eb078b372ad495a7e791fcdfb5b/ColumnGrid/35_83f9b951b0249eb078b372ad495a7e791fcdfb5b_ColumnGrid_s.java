 package proclipsingskyscraperoptimization;
 
 import java.util.ArrayList;
 
 import processing.core.PApplet;
 
 public class ColumnGrid {
 	
 	PApplet parent; // The parent PApplet that we will render ourselves onto
     ArrayList myNSLines;
     ArrayList myEWLines;
     
     static int feet = 12;
    static int typicalGridDist = (10*feet);
     
     int gridWidth;
     int gridDepth;
     
     public ColumnGrid ( int w, int d, PApplet p ){
     	parent = p;
         gridWidth = w;
         gridDepth = d;
     	myNSLines = new ArrayList();
     	myEWLines = new ArrayList();
     	
     	//Creates the size of the NS Array lines
     	for (int i=0; i<((gridDepth+typicalGridDist)/typicalGridDist); i++){ 
    		myNSLines.add( new ColumnGridLine(i*typicalGridDist, parent) );
     	}
     	//Creates the size of the EW Array lines
     	for (int i=0; i<((gridWidth+typicalGridDist)/typicalGridDist); i++){
    		myEWLines.add( new ColumnGridLine(i*typicalGridDist, parent) );
     	}
 
     }
     
     public void drawGrid(){
     	for (int i=0; i<myNSLines.size(); i++){
     		ColumnGridLine g = (ColumnGridLine) myNSLines.get(i);
     		g.drawGridLine(gridWidth, gridDepth);
     		g.drawGridLineName(gridWidth, i);
     			
     	}
     	
     	parent.pushMatrix();
     	parent.rotateZ(PApplet.PI/2);
    	//parent.translate(0, , typicalGridDist);
     	for (int i=0; i<myEWLines.size(); i++){
     		ColumnGridLine g = (ColumnGridLine) myEWLines.get(i);
     		g.drawGridLine(gridWidth, gridDepth);
     		g.drawGridLineLetter(gridWidth, i);
     		//g.drawGridLineLetter(typicalGridDist, i);
     	}
     	parent.popMatrix();
     }
 }
 
