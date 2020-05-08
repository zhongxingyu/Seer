 package ch.bfh.monopoly.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 
 import ch.bfh.monopoly.common.GameClient;
 import ch.bfh.monopoly.tile.Chance;
 import ch.bfh.monopoly.tile.NonProperty;
 import ch.bfh.monopoly.tile.Terrain;
 import ch.bfh.monopoly.tile.Tile;
 
 public class BoardBuilder extends JPanel {
 
 	private static final long serialVersionUID = 4140219449792816066L;
 	
 	private final double WEIGHT_TILE_X = 0.1;
 	private final double WEIGHT_TILE_Y = 0.1;
	
 	private final double WEIGHT_CORNER_X = 0.12;
 	private final double WEIGHT_CORNER_Y = 0.12;
 	
 	private JTextArea eventPane;
 
 	private List<BoardTile> tilesList;
 
 	/**
 	 * Construct a new board
 	 * @param eventPane the JTextArea used to draw event
 	 * @param tiles the list of tiles 
 	 */
 	public BoardBuilder(JTextArea eventPane, List<BoardTile> tiles){
 		this.eventPane = eventPane;
 		this.tilesList = tiles;
 		setLayout(new GridBagLayout());
 		centralElements();
 		drawBoard();
 	}
 
 	/**
 	 * Add to the layout the elements in the middle of
 	 * the board (buttons and event window)
 	 */
 	private void centralElements(){
 
 		/******* Event window **************/
 
 		JPanel p = new JPanel();
 		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
 
 		JPanel notify = new JPanel();
 		notify.setLayout(new BoxLayout(notify, BoxLayout.PAGE_AXIS));
 		notify.setBorder(BorderFactory.createEtchedBorder());
 
 		JButton buy = new JButton("Buy");
 		JButton auction = new JButton("Auction");
 
 		JPanel btnPanel = new JPanel();
 		btnPanel.add(buy);
 		btnPanel.add(auction);
 
 		JScrollPane eventPane = new JScrollPane(this.eventPane);
 		eventPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		eventPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		JLabel title = new JLabel("Events");
 
 		notify.add(title);
 		notify.add(eventPane);
 		notify.add(btnPanel);
 
 		p.add(notify, BorderLayout.NORTH);
 		p.setOpaque(false);
 		add(p, new GridBagConstraints(3,3, 5,5, WEIGHT_TILE_X, WEIGHT_TILE_Y, 
 				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
 
 		/****************************************/
 		/****************************************/
 
 		/******* Community chest card button **************/
 
 		JButton community = new JButton("Community");
 		JPanel commP = new JPanel();
 		commP.add(community);
 		commP.setLayout(new GridLayout(1,1));
 		add(commP, new GridBagConstraints(2, 2, 1, 1, WEIGHT_TILE_X, WEIGHT_TILE_Y, 
 				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
 
 		/****************************************/
 		/****************************************/
 
 		/******* Chance card button **************/
 
 		JButton chance = new JButton("Chance");
 		JPanel chancePan = new JPanel();
 		chancePan.add(chance);
 		chancePan.setLayout(new GridLayout(1,1));
 		add(chancePan, new GridBagConstraints(8, 8, 1, 1, WEIGHT_TILE_X, WEIGHT_TILE_X, 
 				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
 
 		/****************************************/
 		/****************************************/
 	}
 
 	/**
 	 * Initialize the board 
 	 * with the relatives tiles
 	 */
 	private void drawBoard(){
 		for(int j = 0 ; j < this.tilesList.size() ; j++){
 			BoardTile tile = this.tilesList.get(j);
 			
 			//corner
 			if(j % 10 == 0){
 				add(tile, new GridBagConstraints(tile.getTileInfoX(), tile.getTileInfoY(), 1, 1, WEIGHT_CORNER_X, WEIGHT_CORNER_Y, 
 						GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
 			}
 			//other tiles
 			else{
 				add(tile, new GridBagConstraints(tile.getTileInfoX(), tile.getTileInfoY(), 1, 1, WEIGHT_TILE_X, WEIGHT_TILE_Y, 
 						GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
 			}
 		}
 
 	}
 }
