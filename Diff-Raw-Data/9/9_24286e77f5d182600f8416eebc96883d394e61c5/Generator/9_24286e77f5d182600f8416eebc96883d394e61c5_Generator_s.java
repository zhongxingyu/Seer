 /*
  * Copyright (C) 2012 Gyver
 * 
 * This class is derived from Pixelcontroller github.com/neophob/PixelController
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
 package com.gyver.matrixmover.generator;
 
 import com.gyver.matrixmover.core.MatrixData;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The class Generator, a parent class for all generators.
  * 
  * Code-parts copied from http://github.com/neophob/PixelController
  * 
  * @author Gyver
  */
 public abstract class Generator {
 
     /**
      * The Enum GeneratorName.
      */
     public enum GeneratorName {
 
         /** The NULLGENERATOR. */
         SIMPLE_COLOR_GENERATOR(0),
         /** The COLOR_SCROLL. */
         COLOR_SCROLL(1),
         /** The COLOR_FADE */
         COLOR_FADE(2),
         /** The PLASMA */
         PLASMA(3),
         /** The FIRE */
         FIRE(4),
         /** The TEXTWRITER */
         TEXTWRITER(5), 
         /** The METABALLS */
         METABALLS(6);
         
         /*
          * If you add generators, keep in mind to add a case in 
          * core.GeneratorVisual.setGeneratorFromString(int, String), so that a 
          * change via the gui reaches the Visuals. Without changes, 
          * new Generators are not executed. Additionally a case should 
          * be added in gui.GeneratorSetup.openGeneratorSettingsDialog(String, Generator)
          * to open a Settings dialog.
          */
         public static final String STRING_SIMPLE_COLOR_GENERATOR = "Simple Color";
         public static final String STRING_COLOR_SCROLL = "Color Scroll";
         public static final String STRING_COLOR_FADE = "Color Fade";
         public static final String STRING_PLASMA = "Plasma";
         public static final String STRING_FIRE = "Flames";
         public static final String STRING_TEXTWRITER = "Text";
         public static final String STRING_METABALLS = "Metaballs";
         /** The id. */
         private int id;
 
         /**
          * Instantiates a new generator name.
          *
          * @param id the id
          */
         GeneratorName(int id) {
             this.id = id;
         }
 
         /**
          * Gets the id.
          *
          * @return the id
          */
         public int getId() {
             return id;
         }
 
         /**
          * Returns a human readable string for the Generator
          */
         @Override
         public String toString() {
             switch (this) {
                 case SIMPLE_COLOR_GENERATOR:
                     return STRING_SIMPLE_COLOR_GENERATOR;
                 case COLOR_SCROLL:
                     return STRING_COLOR_SCROLL;
                 case COLOR_FADE:
                     return STRING_COLOR_FADE;
                 case PLASMA:
                     return STRING_PLASMA;
                 case FIRE:
                     return STRING_FIRE;
                 case TEXTWRITER:
                     return STRING_TEXTWRITER;
                 case METABALLS:
                     return STRING_METABALLS;
             }
             // if it has no string, return the enum-string
             return super.toString();
         }
     }
     /** The log. */
     private static final Logger LOG = Logger.getLogger(Generator.class.getName());
     /** The name. */
     private GeneratorName name;
     /** The internal buffer. */
     public int[] internalBuffer;
     /** The internal buffer x size. */
     protected int internalBufferWidth;
     /** The internal buffer y size. */
     protected int internalBufferHeight;
     /** is the generator selected and thus active? */
     protected boolean active;
 
     /**
      * Instantiates a new generator.
      *
      * @param controller the controller
      * @param name the name
      * @param resizeOption the resize option
      */
     public Generator(GeneratorName name, MatrixData matrix) {
         this.name = name;
         this.internalBufferWidth = matrix.getWidth();
         this.internalBufferHeight = matrix.getHeight();
         this.internalBuffer = new int[internalBufferWidth * internalBufferHeight];
 
         LOG.log(Level.FINEST, "Generator: internalBufferSize: {0} name: {1} ", new Object[]{internalBuffer.length, name});
 
         this.active = false;
     }
 
     /**
      * update the generator.
      */
     public abstract void update();
 
     /**
      * deinit generator.
      */
     public void close() {
     }
 
     /**
      * Gets the internal buffer x size.
      *
      * @return the internal buffer x size
      */
     public int getInternalBufferXSize() {
         return internalBufferWidth;
     }
 
     /**
      * Gets the internal buffer y size.
      *
      * @return the internal buffer y size
      */
     public int getInternalBufferYSize() {
         return internalBufferHeight;
     }
 
     /**
      * Gets the internal buffer size.
      *
      * @return the internal buffer size
      */
     public int getInternalBufferSize() {
         return internalBuffer.length;
     }
 
     /**
      * Gets the name.
      *
      * @return the name
      */
     public GeneratorName getName() {
         return name;
     }
 
     /**
      * Gets the buffer.
      *
      * @return the buffer
      */
     public int[] getBuffer() {
         return internalBuffer;
     }
 
     /**
      * this method get called if the generator gets activated
      */
     protected void nowActive() {
     }
 
     /**
      * this method get called if the generator gets inactive
      */
     protected void nowInactive() {
     }
 
     /**
      * Gets the id.
      *
      * @return the id
      */
     public int getId() {
         return this.name.getId();
     }
 
     /**
      * is generator selected?
      * @return
      */
     public boolean isActive() {
         return active;
     }
 
     /**
      * update state
      * @param active
      */
     public void setActive(boolean active) {
         if (!active && this.active) {
             nowInactive();
         } else if (active && !this.active) {
             nowActive();
         }
         this.active = active;
     }
 }
