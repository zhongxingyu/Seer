 package viewer;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 
 import common.Vector3D;
 
 
 public class DepthComparator implements Comparator<DrawPoints>
 {
 	private ArrayList<Vector3D> points;
 	public DepthComparator(ArrayList<Vector3D> p)
 	{
 		points = new ArrayList<Vector3D>(p);
 	}
 	public int compare(DrawPoints p1, DrawPoints p2)
 	{
 		double[] zs = new double[2];
 		zs[0] = 0;
 		zs[1] = 0;
 		for(int i = 0; i < p1.getPoints().size(); i++)
 		{
 			zs[0] += points.get(p1.getPoints().get(i)).get(2);
 		}
 		zs[0] /= p1.getPoints().size();
 		for(int i = 0; i < p2.getPoints().size(); i++)
 		{
 			zs[1] += points.get(p2.getPoints().get(i)).get(2);
 		}
 		zs[1] /= p2.getPoints().size();
		return (int)(zs[0]-zs[1]);
 	}
 }
