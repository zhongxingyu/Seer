 /*
  * This Source Code Form is subject to the terms of the Mozilla Public
  * License, v. 2.0. If a copy of the MPL was not distributed with this
  * file, You can obtain one at http://mozilla.org/MPL/2.0/.
  */
 
 package net.sekien.pepper;
 
 import net.sekien.elesmyr.system.Globals;
 import net.sekien.elesmyr.util.FileHandler;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.*;
 import org.newdawn.slick.Image;
 
 import java.awt.*;
 
 public class IntroState extends Node {
 	private boolean skipintro = Globals.get("skipIntro", false);
 	private Image sekien;
 	private Image game;
 	private Image particles;
 	private Image particles2;
 	int time = 0;
 
 	public IntroState(String name) {
 		super(name);
 		try {
 			sekien = FileHandler.getImage("menu.intro1"); //Sekien
 			game = FileHandler.getImage("menu.intro2"); //Elesmyr
 			particles = FileHandler.getImage("menu.introParticles"); //particles
 			particles2 = FileHandler.getImage("menu.introParticlesAlt"); //particles
 		} catch (SlickException e) {
 			e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
 		}
 	}
 
 	private static final float stepTime = 100;
 
 	@Override
 	public void render(Renderer renderer, int w, int h, boolean sel) {
 		w = Display.getWidth();
 		h = Display.getHeight();
 		int ires = renderer.gc.getHeight();
 		w = (int) (ires*(1920f/1080f));
 		h = ires;
 		if (skipintro) {
 			time = 10*(int) stepTime;
 		}
 
 		float bx, by, bw, bh;
 		bw = sekien.getWidth()*w/1920;
 		bh = sekien.getHeight()*h/1080;
 		bx = (Display.getWidth()-bw)/2;
 		by = (Display.getHeight()-bh)/2;
 
 		renderer.fillRect(Color.black, bx, by, bw, bh);
 		if (time < stepTime*0.5) {
 			sekien.setAlpha(time/(stepTime*0.5f));
 			particles.setAlpha(time/(stepTime*0.5f));
 			particles2.setAlpha(time/(stepTime*0.5f));
 			sekien.draw(bx, by, bw, bh);
 		} else if (time < stepTime*2.5f) {
 			sekien.setAlpha(1);
 			particles.setAlpha(1);
 			particles2.setAlpha(1);
 			sekien.draw(bx, by, bw, bh);
 		} else if (time < stepTime*3f) {
 			sekien.draw(bx, by, bw, bh);
 			game.setAlpha((time-stepTime*2.5f)/(stepTime*0.5f));
 			game.draw(bx, by, bw, bh);
 		} else if (time < stepTime*5f) {
 			game.setAlpha(1);
 			game.draw(bx, by, bw, bh);
 		} else if (time < stepTime*5.5f) {
 			game.setAlpha(1-((time-stepTime*5f)/(stepTime*0.5f)));
 			particles.setAlpha(1-((time-stepTime*5f)/(stepTime*0.5f)));
 			particles2.setAlpha(1-((time-stepTime*5f)/(stepTime*0.5f)));
 			game.draw(bx, by, bw, bh);
 		} else {
 			StateManager.forcePop();
 			StateManager.setStateInitial("Main");
 			net.sekien.elesmyr.system.Renderer.init(renderer.gc);
 		}
		particles.draw(bx, (time-stepTime*2.5f)/(stepTime*0.1f), bw, bh);
		particles2.draw(bx, (time-stepTime*2.5f)/(stepTime*0.4f), bw, bh);
 		if (Globals.get("debug", false))
 			renderer.text(0, 16, Float.toString((float) Math.floor((time/stepTime)*10)/10));
 	}
 
 	@Override
 	public void update(GameContainer gc) {
 		time++;
 	}
 
 	@Override
 	public Node nodeAt(int x, int y) {
 		return this;
 	}
 
 	@Override
 	public void setSel(int x, int y) {
 	}
 
 	@Override
 	public void onAction(Action action) {
 		if (action.equals(Action.SELECT))
 			time = 5*(int) stepTime;
 		else if (action.equals(Action.LEFT))
 			time -= 11;
 		else if (action.equals(Action.RIGHT))
 			time += 9;
 	}
 
 	@Override
 	public Dimension getDimensions(boolean sel) {
 		return new Dimension(640, 480);
 	}
 }
