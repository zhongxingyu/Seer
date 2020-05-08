 package teropa.globetrotter.client;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 
 import teropa.globetrotter.client.common.Bounds;
 import teropa.globetrotter.client.common.Calc;
 import teropa.globetrotter.client.common.Direction;
 import teropa.globetrotter.client.common.LonLat;
 import teropa.globetrotter.client.common.Point;
 import teropa.globetrotter.client.common.Position;
 import teropa.globetrotter.client.common.Rectangle;
 import teropa.globetrotter.client.common.Size;
 import teropa.globetrotter.client.controls.Control;
 import teropa.globetrotter.client.event.MapZoomedEvent;
 import teropa.globetrotter.client.event.internal.ViewPanEvent;
 import teropa.globetrotter.client.proj.Projection;
 
import com.google.gwt.event.shared.HandlerManager;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.AbsolutePanel;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.RequiresResize;
 import com.google.gwt.widgetideas.graphics.client.Color;
 import com.google.gwt.widgetideas.graphics.client.GWTCanvas;
 
 public class Map extends Composite implements ViewContext, ViewPanEvent.Handler, RequiresResize {
 
 	private final AbsolutePanel container = new AbsolutePanel();
	private HandlerManager viewHandlers = new HandlerManager(this);
	private View view = new View(this, viewHandlers);
 	private final Calc calc = new Calc(this);
 	private final List<Layer> layers = new ArrayList<Layer>();
 	private final LinkedHashMap<Control, Position> controls = new LinkedHashMap<Control, Position>();
 	private KeyboardControls keyboardControls;
 	private Layer baseLayer;
 	
 	private Bounds maxExtent = new Bounds(-180, -90, 180, 90);
 	private LonLat center = new LonLat(0, 0);
 	private double[] resolutions = new double[] { 1.0, 0.5, 0.2, 0.1, 0.05, 0.02, 0.01, 0.005 };
 	private int resolutionIndex = 4;
 	
 	private Size tileSize = new Size(256, 256);
 	
 	private Grid grid;
 	private Timer reinitTimer;
 	
 	public Map(String width, String height) {
 		initWidget(container);
 		container.add(view);
 		setWidth(width);
 		setHeight(height);
 		view.addViewPanHandler(this);
 		this.keyboardControls = new KeyboardControls(this);
 		DeferredCommand.addCommand(new Command() {
 			public void execute() {
 				init();
 			}
 		});
 	}
 	
 	private void init() {
 		Size fullSize = calc.getVirtualPixelSize();
 		Point centerPoint = calc.getPoint(center);
 		view.position(centerPoint);
 		getGrid().init(fullSize);
 		positionControls();
 	}
 	
 	public void onResize() {
 		container.remove(view);
 		grid = null;
		view = new View(this, viewHandlers);
 		container.add(view);
 		init();				
 	}
 
 	public void addLayer(Layer layer) {
 		layer.init(this);
 		layers.add(layer);
 		if (layer.isBase()) {
 			baseLayer = layer;
 		}
 	}
 	
 	public Layer getLayerByName(String name) {
 		for (Layer each : layers) {
 			if (name.equals(each.getName())) {
 				return each;
 			}
 		}
 		return null;
 	}
 	
 	public void removeLayer(String name) {
 		Layer theLayer = getLayerByName(name);
 		if (theLayer != null) {
 			layers.remove(theLayer);
 		}
 	}
 	
 	public void setCenter(LonLat center) {
 		this.center = center;
 	}
 	
 	public Projection getProjection() {
 		if (baseLayer != null) {
 			return baseLayer.getProjection();
 		} else {
 			return Projection.WGS_84;
 		}
 	}
 
 	public void setResolutions(double[] resolutions, int initialIndex) {
 		this.resolutions = resolutions;
 		this.resolutionIndex = initialIndex;
 	}
 	
 	public double[] getResolutions() {
 		return resolutions;
 	}
 	
 	public void zoomTo(int resolutionIndex) {
 		resizeView(resolutionIndex - this.resolutionIndex);
 	}
 	
 	public void zoomTo(int resolutionIndex, LonLat point) {
 		resizeView(resolutionIndex - this.resolutionIndex, point);
 	}
 
 	public int getResolutionIndex() {
 		return this.resolutionIndex;
 	}
 
 	public Size getTileSize() {
 		return tileSize;
 	}
 
 	public View getView() {
 		return view;
 	}
 	
 	public Bounds getMaxExtent() {
 		return maxExtent;
 	}
 	
 	public void setMaxExtent(Bounds maxExtent) {
 		this.maxExtent = maxExtent;
 	}
 	
 	public double getResolution() {
 		return resolutions[resolutionIndex];
 	}
 
 	public LonLat getCenter() {
 		return center;
 	}
 
 	public Point getViewCenterPoint() {
 		return calc.getPoint(getCenter());
 	}
 	
 	public Grid getGrid() {
 		if (grid == null) {
 			grid = new Grid(getTileSize(), this);
 		}
 		return grid;
 	}
 	
 	public Rectangle getVisibleRectangle() {
 		Size portSize = view.getVisibleSize();
 		Point topLeft = view.getTopLeft();
 		return new Rectangle(topLeft.getX(), topLeft.getY(), portSize.getWidth(), portSize.getHeight());
 	}
 	
 	public void zoomIn() {
 		resizeView(1);
 	}
 
 	public void zoomIn(LonLat newCenter) {
 		resizeView(1, newCenter);
 	}
 
 	public void zoomOut() {
 		resizeView(-1);
 	}
 
 	public void onViewPanned(ViewPanEvent event) {
 		setCenter(calc.getLonLat(event.newCenterPoint));
 	}
 
 	public void move(Direction dir, int amountPx) {
 		switch(dir) {
 		case RIGHT: view.move(amountPx, 0); break;
 		case DOWN: view.move(0, amountPx); break;
 		case LEFT: view.move(-amountPx, 0); break;
 		case UP: view.move(0, -amountPx); break;
 		}
 	}
 	
 	public void setKeyboardControlsEnabled(boolean enabled) {
 		if (enabled && keyboardControls == null) {
 			keyboardControls = new KeyboardControls(this);
 		} else if (!enabled && keyboardControls != null) {
 			keyboardControls.destroy();
 			keyboardControls = null;
 		}
 	}
 
 	private boolean newResolutionInBounds(int delta) {
 		if (delta > 0) {
 			return resolutionIndex + delta < resolutions.length;
 		} else {
 			return resolutionIndex + delta >= 0;
 		}
 	}
 	
 	private void resizeView(int delta) {
 		resizeView(delta, center);
 	}
 	
 	private void resizeView(int delta, LonLat newCenter) {
 		int prevResolutionIndex = resolutionIndex;
 		LonLat prevCenter = center;
 		if (newResolutionInBounds(delta)) {
 			resolutionIndex += delta;
 		}
 		setCenter(newCenter);
 		adjustView(resolutions[prevResolutionIndex], resolutions[resolutionIndex], prevCenter, newCenter);
 		fireEvent(new MapZoomedEvent());
 	}
 
 	private void adjustView(double fromRes, double toRes, LonLat fromCenter, LonLat toCenter) {
 		if (reinitTimer != null) reinitTimer.cancel();
 		zoomEffect(fromRes, toRes, fromCenter, toCenter);
 		reinitTimer = new Timer() {
 			public void run() {
 				reinitGrid();				
 				reinitTimer = null;
 			}
 		};
 		reinitTimer.schedule(10);
 	}
 
 	private void zoomEffect(double fromRes, double toRes, LonLat fromCenter, LonLat toCenter) {
 		final double resScale = fromRes / toRes;
 		
 		double width = getView().getVisibleSize().getWidth();
 		double height = getView().getVisibleSize().getHeight();
 
 		Point fromCenterPoint = calc().getPoint(fromCenter);
 		Point toCenterPoint = calc().getPoint(toCenter);
 		double xMove = fromCenterPoint.getX() - toCenterPoint.getX();
 		double yMove = fromCenterPoint.getY() - toCenterPoint.getY();
 		
 		view.getCanvas().saveContext();		
 		if (resScale >= 1) {
 			view.getCanvas().translate(
 					-view.getTopLeft().getX() - width / resScale + xMove,
 					-view.getTopLeft().getY() - height / resScale + yMove);		
 			view.getCanvas().scale(resScale, resScale);
 		} else {
 			view.getCanvas().scale(resScale, resScale);
 			view.getCanvas().translate(
 					view.getTopLeft().getX() + width - width * resScale,
 					view.getTopLeft().getY() + height - height * resScale);
 		}
 		view.draw(true);
 		view.getCanvas().restoreContext();
 	}
 
 	private void reinitGrid() {
 		Size fullSize = calc.getVirtualPixelSize();
 		Point centerPoint = calc.getPoint(center);
 		view.position(centerPoint);
 		clearAreasOutsideGrid(fullSize, centerPoint);
 		getGrid().init(fullSize);
 		view.draw(false);
 	}
 
 	private void clearAreasOutsideGrid(Size fullSize, Point centerPoint) {
 		Size viewSize = view.getVisibleSize();
 		Point topLeft = view.getTopLeft();
 		
 		GWTCanvas canvas = view.getCanvas();
 		canvas.saveContext();
 		canvas.setFillStyle(Color.WHITE);
 		
 		int heightOutside = viewSize.getHeight() - fullSize.getHeight();
 		int heightTop = -topLeft.getY();
 		int heightBottom = viewSize.getHeight() - (-topLeft.getY() + fullSize.getHeight());
 		if (heightOutside > 0) {
 			canvas.fillRect(topLeft.getX(), topLeft.getY(), viewSize.getWidth(), heightTop);
 			canvas.fillRect(topLeft.getX(), topLeft.getY() + viewSize.getHeight() - heightBottom, viewSize.getWidth(), heightBottom);
 		}
 		
 		int widthOutside = viewSize.getWidth() - fullSize.getWidth();
 		int widthLeft = -topLeft.getX();
 		int widthRight = viewSize.getWidth() - (-topLeft.getX() + fullSize.getWidth());
 		if (widthOutside > 0) {
 			canvas.fillRect(topLeft.getX(), topLeft.getY(), widthLeft, viewSize.getHeight());
 			canvas.fillRect(topLeft.getX() + viewSize.getWidth() - widthRight, topLeft.getY(), widthRight, viewSize.getHeight());
 		}
 		canvas.restoreContext();
 	}
 
 	public void addControl(Control control, Position at) {
 		control.init(this);
 		controls.put(control, at);
 		positionControl(control, at);
 	}
 
 	private void positionControls() {
 		for (Control each : controls.keySet()) {
 			positionControl(each, controls.get(each));
 		}
 	}
 
 	private void positionControl(Control control, Position at) {
 		switch (at) {
 		case TOP_LEFT: container.add(control.asWidget(), 10, 10); break;
 		case MIDDLE_LEFT: container.add(control.asWidget(), 10, 100); break;
 		case BOTTOM_LEFT: container.add(control.asWidget(), 10, getOffsetHeight() - 10 - control.asWidget().getOffsetHeight()); break;
 		}
 	}
 
 	public List<Layer> getLayers() {
 		return layers;
 	}
 
 	public Calc calc() {
 		return calc;
 	}
 	
 	public void addMapZoomedHandler(MapZoomedEvent.Handler handler) {
 		addHandler(handler, MapZoomedEvent.TYPE);
 	}
 
 }
 
