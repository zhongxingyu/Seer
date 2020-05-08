 package mocap.figure;
 
 import java.util.List;
 
 import javax.media.j3d.Group;
 import javax.media.j3d.Node;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import mocap.j3d.Util;
 
 public class Bone {
 	private Point3d offset;
 	
 	private String name;
 	private Bone[] children;
 	private Bone parent;
 	
 	public static final int RZ = 0, RY = 1, RX = 2, 
 			                TX = 3, TY = 4, TZ = 5;
 	
 	private int index;
 	private int dof;
 	
 	private TransformGroup baseTranslate, translate,
 	                       baseRotation, rotation, invBaseRotation;
 	private Transform3D t1 = new Transform3D(),
 						t2 = new Transform3D(),
 						trans = new Transform3D(),
 						transWorld = new Transform3D();
 	private Vector3d posVector = new Vector3d();
 	
 	private Vector3d geomDir;
 	private BoneGeom boneGeom;
 	private JointGeom jointGeom;
 	
 	private double scaleFactor = 1d;
 	
 	public Bone()
 	{
 		baseTranslate = new TransformGroup();
 		baseTranslate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		baseTranslate.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		
 		translate = new TransformGroup();
 		translate.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		baseRotation = new TransformGroup();
 		baseRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		baseRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 		baseRotation.setCapability(TransformGroup.ALLOW_LOCAL_TO_VWORLD_READ);
 		
 		rotation = new TransformGroup();
 		rotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		invBaseRotation = new TransformGroup();
 		invBaseRotation.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		// now link them
 		baseTranslate.addChild(translate);
 		translate.addChild(baseRotation);
 		baseRotation.addChild(rotation);
 		rotation.addChild(invBaseRotation);
 	}
 	
 	public Bone(String name, Bone parent, int dof, int index) {
 		this();
 		
 		this.name = name;
 		this.dof = dof;
 		this.index = index;
 		this.parent = parent;		
 	}
 	
 	public void setChildren(Bone[] children)
 	{
 		this.children = children;
 		for (Bone bone : children)
 			getEndTG().addChild(bone.getBaseTG());
 	}
 	
 	private TransformGroup getEndTG() {
 		// TODO Auto-generated method stub
 		return this.invBaseRotation;
 	}
 
 	public Bone[] getChildren()
 	{
 		return this.children;
 	}
 	
 	public void setParent(Bone parent)
 	{
 		this.parent = parent;
 	}
 	
 	public Bone getParent()
 	{
 		return this.parent;
 	}
 	
 	
 	public void getWorldPosition(Point3d p)
 	{
 		p.set(0,0,0);
 		Util.getFullTransform(baseRotation, transWorld);
 		transWorld.transform(p);
 	}
 	
 	public void setBaseTranslation(Vector3d vec)
 	{
 		Transform3D tf = new Transform3D();
 		tf.setTranslation(vec);
 		baseTranslate.setTransform(tf);
 	}
 	
 	/**
 	 * Sets this bone/joint to the translation and rotation of the given frame.
 	 * Should be called from some animation loop
 	 * 
 	 * @param data
 	 * 			 The data from the line of the current frame
 	 */
 	public void setPose(int frame, float[] data, Point3d offsetTrans)
 	{
 		// only modify if this bone has any DOFs 
 		if (dof > 0)
 		{
			//int offset = this.index;
 			int offset = frame * dof;
 			boolean hasTranslation, hasRotation = false; 
 			
 			// if this bone doesn't have a parent, set default pos vector
 			if (parent == null)
 				posVector.set(0,0,0);
 			
 			// reset translation
 			t1.setIdentity();
 			for (int i = 0; i < dof; i++)
 			{
 				switch(i) {
 				case RZ:
 					t2.rotZ(data[offset + i]);
 					hasRotation = true;
 					break;
 				case RY:
 					t2.rotY(data[offset + i]);
 					hasRotation = true;
 					break;
 				case RX:
 					t2.rotX(data[offset + i]);
 					hasRotation = true;
 					break;
 				case TX:
 					posVector.x = data[offset + i] * scaleFactor;
 					hasTranslation = true;
 					hasRotation = false;
 					break;	
 				case TY:
 					posVector.y = data[offset + i] * scaleFactor;
 					hasTranslation = true;
 					hasRotation = false;
 					break;
 				case TZ:
 					posVector.z = data[offset + i] * scaleFactor;
 					hasTranslation = true;
 					hasRotation = false;
 					break;
 				}
 				
 				if (hasRotation) 
 				{
 					t1.mul(t2);
 					t2.setIdentity();
 				}
 			}
 			
 			// set rotation
 			rotation.setTransform(t1);
 			
			// set translation
			// if this is a root, add the offset
			//if (this.offset != null & this.parent != null)
			//	posVector.add(this.offset);
			
 			// add offset to translation of root
 			if (offsetTrans != null && parent == null) {
 				posVector.add(offsetTrans);
 				hasTranslation = true;
 			}
 			
 			if (this.parent == null)
 			{
 				trans.setIdentity();
 				trans.setTranslation(posVector);
 				translate.setTransform(trans);
 			}
 		}
 	}
 	
 	public void reset()
 	{
 		t1.setIdentity();
 		rotation.setTransform(t1);
 		for (Bone b : this.children)
 			b.reset();
 	}
 	
 	public void geometry(Vector3d dir, double scaleFactor)
 	{
 		this.geomDir = new Vector3d(dir);
 		// new bone geom
 		this.boneGeom = new BoneGeom(invBaseRotation, dir);
 
 		// new join geom
 		this.jointGeom = new JointGeom(baseRotation, (float) (0.4f * scaleFactor));	
 	}
 
 	public Vector3d getOffset() {
 		return null;
 	}
 
 	public int getDOF() {
 		return this.dof;
 	}
 
 	public int getIndex() {
 		return this.index;
 	}
 
 	public void setIndex(int i) {
 		this.index = i;
 	}
 
 	public TransformGroup getBaseTG() {
 		return this.baseTranslate;
 	}
 
 	public void collectBones(List<Bone> ls) {
 		// TODO Auto-generated method stub
 		ls.add(this);
 		for (Bone child : children)
 		{
 			child.collectBones(ls);
 		}
 	}
 	
 	public void scale(double factor) {
 		scaleFactor = factor;
		baseTranslate.getTransform(t1);
 		t1.get(posVector);
 		posVector.scale(scaleFactor);
 		t1.set(posVector);
		baseTranslate.setTransform(t1);
 		for (Bone b : children) {
 			b.scale(scaleFactor);
 		}
 	}
 
 	public double getScale() {
 		return scaleFactor;
 	}
 }
