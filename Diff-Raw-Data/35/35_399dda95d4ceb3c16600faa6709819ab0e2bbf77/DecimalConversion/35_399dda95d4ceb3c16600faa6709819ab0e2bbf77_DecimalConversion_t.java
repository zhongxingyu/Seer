 package com.edsdev.jconvert.domain;
 
 import java.math.BigInteger;
 
 /**
  * This is the class that represents a decimal conversion in the application. By decimal conversion we mean 1.024 or
  * some other decimal factor
  * 
  * @author Ed Sarrazin Created on Jul 14, 2007 10:10:14 AM
  */
 public class DecimalConversion extends Conversion {
 
     private double fromToFactor = 1;
 
     private DecimalConversion() {
         super();
     }
 
     public DecimalConversion(String fromUnit, String fromUnitAbbr, String toUnit, String toUnitAbbr,
         String fromToFactor, double fromToOffset) {
         super(fromUnit, fromUnitAbbr, toUnit, toUnitAbbr, fromToFactor, fromToOffset);
         setFromToFactorString(fromToFactor);
     }
 
     /**
      * @param value double value you want to convert
      * @param pFromUnit Unit that you want to convert from
      * @return answer If the fromUnit does not match the classes from unit, then it is assumed that you are converting
      *         the other way ex. System.out.println(conversion.convertValue(17, conversion.getFromUnit()));
      */
     public double convertValue(double value, String pFromUnit) {
         if (pFromUnit.equals(this.getFromUnit())) {
             return getRoundedResult((value * fromToFactor) + getFromToOffset());
         } else {
             return getRoundedResult((value - getFromToOffset()) / fromToFactor);
         }
     }
 
     /**
      * Method returns nothing since this is a Decimal conversion and it does not know how to handle fractions
      */
     public String convertValue(BigInteger numerator, BigInteger denominator, String pFromUnit) {
         return null;
     }
 
     public String multiply(Conversion byConversion) {
         String rv = "1";
         if (byConversion instanceof FractionalConversion) {
             FractionalConversion fc = (FractionalConversion) byConversion;
            rv = (this.getFromToFactor() * fc.getEffectiveNumerator(fc).doubleValue())
                 / fc.getFromToBottomFactor().doubleValue() + "";
         } else {
             rv = (this.getFromToFactor() * byConversion.getFromToFactor()) + "";
         }
         return rv;
     }
 
     public String divide(Conversion byConversion) {
         String rv = "1";
         if (byConversion instanceof FractionalConversion) {
             FractionalConversion fc = (FractionalConversion) byConversion;
             rv = (this.getFromToFactor() * fc.getFromToBottomFactor().doubleValue())
                / fc.getEffectiveNumerator(fc).doubleValue() + "";
         } else {
             rv = (this.getFromToFactor() / byConversion.getFromToFactor()) + "";
         }
         return rv;
     }
 
     public double getFromToFactor() {
         return fromToFactor;
     }
 
     public void setFromToFactor(double fromToFactor) {
         this.fromToFactor = fromToFactor;
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToBottomFactor()
      */
     public BigInteger getFromToBottomFactor() {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToBottomFactor(long)
      */
     public void setFromToBottomFactor(BigInteger fromToBottomFactor) {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToTopFactor()
      */
     public BigInteger getFromToTopFactor() {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToTopFactor(long)
      */
     public void setFromToTopFactor(BigInteger fromToTopFactor) {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToFactorString(java.lang.String)
      */
     public void setFromToFactorString(String factor) {
         fromToFactor = Double.parseDouble(factor);
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToFactorString()
      */
     public String getFromToFactorString() {
         return fromToFactor + "";
     }
 
 	public BigInteger getFromToWholeNumber() {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
 	}
 
 	public void setFromToWholeNumber(BigInteger fromToWholeNumber) {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
 	}
 
 }
