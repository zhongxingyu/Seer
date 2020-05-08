 /**
  * 
  * 
  * You should probably make an array of distances/delta/theta/rho angles and the like, 
  * that way we can have one 'geometries' file for each secondary structure. Each of these does correspond 
  * to a secondary structure so hopefully that should work out. I would also like to see stored the 
  * first and last residue pdbResNum in the data structure of the geometry.
  * 
  * I'll see about putting together the geometries data structure if you'll see how it works out...
  * */
 
 
 import java.util.*;
 import java.io.*;
 
 public class CalcGeo {
 	
     public static ArrayList<Geometry> geoList;
 
     public CalcGeo(ArrayList<Residue> pmoiList) { // takes pmoi
 	this.geoList = calculate(pmoiList);
 
 	for(int i=0; i<geoList.size(); ++i) {
 	    System.out.println("st : " + geoList.get(i).getStart());
 	}
 
     }
     
     public ArrayList<Geometry> calculate(ArrayList<Residue> residueList) {
 	/*double magnitudeOfX = 0;
 	double distance;
 	double delta;
 	double theta;
 	double rho;
 	CartesianCoord p0, p1, p2, p3, e1, l, e2;*/
 		
 	ArrayList<Geometry> geoArray = new ArrayList<Geometry>();
 
 	System.out.println("residue list in calcgeo size : " + residueList.size());
 	for (int i = 0; i<residueList.size() - 5; i+=2) {
 	    if(!residueList.get(i).getSS().equals("T")){
 		/** I know this looks insane but it's seriously the only way 
 		    to get this to work. Don't judge, alright?
 		    
 		    Also if you waste more time on it I ask that you increment
 		    the following counter:
 		    
 		    time_wasted_here = 5 hrs
 		*/
 		
 		double magnitudeOfX = 0;
 		double distance;
 		double delta;
 		double theta;
 		double rho;
 		CartesianCoord p0, p1, p2, p3, e1, l, e2;
 		
 		p0 = (residueList.get(i).getPMOI()); 
 		p1 = (residueList.get(i+1).getPMOI());
 		p2 = (residueList.get(i+4).getPMOI());
 		p3 = (residueList.get(i+5).getPMOI());
 		
 		//calculate e1, vector
 		e1 = calcE1LE2(p0, p1);
 		//calculate l, vector
 		l = calcE1LE2(p2, p1);
 		//calculate e2, vector
 		e2 = calcE1LE2(p3, p2);
 		
 		//calculate distance, d, scalar
 		distance = calcDistance(p2, p1);
 		//calc delta angle
 		delta = calcAngles(e1, l);
 		//calc theta angle
 		theta = calcAngles(e1, e2);
 		//calc n, this is a vector
 		CartesianCoord crossProd = calcCrossProd(l, e1);
 		double lengthOfCrossProd = calcMagnitude(l)*calcMagnitude(e1)*Math.sin(theta);
 		
 		CartesianCoord n = new CartesianCoord(crossProd.getX()/lengthOfCrossProd,
 						      crossProd.getY()/lengthOfCrossProd, crossProd.getZ()/lengthOfCrossProd);
 		//calc x, this is a vector
 		double e1e2DotProd = calcDotProd(e1, e2);
 		
 		CartesianCoord secondTermOfX = new CartesianCoord(e1.getX()*e1e2DotProd,
 								  e1.getY()*e1e2DotProd, e1.getZ()*e1e2DotProd);
 		
 		CartesianCoord x = new CartesianCoord(e2.getX()-e1.getX(),
 						      e2.getY()-e1.getY(), e2.getZ()-e1.getZ());
 		//calc rho angle, has two cases
 		double whichCaseOfRho = calcDotProd(x, calcCrossProd(e1, n));
 		double xOfX = x.getX();
 		double yOfX = x.getY();
 		double zOfX = x.getZ();
 		magnitudeOfX = calcMagnitude(x);
 		CartesianCoord vectorXDividedByLength = new CartesianCoord(xOfX/magnitudeOfX, yOfX/magnitudeOfX, 
 									   zOfX/magnitudeOfX);
 		if(whichCaseOfRho >= 0) {
 		    rho = Math.acos(calcDotProd(vectorXDividedByLength, n));
 		}
 		else{
 		    magnitudeOfX = calcMagnitude(x);
 		    rho = (2*Math.PI)-Math.acos(calcDotProd(vectorXDividedByLength, n));
 		}
 		
 		//(String stRes, String endRes,
 		//String distance, String delta, String theta, String rho
 		String stRes = residueList.get(i).getResNum();
 		String endRes = residueList.get(i+5).getResNum();
 		//everything with st/end res is fine here
 		geoArray.add(new Geometry(stRes, endRes, "" + distance + "", "" + delta + "",  
 					  "" + theta + "", "" + rho + ""));
 	    }
 	}
 	return geoArray;
     }
     
     public double calcAngles(CartesianCoord first, CartesianCoord second){
 	double dotProduct = calcDotProd(first, second);
 	return Math.acos(dotProduct);
     }
     
     public double calcMagnitude(CartesianCoord a){
 	double a1 = a.getX();
 	double a2 = a.getY();
 	double a3 = a.getZ();
 	double sqrtA1 = Math.pow(a1, 2);
 	double sqrtA2 = Math.pow(a2, 2);
 	double sqrtA3 = Math.pow(a3, 2);
 	double magnitude = Math.sqrt(sqrtA1+sqrtA2+sqrtA3);
 	return magnitude;
     }
     
     public CartesianCoord calcCrossProd(CartesianCoord first, CartesianCoord second){
 	double a1 = first.getX();
 	double a2 = first.getY();
 	double a3 = first.getZ();
	double b1 = first.getX();
	double b2 = first.getY();
	double b3 = first.getZ();
 	double firstTerm=(a2*b3)-(a3*b2);
 	double secondTerm = (a3*b1)-(a1*b3);
 	double thirdTerm = (a1*b2)-(a2*b1);
 	CartesianCoord crossProd = new CartesianCoord(firstTerm, secondTerm, thirdTerm);
 	return crossProd;
     }
     
     public double calcDotProd(CartesianCoord first, CartesianCoord second){
 	double firstTerm = first.getX()*second.getX();
 	double secondTerm = first.getY()*second.getY();
 	double thirdTerm = first.getZ()*second.getZ();
 	return firstTerm+secondTerm+thirdTerm;
     }
 
     public double calcDistance(CartesianCoord p2, CartesianCoord p1) {
 	double differenceX, differenceY, differenceZ, sqDiffX, sqDiffY, sqDiffZ;
 	differenceX = p2.getX()-p1.getX();
 	differenceY = p2.getY()-p1.getY();
 	differenceZ = p2.getZ()-p1.getZ();
 	sqDiffX = Math.pow(differenceX, 2);
 	sqDiffY = Math.pow(differenceY, 2);
 	sqDiffZ = Math.pow(differenceZ, 2);
 	return Math.sqrt(sqDiffX+sqDiffY+sqDiffZ);
     }
 
     public CartesianCoord calcE1LE2(CartesianCoord first, CartesianCoord second){
 	double differenceX, differenceY, differenceZ, denominator;
 	differenceX = second.getX()-first.getX();
 	differenceY = second.getY()-first.getY();
 	differenceZ = second.getZ()-first.getZ();
 	denominator = calcDistance(first, second);
 	return new CartesianCoord((differenceX/denominator), (differenceY/denominator), (differenceZ/denominator));
     }
 }    
