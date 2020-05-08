 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
import javax.swing.GroupLayout.*;
 public class Five_UI extends JFrame{
 	private Five_Game fg;
 	Five_Board fb;
 	JPanel rightPanel1,rightPanel2;
 	JLabel LTurns;
 	JComboBox CBPlayer1,CBPlayer2,CBFirst;
 	JLabel LPlayer1,LPlayer2,LFirst;
 	JButton BRegret,BNew,BStart,BAI;
 	public void initStatus()
 	{
 		//LTurns.setText("Waiting for new game...");
 		this.CBPlayer1.setEnabled(true);
 		this.CBPlayer2.setEnabled(true);
 		this.CBFirst.setEnabled(true);
 		this.BStart.setEnabled(true);
 		this.BRegret.setEnabled(false);
 	}
 	public Five_UI(Five_Game f)
 	{
 		
 		super("Five Chess");
 		//this.setSize(1200,700);
 		fg=f;
 		String options1[] = {"Human","Computer"};
 		String options2[] = {"Player1","Player2"};
 		fb=new Five_Board(f);
 		//fb.setSize(500, 500);
 		JScrollPane jsp = new JScrollPane(fb);
 		jsp.setPreferredSize(new Dimension(600,600));
 		fb.setPreferredSize(new Dimension(1200,1200));
 		rightPanel1 = new JPanel();
 		rightPanel2 = new JPanel();
 		
 		CBPlayer1 = new JComboBox(options1);
 		CBPlayer1.addItemListener(new ItemListener()
 		{
 			public void itemStateChanged(ItemEvent event)
 			{
 				if(event.getStateChange()==ItemEvent.SELECTED)
 				{
 					fg.setPlayer(0, CBPlayer1.getSelectedIndex());
 				}
 			}
 		});
 		CBPlayer2 = new JComboBox(options1);
 		CBPlayer2.addItemListener(new ItemListener()
 		{
 			public void itemStateChanged(ItemEvent event)
 			{
 				if(event.getStateChange()==ItemEvent.SELECTED)
 				{
 					fg.setPlayer(1, CBPlayer2.getSelectedIndex());
 				}
 			}
 		});
 		CBFirst = new JComboBox(options2);
 		CBFirst.addItemListener(new ItemListener()
 		{
 			public void itemStateChanged(ItemEvent event)
 			{
 				if(event.getStateChange()==ItemEvent.SELECTED)
 				{
 					fg.first = CBFirst.getSelectedIndex();
 				}
 			}
 		});
 		LPlayer1 = new JLabel("Choose Player1(White) Type: ");
 		LPlayer2 = new JLabel("Choose Player2(Black) Type: ");
 		LFirst = new JLabel("Choose First Player: ");
 		LTurns = new JLabel("Waiting for new game...");
 		BAI = new JButton("AI HELP");
 		BAI.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				AIHelp();
 			}
 		});
 		BRegret = new JButton("Regret");
 		BRegret.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				Regret();
 			}
 		});
 		BNew = new JButton("New");
 		BNew.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				NewGame();
 			}
 		});
 		BStart = new JButton("Start");
 		BStart.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent event)
 			{
 				StartGame();
 			}
 		});
 		
 		
 		//BoxLayout vbox1 = new BoxLayout(rightPanel1,BoxLayout.Y_AXIS);
 		//BoxLayout vbox2 = new BoxLayout(rightPanel2,BoxLayout.Y_AXIS);
 		rightPanel1.setLayout(new GridBagLayout());
 		//setSize(600,800);
 		GridBagConstraints con = new GridBagConstraints();
 		con.fill = GridBagConstraints.NONE;
 
 		con.anchor = GridBagConstraints.LINE_START;
 		con.gridx=0;
 		con.gridy=0;
 		con.insets = new Insets(3,3,3,3);
 		con.ipady=4;
 		rightPanel1.add(LPlayer1,con);
 		con.gridx=1;
 		rightPanel1.add(CBPlayer1,con);
 		con.gridy=1;
 		rightPanel1.add(CBPlayer2,con);
 		con.gridx=0;
 		rightPanel1.add(LPlayer2,con);
 		con.gridx=0;
 		con.gridy=2;
 		
 		rightPanel1.add(LFirst,con);
 		con.gridy=2;
 		con.gridx=1;
 		con.fill = GridBagConstraints.HORIZONTAL;
 		rightPanel1.add(CBFirst,con);
 		
 		con.gridy=3;
 		con.gridx=0;
 		rightPanel1.add(BRegret,con);
 		con.gridx=1;
 		rightPanel1.add(BNew,con);
 		con.gridx=0;
 		con.gridy=4;
 
 		rightPanel1.add(BStart,con);
 		con.gridx=1;
 		rightPanel1.add(BAI,con);
 		con.gridx=0;
 		con.gridy = 5;
 		rightPanel1.add(LTurns,con);
 
 		
 		Container c = getContentPane();
 		
		BoxLayout hBox = new BoxLayout(c,BoxLayout.X_AXIS);
 		con.fill = GridBagConstraints.NONE;
 		con.anchor = GridBagConstraints.CENTER;
 		con.gridx=0;
 		con.gridy=0;
 		
 		c.setLayout(new GridBagLayout());
 		c.add(jsp,con);
 		con.gridx=1;
 		
 		con.anchor = GridBagConstraints.PAGE_START;
 		c.add(rightPanel1,con);
 		this.initStatus();
 		
 		pack();
 		
 		setVisible(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 	}
 	public void StartGame()
 	{
 		this.CBPlayer1.setEnabled(false);
 		this.CBPlayer2.setEnabled(false);
 		this.CBFirst.setEnabled(false);
 		this.BStart.setEnabled(false);
 		this.BRegret.setEnabled(true);
 		this.LTurns.setText("Player"+(fg.first+1)+"'s Turn...");
 		fg.StartGame();
 		fg.AIHelp();
 	}
 	public void NewGame()
 	{
 		//this.initStatus();
 		fg.NewGame();
 	}
 	public void Regret()
 	{
 		fg.Regret();
 		//this.fb.updateUI();
 	}
 	public void AIHelp()
 	{
 		fg.AIHelp();
 	}
 
 }
