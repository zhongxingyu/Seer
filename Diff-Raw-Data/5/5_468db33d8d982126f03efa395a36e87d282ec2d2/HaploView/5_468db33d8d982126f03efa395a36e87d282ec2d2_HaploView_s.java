 package edu.mit.wi.haploview;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.awt.geom.*;
 import java.awt.image.*;
 
 public class HaploView extends JFrame implements ActionListener{
 
     //start filechooser in current directory
     final JFileChooser fc = new JFileChooser(System.getProperty("user.dir"));
     HaploData theData;
     HaploView window;
     Container contents;
     Haplotype[][] finishedHaplos;
     int[][] lookupPos;
     private BlockDisplay theBlocks;
     private String loadInfoStr = "Load Marker Info";
     private String infileName="";
     private boolean infoKnown = false;
     private boolean useThickness = true;
     private ProgressMonitor progressMonitor;
     private javax.swing.Timer timer;
     private int haploThresh = 1;
     private int colorThresh = 1;
     private int crossThinThresh = 1;
     private int crossThickThresh = 10;
     private NumberTextField minThickCrossValTF, minThinCrossValTF, minColorValTF;
     JMenuItem loadInfoMenuItem = new JMenuItem(loadInfoStr);
     JMenuItem hapMenuItem = new JMenuItem("Generate Haplotypes");
     JMenuItem exportMenuItem = new JMenuItem("Export LD Picture to JPG");
     JMenuItem saveDprimeMenuItem = new JMenuItem("Dump LD Output to Text");
     JMenuItem clearBlocksMenuItem = new JMenuItem("Clear All Blocks");
     JMenuItem guessBlocksMenuItem = new JMenuItem("Define Blocks");
     JMenuItem saveHapsMenuItem = new JMenuItem("Save Haplotypes to Text");
     JMenuItem saveHapsPicMenuItem = new JMenuItem("Save Haplotypes to JPG");
     JMenuItem customizeHapsMenuItem = new JMenuItem("Customize Haplotype Output");
     DPrimePanel theDPrime;
 
     public HaploView(){
 	JMenu fileMenu, toolMenu, helpMenu, blockMenu;
 	JMenuBar menuBar = new JMenuBar();
 	JMenuItem menuItem;
 	
 	addWindowListener(new WindowAdapter() {
 		public void windowClosing(WindowEvent e){
 		    System.exit(0);
 		}
 	    });
 	setJMenuBar(menuBar);
 	
 	fileMenu = new JMenu("File");
 	menuBar.add(fileMenu);
 
 	toolMenu = new JMenu("Tools");
 	menuBar.add(toolMenu);
 
 	/** NEEDS FIXING
 	helpMenu = new JMenu("Help");
 	menuBar.add(Box.createHorizontalGlue());
 	menuBar.add(helpMenu);
 	
 	menuItem = new JMenuItem("Tutorial");
 	menuItem.addActionListener(this);
 	helpMenu.add(menuItem);
 	**/
 	
 	menuItem = new JMenuItem("Open");
 	menuItem.addActionListener(this);
 	fileMenu.add(menuItem);
 
 	loadInfoMenuItem.addActionListener(this);
 	loadInfoMenuItem.setEnabled(false);
 	toolMenu.add(loadInfoMenuItem);
 	toolMenu.addSeparator();
 
 	clearBlocksMenuItem.addActionListener(this);
 	clearBlocksMenuItem.setEnabled(false);
 	toolMenu.add(clearBlocksMenuItem);
 
 	guessBlocksMenuItem.addActionListener(this);
 	guessBlocksMenuItem.setEnabled(false);
 	toolMenu.add(guessBlocksMenuItem);
 	toolMenu.addSeparator();
 	
 	hapMenuItem.addActionListener(this);
 	hapMenuItem.setEnabled(false);
 	toolMenu.add(hapMenuItem);
 
 	customizeHapsMenuItem.addActionListener(this);
 	customizeHapsMenuItem.setEnabled(false);
 	toolMenu.add(customizeHapsMenuItem);
 	toolMenu.addSeparator();
 	
 	saveHapsMenuItem.addActionListener(this);
 	saveHapsMenuItem.setEnabled(false);
 	toolMenu.add(saveHapsMenuItem);
 
 	saveHapsPicMenuItem.addActionListener(this);
 	saveHapsPicMenuItem.setEnabled(false);
 	toolMenu.add(saveHapsPicMenuItem);
 
 	saveDprimeMenuItem.addActionListener(this);
 	saveDprimeMenuItem.setEnabled(false);
 	toolMenu.add(saveDprimeMenuItem);
 
 	exportMenuItem.addActionListener(this);
 	exportMenuItem.setEnabled(false);
 	toolMenu.add(exportMenuItem);
 
 	fileMenu.addSeparator();
 	menuItem = new JMenuItem("Exit");
 	menuItem.addActionListener(this);
 	fileMenu.add(menuItem);
     }
 
     void drawPicture(HaploData theData){
 	contents = getContentPane();
 	contents.removeAll();
 
 	//first, draw the D' picture
 	contents.setLayout(new BoxLayout(contents, BoxLayout.Y_AXIS));
 	theDPrime = new DPrimePanel(theData.dPrimeTable, infoKnown, theData.markerInfo);
 	JPanel holderPanel = new JPanel();
 	holderPanel.add(theDPrime);
 	holderPanel.setBackground(new Color(192,192,192));
 	JScrollPane dPrimeScroller = new JScrollPane(holderPanel);
 	dPrimeScroller.getVerticalScrollBar().setUnitIncrement(60);
 	dPrimeScroller.getHorizontalScrollBar().setUnitIncrement(60);
 	contents.add(dPrimeScroller);
 
 	//next add a little spacer
 	contents.add(Box.createRigidArea(new Dimension(0,5)));
 
 	//and then add the block display
 	theBlocks = new BlockDisplay(theData.markerInfo, theData.blocks, theDPrime, infoKnown);
 	contents.setBackground(Color.black);
 
 	//put the block display in a scroll pane in case the data set is very large.
 	JScrollPane blockScroller = new JScrollPane(theBlocks,
 						    JScrollPane.VERTICAL_SCROLLBAR_NEVER,
 						    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
 	blockScroller.getHorizontalScrollBar().setUnitIncrement(60);
 	blockScroller.setMinimumSize(new Dimension(800, 100));
 	contents.add(blockScroller);
 	repaint();
 	setVisible(true);
     }
 
     void doExportDPrime(){
 	fc.setSelectedFile(null);
 	int returnVal = fc.showSaveDialog(this);
 	if (returnVal == JFileChooser.APPROVE_OPTION){
 	    try {
		int scaleSize = theData.dPrimeTable.length*30;
 		DrawingMethods dm = new DrawingMethods();
		BufferedImage image = new BufferedImage(scaleSize, scaleSize, BufferedImage.TYPE_3BYTE_BGR);
 		dm.dPrimeDraw(theData.dPrimeTable, infoKnown, theData.markerInfo, image.getGraphics());
 		dm.saveImage(image, fc.getSelectedFile().getPath());
 	    } catch (IOException ioexec){
 		JOptionPane.showMessageDialog(this,
 					      ioexec.getMessage(),
 					      "File Error",
 					      JOptionPane.ERROR_MESSAGE);
 	    }
 	}
     }
 
     void showHelp(){
 
 	//Help Text:
 	String helpText = new String();
 	try{
 	    File helpFile = new File(System.getProperty("java.class.path") + File.separator + "haplohelp.txt");
 	    BufferedReader inHelp = new BufferedReader(new FileReader(helpFile));
 	    helpText = inHelp.readLine();
 	    String currentLine = new String();
 	    while ((currentLine = inHelp.readLine()) != null){
 		helpText += ("\n" + currentLine);
 	    }
 	    inHelp.close();
 	}catch (IOException ioexec){
 	    helpText = "Help file not found.\n";
 	}
 
 	JFrame helpFrame = new JFrame("HaploView Help");
 	JTextArea helpTextArea = new JTextArea();
 	JScrollPane helpDisplayPanel = new JScrollPane(helpTextArea);
 	helpDisplayPanel.setBackground(Color.white);
 	helpTextArea.setText(helpText);
 	helpDisplayPanel.setOpaque(true);
 	helpDisplayPanel.setPreferredSize(new Dimension(450,500));
 	helpFrame.setContentPane(helpDisplayPanel);
 	helpFrame.pack();
 	helpFrame.setVisible(true);
     }
 
     void drawHaplos(Haplotype[][] haplos) throws IOException{
 	Haplotype[][] orderedHaplos = new Haplotype[haplos.length][];
 	for (int i = 0; i < haplos.length; i++){
 	    Vector orderedHaps = new Vector();
             //step through each haplotype in this block
             for (int hapCount = 0; hapCount < haplos[i].length; hapCount++){
 		if (orderedHaps.size() == 0){
 		    orderedHaps.add(haplos[i][hapCount]);
 		}else{
 		    for (int j = 0; j < orderedHaps.size(); j++){
 			if (((Haplotype)(orderedHaps.elementAt(j))).getPercentage() < haplos[i][hapCount].getPercentage()){
 			    orderedHaps.add(j, haplos[i][hapCount]);
 			    break;
 			}
 			if ((j+1) == orderedHaps.size()){
 			    orderedHaps.add(haplos[i][hapCount]);
 			    break;
 			}
 		    }
 		}
 	    }
 	    orderedHaplos[i] = new Haplotype[orderedHaps.size()];
 	    for (int z = 0; z < orderedHaps.size(); z++){
 		orderedHaplos[i][z] = (Haplotype)orderedHaps.elementAt(z);
 	    }
 		
 	}
 	finishedHaplos = theData.generateCrossovers(orderedHaplos);
 
 
 	JFrame haploFrame = new JFrame("Haplotypes");
 	HaplotypePanel hapWindow = new HaplotypePanel(finishedHaplos, useThickness,
 						      colorThresh, crossThinThresh, crossThickThresh,
 						      theData.getMultiDprime());
 	hapWindow.setBackground(Color.white);
 
 	haploFrame.setContentPane(hapWindow);
 	haploFrame.pack();
 	haploFrame.setVisible(true);
     }
 
     void customizeHaps(){
 	JPanel itemsPanel = new JPanel();
 	JPanel hapPrefsPanel = new JPanel();
 	JPanel crossPrefsPanel = new JPanel();
 	
 	//text field for min haplotype percentage to display
 	NumberTextField minHapDisplayValTF = new NumberTextField(String.valueOf(haploThresh), 2);
 	hapPrefsPanel.add(new JLabel("Examine haps above "));
 	hapPrefsPanel.add(minHapDisplayValTF);
 	hapPrefsPanel.add(new JLabel("%"));
 	hapPrefsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 	
 	//options related to crossover percentages
 	JRadioButton thickButton = new JRadioButton("Line Thickness");
 	thickButton.setActionCommand("thick");
 
 	JRadioButton colorButton = new JRadioButton("Color");
 	colorButton.setActionCommand("color");
 
 	//radio buttons for which distinguishing method
 	ButtonGroup crossGroup = new ButtonGroup();
 	crossGroup.add(thickButton);
 	crossGroup.add(colorButton);
 
 	//listen for button switch
 	RadioListener myRadListener = new RadioListener();
 	thickButton.addActionListener(myRadListener);
 	colorButton.addActionListener(myRadListener);
 
 	//stuff for thickness mods
 	minThickCrossValTF = new NumberTextField(String.valueOf(crossThickThresh), 2);
 	minThinCrossValTF = new NumberTextField(String.valueOf(crossThinThresh), 2);
 	//all these sub-panels to make the layout pretty
 	JPanel thickPanel = new JPanel();
 	JPanel thickFieldsPanel = new JPanel();
 	JPanel fieldsPanelTop = new JPanel();
 	JPanel fieldsPanelBot = new JPanel();
 	thickFieldsPanel.setLayout(new BoxLayout(thickFieldsPanel, BoxLayout.Y_AXIS));
 	fieldsPanelTop.add(new JLabel("Connect with thick lines if > "));
 	fieldsPanelTop.add(minThickCrossValTF);
 	fieldsPanelTop.add(new JLabel("%"));
 	fieldsPanelBot.add(new JLabel("Connect with thin lines if > "));
 	fieldsPanelBot.add(minThinCrossValTF);
 	fieldsPanelBot.add(new JLabel("%"));
 	thickFieldsPanel.add(fieldsPanelTop);
 	thickFieldsPanel.add(fieldsPanelBot);
 	thickPanel.add(thickButton);
 	thickPanel.add(thickFieldsPanel);
 
 	//stuff for color 
 	JPanel colorPanel = new JPanel();
 	JPanel colorFieldPanel = new JPanel();
 	minColorValTF = new NumberTextField(String.valueOf(colorThresh), 2);
 	colorFieldPanel.add(new JLabel("Display connections > "));
 	colorFieldPanel.add(minColorValTF);
 	colorFieldPanel.add(new JLabel("%"));
 	colorPanel.add(colorButton);
 	colorPanel.add(colorFieldPanel);
 
 	//force the appropriate areas to be greyed out
 	if (useThickness){
 	    thickButton.setSelected(true);
 	    minColorValTF.setEnabled(false);
 	    minThinCrossValTF.setEnabled(true);
 	    minThickCrossValTF.setEnabled(true);
 	}else{
 	    colorButton.setSelected(true);
 	    minColorValTF.setEnabled(true);
 	    minThinCrossValTF.setEnabled(false);
 	    minThickCrossValTF.setEnabled(false);
 	}
 	
 	GridBagLayout gridbag = new GridBagLayout();
 	GridBagConstraints c = new GridBagConstraints();
 	crossPrefsPanel.setLayout(gridbag);
 
 	//header
 	JLabel crossLabel = new JLabel("Select a method to distinguish crossing strength:");
 	c.gridx = 0; c.gridy=0; c.anchor=GridBagConstraints.NORTH;
 	gridbag.setConstraints(crossLabel,c);
 	crossPrefsPanel.add(crossLabel);
 
 	//thickness stuff
 	c.gridy = 1; c.anchor = GridBagConstraints.WEST;
 	gridbag.setConstraints(thickPanel,c);
 	crossPrefsPanel.add(thickPanel);
 
 	//color stuff
 	c.gridy=2; c.anchor = GridBagConstraints.WEST;
 	gridbag.setConstraints(colorPanel,c);
 	crossPrefsPanel.add(colorPanel);
 	crossPrefsPanel.setBorder(BorderFactory.createLineBorder(Color.black));
 
 	//set up the layout of all the created items
 	itemsPanel.setLayout(new BoxLayout(itemsPanel, BoxLayout.Y_AXIS));
 	itemsPanel.add(hapPrefsPanel);
 	itemsPanel.add(Box.createRigidArea(new Dimension(0,5)));
 	itemsPanel.add(crossPrefsPanel);
 
 	JOptionPane.showMessageDialog(window,
 				      itemsPanel,
 				      "Customize Haplotype Output",
 				      JOptionPane.PLAIN_MESSAGE);
 	haploThresh = Integer.parseInt(minHapDisplayValTF.getText());
 	crossThinThresh = Integer.parseInt(minThinCrossValTF.getText());
 	crossThickThresh = Integer.parseInt(minThickCrossValTF.getText());
 	colorThresh = Integer.parseInt(minColorValTF.getText());
     }
 
     //listen to radio buttons
     class RadioListener implements ActionListener {
 	public void actionPerformed(ActionEvent e){
 	    if (e.getActionCommand().equals("thick")){
 		useThickness = true;
 		minThickCrossValTF.setEnabled(true);
 		minThinCrossValTF.setEnabled(true);
 		minColorValTF.setEnabled(false);
 	    }else{
 		useThickness = false;
 		minThickCrossValTF.setEnabled(false);
 		minThinCrossValTF.setEnabled(false);
 		minColorValTF.setEnabled(true);
 	    }
 	}
     }
 
     void saveHapsPic(){
 	fc.setSelectedFile(null);
 	int returnVal = fc.showSaveDialog(this);
 	if (returnVal == JFileChooser.APPROVE_OPTION){
 	    try {
 		DrawingMethods dm = new DrawingMethods();
 		BufferedImage testImage = new BufferedImage(10,10,BufferedImage.TYPE_3BYTE_BGR);
 		Dimension theSize = dm.haploGetPreferredSize(finishedHaplos, testImage.getGraphics());
 		BufferedImage image = new BufferedImage((int)theSize.getWidth(),(int)theSize.getHeight(),BufferedImage.TYPE_3BYTE_BGR);
 		dm.haploDraw(image.getGraphics(), useThickness,
 			     colorThresh, crossThinThresh, crossThickThresh,
 			     theData.getMultiDprime() ,finishedHaplos);
 		dm.saveImage(image, fc.getSelectedFile().getPath());
 	    }catch (IOException ioexec){
 		JOptionPane.showMessageDialog(this, 
 					      ioexec.getMessage(), 
 					      "File Error",
 					      JOptionPane.ERROR_MESSAGE);
 	    }
 	}
     }
 
     void saveDprimeToText(){
 	try{
 	    fc.setSelectedFile(null);
 	    int returnVal = fc.showSaveDialog(this);
 	    if (returnVal == JFileChooser.APPROVE_OPTION) {
 		new TextMethods().saveDprimeToText(theData.dPrimeTable, fc.getSelectedFile(), infoKnown, theData.markerInfo);
 	    }
 	}catch (IOException ioexec){
 	    JOptionPane.showMessageDialog(this, 
 					  ioexec.getMessage(), 
 					  "File Error",
 					  JOptionPane.ERROR_MESSAGE);
 	}
     }
 
     void saveHapsToText(){
 	try{
 	    fc.setSelectedFile(null);
 	    int returnVal = fc.showSaveDialog(this);
 	    if (returnVal == JFileChooser.APPROVE_OPTION) {
 		new TextMethods().saveHapsToText(finishedHaplos, fc.getSelectedFile());
 	    }
 	}catch (IOException ioexec){
 	    JOptionPane.showMessageDialog(this, 
 					  ioexec.getMessage(), 
 					  "File Error",
 					  JOptionPane.ERROR_MESSAGE);
 	}
     }
 
     void defineBlocks(){
 	String[] methodStrings = {"95% of informative pairwise comparisons show strong LD via confidence intervals (SFS)",
 				  "Solid block of strong LD via D prime (MJD)"};
 	JComboBox methodList = new JComboBox(methodStrings);
 	JOptionPane.showMessageDialog(window,
 				      methodList,
 				      "Select a block-finding algorithm",
 				      JOptionPane.QUESTION_MESSAGE);		       
 	theData.blocks = theData.guessBlocks(theData.dPrimeTable, methodList.getSelectedIndex());
 	drawPicture(theData);
     }    
 	
     public void actionPerformed(ActionEvent e) {
 	String command = e.getActionCommand();
 	if (command == "Open"){
 	    int returnVal = fc.showOpenDialog(this);
 	    if (returnVal == JFileChooser.APPROVE_OPTION) {
 		try{
 		    theData = new HaploData(fc.getSelectedFile());
 		    infileName = fc.getSelectedFile().getName();
 		    
 		    //compute D primes and monitor progress
 		    progressMonitor = new ProgressMonitor(this, "Computing " + theData.getToBeCompleted() + " values of D prime","", 0, theData.getToBeCompleted());
 		    progressMonitor.setProgress(0);
 		    progressMonitor.setMillisToDecideToPopup(2000);
 
 		    final SwingWorker worker = new SwingWorker(){
 			    public Object construct(){
 				theData.doMonitoredComputation();
 				return "";
 			    }
 			};
 		    
 		    timer = new javax.swing.Timer(500, new ActionListener(){
 			    public void actionPerformed(ActionEvent evt){
 				progressMonitor.setProgress(theData.getComplete());
 				if (theData.getComplete() == theData.getToBeCompleted()){
 				    timer.stop();
 				    progressMonitor.close();
 				    infoKnown=false;
 				    drawPicture(theData);
 				    loadInfoMenuItem.setEnabled(true);
 				    hapMenuItem.setEnabled(true);
 				    customizeHapsMenuItem.setEnabled(true);
 				    exportMenuItem.setEnabled(true);
 				    saveDprimeMenuItem.setEnabled(true);
 				    clearBlocksMenuItem.setEnabled(true);
 				    guessBlocksMenuItem.setEnabled(true);
 				}
 			    }
 			});
 
 		    worker.start();
 		    timer.start();
 		    
 		}catch (IOException ioexec){
 		    JOptionPane.showMessageDialog(this, 
 						  ioexec.getMessage(), 
 						  "File Error",
 						  JOptionPane.ERROR_MESSAGE);
 		}catch (RuntimeException rtexec){
 		    JOptionPane.showMessageDialog(this,
 						  "An error has occured. It is probably related to file format:\n"+rtexec.toString(),
 						  "Error",
 						  JOptionPane.ERROR_MESSAGE);
 		}
 	    }
 	} else if (command == loadInfoStr){
 	    int returnVal = fc.showOpenDialog(this);
 	    if (returnVal == JFileChooser.APPROVE_OPTION) {
 		try{
 		    theData.prepareMarkerInput(fc.getSelectedFile());
 		    infoKnown=true;
 		    // loadInfoMenuItem.setEnabled(false);
 		    drawPicture(theData);
 		}catch (IOException ioexec){
 		    JOptionPane.showMessageDialog(this, 
 						  ioexec.getMessage(), 
 						  "File Error",
 						  JOptionPane.ERROR_MESSAGE);
 		}catch (RuntimeException rtexec){
 		    JOptionPane.showMessageDialog(this,
 						  "An error has occured. It is probably related to file format:\n"+rtexec.toString(),
 						  "Error",
 						  JOptionPane.ERROR_MESSAGE);
 		}
 	    }
 	}else if (command == "Clear All Blocks"){
 	    theBlocks.clearBlocks();
 	}else if (command == "Define Blocks"){
 	    defineBlocks();
 	}else if (command == "Customize Haplotype Output"){
 	    customizeHaps();
 	}else if (command == "Tutorial"){
 	    showHelp();
 	}else if (command == "Export LD Picture to JPG"){
 	    doExportDPrime();
 	}else if (command == "Dump LD Output to Text"){
 	    saveDprimeToText();
 	}else if (command == "Save Haplotypes to Text"){
 	    saveHapsToText();
 	}else if (command == "Save Haplotypes to JPG"){
 	    saveHapsPic();
 	}else if (command == "Generate Haplotypes"){
 	    try{
 		drawHaplos(theData.generateHaplotypes(theData.blocks, haploThresh));
 		saveHapsMenuItem.setEnabled(true);
 		saveHapsPicMenuItem.setEnabled(true);
 	    }catch (IOException ioe){}
 	} else if (command == "Exit"){
 	    System.exit(0);
 	}
     }
 
     public static void main(String[] args) throws IOException{
 	try {
 	    UIManager.setLookAndFeel(
 		UIManager.getCrossPlatformLookAndFeelClassName());
 	} catch (Exception e) { }
 	
 	HaploView window = new HaploView();
 	window.setTitle("HaploView alpha");
 	window.setSize(800,800);
 	window.setVisible(true);
     }    
 }
 
