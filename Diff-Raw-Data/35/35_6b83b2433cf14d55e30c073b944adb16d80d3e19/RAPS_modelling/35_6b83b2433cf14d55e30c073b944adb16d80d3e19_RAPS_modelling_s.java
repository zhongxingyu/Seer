 package assignment6;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Matrix4f;
 import javax.vecmath.Point3f;
 import javax.vecmath.Vector3f;
 
 import assignment4.LMatrices;
 import meshes.HalfEdge;
 import meshes.HalfEdgeStructure;
 import meshes.Vertex;
 import sparse.CSRMatrix;
 import sparse.CSRMatrix.col_val;
 import sparse.solver.Solver;
 
 
 
 /**
  * As rigid as possible deformations.
  * @author Alf
  *
  */
 public class RAPS_modelling {
 
 	//ArrayList containing all optimized rotations,
 	//keyed by vertex.index
 	ArrayList<Matrix3f> rotations;
 	
 	//A copy of the original half-edge structure. This is needed  to compute the correct
 	//rotation matrices.
 	private HalfEdgeStructure hs_originl;
 	//The halfedge structure being deformed
 	private HalfEdgeStructure hs_deformed;
 	
 	//The unnormalized cotan weight matrix, with zero rows for
 	//boundary vertices.
 	//It can be computed once at setup time and then be reused
 	//to compute the matrix needed for position optimization
 	CSRMatrix L_cotan;
 	//The matrix used when solving for optimal positions
 	CSRMatrix L_deform;
 	
 	//allocate righthand sides and x only once.
 	public ArrayList<Point3f> b;
 	public ArrayList<Point3f> x;
 
 	//sets of vertex indices that are constrained.
 	private HashSet<Integer> keepFixed;
 	private HashSet<Integer> deform;
 	
 	private Solver solver;
 
 	private CSRMatrix LTranspose;
 
 	private CSRMatrix M_constraints;
 	//for the svd.
 	Linalg3x3 l = new Linalg3x3(10);// argument controls number of iterations for ed/svd decompositions 
 									//3 = very low precision but high speed. 3 seems to be good enough
 	
 	private final static float w = 100f; //weight of user constraint
 
 	
 	/**
 	 * The mesh to be deformed
 	 * @param hs
 	 */
 	public RAPS_modelling(HalfEdgeStructure hs){
 		this.hs_originl = new HalfEdgeStructure(hs); //deep copy of the original mesh
 		this.hs_deformed = hs;
 		
 		this.keepFixed = new HashSet<>();
 		this.deform = new HashSet<>();
 		
 		
 		init_b_x(hs);
 		L_cotan = LMatrices.mixedCotanLaplacian(hs, false);
 		
 	}
 	
 	/**
 	 * Set which vertices should be kept fixed. 
 	 * @param verts_idx
 	 */
 	public void keep(Collection<Integer> verts_idx) {
 		this.keepFixed.clear();
 		this.keepFixed.addAll(verts_idx);
 	}
 	
 	/**
 	 * constrain these vertices to the new target position
 	 */
 	public void target(Collection<Integer> vert_idx){
 		this.deform.clear();
 		this.deform.addAll(vert_idx);
 	}
 	
 	
 	/**
 	 * update the linear system used to find optimal positions
 	 * for the currently constrained vertices.
 	 * Good place to do the cholesky decompositoin
 	 */
 	public void updateL() {
 		int nrVertices = hs_originl.getVertices().size();
 		M_constraints = new CSRMatrix(0, nrVertices);
 		for (int i = 0; i < nrVertices; i++) {
 			if (keepFixed.contains(i) || deform.contains(i)) {
 				ArrayList<col_val> row = M_constraints.addRow();
 				row.add(new col_val(i, w*w)); //add w square to constrained vertex diagonal
 				//Collections.sort(row);  //no need to sort, since only 1 entry
 			} else {
 				M_constraints.addRow();
 			}
 		}
 			
 		L_deform = new CSRMatrix(0, nrVertices);
 		CSRMatrix LTL = new CSRMatrix(0, nrVertices);
 		LTranspose = L_cotan.transposed();
 		LTranspose.multParallel(L_cotan, LTL);
 		L_deform.add(LTL, M_constraints);
 		solver = new Cholesky(L_deform);
 		//fill rotations with id
 		rotations = new ArrayList<Matrix3f>();
 		for (int i = 0; i < nrVertices; i++) {
 			Matrix3f id = new Matrix3f();
 			id.setIdentity();
 			rotations.add(id);
 		}
 	}
 	
 	/**
 	 * The RAPS modelling algorithm.
 	 * @param t
 	 * @param nRefinements
 	 */
 	public void deform(Matrix4f t, int nRefinements){
 		this.transformTarget(t);
 		for(int i = 0; i < nRefinements; i++) {
 			optimalPositions();
 			optimalRotations();
 			System.out.println("RAPS iteration " + i + " done!");
 		}
 	}
 	
 
 	/**
 	 * Method to transform the target positions and do nothing else.
 	 * @param t
 	 */
 	public void transformTarget(Matrix4f t) {
 		for(Vertex v : hs_deformed.getVertices()){
 			if(deform.contains(v.index)){
 				t.transform(v.getPos());
 			}
 		}
 	}
	
	
	/**
	 * ArrayList keyed with the vertex indices.
	 * @return
	 */
 	public ArrayList<Matrix3f> getRotations() {
 		return rotations;
 	}

	/**
	 * Getter for undeformed version of the mesh
	 * @return
	 */
 	public HalfEdgeStructure getOriginalCopy() {
 		return hs_originl;
 	}
	
	

	/**
	 * initialize b and x
	 * @param hs
	 */
 	private void init_b_x(HalfEdgeStructure hs) {
 		b = new ArrayList<Point3f>();
 		x = new ArrayList<Point3f>();
 		for(int i = 0; i < hs.getVertices().size(); i++){
 			b.add(new Point3f(0,0,0));
 			x.add(new Point3f(1,1,1));
 		}
	}
	
	
 	
 	/**
 	 * Compute optimal positions for the current rotations.
 	 */
 	public void optimalPositions(){
 		compute_b();
 		solver.solveTuple(L_deform, b, x);
 		hs_deformed.setVerticesTo(x);
 	}
 	
 
 	/**
 	 * compute the right hand side for the position optimization
 	 */
 	private void compute_b() {
 		reset_b();
 		//computing b according to eq on assignment sheet
		for (Vertex v: hs_deformed.getVertices()) {
 			Iterator<HalfEdge> iter = v.iteratorVE();
 			while (iter.hasNext()) {
 				HalfEdge he = iter.next();
 				Matrix3f R = new Matrix3f(rotations.get(he.start().index));
 				R.add(rotations.get(he.end().index));
 				Vector3f vec = he.asVector();
 				R.transform(vec);
 				vec.scale(he.cotanWeights()*0.5f);
 				b.get(v.index).add(vec);
 			}
 		}
 		
 		// Change to *normalized* form of b
 		ArrayList<Point3f> bNew = new ArrayList<Point3f>();
 		LTranspose.multTuple(b, bNew);
 		ArrayList<Point3f> verticesNew = new ArrayList<Point3f>();
 		M_constraints.multTuple(hs_deformed.getVerticesAsPointArray(), verticesNew);
 		for (int i = 0; i < b.size(); i++)
 			bNew.get(i).add(verticesNew.get(i));
 		b = bNew;
 	}
 
 
 
 	/**
 	 * helper method
 	 */
 	private void reset_b() {
 		for(Point3f p: b){
 			p.x = 0;
 			p.y = 0;
 			p.z = 0;
 		}
 	}
 
 
 	/**
 	 * Compute the optimal rotations for 1-neighborhoods, given
 	 * the original and deformed positions.
 	 */
 	public void optimalRotations() {
 		
 		//Note: slightly better results are achieved when the absolute of cotangent
 		//weights w_ij are used instead of plain cotangent weights.		
 			
 		for (int i = 0; i < rotations.size(); i++) {
 			//Matrix3f R = rotations.get(i);
 			Matrix3f S_i = new Matrix3f();
 			Vertex v_deformed = hs_deformed.getVertices().get(i);
 			Vertex v_orig = hs_originl.getVertices().get(i);
 			Iterator<HalfEdge> iter_deformed = v_deformed.iteratorVE();
 			Iterator<HalfEdge> iter_orig = v_orig.iteratorVE();
 			while (iter_deformed.hasNext() || iter_orig.hasNext()) { //throws exception if not same sized iters, bc bad!
 				HalfEdge he_orig = iter_orig.next();
 				Matrix3f ppT = compute_ppT(iter_deformed.next().asVector(), he_orig.asVector());
 				float w_ij = Math.abs(he_orig.cotanWeights()); // abs does prevent artifacts
 				ppT.mul(w_ij); 
 				S_i.add(ppT);
 			}
 			rotations.set(i, makeNewRotationFor(S_i));
 		}
 		
 	}
 	private Matrix3f makeNewRotationFor(Matrix3f S_i) {
 		Matrix3f U = new Matrix3f();
 		Matrix3f V = new Matrix3f();
 		l.svd(S_i, U, new Matrix3f(), V);
 		// make U_tilde with positive determinant by inverting last column possibly (if not already positive)
 		if (U.determinant() < 0) {
 			Vector3f lastCol = new Vector3f();
 			U.getColumn(2, lastCol);
 			lastCol.negate();
 			U.setColumn(2, lastCol);
 		}
 		U.transpose(); // U_tilde^T
 		V.mul(U); // new R_i
 		return V;
 	}
 
 	private Matrix3f compute_ppT(Vector3f p, Vector3f p2) {
 		assert(p.x*0==0);
 		assert(p.y*0==0);
 		assert(p.z*0==0);
 		Matrix3f pp2T = new Matrix3f();
 		pp2T.m00 = p.x*p2.x; pp2T.m01 = p.x*p2.y; pp2T.m02 = p.x*p2.z; 
 		pp2T.m10 = p.y*p2.x; pp2T.m11 = p.y*p2.y; pp2T.m12 = p.y*p2.z; 
 		pp2T.m20 = p.z*p2.x; pp2T.m21 = p.z*p2.y; pp2T.m22 = p.z*p2.z; 
 		return pp2T;
 	}
 
 
 	
 	
 
 
 
 
 }
