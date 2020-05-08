 package proclipsingskyscraperoptimization;
 //import java.io.*;
 import proclipsingskyscraperoptimization.LevelStack;
 import processing.core.PApplet;
 
 
 public class Skyscraper {
 
     float x;
     float y;
     float radius;
     
     
     int feet = 12;
     int numLevels;
     int skyscraperWidth = 100*feet;
     int skyscraperDepth = 100*feet;
     int colSize, rowSize, beamColSize, beamRowSize;
     
     PApplet parent; // The parent PApplet that we will render ourselves onto
     
     LevelStack myLevelStack;
     ColumnGrid myColumnGrid;
     Columns myColumns;
     Floor[] myFloors;
     Column[][][] myColumn;
     Beam[][][] myNSBeam,myEWBeam;
     //PrintWriter output;
 
     public Skyscraper ( int numLevels, PApplet p){
     	parent = p;
     	myLevelStack = new LevelStack(numLevels, parent); 
     	myColumnGrid = new ColumnGrid(skyscraperWidth, skyscraperDepth, parent); 
     	initFloors(numLevels, parent);
     	initColumn(numLevels, parent);
     	initBeam(numLevels, parent);
     	//write.writeFile();
     }
   
     public void draw(){
     	myLevelStack.drawStack();
     	myColumnGrid.drawGrid();
     	//drawFloors();
     	drawColumns();
     	drawBeams();
     	//myStairs.draw();
     }
     
    public void initFloors( int numLevels, PApplet p ){
     	myFloors = new Floor[numLevels];
     	parent = p;
     	for(int i=0; i<numLevels; i++){
     	myFloors[i] = new Floor( (Level) myLevelStack.myLevels.get(i), p);
     	}
     }
    
    public void initColumn(int numLevels, PApplet p){
 	   parent = p;
 	   colSize = myColumnGrid.myNSLines.size();
 	   rowSize = myColumnGrid.myEWLines.size();
 	   ColumnGridLine l,k;
 	   int u;
 	   myColumn = new Column[colSize][rowSize][numLevels-1];
 	   for(int y =0; y< numLevels-1; y++){
 	   for(int i = 0; i<colSize; i++){
 		   for (int j = 0; j<rowSize; j++){
 			   k = (ColumnGridLine) myColumnGrid.myNSLines.get(i);
 			   l = (ColumnGridLine) myColumnGrid.myEWLines.get(j);
 	           u = j+65;
 			   //System.out.println( "NS"+"  EW");
 			   //System.out.println("["+(i+1)+"] "+"["+(char)u+"] : "+k.dist+","+l.dist);
			   myColumn[i][j][y] = new Column( (Level)myLevelStack.myLevels.get(y+1), (Level)myLevelStack.myLevels.get(y), skyscraperWidth, k.dist, l.dist, p);
 		   }
 	   }
 	   }
    }
    
    public void initBeam(int numLevels, PApplet p ){
 	   beamColSize = myColumnGrid.myNSLines.size()-1;
 	   beamRowSize = myColumnGrid.myEWLines.size();
 	   myNSBeam = new Beam[colSize][rowSize][numLevels-1];
 	   myEWBeam = new Beam[colSize][rowSize][numLevels-1];
 	   parent = p;
 	   for(int i=0; i<numLevels-1; i++){
 		   for(int j=0;j<colSize;j++){
 			   for(int k=0; k<rowSize;k++){
 				   myNSBeam[j][k][i] = new Beam((int) myFloors[0].thickness, skyscraperWidth,(Level) myLevelStack.myLevels.get(i+1),myColumn[j][k][i],p);
 				   myEWBeam[j][k][i] = new Beam((int) myFloors[0].thickness, skyscraperWidth,(Level) myLevelStack.myLevels.get(i+1),myColumn[j][k][i],p);
 			   }
 		   }
 	   }
    }
     
     public void drawFloors(){
     	for(int i=0; i<myFloors.length; i++){
     		myFloors[i].drawFloor();
     	}
     }
     
     public void drawColumns(){
     	for(int y=0; y<myFloors.length-1; y++){
     	for(int i = 0; i<colSize; i++){
  		   for (int j = 0; j<rowSize; j++){
  			   myColumn[i][j][y].drawColumn();
  		   }
  	   }
     	}
     }
     public void drawBeams(){
     	for(int i = 0; i<myFloors.length-1; i++){
  		   for (int j = 0; j<beamColSize; j++){
  			   for (int k=0; k<beamRowSize; k++){
  				  myNSBeam[j][k][i].drawBeam();
  				  parent.pushMatrix();
  				  parent.rotateZ(PApplet.PI/2);
  				  myEWBeam[j][k][i].drawBeam();
  				  parent.popMatrix();
  			   }
  		   }
  	   }
     }
 }
