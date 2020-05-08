 package de.sofd.viskit.controllers.cellpaint;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.color.ColorSpace;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.awt.image.Raster;
 import java.awt.image.RescaleOp;
 import java.awt.image.WritableRaster;
 import java.nio.IntBuffer;
 import java.nio.ShortBuffer;
 import java.util.Map;
 
 import javax.media.opengl.GL2;
 import javax.media.opengl.GLAutoDrawable;
 import javax.media.opengl.GLException;
 
 import org.apache.log4j.Logger;
 import org.dcm4che2.data.DicomObject;
 import org.dcm4che2.data.Tag;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.opengl.GL12;
 import org.lwjgl.opengl.GL13;
 
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import de.sofd.math.LinAlg;
 import de.sofd.util.FloatRange;
 import de.sofd.viskit.image.RawImage;
 import de.sofd.viskit.image.ViskitImage;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.GrayscaleRGBLookupTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.ImageTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLGrayscaleRGBLookupTableTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLImageTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.JGLLookupTableTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.LookupTableTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLGrayscaleRGBLookupTableTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLImageTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.LWJGLLookupTableTextureManager;
 import de.sofd.viskit.controllers.cellpaint.texturemanager.ImageTextureManager.TextureRef;
 import de.sofd.viskit.image3D.util.Shader;
 import de.sofd.viskit.image3D.util.ShaderManager;
 import de.sofd.viskit.model.DicomImageListViewModelElement;
 import de.sofd.viskit.model.ImageListViewModelElement;
 import de.sofd.viskit.model.LookupTable;
 import de.sofd.viskit.ui.imagelist.ImageListView;
 import de.sofd.viskit.ui.imagelist.ImageListViewCell;
 
 /**
  * Cell-painting controller that paints the image of the cell's model element
  * (cell.getDisplayedModelElement().getImage()) into the cell.
  * 
  * @author olaf
  */
 public class ImageListViewImagePaintController extends CellPaintControllerBase {
 
     static final Logger logger = Logger.getLogger(ImageListViewImagePaintController.class);
     
     private static ShaderManager shaderManager = ShaderManager.getInstance();
     private Shader rescaleShader;
     private ImageTextureManager texManager;    
     private LookupTableTextureManager lutTexManager;
     private GrayscaleRGBLookupTextureManager grayScaleTexManager;
     
     private boolean isInitialized = false;
     
     static {
         shaderManager.init("shader");
     }
 
 
     
     public ImageListViewImagePaintController() {
         this(null, ImageListView.PAINT_ZORDER_IMAGE);
     }
 
     public ImageListViewImagePaintController(ImageListView controlledImageListView) {
         super(controlledImageListView, ImageListView.PAINT_ZORDER_IMAGE);
     }
 
     public ImageListViewImagePaintController(ImageListView controlledImageListView, int zOrder) {
         super(controlledImageListView, zOrder);
     }
     
     ///////////// OpenGL rendering with JOGL
     
     @Override
     protected void glDrawableInitialized(GLAutoDrawable glAutoDrawable) {
         texManager = JGLImageTextureManager.getInstance();
         lutTexManager = JGLLookupTableTextureManager.getInstance();
         grayScaleTexManager = JGLGrayscaleRGBLookupTableTextureManager.getInstance();
         initializeGLShader();
     }
     
     protected void initializeGLShader() {
         try {        
             shaderManager.read("rescaleop");
             rescaleShader = shaderManager.get("rescaleop");
             rescaleShader.addProgramUniform("preScale");
             rescaleShader.addProgramUniform("preOffset");
             rescaleShader.addProgramUniform("scale");
             rescaleShader.addProgramUniform("offset");
             rescaleShader.addProgramUniform("tex");
             rescaleShader.addProgramUniform("lutTex");
             rescaleShader.addProgramUniform("useLut");
             rescaleShader.addProgramUniform("useLutAlphaBlending");
             rescaleShader.addProgramUniform("useGrayscaleRGBOutput");
             rescaleShader.addProgramUniform("grayscaleRgbTex");
         } catch (Exception e) {
             throw new RuntimeException("couldn't initialize GL shader: " + e.getLocalizedMessage(), e);
         }
     }
     
     
     @Override
     protected void paintLWJGL(ImageListViewCell cell, LWJGLRenderer renderer, Map<String,Object> sharedContextData) {
         // TODO shader initialization by firing events like {@link ImageListViewCellPaintListener#glDrawableInitialized(GLAutoDrawable}
         if(!isInitialized) {
             initializeGLShader();
             texManager = LWJGLImageTextureManager.getInstance();
             lutTexManager = LWJGLLookupTableTextureManager.getInstance();
             grayScaleTexManager = LWJGLGrayscaleRGBLookupTableTextureManager.getInstance();
             isInitialized = true;
         }
         final ImageListViewModelElement elt = cell.getDisplayedModelElement();
         final ViskitImage img = elt.getImage();
         Dimension cellSize = cell.getLatestSize();
 
         GL11.glPushMatrix();
         GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);    
         try {
             GL11.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
             GL11.glTranslated(cell.getCenterOffset().getX(), cell.getCenterOffset().getY(), 0);
             GL11.glScaled(cell.getScale(), cell.getScale(), 1);          
             TextureRef texRef = texManager.bindImageTexture(null, GL13.GL_TEXTURE1, sharedContextData, cell.getDisplayedModelElement());
             
             LookupTable lut = cell.getLookupTable();
             try {
                 rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...? (but shaders are shared betw. contexts, so if it's outdated, we'll have other problems as well...)
             } catch (Exception e) {
                 // TODO: this is a total hack to "resolve" the above. It should not really be done; there is no guarantee
                 // the exception is raised anyway
                 logger.error("binding the rescale GL shader failed, trying to compile it anew", e);
                 initializeGLShader();
             }
             rescaleShader.bindUniform("tex", 1);
             if (cell.isOutputGrayscaleRGBs()) {
                 grayScaleTexManager.bindGrayscaleRGBLutTexture(null, GL13.GL_TEXTURE3, sharedContextData, cell.getDisplayedModelElement());
                 rescaleShader.bindUniform("grayscaleRgbTex", 3);
                 rescaleShader.bindUniform("useGrayscaleRGBOutput", true);
                 rescaleShader.bindUniform("useLut", false);
                 rescaleShader.bindUniform("useLutAlphaBlending", false);
             } else if (lut != null) {
                 lutTexManager.bindLutTexture(null, GL13.GL_TEXTURE2, sharedContextData, cell.getLookupTable());
                 rescaleShader.bindUniform("lutTex", 2);
                 rescaleShader.bindUniform("useLut", true);
                 switch (cell.getCompositingMode()) {
                 case CM_BLEND:
                     rescaleShader.bindUniform("useLutAlphaBlending", true);
                     break;
                 default:
                     rescaleShader.bindUniform("useLutAlphaBlending", false);
                 }
                 rescaleShader.bindUniform("useGrayscaleRGBOutput", false);
             } else {
                 rescaleShader.bindUniform("useLut", false);
                 rescaleShader.bindUniform("useLutAlphaBlending", false);
                 rescaleShader.bindUniform("useGrayscaleRGBOutput", false);
             }
             rescaleShader.bindUniform("preScale", texRef.getPreScale());
             rescaleShader.bindUniform("preOffset", texRef.getPreOffset());
             {
                 FloatRange pxValuesRange = img.getMaximumPixelValuesRange();
                 float minGrayvalue = pxValuesRange.getMin();
                 float nGrayvalues = pxValuesRange.getDelta();
                 float wl = (cell.getWindowLocation() - minGrayvalue) / nGrayvalues;
                 float ww = cell.getWindowWidth() / nGrayvalues;
                 float scale = 1F/ww;
                 float offset = (ww/2-wl)*scale;
                 // HACK HACK: Apply DICOM rescale slope/intercept values if present. TODO: DICOM-specific code doesn't belong here?
                 // TODO: this will cause the "optimal windowing" parameters as calculated by e.g. ImageListViewInitialWindowingController
                 // to produce wrongly windowed images. Happens e.g. with the Charite dental images.
                 if (elt instanceof DicomImageListViewModelElement) {
                     try {
                         DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                         if (delt.getDicomImageMetaData().contains(Tag.RescaleSlope) && delt.getDicomImageMetaData().contains(Tag.RescaleIntercept)) {
                             float rscSlope = delt.getDicomImageMetaData().getFloat(Tag.RescaleSlope);
                             float rscIntercept = delt.getDicomImageMetaData().getFloat(Tag.RescaleIntercept) / nGrayvalues;
                             offset = scale * rscIntercept + offset;
                             scale = scale * rscSlope;
                         }
                     } catch (Exception e) {
                         //ignore -- no error, use default preScale/preOffset
                     }
                 }
                 rescaleShader.bindUniform("scale", scale);
                 rescaleShader.bindUniform("offset", offset);
             }
             // TODO: (GL_TEXTURE_ENV is ignored because a frag shader is active) make the compositing mode configurable (replace/combine)
             GL11.glTexEnvi(GL11.GL_TEXTURE_ENV, GL11.GL_TEXTURE_ENV_MODE, GL11.GL_REPLACE);
             GL11.glColor3f(0, 1, 0);
             float w2 = (float) img.getWidth() / 2, h2 = (float) img.getHeight() / 2;
             GL11.glBegin(GL11.GL_QUADS);
             // TODO: wrong orientation here? check visually!       
             GL11.glTexCoord2f(texRef.left(), texRef.top());
             GL11.glVertex2f(-w2, h2);
             GL11.glTexCoord2f(texRef.right(), texRef.top());
             GL11.glVertex2f( w2,  h2);
             GL11.glTexCoord2f(texRef.right(), texRef.bottom());
             GL11.glVertex2f( w2, -h2);
             GL11.glTexCoord2f(texRef.left(), texRef.bottom());
             GL11.glVertex2f(-w2, -h2);
             
             GL11.glEnd();
             texManager.unbindCurrentImageTexture(null);
             rescaleShader.unbind();
         } finally {
             GL11.glPopMatrix();
         }
     }
 
     @Override
     protected void paintGL(ImageListViewCell cell, GL2 gl, Map<String, Object> sharedContextData) {
         final ImageListViewModelElement elt = cell.getDisplayedModelElement();
         final ViskitImage img = elt.getImage();
         
         Dimension cellSize = cell.getLatestSize();
         gl.glPushMatrix();
         try {
             //gl.glLoadIdentity();
             gl.glTranslated(cellSize.getWidth() / 2, cellSize.getHeight() / 2, 0);
             gl.glTranslated(cell.getCenterOffset().getX(), cell.getCenterOffset().getY(), 0);
             gl.glScaled(cell.getScale(), cell.getScale(), 1);
             //TODO: texture caching using img, not elt, as the cache key
 
             TextureRef texRef = texManager.bindImageTexture(gl, GL2.GL_TEXTURE1, sharedContextData, cell.getDisplayedModelElement()/*, cell.isOutputGrayscaleRGBs()*/);
             
             LookupTable lut = cell.getLookupTable();
             try {
                 rescaleShader.bind();  // TODO: rescaleShader's internal gl may be outdated here...? (but shaders are shared betw. contexts, so if it's outdated, we'll have other problems as well...)
             } catch (GLException e) {
                 // TODO: this is a total hack to "resolve" the above. It should not really be done; there is no guarantee
                 // the exception is raised anyway
                 logger.error("binding the rescale GL shader failed, trying to compile it anew", e);
                 initializeGLShader();
             }
             //TODO: reuse the pixelTransform stuff from the J2D renderer (tryWindowRawImage()) and use only that in the
             // shader rather than the plethora of shader variables we have now
             rescaleShader.bindUniform("tex", 1);
             if (cell.isOutputGrayscaleRGBs()) {
                 grayScaleTexManager.bindGrayscaleRGBLutTexture(gl, GL2.GL_TEXTURE3, sharedContextData, cell.getDisplayedModelElement());
                 rescaleShader.bindUniform("grayscaleRgbTex", 3);
                 rescaleShader.bindUniform("useGrayscaleRGBOutput", true);
                 rescaleShader.bindUniform("useLut", false);
                 rescaleShader.bindUniform("useLutAlphaBlending", false);
             } else if (lut != null) {
                 lutTexManager.bindLutTexture(gl, GL2.GL_TEXTURE2, sharedContextData, cell.getLookupTable());
                 rescaleShader.bindUniform("lutTex", 2);
                 rescaleShader.bindUniform("useLut", true);
                 switch (cell.getCompositingMode()) {
                 case CM_BLEND:
                     rescaleShader.bindUniform("useLutAlphaBlending", true);
                     break;
                 default:
                     rescaleShader.bindUniform("useLutAlphaBlending", false);
                 }
                 rescaleShader.bindUniform("useGrayscaleRGBOutput", false);
             } else {
                 rescaleShader.bindUniform("useLut", false);
                 rescaleShader.bindUniform("useLutAlphaBlending", false);
                 rescaleShader.bindUniform("useGrayscaleRGBOutput", false);
             }
             rescaleShader.bindUniform("preScale", texRef.getPreScale());
             rescaleShader.bindUniform("preOffset", texRef.getPreOffset());
             {
                 FloatRange pxValuesRange = img.getMaximumPixelValuesRange();
                 float minGrayvalue = pxValuesRange.getMin();
                 float nGrayvalues = pxValuesRange.getDelta();
                 float wl = (cell.getWindowLocation() - minGrayvalue) / nGrayvalues;
                 float ww = cell.getWindowWidth() / nGrayvalues;
                 float scale = 1F/ww;
                 float offset = (ww/2-wl)*scale;
                 // HACK HACK: Apply DICOM rescale slope/intercept values if present. TODO: DICOM-specific code doesn't belong here?
                 // TODO: this will cause the "optimal windowing" parameters as calculated by e.g. ImageListViewInitialWindowingController
                 // to produce wrongly windowed images. Happens e.g. with the Charite dental images.
                 if (elt instanceof DicomImageListViewModelElement) {
                     try {
                         DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                         if (delt.getDicomImageMetaData().contains(Tag.RescaleSlope) && delt.getDicomImageMetaData().contains(Tag.RescaleIntercept)) {
                             float rscSlope = delt.getDicomImageMetaData().getFloat(Tag.RescaleSlope);
                             float rscIntercept = delt.getDicomImageMetaData().getFloat(Tag.RescaleIntercept) / nGrayvalues;
                             offset = scale * rscIntercept + offset;
                             scale = scale * rscSlope;
                         }
                     } catch (Exception e) {
                         //ignore -- no error, use default preScale/preOffset
                     }
                 }
                 rescaleShader.bindUniform("scale", scale);
                 rescaleShader.bindUniform("offset", offset);
                 //rescaleShader.bindUniform("scale", 1.0F);
                 //rescaleShader.bindUniform("offset", 0.0F);
             }
             // TODO: (GL_TEXTURE_ENV is ignored because a frag shader is active) make the compositing mode configurable (replace/combine)
             gl.glTexEnvi(GL2.GL_TEXTURE_ENV, GL2.GL_TEXTURE_ENV_MODE, gl.GL_REPLACE);
             gl.glColor3f(0, 1, 0);
             float w2 = (float) img.getWidth() / 2, h2 = (float) img.getHeight() / 2;
             gl.glBegin(GL2.GL_QUADS);
             // TODO: wrong orientation here? check visually!
            
             gl.glTexCoord2f(texRef.left(), texRef.top());
             gl.glVertex2f(-w2, h2);
             gl.glTexCoord2f(texRef.right(), texRef.top());
             gl.glVertex2f( w2,  h2);
             gl.glTexCoord2f(texRef.right(), texRef.bottom());
             gl.glVertex2f( w2, -h2);
             gl.glTexCoord2f(texRef.left(), texRef.bottom());
             gl.glVertex2f(-w2, -h2);            
             gl.glEnd();
             texManager.unbindCurrentImageTexture(gl);
             rescaleShader.unbind();
         } finally {
             gl.glPopMatrix();
         }
     }
     
 
 
 
     ///////////// Java2D rendering
     
     @Override
     protected void paintJ2D(ImageListViewCell cell, Graphics2D g2d) {
         //give the render* methods a Graphics2D whose coordinate system
         //(and eventually, clipping) is already relative to the area in
         //which the image should be drawn
         Graphics2D userGraphics = (Graphics2D)g2d.create();
         Point2D imageOffset = getImageOffset(cell);
         userGraphics.transform(AffineTransform.getTranslateInstance(imageOffset.getX(), imageOffset.getY()));
 
         // render the image
         BufferedImageOp scaleImageOp = new AffineTransformOp(getDicomToUiTransform(cell), AffineTransformOp.TYPE_BILINEAR);
         userGraphics.drawImage(getWindowedImage(cell), scaleImageOp, 0, 0);
     }
     
     public Point2D getImageOffset(ImageListViewCell cell) {
         Point2D imgSize = getScaledImageSize(cell);
         Dimension latestSize = cell.getLatestSize();
         return new Point2D.Double((latestSize.width + 2 * cell.getCenterOffset().getX() - (int) imgSize.getX()) / 2,
                                   (latestSize.height + 2 * cell.getCenterOffset().getY() - (int) imgSize.getY()) / 2);
     }
 
     public Point2D getScaledImageSize(ImageListViewCell cell) {
         ViskitImage img = cell.getDisplayedModelElement().getImage();
         return getDicomToUiTransform(cell).transform(new Point2D.Double(img.getWidth(), img.getHeight()), null);
     }
     
     protected AffineTransform getDicomToUiTransform(ImageListViewCell cell) {
         double z = cell.getScale();
         return AffineTransform.getScaleInstance(z, z);
     }
 
     protected BufferedImage getWindowedImage(ImageListViewCell displayedCell) {
         BufferedImage windowedImage = tryWindowRawImage(displayedCell);
         if (windowedImage != null) {
             return windowedImage;
         } else {
             // TODO: caching of windowed images, probably using
             //       displayedCell.getDisplayedModelElement().getImageKey() and the windowing parameters as the cache key
             BufferedImage srcImg = displayedCell.getDisplayedModelElement().getImage().getBufferedImage();
             // TODO: use the model element's RawImage instead of the BufferedImage when possible
             ///*
             if (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY) {
                 windowedImage = windowMonochrome(displayedCell, srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
             } else if (srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
                 windowedImage = windowRGB(srcImg, displayedCell.getWindowLocation(), displayedCell.getWindowWidth());
             } else {
                 throw new IllegalStateException("don't know how to window image with color space " + srcImg.getColorModel().getColorSpace());
                 // TODO: do something cleverer here? Like, create windowedImage
                 //    with a color space that's "compatible" to srcImg (using
                 //    some createCompatibleImage() method in BufferedImage or elsewhere),
                 //    window all bands of that, and let the JRE figure out how to draw the result?
             }
             //*/
             //windowedImage = windowWithRasterOp(srcImg, windowLocation, windowWidth);
             //windowedImage = srcImg;
     
             return windowedImage;
         }
     }
 
     private BufferedImage tryWindowRawImage(ImageListViewCell displayedCell) {
         ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
         ViskitImage img = elt.getImage();
         if (!(img.hasRawImage() && img.isRawImagePreferable())) {
             return null;
         }
         
         //logger.debug("trying to create windowed BufferedImage for: " + elt.getImageKey());
 
         float[] pixelTransform = new float[]{1,0}; // compute the complete raw input => output pixel transformation in here
         
         // rescale/slope tags
         if (elt instanceof DicomImageListViewModelElement) {
             try {
                 DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                 if (delt.getDicomImageMetaData().contains(Tag.RescaleSlope) && delt.getDicomImageMetaData().contains(Tag.RescaleIntercept)) {
                     float rscSlope = delt.getDicomImageMetaData().getFloat(Tag.RescaleSlope);
                     float rscIntercept = delt.getDicomImageMetaData().getFloat(Tag.RescaleIntercept);
                     LinAlg.matrMult1D(new float[]{rscSlope, rscIntercept}, pixelTransform, pixelTransform);
                 }
             } catch (Exception e) {
                 //ignore -- no error
             }
         }
         
         // normalization to [0,1]
         FloatRange pxValuesRange = img.getMaximumPixelValuesRange();
         float minGrayvalue = pxValuesRange.getMin();
         float nGrayvalues = pxValuesRange.getDelta();
         LinAlg.matrMult1D(new float[]{1.0F/nGrayvalues, -minGrayvalue/nGrayvalues}, pixelTransform, pixelTransform);
 
         // window level/width
         float wl = (displayedCell.getWindowLocation() - minGrayvalue) / nGrayvalues;
         float ww = displayedCell.getWindowWidth() / nGrayvalues;
         float scale = 1F/ww;
         float offset = (ww/2-wl)*scale;
         LinAlg.matrMult1D(new float[]{scale, offset}, pixelTransform, pixelTransform);
 
 
         LookupTable lut = displayedCell.getLookupTable();
         int lutLength = -1, lutLengthMinus1 = -1;
         /*if (displayedCell.isOutputGrayscaleRGBs()) {
             //TODO: perform 12-bit grayscale output in J2D as well
         } else */if (lut != null) {
             // transformation to output LUT index
             lutLength = lut.getRGBAValues().limit() / 4;
             lutLengthMinus1 = lutLength - 1;
             LinAlg.matrMult1D(new float[]{lutLength, 0}, pixelTransform, pixelTransform);
         } else {
             // transformation to output grayscale range ( [0,256) )
             LinAlg.matrMult1D(new float[]{256, 0}, pixelTransform, pixelTransform);
         }
         
         RawImage rimg = img.getRawImage();
         if (rimg.getPixelFormat() != RawImage.PIXEL_FORMAT_LUMINANCE) {
             return null;  // can't window raw RGB images for now
         } else {
             switch (rimg.getPixelType()) {
             // will only window RawImages whose pixelData buffer is a ShortBuffer for now
             case RawImage.PIXEL_TYPE_UNSIGNED_BYTE:
             case RawImage.PIXEL_TYPE_SIGNED_12BIT:
             case RawImage.PIXEL_TYPE_SIGNED_16BIT:
             case RawImage.PIXEL_TYPE_UNSIGNED_12BIT: {
                 //TODO: maybe reuse BufferedImages of the same size to relieve the GC
                 float txscale = pixelTransform[0];
                 float txoffset = pixelTransform[1];
                 
                 ShortBuffer srcBuffer = (ShortBuffer) rimg.getPixelData();
                 
                 int w = rimg.getWidth();
                 int h = rimg.getHeight();
                 int index = 0;
                 BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                 WritableRaster resultRaster = result.getRaster();
                 if (lut != null) {
                     //separate loop for LUTs to spare one if-then-else in the innermost loop body (the difference is probably neglectible though..)
                     for (int y = 0; y < h; y++) {
                         for (int x = 0; x < w; x++) {
                             int destLutIndex = (int)(txscale * srcBuffer.get(index++) + txoffset);
                             //clamp
                             if (destLutIndex < 0) {
                                 destLutIndex = 0;
                             } else if (destLutIndex >= lutLength) {
                                 destLutIndex = lutLengthMinus1;
                             }
                             destLutIndex *= 4; //=> index into the buffer. May have done this with the pixelTransform,
                             //but would have to convert the result to a multiple of 4 afterwards then, which make it slower
 
                             // TODO: for better performance, maybe set the whole pixel at once from
                             //       a pre-computed IntBuffer version of the LUT containing each RGBA quadruple in one int
                             resultRaster.setSample(x, y, 0, 255 * lut.getRGBAValues().get(destLutIndex));
                             resultRaster.setSample(x, y, 1, 255 * lut.getRGBAValues().get(destLutIndex + 1));
                             resultRaster.setSample(x, y, 2, 255 * lut.getRGBAValues().get(destLutIndex + 2));
                         }
                     }
                 } else {
                     for (int y = 0; y < h; y++) {
                         for (int x = 0; x < w; x++) {
                             float destGrayValue = txscale * srcBuffer.get(index++) + txoffset;
                             //clamp
                             if (destGrayValue < 0) {
                                 destGrayValue = 0;
                             } else if (destGrayValue >= 256) {
                                 destGrayValue = 255;
                             }
                             resultRaster.setSample(x, y, 0, destGrayValue);
                             resultRaster.setSample(x, y, 1, destGrayValue);
                             resultRaster.setSample(x, y, 2, destGrayValue);
                         }
                     }
                 }
                 return result;
             }
             //...and IntBuffer
             case RawImage.PIXEL_TYPE_UNSIGNED_16BIT: {
                 //TODO: maybe reuse BufferedImages of the same size to relieve the GC
                 float txscale = pixelTransform[0];
                 float txoffset = pixelTransform[1];
 
                 IntBuffer srcBuffer = (IntBuffer) rimg.getPixelData();
                 int w = rimg.getWidth();
                 int h = rimg.getHeight();
                 int index = 0;
                 BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                 WritableRaster resultRaster = result.getRaster();
                 if (lut != null) {
                     //separate loop for LUTs to spare one if-then-else in the innermost loop body (the difference is probably neglectible though..)
                     for (int y = 0; y < h; y++) {
                         for (int x = 0; x < w; x++) {
                             int destLutIndex = (int)(txscale * srcBuffer.get(index++) + txoffset);
                             //clamp
                             if (destLutIndex < 0) {
                                 destLutIndex = 0;
                             } else if (destLutIndex >= lutLength) {
                                 destLutIndex = lutLengthMinus1;
                             }
                             destLutIndex *= 4; //=> index into the buffer. May have done this with the pixelTransform,
                             //but would have to convert the result to a multiple of 4 afterwards then, which make it slower
 
                             // TODO: for better performance, maybe set the whole pixel at once from
                             //       a pre-computed IntBuffer version of the LUT containing each RGBA quadruple in one int
                            resultRaster.setSample(x, y, 0, 256 * lut.getRGBAValues().get(destLutIndex));
                            resultRaster.setSample(x, y, 1, 256 * lut.getRGBAValues().get(destLutIndex + 1));
                            resultRaster.setSample(x, y, 2, 256 * lut.getRGBAValues().get(destLutIndex + 2));
                         }
                     }
                 } else {
                     for (int y = 0; y < h; y++) {
                         for (int x = 0; x < w; x++) {
                             float destGrayValue = txscale * srcBuffer.get(index++) + txoffset;
                             //clamp
                             if (destGrayValue < 0) {
                                 destGrayValue = 0;
                             } else if (destGrayValue >= 256) {
                                 destGrayValue = 255;
                             }
                             resultRaster.setSample(x, y, 0, destGrayValue);
                             resultRaster.setSample(x, y, 1, destGrayValue);
                             resultRaster.setSample(x, y, 2, destGrayValue);
                         }
                     }
                 }
                 return result;
             }
             default:
                 return null;
             }
         }
     }
 
     private BufferedImage windowMonochrome(ImageListViewCell displayedCell, BufferedImage srcImg, float windowLocation, float windowWidth) {
         BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
 
         boolean isSigned = false;
         int minValue = 0;
         {
             // hack: try to determine signedness and minValue from DICOM metadata if available --
             // the BufferedImage's metadata don't contain that information reliably.
             // Only works for some special cases
             ImageListViewModelElement elt = displayedCell.getDisplayedModelElement();
             if (elt instanceof DicomImageListViewModelElement) {
                 DicomImageListViewModelElement delt = (DicomImageListViewModelElement) elt;
                 DicomObject imgMetadata = delt.getDicomImageMetaData();
                 int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
                 isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
                 if (isSigned && (bitsAllocated > 0)) {
                     minValue = -(1<<(bitsAllocated-1));
                 }
             }
         }
 
 
         final int windowedImageGrayscalesCount = 256;  // for BufferedImage.TYPE_INT_RGB
         float scale = windowedImageGrayscalesCount/windowWidth;
         float offset = (windowWidth/2 - windowLocation)*scale;
         if (! (srcImg.getColorModel().getColorSpace().getType() == ColorSpace.TYPE_GRAY)) {
             throw new IllegalArgumentException("source image must be grayscales");
         }
         Raster srcRaster = srcImg.getRaster();
         if (srcRaster.getNumBands() != 1) {
             throw new IllegalArgumentException("source image must be grayscales");
         }
         WritableRaster resultRaster = destImg.getRaster();
         for (int x = 0; x < srcImg.getWidth(); x++) {
             for (int y = 0; y < srcImg.getHeight(); y++) {
                 int srcGrayValue = srcRaster.getSample(x, y, 0);
                 if (isSigned) {
                     srcGrayValue = (int)(short)srcGrayValue;  // will only work for 16-bit signed...
                 }
                 float destGrayValue = scale * srcGrayValue + offset;
                 // clamp
                 if (destGrayValue < 0) {
                     destGrayValue = 0;
                 } else if (destGrayValue >= windowedImageGrayscalesCount) {
                     destGrayValue = windowedImageGrayscalesCount - 1;
                 }
                 resultRaster.setSample(x, y, 0, destGrayValue);
                 resultRaster.setSample(x, y, 1, destGrayValue);
                 resultRaster.setSample(x, y, 2, destGrayValue);
             }
         }
         return destImg;
     }
 
 
     /*
     private void getImageMetadata() {
         DicomObject imgMetadata = displayedCell.getDisplayedModelElement().getDicomImageMetaData();
         int bitsAllocated = imgMetadata.getInt(Tag.BitsAllocated);
         if (bitsAllocated <= 0) {
         }
         int bitsStored = imgMetadata.getInt(Tag.BitsStored);
         if (bitsStored <= 0) {
         }
         boolean isSigned = (1 == imgMetadata.getInt(Tag.PixelRepresentation));
     }
      */
 
     private BufferedImage windowRGB(BufferedImage srcImg, float windowLocation, float windowWidth) {
         BufferedImage destImg = new BufferedImage(srcImg.getWidth(), srcImg.getHeight(), BufferedImage.TYPE_INT_RGB);
         final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
         float scale = windowedImageBandValuesCount/windowWidth;
         float offset = (windowWidth/2-windowLocation)*scale;
         if (! srcImg.getColorModel().getColorSpace().isCS_sRGB()) {
             throw new IllegalArgumentException("source image must be RGB");
         }
         Raster srcRaster = srcImg.getRaster();
         if (srcRaster.getNumBands() != 3) {
             throw new IllegalArgumentException("source image must be RGB");
         }
         WritableRaster resultRaster = destImg.getRaster();
         for (int x = 0; x < srcImg.getWidth(); x++) {
             for (int y = 0; y < srcImg.getHeight(); y++) {
                 for (int band = 0; band < 3; band++) {
                     int srcGrayValue = srcRaster.getSample(x, y, band);
                     float destGrayValue = scale * srcGrayValue + offset;
                     // clamp
                     if (destGrayValue < 0) {
                         destGrayValue = 0;
                     } else if (destGrayValue >= windowedImageBandValuesCount) {
                         destGrayValue = windowedImageBandValuesCount - 1;
                     }
                     resultRaster.setSample(x, y, band, destGrayValue);
                 }
             }
         }
         return destImg;
     }
 
     private BufferedImage windowWithRasterOp(BufferedImage srcImg, float windowLocation, float windowWidth) {
         //final int windowedImageBandValuesCount = 256;  // for BufferedImage.TYPE_INT_RGB
         //final float windowedImageBandValuesCount = 1.0F;
         //final float windowedImageBandValuesCount = 65535F;
         //final float windowedImageBandValuesCount = 4095F;
         final float windowedImageBandValuesCount = (1 << srcImg.getColorModel().getComponentSize(0)) - 1;
         float scale = windowedImageBandValuesCount/windowWidth;
         float offset = (windowWidth/2-windowLocation)*scale;
         RescaleOp rescaleOp = new RescaleOp(scale, offset, null);
         return rescaleOp.filter(srcImg, null);
     }
 }
