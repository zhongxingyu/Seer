 package dk.itu.mario;
 
 import Jama.*;
 
 public class XPFilter {
 		
 	private Matrix x; // initial state (location and velocity) - estimate
 	private	Matrix P; // initial uncertainty - uncertainty covariance
 	private Matrix u; // external motion - motion vector
 	private Matrix F; // next state function - state transition matrix
 	private Matrix H; // measurement function 
 	private Matrix R; // measurement uncertainty - noise
 	private Matrix I; // identity matrix
 	//filter(x,P)
 	public XPFilter(int observedVarSize, int hiddenVarSize){
 		int m = observedVarSize, n = hiddenVarSize; 
 		x = new Matrix(m,1);
 		u = new Matrix(m+n,1);
 		P = new Matrix(m+n,m+n);
 		F = new Matrix(m+n, m+n);
 		H = Matrix.identity(m,m+n);
 		R = new Matrix(m,m);
 		I = Matrix.identity(m+n, m+n);
 	}
 	
 	/******************************************************************
 	*FOR N Dimensional 	
 
 	
 	*/
 	
 	public void init(Matrix initState, Matrix initSig, Matrix noise, Matrix transitionMatrix){
 		x = initState;
 		F = transitionMatrix;
 		//must be passed in as mx1 matrices
		for(int i=0;i<P.getRowDimension();i++){ P.set(i, i, initSig.get(i, 1));}
		for(int i=0;i<R.getRowDimension();i++){R.set(i, i, noise.get(i, 1));}
 	}
 	
 	
 	public Matrix sample(Matrix measurement){
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
 		
 		return x.copy();
 	}
 	
 }
