 package jason.asSyntax.directives;
 
 import jason.asSyntax.Plan;
 import jason.asSyntax.PlanLibrary;
 import jason.asSyntax.Pred;
 import jason.asSyntax.StringTerm;
import jason.asSyntax.Literal;
 import jason.asSyntax.parser.as2j;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Include implements Directive {
 
     static Logger logger = Logger.getLogger(Include.class.getName());
 
    public boolean process(Pred directive, List<Plan> innerPlans, List<Literal> bels, PlanLibrary pl) {
         return processInclude(((StringTerm)directive.getTerm(0)).getString(), bels, pl);
     }
 
     boolean processInclude(String asFileName, List bels, PlanLibrary pl) {
         try {
             as2j parser = new as2j(new FileInputStream(asFileName));
             parser.belief_base(bels);
             PlanLibrary newPl = parser.plan_base(bels);
             pl.addAll(newPl);
             logger.fine("as2j: AgentSpeak program '"+asFileName+"' parsed successfully!");
             return true;
         } catch (FileNotFoundException e) {
             logger.log(Level.SEVERE,"as2j: the AgentSpeak source file was not found", e);
         } catch (Exception e) {
             logger.log(Level.SEVERE,"as2j: error parsing \"" + asFileName + "\"", e);
         }
         return false;
     }
 }
