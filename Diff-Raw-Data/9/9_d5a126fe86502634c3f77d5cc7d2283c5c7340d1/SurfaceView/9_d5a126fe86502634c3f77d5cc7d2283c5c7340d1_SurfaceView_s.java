 package fr.nantes1900.view.display3d;
 
 import java.awt.Color;
 
 import javax.media.j3d.Appearance;
 import javax.media.j3d.ImageComponent2D;
 import javax.media.j3d.Material;
 import javax.media.j3d.RenderingAttributes;
 import javax.media.j3d.Shape3D;
 import javax.media.j3d.Texture;
 import javax.media.j3d.Texture2D;
 import javax.media.j3d.TextureAttributes;
 import javax.vecmath.Color3f;
 
 import com.sun.j3d.utils.image.TextureLoader;
 
 import fr.nantes1900.models.extended.Surface;
 
 /**
  * A class heriting from Shape3D and corresponding to the surface model item. It
  * contains both a meshView and a polygonView. The displayMode set in the
  * U3DController tells if the meshView or the polygonView is to be displayed. It
  * is possible for a surfaceView instance to have its meshView polygonView set
  * to null (for example in the first steps, the polygonView is null)
  * @author Siju Wu, Nicolas Bouillon
  */
 public class SurfaceView extends Shape3D {
     /**
      * The material of a surface selected.
      */
     public static final Material MATERIAL_SELECTED = new Material(new Color3f(
 
    0.2f, 0.2f, 0.0f), new Color3f(0.0f, 0.0f, 0.0f), new Color3f(.2f, 0.2f,
            0.0f), new Color3f(.2f, 0.2f, 0.0f), 64);
 
     /**
      * The material of a surface non-selected.
      */
 
     public static final Material MATERIAL_NON_POLYGON = new Material(
            new Color3f(0.2f, 0.2f, 0f), new Color3f(0.0f, 0.0f, 0.0f),
            new Color3f(0.2f, 0.2f, 0f), new Color3f(0.2f, 0.2f, 0f), 64);
 
     /**
      * 
      */
     public static final Material MATERIAL_POLYGON = new Material(new Color3f(
             0.2f, 0.0f, 0.0f), new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0.2f,
             0.0f, 0.0f), new Color3f(0.2f, 0.0f, 0.0f), 64);
 
     /**
      * 
      */
     public static final Material MATERIAL_NEIGHBOUR = new Material(new Color3f(
             0f, 0.2f, 0f), new Color3f(0.0f, 0.0f, 0.0f), new Color3f(0f, 0.2f,
             0f), new Color3f(0f, 0.2f, 0f), 64);
 
     /**
      * The surface linked to this view.
      */
     private Surface surface;
     /**
      * The mesh to be displayed.
      */
     private MeshView meshView;
     /**
      * The polygon to be displayed.
      */
     private PolygonView polygonView;
     /**
      * The appearance of the surface.
      */
     private Appearance appearance = new Appearance();
 
     /**
      * Constructor of the surfaceView.
      * @param surfaceIn
      *            the surface to build from
      */
     public SurfaceView(final Surface surfaceIn)
     {
         super();
 
         this.surface = surfaceIn;
 
         this.setCapability(Shape3D.ALLOW_APPEARANCE_READ);
         this.setCapability(Shape3D.ALLOW_APPEARANCE_WRITE);
 
         this.appearance.setCapability(Appearance.ALLOW_MATERIAL_READ);
         this.appearance.setCapability(Appearance.ALLOW_MATERIAL_WRITE);
 
         // Read the texture.
         TextureLoader loader = new TextureLoader("texture.jpg", null);
         ImageComponent2D image = loader.getImage();
         Texture2D texture = new Texture2D(Texture.BASE_LEVEL, Texture.RGB,
                 image.getWidth(), image.getHeight());
         texture.setImage(0, image);
 
         // Set the texture.
         this.appearance.setTexture(texture);
 
         TextureAttributes texAtt = new TextureAttributes();
         texAtt.setTextureMode(TextureAttributes.MODULATE);
         this.appearance.setTextureAttributes(texAtt);
 
         // Ignores vertex colors to allow diffuse colors.
         RenderingAttributes rendering = new RenderingAttributes();
         rendering.setIgnoreVertexColors(true);
         this.appearance.setRenderingAttributes(rendering);
 
         if (this.surface.getPolygon() == null)
         {
             this.setMaterial(MATERIAL_NON_POLYGON);
         } else
         {
             this.setMaterial(MATERIAL_POLYGON);
         }
 
         this.setAppearance(this.appearance);
     }
 
     /**
      * Gets the mesh of triangle in the surfaceView.
      * @return this.meshView
      */
 
     public final MeshView getMeshView() {
         return this.meshView;
     }
 
     /**
      * Gets the polygon in the surfaceView.
      * @return this.polygonView
      */
 
     public final PolygonView getPolygonView() {
         return this.polygonView;
     }
 
     /**
      * Getter.
      * @return the surface
      */
     public final Surface getSurface() {
         return this.surface;
     }
 
     /**
      * Sets the material of the surfaceMesh.
      * @param material
      *            The material to be used.
      */
 
     public final void setMaterial(final Material material) {
         this.appearance.setMaterial(material);
 
     }
 
     /**
      * Sets the mesh of triangles in the surfaceView.
      * @param meshViewIn
      *            The mesh of triangles to be displayed.
      */
 
     public final void setMeshView(final MeshView meshViewIn) {
         this.meshView = meshViewIn;
         this.setGeometry(this.meshView);
     }
 
     /**
      * Sets the polygon in the surfaceView.
      * @param polygonViewIn
      *            The polygon to be displayed.
      */
 
     public final void setPolygonView(final PolygonView polygonViewIn) {
         this.polygonView = polygonViewIn;
         this.setGeometry(this.polygonView);
     }
 
     /**
      * Setter.
      * @param surfaceIn
      *            the surface to set
      */
     public final void setSurface(final Surface surfaceIn) {
         this.surface = surfaceIn;
     }
 
 }
