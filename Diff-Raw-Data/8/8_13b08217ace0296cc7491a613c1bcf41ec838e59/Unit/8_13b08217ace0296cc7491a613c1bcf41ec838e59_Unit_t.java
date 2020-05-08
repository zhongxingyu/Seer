 package model;
 
 import gui.common.SizeUnits;
 
 import java.io.Serializable;
 import java.util.Map;
 import java.util.TreeMap;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 /**
  * Definition of acceptable units.
  */
 public enum Unit implements Serializable {
 	COUNT, POUNDS, OUNCES, GRAMS, KILOGRAMS, GALLONS, QUARTS, PINTS, FLUID_OUNCES, LITERS;
 
 	public static final Map<Unit, UnitType> typeMap = new TreeMap<Unit, UnitType>();
 	static {
 		typeMap.put(Unit.COUNT, UnitType.COUNT);
 		typeMap.put(Unit.POUNDS, UnitType.WEIGHT);
 		typeMap.put(Unit.OUNCES, UnitType.WEIGHT);
 		typeMap.put(Unit.GRAMS, UnitType.WEIGHT);
 		typeMap.put(Unit.KILOGRAMS, UnitType.WEIGHT);
 		typeMap.put(Unit.GALLONS, UnitType.VOLUME);
 		typeMap.put(Unit.QUARTS, UnitType.VOLUME);
 		typeMap.put(Unit.PINTS, UnitType.VOLUME);
 		typeMap.put(Unit.FLUID_OUNCES, UnitType.VOLUME);
 		typeMap.put(Unit.LITERS, UnitType.VOLUME);
 	}
 
 	public static final Map<SizeUnits, Unit> sizeUnitsMap = new TreeMap<SizeUnits, Unit>();
 	static {
 		sizeUnitsMap.put(SizeUnits.Count, Unit.COUNT);
 		sizeUnitsMap.put(SizeUnits.Pounds, Unit.POUNDS);
 		sizeUnitsMap.put(SizeUnits.Ounces, Unit.OUNCES);
 		sizeUnitsMap.put(SizeUnits.Grams, Unit.GRAMS);
 		sizeUnitsMap.put(SizeUnits.Kilograms, Unit.KILOGRAMS);
 		sizeUnitsMap.put(SizeUnits.Gallons, Unit.GALLONS);
 		sizeUnitsMap.put(SizeUnits.Quarts, Unit.QUARTS);
 		sizeUnitsMap.put(SizeUnits.Pints, Unit.PINTS);
 		sizeUnitsMap.put(SizeUnits.FluidOunces, Unit.FLUID_OUNCES);
 		sizeUnitsMap.put(SizeUnits.Liters, Unit.LITERS);
 	}
 
 	public static Unit convertFromSizeUnits(SizeUnits sizeUnit) {
 		return sizeUnitsMap.get(sizeUnit);
 	}
 
 	public static Unit convertToUnit(String s) {
 		s = s.replace(' ', '_');
 		s = s.toUpperCase();
 		return Unit.valueOf(s);
 	}
 
 	/**
 	 * Retrieves the conversion factor to convert a quantity of one type of unit to another.
 	 * 
 	 * @param convertFrom
 	 *            - originalUnits
 	 * @param convertTo
 	 *            - new units
 	 * @return float conversion factor
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public static float getConversionFactor(Unit convertFrom, Unit convertTo) {
 		if (convertFrom.equals(convertTo))
 			return 1f;
 
 		if (typeMap.get(convertFrom).equals(typeMap.get(convertTo))) {
 			switch (typeMap.get(convertFrom)) {
 			case COUNT:
 				return 1;
 			case VOLUME:
 				return getVolumeConversionFactor(convertFrom, convertTo);
 			case WEIGHT:
 				return getWeightConversionFactor(convertFrom, convertTo);
 			default:
 				throw new NotImplementedException();
 			}
 		} else {
 			throw new IllegalArgumentException("Units must match UnitType to convert");
 		}
 	}
 
 	/**
 	 * Determines whether a specified integer is a valid count.
 	 * 
 	 * @param toTest
 	 *            the integer to test
 	 * @return true if toTest is a valid count, false otherwise.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	public static boolean isValidCount(int toTest) {
 		return toTest >= 0;
 	}
 
 	private static float convertFromFluidOunces(Unit convertTo) {
 		switch (convertTo) {
 		case GALLONS: // FLUID_OUNCES -> GALLONS
 			return 0.0078125f;
 
 		case QUARTS: // FLUID_OUNCES -> QUARTS
 			return 0.03125f;
 
 		case PINTS: // FLUID_OUNCES -> PINTS
 			return 0.0625f;
 
 		case LITERS: // FLUID_OUNCES -> LITERS
 			return 0.0295735f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromGallons(Unit convertTo) {
 		switch (convertTo) {
 		case QUARTS: // GALLONS -> QUARTS
 			return 4;
 
 		case PINTS: // GALLONS -> PINTS
 			return 8;
 
 		case FLUID_OUNCES: // GALLONS -> FLUID_OUNCES
 			return 128;
 
 		case LITERS: // GALLONS -> LITERS
 			return 3.78541f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromGrams(Unit convertTo) {
 		switch (convertTo) {
 		case POUNDS: // GRAMS -> POUNDS
 			return 0.00220462f;
 
 		case OUNCES: // GRAMS -> OUNCES
 			return 0.035274f;
 
 		case KILOGRAMS: // GRAMS -> KILOGRAMS
 			return 0.001f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromKilograms(Unit convertTo) {
 		switch (convertTo) {
 		case POUNDS: // KILOGRAMS -> POUNDS
 			return 2.20462f;
 
 		case OUNCES: // KILOGRAMS -> OUNCES
 			return 35.274f;
 
 		case GRAMS: // KILOGRAMS -> GRAMS
 			return 1000f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromLiters(Unit convertTo) {
 		switch (convertTo) {
 		case GALLONS: // LITERS -> GALLONS
 			return 0.264172f;
 
 		case QUARTS: // LITERS -> QUARTS
 			return 1.05669f;
 
 		case PINTS: // LITERS -> PINTS
 			return 2.11338f;
 
 		case FLUID_OUNCES: // LITERS -> FLUID_OUNCES
 			return 33.814f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromOunces(Unit convertTo) {
 		switch (convertTo) {
 		case POUNDS: // OUNCES -> POUNDS
 			return 0.0625f;
 
 		case GRAMS: // OUNCES -> GRAMS
 			return 28.3495f;
 
 		case KILOGRAMS: // OUNCES -> KILOGRAMS
 			return 0.0283495f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromPints(Unit convertTo) {
 		switch (convertTo) {
 		case GALLONS: // PINTS -> GALLONS
 			return .125f;
 
 		case QUARTS: // PINTS -> QUARTS
 			return .5f;
 
 		case FLUID_OUNCES: // PINTS -> FLUID_OUNCES
 			return 16f;
 
 		case LITERS: // PINTS -> LITERS;
 			return 0.473176f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromPounds(Unit convertTo) {
 		switch (convertTo) {
 		case OUNCES: // POUNDS -> OUNCES
 			return 16f;
 
 		case GRAMS: // POUNDS -> GRAMS
 			return 453.592f;
 
 		case KILOGRAMS: // POUNDS -> KILOGRAMS
 			return 0.453592f;
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float convertFromQuarts(Unit convertTo) {
 		switch (convertTo) {
 		case GALLONS: // QUARTS -> GALLONS
 			return .25f;
 
 		case PINTS: // QUARTS -> PINTS
 			return 2f;
 
 		case FLUID_OUNCES: // QUARTS -> FLUID_OUNCES
 			return 32f;
 
 		case LITERS:
 			return 0.946353f; // QUARTS -> LITERS
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float getVolumeConversionFactor(Unit convertFrom, Unit convertTo) {
 		switch (convertFrom) {
 		case GALLONS: // GALLONS ->
 			return convertFromGallons(convertTo);
 
 		case QUARTS: // QUARTS ->
 			return convertFromQuarts(convertTo);
 
 		case PINTS: // PINTS ->
 			return convertFromPints(convertTo);
 
 		case FLUID_OUNCES: // FLUID_OUNCES ->
 			return convertFromFluidOunces(convertTo);
 
 		case LITERS: // LITERS ->
 			return convertFromLiters(convertTo);
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	private static float getWeightConversionFactor(Unit convertFrom, Unit convertTo) {
 		switch (convertFrom) {
 		case POUNDS: // POUNDS ->
 			return convertFromPounds(convertTo);
 
 		case OUNCES: // OUNCES ->
 			return convertFromOunces(convertTo);
 
 		case GRAMS: // GRAMS ->
 			return convertFromGrams(convertTo);
 
 		case KILOGRAMS: // KILOGRAMS ->
 			return convertFromKilograms(convertTo);
 
 		default:
 			throw new NotImplementedException();
 		}
 	}
 
 	/**
 	 * Get the string representation of the Unit.
 	 * 
 	 * @return String representation of this Unit with first letter capitalized.
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public String toString() {
 		String s = super.toString();
		if (s.contains("_")) {
			// FLUID_OUNCES -> Fluid Ounces
			String ounces = s.substring(s.indexOf('_') + 1);
			String fluid = s.substring(0, s.indexOf('_'));
			String finished = fluid.substring(0, 1) + fluid.substring(1).toLowerCase() + " "
					+ ounces.substring(0, 1) + ounces.substring(1).toLowerCase();
			return finished;
		}
 		return s.substring(0, 1) + s.substring(1).toLowerCase();
 	}
 }
