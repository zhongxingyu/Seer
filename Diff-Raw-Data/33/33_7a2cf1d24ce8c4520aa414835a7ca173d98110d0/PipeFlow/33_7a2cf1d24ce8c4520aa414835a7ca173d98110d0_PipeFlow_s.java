 package pleocmd.itfc.gui;
 
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Point2D.Double;
 
 import pleocmd.pipe.PipePart;
 
 final class PipeFlow {
 
 	private static final int STEPS = 10;
 
 	private static final int FLOW_LEN = 4;
 
 	private final PipePart src;
 	private final PipePart dst;
 	private final Double origin;
 	private double theta;
 	private double delta;
 	private double offset;
 	private int steps;
 
 	PipeFlow(final PipePart src, final PipePart dst) {
 		this.src = src;
 		this.dst = dst;
 		origin = new Double();
 		steps = STEPS;
 	}
 
 	public void draw(final Graphics2D g2) {
 		final AffineTransform at = g2.getTransform();
 		g2.translate(origin.x, origin.y);
 		g2.rotate(theta);
 		g2.drawLine((int) -offset, 0, (int) (-offset - FLOW_LEN), 0);
 		g2.setTransform(at);
 	}
 
 	public boolean nextStep() {
 		offset += delta;
 		return --steps > 0;
 	}
 
 	Double getOrigin() {
 		return origin;
 	}
 
 	void setTheta(final double theta) {
 		this.theta = theta;
 	}
 
 	void setParams(final double len) {
 		delta = (len - FLOW_LEN) / STEPS;
 		offset = (STEPS - steps) * delta;
 	}
 
 	PipePart getSrc() {
 		return src;
 	}
 
 	PipePart getDst() {
 		return dst;
 	}
 
 }
