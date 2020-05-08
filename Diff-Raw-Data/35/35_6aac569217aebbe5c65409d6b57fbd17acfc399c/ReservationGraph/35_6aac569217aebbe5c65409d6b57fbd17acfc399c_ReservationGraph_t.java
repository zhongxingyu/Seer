 package vehicleShepard;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 
 public class ReservationGraph extends JPanel {
 
 	//Start position of graph view
 	private static final int GRAPH_X_POS = 100;
 	private static final int GRAPH_Y_POS = 15;
 
 	//Single "graph bar" properties
 	private static final int BAR_HEIGHT = 11;
 	private static final int X_PADDING = 20;
 	private static final int Y_PADDING = 5;
 	private static final int SPACING = 2;
 	private static final int UNIT = 20;
 
 	//Number constants
 	private final static int AREA_RESERVATION_ID = 0;
 	private final static int AREA_MONTH = 1;
 	private final static int AREA_YEAR = 2;
 	private final static int AREA_X = 3;
 	private final static int AREA_Y = 4;
 	private final static int AREA_WIDTH = 5;
 	private final static int AREA_HEIGHT = 6;
 
 	//Reservation info
 	private ArrayList<ArrayList<Reservation>> allRes;
 	private ArrayList<int[]> resOverlay = new ArrayList<int[]>();
 
 	//The selected view
 	private String selectedStartDate;
 	private int selectedYear;
 	private int selectedMonth;
 	private String selectedEndDate;
 	private int selectedMaximumDayInMonth;
 
 	public ReservationGraph(ArrayList<ArrayList<Reservation>> res, String sDate, String eDate) {
 		this.allRes = res;
 		selectedStartDate = sDate;
 		selectedEndDate = eDate;
 		
 		/* Using MouseAdapter, so we only need to override
 		 * the necessary methods (in contrast to MouseListener,
 		 *  where all actions must be declared) */
 		addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				int x = e.getX(), y = e.getY();
 				System.out.println("Clicked @ "+x+"."+y); //TODO Just for debug
 				int resID = getResIdByCoordinate(x, y);
 				if(resID > 0) {
 					//ReservationView rp = new ReservationView();
 					//rp.showExistingWindow(resID);
 					System.out.println("Clicked at reservation " + resID);
 				}
 			}
 		});
 	}
 	
 	public void setTableModel(final StandardTableModel stm) {
 		
 	}
 
 	public void paint(Graphics gSimple) {
 		super.paintComponent(gSimple);
 		Graphics2D g = (Graphics2D)gSimple;
 
 		//Getting calendar instance
 		GregorianCalendar cCal = (GregorianCalendar) GregorianCalendar.getInstance();
 		int thisDay = cCal.get(Calendar.DAY_OF_MONTH);
 		int thisMonth = cCal.get(Calendar.MONTH)+1;
 		int thisYear = cCal.get(Calendar.YEAR);
 
 		//Font
 		String fontName = UIManager.getDefaults().getFont("Label.font").toString();
 		int fontSize = 11;
 		int fontHeight = 8;
 
 		//Draw day labels
 		if(thisMonth >= selectedMonth && thisYear >= selectedYear)
 			g.setColor(Color.LIGHT_GRAY);
 		int labelXPos = GRAPH_X_POS + X_PADDING;
 		int pad = 0;
 
 		for(int i = 1; i <= selectedMaximumDayInMonth; i++) {
 
 			g.setFont(new Font(fontName, Font.PLAIN, fontSize));
 			if(i < 10) pad = 2; else pad = 5; //TODO Use fontmetrics instead
 			g.drawString(""+i, labelXPos-pad+(UNIT/2), 10); //11x8
 
 			if(i == thisDay && thisMonth == selectedMonth && thisYear == selectedYear) {
 				g.setColor(Color.BLACK);
 				g.setFont(new Font(fontName, Font.BOLD, fontSize));
 				g.drawString(""+i, labelXPos-pad+(UNIT/2), 10);
 				g.setColor(Color.DARK_GRAY);
 			}
 			labelXPos += UNIT;
 		}
 
 		//Draw reservations of a single vehicle
 		int resXPos;							//Reservation x position
 		int resYPos = GRAPH_Y_POS + Y_PADDING;	//Reservation y position
 		int[] area;								//Area of a single graph element
 
 
 		for(ArrayList<Reservation> vehicleRes : allRes) {
 
 			resXPos = GRAPH_X_POS + X_PADDING;
 
 			for(Reservation r : vehicleRes) {
 
				int width;
 				resXPos = (r.getFromDateDay()-1)*UNIT;
 
 				int fromDelta = deltaDays(r.getFromDate(), selectedStartDate);
 				int toDelta = deltaDays(selectedEndDate, r.getToDate());
 				int month = r.getFromDateMonth();
 				int year = r.getFromDateYear();
 
				month = selectedMonth;
				year = selectedYear;
				
				if(toDelta > 0 && fromDelta > 0) {
					resXPos = 0;
					width = (r.getLength() - fromDelta - toDelta) * UNIT - SPACING;
				}
				else if(fromDelta > 0) {
 					resXPos = 0;
 					width = (r.getLength()-fromDelta)*UNIT - SPACING;
					
 				}
 				else if(toDelta > 0) {
 					width = (r.getLength()-toDelta)*UNIT - SPACING;
				}
				else {
					width = r.getLength() * UNIT - SPACING;
 				}
 
 				area = new int[7];
 				area[AREA_RESERVATION_ID] = r.getResID();					//Reservation ID
 				area[AREA_YEAR] = year;
 				area[AREA_MONTH] = month;
 
 				area[AREA_X] = GRAPH_X_POS + X_PADDING + resXPos + SPACING;	//X-pos
 				area[AREA_Y] = resYPos;										//Y-pos
 				area[AREA_WIDTH] = width;									//Width
 				area[AREA_HEIGHT] = BAR_HEIGHT;								//Height
 
 				resOverlay.add(area);
 
 				Rectangle2D.Double shadow = new Rectangle2D.Double(area[AREA_X]+1, area[AREA_Y]+1, area[AREA_WIDTH], area[AREA_HEIGHT]);
 				g.setColor(Color.LIGHT_GRAY);
 				g.fill(shadow);
 
 				Rectangle2D.Double rect = new Rectangle2D.Double(area[AREA_X], area[AREA_Y], area[AREA_WIDTH], area[AREA_HEIGHT]);
 				g.setColor(new Color(74, 111, 167)); // Color.BLUE
 				g.fill(rect);
 
 				//Draw arrows (if the reservation extends beyond the view)
 				//g.draw(getRightTriangle(area[AREA_X] + area[AREA_WIDTH], area[AREA_Y]));
 				//g.draw(getLeftTriangle(area[AREA_X], area[AREA_Y]));
 				//TODO UNCONTROLLABLE :O
 				//Vehicle label
 				g.setColor(new Color(26, 56, 96));
 				g.drawString("(" + r.getVehicleID() + ") " + r.getTypeName(), 2, resYPos+(fontHeight/2)+(BAR_HEIGHT/2));
 			}
 
 			resYPos+= BAR_HEIGHT + Y_PADDING;
 		}
 		
 		setPanelHeight(resYPos);
 		//Draw horizontal line
 		g.setColor(Color.DARK_GRAY);
 		g.drawLine(0, 14, GRAPH_X_POS+(selectedMaximumDayInMonth*UNIT)+X_PADDING*2, 14); //Top horizontal
 		//g.drawLine(GRAPH_X_POS, 0, GRAPH_X_POS, resYPos); // Left vertical
 		//g.drawLine(GRAPH_X_POS+(daysInSelMonth*UNIT)+X_PADDING*2, 0, GRAPH_X_POS+(daysInSelMonth*UNIT)+X_PADDING*2, resYPos); // Right vertical
 		//g.drawLine(0, resYPos, graphXPos+(daysInMonth*UNIT)+X_PADDING*2, resYPos); //Bottom horizontal		
 	}
 	
 	private void setPanelHeight(int height) {
 		setPreferredSize(new Dimension(getPreferredSize().width, height));
 	}
 
 	private Polygon getRightTriangle(int xPos, int yPos) {
 		Point p1 = new Point(xPos, yPos);
 		Point p2 = new Point(xPos + BAR_HEIGHT, yPos + BAR_HEIGHT/2);
 		Point p3 = new Point(xPos, yPos + BAR_HEIGHT - 1);
 
 		int[] pX = {p1.x, p3.x, p2.x};
 		int[] pY = {p1.y, p3.y, p2.y};
 
 		return new Polygon(pX, pY, 3);
 	}
 
 	private Polygon getLeftTriangle(int xPos, int yPos) {
 		Point p1 = new Point(xPos, yPos);
 		Point p2 = new Point(xPos-BAR_HEIGHT, yPos + BAR_HEIGHT/2);
 		Point p3 = new Point(xPos, yPos + BAR_HEIGHT - 1);
 
 		int[] pX = {p1.x, p3.x, p2.x};
 		int[] pY = {p1.y, p3.y, p2.y};
 
 		return new Polygon(pX, pY, 3);
 	}
 
 	private int getResIdByCoordinate(int x, int y) {
 		for(int[] o : resOverlay) {
 			if(x >= o[AREA_X] && x <= o[AREA_X] + o[AREA_WIDTH]
 					&& y >= o[AREA_Y] && y <= o[AREA_Y] + o[AREA_HEIGHT]
 							&& o[AREA_YEAR] == selectedYear
 							&& o[AREA_MONTH] == selectedMonth) {
 				return o[AREA_RESERVATION_ID];
 			}
 		}
 		return -1;
 	}
 
 	private int deltaDays(String fromDate, String toDate) {
 		String[] fDateSep = fromDate.split("-");
 		String[] eDateSep = toDate.split("-");
 
 		GregorianCalendar fromCalendar = new GregorianCalendar(Integer.parseInt(fDateSep[0]), Integer.parseInt(fDateSep[1]), Integer.parseInt(fDateSep[2]));
 		GregorianCalendar extCalendar = new GregorianCalendar(Integer.parseInt(eDateSep[0]), Integer.parseInt(eDateSep[1]), Integer.parseInt(eDateSep[2]));
 
 		fromCalendar.getTimeInMillis();
 
 		long millSecDiff = extCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis();
 		int delta = (int) (millSecDiff / (24 * 60 * 60 * 1000));
 		return delta;
 	}
 
 	public void setView(String startDate, String endDate) {
 		selectedStartDate = startDate;
 		selectedYear = Integer.parseInt(selectedStartDate.split("-")[0]);
 		selectedMonth = Integer.parseInt(selectedStartDate.split("-")[1]);
 		selectedEndDate = endDate;
 		selectedMaximumDayInMonth = Integer.parseInt(selectedEndDate.split("-")[2]);
 
 		repaint();
 	}
 	
 	public void setNewData(ArrayList<ArrayList<Reservation>> newData) {
 		allRes = newData;
 		setView(selectedStartDate, selectedEndDate);
 	}
 }
