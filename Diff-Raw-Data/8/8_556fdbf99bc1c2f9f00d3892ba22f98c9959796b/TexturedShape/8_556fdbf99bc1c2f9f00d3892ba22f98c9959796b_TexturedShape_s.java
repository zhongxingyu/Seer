 package org.racenet.framework;
 
 public class TexturedShape extends GameObject {
 	
 	GLGame game = null;
 	public GLVertices vertices = null;
 	public GLTexture texture = null;
 	float texScaleWidth = 0.05f;
 	float texScaleHeight = 0.05f;
	public short func = FUNC_NONE;
 	
 	public TexturedShape(Vector2 ... vertices) {
 		
 		super(vertices);
 	}
 	
 	public void setupTexture(String fileName, float scaleWidth, float scaleHeight) {
 		
 		this.texture = new GLTexture(this.game, fileName);
 		this.texScaleWidth = scaleWidth == 0 ? 0.05f : scaleWidth;
 		this.texScaleHeight = scaleHeight == 0 ? 0.05f : scaleHeight;
 	}
 	
 	public void reloadTexture() {
 		
 		if (this.texture != null) {
 			
 			this.texture.reload();
 		}
 	}
 	
 	public void dispose() {
 		
 		if (this.texture != null) {
 			
 			this.texture.dispose();
 		}
 	}
 	
 	public void draw() {
 		
 	}
 }
