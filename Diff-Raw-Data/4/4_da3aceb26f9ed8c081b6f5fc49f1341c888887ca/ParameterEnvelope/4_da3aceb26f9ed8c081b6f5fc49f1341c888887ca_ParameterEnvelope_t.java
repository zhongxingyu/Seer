 package jcue.domain.fadecue;
 
 import java.awt.geom.Point2D;
 import java.awt.geom.QuadCurve2D;
 import java.util.ArrayList;
 import jcue.domain.CueState;
 import jcue.domain.audiocue.AudioCue;
 import jcue.ui.CurvePanel;
 
 /**
  *
  * @author Jaakko
  */
 public class ParameterEnvelope implements Runnable {
     
     private Thread updater;
     private boolean running;
     
     private ArrayList<QuadCurve2D> curves;
     
     private long startTime;
     private double duration;
     
     private AudioCue targetCue;
 
     public ParameterEnvelope() {
         this.curves = new ArrayList<QuadCurve2D>();
         
         this.curves.add(new QuadCurve2D.Double(0, 0.5, 0.5, 0.5, 1, 0.5));
     }
     
     public void start() {
         if (this.updater == null) {
             this.updater = new Thread(this, "Envelope thread");
         }
         
         this.running = true;
         this.updater.start();
         
         this.startTime = System.nanoTime();
         
         this.targetCue.setState(CueState.FADING);
     }
     
     public void stop() {
         this.running = false;
         this.updater = null;
     }
     
     @Override
     public void run() {
         while (this.running) {
             long elapsedTime = (System.nanoTime() - this.startTime);
             long lDuration = (long) (this.duration * 1000000000);
             double position = ((double) elapsedTime / lDuration);
             
             if (elapsedTime > lDuration) {
                this.targetCue.setState(CueState.PLAYING);
                 this.stop();
                 break;
             }
             
             QuadCurve2D curveAtX = getCurveAtX(position);
             if (curveAtX != null) {
                 double t = findTforX(curveAtX, position);
                 double value = getCurveY(curveAtX, t);
                 
                 this.targetCue.getAudio().setMasterVolumeDirect(1.0 - value);
             }
             
             try {
                 Thread.sleep(1);
             } catch (Exception e) {
             }
         }
     }
     
     public ArrayList<QuadCurve2D> getCurves() {
         return curves;
     }
 
     public void setDuration(double duration) {
         this.duration = duration;
     }
 
     public double getDuration() {
         return duration;
     }
 
     public void setCurve(QuadCurve2D c, double x1, double y1, double ctrlX, double ctrlY, double x2, double y2, boolean link) {
         if (this.curves.contains(c)) {
             //Limit values
             int index = this.curves.indexOf(c);
             
             //First point must stay at the beginning
             if (index == 0) {
                 x1 = 0;
             }
             
             //Last point must stay at the end
             if (index == this.curves.size() - 1) {
                 x2 = 1;
             }
             
             if (x1 < 0) {
                 x1 = 0;
             } else if (x1 > 1) {
                 x1 = 1;
             } else if (x1 > c.getX2()) {    //Don't pass control point
                 x1 = c.getX2();
             }
             
             if (y1 < 0) {
                 y1 = 0; 
             } else if (y1 > 1) {
                 y1 = 1;
             }
             
             if (x2 < 0) {
                 x2 = 0;
             } else if (x2 > 1) {
                 x2 = 1;
             } else if (x2 < c.getX1()) {    //Don't pass control point
                 x2 = c.getX1();
             }
             
             if (y2 < 0) {
                 y2 = 0; 
             } else if (y2 > 1) {
                 y2 = 1;
             }
             
             //Calculate mouse deltas
             double dX1 = c.getX1() - x1;
             double dX2 = c.getX2() - x2;
             double dY1 = c.getY1() - y1;
             double dY2 = c.getY2() - y2;
             
             //Link endpoint and control point movement
             if (link) {
                 if (dX1 != 0) {
                     ctrlX -= dX1;
                 } else {
                     ctrlX -= dX2;
                 }
                 
                 if (dY1 != 0) {
                     ctrlY -= dY1;
                 } else {
                     ctrlY -= dY2;
                 }
             }
             
             //Control points must not pass endpoints
             if (ctrlX < x1) {
                 ctrlX = x1;
             } else if (ctrlX > x2) {
                 ctrlX = x2;
             }
             
             if (ctrlY < 0) {
                 ctrlY = 0;
             } else if (ctrlY > 1) {
                 ctrlY = 1;
             }
             
             //Adjust previous and next curve
             if (index > 0) {
                 QuadCurve2D prev = this.curves.get(index - 1);
                 
                 if (x1 < prev.getCtrlX()) {
                     x1 = prev.getCtrlX();
                 }
                 
                 prev.setCurve(prev.getP1(), prev.getCtrlPt(), new Point2D.Double(x1, y1));
             }
             
             if (index < this.curves.size() - 1) {
                 QuadCurve2D next = this.curves.get(index + 1);
                 
                 if (x2 > next.getCtrlX()) {
                     x2 = next.getCtrlX();
                 }
 
                 Point2D ctrlPoint = next.getCtrlPt();
                 //If linked adjust the next curve's control point also
                 if (link) {
                     double newCX = next.getCtrlX() - dX2;
                     double newCY = next.getCtrlY() - dY2;
                     
                     
                     //Control points must not pass endpoints
                     if (newCX < next.getX1()) {
                         newCX = next.getX1();
                     } else if (newCX > next.getX2()) {
                         newCX = next.getX2();
                     }
 
                     if (newCY < 0) {
                         newCY = 0;
                     } else if (newCY > 1) {
                         newCY = 1;
                     }
                     
                     ctrlPoint.setLocation(newCX, newCY);
                 }
                 
                 next.setCurve(new Point2D.Double(x2, y2), ctrlPoint, next.getP2());
             }
             
             //Adjust the curve itself
             c.setCurve(x1, y1, ctrlX, ctrlY, x2, y2);
         }
     }
     
     public void addPoint(double x, double y) {
         QuadCurve2D curveAtX = getCurveAtX(x);
         int index = this.curves.indexOf(curveAtX);
 
         QuadCurve2D newCurve = new QuadCurve2D.Double();
         
         double cX = curveAtX.getCtrlX();
         double cY = curveAtX.getCtrlY();
         
         if (cX > x) {
             cX = x;
         }
 
         curveAtX.setCurve(curveAtX.getP1(), new Point2D.Double(cX, cY), new Point2D.Double(x, y));
 
         double newCX, newCY, newX2, newY2;
 
         if (this.curves.size() > 1) {
             if (index == this.curves.size() - 1) {
                 newX2 = 1;
                 newY2 = 0.5;
             } else {
                 QuadCurve2D nextCurve = this.curves.get(index + 1);
 
                 newX2 = nextCurve.getX1();
                 newY2 = nextCurve.getY1();
             }
         } else {
             newX2 = 1;
             newY2 = 0.5;
         }
 
         newCX = x + (newX2 - x) / 2;
         newCY = y + (newY2 - y) / 2;
 
         newCurve.setCurve(x, y, newCX, newCY, newX2, newY2);
 
         this.curves.add(index + 1, newCurve);
 
     }
     
     public void deletePoint(QuadCurve2D c, int point) {
         if (!this.curves.contains(c)) {
             return;
         }
         
         if (point == CurvePanel.POINT_CTRL) {
             double newCX = (c.getX2() + c.getX1()) / 2;
             double newCY = (c.getY2() + c.getY1()) / 2;
             
             c.setCurve(c.getP1(), new Point2D.Double(newCX, newCY), c.getP2());
         }
         
         if (this.curves.size() > 1) {
             int index = this.curves.indexOf(c);
             
             if (point == CurvePanel.POINT_P1) {
                 if (index > 0) {
                     QuadCurve2D prev = this.curves.get(index - 1);
                     
                     prev.setCurve(prev.getP1(), prev.getCtrlPt(), new Point2D.Double(c.getX2(), c.getY2()));
                     
                     this.curves.remove(c);
                 }
             } else if (point == CurvePanel.POINT_P2) {
                 if (index < this.curves.size() - 1) {
                     QuadCurve2D next = this.curves.get(index + 1);
                     
                     deletePoint(next, CurvePanel.POINT_P1);
                 }
             }
         }
     }
 
     public void setTargetCue(AudioCue targetCue) {
         this.targetCue = targetCue;
     }
 
     public AudioCue getTargetCue() {
         return targetCue;
     }
     
     
     
     private QuadCurve2D getCurveAtX(double x) {
         for (QuadCurve2D c : this.curves) {
             if (x >= c.getX1() && x <= c.getX2()) {
                 return c;
             }
         }
         
         return null;
     }
     
     private double findTforX(QuadCurve2D curve, double x) {
         double X0 = curve.getX1();
         double X1 = curve.getCtrlX();
         double X2 = curve.getX2();
         
         double a = (X0 - 2 * X1 + X2);
         double b = (2 * X1 - 2 * X0);
         double c = (X0 - x);
         
         double[] eqn = {c, b, a};
         int roots = QuadCurve2D.solveQuadratic(eqn);
         
         double t = 0.0;
         if (roots == 1) {
             t = eqn[0];
         } else {
             if (eqn[0] > 1 || eqn[0] < 0) {
                 t = eqn[1];
             } else if (eqn[1] > 1 || eqn[1] < 0) {
                 t = eqn[0];
             }
         }
         
         return t;
     }
     
     private double getCurveY(QuadCurve2D curve, double t) {
         double Y0 = curve.getY1();
         double Y1 = curve.getCtrlY();
         double Y2 = curve.getY2();
         
         return Math.pow((1 - t), 2) * Y0 + 2 * (1 - t) * t * Y1 + Math.pow(t, 2) * Y2;
     }
     
     public double getEnvelopeY(double x) {
         QuadCurve2D curveAtX = getCurveAtX(x);
         return getCurveY(curveAtX, findTforX(curveAtX, x));
     }
 }
