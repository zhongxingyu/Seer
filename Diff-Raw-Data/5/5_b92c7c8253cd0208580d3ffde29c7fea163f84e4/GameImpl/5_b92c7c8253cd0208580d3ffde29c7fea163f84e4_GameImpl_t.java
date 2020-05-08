 /*
  *
  * (c)2010 Lein-Mathisen Digital
  * http://lmdig.com
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License as
  * published by the Free Software Foundation; either version 2 of
  * the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.  
  *
  */
 
 
 package com.lmdig.android.tutorial.oglbox2dbasics.game;
 
 import static android.opengl.GLES10.glColor4f;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gleason.openbox.JNIOpenBox;
 import org.jbox2d.collision.AABB;
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 
 import android.util.Log;
 
 import com.kristianlm.robotanks.box2dbridge.Box2DFactory;
 import com.kristianlm.robotanks.box2dbridge.IBody;
 import com.kristianlm.robotanks.box2dbridge.IWorld;
 import com.kristianlm.robotanks.box2dbridge.jbox2d.JBox2DWorld;
 import com.kristianlm.robotanks.box2dbridge.jnibox2d.JNIBox2DWorld;
 import com.lmdig.android.tutorial.oglbox2dbasics.MainActivity;
 import com.lmdig.android.tutorial.oglbox2dbasics.geometry.GLRectangle;
 import com.lmdig.android.tutorial.oglbox2dbasics.geometry.GameShape;
 
 
 
 public class GameImpl implements GameInterface {
 
 	
 	private static final float TIME_STEP = 1f / 40f;
 	private static final int   ITERATIONS = 1;
 	
 	
 	IWorld world = Box2DFactory.newWorld();
 
 	List<GameShape> gsl = new ArrayList<GameShape>();
 	
 	public void init() {
 
 		// density of dynamic bodies
 		float density = 1;
 		
 		// create world's bounding box. 
 		// if objects exceed these borders, they will no longer be
 		// animated (body dies). limits imposed for performance reasons.
 		AABB aabb = new AABB(	
 						new Vec2(-50, -50), 
 						new Vec2( 50,  50)
 					);
 		
 		// Vec2 gravity = new Vec2(0, -9.8f); 
        
 		Vec2 gravity = new Vec2(0, -10.0f);
 		Log.d("Problem Seeker", "Test one");
 		world.create(
 				aabb,
 				gravity,
 				true);
 		Log.d("Problem Seeker", "Test two");
 		GameShape gs;
 	    float x = 0, y = 0, o = 0.0f;
 	    int i =0;
 	    
         for(i =0 ; i<5; i++){	
 		  gs = GameShape.create(new GLRectangle(2, 0.5f));
 		  IBody b1 = gs.attachToNewBody(world, null, density);
           x = (float) 1*i+5;
          y = (float) 1*i+10;
                   
		  b1.setPosition(new Vec2(0, y));
 		  gsl.add(gs);
         }
 		
         gs = GameShape.create(new GLRectangle(1, 0.5f));
 	    gs.attachToNewBodyOther(world, null, density);
 	    gsl.add(gs);
 		 
 		 //b2.setPosition(new Vec2(0,3));
 		 //gsl.add(gs);
 		 //Log.d("Problem Seeker", "Finished");
 		 //makeFence();
 	}
 	
 	private void makeFence() {
 		IBody ground = world.getGroundBody();
 		
 		// static bodies are defined as those having mass and intertia 0
 		// this ensures they are never moved. they only affect positions of
 		// other dynamic bodies who collide with them.
 //		float density = 0;
 //		GameShape gs;
 //		gs = GameShape.create(new GLRectangle(50, .1f));
 //		gs.attachToBody(ground, new Vec2(0, -4), density);
 //		gsl.add(gs);
 //		
 //		gs = GameShape.create(new GLRectangle(50, .1f));
 //		gs.attachToBody(ground, new Vec2(0, 4), density);
 //		gsl.add(gs);
 //		
 //		gs = GameShape.create(new GLRectangle(.1f, 50f));
 //		gs.attachToBody(ground, new Vec2(3, 0), density);
 //		gsl.add(gs);
 //		
 //		gs = GameShape.create(new GLRectangle(.1f, 50f));
 //		gs.attachToBody(ground, new Vec2(-3, 0), density);
 //		gsl.add(gs);
 //		
 //		gs = GameShape.create(new GLRectangle(.1f, .1f));
 //		gs.attachToBody(ground, new Vec2(-.5f, -.5f), density);
 //		gsl.add(gs);
 		
 		
 	}
 
 	public void destroy() {
 		// in case we are using JNIBox2D, this
 		// is very important! otherwise we end up with memory leaks.
 		// world.destroy will recursively destroy all its attached content
 		world.destroy();
 	}
 	
 	JNIOpenBox ob = new JNIOpenBox();
 	
 	@Override
 	public void drawFrame() {
         Log.d("Problem Finder", "Test One One");
 		glColor4f(1, 1, 1, 1);
 		
 		ob.draw();
 //		for(GameShape gs : gsl) {
 //			gs.draw();
 //		}
 	}
 	
 	long nanoTime;
 	float fps;
 	int frames;
 
 	@Override
 	public void gameLoop() {
 		Log.d("Problem Finder", "Game loop");
 		if(world == null) {
 			Log.e("pg", "World not initialized");
 			return;
 		}
 		frames++;
 		long elap = System.currentTimeMillis() - nanoTime;
 		if(elap > 1000) {
 			// update info every second
 			fps = frames / ((float)elap / 1000f);
 			nanoTime = System.currentTimeMillis();
 			frames = 0;
 			
 			
 			String engine = (world instanceof JBox2DWorld ? "JBox2D" : world instanceof JNIBox2DWorld ? "JNIBox2D": "unknown");
 			MainActivity.setStatus(engine + ", fps: " + fps);
 
 		}
 		
 		if(world instanceof JBox2DWorld) {
 //			Log.d("pg", "gravity seet to " + MainActivity.x + ", " + MainActivity.y);
 			JBox2DWorld jw = ((JBox2DWorld)world);
 			World w = jw.getWorld();
 			w.setGravity(new Vec2(MainActivity.x, MainActivity.y));
 		}
 	    // Log.d("Game", "Stepping");
 		world.step(TIME_STEP, ITERATIONS);
 		// Log.d("Game", "Stepping");
 		world.sync();
 	}
 	
 
 }
