 import java.io.*;
 import java.util.*;
 
 public class Residue {
     
     double bFactor;
     CartesianCoord coords;
     String pdbResNum;
     String ssType;
     boolean missingSS;
     ArrayList<Atom> atomList = new ArrayList<Atom>();
     // other state
 
     //make as many constructors as needed
     public Residue(String pdbResNum, double bFactor, String ssType, double x, double y, double z) {
         this.bFactor = bFactor;
         this.coords = new CartesianCoord(x, y, z);
         this.pdbResNum = pdbResNum;
         this.ssType = ssType;
     }
     
     public Residue(String pdbResNum, double bFactor, String ssType, CartesianCoord coords) {
         this.bFactor = bFactor;
         this.pdbResNum = pdbResNum;
         this.coords = coords;
         this.ssType = ssType;
     }
 
     public Residue() {
     }
     
     //accessor methods
     public double getBFactor() {
     	return bFactor;
     }
     
     public CartesianCoord getCoords() {
     	return coords;
     }
     
    public String getResNum() {
     	return pdbResNum;
     }
     
    public String getSS() {
     	return ssType;
     }
     
 
     //mutator methods
     public void setBF(double newBF){
     	bFactor = newBF;
     }
     
    	public void setCoords(CartesianCoord newCoord) {
    		coords = newCoord;
    	}
     
     public void setCoords(double x, double y, double z){
     	coords = new CartesianCoord(x,y,z);
     }
     
     public void setResNum(String pdbResNum){
     	this.pdbResNum = pdbResNum;
     }
     
     public void setSSType(String newSsType) {
 		ssType = newSsType;
     }
     
     public void setMissingSS(boolean missingSS) {
     	this.missingSS = missingSS;
     }
 }
