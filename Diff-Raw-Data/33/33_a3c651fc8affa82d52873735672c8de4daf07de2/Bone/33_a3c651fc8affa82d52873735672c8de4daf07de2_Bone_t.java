 package graphics.compatibility.skeleton;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.util.vector.Matrix4f;
 
 public class Bone {
 	
	protected String name;
 	protected Matrix4f transform;
 	protected Matrix4f offset;
 	protected Bone parent;
 	protected List<Bone> children;
 	
 	
 	//constructor assumes that a root bone is made
	public Bone(String name, Matrix4f offset){
 		this.children = new ArrayList<Bone>();
 		
		this.name = name;
 		this.offset = offset;
 		this.transform = new Matrix4f();
 		transform.setIdentity();
 	}
 	
	private Bone(Bone parent, String name, Matrix4f offset){
		this(name, offset);
 		this.parent = parent;
 	}
 	
	public Bone addChild(String name, Matrix4f offset){
		Bone bone = new Bone(this, name, offset);
 		this.children.add(bone);
 		
 		return bone;
 	}
 
 }
