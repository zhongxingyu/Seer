 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.asascience.edc.dap.map;
 
 import com.asascience.edc.utils.WorldwindUtils;
 import gov.nasa.worldwind.geom.Position;
 import ucar.unidata.geoloc.LatLonRect;
 import gov.nasa.worldwind.geom.LatLon;
 import gov.nasa.worldwind.geom.Sector;
 import gov.nasa.worldwind.globes.Globe;
 import gov.nasa.worldwind.layers.RenderableLayer;
 import gov.nasa.worldwind.render.BasicShapeAttributes;
 import gov.nasa.worldwind.render.Box;
 import gov.nasa.worldwind.render.Material;
 import gov.nasa.worldwind.render.ShapeAttributes;
 import gov.nasa.worldwind.render.SurfacePolygon;
 import gov.nasa.worldwind.render.SurfaceSector;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author Kyle
  */
 public class DataExtentLayer extends RenderableLayer {
 
   private SurfacePolygon polygon;
   private SurfaceSector sector;
   private Box box;
   private Globe globe;
   private List<LatLon> positions = new ArrayList<LatLon>();
 
   public DataExtentLayer(Globe e) {
     this.globe = e;
     
     ShapeAttributes satts = new BasicShapeAttributes();
     satts.setOutlineMaterial(Material.RED);
     satts.setInteriorMaterial(Material.RED);
     satts.setOutlineOpacity(0.4);
     satts.setInteriorOpacity(0.4);
     
     this.polygon = new SurfacePolygon();
     this.polygon.setVisible(true);
     this.polygon.setAttributes(satts);
     
     this.sector = new SurfaceSector();
     this.sector.setVisible(true);
     this.sector.setAttributes(satts);
     
     this.box = new Box();
    this.box.setVisible(true);
    this.box.setAttributes(satts);
            
     addRenderable(polygon);
     addRenderable(sector);
     addRenderable(box);
   }
   
   public void setDataExtent(LatLonRect bbox) {
 
     LatLon ul = WorldwindUtils.normalizeLatLon(LatLon.fromDegrees(bbox.getUpperLeftPoint().getLatitude(), bbox.getUpperLeftPoint().getLongitude()));
     LatLon lr = WorldwindUtils.normalizeLatLon(LatLon.fromDegrees(bbox.getLowerRightPoint().getLatitude(), bbox.getLowerRightPoint().getLongitude()));
     
     if (bbox.crossDateline()) {
       
       if (bbox.getWidth() >= 359 || bbox.getUpperLeftPoint().equals(bbox.getUpperRightPoint())) {
         // Most likely a global coverage
         sector.setSector(Sector.FULL_SPHERE);
       } else {
         
         // This is an absolute MESS.  And doesn't work.
         
         List<LatLon> up = new ArrayList<LatLon>();
         List<LatLon> lp = new ArrayList<LatLon>();
         double width = bbox.getWidth();
         double width_drawn = 0;
         LatLon upperLeft = ul;
         LatLon lowerRight;
         double maxLat = ul.getLatitude().getDegrees();
         double minLat = lr.getLatitude().getDegrees();
         double degrees_to_draw = 0.1;
         while (width_drawn < width) {
           if (width - width_drawn < degrees_to_draw) {
             degrees_to_draw = width - width_drawn;
           }
           double nextLongitude = WorldwindUtils.normLon(upperLeft.getLongitude().getDegrees() + degrees_to_draw);
           lowerRight = LatLon.fromDegrees(minLat, nextLongitude);
           up.add(upperLeft);
           lp.add(lowerRight);
           upperLeft = LatLon.fromDegrees(maxLat, nextLongitude);
           width_drawn += degrees_to_draw;
         }
         positions.clear();
         positions.addAll(up);
         for (int i = lp.size() - 1; i >= 0; i--) {
           positions.add(lp.get(i));
         }
         //polygon.setLocations(positions);
         sector.setSector(Sector.boundingSector(positions));
       }
     } else {
       sector.setSector(Sector.boundingSector(ul, lr));
     }
   }
 
   public Position getEyePosition() {
     return WorldwindUtils.getEyePositionFromPositions(sector.getLocations(globe));
   }
 }
