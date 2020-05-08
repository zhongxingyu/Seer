 package prototype;
 
 import processing.core.PApplet;
 import processing.core.PMatrix3D;
 import processing.opengl.PGraphicsOpenGL;
 
 abstract public class Utils {

	final static PGraphicsOpenGL world = (PGraphicsOpenGL)Prototype.stage.g;

	public static float[] localMouse() {
		PMatrix3D cam = world.camera;
		PMatrix3D mvi = world.modelviewInv;  
		// First, transform along the camera matrix.
		float z = 0;
		float aX = Prototype.stage.mouseX*cam.m00 + Prototype.stage.mouseY*cam.m01 + z*cam.m02 + cam.m03;
		float aY = Prototype.stage.mouseX*cam.m10 + Prototype.stage.mouseY*cam.m11 + z*cam.m12 + cam.m13;
		// Next, transform along the modelviewInv matrix.
		float modelX = aX*mvi.m00 + aY*mvi.m01 + z*mvi.m02 + mvi.m03;
		float modelY = aX*mvi.m10 + aY*mvi.m11 + z*mvi.m12 + mvi.m13;
		
		return new float[] {modelX, modelY, 0};
	}
 	
 	public static float[] localMouse(PMatrix3D localModel) {
 		PMatrix3D cam = world.camera;
 		PMatrix3D mvi = localModel;  
		// First, transform along the camera matrix.
 		float z = 0;
 		float aX = Prototype.stage.mouseX*cam.m00 + Prototype.stage.mouseY*cam.m01 + z*cam.m02 + cam.m03;
 		float aY = Prototype.stage.mouseX*cam.m10 + Prototype.stage.mouseY*cam.m11 + z*cam.m12 + cam.m13;
 		float paX = Prototype.stage.pmouseX*cam.m00 + Prototype.stage.pmouseY*cam.m01 + z*cam.m02 + cam.m03;
 		float paY = Prototype.stage.pmouseX*cam.m10 + Prototype.stage.pmouseY*cam.m11 + z*cam.m12 + cam.m13;
 		
		// Next, transform along the modelviewInv matrix.
 		float modelX = aX*mvi.m00 + aY*mvi.m01 + z*mvi.m02 + mvi.m03;
 		float modelY = aX*mvi.m10 + aY*mvi.m11 + z*mvi.m12 + mvi.m13;
 		float pmodelX = paX*mvi.m00 + paY*mvi.m01 + z*mvi.m02 + mvi.m03;
 		float pmodelY = paX*mvi.m10 + paY*mvi.m11 + z*mvi.m12 + mvi.m13;
 		
		return new float[] {modelX, modelY, pmodelX, pmodelY};
 	}
 	
 	public static PMatrix3D getCurrentModel() {
		return world.modelviewInv;
 	}
 	
 	public static int getComplementar(int original) {
 		float R = Prototype.stage.red(original);
 		float G = Prototype.stage.green(original);
 		float B = Prototype.stage.blue(original);
 		float minRGB = PApplet.min(R,PApplet.min(G,B));
 		float maxRGB = PApplet.max(R,PApplet.max(G,B));
 		float minPlusMax = minRGB + maxRGB;
 		return Prototype.stage.color(minPlusMax-R, minPlusMax-G, minPlusMax-B);	
 	}
 	
 	public static String className(Object o) {
 		String fullClassName = o.getClass().getName();
 		return fullClassName.substring(fullClassName.lastIndexOf(".")+1);
 	}

 }
