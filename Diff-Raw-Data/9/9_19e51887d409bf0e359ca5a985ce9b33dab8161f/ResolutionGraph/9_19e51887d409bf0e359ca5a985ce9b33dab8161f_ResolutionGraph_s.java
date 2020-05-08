 /*
  * Copyright 2011-2013 Adele Research Group (http://adele.imag.fr/) 
  * LIG Laboratory (http://www.liglab.fr)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 package fr.liglab.adele.cube.agent.defaults.resolver;
 
 import fr.liglab.adele.cube.agent.*;
 import fr.liglab.adele.cube.agent.defaults.ResolverImpl;
 import fr.liglab.adele.cube.metamodel.*;
 import fr.liglab.adele.cube.archetype.Archetype;
 import fr.liglab.adele.cube.archetype.Characteristic;
 import fr.liglab.adele.cube.archetype.Element;
 import fr.liglab.adele.cube.archetype.Objective;
 
 import java.util.Properties;
 import java.util.Stack;
 
 /**
  *
  *
  *
  * Author: debbabi
  * Date: 4/27/13
  * Time: 11:45 PM
  */
 public class ResolutionGraph {
 
     /**
      * Root variable.
      */
     private Variable root;
 
     /**
      * The resolver that creates this resolution graph.
      */
     private Resolver resolver;
 
     /**
      * The Cube Agent where this resolution graph will be executed.
      */
     private CubeAgent agent;
 
     /**
      * Constructor.
       * @param resolver
      */
     public ResolutionGraph(Resolver resolver) {
         this.resolver = resolver;
         this.agent = resolver.getCubeAgent();
     }
 
     /**
      * Sets the Root Variable of the current resolution graph.
      * @param var
      */
     public void setRoot(Variable var) {
         this.root = var;
     }
 
     /**
      * Sets the Root Variable of the current resolution graph.
      * @return
      */
     public Variable getRoot() {
         return this.root;
     }
 
     /**
      * Resolve the Constraints Graph.
      * perform the objectives.
      * @return
      */
     public boolean resolve() {
         if (root != null) {
             preProcessing();
             print();
             // For each objective constraint related to the root variable,
             // we try to resolve the specified objective constraints.
             for (Constraint o : root.getObjectiveConstraints()) {
                 info("*** trying to resolve the objective: " + o.getName());
                 if (resolveObjective(o) == false) {
                     // TODO: cancel applied constraints!!!!
                     info("*** objective '" + o.getName() +"' not resolved!");
                     return false;
                 }
                 info("*** the objective '" + o.getName() +"' is resolved!");
             }
         }
         return true;
     }
 
     /**
      * Find Objective constraints to be applied to the current root variable instance.
      */
     private void preProcessing() {
         info("pre processing...");
         Archetype archetype = this.agent.getArchetype();
         if (archetype != null) {
 
             // findValue all objectives for this element
             for (Objective obj : archetype.getObjectives()) {
                 info("checking objective " + obj.getName());
                 /*
                  * if the subject of the objective constraint has the same metamodel as the root variable
                  */
                 if (obj.getSubject().getName().equalsIgnoreCase(root.getName()) &&
                         obj.getNamespace().equalsIgnoreCase(root.getNamespace())) {
 
                     //info("ok the same element type! but... ");
                     // we check if all the descriptions in the archetype for this subject variable are met for the
                     // current instance
 
                     if (evaluateSubject(root, obj.getSubject())) {
                         Constraint c = addConstraint(root, obj);
                         info("the objective constraint '"+c.getName()+"' should be resolved");
                         Variable objVar = c.getObjectVariable();
 
                         /*
                          * if it is a binary constraint, we construct the graph's path.
                          */
                         if (objVar != null && !objVar.isPrimitive()) {
                             buildObject(objVar, (Element)obj.getObject());
                         }
                     } else {
                         info("the objective '"+obj.getName()+"' does not correspond to the root variable!");
                     }
                 }
             }
 
         }
     }
 
     /**
      * For the element 'e' specified on the archetype and which is a subject of an archetype objective,
      * evaluate if it corresponds to the actual variable to resolve 'var'.
      * @param var
      * @param e
      * @return
      */
     private boolean evaluateSubject(Variable var, Element e) {
         Stack<Constraint> stack = new Stack<Constraint>();
         if (!evaluateSubject(var, e, stack)) {
             // vider stack
             while (!stack.isEmpty()) {
                 Constraint c = stack.pop();
                 var.removeConstraint(c);
                 //System.out.println("** removing characteristic constraint for subject var: " + c.getName());
             }
             return false;
         } else {
             return true;
         }
     }
 
     /**
      * Recursive implementation of the evaluateSubject function.
      * @param var
      * @param e
      * @param stack
      * @return
      */
     private boolean evaluateSubject(Variable var, Element e, Stack<Constraint> stack) {
         if (var.values.size() > 0) {
             /* if only one predicate is false, return false*/
             for (Characteristic car : e.getUnaryCharacteristics()) {
                 Constraint c = addConstraint(var, car);
                 stack.push(c);
                 //System.out.println("** adding characteristic constraint for subject var: " + c.getName());
                 if (c.check(this.agent) == false)
                     return false;
                 //else
                 //System.out.println("** checked ok");
             }
             /*
             for (Characteristic car : e.getBinaryCharacteristics()) {
                 Constraint c = addConstraint(var, car);
                 System.out.println("** adding characteristic constraint for subject var: " + c.getName());
                 stack.push(c);
                 c.init(this.agent); // initialize object variable.
                 if (c.getObjectVariable().values.size() == 0)
                     // no initialization value from the subject var.
                     return false;
                 if (evaluateSubject(c.getObjectVariable(), (Element) car.getObject()) == false)
                     return false;
             } */
             return true;
         }
         return false;
     }
 
     private void buildObject(Variable var, Element e) {
 
         for (Characteristic car : e.getUnaryCharacteristics()) {
             addConstraint(var, car);
             //info("** adding unary characteristic constraint for subject var: " + c.getName());
         }
         for (Characteristic car : e.getBinaryCharacteristics()) {
             Constraint c = addConstraint(var, car);
             //info("** adding binary characteristic constraint for subject var: " + c.getName());
             if (c != null)
                 buildObject(c.getObjectVariable(), (Element)car.getObject());
         }
         for (Objective obj : this.agent.getArchetype().getObjectives()) {
             // TODO: à revoir!
             // - unary objectives should be added!
             if (obj.getSubject() == e /*&& (obj.getResolutionStrategy() != null && (obj.getResolutionStrategy().equalsIgnoreCase("f") || obj.getResolutionStrategy().equalsIgnoreCase("fc")))*/)  {
 
                 Constraint c = addConstraint(var, obj);
                 if (c != null) {
                     info("the objective constraint '"+c.getName()+"' is added to the description of the variable " + var.getName());
                     Variable objVar = c.getObjectVariable();
                             /*
                              * if it is a binary constraint, we construct the graph's path.
                              */
                     if (objVar != null && !objVar.isPrimitive()) {
                         buildObject(objVar, (Element)obj.getObject());
                     }
                 }
             }
         }
         /*
         for (Objective car : e.getUnaryObjectives()) {
             Constraint c = addConstraint(var, car);
             info("** adding unary characteristic(obj) constraint for subject var: " + c.getName());
         }
         for (Objective car : e.getBinaryObjectives()) {
             Constraint c = addConstraint(var, car);
             info("** adding binary characteristic(obj) constraint for subject var: " + c.getName());
             buildObject(c.getObjectVariable(), (Element)car.getObject());
         } */
 
     }
 
     /**
      * Resolve the Constraints Graph.
      * find a value for the root variable.
      * @return
      */
     public String find() {
         if (root != null) {
             //System.out.println("*********** findValue ************");
             //print();
             //System.out.println("*********************** variable: " +root.getTextualDescription());
             String tmp = findValue(root);
             //System.out.println("*********************** value: " +tmp );
             return tmp;
         }
         return null;
     }
 
     /**
      * Resolve the Constraints Graph.
      * create a new value for the root variable.
      * @return
      */
     public String create() {
         if (root != null) {
             //System.out.println("*********** create ************");
             print();
             //System.out.println("*********************** variable: " +root.getTextualDescription());
             String tmp = createUsingCharacteristics(root);
             if (evaluateValue(root) == true) {
                 //System.out.println("*********************** value created: " +tmp );
                 return tmp;
             }
         }
         return null;
     }
 
 
 
 
     /**
      * Resolve an objective constraint
      * @param objective
      * @return
      */
     private boolean resolveObjective(Constraint objective) {
         if (objective.isBinaryConstraint()) {
             // resolve and apply binary objective here..
             // 1. findValue value for the object variable
 
             String uuid = null;
             if (objective.getResolutionStrategy() == Constraint.FIND) {
                 uuid = findValue(objective.getObjectVariable());
             } else if (objective.getResolutionStrategy() == Constraint.FIND_OR_CREATE) {
                 uuid = findOrCreateValue(objective.getObjectVariable());
             } else if (objective.getResolutionStrategy() == Constraint.CREATE) {
                 uuid = createValue(objective.getObjectVariable());
             }
 
             while (uuid != null) {
                 info("** performing objective '"+objective.getName()+"' between '"+objective.getSubjectVariable().getValue().toString()+"' and '"+uuid+"'...");
                 objective.performObjective(this.agent);
                 if (evaluateValue(objective.getObjectVariable())) {
                     return true;
                 }
                 objective.cancelObjective(this.agent);
 
                 // findValue new value
                 if (objective.getResolutionStrategy() == Constraint.FIND) {
                     uuid = findValue(objective.getObjectVariable());
                 } else if (objective.getResolutionStrategy() == Constraint.FIND_OR_CREATE) {
                     uuid = findOrCreateValue(objective.getObjectVariable());
                 } else if (objective.getResolutionStrategy() == Constraint.CREATE) {
                     uuid = createValue(objective.getObjectVariable());
                 }
             }
 
             /*
              if no value is findValue for the object (related) variable, no solution is found.
              TODO: In the future, we can put it in the unresolved objectives, and try again
              after some moments.
              */
              info("The objective '" + objective.getName() + "' could not be resolved!");
              return false;
 
         }  else {
             // apply unary objective here..
             // TODO: c.performObjective()
             objective.performObjective(this.agent); // always?!
             return true;
         }
     }
 
 
     private String createUsingCharacteristics(Variable v) {
 
         if (v != null && v.getCubeAgent() != null) {
             if (v.getCubeAgent().equalsIgnoreCase(this.agent.getUri())) {
                 info("creating local new instance using caracteristics : " + v.getTextualDescription());
                 // apply binary constraints
                 Properties props = new Properties();
                 for (Property p : v.getProperties()) {
                     props.put(p.getName(), p.getValue());
                 }
                 try {
 
                     ManagedElement me = agent.newManagedElement(v.getNamespace(), v.getName(), props);
                     //System.out.println(me.getTextualDescription());
                     if (me != null) {
                         if (!v.values.contains(me.getUUID())) {
                             //agent.getRuntimeModel().add(me);
                             for (Reference r : v.getReferences()) {
                                 if (me.getReference(r.getName()) == null) {
                                     Reference tmp = me.addReference(r.getName(), r.isOnlyOne());
                                     if (tmp != null) {
                                         for (String re : r.getReferencedElements()) {
                                             tmp.addReferencedElement(re);
                                         }
                                     }
                                 } else {
                                     Reference tmp = me.getReference(r.getName());
                                     if (tmp != null) {
                                         for (String re : r.getReferencedElements()) {
                                             tmp.addReferencedElement(re);
                                         }
                                     }
                                 }
                             }
                             info("new instance..." + me.getTextualDescription());
 
                             v.setValue(me.getUUID());
                             return me.getUUID();
                         }
                     }
 
                 } catch (InvalidNameException e) {
                     e.printStackTrace();
                     return null;
                 } catch (PropertyExistException e) {
                     e.printStackTrace();
                     return null;
                 }
             } else {
                 // createValue remotely
                 info("... remote createValue");
                 String agent = v.getCubeAgent();
                 CMessage msg = new CMessage();
                 msg.setTo(agent);
                 msg.setObject("resolution");
                 msg.setBody("createUsingCharacteristics");
                 msg.setAttachement(v);
                 try {
                     //System.out.println("sending..." + msg.toString());
                     //System.out.println(v.getTextualDescription());
                     CMessage resultmsg = ((ResolverImpl)this.resolver).sendAndWait(msg);
                     if (resultmsg != null) {
                         //System.out.println("result..." + resultmsg.toString());
 
                         if (resultmsg.getBody() != null) {
                             this.agent.addExternalElement(resultmsg.getBody().toString(), resultmsg.getFrom());
                             info("createValue instance for variable '"+v.getName()+"' using characteristics ::: created (remote): " + resultmsg.getBody().toString());
                             v.setValue(resultmsg.getBody().toString());
                             if (evaluateValue(v)) {
                                 return resultmsg.getBody().toString();
                             }
                         }
                     }
                 } catch (TimeOutException e) {
                     e.printStackTrace();
                 }
             }
         }
         return null;
     }
 
     public String findUsingCharacteristics(Variable v) {
         info("findValue value for '"+v.getName()+"' using characteristics..\n"+v.getTextualDescription());
         if (v.getCubeAgent() != null) {
             if (v.getCubeAgent().equalsIgnoreCase(this.agent.getUri())) {
 
                 info("... local search");
                 // local search
                 // TODO only valid instancs?
                 for (ManagedElement me : this.agent.getRuntimeModel().getManagedElements(v.getNamespace(), v.getName(), ManagedElement.VALID)) {
                     if (v.values.contains(me.getUUID())) {
                         // bypass if already tested!
                         continue;
                     }
                     boolean equiv = true;
                     for (Property p : v.getProperties()) {
                         String pme = me.getProperty(p.getName());
                         if (pme != null && pme.equalsIgnoreCase(p.getValue())) {
                             continue;
                         } else {
                             equiv = false;
                             break;
                         }
                     }
                     if (equiv == true) {
                         info("findValue value for '"+v.getName()+"' using characteristics ::: found: " + me.getUri());
                         v.setValue(me.getUUID());
                         if (evaluateValue(v) == true) {
                             return me.getUUID();
                         } else {
                             info("the value '"+me.getUUID()+"' is not adequate!");
                         }
                     }
                 }
             } else {
                 info("... remote search");
                 String agent = v.getCubeAgent();
                 CMessage msg = new CMessage();
                 msg.setTo(agent);
                 msg.setFrom(this.agent.getUri());
                 msg.setReplyTo(this.agent.getUri());
                 msg.setObject("resolution");
                 msg.setBody("findUsingCharacteristics");
                 msg.setAttachement(v);
                 try {
 
                     //System.out.println(msg.toString());
 
                     CMessage resultmsg = ((ResolverImpl)this.resolver).sendAndWait(msg);
                     if (resultmsg != null) {
                         if (resultmsg.getBody() != null) {
                             this.agent.addExternalElement(resultmsg.getBody().toString(), resultmsg.getFrom());
                             info("findValue value for '"+v.getName()+"' using characteristics ::: found (remote): " + resultmsg.getBody().toString());
                             v.setValue(resultmsg.getBody().toString());
                             if (evaluateValue(v) == true) {
                                 return resultmsg.getBody().toString();
                             }
                         }
                     }
                 } catch (TimeOutException e) {
                     e.printStackTrace();
                 }
             }
         }
         info("findValue value for '"+v.getName()+"' using characteristics ::: not found!");
         return null;
     }
 
     public String findUsingBinaryConstraints(Variable v) {
         // findValue from related constraints.
         //String uuid = findUsingConstraints(v);
 
         for (Constraint bc : v.getBinaryConstraints()) {
             Variable ov = bc.getObjectVariable();
 
             String result = null;
             if (ov.hasValue() == false) {
                 // object variable has no value!
                 // we try first to findValue a value for it.
                 result = findValue(ov);
             }
             else {
                 // object variable has already a value. We try to call again the binary constraint to retrieve other
                 // value for the subject variable.
                 // if no value is returned, we change the object variable.
                 result = ov.getValue().toString();
             }
             while (result != null) {
                 info("findValue value for '"+bc.getSubjectVariable().getName()+"' using constraint '"+bc.getName()+"' ...");
 
                 String uuid = bc.find(agent);
                 while (uuid != null) {
                     v.setValue(uuid);
                     info("findValue value for '"+bc.getSubjectVariable().getName()+"' using constraint '"+bc.getName()+"' ::: found but not yet checked: " + uuid);
                     if (evaluateValue(v) == true) {
                         info("findValue value for '"+bc.getSubjectVariable().getName()+"' using constraint '"+bc.getName()+"' ::: found: " + uuid);
                         return uuid;
                     } else {
                         uuid = bc.find(agent);
                     }
                 }
                 info("findValue value for '"+bc.getSubjectVariable().getName()+"' using constraint '"+bc.getName()+"' ::: no value!");
                 result = findValue(ov);
             }
             // problem, we cannot findValue object variable!
             //info("step 2 : not found using binary constraints!");
             //v.findStep++;
             //return null;
 
         }
         return null;
     }
 
     /**
      * Find a value for a variable.
      * @return UUID
      */
     private String findValue(Variable v) {
         info("finding value for '" + v.getName()+"' ...");
         // start the finding process at the last known step.
         switch (v.findStep) {
             case 0: {
                 info("step 0 : applying characteristics..");
                 // apply characteristic constraints on the current variable to minimize the research domain.
                 applyUnaryDescriptions(v);
                 // go directly to step 1 below
                 v.findStep++;
             }
             case 1: {
                 info("step 1 :");
                 // findValue using the current configuration.
                 String uuid = findUsingCharacteristics(v);
                 if (uuid != null) {
 
                         //v.setValue(uuid);
                         //v.addAlreadyTestedValue(uuid);
                         //if (evaluateValue(v)) {
                         return uuid;
 
                     //} else {
                         // add to already tested values
                         // findValue again using the same technique until no value will be returned!
                     //    uuid = findUsingCharacteristics(v);
                     //}
                 }
                 //info("step 1 : not found using characteristics!");
                 info("step 2 : value for '"+v.getName()+"' not found using characteristics!");
                 v.findStep++;
             }
             case 2: {
                 info("step 2 : findValue value for '"+v.getName()+"' using binary constraints..");
                 String uuid = findUsingBinaryConstraints(v);
                 if (uuid != null) {
                     //v.values.push(uuid);
                     //v.addAlreadyTestedValue(uuid);
                     //if (evaluateValue(v)) {
                     return uuid;
                     //} else {
                     // add to already tested values
                     // findValue again using the same technique until no value will be returned!
                     //    uuid = findUsingCharacteristics(v);
                     //}
                 }
                 info("step 2 : value for '"+v.getName()+"' not found using binary constraints!");
                 v.findStep++;
 
             }
            default: {
                 // no solution was found!
                 return null;
             }
         }
     }
 
     /**
      * Find a value for a variable.
      * @return UUID
      */
     private String findOrCreateValue(Variable v) {
         info("finding value for '" + v.getName()+"' ...");
         // start the finding process at the last known step.
         switch (v.findStep) {
             case 0: {
                 info("step 0 : applying characteristics..");
                 // apply characteristic constraints on the current variable to minimize the research domain.
                 applyUnaryDescriptions(v);
                 // go directly to step 1 below
                 v.findStep++;
             }
             case 1: {
                 info("step 1 : ");
                 // findValue using the current configuration.
                 String uuid = findUsingCharacteristics(v);
                 if (uuid != null) {
                     v.values.push(uuid);
                     //v.addAlreadyTestedValue(uuid);
                     return uuid;
 
                 }
                 //info("step 1 : not found using characteristics!");
                 v.findStep++;
             }
             case 2: {
                 info("step 2 : findValue value for '"+v.getName()+"' using binary constraints..");
                 String uuid = findUsingBinaryConstraints(v);
                 if (uuid != null) {
                     v.values.push(uuid);
                     //v.addAlreadyTestedValue(uuid);
                     //if (evaluateValue(v)) {
                     return uuid;
                     //} else {
                     // add to already tested values
                     // findValue again using the same technique until no value will be returned!
                     //    uuid = findUsingCharacteristics(v);
                     //}
                 }
                 info("step 2 : value for '"+v.getName()+"' not found using binary constraints!");
                 v.findStep++;
             }
             case 3: {
                 // createValue an instance with the given characteristics.
                 info("step 3 : creating new '"+v.getName()+"' instance..");
 
                 //apply binary constraints
 
                 Object old = v.getValue();
 
                 for (Constraint c : v.getBinaryConstraints()) {
                     /*System.out.println("######### binary constraints related to '"+v.getName()+"' : " + c.getName());
                     for (Object s : c.getObjectVariable().values) {
                         System.out.println("- " + s);
                     }*/
                     if (old != null && c.getObjectVariable() != null && c.getObjectVariable().values.size() > 1) {
                         c.getObjectVariable().removeValue();
                     }
                     if (c.getObjectVariable() != null && c.getObjectVariable().hasValue()) {
                        // System.out.println("######### object.value: " + c.getObjectVariable().getValue());
                         //c.getObjectVariable().removeValue();
                         c.applyDescription(agent);
                         //System.out.println("######### " + c.getObjectVariable().getValue());
                         c.getObjectVariable().removeValue();
 
                     }
                 }
 
 
                 //System.out.println("######### old.value" + old);
                 String uuid = createUsingCharacteristics(v);
                 if (uuid != null) {
                     if (old != null ) {
 
                         if (agent.getRuntimeModelController().areSimilar(uuid, old.toString())) {
                             //System.out.println("######### old and new values similar!");
                             v.findStep++;
                             return uuid;
                         } else {
                             //System.out.println("old and new are not similar!");
                             //System.out.println("old :" + old);
                             //System.out.println("new :" + uuid);
                         }
                     }  else {
                         //System.out.println("old value null!");
                     }
 
                     v.values.push(uuid);
                     //c.getObjectVariable().removeValue();
                     return uuid;
                 }  else {
                     //System.out.println("create using characteristics return null!");
                 }
                 v.findStep++;
                 /*
                 String uuid = createUsingCharacteristics(v);
                 if (uuid != null) {
                     v.values.push(uuid);
                     v.findStep++;
                     return uuid;
                 }
                 */
                 //v.findStep++;
 
             }
             default: {
                 info("step 3 : error while creating an instance for variable '"+v.getName()+"'!");
                 // no solution was found!
                 return null;
             }
         }
     }
 
     /**
      * Find a value for a variable by creating it.
      * @return UUID
      */
     private String createValue(Variable v) {
         info("creating instance value for '" + v.getName()+"' ...");
         // start the finding process at the last known step.
         switch (v.findStep) {
             case 0: {
                 info("step 0 : applying characteristics..");
                 // apply characteristic constraints on the current variable to minimize the research domain.
                 applyUnaryDescriptions(v);
                 // go directly to step 1 below
                 v.findStep++;
             }
             case 1: {
                 // createValue an instance with the given characteristics.
                 info("step 3 : creating new '"+v.getName()+"' instance..");
 
                 //apply binary constraints
 
                 Object old = v.getValue();
 
                 for (Constraint c : v.getBinaryConstraints()) {
                     /*System.out.println("######### binary constraints related to '"+v.getName()+"' : " + c.getName());
                     for (Object s : c.getObjectVariable().values) {
                         System.out.println("- " + s);
                     }*/
                     if (old != null && c.getObjectVariable() != null && c.getObjectVariable().values.size() > 1) {
                         c.getObjectVariable().removeValue();
                     }
                     if (c.getObjectVariable() != null && c.getObjectVariable().hasValue()) {
                         // System.out.println("######### object.value: " + c.getObjectVariable().getValue());
                         //c.getObjectVariable().removeValue();
                         c.applyDescription(agent);
                         //System.out.println("######### " + c.getObjectVariable().getValue());
                         c.getObjectVariable().removeValue();
 
                     }
                 }
 
 
                 //System.out.println("######### old.value" + old);
                 String uuid = createUsingCharacteristics(v);
                 if (uuid != null) {
                     if (old != null ) {
 
                         if (agent.getRuntimeModelController().areSimilar(uuid, old.toString())) {
                             //System.out.println("######### old and new values similar!");
                             v.findStep++;
                             return uuid;
                         } else {
                             //System.out.println("old and new are not similar!");
                             //System.out.println("old :" + old);
                             //System.out.println("new :" + uuid);
                         }
                     }  else {
                         //System.out.println("old value null!");
                     }
 
                     v.values.push(uuid);
                     //c.getObjectVariable().removeValue();
                     return uuid;
                 }  else {
                     //System.out.println("create using characteristics return null!");
                 }
                 v.findStep++;
                 /*
                 String uuid = createUsingCharacteristics(v);
                 if (uuid != null) {
                     v.values.push(uuid);
                     v.findStep++;
                     return uuid;
                 }
                 */
                 //v.findStep++;
 
             }
             default: {
                 info("step 3 : error while creating an instance for variable '"+v.getName()+"'!");
                 // no solution was found!
                 return null;
             }
         }
     }
 
 
     /**
      * Evaluated the found value uuid against the variable's v constraints.
      *
      * @param v
      * @return
      */
     private boolean evaluateValue(Variable v) {
         info("checking object variable: " + v.getName());
         for (Constraint c : v.getConstraints()) {
             info("checking constraint: " + c.getName());
             if (c.isObjectiveConstraint()) {
                 if (c.isBinaryConstraint()) {
                     info("avoiding to check objective constraint '"+c.getName() + "' used as description constraint!");
                     continue;
                 }
             } else {
                 // check direct constraint
                 if (c.check(agent) == false)  {
                     info("constraint '"+c.getName()+"' returns FALSE!");
                     return false;
                 }
                 // check referenced elements
                 if (c.isBinaryConstraint()) {
 
                     if (evaluateValue(c.getObjectVariable()) == false) {
                         info("checking related value '"+c.getObjectVariable().getName()+"' of the variable '"+v.getName()+"' returns FALSe!");
                         return false;
                     }
 
                 }
             }
             info("constraint '" + c.getName()+"' TRUE");
         }
         return true;
     }
 
     /**
      * Apply characteristics specified by unary constraints to the given variable
      * to minimize the search domain space.
      * @param v
      */
     private void applyUnaryDescriptions(Variable v) {
         //System.out.println("/////////////////////// applyUnaryDescriptions: " + v.getName());
         for (Constraint c : v.getUnaryConstraints()) {
             //System.out.println("/////////////////////// unary constraint: " + c.getName());
             c.applyDescription(this.agent);
         }
     }
 
     private void info(String msg) {
         if (this.agent.getConfig().isDebug() == true) {
             System.out.println("[RESOLVER] " + msg);
         }
     }
 
 
 
 
 
     private Constraint addConstraint(Variable var, Objective objective) {
         Variable objvar = null;
         if (objective.getObject() instanceof Element) {
             // Binary
             Element obj = (Element)objective.getObject();
             objvar = new Variable(this.agent, obj.getNamespace(), obj.getName());
         } else {
             // Unary
             objvar = new Variable(this.agent, objective.getObject());
         }
         Constraint c = new Constraint(var, objective.getNamespace(), objective.getName(), objvar, true);
         if (objective.getResolutionStrategy() != null) {
             if (objective.getResolutionStrategy().equalsIgnoreCase("f")) {
                 c.setResolutionStrategy(Constraint.FIND);
             } else if (objective.getResolutionStrategy().equalsIgnoreCase("fc")) {
                 c.setResolutionStrategy(Constraint.FIND_OR_CREATE);
             } else if (objective.getResolutionStrategy().equalsIgnoreCase("c")) {
                 c.setResolutionStrategy(Constraint.CREATE);
             }
         }
         return c;
     }
 
     private Constraint addConstraint(Variable var, Characteristic car) {
         Variable objvar;
         if (car.getObject() != null) {
             if (car.getObject() instanceof Element) {
                 // Binary
                 Element obj = (Element)car.getObject();
                 objvar = new Variable(this.agent, obj.getNamespace(), obj.getName());
             } else {
                 // Unary
                 objvar = new Variable(this.agent, car.getObject().toString());
             }
             Constraint c = new Constraint(var, car.getNamespace(), car.getName(), objvar);
             return c;
         }
         return null;
     }
 
     private void print() {
         if (this.agent.getConfig().isDebug() == true) {
             info("The resolver will resolve the following Resolution Graph:");
             String msg = "................................................................................\n";
             if (root != null) {
                 msg += root.getName() + "\n";
                 for (Constraint c : root.getConstraints()) {
                     if (c.isObjectiveConstraint())
                         msg += printconstraint(c, "\t", true) + "\n";
                     else
                         msg += printconstraint(c, "\t", false) + "\n";
                 }
             }
             msg += "................................................................................\n";
             System.out.println(msg);
         }
     }
 
     private String printconstraint(Constraint c, String indation, boolean objective) {
         if (c != null) {
             if (c.isUnaryConstraint()) {
                 if (objective == true)
                     return (indation + "===" + c.getName() + "===> " + c.getObjectVariable().getValue() + " [" + c.getResolutionStrategyAsString() + "]\n");
                 else
                     return (indation + "---" + c.getName() + "---> (" + c.getObjectVariable().getValue() + ")");
             } else {
                 String tmp = "";
                 if (objective == true)
                     tmp += (indation + "===" + c.getName() + "===> " + c.getObjectVariable().getName()) + " [" + c.getResolutionStrategyAsString() + "]\n";
                 else
                     tmp += (indation + "---" + c.getName() + "---> " + c.getObjectVariable().getName()) + "\n";
                 for (Constraint cc : c.getObjectVariable().getConstraints()) {
                     tmp += printconstraint(cc, indation+"\t", false) + "\n";
                 }
                 return tmp;
             }
         }
         return "";
     }
 
 
 }
