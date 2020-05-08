 package UI;
 
 import Player.Position;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class ConverterTest {
     private Converter converter;
 
     @Before
     public void setUp() {
         converter = new Converter();
     }
 
     @Test
     public void its_surface_index_should_be_115_when_position_index_is_34() {
         //given
         Position position = new Position(34);
         //when
         int index = converter.convert(position);
         //then
        assertThat(index, is(115));
     }
 
     @Test
     public void its_surface_index_should_be_231_when_position_index_is_69() {
         //given
         Position position = new Position(69);
         //when
         int index = converter.convert(position);
         //then
        assertThat(index, is(231));
     }
 }
