 package com.katspow.caatjagwtdemos.client.welcome.showcase.scenes;
 
 import java.util.Arrays;
 import java.util.List;
 
 import com.katspow.caatja.behavior.AlphaBehavior;
 import com.katspow.caatja.behavior.BaseBehavior;
 import com.katspow.caatja.behavior.BaseBehavior.Status;
 import com.katspow.caatja.behavior.Interpolator;
 import com.katspow.caatja.behavior.RotateBehavior;
 import com.katspow.caatja.behavior.ScaleBehavior;
 import com.katspow.caatja.core.canvas.CaatjaContext2d;
 import com.katspow.caatja.core.canvas.CaatjaColor;
 import com.katspow.caatja.core.canvas.CaatjaGradient;
 import com.katspow.caatja.event.CAATMouseEvent;
 import com.katspow.caatja.foundation.Director;
 import com.katspow.caatja.foundation.Scene;
 import com.katspow.caatja.foundation.actor.Actor;
 import com.katspow.caatja.foundation.actor.Actor.Anchor;
 import com.katspow.caatja.foundation.actor.ActorContainer;
 import com.katspow.caatja.foundation.actor.SpriteActor;
 import com.katspow.caatja.foundation.image.CompoundImage;
 import com.katspow.caatja.foundation.ui.TextActor;
 import com.katspow.caatja.math.Pt;
 
 public class Scene3 {
 
     public static Scene init(Director director) throws Exception {
 
         final CompoundImage conpoundimage = new CompoundImage();
         conpoundimage.initialize(director.getImage("fish"), 1, 3);
 
         Scene scene = new Scene() {
             @Override
             public void paint(Director director, double time) {
                 CaatjaContext2d canvas = director.ctx;
 
                 canvas.setStrokeStyle(CaatjaColor.valueOf("black"));
 
                 for (int i = 0; i < 9; i++) {
                     canvas.strokeRect(60 + (conpoundimage.singleWidth * 2) * (i % 3), 60 + (conpoundimage.singleWidth)
                             * ((i / 3) >> 0), 48, 27);
 
                     canvas.strokeRect(60 + (conpoundimage.singleWidth * 2) * (i % 3), 60 + (conpoundimage.singleWidth)
                             * ((i / 3) >> 0) + 40 + conpoundimage.singleWidth * 3 + 40, 48, 27);
 
                 }
             }
 
         };
 
         CaatjaGradient gradient = director.ctx.createLinearGradient(0, 0, 0, 50);
         gradient.addColorStop(0, "blue");
         gradient.addColorStop(0.5, "orange");
         gradient.addColorStop(1, "yellow");
 
         ActorContainer cc = new ActorContainer();
         cc.setBounds(380, 30, 300, 150);
         scene.addChild(cc);
         cc.mouseEnabled = false;
 
         RotateBehavior rb = new RotateBehavior();
         rb.cycleBehavior = true;
         rb.setValues(-Math.PI/8,Math.PI/8, .5,0d);
         rb.setFrameTime(0, 4000);
         rb.setInterpolator(new Interpolator().createCubicBezierInterpolator(new Pt().set(0, 0),
                 new Pt().set(1, 0), new Pt().set(0, 1), new Pt().set(1, 1), true));
         cc.addBehavior(rb);
 
         TextActor text = new TextActor();
         text.setFont("50px sans-serif");
         text.setText("Anchored");
         text.calcTextSize(director);
         text.setLocation((cc.width-text.width)/2, 0);
         text.setTextFillStyle(gradient);
         text.outline = true;
         text.cacheAsBitmap();
         cc.addChild(text.setLocation((cc.width-text.textWidth)/2,0));
 
         TextActor text2 = new TextActor();
         text2.setFont("50px sans-serif");
         text2.setText("Affine");
         text2.calcTextSize(director);
         text2.setLocation((cc.width-text2.width)/2, 50);
         text2.setTextFillStyle(gradient);
         text2.outline = true;
         text2.cacheAsBitmap();
         cc.addChild(text2.setLocation((cc.width-text2.textWidth)/2,50));
 
         TextActor text3 = new TextActor();
         text3.setFont("50px sans-serif");
         text3.setText("Transforms");
         text3.calcTextSize(director);
         text3.setLocation((cc.width-text3.width)/2, 100);
         text3.setTextFillStyle(gradient);
         text3.outline = true;
         text3.cacheAsBitmap();
         cc.addChild(text3.setLocation((cc.width-text3.textWidth)/2,100));
 
         List<Anchor> anchors = Arrays.asList(Anchor.TOP_LEFT, Anchor.TOP, Anchor.TOP_RIGHT, Anchor.LEFT, Anchor.CENTER,
                 Anchor.RIGHT, Anchor.BOTTOM_LEFT, Anchor.BOTTOM, Anchor.BOTTOM_RIGHT);
 
         int i;
 
         // 10 peces con rotation y escalado. fijos sin path.
         for (i = 0; i < 9; i++) {
             SpriteActor p2 = new SpriteActor();
             p2.setAnimationImageIndex(Arrays.asList(0, 1, 2, 1));
             p2.setSpriteImage(conpoundimage);
             p2.changeFPS = 350;
             p2.setLocation(60 + (conpoundimage.singleWidth * 2) * (i % 3), 60 + (conpoundimage.singleWidth)
                     * ((i / 3) >> 0));
 
             RotateBehavior r2b = new RotateBehavior();
             r2b.cycleBehavior = true;
             r2b.setFrameTime(0, 2000);
             Pt anchor = scene.getAnchorPercent(anchors.get(i).getValue());
             r2b.setValues(0,2*Math.PI, anchor.x,anchor.y);
             p2.addBehavior(r2b);
 
             scene.addChild(p2);
         }
 
         double offset = 40 + conpoundimage.singleWidth * 3 + 40;
         for (i = 0; i < 9; i++) {
             SpriteActor p2 = new SpriteActor();
             p2.setAnimationImageIndex(Arrays.asList(0, 1, 2, 1));
             p2.setSpriteImage(conpoundimage);
             p2.changeFPS = 350;
             p2.setLocation(60 + (conpoundimage.singleWidth * 2) * (i % 3), 60 + (conpoundimage.singleWidth)
                     * ((i / 3) >> 0) + offset);
 
             ScaleBehavior sb = new ScaleBehavior();
             sb.cycleBehavior = true;
             sb.setFrameTime(0, 2000);
             Pt anchor= scene.getAnchorPercent(anchors.get(i).getValue());
             sb.setValues(.5, 1.5, .5, 1.5, anchor.x, anchor.y );
             sb.setPingPong();
             p2.addBehavior(sb);
 
             scene.addChild(p2);
         }
 
         int N = 16;
         int R = 100;
 
         for (i = 0; i < N; i++) {
             SpriteActor p2 = new SpriteActor() {
                 @Override
                 public void mouseEnter(CAATMouseEvent mouseEvent) {
                     Actor actor = mouseEvent.source;
                     if (null == actor) {
                         return;
                     }
                     BaseBehavior behaviour = actor.behaviorList.get(0);
                     if (null == behaviour) {
                         return;
                     }
 
                     if (behaviour.status == Status.EXPIRED) {
                         actor.behaviorList.get(0).setFrameTime(mouseEvent.source.time, 1000);
                     }
                 }
             };
 
             p2.setAnimationImageIndex(Arrays.asList(0, 1, 2, 1));
             p2.setSpriteImage(conpoundimage);
             p2.changeFPS = 350;
             double angle = ((double) i / N) * Math.PI * 2;
             p2.setLocation(525 + R * Math.cos(angle), 300 + R * Math.sin(angle));
             p2.setRotation(angle);
 
             ScaleBehavior sb = new ScaleBehavior();
             sb.setPingPong();
             sb.setValues(1,3,1,1, 0d,.5);
             p2.addBehavior(sb);
 
             AlphaBehavior ab = new AlphaBehavior();
             ab.setPingPong();
             ab.startAlpha = 1;
             ab.endAlpha = 0;
             ab.setFrameTime(i * 250, 2000);
             ab.setCycle(true);
             p2.addBehavior(ab);
 
             scene.addChild(p2);
         }
 
         return scene;
     }
 }
