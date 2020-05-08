 /*
  * Copyright (C) 2012 brweber2
  */
 package com.brweber2.conway;
 
 public class BoardPrinter
 {
     final static String ESC = "\033[";
 
     private final Board board;
 
     public BoardPrinter( Board board )
     {
         this.board = board;
     }
 
     private void clearOldBoard()
     {
         // stealing *nix trick from Ed :)
        System.err.print(ESC + "2J"); System.err.flush();
     }
 
     public void print()
     {
         clearOldBoard();
         int padding = 2;
         BoundingBox boundingBox = board.getBoundingBox();
         for ( int y = boundingBox.getMinY() - padding; y <= boundingBox.getMaxY() + padding; y++ )
         {
             for ( int x = boundingBox.getMinX() - padding; x <= boundingBox.getMaxX() + padding; x++ )
             {
                 if ( board.containsKey( new Coordinate( x, y ) ) )
                 {
                     System.err.print( "*" );
                 }
                 else if ( x == boundingBox.getMinX() - padding || x == boundingBox.getMaxX() + padding
                         || y == boundingBox.getMinY() - padding || y == boundingBox.getMaxY() + padding
                         )
                 {
                     System.err.print( "." );
                 }
                 else
                 {
                     System.err.print( " " );
                 }
             }
             System.err.println();
         }
         System.err.println();
     }
 }
