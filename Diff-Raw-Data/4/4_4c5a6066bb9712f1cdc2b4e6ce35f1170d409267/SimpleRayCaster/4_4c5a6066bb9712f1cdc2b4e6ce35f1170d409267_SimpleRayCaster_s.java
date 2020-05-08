 package ex3.render.raytrace;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 
 import ex3.parser.Element;
 import ex3.parser.SceneDescriptor;
 import ex3.render.IRenderer;
 import ex3.render.raytrace.Hit;
 import ex3.render.raytrace.Scene;
 import math.Ray;
 
 
 /**
  * Simple orthographic raycaster renderer
  * 
  */
 public class SimpleRayCaster implements IRenderer {
 
 	protected int width;
 	protected int height;
 	protected Scene scene;
 
 	@Override
	public void init(SceneDescriptor sceneDesc, int width, int height) {
 		this.width = width;
 		this.height = height;
 		scene = new Scene();
 		scene.init(sceneDesc.getSceneAttributes());
 
 		for (Element e : sceneDesc.getObjects()) {
 			scene.addObjectByName(e.getName(), e.getAttributes());
 		}
 	}
 
 	@Override
 	public void renderLine(BufferedImage canvas, int line) {
 
 		int y = line;
 		for (int x = 0; x < width; ++x) {
 			canvas.setRGB(x, y, castRay(x, height - y - 1).getRGB());
 		}
 
 	}
 
 	/**
 	 * Compute color for given image coordinates (x,y)
 	 * 
 	 * @param x
 	 * @param y
 	 * @return Color at coordinate
 	 */
 	protected Color castRay(int x, int y) {
 		Ray ray = scene.camera.constructRayThroughPixel(x, y);
 		Hit hit = scene.findIntersection(ray);
 		return scene.calcColor(hit, ray);
 	}
 }
