 package org.eclipse.uomo.examples.units.types;
 
 import org.eclipse.uomo.units.impl.BaseAmount;
 import org.unitsofmeasurement.unit.Unit;
 
/**
 * Represents the speed of heart beat.
 * The standard unit for this quantity is "bpm" (Beats per Minute).
 *
 * @author  <a href="mailto:uomo@catmedia.us">Werner Keil</a>
 * @version 1.0, Date: 2013-10-25
 */
 public final class HeartRateAmount extends BaseAmount<HeartRate> implements HeartRate {
 
 	public HeartRateAmount(Number number, Unit<HeartRate> unit) {
 		super(number, unit);
 	}
 
 }
