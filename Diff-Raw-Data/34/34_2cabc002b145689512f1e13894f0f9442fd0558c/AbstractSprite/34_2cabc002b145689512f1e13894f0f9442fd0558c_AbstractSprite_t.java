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
 
 import images.ImageHelper;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.Line2D;
 import java.awt.geom.Point2D;
 import java.awt.geom.Rectangle2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.TreeMap;
 
 import logic.Circle;
 import logic.Formulae;
 import logic.Helper;
 
 public abstract class AbstractSprite implements Sprite {
    
    private static final double baseSpeed = 2;
    private static final double maxMult = 2;
    private static final Random rand = new Random();
 
    private final int width;
    private final int halfWidth;
    private final Circle bounds = new Circle();
    private final Rectangle2D rectangularBounds = new Rectangle2D.Double();
 
    // Keep track of rotated images so every time a sprite rounds a corner
    // the original images do not need to be re-rotated
    private static final Map<Class<? extends AbstractSprite>, Map<LooseFloat,
          List<BufferedImage>>> rotatedImages = new HashMap<Class<? extends AbstractSprite>,
          Map<LooseFloat, List<BufferedImage>>>();
   
   // Keep track of the images for when a sprite dies, filed under class, the imageIndex
   // amd the angle the sprite died on
   private static final Map<Class<? extends AbstractSprite>, Map<Integer, Map<LooseFloat,
         List<BufferedImage>>>> dyingImages = new HashMap<Class<? extends AbstractSprite>,
         Map<Integer, Map<LooseFloat, List<BufferedImage>>>>();
   
    private final List<BufferedImage> originalImages;
    private List<BufferedImage> currentImages;
    private BufferedImage currentImage;
    private int currentImageIndex = 0;
   private double currentAngle;
 
    private final int currentLevel;
    private final double speed;
    private final long levelHP;
    private double hp;
    private final double hpFactor;
    private final List<Point> path;
    private final Point lastPoint = new Point();
    private Point nextPoint;
    private int pointAfterIndex;
    
    @SuppressWarnings("serial")
    private final Point2D centre = new Point2D.Double();
    // private double x, y;
    private double xStep, yStep;
    private double totalDistanceTravelled = 0;
    // The distance in pixels to the next point from the previous one
    private double distance;
    // The steps taken so far
    private double steps;
    // Whether the sprite is still alive
    private boolean alive = true;
    // Whether the sprite is still on the screen
    private boolean onScreen = true;
    // How many times the image has been shrunk after it died
    private int shrinkCounter = 0;
    private static final int dieFrames = 15;
    private double speedFactor = 1;
    private int adjustedSpeedTicksLeft = 0;
    private double damageMultiplier = 1;
    private int adjustedDamageTicksLeft = 0; 
 
    public AbstractSprite(List<BufferedImage> images, int currentLevel, long hp, List<Point> path){
       this.currentLevel = currentLevel;
       this.width = images.get(1).getWidth();
       halfWidth = width / 2;
       centre.setLocation(path.get(0));
       bounds.setRadius(halfWidth);
       setBounds();
       // Use two clones here so that currentImages can be edited without
       // affecting originalImages
       originalImages = Collections.unmodifiableList(new ArrayList<BufferedImage>(images));
       currentImages = new ArrayList<BufferedImage>(images);
       speed = calculateSpeed(hp);
       levelHP = hp;
       hpFactor = levelHP / this.hp;
       this.path = path;
       nextPoint = calculateFirstPoint();
       pointAfterIndex = 0;
       calculateNextMove();
    }
 
    @Override
    public void draw(Graphics g) {
       if (onScreen) {
          g.drawImage(currentImage, (int) centre.getX() - halfWidth,
                (int) centre.getY() - halfWidth, null);
       }
    }
 
    @Override
    public boolean tick() {
       if (!onScreen) {
          return true;
       } else {
          currentImageIndex++;
          currentImageIndex %= currentImages.size();
          currentImage = currentImages.get(currentImageIndex);
          if(alive) {
             move();
             decreaseAdjustedTicksLeft();
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
       return new Point2D.Double(centre.getX(), centre.getY());
    }
    
    @Override
    public double getSpeed() {
       return speedFactor * speed;
    }
    
    @Override
    public double getHPLeft() {
       return hp;
    }
    
    @Override
    public Shape getBounds() {
       return bounds.clone();
    }
 
    @Override
    public boolean isAlive() {
       return alive;
    }
 
    @Override
    public boolean intersects(Point2D p) {
       if (alive) {
          return fastIntersects(p);
       } else {
          // If the sprite is dead or dying it can't be hit
          return false;
       }
    }
    
    @Override
    public Point2D intersects(List<Point2D> points) {
       if(alive) {
          for(Point2D p : points) {
             if(fastIntersects(p)) {
                return p;
             }
          }
       }
       return null;
    }
    
    @Override
    public Point2D intersects(Line2D line) {
       return intersects(Helper.getPointsOnLine(line));
    }
 
    @Override
    public DamageReport hit(double damage) {
       if(!alive) {
          return null;
       }
       if(adjustedDamageTicksLeft > 0) {
          damage *= damageMultiplier;
       }
       if (hp - damage <= 0) {
          alive = false;
          double moneyEarnt = Formulae.damageDollars(hp, hpFactor, currentLevel) +
                Formulae.killBonus(levelHP, currentLevel);
          return new DamageReport(hp, moneyEarnt, true);
       } else {
          hp -= damage;
          double moneyEarnt = Formulae.damageDollars(damage, hpFactor, currentLevel);
          return new DamageReport(damage, moneyEarnt, false);
       }
    }
 
    @Override
    public int getHalfWidth() {
       return halfWidth;
    }
    
    @Override
    public double getHPFactor() {
       return hpFactor;
    }
    
    @Override
    public void slow(double factor, int numTicks) {
       if(factor >= 1) {
          throw new IllegalArgumentException("Factor must be less than 1 in order to slow.");
       }
       if(factor < speedFactor) {
          // New speed is slower, so it is better, even if only for a short
          // time. Or so I think.
          speedFactor = factor;
          adjustedSpeedTicksLeft = numTicks;
       } else if(Math.abs(factor - speedFactor) < 0.01) {
          // The new slow speed factor is basically the same as the current
          // speed, so if it is for longer, lengthen it
          if(numTicks > adjustedSpeedTicksLeft) {
             speedFactor = factor;
             adjustedSpeedTicksLeft = numTicks;
          }
       }
       // Otherwise ignore it as it would increase the sprite's speed
    }
    
    @Override
    public void setDamageMultiplier(double multiplier, int numTicks) {
       assert multiplier > 1 : "Multiplier must be greater than 1";
       if(multiplier > damageMultiplier) {
          damageMultiplier = multiplier;
          adjustedDamageTicksLeft = numTicks;
       } else if(Math.abs(multiplier - damageMultiplier) < 0.01) {
          if(numTicks > adjustedDamageTicksLeft) {
             damageMultiplier = multiplier;
             adjustedDamageTicksLeft = numTicks;
          }
       }
    }
 
    private void move() {
       centre.setLocation(centre.getX() + xStep * speedFactor,
             centre.getY() + yStep * speedFactor);
       setBounds();
       totalDistanceTravelled += (Math.abs(xStep) + Math.abs(yStep)) * speedFactor;
       steps += speedFactor;
       if (steps + 1 > distance) {
          calculateNextMove();
       }
    }
    
    private void setBounds() {
       bounds.setCentre(centre);
       rectangularBounds.setRect(centre.getX() - halfWidth, centre.getY() - halfWidth, width, width);
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
          setBounds();
          nextPoint.setLocation(path.get(pointAfterIndex));
          pointAfterIndex++;
          double dx = nextPoint.getX() - lastPoint.getX();
          double dy = nextPoint.getY() - lastPoint.getY();
          distance = Math.sqrt(dx * dx + dy * dy) / speed;
          xStep = dx / distance;
          yStep = dy / distance;
          steps = 0;
          // Invert yStep here as y coord goes down as it increases, rather than
          // up as in a conventional coordinate system.
          rotateImages(ImageHelper.vectorAngle(xStep, -yStep));
       } else {
          // There are no more points to head towards
          if (nextPoint == null) {
             // Sprite is finished
             onScreen = false;
          } else {
             // This flags that the final path has been extended
             nextPoint = null;
             double distancePerStep = Math.sqrt(xStep * xStep + yStep * yStep);
             // This is enough steps to get the sprite off the screen, add one just
             // to make sure
             distance += (halfWidth + 1) / distancePerStep;
          }
       }
    }
    
    private void rotateImages(double angle) {
      currentAngle = angle;
       if(!rotatedImages.containsKey(getClass())) {
          rotatedImages.put(getClass(), new TreeMap<LooseFloat, List<BufferedImage>>());
       }
       Map<LooseFloat, List<BufferedImage>> m = rotatedImages.get(getClass());
       // Use LooseFloat to reduce precision so rotated images are less likely to be duplicated
       LooseFloat f = new AbstractSpriteLooseFloat(angle);
       if(!m.containsKey(f)) {
          List<BufferedImage> images = new ArrayList<BufferedImage>(originalImages.size());
          for(BufferedImage i : originalImages) {
             images.add(ImageHelper.rotateImage(i, angle));
          }
          m.put(f, Collections.unmodifiableList(images));
       }
       // Need to copy this as when the sprite dies the images are changed
       currentImages = new ArrayList<BufferedImage>(m.get(f));
    }
 
    private void die() {
       if (shrinkCounter >= dieFrames) {
          onScreen = false;
          return;
       } else {
          if(shrinkCounter == 0) {
             currentImages = getDyingImages();
             currentImageIndex = 0;
             currentImage = currentImages.get(currentImageIndex);
          }
          shrinkCounter++;
       }
    }
    
    private List<BufferedImage> getDyingImages() {
       if(!dyingImages.containsKey(getClass())) {
         dyingImages.put(getClass(), new HashMap<Integer, Map<LooseFloat, List<BufferedImage>>>());
      }
      Map<Integer, Map<LooseFloat, List<BufferedImage>>> indexMap = dyingImages.get(getClass());
      if(!indexMap.containsKey(currentImageIndex)) {
         indexMap.put(currentImageIndex, new TreeMap<LooseFloat, List<BufferedImage>>());
       }
      Map<LooseFloat, List<BufferedImage>> imageLists = indexMap.get(currentImageIndex);
      LooseFloat f = new AbstractSpriteLooseFloat(currentAngle);
      if(!imageLists.containsKey(f)) {
         imageLists.put(f, makeDyingImages());
       }
      return imageLists.get(f);
    }
    
    private List<BufferedImage> makeDyingImages() {
       List<BufferedImage> images = new ArrayList<BufferedImage>(dieFrames);
       double newWidth = width;
       for(int i = 0; i < dieFrames; i++) {
          // Shrinks the current image to this size
          newWidth *= 0.85;
          int pos = (int)((width - newWidth)/2);
          BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB);
          Graphics2D g = image.createGraphics();
          g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
          g.drawImage(currentImage, pos, pos, (int)newWidth, (int)newWidth, null);
          images.add(image);
       }
       return images;
    }
    
    /**
     * Calculates the speed and also sets the hp based on this
     * @param hp
     * @return
     */
    private double calculateSpeed(long hp) {
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
    
    private boolean fastIntersects(Point2D p) {
       // Checks the rectangular bounds instead of the bounds of the circle
       // as it's faster
       if (rectangularBounds.contains(p)) {
          int x = (int) (p.getX() - centre.getX() + halfWidth);
          int y = (int) (p.getY() - centre.getY() + halfWidth);
          // RGB of zero means a completely alpha i.e. transparent pixel
          return currentImage.getRGB(x, y) != 0;
       }
       return false;
    }
    
    private void decreaseAdjustedTicksLeft() {
       if(adjustedSpeedTicksLeft > 0) {
          adjustedSpeedTicksLeft--;
          if(adjustedSpeedTicksLeft <= 0) {
             speedFactor = 1;
          }
       }
       if(adjustedDamageTicksLeft > 0) {
          adjustedDamageTicksLeft--;
          if(adjustedDamageTicksLeft <= 0) {
             damageMultiplier = 1;
          }
       }
    }
    
    private class AbstractSpriteLooseFloat extends LooseFloat {
       
       public AbstractSpriteLooseFloat(float f) {
          super(f);
       }
       
       public AbstractSpriteLooseFloat(double d) {
          super(d);
       }
 
       @Override
       protected float getPrecision() {
          return 0.012F;
       }
    }
 
 }
