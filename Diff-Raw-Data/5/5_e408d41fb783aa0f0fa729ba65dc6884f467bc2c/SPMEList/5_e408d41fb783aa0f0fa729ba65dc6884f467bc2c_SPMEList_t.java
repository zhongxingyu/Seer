 package pme;
 
 import gui.SpaceSize;
 
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 import jtransforms.DoubleFFT_1D;
 import jtransforms.DoubleFFT_2D;
 
 import math.Complex;
 import math.ErrorFunction;
 import math.MatrixOperations;
 import math.Vector;
 import particles.Particle;
 import particles.ParticleList;
 
 public class SPMEList extends ParticleList {
 	static int PADDING = 1; //Make the space size this many times larger to reduce period issues
 	static int CELL_SIDE_COUNT = 64; //K (Essman[95])
 	static int ASSIGNMENT_SCHEME_ORDER = 6;
 	
 	final double ewaldCoefficient; //The ewald coefficient
 	static double TOLERANCE = 1e-8;//Used to calculate ewaldCoefficient
 	static double CUTOFF_DISTANCE = 64;//Used to calculate ewaldCoefficient. In spaceSize dimensions
 	final int directRange; //will be CUTOFF_DISTANCE / meshWidth. In mesh cells
 	
 
 	ArrayList<Particle> cellList[][];
 	
 	double[][] Q; //the charge assignment matrix
 	double[][] B; //The B Matrix (Essman[95])
 	double[][] C; //The C Matrix (Essman[95])
 	Complex[] convolution;
 	Complex[][] convolutionMatrix;
 	double[] theta;
 	Complex[] complexTheta;
 	BSpline M;		//Order ASSIGNMENT_SCHEME_ORDER B spline
 	final double meshWidth; //H (Petersen[95])
 	final double inverseMeshWidth; //H^-1 (required for the reciprocal lattice)
 	final SpaceSize windowSize;
 	Vector[] reciprocalLatticeVectors; //Reciprocal lattice vectors
 	
 	//We have an array of 2D particles, but we assume the z component is 0
 	public SPMEList(ArrayList<Particle> particles, SpaceSize windowSize) {
 		super(particles);
 		this.windowSize = windowSize.scale(PADDING); //we make it empty around to help with period problems
 		this.meshWidth = (double)(windowSize.getWidth()) / CELL_SIDE_COUNT;
 		this.inverseMeshWidth = 1.0 / this.meshWidth;
 		this.directRange = (int)Math.ceil(CUTOFF_DISTANCE/meshWidth);
 		this.ewaldCoefficient = calculateEwaldCoefficient(CUTOFF_DISTANCE,TOLERANCE);
 		init();
 	}
 	
 	//subroutine ewaldcof from http://chem.skku.ac.kr/~wkpark/tutor/chem/tinker/source/kewald.f
 	private double calculateEwaldCoefficient(double cutoffDistance, double tolerance)
 	{
 		int i,k;
 		double x,xlo,xhi,y;
 		double ratio;
 		ratio = tolerance + 1.0;
 		x = 0.5;
 		i = 0;
 		while (ratio >= tolerance){
 			i = i + 1;
 			x = 2.0 * x;
 			y = x * cutoffDistance;
 			ratio = ErrorFunction.erfc(y) / cutoffDistance;
 		}
 		//use a binary search to refine the coefficient
 		k = i + 60;
 		xlo = 0.0;
 		xhi = x;
 		for(i=0;i<k;i++){
 			x = (xlo+xhi) / 2.0;
 			y = x * cutoffDistance;
 			ratio = ErrorFunction.erfc(y) / cutoffDistance;
 			if (ratio >= tolerance){
 				xlo = x;
 			}
 			else{
 				xhi = x;
 			}
 		}
 		return x;
 	}
 	
 	private void init()
 	{
 		initCellList();
 		M = new BSpline(ASSIGNMENT_SCHEME_ORDER);
 		initQMatrix();
 		initBMatrix();
 		initCMatrix();
 		
 		//Starting Eq 4.7 Essman[95]
 		double[] BC = MatrixOperations.makeRowMajorVector(MatrixOperations.straightMultiply(B, C));
 		DoubleFFT_1D fft = new DoubleFFT_1D(CELL_SIDE_COUNT*CELL_SIDE_COUNT);
		double[] qInverseFT = MatrixOperations.copyVector(MatrixOperations.makeRowMajorVector(Q), 2*CELL_SIDE_COUNT*CELL_SIDE_COUNT);
 		fft.realInverseFull(qInverseFT, false);
 		Complex[] product = MatrixOperations.straightMultiply(Complex.doubleToComplexVectorNoImaginaryPart(BC), Complex.doubleToComplexVector(qInverseFT));
 		double[] wideProduct = Complex.complexToDoubleVector(product);
 		fft.complexForward(wideProduct);
 		convolution = Complex.doubleToComplexVector(wideProduct);
 		
		theta = MatrixOperations.copyVector(BC, 2*CELL_SIDE_COUNT*CELL_SIDE_COUNT);
 		fft.realForwardFull(theta);
 		complexTheta = Complex.doubleToComplexVector(theta);
 		convolutionMatrix = MatrixOperations.make2DMatrix(convolution, CELL_SIDE_COUNT);
 		
 		System.out.println("Reciprocal energy: "+getRecEnergy());
 		System.out.println("Direct energy: "+getDirEnergy());
 		System.out.println("Corrected energy: "+getCorEnergy());
 		System.out.println("Actual energy: "+getActualEnergy());
 
 	}
 
 	
 	//Eq 4.6 Essman[95]
 	private void initQMatrix()
 	{
 		Q = new double[CELL_SIDE_COUNT][CELL_SIDE_COUNT];
 		for(int x = 0; x < CELL_SIDE_COUNT; x++)
 		{
 			for(int y = 0; y < CELL_SIDE_COUNT; y++)
 			{
 					double sum = 0;
 					for(Particle p : this)
 					{
 						double particleX,particleY,particleZ;
 						particleX = p.getPosition().re();
 						particleY = p.getPosition().im();
 						//Just before Eq 3.1 Essman[95]
 						double uX = (CELL_SIDE_COUNT) * (particleX / (double)(windowSize.getWidth()));
 						double uY = (CELL_SIDE_COUNT) * (particleY / (double)(windowSize.getWidth()));
 						//Removed periodic images? Seems to be off by a grid cell in x/y? FIXME?
 						double a = M.evaluate(uX-x);
 						double b = M.evaluate(uY-y);
 						sum += p.getCharge() * a * b;
 					}
 					Q[x][y] = sum;
 					
 				}
 			}
 	}
 	
 	//Eq 4.8 Essman[95]
 	private void initBMatrix()
 	{
 		B = new double[CELL_SIDE_COUNT][CELL_SIDE_COUNT];
 		for(int x = 0; x < CELL_SIDE_COUNT; x++)
 		{
 			for(int y = 0; y < CELL_SIDE_COUNT; y++)
 			{
 				B[x][y] = squared(M.b(1, x, CELL_SIDE_COUNT).abs())*squared(M.b(2, y, CELL_SIDE_COUNT).abs());
 			}
 		}
 	}
 	
 	//Eq 3.9 Essman[95]
 	private void initCMatrix()
 	{
 		C = new double[CELL_SIDE_COUNT][CELL_SIDE_COUNT];
 		double V = windowSize.getWidth() * windowSize.getHeight();
 		double c = 1.0 / (Math.PI * V);
 		C[0][0] = 0;
 
 		for(int x = 0; x < CELL_SIDE_COUNT; x++)
 		{
 			for(int y = 0; y < CELL_SIDE_COUNT; y++)
 			{
 
 				if(!(x==0 && y==0))
 				{
 					double mXPrime = (0 <= x && x <= CELL_SIDE_COUNT/2)? x : x - CELL_SIDE_COUNT;
 					double mYPrime = (0 <= y && y <= CELL_SIDE_COUNT/2)? y : y - CELL_SIDE_COUNT;
 					double m = mXPrime * inverseMeshWidth + mYPrime * inverseMeshWidth;
 					if(m != 0){ //FIXME: What to do if m==0?
 						C[x][y] = c * (Math.exp(-squared(Math.PI)* squared(m) / squared(ewaldCoefficient))) / squared(m);
 					}else{
 						C[x][y] = 0;
 					}
 				}
 
 			}
 		}
 	}
 	
 	private void initCellList(){
 		cellList = new ArrayList[CELL_SIDE_COUNT][CELL_SIDE_COUNT];
 		for(int i = 0; i < CELL_SIDE_COUNT; i++)
 		{
 			for(int j = 0; j < CELL_SIDE_COUNT; j++)
 			{
 				cellList[i][j] = new ArrayList<Particle>();
 			}
 		}
 		for(Particle p : this)
 		{
 			int cellX = (int)Math.floor(p.getPosition().re() / meshWidth);
 			int cellY = (int)Math.floor(p.getPosition().im() / meshWidth);
 			cellList[cellX][cellY].add(p);
 		}
 	}
 	
 	
 	private static double squared(double x){
 		return x*x;
 	}
 	
 	private double getRecEnergy(){
 		double sum = 0;
 		for(int x = 0; x < CELL_SIDE_COUNT; x++)
 		{
 			for(int y = 0; y < CELL_SIDE_COUNT; y++)
 			{
 				sum += 0.5*Q[x][y]*convolutionMatrix[x][y].re();
 			}
 		}
 		return sum;
 	}
 	
 	private double getDirEnergy(){
 		double sum = 0;
 		for(Particle p : this)
 		{
 			for(Particle q : getNearParticles(p,directRange))
 			{
 				if(!p.equals(q)){
 					double d = p.getPosition().sub(q.getPosition()).mag();
 					sum += p.getCharge()*q.getCharge()/d;
 				}
 			}
 		}
 		return 0.5*sum;
 	}
 	
 	private double getCorEnergy(){
 		double sum = 0;
 		for(Particle p : this)
 		{
 			sum += squared(p.getCharge());
 		}
 		return -ewaldCoefficient/Math.sqrt(Math.PI) * sum;
 	}
 	
 	private double getActualEnergy(){
 		double sum = 0;
 		for(Particle p : this)
 		{
 			for(Particle q : this)
 			{
 				if(!p.equals(q)){
 					double d = p.getPosition().sub(q.getPosition()).mag();
 					sum += p.getCharge()*q.getCharge()/d;
 				}
 			}
 		}
 		return 0.5*sum;
 	}
 	
 	//Uses a cell list method
 	public ArrayList<Particle> getNearParticles(Particle p, int range)
 	{
 		int cellX = (int)Math.floor(p.getPosition().re() / meshWidth);
 		int cellY = (int)Math.floor(p.getPosition().im() / meshWidth);
 		ArrayList<Particle> nearParticles = new ArrayList<Particle>();
 		for(int dx= -range; dx < range; dx++)
 		{
 			for(int dy= -range; dy < range; dy++)
 			{
 				int thisX = (cellX + dx + CELL_SIDE_COUNT)% CELL_SIDE_COUNT;
 				int thisY = (cellY + dy + CELL_SIDE_COUNT)% CELL_SIDE_COUNT;
 				nearParticles.addAll(cellList[thisX][thisY]);
 			}
 		}
 		return nearParticles;
 		
 	}
 	
 
 	
 	
 	@Override
 	public double charge(Complex position) {
 		int i = (int)(position.re() / meshWidth);
 		int j = (int)(position.im() / meshWidth);
 		return 0.5*Q[i][j]*convolutionMatrix[i][j].re();
 	}
 
 	@Override
 	public void debugDraw(Graphics g) {
 		
 	}
 	
 	
 
 }
