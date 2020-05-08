 package com.evanreidland.e.graphics;
 
 import com.evanreidland.e.Vector3;
 import com.evanreidland.e.engine;
 import com.evanreidland.e.graphics.Model.ModelType;
 import com.evanreidland.e.phys.Rect3;
 
 public class generate {
 	private static ModelType type = ModelType.RenderList;
 	public static ModelType getModelType() {
 		return type;
 	}
 	public static void setModelType(ModelType type) {
 		generate.type = type;
 	}
 	
 	public static TriList CubeList(Vector3 offset, Vector3 size, Vector3 angle) {
 		TriList triList = new TriList();
 		
 		Rect3 r = Rect3.fromPoints(size.multipliedBy(-0.5), size.multipliedBy(0.5));
 		
 		Quad q = new Quad();
 		//Top
 		q.vert[0].pos.setAs(r.topLeft());
 		q.vert[1].pos.setAs(r.topRight());
 		q.vert[2].pos.setAs(r.topBRight());
 		q.vert[3].pos.setAs(r.topBLeft());
 		triList.addQuad(q);
 		
 		//Bottom
 		q.vert[0].pos.setAs(r.bottomURight());
 		q.vert[1].pos.setAs(r.bottomULeft());
 		q.vert[2].pos.setAs(r.bottomLeft());
 		q.vert[3].pos.setAs(r.bottomRight());
 		triList.addQuad(q);
 		
 		//Back
 		q.vert[0].pos.setAs(r.topBLeft());
 		q.vert[1].pos.setAs(r.topBRight());
 		q.vert[2].pos.setAs(r.bottomRight());
 		q.vert[3].pos.setAs(r.bottomLeft());
 		triList.addQuad(q);
 		
 		//Front
 		q.vert[0].pos.setAs(r.topRight());
 		q.vert[1].pos.setAs(r.topLeft());
 		q.vert[2].pos.setAs(r.bottomULeft());
 		q.vert[3].pos.setAs(r.bottomURight());
 		triList.addQuad(q);
 		
 		//Left
 		q.vert[0].pos.setAs(r.topLeft());
 		q.vert[1].pos.setAs(r.topBLeft());
 		q.vert[2].pos.setAs(r.bottomLeft());
 		q.vert[3].pos.setAs(r.bottomULeft());
 		triList.addQuad(q);
 		
 		//Right
 		q.vert[0].pos.setAs(r.topBRight());
 		q.vert[1].pos.setAs(r.topRight());
 		q.vert[2].pos.setAs(r.bottomURight());
 		q.vert[3].pos.setAs(r.bottomRight());
 		triList.addQuad(q);
 		
 		triList.Shift(offset);
 		
 		if ( angle.x != 0 || angle.y != 0 || angle.z != 0 ) {
 			triList.Rotate(offset, angle);
 		}
 		
 		return triList;
 	}
 	
 	public static TriList SphereList(Vector3 offset, Vector3 size, Vector3 angle, int arcs, int pointsPerArc) {
 		TriList list = new TriList();
 		for ( int i = 0; i < arcs; i++ ) {
 			double angle1 = (i/(double)arcs)*engine.Pi2,
 				   angle2 = ((i + 1)/(double)arcs)*engine.Pi2,
 				   r = 1/(double)pointsPerArc;
 			
 			Tri tri = new Tri();
 			tri.vert[1].tx = angle2/engine.Pi; tri.vert[1].ty = r;
 			tri.vert[2].tx = angle1/engine.Pi; tri.vert[2].ty = r;
			tri.vert[0].tx = (tri.vert[1].tx + tri.vert[2].tx)*0.5; tri.vert[0].ty = 0;
 			
 			tri.vert[0].pos = new Vector3(0, 0, size.z);
 			tri.vert[1].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle2, r*engine.Pi);
 			tri.vert[2].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle1, r*engine.Pi);
 			
 			list.add(tri);
 			
 			r = (pointsPerArc - 1)/(double)pointsPerArc;
 			
 			tri = new Tri();
 			tri.vert[1].tx = angle1/engine.Pi; tri.vert[2].ty = r;
 			tri.vert[2].tx = angle2/engine.Pi; tri.vert[1].ty = r;
			tri.vert[0].tx = (tri.vert[1].tx + tri.vert[2].tx)*0.5; tri.vert[0].ty = 1;
 			
 			tri.vert[0].pos = new Vector3(0, 0, -size.z);
 			tri.vert[1].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle1, r*engine.Pi);
 			tri.vert[2].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle2, r*engine.Pi);
 			list.add(tri);
 			for ( int j = 1; j < pointsPerArc - 1; j++ ) {
 				Quad q = new Quad();
 				r = j/(double)pointsPerArc;
 				double r2 = (j + 1)/(double)pointsPerArc;
 				
 				q.vert[0].tx = angle1/engine.Pi; q.vert[0].ty = r; 
 				q.vert[0].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle1, r*engine.Pi);
 				
 				q.vert[1].tx = angle2/engine.Pi; q.vert[1].ty = r;
 				q.vert[1].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle2, r*engine.Pi);
 				
 				q.vert[2].tx = angle2/engine.Pi; q.vert[2].ty = r2;
 				q.vert[2].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle2, r2*engine.Pi);
 				
 				q.vert[3].tx = angle1/engine.Pi; q.vert[3].ty = r2;
 				q.vert[3].pos = Vector3.pointOnSphere(Vector3.Zero(), size, angle1, r2*engine.Pi);
 				list.addQuad(q);
 			}
 		}
 		list.Rotate(Vector3.Zero(), angle);
 		list.Shift(offset);
 		
 		return list;
 	}
 	
 	public static Model Cube(Vector3 offset, Vector3 size, Vector3 angle) {
 		Model m = new Model();
 		m.setData(CubeList(offset, size, angle), type);
 		return m;
 	}
 	
 	public static Model Sphere(Vector3 offset, Vector3 size, Vector3 angle, int arcs, int pointsPerArc) {
 		Model m = new Model();
 		m.setData(SphereList(offset, size, angle, arcs, pointsPerArc), type);
 		return m;
 	}
 	
 	public static Model fromList(TriList list) {
 		Model m = new Model();
 		m.setData(list, type);
 		return m;
 	}
 }
