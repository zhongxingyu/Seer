 package klaue.schematic2blueprint;
 
 import java.awt.AWTEvent;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Cursor;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.AWTEventListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.awt.print.PrinterException;
 import java.awt.print.PrinterJob;
 import java.io.File;
 import java.io.IOException;
 import java.util.prefs.Preferences;
 
 import javax.imageio.ImageIO;
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComponent;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JToolBar;
 import javax.swing.KeyStroke;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import klaue.mcschematictool.ImageGridStack;
 import klaue.mcschematictool.ImageProvider;
 import klaue.mcschematictool.SchematicReader;
 import klaue.mcschematictool.SliceStack;
 import klaue.mcschematictool.exceptions.ClassicNotSupportedException;
 import klaue.mcschematictool.exceptions.ParseException;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.mcarchitect.Application;
 import com.mcarchitect.resources.IconFactory;
 
 /**
  * The main frame and initializer of the app
  * 
  * @author klaue
  * @author Fernando Marquardt
  */
 public class MainFrame extends JFrame implements ActionListener, ChangeListener {
 
     private static final long serialVersionUID = 1L;
 
     JFileChooser fc = new JFileChooser();
     SliceStack stack = null;
     ImageGridStack images = null;
     GridBagConstraints defaultContraints = new GridBagConstraints();
     double minZoom = 0.5, maxZoom = 10, currentZoom = 0, defaultZoom = 1, zoomRatio = 0.5;
     Color gridLineColor = Color.BLACK;
     Color markColor = Color.RED;
     int currentLayer = 0;
 
     MyAlmightyListener myAlmightyListener;
 
     JMenuBar menuBar = new JMenuBar();
     JMenu fileMenu, exportMenu, colorMenu, toolMenu, printMenu;
     JMenuItem miOpen, miExportImages, miExportGif, miExportLayer, miExportTxt, miBackgroundColor, miLineColor,
             miMarkColor, miBlockCounter, miPrintSlice, miPrintAll;
 
     JToolBar tbTools = new JToolBar();
 
     JButton btnOpen;
     JButton btnRotateCCW, btnRotateCW;
     JButton btnZoomIn, btnZoomOut;
 
     JSlider sldLayer = new JSlider(SwingConstants.VERTICAL);
 
     JPanel pnlAll = new JPanel();
     JPanel pnlRotate = new JPanel();
     JPanel pnlSchematic = new JPanel();
     JPanel pnlGrid = new JPanel();
     JScrollPane scrGrid;
 
     JLabel lblSize = new JLabel();
     
     private File loadingFile;
 
     public static String PREF_KEY_LAST_SELECTED_PATH = "last_selected_path";
     
     public static String PREF_KEY_RECENTLY_OPENED = "recently_opened";
 
     public MainFrame() { // Made public until further change
         try {
             ImageProvider.initialize();
         } catch (Exception e) {
             JOptionPane.showMessageDialog(null, "Could not read image files, aborting..", "Error",
                     JOptionPane.ERROR_MESSAGE);
             System.exit(1);
         }
 
         setTitle("MCArchitect");
         setSize(500, 500);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         Preferences pref = Preferences.userNodeForPackage(Application.class);
 
         String[] filetypes = { "schematic", "schematics" };
         fc.setFileFilter(new FiletypeFilter(filetypes, "Schematic Files (*.schematic;*.schematics)"));
         fc.setCurrentDirectory(new File(pref.get(PREF_KEY_LAST_SELECTED_PATH, "")));
 
         // make the menu
     	fileMenu = new JMenu("File");
         fileMenu.setMnemonic(KeyEvent.VK_F);
         fileMenu.getAccessibleContext().setAccessibleDescription("The file menu");
         populateFileMenu();
 
         colorMenu = new JMenu("Color");
         colorMenu.setMnemonic(KeyEvent.VK_C);
         colorMenu.getAccessibleContext().setAccessibleDescription("The color menu");
 
         miBackgroundColor = new JMenuItem("Background color");
         miBackgroundColor.setMnemonic(KeyEvent.VK_B);
         miBackgroundColor.getAccessibleContext().setAccessibleDescription("This changes the background color");
         miBackgroundColor.setActionCommand("COLOR_BACKGROUND");
         miBackgroundColor.addActionListener(this);
         colorMenu.add(miBackgroundColor);
 
         miLineColor = new JMenuItem("Line color");
         miLineColor.setMnemonic(KeyEvent.VK_L);
         miLineColor.getAccessibleContext().setAccessibleDescription("This changes the line color");
         miLineColor.setActionCommand("COLOR_LINE");
         miLineColor.addActionListener(this);
         colorMenu.add(miLineColor);
 
         miMarkColor = new JMenuItem("Marker color");
         miMarkColor.setMnemonic(KeyEvent.VK_M);
         miMarkColor.getAccessibleContext().setAccessibleDescription("This changes the marker color");
         miMarkColor.setActionCommand("COLOR_MARK");
         miMarkColor.addActionListener(this);
         colorMenu.add(miMarkColor);
 
         menuBar.add(colorMenu);
 
         toolMenu = new JMenu("Tools");
         toolMenu.setMnemonic(KeyEvent.VK_T);
         toolMenu.getAccessibleContext().setAccessibleDescription("The tool menu");
 
         miBlockCounter = new JMenuItem("Count blocks");
         miBlockCounter.setMnemonic(KeyEvent.VK_C);
         miBlockCounter.getAccessibleContext().setAccessibleDescription("This counts the blocks");
         miBlockCounter.setActionCommand("BLOCKCOUNTER");
         miBlockCounter.addActionListener(this);
         toolMenu.add(miBlockCounter);
 
         menuBar.add(toolMenu);
 
         printMenu = new JMenu("Print");
         printMenu.setMnemonic(KeyEvent.VK_R);
         printMenu.getAccessibleContext().setAccessibleDescription("The print menu");
 
         miPrintSlice = new JMenuItem("Print current slice");
         miPrintSlice.setMnemonic(KeyEvent.VK_P);
         miPrintSlice.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
         miPrintSlice.getAccessibleContext().setAccessibleDescription("This prints the current slice");
         miPrintSlice.setActionCommand("PRINTSLICE");
         miPrintSlice.addActionListener(this);
         printMenu.add(miPrintSlice);
         miPrintAll = new JMenuItem("Print all (one page per slice)");
         miPrintAll.setMnemonic(KeyEvent.VK_A);
         miPrintAll.getAccessibleContext().setAccessibleDescription("This prints all slices");
         miPrintAll.setActionCommand("PRINTALL");
         miPrintAll.addActionListener(this);
         printMenu.add(miPrintAll);
 
         menuBar.add(printMenu);
 
         setJMenuBar(menuBar);
 
         // Toolbar
         tbTools.setFloatable(false);
 
         btnOpen = new JButton(IconFactory.OPEN_32);
         btnOpen.setToolTipText("Open");
         btnOpen.setActionCommand("OPEN");
         btnOpen.addActionListener(this);
 
         tbTools.add(btnOpen);
         tbTools.addSeparator();
 
         btnRotateCW = new JButton(IconFactory.TURN_CW_32);
         btnRotateCW.setToolTipText("Turn Clock-Wise");
         btnRotateCW.setActionCommand("RCW");
         btnRotateCW.addActionListener(this);
         btnRotateCCW = new JButton(IconFactory.TURN_CCW_32);
         btnRotateCCW.setToolTipText("Turn Counter Clock-Wise");
         btnRotateCCW.setActionCommand("RCCW");
         btnRotateCCW.addActionListener(this);
 
         tbTools.add(btnRotateCCW);
         tbTools.add(btnRotateCW);
         tbTools.addSeparator();
 
         btnZoomIn = new JButton(IconFactory.ZOOM_IN_32);
         btnZoomIn.setToolTipText("Zoom In");
         btnZoomIn.setActionCommand("ZOOMIN");
         btnZoomIn.addActionListener(this);
         btnZoomOut = new JButton(IconFactory.ZOOM_OUT_32);
         btnZoomOut.setToolTipText("Zoom Out");
         btnZoomOut.setActionCommand("ZOOMOUT");
         btnZoomOut.addActionListener(this);
 
         tbTools.add(btnZoomIn);
         tbTools.add(btnZoomOut);
 
         for (Component child : tbTools.getComponents()) {
             if (child instanceof AbstractButton) {
                 ((AbstractButton) child).setFocusPainted(false);
             }
         }
 
         // layout mgrs
         pnlAll.setLayout(new BoxLayout(pnlAll, BoxLayout.Y_AXIS));
         pnlRotate.setLayout(new BoxLayout(pnlRotate, BoxLayout.X_AXIS));
         pnlSchematic.setLayout(new BoxLayout(pnlSchematic, BoxLayout.X_AXIS));
 
         pnlGrid.setLayout(new GridBagLayout());
         pnlGrid.setBackground(Color.BLUE);
 
         // init sliders
 
         sldLayer.setBorder(BorderFactory.createTitledBorder("Layer"));
         sldLayer.setMajorTickSpacing(1);
         sldLayer.setValue(currentLayer + 1);
         sldLayer.setMinimum(1);
         sldLayer.setMaximum(10);
         sldLayer.setPaintTicks(true);
         sldLayer.setPaintLabels(true);
         sldLayer.setSnapToTicks(true);
         sldLayer.addChangeListener(this);
 
         // default states
         enableSchematicControls(false);
 
         scrGrid = new JScrollPane(pnlGrid);
 
         long eventMask = AWTEvent.MOUSE_MOTION_EVENT_MASK + AWTEvent.MOUSE_EVENT_MASK;
         myAlmightyListener = new MyAlmightyListener(scrGrid, pnlGrid);
         Toolkit.getDefaultToolkit().addAWTEventListener(myAlmightyListener, eventMask);
 
         pnlSchematic.add(scrGrid);
         pnlSchematic.add(Box.createHorizontalStrut(5));
         pnlSchematic.add(sldLayer);
 
         lblSize.setAlignmentX(SwingConstants.LEFT);
 
         pnlAll.add(Box.createVerticalStrut(5));
         pnlAll.add(pnlSchematic);
         pnlAll.add(lblSize);
         pnlAll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
         this.add(tbTools, BorderLayout.NORTH);
         this.add(pnlAll);
 
         setLocationRelativeTo(null);
         setVisible(true);
     }
 
     public void addRecentlyOpened(String filename) {
     	Preferences pref = Preferences.userNodeForPackage(Application.class);
     	String recentlyOpened = pref.get(PREF_KEY_LAST_SELECTED_PATH, "");
 
     	if (StringUtils.contains(recentlyOpened, filename)) {
     		return;
     	}
 
     	if (StringUtils.countMatches(recentlyOpened, ";") >= 9) {
     		recentlyOpened = recentlyOpened.substring(recentlyOpened.indexOf(';'));
     	}
 
     	recentlyOpened += ";" + filename;
     	pref.put(PREF_KEY_RECENTLY_OPENED, recentlyOpened);
     }
 
     private void populateFileMenu() {
     	fileMenu.removeAll();
         miOpen = new JMenuItem("Open schematic file", IconFactory.OPEN_16);
         miOpen.setMnemonic(KeyEvent.VK_O);
         miOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
         miOpen.getAccessibleContext().setAccessibleDescription("This opens a schematic file");
         miOpen.setActionCommand("OPEN");
         miOpen.addActionListener(this);
         fileMenu.add(miOpen);
 
         exportMenu = new JMenu("Export");
         exportMenu.setMnemonic(KeyEvent.VK_E);
 
         miExportLayer = new JMenuItem("Current layer", UIManager.getIcon("FileView.floppyDriveIcon"));
         miExportLayer.setMnemonic(KeyEvent.VK_C);
         // this.miExportLayer.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
         miExportLayer.getAccessibleContext().setAccessibleDescription("This saves the current layer as a single file");
         miExportLayer.setActionCommand("EXPSINGLE");
         miExportLayer.addActionListener(this);
         exportMenu.add(miExportLayer);
 
         miExportImages = new JMenuItem("One image per layer", UIManager.getIcon("FileChooser.upFolderIcon"));
         miExportImages.setMnemonic(KeyEvent.VK_O);
         // this.miExportImages.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.ALT_MASK));
         miExportImages.getAccessibleContext().setAccessibleDescription(
                 "This saves the current schematic as multiple images");
         miExportImages.setActionCommand("EXPMULTI");
         miExportImages.addActionListener(this);
         exportMenu.add(miExportImages);
 
         miExportGif = new JMenuItem("To an animated Gif", UIManager.getIcon("FileView.floppyDriveIcon"));
         miExportGif.setMnemonic(KeyEvent.VK_G);
         // this.miExportGif.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
         miExportGif.getAccessibleContext().setAccessibleDescription(
                 "This saves the current schematic as a single animated gif file");
         miExportGif.setActionCommand("EXPGIF");
         miExportGif.addActionListener(this);
         exportMenu.add(miExportGif);
 
         miExportTxt = new JMenuItem("To a Textfile (Builders-Mod compatible)",
                 UIManager.getIcon("FileView.floppyDriveIcon"));
         miExportTxt.setMnemonic(KeyEvent.VK_T);
         // this.miExportGif.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.ALT_MASK));
         miExportTxt.getAccessibleContext().setAccessibleDescription(
                 "This saves the current schematic as a Builders-compatible text file");
         miExportTxt.setActionCommand("EXPTXT");
         miExportTxt.addActionListener(this);
         exportMenu.add(miExportTxt);
 
         fileMenu.add(exportMenu);
 
         fileMenu.addSeparator();
 
         Preferences pref = Preferences.userNodeForPackage(Application.class);
 
         String recentlyOpened = pref.get(PREF_KEY_RECENTLY_OPENED, "");
         JMenuItem item;
 
         for (String path: recentlyOpened.split(";")) {
         	item = new JMenuItem(path.substring(path.lastIndexOf(File.pathSeparatorChar) + 1));
         	item.setActionCommand("LOAD");
         	item.addActionListener(new ActionListener() {
 				
 				@Override
 				public void actionPerformed(ActionEvent event) {
 					String filepath = ((JMenuItem) event.getSource()).getText();
 					loadFile(new File(filepath));
 				}
 			});
 
         	fileMenu.add(item);
         }

        menuBar.add(fileMenu);
     }
 
     private void enableSchematicControls(boolean enable) {
         btnRotateCCW.setEnabled(enable);
         btnRotateCW.setEnabled(enable);
         btnZoomIn.setEnabled(enable);
         btnZoomOut.setEnabled(enable);
         sldLayer.setEnabled(enable);
         exportMenu.setEnabled(enable);
         miBlockCounter.setEnabled(enable);
         printMenu.setEnabled(enable);
     }
 
     @Override
     public void actionPerformed(ActionEvent event) {
         if (event.getActionCommand().equals("OPEN")) {
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
             if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
             	loadingFile = fc.getSelectedFile();
 
                 Preferences pref = Preferences.userNodeForPackage(Application.class);
                 pref.put(PREF_KEY_LAST_SELECTED_PATH, loadingFile.getAbsolutePath());
 
                 loadFile(loadingFile);
 
                 addRecentlyOpened(loadingFile.getAbsolutePath());
                 populateFileMenu();
                 loadingFile = null;
             }
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         } else if (event.getActionCommand().equals("LOAD")) {
         	loadFile(loadingFile);
         	loadingFile = null;
         } else if (event.getActionCommand().equals("RCCW") || event.getActionCommand().equals("RCW")) {
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
             if (event.getActionCommand().equals("RCCW")) {
                 stack.turnCCW();
             } else {
                 stack.turnCW();
             }
 
             images = stack.getImages(currentZoom, true);
 
             pnlGrid.removeAll();
             pnlGrid.add(images.getGridAtLevel(currentLayer), defaultContraints);
             pnlGrid.repaint();
             scrGrid.validate();
             lblSize.setText("Size: " + stack.getLength() + " x " + stack.getWidth());
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         } else if (event.getActionCommand().equals("EXPSINGLE")) {
             JFileChooser fc = new JFileChooser();
 
             fc.setCurrentDirectory(this.fc.getCurrentDirectory());
             fc.setFileFilter(new FiletypeFilter("png", "PNG"));
             // fc.setAcceptAllFileFilterUsed(false);
             // fc.setDialogTitle("Save to directory");
             if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                 File f = fc.getSelectedFile();
                 if (!f.getName().toLowerCase().endsWith(".png")) {
                     f = new File(f.getAbsolutePath() + ".png");
                 }
                 if (!f.isDirectory()) {
                     if (f.exists()) {
                         int reply = JOptionPane.showConfirmDialog(this, "File allready exists. Overwrite?",
                                 "File allready exists", JOptionPane.YES_NO_OPTION);
                         if (reply != JOptionPane.YES_OPTION) {
                             return;
                         }
                     }
                     try {
                         BufferedImage img = images.getGridAtLevel(currentLayer).exportImage(null, gridLineColor);
                         try {
                             ImageIO.write(img, "png", f);
                         } catch (IOException e) {
                             JOptionPane.showMessageDialog(this, "Error while saving image " + f.getName().toString()
                                     + ":\n" + e.getLocalizedMessage(), "Could not save", JOptionPane.ERROR_MESSAGE);
                         }
                     } catch (OutOfMemoryError e) {
                         System.gc();
                         JOptionPane.showMessageDialog(this,
                                 "Ran out of memory while trying to generate an image out of the layer.\n"
                                         + "Try a lower zoom value or start this program with more memory.",
                                 "Out of heap memory", JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         } else if (event.getActionCommand().equals("EXPMULTI")) {
             new ExportDialog(this, stack, images, fc.getSelectedFile(), pnlGrid.getBackground(), false);
         } else if (event.getActionCommand().equals("EXPGIF")) {
             new ExportDialog(this, stack, images, fc.getSelectedFile(), pnlGrid.getBackground(), true);
         } else if (event.getActionCommand().equals("EXPTXT")) {
             JFileChooser fc = new JFileChooser();
 
             fc.setCurrentDirectory(this.fc.getCurrentDirectory());
             fc.setFileFilter(new FiletypeFilter("txt", "TXT"));
             // fc.setAcceptAllFileFilterUsed(false);
             // fc.setDialogTitle("Save to directory");
             if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                 File f = fc.getSelectedFile();
                 if (!f.getName().toLowerCase().endsWith(".txt")) {
                     f = new File(f.getAbsolutePath() + ".txt");
                 }
                 if (!f.isDirectory()) {
                     if (f.exists()) {
                         int reply = JOptionPane.showConfirmDialog(this, "File allready exists. Overwrite?",
                                 "File allready exists", JOptionPane.YES_NO_OPTION);
                         if (reply != JOptionPane.YES_OPTION) {
                             return;
                         }
                     }
                     try {
                         String title = f.getName();
                         title = title.substring(0, title.lastIndexOf('.'));
                         stack.exportToTextFile(title, f);
                     } catch (IOException e) {
                         e.printStackTrace();
                         JOptionPane.showMessageDialog(this, "Error while saving text file " + f.getName().toString()
                                 + ":\n" + e.getLocalizedMessage(), "Could not save", JOptionPane.ERROR_MESSAGE);
                     }
                 }
             }
         } else if (event.getActionCommand().equals("COLOR_BACKGROUND")) {
             Color newColor = JColorChooser.showDialog(this, "Choose Background Color", pnlGrid.getBackground());
             if (newColor != null) {
                 pnlGrid.setBackground(newColor);
             }
         } else if (event.getActionCommand().equals("COLOR_LINE")) {
             Color newColor = JColorChooser.showDialog(this, "Choose Line Color", gridLineColor);
             if (newColor != null) {
                 gridLineColor = newColor;
                 if (images != null) {
                     images.setGridColor(newColor);
                     pnlGrid.repaint();
                 }
             }
         } else if (event.getActionCommand().equals("COLOR_MARK")) {
             Color newColor = JColorChooser.showDialog(this, "Choose Marker Color", markColor);
             if (newColor != null) {
                 markColor = newColor;
                 if (images != null) {
                     images.setMarkColor(newColor);
                     pnlGrid.repaint();
                 }
             }
         } else if (event.getActionCommand().equals("BLOCKCOUNTER")) {
             new BlockCounterDialog(this, stack);
         } else if (event.getActionCommand().equals("PRINTSLICE")) {
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             PrinterJob printJob = PrinterJob.getPrinterJob();
             printJob.setPrintable(images.getGridAtLevel(currentLayer));
             if (printJob.printDialog()) {
                 try {
                     printJob.print();
                     this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 } catch (PrinterException pe) {
                     this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     JOptionPane.showMessageDialog(this, "Error printing:\n" + pe.getLocalizedMessage(),
                             "Could not print", JOptionPane.ERROR_MESSAGE);
                 }
             }
         } else if (event.getActionCommand().equals("PRINTALL")) {
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             PrinterJob printJob = PrinterJob.getPrinterJob();
             printJob.setPrintable(images);
             if (printJob.printDialog()) {
                 try {
                     printJob.print();
                     this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                 } catch (PrinterException pe) {
                     this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
                     JOptionPane.showMessageDialog(this, "Error printing:\n" + pe.getLocalizedMessage(),
                             "Could not print", JOptionPane.ERROR_MESSAGE);
                 }
             }
         } else if (event.getActionCommand().equals("ZOOMIN")) {
             if (currentZoom < maxZoom) {
                 currentZoom += zoomRatio;
 
                 setGridZoom(currentZoom);
             }
         } else if (event.getActionCommand().equals("ZOOMOUT")) {
             if (currentZoom > minZoom) {
                 currentZoom -= zoomRatio;
 
                 setGridZoom(currentZoom);
             }
         }
     }
     
     private void loadFile(File file) {
     	try {
             stack = SchematicReader.readSchematicsFile(file);
             stack.trim();
             currentZoom = defaultZoom;
             images = stack.getImages(currentZoom, true);
             images.setGridColor(gridLineColor);
             images.setMarkColor(markColor);
             if (SchematicReader.hasErrorHappened()) {
                 JOptionPane.showMessageDialog(null,
                         "There were some faulty blocks in the schematic. They were replaced with air.",
                         "Warning", JOptionPane.WARNING_MESSAGE);
             }
         } catch (IOException e) {
             e.printStackTrace();
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             JOptionPane.showMessageDialog(null, "Could not read file", "Error", JOptionPane.ERROR_MESSAGE);
             return;
         } catch (ClassicNotSupportedException e) {
             e.printStackTrace();
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             JOptionPane.showMessageDialog(null, "Classic file format is not supported",
                     "Classic not supported", JOptionPane.ERROR_MESSAGE);
             return;
         } catch (ParseException e) {
             e.printStackTrace();
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             // Shenanigans to get a multiline option pane
             String message = "Could not parse schematics file:\n" + e.getMessage();
             if (message.length() > 503) {
                 message = message.substring(0, 500) + "...";
             }
             JOptionPane cleanupPane = new JOptionPane(message, JOptionPane.ERROR_MESSAGE) {
 
                 private static final long serialVersionUID = 1L;
 
                 @Override
                 public int getMaxCharactersPerLineCount() {
                     return 100; // this is unimplemented in normal joptionpane for whatever reason
                 }
             };
             cleanupPane.createDialog(null, "Invalid file").setVisible(true);
             return;
         } catch (OutOfMemoryError e) {
             System.gc();
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
             JOptionPane.showMessageDialog(null, "Ran out of memory while trying to open schematic",
                     "Out of Memory", JOptionPane.ERROR_MESSAGE);
             return;
         }
         enableSchematicControls(true);
         if (currentLayer >= images.getStackSize()) {
             currentLayer = images.getStackSize() - 1;
         }
         sldLayer.setMaximum(images.getStackSize());
         if (images.getStackSize() == 1) {
             sldLayer.setEnabled(false);
         }
 
         pnlGrid.removeAll();
         pnlGrid.add(images.getGridAtLevel(currentLayer), defaultContraints);
         pnlGrid.repaint();
         scrGrid.validate();
         lblSize.setText("Size: " + stack.getLength() + " x " + stack.getWidth());
         System.gc();
     }
 
     private void setGridZoom(double zoom) {
         setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
 
         images.setZoom(zoom);
 
         pnlGrid.repaint();
         scrGrid.validate();
 
         setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
     }
 
     @Override
     public void stateChanged(ChangeEvent e) {
         JSlider source = (JSlider) e.getSource();
 
         if (source.getValueIsAdjusting()) {
             return;
         }
 
         if (source == sldLayer) {
             this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
             Point markedPoint = images.getGridAtLevel(currentLayer).getMarkedBlock();
             currentLayer = sldLayer.getValue() - 1;
             images.getGridAtLevel(currentLayer).setMarkedBlock(markedPoint);
             pnlGrid.removeAll();
             pnlGrid.add(images.getGridAtLevel(currentLayer), defaultContraints);
             pnlGrid.repaint();
             scrGrid.validate();
             this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
         }
     }
 }
 
 /**
  * Modified from http://www.java-forums.org/awt-swing/24693-moving-whole-content-jscrollpane-mousedrag.html#post100182
  * To scroll my scrollpane by dragging
  * 
  * @author Taiko
  */
 class MyAlmightyListener implements AWTEventListener {
     private Point startPt;
     private final Cursor defaultCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);
     private final Cursor handCursor = Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);
     private final JScrollPane scrPane;
     private final JComponent comp;
     private Point move = new Point();
     private Point ptZero, it;
     private final Rectangle rect = new Rectangle();
     private Rectangle vr;
     private int w, h;
     private MouseEvent event;
     private boolean dragInitialized = false;
 
     public MyAlmightyListener(JScrollPane scrPane, JComponent comp) {
         this.comp = comp;
         this.scrPane = scrPane;
     }
 
     @Override
     public void eventDispatched(AWTEvent e) {
 
         event = (MouseEvent) e;
 
         // catching press of button no. 3 (right button)
         if (event.getID() == MouseEvent.MOUSE_PRESSED) {
 
             // do nothing if the source is not inside the scrollpane (scrollbars are not part of the viewport)
             Component source = (Component) e.getSource();
             if (!SwingUtilities.isDescendingFrom(source, scrPane.getViewport())) {
                 return;
             }
 
             // getting mouse location, when mouse button is pressed
             startPt = event.getLocationOnScreen();
 
             // changing mouse cursor on my JDesktopPane
             comp.setCursor(handCursor);
             dragInitialized = true;
         }
 
         // catching release of button no. 3 (right button)
         // and changing mouse cursor back on all components
         if (event.getID() == MouseEvent.MOUSE_RELEASED) {
             if (dragInitialized) {
                 comp.setCursor(defaultCursor);
                 dragInitialized = false;
             }
         }
 
         // catching mouse move when the right mouse button is pressed
         if (event.getID() == MouseEvent.MOUSE_DRAGGED) {
             if (dragInitialized) {
                 it = event.getLocationOnScreen();
 
                 // calculation of move
                 move.setLocation(it.x - startPt.x, it.y - startPt.y);
                 startPt.setLocation(it);
                 vr = scrPane.getViewport().getViewRect();
                 w = vr.width;
                 h = vr.height;
 
                 // getting zero point in my JDesktopPane coordinates
                 ptZero = SwingUtilities.convertPoint(scrPane.getViewport(), 0, 0, comp);
 
                 // setting new rectangle to view
                 rect.setRect(ptZero.x - move.x, ptZero.y - move.y, w, h);
 
                 // viewing of new rectangle
                 comp.scrollRectToVisible(rect);
             }
         }
     }
 }
