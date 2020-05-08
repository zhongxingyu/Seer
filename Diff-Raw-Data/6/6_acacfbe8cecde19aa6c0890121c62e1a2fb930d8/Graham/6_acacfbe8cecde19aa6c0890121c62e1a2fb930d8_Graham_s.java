 import java.util.*;
 import java.awt.geom.*;
 
public class graham{
 
 	public double ccw(point a,point b, point c)
 	{
 		return (b.x-a.x)*(c.y-a.y)-(b.y-a.y)*(c.x-a.x);
 	}
 	
 	public void doGraham(ArrayList <point> points)
 	{
 		int num=points.size();
 		ArrayList <point> increasePoints=new ArrayList <point> ();
 		point lowest=points.get(0);
 		increasePoints.add(new point());
 		for(point p : points)
 			increasePoints.add(p);
 		int j=0;
 		for(int i=0;i<points.size();i++)
 		{
 			if(increasePoints.get(i).y<lowest.y){
 				j=i;
 				lowest=increasePoints.get(i);
 			}
 		}
 		
 		Collections.swap(increasePoints,1,j);
 		for(int temp=2;temp<increasePoints.size();temp++)
 		{
 			increasePoints.get(temp).radian=Math.atan((increasePoints.get(1).y-increasePoints.get(temp).y)/(increasePoints.get(1).x-increasePoints.get(temp).x));
 			
 		}
 		Collections.sort(increasePoints);
 		
 		increasePoints.set(0,points.get(num));
 		
 		int m=1;
 		for (int i=2;i<=num;i++)
 		{
 			while (ccw(increasePoints.get(m-1),increasePoints.get(m),increasePoints.get(i))<=0)
 			{
 				if(m>1)
 					m-=1;
 				else if(i==num)
 					break;
 				else
 					i+=1;
 			}
 			m++;
 			Collections.swap(increasePoints,m,i);
 		}
 	}
 	public class point implements Comparable<point>{
 		double x;
 		double y;
 		double radian;
 		
 		public int compareTo(point other)
 		{
 			return (int) (this.radian-other.radian);
 		}
 	}
}
