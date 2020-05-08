 package com.gunnarhoffman.converters;
 
 import java.math.BigDecimal;
 import java.security.InvalidParameterException;
 
 public abstract class DistanceConverter {
 
 	private static final int DEFAULT_SCALE = 100;
 	private static final int DEFAULT_ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;
 
 	// TODO I dont trust this number, I'll have to find the formula somewhere.
 	private static final BigDecimal micrometersInAnInch = new BigDecimal(
 			"25400");
 
 	private BigDecimal unit;
 
	public static class MetricDistanceConverter extends DistanceConverter {
 
		private MetricDistanceConverter(BigDecimal micrometers) {
 			this.setUnit(micrometers);
 		}
 
 		@Override
 		public BigDecimal toMicrometers() {
 			return this.getUnit();
 		}
 
 		@Override
 		public BigDecimal toInches() {
 			return this.getUnit()
 					.divide(DistanceConverter.micrometersInAnInch,
 							DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
 		}
 
 	}
 
 	public static class ImperialDistanceConverter extends DistanceConverter {
 
 		public ImperialDistanceConverter(BigDecimal inches) {
 			this.setUnit(inches);
 		}
 
 		@Override
 		public BigDecimal toMicrometers() {
 			return this.getUnit()
 					.multiply(DistanceConverter.micrometersInAnInch);
 		}
 
 		@Override
 		public BigDecimal toInches() {
 			return this.getUnit();
 		}
 
 	}
 
 	public final DistanceConverter setUnit(BigDecimal unit) {
 		if (unit.signum() == -1) {
 			throw new InvalidParameterException(
 					"negative distance makes no sense!");
 		}
 		this.unit = unit;
 		return this;
 	}
 
 	public final BigDecimal getUnit() {
 		return this.unit;
 	}
 
 	public static DistanceConverter fromMicroMeters(BigDecimal micrometers) {
		return new MetricDistanceConverter(micrometers);
 	}
 
 	public static DistanceConverter fromInches(BigDecimal inches) {
 		return new ImperialDistanceConverter(inches);
 	}
 
 	// TODO Goal micrometers to parsecs
 
 	public abstract BigDecimal toMicrometers();
 
 	public abstract BigDecimal toInches();
 }
