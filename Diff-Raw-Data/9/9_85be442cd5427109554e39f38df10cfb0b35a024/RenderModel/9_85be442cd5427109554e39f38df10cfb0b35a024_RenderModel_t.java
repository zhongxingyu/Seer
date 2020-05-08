 package com.discretesoftworks.framework;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.nio.FloatBuffer;
 import java.nio.ShortBuffer;
 
 import android.opengl.GLES20;
 import android.opengl.Matrix;
 
 
 
 public class RenderModel extends GriddedObject{
 
 	//Added for Textures
 	private FloatBuffer mTextureCoordinates;
 	private int mTextureUniformHandle;
 	private int mTextureCoordinateHandle;
 	private final int mTextureCoordinateDataSize = 2;
 	private int mTextureDataHandle;
 	
 	
     private final String vertexShaderCode =
     	"attribute vec3 a_normal;" +
     	"attribute vec2 a_TexCoordinate;" +
     	"varying vec2 v_TexCoordinate;" +
     	"varying vec4 lightness;" +
     	
     	//Lights
     	"uniform vec4 lightPos;"+
     	"uniform vec4 lightColor;"+
 
         "uniform mat4 uMMatrix;" +
         "uniform mat4 uVPMatrix;" +
 
         "attribute vec4 vPosition;" +
         "void main() {" +
        "  vec3 ambient = vec3(.4,.4,.4);"+
         "  vec4 worldPos = uMMatrix * vPosition;"+
         "  gl_Position = uVPMatrix * worldPos;" +
 		"  v_TexCoordinate = a_TexCoordinate;" +
 		"  vec4 normal = vec4(a_normal,0.0);"+
         "  float distance = length(lightPos-worldPos);"+
         "  float dottedAngle = dot(normalize(lightPos-worldPos),normal);" +
 		"  lightness = (lightColor * dottedAngle) * (1.0 / (1.0 + distance * distance));"+
         "  lightness = vec4(lightness.xyz + ambient,1.0);"+
         "}";
 
     private final String fragmentShaderCode =
         "precision mediump float;" +
         "uniform vec4 vColor;" +
 	    "uniform sampler2D u_Texture;" +
 	    "varying vec2 v_TexCoordinate;" +
 	    "varying vec4 lightness;" +
 	    
         "void main() {" +
         "  gl_FragColor = (lightness * vColor * texture2D(u_Texture, v_TexCoordinate));" +
         "}";
 
     private FloatBuffer vertexBuffer;
     private FloatBuffer normalBuffer;
     private ShortBuffer drawListBuffer;
     private int mProgram;
     private int mPositionHandle;
     private int mNormalHandle;
     private int mColorHandle;
     private int mMMatrixHandle;
     private int mVPMatrixHandle;
     private int lightPosHandle;
     private int lightColorHandle;
 
     // number of coordinates per vertex in this array
     static final int COORDS_PER_VERTEX = 3;
     //private PointF topLeft = new PointF(0,0);
     private float[] mModelMatrix = new float[16];
     private float[] identityMatrix = new float[16];
 //    private float[] mvpMatrix = new float[16];
     private float[] coords; /*= { -0.5f,  0.5f, 0f,
             						 -0.5f, -0.5f, 0f,
             						  0.5f, -0.5f, 0f,
             						  0.5f,  0.5f, 0f }; /*,
             						  0.5f,  0.5f, 1f,
             						  0.5f, -0.5f, 1f,
             						 -0.5f,  0.5f, 1f, 
             						 -0.5f, -0.5f, 1f} ; */
 
     private short[] drawOrder;/* = { 0, 1, 2, 0, 2, 3 }; /*, 2, 3, 4, 4, 5, 2, 5, 4, 6, 5, 7, 6,
     									7, 1, 2, 2, 5, 7, 0, 1, 7, 7, 6, 0}; // order to draw vertices */
 
     private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
     
     private float[] texCoords;
     private float[] normals;
 
     // Set color with red, green, blue and alpha (opacity) values
     float color[] = { .75f, .75f, .75f, 1.0f };
     
     //float translation[] = {0, 0, 0};
     float scale[] = {1, 1, 1};
     float autoScale[] = {1, 1, 1};
     
     private Sprite myTextureSprite;
     private float imageSingle;
     private float imageSpeed;
     
     private boolean hudElement;
     private boolean visible;
     private boolean set;
     
     private float[] dir;
     private float[] newDir;
     private boolean spin;
     
     public RenderModel() {
     	super(0,0,0,1,1);
     	
     	hudElement = false;
     	visible = false;
         
         myTextureSprite = null;
         imageSingle = 0f;
         imageSpeed = 1f;
         mTextureDataHandle = 0;
         
         set = false;
         spin = true;
         newDir = new float[3];
         dir = new float[3];
         newDir[0] = 0f;
         newDir[1] = 0f;
         newDir[2] = 0f;
         dir[0] = 0f;
         dir[1] = 0f;
         dir[2] = 0f;
         
         Matrix.setIdentityM(identityMatrix, 0);
     }
     
     public void setupModel(float[] verts, float[] normalCoords, float[] textureCoords, short[] indicies) {
     	coords = verts;
     	texCoords = textureCoords;
     	drawOrder = indicies;
     	normals = normalCoords;
     	
     	// initialize vertex byte buffer for shape coordinates
         ByteBuffer bb = ByteBuffer.allocateDirect(
         // (# of coordinate values * 4 bytes per float)
                 coords.length * 4);
         bb.order(ByteOrder.nativeOrder());
         vertexBuffer = bb.asFloatBuffer();
         vertexBuffer.put(coords);
         vertexBuffer.position(0);
         
         
         ByteBuffer bn = ByteBuffer.allocateDirect(
         					//#) of normal values * 4 bytes per float
         					normals.length * 4);
         bn.order(ByteOrder.nativeOrder());
         normalBuffer = bn.asFloatBuffer();
         normalBuffer.put(normals);
         normalBuffer.position(0);
         
         // S, T (or X, Y)
         // Texture coordinate data.
         // Because images have a Y axis pointing downward (values increase as you move down the image) while
         // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
         // What's more is that the texture coordinates are the same for every face.
         
         mTextureCoordinates = ByteBuffer.allocateDirect(texCoords.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
         mTextureCoordinates.put(texCoords).position(0);
         
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
     }
     
     public void remakeModelMatrix(){
     	Matrix.setIdentityM(mModelMatrix, 0);
     	Matrix.scaleM(mModelMatrix, 0, scale[0], scale[1], scale[2]);
     	Matrix.scaleM(mModelMatrix, 0, autoScale[0], autoScale[1], autoScale[2]);
     	Matrix.translateM(mModelMatrix, 0, getX(), getY(), getZ());
     	Matrix.rotateM(mModelMatrix, 0, dir[0], 1.0f, 0.0f, 0.0f);
     	Matrix.rotateM(mModelMatrix, 0, dir[1], 0.0f, 1.0f, 0.0f);
     	Matrix.rotateM(mModelMatrix, 0, dir[2], 0.0f, 0.0f, 1.0f);
     }
     
     public void createSquare(float width, float height){
     	width /= 2f;
     	height /= 2f;
     	float[] squareCoords = { -width,  height, 0f,
    			 					 -width, -height, 0f,
    			 					  width, -height, 0f,
    			 					  width,  height, 0f };
     	float[] squareNormals = { 0.0f, 0.0f, 1.0f,
     							  0.0f, 0.0f, 1.0f,
     							  0.0f, 0.0f, 1.0f,
     							  0.0f, 0.0f, 1.0f};
     	float[] textureCoords = { 0.0f,  0.0f,
    	        					  0.0f,  1.0f,
    	        					  1.0f,  1.0f,
    	        					  1.0f,  0.0f };
    	    short[] squareDrawOrder = { 0, 1, 2, 0, 2, 3 };
    	    setupModel(squareCoords, squareNormals, textureCoords, squareDrawOrder);
     }
     
     public void set(float x, float y, float width, float height){
     	setCoordinates(x,y);
     	setDimensions(width,height);
     	imageSingle = 0f;
         imageSpeed = 1f;
         //mTextureDataHandle = mySprite.getSprite(getImageSingle());
         setHudElement(false);
         for (int i = 0; i < 3; i++){
 	        setDir(i, 0);
 	        setNewDir(i, 0);
         }
         set = true;
         visible = true;
     }
     
     public void setSpin(boolean spin){
     	this.spin = spin;
     }
     
     public boolean getSpin(){
     	return spin;
     }
     
     public void setDir(int xyz, float dir){
     	this.dir[xyz] = dir;
     	remakeModelMatrix();
     }
     
     public void setFDir(int xyz, float dir){
     	this.dir[xyz] = dir;
     	setNewDir(xyz, dir);
     	remakeModelMatrix();
     }
     
     public float getDir(int xyz){
     	return dir[xyz];
     }
     
     public void setNewDir(int xyz, float newDir){
     	this.newDir[xyz] = newDir;
     }
     
     public void free(){
     	set = false;
     	visible = false;
     }
     
     public void setSprite(Sprite sprite){
     	this.myTextureSprite = sprite;
     	imageSingle = 0;
     }
     
     public Sprite getSprite(){
     	return myTextureSprite;
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
     
     public void setScale(float sx, float sy, float sz){
     	scale[0] = sx;
     	scale[1] = sy;
     	scale[2] = sz;
     	remakeModelMatrix();
     }
     
     public void resetColor(){
     	setColor(0.75f,
     			 0.75f,
     			 0.75f, 
     			 1.00f );
     }
     
     public void scaleSprite(float widthStart, float widthEnd, float heightStart, float heightEnd){
     	texCoords[0] = widthStart;
     	texCoords[0] = heightEnd;
     	texCoords[0] = widthStart;
     	texCoords[0] = heightStart;
     	texCoords[0] = widthEnd;
     	texCoords[0] = heightStart;
     	texCoords[0] = widthEnd;
     	texCoords[0] = heightEnd;
     	mTextureCoordinates.put(texCoords).position(0);
     }
     
     public void setHudElement(boolean h){
     	this.hudElement = h;
     	if (h)
     		setAutoScaleX(1/GameRenderer.s_instance.getRatio());
     	else
     		setAutoScaleX(1f);
     }
     
     public void setScaleX(float sx){
     	scale[0] = sx;
     }
     
     public void setAutoScaleX(float sx){
     	autoScale[0] = sx;
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
     
     public void increment(){
     	imageSingle += imageSpeed;
     	if (imageSingle >= myTextureSprite.getSpriteLength())
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
     
     public void draw(float[] vpMatrix) {
     	//int width = GameRenderer.s_instance.getScreenWidth()/2;
     	//int height = GameRenderer.s_instance.getScreenHeight()/2;
     	if (set && getVisible() && (getHudElement() || true/* (getRight() >= centerX-width*centerZ/3 && getLeft() <= centerX + width*centerZ/3 && getBottom() >= centerY-height*centerZ/3 && getTop() <= centerY+height*centerZ/3)*/)){
     		increment();
             mTextureDataHandle = myTextureSprite.getSprite(getImageSingle());
 	    	
             //remakeModelMatrix();
             for (int i = 0; i < 3; i++){
 	            if (newDir[i] != dir[i]){
 		            if (Math.abs(newDir[i]-dir[i]) < 20)
 		            	setDir(i, newDir[i]);
 		            else if ((newDir[i] > dir[i] && Math.abs(newDir[i]-dir[i]) < 180) || (newDir[i] < dir[i] && Math.abs(newDir[i]-dir[i]) > 180))
 		            	setDir(i, dir[i]+10);
 		            else if ((newDir[i] < dir[i] && Math.abs(newDir[i]-dir[i]) < 180) || (newDir[i] > dir[i] && Math.abs(newDir[i]-dir[i]) > 180))
 		            	setDir(i, dir[i]-10);
 	            }
 		        if (dir[i] > 180)
 		          	dir[i] -= 360;
 		        else if (dir[i] < -180)
 		           	dir[i] += 360;
             }
 
 	        // Add program to OpenGL environment
 	        GLES20.glUseProgram(mProgram);
 	        MyGLRenderer.checkGlError("glGetUniformLocation");
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
 	        
 	        //Normals
 	        mNormalHandle = GLES20.glGetAttribLocation(mProgram, "a_normal");
 	        normalBuffer.position(0);
 	        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, 0, normalBuffer);
 	        GLES20.glEnableVertexAttribArray(mNormalHandle);
 	        
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
 	        
 	        //Pass in lights
 	        lightPosHandle = GLES20.glGetUniformLocation(mProgram, "lightPos");
 	        lightColorHandle = GLES20.glGetUniformLocation(mProgram, "lightColor");
 	        GLES20.glUniform4f(lightPosHandle, 50.0f, 50.0f, 500.0f, 1.0f);
 	        GLES20.glUniform4f(lightColorHandle, 300000.0f, 300000.0f, 300000.0f, 1.0f);
 	
 	        // get handle to shape's transformation matrixs
 	        mMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMMatrix");
 	        mVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uVPMatrix");
 	        MyGLRenderer.checkGlError("glGetUniformLocation");
 	        
 	        // Apply the projection and view transformation
 	        if (!getHudElement()) {
 	        	GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mModelMatrix, 0);
 	        	GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, vpMatrix, 0);
 	        } else {
 	        	GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mModelMatrix, 0);
 	        	GLES20.glUniformMatrix4fv(mVPMatrixHandle, 1, false, identityMatrix, 0);
 	        }
 	        MyGLRenderer.checkGlError("glUniformMatrix4fv");
 	
 	        // Draw the square
 	        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length,
 	                              GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
 	        
 	        // Disable vertex array
 	        GLES20.glDisableVertexAttribArray(mPositionHandle);
 	        GLES20.glDisableVertexAttribArray(mNormalHandle);
 	        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
     	}
     }
     
     
 }
 
