 /*
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package celestial;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Random;
 import lib.Parser.Term;
 import lib.WobblyStroke;
 import org.ankh.unfall.planet.PlanetInformation;
 import org.ankh.unfall.planet.texgen.ContinentalGenerator;
 import org.ankh.unfall.planet.texgen.PlanetGenerator;
 import org.ankh.unfall.planet.texgen.palette.TerrainPalette;
 import org.ankh.unfall.planet.texgen.palette.ranges.AlienPalette;
 import org.ankh.unfall.planet.texgen.palette.ranges.EarthPalette;
 import org.ankh.unfall.planet.texgen.palette.ranges.HospitablePalette;
 import org.ankh.unfall.planet.texgen.palette.ranges.LavaPalette;
 import org.ankh.unfall.planet.texgen.palette.ranges.MarsPalette;
 import org.ankh.unfall.planet.texgen.palette.ranges.StrangePalette;
 import universe.Universe;
 
 /**
  *
  * @author nwiehoff
  */
 public class Planet extends Celestial {
 
     private Term texture;
     private int seed = 0;
     protected int diameter;
     protected transient Image raw_tex;
     private ArrayList<Rectangle> bound = new ArrayList<>();
 
     public Planet(String name, Term texture, int diameter) {
         setName(name);
         this.texture = texture;
         this.diameter = diameter;
     }
 
     @Override
     public void init(boolean loadedGame) {
         state = State.ALIVE;
     }
 
     public void initGraphics() {
         /*
          * Load the image for this planet and scale it
          */
         try {
             BufferedImage tmp = new BufferedImage(getUniverse().getSettings().RENDER_SIZE, getUniverse().getSettings().RENDER_SIZE, BufferedImage.TYPE_INT_ARGB);
             //get graphics
             Shape circle = new Ellipse2D.Float(0, 0, getUniverse().getSettings().RENDER_SIZE, getUniverse().getSettings().RENDER_SIZE);
             Graphics2D gfx = (Graphics2D) tmp.getGraphics();
             //only draw inside the circle
             gfx.setClip(circle);
             gfx.clip(circle);
             //debug background
             gfx.setColor(Color.PINK);
             gfx.fillRect(0, 0, getUniverse().getSettings().RENDER_SIZE, getUniverse().getSettings().RENDER_SIZE);
             if (texture.getValue("group").matches("rock")) {
                 /*
                  * The procedural planet generator in the com package gets to do
                  * all the heavy lifting and we just read the output.
                  */
                 //setup RNG
                 Random sRand = new Random(seed);
                 //create planet info
                 PlanetInformation info = new PlanetInformation();
                 info.setDaytime(360);
                 info.setEquatorTemperature(sRand.nextInt(100));
                info.setPoleTemperature(sRand.nextInt(Math.max(info.getEquatorTemperature(), 1)) - 50);
                 info.setRadius(diameter / 2);
                 info.setWaterInPercent(sRand.nextFloat());
                 info.setHeightFactor(sRand.nextFloat());
                 info.setSeed(seed);
                 info.setHumidity(sRand.nextFloat());
                 info.setSmoothness(sRand.nextInt(3) + 7);
                 //setup palette
                 TerrainPalette palette = null;
                 String pal = texture.getValue("palette");
                 if (pal.matches("Earth")) {
                     palette = new EarthPalette(info);
                 } else if (pal.matches("Mars")) {
                     palette = new MarsPalette(info);
                 } else if (pal.matches("Hospitable")) {
                     palette = new HospitablePalette(info);
                 } else if (pal.matches("Strange")) {
                     palette = new StrangePalette(info);
                 } else if (pal.matches("Lava")) {
                     palette = new LavaPalette(info);
                 } else if (pal.matches("Alien")) {
                     palette = new AlienPalette(info);
                 }
                 //call the procedural planet generator
                 PlanetGenerator plan = new ContinentalGenerator(2 * getUniverse().getSettings().RENDER_SIZE, getUniverse().getSettings().RENDER_SIZE, info, palette);
                 //paint texture
                 gfx.drawImage(plan.getDebugImageMap(PlanetGenerator.MAP_COLOR), 0, 0, null);
                 //store texture
                 raw_tex = tmp;
             } else if (texture.getValue("group").matches("doublegas")) {
                 Random sRand = new Random(seed);
                 /*
                  * My gas giants are conservative. They have a color and brightness
                  * which is held constant while bands are drawn varying the saturation.
                  * 
                  * Two passes are made. The first draws primary bands, which define the
                  * overall look. The second does secondary bands which help de-alias
                  * the planet.
                  */
                 //setup stroke
                 int range = (int) (0.007 * getUniverse().getSettings().RENDER_SIZE);
                 int min = (int) (0.01 * range) + 1;
                 gfx.setStroke(new WobblyStroke(sRand.nextInt(range) + min, sRand.nextInt(range) + min, seed));
                 //determine band count
                 int bands = sRand.nextInt(75) + 25;
                 int bandHeight = (getUniverse().getSettings().RENDER_SIZE / bands);
                 //pick sat and val
                 float sat = sRand.nextFloat();
                 float value = sRand.nextFloat();
                 if (value < 0.45f) {
                     value = 0.45f;
                 }
                 //pick a hue
                 float hue = sRand.nextFloat();
                 //draw a baseplate
                 gfx.setColor(new Color(Color.HSBtoRGB(hue, sat, value)));
                 gfx.fillRect(0, 0, getUniverse().getSettings().RENDER_SIZE, getUniverse().getSettings().RENDER_SIZE);
                 //pass 1, big bands
                 for (int a = 0; a < bands; a++) {
                     //vary saturation
                     sat = sRand.nextFloat();
                     //draw a band
                     Color raw = new Color(Color.HSBtoRGB(hue, sat, value));
                     Color col = new Color(raw.getRed(), raw.getGreen(), raw.getBlue(), 64);
                     gfx.setColor(col);
                     gfx.drawRect(0, sRand.nextInt(getUniverse().getSettings().RENDER_SIZE), getUniverse().getSettings().RENDER_SIZE, bandHeight);
                 }
                 //pick a hue
                 hue = sRand.nextFloat();
                 //pass 2, small secondary bands
                 for (int a = 0; a < bands * 4; a++) {
                     //vary saturation
                     sat = sRand.nextFloat();
                     //draw a band
                     Color raw = new Color(Color.HSBtoRGB(hue, sat, value));
                     Color col = new Color(raw.getRed(), raw.getGreen(), raw.getBlue(), 16);
                     gfx.setColor(col);
                     gfx.drawRect(0, sRand.nextInt(getUniverse().getSettings().RENDER_SIZE), getUniverse().getSettings().RENDER_SIZE, bandHeight);
                 }
                 //store
                 raw_tex = tmp;
             } else if (texture.getValue("group").matches("singlegas")) {
                 Random sRand = new Random(seed);
                 //setup stroke
                 int range = (int) (0.007 * getUniverse().getSettings().RENDER_SIZE);
                 int min = (int) (0.125 * range) + 1;
                 gfx.setStroke(new WobblyStroke(sRand.nextInt(range) + min, sRand.nextInt(range) + min, seed));
                 /*
                  * My gas giants are conservative. They have a color and brightness
                  * which is held constant while bands are drawn varying the saturation.
                  * 
                  * Two passes are made. The first draws primary bands, which define the
                  * overall look. The second does secondary bands which help de-alias
                  * the planet.
                  */
                 //determine band count
                 int bands = sRand.nextInt(75) + 25;
                 int bandHeight = (getUniverse().getSettings().RENDER_SIZE / bands);
                 //pick sat and val
                 float sat = sRand.nextFloat();
                 float value = sRand.nextFloat();
                 if (value < 0.45f) {
                     value = 0.45f;
                 }
                 //pick a hue
                 float hue = sRand.nextFloat();
                 //draw a baseplate
                 gfx.setColor(new Color(Color.HSBtoRGB(hue, sat, value)));
                 gfx.fillRect(0, 0, getUniverse().getSettings().RENDER_SIZE, getUniverse().getSettings().RENDER_SIZE);
                 //pass 1, big bands
                 for (int a = 0; a < bands; a++) {
                     //vary saturation
                     sat = sRand.nextFloat();
                     //draw a band
                     Color raw = new Color(Color.HSBtoRGB(hue, sat, value));
                     Color col = new Color(raw.getRed(), raw.getGreen(), raw.getBlue(), 64);
                     gfx.setColor(col);
                     gfx.drawRect(0, sRand.nextInt(getUniverse().getSettings().RENDER_SIZE), getUniverse().getSettings().RENDER_SIZE, bandHeight);
                 }
                 //pass 2, small secondary bands
                 for (int a = 0; a < bands * 4; a++) {
                     //vary saturation
                     sat = sRand.nextFloat();
                     //draw a band
                     Color raw = new Color(Color.HSBtoRGB(hue, sat, value));
                     Color col = new Color(raw.getRed(), raw.getGreen(), raw.getBlue(), 16);
                     gfx.setColor(col);
                     gfx.drawRect(0, sRand.nextInt(getUniverse().getSettings().RENDER_SIZE), getUniverse().getSettings().RENDER_SIZE, bandHeight);
                 }
                 //store
                 raw_tex = tmp;
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
     public void disposeGraphics() {
         raw_tex = null;
     }
 
     @Override
     public ArrayList<Rectangle> getBounds() {
         return bound;
     }
 
     @Override
     public void alive() {
         //update bound
         bound.clear();
         bound.add(new Rectangle((int) getX(), (int) getY(), getDiameter(), getDiameter()));
     }
 
     @Override
     public void render(Graphics f, double dx, double dy) {
         if (raw_tex != null) {
             Graphics2D s = (Graphics2D) (f);
             s.drawImage(raw_tex, (int) (getX() - dx), (int) (getY() - dy), getDiameter(), getDiameter(), null);
         } else {
             initGraphics();
         }
     }
 
     public int getDiameter() {
         return diameter;
     }
 
     public void setRadius(int radius) {
         this.diameter = radius;
     }
 
     public Term getTexture() {
         return texture;
     }
 
     public void setTexture(Term texture) {
         this.texture = texture;
     }
 
     public int getSeed() {
         return seed;
     }
 
     public void setSeed(int seed) {
         this.seed = seed;
     }
 }
