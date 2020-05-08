 package com.edsdev.jconvert.domain;
 
 import java.math.BigInteger;
 
 import com.edsdev.jconvert.util.Logger;
 
 /**
  * This is the class that represents a fraction conversion in the application. By fractional conversion we mean 1/34,
  * 23/1 or some other fractional factor
  * 
  * @author Ed Sarrazin Created on Jul 14, 2007 10:10:14 AM
  */
 public class FractionalConversion extends Conversion {
 
     private BigInteger fromToWholeNumber = BigInteger.ZERO;
 
     private BigInteger fromToTopFactor = BigInteger.ZERO;
 
     private BigInteger fromToBottomFactor = BigInteger.ONE;
 
     private static Logger log = Logger.getInstance(FractionalConversion.class);
 
     public FractionalConversion(String fromUnit, String fromUnitAbbr, String toUnit, String toUnitAbbr,
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
             return getRoundedResult(((value * getEffectiveNumerator(this).doubleValue()) / fromToBottomFactor
                 .doubleValue())
                 + getFromToOffset());
         } else {
             return getRoundedResult(((value - getFromToOffset()) * fromToBottomFactor.doubleValue())
                 / getEffectiveNumerator(this).doubleValue());
         }
     }
 
     public String convertValue(BigInteger numerator, BigInteger denominator, String pFromUnit) {
         if (isWholeNumber(getFromToOffset() + "")) {
             if (pFromUnit.equals(this.getFromUnit())) {
                 BigInteger newTop = numerator.multiply(getEffectiveNumerator(this));
                 BigInteger newBottom = denominator.multiply(fromToBottomFactor);
                 newTop = newTop.add(newBottom.multiply(getBigInteger(getFromToOffset() + "")));
                 return reduceFraction(newTop + "/" + newBottom);
             } else {
                 BigInteger newTop = numerator
                     .subtract(denominator.multiply(this.getBigInteger(getFromToOffset() + "")));
                 newTop = newTop.multiply(fromToBottomFactor);
                 BigInteger newBottom = denominator.multiply(getEffectiveNumerator(this));
                 return reduceFraction(newTop + "/" + newBottom);
             }
         }
         return "unknown";
     }
 
     public String multiply(Conversion byConversion) {
         String rv = "1";
         if (byConversion instanceof FractionalConversion) {
             FractionalConversion fc = (FractionalConversion) byConversion;
             BigInteger fcEn = getEffectiveNumerator(fc);
             rv = (getEffectiveNumerator(this).multiply(fcEn)) + "/"
                 + (this.getFromToBottomFactor().multiply(fc.getFromToBottomFactor()));
            if (!rv.equals("Infinity")) {
                return rv;
            }
         }
        rv = ((getEffectiveNumerator(this).doubleValue() * byConversion.getFromToFactor()) / this
            .getFromToBottomFactor().doubleValue())
            + "";
         return rv;
     }
 
     public String divide(Conversion byConversion) {
         String rv = "1";
         if (byConversion instanceof FractionalConversion) {
             FractionalConversion fc = (FractionalConversion) byConversion;
             BigInteger fcEn = getEffectiveNumerator(fc);
             rv = (getEffectiveNumerator(this).multiply(fc.getFromToBottomFactor())) + "/"
                 + (this.getFromToBottomFactor().multiply(fcEn));
            if (!rv.equals("Infinity")) {
                return rv;
            }
         }
        rv = getEffectiveNumerator(this).doubleValue()
            / (this.getFromToBottomFactor().doubleValue() * byConversion.getFromToFactor()) + "";
        
         return rv;
     }
 
     public BigInteger getFromToBottomFactor() {
         return fromToBottomFactor;
     }
 
     public void setFromToBottomFactor(BigInteger fromToBottomFactor) {
         this.fromToBottomFactor = fromToBottomFactor;
     }
 
     public BigInteger getFromToTopFactor() {
         return fromToTopFactor;
     }
 
     public void setFromToTopFactor(BigInteger fromToTopFactor) {
         this.fromToTopFactor = fromToTopFactor;
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToFactor()
      */
     public double getFromToFactor() {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToFactor(double)
      */
     public void setFromToFactor(double fromToFactor) {
         // TODO do appropriate exception handling here
         throw new RuntimeException("Not Supported");
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#setFromToFactorString(java.lang .String)
      */
     public void setFromToFactorString(String fromToFactor) {
         int pos = fromToFactor.indexOf("/");
         if (pos > 0) {
 
             int spacePos = fromToFactor.indexOf(" ");
             if (spacePos < 0) {
                 spacePos = 0;
             }
 
             // fromToFactor = reduceFraction(fromToFactor);
             // pos = fromToFactor.indexOf("/");
             String whole = fromToFactor.substring(0, spacePos).trim();
             String top = fromToFactor.substring(spacePos, pos).trim();
             String bottom = fromToFactor.substring(pos + 1).trim();
             fromToTopFactor = getBigInteger(top);
             fromToBottomFactor = getBigInteger(bottom);
             if (whole.length() > 0) {
                 fromToWholeNumber = getBigInteger(whole);
             }
         } else if (isWholeNumber(fromToFactor)) {
             // representing a whole number like this helps preserve fractions -
             // bit of a trick
             fromToWholeNumber = getBigInteger(fromToFactor);
             fromToTopFactor = BigInteger.ZERO;
             fromToBottomFactor = BigInteger.ONE;
         } else {
             log.error("Tried to process decimal as fraction:" + fromToFactor);
             // TODO throw proper exception here
         }
     }
 
     private static BigInteger getTop(String val) {
         int spacePos = val.indexOf(" ");
         if (spacePos < 0) {
             spacePos = 0;
         }
         int pos = val.indexOf("/");
         return new BigInteger(val.substring(spacePos, pos).trim());
     }
 
     private static BigInteger getBottom(String val) {
         int pos = val.indexOf("/");
         return new BigInteger(val.substring(pos + 1, val.length()));
     }
 
     private static BigInteger getWholeNum(String val) {
         int spacePos = val.indexOf(" ");
         if (spacePos < 0) {
             return BigInteger.ZERO;
         }
         return new BigInteger(val.substring(0, spacePos).trim());
     }
 
     /**
      * Simply returns what the numerator would be if this was not a composite fraction. Example 1 5/8 would be 13
      * 
      * @param fc FractionalConversion to evaluate
      * @return "effective numerator"
      */
    public BigInteger getEffectiveNumerator(FractionalConversion fc) {
         return fc.getFromToTopFactor().add((fc.getFromToWholeNumber().multiply(fc.getFromToBottomFactor())));
     }
 
     public BigInteger getFromToWholeNumber() {
         return fromToWholeNumber;
     }
 
     public void setFromToWholeNumber(BigInteger fromToWholeNumber) {
         this.fromToWholeNumber = fromToWholeNumber;
     }
 
     public static String reduceFraction(String val) {
         BigInteger top = getTop(val);
         BigInteger bottom = getBottom(val);
         BigInteger whole = getWholeNum(val);
 
         if (top.abs().compareTo(bottom.abs()) == 1) {
             BigInteger[] bis = top.divideAndRemainder(bottom);
             BigInteger wholeNum = bis[0];
             top = top.subtract(wholeNum.multiply(bottom)).abs();
             whole = whole.add(wholeNum);
         }
 
         if (top.equals(BigInteger.ZERO)) {
             return whole + "";
         }
 
         BigInteger gcd = top.gcd(bottom);
         if (!gcd.equals(BigInteger.ONE)) {
             top = top.divide(gcd);
             bottom = bottom.divide(gcd);
         }
 
         String rv = "";
         if (!whole.equals(BigInteger.ZERO)) {
             rv = whole + " ";
         }
         rv += top + "/" + bottom;
         return rv.trim();
 
     }
 
     /*
      * (non-Javadoc)
      * @see com.edsdev.jconvert.domain.Conversion#getFromToFactorString()
      */
     public String getFromToFactorString() {
         return fromToWholeNumber + " " +  fromToTopFactor + "/" + fromToBottomFactor;
     }
 
 }
