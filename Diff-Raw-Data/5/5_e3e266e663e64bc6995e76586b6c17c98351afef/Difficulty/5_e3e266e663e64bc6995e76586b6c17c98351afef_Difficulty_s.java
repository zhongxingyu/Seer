 package net.obnoxint.adsz.memory;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.lwjgl.util.Point;
 
 enum Difficulty {
 
     _3(0, 3, 2),
     _4(1, 4, 2),
     _5(2, 5, 2),
     _6(3, 4, 3),
     _7(4, 7, 2),
     _8(5, 4, 4),
     _9(6, 6, 3),
     _10(7, 5, 4),
    _12(8, 6, 5),
     _14(9, 7, 4),
    _15(10, 5, 5),
     _16(11, 8, 4),
     _18(12, 6, 6),
     _20(13, 8, 5),
     _21(14, 7, 6),
     _24(15, 8, 6),
     _28(16, 8, 7);
 
     private static final int MIN_ID = 0;
     private static final int MAX_ID = 16;
 
     private static final Map<Integer, Difficulty> idMap = new HashMap<>();
 
     static {
         for (final Difficulty v : values()) {
             idMap.put(v.id, v);
         }
     }
 
     static Difficulty getNext(final Difficulty difficulty) {
         return idMap.get(difficulty.id == MAX_ID ? MIN_ID : difficulty.id + 1);
     }
 
     static Difficulty getPrevious(final Difficulty difficulty) {
         return idMap.get(difficulty.id == MIN_ID ? MAX_ID : difficulty.id - 1);
     }
 
     private final int id;      // internal id
 
     private Point ul = null;   // upper left Point of the first card
 
     final int hCount;          // number of cards per row
     final int vCount;          // number of cards per column
 
     private Difficulty(final int id, final int hCount, final int vCount) {
         this.id = id;
         this.hCount = hCount;
         this.vCount = vCount;
     }
 
     int count() {
         return hCount * vCount;
     }
 
     Point upperLeft() {
         if (ul == null) {
             final int w = Main.DISPLAY_WIDTH;
             final int h = Main.DISPLAY_HEIGHT;
             final int b = Main.GAME_BORDER;
             final int m = Main.GAME_CARD_MARGIN;
             final int s = Main.GAME_CARD_SIZE;
             final int ha = w - (b * 2);
             final int va = h - (b * 2);
             final int hp = (s * hCount) + (m * (hCount - 1));
             final int vp = (s * vCount) + (m * (vCount - 1));
             ul = new Point(((ha - hp) / 2) + b, ((va - vp) / 2) + b);
         }
         return ul;
     }
 
     Point upperLeftOf(final int x, final int y) { // the first card is x = 0, y = 0
         final int s = Main.GAME_CARD_SIZE + Main.GAME_CARD_MARGIN;
         return new Point(ul.getX() + (x * s), ul.getY() + (y * s));
     }
 
 }
