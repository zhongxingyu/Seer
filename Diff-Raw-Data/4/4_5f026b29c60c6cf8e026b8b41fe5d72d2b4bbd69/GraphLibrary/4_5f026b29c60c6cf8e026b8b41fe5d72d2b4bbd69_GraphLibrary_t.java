 package nl.cwi.sen.metastudio.graphview;
 
 import metastudio.graph.Edge;
 import metastudio.graph.EdgeList;
 import metastudio.graph.Graph;
 import metastudio.graph.Node;
 import metastudio.graph.NodeList;
 import metastudio.graph.Point;
 import metastudio.graph.Polygon;
 import metastudio.graph.Shape;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.GC;
 
 public class GraphLibrary {
 	private GC gc;
 
 	private final int ARROWHEAD_LENGTH = 15;
 	private final int ARROWHEAD_SHARPNESS = 15;
 
 	private Color background;
 
 	private Color nodeBG;
 	private Color nodeFG;
 	private Color nodeBorder;
 
 	private Color nodeBGHovered;
 	private Color nodeFGHovered;
 	private Color nodeBorderHovered;
 
 	private Color nodeBorderSelected;
 	private Color nodeBGSelected;
 	private Color nodeFGSelected;
 
 	private Node hoveredNode;
 	private Node selectedNode;
 
 	public GraphLibrary(GC gc) {
 		this.gc = gc;
 
 		setupColors();
 	}
 
 	private void setupColors() {
 		background = new Color(null, 0, 0, 0);
 
 		nodeBG = new Color(null, 255, 255, 221);
 		nodeFG = new Color(null, 0, 0, 0);
 		nodeBorder = new Color(null, 0, 0, 0);
 
 		nodeBGHovered = new Color(null, 255, 255, 187);
 		nodeFGHovered = new Color(null, 0, 0, 0);
 		nodeBorderHovered = new Color(null, 0, 0, 255);
 
 		nodeBGSelected = new Color(null, 255, 255, 187);
 		nodeFGSelected = new Color(null, 0, 0, 255);
 		nodeBorderSelected = new Color(null, 255, 0, 0);
 	}
 
 	public void paintEdges(Graph graph) {
 		EdgeList edges = graph.getEdges();
 
 		while (!edges.isEmpty()) {
 			Edge edge = edges.getHead();
 			edges = edges.getTail();
 
 			paintEdge(edge);
 		}
 	}
 
 	private void paintEdge(Edge edge) {
 		if (!edge.isPositioned()) {
 			return;
 		}
 
 		Polygon poly = edge.getPolygon();
 
 		Point to = poly.getHead();
 		Point from = to;
 
 		// TODO: Undo brute initialize of arraylength 16
 		int[][] points = new int[16][2];
 		int n = 0;
 
 		while (poly.hasTail() && !poly.getTail().isEmpty()) {
 			from = to;
 			points[n][0] = from.getX();
 			points[n][1] = from.getY();
 			n++;
 			poly = poly.getTail();
 			to = poly.getHead();
 		}
 
 		points[n][0] = to.getX();
 		points[n][1] = to.getY();
 
 		if (edge.connectedTo(hoveredNode)) {
 			gc.setForeground(nodeBorderHovered);
 		} else if (edge.connectedTo(selectedNode)) {
 			gc.setForeground(nodeBorderSelected);
 		} else {
 			gc.setForeground(nodeBorder);
 		}
		gc.setBackground(gc.getForeground());
 
 		bspline(points, n);
 		arrowHead(from, to);
 	}
 
 	public void paintNodes(Graph graph) {
 		NodeList nodes = graph.getNodes();
 		while (!nodes.isEmpty()) {
 			Node node = nodes.getHead();
 			nodes = nodes.getTail();
 
 			paintNode(node);
 		}
 	}
 
 	private void paintNode(Node node) {
 		if (!node.isPositioned()) {
 			return;
 		}
 
 		int x = node.getX();
 		int y = node.getY();
 		int w = node.getWidth();
 		int h = node.getHeight();
 
 		x -= w / 2;
 		y -= h / 2;
 
 		//		if (!g.hitClip(x, y, w, h)) {
 		//			return;
 		//		}
 		//
 		Color node_bg, node_fg, node_border;
 
 		if (selectedNode != null
 			&& selectedNode.getId().equals(node.getId())) {
 			node_bg = nodeBGSelected;
 			node_fg = nodeFGSelected;
 			node_border = nodeBorderSelected;
 		} else if (
 			hoveredNode != null && hoveredNode.getId().equals(node.getId())) {
 			node_bg = nodeBGHovered;
 			node_fg = nodeFGHovered;
 			node_border = nodeBorderHovered;
 		} else {
 			node_bg = nodeBG;
 			node_fg = nodeFG;
 			node_border = nodeBorder;
 		}
 
 		Shape shape = Graph.getNodeShape(node);
 
 		if (shape.isBox()) {
 			paintBox(x, y, w, h, node_bg, node_border);
 		} else if (shape.isEllipse()) {
 			paintEllipse(x, y, w, h, node_bg, node_border);
 		} else if (shape.isDiamond()) {
 			paintDiamond(x, y, w, h, node_bg, node_border);
 		} else {
 			// default case, we draw a rectangle
 			paintBox(x, y, w, h, node_bg, node_border);
 		}
 
 		//setting font properties
 		FontData data = new FontData("SansSerif", 11, SWT.DRAW_TRANSPARENT);
 		Font font = new Font(null, data);
 		gc.setFont(font);
 
 		String name = node.getLabel();
 		int tw = gc.stringExtent(name).x;
 		int th = gc.getFontMetrics().getAscent();
 
 		gc.setForeground(node_fg);
 		gc.drawString(name, x + (w - tw) / 2, y + (h + th) / 2 - th - 1);
 	}
 
 	private void paintDiamond(
 		int x,
 		int y,
 		int w,
 		int h,
 		Color node_bg,
 		Color node_border) {
 		gc.setBackground(node_bg);
 		int[] xys =
 			new int[] {
 				x,
 				y + h / 2,
 				x + w / 2,
 				y,
 				x + w,
 				y + h / 2,
 				x + w / 2,
 				y + h };
 
 		gc.fillPolygon(xys);
 		gc.setForeground(node_border);
 		gc.drawPolygon(xys);
 	}
 
 	private void paintEllipse(
 		int x,
 		int y,
 		int w,
 		int h,
 		Color node_bg,
 		Color node_border) {
 		gc.setBackground(node_bg);
 		gc.fillOval(x, y, w, h);
 		gc.setForeground(node_border);
 		gc.drawOval(x, y, w, h);
 	}
 
 	private void paintBox(
 		int x,
 		int y,
 		int w,
 		int h,
 		Color node_bg,
 		Color node_border) {
 		gc.setBackground(node_bg);
 		gc.fillRectangle(x, y, w, h);
 		gc.setForeground(node_border);
 		gc.drawRectangle(x, y, w, h);
 	}
 
 	private void arrowHead(Point from, Point to) {
 		int x1 = from.getX();
 		int y1 = from.getY();
 		int x2 = to.getX();
 		int y2 = to.getY();
 
 		// calculate points for arrowhead
 		double angle = Math.atan2(y2 - y1, x2 - x1) + Math.PI;
 		double theta = Math.toRadians(ARROWHEAD_SHARPNESS);
 		double size = ARROWHEAD_LENGTH;
 
 		int x3 = (int) (x2 + Math.cos(angle - theta) * size);
 		int y3 = (int) (y2 + Math.sin(angle - theta) * size);
 
 		int x4 = (int) (x2 + Math.cos(angle + theta) * size);
 		int y4 = (int) (y2 + Math.sin(angle + theta) * size);
 
 		int[] xys = new int[] { x2, y2, x3, y3, x4, y4, x2, y2 };
 
 		gc.fillPolygon(xys);
 		gc.drawPolygon(xys);
 	}
 
 	private void bspline(int[][] P, int n) {
 		float m = 500;
 		float xA, yA, xB, yB, xC, yC, xD, yD;
 		float a0, a1, a2, a3, b0, b1, b2, b3;
 		float x = 0, y = 0, x0, y0;
 		boolean first = true;
 
 		if (n < 5) {
 			gc.drawLine(P[0][0], P[0][1], P[n][0], P[n][1]);
 		} else {
 			for (int i = 1; i < n - 2; i++) {
 				xA = P[i - 1][0];
 				xB = P[i][0];
 				xC = P[i + 1][0];
 				xD = P[i + 2][0];
 				yA = P[i - 1][1];
 				yB = P[i][1];
 				yC = P[i + 1][1];
 				yD = P[i + 2][1];
 				a3 = (-xA + 3 * (xB - xC) + xD) / 6;
 				b3 = (-yA + 3 * (yB - yC) + yD) / 6;
 				a2 = (xA - 2 * xB + xC) / 2;
 				b2 = (yA - 2 * yB + yC) / 2;
 				a1 = (xC - xA) / 2;
 				b1 = (yC - yA) / 2;
 				a0 = (xA + 4 * xB + xC) / 6;
 				b0 = (yA + 4 * yB + yC) / 6;
 				for (int j = 0; j <= m; j++) {
 					x0 = x;
 					y0 = y;
 					float t = (float) j / (float) m;
 					x = (int) (((a3 * t + a2) * t + a1) * t + a0);
 					y = (int) (((b3 * t + b2) * t + b1) * t + b0);
 					if (first) {
 						first = false;
 						x0 = P[0][0];
 						y0 = P[0][1];
 						gc.drawLine((int) x0, (int) y0, (int) x, (int) y);
 						x0 = x;
 						y0 = y;
 					} else {
 						gc.drawLine((int) x0, (int) y0, (int) x, (int) y);
 					}
 				}
 			}
 			gc.drawLine((int) x, (int) y, P[n][0], P[n][1]);
 		}
 	}
 
 	public boolean nodeSelected(Node node) {
 		if ((node == null && selectedNode != null)
 			|| (node != null
 				&& (selectedNode == null
 					|| !selectedNode.getId().equals(node.getId())))) {
 			selectedNode = node;
 			return true;
 		}
 		return false;
 	}
 
 	public boolean nodeHighlighted(Node node) {
 		if ((node == null && hoveredNode != null)
 			|| (node != null
 				&& (hoveredNode == null
 					|| !hoveredNode.getId().equals(node.getId())))) {
 			hoveredNode = node;
 			return true;
 		}
 		return false;
 	}
 
 	public Node getNodeAt(Graph graph, int x, int y) {
 		if (graph == null) {
 			return null;
 		}
 
 		NodeList nodes = graph.getNodes();
 
 		while (!nodes.isEmpty()) {
 			Node node = nodes.getHead();
 			nodes = nodes.getTail();
 
 			if (node.isPositioned()) {
 				int width = node.getWidth();
 				int height = node.getHeight();
 
 				if (x >= node.getX() - width / 2
 					&& x < node.getX() + width / 2
 					&& y >= node.getY() - height / 2
 					&& y < node.getY() + height / 2) {
 					return node;
 				}
 			}
 		}
 		return null;
 	}
 }
