 package com.luugiathuy.apps.remotebluetooth;
 
 import java.io.Serializable;
 
 
 public class InputAndOutput implements Serializable
 {
         double[] _InputPoint;
         double[] _AlgorithmLabel;
         double[] _Size;
         double[] _Refer= new double [8];
         
         public InputAndOutput(double[] inputpoint, double[] algorithm_labels)
         {
             _InputPoint = inputpoint;
             _AlgorithmLabel = algorithm_labels;
         }
         public InputAndOutput(double[] inputpoint, double[] algorithm_labels, double [] _Size)
         {
             _InputPoint = inputpoint;
             _AlgorithmLabel = algorithm_labels;
             _Size=_Size;
             Calculate_Refer(inputpoint, _Size);
         }
         
         public double[] getinputpoint()
         {
                 return _InputPoint;
         }
         
         public double[] getalgorithmlabels ()
         {
                 return _AlgorithmLabel;
         }
         
         //get_index of the 
         public int getindex(){
         	int len=_AlgorithmLabel.length;
         	for (int i=0;i<len;i++){
         		if (_AlgorithmLabel[i]==1)
         			return i;
         	}
         		return -1;
         }
         /**
          * Calculate refer so we can check if another input suit for this one or not. 
          * (used when we do prediction.)
          * qiao 26, August,2012
          * @param inputpoint
          * @param time
          */
         public void  Calculate_Refer(double [] inputpoint, double[] size)
         {
         	//first I will get the direction vector.
         	
         	//accelerometer
         	_Refer[0]=inputpoint[28];
         	_Refer[1]=inputpoint[29];
         	_Refer[2]=inputpoint[30];
         	
         	
         	//gyro
         	_Refer[5]=inputpoint[31];
         	_Refer[6]=inputpoint[32];
         	_Refer[7]=inputpoint[33];
         	
         	//time
         	_Refer[3]=size[0];
         	//accelerate
         	_Refer[4]=size[1];
         }
         
         /**
          * compare time and direction (gyroscope..)
          * 
          * @param newinput
          * @return
          */
         public boolean Compare( double [] newinput ){
         	//time 
         	if( newinput[3]>2*_Refer[3] || newinput[3]<_Refer[3]/1.5){
         		System.out.println("Time erro...newinput: "+newinput[3]+"\nTime erro...refer: "+_Refer[3]);
         		return false;
         	}
         	System.out.println("Time  newinput: "+newinput[3]+"\nTime  refer: "+_Refer[3]);
         	
         	System.out.println("Acc  newinput: "+newinput[4]+"\nAcc  refer: "+_Refer[4]);
         	
         	
         	//angle
         	double result = GetAngle(newinput,_Refer,5);
         	double accresult=GetAngle(newinput,_Refer,0);
         	System.out.println("Acc angle-- "+accresult);
         	
         	//first check angle of gyro 
         	if (result < 0.85 ){
        		//second check acc angle and have to have gyro base 0.2
        		if (accresult <0.9 || result <0.2)
         		{
         			System.out.println("Angle erro... gyro_angle "+result+"  acc_angle  "+accresult);
             		for (int i=5;i<8;i++){
                 		System.out.println("new gyro"+newinput[i]+ " refer gyro "+_Refer[i]);
                 	}
             		for (int i=0;i<3;i++){
                 		System.out.println("new acc "+newinput[i]+ " refer acc "+_Refer[i]);
                 	}
             		return false;
         		}
         	}
         	System.out.println("Angle... "+result);
         	System.out.println("gyro_angle "+result+"  acc_angle  "+accresult);
         	System.out.println("Angle of acc ");
         	for (int i=0;i<3;i++){
         		System.out.println("new acc "+newinput[i]+ " refer acc "+_Refer[i]);
         	}
         	return true;
         }
         
        public double GetAngle(double[] _new, double[] _refer, int index){
     	   	double gyro_new_pow = Math.sqrt(Math.pow(_new[index], 2) + Math.pow(_new[index+1], 2) + Math.pow(_new[index+2], 2));
        		double gyro__refer_pow = Math.sqrt(Math.pow(_refer[index], 2) + Math.pow(_refer[index+1], 2) + Math.pow(_refer[index+2], 2));
        	
        		//calculate the direction angle of movement here. cos ?= a.b/|a||b|
        		double result= (_new[index]*_refer[index]+_new[index+1]*_refer[index+1]+_new[index+2]*_refer[index+2]) /(gyro_new_pow*gyro__refer_pow );
        		return result;
        }
 }
