 package music;
 
 public class Rational {
 	
 	int _numer;			// Rational numerator
 	int _denom;			// Rational denominator
 	
 	public Rational() {
 		_numer = _denom = 1;
 	}
 	
 	public Rational(int numerator, int denominator) {
         if (denominator == 0) {
             throw new RuntimeException("Denominator is zero");
         }
         
         int g = gcd(numerator, denominator);
         _numer = numerator / g;
         _denom = denominator / g;
     }
 	
 	public int getNumerator() {
 		return _numer;
 	}
 	
 	public int getDenominator() {
 		return _denom;
 	}
 	
 	// return { -1, 0, +1 } if a < b, a = b, or a > b
    public int compareTo(Rational b) {
         Rational a = this;
         int lhs = a.getNumerator() * b.getDenominator();
         int rhs = a.getDenominator() * b.getNumerator();
         if (lhs < rhs) return -1;
         if (lhs > rhs) return +1;
         return 0;
     }
 	
 	// return a + b
 	public Rational plus(Rational b) {
 		Rational a = this;
 		int numerator = (a.getNumerator() * b.getDenominator()) + (b.getNumerator() * a.getDenominator());
 		int denominator = a.getDenominator() * b.getDenominator();
 		return new Rational(numerator, denominator);
     }
 
     // return -a
     public Rational negate() {
 		return new Rational(-_numer, _denom);
     }
 
     // return a - b
     public Rational minus(Rational b) {
         Rational a = this;
         return a.plus(b.negate());
     }
 
 	// returns the greatest common divisor of two ints
 	private static int gcd(int m, int n) {
         if (m < 0) m = -m;
         if (n < 0) n = -n;
         if (0 == n) return m;
         else return gcd(n, m % n);
     }
 	
 	// returns least common multiple of two ints
 	private static int lcm(int m, int n) {
         if (m < 0) m = -m;
         if (n < 0) n = -n;
         return m * (n / gcd(m, n));
     }
 }
