 package co.devrandom.vc;
 
import static org.lwjgl.opengl.GL11.*;
 
 import java.io.IOException;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 public enum TextureList {
	EVIL_SMILEY("assets/img/evil-smiley.png", false);
	
 	private String filePath;
 	private Texture texture;
 	private boolean smooth;
	
	private TextureList(String filePath, boolean smoothTexture){
 		try {
			this.filePath = filePath;		
 			this.smooth = smooth;
			texture = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream(filePath));
 		} catch (IOException e) {
 			System.err.println("Failed to load image with error:");
 			e.printStackTrace();
 			System.err.println("Exiting.");
 			System.exit(0);
		} 
 	}
	
	public void bindTexture(){
 		if (smooth) {
 			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
 		} else {
 			glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
 		}
 		texture.bind();
 	}
 }
