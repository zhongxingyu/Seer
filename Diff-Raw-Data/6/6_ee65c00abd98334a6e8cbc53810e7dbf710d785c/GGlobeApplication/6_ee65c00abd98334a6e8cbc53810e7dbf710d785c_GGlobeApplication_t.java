 /*
 
  IGO Software SL  -  info@igosoftware.es
 
  http://www.glob3.org
 
 -------------------------------------------------------------------------------
  Copyright (c) 2010, IGO Software SL
  All rights reserved.
 
  Redistribution and use in source and binary forms, with or without
  modification, are permitted provided that the following conditions are met:
      * Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
      * Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
      * Neither the name of the IGO Software SL nor the
        names of its contributors may be used to endorse or promote products
        derived from this software without specific prior written permission.
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  DISCLAIMED. IN NO EVENT SHALL IGO Software SL BE LIABLE FOR ANY
  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 -------------------------------------------------------------------------------
 
 */
 
 
 package es.igosoftware.globe;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JApplet;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPopupMenu;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JToolBar;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.ToolTipManager;
 import javax.swing.UIDefaults;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 
 import org.pushingpixels.substance.api.SubstanceLookAndFeel;
 import org.pushingpixels.substance.api.fonts.FontPolicy;
 import org.pushingpixels.substance.api.fonts.FontSet;
 import org.pushingpixels.substance.api.skin.SubstanceMistAquaLookAndFeel;
 
 import es.igosoftware.globe.actions.IGenericAction;
 import es.igosoftware.globe.layers.I3DContentCollectionLayer;
 import es.igosoftware.globe.view.customView.GCustomView;
 import es.igosoftware.globe.view.customView.GCustomViewInputHandler;
 import es.igosoftware.util.GCollections;
 import es.igosoftware.util.GHolder;
 import es.igosoftware.util.GLogger;
 import es.igosoftware.util.GPair;
 import es.igosoftware.util.GUtils;
 import es.igosoftware.util.LRUCache;
 import es.igosoftware.utils.GSwingUtils;
 import es.igosoftware.utils.GWrapperFontSet;
 import gov.nasa.worldwind.BasicModel;
 import gov.nasa.worldwind.Configuration;
 import gov.nasa.worldwind.Model;
 import gov.nasa.worldwind.SceneController;
 import gov.nasa.worldwind.View;
 import gov.nasa.worldwind.avlist.AVKey;
 import gov.nasa.worldwind.awt.WorldWindowGLCanvas;
 import gov.nasa.worldwind.examples.sunlight.RectangularNormalTessellator;
 import gov.nasa.worldwind.examples.util.StatusLayer;
 import gov.nasa.worldwind.geom.Angle;
 import gov.nasa.worldwind.geom.Position;
 import gov.nasa.worldwind.geom.Sector;
 import gov.nasa.worldwind.globes.Earth;
 import gov.nasa.worldwind.globes.Globe;
 import gov.nasa.worldwind.layers.Layer;
 import gov.nasa.worldwind.layers.LayerList;
 import gov.nasa.worldwind.layers.ViewControlsLayer;
 import gov.nasa.worldwind.layers.ViewControlsSelectListener;
 import gov.nasa.worldwind.terrain.SectorGeometryList;
 import gov.nasa.worldwind.view.orbit.OrbitView;
 import gov.nasa.worldwind.view.orbit.OrbitViewInputHandler;
 
 
 public abstract class GGlobeApplication
          extends
             JApplet
          implements
             IGlobeApplication {
 
    private static final long    serialVersionUID = 1L;
 
 
    private static final GLogger LOGGER           = GLogger.instance();
 
    static {
       if (GUtils.isWindows()) {
          System.setProperty("sun.awt.noerasebackground", "true"); // prevents flashing during window resizing
       }
 
       if (GUtils.isMac()) {
          // System.setProperty("com.apple.mrj.application.growbox.intrudes", "false");
          // System.setProperty("apple.laf.useScreenMenuBar", "true");
          // System.setProperty("com.apple.mrj.application.apple.menu.about.name", "glob3");
       }
 
 
       Configuration.setValue(AVKey.VIEW_CLASS_NAME, GCustomView.class.getName());
 
       Configuration.setValue(AVKey.TESSELLATOR_CLASS_NAME, RectangularNormalTessellator.class.getName());
 
       //      System.setProperty("sun.java2d.noddraw", "true");
       //      System.setProperty("sun.java2d.opengl", "true");
 

      UIManager.put("ClassLoader", GGlobeApplication.class.getClassLoader());

       JPopupMenu.setDefaultLightWeightPopupEnabled(false);
       ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
 
       SwingUtilities.invokeLater(new Runnable() {
          @Override
          public void run() {
             initializeSubstance();
          }
       });
    }
 
 
    private static class IconKey {
       private final String _name;
       private final int    _width;
       private final int    _height;
 
 
       private IconKey(final String name,
                       final int width,
                       final int height) {
          _name = name;
          _width = width;
          _height = height;
       }
 
 
       @Override
       public int hashCode() {
          final int prime = 31;
          int result = 1;
          result = prime * result + _height;
          result = prime * result + ((_name == null) ? 0 : _name.hashCode());
          result = prime * result + _width;
          return result;
       }
 
 
       @Override
       public boolean equals(final Object obj) {
          if (this == obj) {
             return true;
          }
          if (obj == null) {
             return false;
          }
          if (getClass() != obj.getClass()) {
             return false;
          }
          final IconKey other = (IconKey) obj;
          if (_height != other._height) {
             return false;
          }
          if (_name == null) {
             if (other._name != null) {
                return false;
             }
          }
          else if (!_name.equals(other._name)) {
             return false;
          }
          if (_width != other._width) {
             return false;
          }
          return true;
       }
    }
 
    private static GGlobeApplication                         _application;
 
    private final LRUCache<IconKey, Icon, RuntimeException>  _iconsCache;
    private final LRUCache<IconKey, Image, RuntimeException> _imagesCache;
 
 
    private static void initializeFonts() {
       if (GUtils.isWindows()) {
          return;
       }
 
       // reset the base font policy to null - this
       // restores the original font policy (default size).
       SubstanceLookAndFeel.setFontPolicy(null);
 
       // Get the default font set
       final FontSet substanceCoreFontSet = SubstanceLookAndFeel.getFontPolicy().getFontSet("Substance", null);
 
       // Create the wrapper font set
       final FontPolicy newFontPolicy = new FontPolicy() {
          @Override
          public FontSet getFontSet(final String lafName,
                                    final UIDefaults table) {
             final int delta;
             if (GUtils.isMac()) {
                delta = -3;
             }
             else {
                delta = -1;
             }
             return new GWrapperFontSet(substanceCoreFontSet, delta);
          }
       };
 
       // set the new font policy
       SubstanceLookAndFeel.setFontPolicy(newFontPolicy);
    }
 
 
    private static void initializeSubstance() {
       //      try {
       //         UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
       //      }
       //      catch (final Exception e) {
       //         e.printStackTrace();
       //      }
 
       JFrame.setDefaultLookAndFeelDecorated(true);
       JDialog.setDefaultLookAndFeelDecorated(true);
 
       try {
          UIManager.setLookAndFeel(new SubstanceMistAquaLookAndFeel());
 
          SubstanceLookAndFeel.setToUseConstantThemesOnDialogs(true);
       }
       catch (final UnsupportedLookAndFeelException e) {
          e.printStackTrace();
       }
 
       initializeFonts();
    }
 
 
    private final IGlobeModule[]                   _modules;
    private final WorldWindowGLCanvas              _wwGLCanvas;
    private JFrame                                 _frame;
 
    // UI elements, they must be final
    private JMenuBar                               _menubar;
    private JToolBar                               _toolbar;
 
    private JMenu                                  _fileMenu;
    private JMenu                                  _navigationMenu;
    private JMenu                                  _viewMenu;
    private JMenu                                  _analysisMenu;
    private JMenu                                  _helpMenu;
 
    private final Map<String, Map<String, String>> _translationsSets;
 
    private String                                 _currentLanguage;
 
 
    //      private final boolean             _verbose;
 
 
    protected GGlobeApplication() {
       this(Locale.getDefault().getLanguage());
    }
 
 
    protected GGlobeApplication(final String language) {
       logInfo("Starting " + getApplicationNameAndVersion() + "...");
 
       _currentLanguage = language;
 
       _translationsSets = initializeTranslations();
 
       _wwGLCanvas = new WorldWindowGLCanvas();
 
       final Globe globe = initializeGlobe();
       final Model model = createModel(globe);
 
       _wwGLCanvas.setModel(model);
 
       _modules = initializeModules();
 
       _iconsCache = initializaIconsCache();
       _imagesCache = initializaImagesCache();
 
       //            _verbose = GUtils.isDevelopment();
 
       registerInstance(this);
    }
 
 
    private LRUCache<IconKey, Icon, RuntimeException> initializaIconsCache() {
       return new LRUCache<IconKey, Icon, RuntimeException>(50, new LRUCache.ValueFactory<IconKey, Icon, RuntimeException>() {
          private static final long serialVersionUID = 1L;
 
 
          @Override
          public Icon create(final IconKey key) {
 
             URL url = null;
             for (final String directory : getIconsDirectories()) {
                final String path = directory + "/" + key._name;
 
                url = getClass().getClassLoader().getResource(path);
                if (url != null) {
                   break;
                }
             }
 
             if (url == null) {
                return null;
             }
 
             final ImageIcon icon = new ImageIcon(url);
 
             final Image image = icon.getImage();
             if (image == null) {
                return icon;
             }
             final int width = image.getWidth(null);
             final int height = image.getHeight(null);
             if ((width == -1) || (height == -1)) {
                return icon;
             }
             if ((width == key._width) && (height == key._height)) {
                return icon;
             }
 
             final Image resizedImage = image.getScaledInstance(key._width, key._height, Image.SCALE_SMOOTH);
             return new ImageIcon(resizedImage);
          }
       });
    }
 
 
    private LRUCache<IconKey, Image, RuntimeException> initializaImagesCache() {
       return new LRUCache<IconKey, Image, RuntimeException>(50, new LRUCache.ValueFactory<IconKey, Image, RuntimeException>() {
          private static final long serialVersionUID = 1L;
 
 
          @Override
          public Image create(final IconKey key) {
 
             URL url = null;
             for (final String directory : getIconsDirectories()) {
                final String path = directory + "/" + key._name;
 
                url = getClass().getClassLoader().getResource(path);
                if (url != null) {
                   break;
                }
             }
 
             if (url == null) {
                return null;
             }
 
             try {
 
 
                final BufferedImage image = ImageIO.read(url);
 
                if (image == null) {
                   return null;
                }
 
                final int width = image.getWidth(null);
                final int height = image.getHeight(null);
                if ((width == -1) || (height == -1)) {
                   return image;
                }
                if ((width == key._width) && (height == key._height)) {
                   return image;
                }
 
                final Image resizedImage = image.getScaledInstance(key._width, key._height, Image.SCALE_SMOOTH);
                return resizedImage;
             }
             catch (final IOException e) {
                return null;
             }
          }
       });
    }
 
 
    private static void registerInstance(final GGlobeApplication application) {
       if (_application != null) {
          throw new RuntimeException("Can't register more than one application");
       }
 
       _application = application;
    }
 
 
    public static GGlobeApplication instance() {
       return _application;
    }
 
 
    protected Globe initializeGlobe() {
       return new Earth();
    }
 
 
    private IGlobeModule[] initializeModules() {
       final GHolder<IGlobeModule[]> modules = new GHolder<IGlobeModule[]>(null);
 
       try {
          invokeInEventThread(new Runnable() {
             @Override
             public void run() {
                //            final IGlobeModule[] modules = getModules();
                modules.set(getModules());
 
                for (final IGlobeModule module : modules.get()) {
                   module.initialize(GGlobeApplication.this);
                   module.initializeTranslations(GGlobeApplication.this);
                }
             }
          });
       }
       catch (final InterruptedException e) {
          e.printStackTrace();
       }
       catch (final InvocationTargetException e) {
          e.printStackTrace();
       }
 
       return modules.get();
    }
 
 
    private static void invokeInEventThread(final Runnable runnable) throws InterruptedException, InvocationTargetException {
       if (EventQueue.isDispatchThread()) {
          runnable.run();
       }
       else {
          SwingUtilities.invokeAndWait(runnable);
       }
    }
 
 
    @Override
    public Dimension initialDimension() {
       return new Dimension(1024, 768);
    }
 
 
    protected List<String> getIconsDirectories() {
       return GCollections.createList("bitmaps/icons", "bitmaps", "../globe/bitmaps/icons", "globe/bitmaps/icons");
    }
 
 
    protected List<String> getImagesDirectories() {
       return GCollections.createList("bitmaps/icons", "bitmaps", "../globe/bitmaps/icons", "globe/bitmaps/icons");
    }
 
 
    protected Model createModel(final Globe globe) {
       final GModel model = new GModel(globe);
       //      model.setShowWireframeExterior(true);
 
       model.setLayers(getDefaultLayers());
 
       return model;
    }
 
 
    protected LayerList getDefaultLayers() {
       final LayerList layers = new BasicModel().getLayers();
 
       final StatusLayer statusLayer = new StatusLayer();
       statusLayer.setEventSource(getWorldWindowGLCanvas());
       // statusLayer.setCoordDecimalPlaces(2); // default is 4
       layers.add(statusLayer);
 
       // initWMS(layers);
       // layers.getLayerByName("pnoa").setEnabled(false);
       //
       // layers.getLayerByName("orto2006").setEnabled(false);
 
       layers.getLayerByName("NASA Blue Marble Image").setEnabled(true);
 
       layers.getLayerByName("Blue Marble (WMS) 2004").setEnabled(true);
       layers.getLayerByName("i-cubed Landsat").setEnabled(true);
       //      layers.getLayerByName("MS Virtual Earth Aerial").setEnabled(true);
 
 
       final ViewControlsLayer viewControlsLayer = new ViewControlsLayer();
       layers.add(viewControlsLayer);
       getWorldWindowGLCanvas().addSelectListener(new ViewControlsSelectListener(getWorldWindowGLCanvas(), viewControlsLayer));
 
       return layers;
    }
 
 
    @Override
    public void init() {
       try {
          invokeInEventThread(new Runnable() {
             @Override
             public void run() {
               SwingUtilities.updateComponentTreeUI(GGlobeApplication.this);

                initGUI();
 
                IGlobeModule previousModule = null;
                for (final IGlobeModule module : _modules) {
                   initModuleGUI(module, previousModule);
                   previousModule = module;
                }
 
                postInitGUI();
             }
          });
       }
       catch (final InterruptedException e) {
          e.printStackTrace();
       }
       catch (final InvocationTargetException e) {
          e.printStackTrace();
       }
    }
 
 
    protected void postInitGUI() {
       if (_fileMenu == null) {
          return;
       }
 
       //      _fileMenu.addSeparator();
 
       final JMenuItem exitItem = GSwingUtils.createMenuItem(getTranslation("Exit"), getIcon("quit.png"), 'x',
                new ActionListener() {
                   @Override
                   public void actionPerformed(final ActionEvent e) {
                      exit();
                   }
                });
       _fileMenu.add(exitItem);
    }
 
 
    @Override
    public Icon getIcon(final String iconName,
                        final int width,
                        final int height) {
       return _iconsCache.get(new IconKey(iconName, width, height));
    }
 
 
    @Override
    public Image getImage(final String imageName,
                          final int width,
                          final int height) {
       return _imagesCache.get(new IconKey(imageName, width, height));
    }
 
 
    @Override
    public Icon getIcon(final String iconName) {
       final int defaultIconSize = getDefaultIconSize();
       return getIcon(iconName, defaultIconSize, defaultIconSize);
    }
 
 
    @Override
    public Image getImage(final String imageName) {
       final int defaultIconSize = getDefaultIconSize();
       return getImage(imageName, defaultIconSize, defaultIconSize);
    }
 
 
    protected int getDefaultIconSize() {
       return 20;
    }
 
 
    private void initModuleGUI(final IGlobeModule module,
                               final IGlobeModule previousModule) {
 
       final List<IGenericAction> genericActions = module.getGenericActions(GGlobeApplication.this);
 
       final Set<IGenericAction.MenuArea> firstUseFlags = new HashSet<IGenericAction.MenuArea>();
 
       boolean firstActionOnToolBar = true;
       if (genericActions != null) {
          for (final IGenericAction action : genericActions) {
             if (!action.isVisible()) {
                continue;
             }
 
             putActionOnMenuBar(action, firstUseFlags);
 
             if (action.isShowOnToolBar()) {
                if (firstActionOnToolBar) {
                   firstActionOnToolBar = false;
                   if (_toolbar.getComponents().length > 0) {
                      if ((previousModule != null) && (previousModule.getClass() != module.getClass())) {
                         _toolbar.addSeparator();
                      }
                   }
                }
                _toolbar.add(action.createToolbarWidget(GGlobeApplication.this));
             }
          }
       }
    }
 
 
    private void putActionOnMenuBar(final IGenericAction action,
                                    final Set<IGenericAction.MenuArea> firstUseFlags) {
       final IGenericAction.MenuArea area = action.getMenuBarArea();
 
       final JMenu menu;
       switch (area) {
          case FILE:
             menu = _fileMenu;
             break;
          case NAVIGATION:
             menu = _navigationMenu;
             break;
          case HELP:
             menu = _helpMenu;
             break;
          case ANALYSIS:
             menu = _analysisMenu;
             break;
          case VIEW:
             menu = _viewMenu;
             break;
          default:
             logSevere("Invalid menu bar area: " + area);
             menu = null;
       }
 
       if (menu != null) {
          final boolean firstUse = !firstUseFlags.contains(area);
          if (firstUse) {
             firstUseFlags.add(area);
             if (menu.getComponents().length > 0) {
                menu.addSeparator();
             }
          }
 
          menu.add(action.createMenuWidget(this));
       }
    }
 
 
    public void openInFrame() {
       logInfo("Opening frame...");
 
       _frame = new JFrame();
       _frame.setTitle(getApplicationNameAndVersion());
       final Image imageIcon = getImageIcon();
       if (imageIcon != null) {
          _frame.setIconImage(imageIcon);
       }
       // _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
       _frame.setSize(initialDimension());
 
       _frame.addWindowListener(new WindowAdapter() {
          @Override
          public void windowClosing(final WindowEvent event) {
             exit();
          }
       });
 
       _frame.add(this, BorderLayout.CENTER);
       // frame.pack();
 
       _frame.setLocationRelativeTo(null);
       _frame.setVisible(true);
       init();
       start();
       // _frame.pack();
 
       //      final Dimension parentSize = Toolkit.getDefaultToolkit().getScreenSize();
       //      final int x = (parentSize.width - getSize().width) / 2;
       //      final int y = (parentSize.height - getSize().height) / 2;
       //      _frame.setLocation(x, y);
 
       _wwGLCanvas.requestFocus();
       _wwGLCanvas.requestFocusInWindow();
    }
 
 
    private String getApplicationNameAndVersion() {
       final String version = getApplicationVersion();
       if ((version == null) || version.isEmpty()) {
          return getApplicationName();
       }
       return getApplicationName() + " (" + version + ")";
    }
 
 
    /**
     * Initialize the GUI layout, by default only the WorldWindowGLCanvas is visible on CENTER.
     * 
     * Overwrite to implements alternative layouts (don't forget to put the WorldWindowGLCanvas into the new layout)
     * 
     * @param frame
     *           the container frame (if any), null in applets
     */
    protected final void initGUI() {
 
 
       final Container contentPane = getContentPane();
 
       _menubar = createMenuBar();
       setJMenuBar(_menubar);
 
       _toolbar = createToolbar();
       contentPane.add(_toolbar, BorderLayout.NORTH);
 
       final Component leftPane = createLeftPane();
       if (leftPane == null) {
          contentPane.add(_wwGLCanvas, BorderLayout.CENTER);
       }
       else {
          final JSplitPane splitPane = createSplitPane(leftPane);
          contentPane.add(splitPane, BorderLayout.CENTER);
       }
 
    }
 
 
    private JMenuBar createMenuBar() {
       final JMenuBar menubar = new JMenuBar();
 
       final Set<IGenericAction.MenuArea> neededMenuAreas = neededMenuAreas();
 
       if (neededMenuAreas.contains(IGenericAction.MenuArea.FILE)) {
          menubar.add(createFileMenu());
       }
 
       if (neededMenuAreas.contains(IGenericAction.MenuArea.NAVIGATION)) {
          menubar.add(createNavigationMenu());
       }
 
       if (neededMenuAreas.contains(IGenericAction.MenuArea.VIEW)) {
          menubar.add(createViewMenu());
       }
 
       if (neededMenuAreas.contains(IGenericAction.MenuArea.ANALYSIS)) {
          menubar.add(createAnalysisMenu());
       }
 
       if (neededMenuAreas.contains(IGenericAction.MenuArea.HELP)) {
          menubar.add(createHelpMenu());
       }
 
       return menubar;
    }
 
 
    private Set<IGenericAction.MenuArea> neededMenuAreas() {
       final HashSet<IGenericAction.MenuArea> result = new HashSet<IGenericAction.MenuArea>();
 
       result.add(IGenericAction.MenuArea.FILE);
       //      result.add(IGenericAction.MenuArea.NAVIGATION);
       //      result.add(IGenericAction.MenuArea.HELP);
 
       for (final IGlobeModule module : _modules) {
          final List<IGenericAction> actions = module.getGenericActions(this);
          if (actions != null) {
             for (final IGenericAction action : actions) {
                if (action.isVisible()) {
                   result.add(action.getMenuBarArea());
                }
             }
          }
       }
 
       return result;
    }
 
 
    private JMenu createFileMenu() {
       _fileMenu = new JMenu(getTranslation("File"));
       _fileMenu.setMnemonic('F');
 
       return _fileMenu;
    }
 
 
    private JMenu createViewMenu() {
       _viewMenu = new JMenu(getTranslation("View"));
       _viewMenu.setMnemonic('V');
 
       return _viewMenu;
    }
 
 
    private JMenu createAnalysisMenu() {
       _analysisMenu = new JMenu(getTranslation("Analysis"));
       _analysisMenu.setMnemonic('A');
 
       return _analysisMenu;
    }
 
 
    private JMenu createNavigationMenu() {
       _navigationMenu = new JMenu(getTranslation("Navigation"));
       _navigationMenu.setMnemonic('N');
 
       return _navigationMenu;
    }
 
 
    private JMenu createHelpMenu() {
       _helpMenu = new JMenu(getTranslation("Help"));
       _helpMenu.setMnemonic('H');
 
       //      final JMenuItem aboutItem = GSwingUtils.createMenuItem("About", getIcon("about.png"), 'h', new ActionListener() {
       //         public void actionPerformed(final ActionEvent e) {
       //            showAbout();
       //         }
       //      });
       //      _helpMenu.add(aboutItem);
 
       return _helpMenu;
    }
 
 
    protected void showAbout() {
       logWarning("showAbout() not yet implemented");
    }
 
 
    @Override
    public void logInfo(final String msg) {
       if (GUtils.isDevelopment()) {
          LOGGER.info("Globe: " + msg);
       }
    }
 
 
    @Override
    public void logWarning(final String msg) {
       LOGGER.warning("Globe: " + msg);
    }
 
 
    @Override
    public void logSevere(final String msg) {
       LOGGER.severe("Globe: " + msg);
    }
 
 
    @Override
    public void logSevere(final Throwable e) {
       //LOGGER.severe(e);
       logSevere("", e);
    }
 
 
    @Override
    public void logSevere(final String msg,
                          final Throwable e) {
       LOGGER.severe("Globe: " + msg, e);
    }
 
 
    private JToolBar createToolbar() {
       final JToolBar toolbar = new JToolBar();
       toolbar.setFloatable(false);
 
       return toolbar;
    }
 
 
    @Override
    public void goTo(final Position position,
                     final double elevation) {
       getView().goTo(position, elevation);
    }
 
 
    @Override
    public void goTo(final Position position,
                     final Angle heading,
                     final Angle pitch,
                     final double elevation) {
       if ((heading == null) || (pitch == null)) {
          getView().goTo(position, elevation);
       }
       else {
          try {
             if (this.getView() instanceof GCustomView) {
                final GCustomView view = (GCustomView) this.getView();
 
                final GCustomViewInputHandler customViewInputHandler = (GCustomViewInputHandler) view.getViewInputHandler();
 
                customViewInputHandler.stopAnimators();
                customViewInputHandler.addPanToAnimator(position, heading, pitch, elevation, true);
                //               final int TODO_RESTORE_ANIMATIONS;
                //               view.setCenterPosition(new Position(position.latitude, position.longitude, view.getGlobe().getElevation(
                //                        position.latitude, position.longitude)));
                //               view.setZoom(elevation);
 
                redraw();
 
             }
             else {
                final OrbitView view = (OrbitView) this.getView();
 
                final OrbitViewInputHandler orbitViewInputHandler = (OrbitViewInputHandler) view.getViewInputHandler();
 
                orbitViewInputHandler.stopAnimators();
                orbitViewInputHandler.addPanToAnimator(position, heading, pitch, elevation, true);
 
                redraw();
             }
          }
          catch (final ClassCastException e) {
             logSevere(e);
             getView().goTo(position, elevation);
          }
       }
    }
 
 
    public void jumpTo(final Position position,
                       final double elevation) {
       if (this.getView() instanceof GCustomView) {
          final GCustomView view = (GCustomView) this.getView();
          view.jumpTo(position, elevation);
       }
       else {
          this.getView().goTo(position, elevation);
       }
    }
 
 
    public void jumpTo(final Position position,
                       final Angle heading,
                       final Angle pitch,
                       final double elevation) {
       if ((heading == null) || (pitch == null)) {
          getView().goTo(position, elevation);
       }
       else {
          try {
             if (this.getView() instanceof GCustomView) {
                final GCustomView view = (GCustomView) this.getView();
 
                final GCustomViewInputHandler customViewInputHandler = (GCustomViewInputHandler) view.getViewInputHandler();
 
                customViewInputHandler.stopAnimators();
                view.setCenterPosition(new Position(position.latitude, position.longitude, view.getGlobe().getElevation(
                         position.latitude, position.longitude)));
                view.setZoom(elevation);
 
                redraw();
 
             }
             else {
                final OrbitView view = (OrbitView) this.getView();
 
                final OrbitViewInputHandler orbitViewInputHandler = (OrbitViewInputHandler) view.getViewInputHandler();
 
                orbitViewInputHandler.stopAnimators();
                view.setCenterPosition(new Position(position.latitude, position.longitude, view.getGlobe().getElevation(
                         position.latitude, position.longitude)));
                view.setZoom(elevation);
 
                redraw();
             }
          }
          catch (final ClassCastException e) {
             logSevere(e);
             getView().goTo(position, elevation);
          }
       }
    }
 
 
    private JSplitPane createSplitPane(final Component leftPane) {
       final JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
       splitPane.setContinuousLayout(true);
       splitPane.setOneTouchExpandable(true);
 
       splitPane.setDividerLocation(Math.round(initialDimension().width * getLeftPanelWidthRatio()) + splitPane.getInsets().left);
       // splitPane.setDividerLocation(0.15);
 
       splitPane.setLeftComponent(leftPane);
       splitPane.setRightComponent(getWorldWindowGLCanvas());
 
       return splitPane;
    }
 
 
    protected float getLeftPanelWidthRatio() {
       return 0.2f;
    }
 
 
    private Component createLeftPane() {
       final ArrayList<GPair<String, Component>> allPanels = new ArrayList<GPair<String, Component>>();
 
       for (final IGlobeModule module : _modules) {
          final List<GPair<String, Component>> modulePanels = module.getPanels(this);
          if (modulePanels != null) {
             allPanels.addAll(modulePanels);
          }
       }
 
       final Collection<? extends GPair<String, Component>> applicationPanels = getApplicationPanels();
       if (applicationPanels != null) {
          allPanels.addAll(applicationPanels);
       }
 
       if (allPanels.isEmpty()) {
          return null;
       }
 
       if (allPanels.size() == 1) {
          return allPanels.get(0)._second;
       }
 
       final JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
       tabbedPane.setBorder(BorderFactory.createEmptyBorder());
       tabbedPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
 
       for (final GPair<String, Component> modulePanel : allPanels) {
          tabbedPane.addTab(getTranslation(modulePanel._first), modulePanel._second);
       }
 
       return tabbedPane;
    }
 
 
    protected List<GPair<String, Component>> getApplicationPanels() {
       return null;
    }
 
 
    @Override
    public WorldWindowGLCanvas getWorldWindowGLCanvas() {
       return _wwGLCanvas;
    }
 
 
    @Override
    public Model getModel() {
       return getWorldWindowGLCanvas().getModel();
    }
 
 
    protected void exit() {
       logInfo("Closing " + getApplicationNameAndVersion() + "...");
 
       finalizeModules();
       stop();
       destroy();
       System.exit(0);
    }
 
 
    private void finalizeModules() {
       for (final IGlobeModule module : _modules) {
          module.finalize(this);
       }
    }
 
 
    @Override
    public JFrame getFrame() {
       return _frame;
    }
 
 
    @Override
    public View getView() {
       return getWorldWindowGLCanvas().getView();
    }
 
 
    @Override
    public Globe getGlobe() {
       return getModel().getGlobe();
    }
 
 
    @Override
    public LayerList getLayerList() {
       return getModel().getLayers();
    }
 
 
    @Override
    public List<IGlobeLayer> getGlobeLayers() {
       final List<IGlobeLayer> result = new ArrayList<IGlobeLayer>();
 
       final LayerList layerList = getLayerList();
       for (int i = 0; i < layerList.size(); i++) {
          final Layer candidate = layerList.get(i);
          if (candidate instanceof IGlobeLayer) {
             final IGlobeLayer globeLayer = (IGlobeLayer) candidate;
             result.add(globeLayer);
          }
       }
 
       return Collections.unmodifiableList(result);
    }
 
 
    public SectorGeometryList getTerrain() {
       return getSceneController().getTerrain();
    }
 
 
    public SceneController getSceneController() {
       return getWorldWindowGLCanvas().getSceneController();
    }
 
 
    @Override
    public void redraw() {
       firePropertyChange(AVKey.LAYER, null, this);
    }
 
 
    protected abstract String getApplicationName();
 
 
    protected abstract String getApplicationVersion();
 
 
    protected abstract Image getImageIcon();
 
 
    protected Map<String, Map<String, String>> initializeTranslations() {
       final HashMap<String, Map<String, String>> translations = new HashMap<String, Map<String, String>>();
 
 
       final HashMap<String, String> spanish = new HashMap<String, String>();
       spanish.put("Exit", "Salir");
       spanish.put("File", "Archivo");
       spanish.put("View", "Vista");
       spanish.put("Analysis", "Análisis");
       spanish.put("Navigation", "Navegación");
       spanish.put("Help", "Ayuda");
       translations.put("es", spanish);
 
 
       final HashMap<String, String> german = new HashMap<String, String>();
       german.put("Exit", "Verlassen");
       german.put("File", "Datei");
       german.put("View", "Ansicht");
       german.put("Analysis", "Analyse");
       german.put("Navigation", "Navigation");
       german.put("Help", "Hilfe");
       translations.put("de", german);
 
 
       return translations;
    }
 
 
    @Override
    public String getTranslation(final String string) {
       return getTranslation(_currentLanguage, string);
    }
 
 
    @Override
    public String getTranslation(final String language,
                                 final String string) {
       if (string == null) {
          return null;
       }
 
       final Map<String, String> translations = _translationsSets.get(language);
       if (translations == null) {
          if (!language.equals("en")) {
             logWarning("Can't find a translations-set for language \"" + language + "\"");
          }
          return string;
       }
 
       final String translation = translations.get(string);
       if (translation == null) {
          logWarning("Can't find a translation for \"" + string + "\" in language \"" + language + "\"");
          //         new Exception().printStackTrace();
          return string;
       }
 
       return translation;
    }
 
 
    @Override
    public String getCurrentLanguage() {
       return _currentLanguage;
    }
 
 
    @Override
    public void setCurrentLanguage(final String currentLanguage) {
       if (_currentLanguage.equals(currentLanguage)) {
          return;
       }
 
       _currentLanguage = currentLanguage;
    }
 
 
    @Override
    public void addTranslation(final String language,
                               final String string,
                               final String translation) {
       Map<String, String> translations = _translationsSets.get(language);
       if (translations == null) {
          translations = new HashMap<String, String>();
          _translationsSets.put(language, translations);
       }
       translations.put(string, translation);
    }
 
 
    @Override
    public void zoomToSector(final Sector sector) {
       if (sector == null) {
          return;
       }
 
       final View view = getView();
       if (view == null) {
          return;
       }
 
       final Globe globe = getGlobe();
       if (globe == null) {
          return;
       }
 
       final double w = 0.5 * sector.getDeltaLonRadians() * globe.getEquatorialRadius();
       final double altitude = w / view.getFieldOfView().tanHalfAngle();
       goTo(new Position(sector.getCentroid(), 0), Angle.ZERO, Angle.ZERO, altitude);
    }
 
 
    public I3DContentCollectionLayer getContentCollectionLayer() {
       for (final Layer layer : this.getLayerList()) {
          if (layer instanceof I3DContentCollectionLayer) {
             return (I3DContentCollectionLayer) layer;
          }
          continue;
       }
       return null;
    }
 
 
 }
