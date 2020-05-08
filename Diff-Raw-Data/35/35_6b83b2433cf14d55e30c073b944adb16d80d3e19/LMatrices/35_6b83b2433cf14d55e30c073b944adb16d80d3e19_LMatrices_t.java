 package assignment4;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 
 import javax.vecmath.Tuple3f;
 import javax.vecmath.Vector3f;
 
 import com.jogamp.opengl.math.FloatUtil;
 
 import meshes.HalfEdge;
 import meshes.HalfEdgeStructure;
 import meshes.Vertex;
 import myutils.MyMath;
 import sparse.CSRMatrix;
 import sparse.CSRMatrix.col_val;
 
 /**
  * Methods to create different flavours of the cotangent and uniform laplacian.
  * @author Alf
  *
  */
 public class LMatrices {
 	
 	/**
 	 * The uniform Laplacian
 	 * @param hs
 	 * @return
 	 */
 	public static CSRMatrix uniformLaplacian(HalfEdgeStructure hs){
 		CSRMatrix m = new CSRMatrix(0, hs.getVertices().size());
 		for(Vertex v: hs.getVertices()) {
 			ArrayList<col_val> row = m.addRow();
 			if (v.isOnBoundary()) 
 				continue; //leave row empty
 			int valence = v.getValence();
 			Iterator<Vertex> iter = v.iteratorVV();
 			while(iter.hasNext()) 
 				row.add(new col_val(iter.next().index, 1f/valence));
 			
 			row.add(new col_val(v.index, -1));
 			Collections.sort(row);
 		}
 		return m;
 	}
 	
 	/**
 	 * The cotangent Laplacian
 	 * @param hs
 	 * @return
 	 */
 	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs, boolean normalized){
 		CSRMatrix m = new CSRMatrix(0, hs.getVertices().size());
 		for(Vertex v: hs.getVertices()) {
 			ArrayList<col_val> row = m.addRow();
 			if (v.isOnBoundary()) 
 				continue; //leave row empty
 			float aMixed;
 			if (normalized)
 				aMixed = v.getAMixed();
 			else
				aMixed = 1/2f;
 			//copy paste from vertex.getCurvature() (I'm so sorry)
 			Iterator<HalfEdge> iter = v.iteratorVE();
 			float sum = 0;
 			while(iter.hasNext()) {
 				HalfEdge current = iter.next();
 				// demeter is crying qq
 				float alpha = current.getNext().getIncidentAngle();
 				float beta = current.getOpposite().getNext().getIncidentAngle();
 				float cot_alpha = MyMath.cot(alpha, true);
 				float cot_beta = MyMath.cot(beta, true);
 				float entry = (cot_alpha + cot_beta)/(2*aMixed);
 				sum += entry;
 				row.add(new col_val(current.start().index, entry));
 			}		
 			row.add(new col_val(v.index, -sum));
 			
 			Collections.sort(row);
 		}
 		return m;
 	}
 	
 	public static CSRMatrix mixedCotanLaplacian(HalfEdgeStructure hs){
 		return mixedCotanLaplacian(hs, true);
 	}
 	
 	/**
 	 * A symmetric cotangent Laplacian, cf Assignment 4, exercise 4.
 	 * TODO: combine this with other cotan
 	 * @param hs
 	 * @return
 	 */
 	public static CSRMatrix symmetricCotanLaplacian(HalfEdgeStructure hs){
 		CSRMatrix m = new CSRMatrix(0, hs.getVertices().size());
 		for(Vertex v: hs.getVertices()) {
 			ArrayList<col_val> row = m.addRow();
 			if (v.isOnBoundary()) 
 				continue; //leave row empty
 			float aMixed = v.getAMixed();
 			//copy paste from vertex.getCurvature() (I'm so sorry)
 			Iterator<HalfEdge> iter = v.iteratorVE();
 			float sum = 0;
 			while(iter.hasNext()) {
 				HalfEdge current = iter.next();
 				// demeter is crying qq
 				float alpha = current.getNext().getIncidentAngle();
 				float beta = current.getOpposite().getNext().getIncidentAngle();
 				float cot_alpha = MyMath.cot(alpha, true);
 				float cot_beta = MyMath.cot(beta, true);
 				float scale = FloatUtil.sqrt(aMixed*current.start().getAMixed());
 				float entry = (cot_alpha + cot_beta)/(2*scale);
 				sum += entry;
 				row.add(new col_val(current.start().index, entry));
 			}		
 			row.add(new col_val(v.index, -sum));
 			
 			Collections.sort(row);
 		}
 		return m;
 	}
 
 	/**
 	 * helper method to multiply x,y and z coordinates of the halfedge structure at once
 	 * @param m
 	 * @param s
 	 * @param res
 	 */
 	public static <T extends Tuple3f> void mult(CSRMatrix m, HalfEdgeStructure s, ArrayList<T> res){
 		ArrayList<Float> x = new ArrayList<>(), b = new ArrayList<>(s.getVertices().size());
 		x.ensureCapacity(s.getVertices().size());
 		
 		res.clear();
 		res.ensureCapacity(s.getVertices().size());
 		for(Vertex v : s.getVertices()){
 			x.add(0.f);
 			res.add((T) new Vector3f());
 		}
 		
 		for(int i = 0; i < 3; i++){
 			
 			//setup x
 			for(Vertex v : s.getVertices()){
 				switch (i) {
 				case 0:
 					x.set(v.index, v.getPos().x);	
 					break;
 				case 1:
 					x.set(v.index, v.getPos().y);	
 					break;
 				case 2:
 					x.set(v.index, v.getPos().z);	
 					break;
 				}
 				
 			}
 			
 			m.mult(x, b);
 			
 			for(Vertex v : s.getVertices()){
 				switch (i) {
 				case 0:
 					res.get(v.index).x = b.get(v.index);	
 					break;
 				case 1:
 					res.get(v.index).y = b.get(v.index);	
 					break;
 				case 2:
 					res.get(v.index).z = b.get(v.index);	
 					break;
 				}
 				
 			}
 		}
 	}
 }
