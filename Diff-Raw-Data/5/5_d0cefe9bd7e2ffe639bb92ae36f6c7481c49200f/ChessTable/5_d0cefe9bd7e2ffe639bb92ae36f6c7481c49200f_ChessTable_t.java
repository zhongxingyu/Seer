 package Code;
 
 import java.awt.BorderLayout;
 import java.awt.Component;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JPanel;
 
 public class ChessTable {
 
     private PieceLabel[] table;
     private String[] log;
     private PieceLabel[][] twoTable;
 
     public ChessTable() {
         this.table = new PieceLabel[64];
         this.log = new String[2];
         this.twoTable = new PieceLabel[8][8];
     }
 
     public void updateTable(PieceLabel piece, int indeks) {
         table[indeks] = piece;
     }
     public void newTable(PieceLabel[] table2){
         table = table2;
     }
     public PieceLabel[] getTable(){
         return table;
     }
 
     public void testTwoTable() {
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 if (twoTable[i][j] instanceof PieceLabel) {
                     System.out.println("i: " + i + " j: " + j + " " + twoTable[i][j].getPiece());
                 }
             }
         }
     }
 
     public void updateTwoTable() {
         int a = 0;
         for (int i = 0; i < 8; i++) {
             for (int j = 0; j < 8; j++) {
                 twoTable[i][j] = table[a];
                 a++;
 
             }
         }
     }
 
     public void updateLog(String log2, int indeks) {
         log[indeks] = log2;
     }
 
     public String getLog(int index) {
         return log[index];
     }
 
     public Component getTable(int index) {
         if (table[index] instanceof PieceLabel) {
             return table[index];
         } else {
             JPanel square = new JPanel(new BorderLayout());
             square.setOpaque(false);
             return square;
         }
     }
 
     public void reset() {
         for (int i = 0; i < table.length; i++) {
             table[i] = null;
         }
     }
 
     public void changeUI(int a) {
         switch (a) {
             case 1:
                 for (int i = 0; i < 64; i++) {
                     if (table[i] instanceof PieceLabel) {
                         if (table[i].getPiece() instanceof PawnW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/OkayguyW.png")));
                         }
                         if (table[i].getPiece() instanceof PawnB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/OkayguyB.png")));
                         }
                         if (table[i].getPiece() instanceof QueenW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/FmercuryW.png")));
                         }
                         if (table[i].getPiece() instanceof QueenB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/FmercuryB.png")));
                         }
                         if (table[i].getPiece() instanceof KnightW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/TrollfaceW.png")));
                         }
                         if (table[i].getPiece() instanceof KnightB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/TrollfaceB.png")));
                         }
                         if (table[i].getPiece() instanceof KingW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/YaomingW.png")));
                         }
                         if (table[i].getPiece() instanceof KingB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/YaomingB.png")));
                         }
                         if (table[i].getPiece() instanceof BishopW) {
                            table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/LolW.png")));
                         }
                         if (table[i].getPiece() instanceof BishopB) {
                            table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/LolB.png")));
                         }
                         if (table[i].getPiece() instanceof RookW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/MegustaW.png")));
                         }
                         if (table[i].getPiece() instanceof RookB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/MegustaB.png")));
                         }
                     }
                 }
                 break;
             case 2:
                 for (int i = 0; i < 64; i++) {
                     if (table[i] instanceof PieceLabel) {
                         if (table[i].getPiece() instanceof PawnW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/PawnW.png")));
                         }
                         if (table[i].getPiece() instanceof PawnB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/PawnB.png")));
                         }
                         if (table[i].getPiece() instanceof QueenW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/QueenW.png")));
                         }
                         if (table[i].getPiece() instanceof QueenB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/QueenB.png")));
                         }
                         if (table[i].getPiece() instanceof KnightW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/KnightW.png")));
                         }
                         if (table[i].getPiece() instanceof KnightB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/KnightB.png")));
                         }
                         if (table[i].getPiece() instanceof KingW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/KingW.png")));
                         }
                         if (table[i].getPiece() instanceof KingB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/KingB.png")));
                         }
                         if (table[i].getPiece() instanceof BishopW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/BishopW.png")));
                         }
                         if (table[i].getPiece() instanceof BishopB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/BishopB.png")));
                         }
                         if (table[i].getPiece() instanceof RookW) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/RookW.png")));
                         }
                         if (table[i].getPiece() instanceof RookB) {
                             table[i].setIcon(new ImageIcon(getClass().getResource("/Pictures/RookB.png")));
                         }
                     }
                 }
                 break;
         }
     }
 
     public boolean checkW(int i) {
         if (checkBishopW(i)) {
             System.out.println("Bishop");
             return true;
         }
         if (checkRookW(i)) {
             System.out.println("ROOK");
             return true;
         }
         if (checkKnightW(i)) {
             System.out.println("KNIGHT");
             return true;
         }
         if (checkPawnW(i)) {
             System.out.println("PAWN");
             return true;
         }
         if (checkKingW(i)) {
             System.out.println("KING");
             return true;
         }
         return false;
     }
 
     public boolean checkB(int i) {
         if (checkBishopB(i)) {
             System.out.println("Bishop");
             return true;
         }
         if (checkRookB(i)) {
             System.out.println("ROOK");
             return true;
         }
         if (checkKnightB(i)) {
             System.out.println("KNIGHT");
             return true;
         }
         if (checkPawnB(i)) {
             System.out.println("PAWN");
             return true;
         }
         if (checkKingB(i)) {
             System.out.println("KING");
             return true;
         }
         return false;
     }
 
     public boolean checkKnightW(int i) {
         if ((i + 15) <= 63 && (i + 15) >= 0) {
             if (table[i + 15] instanceof PieceLabel) {
                 if (table[i + 15].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i + 6) <= 63 && (i + 6) >= 0) {
             if (table[i + 6] instanceof PieceLabel) {
                 if (table[i + 6].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i - 10) <= 63 && (i + -10) >= 0) {
             if (table[i - 10] instanceof PieceLabel) {
                 if (table[i - 10].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i - 17) <= 63 && (i - 17) >= 0) {
             if (table[i - 17] instanceof PieceLabel) {
                 if (table[i - 17].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i - 15) <= 63 && (i - 15) >= 0) {
             if (table[i - 15] instanceof PieceLabel) {
                 if (table[i - 15].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i + 10) <= 63 && (i + 10) >= 0) {
             if (table[i + 10] instanceof PieceLabel) {
                 if (table[i + 10].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i + 17) <= 63 && (i + 17) >= 0) {
             if (table[i + 17] instanceof PieceLabel) {
                 if (table[i + 17].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         if ((i - 6) <= 63 && (i - 6) >= 0) {
             if (table[i - 6] instanceof PieceLabel) {
                 if (table[i - 6].getPiece() instanceof KnightB) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean checkPawnW(int i) {
         if ((i - 7) <= 63 && (i - 7) >= 0) {
             if (table[i - 7] instanceof PieceLabel) {
                 if (table[i - 7].getPiece() instanceof PawnB) {
                     return true;
                 }
             }
         }
         if ((i - 9) <= 63 && (i - 9) >= 0) {
             if (table[i - 9] instanceof PieceLabel) {
                 if (table[i - 9].getPiece() instanceof PawnB) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean checkRookW(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         for (int j = b; j < 8; j++) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 if (twoTable[a][j].getPiece() instanceof RookB || twoTable[a][j].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[a][j].getPiece() instanceof RookB) == false && (twoTable[a][j].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         for (int j = b; j >= 0; j--) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 if (twoTable[a][j].getPiece() instanceof RookB || twoTable[a][j].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[a][j].getPiece() instanceof RookB) == false && (twoTable[a][j].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         for (int j = a; j < 8; j++) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookB || twoTable[j][b].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookB) == false && (twoTable[j][b].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         for (int j = a; j >= 0; j--) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookB || twoTable[j][b].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookB) == false && (twoTable[j][b].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
         }
         return false;
     }
 
     public boolean checkBishopW(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c > 0 && d > 0) {
                 c--;
                 d--;
             }
         }
         c = a;
         d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c < 7 && d < 7) {
                 c++;
                 d++;
             }
         }
         c = a;
         d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c < 7 && d > 0) {
                 c++;
                 d--;
             }
         }
         c = a;
         d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopB || twoTable[c][d].getPiece() instanceof QueenB) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopB) == false && (twoTable[c][d].getPiece() instanceof KingW) == false) {
                     break;
                 }
             }
             if (c > 0 && d < 7) {
                 c--;
                 d++;
             }
         }
         return false;
     }
 
     public boolean checkKingW(int i) {
         if ((i - 7) <= 63 && (i - 7) >= 0) {
             if (table[i - 7] instanceof PieceLabel) {
                 if (table[i - 7].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i - 8) <= 63 && (i - 8) >= 0) {
             if (table[i - 8] instanceof PieceLabel) {
                 if (table[i - 8].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i - 9) <= 63 && (i - 9) >= 0) {
             if (table[i - 9] instanceof PieceLabel) {
                 if (table[i - 9].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i - 1) <= 63 && (i - 1) >= 0) {
             if (table[i - 1] instanceof PieceLabel) {
                 if (table[i - 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i + 1) <= 63 && (i + 1) >= 0) {
             if (table[i + 1] instanceof PieceLabel) {
                 if (table[i + 1].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i + 7) <= 63 && (i + 7) >= 0) {
             if (table[i + 7] instanceof PieceLabel) {
                 if (table[i + 7].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i + 8) <= 63 && (i + 8) >= 0) {
             if (table[i + 8] instanceof PieceLabel) {
                 if (table[i + 8].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         if ((i + 9) <= 63 && (i + 9) >= 0) {
             if (table[i + 9] instanceof PieceLabel) {
                 if (table[i + 9].getPiece() instanceof KingB) {
                     return true;
                 }
             }
         }
         return false;
 
     }
 
     public boolean checkKnightB(int i) {
         if ((i + 15) <= 63 && (i + 15) >= 0) {
             if (table[i + 15] instanceof PieceLabel) {
                 if (table[i + 15].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i + 6) <= 63 && (i + 6) >= 0) {
             if (table[i + 6] instanceof PieceLabel) {
                 if (table[i + 6].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i - 10) <= 63 && (i + -10) >= 0) {
             if (table[i - 10] instanceof PieceLabel) {
                 if (table[i - 10].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i - 17) <= 63 && (i - 17) >= 0) {
             if (table[i - 17] instanceof PieceLabel) {
                 if (table[i - 17].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i - 15) <= 63 && (i - 15) >= 0) {
             if (table[i - 15] instanceof PieceLabel) {
                 if (table[i - 15].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i + 10) <= 63 && (i + 10) >= 0) {
             if (table[i + 10] instanceof PieceLabel) {
                 if (table[i + 10].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i + 17) <= 63 && (i + 17) >= 0) {
             if (table[i + 17] instanceof PieceLabel) {
                 if (table[i + 17].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         if ((i - 6) <= 63 && (i - 6) >= 0) {
             if (table[i - 6] instanceof PieceLabel) {
                 if (table[i - 6].getPiece() instanceof KnightW) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean checkPawnB(int i) {
         if ((i + 7) <= 63 && (i + 7) >= 0) {
             if (table[i + 7] instanceof PieceLabel) {
                 if (table[i + 7].getPiece() instanceof PawnW) {
                     return true;
                 }
             }
         }
         if ((i + 9) <= 63 && (i + 9) >= 0) {
             if (table[i + 9] instanceof PieceLabel) {
                 if (table[i + 9].getPiece() instanceof PawnW) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     public boolean checkRookB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         for (int j = b; j < 8; j++) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 if (twoTable[a][j].getPiece() instanceof RookW || twoTable[a][j].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[a][j].getPiece() instanceof RookW) == false && (twoTable[a][j].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         for (int j = b; j >= 0; j--) {
             if (twoTable[a][j] instanceof PieceLabel) {
                 if (twoTable[a][j].getPiece() instanceof RookW || twoTable[a][j].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[a][j].getPiece() instanceof RookW) == false && (twoTable[a][j].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         for (int j = a; j < 8; j++) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookW || twoTable[j][b].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookW) == false && (twoTable[j][b].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         for (int j = a; j >= 0; j--) {
             if (twoTable[j][b] instanceof PieceLabel) {
                 if (twoTable[j][b].getPiece() instanceof RookW || twoTable[j][b].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[j][b].getPiece() instanceof RookW) == false && (twoTable[j][b].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
         }
         return false;
     }
 
     public boolean checkBishopB(int i) {
         int a = 0;
         int b = 0;
         if (i > 7) {
             a = i / 8;
             b = i - (a * 8);
         } else {
             b = i;
             a = 0;
         }
         int c = a;
         int d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c > 0 && d > 0) {
                 c--;
                 d--;
             }
         }
         c = a;
         d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c < 7 && d < 7) {
                 c++;
                 d++;
             }
         }
         c = a;
         d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c < 7 && d > 0) {
                 c++;
                 d--;
             }
         }
         c = a;
         d = b;
         for (int j = 0; j < 8; j++) {
             if (twoTable[c][d] instanceof PieceLabel) {
                 if (twoTable[c][d].getPiece() instanceof BishopW || twoTable[c][d].getPiece() instanceof QueenW) {
                     return true;
                 }
                 if ((twoTable[c][d].getPiece() instanceof BishopW) == false && (twoTable[c][d].getPiece() instanceof KingB) == false) {
                     break;
                 }
             }
             if (c > 0 && d < 7) {
                 c--;
                 d++;
             }
         }
         return false;
     }
 
     public boolean checkKingB(int i) {
         if ((i - 7) <= 63 && (i - 7) >= 0) {
             if (table[i - 7] instanceof PieceLabel) {
                 if (table[i - 7].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i - 8) <= 63 && (i - 8) >= 0) {
             if (table[i - 8] instanceof PieceLabel) {
                 if (table[i - 8].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i - 9) <= 63 && (i - 9) >= 0) {
             if (table[i - 9] instanceof PieceLabel) {
                 if (table[i - 9].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i - 1) <= 63 && (i - 1) >= 0) {
             if (table[i - 1] instanceof PieceLabel) {
                 if (table[i - 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i + 1) <= 63 && (i + 1) >= 0) {
             if (table[i + 1] instanceof PieceLabel) {
                 if (table[i + 1].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i + 7) <= 63 && (i + 7) >= 0) {
             if (table[i + 7] instanceof PieceLabel) {
                 if (table[i + 7].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i + 8) <= 63 && (i + 8) >= 0) {
             if (table[i + 8] instanceof PieceLabel) {
                 if (table[i + 8].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         if ((i + 9) <= 63 && (i + 9) >= 0) {
             if (table[i + 9] instanceof PieceLabel) {
                 if (table[i + 9].getPiece() instanceof KingW) {
                     return true;
                 }
             }
         }
         return false;
 
     }
 }
