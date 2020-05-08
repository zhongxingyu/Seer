 package jason.asSyntax;
 
 import jason.asSemantics.Agent;
 import jason.asSemantics.ArithFunction;
 import jason.asSemantics.Unifier;
 
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 /**
  * Represents an arithmetic function, like math.max(arg1,arg2) -- a functor (math.max) and two arguments.
  * A Structure is thus used to store the data.
  * 
  * @composed - "arguments (from Structure.terms)" 0..* Term
  * 
  * @author Jomi
  *
  */
 public class ArithFunctionTerm extends Structure implements NumberTerm {
 
     private static final long serialVersionUID = 1L;
 
     private static Logger logger = Logger.getLogger(ArithFunctionTerm.class.getName());
     
     private NumberTerm value = null; // value, when evaluated
 
     private ArithFunction function = null;
     
     private Agent agent = null; // the agent where this function was used
     
     public ArithFunctionTerm(String functor, int termsSize) {
         super(functor, termsSize);        
     }
     
     public ArithFunctionTerm(ArithFunction function) {
         super(function.getName(), 2);
         this.function = function;   
     }
 
     public ArithFunctionTerm(ArithFunctionTerm af) {
        super(af); // clone args from af
         value    = af.value;
         function = af.function;
         agent    = af.agent;
     }
     
     public NumberTerm getValue() {
         return value;
     }
     
     @Override
     public boolean isNumeric() {
         return true;
     }
     
     @Override
     public boolean isAtom() {
         return false;
     }
     
     @Override
     public boolean isStructure() {
         return false;
     }
 
     @Override
     public boolean isLiteral() {
         return false;
     }
 
     @Override
     public boolean isArithExpr() {
         return !isEvaluated();
     }
 
     /** returns true if the function/expression was already evaluated */
     public boolean isEvaluated() {
         return value != null;
     }
 
     @Override
     public boolean isGround() {
         return isEvaluated() || super.isGround();
     }
 
     public boolean isUnary() {
         return getArity() == 1;
     }
     
     /**
      * Does a "normal" apply and then solve the expression and store the result,
      * so future calls of solve do not need to compute the value again
      */
     @Override
     public boolean apply(Unifier u) {
         if (isEvaluated()) return false;
         
         super.apply(u);
         if ((function != null && function.allowUngroundTerms()) || isGround()) {
             try {
                 value = new NumberTermImpl(solve());
                 return true;
             } catch (Exception e) {
                 logger.log(Level.SEVERE, getErrorMsg()+ " -- "+ e);
             }
         //} else {
         //  logger.warning(getErrorMsg()+ " -- this function has unground arguments and can not be evaluated! Unifier is "+u);
         }
         
         return false;
     }
 
     public void setAgent(Agent ag) {
         agent = ag;
     }
     public Agent getAgent() {
         return agent;
     }
     
     /** computes the value for this arithmetic function (as defined in the NumberTerm interface) */
     public double solve() {
         if (isEvaluated())
             return value.solve();
         else if (function != null)
             try {
                 return function.evaluate((agent == null ? null : agent.getTS()),getTermsArray());
             } catch (Exception e) {
                 logger.log(Level.SEVERE, getErrorMsg()+ " -- error in evaluate!", e);
             }
         else 
             logger.log(Level.SEVERE, getErrorMsg()+ " -- the function can not be evalutated, it has no function assigned to it!", new Exception());
         return 0;
     }
 
     public boolean checkArity(int a) {
         return function != null && function.checkArity(a);
     }
     
     @Override
     public Iterator<Unifier> logicalConsequence(Agent ag, Unifier un)  {
         logger.log(Level.WARNING, "Arithmetic term cannot be used for logical consequence!", new Exception());
         return LogExpr.EMPTY_UNIF_LIST.iterator();
     }
     
     @Override
     public boolean equals(Object t) {
         if (t == null) return false;
         if (isEvaluated()) return value.equals(t);
         return super.equals(t);
     }
 
     @Override
     public int compareTo(Term o) {
         try {
             NumberTerm st = (NumberTerm)o;
             if (solve() > st.solve()) return 1;
             if (solve() < st.solve()) return -1;
         } catch (Exception e) {}
         return 0;    
     }
 
     @Override
     protected int calcHashCode() {
         if (isEvaluated())
             return value.hashCode();
         else
             return super.calcHashCode();
     }
 
     @Override
     public String toString() {
         if (isEvaluated())
             return value.toString();
         else
             return super.toString();
     }
 
     @Override    
     public String getErrorMsg() {
         return "Error in '"+this+"' ("+ super.getErrorMsg() + ")";       
     }
 
     @Override
     public NumberTerm clone() {
        if (isEvaluated()) {
             return value;
        } else {
             return new ArithFunctionTerm(this);
        }
     } 
     
     public Element getAsDOM(Document document) {
         if (isEvaluated()) {
             return value.getAsDOM(document);
         } else {
             Element u = (Element) document.createElement("expression");
             u.setAttribute("type", "arithmetic");
             Element r = (Element) document.createElement("right");
             r.appendChild(super.getAsDOM(document)); // put the left argument indeed!
             u.appendChild(r);
             return u;
         }
     }
 }
