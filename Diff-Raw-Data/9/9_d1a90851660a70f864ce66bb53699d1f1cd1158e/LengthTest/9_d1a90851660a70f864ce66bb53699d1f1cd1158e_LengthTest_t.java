 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 
 public class LengthTest {
     Length oneFoot;
 
     @Before
     public void setUp() {
         oneFoot = new Length(LengthType.Feet, 1.0);
     }
 
     @Test
     public void lengthsWithSameValuesShouldBeEqual() {
         assertEquals(new Length(LengthType.Inches, 1), new Length(LengthType.Inches, 1));
     }
 
     @Test
     public void lengthShouldHaveTypeAndCount() {
         assertThat(oneFoot.getCount(), is(1.0));
        assertThat(oneFoot.getLengthType(), is(LengthType.Feet));
     }
 
     @Test
     public void oneFootShouldBeTwelveInches() {
         Length oneFootInInches = oneFoot.expressedIn(LengthType.Inches);
         assertThat(oneFootInInches.getCount(), is(12.0));
     }
 
     @Test
     public void thirtyInchesShouldBeTwoAndAHalfFeet() {
         Length twoAndAHalfFeetInInches = new Length(LengthType.Inches, 30);
         Length expectedTwoAndAHalfFeet = twoAndAHalfFeetInInches.expressedIn(LengthType.Feet);
         assertThat(expectedTwoAndAHalfFeet.getCount(), is(2.5));
     }
 
     @Test
     public void thereShouldBe3FeetInAYard(){
         Length oneYard = new Length(LengthType.Yard, 1);
         assertThat(oneYard.expressedIn(LengthType.Feet).getCount(), is(3.0));
     }
 
     @Test
     public void thereShouldBe36InchesInAYard(){
         Length oneYard = new Length(LengthType.Yard, 1);
         assertThat(oneYard.expressedIn(LengthType.Inches).getCount(), is(36.0));
     }
 
     @Test
     public void thereShouldBe1YardFor3Feet(){
         Length oneYard = new Length(LengthType.Feet, 3);
         assertThat(oneYard.expressedIn(LengthType.Yard).getCount(), is(1.0));
     }
 
     @Test
     public void oneInchShouldBeOneInch(){
         Length oneInch = new Length(LengthType.Inches, 1);
         assertThat(oneInch.expressedIn(LengthType.Inches).getCount(), is(1.0));
     }
 }
