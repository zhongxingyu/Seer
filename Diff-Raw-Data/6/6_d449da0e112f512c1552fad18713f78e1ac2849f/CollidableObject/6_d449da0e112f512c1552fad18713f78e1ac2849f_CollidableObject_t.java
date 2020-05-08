 package common;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.*;
 
 import javax.media.j3d.*;
 import javax.vecmath.*;
 
 import com.sun.j3d.utils.geometry.GeometryInfo;
 import com.sun.j3d.utils.geometry.Primitive;
 
 @SuppressWarnings("restriction")
 public abstract class CollidableObject implements Serializable {
 	private static final long serialVersionUID = 3667108226485766929L;
 	protected float inverseMass;
 	// The center of mass in the local coordinate system
 	protected Vector3f centerOfMass;
 	protected Vector3f position, previousPosition;
 	protected Vector3f velocity, previousVelocity;
 	protected Vector3f forceAccumulator;
 	protected Quat4f orientation;
 	protected Vector3f angularVelocity;
 	protected Vector3f previousRotationalVelocity;
 	protected Vector3f torqueAccumulator;
 	protected Matrix3f inverseInertiaTensor;
 	protected float coefficientOfRestitution;
 	protected float penetrationCorrection;
 	protected float dynamicFriction;
 	protected float rotationalFriction;
 	transient protected BranchGroup BG;
 	transient protected TransformGroup TG;
 	transient protected Node node;
 	transient private ArrayList<Vector3f> vertexCache;
 	transient private ArrayList<CollisionDetector.Triangle> triangleCache;
 	transient private Bounds boundsCache;
 	// The inverse inertia tensor in world coordinates
 	transient private Matrix3f inverseInertiaTensorCache;
 	
 	public CollidableObject() {
 		this(1);
 	}
 
 	public CollidableObject(float mass) {
 		if (mass <= 0)
 			throw new IllegalArgumentException();
 		inverseMass = 1 / mass;
 		centerOfMass = new Vector3f();
 		position = new Vector3f();
 		previousPosition = new Vector3f();
 		velocity = new Vector3f();
 		previousVelocity = new Vector3f();
 		forceAccumulator = new Vector3f();
 		orientation = new Quat4f(0, 0, 0, 1);
 		angularVelocity = new Vector3f();
 		previousRotationalVelocity = new Vector3f();
 		torqueAccumulator = new Vector3f();
 		inverseInertiaTensor = new Matrix3f();
 		coefficientOfRestitution = 0.75f;
 		penetrationCorrection = 1.05f;
 		dynamicFriction = 0.02f;
 		rotationalFriction = 0.05f;
 		TG = new TransformGroup();
 		TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		BG = new BranchGroup();
 		BG.setCapability(BranchGroup.ALLOW_DETACH);
 		BG.addChild(TG);
 	}
 
 	protected void setShape(Node node) {
 		this.node = node;
 		TG.addChild(node);
 //		TG.addChild(CollisionDetector.createShape(CollisionDetector.triangularize(node)));
 	}
 
 	public Group getGroup() {
 		return BG;
 	}
 
 	public void detach() {
 		BG.detach();
 	}
 
 	public void updateState(float duration) {
 		previousPosition.set(position);
 		previousVelocity.set(velocity);
 		// The force vector now becomes the acceleration vector.
 		forceAccumulator.scale(inverseMass);
 		position.scaleAdd(duration, velocity, position);
 		position.scaleAdd(duration * duration / 2, forceAccumulator, position);
 		velocity.scaleAdd(duration, forceAccumulator, velocity);
 		// The force vector is cleared.
 		forceAccumulator.set(0, 0, 0);
 		
 		angularVelocity.scaleAdd(duration, torqueAccumulator, angularVelocity);
 		torqueAccumulator.set(0, 0, 0);
 		UnQuat4f tmp = new UnQuat4f(angularVelocity.x, angularVelocity.y, angularVelocity.z, 0);
 		tmp.scale(duration / 2);
 		tmp.mul(orientation);
 		orientation.add(tmp);
 		orientation.normalize();
 	}
 
 	protected void updateTransformGroup() {
 		Vector3f com = new Vector3f(-centerOfMass.x, -centerOfMass.y, -centerOfMass.z);
 		Transform3D tmp = new Transform3D();
 		tmp.setTranslation(com);
 		Transform3D tmp2 = new Transform3D();
 		tmp2.setRotation(orientation);
 		com.negate();
 		com.add(position);
 		tmp2.setTranslation(com);
 		tmp2.mul(tmp);
 		TG.setTransform(tmp2);
 		clearCaches();
 	}
 
 	public ArrayList<Vector3f> getVertices() {
 		if (vertexCache == null)
 			vertexCache = CollisionDetector.extractVertices(node);
 		return vertexCache;
 	}
 	
 	protected ArrayList<CollisionDetector.Triangle> getCollisionTriangles() {
 		if (triangleCache == null)
 			triangleCache = CollisionDetector.triangularize(node);
 		return triangleCache;
 	}
 
 	protected Bounds getBounds() {
 		if (boundsCache == null) {
 			boundsCache = node.getBounds();
 			Transform3D tmp = new Transform3D();
 			node.getLocalToVworld(tmp);
 			boundsCache.transform(tmp);
 		}
 		return boundsCache;
 	}
 
 	protected Matrix3f getInverseInertiaTensor() {
 		if (inverseInertiaTensorCache == null) {
 			inverseInertiaTensorCache = new Matrix3f();
 			inverseInertiaTensorCache.set(orientation);
 			Matrix3f tmp = new Matrix3f(inverseInertiaTensor);
 			Matrix3f tmp2 = new Matrix3f(inverseInertiaTensorCache);
 			tmp2.invert();
 			tmp.mul(tmp2);
 			inverseInertiaTensorCache.mul(tmp);
 		}
 		return inverseInertiaTensorCache;
 	}
 
 	protected void clearCaches() {
 		vertexCache = null;
 		triangleCache = null;
 		boundsCache = null;
 		inverseInertiaTensorCache = null;
 	}
 
 	public void resolveCollisions(CollidableObject other) {
 		ArrayList<CollisionInfo> collisions = CollisionDetector.calculateCollisions(this, other);
 		if (collisions.isEmpty())
 			return;
 
 		CollisionInfo finalCollision = null;
 		float max = Float.NEGATIVE_INFINITY;
 		int count = 0;
 		for (CollisionInfo collision : collisions) {
 			Vector3f thisRelativeContactPosition = new Vector3f();
 			thisRelativeContactPosition.scaleAdd(-1, position, collision.contactPoint);
 			thisRelativeContactPosition.scaleAdd(-1, centerOfMass, thisRelativeContactPosition);
 			Vector3f thisContactVelocity = new Vector3f();
 			thisContactVelocity.cross(angularVelocity, thisRelativeContactPosition);
 			thisContactVelocity.add(previousVelocity);
 			Vector3f otherRelativeContactPosition = new Vector3f();
 			otherRelativeContactPosition.scaleAdd(-1, other.position, collision.contactPoint);
 			otherRelativeContactPosition.scaleAdd(-1, other.centerOfMass, otherRelativeContactPosition);
 			Vector3f otherContactVelocity = new Vector3f();
 			otherContactVelocity.cross(other.angularVelocity, otherRelativeContactPosition);
 			otherContactVelocity.add(other.previousVelocity);
 			float speed = collision.contactNormal.dot(thisContactVelocity) - collision.contactNormal.dot(otherContactVelocity);
 			if (speed > 0)
 				if (speed > max + CollisionDetector.EPSILON) {
 					finalCollision = collision;
 					max = speed;
 					count = 1;
 				} else if (speed >= max - CollisionDetector.EPSILON) {
 					finalCollision.contactPoint.add(collision.contactPoint);
 					finalCollision.penetration += collision.penetration;
 					count++;
 				}
 		}
 		if (finalCollision != null) {
 			finalCollision.contactPoint.scale(1f / count);
 			finalCollision.penetration /= count;
 			resolveCollision(other, finalCollision);
 			updateTransformGroup();
 			other.updateTransformGroup();
 		}
 	}
 
 	public void resolveCollision(CollidableObject other, CollisionInfo ci) {
 		if (ci.penetration <= 0)
 			return;
 		
 		Vector3f thisRelativeContactPosition = new Vector3f();
 		thisRelativeContactPosition.scaleAdd(-1, position, ci.contactPoint);
 		thisRelativeContactPosition.scaleAdd(-1, centerOfMass, thisRelativeContactPosition);
 		
 		Vector3f otherRelativeContactPosition = new Vector3f();
 		otherRelativeContactPosition.scaleAdd(-1, other.position, ci.contactPoint);
 		otherRelativeContactPosition.scaleAdd(-1, other.centerOfMass, otherRelativeContactPosition);
 		
 		Vector3f thisContactVelocity = new Vector3f();
 		thisContactVelocity.cross(angularVelocity, thisRelativeContactPosition);
 		thisContactVelocity.add(previousVelocity);
 		
 		Vector3f otherContactVelocity = new Vector3f();
 		otherContactVelocity.cross(other.angularVelocity, otherRelativeContactPosition);
 		otherContactVelocity.add(other.previousVelocity);
 		
 		float initialClosingSpeed = ci.contactNormal.dot(thisContactVelocity) - ci.contactNormal.dot(otherContactVelocity);
 		float finalClosingSpeed = -initialClosingSpeed * coefficientOfRestitution;
 		float deltaClosingSpeed = finalClosingSpeed - initialClosingSpeed;
 		float totalInverseMass = inverseMass + other.inverseMass;
 		if (totalInverseMass == 0)
 			return;
 		
 		/* Dynamic Friction */
 		if (dynamicFriction > 0) {
 			Vector3f acceleration = new Vector3f();
 			Vector3f perpVelocity = new Vector3f();
 			float contactSpeed = ci.contactNormal.dot(velocity) - ci.contactNormal.dot(other.velocity);
 			
 			perpVelocity.scaleAdd(-contactSpeed, ci.contactNormal, previousVelocity);
 			if (perpVelocity.length() > 0) {
 				perpVelocity.normalize();
 				acceleration.scaleAdd(-1, previousVelocity, velocity);
 				velocity.scaleAdd(dynamicFriction * acceleration.dot(ci.contactNormal), perpVelocity, velocity);
 			}
 			
 			perpVelocity.scaleAdd(contactSpeed, ci.contactNormal, other.previousVelocity);
 			if (perpVelocity.length() > 0) {
 				perpVelocity.normalize();
 				acceleration.scaleAdd(-1, other.previousVelocity, other.velocity);
 				other.velocity.scaleAdd(dynamicFriction * acceleration.dot(ci.contactNormal), perpVelocity, other.velocity);
 			}
 		}
 		
 		Vector3f thisMovementUnit = new Vector3f();
 		thisMovementUnit.cross(thisRelativeContactPosition, ci.contactNormal);
 		getInverseInertiaTensor().transform(thisMovementUnit);
 		Vector3f thisAngularVelocityUnit = new Vector3f();
 		thisAngularVelocityUnit.cross(thisMovementUnit, thisRelativeContactPosition);
 		totalInverseMass += thisAngularVelocityUnit.dot(ci.contactNormal);
 		
 		Vector3f otherMovementUnit = new Vector3f();
 		otherMovementUnit.cross(otherRelativeContactPosition, ci.contactNormal);
 		other.getInverseInertiaTensor().transform(otherMovementUnit);
 		Vector3f otherAngularVelocityUnit = new Vector3f();
 		otherAngularVelocityUnit.cross(otherMovementUnit, otherRelativeContactPosition);
 		totalInverseMass += otherAngularVelocityUnit.dot(ci.contactNormal);
 
 		Vector3f impulse = new Vector3f(ci.contactNormal);
 		impulse.scale(deltaClosingSpeed / totalInverseMass);
 	
 		velocity.scaleAdd(inverseMass, impulse, velocity);
 		Vector3f tmp = new Vector3f();
 		tmp.cross(thisRelativeContactPosition, impulse);
 		getInverseInertiaTensor().transform(tmp);
 		angularVelocity.add(tmp);
 		position.scaleAdd(-ci.penetration * penetrationCorrection * inverseMass / totalInverseMass, ci.contactNormal, position);
 		thisMovementUnit.scale(-ci.penetration * penetrationCorrection / totalInverseMass);
 		UnQuat4f tmp2 = new UnQuat4f(thisMovementUnit.x, thisMovementUnit.y, thisMovementUnit.z, 0);
 		tmp2.scale(0.5f);
 		tmp2.mul(orientation);
 		orientation.add(tmp2);
 		orientation.normalize();
 		
 		impulse.negate();
 		other.velocity.scaleAdd(other.inverseMass, impulse, other.velocity);
 		tmp.cross(otherRelativeContactPosition, impulse);
 		other.getInverseInertiaTensor().transform(tmp);
 		other.angularVelocity.add(tmp);
 		other.position.scaleAdd(ci.penetration * penetrationCorrection * other.inverseMass / totalInverseMass, ci.contactNormal, other.position);
 		otherMovementUnit.scale(ci.penetration * penetrationCorrection / totalInverseMass);
 		tmp2.set(otherMovementUnit.x, otherMovementUnit.y, otherMovementUnit.z, 0);
 		tmp2.scale(0.5f);
 		tmp2.mul(other.orientation);
 		other.orientation.add(tmp2);
 		other.orientation.normalize();
 		
 		if (rotationalFriction > 0) {
 			/* Rotational Friction */
 			Vector3f w = new Vector3f();
 			
 			/*float radius = thisRelativeContactPosition.length();
 			w.cross(angularVelocity, ci.contactNormal);
 			velocity.scaleAdd(-1, previousRotationalVelocity, velocity);
 			previousRotationalVelocity.scale(radius, w);
 			velocity.scaleAdd(radius, w, velocity);
 			w.cross(previousRotationalVelocity, ci.contactNormal);
 			angularVelocity.scaleAdd(-0.5f * w.dot(angularVelocity), w, angularVelocity);
 			
 			radius = otherRelativeContactPosition.length();
 			w.cross(other.angularVelocity, ci.contactNormal);
 			other.velocity.scaleAdd(-1, other.previousRotationalVelocity, other.velocity);
 			other.previousRotationalVelocity.scale(radius, w);
 			other.velocity.scaleAdd(radius , w, other.velocity);
 			w.cross(other.previousRotationalVelocity, ci.contactNormal);
 			other.angularVelocity.scaleAdd(-0.5f * w.dot(other.angularVelocity), w, other.angularVelocity);
 			*/
 			
 			angularVelocity.scaleAdd(-rotationalFriction * ci.contactNormal.dot(angularVelocity), ci.contactNormal, angularVelocity);
 			other.angularVelocity.scaleAdd(-rotationalFriction * ci.contactNormal.dot(other.angularVelocity), ci.contactNormal, other.angularVelocity);
 			
 		}
 	}
 
 private static final int NODE_TYPE_BRANCH = 1;
 	private static final int NODE_TYPE_TRANSFORM = 2;
 	private static final int NODE_TYPE_PRIMITIVE = 3;
 	private static final int NODE_TYPE_SHAPE = 4;
 
 	private void writeObject(ObjectOutputStream out) throws IOException {
 		out.defaultWriteObject();
 		writeObject(out, node);
 	}
 
 	private static void writeObject(ObjectOutputStream out, Node node) throws IOException {
 		if (node instanceof BranchGroup) {
 			out.writeInt(NODE_TYPE_BRANCH);
 			out.writeInt(((BranchGroup) node).numChildren());
 			for (int i = 0; i < ((BranchGroup) node).numChildren(); i++) {
 				Node childNode = ((BranchGroup) node).getChild(i);
 				writeObject(out, childNode);
 			}
 		} else if (node instanceof TransformGroup) {
 			out.writeInt(NODE_TYPE_TRANSFORM);
 			Transform3D tgT = new Transform3D();
 			Matrix4f matrix = new Matrix4f();
 			((TransformGroup) node).getTransform(tgT);
 			tgT.get(matrix);
 			out.writeObject(matrix);
 			out.writeInt(((TransformGroup) node).numChildren());
 			for (int i = 0; i < ((TransformGroup) node).numChildren(); i++) {
 				Node childNode = ((TransformGroup) node).getChild(i);
 				writeObject(out, childNode);
 			}
 		} else if (node instanceof Primitive) {
 			out.writeInt(NODE_TYPE_PRIMITIVE);
 			Primitive prim = (Primitive)node;
 			int index = 0;
 			Shape3D shape;
 			out.writeInt(prim.numChildren());
 			while ((shape = prim.getShape(index++)) != null) {
 				Appearance appearance = shape.getAppearance();
 				if (appearance != null) {
 					out.writeBoolean(true);
 					writeObject(out, appearance);
 				} else
 					out.writeBoolean(false);
 				out.writeInt(shape.numGeometries());
 				for (int i = 0; i < shape.numGeometries(); i++)
 					writeObject(out, shape.getGeometry(i));
 			}
 			
 		} else if (node instanceof Shape3D) {
 			out.writeInt(NODE_TYPE_SHAPE);
 			Shape3D shape = (Shape3D) node;
 			Appearance appearance = shape.getAppearance();
 			if (appearance != null) {
 				out.writeBoolean(true);
 				writeObject(out, appearance);
 			} else
 				out.writeBoolean(false);
 			out.writeInt(shape.numGeometries());
 			for (int i = 0; i < shape.numGeometries(); i++)
 				writeObject(out, shape.getGeometry(i));
 			
 		} else 
 			throw new IllegalArgumentException("Illegal node type for serialization");
 	}
 
 	private static void writeObject(ObjectOutputStream out, Geometry geometry) throws IOException {
 		GeometryInfo gi = new GeometryInfo((GeometryArray)geometry);
 		gi.convertToIndexedTriangles();
 		geometry = gi.getIndexedGeometryArray();
 		IndexedTriangleArray trueGeometry = (IndexedTriangleArray)geometry;
 		
 		int format = trueGeometry.getVertexFormat() & (IndexedTriangleArray.COORDINATES | IndexedTriangleArray.NORMALS);
 		out.writeInt(format);
 		Point3f vertices[] = new Point3f[trueGeometry.getValidVertexCount()];
 		for (int i = 0; i < vertices.length; i++) {
 			vertices[i] = new Point3f();
 			trueGeometry.getCoordinate(i, vertices[i]);
 		}
 		out.writeObject(vertices);
 		int indices[] = new int[trueGeometry.getValidIndexCount()];
 		trueGeometry.getCoordinateIndices(0, indices);
 		out.writeObject(indices);
 		
 		if ((format & IndexedTriangleArray.NORMALS) != 0) {
 			Vector3f normals[] = new Vector3f[trueGeometry.getValidVertexCount()];
 			for (int i = 0; i < normals.length; i++) {
 				normals[i] = new Vector3f();
 				trueGeometry.getNormal(i, normals[i]);
 			}
 			out.writeObject(normals);
 			trueGeometry.getNormalIndices(0, indices);
 			out.writeObject(indices);
 		}
 	}
 
 	private static void writeObject(ObjectOutputStream out, Appearance appearance) throws IOException {
 		Material material = appearance.getMaterial();
 		if (material != null) {
 			out.writeBoolean(true);
 			Color3f color = new Color3f();
 			material.getAmbientColor(color);
 			out.writeObject(color);
 			color = new Color3f();
 			material.getDiffuseColor(color);
 			out.writeObject(color);
 			color = new Color3f();
 			material.getSpecularColor(color);
 			out.writeObject(color);
 			color = new Color3f();
 			material.getEmissiveColor(color);
 			out.writeObject(color);
 			out.writeFloat(material.getShininess());
 			out.writeInt(material.getColorTarget());
 		} else
 			out.writeBoolean(false);
 	}
 
 	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		in.defaultReadObject();
 		TG = new TransformGroup();
 		TG.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		BG = new BranchGroup();
 		BG.setCapability(BranchGroup.ALLOW_DETACH);
 		BG.addChild(TG);
 		setShape(readNode(in));
 	}
 
 	private static Node readNode(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		int type = in.readInt();
 		switch (type) {
 		case NODE_TYPE_BRANCH:
 			BranchGroup bgroup = new BranchGroup();
 			int numTGChildren = in.readInt();
 			for (int i = 0; i < numTGChildren; i++) {
 				bgroup.addChild(readNode(in));
 			}
 			return bgroup;
 		case NODE_TYPE_TRANSFORM:
 			TransformGroup tgroup = new TransformGroup();
 			Matrix4f matrix = (Matrix4f) in.readObject();
 			Transform3D tgT = new Transform3D(matrix);
 			tgroup.setTransform(tgT);
 			int numChildren = in.readInt();
 			for (int i = 0; i < numChildren; i++) {
 				tgroup.addChild(readNode(in));
 			}
 			return tgroup;
 		case NODE_TYPE_PRIMITIVE:
 			BranchGroup bg = new BranchGroup();
 			int shapes = in.readInt();
 			for (int i = 0; i < shapes; i++) {
 				Shape3D shape = new Shape3D();
 				shape.removeAllGeometries();
 				if (in.readBoolean())
 					shape.setAppearance(readAppearance(in));
 				int geometries = in.readInt();
 				for (int j = 0; j < geometries; j++)
 					shape.addGeometry(readGeometry(in));
 				bg.addChild(shape);			
 			}
 			return bg;
 		case NODE_TYPE_SHAPE:
 			BranchGroup shapeBG = new BranchGroup();
 			Shape3D shape = new Shape3D();
 			shape.removeAllGeometries();
 			boolean hasAppearance = in.readBoolean();
 			Appearance shapeApp = new Appearance(); 
			if (hasAppearance) {
 				shapeApp = readAppearance(in);
 			}
 			int geometries = in.readInt();
 			for (int i = 0; i < geometries; i++)
 				shape.addGeometry(readGeometry(in));
			if (hasAppearance) {
				shape.setAppearance(shapeApp);
			}
 			shapeBG.addChild(shape);
 			return shapeBG;
 		default:
 			throw new IllegalArgumentException("Illegal node type for serialization");
 		}
 	}
 
 	private static GeometryArray readGeometry(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		int format = in.readInt();
 		Point3f vertices[] = (Point3f[])in.readObject();
 		int indices[] = (int[])in.readObject();
 		IndexedTriangleArray geometry = new IndexedTriangleArray(vertices.length, format, indices.length);
 		geometry.setCoordinates(0, vertices);
 		geometry.setCoordinateIndices(0, indices);
 
 		if ((format & IndexedTriangleArray.NORMALS) != 0) {
 			Vector3f normals[] = (Vector3f[])in.readObject();
 			indices = (int[])in.readObject();
 			geometry.setNormals(0, normals);
 			geometry.setNormalIndices(0, indices);
 		}
 		
 		return geometry;
 	}
 
 	private static Appearance readAppearance(ObjectInputStream in) throws IOException, ClassNotFoundException {
 		Appearance appearance = new Appearance();
 		if (in.readBoolean()) {
 			Material material = new Material();
 			material.setAmbientColor((Color3f)in.readObject());
 			material.setDiffuseColor((Color3f)in.readObject());
 			material.setSpecularColor((Color3f)in.readObject());
 			material.setEmissiveColor((Color3f)in.readObject());
 			material.setShininess(in.readFloat());
 			material.setColorTarget(in.readInt());
 			appearance.setMaterial(material);
 		}
 		return appearance;
 	}
 }
