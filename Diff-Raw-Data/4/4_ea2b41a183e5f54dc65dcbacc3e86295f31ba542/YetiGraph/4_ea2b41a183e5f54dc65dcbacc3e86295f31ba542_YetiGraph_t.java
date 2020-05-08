 package yeti.monitoring;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Line2D;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 
 import javax.swing.JFileChooser;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 
 import yeti.YetiLog;
 
 /**
  * Class that represents a JPanel that shows graphs. 
  * 
  * @author Manuel Oriol (manuel@cs.york.ac.uk)
  * @date Jul 27, 2009
  *
  */
 @SuppressWarnings({ "serial", "unchecked" })
 public class YetiGraph extends JPanel implements YetiUpdatable, YetiSamplable, ActionListener{
 
 	/**
 	 * Class that represents a popup listener.
 	 * 
 	 * @author Manuel Oriol (manuel@cs.york.ac.uk)
 	 * @date Sep 16, 2009
 	 *
 	 */
 	class PopupListener extends MouseAdapter {
 	    public void mousePressed(MouseEvent e) {
 	        showPopup(e);
 	    }
 
 	    public void mouseReleased(MouseEvent e) {
 	        showPopup(e);
 	    }
 
 	    private void showPopup(MouseEvent e) {
 	        if (e.isPopupTrigger()) {
 	            popup.show(e.getComponent(),
 	                       e.getX(), e.getY());
 	        }
 	    }
 	}
 	/**
 	 * The list of points.
 	 */
 	public ArrayList<Double> []series = new ArrayList[2];
 
 	/**
 	 * The size of the border on the left part.
 	 */
 	int leftBorder = 25;
 
 	/**
 	 * The size of the border on the right part.
 	 */
 	int rightBorder = 25;
 
 	/**
 	 * The size of the border on the lower part.
 	 */
 	int horizontalBorder = 25;
 
 	/**
 	 * THe maximum represented value of x.
 	 */
 	double maxX = 5;
 
 	/**
 	 * The maximum represented value in x
 	 */
 	double maxY = 9;
 
 	/**
 	 * The number of digits of the scale of X.
 	 */
 	int nDigitsX = 1;
 
 	/**
 	 * The number of digits of the scale of Y.
 	 */
 	int nDigitsY = 1;
 	
 	
 	/**
 	 * Popup menu on this graph.
 	 */
 	JPopupMenu popup=null;
 
 
 	protected void paintComponent(Graphics g) {
 		super.paintComponent(g);
 		// we get the graph component
 		Graphics2D g2 = (Graphics2D)g;
 		// we get the size of the area to paint 
 		double w = getWidth();
 		double h = getHeight();
 		
 		// the step between points
 		int pointStep = 1;
 		if ((w!=0)&&(series[0]!=null)&&(series[0].size()>w))
 			pointStep = (int) (series[0].size()/w);
 		
 		// Draw y-axis.
 		g2.draw(new Line2D.Double(leftBorder, horizontalBorder, leftBorder, h-horizontalBorder));
 		// Draw x-axis.
 		g2.draw(new Line2D.Double(leftBorder, h-horizontalBorder, w-rightBorder, h-horizontalBorder));
 		g2.drawString(name, leftBorder , horizontalBorder-5);
 		// we draw the y-labels
 		double ystep = Math.pow(10, nDigitsY);
 		if ((Math.pow(10, nDigitsY)*2)>maxY) 
 			ystep = Math.pow(10, nDigitsY-1)*2;
 
 		for (int i = 0; i<20; i++) {
 			int grade = (int)(i*ystep);
 			if (grade>maxY) {
 				break;
 			}
 			double y1=h-horizontalBorder-(h-2*horizontalBorder)*grade/maxY;
 			g2.draw(new Line2D.Double(leftBorder-1,y1,leftBorder+1,y1));
 			g2.drawString(""+grade, 5, (int)y1+5);
 		}
 
 		// we draw the x-labels
 		double xstep = Math.pow(10, nDigitsX);
 		if ((Math.pow(10, nDigitsX)*2)>maxX) 
 			xstep = Math.pow(10, nDigitsX-1)*2;
 		for (int i = 0; i<10; i++) {
 			int grade = (int)(i*xstep);
 			if (grade>maxX) {
 				break;
 			}
 			double x1=leftBorder+(w-leftBorder-rightBorder)*grade/maxX;
 			g2.draw(new Line2D.Double(x1,h-horizontalBorder+1,x1,h-horizontalBorder-1));
 			g2.drawString(""+grade, (int)(x1-(nDigitsX*4)-2), (int) (h-horizontalBorder+17));
 		}
 
 		// we draw data points.
 		try {
 			g2.setPaint(Color.red);
 			int size = series[0].size();
 			for(int i = 0; i < size; i+=pointStep) {
 				double x = leftBorder + (w-leftBorder-rightBorder)*series[0].get(i)/maxX;
 				double y =  h - horizontalBorder - (h-2*horizontalBorder)*series[1].get(i)/maxY;
 				g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));
 			}
 		} catch (NullPointerException e) {
 
 		}
 	}
 	/**
 	 * Adds a value to draw.
 	 * 
 	 * @param x the x value.
 	 * @param y the y value
 	 */
 	public void addValue(double x, double y) {
 		// if the maximum x is too low we extend it.
 		if (maxX<x) {
 			maxX=1.2*x;
 			nDigitsX=(int)Math.floor(Math.log10(maxX));
 		}
 		// if the maximum y is too low, we extend it.
 		if (maxY<y) {
 			maxY=2*y;
 			nDigitsY=(int)Math.floor(Math.log10(maxY));
 			leftBorder = 20+nDigitsY*7;
 		}
 		YetiLog.printDebugLog("x= "+x+"y= "+y, this);
 		// we add the point to the graph
 		series[0].add(x);
 		series[1].add(y);
 	}
 
 	/**
 	 * Stores the title of the graph.
 	 */
 	public String name = "";
 	
 	/**
 	 * A menu item to save the values in comma-separated values.
 	 */
 	JMenuItem saveAsCSV=null;
 
 	/**
 	 * A menu item to save the values in space-separated values vertically (2 columns).
 	 */
 	JMenuItem saveAsSSV=null;
 
 	/**
 	 * A menu item to save the values in space-separated values horizontally (2 lines).
 	 */
 	JMenuItem saveAsHSSV=null;
 
 	/**
 	 * Simple constructor, stores the name of the graph.
 	 * 
 	 * @param name
 	 */
 	public YetiGraph(String name) {
 		super();
 		this.name = name;
 		this.setBackground(Color.white);
 		series[0] = new ArrayList<Double>();
 		series[1] = new ArrayList<Double>();
 
 		
 		//Create the popup menu.
 	    popup = new JPopupMenu();
 	    saveAsCSV = new JMenuItem("Export as comma-separated values");
 	    saveAsCSV.addActionListener(this);
 	    popup.add(saveAsCSV);
 	    saveAsSSV = new JMenuItem("Export as space-separated values (vertical, 2 columns)");
 	    saveAsSSV.addActionListener(this);
 	    popup.add(saveAsSSV);
 
 	    saveAsHSSV = new JMenuItem("Export as space-separated values (horizontal, 2 lines)");
 	    saveAsHSSV.addActionListener(this);
 	    popup.add(saveAsHSSV);
 
 	    //Add listener to components that can bring up popup menus.
 	    MouseListener popupListener = new PopupListener();
 	    this.addMouseListener(popupListener);
 	}
 
 	
 	/* (non-Javadoc)
 	 * Update this component.
 	 * 
 	 * @see yeti.monitoring.YetiUpdatable#updateValues()
 	 */
 	public void updateValues() {
 		repaint();
 	}
 	
 	/* (non-Javadoc)
 	 * Method to rewrite when subclassing this class.
 	 * 
 	 * @see yeti.monitoring.YetiSamplable#sample()
 	 */
 	public void sample() {
 		
 	}
 
 	/**
 	 * Routine used to save the current values of this graph in a file as comma-separated values.
 	 * 
 	 * @param fileName the name of the file in which save the values.
 	 */
 	public void saveAsCSV(String fileName) {
 		File f0 = new File(fileName);
 		try {
 			// we check that the file is good
 			if (f0.canWrite()||f0.createNewFile()) {
 				YetiLog.printDebugLog("Saving as CSV "+this.name+" in "+fileName, this,true);
 				int max = this.series[1].size();
 				// we print all values in the file
 				PrintStream ps = new PrintStream(f0);
 				for (int i = 0; i<max; i++) {
 					ps.println(this.series[0].get(i)+","+this.series[1].get(i));
 				}
 				ps.close();
 			
 			} else {
 				YetiLog.printDebugLog("Impossible to save as CSV "+this.name+" in "+fileName+" File unwritable", this,true);			
 			}
 		} catch (IOException e) {
 			// Auto-generated catch block
 			YetiLog.printDebugLog("Impossible to save as CSV "+this.name+" in "+fileName+" File unwritable", this,true);			
 		}
 	}
 
 	/**
 	 * Routine used to save the current values of this graph in a file as space-separated values (2 columns).
 	 * 
 	 * @param fileName the name of the file in which save the values.
 	 */
 	public void saveAsSSV(String fileName) {
 		File f0 = new File(fileName);
 		try {
 			// we check that the file is good
 			if (f0.canWrite()||f0.createNewFile()) {
 				YetiLog.printDebugLog("Saving as SSV "+this.name+" in "+fileName, this,true);
 				int max = this.series[1].size();
 				// we print all values in the file
 				PrintStream ps = new PrintStream(f0);
 				for (int i = 0; i<max; i++) {
 					ps.println(this.series[0].get(i)+" "+this.series[1].get(i));
 				}
 				ps.close();
 				
 			} else {
 				YetiLog.printDebugLog("Impossible to save as SSV "+this.name+" in "+fileName+" File unwritable", this,true);			
 			}
 		} catch (IOException e) {
 			// Auto-generated catch block
 			YetiLog.printDebugLog("Impossible to save as SSV "+this.name+" in "+fileName+" File unwritable", this,true);			
 		}
 	}	
 
 	/**
 	 * Routine used to save the current values of this graph in a file as space-separated values (2 lines).
 	 * 
 	 * @param fileName the name of the file in which save the values.
 	 */
 	public void saveAsHSSV(String fileName) {
 		File f0 = new File(fileName);
 		try {
 			// we check that the file is good
 			if (f0.canWrite()||f0.createNewFile()) {
 				YetiLog.printDebugLog("Saving as SSV "+this.name+" in "+fileName, this,true);
 				int max = this.series[1].size();
 				// we print all values in the file
 				PrintStream ps = new PrintStream(f0);
 				for (int i = 0; i<max; i++) {
 					ps.print(this.series[0].get(i)+" ");
 				}
 				ps.print("\n");
 				for (int i = 0; i<max; i++) {
 					ps.print(this.series[1].get(i)+" ");
 				}
 				
 			} else {
 				YetiLog.printDebugLog("Impossible to save as SSV "+this.name+" in "+fileName+" File unwritable", this,true);			
 			}
 		} catch (IOException e) {
 			// Auto-generated catch block
 			YetiLog.printDebugLog("Impossible to save as SSV "+this.name+" in "+fileName+" File unwritable", this,true);			
 		}
 	}	
 
 	/* (non-Javadoc)
 	 * The action listener for the graphs.
 	 * 
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	public void actionPerformed(ActionEvent e) {
 		
 		// if the source is the item hor saving in SSV we call the corresponding routine
 		if (e.getSource().equals(this.saveAsCSV)) {
 			JFileChooser chooser = new JFileChooser();
 			int returnVal=chooser.showSaveDialog(this);
 		    if(returnVal == JFileChooser.APPROVE_OPTION) {
		    	File f = chooser.getSelectedFile();
				String fileName = f.getAbsolutePath();
 				this.saveAsCSV(fileName);
 
 		    }
 		}
 		// if the source is the item hor saving in SSV we call the corresponding routine
 		if (e.getSource().equals(this.saveAsSSV)) {
 			JFileChooser chooser = new JFileChooser();
 			int returnVal=chooser.showSaveDialog(this);
 		    if(returnVal == JFileChooser.APPROVE_OPTION) {
 
 				String fileName = chooser.getSelectedFile().getName();
 				this.saveAsSSV(fileName);
 
 		    }
 		}
 		// if the source is the item hor saving in HSSV we call the corresponding routine
 		if (e.getSource().equals(this.saveAsHSSV)) {
 			JFileChooser chooser = new JFileChooser();
 			int returnVal=chooser.showSaveDialog(this);
 		    if(returnVal == JFileChooser.APPROVE_OPTION) {
 
 				String fileName = chooser.getSelectedFile().getName();
 				this.saveAsHSSV(fileName);
 
 		    }
 		}
 
 	}
 
 	
 
 }
