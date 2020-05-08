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
 
 package com.trollsahead.qcumberless.gui;
 
 import com.trollsahead.qcumberless.engine.DesignerEngine;
 import com.trollsahead.qcumberless.engine.Engine;
 import com.trollsahead.qcumberless.gui.elements.*;
 
 import java.awt.*;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 public class EasterEgg {
     private static final int BACKGROUND_ANIMATION_DELAY = 20;
     private static final int BACKGROUND_ANIMATION_SIZE = 20;
 
     public static int animation = 0;
     public static boolean enabled = false;
 
     private static List<BaseBarElement> customElements = null;
 
     private static final float STICK_ANGLE_CUTOFF = 0.2f;
 
     private static final int STICK_WIDTH  = 10;
     private static final int STICK_HEIGHT = 150;
 
     private static int stickX;
     private static int stickY;
 
     private static final int BALL_SIZE  = 10;
     private static final float BALL_SPEED = 0.9f;
 
     private static boolean backgroundCoveringCanvas;
 
     private static long updateTime;
     private static int updateCount;
 
     private static final int BALL_TRACE_LENGTH = 20;
     private static final int BALL_TRACE_DELAY = 5;
 
     private static int ballTraceUpdateDelay;
     private static int[] ballTraceX;
     private static int[] ballTraceY;
 
     private static float ballX;
     private static float ballY;
     private static float ballVelX;
     private static float ballVelY;
     private static float ballSpeed;
     private static boolean ballSticky;
 
     private static Cursor savedCursor;
 
     public static void update() {
         if (!enabled) {
             return;
         }
         updateElements();
         updateSpeed();
         updateStick();
         updateBall();
         updateCount++;
     }
 
     private static void updateElements() {
         if (DesignerEngine.featuresRoot.children.size() == 0 && customElements.size() == 0) {
             createCustomElements();
         }
         for (Element element : customElements) {
             element.update(System.currentTimeMillis());
         }
     }
 
     private static void createCustomElements() {
         ballSticky = true;
 
         customElements = new LinkedList<BaseBarElement>();
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "You fight with the strength of many men, Sir Knight."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "I am Arthur, King of the Britons."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "I seek the finest and the bravest knights in the land to join me in my court at Camelot."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "You have proved yourself worthy. Will you join me?"));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "You make me sad. So be it. Come, Patsy."));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "None shall pass."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "What?"));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "None shall pass."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "I have no quarrel with you, good Sir Knight, but I must cross this bridge."));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "Then you shall die."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "I command you, as King of the Britons, to stand aside!"));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "I move for no man."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "So be it!"));
 
         customElements.add(createElement(BaseBarElement.TYPE_STEP,     0, 0, 200, "Aaah!, hiyaah!"));
 
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "Now stand aside, worthy adversary."));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "'Tis but a scratch."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "A scratch? Your arm's off!"));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "No, it isn't."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "Well, what's that, then?"));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "I've had worse."));
 
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "You liar!"));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "Come on, you pansy!"));
 
         customElements.add(createElement(BaseBarElement.TYPE_STEP,     0, 0, 200, "Huyah!"));
         customElements.add(createElement(BaseBarElement.TYPE_STEP,     0, 0, 200, "Hiyaah!"));
         customElements.add(createElement(BaseBarElement.TYPE_STEP,     0, 0, 200, "Aaaaaaaah!"));
         customElements.add(createElement(BaseBarElement.TYPE_STEP,     0, 0, 200, "Huyah!"));
 
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "Victory is mine!"));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "We thank Thee Lord, that in Thy mer--"));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "Hah!"));
 
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "Come on, then."));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "What?"));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "Have at you!"));
 
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "Eh. You are indeed brave, Sir Knight, but the fight is mine."));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "Oh, had enough, eh?"));
         customElements.add(createElement(BaseBarElement.TYPE_SCENARIO, 0, 0, 200, "Look, you stupid bastard. You've got no arms left."));
         customElements.add(createElement(BaseBarElement.TYPE_COMMENT,  0, 0, 200, "Yes, I have."));
 
         final int COL_WIDTH = 210;
         final int ROW_HEIGHT = 80;
         List<Integer> x = new ArrayList<Integer>();
         List<Integer> y = new ArrayList<Integer>();
         for (int i = 0; i < customElements.size(); i++) {
             int currentX = i % 4;
             int currentY = i / 4;
             x.add(20 + (currentX * COL_WIDTH));
             y.add(10 + (currentY * ROW_HEIGHT));
         }
         int idx = 0;
         while (!x.isEmpty()) {
             int i = (int) (Math.random() * x.size());
             customElements.get(idx).animation.moveAnimation.setRealPosition(x.get(i), y.get(i));
             x.remove(i);
             y.remove(i);
             idx++;
         }
     }
 
     private static BaseBarElement createElement(int type, int x, int y, int width, String text) {
         BaseBarElement element;
         switch (type) {
             case BaseBarElement.TYPE_SCENARIO:
                 element = new ScenarioElement(BaseBarElement.ROOT_NONE, text);
                 break;
             case BaseBarElement.TYPE_COMMENT:
                 element = new CommentElement(BaseBarElement.ROOT_NONE, text);
                 break;
             default:
                 element = new StepElement(BaseBarElement.ROOT_NONE, text);
                 break;
         }
        element.animation.colorAnimation.setAlpha(Animation.FADE_ALPHA_DEFAULT, Animation.FADE_SPEED_REENTRANCE);
         element.animation.moveAnimation.setRealPosition(x, y);
         element.animation.moveAnimation.setRenderPosition(x, y);
         element.renderWidth = width;
         return element;
     }
 
     private static void updateSpeed() {
         if (System.currentTimeMillis() > updateTime + 1000L) {
             long deltaTime = System.currentTimeMillis() - updateTime;
             updateTime = System.currentTimeMillis();
             ballSpeed = (int) Math.max(((float) deltaTime / (float) updateCount) * BALL_SPEED, 1);
             updateCount = 0;
         }
     }
 
     private static void updateStick() {
         stickX = Engine.windowWidth - STICK_WIDTH - 40;
         stickY = CumberlessMouseListener.mouseY;
         if (stickY < 0) {
             stickY = 0;
         }
         if (stickY + STICK_HEIGHT > DesignerEngine.canvasHeight) {
             stickY = DesignerEngine.canvasHeight - STICK_HEIGHT;
         }
     }
 
     private static void updateBall() {
         if (ballSticky) {
             ballX = stickX - (BALL_SIZE / 2);
             ballY = stickY + (STICK_HEIGHT / 2);
             if (CumberlessMouseListener.isButtonPressed) {
                 ballVelX = (float) -Math.cos(0.3f);
                 ballVelY = (float) -Math.sin(0.3f);
                 ballSticky = false;
                 initBallTrace();
             }
         } else {
             for (int i = 0; i < ballSpeed; i++) {
                 moveBall();
                 updateBallTrace();
             }
         }
     }
 
     private static void updateBallTrace() {
         ballTraceUpdateDelay++;
         if (ballTraceUpdateDelay < BALL_TRACE_DELAY) {
             return;
         }
         ballTraceUpdateDelay = 0;
         for (int i = BALL_TRACE_LENGTH - 1; i > 0; i--) {
             ballTraceX[i] = ballTraceX[i - 1];
             ballTraceY[i] = ballTraceY[i - 1];
         }
         ballTraceX[0] = (int) ballX;
         ballTraceY[0] = (int) ballY;
     }
 
     private static void initBallTrace() {
         ballTraceUpdateDelay = 0;
         ballTraceX = new int[BALL_TRACE_LENGTH];
         ballTraceY = new int[BALL_TRACE_LENGTH];
         for (int i = 0; i < BALL_TRACE_LENGTH; i++) {
             ballTraceX[i] = (int) ballX;
             ballTraceY[i] = (int) ballY;
         }
     }
 
     private static void moveBall() {
         ballX += ballVelX * BALL_SPEED;
         ballY += ballVelY * BALL_SPEED;
         checkBorders();
         checkElements();
     }
 
     private static void checkBorders() {
         if (ballX < 0.0f && ballVelX < 0.0f) {
             ballX = -ballX;
             ballVelX = -ballVelX;
         }
         if (ballX > Engine.windowWidth && ballVelX > 0.0f) {
             hide();
         }
         if (ballY < 0.0f && ballVelY < 0.0f) {
             ballY = -ballY;
             ballVelY = -ballVelY;
         }
         if (ballY > DesignerEngine.canvasHeight && ballVelY > 0.0f) {
             ballY = DesignerEngine.canvasHeight - (ballY - DesignerEngine.canvasHeight);
             ballVelY = -ballVelY;
         }
         if (ballVelX > 0.0f &&
                 ballX + (BALL_SIZE / 2) >= stickX && ballX - (BALL_SIZE / 2) <= stickX + STICK_WIDTH &&
                 ballY + (BALL_SIZE / 2) >= stickY && ballY - (BALL_SIZE / 2) <= stickY + STICK_HEIGHT) {
             double angle = (STICK_ANGLE_CUTOFF / 2.0f) + (ballY - (stickY + (STICK_HEIGHT / 2))) * Math.PI * (1.0f - STICK_ANGLE_CUTOFF) / (STICK_HEIGHT + BALL_SIZE);
             ballVelX = (float) -Math.cos(angle);
             ballVelY = (float) Math.sin(angle);
         }
     }
 
     private static void checkElements() {
         bounceOnElement(
                 -(BALL_SIZE / 2), 0,
                  (BALL_SIZE / 2), 0,
                 0, -(BALL_SIZE / 2),
                 0,  (BALL_SIZE / 2)
         );
     }
 
     private static Element bounceOnElement(int ... points) {
         for (int i = 0; i < points.length; i += 2) {
             int deltaX = points[i + 0];
             int deltaY = points[i + 1];
             int x = (int) (ballX + deltaX);
             int y = (int) (ballY + deltaY);
             Element element = findElement(x, y);
             if (element != null && element.isVisible()) {
                 if (deltaX < 0 && ballVelX < 0.0f) {
                     ballVelX = -ballVelX;
                 }
                 if (deltaX > 0 && ballVelX > 0.0f) {
                     ballVelX = -ballVelX;
                 }
                 if (deltaY < 0 && ballVelY < 0.0f) {
                     ballVelY = -ballVelY;
                 }
                 if (deltaY > 0 && ballVelY > 0.0f) {
                     ballVelY = -ballVelY;
                 }
                 removeElement(element);
                 return element;
             }
         }
         return null;
     }
 
     private static void removeElement(Element element) {
         if (DesignerEngine.featuresRoot.children.size() > 0) {
             if (element.children.size() <= 0) {
                 element.trashElement();
             } else if (element.isFolded()) {
                 element.unfold();
             }
         } else {
             if (element.type == BaseBarElement.TYPE_SCENARIO ||
                 (element.type == BaseBarElement.TYPE_COMMENT && !hasElementLeft(BaseBarElement.TYPE_SCENARIO)) ||
                 (element.type == BaseBarElement.TYPE_STEP && !hasElementLeft(BaseBarElement.TYPE_COMMENT))) {
                 customElements.remove(element);
             }
         }
     }
 
     private static boolean hasElementLeft(int type) {
         for (BaseBarElement element : customElements) {
             if (element.type == type) {
                 return true;
             }
         }
         return false;
     }
 
     private static Element findElement(int x, int y) {
         if (customElements.size() > 0) {
             for (Element element : customElements) {
                 Element foundElement = element.findElement(x, y);
                 if (foundElement != null) {
                     return foundElement;
                 }
             }
         } else {
             return DesignerEngine.cucumberRoot.findElement(x, y);
         }
         return null;
     }
 
     public static void render(Graphics2D g) {
         drawBackground(g);
         if (enabled) {
             drawElements(g);
             drawBall(g);
             drawStick(g);
         }
     }
 
     private static void drawElements(Graphics2D g) {
         for (Element element : customElements) {
             element.render(g);
         }
     }
 
     private static void drawStick(Graphics g) {
         g.setColor(new Color(1.0f, 0.5f, 0.5f));
         g.fillRect(stickX, stickY, STICK_WIDTH + 1, STICK_HEIGHT + 1);
     }
 
     private static void drawBall(Graphics g) {
         if (!ballSticky) {
             for (int i = 0; i < BALL_TRACE_LENGTH; i++) {
                 int idx = BALL_TRACE_LENGTH - i - 1;
                 float alpha = (float) i / (float) BALL_TRACE_LENGTH;
                 g.setColor(new Color(0.5f * alpha, 0.5f * alpha, 0.1f * alpha));
                 g.fillOval(ballTraceX[idx] - (BALL_SIZE / 2), ballTraceY[idx] - (BALL_SIZE / 2), BALL_SIZE, BALL_SIZE);
             }
         }
         g.setColor(new Color(1.0f, 1.0f, 0.2f));
         g.fillOval((int) ballX - (BALL_SIZE / 2), (int) ballY - (BALL_SIZE / 2), BALL_SIZE, BALL_SIZE);
     }
 
     private static void drawBackground(Graphics g) {
         animation += enabled ? 1 : -1;
         int animationMax = DesignerEngine.canvasHeight * 2 / BACKGROUND_ANIMATION_SIZE;
         animation = Math.max(0, Math.min(animation, animationMax));
         backgroundCoveringCanvas = animation >= animationMax;
         g.setColor(Color.BLACK);
         int top = 0;
         for (int y = 0; y < DesignerEngine.canvasHeight; y += BACKGROUND_ANIMATION_SIZE) {
             int height = Math.min(animation - (y / BACKGROUND_ANIMATION_DELAY), BACKGROUND_ANIMATION_SIZE);
             if (height < BACKGROUND_ANIMATION_SIZE) {
                 if (height >= 0) {
                     g.fillRect(0, top, Engine.windowWidth, y - top + height);
                 }
                 top = y + BACKGROUND_ANIMATION_SIZE;
             }
         }
         if (top < DesignerEngine.canvasHeight) {
             g.fillRect(0, top, Engine.windowWidth, DesignerEngine.canvasHeight - top);
         }
     }
 
     public static void show() {
         synchronized (Engine.DATA_LOCK) {
             enabled = true;
             customElements = new LinkedList<BaseBarElement>();
             updateTime = System.currentTimeMillis();
             updateCount = 0;
             if (animation <= 0) {
                 animation = 1;
                 backgroundCoveringCanvas = false;
             }
             ballSticky = true;
             DesignerEngine.featuresRoot.scrollToTop();
             BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
             Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
             savedCursor = Engine.canvas.getCursor();
             Engine.canvas.setCursor(blankCursor);
         }
     }
 
     public static void hide() {
         synchronized (Engine.DATA_LOCK) {
             enabled = false;
             DesignerEngine.spotlight.clear();
             Engine.canvas.setCursor(savedCursor);
         }
     }
 
     public static boolean isBackgroundCoveringCanvas() {
         return backgroundCoveringCanvas;
     }
 }
