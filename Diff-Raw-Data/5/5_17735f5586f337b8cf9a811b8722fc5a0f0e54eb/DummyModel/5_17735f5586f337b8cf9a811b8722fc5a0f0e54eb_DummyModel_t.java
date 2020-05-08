 import java.awt.geom.Ellipse2D;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 
 public class DummyModel implements IBouncingBallsModel {
 
 	 private static final double SHELL_THICKNESS = 0.002; // 0.002 m = 2 mm
 	 private static final double DENSITY_OF_RUBBER = 730; // kg/m^3
 	private static final double GRAVITATION = 9.82; // m/s^2
 
 	private final double areaWidth;
 	private final double areaHeight;
 
 	private List<Ball> ballList;
 
 	protected class Ball {
 		protected double x, y, vx, vy, r;
 
 		public Ball(double x, double y, double r) {
 			this.x = x;
 			this.y = y;
 			this.r = r;
 			vx = 1;
 			vy = 1;
 
 			// Random rand = new Random();
 			// double xs, ys;
 			//
 			// wh: while (true) {
 			// xs = rand.nextDouble() % areaWidth;
 			// ys = rand.nextDouble() % areaHeight;
 			//
 			// for (Ball b : list) {
 			// if ((b.x-b.r) >= x && x < (b.x+b.r)) {
 			// break wh;
 			// }
 			// }
 			//
 			// this.x = xs;
 			// this.y = ys;
 			// }
 			// For every ball in the list
 			// Check that the coordinates doesn't spawn on top of another ball
 			// If there is a ball there, start over with new coordinates
 			// Else place the ball there
 		}
 
 		public void tick(double deltaT) {
 			if (x < r || x > areaWidth - r) {
 				vx *= -1;
 			}
 			if (y < r || y > areaHeight - r) { // ifall den studsar i taket
 				vy *= -1;
 			}
			else {
 			vy -= GRAVITATION * deltaT;
			}
 			x += vx * deltaT;
 			y += vy * deltaT;
 			/*
 			 * Om vi representerar en boll som ett klot så kommer det inte att
 			 * stämma överens med verkligheten riktigt. Utan det vi vill göra är
 			 * snarare att ta mantelarean av klotet med radien r (4*pi*r*r) med
 			 * skalets tjocklek som vi antar är 0.002 m. Därför blir volymen av
 			 * mantelarean*0.002 volymen av själva plasten som vi multiplicerar
 			 * med densiteten 730 kg/m^3 vilket ger massan
 			 */
 			// double m = 4 * Math.PI * r * r * SHELL_THICKNESS *
 			// DENSITY_OF_RUBBER;
 
 			// f = m * y''
 		}
 
 	}
 
 	public DummyModel(double width, double height) {
 		this(width, height, 2);
 	}
 
 	public DummyModel(double width, double height, int ballAmount) {
 		this.areaWidth = width;
 		this.areaHeight = height;
 
 		ballList = new ArrayList<Ball>();
 		Random rand = new Random();
 		for (int i = 0; i < ballAmount; i++) {
 			double tempR = 0.5 + rand.nextDouble() % 1.5;
 			ballList.add(new Ball(tempR + (i * tempR), areaHeight - tempR
 					- (areaHeight / 2) * rand.nextDouble(), tempR));
 		}
 		System.out.println("" + areaHeight + " " + areaWidth);
 	}
 
 	@Override
 	public void tick(double deltaT) {
 		for (Ball b : ballList) {
 			b.tick(deltaT);
 		}
 		
 		List<Ball>[] list = getCollisions(ballList);
 		
 		for (int i = 0; i < ballList.size(); i++) {
 			if (list[i] != null) {
 				for (Ball b: list[i]) {
 					handleCollision(ballList.get(i), b);
 				}
 			}
 		}
 
 		// if (x < r || x > areaWidth - r) {
 		// vx *= -1;
 		// }
 		// if (y < r) { // ifall den studsar i taket
 		// vy *= -1;
 		// }
 		// if (y > areaHeight - r) { // ifall den studsar i golvet
 		// vy *= -1;
 		// }
 		//
 		// vy -= GRAVITATION * deltaT;
 		//
 		// x += vx * deltaT;
 		// y += vy * deltaT;
 
 		/*
 		 * Om vi representerar en boll som ett klot så kommer det inte att
 		 * stämma överens med verkligheten riktigt. Utan det vi vill göra är
 		 * snarare att ta mantelarean av klotet med radien r (4*pi*r*r) med
 		 * skalets tjocklek som vi antar är 0.002 m. Därför blir volymen av
 		 * mantelarean*0.002 volymen av själva plasten som vi multiplicerar med
 		 * densiteten 730 kg/m^3 vilket ger massan
 		 */
 		// double m = 4 * Math.PI * r * r * SHELL_THICKNESS * DENSITY_OF_RUBBER;
 
 		// f = m * y''
 	}
 
 	private void handleCollision(Ball a, Ball b) {
 		/*
 		 * Om vi representerar en boll som ett klot så kommer det inte att
 		 * stämma överens med verkligheten riktigt. Utan det vi vill göra är
 		 * snarare att ta mantelarean av klotet med radien r (4*pi*r*r) med
 		 * skalets tjocklek som vi antar är 0.002 m. Därför blir volymen av
 		 * mantelarean*0.002 volymen av själva plasten som vi multiplicerar med
 		 * densiteten 730 kg/m^3 vilket ger massan
 		 */
 		double m1 = 4 * Math.PI * a.r * a.r * SHELL_THICKNESS * DENSITY_OF_RUBBER;
 		double m2 = 4 * Math.PI * b.r * b.r * SHELL_THICKNESS * DENSITY_OF_RUBBER;
 		
 		double v1x = a.vx, v2x = b.vx, v1y = a.vy, v2y = b.vy;
 		
 		a.vx = ((m1*v1x + m2 * v2x) - m2 * -(v2x-v1x))/(m1+m2);
 		a.vy = ((m1*v1y + m2 * v2y) - m2 * -(v2y-v1y))/(m1+m2);
 		b.vx = ((m2*v2x + m1 * v1x) - m1 * -(v1x-v2x))/(m2+m1);
 		b.vy = ((m2*v2y + m1 * v1y) - m1 * -(v1y-v2y))/(m2+m1);
 		
 		double xx = a.x - b.x;
 		double yy = a.y - b.y;		
 	}
 
 	@Override
 	public List<Ellipse2D> getBalls() {
 		List<Ellipse2D> myBalls = new LinkedList<Ellipse2D>();
 		for (Ball b : ballList) {
 			myBalls.add(new Ellipse2D.Double(b.x - b.r, b.y - b.r, 2 * b.r,
 					2 * b.r));
 		}
 		return myBalls;
 
 		// List<Ellipse2D> myBalls = new LinkedList<Ellipse2D>();
 		// myBalls.add(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
 		// return myBalls;
 	}
 
 	protected List<Ball>[] getCollisions(List<Ball> balls) {
 		@SuppressWarnings("unchecked")
 		List<Ball>[] collisionsArray = new LinkedList[balls.size()];
 
 		/*
 		 * För att kolla om två cirklar är inom räckhåll av varandras areor så
 		 * räknar vi avståndet mellan deras koordinater i var sitt led. Sen
 		 * använder vi pythagoras för att räkna ut avståndet där emellan.
 		 * 
 		 * Om avståndet är större än summan av deras radier så nuddas de inte
 		 * Om det är lika med summan av deras radier så nuddar de varandra. Om det är mindre än
 		 * summan av deras radier så genomskär de varandra
 		 * 
 		 * Vi måste jämföra varje cirkel med alla andra cirklar (utan en annan
 		 * datastruktur och skriva en comparator som gör att man kan använda ett
 		 * träd eller liknande.
 		 */
 		
 		double ix, iy, ir;
 		for (int i = 0; i < balls.size(); i++) {
 			Ball b = balls.get(i);
 			ix = b.x; iy = b.y; ir = b.r;
 			
 			double squaredist;
 			for (Ball b2 : balls) {
 				if (b == b2) {
 					continue;
 				} else {
 					squaredist 	= (ix - b2.x) * (ix - b2.x) 
 								+ (iy - b2.y) * (iy - b2.y);
 					if (squaredist <= (ir + b2.r)*(ir + b2.r)) {
 						addToList(collisionsArray, i, b2);
 					}
 				}
 			}
 		}
 		return collisionsArray;
 	}
 	
 	/*
 	 * Add ball b to the list at position i on the list[]
 	 */
 	private void addToList(List<Ball>[] array, int i, Ball b) {
 		
 		if (array[i] == null) {
 			LinkedList<Ball> t = new LinkedList<Ball>();
 			t.add(b);
 			array[i] = t;
 		} else {
 			array[i].add(b);
 		}
 	}
 }
