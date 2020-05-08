 /*
  * Copyright (c) 2009 Stephan Schloepke and innoQ Deutschland GmbH
  *
  * Stephan Schloepke: http://www.schloepke.de/
  * innoQ Deutschland GmbH: http://www.innoq.com/
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package org.jbasics.math.strategies;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 
 import org.jbasics.math.AlgorithmStrategy;
 import org.jbasics.math.IrationalNumber;
 import org.jbasics.math.impl.SquareRootIrationalNumber;
 
 public class GoldenRatioAlgorithmStrategy implements AlgorithmStrategy<BigDecimal> {
 	private static final BigDecimal TWO = BigDecimal.valueOf(2l);
 	private static final BigDecimal FIVE = BigDecimal.valueOf(5l);
 	private static final BigDecimal HALF = BigDecimal.ONE.divide(GoldenRatioAlgorithmStrategy.TWO);
 
 	private static final IrationalNumber<BigDecimal> SQUARE_ROOT_OF_FIVE = SquareRootIrationalNumber.valueOf(GoldenRatioAlgorithmStrategy.FIVE);
 
 	public BigDecimal calculate(final MathContext mc, final BigDecimal guess, final BigDecimal... xn) {
		BigDecimal phi = GoldenRatioAlgorithmStrategy.HALF.add(GoldenRatioAlgorithmStrategy.SQUARE_ROOT_OF_FIVE.valueToPrecision(mc).divide(
 				GoldenRatioAlgorithmStrategy.TWO), mc);
 		if (xn == null || xn.length == 0) {
 			return phi;
 		} else {
 			return phi.multiply(xn[0], mc);
 		}
 	}
 
 }
