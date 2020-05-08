 import yang.pc.gles.YangGLESFrame;
import samplesurface.SampleSurface;
 
 
 public class TemplateMain {
 
 	public static void main(String[] args) {
 		YangGLESFrame frame = new YangGLESFrame().init(800,600);
		frame.setSurface(new SampleSurface());
 		frame.run();
 	}
 	
 }
