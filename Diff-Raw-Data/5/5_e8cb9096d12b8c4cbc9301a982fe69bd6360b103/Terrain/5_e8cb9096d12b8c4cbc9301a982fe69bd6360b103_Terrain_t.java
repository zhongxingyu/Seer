 package engine.terrain;
 
 import engine.importing.Parser;
 import engine.importing.XGL_Parser;
 
 import javax.vecmath.Vector3f;
 
 import com.bulletphysics.collision.dispatch.CollisionFlags;
 import com.bulletphysics.collision.shapes.BoxShape;
 
 import engine.entity.Entity;
 import engine.entity.EntityList;
 
 public class Terrain {
 	private EntityList objectList;
 	private DynamicMatrix terrain;
 	
 	public Terrain(EntityList objectList) {
 		this.objectList = objectList;
 	}
 	public void createTerrain(int land_size) {
 		Parser p = new XGL_Parser();
 		try {
 			p.readFile("resources/models/misc/singlebox.xgl");
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//Model box = p.createModel();
 		BoxShape boxShape = new BoxShape(new Vector3f(0.5f, 0.5f, 0.5f));
 		Entity ent;
 		
 		terrain = new DynamicMatrix();
 		for(int i=0; i<land_size;i++)
 			terrain.expand();
 		
 		for(int i=0; i<terrain.getSize();i++) {
 			ent = new Entity(0.0f, boxShape, true);
 			ent.setModel(p.createModel());
 			ent.setPosition(terrain.get(i));
 			ent.setCollisionFlags(CollisionFlags.CUSTOM_MATERIAL_CALLBACK);
 			
			objectList.addEntity(ent);
 		}
 	}
 	
 	public String toString() {
 		String text = new String();
 		for(int i=0;i<terrain.getColumns();i++) {
 			for(int j=0;j<terrain.getColumns();j++) {
 				text+=terrain.get(i, j)+",";
 			}
 			text+="\n";
 		}
 		return null;
 		
 	}
 }
