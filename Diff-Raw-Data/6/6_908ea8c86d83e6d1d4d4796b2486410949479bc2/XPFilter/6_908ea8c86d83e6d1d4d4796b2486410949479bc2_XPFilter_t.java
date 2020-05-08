 package dk.itu.mario;
 
 import Jama.*;
 
 public class XPFilter {
 		
 	private Matrix x; // initial state (location and velocity) - estimate
 	private	Matrix P; // initial uncertainty - uncertainty covariance
 	private Matrix u; // external motion - motion vector
 	private Matrix F; // next state function - state transition matrix
 	private Matrix H; // measurement function 
 	private Matrix R; // measurement uncertainty
 	private Matrix I; // identity matrix
 	//filter(x,P)
	public XPFilter(){
		//Assume 4x4 
		//R = Matrix()
		I = Matrix.identity(4, 4);
	}
 	
 	/******************************************************************
 	*FOR 4D
 	*
 	print "### 4-dimensional example ###"
 
 	measurements = [[5., 10.], [6., 8.], [7., 6.], [8., 4.], [9., 2.], [10., 0.]]
 	initial_xy = [4., 12.]
 
 	# measurements = [[1., 4.], [6., 0.], [11., -4.], [16., -8.]]
 	# initial_xy = [-4., 8.]
 
 	# measurements = [[1., 17.], [1., 15.], [1., 13.], [1., 11.]]
 	# initial_xy = [1., 19.]
 
 	dt = 0.1
 
 	x = matrix([[initial_xy[0]], [initial_xy[1]], [0.], [0.]]) # initial state (location and velocity)
 	u = matrix([[0.], [0.], [0.], [0.]]) # external motion
 
 	#### DO NOT MODIFY ANYTHING ABOVE HERE ####
 	#### fill this in, remember to use the matrix() function!: ####
 
 	P = matrix([[0,0,0,0],[0,0,0,0],[0,0,1000,0],[0,0,0,1000]]) # initial uncertainty
 	F = matrix([[1,0,dt,0],[0,1,0,dt],[0,0,1,0],[0,0,0,1]]) # next state function
 	H = matrix([[1,0,0,0],[0,1,0,0]]) # measurement function
 	R = matrix([[.1,0],[0,.1]]) # measurement uncertainty
 	I = matrix([[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]]) # identity matrix
 	*/
 	
 	public void init(Matrix initState, Matrix initSig){
 		
 		//x = matrix([[initial_xy[0]], [initial_xy[1]], [0.], [0.]]); // initial state (location and velocity)
 		//u = matrix([[0.], [0.], [0.], [0.]]); // external motion
 		//P = matrix([[0,0,0,0],[0,0,0,0],[0,0,1000,0],[0,0,0,1000]]); // initial uncertainty
 		//F = matrix([[1,0,dt,0],[0,1,0,dt],[0,0,1,0],[0,0,0,1]]); // next state function
 		//H = matrix([[1,0,0,0],[0,1,0,0]]); // measurement function
 		//R = matrix([[.1,0],[0,.1]]); // measurement uncertainty
 		//I = matrix([[1,0,0,0],[0,1,0,0],[0,0,1,0],[0,0,0,1]]); // identity matrix
 	}
 	
 	
 	public Matrix filter(Matrix measurement){
 		//measurement update
 		Matrix Z,y,S,K;
 		// Z = matrix([measurements[n]])
         //y = Z.transpose() - (H * x)
         //S = H * P * H.transpose() + R
         //K = P * H.transpose() * S.inverse()
         //x = x + (K * y)
         //P = (I - (K * H)) * P
 		Z = measurement;
 		y = Z.transpose().minus(H.times(x));
 		S = H.times(P).times(H.transpose()).minus(R);
 		K = P.times(H.transpose()).times(S.inverse());
 		x = x.plus(K.times(y));
 		P = I.minus(K.times(H)).times(P);
 		
 		//prediction
 		//x = (F * x) + u
         //P = F * P * F.transpose()
 		x = (F.times(x)).plus(u);
 		P = F.times(P).times(F.transpose());
 		return P;
 	}
 	
 }
