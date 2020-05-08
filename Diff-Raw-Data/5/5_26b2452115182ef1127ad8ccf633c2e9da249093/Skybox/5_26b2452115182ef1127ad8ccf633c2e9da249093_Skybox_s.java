 package cge.zeppelin;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import javax.media.opengl.GL2GL3;
 
 import cge.zeppelin.util.Helper;
 import de.bht.jvr.collada14.loader.ColladaLoader;
 import de.bht.jvr.core.Finder;
 import de.bht.jvr.core.GroupNode;
 import de.bht.jvr.core.SceneNode;
 import de.bht.jvr.core.Shader;
 import de.bht.jvr.core.ShaderMaterial;
 import de.bht.jvr.core.ShaderProgram;
 import de.bht.jvr.core.ShapeNode;
 import de.bht.jvr.core.Texture2D;
 import de.bht.jvr.core.Transform;
 
 public class Skybox extends Entity {
     private Texture2D bk, dn, ft, lf, rt, up;
 	private World world;
 	private SceneNode planeBk, planeDn, planeFt, planeLf, planeRt, planeUp;
 	private ShapeNode shapeNodeBk, shapeNodeDn, shapeNodeFt, shapeNodeLf, shapeNodeRt, shapeNodeUp;
    
     public Skybox(World w, GroupNode n) {
     	node = n;
     	world = w;
 
     	try {
 			loadFiles();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
     	float boxSize = 500;
 
 		planeBk.setTransform(Transform.translate(0, 0, boxSize).mul(Transform.rotateYDeg(180)).mul(Transform.scale(1000, 1000, 6)));
 		planeDn.setTransform(Transform.translate(0, -1 * boxSize, 0).mul(Transform.rotateYDeg(180)).mul(Transform.rotateXDeg(-90)).mul(Transform.scale(1000, 1000, 6)));
 		planeFt.setTransform(Transform.translate(0, 0, -1 * boxSize).mul(Transform.scale(1000, 1000, 6)));
 		planeRt.setTransform(Transform.translate(boxSize, 0, 0).mul(Transform.rotateYDeg(-90)).mul(Transform.scale(1000, 1000, 6)));
 		planeLf.setTransform(Transform.translate(-1 * boxSize, 0, 0).mul(Transform.rotateYDeg(90)).mul(Transform.scale(1000, 1000, 6)));
		planeUp.setTransform(Transform.translate(0, -1 * boxSize, 0).mul(Transform.rotateYDeg(180)).mul(Transform.rotateXDeg(90)).mul(Transform.scale(1000, 1000, 6)));
 		
 		
 		shapeNodeBk = Finder.find(planeBk, ShapeNode.class, "Plane01_Shape");
 		shapeNodeDn = Finder.find(planeDn, ShapeNode.class, "Plane01_Shape");
 		shapeNodeFt = Finder.find(planeFt, ShapeNode.class, "Plane01_Shape");
 		shapeNodeLf = Finder.find(planeLf, ShapeNode.class, "Plane01_Shape");
 		shapeNodeRt = Finder.find(planeRt, ShapeNode.class, "Plane01_Shape");
 		shapeNodeUp = Finder.find(planeUp, ShapeNode.class, "Plane01_Shape");
 		
 		GroupNode groupNode = new GroupNode();
 		
 		groupNode.addChildNode(planeBk);
 		groupNode.addChildNode(planeDn);
 		groupNode.addChildNode(planeFt);
 		groupNode.addChildNode(planeLf);
 		groupNode.addChildNode(planeRt);
 		groupNode.addChildNode(planeUp);
 		
 		node.addChildNode(groupNode);
     }
     
     private void loadFiles() throws FileNotFoundException, Exception {
     	
         planeBk = ColladaLoader.load(Helper.getFileResource("models/plane.dae"));
         planeDn = ColladaLoader.load(Helper.getFileResource("models/plane.dae"));
         planeFt = ColladaLoader.load(Helper.getFileResource("models/plane.dae"));
         planeLf = ColladaLoader.load(Helper.getFileResource("models/plane.dae"));
         planeRt = ColladaLoader.load(Helper.getFileResource("models/plane.dae"));
         planeUp = ColladaLoader.load(Helper.getFileResource("models/plane.dae"));
         
 
         bk = new Texture2D(Helper.getFileResource("textures/sky/mountain_ring_bk.jpg"));
         dn = new Texture2D(Helper.getFileResource("textures/sky/mountain_ring_dn.jpg"));
         ft = new Texture2D(Helper.getFileResource("textures/sky/mountain_ring_ft.jpg"));
         lf = new Texture2D(Helper.getFileResource("textures/sky/mountain_ring_lf.jpg"));
         rt = new Texture2D(Helper.getFileResource("textures/sky/mountain_ring_rt.jpg"));
         up = new Texture2D(Helper.getFileResource("textures/sky/mountain_ring_up.jpg"));
     }
     
     /**
      * just for test purpose.
      */
    public void update() {		
         this.node.setTransform(Transform.translate(world.renderer.camera.getEyeWorldTransform(world.renderer.root).getMatrix().translation()));
     }
 	
 	public void refreshShader() {
 		try {
 			// load texture
 			Texture2D texture = new Texture2D(Helper.getFileResource("textures/grass.jpg"));
 	        texture.bind(world.renderer.ctx);
 	        
 			Shader skyVs = new Shader(Helper.getInputStreamResource("shaders/sky.vs"), GL2GL3.GL_VERTEX_SHADER);
 	        Shader skyFs = new Shader(Helper.getInputStreamResource("shaders/sky.fs"), GL2GL3.GL_FRAGMENT_SHADER);
 	        skyFs.compile(world.renderer.ctx);
 	        skyVs.compile(world.renderer.ctx);
 	        ShaderProgram ambientProgram = new ShaderProgram(skyVs, skyFs);
 	        
 	        ShaderMaterial skyMatBk = new ShaderMaterial();
 	        skyMatBk.setTexture("AMBIENT", "jvr_Texture0", bk);
 	        skyMatBk.setShaderProgram("AMBIENT", ambientProgram);
 	        
 	        ShaderMaterial skyMatDn = new ShaderMaterial();
 	        skyMatDn.setTexture("AMBIENT", "jvr_Texture0", dn);
 	        skyMatDn.setShaderProgram("AMBIENT", ambientProgram);
 	        
 	        ShaderMaterial skyMatFt = new ShaderMaterial();
 	        skyMatFt.setTexture("AMBIENT", "jvr_Texture0", ft);
 	        skyMatFt.setShaderProgram("AMBIENT", ambientProgram);
 	        
 	        ShaderMaterial skyMatLf = new ShaderMaterial();
 	        skyMatLf.setTexture("AMBIENT", "jvr_Texture0", lf);
 	        skyMatLf.setShaderProgram("AMBIENT", ambientProgram);
 	        
 	        ShaderMaterial skyMatRt = new ShaderMaterial();
 	        skyMatRt.setTexture("AMBIENT", "jvr_Texture0", rt);
 	        skyMatRt.setShaderProgram("AMBIENT", ambientProgram);
 	        
 	        ShaderMaterial skyMatUp = new ShaderMaterial();
 	        skyMatUp.setTexture("AMBIENT", "jvr_Texture0", up);
 	        skyMatUp.setShaderProgram("AMBIENT", ambientProgram);
 	        
 	        
 	        shapeNodeBk.setMaterial(skyMatBk);
 	        shapeNodeDn.setMaterial(skyMatDn);
 	        shapeNodeFt.setMaterial(skyMatFt);
 	        shapeNodeLf.setMaterial(skyMatLf);
 	        shapeNodeRt.setMaterial(skyMatRt);
 	        shapeNodeUp.setMaterial(skyMatUp);
 	        
 		} catch (IOException e) {
 			e.printStackTrace();
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    	System.out.println("Can not compile shader!");
 		}
 	}
 }
