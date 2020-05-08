 package jason.asSyntax.patterns.goal;
 
 import jason.asSemantics.Agent;
import jason.bb.BeliefBase;
 import jason.asSyntax.Literal;
 import jason.asSyntax.Plan;
 import jason.asSyntax.Pred;
 import jason.asSyntax.directives.Directive;
 import jason.asSyntax.directives.DirectiveProcessor;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Implementation of the  Maintenance Goal pattern (see DALT 2006 papper)
  * 
  * @author jomi
  */
 public class MG implements Directive {
 
     static Logger logger = Logger.getLogger(MG.class.getName());
     
     public Agent process(Pred directive, Agent outterContent, Agent innerContent) {
         try {
             Literal goal = Literal.parseLiteral(directive.getTerm(0).toString());
             Literal subDir;
             if (directive.getArity() > 1) {
                 subDir = Literal.parseLiteral(directive.getTerm(1).toString());
             } else {
                 subDir = Literal.parseLiteral("bc("+goal+")");
             }
             Directive sd = DirectiveProcessor.getDirective(subDir.getFunctor());
 
             // apply sub directive
             Agent newAg = sd.process(subDir, outterContent, innerContent); 
             if (newAg != null) {
                 // add bel g
                Literal ig = (Literal)goal.clone();
                ig.addAnnot(BeliefBase.TPercept);
             	newAg.addInitialBel(goal);
 
                 // add -g : true <- !g.
                 newAg.getPL().add(Plan.parse("-"+goal+" <- !"+goal+"."));
 
             	return newAg;
             }
         } catch (Exception e) {
             logger.log(Level.SEVERE,"Directive error.", e);
         }
         return null;
     }
 }
