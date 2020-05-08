 package gui;
 
 import imgUtil.ImgCommonUtil;
 import imgUtil.FourierProcessingUtil;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.util.LinkedList;
 
 import javax.swing.JPanel;
 
 import mathUtil.Coordinate2D;
 import model.SrcImage;
 
 
 import controller.IMyController;
 
 public class SrcImgPanel extends JPanel implements MouseListener, KeyListener{
 	
 	private static final int UP_MARGIN = 0;
 
 	private static final int LEFT_MARGIN = 0;
 	
 	private static final int PANEL_WIDTH = 800;
 	
 	private static final int PANEL_HEIGHT = 800;
 	
 	private static final Color POINT_COLOR = Color.GREEN;
 	
 	private static final Color FORMER_COLOR = Color.RED;
 	
 	private static final Color LATTER_COLOR = Color.BLUE;
 
 //	private int width;
 //	
 //	private int height;
 	
 	
 	private static final int NEEDLE_LENGTH = 80;
 	
 	private static final int GRID_GAP = 32;
 	
 	private IMyView mainView;
 	
 	private SrcImage srcImg;
 	
 	private int patchWidth;
 	
 	private int patchHeight;
 	
 	
 	
 	
 	private Coordinate2D formerCoord;
 	
 	private Coordinate2D latterCoord;
 	
 	private Coordinate2D centerCoord;
 	
 	
 	private boolean leftPicked;
 	
 	private boolean rightPicked;
 	
 	private boolean centerPicked;
 	
 	private boolean gridSelected;
 	
 	
 	
 	private static final int FORMER = 1;
 	
 	private static final int LATTER = 2;
 	
 	private int active;
 	
 	
 	private LinkedList<double[]> angles;
 	
 	private LinkedList<double[]> angleBuf;
 	
 	private LinkedList<int[]> positions;
 	
 	private LinkedList<Color> needleColor;
 	
 	
 	
 	public SrcImgPanel(){
 		
 	}
 	
 	public SrcImgPanel(SrcImage srcImg){
 		this.srcImg = srcImg;
 		
 //		if(srcImg != null){
 //		
 //			width = srcImg.getWidth();
 //		
 //			height = srcImg.getHeight();
 //		}
 		
 		
 	
 	}
 
 	
 
 	
 	public void mousePressed(MouseEvent e) {
 		this.requestFocus();
 		
 		if(javax.swing.SwingUtilities.isLeftMouseButton(e)){
 			
 			formerCoord.setCoordinate(e.getX(), e.getY(), srcImg.getWidth(), srcImg.getHeight());
 			
 			leftPicked = true;
 			
 			active = FORMER;
 		}
 		else if(javax.swing.SwingUtilities.isRightMouseButton(e)){
 			
 			latterCoord.setCoordinate(e.getX(), e.getY(), srcImg.getWidth(), srcImg.getHeight());
 			
 			rightPicked = true;
 			
 			active = LATTER;
 		}
 		else if(javax.swing.SwingUtilities.isMiddleMouseButton(e)){
 			
 			centerCoord.setCoordinate(e.getX(), e.getY(), srcImg.getWidth(), srcImg.getHeight());
 			
 			System.out.println("Center: " + centerCoord.toString());
 			
 			centerPicked = true;
 			
 			gridSelected = true;
 		}
 		
 		this.repaint();
 		
 	}
 	
 	
 	
 	public void keyPressed(KeyEvent e) {
 		
 		if(!leftPicked && !rightPicked && !centerPicked)
 			return;
 		
 		/*
 		if(e.getKeyChar() == 'f'){
 			
 			if(leftPicked){
 				System.out.println("Former:");
 				mainView.patchSelected(formerCoord);
 				
 			}if(rightPicked){
 				System.out.println("Latter:");
 				mainView.patchSelected(latterCoord);
 				
 			}
 			
 			
 		}
 		*/
 		if(e.getKeyChar() == 'r'){ // estimate the orientation by any two patches selected
 		
 			if(leftPicked && rightPicked){
 			
 				estimateByFormerLatter();
 				
 				this.repaint();
 			}
 		}
 		
 		
 		else if(e.getKeyChar() == 'd'){ // estimate the orientation by two pairs of diagonal patches around a center point
 			
 			if(centerPicked){
 			
 				estimateByCenterDiagonal();
 				
 				this.repaint();
 			}
 			
 			
 		}
 		else if(e.getKeyChar() == 'c'){
 			
 			if(centerPicked){
 
 				estimateByCenterCross();
 				
 				this.repaint();
 			}
 			
 			
 		}
 		else if(e.getKeyChar() == 's'){
 			
 			System.out.println("Simple -- Diagonal & Cross");
 			
 			if(centerPicked){
 				
 				
 				estimateSingle(centerCoord.getX(), centerCoord.getY());
 				
 				this.repaint();
 			}
 			
 		}
 		
 		else if(e.getKeyChar() == 'e'){
 			
 			System.out.println("Energy -- Diagonal & Cross");
 			
 			if(centerPicked){
 				
 				
 				estimateSingleEnergy(centerCoord.getX(), centerCoord.getY());
 				
 				this.repaint();
 			}
 			
 			
 			
 		}
 		
 		else if(e.getKeyChar() == 'z'){
 			
 			System.out.println("Zhankai");
 			if(centerPicked){
 
 				
 				
 				
 				double[] avgAngle = estimateGrid(GRID_GAP);
 				
 				
 				System.out.println(avgAngle[0]);
 				System.out.println(avgAngle[1]);
 			}
 			
 		}
 	
 		else if(e.getKeyChar() == 'p'){ // pre-processing of the target image
 			
 			
 			this.srcImg = new SrcImage(FourierProcessingUtil.bandpassFiltering(srcImg.getSrcImg(), 0.1, 0.45));
 			
 			this.repaint();
 		}
 		
 		else{ // move point
 		
 			Coordinate2D activeCoord = null;
 			
 			if(active == FORMER)
 				activeCoord = formerCoord;
 			else if(active == LATTER)
 				activeCoord = latterCoord;
 			
 			
 			moveCoordinate(activeCoord, e.getKeyCode(), 1);
 		
 			
 			this.repaint();
 		}
 		
 	}
 	
 	
 	
 	///////////////////////////////////////////////////////////////////////////////////////////////////////
 	
 	
 	
 	private void estimateByFormerLatter(){
 		
 		
 			
 			centerPicked = true;
 			needleColor.add(Color.GREEN);
 			
 			int centerX = (formerCoord.getX() + latterCoord.getX())/2;
 			int centerY = (formerCoord.getY() + latterCoord.getY())/2;
 			
 			centerCoord.setCoordinate(centerX, centerY, srcImg.getWidth(), srcImg.getHeight());
 			
 			
 			mainView.shapeEstimate(formerCoord, latterCoord);
 			
 			
 		
 	}
 	
 	
 	private void estimateByCenterDiagonal(){
 		
 			
 			int topLeftX = centerCoord.getX() - patchWidth/2;
 			int topLeftY = centerCoord.getY() - patchHeight/2;
 			
 			int bottomRightX = centerCoord.getX() + patchWidth/2;
 			int bottomRightY = centerCoord.getY() + patchHeight/2;
 					
 //			needleColor = Color.RED;
 			needleColor.add(Color.GREEN);
 			
 			mainView.shapeEstimate(new Coordinate2D(topLeftX, topLeftY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomRightX, bottomRightY, srcImg.getWidth(), srcImg.getHeight()));
 			
 			
 			int topRightX = centerCoord.getX() + patchWidth/2;
 			int topRightY = centerCoord.getY() + patchHeight/2;
 			
 			int bottomLeftX = centerCoord.getX() - patchWidth/2;
 			int bottomLeftY = centerCoord.getY() - patchHeight/2;
 					
 			needleColor.add(Color.GREEN);
 			
 			mainView.shapeEstimate(new Coordinate2D(topRightX, topRightY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomLeftX, bottomLeftY, srcImg.getWidth(), srcImg.getHeight()));
 			
 			
 		
 		
 	}
 	
 	
 	private void estimateByCenterCross(){
 		
 
 		int leftX = centerCoord.getX() - patchWidth/2;
 		int leftY = centerCoord.getY();
 		
 		int rightX = centerCoord.getX() + patchWidth/2;
 		int rightY = centerCoord.getY();
 				
 //		needleColor = Color.RED;
 		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(leftX, leftY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(rightX, rightY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		int topX = centerCoord.getX();
 		int topY = centerCoord.getY() + patchHeight/2;
 		
 		int bottomX = centerCoord.getX();
 		int bottomY = centerCoord.getY() - patchHeight/2;
 				
 		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(topX, topY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomX, bottomY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 	}
 	
 	
 	private double[] estimateGrid(int gridGap){
 		
 		int centerX = centerCoord.getX();
 		int centerY = centerCoord.getY();
 		
 		double slantAll = 0;
 		double tiltAll = 0;
 		
 		LinkedList<double[]> resultList = new LinkedList<double[]>();
 		
 		System.out.println("#1");
 		resultList.add(estimateSingle(centerX, centerY)); //#1
 		System.out.println("#2");
 		resultList.add(estimateSingle(centerX - gridGap, centerY)); //#2
 		System.out.println("#3");
 		resultList.add(estimateSingle(centerX + gridGap, centerY)); //#3
 		System.out.println("#4");
 		resultList.add(estimateSingle(centerX, centerY - gridGap)); //#4
 		System.out.println("#5");
 		resultList.add(estimateSingle(centerX, centerY + gridGap)); //#5
 		System.out.println("#6");
 		resultList.add(estimateSingle(centerX - gridGap, centerY - gridGap)); //#6
 		System.out.println("#7");
 		resultList.add(estimateSingle(centerX + gridGap, centerY - gridGap)); //#7
 		System.out.println("#8");
 		resultList.add(estimateSingle(centerX - gridGap, centerY + gridGap)); //#8
 		System.out.println("#9");
 		resultList.add(estimateSingle(centerX + gridGap, centerY + gridGap)); //#9
 		
 		for(int i = 0; i < resultList.size(); i++){
 			double[] result = resultList.get(i);
 			
 			slantAll += result[0];
 			tiltAll += result[1];
 			
 			
 		}
 		
 		double[] avgAngle = {slantAll/resultList.size(), tiltAll/resultList.size()};
 		
 		return avgAngle;
 		
 	}
 	
 	
 	private double[] estimateSingle(int centerX, int centerY){
 		
 
 		//Diagonal
 		int topLeftX = centerX - patchWidth/2;
 		int topLeftY = centerY - patchHeight/2;
 		
 		int bottomRightX = centerX + patchWidth/2;
 		int bottomRightY = centerY + patchHeight/2;
 				
 
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(topLeftX, topLeftY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomRightX, bottomRightY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		int topRightX = centerX + patchWidth/2;
 		int topRightY = centerY + patchHeight/2;
 		
 		int bottomLeftX = centerX - patchWidth/2;
 		int bottomLeftY = centerY - patchHeight/2;
 				
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(topRightX, topRightY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomLeftX, bottomLeftY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		
 		//Cross
 		int leftX = centerX - patchWidth/2;
 		int leftY = centerY;
 		
 		int rightX = centerX + patchWidth/2;
 		int rightY = centerY;
 				
 
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(leftX, leftY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(rightX, rightY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		int topX = centerX;
 		int topY = centerY + patchHeight/2;
 		
 		int bottomX = centerX;
 		int bottomY = centerY - patchHeight/2;
 				
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimate(new Coordinate2D(topX, topY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomX, bottomY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		double slantAll = 0;
 		double tiltAll = 0;
 		
 		for(int i = 0; i < angleBuf.size(); i++){
 			
 			double[] angle = angleBuf.get(i);
 			
 			slantAll += angle[0];
 			tiltAll += angle[1];
 			
 		}
 		double[] avgAngle = {slantAll/angleBuf.size(), tiltAll/angleBuf.size()};
 		
 		System.out.println("Single Avg: slant = " + avgAngle[0] + ", tilt = " + avgAngle[1]);
 		
 
 		
 		angleBuf.clear();
 		
 		angles.add(avgAngle);
 		
 		int[] position = {centerCoord.getX(), centerCoord.getY()};
 		
 		positions.add(position);
 		
 		needleColor.add(Color.GREEN);
 		
 		return avgAngle;
 	}
 	
 
 	private double[] estimateSingleEnergy(int centerX, int centerY){
 		
 
 		//Diagonal
 		int topLeftX = centerX - patchWidth/2;
 		int topLeftY = centerY - patchHeight/2;
 		
 		int bottomRightX = centerX + patchWidth/2;
 		int bottomRightY = centerY + patchHeight/2;
 				
 
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimateEnergy(new Coordinate2D(topLeftX, topLeftY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomRightX, bottomRightY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		int topRightX = centerX + patchWidth/2;
 		int topRightY = centerY + patchHeight/2;
 		
 		int bottomLeftX = centerX - patchWidth/2;
 		int bottomLeftY = centerY - patchHeight/2;
 				
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimateEnergy(new Coordinate2D(topRightX, topRightY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomLeftX, bottomLeftY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		
 		//Cross
 		int leftX = centerX - patchWidth/2;
 		int leftY = centerY;
 		
 		int rightX = centerX + patchWidth/2;
 		int rightY = centerY;
 				
 
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimateEnergy(new Coordinate2D(leftX, leftY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(rightX, rightY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		
 		int topX = centerX;
 		int topY = centerY + patchHeight/2;
 		
 		int bottomX = centerX;
 		int bottomY = centerY - patchHeight/2;
 				
 //		needleColor.add(Color.GREEN);
 		
 		mainView.shapeEstimateEnergy(new Coordinate2D(topX, topY, srcImg.getWidth(), srcImg.getHeight()), new Coordinate2D(bottomX, bottomY, srcImg.getWidth(), srcImg.getHeight()));
 		
 		double slantAll = 0;
 		double tiltAll = 0;
 		
 		for(int i = 0; i < angleBuf.size(); i++){
 			
 			double[] angle = angleBuf.get(i);
 			
 			slantAll += angle[0];
 			tiltAll += angle[1];
 			
 		}
 		double[] avgAngle = {slantAll/angleBuf.size(), tiltAll/angleBuf.size()};
 		
 		System.out.println("Energy Avg: slant = " + avgAngle[0] + ", tilt = " + avgAngle[1]);
 		
 		angleBuf.clear();
 		
 		angles.add(avgAngle);
 		
 		int[] position = {centerCoord.getX(), centerCoord.getY()};
 		
 		positions.add(position);
 		
 		needleColor.add(Color.YELLOW);
 		
 		return avgAngle;
 	}
 	
 	
 	public void attatchNormalNeedle(double slant, double tilt){
 		
 		double[] estimatedAngles = {slant, tilt}; 
 		
 		angleBuf.add(estimatedAngles);
 		
 //		int[] position = {centerCoord.getX(), centerCoord.getY()};
 //		
 //		positions.add(position);
 		
 //		this.repaint();
 	}
 	
 	
 	
 	
 	protected void paintComponent(Graphics g) {
 		if(srcImg != null)
 			g.drawImage(srcImg.getSrcImg(), 0, 0, this);
         
         
         drawBoundary(g);
         
         drawNeedle(g);
         
 //        drawGrid(g);
 	}
 	
 	
	/*
 	private void drawGrid(Graphics g){
 		
 		if(gridSelected){
 			
 			
 			g.setColor(POINT_COLOR);
         	
         	g.drawRect(centerCoord.getX() - 1, centerCoord.getY() - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - GRID_GAP - 1, centerCoord.getY() - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() + GRID_GAP - 1, centerCoord.getY() - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - 1, centerCoord.getY() - GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - 1, centerCoord.getY() + GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - GRID_GAP - 1, centerCoord.getY() - GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() + GRID_GAP - 1, centerCoord.getY() - GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() - GRID_GAP - 1, centerCoord.getY() + GRID_GAP - 1, 2, 2);
         	
         	g.drawRect(centerCoord.getX() + GRID_GAP - 1, centerCoord.getY() + GRID_GAP - 1, 2, 2);
 			
 		}
 		
 	}
	*/
 	
 	
 
 	private void drawNeedle(Graphics g){
 		
 		if(!centerPicked)
 			return;
 		((Graphics2D)g).setStroke(new BasicStroke(3.0f));
 		
 		for(int i = 0; i < angles.size(); i++){
 			
 			int[] position = positions.get(i); 
 		
 			double[] angle = angles.get(i);
 			
 			double slant = Math.toRadians(angle[0]);
 			
 			double tilt = Math.toRadians(angle[1]);
 			
 			
 			int x1 = position[0];
 			
 			int y1 = position[1];
 			
 			
 			
 			
 			int x2 = (int) Math.round(x1 + NEEDLE_LENGTH * Math.sin(slant) * Math.cos(tilt));
 			
 			int y2 = (int) Math.round(y1 - NEEDLE_LENGTH * Math.sin(slant) * Math.sin(tilt));
 			
 			g.setColor(needleColor.get(i));
 			
 			g.drawLine(x1, y1, x2, y2);
 			
 			g.setColor(Color.RED);
 			
 			g.drawLine(x1, y1, x1, y1);
 			
 			
 			
 		}
 		System.out.println("Center" + centerCoord.toString());
 		
 	}
 	
 	private void drawBoundary(Graphics g){
 		
 		if(leftPicked){
         	
         	System.out.println("Former" + formerCoord.toString());
         	
         	g.setColor(POINT_COLOR);
         	
         	g.drawRect(formerCoord.getX() - 1, formerCoord.getY() - 1, 2, 2);
 
         	g.setColor(FORMER_COLOR);
         	
         	g.drawRect(formerCoord.getX() - (patchWidth - 1) / 2, formerCoord.getY() - (patchHeight - 1) / 2, patchWidth, patchHeight);
         	
         	
         	
 //        	g.drawLine(formerCoord.getX(), formerCoord.getY(), formerCoord.getX(), formerCoord.getY());
         	
         }
        if(rightPicked){
 
        	System.out.println("Latter" + latterCoord.toString());
        	
        	g.setColor(POINT_COLOR);
        	
        	g.drawRect(latterCoord.getX() - 1, latterCoord.getY() - 1, 2, 2);
        	
        	g.setColor(LATTER_COLOR);
        	
        	g.drawRect(latterCoord.getX() - (patchWidth - 1) / 2, latterCoord.getY() - (patchHeight - 1) / 2, patchWidth, patchHeight);
        	
     	   
         }
 		
 	}
 	
 	
 	private void moveCoordinate(Coordinate2D activeCoord, int direction, int step){
 		
 		
 		
 		if(direction == KeyEvent.VK_UP){
 			
 			activeCoord.moveUp(step);
 			
 		}
 		else if(direction == KeyEvent.VK_DOWN){
 			
 			activeCoord.moveDown(step);
 			
 		}
 		else if(direction == KeyEvent.VK_LEFT){
 			
 			activeCoord.moveLeft(step);
 			
 		}
 		else if(direction == KeyEvent.VK_RIGHT){
 			
 			activeCoord.moveRight(step);
 			
 		}
 		
 	}
 	
 	
 
 	public void init() {
 		// TODO Auto-generated method stub
 		
 		this.setLayout(null);
 		
 		this.setBounds(LEFT_MARGIN, UP_MARGIN, PANEL_WIDTH, PANEL_HEIGHT);
 		
 		this.setFocusable(true);
 		
 		
 				
 		this.addMouseListener(this);
 		
 		this.addKeyListener(this);
 		
 		
 		formerCoord = new Coordinate2D();
 		latterCoord = new Coordinate2D();
 		centerCoord = new Coordinate2D();
 		
 		angles = new LinkedList<double[]>();
 		
 		angleBuf = new LinkedList<double[]>();
 		
 		positions = new LinkedList<int[]>();
 		
 		needleColor = new LinkedList<Color>();
 		
 		patchWidth = mainView.getPatchWidth();
 		
 		patchHeight = mainView.getPatchHeight();
 		
 	}
 
 	
 	public void setMainView(IMyView myView){
 		
 		this.mainView = myView;
 		
 	}
 	
 
 	
 	public void keyReleased(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void keyTyped(KeyEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void mouseClicked(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void mouseEntered(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 	public void mouseExited(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 
 	
 	public void mouseReleased(MouseEvent arg0) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	
 
 }
