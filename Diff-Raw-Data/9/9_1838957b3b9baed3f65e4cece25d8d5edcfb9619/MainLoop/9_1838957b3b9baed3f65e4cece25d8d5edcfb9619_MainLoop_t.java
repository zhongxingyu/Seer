 package com.avona.games.towerdefence.awt;
 
 import java.awt.Color;
 import java.awt.Frame;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 
 import com.avona.games.towerdefence.Game;
 import com.avona.games.towerdefence.TimeTrack;
 import com.sun.opengl.util.FPSAnimator;
 
 public class MainLoop implements GLEventListener {
 	final private int EXPECTED_FPS = 60;
 
 	public Game game;
 	public GraphicsEngine ge;
 	public InputMangler input;
 
 	private TimeTrack gameTime;
 	private TimeTrack graphicsTime;
 	private FPSAnimator animator;
 
 	public void performLoop() {
 		final double wallClock = System.nanoTime() * Math.pow(10, -9);
 		graphicsTime.update(wallClock);
 		gameTime.update(wallClock);
 
 		// Updating of inputs is done asynchronously.
 
 		// Update the world.
 		game.updateWorld(gameTime.tick);
 
 		// Show the world.
 		ge.render(gameTime.tick, graphicsTime.tick);
 	}
 
 	public void pauseGame() {
 		gameTime.stopClock();
 	}
 
 	public void unpauseGame() {
 		gameTime.startClock();
 	}
 
 	public void toggleGamePause() {
 		if (gameTime.isRunning()) {
 			pauseGame();
 		} else {
 			unpauseGame();
 		}
 	}
 
 	public void exit() {
 		animator.stop();
 		System.exit(0);
 	}
 
 	public MainLoop() {
 		gameTime = new TimeTrack();
 		graphicsTime = new TimeTrack();
 
 		game = new Game();
 		ge = new GraphicsEngine(this, game);
 		ge.canvas.addGLEventListener(this);
 		input = new InputMangler(this, ge, game);
 
 		final Frame frame = new Frame("Towerdefence");
 		frame.add(ge.canvas);
 		frame.setSize(ge.defaultWidth, ge.defaultHeight);
 		frame.setBackground(Color.WHITE);
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				exit();
 			}
 		});
 		frame.setCursor(java.awt.Toolkit.getDefaultToolkit().createCustomCursor(new BufferedImage(1,1,BufferedImage.TYPE_4BYTE_ABGR),new java.awt.Point(0,0),"NOCURSOR"));
 		frame.setVisible(true);
 
 		animator = new FPSAnimator(ge.canvas, EXPECTED_FPS);
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
