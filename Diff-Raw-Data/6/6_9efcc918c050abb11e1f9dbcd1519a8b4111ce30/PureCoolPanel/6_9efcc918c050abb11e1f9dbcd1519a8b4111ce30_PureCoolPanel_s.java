 package de.cismet.tools.gui;
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Composite;
 import java.awt.EventQueue;
 import java.awt.GradientPaint;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Stroke;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.image.BufferedImage;
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import org.jdesktop.fuse.InjectedResource;
 import org.jdesktop.fuse.ResourceInjector;
 import org.jdesktop.swingx.graphics.GraphicsUtilities;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 
 /**
  * Panel zur "ultracoolen" Darstellung von Klassinformationen in Renderern.
  * Das Panel zeichnet ein abgerundetes Viereck mit metallischer Oberflaeche
  * auf dem die Objektbeschreibung dargestellt wird. Zusaetzlich erzeugt das
  * Panel einen Milchglas-Effekt, indem es eine Karte in den Hintergrund zeichnet
  * und weichzeinet. Damit die Karte dennoch gut sichtbar ist, kann durch
  * hinzufuegen eines weiteren Panels (mit Contraint BorderLayout.CENTER) ein "Loch"
  * in den Milchglas-Effekt gezeichnet werden, worin die Karte klar sichtbar ist.
  *
  * @author srichter
  * @author nhaffke
  */
 public class PureCoolPanel extends JPanel implements ComponentListener {
 
     // Lumbermill Logger initialisieren
     private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
     @InjectedResource()
     public int offset, offsetRight, offsetTop, offsetBetween, arcSize, shadowLength, blurFactor;
     @InjectedResource()
     public float borderWidth, shadowIntensity, titlePanelOpacity, titleLinesOpacity, interPanelOpacity, interLinesOpacity, blurredMapOpacity, cutOutMapOpacity, glossyOpacity;
     @InjectedResource()
     public Color shadowColor, colorBorder, colorMapBorder, colorTitle, colorInter, colorDarkLine, colorBrightLine, colorGlossy, gradientColorTop, gradientColorBottom;
     private static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
     private boolean noTitlePanel, mustBlur;
     private double geoBuffer;
     private int lastX, lastWidth, panelWidth;
     private ImageIcon icons;
     private Image map;
     private BufferedImage cacheImage, gradientImage, blurredMap, cachedBlurredMap, orgMap;
     private JPanel spinner;
     private JComponent panTitle, panMap, panInter, panContent;
     private Rectangle mapBounds;
     private final Composite composite;
     private final ShadowRenderer shadowRenderer;
 
     /**
      * Kontruktor des CoolPanels mit einer Puffergroesse. Diese wird dazu verwendet,
      * um um Punktgeometrien eine BoundingBox der Groesse dieses Puffers zu erstellen.
      * @param geoBuffer Puffergroesse um das Geometrieobjekt herum
      */
     public PureCoolPanel(double geoBuffer) {
         this();
         this.geoBuffer = geoBuffer;
     }
 
     /**
      * Kontruktor des CoolPanels. Erzeugt ein CoolPanel, damit es in einem Renderer
      * verwendet werden kann.
      */
     public PureCoolPanel() {
         // FUSE initialisieren
         FuseLoader.load();
 
         // Ressourcen hierarchisch rekursiv nach oben einfuegen
         ResourceInjector.get("purecoolpanel.style").inject(true, getInstance());
 //
         gradientColorTop = javax.swing.UIManager.getDefaults().getColor("Button.shadow");
         gradientColorBottom = javax.swing.UIManager.getDefaults().getColor("Button.background");
         composite = AlphaComposite.SrcAtop.derive(titleLinesOpacity);
         shadowRenderer = new ShadowRenderer(shadowLength, shadowIntensity, shadowColor);
         initImagesAndMore();
         this.addComponentListener(this);

     }
 
     protected void initImagesAndMore() {
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
 
         gradientImage = null;
 
     }
 
     /**
      * Legt das Icon fest, die spaeter in die rechte obere Ecke des CoolPanels
      * gezeichnet werden. Diese sollten das im Navigator angewaehlte Objekt
      * beschreiben. Icons werden nur gezeichnet, falls sie vorhanden sind. Um mehrere
      * Icons zu zeichnen, muessen diese mit der Methode Static2DTools.joinIcons()
      * zusammengefuegt werden.
      *
      * @param icon das zu zeichnende Icon.
      */
     public void setImageRechtsOben(ImageIcon icon) {
         if (icon != null) {
             try {
 //            ReflectionRenderer renderer2 = new ReflectionRenderer(0.5f,0.4f,true);
                 ShadowRenderer renderer = new ShadowRenderer(3, 0.5f, Color.BLACK);
 
                 BufferedImage temp = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(),
                         IMAGE_TYPE);
                 Graphics tg = temp.createGraphics();
                 tg.drawImage(icon.getImage(), 0, 0, null);
                 tg.dispose();
 
                 BufferedImage shadow = renderer.createShadow(temp);
 
                 BufferedImage result = new BufferedImage(icon.getIconWidth() + 2 * shadowLength,
                         icon.getIconHeight() + 2 * shadowLength, IMAGE_TYPE);
                 Graphics rg = result.createGraphics();
                 rg.drawImage(shadow, 0, 0, null);
                 rg.drawImage(temp, 0, 0, null);
                 rg.dispose();
                 shadow.flush();
 
 //            BufferedImage ref = renderer.appendReflection(tmp);
                 ImageIcon icons = new ImageIcon(result);
                 this.icons = icons;
             } catch (Exception e) {
                 this.icons = null;
             }
         }
     }
     private static final Stroke STROKE = new BasicStroke(1.0f);
 
     /**
      * ueberschreibt die Standard-Zeichenmethode eines JPanels. Zeichnet die "coolen"
      * Effekte des CoolPanels.
      *
      * @param g Graphics-Objekt auf das gezeichnet wird
      */
     protected void paintComponent(Graphics g) {
         //        log.info("CoolPanel: paintComponent()");
         super.paintComponent(g);
         final Graphics2D g2d = (Graphics2D) g;
 
         if (cacheImage == null) {
             // Image zum Zeichnen erstellen von dem wird spaeter der Schlagschatten erstellt wird
             final BufferedImage box = new BufferedImage(getWidth() - offset, getHeight() - offset, IMAGE_TYPE);
 
             // Graphics-Objekt der Box erzeugen
             final Graphics2D boxGraphics = box.createGraphics();
 
             // Standard-Zeichenmodus speichern
             final Composite originalComposite = boxGraphics.getComposite();
 
             if (gradientImage == null || gradientImage.getHeight() != box.getHeight()) {
                 gradientImage = GraphicsUtilities.createCompatibleImage(1, box.getHeight());
                 final Graphics2D gradientGraphics2d = gradientImage.createGraphics();
                 gradientGraphics2d.setPaint(new GradientPaint(0, 0, gradientColorTop, 0, box.getHeight(), gradientColorBottom));
                 gradientGraphics2d.fillRect(0, 0, 1, box.getHeight());
             }
 
             // RoundedRectangle zeichnen und mit Gradient fuellen
 //            boxGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             boxGraphics.setColor(Color.BLACK);
             boxGraphics.fillRoundRect(offset, 0, box.getWidth() - offset, box.getHeight(), arcSize, arcSize);
             boxGraphics.setComposite(AlphaComposite.SrcAtop);
             boxGraphics.drawImage(gradientImage, 0, 0, box.getWidth(), box.getHeight(), null);
             boxGraphics.setComposite(originalComposite);
             boxGraphics.setStroke(STROKE);
 
             // Falls TitlePanel existiert, speziell zeichnen
             if (getPanTitle() != null) {
                 noTitlePanel = false;
                 Rectangle bounds = getPanTitle().getBounds();
                 boxGraphics.setComposite(AlphaComposite.SrcAtop.derive(titlePanelOpacity));
                 boxGraphics.setColor(colorTitle);
                 boxGraphics.fillRect(0, bounds.y, bounds.width + offset, bounds.height);
                 boxGraphics.setComposite(composite);
                 boxGraphics.setColor(colorDarkLine);
                 boxGraphics.drawLine(offset, bounds.height - 1, bounds.width + 3 * offset, bounds.height - 1);
                 boxGraphics.setColor(colorBrightLine);
                 boxGraphics.drawLine(offset, bounds.height, bounds.width + 3 * offset, bounds.height);
             }
 
             // Falls InteractionPanel existiert, speziell zeichnen
             if (getPanInter() != null) {
                 final Rectangle bounds = getPanInter().getBounds();
                 boxGraphics.setComposite(AlphaComposite.SrcAtop.derive(interPanelOpacity));
 //            bg.setColor(colInter);
                 boxGraphics.setPaint(new GradientPaint(0, bounds.y, new Color(0, 0, 0, 160), 0, bounds.y + bounds.height, Color.BLACK));
                 boxGraphics.fillRect(0, bounds.y, bounds.width + offset, bounds.height);
                 boxGraphics.setComposite(AlphaComposite.SrcAtop.derive(interLinesOpacity));
                 boxGraphics.setColor(colorDarkLine);
                 boxGraphics.drawLine(offset, bounds.y - 1, bounds.width + 3 * offset, bounds.y - 1);
                 boxGraphics.setColor(colorBrightLine);
                 boxGraphics.drawLine(offset, bounds.y, bounds.width + 3 * offset, bounds.y);
             }
 
             // Rahmen des RoundRectangel in der Box nachzeichnen
             boxGraphics.setComposite(AlphaComposite.SrcOver.derive(0.7f));
             boxGraphics.setColor(colorBorder);
             boxGraphics.drawRoundRect(offset, 0, box.getWidth() - (offset + 1), box.getHeight() - 1, arcSize, arcSize);
 
             // Weissen oberen Rand zeichnen
             final BufferedImage glossy = new BufferedImage(box.getWidth(), box.getHeight(), IMAGE_TYPE);
             final Graphics2D glossyGraphics2D = glossy.createGraphics();
 //            glossyGraphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             glossyGraphics2D.setStroke(STROKE);
 
             // Glossy-Effekt oben
             glossyGraphics2D.setColor(colorGlossy);
 //            if (noTitlePanel) {
             glossyGraphics2D.drawRoundRect(offset + 1, 1, box.getWidth() - (offset + 3), 2 * arcSize, arcSize, arcSize);
             glossyGraphics2D.setComposite(AlphaComposite.DstIn);
             glossyGraphics2D.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255, 255), 0, arcSize / 2, new Color(255, 255, 255, 0)));
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
                resultGraphics2D.drawImage(this.icons.getImage(), box.getWidth() - this.icons.getIconWidth() - offsetRight, offsetTop, null);
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
 
     public JComponent getPanTitle() {
         return panTitle;
     }
 
     public void setPanTitle(JPanel panTitle) {
         this.panTitle = panTitle;
     }
 
     public JComponent getPanMap() {
         return panMap;
     }
 
     public void setPanMap(JComponent panMap) {
         this.panMap = panMap;
     }
 
     public JComponent getPanInter() {
         return panInter;
     }
 
     public void setPanInter(JComponent panInter) {
         this.panInter = panInter;
     }
 
     public JComponent getPanContent() {
         return panContent;
     }
 
     public void setPanContent(JComponent panContent) {
         this.panContent = panContent;
     }
 
     public PureCoolPanel getInstance() {
         return this;
     }
 
     public JPanel getSpinner() {
         return spinner;
     }
 
     public void setSpinner(JPanel spinner) {
         this.spinner = spinner;
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         setPreferredSize(new java.awt.Dimension(200, 200));
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
         );
     }// </editor-fold>//GEN-END:initComponents
 
     public void componentResized(ComponentEvent arg0) {
         cacheImage = null;
         repaint();
     }
 
     public void componentMoved(ComponentEvent arg0) {
     }
 
     public void componentShown(ComponentEvent arg0) {
     }
 
     public void componentHidden(ComponentEvent arg0) {
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     // End of variables declaration//GEN-END:variables
 }
