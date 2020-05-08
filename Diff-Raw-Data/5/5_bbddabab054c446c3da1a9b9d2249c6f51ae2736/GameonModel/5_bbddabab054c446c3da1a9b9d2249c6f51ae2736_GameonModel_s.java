 /*
    Copyright 2012, Telum Slavonski Brod, Croatia.
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    
    This file is part of QFramework project, and can be used only as part of project.
    Should be used for peace, not war :)   
 */
 
 package com.qframework.core;
 
 import com.qframework.core.GameonModel.RefId;
 import com.qframework.core.LayoutArea.State;
 
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 import java.util.Vector;
 
 import javax.media.opengl.GL2;
 
 // TODO 
 // model creation , from formula? and parameters?
 // different geometry shapes
 // difrerent mapping
 // all programatically 
 
 public class GameonModel extends GLModel{
 	
 	private Vector<GameonModelRef> mRefs = new Vector<GameonModelRef>();
 	private Vector<GameonModelRef> mVisibleRefs = new Vector<GameonModelRef>();
 	protected int mSubmodels  = 0;
 	protected GameonModelData.Type mModelTemplate = GameonModelData.Type.NONE;
 	protected boolean mHasAlpha = false;	
 	protected boolean mIsModel = false;
 	private GameonWorld mWorld;
 	private boolean mActive = true;
 	private Vector<Integer> mIterQueue;
 	protected	LayoutArea	mParentArea;
 	protected	String		mOnClick;
 	private TextureFactory.MaterialData mCurrentMaterial;
 	
 	private static float mStaticBoundsPlane[] =  { 
 		-0.5f,-0.5f,0.0f,1.0f,
 		0.5f,-0.5f,0.0f,1.0f,
 		-0.5f,0.5f,0.0f,1.0f,
 		 0.5f,0.5f,0.0f,1.0f };
 	
 	private static float mStaticBounds[] =  { 
 		0.0f,0.0f,0.0f,1.0f,
 		0.0f,0.0f,0.0f,1.0f,
 		0.0f,0.0f,0.0f,1.0f,
 		0.0f,0.0f,0.0f,1.0f };	
 
 	static public class RefId
 	{
 		String name;
 		String alias;
 		int id;
 	}
 
 	
 	public GameonModel(String name, GameonApp app, LayoutArea parentarea) {
 		super(name , app);
 		mApp = app;
 		mWorld = mApp.world();
 		mParentArea = parentarea;
 	}
     public void createModel2(GameonModelData.Type type, float left, float bottom, float back, 
     		float right, float top, float front,
     		int textid) {
     	float ratiox = right - left;
     	float ratioy = front - bottom;
     	float ratioz = top - back;    	
     	
     	float data[][] = GameonModelData.getData(type);
     	
     	GLColor color = mApp.colors().white;
     	// model info - vertex offset?
     	int len = data.length;
     	GLShape shape = new GLShape(this);
     	float xmid = (right + left) /2;
     	float ymid = (top + bottom) /2;
     	float zmid = (front + back) /2;
 
     	for (int a=0; a< len; a+=9 ) {
     		float vx1 = data[a][0];
     		float vy1 = data[a][1];
     		float vz1 = data[a][2];
     		
     		float tu1 = data[a+2][0];
     		float tv1 = 1.0f- data[a+2][1];    		
     		vx1 *= ratiox; vx1 += xmid;
     		vy1 *= ratioy; vy1 += ymid;
     		vz1 *= ratioz; vz1 += zmid;
     		GLVertex v1 = shape.addVertex(vx1, vy1, vz1, tu1, tv1, color);
     		
     		float vx2 = data[a+3][0];
     		float vy2 = data[a+3][1];
     		float vz2 = data[a+3][2];
     		
     		float tu2 = data[a+5][0];
     		float tv2 = 1.0f- data[a+5][1];    		
     		
     		vx2 *= ratiox; vx2 += xmid;
     		vy2 *= ratioy; vy2 += ymid;
     		vz2 *= ratioz; vz2 += zmid;
     		GLVertex v2 = shape.addVertex(vx2, vy2, vz2, tu2, tv2, color);
 
     		float vx3 = data[a+6][0];
     		float vy3 = data[a+6][1];
     		float vz3 = data[a+6][2];
     		
     		float tu3 = data[a+8][0];
     		float tv3 = 1.0f- data[a+8][1];    		
     		
     		vx3 *= ratiox; vx3 += xmid;
     		vy3 *= ratioy; vy3 += ymid;
     		vz3 *= ratioz; vz3 += zmid;
     		GLVertex v3 = shape.addVertex(vx3, vy3, vz3, tu3, tv3, color);    		
     		
     		shape.addFace( new GLFace(v1,v2,v3));
 
     	}
     	
 		addShape(shape);
 		mTextureID = textid;
     
     }
     public void createModel(GameonModelData.Type type,
     		int textid, GLColor color, float[] grid) {
     	
     	float data[][] = GameonModelData.getData(type);
     	// model info - vertex offset?
     	int len = data.length;
     	
     	
     	float divx = 1; 
     	float divy = 1;
     	float divz = 1;
     	float countx = 1; 
     	float county = 1;
     	float countz = 1;
     	
     	if (grid != null)
     	{
     		divx = 1 / grid[0];
     		divy = 1 / grid[1];
     		divz = 1 / grid[2];
     		countx = grid[0]*2; 
         	county = grid[1]*2;
         	countz = grid[2]*2;
     		
     	}
     	
     	for (float x = 1.0f; x <= countx; x+= 2)
     	{
         	for (float y = 1.0f; y <= county; y+= 2)
         	{    		
             	for (float z = 1.0f; z <= countz; z+= 2)
             	{           		
     	
 			    	GLShape shape = new GLShape(this);
 			    	for (int a=0; a< len; a+=9 ) {
 			    		float vx1 = data[a][0] * divx+ -0.5f + divx*x/2;
 			    		float vy1 = data[a][1] * divy+ -0.5f + divy*y/2;
 			    		float vz1 = data[a][2] * divz+ -0.5f + divz*z/2;
 			    		
 			    		float tu1 = data[a+2][0];
 			    		float tv1 = 1.0f - data[a+2][1];    		
 			    		GLVertex v1 = shape.addVertex(vx1, vy1, vz1, tu1, tv1, color);
 			    		
 			    		float vx2 = data[a+3][0] * divx+ -0.5f + divx*x/2;
 			    		float vy2 = data[a+3][1] * divy+ -0.5f + divy*y/2;
 			    		float vz2 = data[a+3][2] * divz+ -0.5f + divz*z/2;
 			    		
 			    		float tu2 = data[a+5][0];
 			    		float tv2 = 1.0f - data[a+5][1];    		
 			    		
 			    		GLVertex v2 = shape.addVertex(vx2, vy2, vz2, tu2, tv2, color);
 			
 			    		float vx3 = data[a+6][0] * divx+ -0.5f + divx*x/2;
 			    		float vy3 = data[a+6][1] * divy+ -0.5f + divy*y/2;
 			    		float vz3 = data[a+6][2] * divz+ -0.5f + divz*z/2;
 			    		
 			    		float tu3 = data[a+8][0];
 			    		float tv3 = 1.0f - data[a+8][1];    		
 			    		
 			    		GLVertex v3 = shape.addVertex(vx3, vy3, vz3, tu3, tv3, color);    		
 			    		
 			    		shape.addFace( new GLFace(v1,v2,v3));
 			    	}
 			    	addShape(shape);
 		    	}
 	    	}
     	
     	}
 		
 		mTextureID = textid;
     
     }
     
     public void createModel3(GameonModelData.Type type, float left, float bottom, float back, 
     		float right, float top, float front,
     		GLColor color)
     {
     	float ratiox = right - left;
     	float ratioy = front - bottom;
     	float ratioz = top - back;    	
     	
     	float data[][] = GameonModelData.getData(type);
     	// model info - vertex offset?
     	int len = data.length;
     	GLShape shape = new GLShape(this);
     	float xmid = (right + left) /2;
     	float ymid = (top + bottom) /2;
     	float zmid = (front + back) /2;
 
     	for (int a=0; a< len; a+=9 ) {
     		float vx1 = data[a][0];
     		float vy1 = data[a][1];
     		float vz1 = data[a][2];
     		
     		vx1 *= ratiox; vx1 += xmid;
     		vy1 *= ratioy; vy1 += ymid;
     		vz1 *= ratioz; vz1 += zmid;
     		float tu1 = data[a+2][0];
     		float tv1 = 1.0f - data[a+2][1];    		
     		GLVertex v1 = shape.addVertex(vx1, vy1, vz1 , tu1, tv1, color);
     		
     		float vx2 = data[a+3][0];
     		float vy2 = data[a+3][1];
     		float vz2 = data[a+3][2];
     		
     		vx2 *= ratiox; vx2 += xmid;
     		vy2 *= ratioy; vy2 += ymid;
     		vz2 *= ratioz; vz2 += zmid;
     		float tu2 = data[a+5][0];
     		float tv2 = 1.0f - data[a+5][1];    		
     		GLVertex v2 = shape.addVertex(vx2, vy2, vz2, tu2 , tv2, color);
 
     		float vx3 = data[a+6][0];
     		float vy3 = data[a+6][1];
     		float vz3 = data[a+6][2];
     		
     		vx3 *= ratiox; vx3 += xmid;
     		vy3 *= ratioy; vy3 += ymid;
     		vz3 *= ratioz; vz3 += zmid;
     		float tu3 = data[a+8][0];
     		float tv3 = 1.0f - data[a+8][1];    		
     		GLVertex v3 = shape.addVertex(vx3, vy3, vz3, tu3, tv3, color);    		
     		
     		shape.addFace( new GLFace(v1,v2,v3));
 
     	}
 
 		addShape(shape);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
     }
     public void createOctogon(float left, float bottom, float back, float right, float top, float front, GLColor color)  
     {
     	GLShape shape = new GLShape(this);
     	float divx = (right - left ) / 4;
     	float divy = (top - bottom ) / 4;
     	
     	GLVertex p1 = shape.addVertex(left + divx, top, front , 0.0f , 1.00f, color);
     	GLVertex p2 = shape.addVertex(left + 3 * divx, top, front , 0.0f , 1.00f, color);
     	GLVertex p3 = shape.addVertex(right,  bottom + 3 * divy, front , 0.0f , 1.00f, color);
     	GLVertex p4 = shape.addVertex(right,  bottom +  divy, front , 0.0f , 1.00f, color);
     	GLVertex p5 = shape.addVertex(left + 3*divx, bottom, front , 0.0f , 1.00f, color);
     	GLVertex p6 = shape.addVertex(left + divx, bottom, front , 0.0f , 1.00f, color);
     	GLVertex p7 = shape.addVertex(left , bottom + divy, front , 0.0f , 1.00f, color);
     	GLVertex p8 = shape.addVertex(left , bottom + 3* divy, front , 0.0f , 1.00f, color);
     	
     	
 
         GLVertex center = shape.addVertex( (right + left) / 2, (top + bottom ) / 2, front, 0.5f,0.5f, mApp.colors().white);
         // front
 
         shape.addFace(new GLFace(center, p1 , p8));
         shape.addFace(new GLFace(center, p2 , p1));
         shape.addFace(new GLFace(center, p3 , p2));
         shape.addFace(new GLFace(center, p4 , p3));
         shape.addFace(new GLFace(center, p5 , p4));
         shape.addFace(new GLFace(center, p6 , p5));
         shape.addFace(new GLFace(center, p7 , p6));
         shape.addFace(new GLFace(center, p8 , p7));
 
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
     }    
     public void createPlane(float left, float bottom, float back, float right, float top, float front, GLColor color, float[] grid)  
     {
     	
     	
     	float divx = 1; 
     	float divy = 1;
     	
     	float w = right-left;
     	float h = top-bottom;
     	
     	if (grid != null)
     	{
     		divx = w / grid[0];
     		divy = h / grid[1];
     	}
     	
     	for (float x = left; x < right; x+= divx)
     	{
         	for (float y = bottom; y < top; y+= divy)
 			{    		
         		GLShape shape = new GLShape(this);
         		/*
 	    		float left2 = left * divx + x;
 	    		float right2 = right * divx + x;
 	    		float top2 = top * divy + y;
 	    		float bottom2 = bottom * divy + y;
 	    		*/
 	    		float left2 = x;
 	    		float right2 = divx + x;
 				if (right2 > right)
 				{
 					right2 = right;
 				}
 				float top2 = divy + y;
 				if (top2 > top)
 				{
 					top2 = top;
 				}				    		
 	    		float bottom2 = y;
 	    		
 	           	GLVertex leftBottomFront = shape.addVertex(left2, bottom2, front , 0.01f , 0.99f, color);
 	            GLVertex rightBottomFront = shape.addVertex(right2, bottom2, front , 0.99f , 0.99f, color);
 	        	GLVertex leftTopFront = shape.addVertex(left2, top2, front , 0.01f , 0.01f, color);
 	            GLVertex rightTopFront = shape.addVertex(right2, top2, front , 0.99f , 0.01f, color);
 	            // front
 	            shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
 	            shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 	
 	            addShape(shape);            		
         	}
     	}
 
     }
     public void createPlane4(float left, float bottom, float back, float right, float top, float front, GLColor color, GLColor color2 )  
     {
     	GLShape shape = new GLShape(this);
     	if (color == null)
     	{
     		color = mApp.colors().white;
     	}
     	if (color2 == null)
     	{
     		color2 = mApp.colors().white;
     	}    	
     	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , 0.0f , 1.00f, color);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , 1.0f , 1.00f, color);
     	GLVertex leftTopFront = shape.addVertex(left, top, front , 0.0f , 0.00f, color2);
         GLVertex rightTopFront = shape.addVertex(right, top, front , 1.00f , 0.00f, color2);
         // front
 
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
 //        shape.setFaceColor(0, color);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
     }    
     public void createPlaneForLetter(float left, float bottom, float back, float right, float top, float front, GLColor color)  
     {
     	GLShape shape = new GLShape(this);
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , 0.00f , 1.00f, color);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , 1.00f , 1.00f, color);
     	GLVertex leftTopFront = shape.addVertex(left, top, front , 0.09f , 0.00f, color);
         GLVertex rightTopFront = shape.addVertex(right, top, front , 1.00f , 0.00f, color);
         // front
 
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
 //        shape.setFaceColor(0, color);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
     }
     public void createPlane2(float left, float bottom, float back, float right, float top, float front, GLColor color)  
     {
     	GLShape shape = new GLShape(this);
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , 0.0f , 1.0f, color);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , 1.0f , 1.0f, color);
     	GLVertex leftTopFront = shape.addVertex(left, top, front , 0.0f , 0.0f, color);
         GLVertex rightTopFront = shape.addVertex(right, top, front , 1.0f , 0.0f, color);
         // front
 
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
 //        shape.setFaceColor(0, color);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
     }
     public void createPlane3(float left, float bottom, float back, float right, float top, float front, GLColor color)  
     {
     	GLShape shape = new GLShape(this);
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , 0.0f , 1.0f, color);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , 1.0f , 1.0f, mApp.colors().white);
     	GLVertex leftTopFront = shape.addVertex(left, top, front , 0.10f , 0.0f, color);
         GLVertex rightTopFront = shape.addVertex(right, top, front , 0.90f , 0.0f, color);
         // front
 
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
 //        shape.setFaceColor(0, color);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
     }    
 	public void createCube(float left, float bottom, float back, float right, float top, float front, GLColor color) {
 		GLShape shape = new GLShape(this);
 		
 		GLColor white = mApp.colors().white;
 		
 		GLVertex leftBottomBack = shape.addVertex(left, bottom, back , 0 , 0, white);
         GLVertex rightBottomBack = shape.addVertex(right, bottom, back , 0 , 0, white);
        	GLVertex leftTopBack = shape.addVertex(left, top, back , 0 , 0, white);
         GLVertex rightTopBack = shape.addVertex(right, top, back , 0 , 0, white);
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , 0 , 0, color);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , 0 , 0, color);
        	GLVertex leftTopFront = shape.addVertex(left, top, front , 0 , 0, color);
         GLVertex rightTopFront = shape.addVertex(right, top, front , 0 , 0, white );//color);
 
         
         // vertices are added in a clockwise orientation (when viewed from the outside)
         // bottom
         shape.addFace(new GLFace(leftBottomBack, rightBottomFront ,leftBottomFront ));
         shape.addFace(new GLFace(leftBottomBack, rightBottomBack , rightBottomFront ));
 
         // front
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront ));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
         // left
         shape.addFace(new GLFace(leftBottomBack, leftTopFront , leftTopBack ));
         shape.addFace(new GLFace(leftBottomBack, leftBottomFront , leftTopFront ));
 
         // right
         shape.addFace(new GLFace(rightBottomBack, rightTopFront , rightBottomFront  ));
         shape.addFace(new GLFace(rightBottomBack, rightTopBack , rightTopFront ));
 
         // back
         shape.addFace(new GLFace(leftBottomBack, rightTopBack , rightBottomBack  ));
         shape.addFace(new GLFace(leftBottomBack,  leftTopBack , rightTopBack ));
 
         // top
         shape.addFace(new GLFace(leftTopBack, rightTopFront , rightTopBack));
         shape.addFace(new GLFace(leftTopBack, leftTopFront , rightTopFront ));
 
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
 	}
 
 	public void addref(GameonModelRef ref)
 	{
 		if (mRefs.indexOf(ref) < 0)
 		{
 			mRefs.add(ref);
 			ref.set();
 			mEnabled = true;
 		}
 		ref.setParent(this);
 		if (ref.getVisible())
 		{
 			addVisibleRef(ref);
 		}
 		
 	}
 	
 	public void draw(GL2 gl, int loc)
     {
 		if (!mEnabled) {
 			return;
 		}
 		int len = mVisibleRefs.size();
 		if (len > 0) {
 			//setupRef(gl);
 			boolean initRef = true;
 			for (int a=0; a<mVisibleRefs.size() ; a++)
 			{
 				GameonModelRef ref = mVisibleRefs.get(a);
 				if (ref.loc() == loc ) {
 					//setupRefV(gl);
 					initRef = drawRef( gl, ref , initRef);
 				}
 			}
 		}
 	}
 	public void removeref(GameonModelRef ref) {
 		if (mRefs.indexOf(ref) >= 0)
 		{
 			mRefs.remove(ref);
 			if (mRefs.size() == 0)
 		{
 				mEnabled = false;
 			}
 		}
     }
 	public void setTexture(int i) {
 		this.mTextureID = i;
 		
 	}
 
     public void createCard(float left, float bottom, float back, float right, float top, float front, GLColor color)  
     {
     	GLShape shape = new GLShape(this);
     	float t = 0.002f;
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front+t , 0.0f , 0.00f, color);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front+t , 1.0f , 0.00f, color);
        	GLVertex leftBottom2Front = shape.addVertex(left, (bottom+top)/2, front+t , 0.0f , 1.0f, color);
         GLVertex rightBottom2Front = shape.addVertex(right, (bottom+top)/2, front+t , 1.0f ,1.0f, color);
 
         GLVertex leftTopFront = shape.addVertex(left, top, front+t , 0.0f , 0.00f, color);
         GLVertex rightTopFront = shape.addVertex(right, top, front+t , 1.00f , 0.00f, color);
 
         // front
         shape.addFace(new GLFace(leftBottom2Front, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottom2Front, rightBottom2Front , rightTopFront ));
 
         shape.addFace(new GLFace(leftBottomFront, rightBottom2Front , leftBottom2Front));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightBottom2Front ));
         
         
         //back vertexex
 //        color = mApp.colors().blue;
        	GLVertex leftBottomFront_ = shape.addVertex(left, bottom, front-t , 0.0f , 0.00f, color);
         GLVertex rightBottomFront_ = shape.addVertex(right, bottom, front-t , 1.0f , 0.00f, color);
        	GLVertex leftBottom2Front_ = shape.addVertex(left, (bottom+top)/2, front -t, 0.0f , 1.0f, color);
         GLVertex rightBottom2Front_ = shape.addVertex(right, (bottom+top)/2, front -t, 1.0f ,1.0f, color);
 
         GLVertex leftTopFront_ = shape.addVertex(left, top, front - t, 0.0f , 0.00f, color);
         GLVertex rightTopFront_ = shape.addVertex(right, top, front - t, 1.00f , 0.00f, color);
 
         // back
         shape.addFace(new GLFace(leftBottom2Front_, leftTopFront_, rightTopFront_ ));
         shape.addFace(new GLFace(leftBottom2Front_, rightTopFront_ , rightBottom2Front_ ));
 
         shape.addFace(new GLFace(rightBottom2Front_ ,  leftBottomFront_,leftBottom2Front_));
         shape.addFace(new GLFace(rightBottomFront_, leftBottomFront_,rightBottom2Front_  ));
         
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
         
     }
 	
 
     public void createCard2(float left, float bottom, float back, float right, float top, float front, GLColor color)  
     {
     	GLShape shape = new GLShape(this);
     	float w = (right-left) / 30;    
     	float t =0.002f;
        	GLVertex leftBottomFront = shape.addVertex(left+w, bottom+w, front +t, 0.01f , 0.99f, color);
         GLVertex rightBottomFront = shape.addVertex(right-w, bottom+w, front+t , 0.99f , 0.99f, color);
     	GLVertex leftTopFront = shape.addVertex(left+w, top-w, front+t , 0.01f , 0.01f, color);
         GLVertex rightTopFront = shape.addVertex(right-w, top-w, front+t , 0.99f , 0.01f, color);
         // front
 
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
         GLVertex leftBottomFront_ = shape.addVertex(left+w, bottom+w, front -t,  0.99f , 0.99f,color);
         GLVertex rightBottomFront_ = shape.addVertex(right-w, bottom+w, front-t , 0.01f , 0.99f, color);
     	GLVertex leftTopFront_ = shape.addVertex(left+w, top-w, front-t ,  0.99f , 0.01f,color);
         GLVertex rightTopFront_ = shape.addVertex(right-w, top-w, front-t , 0.01f , 0.01f, color);        
         shape.addFace(new GLFace(leftBottomFront_, leftTopFront_, rightTopFront_ ));
         shape.addFace(new GLFace(leftBottomFront_, rightTopFront_, rightBottomFront_  ));
         
         //        shape.setFaceColor(0, color);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
         addShape(shape);
         
     }
 	public void setState(State state) {
 		if (!mActive && state == State.VISIBLE)
 			return;
 		
 		for (int a=0; a< mRefs.size(); a++)
 		{
 			GameonModelRef ref = mRefs.elementAt(a);
 			if (state == State.HIDDEN)
 			{
 				ref.setVisible(false);
 			}else
 			{
 				ref.setVisible(true);
 			}
 		}
 	}
     
 	
     public void createFrame(float left, float bottom, float back, float right, float top, float front, float fw, float fh, GLColor color)  
     {
     	GLColor c;
     	if (color == null)
     	{
     		c = mApp.colors().white;
     	}
     	else
     	{
     		c = color;
     	}
     	
     	createPlane(left-fw/2,bottom-fh/2,front   ,  left+fw/2, top+fh/2,front, color, null);
     	createPlane(right-fw/2,bottom-fh/2,front   ,  right+fw/2, top+fh/2,front, color, null);
     	
     	createPlane(left+fw/2,bottom-fh/2,front   ,  right-fw/2, bottom+fh/2,front, color, null);
     	createPlane(left+fw/2,top-fh/2,front   ,  right-fw/2, top+fh/2,front, color, null);
     }
     
 
     public void createPlaneTex(float left, float bottom, float back, float right, float top, float front, 
     		float tu1, float tv1 , float tu2, float tv2, GLColor[] colors, float no, float div)  
     {
     	GLShape shape = new GLShape(this);
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , tu1 , tv2, mApp.colors().white);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , tu2 , tv2, mApp.colors().white);
     	GLVertex leftTopFront = shape.addVertex(left, top, front , tu1 , tv1, mApp.colors().white);
         GLVertex rightTopFront = shape.addVertex(right, top, front , tu2 , tv1, mApp.colors().white);
 
         float val1 = no * div;
         float val2 = (no+1) * div;
         
         leftBottomFront.red = colors[0].red + (int)((colors[2].red-colors[0].red) * val1);
         leftBottomFront.green = colors[0].green + (int)((colors[2].green-colors[0].green) * val1);
         leftBottomFront.blue = colors[0].blue + (int)((colors[2].blue-colors[0].blue) * val1);
         leftBottomFront.alpha = colors[0].alpha + (int)((colors[2].alpha-colors[0].alpha) * val1);
 
         rightBottomFront.red = colors[1].red + (int)((colors[3].red-colors[1].red) * val1);
         rightBottomFront.green = colors[1].green + (int)((colors[3].green-colors[1].green) * val1);
         rightBottomFront.blue = colors[1].blue + (int)((colors[3].blue-colors[1].blue) * val1);
         rightBottomFront.alpha = colors[1].alpha + (int)((colors[3].alpha-colors[1].alpha) * val1);
 
         leftTopFront.red = colors[0].red + (int)((colors[2].red-colors[0].red) * val2);
         leftTopFront.green = colors[0].green + (int)((colors[2].green-colors[0].green) * val2);
         leftTopFront.blue = colors[0].blue + (int)((colors[2].blue-colors[0].blue) * val2);
         leftTopFront.alpha = colors[0].alpha + (int)((colors[2].alpha-colors[0].alpha) * val2);
 
         rightTopFront.red = colors[1].red + (int)((colors[3].red-colors[1].red) * val2);
         rightTopFront.green = colors[1].green + (int)((colors[3].green-colors[1].green) * val2);
         rightTopFront.blue = colors[1].blue + (int)((colors[3].blue-colors[1].blue) * val2);
         rightTopFront.alpha = colors[1].alpha + (int)((colors[3].alpha-colors[1].alpha) * val2);
 
         // front
         //shape.addFace(new GLFace(leftBottomFront, leftTopFront, rightTopFront, rightBottomFront));
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
         addShape(shape);
     }
 
     public void createPlaneTex2(float left, float bottom, float back, float right, float top, float front, 
     		float tu1, float tv1 , float tu2, float tv2, GLColor[] colors, float no, float div)  
     {
     	GLShape shape = new GLShape(this);
        	GLVertex leftBottomFront = shape.addVertex(left, bottom, front , tu1 , tv2, mApp.colors().white);
         GLVertex rightBottomFront = shape.addVertex(right, bottom, front , tu2 , tv2, mApp.colors().white);
     	GLVertex leftTopFront = shape.addVertex(left, top, front , tu1 , tv1, mApp.colors().white);
         GLVertex rightTopFront = shape.addVertex(right, top, front , tu2 , tv1, mApp.colors().white);
 
         float val1 = no * div;
         float val2 = (no+1) * div;
         
         leftBottomFront.red = colors[0].red + (int)((colors[2].red-colors[0].red) * val1);
         leftBottomFront.green = colors[0].green + (int)((colors[2].green-colors[0].green) * val1);
         leftBottomFront.blue = colors[0].blue + (int)((colors[2].blue-colors[0].blue) * val1);
         leftBottomFront.alpha = colors[0].alpha + (int)((colors[2].alpha-colors[0].alpha) * val1);
 
         rightBottomFront.red = colors[0].red + (int)((colors[2].red-colors[0].red) * val2);
         rightBottomFront.green = colors[0].green + (int)((colors[2].green-colors[0].green) * val2);
         rightBottomFront.blue = colors[0].blue + (int)((colors[2].blue-colors[0].blue) * val2);
         rightBottomFront.alpha = colors[0].alpha + (int)((colors[2].alpha-colors[0].alpha) * val2);
 
         leftTopFront.red = colors[1].red + (int)((colors[3].red-colors[1].red) * val1); 
         leftTopFront.green = colors[1].green + (int)((colors[3].green-colors[1].green) * val1);
         leftTopFront.blue = colors[1].blue + (int)((colors[3].blue-colors[1].blue) * val1);
         leftTopFront.alpha = colors[1].alpha + (int)((colors[3].alpha-colors[1].alpha) * val1);
 
         rightTopFront.red = colors[1].red + (int)((colors[3].red-colors[1].red) * val2);
         rightTopFront.green = colors[1].green + (int)((colors[3].green-colors[1].green) * val2);
         rightTopFront.blue = colors[1].blue + (int)((colors[3].blue-colors[1].blue) * val2);
         rightTopFront.alpha = colors[1].alpha + (int)((colors[3].alpha-colors[1].alpha) * val2);
 
         // front
         //shape.addFace(new GLFace(leftBottomFront, leftTopFront, rightTopFront, rightBottomFront));
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
         addShape(shape);
     }
     
     public void createAnimTrans(String type , int delay, boolean away , int no)
     {
         GameonModelRef to = new GameonModelRef(null, -1); 
         to.copy( mRefs.get(no) );
         to.copyMat( mRefs.get(no) );
         GameonModelRef from = new GameonModelRef(null, -1);
         from.copy(to);
         
         float w,h,x,y;
         RenderDomain domain = mApp.world().getDomain(to.loc());
     	w = domain.mCS.worldWidth();
     	h = domain.mCS.worldHeight();
     
     	x = domain.mCS.worldCenterX();
     	y = domain.mCS.worldCenterY();
 
         if (type.equals("left"))
         {
         	from.addAreaPosition(-w , 0 , 0);	
         }else if (type.equals("right"))
         {
         	from.addAreaPosition(w , 0 , 0);
         }else if (type.equals("top"))
         {
         	from.addAreaPosition(0  , +h , 0);
         }else if (type.equals("tophigh"))
         {
         	from.addAreaPosition(0  , +h+h , 0);
         }else if (type.equals("bottom"))
         {
         	from.addAreaPosition(0  , -h , 0);
         }else if (type.equals("scaleout"))
         {
         	from.mulScale( 30,30 ,30);
         }else if (type.equals("scalein"))
         {
         	from.mulScale( 30, 30 , 30);
         }else if (type.equals("swirlin"))
         {
         	from.mulScale( 30, 30 , 30);
         	from.addAreaRotation( 0, 0, 720);
         }else if (type.equals("swirlout"))
         {
         	from.mulScale( 30, 30 , 30);
         	from.addAreaRotation( 0, 0, 720);
         }
         
                                 
         if (away)
         {
        	mApp.anims().createAnim( to , from , mRefs.get(no) , delay , 2 , null , 1, true, false);
         }else
         {
        	mApp.anims().createAnim( from , to , mRefs.get(no) , delay , 2 , null , 1 , false, false);
         }
             
     }
 
     public void addVisibleRef(GameonModelRef ref)
     {
     	if (this.mWorld == null)
     		return;
     	if (ref.getVisible() )
     	{
     		if ( this.mVisibleRefs.indexOf(ref) < 0)
     		{
 		
     			mVisibleRefs.add( ref );
     			RenderDomain domain = mApp.world().getDomain(ref.loc());
     			if (domain != null)
     			{
     				domain.setVisible(this);
     			}
     		}
     	}
     }
 
     public void remVisibleRef(GameonModelRef ref)
     {
     	if (ref.getVisible() == false)
     	{
     		if ( this.mVisibleRefs.indexOf(ref) >= 0)
     		{
     			mVisibleRefs.remove( ref );
 
     			
                 RenderDomain domain = mApp.world().getDomain(ref.loc());
                 if (domain != null)
     			{
     				domain.remVisible(this , false);
     			}
                 
     		}
     	}
     }
 
     GameonModelRef ref(int no)
     {
     	if (no < 0 || no >= mRefs.size())
     	{
     		return null;
     	}
     	return mRefs.get(no);
     }
     
     int findRef(GameonModelRef ref)
     {
     	return mRefs.indexOf(ref);
     }
 	public void unsetWorld() {
 		mWorld = null;
 		
 	}
 	
 	protected void createAnim(String type, int refid , String delay, String data)
 	{
 		if (refid < 0 && refid >= mRefs.size())
 		{
 			return;
 		}
 		
 		GameonModelRef ref = mRefs.get(refid);
 		mApp.anims().animModelRef(type , ref, delay , data);
 		
 	}
 	
 
 	
 	protected void setActive(boolean active)
 	{
 		mActive = active;
 	}
 	
     public void createModelFromData(float[] inputdata, float mat[] , float uvb[])
     {
     	float umid = (uvb[1] + uvb[0]) /2;
     	float vmid = (uvb[3] + uvb[2]) /2;
     	float ratiou = uvb[1] - uvb[0];
     	float ratiov = uvb[3] - uvb[2];
     	
     	float outvec[] = { 0 ,0,0,1};
     	float cols[] = {0,0,0,0};
     	float tu,tv;
     	
     	// model info - vertex offset?
     	int len = inputdata.length;
     	//  v   c   uv
     	// (3 + 4 + 2) * 3
     	int off;
 		GLShape shape = new GLShape(this);
     	
     	for (int a=0; a< len; a+= 27 ) 
     	{
     		off = a;
     		
     		GMath.matrixVecMultiply2(mat, inputdata, off , outvec ,0);
     		cols[0] = inputdata[off+3];
     		cols[1] = inputdata[off+4];
     		cols[2] = inputdata[off+5];
     		cols[4] = inputdata[off+6];
         	tu = inputdata[off+7] * ratiou + umid;
         	tv  = inputdata[off+8] * ratiou + umid;
     		GLVertex v1 = shape.addVertexColor(outvec[0], outvec[1], outvec[2] , tu, tv, cols);
 
     		off += 9;
     		GMath.matrixVecMultiply2(mat, inputdata, off , outvec ,0);
     		cols[0] = inputdata[off+3];
     		cols[1] = inputdata[off+4];
     		cols[2] = inputdata[off+5];
     		cols[4] = inputdata[off+6];
         	tu = inputdata[off+7] * ratiou + umid;
         	tv  = inputdata[off+8] * ratiou + umid;
     		GLVertex v2 = shape.addVertexColor(outvec[0], outvec[1], outvec[2] , tu, tv, cols);
     		
     		off += 9;
     		GMath.matrixVecMultiply2(mat, inputdata, off , outvec ,0);
     		cols[0] = inputdata[off+3];
     		cols[1] = inputdata[off+4];
     		cols[2] = inputdata[off+5];
     		cols[4] = inputdata[off+6];
         	tu = inputdata[off+7] * ratiou + umid;
         	tv  = inputdata[off+8] * ratiou + umid;
     		GLVertex v3 = shape.addVertexColor(outvec[0], outvec[1], outvec[2] , tu, tv, cols);
     		
     		shape.addFace( new GLFace(v1,v2,v3));
 
     	}
 
 		addShape(shape);
     }
 
     public void createModelFromData2(float[][] inputdata, float mat[] , float uvb[], int[] colors)
     {
     	float umid = uvb[0];//(uvb[2] + uvb[0]) /2;
     	float vmid = uvb[1];//(uvb[3] + uvb[1]) /2;
     	float ratiou = uvb[2] - uvb[0];
     	float ratiov = uvb[3] - uvb[1];
     	
     	float outvec[] = { 0 ,0,0,1};
     	float tu,tv;
     	
     	// model info - vertex offset?
     	int len = inputdata.length;
     	//  v   c   uv
     	// (3 + 4 + 2) * 3
     	int off;
 		GLShape shape = new GLShape(this);
     	
 		float temp[] = new float[4];
     	for (int a=0; a< len; a+= 9 ) 
     	{
     		temp[0] = inputdata[a+0][0];
     		temp[1] = inputdata[a+0][1];
     		temp[2] = inputdata[a+0][2];
     		
     		GMath.matrixVecMultiply2(mat, temp, 0 , outvec ,0);
         	tu = inputdata[a+2][0] * ratiou + umid;
         	tv  = inputdata[a+2][1] * ratiou + umid;
     		GLVertex v1 = shape.addVertexColorInt(outvec[0], outvec[1], outvec[2] , tu, tv, colors[0]);
 
     		temp[0] = inputdata[a+3][0];
     		temp[1] = inputdata[a+3][1];
     		temp[2] = inputdata[a+3][2];
     		
     		GMath.matrixVecMultiply2(mat, temp, 0 , outvec ,0);
         	tu = inputdata[a+5][0] * ratiou + umid;
         	tv  = inputdata[a+5][1] * ratiou + umid;
     		GLVertex v2 = shape.addVertexColorInt(outvec[0], outvec[1], outvec[2] , tu, tv, colors[0]);
 
     		temp[0] = inputdata[a+6][0];
     		temp[1] = inputdata[a+6][1];
     		temp[2] = inputdata[a+6][2];
     		
     		GMath.matrixVecMultiply2(mat, temp, 0 , outvec ,0);
         	tu = inputdata[a+8][0] * ratiou + umid;
         	tv  = inputdata[a+8][1] * ratiou + umid;
     		GLVertex v3 = shape.addVertexColorInt(outvec[0], outvec[1], outvec[2] , tu, tv, colors[0]);
 
     		
     		shape.addFace( new GLFace(v1,v2,v3));
 
     	}
 
 		addShape(shape);
 		mTextureID = mApp.textures().get(TextureFactory.Type.DEFAULT);
     }
 
     
 	public void addPlane(float[] mat, int[] cols, float[] uvb) {
 		/*
     	float umid = (uvb[2] + uvb[0]) /2;
     	float vmid = (uvb[3] + uvb[1]) /2;
     	float ratiou = uvb[2] - uvb[0];
     	float ratiov = uvb[3] - uvb[1];
 */
     	float ulow = uvb[0];
     	float uhigh =uvb[2];
     	float vlow = uvb[1];
     	float vhigh =uvb[3];
     	
     	GLShape shape = new GLShape(this);
     	GMath.matrixVecMultiply2(mat, mStaticBoundsPlane, 0 , mStaticBounds,0);
     	GMath.matrixVecMultiply2(mat, mStaticBoundsPlane, 4 , mStaticBounds,4);
     	GMath.matrixVecMultiply2(mat, mStaticBoundsPlane, 8 , mStaticBounds,8);
     	GMath.matrixVecMultiply2(mat, mStaticBoundsPlane, 12 , mStaticBounds,12);
     	
     	int count = 0;
        	GLVertex leftBottomFront = shape.addVertexColorInt(mStaticBounds[0], mStaticBounds[1], mStaticBounds[2], ulow , vhigh, cols[count]);
        	count++; if (count >= cols.length) count = 0;
         GLVertex rightBottomFront = shape.addVertexColorInt(mStaticBounds[4], mStaticBounds[5], mStaticBounds[6], uhigh , vhigh, cols[count]);
         count++; if (count >= cols.length) count = 0;
     	GLVertex leftTopFront = shape.addVertexColorInt(mStaticBounds[8], mStaticBounds[9], mStaticBounds[10] , ulow , vlow, cols[count]);
     	count++; if (count >= cols.length) count = 0;
         GLVertex rightTopFront = shape.addVertexColorInt(mStaticBounds[12], mStaticBounds[13], mStaticBounds[14] , uhigh , vlow, cols[count]);
         count++; if (count >= cols.length) count = 0;
         // front
         shape.addFace(new GLFace(leftBottomFront, rightTopFront , leftTopFront));
         shape.addFace(new GLFace(leftBottomFront, rightBottomFront , rightTopFront ));
 
         addShape(shape);		
 		
 	}
 	public GameonModel copyOfModel() {
 		GameonModel model = new GameonModel(mName, mApp, mParentArea);
 		model.mEnabled = this.mEnabled;
 		model.mForceHalfTexturing = false;
 		model.mForcedOwner = 0;
 		model.mShapeList = this.mShapeList;	
 		model.mVertexList = this.mVertexList;
 		model.mVertexOffset = this.mVertexOffset;
 	    model.mTextureID = this.mTextureID;
 	    model.mIndexCount = this.mIndexCount;
 	    
 	    model.mForcedOwner = this.mForcedOwner;
 	    model.mTextureW = this.mTextureW;
 	    model.mTextureH = this.mTextureH;
 		return model;
 	}
 	
 	public GameonModelRef getRef(int count, int loc) {
         if (count < mRefs.size()) {
             return mRefs.elementAt(count);
         } else {
     		while (count >= mRefs.size())
     		{
                 GameonModelRef ref = new GameonModelRef(this, loc);
                 mRefs.add(ref);
                 if (mRefs.size() > 1)
                 {
                 	ref.copyData(mRefs.get(0));
                 	ref.set();
                 }
     		}
     		return mRefs.elementAt(count);
         }
 	}
 	public int getVisibleRefs(int renderId) {
 		int count = 0;
 		for (GameonModelRef ref : this.mVisibleRefs)
 		{
 			if (ref.loc()  == renderId && ref.mVisible)
 			{
 				count ++;
 			}
 		}
 		
 		return count;
 	}
 	public void hideDomainRefs(int renderId) {
 		for (GameonModelRef ref : this.mRefs)
 		{
 			if (ref.loc() == renderId)
 			{
 				this.remVisibleRef(ref);
 			}
 		}
 	}
 	public void setupIter(int num) 
 	{
 
 
 		mIterQueue = new Vector<Integer>();		
 		while (mRefs.size() < num)
 		{
 			GameonModelRef ref = new GameonModelRef(this, 0);
 			mRefs.add(ref);
             if (mRefs.size() > 1)
             {
             	ref.copyData(mRefs.get(0));
             	ref.set();
             }			
 		}
 		for (int a=0; a< num; a++)
 		{
 			mIterQueue.add(a);
 		}
 		
 	}
 	public GameonModelRef getRefById(RefId refid, int loc) {
 		if (refid.id >= 0)
 		{
 			return this.getRef(refid.id, loc);
 		}
 		
 		// go through references and find id with name
 		for (int a=0; a< mIterQueue.size(); a++)
 		{
 			int index = mIterQueue.get(a);
 			GameonModelRef ref = mRefs.get(index);
 			if (refid.alias.equals(ref.mRefAlias))
 			{
 				// put it on the end
 				mIterQueue.remove(a);
 				mIterQueue.add(new Integer(index));
 				refid.id = index;
 				return ref;
 			}
 		}
 		
 		// we didn't find alias get first ref
 		int index = mIterQueue.get(0);
 		GameonModelRef ref = mRefs.get(index);
 		ref.mRefAlias = refid.alias;
 		// now remove it - and put to back
 		mIterQueue.remove(0);
 		mIterQueue.add(new Integer(index));
 		refid.id = index;
 		return ref;
 	}
 	public AreaIndexPair onTouch(float[] eye, float[] ray, int renderId, boolean click) {
 		float loc[] = new float[3];
 		
 		if (mParentArea != null)
 		{
 			if (!mParentArea.acceptTouch(this, click))
 			{
 				return null;
 			}
 		}else
 		{
 			if (this.mOnClick == null)
 				return null;
 		}
 		
 		int count = 0;
 		for (GameonModelRef ref : this.mRefs)
 		{
 			if (ref.mVisible && ref.loc() == renderId)
 			{
 				float dist = ref.intersectsRay(eye , ray, loc);
 				if (dist >=0 && dist < 1e06f)
 				{
 					dist = ref.distToCenter(loc);
 					AreaIndexPair pair = new AreaIndexPair();
 					pair.mLoc[0] = loc[0];
 					pair.mLoc[1] = loc[1];
 					pair.mLoc[2] = loc[2];
 					pair.mDist = dist;
 					if (mParentArea != null)
 					{
 						pair.mArea = mParentArea.mID;
 						pair.mOnclick = mParentArea.mOnclick;
 						pair.mOnFocusLost = mParentArea.mOnFocusLost;
 						pair.mOnFocusGain = mParentArea.mOnFocusGain;
 						pair.mIndex = mParentArea.indexOfRef(ref);
 					}
 					else
 					{
 						pair.mArea = mName;
 						pair.mOnclick = mOnClick;
 						pair.mAlias = ref.mRefAlias;
 						pair.mOnFocusLost = null;
 						pair.mOnFocusGain =null;
 						pair.mIndex = count;
 						
 						
 					}
 								
 					return pair;							
 
 				}
 			}
 			count++;
 		}
 		
 		return null;
 	}
 	public void addShapeFromString(Vector<float[]> vertices, Vector<float[]> textvertices, String data)
 	{
 		GLShape shape = new GLShape(this);
 		StringTokenizer tok = new StringTokenizer(data," ");
 		GLVertex vert[] = new GLVertex[4];
 		int count = 0;
 		GLColor c = mApp.colors().white;
 		if (mCurrentMaterial != null && mCurrentMaterial.diffuse != null)
 		{
 			c = mCurrentMaterial.diffuse;
 		}else
 		if (mCurrentMaterial != null && mCurrentMaterial.ambient != null)
 		{
 			c = mCurrentMaterial.ambient;
 		}
 		else
 		{
 			c = mApp.colors().white;
 		}
 		while (tok.hasMoreTokens())
 		{
 			String value = tok.nextToken();
 			if (value.contains("/"))
 			{
 				StringTokenizer tok2 = new StringTokenizer(value, "/");
 				int index = Integer.parseInt(tok2.nextToken())-1;
 				int index2 = Integer.parseInt(tok2.nextToken())-1;
 				float[] vdata = vertices.elementAt(index);
 				float[] tdata = textvertices.elementAt(index2);
 				GLVertex v = null;
 				if (mCurrentMaterial.t != null)
 				{
 					float t0 = 0.0f;
 					float t1 = 0.0f;
 					if (tdata[0] < 0)
 					{
 						tdata[0] = 0;
 					}
 					if (tdata[0] > 1)
 					{
 						tdata[0] = 1;
 					}
 					if (tdata[1] < 0)
 					{
 						tdata[1] = 0;
 					}
 					if (tdata[1] > 1)
 					{
 						tdata[1] = 1;
 					}									
 					t0 = tdata[0] / mCurrentMaterial.t[2];
 					t1 = (1.0f-tdata[1]) / mCurrentMaterial.t[3];
 					
 					t0 += mCurrentMaterial.t[0];
 					t1 += mCurrentMaterial.t[1];
 					v = shape.addVertex(vdata[0], vdata[2], vdata[1], t0, t1, c);
 				}else
 				{
 					v = shape.addVertex(vdata[0], vdata[2], vdata[1], tdata[0], 1.0f-tdata[1], c);	
 				}
 				 
 				vert[count] = v;
 			}else
 			{
 				int index = Integer.parseInt(value)-1;
 				float[] vdata = vertices.elementAt(index);
 				GLVertex v = shape.addVertex(vdata[0], vdata[2], vdata[1], 0,0, c);
 				vert[count] = v;
 			}
 			count ++;
 		}
 		if (count == 3)
 		{
 			GLFace face = new GLFace(vert[0], vert[1], vert[2]);
 			shape.addFace(face);
 		}else
 		if (count == 4)
 		{
 			GLFace face = new GLFace(vert[0], vert[1], vert[2]);
 			shape.addFace(face);
 			
 			GLFace face2 = new GLFace(vert[0], vert[2], vert[3]);
 			shape.addFace(face2);
 		}
 		addShape(shape);
 	}
 	public void useMaterial(String substring) 
 	{
 		mCurrentMaterial = mApp.textures().getMaterial(substring);
 		if (mTextureID == 1)
 		{
 			mTextureID = mCurrentMaterial.diffuseMapId;
 		}
 	}
     
 }
 
