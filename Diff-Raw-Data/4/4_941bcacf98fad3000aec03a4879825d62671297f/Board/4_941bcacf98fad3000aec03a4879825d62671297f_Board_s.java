 package player.millitta;
 
 
 abstract public class Board implements Constants, algds.Constants {
     protected long board;
 
     protected Board(long board) {
         this.board = board;
     }
 
     /************************
      *  Aktionen und Phasen *
      ************************/
     static public long setRemoveAction(long board) {
         return board | (1L << BIT_ACTION) | (1L << (BIT_ACTION + 1));
     }
 
     static public boolean isRemoveAction(long board) {
         return ((board & (1L << BIT_ACTION)) != 0L) && ((board & (1L << (BIT_ACTION+1))) != 0L);
     }
 
     static public long setNoAction(long board) {
         return board & ~((1L << BIT_ACTION) | (1L << (BIT_ACTION + 1)));
     }
 
     /*
         Wenn das positionieren einer Spielfigur auf Position 'at' jetzt eine geschlossene Muehle wird,
         dann bin ich immer noch an der Reihe, ansonsten muessen die Spieler gewechselt werden.
         Achtung: Passt auch automatisch die Aktion auf "Remove" an wenn es sein muss.
     */
     public long changePlayerIfNecessary(long board, int at) {

         if( !isMyMenInOpenMill(at) ) {
             board = switchPlayer(board);
            board = setNoAction(board);
         } else { // Naechse Aktion ist jemanden vom Board zu kicken
             board = setRemoveAction(board);
         }
         return board;
     }
 
     /*
         Speichert die neue Spielphase im Board ab.
 
         Achtung: Muss aufgerufen werden, nachdem neuer Zug schon ausgefuehrt wurde und
                  ggf. auch schon die Spieler IDs getauscht wurden.
                  (aka nach 'changePlayerIfNecessary')
     */
     public long getBoardWithNewPhase(long board) {
         // Es sind noch Spielfiguren zu setzen
         //   -> Setzphase
         if( getMyRest(board) > 0 ) {
             board |=  (1L <<  BIT_PHASE);
             board &= ~(1L << (BIT_PHASE+1));
 
         // 3 oder weniger Spieler noch auf dem Board und nicht in der Setzphase
         //   -> Flugphase
         } else if ( getMyMenOnBoard(board) <= 3 ) {
             board |= (1L <<  BIT_PHASE) | (1L << (BIT_PHASE+1));
         } else { // Zugphase
             board &= ~( (1L <<  BIT_PHASE) | (1L << (BIT_PHASE+1)));
         }
 
 
         return board;
     }
 
 
     /************************
      *  Spieler unabhaengig *
      ************************/
     public int getFreePoss() {
         return 24 - Long.bitCount((board & BITS_MENS1) | ((board & BITS_MENS2) >> 24));
     }
 
     static public long switchPlayer(long board) {
         return board ^ (1L << BIT_PLAYER);
     }
 
 
     /*********************************
      *  Spieler der an der Reihe ist *
      *********************************/
     protected int getMyMenOnBoard() {
         return Helper.getMyMenOnBoard(board);
     }
 
     protected int getMyMenOnBoard(long board) {
         return Helper.getMyMenOnBoard(board);
     }
 
     protected long setMyMan(int at) {
         if ((board & (1L << BIT_PLAYER)) != 0L) {
             long rest = ((board >> BIT_REST2) & 15) - 1;
             return ((board | (1L << (at + 24))) & ~(15L << BIT_REST2)) | (rest << BIT_REST2);
         } else {
             long rest = ((board >> BIT_REST1) & 15) - 1;
             return ((board | (1L << at)) & ~(15L << BIT_REST1)) | (rest << BIT_REST1);
         }
     }
 
     protected boolean isMyMen(int at) {
         if ((board & (1L << BIT_PLAYER)) != 0L) {
             return (board & (1L << (24 + at))) != 0L;
         } else {
             return (board & (1L << at)) != 0L;
         }
     }
 
     protected boolean isMyMenInOpenMill(int at) {
         long tmpBoard = board;
         if ((board & (1L << BIT_PLAYER)) != 0L) {
             tmpBoard >>= 24;
         }
 
         for( int mill : LookupTable.millAt[at] ) {
             if (Long.bitCount(tmpBoard & mill) == 3) {
                 if ((mill & (1L << at)) != 0L) {
                     return true;
                 }
             }
         }
         return false;
     }
 
     protected boolean isMen(int at) {
         return (board & ((1L << (24 + at)) | (1L << at))) != 0L;
     }
 
 
     protected long moveMyMen(int from, int to) {
         if ((board & (1L << BIT_PLAYER)) != 0L) {
             from += 24;
             to += 24;
         }
         return (board | (1L << to)) & ~(1L << from);
     }
 
 
     protected long getMyRest(long board) {
         if ((board & (1L << BIT_PLAYER)) == 0L) {
             return (board >> BIT_REST1) & 15;
         } else {
             return (board >> BIT_REST2) & 15;
         }
     }
 
 
     /******************
      *  Gegenspieler *
      *****************/
     protected int getOppMenOnBoard() {
         return Long.bitCount(board & BITS_MENS2);
     }
 
 
     /*
         Entfernen einer Spielfigur des Gegners.
         Testet nicht ob das ein valider Zug ist!
     */
     protected long removeOppMan(int pos) {
         if ((board & (1L << BIT_PLAYER)) == 0L) {
             return board & ~(1L << (pos + 24));
         } else {
             return board & ~(1L << pos);
         }
     }
 
     /*
         Testet ob das entfernen einer gegnerisches Figur an Position pos
         vom Spielbrett moeglich (erlaubt) ist.
      */
     protected boolean isRemoveOppManPossible(int pos) {
         // Wenn dort keine Figur steht, kann man sie auch nicht entfernen.
         if (!isOppMen(pos)) {
             return false;
         }
 
         // Wenn mehr als drei gegnerische Figuren auf dem Spielbrett sind,
         // dann darf die zu entfernende Figur nicht in einer Muehle sein.
         if (getOppMenOnBoard() >= 3) {
             if (isOppMenInMill(pos)) {
                 return false;
             }
         }
 
         return true;
     }
 
     protected boolean isOppMen(int at) {
         if ((board & (1L << BIT_PLAYER)) == 0L) {
             return (board & (1L << (24 + at))) != 0L;
         } else {
             return (board & (1L << at)) != 0L;
         }
     }
 
     protected boolean isOppMenInMill(int pos) {
         long tmpBoard = board;
         if ((board & (1L << BIT_PLAYER)) == 0L) {
             tmpBoard >>= 24;
         }
 
         for (int mill : LookupTable.millAt[pos]) {
             if (Long.bitCount(tmpBoard & mill) == 3) {
                 if ((mill & (1L << pos)) != 0L) {
                     return true;
                 }
             }
         }
         return false;
     }
 }
