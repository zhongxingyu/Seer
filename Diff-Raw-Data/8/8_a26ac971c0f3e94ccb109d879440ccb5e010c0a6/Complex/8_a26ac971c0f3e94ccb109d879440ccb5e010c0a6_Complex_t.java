 /*
 Program: Java-Complex-Number Class (JCN)
 This program provides arithmetical functions for solving matrix-problems
 
 Copyright (C) 2006 Karsten Bettray
 Version: v0.1f
 
 This program is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation; either version 2.1 of the License, or (at your option) any later version.
 This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License along with this library;
 if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110, USA *
 */
 
 /**
  * <u>Klasse zur Berechnung und R&uuml;ckgabe von komplexen Zahlen</u><br>
  * Dieser Quelltext ist vollst&auml;ndig f&uuml;r jeden frei verf&uuml;gbar<br>
  * und steht der pers&ouml;nlichen Weiterverwendung zur Verf&uuml;gung.
  * @author Karsten Bettray - 27.09.2005<br>
  * Universit&auml;t Duisburg-Essen<br>
  * @version 0.1f
  */
 public class Complex
 {
 	public static final Complex ZERO = new Complex();
 	public static final Complex ONE = new Complex(1.0d, 0.0d);
 	public static final Complex I = new Complex(0.0d, 1.0d);
 	
 	private double real, imag, abs, arg;		// double-Variablen fuer Realteil, Imaginaerteil, Betrag und Phase 
 	private double[] sqrtArgArray = null;		// double-Array, fuer n-Loesungen aus n-tr Wurzel
 	
 	/**
 	 * <u>Leerer Konstruktor, initialisiert  alle Werte mit 0 </u><br>
 	 */
 	public Complex()	// Leerer Konstruktor, initialisiert  alle Werte mit 0 
 	{
 		this.real = 0d;
 		this.imag = 0d;
 		this.abs = 0d;
 		this.arg = 0d;
 	}
 	/**
 	 * <u>Initialisierung mittels Real- und imagin&auml;rteil</u><br>
 	 * @param real Realteil
 	 * @param imag Imagin&auml;teil
 	 */
 	public Complex(double real, double imag) // Initialisierung mittels Real- und imaginaerteil
 	{
 		this.real = real;
 		this.imag = imag;
 		solveAbsAndArg();
 	}
 	/**
 	 * <u>Initialisierung mittels Betrag- und Phase</u><br>
 	 * @param abs Betrag
 	 * @param arg Phase
 	 * @param bool ist bool==true, ist der Winkel in Radiant anzugeben, bei bool=false in '' 
 	 */
 	public Complex(double abs, double arg, boolean bool)		// Initialisierung mittels Betrag- und Phase
 	{
 		this.abs = abs;
 		if(bool)								// wenn bool==true, dann wird der Winkel in radiant uebergeben 
 			this.arg = arg;
 		else									// wenn bool==false, dann wird der Winkel in '' uebergeben
 			this.arg = arg * (Math.PI / 180d);
 		solveRealAndImag();
 	}
 	
 	/**
 	 * <u>Berechnen von Betrag und Phase mittels Real- und Imagin&auml;rteil</u><br>
 	 */
 	private void solveAbsAndArg()
 	{
 		abs = Math.sqrt(Math.pow(real, 2) + Math.pow(imag, 2));
 		arg = Math.atan2(imag, real);
 	}
 	
 	/**
 	 * <u>Berechnen von Real- und Imagin&auml;rteil mittels Betrag und Phase</u><br>
 	 */
 	private void solveRealAndImag()
 	{
 		real = abs * Math.cos(arg);
 		imag = abs * Math.sin(arg);
 	}
 	
 	/**
	 * <u>Liefert den Winkel in  zur&uuml;ck</u><br>
 	 * @return Winkel in Grad zur&uuml;ckliefern
 	 */
 	public double getArgAsDegrease()
 	{
 		return arg * (180d / Math.PI);
 	}
 	
 	/**
	 * <u>Ausgabe der Komplexen Zahl auf der Konsole</u><br>
 	 */
 	public void printComplexNumber()
 	{
 		System.out.println("Realteil="+real+" | Imaginrteil="+imag);
 		System.out.println("Betrag="+abs+" | Phase="+arg+" | Phase in ="+getArgAsDegrease());
 	}
 	/**
	 * <u>Ausgabe der Komplexen Zahl auf der Konsole, inklusive &uuml;bergebenen String</u><br>
 	 * @param string &Uuml;bergebene Zeichenkette wird der Ausgabe vorangestellt
 	 */
 	public void printComplexNumber(String string)
 	{
 		System.out.print(string);
 		System.out.println("Realteil="+real+" | Imaginrteil="+imag);
 		System.out.println("Betrag="+abs+" | Phase="+arg+" | Phase in ="+getArgAsDegrease());
 	}
 
 	/**
 	 * <u>Addition zweier komplexer Zahlen</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Addition
 	 */
 	public Complex add(Complex comp)
 	{
 		return new Complex(this.real + comp.getReal(), this.imag + comp.getImag());
 	}
 	
 	/**
 	 * <u>Subtraktion zweier komplexer Zahlen</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Subtraktion
 	 */
 	public Complex sub(Complex comp)
 	{
 		return new Complex(this.real - comp.getReal(), this.imag - comp.getImag());
 	}
 	
 	/**
 	 * <u>Multiplikation zweier komplexer Zahlen</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Multiplikation
 	 */
 	public Complex mul(Complex comp)
 	{
 		return new Complex(this.abs * comp.getAbs(), this.arg + comp.getArg(), true); 
 	}
 	
 	/**
 	 * <u>Division zweier komplexer Zahlen</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Division
 	 */
 	public Complex div(Complex comp)
 	{
 		return new Complex(this.abs / comp.getAbs(), this.arg - comp.getArg(), true);
 	}
 	
 	/**
 	 * <u>Funktion zur Berechnung mit dem Exponenten 'expon' >= 1</u><br>
 	 * @param expon Exponenten, mit dem die komplexe Zahl exponiert ist.
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Exponierung
 	 */
 	public Complex pow(int expon)
 	{
 		double abs, arg;					// interne Variablen fuer Betrag und Phase
 		Complex swap = null;
 
 		abs = Math.pow(this.getAbs(), (double)expon);	// Betrag exponieren
 		arg = this.getArg() * (double)expon;			// Winkel mit Exponenten multiplizieren
 		swap = new Complex(abs, arg, true);
 
 		return swap;
 	}
 	
 	/**
 	 * <u>Berechnen der n-ten Wurzel der komplexen Zahl</u><br>
 	 * @param expon Wurzelexponent
 	 * @return R&uuml;ckgabe eines Vector mit n L&ouml;sungen
 	 */
 	public Complex sqrt(int expon)
 	{
 		double abs, arg;					// interne Variablen fuer Betrag und Phase
 		double[] sqrtArgArray;				// double-Array, mit n-Winkeln aus Wurzelberechnung
 		Complex swap = null;
 		
 		if(expon>=1) {
 			sqrtArgArray = new double[expon];	// Anlegen des n double-Werte-Arrays fr die n-Lsungen
 			abs = Math.pow(this.getAbs(), 1d/(double)expon);	// n-te Wurzel aus Betrag ziehen
 			
 			for(int i=0; i<=expon-1; i++) {			// Anlegen der n double-Werte fuer die n-Loesungen
 				sqrtArgArray[i] = this.getArg()/(double)expon + ((double)i * 2*Math.PI)/(double)expon;
 			}
 
 			arg = sqrtArgArray[0];					// "Hauptwinkel" bekommt ersten Wert aus double-Werte-Array
 
 			swap = new Complex(abs, arg, true);
 			swap.setSqrtArgArray(sqrtArgArray);
 		}
 		else
 			System.out.println("! Wurzelexponent kleiner 1 !");
 		
 		return swap;
 	}
 
 	/**
 	 * <u>Berechnen des Sinus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus sin(<u>z</u>)
 	 */
 	public Complex sin()
 	{
 		return new Complex(Math.sin(this.getReal()) * Math.cosh(this.getImag()),
 							 Math.cos(this.getReal()) * Math.sinh(this.getImag()));
 	}
 
 	/**
 	 * <u>Berechnen des Kosinus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus cos(<u>z</u>)
 	 */
 	public Complex cos()
 	{
 		return new Complex(Math.cos(this.getReal()) * Math.cosh(this.getImag()),
 							 Math.sin(this.getReal()) * Math.sinh(this.getImag()));
 	}
 	
 	/**
 	 * <u>Berechnen des Tangens der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus tan(<u>z</u>)
 	 */
 	public Complex tan()
 	{
 		double real, imag;
 		double nn = 1d + Math.pow(Math.tan(this.getReal()) * Math.tanh(this.getImag()), 2);	// Nenner der Brueche
 		
 		real = (Math.tan(this.getReal())*(1-Math.pow(Math.tanh(this.getImag()), 2))) / nn;
 		imag = (Math.tanh(this.getImag())*(1+Math.pow(Math.tan(this.getReal()), 2))) / nn;
 		
 		return new Complex(real, imag);
 	}
 	
 	/**
 	 * <u>Berechnen des Kotangens der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus cot(<u>z</u>)
 	 */
 	public Complex cot()
 	{
 		double nn = Math.pow(Math.sin(this.real)*Math.cosh(this.imag), 2) + Math.pow(Math.cos(this.real)*Math.sinh(this.imag), 2); 
 			
 		return new Complex((Math.cos(this.real) * Math.sin(this.real)) / nn, (Math.cosh(this.imag) * Math.sinh(this.imag)) / nn);
 	}
 	
 	/**
 	 * <u>Berechnen des Sinus-Hyperbolikus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus sinh(<u>z</u>)
 	 */
 	public Complex sinh()
 	{
 		return new Complex(Math.sinh(this.getReal()) * Math.cos(this.getImag()),
 							 Math.cosh(this.getReal()) * Math.sin(this.getImag()));
 	}
 	
 	/**
 	 * <u>Berechnen des Kosinus-Hyperbolikus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus cosh(<u>z</u>)
 	 */
 	public Complex cosh()
 	{
 		return new Complex(Math.cosh(this.getReal()) * Math.cos(this.getImag()),
 							 Math.sinh(this.getReal()) * Math.sin(this.getImag()));
 	}
 	
 	/**
 	 * <u>Berechnen des Tangens-Hyperbolikus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus tanh(<u>z</u>)
 	 */
 	public Complex tanh()
 	{
 		return Complex.div(this.sinh(), this.cosh());
 	}
 
 	/**
 	 * <u>Berechnen des Kotangens-Hyperbolikus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus coth(<u>z</u>)
 	 */
 	public Complex coth()
 	{
 		return Complex.div(this.cosh(), this.sinh());
 	}
 
 	/**
 	 * <u>Berechnen der e-Funktion der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus exp(<u>z</u>)
 	 */
 	public Complex exp()
 	{
 		return new Complex(Math.cos(this.getImag()) * (Math.sinh(this.getReal()) + Math.cosh(this.getReal())),
 				 		   Math.sin(this.getImag()) * (Math.cosh(this.getReal()) + Math.sinh(this.getReal())));
 	}
 
 	/**
 	 * <u>Berechnen des nat&uuml;rlichen Logarithmus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus ln(<u>z</u>)
 	 */
 	public Complex ln()
 	{
 		return new Complex(Math.log(this.getAbs()), this.getArg());
 	}
 	
 	/**
 	 * <u>Berechnen des Logarithmus zur  Basis 10 komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus log(<u>z</u>)
 	 */
 	public Complex log()
 	{
 		return new Complex(Math.log10(this.getAbs()), Math.log10(Math.exp(this.getArg())));
 	}
 
 	/**
 	 * <u>Berechnen der konjugierten komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus conj(<u>z</u>)
 	 */
 	public Complex conj()
 	{
 		return new Complex(this.getReal(), -this.getImag());
 	}
 
 	// --- Get- und Set-Methoden
 	
 	/**
 	 * <u>Zur&uuml;ckgeben des Betrages</u><br>
 	 * @return Returns the abs.
 	 */
 	public double getAbs()
 	{
 		return abs;
 	}
 	
 	/**
 	 * <u>Setzen des Betrages</u><br>
 	 * @param abs The abs to set.
 	 */
 	public void setAbs(double abs)
 	{
 		this.abs = abs;
 	}
 	
 	/**
 	 * <u>Zur&uuml;ckgeben des Winkels</u><br>
 	 * @return Returns the arg.
 	 */
 	public double getArg()
 	{
 		return arg;
 	}
 	
 	/**
 	 * <u>Setzen des Winkels</u><br>
 	 * @param arg The arg to set.
 	 */
 	public void setArg(double arg)
 	{
 		this.arg = arg;
 	}
 	
 	/**
 	 * <u>Zur&uuml;ckgeben des Imagin&auml;rteiles</u><br>
 	 * @return Returns the imag.
 	 */
 	public double getImag()
 	{
 		return imag;
 	}
 	
 	/**
 	 * <u>Setzen des Imagin&auml;rteiles</u><br>
 
 	 * @param imag The imag to set.
 	 */
 	public void setImag(double imag)
 	{
 		this.imag = imag;
 	}
 	
 	/**
 	 * <u>Zur&uuml;ckgeben des Realteiles</u><br>
 	 * @return Returns the real.
 	 */
 	public double getReal()
 	{
 		return real;
 	}
 	
 	/**
 	 * <u>Setzen des Realteiles</u><br>
 	 * @param real The real to set.
 	 */
 	public void setReal(double real)
 	{
 		this.real = real;
 	}
 	
 	/**
 	 * <u>Liefert ein Array, mit den n L&ouml;sungen aus n-ter Wurzel zur&uuml;ck</u><br>
 	 * @return Returns the sqrtArgArray.
 	 */
 	public double[] getSqrtArgArray()
 	{
 		return sqrtArgArray;
 	}
 	
 	/**
 	 * @param sqrtArgArray The sqrtVector to set.
 	 */
 	public void setSqrtArgArray(double[] sqrtArgArray)
 	{
 		this.sqrtArgArray = sqrtArgArray;
 	}
 	
 	// Funktionen als statische Funktionen einbinden
 
 	/**
 	 * <u>Addition zweier komplexer Zahlen</u><br>
 	 * @param comp1 1. Komplexe Zahl
 	 * @param comp2 2. Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Addition
 	 */
 	public static Complex add(Complex comp1, Complex comp2)
 	{
 		return new Complex(comp1.getReal() + comp2.getReal(), comp1.getImag() + comp2.getImag());
 	}
 	
 	/**
 	 * <u>Subtraktion zweier komplexer Zahlen</u><br>
 	 * @param comp1 1. Komplexe Zahl
 	 * @param comp2 2. Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Subtraktion
 	 */
 	public static Complex sub(Complex comp1, Complex comp2)
 	{
 		return new Complex(comp1.getReal() - comp2.getReal(), comp1.getImag() - comp2.getImag());
 	}
 
 	/**
 	 * <u>Multiplikation zweier komplexer zahlen</u><br>
 	 * @param comp1 1. Komplexe Zahl
 	 * @param comp2 2. Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Multiplikation
 	 */
 	public static Complex mul(Complex comp1, Complex comp2)
 	{
 		return new Complex(comp1.getAbs() * comp2.getAbs(), comp1.getArg() + comp2.getArg(), true); 
 	}
 
 	/**
 	 * <u>Division zweier komplexer zahlen</u><br>
 	 * @param comp1 1. Komplexe Zahl
 	 * @param comp2 2. Komplexe Zahl
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Division
 	 */
 	public static Complex div(Complex comp1, Complex comp2)
 	{
 		return new Complex(comp1.getAbs() / comp2.getAbs(), comp1.getArg() - comp2.getArg(), true);
 	}
 
 	/**
 	 * <u>Funktion zur Berechnung mit dem Exponenten 'expon' >= 1</u><br>
 	 * @param comp Komplexe Zahl
 	 * @param expon Exponenten, mit dem die komplexe Zahl exponiert ist.
 	 * @return R&uuml;ckgabe von neuem Objekt mit Ergebnis aus Exponierung
 	 */
 	public static Complex pow(Complex comp, int expon)
 	{
 		double abs, arg;					// interne Variablen fuer Betrag und Phase
 		Complex swap = null;
 		if(expon>=1) {
 			abs = Math.pow(comp.getAbs(), (double)expon);	// Betrag exponieren
 			arg = comp.getArg() * (double)expon;			// Winkel mit Exponenten multiplizieren
 			swap = new Complex(abs, arg, true);
 		}
 		else
 			System.out.println("! Exponent kleiner 1 !");
 		
 		return swap;
 	}
 	
 	/**
 	 * <u>Berechnen der n-ten Wurzel der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @param expon Wurzelexponent
 	 * @return R&uuml;ckgabe eines Vector mit n L&ouml;sungen
 	 */
 	public static Complex sqrt(Complex comp, int expon)
 	{
 		double abs, arg;					// interne Variablen fuer Betrag und Phase
 		double[] sqrtArgArray;				// double-Array, mit n-Winkeln aus Wurzelberechnung
 		Complex swap = null;
 		
 		if(expon>=1) {
 			sqrtArgArray = new double[expon];	// Anlegen des n double-Werte-Arrays fr die n-Lsungen
 			abs = Math.pow(comp.getAbs(), 1d/(double)expon);	// n-te Wurzel aus Betrag ziehen
 			
 			for(int i=0; i<=expon-1; i++) {			// Anlegen der n double-Werte fuer die n-Loesungen
 				sqrtArgArray[i] = comp.getArg()/(double)expon + ((double)i * 2*Math.PI)/(double)expon;
 			}
 
 			arg = sqrtArgArray[0];					// "Hauptwinkel" bekommt ersten Wert aus double-Werte-Array
 
 			swap = new Complex(abs, arg, true);
 			swap.setSqrtArgArray(sqrtArgArray);
 		}
 		else
 			System.out.println("! Wurzelexponent kleiner 1 !");
 		
 		return swap;
 	}
 
 	/**
 	 * <u>Berechnen des Sinus der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus sin(<u>z</u>)
 	 */
 	public static Complex sin(Complex comp)
 	{
 		return new Complex(Math.sin(comp.getReal()) * Math.cosh(comp.getImag()),
 							 Math.cos(comp.getReal()) * Math.sinh(comp.getImag()));
 	}
 
 	/**
 	 * <u>Berechnen des Kosinus der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus cos(<u>z</u>)
 	 */
 	public static Complex cos(Complex comp)
 	{
 		return new Complex(Math.cos(comp.getReal()) * Math.cosh(comp.getImag()),
 							 Math.sin(comp.getReal()) * Math.sinh(comp.getImag()));
 	}
 	
 	/**
 	 * <u>Berechnen des Tangens der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus tan(<u>z</u>)
 	 */
 	public static Complex tan(Complex comp)
 	{
 		double real, imag;
 		double nn = 1d + Math.pow(Math.tan(comp.getReal()) * Math.tanh(comp.getImag()), 2);	// Nenner der Brueche
 		
 		real = (Math.tan(comp.getReal())*(1-Math.pow(Math.tanh(comp.getImag()), 2))) / nn;
 		imag = (Math.tanh(comp.getImag())*(1+Math.pow(Math.tan(comp.getReal()), 2))) / nn;
 		
 		return new Complex(real, imag);
 	}
 	
 	/**
 	 * <u>Berechnen des Kotangens der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus cot(<u>z</u>)
 	 */
 	public static Complex cot(Complex comp)
 	{
 		double nn = Math.pow(Math.sin(comp.real)*Math.cosh(comp.imag), 2) + Math.pow(Math.cos(comp.real)*Math.sinh(comp.imag), 2); 
 			
 		return new Complex((Math.cos(comp.real) * Math.sin(comp.real)) / nn, (Math.cosh(comp.imag) * Math.sinh(comp.imag)) / nn);
 	}
 	
 	/**
 	 * <u>Berechnen des Sinus-Hyperbolikus der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus sinh(<u>z</u>)
 	 */
 	public static Complex sinh(Complex comp)
 	{
 		return new Complex(Math.sinh(comp.getReal()) * Math.cos(comp.getImag()),
 							 Math.cosh(comp.getReal()) * Math.sin(comp.getImag()));
 	}
 	
 	/**
 	 * <u>Berechnen des Kosinus-Hyperbolikus der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus cosh(<u>z</u>)
 	 */
 	public static Complex cosh(Complex comp)
 	{
 		return new Complex(Math.cosh(comp.getReal()) * Math.cos(comp.getImag()),
 							 Math.sinh(comp.getReal()) * Math.sin(comp.getImag()));
 	}
 	
 	/**
 	 * <u>Berechnen des -Hyperbolikus der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus tanh(<u>z</u>)
 	 */
 	public static Complex tanh(Complex comp)
 	{
 		return Complex.div(comp.sinh(), comp.cosh());
 	}
 
 	/**
 	 * <u>Berechnen des Kotangens-Hyperbolikus der komplexen Zahl</u><br>
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus coth(<u>z</u>)
 	 */
 	public static Complex coth(Complex comp)
 	{
 		return Complex.div(comp.cosh(), comp.sinh());
 	}
 
 	/**
 	 * <u>Berechnen der e-Funktion der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus exp(<u>z</u>)
 	 */
 	public static Complex exp(Complex comp)
 	{
 		return new Complex(Math.cos(comp.getImag()) * (Math.sinh(comp.getReal()) + Math.cosh(comp.getReal())),
 				 		   Math.sin(comp.getImag()) * (Math.cosh(comp.getReal()) + Math.sinh(comp.getReal())));
 	}
 
 	/**
 	 * <u>Berechnen des nat&uuml;rlichen Logarithmus der komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus ln(<u>z</u>)
 	 */
 	public static Complex ln(Complex comp)
 	{
 		return new Complex(Math.log(comp.getAbs()), comp.getArg());
 	}
 	
 	/**
 	 * <u>Berechnen des Logarithmus zur  Basis 10 komplexen Zahl</u><br>
 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus log(<u>z</u>)
 	 */
 	public static Complex log(Complex comp)
 	{
 		return new Complex(Math.log10(comp.getAbs()), Math.log10(Math.exp(comp.getArg())));
 	}
 	
 	/**
 	 * <u>Berechnen der konjugierten komplexen Zahl</u><br>
 	 * 	 * @param comp Komplexe Zahl
 	 * @return R&uuml;ckgabe eines Objektes vom Typ Complex mit L&ouml;sung aus conj(<u>z</u>)
 	 */
 	public Complex conj(Complex comp)
 	{
 		return new Complex(comp.getReal(), -comp.getImag());
 	}
 
 
 }
