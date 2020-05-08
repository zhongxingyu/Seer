 package com.ahmet.polyshaping;
 
 import java.util.ArrayList;
 
 import com.ahmet.b2d.Tools;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 public class Mesh2d {
 	private Mesh mesh;
 	private Vector2[] polygonVertexList;
 	private Vector2[] triVertexList;
 	private int renderMode=GL10.GL_POINTS;
 	public float PointSize=1,LineWidth=1;
 	
 	private Vector3 color=new Vector3(255,255,255);
 	private Vector2 pos,dim=new Vector2(50,50);
 	public float angle=0;
 	
 	Mesh2d(){}
 	Mesh2d(Vector2 Pos,Vector3 Color)
 	{
 		pos=Pos;
 		color=Color;		
 	}
 	Mesh2d(Vector2[] vertexlist,Vector2 Pos,Vector3 Color,boolean fill)
 	{
 		pos=Pos;
 		color=Color;
 		setVertices(vertexlist);
 		setFill(fill);
 	}
 	public void setVertices(Vector2[] vertexlist)
 	{
 		polygonVertexList=vertexlist;
 		triVertexList=Tools.Triangulate(polygonVertexList);		
 		setRenderMode(renderMode);
 	}
 	public void setFill(boolean fill)
 	{
 		if(fill)
 		{
 			setRenderMode(GL10.GL_TRIANGLES);
 		}
 		else
 		{
 			setRenderMode(GL10.GL_LINE_LOOP);
 		}
 	}
 	public void setRenderMode(int p)
 	{
 		renderMode=p;
 		if(renderMode==GL10.GL_TRIANGLES)
 		{
 			updateMesh(triVertexList);
 		}
 		else
 		{
 			updateMesh(polygonVertexList);
 		}
 	}
 	public void Draw(SpriteBatch s) {
 		s.begin();
 		Gdx.gl10.glPushMatrix();
 		Gdx.gl10.glPointSize(PointSize);
 		Gdx.gl10.glLineWidth(LineWidth);
 		Gdx.gl10.glTranslatef(pos.x,pos.y, 0);
 		Gdx.gl10.glRotatef(angle, 0, 0, 1);	
 		mesh.render(renderMode);
 		Gdx.gl10.glPushMatrix();
 		s.end();
 		Gdx.gl10.glPointSize(1);
 		Gdx.gl10.glLineWidth(1);
 		
 	}
 	public void updateMesh(Vector2[] vertexlist)
 	{
         mesh = new Mesh(false, vertexlist.length,vertexlist.length,
                 new VertexAttribute(Usage.Position, 3, "a_position"),
                 new VertexAttribute(Usage.ColorPacked, 4, "a_color"),
                 new VertexAttribute(Usage.TextureCoordinates,2, "a_texCoords"));
         float[] vertices=new float[vertexlist.length*6];
         short[] indices=new short[vertexlist.length];
         for(int i=0; i<vertexlist.length; i++)
         {
         	Vector2 t1=vertexlist[i];
         	vertices[i*6]=t1.x;
         	vertices[i*6+1]=t1.y;
         	vertices[i*6+2]=0;
         	vertices[i*6+3]=Color.toFloatBits(color.x, color.y, color.z, 255);
         	vertices[i*6+4]=1;
         	vertices[i*6+5]=1;
         	indices[i]=(short) i;
         }
         
         mesh.setVertices(vertices);  
         mesh.setIndices(indices);		
 	}
 	public void addVertex(float x,float y)
 	{
 		Vector2[] vertexlist2=new Vector2[polygonVertexList.length+1];
 		for(int i=0; i<polygonVertexList.length; i++)
 		{
 			vertexlist2[i]=polygonVertexList[i];
 		}
 		vertexlist2[polygonVertexList.length]=new Vector2(x-pos.x,y-pos.y);
 		setVertices(vertexlist2);
 	}
 	public void addVertex(float x,float y,Vector2 after)
 	{
 		Vector2[] vertexlist2=new Vector2[polygonVertexList.length+1];
 		for(int i=0; i<polygonVertexList.length; i++)
 		{
 			vertexlist2[i]=polygonVertexList[i];
 		}
 		vertexlist2[polygonVertexList.length]=new Vector2(x-pos.x,y-pos.y);
 		setVertices(vertexlist2);
 	}
 	public void getAdded(float x,float y)
 	{
 		ArrayList<Vector2> temp=new ArrayList<Vector2>();
 		for(int i=0; i<polygonVertexList.length; i++)
 		{
 			temp.add(polygonVertexList[i]);
 		}
		temp.add(getClosestVertexIndex(x,y)+1,new Vector2(x,y));
 		setVertices(temp.toArray(new Vector2[0]));
 	}
 	public void setVertex(int i,float x,float y)
 	{
 		polygonVertexList[i]=new Vector2(x,y);
 		setVertices(polygonVertexList);
 	}
 	public int getClosestVertexIndex(float x,float y)
 	{
 		int indis=0;
 		float minhip=(float) Math.hypot(polygonVertexList[0].x-x, polygonVertexList[0].y-y);
 		for(int i=0; i<polygonVertexList.length; i++)
 		{
 			float temp=(float) Math.hypot(polygonVertexList[i].x-x, polygonVertexList[i].y-y);
 			if(temp<minhip)
 			{
 				minhip=temp;
 				indis=i;
 			}
 		}
 		return indis;
 	}
 	public Vector2 getVertex(int i)
 	{
 		return polygonVertexList[i];
 	}
 	public Vector2[] getVertices()
 	{
 		return this.polygonVertexList;
 	}
 	public void setPosition(Vector2 Pos)
 	{
 		pos=Pos;
 	}
 
 }
