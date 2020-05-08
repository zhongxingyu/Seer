 package txtfnnl.utils.stringsim;
 
 import java.util.Arrays;
 
 import txtfnnl.utils.StringUtils;
 import txtfnnl.utils.stringsim.Distance;
 import txtfnnl.utils.stringsim.Similarity;
 
 /**
  * Calculate a Leitner-modified Levenshtein measure between two strings A and B.
  * <p>
  * Special Leitner cost function: for character case mismatches only, the distance cost is halved.
  * For example, the distance between "aa" and "bb" is four (i.e., doubled!), while the distance
  * between "aa" and "AA" is two, not zero; In other words, regular operations count as two, while
  * case mismatches count as distance one in the {@link LeitnerLevenshtein#distance(String, String)
  * distance} result. Similarly, Greek letters are treated as case mismatches to their Latin
  * equivalents.
  * <p>
  * Similarity is defined as <code>1 - (D / N)</code> where <code>D</code> is the special
  * {@link LeitnerLevenshtein#distance(String,String) distance} and <code>N</code> is twice the
  * length of the longer of the two input strings measured in <i>Unicode</i> characters.
  * 
  * @author Florian Leitner
  */
 public class LeitnerLevenshtein implements Distance, Similarity {
   public static final LeitnerLevenshtein INSTANCE = new LeitnerLevenshtein();
   protected final int factor;
   /**
    * Greek alphabet, upper-case: alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota, kappa,
    * lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega.
    */
   private static final int[] GREEK_UPPER = new int[] { '\u0391', '\u0392', '\u0393', '\u0394',
       '\u0395', '\u0396', '\u0397', '\u0398', '\u0399', '\u039A', '\u039B', '\u039C', '\u039D',
       '\u039E', '\u039F', '\u03A0', '\u03A1', '\u03A3', '\u03A4', '\u03A5', '\u03A6', '\u03A7',
       '\u03A8', '\u03A9' };
   /** Corresponding Latin upper-case letters for Greek upper-case letters. */
   private static final int[] LATIN_UPPER = new int[] { 'A', 'B', 'C', 'D', 'E', 'Z', 'H', 'Q',
       'I', 'K', 'L', 'M', 'N', 'G', 'O', 'P', 'R', 'S', 'T', 'Y', 'F', 'X', 'U', 'W' };
   /**
    * Greek alphabet, lower-case: alpha, beta, gamma, delta, epsilon, zeta, eta, theta, iota, kappa,
    * lambda, mu, nu, xi, omicron, pi, rho, sigma, tau, upsilon, phi, chi, psi, omega.
    */
   private static final int[] GREEK_LOWER = new int[] { '\u03B1', '\u03B2', '\u03B3', '\u03B4',
       '\u03B5', '\u03B6', '\u03B7', '\u03B8', '\u03B9', '\u03BA', '\u03BB', '\u03BC', '\u03BD',
       '\u03BE', '\u03BF', '\u03C0', '\u03C1', '\u03C3', '\u03C4', '\u03C5', '\u03C6', '\u03C7',
       '\u03C8', '\u03C9' };
   /** Corresponding Latin lower-case letters for Greek lower-case letters. */
   private static final int[] LATIN_LOWER = new int[] { 'a', 'b', 'c', 'd', 'e', 'z', 'h', 'q',
       'i', 'k', 'l', 'm', 'n', 'g', 'o', 'p', 'r', 's', 't', 'y', 'f', 'x', 'u', 'w' };
   private static final int FIRST_GREEK = '\u0391';
   private static final int LAST_GREEK = '\u03C9';
 
   protected LeitnerLevenshtein() {
     factor = 2;
   }
 
   /** To instantiate another Levenshtein measure with a different cost factor. */
   protected LeitnerLevenshtein(int factor) {
     this.factor = factor;
   }
 
   public int distance(String strA, String strB) {
     if (strA.equals(strB)) return 0;
     int lenA = strA.length(), cpcA = strA.codePointCount(0, lenA), idxA, offA;
     int lenB = strB.length(), cpcB = strB.codePointCount(0, lenB), idxB;
     // return the max. distance if at least one string has zero length
     if (cpcA == 0 || cpcB == 0) return Math.max(cpcA, cpcB) * factor;
     // otherwise calculate the distance using dynamic programming with the last and current row
     int[] last = new int[cpcB + 1];
     int[] current = new int[cpcB + 1];
     int[] tmp;
     int[] cpB = StringUtils.toCodePointArray(strB, cpcB);
     for (idxB = 0; idxB < last.length; ++idxB)
       last[idxB] = idxB * factor;
     for (idxA = 1, offA = 0; offA < lenA; offA += StringUtils.charCount(strA, offA), ++idxA) {
       current[0] = idxA * factor;
       for (idxB = 0; idxB < cpcB; ++idxB)
         current[idxB + 1] = Math.min(Math.min(last[idxB + 1] + factor, current[idxB] + factor),
             last[idxB] + cost(strA.codePointAt(offA), cpB[idxB]));
       tmp = last;
       last = current;
       current = tmp;
     }
     return last[cpcB];
   }
 
   public double similarity(String strA, String strB) {
     // exact matches: similarity = 1
     if (strA.equals(strB)) return 1.0;
     int lenA = strA.length();
     int lenB = strB.length();
     // one string has zero length: similarity = 0
     if (lenA == 0 || lenB == 0) return 0.0;
     int cpcA = strA.codePointCount(0, lenA);
     int cpcB = strB.codePointCount(0, lenB);
     // calculate the distance and return the similarity := 1 - distance / length
     return 1.0 - ((double) distance(strA, strB)) / (Math.max(cpcA, cpcB) * factor);
   }
 
   /**
    * Leitner's case-dependent cost function with Greek letter collation.
    * <p>
    * 
    * @return 0 for exact matches, 1 for case-insensitive matches and Greek-to-Latin-mapped matches,
    *         or 2 for mismatches (everything else).
    */
   protected int cost(int target, int other) {
     if (target == other) return 0;
     else if (Character.toLowerCase(target) == Character.toLowerCase(other)) return 1;
     else if (normalizeGreek(target) == normalizeGreek(other)) return 1;
     else return 2;
   }
 
   protected int normalizeGreek(int cp) {
     if (cp >= FIRST_GREEK && cp <= LAST_GREEK) {
       cp = replace(GREEK_LOWER, LATIN_LOWER, cp);
       cp = replace(GREEK_UPPER, LATIN_UPPER, cp);
     }
     return cp;
   }
 
   /**
    * If a character in <code>greek</code> matches the code point <code>cp</code>, return the code
    * point in <code>latin</code> at the same position as the matched position in <code>greek</code>
    * - otherwise, just return the original code point.
    */
   private int replace(final int[] greek, final int[] latin, int cp) {
     int pos = Arrays.binarySearch(greek, cp);
    if (pos != -1) return latin[pos];
     else return cp;
   }
 }
