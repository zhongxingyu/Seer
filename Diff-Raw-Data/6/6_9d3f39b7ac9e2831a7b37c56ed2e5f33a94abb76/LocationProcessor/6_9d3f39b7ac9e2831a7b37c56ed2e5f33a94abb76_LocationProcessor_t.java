 package org.pit.fetegeo.importer.processors;
 
 import org.openstreetmap.osmosis.core.domain.v0_6.*;
 import org.openstreetmap.osmosis.core.lifecycle.CompletableContainer;
 import org.openstreetmap.osmosis.core.store.*;
 import org.pit.fetegeo.importer.objects.Constants;
 import org.postgis.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Author: Pit Apps
  * Date: 10/26/12
  * Time: 3:43 PM
  */
 public class LocationProcessor {
 
   private static Geometry currentLocation;
 
   private static CompletableContainer storeContainer;
 
   private static RandomAccessObjectStore<CleverPoint> nodeOS;
   private static RandomAccessObjectStoreReader<CleverPoint> nodeReader;
   private static IndexStore<Long, LongLongIndexElement> nodeIDX;
   private static IndexStoreReader<Long, LongLongIndexElement> nodeIDXReader;
 
   private static RandomAccessObjectStore<Way> wayOS;
   private static RandomAccessObjectStoreReader<Way> wayReader;
   private static IndexStore<Long, LongLongIndexElement> wayIDX;
   private static IndexStoreReader<Long, LongLongIndexElement> wayIDXReader;
 
   private static Map<Long, Geometry> relationMap = new HashMap<Long, Geometry>();
 
   public LocationProcessor() {
 
     storeContainer = new CompletableContainer();
 
     // Create temp files for locations
     try {
       File nodeOSFile = File.createTempFile("nodeOS-", ".tmp", Constants.OUT_PATH);
       File nodeIDXFile = File.createTempFile("nodeIDX-", ".tmp", Constants.OUT_PATH);
       nodeOSFile.deleteOnExit();
       nodeIDXFile.deleteOnExit();
       nodeOS = storeContainer.add(new RandomAccessObjectStore<CleverPoint>(new SingleClassObjectSerializationFactory(CleverPoint.class), nodeOSFile));
       nodeIDX = storeContainer.add(new IndexStore<Long, LongLongIndexElement>(LongLongIndexElement.class, new ComparableComparator<Long>(), nodeIDXFile));
 
       File wayOSFile = File.createTempFile("wayOS-", ".tmp", Constants.OUT_PATH);
       File wayIDXFile = File.createTempFile("wayIDX-", ".tmp", Constants.OUT_PATH);
       wayOSFile.deleteOnExit();
       wayIDXFile.deleteOnExit();
       wayOS = storeContainer.add(new RandomAccessObjectStore<Way>(new SingleClassObjectSerializationFactory(Way.class), wayOSFile));
       wayIDX = storeContainer.add(new IndexStore<Long, LongLongIndexElement>(LongLongIndexElement.class, new ComparableComparator<Long>(), wayIDXFile));
     } catch (IOException ioe) {
       System.out.println("Could not create cache file " + ioe.getLocalizedMessage());
     }
   }
 
   /*
     Processes Nodes and caches Ways and Relation to file if they're needed for location later on.
     Sets currentLocation to the location found for the Entity
    */
   public void process(Entity entity) {
 
     // Find out what we're dealing with
     switch (entity.getType()) {
       case Node:
         currentLocation = process((Node) entity);
         break;
       case Way:
         currentLocation = process((Way) entity);
         wayIDX.write(new LongLongIndexElement(entity.getId(), wayOS.add((Way) entity)));
         break;
       case Relation:
         currentLocation = process((Relation) entity);
         break;
       default:
         break;
     }
   }
 
   /*
     Returns Geometry of the last processed location.
    */
   public static Geometry findLocation() {
     return currentLocation;
   }
 
   private Geometry process(Node node) {
     CleverPoint cp = new CleverPoint(node.getLatitude(), node.getLongitude());
     nodeIDX.write(new LongLongIndexElement(node.getId(), nodeOS.add(cp)));
 
     return cp;
   }
 
   private static Geometry process(Way way) {
     if (nodeReader == null) {
       nodeOS.complete();
       nodeReader = nodeOS.createReader();
     }
     if (nodeIDXReader == null) {
       nodeIDX.complete();
       nodeIDXReader = nodeIDX.createReader();
     }
 
     List<WayNode> wayNodes = way.getWayNodes();
     Point[] points = new Point[wayNodes.size()];
 
     for (int i = 0; i < points.length; i++) {
       try {
         points[i] = nodeReader.get(nodeIDXReader.get(wayNodes.get(i).getNodeId()).getValue());
       } catch (NoSuchIndexElementException nsiee) {
         // continue (maybe we're importing an incomplete file; we'll do our best to display as much of the way as possible)
       }
     }
 
     Geometry result;
 
     // If points make a circle, we have a polygon. otherwise we have a line
     if (points.length >= 3 && points[0].equals(points[points.length - 1])) {
       result = new Polygon(new LinearRing[]{new LinearRing(points)});
     } else {
       result = new LineString(points);
     }
     result.setSrid(4326);
 
     return result;
   }
 
   private static Geometry process(Relation relation) {
     if (wayReader == null) {
       wayOS.complete();
       wayReader = wayOS.createReader();
     }
     if (wayIDXReader == null) {
       wayIDX.complete();
       wayIDXReader = wayIDX.createReader();
     }
 
     List<Geometry> coordinateList = fetchRelationCoors(relation);
 
     // If we could not find all the coordinates, we return null!
     if (coordinateList == null) {
       return null;
     }
 
     GeometryCollection result = new GeometryCollection(coordinateList.toArray(new Geometry[coordinateList.size()]));
     result.setSrid(4326);
 
     relationMap.put(relation.getId(), result);
 
     return result;
   }
 
   private static List<Geometry> fetchRelationCoors(Relation relation) {
     List<Geometry> coordinateList = new ArrayList<Geometry>();
     for (RelationMember relationMember : relation.getMembers()) {
 
      // only care about outer roles. Some ways are not tagged...
      if (!relationMember.getMemberRole().equalsIgnoreCase("outer") && !relationMember.getMemberRole().isEmpty()) {
         continue;
       }
       switch (relationMember.getMemberType()) {
         case Node:
           // we don't care about Nodes as these are not used for roads or bounds (a part from designating the capital and other useless stuff)
           break;
         case Way:
           try {
             Way way = wayReader.get(wayIDXReader.get(relationMember.getMemberId()).getValue());
             if (way != null) {
               coordinateList.add(process(way));
             }
           } catch (NoSuchIndexElementException nsiee) {
             return null; // if we can't find a way, then the relation might return a faulty geometry
           }
 
           break;
         case Relation:
           Geometry otherRelation = relationMap.get(relationMember.getMemberId());
           if (otherRelation != null) {
             Geometry geometry;
             coordinateList.add(otherRelation);
             // We need to differentiate between the two different types of relations
             if (otherRelation instanceof MultiPolygon) {
               MultiPolygon multiPolygon = (MultiPolygon) otherRelation;
               for (int i = 0; i < multiPolygon.numPolygons(); i++) {
                 if ((geometry = multiPolygon.getPolygon(i)) != null) {
                   coordinateList.add(geometry);
                 }
               }
             } else if (otherRelation instanceof MultiLineString) {
               MultiLineString multiLineString = (MultiLineString) otherRelation;
               for (int i = 0; i < multiLineString.numLines(); i++) {
                 if ((geometry = multiLineString.getLine(i)) != null) {
                   coordinateList.add(geometry);
                 }
               }
             }
           } else {
             return null;
           }
           break;
         default:
           break;
       }
     }
     return coordinateList;
   }
 
   private List<Geometry> cleanList(List<Geometry> list, int type) {
     List<Geometry> cleanedList = new ArrayList<Geometry>();
     for (Geometry g : list) {
       if (g.getType() == type) {
         cleanedList.add(g);
       }
     }
     return cleanedList;
   }
 
   public void completeAndRelease() {
     storeContainer.complete();
     storeContainer.release();
   }
 
 }
