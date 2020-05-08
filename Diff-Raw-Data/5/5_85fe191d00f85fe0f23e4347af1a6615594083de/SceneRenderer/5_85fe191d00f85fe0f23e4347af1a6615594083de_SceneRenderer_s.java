 package graphicsManager;
 
 import inputManager.ExtendedMouseEvent;
 import inputManager.InputUpdateEvent;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import javax.media.opengl.GL;
 import javax.media.opengl.GL3bc;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLEventListener;
 import javax.media.opengl.glu.GLU;
 
 import modelManager.TextureLoader;
 import objectManager.ObjectEvent;
 
 import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
 import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
 import org.apache.commons.math3.linear.Array2DRowRealMatrix;
 import org.apache.commons.math3.linear.LUDecomposition;
 
 import sceneManager.SceneNode;
 import utilityManager.MathBox;
 
 import com.jogamp.newt.event.KeyEvent;
 import com.jogamp.newt.event.KeyListener;
 import com.jogamp.newt.event.MouseEvent;
 import com.jogamp.newt.event.MouseListener;
 import com.jogamp.opengl.util.gl2.GLUT;
 
 import eventManager.Observable;
 import eventManager.Observer;
 import eventManager.UpdateEventType;
 
 public class SceneRenderer implements GLEventListener, Observer<ObjectEvent>, Observable<InputUpdateEvent>, KeyListener, MouseListener {
 	private ArrayList<Observer<InputUpdateEvent>> observers = new ArrayList<Observer<InputUpdateEvent>>();
 	
 	private ArrayList<ObjectEvent> interfaceDisplayQueue = new ArrayList<ObjectEvent>();
 	private ArrayList<ObjectEvent> objectDisplayQueue = new ArrayList<ObjectEvent>();
 	
 	private ObjectEvent currentInterfaceDisplayEvent;
 	private ObjectEvent currentObjectDisplayEvent;
 
 	private GL3bc gl;
 	private GLU glu = new GLU();
 
 	private Camera camera;
 
 	private Hashtable<String, Boolean> cameraControlTable = new Hashtable<String, Boolean>();
 
 	private int mouseX;
 	private int mouseY;
 
 	private double worldX;
 	private double worldY;
 
 	@Override
 	public void display(GLAutoDrawable drawable) {
 		gl = drawable.getGL().getGL3bc();
 
 		gl.glClear(GL3bc.GL_COLOR_BUFFER_BIT | GL3bc.GL_DEPTH_BUFFER_BIT);
 
 		gl.glMatrixMode(GL3bc.GL_PROJECTION);
 		gl.glLoadIdentity();
 		
 		gl.glOrtho(0, Constants.viewWidth, 0, Constants.viewHeight, -1, 1);
 		drawSkybox(gl);
 		
 		gl.glMatrixMode(GL3bc.GL_PROJECTION);
 		gl.glLoadIdentity();
 		
		glu.gluPerspective(camera.getAngle(), 1.0f, 1.0f, 1000.0f);
 		gl.glScalef(1.0f, -1.0f, 1.0f);
 		
 		gl.glMatrixMode(GL3bc.GL_MODELVIEW);
 		gl.glLoadIdentity();
 
 		updateCamera(gl);
 
 		renderObjectDisplayNodes(gl);
 		drawDebugCube(gl, (float) camera.getView().getX(), (float) camera.getView().getY(), (float) camera.getView().getZ());
 		drawText(gl);
 		
 		gl.glMatrixMode(GL3bc.GL_PROJECTION);
 		
 		gl.glLoadIdentity();
 		gl.glOrtho(0, Constants.viewWidth, 0, Constants.viewHeight, -1, 1);
 		
 		renderInterfaceDisplayNodes(gl);
 	}
 
 	private void drawSkybox(GL3bc gl) {
 		if (!TextureLoader.getCurrentTextureName().equals("SkyBox1")) {
 			TextureLoader.loadTexture(gl, "SkyBox1", "stars1.png");
 			TextureLoader.setCurrentTexture(gl, "SkyBox1");
 		}
 
 		gl.glEnable(GL3bc.GL_TEXTURE_2D);
 		gl.glEnable(GL3bc.GL_DEPTH_TEST);
 		gl.glDisable(GL3bc.GL_LIGHTING);
 		gl.glDisable(GL3bc.GL_BLEND);
 		
 		gl.glPushMatrix();
 		
 		gl.glTranslatef(Constants.viewWidth/2.0f, Constants.viewHeight/2, 0.0f);
 		gl.glColor4f(1, 1, 1, 1);
 
 		// Render the front quad
 		gl.glBegin(GL3bc.GL_QUADS);
 		gl.glTexCoord2f(1, 1);
 		gl.glVertex3f(Constants.viewWidth/2, Constants.viewHeight/2, 0.0f);
 		gl.glTexCoord2f(1, 0);
 		gl.glVertex3f(Constants.viewWidth/2, -Constants.viewHeight/2, 0.0f);
 		gl.glTexCoord2f(0, 0);
 		gl.glVertex3f(-Constants.viewWidth/2, -Constants.viewHeight/2, 0.0f);
 		gl.glTexCoord2f(0, 1);
 		gl.glVertex3f(-Constants.viewWidth/2, Constants.viewHeight/2, 0.0f);
 		gl.glEnd();
 		
 		gl.glDisable(GL3bc.GL_TEXTURE_2D);
 		gl.glDisable(GL3bc.GL_DEPTH_TEST);
 		gl.glEnable(GL3bc.GL_LIGHTING);
 		gl.glEnable(GL3bc.GL_BLEND);
 	}
 
 	private void updateCamera(GL3bc gl) {
 		if (mouseX <= 5) {
 			camera.translate(new Vector3D(1.0, 0.0, 0.0), -2.5);
 		}
 		else if (mouseX >= Constants.viewWidth - 6) {
 			camera.translate(new Vector3D(1.0, 0.0, 0.0), 2.5);
 		}
 		else if (mouseY <= 5) {
 			camera.translate(new Vector3D(0.0, 1.0, 0.0), -2.5);
 		}
 		else if (mouseY >= Constants.viewHeight - 6) {
 			camera.translate(new Vector3D(0.0, 1.0, 0.0), 2.5);
 		}
 
 		// update camera input table to include most recent translations and/or
 		// rotations
 		if (cameraControlTable.get("rLeft")) {
 			camera.setAxis(new Vector3D(0, 1, 0));
 			camera.setRotation(0.025);
 		}
 		if (cameraControlTable.get("rRight")) {
 			camera.setAxis(new Vector3D(0, 1, 0));
 			camera.setRotation(-0.025);
 		}
 		if (cameraControlTable.get("rUp")) {
 			camera.setAxis(new Vector3D(1, 0, 0));
 			camera.setRotation(0.025);
 		}
 		if (cameraControlTable.get("rDown")) {
 			camera.setAxis(new Vector3D(1, 0, 0));
 			camera.setRotation(-0.025);
 		}
 		if (cameraControlTable.get("left")) {
 			camera.translate(new Vector3D(1, 0, 0), -10);
 		}
 		if (cameraControlTable.get("right")) {
 			camera.translate(new Vector3D(1, 0, 0), 10);
 		}
 		if (cameraControlTable.get("up")) {
 			camera.translate(new Vector3D(0, 1, 0), -10);
 		}
 		if (cameraControlTable.get("down")) {
 			camera.translate(new Vector3D(0, 1, 0), 10);
 		}
 		
 		gl.glTranslatef((float) -camera.getView().getX(), (float) -camera.getView().getY(), (float) -camera.getCam().getZ());
 		
 		setWorldCoordinates(gl, mouseX, mouseY);
 	}
 
 	private void setWorldCoordinates(GL3bc gl, int screenX, int screenY) {
 		double[] inPoint = new double[4];
 		double[] outPoint = new double[4];
 		double[] projection = new double[16];
 		double[] modelview = new double[16];
 
 		inPoint = new double[] { screenX * 2.0 / Constants.viewWidth - 1.0, (Constants.viewHeight - screenY) * 2.0 / Constants.viewHeight - 1.0, -1.0, 1.0 };
 
 		// obtain ogl matrices
 		gl.glGetDoublev(GL3bc.GL_PROJECTION_MATRIX, projection, 0);
 		gl.glGetDoublev(GL3bc.GL_MODELVIEW_MATRIX, modelview, 0);
 
 		// multiply projection and modelview matrices and then invert
 		Array2DRowRealMatrix projectionMatrix = new Array2DRowRealMatrix(MathBox.unflattenMatrix4(projection));
 		Array2DRowRealMatrix modelviewMatrix = new Array2DRowRealMatrix(MathBox.unflattenMatrix4(modelview));
 		Array2DRowRealMatrix pmMatrix = projectionMatrix.multiply(modelviewMatrix);
 		Array2DRowRealMatrix inverseMatrix = (Array2DRowRealMatrix) new LUDecomposition(pmMatrix).getSolver().getInverse();
 
 		// mutliply inverse matrix by normalized point
 		outPoint = inverseMatrix.operate(inPoint);
 
 		// scale for perspective and translate to viewing position
 		worldX = outPoint[0] * camera.getCam().getZ() + camera.getView().getX();
 		worldY = outPoint[1] * camera.getCam().getZ() + camera.getView().getY();
 	}
 
 	private void renderObjectDisplayNodes(GL3bc gl) {
 		if (currentObjectDisplayEvent != null) {
 			ArrayList<SceneNode> sceneNodes = currentObjectDisplayEvent.getSceneNodes();
 			for (SceneNode sceneNode : sceneNodes) {
 				sceneNode.update(gl);
 			}
 		}
 		
 		if (objectDisplayQueue.size() > 0) {
 			currentObjectDisplayEvent = objectDisplayQueue.remove(0);
 		}
 	}
 	
 	private void renderInterfaceDisplayNodes(GL3bc gl) {
 		if (currentInterfaceDisplayEvent != null) {
 			ArrayList<SceneNode> sceneNodes = currentInterfaceDisplayEvent.getSceneNodes();
 			for (SceneNode sceneNode : sceneNodes) {
 				sceneNode.update(gl);
 			}
 		}
 		
 		if (interfaceDisplayQueue.size() > 0) {
 			currentInterfaceDisplayEvent = interfaceDisplayQueue.remove(0);
 		}
 	}
 
 	private void drawText(GL3bc gl) {
 		gl.glMatrixMode(GL3bc.GL_PROJECTION);
 		gl.glLoadIdentity();
 
 		gl.glOrtho(0f, Constants.viewWidth/2, 0f, Constants.viewHeight, 0f, 1f);
 
 		gl.glMatrixMode(GL3bc.GL_MODELVIEW);
 		gl.glLoadIdentity();
 
 		gl.glDisable(GL3bc.GL_DEPTH_TEST);
 		gl.glDisable(GL3bc.GL_TEXTURE);
 		gl.glDisable(GL3bc.GL_TEXTURE_2D);
 		gl.glDisable(GL3bc.GL_LIGHTING);
 
 		gl.glPushMatrix();
 		GLUT glut = new GLUT();
 		gl.glRasterPos2i(30, 30);
 		gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
 		glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, "Mouse position = " + worldX + ", " + worldY);
 		gl.glPopMatrix();
 
 		gl.glEnable(GL3bc.GL_TEXTURE);
 		gl.glEnable(GL3bc.GL_TEXTURE_2D);
 		gl.glEnable(GL3bc.GL_DEPTH_TEST);
 		gl.glEnable(GL3bc.GL_LIGHTING);
 	}
 
 	private void drawDebugCube(GL3bc gl, float x, float y, float z) {
 		GLUT glut = new GLUT();
 
 		gl.glPushMatrix();
 		gl.glDisable(GL3bc.GL_LIGHTING);
 		gl.glLineWidth(1.0f);
 		gl.glTranslatef(x, y, z);
 
 		// model objects
 		gl.glColor3f(1.0f, 1.0f, 1.0f); // origin
 		glut.glutWireCube(1.0f);
 
 		gl.glPushMatrix(); // x axis
 		gl.glColor3f(1f, 0f, 0f);
 		gl.glBegin(GL3bc.GL_LINES);
 		gl.glVertex3f(0, 0, 0);
 		gl.glVertex3f(0 + 50, 0, 0);
 		gl.glEnd();
 		gl.glTranslatef(0 + 50, 0, 0);
 		glut.glutWireCube(10f);
 		gl.glPopMatrix();
 
 		gl.glPushMatrix(); // y axis
 		gl.glColor3f(0f, 1f, 0f);
 		gl.glBegin(GL3bc.GL_LINES);
 		gl.glVertex3f(0, 0, 0);
 		gl.glVertex3f(0, 0 + 50, 0);
 		gl.glEnd();
 		gl.glTranslatef(0, 0 + 50, 0);
 		glut.glutWireCube(10f);
 		gl.glPopMatrix();
 
 		gl.glPushMatrix(); // z axis
 		gl.glColor3f(0f, 0f, 1f);
 		gl.glBegin(GL3bc.GL_LINES);
 		gl.glVertex3f(0, 0, 0);
 		gl.glVertex3f(0, 0, 50);
 		gl.glEnd();
 		gl.glTranslatef(0, 0, 50);
 		glut.glutWireCube(10f);
 		gl.glPopMatrix();
 
 		gl.glEnable(GL3bc.GL_LIGHTING);
 		gl.glPopMatrix();
 	}
 
 	@Override
 	public void init(GLAutoDrawable drawable) {
 		GL3bc gl = drawable.getGL().getGL3bc();
 
 		gl.glEnable(GL.GL_DEPTH_TEST);
 
 		gl.glEnable(GL3bc.GL_BLEND);
 		gl.glBlendFunc(GL3bc.GL_SRC_ALPHA, GL3bc.GL_ONE_MINUS_SRC_ALPHA);
 
 		// initFog(gl);
 		initLighting(gl);
 
 		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
 
 		camera = new Camera(new Vector3D(0, 0, 1000), new Vector3D(Constants.modelWidth / 2, Constants.modelHeight / 2, 0), 70);
 
 		cameraControlTable.put("left", false);
 		cameraControlTable.put("right", false);
 		cameraControlTable.put("up", false);
 		cameraControlTable.put("down", false);
 		cameraControlTable.put("rLeft", false);
 		cameraControlTable.put("rRight", false);
 		cameraControlTable.put("rUp", false);
 		cameraControlTable.put("rDown", false);
 	}
 
 	@SuppressWarnings("unused")
 	private void initFog(GL3bc gl) {
 		float density = 0.1f;
 		float[] colour = new float[] { 0.5f, 0.5f, 0.5f, 1.0f };
 
 		gl.glEnable(GL3bc.GL_FOG);
 
 		gl.glFogi(GL3bc.GL_FOG_MODE, GL3bc.GL_LINEAR);
 		gl.glFogf(GL3bc.GL_FOG_START, 100.0f);
 		gl.glFogf(GL3bc.GL_FOG_END, 1200.0f);
 
 		gl.glFogfv(GL3bc.GL_FOG_COLOR, colour, 0);
 
 		gl.glFogf(GL3bc.GL_FOG_DENSITY, density);
 
 		gl.glHint(GL3bc.GL_FOG_HINT, GL3bc.GL_NICEST);
 	}
 
 	private void initLighting(GL3bc gl) {
 		gl.glEnable(GL3bc.GL_LIGHTING);
 
 		gl.glLightModelfv(GL3bc.GL_LIGHT_MODEL_AMBIENT, new float[] { 0.1f, 0.1f, 0.1f, 1.0f }, 0);
 
 		gl.glEnable(GL3bc.GL_NORMALIZE);
 
 		gl.glEnable(GL3bc.GL_LINE_SMOOTH);
 		gl.glEnable(GL3bc.GL_POLYGON_SMOOTH);
 		gl.glShadeModel(GL3bc.GL_SMOOTH);
 
 		gl.glEnable(GL3bc.GL_LIGHT0);
 
 		gl.glLightfv(GL3bc.GL_LIGHT0, GL3bc.GL_AMBIENT, new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, 0);
 		gl.glLightfv(GL3bc.GL_LIGHT0, GL3bc.GL_DIFFUSE, new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, 0);
 		gl.glLightfv(GL3bc.GL_LIGHT0, GL3bc.GL_SPECULAR, new float[] { 1.0f, 1.0f, 1.0f, 1.0f }, 0);
 
 		gl.glMaterialfv(GL3bc.GL_FRONT, GL3bc.GL_AMBIENT, new float[] { 0.3f, 0.3f, 0.3f, 1.0f }, 0);
 		gl.glMaterialfv(GL3bc.GL_FRONT, GL3bc.GL_DIFFUSE, new float[] { 0.4f, 0.4f, 0.4f, 1.0f }, 0);
 		gl.glMaterialfv(GL3bc.GL_FRONT, GL3bc.GL_SPECULAR, new float[] { 0.0f, 0.0f, 0.0f, 1.0f }, 0);
 
 		gl.glLightfv(GL3bc.GL_LIGHT0, GL3bc.GL_POSITION, new float[] { -1.0f, -1.0f, 1.0f, 0.0f }, 0);
 	}
 
 	@Override
 	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3, int arg4) {
 	}
 
 	@Override
 	public void dispose(GLAutoDrawable arg0) {
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 			cameraControlTable.put("rLeft", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			cameraControlTable.put("rRight", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			cameraControlTable.put("rUp", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 			cameraControlTable.put("rDown", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_A) {
 			cameraControlTable.put("left", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_D) {
 			cameraControlTable.put("right", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_W) {
 			cameraControlTable.put("up", true);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_S) {
 			cameraControlTable.put("down", true);
 		}
 
 		InputUpdateEvent keyPressedEvent = new InputUpdateEvent(this, e);
 		updateObservers(keyPressedEvent);
 	}
 
 	boolean originShift = false;
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
 			cameraControlTable.put("rLeft", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
 			cameraControlTable.put("rRight", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_UP) {
 			cameraControlTable.put("rUp", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_DOWN) {
 			cameraControlTable.put("rDown", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_A) {
 			cameraControlTable.put("left", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_D) {
 			cameraControlTable.put("right", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_W) {
 			cameraControlTable.put("up", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_S) {
 			cameraControlTable.put("down", false);
 		}
 		if (e.getKeyCode() == KeyEvent.VK_N) {
 			Constants.displayNormals = Constants.displayNormals ? false : true;
 		}
 		if (e.getKeyCode() == KeyEvent.VK_O) {
 			if (!originShift) {
 				camera.translate(new Vector3D(1.0, 0.0, 0.0), -25000);
 				camera.translate(new Vector3D(0.0, 1.0, 0.0), -25000);
 				originShift = true;
 			}
 			else {
 				camera.translate(new Vector3D(1.0, 0.0, 0.0), 25000);
 				camera.translate(new Vector3D(0.0, 1.0, 0.0), 25000);
 				originShift = false;
 			}
 		}
 		InputUpdateEvent inputEvent = new InputUpdateEvent(this, e);
 		updateObservers(inputEvent);
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		updateObservers(new InputUpdateEvent(this, new ExtendedMouseEvent(e, new Vector2D(worldX, worldY))));
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		updateObservers(new InputUpdateEvent(this, new ExtendedMouseEvent(e, new Vector2D(worldX, worldY))));
 	}
 
 	@Override
 	public void mouseDragged(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseWheelMoved(MouseEvent e) {
 		camera.translate(new Vector3D(0, 0, 1), e.getWheelRotation() * 50);
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		mouseX = e.getX();
 		mouseY = e.getY();
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent arg0) {
 	}
 
 	@Override
 	public void addObserver(Observer<InputUpdateEvent> observer) {
 		observers.add(observer);
 	}
 
 	@Override
 	public void removeObserver(Observer<InputUpdateEvent> observer) {
 		observers.remove(observer);
 	}
 
 	@Override
 	public void updateObservers(InputUpdateEvent inputUpdateEvent) {
 		for (Observer<InputUpdateEvent> observer : observers) {
 			observer.update(inputUpdateEvent);
 		}
 
 	}
 
 	@Override
 	public void update(ObjectEvent objectEvent) {
 		if (objectEvent.getEventType() == UpdateEventType.OBJECT_DISPLAY) {
 			objectDisplayQueue.add(objectEvent);
 		}
 		else if (objectEvent.getEventType() == UpdateEventType.INTERFACE_DISPLAY) {
 			interfaceDisplayQueue.add(objectEvent);
 		}
 	}
 }
