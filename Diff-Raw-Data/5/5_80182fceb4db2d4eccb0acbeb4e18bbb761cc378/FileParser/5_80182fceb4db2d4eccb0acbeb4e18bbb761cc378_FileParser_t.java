 package model;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 public class FileParser {
     
     private List<iGizmo> gizmos;
     private GameGrid gameGrid;
     
     public FileParser( List<iGizmo> g, GameGrid gm ) {
         this.gizmos 	= g;
         this.gameGrid 	= gm;
     }
     
     public void loadFile(String fileName) {
         ArrayList<String> input = new ArrayList<String>();
         String strLine = "";
         
         try {
             FileInputStream fstream = new FileInputStream(fileName);
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             
             while((strLine = br.readLine()) != null) {
                 input.add(strLine);
             }
             br.close();
             in.close();
             fstream.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
         
         generate(input);
     }
     
     private void generate(ArrayList<String> input) {
         for(String s : input) {
             String[] tok = s.split("\\s");
             
             switch(tok[0]) {
                 case "":  //blank line in file
                     break;
                 case "Absorber":
                 	this.addAbsorber(   tok[1], 
                 						Integer.parseInt(tok[2]), Integer.parseInt(tok[3]), 
                 						Integer.parseInt(tok[4]) - Integer.parseInt(tok[2]), 
                 						Integer.parseInt(tok[5]) - Integer.parseInt(tok[3])
                 					);
                     break;
                 case "Ball":
                 	this.gizmos.add(
                 					new Ball(tok[1], new Point( (int)Double.parseDouble(tok[2]), (int)Double.parseDouble(tok[3]) ), 
                 								1, 1));
                     break;
                 case "Circle":
                 	this.gizmos.add(
         							new CircleBumper(tok[1], new Point(Integer.parseInt(tok[2]), Integer.parseInt(tok[3])),
         									1, 1));
                     break;
                 case "LeftFlipper":
                 	this.gizmos.add(
 		                			new LeftFlipper(tok[1], new Point(Integer.parseInt(tok[2]), Integer.parseInt(tok[3])),
 		                					1, 2));
                     break;
                 case "RightFlipper":
                 	this.gizmos.add(
 		                			new RightFlipper(tok[1], new Point(Integer.parseInt(tok[2]), Integer.parseInt(tok[3])),
 		                					1, 2));
                     break;
                 case "Square":
                 	this.gizmos.add( 
         							new SquareBumper(tok[1],
         									new Point( Integer.parseInt(tok[2]), Integer.parseInt(tok[3]) ), 
         									1, 1));
                     break;
                 case "Triangle":
                 	this.gizmos.add(
         							new TriangleBumper(tok[1], 
         									new Point( Integer.parseInt(tok[2]), Integer.parseInt(tok[3]) ), 
         									1, 1));
                     break;
                 case "Gravity":
                     
                     break;
                 case "Friction":
                     
                     break;
                 case "Move":
                     this.move(tok[1], Double.parseDouble(tok[2]), Double.parseDouble(tok[3]));
                     break;
                 case "Rotate":
 					this.rotate(tok[1]);
                     break;
                 case "Connect":
                     
                     break;
                 case "KeyConnect":
                     
                     break;
                 case "Delete":
                     this.delete(tok[1]);
                     break;
                 default: //Error throw up error message dialog box
                     break;
             }
         }
     }
     
     public void saveFile() {
         
     }
     
     private void delete(String name) {
     	for(iGizmo g : gizmos){
     		if( g.getIdentifier().equals(name) ) this.gizmos.remove(g);
     	}
     }
     
     private void move(String name, double x, double y) {
     	for(iGizmo g : gizmos){
     		if(g.getIdentifier().equals(name))g.setLocation( new Point((int)x, (int)y) );
     	}
     }
     
     private void rotate(String name) {
     	for(iGizmo g : gizmos){
     		if(g.getIdentifier().equals(name))g.setRotation(90);
     	}
     }
     
     private void addAbsorber(String id, int x, int y, int width, int height)
     {
    	//if(this.gameGrid.setGridPoint(new Point(x, y), width, height, true)){
     		this.gizmos.add(
 					new Absorber(   id, new Point( x, y ),
 									width, 
 									height)
 								);
    	//}
     }
     
 }
