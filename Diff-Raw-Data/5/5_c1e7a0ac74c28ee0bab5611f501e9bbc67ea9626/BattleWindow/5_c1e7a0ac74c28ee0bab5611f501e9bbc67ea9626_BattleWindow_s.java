 /*
  * BattleWindow.java
  *
  * Created on Apr 7, 2009, 11:51:16 PM
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
 
 import java.awt.AlphaComposite;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridLayout;
 import java.awt.RenderingHints;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Arrays;
 import javax.swing.ButtonGroup;
 import javax.swing.JOptionPane;
 import javax.swing.JToggleButton;
 import shoddybattleclient.GameVisualisation.VisualPokemon;
 import shoddybattleclient.network.ServerLink;
 import shoddybattleclient.shoddybattle.*;
 import shoddybattleclient.utils.*;
 
 /**
  *
  * @author ben
  */
 public class BattleWindow extends javax.swing.JFrame {
 
     private static enum Action {
         MOVE,
         SWITCH
     }
 
     private class MoveButton extends JToggleButton {
         private int m_i, m_j;
         private PokemonMove m_move = null;
         public MoveButton() {
             setFocusPainted(false);
         }
         public void setMove(int i, int j, PokemonMove move) {
             m_i = i;
             m_j = j;
             m_move = move;
             repaint();
         }
         public PokemonMove getMove() {
             return m_move;
         }
         private int getPp() {
             int pp = m_pp[m_i][m_j];
             if (pp != -1) {
                 return pp;
             }
             return m_move.pp;
         }
         protected void paintComponent(Graphics g) {
             super.paintComponent(g);
             if (m_move == null) return;
             Graphics2D g2 = (Graphics2D)g.create();
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             if (!isEnabled()) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
             }
             g2.setFont(g2.getFont().deriveFont(Font.BOLD).deriveFont(17f));
             g2.drawString(m_move.name, 10, 25);
             g2.setColor(Color.DARK_GRAY);
             g2.setFont(g2.getFont().deriveFont(Font.PLAIN).deriveFont(12f));
             int y = getHeight() - g2.getFontMetrics().getHeight();
             g2.drawString(m_move.type, 10, y);
             String pp = getPp() + "/" + m_move.maxPp;
             int left = getWidth() - g2.getFontMetrics().stringWidth(pp) - 5;
             g2.drawString(pp, left, y);
             g2.dispose();
         }
     }
 
     private static class SwitchButton extends JToggleButton {
         private Pokemon m_pokemon = null;
         public SwitchButton() {
             this.setFocusPainted(false);
         }
         public void setPokemon(Pokemon pokemon) {
             m_pokemon = pokemon;
         }
         protected void paintComponent(Graphics g) {
             Graphics2D g2 = (Graphics2D)g.create();
             super.paintComponent(g2);
             if (m_pokemon == null) return;
             g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
             if (!isEnabled()) {
                 g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
             }
             g2.setFont(g2.getFont().deriveFont(Font.BOLD));
             g2.drawString(m_pokemon.species, 5, getHeight() / 2 - g2.getFontMetrics().getHeight() / 2 + 7);
             g2.dispose();
         }
     }
 
     private static class TargetButton extends JToggleButton {
         private final int m_target;
         public TargetButton(String text, int target) {
             super(text);
             m_target = target;
             setFocusPainted(false);
         }
         public int getTarget() {
             return m_target;
         }
     }
 
     private ServerLink m_link;
     private MoveButton[] m_moveButtons;
     private SwitchButton[] m_switches;
     private TargetButton[] m_targets = null;
     private GameVisualisation m_visual;
     // TODO: allow for more health bars in doubles
     private HealthBar[] m_healthBars = new HealthBar[2];
     private HTMLPane m_chat;
     private ArrayList<PokemonMove> m_moveList;
     // Your Pokemon in this match
     private Pokemon[] m_pokemon;
     // Users in this match
     private String[] m_users;
     // Pp of moves
     private int[][] m_pp;
     // Your participant number in this battle
     private int m_participant;
     // This battle's field ID
     private int m_fid;
     // if we are forced to make a certain move
     private boolean m_forced = false;
     // if we are in the process of targeting
     private boolean m_targeting = false;
     // the number of pokemon on each team
     private int m_n;
     // the pokemon we are currently selecting for
     private int m_current;
     // the move that we are targeting for
     private int m_selectedMove = -1;
 
     public int getPartySize() {
         return m_n;
     }
 
     public int getParticipant() {
         return m_participant;
     }
 
     /** Creates new form BattleWindow */
     public BattleWindow(ServerLink link, int fid,
             int n,
             int participant,
             String[] users,
             Pokemon[] team) {
         initComponents();
 
         setTitle(users[0] + " vs. " + users[1] + " - Shoddy Battle");
 
         m_link = link;
         m_fid = fid;
         m_n = n;
         m_participant = participant;
         m_users = users;
         m_pokemon = team;
         m_pp = new int[m_pokemon.length][Pokemon.MOVE_COUNT];
         for (int i = 0; i < m_pp.length; ++i) {
             for (int j = 0; j < Pokemon.MOVE_COUNT; ++j) {
                 m_pp[i][j] = -1;
             }
         }
 
         listUsers.setModel(new UserListModel(new ArrayList()));
         setUsers(users);
         if (m_participant == 0) {
             lblPlayer0.setText(users[0]);
             lblPlayer1.setText(users[1]);
         } else {
             lblPlayer0.setText(users[1]);
             lblPlayer1.setText(users[0]);
         }
         m_chat = new HTMLPane("Ben");
         scrollChat.add(m_chat);
         scrollChat.setViewportView(m_chat);
 
         MoveListParser mlp = new MoveListParser();
         m_moveList = mlp.parseDocument(BattleWindow.class.getResource("resources/moves2.xml").toString());
 
         createButtons();
         setupVisual();
         setMoves(0);
         updateSwitches();
 
         btnSwitch.setEnabled(false);
         btnSwitchCancel.setEnabled(false);
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
     }
 
     public String getTrainer(int party) {
         return m_users[party];
     }
 
     public void setPp(int pokemon, int move, int pp) {
         m_pp[pokemon][move] = pp;
     }
 
     public void informVictory(int party) {
         // todo: improve this
         if (party == -1) {
             addMessage(null, "It's a draw!");
         } else {
             addMessage(null, m_users[party] + " wins!");
         }
     }
 
     private void createButtons() {
         m_moveButtons = new MoveButton[4];
         panelMoves.setLayout(new GridLayout(2, 2));
         ButtonGroup moveButtons = new ButtonGroup();
         for (int i = 0; i < m_moveButtons.length; i++) {
             final int idx = i;
             final MoveButton button = new MoveButton();
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     if (!button.isEnabled()) return;
                     if (e.getClickCount() == 2) {
                         sendMove(idx);
                     }
                 }
             });
             moveButtons.add(button);
             m_moveButtons[i] = button;
             panelMoves.add(button);
         }
 
         ButtonGroup switchButtons = new ButtonGroup();
         panelSwitch.setLayout(new GridLayout(3, 2));
         m_switches = new SwitchButton[6];
         for (int i = 0; i < m_switches.length; i++) {
             final int idx = i;
             final SwitchButton button = new SwitchButton();
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     if (!button.isEnabled()) return;
                     if (e.getClickCount() == 2) {
                         sendSwitch(idx);
                     }
                 }
             });
             switchButtons.add(button);
             m_switches[i] = button;
             panelSwitch.add(button);
         }
     }
 
     /**
      * Switches the move panel to show moves
      */
     private void showMoves() {
         if (m_moveButtons[0].isAncestorOf(panelMoves)) return;
         m_targeting = false;
         panelMoves.removeAll();
         panelMoves.setLayout(new GridLayout(2, 2));
         for (int i = 0; i < m_moveButtons.length; i++) {
             panelMoves.add(m_moveButtons[i]);
         }
         panelMoves.repaint();
     }
 
     /**
      * Switches the move panel to show targets
      * @param mode Some constant representing the kind of targeting this move has
      * @param self The index of the current user
      */
     private void showTargets(int mode) {
         m_targeting = true;
         panelMoves.removeAll();
         String[] names;
         if (mode == 0) {
             /*single target*/
             names = m_visual.getPokemonNames();
             panelMoves.setLayout(new GridLayout(2, m_n));
         } else if (mode == 1) {
             /* ally */
             names = m_visual.getAllyNames();
             panelMoves.setLayout(new GridLayout(1, m_n));
         } else {
             names = new String[0];
         }
         
         m_targets = new TargetButton[names.length];
         ButtonGroup bg = new ButtonGroup();
         for (int i = names.length - 1; i >= 0; i--) {
             final int idx = i;
             final TargetButton button = new TargetButton(names[i], idx);
             button.addMouseListener(new MouseAdapter() {
                 @Override
                 public void mouseClicked(MouseEvent e) {
                     if (!button.isEnabled()) return;
                     m_visual.setTarget(idx);
                     if (e.getClickCount() == 2) {
                        sendMove(idx);
                     }
                 }
             });
             if ((idx == m_current) || (names[i] == null)) button.setEnabled(false);
             m_targets[i] = button;
             bg.add(button);
             panelMoves.add(button);
         }
         panelMoves.repaint();
     }
 
     public void setUsers(String[] users) {
         listUsers.setModel(new UserListModel(new ArrayList(Arrays.asList(users))));
     }
 
     private void setMoves(int i) {
         for (int j = 0; j < m_moveButtons.length; j++) {
             setMove(i, j, PokemonMove.getIdFromName(m_moveList, m_pokemon[i].moves[j]));
         }
     }
 
     private void setupVisual() {
         m_visual = new GameVisualisation(0, m_n);
         m_visual.setSize(m_visual.getPreferredSize());
         int base = 15;
         int buffer = 5;
         int healthHeight = 35;
         int x = 20;
         m_visual.setLocation(x, base + healthHeight + buffer);
         m_healthBars[0] = new HealthBar();
         m_healthBars[0].setLocation(x, base);
         m_healthBars[0].setSize(m_visual.getWidth(), healthHeight);
         m_healthBars[1] = new HealthBar();
         m_healthBars[1].setLocation(x, base + healthHeight + (2 * buffer) + m_visual.getHeight());
         m_healthBars[1].setSize(m_visual.getWidth(), healthHeight);
         add(m_healthBars[0]);
         add(m_healthBars[1]);
         add(m_visual);
     }
 
     public void setMove(int pokemon, int idx, int id) {
         if ((idx < 0) || (idx >= m_moveButtons.length)) return;
         for (PokemonMove move : m_moveList) {
             if (move.id == id) {
                 m_moveButtons[idx].setMove(pokemon, idx, move);
                 break;
             }
         }
     }
 
     /**
      * Request an action for a pokemon
      * @param idx the index of the pokemon
      */
     public void requestAction(int idx, int slot) {
         m_current = slot;
         showMoves();
         setMoves(idx);
         btnMove.setEnabled(true);
         btnSwitch.setEnabled(true);
         btnMoveCancel.setEnabled(false);
         btnSwitchCancel.setEnabled(false);
         tabAction.setSelectedIndex(0);
         m_visual.setSelected(slot);
     }
 
     public void requestReplacement() {
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
         btnSwitch.setEnabled(true);
         btnSwitchCancel.setEnabled(false);
         tabAction.setSelectedIndex(1);
     }
 
     public void requestTarget(int mode) {
         showTargets(mode);
         btnMove.setEnabled(true);
         btnMoveCancel.setEnabled(false);
     }
 
     private void sendMove(int idx) {
         if (!btnMove.isEnabled()) return;
         m_selectedMove = idx;
         int defaultTarget = 1;
         String target = m_moveButtons[idx].getMove().target;
         if (m_n == 1) {
             sendAction(Action.MOVE, idx, defaultTarget);
         } else if ("Enemy".equals(target)) {
             requestTarget(0);
         } else if ("Ally".equals(target)) {
             if (m_n == 2) {
                 sendAction(Action.MOVE, idx, defaultTarget);
             } else {
                 requestTarget(1);
             }
         } else {
             sendAction(Action.MOVE, idx, defaultTarget);
         }
     }
 
     private void sendSwitch(int idx) {
         if (!btnSwitch.isEnabled()) return;
         sendAction(Action.SWITCH, idx, 0);
         btnSwitch.setEnabled(false);
         btnSwitchCancel.setEnabled(true);
     }
 
     private void sendAction(Action action, int idx, int target) {
         if (m_participant == 1) {
             if (target >= m_n) {
                 target -= m_n;
             } else {
                 target += m_n;
             }
         }
         if (action == Action.MOVE) {
             m_link.sendMoveAction(m_fid, idx, target);
         } else {
             m_link.sendSwitchAction(m_fid, idx);
         }
        //System.out.println(action + " " + idx + " on " + target);
         //showMoves();
         btnMove.setEnabled(false);
         btnMoveCancel.setEnabled(false);
         btnSwitch.setEnabled(false);
         btnSwitchCancel.setEnabled(false);
     }
 
     public void setValidMoves(boolean[] valid) {
         boolean struggle = true;
         for (int i = 0; i < m_moveButtons.length; i++) {
             m_moveButtons[i].setEnabled(valid[i]);
             if (valid[i]) struggle = false;
         }
         if (struggle && !m_forced) {
             btnMove.setText("Struggle");
         }
     }
 
     public void setValidSwitches(boolean[] valid) {
         for (int i = 0; i < m_switches.length; i++) {
             m_switches[i].setEnabled(valid[i]);
         }
     }
 
     private void updateSwitches() {
         for (int i = 0; i < m_switches.length; i++) {
             Pokemon p = m_pokemon[i];
             m_switches[i].setPokemon(p);
             StringBuilder builder = new StringBuilder();
             builder.append("<html>");
             builder.append(p.species);
             builder.append("<br>");
             builder.append(p.ability);
             builder.append("<br>");
             builder.append(p.item);
             builder.append("<br><br>");
             for (int j = 0; j < p.moves.length; j++) {
                 builder.append("-");
                 builder.append(p.moves[j]);
                 builder.append("<br>");
             }
             builder.append("</html>");
             m_switches[i].setToolTipText(builder.toString());
         }
     }
 
     public void setPokemon(VisualPokemon[] p1, VisualPokemon[] p2) {
         m_visual.setParties(p1, p2);
     }
 
     public void setForced(boolean forced) {
         m_forced = forced;
         if (forced) {
             setValidMoves(new boolean[] {false, false, false, false});
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
 
         txtChat = new javax.swing.JTextField();
         tabAction = new javax.swing.JTabbedPane();
         jPanel4 = new javax.swing.JPanel();
         panelMoves = new javax.swing.JPanel();
         btnMove = new javax.swing.JButton();
         btnMoveCancel = new javax.swing.JButton();
         jPanel3 = new javax.swing.JPanel();
         panelSwitch = new javax.swing.JPanel();
         btnSwitch = new javax.swing.JButton();
         btnSwitchCancel = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         listUsers = new javax.swing.JList();
         scrollChat = new javax.swing.JScrollPane();
         lblPlayer0 = new javax.swing.JLabel();
         lblPlayer1 = new javax.swing.JLabel();
         lblClock0 = new javax.swing.JLabel();
         lblClock1 = new javax.swing.JLabel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
         setLocationByPlatform(true);
         addWindowListener(new java.awt.event.WindowAdapter() {
             public void windowClosing(java.awt.event.WindowEvent evt) {
                 formWindowClosing(evt);
             }
         });
 
         txtChat.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusGained(java.awt.event.FocusEvent evt) {
                 txtChatFocusGained(evt);
             }
             public void focusLost(java.awt.event.FocusEvent evt) {
                 txtChatFocusLost(evt);
             }
         });
         txtChat.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 txtChatKeyReleased(evt);
             }
         });
 
         jPanel4.setOpaque(false);
 
         panelMoves.setOpaque(false);
 
         org.jdesktop.layout.GroupLayout panelMovesLayout = new org.jdesktop.layout.GroupLayout(panelMoves);
         panelMoves.setLayout(panelMovesLayout);
         panelMovesLayout.setHorizontalGroup(
             panelMovesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 319, Short.MAX_VALUE)
         );
         panelMovesLayout.setVerticalGroup(
             panelMovesLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 117, Short.MAX_VALUE)
         );
 
         btnMove.setText("Attack");
         btnMove.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMoveActionPerformed(evt);
             }
         });
 
         btnMoveCancel.setText("Cancel");
         btnMoveCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnMoveCancelActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .add(btnMove, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(btnMoveCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
             .add(panelMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel4Layout.createSequentialGroup()
                 .add(panelMoves, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(btnMove)
                     .add(btnMoveCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel4Layout.linkSize(new java.awt.Component[] {btnMove, btnMoveCancel}, org.jdesktop.layout.GroupLayout.VERTICAL);
 
         tabAction.addTab("Move", jPanel4);
 
         jPanel3.setOpaque(false);
 
         panelSwitch.setOpaque(false);
 
         org.jdesktop.layout.GroupLayout panelSwitchLayout = new org.jdesktop.layout.GroupLayout(panelSwitch);
         panelSwitch.setLayout(panelSwitchLayout);
         panelSwitchLayout.setHorizontalGroup(
             panelSwitchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 319, Short.MAX_VALUE)
         );
         panelSwitchLayout.setVerticalGroup(
             panelSwitchLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 117, Short.MAX_VALUE)
         );
 
         btnSwitch.setText("Switch");
         btnSwitch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSwitchActionPerformed(evt);
             }
         });
 
         btnSwitchCancel.setText("Cancel");
         btnSwitchCancel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSwitchCancelActionPerformed(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(btnSwitch, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 165, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(btnSwitchCancel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE))
             .add(panelSwitch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                 .add(panelSwitch, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(btnSwitch)
                     .add(btnSwitchCancel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         jPanel3Layout.linkSize(new java.awt.Component[] {btnSwitch, btnSwitchCancel}, org.jdesktop.layout.GroupLayout.VERTICAL);
 
         tabAction.addTab("Switch", jPanel3);
 
         jScrollPane1.setViewportView(listUsers);
 
         lblPlayer0.setText("Player 1");
 
         lblPlayer1.setText("Player 2");
 
         lblClock0.setText("20:00:00");
 
         lblClock1.setText("20:00:00");
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .add(29, 29, 29)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 340, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 82, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                     .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                     .add(txtChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 233, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(lblPlayer0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 113, Short.MAX_VALUE)
                             .add(lblPlayer1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 113, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(lblClock1)
                             .add(lblClock0))))
                 .addContainerGap())
         );
 
         layout.linkSize(new java.awt.Component[] {lblPlayer0, lblPlayer1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         layout.linkSize(new java.awt.Component[] {lblClock0, lblClock1}, org.jdesktop.layout.GroupLayout.HORIZONTAL);
 
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(layout.createSequentialGroup()
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(lblClock0)
                             .add(lblPlayer0))
                         .add(6, 6, 6)
                         .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                             .add(lblClock1)
                             .add(lblPlayer1))
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(scrollChat, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                         .add(txtChat, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(layout.createSequentialGroup()
                         .add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 230, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(18, 18, 18)
                         .add(tabAction, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 216, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void txtChatFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusGained
 
 }//GEN-LAST:event_txtChatFocusGained
 
     private void txtChatFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_txtChatFocusLost
 
     }//GEN-LAST:event_txtChatFocusLost
 
     public void addMessage(String user, String message) {
         m_chat.addMessage(user, message);
     }
 
     private void txtChatKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtChatKeyReleased
         if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
             m_chat.addMessage("Ben", txtChat.getText());
             txtChat.setText("");
         }
     }//GEN-LAST:event_txtChatKeyReleased
 
     private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
         int result = JOptionPane.showConfirmDialog(this, "Leaving will cause you " +
                 "to forfeit this battle. Are you sure you want to leave?",
                 "Leaving Battle", JOptionPane.YES_NO_OPTION);
         if (result == JOptionPane.OK_OPTION) {
             dispose();
         }
     }//GEN-LAST:event_formWindowClosing
 
     private void btnMoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveActionPerformed
         int selected = -1;
         if (m_targeting) {
             for (int i = 0; i < m_targets.length; i++) {
                 if (m_targets[i].isSelected() &&
                         m_targets[i].isEnabled()) {
                     selected = m_targets[i].getTarget();
                     m_visual.setTarget(Integer.MAX_VALUE);
                     break;
                 }
             }
             if (selected != -1) {
                 sendAction(Action.MOVE, m_selectedMove, selected);
             }
         } else {
             for (int i = 0; i < m_moveButtons.length; i++) {
                 if (m_moveButtons[i].isSelected() &&
                         m_moveButtons[i].isEnabled()) {
                     selected = i;
                     break;
                 }
             }
             if (selected != -1) {
                 sendMove(selected);
             }
         }
     }//GEN-LAST:event_btnMoveActionPerformed
 
     private void btnSwitchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSwitchActionPerformed
         int selected = -1;
         for (int i = 0; i < m_switches.length; i++) {
             if (m_switches[i].isSelected()) {
                 selected = i;
                 break;
             }
         }
         if (selected == -1) return;
         sendSwitch(selected);
     }//GEN-LAST:event_btnSwitchActionPerformed
 
     private void btnMoveCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnMoveCancelActionPerformed
         btnMove.setEnabled(true);
         btnMoveCancel.setEnabled(false);
     }//GEN-LAST:event_btnMoveCancelActionPerformed
 
     private void btnSwitchCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSwitchCancelActionPerformed
         btnSwitch.setEnabled(true);
         btnSwitchCancel.setEnabled(false);
     }//GEN-LAST:event_btnSwitchCancelActionPerformed
 
     /**
     * @param args the command line arguments
     */
     public static void main(String args[]) {
         java.awt.EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     //javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                 } catch (Exception e) {
                     
                 }
                 TeamFileParser tfp = new TeamFileParser();
                 Pokemon[] pokemon = tfp.parseTeam("/home/Catherine/team1.sbt");
                 BattleWindow battle = new BattleWindow(null, 0, 2, 1, new String[] {"bearzly", "Catherine"},
                         pokemon);
                 battle.setPokemon(new VisualPokemon[] {new VisualPokemon("Wartortle", 1, false), null},
                         new VisualPokemon[] {new VisualPokemon("Groudon", 0, true), null});
                 battle.setVisible(true);
                 battle.requestAction(2, 0);
             }
         });
     }
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnMove;
     private javax.swing.JButton btnMoveCancel;
     private javax.swing.JButton btnSwitch;
     private javax.swing.JButton btnSwitchCancel;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel lblClock0;
     private javax.swing.JLabel lblClock1;
     private javax.swing.JLabel lblPlayer0;
     private javax.swing.JLabel lblPlayer1;
     private javax.swing.JList listUsers;
     private javax.swing.JPanel panelMoves;
     private javax.swing.JPanel panelSwitch;
     private javax.swing.JScrollPane scrollChat;
     private javax.swing.JTabbedPane tabAction;
     private javax.swing.JTextField txtChat;
     // End of variables declaration//GEN-END:variables
 
 }
