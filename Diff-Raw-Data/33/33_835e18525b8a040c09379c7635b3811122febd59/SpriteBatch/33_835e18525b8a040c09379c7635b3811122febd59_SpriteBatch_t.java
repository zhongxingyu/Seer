 package graphics;
 import content.ContentManager;
 import utils.Rectangle;
 
 import math.Matrix4;
 import math.Vector2;
 
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL13.*;
 import static org.lwjgl.opengl.GL20.*;
 import static org.lwjgl.opengl.GL30.*;
 
 /** A class used to do batched 2D rendering.
  * The design is heavily influenced by the class
  * with the same name SpriteBatch in the XNA framework. 
  * However this used OpenGL 3 instead of DirectX 9
  * 
  * @author Lukas Kurtyan
  *
  */
 public class SpriteBatch {
 	
 	//VertexBuffers
 	private final DynamicVertexBuffer vertexBuffer;
 	private final DynamicVertexBuffer textureBuffer;
 	private final DynamicVertexBuffer colorBuffer;
 	//IndexBuffer
 	private final IndexBuffer indexBuffer;
 	
 	
 	//The texture used for drawing.
 	private Texture2D activeTexture;
 	
 	//The x and y offsets of the different corners.
 	private static final float[] xOffsets  = {
 		0f,
 		1f,
 		1f,
 		0f,
 	};	
 	private static final float[] yOffsets  = {
 		0f,
 		0f,
 		1f,
 		1f
 	};	
 	
 	//The current number of sprites in the batch.
 	private int spriteCount;
 	
 	//The max number of sprites in the batch.
 	private final int maxSpriteCount = 1024;
 	
 	//The OpenGL VOA object used for rendering.
 	private int vertexArrayObject;
 	
 	//The viewport we are rendering to.
 	private Rectangle viewport;
 	
 	//SpriteEffects
 	private static ShaderEffect basicEffect;
 	private ShaderEffect activeEffect;
 	
 	//A flag indicating if we are inside of a begin-call sequence.
 	private boolean betweenBeginAndEnd;
 	
 	private Matrix4 view;
 	
 	/**Creates a new SpriteBatch.
 	 * 
 	 * @param viewport the dimensions of the screen rendered to.
 	 */
 	public SpriteBatch(Rectangle viewport) {
 		//Create the buffers with the correct length. 
 		this.vertexBuffer = new DynamicVertexBuffer(this.maxSpriteCount * 8);
 		this.textureBuffer = new DynamicVertexBuffer(this.maxSpriteCount * 8);
 		this.colorBuffer = new DynamicVertexBuffer(this.maxSpriteCount * 16);
 		this.indexBuffer = new IndexBuffer(this.maxSpriteCount * 12);
 		
 		
 		this.viewport = viewport;
 		
 		this.initializeBuffers();
 		
 		//Initialize the basicEffect. 
 		this.initializeShaders();
 		this.activeEffect = basicEffect;
 	}
 
 	private void initializeBuffers() {
 			
 		this.vertexArrayObject = glGenVertexArrays();
 		glBindVertexArray(this.vertexArrayObject);
 		
 		//Bind the position buffer and prepare the VOA for positional input.
 		this.vertexBuffer.bindGL();
 		glEnableVertexAttribArray(0);
 		glVertexAttribPointer(0, 2, GL_FLOAT, false, 0, 0);
 		
 
 		//Bind the textureCoords buffer and prepare the VOA for textureCoord input.
 		this.textureBuffer.bindGL();
 		glEnableVertexAttribArray(1);
 		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
 		
 
 		//Bind the color buffer and prepare the VOA for colored input.
 		this.colorBuffer.bindGL();
 		glEnableVertexAttribArray(2);
 		glVertexAttribPointer(2, 4, GL_FLOAT, false, 0, 0);
 		
 		
 		setupIndexBuffer();
 				
 		glBindVertexArray(0);
 	}
 	
 	private void setupIndexBuffer() {
 		short[] indecies = new short[this.indexBuffer.capacity()];
 		for (int i = 0; i < this.maxSpriteCount; i++)
 		{
 			indecies[i * 6] = (short)(i * 4);
 			indecies[i * 6 + 1] = (short)(i * 4 + 1);
 			indecies[i * 6 + 2] = (short)(i * 4 + 2);
 			indecies[i * 6 + 3] = (short)(i * 4);
 			indecies[i * 6 + 4] = (short)(i * 4 + 2);
 			indecies[i * 6 + 5] = (short)(i * 4 + 3);
 		}
 		//Bind the indexbuffer and buffer the data on the graphics-card. 
 		this.indexBuffer.bindGL();
 		this.indexBuffer.setData(indecies);
 		this.indexBuffer.flushGL();
 	}
 
 	private void initializeShaders() {
 		//Load a shader.
 		basicEffect = ContentManager.loadShaderEffect("Standard.vert", "Standard.frag");
 	}
 
 	private void setupUniforms() {
 		//Start using the active shader. This has to be done so we can set the uniforms.
 		this.activeEffect.bindShaderProgram();
 		
 		//Set the shader uniforms.
 		this.activeEffect.setUniform("viewport", viewport.Width, viewport.Height);
 		this.activeEffect.setUniformSampler("colorTexture", 0);
 		this.activeEffect.setUniform("view", this.view);
 		
 		Matrix4 projectionMatrix =
 				Matrix4.createOrthogonalProjection(viewport.getLeft(),
 												   viewport.getRight(), 
 												   viewport.getBottom(), 
 												   viewport.getTop(), 1, -1);
 		
 		this.activeEffect.setUniform("projection", projectionMatrix);
 		//Stop using the active shader.
 		this.activeEffect.unbindShaderProgram();
 	}
 	
 	/** Fixes the spritebatch to render to the new screen dimensions.
 	 * 
 	 * @param newViewport the screen viewport.
 	 */
 	public void setViewport(Rectangle newViewport) {
 		this.viewport = newViewport;
 	}
 	
 	/**Gets the dimensions of the screen rendered to.
 	 * 
 	 * @return a rectangle containing screen dimensions.
 	 */
 	public Rectangle getViewport() {
 		return this.viewport;
 	}
 	
 	public Matrix4 getView() {
 		return this.view;
 	}
 	
 	public ShaderEffect getActiveEffect() {
 		return activeEffect;
 	}
 	
 	/**Clear the screen with the specified color.
 	 * 
 	 * @param color the color to clear the screen with.
 	 */
 	public void clearScreen(Color color) {
 		
 		glClearColor(color.R, color.G, color.B, color.A); 
 		glClear(GL_COLOR_BUFFER_BIT);	 
 	}	
 	
 	/**Prepares the spritebatch for rendering. 
 	 * This overload uses the standard shadereffect and the identity view.
 	 * (note* begin must be called befoure draw or end can be called)
 	 */
 	public void begin() {
 		this.begin(null);
 	}
 	
 	/**Prepares the spritebatch for rendering.
 	 * This overload uses the provided effect and the identity view.
 	 * (note* begin must be called before draw or end can be called)
 	 * @param effect the custom effect used.
 	 */
 	public void begin(ShaderEffect effect) {
 	
 		this.begin(effect, Matrix4.Identity);
 	}
 	
 	/**Prepares the spritebatch for rendering.
 	 * This overload uses the provided effect and the provided view.
 	 * (note* begin must be called before draw or end can be called)
 	 * @param effect the custom effect used.
 	 * @param view the custom view used.
 	 */
 	public void begin(ShaderEffect effect, Matrix4 view) {
 	
 		glEnable(GL_BLEND);
 		glEnable(GL_TEXTURE_2D);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		
 		if(effect == null) {
 			this.activeEffect = basicEffect;
 		} else {
 			this.activeEffect = effect;	
 		}
 		
 		
 		this.betweenBeginAndEnd = true;
 		this.view = view;
 		this.setupUniforms();
 	}
 	
 	/** Ends the batch drawing, Draws the batched items to the current renderTarget.
 	 */
 	public void end() {
 		if(!this.betweenBeginAndEnd) {
 			throw new GraphicsException("Cannot end before you begin...");
 		}
 		
 		if(this.activeTexture != null) {
 			this.renderBatch();
 		}
 		this.betweenBeginAndEnd = false;
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param position the top-left position.
 	 * @param color the tint.
 	 */
 	public void draw(Texture2D texture, Vector2 position, Color color) {
 		this.internalDraw(texture, position, color, null, Vector2.Zero, Vector2.One, 0.0f, false);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 *  
 	 * @param texture the texture.
 	 * @param destRectangle the bounds of the sprite.
 	 * @param color the tint.
 	 * @param sorceRectangle the part of the texture to draw. (null makes the whole texture draw)
 	 */
 	public void draw(Texture2D texture, Rectangle destRectangle, Color color, Rectangle sorceRectangle) {
 		Vector2 pos = new Vector2(destRectangle.X, destRectangle.Y);
 		Vector2 scale = new Vector2((float)destRectangle.Width / texture.Width, 
 									(float)destRectangle.Height / texture.Height);
 		
 		this.internalDraw(texture, pos, color, sorceRectangle, Vector2.Zero, scale, 0.0f, false);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param destRectangle the bounds of the sprite.
 	 * @param color the tint.
 	 * @param sorceRectangle the part of the texture to draw. (null makes the whole texture draw)
 	 * @param origin the origin (rendering offset)
 	 * @param rotation the rotation in a counterclockwise system Range 0 to 2Pi
 	 * @param mirror a flag indicating if the texture should be horizontally flipped.
 	 */
 	public void draw(Texture2D texture, Rectangle destRectangle, Color color, Rectangle sorceRectangle, Vector2 origin,
 					 float rotation, boolean mirror) {
 		Vector2 pos = new Vector2(destRectangle.X, destRectangle.Y);
 		Vector2 scale = new Vector2(texture.Width / destRectangle.Width, 
 									texture.Height / destRectangle.Height);
 		this.internalDraw(texture, pos, color, sorceRectangle, origin, scale, rotation, mirror);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param position the top-left position.
 	 * @param color the tint.
 	 * @param sorceRectangle the part of the texture to draw. (null makes the whole texture draw)
 	 */
 	public void draw(Texture2D texture, Vector2 position, Color color, Rectangle sorceRectangle) {
 		this.internalDraw(texture, position, color, sorceRectangle, Vector2.Zero, Vector2.One, 0.0f, false);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param position the top-left position.
 	 * @param color the tint.
 	 * @param origin the origin (rendering offset)
 	 */
 	public void draw(Texture2D texture, Vector2 position,  Color color, Vector2 origin) {
 		this.internalDraw(texture, position, color, null, origin, Vector2.One, 0.0f, false);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param position the top-left position.
 	 * @param color the tint.
 	 * @param sorceRectangle the part of the texture to draw. (null makes the whole texture draw)
 	 * @param origin the origin (rendering offset)
 	 */
 	public void draw(Texture2D texture, Vector2 position,  Color color, Rectangle sorceRectangle, Vector2 origin) {
 		this.internalDraw(texture, position, color, sorceRectangle, origin, Vector2.One, 0.0f, false);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param position the top-left position.
 	 * @param color the tint.
 	 * @param sorceRectangle the part of the texture to draw. (null makes the whole texture draw)
 	 * @param origin the origin (rendering offset)
 	 * @param scale the scale.
 	 * @param rotation the rotation in a counterclockwise system Range 0 to 2Pi
 	 * @param mirror a flag indicating if the texture should be horizontally flipped.
 	 */
 	public void draw(Texture2D texture, Vector2 position, Color color, Rectangle sorceRectangle, Vector2 origin,
 			 float scale, float rotation, boolean mirror) {
 		this.internalDraw(texture, position, color, sorceRectangle, origin, new Vector2(scale, scale), rotation, mirror);
 	}
 	
 	/**Adds a sprite to the batch with the specified arguments.
 	 * 
 	 * @param texture the texture.
 	 * @param position the top-left position.
 	 * @param color the tint.
 	 * @param sorceRectangle the part of the texture to draw. (null makes the whole texture draw)
 	 * @param origin the origin (rendering offset)
 	 * @param scale the scale.
 	 * @param rotation the rotation in a counterclockwise system Range 0 to 2Pi
 	 * @param mirror a flag indicating if the texture should be horizontally flipped.
 	 */
 	public void draw(Texture2D texture, Vector2 position, Color color, Rectangle sorceRectangle, Vector2 origin,
 					 Vector2 scale, float rotation, boolean mirror) {
 		this.internalDraw(texture, position, color, sorceRectangle, origin, scale, rotation, mirror);
 	}
 	
 	public void drawString(TextureFont font , String text, Vector2 position, Color color) {		
 		this.internalDrawString(font, text, position, color, Vector2.Zero, Vector2.One, 0.0f, false);	
 	}
 	
 	public void drawString(TextureFont font , String text, Vector2 position, Color color, Vector2 origin) {		
 		this.internalDrawString(font, text, position, color, origin, Vector2.One, 0.0f, false);	
 	}
 	
 	public void drawString(TextureFont font , String text, Vector2 position, Color color, Vector2 origin, Vector2 scale) {		
 		this.internalDrawString(font, text, position, color, origin, scale, 0.0f, false);	
 	}
 	
 	public void drawString(TextureFont font , String text, Vector2 position, Color color, Vector2 origin, Vector2 scale, float rotation, boolean mirror) {		
 		this.internalDrawString(font, text, position, color, origin, scale, rotation, mirror);	
 	}
 	
 	
 	private void internalDrawString(TextureFont font, String text,
 			Vector2 destination, Color color, Vector2 origin, Vector2 scale, float rotation,
 			boolean mirror) {
 		
 		//TODO this is unreadable dosn't work correctly and so on but other then that  like it!'
 		
 		Vector2 textDim = font.meassureString(text);
 		textDim = new Vector2(textDim.X * scale.X, textDim.Y * scale.Y);
 		float x = 0, y = 0, rx = 0;
 		float angleX = (float)Math.cos(rotation), angleY = (float)Math.sin(rotation);
 		float dist = 0;
 		float distY = 0;
 		for (int i = 0; i < text.length(); i++) {
 			char c = text.charAt(i);
 			if(c == '\n') {
 				distY += font.getLineSpacing();
 				dist = 0;
 				continue;
 			}
 			Vector2 rorig = origin;
 			Rectangle srcRect = font.getCharacterSourceRect(c);
 			
 
 			x =  dist * angleX - distY * angleY;
			y =  dist * angleY + distY * angleX + destination.Y;
 			
 			if(mirror) {
 				rx = textDim.X - x + destination.X;
				rorig = new Vector2((origin.X + srcRect.Width), origin.Y);
 			}
 			else {
 				rx = x + destination.X;
 			}
 			
			this.internalDraw(font.getTexture(), new Vector2(rx,y),
						      color, srcRect, rorig, scale, rotation, mirror);
 			
 			dist += (srcRect.Width + font.getCharacterSpacing()) * scale.X;
 			
 		}
 	}
 
 	private void internalDraw(Texture2D texture, Vector2 destination, Color color, Rectangle sorceRectangle,
 							  Vector2 origin, Vector2 scale, float rotation, boolean mirror)
 	{
 			//Make sure the texture is not null.
 			if(texture == null) {
 				throw new NullPointerException("Texture cannot be null!");
 			} else if(!this.betweenBeginAndEnd) {
 				throw new GraphicsException("You cannot draw befoure begin is called!");
 			}
 			
 			//If the texture is not the active texture render the current batch.
 			if(this.activeTexture != texture) {
 				if(this.activeTexture != null) {
 					this.renderBatch();
 				}
 				this.activeTexture = texture;
 			}
 			
 			float destWidth;
 			float destHeight;
 			float texX, texY;
 			float texWidth, texHeight;
 			
 			if(sorceRectangle != null) {
 				destWidth = sorceRectangle.Width * scale.X;
 				destHeight = sorceRectangle.Height * scale.Y;		
 				texX = (float)sorceRectangle.X / texture.Width;
 				texY = -(float)sorceRectangle.Y / texture.Height;
 				texWidth = (float)sorceRectangle.Width / texture.Width;
 				texHeight = (float)sorceRectangle.Height / texture.Height;
 				
 				texY += 1 - texHeight;
 			} else {
 				destWidth = texture.Width * scale.X;
 				destHeight = texture.Height * scale.Y;
 				texX = 0.0f;
 				texY = 0.0f;
 				texWidth = 1.0f;
 				texHeight = 1.0f;
 			}
 			
 			float angleX = (float)Math.cos(rotation);
 			float angleY = (float)Math.sin(rotation);
 			float origX = origin.X / destWidth;
 			float origY = origin.Y / destHeight;
 			float[] colorValues = color.toArray();
 			
 			for (int i = 0; i < 4; i++) {
 				float num0 = xOffsets[i];
 				float num1 = yOffsets[i];
 							
 				float num2 = (num0 - origX) * destWidth;
 				float num3 = (num1 - origY) * destHeight;
 				
 				if(mirror) {
 					num0 = 1f - num0;
 				}
 				
 				float x = destination.X + num2 * angleX - num3 * angleY;
 				float y = destination.Y + num2 * angleY + num3 * angleX;
 				
 				this.vertexBuffer.setData(x);
 				this.vertexBuffer.setData(y);
 				
 				float realTexCoordX = texX + num0 * texWidth;
 				float realTexCoordY = texY + (1f - num1) * texHeight;
 				
 				this.textureBuffer.setData(realTexCoordX);
 				this.textureBuffer.setData(realTexCoordY);
 				
 				this.colorBuffer.setData(colorValues);				
 			}
 			
 			this.spriteCount++;
 			if(this.spriteCount == this.maxSpriteCount) {
 				this.renderBatch();
 			}
 			
 			
 	}
 
 	private void renderBatch() {
 		
 		glBindVertexArray(this.vertexArrayObject);
 		
 		flushBuffers();
 		
 		this.activeEffect.bindShaderProgram();
 		
 		glActiveTexture(GL_TEXTURE0);
 		this.activeTexture.bindGL();
 		
 		glDrawElements(GL_TRIANGLES, this.spriteCount * 6, GL_UNSIGNED_SHORT, 0);
 		
 		this.activeEffect.unbindShaderProgram();
 		
 		glBindVertexArray(0);
 		
 		this.spriteCount = 0;
 		this.activeTexture = null;
 	}
 
 	private void flushBuffers() {
 		this.vertexBuffer.bindGL();
 		this.vertexBuffer.flushGL();
 		
 		this.textureBuffer.bindGL();	
 		this.textureBuffer.flushGL();
 		
 		this.colorBuffer.bindGL();
 		this.colorBuffer.flushGL();
 	}
 }
