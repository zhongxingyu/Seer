 package nz.gen.geek_central.screencalc;
 /*
     Useful numeric routines for Screencalc.
 
     Copyright 2013 Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.
 
     This program is free software: you can redistribute it and/or
     modify it under the terms of the GNU General Public License as
     published by the Free Software Foundation, either version 3 of the
     License, or (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
     General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program. If not, see
     <http://www.gnu.org/licenses/>.
 */
 
 public class NumberUseful
   {
     public static int gcd
       (
         int a,
         int b
       )
       /* returns greatest common divisor of a and b. */
       {
         if (a < 0 || b < 0)
           {
             throw new IllegalArgumentException("gcd of negative numbers");
           } /*if*/
         for (;;)
           {
             if (a < b)
               {
                 final int tmp = a;
                 a = b;
                 b = tmp;
               } /*if*/
             if (b == 0)
                 break;
             a = a % b;
           } /*for*/
         return
             a;
       } /*gcd*/
 
     public static class Fraction
       {
         public final int Numer, Denom;
 
         public Fraction
           (
             int Numer,
             int Denom
           )
           {
             if (Numer <= 0 || Denom <= 0)
               {
                 throw new IllegalArgumentException("Fraction numerator and denominator must both be positive");
               } /*if*/
             int GCD = gcd(Numer, Denom);
             this.Numer = Numer / GCD;
             this.Denom = Denom / GCD;
           } /*Fraction*/
 
         @Override
         public String toString()
           {
             return
                 String.format("%d:%d", Numer, Denom);
           } /*toString*/
 
         public static Fraction FromString
           (
             String s
           )
           {
             final int SepPos = s.indexOf(":");
             final Fraction Result;
             if (SepPos >= 0)
               {
                 final int Numer = Integer.parseInt(s.substring(0, SepPos));
                 final int Denom = Integer.parseInt(s.substring(SepPos + 1, s.length()));
                 Result = new Fraction(Numer, Denom);
               }
             else
               {
                 Result = FromReal(Double.parseDouble(s));
               } /*if*/
             return
                 Result;
           } /*FromString*/
 
         public double ToReal()
           {
             return
                 Numer * 1.0 / Denom;
           } /*ToReal*/
 
         public static Fraction FromReal
           (
             double Val
           )
           {
             final int Multiplier = 360; /* something with lots of factors */
             final int Places = 2; /* decimal places of precision needed */
             final double RangeFactor = 1.0; /* how far to look */
             final double Tol = Math.pow(10, - Places);
 
             int Denom = (int)Math.round
               (
                 Math.pow(Multiplier, Math.ceil(Places / Math.log10(Multiplier)))
               );
             int Numer = (int)Math.round(Val * Denom);
             for (;;)
               {
                 int BestNumer = Numer;
                 int BestDenom = Denom;
                 int BestGCD = gcd(Numer, Denom);
                 final int ILow = Numer - (int)Math.floor((Val * (1 - Tol) * Denom - Numer) * RangeFactor);
                 final int IHigh = Numer + (int)Math.ceil((Val * (1 + Tol) * Denom - Numer) * RangeFactor);
                 boolean IAscending = false;
                 for (int i = Numer;;)
                   {
                     if (IAscending && i > IHigh)
                         break;
                     if (!IAscending && i < ILow)
                       {
                         i = Numer + 1;
                         IAscending = true;
                       } /*if*/
                     final int JLow = Numer - (int)Math.floor(Numer / (Val * (1 + Tol)) * RangeFactor);
                     final int JHigh = Numer + (int)Math.ceil(Numer / (Val * (1 - Tol)) * RangeFactor);
                     boolean JAscending = false;
                     for (int j = Denom;;)
                       {
                         if (JAscending && j > JHigh)
                             break;
                         if (!JAscending && j < JLow)
                           {
                             j = Denom + 1;
                             JAscending = true;
                           } /*if*/
                         if
                           (
                                 i > 0
                             &&
                                 j > 0
                             &&
                                 (i != Numer || j != Denom)
                             &&
                                 Math.abs((i * 1.0 / j - Val) / Val) <= Tol
                           )
                           {
                             final int ThisGCD = gcd(i, j);
                            if (j / ThisGCD < BestDenom / BestGCD)
                               {
                                 BestNumer = i;
                                 BestDenom = j;
                                 BestGCD = ThisGCD;
                               } /*if*/
                           } /*if*/
                         j = JAscending ? j + 1 : j - 1;
                       } /*for*/
                     i = IAscending ? i + 1 : i - 1;
                   } /*for*/
                if (BestDenom / BestGCD >= Denom)
                     break;
                 Denom = BestDenom / BestGCD;
                 Numer = BestNumer / BestGCD;
               } /*for*/
             return
                 new Fraction(Numer, Denom);
           } /*FromReal*/
 
       } /*Fraction*/;
     
   } /*NumberUseful*/;
