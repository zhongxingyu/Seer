 /* BoxDialog.java
  *
  * Created on Sunday May 31, 2010, 8:58 PM
  *
  * This file is a part of Shoddy Battle.
  * Copyright (C) 2009  Catherine Fitzpatrick and Benjamin Gwin
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Affero General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program; if not, visit the Free Software Foundation, Inc.
  * online at http://gnu.org.
  */
 
 package shoddybattleclient;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import javax.swing.AbstractListModel;
 import javax.swing.JOptionPane;
 import javax.swing.event.ListDataEvent;
 import javax.swing.event.ListDataListener;
 import shoddybattleclient.shoddybattle.Pokemon;
 import shoddybattleclient.utils.TeamFileParser;
 
 /**
  * A dialog to manage pokemon and boxes.
  * Files are case insensitive for best cross-platform effects.
  * @author Carlos
  */
 public class BoxDialog extends javax.swing.JDialog {
 
     //This is needed for proper list display
     private class PokemonWrapper {
         public String name;
         public Pokemon pokemon;
         public PokemonWrapper(String pokemonName, Pokemon poke) {
             name = pokemonName;
             pokemon = poke;
         }
         @Override
         public String toString() {
             return name;
         }
     }
 
     private class CaseInsensitiveComparator implements Comparator<String> {
 
         @Override
         public int compare(String o1, String o2) {
             return o1.compareToIgnoreCase(o2);
         }
     }
 
     //TODO: Consider this box with BoxTreeModel.Box
     //Calling methods on this Box modifies the hard drive
     private class Box implements Comparable<Box> {
 
         private String m_name;
         private Map<String, Pokemon> m_pokemon;
 
         public Box(String name) {
             m_name = name;
             m_pokemon = new TreeMap<String, Pokemon>(new CaseInsensitiveComparator());
            
            //Create the box folder if it doesn't exist
            File boxFolder = new File(Preference.getBoxLocation());
            if(!boxFolder.exists())
                boxFolder.mkdir();

            //Create the box itself
             File boxDir = new File(getBoxPath());
             if(!boxDir.exists())
                 boxDir.mkdir();
 
             //Read in all the pokemon in this box
             for(File pokeFile : boxDir.listFiles()) {
                 if(pokeFile.isDirectory())
                     continue;
                 try {
                     TeamFileParser tfp = new TeamFileParser();
                     Pokemon poke = tfp.parseTeam(pokeFile.getAbsolutePath())[0];
                     m_pokemon.put(pokeFile.getName(), poke);
                 }
                 catch(Exception ex) {}
             }
         }
 
         //This creates a new file if it doesn't exist
         public void addPokemon(String name, Pokemon pokemon) throws IOException {
             StringBuffer buf = new StringBuffer();
             buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
             buf.append(pokemon.toXML());
 
             //The file must be removed if the "case" changes
             PokemonWrapper wrapper = getPokemonWrapper(name);
             if(wrapper != null) {
                 new File(getBoxPath() + "/" + wrapper.name).delete();
                 m_pokemon.remove(name);
             }
             
             File pokemonPath = new File(getBoxPath() + "/" + name);
             Writer writer = new PrintWriter(new FileWriter(pokemonPath));
             writer.write(new String(buf));
             writer.flush();
             writer.close();
 
             m_pokemon.put(name, pokemon);
         }
 
         public PokemonWrapper getPokemonAt(int idx) {
             Iterator<Map.Entry<String, Pokemon>> iter = m_pokemon.entrySet().iterator();
             for(int i = 0; i < idx; i++)
                 iter.next();
             Map.Entry<String, Pokemon> entry = iter.next();
             return new PokemonWrapper(entry.getKey(), entry.getValue());
         }
 
         public Pokemon getPokemon(String name) {
             return m_pokemon.get(name);
         }
 
         //This has a use if we want a case-sensitive name
         public PokemonWrapper getPokemonWrapper(String name) {
             for(Map.Entry<String, Pokemon> entry : m_pokemon.entrySet()) {
                 if(entry.getKey().equalsIgnoreCase(name))
                     return new PokemonWrapper(entry.getKey(), entry.getValue());
             }
             return null;
         }
 
         public int getPokemonCount() {
             return m_pokemon.size();
         }
 
         public String getName() {
             return m_name;
         }
 
         public String getBoxPath() {
             return Preference.getBoxLocation() + File.separatorChar + getName();
         }
 
         @Override
         public int compareTo(Box o) {
             return getName().compareToIgnoreCase(o.getName());
         }
 
         @Override
         public String toString() {
             return getName();
         }
     }
 
     private class BoxListModel extends AbstractListModel {
 
         Set<Box> m_boxes = new TreeSet<Box>();
 
         public void addBox(Box box) {
             m_boxes.add(box);
 
             //Let the list know that we added a box
             ListDataEvent evt = new ListDataEvent(box,
                     ListDataEvent.CONTENTS_CHANGED, 0, m_boxes.size());
             for (ListDataListener listener : getListDataListeners()) {
                 listener.contentsChanged(evt);
             }
         }
 
         public Box getBox(String name) {
             //We could make this O(logn) with a custom Box comparator
             //But the speed benefit isn't worth it here
             for(Box box : m_boxes) {
                 if(box.getName().equalsIgnoreCase(name))
                     return box;
             }
             return null;
         }
 
         @Override
         public Object getElementAt(int index) {
             //Sets can't be accessed random access, so we must iterate
             Iterator<Box> iter = m_boxes.iterator();
             for(int i = 0; i < index; i++)
                 iter.next();
             return iter.next();
         }
 
         @Override
         public int getSize() {
             return m_boxes.size();
         }
     }
 
     private class PokemonListModel extends AbstractListModel {
 
         Box owner;
 
         public void setBox(Box box) {
             owner = box;
             fireListChanged();
         }
 
         public void fireListChanged() {
             ListDataEvent evt = new ListDataEvent(owner,
                     ListDataEvent.CONTENTS_CHANGED, 0, getSize());
             for (ListDataListener listener : getListDataListeners()) {
                 listener.contentsChanged(evt);
             }
         }
 
         @Override
         public Object getElementAt(int index) {
             return owner.getPokemonAt(index);
         }
 
         @Override
         public int getSize() {
             if(owner == null)
                 return 0;
             return owner.getPokemonCount();
         }
     }
 
     private BoxListModel boxModel;
     private PokemonListModel pokemonModel;
     private TeamBuilder m_parent;
 
     /** Creates new form BoxDialog */
     public BoxDialog(TeamBuilder parent) {
         initComponents();
         m_parent = parent;
 
         pokemonModel = new PokemonListModel();
         listPokemon.setModel(pokemonModel);
 
         //Load boxes
         File boxDir = new File(Preference.getBoxLocation());
         boxModel = new BoxListModel();
         if(boxDir.exists()) {
             for(File boxFile : boxDir.listFiles()) {
                 if(!boxFile.isDirectory())
                     continue;
                 boxModel.addBox(new Box(boxFile.getName()));
             }
         }
         listBoxes.setModel(boxModel);
 
         setVisible(true);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jScrollPane1 = new javax.swing.JScrollPane();
         listBoxes = new javax.swing.JList();
         jScrollPane2 = new javax.swing.JScrollPane();
         listPokemon = new javax.swing.JList();
         btnNewBox = new javax.swing.JButton();
         btnImport = new javax.swing.JButton();
         btnExport = new javax.swing.JButton();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
 
         listBoxes.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
             public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                 listBoxesValueChanged(evt);
             }
         });
         jScrollPane1.setViewportView(listBoxes);
 
         jScrollPane2.setViewportView(listPokemon);
 
         btnNewBox.setText("New Box");
         btnNewBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnNewBoxActionPerformed(evt);
             }
         });
 
         btnImport.setText("Import");
         btnImport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnImportActionPerformed(evt);
             }
         });
 
         btnExport.setText("Export");
         btnExport.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnExportActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(btnNewBox))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(btnImport)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(btnExport))
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 146, Short.MAX_VALUE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 211, Short.MAX_VALUE)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(btnNewBox)
                     .addComponent(btnImport)
                     .addComponent(btnExport))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void btnNewBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewBoxActionPerformed
         String boxName = JOptionPane.showInputDialog(this, "New box name:");
 
         if(boxName == null)
             return;
         if(boxModel.getBox(boxName) != null) {
             JOptionPane.showMessageDialog(this, "This box already exists", "Error",
                     JOptionPane.ERROR_MESSAGE);
             return;
         }
 
         try {
             boxModel.addBox(new Box(boxName));
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Error making box", "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnNewBoxActionPerformed
 
     private void btnImportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnImportActionPerformed
         Box box = (Box)listBoxes.getSelectedValue();
 
         if(box == null)
             return;
         
         String name = JOptionPane.showInputDialog(this, "New Pokemon's name:");
 
         if(name == null || name.trim().equals(""))
             return;
 
         if(box.getPokemon(name) != null) {
             int confirm = JOptionPane.showConfirmDialog(this, "This Pokemon already exists, are " +
                     "you sure you want to replace it?", "", JOptionPane.YES_NO_OPTION);
             if(confirm != JOptionPane.YES_OPTION)
                 return;
         }
 
         try {
             box.addPokemon(name.trim(), m_parent.getSelectedPokemon());
             pokemonModel.fireListChanged();
         } catch(SecurityException ex) {
             JOptionPane.showMessageDialog(this, "Permission denied", "Error",
                     JOptionPane.ERROR_MESSAGE);
         } catch (Exception ex) {
             JOptionPane.showMessageDialog(this, "Error adding pokemon", "Error",
                     JOptionPane.ERROR_MESSAGE);
         }
     }//GEN-LAST:event_btnImportActionPerformed
 
     private void listBoxesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listBoxesValueChanged
         Box box = (Box)listBoxes.getSelectedValue();
         pokemonModel.setBox(box);
     }//GEN-LAST:event_listBoxesValueChanged
 
     private void btnExportActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnExportActionPerformed
         PokemonWrapper wrapper = (PokemonWrapper)listPokemon.getSelectedValue();
 
         if(wrapper == null)
             return;
 
         m_parent.setSelectedPokemon(wrapper.pokemon);
     }//GEN-LAST:event_btnExportActionPerformed
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnExport;
     private javax.swing.JButton btnImport;
     private javax.swing.JButton btnNewBox;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JList listBoxes;
     private javax.swing.JList listPokemon;
     // End of variables declaration//GEN-END:variables
 
 }
