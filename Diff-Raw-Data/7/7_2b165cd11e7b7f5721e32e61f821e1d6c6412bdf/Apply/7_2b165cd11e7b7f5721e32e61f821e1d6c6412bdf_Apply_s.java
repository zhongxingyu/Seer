 package net.cscott.sdr.calls.ast;
 
 import static net.cscott.sdr.calls.transform.AstTokenTypes.APPLY;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import net.cscott.sdr.calls.BadCallException;
 import net.cscott.sdr.calls.Call;
 import net.cscott.sdr.calls.CallDB;
 import net.cscott.sdr.calls.transform.Evaluator;
 import net.cscott.sdr.calls.transform.TransformVisitor;
 import net.cscott.sdr.calls.transform.ValueVisitor;
 import net.cscott.sdr.util.Fraction;
 
 /**
  * {@code Apply} represents a invocation of a call or concept, with zero or more
  * arguments. The {@link Apply#callName callName} field of the {@code Apply}
  * gives the name of the call (which can be converted to a {@link Call} object
  * with {@link CallDB#lookup(String)}). Note that 'and' is a concept used to
  * sequentially join calls: the caller's "slip and slide" would be represented
  * as:
  * 
  * <pre>
  *  #(APPLY["and"] #(APPLY["slip"]) #(APPLY["slide"]))
  * </pre>
  * 
  * The children of this node represent the arguments. Numerical or selector
  * arguments are represented as text in another Apply (since we don't know the
  * proper types); for example "twice (trade and roll)" is:
  * 
  * <pre>
  *  #(APPLY["_fractional"] #(APPLY["2"]) #(APPLY["roll"] #(APPLY["trade"])))
  * </pre>
  * 
  * Convenience methods are provided to convert numerical or string arguments
  * when implementing {@link Call#apply(Apply)}.
  * 
  * @author C. Scott Ananian
  * @version $Id: Apply.java,v 1.9 2006-10-19 21:00:09 cananian Exp $
  */
 public class Apply extends SeqCall {
     public final String callName;
     public final List<Apply> args;
 
     public Apply(String callName, List<Apply> args) {
         super(APPLY);
         this.callName = callName;
         this.args = Collections.unmodifiableList
         (Arrays.asList(args.toArray(new Apply[args.size()])));
     }
 
     @Override
     public <T> SeqCall accept(TransformVisitor<T> v, T t) {
         return v.visit(this, t);
     }
     @Override
     public <RESULT,CLOSURE>
     RESULT accept(ValueVisitor<RESULT,CLOSURE> v, CLOSURE cl) {
         return v.visit(this, cl);
     }
     @Override
     public String argsToString() {
         StringBuilder sb = new StringBuilder();
         sb.append(callName);
         sb.append(' ');
         for (Apply a : args) {
             sb.append(a.toString());
             sb.append(' ');
         }
         return sb.toString();
     }
 
     public Comp expand() throws BadCallException {
        Call c = CallDB.INSTANCE.lookup(callName);
         assert c.getEvaluator(this) == null; // don't blindly expand
         return c.apply(this);
     }
     public Evaluator evaluator() throws BadCallException {
 	Call c;
 	try {
 	    c = CallDB.INSTANCE.lookup(callName);
 	} catch (IllegalArgumentException e) {
 	    throw new BadCallException("Unknown call: "+callName);
 	}
         return c.getEvaluator(this); // could be null.
     }
     
     public Apply getArg(int n) { return args.get(n); }
 
     public Fraction getNumberArg(int n) {
         // special feature: support arithmetic evaluation by calling
         // 'expand' on this arg, if it isn't already a simple string.
         Apply a = getArg(n);
         while (!a.args.isEmpty()) // typecasts show that this is kludgey...
             a = (Apply) ((Seq)a.expand()).children.get(0); // do arithmetic!
         assert a.args.isEmpty();
         return Fraction.valueOf(a.callName);
     }
 
     public String getStringArg(int n) {
         return getArg(n).callName;
     }
 
     // XXX getSelectorArg, etc?
 
     // factories.
     public static Apply makeApply(String callName) {
         return new Apply(callName, Collections.<Apply> emptyList());
     }
 
     public static Apply makeApply(String callName, Fraction number) {
         Apply arg = makeApply(number.toString()); // sigh
         return makeApply(callName, arg);
     }
     public static Apply makeApply(String callName, Fraction number, String s) {
         return makeApply(callName, number, makeApply(s));
     }
     public static Apply makeApply(String callName, Fraction number, Apply arg2) {
         Apply arg1 = makeApply(number.toString()); // sigh
         return makeApply(callName, arg1, arg2);
     }
 
     public static Apply makeApply(String conceptName, Apply... subCalls) {
         return new Apply(conceptName, Arrays.asList(subCalls));
     }
     /** Factory: creates new Apply only if it would differ from this. */
     public Apply build(String callName, List<Apply> args) {
         if (this.callName.equals(callName) && this.args.equals(args))
             return this;
         return new Apply(callName, args);
     }
 }
