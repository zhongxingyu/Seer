 //
 // Math.java
 // ##library.name## (v.##library.prettyVersion##) is released under the MIT License.
 //
 // Copyright (c) 2012, ##author.name## http://www.fh-potsdam.de
 //
 // Permission is hereby granted, free of charge, to any person obtaining a copy
 // of this software and associated documentation files (the "Software"), to deal
 // in the Software without restriction, including without limitation the rights
 // to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 // copies of the Software, and to permit persons to whom the Software is
 // furnished to do so, subject to the following conditions:
 //
 // The above copyright notice and this permission notice shall be included in
 // all copies or substantial portions of the Software.
 //
 // THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 // IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 // FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 // AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 // LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 // OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 // THE SOFTWARE.
 //
 
 package de.fhpotsdam.util.math;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 public class Math {
 	/**
 	 * Rounds a float
 	 * @param value the float to round
 	 * @param precision the precision for rounding
 	 * @return the float with new precision
 	 */
 	public static float round(float value, int precision) {
 	    if (precision <= 0) {
 	        throw new IllegalArgumentException("Precision cannot be zero or less.");
 	    }
 	    BigDecimal decimal = BigDecimal.valueOf(value);
 	    return decimal.setScale(precision, RoundingMode.FLOOR).floatValue();
 	}
 	
 	/**
 	 * 
 	 * @param values
 	 * @return
 	 */
 	public static float getSum(String[] values){
 		return getSum(values, 0, values.length);
 	}
 	
 	/**
 	 * Parses the String array as float and calculates the sum of all elements. 
 	 * The calculation will sum up all elements between startIndex until endIndex-1, so endIndex is <b>excluded</b>. 
 	 * @param startIndex start index
 	 * @param endIndex end index
 	 * @return the biggest value within the range
 	 */
 	public static float getSum(String[] values, int startIndex, int endIndex){
 		float sum = 0;
		if(startIndex<0 || endIndex > values.length || startIndex >= endIndex ){
 			throw new IllegalArgumentException("Start index needs to be smaller than end index and withing range of the array!");
 		}
 		for(int i=startIndex; i<endIndex; i++){
 			try{
 				sum += Float.parseFloat(values[i]);
 			}catch(NumberFormatException e){
 				throw new IllegalArgumentException("'" + values[i] + "' could not be parsed to float!");
 			}
 		}
 		return sum;
 	}
 	
 	/**
 	 * Parses the row with index rowIndex to float and sums up all values. 
 	 * @param values the two-dimnensional string array containing floats at [][columnIndex]
 	 * @param columnIndex the column index containing floats to sum up
 	 * @param startIndex row start index
 	 * @param endIndex row end index
 	 * @return the sum of all values in the specific column
 	 */
 	public static float getSum(String[][] values, int columnIndex, int startIndex, int endIndex){
 		float sum = 0.0f;
 		if(values == null || values.length == 0){
 			throw new IllegalArgumentException("Values array is null or does not contain any data");
 		}
		else if(startIndex<0 || endIndex > values[0].length || startIndex >= endIndex ){
 			throw new IllegalArgumentException("Start index needs to be smaller than end index and withing range of the array!");
 		}
 		for(int i=startIndex; i<endIndex; i++){
 			try{
 				sum += Float.parseFloat(values[i][columnIndex]);
 			}catch(NumberFormatException e){
 				throw new IllegalArgumentException("'" + values[i][columnIndex] + "' could not be parsed to float!");
 			}
 		}
 		return sum;
 	}
 	
 	/**
 	 * Parses the row with index rowIndex to float and sums up all values. 
 	 * @param values the two-dimnensional string array containing floats at [][columnIndex]
 	 * @param columnIndex the column index containing floats to sum up
 	 * @return the sum of all values in the specific column
 	 */
 	public static float getSum(String[][] values, int columnIndex){
 		if(values == null){
 			throw new IllegalArgumentException("Values array is null");
 		}
 		else{
 			return getSum(values, columnIndex, 0, values.length);
 		}
 	}
 	
 	
 }
