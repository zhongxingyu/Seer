 package RayTracing;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Random;
 
 public class ColorComputation {
 	
 	public Scene scene;
 	
 	public ColorComputation(Scene scene){
 		this.scene=scene;
 	}
 	
 	public Color getColorOfPixel(int w, int h){
 		Ray ray=getRayFromCamToPixel(w,h);
 		
 		ObjectPrimitive intersected=findClosestIntersection(ray);
 		
 		Color c=getColorByIntersectedRay(intersected, ray);
 		
 		return c;
 	}
 	
 	private Ray getRayFromCamToPixel(int w, int h){
 		Vector startPoint=scene.cam.position;
 		Vector toPoint=scene.scr.getPixelPosition(w, h);
 		return Ray.getRay(startPoint, toPoint);
 	}
 	
 	private ObjectPrimitive findClosestIntersection(Ray ray){
 		return findClosestIntersection(ray, null);
 	}
 	
 	private ObjectPrimitive findClosestIntersection(Ray ray, ObjectPrimitive ignored){
 		double t, t_min=Double.MAX_VALUE;
 		ObjectPrimitive obj_min_intr=null;
 		for(ObjectPrimitive obj: scene){
 			if(obj==ignored)
 				continue;
 			t=obj.getIntersection(ray);
 			if(t<0)
 				continue;
 			if(t<t_min){
 				t_min=t;
 				obj_min_intr=obj;
 			}
 		}
 		return obj_min_intr;
 	}
 	
 	private Color getColorByIntersectedRay(ObjectPrimitive obj, Ray ray){
 		if(obj==null)
 			return scene.setts.backgroundColor;
 		Color outputColor, illuminatedColor, reflectionColor, transColor;
 		Material material=obj.getMaterial();
 		double transparency=material.transparencyValue;
 		
 		Vector point=obj.getIntersectionPoint(ray);
 		Vector normal=obj.getNormalForPoint(point);
 		Map<Light, Color> diffuseColorsFromLights=new HashMap<Light, Color>();
 		Map<Light, Color> specularColorsFromLights=new HashMap<Light, Color>();
 		Map<Light, Ray> LRaysForLights=new HashMap<Light, Ray>();
 		for(Light light: scene.lights){
 			Ray L=Ray.getRay(point, light.position);
 			LRaysForLights.put(light, L);
 		}
 		
 		getDiffuseColors(obj, ray, normal, point, diffuseColorsFromLights, LRaysForLights);
 		getSpecularColors(obj, ray, normal, point, specularColorsFromLights, LRaysForLights);
 		reflectionColor=getReflectedColor(obj, ray, normal, point);
 		transColor=getTransColor(obj, ray, normal, point);
 		
 		illuminatedColor=getIlluminatedFromDiffsAndSpecs_plusShadowing(diffuseColorsFromLights, specularColorsFromLights, point, obj);
 		
 		//illuminatedColor=diffuseColor.add(specularColor);
 		outputColor=Color.color(transColor.mul(transparency).add(illuminatedColor.mul(1-transparency)).add(reflectionColor));
 		return outputColor;
 	}
 
 	private void getDiffuseColors(ObjectPrimitive obj, Ray ray, Vector N, Vector point,
 			Map<Light, Color> diffuseForLights, Map<Light, Ray> LRays){
 		Material material=obj.getMaterial();
 		
 		Color surfaceDiffuse=material.diffuseColor;
 		Color lightDiffuse;
 		Color colorFromLight;
 		for(Light light: scene.lights){
 			lightDiffuse=light.color;
 			Ray L=LRays.get(light);
 			double cos=Math.abs(N.dot(L.direction));
 			colorFromLight=Color.color(surfaceDiffuse.mul(lightDiffuse).mul(cos));
 			diffuseForLights.put(light, colorFromLight);
 		}
 	}
 	
 	private void getSpecularColors(ObjectPrimitive obj, Ray V, Vector N, Vector point,
 			Map<Light, Color> specularForLights, Map<Light, Ray> LRays){
 		Material material=obj.getMaterial();
 		
 		Color surfaceSpecular=material.specularColor;
 		Color lightSpecular;
 		Color colorFromLight;
 		for(Light light: scene.lights){
 			lightSpecular=Color.color(light.color.mul(light.specularI));
 			Ray L=LRays.get(light);
 			Ray R=Ray.getReflectedRay(N, L.direction, point);
 			double cos=Math.abs(R.direction.dot(V.direction));
 			colorFromLight=Color.color(surfaceSpecular.mul(lightSpecular).mul(Math.pow(cos, material.phongSpecularityCoefficient)));
 			specularForLights.put(light, colorFromLight);
 		}
 	}
 	
 	private Color getTransColor(ObjectPrimitive obj, Ray ray, Vector N, Vector point) {
 		// TODO Auto-generated method stub
 		return Color.zeroColor();
 	}
 
 	private Color getReflectedColor(ObjectPrimitive obj, Ray ray, Vector N, Vector point) {
 		// TODO Auto-generated method stub
 		return Color.zeroColor();
 	}
 	
 	private Color getIlluminatedFromDiffsAndSpecs_plusShadowing(Map<Light, Color> diffs, Map<Light, Color> specs, Vector point, ObjectPrimitive obj) {
 		Color illum=Color.zeroColor();
 		for(Light light: scene.lights){
 			Color light_illum=Color.color(diffs.get(light).add(specs.get(light)));
 			double shadowCoeff=1-light.shadowsI;
 			Ray lightToObject=Ray.getRay(light.position, point);
 			double raysPrecent=precentageOfReturnedRays(light, lightToObject, point, obj);
 			Color fromThisLight;
 			if(raysPrecent==1)
 				fromThisLight=light_illum;//TODO is this the real formula
 			else
 				fromThisLight=Color.color(light_illum.mul(shadowCoeff*raysPrecent));//TODO is this the real formula
 			illum=Color.color(illum.add(fromThisLight));
 		}
 		return illum;
 	}
 
 	private double precentageOfReturnedRays(Light light, Ray lightToObject, Vector objPoint, ObjectPrimitive obj) {
 		double widthOfPlane=light.radius;//TODO is it the right number
 		double iterationWidth=scene.setts.rootNumberOfShadowRays;
 		double stepSize=widthOfPlane/iterationWidth;
 		Vector center=light.position;
 		Vector v=new Vector();
 		Vector u=new Vector();
 		LinearAlgebra.getPerpendicularPlane(v, u, center, lightToObject.direction);
 		Vector corner=center.sub(v.mul(widthOfPlane/2)).sub(u.mul(widthOfPlane/2));
 		double horizontalDistance=0, verticalDistance=0;
 		boolean doesItHit=false;
 		double hittingRays=0;
 		for(int i=0; i<iterationWidth; i++){
 			for(int j=0; j<iterationWidth; j++){
 				if(obj.getType()=="sphere")
 					hittingRays+=0;
 				Vector lowerLeftCornerOfCell=corner.add(u.mul(horizontalDistance)).add(v.mul(verticalDistance));
 				double randomHstep=random()*stepSize;
 				double randomVstep=random()*stepSize;
 				Vector randomPointInCell=lowerLeftCornerOfCell.add(u.mul(randomHstep)).add(v.mul(randomVstep));
 				Ray toObj=Ray.getRay(randomPointInCell, objPoint);
 				ObjectPrimitive potentiallyShadowingObj=findClosestIntersection(toObj);
 				if(potentiallyShadowingObj!=null && potentiallyShadowingObj.equals(obj)){
 					doesItHit=true;
 				}
 				if(doesItHit)
 					hittingRays++;
 				horizontalDistance+=stepSize;
 				doesItHit=false;
 			}
			verticalDistance+=stepSize;
			horizontalDistance=0;
 		}
 		return hittingRays/(iterationWidth*iterationWidth);
 	}
 	
 	private double random(){
 		final Random r=new Random();
 		return r.nextDouble();
 	}
 }
