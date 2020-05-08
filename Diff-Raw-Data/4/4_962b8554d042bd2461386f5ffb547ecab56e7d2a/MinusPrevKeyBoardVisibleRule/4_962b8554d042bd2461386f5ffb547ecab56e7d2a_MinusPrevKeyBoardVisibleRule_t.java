 package pl.wtopolski.android.polishnotation.support.model.rules;
 
 import android.text.TextUtils;
import pl.wtopolski.android.polishnotation.support.view.KeyBoard;
 
 import static pl.wtopolski.android.polishnotation.support.view.KeyBoard.*;
 
 public class MinusPrevKeyBoardVisibleRule extends KeyBoardVisibleRule {
     @Override
     public boolean pass(int position, String content) {
         String prev = getPrevChar(position, content);
         String secondPrev = getPrevChar(position, content, 1);
 
        if (valueIsOperation(prev) && (secondPrev == null || valueIsOperation(secondPrev) || KeyBoard.SPECIAL_CHAR_START_BRACKET.equals(secondPrev))) {
             return false;
         }
 
         return true;
     }
 }
