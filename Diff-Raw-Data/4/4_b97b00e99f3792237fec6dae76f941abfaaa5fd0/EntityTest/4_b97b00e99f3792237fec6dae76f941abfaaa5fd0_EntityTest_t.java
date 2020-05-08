 package com.punchline.javalib.tests;
 
 import static org.junit.Assert.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 
 import com.badlogic.gdx.math.Vector2;
 import com.punchline.javalib.entities.*;
 import com.punchline.javalib.entities.components.physical.Particle;
 import com.punchline.javalib.entities.components.physical.Transform;
 import com.punchline.javalib.entities.components.physical.Velocity;
 import com.punchline.javalib.entities.components.render.*;
 
 @RunWith(JUnit4.class)
 public class EntityTest {
 
 	@Test
 	public void addgetComponentTest() {
			Entity e = new Entity();
			e.init("tag", "group", "type");
 			
 			Renderable x = e.addComponent(Renderable.class, new Sprite());
 			Renderable p = e.getComponent();
 		
 			assertEquals("x != p; getComponent failure", x, p);
 
 	}
 	
 	@Test
 	public void onAddComponentTest() {
 		Entity e = new Entity();
 		e.init("tag", "group", "type");
 		
 		Particle p = e.addComponent(new Particle(e, new Vector2(0,0), 0f, new Vector2(0,0)));
 		Velocity v = e.getComponent();
 		Transform t = e.getComponent();
 		p.setAngularVelocity(22);
 		v.setAngularVelocity(43);
 		
 		assertEquals("p != t; onAddComponent failure", p, t);
 		assertEquals("v != t; onAddComponent failure", v, t);
 	}
 
 }
