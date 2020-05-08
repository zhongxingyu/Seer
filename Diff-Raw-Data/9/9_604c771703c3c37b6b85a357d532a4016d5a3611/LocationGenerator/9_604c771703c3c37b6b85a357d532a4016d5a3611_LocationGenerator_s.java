 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.util;
 
 import java.awt.Color;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import org.apache.commons.lang.Validate;
 import com.quartercode.disconnected.sim.comp.Location;
 
 /**
  * This utility class generates random locations on an earth map.
  * 
  * @see Location
  */
 public class LocationGenerator {
 
     private static BufferedImage map;
 
     /**
      * Generates the given amount of locations on an earth map.
      * 
      * @param amount The amount of locations to generate.
      * @return The generated locations.
      */
     public static List<Location> generateLocations(int amount) {
 
         return generateLocations(amount, null);
     }
 
     /**
      * Generates the given amount of locations on an earth map, ignoring the given ignore locations.
      * 
      * @param amount The amount of locations to generate.
      * @return The generated locations.
      */
     public static List<Location> generateLocations(int amount, List<Location> ignore) {
 
        Validate.isTrue(amount > 0, "Generation count must be > 0");
 
         if (map == null) {
             try {
                 map = ImageIO.read(LocationGenerator.class.getResource("/data/map.png"));
             }
             catch (IOException e) {
                 throw new RuntimeException("Can't read map data image", e);
             }
         }
 
         if (ignore == null) {
             ignore = new ArrayList<Location>();
         }
 
         int width = map.getWidth();
         int height = map.getHeight();
 
         List<Location> result = new ArrayList<Location>();
         RandomPool random = new RandomPool(100);
         int blackRGB = Color.BLACK.getRGB();
         while (result.size() < amount) {
             int x = random.nextInt(width);
             int y = random.nextInt(height);
             if (map.getRGB(x, y) == blackRGB) {
                 Location location = new Location((float) x / (float) width, (float) y / (float) height);
                 if (!ignore.contains(location) && !result.contains(location)) {
                     result.add(location);
                 }
             }
         }
 
         return result;
     }
 
     private LocationGenerator() {
 
     }
 
 }
