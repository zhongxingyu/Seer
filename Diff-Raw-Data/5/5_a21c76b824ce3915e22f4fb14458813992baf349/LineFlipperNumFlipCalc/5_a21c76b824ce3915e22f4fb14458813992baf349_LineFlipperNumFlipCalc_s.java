 package fastboard.lineflipper.calc;
 
 import fastboard.checkmove.calc.FastCheckCalc;
 import fastboard.checkmove.linedecoder.LineDecoder;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ed
  * Date: Jan 10, 2010
  * Time: 11:54:09 AM
  * This class calculates how many pieces would be flip given a certain line configuration
  */
 public class LineFlipperNumFlipCalc {
 
     /**
      * Calculates how many pieces get flipped given a configuration
      *
      * @return an int[][] for the results. Result[index][line] means how  many discs you would flip for a given
      *         index and line configuration
      */
     public NumFlip[][] calcNumFlipForBlack() {
         NumFlip[][] ret = new NumFlip[FastCheckCalc.squaresForALine][];
 
         for (int index = 0; index < ret.length; index++) {
             ret[index] = new NumFlip[FastCheckCalc.threeToTheEighth];
 
             for (int line = 0; line < FastCheckCalc.threeToTheEighth; line++) {
                 ret[index][line] = numFlipForBlackForThisLine(line, index);
             }
         }
 
         return ret;
     }
 
     NumFlip numFlipForBlackForThisLine(int line, int index) {
         int upCount = 0;
         int downCount = 0;
         if (LineDecoder.decoders[index].isEmpty(line)) {
             if (index > 1) {
                 int curIndex = index - 1;
                 if (LineDecoder.decoders[curIndex].isWhite(line)) {
                     do {
                         curIndex--;
                         downCount++;
                     } while (curIndex != 0 && LineDecoder.decoders[curIndex].isWhite(line));
                     if (!LineDecoder.decoders[curIndex].isBlack(line)) {
                         downCount = 0;
                     }
                 }
             }
 
             if (index < 6) {
                 int curIndex = index + 1;
                 if (LineDecoder.decoders[curIndex].isWhite(line)) {
                     do {
                         curIndex++;
                         upCount++;
                     } while (curIndex != 7 && LineDecoder.decoders[curIndex].isWhite(line));
                     if (!LineDecoder.decoders[curIndex].isBlack(line)) {
                         upCount = 0;
                     }
                 }
             }
         }
         return new NumFlip(upCount, downCount);
     }
 
     public NumFlip[][] calcNumFlipForWhite() {
         NumFlip[][] ret = new NumFlip[FastCheckCalc.squaresForALine][];
 
         for (int index = 0; index < ret.length; index++) {
             ret[index] = new NumFlip[FastCheckCalc.threeToTheEighth];
 
             for (int line = 0; line < FastCheckCalc.threeToTheEighth; line++) {
                 ret[index][line] = numFlipForWhiteForThisLine(line, index);
             }
         }
 
         return ret;
     }
 
     NumFlip numFlipForWhiteForThisLine(int line, int index) {
         int upCount = 0;
         int downCount = 0;
         if (LineDecoder.decoders[index].isEmpty(line)) {
             if (index > 1) {
                 int curIndex = index - 1;
                 if (LineDecoder.decoders[curIndex].isBlack(line)) {
                     do {
                         curIndex--;
                         downCount++;
                    } while (curIndex != 0 && LineDecoder.decoders[curIndex].isWhite(line));
                     if (!LineDecoder.decoders[curIndex].isWhite(line)) {
                         downCount = 0;
                     }
                 }
             }
 
             if (index < 6) {
                 int curIndex = index + 1;
                 if (LineDecoder.decoders[curIndex].isBlack(line)) {
                     do {
                         curIndex++;
                         upCount++;
                    } while (curIndex != 7 && LineDecoder.decoders[curIndex].isWhite(line));
                     if (!LineDecoder.decoders[curIndex].isWhite(line)) {
                         upCount = 0;
                     }
                 }
             }
         }
         return new NumFlip(upCount, downCount);
     }
 }
