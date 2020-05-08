 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JViewport;
 import javax.swing.Timer;
 
 public class GameWindow extends JFrame {
 	private static final long serialVersionUID = 1L; //To suppress warning
 	static JFrame frame;
 	JPanel panel;
 	Graphics graphics;
 	Graphics2D graphics2D;
 	Rectangle rec;
 	int xGridMin = 40;
 	int yGridMin = 70;
 	int maxX;
 	int maxY;
 	int xBoardDim;
 	int yBoardDim;
 	boolean pointSelected = false;
 	int radius = 20;
 	
 	final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
 	
 	GameWindow(int _xBoardDim, int _yBoardDim) {
 		xBoardDim = _xBoardDim;
 		yBoardDim = _yBoardDim;
 		createWindow();
 	}
 	
 	public final void createWindow() {
 		setTitle("Fanorona");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(600, 600);
 		setLocationRelativeTo(null);
 		rec = new Rectangle();
 		if (getParent() instanceof JViewport) {
 	        JViewport vp = (JViewport) getParent();
 	        rec = vp.getViewRect();
 	    } else {
 	        rec = new Rectangle(0, 0, getWidth(), getHeight());
 	    }
 		maxX = (int) rec.getMaxX();
 		maxY = (int) rec.getMaxY();
 		
         ActionListener timerListener = new ActionListener()  
         {  
             public void actionPerformed(ActionEvent e)  
             {  
             	clearTime();
                 updateScreen(false);
             }  
         };  
         Timer timer = new Timer(1000, timerListener);   
         timer.start();
 		
 		addMouseListener(new MouseAdapter() {
 			@Override
             public void mousePressed(MouseEvent event) {
                 if (event.getButton() == MouseEvent.BUTTON1) {
                 	updateScreen(true);
                     processClick(event.getX(), event.getY());
                 }
 
                 if (event.getButton() == MouseEvent.BUTTON3) {
 
                 }
 			}
 		});
 		setVisible(true);
 		graphics = this.getGraphics();
 		graphics2D = (Graphics2D) graphics;
 		/* To clear the window initially, a pause is needed
 		 * otherwise the graphics objects are not usable */
 		try {
 			Thread.sleep(1000);
 			clearWindow();
 			drawGrid();
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 	}
 	
 	private void processClick(int x, int y) {
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
 		int xGridMax = xGridMin + ((xBoardDim - 1) * xSpacing);
 		int yGridMax = yGridMin + ((yBoardDim - 1) * ySpacing);
 		if (x <= xGridMin) {
 			x = 0;
 		} else if (x >= xGridMax) {
 			x = xBoardDim - 1;
 		} else {
 			double xDouble = (double) x;
 			x = (int) Math.round(((xDouble - (double)xGridMin) / (double)xSpacing));
 		}
 		if (y <= yGridMin) {
 			y = 0;
 		} else if (y >= yGridMax) {
 			y = xBoardDim - 1;
 		} else {
 			double yDouble = (double) y;
 			y = (int) Math.round(((yDouble - (double)yGridMin) / (double)ySpacing));
 		}
 		drawIndicator(x, y);
 		
 		graphics2D.drawString("Point nearest click: " + Integer.toString(x), 20, 40);
 		graphics2D.drawString("Point nearest click: " + Integer.toString(y), 20, 60);
 	}
 	
 	private void drawIndicator(int xCoord, int yCoord) {
 		pointSelected = true;
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
		int xTemp = xGridMin + xSpacing * xCoord;
		int yTemp = yGridMin + ySpacing * yCoord;
 		graphics.setColor(Color.RED);
 		graphics2D.drawLine(xTemp - radius, yTemp - radius, xTemp + radius, yTemp - radius); //Top
 		graphics2D.drawLine(xTemp - radius, yTemp - radius, xTemp - radius, yTemp + radius); //Left
 		graphics2D.drawLine(xTemp + radius, yTemp - radius, xTemp + radius, yTemp + radius); //Right
 		graphics2D.drawLine(xTemp - radius, yTemp + radius, xTemp + radius, yTemp + radius); //Bottom
 		graphics.setColor(Color.BLACK);
 	}
 	
 	private void clearWindow() {
 		graphics.setColor(Color.GRAY);
         graphics.fillRect(0, 0, maxX, maxY);
         graphics.setColor(Color.BLACK);
 	}
 	
 	private void clearTime() {
 		graphics.setColor(Color.GRAY);
         graphics.fillRect(maxX-250, 0, 240, 45);
         graphics.setColor(Color.BLACK);
 	}
 	
 	public void drawGrid() {
 		/* Do not process bad dimensions */
 		if ((xBoardDim == 1) || (yBoardDim == 1)) {
 			return;
 		}
 		
 		int xSpacing = (maxX - 2*40)/(xBoardDim - 1);
 		int ySpacing = ((maxY-30) - 2*40)/(yBoardDim - 1);
 		int xGridMax = xGridMin + ((xBoardDim - 1) * xSpacing);
 		int yGridMax = yGridMin + ((yBoardDim - 1) * ySpacing);
 		int xCurrent = xGridMin;
 		int yCurrent = yGridMin;
 
 		boolean altLeft = true;
 		boolean flipAlt = false;
 		if (xBoardDim % 2 == 0) {
 			flipAlt = true;
 		}
 		while(true) {
 			if (yCurrent != yGridMax) {
 				//Draw down line
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent,
 						yCurrent + ySpacing);
 			}
 			if ((xCurrent != xGridMin) && (yCurrent != yGridMax) && altLeft) {
 				//Draw left diagonal
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent - xSpacing,
 						yCurrent + ySpacing);
 			}
 			if ((xCurrent != xGridMax) && (yCurrent != yGridMax) && altLeft) {
 				//Draw right diagonal
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent + xSpacing,
 						yCurrent + ySpacing);
 			}
 			if (xCurrent != xGridMax) {
 				//Draw right line
 				graphics2D.drawLine(xCurrent, yCurrent, xCurrent + xSpacing,
 						yCurrent);
 			}
 			if ((xCurrent >= xGridMax) && (yCurrent >= yGridMax)) {
 				break;
 			}
 			if (xCurrent == xGridMax) {
 				if (flipAlt) {
 					altLeft = !altLeft;
 				}
 				xCurrent = xGridMin;
 				yCurrent += ySpacing;
 			} else {
 				xCurrent += xSpacing;
 			}
 			altLeft = !altLeft;
 		}
 	}
 	
 	public void updateScreen(boolean clicked) {
 		Date date = new Date();  
         String time = timeFormat.format(date);
 		RenderingHints renderHints = new RenderingHints(
 				RenderingHints.KEY_ANTIALIASING,
 				RenderingHints.VALUE_ANTIALIAS_ON);
 		graphics2D.setRenderingHints(renderHints);
 		if (clicked) {
 			clearWindow();
 			drawGrid();
 		} else {
 			clearTime();
 		}
 		
 		
 		graphics2D.drawString("Remaining move time: ", maxX-250, 40);
 		graphics2D.drawString(time + " sec", maxX-100, 40);
 	}
 }
