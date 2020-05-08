 package Prop;
 
import Player.UncleTuu;
 import UI.UIException;
 import org.junit.Test;
 
 public class PropBoxTest {
     @Test(expected = UIException.class)
     public void it_should_not_contains_more_than_10_props_in_a_box() {
         //given
         PropBox box = new PropBox();
         //when
         for (int count = 0; count != 11; ++count) {
            box.add(new Barricade(UncleTuu.class.toString()));
         }
     }
 }
