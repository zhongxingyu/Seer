 package mygame;
 
 import com.jme3.asset.AssetManager;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.List;
 
 public class Game {
     AssetManager assetManager;
     Node rootNode;
     Camera cam;
     
     static Geometry gamePlane;
     Dictionary<String, Material> materials;
     List<Plupp> plupps;
     
     public Material loadMaterial(String path) {
         if(materials.get(path) != null) { return materials.get(path); }
         return new Material(
             assetManager, path);
     }
     
     public Game() {
     }
     
     public void applyShockWave(Vector3f vec) {
         for(Plupp p : plupps) {
             Vector3f ppos = p.geometry.geometry.getLocalTranslation();
             float distance = ppos.distance(vec);
             System.out.println(distance);
         }	
     }
     
     public Plupp createPlupp() {
         Plupp p = new Plupp();
         plupps.add(p);
         return p;
     }
     
     public void Start() {
         
         plupps = new ArrayList();
         
         Material mat = new Material(
             assetManager,
             "Common/MatDefs/Misc/Unshaded.j3md"
         );
         
         mat.setTexture("ColorMap",
             assetManager.loadTexture("Textures/Penguins.jpg")
         );
         
         rootNode.attachChild(
             (new BoxGeomWrapper())
                 .setGeometry(Helper.createCube(10,0.1f,10,"alfred"))
                 .setMaterial(mat)
                 .getGeometry()
         );
         
         Plupp p;
         
         rootNode.attachChild(
             ((p = createPlupp()).geometry = new BoxGeomWrapper()
                 .setGeometry(Helper.createCube(1,1,1,"plupp"))
                 .setMaterial(mat))
             .getGeometry()
         );
         
         
     }
     
     float t = 0;
     
     public void Update(float tpf) {
         t += tpf;
         plupps.get(0).geometry.geometry.setLocalTranslation(
             new Vector3f(0,0,1).mult(5));
         
         cam.lookAt(
            new Vector3f(0,0,0),
            new Vector3f(0,0,-1));
     }
 }
