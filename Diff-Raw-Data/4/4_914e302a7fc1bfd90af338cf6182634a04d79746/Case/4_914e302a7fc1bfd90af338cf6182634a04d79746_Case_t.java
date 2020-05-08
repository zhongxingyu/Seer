 package Case;
 
 import javax.vecmath.*;
 
 import java.awt.*;
 import java.awt.event.*;
 import javax.media.j3d.*;
 
 import com.sun.j3d.utils.picking.PickCanvas;
 import com.sun.j3d.utils.picking.PickTool;
 import com.sun.j3d.utils.universe.*;
 import com.sun.j3d.utils.geometry.*;
 import com.sun.j3d.utils.image.TextureLoader;
 
 import java.applet.*;
 import java.net.URL;
 
 import com.sun.j3d.utils.applet.MainFrame;
 import com.sun.j3d.utils.behaviors.vp.OrbitBehavior;
 
 public class Case extends Applet {
 	public static void main(String[] args) {
 		new MainFrame(new Case(), 800, 800);
 	}
 	PickCanvas pc;
 	public void init() {
 		// create canvas
 		GraphicsConfiguration gc = SimpleUniverse.getPreferredConfiguration();
 		Canvas3D cv = new Canvas3D(gc);
 		setLayout(new BorderLayout());
 		add(cv, BorderLayout.CENTER);
 		BranchGroup bg = createSceneGraph();
 		bg.compile();
 		pc = new PickCanvas(cv, bg);
 	    pc.setMode(PickTool.GEOMETRY);
 		SimpleUniverse su = new SimpleUniverse(cv);
 		su.getViewingPlatform().setNominalViewingTransform();
 		
 		//Skjermbevegelse
 		OrbitBehavior orbit = new OrbitBehavior(cv);
 	    orbit.setSchedulingBounds(new BoundingSphere());
 	    su.getViewingPlatform().setViewPlatformBehavior(orbit);
 		
 	    su.addBranchGraph(bg);
 	}
 	
 	TransformGroup[]  shapeMove;
 	Primitive[]       shapes;
 	Appearance[]      appearance;
 	Material          material;
 	BoundingSphere    bounds;
	
 
 	private BranchGroup createSceneGraph() {
 		int n = 11;
 		
 		/* root */
 		BranchGroup root = new BranchGroup();
 		bounds = new BoundingSphere();
 		
 		/* testTransform */
 		Transform3D tr = new Transform3D();
 		tr.setTranslation(new Vector3f(0.1f, 0.1f, 0.1f));
 		TransformGroup testTransform = new TransformGroup(tr);
 		testTransform.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		
 		//Spin
 		root.addChild(testTransform);
 		Alpha alpha = new Alpha(0, 8000);
 		RotationInterpolator rotator = new RotationInterpolator(alpha, testTransform);
 		rotator.setSchedulingBounds(bounds);
 		testTransform.addChild(rotator);
 		
 		/* background and lights */
 		Background background = new Background(1.0f, 1.0f, 1.0f);
 		//Background background = new Background(0f, 0f, 0f);
 		background.setApplicationBounds(bounds);
 		root.addChild(background);
 		AmbientLight light = new AmbientLight(true, new Color3f(Color.white));
 		light.setInfluencingBounds(bounds);
 		root.addChild(light);
 		PointLight ptlight = new PointLight(new Color3f(Color.BLUE),
 				new Point3f(3f, 3f, 3f), new Point3f(1f, 0f, 0f));
 		ptlight.setInfluencingBounds(bounds);
 		root.addChild(ptlight);
 		
 		/* Material */
 		material = new Material();
 		
 		// temp
 	    material.setAmbientColor(new Color3f(0f,0f,0f));
 	    material.setDiffuseColor(new Color3f(0.15f,0.15f,0.25f));
 		
 		/* Making shapes from 0 to n */
 		
 		// Make arrays
 		shapeMove = new TransformGroup[n];
 		shapes = new Primitive[n];
 		appearance = new Appearance[n];
 		
 		// Make shapes
 		for (int i = 0; i < n; i++) {
 			makeShape(i);
 			testTransform.addChild(shapeMove[i]);
 		}
 		
 		/*
 		SharedGroup sg = new SharedGroup();
 		// object
 		for (int i = 0; i < n; i++) {
 			Transform3D tr1 = new Transform3D();
 			tr1.setRotation(new AxisAngle4d(0, 1, 0, 2 * Math.PI
 					* ((double) i / n)));
 			TransformGroup tgNew = new TransformGroup(tr1);
 			Link link = new Link();
 			link.setSharedGroup(sg);
 			tgNew.addChild(link);
 			tg.addChild(tgNew);
 
 		}*/
 		
 		/*
 		Shape3D torus1 = new Torus(0.1, 0.7);
 		Appearance ap = new Appearance();
 		ap.setMaterial(new Material());
 		torus1.setAppearance(ap);
 		tg.addChild(torus1);
 
 		Shape3D torus2 = new Torus(0.1, 0.4);
 		ap = new Appearance();
 		ap.setMaterial(new Material());
 		ap.setTransparencyAttributes(new TransparencyAttributes(
 				TransparencyAttributes.BLENDED, 0.0f));
 		torus2.setAppearance(ap);
 		Transform3D tr2 = new Transform3D();
 		tr2.setRotation(new AxisAngle4d(1, 0, 0, Math.PI / 2));
 		tr2.setTranslation(new Vector3d(0.8, 0, 0));
 
 		TransformGroup tg2 = new TransformGroup(tr2);
 		tg2.addChild(torus2);
 		*/
 
 		// SharedGroup
 		/*
 		sg.addChild(tg2);
 		Alpha alpha = new Alpha(0, 8000);
 		RotationInterpolator rotator = new RotationInterpolator(alpha, spin);
 		rotator.setSchedulingBounds(bounds);
 		spin.addChild(rotator);
 		*/
 		
 		return root;
 	}
 	
 	public void makeShape (int i)
 	{
 		//System.out.println("Oppretter shape " + i);
 		appearance[i] = createAppearance();
 		appearance[i].setMaterial(material);
 		appearance[i].setTransparencyAttributes(new TransparencyAttributes(
 				TransparencyAttributes.BLENDED, 0.0f));
 		
 		// Oppretter shape
 		shapes[i] = new Box((float) (0.05f * Math.random()),
 				(float) (0.05f * Math.random()),
 				(float) (0.05f * Math.random()),Primitive.ENABLE_GEOMETRY_PICKING |
 			      Primitive.ENABLE_APPEARANCE_MODIFY |
			      Primitive.GENERATE_NORMALS | Primitive.GENERATE_TEXTURE_COORDS,appearance[i]);
 				  //PickTool.setCapabilities(shapes[i], PickTool.INTERSECT_TEST);
 				
 		// Oppretter shapeMove
 		shapeMove[i] = new TransformGroup();
 
 
 		
 		
 		
 		shapeMove[i].addChild(shapes[i]);
 		
 		// Oppretter RotPosScaleIntepolator
 		Alpha alpha = new Alpha(-1, 8000);
 		Transform3D axisOfRotPos = new Transform3D();
 		float[] knots = { 0.0f, 0.3f, 0.5f, 0.7f, 1.0f };
 		Quat4f[] quats = new Quat4f[5];
 		Point3f[] positions = new Point3f[5];
 		float[] scales = {0.4f, 0.4f, 2.0f, 0.4f, 0.4f};
 		
 		shapeMove[i].setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		
 		AxisAngle4f axis = new AxisAngle4f(1.0f, 0.0f, 0.0f, 0.0f);
 		axisOfRotPos.set(axis);
 		
 		quats[0] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[1] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[2] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[3] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		quats[4] = new Quat4f(0.0f, 0.0f, 0.0f, 0.0f);
 		
 		float avstand_ytre  = 0.5f;
 		float avstand_indre = 0.2f;
 		double theta = Math.random()* 2 * Math.PI;
 		positions[0] = new Point3f(
 				(float) (-avstand_ytre * Math.cos(theta)),
 				(float) (-avstand_ytre * Math.sin(theta)),
 				0.0f);
 		positions[1] = new Point3f(
 				(float) (-avstand_indre * Math.cos(theta)),
 				(float) (-avstand_indre * Math.sin(theta)),
 				0.0f);
 		positions[2] = new Point3f(0.0f, 0.0f, 0.0f);
 		positions[3] = new Point3f(
 				(float) (avstand_indre * Math.cos(theta)),
 				(float) (avstand_indre * Math.sin(theta)),
 				0.0f);
 		positions[4] = new Point3f(
 				(float) (avstand_ytre * Math.cos(theta)),
 				(float) (avstand_ytre * Math.sin(theta)),
 				0.0f);
 		
 		
 		// Create a RotPosPathInterpolator object RotPosScalePathInterpolator
 		RotPosScalePathInterpolator rotPosScalePath = new RotPosScalePathInterpolator(alpha,
 				shapeMove[i], axisOfRotPos, knots, quats, positions, scales);
 		rotPosScalePath.setSchedulingBounds(bounds);
 		shapeMove[i].addChild(rotPosScalePath);
 	}
 	
 	public Appearance createAppearance() {
 		Appearance appear = new Appearance();
 		URL filename = getClass().getClassLoader().getResource(
 				"images/earth.jpg");
 		TextureLoader loader = new TextureLoader(filename, this);
 		ImageComponent2D image = loader.getImage();
 		Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGBA,
 				image.getWidth(), image.getHeight());
 		texture.setImage(0, image);
 		texture.setEnable(true);
 		texture.setMagFilter(Texture.BASE_LEVEL_LINEAR);
 		texture.setMinFilter(Texture.BASE_LEVEL_LINEAR);
 		appear.setTexture(texture);
 		return appear;
 	}
 }
