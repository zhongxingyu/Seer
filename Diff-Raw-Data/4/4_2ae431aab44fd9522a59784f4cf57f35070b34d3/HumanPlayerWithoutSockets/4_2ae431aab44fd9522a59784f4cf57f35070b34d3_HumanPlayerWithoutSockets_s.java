 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.AffineTransform;
 import java.text.DecimalFormat;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JApplet;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.border.BevelBorder;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 /**
  * Spawned from the Simulator after "Play Human" option is clicked Gets host,
  * port, nGambes (and the datafile name) from the Simulator at construction
  * Creates GUI for a user to type in name and start, when game starts and it's
  * user's turn, they can select parameters to send in their bets for each gamble
  * game continues until the user quits.
  * 
  * @author ajk377
  * 
  */
 public class HumanPlayerWithoutSockets {
   UserWindow userWindow;
   String name;
   private final int nGambles;
   private static final int windowWidth = 400;
   private static final int sliderHeight = 30;
   private SimulatorWithoutSockets sim;
   private JApplet app;
   public HumanPlayerWithoutSockets(int nGambles, SimulatorWithoutSockets sim,
       JApplet app){
     this.nGambles = nGambles;
     System.out.println(nGambles);
     this.sim = sim;
     this.name="Human Player";
     this.app = app;
     // set up GUI
     // make GUI to get user input
     userWindow = new UserWindow();
     userWindow.setSize(windowWidth, 65+nGambles*(sliderHeight+35)); //100 for the top 
     userWindow.setBackground(Color.WHITE);
     userWindow.setLocation(850, 0);
     userWindow.setVisible(true);
   }
   public Component getGUI(){
     return userWindow;
   }
 
   /**
    * Called when userWindow's start button gets pushed (after user registers his
    * name)
    * 
    * @param host
    * @param port
    */
   private void play(Double[] allocs) {
     if (connectionEstablished) {
     // play=send the results
       String out = convertToString(nGambles, allocs);
       sim.newInputFromClient(this, out);
       //TODO: send it to simulator
     }
   }
   private void sendName(){
 	  if(connectionEstablished){
 		  sim.newInputFromClient(this, name);
 	  }
   }
 
   public void connect(String name) {
     // connect & send name
     System.out.println("connecting");
     sim.newClient(this);
     //TODO:write this
     
     connectionEstablished = true;
   }
 
   boolean connectionEstablished = false;
 
   public String convertToString(int nGambles, Double[] allocs) {
     DecimalFormat df = new DecimalFormat("0.000");
     StringBuffer sb = new StringBuffer(nGambles * 8);
     for (int j = 0; j < nGambles; j++)
       sb.append(df.format(allocs[j])).append(" ");
     String out = sb.toString();
     System.out.println("sending: " + out);
     return out;
   }
 
   public static void main(String[] args) throws Exception {
     if (args.length != 1) {
       System.out.println("usage:  Java HumanPlayerWithoutSocket"
           + "<Ngambles>");
       System.exit(1);
     }
     new HumanPlayerWithoutSockets(Integer.parseInt(args[0]), 
         new SimulatorWithoutSockets(new JApplet()), new JApplet());
   }
 
   class UserWindow extends JPanel {
     private static final long serialVersionUID = 1L;
     Font f1 = new Font("Dialog", Font.BOLD, 12);
     Font f2 = new Font("Dialog", Font.PLAIN, 12);
 
     FontMetrics fm = getFontMetrics(f2);
 
     JButton bPlay, bSetName, bStart;
     JTextField nameField;
     ResultsPanel pInputs;
 
     int idxOrder = -1;
     double[] rets = new double[nGambles];
     double[] drs = new double[nGambles];
     Color[] colors = new Color[nGambles];
 
     AffineTransform atVert = AffineTransform.getRotateInstance(-Math.PI / 2);
     String name;
 
     public UserWindow() {
       //super("Human Player");
       this.name = "Human Player";
       buildGUI();
       /*addWindowListener(new WindowAdapter() {
         public void windowClosing(WindowEvent e) {
           System.exit(0);
         }
       });*/
     }
     JLabel lname;
 
     void buildGUI() {
       lname = new JLabel("Name: " + name);
       JLabel lAttrs = new JLabel("Game with " + nGambles + " gambles");
       lname.setFont(f1);
       lAttrs.setFont(f2);
       bStart = new JButton("Start");
       bPlay = new JButton("Send");
       bSetName = new JButton("Set Name");
       nameField = new JTextField("Type your name", 10);
       bPlay.setFont(f1);
       bSetName.setFont(f1);
       bSetName.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           String newName = nameField.getText();
           System.out.println(nameField.getText());
           if (!newName.equals("Type your name")) {
            name = newName;
            lname.setText("Name: " + name);            
             repaint();
           }
         }
       });
       bStart.setFont(f1);
       bPlay.setEnabled(false);
       bStart.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
           // get name from the text area or something, set it to
           // makes panel like selectable(ungray it)
           bPlay.setEnabled(true);
           bStart.setEnabled(false);
           bSetName.setEnabled(false);
           connect(name);
           sendName();
         }
       });
       bPlay.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
         	// get contents from the panel
         	// if turn..?
           Double[] bets = pInputs.getBets();
           play(bets);
         }
       });
       JPanel pTop = new JPanel();
       pTop.setLayout(new BoxLayout(pTop, BoxLayout.Y_AXIS));
       lAttrs.setAlignmentX(Component.CENTER_ALIGNMENT);
       pTop.add(lAttrs);
       lname.setAlignmentX(Component.CENTER_ALIGNMENT);
       pTop.add(lname);
       nameField.setAlignmentX(Component.CENTER_ALIGNMENT);
       pTop.add(nameField);
       bSetName.setAlignmentX(CENTER_ALIGNMENT);
       pTop.add(bSetName);
       pTop.setPreferredSize(new Dimension(100, 100));
       JPanel pMid = new JPanel();
       pMid.setLayout(new FlowLayout());
       pMid.add(bPlay);
       pMid.add(bStart);
       pInputs = new ResultsPanel(nGambles);
       pInputs.setBackground(Color.white);
       pInputs.setPreferredSize(new Dimension(380, nGambles*60));
       Box boxNorth = Box.createVerticalBox();
       //boxNorth.setPreferredSize(new Dimension(nGambles*100+10, 65));
       boxNorth.add(pTop);
       boxNorth.add(Box.createVerticalStrut(5));
       boxNorth.add(pMid);
       boxNorth.add(Box.createVerticalStrut(5));
       boxNorth.add(pInputs);
       
      JPanel pane = new JPanel();
       pane.setLayout(new BorderLayout());
       pane.setBorder(new EmptyBorder(5, 5, 5, 5));
       pane.add(boxNorth, BorderLayout.NORTH);
       
       app.getContentPane().add(pane);
     }
 
   }
 
   static class MySlider extends JSlider {
     // num is an index into the bets/slids array
     int num;
     static Font f2 = new Font("Dialog", Font.PLAIN, 12);
 
     public MySlider() {
     	super();
     	this.setMajorTickSpacing(10);
     	this.setMinorTickSpacing(1);
     	this.setFont(f2);
     	this.setPaintTicks(true);
     	this.setPaintLabels(true);
     	repaint();
     }
     
     public void setNum(int num) {
       this.num = num;
     }
 
     public int getNum() {
       return this.num;
     }
 
   }
 
   static class ResultsPanel extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	Double[] bets;
     MySlider[] slids;
     int nGambles;
 
     public ResultsPanel(int nGambles) {
       super();
       //setPreferredSize(new Dimension(200, 100));
       setBorder(new BevelBorder(BevelBorder.LOWERED));
       this.nGambles = nGambles;
       this.bets = new Double[nGambles];
       this.slids = new MySlider[nGambles];
       setBets();
     }
 
     public void setBets() {
       for (int i = this.nGambles - 1; i >= 0; i--) {
         // make a slider for each gamble
         slids[i] = new MySlider(); // horiz with 1-100 def 50
         slids[i].setNum(i);
         slids[i].addChangeListener(sliderChanged);
         slids[i].setMajorTickSpacing(10);
         slids[i].setMinorTickSpacing(1);
         slids[i].setPaintTicks(true);
         slids[i].setPaintLabels(false);
         slids[i].setPreferredSize(new Dimension(windowWidth-25, sliderHeight));
         bets[i] = 50.0;
         JLabel sNum = new JLabel(i + " ", JLabel.CENTER);	
         sNum.setFont(new Font("Dialog", Font.PLAIN, 12));
         add(sNum);
         add(slids[i]);
         repaint();//?
       }
     }
 
     ChangeListener sliderChanged = new ChangeListener() {
 	    public void stateChanged(ChangeEvent e) {
 		MySlider source = (MySlider) e.getSource();
 		if (!source.getValueIsAdjusting()) {
 		    bets[(int) source.getNum()] = (double) source.getValue();
 		    System.out.println(source.getNum() + " changed to " + source.getValue());
 		}
 	    }
 	};
 
     public Double[] getBets() {
       return this.bets;
     }
   }
 }
