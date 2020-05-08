 /*
  * Hex - a hex viewer and annotator
  * Copyright (C) 2009  Trejkaz, Hex Project
  *
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
 
 package org.trypticon.hex.gui;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.UIManager;
 
 /**
  * Utility class for bootstrapping LAF-specific settings.
  *
  * @author trejkaz
  */
 class PLAFBootstrap {
     private static final Logger logger = Logger.getLogger(PLAFBootstrap.class.getName());
 
     static void init() {
         String systemLAF = UIManager.getSystemLookAndFeelClassName();
 
         try {
            if ("apple.laf.AquaLookAndFeel".equals(systemLAF)) {
                 MacPLAFBootstrap.init();
                 return;
             }
 
             // Other special cases go here as needed.
 
         } catch (Exception e) {
             logger.log(Level.WARNING, "Unexpected error initialising custom LAF, falling back to generic", e);
         }
 
         initGeneric();
     }
 
 
     /**
      * Initialises the generic look and feel.
      */
     private static void initGeneric() {
         try {
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
             logger.log(Level.WARNING, "Unexpected error initialising platform LAF, falling back to default", e);
         }
     }
 }
