 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.modelstate;
 
 import static cz.cuni.mff.peckam.java.origamist.math.MathHelper.EPSILON;
 import static java.lang.Math.abs;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 import javax.media.j3d.GeometryArray;
 import javax.media.j3d.LineArray;
 import javax.media.j3d.TriangleArray;
 import javax.vecmath.Color4b;
 import javax.vecmath.Point2d;
 import javax.vecmath.Point3d;
 import javax.vecmath.Point4d;
 import javax.vecmath.Vector3d;
 
 import org.apache.log4j.Logger;
 
 import cz.cuni.mff.peckam.java.origamist.math.HalfSpace3d;
 import cz.cuni.mff.peckam.java.origamist.math.IntersectionWithTriangle;
 import cz.cuni.mff.peckam.java.origamist.math.Line3d;
 import cz.cuni.mff.peckam.java.origamist.math.MathHelper;
 import cz.cuni.mff.peckam.java.origamist.math.Polygon3d;
 import cz.cuni.mff.peckam.java.origamist.math.Segment3d;
 import cz.cuni.mff.peckam.java.origamist.math.Stripe3d;
 import cz.cuni.mff.peckam.java.origamist.math.Triangle2d;
 import cz.cuni.mff.peckam.java.origamist.math.Triangle3d;
 import cz.cuni.mff.peckam.java.origamist.model.Origami;
 import cz.cuni.mff.peckam.java.origamist.model.Step;
 import cz.cuni.mff.peckam.java.origamist.model.UnitDimension;
 import cz.cuni.mff.peckam.java.origamist.model.UnitHelper;
 import cz.cuni.mff.peckam.java.origamist.model.jaxb.Unit;
 import cz.cuni.mff.peckam.java.origamist.utils.ChangeNotification;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservableList;
 import cz.cuni.mff.peckam.java.origamist.utils.ObservableList.ChangeTypes;
 import cz.cuni.mff.peckam.java.origamist.utils.Observer;
 
 /**
  * The internal state of the model after some steps.
  * 
  * @author Martin Pecka
  */
 public class ModelState implements Cloneable
 {
     /**
      * Folds on this paper.
      */
     protected ObservableList<Fold>                 folds                 = new ObservableList<Fold>();
 
     /**
      * Cache for array of the lines representing folds.
      */
     protected LineArray                            foldLineArray         = null;
 
     /**
      * If true, the value of foldLineArray doesn't have to be consistent and a call to updateLineArray is needed.
      */
     protected boolean                              foldLineArrayDirty    = true;
 
     /** The triangles this model state consists of. */
     protected ObservableList<ModelTriangle>        triangles             = new ObservableList<ModelTriangle>();
 
     /** The layers of the paper. */
     protected ObservableList<Layer>                layers                = new ObservableList<Layer>();
 
     /** The mapping of triangles to their containing layer. Automatically updated when <code>layers</code> change */
     protected Hashtable<ModelTriangle, Layer>      trianglesToLayers     = new Hashtable<ModelTriangle, Layer>();
 
     /**
      * A cache for quick finding of a 3D triangle corresponding to the given 2D triangle. Automatically updated when
      * <code>triangles</code> change.
      */
     protected Hashtable<Triangle2d, ModelTriangle> paperToSpaceTriangles = new Hashtable<Triangle2d, ModelTriangle>();
 
     /**
      * The triangles the model state consists of. This representation can be directly used by Java3D.
      */
     protected TriangleArray                        trianglesArray        = null;
 
     /**
      * If true, the value of trianglesArray doesn't have to be consistent and a call to updateVerticesArray is needed.
      */
     protected boolean                              trianglesArrayDirty   = true;
 
     /**
      * Rotation of the model (around the axis from eyes to display) in radians.
      */
     protected double                               rotationAngle         = 0;
 
     /**
      * The angle the model is viewed from (angle between eyes and the unfolded paper surface) in radians.
      * 
      * PI/2 means top view, -PI/2 means bottom view
      */
     protected double                               viewingAngle          = Math.PI / 2.0;
 
     /**
      * The step this state belongs to.
      */
     protected Step                                 step;
 
     /**
      * The origami model which is this the state of.
      */
     protected Origami                              origami;
 
     /**
      * The number of steps a foldline remains visible.
      */
     protected int                                  stepBlendingTreshold  = 5;
 
     public ModelState()
     {
         addObservers();
     }
 
     /**
      * Add all the needed observers to this state's observable fields.
      */
     protected void addObservers()
     {
         folds.addObserver(new Observer<Fold>() {
             @Override
             public void changePerformed(ChangeNotification<Fold> change)
             {
                 ModelState.this.foldLineArrayDirty = true;
 
                 if (change.getChangeType() == ChangeTypes.ADD) {
                     change.getItem().lines.addObserver(new Observer<FoldLine>() {
                         @Override
                         public void changePerformed(ChangeNotification<FoldLine> change)
                         {
                             ModelState.this.foldLineArrayDirty = true;
                         }
                     });
                 }
             }
         });
 
         triangles.addObserver(new Observer<ModelTriangle>() {
             @Override
             public void changePerformed(ChangeNotification<ModelTriangle> change)
             {
                 ModelState.this.trianglesArrayDirty = true;
                 if (change.getChangeType() != ChangeTypes.ADD) {
                     ModelTriangle t = change.getOldItem();
                     paperToSpaceTriangles.remove(t.originalPosition);
                 } else if (change.getChangeType() != ChangeTypes.REMOVE) {
                     ModelTriangle t = change.getItem();
                     paperToSpaceTriangles.put(t.originalPosition, t);
                 }
             }
         });
 
         layers.addObserver(new Observer<Layer>() {
             @Override
             public void changePerformed(ChangeNotification<Layer> change)
             {
                 if (change.getChangeType() != ChangeTypes.ADD) {
                     Layer old = change.getOldItem();
                     for (ModelTriangle t : old.getTriangles()) {
                         trianglesToLayers.remove(t);
                     }
                     old.clearTrianglesObservers();
                 } else if (change.getChangeType() != ChangeTypes.REMOVE) {
                     final Layer layer = change.getItem();
                     for (ModelTriangle t : layer.getTriangles()) {
                         trianglesToLayers.put(t, layer);
                     }
                     layer.addTrianglesObserver(new Observer<ModelTriangle>() {
                         @Override
                         public void changePerformed(ChangeNotification<ModelTriangle> change)
                         {
                             if (change.getChangeType() != ChangeTypes.ADD) {
                                 trianglesToLayers.remove(change.getOldItem());
                             } else if (change.getChangeType() != ChangeTypes.REMOVE) {
                                 ModelTriangle triangle = change.getItem();
                                 trianglesToLayers.put(triangle, layer);
                             }
                         }
                     });
                 }
             }
 
         });
     }
 
     /**
      * Set the step this model state belongs to.
      * 
      * @param step The step to set.
      */
     public void setStep(Step step)
     {
         this.step = step;
     }
 
     /**
      * Set the origami model this step will work with.
      * 
      * @param origami The origami model.
      */
     public void setOrigami(Origami origami)
     {
         this.origami = origami;
     }
 
     /**
      * Takes a point defined in the 2D paper relative coordinates and returns the position of the point in the 3D model
      * state (also in relative coordinates).
      * 
      * @param point
      * @return
      */
     protected Point3d locatePointFromPaperTo3D(Point2d point)
     {
         ModelTriangle containingTriangle = null;
         // TODO possible performance loss, try to use some kind of Voronoi diagram??? But it seems that this section
         // won't be preformance-bottle-neck
         for (ModelTriangle t : triangles) {
             if (t.getOriginalPosition().contains(point)) {
                 containingTriangle = t;
                 break;
             }
         }
 
         if (containingTriangle == null) {
             Logger.getLogger(getClass()).warn("locatePointFromPaperTo3D: Couldn't locate point " + point);
             return new Point3d();
         }
 
         Vector3d barycentric = containingTriangle.getOriginalPosition().getBarycentricCoords(point);
 
         return containingTriangle.interpolatePointFromBarycentric(barycentric);
     }
 
     /**
      * Update the contents of the foldLineArray so that it corresponds to the actual contents of the folds variable.
      */
     protected synchronized void updateLineArray()
     {
         int linesCount = 0;
         for (Fold fold : folds) {
             linesCount += fold.getLines().size();
         }
 
         UnitDimension paperSize = origami.getModel().getPaper().getSize();
         double ratio = UnitHelper.convertTo(Unit.REL, Unit.M, 1, paperSize.getUnit(), paperSize.getMax());
 
         foldLineArray = new LineArray(2 * linesCount, GeometryArray.COORDINATES | GeometryArray.COLOR_4);
         int i = 0;
         for (Fold fold : folds) {
             for (FoldLine line : fold.getLines()) {
                 Point3d startPoint = new Point3d(line.getLine().getSegment3d().getP1());
                 startPoint.scale(ratio);
                 foldLineArray.setCoordinate(2 * i, startPoint);
 
                 Point3d endPoint = new Point3d(line.getLine().getSegment3d().getP2());
                 endPoint.scale(ratio);
                 foldLineArray.setCoordinate(2 * i + 1, endPoint);
 
                 // TODO implement some more line thickness and style possibilities
                 byte alpha = (byte) 255;
                 if (line.getDirection() != null) {
                     // TODO invent some more sophisticated way to determine the fold "age"
                     int diff = step.getId() - fold.getOriginatingStepId();
                     if (diff <= stepBlendingTreshold) {
                         alpha = (byte) (255 - (diff / stepBlendingTreshold) * 255);
                     } else {
                         alpha = 0;
                     }
                 }
                 foldLineArray.setColor(2 * i, new Color4b((byte) 0, (byte) 0, (byte) 0, alpha));
                 foldLineArray.setColor(2 * i + 1, new Color4b((byte) 0, (byte) 0, (byte) 0, alpha));
                 i++;
             }
         }
 
         foldLineArrayDirty = false;
     }
 
     /**
      * Retrurn the line array corresponding to the list of folds.
      * 
      * @return The line array corresponding to the list of folds.
      */
     public synchronized LineArray getLineArray()
     {
         if (foldLineArrayDirty)
             updateLineArray();
 
         return foldLineArray;
     }
 
     /**
      * Update the contents of the trianglesArray so that it corresponds to the actual contents of the triangles
      * variable.
      */
     protected synchronized void updateTrianglesArray()
     {
         trianglesArray = new TriangleArray(triangles.size() * 3, TriangleArray.COORDINATES);
 
         UnitDimension paperSize = origami.getModel().getPaper().getSize();
         double ratio = 1.0 / UnitHelper.convertTo(Unit.REL, Unit.M, 1, paperSize.getUnit(), paperSize.getMax());
 
         int i = 0;
         Point3d p1, p2, p3;
         for (Triangle3d triangle : triangles) {
             p1 = (Point3d) triangle.getP1().clone();
             p1.project(new Point4d(p1.x, p1.y, p1.z, ratio));
 
             p2 = (Point3d) triangle.getP2().clone();
             p2.project(new Point4d(p2.x, p2.y, p2.z, ratio));
 
             p3 = (Point3d) triangle.getP3().clone();
             p3.project(new Point4d(p3.x, p3.y, p3.z, ratio));
 
             trianglesArray.setCoordinate(3 * i, p1);
             trianglesArray.setCoordinate(3 * i + 1, p2);
             trianglesArray.setCoordinate(3 * i + 2, p3);
 
             i++;
         }
 
         trianglesArrayDirty = false;
     }
 
     /**
      * Return the triangle array.
      * 
      * @return The triangle array.
      */
     public synchronized TriangleArray getTrianglesArray()
     {
         if (trianglesArrayDirty)
             updateTrianglesArray();
 
         return trianglesArray;
     }
 
     /**
      * Performs a valley/mountain fold.
      * 
      * @param direction The direction of the fold - VALLEY/MOUNTAIN.
      * @param startPoint Starting point of the fold (in 2D paper relative coordinates).
      * @param endPoint Ending point of the fold (in 2D paper relative coordinates).
      * @param affectedLayers The layers the fold will be performed on.
      * @param angle The angle the paper should be bent by (in radians). Value in (0, PI) means that the down right part
      *            of the paper (with respect to the line) will be moved; value in (-PI,0) means that the other part of
      *            paper will be moved.
      */
     public void makeFold(Direction direction, Point2d startPoint, Point2d endPoint, List<Integer> affectedLayers,
             double angle)
     {
         // TODO implement some way of defining which part of the paper will stay on its place and which will move
         Point3d start = locatePointFromPaperTo3D(startPoint);
         Point3d end = locatePointFromPaperTo3D(endPoint);
         Segment3d segment = new Segment3d(start, end);
 
         LinkedHashMap<Layer, Segment3d> layerInts = getLayers(segment);
 
         int i = 1;
         Iterator<Entry<Layer, Segment3d>> it = layerInts.entrySet().iterator();
         while (it.hasNext()) {
             Entry<Layer, Segment3d> entry = it.next();
             if (!affectedLayers.contains(i++)) {
                 it.remove();
             } else {
                 // TODO handle direction in some appropriate way
                 makeFoldInLayer(entry.getKey(), direction, entry.getValue());
             }
         }
 
         bendPaper(direction, segment, layerInts, angle);
     }
 
     /**
      * Bends the paper. Requires that the fold line goes only along triangle edges, not through the interiors of them.
      * 
      * To specify the part of the paper that will be rotated, the segment's direction vector is used. Make cross product
      * of the normal of the layer the segment lies in and the direction vector of the segment. The cross product points
      * to the part of the paper that will be moved.
      * 
      * @param direction The direction of the fold - VALLEY/MOUNTAIN.
      * @param segment The segment to bend around. Note that the direction vector of the segment specifies which part of
      *            the paper will be rotated.
      * @param layerInts A map of affected layers and intersections of the fold stripe with them.
      * @param angle The angle the paper should be bent by (in radians). Value in (0, PI) means that the down right part
      *            of the paper (with respect to the line) will be moved; value in (-PI,0) means that the other part of
      *            paper will be moved.
      */
     protected void bendPaper(Direction direction, Segment3d segment, Map<Layer, Segment3d> layerInts, double angle)
     {
         double angle1 = angle;
         if (abs(angle1) < EPSILON)
             return;
 
         if (direction == Direction.MOUNTAIN)
             angle1 = -angle1;
 
         Point3d segCenter = new Point3d(segment.getP1());
         segCenter.add(segment.getP2());
         segCenter.scale(0.5d);
 
         Layer segLayer = getLayerForPoint(segCenter);
         Vector3d layerNormalSegmentDirCross = new Vector3d();
         layerNormalSegmentDirCross.cross(segLayer.getNormal(), segment.getVector());
         Point3d r = new Point3d(layerNormalSegmentDirCross);
 
         HalfSpace3d halfspace = HalfSpace3d.createPerpendicularToTriangle(segment.getP1(), segment.getP2(), r);
 
         // further we will need to search in layerInts, but the layers will probably change, so we backup the old
         // removed layers here
         Hashtable<Layer, Layer> newLayersToOldOnes = new Hashtable<Layer, Layer>();
 
         Queue<ModelTriangle> queue = new LinkedList<ModelTriangle>();
         for (Entry<Layer, Segment3d> layerInt : layerInts.entrySet()) {
             Layer layer = layerInt.getKey();
             Segment3d splitSegment = layerInt.getValue();
 
             List<Polygon3d<ModelTriangle>> part1 = new LinkedList<Polygon3d<ModelTriangle>>();
             List<Polygon3d<ModelTriangle>> part2 = new LinkedList<Polygon3d<ModelTriangle>>();
             layer.splitPolygon(splitSegment, part1, part2);
 
             boolean swapParts = false;
             if (part1.size() > 0) {
                 Triangle3d part1t = part1.get(0).getTriangles().iterator().next();
                 if (!(halfspace.contains(part1t.getP1()) && halfspace.contains(part1t.getP2()) && halfspace
                         .contains(part1t.getP3()))) {
                     swapParts = true;
                 }
             } else {
                 Triangle3d part2t = part2.get(0).getTriangles().iterator().next();
                 if (!(halfspace.contains(part2t.getP1()) && halfspace.contains(part2t.getP2()) && halfspace
                         .contains(part2t.getP3()))) {
                     swapParts = true;
                 }
             }
 
             if (swapParts) {
                 List<Polygon3d<ModelTriangle>> tmp = part1;
                 part1 = part2;
                 part2 = tmp;
             }
 
             this.layers.remove(layer);
             for (Polygon3d<ModelTriangle> l : part1) {
                 Layer newL = new Layer(l);
                 this.layers.add(newL);
                 newLayersToOldOnes.put(newL, layer);
             }
             for (Polygon3d<ModelTriangle> l : part2) {
                 Layer newL = new Layer(l);
                 this.layers.add(newL);
                 newLayersToOldOnes.put(newL, layer);
             }
 
             for (Polygon3d<ModelTriangle> l : part1) {
                 queue.addAll(l.getTriangles());
             }
         }
 
         // to find all triangles that have to be rotated, first add all triangles in "affected" layers that lie in the
         // right halfspace, and then go over neighbors of all found triangles to rotate and add them, if the neighbor
         // doesn't lie on an opposite side of a fold line.
 
         Set<ModelTriangle> inQueue = new HashSet<ModelTriangle>(queue);
         Set<ModelTriangle> trianglesToRotate = new HashSet<ModelTriangle>();
         ModelTriangle t;
         while ((t = queue.poll()) != null) {
             trianglesToRotate.add(t);
 
             // border is the intersection line in the layer of the processed triangle - if the triangle lies in a layer
             // without intersection line, it can be surely added to the queue
             Segment3d border = layerInts.get(trianglesToLayers.get(t));
             if (border == null) {
                 Layer oldLayer = newLayersToOldOnes.get(trianglesToLayers.get(t));
                 if (oldLayer != null)
                     border = layerInts.get(oldLayer);
             }
 
             List<ModelTriangle> neighbors = findNeighbors(t);
             n: for (ModelTriangle n : neighbors) {
                 if (inQueue.contains(n))
                     continue;
 
                 if (border != null) {
                     Segment3d intWithNeighbor = t.getCommonEdge(n, false);
                     // if the common edge between t and n is a part of the border line, we need to check if n lies in
                     // the processed halfspace; if not, it is "on the other side" of the border line, so we don't want
                     // to add it to the queue
                     if (intWithNeighbor != null && intWithNeighbor.overlaps(border)) {
                         if (!halfspace.contains(n.getP1()) || !halfspace.contains(n.getP2())
                                 || !halfspace.contains(n.getP3()))
                             continue n;
                     }
                 }
                 // else - if no border line lies in t's layer, we automatically want to add all of its neighbors
 
                 queue.add(n);
                 inQueue.add(n);
             }
         }
 
         Set<Layer> layersToRotate = new HashSet<Layer>();
 
         for (ModelTriangle tr : trianglesToRotate) {
             layersToRotate.add(trianglesToLayers.get(tr));
         }
 
         for (Layer l : layersToRotate) {
             // remove, rotate, and then add the triangles back to make sure all caches and maps will hold the correct
             // value
             triangles.removeAll(l.getTriangles());
             l.rotate(segment, angle1);
             triangles.addAll(l.getTriangles());
         }
     }
 
     /**
      * Returns a list of triangles having a common point with the given triangle.
      * 
      * @param t The triangle to find neighbors to.
      * @return The list of neighbors of t.
      */
     protected List<ModelTriangle> findNeighbors(ModelTriangle triangle)
     {
         return triangle.getNeighbors();
     }
 
     /**
      * Returns a list of triangles having a common point with the given triangle.
      * 
      * @param t The triangle to find neighbors to.
      * @return The list of neighbors of t.
      */
     protected List<Triangle2d> findNeighbors(Triangle2d triangle)
     {
         final List<ModelTriangle> list = paperToSpaceTriangles.get(triangle).getNeighbors();
         List<Triangle2d> result = new LinkedList<Triangle2d>();
 
         for (ModelTriangle t : list)
             result.add(t.getOriginalPosition());
 
         return result;
     }
 
     /**
      * <p>
      * Returns a sorted map of layers defined by the given segment.
      * </p>
      * 
      * <p>
      * <i>A layer is a part of the paper surrounded by either fold lines or paper boundaries.</i>
      * </p>
      * 
      * <p>
      * This function returns the layers that intersect with a stripe defined by the two given points and that is
      * perpendicular to the layer the line lies in.
      * </p>
      * 
      * <p>
      * The list is sorted in the order the layers intersect with the stripe.
      * </p>
      * 
      * <p>
      * The very first layer is the one that has its intersection the furthest in the direction of the normal of the
      * layer the segment lies in.
      * </p>
      * 
      * @param segment The segment we search layers for.
      * @return A list of layers defined by the given line and the intersections with the fold stripe with those layers.
      */
     protected LinkedHashMap<Layer, Segment3d> getLayers(Segment3d segment)
     {
         // finds the top layer
         Point3d center = new Point3d(segment.getP1());
         center.add(segment.getP2());
         center.scale(0.5d);
 
         Layer firstLayer = getLayerForPoint(center);
         if (firstLayer == null)
             return new LinkedHashMap<Layer, Segment3d>();
 
         // find another layers: is done by creating a stripe perpendicular to the first layer and finding intersections
         // of the stripe with triangles
 
         final Vector3d stripeDirection = firstLayer.getNormal();
         final Line3d p1line = new Line3d(segment.getP1(), stripeDirection);
         final Line3d p2line = new Line3d(segment.getP2(), stripeDirection);
 
         final Stripe3d stripe = new Stripe3d(p1line, p2line);
 
         LinkedHashMap<Layer, Segment3d> result = new LinkedHashMap<Layer, Segment3d>();
 
         for (Layer l : layers) {
             Segment3d intSegment = l.getIntersectionSegment(stripe);
             if (intSegment == null || intSegment.getVector().epsilonEquals(new Vector3d(), MathHelper.EPSILON)) {
                 continue;
             } else {
                 result.put(l, intSegment);
             }
         }
 
         List<Layer> keys = new ArrayList<Layer>(result.keySet());
         // sort the layers along the stripe
         // we assume that all the layers intersect with the stripe, so it's no problem to sort the layers by their
         // intersections with one of the stripe's border line
         Collections.sort(keys, new Comparator<Layer>() {
             @Override
             public int compare(Layer o1, Layer o2)
             {
                 Line3d int1 = o1.getPlane().getIntersection(p1line);
                 Line3d int2 = o2.getPlane().getIntersection(p1line);
 
                 // we can assume int1 and int2 to be regular points, because intersections with layers parallel to the
                 // stripe are discarded
 
                 double t1 = p1line.getParameterForPoint(int1.getPoint());
                 double t2 = p1line.getParameterForPoint(int2.getPoint());
                 return (t1 - t2 > EPSILON) ? 1 : (t1 - t2 < -EPSILON ? -1 : 0);
             }
         });
 
         LinkedHashMap<Layer, Segment3d> sortedResult = new LinkedHashMap<Layer, Segment3d>(result.size());
         for (Layer l : keys) {
             sortedResult.put(l, result.get(l));
         }
 
         return sortedResult;
     }
 
     /**
      * Return the layer that contains the given point.
      * 
      * @param point The point to find layer for.
      * @return The layer that contains the given point.
      */
     protected Layer getLayerForPoint(Point3d point)
     {
         for (Layer l : layers) {
             if (l.contains(point))
                 return l;
         }
         Logger.getLogger(getClass()).warn("getLayerForPoint: cannot find layer for point " + point);
         return null;
     }
 
     /**
      * Given all triangles in one layer, divides all triangles in the layer by the given line to smaller triangles.
      * 
      * @param layer The triangles in the layer with the appropriate intersection points.
      * @param direction The direction of the created fold.
      * @param segment The segments defining the fold directly in this layer.
      * 
      * @return The intersections of the given segment with the layer.
      */
     protected List<Segment3d> makeFoldInLayer(Layer layer, Direction direction, Segment3d segment)
     {
         List<IntersectionWithTriangle<ModelTriangle>> intersections = layer.getIntersectionsWithTriangles(segment);
 
         Fold fold = new Fold();
         fold.originatingStepId = this.step.getId();
 
         for (IntersectionWithTriangle<ModelTriangle> intersection : intersections) {
             if (intersection == null) {
                 // no intersection with the triangle - something's weird (we loop over intersections with triangles)
                 throw new IllegalStateException(
                         "Invalid diagram: no intersection found in IntersectionWithTriangle in step " + step.getId());
             }
 
             List<ModelTriangle> newTriangles = layer.subdivideTriangle(intersection);
             if (newTriangles.size() > 1) {
                 triangles.remove(intersection.triangle);
                 triangles.addAll(newTriangles);
 
                 for (ModelTriangle t : newTriangles) {
                     int i = 0;
                     for (Segment3d edge : t.getEdges()) {
                         Segment3d edgeInt = edge.getIntersection(intersection);
                         if (edgeInt != null && !edgeInt.getVector().epsilonEquals(new Vector3d(), MathHelper.EPSILON)) {
                             // this method adds all fold lines twice - one for each triangle adjacent to the
                             // intersection segment - but we don't care (maybe we should, it'll be more clear further)
                             FoldLine line = new FoldLine();
                             line.setDirection(direction);
                             line.setFold(fold);
                             line.setLine(new ModelTriangleEdge(t, i));
                             fold.getLines().add(line);
                         }
                         i++;
                     }
                 }
             } else if (newTriangles.size() == 1) {
                 int i = 0;
                 for (Segment3d edge : intersection.getTriangle().getEdges()) {
                     Segment3d edgeInt = edge.getIntersection(intersection);
                     if (edgeInt != null && !edgeInt.getVector().epsilonEquals(new Vector3d(), MathHelper.EPSILON)) {
                         // this method adds all fold lines twice - one for each triangle adjacent to the
                         // intersection segment - but we don't care (maybe we should, it'll be more clear further)
                         FoldLine line = new FoldLine();
                         line.setDirection(direction);
                         line.setFold(fold);
                         line.setLine(new ModelTriangleEdge(intersection.getTriangle(), i));
                         fold.getLines().add(line);
                     }
                     i++;
                 }
             }
         }
 
         folds.add(fold);
 
         return layer.joinNeighboringSegments(intersections);
     }
 
     /**
      * Adds the given angle to the current angle of rotation.
      * 
      * @param rotation The angle to add (in radians).
      */
     public void addRotation(double rotation)
     {
         setRotation(rotationAngle + rotation);
     }
 
     /**
      * Sets the current angle of rotation to the given value.
      * 
      * @param rotation The angle to set (in radians).
      */
     public void setRotation(double rotation)
     {
         rotationAngle = rotation;
 
         while (rotationAngle > Math.PI)
            rotationAngle -= Math.PI;
         while (rotationAngle < -Math.PI)
            rotationAngle += Math.PI;
     }
 
     /**
      * Return the rotation of the paper.
      * 
      * @return The rotation of the paper (in radians).
      */
     public double getRotation()
     {
         return rotationAngle;
     }
 
     /**
      * Adds the given angle to the current viewing angle of the paper.
      * 
      * The angle will be "cropped" to <-PI/2,PI/2> interval.
      * 
      * @param angle The angle to add (in radians).
      */
     public void addViewingAngle(double angle)
     {
         setViewingAngle(viewingAngle + angle);
     }
 
     /**
      * Changes the viewing angle from top to bottom and vice versa.
      */
     public void flipViewingAngle()
     {
         setViewingAngle(-viewingAngle);
     }
 
     /**
      * Sets the current viewing angle to the given value.
      * 
      * The angle will be "cropped" to <-PI/2,PI/2> interval.
      * 
      * @param angle The angle to set (in radians).
      */
     public void setViewingAngle(double angle)
     {
         viewingAngle = angle;
 
         if (viewingAngle > Math.PI / 2.0)
             viewingAngle = Math.PI / 2.0;
         if (viewingAngle < -Math.PI / 2.0)
             viewingAngle = -Math.PI / 2.0;
     }
 
     /**
      * Get the current viewing angle.
      * 
      * @return The viewing angle (in radians).
      */
     public double getViewingAngle()
     {
         return viewingAngle;
     }
 
     @Override
     public ModelState clone() throws CloneNotSupportedException
     {
         ModelState result = (ModelState) super.clone();
 
         result.foldLineArray = null;
         result.foldLineArrayDirty = true;
         result.trianglesArray = null;
         result.trianglesArrayDirty = true;
 
         result.layers = new ObservableList<Layer>(layers.size());
         result.folds = new ObservableList<Fold>(folds.size());
         result.paperToSpaceTriangles = new Hashtable<Triangle2d, ModelTriangle>(paperToSpaceTriangles.size());
         result.triangles = new ObservableList<ModelTriangle>(triangles.size());
         result.trianglesToLayers = new Hashtable<ModelTriangle, Layer>(trianglesToLayers.size());
 
         result.addObservers();
 
         Hashtable<ModelTriangle, ModelTriangle> newTriangles = new Hashtable<ModelTriangle, ModelTriangle>(
                 triangles.size());
         for (ModelTriangle t : triangles) {
             ModelTriangle newT = t.clone();
             // the fold lines will be filled further again with their new instances
             newT.s1FoldLines = null;
             newT.s2FoldLines = null;
             newT.s3FoldLines = null;
             newTriangles.put(t, newT);
             result.triangles.add(newT);
         }
 
         for (ModelTriangle t : result.triangles) {
             List<ModelTriangle> oldNeighbors = new LinkedList<ModelTriangle>(t.getRawNeighbors());
             t.getRawNeighbors().clear();
             for (ModelTriangle n : oldNeighbors) {
                 ModelTriangle newN = newTriangles.get(n);
                 if (newN == null)
                     throw new IllegalStateException();
                 t.getRawNeighbors().add(newN);
             }
         }
 
         for (Fold fold : folds) {
             Fold newFold = fold.clone();
             newFold.lines.getObservers().clear();
             newFold.addObservers();
 
             for (FoldLine l : newFold.lines) {
                 ModelTriangle newTriangle = newTriangles.get(l.getLine().getTriangle());
                 if (newTriangle == null)
                     throw new IllegalStateException();
 
                 l.getLine().setTriangle(newTriangle);
                 List<FoldLine> foldLines = newTriangle.getFoldLines(l.getLine().getIndex());
                 if (foldLines == null) {
                     newTriangle.setFoldLines(l.getLine().getIndex(), new LinkedList<FoldLine>());
                     foldLines = newTriangle.getFoldLines(l.getLine().getIndex());
                 }
                 foldLines.add(l);
             }
 
             result.folds.add(newFold);
         }
 
         for (Layer l : layers) {
             List<ModelTriangle> triangles = new LinkedList<ModelTriangle>();
             for (ModelTriangle t : l.getTriangles()) {
                 ModelTriangle newT = newTriangles.get(t);
                 if (newT == null)
                     throw new IllegalStateException();
                 triangles.add(newT);
             }
             result.layers.add(new Layer(triangles));
         }
 
         return result;
     }
 }
