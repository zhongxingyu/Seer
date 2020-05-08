 package game.entity;
 
 import game.bound.Bound;
 import game.bound.BoundingBox;
 import game.bound.BoundingSphere;
 import game.entity.Entity;
 import game.modelloader.Content;
 import game.modelloader.WavefrontLoader;
 import initial3d.engine.Color;
 import initial3d.engine.Material;
 import initial3d.engine.Mesh;
 import initial3d.engine.MeshContext;
 import initial3d.engine.Vec3;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import common.Timer;
 import comp261.modelview.MeshLoader;
 
 public class PlayerEntity extends Entity {
 	
 	private Bound bound;
 	
 	public PlayerEntity(Vec3 _pos)
 	{
 		position = _pos;
 		bound = new BoundingSphere();
 		
 		this.addMeshContexts(this.getBall());
 	}
 	
 	public Bound getBound(){
 		return bound;
 	}
 	
 	// TODO: Work-in-progress
 	public List<MeshContext> getBall()
 	{
 		Material mat = new Material(Color.RED, new Color(0.6f, 0.1f, 0.1f), new Color(0.3f, 0.3f, 0.3f), new Color(0f, 0f, 0f), 1f, 1f);		
 		Mesh m = Content.loadContent("sphere.obj");
 		MeshContext mc = new MeshContext(m, mat, this);
		mc.setScale(0.25);
 		List<MeshContext> meshes = new ArrayList<MeshContext>();
 		meshes.add(mc);
 		
 		return meshes;
 	}
 
 	@Override
 	public void poke() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void moveTo(Vec3 add) {
 		this.position = add;
 	}
 }
