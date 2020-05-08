 package main;
 
 import static main.MatrixContainer.*;
 import static math.Math.*;
 import static org.lwjgl.opengl.GL11.*;
 
 import java.io.*;
 
 import importers.*;
 import math.matrices.*;
 import openGLCLInterfaces.openGL.shaders.*;
 
 import org.lwjgl.input.*;
 import org.lwjgl.opengl.*;
 
 /**
  * Main class, where update and draw happens.
  */
 public class MainFrame {
 	private ShaderProgram basicShader;
 	private Camera camera;
 	private Model model;
 
 	/**
 	 * Method to do initializing stuff.
 	 */
 	public void init() {
 		Mouse.setGrabbed(true);
 		Keyboard.enableRepeatEvents(false);
 		initOpenGL();
 		camera = new Camera(Matrix.vec3(), 180, 0);
 		try {
 			model = OBJLoader.loadModelFromOBJ("resources/models/tree.obj");
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Initializing OpenGL.
 	 */
 	private void initOpenGL() {
 		// set clear color to black
 		glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 		glEnable(GL_DEPTH_TEST);
 		glDepthFunc(GL_LEQUAL);
 		glEnable(GL_CULL_FACE);
 		glEnable(GL_BLEND);
 		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
 		glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST);
 		glEnable(GL_TEXTURE_2D);
 		loadShaders();
 	}
 
 	/**
 	 * Loads shaders from files and compiles them.
 	 */
 	private void loadShaders() {
 		basicShader = ShaderProgram.createBasicShader("/shaderFiles/vert.c", "/shaderFiles/frag.c");
 	}
 
 	/**
 	 * Main update logic.
 	 * 
 	 * @param delta
 	 *            The time passed from last update to this.
 	 */
 	public void update(int delta) {
 		Vector2 mouseMovement = MouseHelper.getMovement();
 		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
 			camera.move(-0.0001f, delta, camera.getViewVector());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
 			camera.move(-0.0001f, delta, Vector3.getDirectionFromRotation(
					toRadians(camera.getYRotation() + 90), toRadians(camera.getPitch())));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
 			camera.move(0.0001f, delta, camera.getViewVector());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
 			camera.move(0.0001f, delta, Vector3.getDirectionFromRotation(
					toRadians(camera.getYRotation() + 90), toRadians(camera.getPitch())));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
 			camera.move(-0.0001f, delta, Vector3.getDirectionFromRotation(
 					toRadians(camera.getYRotation()), toRadians(camera.getPitch() + 90)));
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
 			camera.move(0.0001f, delta, Vector3.getDirectionFromRotation(
 					toRadians(camera.getYRotation()), toRadians(camera.getPitch() + 90)));
 		}
 		camera.setRotation(camera.getYRotation() + 0.1f * mouseMovement.getX(), camera.getPitch()
 				+ -0.1f * mouseMovement.getY());
 
 		if (camera.getPitch() < -90f) {
 			camera.setRotation(camera.getYRotation(), -90f);
 		}
 		if (camera.getPitch() > 90f) {
 			camera.setRotation(camera.getYRotation(), 90f);
 		}
 		if (camera.getYRotation() < -180f) {
 			camera.setRotation(360f + camera.getYRotation(), camera.getPitch());
 		}
 		if (camera.getYRotation() > 180f) {
 			camera.setRotation(camera.getYRotation() - 360f, camera.getPitch());
 		}
 	}
 
 	/**
 	 * Draw to the screen
 	 */
 	public void draw() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		glDisable(GL_CULL_FACE);
 		camera.setup();
 		uploadMatrices();
 		basicShader.enable();
 		model.render();
 //		glBegin(GL_QUADS);
 //		glVertex3f(-10, -10, 10);
 //		glVertex3f(-10, 10, 10);
 //		glVertex3f(10, 10, 10);
 //		glVertex3f(10, -10, 10);
 //		glEnd();
 		basicShader.disable();
 	}
 }
