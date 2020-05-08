 package ch.pajama.boredomkiller.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Container;
 import java.awt.Font;
 import java.awt.GraphicsEnvironment;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.LayoutManager;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollBar;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 import javax.swing.border.LineBorder;
 import javax.swing.border.TitledBorder;
 
 public class MainView extends JFrame implements ActionListener, MouseListener{
 
 	//GridBagLayout
 	private LayoutManager lm = new GridBagLayout();
 	private GridBagConstraints c = new GridBagConstraints();
 	
 	//JFrame contents
 	private JPanel mainPanel = new JPanel();
 	
 	//mainPanel contents
 	private JPanel filterPanel = new JPanel();
 	private JPanel miscPanel = new JPanel();
 	private JButton killButton = new JButton("Kill the Boredom!!");
 	private RainbowHover rh;
 	
 	//filterPanel contents
 	//Players stuff
 	private JLabel playersLabel = new JLabel("Players");
 	private ArrayList<String> playerListData = new ArrayList<String>();
 	private JList playerList = new JList();
 	private JScrollPane playerScrollPane = new JScrollPane(playerList);
 	private JButton playerAdd = new JButton("Add");
 	private JButton playerRemove = new JButton("Remove");
 	
 	//Modus stuff
 	private JLabel modusLabel = new JLabel("Modus");
 	private JCheckBox cbSingleplayer = new JCheckBox("Singleplayer");
 	private JCheckBox cbVersus = new JCheckBox("Versus");
 	private JCheckBox cbCoop = new JCheckBox("Co-op");
 	
 	//Genre stuff
 	private JLabel genreLabel = new JLabel("Genre");
 	private JPanel genrePanel = new JPanel();
 	private JScrollPane genreScrollPane = new JScrollPane(genrePanel);
 	private JButton genreCheck = new JButton("Check All");
 	private JButton genreUncheck = new JButton("Uncheck All");
 	private ArrayList<JCheckBox> genreListData = new ArrayList<JCheckBox>();
 	//Placeholders
 	//TODO delete those
 	private JCheckBox cb1 = new JCheckBox("Genre 1");
 	private JCheckBox cb2 = new JCheckBox("Genre 2");
 	private JCheckBox cb3 = new JCheckBox("Genre 3");
 	private JCheckBox cb4 = new JCheckBox("Genre 4");
 	private JCheckBox cb5 = new JCheckBox("Genre 5");
 	private JCheckBox cb6 = new JCheckBox("Genre 6");
 	private JCheckBox cb7 = new JCheckBox("Genre 7");
 	private JCheckBox cb8 = new JCheckBox("Genre 8");
 	private JCheckBox cb9 = new JCheckBox("Genre 9");
 	
 	//Platform stuff
 	private JLabel platformLabel = new JLabel("Platform");
 	private JPanel platformPanel = new JPanel();
 	private JScrollPane platformScrollPane = new JScrollPane(platformPanel);
 	private JButton platformCheck = new JButton("Check All");
 	private JButton platformUncheck = new JButton("Uncheck All");
 	private ArrayList<JCheckBox> platformListData = new ArrayList<JCheckBox>();
 	//Placeholders
 	//TODO delete those
 	private JCheckBox cbA = new JCheckBox("Platform 1");
 	private JCheckBox cbB = new JCheckBox("Platform 2");
 	private JCheckBox cbC = new JCheckBox("Platform 3");
 	private JCheckBox cbD = new JCheckBox("Platform 4");
 	private JCheckBox cbE = new JCheckBox("Platform 5");
 	private JCheckBox cbF = new JCheckBox("Platform 6");
 	private JCheckBox cbG = new JCheckBox("Platform 7");
 	private JCheckBox cbH = new JCheckBox("Platform 8");
 	private JCheckBox cbI = new JCheckBox("Platform 9");
 	
 	//miscPanel contents
 	private JLabel challengeLabel = new JLabel("Challenge Difficulty");
 	private JCheckBox cbEasy = new JCheckBox("Easy");
 	private JCheckBox cbMedium = new JCheckBox("Medium");
 	private JCheckBox cbHard = new JCheckBox("Hard");
 	
 	private JLabel challengeSettingLabel = new JLabel("Challenge Setting");
 	private JComboBox<String> challengeSettingCb = new JComboBox<String>(new String[]{"Globally", "Individually"});
 	
 	private JLabel malusLabel = new JLabel("Malus Enabled");
 	private JCheckBox cbMalus = new JCheckBox();
 	
 	//Dialog to add a Player
 	private JDialog newPlayer = new JDialog(this);
 	private JTextField newPlayerTf = new JTextField();
 	private JButton newPlayerOk = new JButton(" Let's a go! ");
 	private JButton newPlayerCancel = new JButton(" Cancel ");
 	
 	public MainView(){
 		setup();
 	}
 	
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		setupTheme();
 		new MainView();
 	}
 
 	private void setup(){
 		this.setSize(350, 600);
 		this.setLocation(200, 150);
 		this.setTitle("BoredomKiller I");
 		this.setResizable(false);
 		Image icon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB_PRE);
 		this.setIconImage(icon);
 
 		
 		this.add(mainPanel);
 		setupMainPanel();
 		
 		//Exit program on pressing the "X"
 		this.addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent we){
 				System.exit(0);
 			}
 		});
 		
 		this.setVisible(true);
 	}
 	
 	private void setupMainPanel(){
 		mainPanel.setLayout(lm);
 		setupFilterPanel();
 		addComponent(mainPanel, filterPanel, 0, 0, 1.0, 8.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, 1);
 		setupMiscPanel();
 		addComponent(mainPanel, miscPanel, 0, 1, 1.0, 2.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1);
 		killButton.setBackground(new Color(40, 40, 40));
 		killButton.setForeground(new Color(255, 255, 255));
 		killButton.addMouseListener(this);
 		addComponent(mainPanel, killButton, 0, 2, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 	}
 	
 	private void setupFilterPanel(){
 		filterPanel.setLayout(lm);
 		filterPanel.setBorder(new TitledBorder(new LineBorder(new Color(150, 150, 150)), "Filters"));
 		//Player
 		Color playerColor = new Color(150, 200, 255);
 		playersLabel.setForeground(playerColor);
 		addComponent(filterPanel, playersLabel, 0, 0, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1);
 		//TODO remove sample data
 		playerListData.add("Pincer");
 		playerListData.add("Kero");
 		playerListData.add("Myrion");
 		playerListData.add("Tschoppi");
 		playerListData.add("Amorpheus");
 		playerList.setForeground(playerColor);
 		playerList.setListData(playerListData.toArray());
 		playerScrollPane.setBorder(new LineBorder(new Color(150, 150, 150)));
 		addComponent(filterPanel, playerScrollPane, 1, 0, 1.0, 10.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 3);
 		playerAdd.addActionListener(this);
 		playerAdd.setBackground(playerColor);
 		playerAdd.setForeground(new Color(0, 50, 100));
 		addComponent(filterPanel, playerAdd, 2, 1, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 		playerRemove.addActionListener(this);
 		playerRemove.setBackground(playerColor);
 		playerRemove.setForeground(new Color(0, 50, 100));
 		addComponent(filterPanel, playerRemove, 2, 2, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 		//Modus
 		Color modusColor = new Color(220, 170, 255);
 		modusLabel.setForeground(modusColor);
 		addComponent(filterPanel, modusLabel, 0, 3, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1);
 		cbSingleplayer.setForeground(modusColor);
 		addComponent(filterPanel, cbSingleplayer, 1, 3, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 1);
 		cbVersus.setForeground(modusColor);
 		addComponent(filterPanel, cbVersus, 1, 4, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 1);
 		cbCoop.setForeground(modusColor);
 		addComponent(filterPanel, cbCoop, 1, 5, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 1);
 		//Genre
 		Color genreColor = new Color(255, 150, 200);
 		genreLabel.setForeground(genreColor);
 		addComponent(filterPanel, genreLabel, 0, 6, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1);
 		genrePanel.setLayout(new BoxLayout(genrePanel, BoxLayout.PAGE_AXIS));
 		genreListData.add(cb1);
 		genreListData.add(cb2);
 		genreListData.add(cb3);
 		genreListData.add(cb4);
 		genreListData.add(cb5);
 		genreListData.add(cb6);
 		genreListData.add(cb7);
 		genreListData.add(cb8);
 		genreListData.add(cb9);
 		
 		Iterator<JCheckBox> i = genreListData.iterator();
 		while(i.hasNext()){
 			JCheckBox tmp = i.next();
 			tmp.setForeground(genreColor);
 			genrePanel.add(tmp);
 		}
 		
 		genreScrollPane.setBorder(new LineBorder(new Color(150, 150, 150)));
 		addComponent(filterPanel, genreScrollPane, 1, 6, 1.0, 10.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 3);
 		genreCheck.addActionListener(this);
 		genreCheck.setBackground(genreColor);
 		genreCheck.setForeground(new Color(100, 0, 50));
 		addComponent(filterPanel, genreCheck, 2, 7, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 		genreUncheck.addActionListener(this);
 		genreUncheck.setBackground(genreColor);
 		genreUncheck.setForeground(new Color(100, 0, 50));
 		addComponent(filterPanel, genreUncheck, 2, 8, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 		//Platform
 		Color platformColor = new Color(255, 200, 150);
 		platformLabel.setForeground(platformColor);
 		addComponent(filterPanel, platformLabel, 0, 9, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1);
 		platformPanel.setLayout(new BoxLayout(platformPanel, BoxLayout.PAGE_AXIS));
 		platformListData.add(cbA);
 		platformListData.add(cbB);
 		platformListData.add(cbC);
 		platformListData.add(cbD);
 		platformListData.add(cbE);
 		platformListData.add(cbF);
 		platformListData.add(cbG);
 		platformListData.add(cbH);
 		platformListData.add(cbI);
 		
 		i = platformListData.iterator();
 		while(i.hasNext()){
 			JCheckBox tmp = i.next();
 			tmp.setForeground(platformColor);
 			platformPanel.add(tmp);
 		}
 		
 		platformScrollPane.setBorder(new LineBorder(new Color(150, 150, 150)));
 		addComponent(filterPanel, platformScrollPane, 1, 9, 1.0, 10.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 3);
 		platformCheck.addActionListener(this);
 		platformCheck.setBackground(platformColor);
 		platformCheck.setForeground(new Color(100, 50, 0));
 		addComponent(filterPanel, platformCheck, 2, 10, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 		platformUncheck.addActionListener(this);
 		platformUncheck.setBackground(platformColor);
 		platformUncheck.setForeground(new Color(100, 50, 0));
 		addComponent(filterPanel, platformUncheck, 2, 11, 1.0, 1.0, GridBagConstraints.SOUTH, GridBagConstraints.BOTH, 1);
 	}
 	
 	private void setupMiscPanel(){
 		miscPanel.setLayout(lm);
 		miscPanel.setBorder(new TitledBorder(new LineBorder(new Color(150, 150, 150)), "Misc"));
 		
 		Color challengeColor = new Color(255, 255, 150);
 		challengeLabel.setForeground(challengeColor);
 		addComponent(miscPanel, challengeLabel, 0, 0, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 10), 1);
 		cbEasy.setForeground(challengeColor);
 		addComponent(miscPanel, cbEasy, 1, 0, 1.0, 1.0, GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, 1);
 		cbMedium.setForeground(new Color(255, 205, 150));
 		addComponent(miscPanel, cbMedium, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, 1);
 		cbHard.setForeground(new Color(255, 155, 150));
 		addComponent(miscPanel, cbHard, 1, 2, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, 1);
 		
 		challengeSettingLabel.setForeground(challengeColor);
 		addComponent(miscPanel, challengeSettingLabel, 0, 3, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 10), 1);
 		challengeSettingCb.setForeground(challengeColor);
 		addComponent(miscPanel, challengeSettingCb, 1, 3, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 1);
 		
 		malusLabel.setForeground(challengeColor);
 		addComponent(miscPanel, malusLabel, 0, 4, 1.0, 1.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 0, 10), 1);
 		addComponent(miscPanel, cbMalus, 1, 4, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 0, 0, 0), 1);
 	}
 	
 	private static void setupTheme(){
 		try {
 	        javax.swing.UIManager.setLookAndFeel("com.jtattoo.plaf.hifi.HiFiLookAndFeel");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Adds a Component to given Container
 	 * @param con Container to add the Component to
 	 * @param toadd Component to add
 	 * @param gridx where to add the Component on the x-axis
 	 * @param gridy	where to add the Component on the y-axis
 	 * @param weightx weight of the Component on the x-axis
 	 * @param weighty weight of the Component on the y-axis
 	 * @param anchor where the anchor is set for the Component
 	 * @param fill how the Component fills
 	 * @param gridheight sets the Components height
 	 */
 	private void addComponent(Container con, Component toadd, int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int gridheight, int gridwidth){
 		c.gridx = gridx;
 		c.gridy = gridy;
 		c.weightx = weightx;
 		c.weighty = weighty;
 		c.anchor = anchor;
 		c.fill = fill;
 		c.gridheight = gridheight;
 		c.gridwidth = gridwidth;
 		
 		c.insets = new Insets(0, 0, 0, 0);
 		
 		con.add(toadd, c);
 	}
 	
 	/**
 	 * Adds a Component to given Container
 	 * @param con Container to add the Component to
 	 * @param toadd Component to add
 	 * @param gridx where to add the Component on the x-axis
 	 * @param gridy	where to add the Component on the y-axis
 	 * @param weightx weight of the Component on the x-axis
 	 * @param weighty weight of the Component on the y-axis
 	 * @param anchor where the anchor is set for the Component
 	 * @param fill how the Component fills
 	 * @param gridheight sets the Components height
 	 * @param gridwidth sets the Components width
 	 */
 	private void addComponent(Container con, Component toadd, int gridx, int gridy, double weightx, double weighty, int anchor, int fill, Insets insets, int gridheight){
 		c.gridx = gridx;
 		c.gridy = gridy;
 		c.weightx = weightx;
 		c.weighty = weighty;
 		c.anchor = anchor;
 		c.fill = fill;
 		c.insets = insets;
 		c.gridheight = gridheight;
 		
 		con.add(toadd, c);
 	}
 	
 	/**
 	 * Adds a Component to given Container
 	 * @param con Container to add the Component to
 	 * @param toadd Component to add
 	 * @param gridx where to add the Component on the x-axis
 	 * @param gridy	where to add the Component on the y-axis
 	 * @param weightx weight of the Component on the x-axis
 	 * @param weighty weight of the Component on the y-axis
 	 * @param anchor where the anchor is set for the Component
 	 * @param fill how the Component fills
 	 * @param gridheight sets the Components height
 	 * @param gridwidth sets the Components width
 	 */
 	private void addComponent(Container con, Component toadd, int gridx, int gridy, double weightx, double weighty, int anchor, int fill, int gridheight){
 		c.gridx = gridx;
 		c.gridy = gridy;
 		c.weightx = weightx;
 		c.weighty = weighty;
 		c.anchor = anchor;
 		c.fill = fill;
 		c.gridheight = gridheight;
 		
 		c.insets = new Insets(0, 0, 0, 0);
 		c.gridwidth = 1;
 		
 		con.add(toadd, c);
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if(ae.getSource().equals(playerAdd)){
 			//TODO Colour Scheme
 			newPlayer.setSize(300, 100);
 			newPlayer.setLocation(this.getLocation().x + 20, this.getLocation().y + 120);
 			newPlayer.setTitle("A Challenger has appeared!");
 			newPlayer.setResizable(false);
 			newPlayer.setModal(true);
 			newPlayer.setLayout(lm);
 
 			newPlayerCancel.addMouseListener(this);
 			newPlayerCancel.addActionListener(this);
 			newPlayerOk.addActionListener(this);
 			
 			addComponent(newPlayer, newPlayerTf, 0, 0, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, 1, 2);
 			addComponent(newPlayer, newPlayerOk, 0, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
 			addComponent(newPlayer, newPlayerCancel, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, 1, 1);
 			
 			newPlayer.setVisible(true);
 		} else if (ae.getSource().equals(playerRemove)){
 			if(playerList.getSelectedIndex() != -1){
 				playerListData.remove(playerList.getSelectedIndex());
 				playerList.setListData(playerListData.toArray());
 			}
 		} else if (ae.getSource().equals(newPlayerOk)){
 			String playername = newPlayerTf.getText();
 			if(!playername.equals("") && !playerListData.contains(playername)){
				playerListData.add(playername);
 				playerList.setListData(playerListData.toArray());
 				newPlayer.setVisible(false);
 				newPlayerTf.setText("");
 			}
			else{
				System.out.println("Can't let you do that, maggot!");
			}
 		} else if (ae.getSource().equals(newPlayerCancel)){
 			newPlayer.setVisible(false);
 			newPlayerTf.setText("");
 		} else if (ae.getSource().equals(genreCheck)){
 			Iterator<JCheckBox> i = genreListData.iterator();
 			while(i.hasNext()){
 				i.next().setSelected(true);
 			}
 		} else if (ae.getSource().equals(genreUncheck)){
 			Iterator<JCheckBox> i = genreListData.iterator();
 			while(i.hasNext()){
 				i.next().setSelected(false);
 			}
 		} else if (ae.getSource().equals(platformCheck)){
 			Iterator<JCheckBox> i = platformListData.iterator();
 			while(i.hasNext()){
 				i.next().setSelected(true);
 			}
 		} else if (ae.getSource().equals(platformUncheck)){
 			Iterator<JCheckBox> i = platformListData.iterator();
 			while(i.hasNext()){
 				i.next().setSelected(false);
 			}
 		}
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void mouseEntered(MouseEvent me) {
 		if(me.getSource().equals(newPlayerCancel)){
 			newPlayerCancel.setText(" YOU PUSSY! ");
 		}
 		if(me.getSource().equals(killButton)){
 			if(rh == null){
 				 rh = new RainbowHover();
 			}
 			rh.go();
 		}
 	}
 
 	@Override
 	public void mouseExited(MouseEvent me) {
 		if(me.getSource().equals(newPlayerCancel)){
 			newPlayerCancel.setText(" Cancel ");
 		}
 		if(me.getSource().equals(killButton)){
 			rh.halt();
 			killButton.setBackground(new Color(40, 40, 40));
 			killButton.setForeground(new Color(255, 255, 255));
 		}
 	}
 
 	@Override
 	public void mousePressed(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	private class RainbowHover extends Thread{
 		private boolean on = false;
 		private int red = 200;
 		private int green = 100;
 		private int blue = 0;
 		boolean redup = false;
 		boolean greenup = true;
 		boolean blueup = true;
 		
 		public RainbowHover(){
 			start();
 		}
 		
 		public void go(){
 			on = true;
 		}
 		
 		public void halt() {
 	        on = false;
 	        red = 200;
 	        green = 100;
 	        blue = 0;
 	        redup = false;
 	        greenup = true;
 	        blueup = true;
 	    }
 		
 		public void run(){
 			while(true){
 				if(on){
 					if(red == 0){
 						redup = true;
 					}
 					else if(red == 200){
 						redup = false;
 					}
 					
 					if(green == 0){
 						greenup = true;
 					}
 					else if(green == 200){
 						greenup = false;
 					}
 					
 					if(blue == 0){
 						blueup = true;
 					}
 					else if(blue == 200){
 						blueup = false;
 					}
 					
 					if(redup){
 						red++;
 					}
 					else{
 						red--;
 					}
 					
 					if(greenup){
 						green++;
 					}
 					else{
 						green--;
 					}
 					
 					if(blueup){
 						blue++;
 					}
 					else{
 						blue--;
 					}
 					
 					killButton.setBackground(new Color(red, green, blue));
 					killButton.setForeground(new Color(255-red, 255-green, 255-blue));
 					try{
 						sleep(2);
 					}
 					catch(Exception e){
 						e.printStackTrace();
 					}
 				}
 				else{
 					try{
 						sleep(2);
 					}
 					catch(Exception e){
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 }
