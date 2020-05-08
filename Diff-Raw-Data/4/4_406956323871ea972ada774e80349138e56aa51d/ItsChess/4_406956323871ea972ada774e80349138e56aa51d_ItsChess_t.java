 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package itschess;
 
 /**
  *
  * @author Ian
  */
 public class ItsChess {
 
     /**
      * @param args the command line arguments
      */
     static byte depth = 1;
     static byte[][] possibleMoves;
     
     public void alphaBetaSearch(Board board)
     {
         byte a = -100; byte b = 100;//should these be ints or Integers?
         int v = maxValue(board, a, b);
         
     }
     
     public byte maxValue(Board board, byte a, byte b)
     {//a = alpha, b = beta
         byte v = -100;//initial max value
         byte mv = -99;//holds the current max
         for(int i=0; i<possibleMoves.length; i++)
         {
             if(depth != 3)
             {//Begining by testing only to depth 3
                 board.move();
                 depth++;
                 mv = minValue(board, a, b);
             }
             else
                 mv = utility(board);
             if(mv > v)
                 v = mv;
             if(v >= b)      
             {
                 board.undo();
                 return v;  
             }
             if(v > a)
                 a = v;
             
             board.undo();
         }
         
         depth--;
         return v;
     }
     
     public byte minValue(Board board, byte a, byte b)
     {//a = alpha, b = beta
         byte v = 100;//initial min value
         byte mv = 99;//holds the current min
         for(int i=0; i<possibleMoves.length; i++)
         {
             if(depth != 3)
             {//Begining by testing only to depth 3
                 board.move();
                 depth++;
                 mv = maxValue(board, a, b);
             }
             else
                 mv = utility(board);
             if(mv < v)
                 v = mv;
             if(v <= a)      {
                 board.undo();
                 return v;   }
             if(v < b)
                 b = v;
             board.undo();
         }
        
         depth--;
         return v;
     }
     
     public byte utility(Board board)
     {
         //return 200(k) + 9(q) + 5(r) + 3(b+n) + (p) - 0.5(d+s+i) + 0.1(m);
         return 0;
     }
     
     //eval function chess //google search
     
     
     public static void main(String[] args) {
         
         Board chessboard = new Board();
         chessboard.analysis();
         System.out.println(chessboard.toString());
         
     }
 }
