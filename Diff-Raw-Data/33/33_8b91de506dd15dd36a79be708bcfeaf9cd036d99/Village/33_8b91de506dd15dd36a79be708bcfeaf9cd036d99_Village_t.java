 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.first.tribes.core.being;
 
 import com.first.tribes.core.IrrigationPipe;
 import com.first.tribes.core.Tile;
 import com.first.tribes.core.TribesWorld;
 import com.first.tribes.core.being.Villager;
 import com.first.tribes.core.being.Being.Personality;
 import com.first.tribes.core.util.Updatee;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import playn.core.CanvasImage;
 import playn.core.Color;
 import playn.core.Font;
 import static playn.core.PlayN.*;
 import playn.core.Surface;
 import playn.core.TextFormat;
 import playn.core.TextLayout;
 import pythagoras.f.Rectangle;
 
 /**
  *
  * @author taylor
  */
 public class Village implements Updatee {
 
 
     public static final float MIN_INTELLIGENCE_TO_BUILD_IRRIGATION_PIPE = 2f;
     public static final float INTELLIGENCE_BASELINE = 0.20f;
     public static final float POSITION_DEVIATION = 10.0f;
     public static final float REPRODUCTIVE_BASE_RATE = 0.02f;
     public static final float REPRODUCTIVE_MODIFIER = 0.6f;
     public static final float FOOD_PRODUCTION_THRESHOLD = 0.22f;
     public static final float FOOD_PRODUCTION_RATE = .8f;
     public static final int MIN_POPULATION = 10;
     public static final int MAX_POPULATION = 1000;
     public static final float POPULATION_DAMPEN_EXP = 0.09f;
     public static final int INITIAL_MANNA = 200;
     public static final int MANNA_PER_BIRTH = 1;
     public static final int MANNA_PER_DEATH = 1;
     public static final int MANNA_PER_KILL = 2;
     private List<Villager> villagers;
     private float[][] densityMap;
     private TribesWorld world;
     private int color;
     private int foodPool;
     private int manna;
 
     public Village(float x, float y, int numVillagers, TribesWorld world, int color) {
         this.world = world;
         this.color = color;
         manna = INITIAL_MANNA;
         villagers = new ArrayList<Villager>(numVillagers * 30);
 
         densityMap = new float[world.tileWidth()][world.tileHeight()];
         for (int i = 0; i < densityMap.length; i++) {
             for (int j = 0; j < densityMap[i].length; j++) {
                 densityMap[i][j] = 0f;
             }
         }
 
         for (int i = 0; i < numVillagers; i++) {
             float villagerX = randomDist(x, POSITION_DEVIATION);
             float villagerY = randomDist(y, POSITION_DEVIATION);
             villagers.add(new Villager(villagerX, villagerY, this, randomVillagerColor()));
         }
     }
 
     public Village(float x, float y, List<Personality> personalities, TribesWorld world, int color) {
         this.world = world;
         this.color = color;
         manna = INITIAL_MANNA;
         villagers = new ArrayList<Villager>(personalities.size() * 30);
 
         densityMap = new float[world.tileWidth()][world.tileHeight()];
         for (int i = 0; i < densityMap.length; i++) {
             for (int j = 0; j < densityMap[i].length; j++) {
                 densityMap[i][j] = 0f;
             }
         }
 
         for (Personality personality : personalities) {
             float villagerX = randomDist(x, POSITION_DEVIATION);
             float villagerY = randomDist(y, POSITION_DEVIATION);
             Villager villager = new Villager(villagerX, villagerY, this, randomVillagerColor());
             villager.personality = personality;
             villagers.add(villager);
         }
     }
 
     public void spawnVillager(float x, float y) {
         villagers.add(new Villager(x, y, this, randomVillagerColor()));
     }
 
     public void spawnAvatar(float x, float y, int trait) {
         villagers.add(new Avatar(x, y, this, randomVillagerColor(), trait));
     }
 
     public int randomVillagerColor() {
         int red = (int) (((random() - 0.5f) * 30) + Color.red(color));
         if (red > 255)
             red = 255;
         if (red < 0)
             red = 0;
 
         int green = (int) (((random() - 0.5f) * 30) + Color.green(color));
         if (green > 255)
             green = 255;
         if (green < 0)
             green = 0;
 
         int blue = (int) (((random() - 0.5f) * 30) + Color.blue(color));
         if (blue > 255)
             blue = 255;
         if (blue < 0)
             blue = 0;
 
         return Color.rgb(red, green, blue);
     }
 
     public float xPos() {
         float xPos = 0;
 
         for (Villager villager : villagers) {
             xPos += villager.xPos;
         }
 
         return xPos / villagers.size();
     }
 
     public float yPos() {
         float yPos = 0;
 
         for (Villager villager : villagers) {
             yPos += villager.yPos;
         }
 
         return yPos / villagers.size();
     }
 
     public static float randomDist(float mean, float stddev) {
         float random = (random() * 2 - 1) + (random() * 2 - 1) + (random() * 2 - 1);
         return random * stddev + mean;
     }
 
     float worldWidth() {
         return world.width();
     }
 
     float worldHeight() {
         return world.height();
     }
 
     public float reproductiveBaseRate() {
         if (villagers.size() < MIN_POPULATION) {
             return REPRODUCTIVE_BASE_RATE * 10 / (float) (Math.sqrt(villagers.size()));
         } else if (villagers.size() > MAX_POPULATION) {
             return REPRODUCTIVE_BASE_RATE / (float) Math.pow(villagers.size() - MAX_POPULATION, POPULATION_DAMPEN_EXP);
         } else {
             return REPRODUCTIVE_BASE_RATE;
         }
     }
 
     @Override
     public void update(float delta) {
 
         float SPREAD_CONST = 0.011f;
         float DECR_CONST = 0.90f;
 
         for (int i = 0; i < villagers.size(); i++) {
 
             Villager villager = villagers.get(i);
             densityMap[tileAt(villager.xPos, villager.yPos).getXIndex()][tileAt(villager.xPos, villager.yPos).getYIndex()] = 1f;
 
         }
 
         for (int i = 0; i < densityMap.length; i++) {
             for (int j = 0; j < densityMap[i].length; j++) {
 
                 densityMap[i][j] *= (DECR_CONST);
                
 
                 int rad = 1;
                 for (int k = Math.max(i - rad, 0); k <= Math.min(i + rad, densityMap.length - 1); k++) {
                     for (int l = Math.max(j - rad, 0); l <= Math.min(j + rad, densityMap[k].length - 1); l++) {
                         densityMap[i][j] += densityMap[k][l] * SPREAD_CONST;
                     }
                 }
                if (densityMap[i][j] < 0 || !world.tiles()[i][j].isSafe(0)) {
                    densityMap[i][j] = 0f;
                }
                if (densityMap[i][j] > 1) {
                    densityMap[i][j] = 1f;
                }
             }
         }
 
 
 
         float[][] intelligenceDensity = new float[world.tileWidth()][world.tileHeight()];
 
         for (int i = 0; i < villagers.size(); i++) {
             Villager villager = villagers.get(i);
             villager.update(delta);
             if (villager.isDead()) {
                 villagers.remove(villager);
                 manna += MANNA_PER_DEATH;
                 i--;
             } else {
                 Tile tile = villager.myTile();
                 intelligenceDensity[tile.getXIndex()][tile.getYIndex()] += (villager.personality.intelligence() - INTELLIGENCE_BASELINE);
             }
         }
 
         for (int i = 0; i < intelligenceDensity.length; i++) {
             for (int j = 0; j < intelligenceDensity[0].length; j++) {
                 if (intelligenceDensity[i][j] > MIN_INTELLIGENCE_TO_BUILD_IRRIGATION_PIPE) {
                     if(world.tiles()[i][j].accessories().size() == 0) {
                         IrrigationPipe pipe = new IrrigationPipe(world.tiles()[i][j].bounds().centerX(), world.tiles()[i][j].bounds().centerY(), world);
                         pipe.addToWorld();
                     }
                 }
             }
         }
 
         int villageSize = villagers.size();
         for (Villager villager : new ArrayList<Villager>(villagers.subList(0, villageSize))) {
             if (villager.personality.reproductiveAppeal() * reproductiveBaseRate() > random()) {
                 reproduce(villager);
                 manna += MANNA_PER_BIRTH;
             }
         }
 
         if (System.currentTimeMillis() > lastVisualInfoUpdate + VISUAL_INFO_UPDATE_TIME) {
             lastVisualInfoUpdate = System.currentTimeMillis();
             visualInfo = null;
         }
     }
 
     public float getDensityAt(int x, int y) {
         return densityMap[x][y];
 
     }
 
     public float getDensityAt(float x, float y) {
         return getDensityAt(tileAt(x, y));
     }
 
     public float getDensityAt(Tile t) {
         return densityMap[t.getXIndex()][t.getYIndex()];
     }
 
     public void reproduce(Villager matingVillager) {
         float totalMusk = 0;
         for (Villager villager : villagers) {
             if (villager != matingVillager) {
                 totalMusk += villager.personality.reproductiveAppeal();
             }
         }
 
         float chosenMusk = totalMusk * random();
 
         Villager mate = null;
 
         for (Villager villager : villagers) {
             if (villager != matingVillager) {
                 chosenMusk -= villager.personality.reproductiveAppeal();
             }
             if (chosenMusk <= 0) {
                 mate = villager;
                 break;
             }
         }
 
         if (mate != null) {
             Personality childPersonality = matingVillager.personality.reproduceWith(mate.personality);
             float villagerX = randomDist(xPos(), POSITION_DEVIATION);
             float villagerY = randomDist(yPos(), POSITION_DEVIATION);
 
             villagerX = matingVillager.xPos;
             villagerY = matingVillager.yPos;
 
             Villager child = new Villager(villagerX, villagerY, this, randomVillagerColor());
             child.personality = childPersonality;
             villagers.add(child);
         }
     }
 
     public boolean isUnsafe(float xPos, float yPos) {
         return world.unsafe(xPos, yPos, 0);
     }
 
     float gatherFood(Villager villager, float requestedFood) {
         Tile tile = world.tileAt(villager.xPos, villager.yPos);
         float foodGathered = Math.min(tile.numFood, requestedFood * (float) Math.pow(1 - villager.personality.intelligence(), 2));
 
         if (villager.personality.intelligence() > FOOD_PRODUCTION_THRESHOLD) {
             float foodProduced = (villager.personality.intelligence() - FOOD_PRODUCTION_THRESHOLD) * FOOD_PRODUCTION_RATE;
             tile.numFood += foodProduced;
         }
 
         tile.numFood -= foodGathered;
         foodGathered = foodGathered / (float) Math.pow(1 - villager.personality.intelligence(), 2);
         return foodGathered;
     }
 
     List<Villager> villagersInArea(Rectangle rectangle) {
         if (rectangle.width < 0) {
             rectangle.x = rectangle.x + rectangle.width;
             rectangle.width *= -1;
         }
         if (rectangle.height < 0) {
             rectangle.y = rectangle.y + rectangle.height;
             rectangle.height *= -1;
         }
 
         List<Villager> area = new ArrayList<Villager>();
         for (Villager villager : villagers) {
             if (rectangle.contains(villager.xPos, villager.yPos)) {
                 area.add(villager);
             }
         }
 
         return area;
     }
     public static final float STATS_BOX_HEIGHT = 200f;
     CanvasImage visualInfo;
     private long lastVisualInfoUpdate;
     private static final long VISUAL_INFO_UPDATE_TIME = 1000;
     private Font titleFont;
     private Font textFont;
 
     public void drawStatsBoxAt(Surface surface, float x, float y, float width, float height) {
         if (visualInfo == null) {
             visualInfo = graphics().createImage((int) width, (int) height);
             float repAppeal = 0;
             float longevity = 0;
             float intelligence = 0;
             float loyalty = 0;
             float mobility = 0;
             float hardiness = 0;
             float aggression = 0;
 
             for (Villager villager : villagers) {
                 repAppeal += villager.personality.reproductiveAppeal();
                 longevity += villager.personality.longevity();
                 intelligence += villager.personality.intelligence();
                 loyalty += villager.personality.loyalty();
                 mobility += villager.personality.mobility();
                 hardiness += villager.personality.hardiness();
                 aggression += villager.personality.aggression();
             }
             intelligence = ((int) ((intelligence / villagers.size()) * 1000)) / 1000f;
             mobility = ((int) ((mobility / villagers.size()) * 1000)) / 1000f;
             hardiness = ((int) ((hardiness / villagers.size()) * 1000)) / 1000f;
             repAppeal = ((int) ((repAppeal / villagers.size()) * 1000)) / 1000f;
             longevity = ((int) ((longevity / villagers.size()) * 1000)) / 1000f;
             aggression = ((int) ((aggression / villagers.size()) * 1000)) / 1000f;
             loyalty = ((int) ((loyalty / villagers.size()) * 1000)) / 1000f;
 
 //            visualInfo.canvas().setFillGradient(graphics().createLinearGradient(width, 0, width, height, new int[]{Color.rgb(50, 50, 50), Color.rgb(0, 0, 0)}, new float[]{0, 1}));
             visualInfo.canvas().setFillColor(Color.rgb(0, 0, 0));
             visualInfo.canvas().fillRect(0, 0, width, height);
 
             visualInfo.canvas().setFillColor(this.color);
             visualInfo.canvas().fillRect(5, 5, width - 10, height - 10);
 
             if (titleFont == null)
                 titleFont = graphics().createFont("Sans serif", Font.Style.PLAIN, 18);
             TextLayout nameLayout = graphics().layoutText(villagers.size() + " villagers -- " + manna + " manna", new TextFormat().withFont(titleFont).withWrapWidth(width));
             visualInfo.canvas().setFillColor(Color.argb(200, 255, 255, 255));
             visualInfo.canvas().fillText(nameLayout, 8, 4);
 
             StringBuilder stats = new StringBuilder();
             stats.append("Avg Rep. Appeal: " + repAppeal);
             stats.append('\n');
             stats.append("Avg Longevity: " + longevity);
             stats.append('\n');
             stats.append("Avg Intelligence: " + intelligence);
             stats.append('\n');
             stats.append("Avg Mobility: " + mobility);
             stats.append('\n');
             stats.append("Avg Hardiness: " + hardiness);
             stats.append('\n');
             stats.append("Avg Aggression: " + aggression);
             stats.append('\n');
             stats.append("Avg Loyalty: " + loyalty);
             stats.append('\n');
 
             if (textFont == null)
                 textFont = graphics().createFont("Sans serif", Font.Style.PLAIN, 15);
             TextLayout intelligenceLayout = graphics().layoutText(stats.toString(), new TextFormat().withFont(textFont).withWrapWidth(width));
             visualInfo.canvas().setFillColor(Color.argb(200, 255, 255, 255));
             visualInfo.canvas().fillText(intelligenceLayout, 8, 10 + nameLayout.height());
 
 //        TextLayout mobilityLayotu = graphics().layoutText("Avg Mobility: " + intelligence, new TextFormat().withFont(textFont).withWrapWidth(width));
 //        visualInfo.canvas().setFillColor(Color.argb(200, 255, 255, 255));
 //        visualInfo.canvas().fillText(intelligenceLayout, 8, 10 + nameLayout.height());
         }
         surface.drawImage(visualInfo, x, y);
     }
 
     public Tile tileAt(float xPos, float yPos) {
         return world.tileAt(xPos, yPos);
     }
 
     public Collection<? extends Villager> villagers() {
         return villagers;
     }
 
     public List<Village> enemyVillages() {
         List<Village> enemies = world.villages();
         enemies.remove(this);
         return enemies;
     }
 
     public int color() {
         return color;
     }
 
     public int manna() {
         return manna;
     }
 
     public ArrayList<Cave> enemyCaves() {
         return world.caves();
     }
 
     public void costManna(int mannaCost) {
         manna -= mannaCost;
     }
 
     public List<Personality> samplePersonalities(int i) {
         List<Personality> sample = new ArrayList<Personality>();
 
         while (sample.size() < i) {
             sample.add(villagers.get((int) (random() * villagers.size())).personality.copy());
         }
 
         return sample;
     }
 }
