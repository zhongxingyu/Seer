 package olap.it.edu.itba.tp;
 
 import com.vividsolutions.jts.geom.Coordinate;
 import com.vividsolutions.jts.geom.Geometry;
 import com.vividsolutions.jts.geom.GeometryFactory;
 import com.vividsolutions.jts.geom.Point;
 import com.vividsolutions.jts.io.ParseException;
 import com.vividsolutions.jts.io.WKBReader;
 import com.vividsolutions.jts.operation.distance.DistanceOp;
 
 public class Centroid {
 	public static String getUnion(String a, String b){
 		if(b == null && a == null){
 			return null;
 		}else if( b == null){
 			return a;
 		}else if( a == null ){
 			return b;
 		}
 		WKBReader info = new WKBReader();
 		Geometry elem1;
 		Geometry elem2;
 		try {
 			
 			elem1 = info.read(WKBReader.hexToBytes(a));
 			elem2 = info.read(WKBReader.hexToBytes(b));
 			return elem1.union(elem2).toString();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return a;
 	}
 	
 	public static String getCentroid(String a) throws ParseException{ 
 		if(a == null){
 			return a;
 		}
 		WKBReader info = new WKBReader();
 		Geometry elem;
 		elem = info.read(WKBReader.hexToBytes(a));
		return getCloser(elem.union(), elem.getCentroid()).toString();
 	}
 	
 	public static Geometry getCloser(Geometry elem, Point point){
 		Coordinate[] elems = DistanceOp.nearestPoints(elem, point);
 		GeometryFactory data = new GeometryFactory();
 		return data.createPoint(elems[0]);
 	}
 }
