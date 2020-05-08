 package com.idamobile.map.yandex;
 
 import ru.yandex.yandexmapkit.MapController;
 import ru.yandex.yandexmapkit.utils.GeoPoint;
 import ru.yandex.yandexmapkit.utils.ScreenPoint;
 
 import com.idamobile.map.IGeoPoint;
 import com.idamobile.map.MapControllerBase;
 
 class MapControllerWrapper implements MapControllerBase {
 
    public static final int MAX_ZOOM_LEVELS = 15;

     private MapController mapController;
 
     public MapControllerWrapper(MapViewWrapper mapViewWrapper) {
         this.mapController = mapViewWrapper.getView().getMapController();
     }
 
     @Override
     public IGeoPoint getMapCenter() {
         return new UniversalGeoPoint(mapController.getMapCenter());
     }
 
     @Override
     public void setMapCenter(IGeoPoint center) {
         mapController.setPositionNoAnimationTo(new UniversalGeoPoint(center).createYandexPoint());
     }
 
     @Override
     public void animateTo(IGeoPoint center) {
         mapController.setPositionAnimationTo(new UniversalGeoPoint(center).createYandexPoint());
     }
 
     @Override
     public int getZoomLevel() {
         return (int) mapController.getZoomCurrent();
     }
 
     @Override
     public int getMaxZoomLevel() {
        return MAX_ZOOM_LEVELS;
     }
 
     @Override
     public void setZoomLevel(int zoomLevel) {
         mapController.setZoomCurrent(zoomLevel);
     }
 
     @Override
     public void zoomIn() {
         mapController.zoomIn();
     }
 
     @Override
     public void zoomOut() {
         mapController.zoomOut();
     }
 
     @Override
     public void zoomToSpan(IGeoPoint span) {
         setZoomLevel(getMaxZoomLevel());
         while (true) {
             int latSpan = getLatitudeSpan();
             int lngSpan = getLongitudeSpan();
             if ((latSpan > span.getLat() && lngSpan > span.getLng()) || getZoomLevel() == 1) {
                 break;
             }
             setZoomLevel(getZoomLevel() - 1);
         }
     }
 
     @Override
     public int getLatitudeSpan() {
         GeoPoint leftTopCorner = mapController.getGeoPoint(new ScreenPoint(0, 0));
         GeoPoint rightBottomCorner = mapController.getGeoPoint(
                 new ScreenPoint(mapController.getMapView().getWidth(), mapController.getMapView().getHeight()));
         return Math.abs(UniversalGeoPoint.fromDoubleTo1e6(rightBottomCorner.getLat() - leftTopCorner.getLat()));
     }
 
     @Override
     public int getLongitudeSpan() {
         GeoPoint leftTopCorner = mapController.getGeoPoint(new ScreenPoint(0, 0));
         GeoPoint rightBottomCorner = mapController.getGeoPoint(
                 new ScreenPoint(mapController.getMapView().getWidth(), mapController.getMapView().getHeight()));
         return Math.abs(UniversalGeoPoint.fromDoubleTo1e6(rightBottomCorner.getLon() - leftTopCorner.getLon()));
     }
 
 }
