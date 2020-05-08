 package com.github.joukojo.testgame;
 
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 
 import org.apache.commons.lang.builder.ToStringBuilder;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.github.joukojo.testgame.images.ImageFactory;
 import com.github.joukojo.testgame.world.core.Moveable;
 
 public class Player implements Moveable {
 
 	private final static Logger LOG = LoggerFactory.getLogger(Player.class);
 	private int positionY = 0;
 	private double directionX = 0;
 	private double directionY = 0;
 	private int level = 1;
 	private long score = 0;
 	private long health = 100;
 	private Point point = new Point(300, 300);
 	private int positionX = 0;
 
 	public int getPositionX() {
 		return positionX;
 	}
 
 	public void setPositionX(final int positionX) {
 		this.positionX = positionX;
 	}
 
 	public int getPositionY() {
 		return positionY;
 	}
 
 	public void setPositionY(final int positionY) {
 		this.positionY = positionY;
 	}
 
 	public double getDirectionX() {
 		return directionX;
 	}
 
 	public void setDirectionX(final double directionX) {
 		this.directionX = directionX;
 	}
 
 	public double getDirectionY() {
 		return directionY;
 	}
 
 	public void setDirectionY(final double directionY) {
 		this.directionY = directionY;
 	}
 
 	public void setTarget(final Point point) {
 		/*
 		 * [px py] (get-in @data [:player :position]) [tx ty] (get-in @data
 		 * [:player :target] [0 0]) dx (* (- tx px) 0.01) dy (* (- ty py) 0.01)
 		 * new-position [(+ px dx) (+ py dy)] new-direction [dx dy]]
 		 */
 
 	}
 
 	@Override
 	public void draw(final Graphics graphics) {
 
 		final double direction = direction(directionX, directionY);
 		if (LOG.isDebugEnabled()) {
 			Object params[] = { directionX, directionY, direction };
 			LOG.debug("direction (x/y) : ({}/{} -> {})", params);
 		}
 		final int value = Double.valueOf(direction).intValue();
 		LOG.debug("player direction: {} degress", value);
 		//
 		// BufferedImage image = ImageFactory.getPlayerNorthImage();
 		//
		BufferedImage image = ImageFactory.getImageForDegree(1);
 		
 		graphics.drawImage(image, positionX, positionY, null);
 
 	}
 
 	protected double direction(final double x, final double y) {
 
 		if (x == 0 && y < 0) {
 			return 0; // NORTH
 		} else if (x > 0 && y == 0) {
 			return 90; // EAST
 		} else if (x == 0 && y > 0) {
 			return 180; // SOUTH
 		} else if (x < 0 && y == 0) {
 			return 270; // WEST
 		}
 
 		if (x > 0 && y < 0) {
 			// NE
 			LOG.debug("NE");
 			return 90 - Math.toDegrees(Math.atan(Math.abs(y) / x));
 		} else if (x > 0 && y > 0) {
 			LOG.debug("SE");
 			// SE
 			return 180 - Math.toDegrees(Math.atan(y / x));
 		} else if (x < 0 && y > 0) {
 			LOG.debug("SW");
 			// SW
 			return 270 - Math.toDegrees(Math.atan(y / Math.abs(x)));
 		} else if (x < 0 && y < 0) {
 			LOG.debug("NW");
 			// NW
 			return 360 - Math.toDegrees(Math.atan(Math.abs(y) / Math.abs(x)));
 		}
 
 		return 0;
 	}
 
 	@Override
 	public void move() {
 
 		final int targetX = getPoint().x;
 		final int targetY = getPoint().y;
 
 		directionX = ((targetX - positionX) * 0.05);
 		directionY = ((targetY - positionY) * 0.05);
 
 		final double newPositionX = positionX + directionX;
 		final double newPositionY = positionY + directionY;
 
 		if (LOG.isDebugEnabled()) {
 			LOG.debug("newPositionX {}", newPositionX);
 			LOG.debug("newPositionY {}", newPositionY);
 		}
 
 		if (newPositionX <= 0) {
 			positionX = 0;
 		} else if (newPositionX >= DisplayConfiguration.getInstance().getWidth()) {
 			positionX = DisplayConfiguration.getInstance().getWidth();
 		} else {
 			positionX = (int) newPositionX;
 		}
 
 		if (newPositionY <= 0) {
 			positionY = 0;
 		} else if (newPositionY >= DisplayConfiguration.getInstance().getHeight()) {
 			positionY = DisplayConfiguration.getInstance().getHeight();
 		} else {
 			positionY = (int) newPositionY;
 		}
 		LOG.debug("PositionX {}", positionX);
 		LOG.debug("PositionY {}", positionY);
 
 	}
 
 	@Override
 	public boolean isOutside(final int x, final int y) {
 		return (positionX > x || positionX < 0)
 				|| (positionY > y || positionY < 0);
 	}
 
 	@Override
 	public boolean isDestroyed() {
 
 		return !(getHealth() > 0);
 	}
 
 	@Override
 	public void setDestroyed(final boolean b) {
 
 	}
 
 	@Override
 	public String toString() {
 		return ToStringBuilder.reflectionToString(this);
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public void setLevel(final int level) {
 		this.level = level;
 	}
 
 	public long getScore() {
 		return score;
 	}
 
 	public void setScore(final long score) {
 		this.score = score;
 	}
 
 	public long getHealth() {
 		return health;
 	}
 
 	public void setHealth(final long health) {
 		this.health = health;
 	}
 
 	public Point getPoint() {
 		return point;
 	}
 
 	public void setPoint(final Point point) {
 		this.point = point;
 	}
 
 }
