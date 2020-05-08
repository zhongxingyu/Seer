 /*  Copyright (C) 2010 - 2011  Fabian Neundorf, Philip Caroli,
  *  Maximilian Madlung,	Usman Ghani Ahmed, Jeremias Mechler
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *  
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *  
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ojim.client.gui.GameField;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.LineBorder;
 
 import org.ojim.client.gui.GUIClient;
 import org.ojim.client.gui.PlayerColor;
 import org.ojim.client.gui.StreetColor;
 import org.ojim.client.gui.OLabel.FontLayout;
 import org.ojim.logic.state.GameState;
 import org.ojim.logic.state.Player;
 import org.ojim.logic.state.fields.BuyableField;
 import org.ojim.logic.state.fields.Field;
 import org.ojim.logic.state.fields.Jail;
 import org.ojim.logic.state.fields.Street;
 
 public class GameFieldPiece extends JPanel {
 
 	private Field field;
 	private JPanel colorTop = new JPanel();
 	private JLabel group = new JLabel();
 	private JLabel name = new JLabel();
 	private JLabel price = new JLabel();
 	private JPanel textPanel = new JPanel();
 	private JPanel jailPanel = new JPanel();
 	private Player[] player = new Player[GameState.MAXIMUM_PLAYER_COUNT];
 	private JLabel playerLabel[] = new JLabel[GameState.MAXIMUM_PLAYER_COUNT];
 	private JPanel playerPanelTwo[] = new JPanel[GameState.MAXIMUM_PLAYER_COUNT];
 	private JPanel playerPanel = new JPanel();
 	private JPanel[] housePanels = new JPanel[5];
 	private JPanel highHousePanel = new JPanel();
 	private JPanel innerHousePanel = new JPanel();
 	private JLabel mortageButtonLabel = new JLabel();
 	private JButton mortageButton = new JButton();
 	private GUIClient gui;
 
 	ActionListener mortageListener = new ActionListener() {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			gui.swtichCard(field);
 
 		}
 	};
 
 	public GameFieldPiece(Field field, String name, int position, Image image,
 			GUIClient guiClient) {
 		gui = guiClient;
 	}
 
 	public GameFieldPiece(Field field, GUIClient guiClient) {
 		gui = guiClient;
 		player = new Player[GameState.MAXIMUM_PLAYER_COUNT];
 		/*
 		 * if (this.getComponentCount() > 0) { remove(textPanel);
 		 * remove(playerPanel); remove(colorTop); }
 		 */
 		this.field = field;
 
 		/*
 		 * textPanel.add(group); textPanel.add(playerPanel);
 		 * textPanel.add(name); textPanel.add(price); for(int i = 0; i <
 		 * GameState.MAXIMUM_PLAYER_COUNT; i++){ playerLabel[i] = new
 		 * JLabel("P"); playerLabel[i].setLayout(new FontLayout());
 		 * playerPanelTwo[i] = new JPanel();
 		 * playerPanelTwo[i].add(playerLabel[i]);
 		 * playerPanel.add(playerPanelTwo[i]); }
 		 */
 		// this.add(textPanel);
 
 		playerPanel.setBackground(Color.white);
 		textPanel.setBackground(Color.white);
 
 		// this.drawer = FieldDrawer.getDrawer(field);
 
 		if (!(this.field.getColorGroup() < 0)) {
 
 			innerHousePanel.setBorder(new LineBorder(Color.BLACK));
 			highHousePanel.add(innerHousePanel);
 			highHousePanel.setBorder(new LineBorder(Color.BLACK));
 
 			for (int i = 0; i < 5; i++) {
 				housePanels[i] = new JPanel();
 				housePanels[i].setSize(new Dimension(2, 2));
 				housePanels[i].setBorder(new LineBorder(Color.BLACK));
 
 			}
 			if (((Street) this.field).getBuiltLevel() == 5) {
 				colorTop.add(highHousePanel);
 			} else {
 				for (int i = 0; i < ((Street) this.field).getBuiltLevel(); i++) {
 					colorTop.add(housePanels[i]);
 				}
 			}
 			// this.remove(colorTop);
 			colorTop.setBackground(StreetColor.getBackGroundColor(this.field
 					.getColorGroup()));
 			colorTop.setBorder(new LineBorder(Color.black, 1));
 			this.add(colorTop);
 		}
 
 		try {
 			group
 					.setText("<html>"
 							+ ((Street) field).getFieldGroup().getName());
 
 			group.setLayout(new FontLayout());
 			group.setHorizontalTextPosition(JLabel.CENTER);
 		} catch (ClassCastException e) {
 
 		}
 
 		if (this.field instanceof Jail) {
 			jailPanel.setBackground(Color.BLACK);
 			textPanel.add(jailPanel);
 		}
 
 		playerPanel.setLayout(new GridBagLayout());
 
 		for (int i = 0; i < player.length; i++) {
 			if (player[i] != null) {
 
 				playerLabel[i] = new JLabel();
 				// playerLabel[i].setLayout(new FontLayout());
 				// playerLabel[i].setBounds(0, 0, 15, 15);
 				playerLabel[i].setForeground(PlayerColor.getFontColor(player[i]
 						.getColor()));
 				playerPanelTwo[i].setBackground(PlayerColor
 						.getBackGroundColor(player[i].getColor()));
 				playerPanelTwo[i].setBorder(new LineBorder(PlayerColor
 						.getFontColor(player[i].getColor()), 1));
 
 				// System.out.println("Karte " + this.field.getName()
 				// + " beherbergt nun Spieler " + player[i].getName());
 				playerPanelTwo[i].add(playerLabel[i]);
 				playerPanel.add(playerPanelTwo[i]);
 			}
 		}
 
 		name.setText("<html>" + field.getName());
 		name.setHorizontalTextPosition(JLabel.CENTER);
 		name.setLayout(new FontLayout());
 		try {
 			if (((BuyableField) field).getOwner() != null) {
 
 				price.setText("<html>"
 						+ ((BuyableField) field).getOwner().getName());
 				// System.out.println("SOLD: "+field.getName());
 				// System.out.println(((BuyableField)
 				// field).getOwner().getId()+" - ID - "+field.getName());
 				textPanel.setBackground(PlayerColor
 						.getBackGroundColor(((BuyableField) field).getOwner()
 								.getColor()));
 				try {
 					group.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 
 					price.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 					name.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 					playerPanel.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 
 					group.setForeground(PlayerColor
 							.getFontColor(((BuyableField) field).getOwner()
 									.getColor()));
 					price.setForeground(PlayerColor
 							.getFontColor(((BuyableField) field).getOwner()
 									.getColor()));
 					name.setForeground(PlayerColor
 							.getFontColor(((BuyableField) field).getOwner()
 									.getColor()));
 				} catch (NullPointerException e) {
 
 				}
 
 			} else {
 				// System.out.println("NOT SOLD: "+field.getName());
 				price.setText("<html>" + ((BuyableField) field).getPrice());
 			}
 			price.setHorizontalTextPosition(JLabel.CENTER);
 			price.setVerticalTextPosition(JLabel.BOTTOM);
 			price.setLayout(new FontLayout());
 		} catch (ClassCastException e) {
 			textPanel.setLayout(new GridLayout(3, 0));
 		}
 
 		if (this.field instanceof Jail) {
 			textPanel.setLayout(new JailLayout());
 		} else {
 			textPanel.setLayout(new GridLayout(4, 0));
 		}
 
 		textPanel.add(playerPanel);
 		textPanel.add(group);
 		textPanel.add(name);
 		textPanel.add(price);
 
 		if (field instanceof BuyableField) {
 			mortageButtonLabel.setText("M");
 			mortageButton.addActionListener(mortageListener);
 			mortageButton.add(mortageButtonLabel);
 			if (((BuyableField) field).getOwner() != null) {
 				if (((BuyableField) field).getOwner().getId() == gui
 						.getPlayerMe().getId()) {
 					textPanel.add(mortageButton);
 				}
 				if (((BuyableField) field).isMortgaged()) {
 					textPanel.setBackground(Color.BLACK);
 				}
 			}
 		}
 
 		// text = new JLabel("<html>" + "test");
 		// textPanel.setLayout(new GridLayout(0, 1));
 
 		// this.add(playerPanel);
 		this.add(textPanel);
 		this.setLayout(new GameFieldPieceLayout());
 		// this.setBackground(Color.white);
 		this.setVisible(true);
 
 	}
 
 	public void draw() {
 		/*
 		 * this.remove(textPanel); textPanel.remove(group);
 		 * 
 		 * textPanel.remove(playerPanel);
 		 * 
 		 * textPanel.remove(name); textPanel.remove(price);
 		 */
 
 		// playerPanel.setBackground(Color.white);
 		// textPanel.setBackground(Color.white);
 
 		// this.drawer = FieldDrawer.getDrawer(field);
 		/*
 		 * if (!(this.field.getColorGroup() < 0)) { //this.remove(colorTop);
 		 * colorTop.setBackground(StreetColor.getBackGroundColor(this.field
 		 * .getColorGroup())); colorTop.setBorder(new LineBorder(Color.black,
 		 * 1)); this.add(colorTop); }
 		 */
 		/*
 		 * try { group = new JLabel("<html>" + ((Street)
 		 * field).getFieldGroup().getName());
 		 * 
 		 * group.setLayout(new FontLayout());
 		 * group.setHorizontalTextPosition(JLabel.CENTER); textPanel.add(group);
 		 * } catch (ClassCastException e) {
 		 * 
 		 * }
 		 */
 
 		// textPanel.add(playerPanel);
 		try {
 			// isBankrupt workaround mit <0
 			if (((BuyableField) field).getOwner() != null
 					&& !((BuyableField) field).getOwner().getIsBankrupt()
 					&& !(((BuyableField) field).getOwner().getBalance() < 0)) {
 
 				price.setText("<html>"
 						+ ((BuyableField) field).getOwner().getName());
 				// System.out.println("SOLD: "+field.getName());
 				// System.out.println(((BuyableField)
 				// field).getOwner().getId()+" - ID - "+field.getName());
 				textPanel.setBackground(PlayerColor
 						.getBackGroundColor(((BuyableField) field).getOwner()
 								.getColor()));
 				try {
 					group.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 
 					price.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 					name.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 					playerPanel.setBackground(PlayerColor
 							.getBackGroundColor(((BuyableField) field)
 									.getOwner().getColor()));
 
 					group.setForeground(PlayerColor
 							.getFontColor(((BuyableField) field).getOwner()
 									.getColor()));
 					price.setForeground(PlayerColor
 							.getFontColor(((BuyableField) field).getOwner()
 									.getColor()));
 					name.setForeground(PlayerColor
 							.getFontColor(((BuyableField) field).getOwner()
 									.getColor()));
 				} catch (NullPointerException e) {
 
 				}
 
 			} else {
 
 				textPanel.setBackground(Color.WHITE);
 				group.setBackground(Color.WHITE);
 
 				price.setBackground(Color.WHITE);
 				name.setBackground(Color.WHITE);
 				playerPanel.setBackground(Color.WHITE);
 				group.setForeground(Color.BLACK);
 				price.setForeground(Color.BLACK);
 				name.setForeground(Color.BLACK);
 
 				price.setText("<html>" + ((BuyableField) field).getPrice());
 			}
 		} catch (ClassCastException e) {
 		}
 
 		if (field instanceof BuyableField) {
 			if (((BuyableField) field).getOwner() != null) {
 				if (((BuyableField) field).getOwner().getId() == gui
 						.getPlayerMe().getId()) {
 					textPanel.remove(mortageButton);
 					textPanel.add(mortageButton);
 				}
 			}
 			if (((BuyableField) field).isMortgaged()) {
 				textPanel.setBackground(Color.BLACK);
 			}
 		}
 
 	}
 
 	public void setField(Field field) {
 		this.field = field;
 	}
 
 	@Override
 	protected void paintComponent(Graphics g) {
 		// Draw Field piece here
 	}
 
 	public void addPlayer(Player player) {
 		boolean found = false;
 		for (int i = 0; i < this.player.length; i++) {
 			if (this.player[i] == player) {
 				found = true;
 				break;
 			}
 		}
 		if (!found) {
 			for (int i = 0; i < this.player.length; i++) {
 				if (this.player[i] == null) {
 					this.player[i] = player;
 					playerLabel[i] = new JLabel();
 					playerPanelTwo[i] = new JPanel();
 					break;
 				}
 			}
 		}
 
 		for (int i = 0; i < this.player.length; i++) {
 			if (this.player[i] != null) {
 				// playerLabel[i] = new JLabel(player[i].getName());
 				// playerLabel[i].setLayout(new FontLayout());
 				// playerLabel[i].setBounds(0, 0, 15, 15);
 				playerLabel[i].setForeground(PlayerColor
 						.getFontColor(this.player[i].getColor()));
 				playerPanelTwo[i].setBackground(PlayerColor
 						.getBackGroundColor(this.player[i].getColor()));
 				playerPanelTwo[i].setBorder(new LineBorder(PlayerColor
 						.getFontColor(this.player[i].getColor()), 1));
 
 				// System.out.println("Karte " + this.field.getName()
 				// + " beherbergt nun Spieler " + this.player[i].getName());
 				playerPanelTwo[i].add(playerLabel[i]);
 				if (this.field instanceof Jail
 						&& (this.player[i].getJail() != null)) {
 					// System.out.println("Jail not Null");
 					jailPanel.add(playerPanelTwo[i]);
 				} else {
 					playerPanel.add(playerPanelTwo[i]);
 				}
 			}
 		}
 		playerPanel.revalidate();
 
 	}
 
 	public void removePlayer() {
 		this.player = null;
 		player = new Player[GameState.MAXIMUM_PLAYER_COUNT];
 
 		for (int i = 0; i < GameState.MAXIMUM_PLAYER_COUNT; i++) {
 			playerPanelTwo[i].remove(playerLabel[i]);
 			playerPanel.remove(playerPanelTwo[i]);
 			jailPanel.remove(playerPanelTwo[i]);
 		}
 
 	}
 
 	public void removeSinglePlayer(Player player) {
 
 		for (int i = 0; i < GameState.MAXIMUM_PLAYER_COUNT; i++) {
 			if (this.player[i] != null) {
 				if (playerLabel[i].getBackground().toString().equals(
 						PlayerColor.getBackGroundColor(player.getColor())
 								.toString())
 						|| this.player[i].getId() == player.getId()) {
 
 					if (this.player[i] != null) {
 						this.player[i] = null;
 					}
 					// System.out.println("Player "+player.getName()+" entfernt von Feld "+this.field.getName());
 					playerPanelTwo[i].remove(playerLabel[i]);
 					playerLabel[i] = new JLabel();
 					playerPanel.remove(playerPanelTwo[i]);
 					jailPanel.remove(playerPanelTwo[i]);
 					playerPanelTwo[i] = new JPanel();
 				}
 			}
 		}
 		this.revalidate();
 		this.repaint();
 	}
 
 	public void init(GameState gameState) {
 		removePlayer();
 		for (int i = 0; i < GameState.MAXIMUM_PLAYER_COUNT; i++) {
 			try {
 				if (gameState.getPlayerByID(i).getPosition() == field
 						.getPosition()) {
 					addPlayer(gameState.getPlayerByID(i));
 				}
 			} catch (NullPointerException e) {
 
 			}
 		}
 		draw();
 	}
 
 	public boolean isField(Field field2) {
 
 		return this.field.getPosition() == field2.getPosition();
 	}
 
 	public void redrawStreet() {
 
 		colorTop.removeAll();
 		colorTop.revalidate();
 
 		System.out.println(((Street) this.field).getName() + " ist auf Level "
 				+ ((Street) this.field).getBuiltLevel());
 
 		if (((Street) this.field).getBuiltLevel() == 5) {
 			colorTop.add(highHousePanel);
 		} else {
 			for (int i = 0; i < ((Street) this.field).getBuiltLevel(); i++) {
 				colorTop.add(housePanels[i]);
 			}
 		}
 		repaint();
 		revalidate();
 
 	}
 
 	public Street getField() {
 		if (this.field instanceof Street) {
 			return (Street) this.field;
 		} else {
 			return null;
 		}
 
 	}
 }
