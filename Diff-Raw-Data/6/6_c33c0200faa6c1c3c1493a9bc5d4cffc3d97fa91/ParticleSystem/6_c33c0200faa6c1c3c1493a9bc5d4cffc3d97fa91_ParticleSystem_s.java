 import java.util.ArrayList;
 import java.awt.Graphics;
 import java.util.Random;
 
 class ParticleSystem {
   private ArrayList<Point> points;
   private int index;
  
   public ParticleSystem(ArrayList<Point> points) {
     this.index = 0;
     this.points = points;
     assignIDs();
   }
 
   public ParticleSystem(int n) {
     Random rand = new Random();
     points = new ArrayList<Point>();
 
     for (int i = 0; i < n; i++) {
       Velocity v = new Velocity();
      /*
       v.setM(rand.nextInt(Constants.POINT_MAX_M) + Constants.POINT_MIN_M);
       v.setT(rand.nextInt(Constants.POINT_MAX_T));
 
      v.setM(rand.nextInt(Constants.POINT_MAX_M));
      v.setT(rand.nextInt(Constants.POINT_MAX_T));      
      */

       Point p = new Point();
       p.setX(rand.nextInt(Constants.FRAME_WIDTH));
       p.setY(Constants.FRAME_OFFSET + rand.nextInt(Constants.FRAME_HEIGHT));
       p.setM(rand.nextInt(Constants.POINT_MAX_MASS)+Constants.POINT_MIN_MASS);
       p.setV(v);
       points.add(p);
     }
     assignIDs();
   }
 
   public void computeFrame() {
     for (Point p : points) {
       for (Point k : points) {
         if (p.sameAs(k)) continue;
 
         else if (p.closeTo(k)) {
           Point[] ps = p.interact(k);
           p = ps[0];
           k = ps[1];
         }
 
 	else {
 	  k.move(Constants.FRAME_REFRESH_RATE);
 	}
       }
     }
   }
 
   public void assignID(Point p) {
     p.setID(this.index);
     this.index++;
   }
 
   public void assignIDs() {
     for (Point p : points) assignID(p);
   }
 
   public void prerender(Graphics g) {
     g.setColor(Constants.FRAME_BACKGROUND);
     for (Point p : points) p.erase(g);
   }
 
   public void render(Graphics g) {
     g.setColor(Constants.POINT_COLOR);
     for (Point p : points) p.draw(g);
   }
 
   public boolean haveFrame() {
     return true;
   }
 
   public void print() {
     for (Point p : points) {
       System.out.println(p.toString());
     }
   }
 }
