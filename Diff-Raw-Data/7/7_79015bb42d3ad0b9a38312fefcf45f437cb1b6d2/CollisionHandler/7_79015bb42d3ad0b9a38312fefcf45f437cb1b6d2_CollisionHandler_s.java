 import javax.vecmath.*;
 
 public class CollisionHandler {
 	private static final float COEFFICIENT_OF_RESTITUTION = .9f;
 	
 	public static void checkAndResolveCollision(PhysicsObject a, PhysicsObject b) {
 		CollisionInfo ci = getCollisionInfo(a, b);
 		if (ci == null)
 			return;
 
 		// Vector from the center of mass of object a to the collision point
 		Vector2f r_ap = new Vector2f();
 		r_ap.scaleAdd(-1, a.getGlobalCenterOfMass(), ci.position);
 		// Vector from the center of mass of object b to the collision point
 		Vector2f r_bp = new Vector2f();
 		r_bp.scaleAdd(-1, b.getGlobalCenterOfMass(), ci.position);
 		// Velocity of object a at the point of collision
 		Vector2f v_ap1 = new Vector2f();
 		v_ap1.x = a.velocity.x - a.angularVelocity * r_ap.y;
 		v_ap1.y = a.velocity.y + a.angularVelocity * r_ap.x;
 		// Velocity of object b at the point of collision
 		Vector2f v_bp1 = new Vector2f();
 		v_bp1.x = b.velocity.x - b.angularVelocity * r_bp.y;
 		v_bp1.y = b.velocity.y + b.angularVelocity * r_bp.x;
 		// The collision impulse
 		Vector2f v_ab1 = new Vector2f();
 		v_ab1.scaleAdd(-1, v_bp1, v_ap1);
 		float tmpA = r_ap.x * ci.normal.y - r_ap.y * ci.normal.x;
 		float tmpB = r_bp.x * ci.normal.y - r_bp.y * ci.normal.x;
 		float j = -(1 + COEFFICIENT_OF_RESTITUTION) * v_ab1.dot(ci.normal) / (1 / a.mass + 1 / b.mass + tmpA * tmpA / a.momentOfInertia + tmpB * tmpB / b.momentOfInertia);
 		// Update object a's velocity
 		a.velocity.scaleAdd(j / a.mass, ci.normal, a.velocity);
 		// Update object b's velocity
 		b.velocity.scaleAdd(-j / b.mass, ci.normal, b.velocity);
 		// Update object a's angular velocity
 		a.angularVelocity += j * (r_ap.x * ci.normal.y - r_ap.y * ci.normal.x) / a.momentOfInertia;
 		// Update object b's angular velocity
 		b.angularVelocity -= j * (r_bp.x * ci.normal.y - r_bp.y * ci.normal.x) / b.momentOfInertia;
 		// Remove object overlap
 		a.position.scaleAdd(-ci.depth / (a.mass * (1 / a.mass + 1 / b.mass)), ci.normal, a.position);
 		b.position.scaleAdd(ci.depth / (b.mass * (1 / a.mass + 1 / b.mass)), ci.normal, b.position);
 		
 		a.clearCaches();
 		b.clearCaches();
 	}
 	
 	private static CollisionInfo getCollisionInfo(PhysicsObject a, PhysicsObject b) {
 		if (a == b)
 			return null;
 		
 		CollisionInfo ci = null;
 		if (a instanceof HalfSpace) {
 			if (b instanceof Circle)
 				ci = getCollision((HalfSpace)a, (Circle)b);
 			else if (b instanceof Triangle)
 				ci = getCollision((HalfSpace)a, (Triangle)b);
 			else if (b instanceof ConvexPolygon)
 		        ci = getCollision((HalfSpace) a, (ConvexPolygon) b);
 		} else if (a instanceof Circle) {
 			if (b instanceof Circle)
 				ci = getCollision((Circle)a, (Circle)b);
 			else if (b instanceof Triangle)
 			    ci = getCollision((Circle) a, (Triangle) b);
 			else if (b instanceof ConvexPolygon)
 			    ci = getCollision((Circle) a, (ConvexPolygon) b);
 			else if (b instanceof HalfSpace)
 			    ci = getCollision((HalfSpace) b, (Circle) a);
 		} else if (a instanceof Triangle) {
 			if (b instanceof Triangle)
 				ci = getCollision((Triangle)a, (Triangle)b);
 			else if (b instanceof Circle)
 			    ci = getCollision((Circle) b, (Triangle) a);
 			else if (b instanceof HalfSpace)
 			    ci = getCollision((HalfSpace) b, (Triangle) a);
 		} else if (a instanceof ConvexPolygon) {
 		    if (b instanceof ConvexPolygon)
 		        ci = getCollision((ConvexPolygon) a, (ConvexPolygon) b);
 		    else if (b instanceof Circle)
 		        ci = getCollision((Circle) b, (ConvexPolygon) a);
 			else if (b instanceof HalfSpace)
 			    ci = getCollision((HalfSpace) b, (ConvexPolygon) a);
 		}
 		
 		return ci;
 	}
 
 	private static CollisionInfo getCollision(HalfSpace a, Circle b) {
 		float distance = a.normal.dot(b.position) - a.intercept - b.radius;
 		if (distance < 0) {
 			CollisionInfo ci = new CollisionInfo();
 			ci.normal = a.normal;
 			ci.depth = -distance;
 			ci.position = new Vector2f();
 			ci.position.scaleAdd(-(b.radius - ci.depth), ci.normal, b.position);
 			return ci;
 		}
 		return null;
 	}
 	
 	private static CollisionInfo getCollision(HalfSpace a, Triangle b) {
 		Vector2f[] vertices = b.getVertices();
 		float[] distances = new float[vertices.length];
 		
 		for (int i = 0; i < vertices.length; i++)
 			distances[i] = a.normal.dot(vertices[i]) - a.intercept;
 		
 		int minIndex = 0;
 		for (int i = 1; i < distances.length; i++)
 			if (distances[i] < distances[minIndex])
 				minIndex = i;
 		if (distances[minIndex] >= 0)
 			return null;
 		
 		CollisionInfo ci = new CollisionInfo();
 		ci.depth = -distances[minIndex];
 		ci.normal = a.normal;
 		ci.position = new Vector2f(vertices[minIndex]);
 		ci.position.scaleAdd(ci.depth, ci.normal, ci.position);
 		return ci;
 	}
 	
 	private static CollisionInfo getCollision(HalfSpace a, ConvexPolygon b) {
         // TODO This doesn't intelligently handle edge-edge collisions.
         // In that case, should probably choose center point on colliding
         // portion of edges. Probably should implement this in terms of
 	    // Convex - Convex collision method.
         Vector2f[] vertices = b.getVertices();
         
         Vector2f minVertex = vertices[0];
         float minDistance = a.normal.dot(minVertex) - a.intercept;
         for (int i = 0; i < vertices.length; i++) {
             final float distance = a.normal.dot(vertices[i]) - a.intercept;
             if (distance < minDistance) {
                 minDistance = distance;
                 minVertex = vertices[i];
             }
         }
         
         if (minDistance >= 0)
             return null;
         
         CollisionInfo ci = new CollisionInfo();
         ci.depth = -minDistance;
         ci.normal = a.normal;
         ci.position = new Vector2f(minVertex);
         ci.position.scaleAdd(ci.depth, ci.normal, ci.position);
         return ci;
     }
 
     private static CollisionInfo getCollision(Circle a, Circle b) {
 		Vector2f n = new Vector2f();
 		n.scaleAdd(-1, a.position, b.position);
 		float distance = n.length() - a.radius - b.radius;
 		if (distance < 0) {
 			CollisionInfo ci = new CollisionInfo();
 			n.normalize();
 			ci.normal = n;
 			ci.depth = -distance;
 			// Approximates collision point as center of overlapping region.
 			ci.position = new Vector2f();
 			ci.position.scaleAdd(a.radius - ci.depth / 2, ci.normal, a.position);
 			return ci;
 		}
 		return null;
 	}
     
     private static CollisionInfo getCollision(Circle c, Triangle t)
     {
         Vector2f[] verticesA = t.getVertices();
         Vector2f[] normalsA = t.getNormals();
         Vector2f[] normalsB = new Vector2f[normalsA.length];
         for (int i = 0; i < normalsB.length; i++)
         {
             Vector2f n = new Vector2f();
             n.scaleAdd(-1, verticesA[i], c.position);
             n.normalize();
             normalsB[i] = n;
         }
         float[] distanceFromA = new float[verticesA.length];
         float[][] distanceFromB = new float[verticesA.length][normalsB.length];
         int indexMinDistanceFromA = 0;
         int[] indexMinDistanceFromB = new int[verticesA.length];
         
         for (int i = 0; i < verticesA.length; i++) {
             Vector2f tmp = new Vector2f();
             tmp.scaleAdd(-1, verticesA[i], c.position);
             distanceFromA[i] = tmp.dot(normalsA[i]) - c.radius;
             if (distanceFromA[i] < distanceFromA[indexMinDistanceFromA])
                 indexMinDistanceFromA = i;
             if (distanceFromA[i] >= 0)
                 return null;
         }
        
         for (int i = 0; i < verticesA.length; i++) {
             for (int j = 0; j < verticesA.length; j++) {
                 Vector2f tmp = new Vector2f(verticesA[j]);
                 tmp.scaleAdd(-1, c.position, verticesA[j]);
                distanceFromB[i][j] = tmp.dot(normalsB[i]);
                 if (distanceFromB[i][j] < distanceFromB[i][indexMinDistanceFromB[i]])
                     indexMinDistanceFromB[i] = j;
             }
             if (distanceFromB[i][indexMinDistanceFromB[i]] >= 0)
                 return null;
         }
         
         int indexMaxDistanceFromA = 0;
         for (int i = 1; i < verticesA.length; i++)
             if (distanceFromA[i] > distanceFromA[indexMaxDistanceFromA])
                 indexMaxDistanceFromA = i;
         int indexMaxDistanceFromB = 0;
         for (int i = 1; i < verticesA.length; i++)
             if (distanceFromB[i][indexMinDistanceFromB[i]] > distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]])
                 indexMaxDistanceFromB = i;
         
         CollisionInfo ci = new CollisionInfo();
         if (distanceFromA[indexMaxDistanceFromA] > distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]]) {
             ci.depth = -distanceFromA[indexMaxDistanceFromA];
             ci.normal = new Vector2f(normalsA[indexMaxDistanceFromA]);
             ci.position = new Vector2f(c.position);
             ci.position.scaleAdd(-ci.depth, ci.normal, ci.position);
         } else {
             ci.depth = -distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]];
             ci.normal = new Vector2f(normalsB[indexMaxDistanceFromB]);
             ci.normal.scale(-1);
             ci.position = new Vector2f(verticesA[indexMinDistanceFromB[indexMaxDistanceFromB]]);
             ci.position.scaleAdd(ci.depth, ci.normal, ci.position);
         }
         return ci;
     }
 
     private static CollisionInfo getCollision(Circle a, ConvexPolygon b) {
         // TODO Auto-generated method stub
         return null;
     }
 
     private static CollisionInfo getCollision(Triangle a, Triangle b) {
 		Vector2f[] verticesA = a.getVertices();
 		Vector2f[] normalsA = a.getNormals();
 		Vector2f[] verticesB = b.getVertices();
 		Vector2f[] normalsB = b.getNormals();
 		
 		// distanceFromX[edge normal index][vertex index] =
 		// distance of vertex from edge
 		float[][] distanceFromA = new float[verticesA.length][verticesB.length];
 		float[][] distanceFromB = new float[verticesB.length][verticesA.length];
 		
 		// indexMinDistanceFromX[edge normal index] =
 		// index of vertex with minimum distance (possibly negative)
 		int[] indexMinDistanceFromA = new int[verticesA.length];
 		int[] indexMinDistanceFromB = new int[verticesB.length];
 		
 		// Get signed distance of every vertex in B from every edge in A.
 		for (int i = 0; i < verticesA.length; i++) {
 			for (int j = 0; j < verticesB.length; j++) {
 				Vector2f tmp = new Vector2f();
 				tmp.scaleAdd(-1, verticesA[i], verticesB[j]);
 				distanceFromA[i][j] = tmp.dot(normalsA[i]);
 				if (distanceFromA[i][j] < distanceFromA[i][indexMinDistanceFromA[i]])
 					indexMinDistanceFromA[i] = j;
 			}
 			
 			// If signed distance for every vertex in B is positive for a given
 			// edge of A, then B is not intersecting with A (true because shapes
 			// are convex).
 			if (distanceFromA[i][indexMinDistanceFromA[i]] >= 0)
 				return null;
 		}
 	
 		// Do the same for edges of B and vertices of A.
 		for (int i = 0; i < verticesB.length; i++) {
 			for (int j = 0; j < verticesA.length; j++) {
 				Vector2f tmp = new Vector2f(verticesA[j]);
 				tmp.scaleAdd(-1, verticesB[i], verticesA[j]);
 				distanceFromB[i][j] = tmp.dot(normalsB[i]);
 				if (distanceFromB[i][j] < distanceFromB[i][indexMinDistanceFromB[i]])
 					indexMinDistanceFromB[i] = j;
 			}
 			
 			if (distanceFromB[i][indexMinDistanceFromB[i]] >= 0)
 				return null;
 		}
 		
 		// There must be an intersection because it was never the case that all
 		// the vertices of one triangle were on the correct side of some edge of
 		// the other triangle.
 		
 	    // For each edge of A, we have the vertex of B that is farthest away and
 		// on wrong side. Of these vertices, find the one that is closest to its
 		// respective edge. This will be a candidate for the vertex where
 		// the collision occurred.
 		int indexMaxDistanceFromA = 0;
 		for (int i = 1; i < verticesA.length; i++)
 			if (distanceFromA[i][indexMinDistanceFromA[i]] > distanceFromA[indexMaxDistanceFromA][indexMinDistanceFromA[indexMaxDistanceFromA]])
 				indexMaxDistanceFromA = i;
 		
 		// Do the same for edges of B and vertices of A.
 		int indexMaxDistanceFromB = 0;
 		for (int i = 1; i < verticesB.length; i++)
 			if (distanceFromB[i][indexMinDistanceFromB[i]] > distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]])
 				indexMaxDistanceFromB = i;
 		
 		// Use the vertex, either from A or B, that is closer to its respective
 		// edge in order to determine collision information.
 		CollisionInfo ci = new CollisionInfo();
 		if (distanceFromA[indexMaxDistanceFromA][indexMinDistanceFromA[indexMaxDistanceFromA]] > distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]]) {
 		    // Some vertex of B is penetrating A the least. Use its penetration depth (positive).
 			ci.depth = -distanceFromA[indexMaxDistanceFromA][indexMinDistanceFromA[indexMaxDistanceFromA]];
 			// Use the normal of the edge of A which B is penetrating the least.
 			ci.normal = new Vector2f(normalsA[indexMaxDistanceFromA]);
 			// Approximate the collision point as the location of B's vertex
 			// projected onto the surface of the respective edge of A.
 			ci.position = new Vector2f(verticesB[indexMinDistanceFromA[indexMaxDistanceFromA]]);
 			ci.position.scaleAdd(-ci.depth, ci.normal, ci.position);
 		} else {
 			ci.depth = -distanceFromB[indexMaxDistanceFromB][indexMinDistanceFromB[indexMaxDistanceFromB]];
 			ci.normal = new Vector2f(normalsB[indexMaxDistanceFromB]);
 			ci.normal.scale(-1);
 			ci.position = new Vector2f(verticesA[indexMinDistanceFromB[indexMaxDistanceFromB]]);
 			ci.position.scaleAdd(ci.depth, ci.normal, ci.position);
 		}
 		return ci;
 	}
 
     private static CollisionInfo getCollision(ConvexPolygon cp1, ConvexPolygon cp2) {
         // For each surface normal, project vertices of both polygons onto it.
         // Since this projection is on a given axis, just need to store min and
         // max for each poly. If they share some space, then they intersect.
         
         // Overlap normal will always be direction that B will need to be moved
         // and opposite direction A needs to be moved.
         /*
         float minOverlap = Float.POSITIVE_INFINITY;
         Vector2f minOverlapNormal = null;
         final Vector2f[] axes = getSATAxes(cp1, cp2);
         
         for (final Vector2f axis : axes) {
             Projection p1 = new Projection(cp1, axis);
             Projection p2 = new Projection(cp2, axis);
         
             if (!p1.isOverlap(p2)) {
     	        // The polygons are separated in this axis.
                 return null;
             } else {
                 // Find amount of overlap in this axis.
                 if (overlap < minOverlap) {
                     minOverlap = overlap;
                     minOverlapNormal = new Vector2f(axis);
                     
                     //if (negateOverlapNormal) {
                     //    minOverlapNormal.negate();
                     //}
                 }
             }
         }
         */
         return null;
     }
 
     private static Vector2f[] getSATAxes(ConvexPolygon cp1, ConvexPolygon cp2) { 
         final Vector2f[] a = cp1.getNormals();
         final Vector2f[] b = cp2.getNormals();
         final Vector2f[] c = new Vector2f[a.length + b.length];
         System.arraycopy(a, 0, c, 0, a.length);
         System.arraycopy(b, 0, c, a.length, b.length);
         return c;
     }
 
     private static class Projection {
         public float min;
         public float max;
         
         public Projection(final ConvexPolygon p, final Vector2f axis) {
             min = Float.POSITIVE_INFINITY;
             max = Float.NEGATIVE_INFINITY;
             
             for (final Vector2f v : p.getVertices()) {
                 final float vProj = v.dot(axis);
                 min = Math.min(min, vProj);
                 max = Math.max(max, vProj);
             }
         }
         
         public boolean isOverlap(final Projection other) {
             return min <= other.max && max >= other.min;
         }
         
         public boolean contains(final Projection other) {
             return other.min >= min && other.max <= max;
         }
     }
 }
