 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.tokenpanel;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.BorderFactory;
 import javax.swing.InputMap;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingConstants;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.tool.PointerTool;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.language.I18N;
 import net.rptools.maptool.model.GUID;
 import net.rptools.maptool.model.InitiativeList;
 import net.rptools.maptool.model.InitiativeListModel;
 import net.rptools.maptool.model.ModelChangeEvent;
 import net.rptools.maptool.model.ModelChangeListener;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.model.Zone;
 import net.rptools.maptool.model.ZonePoint;
 import net.rptools.maptool.model.InitiativeList.TokenInitiative;
 import net.rptools.maptool.model.Token.Type;
 import net.rptools.maptool.model.Zone.Event;
 
 import com.jeta.forms.components.line.HorizontalLineComponent;
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jidesoft.swing.JideButton;
 import com.jidesoft.swing.JideSplitButton;
 
 /**
  * This panel shows the initiative order inside of MapTools.
  * 
  * @author Jay
  */
 public class InitiativePanel extends JPanel implements PropertyChangeListener, ModelChangeListener, ListSelectionListener {
   
     /*---------------------------------------------------------------------------------------------
      * Instance Variables
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Model containing all of the tokens in this initiative.
      */
     private InitiativeList list;
     
     /**
      * The model used to display a list in the panel;
      */
     private InitiativeListModel model;
     
     /**
      * Component that displays the round
      */
     private JLabel round;
     
     /**
      * Component that displays the initiative list.
      */
     private JList displayList;
     
     /**
      * Flag indicating that token images are shown in the list.
      */
     private boolean showTokens = true;
     
     /**
      * Flag indicating that token states are shown in the list. Only valid if {@link #showTokens} is <code>true</code>.
      */
     private boolean showTokenStates = true;
     
     /**
      * Flag indicating that initiative state is shown in the list.
      */
     private boolean showInitState = true;
     
     /**
      * The zone data being displayed.
      */
     private Zone zone;
     
     /**
      * The component that contains the initiative menu.
      */
     private JideSplitButton menuButton;
 
     /**
      * The menu item that tells the GM if NPC's are visible.
      */
     private JCheckBoxMenuItem hideNPCMenuItem;
     
     /**
      * The menu item that tells the GM if players can change the initiative when
      * working with tokens they own.
      */
     private JCheckBoxMenuItem ownerPermissionsMenuItem;
 
     /**
      * Flag indicating that the owners of tokens have been granted permission to restricted
      * actions when they own the token.
      */
     private boolean ownerPermissions;
     
     /*---------------------------------------------------------------------------------------------
      * Constructor
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Setup the menu 
      */
     public InitiativePanel() {
         
         // Build the form and add it's component
         setLayout(new BorderLayout());
         JPanel panel = new JPanel(new FormLayout("2px pref 8dlu pref 4dlu fill:30px 0px:grow 2px", "4dlu fill:pref 7px fill:0px:grow 4dlu"));
         add(panel, SwingConstants.CENTER);
         menuButton = new JideSplitButton(I18N.getText("initPanel.menuButton"));
         panel.add(menuButton, new CellConstraints(2, 2));
         JideButton rButton = new JideButton(RESET_COUNTER_ACTION);
         rButton.setButtonStyle(JideButton.TOOLBOX_STYLE);
         panel.add(rButton, new CellConstraints(4, 2));
         round = new JLabel();
         round.setHorizontalAlignment(SwingConstants.CENTER);
         round.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
         round.setFont(getFont().deriveFont(Font.BOLD));
         panel.add(round, new CellConstraints(6, 2));
         panel.add(new HorizontalLineComponent(), new CellConstraints(2, 3, 6, 1));
 
         ownerPermissions = MapTool.getCampaign().isInitiativeOwnerPermissions();
 
         // Set up the list with an empty model
         displayList = new JList();
         model = new InitiativeListModel();
         displayList.setModel(model);
         setList(new InitiativeList(null));
         displayList.setCellRenderer(new InitiativeListCellRenderer(this));
 
         // Dragging is only for GM
        displayList.setDragEnabled(hasGMPermission());
         displayList.setTransferHandler(new InitiativeTransferHandler(this));
         displayList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         displayList.addListSelectionListener(this);
         displayList.addMouseListener(new MouseHandler());
         panel.add(new JScrollPane(displayList), new CellConstraints(2, 4, 6, 1));
 
         // Set the keyboard mapping
         InputMap imap = displayList.getInputMap();
         imap.put(KeyStroke.getKeyStroke("DELETE"), "REMOVE_TOKEN_ACTION");
         ActionMap map = displayList.getActionMap();
         map.put("REMOVE_TOKEN_ACTION", REMOVE_TOKEN_ACTION);
         
         // Set action text
         I18N.setAction("initPanel.sort", SORT_LIST_ACTION);        
         I18N.setAction("initPanel.toggleHold", TOGGLE_HOLD_ACTION);
         I18N.setAction("initPanel.setInitState", SET_INIT_STATE_VALUE);        
         I18N.setAction("initPanel.clearInitState", CLEAR_INIT_STATE_VALUE);        
         I18N.setAction("initPanel.showTokens", SHOW_TOKENS_ACTION);
         I18N.setAction("initPanel.showTokenStates", SHOW_TOKEN_STATES_ACTION);
         I18N.setAction("initPanel.showInitStates", SHOW_INIT_STATE);
         I18N.setAction("initPanel.toggleHideNPCs", TOGGLE_HIDE_NPC_ACTION);
         I18N.setAction("initPanel.addPCs", ADD_PCS_ACTION);
         I18N.setAction("initPanel.addAll", ADD_ALL_ACTION);
         I18N.setAction("initPanel.remove", REMOVE_TOKEN_ACTION);
         I18N.setAction("initPanel.removeAll", REMOVE_ALL_ACTION);
         I18N.setAction("initPanel.remove", REMOVE_TOKEN_ACTION);
         I18N.setAction("initPanel.menuButton", NEXT_ACTION);
         I18N.setAction("initPanel.toggleOwnerPermissions", TOGGLE_OWNER_PERMISSIONS_ACTION);
         I18N.setAction("initPanel.round", RESET_COUNTER_ACTION);
         updateView();
     }
     
     /*---------------------------------------------------------------------------------------------
      * Instance Methods
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Update the view after the connection has been created. This allows the menus to be tailored for
      * GM's and Player's properly
      */
     public void updateView() {
         // Set up the button
         if (ownerPermissions || hasGMPermission()) {
             NEXT_ACTION.setEnabled(true);
             menuButton.setAction(NEXT_ACTION);
         } else {
             TOGGLE_HOLD_ACTION.setEnabled(true);
             menuButton.setAction(TOGGLE_HOLD_ACTION);
         } // endif
         
         // Set up the menu
         menuButton.removeAll();
         if (hasGMPermission()) {
             menuButton.add(new JMenuItem(SORT_LIST_ACTION));
             menuButton.addSeparator();
         } // endif
         if (ownerPermissions || hasGMPermission()) {
             menuButton.add(new JMenuItem(TOGGLE_HOLD_ACTION));
         } // endif
         menuButton.add(new JMenuItem(SET_INIT_STATE_VALUE));
         menuButton.add(new JMenuItem(CLEAR_INIT_STATE_VALUE));
         menuButton.addSeparator();        
         JCheckBoxMenuItem item = new JCheckBoxMenuItem(SHOW_TOKENS_ACTION);
         item.setSelected(showTokens);
         menuButton.add(item);
         item = new JCheckBoxMenuItem(SHOW_TOKEN_STATES_ACTION);
         item.setSelected(showTokenStates);
         menuButton.add(item);
         item = new JCheckBoxMenuItem(SHOW_INIT_STATE);
         item.setSelected(showInitState);
         menuButton.add(item);
         if (hasGMPermission()) {
             hideNPCMenuItem = new JCheckBoxMenuItem(TOGGLE_HIDE_NPC_ACTION);
             hideNPCMenuItem.setSelected(list == null ? false : list.isHideNPC());
             menuButton.add(hideNPCMenuItem);
             ownerPermissionsMenuItem = new JCheckBoxMenuItem(TOGGLE_OWNER_PERMISSIONS_ACTION);
             ownerPermissionsMenuItem.setSelected(list == null ? false : ownerPermissions);
             menuButton.add(ownerPermissionsMenuItem);
             menuButton.addSeparator();
             menuButton.add(new JMenuItem(ADD_PCS_ACTION));
             menuButton.add(new JMenuItem(ADD_ALL_ACTION));
             menuButton.addSeparator();
             menuButton.add(new JMenuItem(REMOVE_TOKEN_ACTION));
             menuButton.add(new JMenuItem(REMOVE_ALL_ACTION));
             menuButton.setText(I18N.getText("initPanel.menuButton"));
         } else if (ownerPermissions) {
             menuButton.addSeparator();
             menuButton.add(new JMenuItem(REMOVE_TOKEN_ACTION));
         } // endif
         valueChanged(null);
     }
     
     /**
      * Remove all of the tokens from the model and clear round and current 
      */
     public void clearTokens() {
         list.clearModel();
     }
     
     /**
      * Make sure that the token references match the zone
      */
     public void update() {
         list.update();
     }
     
     /** @return Getter for list */
     public InitiativeList getList() {
         return list;
     }
 
     /** @param theList Setter for the list to set */
     public void setList(InitiativeList theList) {
         
         // Remove the old list
         if (list != null)
             list.removePropertyChangeListener(this);
         
         // Add the new one
         list = theList;
         if (list != null) {
             list.addPropertyChangeListener(this);
             round.setText(list.getRound() >= 0 ? Integer.toString(list.getRound()) : "");
         } // endif
         model.setList(list);
         if (menuButton != null && menuButton.getAction() == NEXT_ACTION)
             menuButton.setButtonEnabled(hasGMPermission() || list.getCurrent() >= 0 && hasOwnerPermission(list.getToken(list.getCurrent())));
         if (list.getCurrent() >= 0) {
             int index = model.getDisplayIndex(list.getCurrent());
             if (index >= 0) 
                 displayList.ensureIndexIsVisible(index);
         } // endif
     }
 
     /** @return Getter for showTokens */
     public boolean isShowTokens() {
         return showTokens;
     }
 
     /** @return Getter for showTokenStates */
     public boolean isShowTokenStates() {
         return showTokenStates;
     }
 
     /** @return Getter for showInitState */
     public boolean isShowInitState() {
         return showInitState;
     }
 
     /** @return Getter for model */
     public InitiativeListModel getModel() {
         return model;
     }
 
     /**
      * Set the zone that we are currently working on.
      * 
      * @param aZone The new zone
      */
     public void setZone(Zone aZone) {
         
         // Clean up listeners
         if (aZone == zone) return;
         if (zone != null)
             zone.removeModelChangeListener(this);
         zone = aZone;
         if (zone != null) 
             zone.addModelChangeListener(this);
         
         // Older campaigns didn't have a list, make sure this one does
         InitiativeList list = zone.getInitiativeList();
         if (list == null) {
             list = new InitiativeList(zone);
             zone.setInitiativeList(list);
         } // endif
 
         // Set the list and actions
         setList(list);
         displayList.getSelectionModel().clearSelection();
         updateView();
     }
     
     /**
      * See if the current player has permission to execute owner restricted actions.
      *  
      * @param token Check this token's ownership
      * @return The value <code>true</code> if this player has permission for restricted actions.
      */
     public boolean hasOwnerPermission(Token token) {
     	if (hasGMPermission())
     		return true;
         if (ownerPermissions && (!MapTool.getServerPolicy().useStrictTokenManagement() 
                 || token.isOwner(MapTool.getPlayer().getName())))
         	return true;
         return false;
     }
     
     /**
      * See if the current player has permission to execute GM restricted actions.
      * @return The value <code>true</code> if this player has permission for all actions.
      */
     public boolean hasGMPermission() {
     	return (MapTool.getPlayer() == null || MapTool.getPlayer().isGM());
     }
 
     /** @return Getter for ownerPermissions */
     public boolean isOwnerPermissions() {
         return ownerPermissions;
     }
 
     /** @param anOwnerPermissions Setter for ownerPermissions */
     public void setOwnerPermissions(boolean anOwnerPermissions) {
         ownerPermissions = anOwnerPermissions;
         updateView();
     }
     
     /*---------------------------------------------------------------------------------------------
      * ListSelectionListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
      */
     public void valueChanged(ListSelectionEvent e) {
         if (e != null && e.getValueIsAdjusting())
         	return;
         TokenInitiative ti =  (TokenInitiative)displayList.getSelectedValue();
         boolean enabled = (ti != null && hasOwnerPermission(ti.getToken())) ? true : false;
         CLEAR_INIT_STATE_VALUE.setEnabled(enabled);
         SET_INIT_STATE_VALUE.setEnabled(enabled);
         if (menuButton.getAction() == TOGGLE_HOLD_ACTION) {
             menuButton.setButtonEnabled(enabled);
         } else {
             TOGGLE_HOLD_ACTION.setEnabled(enabled);
         } // endif
 
         REMOVE_TOKEN_ACTION.setEnabled(enabled);
         ti = (list.getCurrent() >= 0) ? list.getTokenInitiative(list.getCurrent()) : null;
         if (hasGMPermission() || (ti != null && hasOwnerPermission(ti.getToken()))) {
             menuButton.setButtonEnabled(true);
         } else {
             if (menuButton.getAction() == NEXT_ACTION)
                 menuButton.setButtonEnabled(false);
         }
     }
     
     /*---------------------------------------------------------------------------------------------
      * PropertyChangeListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
      */
     public void propertyChange(PropertyChangeEvent evt) {
         if (evt.getPropertyName().equals(InitiativeList.ROUND_PROP)) {
             String text = list.getRound() < 0 ? "" : Integer.toString(list.getRound());
             round.setText(text);
         } else if (evt.getPropertyName().equals(InitiativeList.CURRENT_PROP)) {
             if (list.getCurrent() < 0)
             	return;
             Token t = list.getTokenInitiative(list.getCurrent()).getToken();
             if (t == null)
             	return;
             String s = I18N.getText("initPanel.displayMessage", t.getName());
             if (t.isVisible() && t.getType() != Type.NPC)
                 MapTool.addMessage(TextMessage.say(null, s));
             displayList.ensureIndexIsVisible(model.getDisplayIndex(list.getCurrent()));
             if (menuButton.getAction() == NEXT_ACTION)
                 menuButton.setButtonEnabled(list.getCurrent() >= 0 && hasOwnerPermission(list.getToken(list.getCurrent())));
         } else if (evt.getPropertyName().equals(InitiativeList.TOKENS_PROP)) {
             if ((evt.getOldValue() == null && evt.getNewValue() instanceof TokenInitiative)
                     || (evt.getNewValue() == null & evt.getOldValue() instanceof TokenInitiative))
                 displayList.getSelectionModel().clearSelection();
         } else if (evt.getPropertyName().equals(InitiativeList.HIDE_NPCS_PROP)) {
             displayList.getSelectionModel().clearSelection();
         } else if (evt.getPropertyName().equals(InitiativeList.OWNER_PERMISSIONS_PROP)) {
             updateView();
         } // endif
     }
 
     /*---------------------------------------------------------------------------------------------
      * ModelChangeListener Interface Methods
      *-------------------------------------------------------------------------------------------*/
 
     /**
      * @see net.rptools.maptool.model.ModelChangeListener#modelChanged(net.rptools.maptool.model.ModelChangeEvent)
      */
     public void modelChanged(ModelChangeEvent event) {
         if (event.getEvent().equals(Event.INITIATIVE_LIST_CHANGED)) {
           if ((Zone)event.getModel() == zone) {
               int oldSize = model.getSize();
               setList(((Zone)event.getModel()).getInitiativeList());
               if (oldSize != model.getSize())
                   displayList.getSelectionModel().clearSelection();
           }
         } else if (event.getEvent().equals(Event.TOKEN_ADDED) || event.getEvent().equals(Event.TOKEN_CHANGED)
                 || event.getEvent().equals(Event.TOKEN_REMOVED)) {
             model.updateModel();
         }
     }
     
     /*---------------------------------------------------------------------------------------------
      * Menu Actions
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * This action will advance initiative to the next token in the list.
      */
     public final Action NEXT_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.nextInitiative();
         };
     };
     
     /**
      * This action will remove the selected token from the list.
      */
     public final Action REMOVE_TOKEN_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null)
             	return;
             int index = list.indexOf(ti);
             list.removeToken(index);
         };
     };
     
     /**
      * This action will turn the selected token's initiative on and off.
      */
     public final Action TOGGLE_HOLD_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null)
             	return;
             ti.setHolding(!ti.isHolding());
         };
     };
     
     /**
      * This action toggles the display of token images.
      */
     public final Action SHOW_TOKENS_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             showTokens = ((JCheckBoxMenuItem)e.getSource()).isSelected();
             displayList.setCellRenderer(new InitiativeListCellRenderer(InitiativePanel.this)); // Regenerates the size of each row.
         };
     };
     
     /**
      * This action toggles the display of token images.
      */
     public final Action SHOW_TOKEN_STATES_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             showTokenStates = ((JCheckBoxMenuItem)e.getSource()).isSelected();
             displayList.setCellRenderer(new InitiativeListCellRenderer(InitiativePanel.this)); // Regenerates the size of each row.
         };
     };
     
     /**
      * This action toggles the display of token images.
      */
     public final Action SHOW_INIT_STATE = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             showInitState = ((JCheckBoxMenuItem)e.getSource()).isSelected();
             displayList.repaint();
         };
     };
 
     /**
      * This action sorts the tokens in the list.
      */
     public final Action SORT_LIST_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.sort();
         };
     };
 
     /**
      * This action will set the initiative state of the currently selected token.
      */
     public final Action SET_INIT_STATE_VALUE = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null)
             	return;
             Token token = ti.getToken();
             String sName = (token == null) ? "" : token.getName();
             if (hasGMPermission() && token != null && token.getGMName().trim().length() != 0)
                 sName += " (" + token.getGMName().trim() + ")";
             String s = I18N.getText("initPanel.enterState", sName);
             String input = JOptionPane.showInputDialog(s, ti.getState());
             if (input == null)
             	return;
             ti.setState(input.trim());
         };
     };
 
     /**
      * This action will clear the initiative state of the currently selected token.
      */
     public final Action CLEAR_INIT_STATE_VALUE = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             TokenInitiative ti = (TokenInitiative)displayList.getSelectedValue();
             if (ti == null)
             	return;
             ti.setState(null);
         };
     };
 
     /**
      * This action will remove all tokens from the initiative panel.
      */
     public final Action REMOVE_ALL_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             clearTokens();
         };
     };
 
     /**
      * This action will add all tokens in the zone to this initiative panel.
      */
     public final Action ADD_ALL_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.insertTokens(list.getZone().getTokens());
         };
     };
 
     /**
      * This action will add all PC tokens in the zone to this initiative panel.
      */
     public final Action ADD_PCS_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             List<Token> tokens = new ArrayList<Token>();
             for (Token token : list.getZone().getTokens()) {
                 if (token.getType() == Type.PC) 
                     tokens.add(token);
             } // endfor
             list.insertTokens(tokens);
         };
     };
     
     /**
      * This action will hide all initiative items with NPC tokens from players
      */
     public final Action TOGGLE_HIDE_NPC_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.setHideNPC(!list.isHideNPC());
             if (list.isHideNPC() != hideNPCMenuItem.isSelected()) 
                 hideNPCMenuItem.setSelected(list.isHideNPC());
         };
     };
 
     /**
      * This action will toggle the flag that allows players to modify the init for tokens they own.
      */
     public final Action TOGGLE_OWNER_PERMISSIONS_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             boolean op = !MapTool.getCampaign().isInitiativeOwnerPermissions();
             if (ownerPermissionsMenuItem != null) 
                 ownerPermissionsMenuItem.setSelected(op);
             MapTool.getCampaign().setInitiativeOwnerPermissions(op);
             MapTool.serverCommand().updateCampaign(MapTool.getCampaign().getCampaignProperties());
         };
     };
 
     /**
      * This action will reset the round counter for the initiative panel.
      */
     public final Action RESET_COUNTER_ACTION = new AbstractAction() {
         public void actionPerformed(ActionEvent e) {
             list.startUnitOfWork();
             list.setRound(-1);
             list.setCurrent(-1);
             list.finishUnitOfWork();
         };
     };
 
     /*---------------------------------------------------------------------------------------------
      * DoubleClickHandler Inner Class
      *-------------------------------------------------------------------------------------------*/
     
     /**
      * Handle a double click and context menu on the list of the table.
      * 
      * @author jgorrell
      * @version $Revision$ $Date$ $Author$
      */
     private class MouseHandler extends MouseAdapter {
       
       /**
        * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
        */
       public void mouseClicked(MouseEvent e) {
         if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
           SwingUtilities.invokeLater(new Runnable() {
             public void run() { 
                 if (displayList.getSelectedValue() != null) {
                     // Show the selected token on the map.
                     Token token = ((TokenInitiative)displayList.getSelectedValue()).getToken();
         			ZoneRenderer renderer = MapTool.getFrame().getCurrentZoneRenderer();
         			if (renderer == null) {
         				return;
         			}
 
         			MapTool.getFrame().getCurrentZoneRenderer().centerOn(token);
                     if (MapTool.getPlayer().isGM() && AppState.isPlayerViewLinked()) {
                     	ZonePoint zp = new ScreenPoint(renderer.getWidth()/2, renderer.getHeight()/2).convertToZone(renderer);
             			MapTool.serverCommand().enforceZoneView(renderer.getZone().getId(), zp.x, zp.y, renderer.getScale());
                     }           
                 } 
             }
           });
         } else if (SwingUtilities.isRightMouseButton(e)) {
             TokenInitiative ti = (TokenInitiative)displayList.getModel().getElementAt(displayList.locationToIndex(e.getPoint()));
             if (ti == null)
             	return;
             displayList.setSelectedIndex(model.getDisplayIndex(list.indexOf(ti)));
             // TODO Can I use hasOwnerPermission(ti.getToken()) here instead?
             if (!hasGMPermission() && ti.getToken() != null && !ti.getToken().isOwner(MapTool.getPlayer().getName()))
             	return;
             Set<GUID> tokens = Collections.singleton(ti.getId());
             Set<TokenInitiative> tis = Collections.singleton(ti);
             new InitiativeTokenPopupMenu(tokens, tis, e.getX(), e.getY(), MapTool.getFrame().getCurrentZoneRenderer(), ti.getToken(), ti).showPopup(displayList);
         } // endif
       }
     }
 }
