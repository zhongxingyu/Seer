 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.objectrenderer.utils;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.plugin.PluginRegistry;
 import Sirius.server.middleware.types.AbstractAttributeRepresentationFormater;
 import Sirius.server.middleware.types.MetaClass;
 import Sirius.server.middleware.types.MetaObject;
 import Sirius.server.newuser.User;
 import de.cismet.cids.dynamics.CidsBean;
 import de.cismet.cids.navigator.utils.ClassCacheMultiple;
 import de.cismet.cismap.commons.features.Feature;
 import de.cismet.cismap.commons.gui.MappingComponent;
 import de.cismet.cismap.commons.interaction.CismapBroker;
 import de.cismet.cismap.navigatorplugin.CidsFeature;
 import de.cismet.tools.CismetThreadPool;
 import de.cismet.tools.collections.TypeSafeCollections;
 import de.cismet.tools.gui.StaticSwingTools;
 import de.cismet.tools.gui.documents.DefaultDocument;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.font.TextAttribute;
 import java.awt.image.BufferedImage;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import javax.swing.ComboBoxModel;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JTable;
import javax.swing.JTextField;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import org.jdesktop.swingx.error.ErrorInfo;
 import org.jdesktop.swingx.graphics.ShadowRenderer;
 
 /**
  *
  * @author stefan
  */
 public class ObjectRendererUtils {
 
     private static final String ICON_RES_PACKAGE = "/de/cismet/cids/custom/wunda_blau/res/";
     public static final ImageIcon FORWARD_PRESSED;
     public static final ImageIcon FORWARD_SELECTED;
     public static final ImageIcon BACKWARD_PRESSED;
     public static final ImageIcon BACKWARD_SELECTED;
     private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ObjectRendererUtils.class);
     private static final String CISMAP_PLUGIN_NAME = "cismap";
 
     static {
         BACKWARD_SELECTED = new ImageIcon(ObjectRendererUtils.class.getResource(ICON_RES_PACKAGE + "arrow-left-sel.png"));
         BACKWARD_PRESSED = new ImageIcon(ObjectRendererUtils.class.getResource(ICON_RES_PACKAGE + "arrow-left-pressed.png"));
         FORWARD_SELECTED = new ImageIcon(ObjectRendererUtils.class.getResource(ICON_RES_PACKAGE + "arrow-right-sel.png"));
         FORWARD_PRESSED = new ImageIcon(ObjectRendererUtils.class.getResource(ICON_RES_PACKAGE + "arrow-right-pressed.png"));
     }
 
     public enum DateDiff {
 
         MILLISECOND, SECOND, MINUTE, HOUR, DAY, WEEK, MONTH, YEAR
     };
 
     public static void addBeanGeomsAsFeaturesToCismapMap(List<MetaObject> metaObjectList) {
         if (metaObjectList != null) {
             final MappingComponent bigMap = CismapBroker.getInstance().getMappingComponent();
             final List<Feature> addedFeatures = TypeSafeCollections.newArrayList(metaObjectList.size());
             for (MetaObject mo : metaObjectList) {
                 final CidsFeature newGeomFeature = new CidsFeature(mo);
                 addedFeatures.add(newGeomFeature);
                 bigMap.getFeatureCollection().addFeature(newGeomFeature);
             }
             bigMap.zoomToAFeatureCollection(addedFeatures, false, false);
         }
     }
 
     public static void setAllDimensions(JComponent comp, Dimension dim) {
         comp.setMaximumSize(dim);
         comp.setMinimumSize(dim);
         comp.setPreferredSize(dim);
     }
 
     public static void switchToCismapMap() {
         PluginRegistry.getRegistry().getPluginDescriptor(CISMAP_PLUGIN_NAME).getUIDescriptor(CISMAP_PLUGIN_NAME).getView().makeVisible();
     }
 
     public static void addBeanGeomAsFeatureToCismapMap(CidsBean bean) {
         if (bean != null) {
             final MetaObject mo = bean.getMetaObject();
             final List<MetaObject> mos = TypeSafeCollections.newArrayList(1);
             mos.add(mo);
             addBeanGeomsAsFeaturesToCismapMap(mos);
         }
     }
 
    public static void selectAllTextInEditableCombobox(JComboBox box) {
        Component editor = box.getEditor().getEditorComponent();
        if (editor instanceof JTextField) {
            JTextField textEditor = (JTextField) editor;
            textEditor.selectAll();
        } else {
            log.warn("Editor of Combobox " + box + " is not instanceof JTextField - can not select the text : " + editor);
        }
    }

     /**
      * shows an exception window to the user if the parent component is
      * currently shown.
      * @param titleMessage
      * @param ex
      * @param parent
      */
     public static void showExceptionWindowToUser(String titleMessage, Exception ex, Component parent) {
         if (ex != null && parent != null && parent.isShowing()) {
             org.jdesktop.swingx.error.ErrorInfo ei = new ErrorInfo(titleMessage, ex.getMessage(), null, null, ex, Level.ALL, null);
             org.jdesktop.swingx.JXErrorPane.showDialog(StaticSwingTools.getParentFrame(parent), ei);
         }
     }
 
     public static final MetaObject[] getLightweightMetaObjectsForTable(String tabName, final String[] fields) {
         return getLightweightMetaObjectsForTable(tabName, fields, null);
 
     }
 
     public static final MetaObject[] getLightweightMetaObjectsForTable(String tabName, final String[] fields, AbstractAttributeRepresentationFormater formatter) {
         if (formatter == null) {
             formatter = new AbstractAttributeRepresentationFormater() {
 
                 @Override
                 public String getRepresentation() {
                     final StringBuffer sb = new StringBuffer();
                     for (final String attribute : fields) {
                         sb.append(getAttribute(attribute.toLowerCase())).append(" ");
                     }
                     return sb.toString().trim();
                 }
             };
         }
         try {
             final User user = SessionManager.getSession().getUser();
             final MetaClass mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, tabName);
             return SessionManager.getProxy().getAllLightweightMetaObjectsForClass(mc.getID(), user, fields, formatter);
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         return new MetaObject[0];
     }
 
     public static MetaObject[] getLightweightMetaObjectsForQuery(String tabName, String query, final String[] fields, AbstractAttributeRepresentationFormater formatter) {
         if (formatter == null) {
             formatter = new AbstractAttributeRepresentationFormater() {
 
                 @Override
                 public String getRepresentation() {
                     final StringBuffer sb = new StringBuffer();
                     for (final String attribute : fields) {
                         sb.append(getAttribute(attribute.toLowerCase())).append(" ");
                     }
                     return sb.toString().trim();
                 }
             };
         }
         try {
             final User user = SessionManager.getSession().getUser();
             final MetaClass mc = ClassCacheMultiple.getMetaClass(CidsBeanSupport.DOMAIN_NAME, tabName);
             return SessionManager.getProxy().getLightweightMetaObjectsByQuery(mc.getID(), user, query, fields, formatter);
         } catch (Exception ex) {
             log.error(ex, ex);
         }
         return new MetaObject[0];
     }
 
     public static BufferedImage generateShadow(final Image in, final int shadowPixel) {
         if (in == null) {
             return null;
         }
         final BufferedImage input;
         if (in instanceof BufferedImage) {
             input = (BufferedImage) in;
         } else {
             final BufferedImage temp = new BufferedImage(in.getWidth(null), in.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
             final Graphics tg = temp.createGraphics();
             tg.drawImage(in, 0, 0, null);
             tg.dispose();
             input = temp;
         }
         if (shadowPixel < 1) {
             return input;
         }
         final ShadowRenderer renderer = new ShadowRenderer(shadowPixel, 0.5f, Color.BLACK);
         final BufferedImage shadow = renderer.createShadow(input);
         final BufferedImage result = new BufferedImage(input.getWidth() + 2 * shadowPixel,
                 input.getHeight() + 2 * shadowPixel, BufferedImage.TYPE_4BYTE_ABGR);
         final Graphics2D rg = result.createGraphics();
         rg.drawImage(shadow, 0, 0, null);
         rg.drawImage(input, 0, 0, null);
         rg.dispose();
         return result;
     }
 
     /**
      * Starts a background thread with loads the picture from the url, resizes it to the given maximums,
      * adds a dropshadow of the given length and then sets the whole picture on a given JLabel.
      *
      * Can be called from ANY thread, no matter if EDT or not!
      *
      * @param bildURL
      * @param maxPixelX
      * @param maxPixelY
      * @param shadowSize
      * @param toSet
      */
     public static void loadPictureAndSet(final String bildURL, final int maxPixelX, final int maxPixelY, final int shadowSize, final JLabel toSet) {
         if (bildURL != null && toSet != null) {
             final Runnable loader = new Runnable() {
 
                 @Override
                 public void run() {
                     try {
                         final ImageIcon finBild = loadPicture(bildURL, maxPixelX, maxPixelY, shadowSize);
                         EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 if (finBild != null) {
                                     toSet.setIcon(finBild);
                                 } else {
                                     toSet.setIcon(null);
 //                                        toSet.setVisible(false);
                                 }
                             }
                         });
 
                     } catch (Exception e) {
                         log.error("Exeption when loading picture " + bildURL + " : " + e, e);
                         toSet.setIcon(null);
                     }
                 }
             };
             CismetThreadPool.execute(loader);
         }
     }
 
     /**
      * Starts a background thread with loads the picture from the url, resizes it to the given maximums,
      * adds a dropshadow of the given length and then sets the whole picture on a given JButton.
      *
      * Can be called from ANY thread, no matter if EDT or not!
      *
      * @param bildURL
      * @param maxPixelX
      * @param maxPixelY
      * @param shadowSize
      * @param toSet
      */
     public static void loadPictureAndSet(final String bildURL, final int maxPixelX, final int maxPixelY, final int shadowSize, final JButton toSet) {
         if (bildURL != null && toSet != null) {
             final Runnable loader = new Runnable() {
 
                 @Override
                 public void run() {
                     final ImageIcon finBild = loadPicture(bildURL, maxPixelX, maxPixelY, shadowSize);
                     if (finBild != null) {
                         EventQueue.invokeLater(new Runnable() {
 
                             @Override
                             public void run() {
                                 if (finBild != null) {
                                     toSet.setIcon(finBild);
                                 } else {
                                     toSet.setVisible(false);
                                 }
                             }
                         });
                     }
                 }
             };
             CismetThreadPool.execute(loader);
         }
     }
 
     /**
      * Starts a background thread with loads the picture from the url, resizes it to the given maximums,
      * adds a dropshadow of the given length.
      *
      * @param bildURL
      * @param maxPixelX
      * @param maxPixelY
      * @param shadowSize
      * @return ImageIcon with the loaded picture
      */
     public static ImageIcon loadPicture(final String bildURL, final int maxPixelX, final int maxPixelY, final int shadowSize) {
         ImageIcon bild = null;
         if (bildURL != null && bildURL.length() > 0) {
             final String urlString = bildURL.trim();
 
             Image buffImage = new DefaultDocument(urlString, urlString).getPreview(maxPixelX, maxPixelY);
             if (buffImage != null) {
                 //Static2DTools.getFasterScaledInstance(buffImage, width, height, RenderingHints.VALUE_INTERPOLATION_BICUBIC, true)
                 if (shadowSize > 0) {
                     buffImage = generateShadow(buffImage, shadowSize);
                 }
                 bild = new ImageIcon(buffImage);
                 return bild;
             }
         }
         return null;
     }
 
     /**
      * Adds a mouse listener to the given component, so that the cursor will change on mouse entered/exited.
      *
      * Hint: Uses the awt.Cursor.XXX constants!
      *
      * @param toDecorate
      * @param mouseEntered
      * @param mouseExited
      * @return the listener that was added
      */
     public static MouseListener decorateComponentWithMouseOverCursorChange(final JComponent toDecorate, final int mouseEntered, final int mouseExited) {
         final MouseListener toAdd = new MouseAdapter() {
 
             private final Cursor entered = new Cursor(mouseEntered);
             private final Cursor exited = new Cursor(mouseExited);
 
             @Override
             public void mouseEntered(MouseEvent e) {
                 if (toDecorate.isEnabled()) {
                     toDecorate.setCursor(entered);
                 }
             }
 
             @Override
             public void mouseExited(MouseEvent e) {
                 if (toDecorate.isEnabled()) {
                     toDecorate.setCursor(exited);
                 }
             }
         };
         toDecorate.addMouseListener(toAdd);
         return toAdd;
     }
 
     public static void openURL(final String url) {
         if (url == null) {
             return;
         }
         String gotoUrl = url;
         try {
             de.cismet.tools.BrowserLauncher.openURL(gotoUrl);
         } catch (Exception e2) {
             log.warn("das 1te Mal ging schief.Fehler beim Oeffnen von:" + gotoUrl + "\nLetzter Versuch", e2);
             try {
                 gotoUrl = gotoUrl.replaceAll("\\\\", "/");
                 gotoUrl = gotoUrl.replaceAll(" ", "%20");
                 de.cismet.tools.BrowserLauncher.openURL("file:///" + gotoUrl);
             } catch (Exception e3) {
                 log.error("Auch das 2te Mal ging schief.Fehler beim Oeffnen von:file://" + gotoUrl, e3);
             }
         }
     }
 
     public static String propertyPrettyPrint(Object propertyValue) {
         if (propertyValue instanceof Collection) {
             final Collection beanCollection = (Collection) propertyValue;
             final StringBuilder resultSB = new StringBuilder();
             for (Object bean : beanCollection) {
                 if (resultSB.length() != 0) {
                     resultSB.append(", ");
                 }
                 resultSB.append(String.valueOf(bean));
             }
             return resultSB.toString();
         } else if (propertyValue != null) {
             return propertyValue.toString();
         } else {
             return "-";
         }
     }
 
     public static final int findComboBoxItemForString(JComboBox box, String searchString) {
         if (box != null && searchString != null) {
             ComboBoxModel model = box.getModel();
             if (model != null) {
                 for (int i = model.getSize(); --i >= 0;) {
                     if (searchString.equals(String.valueOf(model.getElementAt(i)))) {
                         return i;
                     }
                 }
             }
         }
         return -1;
     }
 
     public static String getUrlFromBean(CidsBean bean, String suffix) {
         final Object obj = bean.getProperty("url_base_id");
         if (obj instanceof CidsBean) {
             final CidsBean urlBase = (CidsBean) obj;
             final StringBuffer bildURL = new StringBuffer(urlBase.getProperty("prot_prefix").toString());
             bildURL.append(urlBase.getProperty("server").toString());
             bildURL.append(urlBase.getProperty("path").toString());
             bildURL.append(bean.getProperty("object_name").toString());
             if (suffix != null) {
                 bildURL.append(suffix);
             }
             return bildURL.toString();
         }
         return null;
     }
 
     /**
      * Makes the parameter table alphanumerically sortable.
      *
      * @param tbl
      */
     public static TableRowSorter<TableModel> decorateTableWithSorter(JTable tbl) {
         final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tbl.getModel());
 //        sorter.setSortsOnUpdates(true);
         for (int i = 0; i < tbl.getColumnCount(); ++i) {
             sorter.setComparator(i, AlphanumComparator.getInstance());
         }
         tbl.setRowSorter(sorter);
         tbl.getTableHeader().addMouseListener(new TableHeaderUnsortMouseAdapter(tbl));
         return sorter;
 
     }
 
     public static MouseAdapter decorateJLabelWithLinkBehaviour(JLabel label) {
         LabelLinkBehaviourMouseAdapter llbma = new LabelLinkBehaviourMouseAdapter(label);
         label.addMouseListener(llbma);
         return llbma;
     }
 
     public static MouseAdapter decorateJLabelAndButtonSynced(JLabel label, JButton button, Icon highlight, Icon pressed) {
         final MouseAdapter syncedAdapter = new SyncLabelButtonMouseAdapter(label, button, highlight, pressed);
         label.addMouseListener(syncedAdapter);
         button.addMouseListener(syncedAdapter);
         return syncedAdapter;
     }
 
     public static MouseAdapter decorateButtonWithStatusImages(JButton button, Icon plain, Icon highlight, Icon pressed) {
         final ImagedButtonMouseAdapter ibma = new ImagedButtonMouseAdapter(button, plain, highlight, pressed);
         button.addMouseListener(ibma);
         return ibma;
     }
 
     public static MouseAdapter decorateButtonWithStatusImages(JButton button, Icon highlight, Icon pressed) {
         final ImagedButtonMouseAdapter ibma = new ImagedButtonMouseAdapter(button, highlight, pressed);
         button.addMouseListener(ibma);
         return ibma;
     }
 }
 
 /**
  * MouseAdapter for remove sorting from the table when perfoming a right-click
  * on the header
  *
  * @author srichter
  */
 final class TableHeaderUnsortMouseAdapter extends MouseAdapter {
 
     public TableHeaderUnsortMouseAdapter(JTable tbl) {
         this.tbl = tbl;
     }
     private JTable tbl;
 
     @Override
     public void mousePressed(MouseEvent e) {
         if (e.isPopupTrigger()) {
             tbl.getRowSorter().setSortKeys(null);
         }
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         if (e.isPopupTrigger()) {
             tbl.getRowSorter().setSortKeys(null);
         }
     }
 }
 
 /**
  *
  * @author srichter
  */
 final class LabelLinkBehaviourMouseAdapter extends MouseAdapter {
 
     public LabelLinkBehaviourMouseAdapter(JLabel label) {
         this.label = label;
         plain = label.getFont();
         final Map<TextAttribute, Object> attributesMap = (Map<TextAttribute, Object>) plain.getAttributes();
         attributesMap.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
         underlined = plain.deriveFont(attributesMap);
 
     }
     private final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
     private final Font underlined;
     private final Font plain;
     protected final JLabel label;
 
     @Override
     public void mouseEntered(MouseEvent e) {
         label.setCursor(handCursor);
         if (label.isEnabled() && label.getFont() != underlined) {
             label.setFont(underlined);
         }
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         label.setCursor(Cursor.getDefaultCursor());
         if (label.getFont() != plain) {
             label.setFont(plain);
         }
     }
 }
 
 final class ImagedButtonMouseAdapter extends MouseAdapter {
 
     public ImagedButtonMouseAdapter(JButton button, Icon plain, Icon highlight, Icon pressed) {
         this.button = button;
         ObjectRendererUtils.decorateComponentWithMouseOverCursorChange(button, Cursor.HAND_CURSOR, Cursor.DEFAULT_CURSOR);
         this.plainIcon = plain;
         this.highlightIcon = highlight;
         this.pressedIcon = pressed;
     }
 
     public ImagedButtonMouseAdapter(JButton button, Icon highlight, Icon pressed) {
         this(button, button.getIcon(), highlight, pressed);
     }
     private final Icon plainIcon;
     private final Icon highlightIcon;
     private final Icon pressedIcon;
     protected final JButton button;
     protected boolean over = false;
     protected boolean pressed = false;
 
     @Override
     public void mouseEntered(MouseEvent e) {
         over = true;
         handleEvent(e);
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         over = false;
         handleEvent(e);
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         pressed = true;
         handleEvent(e);
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         pressed = false;
         handleEvent(e);
     }
 
     private final void testAndSet(Icon icon) {
         if (button.getIcon() != icon) {
             button.setIcon(icon);
         }
     }
 
     protected void handleEvent(MouseEvent e) {
         if (button.isEnabled()) {
             if (pressed && over) {
                 testAndSet(pressedIcon);
             } else if (over) {
                 testAndSet(highlightIcon);
             } else {
                 testAndSet(plainIcon);
             }
         } else {
             testAndSet(plainIcon);
         }
     }
 }
 
 final class SyncLabelButtonMouseAdapter extends MouseAdapter {
 
     public SyncLabelButtonMouseAdapter(JLabel label, JButton button, Icon highlight, Icon pressed) {
         delegateButton = new ImagedButtonMouseAdapter(button, highlight, pressed);
         delegateLabel = new LabelLinkBehaviourMouseAdapter(label);
     }
     private final MouseAdapter delegateButton;
     private final MouseAdapter delegateLabel;
 
     @Override
     public void mouseEntered(MouseEvent e) {
         delegateButton.mouseEntered(e);
         delegateLabel.mouseEntered(e);
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         delegateButton.mouseExited(e);
         delegateLabel.mouseExited(e);
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
         delegateButton.mousePressed(e);
         delegateLabel.mousePressed(e);
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         delegateButton.mouseReleased(e);
         delegateLabel.mouseReleased(e);
     }
 }
