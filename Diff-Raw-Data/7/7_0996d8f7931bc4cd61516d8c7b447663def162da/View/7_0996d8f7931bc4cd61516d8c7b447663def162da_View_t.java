 /*
  * The MIT License
  *
  * Copyright 2013 Bernhard Sirlinger.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package mss;
 
 import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
 import java.awt.BorderLayout;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.File;
 import java.nio.ByteBuffer;
 import java.nio.DoubleBuffer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.concurrent.atomic.AtomicReference;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JSlider;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.filechooser.FileFilter;
 import mss.integratoren.Integratoren;
 import mss.integratoren.Rechenmodul;
 import mss.util.Notifications;
 import mss.util.Planet;
 import mss.util.ProjectFileSaver;
 import mss.util.ScreenshotSaver;
 import mss.util.Util;
 import mss.util.Vektor2D;
 import org.lwjgl.BufferUtils;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 
 /**
  *
  * @author Bernhard Sirlinger
  */
 public class View implements Observer, Runnable {
 
     private static boolean closeRequested = false;
     private final static AtomicReference<Dimension> newCanvasSize = new AtomicReference<>();
     private final Rechenmodul modul;
     private Thread rechenThread;
     private long speed = 100;
     private int currentIndex = 0;
     private double deltaT = 0.01;
 
     private enum ChangeType {
 
         INCREASE, DECREASE
     };
 
     private enum Directions {
 
         UP, DOWN, LEFT, RIGHT
     };
 
     private ArrayList<ArrayList<Planet>> results;
     private ArrayList<Planet> planets;
     private ArrayList<Planet> startPlanets;
 
     private long time;
     private String title;
     private final Canvas canvas = new Canvas();
     private final JFrame frame;
     private final JPanel panel = new JPanel();
     private final JSlider slider = new JSlider();
     private final JButton pauseButton;
     private final JButton playButton;
     private final JButton startCalculationButton;
     private final JButton resetButton;
     private final JButton takeScreenshotButton;
     private final JButton saveProjectButton;
 
     private final JTabbedPane tabbedPane;
     private final JPanel planetsPanel = new JPanel();
     private final JPanel currentDataPanel = new JPanel();
     private final JPanel settingsPanel = new JPanel();
 
     private final JComboBox<String> planetsBox;
     private final JComboBox<String> planetsClonedBox;
 
     private final JLabel integratorLabel;
     private final JComboBox<Integratoren> integratorBox;
     private final JLabel deltatLabel;
     private final JTextField deltatField;
     
     private boolean isPaused = true;
     private int zoomLevel = 1;
     private final DoubleBuffer buffer;
     private boolean wasInitialized = false;
     private boolean shouldReInit = false;
     private boolean shouldTakeScreenshot = false;
 
     private String lastOpenedFilePath = "";
     private String lastSavedFilePath = "";
     private final JMenuBar menuBar;
 
     public View(String title) {
         JPopupMenu.setDefaultLightWeightPopupEnabled(false);
         ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
         try {
             UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
         } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
         }
 
         this.planets = new ArrayList<>();
         this.planets.add(new Planet("Sun", new Vektor2D(0, 3), 1e10, 1, new Vektor2D(0, 0), new org.lwjgl.util.Color(255, 255, 255)));
         this.planets.add(new Planet("Planet", new Vektor2D(0, 0), 100, 0.5, new Vektor2D(-0.05, 0.05), new org.lwjgl.util.Color(244, 233, 10)));
         this.planets.add(new Planet("Planet2", new Vektor2D(0, -3), 1e10, 1, new Vektor2D(), new org.lwjgl.util.Color(255, 255, 255)));
         this.startPlanets = (ArrayList<Planet>) this.planets.clone();
 
         this.modul = new Rechenmodul(Integratoren.RUNGE_KUTTA_KLASSISCH, 0.01);
         this.modul.registerObserver("view", this);
 
         this.buffer = BufferUtils.createDoubleBuffer(16);
         this.initBuffer();
 
         this.title = title;
         this.frame = new JFrame(title);
 
         this.startCalculationButton = new JButton("Calculate");
         this.playButton = new JButton("Play");
         this.pauseButton = new JButton("Pause");
         this.resetButton = new JButton("Reset");
         this.takeScreenshotButton = new JButton("Take Screenshot");
         this.saveProjectButton = new JButton("Save Project");
 
         this.slider.setEnabled(false);
         this.slider.setMinimumSize(new Dimension(800, this.slider.getHeight()));
 
         this.panel.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
         this.panel.setPreferredSize(new Dimension(800, 100));
         this.panel.setBackground(Color.LIGHT_GRAY);
 
         this.panel.add(this.slider);
         this.panel.add(this.startCalculationButton);
         this.panel.add(this.playButton);
         this.panel.add(this.pauseButton);
         this.panel.add(this.resetButton);
         this.panel.add(this.takeScreenshotButton);
         this.panel.add(this.saveProjectButton);
 
         this.tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.WRAP_TAB_LAYOUT);
         this.tabbedPane.setPreferredSize(new Dimension(200, this.tabbedPane.getHeight()));
         this.tabbedPane.addTab("Start Values", this.planetsPanel);
         this.tabbedPane.addTab("Current Values", this.currentDataPanel);
         this.tabbedPane.addTab("Settings", this.settingsPanel);
 
         this.planetsPanel.setLayout(new FlowLayout());
 
         this.planetsBox = new JComboBox<>();
         this.planetsBox.addItem("Choose a Planet...");
         this.planetsClonedBox = new JComboBox<>();
         this.planetsClonedBox.addItem("Choose a Planet...");
         
         /*Settings*/
         this.integratorLabel = new JLabel("Numerical Method:");
         this.integratorBox = new JComboBox<>();
         this.integratorBox.addItem(Integratoren.EULER);
         this.integratorBox.addItem(Integratoren.LEAPFROG);
         this.integratorBox.addItem(Integratoren.RUNGE_KUTTA_KLASSISCH);
         this.integratorBox.setSelectedIndex(2);
         
         this.deltatLabel = new JLabel("Delta t:");
         this.deltatField = new JTextField("" + this.deltaT);
         
         this.settingsPanel.add(this.integratorLabel);
         this.settingsPanel.add(this.integratorBox);
         this.settingsPanel.add(this.deltatLabel);
         this.settingsPanel.add(this.deltatField);
         /*Settings*/
         
         this.planetsPanel.add(this.planetsBox, FlowLayout.LEFT);
         this.currentDataPanel.add(this.planetsClonedBox);
 
         this.addListeners();
 
         this.menuBar = new JMenuBar();
         this.menuBar.addFocusListener(new FocusAdapter() {
             @Override
             public void focusGained(FocusEvent e) {
                 System.out.println("Gained");
                 super.focusGained(e);
             }
         });
         this.frame.setJMenuBar(this.menuBar);
 
         JMenu fileMenu = new JMenu("File");
         JMenu helpMenu = new JMenu("Help");
 
         JMenuItem openFile = new JMenuItem("Open File");
         openFile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 isPaused = true;
                 openFile();
                 canvas.requestFocus();
             }
         });
         fileMenu.add(openFile);
 
         JMenuItem start = new JMenuItem("Start Simulation");
         start.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 modul.setData(planets);
                 rechenThread = new Thread(modul);
                 rechenThread.start();
                 isPaused = false;
                 canvas.requestFocus();
             }
         });
         fileMenu.add(start);
 
         JMenuItem reset = new JMenuItem("Reset Simulation");
         reset.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (results != null) {
                     planets = (ArrayList<Planet>) startPlanets.clone();
                     isPaused = true;
                     currentIndex = 0;
                     slider.setValue(0);
                 }
                 canvas.requestFocus();
             }
         });
         fileMenu.add(reset);
 
         JMenuItem pause = new JMenuItem("Pause");
         pause.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (!isPaused) {
                     isPaused = true;
                     ((JMenuItem) e.getSource()).setText("Restart");
                 } else {
                     isPaused = false;
                     ((JMenuItem) e.getSource()).setText("Pause");
                 }
                 canvas.requestFocus();
             }
         });
         fileMenu.add(pause);
 
         JMenuItem saveDataToFile = new JMenuItem("Save Current Data to File");
         saveDataToFile.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (results != null) {
                     System.out.println("Saving...");
                 }
                 canvas.requestFocus();
             }
         });
         fileMenu.add(saveDataToFile);
 
         JMenuItem about = new JMenuItem("About");
         about.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 ((JMenuItem) e.getSource()).setName("");
                 if (!isPaused) {
                     isPaused = true;
                     ((JMenuItem) e.getSource()).setName("selfPaused");
                 }
                 showAboutDialog();
                 canvas.requestFocus();
             }
         });
         helpMenu.add(about);
 
         this.menuBar.add(fileMenu);
         this.menuBar.add(helpMenu);
     }
 
     private void initBuffer() {
         this.buffer.put(0, this.zoomLevel);
         this.buffer.put(1, 0);
         this.buffer.put(2, 0);
         this.buffer.put(3, 0);
         this.buffer.put(4, 0);
         this.buffer.put(5, this.zoomLevel);
         this.buffer.put(6, 0);
         this.buffer.put(7, 0);
         this.buffer.put(8, 0);
         this.buffer.put(9, 0);
         this.buffer.put(10, this.zoomLevel);
         this.buffer.put(11, 0);
         this.buffer.put(12, 0);
         this.buffer.put(13, 0);
         this.buffer.put(14, 0);
         this.buffer.put(15, this.zoomLevel);
     }
 
     private void addListeners() {
         this.slider.addChangeListener(new ChangeListener() {
             @Override
             public void stateChanged(ChangeEvent e) {
                 JSlider slider = (JSlider) e.getSource();
                 currentIndex = slider.getValue();
                 planets = results.get(currentIndex);
                 if (slider.isFocusOwner()) {
                     isPaused = true;
                 }
             }
         });
 
         this.canvas.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 newCanvasSize.set(new Dimension(canvas.getSize().width, canvas.getSize().height));
             }
         });
 
         this.frame.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 layout(e.getComponent().getWidth(), e.getComponent().getHeight());
                 if (wasInitialized) {
                     shouldReInit = true;
                 }
             }
         });
 
         this.frame.addWindowListener(new WindowAdapter() {
             private boolean wasAlreadyPaused;
 
             @Override
             public void windowActivated(WindowEvent e) {
                 if (!this.wasAlreadyPaused) {
                     isPaused = false;
                 }
                 canvas.requestFocusInWindow();
             }
 
             @Override
             public void windowDeactivated(WindowEvent e) {
                 if (!isPaused) {
                     isPaused = true;
                     this.wasAlreadyPaused = false;
                 } else {
                     this.wasAlreadyPaused = true;
                 }
                 super.windowLostFocus(e);
             }
 
             @Override
             public void windowClosing(WindowEvent e) {
                 closeRequested = true;
             }
         });
 
         this.playButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 isPaused = false;
             }
         });
 
         this.pauseButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 isPaused = true;
             }
         });
 
         this.startCalculationButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 modul.setData(startPlanets);
                 rechenThread = new Thread(modul);
                 rechenThread.start();
                 isPaused = false;
                 canvas.requestFocus();
             }
         });
 
         this.resetButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 if (results != null) {
                     planets = (ArrayList<Planet>) startPlanets.clone();
                     isPaused = true;
                     currentIndex = 0;
                     slider.setValue(0);
                 }
                 canvas.requestFocus();
             }
         });
 
         this.takeScreenshotButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 shouldTakeScreenshot = true;
                 canvas.requestFocus();
             }
         });
         
         this.saveProjectButton.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 saveProject();
                 canvas.requestFocus();
             }
         });
 
         this.integratorBox.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 modul.setIntegrator((Integratoren) ((JComboBox) e.getSource()).getSelectedItem());
             }
         });
         
         this.deltatField.addFocusListener(new FocusAdapter() {
             @Override
             public void focusLost(FocusEvent e) {
                 String currentValue = ((JTextField)e.getSource()).getText();
                 if(currentValue.contains(",")) {
                     currentValue = currentValue.replace(',', '.');
                 }
                 
                 try {
                     deltaT = Double.parseDouble(currentValue);
                     modul.setDeltaT(deltaT);
                 } catch(NumberFormatException ex) {
                     JOptionPane.showMessageDialog(frame, "The value \"" + currentValue + "\" for deltaT is not valid.", "Invalid Value", JOptionPane.ERROR_MESSAGE);
                     deltatField.setText("" + deltaT);
                 }
             }
         });
     }
 
     private void layout(int width, int height) {
         this.slider.setSize(width - 14, this.slider.getHeight());
         this.slider.setPreferredSize(this.slider.getSize());
     }
 
     public void init() {
         updateComboBoxes();
 
         try {
             Display.setParent(this.canvas);
             Keyboard.enableRepeatEvents(true);
             this.frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
             this.frame.setLayout(new BorderLayout());
             this.frame.add(this.tabbedPane, BorderLayout.LINE_START);
             this.frame.add(this.canvas, BorderLayout.CENTER);
             this.frame.add(this.panel, BorderLayout.PAGE_END);
             this.frame.setPreferredSize(new Dimension(1024, 786));
             this.frame.setMinimumSize(new Dimension(800, 600));
             this.frame.pack();
 
             this.frame.setVisible(true);
 
             Display.setTitle(this.title);
             Display.setResizable(true);
             Display.setFullscreen(false);
             Display.create();
             this.initOpenGL();
             this.wasInitialized = true;
             LWJGLRenderer renderer = new LWJGLRenderer();
 
             Dimension newDim;
 
             while (!Display.isCloseRequested() && !View.closeRequested) {
                 if (this.shouldReInit) {
                     this.initOpenGL();
                     this.shouldReInit = false;
                 }
 
                 if (this.shouldTakeScreenshot) {
                     this.saveScreenshot();
                     this.shouldTakeScreenshot = false;
                 }
 
                 GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                 newDim = newCanvasSize.getAndSet(null);
                 checkKeyInput();
                 if (newDim != null) {
                     GL11.glViewport(0, 0, newDim.width, newDim.height);
                     renderer.syncViewportSize();
                 }
 
                 for (int i = 0; i < this.planets.size(); i++) {
                     this.planets.get(i).draw2D();
                 }
 
                 if (!this.isPaused && this.results != null && this.currentIndex < this.results.size() - 1 && this.getDelta() / this.deltaT >= this.speed) {
                     this.planets = this.results.get(this.currentIndex);
                     int add = (int) (1 / deltaT);
                     if (add == 0) {
                         add = 1;
                     }
                     this.time = this.getTime();
                     this.currentIndex += add;
                     this.slider.setValue(this.slider.getValue() + add);
                 }
 
                 checkKeyInput();
                 checkMouseInput();
                 Display.update();
                 Display.sync(60);
             }
 
             Display.destroy();
             this.frame.dispose();
         } catch (LWJGLException e) {
             System.out.println(e.getMessage());
             System.exit(-1);
         }
     }
 
     private void checkKeyInput() {
         while (Keyboard.next()) {
             if (!Keyboard.getEventKeyState()) {
                 switch (Keyboard.getEventKey()) {
                     case Keyboard.KEY_F1:
                         this.saveScreenshot();
                         break;
                     case Keyboard.KEY_SPACE:
                         this.isPaused = !this.isPaused;
                         break;
                 }
             }
 
             switch (Keyboard.getEventKey()) {
                 case Keyboard.KEY_UP:
                     this.changeTranslationMatrix(Directions.UP);
                     break;
                 case Keyboard.KEY_DOWN:
                     this.changeTranslationMatrix(Directions.DOWN);
                     break;
                 case Keyboard.KEY_LEFT:
                     this.changeTranslationMatrix(Directions.LEFT);
                     break;
                 case Keyboard.KEY_RIGHT:
                     this.changeTranslationMatrix(Directions.RIGHT);
                     break;
                 case Keyboard.KEY_0:
                     Keyboard.poll();
                     if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                         this.resetScaleMatrix();
                     }
                     break;
                 case Keyboard.KEY_C:
                     this.resetTranslationMatrix();
                     break;
                 case Keyboard.KEY_ADD:
                     if (this.speed >= 5 / this.deltaT) {
                     this.speed -= 5 / this.deltaT;
                 }
                     break;
                 case Keyboard.KEY_SUBTRACT:
                     this.speed += 5 / this.deltaT;
                     break;
             }
         }
     }
 
     private void initOpenGL() {
         GL11.glMatrixMode(GL11.GL_PROJECTION);
 
         GL11.glLoadMatrix(this.buffer);
 
         GL11.glOrtho(-Display.getWidth(), Display.getWidth(), -Display.getHeight(), Display.getHeight(), 1, -1);
 
         GL11.glMatrixMode(GL11.GL_MODELVIEW);
 
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
     }
 
     private void showAboutDialog() {
         JDialog dialog = new JDialog(this.frame, true);
         dialog.setSize(200, 200);
         dialog.setLocation(this.frame.getX() + this.frame.getWidth() / 2 - 100, this.frame.getY() + this.frame.getHeight() / 2 - 100);
         dialog.setEnabled(true);
         dialog.setVisible(true);
         dialog.addWindowListener(new WindowListener() {
             @Override
             public void windowOpened(WindowEvent e) {
             }
 
             @Override
             public void windowClosing(WindowEvent e) {
             }
 
             @Override
             public void windowClosed(WindowEvent e) {
             }
 
             @Override
             public void windowIconified(WindowEvent e) {
             }
 
             @Override
             public void windowDeiconified(WindowEvent e) {
             }
 
             @Override
             public void windowActivated(WindowEvent e) {
             }
 
             @Override
             public void windowDeactivated(WindowEvent e) {
                 isPaused = false;
                 e.getWindow().dispose();
             }
         });
     }
 
     private void saveScreenshot() {
         final File f = new File(View.class.getProtectionDomain().getCodeSource().getLocation().getPath());
         GL11.glReadBuffer(GL11.GL_FRONT);
         int width = Display.getWidth();
         int height = Display.getHeight();
         ByteBuffer byteBuffer = BufferUtils.createByteBuffer(width * height * 4);
         GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, byteBuffer);
         ScreenshotSaver saver = new ScreenshotSaver(byteBuffer, f.getParent(), width, height);
         saver.start();
     }
 
     private void saveProject() {
        boolean wasSelfPaused = !this.isPaused;
        
         this.isPaused = true;
         
         JFileChooser fileChooser;
 
         if (this.lastOpenedFilePath.isEmpty()) {
             fileChooser = new JFileChooser();
         } else {
             fileChooser = new JFileChooser(this.lastOpenedFilePath);
         }
 
         int state = fileChooser.showSaveDialog(this.frame);
         
         if(state == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             this.lastSavedFilePath = selectedFile.getAbsolutePath();
             
             ProjectFileSaver saver = new ProjectFileSaver(this.lastSavedFilePath, this.startPlanets, this.modul.getIntegrator(), this.deltaT);
             saver.start();
         }
        
        if(wasSelfPaused) {
            this.isPaused = false;
        }
     }
     
     @SuppressWarnings("unchecked")
     private void openFile() {
         JFileChooser fileChooser;
 
         if (this.lastOpenedFilePath.isEmpty()) {
             final File f = new File(View.class.getProtectionDomain().getCodeSource().getLocation().getPath());
             fileChooser = new JFileChooser(f.getParentFile());
         } else {
             fileChooser = new JFileChooser(this.lastOpenedFilePath);
         }
         int state = fileChooser.showOpenDialog(this.frame);
 
         if (state == JFileChooser.APPROVE_OPTION) {
             File selectedFile = fileChooser.getSelectedFile();
             this.lastOpenedFilePath = selectedFile.getAbsolutePath();
             HashMap<String, Object> dataFromDataFile = Util.getDataFromDataFile(selectedFile);
             if ("".equals((String) dataFromDataFile.get("Error")) && !((ArrayList<Planet>) dataFromDataFile.get("Planets")).isEmpty()) {
                 this.planets = (ArrayList<Planet>) dataFromDataFile.get("Planets");
                 this.startPlanets = (ArrayList<Planet>) this.planets.clone();
                 this.deltaT = (double) dataFromDataFile.get("deltaT");
                 this.modul.setDeltaT(this.deltaT);
                 this.modul.setIntegrator((Integratoren)dataFromDataFile.get("Integrator"));
                 this.integratorBox.setSelectedItem(this.modul.getIntegrator());
                 this.speed = (long) (1 / this.deltaT);
                 updateComboBoxes();
             } else {
                 this.showInvalidFileDialog((String) dataFromDataFile.get("Error"));
             }
             this.isPaused = true;
         } else {
             this.isPaused = false;
         }
     }
 
     private void updateComboBoxes() {
         int size = this.startPlanets.size();
         String temp;
 
         for (int i = 0; i < size; i++) {
             temp = this.startPlanets.get(i).getLabel();
             this.planetsBox.addItem(temp);
             this.planetsClonedBox.addItem(temp);
         }
     }
 
     private void showInvalidFileDialog(String errors) {
         System.out.println(errors);
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
         Display.setTitle(this.title);
     }
 
     public void setPlanets(ArrayList<Planet> planets) {
         this.planets = planets;
     }
 
     @Override
     public void run() {
         this.init();
     }
 
     @Override
     public void notify(Notifications type, String data) {
     }
 
     @Override
     public void sendPlanets(Notifications type, ArrayList<ArrayList<Planet>> planets) {
         this.results = planets;
         this.initSlider();
     }
 
     private void initSlider() {
         this.slider.setMaximum(this.results.size() - 2);
         this.slider.setMinorTickSpacing(1);
         this.slider.setValue(0);
         this.slider.setEnabled(true);
     }
 
     private void checkMouseInput() {
         int delta = Mouse.getDWheel();
 
         if (delta > 0) {
             this.changeZoomFactor(ChangeType.INCREASE);
         } else if (delta < 0) {
             this.changeZoomFactor(ChangeType.DECREASE);
         }
     }
 
     private void resetScaleMatrix() {
         this.zoomLevel = 0;
         this.buffer.put(0, this.zoomLevel);
         this.buffer.put(5, this.zoomLevel);
         this.buffer.put(10, this.zoomLevel);
         this.initOpenGL();
     }
 
     private void resetTranslationMatrix() {
         this.buffer.put(12, 0);
         this.buffer.put(13, 0);
         this.initOpenGL();
     }
 
     private void changeTranslationMatrix(Directions direction) {
         switch (direction) {
             case UP:
                 this.buffer.put(13, this.buffer.get(13) + 0.01);
                 break;
             case DOWN:
                 this.buffer.put(13, this.buffer.get(13) - 0.01);
                 break;
             case LEFT:
                 this.buffer.put(12, this.buffer.get(12) - 0.01);
                 break;
             case RIGHT:
                 this.buffer.put(12, this.buffer.get(12) + 0.01);
                 break;
         }
 
         this.initOpenGL();
     }
 
     private void changeZoomFactor(ChangeType change) {
         switch (change) {
             case DECREASE:
                 if (this.zoomLevel != 1) {
                 this.zoomLevel -= 1;
             } else {
                 this.zoomLevel = -1;
             }
                 break;
             case INCREASE:
                 if (this.zoomLevel != -1) {
                 this.zoomLevel += 1;
             } else {
                 this.zoomLevel = 1;
             }
                 break;
         }
 
         if (this.zoomLevel > 0) {
             this.buffer.put(0, this.zoomLevel);
             this.buffer.put(5, this.zoomLevel);
             this.buffer.put(10, this.zoomLevel);
         } else if (this.zoomLevel < 0) {
             this.buffer.put(0, 1.0 / -this.zoomLevel);
             this.buffer.put(5, 1.0 / -this.zoomLevel);
             this.buffer.put(10, 1.0 / -this.zoomLevel);
         }
 
         this.initOpenGL();
     }
 
     private long getTime() {
         return (Sys.getTime() * 1000 / Sys.getTimerResolution());
     }
 
     private long getDelta() {
         long current_time = this.getTime();
         long delta = current_time - this.time;
 
         return delta;
     }
 }
