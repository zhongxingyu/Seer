 /*
  * This is a class that will call the CAGrid simulation and will display the results 
  * graphically in a window for the CA version of the model
  */
 
 
 import java.awt.*;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import java.awt.event.*;
 import java.awt.geom.GeneralPath;
 import java.io.IOException;
 
 import java.text.DecimalFormat;
 import java.util.*;
  
 public class CAStatic extends JFrame implements Runnable, ActionListener, ChangeListener {
 
     CAGridStatic experiment;
     //int[][] savedvals;
     int maxRun =2000;
 	int maxit = 30;
     //int[] savedd;// = new int[maxRun];
     //int[] saveddsq;
     //int[] dCount;
     int runCount = 0;
     //int epsCount = 0;
     int newframe = 0;
     Random rand = new Random();    
 	volatile Thread runner;
 	Image backImg1;
 	Graphics backGr1;
 	CAImagePanel CApicture;
 	//CAImagePanel CApicture2;
 	JButton startBtn,writeBtn,paramsBtn,scaleBtn;
 	JTextArea msgBtn,progressMsg;
 	JLabel cellselectMsg;
 	JPanel buttonHolderlow,buttonHolderhigh;
 	int iterations;
 	int scale = 20;
 	int gSize;
 	int dsize = 1;
 	int maxCellType;
 	int maxdCount = 0;
 	int lastDrawn = 0;
 	//int lin = 1;
 	//int linx = maxit + 1;
 
 	boolean started = false;
 	boolean scaling = false;
 	int scalefactor = 1;
     Colour palette = new Colour();
 	int[] colorindices = {0,1,2,54,4,5};
 	int nnw = colorindices.length-1;
 //    Color[] colours = {Color.white,Color.black,Color.green,Color.blue,Color.yellow,Color.red,Color.pink};
     Color[] javaColours;
     double[][] epsColours;
     String EPSFilename = "file.eps";
     int rowstoDraw = 80;
     ResultsPrinter outPrinter;
     double[] runStats = new double[2];
     Results[] saved;
     SpinnerNumberModel model3;
     JSpinner spinner3;
     int celltoDraw;
 
     
 	public CAStatic(int size) {
 
 		//size is the size of the area containing cells
 		dsize = size;
 	    gSize=size+2*maxit;
 	    celltoDraw = dsize-1;
 
 	    int wscale = 6;//scale for main panel
 	    int btnHeight = 480-384;//found by trial and error - must be a better way!
 
 		//experiment = new CAGridStatic(size, maxC);
 	    //int tint = (int)Math.ceil((double)(400*maxit)/(double)gSize)+(480-384);
 	    //int tint = (int)Math.ceil((double)(400*(50+maxit))/(double)gSize)+(480-384);
 	    int tint = (maxit + rowstoDraw)*wscale + btnHeight;
 	    //only going to show up to 50 dots in lower panel
 		//add 20 to x and 60 to y bcos not printing onto the full frame
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		Container mainWindow = getContentPane();
 		mainWindow.setLayout(new BorderLayout());
 		setSize(gSize*wscale,tint);
 
 		buttonHolderlow = new JPanel();
 		buttonHolderlow.setLayout(new GridLayout(2,2));
  
 		buttonHolderhigh = new JPanel();
 		buttonHolderhigh.setLayout(new GridLayout(1,2));
 
         writeBtn = new JButton("Output Results to file");
         writeBtn.addActionListener(this);
  	
         startBtn = new JButton("Start");
         startBtn.addActionListener(this);  
         
         paramsBtn = new JButton("Set Probabilities");
         paramsBtn.addActionListener(this);
         
         scaleBtn = new JButton("Scale to Fit");
         scaleBtn.addActionListener(this);
         
 
  
         //buttonHolderlow.add(scaleBtn);		
         if (dsize > 1){
            cellselectMsg = new JLabel(" select cell to count:");
             //cellselectMsg.setEditable(false);
             buttonHolderlow.add(cellselectMsg);
         	model3 = new SpinnerNumberModel(celltoDraw, 0, (dsize-1), 1);
         	spinner3 = new JSpinner(model3);
         	spinner3.addChangeListener(this);
         	buttonHolderlow.add(spinner3);
         }
         buttonHolderlow.add(paramsBtn);
         buttonHolderlow.add(startBtn);   
         buttonHolderlow.add(writeBtn);
 		writeBtn.setVisible(false);
         buttonHolderlow.add(scaleBtn);
 		scaleBtn.setVisible(false);
 		
         mainWindow.add(buttonHolderlow,BorderLayout.SOUTH);
    
         progressMsg = new JTextArea("PROGRESS "+runCount+"%");
         progressMsg.setEditable(false);
         buttonHolderhigh.add(progressMsg);
         msgBtn = new JTextArea("Probabilities: "+CAGridStatic.params);
         msgBtn.setEditable(false);
         buttonHolderhigh.add(msgBtn);
         
 	    mainWindow.add(buttonHolderhigh,BorderLayout.NORTH);
 	    
         CApicture = new CAImagePanel();
         CApicture.rowstoShow = rowstoDraw;
         mainWindow.add(CApicture,BorderLayout.CENTER);
 
         
 		//not here - doesn't work: CApicture.setScale(gSize,maxit,scale);
 
 
 //pack();
 
  
 		setVisible(true);
 		setpalette();
 		iterations = 0;
 		outPrinter = new ResultsPrinter(this);
 
 		
 	}
 	
     public void setpalette(){
     	int ind = colorindices.length;
     	javaColours = new Color[ind];
     	epsColours = new double[ind][3];
     	for (int i=0;i<ind;i++){
     		//System.out.println("color index "+colorindices[i]);
     		javaColours[i] = palette.chooseJavaColour(colorindices[i]);
     		epsColours[i] = palette.chooseEPSColour(colorindices[i]);
     	}
     }
 
 	
 	public void saveCA() {
 		int a;
 		//iterations should be correct to use as array ref
 		//backGr1.fillRect(0, 0, this.getSize().width, this.getSize().height);
 		for (CACell c : experiment.tissue){
 			a = c.lineage - 1;//lineage is 1 based
 			//savedvals[xv][yv] = a;
 			if (c.type == 1){
 				saved[a].posx[runCount][iterations] = c.home.x;
 				if ((runCount == 0) && (iterations == 0))saved[a].firstx = c.home.x;
 			}
 		}
 	}
 
 	
 	public void saverunStats() {
         for (int i=0;i<experiment.maxlineage;i++) saved[i].setrunStats(runCount, maxit-1);
 		drawCount();//well, you can only draw 1 of them
 	}
 	
 	public void showStats(){
 		DecimalFormat twoPlaces = new DecimalFormat("0.00");
 		double p,q;
         maxdCount = 0;
         Results r;
         p = CAGridStatic.params.pr;
         q = CAGridStatic.params.pl;
 
 		runStats[0] = maxit*(p-q);//expected d
 		runStats[1] = (maxit*(p+q) + maxit*(maxit-1)*Math.pow(p-q, 2.0));//expected dsq
 		System.out.println("expected d: "+ twoPlaces.format(runStats[0]) +" expected dsq " + twoPlaces.format(runStats[1]));
 
 		for (int i=0;i<experiment.maxlineage;i++) {
 			r = saved[i];
 			r.calcStats(runCount);
 			System.out.println("cell "+r.lineage+" av d: "+twoPlaces.format(r.cellStats[0])+" av d sq "+twoPlaces.format(r.cellStats[1]));
 			System.out.println("range of d: "+ r.mind + " to " + r.maxd);
 			System.out.println("maxdCount " + r.maxdCount);
 			if (r.maxdCount > maxdCount) maxdCount = r.maxdCount;
 		}
         scalefactor = (int) Math.ceil((double)maxdCount/(double) 80);
         if (scaling && (scalefactor > 1)) System.out.println("max maxdCount "+maxdCount+"scale factor "+scalefactor);
 		System.out.println("runCount = "+runCount);
 
 
 	}
 	
 	public void outputEPS(){
 	  //epsCount++;
 	  //String probstring = CAGridStatic.params.filename();	  
 	  //postscriptPrint("CA"+iterations+"."+probstring+"."+epsCount+".eps");
 	  //EPSFilename = "CA"+maxit+"_"+probstring+"_"+epsCount+".eps";
 	  outPrinter.makeFilenames();
 	  outPrinter.findScaleFactor(maxdCount);
 	  for (int i=0;i<experiment.maxlineage;i++){
 	      outPrinter.printEPSDots(i);
 	  }
 	  outPrinter.printData();
 	  outPrinter.printLaTeX(experiment.maxlineage,true);
 	}
 	
 	public void changeParameters(){
 		CAGridStatic.params.SetParamVals();
 	    System.out.println("param vals: "+CAGridStatic.params);
 	    msgBtn.setText(" Probabilities: "+CAGridStatic.params);
 	}
 	
 	public void changeWrap(){
 		CAGridStatic.params.nowrap = !CAGridStatic.params.nowrap;
 	}
 	
 	public void drawCA() {
         int a;
       	//CApicture.clearCAPanel();
 		for (CACell c : experiment.tissue){
 			a = c.lineage;
 			if (a > 0) a = (a-1)%nnw+1;
 			//if(a<7){
 				CApicture.drawCircleAt(c.home.x,iterations,javaColours[a]);
 			//}else{
 				//CApicture.drawCircleAt(c.home.x,iterations,Color.orange);
 			//}
 		}
 	    CApicture.updateGraphic();
 	}
 	
 	public void drawCount() {
 		int xv = saved[celltoDraw].posx[runCount][maxit-1];
 		int a = saved[celltoDraw].lineage;
 		a = (a-1)%nnw+1;
 		//maybe draw one every n count e.g. every 5 for instance.
 		CApicture.drawCircleAt(xv,saved[celltoDraw].dCount[xv],javaColours[a],2);
 	    CApicture.updateGraphic();
 	}
 	public void redrawCount() {
   	    CApicture.clearCAPanel(2);
 		int a = saved[celltoDraw].lineage;
 		a = (a-1)%nnw+1;
 		int xx,yy,posy=0;
 		int inc =1;
 		if (scaling) {
 			inc = scalefactor;
 		}
 		for (xx = 0; xx < gSize; xx++) {
 			posy = 1;
 			for (yy = 0; yy < saved[celltoDraw].dCount[xx]; yy=yy+inc) {
 				CApicture.drawCircleAt(xx,posy++,javaColours[a],2);
 			}
 		}
 		//maybe draw one every n count e.g. every 5 for instance.
 	    CApicture.updateGraphic();
 	}
 
 	public void drawLines(int it,int runnum){
 		int a;
 		int val = runCount*100/maxRun;
 		progressMsg.setText("PROGRESS "+val+"%");
 		for (int c = 0;c<experiment.maxlineage;c++){
 			a = saved[c].lineage;
 			a = (a-1)%nnw+1;
 		for (int i = lastDrawn; i < runnum; i++) {
 			for (int yy = 1; yy < it; yy++) {
 				CApicture.drawALine(saved[c].posx[i][yy-1],yy-1,
 						saved[c].posx[i][yy],yy,javaColours[a]);
 			}
 		}
 		}
 		lastDrawn = runCount;
 	    CApicture.updateGraphic();
 	}
 	
 	public void start() {
 		initialise();
 		 System.out.println("button holder size: "+buttonHolderlow.getSize());
 		lastDrawn = 0;
         started = true;
 		if (runner == null) {
 			runner = new Thread(this);
 		}
 
 		startBtn.setText("Stop");
 		writeBtn.setVisible(false);
 		paramsBtn.setVisible(false);
 		if (dsize> 1) {
 			spinner3.setVisible(false);
 			cellselectMsg.setVisible(false);
 		}
 		scaleBtn.setVisible(false);
 		scalefactor = 1;
 		scaleBtn.setText("Scale to Fit");
 		scaling = false;
 		runner.start();//not the same as this method!
 	}
 	public void stop(){
 		started = false;
 		runner = null;
 
 		startBtn.setText("Start");
 		writeBtn.setVisible(true);
 		scaleBtn.setVisible(true);
 		paramsBtn.setVisible(true);
 		if (dsize> 1) {
 			spinner3.setVisible(true);
 			cellselectMsg.setVisible(true);
 		}
 	}
     public void stateChanged(ChangeEvent e) {
         int i = (Integer) model3.getValue();
         //System.out.println("cell to draw "+i);
         celltoDraw = i;
         if (runCount != 0) redrawCount();
       }
 	public void actionPerformed(ActionEvent e){
 		if(e.getSource() == startBtn){
 			if(startBtn.getText()=="Start"){
 				if (started) {
 //					paramsBtn.setText("change parameters and rerun");
 //					started = false;
 				}
 				start();
 			}else{
 				stop();
 
 			}	
 			}
 		else if(e.getSource() == writeBtn){
              outputEPS();	
 			}
 		else if(e.getSource() == paramsBtn){
 			changeParameters();
 			start();
 
 			}
 		else if(e.getSource() == scaleBtn){
 			if(!scaling){
 				scaling = true;
 				scaleBtn.setText("remove scaling");
 				redrawCount();
 			}
 			else{
 				scaling = false;
 				scaleBtn.setText("Scale to Fit");
 				redrawCount();
 			}
 		}
 	}
 	
 	public void run() {
 		int tmprCount = 0;
 		boolean running = true;
 		for (runCount=0;runCount<maxRun;runCount++){
 
 			running = (runner == Thread.currentThread());
 			if (running){
 				if (runCount > 0) reset();
 				saveCA();
 				iterations++;
 				while ((iterations < maxit)) {
 					experiment.iterate();
 					saveCA();
 					//drawCA();
 					//if ((runCount%10) == 0) 
 					//drawLines();
 					iterations++;
 					//newframe = 0;		
 					//while(newframe<500000) newframe++;
 					//if((iterations%5)==0)postscriptPrint("CA"+iterations+".eps");
 					// This will produce a postscript output of the tissue
 				}
 				tmprCount = runCount;
 				saverunStats();
 				if ((runCount%25) == 0) {
 					drawLines(maxit,runCount);
 				}
 			}
 
 		}
 		//this will print out aborted results
 		//to stop that check if maxit was achieved
 		if (!started) runCount = tmprCount+1;//just in case stop was pressed
 		//iterations should anyway be equal to maxit
 		drawLines(maxit,runCount);
 		showStats();
 		stop();
 	}
 
 
 	public void initialise(){
 		int stuffind = 0;
 		    runCount = 0;
 		    int a;
 			experiment = new CAGridStatic(gSize,maxit,dsize);
            if (dsize > 1) model3.setMaximum(experiment.maxlineage-1);//change the spinner if needed
 			CApicture.setScale(gSize,maxit,scale,gSize,rowstoDraw,scale);
       	    CApicture.clearCAPanel(1);
       	    CApicture.clearCAPanel(2);
 		    iterations=0;
 		    saved = new Results[experiment.maxlineage];
 		    for (CACell c:experiment.tissue){
 
 		    	a = c.lineage;
 
                 if (a > 0 ){
 				    saved[a-1] = new Results(maxRun,maxit,a, gSize);
 
                 }
 			}
 
 	}
 	public void reset(){
         experiment.reSet(maxit,dsize);
 		iterations=0;
 
 	}
     
 	public static void main(String args[]) {
 
 
 		double initalSeed = 0.1;
 		if(args.length>0){
 			initalSeed = Double.parseDouble(args[0]);
 			CAStatic s = new CAStatic(1);
 /*	        s.initialise();
 			s.start();*/
 		}else{
			CAStatic s = new CAStatic(1);
 /*	        s.initialise();
 			s.start();*/
 			System.out.println("finished");//one thread gets here
 		}
 	}
 	
 	}
 
 
 
