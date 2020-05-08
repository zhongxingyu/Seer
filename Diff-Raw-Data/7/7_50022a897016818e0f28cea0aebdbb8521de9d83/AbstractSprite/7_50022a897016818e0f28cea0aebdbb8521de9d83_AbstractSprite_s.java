 /*
  *  This file is part of Pac Defence.
  *
  *  Pac Defence is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  Pac Defence is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Pac Defence.  If not, see <http://www.gnu.org/licenses/>.
  *  
  *  (C) Liam Byrne, 2008.
  */
 
 package sprites;
 
 import gui.Formulae;
 import gui.Helper;
 import images.ImageHelper;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Random;
 
 import towers.Bullet;
 
 public abstract class AbstractSprite implements Sprite {
    
    private static final double baseSpeed = 4;
    private static final double maxMult = 2;
    private static final Random rand = new Random();
 
    private final int width;
    private final int halfWidth;
 
    //private final BufferedImage originalImage;
    //private BufferedImage currentImage;
    private final List<BufferedImage> originalImages;
    private List<BufferedImage> currentImages;
    private int currentImageIndex = 0;
    private final Rectangle imageSize;
 
    private final double speed;
    private final int levelHP;
    private double hp;
    private final double hpFactor;
    private final List<Point> path;
    private final Point lastPoint = new Point();
    private Point nextPoint;
    private int pointAfterIndex;
    private final Point2D centre = new Point2D.Double();
    // private double x, y;
    private double xStep, yStep;
    private double totalDistanceTravelled = 0;
    // The distance in pixels to the next point from the previous one
    private double distance;
    // The steps taken so far
    private int steps;
    // Whether the sprite is still alive
    private boolean alive = true;
    // Whether the sprite is still on the screen
    private boolean onScreen = true;
    // How many times the image has been shrunk after it died
    private int shrinkCounter = 0;
 
    public AbstractSprite(List<BufferedImage> images, int hp, List<Point> path) {
       this.width = images.get(1).getWidth();
       halfWidth = width / 2;
       // Use two clones here so that currentImages can be edited without
       // affecting originalImages
       originalImages = Collections.unmodifiableList(Helper.cloneList(images));
       currentImages = Helper.cloneList(images);
       imageSize = new Rectangle(0, 0, width, width);      
       speed = calculateSpeed(hp);
       levelHP = hp;
       hpFactor = this.hp / hp;
       this.path = path;
       nextPoint = calculateFirstPoint();
       pointAfterIndex = 0;
       calculateNextMove();
    }
 
    @Override
    public void draw(Graphics g) {
       // System.out.println("Sprite draw called");
       if (onScreen) {
          // System.out.println("Drawing");
          currentImageIndex++;
          currentImageIndex %= originalImages.size();
          g.drawImage(currentImages.get(currentImageIndex), (int) centre.getX() - halfWidth,
                (int) centre.getY() - halfWidth, null);
       }
    }
 
    /**
     * 
     * @return true if this Sprite has finished, false otherwise
     */
    @Override
    public boolean tick() {
       if (!onScreen) {
          return true;
       } else {
          if (alive) {
             move();
          } else {
             die();
          }
       }
       return false;
    }
 
    @Override
    public double getTotalDistanceTravelled() {
       return totalDistanceTravelled;
    }
 
    @Override
    public Point2D getPosition() {
       return (Point2D) centre.clone();
    }
 
    @Override
    public boolean isAlive() {
       return alive;
    }
 
    @Override
    public boolean intersects(Point2D p) {
       if (!alive) {
          // If the sprite is dead or dying it can't be hit
          return false;
       }
       int x = (int) (p.getX() - centre.getX() + halfWidth);
       int y = (int) (p.getY() - centre.getY() + halfWidth);
       if (imageSize.contains(x, y)) {
          // RGB of zero means a completely alpha i.e. transparent pixel
          return currentImages.get(currentImageIndex).getRGB(x, y) != 0;
       }
       return false;
    }
 
    @Override
    public double hitBy(Bullet b) {
       // System.out.println("Got hit");
       if (hp - b.getDamage() <= 0) {
          alive = false;
          return Formulae.damageDollars(hp, hpFactor) + Formulae.killBonus(levelHP);
       }
       hp -= b.getDamage();
       return Formulae.damageDollars(b.getDamage(), hpFactor);
    }
 
    @Override
    public int getHalfWidth() {
       return halfWidth;
    }
    
    @Override
    public double getHPFactor() {
       return hpFactor;
    }
    
 
    public static Comparator<Sprite> getTotalDistanceTravelledComparator() {
       return new Comparator<Sprite>() {
          @Override
          public int compare(Sprite s1, Sprite s2) {
             return (int) (s2.getTotalDistanceTravelled() - s1.getTotalDistanceTravelled());
          }
       };
    }
 
    private void move() {
       // System.out.println("Step: " + steps + " Pos: " + x + " " + y);
       centre.setLocation(centre.getX() + xStep, centre.getY() + yStep);
       totalDistanceTravelled += Math.abs(xStep) + Math.abs(yStep);
       steps++;
       if (steps + 1 > distance) {
          calculateNextMove();
       }
    }
 
    private Point calculateFirstPoint() {
       Point p1 = path.get(0);
       Point p2 = path.get(1);
       int dx = (int) (p2.getX() - p1.getX());
       int dy = (int) (p2.getY() - p1.getY());
       double distance = Math.sqrt(dx * dx + dy * dy);
       double mult = (halfWidth + 1) / distance;
       int x = (int) (p1.getX() - mult * dx);
       int y = (int) (p1.getY() - mult * dy);
       return new Point(x, y);
    }
 
    private void calculateNextMove() {
       if (pointAfterIndex < path.size()) {
          // There is still another point to head to
          lastPoint.setLocation(nextPoint);
          centre.setLocation(nextPoint);
          nextPoint.setLocation(path.get(pointAfterIndex));
          pointAfterIndex++;
          double dx = nextPoint.x - lastPoint.x;
          double dy = nextPoint.y - lastPoint.y;
          distance = Math.sqrt(dx * dx + dy * dy) / speed;
          // System.out.println("Distance: " + distance);
          xStep = dx / distance;
          yStep = dy / distance;
          // System.out.println("Steps: " + xStep + " " + yStep);
          steps = 0;
          // Invert yStep here as y coord goes down as it increases, rather than
          // up as in a conventional coordinate system.
          rotateImages(ImageHelper.vectorAngle(xStep, -yStep));
       } else {
          if (nextPoint == null) {
             // System.out.println("Finished");
             onScreen = false;
          } else {
             // This flags that the final path has been extended
             nextPoint = null;
            // Adds 20 steps which should be enough to get the sprite off the
            // screen
            distance += 20;
          }
       }
    }
    
    private void rotateImages(double angle) {
       List<BufferedImage> images = new ArrayList<BufferedImage>(originalImages.size());
       for(BufferedImage i : originalImages) {
          images.add(ImageHelper.rotateImage(i, angle));
       }
       currentImages = images;
    }
 
    private void die() {
       if (shrinkCounter > 8) {
          onScreen = false;
          return;
       } else {
          shrinkCounter++;
          int nextImageIndex = (currentImageIndex + 1) % originalImages.size();
          int width = currentImages.get(nextImageIndex).getWidth();
          BufferedImage bi = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
          // Shrinks it by 25%
          int newWidth = (int) (width * (1 - 0.1 * shrinkCounter));
          int pos = width / 2 - newWidth / 2;
          Graphics2D g = (Graphics2D) bi.getGraphics();
          g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
          g.drawImage(currentImages.get(nextImageIndex), pos, pos, newWidth, newWidth, null);
          currentImages.set(nextImageIndex, bi);
       }
    }
    
    /**
     * Calculates the speed and also sets the hp based on this
     * @param hp
     * @return
     */
    private double calculateSpeed(int hp) {
       double a = rand.nextDouble();
       if(a >= 0.5) {
          // Makes this a into a random number from zero to one
          a = (a - 0.5) * 2;
       } else {
          a = a * 2;
       }
       // A multiplier between one and maxMult
       double mult = ((maxMult - 1) * a + 1);
       if(a >= 0.5) {
          // Rounds it up
          this.hp = (int)(hp / mult + 0.5); 
          return baseSpeed * mult;
       } else {
          this.hp = (int)(hp * mult); 
          return baseSpeed / mult;
       }
    }
 
 }
