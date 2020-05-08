 package com.discretesoftworks.framework;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import android.graphics.PointF;
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 
 
 
 public class RenderModel extends GriddedObject{
 
 	//Added for Textures
 	private final FloatBuffer mTextureCoordinates;
 	private int mTextureUniformHandle;
 	private int mTextureCoordinateHandle;
 	private final int mTextureCoordinateDataSize = 2;
 	private int mTextureDataHandle;
 	
 	
     private final String vertexShaderCode =
    		//Test
     	"attribute vec2 a_TexCoordinate;" +
     	"varying vec2 v_TexCoordinate;" +
     	//End Test
         // This matrix member variable provides a hook to manipulate
         // the coordinates of the objects that use this vertex shader
         "uniform mat4 uMVPMatrix;" +
 
         "attribute vec4 vPosition;" +
         "void main() {" +
         // the matrix must be included as a modifier of gl_Position
         "  gl_Position = vPosition * uMVPMatrix;" +
 		      //Test
 		      "v_TexCoordinate = a_TexCoordinate;" +
 		      //End Test
         "}";
 
     private final String fragmentShaderCode =
         "precision mediump float;" +
         "uniform vec4 vColor;" +
 	      //Test
 	      "uniform sampler2D u_Texture;" +
 	      "varying vec2 v_TexCoordinate;" +
 	      //End Test
         "void main() {" +
         //"  gl_FragColor = vColor;" +
         "gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));" +
         "}";
 
     private FloatBuffer vertexBuffer;
     private final ShortBuffer drawListBuffer;
     private final int mProgram;
     private int mPositionHandle;
     private int mColorHandle;
     private int mMVPMatrixHandle;
 
     // number of coordinates per vertex in this array
 
     static final int COORDS_PER_VERTEX = 3;
     private PointF topLeft = new PointF(0,0);
     private float[] mModelMatrix = new float[16];
     private float squareCoords[] = { -0.5f,  0.5f, 0f,
             						 -0.5f, -0.5f, 0f,
             						  0.5f, -0.5f, 0f,
             						  0.5f,  0.5f, 0f } ;
 
     private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
 
     private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
     
     private float[] mTextureCoordinateData = {	0.0f,  0.0f,
 									        	0.0f,  1.0f,
 									            1.0f,  1.0f,
 									            1.0f,  0.0f };
 
     // Set color with red, green, blue and alpha (opacity) values
     float color[] = { .75f, .75f, .75f, 1.0f };
     
     float translation[] = {0, 0, 0};
     float scale[] = {1, 1, 1};
     
     private Sprite mySprite;
     private float imageSingle;
     private float imageSpeed;
     
     private boolean hudElement;
     private boolean visible;
     private boolean set;
     
     public RenderModel() {
     	super(0,0,1,1);
     	
     	hudElement = false;
     	visible = false;
     	
     	
     	GameRenderer.s_instance.setSurfaceCreated(false);
     	
     	// initialize vertex byte buffer for shape coordinates
         ByteBuffer bb = ByteBuffer.allocateDirect(
         // (# of coordinate values * 4 bytes per float)
                 squareCoords.length * 4);
         bb.order(ByteOrder.nativeOrder());
         vertexBuffer = bb.asFloatBuffer();
         setSize(squareCoords);
         
         
         // S, T (or X, Y)
         // Texture coordinate data.
         // Because images have a Y axis pointing downward (values increase as you move down the image) while
         // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
         // What's more is that the texture coordinates are the same for every face.
         
         mTextureCoordinates = ByteBuffer.allocateDirect(mTextureCoordinateData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
         mTextureCoordinates.put(mTextureCoordinateData).position(0);
         
         // initialize byte buffer for the draw list
         ByteBuffer dlb = ByteBuffer.allocateDirect(
         // (# of coordinate values * 2 bytes per short)
                 drawOrder.length * 2);
         dlb.order(ByteOrder.nativeOrder());
         drawListBuffer = dlb.asShortBuffer();
         drawListBuffer.put(drawOrder);
         drawListBuffer.position(0);
         
         // prepare shaders and OpenGL program
         int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                                                    vertexShaderCode);
         int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                                                      fragmentShaderCode);
         
         mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
         GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
         GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
         
         //Texture Code
         GLES20.glBindAttribLocation(mProgram, 0, "a_TexCoordinate");
         
         GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
         
         //Load the texture
         mySprite = null;
         imageSingle = 0f;
         imageSpeed = 1f;
         mTextureDataHandle = 0;
         
         set = false;
         
         GameRenderer.s_instance.setSurfaceCreated(true);
     }
     
     public void remakeModelMatrix(){
     	Matrix.setIdentityM(mModelMatrix, 0);
     	//Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
     	mModelMatrix[3]  = getX();
     	mModelMatrix[7]  = getY();
     	mModelMatrix[11] = 0f;
     }
     
     public void set(float x, float y, float width, float height, Sprite mySprite){
     	setCoordinates(x,y);
     	setDimensions(width,height);
     	this.mySprite = mySprite;
     	imageSingle = 0f;
         imageSpeed = 1f;
         mTextureDataHandle = mySprite.getSprite(getImageSingle());
         set = true;
         visible = true;
     	
     }
     
     public void free(){
     	set = false;
     	visible = false;
     }
     
     public void setSprite(Sprite sprite){
     	this.mySprite = sprite;
     	imageSingle = 0;
     }
     
     public Sprite getSprite(){
     	return mySprite;
     }
     
     public int getImageSingle(){
     	return (int)imageSingle;
     }
     
     public void setColor(float red, float green, float blue, float alpha){
     	this.color[0] = red;
     	this.color[1] = green;
     	this.color[2] = blue;
     	this.color[3] = alpha;
     }
     
     public void resetColor(){
     	setColor(0.75f,
     			 0.75f,
     			 0.75f, 
     			 1.00f );
     }
     
     public void scaleSprite(float widthStart, float widthEnd, float heightStart, float heightEnd){
     	mTextureCoordinateData[0] = widthStart;
     	mTextureCoordinateData[0] = heightEnd;
     	mTextureCoordinateData[0] = widthStart;
     	mTextureCoordinateData[0] = heightStart;
     	mTextureCoordinateData[0] = widthEnd;
     	mTextureCoordinateData[0] = heightStart;
     	mTextureCoordinateData[0] = widthEnd;
     	mTextureCoordinateData[0] = heightEnd;
     	mTextureCoordinates.put(mTextureCoordinateData).position(0);
     }
     
     public void setSize(float[] squareCoords){
         vertexBuffer.put(squareCoords).position(0);
     }
     
     public void setHudElement(boolean h){
     	this.hudElement = h;
     }
     
     public boolean getHudElement(){
     	return hudElement;
     }
     
     public void setVisible(boolean visible){
     	this.visible = visible;
     }
     
     public boolean getVisible(){
     	return visible;
     }
     
     public float getZ(){
     	return 0f;
     }
 
     public float[] getCoords(int cX, int cY){
     	float x = getX();
     	float y = getY();
     	if (getHudElement()){
     		x = (x * GameRenderer.s_instance.getViewScale());
         	y = (y * GameRenderer.s_instance.getViewScale());
     		x = cX - (GameRenderer.s_instance.getViewWidth() /2.0f) + x;
     		y = cY - (GameRenderer.s_instance.getViewHeight()/2.0f) + y;
     	}
 
     	topLeft.x = ( x-cX)/(GameRenderer.s_instance.getScreenHeight()/2.0f);
     	topLeft.y = (-y+cY)/(GameRenderer.s_instance.getScreenHeight()/2.0f);
     	float h = GameRenderer.s_instance.getScreenHeight();
     	float wScale = 2*(getWidth()/(h));
     	float hScale = 2*(getHeight()/(h));
     	if (getHudElement()){
     		wScale *= GameRenderer.s_instance.getViewScale();
     		hScale *= GameRenderer.s_instance.getViewScale();
     	}
     	wScale /= 2;
 		hScale /= 2;
     	squareCoords[0] = (float)(topLeft.x-wScale);
     	squareCoords[1] = (float)(topLeft.y+hScale);
     	squareCoords[2] = getZ();
     	squareCoords[3] = (float)(topLeft.x-wScale);
     	squareCoords[4] = (float)(topLeft.y-hScale);
     	squareCoords[5] = getZ();
     	squareCoords[6] = (float)(topLeft.x+wScale);
     	squareCoords[7] = (float)(topLeft.y-hScale);
     	squareCoords[8] = getZ();
     	squareCoords[9] = (float)(topLeft.x+wScale);
     	squareCoords[10] = (float)(topLeft.y+hScale);
     	squareCoords[11] = getZ();
     	return squareCoords;
     }
     
     public void increment(){
     	imageSingle += imageSpeed;
     	if (imageSingle >= mySprite.getSpriteLength())
     		imageSingle = 0;
     }
     
     public void setImageSingle(int single){
     	imageSingle = single;
     }
     
     public float getImageSpeed(){
     	return imageSpeed;
     }
     
     public void setImageSpeed(float speed){
     	imageSpeed = speed;
     }
     
     public void draw(float[] vpMatrix, float centerX, float centerY, float centerZ) {
     	//int width = GameRenderer.s_instance.getScreenWidth()/2;
     	//int height = GameRenderer.s_instance.getScreenHeight()/2;
     	if (set && getVisible() && (getHudElement() || true/* (getRight() >= centerX-width*centerZ/3 && getLeft() <= centerX + width*centerZ/3 && getBottom() >= centerY-height*centerZ/3 && getTop() <= centerY+height*centerZ/3)*/)){
     		increment();
             mTextureDataHandle = mySprite.getSprite(getImageSingle());
             
             remakeModelMatrix();
     		
 	    	
 	        // Add program to OpenGL environment
 	        GLES20.glUseProgram(mProgram);
 	
 	        // get handle to vertex shader's vPosition member
 	        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
 	
 	        // Enable a handle to the triangle vertices
 	        GLES20.glEnableVertexAttribArray(mPositionHandle);
 	
 	        // Prepare the triangle coordinate data
 	        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
 	                                     GLES20.GL_FLOAT, false,
 	                                     vertexStride, vertexBuffer);
 	
 	        // get handle to fragment shader's vColor member
 	        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
 	
 	        // Set color for drawing the triangle
 	        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
 	        
 	        /* */
 	        //Set Texture Handles and bind Texture
 	        mTextureUniformHandle = GLES20.glGetAttribLocation(mProgram, "u_Texture");
 	        mTextureCoordinateHandle = GLES20.glGetAttribLocation(mProgram, "a_TexCoordinate");
 
 	        //Set the active texture unit to texture unit 0.
 	        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
 
 	        //Bind the texture to this unit.
 	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);
 
 	        //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
 	        GLES20.glUniform1i(mTextureUniformHandle, 0); 
 
 	        //Pass in the texture coordinate information
 	        mTextureCoordinates.position(0);
 	        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, mTextureCoordinateDataSize, GLES20.GL_FLOAT, false, 0, mTextureCoordinates);
 	        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
 	        /* */
 	        
 	        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
 	        GLES20.glEnable(GLES20.GL_BLEND);
 	        
 	
 	        // get handle to shape's transformation matrix
 	        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
 	        MyGLRenderer.checkGlError("glGetUniformLocation");
	
	        float[] mvpMatrix = new float[16];
 	        
 	        Matrix.multiplyMM(mvpMatrix, 0, mModelMatrix, 0, vpMatrix, 0);
 	        
 	        // Apply the projection and view transformation
 	        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
 	        MyGLRenderer.checkGlError("glUniformMatrix4fv");
 	
 	        // Draw the square
 	        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
 	                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
 	        
 	        // Disable vertex array
 	        GLES20.glDisableVertexAttribArray(mPositionHandle);
     	}
     }
     
     
 }
 
