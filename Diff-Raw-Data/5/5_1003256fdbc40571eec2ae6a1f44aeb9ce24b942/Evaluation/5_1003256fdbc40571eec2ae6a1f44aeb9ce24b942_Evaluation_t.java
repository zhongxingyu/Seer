 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package itschess;
 
 /**
  *
  * @author Ian
  */
 public class Evaluation {
     
     public static double eval(Board chessboard){
         byte[][] board = chessboard.board;
         
         double k = 0;
         double kb = 0;
         double q =0;
         double qb = 0;
         double r =0;
         double rb = 0;
         double b =0;
         double bb = 0;
         double n =0;
         double nb = 0;
         double p =0;
         double pb = 0;
         double d = 0;
         double db = 0;
         double kingBonus = 0;
         double knightBonus = 0;
         double pawnBonus = 0;
         double kingEndBonus = 0;
         double bishopBonus = 0;
         double kingPenalty = 0;
         double knightPenalty = 0;
         double pawnPenalty = 0;
         double kingEndPenalty = 0;
         double bishopPenalty = 0;
 
        for(int i = 0; i < 8; i ++)
         {
            for(int j = 0; j < 8; j++)
             {
                 if(board[i][j] == 0)
                         continue;
                 else if(board[i][j] == 1)
                 {
                         k ++;
                         k += KingTable[(i)*8 + (j)];
                 }
                 else if(board[i][j] == -1)
                 {
                         kb ++;
                         kb += KingTable[Math.abs(i-7)* 8 + Math.abs(j-7)];//have to flip positions for black pieces
                         // kingPenalty = KingTable[(i+1)*(j+1)];
 
                 }
                 else if(board[i][j] == 2)
                 {
                         q ++;
                 }
                 else if(board[i][j] == -2)
                 {
                         qb ++;
                 }
                 else if(board[i][j] == 3)
                 {
                         r ++;
                 }
                 else if(board[i][j] == -3)
                 {
                         rb ++;
                 }
                 else if(board[i][j] == 4)
                 {
                         b ++;
                         b += BishopTable[(i)*8 + (j)];
                 }
                 else if(board[i][j] == -4)
                 {
                         bb ++;
                         bb += BishopTable[Math.abs(i-7)* 8 + Math.abs(j-7)];
                         //  bishopBonus -= BishopTable[(i+1)*(j+1)];
                 }
                 else if(board[i][j] == 5)
                 {
                         n ++;
                         n += KnightTable[(i)*8 + (j)];
                 }
                 else if(board[i][j] == -5)
                 {
                         nb ++;
                         nb += KnightTable[Math.abs(i-7)* 8 + Math.abs(j-7)];
                         //  knightBonus -= KnightTable[(i+1)*(j+1)];
                 }
                 else if(board[i][j] == 6)
                 {
                         p ++;
                         p += PawnTable[(i)*8 + (j)];
                         if (i > 0 && board[i-1][j] == 6)
                                 d ++;
                 }
                 else if(board[i][j] == -6)
                 {
                         pb ++;
                         pb += PawnTable[Math.abs(i-7)* 8 + Math.abs(j-7)];
                         //  pawnBonus -= PawnTable[(i+1)*(j+1)];
                         if (i < 6 && board[i+1][j] == -6)
                                 db ++;
                 }
             }	
         }
         double evalNum = 200*(k-kb) + 9*(q-qb) + 5*(r-rb) +
         		3*((b-bb) + (n-nb)) + (p-pb) - .5*(d-db) + chessboard.boardScore;
 
         return evalNum;
     }
 
     private static double[] PawnTable = new double[]
     {
                         0,   0,  0,  0,  0,  0,  0,  0,
                         .5,  .5, .5, .5, .5, .5, .5, .5,
                         .1,  .1, .2, .3, .3, .2, .1, .1,
                         .05, .05, .1, .27, .27, .1,  .05, .05,
                         0,   0,  0, .25, .25,  0,  0,  0,
                         .05, -.05,-.1,  0,  0,-.1, -.05,  .05,
                         .05, .1, .1,-.25,-.25, .1, .1,  .05,
                         0,  0,  0,  0,  0,  0,  0,  0
     };
 
     private static double[] KnightTable = new double[]
     {
                         -.5,-.4,-.3,-.3,-.3,-.3,-.4,-.5,
                         -.4,-.2,  0,  0,  0,  0,-.2,-.4,
                         -.3,  0, .1, .15, .15, .1,  0,-.3,
                         -.3,  .05, .15, .2, .2, .15,  .05,-.3,
                         -.3,  0, .15, .2, .2, 15,  0,-.3,
                         -.3,  .05, .1, .15, .15, .1,  .05,-.3,
                         -.4,-.2,  0,  .05,  .05,  0,-.2,-.4,
                         -.5,-.4,-.2,-.3,-.3,-.2,-.4,-.5
     };
 
     private static double[] KingTable = new double[]
     {
                         -.3, -.4, -.4, -.5, -.5, -.4, -.4, -.3,
                         -.3, -.4, -.4, -.5, -.5, -.4, -.4, -.3,
                         -.3, -.4, -.4, -.5, -.5, -.4, -.4, -.3,
                         -.3, -.4, -.4, -.5, -.5, -.4, -.4, -.3,
                         -.2, -.3, -.3, -.4, -.4, -.3, -.3, -.2,
                         -.1, -.2, -.2, -.2, -.2, -.2, -.2, -.1, 
                         .2,  .2,   0,   0,   0,   0,  .2,  .2,
                         .2,  .3,  .1,   0,   0,  .1,  .3,  .2
     };
     private static double[] KingTableEndGame = new double[]
     {
                         -.5,-.4,-.3,-.2,-.2,-.3,-.4,-.5,
                         -.3,-.2,-.1,  0,  0,-.1,-.2,-.3,
                         -.3,-.1, .2, .3, .3, .2,-.1,-.3,
                         -.3,-.1, .3, .4, .4, .3,-.1,-.3,
                         -.3,-.1, .3, .4, .4, .3,-.1,-.3,
                         -.3,-.1, .20, .3, .3, .20,-.1,-.3,
                         -.3,-.3,  0,  0,  0,  0,-.3,-.3,
                         -.5,-.3,-.3,-.3,-.3,-.3,-.3,-.5
     };
 
     private static double[] BishopTable = new double[]
     {
                         -.20,-.10,-.10,-.10,-.10,-.10,-.10,-.20,
                         -.10,  0,  0,  0,  0,  0,  0,-.10,
                         -.10,  0,  .05, .10, .10,  .05,  0,-.10,
                         -.10,  .05,  .05, .10, .10,  .05,  .05,-.10,
                         -.10,  0, .10,.10, .10, .10,  0,-.10,
                         -.10, .10, .10, .10, .10, .10, .10,-.10,
                         -.10,  .05,  0,  0,  0,  0,  .05,-.10,
                         -.20,-.10,-.40,-.10,-.10,-.40,-.10,-.20
     };
 
     public static byte pieceValue(byte piece)
     {
         if(Math.abs(piece) == 3)
         {
                 return 5;
         }
         else if(Math.abs(piece) == 2)
         {
                 return 9;
         }
         else if(Math.abs(piece) == 1)
         {
                 return 125;
         }
         else if(Math.abs(piece) == 4 || Math.abs(piece) == 5)
         {
                 return 3;
         }
         else if(Math.abs(piece) == 6)
         {
                 return 1;
         }
         return 0;
 
     }
     
 }
