 package ray.tracer;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import ray.Image;
 import ray.IntersectionRecord;
 import ray.Ray;
 import ray.RayRecord;
 import ray.Scene;
 import ray.Workspace;
 import ray.accel.AccelStruct;
 import ray.accel.Bvh;
 import ray.camera.Camera;
 import ray.light.Light;
 import ray.material.Material;
 import ray.math.Color;
 import ray.math.Vector3;
 import ray.surface.Surface;
 
 public class WhittedRayTracer extends RayTracer {	  
 
 	/**
 	 * The main method takes all the parameters and assumes they are input files
 	 * for the ray tracer. It tries to render each one and write it out to a PNG
 	 * file named <input_file>.png.
 	 *
 	 * @param args
 	 */
 	public static final void main(String[] args) {
 		WhittedRayTracer rayTracer = new WhittedRayTracer();
 		rayTracer.run("data/scenes/whitted_ray_tracer", args);
 	}
 
 	/**
 	 * The renderImage method renders the entire scene.
 	 *
 	 * @param scene The scene to be rendered
 	 */
 	public void renderImage(Scene scene) {
 
 		// Get the output image
 		Image image = scene.getImage();
 		Camera cam = scene.getCamera();
 
 		// Set the camera aspect ratio to match output image
 		int width = image.getWidth();
 		int height = image.getHeight();
 
 		// Timing counters
 		long startTime = System.currentTimeMillis();
 
 		// Do some basic setup
 		Workspace work = new Workspace();
 		Ray ray = work.eyeRay;
 		Color pixelColor = work.pixelColor;
 		Color rayColor = work.rayColor;
 
 		int total = height * width;
 		int counter = 0;
 		int lastShownPercent = 0;
 		int samples = scene.getSamples();
 		double sInv = 1.0/samples;
 		double sInvD2 = sInv / 2;
 		double sInvSqr = sInv * sInv;
 
 		for (int y = 0; y < height; y++) {
 			for (int x = 0; x < width; x++) {
 
 				pixelColor.set(0, 0, 0);
 
 				// TODO(B): Support Anti-Aliasing
 				for(int i = 0; i < samples; i++) {
 					for(int j = 0; j < samples; j++) {
 						// TODO(B): Compute the "ray" and call shadeRay on it.
 						
 						
 						
 						pixelColor.add(rayColor);
 					}
 				}
 				pixelColor.scale(sInvSqr);
 
 				//Gamma correct and clamp pixel values
 				pixelColor.gammaCorrect(2.2);
 				pixelColor.clamp(0, 1);
 				image.setPixelColor(pixelColor, x, y);
 
 				counter ++;
 				if((int)(100.0 * counter / total) != lastShownPercent) {
 					lastShownPercent = (int)(100.0*counter / total);
 					System.out.println(lastShownPercent + "%");
 				}
 			}
 		}
 
 		// Output time
 		long totalTime = (System.currentTimeMillis() - startTime);
 		System.out.println("Done.  Total rendering time: "
 				+ (totalTime / 1000.0) + " seconds");
 	}
 
 	/**
 	 * This method returns the color along a single ray in outColor.
 	 *
 	 * @param outColor output space
 	 * @param scene the scene
 	 * @param ray the ray to shade
 	 */
 	public void shadeRay(Color outColor, Scene scene, Ray ray, Workspace workspace, Color absorption, int depth) {
 
 		// Reset the output color
 		outColor.set(0, 0, 0);
 		
 		// TODO(B): Return immediately if depth is greater than 12.
 		if (depth > 12) {
 			return ;
 		}
 
 
 		// Rename all the workspace entries to avoid field accesses
 		// and alot of typing "workspace."
 		IntersectionRecord intersectionRecord = workspace.intersectionRecord;			
 
 		// TODO(B): Fill in the part of BasicRayTracer.shadeRay
 		//          from getting the first intersection onward.
 		
 		if (!scene.getFirstIntersection(intersectionRecord, ray)) {
 			return;
 		}
 		
 		// TODO(A): Compute the color of the intersection point.
 		// 1) Get the material from the intersection record.
 		// 2) Check whether the material can interact directly with light.
 		//    If not, do nothing.
 		// 3) Compute the direction of outgoing light, by subtracting the
 		//	  intersection point from the origin of the ray.
 		// 4) Loop through each light in the scene.
 		// 5) For each light, compute the incoming direction by subtracting
 		//    the intersection point from the light's position.
 		// 6) Compute the BRDF value by calling the evaluate method of the material. 
 		// 7) If the BRDF is not zero, check whether the intersection point is
 		//    shadowed.
 		// 8) If the intersection point is not shadowed, scale the light intensity
 		//    by the BRDF value and add it to outColor.	
 
 		// 1)
 		Material material = intersectionRecord.surface.getMaterial();
 		// 2)
 		if (!material.canInteractWithLight()) {
 			return;
 		}
 		// 3)
 		Vector3 intersectionPoint = new Vector3(intersectionRecord.location);
 		Vector3 outgoing = new Vector3(ray.origin);
 		outgoing.sub(intersectionPoint);
 		//outgoing.normalize();
 		// 4)
 		for (Iterator<Light> iter = scene.getLights().iterator(); iter.hasNext();) {
 			Light light = iter.next();
 			// 5)
 			Vector3 incoming = new Vector3(light.position);
 			incoming.sub(intersectionPoint);
 			//incoming.normalize();
 
 			// 6)
 			Color BDRF = new Color();
 			material.evaluate(BDRF, intersectionRecord, incoming, outgoing);
 
 			// 7)
 			if (!BDRF.isZero()) {
 				Ray shadowRay = new Ray();
 				// 8)
 				if (!isShadowed(scene, light, intersectionRecord, shadowRay)) {
 					Color intensity = new Color(light.intensity);
 					intensity.scale(BDRF);
 					outColor.add(intensity);
 				}
 			}
 		}
 		
 		// TODO(B): Recursively trace rays due to perfectly specular components.
 		// 1) Check whether the material has perfectly specular components.
 		// 2) If so, call material.getIncomingSpecularRays to get the set of rays.
 		// 3) For each ray, see if the scaling factor is greater than zero.
 		//    If not, ignore the ray.
 		// 4) If the factor is greater than zero, check whether the ray is going
 		//    inside or outside the surface by dotting it with the normal.
 		//    The ray goes inside if the dot product is less than zero.
 		// 5) Based on whether the ray goes inside or outside, compute the absorption
 		//    coefficient of the region the next ray will travel through.
 		// 6) Call shadeRay recursively, increasing the depth by 1 and using
 		//    the absorption coefficient you just computed.
 		// 7) Scale the resulting ray color with the scaling factor of the specular ray,
 		//    and add it to the output color.
 		
 		// 1)
 		if (material.hasPerfectlySpecularComponent()) {
 			// 2)
 			RayRecord[] records = material.getIncomingSpecularRays(intersectionRecord, ray.direction);
 			// 3)
 			for (int i = 0; i < records.length; i++) {
 				Ray specRay = records[i].ray;
 				if (!records[i].factor.isZero() && records[i].factor.b > 0.0 &&
 						records[i].factor.g > 0.0 && records[i].factor.r > 0.0) {
 					// 4)
 					double dot = specRay.direction.dot(intersectionRecord.normal);
 					// 5)
 					Color absorp = new Color();
					if (dot > 0) {
 						//ray is traveling inside, so the ray will travel outside next
 						absorp.set(intersectionRecord.surface.getOutsideAbsorption());
 					}
					else {
 						absorp.set(intersectionRecord.surface.getInsideAbsorption());
 					}
 					// 6)
 					Color outColorRec = new Color();
 					shadeRay(outColorRec, scene, ray, workspace, absorp, depth + 1);
 					
 					// 7)
 					outColorRec.scale(records[i].factor);
 					outColor.add(outColorRec);
 				}
 			}
 		}
 		
 		// TODO(B): Compute the distance that the ray travels and attenuate
 		//          the output color by the absorption according to Beer's law.
 		double tBegin = ray.start;
 		double tEnd = intersectionRecord.t;
 		Vector3 startPos = new Vector3(ray.origin);
 		startPos.scaleAdd(tBegin, ray.direction);
 		Vector3 endPos = new Vector3(ray.origin);
 		endPos.scaleAdd(tEnd, ray.direction);
 		
 		//subtract the end position from the starting position to
 		//get the vector from start to finish
 		endPos.sub(startPos);
 		double length = endPos.length();
 		
 		Color absorp = new Color(absorption);
 		absorp.scale(-length);
 		//outColor = outColor exp(- l * absorp)
 		outColor.scale(Math.exp(absorp.r), Math.exp(absorp.g), Math.exp(absorp.b));
 	}
 
 	@Override
 	protected AccelStruct createAccelStruct(Scene scene) {
 		ArrayList<Surface> allSurfaces = new ArrayList<Surface>();
 		List<Surface> surfaces = scene.getSurfaces();
 		for (Iterator<Surface> iter = surfaces.iterator(); iter.hasNext();) {
 			iter.next().appendRenderableSurfaces(allSurfaces);
 		}
 		Surface []surfaceArray = new Surface[allSurfaces.size()];
 		int count = 0;
 		for(Iterator<Surface> iter = allSurfaces.iterator(); iter.hasNext();)
 			surfaceArray[count++] = iter.next();
 
 		AccelStruct accelStruct = new Bvh(surfaceArray, 0, surfaceArray.length);
 		accelStruct.build();
 		return accelStruct;
 	}
 }
