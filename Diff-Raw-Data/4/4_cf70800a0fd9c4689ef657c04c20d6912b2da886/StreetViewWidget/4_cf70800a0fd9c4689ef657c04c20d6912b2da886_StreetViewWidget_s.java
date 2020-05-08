 package com.scurab.web.drifmaps.client.widget;
 
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.maps.client.MapWidget;
 import com.google.gwt.maps.client.event.StreetviewInitializedHandler;
 import com.google.gwt.maps.client.event.StreetviewPitchChangedHandler;
 import com.google.gwt.maps.client.event.StreetviewYawChangedHandler;
 import com.google.gwt.maps.client.event.StreetviewZoomChangedHandler;
 import com.google.gwt.maps.client.geom.LatLng;
 import com.google.gwt.maps.client.overlay.Polygon;
 import com.google.gwt.maps.client.streetview.LatLngStreetviewCallback;
 import com.google.gwt.maps.client.streetview.Pov;
 import com.google.gwt.maps.client.streetview.StreetviewClient;
 import com.google.gwt.maps.client.streetview.StreetviewPanoramaOptions;
 import com.google.gwt.maps.client.streetview.StreetviewPanoramaWidget;
 import com.google.gwt.user.client.Timer;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.HasValue;
 import com.scurab.web.drifmaps.shared.utils.AppUtils;
 
 public class StreetViewWidget extends Composite implements HasValue<String>
 {
 	private final StreetviewPanoramaWidget panorama;
 	private final StreetviewClient svClient;
 
 //	private Pov currentPov = Pov.newInstance();
 	private Polygon viewPolygon;
 	
 	private double lastPitch = 0;
 	private double lastYaw = 0;
 	private double lastZoom = 0;
 	private boolean moved = false;
 	private Pov currentPov = null;
 	private LatLng currentLatLng = null;
 	
 
 	public StreetViewWidget(LatLng position)
 	{
 		this(position,false);
 	}
 	
 	
 	public StreetViewWidget(final boolean visible)
 	{
 		this(null,visible);
 	}
 	public StreetViewWidget(LatLng position, final boolean visible)
 	{
 		StreetviewPanoramaOptions options = StreetviewPanoramaOptions.newInstance();
 		if(position != null)
 			options.setLatLng(position);
 		svClient = new StreetviewClient();
 		panorama = new StreetviewPanoramaWidget(options);
 		panorama.setSize("320px", "300px");
 
 		panorama.addInitializedHandler(new StreetviewInitializedHandler()
 		{
 			@Override
 			public void onInitialized(StreetviewInitializedEvent event)
 			{
 //				if(!visible)
 //					panorama.hide();
 			}
 		});		
 
 		panorama.addPitchChangedHandler(new StreetviewPitchChangedHandler()
 		{
 			@Override
 			public void onPitchChanged(StreetviewPitchChangedEvent event)
 			{			
 				double pitch = event.getPitch();
 				if(lastPitch == pitch || panorama.isHidden()) //check for some change
 					return;
 				currentPov.setPitch(pitch);
 				currentLatLng = event.getSender().getLatLng();
 				lastPitch = pitch;
 //				panorama.getPov().setPitch(pitch);
 				updatePolyline();
 				ValueChangeEvent.fire(StreetViewWidget.this, getValue());
 			}
 		});
 
 		panorama.addYawChangedHandler(new StreetviewYawChangedHandler()
 		{
 			@Override
 			public void onYawChanged(StreetviewYawChangedEvent event)
 			{	
 				double yaw = event.getYaw();
 				if(lastYaw == yaw ||panorama.isHidden())
 					return;
 				lastYaw = yaw;
 				currentPov.setYaw(yaw);
 				currentLatLng = event.getSender().getLatLng();
 //				panorama.getPov().setYaw(yaw);
 				updatePolyline();
 				ValueChangeEvent.fire(StreetViewWidget.this, getValue());
 			}
 		});
 		
 		panorama.addZoomChangedHandler(new StreetviewZoomChangedHandler()
 		{
 			@Override
 			public void onZoomChanged(StreetviewZoomChangedEvent event)
 			{				
 				double zoom = event.getZoom();
 				//don't check zoom, because if there is only move all handlers are called, 
 				//so let it be handled by this way
 				if(panorama.isHidden()) 
 					return;
 				
 				currentLatLng = event.getSender().getLatLng();
 				currentPov.setZoom(zoom);
 				lastZoom = zoom;
 //				panorama.getPov().setZoom(zoom);
 				updatePolyline();
 				ValueChangeEvent.fire(StreetViewWidget.this, getValue());
 			}
 		});
 		
 		initWidget(panorama);		
 	}
 	
 	public void show()
 	{
 		panorama.show();
 	}
 	
 	public void hide()
 	{
 		hide(true);
 	}
 	public void hide(boolean reset)
 	{
 		panorama.hide();		
 		if(reset)
 		{
 			resetLastValues();
 			if (viewPolygon != null)
 			{
 				mapViewport.removeOverlay(viewPolygon);
 				viewPolygon = null;
 			}
 		}
 	}
 	
 	private void resetLastValues()
 	{
 		lastPitch = 0;
 		lastYaw = 0;
 		lastZoom = 0;
 //		currentPov = Pov.newInstance();
 	}
 	
 	/**
 	 * Sets location
 	 * @param point
 	 */
 	public void setLocation(final LatLng point)
 	{
 		resetLastValues();
 //		panorama.setLocationAndPov(latLng, currentPov);	
 //		Window.alert("set");
 //		LatLng point = event.getLatLng() == null ? event.getOverlayLatLng() : event.getLatLng();
 		if (point != null)
 		{
 			svClient.getNearestPanoramaLatLng(point, new LatLngStreetviewCallback()
 			{
 				@Override
 				public void onFailure()
 				{
 					panorama.hide();
 					if (viewPolygon != null)
 					{
 						mapViewport.removeOverlay(viewPolygon);
 						viewPolygon = null;
 					}
 				}
 
 				@Override
 				public void onSuccess(LatLng result)
 				{
 					double distance = distance(point, result)*1000;
 					if(distance < 10)//max length from point
 					{
 						currentLatLng = LatLng.newInstance(result.getLatitude(), result.getLongitude());
 						currentPov = Pov.newInstance();
 						panorama.setLocationAndPov(currentLatLng, currentPov);
 						updatePolyline();
 //						panorama.setLocationAndPov(result, currentPov);
 //						if(panorama.isHidden())
 //						{
 //							show();
 //							updatePolyline();
 //						}
 //						else
 //						{
 ////							panorama.setPov(currentPov);
 //							updatePolyline();
 //						}
 					}
 					else
 						onFailure();
 				}
 			});
 		}
 	}
 	
 	private double distance(LatLng ll1, LatLng ll2)
 	{		
 		double lat1 = ll1.getLatitude();
 		double lon1 = ll1.getLongitude();
 		double lat2 = ll2.getLatitude();
 		double lon2 = ll2.getLongitude();
 		char unit = 'K';
 		double theta = lon1 - lon2;
 		double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2))
 				* Math.cos(deg2rad(theta));
 		dist = Math.acos(dist);
 		dist = rad2deg(dist);
 		dist = dist * 60 * 1.1515;
 		if (unit == 'K')
 		{
 			dist = dist * 1.609344;
 		} else if (unit == 'N')
 		{
 			dist = dist * 0.8684;
 		}
 		return (dist);
 	}
 
 	private double deg2rad(double deg)
 	{
 		return (deg * Math.PI / 180.0);
 	}
 
 	private double rad2deg(double rad)
 	{
 		return (rad * 180.0 / Math.PI);
 	}
 	
 	private MapWidget mapViewport = null;
 	public void setViewportMap(MapWidget map)
 	{
 		mapViewport = map;
 	}
 
 	protected void updatePolyline()
 	{
 		//LatLng currentLatLng = panorama.getLatLng();
 		if(mapViewport == null)
 			return;
 		
 		if (viewPolygon != null)
 			mapViewport.removeOverlay(viewPolygon);
 		
 		// Some simple math to calculate viewPolygon
 		//Pov currentPov = panorama.getPov();
 		double yaw = currentPov.getYaw();
 		double distanceFactor = Math.cos(Math.toRadians(currentPov.getPitch())) * 0.0015 + 0.0005;
 		double zoomFactor = currentPov.getZoom() * 0.7 + 1;
 
 		LatLng[] points = new LatLng[11];
 		points[0] = points[10] = currentLatLng;
 		for (int i = 1; i < 10; i++)
 		{
 			double angle = Math.toRadians(yaw + (i - 5) * 7d / zoomFactor);
 			points[i] = LatLng.newInstance(currentLatLng.getLatitude() + Math.cos(angle) * distanceFactor, currentLatLng.getLongitude()
 					+ Math.sin(angle) * distanceFactor);
 		}
 
 		viewPolygon = new Polygon(points, "blue", 1, 0.5, "blue", 0.15);
 		mapViewport.addOverlay(viewPolygon);
 	}
 
 	@Override
 	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<String> handler)
 	{
 		return addHandler(handler, ValueChangeEvent.getType());
 	}
 
 	@Override
 	public String getValue()
 	{
 		if(panorama.isHidden())
 			return null;		
 		return transformLocationToString();
 	}
 
 	@Override
 	public void setValue(String value)
 	{
 		setValue(value,false);
 	}
 
 	@Override
 	public void setValue(String value, boolean fireEvents)
 	{
 		setPanorama(value);
 		if(fireEvents)
 			ValueChangeEvent.fire(this, value);
 	}
 	
 	private static final String VALUE_SEPARATOR = "=";
 	private static final String ITEM_SEPARATOR = ";";
 	private static final String X = "X";
 	private static final String Y = "Y";
 	private static final String YAW = "YAW";
 	private static final String PITCH = "PITCH";
 	private static final String ZOOM = "ZOOM";
 	
 	private String transformLocationToString()
 	{
 		StringBuilder sb = new StringBuilder();
 		sb.append(X + VALUE_SEPARATOR + AppUtils.round(currentLatLng.getLongitude(),6) + ITEM_SEPARATOR);
 		sb.append(Y + VALUE_SEPARATOR + AppUtils.round(currentLatLng.getLatitude(),6) + ITEM_SEPARATOR);
 		sb.append(YAW + VALUE_SEPARATOR + AppUtils.round(currentPov.getYaw(),6) + ITEM_SEPARATOR);
 		sb.append(PITCH + VALUE_SEPARATOR + AppUtils.round(currentPov.getPitch(),6) + ITEM_SEPARATOR);
 		sb.append(ZOOM + VALUE_SEPARATOR + AppUtils.round(currentPov.getZoom(),6));
 		return sb.toString();
 	}
 	
 	private void setPanorama(String link)
 	{
 		if(link == null || link.length() == 0)
 			return;
 		String[] data = link.split("\\" + ITEM_SEPARATOR);
 		double x = 0;
 		double y = 0;
 		double yaw = 0;
 		double pitch = 0;
 		double zoom = 0;
 		
 		for(String item : data)
 		{
 			try
 			{
 				String[] values = item.split(VALUE_SEPARATOR);
 				String key = values[0];
 				double value = Double.parseDouble(values[1]);
 				if(key.equals(X))
 					x = value;
 				else if(key.equals(Y))
 					y = value;
 				else if(key.equals(YAW))
 					yaw = value;
 				else if(key.equals(PITCH))
 					pitch = value;
 				else if(key.equals(ZOOM))
 					zoom = value;
 			}
 			catch(Exception e)
 			{
 				//should not never be thrown
 			}
 		}
 		
 		//final LatLng latLng = LatLng.newInstance(y, x);
 		currentLatLng = LatLng.newInstance(y, x);
 		currentPov = Pov.newInstance();
 		currentPov.setPitch(pitch);
 		currentPov.setYaw(yaw);
 		currentPov.setZoom(zoom);
 		show();
 		panorama.setLocationAndPov(currentLatLng, currentPov);
 		Timer t = new Timer()
 		{
 			@Override
 			public void run()
 			{
 //				panorama.setLocationAndPov(latLng, currentPov);
 			}
 		};
 		t.schedule(2500);
 	}
 }
