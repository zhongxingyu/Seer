 package polymer;
 
 import org.json.simple.*;
 import polymer.*;
 import java.io.*;
 import polymer.GraphControl;
 import java.util.LinkedHashMap;   
 import java.util.Map;   
 import org.json.simple.JSONValue;
 
 public class JsonOutput {
 	//Initializing Constants
 	//UserInput input = new UserInput();
 
 	static IM RiRm = new IM();
     static Ufunc U = new Ufunc();
     static NumberAverage X_n = new NumberAverage();
     static WeightAverage X_w=new WeightAverage();
     static PDI PDI_Num = new PDI();
     static Nx N = new Nx(); 
 	/**
 	 * @param args
 	 * @throws IOException 
 	 */
 	public static void main(String[] args) {
 		 
 	    //Initialize Vars
 	    double t,Ri,Rm,PDI,Nx, Xw,Xn,u;
 	    int   x;
 	    double prev_Ri=1;
 	    double sc = GraphControl.sc;
 	    double xe = GraphControl.xe;
 	    int MaxX=(int) Math.ceil(4*Math.sqrt(xe)+xe);
 
 	    
 	    //Creating JSONArray and JSONObjects
 	    JSONArray jArray = new JSONArray();
 
 	    JSONObject jObject=new JSONObject(); 
 	    JSONObject obj=new JSONObject();   
 
 	    JSONObject DataFile=new JSONObject();
 	    JSONArray NumData = new JSONArray();
 	    JSONObject timeT = new JSONObject();
 	    JSONArray N_x = new JSONArray();
 	    
 	    //Loop for Calculation
              // TODO Make sure i=0 Rm and Ri=1 not the value calculated.
 		for (int i = 0; i<100; i++) {
 			
 			t=i*sc;
 			
 			//Ri
 			Ri=RiRm.getRi(i);
 			//Calculate Rm
 	    	Rm=RiRm.getRm(i);
 	    	//Calculate u
 	    	u=U.getU(i);
 			//Calculate Xn
 	    	Xn= X_n.getXn(i);
 			//Calculate Xw
 	    	Xw= X_w.getXw(i);
 			//Calculate PDI
 	    	PDI=PDI_Num.calPDI(i);
 			//Calculate Nx
 				//Accumulate Nx under one key
 	    	
	    	for (int j=1;j<MaxX;){
 	    		Nx=round5(N.calNx(i,j));
 	    		N_x.add(new Double((Nx)));
 				//This is to limit number of Nx down to around 100. 
			j=(int) (j+Math.ceil(MaxX/100));
 	    		}
 	    		
 			//Finally, put array into object
 	    		
 	    		//String format is:
 	    	    // {t:[Ri,Rm,u,Xn,Xw,PID,Nx1,Nx2....]}
 	    		
 	    		//System.out.print(";"+round5(Ri));
 	    		//System.out.print(","+round5(u));
 	    		//System.out.print(","+round5(Rm));
 	    		//System.out.print(";"+round5(Xn));
 	    		//System.out.print(";"+round5(Xw));
 	    		//System.out.print("p"+round5(PDI));
 	    	    //jArray.clear();
 	    	    //jArray.add((round5(Ri)));
 	    	    //jArray.add((round5(u)));
 	    	    //jArray.add((round5(Rm)));
 	    	    //jArray.add((round5(Xn)));
 	    	    //jArray.add((round5(Xw)));
 	    	    //jArray.add((round5(PDI)));
 	    	    //jArray.addAll(N_x);
 	    	    //obj.put(i, new String(jArray.toString()));
 		}
 		//System.out.print(GraphControl.Ri);
 		String jsonText = obj.toString();  
 	    System.out.print(jsonText);
 
 
 	}
 	
 
 
 
 
 	//Rounder!
 	public static double round5(double num) {
 		 double result = num * 1E5;
 		 result = Math.round(result);
 		 result = result / 1E5;
 		 return result;
 		 }
 		 
 
 }
 
