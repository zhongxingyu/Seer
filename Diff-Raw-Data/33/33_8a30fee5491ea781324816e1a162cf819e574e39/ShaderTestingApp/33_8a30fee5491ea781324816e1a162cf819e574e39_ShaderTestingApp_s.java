 package fi.haju.haju3d.client;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Mesh;
 import com.jme3.system.AppSettings;
 import com.jme3.texture.Image;
 import com.jme3.texture.Texture.MinFilter;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.texture.TextureArray;
 
 import fi.haju.haju3d.client.ui.mesh.ChunkSpatialBuilder;
 import fi.haju.haju3d.client.ui.mesh.MyFace;
 import fi.haju.haju3d.client.ui.mesh.MyMesh;
 import fi.haju.haju3d.client.ui.mesh.MyMesh.MyFaceAndIndex;
 import fi.haju.haju3d.client.ui.mesh.MyTexture;
 import fi.haju.haju3d.client.ui.mesh.MyVertex;
 import fi.haju.haju3d.protocol.world.Tile;
 
 public class ShaderTestingApp extends SimpleApplication {
   public static void main(String[] args) {
     ShaderTestingApp app = new ShaderTestingApp();
     AppSettings settings = new AppSettings(true);
     settings.setResolution(1024, 768);
     settings.setVSync(false);
     settings.setAudioRenderer(null);
     settings.setFullscreen(false);
     app.setSettings(settings);
     app.setShowSettings(false);
     app.start();
   }
 
   private TextureArray textures;
   
   private void loadTextures() {
    Map<MyTexture, String> textureToFilename = new HashMap<>();
     textureToFilename.put(MyTexture.DIRT, "new-dirt.png");
     textureToFilename.put(MyTexture.GRASS, "new-grass.png");
     textureToFilename.put(MyTexture.ROCK, "new-rock.png");
     textureToFilename.put(MyTexture.BRICK, "new-brick.png");
     
     List<Image> images = new ArrayList<Image>();
    for (MyTexture tex : MyTexture.values()) {
       String textureResource = "fi/haju/haju3d/client/textures/" + textureToFilename.get(tex);
       TextureKey key = new TextureKey(textureResource);
       key.setGenerateMips(true);
       images.add(assetManager.loadTexture(key).getImage());
     }
     textures = new TextureArray(images);
     textures.setWrap(WrapMode.Clamp);
     textures.setMinFilter(MinFilter.BilinearNearestMipMap);
     textures.setAnisotropicFilter(4);
   }
   
   @Override
   public void simpleInitApp() {
     loadTextures();
     
     int height = 10;
     int width = 10;
     
     MyMesh myMesh = new MyMesh();
     Random r = new Random(0L);
     
     int z = 0;
     for (int x = 0; x < width; x++) {
       for (int y = 0; y < height; y++) {
        int ti = r.nextInt(999) % MyTexture.values().length;
        MyTexture texture = MyTexture.values()[ti];
         int zindex = r.nextInt(10000);
         // grass always on top
         if (texture == MyTexture.GRASS) {
           zindex += 10000;
         }
         myMesh.addFace(
             new Vector3f(x + 1, y, z),
             new Vector3f(x + 1, y + 1, z),
             new Vector3f(x, y + 1, z),
             new Vector3f(x, y, z),
             texture,
             1.0f, true,
             zindex,
             Tile.ROCK);
       }
     }
     
     for (MyFace face : myMesh.faces) {
       face.normal = face.v2.v.subtract(face.v1.v).cross(face.v4.v.subtract(face.v1.v)).normalize();
     }
     for (Map.Entry<MyVertex, List<MyFaceAndIndex>> e : myMesh.vertexFaces.entrySet()) {
       Collections.sort(e.getValue(), new Comparator<MyFaceAndIndex>() {
         @Override
         public int compare(MyFaceAndIndex o1, MyFaceAndIndex o2) {
           return Integer.compare(o1.face.zIndex, o2.face.zIndex);
         }
       });
     }
     
     //Mesh m = new NewMeshBuilder(mesh).build();
     Mesh m = new ChunkSpatialBuilder.NewMeshBuilder(myMesh, myMesh.faces).build();
     
     /*
     // simple faces
     int i = 0;
     for (MyFace face : realFaces) {
       face.normal = new Vector3f(0, 0, -1);
       face.calcCenter();
       
       Vector3f v1 = face.v1.v;
       Vector3f v2 = face.v2.v;
       Vector3f v3 = face.v3.v;
       Vector3f v4 = face.v4.v;
       
       // full face quadrant
       putVector(vertexes, v1);
       putVector(vertexes, v2);
       putVector(vertexes, v3);
       putVector(vertexes, v4);
 
       putVector(vertexNormals, face.normal);
       putVector(vertexNormals, face.normal);
       putVector(vertexNormals, face.normal);
       putVector(vertexNormals, face.normal);
       
       FloatBuffer uvs = allUvs[0];
       int ti = face.texture.ordinal();
       uvs.put(0.75f).put(0.75f).put(ti);
       uvs.put(0.75f).put(0.25f).put(ti);
       uvs.put(0.25f).put(0.25f).put(ti);
       uvs.put(0.25f).put(0.75f).put(ti);
       
       indexes.put(i + 0).put(i + 1).put(i + 3);
       indexes.put(i + 1).put(i + 2).put(i + 3);
 
       i += 4;
     }
     */
     
 
     //unshaded: 5500  FPS (limited?)
     //lightning, simple texture: 3900 FPS
     //lightning, texture array: 3700 FPS
     //lightning, texture array with 2d UVs and modf: 3700 FPS
     //lightning, texture array with 2d UVs and modf, vertex lightning: 5100 FPS
     //lightning, texture array with 3d UVs, 4-way blending: 2900 FPS
     //lightning, texture array with 3d UVs, 4-way blending, doubled polys: 2800 FPS
     //final: 2600 FPS
     Geometry obj = new Geometry("obj", m);
 //    Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
     //Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
     Material mat = new Material(assetManager, "fi/haju/haju3d/client/shaders/Terrain.j3md");
     mat.setBoolean("UseMaterialColors", true);
     mat.setColor("Diffuse", ColorRGBA.White);
     //mat.setTexture("DiffuseMap", assetManager.loadTexture("fi/haju/haju3d/client/textures/rock.png"));
     mat.setTexture("DiffuseMap", textures);
     obj.setMaterial(mat);
     float scale = 1.0f;
     obj.setLocalScale(scale);
     obj.setLocalTranslation(-width * 0.5f * scale, -height * 0.5f * scale, 0);
     rootNode.attachChild(obj);
     
     DirectionalLight light = new DirectionalLight();
     Vector3f lightDir = new Vector3f(-1, -2, -3);
     light.setDirection(lightDir.normalizeLocal());
     light.setColor(new ColorRGBA(1f, 1f, 1f, 1f).mult(1.0f));
     rootNode.addLight(light);
     
   }
   
 }
