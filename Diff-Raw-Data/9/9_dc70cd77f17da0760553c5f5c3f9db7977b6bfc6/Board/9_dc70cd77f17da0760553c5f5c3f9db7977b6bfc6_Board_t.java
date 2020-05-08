 import java.util.ArrayList;
 
 public class Board
 {
     private int max_x = Constants.MAXX;
     private int max_y = Constants.MAXY;
     private ArrayList<ArrayList<String>> landed = new ArrayList<ArrayList<String>>();
 
     public Board()
     {
         for(int x = 0; x < max_x; x++)
         {
             landed.add(new ArrayList<String>());
             for(int y = 0; y < max_y; y++)
             {
                 landed.get(x).add(new String());
             }
         }
     }
 
     public void output()
     {
         for(int y = 0; y < max_y; y++)
         {
             for(int x = 0; x < max_x; x++)
             {
                 String square = landed.get(x).get(y);
                 if(square.isEmpty())
                 {
                     System.out.print(". ");
                 }
                 else
                 {
                     System.out.print(square.charAt(0) + " ");
                 }
             }
             System.out.println();
         }
     }
 
     public String getSquare(int x, int y)
     {
         return landed.get(x).get(y);
     }
 
     private boolean isValid(Coordinate coordinate)
     {
         // first check if it's not out of bounds
         if(coordinate.x < 0 || coordinate.x >= max_x || coordinate.y < 0 || coordinate.y >= max_y)
         {
             return false;
         } // then check if it's already occupied by a spot on the board
         else if(!landed.get(coordinate.x).get(coordinate.y).isEmpty())
         {
             return false;
         }
         else
         {
             return true;
         }
     }
 
 
     public boolean areValid(ArrayList<Coordinate> coordinates)
     {
         for(Coordinate coordinate : coordinates)
         {
             if(!isValid(coordinate))
             {
                 return false;
             }
         }
         return true;
     }
 
     public boolean areEmpty(ArrayList<Coordinate> coordinates) // weaker check than areValid
     {
         for(Coordinate coordinate : coordinates)
         {
             if(!landed.get(coordinate.x).get(coordinate.y).isEmpty())
             {
                 return false;
             }
         }
         return true;
     }
 
     public void addBlocksAt(ArrayList<Coordinate> coordinates, String color)
     {
         for(Coordinate coordinate : coordinates)
         {
             landed.get(coordinate.x).set(coordinate.y, color);
         }
     }
 
     private void removeRow(int row)
     {
         for(int y = row; y > 0; y--)
         {
             for(int x = 0; x < max_x; x++)
             {
                 landed.get(x).set(y, landed.get(x).get(y - 1));
             }
         }
         for(int x = 0; x < max_x; x++) // clear top row
         {
             landed.get(0).set(x, new String());
         }
     }
 
     private boolean isRowFull(int row)
     {
         for(int x = 0; x < max_x; x++)
         {
             if(landed.get(x).get(row).isEmpty())
             {
                 return false;
             }
         }
         return true;
     }
 
     public ArrayList<Integer> clearCompletedRows()
     {
         ArrayList<Integer> removedRows = new ArrayList<Integer>();
        for(int y = 0; y < max_y; y++)
         {
             if(isRowFull(y))
             {
                 removedRows.add(y);
             }
         }
        for(int y : removedRows)
        {
            removeRow(y);
        }
         return removedRows;
     }
 }
