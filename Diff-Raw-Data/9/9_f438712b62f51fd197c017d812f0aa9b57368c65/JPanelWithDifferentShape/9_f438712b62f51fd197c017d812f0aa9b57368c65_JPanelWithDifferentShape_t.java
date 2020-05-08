 package com.mgs.fantasi.ui.driver.swing;
 
 import com.mgs.fantasi.polygon.PolygonPointsIterator;
 import com.mgs.fantasi.ui.profile.BorderDefinition;
 import com.mgs.fantasi.ui.profile.UIStyle;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.geom.Path2D;
 import java.awt.geom.Point2D;
 import java.util.Iterator;
 import java.util.Set;
 
 class JPanelWithDifferentShape extends JPanel {
 	private final PolygonPointsIterator shape;
 	private final Set<UIStyle> uiStyles;
 
 	public JPanelWithDifferentShape(PolygonPointsIterator shape, Set<UIStyle> uiStyles) {
 		this.shape = shape;
 		this.uiStyles = uiStyles;
 		setOpaque(false);
 	}
 
 	@Override
 	public void paint(Graphics graphics) {
 		super.paint(graphics);
 		Graphics2D g2d = (Graphics2D) graphics;
 		Iterator<UIStyle> iterator = uiStyles.iterator();
 		if (! iterator.hasNext()) return;
 		UIStyle currentStyle = iterator.next();
 		BorderDefinition border = currentStyle.getBorder();
 		float borderThickness = 0;
 		Color foregroundColor = g2d.getColor();
 		if (border !=null){
 			borderThickness = border.getWidth();
 			foregroundColor = border.getColor();
 		}
 		Color backgroundColor = currentStyle.getBackgroundColor() != null ?
 				currentStyle.getBackgroundColor() :
 				Color.WHITE;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		drawHexagon(g2d, getSize(), foregroundColor, backgroundColor, borderThickness * 2);
 	}
 
 	private void drawHexagon(Graphics2D g2d, Dimension size, Color foregroundColor, Color backgroundColor, float thickness) {
 		int insetSpace = (int) thickness;
 		Dimension sizeWithoutBorders = new Dimension(size.width - insetSpace, size.height - insetSpace);
 		if (sizeWithoutBorders.getWidth()<0 || sizeWithoutBorders.getHeight()<0) return;
 		java.util.List<Point2D.Double> hexagonPoints = shape.getPointListFromBottomLeftCorner(sizeWithoutBorders);
 		Path2D.Double path = new Path2D.Double();
 		nextLine(path, hexagonPoints.get(0), thickness);
 		for (int i = 1; i < hexagonPoints.size() ; i++) {
 			nextLine(path, hexagonPoints.get(i), thickness);
 		}
 		path.closePath();
 		g2d.setColor(foregroundColor);
 		g2d.setStroke(new BasicStroke(thickness, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
 		g2d.draw(path);
 		g2d.setColor(backgroundColor);
 		g2d.fill(path);
 	}
 
 	private void nextLine(Path2D.Double path, Point2D.Double point, float thickness) {
 		double x = point.getX();
 		double y = point.getY();
 
 		x+=thickness/2;
 		y+=thickness/2;
 
 		if (path.getCurrentPoint() == null){
 			path.moveTo(x, y);
 		}else{
 			path.lineTo(x, y);
 		}
 	}
 }
