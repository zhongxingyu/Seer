 package org.bodytrack.client;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.gwt.event.dom.client.MouseDownEvent;
 import com.google.gwt.event.dom.client.MouseDownHandler;
 import com.google.gwt.event.dom.client.MouseMoveEvent;
 import com.google.gwt.event.dom.client.MouseMoveHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseUpEvent;
 import com.google.gwt.event.dom.client.MouseUpHandler;
 import com.google.gwt.event.dom.client.MouseWheelEvent;
 import com.google.gwt.event.dom.client.MouseWheelHandler;
 import gwt.g2d.client.graphics.Surface;
 import gwt.g2d.client.math.Vector2;
 
 public class GraphWidget extends Surface {
 	private static final double MOUSE_WHEEL_ZOOM_RATE_MAC = 1.003;
 	private static final double MOUSE_WHEEL_ZOOM_RATE_PC = 1.1;
 
 	private List<DataPlot> dataPlots;
 
 	/* xAxes and yAxes provide redundant information - the list
 	 * of all axes used by all the plots in dataPlots.  This is
 	 * for performance reasons.
 	 */
 	private List<GraphAxis> xAxes;
 	private List<GraphAxis> yAxes;
 
 	private int width, height;
 	private int axisMargin;
 	private int graphMargin = 5;
 
 	private int graphWidth, graphHeight;
 
 	private GraphAxis mouseDragAxis;
 	private Vector2 mouseDragLastPos;
 
 	// Once a GraphWidget object is instantiated, this doesn't change
 	private final double mouseWheelZoomRate;
 
 	public GraphWidget(int width, int height, int axisMargin) {
 		super(width, height);
 		this.width = width;
 		this.height = height;
 		this.axisMargin = axisMargin;
 
 		dataPlots = new ArrayList<DataPlot>();
 		xAxes = new ArrayList<GraphAxis>();
 		yAxes = new ArrayList<GraphAxis>();
 
 		this.addMouseWheelHandler(new MouseWheelHandler() {
 			@Override
 			public void onMouseWheel(MouseWheelEvent event) {
 				handleMouseWheelEvent(event);
 			}
 		});
 
 		this.addMouseDownHandler(new MouseDownHandler() {
 			@Override
 			public void onMouseDown(MouseDownEvent event) {
 				handleMouseDownEvent(event);
 			}
 		});
 
 		this.addMouseMoveHandler(new MouseMoveHandler() {
 			@Override
 			public void onMouseMove(MouseMoveEvent event) {
 				handleMouseMoveEvent(event);
 			}
 		});
 
 		this.addMouseUpHandler(new MouseUpHandler() {
 			@Override
 			public void onMouseUp(MouseUpEvent event) {
 				handleMouseUpEvent(event);
 			}
 		});
 
 		this.addMouseOutHandler(new MouseOutHandler() {
 			@Override
 			public void onMouseOut(MouseOutEvent event) {
 				handleMouseOutEvent(event);
 			}
 		});
 
 		mouseWheelZoomRate = shouldZoomMac()
 			? MOUSE_WHEEL_ZOOM_RATE_MAC
 			: MOUSE_WHEEL_ZOOM_RATE_PC;
 	}
 
 	/**
 	 * Tells whether this application should use the Mac scroll wheel ratio.
 	 *
 	 * Checks the <tt>navigator.platform</tt> property in JavaScript to
 	 * determine if this code is on a Mac or not, and returns <tt>true</tt>
 	 * iff the best guess is Mac.  If this property cannot be read, returns
 	 * <tt>false</tt>.
 	 *
 	 * @return
 	 * 		<tt>true</tt> if and only if the <tt>navigator.platform</tt>
 	 * 		property contains the string &quot;Mac&quot;
 	 */
 	private native boolean shouldZoomMac() /*-{
 		// Don't do anything unless navigator.platform is available
		if (! $wnd.navigator && $wnd.navigator.platform)
 			return false;
 
		return $wnd.navigator.platform.toString().match(/.*mac/i);
 	}-*/;
 
 	GraphAxis findAxis(Vector2 pos) {
 		for (GraphAxis axis: xAxes) {
 			if (axis.contains(pos))
 				return axis;
 		}
 
 		for (GraphAxis axis: yAxes) {
 			if (axis.contains(pos))
 				return axis;
 		}
 
 		return null;
 	}
 
 	private void handleMouseWheelEvent(MouseWheelEvent event) {
 		// TODO: enforce minimum zoom on X-axes
 		// Have to check all DataPlot objects to see if at least one
 		// of them allows more zooming
 
 		Vector2 eventLoc = new Vector2(event.getX(), event.getY());
 		GraphAxis axis = findAxis(eventLoc);
 
 		double zoomFactor = Math.pow(mouseWheelZoomRate,
 			event.getDeltaY());
 
 		if (axis != null) {
 			double zoomAbout = axis.unproject(eventLoc);
 			axis.zoom(zoomFactor, zoomAbout);
 		} else {
 			// Zoom on all Y-axes
 
 			for (GraphAxis yAxis: yAxes) {
 				double zoomAbout = yAxis.unproject(eventLoc);
 				yAxis.zoom(zoomFactor, zoomAbout);
 			}
 		}
 
 		paint();
 	}
 
 	private void handleMouseDownEvent(MouseDownEvent event) {
 		Vector2 pos = new Vector2(event.getX(), event.getY());
 
 		mouseDragAxis = findAxis(pos);
 		mouseDragLastPos = pos;
 
 		paint();
 	}
 
 	private void handleMouseMoveEvent(MouseMoveEvent event) {
 		Vector2 pos = new Vector2(event.getX(), event.getY());
 
 		if (mouseDragLastPos != null) {
 			if (mouseDragAxis != null)
 				mouseDragAxis.drag(mouseDragLastPos, pos);
 			else {
 				// Drag on all axes
 
 				for (GraphAxis xAxis: xAxes)
 					xAxis.drag(mouseDragLastPos, pos);
 
 				for (GraphAxis yAxis: yAxes)
 					yAxis.drag(mouseDragLastPos, pos);
 			}
 
 			mouseDragLastPos = pos;
 		}
 
 		paint();
 	}
 
 	private void handleMouseUpEvent(MouseUpEvent event) {
 		mouseDragAxis = null;
 		mouseDragLastPos = null;
 
 		paint();
 	}
 
 	private void handleMouseOutEvent(MouseOutEvent event) {
 		mouseDragAxis = null;
 		mouseDragLastPos = null;
 
 		paint();
 	}
 
 	private void layout() {
 		int xAxesWidth = calculateAxesWidth(xAxes);
 		int yAxesWidth = calculateAxesWidth(yAxes);
 		graphWidth = width - graphMargin - yAxesWidth;
 		graphHeight = height - graphMargin - xAxesWidth;
 		Vector2 xAxesBegin = new Vector2(graphMargin,
 			graphHeight + graphMargin);
 		layoutAxes(xAxes, graphWidth, xAxesBegin, Basis.xDownYRight);
 
 		Vector2 yAxesBegin = new Vector2(graphWidth+graphMargin,
 			graphHeight + graphMargin);
 		layoutAxes(yAxes, graphHeight, yAxesBegin, Basis.xRightYUp);
 	}
 
 	private int calculateAxesWidth(List<GraphAxis> axes) {
 		int ret = 0;
 
 		for (int i=0; i < axes.size(); i++) {
 			ret += axes.get(i).getWidth();
 
 			if (i > 0)
 				ret += axisMargin;
 		}
 
 		return ret;
 	}
 
 	private void layoutAxes(List<GraphAxis> axes, double length,
 			Vector2 begin, Basis basis) {
 		Vector2 offset = begin;
 
 		for (GraphAxis axis: axes) {
 			axis.layout(offset, length);
 
 			offset = offset.add(
 					basis.x.scale(axisMargin + axis.getWidth()));
 		}
 	}
 
 	public void paint() {
 		layout();
 		this.clear();
 		//this.clearRectangle(0,0,400,400);
 		//this.setFillStyle(new Color(
 		//		(int) (Random.nextDouble() * 255), 128, 128));
 		//this.fillRectangle(0,0,500,500);
 		this.save();
 		this.translate(.5, .5);
 
 		for (DataPlot plot: dataPlots)
 			plot.paint();
 
 		this.restore();
 
 		//DirectShapeRenderer renderer = new DirectShapeRenderer(this);
 		//renderer.beginPath();
 
 		//for (double x = 0; x < 400; x++) {
 		//	double y = 200+200*Math.sin(x*.1);
 		//	renderer.drawLineTo(x,y);
 		//}
 		//renderer.stroke();
 	}
 
 	/**
 	 * Returns <tt>true</tt> if and only if this widget holds a
 	 * <strong>reference</strong> to axis.
 	 *
 	 * In other words, this will not return <tt>true</tt> unless
 	 * this GraphWidget contains the exact GraphAxis object axis,
 	 * regardless of whether this contains an axis identical
 	 * to (but with a different memory location from) axis.
 	 *
 	 * @param axis
 	 * 		the {@link org.bodytrack.client.GraphAxis GraphAxis}
 	 * 		for which to look
 	 * @return
 	 * 		<tt>true</tt> iff this GraphWidget holds a reference
 	 * 		to axis
 	 */
 	public boolean refersToAxis(GraphAxis axis) {
 		return xAxes.contains(axis) || yAxes.contains(axis);
 	}
 	
 	/**
 	 * Adds plot to the list of data plots to be drawn.
 	 * 
 	 * Note that a plot can only be added once to this internal list.
 	 * 
 	 * @param plot
 	 * 		the plot to add to the list of plots to be drawn
 	 */
 	public void addDataPlot(DataPlot plot) {
 		if (! dataPlots.contains(plot))
 			dataPlots.add(plot);
 
 		// TODO: Check for bug if the same axis is both an X-axis
 		// and a Y-axis, which should never happen in reality
 
 		if (! xAxes.contains(plot.getXAxis()))
 			xAxes.add(plot.getXAxis());
 
 		if (! yAxes.contains(plot.getYAxis()))
 			yAxes.add(plot.getYAxis());
 	}
 	
 	/**
 	 * Removes plot from the list of data plots to be drawn.
 	 * 
 	 * @param plot
 	 * 		the plot to remove from the list of plots to be drawn
 	 */
 	public void removeDataPlot(DataPlot plot) {
 		// TODO: implement this, removing plot from dataPlots and
 		// removing the axes of plot if and only if those axes are
 		// not referenced by any other DataPlot
 		throw new UnsupportedOperationException(
 			"Plot removal not currently implemented");
 	}
 }
