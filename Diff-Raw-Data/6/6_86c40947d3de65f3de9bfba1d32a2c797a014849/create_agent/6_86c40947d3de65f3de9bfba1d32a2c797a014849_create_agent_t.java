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
 import jason.asSyntax.*;
 import jason.runtime.RuntimeServicesInfraTier;
 import jason.mas2j.ClassParameters;
 
 import java.io.File;
 
 /**
   <p>Internal action: <b><code>.create_agent</code></b>.
   
   <p>Description: creates another agent using the referred AgentSpeak source
   code.
   
   <p>Parameters:<ul>
   
   <li>+ name (atom): the name for the new agent.<br/>
   
   <li>+ source (string): path to the file where the AgentSpeak code
   for the new agent can be found.<br/>
 
   <li><i>+ customisations</i> (list -- optional): list of optional parameters
   as agent class, architecture and belief base.<br/>
 
   </ul>
   
   <p>Examples:<ul> 
 
   <li> <code>.create_agent(bob,"/tmp/x.asl")</code>: creates an agent named "bob" 
   from the source file in "/tmp/x.asl".</li>
 
   <li>
   <code>.create_agent(bob,"x.asl", [agentClass("myp.MyAgent")])</code>:
   creates the agent with customised agent class
   <code>myp.MyAgent</code>.</li>
 
   <code>.create_agent(bob,"x.asl", [agentArchClass("myp.MyArch")])</code>:
   creates the agent with customised architecture class
   <code>myp.MyArch</code>.</li>
 
   <code>.create_agent(bob,"x.asl", [beliefBaseClass("jason.bb.TextPersistentBB")])</code>:
   creates the agent with customised belief base
   <code>jason.bb.TextPersistentBB</code>.</li>
 
   <code>.create_agent(bob,"x.asl", [agentClass("myp.MyAgent"),
   agentArchClass("myp.MyArch"),
   beliefBaseClass("jason.bb.TextPersistentBB")])</code>: creates the
   agent with agent, acrchitecture and belief base customised.</li>
 
   </ul>
 
   @see jason.stdlib.kill_agent
   @see jason.stdlib.stopMAS
   @see jason.runtime.RuntimeServicesInfraTier
 */
 public class create_agent extends DefaultInternalAction {
 
     @Override public int getMinArgs() { return 2; }
     @Override public int getMaxArgs() { return 3; }
 
     @Override protected void checkArguments(Term[] args) throws JasonException {
         super.checkArguments(args); // check number of arguments
         if (!args[1].isString())
             throw JasonException.createWrongArgument(this,"second argument must be a string");
        if (args.length == 3 && !args[2].isList())
             throw JasonException.createWrongArgument(this,"third argument must be a list");  
     }
 
     @Override
     public Object execute(TransitionSystem ts, Unifier un, Term[] args) throws Exception {
         checkArguments(args);
         
         String name;
         if (args[0].isString()) 
             name = ((StringTerm)args[0]).getString();
         else
             name = args[0].toString();
         
         StringTerm source = (StringTerm)args[1];
 
         File fSource = new File(source.getString());
         if (!fSource.exists()) {
             throw new JasonException("The source file " + source + " was not found!");
         }
         String agClass = null;
         String agArchClass = null;
         ClassParameters bbPars = null;
         if (args.length > 2) { // optional parameter
             // get the parameters
             for (Term t: (ListTerm)args[2]) {
                 if (t.isStructure()) {
                     Structure s = (Structure)t;
                     if (s.getFunctor().equals("beliefBaseClass")) {
                         bbPars = new ClassParameters(testString(s.getTerm(0)));
                     } else if (s.getFunctor().equals("agentClass")) {
                         agClass = testString(s.getTerm(0)).toString();
                     } else if (s.getFunctor().equals("agentArchClass")) {
                         agArchClass = testString(s.getTerm(0)).toString();
                     }
                 }
             }
 
         }
         RuntimeServicesInfraTier rs = ts.getUserAgArch().getArchInfraTier().getRuntimeServices();
         return rs.createAgent(name, fSource.getAbsolutePath(), agClass, agArchClass, bbPars, ts.getSettings());
     }
     
     private Structure testString(Term t) {
         if (t.isStructure())
             return (Structure)t;
         if (t.isString())
             return Structure.parse(((StringTerm)t).getString());
         return null;
     }
 }
