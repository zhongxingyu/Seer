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
 
 	public void addLayer(Layer layer) {
 		addComponent(layer);
 	}
 
 	@Override
 	public void removeComponent(Component c) {
 		super.removeComponent(c);
 		layers.remove(c);
 		setDirty("components");
 	}
 
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
 
 	public void setZoom(int zoomLevel) {
 		this.zoom = zoomLevel;
 		setDirty("zoom");
 	}
 
 	private HashSet<String> dirtyFields = new HashSet<String>();
 	private boolean fullRepaint = true;
 	private double top;
 	private double right;
 	private double bottom;
 	private double left;
 
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
 		if (isDirty("clat")) {
 			target.addAttribute("clon", centerLon);
 			target.addAttribute("clat", centerLat);
 		}
 		if (isDirty("zoom")) {
 			target.addAttribute("zoom", zoom);
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
 
 }
