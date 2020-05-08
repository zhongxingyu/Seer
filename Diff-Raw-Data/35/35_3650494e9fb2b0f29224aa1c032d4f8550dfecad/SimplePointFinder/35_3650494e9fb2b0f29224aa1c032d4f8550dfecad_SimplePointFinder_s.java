 package main;
 
 import java.awt.Point;
 import java.awt.geom.Point2D;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 public class SimplePointFinder implements PointFinder {
 	
 	private Set<Segment> segments;
 
 	@Override
 	public int findPoint(Point2D a, Point2D b, Point2D c) {
 		double d = a.getX() * (b.getY() - c.getY()) - a.getY()
 				* (b.getX() - c.getX()) + b.getX() * c.getY() - b.getY()
 				* c.getX();
 		
 		if (d < 0)
 			return -1;
		if (d == 0)
			return 0;
 		return 1;
 	}
 
 	@Override
 	public List<Set<Point2D>> separatePoints(Set<Point2D> pointSet, Point2D a,
 			Point2D b) {
 		List<Set<Point2D>> separatedSets = new ArrayList<>(3);
 		Set<Point2D> leftSet = new HashSet<>();
 		Set<Point2D> rightSet = new HashSet<>();
 		Set<Point2D> onPathSet = new HashSet<>();
 		
 		Iterator<Point2D> iter = pointSet.iterator();
 		while (iter.hasNext()) {
 			Point2D p = iter.next();
 			int res = findPoint(a, b, p);
 			if (res == -1)
 				rightSet.add(p);
 			else if (res == 0)
 				onPathSet.add(p);
 			else
 				leftSet.add(p);
 		}
 		
 		separatedSets.add(leftSet);
 		separatedSets.add(rightSet);
 		separatedSets.add(onPathSet);
 		return separatedSets;
 	}
 
 	@Override
 	public int areCrossed(Point2D a, Point2D b, Point2D c, Point2D d) {
 		if (isOnLine(a, b, c) || isOnLine(a, b, d))
 			return 0;
 		int posA = findPoint(c, d, a);
 		int posB = findPoint(c, d, b);
 		int posC = findPoint(a, b, c);
 		int posD = findPoint(a, b, d);
 		if (posC != posD && posA != posB)
 			return 1;
 		return -1;	
 	}
 	
 	public boolean isOnLine(Point2D a, Point2D b, Point2D c) {
 		int pos = findPoint(a, b, c);
 		if (pos != 0)
 			return false;
 		if ((Math.min(a.getX(), b.getX()) <= c.getX()) && 
 			(c.getX() <= Math.max(a.getX(), b.getX())) && 
 			(Math.min(a.getY(), b.getY()) <= c.getY()) && 
 			(c.getY() <= Math.max(a.getY(), b.getY())))
 			return true;
 		return false;
 	}
 
 	@Override
 	public int countCrossings(Set<Point2D> pointSet) {
 		int count = 0;
 		segments = new HashSet();
 		for (Point2D p : pointSet) {
 			for (Point2D inP : pointSet) {
 				if (!p.equals(inP)) {
 					segments.add(new Segment(p, inP));
 				}
 			}
 		}
 		for (Segment s : segments) {
 			for (Segment inS : segments) {
 				if (!s.equals(inS)) {
 					if (areCrossed(s.getStart(), s.getEnd(), inS.getStart(), inS.getEnd()) == 1)
 						++count;
 				}
 			}
 		}
 		return count;
 	}
 
 	@Override
	public int findColinearNum(List<Segment> ps, Point2D start) {
 		int num = 0;
		for (Segment p : ps) {
			if (findPoint(start, p.getStart(), p.getEnd()) == 0)
 				num++;
 		}
 		return num;
 	}
 	
 	private List<Segment> makeSegment(List<Point2D> ps) {
 		List<Segment> segmets = new ArrayList<>();
 		for (int i=0; i<ps.size(); i++) {
 			for (int j=i; j<ps.size(); j++) {
 				if (ps.get(i) != ps.get(j))
 					segmets.add(new Segment(ps.get(i), ps.get(j)));
 			}
 		}
 		return segmets;
 	}
 
 	@Override
 	public List<Point2D> parseCsv(String f) {
 		List<Point2D> ps = new ArrayList<>();
 		try {
 			BufferedReader br = new BufferedReader(new FileReader(f));
 			String line = br.readLine();
 			while (line != null) {
 				System.out.println(line);
 				System.out.println(ps.size());
 				int idx = Integer.parseInt(line.split(";")[0].trim());
 				float x = Float.parseFloat(line.split(";")[1].replace(",", ".").trim());
 				float y = Float.parseFloat(line.split(";")[2].replace(",", ".").trim());
 				Point2D p = new MyPoint(idx, x, y);
 				ps.add(p);
 				line = br.readLine();
 			}
 		} catch (Exception e) {
 			System.err.println("Error occured: " + e.getMessage());
 		}
 		return ps;
 	}
 
 }
