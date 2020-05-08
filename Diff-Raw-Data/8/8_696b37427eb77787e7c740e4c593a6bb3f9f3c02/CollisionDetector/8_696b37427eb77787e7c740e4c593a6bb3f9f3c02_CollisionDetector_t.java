 package common;
 
 import java.util.ArrayList;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.Geometry;
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.Group;
 import javax.media.j3d.IndexedTriangleArray;
 import javax.media.j3d.IndexedTriangleFanArray;
 import javax.media.j3d.IndexedTriangleStripArray;
 import javax.media.j3d.Node;
 import javax.media.j3d.PolygonAttributes;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TriangleArray;
 import javax.media.j3d.TriangleFanArray;
 import javax.media.j3d.TriangleStripArray;
 import javax.vecmath.Matrix3f;
 import javax.vecmath.Point3f;
import javax.vecmath.SingularMatrixException;
 import javax.vecmath.Tuple3f;
 import javax.vecmath.Vector3f;
 
 import tesseract.objects.HalfSpace;
 import tesseract.objects.Particle;
 import tesseract.objects.Polygon;
 import tesseract.objects.Sphere;
 
 import com.sun.j3d.utils.geometry.Primitive;
 
 @SuppressWarnings("restriction")
 public class CollisionDetector {
 	public static final float EPSILON = 0.0001f;
 	private static final ArrayList<CollisionInfo> EMPTY_COLLISION_LIST = new ArrayList<CollisionInfo>();
 
 	public static class Triangle {
 		private Vector3f a;
 		private Vector3f b;
 		private Vector3f c;
 		private Vector3f normal;
 		private float intercept;
 
 		private static class Line {
 			public Vector3f point;
 			public Vector3f direction;
 			
 			public Line() {
 				point = new Vector3f();
 				direction = new Vector3f();
 			}
 		}
 		
 		private static class TPair {
 			public float t0;
 			public float t1;
 		}
 
 		public Triangle(Tuple3f a, Tuple3f b, Tuple3f c) {
 			this.a = new Vector3f(a);
 			this.b = new Vector3f(b);
 			this.c = new Vector3f(c);
 			Vector3f tmp = new Vector3f();
 			tmp.scaleAdd(-1, a, c);
 			Vector3f tmp2 = new Vector3f();
 			tmp2.scaleAdd(-1, b, c);
 			normal = new Vector3f();
 			normal.cross(tmp, tmp2);
 			if (normal.lengthSquared() == 0)
 				throw new IllegalArgumentException("Degenerate triangle");
 			normal.normalize();
 			intercept = normal.dot(this.a);
 		}
 		
 		// Inspired by Tomas Moller's "A Fast Triangle-Triangle Intersection Test"	
 		public CollisionInfo getIntersection(Triangle other) {
 			float d_0_0 = other.normal.dot(a) - other.intercept;
 			float d_0_1 = other.normal.dot(b) - other.intercept;
 			float d_0_2 = other.normal.dot(c) - other.intercept;
 			if (Math.abs(d_0_0) < EPSILON)
 				d_0_0 = 0;
 			if (Math.abs(d_0_1) < EPSILON)
 				d_0_1 = 0;
 			if (Math.abs(d_0_2) < EPSILON)
 				d_0_2 = 0;
 			if (d_0_0 != 0 && d_0_1 != 0 && d_0_2 != 0 && Math.signum(d_0_0) == Math.signum(d_0_1) && Math.signum(d_0_1) == Math.signum(d_0_2))
 				return null;
 			
 			float d_1_0 = normal.dot(other.a) - intercept;
 			float d_1_1 = normal.dot(other.b) - intercept;
 			float d_1_2 = normal.dot(other.c) - intercept;
 			if (Math.abs(d_1_0) < EPSILON)
 				d_1_0 = 0;
 			if (Math.abs(d_1_1) < EPSILON)
 				d_1_1 = 0;
 			if (Math.abs(d_1_2) < EPSILON)
 				d_1_2 = 0;
 			if (d_1_0 != 0 && d_1_1 != 0 && d_1_2 != 0 && Math.signum(d_1_0) == Math.signum(d_1_1) && Math.signum(d_1_1) == Math.signum(d_1_2))
 				return null;
 
 			// Coplanar, assume no collision
 			if (d_0_0 == 0 && d_0_1 == 0 && d_0_2 == 0)
 				return null;
 			
 			Line line = calculateLineOfIntersection(other);
 			TPair r0 = calculateRegionOfIntersection(line, d_0_0, d_0_1, d_0_2);
 			TPair r1 = other.calculateRegionOfIntersection(line, d_1_0, d_1_1, d_1_2);
 			
 			if (r0.t1 < r1.t0 || r0.t0 > r1.t1)
 				return null;
 			
 			Vector3f contactPoint = new Vector3f();
 			if (r0.t0 >= r1.t0 && r0.t1 <= r1.t1)
 				contactPoint.scaleAdd((r0.t0 + r0.t1) / 2, line.direction, line.point);
 			else if (r0.t0 <= r1.t0 && r0.t1 >= r1.t1)
 				contactPoint.scaleAdd((r1.t0 + r1.t1) / 2, line.direction, line.point);
 			else if (r0.t0 < r1.t0)
 				contactPoint.scaleAdd((r0.t1 + r1.t0) / 2, line.direction, line.point);
 			else
 				contactPoint.scaleAdd((r0.t0 + r1.t1) / 2, line.direction, line.point);
 			
 			assert(Math.abs(normal.dot(contactPoint) - intercept) < 0.01);
 			assert(Math.abs(other.normal.dot(contactPoint) - other.intercept) < 0.01);
 			
 			float penetration = Float.NEGATIVE_INFINITY;
 			boolean useThisNormal = false;
 			if (d_0_0 <= 0 && d_0_0 >= penetration)
 				penetration = d_0_0;
 			if (d_0_1 <= 0 && d_0_1 >= penetration)
 				penetration = d_0_1;
 			if (d_0_2 <= 0 && d_0_2 >= penetration)
 				penetration = d_0_2;
 			if (d_1_0 <= 0 && d_1_0 >= penetration) {
 				penetration = d_1_0;
 				useThisNormal = true;
 			}
 			if (d_1_1 <= 0 && d_1_1 >= penetration) {
 				penetration = d_1_1;
 				useThisNormal = true;
 			}
 			if (d_1_2 <= 0 && d_1_2 >= penetration) {
 				penetration = d_1_2;
 				useThisNormal = true;
 			}
 			Vector3f contactNormal;
 			if (useThisNormal)
 				contactNormal = new Vector3f(normal);
 			else {
 				contactNormal = new Vector3f(other.normal);
 				contactNormal.negate();
 			}
 			
 			return new CollisionInfo(contactPoint, contactNormal, -penetration);
 		}
 
 		private Line calculateLineOfIntersection(Triangle other) {
 			Line line = new Line();
 			line.direction.cross(normal, other.normal);
 			if (Math.abs(line.direction.x) < EPSILON)
 				line.direction.x = 0;
 			if (Math.abs(line.direction.y) < EPSILON)
 				line.direction.y = 0;
 			if (Math.abs(line.direction.z) < EPSILON)
 				line.direction.z = 0;
 			line.direction.normalize();
 			
 			if (line.direction.x != 0) { // x <- 0
 				if (normal.y != 0) {
 					line.point.z = (other.normal.y / normal.y * intercept - other.intercept) / (other.normal.y / normal.y * normal.z - other.normal.z);
 					line.point.y = (intercept - normal.z * line.point.z) / normal.y;
 				} else { // normal.z != 0
 					line.point.y = (other.normal.z / normal.z * intercept - other.intercept) / (other.normal.z / normal.z * normal.y - other.normal.y);
 					line.point.z = (intercept - normal.y * line.point.y) / normal.z;
 				}
 			} else if (line.direction.y != 0) { // y <- 0
 				if (normal.x != 0) {
 					line.point.z = (other.normal.x / normal.x * intercept - other.intercept) / (other.normal.x / normal.x * normal.z - other.normal.z);
 					line.point.x = (intercept - normal.z * line.point.z) / normal.x;
 				} else { // normal.z != 0
 					line.point.x = (other.normal.z / normal.z * intercept - other.intercept) / (other.normal.z / normal.z * normal.x - other.normal.x);
 					line.point.z = (intercept - normal.x * line.point.x) / normal.z;
 				}
 			} else { // z <- 0
 				if (normal.x != 0) {
 					line.point.y = (other.normal.x / normal.x * intercept - other.intercept) / (other.normal.x / normal.x * normal.y - other.normal.y);
 					line.point.x = (intercept - normal.y * line.point.y) / normal.x;
 				} else { // normal.y != 0
 					line.point.x = (other.normal.y / normal.y * intercept - other.intercept) / (other.normal.y / normal.y * normal.x - other.normal.x);
 					line.point.y = (intercept - normal.x * line.point.x) / normal.y;
 				}
 			}
 
 			assert(Math.abs(normal.dot(line.point) - intercept) < 0.01);
 			assert(Math.abs(other.normal.dot(line.point) - other.intercept) < 0.01);
 
 			return line;
 		}
 		
 		private TPair calculateRegionOfIntersection(Line line, float d0, float d1, float d2) {
 			Vector3f v0, v1, v2;
 			if (Math.signum(d0) != 0 && Math.signum(d0) != Math.signum(d1) && Math.signum(d0) != Math.signum(d2)) {
 				v0 = b; v1 = a; v2 = c;
 				float tmp = d0; d0 = d1; d1 = tmp;
 			} else if (Math.signum(d1) != 0 && Math.signum(d0) != Math.signum(d1) && Math.signum(d1) != Math.signum(d2)) {
 				v0 = a; v1 = b; v2 = c;
 			} else if (Math.signum(d2) != 0 && Math.signum(d0) != Math.signum(d2) && Math.signum(d1) != Math.signum(d2)) {
 				v0 = a; v1 = c; v2 = b;
 				float tmp = d1; d1 = d2; d2 = tmp; 
 			} else if (Math.signum(d0) == 0) {
 				v0 = b; v1 = a; v2 = c;
 				float tmp = d0; d0 = d1; d1 = tmp;
 			} else if (Math.signum(d1) == 0) {
 				v0 = a; v1 = b; v2 = c;
 			} else {
 				v0 = a; v1 = c; v2 = b;
 				float tmp = d1; d1 = d2; d2 = tmp;
 			}
 			
 			Vector3f tmp = new Vector3f();
 			tmp.scaleAdd(-1, line.point, v0);
 			float p0 = line.direction.dot(tmp);
 			tmp.scaleAdd(-1, line.point, v1);
 			float p1 = line.direction.dot(tmp);
 			tmp.scaleAdd(-1, line.point, v2);
 			float p2 = line.direction.dot(tmp);
 			
 			TPair region = new TPair();
 			region.t0 = p0 + (p1 - p0) * d0 / (d0 - d1);
 			region.t1 = p2 + (p1 - p2) * d2 / (d2 - d1);
 			if (region.t1 < region.t0) {
 				float tmp2 = region.t0;
 				region.t0 = region.t1;
 				region.t1 = tmp2;
 			}
 			return region;
 		}
 	}
 	
 	public static ArrayList<CollisionInfo> calculateCollisions(CollidableObject a, CollidableObject b) {
 		if (a == b)
 			return EMPTY_COLLISION_LIST;
 		if (a instanceof HalfSpace) {
 			if (b instanceof HalfSpace)
 				return EMPTY_COLLISION_LIST;
 			if (b instanceof Particle)
 				return calculateCollisions((HalfSpace)a, (Particle)b);
 			if (b instanceof Sphere)
 				return calculateCollisions((HalfSpace)a, (Sphere)b);
 			return calculateCollisions((HalfSpace) a, b.getVertices());
 		}
 		if (b instanceof HalfSpace) {
 			if (a instanceof Particle)
 				return flipContactNormals(calculateCollisions((HalfSpace)b, (Particle)a));
 			if (a instanceof Sphere)
 				return flipContactNormals(calculateCollisions((HalfSpace)b, (Sphere)a));
 			return flipContactNormals(calculateCollisions((HalfSpace)b, a.getVertices()));
 		}
 		if (a instanceof Particle) {
 			if (b instanceof Particle)
 				return EMPTY_COLLISION_LIST;
 			if (b instanceof Sphere)
 				return calculateCollisions((Particle)a, (Sphere)b);
 			if (b instanceof Polygon)
 				return calculateCollisions((Particle)a, (Polygon)b);
 		}
 		if (b instanceof Particle) {
 			if (a instanceof Sphere)
 				return flipContactNormals(calculateCollisions((Particle)b, (Sphere)a));
 			if (a instanceof Polygon)
 				return flipContactNormals(calculateCollisions((Particle)b, (Polygon)a));
 		}
 		if (a instanceof Sphere && b instanceof Sphere)
 			return calculateCollisions((Sphere)a, (Sphere)b);
 		
 		if (!a.getBounds().intersect(b.getBounds()))
 			return EMPTY_COLLISION_LIST;
 		
 		if (a instanceof Particle)
 			return calculateCollisions((Particle)a, b);
 		if (b instanceof Particle)
 			return flipContactNormals(calculateCollisions((Particle)b, a));
 		if (a instanceof Polygon)
 			return calculateCollisions((Polygon)a, b);
 		if (b instanceof Polygon)
 			return calculateCollisions((Polygon)b, a);
 		return CollisionDetector.calculateCollisions(a.getCollisionTriangles(), b.getCollisionTriangles());
 	}
 	
 	private static ArrayList<CollisionInfo> calculateCollisions(HalfSpace a, Particle b) {
 		float penetration = a.intercept - a.normal.dot(b.position);
 		if (penetration < 0)
 			return EMPTY_COLLISION_LIST;
 		Vector3f contactPoint = new Vector3f();
 		contactPoint.scaleAdd(penetration, a.normal, b.position);
 		assert(Math.abs(a.normal.dot(contactPoint) - a.intercept) < 0.01);
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		collisions.add(new CollisionInfo(contactPoint, new Vector3f(a.normal), penetration));
 		return collisions;
 	}
 
 	private static ArrayList<CollisionInfo> calculateCollisions(HalfSpace a, Sphere b) {
 		float penetration = b.radius - (a.normal.dot(b.position) - a.intercept);
 		if (penetration < 0)
 			return EMPTY_COLLISION_LIST;
 		Vector3f contactPoint = new Vector3f();
 		contactPoint.scaleAdd(-(b.radius - penetration), a.normal, b.position);
 		assert(Math.abs(a.normal.dot(contactPoint) - a.intercept) < 0.01);
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		collisions.add(new CollisionInfo(contactPoint, new Vector3f(a.normal), penetration));
 		return collisions;
 	}
 
 	private static ArrayList<CollisionInfo> calculateCollisions(HalfSpace a, ArrayList<Vector3f> setB) {
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		for (Vector3f vertex : setB) {
 			float penetration = a.intercept - a.normal.dot(vertex);
 			if (penetration >= 0) {
 				Vector3f contactPoint = new Vector3f();
 				contactPoint.scaleAdd(penetration, a.normal, vertex);
 				assert(Math.abs(a.normal.dot(contactPoint) - a.intercept) < 0.01);
 				collisions.add(new CollisionInfo(contactPoint, new Vector3f(a.normal), penetration));
 			}
 		}
 		return collisions;
 	}
 
 	private static ArrayList<CollisionInfo> calculateCollisions(Particle a, Sphere b) {
 		Vector3f delta = new Vector3f();
 		delta.scaleAdd(-1, a.position, b.position);
 		float penetration = b.radius - delta.length();
 		if (penetration < 0)
 			return EMPTY_COLLISION_LIST;
 		
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		delta.normalize();
 		Vector3f contactPoint = new Vector3f();
 		contactPoint.scaleAdd(-(b.radius - 0.5f * penetration), delta, b.position);
 		collisions.add(new CollisionInfo(contactPoint, delta, penetration));
 		return collisions;
 	}
 
 	private static ArrayList<CollisionInfo> calculateCollisions(Particle a, Polygon b) {
 		float penetration = b.intercept - b.normal.dot(a.position);
 		float previousPenetration = b.intercept - b.normal.dot(a.previousPosition);
 		if (Math.signum(penetration) == Math.signum(previousPenetration))
 			return EMPTY_COLLISION_LIST;
 		
 		for (Triangle triangle : b.getCollisionTriangles()) {
 			Matrix3f tmp = new Matrix3f(a.previousPosition.x - a.position.x, triangle.b.x - triangle.a.x, triangle.c.x - triangle.a.x,
 			                            a.previousPosition.y - a.position.y, triangle.b.y - triangle.a.y, triangle.c.y - triangle.a.y,
 			                            a.previousPosition.z - a.position.z, triangle.b.z - triangle.a.z, triangle.c.z - triangle.a.z);
 			tmp.invert();
 			Vector3f intercept = new Vector3f();
 			intercept.scaleAdd(-1, triangle.a, a.previousPosition);
 			tmp.transform(intercept);
 			
 			assert(intercept.x >= 0 && intercept.x <= 1);
 			
 			if (intercept.y >= 0 && intercept.y <= 1 && intercept.z >= 0 && intercept.z <= 1 && (intercept.y + intercept.z) <= 1) {
 				Vector3f contactPoint = new Vector3f();
 				contactPoint.scaleAdd(-1, a.previousPosition, a.position);
 				contactPoint.scale(intercept.x);
 				contactPoint.add(a.previousPosition);
 				assert(Math.abs(b.normal.dot(contactPoint) - b.intercept) < 0.01);
 				Vector3f contactNormal = new Vector3f(b.normal);
 				if (penetration - previousPenetration > 0)
 					contactNormal.negate();
 				else
 					penetration = -penetration;
 				ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 				collisions.add(new CollisionInfo(contactPoint, contactNormal, penetration));
 				return collisions;
 			}
 		}
 		return EMPTY_COLLISION_LIST;
 	}
 	
 	private static ArrayList<CollisionInfo> calculateCollisions(Particle a, CollidableObject b) {
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		for (Triangle triangle : b.getCollisionTriangles()) {
 			float penetration = triangle.intercept - triangle.normal.dot(a.position);
 			if (penetration < 0 || (!collisions.isEmpty() && penetration <= collisions.get(0).penetration))
 				continue;
 			float previousPenetration = triangle.intercept - triangle.normal.dot(a.previousPosition);
 			if (Math.signum(penetration) == Math.signum(previousPenetration))
 				continue;
 			
 			Matrix3f tmp = new Matrix3f(a.previousPosition.x - a.position.x, triangle.b.x - triangle.a.x, triangle.c.x - triangle.a.x,
 			                            a.previousPosition.y - a.position.y, triangle.b.y - triangle.a.y, triangle.c.y - triangle.a.y,
 			                            a.previousPosition.z - a.position.z, triangle.b.z - triangle.a.z, triangle.c.z - triangle.a.z);
			try {
				tmp.invert();
			} catch (SingularMatrixException e) {
				return EMPTY_COLLISION_LIST;
			}
 			Vector3f intercept = new Vector3f();
 			intercept.scaleAdd(-1, triangle.a, a.previousPosition);
 			tmp.transform(intercept);
 			
 			assert(intercept.x >= 0 && intercept.x <= 1);
 			
 			if (intercept.y >= 0 && intercept.y <= 1 && intercept.z >= 0 && intercept.z <= 1 && (intercept.y + intercept.z) <= 1) {
 				Vector3f contactPoint = new Vector3f();
 				contactPoint.scaleAdd(-1, a.previousPosition, a.position);
 				contactPoint.scale(intercept.x);
 				contactPoint.add(a.previousPosition);
 				assert(Math.abs(triangle.normal.dot(contactPoint) - triangle.intercept) < 0.01);
 				Vector3f contactNormal = new Vector3f(triangle.normal);
 				if (penetration - previousPenetration > 0)
 					contactNormal.negate();
 				else
 					penetration = -penetration;				
 				collisions.clear();
 				collisions.add(new CollisionInfo(contactPoint, contactNormal, penetration));
 			}
 		}
 		return collisions;
 	}
 	
 	private static ArrayList<CollisionInfo> calculateCollisions(Sphere a, Sphere b) {
 		Vector3f delta = new Vector3f();
 		delta.scaleAdd(-1, a.position, b.position);
 		float penetration = a.radius + b.radius - delta.length();
 		if (penetration < 0)
 			return EMPTY_COLLISION_LIST;
 		
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		delta.normalize();
 		Vector3f contactPoint = new Vector3f();
 		contactPoint.scaleAdd(a.radius - 0.5f * penetration, delta, a.position);
 		collisions.add(new CollisionInfo(contactPoint, delta, penetration));
 		return collisions;
 	}
 
 	private static ArrayList<CollisionInfo> calculateCollisions(Polygon a, CollidableObject b) {
 		ArrayList<CollisionInfo> collisions = calculateCollisions(a.getCollisionTriangles(), b.getCollisionTriangles());
 		int size = collisions.size();
 		collisions.ensureCapacity(2 * size);
 		for (int i = 0; i < size; i++) {
 			collisions.add(collisions.get(i).clone());
 			collisions.get(collisions.size() - 1).contactNormal.negate();
 		}
 		return collisions;
 	}
 	
 	private static ArrayList<CollisionInfo> calculateCollisions(ArrayList<Triangle> setA, ArrayList<Triangle> setB) {
 		ArrayList<CollisionInfo> collisions = new ArrayList<CollisionInfo>();
 		for (int i = 0; i < setA.size(); i++)
 			for (int j = 0; j < setB.size(); j++) {
 				CollisionInfo collision = setA.get(i).getIntersection(setB.get(j));
 				if (collision != null)
 					collisions.add(collision);
 			}
 		return collisions;
 	}
 
 	private static ArrayList<CollisionInfo> flipContactNormals(ArrayList<CollisionInfo> collisions) {
 		for (CollisionInfo collision : collisions)
 			collision.contactNormal.negate();
 		return collisions;
 	}
 
 	public static ArrayList<Vector3f> extractVertices(Node node) {
 		ArrayList<Vector3f> vertices = new ArrayList<Vector3f>();
 		extractVertices(node, vertices);
 		return vertices;
 	}
 	
 	private static void extractVertices(Node node, ArrayList<Vector3f> vertices) {
 		if (node instanceof Primitive) {
 			Primitive prim = (Primitive)node;
 			int index = 0;
 			Shape3D shape;
 			Transform3D L2V = new Transform3D();
 			while ((shape = prim.getShape(index++)) != null) {
 				shape.getLocalToVworld(L2V);
 				for (int i = 0; i < shape.numGeometries(); i++)
 					extractVertices(shape.getGeometry(i), L2V, vertices);
 			}
 		} else if (node instanceof Shape3D) {
 			Shape3D shape = (Shape3D)node;
 			Transform3D L2V = new Transform3D();
 			shape.getLocalToVworld(L2V);
 			for (int i = 0; i < shape.numGeometries(); i++)
 				extractVertices(shape.getGeometry(i), L2V, vertices);
 		} else if (node instanceof Group) {
 			Group group = (Group)node;
 			for (int i = 0; i < group.numChildren(); i++)
 				extractVertices(group.getChild(i), vertices);
 		} else 
 			throw new IllegalArgumentException("Illegal node type for vertex extraction");
 	}
 
 	private static void extractVertices(Geometry geometry, Transform3D transform, ArrayList<Vector3f> vertices) {
 		if (geometry instanceof GeometryArray) {
 			GeometryArray trueGeometry = (GeometryArray)geometry;
 			vertices.ensureCapacity(vertices.size() + trueGeometry.getValidVertexCount());
 			Point3f vertex = new Point3f();
 			for (int i = 0; i < trueGeometry.getValidVertexCount(); i++) {
 				trueGeometry.getCoordinate(i, vertex);
 				transform.transform(vertex);
 				vertices.add(new Vector3f(vertex));
 			}
 		}  else
 			throw new IllegalArgumentException("Illegal geometry type for vertex extraction");
 	}
 	
 	public static ArrayList<Triangle> triangularize(Node node) {
 		ArrayList<Triangle> triangles = new ArrayList<Triangle>();
 		triangularize(node, triangles);
 		return triangles;
 	}
 	
 	private static void triangularize(Node node, ArrayList<Triangle> triangles) {
 		if (node instanceof Primitive) {
 			Primitive prim = (Primitive)node;
 			triangles.ensureCapacity(prim.getNumTriangles());
 			int index = 0;
 			Shape3D shape;
 			Transform3D L2V = new Transform3D();
 			while ((shape = prim.getShape(index++)) != null) {
 				shape.getLocalToVworld(L2V);
 				for (int i = 0; i < shape.numGeometries(); i++)
 					triangularize(shape.getGeometry(i), L2V, triangles);
 			}
 		} else if (node instanceof Shape3D) {
 			Shape3D shape = (Shape3D)node;
 			Transform3D L2V = new Transform3D();
 			shape.getLocalToVworld(L2V);
 			for (int i = 0; i < shape.numGeometries(); i++)
 				triangularize(shape.getGeometry(i), L2V, triangles);
 		} else if (node instanceof Group) {
 			Group group = (Group)node;
 			for (int i = 0; i < group.numChildren(); i++)
 				triangularize(group.getChild(i), triangles);
 		} else 
 			throw new IllegalArgumentException("Illegal node type for triangularization");
 	}
 
 	private static void triangularize(Geometry geometry, Transform3D transform, ArrayList<Triangle> triangles) {
 		if (geometry instanceof TriangleArray) {
 			TriangleArray trueGeometry = (TriangleArray)geometry;
 			Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < vertices.length; i++) {
 				vertices[i] = new Point3f();
 				trueGeometry.getCoordinate(i, vertices[i]);
 				transform.transform(vertices[i]);
 			}
 			for (int i = 0; i < vertices.length; i += 3)
 				try {
 					triangles.add(new Triangle(vertices[i], vertices[i+1], vertices[i+2]));
 				} catch (IllegalArgumentException e) {
 				}
 		} else if (geometry instanceof TriangleStripArray) {
 			TriangleStripArray trueGeometry = (TriangleStripArray)geometry;
 			int stripVertexCounts[] = new int[trueGeometry.getNumStrips()];
 			trueGeometry.getStripVertexCounts(stripVertexCounts);
 			Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < vertices.length; i++) {
 				vertices[i] = new Point3f();
 				trueGeometry.getCoordinate(i, vertices[i]);
 				transform.transform(vertices[i]);
 			}
 			int base = 0;
 			for (int i = 0; i < stripVertexCounts.length; i++) {
 				boolean reverse = false;
 				for (int j = 2; j < stripVertexCounts[i]; j++) {
 					try {
 						if (reverse)
 							triangles.add(new Triangle(vertices[base+j-2], vertices[base+j], vertices[base+j-1]));
 						else
 							triangles.add(new Triangle(vertices[base+j-2], vertices[base+j-1], vertices[base+j]));
 					} catch (IllegalArgumentException e) {
 					}
 					reverse = !reverse;
 				}
 				base += stripVertexCounts[i];
 			}
 		} else if (geometry instanceof TriangleFanArray) {
 			TriangleFanArray trueGeometry = (TriangleFanArray)geometry;
 			int stripVertexCounts[] = new int[trueGeometry.getNumStrips()];
 			trueGeometry.getStripVertexCounts(stripVertexCounts);
 			Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < vertices.length; i++) {
 				vertices[i] = new Point3f();
 				trueGeometry.getCoordinate(i, vertices[i]);
 				transform.transform(vertices[i]);
 			}
 			int base = 0;
 			for (int i = 0; i < stripVertexCounts.length; i++) {
 				for (int j = 2; j < stripVertexCounts[i]; j++)
 					try {
 						triangles.add(new Triangle(vertices[base], vertices[base+j-1], vertices[base+j]));
 					} catch (IllegalArgumentException e) {
 					}
 				base += stripVertexCounts[i];
 			}
 		} else if (geometry instanceof IndexedTriangleArray) {
 			IndexedTriangleArray trueGeometry = (IndexedTriangleArray)geometry;
 			Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < vertices.length; i++) {
 				vertices[i] = new Point3f();
 				trueGeometry.getCoordinate(i, vertices[i]);
 				transform.transform(vertices[i]);
 			}
 			int indices[] = new int[trueGeometry.getValidIndexCount()];
 			trueGeometry.getCoordinateIndices(0, indices);
 			for (int i = 0; i < indices.length; i += 3)
 				try {
 					triangles.add(new Triangle(vertices[indices[i]], vertices[indices[i+1]], vertices[indices[i+2]]));
 				} catch (IllegalArgumentException e) {
 				}
 		} else if (geometry instanceof IndexedTriangleStripArray) {
 			IndexedTriangleStripArray trueGeometry = (IndexedTriangleStripArray)geometry;
 			int stripIndexCounts[] = new int[trueGeometry.getNumStrips()];
 			trueGeometry.getStripIndexCounts(stripIndexCounts);
 			Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < vertices.length; i++) {
 				vertices[i] = new Point3f();
 				trueGeometry.getCoordinate(i, vertices[i]);
 				transform.transform(vertices[i]);
 			}			
 			int indices[] = new int[trueGeometry.getValidIndexCount()];
 			trueGeometry.getCoordinateIndices(0, indices);
 			int base = 0;
 			for (int i = 0; i < stripIndexCounts.length; i++) {
 				boolean reverse = false;
 				for (int j = 2; j < stripIndexCounts[i]; j++) {
 					try {
 						if (reverse)
 							triangles.add(new Triangle(vertices[indices[base+j-2]], vertices[indices[base+j]], vertices[indices[base+j-1]]));
 						else
 							triangles.add(new Triangle(vertices[indices[base+j-2]], vertices[indices[base+j-1]], vertices[indices[base+j]]));
 					} catch (IllegalArgumentException e) {
 					}
 					reverse = !reverse;
 				}
 				base += stripIndexCounts[i];
 			}
 		} else if (geometry instanceof IndexedTriangleFanArray) {
 			IndexedTriangleFanArray trueGeometry = (IndexedTriangleFanArray)geometry;
 			int stripIndexCounts[] = new int[trueGeometry.getNumStrips()];
 			trueGeometry.getStripIndexCounts(stripIndexCounts);
 			Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < vertices.length; i++) {
 				vertices[i] = new Point3f();
 				trueGeometry.getCoordinate(i, vertices[i]);
 				transform.transform(vertices[i]);
 			}			
 			int indices[] = new int[trueGeometry.getValidIndexCount()];
 			trueGeometry.getCoordinateIndices(0, indices);
 			int base = 0;
 			for (int i = 0; i < stripIndexCounts.length; i++) {
 				for (int j = 2; j < stripIndexCounts[i]; j++)
 					try {
 						triangles.add(new Triangle(vertices[indices[base]], vertices[indices[base+j-1]], vertices[indices[base+j]]));
 					} catch (IllegalArgumentException e) {
 					}
 				base += stripIndexCounts[i];
 			}
 		} else
 			throw new IllegalArgumentException("Illegal geometry type for triangularization");
 	}
 
 	public static Shape3D createShape(ArrayList<Triangle> triangles) {
 		TriangleArray geometry = new TriangleArray(3 * triangles.size(), TriangleArray.COORDINATES);
 		for (int i = 0; i < triangles.size(); i++) {
 			geometry.setCoordinate(3 * i, new Point3f(triangles.get(i).a));
 			geometry.setCoordinate(3 * i + 1, new Point3f(triangles.get(i).b));
 			geometry.setCoordinate(3 * i + 2, new Point3f(triangles.get(i).c));
 		}
 		Appearance appearance = new Appearance();
 		PolygonAttributes polyAttr = new PolygonAttributes(PolygonAttributes.POLYGON_LINE, PolygonAttributes.CULL_NONE, 0);
 		appearance.setPolygonAttributes(polyAttr);
 		return new Shape3D(geometry, appearance);
 	}
 }
