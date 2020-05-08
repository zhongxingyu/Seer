 package gui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Arrays;
 import java.util.LinkedList;
 
 import javax.swing.*;
 
 import reachForTheTop.Player;
 import reachForTheTop.PlayerIO;
 
 /**
  * @author Aly
  *
  */
 public class PlayFrame extends DefaultFrame implements ActionListener{
 	
 	private AlyButton scoreButtonRight[][],scoreButtonLeft[][], plusButtonRight[], plusButtonLeft[], removeButtonRight[], removeButtonLeft[];
 	private GridLayout mainLayout = new GridLayout(6,1);
 	private JLabel playerNamesLeft[], playerNamesRight[], leftTeam, rightTeam;
 	private JPanel rightSuperSubPanel[], leftSuperSubPanel[];
 	private Player players[][];
 	private int totalLeft=0,totalRight=0;
 	
 	public PlayFrame(){
 		super("Game Time!");
 		setLayout(new GridLayout(1,3));
 		createComponents();
 		pack();
 		repaint();	
 		
 	}
 
 	private void createComponents() {
 		Long time = System.currentTimeMillis();
 		createLeftPanel();
 
 		System.out.println(System.currentTimeMillis()-time);
 		JPanel centerPanel = new JPanel();
 		centerPanel.setBackground(ColorScheme.DEFAULT_MAIN);
 		add(centerPanel);
 		
 		/*
 		 * 
 		 * LOOK AWAY! PLEASE!
 		 * THIS WAS HAO'S IDEA!
 		 * 
 		 */
 		createRightPanel();
 
 		System.out.println(System.currentTimeMillis()-time);
 		players = new Player[2][4];
 		System.out.println("Loaded in "+(System.currentTimeMillis()-time)+"ms!");
 		
 	}
 	
 	private void createRightPanel() {
 		JPanel rightMainPanel = new JPanel();
 		rightMainPanel.setLayout(mainLayout);
 		add(rightMainPanel);
 		
 		JPanel rightSubPanel[] = new JPanel[mainLayout.getRows()];
 		SpringLayout rightSubLayout[] = new SpringLayout[mainLayout.getRows()];
 		for(int i=0;i<rightSubPanel.length;i++){
 			rightSubPanel[i] = new JPanel();
 			rightSubLayout[i] = new SpringLayout();
 			
 			rightSubPanel[i].setBackground(ColorScheme.DEFAULT_MAIN);			
 			rightSubPanel[i].setLayout(rightSubLayout[i]);
 			rightMainPanel.add(rightSubPanel[i]);
 		}
 		
 		JLabel teamTwoLabel = new JLabel("TEAM TWO!");
 		teamTwoLabel.setBackground(ColorScheme.DEFAULT_SECONDARY);
 		teamTwoLabel.setForeground(ColorScheme.DEFAULT_MAIN);
 		teamTwoLabel.setFont(new Font("Mangal",Font.BOLD,24));
 		teamTwoLabel.setHorizontalAlignment(JLabel.CENTER);
 		teamTwoLabel.setVerticalAlignment(JLabel.CENTER);
 		teamTwoLabel.setOpaque(true);
 		
 		rightSubLayout[0].putConstraint(SpringLayout.WEST, teamTwoLabel,
 				15, SpringLayout.WEST,rightSubPanel[0]);
 		rightSubLayout[0].putConstraint(SpringLayout.NORTH, teamTwoLabel,
 				0, SpringLayout.NORTH,rightSubPanel[0]);
 		rightSubLayout[0].putConstraint(SpringLayout.EAST, teamTwoLabel,
 				-15, SpringLayout.EAST,rightSubPanel[0]);
 		rightSubLayout[0].putConstraint(SpringLayout.SOUTH, teamTwoLabel,
 				-10, SpringLayout.SOUTH,rightSubPanel[0]);
 	
 		rightSubPanel[0].add(teamTwoLabel);
 		
 		scoreButtonRight = new AlyButton[4][4];
 		playerNamesRight = new JLabel[4];
 		rightSuperSubPanel = new JPanel[4];		
 		removeButtonRight = new AlyButton[4];
 		
 		for(int i=0;i<scoreButtonRight.length;i++){
 						
 			playerNamesRight[i] = new JLabel("   Player");
 			playerNamesRight[i].setBackground(ColorScheme.DEFAULT_SECONDARY);
 			playerNamesRight[i].setForeground(ColorScheme.DEFAULT_MAIN);
 			playerNamesRight[i].setOpaque(true);
 			playerNamesRight[i].setVisible(false);
 			playerNamesRight[i].setVerticalAlignment(JLabel.TOP);
 			playerNamesRight[i].setFont(new Font("Mangal",Font.PLAIN,16));
 			
 			rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, playerNamesRight[i],
 					0, SpringLayout.NORTH,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.WEST, playerNamesRight[i],
 					15, SpringLayout.WEST,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.EAST, playerNamesRight[i],
 					-15, SpringLayout.EAST,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH, playerNamesRight[i],
 					0, SpringLayout.SOUTH,rightSubPanel[i+1]);
 						
 			
 			rightSuperSubPanel[i] = new JPanel();
 			GridLayout g = new GridLayout(1,4);
 			g.setHgap(5);
 			rightSuperSubPanel[i].setLayout(g);
 			rightSuperSubPanel[i].setVisible(false);
 			rightSuperSubPanel[i].setBackground(ColorScheme.DEFAULT_SECONDARY);						
 			
 			rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, rightSuperSubPanel[i],
 				30, SpringLayout.NORTH,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH, rightSuperSubPanel[i],
 					-15, SpringLayout.SOUTH,rightSubPanel[i+1]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.WEST, rightSuperSubPanel[i],
 					10, SpringLayout.WEST,playerNamesRight[i]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.EAST, rightSuperSubPanel[i],
 					-10, SpringLayout.EAST,playerNamesRight[i]);
 			
 			removeButtonRight[i] = new AlyButton("X",false);
 			removeButtonRight[i].setActionCommand(i+"xr");
 			removeButtonRight[i].setVisible(false);
 			removeButtonRight[i].addActionListener(this);
 			removeButtonRight[i].flipColors();
 			rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, removeButtonRight[i],
 					0, SpringLayout.NORTH,playerNamesRight[i]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH, removeButtonRight[i],
 					0, SpringLayout.NORTH,rightSuperSubPanel[i]);
 			rightSubLayout[i+1].putConstraint(SpringLayout.EAST, removeButtonRight[i],
 					-10, SpringLayout.EAST,playerNamesRight[i]);
 				
 			rightSubPanel[i+1].add(removeButtonRight[i]);
 			
 			rightSubPanel[i+1].add(rightSuperSubPanel[i]);
 			rightSubPanel[i+1].add(playerNamesRight[i]);
 		}
 
 		
 		for(int i=0;i<scoreButtonRight.length;i++)
 			for(int j=0;j<scoreButtonRight[0].length;j++){
 				scoreButtonRight[i][j] = new AlyButton((j+1)*10+"");
 				scoreButtonRight[i][j].addActionListener(this);
 				scoreButtonRight[i][j].setActionCommand(i+","+j+" r");
 				rightSuperSubPanel[i].add(scoreButtonRight[i][j]);
 			}			
 		
 		//add the "+" buttons
 		plusButtonRight = new AlyButton[4];
 		for(int i=0;i<plusButtonRight.length;i++){
 			 plusButtonRight[i] = new AlyButton("+",false);
 			 plusButtonRight[i].setActionCommand(i+"r+");
 			 plusButtonRight[i].flipColors();
 			 plusButtonRight[i].addActionListener(this);
 			 plusButtonRight[i].setFont(new Font("Mangal",Font.PLAIN,40));
 			 
 			 rightSubLayout[i+1].putConstraint(SpringLayout.NORTH, plusButtonRight[i],
 					0, SpringLayout.NORTH,rightSubPanel[i+1]);
 			 rightSubLayout[i+1].putConstraint(SpringLayout.WEST, plusButtonRight[i],
 					15, SpringLayout.WEST,rightSubPanel[i+1]);
 			 rightSubLayout[i+1].putConstraint(SpringLayout.EAST, plusButtonRight[i],
 					-15, SpringLayout.EAST,rightSubPanel[i+1]);
 			 rightSubLayout[i+1].putConstraint(SpringLayout.SOUTH,plusButtonRight[i],
 					0, SpringLayout.SOUTH,rightSubPanel[i+1]);
 			 
 			 rightSubPanel[i+1].add(plusButtonRight[i]);
 		}
 		rightTeam = new JLabel("SCORE: 0");
 		rightTeam.setBackground(ColorScheme.DEFAULT_MAIN);
 		rightTeam.setForeground(ColorScheme.DEFAULT_SECONDARY);
 		rightTeam.setFont(new Font("Mangal",Font.BOLD,23));
 		rightSubLayout[5].putConstraint(SpringLayout.NORTH, rightTeam,
 			0, SpringLayout.NORTH,rightSubPanel[5]);
 		rightSubLayout[5].putConstraint(SpringLayout.HORIZONTAL_CENTER, rightTeam,
 			0, SpringLayout.HORIZONTAL_CENTER,rightSubPanel[5]);
 		rightSubLayout[5].putConstraint(SpringLayout.SOUTH,rightTeam,
 			0, SpringLayout.SOUTH,rightSubPanel[5]);
 		rightSubPanel[5].add(rightTeam);		
 	}
 
 	private void createLeftPanel(){
 		JPanel leftMainPanel = new JPanel();
 		leftMainPanel.setLayout(mainLayout);
 		add(leftMainPanel);
 		
 		JPanel leftSubPanel[] = new JPanel[mainLayout.getRows()];
 		SpringLayout leftSubLayout[] = new SpringLayout[mainLayout.getRows()];
 		for(int i=0;i<leftSubPanel.length;i++){
 			leftSubPanel[i] = new JPanel();
 			leftSubLayout[i] = new SpringLayout();
 			
 			leftSubPanel[i].setBackground(ColorScheme.DEFAULT_MAIN);			
 			leftSubPanel[i].setLayout(leftSubLayout[i]);
 			leftMainPanel.add(leftSubPanel[i]);
 		}
 		
 		JLabel teamOneLabel = new JLabel("TEAM ONE!");
 		teamOneLabel.setBackground(ColorScheme.DEFAULT_SECONDARY);
 		teamOneLabel.setForeground(ColorScheme.DEFAULT_MAIN);
 		teamOneLabel.setFont(new Font("Mangal",Font.BOLD,24));
 		teamOneLabel.setHorizontalAlignment(JLabel.CENTER);
 		teamOneLabel.setVerticalAlignment(JLabel.CENTER);
 		teamOneLabel.setOpaque(true);
 		
 		leftSubLayout[0].putConstraint(SpringLayout.WEST, teamOneLabel,
 				15, SpringLayout.WEST,leftSubPanel[0]);
 		leftSubLayout[0].putConstraint(SpringLayout.NORTH, teamOneLabel,
 				0, SpringLayout.NORTH,leftSubPanel[0]);
 		leftSubLayout[0].putConstraint(SpringLayout.EAST, teamOneLabel,
 				-15, SpringLayout.EAST,leftSubPanel[0]);
 		leftSubLayout[0].putConstraint(SpringLayout.SOUTH, teamOneLabel,
 				-10, SpringLayout.SOUTH,leftSubPanel[0]);
 	
 		leftSubPanel[0].add(teamOneLabel);
 		
 		scoreButtonLeft = new AlyButton[4][4];
 		playerNamesLeft = new JLabel[4];
 		leftSuperSubPanel = new JPanel[4];		
 		removeButtonLeft = new AlyButton[4];
 		
 		for(int i=0;i<scoreButtonLeft.length;i++){
 						
 			playerNamesLeft[i] = new JLabel("   Player");
 			playerNamesLeft[i].setBackground(ColorScheme.DEFAULT_SECONDARY);
 			playerNamesLeft[i].setForeground(ColorScheme.DEFAULT_MAIN);
 			playerNamesLeft[i].setOpaque(true);
 			playerNamesLeft[i].setVisible(false);
 			playerNamesLeft[i].setVerticalAlignment(JLabel.TOP);
 			playerNamesLeft[i].setFont(new Font("Mangal",Font.PLAIN,16));
 			
 			leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, playerNamesLeft[i],
 					0, SpringLayout.NORTH,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.WEST, playerNamesLeft[i],
 					15, SpringLayout.WEST,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.EAST, playerNamesLeft[i],
 					-15, SpringLayout.EAST,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH, playerNamesLeft[i],
 					0, SpringLayout.SOUTH,leftSubPanel[i+1]);
 						
 			
 			leftSuperSubPanel[i] = new JPanel();
 			GridLayout g = new GridLayout(1,4);
 			g.setHgap(5);
 			leftSuperSubPanel[i].setLayout(g);
 			leftSuperSubPanel[i].setVisible(false);
 			leftSuperSubPanel[i].setBackground(ColorScheme.DEFAULT_SECONDARY);						
 			
 			leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, leftSuperSubPanel[i],
 				30, SpringLayout.NORTH,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH, leftSuperSubPanel[i],
 					-15, SpringLayout.SOUTH,leftSubPanel[i+1]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.WEST, leftSuperSubPanel[i],
 					10, SpringLayout.WEST,playerNamesLeft[i]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.EAST, leftSuperSubPanel[i],
 					-10, SpringLayout.EAST,playerNamesLeft[i]);
 			
 			removeButtonLeft[i] = new AlyButton("X",false);
 			removeButtonLeft[i].setActionCommand(i+"xl");
 			removeButtonLeft[i].setVisible(false);
 			removeButtonLeft[i].addActionListener(this);
 			removeButtonLeft[i].flipColors();
 			leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, removeButtonLeft[i],
 					0, SpringLayout.NORTH,playerNamesLeft[i]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH, removeButtonLeft[i],
 					0, SpringLayout.NORTH,leftSuperSubPanel[i]);
 			leftSubLayout[i+1].putConstraint(SpringLayout.EAST, removeButtonLeft[i],
 					-10, SpringLayout.EAST,playerNamesLeft[i]);
 				
 			leftSubPanel[i+1].add(removeButtonLeft[i]);			
 			
 			leftSubPanel[i+1].add(leftSuperSubPanel[i]);
 			leftSubPanel[i+1].add(playerNamesLeft[i]);
 		}
 
 		
 		for(int i=0;i<scoreButtonLeft.length;i++)
 			for(int j=0;j<scoreButtonLeft[0].length;j++){
 				scoreButtonLeft[i][j] = new AlyButton((j+1)*10+"");				
 				scoreButtonLeft[i][j].addActionListener(this);
 				scoreButtonLeft[i][j].setActionCommand(i+","+j+" l");
 				leftSuperSubPanel[i].add(scoreButtonLeft[i][j]);
 			}	
 		
 		//add the "+" buttons
 		plusButtonLeft = new AlyButton[4];
 		for(int i=0;i<plusButtonLeft.length;i++){
 			plusButtonLeft[i] = new AlyButton("+",false);
 			 plusButtonLeft[i].setActionCommand(i+"l+");
 			 plusButtonLeft[i].flipColors();
 			 plusButtonLeft[i].addActionListener(this);
 			 plusButtonLeft[i].setFont(new Font("Mangal",Font.PLAIN,40));
 			 
 			 leftSubLayout[i+1].putConstraint(SpringLayout.NORTH, plusButtonLeft[i],
 					0, SpringLayout.NORTH,leftSubPanel[i+1]);
 			 leftSubLayout[i+1].putConstraint(SpringLayout.WEST, plusButtonLeft[i],
 					15, SpringLayout.WEST,leftSubPanel[i+1]);
 			 leftSubLayout[i+1].putConstraint(SpringLayout.EAST, plusButtonLeft[i],
 					-15, SpringLayout.EAST,leftSubPanel[i+1]);
 			 leftSubLayout[i+1].putConstraint(SpringLayout.SOUTH,plusButtonLeft[i],
 					0, SpringLayout.SOUTH,leftSubPanel[i+1]);
 			 
 			 leftSubPanel[i+1].add(plusButtonLeft[i]);
 		}
 		leftTeam = new JLabel("SCORE: 0");
 		leftTeam.setBackground(ColorScheme.DEFAULT_MAIN);
 		leftTeam.setForeground(ColorScheme.DEFAULT_SECONDARY);
 		leftTeam.setFont(new Font("Mangal",Font.BOLD,23));
 		leftSubLayout[5].putConstraint(SpringLayout.NORTH, leftTeam,
 			0, SpringLayout.NORTH,leftSubPanel[5]);
 		leftSubLayout[5].putConstraint(SpringLayout.HORIZONTAL_CENTER, leftTeam,
 			0, SpringLayout.HORIZONTAL_CENTER,leftSubPanel[5]);
 		leftSubLayout[5].putConstraint(SpringLayout.SOUTH,leftTeam,
 			0, SpringLayout.SOUTH,leftSubPanel[5]);
 		leftSubPanel[5].add(leftTeam);		
 	}
 
 	public void actionPerformed(ActionEvent ev) {
 		String ac = ev.getActionCommand();
 		
 		if(ac.contains("+")){
 			Player p = chosenPlayer();
 			System.out.println(Integer.parseInt(ac.substring(0,1)));
 			if(p!=null){
 				if(ac.contains("l")){
 					plusButtonLeft[Integer.parseInt(ac.substring(0,1))].setVisible(false);
 					leftSuperSubPanel[Integer.parseInt(ac.substring(0,1))].setVisible(true);
 					playerNamesLeft[Integer.parseInt(ac.substring(0,1))].setVisible(true);
 					removeButtonLeft[Integer.parseInt(ac.substring(0,1))].setVisible(true);
 					playerNamesLeft[Integer.parseInt(ac.substring(0,1))].setText("   "+p.getName());
 					players[0][Integer.parseInt(ac.substring(0,1))] = p;
 						
 				}else{
 					plusButtonRight[Integer.parseInt(ac.substring(0,1))].setVisible(false);
 					rightSuperSubPanel[Integer.parseInt(ac.substring(0,1))].setVisible(true);
 					playerNamesRight[Integer.parseInt(ac.substring(0,1))].setVisible(true);
 					removeButtonRight[Integer.parseInt(ac.substring(0,1))].setVisible(true);
 					playerNamesRight[Integer.parseInt(ac.substring(0,1))].setText("   "+p.getName());
 					players[1][Integer.parseInt(ac.substring(0,1))] = p;
 				}					
 			}else
 				System.out.println("Cancelled");	
 			
 		}else if(ac.contains(",")){
 			int player = Integer.parseInt(ac.substring(0,1));
 			int score = (Integer.parseInt(ac.substring(ac.indexOf(",")+1,ac.indexOf(",")+2))+1)*10;
 			System.out.println("player index: "+player+" score: "+score);
 			int team = ac.endsWith("l")?0:1;
 			players[team][player].increaseScore(score);
 			
 			if(team == 0){
 				totalLeft+=score;
 				leftTeam.setText("SCORE: "+totalLeft);				
 			}else{
 				totalRight+=score;
 				rightTeam.setText("SCORE: "+totalRight);							
 			}
 		}else if(ac.contains("x")){
 			System.out.println("Player removed");
 			int player = Integer.parseInt(ac.substring(0,1));
 			int team = ac.endsWith("l")?0:1;
 			if(ac.endsWith("l")){
 				plusButtonLeft[player].setVisible(true);
 				leftSuperSubPanel[player].setVisible(false);
 				playerNamesLeft[player].setVisible(false);
 				removeButtonLeft[player].setVisible(false);
 			}else{
 				plusButtonRight[player].setVisible(true);
 				rightSuperSubPanel[player].setVisible(false);
 				playerNamesRight[player].setVisible(false);
 				removeButtonRight[player].setVisible(false);
 			}
 			PlayerIO.savePlayer(players[team][player]);
 		}
 	}
 
 	private Player chosenPlayer() {
 		LinkedList<String> temp = PlayerIO.getAllPlayers();
 		String list[];
 		
 		list = new String[temp.size()+1];
 		for(int i=0;i<list.length-1;i++)
 			list[i] = " "+temp.get(i);
 		list[list.length-1] = "zzzzzzzzzzzzzzzz";
 		Arrays.sort(list);
 		list[list.length-1] = " Create a new player!"; 
 									
 		String result = (String) JOptionPane.showInputDialog(this.getContentPane(),null,"Choose Player",JOptionPane.PLAIN_MESSAGE,null,list,0);
 		if(result==null)
 			return null;
 		int chosen = Arrays.binarySearch(list, result);
 				
		if(chosen < 0 || chosen == list.length-1){
 			
 			JLabel label = new JLabel("Who's the new player?");
 			label.setFont(new Font("Mangal",Font.BOLD,16));
 			label.setHorizontalAlignment(JLabel.CENTER);
 			
 			String s = (String) JOptionPane.showInputDialog(this.getContentPane(),label,"Add Player",JOptionPane.PLAIN_MESSAGE,null,null,null);
 			int check = Arrays.binarySearch(list, " "+s);
 	
 			if(check>=0){
 				label.setText("Already Exists");
 				JOptionPane.showMessageDialog(this.getContentPane(),label,"Not so fast",JOptionPane.PLAIN_MESSAGE,null);
 				return PlayerIO.getPlayer(list[check].trim());	
			}else if(s==null)
				return null;
			else if(s.isEmpty()){
 				label.setText("Invalid Name");
 				JOptionPane.showMessageDialog(this.getContentPane(),label,"Not so fast",JOptionPane.PLAIN_MESSAGE,null);
 				return null;
 			}
 				
 			PlayerIO.addPlayer(s);
 			
 			return PlayerIO.getPlayer(s);
 		}else
 			return PlayerIO.getPlayer(list[chosen].trim());			
 	}
 
 }
