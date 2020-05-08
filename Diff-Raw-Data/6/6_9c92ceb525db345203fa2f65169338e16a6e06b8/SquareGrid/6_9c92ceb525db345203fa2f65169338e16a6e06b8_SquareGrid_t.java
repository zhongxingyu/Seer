 package net.rptools.maptool.model;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.geom.Area;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.SwingUtilities;
 
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppState;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.ScreenPoint;
 import net.rptools.maptool.client.ui.zone.ZoneRenderer;
 import net.rptools.maptool.client.walker.ZoneWalker;
 import net.rptools.maptool.client.walker.astar.AStarSquareEuclideanWalker;
 
 public class SquareGrid extends Grid {
 
 	private static final Dimension CELL_OFFSET = new Dimension(0, 0);
 	
 	private static BufferedImage pathHighlight;
 	
 	private static List<TokenFootprint> footprintList;
 
 	static {
 		try {
 			pathHighlight = ImageUtil.getCompatibleImage("net/rptools/maptool/client/image/whiteBorder.png");
 			
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 		}
 	}
 	
 	private static final GridCapabilities CAPABILITIES = new GridCapabilities() {
 		public boolean isPathingSupported() {return true;}
 		public boolean isSnapToGridSupported() {return true;}
 		public boolean isPathLineSupported() {return true;}
 		public boolean isSecondDimensionAdjustmentSupported() {return false;}
 		public boolean isCoordinatesSupported() {return true;}
 	};
 
 	private static final int[] FACING_ANGLES = new int[] {
 		-135, -90, -45, 0, 45, 90, 135, 180
 	};
 	
 	public SquareGrid() {
 		super();
 		
 	}
 
 	@Override
 	public void drawCoordinatesOverlay(Graphics2D g, ZoneRenderer renderer) {
 
 		Object oldAA = SwingUtil.useAntiAliasing(g);
 		
		Font oldFont = g.getFont();
		g.setFont(g.getFont().deriveFont(20f).deriveFont(Font.BOLD));
 		FontMetrics fm = g.getFontMetrics();
 		
 		double cellSize = renderer.getScaledGridSize();
 		CellPoint topLeft = convert(new ScreenPoint(0, 0).convertToZone(renderer));
 		ScreenPoint sp = ScreenPoint.fromZonePoint(renderer, convert(topLeft));
 		
 		
 		Dimension size = renderer.getSize();
 		
 		int startX = SwingUtilities.computeStringWidth(fm, "MMM") + 10;
 		
 		double x = sp.x + cellSize/2; // Start at middle of the cell that's on screen
 		int nextAvailableSpace = -1;
 		while (x < size.width) {
 
 			String coord = Integer.toString(topLeft.x);
 			
 			int strWidth = SwingUtilities.computeStringWidth(fm, coord);
 			int strX = (int)x - strWidth/2;
 
 			if (x > startX && strX > nextAvailableSpace) {
 				g.setColor(Color.black);
 				g.drawString(coord, strX, fm.getHeight());
 				g.setColor(Color.orange);
 				g.drawString(coord, strX-1, fm.getHeight()-1);
 				
 				nextAvailableSpace = strX + strWidth + 10;
 			}
 			
 			x += cellSize;
 			topLeft.x ++;
 		}
 		
 		double y = sp.y + cellSize/2; // Start at middle of the cell that's on screen
 		nextAvailableSpace = -1;
 		while (y < size.height) {
 			
 			String coord = Integer.toString(topLeft.y);
 			
 			int strY = (int)y + fm.getAscent()/2;
 
 			if (y > fm.getHeight() && strY > nextAvailableSpace) {
 			
 				g.setColor(Color.black);
 				g.drawString(coord, 10, strY);
 				g.setColor(Color.yellow);
 				g.drawString(coord, 10-1, strY-1);
 				
 				nextAvailableSpace = strY + fm.getAscent()/2 + 10;
 			}
 			
 			y += cellSize;
 			topLeft.y ++;
 		}
 		
		g.setFont(oldFont);
 		SwingUtil.restoreAntiAliasing(g, oldAA);
 	}
 	
 	@Override
 	public List<TokenFootprint> getFootprints() {
 		if (footprintList == null) {
 			try {
 				footprintList = loadFootprints("net/rptools/maptool/model/squareGridFootprints.xml");
 			} catch (IOException ioe) {
 				ioe.printStackTrace();
 				MapTool.showError("Could not load square grid footprints");
 			}
 
 		}
 		return footprintList;
 	}
 	
 	@Override
 	public Rectangle getBounds(CellPoint cp) {
 		
 		return new Rectangle(cp.x * getSize(), cp.y * getSize(), getSize(), getSize());
 	}
 	
 	@Override
 	public BufferedImage getCellHighlight() {
 		return pathHighlight;
 	}
 	
 	@Override
 	protected Area createCellShape(int size) {
 		return new Area(new Rectangle(0, 0, size, size));
 	}
 	
 	@Override
 	public Dimension getCellOffset() {
 		return CELL_OFFSET;
 	}
 	
 	@Override
 	public double getCellHeight() {
 		return getSize();
 	}
 
 	@Override
 	public double getCellWidth() {
 		return getSize();
 	}
 
 	@Override
 	public int[] getFacingAngles() {
 		return FACING_ANGLES;
 	}
 	
 	@Override
 	public CellPoint convert(ZonePoint zp) {
     	
     	double calcX = (zp.x-getOffsetX()) / (float)getSize();
     	double calcY = (zp.y-getOffsetY()) / (float)getSize();
 
     	boolean exactCalcX = (zp.x-getOffsetX()) % getSize() == 0;
     	boolean exactCalcY = (zp.y-getOffsetY()) % getSize() == 0;
     	
     	int newX = (int)(zp.x < 0 && !exactCalcX ? calcX-1 : calcX);
     	int newY = (int)(zp.y < 0 && !exactCalcY ? calcY-1 : calcY);
     	
     	//System.out.format("%d / %d => %f, %f => %d, %d\n", zp.x, getSize(), calcX, calcY, newX, newY);
         return new CellPoint(newX, newY);
 	}
 
 	@Override
 	public ZoneWalker createZoneWalker() {
 		return new AStarSquareEuclideanWalker(getZone());
 	}
 	
 	@Override
 	public ZonePoint convert(CellPoint cp) {
 
         return new ZonePoint((int)(cp.x * getSize() + getOffsetX()), 
         		(int)(cp.y * getSize() + getOffsetY()));
 	}
 
 	@Override
 	public GridCapabilities getCapabilities() {
 		return CAPABILITIES;
 	}
 
 	@Override
 	public void draw(ZoneRenderer renderer, Graphics2D g, Rectangle bounds) {
 
 		double scale = renderer.getScale();
         double gridSize = getSize() * scale;
 
         g.setColor(new Color(getZone().getGridColor()));
         
         int offX = (int)(renderer.getViewOffsetX() % gridSize + getOffsetX()*scale);
         int offY = (int)(renderer.getViewOffsetY() % gridSize + getOffsetY()*scale);
 
         int startCol = (int)((int)(bounds.x / gridSize) * gridSize);
         int startRow = (int)((int)(bounds.y / gridSize) * gridSize);
         
         for (double row = startRow; row < bounds.y + bounds.height + gridSize; row += gridSize) {
             
             if (AppState.getGridSize() == 1) {
                 g.drawLine(bounds.x, (int)(row + offY), bounds.x+bounds.width, (int)(row + offY));
             } else {
             	g.fillRect(bounds.x, (int)(row + offY - (AppState.getGridSize()/2)), bounds.width, AppState.getGridSize());
             }
         }
 
         for (double col = startCol; col < bounds.x + bounds.width + gridSize; col += gridSize) {
             
             if (AppState.getGridSize() == 1) {
                 g.drawLine((int)(col + offX), bounds.y, (int)(col + offX), bounds.y + bounds.height);
             } else {
             	g.fillRect((int)(col + offX - (AppState.getGridSize()/2)), bounds.y, AppState.getGridSize(), bounds.height);
             }
         }
 	}
 	
 	public ZonePoint getCenterPoint(CellPoint cellPoint) {
 		ZonePoint zp = convert(cellPoint);
 		zp.x += getCellWidth()/2;
 		zp.y += getCellHeight()/2;
 		return zp;
 	}
 
 }
