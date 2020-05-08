 /**
  *
  */
 package ca.eandb.jmist.framework.geometry.primitive;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import ca.eandb.jmist.framework.Bounded3;
 import ca.eandb.jmist.framework.BoundingBoxBuilder3;
 import ca.eandb.jmist.framework.Intersection;
 import ca.eandb.jmist.framework.IntersectionRecorder;
 import ca.eandb.jmist.framework.Random;
 import ca.eandb.jmist.framework.ShadingContext;
 import ca.eandb.jmist.framework.geometry.AbstractGeometry;
 import ca.eandb.jmist.framework.random.CategoricalRandom;
 import ca.eandb.jmist.framework.random.RandomUtil;
 import ca.eandb.jmist.math.Basis3;
 import ca.eandb.jmist.math.Box3;
 import ca.eandb.jmist.math.GeometryUtil;
 import ca.eandb.jmist.math.Plane3;
 import ca.eandb.jmist.math.Point2;
 import ca.eandb.jmist.math.Point3;
 import ca.eandb.jmist.math.Ray3;
 import ca.eandb.jmist.math.Sphere;
 import ca.eandb.jmist.math.Vector3;
 
 /**
  * A polyhedron <code>SceneElement</code>.
  * @author Brad Kimmel
  */
 public final class PolyhedronGeometry extends AbstractGeometry {
 
 	/**
 	 * Serialization version ID.
 	 */
 	private static final long serialVersionUID = 262374288661771750L;
 
 	/**
 	 * Creates a new <code>PolyhedronGeometry</code>.
 	 * @param vertices An array of the vertices of the polyhedron.
 	 * @param faces An array of faces.  Each face is an array of indices into
 	 * 		the <code>vertices</code> array.  The front side of the face is the
 	 * 		side from which the vertices appear in counter-clockwise order.
 	 */
 	public PolyhedronGeometry(Point3[] vertices, int[][] faces) {
 		this.vertices = new ArrayList<Point3>(vertices.length);
 		this.vertices.addAll(Arrays.asList(vertices));
 
 		this.texCoords = new ArrayList<Point2>();
 		this.normals = new ArrayList<Vector3>();
 
 		for (int i = 0; i < faces.length; i++) {
 			this.faces.add(new Face(faces[i], null, null));
 		}
 	}
 
 	public PolyhedronGeometry(List<Point3> vertices, List<Point2> texCoords, List<Vector3> normals) {
 		this.vertices = vertices;
 		this.texCoords = texCoords;
 		this.normals = normals;
 	}
 
 	public PolyhedronGeometry() {
 		this(new ArrayList<Point3>(), new ArrayList<Point2>(), new ArrayList<Vector3>());
 	}
 
 	public PolyhedronGeometry addVertex(Point3 v) {
 		vertices.add(v);
 		return this;
 	}
 
 	public PolyhedronGeometry addNormal(Vector3 vn) {
 		normals.add(vn);
 		return this;
 	}
 
 	public PolyhedronGeometry addTexCoord(Point2 vt) {
 		texCoords.add(vt);
 		return this;
 	}
 
 	public PolyhedronGeometry addFace(int[] vi) {
 		return addFace(vi, null, null);
 	}
 
 	public PolyhedronGeometry addFace(int[] vi, int[] vti) {
 		return addFace(vi, vti, null);
 	}
 
 	public PolyhedronGeometry addFace(int[] vi, int[] vti, int[] vni) {
 		faces.add(new Face(vi, vti, vni));
 		return this;
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.SceneElement#intersect(int, ca.eandb.jmist.math.Ray3, ca.eandb.jmist.framework.IntersectionRecorder)
 	 */
 	public void intersect(int index, Ray3 ray, IntersectionRecorder recorder) {
 		intersectFace(index, ray, recorder);
 	}
 
 	/**
 	 * Intersects a <code>Ray3</code> with a face.
 	 * @param faceIndex The index of the <code>Face</code> to intersect.
 	 * @param ray The <code>Ray3</code> to intersect with the indicated
 	 * 		<code>Face</code>.
 	 * @param recorder The <code>IntersectionRecorder</code> to record
 	 * 		intersections to.
 	 */
 	private void intersectFace(int faceIndex, Ray3 ray, IntersectionRecorder recorder) {
 
 		Face			face = this.faces.get(faceIndex);
 		double			t = face.plane.intersect(ray);
 
 		if (recorder.interval().contains(t)) {
 
 			Point3		p = ray.pointAt(t);
 			Vector3		n = face.plane.normal();
 			Vector3		u, v;
 
 			for (int i = 0; i < face.indices.length; i++)
 			{
 				Point3	a = this.vertices.get(face.indices[i]);
 				Point3	b = this.vertices.get(face.indices[(i + 1) % face.indices.length]);
 				Vector3	ab = a.vectorTo(b);
 
 				u = n.cross(ab);
 				v = p.vectorFrom(a);
 
 				if (u.dot(v) < 0.0) {
 					return;
 				}
 			}
 
 			Intersection x = super.newIntersection(ray, t, ray.direction().dot(n) < 0.0, faceIndex)
 				.setLocation(p)
 				.setPrimitiveIndex(faceIndex);
 
 			recorder.record(x);
 
 		}
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#getBasis(ca.eandb.jmist.framework.geometry.AbstractGeometry.GeometryIntersection)
 	 */
 	@Override
 	protected Basis3 getBasis(GeometryIntersection x) {
 		Face face = faces.get(x.getTag());
 		Vector3 n = face.plane.normal();
 		return Basis3.fromW(n, Basis3.Orientation.RIGHT_HANDED);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.SceneElement#getBoundingBox(int)
 	 */
 	public Box3 getBoundingBox(int index) {
 		return faces.get(index).boundingBox();
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.SceneElement#getBoundingSphere(int)
 	 */
 	public Sphere getBoundingSphere(int index) {
 		return faces.get(index).boundingSphere();
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.SceneElement#getNumPrimitives()
 	 */
 	public int getNumPrimitives() {
 		return faces.size();
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Bounded3#boundingBox()
 	 */
 	public Box3 boundingBox() {
 
 		BoundingBoxBuilder3 builder = new BoundingBoxBuilder3();
 
 		for (Point3 vertex : this.vertices) {
 			builder.add(vertex);
 		}
 
 		return builder.getBoundingBox();
 
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.Bounded3#boundingSphere()
 	 */
 	public Sphere boundingSphere() {
 		return Sphere.smallestContaining(this.vertices);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#generateRandomSurfacePoint(int, ca.eandb.jmist.framework.ShadingContext)
 	 */
 	@Override
 	public void generateRandomSurfacePoint(int index, ShadingContext context) {
 		Random random = context.getRandom();
 		Point3 p = faces.get(index).generateRandomSurfacePoint(random);
 		Intersection x = super.newSurfacePoint(p, index).setPrimitiveIndex(index);
 		x.prepareShadingContext(context);
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#getSurfaceArea(int)
 	 */
 	@Override
 	public double getSurfaceArea(int index) {
 		return faces.get(index).getSurfaceArea();
 	}
 
 	/* (non-Javadoc)
 	 * @see ca.eandb.jmist.framework.geometry.AbstractGeometry#getTextureCoordinates(ca.eandb.jmist.framework.geometry.AbstractGeometry.GeometryIntersection)
 	 */
 	@Override
 	protected Point2 getTextureCoordinates(GeometryIntersection x) {
 		Face face = faces.get(x.getTag());
 		return face.getUV(x.getPosition());
 	}
 
 	/**
 	 * A face of a polyhedron.
 	 * @author Brad Kimmel
 	 */
 	private final class Face implements Bounded3, Serializable {
 
 		/**
 		 * Serialization version ID.
 		 */
 		private static final long serialVersionUID = 5733963094194933946L;
 
 		/**
 		 * Creates a new <code>Face</code>.
 		 * @param indices The indices into {@link PolyhedronGeometry#vertices}
 		 * 		of the vertices of the face.
 		 */
 		public Face(int[] indices, int[] texIndices, int[] normalIndices) {
 			this.indices = indices;
 			this.texIndices = texIndices;
 			this.normalIndices = normalIndices;
 			this.plane = this.computePlane();
 		}
 
 		/**
 		 * Computes the <code>Plane3</code> in which this <code>Face</code>
 		 * lies.
 		 * @return The <code>Plane3</code> in which this <code>Face</code>
 		 * 		lies.
 		 */
 		public Plane3 computePlane() {
 			return Plane3.throughPoint(vertices.get(indices[0]), this.computeFaceNormal());
 		}
 
 		/**
 		 * Computes the <code>Vector3</code> perpendicular to this
 		 * <code>Face</code>.
 		 * @return The <code>Vector3</code> perpendicular to this
 		 * 		<code>Face</code>.
 		 */
 		public Vector3 computeFaceNormal() {
 
 			Vector3	u = vertices.get(indices[0]).vectorTo(vertices.get(indices[1]));
 			Vector3	v = vertices.get(indices[0]).vectorTo(vertices.get(indices[indices.length - 1]));
 
 			return u.cross(v).unit();
 
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.Bounded3#boundingBox()
 		 */
 		public Box3 boundingBox() {
 			BoundingBoxBuilder3 builder = new BoundingBoxBuilder3();
 			for (int i = 0; i < indices.length; i++) {
 				builder.add(vertices.get(indices[i]));
 			}
 			return builder.getBoundingBox();
 		}
 
 		/* (non-Javadoc)
 		 * @see ca.eandb.jmist.framework.Bounded3#boundingSphere()
 		 */
 		public Sphere boundingSphere() {
 			List<Point3> verts = new ArrayList<Point3>(indices.length);
 			for (int i = 0; i < indices.length; i++) {
 				verts.add(vertices.get(indices[i]));
 			}
 			return Sphere.smallestContaining(verts);
 		}
 
 		public double getSurfaceArea() {
 			Vector3 n = computeFaceNormal();
			Vector3 v0 = vertices.get(indices[indices.length - 1]).vectorFromOrigin();
 			Vector3 v1;
 			Vector3 r = Vector3.ZERO;
			for (int i = 0; i < indices.length; i++) {
 				v1 = vertices.get(indices[i]).vectorFromOrigin();
 				r = r.plus(v0.cross(v1));
 				v0 = v1;
 			}
			return 0.5 * Math.abs(n.dot(r));
 		}
 
 		public Point3 generateRandomSurfacePoint(Random random) {
 			decompose();
 			int tri = 3 * rnd.next(random);
 			Point3 a = vertices.get(decomp[tri]);
 			Point3 b = vertices.get(decomp[tri + 1]);
 			Point3 c = vertices.get(decomp[tri + 2]);
 			return RandomUtil.uniformOnTriangle(a, b, c, random);
 		}
 
 		private Point2 getUV(Point3 p) {
 			if (this.texIndices == null) {
 				return Point2.ORIGIN;
 			}
 
 			decompose();
 
 			Vector3 n = plane.normal();
 			p = plane.project(p);
 
 
 			for (int i = 0; i < decomp.length; i += 3) {
 				Point3 a = vertices.get(decomp[i]);
 				Point3 b = vertices.get(decomp[i + 1]);
 				Point3 c = vertices.get(decomp[i + 2]);
 				Vector3 ab = a.vectorTo(b);
 				Vector3 ac = a.vectorTo(c);
 				Vector3 pa = p.vectorTo(a);
 				Vector3 pb = p.vectorTo(b);
 				Vector3 pc = p.vectorTo(c);
 
 				double area = n.dot(ab.cross(ac));
 				double A = n.dot(pb.cross(pc)) / area;
 				if (A < 0.0) continue;
 				double B = n.dot(pc.cross(pa)) / area;
 				if (B < 0.0) continue;
 				double C = 1.0 - A - B;
 				if (C < 0.0) continue;
 
 				// XXX oops.. i guess decomp shouldn't refer directly to the vertex array.
 				Point2 ta = null, tb = null, tc = null;
 				for (int j = 0; j < indices.length; j++) {
 					if (indices[j] == decomp[i]) {
 						ta = texCoords.get(texIndices[j]);
 					} else if (indices[j] == decomp[i + 1]) {
 						tb = texCoords.get(texIndices[j]);
 					} else if (indices[j] == decomp[i + 2]) {
 						tc = texCoords.get(texIndices[j]);
 					}
 				}
 
 				return new Point2(ta.x() * A + tb.x() * B + tc.x() * C, ta.y() * A + tb.y() * B + tc.y() * C);
 			}
 
 			return Point2.ORIGIN;
 		}
 
 		private synchronized void decompose() {
 			if (decomp != null) {
 				return;
 			}
 			decomp = new int[3 * (indices.length - 2)];
 			double[] weight = new double[indices.length - 2];
 
 			// FIXME This does not work for a general polygon.  It will work
 			// for all convex polygons (or polygons where the lines between
 			// the first vertex and each other vertex are contained inside the
 			// polygon).
 			for (int i = 0; i < indices.length - 2; i++) {
 				decomp[3 * i] = indices[0];
 				decomp[3 * i + 1] = indices[i + 1];
 				decomp[3 * i + 2] = indices[i + 2];
 				weight[i] = GeometryUtil.areaOfTriangle(
 						vertices.get(decomp[3 * i]),
 						vertices.get(decomp[3 * i + 1]),
 						vertices.get(decomp[3 * i + 2]));
 			}
 			rnd = new CategoricalRandom(weight);
 		}
 
 		private int[] decomp = null;
 
 		private CategoricalRandom rnd;
 
 		/** The <code>Plane3</code> in which this face lies. */
 		public Plane3 plane;
 
 		/**
 		 * The indices into {@link PolyhedronGeometry#vertices} of the vertices
 		 * of this face.
 		 */
 		public final int[] indices;
 
 		/**
 		 * The indices into {@link PolyhedronGeometry#texCoords} of the
 		 * texture vertices of this face.
 		 */
 		public final int[] texIndices;
 
 		/**
 		 * The indices into {@link PolyhedronGeometry#normals} of the normals
 		 * for corresponding to the vertices on this face.
 		 */
 		public final int[] normalIndices;
 
 	}
 
 	/** The array of the vertices of this polyhedron. */
 	private final List<Point3> vertices;
 
 	/**
 	 * The array of vertex normals corresponding to the vertices in
 	 * {@link #vertices}.
 	 */
 	private final List<Vector3> normals;
 
 	/** An array of texture coordinates. */
 	private final List<Point2> texCoords;
 
 	/** An array of the <code>Face</code>s of this polyhedron. */
 	private final List<Face> faces = new ArrayList<Face>();
 
 }
