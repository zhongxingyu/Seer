 import java.awt.Color;
 
 import acm.graphics.GCompound;
 import acm.graphics.GLine;
 import acm.graphics.GPoint;
 
 @SuppressWarnings("serial")
 public class PLine extends GCompound implements Drawable, Selectable, MadeWith2Points {
 
 	public static final double POINT_DIAMETER = 10;
 	public static final double EPSILON = 0.001;
 	public static final int FREE_LINE = 1;
 	public static final int SEGMENT_BISECTOR = 2;
 	private static final double EDGE_OFFSET = 20;
 	
 	public static ViewingRectangle viewingRectangle;
 	
 	private GLine line_;
 	private final String label_;
 	private PPoint p1_;
 	private PPoint p2_;
 	private boolean selected_;
 	private FancyLabel fancyLabel_;
 	private boolean exists_;
 	
 	public GPoint[] getEdgePoints() {
 		
 		GPoint[] edgePts = new GPoint[2];
 		edgePts[0] = viewingRectangle.findBoundingRectIntersection(p1_, p2_);
 		edgePts[1] = viewingRectangle.findBoundingRectIntersection(p2_, p1_);
 		return edgePts;
 	}
 	
 	public PLine(PPoint p1, PPoint p2, String label) {
 		line_ = new GLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
 		this.add(line_);
 		p1_ = p1;
 		p2_ = p2;
 		label_ = label;
 		fancyLabel_ = new FancyLabel(label_);
 		this.add(fancyLabel_);
 		setSelected(false);
 		exists_ = true;
 		update();
 	}
 	
 	public boolean exists() {
 		return exists_;
 	}
 	
 	public void setExists(boolean exists) {
 		exists_ = exists;
 	}
 	
 	public void update() {
 		GPoint[] edgePoints = this.getEdgePoints();
 		
 		if ((edgePoints[0] != null) && (edgePoints[1] != null)) {
 			line_.setStartPoint(edgePoints[0].getX(),edgePoints[0].getY());
 			line_.setEndPoint(edgePoints[1].getX(),edgePoints[1].getY());
 		}
 		
 		double rxMin = viewingRectangle.getXMin() + EDGE_OFFSET;
 		double rxMax = viewingRectangle.getXMax() - EDGE_OFFSET;
 		double ryMin = viewingRectangle.getYMin() + EDGE_OFFSET;
 		double ryMax = viewingRectangle.getYMax() - EDGE_OFFSET;
 		ViewingRectangle labelRectangle = new ViewingRectangle(rxMin, rxMax, ryMin, ryMax);
 		GPoint labelLocationPt = labelRectangle.findBoundingRectIntersection(p1_, p2_);
 		if (labelLocationPt != null) {
 			fancyLabel_.setLocation(labelLocationPt);
 		}
 		
 	}
 	
 	public void setSelected(boolean selected) {
 		selected_ = selected;
 		setColor(selected ? Color.MAGENTA : Color.BLACK);
 	}
 	
 	public boolean isSelected() {
 		return selected_;
 	}
 	
 	public PPoint get1stPoint() {
 		return p1_;
 	}
 	
 	public PPoint get2ndPoint() {
 		return p2_;
 	}
 	
 	public void set1stPoint(PPoint p1) {
 		p1_ = p1;
 	}
 	
 	public void set2ndPoint(PPoint p2) {
 		p2_ = p2;
 	}
 	
 	public String getLabel() {
 		return label_;
 	}
 	
 	public double distanceTo(double x, double y) {
 		double ax = p1_.getX();
 		double ay = p1_.getY();
 		double bx = p2_.getX();
 		double by = p2_.getY();
 		double normalLength = Math.hypot(bx - ax, by - ay);
 		return Math.abs((x-ax)*(by-ay) - (y-ay)*(bx-ax)) / normalLength;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if (o instanceof PLine) {
 			return getLabel().equals(((PLine) o).getLabel());
 		}
 		else {
 			return false;
 		}
 	}
 	
 	@Override
 	public String toString() {
 		return String.format("%s from (%f.1, %f.1) to (%f.1, %f.1)",
 				getLabel(),
 				p1_.getX(), p1_.getY(),
 				p2_.getX(), p2_.getY());
 	}
 	
 	
 }
 
