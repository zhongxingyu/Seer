 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * MainUI3.java
  *
  * Created on 18-Aug-2009, 08:44:11
  */
 package heartsim.gui;
 
 import heartsim.Application;
 import heartsim.CellGenerator;
 import heartsim.CellGeneratorListener;
 import heartsim.CellularAutomaton;
 import heartsim.HeartTissue;
 import heartsim.cam.profile.Profile;
 import heartsim.Simulator;
 import heartsim.SimulatorListener;
 import heartsim.cam.CellularAutomataModel;
 import heartsim.cam.Nishiyama;
 import heartsim.cam.parameter.CAModelParameter;
 import heartsim.cam.speed.Fast;
 import heartsim.cam.speed.Maximum;
 import heartsim.cam.speed.Medium;
 import heartsim.cam.speed.Slow;
 import heartsim.cam.speed.Speed;
 import heartsim.gui.layout.SpringUtilities;
 import heartsim.gui.util.FileChooserFilter;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.geom.AffineTransform;
 import java.io.File;
 import javax.swing.ComboBoxModel;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JFileChooser;
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 import javax.swing.SwingWorker;
 import javax.swing.UIManager;
 import org.apache.batik.ext.swing.JAffineTransformChooser;
 import org.apache.batik.ext.swing.JAffineTransformChooser.Dialog;
 import org.apache.batik.swing.gvt.GVTTreeRendererEvent;
 import org.apache.batik.swing.gvt.GVTTreeRendererListener;
 import org.apache.batik.swing.gvt.JGVTComponentListener;
 import org.apache.batik.swing.svg.GVTTreeBuilderEvent;
 import org.apache.batik.swing.svg.GVTTreeBuilderListener;
 import org.apache.batik.swing.svg.JSVGComponent;
 import org.apache.batik.swing.svg.SVGDocumentLoaderEvent;
 import org.apache.batik.swing.svg.SVGDocumentLoaderListener;
 
 /**
  *
  * @author Lee Boynton
  */
 public class MainUI3 extends javax.swing.JFrame implements CellGeneratorListener,
         SVGDocumentLoaderListener, GVTTreeBuilderListener, GVTTreeRendererListener,
         SimulatorListener, JGVTComponentListener
 {
     private CellGenerator cellGenerator;
     private CellularAutomaton ca = new CellularAutomaton();
     private CellularAutomataModel caModel;
     private CellGeneratorWorker generatorWorker;
     private int stimRow = 250;
     private int stimCol = 450;
     private final BinaryPlotPanelOverlay overlay;
     private final Simulator simulation;
     private String openFile;
     private int runTime = 5000;
 
     /** Creates new form MainUI3 */
     public MainUI3()
     {
         try
         {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         }
         catch (Exception ex)
         {
             System.err.println("Unable to use system look and feel");
         }
 
         initComponents();
 
         // set the JSVGCanvas listeners.
         svgCanvas.addSVGDocumentLoaderListener(this);
         svgCanvas.addGVTTreeBuilderListener(this);
         svgCanvas.addGVTTreeRendererListener(this);
         svgCanvas.addJGVTComponentListener(this);
 
         // create the cell generator and initially tell it to try to load some
         // elements
         cellGenerator = new CellGenerator(svgCanvas, new String[]
                 {
                     "ventricles", "atria", "sinoatrial_node", "atrioventricular_node", "internodal_fibres", "bundle_of_his", "bundle_branches", "insulator1", "insulator2"
                 });
 
         // add listener so we know when it's finished generating the cells array
         cellGenerator.addGeneratorListener(this);
 
         overlay = new BinaryPlotPanelOverlay(svgCanvas);
 
         simulation = new Simulator(overlay);
         simulation.addListener(this);
         simulation.setHeartRate(30);
 
         // centre jframe on screen
         setLocationRelativeTo(null);
 
         simulation.setAutomaton(ca);
 
         // initially load an SVG file
         loadSVG("./geometry_data/heart4.svg");
     }
 
     public void loadSVG()
     {
         loadSVG(openFile);
     }
 
     public void loadSVG(String path)
     {
         loadSVG(new File(path));
         openFile = path;
     }
 
     public void loadSVG(File file)
     {
         String uri = file.toURI().toString();
 
         openFile = file.getPath();
 
         // this ensures an update manager is created
         svgCanvas.setDocumentState(JSVGComponent.ALWAYS_DYNAMIC);
 
         svgCanvas.setURI(uri);
     }
 
     public void incrementProgressBar()
     {
         progressBar.setValue(progressBar.getValue() + 1);
     }
 
     public void resetProgressBar()
     {
         progressBar.setValue(0);
     }
 
     public void setStatusText(String text)
     {
         lblStatus.setText(text);
     }
 
     /**
      * Loads the available CA models into the combo box in the GUI. New CA models
      * should be added in here
      * @return Combo box model containing all the CA models
      */
     private void loadCAModels()
     {
         DefaultComboBoxModel CAModels = new DefaultComboBoxModel();
 
         for (CellularAutomataModel model : Application.getInstance().getCAModels())
         {
             CAModels.addElement(model);
         }
 
         cboBoxModel.setModel(CAModels);
 
         if (cboBoxModel.getItemCount() > 0)
         {
             cboBoxModel.setSelectedIndex(0);
         }
     }
 
     /**
      * Called when the CA model changes. Loads the CA model parameters into the
      * GUI.
      */
     private void loadModelParameters()
     {
         Application.getInstance().output("Loading model parameters");
 
         // skip loading parameters if model hasn't changed
         if (caModel != null && caModel.equals(cboBoxModel.getSelectedItem()))
         {
             Application.getInstance().output("Same model selected, skipping.");
             //return;
         }
 
         // get the CA model that was selected
         caModel = (CellularAutomataModel) cboBoxModel.getSelectedItem();
 
         Application.getInstance().output("Model selected is " + caModel.getName());
 
         // clear any existing parameters in the GUI
         pnlParameters.removeAll();
 
         // use spring layout for this panel
         pnlParameters.setLayout(new SpringLayout());
 
         // add the tissue combo box selector first
         pnlParameters.add(lblTissue);
         pnlParameters.add(cboBoxTissue);
         pnlParameters.add(btnTissueHelp);
 
         // add the CA model combo box selector
         pnlParameters.add(lblModel);
         pnlParameters.add(cboBoxModel);
         pnlParameters.add(btnModelHelp);
 
         // add the profile combo box selector
         pnlParameters.add(lblProfile);
         pnlParameters.add(cboBoxProfile);
         pnlParameters.add(btnProfileHelp);
 
         // loop through the parameters in the CA model and put them on the GUI
         for (final CAModelParameter p : caModel.getParameters().values())
         {
             Application.getInstance().output("Found parameter: " + p.getName());
 
             JLabel lbl = new JLabel(p.getName());
             final JTextField txt = new JTextField();
             txt.setText(p.getValue().toString());
             txt.addKeyListener(new KeyAdapter()
             {
                 @Override
                 public void keyReleased(KeyEvent evt)
                 {
                     if (!p.setValue(txt.getText()))
                     {
                         Application.getInstance().output("Invalid parameter: " + txt.getText());
                         txt.setForeground(Color.red);
                     }
                     else
                     {
                         txt.setForeground(null);
                     }
                     caModel.setParameter(p.getName(), p);
                 }
             });
 
             JButton btn = new JButton(new ImageIcon(getClass().getResource("/heartsim/gui/icon/help-browser.png")));
             btn.setBorder(null);
             btn.setBorderPainted(false);
             btn.addActionListener(new ActionListener()
             {
                 public void actionPerformed(ActionEvent e)
                 {
                     new HelpDialog(MainUI3.this, p.getName(), false, p.getDescription()).setVisible(true);
                 }
             });
 
             pnlParameters.add(lbl);
             pnlParameters.add(txt);
             pnlParameters.add(btn);
         }
 
         // place components in grid
         SpringUtilities.makeCompactGrid(pnlParameters,
                 caModel.getParameters().size() + 3, 3, //rows, cols
                 10, 12, //initX, initY
                 12, 6);       //xPad, yPad
 
         pnlParameters.revalidate();
         pack();
     }
 
     private void loadHeartTissues()
     {
         // set up combo box
         DefaultComboBoxModel tissueModels = new DefaultComboBoxModel();
 
         // set up menu
         mnuHeartTissue.removeAll();
 
         for (final HeartTissue tissue : cellGenerator.getTissues())
         {
             if (cellGenerator.isEnabled(tissue))
             {
                 tissueModels.addElement(tissue);
             }
 
             final JCheckBoxMenuItem item = new JCheckBoxMenuItem(tissue.getName());
             item.setToolTipText(tissue.getDescription());
             item.setSelected(cellGenerator.isEnabled(tissue));
             item.addActionListener(new ActionListener()
             {
                 public void actionPerformed(ActionEvent e)
                 {
                     if (item.isSelected())
                     {
                         System.out.println("Adding tissue");
                         cellGenerator.enableTissue(tissue);
                     }
                     else
                     {
                         System.out.println("Removing tissue");
                         cellGenerator.disableTissue(tissue);
                     }
                 }
             });
 
             mnuHeartTissue.add(item);
         }
 
         cboBoxTissue.setModel(tissueModels);
 
         if (cboBoxTissue.getItemCount() > 0)
         {
             cboBoxTissue.setSelectedIndex(0);
         }
     }
 
     private void loadProfiles()
     {
         DefaultComboBoxModel profileModels = new DefaultComboBoxModel();
 
         for (Profile profile : Application.getInstance().getTissueProfiles())
         {
             profileModels.addElement(profile);
         }
 
         cboBoxProfile.setModel(profileModels);
     }
 
     private ComboBoxModel loadSpeeds()
     {
         DefaultComboBoxModel model = new DefaultComboBoxModel();
 
         model.addElement(new Maximum());
         model.addElement(new Fast());
         model.addElement(new Medium());
         model.addElement(new Slow());
 
         return model;
     }
 
     private void positionHeartInCentre()
     {
         int x = (scrollPaneCanvas.getWidth() / 2) - (svgCanvas.getPreferredSize().width / 2);
         int y = (scrollPaneCanvas.getHeight() / 2) - (svgCanvas.getPreferredSize().height / 2);
 
         if(x < 0) x = 0;
         if(y < 0) y = 0;
 
         pnlCanvasContainer.add(svgCanvas, new org.netbeans.lib.awtextra.AbsoluteConstraints(x, y, -1, -1));
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         toolbar = new javax.swing.JToolBar();
         btnOpen = new javax.swing.JButton();
         separatorFileOpen = new javax.swing.JToolBar.Separator();
         btnStart = new javax.swing.JButton();
         btnPause = new javax.swing.JButton();
         btnStop = new javax.swing.JButton();
         btnStepForward = new javax.swing.JButton();
         separatorControls = new javax.swing.JToolBar.Separator();
         btnZoomOut = new javax.swing.JButton();
         btnZoomIn = new javax.swing.JButton();
         separatorZoom = new javax.swing.JToolBar.Separator();
         btnAbout = new javax.swing.JButton();
         pnlRootContainer = new javax.swing.JPanel();
         progressBar = new javax.swing.JProgressBar();
         lblStatus = new javax.swing.JLabel();
         tabbedPane = new javax.swing.JTabbedPane();
         pnlCA = new javax.swing.JPanel();
         pnlParameters = new javax.swing.JPanel();
         lblTissue = new javax.swing.JLabel();
         lblModel = new javax.swing.JLabel();
         cboBoxTissue = new javax.swing.JComboBox();
         btnProfileHelp = new javax.swing.JButton();
         cboBoxModel = new javax.swing.JComboBox();
         btnModelHelp = new javax.swing.JButton();
         lblProfile = new javax.swing.JLabel();
         cboBoxProfile = new javax.swing.JComboBox();
         btnTissueHelp = new javax.swing.JButton();
         pnlSimulationSettings = new javax.swing.JPanel();
         lblHeart = new javax.swing.JLabel();
         txtHeartRate = new javax.swing.JTextField();
         lblTime = new javax.swing.JLabel();
         txtTime = new javax.swing.JTextField();
         lblSpeed = new javax.swing.JLabel();
         cboBoxSpeed = new javax.swing.JComboBox();
         scrollPaneCanvas = new javax.swing.JScrollPane();
         pnlCanvasContainer = new javax.swing.JPanel();
         svgCanvas = new org.apache.batik.swing.JSVGCanvas();
         lblHeartRateText = new javax.swing.JLabel();
         lblHeartRate = new javax.swing.JLabel();
         menuBar = new javax.swing.JMenuBar();
         mnuFile = new javax.swing.JMenu();
         mnuItmReload = new javax.swing.JMenuItem();
         separatorMnuFile = new javax.swing.JSeparator();
         mnuItmExit = new javax.swing.JMenuItem();
         mnuView = new javax.swing.JMenu();
         mnuItmnZoomIn = new javax.swing.JMenuItem();
         mnuItmZoomOut = new javax.swing.JMenuItem();
         separatorMnuView = new javax.swing.JSeparator();
         mnuItmTransform = new javax.swing.JMenuItem();
         mnuHeartTissue = new javax.swing.JMenu();
         mnuItmNotLoaded = new javax.swing.JMenuItem();
         mnuDebug = new javax.swing.JMenu();
         mnuItmVerboseOutput = new javax.swing.JCheckBoxMenuItem();
         jSeparator2 = new javax.swing.JSeparator();
         mnuItmViewCells = new javax.swing.JMenuItem();
         jSeparator1 = new javax.swing.JSeparator();
         mnuItmPrintCells = new javax.swing.JMenuItem();
         mnuItmPrintArrays = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 
         toolbar.setFloatable(false);
         toolbar.setRollover(true);
 
         btnOpen.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/document-open.png"))); // NOI18N
         btnOpen.setToolTipText("Open SVG file containing heart geometry");
         btnOpen.setFocusable(false);
         btnOpen.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnOpen.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnOpen.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnOpenActionPerformed(evt);
             }
         });
         toolbar.add(btnOpen);
         toolbar.add(separatorFileOpen);
 
         btnStart.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-playback-start.png"))); // NOI18N
         btnStart.setToolTipText("Run the simulation with the specified parameters");
         btnStart.setFocusable(false);
         btnStart.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnStart.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnStart.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStartActionPerformed(evt);
             }
         });
         toolbar.add(btnStart);
 
         btnPause.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-playback-pause.png"))); // NOI18N
         btnPause.setToolTipText("Pauses the simulation");
         btnPause.setEnabled(false);
         btnPause.setFocusable(false);
         btnPause.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnPause.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnPause.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnPauseActionPerformed(evt);
             }
         });
         toolbar.add(btnPause);
 
         btnStop.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-playback-stop.png"))); // NOI18N
         btnStop.setToolTipText("Stops the simulation if it is running and resets the simulation");
         btnStop.setEnabled(false);
         btnStop.setFocusable(false);
         btnStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnStop.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStopActionPerformed(evt);
             }
         });
         toolbar.add(btnStop);
 
         btnStepForward.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/media-seek-forward.png"))); // NOI18N
         btnStepForward.setToolTipText("Step simulation forward");
         btnStepForward.setFocusable(false);
         btnStepForward.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnStepForward.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnStepForward.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnStepForwardActionPerformed(evt);
             }
         });
         toolbar.add(btnStepForward);
         toolbar.add(separatorControls);
 
         btnZoomOut.setAction(svgCanvas.new ZoomAction(0.75));
         btnZoomOut.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/zoom-out.png"))); // NOI18N
         btnZoomOut.setToolTipText("Zoom out and regenerate the cells");
         btnZoomOut.setFocusable(false);
         btnZoomOut.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnZoomOut.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnZoomOut.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnZoomOutActionPerformed(evt);
             }
         });
         toolbar.add(btnZoomOut);
 
         btnZoomIn.setAction(svgCanvas.new ZoomAction(1.5));
         btnZoomIn.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/zoom-in.png"))); // NOI18N
         btnZoomIn.setToolTipText("Zoom in and regenerate the cells");
         btnZoomIn.setFocusable(false);
         btnZoomIn.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnZoomIn.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         btnZoomIn.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnZoomInActionPerformed(evt);
             }
         });
         toolbar.add(btnZoomIn);
         toolbar.add(separatorZoom);
 
         btnAbout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/help-browser.png"))); // NOI18N
         btnAbout.setToolTipText("About");
         btnAbout.setFocusable(false);
         btnAbout.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
         btnAbout.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
         toolbar.add(btnAbout);
 
         progressBar.setMaximum(7);
         progressBar.setStringPainted(true);
 
         lblStatus.setText("Status");
 
         lblTissue.setText("Tissue");
 
         lblModel.setText("Model");
 
         cboBoxTissue.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Ventricles" }));
         cboBoxTissue.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cboBoxTissueActionPerformed(evt);
             }
         });
 
         btnProfileHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/help-browser.png"))); // NOI18N
         btnProfileHelp.setBorder(null);
         btnProfileHelp.setBorderPainted(false);
         btnProfileHelp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnProfileHelpActionPerformed(evt);
             }
         });
 
         cboBoxModel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cboBoxModelActionPerformed(evt);
             }
         });
 
         btnModelHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/help-browser.png"))); // NOI18N
         btnModelHelp.setBorder(null);
         btnModelHelp.setBorderPainted(false);
         btnModelHelp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnModelHelpActionPerformed(evt);
             }
         });
 
         lblProfile.setText("Profile");
 
         cboBoxProfile.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cboBoxProfileActionPerformed(evt);
             }
         });
 
         btnTissueHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/heartsim/gui/icon/help-browser.png"))); // NOI18N
         btnTissueHelp.setBorder(null);
         btnTissueHelp.setBorderPainted(false);
         btnTissueHelp.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnTissueHelpActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlParametersLayout = new javax.swing.GroupLayout(pnlParameters);
         pnlParameters.setLayout(pnlParametersLayout);
         pnlParametersLayout.setHorizontalGroup(
             pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlParametersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(pnlParametersLayout.createSequentialGroup()
                         .addComponent(lblTissue)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(cboBoxTissue, 0, 187, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnProfileHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlParametersLayout.createSequentialGroup()
                         .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlParametersLayout.createSequentialGroup()
                                 .addComponent(lblProfile)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(cboBoxProfile, 0, 188, Short.MAX_VALUE))
                             .addGroup(pnlParametersLayout.createSequentialGroup()
                                 .addComponent(lblModel)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(cboBoxModel, 0, 189, Short.MAX_VALUE)))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(btnTissueHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(btnModelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))))
                 .addContainerGap())
         );
         pnlParametersLayout.setVerticalGroup(
             pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlParametersLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlParametersLayout.createSequentialGroup()
                         .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblTissue)
                             .addComponent(cboBoxTissue, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(cboBoxModel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                             .addComponent(lblModel))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                             .addComponent(lblProfile)
                             .addComponent(cboBoxProfile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                     .addGroup(pnlParametersLayout.createSequentialGroup()
                         .addComponent(btnProfileHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnModelHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(btnTissueHelp, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE))))
         );
 
         javax.swing.GroupLayout pnlCALayout = new javax.swing.GroupLayout(pnlCA);
         pnlCA.setLayout(pnlCALayout);
         pnlCALayout.setHorizontalGroup(
             pnlCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(pnlParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         pnlCALayout.setVerticalGroup(
             pnlCALayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlCALayout.createSequentialGroup()
                 .addComponent(pnlParameters, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(347, Short.MAX_VALUE))
         );
 
         tabbedPane.addTab("Model", pnlCA);
 
         lblHeart.setText("Heart rate");
 
         txtHeartRate.setText("30");
         txtHeartRate.setToolTipText("Set the heart rate in beats per minute. The simulator will attempt to conform to this, however the maximum heart rate is limited by processing power.");
         txtHeartRate.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 txtHeartRateActionPerformed(evt);
             }
         });
 
         lblTime.setText("Time steps");
 
         txtTime.setText("5000");
         txtTime.setToolTipText("Number of time steps in the simulation");
         txtTime.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtTimeKeyReleased(evt);
             }
         });
 
         lblSpeed.setText("Speed");
 
         cboBoxSpeed.setModel(loadSpeeds());
         cboBoxSpeed.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 cboBoxSpeedActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout pnlSimulationSettingsLayout = new javax.swing.GroupLayout(pnlSimulationSettings);
         pnlSimulationSettings.setLayout(pnlSimulationSettingsLayout);
         pnlSimulationSettingsLayout.setHorizontalGroup(
             pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlSimulationSettingsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(lblHeart)
                     .addComponent(lblTime)
                     .addComponent(lblSpeed))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(txtHeartRate, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                     .addComponent(txtTime, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 185, Short.MAX_VALUE)
                     .addComponent(cboBoxSpeed, 0, 185, Short.MAX_VALUE))
                 .addContainerGap())
         );
         pnlSimulationSettingsLayout.setVerticalGroup(
             pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(pnlSimulationSettingsLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblTime)
                     .addComponent(txtTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblSpeed)
                     .addComponent(cboBoxSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlSimulationSettingsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(txtHeartRate, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblHeart))
                 .addContainerGap(347, Short.MAX_VALUE))
         );
 
         tabbedPane.addTab("Simulation", pnlSimulationSettings);
 
         scrollPaneCanvas.addComponentListener(new java.awt.event.ComponentAdapter() {
             public void componentResized(java.awt.event.ComponentEvent evt) {
                 scrollPaneCanvasComponentResized(evt);
             }
         });
 
         pnlCanvasContainer.setBackground(java.awt.Color.white);
         pnlCanvasContainer.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
 
         svgCanvas.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 svgCanvasMouseClicked(evt);
             }
         });
 
         javax.swing.GroupLayout svgCanvasLayout = new javax.swing.GroupLayout(svgCanvas);
         svgCanvas.setLayout(svgCanvasLayout);
         svgCanvasLayout.setHorizontalGroup(
             svgCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 240, Short.MAX_VALUE)
         );
         svgCanvasLayout.setVerticalGroup(
             svgCanvasLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 230, Short.MAX_VALUE)
         );
 
         pnlCanvasContainer.add(svgCanvas, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 120, 240, 230));
 
         scrollPaneCanvas.setViewportView(pnlCanvasContainer);
 
         lblHeartRateText.setText("Estimated heart rate:");
 
         lblHeartRate.setText("0bpm");
 
         javax.swing.GroupLayout pnlRootContainerLayout = new javax.swing.GroupLayout(pnlRootContainer);
         pnlRootContainer.setLayout(pnlRootContainerLayout);
         pnlRootContainerLayout.setHorizontalGroup(
             pnlRootContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRootContainerLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlRootContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlRootContainerLayout.createSequentialGroup()
                         .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 295, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(scrollPaneCanvas, javax.swing.GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE))
                     .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlRootContainerLayout.createSequentialGroup()
                         .addComponent(lblStatus)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 517, Short.MAX_VALUE)
                         .addComponent(lblHeartRateText)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(lblHeartRate)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
         pnlRootContainerLayout.setVerticalGroup(
             pnlRootContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlRootContainerLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(pnlRootContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE)
                     .addComponent(scrollPaneCanvas, javax.swing.GroupLayout.DEFAULT_SIZE, 483, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(pnlRootContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(lblStatus)
                     .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(lblHeartRate)
                     .addComponent(lblHeartRateText))
                 .addContainerGap())
         );
 
         mnuFile.setText("File");
 
         mnuItmReload.setText("Reload file");
         mnuItmReload.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmReloadActionPerformed(evt);
             }
         });
         mnuFile.add(mnuItmReload);
         mnuFile.add(separatorMnuFile);
 
         mnuItmExit.setText("Exit");
         mnuItmExit.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmExitActionPerformed(evt);
             }
         });
         mnuFile.add(mnuItmExit);
 
         menuBar.add(mnuFile);
 
         mnuView.setText("View");
 
         mnuItmnZoomIn.setAction(svgCanvas.new ZoomAction(2));
         mnuItmnZoomIn.setText("Zoom in");
         mnuView.add(mnuItmnZoomIn);
 
         mnuItmZoomOut.setAction(svgCanvas.new ZoomAction(0.5));
         mnuItmZoomOut.setText("Zoom out");
         mnuView.add(mnuItmZoomOut);
         mnuView.add(separatorMnuView);
 
         mnuItmTransform.setText("Transform");
         mnuItmTransform.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmTransformActionPerformed(evt);
             }
         });
         mnuView.add(mnuItmTransform);
 
         menuBar.add(mnuView);
 
         mnuHeartTissue.setText("Heart tissue");
 
         mnuItmNotLoaded.setText("Not yet loaded");
         mnuItmNotLoaded.setEnabled(false);
         mnuHeartTissue.add(mnuItmNotLoaded);
 
         menuBar.add(mnuHeartTissue);
 
         mnuDebug.setText("Debug");
 
         mnuItmVerboseOutput.setSelected(true);
         mnuItmVerboseOutput.setText("Verbose output to terminal");
         mnuItmVerboseOutput.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmVerboseOutputActionPerformed(evt);
             }
         });
         mnuDebug.add(mnuItmVerboseOutput);
         mnuDebug.add(jSeparator2);
 
         mnuItmViewCells.setText("View cells array");
         mnuItmViewCells.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmViewCellsActionPerformed(evt);
             }
         });
         mnuDebug.add(mnuItmViewCells);
         mnuDebug.add(jSeparator1);
 
         mnuItmPrintCells.setText("Print cells to terminal");
         mnuItmPrintCells.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmPrintCellsActionPerformed(evt);
             }
         });
         mnuDebug.add(mnuItmPrintCells);
 
         mnuItmPrintArrays.setText("Print arrays to terminal");
         mnuItmPrintArrays.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuItmPrintArraysActionPerformed(evt);
             }
         });
         mnuDebug.add(mnuItmPrintArrays);
 
         menuBar.add(mnuDebug);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addComponent(toolbar, javax.swing.GroupLayout.DEFAULT_SIZE, 921, Short.MAX_VALUE)
             .addComponent(pnlRootContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addComponent(toolbar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(0, 0, 0)
                 .addComponent(pnlRootContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void svgCanvasMouseClicked(java.awt.event.MouseEvent evt)//GEN-FIRST:event_svgCanvasMouseClicked
     {//GEN-HEADEREND:event_svgCanvasMouseClicked
         // left click
         if (evt.getButton() == MouseEvent.BUTTON1)
         {
             // run simulation
             svgCanvas.getOverlays().add(overlay);
             stimCol = evt.getX();
             stimRow = evt.getY();
             simulation.setStimulatedCell(stimRow, stimCol);
             simulation.run(runTime);
         }
 
         if (evt.getButton() == MouseEvent.BUTTON3)
         {
             // pause simulation
             simulation.pause();
         }
     }//GEN-LAST:event_svgCanvasMouseClicked
 
     private void btnOpenActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnOpenActionPerformed
     {//GEN-HEADEREND:event_btnOpenActionPerformed
         JFileChooser chooser = new JFileChooser(".");
         chooser.setFileFilter(new FileChooserFilter("svg", "SVG Files"));
         int result = chooser.showOpenDialog(this);
 
         if (result == JFileChooser.APPROVE_OPTION)
         {
             loadSVG(chooser.getSelectedFile());
         }
     }//GEN-LAST:event_btnOpenActionPerformed
 
     private void btnStartActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStartActionPerformed
     {//GEN-HEADEREND:event_btnStartActionPerformed
         svgCanvas.getOverlays().add(overlay);
         simulation.setStimulatedCell(stimRow, stimCol);
         simulation.run(runTime);
     }//GEN-LAST:event_btnStartActionPerformed
 
     private void btnStopActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStopActionPerformed
     {//GEN-HEADEREND:event_btnStopActionPerformed
         simulation.stop();
         svgCanvas.getOverlays().remove(overlay);
         svgCanvas.repaint();
     }//GEN-LAST:event_btnStopActionPerformed
 
     private void btnStepForwardActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnStepForwardActionPerformed
     {//GEN-HEADEREND:event_btnStepForwardActionPerformed
         simulation.run(1);
     }//GEN-LAST:event_btnStepForwardActionPerformed
 
     private void btnPauseActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnPauseActionPerformed
     {//GEN-HEADEREND:event_btnPauseActionPerformed
         simulation.pause();
     }//GEN-LAST:event_btnPauseActionPerformed
 
     private void mnuItmPrintArraysActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmPrintArraysActionPerformed
     {//GEN-HEADEREND:event_mnuItmPrintArraysActionPerformed
         ((Nishiyama) caModel).printArrays();
     }//GEN-LAST:event_mnuItmPrintArraysActionPerformed
 
     private void mnuItmPrintCellsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmPrintCellsActionPerformed
     {//GEN-HEADEREND:event_mnuItmPrintCellsActionPerformed
         ca.printCells();
     }//GEN-LAST:event_mnuItmPrintCellsActionPerformed
 
     private void mnuItmExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmExitActionPerformed
     {//GEN-HEADEREND:event_mnuItmExitActionPerformed
         System.exit(0);
     }//GEN-LAST:event_mnuItmExitActionPerformed
 
     private void mnuItmViewCellsActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmViewCellsActionPerformed
     {//GEN-HEADEREND:event_mnuItmViewCellsActionPerformed
         new CellsViewer(cellGenerator.getCells()).setVisible(true);
     }//GEN-LAST:event_mnuItmViewCellsActionPerformed
 
     private void mnuItmTransformActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmTransformActionPerformed
     {//GEN-HEADEREND:event_mnuItmTransformActionPerformed
         Dialog transformDialog = JAffineTransformChooser.createDialog(this, "Transform");
 
         AffineTransform txf = transformDialog.showDialog();
         if (txf != null)
         {
             AffineTransform at = svgCanvas.getRenderingTransform();
             if (at == null)
             {
                 at = new AffineTransform();
             }
 
             txf.concatenate(at);
             svgCanvas.setRenderingTransform(txf);
             generatorWorker = new CellGeneratorWorker();
             generatorWorker.execute();
         }
     }//GEN-LAST:event_mnuItmTransformActionPerformed
 
     private void mnuItmVerboseOutputActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmVerboseOutputActionPerformed
     {//GEN-HEADEREND:event_mnuItmVerboseOutputActionPerformed
         Application.getInstance().setDebugMode(mnuItmVerboseOutput.isSelected());
     }//GEN-LAST:event_mnuItmVerboseOutputActionPerformed
 
     private void mnuItmReloadActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_mnuItmReloadActionPerformed
     {//GEN-HEADEREND:event_mnuItmReloadActionPerformed
         loadSVG();
     }//GEN-LAST:event_mnuItmReloadActionPerformed
 
     private void txtTimeKeyReleased(java.awt.event.KeyEvent evt)//GEN-FIRST:event_txtTimeKeyReleased
     {//GEN-HEADEREND:event_txtTimeKeyReleased
         try
         {
             runTime = Integer.parseInt(txtTime.getText());
             txtTime.setForeground(null);
         }
         catch (NumberFormatException ex)
         {
             txtTime.setForeground(Color.red);
             Application.getInstance().output("Invalid time specified");
         }
 }//GEN-LAST:event_txtTimeKeyReleased
 
     private void btnProfileHelpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnProfileHelpActionPerformed
     {//GEN-HEADEREND:event_btnProfileHelpActionPerformed
         new HelpDialog(this, "Profile", false, "Select the heart tissue profile " +
                 "to use for this tissue.").setVisible(true);
 }//GEN-LAST:event_btnProfileHelpActionPerformed
 
     private void cboBoxProfileActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cboBoxProfileActionPerformed
     {//GEN-HEADEREND:event_cboBoxProfileActionPerformed
         HeartTissue tissue = (HeartTissue) cboBoxTissue.getSelectedItem();
         Profile profile = (Profile) cboBoxProfile.getSelectedItem();
 
         tissue.setModel(profile.loadParameters(tissue.getModel()));
         loadModelParameters();
 }//GEN-LAST:event_cboBoxProfileActionPerformed
 
     private void btnTissueHelpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnTissueHelpActionPerformed
     {//GEN-HEADEREND:event_btnTissueHelpActionPerformed
         new HelpDialog(this, "Heart tissue", false, "You can select which heart tissue to alter the parameters of. The tissues listed here were found in the SVG file.").setVisible(true);
 }//GEN-LAST:event_btnTissueHelpActionPerformed
 
     private void btnModelHelpActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnModelHelpActionPerformed
     {//GEN-HEADEREND:event_btnModelHelpActionPerformed
         new HelpDialog(this, caModel.getName(), false, caModel.getDescription()).setVisible(true);
 }//GEN-LAST:event_btnModelHelpActionPerformed
 
     private void cboBoxModelActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cboBoxModelActionPerformed
     {//GEN-HEADEREND:event_cboBoxModelActionPerformed
         loadModelParameters();
        HeartTissue tissue = (HeartTissue) cboBoxTissue.getSelectedItem();
        tissue.setModel(caModel);
         //rangeAxis.setRange(caModel.getMin(), caModel.getMax());
 }//GEN-LAST:event_cboBoxModelActionPerformed
 
     private void cboBoxTissueActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cboBoxTissueActionPerformed
     {//GEN-HEADEREND:event_cboBoxTissueActionPerformed
         Application.getInstance().output("Tissue changed");
         loadCAModels();
         loadProfiles();
 }//GEN-LAST:event_cboBoxTissueActionPerformed
 
     private void btnZoomOutActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnZoomOutActionPerformed
     {//GEN-HEADEREND:event_btnZoomOutActionPerformed
         svgCanvas.getOverlays().remove(overlay);
     }//GEN-LAST:event_btnZoomOutActionPerformed
 
     private void btnZoomInActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_btnZoomInActionPerformed
     {//GEN-HEADEREND:event_btnZoomInActionPerformed
         svgCanvas.getOverlays().remove(overlay);
     }//GEN-LAST:event_btnZoomInActionPerformed
 
     private void txtHeartRateActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_txtHeartRateActionPerformed
     {//GEN-HEADEREND:event_txtHeartRateActionPerformed
         simulation.setHeartRate(Integer.parseInt(txtHeartRate.getText()));
     }//GEN-LAST:event_txtHeartRateActionPerformed
 
     private void cboBoxSpeedActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_cboBoxSpeedActionPerformed
     {//GEN-HEADEREND:event_cboBoxSpeedActionPerformed
         simulation.setSpeed((Speed) cboBoxSpeed.getSelectedItem());
     }//GEN-LAST:event_cboBoxSpeedActionPerformed
 
     private void scrollPaneCanvasComponentResized(java.awt.event.ComponentEvent evt)//GEN-FIRST:event_scrollPaneCanvasComponentResized
     {//GEN-HEADEREND:event_scrollPaneCanvasComponentResized
         positionHeartInCentre();
     }//GEN-LAST:event_scrollPaneCanvasComponentResized
 
     /**
      * @param args the command line arguments
      */
     public static void main(String args[])
     {
         java.awt.EventQueue.invokeLater(new Runnable()
         {
             public void run()
             {
                 new MainUI3().setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnAbout;
     private javax.swing.JButton btnModelHelp;
     private javax.swing.JButton btnOpen;
     private javax.swing.JButton btnPause;
     private javax.swing.JButton btnProfileHelp;
     private javax.swing.JButton btnStart;
     private javax.swing.JButton btnStepForward;
     private javax.swing.JButton btnStop;
     private javax.swing.JButton btnTissueHelp;
     private javax.swing.JButton btnZoomIn;
     private javax.swing.JButton btnZoomOut;
     private javax.swing.JComboBox cboBoxModel;
     private javax.swing.JComboBox cboBoxProfile;
     private javax.swing.JComboBox cboBoxSpeed;
     private javax.swing.JComboBox cboBoxTissue;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JLabel lblHeart;
     private javax.swing.JLabel lblHeartRate;
     private javax.swing.JLabel lblHeartRateText;
     private javax.swing.JLabel lblModel;
     private javax.swing.JLabel lblProfile;
     private javax.swing.JLabel lblSpeed;
     private javax.swing.JLabel lblStatus;
     private javax.swing.JLabel lblTime;
     private javax.swing.JLabel lblTissue;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JMenu mnuDebug;
     private javax.swing.JMenu mnuFile;
     private javax.swing.JMenu mnuHeartTissue;
     private javax.swing.JMenuItem mnuItmExit;
     private javax.swing.JMenuItem mnuItmNotLoaded;
     private javax.swing.JMenuItem mnuItmPrintArrays;
     private javax.swing.JMenuItem mnuItmPrintCells;
     private javax.swing.JMenuItem mnuItmReload;
     private javax.swing.JMenuItem mnuItmTransform;
     private javax.swing.JCheckBoxMenuItem mnuItmVerboseOutput;
     private javax.swing.JMenuItem mnuItmViewCells;
     private javax.swing.JMenuItem mnuItmZoomOut;
     private javax.swing.JMenuItem mnuItmnZoomIn;
     private javax.swing.JMenu mnuView;
     private javax.swing.JPanel pnlCA;
     private javax.swing.JPanel pnlCanvasContainer;
     private javax.swing.JPanel pnlParameters;
     private javax.swing.JPanel pnlRootContainer;
     private javax.swing.JPanel pnlSimulationSettings;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JScrollPane scrollPaneCanvas;
     private javax.swing.JToolBar.Separator separatorControls;
     private javax.swing.JToolBar.Separator separatorFileOpen;
     private javax.swing.JSeparator separatorMnuFile;
     private javax.swing.JSeparator separatorMnuView;
     private javax.swing.JToolBar.Separator separatorZoom;
     private org.apache.batik.swing.JSVGCanvas svgCanvas;
     private javax.swing.JTabbedPane tabbedPane;
     private javax.swing.JToolBar toolbar;
     private javax.swing.JTextField txtHeartRate;
     private javax.swing.JTextField txtTime;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Interface events
      */
     public void cellGenerationStarted()
     {
         setStatusText("Generating cells for (this may take a while)...");
     }
 
     public void cellGenerationCompleted()
     {
         progressBar.setValue(cellGenerator.getProgress());
         setStatusText("Cells generated");
         ca.setCells(cellGenerator.getCells());
         ca.setTissues(cellGenerator.getTissues());
         ca.setTissueLocations(cellGenerator.getTissueNames());
         overlay.setSize(cellGenerator.getCells()[0].length, cellGenerator.getCells().length);
         loadHeartTissues();
         simulation.setInitialised(false);
     }
 
     public void documentLoadingStarted(SVGDocumentLoaderEvent e)
     {
         setStatusText("Document loading...");
         incrementProgressBar();
     }
 
     public void documentLoadingCompleted(SVGDocumentLoaderEvent e)
     {
         setStatusText("Document loaded");
         incrementProgressBar();
     }
 
     public void gvtBuildStarted(GVTTreeBuilderEvent e)
     {
         setStatusText("Build started...");
         incrementProgressBar();
     }
 
     public void gvtBuildCompleted(GVTTreeBuilderEvent e)
     {
         setStatusText("Build completed");
         incrementProgressBar();
     }
 
     public void gvtRenderingPrepare(GVTTreeRendererEvent e)
     {
         lblStatus.setText("Rendering started...");
         incrementProgressBar();
     }
 
     public void gvtRenderingCompleted(GVTTreeRendererEvent e)
     {
         lblStatus.setText("Rendering completed");
         incrementProgressBar();
     }
 
     public void documentLoadingCancelled(SVGDocumentLoaderEvent arg0)
     {
     }
 
     public void documentLoadingFailed(SVGDocumentLoaderEvent arg0)
     {
     }
 
     public void gvtBuildCancelled(GVTTreeBuilderEvent arg0)
     {
     }
 
     public void gvtBuildFailed(GVTTreeBuilderEvent arg0)
     {
     }
 
     public void gvtRenderingStarted(GVTTreeRendererEvent arg0)
     {
     }
 
     public void gvtRenderingCancelled(GVTTreeRendererEvent arg0)
     {
     }
 
     public void gvtRenderingFailed(GVTTreeRendererEvent arg0)
     {
     }
 
     public void simulationStarted()
     {
         Application.getInstance().output("Started");
         setStatusText("Simulation started");
         btnStop.setEnabled(true);
         btnPause.setEnabled(true);
         btnStart.setEnabled(false);
         progressBar.setMaximum(simulation.getRunTime());
         resetProgressBar();
     }
 
     public void simulationPaused()
     {
         Application.getInstance().output("Paused");
         setStatusText("Simulation paused");
         btnStop.setEnabled(true);
         btnPause.setEnabled(false);
         btnStart.setEnabled(true);
     }
 
     public void simulationUpdated()
     {
         incrementProgressBar();
         svgCanvas.repaint();
     }
 
     public void simulationStopped()
     {
         Application.getInstance().output("Stopped");
         setStatusText("Simulation stopped");
         btnStop.setEnabled(false);
         btnPause.setEnabled(false);
         btnStart.setEnabled(true);
     }
 
     public void simulationCompleted()
     {
         Application.getInstance().output("Completed");
         setStatusText("Simulation completed");
         btnStop.setEnabled(false);
         btnPause.setEnabled(false);
         btnStart.setEnabled(true);
         progressBar.setValue(progressBar.getMaximum());
     }
 
     public void componentTransformChanged(ComponentEvent event)
     {
         if (event.getID() == JGVTComponentListener.COMPONENT_TRANSFORM_CHANGED)
         {
             generatorWorker = new CellGeneratorWorker();
             generatorWorker.execute();
         }
     }
 
     public class CellGeneratorWorker extends SwingWorker
     {
         @Override
         protected Object doInBackground() throws Exception
         {
             progressBar.setMaximum(100);
             resetProgressBar();
             cellGenerator.run();
             while (!cellGenerator.isCompleted())
             {
                 progressBar.setValue(cellGenerator.getProgress());
                 Thread.sleep(1000);
             }
             return null;
         }
     }
 }
