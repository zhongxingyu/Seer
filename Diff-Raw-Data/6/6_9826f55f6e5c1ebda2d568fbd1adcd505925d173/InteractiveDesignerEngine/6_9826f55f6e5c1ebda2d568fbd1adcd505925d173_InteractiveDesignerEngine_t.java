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
 import com.trollsahead.qcumberless.device.InteractiveDesignerCallback;
 import com.trollsahead.qcumberless.device.InteractiveDesignerClient;
 import com.trollsahead.qcumberless.gui.CumberlessMouseListener;
 import com.trollsahead.qcumberless.gui.elements.*;
 import com.trollsahead.qcumberless.util.ElementHelper;
 import com.trollsahead.qcumberless.util.Util;
 
 import static com.trollsahead.qcumberless.gui.elements.Element.ColorScheme;
 
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.util.Set;
 
 public class InteractiveDesignerEngine implements CucumberlessEngine {
     private static final Color BG_COLOR_SCREENSHOT = new Color(0.0f, 0.0f, 0.0f, 0.5f);
 
     private static final String WAITING_FOR_DEVICE_TEXT = "WAITING FOR DEVICE...";
 
     private static ColorScheme originalColorScheme;
 
     private static BufferedImage screenshot = null;
 
     private static int screenshotX;
     private static int screenshotY;
 
     private static int screenWidth;
     private static int screenHeight;
 
     private static int screenshotRenderX;
     private static int screenshotRenderY;
 
     private static Device device = null;
     private static InteractiveDesignerClient client = null;
 
     private static String message;
 
     private BaseBarElement scenario;
 
     public void initialize() {
     }
 
     public void show() {
         DesignerEngine.stepsRoot.hide(false);
         scenario.groupParent.unfoldAll();
 
         originalColorScheme = DesignerEngine.colorScheme;
         DesignerEngine.setColorScheme(ColorScheme.DESIGN);
 
         screenshot = null;
         message = WAITING_FOR_DEVICE_TEXT;
 
         findDevice();
 
         new Thread(new Runnable() {
             public void run() {
                 client = device != null ? device.getInteractiveDesignerClient() : null;
                 if (client != null) {
                     client.setCallback(clientCallback);
                     client.start();
                 }
             }
         }).start();
     }
 
     public void hide() {
         if (client != null) {
             client.stop();
         }
         DesignerEngine.stepsRoot.show(false);
         DesignerEngine.setColorScheme(originalColorScheme);
         ElementHelper.removeFilter();
     }
 
     private void findDevice() {
         InteractiveDesignerEngine.device = null;
         for (Device device : Engine.devices) {
             if (device.isEnabled() && device.getCapabilities().contains(Device.Capability.INTERACTIVE_DESIGNING)) {
                 InteractiveDesignerEngine.device = device;
             }
         }
     }
     
     public void update() {
         Engine.designerEngine.update();
     }
     
     public void render(Graphics2D g) {
         Engine.drawBackgroundPicture(g);
         Engine.designerEngine.renderOnlyElements(g);
         drawMessage(g);
         drawScreenshot(g);
     }
 
     private void drawScreenshot(Graphics2D g) {
         if (screenshot == null) {
             return;
         }
         screenshotRenderX = DesignerEngine.dragSplitterX + ((Engine.windowWidth - DesignerEngine.dragSplitterX - screenWidth) / 2);
         screenshotRenderY = (Engine.windowHeight - screenHeight) / 2;
         g.setColor(BG_COLOR_SCREENSHOT);
         g.fillRect(screenshotRenderX, screenshotRenderY, screenWidth, screenHeight);
         g.drawImage(screenshot, screenshotRenderX + screenshotX, screenshotRenderY + screenshotY, null);
     }
 
     private void drawMessage(Graphics2D g) {
         if (Util.isEmpty(message)) {
             return;
         }
         int x = DesignerEngine.dragSplitterX + ((Engine.windowWidth - DesignerEngine.dragSplitterX - Engine.fontMetrics.stringWidth(message)) / 2);
         int y = 20;
 
         g.setColor(Color.WHITE);
         g.drawString(message, x, y);
     }
 
     public void postRender() {
         Engine.designerEngine.postRender();
     }
 
     public void resize() {
         Engine.designerEngine.resize();
     }
 
     public void mouseMoved() {
         Engine.designerEngine.mouseMoved();
     }
 
     public void mouseWheelMoved(int unitsToScroll) {
         Engine.designerEngine.mouseWheelMoved(unitsToScroll);
     }
 
     public void click(int clickCount) {
         if (isInsideScreenshotArea()) {
             client.click(CumberlessMouseListener.mouseX - screenshotRenderX, CumberlessMouseListener.mouseY - screenshotRenderY);
             return;
         }
         Engine.designerEngine.click(clickCount);
     }
 
     private boolean isInsideScreenshotArea() {
         return  CumberlessMouseListener.mouseX >= screenshotRenderX && CumberlessMouseListener.mouseY >= screenshotRenderY &&
                 CumberlessMouseListener.mouseX <= screenshotRenderX + screenWidth && CumberlessMouseListener.mouseY <= screenshotRenderY + screenHeight;
     }
 
     public void keyPressed(KeyEvent keyEvent) {
         if (keyEvent.getKeyCode() == KeyEvent.VK_ESCAPE) {
             Engine.prevEngine();
         }
     }
 
     public void startDrag(boolean isControlDown) {
         Engine.designerEngine.startDrag(isControlDown);
     }
 
     public void endDrag() {
         Engine.designerEngine.endDrag();
     }
 
     public void updateDrag() {
         Engine.designerEngine.updateDrag();
     }
 
     public void updateDevices(Set<Device> devices) {
         Engine.designerEngine.updateDevices(devices);
     }
 
     private InteractiveDesignerCallback clientCallback = new InteractiveDesignerCallback() {
         public void addStep(String step) {
            synchronized (Engine.DATA_LOCK) {
                scenario.addChild(new StepElement(Element.ROOT_FEATURE_EDITOR, step, FeatureLoader.findMatchingStep(step)));
                scenario.unfold();
            }
             System.out.println("Added step: " + step);
         }
 
         public void message(String message) {
             InteractiveDesignerEngine.message = message;
         }
 
         public void screenshot(BufferedImage screenshot, int x, int y, int width, int height, int screenWidth, int screenHeight) {
             InteractiveDesignerEngine.screenshot = screenshot;
             InteractiveDesignerEngine.screenWidth = screenWidth;
             InteractiveDesignerEngine.screenHeight = screenHeight;
             InteractiveDesignerEngine.screenshotX = x;
             InteractiveDesignerEngine.screenshotY = y;
         }
     };
 
     public void setElement(BaseBarElement element) {
         synchronized (Engine.DATA_LOCK) {
             if (element.type == BaseBarElement.TYPE_FEATURE) {
                 ScenarioElement scenario = new ScenarioElement(Element.ROOT_FEATURE_EDITOR, "Interactive Designer Scenario");
                 element.addChild(scenario);
                 this.scenario = scenario;
             } else {
                 this.scenario = element;
             }
             ElementHelper.filterFeaturesAndScenariosByElement(this.scenario);
         }
     }
 }
