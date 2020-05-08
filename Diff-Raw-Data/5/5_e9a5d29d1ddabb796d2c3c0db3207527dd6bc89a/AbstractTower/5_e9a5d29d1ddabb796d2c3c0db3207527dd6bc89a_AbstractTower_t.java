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
 
 import java.awt.AlphaComposite;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.geom.AffineTransform;
 import java.awt.image.AffineTransformOp;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.EnumMap;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import logic.Constants;
 import logic.Formulae;
 import util.Circle;
 import util.Helper;
 import util.Vector2D;
 import creeps.Creep;
 import creeps.Creep.DistanceComparator;
 import creeps.Creep.FirstComparator;
 import creeps.LooseFloat;
 
 public abstract class AbstractTower implements Tower {
 
    public static final float shadowAmount = 0.75F;
    // Made this slightly greater than the intended value, 1.05, as sometimes things would round
    // down or something when it's multiplied with them
    protected static final double upgradeIncreaseFactor = 1.050001;
    
    public static final Comparator<Creep> DEFAULT_CREEP_COMPARATOR = new FirstComparator();
    
    // Keep track of the loaded images so they are only loaded once
    private static final Map<String, BufferedImage> towerImages =
          new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> overlayImages =
          new HashMap<String, BufferedImage>();
    private static final Map<String, BufferedImage> buttonImages =
          new HashMap<String, BufferedImage>();
    
    private static final Map<Class<? extends AbstractTower>, Map<LooseFloat, BufferedImage>>
          rotatedImages = new HashMap<Class<? extends AbstractTower>, Map<LooseFloat,
          BufferedImage>>();
    
    public static final int turretThickness = 4;
    
    // Maps the ID of an aid tower to the aid factor it gives for each attribute
    private final Map<Integer, Map<Attribute, Double>> aidFactors =
          new HashMap<Integer, Map<Attribute, Double>>();
    private final Map<Attribute, Double> currentFactors = createCurrentFactors();
 
    private Map<Attribute, Integer> attributeLevels = createAttributeLevels();
 
    // The top left point of this tower
    private final Point topLeft;
    // The centre of this tower
    private final Point centre;
    private final Circle bounds;
    private final Rectangle boundingRectangle = new Rectangle();
    private final String name;
    // The number of clock ticks between each shot
    private double fireRate;
    // The number of clock ticks until this tower's next shot
    private double timeToNextShot = 0;
    private double range;
    private final double rangeUpgrade;
    private int twiceRange;
    private double bulletSpeed;
    private final double bulletSpeedUpgrade;
    private double damage;
 
    // The width/height of the image (as it's square)
    protected final int width;
    protected final int halfWidth;
    // The width of the turret from the centre of the tower
    protected final int turretWidth;
 
    private final BufferedImage baseImage;
    private final BufferedImage overlayImage;
    private final boolean imageRotates;
    private BufferedImage currentImage;
    private final BufferedImage buttonImage;
    
    private final List<Shape> pathBounds;
 
    private boolean isSelected = false;
 
    private List<DamageNotifier> damageNotifiers = new ArrayList<DamageNotifier>();
    
    private int kills = 0;
    private int killsLevel = 1;
    private long damageDealt = 0;
    private double fractionalDamageDealt = 0;
    private int damageDealtLevel = 1;
    private long nextUpgradeDamage = Formulae.nextUpgradeDamage(killsLevel);
    private int nextUpgradeKills = Formulae.nextUpgradeKills(damageDealtLevel);
    
    // Defaults to FirstComparator
    private Comparator<Creep> creepComparator = DEFAULT_CREEP_COMPARATOR;
    
   private List<Bullet> bulletsToAdd = Collections.synchronizedList(new ArrayList<Bullet>());
 
    protected AbstractTower(Point p, List<Shape> pathBounds, String name, int fireRate,
          double range, double bulletSpeed, double damage, int width, int turretWidth,
          boolean hasOverlay) {
       this.width = width;
       halfWidth = width / 2;
       centre = new Point(p);
       topLeft = new Point((int) centre.getX() - halfWidth, (int) centre.getY() - halfWidth);
       bounds = new Circle(centre, halfWidth);
       setBounds(); // Sets the bounding rectangle
       this.pathBounds = pathBounds;
       this.name = name;
       this.fireRate = fireRate;
       this.range = range;
       twiceRange = (int)(range * 2);
       rangeUpgrade = range * (upgradeIncreaseFactor - 1);
       this.bulletSpeed = bulletSpeed;
       bulletSpeedUpgrade = bulletSpeed * (upgradeIncreaseFactor - 1);
       this.damage = damage;
       this.turretWidth = turretWidth;
       // Use the class name as the actual name could be anything
       String className = getClass().getSimpleName();
       // Need to remove the 'Tower' off the end
       className = makeFirstCharacterLowerCase(className.substring(0, className.length() - 5));
       baseImage = loadImage(towerImages, width, "towers", className + ".png");
       imageRotates = hasOverlay && (turretWidth != 0);
       overlayImage = hasOverlay ?
             loadImage(overlayImages, width, "towers", "overlays", className + "Overlay.png") : null;
       currentImage = overlayImage == null ? baseImage : getRotatedImage(0.0);
       buttonImage = loadImage(buttonImages, 0, "buttons", "towers", className + "Button.png");
    }
    
    private String makeFirstCharacterLowerCase(String s) {
       StringBuilder sb = new StringBuilder(s);
       sb.setCharAt(0, Character.toLowerCase(s.charAt(0)));
       return sb.toString();
    }
    
    /**
     * Flush the cache of rotated images.
     */
    public static void flushImageCache() {
       // Only clear this as normally it will be at least an order of magnitude greater than the
       // other maps used for caching images.
       rotatedImages.clear();
    }
 
    @Override
    public List<Bullet> tick(List<Creep> creeps, boolean levelInProgress) {
       // Decrements here so it's on every tick, not just when it is able to shoot
       timeToNextShot--;
       List<Bullet> fired = null;
       if(imageRotates || timeToNextShot <= 0) {
          // The creeps are sorted by the default creep comparator in Clock, so that they don't
          // have to be resorted for any tower left on the default (which is often most of them)
          if(!creepComparator.getClass().equals(DEFAULT_CREEP_COMPARATOR.getClass())) {
             // Make a copy so it can be sorted
             creeps = new ArrayList<Creep>(creeps);
             Collections.sort(creeps, creepComparator);
          }
          // If the image rotates, this needs to be done to find out the direction to rotate to
          fired = fireBullets(creeps);
       }
       if (timeToNextShot <= 0 && fired != null && fired.size() > 0) {
          timeToNextShot = fireRate;
          // Use bulletsToAdd as some towers launch bullets between ticks
          bulletsToAdd.addAll(fired);
       }
       List<Bullet> bulletsToReturn = bulletsToAdd;
      bulletsToAdd = Collections.synchronizedList(new ArrayList<Bullet>());
       return bulletsToReturn;
    }
 
    @Override
    public void draw(Graphics2D g) {
       if(isSelected) {
          drawRange(g);
       }
       g.drawImage(currentImage, (int) topLeft.getX(), (int) topLeft.getY(), null);
    }
 
    @Override
    public void drawShadowAt(Graphics2D g, Point p, boolean validPlacement) {
       drawRange(g, p);
       
       // Makes it so what is drawn is partly transparent
       Graphics2D gCopy = (Graphics2D) g.create();
       gCopy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, shadowAmount));
       gCopy.drawImage(currentImage, p.x - halfWidth, p.y - halfWidth, width, width, null);
       gCopy.dispose();
       
       if(!validPlacement) {
          drawX(g, p, halfWidth);
       }
    }
    
    public static void drawX(Graphics2D g, Point p, int halfWidth) {
       Graphics2D gCopy = (Graphics2D) g.create();
       gCopy.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
       gCopy.setColor(Color.RED);
       gCopy.drawLine(p.x - halfWidth, p.y - halfWidth, p.x + halfWidth, p.y + halfWidth);
       gCopy.drawLine(p.x - halfWidth, p.y + halfWidth, p.x + halfWidth, p.y - halfWidth);
       gCopy.dispose();
    }
    
    @Override
    public ZCoordinate getZ() {
       return isSelected ? ZCoordinate.SelectedTower : ZCoordinate.Tower;
    }
 
    @Override
    public boolean doesTowerClashWith(Tower t) {
       Shape s = t.getBounds();
       if (s instanceof Circle) {
          Circle c = (Circle) s;
          return centre.distance(c.getCentre()) < bounds.getRadius() + c.getRadius();
       } else {
          return bounds.intersects(s.getBounds2D());
       }
    }
    
    @Override
    public boolean canTowerBeBuilt(List<Polygon> path) {
       for(Polygon p : path) {
          if(bounds.intersects(p)) {
             return false;
          }
       }
       return true;
    }
 
    @Override
    public boolean contains(Point p) {
       if(boundingRectangle.contains(p)) {
          int x = (int) (p.getX() - centre.getX() + halfWidth);
          int y = (int) (p.getY() - centre.getY() + halfWidth);
          // Only counts as a hit if the pixel has something in it, isn't completely transparent
          return !ImageHelper.isCompletelyTransparent(currentImage, x, y);
       }
       return false;
    }
 
    @Override
    public Shape getBounds() {
       return bounds;
    }
 
    @Override
    public String getName() {
       return name + " Tower";
    }
 
    @Override
    public Point getCentre() {
       return centre;
    }
 
    @Override
    public int getAttributeLevel(Attribute a) {
       return attributeLevels.get(a);
    }
 
    @Override
    public void raiseAttributeLevel(Attribute a, boolean boughtUpgrade) {
       if(boughtUpgrade) {
          attributeLevels.put(a, attributeLevels.get(a) + 1);
       }
       switch(a) {
          case Damage:
             upgradeDamage();
             break;
          case Range:
             upgradeRange();
             break;
          case Rate:
             upgradeFireRate();
             break;
          case Speed:
             upgradeBulletSpeed();
             break;
          case Special:
             upgradeSpecial();
             break;
          default:
             throw new RuntimeException("New Attribute has been added or something.");
       }
    }
    
    @Override
    public void aidAttribute(Attribute a, double factor, int towerID) {
       assert a != Attribute.Special : "Special cannot be aided";
       double currentFactor = currentFactors.get(a);
       if(factor == 1) { // This signals that this aid tower has been sold
          aidFactors.remove(towerID);
          // Find the best factor from the remaining factors
          for(Map<Attribute, Double> m : aidFactors.values()) {
             double d = m.get(a);
             if(d > factor) {
                factor = d;
             }
          }
          // If it isn't the current factor, make it so, as the current factor would've been removed
          if(factor != currentFactor) {
             multiplyAttribute(a, factor / currentFactor);
             currentFactors.put(a, factor);
          }
       } else { // Either a new aid tower has built, or an old one upgraded
          if(!aidFactors.containsKey(towerID)) {
             aidFactors.put(towerID, new EnumMap<Attribute, Double>(Attribute.class));
          }
          aidFactors.get(towerID).put(a, factor);
          // This limits it to one aid tower's upgrade (the best one)
          if(factor > currentFactor) {
             // This applies the new upgrade and cancels out the last one
             multiplyAttribute(a, factor / currentFactor);
             currentFactors.put(a, factor);
          }
       }
    }
    
    @Override
    public String getStat(Attribute a) {
       switch(a) {
          case Damage:
             return Helper.format(damage, 2);
          case Range:
             return Helper.format(range, 1);
          case Rate:
             return Helper.format(fireRate / Constants.CLOCK_TICKS_PER_SECOND, 2) + "s";
          case Speed:
             return Helper.format(bulletSpeed, 2);
          case Special:
             return getSpecial();
       }
       throw new RuntimeException("Invalid attribute: " + a + " given.");
    }
    
    @Override
    public String getStatName(Attribute a) {
       switch(a) {
          case Special:
             return getSpecialName();
          case Rate:
             return "Shoots Every";
          default:
             return a.toString();
       }
    }
 
    @Override
    public void select(boolean select) {
       isSelected = select;
    }
    
    @Override
    public Tower constructNew(Point p, List<Shape> pathBounds) {
       try {
          return this.getClass().getConstructor(Point.class, List.class).newInstance(p, pathBounds);
       } catch(Exception e) {
          // No exception should be thrown if the superclass is reasonably behaved
          throw new RuntimeException("\nSuperclass of AbstractTower is not well behaved.\n" + e +
                " was thrown.\n Either fix this, or override constructNew()");
       }
    }
    
    @Override
    public void addDamageNotifier(DamageNotifier d) {
       damageNotifiers.add(d);
    }
 
    @Override
    public synchronized void increaseDamageDealt(double damage) {
       assert damage > 0 : "Damage given was negative or zero.";
       for(DamageNotifier d : damageNotifiers) {
          d.notifyOfDamage(damage);
       }
       damage += fractionalDamageDealt; // Add leftover fractional damage from the last hit
       long longDamage = (long)damage;
       damageDealt += longDamage;
       // Handling of fractional amounts
       fractionalDamageDealt = damage - longDamage;
       while(damageDealt >= nextUpgradeDamage) {
          damageDealtLevel++;
          nextUpgradeDamage = Formulae.nextUpgradeDamage(damageDealtLevel);
          upgradeAllStats();
       }
    }
 
    @Override
    public synchronized void increaseKills(int kills) {
       assert kills > 0 : "Kills given was less than or equal to zero";
       for(DamageNotifier d : damageNotifiers) {
          d.notifyOfKills(kills);
       }
       this.kills += kills;
       while(this.kills >= nextUpgradeKills) {
          killsLevel++;
          nextUpgradeKills = Formulae.nextUpgradeKills(killsLevel);
          upgradeAllStats();
       }
    }
 
    @Override
    public ExperienceReport getExperienceReport() {
       // -1 so it starts at level 1 (rather than level 2, which is odd)
       int level = killsLevel + damageDealtLevel - 1;
       return new ExperienceReport(level, kills, nextUpgradeKills, (long) damageDealt,
             (long) nextUpgradeDamage);
    }
 
    @Override
    public BufferedImage getButtonImage() {
       return buttonImage;
    }
    
    @Override
    public void setCreepComparator(Comparator<Creep> c) {
       if(c instanceof DistanceComparator) {
          creepComparator = new DistanceComparator(centre,
                ((DistanceComparator) c).isClosestFirst());
       } else {
          creepComparator = c;
       }
    }
    
    @Override
    public Comparator<Creep> getCreepComparator() {
       return creepComparator;
    }
    
    @Override
    public void sell() {
       // Defaults to doing nothing when sold
    }
    
    protected double getFireRate() {
       return fireRate;
    }
    
    protected double getRange() {
       return range;
    }
    
    protected double getBulletSpeed() {
       return bulletSpeed;
    }
    
    protected double getDamage() {
       return damage;
    }
    
    protected double getTimeToNextShot() {
       return timeToNextShot;
    }
    
    protected abstract String getSpecial();
    
    protected abstract String getSpecialName();
 
    protected Bullet makeBullet(Vector2D dir, int turretWidth, int range, double speed,
          double damage, Point p, Creep c, List<Shape> pathBounds) {
       return new BasicBullet(this, dir, turretWidth, range, speed, damage, p, pathBounds);
    }
    
    protected List<Bullet> makeBullets(Vector2D dir, int turretWidth, int range,
             double speed, double damage, Point p, Creep c, List<Shape> pathBounds) {
       return Helper.makeListContaining(makeBullet(dir, turretWidth, range, speed, damage, p, c,
             pathBounds));
    }
    
    protected List<Bullet> fireBullets(List<Creep> creeps) {
       for(Creep c : creeps) {
          if(checkDistance(c)) {
             return fireBulletsAt(c, true);
          }
       }
       return Collections.emptyList();
    }
    
    protected boolean checkDistance(Creep c) {
       return checkDistance(c, centre, range);
    }
    
    /**
     * Checks if the distance from the creep to a point is less than the range.
     * @param c
     * @param p
     * @param range
     * @return
     */
    protected boolean checkDistance(Creep c, Point p, double range) {
       if (!c.isAlive()) {
          return false;
       }
       return p.distance(c.getPosition()) < range + c.getHalfWidth();
    }
    
    protected List<Bullet> fireBulletsAt(Creep c, boolean rotateTurret) {
       return fireBulletsAt(c, centre, rotateTurret, turretWidth, range, bulletSpeed, damage);
    }
    
    protected List<Bullet> fireBulletsAt(Creep c, Point p, boolean rotateTurret,
          int turretWidth, double range, double bulletSpeed, double damage) {
       Vector2D dir = Vector2D.createFromPoints(p, c.getPosition());
       if(imageRotates && rotateTurret) {
          currentImage = getRotatedImage(Vector2D.angle(dir.getX(), -dir.getY()));
       }
       return makeBullets(dir, turretWidth, (int)range, bulletSpeed, damage, p, c, pathBounds);
    }
 
    protected void upgradeDamage() {
       damage *= upgradeIncreaseFactor;
    }
 
    protected void upgradeRange() {
       range += currentFactors.get(Attribute.Range) * rangeUpgrade;
       twiceRange = (int)(range * 2);
    }
 
    protected void upgradeFireRate() {
       fireRate /= upgradeIncreaseFactor;
    }
 
    protected void upgradeBulletSpeed() {
       bulletSpeed += currentFactors.get(Attribute.Speed) * bulletSpeedUpgrade;
    }
 
    protected abstract void upgradeSpecial();
    
    protected void addExtraBullets(Bullet... bullets) {
       bulletsToAdd.addAll(Arrays.asList(bullets));
    }
    
    private BufferedImage loadImage(Map<String, BufferedImage> map, int width, String... imagePath) {
       String imageName = imagePath[imagePath.length - 1];
       if(!map.containsKey(imageName)) {
          if(width <= 0) {
             map.put(imageName, ImageHelper.loadImage(imagePath));
          } else {
             map.put(imageName, ImageHelper.loadImage(width, width, imagePath));
          }
       }
       return map.get(imageName);
    }
    
    private BufferedImage drawOverlayOnBaseImage(double angle) {
       BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
       
       Graphics2D g = image.createGraphics();
 
       AffineTransform at = AffineTransform.getRotateInstance(angle, image.getWidth() / 2,
             image.getHeight() / 2);
       BufferedImageOp op = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
       
       g.drawImage(baseImage, 0, 0, null);
       g.drawImage(overlayImage, op, 0, 0);
       
       g.dispose();
       return image;
    }
    
    private void upgradeAllStats() {
       upgradeDamage();
       upgradeRange();
       upgradeFireRate();
       upgradeBulletSpeed();
       upgradeSpecial();
    }
    
    private void drawRange(Graphics2D g) {
       drawRange(g, centre);
    }
    
    private void drawRange(Graphics2D g, Point p) {
       int topLeftRangeX = (int)(p.getX() - range);
       int topLeftRangeY = (int)(p.getY() - range);
       
       Graphics2D g2D = (Graphics2D) g.create();
       
       // This draws the white shadow showing the range
       g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5F));
       g2D.setColor(Color.WHITE);
       g2D.fillOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
 
       // This draws a circle at the edge of the range
       // Antialiasing makes this look much nicer
       g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
       g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
       g2D.setStroke(new BasicStroke(2));
       g2D.setColor(Color.DARK_GRAY);
       g2D.drawOval(topLeftRangeX, topLeftRangeY, twiceRange, twiceRange);
       
       g2D.dispose();
    }
 
    private void setBounds() {
       boundingRectangle.setBounds((int) topLeft.getX(), (int) topLeft.getY(), width, width);
       bounds.setCentre(centre);
    }
    
    private void multiplyAttribute(Attribute a, double factor) {
       switch(a) {
          case Damage:
             damage *= factor;
             return;
          case Range:
             range *= factor;
             twiceRange = (int)(range * 2);
             return;
          case Rate:
             fireRate /= factor;
             return;
          case Speed:
             bulletSpeed *= factor;
             return;
          case Special:
             assert false : "Special cannot be simply multiplied";
             return;
       }
    }
    
    private Map<Attribute, Double> createCurrentFactors() {
       Map<Attribute, Double> map = new EnumMap<Attribute, Double>(Attribute.class);
       for(Attribute a : Attribute.values()) {
          if(a != Attribute.Special) {
             map.put(a, 1.0);
          }
       }
       return map;
    }
    
    private BufferedImage getRotatedImage(double angle) {
       if(!rotatedImages.containsKey(getClass())) {
          rotatedImages.put(getClass(), new HashMap<LooseFloat, BufferedImage>());
       }
       Map<LooseFloat, BufferedImage> m = rotatedImages.get(getClass());
       // Use LooseFloat to reduce precision so rotated images are less likely to be duplicated
       LooseFloat f = new LooseFloat(angle) {
          @Override
          public float getPrecision() {
             // Watch out with decreasing this, while it may improve the quality, because images are
             // cached it can increase the memory use significantly
             // I'd prefer to have it a bit lower, but the reduced range of angle is almost
             // unnoticeable at this setting.
             return 0.09F;
          }
       };
       if(!m.containsKey(f)) {
          m.put(f, drawOverlayOnBaseImage(angle));
       }
       return m.get(f);
    }
    
    private Map<Attribute, Integer> createAttributeLevels() {
       Map<Attribute, Integer> map = new EnumMap<Attribute, Integer>(Attribute.class);
       for(Attribute a : Attribute.values()) {
          // All levels start at 1
          map.put(a, 1);
       }
       return map;
    }
    
    private static BufferedImage createRotatingImage(int width, int turretWidth) {
       assert turretWidth > 0 : "turretWidth must be > 0";
       BufferedImage turretCentre = ImageHelper.loadImage("towers", "overlays", "turrets",
             "turretCentre.png");
       BufferedImage image = new BufferedImage(width, width, BufferedImage.TYPE_INT_ARGB_PRE);
       Graphics2D g = image.createGraphics();
       g.drawImage(turretCentre, width/2 - turretThickness/2, width/2 - turretThickness/2, null);
       g.setColor(Color.BLACK);
       g.fillRect(width/2 - turretThickness/2, width/2 - turretWidth, turretThickness, turretWidth);
       // TODO perhaps make the end of the turret a little prettier
       g.dispose();
       return image;
    }
    
    public static void main(String... args) {
       for(int i = 1; i <= 25; i++) {
          ImageHelper.writePNG(createRotatingImage(50, i), "towers", "rotatingOverlays", "turrets",
                "turret" + i + ".png");
       }
    }
 
 }
