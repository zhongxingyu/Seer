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
 
 package towers;
 
 import gui.Circle;
 import images.ImageHelper;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.awt.image.BufferedImage;
 import java.util.List;
 import java.util.Set;
import java.util.TreeSet;
 
 import sprites.Sprite;
 import sprites.Sprite.DamageReport;
 
 public class BomberTower extends AbstractTower {
 
    private static final BufferedImage image = ImageHelper.makeImage("towers", "bomber.png");
    private static final BufferedImage buttonImage = ImageHelper.makeImage("buttons",
          "BomberTower.png");
    private int blastRadius = 20;
    private final int blastRadiusIncrease = (int) (blastRadius * upgradeIncreaseFactor)
          - blastRadius;
 
    public BomberTower() {
       this(new Point());
    }
 
    public BomberTower(Point p) {
       super(p, "Bomber", 40, 100, 5, 5, 50, 15, image);
    }
 
    @Override
    public BufferedImage getButtonImage() {
       return buttonImage;
    }
 
    @Override
    public String getSpecial() {
       return Integer.toString(blastRadius);
    }
 
    @Override
    public String getSpecialName() {
       return "Blast Radius";
    }
 
    @Override
    protected void upgradeSpecial() {
       blastRadius += blastRadiusIncrease;
    }
 
    @Override
    protected Bullet makeBullet(double dx, double dy, int turretWidth, int range, double speed,
          double damage, Point p) {
       return new Bomb(this, dx, dy, turretWidth, range, speed, damage, p);
    }
 
    private class Bomb extends AbstractBullet {
 
       private boolean exploding = false;
       private boolean expanding = true;
      private Set<Sprite> hitSprites = new TreeSet<Sprite>();
       private final Color blastColour = new Color(255, 0, 0, 150);
       private final int blastSizeIncrement;
       private final int frames = 5;
       private final Circle blast = new Circle(new Point(0, 0), 0);
       private double moneyEarnt;
 
       public Bomb(Tower shotBy, double dx, double dy, int turretWidth, int range, double speed,
             double damage, Point p) {
          super(shotBy, dx, dy, turretWidth, range, speed, damage, p);
          blastSizeIncrement = blastRadius / frames;
       }
 
       @Override
       public double tick(List<Sprite> sprites) {
          if (exploding) {
             double radius = blast.getRadius();
             if (expanding) {
                blast.setRadius(radius + blastSizeIncrement);
                if (blast.getRadius() >= blastRadius) {
                   blast.setRadius(blastRadius);
                   expanding = false;
                }
             } else {
                blast.setRadius(radius - blastSizeIncrement * 2);
                if (blast.getRadius() < 0) {
                   return moneyEarnt;
                }
             }
             checkForSprites(sprites);
             return -1;
          } else {
             double earnings = super.tick(sprites);
             if (earnings <= 0) {
                return earnings;
             } else {
                moneyEarnt = earnings;
                return -1;
             }
          }
       }
 
       @Override
       public void draw(Graphics g) {
          if (exploding) {
             g.setColor(blastColour);
             blast.fill(g);
          } else {
             super.draw(g);
          }
       }
 
       @Override
       protected void specialOnHit(Point2D p, Sprite s) {
          // System.out.println(p.getX() + " " + p.getY());
          blast.setCentre(p);
          exploding = true;
       }
 
       private void checkForSprites(List<Sprite> sprites) {
          for (Sprite s : sprites) {
             if (!hitSprites.contains(s)) {
                // Sprites are only affected by the blast once
                if (blast.intersects(s.getBounds())) {
                   hitSprites.add(s);
                   DamageReport d = s.hit(getDamage() / 5);
                   if (d != null) {
                      moneyEarnt += processDamageReport(d);
                   }
                }
             }
          }
       }
 
    }
 
 }
