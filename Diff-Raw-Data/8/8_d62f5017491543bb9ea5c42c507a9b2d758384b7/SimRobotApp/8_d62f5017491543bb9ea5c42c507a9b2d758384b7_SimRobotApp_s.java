 	
 import java.io.*;
 import java.net.*;
 import java.util.*;
 import java.nio.ByteBuffer;
 import java.nio.DoubleBuffer;
 	import java.nio.channels.SocketChannel;
 
 	import java.io.IOException;
 	import java.io.InputStream;
 	import java.io.OutputStream;
 	import java.net.InetSocketAddress;
 import java.net.Socket;
 	import java.net.UnknownHostException;
 
 	import com.kuka.common.StatisticTimer;
 	import com.kuka.common.StatisticTimer.OneTimeStep;
 
 	import com.kuka.roboticsAPI.geometricModel.math.ITransformation;
 import com.kuka.roboticsAPI.geometricModel.math.Matrix;
 	import com.kuka.roboticsAPI.geometricModel.math.MatrixTransformation;
 import com.kuka.roboticsAPI.geometricModel.math.Vector;
 
 
 //This is the Robot Communication Simulator using some of KUKA-library
 
 
 
 public class SimRobotApp {
 	
 	 public static void main(String[] args) throws IOException{
 
 		 	//Initializing some variables and stuff in the real Programm the robot and controller will set here
 		 // So this is hear so the Sim Programm got a similiar appearance to the programm running on the robot later on
 		 	initialize();
 			runRealtimeMotion();
 
 		}
 
 	 	//Member variables for Virtual Fixtures
 		private static Vector plane_ap;
 		private static Vector plane_n;
 		private static Vector cone_ap;
 		private static Vector cone_n;
 	    static double  cone_phi;
 		private static int mVFtype=0;
 	    
 	    private static int _numRuns = 10000;
 
 	    //State Machine
 		private static int mCurrentState = 0;
 		private static int mOldState = 0;
 		
 		//Some Falgs for the Transitions
 		static boolean RegistrationFinished = false;
 		static boolean DataSend=false;
 		static boolean RecievedT_CT_Base= false;
 		static boolean mfirstStep=false;
 		
 		//Some Variables for Stiffness Control
 		static double mdistance;
 		static double mawaredist;
 		static double mlast_distance=0.0;
 		static double mStiffVal=0.0;
 		static int mError= 0;
 		
 		
 		//and the Registration...
 		private static int pointstoregister;
 		private static int registeredpoints = 0;
 		private static Vector sumPosition = Vector.of(0.0, 0.0, 0.0);
 		private static Vector SumSquares = Vector.of(0.0, 0.0, 0.0);
 		private static Vector StandardDeviation = Vector.of(100, 100, 100);
 		private static int N= 10;
 		private static double Threshhold_Pose = 0.2;
 		private static Vector[] OldestValues= null;
 		private static Vector LastSavedPosition= null;
 		private static Vector[] RegPoints = null;
 		private static  int posemeasured=0;
 
 		//Container for the Transformation from Roboterbase to Imagespace
 		private static MatrixTransformation T_CT_Base;
 		
 		//..the Socket Communication
 	   private static Socket ProxySocket = null;;
 	   private static OutputStream out = null;
 	   private static InputStream in = null;
 	    
 	    public static void initialize()
 	    {
 			    //The Translation to the Tool Tip in mm
 				double translationOfTool[] =
 				{ 0, 0, 100 };
 				
 				plane_ap = Vector.of(0, 0, 10.0);
 				plane_n = Vector.of(0, 0, 1);
 				mawaredist = 100;
 
 				pointstoregister = 4;
 				OldestValues = new Vector[100];
 				RegPoints = new Vector[pointstoregister];
 			
 
 
 	    }
 
 
 
 		/**
 		 * In this Function the Stiffness value for the case that the robot is getting closer to a virtual fixture is calculated
 
 		 */
 		
 
 		public static double get_stiffness_value_approach( double dist, double awaredist)	//Berechnung der Dmpfung bei Annherung an die Grenze
 		 {
 			double max_stiff=1300;
 			if (dist>=awaredist) return 0.01;
 			else if (dist<awaredist && dist>0)
 			{
 				double y;
 				double m= max_stiff/Math.pow(awaredist,2);
 				y =m*Math.pow(awaredist-dist,2); 
 				return y;
 			}
 			else return max_stiff;
 		 }
 		
 		
 
 		/**
 		 * In this Function the Stiffness value for the case that the robot is getting further away from a virtual fixture is calculated
 
 		 */
 		 public static double get_stiffness_value_remove( double dist, double awaredist)	//Berechnung der Dmpfung bei Entfernung von der Grenze
 		 { 	
 
 			
 			double zero_point=awaredist/2;
 			double max_stiff=500;
 			if (dist>=zero_point) return 0.01;
 			else if (dist<zero_point && dist>0)
 			{
 				double y;
 				double m= max_stiff/Math.pow(zero_point,3);
 				y =m*Math.pow(zero_point-dist,3); 
 				return y;
 			}
 			else return max_stiff;
 		 }
 		 
 		 
 		 
 	    /**
 	     * In this mode the current position is saved and a floating average is calculated if the std of this average is smaller then the
 	     *  threshhold the mean value is saved as one of the registration Points
 	     */
 		 public static int LBRRegistration(MatrixTransformation curPose, int curIndex, boolean isFirstStep ){
 			
 
 				// We are in CartImp Mode,
 				// Modify the settings:
 				// NOTE: YOU HAVE TO REMAIN POSITIVE SEMI-DEFINITE !!
 				// NOTE: DONT CHANGE TOO FAST THE SETTINGS, ELSE YOU
 				// WILL DESTABILIZE THE CONTROLLER
 				double aTransStiffVal =0;
 				double aRotStiffVal = 0;
 				
 				// check if the Tool has been moved regarding the lastSaved Position
 				Vector DeltaPosition=LastSavedPosition.subtract(curPose.getTranslation());
 				double DeltaNorm = Math.sqrt(Math.pow(DeltaPosition.getX(),2) +Math.pow(DeltaPosition.getY(), 2) + Math.pow(DeltaPosition.getZ(),2));
 				if(DeltaNorm>= 50){
 					
 					//Saving N Points into an Array
 					if(posemeasured<N){
 						sumPosition = sumPosition.add(curPose.getTranslation());
 						double xSquare = curPose.getTranslation().getX()*curPose.getTranslation().getX();
 						double ySquare = curPose.getTranslation().getY()*curPose.getTranslation().getY();
 						double zSquare = curPose.getTranslation().getZ()*curPose.getTranslation().getZ();
 						SumSquares = SumSquares.add(Vector.of(xSquare, ySquare, zSquare));
 						OldestValues[posemeasured] = curPose.getTranslation();
 						posemeasured++;
 						
 					}else{//Calculationg the floating average
 						sumPosition = sumPosition.subtract(OldestValues[curIndex]);
 						double xSquareOld = OldestValues[curIndex].getX()*OldestValues[curIndex].getX();
 						double ySquareOld = OldestValues[curIndex].getY()*OldestValues[curIndex].getY();
 						double zSquareOld = OldestValues[curIndex].getZ()*OldestValues[curIndex].getZ();
 						SumSquares = SumSquares.subtract(Vector.of(xSquareOld, ySquareOld, zSquareOld));
 						double xSquare = curPose.getTranslation().getX()*curPose.getTranslation().getX();
 						double ySquare = curPose.getTranslation().getY()*curPose.getTranslation().getY();
 						double zSquare = curPose.getTranslation().getZ()*curPose.getTranslation().getZ();
 						SumSquares = SumSquares.add(Vector.of(xSquare, ySquare, zSquare));
 						sumPosition = sumPosition.add(curPose.getTranslation());
 						OldestValues[curIndex] = curPose.getTranslation();
 						curIndex++;
 					
 						if (curIndex==100){
 							curIndex=0;
 						}
 						
 						//Check if the StandardDeviation is less then Threshhold => no movement save Mean Value as Registration Point
 						StandardDeviation = SumSquares.subtract(Vector.of(sumPosition.getX()*sumPosition.getX(), sumPosition.getY()*sumPosition.getY(), sumPosition.getZ()*sumPosition.getZ())).multiply( N/ (N * (N - 1)));
 						if (StandardDeviation.getX() <= Threshhold_Pose && StandardDeviation.getY() <= Threshhold_Pose && StandardDeviation.getZ() <= Threshhold_Pose){
 							
 							RegPoints[registeredpoints] = Vector.of( (1/((double)N))*sumPosition.getX(), (1/((double)N))*sumPosition.getY(), (1/((double)N))*sumPosition.getZ());
 							LastSavedPosition = RegPoints[registeredpoints];
 							
 							//Reseting Variables for the data array
 							posemeasured = 0;
 							sumPosition = Vector.of( 0, 0, 0);
 							SumSquares = Vector.of(0,  0, 0);
 							curIndex = 0;
 							StandardDeviation = Vector.of(100, 100, 100);
 							
 							//Increase registered points
 							registeredpoints++;
 							System.out.println(registeredpoints + ". point succesfully saved!!");
 						}
 					}
 					
 					
 				}
 				//For Debugging just send some data and that registeredpoints to pointstoregister so next state could be change to the next State
 				RegPoints[0] = Vector.of( 1, 2, 1);
 				RegPoints[1] = Vector.of( 2, 3, 2);
 				RegPoints[2] = Vector.of( 3, 4, 3);
 				RegPoints[3] = Vector.of( 4, 5, 4);
 				registeredpoints=pointstoregister;
 				
 				//Return the current Position
 				return curIndex;
 		 }
 		 
 		 /**
 		     * In this Function the Registrationdata is Send to the state control
 		     */
 		 public static boolean SendRegistrationData(int State, Vector [] RegPoints, int n){
 			 
 			 byte[] out_array= new byte [128];
 			 byte[] tmp = new byte[Double.SIZE/8];
 			 boolean check;
 			 out_array[0] = 'R';
 			 out_array[1] = (byte) mCurrentState;
 			 out_array[2] = (byte) mError;
 			
 			 
 			 for(int l= 0; l<pointstoregister;l++){
 				 for(int  f =0; f<3; f++){
 					 //Wrapping the Double data to an byte array
 					 ByteBuffer.wrap(tmp).putDouble(RegPoints[l].get(f));
 					 for(int m =0; m<(Double.SIZE/8); m++){
 						 //Switch the byte order
 						 out_array[3 + (Double.SIZE/8)*f+(Double.SIZE/8)*3*l +7- m] = tmp[m];
 					 }
 
 				 }
 		
 			 }
 			 //Sending the data to Slicer/Proxy
 		        try {
 					out.write(out_array);
 					check = true;
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 					check = false;
 				}
 	       
 			 return check;
 		 }
 		 
 		 /**
 		     * Gravitation Compensation mode
 		     */
 		 public  static void GravComp(boolean isFirstStep){
 
 				   
 				   if (isFirstStep)
 				   {
 						// We are in CartImp Mode,
 						// Modify the settings:
 						// NOTE: YOU HAVE TO REMAIN POSITIVE SEMI-DEFINITE !!
 						// NOTE: DONT CHANGE TOO FAST THE SETTINGS, ELSE YOU
 						// WILL DESTABILIZE THE CONTROLLER
 						double aTransStiffVal =0;
 						double aRotStiffVal = 0;
 				   }
 
 
 
 	
 				 
 				  
 		 }
 		 
 		 
 		 public static void VirtualFixtures(MatrixTransformation curPose){
 			// Berechnung des Abstandsvektors zur Ebene
 			 	MatrixTransformation curCmdCartPose;
 				Vector dv = curPose.getTranslation().subtract(plane_ap);
 				mdistance = plane_n.dotProduct(dv);
 				Vector normvec = plane_n;
 				double aDampVal=0.0, StiffVal =0.0 ;
 				
 				// setzen der Stellgren D und K
 				if (mdistance < mawaredist)
 				{
 					aDampVal = 0.7;
 					
 					if (mdistance >=0)	// im zulssigen Bereich
 					{ 	
 						
 						
 						if (mdistance-mlast_distance <= 0) // EE nhert sich an die Grenze	
 						{
 							mStiffVal = get_stiffness_value_approach(mdistance, mawaredist);
 							
 							
 						}	
 						else 							 // EE entfernt sich von der Grenze
 						{
 							mStiffVal = get_stiffness_value_remove(mdistance, mawaredist);
 						}
 						curCmdCartPose = MatrixTransformation
 								.of(curPose.getTranslation(), curPose.getRotation());
 					}
 					else 				// im gesperrten Bereich
 					{
 						mStiffVal = 5000;
 						curCmdCartPose = MatrixTransformation
 								.of(curPose.getTranslation().subtract(normvec.multiply(mdistance)), curPose.getRotation());
 					}
 				}
 				else 
 				{			// auerhalb des Gefahrenbereichs
 					mStiffVal = 0.01;
 						aDampVal = 0.7;
 						curCmdCartPose = MatrixTransformation
 								.of(curPose.getTranslation(), curPose.getRotation());
 				}
 				Vector aTransStiffVal = Vector.of(Math.abs(normvec.getX()*mStiffVal), Math.abs(normvec.getY()*mStiffVal), Math.abs(normvec.getZ()*mStiffVal));
 				// Transformieren der Impedanzwerte in das Flanschkoordinatensystem			
 				double aRotStiffVal = mStiffVal*300/5000;
 				double aNullStiffVal = mStiffVal;
 				 
 				Vector TransStiffVal_tool = curPose.getRotation().getMatrix().multiply(aTransStiffVal);
 				if(TransStiffVal_tool.getX()<=0) TransStiffVal_tool.withX(0.01);
 				if(TransStiffVal_tool.getY()<=0) TransStiffVal_tool.withY(0.01);
 				if(TransStiffVal_tool.getZ()<=0) TransStiffVal_tool.withZ(0.01);
 					
 				mlast_distance = mdistance;
 
 			    
 			
 			    if (true)
 				    {
 					// We are in CartImp Mode,
 					// Modify the settings:
 					// NOTE: YOU HAVE TO REMAIN POSITIVE SEMI-DEFINITE !!
 					// NOTE: DONT CHANGE TOO FAST THE SETTINGS, ELSE YOU
 					// WILL DESTABILIZE THE CONTROLLER
 				    }
 			 
 			 
 		 }
 		
 		 
 		 //Reading Current state
 		public static int GetCurrentState(){
 			 byte[] buffer= new byte [128];
 
 			// Reading data from Socket
 	        try {
 				in.read(buffer, 0, 128);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 	        
 	        if(buffer[1] == 5)//State is Virtual Fixtures then the VF Definition is send as well
 	        {
 	        	mVFtype =(int)buffer[2];
 	        	double tmpDouble;
 	        	double x_ap=0, y_ap=0, z_ap=0, x_n=0, y_n=0, z_n=0;
 	        	byte [] tmpArray = new byte [8];
 	        	
 	        	for (int m=0; m<6+mVFtype; m++){
 	        		//the order of Bytes needs to be switched so start with 7-n
 	        		for(int n=0; n<8; n++){
 	        			tmpArray[7-n]= buffer[3 + m*8+n];
 	        		}
 	        		//Wrap to a double Buffer
 	        		tmpDouble = ByteBuffer.wrap(tmpArray).getDouble();
 	        		
 	        		//And store into Temporary varaibles
 					if(m==0) x_ap =tmpDouble;
 	        		if(m==1)y_ap =tmpDouble;
 	        		if(m==2)z_ap =tmpDouble;
         		
         		
 	        		if(m==3)x_n =tmpDouble;
 	        		if(m==4)y_n =tmpDouble;
 	        		if(m==5)z_n = tmpDouble;
 	        		
 	        		if(m==6)cone_phi=tmpDouble;
 	        	}
 	        	//Finally Creating Vectors
 	        	if(mVFtype ==0){
 	        		
 	        			plane_ap= Vector.of(x_ap, y_ap, z_ap);
 	        			plane_n= Vector.of(x_n, y_n, z_n);
 	        			//if its not normalize the do it
 	        			if(Math.sqrt(plane_n.dotProduct(plane_n))!=1){
 	        				plane_n= plane_n.normalize();
 	        			}
 	        	}else{
 	        			cone_ap= Vector.of(x_ap, y_ap, z_ap);
 	        			cone_n= Vector.of(x_n, y_n, z_n);
 	        			//if its not normalize the do it
 	        			if(Math.sqrt(plane_n.dotProduct(plane_n))!=1){
 	        				plane_n= plane_n.normalize();
 	        			}
 	        			
 	        	}
 	        }else if(buffer[1] == 3)//State is WaitforTCtBase
 	        {
 	        	double[] tmpMatrix = new double [12];
 	        	byte [] tmpArray = new byte [8];
 	        	for (int m=0; m<12; m++){
 	        		for(int n=0; n<8; n++){
 	        			tmpArray[7-n]= buffer[2 + m*8+n];
 	        		}
 	        		tmpMatrix[m] = ByteBuffer.wrap(tmpArray).getDouble();		
 	        	}
 	        	T_CT_Base= MatrixTransformation.of(Vector.of(tmpMatrix[3],tmpMatrix[7], tmpMatrix[11]), Matrix.ofRowFirst(tmpMatrix[0], tmpMatrix[1], tmpMatrix[2], tmpMatrix[4], tmpMatrix[5], tmpMatrix[6], tmpMatrix[8], tmpMatrix[9], tmpMatrix[10]));
 	        	//Check if the determinant of the Rotation Matrix is 1 and that the flag
 	        	if(T_CT_Base.getRotationMatrix().determinant()==1)
 	        		RecievedT_CT_Base=true;
 	        }else if(buffer[1] == 1){//State is Registration
 	        	//Set number of points for Registration
 	        	pointstoregister = buffer[2];
 	        }
 			return buffer[1];
 		}
 		
 		
 		//Sending the current state to Proxy/Slicer
 		public static boolean SendCurrentState(){
 			 byte[] out_array= new byte [128];
 			 boolean check =false;
 			 out_array[0] = 'S';
 			 out_array[1] =(byte) mCurrentState;
 			 out_array [2] = (byte) mError;
 			 
 			 //for some of the states adding some data
 			 if(mCurrentState == 1){//Current State Registration=> send flag RegistrationFinished 
 				 if(RegistrationFinished)out_array[3] = 1;
 				 else out_array[3]=0;
 				 
 			 }if(mCurrentState == 3) {//State is Wait for T_CT_Base=> send flag ReceivedT_CT_Base
 				 if(RecievedT_CT_Base)out_array[2] = 1;
 				 else out_array[3]=0;
 			 }
 			//Sending bytearray via socket
 	       try {
 				out.write(out_array);
 				check = true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				check = false;
 			}
 	       
 			return check;
 		}	
 		
 		//Sending the Transformation to the Slicer side/IGT Proxy
 		public static boolean SendTransform(MatrixTransformation matrix){
 			 byte [] out_array= new byte[128];
 			 boolean check =false;
 			 byte[] tmp = new byte[(Double.SIZE/8)];
 			 out_array[0] =  'T';
 			 out_array[1] =(byte) mCurrentState;
 			 out_array[2] = (byte) mError;
 			 int k=0;
 			 MatrixTransformation CurPoseCOF_CT;
 			 
 			 //Transform it to Imagespace
 			 CurPoseCOF_CT = T_CT_Base.compose(matrix);
 			 //Writing the Transformation into byte array 
 			 for(int l= 0; l<3;l++){
 				 for(int f =0;f<4; f++){
 					 if(f!=3){
 						 ByteBuffer.wrap(tmp).putDouble( CurPoseCOF_CT.getRotation().getMatrix().get(l, f));
 					 }else if(f==3){
 						 ByteBuffer.wrap(tmp).putDouble( CurPoseCOF_CT.getTranslation().get(l));
 					 }
 					 for(int m =0; m<(Double.SIZE/8); m++){
 						 out_array[3 + (Double.SIZE/8)*f+(Double.SIZE/8)*4*l +7- m] = tmp[m];
 					 }
 
 					 
 				 }
 			 }
 		//Sending byte array
 	      try {
 	    	  out.write(out_array);
 				check = true;
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				check = false;
 			}
 	      
 			return check;
 		}
 		
 		
 		//Generates just some "Random" Positions to SImulate the Data send by robot
 		public static MatrixTransformation GetRandomPosition(){
 			
 			  double[] position= new double[3];
 			  double[] orientation  = new double [4];
 			  
 
 			  // random position
 			  double alpha = 0.0;
 			  position[0] = 50.0 +Math.floor(alpha);//* Math.cos(alpha);
 			  position[1] = 50.0 +Math.floor(alpha);
 			  position[2] = 50.0 + Math.floor(alpha);
 			  alpha = alpha + 0.05;
 
 			  // random orientation
 			  double theta = 0.0;
 			  orientation[0]=0.0;
 			  orientation[1]=0.6666666666*Math.cos(theta);
 			  orientation[2]=0.577350269189626;
 			  orientation[3]=0.6666666666*Math.sin(theta);
 			  theta = theta + 0.1;
 
 			  MatrixTransformation RandomPose = MatrixTransformation.of(Vector.of(position[0], position[1], position[2]), Matrix.ofColumnFirst(1, 0, 0, 0, 1, 0, 0, 0, 1));
 			  return RandomPose;
 			  
 			
 			}
 
 		//the RealTime Motion loop including the State machine and the Socket Communication
 	    public static void runRealtimeMotion()
 	    {
 
 		boolean doDebugPrints = false;
 		int j=0;
 
 		
 		System.out.println("Starting Simulation");
 
 
 		// For Roundtrip time measurement...
 		StatisticTimer timing = new StatisticTimer();
 		System.out.println("Try to Connect to Server");
 
 		try {
 	    	//Verbindungsaufbau zu einem Socket
 			ProxySocket = new Socket("localhost", 22222);
 	        out = ProxySocket.getOutputStream();
 	        in = ProxySocket.getInputStream();
 	        //Timeout setzen
 	        ProxySocket.setSoTimeout(300);
 	        ProxySocket.setTcpNoDelay(true);
 	    } catch (UnknownHostException e) {
 	        System.err.println("Unknown host!");
 	    } catch (IOException e) {
 	        System.err.println("Couldn't get I/O for "
 	                           + e + "the connection to: 192.168.42.1.");
 	    }
 		
 		/*
 	    try {
 	        Socket= SocketChannel.open();
 	        Socket.configureBlocking(false);
 	        // make sure to call sc.connect() or else
 	        // calling sc.finishConnect() will throw
 	        // java.nio.channels.NoConnectionPendingException
 	        Socket.connect(new InetSocketAddress("localhost", 18944));
 	      
 	    	//Verbindungsaufbau zu einem Socket
 	        in = Socket.socket().getInputStream();
 	    } catch (UnknownHostException e) {
 	    	System.out.println("Unknown host!");
 	    } catch (IOException e) {
 	    	System.out.println("Couldn't get I/O for "
 	                           + "the connection to: 192.168.42.1.");
 	    }
 	    System.out.println("and go on...");
 	    boolean check;
 	    try{
 	    	check = Socket.finishConnect();
 	    }catch (IOException e){
 	    	check = false;
 	    }
 	    while (!check) {
 	    	try{
 	        	check = Socket.finishConnect();
 	        }catch (IOException e){
 	        	check = false;
 	        }
 	    	System.out.println("Wait for Connction....");
 	    }
 	    
 	   	System.out.println("Finally connected");*/
 		
 		try
 		{
 			long startTimeStamp = System.nanoTime();
 		    // do a cyclic loop
 			
 			
 			ITransformation curMsrCartPose = GetRandomPosition();
 			LastSavedPosition= curMsrCartPose.getTranslation();
 			
 		    for (int i = 0; i < _numRuns; ++i)
 		    {
 			// Timing - draw one step
 			OneTimeStep aStep = timing.newTimeStep();
 			// ///////////////////////////////////////////////////////
 			//
 			if(ProxySocket.isConnected())mCurrentState = GetCurrentState();
 			else mCurrentState =0;//In case there is no Connection to Proxy thet Sate to Idle
 			curMsrCartPose = GetRandomPosition();
 			double curTime = System.nanoTime() - startTimeStamp;
 			
 			switch (mCurrentState ) {
 				case 0: //State IDLE
 					
 					if(mOldState != 0){
 						System.out.println("IDLE");
 						RegistrationFinished = false;
 						DataSend = false;
 						RecievedT_CT_Base= false;
 						mOldState = 0;
 						j=0;
 					}
 					mError = 0;
 					if(ProxySocket.isConnected())SendCurrentState();
 					break;
 
 				case 1: //State Registration
					if(mOldState == 0) {
 						if (mOldState != mCurrentState) {
 							mfirstStep = true;
 							LastSavedPosition= curMsrCartPose.getTranslation();
 							System.out.println("Registration");
 						}else{ mfirstStep = false;}
 						if(RegistrationFinished == false){
 								j = LBRRegistration(MatrixTransformation.of(curMsrCartPose.getTranslation(), curMsrCartPose.getRotation()), j, mfirstStep);
 						}
 						mOldState = mCurrentState;
 						if ( registeredpoints == pointstoregister){
 							
 							if(RegistrationFinished == false){
 								System.out.println("Registration finished");
 								System.out.println("Points 1: " + RegPoints[0].getX() + " " + RegPoints[0].getY() + " " + RegPoints[0].getZ()  );
 								System.out.println("Points 2: " + RegPoints[1].getX() + " " + RegPoints[1].getY() + " " + RegPoints[1].getZ()  );
 								System.out.println("Points 3: " + RegPoints[2].getX() + " " + RegPoints[2].getY() + " " + RegPoints[2].getZ()  );
 								System.out.println("Points 4: " + RegPoints[3].getX() + " " + RegPoints[3].getY() + " " + RegPoints[3].getZ()  );
 							}
 							RegistrationFinished = true;
 							mError =0;
 						}
 					}else{
 						
 						//Set State to IDLE
 						mCurrentState = 0;
 						mError = 1;
 					}
 
 					if(ProxySocket.isConnected())SendCurrentState();
 				
 				break;
 				
 				case 2: //State SendData
 				if((mOldState == 1|| mOldState==2) && RegistrationFinished==true){
 					System.out.println("Sending Data");
 					mError = 0;
 					DataSend =  SendRegistrationData(mCurrentState, RegPoints,registeredpoints );
 					mOldState = mCurrentState;
 				}else{
 					mCurrentState=mOldState;
 					mError = 1;
 					if(ProxySocket.isConnected())SendCurrentState();
 
 				}
 				break;
 				
 				case 3://State Wait for T_CT_robbase
 					if((mOldState == 2|| mOldState == 3) && DataSend==true){
 						if(mOldState!=mCurrentState){
 							System.out.println("Wait for Transformation to Image Space");
 						}
 						mOldState = mCurrentState;
 						mError = 0;
 					}else{
 						mError = 1;
 					}
 					if(ProxySocket.isConnected())SendCurrentState();
 					break;
 		
 				case 4://GravComp
 						if (mOldState != mCurrentState) {
 							mfirstStep = true;
 							System.out.println("GravComp");
 						}
 						else mfirstStep = false;
 						mOldState = mCurrentState;
 						GravComp(mfirstStep );	
 						mError = 0;
 						if(ProxySocket.isConnected())SendCurrentState();
 					break;
 		
 				case 5://Virtual Fixtures
 					if(RecievedT_CT_Base == true){
 						mOldState = mCurrentState;
 						VirtualFixtures(MatrixTransformation.of(curMsrCartPose.getTranslation(), curMsrCartPose.getRotation()));
 						if (i % 100 == 0){ 
 							System.out.println( "Distance"
 								    + mdistance);
 							    System.out.println(" Stiffness"
 								    +  mStiffVal);
 						}
 						if (mOldState != mCurrentState) {
 							mfirstStep = true;
 							System.out.println("Virtual Fixtures");;
 						}
 						else mfirstStep = false;
 						mError = 0;
 						
 					}else{
 						mError = 1;
 					}
 					if(ProxySocket.isConnected())SendCurrentState();
 					break;
 		
 				case 6://NavGravComp
 					if(RecievedT_CT_Base == true){
 						if (mOldState != mCurrentState) {
 							mfirstStep = true;
 							System.out.println("GravComp");
 						}
 						else mfirstStep = false;
 						mOldState = mCurrentState;
 						GravComp(mfirstStep );
 						System.out.println("NavGravComp");
 						mError=0;
 						if(ProxySocket.isConnected())SendTransform(MatrixTransformation.of(curMsrCartPose.getTranslation(), curMsrCartPose.getRotation()));
 					}else{
 						mError = 1;
 						if(ProxySocket.isConnected())SendCurrentState();
 					}
 				case 7://NavGravCompVF
 					if(RecievedT_CT_Base == true){
 						mOldState = mCurrentState;
 						VirtualFixtures(MatrixTransformation.of(curMsrCartPose.getTranslation(), curMsrCartPose.getRotation()));
 						if (i % 100 == 0){ 
 							System.out.println( "Distance"
 								    + mdistance);
 							    System.out.println(" Stiffness"
 								    +  mStiffVal);
 						}
 						if (mOldState != mCurrentState) {
 							mfirstStep = true;
 							System.out.println("Virtual Fixtures");;
 						}
 						else mfirstStep = false;
 						mError = 0;
 						if(ProxySocket.isConnected())SendTransform(MatrixTransformation.of(curMsrCartPose.getTranslation(), curMsrCartPose.getRotation()));
 						
 					}else{
 						mError = 1;
 						if(ProxySocket.isConnected())SendCurrentState();
 					}
 					break;
 			default:
 				System.out.println("Unknown State Request");
 				mCurrentState = 0;
 				mError = 1;
 				if(ProxySocket.isConnected())SendCurrentState();
 				break;
 			}
 			// Overall timing end
 			aStep.end();
 
 		    } // end for
 		}
 		catch (Exception e)
 		{
 		    System.out.println(e);
 		    e.printStackTrace();
 		}
 
 
 		System.out.println("Statistic Timing of Overall Loop " + timing);
 		if (timing.getMeanTimeMillis() > 150)
 		{
 		    System.out
 			    .println("Statistic Timing is unexpected slow, you should try to optimize TCP/IP Transfer");
 		    System.out
 			    .println("Under Windows, you should play with the registry, see the e.g. the RealtimePTP Class javaDoc for details");
 		}
 	    }
 
 
 
 
 	    /** Sample to switch the motion control mode */
 	    /** Sample to switch the motion control mode */
 	// end for
 
 	   
 	}
 
 
 
