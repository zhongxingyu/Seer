 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Ellipse2D;
 import java.awt.image.BufferStrategy;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 import javax.swing.JFrame;
 import javax.swing.JViewport;
 import javax.swing.Timer;
 
 public class GameWindow extends JFrame {
 	private static final long serialVersionUID = 1L; //To suppress warning
 	static JFrame frame;
 	BufferStrategy bufferStrat;
 	Graphics graphics;
 	Rectangle rec;
 	int xGridMin = 40;
 	int yGridMin = 70;
 	int xGridMax;
 	int yGridMax;
 	int xMax;
 	int yMax;
 	int xSpacing;
 	int ySpacing;
 	int xBoardDim;
 	int yBoardDim;
 	int xClick;
 	int yClick;
 	int xLastCoord = -1;
 	int yLastCoord = -1;
 	int radius;
 	boolean clicked = false;
 	boolean initialUpdate = true;
 	
 	private enum selectionStates {
		NONE,
		FIRSTPIECE,
		SECONDCOORD;
 	}
	selectionStates currentSelectState = selectionStates.NONE;
 	
 	private enum pieceType {
 		BLACK,
 		WHITE,
 		SACRIFICED
 	}
 	
 	final DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
 	
 	public GameWindow(int _xBoardDim, int _yBoardDim) {
 		xBoardDim = _xBoardDim;
 		yBoardDim = _yBoardDim;
 		createWindow();
 	}
 	
 	private final void createWindow() {
 		setTitle("Fanorona");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(600, 600);
 		setLocationRelativeTo(null);
 		setVisible(true);
 		this.createBufferStrategy(2);
 		
 		/* Set window dimensions used elsewhere */
 		rec = new Rectangle();
 		if (getParent() instanceof JViewport) {
 	        JViewport vp = (JViewport) getParent();
 	        rec = vp.getViewRect();
 	    } else {
 	        rec = new Rectangle(0, 0, getWidth(), getHeight());
 	    }
 		xMax = (int) rec.getMaxX();
 		yMax = (int) rec.getMaxY();
 
 		calculateDimensions();
 		if (radius > xGridMin) {
 			xGridMin = (radius) + 10;
 			yGridMin = (radius) + 40;
 			calculateDimensions();
 		}
 		
         ActionListener timerListener = new ActionListener()  
         {
             public void actionPerformed(ActionEvent e)  
             {
             	updateScreen();
             }
         };
         Timer timer = new Timer(1000, timerListener);   
         timer.start();
 		
 		addMouseListener(new MouseAdapter() {
 			@Override
             public void mousePressed(MouseEvent event) {
                 if (event.getButton() == MouseEvent.BUTTON1) {
                 	clicked = true;
                 	xClick = event.getX();
                 	yClick = event.getY();
                 	updateScreen();
                 }
 
                 if (event.getButton() == MouseEvent.BUTTON3) {
 
                 }
 			}
 		});
 	}
 	
 	private void calculateDimensions() {
		/* TODO Add stroke size scaling coefficient */
 		xSpacing = (xMax - 2 * xGridMin)/(xBoardDim - 1);
 		ySpacing = ((yMax - 30) - 2 * xGridMin)/(yBoardDim - 1);
 		if (xSpacing >= ySpacing) {
 			radius = (int) (0.375 * (double) ySpacing);
 		} else {
 			radius = (int) (0.375 * (double) xSpacing);
 		}
 		xGridMax = xGridMin + ((xBoardDim - 1) * xSpacing);
 		yGridMax = yGridMin + ((yBoardDim - 1) * ySpacing);
 	}
 	
 	private void processClick(int x, int y) {
 		xClick = -1;
 		yClick = -1;
 		int xTemp;
 		int yTemp;
 		if (x <= xGridMin) {
 			xTemp = 0;
 		} else if (x >= xGridMax) {
 			xTemp = xBoardDim - 1;
 		} else {
 			double xDouble = (double) x;
 			xTemp = (int) Math.round(((xDouble -
 					(double)xGridMin) / (double)xSpacing));
 		}
 		if (y <= yGridMin) {
 			yTemp = 0;
 		} else if (y >= yGridMax) {
 			yTemp = yBoardDim - 1;
 		} else {
 			double yDouble = (double) y;
 			yTemp = (int) Math.round(((yDouble -
 					(double)yGridMin) / (double)ySpacing));
 		}
 		drawIndicator(xTemp, yTemp, x, y);
 
 		graphics.drawString("Point nearest click: " +
 				Integer.toString(xTemp), 20, 40);
 		graphics.drawString("Point nearest click: " +
 				Integer.toString(yTemp), 20, 60);
 	}
 
 	private void drawIndicator(int xCoord, int yCoord, int xActual,
 			int yActual) {
 		int xTemp = xGridMin + xSpacing * xCoord;
 		int yTemp = yGridMin + ySpacing * yCoord;
 
 		double distance = Math.sqrt(((xTemp - xActual) * (xTemp - xActual)) +
 				((yTemp - yActual) * (yTemp - yActual)));
 		if (distance <= radius) {
			if (currentSelectState == selectionStates.FIRSTPIECE) {
 				if (xCoord == xLastCoord && yCoord == yLastCoord) {
 					/* Currently selected piece is selected again. This is
 					 * interpreted as a deselect action */
 					/* TODO Check to see if the selected coordinate has a
 					 * selectable piece in it, otherwise take no action */
					currentSelectState = selectionStates.NONE;
 					return;
 				} else {
 					/* A location different from the first is selected. This
 					 * is interpreted as an attempted move action. */
					currentSelectState = selectionStates.SECONDCOORD;
 					drawSelection(xTemp, yTemp);
 				}
 			}
 
 			xLastCoord = xCoord;
 			yLastCoord = yCoord;
			currentSelectState = selectionStates.FIRSTPIECE;
 			drawSelection(xTemp, yTemp);
 
 		} else {
			currentSelectState = selectionStates.NONE;
 			xLastCoord = -1;
 			yLastCoord = -1;
 		}
 	}
 	
 	private void drawSelection(int xPoint, int yPoint) {
		if (currentSelectState == selectionStates.FIRSTPIECE) {
 			graphics.setColor(Color.RED);
 		} else {
 			graphics.setColor(Color.BLUE);
 		}
 		Graphics2D graphics2D = (Graphics2D) graphics;
 	    graphics2D.setStroke(new BasicStroke(2F));
 		graphics.drawLine(xPoint - radius, yPoint - radius,
 				xPoint + radius, yPoint - radius); //Top
 		graphics.drawLine(xPoint - radius, yPoint - radius,
 				xPoint - radius, yPoint + radius); //Left
 		graphics.drawLine(xPoint + radius, yPoint - radius,
 				xPoint + radius, yPoint + radius); //Right
 		graphics.drawLine(xPoint - radius, yPoint + radius,
 				xPoint + radius, yPoint + radius); //Bottom
 		graphics.setColor(Color.BLACK);
 		graphics2D.setStroke(new BasicStroke(0F));
 		
 		/* REMOVE THIS LINE */
 		drawPiece(pieceType.WHITE, xPoint, yPoint);
 	}
 	
 	private void clearWindow() {
 		graphics.setColor(Color.GRAY);
         graphics.fillRect(0, 0, xMax+20, yMax);
         graphics.setColor(Color.BLACK);
 	}
 	
 	private void clearTime() {
 		graphics.setColor(Color.GRAY);
         graphics.fillRect(xMax-250, 0, 240, 45);
         graphics.setColor(Color.BLACK);
 	}
 
 	private void drawGrid() {
 		/* TODO Add special processing for 1xX and Xx1 board sizes */
 		if ((xBoardDim == 1) || (yBoardDim == 1)) {
 			return;
 		}
 
 		int xCurrent = xGridMin;
 		int yCurrent = yGridMin;
 
 		Graphics2D graphics2D = (Graphics2D) graphics;
 		graphics2D.setStroke(new BasicStroke(7F));
 
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
 		graphics2D.setStroke(new BasicStroke(0F));
 	}
 
 	private void drawPiece(pieceType pT, int xPoint, int yPoint) {
 		if (pT == pieceType.WHITE) {
 			graphics.setColor(Color.WHITE);
 		} else if (pT == pieceType.BLACK) {
 			graphics.setColor(Color.BLACK);
 		} else {
 			graphics.setColor(Color.GRAY);
 		}
 		Graphics2D graphics2D = (Graphics2D) graphics;
 		int x = (int) (((double) xPoint) - ((double) radius));
 		int y = (int) (((double) yPoint) - ((double) radius));
 		Ellipse2D.Double circle = new Ellipse2D.Double(x, y,
 				radius * 2, radius * 2);
 		graphics2D.fill(circle);
 		graphics.setColor(Color.BLACK);
 	}
 	
 	
 	public void drawPieces() {
 		/* Iterate over pieces here */
 		/* xCoord and yCoord points start at the top left corner with (0,0)
 		 * and go to the bottom left corner with (n,m) where n and m are the
 		 * x and y dimensions of the board, respectively */
 		int xCoord = 4;
 		int yCoord = 4;
 		int xPoint = xGridMin + (xSpacing * xCoord);
 		int yPoint = yGridMin + (ySpacing * yCoord);
 	}
 	
 	private void updateScreen() {
 		Date date = new Date();
 		String time = timeFormat.format(date);
 
 		bufferStrat = this.getBufferStrategy();
 		graphics = null;
 
 		try {
 			graphics = bufferStrat.getDrawGraphics();
 			Graphics2D graphics2D = (Graphics2D) graphics;
 			RenderingHints renderHints = new RenderingHints(
 					RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			graphics2D.setRenderingHints(renderHints);
 			if (clicked || initialUpdate) {
 				clicked = false;
 				clearWindow();
 				drawGrid();
 				drawPieces();
 				processClick(xClick, yClick);
 			} else {
 
 			}
 			clearTime();
 			graphics.drawString("Remaining move time: ", xMax - 250, 40);
 			graphics.drawString(time + " sec", xMax - 100, 40);
 
 		} catch (Exception e) {
 			System.out.println("Error: " + e.getMessage());
 		} finally {
 
 		}
 		graphics.dispose();
 		bufferStrat.show();
 		Toolkit.getDefaultToolkit().sync();
 		initialUpdate = false;
 	}
 }
