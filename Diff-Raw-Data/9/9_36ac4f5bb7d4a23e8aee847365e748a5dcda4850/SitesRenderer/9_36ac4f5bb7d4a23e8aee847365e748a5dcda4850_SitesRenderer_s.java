 package org.amanzi.awe.render.sites;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.geom.AffineTransform;
 import java.io.IOException;
 import java.util.Map;
 
 import net.refractions.udig.catalog.IGeoResource;
 import net.refractions.udig.project.ILayer;
 import net.refractions.udig.project.internal.render.impl.RendererImpl;
 import net.refractions.udig.project.render.RenderException;
 import net.sf.json.JSONArray;
 import net.sf.json.JSONObject;
 
 import org.amanzi.awe.catalog.csv.CSV;
 import org.amanzi.awe.catalog.json.JSONReader;
 import org.amanzi.awe.catalog.json.JSONReader.Feature;
 import org.amanzi.awe.catalog.json.JSONReader.FeatureReader;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.geotools.geometry.jts.JTS;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.MathTransform;
 import org.opengis.referencing.operation.TransformException;
 
 import com.csvreader.CsvReader;
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Envelope;
 import com.vividsolutions.jts.geom.Point;
 
 public class SitesRenderer extends RendererImpl {
     private AffineTransform base_transform = null;  // save original graphics transform for repeated re-use
     private Color drawColor = Color.DARK_GRAY;
     private Color fillColor = new Color(120, 255, 170);
     private MathTransform transform_d2w;
     private MathTransform transform_w2d;
 
    private void setCrsTransforms(CoordinateReferenceSystem dataCrs, boolean lenient) throws FactoryException{
         CoordinateReferenceSystem worldCrs = context.getCRS();
         this.transform_d2w = CRS.findMathTransform(dataCrs, worldCrs, lenient);
         this.transform_w2d = CRS.findMathTransform(worldCrs, dataCrs, lenient); // could use transform_d2w.inverse() also
     }
 
     private Envelope getTransformedBounds() throws TransformException{
         ReferencedEnvelope bounds = getRenderBounds();
         Envelope bounds_transformed = null;
         if (bounds != null && transform_w2d != null) {
             bounds_transformed = JTS.transform(bounds, transform_w2d);
         }
         return bounds_transformed;
     }
 
     /**
      * This method is called to render what it can. It is passed a graphics context
      * with which it can draw. The class already contains a reference to a RenderContext
      * from which it can obtain the layer and the GeoResource to render.
      * @see net.refractions.udig.project.internal.render.impl.RendererImpl#render(java.awt.Graphics2D, org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     public void render( Graphics2D g, IProgressMonitor monitor ) throws RenderException {
         ILayer layer = getContext().getLayer();
         // Are there any resources in the layer that respond to the CSV class (should be the case if we added any *.csv files)
         // See CSVGeoResource class in csv plugin
         IGeoResource resource = layer.findGeoResource(JSONReader.class);
         if(resource == null){
             resource = layer.findGeoResource(CSV.class);
             if (resource != null){
                 renderCSV(g,resource,monitor);
             }
         }else{
             renderJSON(g,resource,monitor);
         }
     }
 
     /**
      * This method is called to render data from the basic CSV geo resource.
      */
     private void renderCSV( Graphics2D g, IGeoResource csvGeoResource, IProgressMonitor monitor ) throws RenderException {
         if (monitor == null)
             monitor = new NullProgressMonitor();
         monitor.beginTask("render network sites and sectors", IProgressMonitor.UNKNOWN);
 
         CsvReader reader = null;
         try {
             monitor.subTask("connecting");
             CSV csv = csvGeoResource.resolve(CSV.class, new SubProgressMonitor(monitor, 10));
 
             reader = csv.reader();
             reader.readHeaders();
 
             g.setColor(drawColor);
 
            setCrsTransforms(csvGeoResource.getInfo(null).getCRS(), false);
             Envelope bounds_transformed = getTransformedBounds();
 
             int count = 0;
             monitor.subTask("drawing");
             int nameIndex = reader.getIndex("name");
             Coordinate world_location = new Coordinate(); // single object for re-use in transform below (minimize object creation)
             while( reader.readRecord() ) {
                 Point point = CSV.getPoint(reader);
                 Coordinate location = point.getCoordinate();
 
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     //JTS.transform(location, world_location, transform_w2d.inverse());
                 }
 
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 renderSite(g, p);
                 try {
                     for( int s = 0; s < 3; s++ ) {
                         double azimuth = (10.0 * count) - (120.0 * s);
                         renderSector(g, p, azimuth, 60.0);
                     }
                 } finally {
                     if (base_transform != null) {
                         // recover the normal transform
                         g.setTransform(base_transform);
                         g.setColor(drawColor);
                     }
                 }
                 g.drawString(reader.get(nameIndex), p.x + 10, p.y + 10);
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
             }
         } catch (TransformException e) {
             throw new RenderException(e);
         } catch (FactoryException e) {
             throw new RenderException(e);
         } catch (IOException e) {
             throw new RenderException(e); // rethrow any exceptions encountered
         } finally {
             if (reader != null)
                 reader.close();
             monitor.done();
         }
     }
 
     /**
      * This method is called to render data from the JSON Geo-Resource.
      */
     private void renderJSON( Graphics2D g, IGeoResource jsonGeoResource, IProgressMonitor monitor ) throws RenderException {
         if (monitor == null)
             monitor = new NullProgressMonitor();
         monitor.beginTask("render network sites and sectors", IProgressMonitor.UNKNOWN);    // TODO: Get size from info
 
         JSONReader jsonReader = null;
         try {
             monitor.subTask("connecting");
             jsonReader = jsonGeoResource.resolve(JSONReader.class, new SubProgressMonitor(monitor, 10));
 
            setCrsTransforms(jsonGeoResource.getInfo(null).getCRS(), false);
             Envelope bounds_transformed = getTransformedBounds();
 
             g.setColor(drawColor);
             int count = 0;
             monitor.subTask("drawing");
             Coordinate world_location = new Coordinate(); // single object for re-use in transform below (minimize object creation)
             FeatureReader featureReader = jsonReader.getFeatureReader();
             Feature feature;
             while( (feature = featureReader.getFeature()) != null ) {
                 Point[] points = feature.getPoints();
                 Point point = points[0];    // TODO: Support multi-point geometries
                 Coordinate location = point.getCoordinate();
 
                 if (bounds_transformed != null && !bounds_transformed.contains(location)) {
                     continue; // Don't draw points outside viewport
                 }
                 try {
                     JTS.transform(location, world_location, transform_d2w);
                 } catch (Exception e) {
                     //JTS.transform(location, world_location, transform_w2d.inverse());
                 }
 
                 java.awt.Point p = getContext().worldToPixel(world_location);
                 renderSite(g, p);
                 double[] label_position_angles = new double[]{0,90};
                 Map<String,Object> properties = feature.getProperties();
                 if(properties!=null){
                     try {
                         if(properties.containsKey("sectors")){
                             Object sectorsObj = properties.get("sectors");
                             System.out.println("Found sectors: "+sectorsObj);
                             if(sectorsObj instanceof JSONArray){
                                 JSONArray sectors = (JSONArray)sectorsObj;
                                 for( int s = 0; s < sectors.size(); s++ ) {
                                     JSONObject sector = sectors.getJSONObject(s);
                                     if(sector!=null){
                                         System.out.println("Sector: "+sector);
                                         JSONObject sectorProperties = sector.getJSONObject("properties");
                                         double azimuth = sectorProperties.getDouble("azimuth");
                                         double beamwidth = sectorProperties.getDouble("beamwidth");
                                         renderSector(g, p, azimuth, beamwidth);
                                         if(s<label_position_angles.length){
                                             label_position_angles[s] = azimuth;
                                         }
                                         //g.setColor(drawColor);
                                         //g.rotate(-Math.toRadians(beamwidth/2));
                                         //g.drawString(sector.getString("name"),20,0);
                                     }
                                 }
                             }else{
                                 System.err.println("sectors object is not a JSONArray: "+sectorsObj);
                             }
                         }
                     } finally {
                         if (base_transform != null) {
                             // recover the normal transform
                             g.setTransform(base_transform);
                             g.setColor(drawColor);
                         }
                     }
                 }
                 double label_position_angle = Math.toRadians(-90 + (label_position_angles[0]+label_position_angles[1])/2.0);
                 int label_x = 5+(int)(10 * Math.cos(label_position_angle));
                 int label_y = (int)(10 * Math.sin(label_position_angle));
                 g.drawString(feature.toString(), p.x + label_x, p.y + label_y);
                 g.setTransform(base_transform);
                 monitor.worked(1);
                 count++;
                 if (monitor.isCanceled())
                     break;
             }
         } catch (TransformException e) {
             throw new RenderException(e);
         } catch (FactoryException e) {
             throw new RenderException(e);
         } catch (IOException e) {
             throw new RenderException(e); // rethrow any exceptions encountered
         } finally {
 //            if (jsonReader != null)
 //                jsonReader.close();
             monitor.done();
         }
     }
 
     /**
      * Render the sector symbols based on the point and azimuth.
      * We simply save the graphics transform, then modify the graphics
      * through the appropriate transformations (origin to site, and rotations
      * for drawing the lines and arcs).
      * @param g
      * @param p
      * @param azimuth
      */
     private void renderSector( Graphics2D g, java.awt.Point p, double azimuth, double beamwidth ) {
         if(base_transform==null) base_transform = g.getTransform();
         if(beamwidth<10) beamwidth = 10;
         g.setTransform(base_transform);
         g.translate(p.x, p.y);
         g.rotate(Math.toRadians(-90 + azimuth - beamwidth/2.0));
         g.setColor(fillColor);
         g.fillArc(-20, -20, 40, 40, 0, -(int)beamwidth);
         g.setColor(drawColor);
         g.drawArc(-20, -20, 40, 40, 0, -(int)beamwidth);
         g.drawLine(0, 0, 20, 0);
         g.rotate(Math.toRadians(beamwidth));
         g.drawLine(0, 0, 20, 0);
     }
 
     /**
      * This one is very simple, just draw a circle at the site location.
      * @param g
      * @param p
      */
     private void renderSite( Graphics2D g, java.awt.Point p ) {
         g.fillOval(p.x - 5, p.y - 5, 10, 10);
     }
 
     @Override
     public void render( IProgressMonitor monitor ) throws RenderException {
         Graphics2D g = getContext().getImage().createGraphics();
         render(g, monitor);
     }
 
 }
