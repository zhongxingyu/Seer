 package player.millitta;
 
 
 public class Evaluate implements Constants {
     /*
         Masks for all 16 mills.
         Bits 1-24 are board position.
         Bits 24-31 are unused
      */
     private final static int mills[] = {
             7, // 0, 1, 2
             28, // 2, 3, 4
             112, // 4, 5, 6
             193, // 6, 7, 0
 
             1792, // 8, 9, 10
             7168, // 10, 11, 12
             28672, // 12, 13, 14
             49408, // 14, 15, 8
 
             459752, // 16, 17, 18
             1835008, // 18, 19, 20
             7340032, // 20, 21, 22
             12648448, // 22, 23, 16
 
             131586, // 1, 9, 17
             526344, // 3, 11, 19
             2105376, // 5, 13, 21
             8421504 // 7, 15, 23
     };
     private static final double[] Weighting = {
             1.0, // open mills
             3.0, // closed mills
             5.0, // Zwickmuehle
             1.5, // Gabeln
             1.2, // men
             1.2, // rest
             1.5, // moveable
             1.0  // Kreuzungen
     };
     private long boardState_ = 0L;
     /* board state for only the player to evaluate */
     private long playersBoard_ = 0L;
     private long boardWithoutMills = -1L;
 
     /*
          0-22 = Mens of White Player (0)
         24-47 = Mens of Black Player (1)
            48 = Player ID to evaluate
         49-51 = Game phase
     */
     public Evaluate(long boardState) {
         this.boardState_ = playersBoard_ = boardState;
 
         if ((boardState_ & (1L << BIT_PLAYER)) != 0) {
             playersBoard_ = boardState_ & ~(long) (Math.pow(2, 24) - 1); // Alles bis Bit 24 löschen
            playersBoard_ &= (long) (Math.pow(2, 48) - 1) << 24; // Player 1 Daten auf Player 0 Datenposition verschieben
         }
     }
 
     public double getFitness() {
         double fitness = 0.f;
 
         fitness += Weighting[WEIGHT_OPEN_MILL] * getOpenMills();
         fitness += Weighting[WEIGHT_CLOSED_MILL] * getClosedMills();
 
         return fitness;
     }
 
     /*
         Es existieren insgesamt 16 mögliche Mühlen.
         Für jede Mühle wird eine 24 Bit-Maske angelegt und die entsprechenden Bits gemäß des Spielfeldes gesetzt.
         Auf diese Masken kann dann eine logische UND-Verknüpfung mit den Bits der gesetzten Steine
         des entsprechenden Spielers angewandt werden.
 
         Sind bei einer Maske noch 3 Bits gesetzt, so hat der Spieler dort eine geschlossene Mühle.
         Durch Abzählen, bei wievielen der 16 Masken dies vorkommt,
         weiß man wieviele Mühlen der Spieler insgesamt hat.
      */
     public int getClosedMills() {
         int closedMills = 0;
         boardWithoutMills = playersBoard_;
 
         for (int mill : mills) {
             if (Long.bitCount(playersBoard_ & mill) == 3) {
                 closedMills++;
                 boardWithoutMills &= ~(mill);
             }
         }
 
         return closedMills;
     }
 
     /*
         Genau wie beim Erkennen der geschlossenen Mühlen werden hier die Steine des Spielers mit allen 16 Masken
         logisch mit UND-Verknüpft. Eine offene Mühle liegt genau dann vor,
         wenn 2 Bits in einer verknüpften Maske noch gesetzt sind.
 
         Solange der auszuwertende Spieler noch Steine zum Setzen hat oder springen kann,
         reicht diese Bedingung schon aus. Ansonsten muss geprüft werden, ob ein angrenzedes Feld zu dem Feld,
         in dem der Stein zur fertigen Mühle fehlt, auch ein Stein des selben Spielers liegt.
         Dieser Stein darf dann natürlich nicht selber schon Teil der gerade betrachteten offenen Mühle sein.
      */
     public int getOpenMills() {
         int openMills = 0;
         final long mask_move_phase = 4L << BIT_PHASE; // 0b100
 
         calcBoardWithoutMills();
 
         for (int mill : mills) {
             if (Long.bitCount(playersBoard_ & mill) == 2) {
                 if ((playersBoard_ ^ mask_move_phase) == 0) { // Zugphase
                     openMills++;
                 } else { // Angrenzend bewegbarer Stein, der nicht in einer Mühle ist
                     long holePos = Math.round(Math.log(playersBoard_ ^ mill) / LOG2);
                     long boardWithoutOpenAndClosedMills = boardWithoutMills & ~(mill);
 
                     long nextPos = holePos + 1;
                     long prevPos = holePos - 1;
                    if (nextPos % 8 == 0) { // "Überlauf"
                         nextPos -= 8;
                     } else if (prevPos == -1 || prevPos % 8 == 7) {
                         prevPos += 8;
                     }
 
                     // Davor oder danach liegt noch ein Stein, der weder in einer geschlossenen
                     // Mühle, noch in genau der offenen die wir uns gerade angucken, liegt.
                     if ((boardWithoutOpenAndClosedMills & ((1L << prevPos) | (1L << nextPos))) != 0) {
                         openMills++;
                         continue; // mehr anliegende Steine brauchen wir uns hier nicht anschauen
                     }
                     if (holePos % 2 == 1) { // Fehlender Steine in einer Kreuzung Ecke
                         nextPos = holePos + 8;
                         if (nextPos < 24) {
                             if ((boardWithoutOpenAndClosedMills & (1L << nextPos)) != 0) {
                                 openMills++;
                                 continue;
                             }
                         }
                         prevPos = prevPos - 8;
                         if (prevPos > 0) {
                             if ((boardWithoutOpenAndClosedMills & (1L << prevPos)) != 0) {
                                 openMills++;
                                 // continue; // Nicht notwendig
                             }
                         }
                     }
                 }
             }
         }
 
         return openMills;
     }
 
     /*
         Erweiterung des Algorithmus zum erkennen von offenen Mühlen.
         Hier muss nur der angrenzende Stein selbst schon in einer Mühle sein.
      */
     public int getDoubleMills() {
         int doubleMills = 0;
 
 
         return doubleMills;
     }
 
     private void calcBoardWithoutMills() {
         if (boardWithoutMills == -1) {
             getClosedMills();
         }
     }
 
 
 }
