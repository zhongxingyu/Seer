 package View;
 
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JSlider;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import net.miginfocom.swing.MigLayout;
 
 import Model.Airport;
 import Model.AirportObserver;
 import Model.Obstacle;
 import Model.Runway;
 
 
 @SuppressWarnings("serial")
 public class SideView extends JPanel implements AirportObserver, ViewPanel{
 
 	Color toraColor = new Color(184, 134, 11);
 	Color dtColor = new Color(184, 11, 134);
 	Color todaColor = new Color(255, 0, 0);
 	Color asdaColor = new Color(0, 0, 255);
 	Color ldaColor = new Color(255, 0, 255);
 	Color resaColor = new Color(84, 84, 84);
 	
 	
 	
 	boolean visible = true;
 
 	//this value determines the amount of pixels the tag is from the end of the runway
 	final int tagBorder = 10;
 
 	//this value determines how much of the width of the panel the runway takes up.
 	double ratio = 0.95;
 	double ratio2 = 0.25;
 
 	//this value determines how much of the width of the runway the runwayTag takes up.
 	final double fontRatio = 0.5;
 
 	final double tagRotate = 1;
 
 	int spaceForScale;
 	double meterToPixel;
 	double meterToPixel2;
 	int TORA;
 	int TODA;
 	int ASDA;
 	int LDA;
 	int DT;
 	int RESA;
 	int ANGLE;
 	int stopway;
 	int TORAStart;
 	int TODAStart;
 	int ASDAStart;
 	int LDAStart;
 	int DTStart;
 	int runwayLength;
 	int declaredWidth;
 	int obstacleLength;
 	int obstacleHeight;
 	Airport airport;
 	Runway runway;
 	Obstacle obstacle;
 	String threshold;
 	boolean obstacleLeft;
 	String leftTag;
 	
 	int runwayStripWidthFromCentreLine;
 
 	//relative to panel
 	int xRunway;
 	int yRunway;
 
 	//relative to runway
 	int xObstacle;
 
 	int xOffset = 0;
 	int yOffset = 0;
 
 	int mousex;
 	int mousey;
 
 	public SideView(Airport airport){
 		super();
 		setSize(300,200);
 		this.setBackground(new Color(154, 205, 50));
 
 		updateAirport(airport);
 
 		setLayout(new MigLayout("", "[grow]", "[grow][]"));
 
 		createSlider();
 		createDraggingListeners();
 		
 		setDragCursor(this);
 		
 		setValues();
 		setVisible(true);
 
 	}
 
 	public void setZoom(double ratio){
 		this.ratio = ratio;
 	}
 	
 	public int meterToPixel(int x){
 		return (int) (meterToPixel*x);
 	}
 	
 	public int pixelToMeter(int x){
 		return (int) (x/meterToPixel);
 	}	
 	
 	public int meterToPixel2(int x){
 		return (int) (meterToPixel2*x);
 	}
 	
 	public int pixelToMeter2(int x){
 		return (int) (x/meterToPixel2);
 	}	
 	
 	
 	public void setVisible(boolean b){
 		visible=b;
 	}
 
 	public void paint (Graphics g){
 		super.paint(g);
 		if(visible){
 			g.translate(xOffset, yOffset);
 			setValues();
 			Graphics2D g2d = (Graphics2D)g;
 			runwayCreation(g2d);
 			drawDirection(g2d);
 			obstacleCreation(g2d);
 			declaredRunwaysCreation(g2d);
 			g.translate(-xOffset, -yOffset);
 			paintComponents(g2d);
 			drawKey(g2d);
 			drawScale(g2d);
 			}
 
 	}
 	
 	public void drawDirection(Graphics2D g2d){
 		if(airport.getCurrentRunway()!=null){
 			for(int i=5; i<1000; i++){
 				Font f = new Font("tag", 1, i);
 				g2d.setFont(f);
 				if(g2d.getFontMetrics().stringWidth("Runway Take-off/Landing Direction")>=meterToPixel(runwayLength/2)){
 					f = new Font("tag", 1, i-1);break;
 				}
 			}			
 		g2d.setColor(Color.BLACK);
 		g2d.drawString("Runway Take-off/Landing Direction", xRunway+meterToPixel(runwayLength/4),  yRunway+-10-meterToPixel(runwayStripWidthFromCentreLine*2));
 		
 		g2d.setStroke(new BasicStroke(3));
 		g2d.drawLine(xRunway+meterToPixel(runwayLength/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(3*runwayLength/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2));
 
 		if(airport.getCurrentRunway().getName().equals(leftTag)){
 			g2d.drawLine(xRunway+meterToPixel(3*runwayLength/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(11*runwayLength/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)+meterToPixel(runwayStripWidthFromCentreLine/4));
 			g2d.drawLine(xRunway+meterToPixel(3*runwayLength/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(11*runwayLength/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)-meterToPixel(runwayStripWidthFromCentreLine/4));
 		}else{
 			g2d.drawLine(xRunway+meterToPixel(runwayLength/4), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(5*runwayLength/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)+meterToPixel(runwayStripWidthFromCentreLine/4));
 			g2d.drawLine(xRunway+meterToPixel(runwayLength/4),yRunway-meterToPixel(runwayStripWidthFromCentreLine*2), xRunway+meterToPixel(5*runwayLength/16), yRunway-meterToPixel(runwayStripWidthFromCentreLine*2)-meterToPixel(runwayStripWidthFromCentreLine/4));
 		}
 		}
 	}
 
 	public void createSlider(){
 		final JSlider zoomSlider = new JSlider();
 		zoomSlider.setMinorTickSpacing(1);
 		zoomSlider.setBackground(new Color(154, 205, 50));
 		
 		zoomSlider.setMinimum(900);
 		zoomSlider.setMaximum(6000);
 		zoomSlider.setValue(950);
 		zoomSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				JSlider slider = ((JSlider)e.getSource());
 				setZoom((double)slider.getValue() / (double)1000);
 				repaint();
 				updateUI();
 			}
 		});
 		add(zoomSlider, "cell 0 1,alignx right,aligny bottom");
 		
 		zoomSlider.setCursor(Cursor.getDefaultCursor());
 		
 		addMouseWheelListener(new MouseWheelListener() {
 			public void mouseWheelMoved(MouseWheelEvent arg0) {
 				zoomSlider.setValue(zoomSlider.getValue() - (arg0.getWheelRotation() * 100));
 			}
 		});
 		
 		zoomSlider.setPaintLabels(false);
 		
 	}
 
 	public void createDraggingListeners(){
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				mousex = arg0.getX();
 				mousey = arg0.getY();
 			}
 		});
 		addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseDragged(MouseEvent arg0) {
 
 				int deltaMouseX = arg0.getX() - mousex;
 				int deltaMouseY = arg0.getY() - mousey;
 
 				mousex = arg0.getX();
 				mousey = arg0.getY();
 
 				setOffset(xOffset + deltaMouseX, yOffset + deltaMouseY);
 				repaint();
 				updateUI();
 			}
 		});
 	}
 	
 	public void drawKey(Graphics2D g2d){
 		g2d.setFont(new Font("key", 1, 15));
 		int textDistance = g2d.getFontMetrics().getHeight();
 		spaceForScale = 30;
 		int spaceFromLeftEdge = 10;
 		g2d.setColor(toraColor);
 		g2d.drawString("TORA", spaceFromLeftEdge, this.getHeight()- spaceForScale-textDistance);
 		g2d.setColor(todaColor);
 		g2d.drawString("TODA", spaceFromLeftEdge, this.getHeight()- spaceForScale-(2*textDistance) );
 		g2d.setColor(asdaColor);
 		g2d.drawString("ASDA",spaceFromLeftEdge, this.getHeight()- spaceForScale- (3*textDistance));
 		g2d.setColor(ldaColor);
 		g2d.drawString("LDA", spaceFromLeftEdge, this.getHeight()- spaceForScale);
 		
 		
 		g2d.setColor(dtColor);
 		g2d.drawString("DT", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale);
 		g2d.setColor(Color.WHITE);
 		g2d.drawString("STOPWAY", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale-textDistance);
 		g2d.setColor(resaColor);
 		g2d.drawString("RESA", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale-(2*textDistance));
 		g2d.setColor(Color.BLACK);
 		g2d.drawString("ANGLE", (2*spaceFromLeftEdge)+g2d.getFontMetrics().stringWidth("TODA"), this.getHeight()- spaceForScale-(3*textDistance));
 
 	}
 	
 	public void drawScale(Graphics2D g2d){
 		if(airport!=null && runway!=null){
 			int scaleWidth = Math.round(pixelToMeter(this.getWidth()/3)/100)*100;
 			g2d.setColor(Color.BLACK);
 			g2d.setFont(new Font("scale", 1, 10));
 			g2d.fillRect(10, (int) this.getHeight()-10, meterToPixel(scaleWidth), 2);	
 			g2d.fillRect(10, (int) this.getHeight()-15, (int) 2, 5);
 			g2d.fillRect((int) (8+(scaleWidth*meterToPixel)), (int) this.getHeight()-15, (int) 2, 5);
 			g2d.fillRect((int) (8+(scaleWidth/2*meterToPixel)), (int) this.getHeight()-12, (int) 2, 2);
 			g2d.drawString(Integer.toString(scaleWidth)+"m", (int) ((scaleWidth*meterToPixel)), (int) this.getHeight()-15);
 			g2d.drawString(Integer.toString(scaleWidth/2)+"m", (int) ((scaleWidth/2*meterToPixel)), (int) this.getHeight()-12);
 			g2d.drawString("0m", 8, (int) this.getHeight()-15);
 		}
 	}
 	
 	
 
 	public void runwayCreation(Graphics2D g2d){
 		if(airport!=null&& runway!=null){
 
 
 			int pWidth = this.getWidth();
 			int pHeight = this.getHeight();
 	
 	
 			int width = (int) (ratio*pWidth);
 			meterToPixel=width/(double)runwayLength;
 	
 			//calculates the x and y values to position the runway on the view
 			xRunway = (int) (((1.0-ratio)/2) * pWidth);
 			yRunway = 3*pHeight/8;
 			g2d.setColor(new Color(135,206,250));
 			g2d.fillRect(-meterToPixel((int)(runwayLength*1.5)), yRunway-meterToPixel((int)(runwayLength*1.5)), pWidth+meterToPixel((int)(runwayLength*3)), +meterToPixel((int)(runwayLength*1.5)));
 			g2d.setColor(Color.GRAY);
 			g2d.fillRect(xRunway,  yRunway, meterToPixel(runwayLength), 5);
 
 		}
 	}
 
 	public void obstacleCreation(Graphics2D g2d){
 		if(obstacle!=null){
 			g2d.setColor(Color.WHITE);
 			g2d.fillRect(meterToPixel(xObstacle)+xRunway, yRunway-meterToPixel2(obstacleHeight), meterToPixel(obstacleLength), meterToPixel2(obstacleHeight));
 			g2d.setStroke(new BasicStroke(1));
 			g2d.setColor(Color.BLACK);
 			g2d.drawRect(meterToPixel(xObstacle)+xRunway, yRunway-meterToPixel2(obstacleHeight), meterToPixel(obstacleLength), meterToPixel2(obstacleHeight));
 			g2d.drawLine(meterToPixel(xObstacle)+xRunway, yRunway-meterToPixel2(obstacleHeight), meterToPixel(xObstacle+obstacleLength)+xRunway, yRunway);
 			g2d.drawLine(meterToPixel(xObstacle+obstacleLength)+xRunway, yRunway-meterToPixel2(obstacleHeight), meterToPixel(xObstacle)+xRunway, yRunway);
 			g2d.drawLine(meterToPixel(xObstacle)+xRunway, yRunway-meterToPixel2(obstacleHeight), xRunway+meterToPixel(xObstacle)-meterToPixel((int) (obstacle.getHeight()*airport.getCurrentPhysicalRunway().getAngleOfSlope())), yRunway);
 			g2d.drawString(Integer.toString(obstacleHeight)+"m", meterToPixel(xObstacle+obstacleLength)+xRunway+10, yRunway-meterToPixel2(obstacleHeight/2));
 			g2d.setStroke(new BasicStroke(2));
 			g2d.drawLine(meterToPixel(xObstacle+obstacleLength)+xRunway+5, yRunway-meterToPixel2(obstacleHeight), meterToPixel(xObstacle+obstacleLength)+xRunway+5, yRunway );
 			
 	}
 	
 
 	}
 
 	public void declaredRunwaysCreation(Graphics2D g2d){
 		if(airport!=null){
 			int yInc = 15;
 			int width=3;
 			
 			g2d.setColor(toraColor);
 			g2d.fillRect(xRunway+TORAStart, yRunway +yInc, meterToPixel(TORA), width);
 			g2d.setColor(todaColor);
 			g2d.fillRect(xRunway+TODAStart, yRunway +2*yInc, meterToPixel(TODA), width);
 			g2d.setColor(asdaColor);
 			g2d.fillRect(xRunway+ASDAStart,  yRunway +3*yInc, meterToPixel(ASDA), width);
 			g2d.setColor(ldaColor);
 			g2d.fillRect(xRunway+LDAStart,  yRunway +4*yInc, meterToPixel(LDA), width);
 			g2d.setColor(dtColor);
 			g2d.fillRect(xRunway+DTStart,  yRunway +5*yInc, meterToPixel(DT), width);
 			
 			if(obstacle!=null){
 				g2d.setColor(Color.WHITE);
 				g2d.fillRect(xRunway+TORAStart+ meterToPixel(TORA),  (yRunway+yInc),  meterToPixel(stopway), width);
 
 				g2d.fillRect(xRunway+TODAStart+ meterToPixel(TODA),  (yRunway+yInc*2),  meterToPixel(stopway), width);
 
 				g2d.fillRect(xRunway+ASDAStart+ meterToPixel(ASDA),  (yRunway+yInc*3),  meterToPixel(stopway), width);
 
 				g2d.fillRect(xRunway+LDAStart+ meterToPixel(LDA),  (yRunway+yInc*4),  meterToPixel(stopway), width);
 				
 				
 				int greater = RESA;
 				g2d.setColor(resaColor);
 				if(greater<ANGLE){
 					g2d.setColor(Color.BLACK);
 					greater=ANGLE;
 				}
 				
 				
 				if(airport.getCurrentRunway().getName().equals(leftTag)){
 									
 					g2d.fillRect(xRunway+TORAStart+ meterToPixel(TORA+stopway),  (yRunway+yInc),  meterToPixel(greater), width);
 		
 					g2d.fillRect(xRunway+TODAStart+ meterToPixel(TODA+stopway),  (yRunway+yInc*2),  meterToPixel(greater), width);
 		
 					g2d.fillRect(xRunway+ASDAStart+ meterToPixel(ASDA+stopway),  (yRunway+yInc*3),  meterToPixel(greater), width);
 					
 					g2d.setColor(resaColor);
 		
 					g2d.fillRect(xRunway+LDAStart+ meterToPixel(LDA+stopway),  (yRunway+yInc*4),  meterToPixel(RESA), width);
 				}else{
 					
 					
 					g2d.fillRect(xRunway+LDAStart+ meterToPixel(LDA+stopway),  (yRunway+yInc*4),  meterToPixel(greater), width);
 					
 					g2d.setColor(resaColor);
 					
 					g2d.fillRect(xRunway+TORAStart+ meterToPixel(TORA+stopway),  (yRunway+yInc),  meterToPixel(RESA), width);
 		
 					g2d.fillRect(xRunway+TODAStart+ meterToPixel(TODA+stopway),  (yRunway+yInc*2),  meterToPixel(RESA), width);
 		
 					g2d.fillRect(xRunway+ASDAStart+ meterToPixel(ASDA+stopway),  (yRunway+yInc*3),  meterToPixel(RESA), width);
 		
 					
 				}
 				
 				}
 			
 		}
 	}
 
 	public void setValues(){
 		if(airport!=null && runway!=null && airport.getCurrentPhysicalRunway() != null){
 		this.runwayLength = (int) runway.getTORA(Runway.DEFAULT);
 	
 		this.TORA = (int) runway.getTORA(Runway.REDECLARED);
 		this.TODA = (int) runway.getTODA(Runway.REDECLARED);
 		this.ASDA = (int) runway.getASDA(Runway.REDECLARED);
 		this.LDA = (int) runway.getLDA(Runway.REDECLARED);
 		this.DT = (int) runway.getDisplacedThreshold(runway.DEFAULT);
 
 		this.RESA = (int) airport.getCurrentPhysicalRunway().getRESA();
 		this.stopway = (int) airport.getCurrentPhysicalRunway().getStopway();
 		this.leftTag= airport.getCurrentPhysicalRunway().getRunway(0).getName();
 		this.runwayStripWidthFromCentreLine = (int) airport.getCurrentPhysicalRunway().getRunwayStripWidth();
 				
 		int pWidth = this.getWidth();
 		int pHeight = this.getHeight();
 
 		int length = (int) (ratio*pWidth);
 		meterToPixel=length/(double)runwayLength;
 
 		
 
 		//calculates the x and y values to position the runway on the view
 		xRunway = (int) (((1.0-ratio)/2) * pWidth);
 		yRunway = (3*pHeight/4);
 		
 		
 		
 		if(obstacle!=null){
 			int distance = (int) airport.getCurrentPhysicalRunway().getDistanceAwayFromThreshold();
 			this.threshold=airport.getCurrentPhysicalRunway().closeTo().getName(); 
 			if(threshold.equals(airport.getCurrentPhysicalRunway().getRunway(0).getName())){this.leftTag=airport.getCurrentPhysicalRunway().getRunway(1).getName();}else{this.leftTag=airport.getCurrentPhysicalRunway().getRunway(0).getName();}
 			this.xObstacle=runwayLength-distance;
 			obstacleHeight = (int) obstacle.getHeight();
 			meterToPixel2 = (ratio2*this.getHeight())/obstacleHeight;
 			this.obstacleLength =(int) obstacle.getLength();
 			
 			this.ANGLE=(int) (obstacle.getHeight()*airport.getCurrentPhysicalRunway().getAngleOfSlope());
 			if(runway.getTORA(Runway.DEFAULT)==runway.getTORA(Runway.REDECLARED)){
 				ANGLE=0;
 				RESA=0;
 			}
 		}
 		int leftDT;
 		int rightDT;
 		if(leftTag.equals(airport.getCurrentPhysicalRunway().getRunway(1).getName())){
 			leftDT =(int) airport.getCurrentPhysicalRunway().getRunway(1).getDisplacedThreshold(0);
 			rightDT =(int) airport.getCurrentPhysicalRunway().getRunway(0).getDisplacedThreshold(0);
 		}else{
 			leftDT =(int) airport.getCurrentPhysicalRunway().getRunway(0).getDisplacedThreshold(0);
 			rightDT =(int) airport.getCurrentPhysicalRunway().getRunway(1).getDisplacedThreshold(0);
 		}
 		
 		if(leftDT>0){
 			DTStart=0;
 			LDAStart=DTStart+meterToPixel(DT);
 		}else{
 			DTStart=0;
 			LDAStart=0;
 		}
 	
 		this.TORAStart=0;
 		this.TODAStart=(int) (TORAStart+meterToPixel(TORA-TODA));
 		this.ASDAStart=(int) (TORAStart+meterToPixel(TORA-ASDA));
 		
 		}
 	}
 
 	public void updateAirport(Airport airport) {
 		this.airport=airport;
 		if(airport!=null &&
 				airport.getCurrentPhysicalRunway() != null)
 		{
 			runway=airport.getCurrentRunway();
 			obstacle=airport.getCurrentPhysicalRunway().getObstacle();
 			setOffset(0, 0);
 			setValues();
 			repaint();
 		}
 		
 		
 
 	}
 
 	public void setOffset(int x, int y){
 		int a = meterToPixel(runwayLength/2);
 		int b = meterToPixel(this.getHeight());
 		if(x>a){
 			x=a;
 		}
 		if(y>b){
 			y=b;
 		}
 		if(x<-a){
 			x=-a;
 		}
 		if(y<-b){
 			y=-b;
 		}
 		this.xOffset=x;
 		this.yOffset=y;
 	}
 
 	public void setDragCursor(final JComponent component){
 		Cursor openHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("data/opengrab.png"), new Point(16,16), "closed");
 		component.setCursor(openHandCursor);	
 
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent e) {
 				super.mousePressed(e);
 				Cursor closedHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("data/closedgrab.png"), new Point(16,16), "closed");
 				component.setCursor(closedHandCursor);	
 
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent e) {
 				super.mouseReleased(e);
 				Cursor openHandCursor = Toolkit.getDefaultToolkit().createCustomCursor(Toolkit.getDefaultToolkit().getImage("data/opengrab.png"), new Point(16,16), "closed");
 				component.setCursor(openHandCursor);	
 			}
 		});
 	}
 	
 }
 
 
 
