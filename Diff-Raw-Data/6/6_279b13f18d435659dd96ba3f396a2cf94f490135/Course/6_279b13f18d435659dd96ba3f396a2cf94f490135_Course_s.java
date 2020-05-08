 package edu.stanford;
 
 import com.google.android.maps.GeoPoint;
 import java.util.ArrayList;
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.File;
 import java.io.IOException;
 import android.util.Log;
 
 public class Course {
 	private ArrayList<CoursePoint> course;
 	private CoursePoint selectedPoint;
 	
 	public CoursePoint getSelectedPoint() {
 		return selectedPoint;
 	}
 	public void setSelectedPoint(CoursePoint pt) {
 		selectedPoint = pt;
 	}
 	
 	
 	
 	//The coordinate frames as defined by me.
 	//The google maps frame is the real world, the true frame
 	//    -All of the course points (the course variable) are in here
 	//myStartPoint is the start point of the current GPS operation session,
 	//     to be loaded at the start of the run or when the human hits the
 	//     reset button. This is the origin of the current run in the google
 	//     maps frame. Subtracting the difference between this and the
 	//     course start point gets you the location in the course.
 	
 	
 	public Course() {
 		course = new ArrayList<CoursePoint>();
 		selectedPoint = null;
 	}
 	
 	
 	public CoursePoint getStartPoint() {
 		for (CoursePoint p : course) {
 			if (p.getType() == CoursePoint.Type.START) {
 				return p;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns null if no points in course. Returns previous point
 	 * or first point if pt is not in the course.
 	 */
 	public CoursePoint getPreviousPoint(CoursePoint pt) {
 		if (course.indexOf(pt) == -1) {
 			if (course.size() != 0) {
 				return course.get(0);
 			}
 		} else if (course.indexOf(pt) == 0) {
 			return course.get(course.size() - 1);
 		} else {
 			return course.get(course.indexOf(pt) - 1);
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns null if no points in course. Returns previous point
 	 * or first point if pt is not in the course.
 	 */
 	public CoursePoint getNextPoint(CoursePoint pt) {
 		if (course.indexOf(pt) == -1) {
 			if (course.size() != 0) {
 				return course.get(0);
 			}
 		} else {
 			return course.get((course.indexOf(pt) + 1) % course.size());
 		}
 		return null;
 	}
 	
 	public void reorderCoursePoint(CoursePoint pt, int delta) {
 		if (course.indexOf(pt) == -1) {
 			return;
 		}
 		int n = course.indexOf(pt) + delta;
 		if (n < 0) {
 			course.remove(pt);
 			course.add(pt);
		} else {
 			course.remove(pt);
 			course.add(n, pt);
 		}
 	}
 	
 	
 	public void add(int i, CoursePoint pt) {
 		course.add(i, pt);
 	}
 	public void add(CoursePoint pt) {
 		course.add(pt);
 	}
 	public CoursePoint get(int i) {
 		return course.get(i);
 	}
 	public int size() {
 		return course.size();
 	}
 	public void remove(CoursePoint pt) {
 		course.remove(pt);
 	}
 	
 	public void save(File currentCourse) {
 		try {
 			BufferedWriter w = new BufferedWriter(new FileWriter(currentCourse));
 			w.write("SELECT," + course.indexOf(selectedPoint) + "\r\n");
 			for (CoursePoint p : course) {
 				w.write(p.getType() + "," + p.getLocation().getLatitudeE6() 
 					+ "," + p.getLocation().getLongitudeE6() + "\r\n");
 			}
 			w.close();
 		} catch (IOException ex) {
 			//FIXME: error!!!
 		}
 	}
 	
 	public void load(File currentCourse) {
 		try {
 			BufferedReader r = new BufferedReader(new FileReader(currentCourse));
 			String l = r.readLine();
 			int selected = 0;
 			course = new ArrayList<CoursePoint>();
 			while (l != null) {
 				String[] s = l.split(",");
 				for (String t : s) {
 					Log.i("Course", "S: '" + t + "'");
 				}
 				if (s.length == 0) {
 					//Blank
 				} else if (s[0].equals("SELECT")) {
 					selected = Integer.parseInt(s[1]);
 				} else if (s[0].equals("START") || s[0].equals("CONE") || s[0].equals("WAYPOINT")) {
 					CoursePoint p = new CoursePoint(CoursePoint.Type.valueOf(s[0]),
 									Integer.parseInt(s[1]),
 									Integer.parseInt(s[2]));
 					course.add(p);
 					
 				} else {
 					Log.e("Course", "Bad line: '" + l + "'");
 				}
 				l = r.readLine();
 			}
 			selectedPoint = course.get(selected);
 		} catch (IOException ex) {
 			//FIXME: error!!!
 		}
 	}
 	
 	
 }
 
