 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui;
 
 import org.jdesktop.fuse.InjectedResource;
 import org.jdesktop.fuse.ResourceInjector;
 import org.jdesktop.swingx.graphics.GraphicsUtilities;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.Dimension;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Stroke;
 import java.awt.image.BufferedImage;
 
import java.util.Properties;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 
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
 public class PureCoolPanel extends PainterCoolPanel {
 
     //~ Static fields/initializers ---------------------------------------------
 
     // Lumbermill Logger initialisieren
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PureCoolPanel.class);
     private static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
     private static final Stroke STROKE = new BasicStroke(1.0f);
 
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
     private JPanel spinner;
     private JComponent panMap;
     private JComponent panContent;
 //    private boolean usePainterCoolPanel = false;
     private ImageIcon icons;
     private BufferedImage cacheImage;
     private BufferedImage gradientImage;
     private JComponent panTitle;
     private JComponent panInter;
     private final Composite composite;
     private final ShadowRenderer shadowRenderer;
     private Dimension lastPaintSize;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Kontruktor des CoolPanels. Erzeugt ein CoolPanel, damit es in einem Renderer verwendet werden kann.
      */
     public PureCoolPanel() {
         // FUSE initialisieren
         FuseLoader.load();
 
         // Ressourcen hierarchisch rekursiv nach oben einfuegen
        ResourceInjector.get("purecoolpanel.style").inject(true, this); // NOI18N
 
 //
         gradientColorTop = javax.swing.UIManager.getDefaults().getColor("Button.shadow");        // NOI18N
         gradientColorBottom = javax.swing.UIManager.getDefaults().getColor("Button.background"); // NOI18N
         composite = AlphaComposite.SrcAtop.derive(titleLinesOpacity);
         shadowRenderer = new ShadowRenderer(shadowLength, shadowIntensity, shadowColor);
         cacheImage = null;
         gradientImage = null;
         lastPaintSize = getSize();
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     protected void paintComponent(final Graphics g) {
         if (usePainterCoolPanel) {
             super.paintComponent(g);
         } else {
             // alter PureCoolPanel paint code....
             final Graphics2D g2d = (Graphics2D)g;
             if ((cacheImage == null) || !getSize().equals(lastPaintSize)) {
                 lastPaintSize = getSize();
                 // Image zum Zeichnen erstellen von dem wird spaeter der Schlagschatten erstellt wird
                 final BufferedImage box = new BufferedImage(getWidth() - offset, getHeight() - offset, IMAGE_TYPE);
 
                 // Graphics-Objekt der Box erzeugen
                 final Graphics2D boxGraphics = box.createGraphics();
 
                 // Standard-Zeichenmodus speichern
                 final Composite originalComposite = boxGraphics.getComposite();
 
                 if ((gradientImage == null) || (gradientImage.getHeight() != box.getHeight())) {
                     gradientImage = GraphicsUtilities.createCompatibleImage(1, box.getHeight());
                     final Graphics2D gradientGraphics2d = gradientImage.createGraphics();
                     gradientGraphics2d.setPaint(new GradientPaint(
                             0,
                             0,
                             gradientColorTop,
                             0,
                             box.getHeight(),
                             gradientColorBottom));
                     gradientGraphics2d.fillRect(0, 0, 1, box.getHeight());
                 }
 
                 // RoundedRectangle zeichnen und mit Gradient fuellen
                 boxGraphics.setColor(Color.BLACK);
                 boxGraphics.fillRoundRect(offset, 0, box.getWidth() - offset, box.getHeight(), arcSize, arcSize);
                 boxGraphics.setComposite(AlphaComposite.SrcAtop);
                 boxGraphics.drawImage(gradientImage, 0, 0, box.getWidth(), box.getHeight(), null);
                 boxGraphics.setComposite(originalComposite);
                 boxGraphics.setStroke(STROKE);
 
                 // Falls TitlePanel existiert, speziell zeichnen
                 if (getPanTitle() != null) {
                     final Rectangle bounds = getPanTitle().getBounds();
                     boxGraphics.setComposite(AlphaComposite.SrcAtop.derive(titlePanelOpacity));
                     boxGraphics.setColor(colorTitle);
                     boxGraphics.fillRect(0, bounds.y, bounds.width + offset, bounds.height);
                     boxGraphics.setComposite(composite);
                     boxGraphics.setColor(colorDarkLine);
                     boxGraphics.drawLine(offset, bounds.height - 1, bounds.width + (3 * offset), bounds.height - 1);
                     boxGraphics.setColor(colorBrightLine);
                     boxGraphics.drawLine(offset, bounds.height, bounds.width + (3 * offset), bounds.height);
                 }
 
                 // Falls InteractionPanel existiert, speziell zeichnen
                 if (getPanInter() != null) {
                     final Rectangle bounds = getPanInter().getBounds();
                     boxGraphics.setComposite(AlphaComposite.SrcAtop.derive(interPanelOpacity));
                     boxGraphics.setPaint(new GradientPaint(
                             0,
                             bounds.y,
                             new Color(0, 0, 0, 160),
                             0,
                             bounds.y
                                     + bounds.height,
                             Color.BLACK));
                     boxGraphics.fillRect(0, bounds.y, bounds.width + offset, bounds.height);
                     boxGraphics.setComposite(AlphaComposite.SrcAtop.derive(interLinesOpacity));
                     boxGraphics.setColor(colorDarkLine);
                     boxGraphics.drawLine(offset, bounds.y - 1, bounds.width + (3 * offset), bounds.y - 1);
                     boxGraphics.setColor(colorBrightLine);
                     boxGraphics.drawLine(offset, bounds.y, bounds.width + (3 * offset), bounds.y);
                 }
 
                 // Rahmen des RoundRectangel in der Box nachzeichnen
                 boxGraphics.setComposite(AlphaComposite.SrcOver.derive(0.7f));
                 boxGraphics.setColor(colorBorder);
                 boxGraphics.drawRoundRect(
                     offset,
                     0,
                     box.getWidth()
                             - (offset + 1),
                     box.getHeight()
                             - 1,
                     arcSize,
                     arcSize);
 
                 // Weissen oberen Rand zeichnen
                 final BufferedImage glossy = new BufferedImage(box.getWidth(), box.getHeight(), IMAGE_TYPE);
                 final Graphics2D glossyGraphics2D = glossy.createGraphics();
 //            glossyGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                 glossyGraphics2D.setStroke(STROKE);
 
                 // Glossy-Effekt oben
                 glossyGraphics2D.setColor(colorGlossy);
 //            if (noTitlePanel) {
                 glossyGraphics2D.drawRoundRect(offset + 1,
                     1,
                     box.getWidth()
                             - (offset + 3),
                     2
                             * arcSize,
                     arcSize,
                     arcSize);
                 glossyGraphics2D.setComposite(AlphaComposite.DstIn);
                 glossyGraphics2D.setPaint(new GradientPaint(
                         0,
                         0,
                         new Color(255, 255, 255, 255),
                         0,
                         arcSize
                                 / 2,
                         new Color(255, 255, 255, 0)));
                 glossyGraphics2D.fillRect(0, 0, box.getWidth(), arcSize);
                 glossyGraphics2D.setPaint(new Color(255, 255, 255, 0));
                 glossyGraphics2D.fillRect(0, arcSize / 2, box.getWidth(), 2 * arcSize);
 //            } else {
 //                gg.fillRoundRect(offset+2,2, box.getWidth()-(offset+4), getPanTitle().getHeight(),arcSize-2,arcSize-2);
 //                gg.setComposite(AlphaComposite.DstIn);
 //                gg.setPaint(new GradientPaint(0,0,new Color(255,255,255,255), 0, getPanTitle().getHeight()/2, new Color(255,255,255,0)));
 //                gg.fillRect(0,0,box.getWidth(), getPanTitle().getHeight());
 //                gg.setPaint(new Color(255,255,255,0));
 //                gg.fillRect(0,getPanTitle().getHeight()/2,box.getWidth(), getPanTitle().getHeight());
 //            }
 
                 // Drop Shadow rendern
                 final BufferedImage shadow = shadowRenderer.createShadow(box);
 
                 // Fertige Box und ihren Schatten zeichnen.
                 cacheImage = new BufferedImage(shadow.getWidth(), shadow.getHeight(), IMAGE_TYPE);
                 final Graphics2D resultGraphics2D = cacheImage.createGraphics();
                 resultGraphics2D.drawImage(shadow, 0, 0, null);
                 resultGraphics2D.drawImage(box, 0, 0, null);
                 resultGraphics2D.setComposite(AlphaComposite.SrcOver.derive(glossyOpacity));
                 resultGraphics2D.drawImage(glossy, 0, 0, null);
                 resultGraphics2D.setComposite(originalComposite);
                 if (this.icons != null) {
 //                resultGraphics2D.drawImage(this.icons.getImage(), box.getWidth() - this.icons.getIconWidth() - offsetRight, offsetTop, null);
                     resultGraphics2D.drawImage(this.icons.getImage(),
                         box.getWidth()
                                 - this.icons.getIconWidth()
                                 - offsetRight,
                         (panTitle.getHeight() / 2)
                                 + 3
                                 - (this.icons.getIconHeight() / 2),
                         null);
                 }
                 boxGraphics.dispose();
                 glossyGraphics2D.dispose();
                 resultGraphics2D.dispose();
                 box.flush();
                 glossy.flush();
                 shadow.flush();
             }
 
             // Entgueltiges Bild in Panel zeichnen
             g2d.drawImage(cacheImage, 0, 0, null);
         }
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
     public JComponent getPanContent() {
         return panContent;
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  panContent  DOCUMENT ME!
      */
     public void setPanContent(final JComponent panContent) {
         this.panContent = panContent;
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
      * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
      * content of this method is always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
         setPreferredSize(new java.awt.Dimension(200, 200));
 
         final javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 400, Short.MAX_VALUE));
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 300, Short.MAX_VALUE));
     } // </editor-fold>//GEN-END:initComponents
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
