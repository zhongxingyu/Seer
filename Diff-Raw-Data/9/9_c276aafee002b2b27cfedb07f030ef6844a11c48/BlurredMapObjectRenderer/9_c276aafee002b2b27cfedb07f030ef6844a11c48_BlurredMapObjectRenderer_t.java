 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.tools.metaobjectrenderer;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.jdesktop.fuse.InjectedResource;
 import org.jdesktop.fuse.ResourceInjector;
 import org.jdesktop.swingx.graphics.GraphicsUtilities;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 import org.jdesktop.swingx.image.StackBlurFilter;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.input.SAXBuilder;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.image.BufferedImage;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
 import de.cismet.cismap.commons.BoundingBox;
 import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
 import de.cismet.cismap.commons.retrieval.RetrievalEvent;
 import de.cismet.cismap.commons.retrieval.RetrievalListener;
 
 import de.cismet.tools.CismetThreadPool;
 
 import de.cismet.tools.gui.FuseLoader;
 import de.cismet.tools.gui.WrappedComponent;
 
 /**
  * Panel zur "ultracoolen" Darstellung von Klassinformationen in Renderern. Das Panel zeichnet ein abgerundetes Viereck
  * mit metallischer Oberflaeche auf dem die Objektbeschreibung dargestellt wird. Zusaetzlich erzeugt das Panel einen
  * Milchglas-Effekt, indem es eine Karte in den Hintergrund zeichnet und weichzeinet. Damit die Karte dennoch gut
  * sichtbar ist, kann durch hinzufuegen eines weiteren Panels (mit Contraint BorderLayout.CENTER) ein "Loch" in den
  * Milchglas-Effekt gezeichnet werden, worin die Karte klar sichtbar ist.
  *
  * @author   srichter
  * @author   nhaffke
  * @version  $Revision$, $Date$
  */
 public class BlurredMapObjectRenderer extends CustomMetaObjectRenderer implements ComponentListener, WrappedComponent {
 
     //~ Static fields/initializers ---------------------------------------------
 
 // private static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
 
     private static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
     private static final Dimension MAX_SIZE = new Dimension(2048, 1024);
 
     //~ Instance fields --------------------------------------------------------
 
     @InjectedResource
     public int offset;
     @InjectedResource
     public int offsetRight;
     @InjectedResource
     public int offsetTop;
     @InjectedResource
     public int offsetBetween;
     @InjectedResource
     public int arcSize;
     @InjectedResource
     public int shadowLength;
     @InjectedResource
     public int blurFactor;
     @InjectedResource
     public float borderWidth;
     @InjectedResource
     public float shadowIntensity;
     @InjectedResource
     public float titlePanelOpacity;
     @InjectedResource
     public float titleLinesOpacity;
     @InjectedResource
     public float interPanelOpacity;
     @InjectedResource
     public float interLinesOpacity;
     @InjectedResource
     public float blurredMapOpacity;
     @InjectedResource
     public float cutOutMapOpacity;
     @InjectedResource
     public float glossyOpacity;
     @InjectedResource
     public Color shadowColor;
     @InjectedResource
     public Color colorBorder;
     @InjectedResource
     public Color colorMapBorder;
     @InjectedResource
     public Color colorTitle;
     @InjectedResource
     public Color colorInter;
     @InjectedResource
     public Color colorDarkLine;
     @InjectedResource
     public Color colorBrightLine;
     @InjectedResource
     public Color colorGlossy;
     @InjectedResource
     public Color gradientColorTop;
     @InjectedResource
     public Color gradientColorBottom;
     @InjectedResource
     public boolean usePainterCoolPanel;
 
     // Lumbermill Logger initialisieren
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     private boolean noTitlePanel;
     private boolean mustBlur;
     private double geoBuffer;
     private int lastX;
     private int lastWidth;
     private int panelWidth;
     private ImageIcon icons;
     private Image map;
     private BufferedImage cacheImage;
     private BufferedImage gradientImage;
     private BufferedImage blurredMap;
     private BufferedImage cachedBlurredMap;
     private BufferedImage orgMap;
     private Geometry geometry;
     private SimpleWMS swms;
     private JPanel spinner;
     private JComponent panTitle;
     private JComponent panMap;
     private JComponent panInter;
     private JComponent panContent;
     private Rectangle mapBounds;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Kontruktor des CoolPanels. Erzeugt ein BlurredMapObjectRenderer, damit es in einem Renderer verwendet werden
      * kann.
      */
     public BlurredMapObjectRenderer() {
         // FUSE initialisieren
         FuseLoader.load();
         // Ressourcen hierarchisch rekursiv nach oben einfuegen
        ResourceInjector.get("blurredmapobjectrenderer.style").inject(true, new Object[] {getInstance()});       // NOI18N
         gradientColorTop = javax.swing.UIManager.getDefaults().getColor("Button.shadow");                        // NOI18N
         gradientColorBottom = javax.swing.UIManager.getDefaults().getColor("Button.background");                 // NOI18N
         mapBounds = null;
         cacheImage = null;
         noTitlePanel = true;
         mustBlur = true;
         geoBuffer = 40d;
         lastX = 0;
         lastWidth = 0;
         panelWidth = 0;
         map = null;
         blurredMap = null;
         orgMap = null;
         geometry = null;
         gradientImage = null;
         try {
             final SAXBuilder builder = new SAXBuilder(false);
             final Document doc = builder.build(getClass().getResource("/coolobjectrenderer/backgroundWMS.xml")); // NOI18N
             final Element prefs = doc.getRootElement();
 
             swms = new SimpleWMS(prefs);
             swms.addRetrievalListener(new RetrievalListener() {
 
                     @Override
                     public void retrievalAborted(final RetrievalEvent retrievalEvent) {
                     }
 
                     @Override
                     public void retrievalComplete(final RetrievalEvent retrievalEvent) {
                         final Object o = retrievalEvent.getRetrievedObject();
                         if (o instanceof Image) {
                             map = (Image)o;
                             final BufferedImage erg = new BufferedImage(
                                     map.getWidth(null),
                                     map.getHeight(null),
                                     IMAGE_TYPE);
                             final Graphics2D g = erg.createGraphics();
                             g.drawImage(map, null, null);
                             g.dispose();
                             cacheImage = null;
                             lastWidth = getWidth();
                             lastX = 0;
                             mustBlur = true;
                             if (getSpinner() != null) {
                                 getSpinner().setVisible(false);
                             }
                             createBackground(erg);
                             if (log.isDebugEnabled()) {
                                 log.debug("MapRetrieval completed"); // NOI18N
                             }
                         } else {
                             if (getSpinner() != null) {
                                 getSpinner().setVisible(false);
                             }
 
                             log.warn("no image"); // NOI18N
                         }
                     }
 
                     @Override
                     public void retrievalError(final RetrievalEvent retrievalEvent) {
                         if (getSpinner() != null) {
                             getSpinner().setVisible(false);
                         }
                     }
 
                     @Override
                     public void retrievalProgress(final RetrievalEvent retrievalEvent) {
                     }
 
                     @Override
                     public void retrievalStarted(final RetrievalEvent retrievalEvent) {
                         if (log.isDebugEnabled()) {
                             log.debug("retrievalStarted"); // NOI18N
                         }
                     }
                 });
         } catch (Exception e) {
             log.error("Error while loading the map info", e); // NOI18N
             if (getSpinner() != null) {
                 getSpinner().setVisible(false);
             }
         }
         addComponentListener(this);
     }
 
     /**
      * Kontruktor des CoolPanels mit einer Puffergroesse. Diese wird dazu verwendet, um um Punktgeometrien eine
      * BoundingBox der Groesse dieses Puffers zu erstellen.
      *
      * @param  geoBuffer  Puffergroesse um das Geometrieobjekt herum
      */
     public BlurredMapObjectRenderer(final double geoBuffer) {
         this();
         this.geoBuffer = geoBuffer;
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public Dimension getMaximumSize() {
         return MAX_SIZE;
     }
 
     /**
      * Setzt das Geometry-Objekt des momentan im Konfigurator angewaehlten Objekts. Wird benoetigt, um einen
      * Kartenhintergrund zu zeichnen.
      *
      * @param  geometry  Geometry-Objekt
      */
     public void setGeometry(final Geometry geometry) {
         this.geometry = geometry;
     }
 
     @Override
     public void assignSingle() {
     }
 
     @Override
     public void assignAggregation() {
     }
 
     @Override
     public double getWidthRatio() {
         return 1.0d;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getPanTitle() {
         return panTitle;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panTitle  DOCUMENT ME!
      */
     public void setPanTitle(final JComponent panTitle) {
         this.panTitle = panTitle;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getPanMap() {
         return panMap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panMap  DOCUMENT ME!
      */
     public void setPanMap(final JComponent panMap) {
         this.panMap = panMap;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getPanInter() {
         return panInter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panInter  DOCUMENT ME!
      */
     public void setPanInter(final JComponent panInter) {
         this.panInter = panInter;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JComponent getPanContent() {
         return panContent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panContent  DOCUMENT ME!
      */
     public final void setPanContent(final JComponent panContent) {
         this.panContent = panContent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public BlurredMapObjectRenderer getInstance() {
         return this;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public JPanel getSpinner() {
         return spinner;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  spinner  DOCUMENT ME!
      */
     public void setSpinner(final JPanel spinner) {
         this.spinner = spinner;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     public Geometry getGeometry() {
         return geometry;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  icon  DOCUMENT ME!
      */
     public void setImageRechtsOben(final ImageIcon icon) {
         try {
 //            ReflectionRenderer renderer2 = new ReflectionRenderer(0.5f,0.4f,true);
             final ShadowRenderer renderer = new ShadowRenderer(shadowLength, 0.5f, Color.BLACK);
 
             final BufferedImage temp = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                     IMAGE_TYPE);
             final Graphics tg = temp.createGraphics();
             tg.drawImage(icon.getImage(), 0, 0, null);
             tg.dispose();
 
             final BufferedImage shadow = renderer.createShadow(temp);
 
             final BufferedImage result = new BufferedImage(icon.getIconWidth() + (2 * shadowLength),
                     icon.getIconHeight()
                             + (2 * shadowLength),
                     IMAGE_TYPE);
             final Graphics rg = result.createGraphics();
             rg.drawImage(shadow, 0, 0, null);
             rg.drawImage(temp, 0, 0, null);
             rg.dispose();
             shadow.flush();
 
 //            BufferedImage ref = renderer.appendReflection(tmp);
             final ImageIcon ic = new ImageIcon(result);
             this.icons = ic;
         } catch (Exception e) {
             this.icons = null;
         }
     }
 
     /**
      * ueberschreibt die Standard-Zeichenmethode eines JPanels. Zeichnet die "coolen" Effekte des CoolPanels.
      *
      * @param  g  Graphics-Objekt auf das gezeichnet wird
      */
     @Override
     protected void paintComponent(final Graphics g) {
 //        log.info("CoolPanel: paintComponent()");
         super.paintComponent(g);
         final Graphics2D g2d = (Graphics2D)g;
 
         if (cacheImage == null) {
             // Image zum Zeichnen erstellen von dem wird spaeter der Schlagschatten erstellt wird
             final BufferedImage box = new BufferedImage(getWidth() - offset, getHeight() - offset, IMAGE_TYPE);
 
             // Graphics-Objekt der Box erzeugen
             final Graphics2D bg = box.createGraphics();
 
             // Standard-Zeichenmodus speichern
             final Composite orig = bg.getComposite();
 
             if ((gradientImage == null) || (gradientImage.getHeight() != box.getHeight())) {
                 gradientImage = GraphicsUtilities.createCompatibleImage(1, box.getHeight());
                 final Graphics2D grad2d = gradientImage.createGraphics();
                 grad2d.setPaint(new GradientPaint(0, 0, gradientColorTop, 0, box.getHeight(), gradientColorBottom));
                 grad2d.fillRect(0, 0, 1, box.getHeight());
             }
 
             // RoundedRectangle zeichnen und mit Gradient fuellen
             bg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             bg.setColor(Color.BLACK);
             bg.fillRoundRect(offset, 0, box.getWidth() - offset, box.getHeight(), arcSize, arcSize);
             bg.setComposite(AlphaComposite.SrcAtop);
             bg.drawImage(gradientImage, 0, 0, box.getWidth(), box.getHeight(), null);
             bg.setComposite(orig);
             bg.setStroke(new BasicStroke(1.0f));
 
             // Falls TitlePanel existiert, speziell zeichnen
             if (getPanTitle() != null) {
                 noTitlePanel = false;
                 final Rectangle bounds = getPanTitle().getBounds();
                 bg.setComposite(AlphaComposite.SrcAtop.derive(titlePanelOpacity));
                 bg.setColor(colorTitle);
                 bg.fillRect(0, bounds.y, bounds.width + offset, bounds.height);
                 bg.setComposite(AlphaComposite.SrcAtop.derive(titleLinesOpacity));
                 bg.setColor(colorDarkLine);
                 bg.drawLine(offset, bounds.height - 1, bounds.width + (3 * offset), bounds.height - 1);
                 bg.setColor(colorBrightLine);
                 bg.drawLine(offset, bounds.height, bounds.width + (3 * offset), bounds.height);
             }
 
             // Falls InteractionPanel existiert, speziell zeichnen
             if (getPanInter() != null) {
                 final Rectangle bounds = getPanInter().getBounds();
                 bg.setComposite(AlphaComposite.SrcAtop.derive(interPanelOpacity));
 //            bg.setColor(colInter);
                 bg.setPaint(new GradientPaint(
                         0,
                         bounds.y,
                         new Color(0, 0, 0, 160),
                         0,
                         bounds.y
                                 + bounds.height,
                         Color.BLACK));
                 bg.fillRect(0, bounds.y, bounds.width + offset, bounds.height);
                 bg.setComposite(AlphaComposite.SrcAtop.derive(interLinesOpacity));
                 bg.setColor(colorDarkLine);
                 bg.drawLine(offset, bounds.y - 1, bounds.width + (3 * offset), bounds.y - 1);
                 bg.setColor(colorBrightLine);
                 bg.drawLine(offset, bounds.y, bounds.width + (3 * offset), bounds.y);
             }
 
             // Karte zeichnen
             if (getBlurredMap() != null) {
                 bg.setComposite(AlphaComposite.SrcAtop.derive(blurredMapOpacity));
                 bg.drawImage(getBlurredMap(), 0, 0, null);
                 bg.setComposite(orig);
 
                 // "Fenster zum Hof" ausschneiden und zeichnen, falls panMap gesetzt wurde
                 if (getPanMap() != null) {
                     final Rectangle b = getPanMap().getBounds();
 
                     // Karte in Ausschnitt zeichnen
                     if (getMap() != null) {
                         log.info("CoolPanel: draw small map"); // NOI18N
                         bg.setColor(colorMapBorder);
                         bg.setComposite(AlphaComposite.Clear);
                         if (b.width < getMap().getWidth()) {
                             bg.fillRect(b.x, b.y + (2 * offset), b.width - (3 * offset), b.height - (4 * offset));
                             bg.setComposite(orig);
                             bg.drawRect(b.x, b.y + (2 * offset), b.width - (3 * offset), b.height - (4 * offset));
                         } else {
                             bg.fillRect(b.x, b.y + (2 * offset), getMap().getWidth(), b.height - (4 * offset));
                             bg.setComposite(orig);
                             bg.drawRect(b.x, b.y + (2 * offset), getMap().getWidth(), b.height - (4 * offset));
                         }
                         // Fensterausschnitt zeichnen
                         bg.setComposite(AlphaComposite.DstOver.derive(cutOutMapOpacity));
                         bg.drawImage(getMap(), b.x, b.y + (2 * offset), null);
                     }
                 }
             } // Ende Karte zeichnen
 
             // Rahmen des RoundRectangel in der Box nachzeichnen
             bg.setComposite(AlphaComposite.SrcOver.derive(0.7f));
             bg.setColor(colorBorder);
             bg.drawRoundRect(offset, 0, box.getWidth() - (offset + 1), box.getHeight() - 1, arcSize, arcSize);
 
             // Weissen oberen Rand zeichnen
             final BufferedImage glossy = new BufferedImage(box.getWidth(), box.getHeight(), IMAGE_TYPE);
             final Graphics2D gg = glossy.createGraphics();
             gg.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             gg.setStroke(new BasicStroke(1.0f));
 
             // Glossy-Effekt oben
             gg.setColor(colorGlossy);
 //            if (noTitlePanel) {
             gg.drawRoundRect(offset + 1, 1, box.getWidth() - (offset + 3), 2 * arcSize, arcSize, arcSize);
             gg.setComposite(AlphaComposite.DstIn);
             gg.setPaint(new GradientPaint(
                     0,
                     0,
                     new Color(255, 255, 255, 255),
                     0,
                     arcSize
                             / 2,
                     new Color(255, 255, 255, 0)));
             gg.fillRect(0, 0, box.getWidth(), arcSize);
             gg.setPaint(new Color(255, 255, 255, 0));
             gg.fillRect(0, arcSize / 2, box.getWidth(), 2 * arcSize);
 //            } else {
 //                gg.fillRoundRect(offset+2,2, box.getWidth()-(offset+4), getPanTitle().getHeight(),arcSize-2,arcSize-2);
 //                gg.setComposite(AlphaComposite.DstIn);
 //                gg.setPaint(new GradientPaint(0,0,new Color(255,255,255,255), 0, getPanTitle().getHeight()/2, new Color(255,255,255,0)));
 //                gg.fillRect(0,0,box.getWidth(), getPanTitle().getHeight());
 //                gg.setPaint(new Color(255,255,255,0));
 //                gg.fillRect(0,getPanTitle().getHeight()/2,box.getWidth(), getPanTitle().getHeight());
 //            }
 
             // Drop Shadow rendern
             final ShadowRenderer renderer = new ShadowRenderer(shadowLength, shadowIntensity, shadowColor);
             final BufferedImage shadow = renderer.createShadow(box);
 
             // Fertige Box und ihren Schatten zeichnen.
             cacheImage = new BufferedImage(shadow.getWidth(), shadow.getHeight(), IMAGE_TYPE);
             final Graphics2D cg = cacheImage.createGraphics();
             cg.drawImage(shadow, 0, 0, null);
             cg.drawImage(box, 0, 0, null);
             cg.setComposite(AlphaComposite.SrcOver.derive(glossyOpacity));
             cg.drawImage(glossy, 0, 0, null);
             cg.setComposite(orig);
             if (this.icons != null) {
                 cg.drawImage(this.icons.getImage(),
                     box.getWidth()
                             - this.icons.getIconWidth()
                             - offsetRight,
                     offsetTop,
                     null);
             }
             bg.dispose();
             gg.dispose();
             cg.dispose();
             box.flush();
             glossy.flush();
             shadow.flush();
         }
 
         // Entgueltiges Bild in Panel zeichnen
         g2d.drawImage(cacheImage, 0, 0, null);
     }
 
     /**
      * Liefert den weichgezeichneten Kartenausschnitt zurueck.
      *
      * @return  der bereits weichgezeichnete Kartenausschnitt
      */
     private BufferedImage getBlurredMap() {
         return blurredMap;
     }
 
     /**
      * Liefert den Kartenausschnitt ohne Weichzeichner zurueck.
      *
      * @return  der "originale" Kartenausschnitt
      */
     private BufferedImage getMap() {
         return orgMap;
     }
 
     /**
      * Setzt den relevanten Kartenausschnitt. Hierbei wird der Ausschnitt automatisch weichgezeichnet. Um diese Karte im
      * CoolPanel zu verwenden sollte er mit der {@link getBlurredMap()}-Methode aufgerufen werden.
      *
      * @param  newMap  der geaenderte zu blurrende Kartenauschnitt
      */
     private void createBackground(final BufferedImage newMap) {
         if (newMap != null) {
             orgMap = newMap;
             // Ausfuehrung des Blurrens im Thread
             final Thread t = new Thread(new Runnable() {
 
                         @Override
                         public void run() {
                             if (mustBlur) {
                                 final StackBlurFilter blur = new StackBlurFilter(6);
                                 cachedBlurredMap = blur.filter(orgMap, null);
                                 blurredMap = new BufferedImage(orgMap.getWidth(), orgMap.getHeight(), IMAGE_TYPE);
                                 final Graphics2D b = blurredMap.createGraphics();
                                 b.drawImage(cachedBlurredMap, 0, 0, null);
                                 b.dispose();
                             } else {
                                 blurredMap.flush();
                                 final Graphics2D b = blurredMap.createGraphics();
                                 b.drawImage(cachedBlurredMap, lastX, 0, null);
                                 b.dispose();
                             }
 
                             if (getPanMap() != null) {
                                 final Rectangle bounds = getPanMap().getBounds();
                                 if ((bounds.width - (3 * offset)) > orgMap.getWidth()) {
                                     orgMap = orgMap.getSubimage(
                                             bounds.x,
                                             bounds.y
                                                     + (2 * offset),
                                             orgMap.getWidth(),
                                             bounds.height);
                                 } else {
                                     orgMap = orgMap.getSubimage(
                                             bounds.x,
                                             bounds.y
                                                     + (2 * offset),
                                             bounds.width
                                                     - (3 * offset),
                                             bounds.height);
                                 }
                             }
                             // CoolPanel neu zeichnen, sobald die geblurrte Karte fertig erstellt wurde
                             EventQueue.invokeLater(new Runnable() {
 
                                     @Override
                                     public void run() {
                                         cacheImage = null;
                                         repaint();
                                     }
                                 });
                         }
                     });
             CismetThreadPool.execute(t);
         }
     }
 
     /**
      * Interne Methode die den WMS-Server anstoesst eine neue Karte zu liefern.
      */
     private void mapIt() {
         if (log.isDebugEnabled()) {
             log.debug("MAPIT"); // NOI18N
         }
         try {
             if ((getSpinner() != null) && !getSpinner().isVisible()) {
                 getSpinner().setVisible(true);
             }
 
             if (geometry != null) {
                 mapBounds = getBounds();
                 // Neue BoundingBox 40 Einheiten um die Geometrie herum erzeugen
                 BoundingBox bb = new BoundingBox(geometry.buffer(geoBuffer));
 
                 // Panelgroessen speichern
                 this.panelWidth = getWidth();
                 if (getPanMap() != null) {
                     final double panWith = new Integer(getWidth() - (3 * offset)).doubleValue();
                     final double panHeight = new Integer(getHeight() - (4 * offset)).doubleValue();
                     final double cutOutWidth = new Integer(getPanMap().getWidth() - offset).doubleValue();
                     final double cutOutHeight = new Integer(getPanMap().getHeight() - offset).doubleValue();
 
                     // Mittelpunkt der BoundingBox bestimmen
                     final double midX = bb.getX1() + ((bb.getX2() - bb.getX1()) / 2);
                     final double midY = bb.getY1() + ((bb.getY2() - bb.getY1()) / 2);
 
                     // Groesse des Kartenausschnitts in WMS-Einheiten speichern
                     double worldWidth = bb.getWidth();
                     double worldHeight = bb.getHeight();
 
                     // Verhaeltnis Breite/Hoehe berechnen
                     final double widthToHeightRatio = cutOutWidth / cutOutHeight;
 
                     // Testen, wie die RealWorld-Groessen angepasst werden muessen
                     if ((widthToHeightRatio / (worldWidth / worldHeight)) > 1) {
                         // Breite der Hoehe anpassen
                         worldWidth = worldHeight * widthToHeightRatio;
                     } else {
                         // Hoehe der Breite anpassen
                         worldHeight = worldWidth * widthToHeightRatio;
                     }
 
                     // Pixel-WMS-Einheit-Verhaeltnis bezeichnen
                     final double widthValuePerPixel = worldWidth / cutOutWidth;
                     final double heightValuePerPixel = worldHeight / cutOutHeight;
 
                     // Versatz berechnen
                     final Rectangle b = getPanMap().getBounds();
                     final double offTop = b.getY() * heightValuePerPixel;
                     final double offBottom = (panHeight - (b.getY() + b.height)) * heightValuePerPixel;
                     final double offLeft = b.getX() * widthValuePerPixel;
                     final double offRight = (panWith - (b.getX() + b.width - (3 * offset))) * widthValuePerPixel;
 
                     // BoundingBox mit neuer Groesse erstellen
                     bb = new BoundingBox((midX - ((worldWidth / 2) + offLeft)),
                             (midY - ((worldHeight / 2) + offTop)),
                             (midX + (worldWidth / 2) + offRight),
                             (midY + (worldHeight / 2) + offBottom));
                 } else {
                     final double midX = bb.getX1() + ((bb.getX2() - bb.getX1()) / 2);
                     final double midY = bb.getY1() + ((bb.getY2() - bb.getY1()) / 2);
                     double realWorldWidth = bb.getWidth();
                     double realWorldHeight = bb.getHeight();
                     final double widthToHeightRatio = getWidth() / getHeight();
 
                     if ((widthToHeightRatio / (realWorldWidth / realWorldHeight)) > 1) {
                         // height is bestimmer ;-)
                         realWorldWidth = realWorldHeight * widthToHeightRatio;
                     } else {
                         realWorldHeight = realWorldWidth * widthToHeightRatio;
                     }
 
                     bb = new BoundingBox(midX - (realWorldWidth / 2),
                             midY
                                     - (realWorldHeight / 2),
                             midX
                                     + (realWorldWidth / 2),
                             midY
                                     + (realWorldHeight / 2));
                 }
 
                 // Karte von WMS-Server holen
                 swms.setBoundingBox(bb);
                 swms.setSize(getHeight(), getWidth());
                 swms.retrieve(true);
             } else {
                 if (log.isDebugEnabled()) {
                     log.debug("No geometry object available."); // NOI18N
                 }
                 if (getSpinner() != null) {
                     getSpinner().setVisible(false);
                 }
                 repaint();
             }
         } catch (Exception e) {
             log.warn("Error while displaying the map.", e);     // NOI18N
             if (getSpinner() != null) {
                 getSpinner().setVisible(false);
             }
         }
     }
 
     @Override
     public void componentResized(final ComponentEvent e) {
         cacheImage = null;
         if ((getMap() == null) || (mapBounds.height < getHeight()) || (mapBounds.width < getWidth())) {
             map = null;
             blurredMap = null;
             orgMap = null;
             repaint();
             mapIt();
         } else {
             // Test, ob Groesse zum ersten Mal geaendert wird
             if (lastWidth == 0) {
                 lastWidth = panelWidth;
             }
 
             final int width = getWidth();
 
             // Test, ob vergroessert oder verkleinert wird
             if (width > lastWidth) {
                 lastX += (width - lastWidth) / 2;
             } else {
                 lastX -= (lastWidth - width) / 2;
             }
 
             // letzte Panelbreite speichern fuer naechsten Aufruf
             lastWidth = width;
 
             // neues Hintergrundbild erstellen und zeichnen
             final BufferedImage erg = new BufferedImage(getWidth(), getHeight(), IMAGE_TYPE);
             final Graphics2D g = erg.createGraphics();
             g.drawImage(map, lastX, 0, null);
             g.dispose();
 
             // ChachedBlurImage verwenden
             mustBlur = false;
             createBackground(erg);
         }
     }
 
     @Override
     public void componentMoved(final ComponentEvent e) {
     }
 
     @Override
     public void componentShown(final ComponentEvent e) {
         mapIt();
     }
 
     @Override
     public void componentHidden(final ComponentEvent e) {
     }
 
     /**
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
     } // </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 
     @Override
     public JComponent getOriginalComponent() {
         return this;
     }
 }
