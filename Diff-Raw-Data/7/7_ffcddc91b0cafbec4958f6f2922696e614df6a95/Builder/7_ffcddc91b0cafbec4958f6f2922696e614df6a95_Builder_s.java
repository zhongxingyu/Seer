 package euclidstand;
 
 import com.jmex.effects.particles.ParticleSystem;
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.Random;
 
 import com.jme.bounding.BoundingBox;
 import com.jme.bounding.BoundingSphere;
 import com.jme.math.Vector3f;
 import com.jme.scene.Node;
 import com.jme.scene.Skybox;
 import com.jme.scene.shape.Box;
 import com.jme.scene.state.MaterialState;
 import com.jme.scene.state.GLSLShaderObjectsState;
 import com.jme.image.Texture;
 import com.jme.math.Quaternion;
 import com.jme.util.TextureManager;
 import com.jme.util.export.binary.BinaryImporter;
 import com.jme.renderer.ColorRGBA;
 import com.jme.renderer.Renderer;
 import com.jme.scene.shape.Sphere;
 import com.jme.util.resource.ResourceLocatorTool;
 import com.jme.util.resource.SimpleResourceLocator;
 import com.jmex.effects.particles.ParticleControllerListener;
 import com.jmex.effects.particles.ParticleMesh;
 import com.jmex.terrain.util.MidPointHeightMap;
 
 import euclidstand.engine.JMESpatial;
 import euclidstand.engine.JMETerrain;
 import java.net.URL;
 import jmetest.TestChooser;
 
 /**
  * Builder class which constructs model representations of ingame objects
  */
 public class Builder {
 
 	private static final Logger logger = Logger.getLogger(Builder.class.getName());
 	private static final String mediaDir = "media wip/";
 	private static final String shaderDir = "shaders/";
 	// Terrain parameters
 	private static final int mapsize = 32;
 	private static final float scale = 20;
 	private Renderer renderer;
 	private Random random;
 
 	public Builder() {
 		this.random = new Random();
 	}
 
 	public void initialise(Renderer renderer) {
 		ResourceLocatorTool.addResourceLocator(
 				ResourceLocatorTool.TYPE_TEXTURE,
 				new SimpleResourceLocator(new File(mediaDir).toURI()));
 		this.renderer = renderer;
 	}
 
 	/**
 	 * @param baseName of base model
 	 * @param barrelName of barrel model
 	 * @param renderer for binding materials
 	 * @return the player model
 	 */
 	public JMESpatial buildPlayer(String baseName, String barrelName) {
 		logger.fine("Building player");
 		Node node = null;
 		try {
 			File file = new File(mediaDir + "cannon-base.jme");
 			node = (Node) BinaryImporter.getInstance().load(file);
 			node.setName(baseName);
 			node.setLocalScale(4f);
 			node.setModelBound(new BoundingBox());
 
 			File fileBarrel = new File(mediaDir + "cannon-barrel.jme");
 			Node barrelNode = (Node) BinaryImporter.getInstance().load(fileBarrel);
 			barrelNode.setName(barrelName);
 			node.attachChild(barrelNode);
 
 			GLSLShaderObjectsState testShader = renderer.createGLSLShaderObjectsState();
 			ClassLoader cl = Builder.class.getClassLoader();
 			URL vertexShader = cl.getResource(shaderDir + "sphereharm.vs");
 			URL fragmentShader = cl.getResource(shaderDir + "sphereharm.fs");
 			testShader.load(vertexShader, fragmentShader);
 			// TODO: How to share shader objects across instances
 			//testShader.setEnabled(true);
 			//node.setRenderState(testShader);
 
 			node.updateRenderState();
 			node.updateModelBound();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return new JMESpatial(node);
 	}
 
 	/**
 	 * @param name of model
 	 * @param renderer for binding materials
 	 * @return the terrain model
 	 */
 	public JMETerrain buildTerrain(String name) {
 		logger.fine("Building terrain");
 		MidPointHeightMap map = new MidPointHeightMap(mapsize, 1f);
 		float offset = (mapsize * scale) / 2;
 		JMETerrain terrain = new JMETerrain(
 				name,
 				mapsize, new Vector3f(scale, 0.1f, scale),
 				map.getHeightMap(), new Vector3f(-offset, 0, -offset));
 		terrain.setModelBound(new BoundingBox());
 		terrain.updateModelBound();
 		GLSLShaderObjectsState testShader = renderer.createGLSLShaderObjectsState();
 		testShader.load(Builder.class.getClassLoader().getResource("shaders/sphereharm.vs"),
 				Builder.class.getClassLoader().getResource("shaders/sphereharm.fs"));
 		//testShader.setEnabled(true);
 		//terrain.setRenderState(testShader);
 		terrain.updateRenderState();
 		return terrain;
 	}
 
 	/**
 	 * @param name of model
 	 * @return the sky model
 	 */
 	public JMESpatial buildSky(String name) {
 		logger.fine("Building sky");
 		Skybox skybox = new Skybox(name, 500, 500, 500);
 		Texture north = TextureManager.loadTexture(
 				TestChooser.class.getClassLoader().getResource(
 				"jmetest/data/texture/north.jpg"),
 				Texture.MinificationFilter.BilinearNearestMipMap,
 				Texture.MagnificationFilter.Bilinear);
 		Texture south = TextureManager.loadTexture(
 				TestChooser.class.getClassLoader().getResource(
 				"jmetest/data/texture/south.jpg"),
 				Texture.MinificationFilter.BilinearNearestMipMap,
 				Texture.MagnificationFilter.Bilinear);
 		Texture east = TextureManager.loadTexture(
 				TestChooser.class.getClassLoader().getResource(
 				"jmetest/data/texture/east.jpg"),
 				Texture.MinificationFilter.BilinearNearestMipMap,
 				Texture.MagnificationFilter.Bilinear);
 		Texture west = TextureManager.loadTexture(
 				TestChooser.class.getClassLoader().getResource(
 				"jmetest/data/texture/west.jpg"),
 				Texture.MinificationFilter.BilinearNearestMipMap,
 				Texture.MagnificationFilter.Bilinear);
 		Texture up = TextureManager.loadTexture(
 				TestChooser.class.getClassLoader().getResource(
 				"jmetest/data/texture/top.jpg"),
 				Texture.MinificationFilter.BilinearNearestMipMap,
 				Texture.MagnificationFilter.Bilinear);
 		Texture down = TextureManager.loadTexture(
 				TestChooser.class.getClassLoader().getResource(
 				"jmetest/data/texture/bottom.jpg"),
 				Texture.MinificationFilter.BilinearNearestMipMap,
 				Texture.MagnificationFilter.Bilinear);
 
 		skybox.setTexture(Skybox.Face.North, north);
 		skybox.setTexture(Skybox.Face.West, west);
 		skybox.setTexture(Skybox.Face.South, south);
 		skybox.setTexture(Skybox.Face.East, east);
 		skybox.setTexture(Skybox.Face.Up, up);
 		skybox.setTexture(Skybox.Face.Down, down);
 		skybox.preloadTextures();
 		skybox.updateRenderState();
 		return new JMESpatial(skybox);
 	}
 
 	/**
 	 * @param name of model
 	 * @param renderer for binding materials
 	 * @return the bad guy model
 	 */
 	public JMESpatial buildBaddie(String name) {
 		logger.fine("Building baddie");
 		float size = mapsize * scale;
 		int side = random.nextInt(4);
 		float offset = random.nextFloat() * size - size / 2;
 		Vector3f location = null;
 		switch (side) {
 			case 0: // north
 				location = new Vector3f(offset, 0, size / 2 - 5);
 				break;
 			case 1: // south
 				location = new Vector3f(offset, 0, -size / 2 + 5);
 				break;
 			case 2: // east
 				location = new Vector3f(-size / 2 + 5, 0, offset);
 				break;
 			case 3: // west
 				location = new Vector3f(size / 2 - 5, 0, offset);
 				break;
 		}
 
 		Box box = new Box(name, new Vector3f(0, 0, 0), 1, 1, 2);
 		box.setLocalTranslation(location);
 		box.setRandomColors();
 		box.setModelBound(new BoundingBox());
 		box.updateModelBound();
 		MaterialState ms = renderer.createMaterialState();
 		ms.setDiffuse(ColorRGBA.red);
 		box.setRenderState(ms);
 		GLSLShaderObjectsState testShader = renderer.createGLSLShaderObjectsState();
 		testShader.load(Builder.class.getClassLoader().getResource("shaders/sphereharm.vs"),
 				Builder.class.getClassLoader().getResource("shaders/sphereharm.fs"));
 		//testShader.setEnabled(true);
 		//box.setRenderState(testShader);
 		box.updateRenderState();
 		box.lookAt(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0));
 		return new JMESpatial(box);
 	}
 
 	/**
 	 * @param name of model
 	 * @param renderer for binding materials
 	 * @param barrel model which forms starting coordinates for the shell
 	 * @return the shell model
 	 */
 	public JMESpatial buildShell(String name, JMESpatial barrel) {
 		logger.fine("Building shell");
 		Vector3f translation = new Vector3f(barrel.getWorldTranslation());
 		Quaternion rotation = new Quaternion(barrel.getWorldRotation());
 		float[] angles = rotation.toAngles(null);
 		angles[0] += -0.45f;
 		rotation.fromAngles(angles);
 		translation.addLocal(rotation.getRotationColumn(2).mult(10f));
 
 		Sphere sphere = new Sphere(name, 5, 20, 1);
 		sphere.setModelBound(new BoundingSphere());
 		sphere.updateModelBound();
 		sphere.updateRenderState();
 		sphere.setLocalTranslation(translation);
 		sphere.setLocalRotation(rotation);
 		GLSLShaderObjectsState testShader = renderer.createGLSLShaderObjectsState();
 		testShader.load(Builder.class.getClassLoader().getResource("shaders/sphereharm.vs"),
 				Builder.class.getClassLoader().getResource("shaders/sphereharm.fs"));
 		//testShader.setEnabled(true);
 		return new JMESpatial(sphere);
 	}
 
 	public ParticleMesh buildBigExplosion(String name, JMESpatial victim) {
 		logger.fine("Building big explosion");
 		ParticleMesh mesh = buildExplosion(name, victim, "explosion.jme");
 		return mesh;
 	}
 
 	public ParticleMesh buildSmallExplosion(String name, JMESpatial victim) {
 		logger.fine("Building small explosion");
 		ParticleMesh mesh = buildExplosion(name, victim, "fast-explosion.jme");
 		mesh.setLocalScale(.1f);
 		return mesh;
 	}
 
 	private ParticleMesh buildExplosion(String name, JMESpatial victim, String filename) {
 		File file = new File(mediaDir + filename);
 		ParticleMesh mesh = null;
 		try {
 			mesh = (ParticleMesh) BinaryImporter.getInstance().load(file);
 			mesh.setName(name);
 			mesh.setLocalTranslation(victim.getWorldTranslation());
 			mesh.setModelBound(new BoundingBox());
 			mesh.getParticleController().addListener(new ParticleControllerListener() {
 
 				public void onDead(ParticleSystem particles) {
 					particles.removeFromParent();
 				}
 			});
 		} catch (IOException ex) {
 			Logger.getLogger(Builder.class.getName()).log(Level.SEVERE, null, ex);
 		}
 
 		return mesh;
 	}
 }
