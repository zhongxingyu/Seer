 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package edu.gatech.statics.modes.equation.solver;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 /**
  *
  * @author Calvin Ashmore
  */
 public class Polynomial {
 
     private static final double EPSILON = .0001f;
 
     public Polynomial() {
     }
 
     public Polynomial(Polynomial poly) {
         this.terms.putAll(poly.terms);
     }
 
     static class Symbol {
 
         private final String name;
         private final int degree; // must be > 0
 
         public Symbol(String name, int degree) {
             this.name = name;
             this.degree = degree;
         }
 
         @Override
         public String toString() {
             if (degree == 1) {
                 return name;
             } else {
                 return name + "^" + degree;
             }
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Symbol other = (Symbol) obj;
             if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
                 return false;
             }
             if (this.degree != other.degree) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 7;
             hash = 53 * hash + (this.name != null ? this.name.hashCode() : 0);
             hash = 53 * hash + this.degree;
             return hash;
         }
 
         public String getName() {
             return name;
         }
     }
 
     static class Term implements Comparable<Term> {
 
        public static final Term unitTerm = new Term(Collections.<Symbol>emptyList());
         private final List<Symbol> symbols; // empty list means unit term.
 
         public Term(List<Symbol> symbols) {
             this.symbols = new ArrayList<Symbol>(symbols);
 
             // order by variable names.
             Collections.sort(this.symbols, new Comparator<Symbol>() {
 
                 public int compare(Symbol o1, Symbol o2) {
                     return o1.name.compareTo(o2.name);
                 }
             });
         }
 
         public Term(Map<String, Integer> symbols) {
             this.symbols = new ArrayList<Symbol>();
             for (Map.Entry<String, Integer> entry : symbols.entrySet()) {
                 this.symbols.add(new Symbol(entry.getKey(), entry.getValue()));
             }
 
             // order by variable names.
             Collections.sort(this.symbols, new Comparator<Symbol>() {
 
                 public int compare(Symbol o1, Symbol o2) {
                     return o1.name.compareTo(o2.name);
                 }
             });
         }
 
         /**
          * This finds the LCM - least common multiple of the two terms.
          * This is the smallest term that can be divided by both terms.
          * @param other
          * @return
          */
         public Term lcm(Term other) {
             Map<String, Integer> newTermDegrees = new HashMap<String, Integer>();
             for (Symbol symbol : symbols) {
                 newTermDegrees.put(symbol.name, symbol.degree);
             }
             for (Symbol symbol : other.symbols) {
                 Integer existingDegree = newTermDegrees.get(symbol.name);
                 if (existingDegree != null) {
                     int maxValue = Math.max(existingDegree, symbol.degree);
                     newTermDegrees.put(symbol.name, maxValue);
                 } else {
                     newTermDegrees.put(symbol.name, symbol.degree);
                 }
             }
 
             return new Term(newTermDegrees);
         }
 
         /**
          * Returns the greatest common devisor between the two terms.
          * This is the largest term that can divide both terms.
          * @param other
          * @return
          */
         public Term gcd(Term other) {
             Map<String, Integer> myTerms = new HashMap<String, Integer>();
             Map<String, Integer> newTermDegrees = new HashMap<String, Integer>();
             for (Symbol symbol : symbols) {
                 myTerms.put(symbol.name, symbol.degree);
             }
             for (Symbol symbol : other.symbols) {
                 Integer existingDegree = myTerms.get(symbol.name);
                 if (existingDegree != null) {
                     int minValue = Math.min(existingDegree, symbol.degree);
                     if (minValue > 0) {
                         newTermDegrees.put(symbol.name, minValue);
                     }
                 }
             }
 
             return new Term(newTermDegrees);
         }
 
         /**
          * Returns a term that is the product between this term and the other.
          * @param other
          * @return
          */
         public Term multiply(Term other) {
             Map<String, Integer> newTermDegrees = new HashMap<String, Integer>();
             for (Symbol symbol : symbols) {
                 newTermDegrees.put(symbol.name, symbol.degree);
             }
             for (Symbol symbol : other.symbols) {
                 Integer existingDegree = newTermDegrees.get(symbol.name);
                 if (existingDegree != null) {
                     int newValue = existingDegree + symbol.degree;
                     newTermDegrees.put(symbol.name, newValue);
                 } else {
                     newTermDegrees.put(symbol.name, symbol.degree);
                 }
             }
             return new Term(newTermDegrees);
         }
 
         public boolean isDivisible(Term other) {
             Map<String, Integer> myTermDegrees = new HashMap<String, Integer>();
             for (Symbol symbol : symbols) {
                 myTermDegrees.put(symbol.name, symbol.degree);
             }
             for (Symbol symbol : other.symbols) {
                 Integer existingDegree = myTermDegrees.get(symbol.name);
                 if (existingDegree != null) {
                     int newValue = existingDegree - symbol.degree;
                     if (newValue < 0) {
                         return false;
                     }
                 } else {
                     return false;
                 }
             }
             return true;
         }
 
         /**
          * Returns a term that is the quotient between this term and the other.
          * Note: This term SHOULD be divisible by the other. This method throws an arithmetic exception if it is not.
          * @param other
          * @return
          */
         public Term divide(Term other) {
             Map<String, Integer> newTermDegrees = new HashMap<String, Integer>();
             for (Symbol symbol : symbols) {
                 newTermDegrees.put(symbol.name, symbol.degree);
             }
             for (Symbol symbol : other.symbols) {
                 Integer existingDegree = newTermDegrees.get(symbol.name);
                 if (existingDegree != null) {
                     int newValue = existingDegree - symbol.degree;
                     if (newValue < 0) {
                         throw new ArithmeticException("Term " + this + " is not divisible by " + other + "!");
                     }
                     if (newValue == 0) {
                         newTermDegrees.remove(symbol.name);
                     } else {
                         newTermDegrees.put(symbol.name, newValue);
                     }
                 } else {
                     throw new ArithmeticException("Term " + this + " is not divisible by " + other + "!");
                 }
             }
             return new Term(newTermDegrees);
         }
 
         int degree() {
             int degree = 0;
             for (Symbol symbol : symbols) {
                 degree += symbol.degree;
             }
             return degree;
         }
 
         @Override
         public String toString() {
             String s = "";
             for (int i = 0; i < symbols.size(); i++) {
                 if (i > 0) {
                     s += " * ";
                 }
                 s += symbols.get(i);
             }
             return s;
         }
 
         public int compareTo(Term other) {
             // degree lexographic ordering
             int myDegree = degree();
             int otherDegree = other.degree();
             if (myDegree != otherDegree) {
                 return myDegree - otherDegree;
             } else {
                 // don't know if this will work...
                 return toString().compareTo(other.toString());
 //                if (myDegree == 0) {
 //                    return 0; // both must have zero.
 //                }
 //                for (Symbol symbol : symbols) {
 //                }
             }
         }
 
         @Override
         public boolean equals(Object obj) {
             if (obj == null) {
                 return false;
             }
             if (getClass() != obj.getClass()) {
                 return false;
             }
             final Term other = (Term) obj;
             if (this.symbols != other.symbols && (this.symbols == null || !this.symbols.equals(other.symbols))) {
                 return false;
             }
             return true;
         }
 
         @Override
         public int hashCode() {
             int hash = 5;
             hash = 89 * hash + (this.symbols != null ? this.symbols.hashCode() : 0);
             return hash;
         }
 
         List<Symbol> getSymbols() {
             return symbols;
         }
     }
     private TreeMap<Term, Double> terms = new TreeMap<Term, Double>();
 
     void addTerm(Term term, double coefficient) {
         if (Double.isNaN(coefficient) || Double.isInfinite(coefficient)) {
             throw new ArithmeticException("coefficient is invalid!");
         }
 
         double existingCoefficient = getCoefficient(term);
         terms.put(term, coefficient + existingCoefficient);
     }
 
     double getCoefficient(Term term) {
         Double coefficient = terms.get(term);
         if (coefficient != null) {
             return coefficient;
         }
         return 0;
     }
 
     Set<Term> getAllTerms() {
         return Collections.unmodifiableSet(terms.keySet());
     }
 
     Term getLeadingTerm() {
         return terms.lastKey();
     }
 
     double getLeadingCoefficient() {
         return terms.get(getLeadingTerm());
     }
 
     /**
      * Returns true if this polynomial has one or fewer terms.
      * If the polynomial has one term, that means the term is identically zero. 
      * If true, this leads to messy cases where for a term x*y, then either x OR y is zero.
      * @return
      */
     public boolean isSingular() {
         return terms.size() <= 1;
     }
 
     /**
      * Returns true if this polynomial is identically zero.
      * @return
      */
     public boolean isZero() {
         return terms.size() == 0;
     }
 
     /**
      * Returns the greatest common divisor across the entire polynomial.
      * Returns NULL if the equation is empty.
      * @return
      */
     Term getGCD() {
         if (isSingular() && terms.size() == 1) {
             return terms.firstKey();
         } else if (isSingular()) {
             return null;
         }
 
         // there are going to be at least two terms in the system.
         // start with a base GCD and make sure it is compatible with all terms.
         Term gcd = terms.firstKey().gcd(terms.lastKey());
         for (Term term : terms.keySet()) {
             gcd = gcd.gcd(term);
         }
         return gcd;
     }
 
     public boolean isReducible() {
         Term gcd = getGCD();
         return gcd != null && !gcd.equals(Term.unitTerm);
     }
 
     public Polynomial reduce() {
         Term gcd = getGCD();
         Polynomial newPoly = new Polynomial();
         for (Map.Entry<Term, Double> entry : terms.entrySet()) {
             newPoly.addTerm(entry.getKey().divide(gcd), entry.getValue());
         }
         newPoly.cleanup();
         return newPoly;
     }
 
     public Polynomial add(Polynomial other) {
         Polynomial newPoly = new Polynomial(this);
         for (Map.Entry<Term, Double> entry : other.terms.entrySet()) {
             newPoly.addTerm(entry.getKey(), entry.getValue());
         }
         newPoly.cleanup();
         return newPoly;
     }
 
     public Polynomial sub(Polynomial other) {
         Polynomial newPoly = new Polynomial(this);
         for (Map.Entry<Term, Double> entry : other.terms.entrySet()) {
             newPoly.addTerm(entry.getKey(), -entry.getValue());
         }
         newPoly.cleanup();
         return newPoly;
     }
 
     Polynomial multiply(Term term) {
         Polynomial newPoly = new Polynomial();
         for (Map.Entry<Term, Double> entry : terms.entrySet()) {
             newPoly.addTerm(entry.getKey().multiply(term), entry.getValue());
         }
         newPoly.cleanup();
         return newPoly;
     }
 
     public Polynomial multiply(double c) {
         Polynomial copy = new Polynomial(this);
 
         for (Map.Entry<Term, Double> entry : copy.terms.entrySet()) {
             entry.setValue(entry.getValue() * c);
         }
         copy.cleanup();
         return copy;
     }
 
     public Polynomial cancelLeadingTerms(Polynomial other) {
 
         Polynomial poly1 = getNormalizedPoly();
         Polynomial poly2 = other.getNormalizedPoly();
 
         Term lt1 = poly1.getLeadingTerm();
         Term lt2 = poly2.getLeadingTerm();
 
         Term lcm = lt1.lcm(lt2);
         Term quotient1 = lcm.divide(lt1);
         Term quotient2 = lcm.divide(lt2);
 
         Polynomial pMult1 = poly1.multiply(quotient1);
         Polynomial pMult2 = poly2.multiply(quotient2);
 
         Polynomial diff = pMult1.sub(pMult2);
 
         return diff;
     }
 
     public Polynomial getNormalizedPoly() {
         return multiply(1.0f / getLeadingCoefficient());
     }
 
     public boolean isReducible(Polynomial other) {
 
         Term leadingTerm = other.getLeadingTerm();
 
         for (Term term : terms.keySet()) {
             if (term.isDivisible(leadingTerm)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * This assumes that this polynomial is reducible in terms of the other.
      * @param other
      * @return
      */
     public Polynomial reduce(Polynomial other) {
 
 
         Term leadingTerm = other.getLeadingTerm();
         Term quotient = null;
         double coefficient = 0;
 
         for (Term term : terms.keySet()) {
             if (term.isDivisible(leadingTerm)) {
                 //return true;
                 quotient = term.divide(leadingTerm);
                 coefficient = terms.get(term);
                 break;
             }
         }
 
         Polynomial toSubtract = other.multiply(quotient);
         toSubtract = toSubtract.multiply(coefficient);
 
         return sub(toSubtract);
     }
 
     public int getMaxDegree() {
         int maxValue = 0;
         for (Term term : terms.keySet()) {
             maxValue = Math.max(maxValue, term.degree());
         }
         return maxValue;
     }
 
     /**
      * Removes zero coefficients.
      */
     private void cleanup() {
         List<Term> toRemove = new ArrayList<Term>();
         for (Map.Entry<Term, Double> entry : terms.entrySet()) {
             if (Math.abs(entry.getValue()) < EPSILON) {
                 toRemove.add(entry.getKey());
             }
             if (Double.isNaN(entry.getValue()) || Double.isInfinite(entry.getValue())) {
                 throw new ArithmeticException("Term value is invalid!");
             }
         }
         for (Term term : toRemove) {
             terms.remove(term);
         }
     }
 
    /**
     * This is used for cleaning up polynomials in which the unit term is too small.
     * @return
     */
    Polynomial stripUnit() {
        if (Math.abs(getCoefficient(Term.unitTerm)) < .01) {
            Polynomial copy = new Polynomial(this);
            copy.terms.remove(Term.unitTerm);
            return copy;
        } else {
            return this;
        }
    }

 //    void normalize() {
 //        double lc = getLeadingCoefficient();
 //
 //        for (Map.Entry<Term, Double> entry : terms.entrySet()) {
 //            entry.setValue(entry.getValue() / lc);
 //        }
 //    }
     @Override
     public String toString() {
         String s = "";
         int i = 0;
         for (Term term : terms.keySet()) {
            //for (Term term : terms.descendingKeySet()) {
 
             if (i != 0) {
                 s += " + ";
             }
 
             if (term.degree() == 0) {
                 s += terms.get(term);
             } else {
                 if (terms.get(term) == 1) {
                     s += term;
                 } else {
                     s += terms.get(term) + " * " + term;
                 }
             }
             i++;
         }
         return s;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Polynomial other = (Polynomial) obj;
         if (this.terms != other.terms && (this.terms == null || !this.terms.equals(other.terms))) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 5;
         hash = 97 * hash + (this.terms != null ? this.terms.hashCode() : 0);
         return hash;
     }
 }
