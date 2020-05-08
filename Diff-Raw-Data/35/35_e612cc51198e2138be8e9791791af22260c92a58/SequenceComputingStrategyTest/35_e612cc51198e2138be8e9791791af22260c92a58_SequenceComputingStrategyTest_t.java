 package pl.edu.agh.to1.dice.logic.computing;
 
 import org.junit.Test;
 import pl.edu.agh.to1.dice.logic.Dice;
 import pl.edu.agh.to1.dice.logic.DiceBucket;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.fest.assertions.Assertions.assertThat;
 import static org.mockito.BDDMockito.given;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 
 /**
  * Author: Piotr Turek
  */
 public class SequenceComputingStrategyTest {
     @Test
     public void shouldCorrectlyComputePoints1() {
         //given
         Dice dice1 = mock(Dice.class);
         given(dice1.getState()).willReturn(1);
         Dice dice2 = mock(Dice.class);
         given(dice2.getState()).willReturn(2);
         Dice dice3 = mock(Dice.class);
         given(dice3.getState()).willReturn(3);
         Dice dice4 = mock(Dice.class);
         given(dice4.getState()).willReturn(4);
         Dice dice5 = mock(Dice.class);
         given(dice5.getState()).willReturn(5);
         List<Dice> diceList = Arrays.asList(dice3, dice1, dice4, dice2, dice5);
         DiceBucket diceBucket = mock(DiceBucket.class);
         given(diceBucket.iterator()).willReturn(diceList.iterator());
 
         //when
         ComputingStrategy strategy = new SequenceComputingStrategy(40, 5);
         final int result = strategy.compute(diceBucket);
 
         //then
         verify(diceBucket).iterator();
         assertThat(result).isEqualTo(40);
     }
 
     @Test
     public void shouldCorrectlyComputePoints2() {
         //given
         Dice dice1 = mock(Dice.class);
         given(dice1.getState()).willReturn(2);
         Dice dice2 = mock(Dice.class);
         given(dice2.getState()).willReturn(3);
         Dice dice3 = mock(Dice.class);
         given(dice3.getState()).willReturn(4);
         Dice dice4 = mock(Dice.class);
         given(dice4.getState()).willReturn(5);
         Dice dice5 = mock(Dice.class);
         given(dice5.getState()).willReturn(6);
         List<Dice> diceList = Arrays.asList(dice5, dice1, dice4, dice2, dice3);
         DiceBucket diceBucket = mock(DiceBucket.class);
         given(diceBucket.iterator()).willReturn(diceList.iterator());
 
         //when
         ComputingStrategy strategy = new SequenceComputingStrategy(40, 5);
         final int result = strategy.compute(diceBucket);
 
         //then
         verify(diceBucket).iterator();
         assertThat(result).isEqualTo(40);
     }
 
     @Test
     public void shouldReturnZero() {
         //given
         Dice dice = mock(Dice.class);
         given(dice.getState()).willReturn(5);
         List<Dice> diceList = Arrays.asList(dice, dice, dice, dice, dice);
         DiceBucket diceBucket = mock(DiceBucket.class);
         given(diceBucket.iterator()).willReturn(diceList.iterator());
 
         //when
         ComputingStrategy strategy = new SequenceComputingStrategy(40, 5);
         final int result = strategy.compute(diceBucket);
 
         //then
         verify(diceBucket).iterator();
         assertThat(result).isEqualTo(0);
     }

    @Test
    public void shouldReturnZero2() {
        //given
        Dice dice1 = mock(Dice.class);
        given(dice1.getState()).willReturn(1);
        Dice dice2 = mock(Dice.class);
        given(dice2.getState()).willReturn(2);
        Dice dice4 = mock(Dice.class);
        given(dice4.getState()).willReturn(4);
        Dice dice5 = mock(Dice.class);
        given(dice5.getState()).willReturn(5);
        List<Dice> diceList = Arrays.asList(dice4, dice2, dice5, dice1, dice2);
        DiceBucket diceBucket = mock(DiceBucket.class);
        given(diceBucket.iterator()).willReturn(diceList.iterator());

        //when
        ComputingStrategy strategy = new SequenceComputingStrategy(30, 4);
        final int result = strategy.compute(diceBucket);

        //then
        verify(diceBucket).iterator();
        assertThat(result).isEqualTo(0);
    }

 }
