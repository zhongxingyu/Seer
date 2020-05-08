 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.engine;
 
 import com.trollsahead.qcumberless.device.Device;
 import com.trollsahead.qcumberless.gui.Button;
 import com.trollsahead.qcumberless.gui.*;
 import com.trollsahead.qcumberless.plugins.Plugin;
 import com.trollsahead.qcumberless.util.ConfigurationManager;
 import com.trollsahead.qcumberless.util.Util;
 
 import java.awt.*;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.image.BufferedImage;
 import java.util.*;
 import java.util.List;
 
 public class Engine implements Runnable, ComponentListener, KeyListener {
     public static final Object DATA_LOCK = new Object();
     public static final Object RENDER_LOCK = new Object();
 
     public static final Font FONT_DEFAULT = new Font("Verdana", Font.PLAIN, 12);
 
     public static final int FRAME_RATE = 50;
     
     public static FontMetrics fontMetrics;
 
     private static List<CucumberlessEngine> engines;
     private static List<CucumberlessEngine> enginesHistory;
     public static CucumberlessEngine currentEngine = null;
     public static DesignerEngine designerEngine;
     public static TagsFilterEngine tagsFilterEngine;
     public static HistoryEngine historyEngine;
     public static InteractiveDesignerEngine interactiveDesignerEngine;
 
     public static enum AnimationState {NONE, ACTIVATING, DEACTIVATING, FORWARD, BACKWARD}
     public static AnimationState animationState = AnimationState.NONE;
     public static float animationProgress = 1.0f;
     public static BufferedImage animationBackground = null;
     public static Graphics2D animationGraphics = null;
 
     public static CumberlessCanvas canvas;
 
     public static CumberlessMouseListener mouseListener;
 
     public static int windowWidth;
     public static int windowHeight;
 
     public static final int DETAILS_HIGH = 0;
     public static final int DETAILS_MEDIUM = 1;
     public static final int DETAILS_LOW = 2;
 
     public static long fpsSecondTimer;
     public static long fpsFrameTimer;
     public static int fpsUpdateCount;
     public static int fpsLastCount;
     public static int fpsDetails = DETAILS_HIGH;
     public static boolean fpsShow = false;
 
     public static long renderCounter = 0;
 
     private static boolean isRunning;
 
     public static BufferedImage backbuffer = null;
     private static Graphics2D backbufferGraphics = null;
 
     public static List<Plugin> plugins = new LinkedList<Plugin>();
     public static Set<Device> devices = new HashSet<Device>();
 
     private static final long POLL_FOR_DEVICES_PERIOD = 1000L * 5;
 
     public static long lastTimePolledForDevices;
     public static boolean isPollingForDevices;
 
     public Engine() {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         windowWidth = screenSize.width;
         windowHeight = screenSize.height;
         createBackbuffer();
 
         RenderOptimizer.initialize();
 
         mouseListener = new CumberlessMouseListener();
 
         canvas = new CumberlessCanvas();
         canvas.addMouseListener(mouseListener);
         canvas.addMouseMotionListener(mouseListener);
         canvas.addMouseWheelListener(mouseListener);
         canvas.addComponentListener(this);
         canvas.addKeyListener(this);
 
         designerEngine = new DesignerEngine();
         tagsFilterEngine = new TagsFilterEngine();
         historyEngine = new HistoryEngine();
         interactiveDesignerEngine = new InteractiveDesignerEngine();
 
         engines = new LinkedList<CucumberlessEngine>();
         enginesHistory = new LinkedList<CucumberlessEngine>();
         engines.add(designerEngine);
         engines.add(tagsFilterEngine);
         engines.add(historyEngine);
         engines.add(interactiveDesignerEngine);
 
         resetFps();
     }
 
     public static void resetFps() {
         fpsSecondTimer = System.currentTimeMillis();
         fpsFrameTimer = fpsSecondTimer;
         fpsUpdateCount = 0;
         fpsLastCount = 0;
     }
 
     public static void initializePlugins() {
         for (Plugin plugin : plugins) {
             plugin.initialize();
         }
         lastTimePolledForDevices = 0;
         isPollingForDevices = false;
     }
 
     public CumberlessCanvas getCanvas() {
         return canvas;
     }
 
     public void show() {
         for (CucumberlessEngine engine : engines) {
             engine.initialize();
         }
         showEngine(designerEngine);
         animationState = AnimationState.NONE;
         animationProgress = 1.0f;
     }
 
     public static boolean prevEngine() {
         if (Util.isEmpty(enginesHistory)) {
             return showEngine(designerEngine);
         } else {
             CucumberlessEngine engine = enginesHistory.remove(enginesHistory.size() - 1);
             return showEngine(engine, false);
         }
     }
 
     public static boolean showEngine(CucumberlessEngine engine) {
         return showEngine(engine, true);
     }
 
     public static boolean showEngine(CucumberlessEngine engine, boolean pushToHistory) {
         synchronized (DATA_LOCK) {
             if (currentEngine == engine || animationState != AnimationState.NONE) {
                 return false;
             }
             createAnimationBackground();
             if (currentEngine != null) {
                 currentEngine.hide();
             }
             animationState = AnimationState.ACTIVATING;
             animationProgress = 0.0f;
             animationGraphics.drawImage(Engine.backbuffer, 0, 0, null);
             if (pushToHistory && currentEngine != null) {
                 enginesHistory.add(currentEngine);
             }
             engine.show();
             currentEngine = engine;
             return true;
         }
     }
 
     public static void stop() {
         isRunning = false;
         CucumberlessDialog.close();
     }
 
     public void run() {
         isRunning = true;
         while (isRunning) {
             if (currentEngine != null) {
                 break;
             }
             Util.sleep(100);
         }
         while (isRunning) {
             synchronized (DATA_LOCK) {
                 update();
                 synchronized (RENDER_LOCK) {
                     render();
                 }
                 postRender();
             }
             canvas.repaint();
             synchFramerate();
         }
     }
 
     private void update() {
         int lastFpsCount = fpsLastCount > 0 ? fpsLastCount : FRAME_RATE;
         int count = Math.max(1, FRAME_RATE / lastFpsCount);
         for (int i = 0; i < count; i++) {
             updateAnimation();
             Button.isOneTouched = false;
             pollForDevices();
             RenderOptimizer.update();
             if (DropDown.isVisible) {
                 DropDown.update();
             }
             currentEngine.update();
         }
     }
 
     private void updateAnimation() {
         if (animationProgress < 1.0f) {
             animationProgress = Math.min(1.0f, animationProgress + GuiUtil.DISAPPEAR_SPEED);
             if (animationProgress >= 1.0f) {
                 animationState = AnimationState.NONE;
             }
         }
     }
 
     private void render() {
         renderCounter++;
         setLevelOfDetails(backbufferGraphics);
         currentEngine.render(backbufferGraphics);
     }
 
     private void postRender() {
         currentEngine.postRender();
         if (animationState != AnimationState.NONE) {
             GuiUtil.renderAppearAnimation(backbufferGraphics, animationBackground, animationState, animationProgress);
         }
     }
 
     private void setLevelOfDetails(Graphics2D g) {
         if (fpsDetails != Engine.DETAILS_LOW) {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
             g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
         } else {
             g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
             g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
             g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
         }
     }
     
     private void synchFramerate() {
         long time = System.currentTimeMillis();
         if (time > fpsSecondTimer + 1000L) {
             fpsLastCount = fpsUpdateCount;
             String configDetails = ConfigurationManager.get("renderDetails");
             if (Util.isEmpty(configDetails) || "auto".equalsIgnoreCase(configDetails)) {
                 if (fpsUpdateCount >= 45) {
                     fpsDetails = DETAILS_HIGH;
                 }
                 if (fpsUpdateCount >= 30 && fpsUpdateCount <= 35) {
                     fpsDetails = DETAILS_MEDIUM;
                 }
                 if (fpsUpdateCount <= 20) {
                     fpsDetails = DETAILS_LOW;
                 }
             } else {
                 if ("low".equalsIgnoreCase(configDetails)) {
                     fpsDetails = DETAILS_LOW;
                 } else if ("medium".equalsIgnoreCase(configDetails)) {
                     fpsDetails = DETAILS_MEDIUM;
                 } else {
                     fpsDetails = DETAILS_HIGH;
                 }
             }
             fpsUpdateCount = 0;
             fpsSecondTimer = time;
         }
         fpsUpdateCount++;
         Util.sleep(Math.max(5, (1000L / FRAME_RATE) - (time - fpsFrameTimer)));
         fpsFrameTimer = System.currentTimeMillis();
     }
 
     private static void createBackbuffer() {
         if (backbufferGraphics != null) {
             backbufferGraphics.dispose();
         }
         backbuffer = RenderOptimizer.graphicsConfiguration.createCompatibleImage(windowWidth, windowHeight);
         backbufferGraphics = backbuffer.createGraphics();
         backbufferGraphics.setFont(FONT_DEFAULT);
         fontMetrics = backbufferGraphics.getFontMetrics();
         backbufferGraphics.setColor(Color.BLACK);
         backbufferGraphics.fillRect(0, 0, windowWidth + 1, windowHeight + 1);
     }
 
     private static void createAnimationBackground() {
         if (animationGraphics != null) {
             animationGraphics.dispose();
         }
         animationBackground = RenderOptimizer.graphicsConfiguration.createCompatibleImage(windowWidth, windowHeight);
         animationGraphics = animationBackground.createGraphics();
     }
 
     private static void setWindowSize(int width, int height) {
         windowWidth = width;
         windowHeight = height;
         if (width > backbuffer.getWidth() || height > backbuffer.getHeight()) {
             createBackbuffer();
         }
     }
 
     public void componentResized(ComponentEvent componentEvent) {
        synchronized (DATA_LOCK) {
             setWindowSize(canvas.getWidth(), canvas.getHeight());
             if (currentEngine != null) {
                 currentEngine.resize();
             }
         }
     }
 
     public void componentMoved(ComponentEvent componentEvent) {
     }
 
     public void componentShown(ComponentEvent componentEvent) {
     }
 
     public void componentHidden(ComponentEvent componentEvent) {
     }
 
     public static void click(int clickCount) {
         synchronized (DATA_LOCK) {
             if (DropDown.click()) {
                 return;
             }
         }
         currentEngine.click(clickCount);
     }
 
     public static void mousePressed(boolean isControlDown) {
         if (DropDown.mousePressed()) {
             return;
         }
         if (EditBox.mousePressed()) {
             return;
         }
         startDrag(isControlDown);
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
         if (currentEngine != null) {
             currentEngine.mouseMoved();
         }
         updateDrag();
     }
 
     public static void mouseWheelMoved(int unitsToScroll) {
         if (currentEngine != null) {
             currentEngine.mouseWheelMoved(unitsToScroll);
         }
     }
 
     private static void startDrag(boolean isControlDown) {
         if (currentEngine != null) {
             currentEngine.startDrag(isControlDown);
         }
     }
 
     private static void endDrag() {
         if (currentEngine != null) {
             currentEngine.endDrag();
         }
     }
 
     private static void updateDrag() {
         if (currentEngine != null) {
             currentEngine.updateDrag();
         }
     }
 
     public void keyTyped(KeyEvent keyEvent) {
     }
 
     public void keyPressed(KeyEvent keyEvent) {
         if (currentEngine != null) {
             currentEngine.keyPressed(keyEvent);
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
 
     public static boolean isInteractiveDesignerDeviceEnabled() {
         for (Device device : devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.INTERACTIVE_DESIGNING)) {
                 return true;
             }
         }
         return false;
     }
 
     private void pollForDevices() {
         if (isPollingForDevices || System.currentTimeMillis() < lastTimePolledForDevices + POLL_FOR_DEVICES_PERIOD) {
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
                 synchronized (DATA_LOCK) {
                     devices = newDevices;
                     currentEngine.updateDevices(devices);
                 }
                 isPollingForDevices = false;
                 lastTimePolledForDevices = System.currentTimeMillis();
             }
         }).start();
     }
 
     public static void drawBackgroundPicture(Graphics g) {
         BufferedImage backgroundImage = Images.getImage(Images.IMAGE_BACKGROUND, Images.ThumbnailState.NORMAL.ordinal());
         for (int y = 0; y <= Engine.windowHeight / backgroundImage.getHeight(); y++) {
             for (int x = 0; x <= Engine.windowWidth / backgroundImage.getWidth(); x++) {
                 g.drawImage(backgroundImage, x * backgroundImage.getWidth(), y * backgroundImage.getHeight(), null);
             }
         }
     }
 }
