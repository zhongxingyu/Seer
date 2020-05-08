 import org.hamcrest.CoreMatchers;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 
 public class MeasurementTest {
     private Measurement oneFoot;
     private Measurement oneCup;
 
     @Before
     public void setUp() {
         oneFoot = new Measurement(MeasurementType.Feet, 1.0);
         oneCup = new Measurement(MeasurementType.Cups, 1.0);
     }
 
     @Test
     public void addingFreezingInCelsiusToZeroFahrenheitEqualsFreezingInFahrenheit(){
         Measurement celsiusFreezing = new Measurement(MeasurementType.Celsius, 0.0);
         Measurement fahrenheitZero = new Measurement(MeasurementType.Fahrenheit, 0.0);
         assertThat(celsiusFreezing.plus(fahrenheitZero), is(new Measurement(MeasurementType.Fahrenheit, 32.0)));
        assertThat(fahrenheitZero.plus(celsiusFreezing), is(new Measurement(MeasurementType.Celsius, 0.0)));
     }
 
     @Test
     public void constructorShouldStoreBaseUnitsForFootInInches() {
         assertThat(oneCup.getBaseUnitCount(), is(16.0));
     }
 
     @Test
     public void constructorShouldStoreBaseUnitCountForInchesInInches() {
         assertThat(new Measurement(MeasurementType.Inches, 2).getBaseUnitCount(), is(2.0));
     }
 
     @Test
     public void measurementKnowsCountOfOriginalUnits() {
         assertThat(oneFoot.measurementUnitCount(), is(1.0));
     }
 
     @Test
     public void shouldTranslateFarenheitToBaseUnitsWithoutChangingCount() {
         Measurement zeroDegreesFarenheit = new Measurement(MeasurementType.Fahrenheit, 1.0);
         assertThat(zeroDegreesFarenheit.measurementUnitCount(), is(1.0));
     }
 
     @Test
     public void shouldTranslateCelsiusToBaseUnitsAndChangeCount() {
         Measurement zeroDegreesCelsius = new Measurement(MeasurementType.Celsius, 0.0);
         assertThat(zeroDegreesCelsius.getBaseUnitCount(), is(32.0));
     }
 
     @Test
     public void thirtyTwoDegreesFahrenheitIs0DegreesCelsius() {
         Measurement freezingFahrenheit = new Measurement(MeasurementType.Fahrenheit, 32.0);
         assertThat(freezingFahrenheit.expressedIn(MeasurementType.Celsius).measurementUnitCount(), is(0.0));
     }
 
     @Test
     public void zeroDegreesCelsiusIs32DegreesFahrenheit() {
         Measurement freezingCelsius = new Measurement(MeasurementType.Celsius, 0.0);
         assertThat(freezingCelsius.expressedIn(MeasurementType.Fahrenheit).measurementUnitCount(), is(32.0));
     }
 
     @Test
     public void celsiusIsATemperature() {
         assertThat(MeasurementType.Celsius.measurementClass, is(MeasurementClassification.Temperature));
     }
 
     @Test
     public void oneTbspIsThreeTsp() {
         Measurement oneTbsp = new Measurement(MeasurementType.Tablespoons, 1.0);
         assertThat(oneTbsp.expressedIn(MeasurementType.Teaspoons).measurementUnitCount(), CoreMatchers.is(3.0));
     }
 
     @Test
     public void oneTspIsHalfOfATbsp() {
         Measurement oneTsp = new Measurement(MeasurementType.Teaspoons, 1.0);
         assertThat(oneTsp.expressedIn(MeasurementType.Tablespoons).measurementUnitCount(), CoreMatchers.is(0.3333333333333333));
     }
 
     @Test
     public void oneCupShouldContain16Tablespoons() {
         assertThat(oneCup.expressedIn(MeasurementType.Tablespoons).measurementUnitCount(), CoreMatchers.is(16.0));
     }
 
     @Test
     public void oneCupShouldContain48Teaspoons() {
         assertThat(oneCup.expressedIn(MeasurementType.Teaspoons).measurementUnitCount(), CoreMatchers.is(48.0));
     }
 
     @Test
     public void lengthShouldPrintCountAndTypeInToString() {
         assertThat(new Measurement(MeasurementType.Tablespoons, 1.0).toString(), CoreMatchers.is("1.0 Tablespoons"));
     }
 
     @Test
     public void addingOneCupAndOneTablespoonShouldBe17Tablespoons() {
         Measurement oneCupAndOneTablespoon = oneCup.plus(new Measurement(MeasurementType.Tablespoons, 1.0));
         assertThat(oneCupAndOneTablespoon, is(new Measurement(MeasurementType.Tablespoons, 17.0)));
     }
 
     @Test
     public void addingOneTablespoonAndOneTeaspoonShouldBe4Teaspoons() {
         Measurement oneTablespoonAndOneTeaspoon = new Measurement(MeasurementType.Tablespoons, 1.0).plus(new Measurement(MeasurementType.Teaspoons, 1.0));
         assertThat(oneTablespoonAndOneTeaspoon, is(new Measurement(MeasurementType.Teaspoons, 4.0)));
     }
 
     @Test
     public void unitsOfTranslatedMeasurementAreTheSameAsMeasurementBeingTranslatedTo() {
         assertThat(oneCup.expressedIn(MeasurementType.Teaspoons).getMeasurementType(), CoreMatchers.is(MeasurementType.Teaspoons));
     }
 
 
     @Test
     public void MeasurementsWithSameValuesShouldBeEqual() {
         assertEquals(new Measurement(MeasurementType.Inches, 1), new Measurement(MeasurementType.Inches, 1));
     }
 
     @Test
     public void MeasurementShouldHaveTypeAndCount() {
         assertThat(oneFoot.measurementUnitCount(), is(1.0));
         assertThat(oneFoot.getMeasurementType(), is(MeasurementType.Feet));
     }
 
     @Test
     public void oneFootShouldBeTwelveInches() {
         Measurement oneFootInInches = oneFoot.expressedIn(MeasurementType.Inches);
         assertThat(oneFootInInches.measurementUnitCount(), is(12.0));
     }
 
     @Test
     public void thirtyInchesShouldBeTwoAndAHalfFeet() {
         Measurement twoAndAHalfFeetInInches = new Measurement(MeasurementType.Inches, 30);
         Measurement expectedTwoAndAHalfFeet = twoAndAHalfFeetInInches.expressedIn(MeasurementType.Feet);
         assertThat(expectedTwoAndAHalfFeet.measurementUnitCount(), is(2.5));
     }
 
     @Test
     public void thereShouldBe3FeetInAYard() {
         Measurement oneYard = new Measurement(MeasurementType.Yards, 1);
         assertThat(oneYard.expressedIn(MeasurementType.Feet).measurementUnitCount(), is(3.0));
     }
 
     @Test
     public void thereShouldBe36InchesInAYard() {
         Measurement oneYard = new Measurement(MeasurementType.Yards, 1);
         assertThat(oneYard.expressedIn(MeasurementType.Inches).measurementUnitCount(), is(36.0));
     }
 
     @Test
     public void thereShouldBe1YardFor3Feet() {
         Measurement oneYard = new Measurement(MeasurementType.Feet, 3);
         assertThat(oneYard.expressedIn(MeasurementType.Yards).measurementUnitCount(), is(1.0));
     }
 
     @Test
     public void oneInchShouldBeOneInch() {
         Measurement oneInch = new Measurement(MeasurementType.Inches, 1);
         assertThat(oneInch.expressedIn(MeasurementType.Inches).measurementUnitCount(), is(1.0));
     }
 
     @Test()
     public void shouldReturnMeasurementWithTypeInvalidConversionWithCountOfZeroWhenAttemptingToTranslateAMeasurementToAMeasurement() {
         Measurement oneInch = new Measurement(MeasurementType.Inches, 1.0);
         assertThat(oneInch.expressedIn(MeasurementType.Teaspoons), is(new Measurement(MeasurementType.InvalidConversion, 0.0)));
     }
 
     @Test()
     public void MeasurementShouldPrintCountAndTypeInToString() {
         assertThat(oneFoot.toString(), is("1.0 Feet"));
     }
 
     @Test
     public void addingOneFootAndOneInchShouldBe13Inches() {
         Measurement oneFootAndOneInch = oneFoot.plus(new Measurement(MeasurementType.Inches, 1.0));
         assertThat(oneFootAndOneInch, is(new Measurement(MeasurementType.Inches, 13.0)));
     }
 }
