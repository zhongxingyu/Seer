 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.viewer;
 
 import java.applet.AppletContext;
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.swing.AbstractAction;
 import javax.swing.AbstractButton;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.DropDownButton;
 import javax.swing.ImageIcon;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTitledSeparator;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.tree.DefaultMutableTreeNode;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.jgoodies.forms.util.DefaultUnitConverter;
 
 import cz.cuni.mff.peckam.java.origamist.exceptions.UnsupportedDataFormatException;
 import cz.cuni.mff.peckam.java.origamist.files.Categories;
 import cz.cuni.mff.peckam.java.origamist.files.Listing;
 import cz.cuni.mff.peckam.java.origamist.files.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.gui.CommonGui;
 import cz.cuni.mff.peckam.java.origamist.gui.DiagramRenderer;
 import cz.cuni.mff.peckam.java.origamist.gui.listing.ListingTree;
 import cz.cuni.mff.peckam.java.origamist.logging.GUIAppender;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.Step;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ListingHandler;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.OrigamiHandler;
 
 /**
  * The viewer of the origami model. <br />
  * <br />
  * The applet recognizes these parameters:<br />
  * 
  * <code>files</code> (<b>required</b>):
  * <ul>
  * <li>either a string containing either a location of <code>listing.xml</code>, or</li>
  * <li>a list of files and directories.</li>
  * </ul>
  * <code>recursive</code>: Effective only if <code>files</code> contain some directories.
  * <ul>
  * <li>If not set, no files from subdirectories will be loaded (but direct children of the specified folders will be
  * loaded).</li>
  * <li>If set to <code>recursive</code>, then all subdirectories will be searched for models.</li>
  * <li>If set to a number, the number means the depth od subdirectories to search in.
  * <em>Please notice, that the value <code>0</code> means that only the specified files will be loaded and the specified directories will be ignored!</em>
  * </li>
  * </ul>
  * <code>displayMode</code>:
  * <ul>
  * <li>either <code>page</code> (displays a bunch of steps at once) or</li>
  * <li><code>diagram</code> (displays only one step at once). Defaults to <code>page</code>.</li>
  * <li>when not set, defaults to <code>page</code></li>
  * </ul>
  * <code>modelDownloadMode</code>:
  * <ul>
  * <li>if set to <code>all</code>, all contents of all models defined in <code>files</code> will be downloaded at
  * startup.</li>
  * <li>If set to <code>headers</code>, only metadata for each model will be downloaded immediately and the models
  * themselves will be downloaded on demand.</li>
  * <li>If set to <code>none</code> , no files will be downloaded at startup and everything will be downloaded on-demand
  * (this means the metadata will not be available at startup).</li>
  * <li>If set to a number <code>n</code>, mode <code>all</code> will be used for first <code>n</code> files and mode
  * <code>none</code> will be used for the rest of them.</li>
  * <li>if not set, defaults to <code>10</code></li>
  * </ul>
  * 
  * Properties handled by this class (events are fired when they change - but not at startup):
  * <ul>
  * <li><code>showFileListing</code></li>
  * <li><code>displayMode</code></li>
  * <li><code>displayedOrigami</code></li>
  * </ul>
  * 
  * This applet is intended to be started using a bootstrapper that will add support for Java3D without the need to
  * install it on the client computer.
  * 
  * @author Martin Pecka
  */
 public class OrigamiViewer extends CommonGui
 {
     private static final long serialVersionUID            = -6853141518719373854L;
 
     /** Download whole models for all selected files. */
     public static int         MODEL_DOWNLOAD_MODE_ALL     = -2;
     /** Don't download models. They'll be downloaded on demand (metadata will not be accessible). */
     public static int         MODEL_DOWNLOAD_MODE_HEADERS = -1;
     /** Download only metadata for all models. */
     public static int         MODEL_DOWNLOAD_MODE_NONE    = 0;
     /**
      * Meaning of the values is listed below. The values only have effect when a whole directory or a set of files is
      * specified as the list of files to show.
      * 
      * MODEL_DOWNLOAD_MODE_ALL - Download whole models for all selected files.
      * MODEL_DOWNLOAD_MODE_NONE - Don't download models. They'll be downloaded on demand (metadata will not be
      * accessible).
      * MODEL_DOWNLOAD_MODE_HEADERS - Download only metadata for all models.
      * other positive number <code>n</code> - use the ALL mode for the first <code>n</code> files and the NONE mode
      * for all other files.
      */
     protected int             modelDownloadMode           = 10;
 
     /** Name of the file containing the listing of files to display. */
     public static String      LISTING_FILE_NAME           = "listing.xml";
 
     /** The files to be displayed by the viewer. */
     protected Listing         filesToDisplay              = null;
 
     /** The mode of displaying diagrams. Do not access directly, use setter/getter. */
     protected DisplayMode     displayMode                 = DisplayMode.PAGE;
 
     /** The origami currently displayed. Do not access directly, use setter/getter. */
     protected Origami         displayedOrigami            = null;
 
     /** The list of files that were recognized at startup. */
     protected JTree           fileListing                 = null;
 
     /** If true, show the sidebar with open files. Do not access directly, use setter/getter. */
     protected boolean         showFileListing             = false;
 
     /** The renderer used to render the diagrams. */
     protected DiagramRenderer renderer                    = null;
 
     /** The bootstrapper that has started this applet, or <code>null</code>, if it has not been bootstrapped. */
     protected JApplet         bootstrap                   = null;
 
     /**
      * Instantiate the origami viewer without a bootstrapper.
      */
     public OrigamiViewer()
     {
         super();
     }
 
     /**
      * Instanitate the origami viewer with the given bootstrapper.
      * 
      * @param bootstrap The bootstrapper that starts this applet.
      */
     public OrigamiViewer(JApplet bootstrap)
     {
         super();
         this.bootstrap = bootstrap;
     }
 
     /**
      * Create and setup all the form components.
      */
     @Override
     protected void createComponents()
     {
         try {
             handleAppletParams();
 
             // if the iterator should be empty, then handleAppletParams will die with an exception
             // we must access this property directly, because we don't want to fire events before the GUI is properly
             // setup.
             displayedOrigami = filesToDisplay.recursiveFileIterator().next().getOrigami();
 
             // TODO remove testing stuff and put some more meaningful code
             renderer = new DiagramRenderer(displayedOrigami, (Step) displayedOrigami.getModel().getSteps().getStep()
                     .get(0));
             renderer.setPreferredSize(new Dimension(500, 500));
             getContentPane().add(renderer, BorderLayout.CENTER);
 
             fileListing = new ListingTree(filesToDisplay);
             JScrollPane listingPane = new JScrollPane(fileListing);
             listingPane.setPreferredSize(new Dimension(DefaultUnitConverter.getInstance().dialogUnitXAsPixel(170,
                     fileListing), 0));
             getContentPane().add(listingPane, BorderLayout.WEST);
 
             createMainToolbar();
 
         } catch (UnsupportedDataFormatException e) {
             System.err.println(e); // TODO handle errors in data files
         } catch (IOException e) {
             System.err.println(e); // TODO handle IO errors
         } catch (IllegalArgumentException e) {
             Logger.getLogger("viewer").fatal(e.getMessage(), e);
         }
 
     }
 
     /**
      * Setup the form layout.
      */
     @Override
     protected void buildLayout()
     {
 
     }
 
     /**
      * Handles the applet parameters. Eg. loads listing.xml, or sets other settings.
      * 
      * @throws IllegalArgumentException If an argument has bad value and the continuation of the app is impossible due
      *             to it.
      */
     protected void handleAppletParams() throws IllegalArgumentException
     {
         handleStartupModeParam();
         handleModelDownloadModeParam();
         handleFilesParam();
         // the "recursive" param is handled by the previous handler
     }
 
     /**
      * Handles the "displayMode" applet parameter.
      */
     protected void handleStartupModeParam()
     {
         if (getParameter("displayMode") != null) {
             try {
                 displayMode = DisplayMode.valueOf(getParameter("displayMode").toUpperCase());
             } catch (IllegalArgumentException e) {
                 Logger.getLogger("viewer").l7dlog(Level.ERROR, "startupModeParamInvalid",
                         new Object[] { Arrays.asList(DisplayMode.values()) }, e);
                 // we just do nothing, so the default value remains set
             }
         }
     }
 
     /**
      * Handles the "modelDownloadMode" applet parameter.
      */
     protected void handleModelDownloadModeParam()
     {
         String param = getParameter("modelDownloadMode");
         if (param != null) {
             try {
                 modelDownloadMode = Integer.parseInt(param);
                 if (modelDownloadMode < 0) {
                     modelDownloadMode = MODEL_DOWNLOAD_MODE_NONE;
                     throw new NumberFormatException();
                 }
             } catch (NumberFormatException e) {
                 if (param.equalsIgnoreCase("all"))
                     modelDownloadMode = MODEL_DOWNLOAD_MODE_ALL;
                 else if (param.equalsIgnoreCase("headers"))
                     modelDownloadMode = MODEL_DOWNLOAD_MODE_HEADERS;
                 else if (param.equalsIgnoreCase("none")) {
                     modelDownloadMode = MODEL_DOWNLOAD_MODE_NONE;
                 } else {
                     Logger.getLogger("viewer").l7dlog(Level.ERROR, "modelDownloadModeParamInvalid", e);
                     // we just do nothing, so the default value remains set
                 }
             }
         }
     }
 
     /**
      * Handles the "files" applet parameter. (Also handles the "recursive" parameter, where applicable)
      * 
      * @throws IllegalArgumentException If the loaded listing would be empty or listing.xml could not be loaded.
      */
     protected void handleFilesParam() throws IllegalArgumentException
     {
         String param = getParameter("files");
         if (param == null) {
             Logger.getLogger("viewer").l7dlog(Level.FATAL, "filesParamMissing",
                     new IllegalArgumentException("The 'files' applet parameter must be set"));
             return;
         }
 
         if (param.endsWith(LISTING_FILE_NAME)) {
             showFileListing = true;
             try {
                 URL paramURL = new URL(getDocumentBase(), param);
                 // use the listing.xml location as the base for relative model URLs
                 ServiceLocator.get(OrigamiHandler.class).setDocumentBase(paramURL);
                 filesToDisplay = ServiceLocator.get(ListingHandler.class).load(paramURL);
             } catch (MalformedURLException e) {
                 ResourceBundle messages = ResourceBundle.getBundle("viewer",
                         ServiceLocator.get(ConfigurationManager.class).get().getLocale());
                 throw new IllegalArgumentException(messages.getString("filesParamInvalidListingURL"), e);
             } catch (UnsupportedDataFormatException e) {
                 ResourceBundle messages = ResourceBundle.getBundle("viewer",
                         ServiceLocator.get(ConfigurationManager.class).get().getLocale());
                 throw new IllegalArgumentException(messages.getString("filesParamInvalidListingFormat"), e);
             } catch (IOException e) {
                 ResourceBundle messages = ResourceBundle.getBundle("viewer",
                         ServiceLocator.get(ConfigurationManager.class).get().getLocale());
                 throw new IllegalArgumentException(messages.getString("filesParamInvalidListingUnread"), e);
             }
         } else {
             List<String> filesAsStrings = Arrays.asList(param.split(" "));
 
             Integer recursive;
             String recursiveParam = getParameter("recursive");
             if (recursiveParam == null) {
                 recursive = 1;
             } else if (recursiveParam.equalsIgnoreCase("recursive")) {
                 recursive = null;
             } else {
                 try {
                     recursive = Integer.parseInt(recursiveParam);
                 } catch (NumberFormatException e) {
                     recursive = 1;
                 }
             }
 
             ObjectFactory of = new ObjectFactory();
             filesToDisplay = (Listing) of.createListing();
             List<File> files = new LinkedList<File>();
             for (String fileString : filesAsStrings) {
                 try {
                     File file = new File(new URL(getDocumentBase(), fileString).toURI());
                     files.add(file);
                 } catch (MalformedURLException e) {
                     Logger.getLogger("viewer").l7dlog(Level.ERROR, "filesParamInvalidItem",
                             new Object[] { fileString }, e);
                 } catch (URISyntaxException e) {
                     Logger.getLogger("viewer").l7dlog(Level.ERROR, "filesParamInvalidItem",
                             new Object[] { fileString }, e);
                 }
             }
             filesToDisplay.addFiles(files, recursive, filesToDisplay);
 
             if ((filesToDisplay.getFiles() == null || filesToDisplay.getFiles().getFile().size() == 0)
                     && (filesToDisplay.getCategories() == null || ((Categories) filesToDisplay.getCategories())
                             .numOfFilesRecursive() == 0)) {
                 Logger.getLogger("viewer").l7dlog(Level.FATAL, "filesParamInvalidFileList", null,
                         new IllegalArgumentException("No input files defined."));
             }
 
             if (modelDownloadMode == MODEL_DOWNLOAD_MODE_HEADERS || modelDownloadMode == MODEL_DOWNLOAD_MODE_ALL
                     || modelDownloadMode > 0) {
 
                 Iterator<cz.cuni.mff.peckam.java.origamist.files.File> iterator = filesToDisplay
                         .recursiveFileIterator();
 
                 boolean onlyMetadata = modelDownloadMode == MODEL_DOWNLOAD_MODE_HEADERS;
 
                 int i = 0;
                 while (iterator.hasNext() && (modelDownloadMode == MODEL_DOWNLOAD_MODE_ALL || i++ < modelDownloadMode)) {
                     cz.cuni.mff.peckam.java.origamist.files.File file = iterator.next();
                     try {
                         file.getOrigami(onlyMetadata, false);
                         file.fillFromOrigami();
                         continue;
                     } catch (UnsupportedDataFormatException e) {
                         Logger.getLogger("viewer").l7dlog(Level.ERROR, "invalidModelFile",
                                 new Object[] { file.getSrc() }, e);
                     } catch (IOException e) {
                         Logger.getLogger("viewer").l7dlog(Level.ERROR, "modelLoadIOError",
                                 new Object[] { file.getSrc() }, e);
                     }
                     i--;
                     iterator.remove();
                     file.setParent(null);
                 }
 
                 iterator = null;
             }
 
             // we only show the file listing if two or more models are being displayed
             Iterator<cz.cuni.mff.peckam.java.origamist.files.File> it = filesToDisplay.recursiveFileIterator();
             int numOfModels = 0;
             if (it != null) {
                 while (it.hasNext()) {
                     it.next();
                     numOfModels++;
                     if (numOfModels == 2)
                         break;
                 }
             }
             it = null;
 
             // we must access this property directly, because we don't want to fire events before the GUI is properly
             // setup.
             showFileListing = numOfModels >= 2;
 
             if (numOfModels == 0) {
                 ResourceBundle messages = ResourceBundle.getBundle("viewer",
                         ServiceLocator.get(ConfigurationManager.class).get().getLocale());
                 throw new IllegalArgumentException(messages.getString("filesParamInvalidFileList"));
             }
         }
     }
 
     protected JComponent createMainToolbar()
     {
         JToolBar toolbar = new JToolBar();
         toolbar.setFloatable(false);
 
         DropDownButton dropDown = createToolbarDropdownButton(null, "menu.save", "save.png");
         toolbar.add(dropDown);
 
         dropDown.addComponent(createToolbarDropdownSeparator("menu.separator.editable"));
         dropDown.addComponent(createToolbarDropdownItem(null, "menu.save.asXML", "xml.png"));
         dropDown.addComponent(createToolbarDropdownItem(null, "menu.save.asSVG", "svg.png"));
 
         dropDown.addComponent(createToolbarDropdownSeparator("menu.separator.non-editable"));
         dropDown.addComponent(createToolbarDropdownItem(null, "menu.save.asPDF", "pdf.png"));
         dropDown.addComponent(createToolbarDropdownItem(null, "menu.save.asPNG", "png.png"));
 
         dropDown.addComponent(createToolbarDropdownSeparator("menu.separator.listing"));
 
         final JMenuItem listingItem;
         dropDown.addComponent(listingItem = createToolbarDropdownItem(null, "menu.save.listing", "listing.png"));
         addPropertyChangeListener("showFileListing", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 listingItem.setEnabled(isShowFileListing());
             }
         });
 
         toolbar.add(new JToolBar.Separator());
 
         final ButtonGroup displayGroup = new ButtonGroup();
 
         final JToggleButton displayDiagram;
         toolbar.add(displayDiagram = createToolbarItem(new JToggleButton(), new DisplayModeAction(DisplayMode.DIAGRAM),
                 "menu.display.diagram", "view-diagram.png"));
         displayGroup.add(displayDiagram);
         addPropertyChangeListener("displayMode", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (getDisplayMode() == DisplayMode.DIAGRAM)
                     displayGroup.setSelected(displayDiagram.getModel(), true);
             }
         });
 
         final JToggleButton displayPage;
         toolbar.add(displayPage = createToolbarItem(new JToggleButton(), new DisplayModeAction(DisplayMode.PAGE),
                 "menu.display.page", "view-page.png"));
         displayGroup.add(displayPage);
         addPropertyChangeListener("displayMode", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 if (getDisplayMode() == DisplayMode.PAGE)
                     displayGroup.setSelected(displayPage.getModel(), true);
             }
         });
         displayGroup.setSelected(displayPage.getModel(), true);
 
         toolbar.add(new JToolBar.Separator());
 
         toolbar.add(createToolbarButton(null, "menu.zoom.in", "zoom-in.png"));
         toolbar.add(createToolbarButton(null, "menu.zoom.out", "zoom-out.png"));
 
         toolbar.add(new JToolBar.Separator());
 
         final JButton diagramPrev;
         toolbar.add(diagramPrev = createToolbarButton(null, "menu.prevDiagram", "left.png"));
         addPropertyChangeListener("displayedOrigami", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 try {
                     diagramPrev.setEnabled(((DefaultMutableTreeNode) fileListing.getSelectionPath()
                             .getLastPathComponent()).getPreviousLeaf() != null);
                 } catch (NullPointerException e) {}
             }
         });
 
         final JButton diagramNext;
         toolbar.add(diagramNext = createToolbarButton(null, "menu.nextDiagram", "right.png"));
         addPropertyChangeListener("displayedOrigami", new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 try {
                     diagramNext.setEnabled(((DefaultMutableTreeNode) fileListing.getSelectionPath()
                             .getLastPathComponent()).getNextLeaf() != null);
                 } catch (NullPointerException e) {}
             }
         });
 
         toolbar.add(new JToolBar.Separator());
 
         toolbar.add(createToolbarButton(null, "menu.settings", "settings.png"));
 
         getContentPane().add(toolbar, BorderLayout.NORTH);
         return toolbar;
     }
 
     protected JButton createToolbarButton(Action action, final String bundleName, final String iconName)
     {
         return createToolbarItem(new JButton(), action, bundleName, iconName);
     }
 
     protected DropDownButton createToolbarDropdownButton(Action action, final String bundleName, final String iconName)
     {
         return createToolbarItem(new DropDownButton(new JButton()), action, bundleName, iconName);
     }
 
     protected JMenuItem createToolbarDropdownItem(Action action, final String bundleName, final String iconName)
     {
         JMenuItem item = new JMenuItem();
         item.setBorder(BorderFactory.createCompoundBorder(item.getBorder(), BorderFactory.createEmptyBorder(3, 0, 3, 0)));
         return createToolbarItem(item, action, bundleName, iconName);
     }
 
     protected <T extends AbstractButton> T createToolbarItem(final T button, final Action action,
             final String bundleName, final String iconName)
     {
         if (bundleName == null)
             throw new NullPointerException("Tried to create toolbar item without giving the corresponding bundle name.");
 
         button.setAction(action);
         if (iconName != null) {
             URL url = getClass().getResource("/resources/images/" + iconName);
             if (url != null)
                 button.setIcon(new ImageIcon(url));
         }
 
         PropertyChangeListener listener = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 try {
                     button.setText(appMessages.getString(bundleName));
                 } catch (MissingResourceException e) {
                     button.setText("");
                 }
 
                 try {
                     String tooltip = appMessages.getString(bundleName + ".tooltip");
                    button.setToolTipText(appMessages.getString(tooltip));
                    button.getAccessibleContext().setAccessibleDescription(button.getToolTipText());
                 } catch (MissingResourceException e) {}
 
                 try {
                     String mnemonic = appMessages.getString(bundleName + ".mnemonic");
                     KeyStroke stroke = KeyStroke.getKeyStroke(mnemonic);
                     if (stroke != null) {
 
                         if ((button instanceof JMenuItem) && !(button instanceof JMenu))
                             ((JMenuItem) button).setAccelerator(stroke);
                         button.setMnemonic(stroke.getKeyCode());
 
                         getRootPane().registerKeyboardAction(action, stroke, JComponent.WHEN_IN_FOCUSED_WINDOW);
                     }
                 } catch (MissingResourceException e) {}
             }
         };
         listener.propertyChange(new PropertyChangeEvent(this, "appMessages", null, appMessages));
         addPropertyChangeListener("appMessages", listener);
         return button;
     }
 
     protected JTitledSeparator createToolbarDropdownSeparator(final String bundleName)
     {
         final JTitledSeparator separator = new JTitledSeparator("");
 
         PropertyChangeListener listener = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 try {
                     separator.setTitle(appMessages.getString(bundleName));
                 } catch (MissingResourceException e) {
                     separator.setTitle("");
                 }
             }
         };
         listener.propertyChange(new PropertyChangeEvent(this, "appMessages", null, appMessages));
         addPropertyChangeListener("appMessages", listener);
 
         return separator;
     }
 
     @Override
     public void start()
     {
         super.start();
 
     }
 
     @Override
     public void stop()
     {
         super.stop();
     }
 
     @Override
     public void destroy()
     {
         super.destroy();
     }
 
     @Override
     protected void setupLoggers()
     {
         super.setupLoggers();
 
         Logger l = Logger.getLogger("viewer");
         l.setResourceBundle(ResourceBundle.getBundle("viewer", ServiceLocator.get(ConfigurationManager.class).get()
                 .getLocale()));
         l.setLevel(Level.ALL);
         l.addAppender(new GUIAppender(this));
     }
 
     /**
      * @return Whether to show the sidebar with open files.
      */
     public boolean isShowFileListing()
     {
         return showFileListing;
     }
 
     /**
      * @param showFileListing Whether to show the sidebar with open files.
      */
     public void setShowFileListing(boolean showFileListing)
     {
         boolean oldVal = this.showFileListing;
         this.showFileListing = showFileListing;
         this.firePropertyChange("showFileListing", oldVal, showFileListing);
     }
 
     /**
      * @return The mode of displaying diagrams.
      */
     public DisplayMode getDisplayMode()
     {
         return displayMode;
     }
 
     /**
      * @param displayMode The mode of displaying diagrams.
      */
     public void setDisplayMode(DisplayMode displayMode)
     {
         DisplayMode oldVal = this.displayMode;
         this.displayMode = displayMode;
         this.firePropertyChange("displayMode", oldVal, displayMode);
     }
 
     /**
      * @return The origami currently displayed.
      */
     public Origami getDisplayedOrigami()
     {
         return displayedOrigami;
     }
 
     /**
      * @param displayedOrigami The origami currently displayed.
      */
     public void setDisplayedOrigami(Origami displayedOrigami)
     {
         Origami oldVal = this.displayedOrigami;
         this.displayedOrigami = displayedOrigami;
         this.firePropertyChange("displayedOrigami", oldVal, displayedOrigami);
         renderer.setOrigami(displayedOrigami);
     }
 
     @Override
     protected void registerServices()
     {
         super.registerServices();
         ServiceLocator.add(OrigamiViewer.class, this);
     }
 
     // bootstrapping support
 
     @Override
     public JRootPane getRootPane()
     {
         if (bootstrap != null)
             return bootstrap.getRootPane();
         return super.getRootPane();
     }
 
     @Override
     public Container getContentPane()
     {
         if (bootstrap != null)
             return bootstrap.getContentPane();
         return super.getContentPane();
     }
 
     @Override
     public URL getDocumentBase()
     {
         if (bootstrap != null)
             return bootstrap.getDocumentBase();
         return super.getDocumentBase();
     }
 
     @Override
     public URL getCodeBase()
     {
         if (bootstrap != null)
             return bootstrap.getCodeBase();
         return super.getCodeBase();
     }
 
     @Override
     public String getParameter(String name)
     {
         if (bootstrap != null)
             return bootstrap.getParameter(name);
         return super.getParameter(name);
     }
 
     @Override
     public AppletContext getAppletContext()
     {
         if (bootstrap != null)
             return bootstrap.getAppletContext();
         return super.getAppletContext();
     }
 
     @Override
     public String getAppletInfo()
     {
         if (bootstrap != null)
             return bootstrap.getAppletInfo();
         return super.getAppletInfo();
     }
 
     /**
      * Action for changing the current display mode to the one given in constructor.
      * 
      * @author Martin Pecka
      */
     class DisplayModeAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -4054470305119745178L;
 
         DisplayMode               mode;
 
         public DisplayModeAction(DisplayMode mode)
         {
             this.mode = mode;
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             setDisplayMode(mode);
         }
     }
 
 }
