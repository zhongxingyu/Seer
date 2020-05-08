 /*
  * DvRLib - Local search
  * Duncan van Roermund, 2010
  * AbstractProblem.java
  */
 
 package dvrlib.localsearch;
 
 public abstract class AbstractProblem<S extends Solution, E extends Evaluation> implements Problem<S, E> {
    /**
     * Returns true if the first of the given solutions is better than the second, i.e. <tt>evaluate(s1).better(evaluate(s2))</tt>.
     */
    @Override
    public boolean better(S s1, S s2) {
      return (s1 == null ? false : (s2 == null ? true : evaluate(s1).better(evaluate(s2))));
    }
 
    /**
     * Returns true if the first of the given solutions is better than the second, i.e. <tt>evaluate(s1).betterEq(evaluate(s2))</tt>.
     */
    @Override
    public boolean betterEq(S s1, S s2) {
      return (s1 == null ? false : (s2 == null ? true : evaluate(s1).betterEq(evaluate(s2))));
    }
 }
