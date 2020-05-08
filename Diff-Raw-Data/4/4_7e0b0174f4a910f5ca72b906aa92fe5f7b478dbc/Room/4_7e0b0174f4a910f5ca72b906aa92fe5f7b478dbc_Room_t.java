 package edu.brown.cs32.jcadler.GameLogic.RogueMap;
 
 import java.awt.geom.Rectangle2D;
 import java.awt.geom.Point2D;
 import java.awt.Point;
 import java.util.List;
 import java.util.ArrayList;
 
 import edu.brown.cs32.goingrogue.map.Space;
 import edu.brown.cs32.goingrogue.map.Tile;
 import edu.brown.cs32.goingrogue.map.Wall;
 
 /**
  *
  * @author john
  */
 public class Room implements Space
 {
     private Rectangle2D.Double room;
     private String id;
     private List<Corridor> connections;
     
     public Room(Point p, int w, int h, String i)
     {
         room=new Rectangle2D.Double(p.getX(),p.getY(),w,h);
         connections = new ArrayList<>();
         id=i;
     }
     
     public void addCorridor(Corridor c)
     {
         connections.add(c);
     }
     
     public String getID()
     {
         return id;
     }
     
     public int getWidth()
     {
         return (int)room.getWidth();
     }
     
     public int getHeight()
     {
         return (int)room.getHeight();
     }
     
     public int width()
     {
         return getWidth();
     }
     
     public int height()
     {
         return getHeight();
     }
     
     public Point upperLeft()
     {
         return new Point((int)room.getX(),(int)room.getY());
     }
     
     public Tile[][] getFloor()
     {
         int x = (int)room.getWidth();
         int y = (int)room.getHeight();
         Tile[][] ret = new Tile[x][y];
         for(int i=0;i<x;i++)
         {
             for(int j=0;j<y;j++)
                 ret[i][j]=Tile.GROUND;
         }
         return ret;
     }
     
     public Wall getWallType()
     {
         return Wall.DEFAULT;
     }
     
     public int getX()
     {
         return (int)room.getX();
     }
     
     public int getY()
     {
         return (int)room.getY();
     }
     
     public boolean isValid(Point2D.Double p)
     {
        if(p.getX()<=room.getX() && p.getX()<=room.getX()+room.getWidth()&&
           p.getY()<=room.getY() && p.getY()<=room.getY()+room.getHeight())
             return true;
         for(Corridor c : connections)
         {
             if(c.isValid(p))
                 return true;
         }
         return false;
     }
     
     public List<Corridor> getCorridors()
     {
         return connections;
     }
 }
