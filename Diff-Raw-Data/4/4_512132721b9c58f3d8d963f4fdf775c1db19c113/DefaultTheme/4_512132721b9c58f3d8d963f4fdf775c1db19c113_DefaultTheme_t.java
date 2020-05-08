 /**
  * DefaultTheme.java
  *
  * 2011.09.30
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
 package uk.ac.ebi.mnb.view.theme;
 
 import javax.swing.Icon;
 import java.awt.Color;
 import java.awt.Font;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.interfaces.Theme;
 import uk.ac.ebi.visualisation.ViewUtils;
 
 /**
  *          DefaultTheme – 2011.09.30 <br>
  *          Class description
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 public class DefaultTheme implements Theme {
 
     private static final Logger LOGGER = Logger.getLogger(DefaultTheme.class);
     private Color bg = new Color(237, 237, 237);
    private Icon plus =  ViewUtils.getIcon("images/plus_12x12.png");
    private Icon minus =  ViewUtils.getIcon("images/minus_12x12.png");
 
     public Color getWarningForeground() {
         return Color.RED;
     }
 
     public Color getForeground() {
         return Color.DARK_GRAY;
     }
 
     public Color getEmphasisedForeground() {
         return Color.BLACK;
     }
 
 
 
     public Color getAltForeground() {
         return Color.DARK_GRAY;
     }
 
     public Color getBackground() {
         return Color.WHITE;
     }
 
     public Color getDialogBackground() {
         return bg;
     }
 
     public Font getBodyFont() {
         return ViewUtils.DEFAULT_BODY_FONT;
     }
 
     public Font getHeaderFont() {
         return ViewUtils.DEFAULT_HEADER_FONT;
     }
 
     public Font getLinkFont(){
         return ViewUtils.DEFAULT_LINK_FONT;
     }
 
     
 
     public float getDialogOpacity(){
         return 0.95f;
     }
 
     public Icon getPlusIcon() {
         return plus;
     }
 
     public Icon getMinusIcon() {
         return minus;
     }
 }
