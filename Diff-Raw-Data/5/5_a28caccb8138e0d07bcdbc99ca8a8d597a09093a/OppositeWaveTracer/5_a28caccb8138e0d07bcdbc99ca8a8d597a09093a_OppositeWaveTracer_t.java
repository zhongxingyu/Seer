 package general.tracers;
 
 import general.*;
 import general.Panel;
 
 import java.awt.*;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Vans
  * Date: 19.05.13
  * Time: 23:25
  * To change this template use File | Settings | File Templates.
  */
 public class OppositeWaveTracer implements Tracer {
 
 
     private static int[][] field = new int[Scheme.HEIGHT][Scheme.WIDTH];
     private int Ni = 0;//wave count
     private boolean finished = false;
     ArrayList<Point> points  = new ArrayList<Point>();
     public int finishX;
     public int finishY;
     @Override
     public Point[] getPath(int x1, int y1, int x2, int y2) {
 
         finishX = x2;
         finishY = y2;
 
         /* here we add points, which are already marked with wave
         * to spread next front of wave from them too*/
         //points.add(Panel.tiles[x1][y1]);
         //points.add(Panel.tiles[x2][y2]);
 
 
         points.add(new Point(x1,y1));
         /*Problem is that this loop is infinite - points.size seem to
         * increase constantly*/
 
 
         for (int i = 0; i<points.size(); i++)
         {
             makeWave(i);
             /*HashSet hashSet = new HashSet();
             hashSet.addAll(points);
             points.clear();
             points.addAll(hashSet); */
         }
 
      Point[] path = new Point[points.size()];
      return points.toArray(path);
     }
 
     private void makeWave (int index)
     {
         int x = (int) points.get(index).getX();
         int y = (int) points.get(index).getY();
       //  points.remove(index);
         int waveFront;
         if (Scheme.labels[x][y].equals(""))
        {
        	waveFront=0;
        	Scheme.labels[x][y]="0";
        }
         else
             waveFront = Integer.parseInt(Scheme.labels[x][y]);
 
         if (x+1 == finishX || y+1 == finishY || x-1 == finishX || y-1 == finishY)
             {
                 finished = true;
                 return;
             }
 
         if (Scheme.tiles[x+1][y] == TileType.EMPTY && x < Scheme.WIDTH
         	&& Scheme.labels[x+1][y] == "")
             {
                 Scheme.labels[x+1][y] = "" +(waveFront+1);
                 points.add(new Point(x+1,y));
             }
         if ( x > 0 && Scheme.tiles[x-1][y] == TileType.EMPTY
         	&& Scheme.labels[x-1][y] == "")
             {
                 Scheme.labels[x-1][y] = "" +(waveFront+1);
                 points.add(new Point(x-1,y));
             }
         if (Scheme.tiles[x][y+1] == TileType.EMPTY && y <Scheme.HEIGHT
         		&& Scheme.labels[x][y+1] == "")
             {
                 Scheme.labels[x][y+1] = "" + (waveFront+1);
                 points.add(new Point(x,y+1));
             }
         if ( y > 0 && Scheme.tiles[x][y-1] == TileType.EMPTY && Scheme.labels[x][y-1] == "")
             {
                 Scheme.labels[x][y-1] = "" +(waveFront+1);
                 points.add(new Point(x,y-1));
             }
     }
 }
