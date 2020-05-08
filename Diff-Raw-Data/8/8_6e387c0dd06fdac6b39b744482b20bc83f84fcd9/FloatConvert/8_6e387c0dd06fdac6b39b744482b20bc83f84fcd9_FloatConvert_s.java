 /*
 Copyright (C) 2001 Concord Consortium
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
 package org.concord.waba.graph;
 
 import org.concord.waba.extra.util.*;
 
 public class FloatConvert
 {
     public int minDigits = 1;
     public int maxDigits = 4;
 
    /**
    * Convert float with correct digits
    *
    * This is very ugly code I'll fix it someday :)
    */
   public String toString(float val)
   {
     // We don't want any exponents printed so we need to do this ourselves.
         int j;
 	char intChars[];
 	char fltChars[];
 	int len, nLen;
 	int start=0, end;
 	int exp=0;
 	int multExp;
 	int count;
 	String absLabel;
 
 	for(j=0; j<maxDigits; j++) val *= (float)10;
 	if(val < 0) val -= (float)0.5;
 	else val += (float)0.5;
 
 	if(((int)val) == 0){
 	    if(minDigits != 0)
 		return new String("0.0");
 	    else
 		return new String("0");
 	}
 
 	intChars = String.valueOf((int)Maths.abs(val)).toCharArray();
 	len = intChars.length;
 
 	if(len <= maxDigits){
 	    fltChars = new char[maxDigits + 2];
 	    fltChars[0] = '0';
 	    fltChars[1] = '.';
 	    for(j=0; j < maxDigits - len; j++){
 		fltChars[2+j] = '0';
 	    }
 	    start = 2+j;
 	    for(j=0; j < len; j++)
 		fltChars[start + j] = intChars[j];
 	} else {
 	    fltChars = new char [len + 1];
 	    for(j=0; j < len - maxDigits; j++){
 		fltChars[j] = intChars[j];
 	    }
 	    fltChars[j] = '.';
 	    for(; j < len; j++)
 		fltChars[j + 1] = intChars[j];
 	}
 
 	end = fltChars.length - 1;
 	for(j=0; j < maxDigits - minDigits; j++){
 	    if(fltChars[end - j] != '0') break;
 	}
 	
 
 	absLabel = new String(fltChars, 0, fltChars.length - j);
 
 	if(val < 0)
 	    return new String("-" + absLabel);
 	else 
 	    return absLabel;
 
   }    
 
     float toFloat(String s)
     {
 
 	// Just returning junk for now
 	return 0f;
     }
 
 	int intValue = 0;
 	int exponent = 0;
 	int precision = -2;
 
 	public void setVal(float val)
 	{
 		exponent = getExp(val);
 		intValue = retVal;
 		
 	}
 
 	public int getExponent(){ return exponent; }
 
 	public void setPrecision(int exp)
 	{
 		precision = exp;
 	}
 
 	public String getString(int exp)
 	{
 		if(intValue == 0 ||
 		   exponent < precision){
 			if(precision >= 0){ 
 				return "0"; 
 			} else {
 				String retStr = "0.";
 				for(int i=0; i< -precision; i++){
 					retStr += "0";
 				}
 				return retStr;
 			}			
 		}
 
 		int intValue = this.intValue;
 
 		// If exponent is 3 and exp is 0
 		// then we want to output things like
 		// 1000.0
 		int sigFigs = exponent - precision + 1;
 		if(sigFigs > 8){
 			sigFigs = 8;
 		} else if(sigFigs < 8){
 			int lostDigits = quickExp[8-sigFigs];
 			intValue = (intValue + lostDigits - 1) / lostDigits * lostDigits;
 
 			// watch out for rounding that causes the total numb of digits to 
 			// change
 			if(intValue >= quickExp[9]) {
 				intValue /= 10;
 				exponent++;
 				sigFigs++;
 			}
 		}
 
 		int expOffset = exponent - exp;
 
 		String intStr = "" + intValue;
 	    String sign = "";
 		if(intValue < 0){ 
 			sign = "-";
 			intStr = intStr.substring(1, intStr.length() - 1);
 		} 
 
 		String intNumStr;
 		if(expOffset < 0){
 			intNumStr = "0";
 		} else if(expOffset + 1 > intStr.length()){
 			intNumStr = intStr;
 			for(int i=0; i < (expOffset + 1 - intStr.length()); i++){
 				intNumStr += "0";
 			}
 		} else {
 			intNumStr = intStr.substring(0, expOffset + 1);
 		}
 
 		String intFracStr = null;
		if(exp - precision >= 0){
 			int startPos = expOffset + 1;
 			if(startPos < 0){
 				intFracStr = "";
 				for(int i=0; i < -startPos; i++){
 					intFracStr += "0";
 				}
 				intFracStr += intStr.substring(0, sigFigs);
 			} else {
 				intFracStr = intStr.substring(startPos, sigFigs);
 			}
 		}
 
 		String retStr = sign + intNumStr;
 		if(intFracStr != null){
 			retStr += "." + intFracStr;
 		}
 
 		if(exp != 0){
 			retStr += "E" + exp;
 		}
 
 		return retStr;
 	}
 
 	static float [] lookup = null;
 	public final static float LOG_2_10_FACTOR = 0.301029996f;
 
 	final static int toExp10From2(int x)
 	{
 		return x*301029/1000000;
 	}
 
 	static float xToTheY(float x, int y)
 	{
 		float retVal = 1f;
 		if(y > 0){
 			for(int i=0; i<y ; i++){
 				retVal = x*retVal;
 			}
 			return retVal;
 		} else {
 			for(int i=0; i>y; i--){
 				retVal = retVal/x;
 			}
 		}
 
 		return retVal;
 	}
 	
 	static int [] quickExp = {1,
 							  10,
 							  100,
 							  1000,
 							  10000,
 							  100000,
 							  1000000,
 							  10000000,
 							  100000000,
 							  1000000000,};
 
 	public static void makeLookup()
 	{
 		if(lookup == null){
 			lookup = new float [256];
 		} else {
 			return;
 		}
 
 		for(int i=0; i<256; i++){
 			int base2exp = i - 127;
 			int base10exp = toExp10From2(base2exp);
 			lookup[i] = xToTheY(2f, base2exp) / xToTheY(10f, base10exp) / 
 				(float)(0x800000) * 1000000;
 		}
 	}
 
 	static int retVal = 0;
 	public static int getExp(float val)
 	{
 		int fBits = Float.floatToIntBits(val);
 
 		if(fBits == 0){
 			retVal = 0;
 			return 1;
 		}
 		int unsignedExp= ((fBits & 0x7f800000) >> 23) & 0xFF;
 
 		int base2exp = unsignedExp - 127;
 
 		int base10exp = toExp10From2(base2exp);
 
 		int mantissa = (fBits & 0x007fffff) | 0x0800000;
 		
 		retVal = (int)((float)mantissa * lookup[unsignedExp]);
 
 		while(retVal < 100000000){
 			retVal *= 10;
 			base10exp--;
 		}
 		
 		if((fBits & 0x80000000) != 0) retVal = -retVal;
 
 		base10exp -= 6;
 		
 		return base10exp + 8;
 	}
 }
