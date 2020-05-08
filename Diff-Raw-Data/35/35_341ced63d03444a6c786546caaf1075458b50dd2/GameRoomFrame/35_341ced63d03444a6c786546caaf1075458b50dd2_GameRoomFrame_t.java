 /*
  * VicisCycle 2012
  * Released under GNU GPL License v2
  */
 package viciscycle.gui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.FontFormatException;
 import java.awt.geom.Area;
 import java.io.File;
 import java.io.IOException;
import java.util.Locale;
 import java.util.ResourceBundle;
 import javax.swing.*;
 import viciscycle.model.tile.*;
 
 /**
  *
  * @author roland
  */
 public class GameRoomFrame extends JInternalFrame{
 	public GameRoomFrame(){
 		super("15", false, false, false, false);
		ResourceBundle currentRes = ResourceBundle.getBundle("viciscycle.translation.lang" /* ,Locale.CHINESE*/);
 		
 		setTitle(currentRes.getString("viciscycle.gui.gameTitle") + " - " + currentRes.getString("viciscycle.gui.gameRoom"));
 		setBounds(0, 0, 1060, 700);
 		setLocation((800 - 330) / 2, (600 - 310) / 2);
 		JPanel gp = new JPanel();
 		gp.setSize(1060, 600);
 
 		JList<ImageIcon> stage = new JList<>();
 		
 		DefaultListModel model = new DefaultListModel();
 		
 		
 		
 		
 		/**control code**/
 		Tile data = new Tile( TileBaseSet.getTilePrototype( TileSymbol.SUN, TileColor.RED ), TileOrientation.UPRIGHT );
 		Tile data2 = new Tile( TileBaseSet.getTilePrototype( TileSymbol.MERCURY, TileColor.RED), TileOrientation.UPRIGHT );
 		ImageIcon[] dataIcons = {data,data2};
 		for(int i= 0; i<10;i++){
 			model.addElement(dataIcons[1]);
 			model.addElement(dataIcons[0]);
 			model.addElement(dataIcons[1]);
 		}
 		
 		
 		
 		
 		stage.setModel(model);
 		stage.setVisibleRowCount(0);
 		stage.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		stage.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		stage.setDragEnabled(true);
		stage.setFixedCellHeight(96);
		stage.setFixedCellWidth(96);
		
 		JList<Tile> rack = new JList<Tile>();
 		rack.setModel(model);
 		rack.setVisibleRowCount(0);
 		rack.setLayoutOrientation(JList.HORIZONTAL_WRAP);
 		rack.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		rack.setDragEnabled(true);
 		
		
 		//stage.setSize(816, 340);
 		//rack.setSize(1016,180);
 		JScrollPane rackScrollPane = new JScrollPane(rack);
 		JScrollPane stageScrollPane= new JScrollPane(stage);
 		//rackScrollPane.setSize(1016, 180);
 				
 		JLabel tileLeftJLabel = new JLabel(currentRes.getString("viciscycle.gui.tilesRemaining") + ":");
 		
 		drawTileButton = new JButton(currentRes.getString("viciscycle.gui.drawTile"));
 		confirmMovesButton = new JButton(currentRes.getString("viciscycle.gui.confirmMoves"));
 		revertMovesButton = new JButton(currentRes.getString("viciscycle.gui.revertMoves"));
 		abandonGameButton = new JButton(currentRes.getString("viciscycle.gui.abandonGame"));
 		
 		try {
 			File HanWangFile = new File("font/WCL-07.ttf");
 			Font HanWangFont = Font.createFont(Font.TRUETYPE_FONT, HanWangFile).deriveFont(Font.PLAIN, 18);
 			
 			revertMovesButton.setFont(HanWangFont);
 			drawTileButton.setFont(HanWangFont);
 			confirmMovesButton.setFont(HanWangFont);
 			abandonGameButton.setFont(HanWangFont);
 		} catch (FontFormatException|IOException e) {
 		}
 		
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(
 				layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 					.addComponent(stageScrollPane,GroupLayout.Alignment.LEADING, 800, 800, 800)
 					.addGroup(layout.createSequentialGroup()
 						.addComponent(tileLeftJLabel)
 						.addComponent(drawTileButton)
 						.addComponent(confirmMovesButton)
 						.addComponent(revertMovesButton)
 						.addComponent(abandonGameButton)
 						.addGap(10,10,10)
 					).addComponent(rackScrollPane)
 				);
 		layout.setVerticalGroup(
 				layout.createSequentialGroup()
 					.addComponent(stageScrollPane,340,340,340)
 					.addGap(10,10,10)
 					.addGroup(layout.createParallelGroup()
 						.addComponent(tileLeftJLabel,GroupLayout.Alignment.LEADING)
 						.addComponent(drawTileButton)
 						.addComponent(confirmMovesButton)
 						.addComponent(revertMovesButton)
 						.addComponent(abandonGameButton))
 					.addGap(10,10,10)
 					.addComponent(rackScrollPane)
 					.addGap(10,10,10)
 				);
 	}
 	
 	private JButton drawTileButton;
 	private JButton confirmMovesButton;
 	private JButton revertMovesButton;
 	private JButton abandonGameButton;
 }
