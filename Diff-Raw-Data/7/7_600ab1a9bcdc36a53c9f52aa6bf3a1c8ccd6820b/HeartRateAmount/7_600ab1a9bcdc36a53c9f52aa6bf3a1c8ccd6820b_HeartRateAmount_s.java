 package org.eclipse.uomo.examples.units.types;
 
 import org.eclipse.uomo.units.impl.BaseAmount;
 import org.unitsofmeasurement.unit.Unit;
 
 public final class HeartRateAmount extends BaseAmount<HeartRate> implements HeartRate {
 
 	public HeartRateAmount(Number number, Unit<HeartRate> unit) {
 		super(number, unit);
 	}
 
 }
