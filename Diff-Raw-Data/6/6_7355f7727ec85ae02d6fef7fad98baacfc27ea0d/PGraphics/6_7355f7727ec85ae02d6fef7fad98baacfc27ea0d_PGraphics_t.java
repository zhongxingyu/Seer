 /* -*- mode: java; c-basic-offset: 2; indent-tabs-mode: nil -*- */
 
 /*
   Part of the Processing project - http://processing.org
 
   Copyright (c) 2004-08 Ben Fry and Casey Reas
   Copyright (c) 2001-04 Massachusetts Institute of Technology
 
   This library is free software; you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public
   License as published by the Free Software Foundation; either
   version 2.1 of the License, or (at your option) any later version.
 
   This library is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
   Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General
   Public License along with this library; if not, write to the
   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
   Boston, MA  02111-1307  USA
 */
 
 package processing.core;
 
 import java.awt.*;
 import java.awt.image.*;
 
 
 /**
  * Main graphics and rendering context, as well as
  * the base API implementation for processing "core".
  */
 public abstract class PGraphics extends PImage implements PConstants {
 
   static public final int X = 0;  // model coords xyz (formerly MX/MY/MZ)
   static public final int Y = 1;
   static public final int Z = 2;
 
   static public final int R = 3;  // actual rgb, after lighting
   static public final int G = 4;  // fill stored here, transform in place
   static public final int B = 5;  // TODO don't do that anymore
   static public final int A = 6;
 
   static public final int U = 7; // texture
   static public final int V = 8;
 
   static public final int NX = 9; // normal
   static public final int NY = 10;
   static public final int NZ = 11;
 
   static public final int EDGE = 12;
 
   //
 
   /** stroke argb values */
   static public final int SR = 13;
   static public final int SG = 14;
   static public final int SB = 15;
   static public final int SA = 16;
 
   /** stroke weight */
   static public final int SW = 17;
 
   static public final int TX = 18; // transformed xyzw
   static public final int TY = 19;
   static public final int TZ = 20;
 
   static public final int VX = 21; // view space coords
   static public final int VY = 22;
   static public final int VZ = 23;
   static public final int VW = 24;
 
   // Ambient color (usually to be kept the same as diffuse)
   // fill(_) sets both ambient and diffuse.
   static public final int AR = 25;
   static public final int AG = 26;
   static public final int AB = 27;
 
   // Diffuse is shared with fill.
   static public final int DR = 3;  // TODO needs to not be shared, this is a material property
   static public final int DG = 4;
   static public final int DB = 5;
   static public final int DA = 6;
 
   //specular (by default kept white)
  static public final int SPR = 28;
  static public final int SPG = 29;
  static public final int SPB = 30;
   //GL doesn't use a separate specular alpha, but we do (we're better)
   //static public final int SPA = 30;
 
   static public final int SHINE = 31;
 
   //emissive (by default kept black)
   static public final int ER = 32;
   static public final int EG = 33;
   static public final int EB = 34;
 
   // has this vertex been lit yet
   static public final int BEEN_LIT = 35;
 
   static final int VERTEX_FIELD_COUNT = 36;
 
   /// width minus one (useful for many calculations)
   public int width1;
 
   /// height minus one (useful for many calculations)
   public int height1;
 
   /// width * height (useful for many calculations)
   public int pixelCount;
 
   /// true if defaults() has been called a first time
   public boolean settingsInited;
   /// true if defaults need to be reapplied
   //public boolean reapplySettings;
 
   /// true if in-between beginDraw() and endDraw()
 //  protected boolean insideDraw;
 
   /// true if in the midst of resize (no drawing can take place)
 //  boolean insideResize;
 
   // ........................................................
 
   /** path to the file being saved for this renderer (if any) */
   protected String path;
 
   /**
    * true if this is the main drawing surface for a particular sketch.
    * This would be set to false for an offscreen buffer or if it were
    * created any other way than size(). When this is set, the listeners
    * are also added to the sketch.
    */
   protected boolean primarySurface;
 
   // ........................................................
 
   // specifics for java memoryimagesource
   DirectColorModel cm;
   MemoryImageSource mis;
   public Image image;
 
   // ........................................................
 
   // used by recordRaw()
   protected PGraphics raw;
 
   // ........................................................
 
   /**
    * Array of hint[] items. These are hacks to get around various
    * temporary workarounds inside the environment.
    * <p/>
    * Note that this array cannot be static, as a hint() may result in a
    * runtime change specific to a renderer. For instance, calling
    * hint(DISABLE_DEPTH_TEST) has to call glDisable() right away on an
    * instance of PGraphicsOpenGL.
    * <p/>
    * The hints[] array is allocated early on because it might
    * be used inside beginDraw(), allocate(), etc.
    */
   protected boolean[] hints = new boolean[HINT_COUNT];
 
   // ........................................................
 
   // underscored_names are used for private functions or variables
 
   /** The current colorMode */
   public int colorMode; // = RGB;
 
   /** Max value for red (or hue) set by colorMode */
   public float colorModeX; // = 255;
 
   /** Max value for green (or saturation) set by colorMode */
   public float colorModeY; // = 255;
 
   /** Max value for blue (or value) set by colorMode */
   public float colorModeZ; // = 255;
 
   /** Max value for alpha set by colorMode */
   public float colorModeA; // = 255;
 
   /** True if colors are not in the range 0..1 */
   boolean colorScale; // = true;
 
   /** True if colorMode(RGB, 255) */
   boolean colorRgb255; // = true;
 
   // ........................................................
 
   /**
    * true if tint() is enabled (read-only).
    * Using tint/tintColor seems a better option for naming than
    * tintEnabled/tint because the latter seems ugly, even though
    * g.tint as the actual color seems a little more intuitive,
    * it's just that g.tintEnabled is even more unintuitive.
    * Same goes for fill and stroke et al.
    */
   public boolean tint;
 
   /** tint that was last set (read-only) */
   public int tintColor;
 
   protected boolean tintAlpha;
   protected float tintR, tintG, tintB, tintA;
   protected int tintRi, tintGi, tintBi, tintAi;
 
   // ........................................................
 
   /** true if fill() is enabled, (read-only) */
   public boolean fill;
 
   /** fill that was last set (read-only) */
   public int fillColor = 0xffFFFFFF;
 
   protected boolean fillAlpha;
   protected float fillR, fillG, fillB, fillA;
   protected int fillRi, fillGi, fillBi, fillAi;
 
   // ........................................................
 
   /** true if stroke() is enabled, (read-only) */
   public boolean stroke;
 
   /** stroke that was last set (read-only) */
   public int strokeColor = 0xff000000;
 
   protected boolean strokeAlpha;
   protected float strokeR, strokeG, strokeB, strokeA;
   protected int strokeRi, strokeGi, strokeBi, strokeAi;
 
   // ........................................................
 
   /** Last background color that was set, zero if an image */
   public int backgroundColor = 0xffCCCCCC;
 
   protected boolean backgroundAlpha;
   protected float backgroundR, backgroundG, backgroundB, backgroundA;
   protected int backgroundRi, backgroundGi, backgroundBi, backgroundAi;
 
   // ........................................................
 
   // internal color for setting/calculating
   protected float calcR, calcG, calcB, calcA;
   int calcRi, calcGi, calcBi, calcAi;
   int calcColor;
   boolean calcAlpha;
 
   /** The last rgb value converted to HSB */
   int cacheHsbKey;
   /** Result of the last conversion to HSB */
   float[] cacheHsbValue = new float[3]; // inits to zero
 
   // ........................................................
 
   static final float DEFAULT_STROKE_WEIGHT = 1;
   static final int DEFAULT_STROKE_JOIN = MITER;
   static final int DEFAULT_STROKE_CAP = ROUND;
 
   /**
    * Last value set by strokeWeight() (read-only). This has a default
    * setting, rather than fighting with renderers about whether that
    * renderer supports thick lines.
    */
   public float strokeWeight = DEFAULT_STROKE_WEIGHT;
 
   /**
    * Set by strokeJoin() (read-only). This has a default setting
    * so that strokeJoin() need not be called by defaults,
    * because subclasses may not implement it (i.e. PGraphicsGL)
    */
   public int strokeJoin = DEFAULT_STROKE_JOIN;
 
   /**
    * Set by strokeCap() (read-only). This has a default setting
    * so that strokeCap() need not be called by defaults,
    * because subclasses may not implement it (i.e. PGraphicsGL)
    */
   public int strokeCap = DEFAULT_STROKE_CAP;
 
   // ........................................................
 
   /**
    * Model transformation of the form m[row][column],
    * which is a "column vector" (as opposed to "row vector") matrix.
    */
   public float m00, m01, m02, m03;
   public float m10, m11, m12, m13;
   public float m20, m21, m22, m23;
   public float m30, m31, m32, m33;
 
   static final int MATRIX_STACK_DEPTH = 32;
   float matrixStack[][] = new float[MATRIX_STACK_DEPTH][16];
   float matrixInvStack[][] = new float[MATRIX_STACK_DEPTH][16];
   int matrixStackDepth;
 
   // ........................................................
 
   /**
    * Type of shape passed to beginShape(),
    * zero if no shape is currently being drawn.
    */
   protected int shape;
 
   // vertices
   static final int DEFAULT_VERTICES = 512;
   protected float vertices[][] =
     new float[DEFAULT_VERTICES][VERTEX_FIELD_COUNT];
   protected int vertexCount; // total number of vertices
 
 
   // ........................................................
 
   protected boolean bezierInited = false;
   public int bezierDetail = 20;
 
   // used by both curve and bezier, so just init here
   protected PMatrix3D bezierBasisMatrix =
     new PMatrix3D(-1,  3, -3,  1,
                    3, -6,  3,  0,
                   -3,  3,  0,  0,
                    1,  0,  0,  0);
 
   //protected PMatrix3D bezierForwardMatrix;
   protected PMatrix3D bezierDrawMatrix;
 
   // ........................................................
 
   protected boolean curveInited = false;
   protected int curveDetail = 20;
   // catmull-rom basis matrix, perhaps with optional s parameter
   public float curveTightness = 0;
   protected PMatrix3D curveBasisMatrix;
   //protected PMatrix3D curveForwardMatrix;
   protected PMatrix3D curveDrawMatrix;
 
   protected PMatrix3D bezierBasisInverse;
   protected PMatrix3D curveToBezierMatrix;
 
   // ........................................................
 
   // spline vertices
 
   static final int DEFAULT_SPLINE_VERTICES = 128;
   protected float splineVertices[][];
   protected int splineVertexCount;
 
   // ........................................................
 
   // precalculate sin/cos lookup tables [toxi]
   // circle resolution is determined from the actual used radii
   // passed to ellipse() method. this will automatically take any
   // scale transformations into account too
 
   // [toxi 031031]
   // changed table's precision to 0.5 degree steps
   // introduced new vars for more flexible code
   static final protected float sinLUT[];
   static final protected float cosLUT[];
   static final protected float SINCOS_PRECISION = 0.5f;
   static final protected int SINCOS_LENGTH = (int) (360f / SINCOS_PRECISION);
   static {
     sinLUT = new float[SINCOS_LENGTH];
     cosLUT = new float[SINCOS_LENGTH];
     for (int i = 0; i < SINCOS_LENGTH; i++) {
       sinLUT[i] = (float) Math.sin(i * DEG_TO_RAD * SINCOS_PRECISION);
       cosLUT[i] = (float) Math.cos(i * DEG_TO_RAD * SINCOS_PRECISION);
     }
   }
 
   // ........................................................
 
   /** The current rect mode (read-only) */
   public int rectMode;
 
   /** The current ellipse mode (read-only) */
   public int ellipseMode;
 
   /** The current text font (read-only) */
   public PFont textFont;
 
   /** The current font if a Java version of it is installed */
   public Font textFontNative;
 
   /** Metrics for the current native Java font */
   public FontMetrics textFontNativeMetrics;
 
   /** The current text align (read-only) */
   public int textAlign;
 
   /** The current vertical text alignment (read-only) */
   public int textAlignY;
 
   /** The current text mode (read-only) */
   public int textMode;
 
   /** The current text size (read-only) */
   public float textSize;
 
   /** The current text leading (read-only) */
   public float textLeading;
 
   /** Last text position, because text often mixed on lines together */
   public float textX, textY, textZ;
 
   /**
    * Internal buffer used by the text() functions
    * because the String object is slow
    */
   protected char[] textBuffer = new char[8 * 1024];
   protected char[] textWidthBuffer = new char[8 * 1024];
 
   protected int textBreakCount;
   protected int[] textBreakStart;
   protected int[] textBreakStop;
 
 
   //////////////////////////////////////////////////////////////
 
   // VARIABLES FOR 3D (used to prevent the need for a subclass)
 
 
   /** The modelview matrix. */
   public PMatrix3D modelview;
 
   /** Inverse modelview matrix, used for lighting. */
   public PMatrix3D modelviewInv;
 
   protected float[][] modelviewStack;
   protected float[][] modelviewInvStack;
   protected int modelviewStackPointer;
 
   /**
    * The camera matrix, the modelview will be set to this on beginDraw.
    */
   public PMatrix3D camera;
 
   /** Inverse camera matrix */
   public PMatrix3D cameraInv;
 
   // ........................................................
 
   // Material properties
 
   public float ambientR, ambientG, ambientB;
   public float specularR, specularG, specularB; /*, specularA;*/
   public float emissiveR, emissiveG, emissiveB;
   public float shininess;
 
   // ........................................................
 
   /** Camera field of view (in radians, as of rev 86) */
   public float cameraFOV;
 
   /** Position of the camera */
   public float cameraX, cameraY, cameraZ;
 
   public float cameraNear, cameraFar;
   public float cameraAspect;
 
   // projection matrix
   public PMatrix3D projection; // = new PMatrix();
 
   // ........................................................
 
   /// the stencil buffer
   public int stencil[];
 
   /// depth buffer
   public float zbuffer[];
 
   // ........................................................
 
   /** Maximum lights by default is 8, which is arbitrary,
       but is the minimum defined by OpenGL */
   public static final int MAX_LIGHTS = 8;
 
   public int lightCount = 0;
 
   /** Light types */
   public int[] lightType;
 
   /** Light positions */
   public float[][] lightPosition;
 
   /** Light direction (normalized vector) */
   public float[][] lightNormal;
 
   /** Light falloff */
   public float[] lightFalloffConstant;
   public float[] lightFalloffLinear;
   public float[] lightFalloffQuadratic;
 
   /** Light spot angle */
   public float[] lightSpotAngle;
 
   /** Cosine of light spot angle */
   public float[] lightSpotAngleCos;
 
   /** Light spot concentration */
   public float[] lightSpotConcentration;
 
   /** Diffuse colors for lights.
    *  For an ambient light, this will hold the ambient color.
    *  Internally these are stored as numbers between 0 and 1. */
   public float[][] lightDiffuse;
 
   /** Specular colors for lights.
       Internally these are stored as numbers between 0 and 1. */
   public float[][] lightSpecular;
 
   /** Current specular color for lighting */
   public float[] currentLightSpecular;
 
   /** Current light falloff */
   public float currentLightFalloffConstant;
   public float currentLightFalloffLinear;
   public float currentLightFalloffQuadratic;
 
   // ........................................................
 
   /**
    * Sets whether texture coordinates passed to
    * vertex() calls will be based on coordinates that are
    * based on the IMAGE or NORMALIZED.
    */
   public int textureMode;
 
   /**
    * Current horizontal coordinate for texture, will always
    * be between 0 and 1, even if using textureMode(IMAGE).
    */
   public float textureU;
 
   /** Current vertical coordinate for texture, see above. */
   public float textureV;
 
   /** Current image being used as a texture */
   public PImage textureImage;
 
   // ........................................................
 
   /**
    * Normals
    */
   public float normalX, normalY, normalZ;
 
   // ........................................................
 
   /// Number of U steps (aka "theta") around longitudinally spanning 2*pi
   public int sphereDetailU = 0;
   /// Number of V steps (aka "phi") along latitudinally top-to-bottom spanning pi
   public int sphereDetailV = 0;
 
 
 
   //////////////////////////////////////////////////////////////
 
   // INTERNAL
 
 
   /**
    * Constructor for the PGraphics object. Use this to ensure that
    * the defaults get set properly. In a subclass, use this(w, h)
    * as the first line of a subclass' constructor to properly set
    * the internal fields and defaults.
    *
    * @param iwidth  viewport width
    * @param iheight viewport height
    * @param path path to filename or null if not relevant
    * @param parent null unless this is the main drawing surface
    */
   public PGraphics() {
   }
 
 
   public void setParent(PApplet parent) {  // ignore
     this.parent = parent;
   }
 
 
   /**
    * Set (or unset) this as the main drawing surface. Meaning that it can
    * safely be set to opaque (and given a default gray background), or anything
    * else that goes along with that.
    */
   public void setPrimary(boolean primary) {  // ignore
     this.primarySurface = primary;
 
     // base images must be opaque (for performance and general
     // headache reasons.. argh, a semi-transparent opengl surface?)
     // use createGraphics() if you want a transparent surface.
     if (primarySurface) {
       format = RGB;
     }
   }
 
 
   public void setPath(String path) {  // ignore
     this.path = path;
   }
 
 
   /**
    * The final step in setting up a renderer, set its size of this renderer.
    * This was formerly handled by the constructor, but instead it's been broken
    * out so that setParent/setPrimary/setPath can be handled differently.
    *
    * Important that this is ignored by preproc.pl because otherwise it will
    * override setSize() in PApplet/Applet/Component, which will 1) not call
    * super.setSize(), and 2) will cause the renderer to be resized from the
    * event thread (EDT), causing a nasty crash as it collides with the
    * animation thread.
    */
   public void setSize(int iwidth, int iheight) {  // ignore
     width = iwidth;
     height = iheight;
     width1 = width - 1;
     height1 = height - 1;
 
     allocate();
     reapplySettings();
   }
 
 
   // broken out because of subclassing
   abstract protected void allocate();
 
 
 
   //////////////////////////////////////////////////////////////
 
   // FRAME
 
 
   /**
    * Some renderers have requirements re: when they are ready to draw.
    */
   public boolean canDraw() {  // ignore
     return true;
   }
 
 
   /**
    * Prepares the PGraphics for drawing.
    * <p/>
    * When creating your own PGraphics, you should call this before
    * drawing anything.
    */
   abstract public void beginDraw();  // ignore
 
 
   /**
    * This will finalize rendering so that it can be shown on-screen.
    * <p/>
    * When creating your own PGraphics, you should call this when
    * you're finished drawing.
    */
   abstract public void endDraw();  // ignore
 
 
   protected void flush() {
     // no-op, mostly for P3D to write sorted stuff
   }
 
   
   protected void checkSettings() {
     if (!settingsInited) defaultSettings();
   }
 
 
   /**
    * Set engine's default values. This has to be called by PApplet,
    * somewhere inside setup() or draw() because it talks to the
    * graphics buffer, meaning that for subclasses like OpenGL, there
    * needs to be a valid graphics context to mess with otherwise
    * you'll get some good crashing action.
    *
    * This is currently called by checkSettings(), during beginDraw().
    */
   protected void defaultSettings() {  // ignore
 //    System.out.println("PGraphics.defaultSettings() " + width + " " + height);
 
     colorMode(RGB, 255);
     fill(255);
     stroke(0);
     // other stroke attributes are set in the initializers
     // inside the class (see above, strokeWeight = 1 et al)
 
     // init shape stuff
     shape = 0;
 
     // init matrices (must do before lights)
     matrixStackDepth = 0;
 
     rectMode(CORNER);
     ellipseMode(CENTER);
     //arcMode(CENTER);
     //angleMode(RADIANS);
 
     // no current font
     textFont = null;
     textSize = 12;
     textLeading = 14;
     textAlign = LEFT;
     textMode = MODEL;
 
     // if this fella is associated with an applet, then clear its background.
     // if it's been created by someone else through createGraphics,
     // they have to call background() themselves, otherwise everything gets
     // a gray background (when just a transparent surface or an empty pdf
     // is what's desired).
     // this background() call is for the Java 2D and OpenGL renderers.
     if (primarySurface) {
       //System.out.println("main drawing surface bg " + getClass().getName());
       background(backgroundColor);
     }
 
     settingsInited = true;
     // defaultSettings() overlaps reapplySettings(), don't do both
     //reapplySettings = false;
   }
 
 
   /**
    * Re-apply current settings. Some methods, such as textFont(), require that
    * their methods be called (rather than simply setting the textFont variable)
    * because they affect the graphics context, or they require parameters from
    * the context (e.g. getting native fonts for text).
    *
    * This will only be called from an allocate(), which is only called from
    * size(), which is safely called from inside beginDraw(). And it cannot be
    * called before defaultSettings(), so we should be safe.
    */
   protected void reapplySettings() {
 //    System.out.println("attempting reapplySettings()");
     if (!settingsInited) return;  // if this is the initial setup, no need to reapply
 
 //    System.out.println("  doing reapplySettings");
 //    new Exception().printStackTrace(System.out);
 
     colorMode(colorMode, colorModeX, colorModeY, colorModeZ);
     if (fill) {
 //      PApplet.println("  fill " + PApplet.hex(fillColor));
       fill(fillColor);
     } else {
       noFill();
     }
     if (stroke) {
       stroke(strokeColor);
       if (strokeWeight != DEFAULT_STROKE_WEIGHT) {
         strokeWeight(strokeWeight);
       }
       if (strokeCap != DEFAULT_STROKE_CAP) {
         strokeCap(strokeCap);
       }
       if (strokeJoin != DEFAULT_STROKE_JOIN) {
         strokeJoin(strokeJoin);
       }
     } else {
       noStroke();
     }
     if (tint) {
       tint(tintColor);
     } else {
       noTint();
     }
     if (smooth) {
       smooth();
     } else {
       // Don't bother setting this, cuz it'll anger P3D.
       //noSmooth();
     }
     if (textFont != null) {
 //      System.out.println("  textFont in reapply is " + textFont);
       // textFont() resets the leading, so save it in case it's changed
       float saveLeading = textLeading;
       textFont(textFont, textSize);
       textLeading(saveLeading);
     }
     background(backgroundColor);
 
     //reapplySettings = false;
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // HINTS
 
   /**
    * Enable a hint option.
    * <P>
    * For the most part, hints are temporary api quirks,
    * for which a proper api hasn't been properly worked out.
    * for instance SMOOTH_IMAGES existed because smooth()
    * wasn't yet implemented, but it will soon go away.
    * <P>
    * They also exist for obscure features in the graphics
    * engine, like enabling/disabling single pixel lines
    * that ignore the zbuffer, the way they do in alphabot.
    * <P>
    * Current hint options:
    * <UL>
    * <LI><TT>DISABLE_DEPTH_TEST</TT> -
    * turns off the z-buffer in the P3D or OPENGL renderers.
    * </UL>
    */
   public void hint(int which) {
     if (which > 0) {
       hints[which] = true;
     } else {
       hints[-which] = false;
     }
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // SHAPES
 
   /**
    * Start a new shape of type POLYGON
    */
   public void beginShape() {
     beginShape(POLYGON);
   }
 
 
   /**
    * Start a new shape.
    * <P>
    * <B>Differences between beginShape() and line() and point() methods.</B>
    * <P>
    * beginShape() is intended to be more flexible at the expense of being
    * a little more complicated to use. it handles more complicated shapes
    * that can consist of many connected lines (so you get joins) or lines
    * mixed with curves.
    * <P>
    * The line() and point() command are for the far more common cases
    * (particularly for our audience) that simply need to draw a line
    * or a point on the screen.
    * <P>
    * From the code side of things, line() may or may not call beginShape()
    * to do the drawing. In the beta code, they do, but in the alpha code,
    * they did not. they might be implemented one way or the other depending
    * on tradeoffs of runtime efficiency vs. implementation efficiency &mdash
    * meaning the speed that things run at vs. the speed it takes me to write
    * the code and maintain it. for beta, the latter is most important so
    * that's how things are implemented.
    */
   abstract public void beginShape(int kind);
 
 
   public void normal(float nx, float ny, float nz) {
   }
 
 
   /**
    * Set texture mode to either to use coordinates based on the IMAGE
    * (more intuitive for new users) or NORMALIZED (better for advanced chaps)
    */
   public void textureMode(int mode) {
     this.textureMode = mode;
   }
 
 
   /**
    * Set texture image for current shape.
    * Needs to be called between @see beginShape and @see endShape
    *
    * @param image reference to a PImage object
    */
   public void texture(PImage image) {
     textureImage = image;
   }
 
 
   /**
    * Set (U, V) coords for the next vertex in the current shape.
    * This is ugly as its own function, and will (almost?) always be
    * coincident with a call to vertex. As of beta, this was moved to
    * the protected method you see here, and called from an optional
    * param of and overloaded vertex().
    * <p/>
    * The parameters depend on the current textureMode. When using
    * textureMode(IMAGE), the coordinates will be relative to the size
    * of the image texture, when used with textureMode(NORMAL),
    * they'll be in the range 0..1.
    * <p/>
    * Used by both PGraphics2D (for images) and PGraphics3D.
    */
   protected void textureVertex(float u, float v) {
     if (textureImage == null) {
       throw new RuntimeException("need to set an image with texture() " +
                                  "before using u and v coordinates");
     }
     if (textureMode == IMAGE) {
       u /= (float) textureImage.width;
       v /= (float) textureImage.height;
     }
 
     textureU = u;
     textureV = v;
 
     if (textureU < 0) textureU = 0;
     else if (textureU > 1) textureU = 1;
 
     if (textureV < 0) textureV = 0;
     else if (textureV > 1) textureV = 1;
   }
 
 
   // eventually need to push a "default" setup down to this class
   abstract public void vertex(float x, float y);
     /*
     splineVertexCount = 0;
     //float vertex[];
 
     if (vertexCount == vertices.length) {
       float temp[][] = new float[vertexCount<<1][VERTEX_FIELD_COUNT];
       System.arraycopy(vertices, 0, temp, 0, vertexCount);
       vertices = temp;
       //message(CHATTER, "allocating more vertices " + vertices.length);
     }
     // not everyone needs this, but just easier to store rather
     // than adding another moving part to the code...
     vertices[vertexCount][MX] = x;
     vertices[vertexCount][MY] = y;
     vertexCount++;
 
     switch (shape) {
 
     case POINTS:
       point(x, y);
       break;
 
     case LINES:
       if ((vertexCount % 2) == 0) {
         line(vertices[vertexCount-2][MX],
              vertices[vertexCount-2][MY], x, y);
       }
       break;
 
     case LINE_STRIP:
     case LINE_LOOP:
       if (vertexCount == 1) {
         path = new Path();
         path.moveTo(x, y);
       } else {
         path.lineTo(x, y);
       }
       break;
 
     case TRIANGLES:
       if ((vertexCount % 3) == 0) {
         triangle(vertices[vertexCount - 3][MX],
                  vertices[vertexCount - 3][MY],
                  vertices[vertexCount - 2][MX],
                  vertices[vertexCount - 2][MY],
                  x, y);
       }
       break;
 
     case TRIANGLE_STRIP:
       if (vertexCount == 3) {
         triangle(vertices[0][MX], vertices[0][MY],
                  vertices[1][MX], vertices[1][MY],
                  x, y);
       } else if (vertexCount > 3) {
         path = new Path();
         // when vertexCount == 4, draw an un-closed triangle
         // for indices 2, 3, 1
         path.moveTo(vertices[vertexCount - 2][MX],
                     vertices[vertexCount - 2][MY]);
         path.lineTo(vertices[vertexCount - 1][MX],
                     vertices[vertexCount - 1][MY]);
         path.lineTo(vertices[vertexCount - 3][MX],
                     vertices[vertexCount - 3][MY]);
         draw_shape(path);
       }
       break;
 
     case TRIANGLE_FAN:
       if (vertexCount == 3) {
         triangle(vertices[0][MX], vertices[0][MY],
                  vertices[1][MX], vertices[1][MY],
                  x, y);
       } else if (vertexCount > 3) {
         path = new Path();
         // when vertexCount > 3, draw an un-closed triangle
         // for indices 0 (center), previous, current
         path.moveTo(vertices[0][MX],
                     vertices[0][MY]);
         path.lineTo(vertices[vertexCount - 2][MX],
                     vertices[vertexCount - 2][MY]);
         path.lineTo(x, y);
         draw_shape(path);
       }
       break;
 
     case QUADS:
       if ((vertexCount % 4) == 0) {
         quad(vertices[vertexCount - 4][MX],
              vertices[vertexCount - 4][MY],
              vertices[vertexCount - 3][MX],
              vertices[vertexCount - 3][MY],
              vertices[vertexCount - 2][MX],
              vertices[vertexCount - 2][MY],
              x, y);
       }
       break;
 
     case QUAD_STRIP:
       // 0---2---4
       // |   |   |
       // 1---3---5
       if (vertexCount == 4) {
         // note difference in winding order:
         quad(vertices[0][MX], vertices[0][MY],
              vertices[2][MX], vertices[2][MY],
              x, y,
              vertices[1][MX], vertices[1][MY]);
 
       } else if (vertexCount > 4) {
         path = new Path();
         // when vertexCount == 5, draw an un-closed triangle
         // for indices 2, 4, 5, 3
         path.moveTo(vertices[vertexCount - 3][MX],
                     vertices[vertexCount - 3][MY]);
         path.lineTo(vertices[vertexCount - 1][MX],
                     vertices[vertexCount - 1][MY]);
         path.lineTo(x, y);
         path.lineTo(vertices[vertexCount - 2][MX],
                     vertices[vertexCount - 2][MY]);
         draw_shape(path);
       }
       break;
 
     case POLYGON:
       //case CONCAVE_POLYGON:
       //case CONVEX_POLYGON:
       if (vertexCount == 1) {
         path = new Path();
         path.moveTo(x, y);
       } else {
         path.lineTo(x, y);
       }
       break;
     }
     */
 
 
   abstract public void vertex(float x, float y, float z);
 
 
   abstract public void vertex(float x, float y, float u, float v);
 
 
   abstract public void vertex(float x, float y, float z, float u, float v);
 
 
   public void bezierVertex(float x2, float y2,
                            float x3, float y3,
                            float x4, float y4) {
     bezierVertex(x2, y2, Float.MAX_VALUE,
                  x3, y3, Float.MAX_VALUE,
                  x4, y4, Float.MAX_VALUE);
   }
 
 
   /**
    * See notes with the bezier() function.
    */
   public void bezierVertex(float x2, float y2, float z2,
                            float x3, float y3, float z3,
                            float x4, float y4, float z4) {
     if (shape != POLYGON) {
       throw new RuntimeException("beginShape() and vertex() " +
                                  "must be used before bezierVertex()");
     }
     if (splineVertexCount > 0) {
       float vertex[] = splineVertices[splineVertexCount-1];
       splineVertex(vertex[X], vertex[Y], vertex[Z], true);
 
     } else if (vertexCount > 0) {
       // make sure there's at least a call to vertex()
       float vertex[] = vertices[vertexCount-1];
       splineVertex(vertex[X], vertex[Y], vertex[Z], true);
 
     } else {
       throw new RuntimeException("A call to vertex() must be used " +
                                  "before bezierVertex()");
     }
     splineVertex(x2, y2, z2, true);
     splineVertex(x3, y3, z3, true);
     splineVertex(x4, y4, z4, true);
   }
 
 
   /**
    * See notes with the curve() function.
    */
   public void curveVertex(float x, float y) {
     splineVertex(x, y, Float.MAX_VALUE, false);
   }
 
 
   /**
    * See notes with the curve() function.
    */
   public void curveVertex(float x, float y, float z) {
     splineVertex(x, y, z, false);
   }
 
 
   /**
    * Implementation of generic spline vertex, will add coords to
    * the splineVertices[] array and emit calls to draw segments
    * as needed (every fourth point for bezier or every point starting
    * with the fourth for catmull-rom).
    * @param z z-coordinate, set to MAX_VALUE if it's 2D
    * @param bezier true if it's a bezier instead of catmull-rom
    */
   protected void splineVertex(float x, float y, float z, boolean bezier) {
     // to improve processing applet load times, don't allocate
     // space for the vertex data until actual use
     if (splineVertices == null) {
       splineVertices = new float[DEFAULT_SPLINE_VERTICES][VERTEX_FIELD_COUNT];
     }
 
     // if more than 128 points, shift everything back to the beginning
     if (splineVertexCount == DEFAULT_SPLINE_VERTICES) {
       System.arraycopy(splineVertices[DEFAULT_SPLINE_VERTICES-3], 0,
                        splineVertices[0], 0, VERTEX_FIELD_COUNT);
       System.arraycopy(splineVertices[DEFAULT_SPLINE_VERTICES-2], 0,
                        splineVertices[1], 0, VERTEX_FIELD_COUNT);
       System.arraycopy(splineVertices[DEFAULT_SPLINE_VERTICES-1], 0,
                        splineVertices[2], 0, VERTEX_FIELD_COUNT);
       splineVertexCount = 3;
     }
 
     float vertex[] = splineVertices[splineVertexCount];
 
     vertex[X] = x;
     vertex[Y] = y;
 
     if (fill) {
       vertex[R] = fillR;
       vertex[G] = fillG;
       vertex[B] = fillB;
       vertex[A] = fillA;
     }
 
     if (stroke) {
       vertex[SR] = strokeR;
       vertex[SG] = strokeG;
       vertex[SB] = strokeB;
       vertex[SA] = strokeA;
       vertex[SW] = strokeWeight;
     }
 
     if (textureImage != null) {
       vertex[U] = textureU;
       vertex[V] = textureV;
     }
 
     // when the coords are Float.MAX_VALUE, then treat as a 2D curve
     int dimensions = (z == Float.MAX_VALUE) ? 2 : 3;
 
     if (dimensions == 3) {
       vertex[Z] = z;
 
       vertex[NX] = normalX;
       vertex[NY] = normalY;
       vertex[NZ] = normalZ;
     }
 
     splineVertexCount++;
 
     // draw a segment if there are enough points
     if (splineVertexCount > 3) {
       if (bezier) {
         if ((splineVertexCount % 4) == 0) {
           bezierInitCheck();
           splineSegment(splineVertexCount-4,
                         splineVertexCount-4,
                         bezierDrawMatrix, dimensions,
                         bezierDetail);
         }
       } else {  // catmull-rom curve (!bezier)
         curveInitCheck();
         splineSegment(splineVertexCount-4,
                       splineVertexCount-3,
                       curveDrawMatrix, dimensions,
                       curveDetail);
       }
     }
   }
 
 
   /** This feature is in testing, do not use or rely upon its implementation */
   public void breakShape() {
   }
 
 
   public void endShape() {
     endShape(OPEN);
   }
 
 
   abstract public void endShape(int mode);
 
 
 
   //////////////////////////////////////////////////////////////
 
   // COMPOUND PATHS
 
 
   /**
    * Begin a new path. This can be used after beginShape() to draw
    * a compound path (i.e. to draw shape with a hole on the interior)
    * For instance, to draw a shape that has a hole in its interior,
    * the format would be:
    * <PRE>
    * beginShape();
    * beginPath();
    * // multiple calls to vertex() that draw the exterior shape
    * endPath();
    * beginPath();
    * // several calls to vertex() to draw the interior hole
    * endPath();
    * // more beginPath/endPath pairs can be used for additional holes
    * endShape();
    * </PRE>
    * <P/>
    * This will probably be available only with the OpenGL renderer,
    * because it has a built-in tesselator from GLU.
    */
   //public void beginPath() {
   //throw new RuntimeException("beginPath() is not available");
   //}
 
 
   /**
    * End a path. Use this with beginPath() to close out a compound path.
    * <P/>
    * This will probably be available only with the OpenGL renderer,
    * because it has a built-in tesselator from GLU.
    */
   //public void endPath() {
   //throw new RuntimeException("endPath() is not available");
   //}
 
 
 
   //////////////////////////////////////////////////////////////
 
   // SIMPLE SHAPES WITH ANALOGUES IN beginShape()
 
 
   public void point(float x, float y) {
     beginShape(POINTS);
     vertex(x, y);
     endShape();
   }
 
 
   public void point(float x, float y, float z) {
     beginShape(POINTS);
     vertex(x, y, z);
     endShape();
   }
 
 
   public void line(float x1, float y1, float x2, float y2) {
     beginShape(LINES);
     vertex(x1, y1);
     vertex(x2, y2);
     endShape();
   }
 
 
   public void line(float x1, float y1, float z1,
                    float x2, float y2, float z2) {
     beginShape(LINES);
     vertex(x1, y1, z1);
     vertex(x2, y2, z2);
     endShape();
   }
 
 
   public void triangle(float x1, float y1, float x2, float y2,
                        float x3, float y3) {
     beginShape(TRIANGLES);
     vertex(x1, y1);
     vertex(x2, y2);
     vertex(x3, y3);
     endShape();
   }
 
 
   public void quad(float x1, float y1, float x2, float y2,
                    float x3, float y3, float x4, float y4) {
     beginShape(QUADS);
     vertex(x1, y1);
     vertex(x2, y2);
     vertex(x3, y3);
     vertex(x4, y4);
     endShape();
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // RECT
 
 
   public void rectMode(int mode) {
     rectMode = mode;
   }
 
 
   public void rect(float x1, float y1, float x2, float y2) {
     float hradius, vradius;
     switch (rectMode) {
     case CORNERS:
       break;
     case CORNER:
       x2 += x1; y2 += y1;
       break;
     case RADIUS:
       hradius = x2;
       vradius = y2;
       x2 = x1 + hradius;
       y2 = y1 + vradius;
       x1 -= hradius;
       y1 -= vradius;
       break;
     case CENTER:
       hradius = x2 / 2.0f;
       vradius = y2 / 2.0f;
       x2 = x1 + hradius;
       y2 = y1 + vradius;
       x1 -= hradius;
       y1 -= vradius;
     }
 
     if (x1 > x2) {
       float temp = x1; x1 = x2; x2 = temp;
     }
 
     if (y1 > y2) {
       float temp = y1; y1 = y2; y2 = temp;
     }
 
     rectImpl(x1, y1, x2, y2);
   }
 
 
   protected void rectImpl(float x1, float y1, float x2, float y2) {
     quad(x1, y1,  x2, y1,  x2, y2,  x1, y2);
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // ELLIPSE AND ARC
 
 
   public void ellipseMode(int mode) {
     ellipseMode = mode;
   }
 
 
   public void ellipse(float a, float b, float c, float d) {
     float x = a;
     float y = b;
     float w = c;
     float h = d;
 
     if (ellipseMode == CORNERS) {
       w = c - a;
       h = d - b;
 
     } else if (ellipseMode == RADIUS) {
       x = a - c;
       y = b - d;
       w = c * 2;
       h = d * 2;
 
     } else if (ellipseMode == CENTER) {
       x = a - c/2f;
       y = b - d/2f;
     }
 
     if (w < 0) {  // undo negative width
       x += w;
       w = -w;
     }
 
     if (h < 0) {  // undo negative height
       y += h;
       h = -h;
     }
 
     ellipseImpl(x, y, w, h);
   }
 
 
   protected void ellipseImpl(float x1, float y1, float w, float h) {
     float hradius = w / 2f;
     float vradius = h / 2f;
 
     float centerX = x1 + hradius;
     float centerY = y1 + vradius;
 
     // adapt accuracy to radii used w/ a minimum of 4 segments [toxi]
     // now uses current scale factors to determine "real" transformed radius
 
     //int cAccuracy = (int)(4+Math.sqrt(hradius*abs(m00)+vradius*abs(m11))*2);
     //int cAccuracy = (int)(4+Math.sqrt(hradius+vradius)*2);
 
     // notched this up to *3 instead of *2 because things were
     // looking a little rough, i.e. the calculate->arctangent example [fry]
 
     // also removed the m00 and m11 because those were causing weirdness
     // need an actual measure of magnitude in there [fry]
 
     int accuracy = (int)(4+Math.sqrt(hradius+vradius)*3);
     //System.out.println("accuracy is " + accuracy);
 
     // [toxi031031] adapted to use new lookup tables
     float inc = (float)SINCOS_LENGTH / accuracy;
 
     float val = 0;
     /*
     beginShape(POLYGON);
     for (int i = 0; i < cAccuracy; i++) {
       vertex(centerX + cosLUT[(int) val] * hradius,
              centerY + sinLUT[(int) val] * vradius);
       val += inc;
     }
     endShape();
     */
 
     if (fill) {
       boolean savedStroke = stroke;
       stroke = false;
 
       beginShape(TRIANGLE_FAN);
       normal(0, 0, 1);
       vertex(centerX, centerY);
       for (int i = 0; i < accuracy; i++) {
         vertex(centerX + cosLUT[(int) val] * hradius,
                centerY + sinLUT[(int) val] * vradius);
         val += inc;
       }
       // back to the beginning
       vertex(centerX + cosLUT[0] * hradius,
              centerY + sinLUT[0] * vradius);
       endShape();
 
       stroke = savedStroke;
     }
 
     if (stroke) {
       boolean savedFill = fill;
       fill = false;
 
       val = 0;
       beginShape(); //LINE_LOOP);
       for (int i = 0; i < accuracy; i++) {
         vertex(centerX + cosLUT[(int) val] * hradius,
                centerY + sinLUT[(int) val] * vradius);
         val += inc;
       }
       endShape(CLOSE);
 
       fill = savedFill;
     }
   }
 
 
   /**
    * Identical parameters and placement to ellipse,
    * but draws only an arc of that ellipse.
    * <p/>
    * start and stop are always radians because angleMode() was goofy.
    * ellipseMode() sets the placement.
    * <p/>
    * also tries to be smart about start < stop.
    */
   public void arc(float a, float b, float c, float d,
                   float start, float stop) {
     float x = a;
     float y = b;
     float w = c;
     float h = d;
 
     if (ellipseMode == CORNERS) {
       w = c - a;
       h = d - b;
 
     } else if (ellipseMode == RADIUS) {
       x = a - c;
       y = b - d;
       w = c * 2;
       h = d * 2;
 
     } else if (ellipseMode == CENTER) {
       x = a - c/2f;
       y = b - d/2f;
     }
 
     //if (angleMode == DEGREES) {
     //start = start * DEG_TO_RAD;
     //stop = stop * DEG_TO_RAD;
     //}
     // before running a while loop like this,
     // make sure it will exit at some point.
     if (Float.isInfinite(start) || Float.isInfinite(stop)) return;
     while (stop < start) stop += TWO_PI;
 
     arcImpl(x, y, w, h, start, stop);
   }
 
 
   /**
    * Start and stop are in radians, converted by the parent function.
    * Note that the radians can be greater (or less) than TWO_PI.
    * This is so that an arc can be drawn that crosses zero mark,
    * and the user will still collect $200.
    */
   protected void arcImpl(float x1, float y1, float w, float h,
                          float start, float stop) {
     float hr = w / 2f;
     float vr = h / 2f;
 
     float centerX = x1 + hr;
     float centerY = y1 + vr;
 
     if (fill) {
       // shut off stroke for a minute
       boolean savedStroke = stroke;
       stroke = false;
 
       int startLUT = (int) (0.5f + (start / TWO_PI) * SINCOS_LENGTH);
       int stopLUT = (int) (0.5f + (stop / TWO_PI) * SINCOS_LENGTH);
 
       beginShape(TRIANGLE_FAN);
       vertex(centerX, centerY);
       int increment = 1; // what's a good algorithm? stopLUT - startLUT;
       for (int i = startLUT; i < stopLUT; i += increment) {
         int ii = i % SINCOS_LENGTH;
         // modulo won't make the value positive
         if (ii < 0) ii += SINCOS_LENGTH;
         vertex(centerX + cosLUT[ii] * hr,
                centerY + sinLUT[ii] * vr);
       }
       // draw last point explicitly for accuracy
       vertex(centerX + cosLUT[stopLUT % SINCOS_LENGTH] * hr,
              centerY + sinLUT[stopLUT % SINCOS_LENGTH] * vr);
       endShape();
 
       stroke = savedStroke;
     }
 
     if (stroke) {
       // Almost identical to above, but this uses a LINE_STRIP
       // and doesn't include the first (center) vertex.
 
       boolean savedFill = fill;
       fill = false;
 
       int startLUT = (int) (0.5f + (start / TWO_PI) * SINCOS_LENGTH);
       int stopLUT = (int) (0.5f + (stop / TWO_PI) * SINCOS_LENGTH);
 
       beginShape(); //LINE_STRIP);
       int increment = 1; // what's a good algorithm? stopLUT - startLUT;
       for (int i = startLUT; i < stopLUT; i += increment) {
         int ii = i % SINCOS_LENGTH;
         if (ii < 0) ii += SINCOS_LENGTH;
         vertex(centerX + cosLUT[ii] * hr,
                centerY + sinLUT[ii] * vr);
       }
       // draw last point explicitly for accuracy
       vertex(centerX + cosLUT[stopLUT % SINCOS_LENGTH] * hr,
              centerY + sinLUT[stopLUT % SINCOS_LENGTH] * vr);
       endShape();
 
       fill = savedFill;
     }
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // BOX & SPHERE
 
 
   public void box(float size) {
     depthError("box");
   }
 
   public void box(float w, float h, float d) {
     depthError("box");
   }
 
   public void sphereDetail(int res) {
     depthError("sphereDetail");
   }
 
   // [davbol 2008-08-01]
   public void sphereDetail(int ures, int vres) {
     depthError("sphereDetail");
   }
 
   public void sphere(float r) {
     depthError("sphere");
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // BEZIER
 
 
   /**
    * Evalutes quadratic bezier at point t for points a, b, c, d.
    * t varies between 0 and 1, and a and d are the on curve points,
    * b and c are the control points. this can be done once with the
    * x coordinates and a second time with the y coordinates to get
    * the location of a bezier curve at t.
    * <P>
    * For instance, to convert the following example:<PRE>
    * stroke(255, 102, 0);
    * line(85, 20, 10, 10);
    * line(90, 90, 15, 80);
    * stroke(0, 0, 0);
    * bezier(85, 20, 10, 10, 90, 90, 15, 80);
    *
    * // draw it in gray, using 10 steps instead of the default 20
    * // this is a slower way to do it, but useful if you need
    * // to do things with the coordinates at each step
    * stroke(128);
    * beginShape(LINE_STRIP);
    * for (int i = 0; i <= 10; i++) {
    *   float t = i / 10.0f;
    *   float x = bezierPoint(85, 10, 90, 15, t);
    *   float y = bezierPoint(20, 10, 90, 80, t);
    *   vertex(x, y);
    * }
    * endShape();</PRE>
    */
   public float bezierPoint(float a, float b, float c, float d, float t) {
     float t1 = 1.0f - t;
     return a*t1*t1*t1 + 3*b*t*t1*t1 + 3*c*t*t*t1 + d*t*t*t;
   }
 
 
   /**
    * Provide the tangent at the given point on the bezier curve.
    * Fix from davbol for 0136.
    */
   public float bezierTangent(float a, float b, float c, float d, float t) {
     return (3*t*t * (-a+3*b-3*c+d) +
             6*t * (a-2*b+c) +
             3 * (-a+b));
   }
 
 
   protected void bezierInitCheck() {
     if (!bezierInited) {
       bezierDetail(bezierDetail);
     }
   }
 
 
   public void bezierDetail(int detail) {
     bezierDetail = detail;
 
     if (bezierDrawMatrix == null) {
       bezierDrawMatrix = new PMatrix3D();
     }
 
     // setup matrix for forward differencing to speed up drawing
     splineForward(detail, bezierDrawMatrix);
 
     // multiply the basis and forward diff matrices together
     // saves much time since this needn't be done for each curve
     //mult_spline_matrix(bezierForwardMatrix, bezier_basis, bezierDrawMatrix, 4);
     //bezierDrawMatrix.set(bezierForwardMatrix);
     bezierDrawMatrix.apply(bezierBasisMatrix);
     
     bezierInited = true;
   }
 
 
   /**
    * Draw a cubic bezier curve. The first and last points are
    * the on-curve points. The middle two are the 'control' points,
    * or 'handles' in an application like Illustrator.
    * <P>
    * Identical to typing:
    * <PRE>beginShape();
    * vertex(x1, y1);
    * bezierVertex(x2, y2, x3, y3, x4, y4);
    * endShape();
    * </PRE>
    * In Postscript-speak, this would be:
    * <PRE>moveto(x1, y1);
    * curveto(x2, y2, x3, y3, x4, y4);</PRE>
    * If you were to try and continue that curve like so:
    * <PRE>curveto(x5, y5, x6, y6, x7, y7);</PRE>
    * This would be done in processing by adding these statements:
    * <PRE>bezierVertex(x5, y5, x6, y6, x7, y7)
    * </PRE>
    * To draw a quadratic (instead of cubic) curve,
    * use the control point twice by doubling it:
    * <PRE>bezier(x1, y1, cx, cy, cx, cy, x2, y2);</PRE>
    */
   public void bezier(float x1, float y1,
                      float x2, float y2,
                      float x3, float y3,
                      float x4, float y4) {
     beginShape(); //LINE_STRIP);
     vertex(x1, y1);
     bezierVertex(x2, y2, x3, y3, x4, y4);
     endShape();
   }
 
 
   public void bezier(float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float x4, float y4, float z4) {
     beginShape(); //LINE_STRIP);
     vertex(x1, y1, z1);
     bezierVertex(x2, y2, z2,
                  x3, y3, z3,
                  x4, y4, z4);
     endShape();
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // CATMULL-ROM CURVE
 
 
   /**
    * Get a location along a catmull-rom curve segment.
    *
    * @param t Value between zero and one for how far along the segment
    */
   public float curvePoint(float a, float b, float c, float d, float t) {
     curveInitCheck();
 
     float tt = t * t;
     float ttt = t * tt;
     PMatrix3D cb = curveBasisMatrix;
 
     // not optimized (and probably need not be)
     return (a * (ttt*cb.m00 + tt*cb.m10 + t*cb.m20 + cb.m30) +
             b * (ttt*cb.m01 + tt*cb.m11 + t*cb.m21 + cb.m31) +
             c * (ttt*cb.m02 + tt*cb.m12 + t*cb.m22 + cb.m32) +
             d * (ttt*cb.m03 + tt*cb.m13 + t*cb.m23 + cb.m33));
   }
 
 
   /**
    * Calculate the tangent at a t value (0..1) on a Catmull-Rom curve.
    * Code thanks to Dave Bollinger (Bug #715)
    */
   public float curveTangent(float a, float b, float c, float d, float t) {
     curveInitCheck();
 
     float tt3 = t * t * 3;
     float t2 = t * 2;
     PMatrix3D cb = curveBasisMatrix;
 
     // not optimized (and probably need not be)
     return (a * (tt3*cb.m00 + t2*cb.m10 + cb.m20) +
             b * (tt3*cb.m01 + t2*cb.m11 + cb.m21) +
             c * (tt3*cb.m02 + t2*cb.m12 + cb.m22) +
             d * (tt3*cb.m03 + t2*cb.m13 + cb.m23) );
   }
 
 
   public void curveDetail(int detail) {
     curveInit(detail, curveTightness);
   }
 
 
   public void curveTightness(float tightness) {
     curveInit(curveDetail, tightness);
   }
 
 
   protected void curveInitCheck() {
     if (!curveInited) {
       curveInit(curveDetail, curveTightness);
     }
   }
 
 
   /**
    * Set the number of segments to use when drawing a Catmull-Rom
    * curve, and setting the s parameter, which defines how tightly
    * the curve fits to each vertex. Catmull-Rom curves are actually
    * a subset of this curve type where the s is set to zero.
    * <P>
    * (This function is not optimized, since it's not expected to
    * be called all that often. there are many juicy and obvious
    * opimizations in here, but it's probably better to keep the
    * code more readable)
    */
   protected void curveInit(int segments, float s) {
     curveDetail = segments;
     curveTightness = s;
 
     // allocate only if/when used to save startup time
     if (curveDrawMatrix == null) {
       curveBasisMatrix = new PMatrix3D();
       curveDrawMatrix = new PMatrix3D();
       curveInited = true;
     }
 
     curveBasisMatrix.set((s-1)/2f, (s+3)/2f,  (-3-s)/2f, (1-s)/2f,
                          (1-s),    (-5-s)/2f, (s+2),     (s-1)/2f,
                          (s-1)/2f, 0,         (1-s)/2f,  0,
                          0,        1,         0,         0);
 
     //setup_spline_forward(segments, curveForwardMatrix);
     splineForward(segments, curveDrawMatrix);
 
     if (bezierBasisInverse == null) {
       bezierBasisInverse = bezierBasisMatrix.get();
       bezierBasisInverse.invert();
       curveToBezierMatrix = new PMatrix3D();
     }
 
     // TODO only needed for PGraphicsJava2D? if so, move it there
     // actually, it's generally useful for other renderers, so keep it
     // or hide the implementation elsewhere.
     curveToBezierMatrix.set(curveBasisMatrix);
     curveToBezierMatrix.preApply(bezierBasisInverse);
 
     // multiply the basis and forward diff matrices together
     // saves much time since this needn't be done for each curve
     curveDrawMatrix.apply(curveBasisMatrix);
   }
 
 
   /**
    * Draws a segment of Catmull-Rom curve.
    * <P>
    * As of 0070, this function no longer doubles the first and
    * last points. The curves are a bit more boring, but it's more
    * mathematically correct, and properly mirrored in curvePoint().
    * <P>
    * Identical to typing out:<PRE>
    * beginShape();
    * curveVertex(x1, y1);
    * curveVertex(x2, y2);
    * curveVertex(x3, y3);
    * curveVertex(x4, y4);
    * endShape();
    * </PRE>
    */
   public void curve(float x1, float y1,
                     float x2, float y2,
                     float x3, float y3,
                     float x4, float y4) {
     beginShape();
     curveVertex(x1, y1);
     curveVertex(x2, y2);
     curveVertex(x3, y3);
     curveVertex(x4, y4);
     endShape();
   }
 
 
   public void curve(float x1, float y1, float z1,
                     float x2, float y2, float z2,
                     float x3, float y3, float z3,
                     float x4, float y4, float z4) {
     beginShape();
     curveVertex(x1, y1, z1);
     curveVertex(x2, y2, z2);
     curveVertex(x3, y3, z3);
     curveVertex(x4, y4, z4);
     endShape();
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // SPLINE UTILITY FUNCTIONS (used by both Bezier and Catmull-Rom)
 
 
   /**
    * Setup forward-differencing matrix to be used for speedy
    * curve rendering. It's based on using a specific number
    * of curve segments and just doing incremental adds for each
    * vertex of the segment, rather than running the mathematically
    * expensive cubic equation.
    * @param segments number of curve segments to use when drawing
    * @param fwd target object for the new matrix 
    */
   protected void splineForward(int segments, PMatrix3D fwd) {
     float f  = 1.0f / segments;
     float ff = f * f;
     float fff = ff * f;
 
     fwd.set(0,     0,    0, 1,
             fff,   ff,   f, 0,
             6*fff, 2*ff, 0, 0,
             6*fff, 0,    0, 0);
   }
 
 
   /**
    * Draw a segment of spline (bezier or catmull-rom curve)
    * using the matrix m, which is the basis matrix already
    * multiplied with the forward differencing matrix.
    * <P>
    * the x0, y0, z0 points are the point that's being used as
    * the start, and also as the accumulator. for bezier curves,
    * the x1, y1, z1 are the first point drawn, and added to.
    * for catmull-rom curves, the first control point (x2, y2, z2)
    * is the first drawn point, and is accumulated to.
    */
   protected void splineSegment(int offset, int start, PMatrix3D basis,
                                int dimensions, int segments) {
     float x1 = splineVertices[offset+0][X];
     float x2 = splineVertices[offset+1][X];
     float x3 = splineVertices[offset+2][X];
     float x4 = splineVertices[offset+3][X];
     float x0 = splineVertices[start][X];
 
     float y1 = splineVertices[offset+0][Y];
     float y2 = splineVertices[offset+1][Y];
     float y3 = splineVertices[offset+2][Y];
     float y4 = splineVertices[offset+3][Y];
     float y0 = splineVertices[start][Y];
 
     float xplot1 = basis.m10*x1 + basis.m11*x2 + basis.m12*x3 + basis.m13*x4;
     float xplot2 = basis.m20*x1 + basis.m21*x2 + basis.m22*x3 + basis.m23*x4;
     float xplot3 = basis.m30*x1 + basis.m31*x2 + basis.m32*x3 + basis.m33*x4;
 
     float yplot1 = basis.m10*y1 + basis.m11*y2 + basis.m12*y3 + basis.m13*y4;
     float yplot2 = basis.m20*y1 + basis.m21*y2 + basis.m22*y3 + basis.m23*y4;
     float yplot3 = basis.m30*y1 + basis.m31*y2 + basis.m32*y3 + basis.m33*y4;
 
     // vertex() will reset splineVertexCount, so save it
     int cvertexSaved = splineVertexCount;
 
     if (dimensions == 3) {
       float z1 = splineVertices[offset+0][Z];
       float z2 = splineVertices[offset+1][Z];
       float z3 = splineVertices[offset+2][Z];
       float z4 = splineVertices[offset+3][Z];
       float z0 = splineVertices[start][Z];
 
       float zplot1 = m10*z1 + m11*z2 + m12*z3 + m13*z4;
       float zplot2 = m20*z1 + m21*z2 + m22*z3 + m23*z4;
       float zplot3 = m30*z1 + m31*z2 + m32*z3 + m33*z4;
 
       vertex(x0, y0, z0);
       for (int j = 0; j < segments; j++) {
         x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
         y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
         z0 += zplot1; zplot1 += zplot2; zplot2 += zplot3;
         vertex(x0, y0, z0);
       }
     } else {
       vertex(x0, y0);
       for (int j = 0; j < segments; j++) {
         x0 += xplot1; xplot1 += xplot2; xplot2 += xplot3;
         y0 += yplot1; yplot1 += yplot2; yplot2 += yplot3;
         vertex(x0, y0);
       }
     }
     splineVertexCount = cvertexSaved;
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // IMAGE
 
 
   public void image(PImage image, float x, float y) {
     // Starting in release 0144, image errors are simply ignored.
     // loadImageAsync() sets width and height to -1 when loading fails.
     if (image.width == -1 || image.height == -1) return;
 
     imageImpl(image,
               x, y, x+image.width, y+image.height,
               0, 0, image.width, image.height);
   }
 
 
   public void image(PImage image,
                     float x, float y, float c, float d) {
     image(image, x, y, c, d, 0, 0, image.width, image.height);
   }
 
 
   /**
    * u, v coordinates are always based on image space location,
    * regardless of the current textureMode().
    */
   public void image(PImage image,
                     float a, float b, float c, float d,
                     int u1, int v1, int u2, int v2) {
     // Starting in release 0144, image errors are simply ignored.
     // loadImageAsync() sets width and height to -1 when loading fails.
     if (image.width == -1 || image.height == -1) return;
 
     if (imageMode == CORNER) {
       if (c < 0) {  // reset a negative width
         a += c; c = -c;
       }
       if (d < 0) {  // reset a negative height
         b += d; d = -d;
       }
 
       imageImpl(image,
                 a, b, a + c, b + d,
                 u1, v1, u2, v2);
 
     } else if (imageMode == CORNERS) {
       if (c < a) {  // reverse because x2 < x1
         float temp = a; a = c; c = temp;
       }
       if (d < b) {  // reverse because y2 < y1
         float temp = b; b = d; d = temp;
       }
 
       imageImpl(image,
                 a, b, c, d,
                 u1, v1, u2, v2);
 
     } else if (imageMode == CENTER) {
       // c and d are width/height
       if (c < 0) c = -c;
       if (d < 0) d = -d;
       float x1 = a - c/2;
       float y1 = b - d/2;
 
       imageImpl(image,
                 x1, y1, x1 + c, y1 + d,
                 u1, v1, u2, v2);
     }
   }
 
 
   /**
    * Expects x1, y1, x2, y2 coordinates where (x2 >= x1) and (y2 >= y1).
    * If tint() has been called, the image will be colored.
    * <p/>
    * The default implementation draws an image as a textured quad.
    * The (u, v) coordinates are in image space (they're ints, after all..)
    */
   protected void imageImpl(PImage image,
                            float x1, float y1, float x2, float y2,
                            int u1, int v1, int u2, int v2) {
     boolean savedStroke = stroke;
     boolean savedFill = fill;
     int savedTextureMode = textureMode;
 
     stroke = false;
     fill = true;
     textureMode = IMAGE;
 
     float savedFillR = fillR;
     float savedFillG = fillG;
     float savedFillB = fillB;
     float savedFillA = fillA;
 
     if (tint) {
       fillR = tintR;
       fillG = tintG;
       fillB = tintB;
       fillA = tintA;
 
     } else {
       fillR = 1;
       fillG = 1;
       fillB = 1;
       fillA = 1;
     }
 
     //System.out.println(fill + " " + fillR + " " + fillG + " " + fillB);
 
     beginShape(QUADS);
     texture(image);
     vertex(x1, y1, u1, v1);
     vertex(x1, y2, u1, v2);
     vertex(x2, y2, u2, v2);
     vertex(x2, y1, u2, v1);
     endShape();
 
     stroke = savedStroke;
     fill = savedFill;
     textureMode = savedTextureMode;
 
     fillR = savedFillR;
     fillG = savedFillG;
     fillB = savedFillB;
     fillA = savedFillA;
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // TEXT/FONTS
 
 
   /**
    * Sets the alignment of the text to one of LEFT, CENTER, or RIGHT.
    * This will also reset the vertical text alignment to BASELINE.
    */
   public void textAlign(int align) {
     textAlign(align, BASELINE);
   }
 
 
   /**
    * Sets the horizontal and vertical alignment of the text. The horizontal
    * alignment can be one of LEFT, CENTER, or RIGHT. The vertical alignment
    * can be TOP, BOTTOM, CENTER, or the BASELINE (the default).
    */
   public void textAlign(int alignX, int alignY) {
     textAlign = alignX;
     textAlignY = alignY;
   }
 
 
   /**
    * Returns the ascent of the current font at the current size.
    * This is a method, rather than a variable inside the PGraphics object
    * because it requires calculation.
    */
   public float textAscent() {
     if (textFont == null) {
       throw new RuntimeException("use textFont() before textAscent()");
     }
 
     return textFont.ascent() *
       ((textMode == SCREEN) ? textFont.size : textSize);
   }
 
 
   /**
    * Returns the descent of the current font at the current size.
    * This is a method, rather than a variable inside the PGraphics object
    * because it requires calculation.
    */
   public float textDescent() {
     if (textFont != null) {
       return textFont.descent() *
         ((textMode == SCREEN) ? textFont.size : textSize);
 
     } else {
       throw new RuntimeException("use textFont() before textDescent()");
     }
   }
 
 
   /**
    * Sets the current font. The font's size will be the "natural"
    * size of this font (the size that was set when using "Create Font").
    * The leading will also be reset.
    */
   public void textFont(PFont which) {
     if (which != null) {
       textFont = which;
       if (hints[ENABLE_NATIVE_FONTS]) {
         if (which.font == null) {
           which.findFont();
         }
       }
       textFontNative = which.font;
 
       //textFontNativeMetrics = null;
       // changed for rev 0104 for textMode(SHAPE) in opengl
       if (textFontNative != null) {
         // TODO need a better way to handle this. could use reflection to get
         // rid of the warning, but that'd be a little silly. supporting this is
         // an artifact of supporting java 1.1, otherwise we'd use getLineMetrics,
         // as recommended by the @deprecated flag.
         textFontNativeMetrics =
           Toolkit.getDefaultToolkit().getFontMetrics(textFontNative);
         // The following is what needs to be done, however we need to be able
         // to get the actual graphics context where the drawing is happening.
         // For instance, parent.getGraphics() doesn't work for OpenGL since
         // an OpenGL drawing surface is an embedded component.
 //        if (parent != null) {
 //          textFontNativeMetrics = parent.getGraphics().getFontMetrics(textFontNative);
 //        }
 
         // float w = font.getStringBounds(text, g2.getFontRenderContext()).getWidth();
       }
       textSize(which.size);
 
     } else {
       throw new RuntimeException("a null PFont was passed to textFont()");
     }
   }
 
 
   /**
    * Useful function to set the font and size at the same time.
    */
   public void textFont(PFont which, float size) {
     textFont(which);
     textSize(size);
   }
 
 
   /**
    * Set the text leading to a specific value. If using a custom
    * value for the text leading, you'll have to call textLeading()
    * again after any calls to textSize().
    */
   public void textLeading(float leading) {
     textLeading = leading;
   }
 
 
   /**
    * Sets the text rendering/placement to be either SCREEN (direct
    * to the screen, exact coordinates, only use the font's original size)
    * or MODEL (the default, where text is manipulated by translate() and
    * can have a textSize). The text size cannot be set when using
    * textMode(SCREEN), because it uses the pixels directly from the font.
    */
   public void textMode(int mode) {
     // CENTER and MODEL overlap (they're both 3)
     if ((mode == LEFT) || (mode == RIGHT)) {
       throw new RuntimeException("textMode() is now textAlign() " +
                                  "in Processing beta");
     }
     if ((mode != SCREEN) && (mode != MODEL)) {
       throw new RuntimeException("Only textMode(SCREEN) and textMode(MODEL) " +
                                  "are available with this renderer.");
     }
 
     //if (textFont != null) {
     textMode = mode;
 
     // reset the font to its natural size
     // (helps with width calculations and all that)
     //if (textMode == SCREEN) {
     //textSize(textFont.size);
     //}
 
     //} else {
     //throw new RuntimeException("use textFont() before textMode()");
     //}
   }
 
 
   /**
    * Sets the text size, also resets the value for the leading.
    */
   public void textSize(float size) {
     if (textFont != null) {
       if ((textMode == SCREEN) && (size != textFont.size)) {
         throw new RuntimeException("textSize() cannot be used with " +
                                    "textMode(SCREEN)");
       }
       textSize = size;
       textLeading = (textAscent() + textDescent()) * 1.275f;
 
     } else {
       throw new RuntimeException("Use textFont() before textSize()");
     }
   }
 
 
   // ........................................................
 
 
   public float textWidth(char c) {
     textWidthBuffer[0] = c;
     return textWidthImpl(textWidthBuffer, 0, 1);
   }
 
 
   /**
    * Return the width of a line of text. If the text has multiple
    * lines, this returns the length of the longest line.
    */
   public float textWidth(String str) {
     if (textFont == null) {
       throw new RuntimeException("use textFont() before textWidth()");
     }
 
     int length = str.length();
     if (length > textWidthBuffer.length) {
       textWidthBuffer = new char[length + 10];
     }
     str.getChars(0, length, textWidthBuffer, 0);
 
     float wide = 0;
     int index = 0;
     int start = 0;
 
     while (index < length) {
       if (textWidthBuffer[index] == '\n') {
         wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
         start = index+1;
       }
       index++;
     }
     if (start < length) {
       wide = Math.max(wide, textWidthImpl(textWidthBuffer, start, index));
     }
     return wide;
   }
 
 
   /**
    * Implementation of returning the text width of
    * the chars [start, stop) in the buffer.
    * Unlike the previous version that was inside PFont, this will
    * return the size not of a 1 pixel font, but the actual current size.
    */
   protected float textWidthImpl(char buffer[], int start, int stop) {
     float wide = 0;
     for (int i = start; i < stop; i++) {
       // could add kerning here, but it just ain't implemented
       wide += textFont.width(buffer[i]) * textSize;
     }
     return wide;
   }
 
 
   // ........................................................
 
 
   /**
    * Write text where we just left off.
    */
   public void text(char c) {
     text(c, textX, textY, textZ);
   }
 
 
   /**
    * Draw a single character on screen.
    * Extremely slow when used with textMode(SCREEN) and Java 2D,
    * because loadPixels has to be called first and updatePixels last.
    */
   public void text(char c, float x, float y) {
     if (textFont == null) {
       throw new RuntimeException("use textFont() before text()");
     }
 
     if (textMode == SCREEN) loadPixels();
 
     textBuffer[0] = c;
     textLineImpl(textBuffer, 0, 1, x, y);
 
     if (textMode == SCREEN) updatePixels();
   }
 
 
   /**
    * Draw a single character on screen (with a z coordinate)
    */
   public void text(char c, float x, float y, float z) {
     if ((z != 0) && (textMode == SCREEN)) {
       String msg = "textMode(SCREEN) cannot have a z coordinate";
       throw new RuntimeException(msg);
     }
 
     if (z != 0) translate(0, 0, z);  // slowness, badness
 
     text(c, x, y);
     textZ = z;
 
     if (z != 0) translate(0, 0, -z);
   }
 
 
   /**
    * Write text where we just left off.
    */
   public void text(String str) {
     text(str, textX, textY, textZ);
   }
 
 
   /**
    * Draw a chunk of text.
    * Newlines that are \n (Unix newline or linefeed char, ascii 10)
    * are honored, but \r (carriage return, Windows and Mac OS) are
    * ignored.
    */
   public void text(String str, float x, float y) {
     if (textFont == null) {
       throw new RuntimeException("use textFont() before text()");
     }
 
     if (textMode == SCREEN) loadPixels();
 
     int length = str.length();
     if (length > textBuffer.length) {
       textBuffer = new char[length + 10];
     }
     str.getChars(0, length, textBuffer, 0);
 
     // If multiple lines, sum the height of the additional lines
     float high = 0; //-textAscent();
     for (int i = 0; i < length; i++) {
       if (textBuffer[i] == '\n') {
         high += textLeading;
       }
     }
     if (textAlignY == CENTER) {
       // for a single line, this adds half the textAscent to y
       // for multiple lines, subtract half the additional height
       //y += (textAscent() - textDescent() - high)/2;
       y += (textAscent() - high)/2;
     } else if (textAlignY == TOP) {
       // for a single line, need to add textAscent to y
       // for multiple lines, no different
       y += textAscent();
     } else if (textAlignY == BOTTOM) {
       // for a single line, this is just offset by the descent
       // for multiple lines, subtract leading for each line
       y -= textDescent() + high;
     //} else if (textAlignY == BASELINE) {
       // do nothing
     }
 
     int start = 0;
     int index = 0;
     while (index < length) {
       if (textBuffer[index] == '\n') {
         textLineImpl(textBuffer, start, index, x, y);
         start = index + 1;
         y += textLeading;
       }
       index++;
     }
     if (start < length) {
       textLineImpl(textBuffer, start, index, x, y);
     }
     if (textMode == SCREEN) updatePixels();
   }
 
 
   /**
    * Same as above but with a z coordinate.
    */
   public void text(String str, float x, float y, float z) {
     if ((z != 0) && (textMode == SCREEN)) {
       String msg = "textMode(SCREEN) cannot have a z coordinate";
       throw new RuntimeException(msg);
     }
 
     if (z != 0) translate(0, 0, z);  // slow!
 
     text(str, x, y);
     textZ = z;
 
     if (z != 0) translate(0, 0, -z);
   }
 
 
   /**
    * Handles placement of a text line, then calls textLinePlaced
    * to actually render at the specific point.
    */
   protected void textLineImpl(char buffer[], int start, int stop,
                               float x, float y) {
     if (textAlign == CENTER) {
       x -= textWidthImpl(buffer, start, stop) / 2f;
 
     } else if (textAlign == RIGHT) {
       x -= textWidthImpl(buffer, start, stop);
     }
 
     textLinePlacedImpl(buffer, start, stop, x, y);
   }
 
 
   protected void textLinePlacedImpl(char buffer[], int start, int stop,
                                     float x, float y) {
     for (int index = start; index < stop; index++) {
       textCharImpl(buffer[index], x, y);
 
       // this doesn't account for kerning
       x += textWidth(buffer[index]);
     }
     textX = x;
     textY = y;
     textZ = 0;  // this will get set by the caller if non-zero
   }
 
 
   /**
    * Draw text in a box that is constrained to a particular size.
    * The current rectMode() determines what the coordinates mean
    * (whether x1/y1/x2/y2 or x/y/w/h).
    * <P/>
    * Note that the x,y coords of the start of the box
    * will align with the *ascent* of the text, not the baseline,
    * as is the case for the other text() functions.
    * <P/>
    * Newlines that are \n (Unix newline or linefeed char, ascii 10)
    * are honored, and \r (carriage return, Windows and Mac OS) are
    * ignored.
    */
   public void text(String str, float x1, float y1, float x2, float y2) {
     if (textFont == null) {
       throw new RuntimeException("use textFont() before text()");
     }
 
     if (textMode == SCREEN) loadPixels();
 
     float hradius, vradius;
     switch (rectMode) {
     case CORNER:
       x2 += x1; y2 += y1;
       break;
     case RADIUS:
       hradius = x2;
       vradius = y2;
       x2 = x1 + hradius;
       y2 = y1 + vradius;
       x1 -= hradius;
       y1 -= vradius;
       break;
     case CENTER:
       hradius = x2 / 2.0f;
       vradius = y2 / 2.0f;
       x2 = x1 + hradius;
       y2 = y1 + vradius;
       x1 -= hradius;
       y1 -= vradius;
     }
     if (x2 < x1) {
       float temp = x1; x1 = x2; x2 = temp;
     }
     if (y2 < y1) {
       float temp = y1; y1 = y2; y2 = temp;
     }
 
 //    float currentY = y1;
     float boxWidth = x2 - x1;
 
 //    // ala illustrator, the text itself must fit inside the box
 //    currentY += textAscent(); //ascent() * textSize;
 //    // if the box is already too small, tell em to f off
 //    if (currentY > y2) return;
 
     float spaceWidth = textWidth(' ');
 
     if (textBreakStart == null) {
       textBreakStart = new int[20];
       textBreakStop = new int[20];
     }
     textBreakCount = 0;
 
     int length = str.length();
     if (length + 1 > textBuffer.length) {
       textBuffer = new char[length + 1];
     }
     str.getChars(0, length, textBuffer, 0);
     // add a fake newline to simplify calculations
     textBuffer[length++] = '\n';
 
     int sentenceStart = 0;
     for (int i = 0; i < length; i++) {
       if (textBuffer[i] == '\n') {
 //        currentY = textSentence(textBuffer, sentenceStart, i,
 //                                lineX, boxWidth, currentY, y2, spaceWidth);
         boolean legit =
           textSentence(textBuffer, sentenceStart, i, boxWidth, spaceWidth);
         if (!legit) break;
 //      if (Float.isNaN(currentY)) break;  // word too big (or error)
 //      if (currentY > y2) break;  // past the box
         sentenceStart = i + 1;
       }
     }
 
     // lineX is the position where the text starts, which is adjusted
     // to left/center/right based on the current textAlign
     float lineX = x1; //boxX1;
     if (textAlign == CENTER) {
       lineX = lineX + boxWidth/2f;
     } else if (textAlign == RIGHT) {
       lineX = x2; //boxX2;
     }
 
     float boxHeight = y2 - y1;
     int lineFitCount = 1 + PApplet.floor((boxHeight - textAscent()) / textLeading);
     int lineCount = Math.min(textBreakCount, lineFitCount);
 
     if (textAlignY == CENTER) {
       float lineHigh = textAscent() + textLeading * (lineCount - 1);
       float y = y1 + textAscent() + (boxHeight - lineHigh) / 2;
       for (int i = 0; i < lineCount; i++) {
         textLineImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
         y += textLeading;
       }
 
     } else if (textAlignY == BOTTOM) {
       float y = y2 - textDescent() - textLeading * (lineCount - 1);
       for (int i = 0; i < lineCount; i++) {
         textLineImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
         y += textLeading;
       }
 
     } else {  // TOP or BASELINE just go to the default
       float y = y1 + textAscent();
       for (int i = 0; i < lineCount; i++) {
         textLineImpl(textBuffer, textBreakStart[i], textBreakStop[i], lineX, y);
         y += textLeading;
       }
     }
 
     if (textMode == SCREEN) updatePixels();
   }
 
 
   /**
    * Emit a sentence of text, defined as a chunk of text without any newlines.
    * @param stop non-inclusive, the end of the text in question
    */
   protected boolean textSentence(char[] buffer, int start, int stop,
                                  float boxWidth, float spaceWidth) {
     float runningX = 0;
 
     // Keep track of this separately from index, since we'll need to back up
     // from index when breaking words that are too long to fit.
     int lineStart = start;
     int wordStart = start;
     int index = start;
     while (index <= stop) {
       // boundary of a word or end of this sentence
       if ((buffer[index] == ' ') || (index == stop)) {
         float wordWidth = textWidthImpl(buffer, wordStart, index);
 
         if (runningX + wordWidth > boxWidth) {
           if (runningX != 0) {
             // Next word is too big, output the current line and advance
             //textLineImpl(buffer, lineStart, index, x, y);
             textSentenceBreak(lineStart, index);
             // only increment index if a word wasn't broken inside the
             // do/while loop below..
 
             // Eat whitespace because multiple spaces don't count for s*
             // when they're at the end of a line.
             //index = wordStop;  // back that azz up
             while ((index < stop) && (buffer[index] == ' ')) {
               index++;
             }
           } else {  // (runningX == 0)
             // If this is the first word on the line, and its width is greater
             // than the width of the text box, then break the word where at the
             // max width, and send the rest of the word to the next line.
             do {
               index--;
               if (index == wordStart) {
                 // Not a single char will fit on this line. screw 'em.
                 //System.out.println("screw you");
                 return false; //Float.NaN;
               }
               wordWidth = textWidthImpl(buffer, wordStart, index);
             } while (wordWidth > boxWidth);
 
             //textLineImpl(buffer, lineStart, index, x, y);
             textSentenceBreak(lineStart, index);
           }
           lineStart = index;
           wordStart = index;
           runningX = 0;
 //          y += textLeading;
 //          if (y > y2) return y;  // box is now full
 
         } else if (index == stop) {
           // last line in the block, time to unload
           //textLineImpl(buffer, lineStart, index, x, y);
           textSentenceBreak(lineStart, index);
 //          y += textLeading;
           index++;
 
         } else {  // this word will fit, just add it to the line
           runningX += wordWidth + spaceWidth;
           wordStart = index + 1;  // move on to the next word
           index++;
         }
       } else {  // not a space or the last character
         index++;  // this is just another letter
       }
     }
 //    return y;
     return true;
   }
 
 
   protected void textSentenceBreak(int start, int stop) {
     if (textBreakCount == textBreakStart.length) {
       textBreakStart = PApplet.expand(textBreakStart);
       textBreakStop = PApplet.expand(textBreakStop);
     }
     textBreakStart[textBreakCount] = start;
     textBreakStop[textBreakCount] = stop;
     textBreakCount++;
   }
 
 
   public void text(String s, float x1, float y1, float x2, float y2, float z) {
     if ((z != 0) && (textMode == SCREEN)) {
       String msg = "textMode(SCREEN) cannot have a z coordinate";
       throw new RuntimeException(msg);
     }
 
     if (z != 0) translate(0, 0, z);  // slowness, badness
 
     text(s, x1, y1, x2, y2);
     textZ = z;
 
     if (z != 0) translate(0, 0, -z);  // TEMPORARY HACK! SLOW!
   }
 
 
   public void text(int num, float x, float y) {
     text(String.valueOf(num), x, y);
   }
 
 
   public void text(int num, float x, float y, float z) {
     text(String.valueOf(num), x, y, z);
   }
 
 
   /**
    * This does a basic number formatting, to avoid the
    * generally ugly appearance of printing floats.
    * Users who want more control should use their own nf() cmmand,
    * or if they want the long, ugly version of float,
    * use String.valueOf() to convert the float to a String first.
    */
   public void text(float num, float x, float y) {
     text(PApplet.nfs(num, 0, 3), x, y);
   }
 
 
   public void text(float num, float x, float y, float z) {
     text(PApplet.nfs(num, 0, 3), x, y, z);
   }
 
 
   // . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . . .
 
 
   // what was this for?
   //font.getStringBounds(text, g2.getFontRenderContext()).getWidth();
 
 
   protected void textCharImpl(char ch, float x, float y) { //, float z) {
     int index = textFont.index(ch);
     if (index == -1) return;
 
     PImage glyph = textFont.images[index];
 
     if (textMode == MODEL) {
       float high    = (float) textFont.height[index]     / textFont.fheight;
       float bwidth  = (float) textFont.width[index]      / textFont.fwidth;
       float lextent = (float) textFont.leftExtent[index] / textFont.fwidth;
       float textent = (float) textFont.topExtent[index]  / textFont.fheight;
 
       float x1 = x + lextent * textSize;
       float y1 = y - textent * textSize;
       float x2 = x1 + bwidth * textSize;
       float y2 = y1 + high * textSize;
 
       textCharModelImpl(glyph,
                         x1, y1, x2, y2,
                         //x1, y1, z, x2, y2, z,
                         textFont.width[index], textFont.height[index]);
 
     } else if (textMode == SCREEN) {
       int xx = (int) x + textFont.leftExtent[index];;
       int yy = (int) y - textFont.topExtent[index];
 
       int w0 = textFont.width[index];
       int h0 = textFont.height[index];
 
       textCharScreenImpl(glyph, xx, yy, w0, h0);
     }
   }
 
 
   protected void textCharModelImpl(PImage glyph,
                                    float x1, float y1, //float z1,
                                    float x2, float y2, //float z2,
                                    int u2, int v2) {
     boolean savedTint = tint;
     int savedTintColor = tintColor;
     float savedTintR = tintR;
     float savedTintG = tintG;
     float savedTintB = tintB;
     float savedTintA = tintA;
     boolean savedTintAlpha = tintAlpha;
 
     tint = true;
     tintColor = fillColor;
     tintR = fillR;
     tintG = fillG;
     tintB = fillB;
     tintA = fillA;
     tintAlpha = fillAlpha;
 
     imageImpl(glyph, x1, y1, x2, y2, 0, 0, u2, v2);
 
     tint = savedTint;
     tintColor = savedTintColor;
     tintR = savedTintR;
     tintG = savedTintG;
     tintB = savedTintB;
     tintA = savedTintA;
     tintAlpha = savedTintAlpha;
   }
 
 
   // should take image, int x1, int y1, and x2, y2
 
   protected void textCharScreenImpl(PImage glyph,
                                     int xx, int yy, //int x2, int y2,
                                     int w0, int h0) {
     /*
     System.out.println("textimplscreen");
     rectMode(CORNER);
     stroke(255);
     rect(xx, yy, w0, h0);
     */
 
     int x0 = 0;
     int y0 = 0;
 
     if ((xx >= width) || (yy >= height) ||
         (xx + w0 < 0) || (yy + h0 < 0)) return;
 
     if (xx < 0) {
       x0 -= xx;
       w0 += xx;
       xx = 0;
     }
     if (yy < 0) {
       y0 -= yy;
       h0 += yy;
       yy = 0;
     }
     if (xx + w0 > width) {
       w0 -= ((xx + w0) - width);
     }
     if (yy + h0 > height) {
       h0 -= ((yy + h0) - height);
     }
 
     int fr = fillRi;
     int fg = fillGi;
     int fb = fillBi;
     int fa = fillAi;
 
     int pixels1[] = glyph.pixels; //images[glyph].pixels;
 
     // TODO this can be optimized a bit
     for (int row = y0; row < y0 + h0; row++) {
       for (int col = x0; col < x0 + w0; col++) {
         int a1 = (fa * pixels1[row * textFont.twidth + col]) >> 8;
         int a2 = a1 ^ 0xff;
         //int p1 = pixels1[row * glyph.width + col];
         int p2 = pixels[(yy + row-y0)*width + (xx+col-x0)];
 
         pixels[(yy + row-y0)*width + xx+col-x0] =
           (0xff000000 |
            (((a1 * fr + a2 * ((p2 >> 16) & 0xff)) & 0xff00) << 8) |
            (( a1 * fg + a2 * ((p2 >>  8) & 0xff)) & 0xff00) |
            (( a1 * fb + a2 * ( p2        & 0xff)) >> 8));
       }
     }
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // MATRIX TRANSFORMATIONS
 
 
   /**
    * Translate in X and Y.
    */
   abstract public void translate(float tx, float ty);
 
 
   /**
    * Translate in X, Y, and Z.
    */
   abstract public void translate(float tx, float ty, float tz);
 
 
   /**
    * Two dimensional rotation.
    *
    * Same as rotateZ (this is identical to a 3D rotation along the z-axis)
    * but included for clarity. It'd be weird for people drawing 2D graphics
    * to be using rotateZ. And they might kick our a-- for the confusion.
    *
    * <A HREF="http://www.xkcd.com/c184.html">Additional background</A>.
    */
   abstract public void rotate(float angle);
 
 
   /**
    * Rotate around the X axis.
    */
   abstract public void rotateX(float angle);
 
 
   /**
    * Rotate around the Y axis.
    */
   abstract public void rotateY(float angle);
 
 
   /**
    * Rotate around the Z axis.
    *
    * The functions rotate() and rotateZ() are identical, it's just that it make
    * sense to have rotate() and then rotateX() and rotateY() when using 3D;
    * nor does it make sense to use a function called rotateZ() if you're only
    * doing things in 2D. so we just decided to have them both be the same.
    */
   abstract public void rotateZ(float angle);
 
 
   /**
    * Rotate about a vector in space. Same as the glRotatef() function.
    */
   abstract public void rotate(float angle, float vx, float vy, float vz);
 
 
   /**
    * Scale in all dimensions.
    */
   abstract public void scale(float s);
 
 
   /**
    * Scale in X and Y. Equivalent to scale(sx, sy, 1).
    *
    * Not recommended for use in 3D, because the z-dimension is just
    * scaled by 1, since there's no way to know what else to scale it by.
    */
   abstract public void scale(float sx, float sy);
 
 
   /**
    * Scale in X, Y, and Z.
    */
   abstract public void scale(float x, float y, float z);
 
 
 
   //////////////////////////////////////////////////////////////
 
   // TRANSFORMATION MATRIX
 
 
   static final String ERROR_PUSHMATRIX_OVERFLOW =
     "Too many calls to pushMatrix(), the maximum is " + MATRIX_STACK_DEPTH + ".";
   static final String ERROR_PUSHMATRIX_UNDERFLOW =
     "Too many calls to popMatrix(), and not enough to pushMatrix().";
 
 
   /**
    * Push a copy of the current transformation matrix onto the stack.
    */
   abstract public void pushMatrix();
 
 
   /**
    * Replace the current transformation matrix with the top of the stack.
    */
   abstract public void popMatrix();
 
 
   /**
    * Set the current transformation matrix to identity.
    */
   abstract public void resetMatrix();
 
 
   /**
    * Apply a 3x2 affine transformation matrix.
    */
   abstract public void applyMatrix(float n00, float n01, float n02,
                                    float n10, float n11, float n12);
 
 
   /**
    * Apply a 4x4 transformation matrix.
    */
   abstract public void applyMatrix(float n00, float n01, float n02, float n03,
                                    float n10, float n11, float n12, float n13,
                                    float n20, float n21, float n22, float n23,
                                    float n30, float n31, float n32, float n33);
 
 
   /**
    * Loads the current matrix into m00, m01 etc (or modelview and
    * projection when using 3D) so that the values can be read.
    * <P/>
    * Note that there is no "updateMatrix" because that gets too
    * complicated (unnecessary) when considering the 3D matrices.
    */
   abstract public void loadMatrix();
 
 
   /**
    * Print the current model (or "transformation") matrix.
    */
   abstract public void printMatrix();
 
 
 
   //////////////////////////////////////////////////////////////
 
   // CAMERA (none are supported in 2D)
 
 
   public void beginCamera() {
     depthError("beginCamera");
   }
 
   public void endCamera() {
     depthError("endCamera");
   }
 
   public void camera() {
     depthError("camera");
   }
 
   public void camera(float eyeX, float eyeY, float eyeZ,
                      float centerX, float centerY, float centerZ,
                      float upX, float upY, float upZ) {
     depthError("camera");
   }
 
   public void printCamera() {
     depthError("printCamera");
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // PROJECTION (none are supported in 2D)
 
 
   public void ortho() {
     depthError("ortho");
   }
 
   public void ortho(float left, float right,
                     float bottom, float top,
                     float near, float far) {
     depthError("ortho");
   }
 
   public void perspective() {
     depthError("perspective");
   }
 
   public void perspective(float fovy, float aspect, float zNear, float zFar) {
     depthError("perspective");
   }
 
   public void frustum(float left, float right, 
                       float bottom, float top, 
                       float near, float far) {
     depthError("frustum");
   }
 
   public void printProjection() {
     depthError("printCamera");
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // SCREEN TRANSFORMS
 
 
   /**
    * Given an x and y coordinate, returns the x position of where
    * that point would be placed on screen, once affected by translate(),
    * scale(), or any other transformations.
    */
   public float screenX(float x, float y) {
     return m00*x + m01*y + m02;
   }
 
 
   /**
    * Given an x and y coordinate, returns the y position of where
    * that point would be placed on screen, once affected by translate(),
    * scale(), or any other transformations.
    */
   public float screenY(float x, float y) {
     return m10*x + m11*y + m12;
   }
 
 
   /**
    * Maps a three dimensional point to its placement on-screen.
    * <P>
    * Given an (x, y, z) coordinate, returns the x position of where
    * that point would be placed on screen, once affected by translate(),
    * scale(), or any other transformations.
    */
   public float screenX(float x, float y, float z) {
     depthErrorXYZ("screenX");
     return 0;
   }
 
 
   /**
    * Maps a three dimensional point to its placement on-screen.
    * <P>
    * Given an (x, y, z) coordinate, returns the y position of where
    * that point would be placed on screen, once affected by translate(),
    * scale(), or any other transformations.
    */
   public float screenY(float x, float y, float z) {
     depthErrorXYZ("screenY");
     return 0;
   }
 
 
   /**
    * Maps a three dimensional point to its placement on-screen.
    * <P>
    * Given an (x, y, z) coordinate, returns its z value.
    * This value can be used to determine if an (x, y, z) coordinate
    * is in front or in back of another (x, y, z) coordinate.
    * The units are based on how the zbuffer is set up, and don't
    * relate to anything "real". They're only useful for in
    * comparison to another value obtained from screenZ(),
    * or directly out of the zbuffer[].
    */
   public float screenZ(float x, float y, float z) {
     depthErrorXYZ("screenZ");
     return 0;
   }
 
 
   /**
    * Returns the model space x value for an x, y, z coordinate.
    * <P>
    * This will give you a coordinate after it has been transformed
    * by translate(), rotate(), and camera(), but not yet transformed
    * by the projection matrix. For instance, his can be useful for
    * figuring out how points in 3D space relate to the edge
    * coordinates of a shape.
    */
   public float modelX(float x, float y, float z) {
     depthError("modelX");
     return 0;
   }
 
 
   /**
    * Returns the model space y value for an x, y, z coordinate.
    */
   public float modelY(float x, float y, float z) {
     depthError("modelY");
     return 0;
   }
 
 
   /**
    * Returns the model space z value for an x, y, z coordinate.
    */
   public float modelZ(float x, float y, float z) {
     depthError("modelZ");
     return 0;
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // COLOR
 
 
   public void colorMode(int mode) {
     colorMode(mode, colorModeX, colorModeY, colorModeZ, colorModeA);
   }
 
 
   public void colorMode(int mode, float max) {
     colorMode(mode, max, max, max, max);
   }
 
 
   /**
    * Set the colorMode and the maximum values for (r, g, b)
    * or (h, s, b).
    * <P>
    * Note that this doesn't set the maximum for the alpha value,
    * which might be confusing if for instance you switched to
    * <PRE>colorMode(HSB, 360, 100, 100);</PRE>
    * because the alpha values were still between 0 and 255.
    */
   public void colorMode(int mode,
                         float maxX, float maxY, float maxZ) {
     colorMode(mode, maxX, maxY, maxZ, colorModeA);
   }
 
 
   public void colorMode(int mode,
                         float maxX, float maxY, float maxZ, float maxA) {
     colorMode = mode;
 
     colorModeX = maxX;  // still needs to be set for hsb
     colorModeY = maxY;
     colorModeZ = maxZ;
     colorModeA = maxA;
 
     // if color max values are all 1, then no need to scale
     colorScale = ((maxA != 1) || (maxX != maxY) ||
                   (maxY != maxZ) || (maxZ != maxA));
 
     // if color is rgb/0..255 this will make it easier for the
     // red() green() etc functions
     colorRgb255 = (colorMode == RGB) &&
       (colorModeA == 255) && (colorModeX == 255) &&
       (colorModeY == 255) && (colorModeZ == 255);
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   protected void colorCalc(float gray) {
     colorCalc(gray, colorModeA);
   }
 
 
   protected void colorCalc(float gray, float alpha) {
     if (gray > colorModeX) gray = colorModeX;
     if (alpha > colorModeA) alpha = colorModeA;
 
     if (gray < 0) gray = 0;
     if (alpha < 0) alpha = 0;
 
     calcR = colorScale ? (gray / colorModeX) : gray;
     calcG = calcR;
     calcB = calcR;
     calcA = colorScale ? (alpha / colorModeA) : alpha;
 
     calcRi = (int)(calcR*255); calcGi = (int)(calcG*255);
     calcBi = (int)(calcB*255); calcAi = (int)(calcA*255);
     calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
     calcAlpha = (calcAi != 255);
   }
 
 
   protected void colorCalc(float x, float y, float z) {
     colorCalc(x, y, z, colorModeA);
   }
 
 
   protected void colorCalc(float x, float y, float z, float a) {
     if (x > colorModeX) x = colorModeX;
     if (y > colorModeY) y = colorModeY;
     if (z > colorModeZ) z = colorModeZ;
     if (a > colorModeA) a = colorModeA;
 
     if (x < 0) x = 0;
     if (y < 0) y = 0;
     if (z < 0) z = 0;
     if (a < 0) a = 0;
 
     switch (colorMode) {
     case RGB:
       if (colorScale) {
         calcR = x / colorModeX;
         calcG = y / colorModeY;
         calcB = z / colorModeZ;
         calcA = a / colorModeA;
       } else {
         calcR = x; calcG = y; calcB = z; calcA = a;
       }
       break;
 
     case HSB:
       x /= colorModeX; // h
       y /= colorModeY; // s
       z /= colorModeZ; // b
 
       calcA = colorScale ? (a/colorModeA) : a;
 
       if (y == 0) {  // saturation == 0
         calcR = calcG = calcB = z;
 
       } else {
         float which = (x - (int)x) * 6.0f;
         float f = which - (int)which;
         float p = z * (1.0f - y);
         float q = z * (1.0f - y * f);
         float t = z * (1.0f - (y * (1.0f - f)));
 
         switch ((int)which) {
         case 0: calcR = z; calcG = t; calcB = p; break;
         case 1: calcR = q; calcG = z; calcB = p; break;
         case 2: calcR = p; calcG = z; calcB = t; break;
         case 3: calcR = p; calcG = q; calcB = z; break;
         case 4: calcR = t; calcG = p; calcB = z; break;
         case 5: calcR = z; calcG = p; calcB = q; break;
         }
       }
       break;
     }
     calcRi = (int)(255*calcR); calcGi = (int)(255*calcG);
     calcBi = (int)(255*calcB); calcAi = (int)(255*calcA);
     calcColor = (calcAi << 24) | (calcRi << 16) | (calcGi << 8) | calcBi;
     calcAlpha = (calcAi != 255);
   }
 
 
   /**
    * Unpacks AARRGGBB color for direct use with colorCalc.
    * <P>
    * Handled here with its own function since this is indepenent
    * of the color mode.
    * <P>
    * Strangely the old version of this code ignored the alpha
    * value. not sure if that was a bug or what.
    * <P>
    * Note, no need for a bounds check since it's a 32 bit number.
    */
   protected void colorCalcARGB(int argb, float alpha) {
     if (alpha == colorModeA) {
       calcAi = (argb >> 24) & 0xff;
       calcColor = argb;
     } else {
       calcAi = (int) (((argb >> 24) & 0xff) * (alpha / colorModeA));
       calcColor = (calcAi << 24) | (argb & 0xFFFFFF);
     }
     calcRi = (argb >> 16) & 0xff;
     calcGi = (argb >> 8) & 0xff;
     calcBi = argb & 0xff;
     calcA = (float)calcAi / 255.0f;
     calcR = (float)calcRi / 255.0f;
     calcG = (float)calcGi / 255.0f;
     calcB = (float)calcBi / 255.0f;
     calcAlpha = (calcAi != 255);
 
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void strokeWeight(float weight) {
     strokeWeight = weight;
   }
 
 
   public void strokeJoin(int join) {
     strokeJoin = join;
   }
 
 
   public void strokeCap(int cap) {
     strokeCap = cap;
   }
 
 
   public void noStroke() {
     stroke = false;
   }
 
 
   /**
    * Set the tint to either a grayscale or ARGB value.
    * See notes attached to the fill() function.
    */
   public void stroke(int rgb) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {  // see above
       stroke((float) rgb);
 
     } else {
       colorCalcARGB(rgb, colorModeA);
       strokeFromCalc();
     }
   }
 
 
   public void stroke(int rgb, float alpha) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
       stroke((float) rgb, alpha);
 
     } else {
       colorCalcARGB(rgb, alpha);
       strokeFromCalc();
     }
   }
 
 
   public void stroke(float gray) {
     colorCalc(gray);
     strokeFromCalc();
   }
 
 
   public void stroke(float gray, float alpha) {
     colorCalc(gray, alpha);
     strokeFromCalc();
   }
 
 
   public void stroke(float x, float y, float z) {
     colorCalc(x, y, z);
     strokeFromCalc();
   }
 
 
   public void stroke(float x, float y, float z, float a) {
     colorCalc(x, y, z, a);
     strokeFromCalc();
   }
 
 
   protected void strokeFromCalc() {
     stroke = true;
     //strokeChanged = true;
     strokeR = calcR;
     strokeG = calcG;
     strokeB = calcB;
     strokeA = calcA;
     strokeRi = calcRi;
     strokeGi = calcGi;
     strokeBi = calcBi;
     strokeAi = calcAi;
     strokeColor = calcColor;
     strokeAlpha = calcAlpha;
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void noTint() {
     tint = false;
   }
 
 
   /**
    * Set the tint to either a grayscale or ARGB value. See notes
    * attached to the fill() function.
    */
   public void tint(int rgb) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
       tint((float) rgb);
 
     } else {
       colorCalcARGB(rgb, colorModeA);
       tintFromCalc();
     }
   }
 
   public void tint(int rgb, float alpha) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
       tint((float) rgb, alpha);
 
     } else {
       colorCalcARGB(rgb, alpha);
       tintFromCalc();
     }
   }
 
   public void tint(float gray) {
     colorCalc(gray);
     tintFromCalc();
   }
 
 
   public void tint(float gray, float alpha) {
     colorCalc(gray, alpha);
     tintFromCalc();
   }
 
 
   public void tint(float x, float y, float z) {
     colorCalc(x, y, z);
     tintFromCalc();
   }
 
 
   public void tint(float x, float y, float z, float a) {
     colorCalc(x, y, z, a);
     tintFromCalc();
   }
 
 
   protected void tintFromCalc() {
     tint = true;
     tintR = calcR;
     tintG = calcG;
     tintB = calcB;
     tintA = calcA;
     tintRi = calcRi;
     tintGi = calcGi;
     tintBi = calcBi;
     tintAi = calcAi;
     tintColor = calcColor;
     tintAlpha = calcAlpha;
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void noFill() {
     fill = false;
   }
 
 
   /**
    * Set the fill to either a grayscale value or an ARGB int.
    * <P>
    * The problem with this code is that it has to detect between
    * these two situations automatically. This is done by checking
    * to see if the high bits (the alpha for 0xAA000000) is set,
    * and if not, whether the color value that follows is less than
    * colorModeX (the first param passed to colorMode).
    * <P>
    * This auto-detect would break in the following situation:
    * <PRE>size(256, 256);
    * for (int i = 0; i < 256; i++) {
    *   color c = color(0, 0, 0, i);
    *   stroke(c);
    *   line(i, 0, i, 256);
    * }</PRE>
    * ...on the first time through the loop, where (i == 0),
    * since the color itself is zero (black) then it would appear
    * indistinguishable from someone having written fill(0).
    */
   public void fill(int rgb) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {  // see above
       fill((float) rgb);
 
     } else {
       colorCalcARGB(rgb, colorModeA);
       fillFromCalc();
     }
   }
 
 
   public void fill(int rgb, float alpha) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {  // see above
       fill((float) rgb, alpha);
 
     } else {
       colorCalcARGB(rgb, alpha);
       fillFromCalc();
     }
   }
 
 
   public void fill(float gray) {
     colorCalc(gray);
     fillFromCalc();
   }
 
 
   public void fill(float gray, float alpha) {
     colorCalc(gray, alpha);
     fillFromCalc();
   }
 
 
   public void fill(float x, float y, float z) {
     colorCalc(x, y, z);
     fillFromCalc();
   }
 
 
   public void fill(float x, float y, float z, float a) {
     colorCalc(x, y, z, a);
     fillFromCalc();
   }
 
 
   protected void fillFromCalc() {
     fill = true;
     fillR = calcR;
     fillG = calcG;
     fillB = calcB;
     fillA = calcA;
     fillRi = calcRi;
     fillGi = calcGi;
     fillBi = calcBi;
     fillAi = calcAi;
     fillColor = calcColor;
     fillAlpha = calcAlpha;
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void ambient(int rgb) {
     depthError("ambient");
   }
 
   public void ambient(float gray) {
     depthError("ambient");
   }
 
   public void ambient(float x, float y, float z) {
     depthError("ambient");
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void specular(int rgb) {
     depthError("specular");
   }
 
   public void specular(float gray) {
     depthError("specular");
   }
 
 //  public void specular(float gray, float alpha) {
 //    depthError("specular");
 //  }
 
   public void specular(float x, float y, float z) {
     depthError("specular");
   }
 
 //  public void specular(float x, float y, float z, float a) {
 //    depthError("specular");
 //  }
 
   public void shininess(float shine) {
     depthError("shininess");
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void emissive(int rgb) {
     depthError("emissive");
   }
 
   public void emissive(float gray) {
     depthError("emissive");
   }
 
   public void emissive(float x, float y, float z ) {
     depthError("emissive");
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // LIGHTS
 
 
   public void lights() {
     depthError("lights");
   }
 
   public void noLights() {
     depthError("noLights");
   }
 
   public void ambientLight(float red, float green, float blue) {
     depthError("ambientLight");
   }
 
   public void ambientLight(float red, float green, float blue,
                            float x, float y, float z) {
     depthError("ambientLight");
   }
 
   public void directionalLight(float red, float green, float blue,
                                float nx, float ny, float nz) {
     depthError("directionalLight");
   }
 
   public void pointLight(float red, float green, float blue,
                          float x, float y, float z) {
     depthError("pointLight");
   }
 
   public void spotLight(float red, float green, float blue,
                         float x, float y, float z,
                         float nx, float ny, float nz,
                         float angle, float concentration) {
     depthError("spotLight");
   }
 
   public void lightFalloff(float constant, float linear, float quadratic) {
     depthError("lightFalloff");
   }
 
   public void lightSpecular(float x, float y, float z) {
     depthError("lightSpecular");
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
 
   /**
    * Set the background to a gray or ARGB color.
    * <p>
    * For the main drawing surface, the alpha value will be ignored. However,
    * alpha can be used on PGraphics objects from createGraphics(). This is
    * the only way to set all the pixels partially transparent, for instance.
    * <p>
    * Note that background() should be called before any transformations occur,
    * because some implementations may require the current transformation matrix
    * to be identity before drawing.
    */
   public void background(int rgb) {
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
       background((float) rgb);
 
     } else {
       if (format == RGB) {
         rgb |= 0xff000000;  // ignore alpha for main drawing surface
       }
       colorCalcARGB(rgb, colorModeA);
       backgroundFromCalc();
       clear();
     }
   }
 
 
   /**
    * See notes about alpha in background(x, y, z, a).
    */
   public void background(int rgb, float alpha) {
     if (format == RGB) {
       background(rgb);  // ignore alpha for main drawing surface
 
     } else {
       if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
         background((float) rgb, alpha);
 
       } else {
         colorCalcARGB(rgb, alpha);
         backgroundFromCalc();
         clear();
       }
     }
   }
 
 
   /**
    * Set the background to a grayscale value, based on the
    * current colorMode.
    */
   public void background(float gray) {
     colorCalc(gray);
     backgroundFromCalc();
     clear();
   }
 
 
   /**
    * See notes about alpha in background(x, y, z, a).
    */
   public void background(float gray, float alpha) {
     if (format == RGB) {
       background(gray);  // ignore alpha for main drawing surface
 
     } else {
       colorCalc(gray, alpha);
       backgroundFromCalc();
       clear();
     }
   }
 
 
   /**
    * Set the background to an r, g, b or h, s, b value,
    * based on the current colorMode.
    */
   public void background(float x, float y, float z) {
     colorCalc(x, y, z);
     backgroundFromCalc();
     clear();
   }
 
 
   /**
    * Clear the background with a color that includes an alpha value. This can
    * only be used with objects created by createGraphics(), because the main
    * drawing surface cannot be set transparent.
    * <p>
    * It might be tempting to use this function to partially clear the screen
    * on each frame, however that's not how this function works. When calling
    * background(), the pixels will be replaced with pixels that have that level
    * of transparency. To do a semi-transparent overlay, use fill() with alpha
    * and draw a rectangle.
    */
   public void background(float x, float y, float z, float a) {
     if (format == RGB) {
       background(x, y, z);  // don't allow people to set alpha
 
     } else {
       colorCalc(x, y, z, a);
       backgroundFromCalc();
       clear();
     }
   }
 
 
   protected void backgroundFromCalc() {
     backgroundR = calcR;
     backgroundG = calcG;
     backgroundB = calcB;
     backgroundA = calcA;
     backgroundRi = calcRi;
     backgroundGi = calcGi;
     backgroundBi = calcBi;
     backgroundAi = calcAi;
     backgroundAlpha = calcAlpha;
     backgroundColor = calcColor;
   }
 
 
   /**
    * Takes an RGB or ARGB image and sets it as the background.
    * <P>
    * Note that even if the image is set as RGB, the high 8 bits of
    * each pixel should be set opaque (0xFF000000), because the image data
    * will be copied directly to the screen, and non-opaque background
    * images may have strange behavior. Using image.filter(OPAQUE)
    * will handle this easily.
    * <P>
    * When using 3D, this will also clear out the zbuffer and
    * stencil buffer if they exist.
    */
   public void background(PImage image) {
     if ((image.width != width) || (image.height != height)) {
       throw new RuntimeException("background image must be " +
                                  "the same size as your application");
     }
     if ((image.format != RGB) && (image.format != ARGB)) {
       throw new RuntimeException("background images should be RGB or ARGB");
     }
 
     // zero this out since it's an image
     backgroundColor = 0;
 
     // blit image to the screen
     System.arraycopy(image.pixels, 0, pixels, 0, pixels.length);
   }
 
 
   /**
    * Clear the pixel buffer.
    */
   abstract protected void clear();
 
 
 
   //////////////////////////////////////////////////////////////
 
   // MESSAGES / ERRORS / LOGGING
 
 
   protected void depthError(String method) {
     throw new RuntimeException(method + "() can only be used " +
                                "with P3D or OPENGL.");
   }
 
 
   protected void depthErrorXYZ(String method) {
     throw new RuntimeException(method + "(x, y, z) can only be used with " +
                                "OPENGL or P3D, use " +
                                method + "(x, y) instead.");
   }
 
 
   protected void unavailableError(String methodStr) {
     throw new RuntimeException(methodStr +
                                " is not available with this renderer");
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
   // COLOR MANIPULATION
 
   // these functions are really slow, but easy to use
   // if folks are advanced enough to want something faster,
   // they can write it themselves (not difficult)
 
 
   public final int color(int gray) {  // ignore
     if (((gray & 0xff000000) == 0) && (gray <= colorModeX)) {
       if (colorRgb255) {
         // bounds checking to make sure the numbers aren't to high or low
         if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
         return 0xff000000 | (gray << 16) | (gray << 8) | gray;
       } else {
         colorCalc(gray);
       }
     } else {
       colorCalcARGB(gray, colorModeA);
     }
     return calcColor;
   }
 
   public final int color(float gray) {  // ignore
     colorCalc(gray);
     return calcColor;
   }
 
 
   /**
    * @param gray can be packed ARGB or a gray in this case
    */
   public final int color(int gray, int alpha) {  // ignore
     if (colorRgb255) {
       // bounds checking to make sure the numbers aren't to high or low
       if (gray > 255) gray = 255; else if (gray < 0) gray = 0;
       if (alpha > 255) alpha = 255; else if (alpha < 0) alpha = 0;
 
       return ((alpha & 0xff) << 24) | (gray << 16) | (gray << 8) | gray;
     }
     colorCalc(gray, alpha);
     return calcColor;
   }
 
   /**
    * @param rgb can be packed ARGB or a gray in this case
    */
   public final int color(int rgb, float alpha) {  // ignore
     if (((rgb & 0xff000000) == 0) && (rgb <= colorModeX)) {
       colorCalc(rgb, alpha);
     } else {
       colorCalcARGB(rgb, alpha);
     }
     return calcColor;
   }
 
   public final int color(float gray, float alpha) {  // ignore
     colorCalc(gray, alpha);
     return calcColor;
   }
 
 
   public final int color(int x, int y, int z) {  // ignore
     if (colorRgb255) {
       // bounds checking to make sure the numbers aren't to high or low
       if (x > 255) x = 255; else if (x < 0) x = 0;
       if (y > 255) y = 255; else if (y < 0) y = 0;
       if (z > 255) z = 255; else if (z < 0) z = 0;
 
       return 0xff000000 | (x << 16) | (y << 8) | z;
     }
     colorCalc(x, y, z);
     return calcColor;
   }
 
   public final int color(float x, float y, float z) {  // ignore
     colorCalc(x, y, z);
     return calcColor;
   }
 
 
   public final int color(int x, int y, int z, int a) {  // ignore
     if (colorRgb255) {
       // bounds checking to make sure the numbers aren't to high or low
       if (a > 255) a = 255; else if (a < 0) a = 0;
       if (x > 255) x = 255; else if (x < 0) x = 0;
       if (y > 255) y = 255; else if (y < 0) y = 0;
       if (z > 255) z = 255; else if (z < 0) z = 0;
 
       return (a << 24) | (x << 16) | (y << 8) | z;
     }
     colorCalc(x, y, z, a);
     return calcColor;
   }
 
   public final int color(float x, float y, float z, float a) {  // ignore
     colorCalc(x, y, z, a);
     return calcColor;
   }
 
 
   public final float alpha(int what) {
     float c = (what >> 24) & 0xff;
     if (colorModeA == 255) return c;
     return (c / 255.0f) * colorModeA;
   }
 
   public final float red(int what) {
     float c = (what >> 16) & 0xff;
     if (colorRgb255) return c;
     return (c / 255.0f) * colorModeX;
   }
 
   public final float green(int what) {
     float c = (what >> 8) & 0xff;
     if (colorRgb255) return c;
     return (c / 255.0f) * colorModeY;
   }
 
   public final float blue(int what) {
     float c = (what) & 0xff;
     if (colorRgb255) return c;
     return (c / 255.0f) * colorModeZ;
   }
 
 
   public final float hue(int what) {
     if (what != cacheHsbKey) {
       Color.RGBtoHSB((what >> 16) & 0xff, (what >> 8) & 0xff,
                      what & 0xff, cacheHsbValue);
       cacheHsbKey = what;
     }
     return cacheHsbValue[0] * colorModeX;
   }
 
   public final float saturation(int what) {
     if (what != cacheHsbKey) {
       Color.RGBtoHSB((what >> 16) & 0xff, (what >> 8) & 0xff,
                      what & 0xff, cacheHsbValue);
       cacheHsbKey = what;
     }
     return cacheHsbValue[1] * colorModeY;
   }
 
   public final float brightness(int what) {
     if (what != cacheHsbKey) {
       Color.RGBtoHSB((what >> 16) & 0xff, (what >> 8) & 0xff,
                      what & 0xff, cacheHsbValue);
       cacheHsbKey = what;
     }
     return cacheHsbValue[2] * colorModeZ;
   }
 
 
   /**
    * Interpolate between two colors, using the current color mode.
    */
   public int lerpColor(int c1, int c2, float amt) {
     return lerpColor(c1, c2, amt, colorMode);
   }
 
   static float[] lerpColorHSB1;
   static float[] lerpColorHSB2;
 
   /**
    * Interpolate between two colors. Like lerp(), but for the
    * individual color components of a color supplied as an int value.
    */
   static public int lerpColor(int c1, int c2, float amt, int mode) {
     if (mode == RGB) {
       float a1 = ((c1 >> 24) & 0xff);
       float r1 = (c1 >> 16) & 0xff;
       float g1 = (c1 >> 8) & 0xff;
       float b1 = c1 & 0xff;
       float a2 = (c2 >> 24) & 0xff;
       float r2 = (c2 >> 16) & 0xff;
       float g2 = (c2 >> 8) & 0xff;
       float b2 = c2 & 0xff;
 
       return (((int) (a1 + (a2-a1)*amt) << 24) |
               ((int) (r1 + (r2-r1)*amt) << 16) |
               ((int) (g1 + (g2-g1)*amt) << 8) |
               ((int) (b1 + (b2-b1)*amt)));
 
     } else if (mode == HSB) {
       if (lerpColorHSB1 == null) {
         lerpColorHSB1 = new float[3];
         lerpColorHSB2 = new float[3];
       }
 
       float a1 = (c1 >> 24) & 0xff;
       float a2 = (c2 >> 24) & 0xff;
       int alfa = ((int) (a1 + (a2-a1)*amt)) << 24;
 
       Color.RGBtoHSB((c1 >> 16) & 0xff, (c1 >> 8) & 0xff, c1 & 0xff,
                      lerpColorHSB1);
       Color.RGBtoHSB((c2 >> 16) & 0xff, (c2 >> 8) & 0xff, c2 & 0xff,
                      lerpColorHSB2);
 
       /* If mode is HSB, this will take the shortest path around the
        * color wheel to find the new color. For instance, red to blue
        * will go red violet blue (backwards in hue space) rather than
        * cycling through ROYGBIV.
        */
       // Disabling rollover (wasn't working anyway) for 0126.
       // Otherwise it makes full spectrum scale impossible for
       // those who might want it...in spite of how despicable
       // a full spectrum scale might be.
       // roll around when 0.9 to 0.1
       // more than 0.5 away means that it should roll in the other direction
       /*
       float h1 = lerpColorHSB1[0];
       float h2 = lerpColorHSB2[0];
       if (Math.abs(h1 - h2) > 0.5f) {
         if (h1 > h2) {
           // i.e. h1 is 0.7, h2 is 0.1
           h2 += 1;
         } else {
           // i.e. h1 is 0.1, h2 is 0.7
           h1 += 1;
         }
       }
       float ho = (PApplet.lerp(lerpColorHSB1[0], lerpColorHSB2[0], amt)) % 1.0f;
       */
       float ho = PApplet.lerp(lerpColorHSB1[0], lerpColorHSB2[0], amt);
       float so = PApplet.lerp(lerpColorHSB1[1], lerpColorHSB2[1], amt);
       float bo = PApplet.lerp(lerpColorHSB1[2], lerpColorHSB2[2], amt);
 
       return alfa | (Color.HSBtoRGB(ho, so, bo) & 0xFFFFFF);
     }
     return 0;
   }
 
 
   //////////////////////////////////////////////////////////////
 
   // MATH
 
 
   static final float sqrt(float a) {
     return (float)Math.sqrt(a);
   }
 
 
 
   //////////////////////////////////////////////////////////////
 
 
   /**
    * Use with caution on PGraphics. This should not be used with
    * the base PGraphics that's tied to a PApplet, but it can be used
    * with user-created PGraphics objects that are drawn to the screen.
    */
   public void mask(int alpha[]) {  // ignore
     super.mask(alpha);
   }
 
 
   /**
    * Use with caution on PGraphics. This should not be used with
    * the base PGraphics that's tied to a PApplet, but it can be used
    * with user-created PGraphics objects that are drawn to the screen.
    */
   public void mask(PImage alpha) {  // ignore
     super.mask(alpha);
   }
 
 
   //////////////////////////////////////////////////////////////
 
 
   public void beginRaw(PGraphics rawGraphics) {  // ignore
     this.raw = rawGraphics;
     rawGraphics.beginDraw();
   }
 
 
   public void endRaw() {  // ignore
     if (raw != null) {
       // for 3D, need to flush any geometry that's been stored for sorting
       // (particularly if the ENABLE_DEPTH_SORT hint is set)
       flush();
 
       // just like beginDraw, this will have to be called because
       // endDraw() will be happening outside of draw()
       raw.endDraw();
       raw.dispose();
       raw = null;
     }
   }
 
 
   /**
    * Handle any takedown for this graphics context.
    * <p>
    * This is called when a sketch is shut down and this renderer was
    * specified using the size() command, or inside endRecord() and
    * endRaw(), in order to shut things off.
    */
   public void dispose() {  // ignore
   }
 
 
   /**
    * Return true if this renderer should be drawn to the screen.
    * Overridden for subclasses like PDF so that an enormous window
    * doesn't open up.
    * showFrame, displayable, isVisible, visible, shouldDisplay,
    * what to call this?
    */
   public boolean displayable() {
     return true;
   }
 }
