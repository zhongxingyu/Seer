 package com.chris.spacedragon;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.Keys;
 import com.badlogic.gdx.Input.Orientation;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.files.FileHandleStream;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Mesh;
 import com.badlogic.gdx.graphics.PerspectiveCamera;
 import com.badlogic.gdx.graphics.VertexAttribute;
 import com.badlogic.gdx.graphics.VertexAttributes;
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
 	public Vector3 Ypr = new Vector3();
 	
 	public Quaternion rotationspeed = new Quaternion();
 	public Quaternion Ident = new Quaternion(0, 0, 0, 1);
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
 	public Vector3 ModelAxisUp;
 
 	public static float WingDist = 1;
 	public static float FRa = -5; // forward accel with wing swinging
 	public static Vector3 GRAV = new Vector3(0, -9.81f, 0); // basic Downward
 															// accel
 	public static float TopUPa = 2; // upward acceleration with wing at the //
 									// top
 	public static float DownUPa = 1f; // upward acceleration with wing at //
 										// the bottom
 	public static float SwUPa = 20; // upward acceleration with wing swinging
 	public static float WingMovePerSec = 2.0f; // wing movement per //
 												// second;
 
 	// loading of assets etc
 	public void create() {
 		int i = 0;
 
 	
 
 		/*
 		 * meshBody = new Mesh(true, 3, 0, // static mesh with 4 vertices and no
 		 * // indices new VertexAttribute(Usage.Position, 3,
 		 * ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(
 		 * Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE +
 		 * "0"));
 		 */
 
 		// FileHandle handle = new FileHandle("data/models/spacedragon.obj");
 		meshBody = ObjLoader.loadObj(Gdx.files.internal("data/models/body.obj")
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
 
 		meshWing = ObjLoader.loadObj(Gdx.files.internal("data/models/wing.obj")
 				.read());
 
 	
 		rightWingDown = 1.0f;
 		leftWingDown = 1.0f;
 		
 		VertexAttributes vo = meshBody.getVertexAttributes();
 		lastUpdate = System.currentTimeMillis();
 		FileHandle vertexShader = Gdx.files.internal("data/shader/dragon.vsh");
 		FileHandle fragmentShader =  Gdx.files.internal("data/shader/dragon.fsh");
 		shaderDragon = new ShaderProgram(vertexShader, fragmentShader);
 
 		ModelAxis = new Vector3(0, 0, -1);
 		ModelAxisUp = new Vector3(0, 1, 0);
 		orientation.idt();
 		rotationspeed.idt();
 	}
 
 	public void render(PerspectiveCamera camera) {
 		Matrix4 mat = camera.combined.cpy();
 		mat.translate(position);
 		Matrix4 shadowmat = mat.cpy();
 		mat.rotate(orientation);
 		Matrix4 wing = mat.cpy();
 		shaderDragon.begin();
 
 		// render body
 		shaderDragon.setUniformMatrix("u_worldView", mat);
 		meshBody.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		// render shadow
 		shadowmat.translate(0, -position.y + 0.01f, 0);
 		shadowmat.scale(1, 0, 1);
 		shadowmat.rotate(orientation);
 		shaderDragon.setUniformMatrix("u_worldView", shadowmat);
 		meshBody.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		// render left wing
 		wing.rotate(0, 0, -1, 50.0f);
 		wing.rotate(0, 0, -1, leftWingDown * -70.0f);
 		shaderDragon.setUniformMatrix("u_worldView", wing);
 
 		meshWing.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		// render right wing
 		wing = mat.cpy();
 		wing.scale(-1, 1, 1);
 		wing.rotate(0, 0, -1, 50.0f);
 		wing.rotate(0, 0, -1, rightWingDown * -70.0f);
 
 		shaderDragon.setUniformMatrix("u_worldView", wing);
 
 		meshWing.render(Game.shaderMain, GL20.GL_TRIANGLES);
 
 		shaderDragon.end();
 	}
 
 	// updates position of dragon
	public void update(long dt) {
 		// pc input for now
 		Vector3 Left = new Vector3();
 		Vector3 Right = new Vector3();
 		Vector3 LeftDist = new Vector3(-WingDist / 4, 0, 1);
 		Vector3 RightDist = new Vector3(WingDist / 4, 0, 1);
 
 		ModelAxis.x = 0;
 		ModelAxis.y = 0;
 		ModelAxis.z = -1;
 
 		ModelAxisUp.x = 0;
 		ModelAxisUp.y = 1;
 		ModelAxisUp.z = 0;
 		orientation.transform(ModelAxis);
 		orientation.transform(ModelAxisUp);
 		speed.set(0, 0, 0);
 		speed.set(0,0,0);
 		
 		float UpUpmove = 2;
 		float SwingUpMove =3;
 		float SwingFrontMove =4;
 		float UpFrontMove = 4;
 		float DownFrontMove=1;
 		float DownUpMove=-1;
 	
 		
		float timestep = dt / 1000f;
 		if (Gdx.input.isKeyPressed(Keys.LEFT)) {
 			if (leftWingDown < 1) {
 				//roll.mul(new Quaternion(ModelAxis,- 1));
 				Ypr.x += 60*timestep;
 				Ypr.z +=100*timestep;
 				leftWingDown += WingMovePerSec * timestep;
 				speed.y += SwingUpMove * timestep;
 				speed.z += -SwingFrontMove * timestep;
 			} else if (leftWingDown >= 1) {
 				speed.y += DownUpMove * timestep;
 				speed.z += -DownFrontMove * timestep;
 				//Ypr.z -=60*timestep;
 			}
 			leftKeyDown = true;
 		} else {
 			if (leftWingDown > 0) {
 				leftWingDown -= WingMovePerSec * timestep;
 			}
 			speed.y +=(float) (- Math.exp(0.5 - leftWingDown) * timestep);
 			speed.z +=- UpFrontMove * timestep;
 		}
 
 		if (Gdx.input.isKeyPressed(Keys.RIGHT)) {
 			rightKeyDown = true;
 			if (rightWingDown < 1) {
 				//roll.mul(new Quaternion(ModelAxis,1));
 				//orientation.mul(new Quaternion(ModelAxisUp, -1));
 				Ypr.x += -60*timestep;
 				Ypr.z +=-100*timestep;
 				rightWingDown += WingMovePerSec * timestep;
 				speed.y += SwingUpMove * timestep;
 				speed.z += -SwingFrontMove * timestep;
 			} else if (rightWingDown >= 1) {
 				speed.y += -DownUpMove * timestep;
 				speed.z += -DownFrontMove * timestep;
 				//rientation.mul(new Quaternion(ModelAxisUp,- 0.5f));
 			} 
 			rightKeyDown = true;
 		} else {
 			if (rightWingDown > 0) {
 				rightWingDown -= WingMovePerSec * timestep;			
 			}
 			speed.y +=(float) (- Math.exp(0.5 - rightWingDown) * timestep);
 			speed.z +=- UpFrontMove * timestep;
 		}
 		
 		orientation.setEulerAngles(Ypr.x,Ypr.y,Ypr.z);
 		//orientation.mul(roll);
 		orientation.transform(speed);
 		//Ypr.slerp(target, alpha)
 		position.add(speed);
 		Ypr.lerp(Ypr.tmp().set(Ypr.x, Ypr.y, 0), 0.01f);
 		
 		//System.out.println(""+ );
 		//System.out.println(""+ Ypr.tmp().set(Ypr.x, Ypr.y, 0));
 		//orientation.slerp(Ident, 0.1f);
 		// speed.slerp(Vector3.Zero, 0.2f);
 		lastUpdate = System.currentTimeMillis();
 
 	}
 }
