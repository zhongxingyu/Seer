 /**
 * Copyright (c) 2005, 2011, Werner Keil and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Werner Keil - initial API and implementation
  */
 package org.eclipse.uomo.examples.units.console;
 
 import static org.eclipse.uomo.units.SI.*;
 import static org.eclipse.uomo.units.SI.Prefix.KILO;
 import static org.eclipse.uomo.units.impl.system.USCustomary.*;
 
 import org.eclipse.uomo.units.IMeasure;
 import org.eclipse.uomo.units.impl.quantity.LengthAmount;
 import org.unitsofmeasurement.quantity.Length;
 import org.unitsofmeasurement.unit.UnitConverter;
 
 public class ConverterExample {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 	    // Conversion between units.
 		UnitConverter conv = KILO(METRE).getConverterTo(MILE);
 	    System.out.println(conv.convert(10.0));
 	    // Retrieval of the system unit (identifies the measurement type).
 	    System.out.println(REVOLUTION.divide(MINUTE).getSystemUnit());
 	    // Dimension checking (allows/disallows conversions)
 	    System.out.println(ELECTRON_VOLT.isCompatible(WATT.multiply(HOUR)));
 	    // Retrieval of the unit dimension (depends upon the current model).
 	    System.out.println(ELECTRON_VOLT.getDimension());
 	    System.out.println(KILOGRAM.equals(KILO(GRAM)));
 	    System.out.println(KILOGRAM.equals(KILO(OUNCE)));
 	    
 	    LengthAmount foot = new LengthAmount(1, FOOT); 
 	    LengthAmount inches = new LengthAmount(24, INCH);
 	    double ratio = INCH.getConverterTo(FOOT).convert(24);
 	    IMeasure<Length> lRatio = inches.to(FOOT);
 	    //long ratio = inches.longValue(FOOT);
 	    //double ratio = foot.doubleValue(INCH);
 	    System.out.println("Ratio: " + ratio);
 	    @SuppressWarnings("unchecked")
 		IMeasure<Length> iRatio = (IMeasure<Length>) foot.divide(inches);
 	    System.out.println("Ratio2: " + iRatio);
 	    System.out.println("Ratio3: " + lRatio);
 	    //BaseAmount<Length> qaRatio = (BaseAmount<Length>)lRatio;
 	    //System.out.println(qaRatio.getNumber());
 	}
 
 }
