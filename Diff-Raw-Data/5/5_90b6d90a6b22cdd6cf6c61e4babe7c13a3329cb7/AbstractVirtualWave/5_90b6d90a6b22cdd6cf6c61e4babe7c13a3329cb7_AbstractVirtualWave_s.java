 package bnorm.virtual;
 
 import bnorm.base.Tank;
 import bnorm.robots.IRobot;
import bnorm.robots.RobotSnapshots;
 import bnorm.utils.Utils;
 
 /**
  * Represents a wave that exists on the Robocode battlefield. This wave has a
  * starting coordinate, a speed, and a starting time. Given a specified time,
  * we can see where this wave is on the battlefield and how far it has
  * traveled.
  *
  * @author Brian Norman
  * @version 1.0
  */
 public abstract class AbstractVirtualWave implements IVirtualWave {
 
    /**
     * The starting x coordinate of the wave.
     */
    private double x;
 
    /**
     * The starting y coordinate of the wave.
     */
    private double y;
 
    /**
     * The speed of the wave in pixels/tick.
     */
    private double velocity;
 
    /**
     * The time the wave started.
     */
    private long time;
 
    /**
     * Creates a new blank wave. This wave is centered at negative infinity and has no speed and was created at time
     * <code>0</code>.
     */
    public AbstractVirtualWave() {
       this(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, 0.0, 0);
    }
 
    /**
     * Creates a new wave at the specified starting coordinates and with the specified speed and start time.
     *
     * @param x the starting x coordinate.
     * @param y the starting y coordinate.
     * @param velocity the speed of the wave in pixels/tick.
     * @param time the time the wave started.
     */
    public AbstractVirtualWave(double x, double y, double velocity, long time) {
       this.x = x;
       this.y = y;
       this.velocity = velocity;
       this.time = time;
    }
 
    /**
     * Creates a copy of the wave.
     *
     * @param wave the wave to copy.
     */
    protected AbstractVirtualWave(IVirtualWave wave) {
       this(wave.getX(), wave.getY(), wave.getVelocity(), wave.getTime());
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double getX() {
       return x;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double getY() {
       return y;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double getVelocity() {
       return velocity;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public long getTime() {
       return time;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive(long currentTime) {
       return x >= 0 && y >= 0 && x <= Tank.MAX_BATTLEFIELD_WIDTH && y <= Tank.MAX_BATTLEFIELD_HEIGHT &&
               distSq(currentTime) <= Tank.MAX_BATTLEFIELD_DIAGONAL;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive(long currentTime, IRobot target) {
      return isActive(currentTime) && distSq(currentTime) < RobotSnapshots.distSq(target.getSnapshot(), getX(), getY());
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double dist(long currentTime) {
       return velocity * (currentTime - time);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double distSq(long currentTime) {
       return Utils.sqr(dist(currentTime));
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
       if (obj instanceof IVirtualWave) {
          IVirtualWave wave = (IVirtualWave) obj;
          return wave.getX() == x && wave.getY() == y && wave.getVelocity() == velocity && wave.getTime() == time;
       }
       return super.equals(obj);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
       return this.getClass() + "[x=" + x + ", y=" + y + ", velocity=" + velocity + ", time" + time + "]";
    }
 }
