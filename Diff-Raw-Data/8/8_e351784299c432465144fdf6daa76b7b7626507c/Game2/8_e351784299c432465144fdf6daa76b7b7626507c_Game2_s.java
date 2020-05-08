 package com.ahmet.polyshaping;
 
 
 import com.ahmet.b2d.GIcombin;
 import com.ahmet.b2d.Rect;
 import com.ahmet.b2d.Tools;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
 import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
 import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import com.badlogic.gdx.physics.box2d.World;
 
 public class Game2 extends GIcombin implements InputProcessor {
 	private SpriteBatch batch;
 	Mesh2d ak;
 	Rect r1;
 	Ellipse e1;
 	Line l1;
 	boolean touchDown;
 	int indis2Change;
 	@Override
 	public void create () {
 
 		batch = new SpriteBatch();
 		Gdx.input.setInputProcessor(this);
 		Vector2[] vv={
 				new Vector2(0,0)
 		};	
 		ak=new Mesh2d(vv,new Vector2(0,0),new Vector3(255,0,0),false);
 		ak.setRenderMode(GL10.GL_LINE_LOOP);
 		r1=new Rect(new Vector2(300,100),new Vector2(50,50),new Vector3(255,0,0),false);
 		e1=new Ellipse(new Vector2(60,60),new Vector2(25,25),new Vector3(255,0,0),false);
 		l1=new Line(new Vector2(60,60),new Vector2(100,100),new Vector3(255,255,255));
 	}
 	@Override
 	public void render () {
 		GL10 gl = Gdx.graphics.getGL10();
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		gl.glEnable(GL10.GL_TEXTURE_2D);	
 		batch.getProjectionMatrix().setToOrtho2D(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		ak.Draw(batch);
 		r1.Draw(batch);			
 		e1.Draw(batch);
 		l1.Draw(batch);
 	}
 	@Override
 	public boolean touchUp (int x, int y, int pointer, int button) {
 		Vector2 temp=ak.getVertex(indis2Change);
 		if(Math.hypot(temp.x-x, temp.y-Gdx.graphics.getHeight()+y)>25)
 		{
 			ak.getAdded(x, Gdx.graphics.getHeight()-y);
 		}
 		else
 		{
 			ak.setVertex(indis2Change,x, Gdx.graphics.getHeight()-y);
 		}
 		touchDown=false;
 		return false;
 	}
 	@Override
 	public boolean touchMoved (int x, int y) {
 		e1.setPosition(new Vector2(x,Gdx.graphics.getHeight()-y));
 		l1.setPos2(new Vector2(x,Gdx.graphics.getHeight()-y));
 		indis2Change=ak.getClosestVertexIndex(x,Gdx.graphics.getHeight()-y);
 		Vector2 temp=ak.getVertex(indis2Change);
 		l1.setPos1(temp);
 
 		return false;
 	}
 	@Override
 	public boolean touchDragged(int x,int y,int pointer)
 	{
 		Vector2 temp=ak.getVertex(indis2Change);
 		if(touchDown)
 		{
 			if(Math.hypot(temp.x-x, temp.y-Gdx.graphics.getHeight()+y)<=25)
 			{
 				ak.setVertex(indis2Change,x, Gdx.graphics.getHeight()-y);
 				e1.setPosition(new Vector2(x,Gdx.graphics.getHeight()-y));
 				l1.setPos2(new Vector2(x,Gdx.graphics.getHeight()-y));
 				l1.setPos1(temp);
 			}
 		}		
 		return false;
 	}
 	@Override
 	public boolean touchDown (int x, int y, int pointer, int newParam) {
 		touchDown=true;
 		e1.setPosition(new Vector2(x,Gdx.graphics.getHeight()-y));
 		return false;
 	}
 }
 
