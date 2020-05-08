 package proclipsingskyscraperoptimization;
 
 import processing.core.PApplet;
 
 public class ColumnGridLine {
 	
 	PApplet parent; // The parent PApplet that we will render ourselves onto
 	int dist;
 	
 	int feet = 12;
 	int length = 100*feet;
 	int margin = 25*feet;
 	int gridTextSize = 50;
 	String name;
 	
 	
 	ColumnGridLine(int num, PApplet p){
 		parent = p;
 		dist = num;
 		name = (Integer.toString(num));
 	
     }
 	
 	public String getName(){
 		return name;
 		
 	}
 	void drawGridLine(int w, int d){
 		parent.pushMatrix();
 		parent.stroke(150);
 		parent.translate(-(w/2), 0 , 0);
		parent.line(dist, (-length/2)-margin, dist, (length/2)+margin );
     	parent.fill(50, 50, 50, 155);
     	parent.textSize(gridTextSize);
     	parent.text(name,dist+2,(length/2)+margin);
     	parent.popMatrix();
 	}
 	
 	 public void drawGridLineName(int w, int name){
 	    	parent.pushMatrix();
 			parent.stroke(150);
 			parent.translate( -(w/2), 0, 0 );
 	    	parent.fill(50, 50, 50, 155);
 	    	parent.textSize(50);
 	    	parent.text(name+1,dist+2,(-length/2)-margin);
 	    	parent.popMatrix();
 	 }
 	
 	 public void drawGridLineLetter(int w, int name){
 		    int newname = name + 65;
 		 	parent.pushMatrix();
 			parent.stroke(150);
 			parent.translate(-(w/2), 0, 0 );
 	    	parent.fill(50, 50, 50, 155);
 	    	parent.textSize(50);
 	    	parent.text((char)newname,dist+2,(-length/2)-margin);
 	    	parent.popMatrix();
 	 }
 	 
 	 public void drawColumn(int w){
 			setColors();
 	    	parent.pushMatrix();
 	 
 	    	//parent.translate( -(mySkyscraper.myColumn.w/2), 0, (base.elevation + h/2) ); // CRUD the grid line doesn't know where it is!
 	    	parent.translate(-(w/2), 0, 1000); // CRUD the grid line doesn't know where it is!
 	    	parent.box(12, 12, 2236); //to be replaced with extruded sketch of profile
 	    	parent.popMatrix();
 	}
 		
 	    void setColors(){
 	    	parent.stroke(155);  // Floors are outlined
 	    	parent.fill(155,155); // levels are transparent grey
 	    }
 }
 
