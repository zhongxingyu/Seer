 /*
  * GuiAbstrSingleEntryPanel.java
  * 
  * Copyright (C) 2009 Nicola Roberto Viganò
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 /*
  * GuiAbstrSingleEntryPanel.java
  *
  * Created on 23-mag-2009, 14.48.59
  */
 
 package gestionecassa.clients.cassa.gui;
 
 /**
  *
  * @author ben
  */
 abstract public class GuiAbstrSingleEntryPanel extends javax.swing.JPanel
         implements Comparable<GuiAbstrSingleEntryPanel> {
 
     /**
      * 
      */
     public void clean() { }
 
     /**
      *
      * @return
      */
     abstract public int getNumTot();
 
    @Override
     public int compareTo(GuiAbstrSingleEntryPanel o) {
         return (this.hashCode() - o.hashCode());
     }
 }
