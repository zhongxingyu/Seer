 package com.swtanalytics.math;
 
 public class Fraction {
     protected int numerator;
     protected int denominator;
 
 
     public Fraction(int n, int d) {
         if (d == 0) {
             throw new IllegalArgumentException("Argument 'd' is 0");
         }
 
 	int g = gcf(n, d);
 	this.numerator = n / g;
 	this.denominator = d / g;
 
 	// Fix the sign of the numerator and denominator if the gcf is
 	// negative. We may only need to check and fix the denominator
 	// sign here...
 	if (this.numerator > 0 && this.denominator < 0) {
 	    this.numerator *= -1;
 	    this.denominator *= -1;
 	}
     }
 
     public String toString() {
         int n = this.numerator;
         int d = Math.abs(this.denominator);
         if (this.denominator < 0) {
             n *= -1;
         }
 
         String result = String.format("%+d", n);
 
         // Collapse into integer
         if (d != 1) {
             result = String.format("%s/%d", result, d);
         }
 
         return result;
     }
 
     public String formatString(boolean stripPositive) {
         int n = this.numerator;
         int d = Math.abs(this.denominator);
         if (this.denominator < 0) {
             n *= -1;
         }
 
         String fmt;
         if (stripPositive) {
             fmt = "%d";
         } else {
             fmt = "%+d";
         }
         String result = String.format(fmt, n);
 
         // Collapse into integer
         if (d != 1) {
             result = String.format("%s/%d", result, d);
         }
 
         return result;
     }
 
    public double doubleValue() {
        return new Double(this.numerator) / this.denominator;
     }
 
     public int compareTo(Fraction f) {
         Double result = new Double(f.doubleValue() - this.doubleValue());
         return result.intValue();
     }
 
     public Fraction subtract(Fraction f) {
         int n = this.numerator*f.denominator - f.numerator*this.denominator;
 	int d = this.denominator*f.denominator;
         return new Fraction(n, d);
     }
 
     public Fraction multiply(Fraction f) {
         return new Fraction(this.numerator*f.numerator,
                             this.denominator*f.denominator);
     }
 
     private int gcf(int a,int b)
     {
         int rem = 0;
         int gcf = 0;
         do {
 	    rem = a % b;
 	    if (rem == 0)
 		gcf = b;
 	    else {
 		a = b;
 		b = rem;
 	    }
 	} while (rem != 0);
 
         return gcf;
     }
 
 }
 
