 package com.example.oob.uom;
 
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 
 import static com.example.oob.uom.UnitOfMeasure.Unit.*;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created by Saleem Siddiqui on 10/5/12 at 3:46 AM
  */
 public class TestUnitOfMeasure {
     @Rule
     public ExpectedException thrown = ExpectedException.none();
 
     @Test
     public void compareFeetToOtherLengthUnits() {
         assertThat(createUOM(1.1, FOOT), is(createUOM(1.1, FOOT)));
         assertThat(createUOM(1.1, FOOT), is(createUOM(13.2, INCH)));
         assertThat(createUOM(1.1, FOOT), is(createUOM(33.528, CM)));
         assertThat(createUOM(1.1, FOOT), is(createUOM(335.28, MM)));
     }
 
     @Test
     public void compareInchesToOtherLengthUnits() {
         assertThat(createUOM(1.1, INCH), is(createUOM(1.1, INCH)));
         assertThat(createUOM(1.1, INCH), is(createUOM(0.09166666666, FOOT)));
         assertThat(createUOM(1.1, INCH), is(createUOM(2.794, CM)));
         assertThat(createUOM(1.1, INCH), is(createUOM(27.94, MM)));
     }
 
     @Test
     public void compareCentimetersToOtherLengthUnits() {
         assertThat(createUOM(1.1, CM), is(createUOM(0.433070866141732, INCH)));
         assertThat(createUOM(1.1, CM), is(createUOM(0.036089238845144, FOOT)));
         assertThat(createUOM(1.1, CM), is(createUOM(1.1, CM)));
         assertThat(createUOM(1.1, CM), is(createUOM(11, MM)));
     }
 
     @Test
     public void compareMillimetersToOtherLengthUnits() {
         assertThat(createUOM(1.1, MM), is(createUOM(0.0433070866141732, INCH)));
         assertThat(createUOM(1.1, MM), is(createUOM(0.0036089238845144, FOOT)));
         assertThat(createUOM(1.1, MM), is(createUOM(0.11, CM)));
         assertThat(createUOM(1.1, MM), is(createUOM(1.1, MM)));
     }
 
     @Test
     public void compareLitersToOtherVolumeUnits() {
         assertThat(createUOM(1.1, LITER), is(createUOM(1.1, LITER)));
         assertThat(createUOM(1.1, LITER), is(createUOM(0.290589394543, US_GALLON)));
         assertThat(createUOM(1.1, LITER), is(createUOM(0.241966173128, IMP_GALLON)));
     }
 
     @Test
     public void compareUSGallonsToOtherVolumeUnits() {
         assertThat(createUOM(1.1, US_GALLON), is(createUOM(4.163951, LITER)));
         assertThat(createUOM(1.1, US_GALLON), is(createUOM(1.1, US_GALLON)));
         assertThat(createUOM(1.1, US_GALLON), is(createUOM(0.915941171424, IMP_GALLON)));
     }
 
     @Test
     public void compareImperialGallonsToOtherVolumeUnits() {
         assertThat(createUOM(1.1, IMP_GALLON), is(createUOM(5.000699, LITER)));
         assertThat(createUOM(1.1, IMP_GALLON), is(createUOM(1.321045540641, US_GALLON)));
         assertThat(createUOM(1.1, IMP_GALLON), is(createUOM(1.1, IMP_GALLON)));
     }
 
     @Test
     public void compareKelvinToOtherTemperatureUnits() {
         assertThat(createUOM(1.1, KELVIN), is(createUOM(1.1, KELVIN)));
         assertThat(createUOM(1.1, KELVIN), is(createUOM(-272.05, CELSIUS)));
         assertThat(createUOM(1.1, KELVIN), is(createUOM(-457.69, FAHRENHEIT)));
     }
 
     @Test
     public void compareCelsiusToOtherTemperatureUnits() {
         assertThat(createUOM(1.1, CELSIUS), is(createUOM(274.25, KELVIN)));
         assertThat(createUOM(1.1, CELSIUS), is(createUOM(1.1, CELSIUS)));
         assertThat(createUOM(1.1, CELSIUS), is(createUOM(33.98, FAHRENHEIT)));
     }
 
     @Test
     public void compareFahrenheitToOtherTemperatureUnits() {
         assertThat(createUOM(1.1, FAHRENHEIT), is(createUOM(255.983333333, KELVIN)));
         assertThat(createUOM(1.1, FAHRENHEIT), is(createUOM(-17.166666666, CELSIUS)));
         assertThat(createUOM(1.1, FAHRENHEIT), is(createUOM(1.1, FAHRENHEIT)));
     }
 
     @Test
     public void addFeetToOtherLengthUnits() {
         assertThat(createUOM(1.1, FOOT).add(createUOM(1.1, FOOT)), is(createUOM(2.2, FOOT)));
     }
 
     @Test
     public void cannotAddLengthAndVolume() {
         thrown.expect(IllegalOperationException.class);
        thrown.expectMessage("Cannot perform any meaningful operation between a LENGTH and a VOLUME!");
         createUOM(1, FOOT).add(createUOM(1, LITER));
     }
 
     @Test
    public void cannotMultiplyTwoTemperatureUnits() {
         thrown.expect(IllegalOperationException.class);
        thrown.expectMessage("Cannot perform multiplication on a TEMPERATURE!");
        createUOM(1.1, FAHRENHEIT).multiply(createUOM(1.1, CELSIUS));
     }
 
     private UnitOfMeasure createUOM(double magnitude, UnitOfMeasure.Unit unit) {
         return UnitOfMeasure.create(magnitude, unit);
     }
 }
