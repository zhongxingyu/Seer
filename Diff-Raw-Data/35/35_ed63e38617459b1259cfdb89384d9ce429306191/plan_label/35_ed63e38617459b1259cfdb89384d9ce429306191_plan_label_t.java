 //----------------------------------------------------------------------------
 // Copyright (C) 2003  Rafael H. Bordini, Jomi F. Hubner, et al.
 // 
 // This library is free software; you can redistribute it and/or
 // modify it under the terms of the GNU Lesser General Public
 // License as published by the Free Software Foundation; either
 // version 2.1 of the License, or (at your option) any later version.
 // 
 // This library is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 // Lesser General Public License for more details.
 // 
 // You should have received a copy of the GNU Lesser General Public
 // License along with this library; if not, write to the Free Software
 // Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 // 
 // To contact the authors:
 // http://www.dur.ac.uk/r.bordini
 // http://www.inf.furb.br/~jomi
 //
 //----------------------------------------------------------------------------
 
 package jason.stdlib;
 
 import jason.JasonException;
 import jason.asSemantics.DefaultInternalAction;
 import jason.asSemantics.TransitionSystem;
 import jason.asSemantics.Unifier;
 import jason.asSyntax.Plan;
 import jason.asSyntax.StringTermImpl;
 import jason.asSyntax.Term;
 
 /**
   <p>Internal action: <b><code>.plan_label(<i>P</i>,<i>L</i>)</code></b>.
   
   <p>Description: unifies <i>P</i> with a string representing the plan
   labelled with the term <i>L</i> within the agent's plan library.
   
   <p>Parameters:<ul>
   
   <li>- plan (string): the string representing the plan.<br/>
   
   <li>+ label (structure): the label of that plan.<br/>
   
   </ul>
   
   <p>Example:<ul> 
 
   <li> <code>.plan_label(P,p1)</code>: unifies P with the string
   representation of the plan with label <code>p1</code>.</li>
 
   </ul>
 
   @see jason.stdlib.add_plan
   @see jason.stdlib.relevant_plans
   @see jason.stdlib.remove_plan
 
  */
 public class plan_label extends DefaultInternalAction {
 
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         try {
             Term label = args[1];
             Plan p = ts.getAg().getPL().get(label.toString());
            String ps = p.toASString().replaceAll("\"", "\\\\\"");
            return un.unifies(new StringTermImpl(ps), args[0]);
         } catch (ArrayIndexOutOfBoundsException e) {
             throw new JasonException("The internal action 'plan_label' has not received two arguments.");
         } catch (Exception e) {
             throw new JasonException("Error in internal action 'plan_label': " + e, e);
         }
     }
 }
