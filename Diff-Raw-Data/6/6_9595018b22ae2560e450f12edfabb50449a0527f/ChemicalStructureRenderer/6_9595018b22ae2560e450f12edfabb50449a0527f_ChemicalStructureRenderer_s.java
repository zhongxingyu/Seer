 /**
  * ChemicalStructureRenderer.java
  *
  * 2011.09.29
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
 package uk.ac.ebi.chemet.render.table.renderers;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Rectangle;
 import java.util.Arrays;
 import java.util.Collection;
 import javax.swing.ImageIcon;
 import javax.swing.JTable;
 import org.openscience.cdk.exception.CDKException;
 import uk.ac.ebi.annotation.chemical.ChemicalStructure;
 import uk.ac.ebi.render.molecule.MoleculeRenderer;
 
 /**
  *          ChemicalStructureRenderer – 2011.09.29 <br>
  *          
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  */
 public class ChemicalStructureRenderer
         extends DefaultRenderer {
 
     public ChemicalStructureRenderer() {
     }
 
     @Override
     public Component getTableCellRendererComponent(JTable table,
                                                    Object value,
                                                    boolean isSelected,
                                                    boolean hasFocus,
                                                    int row,
                                                    int column) {
 
         Collection<ChemicalStructure> collection = value instanceof Collection ? (Collection) value : Arrays.asList(
                 value);
 
         if (table.getColumnModel().getColumn(column).getPreferredWidth() != table.getRowHeight(row)) {
             table.setRowHeight(row, table.getColumnModel().getColumn(column).getPreferredWidth());
         }
 
         if (collection.iterator().hasNext()) {
             try {
                 ChemicalStructure structure = collection.iterator().next();
                this.setBackground(isSelected ? table.getSelectionBackground() : table.getSelectionForeground());
                 this.setIcon(new ImageIcon(
                         MoleculeRenderer.getInstance().getImage(structure.getMolecule(),
                                                                 new Rectangle(0, 0,
                                                                               table.getRowHeight(row),
                                                                               table.getRowHeight(row)))));
             } catch (CDKException ex) {
                 System.err.println("Unable to render molecule: " + ex.getMessage());
             }
 
         } else {
             this.setIcon(null);
         }
 
 
         return this;
 
     }
 }
