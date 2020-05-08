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
 
 package com.quartercode.disconnected.sim.comp;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
 import com.quartercode.disconnected.sim.comp.ComputerPart.ComputerPartAdapter;
 
 /**
  * This class stores information about an operating system.
  * This also contains a list of all vulnerabilities this operating system has.
  * 
  * @see ComputerPart
  */
 @XmlJavaTypeAdapter (value = ComputerPartAdapter.class)
 public class OperatingSystem extends ComputerPart {
 
     private static final long serialVersionUID = 1L;
 
     private List<RightLevel>  rightLevels;
 
     /**
      * Creates a new operating system and sets the name, the vulnerabilities and the avaiable right levels.
      * 
      * @param name The name the operating system has.
      * @param vulnerabilities The vulnerabilities the operating system has.
      * @param rightLevels A list of all right levels which a user can have on this system.
      */
     public OperatingSystem(String name, List<Vulnerability> vulnerabilities, List<RightLevel> rightLevels) {
 
         super(name, vulnerabilities);
 
         this.rightLevels = rightLevels;
     }
 
     /**
      * Creates a new operating system and sets the name, the vulnerabilities and the avaiableright levels using a given string list.
      * The given right level string list gets splitted at commas, the trimmed parts should represent names.
      * 
      * @param name The name the operating system has.
      * @param vulnerabilities The vulnerabilities the operating system has.
      * @param rightLevels A list of all right levels which a user can have on this system.
      */
     public OperatingSystem(String name, List<Vulnerability> vulnerabilities, String rightLevels) {
 
         super(name, vulnerabilities);
 
         this.rightLevels = new ArrayList<RightLevel>();
         for (String rightLevel : rightLevels.split(",")) {
             this.rightLevels.add(new RightLevel(rightLevel.trim()));
         }
     }
 
     /**
      * Returns all right levels the operating system offers. The right level defines what a user can or cannot do.
      * 
      * @return All right levels the operating system offers.
      */
     public List<RightLevel> getRightLevels() {
 
         return Collections.unmodifiableList(rightLevels);
     }
 
     /**
     * This class represents a final right level a user can have on an operating system.
      * The right level defines what a user can or cannot do. If a user has a right level, he can use every other right level below his one.
      * Every operating system has its own right levels, and programs define the right level you need for executing it.
      * 
      * @see OperatingSystem
      * @see Program
      */
     public static class RightLevel implements Serializable {
 
         private static final long serialVersionUID = 1L;
 
         private String            name;
 
         /**
          * Creates a new right level and sets the name.
          * 
          * @param name The name for the right level.
          */
         public RightLevel(String name) {
 
             this.name = name;
         }
 
         /**
          * Returns the name of the right level. The name is used by programs for defining who can execute the software.
          * 
          * @return The name of the right level.
          */
         public String getName() {
 
             return name;
         }
 
     }
 
 }
