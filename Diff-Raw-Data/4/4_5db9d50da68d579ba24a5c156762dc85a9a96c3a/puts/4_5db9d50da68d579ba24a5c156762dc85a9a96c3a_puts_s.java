 package jason.stdlib;
 
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.InternalAction;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.ASSyntax;
 import jason.asSyntax.StringTerm;
 import jason.asSyntax.StringTermImpl;
 import jason.asSyntax.Term;
 import jason.asSyntax.parser.ParseException;
 
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * <p>
  * Internal action: <b><code>.puts</code></b>.
  * 
  * <p>
  * Description: used for printing messages to the console where the system is
  * running, or unifying the message to a variable parameter. It receives one
  * string parameter, containing escaped variable names that are replaced by
  * their bindings in the current intention's unifier. Terms are made ground
  * according to the current unifying function before being printed out. No new
  * line is printed after the parameters. In this version a user can also 
  * include any Jason expression (logical or arithmetic) that will be replaced
  * by it's evaluated value.
  * 
  * <p>
  * The precise format and output device of the message is defined by the Java
  * logging configuration as defined in the <code>logging.properties</code>
  * file in the project directory.
  * 
  * <p>
  * Parameters:
  * <ul>
  * 
  * <li>+message (string): the string to be printed out.</li>
  * <li>-output (any variable [optional]): the variable to print the processed
  * result.</li>
  * 
  * </ul>
  * 
  * <p>
  * Example:
  * <ul>
  * 
  * <li> <code>.puts("Testing variable #{A}")</code>: prints out to the
  * console the supplied string replacing #{A} with the value of variable A.</li>
  * <li> <code>.puts("Testing variable #{A}, into B", B)</code>: tries to unify 
  * B with the supplied string replacing #{A} with the value of variable A.</li>
  * <li> <code>.puts("The value of the expression is #{X+2}")</code>: prints out
  * the result of the X+2 expression. Assuming X is unified to a numeric value,
  * the printed result will be the sum of X and two, if X is unified to any 
  * other value, the original expression (X+2) will be printed.</li> 
  * 
  * </ul>
  * 
 * @see act.puts
  * @author Felipe Meneguzzi (http://www.meneguzzi.eu/felipe)
  * 
  */
 
 public class puts extends DefaultInternalAction {
 
     /**
      * 
      */
     private static final long serialVersionUID = 1L;
     private static InternalAction singleton = null;
 
     public static InternalAction create() {
         if (singleton == null)
             singleton = new puts();
         return singleton;
     }
 
     //Pattern regex = Pattern.compile("#\\{\\p{Upper}\\p{Alnum}*\\}");
    Pattern regex = Pattern.compile("#\\{\\p{ASCII}+\\}");
 
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args)
             throws Exception {
         if (!args[0].isString()) {
             return false;
         }
 
         StringBuffer sb = new StringBuffer();
         for (Term term : args) {
             if (!term.isString()) {
                 continue;
             }
             StringTerm st = (StringTerm) term;
             Matcher matcher = regex.matcher(st.getString());
 
             while (matcher.find()) {
                 /*
                  * System.out.println("I found the text \""+matcher.group()+ "\"
                  * starting at index "+matcher.start()+ " and ending at index
                  * "+matcher.end());
                  */
                 String sVar = matcher.group();
                 sVar = sVar.substring(2, sVar.length() - 1);
                 try {
                     Term t = ASSyntax.parseTerm(sVar);
                     //We use t.apply to evaluate any logical or arithmetic expression in Jason
                     t.apply(un);
                     matcher.appendReplacement(sb, t.toString());
                 } catch (ParseException pe) {
                     // TODO: handle exception
                     // TODO: Decide whether or not we should ignore the exception and print the call instead
                     // Right now, if I get a parse error from ASSyntax, I just print the original escaped
                     // sequence, so a user can see that his/her expression was problematic
                     matcher.appendReplacement(sb, "#{"+sVar+"}");
                 }
                 
             }
             matcher.appendTail(sb);
         }
 
         if (args[args.length - 1].isVar()) {
             StringTerm stRes = new StringTermImpl(sb.toString());
             return un.unifies(stRes, args[args.length - 1]);
         } else {
             ts.getLogger().info(sb.toString());
             return true;
         }
     }
 }
