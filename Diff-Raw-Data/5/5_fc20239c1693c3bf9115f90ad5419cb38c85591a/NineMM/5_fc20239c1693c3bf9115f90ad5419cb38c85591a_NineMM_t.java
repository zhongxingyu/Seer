 package com.gui;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.net.URL;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 
 import com.ai.Brain;
 import com.ruleset.Referee;
 
 public class NineMM {
 
 	public JFrame frame;
 	private int appWidth = 736;
 	private int appHeigth = 736;
 	private JPanel leftPanel;
 	private JPanel rightPanel;
 	private JTextArea txtLogArea;
 	private Settings setting;
 	private JLabel[] blacks;
 	private int whitePointer = 0;
 	private int blackPointer = 0;
 	private JLabel[] whites;
 	private JLayeredPane centerPanel;
 	private Node[] nodes = new Node[24];
 	private boolean turnOfStarter = true;
 	private ArrayList<Point> placedPieces = new ArrayList<Point>();
 	private int placedCounter = 0;
 //	private boolean isStartPhase = true;
 //	private int lastPos = 0;
 	private Referee referee;
 	private Brain brain = new Brain(this);
 //	private ArrayList<Node> nbs = new ArrayList<Node>();
 	private static Splashscreen splash;
 	private JLabel selectedPiece;
 	private boolean deleteFlag = false;
 	
 //	private JLabel lastSelectedPiece;
 	private boolean blacksTurn = false;
 	private boolean whitesTurn = true;
 
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 
 		// EventQueue.invokeLater(new Runnable() {
 		// public void run() {
 		// try {
 		// NineMM window = new NineMM(null);
 		// window.frame.setVisible(true);
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// }
 		// }
 		// });
 
 		splash = new Splashscreen();
 		splash.setVisible(true);
 	}
 
 	/**
 	 * Create the application.
 	 * 
 	 * @param setting
 	 */
 	public NineMM(Settings setting) {
 		this.setting = setting;
 		splash.dispose();
 		initializeWindow();
 		initializeGameField();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initializeWindow() {
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 		frame = new JFrame();
 		frame.setBounds((dim.width / 2 - (appWidth / 2)),
 				(dim.height / 2 - (appHeigth / 2)), appWidth, appHeigth);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(null);
 		frame.setTitle("9MM - Nine Men's Morris");
 
 		leftPanel = new JPanel();
 		leftPanel.setForeground(new Color(0, 0, 0));
 		leftPanel.setBackground(new Color(255, 255, 204));
 		leftPanel.setBounds(6, 6, 100, 500);
 		frame.getContentPane().add(leftPanel);
 		leftPanel.setLayout(null);
 
 		JLabel lblPlayer = new JLabel("PLAYER 1");
 		lblPlayer.setBounds(20, 454, 61, 16);
 		leftPanel.add(lblPlayer);
 
 		ImageIcon texture = createImageIcon("/resources/Wood.jpg");
 
 		rightPanel = new JPanel();
 		rightPanel.setBackground(Color.DARK_GRAY);
 		rightPanel.setBounds(630, 6, 100, 500);
 		frame.getContentPane().add(rightPanel);
 		rightPanel.setLayout(null);
 
 		JLabel lblPlayer_1 = new JLabel("PLAYER 2");
 		lblPlayer_1.setForeground(new Color(255, 255, 255));
 		lblPlayer_1.setBounds(20, 454, 61, 16);
 		rightPanel.add(lblPlayer_1);
 
 		centerPanel = new JLayeredPane();
 		centerPanel.setBackground(Color.ORANGE);
 		centerPanel.setBounds(118, 6, 500, 500);
 		frame.getContentPane().add(centerPanel);
 		centerPanel.setLayout(null);
 		ImageIcon field = createImageIcon("/resources/Spielfeld_roundedCorners.png");
 
 		JLabel feld = new JLabel(field);
 		feld.setBounds(0, 0, 500, 500);
 		centerPanel.add(feld, 2);
 
 		JLabel textureCenter = new JLabel(texture);
 		textureCenter.setBounds(0, 0, 500, 500);
 		centerPanel.add(textureCenter, 3);
 
 		setupEventFields(24);
 
 		JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setBounds(6, 518, 724, 149);
 		frame.getContentPane().add(scrollPane);
 
 		txtLogArea = new JTextArea();
 		txtLogArea.setEditable(false);
 		scrollPane.setViewportView(txtLogArea);
 
 		JButton btnClose = new JButton("Close");
 		btnClose.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		btnClose.setBounds(613, 673, 117, 35);
 		frame.getContentPane().add(btnClose);
 		
 		referee = new Referee(this);
 		
 		JButton btnRestart = new JButton("Restart");
 		btnRestart.setBounds(487, 673, 117, 35);
 		frame.getContentPane().add(btnRestart);
 		if(turnOfStarter){
 			txtLogArea.append("Whites turn!\n");
 		}
 		else{
 			txtLogArea.append("Blacks turn!\n");
 		}
 		
 	}
 
 	/**
 	 * Initialize the contents of the game field.
 	 */
 	private void initializeGameField() {
 		ImageIcon iconWhite = createImageIcon("/resources/White_Stone.png");
 		int space = 0;
 		blacks = new JLabel[9];
 		whites = new JLabel[9];
 
 		for (int i = 0; i < 9; i++) {
 			final JLabel lblWhite = new JLabel(iconWhite);
 			lblWhite.addMouseListener(new MouseAdapter() {
 
 				@Override
 				public void mouseClicked(MouseEvent e) {
 //					if(deleteFlag && whitesTurn){
 //						System.out.println("delete Stein");
 //						deleteFlag = false;
 //						lblWhite.setVisible(false);
 //						lblWhite.setLocation(0, 0);
 //						for(Point p : placedPieces){
 //							if(p.x == lblWhite.getBounds().x && p.y == lblWhite.getBounds().y){
 //								p.x = 0;
 //								p.y = 0;
 //							}
 //						}
 //						getNode(lblWhite.getBounds().getLocation()).setIsBusy(0);
 //						
 //					}
 //					else{
 //						if (placedCounter == 18) {
 //							if(whitesTurn){
 //								selectedPiece = lblWhite;
 //							}
 //						}
 //					}
 					doSomething(lblWhite.getLocation());
 					if(!deleteFlag){
 						if(setting.getPlayer1().contains("Computer")){
 							if(placedCounter <= 18)
 								brain.setStone(nodes, 1);
 							else{
 								brain.moveStone(nodes, 1);
 								System.out.println("seems to work");
 							}
 						}
 					}					
 				}
 			});
 			lblWhite.setBounds(25, space, 50, 50);
 			leftPanel.add(lblWhite);
 			space += 35;
 			whites[i] = lblWhite;
 		}
 
 		ImageIcon iconBlack = createImageIcon("/resources/Black_Stone.png");
 		space = 0;
 		for (int i = 0; i < 9; i++) {
 			final JLabel lblBlack = new JLabel(iconBlack);
 			lblBlack.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent e) {
 //					if(deleteFlag && blacksTurn){
 //						System.out.println("delete Stein");
 //						deleteFlag = false;
 //						lblBlack.setVisible(false);
 //						for(Point p : placedPieces){
 //							if(p.x == lblBlack.getBounds().x && p.y == lblBlack.getBounds().y){
 //								p.x = 0;
 //								p.y = 0;
 //							}
 //						}
 //						getNode(lblBlack.getBounds().getLocation()).setIsBusy(0);
 //
 //					}
 //					else{
 //						if (placedCounter <= 18) {
 //							if(blacksTurn){
 //								selectedPiece = lblBlack;
 //							}
 //						}
 //					}
 					doSomething(lblBlack.getLocation());
 					if(!deleteFlag){
 						if(setting.getPlayer2().contains("Computer")){
 							if(placedCounter <= 18)
 								brain.setStone(nodes, 2);
 							else{
 								brain.moveStone(nodes, 2);
 								System.out.println("seems to work");
 							}
 								
 						}
 					}
 					
 				}
 			});
 
 			lblBlack.setBounds(25, space, 50, 50);
 			rightPanel.add(lblBlack);
 			space += 35;
 			blacks[i] = lblBlack;
 		}
 
 		setting.printProperties();
 		String path = "";
 		if (setting.getPlayer2().contains("Computer")) {
 			path = "/resources/computer.png";
 		} else {
 			path = "/resources/human.png";
 		}
 
 		turnOfStarter = setting.isStart();
 		
 		space += 64;
 
 		ImageIcon iconRight = createImageIcon(path);
 		JLabel lblRight = new JLabel(iconRight);
 		lblRight.setBounds(20, space, 64, 64);
 		rightPanel.add(lblRight);
 
 		if (setting.getPlayer1().contains("Computer")) {
 			path = "/resources/computer.png";
 		} else {
 			path = "/resources/human.png";
 		}
 
 		ImageIcon iconLeft = createImageIcon(path);
 		JLabel lblLeft = new JLabel(iconLeft);
 		lblLeft.setBounds(20, space, 64, 64);
 		leftPanel.add(lblLeft);
 	}
 
 	public void doSomething(Point point) {
 		if(whitesTurn){
 			JLabel lblWhite = getLabel(point);
 
 			if(deleteFlag && whitesTurn){
 				if(getNode(point).getIsBusy() == 1){
 					//System.out.println("delete Stein");
 					deleteFlag = false;
 					getNode(lblWhite.getBounds().getLocation()).setIsBusy(0);
 	
 					lblWhite.setVisible(false);
 					lblWhite.setLocation(0, 0);
 					for(Point p : placedPieces){
 						if(p.x == lblWhite.getBounds().x && p.y == lblWhite.getBounds().y){
 							placedPieces.remove(p);
 							break;
 						}
 					}
 					if(setting.getPlayer1().contains("Computer")){
						if(countPieces(false) <= 3 && placedCounter > 17){
 							System.out.println(countPieces(false));
 							brain.jumpStone(nodes, 1);								
 						}else{
 							System.out.println(countPieces(false));
 							brain.moveStone(nodes, 1);
 						}
 					}
 				}
 			}
 			else{
 				if (placedCounter == 18) {
 					if(whitesTurn){
 						selectedPiece = lblWhite;
 					}
 				}
 			}
 		}
 		else{
 			JLabel lblBlack = getLabel(point);
 ;
 			if(deleteFlag && blacksTurn){
 				if(getNode(point).getIsBusy() == 2){
 					System.out.println("delete Stein");
 					deleteFlag = false;
 					getNode(lblBlack.getBounds().getLocation()).setIsBusy(0);
 	
 					lblBlack.setVisible(false);
 					for(Point p : placedPieces){
 						if(p.x == lblBlack.getBounds().x && p.y == lblBlack.getBounds().y){
 							placedPieces.remove(p);
 							break;
 						}
 					}
 					if(setting.getPlayer2().contains("Computer")){
						if(countPieces(false) <= 3 && placedCounter > 17){
 							System.out.println(countPieces(false));
 							brain.jumpStone(nodes, 2);								
 						}else{
 							System.out.println(countPieces(false));
 	
 							brain.moveStone(nodes, 2);
 						}
 					}
 				}
 
 			}
 			else{
 				if (placedCounter <= 18) {
 					if(blacksTurn){
 						selectedPiece = lblBlack;
 					}
 				}
 			}
 		}
 	}
 
 	private JLabel getLabel(Point point) {
 		JLabel label = null;
 		JLabel[] searchLabel;
 		if(whitesTurn){
 			searchLabel = whites;
 		}
 		else{
 			searchLabel = blacks;
 		}
 		
 		for(JLabel l : searchLabel){
 			if(l.getBounds().x == point.x && l.getBounds().y == point.y){
 				label = l;
 			}
 		}
 		return label;
 	}
 
 	protected ArrayList<Node> getNeighbours(int x, int y) {
 		for(int i = 0; i < nodes.length; i++){
 			if((nodes[i].location.x == x)&&(nodes[i].location.y == y)){
 				return nodes[i].getNeighbours();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param counter
 	 */
 	/*
 	 * Locations (left to right) upper row: x|y 8|6 218|6 442|6 row: 70|70
 	 * 218|70 370|70 row: 140|140 218|140 310|140 row: 8|230 70|230 140|230
 	 * 310|230 370|230 442|230 row: 140|310 218|310 310|310 row: 70|370 218|370
 	 * 370|370 row: 8|442 218|442 442|442
 	 */
 
 	private void initNodes() {
 		Point p = new Point(0, 0);
 		for (int i = 0; i < 18; i++) {
 			placedPieces.add(p);
 		}
 
 		int x = 8;
 		int y = 6;
 		nodes[0] = new Node("O_0", x, y);
 
 		x = 218;
 		y = 6;
 		nodes[1] = new Node("O_1", x, y);
 
 		x = 442;
 		y = 6;
 		nodes[2] = new Node("O_2", x, y);
 
 		x = 70;
 		y = 70;
 		nodes[3] = new Node("M_0", x, y);
 
 		x = 218;
 		y = 70;
 		nodes[4] = new Node("M_1", x, y);
 
 		x = 370;
 		y = 70;
 		nodes[5] = new Node("M_2", x, y);
 
 		x = 140;
 		y = 140;
 		nodes[6] = new Node("I_0", x, y);
 
 		x = 218;
 		y = 140;
 		nodes[7] = new Node("I_1", x, y);
 
 		x = 310;
 		y = 140;
 		nodes[8] = new Node("I_2", x, y);
 
 		x = 8;
 		y = 230;
 		nodes[9] = new Node("O_3", x, y);
 
 		x = 70;
 		y = 230;
 		nodes[10] = new Node("M_3", x, y);
 
 		x = 140;
 		y = 230;
 		nodes[11] = new Node("I_3", x, y);
 
 		x = 310;
 		y = 230;
 		nodes[12] = new Node("I_4", x, y);
 
 		x = 370;
 		y = 230;
 		nodes[13] = new Node("M_4", x, y);
 
 		x = 442;
 		y = 230;
 		nodes[14] = new Node("O_4", x, y);
 
 		x = 140;
 		y = 310;
 		nodes[15] = new Node("I_5", x, y);
 
 		x = 218;
 		y = 310;
 		nodes[16] = new Node("I_6", x, y);
 
 		x = 310;
 		y = 310;
 		nodes[17] = new Node("I_7", x, y);
 
 		x = 70;
 		y = 370;
 		nodes[18] = new Node("M_5", x, y);
 
 		x = 218;
 		y = 370;
 		nodes[19] = new Node("M_6", x, y);
 
 		x = 370;
 		y = 370;
 		nodes[20] = new Node("M_7", x, y);
 
 		x = 8;
 		y = 442;
 		nodes[21] = new Node("O_5", x, y);
 
 		x = 218;
 		y = 442;
 		nodes[22] = new Node("O_6", x, y);
 
 		x = 442;
 		y = 442;
 		nodes[23] = new Node("O_7", x, y);
 
 		for (int i = 0; i < nodes.length; i++) {
 			nodes[i].setIsBusy(0);
 		}
 		
 		setNeighbours();
 		
 	}
 
 	private void setNeighbours() {
 		nodes[0].addNeighbour(nodes[1]); // outer top left
 		nodes[0].addNeighbour(nodes[9]);
 		nodes[1].addNeighbour(nodes[0]); // outer top center
 		nodes[1].addNeighbour(nodes[2]);
 		nodes[1].addNeighbour(nodes[7]);
 		nodes[2].addNeighbour(nodes[1]); // outer top right
 		nodes[2].addNeighbour(nodes[14]);
 		nodes[3].addNeighbour(nodes[4]); // middle top right
 		nodes[3].addNeighbour(nodes[10]);
 		nodes[4].addNeighbour(nodes[1]); // middle top center
 		nodes[4].addNeighbour(nodes[3]);
 		nodes[4].addNeighbour(nodes[5]);
 		nodes[4].addNeighbour(nodes[7]);
 		nodes[5].addNeighbour(nodes[4]); // middle top right
 		nodes[5].addNeighbour(nodes[13]);
 		nodes[6].addNeighbour(nodes[7]); // inner top left
 		nodes[6].addNeighbour(nodes[11]);
 		nodes[7].addNeighbour(nodes[4]); // inner top center
 		nodes[7].addNeighbour(nodes[6]);
 		nodes[7].addNeighbour(nodes[8]);
 		nodes[8].addNeighbour(nodes[7]); // inner top right
 		nodes[8].addNeighbour(nodes[12]);
 		nodes[9].addNeighbour(nodes[0]); // outer middle left
 		nodes[9].addNeighbour(nodes[10]);
 		nodes[9].addNeighbour(nodes[21]);
 		nodes[10].addNeighbour(nodes[3]); // middle middle left
 		nodes[10].addNeighbour(nodes[9]);
 		nodes[10].addNeighbour(nodes[11]);
 		nodes[10].addNeighbour(nodes[18]);
 		nodes[11].addNeighbour(nodes[6]); // inner middle left
 		nodes[11].addNeighbour(nodes[10]);
 		nodes[11].addNeighbour(nodes[15]);
 		nodes[12].addNeighbour(nodes[8]); // inner middle right
 		nodes[12].addNeighbour(nodes[13]);
 		nodes[12].addNeighbour(nodes[17]);
 		nodes[13].addNeighbour(nodes[5]); // middle middle right
 		nodes[13].addNeighbour(nodes[12]);
 		nodes[13].addNeighbour(nodes[14]);
 		nodes[13].addNeighbour(nodes[20]);
 		nodes[14].addNeighbour(nodes[2]); // outer middle right
 		nodes[14].addNeighbour(nodes[13]);
 		nodes[14].addNeighbour(nodes[23]);
 		nodes[15].addNeighbour(nodes[11]); // inner bottom left
 		nodes[15].addNeighbour(nodes[16]);
 		nodes[16].addNeighbour(nodes[15]); // inner bottom center
 		nodes[16].addNeighbour(nodes[17]);
 		nodes[16].addNeighbour(nodes[19]);
 		nodes[17].addNeighbour(nodes[12]); // inner bottom right
 		nodes[17].addNeighbour(nodes[16]);
 		nodes[18].addNeighbour(nodes[10]); // middle bottom left
 		nodes[18].addNeighbour(nodes[19]);
 		nodes[19].addNeighbour(nodes[16]); // middle bottom center
 		nodes[19].addNeighbour(nodes[18]);
 		nodes[19].addNeighbour(nodes[20]);
 		nodes[19].addNeighbour(nodes[22]);
 		nodes[20].addNeighbour(nodes[13]); // middle bottom right
 		nodes[20].addNeighbour(nodes[19]);
 		nodes[21].addNeighbour(nodes[9]); // outer bottom left
 		nodes[21].addNeighbour(nodes[22]);
 		nodes[22].addNeighbour(nodes[19]); // outer bottom center
 		nodes[22].addNeighbour(nodes[21]);
 		nodes[22].addNeighbour(nodes[23]);
 		nodes[23].addNeighbour(nodes[14]); // outer bottom right
 		nodes[23].addNeighbour(nodes[22]);
 
 	}
 
 	private void setupEventFields(int counter) {
 		initNodes();
 		for (int i = 0; i < counter; i++) {
 			final JLabel interactionFields = new JLabel();
 			interactionFields.setHorizontalAlignment(SwingConstants.CENTER);
 			interactionFields.setBounds(nodes[i].location.x,
 					nodes[i].location.y, 50, 50);
 			interactionFields.setCursor(Cursor
 					.getPredefinedCursor(Cursor.HAND_CURSOR));
 			interactionFields.addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent arg0) {
 					boolean automateAI = true;
 					int limitMoves = 0;
 					
 					while(automateAI && placedCounter < 18){
 						if(!deleteFlag){
 							if(setting.getPlayer1().contains("Computer")){
 								brain.setStone(nodes, 1);								
 							}
 							else{
 								setStones(interactionFields.getLocation());
 								automateAI = false;
 							}						
 						}
 						
 						if(!deleteFlag  && setting.getPlayer2().contains("Computer")){
 							brain.setStone(nodes, 2);
 						}
 					}
 					
 					while(automateAI && placedCounter >= 18 && limitMoves < 1){
 						limitMoves++;
 						if(!deleteFlag){						
 							if(setting.getPlayer1().contains("Computer")){
 								if(countPieces(true) <= 3){
 									System.out.println(countPieces(true));
 
 									brain.jumpStone(nodes, 1);								
 								}else{
 									System.out.println(countPieces(true));
 
 									brain.moveStone(nodes, 1);											
 								}
 							}
 							else{
 								setStones(interactionFields.getLocation());
 								automateAI = false;
 							}						
 						}
 						
 						if(!deleteFlag  && setting.getPlayer2().contains("Computer")){
 							if(countPieces(false) <= 3){
 								System.out.println(countPieces(false));
 								brain.jumpStone(nodes, 2);								
 							}else{
 								System.out.println(countPieces(false));
 
 								brain.moveStone(nodes, 2);
 							}
 						}
 					}
 				}
 			});
 			centerPanel.add(interactionFields, 1);
 		}
 	}
 	private int countPieces(boolean whitesTurn) {
 		int state = 0;
 		int count = 0;
 		if(whitesTurn){
 			state = 1;
 		}
 		else{
 			state = 2;
 		}
 		for(Node n : nodes){
 			if(n.getIsBusy() == state){
 				count++;
 			}
 		}
 		return count;
 	}
 
 	private boolean checkPlace(Point location) {
 		boolean check = false;
 		for (Point p : placedPieces) {
 			if ((p.x == location.x)
 					&& (p.y == location.y)) {
 				check = true;
 				break;
 			} else {
 				check = false;
 			}
 		}
 		return check;
 	}
 
 	public void setStones(Point point) {
 //		System.out.println("+++SETSTONES+++");
 		if (placedCounter <= 17) {
 			if (!checkPlace(point)) {
 				if (turnOfStarter) {
 					
 					getNode(point).setIsBusy(1);
 					
 					whites[whitePointer].setLocation(point);
 					centerPanel.add(whites[whitePointer], 0);
 					whitePointer++;
 					Point p = new Point();
 					p.x = point.x;
 					p.y = point.y;
 					placedPieces.add(p);
 					placedCounter++;
 					
 					deletePiece(referee.checkRules(nodes,whitesTurn));
 
 					whitesTurn = false;
 					blacksTurn = true;
 													
 				}
 				else {
 					
 					getNode(point).setIsBusy(2);
 					
 					blacks[blackPointer].setLocation(point);
 					centerPanel.add(blacks[blackPointer], 0);
 					blackPointer++;
 					Point p = new Point();
 					p.x = point.x;
 					p.y = point.y;
 					placedPieces.add(p);
 					placedCounter++;
 					
 					deletePiece(referee.checkRules(nodes,whitesTurn));
 					
 					whitesTurn = true;
 					blacksTurn = false;
 				}
 				if(whitesTurn){
 					txtLogArea.append("Whites turn!\n");
 				}
 				else{
 					txtLogArea.append("Blacks turn!\n");
 				}
 				turnOfStarter = !turnOfStarter;
 			}
 		} else {
 			if(selectedPiece != null){
 				Node resetNode = getNode(new Point(selectedPiece.getBounds().x,selectedPiece.getBounds().y));
 				int x = point.x;
 				int y = point.y;
 				Node n = getNode(new Point(x,y));
 				boolean isNeighbour = false;
 				ArrayList<Node> tmp = getNeighbours(selectedPiece.getBounds().x,selectedPiece.getBounds().y);
 				for(Node tmpN : tmp){
 					//System.out.println(tmpN.getId());
 					if(tmpN.getId().equals(n.getId())){
 						isNeighbour = true;
 					}
 				}
 				if(countPieces(whitesTurn) <= 3){
 					System.out.println("HOPPING ACTION");
 					if(n.getIsBusy() == 0){
 						selectedPiece.setBounds(x,y, 50, 50);
 						n.setIsBusy(resetNode.getIsBusy());
 						resetNode.setIsBusy(0);
 						selectedPiece = null;
 						
 						deletePiece(referee.checkRules(nodes,whitesTurn));
 						
 						whitesTurn = !whitesTurn;
 						blacksTurn = !blacksTurn;
 						if(whitesTurn){
 							txtLogArea.append("Whites turn!\n");
 						}
 						else{
 							txtLogArea.append("Blacks turn!\n");
 						}
 					}
 				}
 				else{
 					System.out.println("RUECK ACTION");
 					if(n.getIsBusy() == 0 && isNeighbour){
 						selectedPiece.setBounds(x,y, 50, 50);
 						n.setIsBusy(resetNode.getIsBusy());
 						resetNode.setIsBusy(0);
 						selectedPiece = null;
 						
 						deletePiece(referee.checkRules(nodes,whitesTurn));
 						
 						whitesTurn = !whitesTurn;
 						blacksTurn = !blacksTurn;
 						if(whitesTurn){
 							txtLogArea.append("Whites turn!\n");
 						}
 						else{
 							txtLogArea.append("Blacks turn!\n");
 						}
 					}
 				}
 			}
 		}		
 	}
 
 	protected void deletePiece(boolean checkRules) {
 		if(checkRules){
 			txtLogArea.append("############## Mill! ############### \n");
 			txtLogArea.append("##    Select piece to delete then...    ## \n");
 			txtLogArea.append("#################################### \n");
 
 
 			deleteFlag = true;
 		}
 	}
 
 	/** Returns an ImageIcon, or null if the path was invalid. */
 	protected ImageIcon createImageIcon(String path) {
 		URL imgURL = getClass().getResource(path);
 		if (imgURL != null) {
 			return new ImageIcon(imgURL);
 		} else {
 			System.err.println("Couldn't find file: " + path);
 			return null;
 		}
 	}
 	
 	protected Node getNode(Point location) {
 		for(int i = 0; i < nodes.length; i++){
 			if((nodes[i].location.x == location.x)&&(nodes[i].location.y == location.y)){
 				return nodes[i];
 			}
 		}
 		return null;
 	}
 
 	public int getPlacedCounter() {
 		return placedCounter;
 	}
 
 	public void setPlacedCounter(int placedCounter) {
 		this.placedCounter = placedCounter;
 	}
 
 
 }
