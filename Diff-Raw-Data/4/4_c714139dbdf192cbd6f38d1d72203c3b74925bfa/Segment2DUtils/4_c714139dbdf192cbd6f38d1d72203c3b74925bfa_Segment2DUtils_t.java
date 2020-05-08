 /* (c) Copyright by Man YUAN */
 package net.epsilony.tb.solid;
 
 import java.awt.geom.Path2D;
 import java.util.Collection;
 import net.epsilony.tb.analysis.Math2D;
 
 /**
  *
  * @author <a href="mailto:epsilonyuan@gmail.com">Man YUAN</a> St-Pierre</a>
  */
 public class Segment2DUtils {
 
     public static double chordLength(Segment seg) {
         return Math2D.distance(seg.getStart().getCoord(), seg.getEnd().getCoord());
     }
 
     public static double[] chordMidPoint(Segment seg, double[] result) {
         return Math2D.pointOnSegment(seg.getStart().getCoord(), seg.getEnd().getCoord(), 0.5, result);
     }
 
     public static boolean isPointStrictlyAtChordLeft(Segment seg, double[] xy) {
         double[] startCoord = seg.getStart().coord;
         double[] endCoord = seg.getEnd().coord;
         double dhrX = endCoord[0] - startCoord[0];
         double dhrY = endCoord[1] - startCoord[1];
         double dx = xy[0] - startCoord[0];
         double dy = xy[1] - startCoord[1];
         double cross = Math2D.cross(dhrX, dhrY, dx, dy);
         return cross > 0 ? true : false;
     }
 
     public static boolean isPointStrictlyAtChordRight(Segment seg, double[] xy) {
         double[] startCoord = seg.getStart().coord;
         double[] endCoord = seg.getEnd().coord;
         double dhrX = endCoord[0] - startCoord[0];
         double dhrY = endCoord[1] - startCoord[1];
         double dx = xy[0] - startCoord[0];
         double dy = xy[1] - startCoord[1];
         double cross = Math2D.cross(dhrX, dhrY, dx, dy);
         return cross < 0 ? true : false;
     }
 
     public static double distanceToChord(Segment seg, double x, double y) {
         double[] v1 = seg.getStart().coord;
         double[] v2 = seg.getEnd().coord;
         double d12_x = v2[0] - v1[0];
         double d12_y = v2[1] - v1[1];
         double len12 = Math.sqrt(d12_x * d12_x + d12_y * d12_y);
         double d1p_x = x - v1[0];
         double d1p_y = y - v1[1];
         double project_len = Math2D.dot(d1p_x, d1p_y, d12_x, d12_y) / len12;
         if (project_len > len12) {
             double dx = x - v2[0];
             double dy = y - v2[1];
             return Math.sqrt(dx * dx + dy * dy);
         } else if (project_len < 0) {
             return Math.sqrt(d1p_x * d1p_x + d1p_y * d1p_y);
         } else {
             return Math.abs(Math2D.cross(d12_x, d12_y, d1p_x, d1p_y)) / len12;
         }
     }
 
     public static double distanceToChord(Segment seg, double[] pt) {
         return distanceToChord(seg, pt[0], pt[1]);
     }
 
     public static double maxChordLength(Iterable<? extends Segment> segments) {
         double maxLength = 0;
         for (Segment seg : segments) {
             double chordLength = chordLength(seg);
             if (chordLength > maxLength) {
                 maxLength = chordLength;
             }
         }
         return maxLength;
     }
 
     public static void link(Segment asPred, Segment asSucc) {
         asPred.setSucc(asSucc);
         asSucc.setPred(asPred);
     }
 
     public static Path2D genChordPath(Collection<? extends Segment> heads) {
         Path2D path = new Path2D.Double();
         for (Segment line : heads) {
             double[] startCoord = line.getStart().getCoord();
             path.moveTo(startCoord[0], startCoord[1]);
             SegmentIterator<Segment> lineIter = new SegmentIterator<>(line);
             Segment next;
             next = lineIter.next();
             while (lineIter.hasNext()) {
                 next = lineIter.next();
                 startCoord = next.getStart().getCoord();
                 path.lineTo(startCoord[0], startCoord[1]);
             }
             if (next.getSucc() != null) {
                 path.closePath();
             }
         }
 
         return path;
     }
 
     public static double[] chordVector(Segment seg, double[] result) {
         return Math2D.subs(seg.getEnd().getCoord(), seg.getStart().getCoord(), result);
     }
 
     public static double chordVectorDot(Segment seg1, Segment seg2) {
         double[] s1 = seg1.getStart().getCoord();
         double[] e1 = seg1.getEnd().getCoord();
         double[] s2 = seg2.getStart().getCoord();
         double[] e2 = seg2.getEnd().getCoord();
         double dx1 = e1[0] - s1[0];
         double dy1 = e1[1] - s1[1];
         double dx2 = e2[0] - s2[0];
         double dy2 = e2[1] - s2[1];
         return Math2D.dot(dx1, dy1, dx2, dy2);
     }
 
     public static double[] chordUnitOutNormal(Segment seg, double[] result) {
         double[] vec = chordVector(seg, result);
         double dx = vec[0];
         double dy = vec[1];
         double len = chordLength(seg);
        vec[0] = dy / len;
        vec[1] = -dx / len;
         return vec;
     }
 }
