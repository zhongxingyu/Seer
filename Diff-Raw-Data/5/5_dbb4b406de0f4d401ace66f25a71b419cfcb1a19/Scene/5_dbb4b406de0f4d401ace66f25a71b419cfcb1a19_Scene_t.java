 package com.ting.scene;
 
 import com.threed.jpct.Light;
 import com.threed.jpct.Object3D;
 import com.threed.jpct.RGBColor;
 import com.threed.jpct.SimpleVector;
 
 public class Scene extends BaseScene {
 	public RGBColor background = new RGBColor(20, 120, 20);
 	public RGBColor ambient = new RGBColor(0, 255, 0);
 
 	Object3D shape;
 	SimpleVector lightOffset = new SimpleVector(-100, -100, -75);
 
 	public Scene() {
 		//world.setAmbientLight(ambient.getRed(), ambient.getGreen(), ambient.getBlue());
 
		shape = loadOBJ("BMan_02", 6);
 		// shape.calcTextureWrapSpherical();
 		shape.setTexture(addTexture("BMan_02.png"));
 		shape.build();
 		world.addObject(shape);
 
		world.getCamera().setPosition(50, -40, -15);
 		world.getCamera().lookAt(shape.getTransformedCenter());
 
 	    Light light = new Light(world);
 		light.setIntensity(250, 250, 250);
 		light.setPosition(shape.getTransformedCenter().calcAdd(lightOffset));
 	}
 
 	public void loop() {
 		shape.rotateY(0.01f);
 	}
 
 	public void move(float dx, float dy) {
 		if (dx != 0) {
 			shape.rotateY(dx/-100f);
 		}
 		if (dy != 0) {
 			shape.rotateX(dy/-100f);
 		}
 	}
 
 }
