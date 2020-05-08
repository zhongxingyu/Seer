 /*
  * Copyright (C) 2013 Zhao Yi
  *
  * This program is free software: you can redistribute it and/or modify
  * --it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package zhyi.zse.swing;
 
 import java.awt.AWTEvent;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.ComponentOrientation;
 import java.awt.Container;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.Window;
 import java.awt.event.AWTEventListener;
 import java.awt.event.ContainerEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Rectangle2D;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.Locale;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.ServiceLoader;
 import java.util.WeakHashMap;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JViewport;
 import javax.swing.LayoutStyle;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.Popup;
 import javax.swing.PopupFactory;
 import javax.swing.SwingUtilities;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 import javax.swing.plaf.metal.MetalTheme;
 import javax.swing.plaf.nimbus.AbstractRegionPainter;
 import javax.swing.plaf.nimbus.NimbusLookAndFeel;
 import javax.swing.text.JTextComponent;
 import javax.swing.undo.UndoManager;
 import zhyi.zse.i18n.FallbackLocaleControl;
 import zhyi.zse.lang.ReflectionUtils;
 
 /**
  * A command main class for a Swing application.
  * <p>
  * The instance is loaded by {@link ServiceLoader}, and only the first found one
  * is used.
  * <p>
  * Using this class benefits the following UI experience enhancements:
  * <p>
  * <b>Dynamic Look And Feel</b>
  * <p>
  * If the look and feel is changed by invoking {@link UIManager#setLookAndFeel},
  * it immediately takes effect. Additionally, the default look and feel is set
  * to system look and feel, instead of the Metal look and feel.
  * <p>
  * <b>Dynamic Locale</b>
  * <p>
  * By invoking {@link #updateLocale}, the JVM's current default locale will be
  * set to all components, so that the display language can be dynamically changed
  * by appropriate property change listeners.
  * <p>
  * <b>Default Popup Menus for Text Components</b>
  * <p>
  * Text components without component popup menus set up will have default popup
  * menus to do context actions like cut, copy, paste, etc.
  * <p>
  * <b>Fixes for Common Look And Feel Issues</b>
  * <ul>
  * <li>The distance of indent in group layout is too small.
  * <li>Drop shadows are missing for popup components.
  * <li>Read-only text components have wrong cursors.
  * </ul>
  * <b>Fixes for Windows 7 Aero Look And Feel Issues</b>
  * <ul>
 * <li>Combo boxes have wrong borders.
  * <li>Menu bar menus are insufficiently padded.
  * <li>Inactive or disabled text components have wrong background colors.
  * <li>Inactive text components have wrong cursors.
  * <li>Display properties are not honored by editor pane.
  * </ul>
  * <b>Fixes for Nimbus Look And Feel Issues</b>
  * <ul>
  * <li>Background color and opacity are not honored by some text components.
  * <li>The background color is incorrect for inactive text component.
  * <li>The cursor is incorrect for inactive text component.
  * </ul>
  *
  * @author Zhao Yi
  */
 public abstract class SwingApplication {
     private static final Field UI
             = ReflectionUtils.getDeclaredField(JComponent.class, "ui");
     private static final JPopupMenu TEXT_POPUP_MENU = new JPopupMenu();
     private static final Map<JTextComponent, Action[]>
             TEXT_POPUP_ACTION_MAP = new WeakHashMap<>();
     private static final String METAL_THEME_KEY = "metalTheme";
 
     private static boolean lafMonitored;
     private static boolean localeMonitored;
 
     /**
      * Sets the specified locale as the JVM's default locale, and applies it
      * to all components immediately.
      * <p>
      * This method can be used to switch the GUI language at runtime with
      * appropriate property change listeners on components' locale properties.
      *
      * @param l The new locale.
      */
     public static void setLocale(Locale l) {
         if (!Locale.getDefault().equals(l)) {
             Locale.setDefault(l);
             UIManager.getDefaults().setDefaultLocale(l);
             UIManager.getLookAndFeelDefaults().setDefaultLocale(l);
             JComponent.setDefaultLocale(l);
             for (Window w : Window.getWindows()) {
                 setComponentTreeLocale(
                         w, l, ComponentOrientation.getOrientation(l));
             }
             localeMonitored = true;
         }
     }
 
     /**
      * The callback back method to be invoked before entering the EDT.
      *
      * @param args The command line arguments passed to the main method.
      */
     protected abstract void preprocess(String[] args);
 
     /**
      * The callback method to be invoked in the EDT to show the GUI.
      */
     protected abstract void showGui();
 
     /**
      * The main method.
      *
      * @param args The command line arguments.
      */
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
             fixSwingIssues();
         } catch (final ReflectiveOperationException
                 | UnsupportedLookAndFeelException ex) {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     SwingUtils.showStackTrace(null, ex, false);
                 }
             });
         }
         monitorLaf();
         monitorComponents();
         addPopupShadows();
         addDefaultTextPopupMenu();
 
         final SwingApplication app = ServiceLoader.load(
                 SwingApplication.class).iterator().next();
         app.preprocess(args);
         SwingUtilities.invokeLater(new Runnable() {
             @Override
             public void run() {
                 app.showGui();
             }
         });
     }
 
     private static void addPopupShadows() {
         PopupFactory.setSharedInstance(new PopupFactory() {
             @Override
             public Popup getPopup(Component owner, Component contents, int x, int y) {
                 Popup popup = super.getPopup(owner, contents, x, y);
 
                 // If the popup component is contained in a heavy weight window,
                 // make that window's background transparent.
                 Window popupWindow = SwingUtilities.getWindowAncestor(contents);
                 if (popupWindow != null) {
                     popupWindow.setBackground(new Color(0, 0, 0, 0));
                 }
 
                 JPanel panel = (JPanel) contents.getParent();
                 panel.setOpaque(false);
                 panel.setBorder(new ShadowBorder(4, 4));
                 panel.setSize(panel.getPreferredSize());
 
                 return popup;
             }
         });
     }
 
     private static void fixSwingIssues() {
         // Double the group layout's indent distance.
         LayoutStyle.setInstance(null);
         final LayoutStyle lafLayoutStyle = LayoutStyle.getInstance();
         LayoutStyle.setInstance(new LayoutStyle() {
             @Override
             public int getPreferredGap(JComponent component1,
                     JComponent component2, ComponentPlacement type,
                     int position, Container parent) {
                 if (type == ComponentPlacement.INDENT) {
                     return 2 * lafLayoutStyle.getPreferredGap(
                             component1, component2,
                             ComponentPlacement.UNRELATED,
                             position, parent);
                 } else {
                     return lafLayoutStyle.getPreferredGap(
                             component1, component2, type, position, parent);
                 }
             }
 
             @Override
             public int getContainerGap(JComponent component,
                     int position, Container parent) {
                 return lafLayoutStyle.getContainerGap(component, position, parent);
             }
         });
 
         // Fix specific L&F issues.
         UIDefaults uid = UIManager.getLookAndFeelDefaults();
         switch (UIManager.getLookAndFeel().getName()) {
             case "Windows":
                 if (Double.parseDouble(System.getProperty("os.version")) >= 6.0
                         && Boolean.TRUE.equals(Toolkit.getDefaultToolkit()
                                 .getDesktopProperty("win.xpstyle.themeActive"))) {
                    uid.put("ComboBox.border", BorderFactory.createEmptyBorder(1, 2, 1, 1));
                     uid.put("Menu.border", BorderFactory.createEmptyBorder(0, 3, 0, 3));
                     uid.put("TextArea.inactiveBackground",
                             UIManager.get("TextArea.disabledBackground"));
                     uid.put("EditorPane.inactiveBackground",
                             UIManager.get("EditorPane.disabledBackground"));
                     uid.put("TextPane.inactiveBackground",
                             UIManager.get("TextPane.disabledBackground"));
                 }
                 break;
             case "Nimbus":
                 uid.put("TextField[Enabled].backgroundPainter",
                         new NimbusTextBackgroundPainter((AbstractRegionPainter)
                                 UIManager.get("TextField[Enabled].backgroundPainter")));
                 uid.put("FormattedTextField[Enabled].backgroundPainter",
                         new NimbusTextBackgroundPainter(
                                 (AbstractRegionPainter) UIManager.get(
                                 "FormattedTextField[Enabled].backgroundPainter")));
                 uid.put("PasswordField[Enabled].backgroundPainter",
                         new NimbusTextBackgroundPainter((AbstractRegionPainter)
                                 UIManager.get("PasswordField[Enabled].backgroundPainter")));
                 uid.put("TextArea[Enabled].backgroundPainter",
                         new NimbusTextBackgroundPainter((AbstractRegionPainter)
                                 UIManager.get("TextArea[Enabled].backgroundPainter")));
                 uid.put("TextArea[Enabled+NotInScrollPane].backgroundPainter",
                         new NimbusTextBackgroundPainter((AbstractRegionPainter)
                                 UIManager.get("TextArea[Enabled+NotInScrollPane].backgroundPainter")));
                 uid.put("EditorPane[Enabled].backgroundPainter",
                         new NimbusTextBackgroundPainter((AbstractRegionPainter)
                                 UIManager.get("EditorPane[Enabled].backgroundPainter")));
                 uid.put("TextPane[Enabled].backgroundPainter",
                         new NimbusTextBackgroundPainter((AbstractRegionPainter)
                                 UIManager.get("TextPane[Enabled].backgroundPainter")));
         }
     }
 
     private static void monitorLaf() {
         UIManager.addPropertyChangeListener(new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt) {
                 if (evt.getPropertyName().equals("lookAndFeel")) {
                     fixSwingIssues();
 
                     // Update components that have been added to windows.
                     // Other components will be handled later when they are
                     // added to a container.
                     Window[] windows = Window.getWindows();
                     if (windows.length > 0) {
                         for (Window window : windows) {
                             SwingUtilities.updateComponentTreeUI(window);
                         }
                         lafMonitored = true;
                     }
                 }
             }
         });
     }
 
     private static void monitorComponents() {
         Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
             @Override
             public void eventDispatched(AWTEvent event) {
                 ContainerEvent ce = (ContainerEvent) event;
                 if (ce.getID() == ContainerEvent.COMPONENT_ADDED) {
                     Component c = ce.getChild();
 
                     // Force to honor display properties for editor panes.
                     if (c instanceof JEditorPane) {
                         JEditorPane ep = (JEditorPane) c;
                         if (ep.getClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES) == null) {
                             ep.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
                         }
                     }
 
                     // Update L&F as needed.
                     if (lafMonitored && c instanceof JComponent) {
                         JComponent jc = (JComponent) c;
                         Object ui = ReflectionUtils.getValue(UI, jc);
                         if (ui != null) {
                             if (!ui.getClass().equals(
                                     UIManager.getDefaults().getUIClass(jc.getUIClassID()))) {
                                 jc.updateUI();
                             } else if (UIManager.getLookAndFeel().getName().equals("Metal")) {
                                 MetalTheme mt = MetalLookAndFeel.getCurrentTheme();
                                 if (jc.getClientProperty(METAL_THEME_KEY) != mt) {
                                     jc.updateUI();
                                     jc.putClientProperty(METAL_THEME_KEY, mt);
                                 }
                             }
                         }
                     }
 
                     // Update locale as needed.
                     if (localeMonitored && !c.getLocale().equals(Locale.getDefault())) {
                         c.setLocale(Locale.getDefault());
                     }
                 }
             }
         }, AWTEvent.CONTAINER_EVENT_MASK);
     }
 
     private static void addDefaultTextPopupMenu() {
         Toolkit.getDefaultToolkit().addAWTEventListener(new AWTEventListener() {
             @Override
             public void eventDispatched(AWTEvent event) {
                 MouseEvent me = (MouseEvent) event;
                 if (me.isPopupTrigger()) {
                     if (me.getComponent() instanceof JTextComponent) {
                         JTextComponent tc = (JTextComponent) me.getComponent();
                         if (tc.isEnabled() && tc.getComponentPopupMenu() == null) {
                             ContextActionHandler cah = (ContextActionHandler)
                                     tc.getClientProperty(ContextActionHandler.KEY);
                             if (cah == null) {
                                 cah = new TextContextActionHandler(tc);
                                 tc.putClientProperty(ContextActionHandler.KEY, cah);
                             }
 
                             Action[] popupActions = TEXT_POPUP_ACTION_MAP.get(tc);
                             if (popupActions == null) {
                                 popupActions = new Action[11];
                                 popupActions[0] = ContextActionFactory.createUndoAction(tc);
                                 popupActions[1] = ContextActionFactory.createRedoAction(tc);
                                 popupActions[2] = ContextActionFactory.createCutAction(tc);
                                 popupActions[3] = ContextActionFactory.createCopyAction(tc);
                                 popupActions[4] = ContextActionFactory.createPasteAction(tc);
                                 popupActions[5] = ContextActionFactory.createDeleteAction(tc);
                                 popupActions[6] = ContextActionFactory.createSelectAllAction(tc);
                                 popupActions[7] = ContextActionFactory.createCutAllAction(tc);
                                 popupActions[8] = ContextActionFactory.createCopyAllAction(tc);
                                 popupActions[9] = ContextActionFactory.createReplaceAllAction(tc);
                                 popupActions[10] = ContextActionFactory.createDeleteAllAction(tc);
                                 TEXT_POPUP_ACTION_MAP.put(tc, popupActions);
                             }
 
                             boolean editable = tc.isEditable();
                             boolean hasText = !SwingUtils.getRawText(tc).isEmpty();
                             boolean hasSelectedText = tc.getSelectedText() != null;
                             boolean canImport = tc.getTransferHandler().canImport(tc,
                                     Toolkit.getDefaultToolkit().getSystemClipboard()
                                             .getAvailableDataFlavors());
                             TEXT_POPUP_MENU.removeAll();
                             if (editable) {
                                 UndoManager um = cah.getUndoManager();
                                 popupActions[0].putValue(Action.NAME,
                                         um.getUndoPresentationName());
                                 popupActions[0].setEnabled(um.canUndo());
                                 popupActions[1].putValue(Action.NAME,
                                         um.getRedoPresentationName());
                                 popupActions[1].setEnabled(um.canRedo());
                                 TEXT_POPUP_MENU.add(popupActions[0]);    // Undo
                                 TEXT_POPUP_MENU.add(popupActions[1]);    // Redo
                                 TEXT_POPUP_MENU.addSeparator();
                                 popupActions[2].setEnabled(hasSelectedText);
                                 TEXT_POPUP_MENU.add(popupActions[2]);    // Cut
                             }
                             popupActions[3].setEnabled(hasSelectedText);
                             TEXT_POPUP_MENU.add(popupActions[3]);    // Copy
                             if (editable) {
                                 popupActions[4].setEnabled(canImport);
                                 TEXT_POPUP_MENU.add(popupActions[4]);    // Paste
                                 popupActions[5].setEnabled(hasSelectedText);
                                 TEXT_POPUP_MENU.add(popupActions[5]);    // Delete
                             }
                             TEXT_POPUP_MENU.addSeparator();
                             if (editable) {
                                 popupActions[6].setEnabled(hasText);
                                 TEXT_POPUP_MENU.add(popupActions[6]);    // Select All
                                 popupActions[7].setEnabled(hasText);
                                 TEXT_POPUP_MENU.add(popupActions[7]);    // Cut All
                             }
                             popupActions[7].setEnabled(hasText);
                             TEXT_POPUP_MENU.add(popupActions[8]);    // Copy All
                             if (editable) {
                                 popupActions[9].setEnabled(canImport);
                                 TEXT_POPUP_MENU.add(popupActions[9]);    // Replace All
                                 popupActions[10].setEnabled(hasText);
                                 TEXT_POPUP_MENU.add(popupActions[10]);    // Delete All
                             }
                             TEXT_POPUP_MENU.show(tc, me.getX(), me.getY());
                         }
                     }
                 }
             }
 
             private void addLocaleChangeListener(final JMenuItem mi) {
                 mi.addPropertyChangeListener("locale", new PropertyChangeListener() {
                     @Override
                     public void propertyChange(PropertyChangeEvent evt) {
                         mi.setText(ResourceBundle.getBundle(
                                 "zhyi.zse.swing.TextContextAction",
                                 FallbackLocaleControl.EN_US_CONTROL)
                                         .getString(mi.getActionCommand()));
                     }
                 });
             }
         }, AWTEvent.MOUSE_EVENT_MASK);
     }
 
     private static void setComponentTreeLocale(Component c,
             Locale l, ComponentOrientation o) {
         c.setLocale(l);
         c.setComponentOrientation(o);
 
         if (c instanceof JComponent) {
             JPopupMenu popupMenu = ((JComponent) c).getComponentPopupMenu();
             if (popupMenu != null) {
                 setComponentTreeLocale(popupMenu, l, o);
             }
         }
 
         Component[] children = null;
         if (c instanceof JMenu) {
             children = ((JMenu) c).getMenuComponents();
         }
         if (c instanceof Container) {
             children = ((Container) c).getComponents();
         }
         if (children != null) {
             for (Component child : children) {
                 setComponentTreeLocale(child, l, o);
             }
         }
     }
 
     private static class NimbusTextBackgroundPainter extends AbstractRegionPainter {
         private static final Color READONLY_TEXT_BACKGROUND
                 = ((NimbusLookAndFeel) UIManager.getLookAndFeel()).getDerivedColor(
                         "nimbusBlueGrey", -0.015872955F, -0.07995863F, 0.15294117F, 0, true);
         private static final Method GET_PAINT_CONTEXT = ReflectionUtils.getDeclaredMethod(
                 AbstractRegionPainter.class, "getPaintContext");
         private static final Method DO_PAINT = ReflectionUtils.getDeclaredMethod(
                 AbstractRegionPainter.class, "doPaint", Graphics2D.class,
                 JComponent.class, int.class, int.class, Object[].class);
 
         private AbstractRegionPainter originalPainter;
         private Rectangle2D.Float rec;
 
         private NimbusTextBackgroundPainter(AbstractRegionPainter originalPainter) {
             this.originalPainter = originalPainter;
             rec = new Rectangle2D.Float();
         }
 
         @Override
         protected PaintContext getPaintContext() {
             return (PaintContext) ReflectionUtils.invoke(
                     GET_PAINT_CONTEXT, originalPainter);
         }
 
         @Override
         protected void doPaint(Graphics2D g, JComponent c,
             int width, int height, Object[] extendedCacheKeys) {
             if (!c.isOpaque()) {
                 return;
             }
 
             Color background = c.getBackground();
             boolean enabled = c.isEnabled();
             boolean editable = c instanceof JTextComponent
                     ? ((JTextComponent) c).isEditable() : true;
             boolean backgroundSet = background.equals(
                     UIManager.getColor("background"));
 
             if (!enabled || enabled && editable && !backgroundSet) {
                 ReflectionUtils.invoke(DO_PAINT, originalPainter,
                         g, c, width, height, extendedCacheKeys);
             } else {
                 if (!editable) {
                     background = READONLY_TEXT_BACKGROUND;
                 }
                 g.setPaint(background);
                 if (c.getParent() instanceof JViewport) {
                     rec.setRect(decodeX(0.0F), decodeY(0.0F),
                             decodeX(3.0F) - decodeX(0.0F), decodeY(3.0F) - decodeY(0.0F));
                 } else {
                     rec.setRect(decodeX(0.4F), decodeY(0.4F),
                             decodeX(2.6F) - decodeX(0.4F), decodeY(2.6F) - decodeY(0.4F));
                 }
                 g.fill(rec);
             }
         }
     }
 }
