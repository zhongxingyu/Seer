 package ja3d.collisions;
 
 import ja3d.core.Object3D;
 import ja3d.core.Transform3D;
 import ja3d.core.VertexAttributes;
 import ja3d.core.VertexStream;
 import ja3d.resources.Geometry;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import utils.ByteArray;
 import utils.Vector3D;
 import static utils.Vector3D.*;
 
 public class EllipsoidCollider {
 	
 	public double radiusX;
 	public double radiusY;
 	public double radiusZ;
 	
 	// rotationZ, is this value compatible with the value of that rotationZ in the global space?
 	public double rotationZ;
 	
 	private double threshold = 0.001;
 	
 	private Transform3D matrix = new Transform3D();
 	private Transform3D inverseMatrix = new Transform3D();
 	
 	// when preparing, object.collectGeometry will invoke this.addGeometry to fill this.
 	// After collision detected, these pools will be cleared.
 	private List<Geometry> geometries = new ArrayList<Geometry>();
 	private List<Transform3D> transforms = new ArrayList<Transform3D>();
 	
 	private List<Double> vertices = new ArrayList<Double>();
 	private List<Double> normals = new ArrayList<Double>();
 	private List<Integer> indices = new ArrayList<Integer>();
 	private int numTriangles;
 	
 	// (number, name) pairs, which indicates the triangles starting from index number belongs to the mesh of the given name.
 	// This array is filled in prepare and used in the collision detection.
 	private List<Object> triangleIndices = new ArrayList<Object>();
 	// the name of the collided object. This value is set in checkCollision.
 	private String collisionTargetName;
 	
 	// source, displacement and destination in the local space.
 	private double radius;
 	private Vector3D src = new Vector3D();
 	private Vector3D displ = new Vector3D();
 	private Vector3D dest = new Vector3D();
 	//
 	
 	private Vector3D collisionPoint = new Vector3D();
 	private Vector3D collisionPlane = new Vector3D();
 	
 	// the bound of the movement base.
 	private Vector3D sphere = new Vector3D();
 	private Vector3D cornerA = new Vector3D();
 	private Vector3D cornerB = new Vector3D();
 	private Vector3D cornerC = new Vector3D();
 	private Vector3D cornerD = new Vector3D();
 	//
 	
 	public EllipsoidCollider(double radiusX, double radiusY, double radiusZ) {
 		this.radiusX = radiusX;
 		this.radiusY = radiusY;
 		this.radiusZ = radiusZ;
 	}
 	
 	public void calculateSphere(Transform3D transform) {
 		// place the sphere at the transform translation.
 		sphere.setTo(transform.d(), transform.h(), transform.l());
 		sphere.w = 0;
 		
 		// sphere.w, the radius is the max distance from the sphere to the four corners.
 		Vector3D s = new Vector3D();
 		Vector3D d = new Vector3D();
 		Vector3D[] corners = {cornerA, cornerB, cornerC, cornerD};
 		for (Vector3D corner : corners) {
 			transform.transform(corner, s);
 			substract(s, sphere, d);
 			double dxyz = d.getLength();
 			if (dxyz > sphere.w) {
 				sphere.w = dxyz;
 			}
 		}
 	}
 	
 	private void prepare(Vector3D source, Vector3D displacement, 
 			Object3D object, Map<Object3D, Object3D> excludedObjects) {
 		
 		// Radius of the sphere
 		radius = Math.max(Math.max(radiusX, radiusY), radiusZ);
 		
 		// use the virtual sphere to do collision detection
 		// Using the status of this ellipsoid as control,
 		// transform the specified object in the global world into the local world of the virtual sphere.
 		// This is the transform matrix with the ellipsoid as control.
 		// We call this coordinates space as the collider space or the collision space.  
 		// TODO: can we deal with rotation here?
 		matrix.compose(source.x, source.y, source.z, 0, 0, rotationZ, radiusX / radius, radiusY / radius, radiusZ / radius);
 		inverseMatrix.copy(matrix);
 		inverseMatrix.invert();
 		
 		// Local source - src
 		src.setTo(0, 0, 0);
 		// Local offset - displ
 		inverseMatrix.transformWithoutTranslate(displacement, displ);
 		// Local destination point - dest
 		add(src, displ, dest);
 		
 		// Bound defined by movement of the sphere
 		double rad = radius + displ.getLength();
 		cornerA.setTo(-rad, -rad, -rad);
 		cornerB.setTo(+rad, -rad, -rad);
 		cornerC.setTo(+rad, +rad, -rad);
 		cornerD.setTo(-rad, +rad, -rad);
 		
 		if (excludedObjects == null || !excludedObjects.containsKey(object)) {
 			object.composeTransforms();
 			// inverseTransform transforms the object coordinates into the global world.
 			// matrix transforms the global world coordinates into the sphere local coordinates.
 			// now the globalToLocalTransform of the object has the value as the combine.
 			Transform3D.multiply(object.inverseTransform, matrix, object.globalToLocalTransform);
 			
 			// Check collision with the bound
 			boolean intersects = true;
 			if (object.getBoundBox() != null) {
 				calculateSphere(object.globalToLocalTransform);
 				intersects = object.getBoundBox().checkSphere(sphere);
 			}
 			if (intersects) {
 				Transform3D.multiply(inverseMatrix, object.transform, object.localToGlobalTransform);
 				object.collectGeometry(this, excludedObjects);
 			}
 			// Check children
 			if (object.hasChildren()) {
 				object.collectChildrenGeometry(this, excludedObjects);
 			}
 		}
 		
 		triangleIndices.clear();
 		numTriangles = 0;
 		
 		// Loop geometries
 		// because vertices, indices and normals have more than one geometry information,
 		// use mapOffset variable to identify which geometry is in use.
 		int mapOffset = 0;
 		for (int i = 0; i < geometries.size(); i++) {
 			Geometry geometry = geometries.get(i);
 			Transform3D transform = transforms.get(i);
 			int geometryIndicesLength = geometry._indices.size();
 			if (geometry.getNumVertices() == 0 || geometryIndicesLength == 0) {
 				continue;
 			}
 			
 			if (triangleIndices.size() > 0) {
 				Integer lastIndex = (Integer) triangleIndices.get(triangleIndices.size() - 2);
 				if (lastIndex.intValue() == numTriangles) {
 					triangleIndices.set(triangleIndices.size() - 1, geometry.getName());
 				} else {
 					triangleIndices.add(new Integer(numTriangles));
 					triangleIndices.add(geometry.getName());
 				}
 			} else {
 				triangleIndices.add(new Integer(numTriangles));
 				triangleIndices.add(geometry.getName());
 			}
 			
 			// Transform vertices
 			VertexStream vBuffer = geometry.getVertexStream(VertexAttributes.POSITION);
 			if (vBuffer != null) {
 				int attributesOffset = geometry.getAttributeOffset(VertexAttributes.POSITION);
 				int numMappings = vBuffer.attributes.length;
 				ByteArray data = vBuffer.data;
 				for (int j = 0; j < geometry.getNumVertices(); j++) {
 					data.setPosition(4 * (numMappings * j + attributesOffset));
 					Vector3D v = new Vector3D();
 					v.x = data.readFloat();
 					v.y = data.readFloat();
 					v.z = data.readFloat();
 					Vector3D tv = new Vector3D();
 					transform.transform(v, tv);
 					vertices.add(tv.x);
 					vertices.add(tv.y);
 					vertices.add(tv.z);
 				}
 			}
 			
 			// Loop triangles
 			List<Integer> geometryIndices = geometry._indices;
 			for (int j = 0; j < geometryIndicesLength; j += 3) {
 				int ai = geometryIndices.get(j) + mapOffset;
 				int bi = geometryIndices.get(j + 1) + mapOffset;
 				int ci = geometryIndices.get(j + 2) + mapOffset;
 				
 				Vector3D a = new Vector3D(vertices.get(ai * 3), vertices.get(ai * 3 + 1), vertices.get(ai * 3 + 2));
 				Vector3D b = new Vector3D(vertices.get(bi * 3), vertices.get(bi * 3 + 1), vertices.get(bi * 3 + 2));
 				Vector3D c = new Vector3D(vertices.get(ci * 3), vertices.get(ci * 3 + 1), vertices.get(ci * 3 + 2));
 				
 				// Exclusion by bound
 				if (a.x > rad && b.x > rad && c.x > rad || a.x < -rad && b.x < -rad && c.x < -rad) continue;
 				if (a.y > rad && b.y > rad && c.y > rad || a.y < -rad && b.y < -rad && c.y < -rad) continue;
 				if (a.z > rad && b.z > rad && c.z > rad || a.z < -rad && b.z < -rad && c.z < -rad) continue;
 				
 				// The normal
 				Vector3D ab = substract(b, a, new Vector3D());
 				Vector3D ac = substract(c, a, new Vector3D());
 				Vector3D normal = crossProduct(ab, ac, new Vector3D());
 				double len = normal.getLengthSquared();
 				if (len < 0.001) continue;
 				normal.normalize();
 				double offset = dotProduct(a, normal);
 				if (offset > rad || offset < -rad) continue;
 				indices.add(ai);
 				indices.add(bi);
 				indices.add(ci);
 				normals.add(normal.x);
 				normals.add(normal.y);
 				normals.add(normal.z);
 				normals.add(offset);
 				numTriangles ++;
 			}
 			// Offset by number of vertices
 			mapOffset += geometry.getNumVertices();
 		}
 		
 		geometries.clear();
 		transforms.clear();
 		
 		triangleIndices.add(Integer.MAX_VALUE);
 	}
 	
 	public Vector3D calculateDestination(Vector3D source, Vector3D displacement, Object3D object, Map<Object3D, Object3D> excludedObjects) {
 		if (displacement.getLength() <= threshold) {
 			return new Vector3D(source.x, source.y, source.z);
 		}
 		
 		prepare(source, displacement, object, excludedObjects);
 		
 		if (numTriangles > 0) {
 			int limit = 50;
 			for (int i = 0; i < limit; i++) {
 				if (checkCollision()) {
 					// Offset destination point from behind collision plane by radius of the sphere over plane, along the normal
 					double offset = radius + threshold + collisionPlane.w - dotProduct(dest, collisionPlane);
 					ma(dest, collisionPlane, offset, dest);
 					// Fixing up the current sphere coordinates for the next iteration
 					ma(collisionPoint, collisionPlane, radius + threshold, src);
 					// Fixing up velocity vector. The result ordered along plane of collision.
 					substract(dest, src, displ);
 					if (displ.getLength() < threshold) break;
 				} else {
 					break;
 				}
 			}
 			// Setting the coordinates
 			return matrix.transform(dest, new Vector3D());
 		} else {
 			return add(source, displacement, new Vector3D());
 		}
 	}
 	
 	public boolean getCollision(Vector3D source, Vector3D displacement, 
 			Vector3D resCollisionPoint, Vector3D resCollisionPlane,
 			Object3D object, Map<Object3D, Object3D> excludedObjects) {
 		
 		if (displacement.getLength() < threshold) {
 			return false;
 		}
 		
 		prepare(source, displacement, object, excludedObjects);
 		
 		if (numTriangles > 0) {
 			if (checkCollision()) {
 				// Transform the point to the global space
 				matrix.transform(collisionPoint, resCollisionPoint);
 				
 				// Transform the plane to the global space
 				Vector3D ab = new Vector3D();
 				if (collisionPlane.x < collisionPlane.y) {
 					if (collisionPlane.x < collisionPlane.z) {
 						ab.x = 0;
 						ab.y = -collisionPlane.z;
 						ab.z = collisionPlane.y;
 					} else {
 						ab.x = -collisionPlane.y;
 						ab.y = collisionPlane.x;
 						ab.z = 0;
 					}
 				} else {
 					if (collisionPlane.y < collisionPlane.z) {
 						ab.x = collisionPlane.z;
 						ab.y = 0;
 						ab.z = -collisionPlane.x;
 					} else {
 						ab.x = -collisionPlane.y;
 						ab.y = collisionPlane.x;
 						ab.z = 0;
 					}
 				}
 				
 				Vector3D ac = crossProduct(ab, collisionPlane, new Vector3D());
 				Vector3D ab2 = matrix.transformWithoutTranslate(ab, new Vector3D());
 				Vector3D ac2 = matrix.transformWithoutTranslate(ac, new Vector3D());
 				
 				crossProduct(ac2, ab2, resCollisionPlane);
 				resCollisionPlane.normalize();
 				resCollisionPlane.w = 1;
 				//
 				
 				return true;
 			} else {
 				return false;
 			}
 		}
 		
 		return false;
 	}
 
 	private boolean checkCollision() {
 		collisionTargetName = "";
 		lastIndex = -2;
 		
 		double minTime = 1;
 		double displacementLength = displ.getLength();
 		
 		// Loop triangles
 		int indicesLength = numTriangles * 3;
 		for (int i = 0, j = 0; i < indicesLength;) {
 			// which triangle is this? used to identity the geometry and its mesh name.
 			String meshName = getGeometryNameForTriangle(i / 3);
 			
 			// three Points for the triangle
 			int indexA = indices.get(i) * 3;
 			int indexB = indices.get(i + 1) * 3;
 			int indexC = indices.get(i + 2) * 3;
 			i += 3;
 			
 			Vector3D a = new Vector3D(vertices.get(indexA), vertices.get(indexA + 1), vertices.get(indexA + 2));
 			Vector3D b = new Vector3D(vertices.get(indexB), vertices.get(indexB + 1), vertices.get(indexB + 2));
 			Vector3D c = new Vector3D(vertices.get(indexC), vertices.get(indexC + 1), vertices.get(indexC + 2));
 			
 			// Normal
 			Vector3D normal = new Vector3D(normals.get(j), normals.get(j + 1), normals.get(j + 2));
 			double offset = normals.get(j + 3);
 			j += 4;
 			
 			// distance from src to the triangle
 			double distance = dotProduct(src, normal) - offset; 
 			
 			// The intersection of plane and sphere
 			Vector3D point = new Vector3D();
 			if (distance < radius) {
 				ma(src, normal, -distance, point);
 			} else {
 				double t = (distance - radius) / (distance - dotProduct(dest, normal) + offset);
 				ma(ma(src, displ, t, point), normal, -radius, point);
 			}
 			
 			// Now to calculate Closest polygon vertex(face)
 			Vector3D face = null;
 			double min = Double.MAX_VALUE;
 			boolean inside = true;
 			Vector3D[] ps = {a, b, c};
 			for (int k = 0; k < ps.length; k++) {
 				Vector3D p1 = ps[k];
 				Vector3D p2 = ps[(k + 1) % ps.length];
 				Vector3D ab = substract(p2, p1, new Vector3D());
 				Vector3D ac = substract(point, p1, new Vector3D());
 				Vector3D cr = crossProduct(ab, ac, new Vector3D());
 				// Case of the point is outside of the polygon
 				if (dotProduct(cr, normal) < 0) {
 					double edgeLength = ab.getLengthSquared();
 					double edgeDistanceSqr = cr.getLengthSquared() / edgeLength;
 					if (edgeDistanceSqr < min) {
 						// Edge normalization
 						ab.normalize();
 						// Distance to intersection of normal along the edge
 						double t = dotProduct(ab, ac);
 						if (t < 0) {
 							// Closest point is the first one
 							double acLen = ac.getLengthSquared();
 							if (acLen < min) {
 								min = acLen;
 								face = p1;
 							}
 						} else if (t > edgeLength) {
 							// Closest point is the second one
 							substract(point, p2, ac);
 							double acLen = ac.getLengthSquared();
 							if (acLen < min) {
 								min = acLen;
 								face = p2;
 							}
 						} else {
 							// Closest point is on edge
 							min = edgeDistanceSqr;
 							face = ma(p1, ab, t, new Vector3D());
 						}
 					}
 					inside = false;
 				}
 			}
 			
 			// Case of point is inside polygon
 			if (inside) {
 				face = point;
 			}
 			
 			// Vector pointed from closest point to the center of sphere
 			Vector3D delta = substract(src, face, new Vector3D());
 			
 			// If move directed to point
 			if (dotProduct(delta, displ) <= 0) {
 				// reserved vector
 				Vector3D back = scale(displ, -1 / displacementLength, new Vector3D());
 				// Length of Vector pointed from closest point to the center of sphere
 				double deltaLength = delta.getLengthSquared();
 				double projectionLength = dotProduct(delta, back);
 				double projectionInsideLength = radius * radius - deltaLength + projectionLength * projectionLength;
 				if (projectionInsideLength > 0) {
 					// Time of the intersection
 					double time = (projectionLength - Math.sqrt(projectionInsideLength)) / displacementLength;
 					// Collision with closest point occurs
 					if (time < minTime) {
 						minTime = time;
 						collisionTargetName = meshName;
 						collisionPoint = face;
 						if (inside) {
 							collisionPlane = normal;
 							collisionPlane.w = offset;
 						} else {
 							collisionPlane = delta;
 							collisionPlane.normalize();
 							collisionPlane.w = dotProduct(collisionPoint, collisionPlane);
 						}
 					}
 				}
 			}
 		}
 		
 		return minTime < 1;
 	}
 
 	public Vector3D getSphere() {
 		return sphere;
 	}
 
 	// this method is called while collecting geometries from the object3d to be detected with.
 	public void addGeometry(Geometry geometry, Transform3D transform) {
 		geometries.add(geometry);
 		transforms.add(transform);
 	}
 	
 	private int lastIndex = 0;
 	private String getGeometryNameForTriangle(int triangleNumber) {
 		while (true) {
 			Integer index = (Integer)triangleIndices.get(lastIndex + 2);
 			if (triangleNumber >= index.intValue()) {
 				lastIndex += 2;
 			} else {
 				break;
 			}
 		}
 		
 		return (String)triangleIndices.get(lastIndex + 1);
 	}
 	
 	public String getCollisionTarget() {
 		return collisionTargetName;
 	}
 }
