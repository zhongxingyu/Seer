 package projectrts.view;
 
 import projectrts.io.MaterialManager;
 import projectrts.io.TextureManager;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.material.Material;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Node;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 
 /**
  * A class handling the drawing of terrain.
  * 	
  * Based on Jmonkey terrain example code
  * http://jmonkeyengine.org/wiki/doku.php/jme3:beginner:hello_terrain
  * 
  * @author Heqir
  *
  */
 class TerrainHandler {
 	private Node terrainNode = new Node("terrain"); // The node for all terrain
 	private SimpleApplication app;
 
 	
 	public TerrainHandler(SimpleApplication app) {
 		this.app = app;
 	}
 	
 	public void initialize() {
 		  /** 1. Create terrain material and load four textures into it. */
        Material matTerrain = MaterialManager.INSTANCE.getMaterial("Terrain");
      
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
        matTerrain.setTexture("Alpha",TextureManager.INSTANCE.getTexture("Alpha"));
      
         /** 1.2) Add GRASS texture into the red layer (Tex1). */
        Texture grass = TextureManager.INSTANCE.getTexture("Grass");
         grass.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex1", grass);
         matTerrain.setFloat("Tex1Scale", 64f);
      
         /** 1.3) Add WATER texture into the green layer (Tex2) */
        Texture water = TextureManager.INSTANCE.getTexture("Water");
         water.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex2", water);
         matTerrain.setFloat("Tex2Scale", 32f);
      
         /** 1.4) Add ROAD texture into the blue layer (Tex3) */
        Texture rock = TextureManager.INSTANCE.getTexture("Rock");
         rock.setWrap(WrapMode.Repeat);
         matTerrain.setTexture("Tex3", rock);
         matTerrain.setFloat("Tex3Scale", 128f);
      
         /** 2. Create the height map */
        Texture heightMapImage = TextureManager.INSTANCE.getTexture("HeightMap");
         AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
         heightmap.load();
      
         /** 3. We have prepared material and heightmap. 
          * Now we create the actual terrain:
          * 3.1) Create a TerrainQuad and name it "my terrain".
          * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
          * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
          * 3.4) As LOD step scale we supply Vector3f(1,1,1).
          * 3.5) We supply the prepared heightmap itself.
          */
         int patchSize = 65;
         TerrainQuad terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
      
         /** 4. We give the terrain its material, position & scale it, and attach it. */
         terrain.setMaterial(matTerrain);
         terrain.setLocalTranslation(0, 0, 0);
         terrain.setLocalScale(.02f, .01f, .02f);
         terrainNode.attachChild(terrain);
         app.getRootNode().attachChild(terrainNode);
 
         terrainNode.setLocalTranslation(2, -2, -100);
         terrainNode.rotateUpTo(new Vector3f(0f,0f,1f));
 
      
         /** 5. The LOD (level of detail) depends on were the camera is: */
         TerrainLodControl control = new TerrainLodControl(terrain, app.getCamera());
         terrain.addControl(control);
 	}
 
 }
