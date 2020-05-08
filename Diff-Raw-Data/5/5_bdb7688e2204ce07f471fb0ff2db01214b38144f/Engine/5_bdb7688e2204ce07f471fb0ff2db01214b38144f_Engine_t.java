 // This file is part of Q-Cumberless Testing.
 //
 // Q-Cumberless Testing is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // Q-Cumberless Testing is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with Q-Cumberless Testing.  If not, see <http://www.gnu.org/licenses/>.
 //
 // Copyright 2012
 
 // Daniel Andersen (dani_ande@yahoo.dk)
 
 package com.trollsahead.qcumberless.engine;
 
 import com.trollsahead.qcumberless.device.Device;
 import com.trollsahead.qcumberless.gui.Button;
 import com.trollsahead.qcumberless.gui.Spotlight;
 import com.trollsahead.qcumberless.gui.*;
 import com.trollsahead.qcumberless.model.Step;
 import com.trollsahead.qcumberless.model.StepDefinition;
 import com.trollsahead.qcumberless.plugins.Plugin;
 import com.trollsahead.qcumberless.util.Util;
 
 import java.awt.*;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.*;
 import java.util.List;
 
 public class Engine implements Runnable, ComponentListener, KeyListener {
     public static final Object LOCK = new Object();
 
     private static final int FRAME_DELAY = 20;
 
     public static final Font FONT_DEFAULT = new Font("Verdana", Font.PLAIN, 12);
 
     public static CumberlessCanvas canvas;
 
     public static RootElement cucumberRoot = null;
     public static RootElement featuresRoot = null;
     public static RootElement stepsRoot = null;
 
     public static List<Step> stepDefinitions = null;
 
     public static CumberlessMouseListener mouseListener;
 
     private static Element oldTouchedElement = null;
     private static Element touchedElement = null;
     private static RootElement touchedRootElement = null;
 
     public static Element lastAddedElement = null;
 
     private static int dragSplitterX = 0;
 
     private static boolean canvasHasMouseFocus = true;
 
     public static int canvasWidth;
     public static int canvasHeight;
 
     public static final int DETAILS_ALL   = 0;
     public static final int DETAILS_FEWER = 1;
     public static final int DETAILS_NONE  = 2;
 
     private static long cycleTime;
     private static long fpsTimer;
     private static int fpsUpdateCount;
     private static int fpsLastCount;
     public static int fpsDetails = DETAILS_ALL;
     private static boolean fpsShow = false;
 
     private static boolean isRunning;
 
     public static ButtonBar buttonBar;
     public static Spotlight spotlight;
 
     public static String featuresBaseDir = null;
 
     public static BufferedImage backbuffer;
 
     private static final long POLL_FOR_DEVICES_PERIOD = 1000L * 5;
 
     public static List<Plugin> plugins = new LinkedList<Plugin>();
     public static Set<Device> devices = new HashSet<Device>();
     public static long lastTimePolledForDevices;
     public static boolean isPollingForDevices;
 
     public Engine() {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         canvasWidth = screenSize.width;
         canvasHeight = screenSize.height;
         createBackbuffer();
         
         mouseListener = new CumberlessMouseListener();
 
         canvas = new CumberlessCanvas();
         canvas.addMouseListener(mouseListener);
         canvas.addMouseMotionListener(mouseListener);
         canvas.addComponentListener(this);
         canvas.addKeyListener(this);
 
         buttonBar = new ButtonBar();
         spotlight = new Spotlight();
 
         cucumberRoot = new RootElement();
         scratchFeatures();
         resetStepDefinitions();
 
         initializePlugins();
 
         resetFps();
     }
 
     private static void resetFps() {
         fpsTimer = System.currentTimeMillis();
         fpsUpdateCount = 0;
         fpsLastCount = 0;
     }
 
     private void initializePlugins() {
         for (Plugin plugin : plugins) {
             plugin.initialize();
         }
         lastTimePolledForDevices = 0;
         isPollingForDevices = false;
     }
 
     public CumberlessCanvas getCanvas() {
         return canvas;
     }
 
     public static void stop() {
         isRunning = false;
     }
 
     public void run() {
         isRunning = true;
         cycleTime = System.currentTimeMillis();
         while (isRunning) {
             synchronized (LOCK) {
                 update();
                 render();
                 cucumberRoot.stickToParentRenderPosition(false);
             }
             canvas.repaint();
             synchFramerate();
         }
     }
 
     private void update() {
         Button.isOneTouched = false;
         pollForDevices();
         updateHighlight();
         buttonBar.update();
         spotlight.update();
         cucumberRoot.update(System.currentTimeMillis());
         if (DropDown.isVisible) {
             DropDown.update();
         }
         EasterEgg.update();
     }
 
     private void pollForDevices() {
         if (isPollingForDevices || System.currentTimeMillis() < lastTimePolledForDevices + POLL_FOR_DEVICES_PERIOD) {
             return;
         }
         if (Player.isRunning()) {
             return;
         }
         isPollingForDevices = true;
         new Thread(new Runnable() {
             public void run() {
                 Set<Device> newDevices = new HashSet<Device>();
                 for (Plugin plugin : plugins) {
                    Set<Device> devices = plugin.getDevices();
                    if (devices != null) {
                        newDevices.addAll(devices);
                    }
                 }
                 synchronized (LOCK) {
                     devices = newDevices;
                     buttonBar.updateDevices(devices);
                 }
                 isPollingForDevices = false;
                 lastTimePolledForDevices = System.currentTimeMillis();
             }
         }).start();
     }
 
     private void render() {
         Graphics g = backbuffer.getGraphics();
         g.setFont(FONT_DEFAULT);
         canvas.clear(g);
         cucumberRoot.render(g);
         if (DropDown.isVisible) {
             DropDown.render(g);
         }
         buttonBar.render(g);
         spotlight.render(g);
         Player.render(g);
         cucumberRoot.renderHints(g);
         renderFps(g);
     }
 
     private void renderFps(Graphics g) {
         if (!fpsShow) {
             return;
         }
         FontMetrics fontMetrics = g.getFontMetrics();
         String str = "FPS: " + fpsLastCount;
         int x = (canvasWidth - fontMetrics.stringWidth(str)) / 2;
         int y = canvasHeight - 5 - ButtonBar.BUTTONBAR_HEIGHT;
         g.setColor(Color.BLACK);
         g.drawString(str, x + 1, y + 1);
         g.setColor(Color.WHITE);
         g.drawString(str, x, y);
     }
 
     private void synchFramerate() {
         long time = System.currentTimeMillis();
         if (time > fpsTimer + 1000L) {
             fpsLastCount = fpsUpdateCount;
             if (fpsUpdateCount >= 45) {
                 fpsDetails = DETAILS_ALL;
             }
             if (fpsUpdateCount >= 30 && fpsUpdateCount <= 35) {
                 fpsDetails = DETAILS_FEWER;
             }
             if (fpsUpdateCount <= 25) {
                 fpsDetails = DETAILS_NONE;
             }
             fpsUpdateCount = 0;
             fpsTimer = time;
         }
         fpsUpdateCount++;
         cycleTime += FRAME_DELAY;
         long difference = cycleTime - time;
         Util.sleep(Math.max(5, difference));
     }
 
     private static void createBackbuffer() {
         backbuffer = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_ARGB);
         Graphics g = backbuffer.getGraphics();
         g.setColor(Color.BLACK);
         g.fillRect(0, 0, canvasWidth + 1, canvasHeight + 1);
     }
 
     public static void setWindowSize(int width, int height) {
         canvasWidth = width;
         canvasHeight = height;
         if (width > backbuffer.getWidth() || height > backbuffer.getHeight()) {
             createBackbuffer();
         }
         updateRootPositions();
     }
 
     public void componentResized(ComponentEvent componentEvent) {
         try {
             synchronized (LOCK) {
                 setWindowSize(canvas.getWidth(), canvas.getHeight());
                 Graphics g = backbuffer.getGraphics();
                 buttonBar.resize(g);
             }
         } catch (Exception e) {
             // Ignore!
         }
     }
 
     public void componentMoved(ComponentEvent componentEvent) {
     }
 
     public void componentShown(ComponentEvent componentEvent) {
     }
 
     public void componentHidden(ComponentEvent componentEvent) {
     }
 
     public static void click() {
         synchronized (LOCK) {
             if (buttonBar.click()) {
                 return;
             }
             if (DropDown.click()) {
                 return;
             }
             if (EditBox.click()) {
                 return;
             }
             if (touchedElement != null) {
                 touchedElement.click();
             }
         }
     }
 
     public static void mousePressed() {
         if (DropDown.mousePressed()) {
             return;
         }
         if (EditBox.mousePressed()) {
             return;
         }
         startDrag();
     }
 
     public static void mouseReleased() {
         if (DropDown.mouseReleased()) {
             return;
         }
         if (EditBox.mouseReleased()) {
             return;
         }
         endDrag();
     }
 
     public static void mouseDragged() {
         if (DropDown.mouseDragged()) {
             return;
         }
         if (EditBox.mouseDragged()) {
             return;
         }
         updateDrag();
     }
 
     public static void mouseMoved() {
         if (DropDown.mouseMoved()) {
             canvasHasMouseFocus = false;
             return;
         }
         if (EditBox.mouseMoved()) {
             canvasHasMouseFocus = false;
             return;
         }
         canvasHasMouseFocus = true;
         updateDrag();
     }
 
     private static void startDrag() {
         synchronized (Engine.LOCK) {
             if (touchedElement != null && touchedElement.isDragable()) {
                 touchedElement.startDrag();
             } else if (touchedRootElement != null) {
                 touchedRootElement.startDrag();
             }
         }
     }
 
     private static void endDrag() {
         synchronized (Engine.LOCK) {
             if (touchedElement != null) {
                 touchedElement.endDrag();
             } else if (touchedRootElement != null) {
                 touchedRootElement.endDrag();
             }
         }
     }
 
     private static void updateDrag() {
         if (!CumberlessMouseListener.isButtonPressed) {
             return;
         }
         synchronized (Engine.LOCK) {
             if (touchedElement != null && touchedElement.isBeingDragged()) {
                 touchedElement.applyDragOffset();
             } else if (touchedRootElement != null && touchedRootElement.isDragable()) {
                 touchedRootElement.scroll(CumberlessMouseListener.mouseY - CumberlessMouseListener.oldMouseY);
             }
         }
     }
 
     private static void updateHighlight() {
         if (!canvasHasMouseFocus) {
             return;
         }
         if (touchedElement != null && touchedElement.isBeingDragged()) {
             return;
         }
         findTouchedElement();
         if (touchedElement != oldTouchedElement) {
             toggleHighlight(oldTouchedElement, false);
             oldTouchedElement = touchedElement;
         }
         toggleHighlight(touchedElement, true);
     }
 
     private static void findTouchedElement() {
         touchedElement = cucumberRoot.findElement(CumberlessMouseListener.mouseX, CumberlessMouseListener.mouseY);
         touchedRootElement = CumberlessMouseListener.mouseX < dragSplitterX ? featuresRoot : stepsRoot;
     }
 
     private static void toggleHighlight(Element element, boolean highlight) {
         if (element != null) {
             element.highlight(highlight);
         }
     }
 
     public static void runTests(TextElement cucumberTextElement) {
         final StringBuilder feature = buildFeature(cucumberTextElement);
         System.out.println(feature.toString());
         Player.prepareRun();
         for (final Device device : devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.PLAY)) {
                 System.out.println("Running tests on device: " + device.name());
                 new Player().play(feature, device);
             }
         }
     }
 
     private static StringBuilder buildFeature(TextElement cucumberTextElement) {
         if (cucumberTextElement.type == TextElement.TYPE_FEATURE) {
             return cucumberTextElement.buildFeature();
         }
         StringBuilder sb = new StringBuilder();
         if (cucumberTextElement.type == TextElement.TYPE_SCENARIO) {
             sb.append("Feature: " + cucumberTextElement.groupParent.getTitle() + "\n\n");
             Element background = findBackgroundElement(cucumberTextElement.groupParent);
             if (background != null) {
                 sb.append(background.buildFeature());
             } else {
                 sb.append("Background:\n");
                 sb.append("\tWhen I see that I'm on the \"bank\" page\n\n");
             }
         }
         sb.append(cucumberTextElement.buildFeature());
         return sb;
     }
 
     private static Element findBackgroundElement(Element element) {
         for (Element child : element.children) {
             if (child.type == TextElement.TYPE_BACKGROUND) {
                 return child;
             }
         }
         return null;
     }
 
     public static void importSteps() {
         new Thread(new Runnable() {
             public void run() {
                 List<StepDefinition> stepDefinitions = plugins.get(0).getStepDefinitions(); // TODO!
                 synchronized (LOCK) {
                     CucumberStepDefinitionLoader.parseStepDefinitions(stepDefinitions);
                     featuresRoot.updateSteps();
                 }
             }
         }).start();
     }
 
     public static void importFeatures(File[] files) {
         resetFps();
         if (files == null || files.length == 0) {
             return;
         }
         featuresBaseDir = (files.length == 1 && files[0].isDirectory()) ? files[0].getAbsolutePath() : null;
         synchronized (LOCK) {
             try {
                 FeatureLoader.parseFeatureFiles(Util.getFeatureFiles(files));
             } catch (Exception e) {
                 e.printStackTrace();
             }
             Engine.featuresRoot.isLoaded = true;
         }
     }
 
     public static void exportFeatures(File directory) {
         resetFps();
         featuresRoot.export(directory);
     }
 
     public static void saveFeatures() {
         resetFps();
         featuresRoot.save();
     }
 
     public static void scratchFeatures() {
         resetFps();
         synchronized (LOCK) {
             resetFeatures();
             addTemplateFeature();
         }
     }
 
     public static void resetStepDefinitions() {
         resetFps();
         stepDefinitions = new ArrayList<Step>();
 
         cucumberRoot.removeChild(stepsRoot);
         stepsRoot = new RootElement();
         cucumberRoot.addChild(stepsRoot, 1);
 
         stepsRoot.addChild(new TextElement(TextElement.TYPE_FEATURE, TextElement.ROOT_STEP_DEFINITIONS, "Feature"));
         stepsRoot.addChild(new TextElement(TextElement.TYPE_SCENARIO, TextElement.ROOT_STEP_DEFINITIONS, "Scenario"));
         stepsRoot.addChild(new TextElement(TextElement.TYPE_COMMENT, TextElement.ROOT_STEP_DEFINITIONS, "Comment"));
         TextElement stepElement = new TextElement(TextElement.TYPE_STEP, TextElement.ROOT_STEP_DEFINITIONS, "New step");
         stepElement.step.isMatched = false;
         stepsRoot.addChild(stepElement);
 
         updateRootPositions();
     }
 
     public static void resetFeatures() {
         resetFps();
         featuresBaseDir = null;
         cucumberRoot.removeChild(Engine.featuresRoot);
         featuresRoot = new RootElement();
         featuresRoot.isLoaded = false;
         cucumberRoot.addChild(featuresRoot, 0);
         updateRootPositions();
     }
 
     private static void addTemplateFeature() {
         TextElement scenario = new TextElement(TextElement.TYPE_SCENARIO, TextElement.ROOT_FEATURE_EDITOR, "New Scenario");
         scenario.unfold();
         TextElement feature = new TextElement(TextElement.TYPE_FEATURE, TextElement.ROOT_FEATURE_EDITOR, "New Feature");
         feature.setFilename("noname_" + System.currentTimeMillis() + ".feature");
         feature.addChild(scenario);
         feature.unfold();
         featuresRoot.addChild(feature);
         updateLastAddedElement(scenario);
     }
 
     private static void updateRootPositions() {
         dragSplitterX = TextElement.RENDER_WIDTH_MAX_FEATURE_EDITOR + ((canvasWidth - TextElement.RENDER_WIDTH_MAX_STEP_DEFINITIONS - TextElement.RENDER_WIDTH_MAX_FEATURE_EDITOR) / 2);
         int divider = Math.max(dragSplitterX, canvasWidth - TextElement.RENDER_WIDTH_MAX_STEP_DEFINITIONS - RootElement.PADDING_HORIZONTAL * 2);
         cucumberRoot.setBounds(0, 0, 0, 0);
         if (featuresRoot != null) {
             featuresRoot.setBounds(0, 10, divider - 20, canvasHeight);
         }
         if (stepsRoot != null) {
             stepsRoot.setBounds(divider, 10, canvasWidth - divider, canvasHeight);
         }
     }
 
     public void keyTyped(KeyEvent keyEvent) {
     }
 
     public void keyPressed(KeyEvent keyEvent) {
         if (!EditBox.isVisible) {
             synchronized (LOCK) {
                 spotlight.searchKeyPressed(keyEvent);
             }
             if (keyEvent.getKeyChar() == '!') {
                 fpsShow = !fpsShow;
             }
         }
     }
 
     public void keyReleased(KeyEvent keyEvent) {
     }
 
     public static boolean isPlayableDeviceEnabled() {
         for (Device device : devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.PLAY)) {
                 return true;
             }
         }
         return false;
     }
 
     public static void updateLastAddedElement(Element element) {
         if (element.groupParent != stepsRoot) {
             lastAddedElement = element;
         }
     }
 
     public static List<String> getDefinedTags() {
         return Arrays.asList(featuresRoot.getTags().toArray(new String[0]));
     }
 }
