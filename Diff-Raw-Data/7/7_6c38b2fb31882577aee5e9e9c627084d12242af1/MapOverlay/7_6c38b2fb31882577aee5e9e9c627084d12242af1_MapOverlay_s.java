 package org.vaadin.vol.client.wrappers;
 
 import org.vaadin.vol.client.wrappers.control.Control;
 import org.vaadin.vol.client.wrappers.layer.Layer;
 import org.vaadin.vol.client.wrappers.popup.Popup;
 
 import com.google.gwt.core.client.JavaScriptObject;
 
 public class MapOverlay extends AbstractOpenLayersWrapper {
 
 	protected MapOverlay() {
 	}
 
 	/**
 	 * TODO parametrice options
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public static native MapOverlay get(String id)
 	/*-{

		
		// This is needed to overcome infamous javascript array detection issue with iframe borders
		if(!$wnd.toOlArray) {
			$wnd.eval("toOlArray = function(a) { var ra = []; for(var i = 0; i < a.length; i++) { ra.push(a[i]);}return ra;};");
		}

 	            var options = {
 	            // for only map tiler base layer, use these
 	//	                projection: new $wnd.OpenLayers.Projection("EPSG:900913"),
 	//	                displayProjection: new $wnd.OpenLayers.Projection("EPSG:4326"),
 	//	                units: "m",
 	//	                maxResolution: 156543.0339,
 	//	                maxExtent: new $wnd.OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508.34)
 		            };
 
 		return new $wnd.OpenLayers.Map(id, options);
 	}-*/;
 
 	public final native void addControl(Control control)
 	/*-{
 		this.addControl(control);
 	}-*/;
 
 	public final native void addLayer(Layer layer)
 	/*-{
 		this.addLayer(layer);
 	}-*/;
 
 	public final native void setCenter(LonLat lonLat, int zoom)
 	/*-{
 		this.setCenter(lonLat,zoom);
 	}-*/;
 
 	public final native Projection getProjection()
 	/*-{
 		return this.getProjectionObject();
 	}-*/;
 
 	public final native void removeLayer(Layer remove)
 	/*-{
 		this.removeLayer(remove);
 	}-*/;
 
 	public final native void addPopup(Popup popup)
 	/*-{
 		this.addPopup(popup);
 	}-*/;
 
 	public final native void removePopup(Popup popup)
 	/*-{
 		this.removePopup(popup);
 	}-*/;
 
 	public final native Layer getLayer(String id)
 	/*-{
 		return this.getLayer(id);
 	}-*/;
 
 	public final native void removeContol(Control control)
 	/*-{
 		this.removeControl(control);
 	}-*/;
 
 	public final native Bounds getMaxExtent()
 	/*-{
 		return this.getMaxExtent();
 	}-*/;
 
 	public final native int getZoom()
 	/*-{
 		return this.getZoom();
 	}-*/;
 
 	public final native void zoomTo(int zoom)
 	/*-{
 		this.zoomTo(zoom);
 	}-*/;
 
 	public final native Bounds getExtent() 
 	/*-{
 		return this.getExtent();
 	}-*/;
 
 }
