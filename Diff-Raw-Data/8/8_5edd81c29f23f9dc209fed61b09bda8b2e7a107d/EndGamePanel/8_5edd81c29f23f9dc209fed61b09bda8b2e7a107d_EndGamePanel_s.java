 package edu.brown.cs32.MFTG.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.event.MouseInputAdapter;
 
 import edu.brown.cs32.MFTG.gui.gameboard.Board;
 
 
 
 public class EndGamePanel extends JPanel{
 	private ImagePanel  _backLite, _backDark, _viewLite, _viewDark;
 	private BufferedImage _winner, _loser, _foreground;
 	private Point _backLoc, _goLoc, _viewLoc;
 	private MonopolyGui _main;
 	private List<String> _names;
 	private boolean _winning;
 	private JLabel _name1, _name2, _name3, _name4;
 	private final int BUTTON_HEIGHT=Constants.FULL_PANEL_HEIGHT/8;
 	private final int BUTTON_WIDTH=2*Constants.FULL_PANEL_HEIGHT/3;
 	private final int START_HEIGHT=Constants.FULL_PANEL_HEIGHT/8;
 	private final int START_WIDTH=Constants.FULL_WIDTH/6;
 	private Font _nameFont;
 	private Board _board;
 	
 	public EndGamePanel(MonopolyGui main, Board board) {
 		try {
 			_board=board;
 			_main=main;
 			java.awt.Dimension size = new java.awt.Dimension(Constants.FULL_WIDTH,Constants.FULL_PANEL_HEIGHT);
 			this.setPreferredSize(size);
 			this.setSize(size);
 			this.setBackground(Color.GRAY);
 			this.setLayout(null);
 			_winner = Helper.resize(ImageIO.read(new File("images/Winner2.png")),this.getWidth(),this.getHeight());
 			_loser = Helper.resize(ImageIO.read(new File("images/Loser.png")),this.getWidth(),this.getHeight());
 			_foreground = Helper.resize(ImageIO.read(new File("images/WinnerColumns.png")),this.getWidth(),this.getHeight());
 								
 			_backLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/MainMenuLite.png")), 130, 50));
 			_backDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/MainMenuDark.png")), 130, 50));
 			_backLoc= new Point(this.getWidth()-_backLite.getWidth()-50, this.getHeight()-_backDark.getHeight()-40);
 			_backLite.setLocation(_backLoc);
 			_backDark.setLocation(_backLoc);
 			_backDark.setVisible(false);
 			
 			_viewLite = new ImagePanel(Helper.resize(ImageIO.read(new File("images/ViewLite.png")), 110, 50));
 			_viewDark = new ImagePanel(Helper.resize(ImageIO.read(new File("images/ViewDark.png")), 110, 50));
 			_viewLoc= new Point(this.getWidth()-_backLite.getWidth()-40, this.getHeight()-_viewLite.getHeight()-_backDark.getHeight()-50);
 			_viewLite.setLocation(_viewLoc);
 			_viewDark.setLocation(_viewLoc);
 			_viewDark.setVisible(false);
 			
 			_winning=false;
 			addMouseListener(new MyMouseListener());
 			
 			_nameFont = new Font("nameFont", Font.BOLD, 16);
 
 			_names = new ArrayList<>();
 			_name1 = new JLabel("<html>First Place</html>");
 			_name2 = new JLabel("<html>Second Place</html>");
 			_name3 = new JLabel("<html>Third Place</html>");
 			_name4 = new JLabel("<html>Last Place</html>");
 			
 			Dimension nameSize = new Dimension(120,50);
 			_name1.setSize(nameSize);
 			_name1.setPreferredSize(nameSize);
 			_name2.setSize(nameSize);
 			_name2.setPreferredSize(nameSize);
 			_name3.setSize(nameSize);
 			_name3.setPreferredSize(nameSize);
 			_name4.setSize(nameSize);
 			_name4.setPreferredSize(nameSize);
 			
			_name1.setLocation(10,245);
			_name2.setLocation(140,380);
			_name3.setLocation(260,560);
			_name4.setLocation(390,770);
 			
 			_name1.setFont(_nameFont);
 			_name2.setFont(_nameFont);
 			_name3.setFont(_nameFont);
 			_name4.setFont(_nameFont);
 			
 			add(_backLite);
 			add(_backDark);
 			add(_viewLite);
 			add(_viewDark);
 			add(_name1);
 			add(_name2);
 			add(_name3);
 			add(_name4);
 
 			
 		} catch (IOException e) {
 			System.out.println("ERROR: "+e.getMessage());
 			System.exit(1);
 		}
 
 	}
 	
 	/**
 	 * sets if player won
 	 * @param didWin
 	 */
 	public void setWinner(boolean didWin, String[] names) {
 		_winning=didWin;
 		if(names.length>0)_name1.setText("<html>"+names[0]+"</html>");
 		if(names.length>1)_name2.setText("<html>"+names[1]+"</html>");
 		if(names.length>2)_name3.setText("<html>"+names[2]+"</html>");
 		if(names.length>3)_name4.setText("<html>"+names[3]+"</html>");
 	}
 	
 	@Override
 	public void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		Graphics2D brush = (Graphics2D) g;
 		brush.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		if(_winning)brush.drawImage(_winner, 0, 0, null);
 		else brush.drawImage(_loser, 0, 0, null);
 		brush.drawImage(_foreground, 0, 0, null);
 	}
 
 	private class MyMouseListener extends MouseInputAdapter{
 		public MyMouseListener() {
 			super();
 		}
 
 		@Override
 		public void mousePressed(MouseEvent e) {
 			int xloc=e.getX();
 			int yloc=e.getY();
 			if(intersects(xloc,yloc,_backLite,_backLoc)) {
 				_backLite.setVisible(false);
 				_backDark.setVisible(true);
 				repaint();
 			}
 			if(intersects(xloc,yloc,_viewLite,_viewLoc)) {
 				_viewLite.setVisible(false);
 				_viewDark.setVisible(true);
 				repaint();
 			}
 
 		}
 		
 		@Override
 		public void mouseReleased(MouseEvent e) {
 
 			int xloc=e.getX();
 			int yloc=e.getY();
 			if(intersects(xloc,yloc,_backLite,_backLoc)) {
 				if(_backDark.isVisible()) {
 					fixPanels();
 					_main.switchPanels("greet");
 				}
 				else {
 					fixPanels();
 				}
 
 			}
 			else if(intersects(xloc,yloc,_viewLite,_viewLoc)) {
 				if(_viewDark.isVisible()) {
 					fixPanels();
 					_board.switchToEndGameMenu();
 					_main.switchPanels("board");
 				}
 				else {
 					fixPanels();
 				}
 
 			}
 			else {
 				fixPanels();
 			}
 		}
 		
 		private void fixPanels() {
 			_backDark.setVisible(false);
 			_backLite.setVisible(true);
 			_viewDark.setVisible(false);
 			_viewLite.setVisible(true);
 			repaint();
 		}
 		private boolean intersects(int xloc, int yloc, JPanel img, Point loc) {
 			if(xloc>=loc.x && xloc<=loc.x+img.getWidth() && yloc>=loc.y && yloc<=loc.y+img.getHeight()) {
 				return true;
 			}
 			return false;
 		}
 
 	}
 
 
 }
 
