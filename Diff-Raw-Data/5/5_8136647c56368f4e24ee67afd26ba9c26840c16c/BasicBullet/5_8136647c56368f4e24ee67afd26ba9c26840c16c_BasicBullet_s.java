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
  *  (C) Liam Byrne, 2008 - 2012.
  */
 
 package towers;
 
 import images.ImageHelper;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Shape;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 
 import logic.Constants;
 import sprites.Sprite;
 import sprites.Sprite.DamageReport;
 
 public class BasicBullet implements Bullet {
 
    
    // Determined by the image
    public static final int radius = 3;
    // Extra distance a bullet can be off screen before it is removed. Should be greater than the
    // radius of the largest sprite and the radius of the bullet
    private static final int offScreenFudgeDistance = 50 + radius;
    
    // The direction of the bullet, first is dx, second is dy. Should be normalised
    // then multiplied by the speed.
    protected final double[] dir = new double[2];
    protected final double speed;
    protected double distanceTravelled = 0;
    protected final Point2D lastPosition;
    protected final Point2D position;
    protected final int range;
    protected final double damage;
    private boolean draw = true;
    protected final Tower shotBy;
    private static final BufferedImage image = ImageHelper.loadImage(radius * 2, radius * 2,
          "other", "bullet.png");
    private final List<Shape> pathBounds;
    
    /**
     * Creates a new BasicBuller with fields set, but tick and draw
     * methods must be overrided.
     * 
     * @param path
     * @param shotBy
     * @param damage
     * @param range
     * @param speed
     */
    protected BasicBullet(List<Shape> pathBounds, Tower shotBy, double damage, int range,
          double speed) {
       this.pathBounds = pathBounds;
       this.shotBy = shotBy;
       this.damage = damage;
       this.range = range;
       this.speed = speed;
       lastPosition = null;
       position = null;
    }
    
    public BasicBullet(Tower shotBy, double dx, double dy, int turretWidth, int range,
          double speed, double damage, Point p, List<Shape> pathBounds) {
       this.shotBy = shotBy;
       int turretWidthPlusRadius = turretWidth + radius;
       this.range = range - turretWidthPlusRadius;
       this.speed = speed;
       this.damage = damage;
       setDirections(dx, dy);
       double divisor = Math.sqrt(dx * dx + dy * dy);
       position = new Point2D.Double(p.getX() + turretWidthPlusRadius * dx / divisor,
             p.getY() + turretWidthPlusRadius * dy / divisor);
       lastPosition = new Point2D.Double(position.getX(), position.getY());
       this.pathBounds = pathBounds;
    }
 
    @Override
public final double tick(List<Sprite> sprites) {
       double tick = doTick(sprites);
       draw = tick < 0;
       return tick;
    }
 
    @Override
public void draw(Graphics g) {
       if(draw && !checkIfBulletIsOffScreen()) {
          g.drawImage(image, (int) position.getX() - radius, (int) position.getY() - radius, null);
       }
    }
    
    public static double processDamageReport(DamageReport d, Tower t) {
       if(d == null) {
          return 0;
       }
       if(d.wasKill()) {
          t.increaseKills(1);
       }
       t.increaseDamageDealt(d.getDamage());
       return d.getMoneyEarnt();
    }
    
    protected void setDirections(double dx, double dy) {
       double divisor = Math.sqrt(dx * dx + dy * dy);
       dir[0] = speed * dx / divisor;
       dir[1] = speed * dy / divisor;
    }
    
    protected double checkIfSpriteIsHit(List<Sprite> sprites) {
       return checkIfSpriteIsHit(lastPosition, position, sprites);
    }
    
    protected double checkIfSpriteIsHit(Point2D p1, Point2D p2, List<Sprite> sprites) {
       // It turns out using the line is much faster than using a list of points, even though the
       // list of points must be calculated if the lines intersects the shape.
       // A number of reasons:
       // The Helper.getPointsOnLine is relatively slow
       // The majority of the time the line does not intersect any shapes so that method is never
       //    called
       // Calculating whether a line intersects a circle can be done in constant time
       // The list only ever needs to be calculated once, as this returns as soon as there is a hit
       //    (OK, d == null due to threading means sometimes more than once)
       // Overall this is around 3x faster than the above
       Line2D line = new Line2D.Double(p1, p2);
       // I used to do intersectsPath here first, but that doesn't work for a bullet that's just off
       // screen hitting a just started/nearly finished sprite
       for(Sprite s : sprites) {
          Point2D p = s.intersects(line);
          if(p != null) {
             DamageReport d = s.hit(damage, shotBy.getClass());
             if(d != null) { // Sprite is not already dead, may happen due to threading
                specialOnHit(p, s, sprites);
                return processDamageReport(d);
             }
          }
       }
       return -1;
    }
    
    private boolean intersectsPath(Line2D line) {
       // Do this as line.getBounds2D() is not guaranteed to be the smallest bounding rectangle
       double x1 = line.getX1(), x2 = line.getX2(), y1 = line.getY1(), y2 = line.getY2();
       double width, height;
       if(x1 < x2) {
          width = x2 - x1;
       } else {
          width = x1 - x2;
          x1 = x2;
       }
       if(y1 < y2) {
          height = y2 - y1;
       } else {
          height = y1 - y2;
          y1 = y2;
       }
       // Make sure these are non-zero as otherwise a vertical/horizontal line will have a box with
       // no area, so won't count as intersecting the shape
       if(width == 0) {
          width = 0.1;
       }
       if(height == 0) {
          height = 0.1;
       }
       assert width > 0 && height > 0 && x1 <= x2 && y1 <= y2 : "The above logic isn't working.";
       for(Shape s : pathBounds) {
          // The rectangle is actually larger than the line, so even though the rectangle intersects
          // pathBounds, the line may not, but there is no Shape.intersect(Line2D) method, and it is
          // good enough
          if(s.intersects(x1, y1, width, height)) {
             return true;
          }
       }
       return false;
    }
    
    /**
     * To be overriden by subclasses whose bullets do something special on a hit
     * 
     * @param p
     */
    protected void specialOnHit(Point2D p, Sprite s, List<Sprite> sprites) {}
    
    protected double processDamageReport(DamageReport d) {
       return processDamageReport(d, shotBy);
    }
    
    /**
     * To be overriden by subclasses whose bullets aren't to be removed when merely off screen
     * 
     * @return
     */
    protected boolean canBulletBeRemovedAsOffScreen() {
       return checkIfBulletIsOffScreen();
    }
    
    protected boolean checkIfBulletIsOffScreen() {
       return checkIfBulletIsOffScreen(position);
    }
    
    protected boolean checkIfBulletIsOffScreen(Point2D p) {
       return p.getX() < -getOffScreenFudgeDistance() ||
             p.getY() < -getOffScreenFudgeDistance() ||
             p.getX() > Constants.MAP_WIDTH + getOffScreenFudgeDistance() ||
             p.getY() > Constants.MAP_HEIGHT + getOffScreenFudgeDistance();
    }
    
    protected int getOffScreenFudgeDistance() {
       return offScreenFudgeDistance;
    }
    
    protected double doTick(List<Sprite> sprites) {
       if(isOutOfRange() || canBulletBeRemovedAsOffScreen()) {
          return 0;
       }
       distanceTravelled += speed;
       lastPosition.setLocation(position);
       if(isOutOfRange()) { // Check if it's now out of range
          double extraFraction = (distanceTravelled - range) / speed;
          position.setLocation(position.getX() + extraFraction * dir[0],
                position.getY() + extraFraction * dir[1]);
          double result = checkIfSpriteIsHit(sprites);
          // Bullet has exceeded range so should be removed no matter what
          return result > 0 ? result : 0;
       } else {
          position.setLocation(position.getX() + dir[0], position.getY() + dir[1]);
          return checkIfSpriteIsHit(sprites);
       }
    }
    
    /**
     * True if the bullet has travelled so far it should be removed, false otherwise
     */
    protected boolean isOutOfRange() {
       return distanceTravelled > range;
    }
    
    protected List<Shape> getPathBounds() {
       return pathBounds;
    }
 
 }
