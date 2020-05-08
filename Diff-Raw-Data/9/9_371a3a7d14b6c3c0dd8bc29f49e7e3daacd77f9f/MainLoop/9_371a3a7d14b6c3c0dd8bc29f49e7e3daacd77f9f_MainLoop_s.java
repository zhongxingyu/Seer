 package com.avona.games.towerdefence.awt;
 
import java.io.IOException;
import java.io.ObjectOutputStream;

 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 
 import com.avona.games.towerdefence.PortableMainLoop;
 import com.sun.opengl.util.Animator;
 import com.sun.opengl.util.FPSAnimator;
 
 public class MainLoop extends PortableMainLoop implements GLEventListener {
 	private static final long serialVersionUID = 1L;
 
 	final private int EXPECTED_FPS = 30;
 
 	public InputMangler input;
 	private Animator animator;
 
 	@Override
 	public void exit() {
 		animator.stop();
 		System.exit(0);
 	}
 
 	public MainLoop() {
 		super();
 
 		GraphicsEngine graphicsEngine = new GraphicsEngine(game, mouse,
 				layerHerder, graphicsTime);
 		ge = graphicsEngine;
 		graphicsEngine.canvas.addGLEventListener(this);
 
 		setupInputActors();
 		input = new InputMangler(graphicsEngine, this, inputActor);
 
 		animator = new FPSAnimator(graphicsEngine.canvas, EXPECTED_FPS);
 		animator.setRunAsFastAsPossible(false);
 		animator.start();
 	}
 
 	public static void main(String[] args) {
 		new MainLoop();
 	}
 
 	@Override
 	public void display(GLAutoDrawable arg0) {
 		performLoop();
 	}
 
 	@Override
 	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {
 		// Unused.
 	}
 
 	@Override
 	public void init(GLAutoDrawable arg0) {
 		// Unused.
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
 			int arg4) {
 		// Unused.
 	}
 }
