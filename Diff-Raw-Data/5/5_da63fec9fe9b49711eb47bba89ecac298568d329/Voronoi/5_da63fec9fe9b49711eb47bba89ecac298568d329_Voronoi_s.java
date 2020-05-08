 package computational_geometry.model.algorithms;
 
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import computational_geometry.model.beans.Line;
 import computational_geometry.model.beans.Point;
 import computational_geometry.model.beans.Segment;
 import computational_geometry.model.core.Lines;
 import computational_geometry.model.core.PointComparatorX;
 import computational_geometry.model.data_structures.HalfEdge.Edge;
 import computational_geometry.model.data_structures.HalfEdge.Face;
 import computational_geometry.model.data_structures.HalfEdge.Vert;
 import computational_geometry.model.data_structures.VoronoiDiagram;
 import computational_geometry.model.data_structures.VoronoiDiagram.VorCell;
 import computational_geometry.model.traces.HullResult;
 import computational_geometry.model.traces.VoronoiTrace;
 
 public class Voronoi {
 
     public static Rectangle bound = new Rectangle();
 
     /**
      * Compute voronoi diagram of a given list of points
      * using the divide and conquer method
      * @param points
      * @return
      */
     public static VoronoiTrace ComputeVoronoiDiagram(List<Point> points) {
         if (points.size() < 2) {
             return null;
         }
         List<Point> sortedPoints = new ArrayList<Point>(points);
         Collections.sort(sortedPoints, new PointComparatorX());
         return Vor(sortedPoints);
     }
 
     /**
      * Recursively compute the actual voronoi diagram of a given list of points
      * assuming the list is sorted
      * @param points : the sorted (by x) list of points
      * @return
      */
     private static VoronoiTrace Vor(List<Point> points) {
         VoronoiTrace trace = new VoronoiTrace();
         VoronoiDiagram vor;
         if (points.size() == 2) {
             vor = handle2PointsVor(points);
         } else if (points.size() == 3) {
             vor = handle3PointsVor(points);
         } else {
             vor = handleNPointsVor(points);
         }
 
         trace.vor = vor;
         return trace;
     }
 
     /**
      * Compute the voronoi diagram of two points
      * @param points : the sorted (by x) list of points
      * @return
      */
     private static VoronoiDiagram handle2PointsVor(List<Point> points) {
         VoronoiDiagram vor = new VoronoiDiagram();
         Point p1 = points.get(0);
         Point p2 = points.get(1);
         Line bisector = Lines.findBisector(p1, p2);
 
         Edge e1 = vor.new Edge();
         Edge e2 = vor.new Edge();
 
         VorCell c1 = vor.new VorCell(p1, e1);
         VorCell c2 = vor.new VorCell(p2, e2);
 
         Vert v1 = vor.new Vert(bisector.findLowerPoint(bound), e1);
         Vert v2 = vor.new Vert(bisector.findUpperPoint(bound), e2);
 
         e1.fill(v1, e2, c1, e1);
         e2.fill(v2, e1, c2, e2);
 
         vor.addEdge(e1);
         vor.addEdge(e2);
         vor.addFace(c1);
         vor.addFace(c2);
 
         return vor;
     }
 
     /**
      * Compute the voronoi diagram of three points
      * @param points : the sorted (by x) list of points
      * @return
      */
     private static VoronoiDiagram handle3PointsVor(List<Point> points) {
         VoronoiDiagram vor = new VoronoiDiagram();
         Point p1 = points.get(0);
         Point p2 = points.get(1);
         Point p3 = points.get(2);
         Line l1 = Lines.findLine(p1, p2);
         Line l2 = Lines.findLine(p1, p3);
         Line l3 = Lines.findLine(p2, p3);
         Line b1 = Lines.findBisector(p1, p2);
         Line b2 = Lines.findBisector(p1, p3);
         Line b3 = Lines.findBisector(p2, p3);
         Point inter = b1.findIntersection(b2);
 
         VorCell c1;
         VorCell c2;
         VorCell c3;
         if (inter == null) {
             // two bisectors,
             // assuming that point is sorted, it's bisector(p1, p2) and bisector(p2, p3)
             Edge e11 = vor.new Edge();
             Edge e12 = vor.new Edge();
             Edge e21 = vor.new Edge();
             Edge e22 = vor.new Edge();
 
             c1 = vor.new VorCell(p1, e11);
             c2 = vor.new VorCell(p2, e12);
             c3 = vor.new VorCell(p3, e22);
 
             Vert v1 = vor.new Vert(b1.findLowerPoint(bound), e11);
             Vert v2 = vor.new Vert(b1.findUpperPoint(bound), e12);
             Vert v3 = vor.new Vert(b3.findLowerPoint(bound), e21);
             Vert v4 = vor.new Vert(b3.findUpperPoint(bound), e22);
 
             e11.fill(v1, e12, c1, e11);
             e12.fill(v2, e11, c2, e12);
             e21.fill(v3, e22, c2, e21);
             e22.fill(v4, e21, c3, e22);
 
             vor.addEdge(e11);
             vor.addEdge(e12);
             vor.addEdge(e21);
             vor.addEdge(e22);
 
         } else {    // inter != null
 
             Edge e11 = vor.new Edge();
             Edge e12 = vor.new Edge();
             Edge e21 = vor.new Edge();
             Edge e22 = vor.new Edge();
             Edge e31 = vor.new Edge();
             Edge e32 = vor.new Edge();
 
             c1 = vor.new VorCell(p1, e11);
             c2 = vor.new VorCell(p2, e22);
             c3 = vor.new VorCell(p3, e31);
 
             // the three points bounding the bisectors in the rectangle
             Point q1, q2, q3;
             q1 = b1.findUpperPoint(bound);
             if (l1.findSide(p3) == l1.findSide(q1)) {
                 q1 = b1.findLowerPoint(bound);
             }
             q2 = b2.findUpperPoint(bound);
             if (l2.findSide(p2) == l2.findSide(q2)) {
                 q2 = b2.findLowerPoint(bound);
             }
             q3 = b3.findUpperPoint(bound);
             if (l3.findSide(p1) == l3.findSide(q3)) {
                 q3 = b3.findLowerPoint(bound);
             }
 
             Vert vInter = vor.new Vert(inter, e11);
             Vert v1 = vor.new Vert(q1, e11);
             Vert v2 = vor.new Vert(q2, e32);
             Vert v3 = vor.new Vert(q3, e22);
 
             e11.fill(v1, e21, c1, e12);
             e12.fill(vInter, e32, c1, e11);
             e21.fill(vInter, e11, c2, e22);
             e22.fill(v3, e31, c2, e21);
             e31.fill(vInter, e22, c3, e32);
             e32.fill(v2, e12, c3, e31);
 
             vor.addEdge(e11);
             vor.addEdge(e12);
             vor.addEdge(e21);
             vor.addEdge(e22);
             vor.addEdge(e31);
             vor.addEdge(e32);
         }
         vor.addFace(c1);
         vor.addFace(c2);
         vor.addFace(c3);
 
         return vor;
     }
 
     private static VoronoiDiagram handleNPointsVor(List<Point> points) {
         int splitIndex = points.size()/2;
         List<Point> L1 = points.subList(0, splitIndex);
         List<Point> L2 = points.subList(splitIndex, points.size());
 
         VoronoiDiagram vor1 = Voronoi.Vor(L1).vor;
         VoronoiDiagram vor2 = Voronoi.Vor(L2).vor;
 
         return mergeVor(vor1, vor2, ConvexHull.ConvexHullDivideAndConquer(points));
     }
 
     private static VoronoiDiagram mergeVor(VoronoiDiagram vor1,
                                            VoronoiDiagram vor2,
                                            HullResult hullResult) {
         VoronoiDiagram res = new VoronoiDiagram();
         Point u, v;
         u = hullResult.getUpperTangent().u;
         v = hullResult.getUpperTangent().v;
         Line l = Lines.findBisector(u, v);
 
         VorCell cr = null, cl = null;
         Iterator<Face> itFace = vor1.getFaceIterator();
         while (itFace.hasNext()) {
             VorCell cell = (VorCell)itFace.next();
             if (cell.getSite().equals(u)) {
                 cl = cell;
                 break;
             }
         }
         itFace = vor2.getFaceIterator();
         while (itFace.hasNext()) {
             VorCell cell = (VorCell)itFace.next();
             if (cell.getSite().equals(v)) {
                 cr = cell;
                 break;
             }
         }
         if (cl == null || cr == null) {
             System.err.println("Unable to find left or right cell");
             return null;
         }
 
         List<Point> divPoints = new ArrayList<Point>();
 
         Point lastDivPoint = l.findUpperPoint(bound);
         Edge lastDivEdge = vor1.new Edge(); // downward
         Edge twinLastDivEdge = vor1.new Edge();
 
         Vert v1 = vor1.new Vert(lastDivPoint, lastDivEdge);
         Vert v2 = vor1.new Vert(l.findLowerPoint(bound), twinLastDivEdge);
 
         lastDivEdge.fill(v1, twinLastDivEdge, cr, lastDivEdge);
         twinLastDivEdge.fill(v2, lastDivEdge, cl, twinLastDivEdge);
 
         res.addEdge(lastDivEdge);
         res.addEdge(twinLastDivEdge);
 
         Segment curSeg;
         Point interCr = null, interCl = null;
         divPoints.add(lastDivPoint);
         Edge eCr = cr.getEdge();
         Edge eCl = cl.getEdge();
         while (!(curSeg = new Segment(cl.getSite(), cr.getSite())).equals(hullResult.getLowerTangent())) {
             Segment ray = new Segment(lastDivPoint, l.findLowerPoint(bound));
             // find intersection between the dividing line and the right cell bounds
             do {
                 Segment s = new Segment(eCr.getOrigin().getPoint(), eCr.getTwin().getOrigin().getPoint());
                 if ((interCr = Lines.findIntersection(ray, s)) != null) {
                     if (interCr.isInRange(lastDivPoint)) {
                         interCr = null;
                     } else {
                         break;
                     }
                 }
                 eCr = eCr.getNext();
             } while (!eCr.equals(cr.getEdge()));
             // find intersection between the dividing line and the right cell bounds
             do {
                 Segment s = new Segment(eCl.getOrigin().getPoint(), eCl.getTwin().getOrigin().getPoint());
                 if ((interCl = Lines.findIntersection(ray, s)) != null) {
                     if (interCl.equals(lastDivPoint)) {
                         interCl = null;
                     } else {
                         break;
                     }
                 }
                 eCl = eCl.getNext();
             } while (!eCl.equals(cl.getEdge()));
             if (interCr == null && interCl == null) {
                 System.err.println("OMG : ray didn't find anything");
                 break;
             } else if (interCl == null || (interCr != null && interCl.y > interCr.y)) {
                 cr = (VorCell) eCr.getTwin().getFace();
                 l = Lines.findBisector(cl.getSite(), cr.getSite());
 
                 Edge newEdge = vor1.new Edge();
                 Edge twinNewEdge = vor1.new Edge();
 
                 Vert newVert = vor1.new Vert(interCr, newEdge);
                 Vert newEndDivEdge = vor1.new Vert(l.findLowerPoint(bound), twinNewEdge);
 
                 newEdge.fill(newVert, twinNewEdge, eCr.getTwin().getFace(), eCr.getTwin());
                 twinNewEdge.fill(newEndDivEdge, newEdge, lastDivEdge.getTwin().getFace(), lastDivEdge.getTwin());
                 lastDivEdge.getTwin().setOrigin(newVert);
                 lastDivEdge.setNext(eCr);
                 eCr.setNext(newEdge);
                 eCr.setOrigin(newVert);
 
                 res.addEdge(newEdge);
                 res.addEdge(twinNewEdge);
 
                 eCr = cr.getEdge();
                 lastDivEdge = newEdge;
                 lastDivPoint = interCr;
             } else if (interCr == null || (interCl != null && interCl.y < interCr.y)) {
                 cl = (VorCell) eCl.getTwin().getFace();
                 l = Lines.findBisector(cl.getSite(), cr.getSite());
                 Edge newEdge = vor1.new Edge();
                 Edge twinNewEdge = vor1.new Edge();
 
                 Vert newVert = vor1.new Vert(interCl, newEdge);
                 Vert newEndDivEdge = vor1.new Vert(l.findLowerPoint(bound), twinNewEdge);
 
                 newEdge.fill(newVert, twinNewEdge, eCl.getTwin().getFace(), lastDivEdge.getNext());
                 twinNewEdge.fill(newEndDivEdge, newEdge, eCl.getTwin().getFace(), eCl.getTwin());
                 lastDivEdge.getTwin().setOrigin(newVert);
                 lastDivEdge.setNext(newEdge);
                 eCl.setNext(lastDivEdge.getTwin());
                 eCl.getTwin().setOrigin(newVert);
 
                 res.addEdge(newEdge);
                 res.addEdge(twinNewEdge);
 
                 eCl = cl.getEdge();
                 lastDivEdge = newEdge;
                 lastDivPoint = interCl;
            } else {
                System.err.println("whut ? shouldnt have happened");
             }
             divPoints.add(lastDivPoint);
             interCr = interCl = null;
         }
         l = Lines.findBisector(curSeg.u, curSeg.v);
         divPoints.add(l.findLowerPoint(bound));
         res.lastDivideLine = divPoints;
 
         itFace = vor1.getFaceIterator();
         while (itFace.hasNext()) {
             res.addFace(itFace.next());
         }
         itFace = vor2.getFaceIterator();
         while (itFace.hasNext()) {
             res.addFace(itFace.next());
         }
         Iterator<Edge> itEdge = vor1.getEdgeIterator();
         while (itEdge.hasNext()) {
             res.addEdge(itEdge.next());
         }
         itEdge = vor2.getEdgeIterator();
         while (itEdge.hasNext()) {
             res.addEdge(itEdge.next());
         }
         return res;
     }
 
 }
