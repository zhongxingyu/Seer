 package se2.e.engine3d.j3d;
 
 import java.io.IOException;
 import java.util.logging.Logger;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.BranchGroup;
 import javax.media.j3d.Canvas3D;
 import javax.media.j3d.ColoringAttributes;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.LineAttributes;
 import javax.media.j3d.Node;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Texture;
 import javax.media.j3d.TextureAttributes;
 import javax.media.j3d.Transform3D;
 import javax.media.j3d.TransformGroup;
 import javax.vecmath.Color3f;
 import javax.vecmath.Point3d;
 import javax.vecmath.Vector3d;
 
 import se2.e.engine3d.GeometryAndAppearanceLoader;
 import se2.e.geometry.Position;
 import se2.e.geometry.SimplePosition;
 import se2.e.utilities.Vector2D;
 import appearance.AppearanceInfo;
 
 import com.sun.j3d.utils.geometry.ColorCube;
 import com.sun.j3d.utils.geometry.Sphere;
 import com.sun.j3d.utils.image.TextureLoader;
 
 /**
  * A factory for creating nodes containing the representation for the geometry.
  * 
  * @author cosmin
  */
 public class GeometryNodeFactory {
 
 	/** The Constant DRAWING_PLANE_Z. */
 	private static final double DRAWING_PLANE_Z = 0d;
 
 	/** The loader. */
 	private GeometryAndAppearanceLoader loader;
 
 	private Canvas3D canvas;
 	
 	private J3DEngine engine;
 	/**
 	 * Instantiates a new geometry node factory.
 	 * 
 	 * @param loader the loader
 	 */
 	public GeometryNodeFactory(GeometryAndAppearanceLoader loader, J3DEngine engine, Canvas3D canvas) {
 		super();
 		this.loader = loader;
 		this.canvas = canvas;
 		this.engine = engine;
 	}
 
 	/**
 	 * Gets the geometry node (could be, for example, a Transform Group) for a specific label (e.g. 'track1').
 	 * 
 	 * @param geometryLabel the geometry label
 	 * @return the node containing the representation for the geometry, or null, if there is no geometry with the given
 	 * label or it's not a static GeometryObject (Track).
 	 */
 	public Node getGeometryNode(String geometryLabel) {
 		// Get the track points
 		Vector2D[] trackPoints = loader.getTrackPoints(geometryLabel);
 		if (trackPoints == null)
 			return null;
 		Logger.getAnonymousLogger().info("Generating " + geometryLabel + " for: " + trackPoints);
 
 		// Prepare the points of the tracks
 		LineArray lineArr = new LineArray((trackPoints.length - 1) * 2, LineArray.COORDINATES);
 		lineArr.setCoordinate(0, new Point3d(trackPoints[0].getX(), trackPoints[0].getY(), DRAWING_PLANE_Z));
 		for (int i = 1; i < trackPoints.length - 1; i++) {
 			// Add each point twice, as it will be both an endpoint for a line and a startpoint for the next one
 			lineArr.setCoordinate(2 * i - 1, new Point3d(trackPoints[i].getX(), trackPoints[i].getY(), DRAWING_PLANE_Z));
 			lineArr.setCoordinate(2 * i, new Point3d(trackPoints[i].getX(), trackPoints[i].getY(), DRAWING_PLANE_Z));
			
 		}
 		lineArr.setCoordinate(2 * (trackPoints.length - 1) - 1, new Point3d(trackPoints[trackPoints.length - 1].getX(),
 				trackPoints[trackPoints.length - 1].getY(), DRAWING_PLANE_Z));
		
 		// Add the line to the track group
 		TransformGroup g = new TransformGroup();
 		
 		// set line width
 		LineAttributes la = new LineAttributes();
 		la.setLineWidth(5.0f);
 		Appearance app = new Appearance();
 		app.setLineAttributes(la);
 		//set line color
 		ColoringAttributes ca = new ColoringAttributes();
 		ca.setColor(new Color3f(1.0f, 0, 0));
 		app.setColoringAttributes(ca);
 		//System.out.println(new java.io.File(".").getAbsolutePath());
 		/**
 		*the texture file and folder has to be in eclipse's home directory (where eclipse.exe is)
 		**/
 //		Texture tex = new TextureLoader("graphics/textures/texture-green.png", engine).getTexture();
 //		app.setTexture(tex);
 //		TextureAttributes texAttr = new TextureAttributes();
 //		texAttr.setTextureMode(TextureAttributes.REPLACE);
 //		app.setTextureAttributes(texAttr);
 		
 		g.addChild(new Shape3D(lineArr, app));
 		return g;
 	}
 	
 	
 	/**
 	 *  @author Marius
 	 * Returns a static branch that contains an interactive input point that, when clicked,
 	 * will callback the 3D engine
 	 * @param appearanceLabel
 	 * @param geomLabel
 	 * @return
 	 */
 	public InteractiveInputBranch getInteractiveInputBranch(String appearanceLabel, String geomLabel) {
 		AppearanceInfo appearanceInfo = this.loader.getAppearanceInfo(appearanceLabel);
 		Position position = this.loader.getSimplePositionObject(geomLabel).getPosition();
 		BranchGroup branchGroup = new BranchGroup();
 		TransformGroup tg = null;
 		
 		String apinfo = appearanceInfo.getLabel();
 		//switch - case with strings only in JRE 7. for compatibility issues, I'm using if - else
 		if (appearanceInfo instanceof Shape3D){
 			if (apinfo.equalsIgnoreCase("Cube"))
 			{
 				ColorCube model = new ColorCube(0.5f);
 				Transform3D trans3d = new Transform3D();
 				trans3d.setTranslation(new Vector3d(position.getX(), position.getY(), DRAWING_PLANE_Z));
 				tg = new TransformGroup(trans3d);
 				tg.addChild(model);
 				tg.setPickable(true);
 				branchGroup.addChild(tg);
 			}
 			else if (apinfo.equalsIgnoreCase("Sphere"))
 			{
 				Appearance app = new Appearance();
 //				Texture tex = new TextureLoader("graphics/textures/earth.png", engine).getTexture();
 //				//FIXME: may need to be changed from / to \ depending on the operating system
 //				app.setTexture(tex);
 //				TextureAttributes texAttr = new TextureAttributes();
 //				texAttr.setTextureMode(TextureAttributes.MODULATE);
 //				app.setTextureAttributes(texAttr);
 //				Sphere model = new Sphere(0.86f, Sphere.GENERATE_TEXTURE_COORDS, app);
 				ColoringAttributes ca = new ColoringAttributes();
 				ca.setColor(new Color3f(0.0f, 1.0f, 0));
 				app.setColoringAttributes(ca);
 				Sphere model = new Sphere(5, app);
 				Transform3D trans3d = new Transform3D();
 				trans3d.setTranslation(new Vector3d(position.getX(), position.getY(), DRAWING_PLANE_Z));
 				tg = new TransformGroup(trans3d);
 				tg.addChild(model);
 				tg.setPickable(true);
 				branchGroup.addChild(tg);
 			}
 		}
 		
 		InteractiveInputBranch branch = new InteractiveInputBranch(geomLabel, tg, branchGroup, engine, canvas);
 		return branch;
 	
 	}
 }
