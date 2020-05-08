 
 
 package ui;
 
 import objects.Sun;
 import objects.Planet;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.Timer;
 import java.util.TimerTask;
 
 public class GameCanvas extends Canvas implements Runnable {
 
     private Sun sun;
     private Planet[] planets;
 
     public GameCanvas() {
         sun = new Sun(0, 0, 0.8, 40, Color.orange);
 
         planets = new Planet[6];
         for (int i = 0; i < 6; i++) {
             planets[i] = new Planet(0, i * 50 + 80, 0.4, (int)(Math.random() * 15) + 5,
                     new Color((float)Math.random(), (float)Math.random(), (float)Math.random()), (int)(Math.random() * 10));
            planets[i].run();
         }
     }
 
     @Override
     public void paint(Graphics g) {
         g.setColor(Color.black);
         g.fillRect(0, 0, getWidth(), getHeight());
 
        //g.setColor(Color.white);
        //for (int i = 0; i < 100; i++) {
        //    int size = (int)(Math.random() * 4);
        //    g.fillOval((int)(Math.random() * getWidth()), (int)(Math.random() * getHeight()), size, size);
        //}
 
         g.setColor(sun.getColor());
         Point p = transformCoordinates(new Point(sun.getPositionX() - sun.getRadius(), sun.getPositionY() + sun.getRadius()));
         g.fillOval(p.x, p.y, sun.getRadius() * 2, sun.getRadius() * 2);
 
         for (Planet planet : planets) {
             g.setColor(planet.getColor());
             p = transformCoordinates(new Point(planet.getPositionX() - planet.getRadius(), planet.getPositionY() + planet.getRadius()));
             g.fillOval(p.x, p.y, planet.getRadius() * 2, planet.getRadius() * 2);
         }
         
     }
 
     public Point transformCoordinates(Point p) {
         return new Point(p.x + getWidth() / 2, getHeight() / 2 - p.y);
     }
 
     public void run() {
         new Timer(true).scheduleAtFixedRate(
                 new TimerTask() {
                     public void run() {
                         repaint();
                     }
                 }, 0, 40);
     }
 
 }
