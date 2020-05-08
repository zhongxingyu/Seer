 import java.awt.*;
 
 public class Turret {
     private Vec position = new Vec(0, 0);
     public final int cooldown; //in millisec
 
     private float fire_angle = 0;
     public Vec fpos = new Vec(0, 0);
 
     private int TimeToCD = 1000;
 
     Turret(Ship s) {
         position = s.getPosition();
         cooldown = 5;
 
     }
 
     Turret(Vec xy, int cd) {   //constructor for elswhere locataed Turrel
         cooldown = cd;
         position.setV(xy);
     }
 
     public void turnLeft() {
         fire_angle += 5; //spin on +5degrees
 
     }
 
     public void turnRight() {
         fire_angle -= 5; //spin on +5degrees
     }
 
     public void render(Graphics2D g) {
 
         g.setColor(Color.DARK_GRAY);
         GeomHelp.fillPolygon((int) fpos.getX(), (int) fpos.getY(), 15, 3, (float) Math.toRadians(fire_angle), g);
 
 
         g.setColor(Color.BLUE);
         GeomHelp.FillCircle((int) position.getX(), (int) position.getY(), 10, g);
     }
 
     public void setPosition(Vec v) {
         position.setV(v);
     }
 
     public Vec getPosition() {
         return position;
     }
 
     public Vec getFire_direction() {
         return new Vec((float) Math.cos(Math.toRadians(fire_angle)), (float) -Math.sin(Math.toRadians(fire_angle)));
     }
 
 
     public void fire() {
         if (TimeToCD > cooldown) {
             TimeToCD = 0;
            Game.Bullets.add(new Bullet(getPosition().addReturn(getFire_direction().multiplied(30)), getFire_direction()));          ////was getPosition()/ getFireDirection()
            System.out.println((getPosition().addReturn(getFire_direction().multiplied(30))).toString());
         }
     }
 
     public void incTimeToCD(){
         TimeToCD++;
     }
 
 
 
 }
