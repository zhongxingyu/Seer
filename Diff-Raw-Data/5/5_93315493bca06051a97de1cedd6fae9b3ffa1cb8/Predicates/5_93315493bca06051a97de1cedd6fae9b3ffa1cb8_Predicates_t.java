 package com.bluespot.logic;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import com.bluespot.logic.predicates.EndsWithPredicate;
 import com.bluespot.logic.predicates.EqualityPredicate;
 import com.bluespot.logic.predicates.GreaterThanPredicate;
 import com.bluespot.logic.predicates.IdentityPredicate;
 import com.bluespot.logic.predicates.InversePredicate;
 import com.bluespot.logic.predicates.LessThanPredicate;
 import com.bluespot.logic.predicates.Predicate;
 import com.bluespot.logic.predicates.RegexPredicate;
 import com.bluespot.logic.predicates.StartsWithPredicate;
 import com.bluespot.logic.predicates.UnanimousPredicate;
 import com.bluespot.logic.predicates.UnilateralPredicate;
 
 /**
  * A set of factory methods for constructing {@link Predicate} objects. Many
  * methods in this library are named so as to encourage chaining.
  * <p>
  * Unless otherwise noted, null values are never allowed as parameters to any
  * methods in this library. Also, tested values that are {@code null} will
  * always evaluate to {@code false}.
  * <p>
  * We intentionally return {@link Predicate} interfaces only, instead of
  * concrete implementations. This seems like good programming practice since it
  * doesn't commit us to a single implementation. Clients may instantiate
  * specific {@code Predicate} implementations if they prefer to do so.
  * <p>
  * These methods are not guaranteed to return unique instances of predicates.
  * For some methods, only one predicate is ever returned. Since all predicates
  * are immutable and implement {@link #equals(Object)} appropriately, this
  * should not be a problem and potentially yield performance gains.
  * 
  * @author Aaron Faanes
  * @see Predicate
  */
 public final class Predicates {
 
     private Predicates() {
         // Suppress default constructor to ensure non-instantiability
         throw new AssertionError("Instantiation not allowed");
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * tested value is strictly greater than the specified constant.
      * 
      * @param <T>
      *            the type of the tested value
      * @param constant
      *            the non-null constant value. The returned predicate will
      *            evaluate to {@code true} for all values that are strictly
      *            greater than this value.
      * @return a predicate that evaluates against the specified constant
      * @see #greaterThanOrEqualTo(Comparable)
      * @see GreaterThanPredicate
      */
     public static <T extends Comparable<? super T>> Predicate<T> greaterThan(final T constant) {
         return new GreaterThanPredicate<T>(constant);
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * tested value is equal to or greater than the specified constant. Tested
      * values that are null evaluate to {@code false}.
      * 
      * @param <T>
      *            the type of the tested value
      * @param constant
      *            the non-null constant value. The returned predicate will
      *            evaluate to {@code true} for all values that are equal to or
      *            greater than this value.
      * @return a predicate that evaluates against the specified constant
      * @see #greaterThan(Comparable)
      * @see GreaterThanPredicate
      */
     public static <T extends Comparable<? super T>> Predicate<T> greaterThanOrEqualTo(final T constant) {
         final List<Predicate<? super T>> predicates = new ArrayList<Predicate<? super T>>();
         predicates.add(Predicates.is(constant));
         predicates.add(Predicates.greaterThan(constant));
         return new UnilateralPredicate<T>(predicates);
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * tested value is strictly less than the specified constant.
      * 
      * @param <T>
      *            the type of the tested value
      * @param constant
      *            the non-null constant value. The returned predicate will
      *            evaluate to {@code true} for all values that are strictly less
      *            than this value.
      * @return a predicate that evaluates against the specified constant
      * @see LessThanPredicate
      * @see #lessThanOrEqualTo(Comparable)
      */
     public static <T extends Comparable<? super T>> Predicate<T> lessThan(final T constant) {
         return new LessThanPredicate<T>(constant);
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * tested value is equal to or less than the specified constant.
      * 
      * @param <T>
      *            the type of the tested value
      * @param constant
      *            the non-null constant value. The returned predicate will
      *            evaluate to {@code true} for all values that are equal to or
      *            less than this value.
      * @return a predicate that evaluates against the specified constant
      * @see LessThanPredicate
      * @see #lessThan(Comparable)
      */
     public static <T extends Comparable<? super T>> Predicate<T> lessThanOrEqualTo(final T constant) {
         final List<Predicate<? super T>> predicates = new ArrayList<Predicate<? super T>>();
         predicates.add(Predicates.is(constant));
         predicates.add(Predicates.greaterThan(constant));
         return new UnilateralPredicate<T>(predicates);
     }
 
     /**
      * A predicate that tests whether a given file exists and is a directory.
      * 
      * @see #isDirectory()
      */
     private static final Predicate<File> PREDICATE_IS_DIRECTORY = new Predicate<File>() {
 
         public boolean test(final File candidate) {
             if (candidate == null) {
                 return false;
             }
             return candidate.isDirectory();
         }
 
         @Override
         public String toString() {
             return "is directory";
         }
     };
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * given {@link File} exists and is a directory.
      * 
      * @return a predicate that tests whether a given {@code File} is a
      *         directory
      * @see File#isDirectory()
      */
     public static Predicate<File> isDirectory() {
         return PREDICATE_IS_DIRECTORY;
     }
 
     /**
      * A predicate that tests whether a given file exists and is a file.
      * 
      * @see #isFile()
      */
     private static final Predicate<File> PREDICATE_IS_FILE = new Predicate<File>() {
 
         public boolean test(final File candidate) {
             if (candidate == null) {
                 return false;
             }
             return candidate.isFile();
         }
 
         @Override
         public String toString() {
             return "is file";
         }
     };
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * given {@link File} exists and is a file.
      * 
      * @return a predicate that tests whether a given {@code File} is a file
      * @see File#isFile()
      */
     public static Predicate<File> isFile() {
         return PREDICATE_IS_FILE;
     }
 
     /**
      * A predicate that tests for null values.
      * 
      * @see #nullValue()
      */
     private static final Predicate<Object> PREDICATE_NULL = new Predicate<Object>() {
 
         public boolean test(final Object candidate) {
             return candidate == null;
         }
 
         @Override
         public String toString() {
             return "is null";
         }
     };
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * tested value is {@code null}.
      * 
      * @return a predicate that evaluates to {@code true} for only the {@code
      *         null} value
      */
     public static Predicate<Object> nullValue() {
         return Predicates.PREDICATE_NULL;
     }
 
     /**
      * A predicate that tests for non-null values.
      * 
      * @see #notNullValue()
      */
     private static final Predicate<Object> PREDICATE_NOT_NULL = new Predicate<Object>() {
 
         public boolean test(final Object candidate) {
             return candidate != null;
         }
 
         @Override
         public String toString() {
             return "is not null";
         }
     };
 
     /**
      * Returns a predicate that evaluates to {@code true} if and only if the
      * tested value is not {@code null}.
      * 
      * @return a predicate that evaluates to {@code true} for all values that
      *         are not {@code null}
      */
     public static Predicate<Object> notNullValue() {
         return Predicates.PREDICATE_NOT_NULL;
     }
 
     /**
      * A predicate that always returns {@code true}.
      * 
      * @see #truth()
      */
     private static final Predicate<Object> PREDICATE_TRUTH = new Predicate<Object>() {
 
         public boolean test(final Object candidate) {
             return true;
         }
 
         @Override
         public String toString() {
             return "is anything";
         }
     };
 
     /**
      * Returns a predicate that always evaluates to {@code true}.
      * 
      * @return a predicate that evaluates to {@code true} for all values
      */
     public static Predicate<Object> truth() {
         return Predicates.PREDICATE_TRUTH;
     }
 
     /**
      * A predicate that always returns {@code false}.
      * 
      * @see #never()
      */
     private static final Predicate<Object> PREDICATE_NEVER = new Predicate<Object>() {
 
         public boolean test(final Object candidate) {
             return false;
         }
 
         @Override
         public String toString() {
             return "is impossible";
         }
     };
 
     /**
      * Returns a predicate that always evaluates to {@code false}.
      * 
      * @return a predicate that evaluates to {@code false} for all values
      */
     public static Predicate<Object> never() {
         return Predicates.PREDICATE_NEVER;
     }
 
     /**
      * Returns a predicate that tests values for identity. It will evaluate to
      * {@code true} if and only if the tested value is a reference to the
      * specified constant.
      * 
      * @param <T>
      *            the type of value used in the predicate
      * @param constant
      *            the constant used in evaluation. The predicate will evaluate
      *            to {@code true} for values that refer to this constant.
      * @return a new predicate that evaluates against the specified constant
      */
     public static <T> Predicate<T> exact(final T constant) {
         return new IdentityPredicate<T>(constant);
     }
 
     /**
      * Returns a predicate that tests values against a regular expression that
      * is compiled from the specified string. The returned predicate will
      * evaluate to {@code true} if and only if the regular expression evaluates
      * to true for the tested value.
      * 
      * @param regexPattern
      *            the string value of the regular expression used by the
      *            returned predicate. The predicate will evaluate to {@code
      *            true} for all values that evaluate to {@code true} according
      *            to this pattern.
      * @return a predicate that uses the specified pattern
      * @see Pattern#matcher(CharSequence)
      * @see Matcher#matches()
      */
     public static Predicate<String> matches(final String regexPattern) {
         return Predicates.matches(Pattern.compile(regexPattern));
     }
 
     /**
      * Returns a predicate that tests values against the specified value.
      * 
      * @param regexPattern
      *            the regular expression used by the returned predicate. The
      *            predicate will evaluate to {@code true} for all values that
      *            evaluate to {@code true} according to this pattern.
      * @return a predicate that uses the specified pattern
      * @see Pattern#matcher(CharSequence)
      * @see Matcher#matches()
      */
     public static Predicate<String> matches(final Pattern regexPattern) {
         return new RegexPredicate(regexPattern);
     }
 
     /**
      * Returns a predicate that tests for equality with the specified constant.
      * The returned predicate will evaluate to {@code true} if, and only if, the
      * tested value is equivalent to {@code constant} according to
      * {@link #equals(Object)}.
      * 
      * @param <T>
      *            the type of the specified value
      * @param constant
      *            the constant used in the returned predicate. The returned
      *            predicate evaluates to {@code true} for all values that are
      *            "equal" to this constant.
      * @return a predicate that uses the specified constant during evaluation
      */
     public static <T> EqualityPredicate<T> is(final T constant) {
         return new EqualityPredicate<T>(constant);
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} for a given value if,
      * and only if, the specified predicate evaluates to {@code true} for that
      * value.
      * 
      * @param <T>
      *            the type of the specified predicate
      * @param predicate
      *            the wrapped predicate.
      * @return a predicate that mirrors the evaluation of the specified
      *         predicate.
      */
     public static <T> Predicate<T> is(final Predicate<T> predicate) {
         return predicate;
     }
 
     /**
      * Returns a predicate that is the inverse of the specified predicate. The
      * returned predicate will evaluate to {@code true} for a given value if,
      * and only if, the specified predicate evaluates to {@code false} for that
      * value.
      * 
      * @param <T>
      *            the type of tested value
      * @param predicate
      *            the predicate that is inverted. The returned predicate will
      *            evaluate to {@code true} for all values that evaluate to
      *            {@code false} according to this specified predicate.
      * @return a predicate that is the inverse of the specified predicate
      */
     public static <T> Predicate<T> not(final Predicate<T> predicate) {
         return new InversePredicate<T>(predicate);
     }
 
     /**
      * Returns a predicate that is {@code true} if, and only if, the tested
      * value is not equal to the specified value.
      * <p>
      * This is a helper method for {@code
      * Predicates.not(Predicates.is(constant))}.
      * 
      * @param <T>
      *            the type of the tested values
      * @param constant
      *            the constant value
      * @return a predicate that tests for inequality
      * @see #is(Predicate)
      * @see #not(Predicate)
      */
     public static <T> Predicate<T> not(final T constant) {
         return Predicates.not(Predicates.is(constant));
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} for a given value if,
      * and only if, all specified predicates evaluate to {@code true} for that
      * value. This represents a boolean AND operation.
      * 
      * @param <T>
      *            the type of value common to all predicates
      * @param predicates
      *            the child predicates. No predicate may be {@code null}. The
      *            order of predicates is preserved.
      * @return a predicate that evaluates to {@code true} only if its child
      *         predicates evaluate to {@code true}.
      */
     public static <T> Predicate<T> all(final Predicate<? super T>[] predicates) {
         return new UnanimousPredicate<T>(Arrays.asList(predicates));
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} if, and only if, the
      * tested string value starts with the specified string.
      * 
      * @param startingValue
      *            the string value used in the predicate's evaluation. The
      *            predicate will evaluate to {@code true} for all non-null
      *            strings that start with this value.
      * @return a predicate that tests for the specified string
      * @see #endsWith(String)
      */
     public static Predicate<String> startsWith(final String startingValue) {
         return new StartsWithPredicate(startingValue);
     }
 
     /**
      * Returns a predicate that evaluates to {@code true} if, and only if, the
      * tested string value ends with the specified string.
      * 
      * @param endingValue
      *            the string value used in the predicate's evaluation. The
      *            predicate will evaluate to {@code true} for all non-null
      *            strings that end with this value.
      * @return a regex predicate that tests for the specified string
      * @see #startsWith(String)
      */
     public static Predicate<String> endsWith(final String endingValue) {
         return new EndsWithPredicate(endingValue);
     }
 
     /**
      * A predicate that tests for lower-case strings.
      * 
     * @see #lowerCase()
      */
     private static final Predicate<String> PREDICATE_LOWER_CASE = new Predicate<String>() {
         public boolean test(final String candidate) {
             if (candidate == null) {
                 return false;
             }
             return candidate.toLowerCase().equals(candidate);
         }
 
         @Override
         public String toString() {
             return "is lower case";
         }
     };
 
     /**
      * Returns a new {@link Predicate} that tests whether a string is
      * lower-case. Specifically, the predicate tests whether the lower-cased
      * version of a given string is equal to the original version. As a
      * consequence, strings that contain characters that cannot be upper or
      * lower-case will implicitly evaluate to {@code true}. This includes
      * numbers, the empty string {@code ""}, whitespace, and all other special
      * characters. Null values, however, will evaluate to {@code false}.
      * <p>
      * Since this method relies on {@link String#toLowerCase()}, results will be
      * locale-dependent.
      * 
      * @return a predicate that evaluates to {@code true} for all strings that
      *         are lower-case.
      * @see #upperCase()
      */
     public static Predicate<String> lowerCase() {
         return Predicates.PREDICATE_LOWER_CASE;
     }
 
     /**
      * A predicate that tests for upper-case strings.
      * 
     * @see #lowerCase()
      */
     private static final Predicate<String> PREDICATE_UPPER_CASE = new Predicate<String>() {
         public boolean test(final String candidate) {
             if (candidate == null) {
                 return false;
             }
             return candidate.toUpperCase().equals(candidate);
         }
 
         @Override
         public String toString() {
             return "is upper case";
         }
     };
 
     /**
      * Returns a new {@link Predicate} that tests whether a string is
      * lower-case. Specifically, the predicate tests whether the upper-cased
      * version of a given string is equal to the original version. As a
      * consequence, strings that contain characters that cannot be upper or
      * lower-case will implicitly evaluate to {@code true}. This includes
      * numbers, the empty string {@code ""}, whitespace, and all other special
      * characters. Null values, however, will evaluate to {@code false}.
      * <p>
      * Since this method relies on {@link String#toLowerCase()}, results will be
      * locale-dependent.
      * 
      * @return a predicate that evaluates to {@code true} for all strings that
      *         are upper-case.
      * @see #lowerCase()
      */
     public static Predicate<String> upperCase() {
         return Predicates.PREDICATE_UPPER_CASE;
     }
 
     /**
      * A predicate that tests whether a given file exists.
      * 
      * @see #fileExists()
      */
     private static final Predicate<File> PREDICATE_FILE_EXISTS = new Predicate<File>() {
         public boolean test(final File candidate) {
             if (candidate == null) {
                 return false;
             }
             return candidate.exists();
         }
 
         @Override
         public String toString() {
             return "exists";
         }
     };
 
     /**
      * Returns a predicate that tests whether a given file exists. The returned
      * predicate will evaluate to {@code true} if, and only if,
      * {@link File#exists()} returns {@code true} for the given file.
      * 
      * @return a predicate that tests whether a given file exists using
      *         {@link File#exists()}
      */
     public static Predicate<File> fileExists() {
         return PREDICATE_FILE_EXISTS;
     }
 
 }
