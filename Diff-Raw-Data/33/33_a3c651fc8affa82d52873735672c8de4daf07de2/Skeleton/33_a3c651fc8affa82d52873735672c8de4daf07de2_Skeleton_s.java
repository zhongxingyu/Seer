 package graphics.compatibility.skeleton;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lwjgl.util.vector.Matrix4f;
 
 public class Skeleton {
 	
 	public Map<String, Bone> bones;
 	
 	public Skeleton(){
 		this.bones = new HashMap<String,Bone>();
 	}
 	
 	public void addRoot(String childName, Matrix4f offset){
		this.bones.put(childName, new Bone(offset));
 	}
 	
 	public void addChild(String parentName, String childName, Matrix4f offset){
		Bone bone = this.bones.get(parentName).addChild(offset);
 		
 		this.bones.put(childName, bone);
 	}
 	
 	
 
 }
