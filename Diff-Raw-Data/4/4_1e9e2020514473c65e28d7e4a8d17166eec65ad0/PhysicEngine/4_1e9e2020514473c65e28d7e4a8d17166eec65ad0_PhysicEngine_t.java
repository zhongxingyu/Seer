 package newOne;
 
 public class PhysicEngine {
 	
 	
 	public boolean[] calc;
 	/*
 	 * vars is the presentation of location and speed of every particle in the matter of 
 	 * 0	1	2	3		4	...
 	 * Ux	Uy	Vx	Vy		U1x	...
 	 * 
 	 * It is used for computating inside the PhysicEngine Class
 	 * The method modifyObjects() applies the computed locations and speeds to the pm
 	 */
 	public double[] vars;
 	
 	/*
 	 * The Particle Mngt to be worked with
 	 */
 	private Particle[] pm;
 	/*
 	 * Physical Constants
 	 */
 	
 	private double SPRINGCONSTANT = 6;
 	private double MASS = 0.5;
 	private double LEN = 100;
 	private double GRAVITY = 0;
 	private double DAMPING = 0.1;
 	
 	/*
 	 * vars is the presentation of location and speed of every particle in the matter of 
 	 * 0	1	2	3		4	...
 	 * Ux	Uy	Vx	Vy		U1x	...
 	 * 
 	 * It is used for computating inside the PhysicEngine Class
 	 * The method modifyObjects() applies the computed locations and speeds to the pm
 	 */
 	public PhysicEngine(Particle[] pm) {
 		this.pm = pm;
 		this.vars = new double[pm.length * 4];
 		for (int i = 0; i < pm.length; i++) {
 			vars[4*i] = pm[i].getLocation(0);
 			vars[4*i + 1] = pm[i].getLocation(1);
 			vars[4*i + 2] = pm[i].getSpeed(0);
 			vars[4*i + 3] = pm[i].getSpeed(1);
 		}
 		this.calc = new boolean[pm.length];
 		for (int i = 0; i < pm.length; i++) {
 			calc[i] = true;
 		}
 	}
 	/*
 	 * Runge Kutta Algorithm, computes the most accurate forecast of position for 4 iterations
 	 */
 	public void step(double stepSize) {
 		int i;
 		int N = vars.length;
 		double[] inp = new double[N];
 		double[] k1 = new double[N];
 		double[] k2 = new double[N];
 		double[] k3 = new double[N];
 		double[] k4 = new double[N];
 
 		evaluate(vars, k1); // evaluate at time t
 		for (i = 0; i < N; i++)
 			inp[i] = vars[i] + k1[i] * stepSize / 2; // set up input to diffeqs
 		evaluate(inp, k2); // evaluate at time t+stepSize/2
 		for (i = 0; i < N; i++)
 			inp[i] = vars[i] + k2[i] * stepSize / 2; // set up input to diffeqs
 		evaluate(inp, k3); // evaluate at time t+stepSize/2
 		for (i = 0; i < N; i++)
 			inp[i] = vars[i] + k3[i] * stepSize; // set up input to diffeqs
 		evaluate(inp, k4); // evaluate at time t+stepSize
 		// modify the variables
 		for (i = 0; i < N; i++)
			if (calc[i/4]){
 			vars[i] = vars[i] + (k1[i] + 2 * k2[i] + 2 * k3[i] + k4[i])
 					* stepSize / 6;
 			}
 
 	}
 
 	public void evaluate(double[] x, double[] change) {
 		for (int i = 0; i < vars.length; i++) {
 			int j = i % 4; // % is mod, so j tells what derivative is wanted:
 			// 0=Ux, 1=Uy, 2=Vx, 3=Vy
 			if ((j == 0) || (j == 1)) // requested derivative for Ux or Uy
 				change[i] = x[i + 2]; // derivative of position U is velocity V
 			else {
 				double r = 0;
 				double sc = SPRINGCONSTANT;
 
 					for (int t = 0; t < pm.length; t++) {
 						if ((i/4 != t)) {
 							double xx = x[4*t] - x[(i/4)*4];  // x distance between objects
 				            double yy = x[1 + 4*t] - x[1 + (i/4)*4];  // y distance betw objects
 				            double len = Math.sqrt(xx*xx + yy*yy);  // total distance betw objects
 							double f = (sc / MASS) * (len - LEN) / len;// Springforce
 							r += (j == 2) ? f * xx : -GRAVITY + f * yy; //apply to x or y Direction
 						}	
 				}
 					if (DAMPING != 0)
 				          r -= (DAMPING/MASS)*x[i];
 				change[i] = r;
 			}
 		}
 	}
 
 	public void modifyObjects() {
 		double w = pm[0].OUTER_RAD / 2;
 		for (int i = 0; i < pm.length; i++) {
 			pm[i].setLocation((vars[4 * i] - w), (vars[1 + 4 * i] - w));
 		}
 	}
 	/*
 	 * Sets Particles in a stable round position
 	 */
 	public void stop(){
 		//TODO Fix central point
 		double r = 1; // radius
 	    for (int i=0; i<pm.length; i++)
 	    {
 	      double rnd = 1+ 0.1*Math.random();
 	      vars[0 + i*4] = r*Math.cos(rnd*i*2*Math.PI/pm.length);
 	      vars[1 + i*4] = r*Math.sin(rnd*i*2*Math.PI/pm.length);
 	    }
 	    
 	}
 	public double getSPRINGCONSTANT() {
 		return SPRINGCONSTANT;
 	}
 	public void setSPRINGCONSTANT(double sPRINGCONSTANT) {
 		SPRINGCONSTANT = sPRINGCONSTANT;
 	}
 	public double getMASS() {
 		return MASS;
 	}
 	public void setMASS(double mASS) {
 		MASS = mASS;
 	}
 	public double getLEN() {
 		return LEN;
 	}
 	public void setLEN(double lEN) {
 		LEN = lEN;
 	}
 	public double getGRAVITY() {
 		return GRAVITY;
 	}
 	public void setGRAVITY(double gRAVITY) {
 		GRAVITY = gRAVITY;
 	}
 	public double getDAMPING() {
 		return DAMPING;
 	}
 	public void setDAMPING(double dAMPING) {
 		DAMPING = dAMPING;
 	}
 
 }
