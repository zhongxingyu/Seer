 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 public class Game {
 
 	private static final Game instance = new Game();
 	private List<Ball> balls;
 	private List<Joint> joints;
 	private float fieldSize;
 	private double jointLength = 40;
 	private double jointStrength = 0.1;
 	private Vektor pointer;
 	
 	public Ball active;
 	public int width = 600;
 	public int height = 400;
 	
 	private float ballsize = 15;
 	private int refreshInterval = 50;
 
 	public Vektor getCenter() {
 		return new Vektor(width / 2, height / 2);
 	}
 
 	public void setPointer(Vektor pointer) {
 		this.pointer = pointer;
 	}
 	
 	public int getNumberOfJoints() {
 		return joints.size();
 	}
 
 	private Timer timer;
 	private TimerTask task = new TimerTask() {
 		public void run() {
 			refresh();
 		}
 	};
 
 	private Game() {
 		balls = new CopyOnWriteArrayList<Ball>();
 		//balls = Collections.synchronizedList(balls);
 		joints = new CopyOnWriteArrayList<Joint>();
 		//joints = Collections.synchronizedList(joints);
 		setFieldSize(320f);
 		timer = new Timer();
 		timer.schedule(task, 0, refreshInterval);
 		pointer = getCenter();
 	}
 
 	public static Game instance() {
 		return instance;
 	}
 
 	public void refresh() {
 		physik();
 	}
 
 	@SuppressWarnings("unused")
 	private void gravity(Ball b) {
 		b.acceleration.add(new Vektor(b.position, getCenter(), 
 			4 / b.distanceTo(getCenter()).getLength()));
 	}
 	
 	private void indirectGravity(Ball b) {
 		//Vektor v = new Vektor(b.position, getCenter(),
 		//		b.distanceTo(getCenter()).getLength() * 0.0001 + 0.2);
 		Vektor difference = b.position.sub(getCenter());
 		double length = difference.getLength();
 		difference.setLength(length * 0.0001 + 0.2);
 		b.accelerate(difference);
 	}
 	
 	/* Federkraefte zwischen gejointen Baellen */
 	private void swingBalls(Joint j) {
 		Vektor real_diff   = j.a.position.sub(j.b.position);
 		double real_length = real_diff.getLength();
 		double wish_length = j.getLength(); 
 		Vektor wish_diff   = Vektor.polar(real_diff.getAngle(), wish_length);
 		Vektor force       = wish_diff.sub(real_diff);
 		force = force.mul((real_length / wish_length) * j.getStrength());
 		j.a.accelerate(force);
 		j.b.accelerate(force.mul(-1));
 	}
 	
 	/* Kollisions behandlung */
 	private void collide(Collision c) {
 		/* farben vergleichen
 		 * TODO besseres Joinen von Balls*/
 		if (c.a.color.equals(c.b.color))
 			join(c.a, c.b);
 		Vektor diff2 = c.diff.mul(0.05);
 		c.a.accelerate(diff2.mul(-1));
 		c.b.accelerate(diff2);
 	}
 	
 	private void moveBall(Ball b) {
 		b.speed = b.speed.add(b.acceleration);
 		b.position = b.position.add(b.speed);
 	}
 	
	private void friction(Ball b) {
		b.accelerate(b.speed.mul(-0.2));
	}
	
 	private void moveActiveBall() {
 		active.accelerate(active.position.sub(pointer));
 	}
 	
 	public void physik() {
 		List<Collision> collisions = new LinkedList<Collision>();
 		//collisions = Collections.synchronizedList(collisions);
 		int balls_count = balls.size();
 		if (balls_count > 1) {
 			for (int i = 0; i < balls_count-1; i++) {
 				for (int j = i+1; j < balls_count; j++) {
 				
 					Collision collision = Collision.test(balls.get(i), balls.get(j));
 					if (collision != null)
 						collisions.add(collision);
 				}
 			}
 		}
 		if (active != null) moveActiveBall();
 		
 		for (Ball b : balls) {
 			b.acceleration = new Vektor();
 			// gravity(b);
 			indirectGravity(b);
 			//repulseOtherBalls(b);
			friction(b);
 		}
 		for (Collision c : collisions)
 			collide(c);
 		for (Joint j : joints)
 			swingBalls(j);
 		for (Ball b : balls)
 			moveBall(b);
 	}
 
 	public void setFieldSize(float fieldSize) {
 		this.fieldSize = fieldSize;
 	}
 
 	public float getFieldSize() {
 		return fieldSize;
 	}
 
 	public List<Ball> getBalls() {
 		return balls;
 	}
 
 	public List<Joint> getJoints() {
 		return joints;
 	}
 
 	public Ball getBall(int index) {
 		return balls.get(index);
 	}
 
 	private Ball createBall(Vektor v) {
 		Ball ball = new Ball(v, ballsize);
 		ball.color = randomColor();
 		balls.add(ball);
 		return ball;
 	}
 
 	public void createPair(Vektor v) {
 		Vektor bla = new Vektor(1, 0).mul(jointLength / 2 + 1);
 		Vektor pos1 = v.add(bla);
 		Vektor pos2 = bla.sub(v);
 		Ball ball1 = createBall(pos1);
 		Ball ball2 = createBall(pos2);
 		join(ball1, ball2);
 	}
 
 	private void join(Ball a, Ball b) {
 		Joint joint = new Joint(a, b, jointLength, jointStrength);
 		a.addJoint(joint);
 		b.addJoint(joint);
 		joints.add(joint);
 	}
 
 	/* gibt eine zufällige Farbe aus dem OUTPUT farbschema zurück */
 	private Color randomColor() {
 		List<Color> list = new ArrayList<Color>();
 		Color red = new Color(229, 53, 23, "red");
 		Color green = new Color(151, 190, 13, "green");
 		Color pink = new Color(226, 0, 122, "pink");
 		Color blue = new Color(0, 139, 208, "blue");
 		Color purple = new Color(100, 31, 128, "purple");
 		Color yellow = new Color(255, 204, 0, "yellow");
 
 		list.add(red);
 		list.add(green);
 		list.add(pink);
 		list.add(blue);
 		list.add(purple);
 		list.add(yellow);
 
 		Random rand = new Random();
 
 		return list.get(rand.nextInt(list.size()));
 	}
 
 	public void mousePressedLeft(Vektor v) {
 		Ball b = collidingBall(v);
 		if (b != null) active = b;
 	}
 	
 	public void mousePressedRight(Vektor v) {
 		createPair(v);
 	}
 	
 	public void mouseReleasedLeft(Vektor v) {
 		active = null;
 	}
 	
 	private Ball collidingBall(Vektor v) {
 		for (Ball b : balls)
 			if (b.isHit(v))
 				return b; // TODO more than one ball is clicked?
 		return null;
 	}
 
 	public void checkForHits(float x, float y) {
 		for (Ball b : balls) {
 			if (b.isHit(new Vektor(x, y))) {
 				b.color = new Color(0, 0, 255);
 			}
 		}
 	}
 
 	public Ball checkForCollisions(Ball active) {
 		for (Ball b : balls) {
 			float distance = (float) Math.sqrt(Math.pow(b.getX() - active.getX(), 2)
 					+ Math.pow(b.getY() - active.getY(), 2));
 
 			/* TODO Collisionen mit sich selbst schoener vermeiden */
 			if (distance > 0 && distance <= ballsize && b.color.name == active.color.name) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 	/* deletes all balls */
 	public void resetBalls() {
 		balls = new ArrayList<Ball>();
 	}
 
 	/* deletes all joints */
 	public void resetJoints() {
 		joints = new ArrayList<Joint>();
 	}
 
 	/* deletes orphaned joints */
 	public void cleanUpJoints() {
 		for (Joint j : joints) {
 			if (j.a == null || j.b == null)
 				joints.remove(j);
 		}
 	}
 
 }
