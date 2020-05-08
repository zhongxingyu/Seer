 package com.chris.spacedragon;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.files.FileHandleStream;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 import com.badlogic.gdx.graphics.g3d.loaders.obj.ObjLoader;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.Matrix4;
 import com.badlogic.gdx.math.Quaternion;
 import com.badlogic.gdx.math.Vector3;
 
 public class Dragon {
 	public Vector3 position = new Vector3();
 	public Vector3 speed = new Vector3();
 	public Quaternion orientation = new Quaternion();
 	public Quaternion rotationspeed = new Quaternion();
 	public Quaternion Ident = new Quaternion(0,0,0,1);
 	// 0 = left wing up, 1 = left wing down
 	public float leftWingDown;
 	// 0 = right wing up, 1 = right wing down
 	public float rightWingDown;
 
 	public float[] vertsBody = new float[15];
 	public float[] vertsWingUp = new float[15];
 	public Mesh meshBody;
 	public Mesh meshWing;
 
 	public static ShaderProgram shaderDragon;
 
 	public long lastUpdate;
 	public Boolean leftKeyDown;
 	public long lastLeftKeyDown;
 	public Boolean rightKeyDown;
 	public long lastRightKeyDown;
 	public Vector3 ModelAxis;
 
 	public static float WingDist = 1;
 	public static float FRa = -5; // forward accel with wing swinging
 	public static Vector3 GRAV = new Vector3(0,-9.81f,0); // basic Downward accel
 	public static float TopUPa = 2; // upward acceleration with wing at the //
 									// top
 	public static float DownUPa = 1f; // upward acceleration with wing at //
 										// the bottom
 	public static float SwUPa = 20; // upward acceleration with wing swinging
 	public static float WingMovePerMSec = 5f / 1000f; // wing movement per //
 														// second;
 
 	// loading of assets etc
 	public void create() {
 		int i = 0;
 
 		vertsBody[i++] = 0; // x1
 		vertsBody[i++] = 0; // y1
 		vertsBody[i++] = -1;
 		vertsBody[i++] = 0f; // u1
 		vertsBody[i++] = 0f; // v1
 
 		vertsBody[i++] = 1f; // x2
 		vertsBody[i++] = 0; // y2
 		vertsBody[i++] = 0;
 		vertsBody[i++] = 1f; // u2
 		vertsBody[i++] = 0f; // v2
 
 		vertsBody[i++] = -1f; // x3
 		vertsBody[i++] = 0f; // y2
 		vertsBody[i++] = 0;
 		vertsBody[i++] = 1f; // u3
 		vertsBody[i++] = 1f; // v3
 
 		i = 0;
 
 		vertsWingUp[i++] = -1; // x1
 		vertsWingUp[i++] = 0; // y1
 		vertsWingUp[i++] = 0;
 		vertsWingUp[i++] = 0f; // u1
 		vertsWingUp[i++] = 0f; // v1
 
 		vertsWingUp[i++] = -2f; // x2
 		vertsWingUp[i++] = 0; // y2
 		vertsWingUp[i++] = 0;
 		vertsWingUp[i++] = 1f; // u2
 		vertsWingUp[i++] = 0f; // v2
 
 		vertsWingUp[i++] = -2f; // x3
 		vertsWingUp[i++] = 0f; // y2
 		vertsWingUp[i++] = 1;
 		vertsWingUp[i++] = 1f; // u3
 		vertsWingUp[i++] = 1f; // v3
 
 		/*
 		 * meshBody = new Mesh(true, 3, 0, // static mesh with 4 vertices and no
 		 * // indices new VertexAttribute(Usage.Position, 3,
 		 * ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
 		 * Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE +
 		 * "0"));
 		 */
 
 		// FileHandle handle = new FileHandle("models/spacedragon.obj");
 		meshBody = ObjLoader.loadObj(Gdx.files.internal("models/body.obj")
 				.read());
 		/*
 		 * meshWing = new Mesh(true, 3, 0, // static mesh with 4 vertices and //
 		 * no // indices new VertexAttribute(Usage.Position, 3,
 		 * ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
 		 * Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE +
 		 * "0"));
 		 * 
 		 * //meshBody.setVertices(vertsBody); meshWing.setVertices(vertsWingUp);
 		 */
 
 		meshWing = ObjLoader.loadObj(Gdx.files.internal("models/wing.obj")
 				.read());
 
 		rightWingDown = 1.0f;
 		leftWingDown = 1.0f;
 
 		lastUpdate = System.currentTimeMillis();
 		String vertexShader = "attribute vec4 a_position;    \n"
 				+ "attribute vec4 a_color;\n" + "attribute vec2 a_texCoord0;\n"
 				+ "uniform mat4 u_worldView;\n" + "varying vec4 v_color;"
 				+ "varying vec2 v_texCoords;"
 				+ "void main()                  \n"
 				+ "{                            \n"
 				+ "   v_color = vec4(0.3, 1.0, 0.3, 1); \n"
 				+ "   v_texCoords = a_texCoord0; \n"
 				+ "   gl_Position =  u_worldView * a_position;  \n"
 				+ "}                            \n";
 		String fragmentShader = "#ifdef GL_ES\n" + "precision mediump float;\n"
 				+ "#endif\n" + "varying vec4 v_color;\n"
 				+ "varying vec2 v_texCoords;\n"
 				+ "uniform sampler2D u_texture;\n"
 				+ "void main()                                  \n"
 				+ "{                                            \n"
 				+ "  gl_FragColor = v_color;\n" + "}";
 		shaderDragon = new ShaderProgram(vertexShader, fragmentShader);
 
 		ModelAxis = new Vector3(0, 0, -1);
 		orientation.idt();
 		rotationspeed.idt();
 	}
 
 	public void render(PerspectiveCamera camera) {
 		Matrix4 mat = camera.combined.cpy();
 		mat.translate(position);
		mat.rotate(orientation);
 		Matrix4 shadowmat = mat.cpy();
 		Matrix4 wing = mat.cpy();
 		shaderDragon.begin();
 
 		// render body
 		shaderDragon.setUniformMatrix("u_worldView", mat);
 		meshBody.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		// render shadow
 		shadowmat.translate(0, -position.y + 0.01f, 0);
 		shadowmat.scale(1, 0, 1);
 		shaderDragon.setUniformMatrix("u_worldView", shadowmat);
 		meshBody.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		// render left wing
 		wing.rotate(0, 0, -1, leftWingDown * -70.0f);
 		shaderDragon.setUniformMatrix("u_worldView", wing);
 
 		meshWing.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		// render right wing
 		wing = mat.cpy();
 		wing.scale(-1, 1, 1);
 		wing.rotate(0, 0, -1, rightWingDown * -70.0f);
 
 		shaderDragon.setUniformMatrix("u_worldView", wing);
 
 		meshWing.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		shaderDragon.end();
 	}
 
 	// updates position of dragon
 	public void update() {
 		// pc input for now
 		Vector3 Left = new Vector3();
 		Vector3 Right = new Vector3();
 		Vector3 LeftDist = new Vector3(-WingDist, 0, 0);
 		Vector3 RightDist = new Vector3(WingDist, 0, 0);
 
 		ModelAxis.x = 0;
 		ModelAxis.y = 0;
 		ModelAxis.z = -1;
 
 		long timestep = System.currentTimeMillis() - lastUpdate;
 		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
 			if (leftWingDown < 1) {
 				Left.y = SwUPa;
 				Left.z = FRa;
 				leftWingDown += WingMovePerMSec * timestep;
 			} else if (leftWingDown >= 1) {
 				Left.y = DownUPa;
 				Left.z = 0;
 			} else {
 				Left.y = TopUPa;
 				Left.z = 0;
 			}
 			leftKeyDown = true;
 		} else {
 			if (leftWingDown > 0) {
 				Left.z = FRa;
 				leftWingDown -= WingMovePerMSec * timestep;
 			} else if (leftWingDown <= 0) {
 				Left.y = TopUPa;
 				Left.z = 0;
 			}
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
 			rightKeyDown = true;
 			if (rightWingDown < 1) {
 				rightWingDown += WingMovePerMSec * timestep;
 				Right.y = SwUPa;
 				Right.z = FRa;
 			} else if (rightWingDown >= 1) {
 				Right.z = 0;
 				Right.y = DownUPa;
 			} else {
 				Right.y = TopUPa;
 				Right.z = 0;
 			}
 		} else {
 			if (rightWingDown > 0) {
 				rightWingDown -= WingMovePerMSec * timestep;
 				Right.z = FRa;
 			} else if (rightWingDown <= 0) {
 				Right.y = TopUPa;
 				Right.z = 0;
 			}
 		}
 
 		orientation.transform(Left);
 		orientation.transform(Right);
 		orientation.transform(LeftDist);
 		orientation.transform(RightDist);
 		orientation.transform(ModelAxis);
 		speed.add(Left.tmp().mul(timestep/10000f)).add(Right.tmp2().mul(timestep/10000f)).add(GRAV.tmp().mul(timestep/10000f));
 		position.add(speed.tmp().mul(timestep/1000f));
 
 
 		Left.crs(LeftDist);
 		Right.crs(RightDist);
 		Right.add(Left);
 		Quaternion q = new Quaternion(ModelAxis, (float) (Right.z * timestep/500f )).nor();
 		
 		rotationspeed.mul(q).nor();
 		
 		System.out.println("" + position);
 		
 		orientation.mul(new Quaternion(rotationspeed)).nor();
 		
 		rotationspeed.slerp(Ident,  0.4f);
 		speed.slerp(Vector3.Zero, 0.2f);
 		lastUpdate = System.currentTimeMillis();
 
 	}
 }
