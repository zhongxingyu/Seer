 /*
     Copyright (c) 2000-2014 Alessandro Coppo
     All rights reserved.
 
     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:
     1. Redistributions of source code must retain the above copyright
        notice, this list of conditions and the following disclaimer.
     2. Redistributions in binary form must reproduce the above copyright
        notice, this list of conditions and the following disclaimer in the
        documentation and/or other materials provided with the distribution.
     3. The name of the author may not be used to endorse or promote products
        derived from this software without specific prior written permission.
 
     THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
     IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
     OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
     IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
     INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
     NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
     DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
     THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
     (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
     THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
 package net.sf.jautl.numeric;
 
 /**
  * This class represents intervals in R with methods to compute them from data.
  */
 public class DoubleRange {
     /**
      * Construct a null range instance.
      */
     public DoubleRange() {
         set(0, 0);
     }
 
     /**
      * Construct an instance with given minimum and maximum values.
      * Maximum must be >= of minimum.
      * @param minimum the minimum value
      * @param maximum the maximum value
      */
     public DoubleRange(double minimum, double maximum) {
         set(minimum, maximum);
     }
 
     /**
      * Set given values for minimum and maximum.
      * Maximum must be >= of minimum.
      * @param minimum the minimum value
      * @param maximum the maximum value
      */
     public final void set(double minimum, double maximum) {
         if (minimum > maximum)
             throw new IllegalArgumentException();
 
         this.minimum = minimum;
         this.maximum = maximum;
     }
 
     /**
      * Return the minimum value.
      */
 	public final double getMinimum() {
 		return minimum;
 	}
 
     /**
      * Return the maximum value.
      */
 	public final double getMaximum() {
 		return maximum;
 	}
 
     /**
      * Return the range.
      */
 	public final double getRange() {
 		return maximum - minimum;
 	}
 
     /**
     * Checks whether a number is withing the current range.
      * @param x the input value
      * @return 
      */
    public final boolean isWithin(double x) {
         return x >= minimum && x <= maximum;
     }
 
     /**
      * Scale an input value according to the current range.
      * @param x the input value
      * @return 0 if x<=minimum, 1 if x>=maximum, otherwise a linear interpolation.
      */
     public final double scale(double x) {
         if (x <= minimum)
             return 0;
         if (x >= maximum)
             return 1;
         return (x - minimum) / getRange();
     }
 
     /**
      * Update the current minimum and maximum values with another number.
      * @param x the number to be taken into consideration.
      */
     public final void update(double x) {
         if (x < minimum)
             minimum = x;
         if (x > maximum)
             maximum = x;
     }
 
     /**
      * Compute the range of a given vector.
      * @param data the float vector to be analyzed.
      */
 	public final void calc(float[] data) {
         set(data[0], data[0]);
 
 		for (int x = 1; x < data.length; x++)
             update(data[x]);
 	}
 
     /**
      * Compute the range of a given matrix.
      * @param data the float matrix to be analyzed.
      */
 	public final void calc(float[][] data) {
         set(data[0][0], data[0][0]);
 
 		for (int x = 0; x < data.length; x++)
 			for (int y = 0; y < data[x].length; y++)
                 update(data[x][y]);
 	}
 
     /**
      * Compute the range of a given vector.
      * @param data the double vector to be analyzed.
      */
 	public final void calc(double[] data) {
         set(data[0], data[0]);
 
 		for (int x = 1; x < data.length; x++)
             update(data[x]);
 	}
 
     /**
      * Compute the range of a given matrix.
      * @param data the double matrix to be analyzed.
      */
 	public final void calc(double[][] data) {
         set(data[0][0], data[0][0]);
 
 		for (int x = 0; x < data.length; x++)
 			for (int y = 0; y < data[x].length; y++)
                 update(data[x][y]);
 	}
 
 	private double minimum;
 	private double maximum;
 }
