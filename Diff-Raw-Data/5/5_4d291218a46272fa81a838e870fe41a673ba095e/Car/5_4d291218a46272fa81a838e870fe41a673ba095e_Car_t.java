 package racing;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.List;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 
 public abstract class Car extends JComponent {
 
 	private static final long serialVersionUID = -8147987130820080891L;
 	public static final double SLOW_SPEED = 1;
 
 	protected double x = 0;
 	protected double y = 0;
 	protected float angle = 0;
 
 	private double oldX = 0;
 	private double oldY = 0;
 	private double speed = 0.0;
 
 	public Car() {
 	}
 
 	@Override
 	public void move(final int x, final int y) {
 		this.x = x;
 		this.y = y;
 	}
 
 	public void move(final double x, final double y) {
 		oldX = this.x;
 		oldY = this.y;
 		this.x = x;
 		this.y = y;
 	}
 
 	public void move() {
 		final double newX = x + Math.sin(angle) * speed;
 		final double newY = y - Math.cos(angle) * speed;
 		move(newX, newY);
 	}
 
 	private int doubleToInt(final double doubleValue) {
 		final int baseInt = (int) doubleValue;
 		return doubleValue - baseInt >= 0.5 ? baseInt + 1 : baseInt;
 	}
 
 	public void accelerate() {
 		if (speed <= getMaxForwardSpeed()) {
 			speed += 0.1;
 		}
 	}
 
 	public void deccelerate() {
 		if (speed >= getMaxBackwardSpeed()) {
 			speed -= 0.1;
 		}
 	}
 
 	public void slowdown() {
 		slowdown(1);
 	}
 
 	public void activateBreaks() {
 		slowdown(2);
 	}
 
 	public void slowdown(final double factor) {
 		speed -= 0.2 * factor;
 		if (speed < 0) {
 			speed = 0;
 		}
 	}
 
 	public void slowdownBackward(final double factor) {
 		speed += 0.2 * factor;
 		if (speed > 0) {
 			speed = 0;
 		}
 	}
 
 	public void setSlowSpeed() {
 		if (speed > SLOW_SPEED) {
 			slowdown(2.8);
 		}
 	}
 
 	public void setMinSlowSpeed() {
 		if (speed < -SLOW_SPEED) {
 			speed = -SLOW_SPEED;
 		}
 
 	}
 
 	public void stop() {
 		speed = 0;
 	}
 
 	public void turnRight() {
 		angle += Math.PI / (40 / getAgility());
 	}
 
 	public void turnLeft() {
 		angle -= Math.PI / (40 / getAgility());
 	}
 
 	@Override
 	public int getX() {
 		return doubleToInt(x);
 	}
 
 	@Override
 	public int getY() {
 		return doubleToInt(y);
 	}
 
 	public int getOldY() {
 		return doubleToInt(oldY);
 	}
 
 	public int getOldX() {
 		return doubleToInt(oldX);
 	}
 
 	public float getAngle() {
 		return angle;
 	}
 
 	public double getSpeed() {
 		return speed;
 	}
 
 	public void setAngle(final float angle) {
 		this.angle = angle;
 	}
 
 	protected Dimension calculateMatrix(double absoluteX, double absoluteY) {
 		absoluteX -= x;
 		absoluteY -= y;
 		final double rotatedX = (Math.cos(angle) * absoluteX)
 				- (absoluteY * Math.sin(angle));
 		final double rotatedY = (Math.sin(angle) * absoluteX)
 				+ (absoluteY * Math.cos(angle));
 		final Dimension d = new Dimension();
 		d.setSize(rotatedX + x, rotatedY + y);
 
 		return d;
 	}
 
 	public void bumpBack() {
 		if (speed >= 0) {
			speed = -0.3 * speed;
 		} else {
			speed = 0.3 * (2 * speed);
 		}
 	}
 
 	public abstract List<Dimension> getCollisionModel();
 
 	public abstract double getAcceleration();
 
 	public abstract double getAgility();
 
 	public abstract double getMaxForwardSpeed();
 
 	public abstract double getMaxBackwardSpeed();
 
 	@Override
 	protected void paintComponent(final Graphics g) {
 		super.paintComponent(g);
 	}
 
 	public static void main(final String[] args) {
 		final JFrame f = new JFrame("Racing");
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		final Car c = new RacingCar();
 		c.move(50, 50);
 		f.add(c);
 		f.setSize(200, 240);
 		f.setResizable(false);
 		f.setVisible(true);
 
 	}
 
 }
