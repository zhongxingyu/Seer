 package computational_geometry.model.algorithms;
 
 import java.awt.Dimension;
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
 import computational_geometry.model.core.Utils;
 import computational_geometry.model.data_structures.DCEL.Edge;
 import computational_geometry.model.data_structures.DCEL.Face;
 import computational_geometry.model.data_structures.DCEL.Vert;
 import computational_geometry.model.data_structures.VoronoiDiagram;
 import computational_geometry.model.data_structures.VoronoiDiagram.VorCell;
 import computational_geometry.model.traces.HullResult;
 import computational_geometry.model.traces.VoronoiTrace;
 
 public class Voronoi {
 
     public static final Rectangle bound =
             new Rectangle(new java.awt.Point(Integer.MIN_VALUE/2, Integer.MIN_VALUE/2),
                           new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
 
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
 
         e1.fill(v1, e2, c1, e1, e1);
         e2.fill(v2, e1, c2, e2, e2);
 
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
         if (inter == null || !bound.contains(inter.x, inter.y)) {
             // two bisectors,
             // assuming that point is sorted, it's bisector(p1, p2) and bisector(p2, p3)
             Edge e11 = vor.new Edge();
             Edge e12 = vor.new Edge();
             Edge e21 = vor.new Edge();
             Edge e22 = vor.new Edge();
 
             Line bisector1, bisector2;
             Point q1, q2, q3, q4;
             boolean b = false;
             if (inter == null ||
                (inter.x > bound.x && inter.x < (bound.x + bound.width))) {
                 c1 = vor.new VorCell(p1, e11);
                 c2 = vor.new VorCell(p2, e12);
                 c3 = vor.new VorCell(p3, e22);
                 // here, qi are infinite
                 q1 = b1.findLowerPoint(bound);
                 q2 = b1.findUpperPoint(bound);
                 q3 = b3.findLowerPoint(bound);
                 q4 = b3.findUpperPoint(bound);
             } else {
                if ((p1.y < p2.y && p2.y < p3.y) ||
                    (b=(p1.y > p2.y && p2.y > p3.y))) {      // bisector(p1, p2) and bisector(p2, p3)
                    if (b) {
                        bisector1 = b3;
                        bisector2 = b1;
                        c1 = vor.new VorCell(p3, e11);
                        c3 = vor.new VorCell(p1, e22);
                    } else {
                        bisector1 = b1;
                        bisector2 = b3;
                        c1 = vor.new VorCell(p1, e11);
                        c3 = vor.new VorCell(p3, e22);
                    }
                    c2 = vor.new VorCell(p2, e12);
                } else if ((b=(p1.y < p2.y && p1.y > p3.y)) ||
                           (p1.y > p2.y && p1.y < p3.y)) {   // bisector(p1, p2) and bisector(p1, p3)
                     if (b) {
                         bisector1 = b2;
                         bisector2 = b1;
                         c1 = vor.new VorCell(p3, e11);
                         c3 = vor.new VorCell(p2, e22);
                     } else {
                         bisector1 = b1;
                         bisector2 = b2;
                         c1 = vor.new VorCell(p2, e11);
                         c3 = vor.new VorCell(p3, e22);
                     }
                     c2 = vor.new VorCell(p1, e12);
                 } else if ((b=(p3.y < p2.y && p3.y > p1.y)) ||
                            (p3.y > p2.y && p3.y < p1.y)) {  // bisector(p1, p3) and bisector(p2, p3)
                     if (b) {
                         bisector1 = b2;
                         bisector2 = b3;
                         c1 = vor.new VorCell(p1, e11);
                         c3 = vor.new VorCell(p2, e22);
                     } else {
                         bisector1 = b3;
                         bisector2 = b2;
                         c1 = vor.new VorCell(p2, e11);
                         c3 = vor.new VorCell(p1, e22);
                     }
                     c2 = vor.new VorCell(p3, e12);
                 } else {
                     System.err.println("Missing case in construction of diagram of 3 points.");
                     return null;
                 }
                if (inter.x < bound.getX()) {
                    q1 = q3 = inter;
                    q2 = new Point(bound.getWidth() + bound.getX(), bisector1.findY(bound.getWidth() + bound.getX()));
                    q4 = new Point(bound.getWidth() + bound.getX(), bisector2.findY(bound.getWidth() + bound.getX()));
                    q2.setInfinite(true);
                    q4.setInfinite(true);
                } else if (inter.x > bound.getX() + bound.getWidth()) {
                    q2 = q4 = inter;
                    q1 = new Point(bound.getX(), bisector1.findY(bound.getX()));
                    q3 = new Point(bound.getX(), bisector2.findY(bound.getX()));
                    q1.setInfinite(true);
                    q4.setInfinite(true);
                } else {
                    System.err.println("inter is out of bound but in ?? WTF");
                    return null;
                }
             }
             Vert v1 = vor.new Vert(q1, e11);
             Vert v2 = vor.new Vert(q2, e12);
             Vert v3 = vor.new Vert(q3, e21);
             Vert v4 = vor.new Vert(q4, e22);
 
             e11.fill(v1, e12, c1, e11, e11);
             e12.fill(v2, e11, c2, e21, e21);
             e21.fill(v3, e22, c2, e12, e12);
             e22.fill(v4, e21, c3, e22, e22);
 
             vor.addEdge(e11);
             vor.addEdge(e12);
             vor.addEdge(e21);
             vor.addEdge(e22);
 
         } else {    // inter != null <=> orientation(p1, p2, p3) != 0
 
             // edges naming convention :
             // e11 is the edge of cell c1 which origin is inter, e12 is it's twin
             // e21 is the edge of cell c2 which origin is inter, e22 is it's twin
             // e31 is the edge of cell c3 which origin is inter, e32 is it's twin
             Edge e11 = vor.new Edge();
             Edge e12 = vor.new Edge();
             Edge e21 = vor.new Edge();
             Edge e22 = vor.new Edge();
             Edge e31 = vor.new Edge();
             Edge e32 = vor.new Edge();
 
             // voronoi cells naming convention :
             // c1 is the cell whose site is p1
             // c2 is the cell whose site is p2
             // c3 is the cell whose site is p3
             c1 = vor.new VorCell(p1, e11);
             c2 = vor.new VorCell(p2, e21);
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
 
             Vert v0 = vor.new Vert(inter, e12);
             // vertex naming convention :
             // v1 is the infinite point in bisector(p1, p2),
             // v2 is the infinite point in bisector(p1, p3),
             // v3 is the infinite point in bisector(p3, p4),
             Vert v1, v2, v3;
             if (Utils.orientation(p1, p2, p3) < 0) {
                 v1 = vor.new Vert(q1, e11);
                 v2 = vor.new Vert(q2, e31);
                 v3 = vor.new Vert(q3, e21);
                 e11.fill(v1, e12, c1, e32, e32);
                 e12.fill(v0, e11, c2, e21, e21);
                 e21.fill(v3, e22, c2, e12, e12);
                 e22.fill(v0, e21, c3, e31, e31);
                 e31.fill(v2, e32, c3, e22, e22);
                 e32.fill(v0, e31, c1, e11, e11);
             } else {    // Utils.orientation(p1, p2, p3) > 0
                 v1 = vor.new Vert(q1, e21);
                 v2 = vor.new Vert(q2, e11);
                 v3 = vor.new Vert(q3, e31);
                 e11.fill(v2, e12, c1, e22, e22);
                 e12.fill(v0, e11, c3, e31, e31);
                 e21.fill(v1, e22, c2, e32, e32);
                 e22.fill(v0, e21, c1, e11, e11);
                 e31.fill(v3, e32, c3, e12, e12);
                 e32.fill(v0, e31, c2, e21, e21);
             }
 
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
 
         // the ray will be l bounded at y by rayUpperBound
         Point rayUpperBound = new Point(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
 
         List<ZipStep> zipSteps = new ArrayList<ZipStep>();
         Point interCr = null, interCl = null;
         while (!(new Segment(cl.getSite(), cr.getSite())).equals(hullResult.getLowerTangent())) {
             Edge eCr = cr.getEdge();
             Edge eCl = cl.getEdge();
             // find intersection between the dividing line and the right cell bounds
             do {
                 Segment s = new Segment(eCr.getOrigin().getPoint(), eCr.getTwin().getOrigin().getPoint());
                 if ((interCr = Lines.findIntersection(l, s)) != null) {
 //                    if ((Math.round(interCr.y) == Math.round(rayUpperBound.y) && Math.round(interCr.x) != Math.round(rayUpperBound.x)) || Math.round(interCr.y) > Math.round(rayUpperBound.y)) {
                     if (interCr.y <= rayUpperBound.y) {
                         interCr = null;
                     } else {
                         break;
                     }
                 }
                 eCr = eCr.getNext();
             } while (!eCr.equals(cr.getEdge()));
             // find intersection between the dividing line and the left cell bounds
             do {
                 Segment s = new Segment(eCl.getOrigin().getPoint(), eCl.getTwin().getOrigin().getPoint());
                 if ((interCl = Lines.findIntersection(l, s)) != null) {
 //                    if ((Math.round(interCl.y) == Math.round(rayUpperBound.y) && Math.round(interCl.x) != Math.round(rayUpperBound.x)) || Math.round(interCl.y) > Math.round(rayUpperBound.y)) {
                     if (interCl.y <= rayUpperBound.y) {
                         interCl = null;
                     } else {
                         break;
                     }
                 }
                 eCl = eCl.getNext();
             } while (!eCl.equals(cl.getEdge()));
             ZipStep step = new ZipStep();
             if (interCr == null && interCl == null) {
                 System.err.println("OMG : ray didn't find anything");
                 break;
             } else if (interCl == null || (interCr != null && interCl.y > interCr.y+2)) {
                 cr.setEdge(eCr);
                 cr = (VorCell) eCr.getTwin().getFace();
                 l = Lines.findBisector(cl.getSite(), cr.getSite());
 
                 step.eCl = null;
                 step.eCr = eCr;
                 step.inter = interCr;
 
                 eCr = eCr.getTwin();
                 cr.setEdge(eCr);
             } else if (interCr == null || (interCl != null && interCl.y+2 < interCr.y)) {
                 cl.setEdge(eCl);
                 cl = (VorCell) eCl.getTwin().getFace();
                 l = Lines.findBisector(cl.getSite(), cr.getSite());
 
                 step.eCl = eCl;
                 step.eCr = null;
                 step.inter = interCl;
 
                 eCl = eCl.getTwin();
                 cl.setEdge(eCl);
             } else {    // interCl = interCr
                 cl.setEdge(eCl);
                 cr.setEdge(eCr);
                 cl = (VorCell) eCl.getTwin().getFace();
                 cr = (VorCell) eCr.getTwin().getFace();
                 l = Lines.findBisector(cl.getSite(), cr.getSite());
 
                 step.eCl = eCl;
                 step.eCr = eCr;
                 step.inter = interCl;
 
                 eCl = cl.getEdge();
                 eCr = cr.getEdge();
                 cl.setEdge(eCl);
                 cr.setEdge(eCr);
             }
             zipSteps.add(step);
             rayUpperBound.x = (int) Math.ceil(step.inter.x);
             rayUpperBound.y = (int) Math.ceil(step.inter.y);//+1;
             interCr = interCl = null;
         }
 
         clipUnwantedEdges(zipSteps, vor1, vor2, hullResult);
 
         itFace = vor1.getFaceIterator();
         while (itFace.hasNext()) {
             Face f = itFace.next();
             res.addFace(f);
             Edge e = f.getEdge();
             do {
                 res.addEdge(e);
                 e = e.getNext();
             } while (!e.equals(f.getEdge()));
         }
         itFace = vor2.getFaceIterator();
         while (itFace.hasNext()) {
             Face f = itFace.next();
             res.addFace(f);
             Edge e = f.getEdge();
             do {
                 res.addEdge(e);
                 e = e.getNext();
             } while (!e.equals(f.getEdge()));
         }
         return res;
     }
 
     /**
      * Pass through the zip line to :
      *  - delete from vor1 the part to the right of the zip line,
      *  - delete from vor2 the part to the left of the zip line
      *  - update everything
      * @param zipSteps
      * @param vor1
      * @param vor2
      * @param hullResult
      */
     private static void clipUnwantedEdges(List<ZipStep> zipSteps, VoronoiDiagram vor1,
                                             VoronoiDiagram vor2, HullResult hullResult) {
         Point u, v;
         u = hullResult.getUpperTangent().u;
         v = hullResult.getUpperTangent().v;
         Line l = Lines.findBisector(u, v);
 
         Edge lastZipEdge = vor1.new Edge();
         Edge eTmp = vor1.new Edge();
 
         Vert v1 = vor1.new Vert(l.findUpperPoint(bound), lastZipEdge);
         Vert v2 = vor1.new Vert(l.findLowerPoint(bound), eTmp);
 
         VorCell cl = findCell(vor1, u);
         VorCell cr = findCell(vor2, v);
 
         lastZipEdge.fill(v1, eTmp, cr, null, null);
         eTmp.fill(v2, lastZipEdge, cl, null, null);
 
         // find first lastZipEdge's prev edge on cr bound
         eTmp = cr.getEdge();
         do {
             if (!eTmp.getNext().getOrigin().equals(eTmp.getTwin().getOrigin())) {
                 lastZipEdge.setPrev(eTmp);
                 eTmp.setNext(lastZipEdge);
                 break;
             }
             eTmp = eTmp.getNext();
         } while (!eTmp.equals(cr.getEdge()));
         if (lastZipEdge.getPrev() == null) {
             System.err.println("Cannot find first zip edge's prev.");
             return;
         }
         // find first twinLastZipEdge's next edge on cl bound
         eTmp = cl.getEdge();
         do {
             if (!eTmp.getOrigin().equals(eTmp.getPrev().getTwin().getOrigin())) {
                 lastZipEdge.getTwin().setNext(eTmp);
                 eTmp.setPrev(lastZipEdge.getTwin());
                 break;
             }
             eTmp = eTmp.getPrev();
         } while (!eTmp.equals(cl.getEdge()));
         if (lastZipEdge.getTwin().getNext() == null) {
             System.err.println("Cannot find first zip edge twin's next.");
             return;
         }
 
         for (ZipStep step : zipSteps) {
             Edge newEdge = vor1.new Edge();
             Edge twinNewEdge = vor1.new Edge();
 
             Vert newVert = vor1.new Vert(step.inter, newEdge);
             if (step.eCl != null && step.eCr == null) {
                 newEdge.fill(newVert, twinNewEdge, cr, null, lastZipEdge);
                 twinNewEdge.fill(null, newEdge, step.eCl.getTwin().getFace(), step.eCl.getTwin(), null);
                 lastZipEdge.setNext(newEdge);
                 lastZipEdge.getTwin().setOrigin(newVert);
                 lastZipEdge.getTwin().setPrev(step.eCl);
                 step.eCl.setNext(lastZipEdge.getTwin());
                 step.eCl.getTwin().setOrigin(newVert);
                 step.eCl.getTwin().setPrev(twinNewEdge);
                 cl = (VorCell) step.eCl.getTwin().getFace();
             }
             else if (step.eCr != null && step.eCl == null) {
                 newEdge.fill(newVert, twinNewEdge, step.eCr.getTwin().getFace(), null, step.eCr.getTwin());
                 twinNewEdge.fill(null, newEdge, cl, lastZipEdge.getTwin(), null);
                 lastZipEdge.setNext(step.eCr);
                 lastZipEdge.getTwin().setOrigin(newVert);
                 lastZipEdge.getTwin().setPrev(twinNewEdge);
                 step.eCr.setOrigin(newVert);
                 step.eCr.setPrev(lastZipEdge);
                 step.eCr.getTwin().setNext(newEdge);
                 cr = (VorCell) step.eCr.getTwin().getFace();
             }
             else {
                newEdge.fill(newVert, twinNewEdge, step.eCr.getTwin().getFace(), null, step.eCr.getTwin());
                twinNewEdge.fill(null, newEdge, step.eCl.getTwin().getFace(), step.eCl.getTwin(), null);
                 lastZipEdge.setNext(step.eCr);
                 lastZipEdge.getTwin().setOrigin(newVert);
                 lastZipEdge.getTwin().setPrev(step.eCl);
                 step.eCl.setNext(lastZipEdge.getTwin());
                 step.eCl.getTwin().setOrigin(newVert);
                 step.eCl.getTwin().setPrev(twinNewEdge);
                 step.eCr.setOrigin(newVert);
                 step.eCr.setPrev(lastZipEdge);
                 step.eCr.getTwin().setNext(newEdge);
                 cl = (VorCell) step.eCl.getTwin().getFace();
                 cr = (VorCell) step.eCr.getTwin().getFace();
             }
 
             lastZipEdge = newEdge;
         }
 
         // find last lastZipEdge's next edge on cr bound
         eTmp = cr.getEdge();
         do {
             if (!eTmp.getOrigin().equals(eTmp.getPrev().getTwin().getOrigin())) {
                 eTmp.setPrev(lastZipEdge);
                 lastZipEdge.setNext(eTmp);
                 break;
             }
             eTmp = eTmp.getPrev();
         } while (!eTmp.equals(cr.getEdge()));
         if (lastZipEdge.getPrev() == null) {
             System.err.println("Cannot find last zip edge's next.");
             return;
         }
         // find last twinLastZipEdge's prev edge on cl bound
         eTmp = cl.getEdge();
         do {
             if (!eTmp.getNext().getOrigin().equals(eTmp.getTwin().getOrigin())) {
                 eTmp.setNext(lastZipEdge.getTwin());
                 lastZipEdge.getTwin().setPrev(eTmp);
                 break;
             }
             eTmp = eTmp.getNext();
         } while (!eTmp.equals(cl.getEdge()));
         if (lastZipEdge.getTwin().getNext() == null) {
             System.err.println("Cannot find last zip edge twin's prev.");
             return;
         }
 
         // find last point (at infinity) of the zip line
         u = hullResult.getLowerTangent().u;
         v = hullResult.getLowerTangent().v;
         l = Lines.findBisector(u, v);
         Vert lastVert = vor1.new Vert(l.findLowerPoint(bound), lastZipEdge.getTwin());
         lastZipEdge.getTwin().setOrigin(lastVert);
     }
 
     /**
      * Find a cell in the voronoi diagram given a points
      * representing the site (generator) of the wanted cell
      * @param vor
      * @param site
      * @return
      */
     private static VorCell findCell(VoronoiDiagram vor, Point site) {
         Iterator<Face> it = vor.getFaceIterator();
         while (it.hasNext()) {
             VorCell cell = (VorCell)it.next();
             if (cell.getSite().equals(site)) {
                 return cell;
             }
         }
         return null;
     }
 
     /**
      * Embedded class representing a "step" during the process of merging
      * two voronoi diagrams.
      * @author Eloi
      *
      */
     static class ZipStep {
         private Point inter;
         private Edge eCr;
         private Edge eCl;
     }
 
 }
