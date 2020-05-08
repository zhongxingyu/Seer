 /**
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * © Copyright 2013, Gardet Julien, Droy Yann, Araujo Auxence.
  * 
  * The logo in edu.cubesta.ressources.favicon.png is a derivate work from
  * <http://commons.wikimedia.org/w/index.php?title=File:Rubik%27s_cube.svg&oldid=70000649>.
  * 
  * Other legal notices on <http://cubesta-project.github.io/CubeSTA/legals.html>.
  */
 /* Project : CubeSTA
  * Location : edu.cubesta.help
  * Class : MoveHelpPanel.java
  */
 
 package edu.cubesta.help;
 
 import edu.cubesta.ressources.L10n;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import javax.swing.JPanel;
 
 /**
  * Permet de gérer le contenu de la boîte de dialogue d'aide des touches
  * @author julien.gardet
  */
 
 public class MoveHelpPanel extends JPanel {
     
     /**
      * Permet de créer le contenu de la boîte de dialogue d'aide des mouvements
      * @param g 
      */
     
     @Override
     public void paintComponent(Graphics g){
         this.setBackground(Color.WHITE);
        g.setColor(Color.WHITE);
        g.fillRect(0,0,this.getWidth(),this.getHeight());
         Font font = new Font(Font.SANS_SERIF, Font.BOLD, 20);
         g.setColor(Color.black);
         g.setFont(font);
         g.drawString("\u2328 " + L10n.getLanguage(16), 10, 25);
         font = new Font(Font.SANS_SERIF, 0, 12);
         g.setFont(font);
         for(int i = 14; i <= 22; i++){
             g.drawString(L10n.getLanguageHelp(i), 10, 50+20*(i-13));
         }
     }   
 }
