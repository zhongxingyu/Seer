 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.gui.viewer;
 
 import java.applet.AppletContext;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ResourceBundle;
 
 import javax.swing.AbstractAction;
 import javax.swing.ButtonGroup;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JRootPane;
 import javax.swing.JScrollPane;
 import javax.swing.JToggleButton;
 import javax.swing.JToolBar;
 import javax.swing.JTree;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.filechooser.FileNameExtensionFilter;
 import javax.swing.origamist.BackgroundImageSupport;
 import javax.swing.origamist.BackgroundImageSupport.BackgroundRepeat;
 import javax.swing.origamist.DropDownButton;
 import javax.swing.origamist.JToolBarWithBgImage;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreePath;
 import javax.xml.bind.JAXBException;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.util.DefaultUnitConverter;
 
 import cz.cuni.mff.peckam.java.origamist.exceptions.UnsupportedDataFormatException;
 import cz.cuni.mff.peckam.java.origamist.files.Listing;
 import cz.cuni.mff.peckam.java.origamist.files.ObjectFactory;
 import cz.cuni.mff.peckam.java.origamist.gui.CommonGui;
 import cz.cuni.mff.peckam.java.origamist.gui.DiagramRenderer;
 import cz.cuni.mff.peckam.java.origamist.gui.listing.ListingTree;
 import cz.cuni.mff.peckam.java.origamist.gui.listing.ListingTreeSelectionListener;
 import cz.cuni.mff.peckam.java.origamist.logging.GUIAppender;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.services.ServiceLocator;
 import cz.cuni.mff.peckam.java.origamist.services.TooltipFactory;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ListingHandler;
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.OrigamiHandler;
 import cz.cuni.mff.peckam.java.origamist.utils.ExportFormat;
 
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
 
     /** The scroll pane containing the file listing. */
     protected JScrollPane     fileListingScrollPane       = null;
 
     /** If true, show the sidebar with open files. Do not access directly, use setter/getter. */
     protected boolean         showFileListing             = false;
 
     /** The renderer used to render the diagrams. */
     protected DiagramRenderer renderer                    = null;
 
     /** The panel that shows info about the model. */
     protected ModelInfoPanel  modelInfo                   = null;
 
     /** The main menu component. */
     protected JComponent      mainToolbar                 = null;
 
     /** The bootstrapper that has started this applet, or <code>null</code>, if it has not been bootstrapped. */
     protected JApplet         bootstrap                   = null;
 
     /**
      * Instantiate the origami viewer without a bootstrapper.
      */
     public OrigamiViewer()
     {
         this(null);
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
 
             fileListing = new ListingTree(filesToDisplay);
             fileListing.setExpandsSelectedPaths(true);
             DefaultMutableTreeNode root = (DefaultMutableTreeNode) fileListing.getModel().getRoot();
             @SuppressWarnings("rawtypes")
             Enumeration bfNodeEnum = root.breadthFirstEnumeration();
             DefaultMutableTreeNode toSelect = null;
             boolean wasFound = false;
             while ((toSelect = (DefaultMutableTreeNode) bfNodeEnum.nextElement()) != null) {
                 if (toSelect.getUserObject() instanceof cz.cuni.mff.peckam.java.origamist.files.File) {
                     wasFound = true;
                     break;
                 }
             }
             if (toSelect != null && wasFound)
                 fileListing.setSelectionPath(new TreePath(toSelect.getPath()));
 
             // we intentionally add the selection listener just after the first selection has been performed
             fileListing.addTreeSelectionListener(new ListingTreeSelectionListener());
 
             fileListingScrollPane = new JScrollPane(fileListing);
             fileListingScrollPane.setPreferredSize(new Dimension(DefaultUnitConverter.getInstance().dialogUnitXAsPixel(
                     170, fileListing), 0));
 
             if (toSelect != null && wasFound) {
                 displayedOrigami = ((cz.cuni.mff.peckam.java.origamist.files.File) toSelect.getUserObject())
                         .getOrigami();
             } else {
                 // if the iterator should be empty, then handleAppletParams will die with an exception
                 // we must access this property directly, because we don't want to fire events before the GUI is
                 // properly setup.
                 displayedOrigami = filesToDisplay.recursiveFileIterator().next().getOrigami();
             }
 
             modelInfo = new ModelInfoPanel(displayedOrigami);
             addPropertyChangeListener("displayedOrigami", new PropertyChangeListener() {
                 @Override
                 public void propertyChange(PropertyChangeEvent evt)
                 {
                     modelInfo.setOrigami(displayedOrigami);
                 }
             });
 
             renderer = new DiagramRenderer(displayedOrigami, displayedOrigami.getModel().getSteps().getStep().get(0));
             renderer.setPreferredSize(new Dimension(500, 500));
             addPropertyChangeListener("displayMode", new PropertyChangeListener() {
                 @Override
                 public void propertyChange(final PropertyChangeEvent evt)
                 {
                     SwingUtilities.invokeLater(new Runnable() {
                         @Override
                         public void run()
                         {
                             renderer.setDisplayMode((DisplayMode) evt.getNewValue());
                         }
                     });
                 }
             });
 
             mainToolbar = createMainToolbar();
         } catch (UnsupportedDataFormatException e) {
             Logger.getLogger("viewer").fatal(
                     appMessages.getString("exception.UnsupportedDataFormatException.loadModel"), e);
         } catch (IOException e) {
             Logger.getLogger("viewer").fatal(appMessages.getString("excpetion.IOException.loadModel"), e);
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
         Container pane = getContentPane();
        // the "0:grow" items in the layout specification says that the preferred size of the component is the maximum
        // available space and no minimum constraints are applied
        pane.setLayout(new FormLayout("min(pref;200dlu),0:grow", "pref,fill:0:grow,pref"));
         CellConstraints cc = new CellConstraints();
         pane.add(mainToolbar, cc.xyw(1, 1, 2));
         pane.add(fileListingScrollPane, cc.xy(1, 2));
         pane.add(modelInfo, cc.xy(1, 3));
         pane.add(renderer, cc.xywh(2, 2, 1, 2));
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
                     && (filesToDisplay.getCategories() == null || (filesToDisplay.getCategories())
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
 
     /**
      * Return the component that is to be used as the main menu.
      * 
      * Also register all provided accelerators. Currently implemented by a JToolbar.
      * 
      * @return The component that is to be used as the main menu.
      */
     protected JComponent createMainToolbar()
     {
         JToolBarWithBgImage toolbar = new JToolBarWithBgImage();
         toolbar.setFloatable(false);
         toolbar.setBackground(new Color(231, 231, 184, 230));
         toolbar.setBackgroundImage(new BackgroundImageSupport(getClass()
                 .getResource("/resources/images/tooltip-bg.png"), toolbar, 0, 0, BackgroundRepeat.REPEAT_X));
 
         DropDownButton dropDown = toolbar.createToolbarDropdownButton(null, "menu.save", "save.png");
         toolbar.add(dropDown);
 
         dropDown.addComponent(toolbar.createToolbarDropdownSeparator("menu.separator.editable"));
         dropDown.addComponent(toolbar.createToolbarDropdownItem(new ExportAction(ExportFormat.XML), "menu.save.asXML",
                 "xml.png"));
         dropDown.addComponent(toolbar.createToolbarDropdownItem(new ExportAction(ExportFormat.SVG), "menu.save.asSVG",
                 "svg.png"));
 
         dropDown.addComponent(toolbar.createToolbarDropdownSeparator("menu.separator.non-editable"));
         dropDown.addComponent(toolbar.createToolbarDropdownItem(new ExportAction(ExportFormat.PDF), "menu.save.asPDF",
                 "pdf.png"));
         dropDown.addComponent(toolbar.createToolbarDropdownItem(new ExportAction(ExportFormat.PNG), "menu.save.asPNG",
                 "png.png"));
 
         dropDown.addComponent(toolbar.createToolbarDropdownSeparator("menu.separator.listing"));
 
         final JMenuItem listingItem;
         dropDown.addComponent(listingItem = toolbar.createToolbarDropdownItem(new ExportListingAction(),
                 "menu.save.listing", "listing.png"));
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
         toolbar.add(displayDiagram = toolbar.createToolbarItem(new JToggleButton(), new DisplayModeAction(
                 DisplayMode.DIAGRAM), "menu.display.diagram", "view-diagram.png"));
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
         toolbar.add(displayPage = toolbar.createToolbarItem(new JToggleButton(),
                 new DisplayModeAction(DisplayMode.PAGE), "menu.display.page", "view-page.png"));
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
 
         toolbar.add(toolbar.createToolbarButton(new ZoomInAction(), "menu.zoom.in", "zoom-in.png"));
         toolbar.add(toolbar.createToolbarButton(new ZoomOutAction(), "menu.zoom.out", "zoom-out.png"));
 
         toolbar.add(new JToolBar.Separator());
 
         final JButton diagramPrev;
         toolbar.add(diagramPrev = toolbar.createToolbarButton(new PrevOrigamiAction(), "menu.prevDiagram", "left.png"));
         TreeSelectionListener prevListener = new TreeSelectionListener() {
             @Override
             public void valueChanged(TreeSelectionEvent evt)
             {
                 try {
                     diagramPrev.setEnabled(((DefaultMutableTreeNode) fileListing.getSelectionPath()
                             .getLastPathComponent()).getPreviousLeaf() != null);
                 } catch (NullPointerException e) {}
             }
         };
         fileListing.addTreeSelectionListener(prevListener);
         prevListener.valueChanged(new TreeSelectionEvent(this, null, true, null, null));
 
         final JButton diagramNext;
         toolbar.add(diagramNext = toolbar.createToolbarButton(new NextOrigamiAction(), "menu.nextDiagram", "right.png"));
         TreeSelectionListener nextListener = new TreeSelectionListener() {
             @Override
             public void valueChanged(TreeSelectionEvent evt)
             {
                 try {
                     diagramNext.setEnabled(((DefaultMutableTreeNode) fileListing.getSelectionPath()
                             .getLastPathComponent()).getNextLeaf() != null);
                 } catch (NullPointerException e) {}
             }
         };
         fileListing.addTreeSelectionListener(nextListener);
         nextListener.valueChanged(new TreeSelectionEvent(this, null, true, null, null));
 
         toolbar.add(new JToolBar.Separator());
 
         toolbar.add(toolbar.createToolbarButton(new SettingsAction(), "menu.settings", "settings.png"));
 
         return toolbar;
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
         this.firePropertyChangeEvenIfNewValIsEqual("displayedOrigami", oldVal, displayedOrigami);
         renderer.setOrigami(displayedOrigami);
     }
 
     /**
      * Fires the property changed event even if the new value <code>.equals()</code> the old value.
      * 
      * @param property The property name.
      * @param oldVal Old value.
      * @param newVal New value.
      */
     protected void firePropertyChangeEvenIfNewValIsEqual(String property, Object oldVal, Object newVal)
     {
         for (PropertyChangeListener l : getPropertyChangeListeners(property)) {
             l.propertyChange(new PropertyChangeEvent(this, property, oldVal, newVal));
         }
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
 
     /**
      * Sets the previous origami as the current origami.
      * 
      * @author Martin Pecka
      */
     class PrevOrigamiAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -768961575848838395L;
 
         @Override
         public void actionPerformed(ActionEvent evt)
         {
             TreePath currPath = fileListing.getSelectionPath();
             try {
                 TreePath newPath = new TreePath(((DefaultMutableTreeNode) currPath.getLastPathComponent())
                         .getPreviousLeaf().getPath());
                 fileListing.setSelectionPath(newPath);
             } catch (NullPointerException e) {}
         }
     }
 
     /**
      * Sets the next origami as the current origami.
      * 
      * @author Martin Pecka
      */
     class NextOrigamiAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = -768961575848838395L;
 
         @Override
         public void actionPerformed(ActionEvent evt)
         {
             TreePath currPath = fileListing.getSelectionPath();
             try {
                 TreePath newPath = new TreePath(((DefaultMutableTreeNode) currPath.getLastPathComponent())
                         .getNextLeaf().getPath());
                 fileListing.setSelectionPath(newPath);
             } catch (NullPointerException e) {}
         }
     }
 
     /**
      * Exports the currently displayed origami to the given format.
      * 
      * @author Martin Pecka
      */
     class ExportAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -399462365929673938L;
 
         /** The format to export to. */
         protected ExportFormat    format;
 
         /**
          * @param format The format to export to.
          */
         public ExportAction(ExportFormat format)
         {
             this.format = format;
         }
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             JFileChooser chooser = new JFileChooser();
             File defaultFile = ServiceLocator.get(ConfigurationManager.class).get().getLastExportPath().getParentFile();
             chooser.setCurrentDirectory(defaultFile);
             chooser.setFileFilter(new FileNameExtensionFilter("*." + format.toString().toLowerCase(), format.toString()));
             chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             chooser.setDialogType(JFileChooser.SAVE_DIALOG);
             chooser.setApproveButtonText(appMessages.getString("exportDialog.approve"));
             chooser.setApproveButtonMnemonic(KeyStroke.getKeyStroke(
                     appMessages.getString("exportDialog.approve.mnemonic")).getKeyCode());
             chooser.setApproveButtonToolTipText(ServiceLocator.get(TooltipFactory.class).getDecorated(
                     appMessages.getString("exportDialog.approve.tooltip.message"),
                     appMessages.getString("exportDialog.approve.tooltip.title"), "save.png",
                     KeyStroke.getKeyStroke("alt " + appMessages.getString("exportDialog.approve.mnemonic"))));
             if (chooser.showDialog(OrigamiViewer.this, null) == JFileChooser.APPROVE_OPTION) {
                 File f = chooser.getSelectedFile();
                 if (!chooser.accept(f)) {
                     f = new File(f.toString() + "." + format.toString().toLowerCase());
                 }
                 ServiceLocator.get(ConfigurationManager.class).get().setLastExportPath(f);
 
                 if (f.exists()) {
                     OrigamiViewer.this.format.applyPattern(appMessages.getString("exportDialog.overwrite"));
                     if (JOptionPane.showConfirmDialog(OrigamiViewer.this,
                             OrigamiViewer.this.format.format(new Object[] { f }),
                             appMessages.getString("exportDialog.overwrite.title"), JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE, null) != JOptionPane.YES_OPTION) {
                         return;
                     }
                 }
 
                 try {
                     ServiceLocator.get(OrigamiHandler.class).export(displayedOrigami, f, format);
                     OrigamiViewer.this.format.applyPattern(appMessages.getString("exportSuccessful.message"));
                     JOptionPane.showMessageDialog(getRootPane(),
                             OrigamiViewer.this.format.format(new Object[] { f.toString() }),
                             appMessages.getString("exportSuccessful.title"), JOptionPane.INFORMATION_MESSAGE, null);
                 } catch (IOException e1) {
                     OrigamiViewer.this.format.applyPattern(appMessages.getString("failedToExport.message"));
                     JOptionPane.showMessageDialog(getRootPane(),
                             OrigamiViewer.this.format.format(new Object[] { f.toString() }),
                             appMessages.getString("failedToExport.title"), JOptionPane.ERROR_MESSAGE, null);
                     Logger.getLogger("application").warn("Unable to export origami.", e1);
                 }
             }
         }
 
     }
 
     /**
      * Exports the current listing.
      * 
      * @author Martin Pecka
      */
     class ExportListingAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -904576077867527286L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             // ask the user for the base URI for URL relativization
             boolean relativeBaseCorrect = false;
             URI relativeBase = null;
             URI modelRelativeBase = null;
             try {
                 // if you would just use URL.toString(), URL encoding would be performed on non-ASCII characters in the
                 // path, which we don't want to happen
                 modelRelativeBase = new File(ServiceLocator.get(OrigamiHandler.class).getDocumentBase().toURI())
                         .getParentFile().toURI();
             } catch (URISyntaxException e2) {}
             String defaultPath = (modelRelativeBase != null ? modelRelativeBase.toString() : null);
 
             while (!relativeBaseCorrect) {
                 String base = (String) JOptionPane.showInputDialog(rootPane,
                         appMessages.getString("exportListing.selectBase"),
                         appMessages.getString("exportListing.selectBase.title"), JOptionPane.QUESTION_MESSAGE, null,
                         null, defaultPath);
                 if (base == null) {
                     // relativeBase = null; // is already done
                     relativeBaseCorrect = true;
                 } else {
                     defaultPath = base;
                     try {
                         relativeBase = new URI(base);
                         relativeBaseCorrect = true;
                     } catch (URISyntaxException e1) {
                         JOptionPane.showMessageDialog(rootPane,
                                 appMessages.getString("exportListing.selectBase.invalidURI"),
                                 appMessages.getString("exportListing.selectBase.invalidURI.title"),
                                 JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
 
             // ask the user for the file name
             JFileChooser chooser = new JFileChooser();
             File defaultDir = ServiceLocator.get(ConfigurationManager.class).get().getLastExportPath().getParentFile();
             File defaultFile = new File(defaultDir, "listing.xml");
             chooser.setSelectedFile(defaultFile);
             chooser.setFileFilter(new FileNameExtensionFilter("listing.xml", "xml"));
             chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
             chooser.setDialogType(JFileChooser.SAVE_DIALOG);
             chooser.setApproveButtonText(appMessages.getString("exportListingDialog.approve"));
             chooser.setApproveButtonMnemonic(KeyStroke.getKeyStroke(
                     appMessages.getString("exportListingDialog.approve.mnemonic")).getKeyCode());
             chooser.setApproveButtonToolTipText(ServiceLocator.get(TooltipFactory.class).getDecorated(
                     appMessages.getString("exportListingDialog.approve.tooltip.message"),
                     appMessages.getString("exportListingDialog.approve.tooltip.title"), "save.png",
                     KeyStroke.getKeyStroke("alt " + appMessages.getString("exportListingDialog.approve.mnemonic"))));
             if (chooser.showDialog(OrigamiViewer.this, null) == JFileChooser.APPROVE_OPTION) {
                 File f = chooser.getSelectedFile();
                 ServiceLocator.get(ConfigurationManager.class).get().setLastExportPath(f);
                 if (!chooser.accept(f)) {
                     f = new File(f, "listing.xml");
                 }
 
                 if (f.exists()) {
                     OrigamiViewer.this.format.applyPattern(appMessages.getString("exportListingDialog.overwrite"));
                     if (JOptionPane.showConfirmDialog(OrigamiViewer.this,
                             OrigamiViewer.this.format.format(new Object[] { f }),
                             appMessages.getString("exportListingDialog.overwrite.title"), JOptionPane.YES_NO_OPTION,
                             JOptionPane.QUESTION_MESSAGE, null) != JOptionPane.YES_OPTION) {
                         return;
                     }
                 }
 
                 try {
                     try {
                         ServiceLocator.get(ListingHandler.class).export(filesToDisplay, f, relativeBase,
                                 modelRelativeBase);
                         OrigamiViewer.this.format
                                 .applyPattern(appMessages.getString("exportListingSuccessful.message"));
                         JOptionPane.showMessageDialog(getRootPane(),
                                 OrigamiViewer.this.format.format(new Object[] { f.toString() }),
                                 appMessages.getString("exportListingSuccessful.title"),
                                 JOptionPane.INFORMATION_MESSAGE, null);
                     } catch (JAXBException e1) {
                         throw new IOException(e1);
                     }
                 } catch (IOException e1) {
                     OrigamiViewer.this.format.applyPattern(appMessages.getString("failedToExportListing.message"));
                     JOptionPane.showMessageDialog(getRootPane(),
                             OrigamiViewer.this.format.format(new Object[] { f.toString() }),
                             appMessages.getString("failedToExportListing.title"), JOptionPane.ERROR_MESSAGE, null);
                     Logger.getLogger("application").warn("Unable to export listing.", e1);
                 }
             }
         }
     }
 
     /**
      * Zooms all the displayed steps by 10% in.
      * 
      * @author Martin Pecka
      */
     class ZoomInAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = 2401463300217345719L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             renderer.incZoom();
         }
 
     }
 
     /**
      * Zooms all the displayed steps by 10% out.
      * 
      * @author Martin Pecka
      */
     class ZoomOutAction extends AbstractAction
     {
         /** */
         private static final long serialVersionUID = 6762119407937700068L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             renderer.decZoom();
         }
 
     }
 
     /**
      * Shows the settings dialog.
      * 
      * @author Martin Pecka
      */
     class SettingsAction extends AbstractAction
     {
 
         /** */
         private static final long serialVersionUID = -583073126579360879L;
 
         @Override
         public void actionPerformed(ActionEvent e)
         {
             new SettingsFrame().setVisible(true);
         }
 
     }
 
 }
