 /*
  * TeamBuilderForm.java
  *
  * Created on Apr 4, 2009, 4:35:47 PM
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
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 import javax.swing.event.TableModelEvent;
 import javax.swing.event.TableModelListener;
 import shoddybattleclient.shoddybattle.*;
 import shoddybattleclient.shoddybattle.Pokemon.Gender;
 import shoddybattleclient.shoddybattle.PokemonSpecies.IllegalCombo;
 import shoddybattleclient.utils.FilteredDocument;
 import shoddybattleclient.utils.IntegerDocument;
 import shoddybattleclient.utils.MoveTableModel;
 import shoddybattleclient.utils.MoveTableModel.SelectedMoveModel;
 import shoddybattleclient.utils.MoveTableModel.TableRow;
 import shoddybattleclient.utils.JButtonTable;
 /**
  *
  * @author ben
  */
 
 public class TeamBuilderForm extends javax.swing.JPanel {
 
     private class IllegalCheckRenderer extends DefaultListCellRenderer {
         boolean m_illegal[];
         public IllegalCheckRenderer (int nItems) {
             m_illegal = new boolean[nItems];
         }
         public void setIllegal(int index) {
             if (index >= 0) {
                 m_illegal[index] = true;
             }
         }
         public void clear() {
             for (int i = 0; i < m_illegal.length; i++) {
                 m_illegal[i] = false;
             }
         }
         @Override
         public Component getListCellRendererComponent(JList list, Object value,
                 int index, boolean isSelected, boolean cellHasFocus) {
             Component c = super.getListCellRendererComponent(list, value,
                     index, isSelected, cellHasFocus);
             if ((index < 0) || !m_illegal[index]) {
                 c.setForeground(defaultForeground);
             } else {
                 c.setForeground(Color.RED);
             }
             return c;
         }
     }
 
     private TeamBuilder m_parent;
     //The generation the team builder represents
     private Generation m_generation;
     //The current species
     private PokemonSpecies m_species = null;
     //pokemon represented by this form
     private Pokemon m_pokemon = null;
 
     private JTextField[] m_ivs;
     private JTextField[] m_evs;
     private JLabel[] m_bases;
     private JLabel[] m_totals;
     private JLabel[] m_natures;
 
     private JLabel m_evHeader;
 
     private JButtonTable tblMoves;
     private JTable tblSelected;
 
     private Color defaultForeground;
     private IllegalCheckRenderer genderRenderer;
     private IllegalCheckRenderer natureRenderer;
     private IllegalCheckRenderer abilityRenderer;
 
     //hacky solution to hacky problem
     //changing the IVs changes the hiddenpower (itemStateChanged) which changes the IVs
     //use a flag so cmbNatureItemStateChanged knows we don't want it changed
     private boolean hpProgramSelect = false;
 
     private static final Map<String, int[]> m_hiddenPowers = new HashMap<String, int[]>();
     
     static {
         m_hiddenPowers.put("Dark", new int[] { 31, 31, 31, 31, 31, 31 });
         m_hiddenPowers.put("Fire", new int[] { 31, 30, 31, 30, 30, 31 });
         m_hiddenPowers.put("Water", new int[] { 31, 31, 31, 30, 30, 31 });
         m_hiddenPowers.put("Grass", new int[] { 31, 30, 31, 31, 30, 31 });
         m_hiddenPowers.put("Electric", new int[] { 31, 31, 31, 31, 30, 31 });
         m_hiddenPowers.put("Ice", new int[] { 31, 30, 30, 31, 31, 31 });
         m_hiddenPowers.put("Fighting", new int[] { 31, 31, 30, 30, 30, 30 });
         m_hiddenPowers.put("Poison", new int[] { 31, 31, 30, 31, 30, 30 });
         m_hiddenPowers.put("Ground", new int[] { 31, 31, 31, 31, 30, 30 });
         m_hiddenPowers.put("Flying", new int[] { 31, 31, 31, 30, 30, 30 });
         m_hiddenPowers.put("Psychic", new int[] { 31, 30, 31, 30, 31, 31 });
         m_hiddenPowers.put("Bug", new int[] { 31, 31, 31, 30, 31, 30 });
         m_hiddenPowers.put("Rock", new int[] { 31, 31, 30, 30, 31, 30 });
         m_hiddenPowers.put("Ghost", new int[] { 31, 31, 30, 31, 31, 30 });
         m_hiddenPowers.put("Dragon", new int[] { 31, 30, 31, 31, 31, 31 });
         m_hiddenPowers.put("Steel", new int[] { 31, 31, 31, 31, 31, 30 });
     }
 
     /** Creates new form TeamBuilderForm */
     public TeamBuilderForm(TeamBuilder parent) {
         initComponents();
         m_parent = parent;
         m_generation = parent.getGeneration();
         
         tblMoves = new JButtonTable();
         tblMoves.setModel(new MoveTableModel(null, new String[0], this));
         tblMoves.setRowSelectionAllowed(false);
         tblMoves.setColumnSelectionAllowed(false);
         scrollMoves.add(tblMoves);
         scrollMoves.setViewportView(tblMoves);
         tblSelected = new JButtonTable();
         tblSelected.setModel(new SelectedMoveModel(this));
         tblSelected.getColumnModel().getColumn(0).setPreferredWidth(80);
         tblSelected.getColumnModel().getColumn(1).setPreferredWidth(100);
         tblSelected.getColumnModel().getColumn(2).setPreferredWidth(170);
         tblSelected.setRowSelectionAllowed(false);
         tblSelected.setColumnSelectionAllowed(false);
         scrollSelected.add(tblSelected);
         scrollSelected.setViewportView(tblSelected);
 
         Insets insets = scrollSelected.getInsets();
         splitPane.setDividerLocation(
                 tblSelected.getTableHeader().getPreferredSize().height
                 + (tblSelected.getRowHeight() * 4)
                 + insets.top + insets.bottom + 1);
 
         String[] items = new String[m_generation.getItems().size() + 1];
         items[0] = "No Item";
         for (int i = 0; i < m_generation.getItems().size(); i++)
             items[i+1] = m_generation.getItems().get(i);
 
         cmbNature.setModel(new DefaultComboBoxModel(PokemonNature.getNatures()));
         natureRenderer =
                 new IllegalCheckRenderer(PokemonNature.getNatures().length);
         cmbNature.setRenderer(natureRenderer);
         cmbItem.setModel(new DefaultComboBoxModel(items));
         genderRenderer = new IllegalCheckRenderer(2);
         cmbGender.setRenderer(genderRenderer);
 
         setupStats();
 
         cmbNatureItemStateChanged(null);
 
         // Illegal move checking
         defaultForeground = tblSelected.getForeground();
         tblSelected.getModel().addTableModelListener(new TableModelListener() {
             public void tableChanged(TableModelEvent e) {
                 checkIllegalMovesets();
             }
         });
     }
 
     private void setupStats() {
         m_ivs = new JTextField[6];
         m_evs = new JTextField[6];
         m_bases = new JLabel[6];
         m_totals = new JLabel[6];
         m_natures = new JLabel[6];
         JLabel stat = new JLabel("Stat");
         Font f = stat.getFont().deriveFont(11.0f).deriveFont(Font.BOLD);
         stat.setFont(f);
         JLabel baseHeader = new JLabel("Base");
         baseHeader.setFont(f);
         JLabel totalHeader = new JLabel("Total");
         totalHeader.setFont(f);
         JLabel ivHeader = new JLabel("IVs");
         ivHeader.setFont(f);
         m_evHeader = new JLabel("EVs");
         m_evHeader.setFont(f);
         panelStats.add(stat); panelStats.add(totalHeader); panelStats.add(baseHeader);
         panelStats.add(ivHeader); panelStats.add(m_evHeader);
         for (int i = 0; i < m_ivs.length; i++) {
             JTextField iv = new JTextField(""+31);
             iv.setFont(iv.getFont().deriveFont(11.0f));
             iv.setSize(new Dimension(0, 0));
             iv.setPreferredSize(new Dimension(0, 0));
             iv.setMaximumSize(new Dimension(0, 0));
             iv.setDocument(new IntegerDocument(31, iv));
             m_ivs[i] = iv;
             JTextField ev = new JTextField("0");
             ev.setFont(iv.getFont());
             ev.setSize(new Dimension(0, 0));
             ev.setPreferredSize(new Dimension(0, 0));
             ev.setMaximumSize(new Dimension(0, 0));
             ev.setDocument(new IntegerDocument(0, 255, 0, ev));
             m_evs[i] = ev;
             JLabel base = new JLabel("000");
             base.setFont(iv.getFont());
             m_bases[i] = base;
             JLabel total = new JLabel("000");
             total.setFont(iv.getFont());
             m_totals[i] = total;
             JLabel name = new JLabel(Pokemon.getStatName(i));
             name.setFont(iv.getFont());
             JLabel nature = new JLabel();
             nature.setFont(iv.getFont());
             m_natures[i] = nature;
             panelStats.add(name);
             panelStats.add(m_totals[i]);
             panelStats.add(m_bases[i]);
             panelStats.add(m_ivs[i]);
             panelStats.add(m_evs[i]);
             //panelStats.add(nature);
         }
 
         for (int i = 0; i < m_evs.length; i++) {
             final int index = i;
             KeyListener evListener = new KeyListener() {
                 //Linux has a "bug" where holding down a key fires many keyReleased
                 //This attempts to soften that bug, and shouldn't affect Mac/Windows
                 //Stopping it at actionPerformed gives keyPressed some time to save itself
                 private boolean m_increasing = false;
                 private JTextField m_caller = null;
                 boolean stopped = false;
                 private Timer m_timer = new Timer(200, new ActionListener() {
                     public void actionPerformed(ActionEvent e) {
                         if(stopped) {
                             m_timer.stop();
                             return;
                         }
                         updateNumber();
                     }
                 });
                 public void keyTyped(KeyEvent e) {}
                 public void keyPressed(KeyEvent e) {
                     m_caller = (JTextField)e.getSource();
                     if (e.getKeyCode() == KeyEvent.VK_UP) {
                         m_increasing = true;
                         if (stopped) updateNumber();
                         m_timer.start();
                         stopped = false;
                     } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                         m_increasing = false;
                         if (stopped) updateNumber();
                         m_timer.start();
                         stopped = false;
                     }
                 }
                 public void keyReleased(KeyEvent e) {
                     stopped = true;
                     updateStat(index);
                 }
                 private void updateNumber() {
                     try {
                         int current = Integer.parseInt(m_caller.getText());
                         current += m_increasing ? 4 : -4;
                         if (current < 0) current = 0;
                         else if (current > 255) current = 255;
                         m_caller.setText(String.valueOf(current));
                         updateStat(index);
                     } catch (NumberFormatException ex) {    }
                 }
             };
             KeyListener ivListener = new KeyListener() {
                 public void keyTyped(KeyEvent e) { }
                 public void keyPressed(KeyEvent e) { }
                 public void keyReleased(KeyEvent e) {
                     updateStat(index);
                     updateHiddenPower();
                 }
             };
             FocusListener releaseListener = new FocusListener() {
                 public void focusLost(FocusEvent evt) {
                     Component c = (Component)evt.getSource();
                     for (KeyListener listener : c.getKeyListeners()) {
                         listener.keyReleased(null);
                     }
                 }
                 public void focusGained(FocusEvent evt) {}
             };
             m_ivs[i].addKeyListener(ivListener);
             m_evs[i].addKeyListener(evListener);
             m_ivs[i].addFocusListener(releaseListener);
             m_evs[i].addFocusListener(releaseListener);
         }
     }
 
     /**
      * Set the Pokemon shown by this form
      * @param p The pokemon to set
      * @param loading whether we are loading from a team or selected from the list
      */
     public void setPokemon(Pokemon p, boolean loading) {
         m_pokemon = p;
         m_species = m_parent.getSpecies(p.species);
         Gender g = m_species.getGenders();
         if (g.equals(Gender.GENDER_MALE)) {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {Gender.GENDER_MALE}));
             cmbGender.setSelectedIndex(0);
         } else if (g.equals(Gender.GENDER_FEMALE)) {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {Gender.GENDER_FEMALE}));
         } else if (g.equals(Gender.GENDER_BOTH)) {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {
                 Gender.GENDER_MALE,
                 Gender.GENDER_FEMALE
             }));
             cmbGender.setSelectedIndex(0);
         } else {
             cmbGender.setModel(new DefaultComboBoxModel(new Gender[] {Gender.GENDER_NONE}));
             cmbGender.setSelectedIndex(0);
         }
         cmbGender.setEnabled(!g.equals(Gender.GENDER_NONE));
 
         txtNickname.setText(p.nickname);
         txtLevel.setText(String.valueOf(p.level));
         chkShiny.setSelected(p.shiny);
         cmbAbility.setModel(new DefaultComboBoxModel(m_species.getAbilities()));
         abilityRenderer = 
                 new IllegalCheckRenderer(m_species.getAbilities().length);
         cmbAbility.setRenderer(abilityRenderer);
         cmbAbility.setSelectedItem(p.ability);
         if (g.equals(Gender.GENDER_BOTH)) {
             cmbGender.setSelectedIndex(p.gender.ordinal());
         }
         for (int i = 0; i < Pokemon.STAT_COUNT; i++) {
             m_ivs[i].setText(String.valueOf(p.ivs[i]));
             m_evs[i].setText(String.valueOf(p.evs[i]));
         }
         for (int i = 0; i < Pokemon.STAT_COUNT; i++) {
             m_bases[i].setText(String.valueOf(m_species.getBase(i)));
             m_totals[i].setText(String.valueOf(calculateStat(i)));
         }
 
         if (loading) {
             cmbItem.setSelectedItem(p.item);
             cmbNature.setSelectedItem(PokemonNature.getNature(p.nature));
 
             if ("".equals(p.nature)) {
                 cmbNature.setSelectedIndex(0);
             }
         }
         
         ((SelectedMoveModel)tblSelected.getModel()).clear();
         MoveTableModel mtm = new MoveTableModel(m_generation.getMoves(),
                 m_species.getMoves(), this);
         mtm.selectMoves(p.moves, p.ppUps);
         tblMoves.setModel(mtm);
         //name column should be wider
         tblMoves.getColumnModel().getColumn(0).setPreferredWidth(80);
         tblMoves.getColumnModel().getColumn(1).setPreferredWidth(160);
 
         updateEvs();
        updateHiddenPower();
     }
 
     public int calculateStat(int i)  {
         PokemonNature n = (PokemonNature)cmbNature.getSelectedItem();
         return Pokemon.calculateStat(m_pokemon, i, m_species, n);
     }
 
     private void updateEvs() {
         int left = 510;
         for (int i = 0; i < m_evs.length; i++) {
             left -= m_pokemon.evs[i];
         }
         Color c = null;
         if (left < 0) {
             m_evHeader.setToolTipText("<html>Pokemon have a limit of 510 total "
                     + "EVs,<br>this pokemon has too many.");
             c = Color.RED;
         } else {
             m_evHeader.setToolTipText(null);
             c = Color.BLACK;
         }
         m_evHeader.setText(String.valueOf(left));
         m_evHeader.setForeground(c);
     }
     
     private void updateStat(int idx) {
         if (m_pokemon == null) return;
         if ((idx < 0) || (idx > m_totals.length)) return;
         try {
             m_pokemon.ivs[idx] = Integer.parseInt(m_ivs[idx].getText());
         } catch (NumberFormatException e) {
             m_pokemon.ivs[idx] = 0;
         }
         try {
             m_pokemon.evs[idx] = Integer.parseInt(m_evs[idx].getText());
         } catch (NumberFormatException e) {
             m_pokemon.evs[idx] = 0;
         }
         m_totals[idx].setText(String.valueOf(calculateStat(idx)));
 
         updateEvs();
     }
 
     private void updateHiddenPower() {
         hpProgramSelect = true;
         txtHiddenPower.setText(String.valueOf(
                 PokemonMove.calculateHiddenPowerPower(m_pokemon.ivs)));
        cmbHiddenPower.setSelectedItem(
                PokemonMove.getHiddenPowerType(m_pokemon.ivs));
         hpProgramSelect = false;
     }
 
     public Pokemon getPokemon() {
         if (m_pokemon == null) return null;
         
         m_pokemon.nickname = txtNickname.getText();
         try {
             m_pokemon.level = Integer.parseInt(txtLevel.getText());
         } catch (NumberFormatException e) {
             m_pokemon.level = 100;
         }
         m_pokemon.shiny = chkShiny.isSelected();
         m_pokemon.gender = (Gender)cmbGender.getSelectedItem();
         m_pokemon.nature = ((PokemonNature)cmbNature.getSelectedItem()).getName();
         m_pokemon.item = (String)cmbItem.getSelectedItem();
         m_pokemon.ability = (String)cmbAbility.getSelectedItem();
         SelectedMoveModel m = (SelectedMoveModel)tblSelected.getModel();
         m_pokemon.moves = m.getMoves();
         m_pokemon.ppUps = m.getPpUps();
         return m_pokemon;
     }
 
     public void setHappiness(int happy) {
         m_pokemon.happiness = happy;
     }
 
     public int getHappiness() {
         return m_pokemon.happiness;
     }
 
     // returns if the move was successfully added
     public boolean moveSelected(TableRow row) {
         boolean success;
         if (row.isSelected()) {
             success = ((MoveTableModel)tblMoves.getModel()).addMove(row);
         } else {
             success = ((SelectedMoveModel)tblSelected.getModel()).addMove(row);
             if (success) {
                 //Set the happiness to the max value if return or frustration is picked
                 //todo: translation?
                 String move = row.getMove();
                 if (move.equalsIgnoreCase("Return")) {
                     m_pokemon.happiness = 255;
                 } else if (move.equalsIgnoreCase("Frustration")) {
                     m_pokemon.happiness = 0;
                 }
             }
         }
         return success;
     }
 
     private void checkIllegalMovesets() {
         if (tblSelected.getRowCount() == 0) {
             // This is necessary or the below call to getPokemon()
             // will wipe out moves midloading
             return;
         }
 
         natureRenderer.clear();
         abilityRenderer.clear();
         cmbNature.setForeground(defaultForeground);
         cmbAbility.setForeground(defaultForeground);
         cmbGender.setForeground(defaultForeground);
 
         Pokemon p = getPokemon();
         List<IllegalCombo> illegalMovesets =
                 p.getViolatedMovesets(m_generation);
         List<IllegalCombo> illegal = new ArrayList<IllegalCombo>();
         for (IllegalCombo combo : illegalMovesets) {
             if (combo.getAbility() != null) {
                 abilityRenderer.setIllegal(Arrays.asList(
                         m_species.getAbilities()).indexOf(combo.getAbility()));
             }
             if (combo.getNature() != null) {
                 natureRenderer.setIllegal(
                         Arrays.asList(PokemonNature.getNatureNames()).indexOf(
                         combo.getNature()));
             }
             if (combo.getGender() != null) {
                 genderRenderer.setIllegal(combo.getGender().getValue() - 1);
             }
 
             // Now check if we actually violated this combination
             if ((combo.getNature() != null) &&
                     !combo.getNature().equals(p.nature)) {
                 continue;
             }
             if ((combo.getAbility() != null) &&
                     !combo.getAbility().equals(p.ability)) {
                 continue;
             }
             if ((combo.getGender() != null) &&
                     (p.gender != combo.getGender())) {
                 continue;
             }
             illegal.add(combo);
         }
 
         cmbNature.repaint();
         cmbAbility.repaint();
         cmbGender.repaint();
 
         if (!illegal.isEmpty()) {
             StringBuilder buf = new StringBuilder();
             buf.append("<html>Illegal combinations:<br>");
             for (IllegalCombo combo : illegal) {
                 buf.append("- ");
                 buf.append(combo);
                 buf.append("<br>");
 
                 if (combo.getNature() != null) {
                     cmbNature.setForeground(Color.RED);
                 }
                 if (combo.getAbility() != null) {
                     cmbAbility.setForeground(Color.RED);
                 }
                 if (combo.getGender() != null) {
                     cmbGender.setForeground(Color.RED);
                 }
             }
             tblSelected.setToolTipText(buf.toString());
             tblSelected.setForeground(Color.RED);
         } else if (illegal.isEmpty()) {
             tblSelected.setToolTipText(null);
             tblSelected.setForeground(defaultForeground);
         }
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         txtNickname = new javax.swing.JTextField();
         jLabel24 = new javax.swing.JLabel();
         txtLevel = new javax.swing.JTextField();
         cmbGender = new javax.swing.JComboBox();
         chkShiny = new javax.swing.JCheckBox();
         panelStats = new javax.swing.JPanel();
         cmbItem = new javax.swing.JComboBox();
         jLabel5 = new javax.swing.JLabel();
         jLabel26 = new javax.swing.JLabel();
         jLabel27 = new javax.swing.JLabel();
         cmbNature = new javax.swing.JComboBox();
         cmbAbility = new javax.swing.JComboBox();
         jLabel25 = new javax.swing.JLabel();
         txtHiddenPower = new javax.swing.JTextField();
         cmbHiddenPower = new javax.swing.JComboBox();
         splitPane = new javax.swing.JSplitPane();
         scrollSelected = new javax.swing.JScrollPane();
         scrollMoves = new javax.swing.JScrollPane();
 
         setOpaque(false);
 
         txtNickname.setDocument(new FilteredDocument(15, "[<>]+"));
 
         jLabel24.setText("Level:");
 
         txtLevel.setDocument(new IntegerDocument(1, 100, txtLevel));
         txtLevel.setText("100");
         txtLevel.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtLevelKeyReleased(evt);
             }
         });
 
         cmbGender.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbGenderItemStateChanged(evt);
             }
         });
 
         chkShiny.setText("Shiny?");
         chkShiny.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
         chkShiny.setOpaque(false);
         chkShiny.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 chkShinyActionPerformed(evt);
             }
         });
 
         panelStats.setOpaque(false);
         panelStats.setLayout(new java.awt.GridLayout(7, 5));
 
         cmbItem.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Item", "Leftovers" }));
 
         jLabel5.setText("Nickname:");
 
         jLabel26.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         jLabel26.setText("Nature:");
 
         jLabel27.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         jLabel27.setText("Ability:");
 
         cmbNature.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         cmbNature.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Jolly", "Hasty" }));
         cmbNature.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbNatureItemStateChanged(evt);
             }
         });
 
         cmbAbility.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         cmbAbility.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Overgrow", "Truant" }));
         cmbAbility.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbAbilityItemStateChanged(evt);
             }
         });
 
         jLabel25.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         jLabel25.setText("Hidden Power:");
 
         txtHiddenPower.setEditable(false);
         txtHiddenPower.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         txtHiddenPower.setText("70");
 
         cmbHiddenPower.setFont(new java.awt.Font("Lucida Grande", 0, 11));
         cmbHiddenPower.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Bug", "Dark", "Dragon", "Electric", "Fighting", "Fire", "Flying", "Ghost", "Grass", "Ground", "Ice", "Poison", "Psychic", "Rock", "Steel", "Water" }));
         cmbHiddenPower.setSelectedIndex(1);
         cmbHiddenPower.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 cmbHiddenPowerItemStateChanged(evt);
             }
         });
 
         splitPane.setBorder(null);
         splitPane.setDividerLocation(100);
         splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
         splitPane.setEnabled(false);
         splitPane.setLeftComponent(scrollSelected);
         splitPane.setRightComponent(scrollMoves);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(jLabel5)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtNickname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 131, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(jLabel24)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(cmbGender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 128, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(chkShiny))
                     .add(layout.createSequentialGroup()
                         .add(7, 7, 7)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(panelStats, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                     .add(jLabel25)
                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                     .add(cmbHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 91, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                     .add(txtHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                         .add(jLabel27)
                                         .add(jLabel26))
                                     .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                     .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                         .add(cmbNature, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                         .add(cmbAbility, 0, 162, Short.MAX_VALUE)))))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(splitPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 404, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel5)
                     .add(txtNickname, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jLabel24)
                     .add(txtLevel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(cmbItem, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(cmbGender, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(chkShiny))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(panelStats, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 190, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel25)
                             .add(cmbHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(txtHiddenPower, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(jLabel26)
                             .add(cmbNature, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(cmbAbility, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(jLabel27)))
                     .add(splitPane, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 295, Short.MAX_VALUE))
                 .addContainerGap())
         );
     }// </editor-fold>//GEN-END:initComponents
 
     private void cmbNatureItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbNatureItemStateChanged
         if ((evt != null) && (evt.getStateChange() != ItemEvent.SELECTED)) return;
         PokemonNature n = (PokemonNature)cmbNature.getSelectedItem();
         for (int i = 0; i < m_totals.length; i++) {
             updateStat(i);
             if (i == n.getBenefits()) {
                 m_totals[i].setForeground(new Color(0, 150, 0));
             } else if (i == n.getHarms()) {
                 m_totals[i].setForeground(new Color(180, 0, 0));
             } else {
                 m_totals[i].setForeground(Color.BLACK);
             }
         }
         checkIllegalMovesets();
     }//GEN-LAST:event_cmbNatureItemStateChanged
 
     private void chkShinyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_chkShinyActionPerformed
         m_parent.setSpriteShiny(chkShiny.isSelected());
     }//GEN-LAST:event_chkShinyActionPerformed
 
     private void cmbHiddenPowerItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbHiddenPowerItemStateChanged
         if (evt.getStateChange() != ItemEvent.SELECTED) return;
         if (hpProgramSelect) return;
         String type = (String)cmbHiddenPower.getSelectedItem();
         int[] ivs = m_hiddenPowers.get(type);
         for (int i = 0; i < m_ivs.length; i++) {
             m_ivs[i].setText(String.valueOf(ivs[i]));
             updateStat(i);
         }
         updateHiddenPower();
     }//GEN-LAST:event_cmbHiddenPowerItemStateChanged
 
     private void txtLevelKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtLevelKeyReleased
         try {
             m_pokemon.level = Integer.parseInt(txtLevel.getText());
         } catch (NumberFormatException e) {
             // Integer document should prevent this
         }
         for (int i = 0; i < Pokemon.STAT_COUNT; i++) {
             updateStat(i);
         }
         
     }//GEN-LAST:event_txtLevelKeyReleased
 
     private void cmbAbilityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbAbilityItemStateChanged
         checkIllegalMovesets();
     }//GEN-LAST:event_cmbAbilityItemStateChanged
 
     private void cmbGenderItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_cmbGenderItemStateChanged
         checkIllegalMovesets();
     }//GEN-LAST:event_cmbGenderItemStateChanged
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JCheckBox chkShiny;
     private javax.swing.JComboBox cmbAbility;
     private javax.swing.JComboBox cmbGender;
     private javax.swing.JComboBox cmbHiddenPower;
     private javax.swing.JComboBox cmbItem;
     private javax.swing.JComboBox cmbNature;
     private javax.swing.JLabel jLabel24;
     private javax.swing.JLabel jLabel25;
     private javax.swing.JLabel jLabel26;
     private javax.swing.JLabel jLabel27;
     private javax.swing.JLabel jLabel5;
     private javax.swing.JPanel panelStats;
     private javax.swing.JScrollPane scrollMoves;
     private javax.swing.JScrollPane scrollSelected;
     private javax.swing.JSplitPane splitPane;
     private javax.swing.JTextField txtHiddenPower;
     private javax.swing.JTextField txtLevel;
     private javax.swing.JTextField txtNickname;
     // End of variables declaration//GEN-END:variables
 
     public static void main(String[] args) {
         TeamBuilder.main(args);
     }
 
 }
