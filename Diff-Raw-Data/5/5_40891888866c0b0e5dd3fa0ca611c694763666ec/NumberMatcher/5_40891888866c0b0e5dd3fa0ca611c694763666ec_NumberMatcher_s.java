 package fw.numbers;
 
 import fw.Log;
 import fw.Matcher;
 
 public abstract class NumberMatcher<T extends Number> implements Matcher<T> {
 
   public abstract T getValue();
 
   public NumberMatcher<T> equal() {
     return new EqualMatcher<T>(this);
   }
 
   public NumberMatcher<T> greaterThan() {
     return new GreaterMatcher<T>(this);
   }
 
   public NumberMatcher<T> lowerThan() {
     return new LowerMatcher<T>(this);
   }
 
   public NumberMatcher<T> orEqual() {
     return equal();
   }
 
   public NumberMatcher<T> orGreater() {
     return greaterThan();
   }
 
   public NumberMatcher<T> orLower() {
     return lowerThan();
   }
 
   @Override
   public abstract boolean evaluate(final T number);
 
   @Override
   public void printSuccess(final T number) {
     Log.success("Got expected: <number> %s", this);
   }
 
   @Override
   public void printNegationSuccess(final T number) {
    Log.error("Not expected: <number> %s%nActual: %s", this, number);
   }
 
   @Override
   public void raiseFailure(final T number) {
    Log.success("Expected: <number> %s%nActual: %s", this, number);
   }
 
   @Override
   public void raiseNegationFailure(final T number) {
     Log.error("Expected not to be: <number> %s", this);
   }
 
   @Override
   public abstract String toString();
 
 }
