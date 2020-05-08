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
 package org.jbasics.math.expression.simple;
 
 import java.math.BigDecimal;
 import java.math.MathContext;
 
 import org.jbasics.checker.ContractCheck;
 
 public class SimpleSymbolExpression extends SimpleExpression {
 	private final String symbol;
 
 	public SimpleSymbolExpression(CharSequence symbol) {
 		this.symbol = ContractCheck.mustNotBeNullOrTrimmedEmpty(symbol, "symbol").intern();
 	}
 
 	public String getSymbol() {
		return this.symbol;
 	}
 
 	public BigDecimal eval(SimpleSymbolResolver resolver, MathContext mc) {
 		BigDecimal temp = resolver.resolve(this.symbol);
 		if (temp == null) {
 			throw new RuntimeException("Cannot resolve symbol " + this.symbol);
 		}
 		return temp;
 	}
 
 	@Override
 	public String toString() {
 		if (Character.isJavaIdentifierStart(this.symbol.charAt(0))) {
 			return this.symbol;
 		} else {
 			return "$" + this.symbol;
 		}
 	}
 
 }
