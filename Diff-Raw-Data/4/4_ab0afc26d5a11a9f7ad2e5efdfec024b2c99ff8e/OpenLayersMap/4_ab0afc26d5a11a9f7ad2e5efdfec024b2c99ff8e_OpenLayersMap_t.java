 package org.vaadin.vol;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.vaadin.terminal.PaintException;
 import com.vaadin.terminal.PaintTarget;
 import com.vaadin.ui.AbstractComponentContainer;
 import com.vaadin.ui.Component;
 
 /**
  * Server side component for the VOpenLayersMap widget.
  */
 @SuppressWarnings("serial")
 @com.vaadin.ui.ClientWidget(org.vaadin.vol.client.ui.VOpenLayersMap.class)
 public class OpenLayersMap extends AbstractComponentContainer {
 
 	private List<Component> layers = new LinkedList<Component>();
 	private double centerLon = 0;
 	private double centerLat = 0;
 	private int zoom = 3;
 	private boolean partialRepaint;
 
 	public OpenLayersMap() {
 		setWidth("500px");
 		setHeight("350px");
 	}
 
 	/**
 	 * A typed alias for {@link #addComponent(Component)}.
 	 * 
 	 * @param layer
 	 */
 	public void addLayer(Layer layer) {
 		addComponent(layer);
 	}
 
 	@Override
 	public void removeComponent(Component c) {
 		super.removeComponent(c);
 		layers.remove(c);
 		setDirty("components");
 	}
 
 	/**
 	 * Adds component into the OpenLayers Map. Note that the map only supports
 	 * certain types of Components.
 	 * <p>
 	 * Developers are encouraged to use better typed methods instead:
 	 * 
 	 * @see #addLayer(Layer)
 	 * @see #addPopup(Popup)
 	 * 
 	 * @see com.vaadin.ui.AbstractComponentContainer#addComponent(com.vaadin.ui.Component)
 	 */
 	@Override
 	public void addComponent(Component c) {
 		setDirty("components");
 		super.addComponent(c);
 		layers.remove(c);
 		layers.add(c);
 	}
 
 	public void setCenter(double lon, double lan) {
 		this.centerLat = lan;
 		this.centerLon = lon;
 		setDirty("clat");
 		requestRepaint();
 	}
 
 	/**
 	 * Set the center of map to the center of a bounds 
 	 * 
 	 */
 	public void setCenter(Bounds bounds) {
 		this.centerLat = (bounds.getBottom() + bounds.getTop()) / 2.0;
 		this.centerLon = (bounds.getRight() + bounds.getLeft()) / 2.0;
 		setDirty("clat");
 		requestRepaint();
 	}
 	
 	public void setZoom(int zoomLevel) {
 		this.zoom = zoomLevel;
 		setDirty("zoom");
 	}
	
	public int getZoom() {
		return zoom;
	}
 
 	private HashSet<String> dirtyFields = new HashSet<String>();
 	private boolean fullRepaint = true;
 	private double top;
 	private double right;
 	private double bottom;
 	private double left;
 
 	private String jsMapOptions;
 	private Bounds zoomToExtent;
 	private Bounds restrictedExtend;
 
 	private void setDirty(String fieldName) {
 		if (!fullRepaint) {
 			dirtyFields.add(fieldName);
 			partialPaint();
 		}
 	}
 
 	private boolean isDirty(String fieldName) {
 		/*
 		 * If full repaint if request repaint called directly or painted without
 		 * repaint.
 		 */
 		if (fullRepaint || dirtyFields.isEmpty()) {
 			return true;
 		} else {
 			return dirtyFields.contains(fieldName);
 		}
 	}
 
 	@Override
 	public void paintContent(PaintTarget target) throws PaintException {
 		super.paintContent(target);
 		if (fullRepaint && jsMapOptions != null) {
 			target.addAttribute("jsMapOptions", jsMapOptions);
 		}
 
 		if (isDirty("restrictedExtend") && restrictedExtend != null) {
 			restrictedExtend.paint("re", target);
 		}
 
 		if (isDirty("zoomToExtent") && zoomToExtent != null) {
 			zoomToExtent.paint("ze", target);
 			zoomToExtent = null;
 		} else {
 			if (isDirty("clat")) {
 				target.addAttribute("clon", centerLon);
 				target.addAttribute("clat", centerLat);
 			}
 			if (isDirty("zoom")) {
 				target.addAttribute("zoom", zoom);
 			}
 		}
 		if (isDirty("components")) {
 			for (Component component : layers) {
 				component.paint(target);
 			}
 		}
 		clearPartialPaintFlags();
 		fullRepaint = false;
 	}
 
 	/**
 	 * Receive and handle events and other variable changes from the client.
 	 * 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void changeVariables(Object source, Map<String, Object> variables) {
 		super.changeVariables(source, variables);
 		if (variables.containsKey("top")) {
 			updateExtent(variables);
 		}
 	}
 
 	protected void updateExtent(Map<String, Object> variables) {
 		int zoom = (Integer) variables.get("zoom");
 		this.zoom = zoom;
 		top = (Double) variables.get("top");
 		right = (Double) variables.get("right");
 		bottom = (Double) variables.get("bottom");
 		left = (Double) variables.get("left");
 	}
 
 	/**
 	 * Note, this does not work until the map is rendered.
 	 * 
 	 * @return
 	 */
 	public Bounds getExtend() {
 		Bounds bounds = new Bounds();
 		bounds.setTop(top);
 		bounds.setLeft(left);
 		bounds.setRight(right);
 		bounds.setBottom(bottom);
 		return bounds;
 	}
 
 	public void replaceComponent(Component oldComponent, Component newComponent) {
 		throw new UnsupportedOperationException();
 	}
 
 	public Iterator<Component> getComponentIterator() {
 		return new LinkedList<Component>(layers).iterator();
 	}
 
 	public void addPopup(Popup popup) {
 		addComponent(popup);
 	}
 
 	@Override
 	public void requestRepaint() {
 		if (!partialRepaint) {
 			clearPartialPaintFlags();
 			fullRepaint = true;
 		}
 		super.requestRepaint();
 	}
 
 	private void partialPaint() {
 		partialRepaint = true;
 		try {
 			requestRepaint();
 		} finally {
 			partialRepaint = false;
 		}
 	}
 
 	private void clearPartialPaintFlags() {
 		dirtyFields.clear();
 	}
 
 	/**
 	 * Sets the js snippet that will be evaluated as maps custom options. See
 	 * OpenLayers JS api for more details.
 	 * <p>
 	 * Note, that the string will be executed as javascript on the client side.
 	 * VALIDATE content in case you accept input from the client.
 	 * <p>
 	 * Also note that init options only take effect if they are set before the
 	 * map gets rendered.
 	 * 
 	 * @param jsMapOptions
 	 */
 	public void setJsMapOptions(String jsMapOptions) {
 		this.jsMapOptions = jsMapOptions;
 	}
 
 	public String getJsMapOptions() {
 		return jsMapOptions;
 	}
 
 	/**
 	 * Zooms the map to display given bounds.
 	 * 
 	 * <p>
 	 * Note that this method overrides possibly set center and zoom levels.
 	 * 
 	 * @param bounds
 	 */
 	public void zoomToExtent(Bounds bounds) {
 		zoomToExtent = bounds;
 		setDirty("zoomToExtent");
 	}
 
 	/**
 	 * Sets the area within the panning and zooming is restricted. With this
 	 * method developer can "limit" the area that is shown for the end user.
 	 * <p>
 	 * Note, that due the fact that open layers supports just zoom levels, the
 	 * displayed area might be slightly larger if the size of the component
 	 * don't match with the size of restricted area on minimum zoom level. If
 	 * area outside restricted extent may not be displayed at all, one must
 	 * ensure about this by either using a base layer that only contains the
 	 * desired area or by "masking" out the undesired area with e.g. a vector
 	 * layer.
 	 * 
 	 * @param bounds
 	 */
 	public void setRestrictedExtent(Bounds bounds) {
 		restrictedExtend = bounds;
 		setDirty("restrictedExtend");
 	}
 
 }
