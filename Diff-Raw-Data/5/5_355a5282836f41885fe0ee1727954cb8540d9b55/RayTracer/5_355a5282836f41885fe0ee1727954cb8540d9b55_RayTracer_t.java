 package raytracing;
 
 
 import image.Color;
 import image.Image;
 import scene.*;
 import math.*;
 
 public class RayTracer {
 	
 	public static final int RECURSION_LIMIT = 10;
 	
 	private Scene scene;
 	private Image image;
 	
 	public RayTracer(Scene scene) {
 		this.scene = scene;
 		image = new Image(scene.getWidth(), scene.getHeight());
 	}
 	
 	public void execute() {
 		for (int i = 0; i < image.getWidth(); i++) {
 			for (int j = 0; j < image.getHeight(); j++) {
 				
 				// cast ray
 				Ray viewRay = new Ray(new Vector((double)i, (double)j, -1000.0), 
 									  new Vector(0.0, 0.0, 1.0).normalized());
 				
 				image.writePixel(i, j, intersectObject(viewRay, 0));
 			}
 		}
 	}	
 	
 	public Color intersectObject(Ray r, int recursion){
 		double distance = Double.MAX_VALUE;
 		Color color = Color.createColor(Color.BLACK);
 		SceneObject closerObject = null;
 		// search for closer object
 		for (int k = 0; k < scene.getNumObjects(); k++) {
 			SceneObject object = scene.getObject(k);
 			double dist = object.intersects(r);
 			if (dist > 0 && dist < distance) {
 				distance = dist;
 				closerObject = object;
 			}
 		}
 		if (recursion < RECURSION_LIMIT && closerObject != null){
 			color = findColor(closerObject, distance, r, ++recursion);
 		}
 		
 		return color;
 	}
 	
 	public Color findColor(SceneObject o, double d, Ray r, int recursion){
 		Color color, reflectColor, refractColor;
 		color = reflectColor = refractColor = o.getMaterial().getColor();
 		Vector intersection = r.getPoint(d);
 		Vector normal = o.getNormal(intersection);
 		double diffuse = o.getMaterial().getDiffuse();
 		double reflection = o.getMaterial().getReflection();
 		double refraction = o.getMaterial().getRefraction();
 		double shade = 0.0;
 		
 		for (int k = 0; k < scene.getNumLights(); k++){
 			SceneObject light = scene.getLight(k);
 			Vector posLight = light.getPos();
 			posLight.normalize();
 			shade = posLight.dot(normal);
 			if (shade < 0){
 				shade = 0;
 			}
 			color = color.multiply((float)((1-diffuse)+diffuse*shade));
 		}
 		
 		double c1 = - normal.dot(r.getDirection());
 		if (recursion < RECURSION_LIMIT && reflection > 0.01){
 			Vector reflectDirection = r.getDirection().add(normal.multiply(2*c1));
 			reflectColor = intersectObject(new Ray(intersection, reflectDirection), ++recursion);
			reflectColor = reflectColor.multiply((float)reflection);
 		}
 		if(recursion < RECURSION_LIMIT && refraction > 0.01){
 			double n = 1/refraction;
 			double c2 = 1- Math.pow(n, 2) * (1 - Math.pow(c1, 2));
 			if (c2 > 0.0){
 				c2 = Math.sqrt(c2);
 				Vector refractDirection = r.getDirection().multiply(n).add(normal.multiply(n*c1-c2));
 				refractColor = intersectObject(new Ray(intersection, refractDirection), ++recursion);
				refractColor = refractColor.multiply((float)refraction);
 			}
 		}
 		//Combinar los colores
 		Color[] colors = {color, reflectColor};
 		return Color.combine(colors);
 	}
 	
 	public Image getImage() {
 		return image;
 	}
 	
 	public void setScene(Scene scene) {
 		this.scene = scene;
 	}
 	
 }
