 package ru.exorg.processing;
 
 import org.springframework.beans.factory.InitializingBean;
 import ru.exorg.core.model.City;
 import ru.exorg.core.model.Location;
 import ru.exorg.core.model.POI;
 import ru.exorg.core.service.CafeProvider;
 import ru.exorg.core.service.DataProvider;
 import ru.exorg.core.service.POIProvider;
 
 import java.lang.Exception;
 import java.lang.String;
 import java.net.SocketTimeoutException;
 import java.util.*;
 
 // ================================================================================
 
 final public class Main implements InitializingBean {
     private DataProvider dataProvider;
     private POIProvider poiProvider;
     private CafeProvider cafeProvider;
 
     private GeoService geoService;
     private Clustering clustering;
 
     private List<POI> pois;
 
     private int clusterLevel = 1;
     private double distLim = 10000;
     
 
     public void setGeoService(GeoService gs) {
         this.geoService = gs;
     }
 
     public void setDataProvider(DataProvider p) {
         this.dataProvider = p;
         this.poiProvider = p.getPOIProvider();
         this.cafeProvider = p.getCafeProvider();
     }
 
     public void setClusteringService(Clustering c) {
         this.clustering = c;
     }
 
 
     public void setClusterLevel(int cl) {
         this.clusterLevel = cl;
     }
 
 
     private void addGeoInfo(POI poi) throws Exception {
         if (!poi.getLocation().isValid()) {
             if (poi.hasAddress()) {
                 System.out.println("Quering for " + poi.getLocation().getAddress() + " (" + poi.getName() + ")");
             } else {
                 System.out.println("Quering for " + poi.getName());
             }
 
             List<Location> locs = this.geoService.lookupLocation(poi.getLocation(), poi.getName());
             if (locs != null) {
                 for (Location loc : locs) {
                     if (this.dataProvider.isWithinCity(poi.getLocation().getCityId(), loc)) {
                         double lat = loc.getLat();
                         double lng = loc.getLng();
 
                         poi.getLocation().setAddress(loc.getAddress());
                         poi.getLocation().setLat(lat);
                         poi.getLocation().setLng(lng);
 
                         City c = this.dataProvider.queryCity(poi.getCityId());
                         int sqId = (int)(Math.abs(lat - c.getNeLatLng().getLat())/c.getLngSubdivLen()*c.getLatSubdivs()
                                 +
                                 Math.abs(lng - c.getSwLatLng().getLng())/c.getLatSubdivLen() + 1);
 
                         poi.setSquareId(sqId);
                         break;
                     }
                 }
             }
 
             if (!poi.getLocation().isValid()) {
                 System.out.println("Failed");
             }
 
             Thread.sleep(500);
         }
     }
 
     private void guessType(POI poi) throws Exception {
         if (!poi.hasType()) {
             dataProvider.guessPOIType(poi);
         }
     }
 
     private void clusterize1() {
         for (POI poi : this.pois) {
             if (poi.hasAddress()) {
                 List<POI> poiList = this.poiProvider.queryByAddress(poi.getAddress());
 
                 if (poiList != null) {
                     if (!this.clustering.isInCluster(poiList.get(0)) && poiList.size() >= 2) {
                         long cid = this.clustering.getMaxClusterId() + 1;
                         for (POI p : poiList) {
                             this.clustering.setPOICluster(p, cid);
                         }
                     }
                 }
             }
         }
 
         Clustering.Clusters clusters = this.clustering.getClusters();
         for (List<Long> cluster : clusters.values()) {
             for (int i = 0; i < cluster.size(); i++) {
                 boolean remove = true;
                 POI curPOI = this.poiProvider.queryById(cluster.get(i));
                 String cur = curPOI.getName();
 
                 for (int j = i + 1; j < cluster.size(); j++) {
                     String other = this.poiProvider.queryById(cluster.get(j)).getName();
 
                     if ((float)Util.getLevenshteinDistance(cur, other)/Math.max(cur.length(), other.length()) < 0.6) {
                         remove = false;
                         break;
                     }
                 }
 
                 if (remove) {
                     this.clustering.removeFromCluster(curPOI);
                 }
             }
         }
     }
 
     /*
     private void clusterize2() {
         for (POI cur : this.pois) {
             float m = 1;
             POI nearest = null;
 
             for (POI other : this.pois) {
                 if (other.getId() != cur.getId()) {
                     String n1 = cur.getName();
                     String n2 = other.getName();
 
                     float cm = (float)Util.getLevenshteinDistance(n1, n2)/Math.max(n1.length(), n2.length());
                     if (cm < 0.1 && cm < m) {
                         m = cm;
                         nearest = other;
                         //cid = this.poiProvider.getPOICluster(other);
                     }
 
                     if (cm > 1) {
                         System.out.println("Nonsence, edit distance is " + String.valueOf(cm));
                     }
                 }
             }
 
             if (nearest != null) {
                 long cid = this.poiProvider.getPOICluster(nearest);
                 if (cid != 0) {
                     this.poiProvider.setPOICluster(cur, cid);
                 } else {
                     cid = this.poiProvider.getMaxClusterId() + 1;
                     this.poiProvider.setPOICluster(cur, cid);
                     this.poiProvider.setPOICluster(nearest, cid);
 
                     System.out.println("Adding POI #" + String.valueOf(nearest.getId()) + " into cluster #" + String.valueOf(cid));
                 }
 
                 System.out.println("Adding POI #" + String.valueOf(cur.getId()) + " into cluster #" + String.valueOf(cid) + "; min edit distance is " + String.valueOf(m));
             } else {
                 System.out.println("Not clustering POI #" + String.valueOf(cur.getId()));
             }
         }
     }
 
     private static final double R = 6356800;
 
     private double rad(double d) {
         return d*Math.PI/180.0;
     }
 
 
     private void evalDistances() {
         System.out.println("Be patient, this will insert more than 100000 entries into your database :)");
 
         for (POI p1 : this.pois) {
             for (POI p2 : this.pois) {
                 if (p1.getLocation().isValid() && p2.getLocation().isValid()) {
                     double lat1 = rad(p1.getLocation().getLat());
                     double lng1 = rad(p1.getLocation().getLng());
                     double x1 = R*Math.cos(lat1)*Math.cos(lng1);
                     double y1 = R*Math.cos(lat1)*Math.sin(lng1);
                     double z1 = R*Math.sin(lat1);
 
                     double lat2 = rad(p2.getLocation().getLat());
                     double lng2 = rad(p2.getLocation().getLng());
                     double x2 = R*Math.cos(lat2)*Math.cos(lng2);
                     double y2 = R*Math.cos(lat2)*Math.sin(lng2);
                     double z2 = R*Math.sin(lat2);
 
                     double cosA = (x1*x2 + y1*y2 + z1*z2)/Math.sqrt((x1*x1 + y1*y1 + z1*z1)*(x2*x2 + y2*y2 + z2*z2));
                     double d = Math.acos(cosA)*R;
 
                     System.out.println(String.format("%f %f %f %f %f %f", x1, y1, x1, lat1, lng1, Math.acos(cosA)*180/Math.PI));
                     System.out.println(String.format("%f %f %f %f %f", x2, y2, x2, lat2, lng2));
 
                     if (d < this.distLim) {
                         this.poiProvider.setDistance(p1, p2, d);
                     }
                 }
             }
         }
     }
     */
 
     private void processPOI() throws Exception {
         this.clustering.clearClusters();
 
        
         for (POI poi : this.pois) {
             try {
                 this.addGeoInfo(poi);
                 this.guessType(poi);
 
                 this.poiProvider.sync(poi);
             } catch (SocketTimeoutException e) {
                 System.out.println("Failed to retrieve geographic information for " + poi.getName());
             }
         }
        
 
         if (this.clusterLevel >= 1) {
             this.clusterize1();
             this.clustering.commitClusters();
 
             this.pois = this.poiProvider.poiList();
 
             /*
             if (this.clusterLevel >= 2) {
                 this.clusterize2();
                 this.poiProvider.collapseClusters();
                 this.pois = this.poiProvider.poiList();
             }
             */
         }
 
         //this.evalDistances();
 
         /*
         for (POI poi : this.pois) {
             this.poiProvider.serializeDescriptionsAndPhotos(poi);
         }
         */
     }
 
     public void afterPropertiesSet() {
         try {
             this.pois = this.poiProvider.poiList();
 
             processPOI();
             //processCafes();
         } catch (Exception e) {
             System.out.println(e.toString());
             e.printStackTrace();
         }
     }
 }
