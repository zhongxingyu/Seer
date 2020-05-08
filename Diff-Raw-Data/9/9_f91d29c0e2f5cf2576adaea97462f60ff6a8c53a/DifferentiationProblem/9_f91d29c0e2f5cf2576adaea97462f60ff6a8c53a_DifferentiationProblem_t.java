 /*******************************************************************************
  * Copyright (c) 2013 See AUTHORS file.
  * 
  * This file is part of SleepFighter.
  * 
  * SleepFighter is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * SleepFighter is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  * See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with SleepFighter. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package se.chalmers.dat255.sleepfighter.challenge.math;
 
 import java.util.Random;
 
 import org.apache.commons.math3.analysis.differentiation.DerivativeStructure;
 
 import android.content.Context;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.utils.debug.Debug;
 import se.chalmers.dat255.sleepfighter.utils.math.RandomMath;
 
 /*
  * Challenge: compute the value of a derivative. 
  */
 public class DifferentiationProblem implements MathProblem {
 	
 	private final Context context;
 	
 	// See the documentation of DerivativeStructure for details. 
 	private static final int PARAMETERS = 1;
 	private static final int INDEX = 0;
 	
 	// so we only ask for second derivatives. 
 	private static final int ORDER = 1;
 	
 	// Used for storing a function and its string representatin. Utilized in DiffentiationProblem
 	public class Function {
 		
 		public Function() {
 			s = "";
 			f = getZero();
 		}
 		
 		public void add(DerivativeStructure term, String termStr) {
 			this.f = this.f.add(term);
 			
 			String sign;
 			if(!s.equals("")) {
 				// If we don't do this, minus signs will look like this:
 				// " x + -x". Instead it should obviously be "x - x"
 				sign  = (termStr.charAt(0) == '-' ? " " : " + ");		
 			} else {
 				sign = "";
 			}
 			this.s = this.s + sign + termStr;
 		}
 
 		public void add(Function func) {
 			this.add(func.f, func.s);
 		}
 		
 		public void abs() {
 			this.f = this.f.abs();
 			this.s = "|" + this.s + "|";
 		}
 
 		public void sqrt() {
 			this.f = this.f.sqrt();
 			this.s = "√{" + this.s + "}";
 		}
 
 		
 		public DerivativeStructure f;
 		
 		// the string represenation of f.
 		public String s;
 		
 		// Whether the function needs to be surrounded by parenthesis when it is for example multiplied 
 		// by another function, or taken to a power.
 		/*
 		 *  For example, if this function is 2x + x^2, then it is necessary to surround it with parenthesis
 		 *  when you for example take it to the power of 2, so that it becomes (2x + x^2)^2, instead of  2x + (x^2)^2
 		 *  So if this function is 2x + x^2, then needsParenthesis should be true, but if it is 
 		 *  for example x, |2+x| or sqrt(3x + 4), then it should be false, because in these cases it is
 		 *  not possible for such a confusion to occur. Plus it doesn't look good to have parenthesis surrounding
 		 *  functions like |2+x|(if it were to be raised to a power of two, it would become (|2+x|)^2, which looks
 		 *  fucking awful.)
 		 */
 		public boolean needsParenthesis; 
 	}
 	
 	private int solution;
 	
 	private String renderedString;
 	
 	// the value for which to calculate to derivative for. So the solution becomes f'(x)
 	private int x;
 	
 	private Random rng = new Random();
 
 	public String render() {
 		return this.renderedString;
 	}
 	
 	public int solution() {
 		Debug.d("solution is " + solution);
 		return this.solution;
 	}
 	
 	public void newProblem() {
 		
 		this.x = RandomMath.nextRandomRanged(rng, -10, 10);
 		Function f = randomFunction();
 		this.solution = (int)Math.round(f.f.getPartialDerivative(1));
 		this.renderedString = "$f(x) = " + f.s + "$";
 		
 		String format =  context.getResources().getString(R.string.differentiation_challenge_desc);
 		this.renderedString += "<br>" + String.format(format, "$f'(" + this.x + ")$");
 		Debug.d("solution is " + solution);	
 	}
 	
 	// The identity function f(x) = x
 	private Function getIdentity() {
 		Function f = new Function();
 		
 		f.f = new DerivativeStructure(PARAMETERS, ORDER, INDEX, this.x);
 		f.s = "x";
 		f.needsParenthesis = false;
 		
 		return f;
 	}
 	
 	// A DerivativeStructure with value 0
 	private static DerivativeStructure getZero() {
 		return new DerivativeStructure(PARAMETERS, ORDER, 0);
 	}
 	
 	private Function randomFunction() {
 		Function f = new Function();
 		
 		final int numberTerms = RandomMath.nextRandomRanged(rng, 1, 2);
 		
 		for(int i = 0; i < numberTerms; ++i) {
 			Function polynomial = randomPolynomial();
 			
 			int type = rng.nextInt(3);
 			
 			if (type == 0) {
 				// now take this polynomial(we'll call it f(x)) and create from it a function on the form a(f(x))^c
 				// for some random a and c.
 				f.add(randomExponentFunction(polynomial));
 				
 			} else if(type == 1) {
 				// Take f(x) and create a new function |f(x)|
 				polynomial.abs();
 				
 				f.add(polynomial);
 			} else {
 				// Take f(x) and create a new function sqrt(f(x))
 				polynomial.sqrt();
 				f.add(polynomial);
 				
 			}
 		}
 		
 		return f;
 	}
 	
 	// gets a random function on the form ax^c
 	private Function randomExponentFunction() { 
 		return randomExponentFunction(getIdentity());
 	}
 	
 	// This function takes a function g, generates two random numbers a and c,
 	// and then returns the new function f(x) = a * g(x)^c
 	private Function randomExponentFunction(Function g) {
 		
 		Function f = new Function();
 		
 		// Format a string for g(x) if it is necessary to surround it with parenthesis. 
 		String gStr;
 		if(g.needsParenthesis) {
 			gStr = "(" +  g.s + ")";
 		} else {
 			gStr = g.s;
 		}
 		
 		// generate an expression on the form ax^c
 		int c = RandomMath.nextRandomRanged(rng, 1, 5);
 		int a =  RandomMath.nextRandomRanged(rng, -5, 5, 0);	
 
 		// if a is 1 we don't need a string at all. Because the expression x looks better than 1x.
		String aStr = (Math.abs(a) != 1 ? a + "" : "");
 
 		// Because x looks better than x^1
 		String xcStr = (c == 1 ? gStr : gStr + "^" + c);
 
 		// form the term
 		f.f = g.f.pow(c).multiply(a);
 		f.s = aStr + xcStr ;
 		
 		f.needsParenthesis = true;
 		
 		return f;
 	}
 	
 	// get a random polynomial. 
 	// http://en.wikipedia.org/wiki/Polynomial#Definition
 	private Function randomPolynomial() {
 		Function ret = new Function();
 
 		final int numberTerms = RandomMath.nextRandomRanged(rng, 1, 2);
 		
 		for(int term = 0; term < numberTerms; ++term) {
 			
 			// get a random function on the form ax
 			Function f = randomExponentFunction();
 			
 			ret.add(f);
 		}
 		
 		ret.needsParenthesis = true;
 		
 		return ret;
 	}
 	
 	public DifferentiationProblem(Context context) {
 		this.context = context;
 	}
 	
 }
