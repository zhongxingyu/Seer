 /**
  * Jin - a chess client for internet chess servers.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2002 Alexander Maryanovsky.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.board;
 
 import free.jin.Connection;
 import free.jin.Game;
 import free.jin.event.GameAdapter;
 import free.jin.event.GameEndEvent;
 import free.jin.event.GameListener;
 import free.jin.event.OfferEvent;
 import free.jin.plugin.Plugin;
 import free.jin.ui.OptionPanel;
 import free.util.TableLayout;
 import free.workarounds.FixedJPanel;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 
 /**
  * The panel which contains all the action buttons for a played game of type
  * Game.MY_GAME.
  */
 
 public class PlayedGameButtonPanel extends FixedJPanel implements ActionListener{
 
 
 
   /**
    * The offered state - when the offer has already been made by the user.
    */
    
   protected static final int OFFERED_STATE = 0;
   
   
   
   /**
    * The offer state - when the user can merely offer an abort/adjourn/draw by
    * pressing the corresponding button.
    */
 
   protected static final int OFFER_STATE = 1;
 
 
   
 
   /**
    * The claim state - when the user can claim an abort/adjourn/draw by pressing
    * the corresponding button, without his opponent's consent.
    */
 
   protected static final int CLAIM_STATE = 2;
 
 
 
   /**
    * The accept state - when the abort/adjourn/draw has been offered by the
    * opponent and the user can accept it by pressing the corresponding button.
    */
 
   protected static final int ACCEPT_STATE = 3;
 
   
   
   /**
    * The size of the state border.
    */
 
   private static final int STATE_BORDER_SIZE = 5;
 
 
 
   /**
    * The button border for the offered state.
    */
 
   private static final Border OFFERED_STATE_BORDER = 
     new EmptyBorder(STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE);
 
 
   /**
    * The button border for the offer state.
    */
 
   private static final Border OFFER_STATE_BORDER = 
     new EmptyBorder(STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE);
 
 
 
   /**
    * The button border for the claim state.
    */
 
   private static final Border CLAIM_STATE_BORDER =
     new LineBorder(Color.orange, STATE_BORDER_SIZE);
 
 
 
   /**
    * The button border for the accept state.
    */
 
   private static final Border ACCEPT_STATE_BORDER = 
     new LineBorder(Color.green.darker(), STATE_BORDER_SIZE);
 
 
 
 
   /**
    * The Plugin we're being used by.
    */
 
   protected final Plugin plugin;
 
 
 
 
   /**
    * The Game for which this PlayedGameButtonPanel is used.
    */
 
   protected final Game game;
 
 
 
 
   /**
    * The component over which confirmation dialogs are displayed.
    */
 
   protected final Component parentComponent;
 
 
 
 
   /**
    * The "Resign" button.
    */
 
   protected JButton resignButton;
 
 
 
   /**
    * The panel for the border of the resign button.
    */
 
   private JPanel resignButtonPanel;
 
 
 
   /**
    * The "Draw" button.
    */
 
   protected JButton drawButton;
 
 
 
   /**
    * The panel for the border of the draw button.
    */
 
   private JPanel drawButtonPanel;
 
 
 
   /**
    * The "Abort" button.
    */
 
   protected JButton abortButton;
 
 
 
   /**
    * The panel for the border of the abort button.
    */
 
   private JPanel abortButtonPanel;
 
 
 
   /**
    * The "Adjourn" button.
    */
 
   protected JButton adjournButton;
 
 
 
   /**
    * The panel for the border of the adjourn button.
    */
 
   private JPanel adjournButtonPanel;
   
   
   
   /**
    * The takeback (1) button.
    */
    
   protected JButton takeback1Button;
   
   
   
   /**
    * The button for the border of the takeback (1) button.
    */
    
   private JPanel takeback1ButtonPanel;
   
   
   
   /**
    * The multiple takeback button.
    */
    
   protected JButton takebackNButton;
   
   
   
   /**
    * The button for the border of the multiple takeback button.
    */
    
   private JPanel takebackNButtonPanel;
   
   
   /**
    * The result variable obtained through gameEndEvent.
    */
 
    private int result;
 
   /**
    * The game listener that gets notified of various game events we're
    * interested in.
    */
    
      /**
    * The button that let's user to getgame right after the game end. whp 2006
    */
 
     private JButton getgameButton;
 
     /**
      * getgameButton's panel. whp 2006
      */
     
     private JPanel getgameButtonPanel;
     
     /**
      *  The label giving user the result of the game. whp 2006
      */
   
     private JLabel resultLabel;
     
     /**
      * A panel that holds the buttons under the result label. whp 2006
      */
    
     private JPanel buttonsFlowPanel;
      
     
     
   private GameListener gameListener = new GameAdapter(){
 
 
     public void offerUpdated(OfferEvent evt){
       if (evt.getGame() != game)
         return;
       
       // getUserPlayer shouldn't return null here because this panel should only
       // be used for games played by the user.
       boolean isOppsOffer = evt.getPlayer().equals(game.getUserPlayer().getOpponent());
       switch (evt.getOfferId()){
         case OfferEvent.DRAW_OFFER:
           drawOfferUpdate(isOppsOffer, evt.isOffered()); break;
         case OfferEvent.ABORT_OFFER:
           abortOfferUpdate(isOppsOffer, evt.isOffered()); break;
         case OfferEvent.ADJOURN_OFFER:
           adjournOfferUpdate(isOppsOffer, evt.isOffered()); break;
         case OfferEvent.TAKEBACK_OFFER:
           takebackOfferUpdate(isOppsOffer, evt.isOffered(), evt.getTakebackCount()); break;
       }
 
       super.offerUpdated(evt);
     }
 
        @Override
     public void gameEnded(GameEndEvent evt){
        
         
       //TODO add methods and objects that will display to the user the result of the game on the board, near buttons panel.
 
        if (evt.getGame().getID()==game.getID()) {
             switch (evt.getResult()){
                 case Game.WHITE_WINS:
                     resultLabel.setText("White wins!"); break;
                 case Game.BLACK_WINS:
                     resultLabel.setText("Black wins!"); break;
                 case Game.DRAW:
                     resultLabel.setText("It is draw."); break;
                 case Game.UNKNOWN_RESULT:
                     resultLabel.setText("Result unknown. Check console for reason."); break;
 
             }
              if (getgameButton != null){
         getgameButton.setEnabled(true);
       }
       drawButton.setEnabled(false);
       resignButton.setEnabled(false);
       if (abortButton != null)
         abortButton.setEnabled(false);
       if (adjournButton != null)
         adjournButton.setEnabled(false);
       if (takeback1Button != null)
         takeback1Button.setEnabled(false);
       if (takebackNButton != null)
         takebackNButton.setEnabled(false);
 
       plugin.getConn().getListenerManager().removeGameListener(this);
         }     else {
             return;
         }
 
 /*
       if (evt.getGame() != game)
         return;*/
 
 
       super.gameEnded(evt);
     }
 
 
   };
   
 
 
   /**
    * Creates a new PlayedGameButtonPanel. It will be used by the given Plugin
    * for the given Game. The given parent Component determines over which component
    * JOptionPane dialogs will be displayed.
    */
 
   public PlayedGameButtonPanel(Plugin plugin, Game game, Component parentComponent){
     this.plugin = plugin;
     this.game = game;
     this.parentComponent = parentComponent;
 
     init(plugin, game);
   }
 
 
 
 
   /**
    * Initializes this PlayedGameButtonPanel. This method calls delegates to
    * {@link #createComponents(Plugin, Game)} and
    * {@link #addComponents(Plugin, Game)}
    */
 
   protected void init(Plugin plugin, Game game){
     createComponents(plugin, game);
     addComponents(plugin, game);
 
     setDrawState(OFFER_STATE);
     setAbortState(OFFER_STATE);
     setAdjournState(OFFER_STATE);
     setResignState(CLAIM_STATE);
     setTakeback1State(OFFER_STATE);
     setTakebackNState(OFFER_STATE, 2);
 
     plugin.getConn().getListenerManager().addGameListener(gameListener);
   }
 
 
 
 
   /**
    * Gets called when the state of the draw offer (by the opponent) changes.
    */
 
   protected void drawOfferUpdate(boolean isOppsOffer, boolean isOffered){
     setDrawState(isOffered ? (isOppsOffer ? ACCEPT_STATE : OFFERED_STATE) : OFFER_STATE);
   }
 
 
 
   /**
    * Gets called when the state of the abort offer (by the opponent) changes.
    */
 
   protected void abortOfferUpdate(boolean isOppsOffer, boolean isOffered){
     setAbortState(isOffered ? (isOppsOffer ? ACCEPT_STATE : OFFERED_STATE) : OFFER_STATE);
   }
 
 
 
   /**
    * Gets called when the state of the adjourn offer (by the opponent) changes.
    */
 
   protected void adjournOfferUpdate(boolean isOppsOffer, boolean isOffered){
     setAdjournState(isOffered ? (isOppsOffer ? ACCEPT_STATE : OFFERED_STATE) : OFFER_STATE);
   }
   
   
   
   /**
    * A list of the ply counts for all of the user's current outstanding
    * takeback offers.
    */
    
   private final ArrayList userTakebacks = new ArrayList(2);
   
   
   
   /**
    * A list of the ply counts for all of the opponent's current outstanding
    * takeback offers.
    */
    
   private final ArrayList oppTakebacks = new ArrayList(2);
   
   
   
   /**
    * Gets called when the state of the takeback offer (by the opponent) changes.
    */
    
   protected void takebackOfferUpdate(boolean isOppsOffer, boolean isOffered, int plyCount){
     ArrayList offers = isOppsOffer ? oppTakebacks : userTakebacks;
     if (isOffered)
       offers.add(new Integer(plyCount));
     else
       offers.remove(new Integer(plyCount));
     
     int newState;
     int plies;
     if (oppTakebacks.isEmpty()){
       if (userTakebacks.isEmpty()){
         newState = OFFER_STATE;
         plies = plyCount;
       }
       else{
         Integer lastOffer = (Integer)userTakebacks.get(userTakebacks.size() - 1);
         newState = OFFERED_STATE;
         plies = lastOffer.intValue();
       }
     }
     else{
       Integer lastOffer = (Integer)oppTakebacks.get(oppTakebacks.size() - 1);
       newState = ACCEPT_STATE;
       plies = lastOffer.intValue();
     }
     
     if (plies == 1)
       setTakeback1State(newState);
     else
       setTakebackNState(newState, newState == OFFER_STATE ? 2 : plies);
   }
    
   
 
 
   /**
    * Creates all the components of this PlayedGameButtonPanel.
    */
 
   protected void createComponents(Plugin plugin, Game game){
     Connection conn = plugin.getConn();
 
     resultLabel = new JLabel("Game in progress.");
     resultLabel.setFont(new Font(UIManager.getFont("Label.font").getFamily(), Font.BOLD, 18));
     buttonsFlowPanel = new JPanel();
     resignButton = createButton("resign.png" ,'r');
       resignButton.setToolTipText("Resign this game");
     drawButton = createButton("draw.png", 'd');
       drawButton.setToolTipText("Offer a draw");
     abortButton = conn.isAbortSupported() ? createButton("abort.png", 'a') : null;
       abortButton.setToolTipText("Abort the game");
     adjournButton = conn.isAdjournSupported() ? createButton("adjourn.png", 'j') : null;
       adjournButton.setToolTipText("Adjourn a game to resume later");
     takeback1Button = conn.isTakebackSupported() ? createButton( "back1.png", 't') : null;
       takeback1Button.setToolTipText("Request/accept one ply takeback");
     takebackNButton = conn.isMultipleTakebackSupported() ?
       createButton("back2.png", 'k') : null;
       takebackNButton.setToolTipText("Request/accept multi ply takeback");
     
     //creates getgameButton when connected to fics. whp 2006
     getgameButton = (conn.getConnectionName() == "FreechessConnection") ? createButton("getgame.png", 'g') : null;
       //getgameButton = (conn instanceof JinFreechessConnection) ? createButton("getgame.png", 'g') : null;
 
       if (getgameButton != null){
         getgameButton.setToolTipText("Get a game filtered by formule");
       }
   }
 
 
 
   /**
    * Creates a button with the specified text, mnemonic and action command.
    */
    
   private JButton createButton(String filename, char mnemonic){
     JButton button = new JButton(new ImageIcon(getClass().getResource("images/" + filename)));
     button.addActionListener(this);
     button.setMnemonic(mnemonic);
     button.setDefaultCapable(false);
     button.setRequestFocusEnabled(false);
     button.setMargin(new Insets(0, 0, 0, 0));
     
     Font defaultFont = UIManager.getFont("Button.font");
     int fontSize = Math.max(14, defaultFont.getSize());
     button.setFont(new Font(defaultFont.getFamily(), Font.PLAIN, fontSize));
     
 
     return button;
   }
   
   
   
   /**
    * Sets the draw button's state to the specified value.
    */
 
   protected void setDrawState(int state){
     drawButton.setEnabled(state != OFFERED_STATE);
     
     switch (state){
       case OFFERED_STATE:{
         drawButtonPanel.setBorder(OFFERED_STATE_BORDER);
         break;
       }
       case OFFER_STATE:{
         drawButton.setToolTipText("Offer a draw");
         drawButtonPanel.setBorder(OFFER_STATE_BORDER);
         break;
       }
       case CLAIM_STATE:{
         drawButton.setToolTipText("Claim a draw");
         drawButtonPanel.setBorder(CLAIM_STATE_BORDER);
         break;
       }
       case ACCEPT_STATE:{
         drawButton.setToolTipText("Accept a draw");
         drawButtonPanel.setBorder(ACCEPT_STATE_BORDER);
         break;
       }
       default:
         throw new IllegalArgumentException("Unrecognized state: "+state);
     }
   }
 
 
 
 
   /**
    * Sets the abort button's state to the specified value.
    */
 
   protected void setAbortState(int state){
     if (abortButton == null)
       return;
     
     abortButton.setEnabled(state != OFFERED_STATE);    
 
     switch (state){
       case OFFERED_STATE:{
         abortButtonPanel.setBorder(OFFERED_STATE_BORDER);
         break;
       }
       case OFFER_STATE:{
         abortButton.setToolTipText("Offer to abort the game");
         abortButtonPanel.setBorder(OFFER_STATE_BORDER);
         break;
       }
       case CLAIM_STATE:{
         abortButton.setToolTipText("Abort the game");
         abortButtonPanel.setBorder(CLAIM_STATE_BORDER);
         break;
       }
       case ACCEPT_STATE:{
         abortButton.setToolTipText("Agree to abort the game");
         abortButtonPanel.setBorder(ACCEPT_STATE_BORDER);
         break;
       }
       default:
         throw new IllegalArgumentException("Unrecognized state: "+state);
     }
   }
 
 
 
 
   /**
    * Sets the adjourn button's state to the specified value.
    */
 
   protected void setAdjournState(int state){
     if (adjournButton == null)
       return;
     
     adjournButton.setEnabled(state != OFFERED_STATE);    
 
     switch (state){
       case OFFERED_STATE:{
         adjournButtonPanel.setBorder(OFFERED_STATE_BORDER);
         break;
       }
       case OFFER_STATE:{
         adjournButton.setToolTipText("Offer to adjourn the game");
         adjournButtonPanel.setBorder(OFFER_STATE_BORDER);
         break;
       }
       case CLAIM_STATE:{
         adjournButton.setToolTipText("Adjourn the game");
         adjournButtonPanel.setBorder(CLAIM_STATE_BORDER);
         break;
       }
       case ACCEPT_STATE:{
         adjournButton.setToolTipText("Agree to adjourn the game");
         adjournButtonPanel.setBorder(ACCEPT_STATE_BORDER);
         break;
       }
       default:
         throw new IllegalArgumentException("Unrecognized state: "+state);
     }
   }
   
   
   
   /**
    * Sets the state of the takeback (1) button to the specified state.
    */
    
   protected void setTakeback1State(int state){
     if (takeback1Button == null)
       return;
     
     takeback1Button.setEnabled(state != OFFERED_STATE);    
 
     switch (state){
       case OFFERED_STATE:{
         takeback1ButtonPanel.setBorder(OFFERED_STATE_BORDER);
         break;
       }
       case OFFER_STATE:{
         takeback1Button.setToolTipText("Offer to take back a move");
         takeback1ButtonPanel.setBorder(OFFER_STATE_BORDER);
         break;
       }
       case CLAIM_STATE:{
         takeback1Button.setToolTipText("Take back a move");
         takeback1ButtonPanel.setBorder(CLAIM_STATE_BORDER);
         break;
       }
       case ACCEPT_STATE:{
         takeback1Button.setToolTipText("Agree to take back a move");
         takeback1ButtonPanel.setBorder(ACCEPT_STATE_BORDER);
         break;
       }
       default:
         throw new IllegalArgumentException("Unrecognized state: "+state);
     }
   }
 
   
   
   /**
    * Sets the state of the takeback (1) button to the specified state.
    */
    
   protected void setTakebackNState(int state, int plyCount){
     if (takebackNButton == null)
       return;
     
     takebackNButton.setEnabled(state != OFFERED_STATE);
     takebackNButton.setText(Integer.toString(plyCount));
     takebackNButton.setActionCommand(String.valueOf(plyCount));
     
 
     switch (state){
       case OFFERED_STATE:{
         takebackNButtonPanel.setBorder(OFFERED_STATE_BORDER);
         break;
       }
       case OFFER_STATE:{
         takebackNButton.setToolTipText("Offer to take back " + plyCount + " moves");
         takebackNButtonPanel.setBorder(OFFER_STATE_BORDER);
         break;
       }
       case CLAIM_STATE:{
         takebackNButton.setToolTipText("Take back " + plyCount + " moves");
         takebackNButtonPanel.setBorder(CLAIM_STATE_BORDER);
         break;
       }
       case ACCEPT_STATE:{
         takebackNButton.setToolTipText("Agree to take back " + plyCount + " moves");
         takebackNButtonPanel.setBorder(ACCEPT_STATE_BORDER);
         break;
       }
       default:
         throw new IllegalArgumentException("Unrecognized state: "+state);
     }
   }
   
 
 
   /**
    * Sets the resign button's state to the specified value. This button may only
    * be in the claim state.
    */
 
   protected void setResignState(int state){
     switch (state){
       case OFFER_STATE: 
         throw new IllegalArgumentException("The resign button may only be in claim state");
       case CLAIM_STATE:{
         resignButton.setToolTipText("Resign the game");
         resignButtonPanel.setBorder(new EmptyBorder(
           STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE, STATE_BORDER_SIZE));
         break;
       }
       case ACCEPT_STATE:
         throw new IllegalArgumentException("The resign button may only be in claim state");
       default:
         throw new IllegalArgumentException("Unrecognized state: "+state);
     }
   }
 
 
 
   /**
    * Adds all the components to this PlayedGameButtonPanel.
    */
 
   protected void addComponents(Plugin plugin, Game game){
     setLayout(new BorderLayout());
     
     buttonsFlowPanel.setLayout(new TableLayout(4));
     
     add(resultLabel, BorderLayout.PAGE_START);
     
     
     if (getgameButton != null){
         getgameButton.setEnabled(false);
         getgameButtonPanel = new JPanel(new BorderLayout());
         getgameButtonPanel.add(getgameButton, BorderLayout.CENTER);
         getgameButtonPanel.setBorder(this.OFFER_STATE_BORDER);
         buttonsFlowPanel.add(getgameButtonPanel);
     }
     
     drawButtonPanel = new JPanel(new BorderLayout());
     drawButtonPanel.add(drawButton, BorderLayout.CENTER);
     buttonsFlowPanel.add(drawButtonPanel);
 
     resignButtonPanel = new JPanel(new BorderLayout());
     resignButtonPanel.add(resignButton, BorderLayout.CENTER);
     buttonsFlowPanel.add(resignButtonPanel);
 
     if (abortButton != null){    
       abortButtonPanel = new JPanel(new BorderLayout());
       abortButtonPanel.add(abortButton, BorderLayout.CENTER);
       buttonsFlowPanel.add(abortButtonPanel);
     }
     
     if (adjournButton != null){
       adjournButtonPanel = new JPanel(new BorderLayout());
       adjournButtonPanel.add(adjournButton, BorderLayout.CENTER);
       buttonsFlowPanel.add(adjournButtonPanel);
     }
     
     if (takeback1Button != null){
       takeback1ButtonPanel = new JPanel(new BorderLayout());
       takeback1ButtonPanel.add(takeback1Button, BorderLayout.CENTER);
       buttonsFlowPanel.add(takeback1ButtonPanel);
     }
 
     if (takebackNButton != null){
       takebackNButtonPanel = new JPanel(new BorderLayout());
       takebackNButtonPanel.add(takebackNButton, BorderLayout.CENTER);
       buttonsFlowPanel.add(takebackNButtonPanel);
     }
     add(buttonsFlowPanel,BorderLayout.PAGE_END);
   }
 
 
 
   /**
    * ActionListener implementation. Executes the appropriate command depending
    * on the button that was pressed.
    */
 
   public void actionPerformed(ActionEvent evt){
     Object source = evt.getSource();
 
     Connection conn = plugin.getConn();
     if (source == resignButton){
       Object result = OptionPanel.confirm(parentComponent, "Resign?", "RESIGN this game?", OptionPanel.OK);
       if (result == OptionPanel.OK)
         conn.resign(game);
     }
     else if (source == drawButton)
       conn.requestDraw(game);
     else if (source == abortButton)
       conn.requestAbort(game);
     else if (source == adjournButton)
       conn.requestAdjourn(game);
     else if (source == takeback1Button)
       conn.requestTakeback(game);
     else if (source == takebackNButton){
       int plies = Integer.parseInt(takebackNButton.getActionCommand());
       conn.requestTakeback(game, plies);
     }
     else if (source == getgameButton){
         conn.sendCommand("getgame f");
     }
   }
 
 
 
   /**
    * Overrides getMaximumSize() to return the value of getPreferredSize().
    */
 
   public Dimension getMaximumSize(){
     return getPreferredSize();
   }
 
 
 }
