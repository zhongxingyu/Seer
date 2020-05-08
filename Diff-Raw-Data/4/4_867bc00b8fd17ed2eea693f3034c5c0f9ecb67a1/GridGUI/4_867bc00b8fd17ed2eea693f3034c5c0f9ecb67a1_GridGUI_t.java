 package de.haw_hamburg.inf.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.SwingConstants;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import de.haw_hamburg.inf.environment.GWorld;
 import de.haw_hamburg.inf.rl.Agent;
 
 public class GridGUI implements Observer {
 
     private JFrame            frame;
     private JPanel            gridPanel;
     JPanel[][]                grid;
 
     private int[]             dimension   = { 10, 5, 4 };
     private GWorld            gw          = new GWorld(dimension);
     private Agent             agent       = new Agent(gw);
     private GridControl       gc          = new GridControl(gw, agent);
 
     /**
      * Launch the application.
      */
     static GridGUI            window;
     private JTextField        txtTargethoptime;
     private JToggleButton     btnStart;
     private final ButtonGroup buttonGroup = new ButtonGroup();
     private int[]             agentPos    = { 0, 0 };
 
     public static void main(String[] args) {
 
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                     window = new GridGUI();
                     window.frame.setVisible(true);
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         });
     }
 
     /**
      * Create the application.
      */
     public GridGUI() {
         initialize();
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize() {
         frame = new JFrame();
         frame.setResizable(false);
         frame.getContentPane().setLayout(null);
         frame.setSize(800, 400);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         gridPanel = new JPanel();
         gridPanel.setBounds(248, 13, 522, 262);
         frame.getContentPane().add(gridPanel);
         gridPanel.setLayout(new GridLayout(dimension[1],
                 dimension[0], 0, 0));
 
         // INIT GRID
         grid = new JPanel[dimension[0]][dimension[1]];
         JLabel[] Qvalues = new JLabel[4 * dimension[0] * dimension[1]];
         int boundCnt = -1;
         for (int i = 0; i < Qvalues.length; i++) {
             Qvalues[i] = new JLabel("0");
             Qvalues[i].setFont(new Font("Tahoma", Font.PLAIN, 10));
             Qvalues[i].setHorizontalAlignment(SwingConstants.CENTER);
             boundCnt++;
             switch (boundCnt) {
                 case 0:
                     Qvalues[i].setBounds(1, 1, 13, 51);
                     break;
                 case 1:
                     Qvalues[i].setBounds(39, 1, 13, 51);
                     break;
                 case 2:
                     Qvalues[i].setBounds(0, 1, 52, 16);
                     break;
                 case 3:
                     Qvalues[i].setBounds(1, 36, 51, 16);
                     boundCnt = -1;
                     break;
             }
         }
 
         int x, y, i, l = 0;
         for (y = 0; y < dimension[1]; y++) {
             for (x = 0; x < dimension[0]; x++) {
                 grid[x][y] = new JPanel();
                 grid[x][y].setLayout(null);
                 grid[x][y].setForeground(Color.BLACK);
                 grid[x][y].setBorder(new LineBorder(
                         new Color(0, 0, 0)));
                 grid[x][y].setBackground(Color.WHITE);
                 for (i = 0; i < 4; i++) {
                     grid[x][y].add(Qvalues[l++]);
                 }
                 if (x == dimension[0] - 1) {
                     grid[x][y].setBackground(Color.GREEN);
                     final JLabel lblTarget = new JLabel("T");
                     grid[x][y].add(lblTarget);
                     grid[x][y]
                             .setName(Integer.toString((y + 1) * 10));
                     setUpTargetLabel(lblTarget);
                     if (y == dimension[1] - 1)
                         lblTarget.setEnabled(true);
                 }
                 gridPanel.add(grid[x][y]);
             }
         }
 
         // agentPos: BLUE
         grid[agentPos[0]][agentPos[1]].setBackground(Color.BLUE);
 
         txtTargethoptime = new JTextField();
         txtTargethoptime.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 gc.setTargetHopTime(Integer.parseInt(txtTargethoptime
                         .getText()));
             }
         });
         txtTargethoptime.setHorizontalAlignment(SwingConstants.RIGHT);
         txtTargethoptime.setText("2");
         txtTargethoptime.setBounds(383, 311, 59, 22);
         frame.getContentPane().add(txtTargethoptime);
         txtTargethoptime.setColumns(10);
 
         JLabel lblTargetHopTime = new JLabel("Target hop Time");
         lblTargetHopTime.setBounds(383, 288, 116, 16);
         frame.getContentPane().add(lblTargetHopTime);
 
         JLabel lblSeconds = new JLabel("Seconds");
         lblSeconds.setBounds(454, 314, 56, 16);
         frame.getContentPane().add(lblSeconds);
 
         btnStart = new JToggleButton("Start");
         btnStart.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 if (btnStart.isSelected()) {
                     btnStart.setText("Stop");
                     gc.addObserver(window);
                     agent.addObserver(window);
                     agent.setExplorationRate(Integer
                             .parseInt(txtExploration.getText()));
                     gc.setEpisodes(Integer.parseInt(txtEpisodes
                             .getText()));
                     Thread t = new Thread(gc);
                     t.start();
                 } else {
                     gc.terminate();
                     btnStart.setText("Start");
                 }
             }
         });
         btnStart.setBounds(522, 310, 97, 25);
         frame.getContentPane().add(btnStart);
 
         JRadioButton rdbtnFixedTarget = new JRadioButton(
                 "fixed target");
         buttonGroup.add(rdbtnFixedTarget);
         rdbtnFixedTarget.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 gc.setMovingTarget(false);
                 gc.setCirclingTarget(false);
             }
         });
         rdbtnFixedTarget.setBounds(248, 284, 127, 25);
         rdbtnFixedTarget.setSelected(true);
         frame.getContentPane().add(rdbtnFixedTarget);
 
         JRadioButton rdbtnCirclingTarget = new JRadioButton(
                 "circling target");
         buttonGroup.add(rdbtnCirclingTarget);
         rdbtnCirclingTarget.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 gc.setCirclingTarget(true);
             }
         });
         rdbtnCirclingTarget.setBounds(248, 310, 127, 25);
         frame.getContentPane().add(rdbtnCirclingTarget);
 
         txtEpisodes = new JTextField();
         txtEpisodes.setHorizontalAlignment(SwingConstants.RIGHT);
         txtEpisodes.setText("1");
         txtEpisodes.setBounds(80, 13, 156, 16);
         frame.getContentPane().add(txtEpisodes);
         txtEpisodes.setColumns(10);
 
         JLabel lblEpisodes = new JLabel("Episodes");
         lblEpisodes.setBounds(12, 13, 56, 16);
         frame.getContentPane().add(lblEpisodes);
 
         txtAlpha = new JTextField();
         txtAlpha.setText("1");
         txtAlpha.setHorizontalAlignment(SwingConstants.RIGHT);
         txtAlpha.setBounds(80, 42, 156, 16);
         frame.getContentPane().add(txtAlpha);
         txtAlpha.setColumns(10);
 
         JLabel lblAlpha = new JLabel("Alpha");
         lblAlpha.setBounds(12, 42, 56, 16);
         frame.getContentPane().add(lblAlpha);
 
         txtGamma = new JTextField();
         txtGamma.setText("0.9");
         txtGamma.setHorizontalAlignment(SwingConstants.RIGHT);
         txtGamma.setBounds(80, 71, 156, 16);
         frame.getContentPane().add(txtGamma);
         txtGamma.setColumns(10);
 
         JLabel lblGamma = new JLabel("Gamma");
         lblGamma.setBounds(12, 71, 56, 16);
         frame.getContentPane().add(lblGamma);
 
         JSlider sliderSpeed = new JSlider();
         sliderSpeed.addChangeListener(new ChangeListener() {
             public void stateChanged(ChangeEvent e) {
                 JSlider source = (JSlider) e.getSource();
                 if (!source.getValueIsAdjusting()) {
                     int speed = (int) source.getValue();
                     agent.setLearningSpeed(speed * 100);
                 }
             }
         });
         sliderSpeed.setValue(0);
         sliderSpeed.setMinorTickSpacing(1);
         sliderSpeed.setMaximum(20);
         sliderSpeed.setBounds(12, 129, 224, 26);
         frame.getContentPane().add(sliderSpeed);
 
         JLabel lblLearningSpeed = new JLabel("Learning speed");
         lblLearningSpeed.setBounds(12, 100, 97, 16);
         frame.getContentPane().add(lblLearningSpeed);
 
         JLabel lblNumberOfSteps = new JLabel(
                 "Number of steps in best Episode:");
         lblNumberOfSteps.setBounds(12, 168, 206, 16);
         frame.getContentPane().add(lblNumberOfSteps);
 
         txtSteps = new JTextField();
         txtSteps.setHorizontalAlignment(SwingConstants.CENTER);
         txtSteps.setText(Integer.toString(Integer.MAX_VALUE));
         txtSteps.setEditable(false);
         txtSteps.setBounds(12, 197, 97, 16);
         frame.getContentPane().add(txtSteps);
         txtSteps.setColumns(10);
         txtSteps.setVisible(false);
 
         JButton btnResetLearning = new JButton("Reset learning");
         btnResetLearning.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 gc.terminate();
                 gw.resetState();
                 agent.resetAgent();
                 agent.resetLearning();
                 btnStart.setText("Start");
                 btnStart.setSelected(false);
             }
         });
         btnResetLearning.setBounds(631, 310, 139, 25);
         frame.getContentPane().add(btnResetLearning);
 
         txtExploration = new JTextField();
         txtExploration.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 agent.setExplorationRate(Integer
                         .parseInt(txtExploration.getText()));
             }
         });
         txtExploration.setHorizontalAlignment(SwingConstants.RIGHT);
         txtExploration.setText("15");
         txtExploration.setBounds(121, 259, 97, 16);
         frame.getContentPane().add(txtExploration);
         txtExploration.setColumns(10);
 
         JLabel lblExplorationRate = new JLabel("Exploration rate");
         lblExplorationRate.setBounds(12, 259, 97, 16);
         frame.getContentPane().add(lblExplorationRate);
 
         JLabel label = new JLabel("%");
         label.setBounds(219, 259, 12, 16);
         frame.getContentPane().add(label);
 
         lblBest = new JLabel("best (= 13) after ");
         lblBest.setVerticalAlignment(SwingConstants.TOP);
         lblBest.setBounds(12, 226, 224, 16);
         frame.getContentPane().add(lblBest);
     }
 
     /**
      * @param lblTarget
      */
     private void setUpTargetLabel(final JLabel lblTarget) {
         lblTarget.addMouseListener(new MouseAdapter() {
             @Override
             public void mouseClicked(MouseEvent e) {
                 if (e.getButton() == MouseEvent.BUTTON1) {
                     gc.terminate();
                     togglePanel(Integer.parseInt(lblTarget
                             .getParent().getName()));
                 }
             }
         });
         lblTarget.setEnabled(false);
         lblTarget.setHorizontalAlignment(SwingConstants.CENTER);
         lblTarget.setForeground(Color.RED);
         lblTarget.setFont(new Font("Comic Sans MS", Font.BOLD, 15));
         lblTarget.setBackground(Color.ORANGE);
         lblTarget.setBounds(10, 17, 30, 16);
     }
 
     /**
      * TARGET PANEL LIST FOR SWITCH
      * 
      * @param panelNr
      */
     public void togglePanel(int panelNr) {
         turnOtherPanelOff();
         switch (panelNr) {
             case 10:
                 grid[9][0].getComponent(4)
                         .setEnabled(
                                 grid[9][0].getComponent(4)
                                         .isEnabled() ? false : true);
                 break;
             case 20:
                 grid[9][1].getComponent(4)
                         .setEnabled(
                                 grid[9][1].getComponent(4)
                                         .isEnabled() ? false : true);
                 break;
             case 30:
                 grid[9][2].getComponent(4)
                         .setEnabled(
                                 grid[9][2].getComponent(4)
                                         .isEnabled() ? false : true);
                 break;
             case 40:
                 grid[9][3].getComponent(4)
                         .setEnabled(
                                 grid[9][3].getComponent(4)
                                         .isEnabled() ? false : true);
                 break;
             case 50:
                 grid[9][4].getComponent(4)
                         .setEnabled(
                                 grid[9][4].getComponent(4)
                                         .isEnabled() ? false : true);
                 break;
             default:
                 break;
         }
     }
 
     private void turnOtherPanelOff() {
         for (int y = 0; y < grid[9].length; y++) {
             if (grid[9][y].getComponent(4).isEnabled()) {
                 grid[9][y].getComponent(4).setEnabled(false);
             }
         }
     }
 
     public Component getFrame() {
         return frame;
     }
 
     Color              savedColor     = Color.WHITE;
     private JTextField txtEpisodes;
     private JTextField txtAlpha;
     private JTextField txtGamma;
     private int        stepCounter    = 0;
     private JTextField txtSteps;
     private JTextField txtExploration;
     private int        episodeCounter = 0;
     private boolean    bestWay;
     private JLabel     lblBest;
 
     @Override
     public void update(Observable o, Object arg) {
         if (o instanceof GridControl) {
             togglePanel((int) arg);
             if ((int) arg == -1) {
                 btnStart.setText("Start");
                 btnStart.setSelected(false);
             }
         } else {
             moveAgent((int[]) arg);
             if (((Agent) o).episodeEnded()) {
                 savedColor = Color.WHITE;
                 grid[9][4].setBackground(Color.GREEN);
                 System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>STEPS: "
                         + stepCounter);
                 if (Integer.parseInt(txtSteps.getText()) > stepCounter) {
                     txtSteps.setText(Integer.toString(stepCounter));
                     txtSteps.setVisible(true);
                 } else {
                     // Steps in last episode
                 }
                 if (stepCounter == 13 && !bestWay) {
                     bestWay = true;
                     lblBest.setText(lblBest.getText() + "\n"
                             + episodeCounter + " episodes");
                 }
                 episodeCounter++;
                 stepCounter = 0;
             } else {
                 stepCounter++;
             }
         }
     }
 
     private void moveAgent(int[] arg) {
         grid[agentPos[0]][agentPos[1]].setBackground(savedColor);
         savedColor = grid[arg[0]][arg[1]].getBackground();
         agentPos = arg;
         grid[arg[0]][arg[1]].setBackground(Color.BLUE);
     }
 }
