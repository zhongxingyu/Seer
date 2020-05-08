 package demos;
 
 import demos.lunar.*;
 import vitro.*;
 import vitro.plane.*;
 import vitro.util.*;
 import java.awt.*;
 import java.awt.geom.*;
 import java.util.*;
 
 
 public class LunarView implements View {
 
 	private final ColorScheme colors = new ColorScheme(Color.WHITE, new Color(75, 75, 75), Color.BLACK);
 
 	private final Lunar      model;
 	private final Controller controller;
 	private final int        width;
 	private final int        height;
 
	private final ObserverView   observerView;
 	private final LandingPadView landingPadView;
 	private final HUDView        hudView;
 
 	private final Set<LanderView> landerViews;
 
 	private AffineTransform screen;
 	private AffineTransform world;
 
 	private Polygon blast = null;
 
 	public LunarView(Lunar model, Controller controller, int width, int height) {
 		this.model      = model;
 		this.controller = controller;
 		this.width      = width;
 		this.height     = height;
 
 		observerView   = new ObserverView();
 
 
 		landingPadView = new LandingPadView(model.landingPad);
 		hudView        = new HUDView((Lander)Groups.firstOfType(Lander.class, model.actors), model.landingPad);
 
 		landerViews = new HashSet<LanderView>();
 		for(Actor actor : Groups.ofType(Lander.class, model.actors)) {
 			landerViews.add(new LanderView((Lander)actor));
 		}
 	}
 
 	public Controller  controller()  { return controller; }
 	public ColorScheme colorScheme() { return colors;     }
 	public int         width()       { return width;      }
 	public int         height()      { return height;     }
 
 	private Polygon blast(int sx, int sy) {
 		int[] x = new int[20];
 		int[] y = new int[20];
 		for(int i = 0; i < x.length; i++) {
 			double r = Math.PI * 2 * ((double)i / x.length);
 			x[i] = sx + (int)(Math.cos(r) * ((Math.random() * 20) + 25));
 			y[i] = sy + (int)(Math.sin(r) * ((Math.random() * 20) + 25));
 		}
 		return new Polygon(x, y, 20);
 	}
 
 	public void draw(Graphics g) {
 		Drawing.configureVector(g);
 
 		g.setColor(colors.background);
 		g.fillRect(0, 0, width, height);
 
 		synchronized(model) {
 			observerView.draw((Graphics2D)g);
 
 			AffineTransform oldTransform = ((Graphics2D)g).getTransform();
 			((Graphics2D)g).scale( 1, -1);
 			((Graphics2D)g).translate(width / 2, 20 - height);
 
 			landingPadView.draw((Graphics2D)g);
 			for(LanderView landerView : landerViews) {
 				landerView.draw((Graphics2D)g);
 			}
 
 			((Graphics2D)g).setTransform(oldTransform);
 			hudView.draw((Graphics2D)g);
 		}
 	}
 
 	private double sofar = 0;
 	public void tick(double time) {
 		sofar += time;
 		if(sofar > .50) {
 			controller.next();
 			sofar = 0;
 		}
 	}
 
 	public void flush() {
 
 	}
 
 
 
 	/**
 	*
 	**/
 	public class LanderView {
 		public final Lander lander;
 		
 		public LanderView(Lander lander) {
 			this.lander = lander;
 		}
 
 		protected void draw(Graphics2D g) {
 			int x = (int)model.positions.get(lander).x;
 			int y = (int)model.positions.get(lander).y;
 			boolean success = lander.state == Lander.State.LANDED;
 			boolean dead    = lander.state == Lander.State.CRASHED;
 
 			AffineTransform oldTransform = g.getTransform();
 			g.rotate(Math.PI, x, y);
 
 			if(lander.state == Lander.State.CRASHED) {
				g.setColor(Color.WHITE);
 				g.fillPolygon(blast(x, y));
 			}
 
 			// draw engines
 			g.setColor(colors.outline.brighter().brighter());
 			if(lander.mThruster.activated) {
 				int  h = (int)(Math.random() * 10) + 20;
 				int dx = (int)(Math.random() *  2)  - 1;
 				g.drawLine(x - 6, y + 10, x + dx, y + 10 + h);
 				g.drawLine(x + 6, y + 10, x + dx, y + 10 + h);
 			}
 			if(lander.rThruster.activated) {
 				int  w = (int)(Math.random() * 7) + 10;
 				int dy = (int)(Math.random() * 2) - 1;
 				g.drawLine(x + 16, y - 15, x + 16 + w, y - 13 + dy);
 				g.drawLine(x + 16, y - 11, x + 16 + w, y - 13 + dy);
 			}
 			if(lander.lThruster.activated) {
 				int  w = (int)(Math.random() * 7) + 10;
 				int dy = (int)(Math.random() * 2) - 1;
 				g.drawLine(x - 16, y - 15, x - 16 - w, y - 13 + dy);
 				g.drawLine(x - 16, y - 11, x - 16 - w, y - 13 + dy);
 			}
 			//}
 
 			// victory flag
 			if (success) {
 				Rectangle flag = new Rectangle(x, y - 40, 10, 8);
 				g.setColor(colors.background);
 				g.fill(flag);
 				g.setColor(colors.outline);
 				g.draw(flag);
 				g.drawLine(x, y - 40, x, y);
 			}
 	
 			// base
 			fill(g, new Rectangle(x - 15, y - 3, 30, 6));
 
 			// left leg
 			fill(g, new Polygon(
 				new int[] { x - 13, x - 9, x - 17 },
 				new int[] { y +  3, y + 3, y + 13 },
 				3
 			));
 			g.drawLine(x - 19, y + 13, x - 15, y + 13);
 
 			// right leg
 			fill(g, new Polygon(
 				new int[] { x + 13, x + 9, x + 17 },
 				new int[] { y +  3, y + 3, y + 13 },
 				3
 			));
 			g.drawLine(x + 19, y + 13, x + 15, y + 13);
 
 			// crew module
 			fill(g, new Polygon(
 				new int[] { x - 7, x - 13, x - 13, x -  7, x +  7, x + 13, x + 13, x + 7 },
 				new int[] { y - 3, y - 10, y - 17, y - 24, y - 24, y - 17, y - 10, y - 3 },
 				8
 			));
 
 			// main thruster
 			fill(g, new Polygon(
 				new int[] { x - 5, x -  8, x +  8, x + 5 },
 				new int[] { y + 3, y + 10, y + 10, y + 3 },
 				4
 			));
 
 			// right thruster
 			fill(g, new Polygon(
 				new int[] { x + 13, x + 16, x + 16, x + 13 },
 				new int[] { y - 14, y - 17, y -  9, y - 12 },
 				4
 			));
 
 			// left thruster
 			fill(g, new Polygon(
 				new int[] { x - 13, x - 16, x - 16, x - 13 },
 				new int[] { y - 14, y - 17, y -  9, y - 12 },
 				4
 			));
 
 			g.setTransform(oldTransform);
 		}
 
 		void fill(Graphics2D g, Shape shape) {
 			g.setColor(colors.background);
 			g.fill(shape);
 			g.setColor(new Color(100, 100, 100));
 			g.draw(shape);
 		}
 	}
 
 	/**
 	*
 	**/
 	/*
 	public class BlastView {
 		private Polygon blast(int sx, int sy) {
 		int[] x = new int[20];
 		int[] y = new int[20];
 		for(int i = 0; i < x.length; i++) {
 			double r = Math.PI * 2 * ((double)i / x.length);
 			x[i] = sx + (int)(Math.cos(r) * ((Math.random() * 20) + 25));
 			y[i] = sy + (int)(Math.sin(r) * ((Math.random() * 20) + 25));
 		}
 		return new Polygon(x, y, 20);
 	}
 	*/
 
 	/**
 	*
 	**/
 	public class LandingPadView {
 		public final LandingPad landingPad;
 
 		public LandingPadView(LandingPad landingPad) {
 			this.landingPad = landingPad;
 		}
 
 		public void draw(Graphics2D g) {
 			AlignedBox bound = (AlignedBox)landingPad.bound();
 			g.setColor(colors.background);
 			g.fillRect(
 				(int)(bound.point0.x),
 				(int)(bound.point0.y),
 				(int)(bound.point1.x - bound.point0.x),
 				(int)(bound.point1.y - bound.point0.y)
 			);
 			g.setColor(colors.outline);
 			g.drawRect(
 				(int)(bound.point0.x),
 				(int)(bound.point0.y),
 				(int)(bound.point1.x - bound.point0.x),
 				(int)(bound.point1.y - bound.point0.y)
 			);
 		}
 	}
 
 	/**
 	*
 	**/
 	public class ObserverView {
 		private final Set<Point> stars;
 		private final Polygon    mountains;
 		private final Polygon    ground;
 
 		public ObserverView() {
 			stars     = generateStars(400);
 			mountains = generateMountains(height / 2, 20);
 			ground    = generateGround(height - 20);
 		}
 
 		public void draw(Graphics2D g) {
 			drawStars(g);
 			drawMountains(g);
 			drawGround(g);
 		}
 
 
 		private void drawStars(Graphics2D g) {
 			g.setColor(colors.outline);
 			for(Point star : stars) {
 				g.drawLine(star.x, star.y, star.x, star.y);
 			}
 		}
 
 		public void drawMountains(Graphics2D g) {
 			g.setColor(colors.background);
 			g.fillPolygon(mountains);
 			g.setColor(colors.outline.darker().darker());
 			g.drawPolygon(mountains);
 		}
 
 		public void drawGround(Graphics2D g) {
 			g.setColor(colors.background);
 			g.fillPolygon(ground);
 			g.setColor(colors.outline);
 			g.drawPolygon(ground);
 		}
 
 		private Set<Point> generateStars(int density) {
 			Set<Point> stars = new HashSet<Point>();
 			for(int x = 0; x < density; x++) {
 				stars.add(new Point((int)(Math.random() * width), (int)(Math.random() * height)));
 			}
 			return stars;
 		}
 
 		private Polygon generateMountains(int startHeight, int diffHeight) {
 			int[] x = new int[50];
 			int[] y = new int[50];
 
 			int xp = -10;
 			int yp = startHeight;
 
 			for(int i = 1; i < 49; i++) {
 				x[i] = xp;
 				y[i] = yp;
 				xp += (int)(Math.random() * 10) + (width / 40);
 				yp += (int)(Math.random() * (2 * diffHeight)) - diffHeight;
 			}
 
 			x[ 0] = -20;       y[ 0] = 2 * height;
 			x[49] = 2 * width; y[49] = 2 * height;
 
 			return new Polygon(x, y, 50);
 		}
 
 		private Polygon generateGround(int startHeight) {
 			int[] x = new int[50];
 			int[] y = new int[50];
 
 			int xp = -10;
 			for(int i = 1; i < 49; i++) {
 				x[i] = xp;
 				y[i] = startHeight + (int)(Math.random() *  6) - 3;
 
 				xp += (int)(Math.random() * 10) + (width / 40);
 			}
 
 			x[ 0] = -20;       y[ 0] = 2 * height;
 			x[49] = 2 * width; y[49] = 2 * height;
 
 			return new Polygon(x, y, 50);
 		}
 	}
 
 
 	/**
 	*
 	**/
 	public class HUDView {
 		private final VectorFont font = new VectorFont(6, 9);
 
 		public final Lander     lander;
 		public final LandingPad landingPad;
 
 		private final int bufX = 10;
 		private final int bufY = 10;
 
 		public HUDView(Lander lander, LandingPad landingPad) {
 			this.lander     = lander;
 			this.landingPad = landingPad;
 		}
 
 		public void draw(Graphics2D g) {
 			// draw main details
 			drawMarkerLander(g);
 			drawMarkerLandingPad(g);
 
 			drawLanderDetails(g);
 			drawGlobalDetails(g);
 
 			// draw exception notifications
 			if     (lander                      == null) { drawNotification(g, "DISCONNECTED");          }
 			else if(model.positions.get(lander) == null) { drawNotification(g, "OBTAINING POSITION..."); }
 		}
 
 		private final void drawLanderDetails(Graphics2D g) {
 			String xVelString = String.format("VEL [X] :  %s", "----.--");
 			String yVelString = String.format("VEL [Y] :  %s", "----.--");
 			String fuelString = String.format("FUEL    :  %s", "----"   );
 
 			if(lander != null && model.positions.get(lander) != null) {
 				xVelString = String.format("VEL [X] :  % 07.2f", lander.velocity.x);
 				yVelString = String.format("VEL [Y] :  % 07.2f", lander.velocity.y);
 				fuelString = String.format("FUEL    : % 4d"   , lander.fuel      );
 			}
 
 			g.setColor(colors.outline);
 			font.draw(g, bufX, bufY + 0 * (font.characterHeight + 3), xVelString);
 			font.draw(g, bufX, bufY + 1 * (font.characterHeight + 3), yVelString);
 			font.draw(g, bufX, bufY + 2 * (font.characterHeight + 3), fuelString);
 		}
 
 		private final void drawGlobalDetails(Graphics2D g) {
 			String altString = String.format("ALTITUDE :  %s", "----.--");
 			String disString = String.format("DISTANCE :  %s", "----.--");
 
 			if(lander     != null && model.positions.get(lander)     != null &&
 			   landingPad != null && model.positions.get(landingPad) != null) {
 				double altitude =   model.positions.get(lander).y - model.positions.get(landingPad).y - 15;
 				double distance = - model.positions.get(lander).x - model.positions.get(landingPad).x;
 
 				altString = String.format("ALTITUDE : % 07.2f", altitude);
 				disString = String.format("DISTANCE : % 07.2f", distance);
 			}
 
 			g.setColor(colors.outline);
 			font.draw(g, bufX + (width / 2), bufY + 0 * (font.characterHeight + 3), altString);
 			font.draw(g, bufX + (width / 2), bufY + 1 * (font.characterHeight + 3), disString);
 		}
 
 		private final void drawNotification(Graphics2D g, String string) {
 			int length = font.length(string);
 
 			g.setColor(colors.background);
 			g.fillRect(width / 2 - length / 2 - 15, height / 2 - font.characterHeight / 2 - 10, length + 30, font.characterHeight + 20);
 			g.setColor(colors.outline);
 			g.drawRect(width / 2 - length / 2 - 15, height / 2 - font.characterHeight / 2 - 10, length + 30, font.characterHeight + 20);
 
 			font.draw(g, width / 2 - length / 2, height / 2 - font.characterHeight / 2, string);
 		}
 
 		private final void drawMarkerLander(Graphics2D g) {
 			if(lander != null && model.positions.get(lander) != null) {
 				Position p = model.positions.get(lander);
 				int[][] polygon = new int[][] { 
 					{ (int)(p.x)     , (int)(p.x) -  5, (int)(p.x) +  5 }, 
 					{ (int)(p.y) + 30, (int)(p.y) + 35, (int)(p.y) + 35 }
 				};
 
 				AffineTransform oldTransform = g.getTransform();
 				g.scale( 1, -1);
 				g.translate(width / 2, 20 - height);
 
 				g.setColor(colors.outline);
 				g.fillPolygon(polygon[0], polygon[1], 3);
 
 				g.setTransform(oldTransform);
 			}
 		}
 
 		private final void drawMarkerLandingPad(Graphics2D g) {
 			if(landingPad != null && model.positions.get(landingPad) != null) {
 				Position p = model.positions.get(landingPad);
 				int[][] polygon = new int[][] { { (int)(p.x), (int)(p.x) -  5, (int)(p.x) +  5 }, { -10, -15, -15 } };
 
 				AffineTransform oldTransform = g.getTransform();
 				g.scale( 1, -1);
 				g.translate(width / 2, 20 - height);
 
 				g.setColor(colors.outline);
 				g.fillPolygon(polygon[0], polygon[1], 3);
 
 				g.setTransform(oldTransform);
 			}
 		}
 	}
 }
