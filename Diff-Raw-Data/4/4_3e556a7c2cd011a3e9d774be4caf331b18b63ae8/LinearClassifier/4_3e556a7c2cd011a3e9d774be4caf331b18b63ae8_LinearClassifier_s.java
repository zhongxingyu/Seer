 /**
  * ##library.name##
  * ##library.sentence##
  * ##library.url##
  *
  * Copyright ##copyright## ##author##
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * 
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General
  * Public License along with this library; if not, write to the
  * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
  * Boston, MA  02111-1307  USA
  * 
  * @author      ##author##
  * @modified    ##date##
  * @version     ##library.prettyVersion## (##library.version##)
  */
 
 package linearclassifier;
 
 import java.util.ArrayList; 
 import processing.core.*;
 
 /**
  * This library implements linear classification for numerical data. It accepts
  * two labeled data sets, each element of which can contain two or more floats as data.
  * Once trained, LinearClassifier can predict which of the two original sets any new piece
  * of test data will fall into.
  * 
  * LinearClassifier also includes tools for displaying data in the 2D case. See the 2D example for details:
  * 
  * @example LinearClassifier2D
  * 
  * For use in more than 2D see the Matchmaker example:
  * 
  * @example LinearClassifierMatchmaker
  * 
  * LinearClassifier is based on examples in Chapter 9 of Programming Collective by Toby Segaran:
  * http://shop.oreilly.com/product/9780596529321.do
  *  * 
  */
 
 public class LinearClassifier {
 	public final static String VERSION = "##library.prettyVersion##";
 
 	private PApplet parent;
 	
 	public ArrayList<ArrayList<Float>> set1;
 	public ArrayList<ArrayList<Float>> set2;
 	  
 	public  ArrayList<Float> maxs;
 	public  ArrayList<Float> mins;
 
 	private  boolean boundsInitialized = false;
 
 	public ArrayList<PVector> outputScales;
 
 	public LinearClassifier(PApplet parent) {
 		this.parent = parent;
 		
 		set1 = new ArrayList<ArrayList<Float>>();
 	    set2 = new ArrayList<ArrayList<Float>>();
 	    maxs = new ArrayList<Float>();
 	    mins = new ArrayList<Float>();
 	    outputScales = new ArrayList<PVector>();
 	}
 
 	public void setOutputScale(ArrayList<PVector> outputScales){
 		this.outputScales = new ArrayList<PVector>();
 	    for(PVector p : outputScales){
 	      this.outputScales.add(p);
 	    }
 	}
 	
 	public void setOutputScale(float min, float max){
 	    for(int i = 0; i < getDataSize(); i++){
 	      outputScales.add(new PVector(min, max));
 	    }
 	}
 	  
 	public int getDataSize(){
 	    return set1.get(0).size();
 	}
 	  
 
 	private  void initializeBounds(int numItems) {
 	    for (int i = 0; i<numItems; i++) {
 	      maxs.add((float)-50000.0);
 	      mins.add((float)50000.0);
 	    }
 
 	    boundsInitialized = true;
 	 }
 
 	 public void loadSet1(ArrayList<ArrayList<Float>> set) {
 	    if (!boundsInitialized) {
 	      initializeBounds(set.get(0).size());
 	    }
 
 	    for (ArrayList<Float> item : set) {
 	      this.set1.add(item);
 
 	      for (int i = 0; i < item.size(); i++) {
 	        maxs.set(i, PApplet.max(item.get(i), maxs.get(i)));
 	        mins.set(i, PApplet.min(item.get(i), mins.get(i)));
 	      }
 	    }
 	  }
 
 	  public void loadSet2(ArrayList<ArrayList<Float>> set) {
 	    if (!boundsInitialized) {
 	      initializeBounds(set.get(0).size());
 	    }
 
 	    for (ArrayList<Float> item : set) {
 	      this.set2.add(item);
 	      for (int i = 0; i < item.size(); i++) {
 	        maxs.set(i, PApplet.max(item.get(i), maxs.get(i)));
 	        mins.set(i, PApplet.min(item.get(i), mins.get(i)));
 	      }
 	    }
 	  }
 
 	  public void drawSet1() {
 	    for (ArrayList<Float> item : set1) {
 	      PVector scaled = pToV(getScaledPoint(item));
 	      parent.ellipse(scaled.x, scaled.y, 5, 5);
 	    }
 	  }
 
 	  public void drawSet2() {
 	    for (ArrayList<Float> item : set2) {
 	      PVector scaled = pToV(getScaledPoint(item));
 	      parent.ellipse(scaled.x, scaled.y, 5, 5);
 	    }
 	  }
 
 	  public ArrayList<Float> getScaledPoint(PVector p){
 	    return getScaledPoint(vToP(p));
 	  }
 
 	  public ArrayList<Float> getScaledPoint(ArrayList<Float> p) {
 	    ArrayList<Float> result = new ArrayList<Float>();
 	    for(Float f : p){
 	      result.add((float)0.0);
 	    }
 	    
 	    for (int i = 0; i < p.size(); i++) {
 	      result.set(i, PApplet.map(p.get(i), mins.get(i), maxs.get(i), outputScales.get(i).x, outputScales.get(i).y));
 	    }
 	    
 	    return result;
 	  }
 	  
 	  public PVector getUnscaledPoint(ArrayList<Float> p) {
 	    float mX = PApplet.map(p.get(0), outputScales.get(0).x, outputScales.get(0).y, mins.get(0), maxs.get(0));
 	    float mY = PApplet.map(p.get(1), outputScales.get(1).x, outputScales.get(1).y, mins.get(1), maxs.get(1));
 	    return new PVector(mX, mY);
 	  }
 
 	  public PVector getUnscaledPoint(PVector p) {
 	    return getUnscaledPoint(vToP(p));
 	  }
 
 	  public float dotProduct(ArrayList<Float> v1, ArrayList<Float> v2) {
 	    float result = 0;
 	    for (int i = 0; i < v1.size(); i++) {
 	      result = result + v1.get(i)*v2.get(i);
 	    }
 
 	    return result;
 	  }
 
 
 	  public PVector getCenterPoint() {
 	    return new PVector((getSet1Average().get(0) + getSet2Average().get(0))/(float)2.0, (getSet1Average().get(1) + getSet2Average().get(1))/(float)2.0 );
 	  }
 
 	  public boolean isInSet1(PVector p) {
 	    return isInSet1(vToP(p));
 	  }
 
 	  public boolean isInSet1(ArrayList<Float> p) {
 	    float b = (dotProduct(getSet2Average(), getSet2Average()) - dotProduct(getSet1Average(), getSet1Average()))/2;
 	    float y = dotProduct(p, getSet1Average()) - dotProduct(p, getSet2Average()) + b;
 
 	    return (y > 0);
 	  }
 
 	  public ArrayList<Float> vToP(PVector v) {
 	    ArrayList<Float> result = new ArrayList<Float>();
 	    result.add(v.x);
 	    result.add(v.y);
 	    return result;
 	  }
 
 	  public PVector pToV(ArrayList<Float> p) {
 	    return new PVector(p.get(0), p.get(1));
 	  }
 
 	  public boolean isInSet2(ArrayList<Float> p){
 	    return !isInSet1(p);
 	  }
 
 	  public boolean isInSet2(PVector p) {
 	    return isInSet2(vToP(p));
 	  }
 
 	  public ArrayList<Float> getSet1Average() {
 	    return getAverage(set1);
 	  }
 
 	  public ArrayList<Float> getSet2Average() {
 	    return getAverage(set2);
 	  }
 
 	  public void scaleData(int min ,int max) {
 	    setOutputScale(min,max);
 	    
 	    for (ArrayList<Float> item : set1) {
 	      scaleData(item, min, max);
 	    }
 
 	    for (ArrayList<Float> item : set2) {
 	      scaleData(item, min, max);
 	    }
 	  }
 
 	  public void scaleData(ArrayList<Float> data, int min, int max) {
 	    for (int i = 0; i < data.size(); i++) {
 	      data.set(i, PApplet.map(data.get(i), mins.get(i), maxs.get(i), min, max));
 	    }
 	  }
 
 	  public ArrayList<Float> getAverage(ArrayList<ArrayList<Float>> set) {
 	    ArrayList<Float> result = new ArrayList<Float>();
 	    for(Float f : set.get(0)){
 	      result.add((float)0.0);
 	    }
 	    
 	    for (int i = 0; i < set.size(); i++) {
 	      ArrayList<Float> p = set.get(i);
 	      for(int j = 0; j < p.size(); j++){
 	    	  result.set(j, result.get(j) + p.get(j));
 	      }
 	    }
 	    
 	    for(int j = 0; j < result.size(); j++){
	    	result.set(j, result.get(j)/ result.size());
 	    }
 	    
 	    return getScaledPoint(result);
 	  }
 }
 
