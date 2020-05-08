 package uk.ac.ebi.chemet.visualisation;
 

 /**
  * AbstractAlignmentRenderer.java
  *
  * 2011.07.14
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 import org.apache.log4j.Logger;
 
 /**
  * @name    AbstractAlignmentRenderer
  * @date    2011.07.14
  * @date    $LastChangedDate$ (this version)
  * @version $Revision$
  * @author  johnmay
  * @author  $Author$ (this version)
  * @brief   ...class description...
  *
  */
 public class AbstractAlignmentRenderer {
 
     private static final Logger LOGGER = Logger.getLogger( AbstractAlignmentRenderer.class );
     // no need to set this once it's been built
     protected final Rectangle outerBounds;
     protected final Rectangle innerBounds;
     protected final AbstractAlignmentColor color;
 
     /**
      * Constructor specifies the outerBounds and colouring scheme
      * @param outerBounds
      * @param color
      */
     public AbstractAlignmentRenderer( Rectangle outerBounds , AbstractAlignmentColor colour , Integer padding ) {
         this.outerBounds = outerBounds;
         this.color = colour;
         // set the inner bounds
         this.innerBounds = new Rectangle( outerBounds.x + padding , outerBounds.y + padding ,
                                          outerBounds.width - padding , outerBounds.height - padding );
 
 
     }
 
     /**
      * @brief  Accessor for the outerBounds of the image to be rendered
      * @return outerBounds as a Rectangle object
      */
     public Rectangle getBounds() {
         return outerBounds;
     }
 
     /**
      * @brief  Accessor for the innerBounds of the image to be rendered. The inner bounds
      *         is the outerBounds subtract the padding
      * @return innerBounds as a Rectangle object
      */
     public Rectangle getInnerBounds() {
         return innerBounds;
     }
 
     /**
      * @brief  Accessor for the alignment color
      * @return A class that inherits from AbstractAlignmentColor interface
      */
     public AbstractAlignmentColor getColour() {
         return color;
     }
 
     /**
      * Instantiates a buffered image to the size and image type specified in
      * the fields
      * @return new BufferedImager object to draw on
      */
     public BufferedImage newBufferedImage() {
         return new BufferedImage( outerBounds.width , outerBounds.height , color.getImageType() );
     }
 }
