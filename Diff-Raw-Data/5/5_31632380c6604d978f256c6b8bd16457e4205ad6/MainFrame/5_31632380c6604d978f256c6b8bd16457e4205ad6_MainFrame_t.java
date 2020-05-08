 package main;
 
 import static main.MatrixContainer.*;
 import static math.Math.*;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL30.*;
 import importers.*;
 
 import java.io.*;
 import java.nio.*;
 
 import main.MatrixContainer.MatrixType;
 import math.matrices.*;
 import openGLCLInterfaces.openGL.buffers.*;
 import openGLCLInterfaces.openGL.shaders.*;
 
 import org.lwjgl.*;
 import org.lwjgl.input.*;
 import org.lwjgl.opengl.*;
 
 /**
  * Main class, where update and draw happens.
  */
 public class MainFrame {
 	private ShaderProgram defaultLightShader, indexShader, visibleIndexShader, blackShader;
 	private IndexedModel model;
 	private Texture selTexture;
 	private FrameBuffer fb;
 	private int[] selData;
 	private Matrix4x4 perspective;
 	private MemoryList<Integer> selection0;
 	private boolean[] selection1;
 	private boolean dragging = false;
 	private Vector2 lDrag;
 
 	/**
 	 * Method to do initializing stuff.
 	 */
 	public void init() {
 		Keyboard.enableRepeatEvents(false);
 		initOpenGL();
 		try {
 			File file = new File(System.getProperty("user.dir"),
 					"../ModelConverter/resources/models/ship2.obj");
 			model = OBJLoader.loadModelFromOBJ(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		MainWindow.vertCountLbl.setText(String.valueOf(this.model.getVertexCount()));
 		selTexture = TextureLoader.createTexture(Display.getWidth(), Display.getHeight(), GL_R32UI,
 				GL_RED_INTEGER, GL_UNSIGNED_INT);
 		selData = new int[Display.getWidth() * Display.getHeight()];
 		fb = new FrameBuffer(new Texture[] { selTexture });
 		fb.showStatus();
 		this.perspective = (Matrix4x4) MatrixDB.generateIdentityMatrix(4);
 		this.selection0 = new MemoryList<Integer>(1024 * 1024 * 5);
 		this.selection1 = new boolean[model.getVertexCount()];
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
 		defaultLightShader = ShaderProgram.createShaderFromDir("/shaderFiles/defaultLightShader");
 		indexShader = ShaderProgram.createShaderFromDir("/shaderFiles/indexShader");
 		visibleIndexShader = ShaderProgram.createShaderFromDir("/shaderFiles/visibleIndexShader");
 		blackShader = ShaderProgram.createShaderFromDir("/shaderFiles/blackShader");
 	}
 
 	/**
 	 * Main update logic.
 	 * 
 	 * @param delta
 	 *            The time passed from last update to this.
 	 */
 	public void update(int delta) {
 		VertexBuffer vb = (VertexBuffer) model.getVertexBuffer();
 		Vector2 mouseMovement = MouseHelper.getMovement(false);
 		Vector2 mousePos = MouseHelper.getPosition();
 		boolean operation = false;
 		Vector3 center = Matrix.vec3();
 		float distance = 1f;
		float rotate = 0.002f;
 		float shift = 0.1f;
 		float zoom = 0.1f;
		float r_m = 5f;
 		float s_m = 1f;
 		float z_m = 1f;
 
 		if (this.selection0.size() > 0) {
 			center = (Vector3) this.perspective.multiply(
 					vb.getData(0, this.selection0.get(this.selection0.size() / 2) * 3).resize(1, 4,
 							1f)).resize(1, 3);
 			distance = center.getMagnitude() * 0.01f;
 		} else {
 			r_m = 1;
 			s_m = 1;
 			z_m = 1;
 		}
 		if (MouseHelper.middle() && Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
 			this.perspective = (Matrix4x4) MatrixDB.generateTranslationMatrix(
 					(Vector3) center.multiply(-1)).multiply(perspective);
 			this.perspective = (Matrix4x4) MatrixDB.generateRotationMatrix(
 					mouseMovement.getX() * rotate * distance * r_m, Matrix.vec3(0, 1, 0)).multiply(
 					perspective);
 			this.perspective = (Matrix4x4) MatrixDB.generateRotationMatrix(
 					mouseMovement.getY() * rotate * distance * r_m, Matrix.vec3(1, 0, 0)).multiply(
 					perspective);
 			this.perspective = (Matrix4x4) MatrixDB.generateTranslationMatrix(
 					(Vector3) center.multiply(1)).multiply(perspective);
 			operation = true;
 		}
 		if (MouseHelper.middle() && !Keyboard.isKeyDown(Keyboard.KEY_LMENU)) {
 			this.perspective = (Matrix4x4) MatrixDB.generateTranslationMatrix(
 					(Vector3) mouseMovement.resize(1, 3).componentMultiply(
 							Matrix.vec3(shift * distance * s_m, -shift * distance * s_m, shift
 									* distance * s_m))).multiply(perspective);
 			operation = true;
 		}
 		int wheel = MouseHelper.wheel();
 		if (wheel != 0) {
 			this.perspective = (Matrix4x4) MatrixDB.generateTranslationMatrix(
 					(Vector3) Matrix.vec3(0, 0, 1).multiply(wheel * zoom * z_m * distance))
 					.multiply(perspective);
 			operation = true;
 		}
 
 		if (operation) {
 			if (mousePos.getX() <= 1) {
 				MouseHelper.setPosition(Matrix.vec2(Display.getWidth() - 2, mousePos.getY()));
 			}
 			if (mousePos.getX() >= Display.getWidth() - 1) {
 				MouseHelper.setPosition(Matrix.vec2(2, mousePos.getY()));
 			}
 			if (mousePos.getY() <= 1) {
 				MouseHelper.setPosition(Matrix.vec2(mousePos.getX(), Display.getHeight() - 2));
 			}
 			if (mousePos.getY() >= Display.getHeight() - 1) {
 				MouseHelper.setPosition(Matrix.vec2(mousePos.getX(), 2));
 			}
 		}
 		if (MouseHelper.left() && !dragging && !operation) {
 			if (lDrag == null) {
 				lDrag = mousePos;
 			} else {
 				if (((Vector2) mousePos.subtract(lDrag)).getMagnitudeSq() > 10 * 10) {
 					dragging = true;
 					clearSelection();
 				}
 			}
 			int index = getSelectedIndex();
 			if (index != 0) {
 				index--;
 				if (!selection1[index]) {
 					vb.setData(2, index, new int[] { 1 });
 					selection1[index] = true;
 					selection0.add(index);
 				}
 			} else {
 				clearSelection();
 			}
 		}
 		if (!MouseHelper.left() | operation) {
 			lDrag = null;
 			dragging = false;
 		}
 		if (dragging && !operation) {
 			updateSelection();
 		}
 		getTextureSelData();
 		MainWindow.selCountLbl.setText(String.valueOf(this.selection0.size()));
 	}
 
 	private void clearSelection() {
 		VertexBuffer vb = (VertexBuffer) model.getVertexBuffer();
 		for (int i = 0; i < selection0.size(); i++) {
 			int j = selection0.get(i);
 			selection1[j] = false;
 			vb.setData(2, j, new int[] { 0 });
 		}
 		selection0.clear();
 	}
 
 	private void updateSelection() {
 		VertexBuffer vb = (VertexBuffer) model.getVertexBuffer();
 		clearSelection();
 		Vector2 mousePos = MouseHelper.getPosition();
 		int x0 = (int) lDrag.getX();
 		int x1 = (int) mousePos.getX();
 		int y0 = (int) lDrag.getY();
 		int y1 = (int) mousePos.getY();
 		int xStep = signum(x1 - x0);
 		int yStep = signum(y1 - y0);
 		for (int y = y0; y != y1; y += yStep) {
 			for (int x = x0; x != x1; x += xStep) {
 				int index = getIndex(x, y);
 				if (index > 0) {
 					index--;
 					if (!selection1[index]) {
 						vb.setData(2, index, new int[] { 1 });
 						selection1[index] = true;
 						selection0.add(index);
 					}
 				}
 			}
 		}
 	}
 
 	private int getIndex(int x, int y) {
 		int index = (Display.getHeight() - y) * Display.getWidth() + x;
 		if (index < 0 | index >= selData.length) {
 			return 0;
 		}
 		return this.selData[index];
 	}
 
 	private int getSelectedIndex() {
 		Vector2 mousePosition = MouseHelper.getPosition();
 		int x = (int) mousePosition.getX();
 		int y = (int) mousePosition.getY();
 		for (int i = 0; i < 10; i++) {
 			for (int y1 = y - i; y1 <= y + i; y1++) {
 				for (int x1 = x - i; x1 <= x + i; x1++) {
 					int index = getIndex(x1, y1);
 					if (index != 0) {
 						return index;
 					}
 				}
 			}
 		}
 		return 0;
 	}
 
 	/**
 	 * Draw to the screen
 	 */
 	public void draw() {
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 		glViewport(0, 0, Display.getWidth(), Display.getHeight());
 		glPointSize(1f);
 		activeMatrix(MatrixType.projectionMatrix);
 		loadIdentity();
 		perspective(toRadians((float) Display.getHeight() / (float) Display.getWidth() * 45f),
 				(float) Display.getWidth() / (float) Display.getHeight(), 0.1f, 100000.0f);
 		multiplyCurrentMatrix(this.perspective);
 		activeMatrix(MatrixType.modelViewMatrix);
 		uploadMatrices();
 		defaultLightShader.enable();
 		model.render();
 		defaultLightShader.disable();
 		fb.enable();
 		fb.clearBuffer();
 		if (MainWindow.backface.isSelected()) {
 			blackShader.enable();
 			model.render();
 			blackShader.disable();
 		}
 		indexShader.enable();
 		model.getVertexBuffer().enable();
 		glDrawArrays(GL_POINTS, 0, model.getVertexCount());
 		model.getVertexBuffer().disable();
 		indexShader.disable();
 		fb.disable();
 		glPointSize(5f);
 		visibleIndexShader.enable();
 		UniformVariable selIndex = visibleIndexShader.getUniformVariable("selectedVertex");
 		selIndex.set(getSelectedIndex());
 		model.getVertexBuffer().enable();
 		glDrawArrays(GL_POINTS, 0, model.getVertexCount());
 		model.getVertexBuffer().disable();
 		visibleIndexShader.disable();
 		if (dragging) {
 			glDisable(GL_CULL_FACE);
 			Vector2 mousePos = MouseHelper.getPosition();
 			glMatrixMode(GL_PROJECTION_MATRIX);
 			glLoadIdentity();
 			glOrtho(0, Display.getWidth(), Display.getHeight(), 0, 0, 1);
 			glColor4f(1, 0, 0, 0.3f);
 			glBegin(GL_QUADS);
 			glVertex2f(lDrag.getX(), lDrag.getY());
 			glVertex2f(lDrag.getX(), mousePos.getY());
 			glVertex2f(mousePos.getX(), mousePos.getY());
 			glVertex2f(mousePos.getX(), lDrag.getY());
 			glEnd();
 			glColor4f(1, 0, 0, 1f);
 			glBegin(GL_LINE_STRIP);
 			glVertex2f(lDrag.getX(), lDrag.getY());
 			glVertex2f(lDrag.getX(), mousePos.getY());
 			glVertex2f(mousePos.getX(), mousePos.getY());
 			glVertex2f(mousePos.getX(), lDrag.getY());
 			glVertex2f(lDrag.getX(), lDrag.getY());
 			glEnd();
 		}
 	}
 
 	public void getTextureSelData() {
 		IntBuffer buffer = BufferUtils.createIntBuffer(selTexture.getWidth()
 				* selTexture.getHeight());
 		fb.enable();
 		glReadPixels(0, 0, selTexture.getWidth(), selTexture.getHeight(), GL_RED_INTEGER,
 				GL_UNSIGNED_INT, buffer);
 		fb.disable();
 		buffer.get(selData);
 	}
 }
